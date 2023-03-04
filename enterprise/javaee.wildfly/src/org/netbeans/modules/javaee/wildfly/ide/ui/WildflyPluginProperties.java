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
package org.netbeans.modules.javaee.wildfly.ide.ui;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils.Version;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Plugin Properties Singleton class
 * @author Ivan Sidorkin
 */
public final class WildflyPluginProperties {

    public static final String PROPERTY_DISPLAY_NAME ="displayName";//NOI18N
    public static final String PROPERTY_SERVER = "server";//NOI18N
    public static final String PROPERTY_DEPLOY_DIR = "deploy-dir";//NOI18N
    public static final String PROPERTY_SERVER_DIR = "server-dir";//NOI18N
    public static final String PROPERTY_ROOT_DIR = "root-dir";//NOI18N
    public static final String PROPERTY_HOST = "host";//NOI18N
    public static final String PROPERTY_PORT = "port";//NOI18N
    public static final String PROPERTY_ADMIN_PORT = "admin-port";//NOI18N
    public static final String PROPERTY_JAVA_OPTS = "java_opts"; // NOI18N
    public static final String PROPERTY_CONFIG_FILE = "config_file"; // NOI18N

    private static WildflyPluginProperties pluginProperties = null;
    private String installLocation;
    private String domainLocation;
    private String configLocation;
    private Version serverVersion = WildflyPluginUtils.WILDFLY_8_0_0;


    public static WildflyPluginProperties getInstance(){
        if(pluginProperties==null){
            pluginProperties = new WildflyPluginProperties();
        }
        return pluginProperties;
    }



    /** Creates a new instance of */
    private WildflyPluginProperties() {
        java.io.InputStream inStream = null;
        try {
            try {
                propertiesFile = getPropertiesFile();
                if (null != propertiesFile) {
                    inStream = propertiesFile.getInputStream();
                }
            } catch (java.io.FileNotFoundException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            } catch (java.io.IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            } finally {
                loadPluginProperties(inStream);
                if (null != inStream) {
                    inStream.close();
                }
            }
        } catch (java.io.IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }

    }

    void loadPluginProperties(java.io.InputStream inStream) {
        Properties inProps = new Properties();
        if (null != inStream) {
            try {
                inProps.load(inStream);
            } catch (java.io.IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        }
        String loc = inProps.getProperty(INSTALL_ROOT_KEY);
        if (loc!=null){// try to get the default value
            setInstallLocation(loc);
        }
    }

    private static final String INSTALL_ROOT_KEY = "installRoot"; // NOI18N
    public static final String INSTALL_ROOT_PROP_NAME = "com.sun.aas.installRoot"; //NOI18N


    private  FileObject propertiesFile = null;

    private FileObject getPropertiesFile() throws java.io.IOException {
        FileObject dir = FileUtil.getConfigFile("J2EE");
        FileObject retVal = null;
        if (null != dir) {
            retVal = dir.getFileObject("jb","properties"); // NOI18N
            if (null == retVal) {
                retVal = dir.createData("jb","properties"); //NOI18N
            }
        }
        return retVal;
    }


    public void saveProperties(){
        Properties outProp = new Properties();
        String installRoot = getInstallLocation();
        if (installRoot != null) {
            outProp.setProperty(INSTALL_ROOT_KEY, installRoot);
        }

        FileLock l = null;
        java.io.OutputStream outStream = null;
        try {
            if (null != propertiesFile) {
                try {
                    l = propertiesFile.lock();
                    outStream = propertiesFile.getOutputStream(l);
                    if (null != outStream) {
                        outProp.store(outStream, "");
                    }
                } catch (java.io.IOException e) {
                    Logger.getLogger("global").log(Level.INFO, null, e);
                } finally {
                    if (null != outStream) {
                        outStream.close();
                    }
                    if (null != l) {
                        l.releaseLock();
                    }
                }
            }
        } catch (java.io.IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
    }

    public boolean isCurrentServerLocationValid() {
        if (getInstallLocation() != null) {
            return WildflyPluginUtils.isGoodJBServerLocation(new File(getInstallLocation()));
        }

        return false;
    }

    public int getAdminPort() {
        if(this.installLocation == null || WildflyPluginUtils.WILDFLY_8_0_0.compareTo(serverVersion) > 0){
            return 9999;
        }
        return 9990;
    }


    public void setInstallLocation(String installLocation) {
        if (installLocation.endsWith(File.separator)) {
            this.installLocation = installLocation.substring(0, installLocation.length() - 1);
        } else {
            this.installLocation = installLocation;
        }
        this.serverVersion = WildflyPluginUtils.getServerVersion(new File(this.installLocation));
    }

    public String getInstallLocation() {
        return this.installLocation;
    }

    public String getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(String configLocation) {
        if (configLocation.endsWith(File.separator)) {
            configLocation = configLocation.substring(0, configLocation.length() - 1);
        }
        this.configLocation = configLocation;
    }

    public void setDomainLocation(String domainLocation) {
        if (domainLocation.endsWith(File.separator)) {
            domainLocation = domainLocation.substring(0, domainLocation.length() - 1);
        }

        this.domainLocation = domainLocation;
    }

    public String getDomainLocation() {
        return domainLocation;
    }
}
