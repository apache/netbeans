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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import org.netbeans.api.j2ee.core.Profile;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.config.J2eeModuleAccessor;
import org.netbeans.modules.j2ee.deployment.impl.sharability.ServerLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl2;
import org.netbeans.modules.j2ee.deployment.plugins.spi.support.LookupProviderSupport;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;


/**
 * J2eePlatform describes the target environment J2EE applications are build against
 * and subsequently deployed to. Each server instance defines its own J2EE platform.
 *
 * @author Stepan Herold
 * @since 1.5
 */
public final class J2eePlatform implements Lookup.Provider {

    /**
     * Type of the library created by {@link #createLibrary(File, String)}.
     * 
     * @since 1.40
     */
    @Deprecated
    public static final String LIBRARY_TYPE = ServerLibraryTypeProvider.LIBRARY_TYPE;

    /** Display name property */
    public static final String PROP_DISPLAY_NAME = "displayName"; //NOI18N
    /** Classpath property */
    public static final String PROP_CLASSPATH = "classpath"; //NOI18N
    /** Platform roots property */
    public static final String PROP_PLATFORM_ROOTS = "platformRoots"; //NOI18N

    /**
     * Constant for the application runtime tool. The standard properties defined
     * for this tool are as follows {@link #TOOL_PROP_MAIN_CLASS},
     * {@link #TOOL_PROP_MAIN_CLASS_ARGS}, {@link #TOOL_PROP_JVM_OPTS}
     * @since 1.16
     */
    public static final String TOOL_APP_CLIENT_RUNTIME = "appClientRuntime";     // NOI18N

    /**
     * Constant for the JSR109 tool.
     * @since 1.16
     */
    public static final String TOOL_JSR109      = "jsr109";     // NOI18N

    /**
     * Constant for the WSCOMPILE tool.
     * @since 1.16
     */
    public static final String TOOL_WSCOMPILE   = "wscompile";  // NOI18N

    /**
     * Constant for the embedabble EJB (Java EE 6).
     * @since 1.60
     */
    public static final String TOOL_EMBEDDABLE_EJB   = "embeddableejb";  // NOI18N

    /**
     * Constant for the WSIMPORT tool.
     * @since 1.16
     */
    public static final String TOOL_WSIMPORT    = "wsimport";   // NOI18N

    /**
     * Constant for the WSGEN tool.
     * @since 1.16
     */
    public static final String TOOL_WSGEN       = "wsgen";      // NOI18N

    /**
     * Constant for the WSIT tool.
     * @since 1.16
     */
    public static final String TOOL_WSIT        = "wsit";       // NOI18N

    /**
     * Constant for the JWSDP tool.
     * @since 1.16
     */
    public static final String TOOL_JWSDP       = "jwsdp";      // NOI18N

    /**
     * Constant for the KEYSTORE tool.
     * @since 1.16
     */
    public static final String TOOL_KEYSTORE        = "keystore";       // NOI18N

    /**
     * Constant for the KEYSTORE_CLIENT tool.
     * @since 1.16
     */
    public static final String TOOL_KEYSTORE_CLIENT = "keystoreClient"; // NOI18N

    /**
     * Constant for the TRUSTSTORE tool.
     * @since 1.16
     */
    public static final String TOOL_TRUSTSTORE      = "truststore";     // NOI18N

    /**
     * Constant for the TRUSTSTORE_CLIENT tool.
     * @since 1.16
     */
    public static final String TOOL_TRUSTSTORE_CLIENT = "truststoreClient";     // NOI18N

    /**
     * Constant for the main class tool property.
     * @since 1.16
     */
    public static final String TOOL_PROP_MAIN_CLASS         = "main.class";     // NOI18N

    /**
     * Constant for the main class arguments tool property.
     * @since 1.16
     */
    public static final String TOOL_PROP_MAIN_CLASS_ARGS    = "main.class.args"; // NOI18N

