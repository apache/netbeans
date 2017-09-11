/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

// See #13931.

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Mapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Task to check various aspects of JavaHelp helpsets.
 * <ol>
 * <li>General parsability as far as JavaHelp is concerned.
 * <li>Map IDs are not duplicated.
 * <li>Map IDs point to real HTML files (and anchors where specified).
 * <li>TOC/Index navigators refer to real map IDs.
 * <li>HTML links in reachable HTML files point to valid places (including anchors).
 * </ol>
 * @author Jesse Glick, Marek Slama
 */
public class CheckHelpSetsBin extends Task {
    
    private List<FileSet> filesets = new ArrayList<FileSet>();
    
    private Set<String> excludedModulesSet;
    
    /**
     * Adds a set of module JARs. Class-Path extensions (including separate doc JARs) should not be included.
     */
    public void addFileset(FileSet fs) {
        filesets.add(fs);
    }
    
    private URLClassLoader createGlobalClassLoader(File dir, String[] files) throws MalformedURLException {
        URL[] globalClassPath = new URL[files.length];
        for (int i = 0; i < files.length; i++) {
            globalClassPath[i] = new File(dir, files[i]).toURI().toURL();
        }
        return new URLClassLoader(globalClassPath, ClassLoader.getSystemClassLoader().getParent(), new NbDocsStreamHandler.Factory());
    }

    private Map<String,URLClassLoader> createClassLoaderMap(File dir, String[] files) throws IOException {
        Map<String,URLClassLoader> m = new TreeMap<String,URLClassLoader>();
        for (int i = 0; i < files.length; i++) {
            File moduleJar = new File(dir, files[i]);
            Manifest manifest;
            JarFile jar = new JarFile(moduleJar);
            try {
                manifest = jar.getManifest();
            } finally {
                jar.close();
            }
            if (manifest == null) {
                log(moduleJar + " has no manifest", Project.MSG_WARN);
                continue;
            }
            String codename = JarWithModuleAttributes.extractCodeName(manifest.getMainAttributes());
            if (codename == null) {
                log(moduleJar + " is not a module", Project.MSG_WARN);
                continue;
            }
            m.put(codename.replaceFirst("/[0-9]+$", ""), new URLClassLoader(new URL[] {moduleJar.toURI().toURL()}, ClassLoader.getSystemClassLoader().getParent(), new NbDocsStreamHandler.Factory()));
        }
        return m;
    }
    
    private Set<String> parseExcludeModulesProperty (String prop) {
        if (prop == null) {
            return Collections.emptySet();
        }
        return new HashSet<String>(Arrays.asList(prop.split(",")));
    }
    
