/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.web.jspparser_ext;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.*;
import java.util.zip.ZipException;
import javax.servlet.ServletContext;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileStateInvalidException;

import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.Node;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;


import org.apache.jasper.Options;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.ExtractPageData;
import org.apache.jasper.compiler.GetParseData;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.apache.jasper.runtime.TldScanner;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.web.jspparser.ContextUtil;
import org.netbeans.modules.web.jspparser.JspParserImpl;
import org.netbeans.modules.web.jspparser.ParserServletContext;
import org.netbeans.modules.web.jspparser.WebAppParseProxy;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * Class that provides JSP parsing support for one web application. It caches
 * some useful data on a per-webapp basis.<br>
 *
 * Among other things, it does the following to correctly manage the development cycle:
 * <ul>
 *   <li>Creates the correct classloader for loading JavaBeans, tag handlers and other classes managed
 *      by the application.</li>
 *   <li>Caches the ServletContext (needed by the parser) corresponding to the application.</li>
 *   <li>Listens on changes in the application and releases caches as needed.</li>
 * </ul>
 * <p>UPDATE
 * <p>
 * Caching was changed - this class listens to the changes on FS and in libraries. The logic should be:
 * <ul>
 *   <li>for tld files: listen to FS changes and if it the change is related to "our" tld file, then release the cache
 *   <li>for web.xml: same as for tld files
 *   <li>for jar files: listen to the class path changes and behave similarly as for web.xml and tld files
 * </ul>
 * <p>
 * The cache behaviour could be probably improved (it means remove, update, add just one item, do not release
 * the whole cache).
 * @author Petr Jiricka, Tomas Mysik
 */
public class WebAppParseSupport implements WebAppParseProxy, PropertyChangeListener, ParserServletContext.WebModuleProvider {

    static final Logger LOG = Logger.getLogger(WebAppParseSupport.class.getName());
    private static final JspParserAPI.JspOpenInfo DEFAULT_JSP_OPEN_INFO = new JspParserAPI.JspOpenInfo(false, "8859_1"); // NOI18N
    // #134455 (see #67017 and #128360 as well)
    // commenting for NB 6.5M1, let's see whether some issues caused by this change will happen or whether this code can be removed
    //private static final Pattern RE_PATTERN_COMMONS_LOGGING = Pattern.compile(".*commons-logging.*\\.jar.*"); // NOI18N

    final JspParserImpl jspParser;
    // #85817
    private final Reference<WebModule> wm;
    final FileObject wmRoot;
    final FileObject webInf;
    private final FileSystemListener fileSystemListener;

    OptionsImpl editorOptions;
    OptionsImpl diskOptions;
    ServletContext editorContext;
    ServletContext diskContext;
    private JspRuntimeContext rctxt;
    private ParserClassLoader waClassLoader;
    private ParserClassLoader waContextClassLoader;

    // This is intended for tests exclusively!!!
    private static boolean noReset = false;

    // @GuardedBy(this)
    /**
     * The library mappings are cashed here.
     */
    final Map<String, String[]> mappings = new HashMap<String, String[]>();

    /**
     * Flag whether class path is current.
     * <p>
     * <ul><em>AtomicLong</em> and not <em>volatile boolean</em> because of the following race condition:
     * <li> class path changes
     * <li> task starts
     * <li> tast changes flag to 'synchronized'
     * <li> but a very short while before, the class path changes again
     * </ul>
     */
    final AtomicLong cpCurrent = new AtomicLong(0L);

    // request processor for cleaning mappings cache and reiniting options
    private static final int REINIT_OPTIONS_DELAY = 2000; // ms
    private static final int REINIT_CACHES_DELAY = 1000; // ms
    private static final int INITIAL_CACHES_DELAY = 2000; // ms
    private final RequestProcessor.Task reinitCachesTask;

    private final RequestProcessor.Task tldChangeTask;

    // #152482 - this escaped from constructor
    public static WebAppParseProxy create(JspParserImpl jspParser, WebModule wm) {
        WebAppParseSupport instance = new WebAppParseSupport(jspParser, wm);

        // register file listener (listen to changes of tld files, web.xml)
        try {
            FileSystem fs = wm.getDocumentBase().getFileSystem();
            fs.addFileChangeListener(FileUtil.weakFileChangeListener(instance.fileSystemListener, fs));
        } catch (FileStateInvalidException ex) {
            LOG.log(Level.INFO, null, ex);
        }

        // register weak class path listeners
        if (instance.wmRoot != null) { // in fact should not happen, see #154892 for more information
            ClassPath compileCP = ClassPath.getClassPath(instance.wmRoot, ClassPath.COMPILE);
            if (compileCP != null) {
                compileCP.addPropertyChangeListener(WeakListeners.propertyChange(instance, compileCP));
            }
            ClassPath executeCP = ClassPath.getClassPath(instance.wmRoot, ClassPath.EXECUTE);
            if (executeCP != null) {
                executeCP.addPropertyChangeListener(WeakListeners.propertyChange(instance, executeCP));
            }
        }

        return instance;
    }

