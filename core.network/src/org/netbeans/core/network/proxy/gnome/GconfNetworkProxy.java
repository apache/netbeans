/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
    
    private final static Logger LOGGER = Logger.getLogger(GconfNetworkProxy.class.getName());
    
    private final static String EQUALS = "="; //NOI18N
    private final static String COLON = ","; //NOI18N
    private final static String SQ_BRACKET_LEFT = "["; //NOI18N
    private final static String SQ_BRACKET_RIGHT = "]"; //NOI18N
    
    protected final static String GCONF_PATH = "/usr/bin/gconftool-2"; //NOI18N
    private final static String GCONF_ARGUMENT_LIST_RECURSIVELY = " -R "; //NOI18N
    private final static String GCONF_NODE_PROXY = "/system/proxy"; //NOI18N
    private final static String GCONF_NODE_HTTP_PROXY = "/system/http_proxy"; //NOI18N
    private final static String GCONF_KEY_MODE = "mode"; //NOI18N
    private final static String GCONF_KEY_PAC_URL = "autoconfig_url"; //NOI18N
    private final static String GCONF_KEY_HTTP_ALL = "use_http_proxy"; //NOI18N
    private final static String GCONF_KEY_HTTP_HOST = "host"; //NOI18N
    private final static String GCONF_KEY_HTTP_PORT = "port"; //NOI18N
    private final static String GCONF_KEY_HTTPS_HOST = "secure_host"; //NOI18N
    private final static String GCONF_KEY_HTTPS_PORT = "secure_port"; //NOI18N
    private final static String GCONF_KEY_SOCKS_HOST = "socks_host"; //NOI18N
    private final static String GCONF_KEY_SOCKS_PORT = "socks_port"; //NOI18N
    private final static String GCONF_KEY_IGNORE_HOSTS = "ignore_hosts"; //NOI18N
    private final static String GCONF_VALUE_NONE = "none"; //NOI18N
    private final static String GCONF_VALUE_AUTO = "auto"; //NOI18N
    private final static String GCONF_VALUE_MANUAL = "manual"; //NOI18N

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
