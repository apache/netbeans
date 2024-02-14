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

package org.netbeans.modules.apisupport.project.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.universe.SourceRootsProvider;
import org.netbeans.modules.apisupport.project.universe.SourceRootsSupport;
import org.netbeans.modules.apisupport.project.universe.TestEntry;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.ChangeSupport;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Able to find sources in the NetBeans sources zip.
 *
 * @author Martin Krauskopf
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation.class)
public final class GlobalSourceForBinaryImpl implements SourceForBinaryQueryImplementation {
    
    /** for use from unit tests */
    static boolean quiet = false;
    
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        try {
            String binaryRootS = binaryRoot.toExternalForm();
            if (binaryRootS.startsWith("jar:file:")) { // NOI18N
                // check for tests.jar in test distribution
                File jar = FileUtil.archiveOrDirForURL(binaryRoot);
                TestEntry testJar = jar != null ? TestEntry.get(jar) : null;
                if (testJar != null) {
                    URL result = testJar.getSrcDir();
                    if (result != null) {
                        final FileObject resultFO = URLMapper.findFileObject(result);
                        if (resultFO != null) {
                            return new SourceForBinaryQuery.Result() {
                                public FileObject[] getRoots() {
                                    return new FileObject[] {resultFO};
                                }
                                public void addChangeListener(ChangeListener l) {}
                                public void removeChangeListener(ChangeListener l) {}
                            };
                        }
                    }
                }
            }
            if (!binaryRoot.getProtocol().equals("jar")) { // NOI18N
                Util.err.log(binaryRoot + " is not an archive file."); // NOI18N
                return null;
            }
            File binaryRootF = FileUtil.archiveOrDirForURL(binaryRoot);
            if (binaryRootF == null) {
                return null;
            }
            String name = binaryRootF.getName();
            if (!name.endsWith(".jar")) { // NOI18N
                return null;
            }
            String cnb = name.substring(0, name.length() - 4).replace('-', '.');
            NbPlatform supposedPlaf = null;
            for (NbPlatform plaf : NbPlatform.getPlatformsOrNot()) {
                String plafS = Utilities.toURI(plaf.getDestDir()).toURL().toExternalForm();
                Matcher m = Pattern.compile("jar:\\Q" + plafS + "\\E[^/]+/(?:modules|lib|core)/([^/]+)[.]jar!/").matcher(binaryRootS);
                if (m.matches()) {
                    supposedPlaf = plaf;
                    break;
                }
            }
            if (supposedPlaf == null) {
                // try external clusters
                URL[] sourceRoots = ModuleList.getSourceRootsForExternalModule(binaryRootF);
                if (sourceRoots.length > 0) {
                    return new ExtClusterResult(new SourceRootsSupport(sourceRoots, null), cnb);
                }
                return null;    // TODO C.P library wrapper sources support? probably not
            }
            return new NbPlatformResult(supposedPlaf, cnb);
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }
    }

    abstract static class SourceRootsProviderResult implements SourceForBinaryQuery.Result {
        private SourceRootsProvider srp;
        final String cnb;

        SourceRootsProviderResult(SourceRootsProvider srp, String cnb) {
            this.srp = srp;
            this.cnb = cnb;
        }

        public FileObject[] getRoots() {
            final List<FileObject> candidates = new ArrayList<FileObject>();
            try {
                for (URL root : srp.getSourceRoots()) {
                    if (root.getProtocol().equals("jar")) { // NOI18N
                        // suppose zipped sources
                        File nbSrcF = FileUtil.archiveOrDirForURL(root);
                        if (nbSrcF == null || !nbSrcF.exists()) {
                            continue;
                        }
                        NetBeansSourcesParser nbsp;
                        try {
                            nbsp = NetBeansSourcesParser.getInstance(nbSrcF);
                        } catch (ZipException e) {
                            if (!quiet) {
                                Util.err.annotate(e, ErrorManager.UNKNOWN, nbSrcF + " does not seem to be a valid ZIP file.", null, null, null); // NOI18N
                                Util.err.notify(ErrorManager.INFORMATIONAL, e);
                            }
                            continue;
                        }
                        if (nbsp == null) {
                            continue;
                        }
                        String pathInZip = nbsp.findSourceRoot(cnb);
                        if (pathInZip == null) {
                            continue;
                        }
                        URL u = new URL(root, pathInZip);
                        FileObject entryFO = URLMapper.findFileObject(u);
                        if (entryFO != null) {
                            candidates.add(entryFO);
                        }
                    } else {
                        // Does not resolve nbjunit and similar from ZIPped
                        // sources. Not a big issue since the default distributed
                        // sources do not contain them anyway.
                        String relPath = resolveRelativePath(root);
                        if (relPath == null) {
                            continue;
                        }
                        URL url = new URL(root, relPath);
                        FileObject dir = URLMapper.findFileObject(url);
                        if (dir != null) {
                            candidates.add(dir);
                        } // others dirs are currently resolved by o.n.m.apisupport.project.queries.SourceForBinaryImpl
                    }
                }
            } catch (IOException ex) {
                throw new AssertionError(ex);
            }
            return candidates.toArray(new FileObject[0]);
        }

        protected abstract String resolveRelativePath(URL sourceRoot) throws IOException;

        // TODO C.P listening on cluster sources change? probably not needed
        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }
    }

    private static final class ExtClusterResult extends SourceRootsProviderResult {

        private ExtClusterResult(SourceRootsSupport sourceRootsSupport, String cnb) {

            super(sourceRootsSupport, cnb);
        }

        @Override
        protected String resolveRelativePath(URL sourceRoot) throws IOException {
            // TODO C.P cache root + cnb -> relpath? dig into library wrappers?
            File srPath = Utilities.toFile(URI.create(sourceRoot.toExternalForm()));
            File moduleSrc = new File(srPath, "src");    // NOI18N
            if (moduleSrc.exists()) {   // src root is module project root directly
                return "src/";    // NOI18N
            }
            File prjProp = new File(srPath, "nbproject/project.properties");    // NOI18N
            if (prjProp.exists()) {
                // maybe suite root
                EditableProperties ep = Util.loadProperties(FileUtil.toFileObject(prjProp));
                String prjName = ep.get("project." + cnb);    // NOI18N
                if (prjName != null) {
                    return prjName + "/src/";
                }
            }
            // either artificially composed sources without nbproject - not supported -
            // or (part of) NB.org source tree, which should have been resolved against NbPlatform
            return null;
        }
    }

    private static final class NbPlatformResult extends SourceRootsProviderResult
            implements PropertyChangeListener {
        
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private final NbPlatform platform;
        
        private boolean alreadyListening;
        
        NbPlatformResult(NbPlatform platform, String cnb) {
            super(platform, cnb);
            this.platform = platform;
//            this.testType = testType;
//            this.testCluster = testCluster;
        }

        public @Override void addChangeListener(ChangeListener l) {
            // start listening on NbPlatform
            changeSupport.addChangeListener(l);
            if (!alreadyListening) {
                platform.addPropertyChangeListener(this);
                alreadyListening = true;
            }
        }

        public @Override void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
            if (!changeSupport.hasListeners()) {
                platform.removePropertyChangeListener(this);
                alreadyListening = false;
            }
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (SourceRootsProvider.PROP_SOURCE_ROOTS.equals(evt.getPropertyName())) {
                changeSupport.fireChange();
            }
        }

        @Override
        protected String resolveRelativePath(URL sourceRoot) throws IOException {
            return null;
        }
        
    }
    
    public static final class NetBeansSourcesParser {
        
        /** Zip file to instance map. */
        private static final Map<File,NetBeansSourcesParser> instances = new HashMap<File,NetBeansSourcesParser>();
        
        private static final String NBBUILD_ENTRY = "nbbuild/"; // NOI18N
        
        private Map<String,String> cnbToPrjDir;
        private final ZipFile nbSrcZip;
        private final String zipNBRoot;
        
        /**
         * May return <code>null</code> if the given zip is not a valid
         * NetBeans sources zip.
         */
        public static NetBeansSourcesParser getInstance(File nbSrcZip) throws ZipException, IOException {
            NetBeansSourcesParser nbsp = instances.get(nbSrcZip);
            if (nbsp == null) {
                ZipFile nbSrcZipFile = new ZipFile(nbSrcZip);
                String zipNBRoot = NetBeansSourcesParser.findNBRoot(nbSrcZipFile);
                if (zipNBRoot != null) {
                    nbsp = new NetBeansSourcesParser(nbSrcZipFile, zipNBRoot);
                    instances.put(nbSrcZip, nbsp);
                }
            }
            return nbsp;
        }
        
        NetBeansSourcesParser(ZipFile nbSrcZip, String zipNBRoot) {
            this.nbSrcZip = nbSrcZip;
            this.zipNBRoot = zipNBRoot;
        }
        
        String findSourceRoot(final String cnb) {
            if (cnbToPrjDir == null) {
                try {
                    doScanZippedNetBeansOrgSources();
                } catch (IOException ex) {
                    Util.err.notify(ErrorManager.WARNING, ex);
                }
            }
            return cnbToPrjDir.get(cnb);
        }
        
        private static String findNBRoot(final ZipFile nbSrcZip) {
            String nbRoot = null;
            for (Enumeration<? extends ZipEntry> en = nbSrcZip.entries(); en.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) en.nextElement();
                if (!entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName();
                if (!name.equals(NBBUILD_ENTRY) &&
                        !(name.endsWith(NBBUILD_ENTRY) && name.substring(name.indexOf('/') + 1).equals(NBBUILD_ENTRY))) {
                    continue;
                }
                ZipEntry xmlEntry = nbSrcZip.getEntry(name + "nbproject/project.xml"); // NOI18N
                if (xmlEntry != null) {
                    nbRoot = name.substring(0, name.length() - NBBUILD_ENTRY.length());
                    break;
                }
            }
            return nbRoot;
        }
        
        private void doScanZippedNetBeansOrgSources() throws IOException {
            cnbToPrjDir = new HashMap<String,String>();
            for (Enumeration<? extends ZipEntry> en = nbSrcZip.entries(); en.hasMoreElements(); ) {
                ZipEntry entry = en.nextElement();
                if (!entry.isDirectory()) {
                    continue;
                }
                String path = entry.getName().substring(0, entry.getName().length() - 1); // remove last slash
                if (this.zipNBRoot != null && (!path.startsWith(this.zipNBRoot) || path.equals(this.zipNBRoot))) {
                    continue;
                }
                StringTokenizer st = new StringTokenizer(path, "/"); // NOI18N
                if (st.countTokens() > /*ModuleList.DEPTH_NB_ALL*/3) {
                    continue;
                }
                String name = path.substring(path.lastIndexOf('/') + 1, path.length());
                if (ModuleList.EXCLUDED_DIR_NAMES.contains(name)) {
                    // #61579: known to not be project dirs, so skip to save time.
                    continue;
                }
                // XXX should read src.dir from properties
                ZipEntry src = nbSrcZip.getEntry(entry.getName() + "src/"); // NOI18N
                if (src == null || !src.isDirectory()) {
                    continue;
                }
                
                ZipEntry projectXML = nbSrcZip.getEntry(entry.getName() + "nbproject/project.xml"); // NOI18N
                if (projectXML == null) {
                    continue;
                }
                String cnb = parseCNB(projectXML);
                if (cnb != null) {
                    cnbToPrjDir.put(cnb, entry.getName() + "src/"); // NOI18N
                }
            }
        }
        
        private String parseCNB(final ZipEntry projectXML) throws IOException {
            Document doc;
            InputStream is = nbSrcZip.getInputStream(projectXML);
            try {
                doc = XMLUtil.parse(new InputSource(is), false, true, null, null);
            } catch (SAXException e) {
                throw (IOException) new IOException(projectXML + ": " + e.toString()).initCause(e); // NOI18N
            } finally {
                is.close();
            }
            Element docel = doc.getDocumentElement();
            Element type = XMLUtil.findElement(docel, "type", "http://www.netbeans.org/ns/project/1"); // NOI18N
            String cnb = null;
            if (XMLUtil.findText(type).equals("org.netbeans.modules.apisupport.project")) { // NOI18N
                Element cfg = XMLUtil.findElement(docel, "configuration", "http://www.netbeans.org/ns/project/1"); // NOI18N
                Element data = XMLUtil.findElement(cfg, "data", null); // NOI18N
                if (data != null) {
                    cnb = XMLUtil.findText(XMLUtil.findElement(data, "code-name-base", null)); // NOI18N
                }
            }
            return cnb;
        }
        
    }
    
}
