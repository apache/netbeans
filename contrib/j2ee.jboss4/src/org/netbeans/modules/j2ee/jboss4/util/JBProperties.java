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
package org.netbeans.modules.j2ee.jboss4.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.customizer.CustomizerSupport;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils.Version;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbCollections;

/**
 * Helper class that makes it easier to access and set JBoss instance properties.
 *
 * @author sherold
 */
public class JBProperties {

    /** Java platform property which is used as a java platform ID */
    public static final String PLAT_PROP_ANT_NAME = "platform.ant.name"; //NOI18N

    // properties
    public  static final String PROP_PROXY_ENABLED = "proxy_enabled";   // NOI18N
    private static final String PROP_JAVA_PLATFORM = "java_platform";   // NOI18N
    private static final String PROP_SOURCES       = "sources";         // NOI18N
    private static final String PROP_JAVADOCS      = "javadocs";        // NOI18N

    private static final FilenameFilter CP_FILENAME_FILTER = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".jar") || new File(dir, name).isDirectory(); // NOI18N
        }
    };

    // default values
    private static final String DEF_VALUE_JAVA_OPTS = ""; // NOI18N
    private static final boolean DEF_VALUE_PROXY_ENABLED = true;

    private final InstanceProperties ip;
    private final JBDeploymentManager manager;

    // credentials initialized as null - must be configured by user
    private String username = null; // NOI18N
    private String password = null; // NOI18N

    /** timestamp of the jmx-console-users.properties file when it was parsed for the last time */
    private long updateCredentialsTimestamp;

    private static final Logger LOGGER = Logger.getLogger(JBProperties.class.getName());

    private final Version version;

    /** Creates a new instance of JBProperties */
    public JBProperties(JBDeploymentManager manager) {
        this.manager = manager;
        ip = manager.getInstanceProperties();
        version = JBPluginUtils.getServerVersion(new File(ip.getProperty(JBPluginProperties.PROPERTY_ROOT_DIR)));
    }

    public boolean supportsJavaEE6() {
        // FIXME detect properly
        return version != null
                && version.compareToIgnoreUpdate(JBPluginUtils.JBOSS_6_0_0) >= 0; // NOI18N
    }

    public boolean supportsJavaEE6Web() {
        // FIXME
        return supportsJavaEE6();
    }
    
    public boolean supportsJavaEE5ejb3() {
        return new File(getServerDir(), "deploy/ejb3.deployer").exists() // JBoss 4 // NOI18N
                || new File(getServerDir(), "deployers/ejb3.deployer").exists(); // JBoss 5 // NOI18N
    }

    public boolean supportsJavaEE5web() {
        return new File(getServerDir(), "deploy/jboss-web.deployer").exists() // JBoss 4.2 // NOI18N
                || new File(getServerDir(), "deployers/jbossweb.deployer").exists(); // JBoss 5 // NOI18N
    }

    public boolean supportsJavaEE5ear() {
        return supportsJavaEE5ejb3() && supportsJavaEE5web()
                && version != null && version.compareToIgnoreUpdate(JBPluginUtils.JBOSS_5_0_0) >= 0; // NOI18N
    }

    public Version getServerVersion() {
        return version;
    }

    public boolean isVersion(Version targetVersion) {
        return (version != null && version.compareToIgnoreUpdate(targetVersion) >= 0); // NOI18N
    }

    public File getServerDir() {
        return new File(ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR));
    }

    public File getRootDir() {
        return new File(ip.getProperty(JBPluginProperties.PROPERTY_ROOT_DIR));
    }

    public File getDeployDir() {
        return new File(ip.getProperty(JBPluginProperties.PROPERTY_DEPLOY_DIR));
    }

    public File getLibsDir() {
        return new File(getServerDir(), "lib"); // NOI18N
    }

    public boolean getProxyEnabled() {
        String val = ip.getProperty(PROP_PROXY_ENABLED);
        return val != null ? Boolean.valueOf(val).booleanValue()
                           : DEF_VALUE_PROXY_ENABLED;
    }

    public void setProxyEnabled(boolean enabled) {
        ip.setProperty(PROP_PROXY_ENABLED, Boolean.toString(enabled));
    }

    public JavaPlatform getJavaPlatform() {
        String currentJvm = ip.getProperty(PROP_JAVA_PLATFORM);
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        JavaPlatform[] installedPlatforms = jpm.getPlatforms(null, new Specification("J2SE", null)); // NOI18N
        for (int i = 0; i < installedPlatforms.length; i++) {
            String platformName = (String)installedPlatforms[i].getProperties().get(PLAT_PROP_ANT_NAME);
            if (platformName != null && platformName.equals(currentJvm)) {
                return installedPlatforms[i];
            }
        }
        // return default platform if none was set
        return jpm.getDefaultPlatform();
    }

    public void setJavaPlatform(JavaPlatform javaPlatform) {
        ip.setProperty(PROP_JAVA_PLATFORM, (String)javaPlatform.getProperties().get(PLAT_PROP_ANT_NAME));
    }

    public String getJavaOpts() {
        String val = ip.getProperty(JBPluginProperties.PROPERTY_JAVA_OPTS);
        return val != null ? val : DEF_VALUE_JAVA_OPTS;
    }

    public void setJavaOpts(String javaOpts) {
        ip.setProperty(JBPluginProperties.PROPERTY_JAVA_OPTS, javaOpts);
    }

    private static void addFileToList(List<URL> list, File f) {
        URL u = FileUtil.urlForArchiveOrDir(f);
        if (u != null) {
            list.add(u);
        }
    }

    public List<URL> getClasses() {
        List<URL> list = new ArrayList<URL>();
        File rootDir = getRootDir();
        File serverDir = getServerDir();
        File commonLibDir =  new File(rootDir, "common" + File.separator + "lib");

        File javaEE = new File(commonLibDir, "jboss-javaee.jar");
        if (!javaEE.exists()) {
            javaEE = new File(rootDir, "client/jboss-j2ee.jar"); // NOI18N
            if (!javaEE.exists()) {
                // jboss 5
                javaEE = new File(rootDir, "client/jboss-javaee.jar"); // NOI18N
            }
        } else {
            assert version != null && version.compareToIgnoreUpdate(JBPluginUtils.JBOSS_5_0_0) >= 0;
        }

        if (javaEE.exists()) {
            addFileToList(list, javaEE);
        }

        File jaxWsAPILib = new File(rootDir, "client/jboss-jaxws.jar"); // NOI18N
        if (jaxWsAPILib.exists()) {
           addFileToList(list, jaxWsAPILib);
        }

        File wsClientLib = new File(rootDir, "client/jbossws-client.jar"); // NOI18N
        if (wsClientLib.exists()) {
            addFileToList(list, wsClientLib);
        }

        addFiles(new File(rootDir, "lib"), list); // NOI18N
        addFiles(new File(serverDir, "lib"), list); // NOI18N

        if (version != null
                && version.compareToIgnoreUpdate(JBPluginUtils.JBOSS_7_0_0) >= 0) {
            addFiles(new File(new File(rootDir, JBPluginUtils.getModulesBase(rootDir.getAbsolutePath())), // NOI18N
                    "javax"), list); // NOI18N
            addFiles(new File(new File(rootDir, JBPluginUtils.getModulesBase(rootDir.getAbsolutePath())), // NOI18N
                    "org" + File.separator + "hibernate" + File.separator + "main"), list); // NOI18N
        }

        Set<String> commonLibs = new HashSet<String>();

        if (version != null
                && version.compareToIgnoreUpdate(JBPluginUtils.JBOSS_6_0_0) >= 0) {
            // Needed for JBoss 6
            Collections.addAll(commonLibs, "jboss-servlet-api_3.0_spec.jar", // NOI18N
                "jboss-jsp-api_2.2_spec.jar", "jboss-el-api_2.2_spec.jar", // NOI18N
                "mail.jar", "jboss-jsr77.jar", "jboss-ejb-api_3.1_spec.jar", // NOI18N
                "hibernate-jpa-2.0-api.jar", "hibernate-entitymanager.jar", // NOI18N
                "jboss-transaction-api_1.1_spec.jar", "jbossws-common.jar", // NOI18N
                "jbossws-framework.jar", "jbossws-jboss60.jar",  // NOI18N
                "jbossws-native-core.jar", "jbossws-spi.jar"); // NOI18N
        } else {
            // Add common libs for JBoss 5.x
            Collections.addAll(commonLibs, "servlet-api.jar", // NOI18N
                "jsp-api.jar", "el-api.jar", "mail.jar", "jboss-jsr77.jar", //NOI18N
                "ejb3-persistence.jar", "hibernate-entitymanager.jar","jbossws-native-jaxws.jar", // NOI18N
                "jbossws-native-jaxws-ext.jar", "jbossws-native-jaxrpc.jar", // NOI18N
                "jbossws-native-saaj.jar"); // NOI18N                
        }

        for (String commonLib : commonLibs) {
            File libJar = new File(commonLibDir, commonLib);
            if (libJar.exists()) {
                addFileToList(list, libJar);
            }
        }

        if (supportsJavaEE5ejb3()) {
            File ejb3deployer = new File(serverDir, "/deploy/ejb3.deployer/");  // NOI18N
            if (ejb3deployer.exists()) {
                addFiles(ejb3deployer, list);
            } else if ((ejb3deployer = new File(serverDir, "/deployers/ejb3.deployer/")).exists()) { // NOI18N
                addFiles(ejb3deployer, list);
            }
        }

        // JBoss 6
        File jsfAPI = new File(serverDir, "/deployers/jsf.deployer/Mojarra-2.0/jsf-libs/jsf-api-2.0.2-FCS.jar"); // NOI18N
        if (jsfAPI.exists()) {
            addFileToList(list, jsfAPI);
        // JBoss 5
        } else if ((jsfAPI = new File(serverDir, "/deploy/jbossweb.sar/jsf-libs/jsf-api.jar")).exists()) {
            addFileToList(list, jsfAPI);
        } else if ((jsfAPI = new File(serverDir, "/deploy/jboss-web.deployer/jsf-libs/jsf-api.jar")).exists()) { // NOI18N
            addFileToList(list, jsfAPI);
        } else if ((jsfAPI = new File(serverDir, "/deploy/jbossweb-tomcat55.sar/jsf-libs/myfaces-api.jar")).exists()) { // NOI18N
            addFileToList(list, jsfAPI);
        } else if ((jsfAPI = new File(serverDir, "/deployers/jbossweb.deployer/jsf-libs/jsf-api.jar")).exists()) { // NOI18N
            addFileToList(list, jsfAPI);
        }

        // JBoss 6
        File jsfIMPL = new File(serverDir, "/deployers/jsf.deployer/Mojarra-2.0/jsf-libs/jsf-impl-2.0.2-FCS.jar"); // NOI18N
        if (jsfIMPL.exists()) {
            addFileToList(list, jsfIMPL);
        // JBoss 5
        } else if ((jsfIMPL = new File(serverDir, "/deploy/jbossweb.sar/jsf-libs/jsf-impl.jar")).exists()) { // NOI18N
            addFileToList(list, jsfIMPL);
        } else if ((jsfIMPL = new File(serverDir, "/deploy/jboss-web.deployer/jsf-libs/jsf-impl.jar")).exists()) { // NOI18N
            addFileToList(list, jsfIMPL);
        } else if ((jsfIMPL = new File(serverDir, "/deploy/jbossweb-tomcat55.sar/jsf-libs/myfaces-impl.jar")).exists()) { // NOI18N
            addFileToList(list, jsfIMPL);
        } else if ((jsfIMPL = new File(serverDir, "/deployers/jbossweb.deployer/jsf-libs/jsf-impl.jar")).exists()) { // NOI18N
            addFileToList(list, jsfIMPL);
        }

        // JBoss 5 & 6
        File jstlImpl = new File(serverDir, "/deploy/jbossweb.sar/jstl.jar"); // NOI18N
        if (jstlImpl.exists()) {
            addFileToList(list, jstlImpl);
        } else if ((jstlImpl = new File(serverDir, "/deploy/jboss-web.deployer/jstl.jar")).exists()) { // NOI18N
            addFileToList(list, jstlImpl);
        } else if ((jstlImpl = new File(serverDir, "/deploy/jbossweb-tomcat55.sar/jsf-libs/jstl.jar")).exists()) { // NOI18N
            addFileToList(list, jstlImpl);
        }
        return list;
    }

    private void addFiles(File folder, List l) {
        File[] files = folder.listFiles(CP_FILENAME_FILTER);
        if (files == null) {
            return;
        }
        Arrays.sort(files);
        
        // directories first
        List<File> realFiles = new ArrayList<File>(files.length);
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                addFiles(files[i], l);
            } else {
                realFiles.add(files[i]);
            }
        }
        for (File file : realFiles) {
            addFileToList(l, file);
        }
    }

    public List<URL> getSources() {
        String path = ip.getProperty(PROP_SOURCES);
        if (path == null) {
            return new ArrayList<URL>();
        }
        return CustomizerSupport.tokenizePath(path);
    }

    public void setSources(List<URL> path) {
        ip.setProperty(PROP_SOURCES, CustomizerSupport.buildPath(path));
        manager.getJBPlatform().notifyLibrariesChanged();
    }

    public List<URL> getJavadocs() {
        String path = ip.getProperty(PROP_JAVADOCS);
        if (path == null) {
            ArrayList<URL> list = new ArrayList<URL>();
                File j2eeDoc = InstalledFileLocator.getDefault().locate("docs/javaee-doc-api.jar", null, false); // NOI18N
                if (j2eeDoc != null) {
                    addFileToList(list, j2eeDoc);
                }
            return list;
        }
        return CustomizerSupport.tokenizePath(path);
    }

    public void setJavadocs(List<URL> path) {
        ip.setProperty(PROP_JAVADOCS, CustomizerSupport.buildPath(path));
        manager.getJBPlatform().notifyLibrariesChanged();
    }

    public synchronized String getUsername() {
        updateCredentials();
        return username;
    }

    public synchronized String getPassword() {
        updateCredentials();
        return password;
    }

    // private helper methods -------------------------------------------------

    private synchronized void updateCredentials() {
        File usersPropFile = new File(getServerDir(), "/conf/props/jmx-console-users.properties");
        long lastModified = usersPropFile.lastModified();
        if (lastModified == updateCredentialsTimestamp) {
            LOGGER.log(Level.FINER, "Credentials are up-to-date.");
            return;
        }
        Properties usersProps = new Properties();
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(usersPropFile));
            try {
                usersProps.load(is);
            } finally {
                is.close();
            }
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.WARNING, usersPropFile + " not found.", e);
            return;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error while reading " + usersPropFile, e);
            return;
        }

        Enumeration<String> names = NbCollections.checkedEnumerationByFilter(usersProps.propertyNames(), String.class, false);
        if (names.hasMoreElements()) {
            username = names.nextElement();
            password = usersProps.getProperty(username);
        }

        updateCredentialsTimestamp = lastModified;
    }
}
