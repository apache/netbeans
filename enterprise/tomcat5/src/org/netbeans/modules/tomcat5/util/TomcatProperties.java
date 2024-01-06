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

package org.netbeans.modules.tomcat5.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.tomcat5.TomcatFactory;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.netbeans.modules.tomcat5.deploy.TomcatManager.TomcatVersion;
import org.netbeans.modules.tomcat5.customizer.CustomizerSupport;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.EditableProperties;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
/**
 * Utility class that makes it easier to access and set Tomcat instance properties.
 *
 * @author sherold
 */
public class TomcatProperties {
    
    private static final Logger LOGGER = Logger.getLogger(TomcatProperties.class.getName());
    
    /** Java platform property which is used as a java platform ID */
    public static final String PLAT_PROP_ANT_NAME = "platform.ant.name"; //NOI18N
    
    public static final String DEBUG_TYPE_SOCKET = "SEL_debuggingType_socket";  // NOI18N
    public static final String DEBUG_TYPE_SHARED = "SEL_debuggingType_shared";  // NOI18N
    
    public static final String BUNDLED_TOMCAT_SETTING = "J2EE/BundledTomcat/Setting"; // NOI18N
    
    // properties    
    private static final String PROP_URL           = InstanceProperties.URL_ATTR;
    private static final String PROP_USERNAME      = InstanceProperties.USERNAME_ATTR;
    private static final String PROP_PASSWORD      = InstanceProperties.PASSWORD_ATTR;
    public  static final String PROP_SERVER_PORT   = InstanceProperties.HTTP_PORT_NUMBER;
    private static final String PROP_DISPLAY_NAME  = InstanceProperties.DISPLAY_NAME_ATTR;
    public  static final String PROP_SHUTDOWN      = "admin_port";      //NOI18N
    public  static final String PROP_MONITOR       = "monitor_enabled"; // NOI18N   
    public  static final String PROP_PROXY_ENABLED = "proxy_enabled";   // NOI18N   
    private static final String PROP_CUSTOM_SCRIPT = "custom_script_enabled"; // NOI18N
    private static final String PROP_SCRIPT_PATH   = "script_path";     // NOI18N
    private static final String PROP_FORCE_STOP    = "forceStopOption"; // NOI18N    
    private static final String PROP_DEBUG_TYPE    = "debug_type";      // NOI18N
    private static final String PROP_DEBUG_PORT    = "debugger_port";   // NOI18N
    private static final String PROP_SHARED_MEM    = "shared_memory";   // NOI18N    
    private static final String PROP_JAVA_PLATFORM = "java_platform";   // NOI18N
    private static final String PROP_JAVA_OPTS     = "java_opts";       // NOI18N
    private static final String PROP_SEC_MANAGER   = "securityStartupOption"; // NOI18N    
    private static final String PROP_SOURCES       = "sources";         // NOI18N
    private static final String PROP_JAVADOCS      = "javadocs";        // NOI18N
    private static final String PROP_OPEN_LOG      = "openContextLogOnRun"; // NOI18N
    /** server.xml check timestamp */
    private static final String PROP_TIMESTAMP     = "timestamp";       // NOI18N
    private static final String PROP_HOST          = "host";            // NOI18N
    public  static final String PROP_RUNNING_CHECK_TIMEOUT = "runningCheckTimeout"; // NOI18N
    private static final String PROP_INSTANCE_ID   = "instance_id";     // NOI18N
    public  static final String PROP_AUTOREGISTERED = "autoregistered"; // NOI18N
    private static final String PROP_DRIVER_DEPLOYMENT = "driverDeploymentEnabled"; // NOI18N
    public  static final String PROP_SERVER_HEADER = "server_header";
    
    
    // default values
    private static final boolean DEF_VALUE_SEC_MANAGER   = false;
    private static final boolean DEF_VALUE_CUSTOM_SCRIPT = false;
    private static final String  DEF_VALUE_SCRIPT_PATH   = ""; // NOI18N
    private static final boolean DEF_VALUE_FORCE_STOP    = false;
    private static final String  DEF_VALUE_JAVA_OPTS     = ""; // NOI18N
    private static final String  DEF_VALUE_DEBUG_TYPE = Utilities.isWindows() ? DEBUG_TYPE_SHARED 
                                                                              : DEBUG_TYPE_SOCKET;
    private static final boolean DEF_VALUE_MONITOR              = true;
    private static final boolean DEF_VALUE_PROXY_ENABLED        = true;
    private static final int     DEF_VALUE_DEBUG_PORT           = 11550;
    private static final int     DEF_VALUE_DEBUG_PORT_BUNDLED   = 11555;
    private static final int     DEF_VALUE_SERVER_PORT          = 8080;
    public  static final int     DEF_VALUE_SHUTDOWN_PORT        = 8005;
    
