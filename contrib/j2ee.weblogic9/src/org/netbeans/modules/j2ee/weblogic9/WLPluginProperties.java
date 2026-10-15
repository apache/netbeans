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
package org.netbeans.modules.j2ee.weblogic9;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.weblogic.common.api.WebLogicLayout;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Petr Hejl
 * @author Ivan Sidorkin
 */
public final class WLPluginProperties {

    private static final Logger LOGGER = Logger.getLogger(WLPluginProperties.class.getName());

    private static final String CONFIG_XML = "config/config.xml"; //NOI18N

    // additional properties that are stored in the InstancePropeties object
    public static final String SERVER_ROOT_ATTR = "serverRoot";        // NOI18N
    public static final String DOMAIN_ROOT_ATTR = "domainRoot";        // NOI18N
    public static final String HOST_ATTR = "host";                     // NOI18N
    public static final String PORT_ATTR = "port";                     // NOI18N
    public static final String REMOTE_ATTR = "remote";                 // NOI18N
    public static final String SECURED_ATTR = "secured";               // NOI18N
    public static final String DEBUGGER_PORT_ATTR = "debuggerPort";    // NOI18N
    public static final String DOMAIN_NAME = "domainName";          // NOI18N
    public static final String REMOTE_DEBUG_ENABLED = "remoteDebug"; // NOI18N
    public static final String DEPLOY_TARGETS = "deployTargets"; // NOI18N
    public static final String PROXY_ENABLED = "proxy_enabled"; // NOI18N

    public static final String VENDOR   = "vendor";                 // NOI18N
    public static final String JAVA_OPTS="java_opts";               // NOI18N
    public static final String MEM_OPTS = "mem_opts";               // NOI18N
    
    public static final String BEA_JAVA_HOME="bea_java_home";           // NOI18N
    public static final String SUN_JAVA_HOME="sun_java_home";           // NOI18N
    public static final String JAVA_HOME ="java_home";                  // NOI18N
    
    private static final Pattern WIN_BEA_JAVA_HOME_PATTERN = 
        Pattern.compile("\\s*(set) BEA_JAVA_HOME\\s*=(.*)");
    
    private static final Pattern WIN_SUN_JAVA_HOME_PATTERN = 
        Pattern.compile("\\s*(set) SUN_JAVA_HOME\\s*=(.*)");
    
    private static final Pattern WIN_JAVA_VENDOR_CHECK_PATTERN = 
        Pattern.compile("\\s*if\\s+\"%JAVA_VENDOR%\"\\s*==\\s*\"([^\"]+)\".*");
    
    private static final Pattern WIN_JAVA_HOME_PATTERN = 
        Pattern.compile("\\s*(set) JAVA_HOME\\s*=(.*)");
    
    private static final Pattern WIN_DEFAULT_VENDOR_PATTERN = 
        Pattern.compile("\\s*(set) JAVA_VENDOR\\s*=(.*)");
    
    private static final Pattern SHELL_JAVA_VENDOR_CHECK_PATTERN = 
        Pattern.compile("\\s*if\\s+\\[\\s+\"\\$\\{JAVA_VENDOR\\}\"\\s*=\\s*\"([^\"]+)\"\\s*\\].*");
    
    private static final Pattern SHELL_BEA_JAVA_HOME_PATTERN = 
        Pattern.compile("\\s*(export)?\\s*BEA_JAVA_HOME\\s*=(.*)");
    
    private static final Pattern SHELL_SUN_JAVA_HOME_PATTERN = 
        Pattern.compile("\\s*(export)?\\s*SUN_JAVA_HOME\\s*=(.*)");
    
    private static final Pattern SHELL_JAVA_HOME_PATTERN = 
        Pattern.compile("\\s*(export)?\\s*JAVA_HOME\\s*=(.*)");
    
    private static final Pattern SHELL_DEFAULT_VENDOR_PATTERN = 
        Pattern.compile("\\s*(export)?\\s*JAVA_VENDOR\\s*=(.*)");
    
