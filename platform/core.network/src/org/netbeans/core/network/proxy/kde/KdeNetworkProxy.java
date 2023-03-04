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
package org.netbeans.core.network.proxy.kde;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.network.proxy.NetworkProxyResolver;
import org.netbeans.core.network.proxy.NetworkProxySettings;

/**
 *
 * @author lfischme
 */
public class KdeNetworkProxy implements NetworkProxyResolver {
    
    private static final Logger LOGGER = Logger.getLogger(KdeNetworkProxy.class.getName());

    private static final String EMPTY_STRING = ""; //NOI18N
    private static final String SPACE = " "; //NOI18N
    private static final String EQUALS = "="; //NOI18N
    private static final String COLON = ":"; //NOI18N
    private static final String COMMA = ","; //NOI18N
    private static final String SQ_BRACKET_LEFT = "["; //NOI18N
    private static final String HOME = "HOME"; //NOI18N
    private static final String KIOSLAVERC_PROXY_SETTINGS_GROUP = "[Proxy Settings]"; //NOI18N
    private static final String KIOSLAVERC_PROXY_TYPE = "ProxyType"; //NOI18N
    private static final String KIOSLAVERC_PROXY_CONFIG_SCRIPT = "Proxy Config Script"; //NOI18N
    private static final String KIOSLAVERC_HTTP_PROXY = "httpProxy"; //NOI18N
    private static final String KIOSLAVERC_HTTPS_PROXY = "httpsProxy"; //NOI18N
    private static final String KIOSLAVERC_SOCKS_PROXY = "socksProxy"; //NOI18N
    private static final String KIOSLAVERC_NO_PROXY_FOR = "NoProxyFor"; //NOI18N
    private static final String KIOSLAVERC_PROXY_TYPE_NONE = "0"; //NOI18N
    private static final String KIOSLAVERC_PROXY_TYPE_MANUAL = "1"; //NOI18N
    private static final String KIOSLAVERC_PROXY_TYPE_PAC = "2"; //NOI18N
    private static final String KIOSLAVERC_PROXY_TYPE_AUTO = "3"; //NOI18N
    private static final String KIOSLAVERC_PROXY_TYPE_SYSTEM = "4"; //NOI18N    
    private static final String KIOSLAVERC_PATH_IN_HOME = ".config/kioslaverc"; //NOI18N 
    private static final String KIOSLAVERC_PATH_IN_HOME_KDE4 = ".kde/share/config/kioslaverc"; //NOI18N 
    
    private final List<Path> KIOSLAVERC_PATHS;

    public KdeNetworkProxy() {
        KIOSLAVERC_PATHS = getKioslavercPaths();
    }

    @Override
    public NetworkProxySettings getNetworkProxySettings() {
        LOGGER.log(Level.FINE, "KDE system proxy resolver started."); //NOI18N
        Map<String, String> kioslavercMap = getKioslavercMap();

        String proxyType = kioslavercMap.get(KIOSLAVERC_PROXY_TYPE);
        if (proxyType == null) {
            LOGGER.log(Level.WARNING, "KDE system proxy resolver: The kioslaverc key not found ({0})", KIOSLAVERC_PROXY_TYPE); //NOI18N
            return new NetworkProxySettings(false);
        }

        if (proxyType.equals(KIOSLAVERC_PROXY_TYPE_NONE)) {
            LOGGER.log(Level.INFO, "KDE system proxy resolver: direct (proxy type: {0})", proxyType); //NOI18N
            return new NetworkProxySettings();
        }

        if (proxyType.equals(KIOSLAVERC_PROXY_TYPE_PAC)  || proxyType.equals(KIOSLAVERC_PROXY_TYPE_AUTO)) {
            String pacFileUrl;
            
            if (proxyType.equals(KIOSLAVERC_PROXY_TYPE_AUTO)) {
                LOGGER.log(Level.INFO, "KDE system proxy resolver: auto"); //NOI18N
                pacFileUrl = "http://wpad/wpad.dat"; // NOI18N
            } else {
                LOGGER.log(Level.INFO, "KDE system proxy resolver: auto - PAC"); //NOI18N
                pacFileUrl = kioslavercMap.get(KIOSLAVERC_PROXY_CONFIG_SCRIPT);
            }
            if (pacFileUrl != null) {
                LOGGER.log(Level.INFO, "KDE system proxy resolver: PAC URL ({0})", pacFileUrl); //NOI18N
                return new NetworkProxySettings(pacFileUrl);
            } else {
                LOGGER.log(Level.INFO, "KDE system proxy resolver: PAC URL null value"); //NOI18N
                return new NetworkProxySettings(false);
            }
        }

        if (proxyType.equals(KIOSLAVERC_PROXY_TYPE_MANUAL) || proxyType.equals(KIOSLAVERC_PROXY_TYPE_SYSTEM)) {
            LOGGER.log(Level.INFO, "KDE system proxy resolver: manual (proxy type: {0})", proxyType); //NOI18N
            
            String httpProxy = kioslavercMap.get(KIOSLAVERC_HTTP_PROXY);
            String httpsProxy = kioslavercMap.get(KIOSLAVERC_HTTPS_PROXY);
            String socksProxy = kioslavercMap.get(KIOSLAVERC_SOCKS_PROXY);
            String noProxyFor = kioslavercMap.get(KIOSLAVERC_NO_PROXY_FOR);
            
            LOGGER.log(Level.INFO, "KDE system proxy resolver: http proxy ({0})", httpProxy); //NOI18N
            LOGGER.log(Level.INFO, "KDE system proxy resolver: https proxy ({0})", httpsProxy); //NOI18N
            LOGGER.log(Level.INFO, "KDE system proxy resolver: socks proxy ({0})", socksProxy); //NOI18N
            LOGGER.log(Level.INFO, "KDE system proxy resolver: no proxy ({0})", noProxyFor); //NOI18N
            
            if (proxyType.equals(KIOSLAVERC_PROXY_TYPE_MANUAL)) {
                httpProxy = httpProxy == null ? EMPTY_STRING : httpProxy.trim().replaceAll(SPACE, COLON);
                httpsProxy = httpsProxy == null ? EMPTY_STRING : httpsProxy.trim().replaceAll(SPACE, COLON);
                socksProxy = socksProxy == null ? EMPTY_STRING : socksProxy.trim().replaceAll(SPACE, COLON);
            }
            
            String[] noProxyHosts = getNoProxyHosts(noProxyFor);
            
            return new NetworkProxySettings(httpProxy, httpsProxy, socksProxy, noProxyHosts);
        }

        return new NetworkProxySettings(false);
    }