    public @Override void execute() throws BuildException {
        String p = getProject().getProperty("javahelpbin.exclude.modules");
        excludedModulesSet = parseExcludeModulesProperty(p);
        for (FileSet fs : filesets) {
            FileScanner scanner = fs.getDirectoryScanner(getProject());
            File dir = scanner.getBasedir();
            String[] files = scanner.getIncludedFiles();

            URLClassLoader globalClassLoader;
            Map<String,URLClassLoader> classLoaderMap;
            try {
                globalClassLoader = createGlobalClassLoader(dir, files);
                classLoaderMap = createClassLoaderMap(dir, files);
                NbDocsStreamHandler.NbDocsURLConnection.globalClassLoader.set(globalClassLoader);
                NbDocsStreamHandler.NbDocsURLConnection.classLoaderMap.set(classLoaderMap);
                CheckLinks.handlerFactory.set(new NbDocsStreamHandler.Factory());
                for (Map.Entry<String,URLClassLoader> entry : classLoaderMap.entrySet()) {
                    String cnb = entry.getKey();
                    if (excludedModulesSet.contains(cnb)) {
                        log("skipping module: " + cnb, Project.MSG_INFO);
                        continue;
                    }
                    URLClassLoader l = entry.getValue();
                    Manifest m;
                    InputStream is = l.getResourceAsStream("META-INF/MANIFEST.MF");
                    if (is != null) {
                        try {
                            m = new Manifest(is);
                        } finally {
                            is.close();
                        }
                    } else {
                        log("No manifest in " + Arrays.toString(l.getURLs()), Project.MSG_WARN);
                        continue;
                    }
                    for (String resource : new String[] {m.getMainAttributes().getValue("OpenIDE-Module-Layer"), "META-INF/generated-layer.xml"}) {
                        if (resource == null) {
                            continue;
                        }
                        URL layer = l.getResource(resource);
                        if (layer == null) {
                            log("No layer " + resource, Project.MSG_VERBOSE);
                            continue;
                        }
                        Document doc;
                        try {
                            doc = XMLUtil.parse(new InputSource(layer.toString()), false, false, XMLUtil.rethrowHandler(), XMLUtil.nullResolver());
                        } catch (SAXException x) {
                            log("Could not parse " + layer, x, Project.MSG_WARN);
                            continue;
                        }
                        for (Element services : XMLUtil.findSubElements(doc.getDocumentElement())) {
                            if (!services.getTagName().equals("folder") || !services.getAttribute("name").equals("Services")) {
                                continue;
                            }
                            for (Element javahelp : XMLUtil.findSubElements(services)) {
                                if (!javahelp.getTagName().equals("folder") || !javahelp.getAttribute("name").equals("JavaHelp")) {
                                    continue;
                                }
                                JAVAHELP: for (Element registration : XMLUtil.findSubElements(javahelp)) {
                                    if (!registration.getTagName().equals("file")) {
                                        continue;
                                    }
                                    InputSource input = null;
                                    String url = registration.getAttribute("url");
                                    if (!url.isEmpty()) {
                                        input = new InputSource(new URL(layer, url).toString());
                                    } else {
                                        NodeList nl = registration.getChildNodes();
                                        for (int i = 0; i < nl.getLength(); i++) {
                                            if (nl.item(i).getNodeType() == Node.CDATA_SECTION_NODE) {
                                                if (input == null) {
                                                    input = new InputSource(new StringReader(nl.item(i).getNodeValue()));
                                                } else {
                                                    log("Multiple content for " + registration.getAttribute("name") + " in " + layer, Project.MSG_WARN);
                                                    continue JAVAHELP;
                                                }
                                            }
                                        }
                                        if (input == null) {
                                            log("No content for " + registration.getAttribute("name") + " in " + layer, Project.MSG_WARN);
                                        }
                                    }
                                    Document doc2;
                                    try {
                                        doc2 = XMLUtil.parse(input, false, false, XMLUtil.rethrowHandler(), XMLUtil.nullResolver());
                                    } catch (SAXException x) {
                                        log("Could not parse " + registration.getAttribute("name") + " in " + layer, x, Project.MSG_WARN);
                                        continue;
                                    }
                                    URI helpsetref = URI.create(doc2.getDocumentElement().getAttribute("url"));
                                    if ("nbdocs".equals(helpsetref.getScheme()) && helpsetref.getAuthority() == null) {
                                        try {
                                            helpsetref = new URI(helpsetref.getScheme(), cnb, helpsetref.getPath(), helpsetref.getQuery(), helpsetref.getFragment());
                                        } catch (URISyntaxException x) {
                                            throw new BuildException(x);
                                        }
                                    }
                                    log("checking: " + helpsetref, Project.MSG_INFO);
                                    checkHelpSetURL(CheckLinks.toURL(helpsetref), globalClassLoader, l, classLoaderMap, cnb);
                                }
                            }
                        }
                    }
                }
            } catch (IOException x) {
                throw new BuildException(x);
            } finally {
                NbDocsStreamHandler.NbDocsURLConnection.globalClassLoader.set(null);
                NbDocsStreamHandler.NbDocsURLConnection.classLoaderMap.set(null);
                CheckLinks.handlerFactory.set(null);
            }
        }
    }
    
