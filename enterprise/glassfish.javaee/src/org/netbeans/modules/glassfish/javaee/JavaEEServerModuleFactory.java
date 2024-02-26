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
package org.netbeans.modules.glassfish.javaee;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModuleFactory;
import org.netbeans.modules.glassfish.spi.RegisteredDerbyServer;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Peter Williams
 */
public class JavaEEServerModuleFactory implements GlassfishModuleFactory {

    private static final JavaEEServerModuleFactory singleton = new JavaEEServerModuleFactory();
    private static final Logger LOG = Logger.getLogger(JavaEEServerModuleFactory.class.getName());
    
    private JavaEEServerModuleFactory() {
    }
    
    public static GlassfishModuleFactory getDefault() {
        return singleton;
    }
    
    @Override
    public boolean isModuleSupported(String glassfishHome, Properties asenvProps) {

        // Do some moderate sanity checking to see if this v3 build looks ok.
        File jar = ServerUtilities.getJarName(glassfishHome, ServerUtilities.GFV3_JAR_MATCHER);


        if (jar==null) {
            return false;
        }
        if (jar.exists()) {
            return true;
        }

        return false;
    }

    private static final RequestProcessor RP = new RequestProcessor("JavaEEServerModuleFactory");

    @Override
    public Object createModule(Lookup instanceLookup) {
        // When creating JavaEE support, also ensure this instance is added to j2eeserver
        InstanceProperties ip = null;
        final GlassfishModule commonModule
                = instanceLookup.lookup(GlassfishModule.class);
        if(commonModule != null) {
            Map<String, String> props = commonModule.getInstanceProperties();
            String url = props.get(InstanceProperties.URL_ATTR);
            ip = InstanceProperties.getInstanceProperties(url);
            if (ip == null) {
                String username = props.get(InstanceProperties.USERNAME_ATTR);
                // Password shall not be read from keyring during initialization
                String password = null;
                String displayName = props.get(InstanceProperties.DISPLAY_NAME_ATTR);
                    try {
                        ip = InstanceProperties.createInstancePropertiesNonPersistent(
                                url, username, password, displayName, props);
                    } catch (InstanceCreationException ex) {
                        // the initialization delay of the ServerRegistry may have triggered a ignorable
                        // exception
                        ip = InstanceProperties.getInstanceProperties(url);
                        if (null == ip) {
                            LOG.log(Level.WARNING, null, ex); // NOI18N
                        }
                    }

                if(ip == null) {
                    LOG.log(Level.INFO, "Unable to create/locate J2EE InstanceProperties for {0}", url);
                }
            }

            final String glassfishRoot = commonModule.getInstanceProperties().get(
                    GlassfishModule.GLASSFISH_FOLDER_ATTR);
            final String installRoot = commonModule.getInstanceProperties().get(
                    GlassfishModule.INSTALL_FOLDER_ATTR);
            RP.post(new Runnable() {
                @Override
                public void run() {
                    ensureEclipseLinkSupport(glassfishRoot);
                    ensureCometSupport(glassfishRoot);
                    ensureGlassFishApiSupport(commonModule.getInstance());
                    // lookup the javadb register service here and use it.
                    RegisteredDerbyServer db = Lookup.getDefault().lookup(RegisteredDerbyServer.class);
                    if (null != db  && null != installRoot) {
                        File ir = new File(installRoot);
                        File f = new File(ir,"javadb");
                        if (f.exists() && f.isDirectory() && f.canRead()) {
                            db.initialize(f.getAbsolutePath());
                        }
                    }
                }
            });
        } else {
            LOG.log(Level.WARNING, "commonModule is NULL");
        }

        return (ip != null) ? new JavaEEServerModule(instanceLookup, ip) : null;
    }
    
    private static final String CLASS_LIBRARY_TYPE = "j2se"; // NOI18N
    private static final String CLASSPATH_VOLUME = "classpath"; // NOI18N
    private static final String SOURCE_VOLUME = "src"; // NOI18N
    private static final String JAVADOC_VOLUME = "javadoc"; // NOI18N
    