    /**
     * Raturns map of keys and values from kioslaverc group Proxy settings.
     * 
     * Reads "[userhome]/.kde/share/config/kioslaverc" file. 
     * 
     * @return Map of keys and values from kioslaverc group Proxy settings.
     */
    private Map<String, String> getKioslavercMap() {
        boolean fileExists = false;
        for (Path p : KIOSLAVERC_PATHS) {
            if (!Files.isRegularFile(p)) {
                continue;
            }
            fileExists = true;
            Map<String, String> map = new HashMap<String, String>();
            boolean groupFound = false;
            
            try (BufferedReader br = Files.newBufferedReader(p)) {
                String line;
                boolean inGroup = false;
                while ((line = br.readLine()) != null) {
                    if (inGroup) {
                        if (line.contains(EQUALS)) {
                            int indexOfEquals = line.indexOf(EQUALS);
                            String key = line.substring(0, indexOfEquals);
                            String value = line.substring(indexOfEquals + 1);
                            map.put(key, value);
                        } else if (line.startsWith(SQ_BRACKET_LEFT)) {
                            break;
                        }
                    } else if (line.startsWith(KIOSLAVERC_PROXY_SETTINGS_GROUP)) {
                        inGroup = true;
                        groupFound = true;
                    }
                }
            } catch (FileNotFoundException fnfe) {
                LOGGER.log(Level.SEVERE, "Cannot read file: ", fnfe);
            } catch (IOException ioe) {
                LOGGER.log(Level.SEVERE, "Cannot read file: ", ioe);
            }
            if (groupFound) {
                return map;
            }
        }
        
        if (fileExists) {
            LOGGER.log(Level.WARNING, "KDE system proxy resolver: The kioslaverc file does not contain proxy configuration ({0})", KIOSLAVERC_PATHS);
        } else {
            LOGGER.log(Level.WARNING, "KDE system proxy resolver: The kioslaverc file not found ({0})", KIOSLAVERC_PATHS);
        }                
        return Collections.emptyMap();
    }

    /**
     * Returns path of the kioslaverc config file.
     * 
     * @return Path of the kioslaverc config file.
     */
    private List<Path> getKioslavercPaths() {
        String homePath = System.getenv(HOME);

        if (homePath != null) {
            return Arrays.asList(
                        Paths.get(homePath, KIOSLAVERC_PATH_IN_HOME),
                        Paths.get(homePath, KIOSLAVERC_PATH_IN_HOME_KDE4)
            );
        } else {
            return Collections.emptyList();
        }
    }
    
    /**
     * Returns array of Strings of no proxy hosts.
     * 
     * @param noProxyHostsString No proxy host in one string separated by comma.
     * @return Array of Strings of no proxy hosts.
     */
    private static String[] getNoProxyHosts(String noProxyHostsString) {
        if (noProxyHostsString != null && !noProxyHostsString.isEmpty()) {
            return noProxyHostsString.split(COMMA);
        }
            
        return new String[0];
    }
}
