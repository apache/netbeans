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
package org.netbeans.core.network.proxy.gnome;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.network.proxy.NetworkProxySettings;
import static org.netbeans.core.network.proxy.gnome.GnomeNetworkProxy.executeCommand;

/**
 *
 * @author lfischme
 */
public class GconfNetworkProxy {
    
    private static final Logger LOGGER = Logger.getLogger(GconfNetworkProxy.class.getName());
    
    private static final String EQUALS = "="; //NOI18N
    private static final String COLON = ","; //NOI18N
    private static final String SQ_BRACKET_LEFT = "["; //NOI18N
    private static final String SQ_BRACKET_RIGHT = "]"; //NOI18N
    
    protected static final String GCONF_PATH = "/usr/bin/gconftool-2"; //NOI18N
    private static final String GCONF_ARGUMENT_LIST_RECURSIVELY = " -R "; //NOI18N
    private static final String GCONF_NODE_PROXY = "/system/proxy"; //NOI18N
    private static final String GCONF_NODE_HTTP_PROXY = "/system/http_proxy"; //NOI18N
    private static final String GCONF_KEY_MODE = "mode"; //NOI18N
    private static final String GCONF_KEY_PAC_URL = "autoconfig_url"; //NOI18N
    private static final String GCONF_KEY_HTTP_ALL = "use_http_proxy"; //NOI18N
    private static final String GCONF_KEY_HTTP_HOST = "host"; //NOI18N
    private static final String GCONF_KEY_HTTP_PORT = "port"; //NOI18N
    private static final String GCONF_KEY_HTTPS_HOST = "secure_host"; //NOI18N
    private static final String GCONF_KEY_HTTPS_PORT = "secure_port"; //NOI18N
    private static final String GCONF_KEY_SOCKS_HOST = "socks_host"; //NOI18N
    private static final String GCONF_KEY_SOCKS_PORT = "socks_port"; //NOI18N
    private static final String GCONF_KEY_IGNORE_HOSTS = "ignore_hosts"; //NOI18N
    private static final String GCONF_VALUE_NONE = "none"; //NOI18N
    private static final String GCONF_VALUE_AUTO = "auto"; //NOI18N
    private static final String GCONF_VALUE_MANUAL = "manual"; //NOI18N

    /**
     * Returns network proxy settings - retrieved via gconftool.
     * 
     * @return network proxy settings via GSettings.
     */
    protected static NetworkProxySettings getNetworkProxySettings() {
        LOGGER.log(Level.FINE, "GConf system proxy resolver started."); //NOI18N
        Map<String, String> proxyProperties = getGconfMap(GCONF_NODE_PROXY);
                        
        String proxyMode = proxyProperties.get(GCONF_KEY_MODE);
        if (proxyMode == null) {
            LOGGER.log(Level.SEVERE, "GConf proxy mode is null.");
            return new NetworkProxySettings(false);
        }        
        
        if (proxyMode.equals(GCONF_VALUE_NONE)) {
            LOGGER.log(Level.INFO, "GConf system proxy resolver: direct connection"); //NOI18N
            return new NetworkProxySettings();
        }
        
        if (proxyMode.equals(GCONF_VALUE_AUTO)) {
            String pacUrl = proxyProperties.get(GCONF_KEY_PAC_URL);
            
            LOGGER.log(Level.INFO, "GConf system proxy resolver: auto - PAC ({0})", pacUrl); //NOI18N
            
            if (pacUrl != null) {
                return new NetworkProxySettings(pacUrl);             
            } else {
                return new NetworkProxySettings("");
            }
        }
        
        if (proxyMode.equals(GCONF_VALUE_MANUAL)) {
            proxyProperties.putAll(getGconfMap(GCONF_NODE_HTTP_PROXY));
            
            String httpProxyAll = proxyProperties.get(GCONF_KEY_HTTP_ALL);
            String httpProxyHost = proxyProperties.get(GCONF_KEY_HTTP_HOST);
            String httpProxyPort = proxyProperties.get(GCONF_KEY_HTTP_PORT);
            String noProxyHosts = proxyProperties.get(GCONF_KEY_IGNORE_HOSTS);
            
            LOGGER.log(Level.INFO, "GConf system proxy resolver: manual - http for all ({0})", httpProxyAll); //NOI18N
            LOGGER.log(Level.INFO, "GConf system proxy resolver: manual - http host ({0})", httpProxyHost); //NOI18N
            LOGGER.log(Level.INFO, "GConf system proxy resolver: manual - http port ({0})", httpProxyPort); //NOI18N
            LOGGER.log(Level.INFO, "GConf system proxy resolver: manual - no proxy hosts ({0})", noProxyHosts); //NOI18N
            
            if (httpProxyAll != null && Boolean.parseBoolean(httpProxyAll)) {
                return new NetworkProxySettings(httpProxyHost, httpProxyPort, getNoProxyHosts(noProxyHosts));
            } else {
                String httpsProxyHost = proxyProperties.get(GCONF_KEY_HTTPS_HOST);
                String httpsProxyPort = proxyProperties.get(GCONF_KEY_HTTPS_PORT);
                String socksProxyHost = proxyProperties.get(GCONF_KEY_SOCKS_HOST);
                String socksProxyPort = proxyProperties.get(GCONF_KEY_SOCKS_PORT);
                
                LOGGER.log(Level.INFO, "GConf system proxy resolver: manual - https host ({0})", httpsProxyHost); //NOI18N
                LOGGER.log(Level.INFO, "GConf system proxy resolver: manual - https port ({0})", httpsProxyPort); //NOI18N
                LOGGER.log(Level.INFO, "GConf system proxy resolver: manual - socks host ({0})", socksProxyHost); //NOI18N
                LOGGER.log(Level.INFO, "GConf system proxy resolver: manual - socks port ({0})", socksProxyPort); //NOI18N
                
                return new NetworkProxySettings(httpProxyHost, httpProxyPort, 
                        httpsProxyHost, httpsProxyPort, 
                        socksProxyHost, socksProxyPort, getNoProxyHosts(noProxyHosts));
            }
        }
        
        return new NetworkProxySettings(false);
    }
    