    public static final int      DEF_VALUE_BUNDLED_SERVER_PORT   = 8084;
    public static final int      DEF_VALUE_BUNDLED_SHUTDOWN_PORT = 8025;
    
    private static final String  DEF_VALUE_SHARED_MEM    = "tomcat_shared_memory_id"; // NOI18N
    private static final boolean DEF_VALUE_OPEN_LOG      = true;
    private static final String  DEF_VALUE_HOST          = "localhost"; // NOI18N
    public  static final int     DEF_VALUE_RUNNING_CHECK_TIMEOUT = 2000;
    private static final String  DEF_VALUE_DISPLAY_NAME  = 
            NbBundle.getMessage(TomcatProperties.class, "LBL_DefaultDisplayName");
    private static final boolean DEF_VALUE_DRIVER_DEPLOYMENT = true;
    private static final int     DEF_VALUE_DEPLOYMENT_TIMEOUT = 120;
    private static final int     DEF_VALUE_STARTUP_TIMEOUT = 120;
    private static final int     DEF_VALUE_SHUTDOWN_TIMEOUT = 120;
    
    private TomcatManager tm;
    private InstanceProperties ip;
    private File homeDir;
    private File baseDir;
    
    /** Creates a new instance of TomcatProperties */
    public TomcatProperties(TomcatManager tm) throws IllegalArgumentException {
        this.tm = tm;
        this.ip = tm.getInstanceProperties();
        String catalinaHome = null;
        String catalinaBase = null;
        String uri = ip.getProperty(PROP_URL); // NOI18N
        final String home = "home=";    // NOI18N
        final String base = ":base=";   // NOI18N
        final String uriString = "http://";  // NOI18N
        int uriOffset = uri.indexOf (uriString);
        int homeOffset = uri.indexOf (home) + home.length ();
        int baseOffset = uri.indexOf (base, homeOffset);
        if (homeOffset >= home.length ()) {
            int homeEnd = baseOffset > 0 ? baseOffset : (uriOffset > 0 ? uriOffset - 1 : uri.length ());
            int baseEnd = uriOffset > 0 ? uriOffset - 1 : uri.length ();
            catalinaHome= uri.substring (homeOffset, homeEnd);
            if (baseOffset > 0) {
                catalinaBase = uri.substring (baseOffset + base.length (), baseEnd);
            }
            // Bundled Tomcat home and base dirs can be specified as attributes
            // specified in BUNDLED_TOMCAT_SETTING file. Tomcat manager URL can 
            // then look like "tomcat:home=$bundled_home:base=$bundled_base" and
            // therefore remains valid even if Tomcat version changes. (issue# 40659)
            if (catalinaHome.length() > 0 && catalinaHome.charAt(0) == '$') {
                FileObject fo = FileUtil.getConfigFile(BUNDLED_TOMCAT_SETTING);
                if (fo != null) {
                    catalinaHome = fo.getAttribute(catalinaHome.substring(1)).toString();
                    if (catalinaBase != null && catalinaBase.length() > 0 
                        && catalinaBase.charAt(0) == '$') {
                        catalinaBase = fo.getAttribute(catalinaBase.substring(1)).toString();
                    }
                }
            }
        }
        if (catalinaHome == null) {
            throw new IllegalArgumentException("CATALINA_HOME must not be null."); // NOI18N
        }
        homeDir = new File(catalinaHome);
        if (!homeDir.isAbsolute ()) {
            InstalledFileLocator ifl = InstalledFileLocator.getDefault();
            homeDir = ifl.locate(catalinaHome, null, false);
        }
        if (!homeDir.exists()) {
            throw new IllegalArgumentException("CATALINA_HOME directory does not exist."); // NOI18N
        }
        if (catalinaBase != null) {
            baseDir = new File(catalinaBase);
            if (!baseDir.isAbsolute ()) {
                InstalledFileLocator ifl = InstalledFileLocator.getDefault();
                baseDir = ifl.locate(catalinaBase, null, false);
                if (baseDir == null) {
                    baseDir = new File(System.getProperty("netbeans.user"), catalinaBase);   // NOI18N
                }
            }
        }        
        
//        //parse the old format for backward compatibility
//        if (uriOffset > 0) {
//            String theUri = uri.substring (uriOffset + uriString.length ());
//            int portIndex = theUri.indexOf (':');
//            String host = theUri.substring (0, portIndex - 1);
//            setHost (host);
//            //System.out.println("host:"+host);
//            int portEnd = theUri.indexOf ('/');
//            portEnd = portEnd > 0 ? portEnd : theUri.length ();
//            String port = theUri.substring (portIndex, portEnd - 1);
//            //System.out.println("port:"+port);
//            try {
//                setServerPort (Integer.valueOf (port));
//            } catch (NumberFormatException nef) {
//                org.openide.ErrorManager.getDefault ().log (nef.getLocalizedMessage ());
//            }
//        }
        ip.addPropertyChangeListener( (PropertyChangeEvent evt) -> {
            String name = evt.getPropertyName();
            if (PROP_SERVER_PORT.equals(name) || PROP_USERNAME.equals(name) 
                    || PROP_PASSWORD.equals(name)) {
                // update Ant deployment properties file if it exists
                try {
                    storeAntDeploymentProperties(getAntDeploymentPropertiesFile(), false);
                } catch(IOException ioe) {
                    Logger.getLogger(TomcatProperties.class.getName()).log(Level.INFO, null, ioe);
                }
            }
        });
    }
    
