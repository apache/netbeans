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
package org.netbeans.modules.payara.jakartaee;

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
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.netbeans.modules.payara.spi.PayaraModuleFactory;
import org.netbeans.modules.payara.spi.RegisteredDerbyServer;
import org.netbeans.modules.payara.spi.ServerUtilities;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Peter Williams
 */
public class JavaEEServerModuleFactory implements PayaraModuleFactory {

    private static final Logger LOGGER = Logger.getLogger("payara-jakartaee");

    private static final JavaEEServerModuleFactory singleton = new JavaEEServerModuleFactory();
    
    private JavaEEServerModuleFactory() {
    }
    
    public static PayaraModuleFactory getDefault() {
        return singleton;
    }
    
    @Override
    public boolean isModuleSupported(String payaraHome, Properties asenvProps) {
        // Do some moderate sanity checking to see if this build looks ok.
        File jar = ServerUtilities.getJarName(payaraHome, ServerUtilities.GF_JAR_MATCHER);

        if (jar==null) {
            return false;
        }
        return jar.exists();
    }

    private static final RequestProcessor RP = new RequestProcessor("JavaEEServerModuleFactory");

    @Override
    public Object createModule(Lookup instanceLookup) {
        // When creating JavaEE support, also ensure this instance is added to j2eeserver
        InstanceProperties ip = null;
        final PayaraModule commonModule
                = instanceLookup.lookup(PayaraModule.class);
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
                            LOGGER.log(Level.WARNING, null, ex); // NOI18N
                        }
                    }

                if(ip == null) {
                    LOGGER.log(Level.INFO, "Unable to create/locate J2EE InstanceProperties for {0}", url);
                }
            }

            final String installRoot = commonModule.getInstanceProperties().get(
                    PayaraModule.INSTALL_FOLDER_ATTR);
            RP.post(new Runnable() {
                @Override
                public void run() {
                    ensureEclipseLinkSupport(commonModule.getInstance());
                    ensurePayaraApiSupport(commonModule.getInstance());
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
            LOGGER.log(Level.WARNING, "commonModule is NULL");
        }

        return (ip != null) ? new JavaEEServerModule(instanceLookup, ip) : null;
    }
    
    private static final String CLASS_LIBRARY_TYPE = "j2se"; // NOI18N
    private static final String CLASSPATH_VOLUME = "classpath"; // NOI18N
    private static final String JAVADOC_VOLUME = "javadoc"; // NOI18N
    
    private static final String ECLIPSE_LINK_LIB = "EclipseLink-GlassFish-v"; // NOI18N
    private static final String EL_CORE_JAR_MATCHER = "eclipselink-wrapper" + ServerUtilities.VERSION_MATCHER; // NOI18N

    private static final String PERSISTENCE_API_JAR_MATCHER_1 = "javax.javaee" + ServerUtilities.VERSION_MATCHER; // NOI18N
    private static final String PERSISTENCE_API_JAR_MATCHER_2 = "javax.persistence" + ServerUtilities.VERSION_MATCHER; // NOI18N
        
    private static final String PERSISTENCE_JAVADOC = "javaee-doc-api.jar"; // NOI18N
    
    private static boolean ensureEclipseLinkSupport(PayaraServer server) {
        String payaraHome = server.getServerHome();
        List<URL> libraryList = new ArrayList<URL>();
        List<URL> docList = new ArrayList<URL>();
        try {
            File f = ServerUtilities.getJarName(payaraHome, EL_CORE_JAR_MATCHER);
            if (f != null && f.exists()) {
                libraryList.add(ServerUtilities.fileToUrl(f));
            } else {// we are in the final V3 Prelude jar name structure
                // find the org.eclipse.persistence*.jar files and add them
                for (File candidate : new File(payaraHome, "modules").listFiles()) {// NOI18N
                    if (candidate.getName().indexOf("org.eclipse.persistence") != -1) {// NOI18N
                        libraryList.add(ServerUtilities.fileToUrl(candidate));
                    }
                }
            }
            f = ServerUtilities.getJarName(payaraHome, PERSISTENCE_API_JAR_MATCHER_1);
            if (f != null && f.exists()) {
                libraryList.add(ServerUtilities.fileToUrl(f));
            } else {
                f = ServerUtilities.getJarName(payaraHome, PERSISTENCE_API_JAR_MATCHER_2);
                if (f != null && f.exists()) {
                    libraryList.add(ServerUtilities.fileToUrl(f));
                }
            }

            File j2eeDoc = InstalledFileLocator.getDefault().locate(
                    "docs/" + PERSISTENCE_JAVADOC,
                    Hk2LibraryProvider.JAVAEE_DOC_CODE_BASE, false);
            if (j2eeDoc != null) {
                docList.add(ServerUtilities.fileToUrl(j2eeDoc));
            } else {
                LOGGER.log(Level.WARNING, "Warning: Java EE documentation not found when registering EclipseLink library.");
            }
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            return false;
        }
        return addLibrary(ECLIPSE_LINK_LIB + server.getVersion().getMajor(), libraryList, docList,
                NbBundle.getMessage(JavaEEServerModuleFactory.class, "DNAME_PF_ECLIPSELINK", server.getVersion().getMajor()),  // NOI18N
                NbBundle.getMessage(JavaEEServerModuleFactory.class, "DESC_PF_ECLIPSELINK"));  // NOI18N
    }

    private static final String[] PAYARA_LIBRARIES
            = {"web-core", "payara-api"}; //NOI18N

    private static final String JAVA_EE_LIB = "Java-EE-Payara-v"; // NOI18N

    private static final String JAVA_EE_JAVADOC = "javaee-doc-api.jar"; // NOI18N

    private static boolean ensurePayaraApiSupport(PayaraServer server) {
        String payaraHome = server.getServerHome();
        Hk2LibraryProvider provider = Hk2LibraryProvider.getProvider(server);
        List<URL> libraryList = new ArrayList<>();
        libraryList.addAll(provider.getJavaEEClassPathURLs());
        libraryList.addAll(provider.getMicroProfileClassPathURLs());
        libraryList.addAll(provider.getJerseyClassPathURLs());

        List<URL> docList = new ArrayList<>();
        File j2eeDoc = InstalledFileLocator.getDefault().locate(
                "docs/" + JAVA_EE_JAVADOC,
                Hk2LibraryProvider.JAVAEE_DOC_CODE_BASE, false);
        if (j2eeDoc != null) {
            try {
                docList.add(ServerUtilities.fileToUrl(j2eeDoc));
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.INFO, "Problem while registering Java EE API library JavaDoc."); // NOI18N
            }
        } else {
            LOGGER.log(Level.INFO, "Java EE documentation not found when registering Java EE API library."); // NOI18N
        }

        for (String entry : PAYARA_LIBRARIES) {
            File f = ServerUtilities.getJarName(payaraHome, entry + ServerUtilities.VERSION_MATCHER);
            if (f != null && f.exists()) {
                try {
                    libraryList.add(ServerUtilities.fileToUrl(f));
                } catch (MalformedURLException ex) {
                    LOGGER.log(Level.INFO, "Problem while registering web-core into Payara API library."); // NOI18N
                }
            }
        }

        return addLibrary(JAVA_EE_LIB + server.getVersion().getMajor(), libraryList, docList,
                NbBundle.getMessage(JavaEEServerModuleFactory.class, "DNAME_PF_JAVA_EE_IMPL", server.getVersion().getMajor()), // NOI18N
                NbBundle.getMessage(JavaEEServerModuleFactory.class, "DESC_PF_JAVA_EE_IMPL")); // NOI18N
    }

    private static boolean addLibrary(String name, List<URL> libraryList, List<URL> docList, String displayName, String description) {
        return addLibrary(name, CLASS_LIBRARY_TYPE, libraryList, docList, displayName, description);
    }

    private synchronized static boolean addLibrary(
            String name, String libType, List<URL> libraryList,
            List<URL> docList, String displayName, String description) {
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
                    LOGGER.log(Level.FINE, "libPath does not exist.  Updating {0}", name);
                    try {
                        lmgr.removeLibrary(lib);
                    } catch (IOException ex) {
                        LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
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
                LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
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
                    LOGGER.log(Level.FINE, "libPath does not exist.  Updating {0}", name);
                    try {
                        lmgr.removeLibrary(lib);
                    } catch (IOException ex) {
                        LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
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
                LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
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
                    LOGGER.log(Level.FINE, "Created library {0}", name);
                } else {
                    lmgr.addPropertyChangeListener(new InitializeLibrary(lmgr, libType, name, contents, displayName, description));
                    LOGGER.log(Level.FINE, "schedule to create library {0}", name);
                }
            } catch (IOException | IllegalArgumentException ex) {
                // Someone must have created the library in a parallel thread, try again otherwise fail.
                lib = lmgr.getLibrary(name);
                if (lib == null) {
                    LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
                }
            }
            // Someone must have created the library in a parallel thread, try again otherwise fail.
            
        }
        return lib != null;
    }

    static class InitializeLibrary implements PropertyChangeListener {

        final private LibraryManager lmgr;
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
                                LOGGER.log(Level.FINE, "Created library {0}", name);
                                removeFromListenerList(pcl);
                            }
                        } catch (IOException | IllegalArgumentException ex) {
                            LOGGER.log(Level.INFO,
                                    ex.getLocalizedMessage(), ex);
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