    private static final String ECLIPSE_LINK_LIB = "EclipseLink-GlassFish-v3-Prelude"; // NOI18N
    private static final String ECLIPSE_LINK_LIB_2 = "EclipseLink-GlassFish-v3"; // NOI18N
    private static final String EL_CORE_JAR_MATCHER = "eclipselink-wrapper" + ServerUtilities.GFV3_VERSION_MATCHER; // NOI18N

    private static final String PERSISTENCE_API_JAR_MATCHER_1 = "javax.javaee" + ServerUtilities.GFV3_VERSION_MATCHER; // NOI18N
    private static final String PERSISTENCE_API_JAR_MATCHER_2 = "javax.persistence" + ServerUtilities.GFV3_VERSION_MATCHER; // NOI18N
        
    private static final String PERSISTENCE_JAVADOC = "javaee-doc-api.jar"; // NOI18N
    
    private static boolean ensureEclipseLinkSupport(String installRoot) {
        List<URL> libraryList = new ArrayList<URL>();
        List<URL> docList = new ArrayList<URL>();
        try {
            File f = ServerUtilities.getJarName(installRoot, EL_CORE_JAR_MATCHER);
            if (f != null && f.exists()) {
                libraryList.add(ServerUtilities.fileToUrl(f));
            } else {// we are in the final V3 Prelude jar name structure
                // find the org.eclipse.persistence*.jar files and add them
                for (File candidate : new File(installRoot, "modules").listFiles()) {// NOI18N
                    if (candidate.getName().indexOf("org.eclipse.persistence") != -1) {// NOI18N
                        libraryList.add(ServerUtilities.fileToUrl(candidate));
                    }
                }
            }
            f = ServerUtilities.getJarName(installRoot, PERSISTENCE_API_JAR_MATCHER_1);
            if (f != null && f.exists()) {
                libraryList.add(ServerUtilities.fileToUrl(f));
            } else {
                f = ServerUtilities.getJarName(installRoot, PERSISTENCE_API_JAR_MATCHER_2);
                if (f != null && f.exists()) {
                    libraryList.add(ServerUtilities.fileToUrl(f));
                }
            }
            // TODO: add support for JPA 3.x

            File j2eeDoc = InstalledFileLocator.getDefault().locate(
                    "docs/" + PERSISTENCE_JAVADOC,
                    Hk2LibraryProvider.JAVAEE_DOC_CODE_BASE, false);
            if (j2eeDoc != null) {
                docList.add(ServerUtilities.fileToUrl(j2eeDoc));
            } else {
                LOG.log(Level.WARNING, "Warning: Java EE documentation not found when registering EclipseLink library.");
            }
        } catch (MalformedURLException ex) {
            LOG.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            return false;
        }
        String name = ECLIPSE_LINK_LIB;
        File f = ServerUtilities.getJarName(installRoot, "gmbal" + ServerUtilities.GFV3_VERSION_MATCHER);
        if (f != null && f.exists()) {
            name = ECLIPSE_LINK_LIB_2;
        }
        return addLibrary(name, libraryList, docList,
                NbBundle.getMessage(JavaEEServerModuleFactory.class, "DNAME_GF_ECLIPSELINK"),  // NOI18N
                NbBundle.getMessage(JavaEEServerModuleFactory.class, "DESC_GF_ECLIPSELINK"));  // NOI18N
    }
    private static final String COMET_LIB = "Comet-GlassFish-v3-Prelude"; // NOI18N
    private static final String COMET_LIB_2 = "Comet-GlassFish-v3"; // NOI18N
    private static final String COMET_JAR_MATCHER = "grizzly-module" + ServerUtilities.GFV3_VERSION_MATCHER; // NOI18N
    private static final String COMET_JAR_2_MATCHER = "grizzly-comet" + ServerUtilities.GFV3_VERSION_MATCHER; // NOI18N
    private static final String GRIZZLY_OPTIONAL_JAR_MATCHER = "grizzly-optional" + ServerUtilities.GFV3_VERSION_MATCHER; // NOI18N