    private static final String DOMAIN_LIST = "common/nodemanager/nodemanager.domains"; // NOI18N

    private static final String DOMAIN_REGISTRY = "domain-registry.xml"; // NOI18N

    private static final String INSTALL_ROOT_KEY = "installRoot"; // NOI18N

    private static final String FAILED_AUTHENTICATION_REPORTED_KEY = "failedAuthenticationReported"; // NOI18N

    private WLPluginProperties() {
        super();
    }

    public static String getLastServerRoot() {
        return getPreferences().get(INSTALL_ROOT_KEY, "");
    }

    public static void setLastServerRoot(String serverRoot) {
        getPreferences().put(INSTALL_ROOT_KEY, serverRoot);
    }

    public static boolean isFailedAuthenticationReported() {
        return getPreferences().getBoolean(FAILED_AUTHENTICATION_REPORTED_KEY, true);
    }

    public static void setFailedAuthenticationReported(boolean reported) {
        getPreferences().putBoolean(FAILED_AUTHENTICATION_REPORTED_KEY, reported);
    }

    @CheckForNull
    public static FileObject getDomainConfigFileObject(WLDeploymentManager manager) {
        String domainDir = manager.getInstanceProperties().getProperty(WLPluginProperties.DOMAIN_ROOT_ATTR);
        if (domainDir == null) {
            return null;
        }
        return getDomainConfigFileObject(new File(domainDir));
    }

    @CheckForNull
    public static FileObject getDomainConfigFileObject(File domainDir) {
        File domainPath = FileUtil.normalizeFile(domainDir);
        FileObject domain = FileUtil.toFileObject(domainPath);
        FileObject domainConfig = null;
        if (domain != null) {
            domainConfig = domain.getFileObject(CONFIG_XML);
        }
        return domainConfig;
    }

    @CheckForNull
    public static File getDomainConfigFile(InstanceProperties props) {
        String domainDir = props.getProperty(WLPluginProperties.DOMAIN_ROOT_ATTR);
        if (domainDir == null) {
            // may happen during the registration
            return null;
        }
        return WebLogicLayout.getDomainConfigFile(new File(domainDir));
    }

    @CheckForNull
    public static File getDomainLibDirectory(WLDeploymentManager manager) {
        String domain = (String) manager.getInstanceProperties().getProperty(WLPluginProperties.DOMAIN_ROOT_ATTR);
        if (domain != null) {
            File domainLib = new File(new File(domain), "lib"); // NOI18N
            if (domainLib.exists() && domainLib.isDirectory()) {
                return domainLib;
            }
        }
        return null;
    }

    @CheckForNull
    public static File getServerLibDirectory(WLDeploymentManager manager, boolean fallback) {
        File server = getServerRoot(manager, fallback);
        if (server != null) {
            File serverLib = new File(server, "server" + File.separator + "lib"); // NOI18N
            if (serverLib.exists() && serverLib.isDirectory()) {
                return serverLib;
            }
        }
        return null;
    }

    @CheckForNull
    public static File getServerLibDirectory(File serverFile) {
        File serverLib = new File(serverFile, "server" + File.separator + "lib"); // NOI18N
        if (serverLib.exists() && serverLib.isDirectory()) {
            return serverLib;
        }
        return null;
    }
    
    @CheckForNull
    public static File getServerRoot(WLDeploymentManager manager, boolean fallback) {
        String server = (String) manager.getInstanceProperties().getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
        // if serverRoot is null, then we are in a server instance registration process, thus this call
        // is made from InstanceProperties creation -> WLPluginProperties singleton contains
        // install location of the instance being registered
        if (fallback && server == null) {
            server = WLPluginProperties.getLastServerRoot();
        }
        if (server != null) {
            File serverFile = new File(server);
            if (serverFile.exists() && serverFile.isDirectory()) {
                return serverFile;
            }
        }
        return null;
    }
    
