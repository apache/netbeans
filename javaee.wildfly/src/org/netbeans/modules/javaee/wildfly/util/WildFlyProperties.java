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
package org.netbeans.modules.javaee.wildfly.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.javaee.wildfly.WildflyDeploymentManager;
import org.netbeans.modules.javaee.wildfly.customizer.CustomizerSupport;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginProperties;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils.Version;
import static org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils.getDefaultConfigurationFile;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbCollections;

/**
 * Helper class that makes it easier to access and set JBoss instance
 * properties.
 *
 * @author sherold
 */
public class WildFlyProperties {

    /**
     * Java platform property which is used as a java platform ID
     */
    public static final String PLAT_PROP_ANT_NAME = "platform.ant.name"; //NOI18N

    // properties
    public static final String PROP_PROXY_ENABLED = "proxy_enabled";   // NOI18N
    private static final String PROP_JAVA_PLATFORM = "java_platform";   // NOI18N
    private static final String PROP_SOURCES = "sources";         // NOI18N
    private static final String PROP_JAVADOCS = "javadocs";        // NOI18N

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
    private final WildflyDeploymentManager manager;

    // credentials initialized with default values
    private String username = "admin"; // NOI18N
    private String password = "admin"; // NOI18N

    /**
     * timestamp of the jmx-console-users.properties file when it was parsed for
     * the last time
     */
    private long updateCredentialsTimestamp;

    private static final Logger LOGGER = Logger.getLogger(WildFlyProperties.class.getName());

    private final Version version;
    
    private final boolean wildfly;
    
    private final boolean servletOnly;

    /**
     * Creates a new instance of JBProperties
     */
    public WildFlyProperties(WildflyDeploymentManager manager) {
        this.manager = manager;
        ip = manager.getInstanceProperties();
        File serverPath = new File(ip.getProperty(WildflyPluginProperties.PROPERTY_ROOT_DIR));
        version = manager.getServerVersion();
        wildfly = manager.isWildfly();
        servletOnly = WildflyPluginUtils.isWildFlyServlet(serverPath);
    }

    public boolean isWildfly() {
         return wildfly;
    }

    public String getServerProfile() {
        if (this.ip.getProperty(WildflyPluginProperties.PROPERTY_CONFIG_FILE) == null) {
            return getDefaultConfigurationFile(ip.getProperty(WildflyPluginProperties.PROPERTY_ROOT_DIR));
        }
        return this.ip.getProperty(WildflyPluginProperties.PROPERTY_CONFIG_FILE);
    }

    public InstanceProperties getInstanceProperties() {
        return this.ip;
    }

    public Version getServerVersion() {
        return version;
    }

    public boolean isServletOnly() {
        return servletOnly;
    }

    public boolean isVersion(Version targetVersion) {
        return (version != null && version.compareToIgnoreUpdate(targetVersion) >= 0); // NOI18N
    }

    public File getServerDir() {
        return new File(ip.getProperty(WildflyPluginProperties.PROPERTY_SERVER_DIR));
    }

    public File getRootDir() {
        return new File(ip.getProperty(WildflyPluginProperties.PROPERTY_ROOT_DIR));
    }

    public File getDeployDir() {
        return new File(ip.getProperty(WildflyPluginProperties.PROPERTY_DEPLOY_DIR));
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
            String platformName = (String) installedPlatforms[i].getProperties().get(PLAT_PROP_ANT_NAME);
            if (platformName != null && platformName.equals(currentJvm)) {
                return installedPlatforms[i];
            }
        }
        // return default platform if none was set
        return jpm.getDefaultPlatform();
    }

    public void setJavaPlatform(JavaPlatform javaPlatform) {
        ip.setProperty(PROP_JAVA_PLATFORM, (String) javaPlatform.getProperties().get(PLAT_PROP_ANT_NAME));
    }

    public String getJavaOpts() {
        String val = ip.getProperty(WildflyPluginProperties.PROPERTY_JAVA_OPTS);
        return val != null ? val : DEF_VALUE_JAVA_OPTS;
    }

    public void setJavaOpts(String javaOpts) {
        ip.setProperty(WildflyPluginProperties.PROPERTY_JAVA_OPTS, javaOpts);
    }

    public String getModulePath(String module) {
        return getRootDir().getAbsolutePath() + ("/modules/system/layers/base/" + module).replace('/', File.separatorChar);
    }

    private static void addFileToList(List<URL> list, File f) {
        URL u = FileUtil.urlForArchiveOrDir(f);
        if (u != null) {
            list.add(u);
        }
    }

    private List<URL> selectJars(FileObject file) {
        if (file == null) {
            return Collections.EMPTY_LIST;
        }
        if (file.isData()) {
            if (file.isValid() && FileUtil.isArchiveFile(file)) {
                URL url = URLMapper.findURL(file, URLMapper.EXTERNAL);
                if (url != null) {
                    return Collections.singletonList(FileUtil.getArchiveRoot(url));
                }
            }
            return Collections.EMPTY_LIST;
        }
        List<URL> result = new ArrayList<URL>();
        for (FileObject child : file.getChildren()) {
            result.addAll(selectJars(child));
        }
        return result;
    }

    public List<URL> getClasses() {
        List<URL> list = selectJars(FileUtil.toFileObject(new File(getModulePath("javax"))));
        File glassfish = new File(getModulePath("org/glassfish/javax"));
        if(glassfish.exists()) {
            list.addAll(selectJars(FileUtil.toFileObject(glassfish)));
        }
        return list;
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
        // XXX WILDFLY IMPLEMENT
        //manager.getJBPlatform().notifyLibrariesChanged();
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
        // XXX WILDFLY IMPLEMENT
        //manager.getJBPlatform().notifyLibrariesChanged();
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