    private static boolean ensureCometSupport(String installRoot) {
        List<URL> libraryList = new ArrayList<URL>();
        String name = COMET_LIB;
        File f = ServerUtilities.getJarName(installRoot, GRIZZLY_OPTIONAL_JAR_MATCHER);
        if (f == null || !f.exists()) {
            f = ServerUtilities.getJarName(installRoot, COMET_JAR_MATCHER);
        }
        if (f == null || !f.exists()) {
            name = COMET_LIB_2;
            f = ServerUtilities.getJarName(installRoot, COMET_JAR_2_MATCHER);
        }
        if (f != null && f.exists()) {
            try {
                libraryList.add(ServerUtilities.fileToUrl(f));
            } catch (MalformedURLException ex) {
                LOG.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                return false;
            }
        }

        return addLibrary(name, libraryList, null,
                NbBundle.getMessage(JavaEEServerModuleFactory.class, "DNAME_GF_COMET"),  // NOI18N
                NbBundle.getMessage(JavaEEServerModuleFactory.class, "DESC_GF_COMET"));  // NOI18N
    }

    private static final String JERSEY_GF_SERVER = "jersey-gf-server"; //NOI18N
    private static final String[] JAXRS_LIBRARIES =
             {"jackson-asl", "jackson-core-asl", "jersey-bundle", "jersey-gf-bundle", "jersey-multipart", "jettison", "mimepull", "jsr311-api"}; //NOI18N

    private static final String[] JAXRS_LIBRARIES_31 =
             {"jackson-core-asl", "jackson-jaxrs", "jackson-mapper-asl", "jersey-client", 
                 "jersey-core", JERSEY_GF_SERVER, "jersey-json", "jersey-multipart", "jettison", "mimepull"}; //NOI18N

    private static final String JAVA_EE_6_LIB = "Java-EE-GlassFish-v3"; // NOI18N
    private static final String JAVA_EE_5_LIB = "Java-EE-GlassFish-v3-Prelude"; // NOI18N

    private static final String JAVA_EE_JAVADOC = "javaee-doc-api.jar"; // NOI18N
    private static final String JAKARTA_EE_8_JAVADOC = "jakartaee8-doc-api.jar"; // NOI18N
    private static final String JAKARTA_EE_9_JAVADOC = "jakartaee9-doc-api.jar"; // NOI18N
    private static final String JAKARTA_EE_10_JAVADOC = "jakartaee10-doc-api.jar"; // NOI18N
    private static final String JAKARTA_EE_11_JAVADOC = "jakartaee11-doc-api.jar"; // NOI18N