    @CheckForNull
    public static File getWeblogicJar(WLDeploymentManager manager) {
        String server = (String) manager.getInstanceProperties().getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
        if (server != null) {
            File serverFile = new File(server);
            return WebLogicLayout.getWeblogicJar(serverFile);
        }
        return null;
    }   
    
    /**
     * Gets the list of registered domains according to the given server
     * installation root
     *
     * @param serverRoot the server's installation location
     *
     * @return an array if strings with the domains' paths
     */
    public static String[] getRegisteredDomainPaths(String serverRoot) {
        // init the resulting vector
        List<String> result = new ArrayList<String>(getDomainsFromRegistry(serverRoot));
        if (result.isEmpty()) {
            result.addAll(getDomainsFromNodeManager(serverRoot));
        }
        return result.toArray(new String[0]);
    }
    
    @CheckForNull
    public static String getDefaultPlatformHome() {
        Collection<FileObject> instFolders = JavaPlatformManager.getDefault().
                getDefaultPlatform().getInstallFolders();
        return instFolders.isEmpty() ? null : FileUtil.toFile(
                instFolders.iterator().next()).getAbsolutePath();
    }

    /**
     * Returns map of JDK configuration which is used for starting server
     */
    public static Properties getRuntimeProperties(String domainPath) {
        Properties properties = new Properties();
        Properties javaHomeVendors = new Properties();
        String beaJavaHome = null;
        String sunJavaHome = null; 
        properties.put(JAVA_HOME, javaHomeVendors);

        // for remote instances domain is null
        if (domainPath == null) {
            javaHomeVendors.put("", getDefaultPlatformHome());
            return properties;
        }

        try {
            String setDomainEnv = domainPath + (Utilities.isWindows() ? "/bin/setDomainEnv.cmd" : "/bin/setDomainEnv.sh"); // NOI18N
            File file = new File(setDomainEnv);
            if (!file.exists()) {
                LOGGER.log(Level.INFO, "Domain environment "
                        + "setup {0} is not found. Probably server configuration was "
                        + "changed externally", setDomainEnv); // NOI18N
                javaHomeVendors.put("", getDefaultPlatformHome());
                return properties;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            try {
                String line;
                String vendorName = null;
                boolean vendorsSection = false;
                boolean defaultVendorInit = false;

                final Pattern beaPattern;
                final Pattern sunPattern;
                final Pattern vendorPattern;
                final Pattern javaHomePattern;
                final Pattern defaultVendorPattern;

                if (Utilities.isWindows()) {
                    beaPattern = WIN_BEA_JAVA_HOME_PATTERN;
                    sunPattern = WIN_SUN_JAVA_HOME_PATTERN;
                    vendorPattern = WIN_JAVA_VENDOR_CHECK_PATTERN;
                    javaHomePattern = WIN_JAVA_HOME_PATTERN;
                    defaultVendorPattern = WIN_DEFAULT_VENDOR_PATTERN;
                } else {
                    beaPattern = SHELL_BEA_JAVA_HOME_PATTERN;
                    sunPattern = SHELL_SUN_JAVA_HOME_PATTERN;
                    vendorPattern = SHELL_JAVA_VENDOR_CHECK_PATTERN;
                    javaHomePattern = SHELL_JAVA_HOME_PATTERN;
                    defaultVendorPattern = SHELL_DEFAULT_VENDOR_PATTERN;
                }

                while ((line = reader.readLine()) != null) {
                    Matcher bea = beaPattern.matcher(line);
                    Matcher sun = sunPattern.matcher(line);
                    Matcher vendor = vendorPattern.matcher(line);
                    Matcher javaHomeMatcher = javaHomePattern.matcher(line);
                    Matcher defaultVendor = defaultVendorPattern.matcher(line);

                    if (vendor.matches()) {
                        vendorsSection = true;
                        vendorName = vendor.group(1).trim();
                        continue;
                    } else if (javaHomeMatcher.matches()) {
                        if (vendorName != null) {
                            javaHomeVendors.put(vendorName, unquote(javaHomeMatcher.group(2)).trim());
                        } else if (defaultVendorInit) {
                            javaHomeVendors.put("", unquote(javaHomeMatcher.group(2)).trim());
                            defaultVendorInit = false;
                        }
                        continue;
                    } else {
                        vendorName = null;
                    }
                    if (bea.matches()) {
                        beaJavaHome = bea.group(2).trim();
                    } else if (sun.matches()) {
                        sunJavaHome = sun.group(2).trim();
                    } else if (vendorsSection && defaultVendor.matches()){
                        defaultVendorInit = true;
                        vendorsSection = false;
                    }
                }
            } finally {
                reader.close();
            }
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.INFO, null, e);
        } catch (IOException e) {
            LOGGER.log(Level.INFO, null, e);
        }
        if (beaJavaHome != null) {
            properties.put(BEA_JAVA_HOME, unquote(beaJavaHome));
        }
        if (sunJavaHome != null) {
            properties.put(SUN_JAVA_HOME, unquote(sunJavaHome));
        }
        return properties;
    }
    
