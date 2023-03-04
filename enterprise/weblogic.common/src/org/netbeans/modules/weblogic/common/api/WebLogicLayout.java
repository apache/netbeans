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
package org.netbeans.modules.weblogic.common.api;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.filesystems.FileUtil;

public final class WebLogicLayout {

    private static final WeakHashMap<WebLogicConfiguration, ClassLoader> CLASSLOADERS = new WeakHashMap<WebLogicConfiguration, ClassLoader>();

    private static final String WEBLOGIC_JAR = "server/lib/weblogic.jar"; // NOI18N

    private static final Collection<String> EXPECTED_FILES = new ArrayList<>();

    static {
        EXPECTED_FILES.add("common"); // NOI18N
        EXPECTED_FILES.add("server/bin"); // NOI18N
        EXPECTED_FILES.add(WEBLOGIC_JAR);
    }

    private static final Logger LOGGER = Logger.getLogger(WebLogicLayout.class.getName());

    private final WebLogicConfiguration config;

    WebLogicLayout(WebLogicConfiguration config) {
        this.config = config;
    }

    @CheckForNull
    public File getDomainConfigFile() {
        if (config.isRemote()) {
            return null;
        }
        return getDomainConfigFile(config.getDomainHome());
    }

    @NonNull
    public static File getDomainConfigFile(File domain) {
        return FileUtil.normalizeFile(new File(domain,
                "config" + File.separator + "config.xml")); // NOI18N
    }

    @CheckForNull
    public File getServerLibDirectory() {
        File serverLib = new File(config.getServerHome(), "server" + File.separator + "lib"); // NOI18N
        if (serverLib.isDirectory()) {
            return serverLib;
        }
        return null;
    }

    @CheckForNull
    public File getDomainLibDirectory() {
        if (config.isRemote()) {
            return null;
        }
        File domainLib = new File(config.getDomainHome(), "lib"); // NOI18N
        if (domainLib.exists() && domainLib.isDirectory()) {
            return domainLib;
        }
        return null;
    }

    @NonNull
    public File getWeblogicJar() {
        return getWeblogicJar(config.getServerHome());
    }

    @NonNull
    public static File getWeblogicJar(@NonNull File serverHome) {
        File weblogicJar = FileUtil.normalizeFile(new File(serverHome, WEBLOGIC_JAR));
        return weblogicJar;
    }

    @CheckForNull
    public static DomainConfiguration getDomainConfiguration(String domainPath) {
        DomainConfiguration ret = DomainConfiguration.getInstance(new File(domainPath), false);
        if (ret == null) {
            LOGGER.log(Level.FINE, "Domain config file "
                    + "is not found. Probably server configuration was "
                    + "changed externally"); // NOI18N
        }
        return ret;
    }

    public static boolean isSupportedLayout(File candidate){
        return null != candidate
                && candidate.canRead()
                && candidate.isDirectory()
                && hasRequiredChildren(candidate, EXPECTED_FILES);
    }

    /**
     * Checks whether the server root contains weblogic.jar of version 9, 10 or 11.
     */
    public static boolean isSupportedVersion(Version version) {
        return version != null && (Integer.valueOf(9).equals(version.getMajor())
                    || Integer.valueOf(10).equals(version.getMajor())
                    || Integer.valueOf(11).equals(version.getMajor())
                    || Integer.valueOf(12).equals(version.getMajor()));
    }

    @NonNull
    public File[] getClassPath() {
        File weblogicJar = getWeblogicJar();
        if (!weblogicJar.exists()) {
            LOGGER.log(Level.INFO, "File {0} does not exist for {1}",
                    new Object[] {weblogicJar.getAbsolutePath(), config.getServerHome()});
            return new File[] {weblogicJar};
        }

        // we will add weblogic.server.modules jar manually as the path is hardcoded
        // and may not be valid see #189537 and #206259
        String serverModulesJar = null;
        try {
            // JarInputStream cannot be used due to problem in weblogic.jar in Oracle Weblogic Server 10.3
            JarFile jar = new JarFile(weblogicJar);
            try {
                Manifest manifest = jar.getManifest();
                if (manifest != null) {
                    String classpath = manifest.getMainAttributes()
                            .getValue("Class-Path"); // NOI18N
                    String[] elements = classpath.split("\\s+"); // NOI18N
                    for (String element : elements) {
                        if (element.contains("weblogic.server.modules")) { // NOI18N
                            File ref = new File(weblogicJar.getParentFile(), element);
                            if (!ref.exists()) {
                                LOGGER.log(Level.INFO, "Broken {0} classpath file {1} for {2}",
                                        new Object[] {weblogicJar.getAbsolutePath(), ref.getAbsolutePath(), config.getServerHome()});
                            }
                            serverModulesJar = element;
                            // last element of ../../../modules/something
                            int index = serverModulesJar.lastIndexOf("./"); // NOI18N
                            if (index >= 0) {
                                serverModulesJar = serverModulesJar.substring(index + 1);
                            }
                        }
                    }
                }
            } finally {
                try {
                    jar.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.FINEST, null, ex);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.FINE, null, e);
        }

        if (serverModulesJar != null) {
            // XXX cache values
            File mwHome = getMiddlewareHome(config.getServerHome());
            if (mwHome != null) {
                File serverModuleFile = FileUtil.normalizeFile(
                        new File(mwHome, serverModulesJar.replace("/", File.separator))); // NOI18N
                return new File[] {weblogicJar, serverModuleFile};
            }
        }

        return new File[] {weblogicJar};
    }

