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
package org.netbeans.modules.javafx2.scenebuilder.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import org.netbeans.modules.javafx2.scenebuilder.Home;
import org.netbeans.modules.javafx2.scenebuilder.HomeFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * Creates {@linkplain Home} instance for an SB installation path
 * @author Jaroslav Bachorik
 * @author Petr Somol
 */
public class SBHomeFactory {
    private static final String PATH_DELIMITER = ";"; //NOI18N
    private static final String VER_DELIMITER = "$ver$"; //NOI18N
    private static final String APPDATA_DELIMITER = "$AppData$"; //NOI18N
    private static final String EMPTY_STRING = "$empty$"; //NOI18N
    private static final String DEFAULT_VERSION = "1.0"; //NOI18N
    
    private static final List<String> VER_CURRENT = tokenize(NbBundle.getMessage(SBHomeFactory.class, "SB_Version"), PATH_DELIMITER); // NOI18N
    
    private static HomeFactory WINDOWS_HOME_LOCATOR = null;
    private static HomeFactory MAC_HOME_LOCATOR = null;    
    private static HomeFactory UX_HOME_LOCATOR = null;
    
    /**
     * Return default Home factory depending on current OS
     * @return default Home factory
     */
    public static HomeFactory getDefault() {
        if (Utilities.isWindows()) {
            return getDefaultWindows();
        } else if (Utilities.isMac()) {
            return getDefaultMac();
        } else {
            return getDefaultUx();
        }
    }
    
    private static HomeFactory getDefaultWindows() {
        if(WINDOWS_HOME_LOCATOR == null) {
            WINDOWS_HOME_LOCATOR = new HomeFactoryCommon(
                tokenize(NbBundle.getMessage(SBHomeFactory.class, "WIN_WKIP"), PATH_DELIMITER), // NOI18N
                tokenize(NbBundle.getMessage(SBHomeFactory.class, "WIN_LAUNCHER"), PATH_DELIMITER), // NOI18N
                tokenize(NbBundle.getMessage(SBHomeFactory.class, "WIN_PROPERTIES"), PATH_DELIMITER) // NOI18N
            );
        }
        return WINDOWS_HOME_LOCATOR;
    }
    
    private static HomeFactory getDefaultMac() {
        if(MAC_HOME_LOCATOR == null) {
            MAC_HOME_LOCATOR = new HomeFactoryCommon(
                tokenize(NbBundle.getMessage(SBHomeFactory.class, "MAC_WKIP"), PATH_DELIMITER), // NOI18N
                tokenize(NbBundle.getMessage(SBHomeFactory.class, "MAC_LAUNCHER"), PATH_DELIMITER), // NOI18N
                tokenize(NbBundle.getMessage(SBHomeFactory.class, "MAC_PROPERTIES"), PATH_DELIMITER) // NOI18N
            );
        }
        return MAC_HOME_LOCATOR;
    }
    
    private static HomeFactory getDefaultUx() {
        if(UX_HOME_LOCATOR == null) {
            UX_HOME_LOCATOR = new HomeFactoryCommon(
                tokenize(NbBundle.getMessage(SBHomeFactory.class, "UX_WKIP"), PATH_DELIMITER), // NOI18N
                tokenize(NbBundle.getMessage(SBHomeFactory.class, "UX_LAUNCHER"), PATH_DELIMITER), // NOI18N
                tokenize(NbBundle.getMessage(SBHomeFactory.class, "UX_PROPERTIES"), PATH_DELIMITER) // NOI18N
            );
        }
        return UX_HOME_LOCATOR;
    }
    
    /**
     * Home factory implementation to be shared across different OSs.
     * See Bundle.properties for OS specific definitions
     */
    private static final class HomeFactoryCommon implements HomeFactory {
        private final List<String> WKIP;
        private final List<String> LAUNCHER_PATH;
        private final List<String> PROPERTIES_PATH;
        
        HomeFactoryCommon(List<String> WKIP, List<String> LAUNCHER_PATH, List<String> PROPERTIES_PATH) {
            Parameters.notNull("WKIP", WKIP); //NOI18N
            Parameters.notNull("LAUNCHER_PATH", LAUNCHER_PATH); //NOI18N
            Parameters.notNull("PROPERTIES_PATH", PROPERTIES_PATH); //NOI18N
            this.WKIP = WKIP;
            this.LAUNCHER_PATH = LAUNCHER_PATH;
            this.PROPERTIES_PATH = PROPERTIES_PATH;
        }
        
