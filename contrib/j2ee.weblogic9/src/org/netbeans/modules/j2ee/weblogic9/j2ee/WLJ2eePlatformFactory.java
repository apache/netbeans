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
package org.netbeans.modules.j2ee.weblogic9.j2ee;


import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.netbeans.modules.j2ee.deployment.plugins.spi.support.LookupProviderSupport;
import org.openide.modules.InstalledFileLocator;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl2;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.config.WLServerLibrarySupport;
import org.netbeans.modules.j2ee.weblogic9.config.WLServerLibrarySupport.WLServerLibrary;
import org.netbeans.modules.javaee.specs.support.api.JaxWs;
import org.netbeans.modules.weblogic.common.api.WebLogicLayout;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.modules.websvc.wsstack.spi.WSStackFactory;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A sub-class of the J2eePlatformFactory that is set up to return the 
 * plugin-specific J2eePlatform.
 * 
 * @author Petr Hejl
 */
public class WLJ2eePlatformFactory extends J2eePlatformFactory {

    static final String OPENJPA_JPA_PROVIDER = "org.apache.openjpa.persistence.PersistenceProviderImpl"; // NOI18N

    static final String ECLIPSELINK_JPA_PROVIDER = "org.eclipse.persistence.jpa.PersistenceProvider"; // NOI18N
    
    private static final Logger LOGGER = Logger.getLogger(WLJ2eePlatformFactory.class.getName());

    // always prefer JPA 1.0 see #189205
    private static final Pattern JAVAX_PERSISTENCE_PATTERN = Pattern.compile(
            "^.*javax\\.persistence.*_1-\\d+-\\d+\\.jar$");

    private static final Pattern JAVAX_PERSISTENCE_2_PATTERN = Pattern.compile(
            "^.*javax\\.persistence.*(_2-\\d+(-\\d+)?)\\.jar$");
    
    private static final Pattern JAVAX_PERSISTENCE_21_PATTERN = Pattern.compile(
            "^.*javax\\.persistence_2\\.1\\.jar$");

    private static final Pattern JERSEY_PATTERN = Pattern.compile(
            "^.*com\\.sun\\.jersey.*\\.jar$");

    private static final Pattern JERSEY_PLAIN_PATTERN = Pattern.compile(
            "^jersey-.*[\\d]+\\.[\\d]+(\\.[\\d]+)?\\.jar$");

    private static final Pattern GLASSFISH_JAXWS_PATTERN = Pattern.compile(
            "^.*glassfish\\.jaxws\\.rt.*\\.jar$");

    private static final Pattern GLASSFISH_JSF2_PATTERN = Pattern.compile(
            "^.*glassfish\\.jsf(_[\\d]+(\\.[\\d]+)*)?_2[\\.-].*\\.jar$");

    private static final Pattern OEPE_CONTRIBUTIONS_PATTERN = Pattern.compile("^.*oepe-contributions\\.jar.*$"); // NOI18N

    private static final FilenameFilter PATCH_DIR_FILTER = new PrefixesFilter("patch_wls"); // NOI18N    

    private static final Version JDK6_SUPPORTED_SERVER_VERSION = Version.fromJsr277NotationWithFallback("10.3"); // NOI18N

    private static final Version JDK7_SUPPORTED_SERVER_VERSION = Version.fromJsr277NotationWithFallback("12.1.1"); // NOI18N

    private static final Version JDK8_SUPPORTED_SERVER_VERSION = Version.fromJsr277NotationWithFallback("12.1.3"); // NOI18N

    private static final Version JPA2_SUPPORTED_SERVER_VERSION = Version.fromJsr277NotationWithFallback("12.1.1"); // NOI18N

    private static final Version JPA21_SUPPORTED_SERVER_VERSION = Version.fromJsr277NotationWithFallback("12.1.3"); // NOI18N

    // since 12.2.1 there is no separate JPA jar
    private static final Version JPA21_BUNDLED_SERVER_VERSION = Version.fromJsr277NotationWithFallback("12.2.1"); // NOI18N