    @NonNull
    public ClassLoader getClassLoader() {
        synchronized (CLASSLOADERS) {
            ClassLoader classLoader = CLASSLOADERS.get(config);
            if (classLoader != null) {
                return classLoader;
            }

            // two instances may have the same classpath because of the same
            // server root directory
            for (Map.Entry<WebLogicConfiguration, ClassLoader> entry : CLASSLOADERS.entrySet()) {
                // FIXME base the check on classpath - it would be more safe
                // more expensive as well
                String serverRootCached = entry.getKey().getServerHome().getAbsolutePath();
                String serverRootFresh = config.getServerHome().getAbsolutePath();

                if ((serverRootCached == null) ? (serverRootFresh == null) : serverRootCached.equals(serverRootFresh)) {
                    classLoader = entry.getValue();
                    break;
                }
            }

            if (classLoader == null) {
                classLoader = createClassLoader();
            }

            CLASSLOADERS.put(config, classLoader);
            return classLoader;
        }
    }

    @CheckForNull
    public Version getServerVersion() {
        return getServerVersion(config.getServerHome());
    }

    @CheckForNull
    public static Version getServerVersion(@NonNull File serverHome) {
        File weblogicJar = getWeblogicJar(serverHome);
        if (!weblogicJar.exists()) {
            return null;
        }
        try {
            // JarInputStream cannot be used due to problem in weblogic.jar in Oracle Weblogic Server 10.3
            JarFile jar = new JarFile(weblogicJar);
            try {
                Manifest manifest = jar.getManifest();
                String implementationVersion = null;
                if (manifest != null) {
                    implementationVersion = manifest.getMainAttributes()
                            .getValue("Implementation-Version"); // NOI18N
                }
                if (implementationVersion != null) { // NOI18N
                    implementationVersion = implementationVersion.trim();
                    return Version.fromJsr277OrDottedNotationWithFallback(implementationVersion);
                }
            } finally {
                try {
                    jar.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.FINEST, null, ex);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.FINE, null, e);
        }
        return null;
    }

    @CheckForNull
    public File getMiddlewareHome() {
        return getMiddlewareHome(config.getServerHome());
    }

    @CheckForNull
    public static File getMiddlewareHome(@NonNull File serverHome) {
        Properties ret = new Properties();
        File productProps = new File(serverHome, ".product.properties"); // NOI18N

        if (!productProps.exists() || !productProps.canRead()) {
            return getMiddlewareHome(serverHome, null);
        }
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(productProps));
            try {
                ret.load(is);
            } finally {
                is.close();
            }
            return getMiddlewareHome(serverHome, ret.getProperty("MW_HOME"));
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return getMiddlewareHome(serverHome, null);
        }
    }

    @CheckForNull
    private static File getMiddlewareHome(@NonNull File serverHome, @NullAllowed String mwHome) {
        File middleware = null;
        if (mwHome != null) {
            middleware = new File(mwHome);
        }
        if (middleware == null || !middleware.exists() || !middleware.isDirectory()) {
            middleware = serverHome.getParentFile();
        }

        if (middleware != null && middleware.exists() && middleware.isDirectory()) {
            return middleware;
        }
        return null;
    }

    private static boolean hasRequiredChildren(File candidate, Collection<String> requiredChildren) {
        if (null == candidate) {
            return false;
        }
        String[] children = candidate.list();
        if (null == children) {
            return false;
        }
        if (null == requiredChildren) {
            return true;
        }

        for (Iterator<String> it = requiredChildren.iterator(); it.hasNext();) {
            String next = it.next();
            File test = new File(candidate.getPath() + File.separator + next);
            if (!test.exists()) {
                return false;
            }
        }
        return true;
    }

    private WebLogicClassLoader createClassLoader() {
        LOGGER.log(Level.FINE, "Creating classloader for {0}", config.getId());
        try {
            File[] classpath = getClassPath();
            URL[] urls = new URL[classpath.length];
            for (int i = 0; i < classpath.length; i++) {
                urls[i] = classpath[i].toURI().toURL();
            }
            WebLogicClassLoader classLoader = new WebLogicClassLoader(urls, WebLogicConfiguration.class.getClassLoader());
            LOGGER.log(Level.FINE, "Classloader for {0} created successfully", config.getId());
            return classLoader;
        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, null, e);
        }
        return new WebLogicClassLoader(new URL[] {}, WebLogicConfiguration.class.getClassLoader());
    }

    private static class WebLogicClassLoader extends URLClassLoader {

        public WebLogicClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            Class<?> clazz = super.findClass(name);
            if (LOGGER.isLoggable(Level.FINEST)) {
                String filename = name.replace('.', '/'); // NOI18N
                int index = filename.indexOf('$'); // NOI18N
                if (index > 0) {
                    filename = filename.substring(0, index);
                }
                filename = filename + ".class"; // NOI18N

                URL url = this.getResource(filename);
                LOGGER.log(Level.FINEST, "WebLogic classloader asked for {0}", name);
                if (url != null) {
                    LOGGER.log(Level.FINEST, "WebLogic classloader found {0} at {1}",new Object[]{name, url});
                }
            }
            return clazz;
        }

        @Override
        protected PermissionCollection getPermissions(CodeSource codeSource) {
            Permissions p = new Permissions();
            p.add(new AllPermission());
            return p;
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            // get rid of annoying warnings
            if (name.contains("jndi.properties") || name.contains("i18n_user.properties")) { // NOI18N
                return Collections.enumeration(Collections.<URL>emptyList());
            }

            return super.getResources(name);
        }
    }
    private static class ServerDescriptor {

        private final String host;

        private final String port;

        private final String name;

        public ServerDescriptor(String host, String port, String name) {
            this.host = host;
            this.port = port;
            this.name = name;
        }

        public String getHost() {
            return host;
        }

        public String getName() {
            return name;
        }

        public String getPort() {
            return port;
        }
    }
}