    /** 
     * Stores the Ant deployment properties in the specified file.
     * @param create if false the deployment properties file won't be created if
     *               it does not exist.
     * @throws IOException if a problem occurs.
     */
    public void storeAntDeploymentProperties(File file, boolean create) throws IOException {
        if (!create && !file.exists()) {
            return;
        }
        EditableProperties antProps = new EditableProperties(false);
        antProps.setProperty("tomcat.home", homeDir.getAbsolutePath()); // NOI18N
        antProps.setProperty("tomcat.url", getWebUrl());                // NOI18N
        antProps.setProperty("tomcat.username", getUsername());         // NOI18N
        file.createNewFile();
        FileObject fo = FileUtil.toFileObject(file);
        try (FileLock lock = fo.lock();
                OutputStream os = fo.getOutputStream(lock)) {
            antProps.store(os);
        }
    }
    
    /** Returns file the Ant deployment properties are stored in. */
    public File getAntDeploymentPropertiesFile() {
        return new File(System.getProperty("netbeans.user"), getInstanceID() + ".properties"); // NOI18N
    }
    
    /** 
     * Unique instance identifier used to differentiate different Tomcat instances 
     * in a human-readable form, unlike InstanceProperties.URL_ATTR.
     */
    private String getInstanceID() {
        String name = ip.getProperty(PROP_INSTANCE_ID);
        if (name != null) {
            return name;
        }
        // generate unique tomcat instance identifier (e.g. tomcat55, tomcat55_1, ...
        // for Tomcat 5.5.x and tomcat50, tomcat50_1... for Tomcat 5.0.x)
        String prefix;
        String serverID;
        switch (tm.getTomcatVersion()) {
            case TOMCAT_110:
                prefix = "tomcat110"; // NIO18N
                serverID = TomcatFactory.SERVER_ID_110;
                break;
            case TOMCAT_101:
                prefix = "tomcat101"; // NIO18N
                serverID = TomcatFactory.SERVER_ID_101;
                break;
            case TOMCAT_100:
                prefix = "tomcat100"; // NIO18N
                serverID = TomcatFactory.SERVER_ID_100;
                break;
            case TOMCAT_90:
                prefix = "tomcat90"; // NIO18N
                serverID = TomcatFactory.SERVER_ID_90;
                break;
            case TOMCAT_80:
                prefix = "tomcat80"; // NIO18N
                serverID = TomcatFactory.SERVER_ID_80;
                break;
            case TOMCAT_70:
                prefix = "tomcat70"; // NIO18N
                serverID = TomcatFactory.SERVER_ID_70;
                break;
            case TOMCAT_60:
                prefix = "tomcat60"; // NIO18N
                serverID = TomcatFactory.SERVER_ID_60;
                break;
            case TOMCAT_55:
                prefix = "tomcat55"; // NIO18N
                serverID = TomcatFactory.SERVER_ID_55;
                break;
            case TOMCAT_50:
            default:
                prefix = "tomcat50"; // NIO18N
                serverID = TomcatFactory.SERVER_ID_50;
        }
        String[] instanceURLs = Deployment.getDefault().getInstancesOfServer(serverID);
        for (int i = 0; name == null; i++) {
            if (i == 0) {
                name = prefix;
            } else {
                name = prefix + "_" + i; // NOI18N
            }
            for (String url: instanceURLs) {
                if (!tm.getUri().equals(url)) {
                    InstanceProperties ip = InstanceProperties.getInstanceProperties(url);
                    if (ip != null) {
                        String anotherName = ip.getProperty(PROP_INSTANCE_ID);
                        if (name.equals(anotherName)) {
                            name = null;
                            break;
                        }
                    }
                }
            }
        }
        ip.setProperty(PROP_INSTANCE_ID, name);
        return name;
    }
    