    private static final Version JAX_RS_SUPPORTED_SERVER_VERSION = Version.fromJsr277NotationWithFallback("12.1.1"); // NOI18N

    // since 12.2.1 there is no separate JPA jar
    private static final Version JAX_RS_BUNDLED_SERVER_VERSION = Version.fromJsr277NotationWithFallback("12.2.1"); // NOI18N

    @Override
    public J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager dm) {
        assert WLDeploymentManager.class.isAssignableFrom(dm.getClass()) : this + " cannot create platform for unknown deployment manager:" + dm;
        return ((WLDeploymentManager) dm).getJ2eePlatformImpl();
    }
    
    private static void addFileToList(List<URL> list, File f) {
        URL u = FileUtil.urlForArchiveOrDir(f);
        if (u != null) {
            list.add(u);
        }
    }

    public static List<URL> getWLSClassPath(@NonNull File platformRoot,
            @NullAllowed File mwHome, @NullAllowed J2eePlatformImplImpl j2eePlatform) {

        List<URL> list = new ArrayList<URL>();
        try {
            // the WLS jar is intentional
            File weblogicFile = WebLogicLayout.getWeblogicJar(platformRoot);
            if (weblogicFile.exists()) {
                addFileToList(list, weblogicFile);
            }
            File apiFile = new File(platformRoot, "server/lib/api.jar"); // NOI18N
            if (apiFile.exists()) {
                addFileToList(list, apiFile);
                list.addAll(getJarClassPath(apiFile, mwHome));
            }

            // patches
            // FIXME multiple versions under same middleware
            if (mwHome != null) {
                File[] patchDirCandidates = mwHome.listFiles(PATCH_DIR_FILTER);
                if (patchDirCandidates != null) {
                    for (File candidate : patchDirCandidates) {
                        File jarFile = FileUtil.normalizeFile(new File(candidate,
                                "profiles/default/sys_manifest_classpath/weblogic_patch.jar")); // NOI18N
                        if (jarFile.exists()) {
                            addFileToList(list, jarFile);
                            List<URL> deps = getJarClassPath(jarFile, mwHome);
                            list.addAll(deps);
                            for (URL dep : deps) {
                                List<URL> innerDeps = getJarClassPath(dep, mwHome);
                                list.addAll(innerDeps);
                                for (URL innerDep : innerDeps) {
                                    if (innerDep.getPath().contains("patch_jars")) { // NOI18N
                                        list.addAll(getJarClassPath(innerDep, mwHome));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // oepe contributions
            if (weblogicFile.exists()) {
                List<URL> cp = getJarClassPath(weblogicFile, mwHome);
                URL oepe = null;
                for (URL cpElem : cp) {
                    if (OEPE_CONTRIBUTIONS_PATTERN.matcher(cpElem.getPath()).matches()) {
                        oepe = cpElem;
                        //list.add(oepe);
                        break;
                    }
                }
                if (oepe != null) {
                    list.addAll(getJarClassPath(oepe, mwHome));
                }
            }

            addPersistenceLibrary(list, platformRoot, mwHome, j2eePlatform);
            addMissingLibraries(list, platformRoot, mwHome, j2eePlatform);

            // file needed for jsp parsing WL9 and WL10
            addFileToList(list, new File(platformRoot, "server/lib/wls-api.jar")); // NOI18N
        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, null, e);
        }
        return list;
    }

    // package for tests only
    static List<URL> getJarClassPath(URL url, File mwHome) {
        URL fileUrl = FileUtil.getArchiveFile(url);
        if (fileUrl != null) {
            FileObject fo = URLMapper.findFileObject(fileUrl);
            if (fo != null) {
                File file = FileUtil.toFile(fo);
                if (file != null) {
                    return getJarClassPath(file, mwHome);
                }
            }
        }
        return Collections.emptyList();
    }

    // package for tests only
    static List<URL> getJarClassPath(File jarFile, File mwHome) {
        List<URL> urls = new ArrayList<URL>();

        try {
            JarFile file = new JarFile(jarFile);
            try {
                Manifest manifest = file.getManifest();
                if (manifest != null) {
                    Attributes attrs = manifest.getMainAttributes();
                    String value = attrs.getValue("Class-Path"); //NOI18N
                    if (value != null) {
                        String[] values = value.split("\\s+"); // NOI18N
                        File parent = FileUtil.normalizeFile(jarFile).getParentFile();
                        if (parent != null) {
                            for (String cpElement : values) {
                                if (!"".equals(cpElement.trim())) { // NOI18N
                                    File f = new File(cpElement);
                                    if (!f.isAbsolute()) {
                                        f = new File(parent, cpElement);
                                    }
                                    f = FileUtil.normalizeFile(f);
                                    if (!f.exists()) {
                                        // fix the possibly wrong path in the jar #206528
                                        if (mwHome != null && cpElement.startsWith("../../../modules")) { // NOI18N
                                            f = FileUtil.normalizeFile(
                                                    new File(mwHome, cpElement.substring(9)));
                                            if (f.exists()) {
                                                addFileToList(urls, f);
                                            }
                                        }
                                    // for some reason there is deployable war in api.jar in 12.2.1
                                    } else if (!(f.getPath().contains("deployable-libraries") // NOI18N
                                            && f.getName().endsWith(".war"))) { // NOI18N
                                        addFileToList(urls, f);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, "Could not read WebLogic JAR", ex);
            } finally {
                file.close();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Could not open WebLogic JAR", ex);
        }

        return urls;
    }

    //XXX there seems to be a bug in api.jar - it does not contain link to javax.persistence
    // method checks whether there is already persistence API present in the list
    private static void addPersistenceLibrary(List<URL> list, @NonNull File serverRoot,
            @NullAllowed File middleware, @NullAllowed J2eePlatformImplImpl j2eePlatform) throws MalformedURLException {

        Version serverVersion;
        if (j2eePlatform != null) {
            serverVersion = j2eePlatform.dm.getServerVersion();
        } else {
            serverVersion = WLPluginProperties.getServerVersion(serverRoot);
        }
        if (serverVersion != null && JPA21_BUNDLED_SERVER_VERSION.isBelowOrEqual(serverVersion)) {
            if (j2eePlatform != null) {
                synchronized (j2eePlatform) {
                    j2eePlatform.jpa2Available = true;
                    j2eePlatform.jpa21Available = true;
                }
            }
            return;
        }

        boolean foundJpa21 = false;
        boolean foundJpa2 = false;
        boolean foundJpa1 = false;

        for (Iterator<URL> it = list.iterator(); it.hasNext(); ) {
            URL archiveUrl = FileUtil.getArchiveFile(it.next());
            if (archiveUrl != null) {
                if (JAVAX_PERSISTENCE_21_PATTERN.matcher(archiveUrl.getPath()).matches()) {
                    foundJpa21 = true;
                    break;
                } else if (JAVAX_PERSISTENCE_2_PATTERN.matcher(archiveUrl.getPath()).matches()) {
                    foundJpa2 = true;
                    break;
                } else if (JAVAX_PERSISTENCE_PATTERN.matcher(archiveUrl.getPath()).matches()) {
                    foundJpa1 = true;
                    // we still may found jpa2
                }
            }
        }

        if (j2eePlatform != null) {
            synchronized (j2eePlatform) {
                j2eePlatform.jpa2Available = foundJpa2 || foundJpa21;
                j2eePlatform.jpa21Available = foundJpa21;
            }
        }

        if (foundJpa21 || foundJpa1) {
            return;
        }

        if (middleware != null) {
            File modules = getMiddlewareModules(middleware);
            if (modules.exists() && modules.isDirectory()) {
                List<FilenameFilter> filters = new ArrayList<FilenameFilter>(2);
                
                // we have to remove jpa2 jar otherwise we would have both jpa2 and jpa21 on classpath
                if (serverVersion != null && JPA21_SUPPORTED_SERVER_VERSION.isBelowOrEqual(serverVersion) && foundJpa2) {    
                    for (Iterator<URL> it = list.iterator(); it.hasNext();) {
                        URL archiveUrl = FileUtil.getArchiveFile(it.next());
                        if (archiveUrl != null && JAVAX_PERSISTENCE_2_PATTERN.matcher(archiveUrl.getPath()).matches()) {
                            it.remove();
                        }
                    }
                }
                
                if (serverVersion != null && JPA2_SUPPORTED_SERVER_VERSION.isBelowOrEqual(serverVersion)) {
                    filters.add(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return JAVAX_PERSISTENCE_21_PATTERN.matcher(name).matches();
                        }
                    });
                    filters.add(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return JAVAX_PERSISTENCE_2_PATTERN.matcher(name).matches();
                        }
                    });
                } else {
                    filters.add(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return JAVAX_PERSISTENCE_PATTERN.matcher(name).matches();
                        }
                    });
                }
                for (FilenameFilter filter : filters) {
                    File[] persistenceCandidates = modules.listFiles(filter);
                    if (persistenceCandidates.length > 0) {
                        for (File candidate : persistenceCandidates) {
                            addFileToList(list, candidate);
                            // mark we have jpa21 available
                            if (j2eePlatform != null
                                    && JAVAX_PERSISTENCE_21_PATTERN.matcher(candidate.getName()).matches()) {
                                synchronized (j2eePlatform) {
                                    j2eePlatform.jpa21Available = true;
                                }
                            }
                        }
                        if (persistenceCandidates.length > 1) {
                            LOGGER.log(Level.INFO, "Multiple javax.persistence JAR candidates");
                        }
                        break;
                    }
                }
            }
        }
    }

    //XXX there seems to be a bug in api.jar - it does not contain link to
    // javax.ws and (in 12.1.2 and higher) glassfish.jsf_2.x.x where jsf2 is
    // available
    private static void addMissingLibraries(List<URL> list, @NonNull File serverRoot,
            @NullAllowed File middleware, @NullAllowed J2eePlatformImplImpl j2eePlatform) throws MalformedURLException {

        if (middleware != null) {
            File modules = getMiddlewareModules(middleware);
            if (modules.exists() && modules.isDirectory()) {
                final Version serverVersion;
                if (j2eePlatform != null) {
                    serverVersion = j2eePlatform.dm.getServerVersion();
                } else {
                    serverVersion = WLPluginProperties.getServerVersion(serverRoot);
                }
                FilenameFilter filter = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return ((JERSEY_PATTERN.matcher(name).matches() || JERSEY_PLAIN_PATTERN.matcher(name).matches())
                                && serverVersion != null
                                && JAX_RS_SUPPORTED_SERVER_VERSION.isBelowOrEqual(serverVersion)
                                && !JAX_RS_BUNDLED_SERVER_VERSION.isBelowOrEqual(serverVersion))
                                        || GLASSFISH_JAXWS_PATTERN.matcher(name).matches();
                    }
                };
                for (File missingFile : modules.listFiles(filter)) {
                    addFileToList(list, missingFile);
                }
            }
        }

        // only add it if not already present
        for (Iterator<URL> it = list.iterator(); it.hasNext();) {
            URL archiveUrl = FileUtil.getArchiveFile(it.next());
            if (archiveUrl != null && GLASSFISH_JSF2_PATTERN.matcher(archiveUrl.getPath()).matches()) {
                return;
            }
        }

        File serverModules = getServerModules(serverRoot);
        if (serverModules != null && serverModules.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return GLASSFISH_JSF2_PATTERN.matcher(name).matches();
                }
            };
            for (File missingFile : serverModules.listFiles(filter)) {
                addFileToList(list, missingFile);
            }
        }
    }

    @NonNull
    public static File getMiddlewareModules(File middleware) {
        File modules = new File(middleware, "modules"); // NOI18N
        if (!modules.exists() || !modules.isDirectory()) {
            modules = new File(new File(middleware, "oracle_common"), "modules"); // NOI18N
        }
        return modules;
    }

    private static File getServerModules(File server) {
        File modules = new File(server, "modules"); // NOI18N
        return modules;
    }

    public static class J2eePlatformImplImpl extends J2eePlatformImpl2 {

        /**
         * The platform icon's URL
         */
        private static final String ICON = "org/netbeans/modules/j2ee/weblogic9/resources/16x16.gif"; // NOI18N

        private static final String J2EE_API_DOC    = "docs/javaee-doc-api.jar";    // NOI18N

        private final Set<Type> moduleTypes = new HashSet<Type>();

        private final Set<Profile> profiles = new HashSet<Profile>();

        private final WLDeploymentManager dm;

        private final ChangeListener domainChangeListener;

        private String platformRoot;
        
        /** <i>GuardedBy("this")</i> */
        private LibraryImplementation[] libraries = null;

        /** <i>GuardedBy("this")</i> */
        private String defaultJpaProvider;
        
        /** <i>GuardedBy("this")</i> */
        private boolean jpa2Available;
        
        /** <i>GuardedBy("this")</i> */
        private boolean jpa21Available;
        
        public J2eePlatformImplImpl(WLDeploymentManager dm) {
            this.dm = dm;

            moduleTypes.add(Type.WAR);
            moduleTypes.add(Type.EJB);
            moduleTypes.add(Type.EAR);

            // Allow J2EE 1.4 Projects
            profiles.add(Profile.J2EE_14);

            // Check for WebLogic Server 10x to allow Java EE 5 Projects
            Version version = dm.getDomainVersion();
            if (version == null) {
                version = dm.getServerVersion();
            }

            if (version != null) {
                if (version.isAboveOrEqual(WLDeploymentFactory.VERSION_10)) {
                    profiles.add(Profile.JAVA_EE_5);
                }
                if (version.isAboveOrEqual(WLDeploymentFactory.VERSION_11)) {
                    profiles.add(Profile.JAVA_EE_6_FULL);
                    profiles.add(Profile.JAVA_EE_6_WEB);
                }
                if (version.isAboveOrEqual(WLDeploymentFactory.VERSION_1221)) {
                    profiles.add(Profile.JAVA_EE_7_FULL);
                    profiles.add(Profile.JAVA_EE_7_WEB);
                }
            }

            domainChangeListener = new DomainChangeListener(this);
            dm.addDomainChangeListener(WeakListeners.change(domainChangeListener, dm));
        }

        @Override
        public boolean isToolSupported(String toolName) {
            if (J2eePlatform.TOOL_WSGEN.equals(toolName) || J2eePlatform.TOOL_WSIMPORT.equals(toolName)) {
                return true;
            }
            if (J2eePlatform.TOOL_JSR109.equals(toolName)) {
                return false; // to explicitelly emphasise that JSR 109 is not supported
            }

            if ("defaultPersistenceProviderJavaEE5".equals(toolName)) { // NOI18N
                return true;
            }
            
            // this property says whether following two props are taken into account
            if("jpaversionverification".equals(toolName)) { // NOI18N
                return true;
            }
            if("jpa1.0".equals(toolName)) { // NOI18N
                return true;
            }
            if("jpa2.0".equals(toolName)) { // NOI18N
                return isJpa2Available();
            }

            // shortcut
            if (!"openJpaPersistenceProviderIsDefault1.0".equals(toolName) // NOI18N
                    && !"eclipseLinkPersistenceProviderIsDefault2.0".equals(toolName) // NOI18N
                    && !OPENJPA_JPA_PROVIDER.equals(toolName)
                    && !ECLIPSELINK_JPA_PROVIDER.equals(toolName)) {
                return false;
            }

            // JPA provider part
            String currentDefaultJpaProvider = getDefaultJpaProvider();
            if ("openJpaPersistenceProviderIsDefault1.0".equals(toolName)) { // NOI18N
                return currentDefaultJpaProvider.equals(OPENJPA_JPA_PROVIDER);
            }
            if ("eclipseLinkPersistenceProviderIsDefault2.0".equals(toolName)) { // NOI18N
                return currentDefaultJpaProvider.equals(ECLIPSELINK_JPA_PROVIDER);
            }

            // both are supported
            if (OPENJPA_JPA_PROVIDER.equals(toolName) || ECLIPSELINK_JPA_PROVIDER.equals(toolName)) {
                return true;
            }

            return false;
        }
        
        public File[] getToolClasspathEntries(String toolName) {
            File[] cp = new File[0];
            if (J2eePlatform.TOOL_WSGEN.equals(toolName) || J2eePlatform.TOOL_WSIMPORT.equals(toolName)) {
                File weblogicJar = WLPluginProperties.getWeblogicJar(dm);
                if (weblogicJar != null) {
                    cp = new File[] { weblogicJar };
                }
            }
            return cp;
        }

        @Override
        public Set<Profile> getSupportedProfiles() {
            return profiles;
        }

        @Override
        public Set<Type> getSupportedTypes() {
            return moduleTypes;
        }

        @Override
        public Set/*<String>*/ getSupportedJavaPlatformVersions() {
            Set versions = new HashSet();
            versions.add("1.4"); // NOI18N
            versions.add("1.5"); // NOI18N
            Version serverVersion = dm.getServerVersion();
            if (serverVersion != null) {
                if (serverVersion.isAboveOrEqual(JDK6_SUPPORTED_SERVER_VERSION)) {
                    versions.add("1.6"); // NOI18N
                }
                if (serverVersion.isAboveOrEqual(JDK7_SUPPORTED_SERVER_VERSION)) {
                    versions.add("1.7"); // NOI18N
                }
                if (serverVersion.isAboveOrEqual(JDK8_SUPPORTED_SERVER_VERSION)) {
                    versions.add("1.8"); // NOI18N
                }
            }
            return versions;
        }

        @Override
        public JavaPlatform getJavaPlatform() {
            return null;
        }
        
        @Override
        public File[] getPlatformRoots() {
            File server = getServerHome();
            File domain = getDomainHome();
            File middleware = getMiddlewareHome();
            
            if (middleware != null) {
                return new File[] {server, domain, middleware};
            }
            return new File[] {server, domain};
        }

        @Override
        public File getDomainHome() {
            if (dm.isRemote()) {
                return null;
            }

            File domain = new File(dm.getInstanceProperties().getProperty(
                    WLPluginProperties.DOMAIN_ROOT_ATTR));
            
            assert domain.isAbsolute();
            return domain;
        }

        @NonNull
        @Override
        public File getServerHome() {
            File server = new File(getPlatformRoot());
            
            assert server.isAbsolute();
            return server;
        }
        
        @Override
        public File getMiddlewareHome() {
            return WLPluginProperties.getMiddlewareHome(getServerHome());
        }        

        @Override
        public synchronized LibraryImplementation[] getLibraries() {
            if (libraries != null) {
                return libraries;
            }

            initLibrariesForWLS();
            return libraries;
        }

        @Override
        public LibraryImplementation[] getLibraries(Set<ServerLibraryDependency> libraries) {
            // FIXME cache & listen for file changes
            String domainDir = dm.getInstanceProperties().getProperty(WLPluginProperties.DOMAIN_ROOT_ATTR);
            assert domainDir != null || dm.isRemote();
            String serverDir = dm.getInstanceProperties().getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
            assert serverDir != null;
            WLServerLibrarySupport support = new WLServerLibrarySupport(new File(serverDir),
                    domainDir == null ? null : new File(domainDir));

            Map<ServerLibrary, List<File>> serverLibraries =  null;
            try {
                serverLibraries = support.getClasspathEntries(libraries);
            } catch (ConfigurationException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }

            if (serverLibraries == null || serverLibraries.isEmpty()) {
                return getLibraries();
            }

            List<LibraryImplementation> serverImpl = new ArrayList<LibraryImplementation>();
            for (Map.Entry<ServerLibrary, List<File>> entry : serverLibraries.entrySet()) {
                LibraryImplementation library = new J2eeLibraryTypeProvider().
                        createLibrary();
                ServerLibrary lib = entry.getKey();
                // really localized ?
                // FIXME more accurate name needed ?
                if (lib.getSpecificationTitle() == null && lib.getName() == null) {
                    library.setName(NbBundle.getMessage(WLJ2eePlatformFactory.class,
                        "UNKNOWN_SERVER_LIBRARY_NAME"));
                } else {
                    library.setName(NbBundle.getMessage(WLJ2eePlatformFactory.class,
                        "SERVER_LIBRARY_NAME", new Object[] {
                            lib.getSpecificationTitle() == null ? lib.getName() : lib.getSpecificationTitle(),
                            lib.getSpecificationVersion() == null ? "" : lib.getSpecificationVersion()}));
                }

                List<URL> cp = new ArrayList<URL>();
                for (File file : entry.getValue()) {
                    addFileToList(cp, file);
                }

                library.setContent(J2eeLibraryTypeProvider.
                        VOLUME_TYPE_CLASSPATH, cp);
                serverImpl.add(library);
            }
            // add the standard server cp as last it is logical and prevents
            // issues like #188753
            serverImpl.addAll(Arrays.asList(getLibraries()));

            return serverImpl.toArray(new LibraryImplementation[0]);
        }
        
        public void notifyLibrariesChange() {
            synchronized (this) {
                libraries = null;
            }
            firePropertyChange(PROP_LIBRARIES, null, getLibraries());
        }
        
        private void initLibrariesForWLS() {
            LibraryImplementation library = new J2eeLibraryTypeProvider().
                    createLibrary();
            
            // set its name
            library.setName(NbBundle.getMessage(WLJ2eePlatformFactory.class, 
                    "LIBRARY_NAME"));
            
            // add the required jars to the library
            List<URL> list = new ArrayList<URL>();
            list.addAll(getWLSClassPath(getServerHome(), getMiddlewareHome(), this));

            library.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, list);
            File j2eeDoc = InstalledFileLocator.getDefault().locate(J2EE_API_DOC, null, false);
            if (j2eeDoc != null) {
                list = new ArrayList<>();
                addFileToList(list, j2eeDoc);
                library.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, list);
            }
            
            synchronized (this) {
                libraries = new LibraryImplementation[1];
                libraries[0] = library;
            }
        }
        
        public synchronized boolean isJpa2Available() {
            if (libraries != null) {
                return jpa2Available;
            }
            
            // initialize and return value
            getLibraries();
            return jpa2Available;
        }
        
        public synchronized boolean isJpa21Available() {
            if (libraries != null) {
                return jpa21Available;
            }
            
            // initialize and return value
            getLibraries();
            return jpa21Available;
        }

        WLDeploymentManager getDeploymentManager() {
            return dm;
        }

        String getDefaultJpaProvider() {
            synchronized (this) {
                if (defaultJpaProvider != null) {
                    return defaultJpaProvider;
                }
            }

            // XXX we could use JPAMBean for remote instances
            String newDefaultJpaProvider = null;
            FileObject config = WLPluginProperties.getDomainConfigFileObject(dm);
            if (config != null) {
                try {
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser parser = factory.newSAXParser();
                    JPAHandler handler = new JPAHandler();
                    InputStream is = new BufferedInputStream(config.getInputStream());
                    try {
                        parser.parse(is, handler);
                        newDefaultJpaProvider = handler.getDefaultJPAProvider();
                    } finally {
                        is.close();
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                } catch (ParserConfigurationException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                } catch (SAXException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
            if (newDefaultJpaProvider == null) {
                if (JPA2_SUPPORTED_SERVER_VERSION.isBelowOrEqual(dm.getServerVersion())) {
                    newDefaultJpaProvider = ECLIPSELINK_JPA_PROVIDER;
                } else {
                    newDefaultJpaProvider = OPENJPA_JPA_PROVIDER;
                }
            }

            synchronized (this) {
                defaultJpaProvider = newDefaultJpaProvider;
                return defaultJpaProvider;
            }
        }

        /**
         * Gets the platform icon. A platform icon is the one that appears near
         * the libraries attached to j2ee project.
         * 
         * @return the platform icon
         */
        public Image getIcon() {
            return ImageUtilities.loadImage(ICON);
        }
        
        /**
         * Gets the platform display name. This one appears exactly to the 
         * right of the platform icon ;)
         * 
         * @return the platform's display name
         */
        public String getDisplayName() {
            return NbBundle.getMessage(WLJ2eePlatformFactory.class, "PLATFORM_NAME"); // NOI18N
        }     
        
        private String getPlatformRoot() {
            if (platformRoot == null) {
                platformRoot = dm.getInstanceProperties().getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
            }
            return platformRoot;
        }

        @Override
        public Lookup getLookup() {
            List content = new ArrayList();
            File platformRoot = new File(getPlatformRoot());
            WSStack<JaxWs> wsStack = WSStackFactory.createWSStack(JaxWs.class ,
                    new WebLogicJaxWsStack(dm.getServerVersion()), WSStack.Source.SERVER);
            Collections.addAll(content, platformRoot, 
                    new JpaSupportImpl(this),new JaxWsPoliciesSupportImpl(this), 
                    new JaxRsStackSupportImpl(this, dm.getServerVersion()), wsStack);
           
            Lookup baseLookup = Lookups.fixed(content.toArray());
            return LookupProviderSupport.createCompositeLookup(baseLookup, 
                    "J2EE/DeploymentPlugins/WebLogic9/Lookup"); //NOI18N
        }
    }

    private static class DomainChangeListener implements ChangeListener {

        private final J2eePlatformImplImpl platform;

        private Set<WLServerLibrary> oldLibraries = Collections.emptySet();

        public DomainChangeListener(J2eePlatformImplImpl platform) {
            this.platform = platform;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            synchronized (platform) {
                platform.defaultJpaProvider = null;
            }

            Set<WLServerLibrary> tmpNewLibraries;
            // #249289 - deleted server may still listen until GCed
            if (platform.dm.getRealInstanceProperties() != null) {
                tmpNewLibraries = new WLServerLibrarySupport(platform.dm).getDeployedLibraries();
            } else {
                tmpNewLibraries = Collections.emptySet();
            }

            Set<WLServerLibrary> tmpOldLibraries;
            synchronized (this) {
                tmpOldLibraries = new HashSet<WLServerLibrary>(oldLibraries);
                oldLibraries = tmpNewLibraries;
            }

            if (fireChange(tmpOldLibraries, tmpNewLibraries)) {
                LOGGER.log(Level.FINE, "Firing server libraries change");
                platform.firePropertyChange(J2eePlatformImpl.PROP_SERVER_LIBRARIES, null, null);
            }
        }

        private boolean fireChange(Set<WLServerLibrary> paramOldLibraries, Set<WLServerLibrary> paramNewLibraries) {
            if (paramOldLibraries.size() != paramNewLibraries.size()) {
                return true;
            }

            Set<WLServerLibrary> newLibraries = new HashSet<WLServerLibrary>(paramNewLibraries);
            for (Iterator<WLServerLibrary> it = newLibraries.iterator(); it.hasNext();) {
                WLServerLibrary newLib = it.next();
                for (WLServerLibrary oldLib : paramOldLibraries) {
                    if (WLServerLibrarySupport.sameLibraries(newLib, oldLib)) {
                        it.remove();
                        break;
                    }
                }
            }

            return !newLibraries.isEmpty();
        }
    }
    
    private static class JPAHandler extends DefaultHandler {

        private String defaultJPAProvider;

        private final StringBuilder value = new StringBuilder();

        private boolean start;

        public JPAHandler() {
            super();
        }

        @Override
        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) throws SAXException {
            value.setLength(0);
            if ("default-jpa-provider".equals(qName)) { // NOI18N
                start = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (!start) {
                return;
            }

            if ("default-jpa-provider".equals(qName)) { // NOI18N
                defaultJPAProvider = value.toString();
                start = false;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            value.append(ch, start, length);
        }

        public String getDefaultJPAProvider() {
            return defaultJPAProvider;
        }
    }

    private static class PrefixesFilter implements FilenameFilter {

        private final String[] prefixes;

        public PrefixesFilter(String... prefixes) {
            this.prefixes = prefixes;
        }

        @Override
        public boolean accept(File dir, String name) {
            for (String prefix : prefixes) {
                if (name.startsWith(prefix)) {
                    return true;
                }
            }
            return false;
        }
    }

}