        /**
         * Return Home for default SB location, if SB can be found there
         * @return Home
         */
        @Override
        public Home defaultHome() {
            Home h = null;
            for (String ver : VER_CURRENT) {
                for (String path : WKIP) {
                    if (Utilities.isWindows() && path.contains("AppData")) { //NOI18N
                        // issue #251710 - Gluon SceneBuilder by default installs to ﻿C:\Users\<username>\AppData\Local\SceneBuilder
                        final String appDataPath = System.getenv("AppData"); //NOI18N
                        if (appDataPath != null) {
                            final FileObject appDataFo = FileUtil.toFileObject(new File(appDataPath)).getParent();
                            h = loadHome(path.replace(VER_DELIMITER, ver).replace(APPDATA_DELIMITER, appDataFo.getPath()));
                        }
                    } else {
                        h = loadHome(path.replace(VER_DELIMITER, ver), ver);
                    }
                    if (h != null) {
                        return h;
                    }
                }
            }
            return h;
        }

        /**
         * Return SB Home for given customPath, if it is a valid SB location
         * @param customPath
         * @return Home
         */
        @Override
        public Home loadHome(String customPath) {
            return loadHome(customPath, DEFAULT_VERSION);
        }
        
        private Home loadHome(String customPath, String defaultVersion) {
            Home h = null;
            for(String ver : VER_CURRENT) {
                for(String launcher : LAUNCHER_PATH) {
                    for(String props : PROPERTIES_PATH) {
                        try {
                            h = getHomeForPath(customPath, launcher.replace(VER_DELIMITER, ver), props.replace(VER_DELIMITER, ver), defaultVersion.isEmpty() ? (ver.isEmpty() ? DEFAULT_VERSION : ver) : defaultVersion);
                            if(h != null) {
                                return h;
                            }
                        } catch(PathDoesNotExist e) {
                            // stop search only if customPath does not exits, otherwise try other version/launcher/properties combination
                            return null;
                        }
                    }
                }
            }
            return null;
        }
    };

    /**
     * Returns Home if path is valid path and launcherPath points at existing launcher file
     * @param path
     * @param launcherPath
     * @param propertiesPath
     * @return Home
     */
    private static Home getHomeForPath(String path, String launcherPath, String propertiesPath, String defaultVersion) throws PathDoesNotExist {
        Parameters.notNull("path", path); //NOI18N
        Parameters.notNull("launcherPath", launcherPath); //NOI18N
        Parameters.notNull("propertiesPath", propertiesPath); //NOI18N
        String homePath = path;
        if(path.startsWith("~")) { // NOI18N
            String userHome = System.getProperty("user.home"); // NOI18N
            homePath = userHome + path.substring(1);
        }
        File installDir = new File(homePath);
        if (installDir != null && installDir.exists() && installDir.isDirectory()) {
            FileObject installDirFO = FileUtil.toFileObject(installDir);

            File launcher = new File(homePath + File.separator + launcherPath);
            if(launcher != null && launcher.exists() && launcher.isFile()) {
            
                FileObject propertiesFO = installDirFO.getFileObject(propertiesPath); // NOI18N
                if (propertiesFO != null && propertiesFO.isValid() && propertiesFO.isData()) {
                    try {
                        Properties props = new Properties();
                        FileReader reader = new FileReader(FileUtil.toFile(propertiesFO));
                        try {
                            props.load(reader);
                        } finally {
                            reader.close();
                        }
                        String version = props.getProperty("version"); //NOI18N
                        if (version == null) {
                            version = props.getProperty("app.version"); //NOI18N
                        }
                        return new Home(homePath, launcherPath, propertiesPath, version == null ? defaultVersion : version);
                    } catch (IOException e) {
                    }
                } else if (Utilities.isMac() && path.equals(NbBundle.getMessage(SBHomeFactory.class, "MAC_GLUON_HOME"))) { //NOI18N
                    // Gluon SceneBuilder 8.0.0 does not have scenebuilder.properties file
                    return new Home(homePath, launcherPath, propertiesPath, defaultVersion);
                }
            }
        } else {
            throw new PathDoesNotExist();
        }
        return null;
    }
    
    private static final class PathDoesNotExist extends IOException {
        int code;
        PathDoesNotExist(int code) {
            this.code = code;
        }
        PathDoesNotExist() {
            this.code = 0;
        }
        int getCode() {
            return code;
        }
    }
    
    private static List<String> tokenize(String sequence, String delimiter) {
        StringTokenizer st = new StringTokenizer(sequence, delimiter);
        List<String> r = new ArrayList<String>();
        while(st.hasMoreTokens()) {
            String next = st.nextToken();
            r.add(next.equals(EMPTY_STRING) ? "" : next); // NOI18N
        }
        return r;
    }
}