    /** Returns server web url e.g. http://localhost:8080 */
    public String getWebUrl() {
        return "http://" + getHost() + ":" + getServerPort(); // NOI18N
    }
    
    /** Return CATALINA_HOME directory.*/
    public File getCatalinaHome() {
        return homeDir;
    }
    
    /** Return CATALINA_BASE directory or null if not defined. */
    public File getCatalinaBase() {
        return baseDir;
    }    
    
    /** Return CATALINA_BASE directory if defined, CATALINA_HOME otherwise. */
    public File getCatalinaDir() {
        return baseDir == null ? homeDir : baseDir;
    }
    
    /** Returns the lib directory where for example the JDBC drivers should be deployed.
     */
    public File getLibsDir() {
        String libsDir = tm.isTomcat50() || tm.isTomcat55() ? "common/lib" : "lib"; // NOI18N
        return new File(getCatalinaHome(), libsDir);
    }
    
    /** Returns the folder where the HTTP Monitor jar files should be placed */
    public File getMonitorLibFolder() {
        if (tm.isBundledTomcat()) {
            return new File(baseDir, "nblib"); // NOI18N
        }
        return tm.isTomcat50() || tm.isTomcat55()
                ? new File(homeDir, "common/lib") // NOI18N
                : new File(homeDir, "lib"); // NOI18N
    }
    
    /**
     * Return the default Tomcat Java endorsed directory.
     */
    public File getJavaEndorsedDir() {
        if (TomcatVersion.TOMCAT_50 == tm.getTomcatVersion()
                || TomcatVersion.TOMCAT_55 == tm.getTomcatVersion()) {
            return new File(getCatalinaHome(), "common/endorsed"); // NOI18N
        } else {
            return new File(getCatalinaHome(), "endorsed"); // NOI18N
        }
    }
    
    public String getUsername() {
        String val = ip.getProperty(PROP_USERNAME);
        return val != null ? val : ""; // NOI18N
    }
    
    public void setUsername(String value) {
        ip.setProperty(PROP_USERNAME, value);
    }
    
    public String getPassword() {
        String val = ip.getProperty(PROP_PASSWORD);
        return val != null ? val : ""; // NOI18N
    }
    
    public void setPassword(String value) {
        ip.setProperty(PROP_PASSWORD, value);
    }
    
    public JavaPlatform getJavaPlatform() {
        String currentJvm = ip.getProperty(PROP_JAVA_PLATFORM);
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        JavaPlatform[] installedPlatforms = jpm.getPlatforms(null, new Specification("J2SE", null)); // NOI18N
        for (int i = 0; i < installedPlatforms.length; i++) {
            String platformName = installedPlatforms[i].getProperties().get(PLAT_PROP_ANT_NAME);
            if (platformName != null && platformName.equals(currentJvm)) {
                return installedPlatforms[i];
            }
        }
        // return default platform if none was set
        return jpm.getDefaultPlatform();
    }
    
    public void setJavaPlatform(JavaPlatform javaPlatform) {
        ip.setProperty(PROP_JAVA_PLATFORM, javaPlatform.getProperties().get(PLAT_PROP_ANT_NAME));
    }
    
    public String getJavaOpts() {
        String val = ip.getProperty(PROP_JAVA_OPTS);
        return val != null ? val 
                           : DEF_VALUE_JAVA_OPTS;
    }
    
    public void setJavaOpts(String javaOpts) {
        ip.setProperty(PROP_JAVA_OPTS, javaOpts);
    }
    
    public boolean getSecManager() {
        String val = ip.getProperty(PROP_SEC_MANAGER);
        return val != null ? Boolean.valueOf(val)
                           : DEF_VALUE_SEC_MANAGER;
    }
    