    private WebAppParseSupport(JspParserImpl jspParser, WebModule wm) {
        this.jspParser = jspParser;
        this.wm = new WeakReference<WebModule>(wm);
        wmRoot = wm.getDocumentBase();
        webInf = wm.getWebInf();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Web pages: {0}", wmRoot);
            LOG.log(Level.FINE, "WEB-INF: {0}", webInf);
        }
        fileSystemListener = new FileSystemListener();

        // request procesor tasks
        RequestProcessor requestProcessor = new RequestProcessor("JSP parser :: Reinit caches"); // NOI18N
        reinitCachesTask = requestProcessor.create(new ReinitCaches());

        requestProcessor = new RequestProcessor("JSP parser :: TLD change"); // NOI18N
        tldChangeTask = requestProcessor.create(new TldChange());

        // init tag library cache
        reinitCachesTask.schedule(INITIAL_CACHES_DELAY);

        initOptions(true);
    }

    public JspParserAPI.JspOpenInfo getJspOpenInfo(FileObject jspFile, boolean useEditor
            /*, URLClassLoader waClassLoader*/) {
        // PENDING - do caching for individual JSPs
        //  in fact should not be needed - see FastOpenInfoParser.java
        checkReinitCachesTask();
        JspCompilationContext ctxt = createCompilationContext(jspFile, useEditor);
        ExtractPageData epd = new ExtractPageData(ctxt);
        try {
            return new JspParserAPI.JspOpenInfo(epd.isXMLSyntax(), epd.getEncoding());
        } catch (Exception e) {
            LOG.fine(e.getMessage());
        } finally {
            resetClassLoaders();
        }
        return DEFAULT_JSP_OPEN_INFO;
    }

    /**
     * Reinit options related to class path.
     * Method is always called in the current thread.
     */
    void reinitOptions() {
        assert Thread.holdsLock(this);
        initOptions(false);
    }

    private synchronized void initOptions(boolean firstTime) {
        long start = 0;
        if (LOG.isLoggable(Level.FINE)) {
            start = System.currentTimeMillis();
            Object[] objects = new Object[]{firstTime ? "" : "re", wmRoot != null ? FileUtil.toFile(wmRoot) : "<null>"}; // NOI18N
            LOG.log(Level.FINE, "JSP parser {0}initializing for WM {1}", objects); // NOI18N
        }
        WebModule webModule = wm.get();
        if (webModule == null) {
            // already gced
            LOG.fine("WebModule already GCed"); // NOI18N
            return;
        }
        editorContext = new ParserServletContext(wmRoot, this, true);
        diskContext = new ParserServletContext(wmRoot, this, false);
        editorOptions = new OptionsImpl(editorContext);
        diskOptions = new OptionsImpl(diskContext);
        rctxt = null;
        // try null, but test with tag files
        //new JspRuntimeContext(context, options);
        createClassLoaders();
        if (LOG.isLoggable(Level.FINE)) {
            Object[] objects = new Object[]{firstTime ? "" : "re", System.currentTimeMillis() - start}; // NOI18N
            LOG.log(Level.FINE, "JSP parser {0}initialized in {1} ms", objects); // NOI18N
        }
    }

    // #134455 (see #67017 and #128360 as well)
    // commenting for NB 6.5M1, let's see whether some issues caused by this change will happen or whether this code can be removed
    private boolean isUnexpectedLibrary(URL url) {
        return false;
//        Matcher m = RE_PATTERN_COMMONS_LOGGING.matcher(url.getFile());
//        return m.matches();
    }

    private void createClassLoaders() {
        Map<URL, URL> tomcatTable = new HashMap<URL, URL>();
        Map<URL, URL> loadingTable = new HashMap<URL, URL>();
        URL helpurl;

        FileObject actualWebInf = getWebInf();
        putWebInfLibraries(actualWebInf, tomcatTable, loadingTable);

        // issue 54845. On the class loader we must put the java sources as well. It's in the case, when there are a
        // tag hendler, which is added in a tld, which is used in the jsp file.
        if (wmRoot != null) {
            ClassPath cp = ClassPath.getClassPath(wmRoot, ClassPath.COMPILE);
            if (cp != null) {
                for (FileObject root : cp.getRoots()) {
                    helpurl = findInternalURL(root);
                    if (loadingTable.get(helpurl) == null && !isUnexpectedLibrary(helpurl)) {
                        loadingTable.put(helpurl, helpurl);
                        tomcatTable.put(helpurl, findExternalURL(root));
                    }
                }
            }
            // libraries and built classes are on the execution classpath
            cp = ClassPath.getClassPath(wmRoot, ClassPath.EXECUTE);
            if (cp != null) {
                for (FileObject root : cp.getRoots()) {
                    helpurl = findInternalURL(root);
                    if (loadingTable.get(helpurl) == null && !isUnexpectedLibrary(helpurl)) {
                        loadingTable.put(helpurl, helpurl);
                        tomcatTable.put(helpurl, findExternalURL(root));
                    }
                }
            }
        }
        if (actualWebInf != null) {
            FileObject classesDir = actualWebInf.getFileObject("classes");  //NOI18N
            if (classesDir != null) {
                helpurl = findInternalURL(classesDir);
                if (loadingTable.get(helpurl) == null) {
                    loadingTable.put(helpurl, helpurl);
                    tomcatTable.put(helpurl, helpurl);
                }
            }
        }

        URL[] loadingURLs = loadingTable.values().toArray(new URL[0]);
        URL[] tomcatURLs = tomcatTable.values().toArray(new URL[0]);

        waClassLoader = new ParserClassLoader(loadingURLs, tomcatURLs, getClass().getClassLoader());
        waContextClassLoader = new ParserClassLoader(loadingURLs, tomcatURLs, getClass().getClassLoader());
    }

    // #128360
    private void resetClassLoaders() {
        if (noReset) {
            return;
        }
        URL[] loadingURLs = waClassLoader.getLoadingURLs();
        URL[] tomcatURLs = waClassLoader.getTomcatURLs();
        URL[] loadingContextURLs = waContextClassLoader.getLoadingURLs();
        URL[] tomcatContextURLs = waContextClassLoader.getTomcatURLs();

        try {
            waClassLoader.close();
            waContextClassLoader.close();
        } catch (Exception e) {
            LOG.log(Level.WARNING, null, e);
        }
        waClassLoader = new ParserClassLoader(loadingURLs, tomcatURLs, waClassLoader.getParent());
        waContextClassLoader = new ParserClassLoader(loadingContextURLs, tomcatContextURLs, waContextClassLoader.getParent());
     }

    // #127379 - XXX review after listening on a web module is possible
    private FileObject getWebInf() {
        if (wmRoot == null) {
            return null;
        }
        WebModule webModule = WebModule.getWebModule(wmRoot);
        if (webModule != null) {
            return webModule.getWebInf();
        }
        return null;
    }

    // libraries in WEB-INF/lib
    // Looking for jars in WEB-INF/lib is mainly for tests. Can a user create a lib dir in the document base
    // and put here a jar?
    private void putWebInfLibraries(FileObject webInf, Map<URL, URL> tomcatTable, Map<URL, URL> loadingTable) {
        if (webInf == null) {
            return;
        }
        FileObject libDir = webInf.getFileObject("lib"); // NOI18N
        if (libDir == null) {
            return;
        }

        URL helpurl;
        Enumeration<? extends FileObject> libDirKids = libDir.getChildren(false);
        while (libDirKids.hasMoreElements()) {
            FileObject elem = libDirKids.nextElement();
            if (elem.getExt().equals("jar")) { // NOI18N
                helpurl = findInternalURL(elem);
                if (!isUnexpectedLibrary(helpurl)) {
                    tomcatTable.put(helpurl, helpurl);
                    loadingTable.put(helpurl, helpurl);
                }
            }
        }
    }

    private URL findInternalURL(FileObject fo) {
        URL url = URLMapper.findURL(fo, URLMapper.INTERNAL);
        return url;
    }

    private URL findExternalURL(FileObject fo) {
        // PENDING - URLMapper.EXTERNAL does not seem to be working now, so using this workaround
        File f = FileUtil.toFile(fo);
        if ((f != null)/* && (f.isDirectory())*/) {
            try {
                return f.toURI().toURL();
            } catch (MalformedURLException e) {
                LOG.log(Level.INFO, null, e);
            }
        }
        // fallback
        URL u = URLMapper.findURL(fo,  URLMapper.EXTERNAL);
        URL archiveFile = FileUtil.getArchiveFile(u);
        if (archiveFile != null) {
            return archiveFile;
        }
        return u;
    }

    private synchronized JspCompilationContext createCompilationContext(FileObject jspFile, boolean useEditor) {
        boolean isTagFile = determineIsTagFile(jspFile);
        String jspUri = getJSPUri(jspFile);
        Options options = useEditor ? editorOptions : diskOptions;
        ServletContext context = useEditor ? editorContext : diskContext;
        JspCompilationContext clctxt = null;
        try {
            if (isTagFile) {
                clctxt = new JspCompilationContext(jspUri, null,  options, context, null, rctxt, null);
            } else {
                clctxt = new JspCompilationContext(jspUri, false,  options, context, null, rctxt);
            }
        } catch (JasperException ex) {
            LOG.log(Level.INFO, null, ex);
        }
        clctxt.setClassLoader(getWAClassLoader());
        return clctxt;
    }

    boolean determineIsTagFile(FileObject fo) {
        if (fo.getExt().startsWith("tag")) { // NOI18N - all tag, tagx and even tagf are considered tag files
            return true;
        }
        if (JspParserAPI.TAG_MIME_TYPE.equals(fo.getMIMEType())) {
            return true;
        }
        return false;
    }

    private String getJSPUri(FileObject jsp) {
        // #154892
        if (wmRoot == null) {
            // what to return???
            return "/"; // NOI18N
        }
        return ContextUtil.findRelativeContextPath(wmRoot, jsp);
    }

    // from JspCompileUtil
    public JspParserAPI.ParseResult analyzePage(FileObject jspFile, /*String compilationURI, */
            int errorReportingMode) {
        // PENDING - do caching for individual JSPs
        checkReinitCachesTask();
        JspCompilationContext ctxt = createCompilationContext(jspFile, true);

        try {
            return callTomcatParser(jspFile, ctxt, waContextClassLoader, errorReportingMode);
        } finally {
            resetClassLoaders();
        }
    }

    /**
     * Returns the mapping of the 'global' tag library URI to the location (resource
     * path) of the TLD associated with that tag library. The location is
     * returned as a String array:
     *    [0] The location
     *    [1] If the location is a jar file, this is the location of the tld.
     */
    public synchronized Map<String, String[]> getTaglibMap(boolean useEditor) throws IOException {
        // useEditor not needed, both caches are the same
        // we have to clear all the caches when the class path is not current or the taglibmap is not initialized yet

        checkReinitCachesTask();
        // XXX return deep copy (not needed now, only jsp editor uses it)
        return new HashMap<String, String[]>(mappings);
    }

    /**
     * Returns the classloader to be used by the JSP parser.
     * This classloader loads the classes belonging to the application
     * from both expanded directory structures and jar files.
     */
    public synchronized URLClassLoader getWAClassLoader() {
        if (cpCurrent.get() != 0L) {
            reinitOptions();
        }
        // XXX #128360 - classloader is leaking out - no chance to close it
        return waClassLoader;
    }

    @Override
    public boolean isValid(WebModule wm) {
        boolean valid = wmRoot != null
                && wmRoot.isValid()
                && webInf != null
                && webInf.isValid()
                && wmRoot.equals(wm.getDocumentBase())
                && webInf.equals(wm.getWebInf());
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Checking validity - WM root: {0}, WEB-INF: {1}, valid: {2}", new Object[] {wmRoot, webInf, valid}); // NOI18N
        }
        return valid;
    }

    public class RRef {
        JspParserAPI.ParseResult result;
    }

    private JspParserAPI.ParseResult callTomcatParser(final FileObject jspFile,
            final JspCompilationContext ctxt, final ClassLoader contextClassLoader, final int errorReportingMode) {

        final RRef resultRef = new RRef();

        // calling the parser in a new thread, as per the spec, we need to set the context classloader
        // to contain the web application classes. Jasper really relies on this
        Thread compThread = new Thread("JSP Parsing") { // NOI18N

            private void setResult(GetParseData gpd){
                PageInfo nbPageInfo = gpd.getNbPageInfo();
                Node.Nodes nbNodes = gpd.getNbNodes();
                Throwable e = gpd.getParseException();
                if (e == null) {
                    resultRef.result = new JspParserAPI.ParseResult(nbPageInfo, nbNodes);
                } else {
                    // the exceptions we may see here:
                    // JasperException - usual
                    // ArrayIndexOutOfBoundsException - see issue 20919
                    // Throwable - see issue 21169, related to Tomcat bug 7124
                    // XXX has to be returned back to track all errors
                    Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(WebAppParseSupport.class, "MSG_errorDuringJspParsing")); // NOI18N
                    LOG.fine(e.getMessage());
                    LOG.log(Level.FINE, null, e);
                    JspParserAPI.ErrorDescriptor error = constructErrorDescriptor(e, wmRoot, jspFile);
                    resultRef.result = new JspParserAPI.ParseResult(nbPageInfo, nbNodes, new JspParserAPI.ErrorDescriptor[] {error});
                }
            }

            @Override
            public void run() {
                GetParseData gpd = null;
                try {
                    gpd = new GetParseData(ctxt, errorReportingMode);
                    gpd.parse();
                    setResult(gpd);

                } catch (ThreadDeath td) {
                    LOG.log(Level.INFO, null, td);
                    throw td;
                } catch (Throwable t) {
                    if (gpd != null) {
                        if (gpd.getParseException() == null) {
                            gpd.setParseException(t);
                        } else {
                            LOG.log(Level.INFO, null, t);
                        }
                        setResult(gpd);
                    } else {
                        LOG.log(Level.INFO, null, t);
                    }
                }
            }
        };
        compThread.setContextClassLoader(contextClassLoader);
        compThread.start();
        try {
            compThread.join();
            return resultRef.result;
        } catch (InterruptedException e) {
            JspParserAPI.ErrorDescriptor error = constructErrorDescriptor(e, wmRoot, jspFile);
            return new JspParserAPI.ParseResult(new JspParserAPI.ErrorDescriptor[] {error});
        }
    }

    private static JspParserAPI.ErrorDescriptor constructErrorDescriptor(Throwable e, FileObject wmRoot,
            FileObject jspPage) {
        JspParserAPI.ErrorDescriptor error = null;
        try {
            error = constructJakartaErrorDescriptor(wmRoot, jspPage, e);
        } catch (FileStateInvalidException e2) {
            LOG.log(Level.INFO, null, e2);
            // do nothing, error will just remain to be null
        } catch (IOException e2) {
            LOG.log(Level.INFO, null, e2);
            // do nothing, error will just remain to be null
        }
        if (error == null) {
            error = new JspParserAPI.ErrorDescriptor(wmRoot, jspPage, -1, -1,
                    ContextUtil.getThrowableMessage(e, !(e instanceof JasperException)), "");
        }
        return error;
    }

    /** Returns an ErrorDescriptor for a compilation error if the throwable was thrown by Jakarta,
     * otherwise returns null. */
    private static JspParserAPI.ErrorDescriptor constructJakartaErrorDescriptor(FileObject wmRoot, FileObject jspPage,
            Throwable ex) throws IOException {

        // PENDING: maybe we should check all nested exceptions
        Throwable last = ex;
        while (ex instanceof JasperException) {
            last = ex;
            ex = ((JasperException) ex).getRootCause();
            if (ex != null) {
                ErrorManager.getDefault().annotate(last, ex);
            }
        }

        if (ex == null) {
            ex = last;
        }

        // now it can be JasperException, which starts with error location description
        String m1 = ex.getMessage();
        if (m1 == null) {
            return null;
        }
        int lpar = m1.indexOf('('); // NOI18N
        if (lpar == -1) {
            return null;
        }
        int comma = m1.indexOf(',', lpar); // NOI18N
        if (comma == -1) {
            return null;
        }
        int rpar = m1.indexOf(')', comma); // NOI18N
        if (rpar == -1) {
            return null;
        }
        String line = m1.substring(lpar + 1, comma).trim();
        String col = m1.substring(comma + 1, rpar).trim();
        String fileName = m1.substring(0, lpar);

        // now cnstruct the FileObject using this file name and the web module root
        File file = FileUtil.toFile(wmRoot);
        FileObject errorFile = jspPage; // a sensible default

        fileName = new File(fileName).getCanonicalPath();
        String wmFileName = file.getCanonicalPath();
        if (fileName.startsWith(wmFileName)) {
            String errorRes = fileName.substring(wmFileName.length());
            errorRes = errorRes.replace(File.separatorChar, '/'); // NOI18N
            if (errorRes.startsWith("/")) { // NOI18N
                errorRes = errorRes.substring(1);
            }
            FileObject errorTemp = wmRoot.getFileObject(errorRes);
            if (errorTemp != null) {
                errorFile = errorTemp;
            }
        }

        // now construct the ErrorDescriptor
        try {
            String errContextPath = ContextUtil.findRelativeContextPath(wmRoot, errorFile);
            // some information in the message are unnecessary
            int index = m1.indexOf("PWC");  //NOI18N
            String errorMessage;
            if (index > -1) {
                index = m1.indexOf(':', index); // NOI18N
                errorMessage = m1.substring(index + 2).trim();
            } else {
                errorMessage = errContextPath + " [" + line + ";" + col + "] " + m1.substring(rpar + 1).trim(); // NOI18N
            }
            return new JspParserAPI.ErrorDescriptor(
                    wmRoot, errorFile, Integer.parseInt(line), Integer.parseInt(col),
                    errorMessage, ""); // NOI18N
            // pending - should also include a line of code
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Handles the event of property change of class path (source, run).
     */
    public void propertyChange(PropertyChangeEvent evt) {
        // classpath has channged => invalidate cache
        LOG.fine("Class path has changed");
        cpCurrent.incrementAndGet();
        reinitCachesTask.schedule(REINIT_OPTIONS_DELAY);
    }

    private void clearTagLibraryInfoCache() {
        assert Thread.holdsLock(this);
        // clear the cache of tagLibrary map
        Map<String, TagLibraryInfo> map = (ConcurrentHashMap<String, TagLibraryInfo>) editorContext
                .getAttribute("com.sun.jsp.taglibraryCache"); // NOI18N
        if (map != null) {
            map.clear();
        }
        map = (ConcurrentHashMap<String, TagLibraryInfo>) diskContext.getAttribute("com.sun.jsp.taglibraryCache"); // NOI18N
        if (map != null) {
            map.clear();
        }
    }

    void reinitTagLibMappings() {
        assert Thread.holdsLock(this);
        try {
            // editor options
            if (editorOptions == null) {
                // issue #213548 - It looks that REINIT task was called earlier than the
                // editorOptions initialization happened. It means that the #INITIAL_CACHES_DELAY
                // delay wasn't long enough. In that case it is rescheduled the task again and
                // finished current processing. This happens until the initialization procedure
                // is comleted and the editorOptions initialized.
                LOG.info("JSP parser :: EditorOption not initialized yet - reschedule reinit caches");
                reinitCachesTask.schedule(REINIT_CACHES_DELAY);
                return;
            }
            TldScanner lc = editorOptions.getTldScanner();

            Field mappingsField = TldScanner.class.getDeclaredField("mappings"); //NOI18N
            mappingsField.setAccessible(true);
            // Before new parsing, the old mappings in the TldLocationCache has to be cleared. Else there are
            // stored the old mappings.
            Map<String, String[]> tmpMappings = (Map<String, String[]>) mappingsField.get(lc);
            // the mapping doesn't have to be initialized yet
            if (tmpMappings != null) {
                tmpMappings.clear();
            }

            Thread compThread = new WebAppParseSupport.InitTldLocationCacheThread(lc);
            compThread.setContextClassLoader(waContextClassLoader);
            long start = 0;
            if (LOG.isLoggable(Level.FINE)) {
                start = System.currentTimeMillis();
                LOG.fine("InitTldLocationCacheThread start"); //NOI18N
            }
            compThread.start();

            try {
                compThread.join();
                if (LOG.isLoggable(Level.FINE)) {
                    long end = System.currentTimeMillis();
                    LOG.log(Level.FINE, "InitTldLocationCacheThread finished in {0} ms", (end - start)); //NOI18N
                }
            } catch (InterruptedException e) {
                LOG.log(Level.INFO, null, e);
            }

            // obtain the current mappings after parsing
            tmpMappings = (Map<String, String[]>) mappingsField.get(lc);

            // disk options
            lc = editorOptions.getTldScanner();

            mappingsField = TldScanner.class.getDeclaredField("mappings"); //NOI18N
            mappingsField.setAccessible(true);
            // store the same tld cache into disk options as well
            mappingsField.set(lc, tmpMappings);

            // update cache
            mappings.clear();
            if (tmpMappings != null){
//                System.err.println("****** tmpMappings != null");
                mappings.putAll(tmpMappings);
            }
            // cache tld files under WEB-INF directory as well
            mappings.putAll(getImplicitLocation());
        } catch (NoSuchFieldException e) {
            LOG.log(Level.INFO, null, e);
        } catch (IllegalAccessException e) {
            LOG.log(Level.INFO, null, e);
        }
    }

    /**
     * Returns map with tlds, which doesn't have defined <uri>.
     */
    private Map<String, String[]> getImplicitLocation() {
        assert Thread.holdsLock(this);
        Map<String, String[]> returnMap = new HashMap<String, String[]>();
        // Obtain all tld files under WEB-INF folder
        FileObject fo;
        if (webInf != null && webInf.isFolder() && webInf.isValid()) {
            Enumeration<? extends FileObject> en = webInf.getChildren(true);
            while (en.hasMoreElements()) {
                fo = en.nextElement();
                if (fo.getExt().equals("tld") && fo.isValid()) { // NOI18N
                    String path;
                    if (wmRoot != null && ContextUtil.isInSubTree(wmRoot, fo)) {
                        path = "/" + ContextUtil.findRelativePath(wmRoot, fo); // NOI18N
                    } else {
                        // the web-inf folder is mapped somewhere else
                        path = "/WEB-INF/" + ContextUtil.findRelativePath(webInf, fo); //NOI18N
                    }
                    returnMap.put(path, new String[] {path, wmRoot.getPath() + path});
                }
            }
        }
        return returnMap;
    }

    private synchronized void checkReinitCachesTask() {
        // process sliding task
        if (!reinitCachesTask.isFinished()) {
            reinitCachesTask.cancel();
            reinitCaches();
        }
    }

    void reinitCaches() {
        assert Thread.holdsLock(this);
        final long counter = cpCurrent.get();
        LOG.log(Level.FINE, "Current class path: {0}", (counter == 0L)); // NOI18N
        if (counter != 0L) {
            reinitOptions();
            boolean cpSet = cpCurrent.compareAndSet(counter, 0L);
            LOG.log(Level.FINE, "Class path {0}set to current", (cpSet ? "" : "*NOT* ")); // NOI18N
        }
        clearTagLibraryInfoCache();
        reinitTagLibMappings();
        tldChangeTask.run();
    }

    final class ReinitCaches implements Runnable {
        public void run() {
            synchronized (WebAppParseSupport.this) {
                LOG.fine("ReinitCaches task started"); // NOI18N
                reinitCaches();
                LOG.fine("ReinitCaches task finished"); // NOI18N
            }
        }
    }

    final class TldChange implements Runnable {
        public void run() {
            WebModule webModule = wm.get();
            if (webModule == null) {
                return;
            }
            LOG.fine("TLD change fired"); // NOI18N
            jspParser.fireChange(webModule);
        }
    }

    public static class JasperSystemClassLoader extends URLClassLoader {
        private static final java.security.AllPermission ALL_PERM = new AllPermission();

        public JasperSystemClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        protected PermissionCollection getPermissions(CodeSource codesource) {
            PermissionCollection perms = super.getPermissions(codesource);
            perms.add(ALL_PERM);
            return perms;
        }
    }

    /**
     * This classloader does some security stuff, but equally importantly, it does one horrible hack,
     * explained in the constructor Javadoc.
     * <p>
     * The classloader is hiding some resources to avoid commons logging
     * related jar locking. See issue 128360.
     */
    public static class ParserClassLoader extends URLClassLoader {

        private static final Logger LOGGER = Logger.getLogger(ParserClassLoader.class.getName());

        private static final java.security.AllPermission ALL_PERM = new AllPermission();

        /*
         * All three following constants are used to avoid not necessary jar
         * locking (just due to commons logging).
         */
        private static final String LOGGING_CONFIG = "commons-logging.properties"; // NOI18N
        private static final String SERVICES_FOLDER = "META-INF/services/"; // NOI18N
        // suppress annoying warning
        private static final List<String> LOG4J_CONFIGS = Arrays.asList("log4j.properties", "log4j.xml"); // NOI18N

        private final URL[] tomcatURLs;

        private final ClassLoader parent;

        /** This constructor and the getURLs() method is one horrible hack. On the one hand, we want to give
         * Tomcat a JarURLConnection when it attempts to load classes from jars, on the other hand
         * we don't want this classloader to load classes using JarURLConnection, as that's buggy.
         * We want it to load classes using internal nb: protocols. So the getURLs() method
         * returns an "external" list of URLs for Tomcat, while internally, the classloader uses
         * an "internal" list of URLs.
         */
        public ParserClassLoader(URL[] classLoadingURLs, URL[] tomcatURLs, ClassLoader parent) {
            super(classLoadingURLs, parent);
            this.parent = parent;
            this.tomcatURLs = tomcatURLs;
        }

        /** See the constructor for explanation of what thie method does.
         */
        @Override
        public URL[] getURLs() {
            return tomcatURLs;
        }

        public URL[] getLoadingURLs() {
            return super.getURLs();
        }

        public URL[] getTomcatURLs() {
            return tomcatURLs;
        }

        @Override
        protected PermissionCollection getPermissions(CodeSource codesource) {
            PermissionCollection perms = super.getPermissions(codesource);
            perms.add(ALL_PERM);
            return perms;
        }

        @Override
        public URL getResource(String name) {
            if (LOG4J_CONFIGS.contains(name)) {
                return null;
            }
            URL url;
            if (parent != null) {
                url = parent.getResource(name);
            } else {
                url = findResource(name);
            }
            return url;
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            // #139257 - see http://hg.netbeans.org/main/rev/8e2336205ff3
            if (name.equals("META-INF/services/javax.xml.stream.XMLInputFactory")) { // NOI18N
                // StAX is different; defined and implemented in BEA-specific code with different
                // search logic and an unusable fallback implementation.
                //return super.getResourceAsStream(name); // noop here, in jsp parser
            } else if (name.startsWith("META-INF/services/javax.xml.")) { // NOI18N
                // For JAXP services defined and implemented in the JRE,
                // this is the only workaround for JDK 5 & 6 to load the JRE's copy.
                return new ByteArrayInputStream(new byte[0]);
            }
            URL url = getResource(name);
            try {
                if (url != null) {
                    URLConnection conn = url.openConnection();
                    conn.setUseCaches(false);
                    return conn.getInputStream();
                }
                return null;
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        public URL findResource(String name) {
            if (LOGGING_CONFIG.equals(name) || name.startsWith(SERVICES_FOLDER)) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.log(Level.FINER, "Discarding request for {0}", name); // NOI18N
                }
                return null;
            }
            return super.findResource(name);
        }

        @Override
        public Enumeration<URL> findResources(String name) throws IOException {
            if (LOGGING_CONFIG.equals(name) || name.startsWith(SERVICES_FOLDER)) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.log(Level.FINEST, "Discarding request for {0}", name); // NOI18N
                }
                return Collections.enumeration(Collections.<URL>emptyList());
            }
            return super.findResources(name);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            sb.append(", parent : "); // NOI18N
            sb.append(getParent().toString());
            return sb.toString();
        }
    }

    private static class InitTldLocationCacheThread extends Thread {

        private final TldScanner cache;
        private static final TldScannerLoggerFilter LOGGER_FILTER = new TldScannerLoggerFilter();

        InitTldLocationCacheThread(TldScanner lc) {
            super("Init TldLocationCache"); // NOI18N
            cache = lc;
        }

        @Override
        public void run() {
            try {
                // #188702
//                Field mappings = TldScanner.class.getDeclaredField("mappings"); // NOI18N
//                mappings.setAccessible(true);
//                mappings.set(cache, null);
//                cache.getLocation(""); // NOI18N

                // see #205387 - adjust TldScanner logger
                updateTldScannerLogger();
                Method mappingsMethod = TldScanner.class.getDeclaredMethod("scanTlds"); //NOI18N
                mappingsMethod.setAccessible(true);
                mappingsMethod.invoke(cache);
            } catch (IllegalArgumentException ex) {
                LOG.log(Level.WARNING, null, ex);
            } catch (InvocationTargetException ex) {
                LOG.log(Level.INFO, null, ex);
            } catch (NoSuchMethodException ex) {
                LOG.log(Level.WARNING, null, ex);
            } catch (SecurityException ex) {
                LOG.log(Level.WARNING, null, ex);
            } catch (IllegalAccessException e) {
                LOG.log(Level.WARNING, null, e);
            }
        }

        private static void updateTldScannerLogger() {
            Logger tldScannerLogger = Logger.getLogger(TldScanner.class.getName());
            if (tldScannerLogger.getFilter() != LOGGER_FILTER) {
                tldScannerLogger.setFilter(LOGGER_FILTER);
            }
        }
    }

    public static class TldScannerLoggerFilter implements Filter {

        @Override
        public boolean isLoggable(LogRecord record) {
            Throwable thrown = record.getThrown();
            if (thrown == null) {
                return true;
            }
            if (thrown instanceof ZipException) {
                LOG.log(Level.INFO, "TldScanner's ZipException: ", thrown); //NOI18N
                return false;
            } else if (thrown instanceof FileNotFoundException) {
                // see issues #214308, #205387
                LOG.log(Level.INFO, "TldScanner's FileNotFoundException: ", thrown); //NOI18N
                return false;
            }
            return true;
        }

    }

    final class FileSystemListener extends FileChangeAdapter {

        @Override
        public void fileChanged(FileEvent fe) {
            processFileChange(fe);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            processFileChange(fe);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            processFileChange(fe);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            processFileChange(fe);
        }

        private void processFileChange(final FileEvent fe) {
            // check the file type/name/extension and then
            // check if the file belongs to this parse proxy
            FileObject fo = fe.getFile();
            if (fo.getExt().equals("tld") // NOI18N
                    || fo.getNameExt().equals("web.xml") // NOI18N
                    || determineIsTagFile(fo)) { // #109478
                if ((wmRoot != null && FileUtil.isParentOf(wmRoot, fo))
                        || (webInf != null && FileUtil.isParentOf(webInf, fo))) {
                    LOG.log(Level.FINE, "File {0} has changed, reinitCaches() called", fo); // NOI18N
                    // our file => process caches
                    // #133702
                    if (fo.getNameExt().equals("web.xml")) { // NOI18N
                        // clear JspConfig cache as well
                        cpCurrent.incrementAndGet();
                    }
                    reinitCachesTask.schedule(REINIT_CACHES_DELAY);
                }
            }
        }
    }

    public WebModule getWebModule() {
        return wm.get();
    }
}