    /**
     * Constant for the JVM options tool property.
     * @since 1.16
     */
    public static final String TOOL_PROP_JVM_OPTS           = "jvm.opts";       // NOI18N

    /**
     * Tool property constant for application client jar location.
     * @since 1.40
     */
    public static final String TOOL_PROP_CLIENT_JAR_LOCATION = "client.jar.location";       // NOI18N

    /**
     * Constant for the distribution archive client property. Some of the tool
     * property values may refer to this property.
     * @since 1.16
     */
    public static final String CLIENT_PROP_DIST_ARCHIVE     = "client.dist.archive"; // NOI18N

    private static final String DEFAULT_ICON = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/Servers.png"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(J2eePlatform.class.getName());

    private final J2eePlatformImpl impl;
    private File[] classpathCache;
    private String currentClasspath;
    private final ServerInstance serverInstance;

    // listens to libraries content changes
    private PropertyChangeListener librariesChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(LibraryImplementation.PROP_CONTENT)) {
                classpathCache = null;
                String newClassPath = getClasspathAsString();
                if (currentClasspath == null || !currentClasspath.equals(newClassPath)) {
                    currentClasspath = newClassPath;
                    impl.firePropertyChange(PROP_CLASSPATH, null, null);
                }
            }
        }
    };

    /**
     * Creates a new instance of J2eePlatform.
     *
     * @param aImpl instance of <code>J2eePlatformImpl</code>.
     */
    private J2eePlatform(ServerInstance aServerInstance, J2eePlatformImpl aImpl) {
        assert aServerInstance != null;
        impl = aImpl;
        serverInstance = aServerInstance;
        // listens to libraries changes
        addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (J2eePlatformImpl.PROP_LIBRARIES.equals(evt.getPropertyName())) {
                    LibraryImplementation[] libs = getLibraries();
                    for (int i = 0; i < libs.length; i++) {
                        libs[i].removePropertyChangeListener(librariesChangeListener);
                        libs[i].addPropertyChangeListener(librariesChangeListener);
                    }

                    classpathCache = null;
                    String newClassPath = getClasspathAsString();
                    if (currentClasspath == null || !currentClasspath.equals(newClassPath)) {
                        currentClasspath = newClassPath;
                        impl.firePropertyChange(PROP_CLASSPATH, null, null);
                    }
                } else if (J2eePlatformImpl.PROP_SERVER_LIBRARIES.equals(evt.getPropertyName())) {
                    impl.firePropertyChange(PROP_CLASSPATH, null, null);
                }
            }
        });
        LibraryImplementation[] libs = getLibraries();
        for (int i = 0; i < libs.length; i++) {
            libs[i].addPropertyChangeListener(librariesChangeListener);
        }
        currentClasspath = getClasspathAsString();
    }

    static J2eePlatform create(ServerInstance serInst) {
        J2eePlatform result = serInst.getJ2eePlatform();
        if (result == null) {
            J2eePlatformImpl platformImpl = serInst.getJ2eePlatformImpl();
            if (platformImpl != null) {
                result = new J2eePlatform(serInst, platformImpl);
                serInst.setJ2eePlatform(result);
            }
        }
        return result;
    }

    /**
     * Return classpath entries.
     *
     * @return classpath entries.
     */
    @NonNull
    public File[] getClasspathEntries() {
        if (classpathCache == null) {
            List<File> classpath = getClasspath(impl.getLibraries());
            classpathCache = (File[]) classpath.toArray(new File[0]);
        }
        return classpathCache;
    }

    @NonNull
    public File[] getClasspathEntries(Set<ServerLibraryDependency> libraries) {
        // FIXME optimize - cache
        List<File> classpath = getClasspath(impl.getLibraries(libraries));
        return (File[]) classpath.toArray(new File[0]);
    }
    
    private List<File> getClasspath(LibraryImplementation[] libraries) {
        List<File> classpath = new ArrayList<>();
        for (int i = 0; i < libraries.length; i++) {
            List classpathList = libraries[i].getContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH);
            for (Iterator iter = classpathList.iterator(); iter.hasNext();) {
                URL url = (URL)iter.next();
                if ("jar".equals(url.getProtocol())) { //NOI18N
                    url = FileUtil.getArchiveFile(url);
                }
                FileObject fo = URLMapper.findFileObject(url);
                if (fo != null) {
                    File f = FileUtil.toFile(fo);
                    if (f != null) {
                        classpath.add(f);
                    }
                }
            }
        }
        return classpath;
    }

    /**
     * Return classpath for the specified tool. Use the tool constants declared
     * in this class.
     *
     * @param  toolName tool name, for example {@link #TOOL_APP_CLIENT_RUNTIME}.
     * @return classpath for the specified tool.
     */
    public File[] getToolClasspathEntries(String toolName) {
        return impl.getToolClasspathEntries(toolName);
    }

    /**
     * Returns the property value for the specified tool.
     * <p>
     * The property value uses Ant property format and therefore may contain
     * references to another properties defined either by the client of this API
     * or by the tool itself.
     * <p>
     * The properties the client may be requited to define are as follows
     * {@link #CLIENT_PROP_DIST_ARCHIVE}
     *
     * @param toolName tool name, for example {@link #TOOL_APP_CLIENT_RUNTIME}.
     * @param propertyName tool property name, for example {@link #TOOL_PROP_MAIN_CLASS}.
     *
     * @return property value or null, if the property is not defined for the
     *         specified tool.
     *
     * @since 1.16
     * @deprecated {@link #getLookup()} should be used to obtain tool specifics
     */
    @Deprecated
    public String getToolProperty(String toolName, String propertyName) {
        return impl.getToolProperty(toolName, propertyName);
    }

    /**
     * Specifies whether a tool of the given name is supported by this platform.
     * Use the tool constants declared in this class.
     *
     * @param  toolName tool name, for example {@link #TOOL_APP_CLIENT_RUNTIME}.
     * @return <code>true</code> if platform supports tool of the given name,
     *         <code>false</code> otherwise.
     * @deprecated {@link #getLookup()} should be used to obtain tool specifics
     */
    @Deprecated
    public boolean isToolSupported(String toolName) {
        return impl.isToolSupported(toolName);
    }

    // this will be made public and will return Library
    LibraryImplementation[] getLibraries() {
        return impl.getLibraries();
    }

    /**
     * Return platform's display name.
     *
     * @return platform's display name.
     */
    public String getDisplayName() {
        // return impl.getDisplayName();
        // AB: for now return server instance's display name
        return serverInstance.getDisplayName();
    }

    /**
     * Return platform's icon.
     *
     * @return platform's icon.
     * @since 1.6
     */
    public Image getIcon() {
        Image result = impl.getIcon();
        if (result == null)
            result = ImageUtilities.loadImage(DEFAULT_ICON);

        return result;
    }

    /**
     * Return platform's root directories. This will be mostly server's installation
     * directory.
     *
     * @return platform's root directories.
     * @deprecated use {@link #getServerHome()} or {@link #getDomainHome()}
     *             or {@link #getMiddlewareHome()}
     */
    @Deprecated
    public File[] getPlatformRoots() {
        return impl.getPlatformRoots();
    }
    
    /**
     * Returns the server installation directory or <code>null</code> if not
     * specified or unknown.
     * 
     * @return the server installation directory or <code>null</code> if not
     *            specified or unknown
     * @since 1.72
     * @see J2eePlatformImpl2#getServerHome() 
     */
    @CheckForNull
    public File getServerHome() {
        if (impl instanceof J2eePlatformImpl2) {
            return ((J2eePlatformImpl2) impl).getServerHome();
        }
        return null;
    }
    
    /**
     * Returns the domain directory or <code>null</code> if not
     * specified or unknown. Many Java EE servers allows usage of multiple
     * server instances using single binaries. In such case this method should
     * return the installation/configuration directory of such instance.
     * 
     * @return the domain directory or <code>null</code> if not
     *            specified or unknown
     * @since 1.72
     * @see J2eePlatformImpl2#getDomainHome() 
     */  
    @CheckForNull
    public File getDomainHome() {
        if (impl instanceof J2eePlatformImpl2) {
            return ((J2eePlatformImpl2) impl).getDomainHome();
        }
        return null;        
    }
    
    /**
     * Returns the middleware directory or <code>null</code> if not
     * specified or unknown. Some servers share certain binaries on higher level
     * with other products of the same vendor. In such case this method should
     * return the appropriate directory.
     * 
     * @return the middleware directory or <code>null</code> if not
     *            specified or unknown
     * @since 1.72
     * @see J2eePlatformImpl2#getMiddlewareHome() 
     */    
    @CheckForNull
    public File getMiddlewareHome() {
        if (impl instanceof J2eePlatformImpl2) {
            return ((J2eePlatformImpl2) impl).getMiddlewareHome();
        }
        return null;         
    }

    /**
     * Return a list of supported J2EE specification versions. Use J2EE specification
     * versions defined in the {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule}
     * class.
     *
     * @return list of supported J2EE specification versions.
     * @deprecated use {@link #getSupportedProfiles()}
     */
    @Deprecated
    public Set/*<String>*/ getSupportedSpecVersions() {
        boolean assertsEnabled = false;
        assert assertsEnabled = true;
        if (assertsEnabled) {
            LOGGER.log(Level.INFO, "Call to deprecated method "
                    + J2eePlatform.class.getName() + "getSupportedSpecVersions", new Exception());
        }

        return convertProfilesToKnownSpecVersions(getSupportedProfiles());
    }

    /**
     * Return a list of supported J2EE specification versions for
     * a given module type.
     *
     * @param moduleType one of the constants defined in
     *   {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule}
     * @return list of supported J2EE specification versions.
     * @deprecated use {@link #getSupportedProfiles(Type)}
     */
    @Deprecated
    public Set<String> getSupportedSpecVersions(Object moduleType) {
        boolean assertsEnabled = false;
        assert assertsEnabled = true;
        if (assertsEnabled) {
            LOGGER.log(Level.INFO, "Call to deprecated method "
                    + J2eePlatform.class.getName() + "getSupportedSpecVersions", new Exception());
        }

        J2eeModule.Type type = J2eeModule.Type.fromJsrType(moduleType);
        if (type != null) {
            return convertProfilesToKnownSpecVersions(getSupportedProfiles(type));
        }
        return Collections.emptySet();
    }

    /**
     * Returns the set of supported profiles (terminology of Java EE 6). There
     * are also profiles for J2EE 1.4 and Java EE 5.
     *
     * @return set of {@link Profile}s supported by the server.
     * @see Profile
     * @since 1.58
     */
    // TODO filter out J2EE 1.3 in future
    @NonNull
    public Set<Profile> getSupportedProfiles() {
        return impl.getSupportedProfiles();
    }

    /**
     * Returns the set of supported profiles (terminology of Java EE 6) for
     * the given module type (one of {@link J2eeModule.Type#EAR},
     * {@link J2eeModule.Type#EJB}, {@link J2eeModule.Type#WAR}, {@link J2eeModule.Type#RAR}
     * and {@link J2eeModule.Type#CAR}).
     *
     * @param moduleType type of the module
     * @return set of {@link Profile}s supported by the server.
     * @see Profile
     * @since 1.59
     */
    @NonNull
    // TODO filter out J2EE 1.3 in future
    public Set<Profile> getSupportedProfiles(@NonNull J2eeModule.Type moduleType) {
        return impl.getSupportedProfiles(moduleType);
    }

    /**
     * Return a list of supported J2EE module types. Use module types defined in the
     * {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule}
     * class.
     *
     * @return list of supported J2EE module types.
     * @deprecated use {@link #getSupportedTypes()}
     */
    @Deprecated
    public Set getSupportedModuleTypes() {
        boolean assertsEnabled = false;
        assert assertsEnabled = true;
        if (assertsEnabled) {
            LOGGER.log(Level.INFO, "Call to deprecated method "
                    + J2eePlatform.class.getName() + "getSupportedModuleTypes", new Exception());
        }

        Set ret = new HashSet();
        for (J2eeModule.Type type : getSupportedTypes()) {
            Object obj = J2eeModuleAccessor.getDefault().getJsrModuleType(type);
            if (obj != null) {
                ret.add(obj);
            }
        }
        return ret;
    }

    /**
     * Return a list of supported module types. Use module types defined in the
     * {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type}
     * class.
     *
     * @return set of supported module types
     * @since 1.59
     */
    public Set<J2eeModule.Type> getSupportedTypes() {
        return impl.getSupportedTypes();
    }

    /**
     * Return a set of J2SE platform versions this J2EE platform can run with.
     * Versions should be specified as strings i.g. ("1.3", "1.4", etc.)
     *
     * @return set of J2SE platform versions this J2EE platform can run with.
     *
     * @since 1.9
     */
    public Set getSupportedJavaPlatformVersions() {
        return impl.getSupportedJavaPlatformVersions();
    }

    /**
     * Is profiling supported by this J2EE platform?
     *
     * @return true, if profiling is supported, false otherwise.
     *
     * @since 1.9
     */
    public boolean supportsProfiling() {
        return true;
    }

    /**
     * Return server J2SE platform or null if the platform is unknown, not
     * registered in the IDE.
     *
     * @return server J2SE platform or null if the platform is unknown, not
     *         registered in the IDE.
     *
     * @since 1.9
     */
    public JavaPlatform getJavaPlatform() {
        return impl.getJavaPlatform();
    }

    /**
     * Register a listener which will be notified when some of the platform's properties
     * change.
     *
     * @param l listener which should be added.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        impl.addPropertyChangeListener(l);
    }

    /**
     * Remove a listener registered previously.
     *
     * @param l listener which should be removed.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        impl.removePropertyChangeListener(l);
    }

    @Override
    public String toString() {
        return impl.getDisplayName() + " [" + getClasspathAsString() + "]"; // NOI18N
    }

    /**
     * Creates sharable Java library containing all libraries and sources
     * provided by this platform. All files are copied to shared location and
     * library is created.
     *
     * @param location sharable libraries location
     * @param libraryName name of the library
     * @return created library
     * @throws java.io.IOException if the library can't be created for some reason
     * @since 1.40
     */
    @Deprecated
    public Library createLibrary(File location, String libraryName) throws IOException {
        Parameters.notNull("location", location); // NOI18N

        File parent = location.getAbsoluteFile().getParentFile();
        if (parent == null) {
            throw new IOException("Wrong library location " + location); // NOI18N
        }
        FileObject baseFolder = FileUtil.toFileObject(parent);
        if (baseFolder == null) {
            baseFolder = FileUtil.createFolder(parent);
        }

        LibraryManager manager = LibraryManager.forLocation(location.toURI().toURL());
        Map<String, List<URI>> content = new HashMap<String, List<URI>>();

        String folderName = getFolderName(baseFolder, libraryName);
        FileObject jarFolder = FileUtil.createFolder(baseFolder, folderName);

        Map<String, Integer> usedNames = new  HashMap<String, Integer>();
        Map<FileObject, String> copied = new  HashMap<FileObject, String>();

        List<URI> contentItem = new ArrayList<URI>();
        content.put(ServerLibraryTypeProvider.VOLUME_CLASSPATH, contentItem);
        copyFiles(copied, usedNames, jarFolder, folderName,
                getVolumeContent(this, J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH), contentItem);

        contentItem = new  ArrayList<URI>();
        content.put(ServerLibraryTypeProvider.VOLUME_EMBEDDABLE_EJB_CLASSPATH, contentItem);
        copyFiles(copied, usedNames, jarFolder, folderName,
                getToolClasspathEntries(TOOL_EMBEDDABLE_EJB), contentItem);

        contentItem = new  ArrayList<URI>();
        content.put(ServerLibraryTypeProvider.VOLUME_WS_COMPILE_CLASSPATH, contentItem);
        copyFiles(copied, usedNames, jarFolder, folderName,
                getToolClasspathEntries(TOOL_WSCOMPILE), contentItem);

        contentItem = new  ArrayList<URI>();
        content.put(ServerLibraryTypeProvider.VOLUME_WS_GENERATE_CLASSPATH, contentItem);
        copyFiles(copied, usedNames, jarFolder, folderName,
                getToolClasspathEntries(TOOL_WSGEN), contentItem);

        contentItem = new  ArrayList<URI>();
        content.put(ServerLibraryTypeProvider.VOLUME_WS_IMPORT_CLASSPATH, contentItem);
        copyFiles(copied, usedNames, jarFolder, folderName,
                getToolClasspathEntries(TOOL_WSIMPORT), contentItem);

        contentItem = new  ArrayList<URI>();
        content.put(ServerLibraryTypeProvider.VOLUME_WS_INTEROP_CLASSPATH, contentItem);
        copyFiles(copied, usedNames, jarFolder, folderName,
                getToolClasspathEntries(TOOL_WSIT), contentItem);

        contentItem = new  ArrayList<URI>();
        content.put(ServerLibraryTypeProvider.VOLUME_WS_JWSDP_CLASSPATH, contentItem);
        copyFiles(copied, usedNames, jarFolder, folderName,
                getToolClasspathEntries(TOOL_JWSDP), contentItem);

        contentItem = new ArrayList<URI>();
        content.put(ServerLibraryTypeProvider.VOLUME_JAVADOC, contentItem);
        copyFiles(copied, usedNames, jarFolder, folderName,
                getVolumeContent(this, J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC), contentItem);

        contentItem = new ArrayList<URI>();
        content.put(ServerLibraryTypeProvider.VOLUME_SOURCE, contentItem);
        copyFiles(copied, usedNames, jarFolder, folderName,
                getVolumeContent(this, J2eeLibraryTypeProvider.VOLUME_TYPE_SRC), contentItem);

        return manager.createURILibrary(ServerLibraryTypeProvider.LIBRARY_TYPE, libraryName, content); // NOI18N
    }

    /**
     * Lookup providing a way to find non mandatory technologies supported
     * by the platform.
     * <div class="nonnormative">
     * The typical example of such support is a webservice stack.
     * </div>
     *
     * @return Lookup providing way to find other supported technologies
     * @since 1.44
     */
    public Lookup getLookup() {
        return impl.getLookup();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final J2eePlatform other = (J2eePlatform) obj;
        if (this.serverInstance.getUrl() != other.serverInstance.getUrl()
                && (this.serverInstance.getUrl() == null || !this.serverInstance.getUrl().equals(other.serverInstance.getUrl()))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (this.serverInstance.getUrl() != null ? this.serverInstance.getUrl().hashCode() : 0);
        return hash;
    }

    @NonNull
    @SuppressWarnings("deprecation")
    private Set<String> convertProfilesToKnownSpecVersions(Iterable<Profile> profiles) {
        Set ret = new HashSet();
        for (Profile profile : profiles) {
            String str = profile.toPropertiesString();
            if (J2eeModule.J2EE_13.equals(str)
                    || J2eeModule.J2EE_14.equals(str)
                    || J2eeModule.JAVA_EE_5.equals(str)) {
                ret.add(str);
            }
        }
        return ret;
    }

    private FileObject[] getVolumeContent(J2eePlatform platform, String volumeType) {
        LibraryImplementation[] libraries = platform.getLibraries();
        List<FileObject> ret = new ArrayList<FileObject>();
        for (int i = 0; i < libraries.length; i++) {
            for (URL url : libraries[i].getContent(volumeType)) {
                if ("jar".equals(url.getProtocol())) { // NOI18N
                    url = FileUtil.getArchiveFile(url);
                }
                FileObject fo = URLMapper.findFileObject(url);
                if (fo != null) {
                    ret.add(fo);
                }
            }
        }
        return ret.toArray(new FileObject[0]);
    }

    private void copyFiles(Map<FileObject, String> copied, Map<String, Integer> usedNames,
            FileObject jarFolder, String folderName, File[] files, List<URI> content) throws IOException {

        if (files == null) {
            return;
        }

        List<FileObject> fileObjects = new  ArrayList<FileObject>();
        for (File jarFile : files) {
            File normalized = FileUtil.normalizeFile(jarFile);
            FileObject jarObject = FileUtil.toFileObject(normalized);
            if (jarObject != null) {
                fileObjects.add(jarObject);
            } else {
                LOGGER.log(Level.INFO, "Could not find " + jarFile); // NOI18N
            }
        }
        copyFiles(copied, usedNames, jarFolder, folderName,
                fileObjects.toArray(new FileObject[0]), content);
    }

    private void copyFiles(Map<FileObject, String> copied, Map<String, Integer> usedNames,
            FileObject jarFolder, String folderName, FileObject[] files, List<URI> content) throws IOException {

        if (files == null) {
            return;
        }

        for (FileObject jarObject : files) {
            if (!copied.containsKey(jarObject)) {
                String name = jarObject.getName() + getEntrySuffix(jarObject.getNameExt(), usedNames);
                if (jarObject.isFolder()) {
                    FileObject folder = FileUtil.createFolder(jarFolder, name);
                    copyFolder(jarObject, folder);
                } else {
                    FileUtil.copyFile(jarObject, jarFolder, name, jarObject.getExt());
                }
                copied.put(jarObject, jarObject.getNameExt().replace(jarObject.getName(), name));
            }

            FileObject fresh = jarFolder.getFileObject(copied.get(jarObject));
            URI u = LibrariesSupport.convertFilePathToURI(folderName
                    + File.separator + copied.get(jarObject));

            if (FileUtil.isArchiveFile(fresh)) {
                u = LibrariesSupport.getArchiveRoot(u);
            }

            if (!content.contains(u)) {
                content.add(u);
            }
        }
    }

    private void copyFolder(FileObject source, FileObject dest) throws IOException {
        assert source.isFolder() : "Source is not a folder"; // NOI18N
        assert dest.isFolder() : "Source is not a folder"; // NOI18N

        for (FileObject child : source.getChildren()) {
            if (child.isFolder()) {
                FileObject created = FileUtil.createFolder(dest, child.getNameExt());
                copyFolder(child, created);
            } else {
                FileUtil.copyFile(child, dest, child.getName(), child.getExt());
            }
        }
    }

    private String getEntrySuffix(String realName, Map<String, Integer> usages) {
        Integer value = usages.get(realName);
        if (value == null) {
            value = 0;
        } else {
            value = value + 1;
        }

        usages.put(realName, value);
        if (value == 0) {
            return ""; // NOI18N
        }
        return "-" + value.toString();
    }

    private String getFolderName(FileObject baseFolder, String libraryName) {
        int suffix = 2;
        String baseName = libraryName;  //NOI18N

        String name = baseName;
        while (baseFolder.getFileObject(name) != null) {
            name = baseName + "-" + suffix; // NOI18N
            suffix++;
        }
        return name;
    }

    private String getClasspathAsString() {
        File[] classpathEntr = getClasspathEntries();
        StringBuilder classpath = new StringBuilder();
        final String PATH_SEPARATOR = System.getProperty("path.separator"); // NOI18N
        for (int i = 0; i < classpathEntr.length; i++) {
            classpath.append(classpathEntr[i].getAbsolutePath());
            if (i + 1 < classpathEntr.length) {
                classpath.append(PATH_SEPARATOR);
            }
        }
        return classpath.toString();
    }
}