    private void checkHelpSetURL
    (URL hsURL, ClassLoader globalClassLoader, ClassLoader moduleClassLoader, Map<String,URLClassLoader> classLoaderMap, String cnb) {
        HelpSet hs = null;
        try {
            hs = new HelpSet(moduleClassLoader, hsURL);
        } catch (HelpSetException ex) {
            throw new BuildException("Failed to parse " + hsURL + ": " + ex, ex, getLocation());
        }
        javax.help.Map map = hs.getCombinedMap();
        Enumeration<?> e = map.getAllIDs();
        Set<URI> okurls = new HashSet<URI>(1000);
        Set<URI> badurls = new HashSet<URI>(1000);
        Set<URI> cleanurls = new HashSet<URI>(1000);
        while (e.hasMoreElements()) {
            javax.help.Map.ID id = (javax.help.Map.ID)e.nextElement();
            URL u = null;
            try {
                u = id.getURL();
            } catch (MalformedURLException ex) {
                log("id:" + id, Project.MSG_WARN);
                ex.printStackTrace();
            }
            if (u == null) {
                throw new BuildException("Bogus map ID: " + id.id + " in: " + cnb);
            }
            log("Checking ID " + id.id, Project.MSG_VERBOSE);
            try {
                //System.out.println("CALL OF CheckLinks.scan");
                List<String> errors = new ArrayList<String>();
                CheckLinks.scan(this, globalClassLoader, classLoaderMap, id.id, "",
                new URI(u.toExternalForm()), okurls, badurls, cleanurls, false, false, false, 2, 
                Collections.<Mapper>emptyList(), errors);
                for (String error : errors) {
                    log(error, Project.MSG_WARN);
                }
                //System.out.println("RETURN OF CheckLinks.scan");
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /* Unused:
    private void checkHelpSet(File hsfile) throws Exception {
        log("Checking helpset: " + hsfile);
        HelpSet hs = new HelpSet(null, hsfile.toURI().toURL());
        javax.help.Map map = hs.getCombinedMap();
        log("Parsed helpset, checking map IDs in TOC/Index navigators...");
        NavigatorView[] navs = hs.getNavigatorViews();
        for (int i = 0; i < navs.length; i++) {
            String name = navs[i].getName();
            File navfile = new File(hsfile.getParentFile(), (String)navs[i].getParameters().get("data"));
            if (! navfile.exists()) throw new BuildException("Navigator " + name + " not found", new Location(navfile.getAbsolutePath()));
            if (navs[i] instanceof IndexView) {
                log("Checking index navigator " + name, Project.MSG_VERBOSE);
                IndexView.parse(navfile.toURI().toURL(), hs, Locale.getDefault(), new VerifyTIFactory(hs, map, navfile, false));
            } else if (navs[i] instanceof TOCView) {
                log("Checking TOC navigator " + name, Project.MSG_VERBOSE);
                TOCView.parse(navfile.toURI().toURL(), hs, Locale.getDefault(), new VerifyTIFactory(hs, map, navfile, true));
            } else {
                log("Skipping non-TOC/Index view: " + name, Project.MSG_VERBOSE);
            }
        }
        log("Checking for duplicate map IDs...");
        HelpSet.parse(hsfile.toURI().toURL(), null, new VerifyHSFactory());
        log("Checking links from help map and between HTML files...");
        Enumeration e = map.getAllIDs();
        Set<URI> okurls = new HashSet<URI>(1000);
        Set<URI> badurls = new HashSet<URI>(1000);
        Set<URI> cleanurls = new HashSet<URI>(1000);
        while (e.hasMoreElements()) {
            javax.help.Map.ID id = (javax.help.Map.ID)e.nextElement();
            URL u = map.getURLFromID(id);
            if (u == null) {
                throw new BuildException("Bogus map ID: " + id.id, new Location(hsfile.getAbsolutePath()));
            }
            log("Checking ID " + id.id, Project.MSG_VERBOSE);
            try {
                CheckLinks.scan(this, null, null, id.id, "", 
                new URI(u.toExternalForm()), okurls, badurls, cleanurls, false, false, false, 2,
                Collections.<Mapper>emptyList());
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private final class VerifyTIFactory implements TreeItemFactory {
        
        private final HelpSet hs;
        private final javax.help.Map map;
        private final File navfile;
        private final boolean toc;
        public VerifyTIFactory(HelpSet hs, javax.help.Map map, File navfile, boolean toc) {
            this.hs = hs;
            this.map = map;
            this.navfile = navfile;
            this.toc = toc;
        }
        
        // The useful method:
        
        public TreeItem createItem(String str, Hashtable hashtable, HelpSet helpSet, Locale locale) {
            String target = (String)hashtable.get("target");
            if (target != null) {
                if (! map.isValidID(target, hs)) {
                    log(navfile + ": invalid map ID: " + target, Project.MSG_WARN);
                } else {
                    log("OK map ID: " + target, Project.MSG_VERBOSE);
                }
            }
            return createItem();
        }
        
        // Filler methods:
        
        public java.util.Enumeration listMessages() {
            return Collections.enumeration(Collections.<String>emptyList());
        }
        
        public void processPI(HelpSet helpSet, String str, String str2) {
        }
        
        public void reportMessage(String str, boolean param) {
            log(str, param ? Project.MSG_VERBOSE : Project.MSG_WARN);
        }
        
        public void processDOCTYPE(String str, String str1, String str2) {
        }
        
        public void parsingStarted(URL uRL) {
        }
        
        public DefaultMutableTreeNode parsingEnded(DefaultMutableTreeNode defaultMutableTreeNode) {
            return defaultMutableTreeNode;
        }
        
        public TreeItem createItem() {
            if (toc) {
                return new TOCItem();
            } else {
                return new IndexItem();
            }
        }
        
    }
    
    private final class VerifyHSFactory extends HelpSet.DefaultHelpSetFactory {
        
        private Set<String> ids = new HashSet<String>(1000);
        
        public void processMapRef(HelpSet hs, Hashtable attrs) {
            try {
                URL map = new URL(hs.getHelpSetURL(), (String)attrs.get("location"));
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setValidating(false);
                factory.setNamespaceAware(false);
                SAXParser parser = factory.newSAXParser();
                parser.parse(new InputSource(map.toExternalForm()), new Handler(map.getFile()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        private final class Handler extends DefaultHandler {
            
            private final String map;
            public Handler(String map) {
                this.map = map;
            }
            
            public void startElement(String uri, String lname, String name, Attributes attributes) throws SAXException {
                if (name.equals("mapID")) {
                    String target = attributes.getValue("target");
                    if (target != null) {
                        if (ids.add(target)) {
                            log("Found map ID: " + target, Project.MSG_DEBUG);
                        } else {
                            log(map + ": duplicated ID: " + target, Project.MSG_WARN);
                        }
                    }
                }
            }
            
            public InputSource resolveEntity(String pub, String sys) throws SAXException {
                if (pub.equals("-//Sun Microsystems Inc.//DTD JavaHelp Map Version 1.0//EN") ||
                        pub.equals("-//Sun Microsystems Inc.//DTD JavaHelp Map Version 2.0//EN")) {
                    // Ignore.
                    return new InputSource(new ByteArrayInputStream(new byte[0]));
                } else {
                    return null;
                }
            }
            
        }
        
    }
    */
    
    private static class NbDocsStreamHandler extends URLStreamHandler {

        static class Factory implements URLStreamHandlerFactory {

            public URLStreamHandler createURLStreamHandler(String protocol) {
                if (protocol.equals("nbdocs")) { // NOI18N
                    return new CheckHelpSetsBin.NbDocsStreamHandler();
                } else {
                    return null;
                }
            }
        }

        /** Make a URLConnection for nbdocs: URLs.
         * @param u the URL
         * @throws IOException if the wrong protocol
         * @return the connection
         */
        protected URLConnection openConnection(URL u) throws IOException {
            if (u.getProtocol().equals("nbdocs")) { // NOI18N
                return new NbDocsURLConnection(u);
            } else {
                throw new IOException();
            }
        }

        /** A URL connection that reads from the docs classloader.
         */
        static class NbDocsURLConnection extends URLConnection {

            static ThreadLocal<URLClassLoader> globalClassLoader = new ThreadLocal<URLClassLoader>();
            static ThreadLocal<Map<String,URLClassLoader>> classLoaderMap = new ThreadLocal<Map<String,URLClassLoader>>();

            /** underlying URL connection
             */
            private URLConnection real = null;

            /** any associated exception while handling
             */
            private IOException exception = null;

            /** Make the connection.
             * @param u URL to connect to
             */
            public NbDocsURLConnection(URL u) {
                super(u);
            }

            /** Connect to the URL.
             * Actually look up and open the underlying connection.
             * @throws IOException for the usual reasons
             */
            public synchronized void connect() throws IOException {
                tryToConnect();
                if (exception != null) {
                    exception.printStackTrace();//XXX
                    throw exception;
                }
            }

            /** Maybe connect, if not keep track of the problem.
             */
            private synchronized void tryToConnect() {
                if (connected || exception != null) {
                    return;
                }
                try {
                    URLClassLoader l;
                    String cnb = url.getHost();
                    if (cnb.isEmpty()) {
                        l = globalClassLoader.get();
                    } else {
                        l = classLoaderMap.get().get(cnb);
                        if (l == null) {
                            throw new IOException("no loader for " + cnb);
                        }
                    }
                    String path = url.getPath().substring(1);
                    URL u = l.getResource(path);
                    if (u == null) {
                        throw new FileNotFoundException(path + " in " + Arrays.toString(l.getURLs()));
                    }
                    real = u.openConnection();
                    real.connect();
                    connected = true;
                } catch (IOException ioe) {
                    exception = ioe;
                }
            }

            /** Get a URL header.
             * @param n index of the header
             * @return the header value
             */
            public @Override String getHeaderField(int n) {
                tryToConnect();
                if (connected) {
                    return real.getHeaderField(n);
                } else {
                    return null;
                }
            }

            /** Get the name of a header.
             * @param n the index
             * @return the header name
             */
            public @Override String getHeaderFieldKey(int n) {
                tryToConnect();
                if (connected) {
                    return real.getHeaderFieldKey(n);
                } else {
                    return null;
                }
            }

            /** Get a header by name.
             * @param key the header name
             * @return the value
             */
            public @Override String getHeaderField(String key) {
                tryToConnect();
                if (connected) {
                    return real.getHeaderField(key);
                } else {
                    return null;
                }
            }

            /** Get an input stream on the connection.
             * @throws IOException for the usual reasons
             * @return a stream to the object
             */
            public @Override InputStream getInputStream() throws IOException {
                connect();
                return real.getInputStream();
            }

            /** Get an output stream on the object.
             * @throws IOException for the usual reasons
             * @return an output stream writing to it
             */
            public @Override OutputStream getOutputStream() throws IOException {
                connect();
                return real.getOutputStream();
            }

            /** Get the type of the content.
             * @return the MIME type
             */
            public @Override String getContentType() {
                tryToConnect();
                if (connected) {
                    return real.getContentType();
                } else {
                    return "application/octet-stream";
                }
            }

            /** Get the length of content.
             * @return the length in bytes
             */
            public @Override int getContentLength() {
                tryToConnect();
                if (connected) {
                    return real.getContentLength();
                } else {
                    return 0;
                }
            }

        }

    }
        
}