    private static boolean ensureGlassFishApiSupport(GlassFishServer server) {
        String installRoot = server.getServerRoot();
        List<URL> libraryList = Hk2LibraryProvider.getProvider(server).getJavaEEClassPathURLs();
        List<URL> docList = new ArrayList<URL>();
        String name = JAVA_EE_5_LIB;

        File f = ServerUtilities.getJarName(installRoot, "gmbal" + ServerUtilities.GFV3_VERSION_MATCHER);
        if (f != null && f.exists()) {
            name = JAVA_EE_6_LIB;
        }
        
        File j2eeDoc;
        if (GlassFishVersion.ge(server.getVersion(), GlassFishVersion.GF_8_0_0)) {
            j2eeDoc = InstalledFileLocator.getDefault().locate(
                "docs/" + JAKARTA_EE_11_JAVADOC,
                Hk2LibraryProvider.JAVAEE_DOC_CODE_BASE, false);
        } else if (GlassFishVersion.ge(server.getVersion(), GlassFishVersion.GF_7_0_0)) {
            j2eeDoc = InstalledFileLocator.getDefault().locate(
                "docs/" + JAKARTA_EE_10_JAVADOC,
                Hk2LibraryProvider.JAVAEE_DOC_CODE_BASE, false);
        } else if (GlassFishVersion.ge(server.getVersion(), GlassFishVersion.GF_6)) {
            j2eeDoc = InstalledFileLocator.getDefault().locate(
                "docs/" + JAKARTA_EE_9_JAVADOC,
                Hk2LibraryProvider.JAVAEE_DOC_CODE_BASE, false);
        } else if (GlassFishVersion.ge(server.getVersion(), GlassFishVersion.GF_5_1_0)) {
            j2eeDoc = InstalledFileLocator.getDefault().locate(
                "docs/" + JAKARTA_EE_8_JAVADOC,
                Hk2LibraryProvider.JAVAEE_DOC_CODE_BASE, false);
        } else {
            j2eeDoc = InstalledFileLocator.getDefault().locate(
                "docs/" + JAVA_EE_JAVADOC,
                Hk2LibraryProvider.JAVAEE_DOC_CODE_BASE, false);
        }

        if (j2eeDoc != null) {
            try {
                docList.add(ServerUtilities.fileToUrl(j2eeDoc));
            } catch (MalformedURLException ex) {
                LOG.log(Level.INFO, "Problem while registering Java EE API library JavaDoc."); // NOI18N
            }
        } else {
            LOG.log(Level.INFO, "Java EE documentation not found when registering Java EE API library."); // NOI18N
        }

        // additional jar for glassfish-samples support
        f = ServerUtilities.getJarName(installRoot, "web-core" + ServerUtilities.GFV3_VERSION_MATCHER);
        if (f != null && f.exists()) {
            try {
                libraryList.add(ServerUtilities.fileToUrl(f));
            } catch (MalformedURLException ex) {
                LOG.log(Level.INFO, "Problem while registering web-core into GlassFish API library."); // NOI18N
            }
        }

        File jerseyGFServer = ServerUtilities.getJarName(installRoot, JERSEY_GF_SERVER + ServerUtilities.GFV3_VERSION_MATCHER);
        boolean isGFV31 =  jerseyGFServer != null;

        String[] JERSEY_LIBS = (isGFV31 ? JAXRS_LIBRARIES_31 : JAXRS_LIBRARIES);
        for (String entry : JERSEY_LIBS) {
            f = ServerUtilities.getJarName(installRoot, entry + ServerUtilities.GFV3_VERSION_MATCHER);
            if ((f != null) && (f.exists())) {
                try {
                    libraryList.add(
                            FileUtil.getArchiveRoot(Utilities.toURI(f).toURL()));
                } catch (MalformedURLException ex) {
                }
            }
        }
        return addLibrary(name, libraryList, docList,
                NbBundle.getMessage(JavaEEServerModuleFactory.class, "DNAME_GF_JAVA_EE_IMPL"), // NOI18N
                NbBundle.getMessage(JavaEEServerModuleFactory.class, "DESC_GF_JAVA_EE_IMPL")); // NOI18N
    }

    private static boolean addLibrary(String name, List<URL> libraryList, List<URL> docList, String displayName, String description) {
        return addLibrary(name, CLASS_LIBRARY_TYPE, libraryList, docList, displayName, description);
    }

    private static synchronized boolean addLibrary(String name, String libType, List<URL> libraryList, List<URL> docList, String displayName,
            String description) {
        LibraryManager lmgr = LibraryManager.getDefault();

        int size = 0;

        Library lib = lmgr.getLibrary(name);

        // Verify that existing library is still valid.
        if (lib != null) {
            List<URL> libList = lib.getContent(CLASSPATH_VOLUME);
            size = libList.size();
            for (URL libUrl : libList) {
                String libPath = libUrl.getFile();
                // file seems to want to return a file: protocol string... not the FILE portion of the URL
                if (libPath.length() > 5) {
                    libPath = libPath.substring(5);
                }
                if (!new File(libPath.replace("!/", "")).exists()) {
                    LOG.log(Level.FINE, "libPath does not exist.  Updating {0}", name);
                    try {
                        lmgr.removeLibrary(lib);
                    } catch (IOException ex) {
                        LOG.log(Level.INFO, ex.getLocalizedMessage(), ex);
                    } catch (IllegalArgumentException ex) {
                        // Already removed somehow, ignore.
                        }
                    lib = null;
                    size = 0;
                    break;
                }
            }
        }

        // verify that there are not new components in the 'new' definition
        // of the library...  If there are new components... rebuild the library.
        if (lib != null && size < libraryList.size()) {
            try {
                lmgr.removeLibrary(lib);
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getLocalizedMessage(), ex);
            } catch (IllegalArgumentException ex) {
                // Already removed somehow, ignore.
            }
            lib = null;
        }