        /**
     * Checks if gconftool returns suitable response
     * 
     * @return true if gconftool returns suitable response
     */
    protected static boolean isGconfValid() {
        String command = GCONF_PATH + GCONF_ARGUMENT_LIST_RECURSIVELY + GCONF_NODE_PROXY;
        
        try {
            BufferedReader reader = executeCommand(command);
            if (reader.ready()) {
                return true;
            }
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "Cannot read line: " + command, ioe); //NOI18N
        }
        
        LOGGER.log(Level.WARNING, "GConf return empty list"); //NOI18N
        return false;
    }
    
    /**
     * Returns map of properties retrieved from gconftool-2.
     * 
     * Executes the command "/usr/bin/gconftool-2 -R [node]".
     * 
     * @param gconfNode Node for which the properties should be returned.
     * @return Map of properties retrieved from gconftool-2.
     */
    private static Map<String, String> getGconfMap(String gconfNode) {
        Map<String, String> map = new HashMap<String, String>();
        
        String command = GCONF_PATH + GCONF_ARGUMENT_LIST_RECURSIVELY + gconfNode;
        
        try {
            BufferedReader reader = executeCommand(command);
            if (reader != null) {
                String line = reader.readLine();
                while (line != null) {
                    String key = getKey(line).toLowerCase();                                   
                    if (key != null && !key.isEmpty()) {
                        String value = getValue(line); 
                        map.put(key, value);
                    }
                    line = reader.readLine();
                }
            } else {
                return map;
            }
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "Cannot read line: " + command, ioe); //NOI18N
        }

        return map;
    }
    
    /**
     * Returns the key for one line response from gconftool-2.
     * 
     * @param line Line from gconftool-2 response.
     * @return The key for one line response from gconftool-2.
     */
    private static String getKey(String line) {        
        return line.substring(0, line.indexOf(EQUALS)).trim();
    }

    /**
     * Returns the value for one line response from gconftool-2.
     * 
     * @param line Line from gconftool-2 response.
     * @return The value for one line response from gconftool-2.
     */
    private static String getValue(String line) {
        return line.substring(line.indexOf(EQUALS) + 1).trim();
    }
    
    /**
     * Returns array of Strings of no proxy hosts.
     * 
     * The value responding to "ignore_hosts" key.
     * 
     * Parses the value returned from gconftool-2.
     * Usually [host1,host2,host3]
     * 
     * @param noProxyHostsString The value returned from gconftool-2.
     * @return Array of Strings of no proxy hosts.
     */
    private static String[] getNoProxyHosts(String noProxyHostsString) {
        if (noProxyHostsString != null && !noProxyHostsString.isEmpty()) {
            if (noProxyHostsString.startsWith(SQ_BRACKET_LEFT)) {
                noProxyHostsString = noProxyHostsString.substring(1);
            }
            
            if (noProxyHostsString.endsWith(SQ_BRACKET_RIGHT)) {
                noProxyHostsString = noProxyHostsString.substring(0, noProxyHostsString.length() - 1);
            }
            
            return noProxyHostsString.split(COLON);
        }
            
        return new String[0];
    }
}