    private static String unquote(String value) {
        if (Utilities.isWindows()) {
            return value;
        }

        String quote = "\""; // NOI18N
        String result = value ;
        if (result.startsWith(quote)){
            result = result.substring(1);
        }
        if (result.endsWith(quote)){
            result = result.substring(0, result.length()-1);
        }
        return result;
    }

    private static Preferences getPreferences() {
        return NbPreferences.forModule(WLPluginProperties.class);
    }

    public static File[] getClassPath(WLDeploymentManager manager) {
        File serverRoot = WLPluginProperties.getServerRoot(manager, true);
        if (serverRoot == null) {
            LOGGER.log(Level.INFO, "The server root directory does not exist for {0}", manager.getUri());
            return new File[] {};
        }

        File weblogicJar = WebLogicLayout.getWeblogicJar(serverRoot);
        if (!weblogicJar.exists()) {
            LOGGER.log(Level.INFO, "File {0} does not exist for {1}",
                    new Object[] {weblogicJar.getAbsolutePath(), manager.getUri()});
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
                                        new Object[] {weblogicJar.getAbsolutePath(), ref.getAbsolutePath(), manager.getUri()});
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
            WLProductProperties prodProps = manager.getProductProperties();
            String mwHome = prodProps.getMiddlewareHome();
            if (mwHome != null) {
                File serverModuleFile = FileUtil.normalizeFile(
                        new File(new File(mwHome), serverModulesJar.replace("/", File.separator))); // NOI18N
                return new File[] {weblogicJar, serverModuleFile};
            }
        }

