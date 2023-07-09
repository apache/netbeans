/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.source;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.Module;
import org.netbeans.modules.java.source.base.Bundle;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.parsing.CachingArchiveProvider;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.Convertors;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * Utilities to assist with retrieval of Javadoc text.
 */
public class JavadocHelper {

    private static final Logger LOG = Logger.getLogger(JavadocHelper.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(JavadocHelper.class.getName(), 1);
    private static final int DEFAULT_REMOTE_CONNECTION_TIMEOUT = 30;   //30s
    private static final int DEFAULT_REMOTE_FILE_CONTENT_CACHE_SIZE = 50;
    private static final int REMOTE_FILE_CONTENT_CACHE_SIZE = Integer.getInteger(
        "JavadocHelper.remoteCache.size",   //NOI18N
        DEFAULT_REMOTE_FILE_CONTENT_CACHE_SIZE);
    private static final int REMOTE_CONNECTION_TIMEOUT = Integer.getInteger(
        "JavadocHelper.remote.timeOut",   //NOI18N
        DEFAULT_REMOTE_CONNECTION_TIMEOUT) * 1000;

    /**
     * Remote Javadoc handling policy.
     * @since 0.138
     */
    public enum RemoteJavadocPolicy {
        /**
         * The connection to remote Javadoc is verified and when valid it's returned.
         */
        USE,
        /**
         * The connection to remote Javadoc is ignored.
         */
        IGNORE,
        /**
         * The {@link RemoteJavadocException} is thrown in case of remote Javadoc.
         */
        EXCEPTION,
        /**
         * All possible connections to remote Javadoc are returned without verification.
         */
        SPECULATIVE
    }

    /**
     * A RemoteJavadocException is thrown in case of remote Javadoc with {@link RemoteJavadocPolicy#EXCEPTION} policy.
     * @since 0.138
     */
    public static final class RemoteJavadocException extends Exception {
        private final URL root;

        /**
         * Creates a new RemoteJavadocException.
         * @param root the remote Javadoc root
         */
        public RemoteJavadocException(@NullAllowed URL root) {
            this.root = root;
        }

        /**
         * Returns the remote Javadoc root.
         * @return the root
         */
        @CheckForNull
        public URL getRoot() {
            return root;
        }
    }

    private JavadocHelper() {}
    
    /**
     * A reopenable stream of text from a particular location.
     * You <em>must</em> either call {@link #close}, or call {@link #openStream}
     * (and {@linkplain InputStream#close close} it) at least once.
     */
    public static final class TextStream {
        private static Map<URI,byte[]> remoteFileContentCache = Collections.synchronizedMap(
            new LinkedHashMap<URI, byte[]>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<URI, byte[]> eldest) {
                    return size() > REMOTE_FILE_CONTENT_CACHE_SIZE;
                }
            });
        private static final Map<URI,Integer> jdocCache = new ConcurrentHashMap<>();
        private static final Set<String> docLet1 = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList(new String[]{
                "constructor_summary",  //NOI18N
                "method_summary",       //NOI18N
                "field_detail",         //NOI18N
                "constructor_detail",   //NOI18N
                "method_detail"         //NOI18N
            })));
        private static final Set<String> docLet2 = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList(new String[]{
                "constructor.summary",  //NOI18N
                "method.summary",       //NOI18N
                "field.detail",         //NOI18N
                "constructor.detail",   //NOI18N
                "method.detail"         //NOI18N
            })));
        private final List<? extends URL> urls;
        private final AtomicReference<InputStream> stream = new AtomicReference<InputStream>();
        private URI jdocRoot = null;
        private byte[] cache;
        /**
         * Creates a text stream from a given URL with no preopened stream.
         * @param url a URL
         */
        public TextStream(@NonNull final URL url) {
            Parameters.notNull("url", url); //NOI18N
            this.urls = Collections.singletonList(url);
        }

        TextStream(@NonNull final Collection<? extends URL> urls) {
            Parameters.notNull("urls", urls);   //NOI18N            
            final List<URL> tmpUrls = new ArrayList<>(urls.size());
            for (URL u : urls) {
                Parameters.notNull("urls[]", u);  //NOI18N
                tmpUrls.add(u);
            }
            if (tmpUrls.isEmpty()) {
                throw new IllegalArgumentException("At least one URL has to be given.");    //NOI18N
            }
            this.urls = Collections.unmodifiableList(tmpUrls);
        }

        TextStream(@NonNull final Collection<? extends URL> urls, @NonNull final URL root, InputStream stream) {
            this(urls);
            try {
                this.jdocRoot = root.toURI();
            } catch (URISyntaxException ex) {
            }
            this.stream.set(stream);
        }
        /**
         * Location of the text.
         * @return its (possibly network) location
         */
        @CheckForNull
        public URL getLocation() {
            try {
                return getLocation(RemoteJavadocPolicy.USE);
            } catch (RemoteJavadocException e) {
                //Nover happens
                throw new IllegalStateException(e);
            }
        }

        @CheckForNull
        public URL getLocation(@NonNull final RemoteJavadocPolicy rjp) throws RemoteJavadocException {
            if (urls.isEmpty()) {
                return null;
            } else {
                Integer index = jdocRoot == null ? null : jdocCache.get(jdocRoot);
                if (index == null || index >= urls.size()) {
                    switch (rjp) {
                        case USE:
                            break;
                        case EXCEPTION:
                            if (isRemote()) {
                                throw new RemoteJavadocException(urls.get(0));
                            }
                            break;
                        default:
                            throw new IllegalArgumentException(String.format(
                            "Unsupported RemoteJavadocPolicy: %s",  //NOI18N
                            rjp));
                    }
                    try {
                        String charset = null;
                        for (;;) {
                            try (BufferedReader reader = new BufferedReader(charset == null ?
                                    new InputStreamReader(this.openStream()) :
                                    new InputStreamReader(this.openStream(), charset))) {
                                if (urls.size() > 1) {
                                    reader.mark(256);
                                    String line = reader.readLine();
                                    if (line.contains("<!DOCTYPE") && line.contains("HTML>")) {
                                        index = 2;
                                        if (jdocRoot != null) {
                                            jdocCache.put(jdocRoot,index);
                                        }
                                        break;
                                    } else {
                                        reader.reset();
                                    }
                                    final HTMLEditorKit.Parser parser = new ParserDelegator();
                                    final int[] state = {-1};
                                    try {
                                        parser.parse(
                                            reader,
                                            new HTMLEditorKit.ParserCallback() {
                                                @Override
                                                public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                                                    if (state[0] == -1) {
                                                        if (t == HTML.Tag.A) {
                                                            final String attrName = (String)a.getAttribute(HTML.Attribute.NAME);
                                                            if (attrName != null) {
                                                                if (docLet1.contains(attrName)) {
                                                                    state[0] = 0;
                                                                } else if (docLet2.contains(attrName)) {
                                                                    state[0] = 1;
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                            charset != null);
                                        index = state[0] == -1 ? 0 : state[0];
                                        if (jdocRoot != null) {
                                            jdocCache.put(jdocRoot,index);
                                        }
                                        break;
                                    } catch (ChangedCharSetException e) {
                                        if (charset == null) {
                                            charset = JavadocHelper.getCharSet(e);
                                            //restart with valid charset
                                        } else {
                                            throw new IOException(e);
                                        }
                                    }                            
                                } else {
                                    index = 0;
                                    break;
                                }
                            }
                        }
                    } catch (IOException e) {
                        return null;
                    }
                }
                assert index != null && index != -1;
                return urls.get(index);
            }
        }

        @NonNull
        public List<? extends URL> getLocations() {
            return urls;
        }
        
        @CheckForNull
        public URL getDocRoot() {
            try {
                if (jdocRoot != null) {
                    return jdocRoot.toURL();
                }
            } catch (MalformedURLException ex) {
            }
            return null;
        }

        /**
         * Close any preopened stream without reading it.
         */
        public void close() {
            final InputStream is = stream.getAndSet(null);
            if (is != null) {
                try {
                    is.close();
                } catch (IOException x) {
                    LOG.log(Level.INFO, null, x);
                }
            }
        }
        /**
         * Open a stream.
         * (Might have already been opened but not read, in which case the preexisting stream is used.)
         * @return a stream, which you are obliged to close
         * @throws IOException if there is a problem reopening the stream
         */
        public synchronized InputStream openStream() throws IOException {
            if (cache != null) {
                LOG.log(Level.FINE, "loaded cached content for {0}", getFirstLocation());
                return new ByteArrayInputStream(cache);
            }
            assert !isRemote() || !EventQueue.isDispatchThread();
            InputStream uncached = stream.getAndSet(null);
            if (isRemote()) {
                try {
                    final URI fileURI = getFileURI();
                    byte[] data = fileURI == null ? null : remoteFileContentCache.get(fileURI);
                    if (data == null) {
                        if (uncached == null) {
                            uncached = JavadocHelper.openStream(
                                getFirstLocation(),
                                Bundle.LBL_HTTPJavadocDownload());
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream(20 * 1024); // typical size for Javadoc page?
                        FileUtil.copy(uncached, baos);
                        data = baos.toByteArray();
                        if (fileURI != null) {
                            remoteFileContentCache.put(fileURI, data);
                        }
                    }
                    cache = data;
                } finally {
                    if (uncached != null) {
                        uncached.close();
                    }
                }
                LOG.log(Level.FINE, "cached content for {0} ({1}k)", new Object[] {getFirstLocation(), cache.length / 1024});
                return new ByteArrayInputStream(cache);
            } else {
                if (uncached == null) {
                    uncached = JavadocHelper.openStream(getFirstLocation(), null);
                }
                return uncached;
            }
        }
        /**
         * @return true if this looks to be a web location
         */
        public boolean isRemote() {
            return JavadocHelper.isRemote(getFirstLocation());
        }

        private URL getFirstLocation() {
            return urls.iterator().next();
        }

        @CheckForNull
        private URI getFileURI() {
            final URL location = getFirstLocation();
            final String surl = location.toString();
            final int index = surl.lastIndexOf('#');    //NOI18N
            try {
                return index < 0 ?
                     location.toURI() :
                     new URI(surl.substring(0, index));
            } catch (URISyntaxException use) {
                return null;
            }
        }
    }

    private static boolean isRemote(URL url) {
        return url.getProtocol().startsWith("http") || url.getProtocol().startsWith("ftp"); // NOI18N
    }
    
    /**
     * Like {@link URL#openStream} but uses the platform's user JAR cache ({@code ArchiveURLMapper}) when available.
     * @param url a url to open
     * @return its input stream
     * @throws IOException for the usual reasons
     */
    @NonNull
    private static InputStream openStream(@NonNull final URL url, @NullAllowed final String message) throws IOException {
        ProgressHandle progress = null;
        if (message != null) {
            progress = ProgressHandle.createHandle(message);
            progress.start();
        }
        try {
            if (url.getProtocol().equals("jar")) { // NOI18N
                FileObject f = URLMapper.findFileObject(url);
                if (f != null) {
                    return f.getInputStream();
                }
            }
            if (isRemote(url)) {
                LOG.log(Level.FINE, "opening network stream: {0}", url);
            }
            final URLConnection c = url.openConnection();
            c.setConnectTimeout(REMOTE_CONNECTION_TIMEOUT);
            return c.getInputStream();
        } finally {
            if (progress != null) {
                progress.finish();
            }
        }
    }

    /**
     * Richer version of {@link SourceUtils#getJavadoc}.
     * Finds {@link URL} of a javadoc page for given element when available. This method
     * uses {@link JavadocForBinaryQuery} to find the javadoc page for the give element.
     * For {@link PackageElement} it returns the package-summary.html for given package.
     * @param element to find the Javadoc for
     * @param cancel a Callable to signal cancel request
     * @return the javadoc page or null when the javadoc is not available.
     */
    public static TextStream getJavadoc(Element element, final @NullAllowed Callable<Boolean> cancel) {
        return getJavadoc(element, true, cancel);
    }

    /**
     * Richer version of {@link SourceUtils#getJavadoc}.
     * Finds {@link URL} of a javadoc page for given element when available. This method
     * uses {@link JavadocForBinaryQuery} to find the javadoc page for the give element.
     * For {@link PackageElement} it returns the package-summary.html for given package.
     * @param element to find the Javadoc for
     * @param allowRemoteJavadoc true if non-local javadoc sources should be enabled
     * @param cancel a Callable to signal cancel request
     * @return the javadoc page or null when the javadoc is not available.
     */
    public static TextStream getJavadoc(Element element, boolean allowRemoteJavadoc, final @NullAllowed Callable<Boolean> cancel) {
        try {
            final List<TextStream> res = getJavadoc(
                element,
                allowRemoteJavadoc ? RemoteJavadocPolicy.USE : RemoteJavadocPolicy.IGNORE,
                cancel);
            return res.isEmpty() ?
                null :
                res.get(0);
        } catch (RemoteJavadocException rje) {
            throw new IllegalStateException(
                "Never thrown", //NOI18N
                rje);
        }
    }

    /**
     * Returns Javadoc for given {@link Element}.
     * Finds {@link URL} of a javadoc page for given element when available. This method
     * uses {@link JavadocForBinaryQuery} to find the javadoc page for the give element.
     * For {@link PackageElement} it returns the package-summary.html for given package.
     * @param element to find the Javadoc for
     * @param remoteJavadocPolicy the remote javadoc hanlding policy
     * @param cancel a Callable to signal cancel request
     * @return the javadoc pages
     * @throws JavadocHelper.RemoteJavadocException in case of remote Javadoc and {@link RemoteJavadocPolicy#EXCEPTION} policy
     * @since 0.138
     */
    @NonNull
    public static List<TextStream> getJavadoc(
        @NonNull final Element element,
        @NonNull final RemoteJavadocPolicy remoteJavadocPolicy,
        @NullAllowed final Callable<Boolean> cancel) throws RemoteJavadocException {
        Parameters.notNull("element", element); //NOI18N
        Parameters.notNull("remoteJavadocPolicy", remoteJavadocPolicy); //NOI18N
        return doGetJavadoc(element, remoteJavadocPolicy, cancel);
    }

    /**
     * Richer version of {@link SourceUtils#getJavadoc}.
     * Finds {@link URL} of a javadoc page for given element when available. This method
     * uses {@link JavadocForBinaryQuery} to find the javadoc page for the give element.
     * For {@link PackageElement} it returns the package-summary.html for given package.
     * @param element to find the Javadoc for
     * @return the javadoc page or null when the javadoc is not available.
     */
    public static TextStream getJavadoc(Element element) {
        return getJavadoc(element, null);
    }

    /**
     * Returns the charset from given {@link ChangedCharSetException}
     * @param e the {@link ChangedCharSetException}
     * @return the charset or null
     */
    @CheckForNull
    public static String getCharSet(ChangedCharSetException e) {
        String spec = e.getCharSetSpec();
        if (e.keyEqualsCharSet()) {
            //charsetspec contains only charset
            return spec;
        }

        //charsetspec is in form "text/html; charset=UTF-8"

        int index = spec.indexOf(";"); // NOI18N
        if (index != -1) {
            spec = spec.substring(index + 1);
        }

        spec = spec.toLowerCase();

        StringTokenizer st = new StringTokenizer(spec, " \t=", true); //NOI18N
        boolean foundCharSet = false;
        boolean foundEquals = false;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals(" ") || token.equals("\t")) { //NOI18N
                continue;
            }
            if (foundCharSet == false && foundEquals == false
                    && token.equals("charset")) { //NOI18N
                foundCharSet = true;
                continue;
            } else if (foundEquals == false && token.equals("=")) {//NOI18N
                foundEquals = true;
                continue;
            } else if (foundEquals == true && foundCharSet == true) {
                return token;
            }

            foundCharSet = false;
            foundEquals = false;
        }

        return null;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(value="DMI_COLLECTION_OF_URLS", justification="URLs have never host part")
    private static List<TextStream> doGetJavadoc(final Element element, final RemoteJavadocPolicy remoteJavadocPolicy, final Callable<Boolean> cancel) throws RemoteJavadocException {
        if (element == null) {
            throw new IllegalArgumentException("Cannot pass null as an argument of the SourceUtils.getJavadoc"); // NOI18N
        }
        ClassSymbol clsSym = null;
        String moduleName = null;
        String pkgName;
        String pageName;
        boolean buildFragment = false;
        if (element.getKind() == ElementKind.PACKAGE) {
            List<? extends Element> els = element.getEnclosedElements();
            for (Element e : els) {
                if (e.getKind().isClass() || e.getKind().isInterface()) {
                    clsSym = (ClassSymbol) e;
                    break;
                }
            }
            if (clsSym == null) {
                return Collections.emptyList();
            }
            moduleName = moduleNameFor(element);
            pkgName = FileObjects.convertPackage2Folder(((PackageElement) element).getQualifiedName().toString());
            pageName = PACKAGE_SUMMARY;
        } else if (element.getKind() == ElementKind.MODULE) {
            //The module-info has no javadoc, at least now.
            return Collections.emptyList();
        } else {
            Element e = element;
            StringBuilder sb = new StringBuilder();
            while (e.getKind() != ElementKind.PACKAGE) {
                if (e.getKind().isClass() || e.getKind().isInterface()) {
                    if (sb.length() > 0) {
                        sb.insert(0, '.');
                    }
                    sb.insert(0, e.getSimpleName());
                    if (clsSym == null) {
                        clsSym = (ClassSymbol) e;
                    }
                }
                e = e.getEnclosingElement();
            }
            if (clsSym == null) {
                return Collections.emptyList();
            }
            moduleName = moduleNameFor(e);
            pkgName = FileObjects.convertPackage2Folder(((PackageElement) e).getQualifiedName().toString());
            pageName = sb.toString();
            buildFragment = element != clsSym;
        }

        if (clsSym.completer != null) {
            clsSym.complete();
        }
        if (clsSym.classfile != null) {
            try {
                final URL classFile = clsSym.classfile.toUri().toURL();
                final String moduleNameF = moduleName;
                final String pkgNameF = pkgName;
                final String pageNameF = pageName;
                final Collection<? extends CharSequence> fragment = buildFragment ? getFragment(element) : Collections.<CharSequence>emptySet();
                final Callable<List<TextStream>> action = new Callable<List<TextStream>>() {
                    @Override
                    @NonNull
                    public List<TextStream> call() throws Exception {
                        return findJavadoc(classFile, moduleNameF, pkgNameF, pageNameF, fragment, remoteJavadocPolicy);
                    }
                };
                final boolean sync = cancel == null || remoteJavadocPolicy != RemoteJavadocPolicy.USE;
                if (sync) {
                    return action.call();
                } else {
                    final Future<List<TextStream>> future = RP.submit(action);
                    do {
                        if (cancel != null && cancel.call()) {
                            future.cancel(false);
                            break;
                        }
                        try {
                            return future.get(100, TimeUnit.MILLISECONDS);
                        } catch (TimeoutException timeOut) {
                            //Retry
                        }
                    } while (true);
                }
            } catch (Throwable t) {
                if (t instanceof ExecutionException) {
                    t = ((ExecutionException)t).getCause();
                }
                if (t instanceof ThreadDeath) {
                    throw (ThreadDeath) t;
                } else if (t instanceof RemoteJavadocException) {
                    throw (RemoteJavadocException) t;
                } else if (t instanceof InterruptedException) {
                    LOG.log(
                        Level.INFO,
                        "The HTTP Javadoc timeout expired ({0}s), to increase the timeout set the JavadocHelper.remote.timeOut property.",
                        (REMOTE_CONNECTION_TIMEOUT/1000));
                } else {
                    LOG.log(Level.INFO, null, t);
                }
            }
        }
        return Collections.emptyList();
    }

    private static final String PACKAGE_SUMMARY = "package-summary"; // NOI18N

    private static String moduleNameFor(Element element) {
        Element e = element;
        while (e != null && e.getKind() != ElementKind.MODULE) {
            e = element.getEnclosingElement();
        }
        if (e == null) {
            return null;
        }
        String name = ((ModuleElement) e).getQualifiedName().toString();
        if (!name.isEmpty()) {
            return name;
        } else {
            return null;
        }
    }

    @NonNull
    private static List<TextStream> findJavadoc(
            @NonNull final URL classFile,
            final String moduleName,
            @NonNull final String pkgName,
            @NonNull final String pageName,
            @NonNull final Collection<? extends CharSequence> fragment,
            @NonNull final RemoteJavadocPolicy remoteJavadocPolicy) throws RemoteJavadocException, InterruptedException {
        final List<TextStream> resList = new ArrayList<>();
        URL sourceRoot = null;
        Set<URL> binaries = new HashSet<URL>();
        try {
            FileObject fo = URLMapper.findFileObject(classFile);
            StringTokenizer tk = new StringTokenizer(pkgName, "/"); // NOI18N
            for (int i = 0; fo != null && i <= tk.countTokens(); i++) {
                fo = fo.getParent();
            }
            if (fo != null) {
                final URL url = CachingArchiveProvider.getDefault().mapCtSymToJar(fo.toURL());
                sourceRoot = JavaIndex.getSourceRootForClassFolder(url);
                if (sourceRoot == null) {
                    binaries.add(url);
                } else {
                    // sourceRoot may be a class root in reality
                    binaries.add(sourceRoot);
                }
            }
            if (sourceRoot != null) {
                FileObject sourceFo = URLMapper.findFileObject(sourceRoot);
                if (sourceFo != null) {
                    ClassPath exec = ClassPath.getClassPath(sourceFo, ClassPath.EXECUTE);
                    ClassPath compile = ClassPath.getClassPath(sourceFo, ClassPath.COMPILE);
                    ClassPath source = ClassPath.getClassPath(sourceFo, ClassPath.SOURCE);
                    if (exec == null) {
                        exec = compile;
                        compile = null;
                    }
                    if (exec != null && source != null) {
                        Set<URL> roots = new HashSet<URL>();
                        for (ClassPath.Entry e : exec.entries()) {
                            roots.add(e.getURL());
                        }
                        if (compile != null) {
                            for (ClassPath.Entry e : compile.entries()) {
                                roots.remove(e.getURL());
                            }
                        }
                        List<FileObject> sourceRoots = Arrays.asList(source.getRoots());
                        out:
                        for (URL e : roots) {
                            FileObject[] res = SourceForBinaryQuery.findSourceRoots(e).getRoots();
                            for (FileObject r : res) {
                                if (sourceRoots.contains(r)) {
                                    binaries.add(e);
                                    continue out;
                                }
                            }
                        }
                    }
                }
            }
binRoots:   for (URL binary : binaries) {
                JavadocForBinaryQuery.Result javadocResult = JavadocForBinaryQuery.findJavadoc(binary);
                URL[] result = javadocResult.getRoots();
                for (URL root : result) {
                    if (!root.toExternalForm().endsWith("/")) { // NOI18N
                        LOG.log(Level.WARNING, "JavadocForBinaryQuery.Result: {0} returned non-folder URL: {1}, ignoring",
                                new Object[] {javadocResult.getClass(), root.toExternalForm()});
                        continue;
                    }
                    boolean isRemote = isRemote(root);
                    boolean speculative = false;
                    if (isRemote) {
                        switch (remoteJavadocPolicy) {
                            case EXCEPTION:
                                throw new RemoteJavadocException(root);
                            case IGNORE:
                                continue;
                            case USE:
                                break;
                            case SPECULATIVE:
                                speculative = true;
                                break;
                            default:
                                throw new IllegalArgumentException(remoteJavadocPolicy.name());
                        }
                    }
                    URL url;
                    if (moduleName != null) {
                        url = new URL(root, moduleName + "/" + pkgName + "/" + pageName + ".html");
                    } else {
                        url = new URL(root, pkgName + "/" + pageName + ".html");
                    }
                    InputStream is = null;
                    String rootS = root.toString();
                    boolean useKnownGoodRoots = result.length == 1 && isRemote;
                    if (useKnownGoodRoots && knownGoodRoots.contains(rootS)) {
                        LOG.log(Level.FINE, "assumed valid Javadoc stream at {0}", url);
                    } else if (!speculative || !isRemote) {
                        try {
                            try {
                                is = openStream(url, Bundle.LBL_HTTPJavadocDownload());
                            } catch (InterruptedIOException iioe)  {
                                throw iioe;
                            } catch (IOException x) {
                                if (moduleName == null) {
                                    // Some libraries like OpenJFX prefix their
                                    // javadoc by module, similar to the JDK.
                                    // Only search there when the default fails
                                    // to avoid additional I/O.
                                    // NOTE: No multi-release jar support for now.
                                    URL moduleInfo = new URL(binary, "module-info.class");
                                    try (InputStream classData = moduleInfo.openStream()) {
                                        ClassFile clazz = new ClassFile(classData, false);
                                        Module module = clazz.getModule();
                                        if (module == null) {
                                            throw x;
                                        }
                                        String modName = module.getName();
                                        if (modName == null) {
                                            throw x;
                                        }
                                        url = new URL(root, modName + "/" + pkgName + "/" + pageName + ".html");
                                    }
                                } else {
                                    // fallback to without module name
                                    url = new URL(root, pkgName + "/" + pageName + ".html");
                                }
                                is = openStream(url, Bundle.LBL_HTTPJavadocDownload());
                            }
                            if (useKnownGoodRoots) {
                                knownGoodRoots.add(rootS);
                                LOG.log(Level.FINE, "found valid Javadoc stream at {0}", url);
                            }
                        } catch (InterruptedIOException iioe) {
                            throw new InterruptedException();
                        } catch (IOException x) {
                            LOG.log(Level.FINE, "invalid Javadoc stream at {0}: {1}", new Object[] {url, x});
                            continue;
                        }
                    }
                    if (!fragment.isEmpty()) {
                        try {
                            // Javadoc fragments may contain chars that must be escaped to comply with RFC 2396.
                            // Unfortunately URLEncoder escapes almost everything but
                            // spaces replaces with '+' char which is wrong so it is
                            // replaced with "%20"escape sequence here.                            
                            final Collection<URL> urls = new ArrayList<>(fragment.size());
                            for (CharSequence f : fragment) {
                                final String encodedfragment = URLEncoder.encode(f.toString(), "UTF-8").  // NOI18N
                                    replace("+", "%20"); // NOI18N
                                urls.add(new URI(url.toExternalForm() + '#' + encodedfragment).toURL());
                            }
                            resList.add(new TextStream(urls, root, is));
                            if (!speculative) {
                                break binRoots;
                            }
                        } catch (URISyntaxException x) {
                            LOG.log(Level.INFO, null, x);
                        } catch (UnsupportedEncodingException x) {
                            LOG.log(Level.INFO, null, x);
                        } catch (MalformedURLException x) {
                            LOG.log(Level.INFO, null, x);
                        }
                    } else {
                        resList.add(new TextStream(Collections.<URL>singleton(url), root, is));
                    }
                    if (!speculative) {
                        break binRoots;
                    }
                }
            }

        } catch (MalformedURLException x) {
            LOG.log(Level.INFO, null, x);
        }
        return resList;
    }
    
    /**
     * {@code ElementJavadoc} currently will check every class in an API set if you keep on using code completion.
     * We do not want to make a new network connection each time, especially if src.zip supplies the Javadoc anyway.
     * Assume that if one class can be found, they all can.
     */
    private static final Set<String> knownGoodRoots = Collections.synchronizedSet(new HashSet<String>());

    @NonNull
    private static Collection<? extends CharSequence> getFragment(Element e) {
        final FragmentBuilder fb = new FragmentBuilder(e.getKind());
        if (!e.getKind().isClass() && !e.getKind().isInterface()) {
            if (e.getKind() == ElementKind.CONSTRUCTOR) {
                fb.constructor(e.getEnclosingElement().getSimpleName());
            } else {
                fb.append(e.getSimpleName());
            }
            if (e.getKind() == ElementKind.METHOD || e.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement ee = (ExecutableElement) e;
                fb.append("("); //NOI18N
                for (Iterator<? extends VariableElement> it = ee.getParameters().iterator(); it.hasNext();) {
                    VariableElement param = it.next();
                    appendType(fb, param.asType(), ee.isVarArgs() && !it.hasNext());
                    if (it.hasNext()) {
                        fb.append(", ");    //NOI18N
                    }
                }
                fb.append(")"); //NOI18N
            }
        }
        return fb.getFragments();
    }
    
    private static void appendType(FragmentBuilder fb, TypeMirror type, boolean varArg) {
        switch (type.getKind()) {
        case ARRAY:
            appendType(fb, ((ArrayType) type).getComponentType(), false);
            fb.append(varArg ? "..." : "[]"); // NOI18N
            break;
        case DECLARED:
            fb.append(((TypeElement) ((DeclaredType) type).asElement()).getQualifiedName());
            break;
        default:
            fb.append(type.toString());
        }
    }

    private static final class FragmentBuilder {
        private static final List<Convertor<CharSequence,CharSequence>> FILTERS;
        static  {
            final List<Convertor<CharSequence,CharSequence>> tmp = new ArrayList<>();
            tmp.add(Convertors.<CharSequence>identity());
            tmp.add(new JDoc8025633());
            tmp.add(new JDoc8046068());
            FILTERS = Collections.unmodifiableList(tmp);
        };
        private final StringBuilder[] sbs;

        FragmentBuilder(@NonNull ElementKind kind) {
            int size = FILTERS.size();
            this.sbs = new StringBuilder[size];
            for (int i = 0; i < sbs.length; i++) {
                sbs[i] = new StringBuilder();
            }
        }
        
        @NonNull
        FragmentBuilder constructor(@NonNull final CharSequence text) {
            for (int i = 0; i < sbs.length; i++) {
                // JDK-8046068 changed the constructor format from "Name" to "<init>"
                CharSequence constructor = i >= 2 ? "<init>" : text;
                sbs[i].append(FILTERS.get(i).convert(constructor));
            }
            return this;
        }

        @NonNull
        FragmentBuilder append(@NonNull final CharSequence text) {
            for (int i = 0; i < sbs.length; i++) {
                sbs[i].append(FILTERS.get(i).convert(text));
            }
            return this;
        }

        @NonNull
        Collection<? extends CharSequence> getFragments() {
            final Collection<CharSequence> res = new ArrayList<>(sbs.length);
            for (StringBuilder sb : sbs) {
                res.add(sb.toString());
            }
            return Collections.unmodifiableCollection(res);
        }

        private static final class JDoc8025633 implements Convertor<CharSequence,CharSequence> {
            @Override
            @NonNull
            @SuppressWarnings("fallthrough")
            public CharSequence convert(@NonNull final CharSequence text) {
                final StringBuilder sb = new StringBuilder();
                for (int i = 0; i < text.length(); i++) {
                    final char c = text.charAt(i);
                    switch (c) {
                        case '(':    //NOI18N
                        case ')':    //NOI18N
                        case '<':    //NOI18N
                        case '>':    //NOI18N
                        case ',':    //NOI18N
                            sb.append('-');    //NOI18N
                            break;
                        case ' ':    //NOI18N
                        case '[':    //NOI18N
                            //NOP
                            break;
                        case ']':    //NOI18N
                            sb.append(":A");    //NOI18N
                            break;
                        case '$':   //NOI18N
                            if (i == 0) {
                                sb.append("Z:Z");   //NOI18N
                            }
                            sb.append(":D");        //NOI18N
                            break;
                        case '_':   //NOI18N
                            if (i == 0) {
                                sb.append("Z:Z");   //NOI18N
                            }
                        default:
                            sb.append(c);
                    }
                }
                return sb.toString();
            }
        }
        
        private static final class JDoc8046068 implements Convertor<CharSequence,CharSequence> {
            @Override
            @NonNull
            public CharSequence convert(@NonNull final CharSequence text) {
                return text.toString().replace(" ", "");
            }
        }
    }

}