    public void setSecManager(boolean enabled) {
        ip.setProperty(PROP_SEC_MANAGER, Boolean.toString(enabled));
    }
    
    
    public boolean getCustomScript() {
        String val = ip.getProperty(PROP_CUSTOM_SCRIPT);
        return val != null ? Boolean.valueOf(val)
                           : DEF_VALUE_CUSTOM_SCRIPT;
    }
    
    public void setCustomScript(boolean enabled) {
        ip.setProperty(PROP_CUSTOM_SCRIPT, Boolean.toString(enabled));
    }
    
    public String getScriptPath() {
        String val = ip.getProperty(PROP_SCRIPT_PATH);
        return val != null ? val 
                           : DEF_VALUE_SCRIPT_PATH;
    }
    
    public void setScriptPath(String path) {
        ip.setProperty(PROP_SCRIPT_PATH, path);
    }
    
    public boolean getForceStop() {
        if (Utilities.isWindows()) {
            return false;
        }
        String val = ip.getProperty(PROP_FORCE_STOP);
        return val != null ? Boolean.valueOf(val)
                           : DEF_VALUE_FORCE_STOP;
    }
    
    public void setForceStop(boolean enabled) {
        ip.setProperty(PROP_FORCE_STOP, Boolean.toString(enabled));
    }
    
    public String getDebugType() {
        String val = ip.getProperty(PROP_DEBUG_TYPE);
        if ((DEBUG_TYPE_SHARED.equalsIgnoreCase(val) && Utilities.isWindows()) 
                || DEBUG_TYPE_SOCKET.equalsIgnoreCase(val)) {
            return val;
        }
        return DEF_VALUE_DEBUG_TYPE;
    }
    
    public void setDebugType(String type) {
        ip.setProperty(PROP_DEBUG_TYPE, type);
    }
    
    public boolean getMonitor() {
        String val = ip.getProperty(PROP_MONITOR);
        return val != null ? Boolean.valueOf(val)
                           : DEF_VALUE_MONITOR;
    }
    
    public void setMonitor(boolean enabled) {
        ip.setProperty(PROP_MONITOR, Boolean.toString(enabled));
    }
    
    public boolean getProxyEnabled() {
        String val = ip.getProperty(PROP_PROXY_ENABLED);
        return val != null ? Boolean.valueOf(val)
                           : DEF_VALUE_PROXY_ENABLED;
    }
    
    public void setProxyEnabled(boolean enabled) {
        ip.setProperty(PROP_PROXY_ENABLED, Boolean.toString(enabled));
    }
    
    public int getDebugPort() {
        String val = ip.getProperty(PROP_DEBUG_PORT);
                
        if (val != null) {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException nfe) {
                Logger.getLogger(TomcatProperties.class.getName()).log(Level.INFO, null, nfe);
            }
        }
        