        return new File[] {weblogicJar};
    }

    public static Version getServerVersion(File serverRoot) {
        File weblogicJar = WebLogicLayout.getWeblogicJar(serverRoot);
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
    public static File getMiddlewareHome(File platformRootFile) {
        String mwHome = WLProductProperties.getMiddlewareHome(platformRootFile);
        return getMiddlewareHome(platformRootFile, mwHome);
    } 
    
    @CheckForNull
    public static File getMiddlewareHome(@NonNull File platformRootFile, @NullAllowed String mwHome) {
        File middleware = null;
        if (mwHome != null) {
            middleware = new File(mwHome);
        }
        if (middleware == null || !middleware.exists() || !middleware.isDirectory()) {
            middleware = platformRootFile.getParentFile();
        }

        if (middleware != null && middleware.exists() && middleware.isDirectory()) {
            return middleware;
        }
        return null;
    }     

    private static List<String> getDomainsFromNodeManager(String serverRoot) {
        // is the server root was not defined, return an empty array of domains
        if (serverRoot == null) {
            return Collections.emptyList();
        }

        // init the input stream for the file and the w3c document object
        File file = new File(serverRoot + File.separator + DOMAIN_LIST.replace("/", File.separator));
        if (!file.exists() || !file.canRead()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>();
        BufferedReader lnr = null;

        // read the list file line by line fetching out the domain paths
        try {
            // create a new reader for the FileInputStream
            lnr = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));

            // read the lines
            String line;
            while ((line = lnr.readLine()) != null) {
                // skip the comments
                if (line.startsWith("#")) {  // NOI18N
                    continue;
                }

                // fetch the domain path
                String path = line.split("=")[1].replaceAll("\\\\\\\\", "/").replaceAll("\\\\:", ":"); // NOI18N

                // add the path to the resulting set
                result.add(path);
            }
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.INFO, null, e);   // NOI18N
        } catch (IOException e) {
            LOGGER.log(Level.INFO, null, e);   // NOI18N
        } finally {
            try {
                // close the stream
                if (lnr != null) {
                    lnr.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);  // NOI18N
            }
        }
        return result;
    }

    private static List<String> getDomainsFromRegistry(String serverRoot) {
        // is the server root was not defined, return an empty array of domains
        if (serverRoot == null) {
            return Collections.emptyList();
        }

        File mwHome = getMiddlewareHome(new File(serverRoot));
        if (mwHome == null) {
            return Collections.emptyList();
        }
        // init the input stream for the file and the w3c document object
        File file = new File(mwHome, DOMAIN_REGISTRY);
        if (!file.exists() || !file.canRead()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>();
        // init the input stream for the file and the w3c document object
        InputStream inputStream = null;
        Document document = null;

        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));

            // parse the document
            document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(inputStream);

            // get the root element
            Element root = document.getDocumentElement();

            // get the child nodes
            NodeList children = root.getChildNodes();

            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if ("domain".equals(child.getNodeName())) { // NOI18N
                    Node attr = child.getAttributes().getNamedItem("location"); // NOI18N
                    if (attr != null) {
                        String location = attr.getNodeValue();
                        if (location != null) {
                            result.add(location);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.INFO, null, e);
        } catch (IOException e) {
            LOGGER.log(Level.INFO, null, e);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.INFO, null, e);
        } catch (SAXException e) {
            LOGGER.log(Level.INFO, null, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }
        return result;
    }

    private static boolean hasRequiredChildren(File candidate, Collection requiredChildren) {
        if (null == candidate)
            return false;
        String[] children = candidate.list();
        if (null == children)
            return false;
        if (null == requiredChildren)
            return true;
        Iterator iter = requiredChildren.iterator();
        while (iter.hasNext()){
            String next = (String)iter.next();
            File test = new File(candidate.getPath()+File.separator+next);
            if (!test.exists())
                return false;
        }
        return true;
    }

    public static final class JvmVendor {

        public static final JvmVendor ORACLE = new JvmVendor("Oracle", // NOI18N
                NbBundle.getMessage(JvmVendor.class, "LBL_OracleJvmJRockit"));

        public static final JvmVendor SUN = new JvmVendor("Sun", // NOI18N
                NbBundle.getMessage(JvmVendor.class, "LBL_OracleJvmHotSpot"));
        
        public static final JvmVendor DEFAULT = new JvmVendor("", // NOI18N
                NbBundle.getMessage(JvmVendor.class, "LBL_OracleJvmDefault"));

        private final String name;
        
        private final String displayName;

        private JvmVendor(String name, String displayName) {
            this.name = name;
            this.displayName = displayName;
        }

        public String toPropertiesString() {
            return name;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public static JvmVendor fromPropertiesString(String value) {
            if (ORACLE.toPropertiesString().equals(value)) {
                return ORACLE;
            } else if (SUN.toPropertiesString().equals(value)) {
                return SUN;
            } else if (value == null || value.trim().length() == 0) {
                return DEFAULT;
            }
            return new JvmVendor(value, value);
        }
    }
}