        if (lib != null) {
            List<URL> libList = lib.getContent(JAVADOC_VOLUME);
            size = libList.size();
            for (URL libUrl : libList) {
                String libPath = libUrl.getFile();
                // file seems to want to return a file: protocol string... not the FILE portion of the URL
                if (libPath.length() > 5) {
                    libPath = libPath.substring(5);
                }
                if (!new File(libPath.replace("!/", "")).exists()) {
                    LOG.log(Level.FINE, "libPath does not exist.  Updating {0}", name);
                    try {
                        lmgr.removeLibrary(lib);
                    } catch (IOException ex) {
                        LOG.log(Level.INFO, ex.getLocalizedMessage(), ex);
                    } catch (IllegalArgumentException ex) {
                        // Already removed somehow, ignore.
                        }
                    lib = null;
                    size = 0;
                    break;
                }
            }
        }

        // verify that there are not new components in the 'new' definition
        // of the library...  If there are new components... rebuild the library.
        if (lib != null && null != docList &&  size < docList.size()) {
            try {
                lmgr.removeLibrary(lib);
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getLocalizedMessage(), ex);
            } catch (IllegalArgumentException ex) {
                // Already removed somehow, ignore.
            }
            lib = null;
        }

        if (lib == null) {
            Map<String, List<URL>> contents;
            try {
                contents = new HashMap<String, List<URL>>();
                if (null != libraryList) {
                    contents.put(CLASSPATH_VOLUME, libraryList);
                }
                if (null != docList) {
                    contents.put(JAVADOC_VOLUME, docList);
                }

                LibraryTypeProvider ltp = LibrariesSupport.getLibraryTypeProvider(libType);
                if (null != ltp) {
                    lib = lmgr.createLibrary(libType, name, displayName, description, contents);
                    LOG.log(Level.FINE, "Created library {0}", name);
                } else {
                    lmgr.addPropertyChangeListener(new InitializeLibrary(lmgr, libType, name, contents, displayName, description));
                    LOG.log(Level.FINE, "schedule to create library {0}", name);
                }
            } catch (IOException ex) {
                // Someone must have created the library in a parallel thread, try again otherwise fail.
                lib = lmgr.getLibrary(name);
                if (lib == null) {
                    LOG.log(Level.INFO, ex.getLocalizedMessage(), ex);
                }
            } catch (IllegalArgumentException ex) {
                // Someone must have created the library in a parallel thread, try again otherwise fail.
                lib = lmgr.getLibrary(name);
                if (lib == null) {
                    LOG.log(Level.INFO, ex.getLocalizedMessage(), ex);
                }
            }
        }
        return lib != null;
    }

    static class InitializeLibrary implements PropertyChangeListener {

        private final LibraryManager lmgr;
        private String name;
        private Map<String, List<URL>> content;
        private final String libType;
        private final String displayName;
        private final String description;

        InitializeLibrary(LibraryManager lmgr, String libType, String name, Map<String, List<URL>> content,
                String displayName, String description) {
            this.lmgr = lmgr;
            this.name = name;
            this.content = content;
            this.libType = libType;
            this.displayName = displayName;
            this.description = description;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            synchronized (singleton) {
                if (null != name) {
                    Library l = lmgr.getLibrary(name);
                    final PropertyChangeListener pcl = this;
                    if (null == l) {
                        try {
                            LibraryTypeProvider ltp = LibrariesSupport.getLibraryTypeProvider(libType);
                            if (null != ltp) {
                                lmgr.createLibrary(libType, name, displayName, description, content);
                                LOG.log(Level.FINE, "Created library {0}", name);
                                removeFromListenerList(pcl);
                            }
                        } catch (IOException ex) {
                            LOG.log(Level.INFO,
                                    ex.getLocalizedMessage(), ex);
                        } catch (IllegalArgumentException iae) {
                            LOG.log(Level.INFO,
                                    iae.getLocalizedMessage(), iae);
                        }
                    } else {
                        // The library is there... and the listener is still active... hmmm.
                        removeFromListenerList(pcl);
                    }
                }
            }
        }

        private void removeFromListenerList(final PropertyChangeListener pcl) {
            RP.post(new Runnable() {

                @Override
                public void run() {
                    synchronized (singleton) {
                    if (null != lmgr) {
                        lmgr.removePropertyChangeListener(pcl);
                        content = null;
                        name = null;
                    }
                    }
                }
            });
        }
    }
}