        if (tm.isBundledTomcat()) {
            return DEF_VALUE_DEBUG_PORT_BUNDLED;
        } else {
            return DEF_VALUE_DEBUG_PORT;
        }
    }
    
    public void setDebugPort(int port) {
        ip.setProperty(PROP_DEBUG_PORT, Integer.toString(port));
    }
    
    
    public int getServerPort() {
        String val = ip.getProperty(PROP_SERVER_PORT);
        if (val != null) {
            try {
                int port = Integer.parseInt(val);
                if (port >= 0 && port <= 65535) {
                    return port;
                }
            } catch (NumberFormatException nfe) {
                Logger.getLogger(TomcatProperties.class.getName()).log(Level.INFO, null, nfe);
            }
        }
        return tm.isBundledTomcat() ? DEF_VALUE_BUNDLED_SERVER_PORT : DEF_VALUE_SERVER_PORT;
    }
    
    /** this needs to be kept in sync with value in the server.xml conf file */
    public void setServerPort(int port) {
        ip.setProperty(PROP_SERVER_PORT, Integer.toString(port));
    }

    public String getServerHeader() {
        String val = ip.getProperty(PROP_SERVER_HEADER);
        if (val != null) {
            return val;
        }
        return "Apache-Coyote/1.1"; // NOI18N
    }

    /** this needs to be kept in sync with value in the server.xml conf file */
    public void setServerHeader(String serverHeader) {
        ip.setProperty(PROP_SERVER_HEADER, serverHeader);
    }
    
    public int getShutdownPort() {
        String val = ip.getProperty(PROP_SHUTDOWN);
        if (val != null) {
            try {
                int port = Integer.parseInt(val);
                if (port >= 0 && port <= 65535) {
                    return port;
                }
            } catch (NumberFormatException nfe) {
                Logger.getLogger(TomcatProperties.class.getName()).log(Level.INFO, null, nfe);
            }
        }
        return tm.isBundledTomcat() ? DEF_VALUE_BUNDLED_SHUTDOWN_PORT : DEF_VALUE_SHUTDOWN_PORT;
    }
    
    /** this needs to be kept in sync with value in the server.xml conf file */
    public void setShutdownPort(int port) {
        ip.setProperty(PROP_SHUTDOWN, Integer.toString(port));
    }
    
    public String getSharedMem() {
        String val = ip.getProperty(PROP_SHARED_MEM);
        return val != null && val.length() > 0 ? val 
                                               : DEF_VALUE_SHARED_MEM;
    }
    
    public void setSharedMem(String val) {
        ip.setProperty(PROP_SHARED_MEM, val);
    }
    
    public int getDeploymentTimeout() {
        String val = ip.getProperty(InstanceProperties.DEPLOYMENT_TIMEOUT);
        if (val != null) {
            try {
                int timeout = Integer.parseInt(val);
                if (timeout >= 1) {
                    return timeout;
                }
            } catch (NumberFormatException nfe) {
                Logger.getLogger(TomcatProperties.class.getName()).log(Level.INFO, null, nfe);
            }
        }
        return DEF_VALUE_DEPLOYMENT_TIMEOUT;
    }
    
    public void setDeploymentTimeout(int timeout) {
        ip.setProperty(InstanceProperties.DEPLOYMENT_TIMEOUT, Integer.toString(timeout));
    }
    
    public int getStartupTimeout() {
        String val = ip.getProperty(InstanceProperties.STARTUP_TIMEOUT);
        if (val != null) {
            try {
                int timeout = Integer.parseInt(val);
                if (timeout >= 1) {
                    return timeout;
                }
            } catch (NumberFormatException nfe) {
                Logger.getLogger(TomcatProperties.class.getName()).log(Level.INFO, null, nfe);
            }
        }
        return DEF_VALUE_STARTUP_TIMEOUT;
    }
    
    public void setStartupTimeout(int timeout) {
        ip.setProperty(InstanceProperties.STARTUP_TIMEOUT, Integer.toString(timeout));
    }
    
    public int getShutdownTimeout() {
        String val = ip.getProperty(InstanceProperties.SHUTDOWN_TIMEOUT);
        if (val != null) {
            try {
                int timeout = Integer.parseInt(val);
                if (timeout >= 1) {
                    return timeout;
                }
            } catch (NumberFormatException nfe) {
                Logger.getLogger(TomcatProperties.class.getName()).log(Level.INFO, null, nfe);
            }
        }
        return DEF_VALUE_SHUTDOWN_TIMEOUT;
    }
    
    public void setShutdownTimeout(int timeout) {
        ip.setProperty(InstanceProperties.SHUTDOWN_TIMEOUT, Integer.toString(timeout));
    }
    
    public boolean getDriverDeployment() {
        String val = ip.getProperty(PROP_DRIVER_DEPLOYMENT);
        return val != null ? Boolean.valueOf(val)
                           : DEF_VALUE_DRIVER_DEPLOYMENT;
    }
    
    public void setDriverDeployment(boolean enabled) {
        ip.setProperty(PROP_DRIVER_DEPLOYMENT, Boolean.toString(enabled));
    }

    public List/*<URL>*/ getClasses() {
        String[] nbFilter = new String[] {
            "httpmonitor",
            "schema2beans",
            /*
             * The following two jars contains eclipse JDT parser. We have to
             * exclude it to not to clash with our jsp parser. See issue #115529.
             */
            "jasper-compiler-jdt",
            "jasper-jdt"
        };

        String[] implFilter = new String[] {
            "-impl.jar"
        };
        
        // tomcat libs
        List<URL> retValue = new ArrayList<>();
        retValue.addAll(listUrls(new File(homeDir, tm.libFolder()), nbFilter));

        // TOMEE as webapp
        if (tm.isTomEE()) {
            File tomee = TomcatFactory.getTomEEWebAppJar(homeDir, baseDir);
            if (tomee != null) {
                retValue.addAll(listUrls(tomee.getParentFile(), nbFilter));
            }
        }

        if (tm.getTomcatVersion().isAtLeast(TomcatVersion.TOMCAT_60)) {
            try {
                retValue.add(new File(homeDir, "bin/tomcat-juli.jar").toURI().toURL()); // NOI18N
            } catch (MalformedURLException e) {
                LOGGER.log(Level.WARNING, "$CATALINA_HOME/bin/tomcat-juli.jar not found", e); // NOI18N
            }
        }

        // wsit
        retValue.addAll(listUrls(new File(homeDir, "common/endorsed"),  implFilter)); // NOI18N
        retValue.addAll(listUrls(new File(homeDir, "shared/lib"),  implFilter)); // NOI18N

        // jwsdp libs
        retValue.addAll(listUrls(new File(homeDir, "jaxws/lib"),    implFilter)); // NOI18N
        retValue.addAll(listUrls(new File(homeDir, "jaxb/lib"),    implFilter)); // NOI18N
        retValue.addAll(listUrls(new File(homeDir, "jwsdp-shared/lib"), implFilter)); // NOI18N
        retValue.addAll(listUrls(new File(homeDir, "jaxp/lib"),    implFilter)); // NOI18N
        retValue.addAll(listUrls(new File(homeDir, "jaxrpc/lib"),  implFilter)); // NOI18N
        retValue.addAll(listUrls(new File(homeDir, "jaxr/lib"),    implFilter)); // NOI18N
        retValue.addAll(listUrls(new File(homeDir, "saaj/lib"),    implFilter)); // NOI18N
        retValue.addAll(listUrls(new File(homeDir, "sjsxp/lib"),   implFilter)); // NOI18N

        // other
        retValue.addAll(listUrls(new File(homeDir, "jstl/lib"),    implFilter)); // NOI18N
        retValue.addAll(listUrls(new File(baseDir, "shared/lib"),  nbFilter));   // NOI18N
        return retValue;
    }
    
    public List/*<URL>*/ getSources() {
        String path = ip.getProperty(PROP_SOURCES);
        if (path == null) {
            return new ArrayList();
        }
        return CustomizerSupport.tokenizePath(path);
    }
                                                                                                                                                                           
    public void setSources(List/*<URL>*/ path) {
        ip.setProperty(PROP_SOURCES, CustomizerSupport.buildPath(path));
        tm.getTomcatPlatform().notifyLibrariesChanged();
    }
    
    private static void addFileToList(List<URL> list, File f) {
        URL u = FileUtil.urlForArchiveOrDir(f);
        if (u != null) {
            list.add(u);
        }
    }

    public List/*<URL>*/ getJavadocs() {
        String path = ip.getProperty(PROP_JAVADOCS);
        if (path == null) {                
            ArrayList list = new ArrayList();
                // tomcat docs
                File jspApiDoc = new File(homeDir, "webapps/tomcat-docs/jspapi"); // NOI18N
                File servletApiDoc = new File(homeDir, "webapps/tomcat-docs/servletapi"); // NOI18N
                if (jspApiDoc.exists() && servletApiDoc.exists()) {
                    addFileToList(list, jspApiDoc);
                    addFileToList(list, servletApiDoc);
                } else {
                    String eeDocs;
                    switch (tm.getTomcatVersion()) {
                        case TOMCAT_110:
                           eeDocs = "docs/jakartaee11-doc-api.jar";
                           break;
                        case TOMCAT_101:
                           eeDocs = "docs/jakartaee10-doc-api.jar";
                           break;
                        case TOMCAT_100:
                            eeDocs = "docs/jakartaee9-doc-api.jar";
                            break;
                        case TOMCAT_90:
                            eeDocs = "docs/jakartaee8-doc-api.jar";
                            break;
                        default:
                            eeDocs = "docs/javaee-doc-api.jar";
                    }
                    File j2eeDoc = InstalledFileLocator.getDefault().locate(eeDocs, null, false); // NOI18N
                    if (j2eeDoc != null) {
                        addFileToList(list, j2eeDoc);
                    }
                }
                // jwsdp docs
                File docs = new File(homeDir, "docs/api"); // NOI18N
                if (docs.exists()) {
                    addFileToList(list, docs);
                }
            return list;
        }
        return CustomizerSupport.tokenizePath(path);
    }
                                                                                                                                                                           
    public void setJavadocs(List/*<URL>*/ path) {
        ip.setProperty(PROP_JAVADOCS, CustomizerSupport.buildPath(path));
        tm.getTomcatPlatform().notifyLibrariesChanged();
    }
    
    public void setOpenContextLogOnRun(boolean val) {
        ip.setProperty(PROP_OPEN_LOG, Boolean.toString(val));
    }
    
    public boolean getOpenContextLogOnRun() {
        Object val = ip.getProperty(PROP_OPEN_LOG);
        if (val != null) {
            return Boolean.valueOf(val.toString());
        }
        return DEF_VALUE_OPEN_LOG;
    }
    
        
    public void setTimestamp(long timestamp) {
        ip.setProperty(PROP_TIMESTAMP, Long.toString(timestamp));
    }
    
    /** Return last server.xml check timestamp, or -1 if not set */
    public long getTimestamp() {
        String val = ip.getProperty(PROP_TIMESTAMP);        
        if (val != null) {
            try {
                return Long.parseLong(val);
            } catch (NumberFormatException nfe) {
                Logger.getLogger(TomcatProperties.class.getName()).log(Level.INFO, null, nfe);
            }
        }
        return -1;
    }
    
    /**
     * Return server.xml file from the catalina base folder if the base folder is used 
     * or from the catalina home folder otherwise.
     * <p>
     * <b>BEWARE</b>: If the catalina base folder is used but has not bee generated yet,
     * the server.xml file from the catalina home folder will be returned.
     * </p>
     */
    public File getServerXml() {
        String confServerXml = "conf/server.xml"; // NIO18N
        File serverXml = null;
        if (baseDir != null) {
            serverXml = new File(baseDir, confServerXml);
        }
        if (serverXml == null || !serverXml.exists()) {
            serverXml = new File(getCatalinaHome(), confServerXml);
        }
        return serverXml;
    }

    /**
     * Return tomee.xml/openejb.xml file from the catalina base folder if the base
     * folder is used or from the catalina home folder otherwise. The file must
     * exist otherwise <code>null</code> is returned.
     * <p>
     * <b>BEWARE</b>: If the catalina base folder is used but has not bee generated
     * yet, the file from the catalina home folder will be returned.
     * </p>
     */
    @CheckForNull
    public File getTomeeXml() {
        String confTomeeXml = "conf/tomee.xml"; // NIO18N
        String confOpenejbXml = "conf/openejb.xml"; // NIO18N
        File tomeeXml = null;
        if (baseDir != null) {
            tomeeXml = new File(baseDir, confTomeeXml);
            if (!tomeeXml.isFile()) {
                tomeeXml = new File(baseDir, confOpenejbXml);
            }
        }
        if (tomeeXml == null || !tomeeXml.isFile()) {
            tomeeXml = new File(getCatalinaHome(), confTomeeXml);
            if (!tomeeXml.isFile()) {
                tomeeXml = new File(getCatalinaHome(), confOpenejbXml);
            }
        }
        if (tomeeXml.isFile()) {
            return tomeeXml;
        }
        return null;
    }
    
    public String getHost () {
        String val = ip.getProperty(PROP_HOST);
        return val != null ? val : DEF_VALUE_HOST;
    }
    
    public int getRunningCheckTimeout() {
        String val = ip.getProperty(PROP_RUNNING_CHECK_TIMEOUT);
        if (val != null) {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException nfe) {
                Logger.getLogger(TomcatProperties.class.getName()).log(Level.INFO, null, nfe);
            }
        }
        return DEF_VALUE_RUNNING_CHECK_TIMEOUT;
    }
    
    public String getDisplayName() {
        String val = ip.getProperty(PROP_DISPLAY_NAME);
        return val != null && val.length() > 0 ? val 
                                               : DEF_VALUE_DISPLAY_NAME;
    }
    
    // private helper methods -------------------------------------------------
    
    private static List<URL> listUrls(final File folder, final String[] filter) {
        File[] jars = folder.listFiles( (File dir, String name) -> {
            if (!name.endsWith(".jar") || !dir.equals(folder)) {
                return false;
            }
            for (int i = 0; i < filter.length; i++) {
                if (name.indexOf(filter[i]) != -1) {
                    return false;
                }
            }
            return true;
        });
        if (jars == null) {
            return Collections.emptyList();
        }
        Arrays.sort(jars);
        List/*<URL>*/ urls = new ArrayList(jars.length);
        for (int i = 0; i < jars.length; i++) {
            addFileToList(urls, jars[i]);
        }
        return urls;
    }
}
