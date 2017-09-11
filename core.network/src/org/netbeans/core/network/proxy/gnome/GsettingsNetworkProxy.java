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
public class GsettingsNetworkProxy {
    
    private final static Logger LOGGER = Logger.getLogger(GsettingsNetworkProxy.class.getName());
    
    private final static String EMPTY_STRING = ""; //NOI18N
    private final static String SPACE = " "; //NOI18N
    private final static String DOT = "."; //NOI18N
    private final static String COLON = ","; //NOI18N
    private final static String SINGLE_QUOTE = "'"; //NOI18N
    private final static String SQ_BRACKET_LEFT = "["; //NOI18N
    private final static String SQ_BRACKET_RIGHT = "]"; //NOI18N
    
    protected final static String GSETTINGS_PATH = "/usr/bin/gsettings"; //NOI18N
    private final static String GSETTINGS_ARGUMENT_LIST_RECURSIVELY = " list-recursively "; //NOI18N
    private final static String GSETTINGS_PROXY_SCHEMA = "org.gnome.system.proxy"; //NOI18N
    
    private final static String GSETTINGS_KEY_MODE = "org.gnome.system.proxy.mode"; //NOI18N
    private final static String GSETTINGS_KEY_PAC_URL = "org.gnome.system.proxy.autoconfig-url"; //NOI18N
    private final static String GSETTINGS_KEY_HTTP_ALL = "org.gnome.system.proxy.http.use-same-proxy"; //NOI18N
    private final static String GSETTINGS_KEY_HTTP_HOST = "org.gnome.system.proxy.http.host"; //NOI18N
    private final static String GSETTINGS_KEY_HTTP_PORT = "org.gnome.system.proxy.http.port"; //NOI18N
    private final static String GSETTINGS_KEY_HTTPS_HOST = "org.gnome.system.proxy.https.host"; //NOI18N
    private final static String GSETTINGS_KEY_HTTPS_PORT = "org.gnome.system.proxy.https.port"; //NOI18N
    private final static String GSETTINGS_KEY_SOCKS_HOST = "org.gnome.system.proxy.socks.host"; //NOI18N
    private final static String GSETTINGS_KEY_SOCKS_PORT = "org.gnome.system.proxy.socks.port"; //NOI18N
    private final static String GSETTINGS_KEY_IGNORE_HOSTS = "org.gnome.system.proxy.ignore-hosts"; //NOI18N       
    private final static String GSETTINGS_VALUE_NONE = "none"; //NOI18N
    private final static String GSETTINGS_VALUE_AUTO = "auto"; //NOI18N
    private final static String GSETTINGS_VALUE_MANUAL = "manual"; //NOI18N

    /**
     * Returns network proxy settings - retrieved via GSettings (preferred).
     * 
     * @return network proxy settings via GSettings.
     */
    protected static NetworkProxySettings getNetworkProxySettings() {
        LOGGER.log(Level.FINE, "GSettings system proxy resolver started."); //NOI18N
        Map<String, String> proxyProperties = getGsettingsMap();
                        
        String proxyMode = proxyProperties.get(GSETTINGS_KEY_MODE);
        if (proxyMode == null) {
            LOGGER.log(Level.SEVERE, "GSettings proxy mode is null.");
            return new NetworkProxySettings(false);
        }        
        
        if (proxyMode.equals(GSETTINGS_VALUE_NONE)) {
            LOGGER.log(Level.INFO, "GSettings system proxy resolver: direct connection"); //NOI18N
            return new NetworkProxySettings();
        }
        
        if (proxyMode.equals(GSETTINGS_VALUE_AUTO)) {
            String pacUrl = proxyProperties.get(GSETTINGS_KEY_PAC_URL);
            
            LOGGER.log(Level.INFO, "GSettings system proxy resolver: auto - PAC ({0})", pacUrl); //NOI18N
            
            if (pacUrl != null) {
                return new NetworkProxySettings(pacUrl);             
            } else {
                return new NetworkProxySettings("");
            }
        }
        
        if (proxyMode.equals(GSETTINGS_VALUE_MANUAL)) {            
            String httpProxyAll = proxyProperties.get(GSETTINGS_KEY_HTTP_ALL);
            String httpProxyHost = proxyProperties.get(GSETTINGS_KEY_HTTP_HOST);
            String httpProxyPort = proxyProperties.get(GSETTINGS_KEY_HTTP_PORT);
            String noProxyHosts = proxyProperties.get(GSETTINGS_KEY_IGNORE_HOSTS);
            
            LOGGER.log(Level.INFO, "GSettings system proxy resolver: manual - http for all ({0})", httpProxyAll); //NOI18N
            LOGGER.log(Level.INFO, "GSettings system proxy resolver: manual - http host ({0})", httpProxyHost); //NOI18N
            LOGGER.log(Level.INFO, "GSettings system proxy resolver: manual - http port ({0})", httpProxyPort); //NOI18N
            LOGGER.log(Level.INFO, "GSettings system proxy resolver: manual - ho proxy hosts ({0})", noProxyHosts); //NOI18N
            
            if (httpProxyAll != null && Boolean.parseBoolean(httpProxyAll)) {
                return new NetworkProxySettings(httpProxyHost, httpProxyPort, getNoProxyHosts(noProxyHosts));
            } else {
                String httpsProxyHost = proxyProperties.get(GSETTINGS_KEY_HTTPS_HOST);
                String httpsProxyPort = proxyProperties.get(GSETTINGS_KEY_HTTPS_PORT);
                String socksProxyHost = proxyProperties.get(GSETTINGS_KEY_SOCKS_HOST);
                String socksProxyPort = proxyProperties.get(GSETTINGS_KEY_SOCKS_PORT);
                
                LOGGER.log(Level.INFO, "GSettings system proxy resolver: manual - https host ({0})", httpsProxyHost); //NOI18N
                LOGGER.log(Level.INFO, "GSettings system proxy resolver: manual - https port ({0})", httpsProxyPort); //NOI18N
                LOGGER.log(Level.INFO, "GSettings system proxy resolver: manual - socks host ({0})", socksProxyHost); //NOI18N
                LOGGER.log(Level.INFO, "GSettings system proxy resolver: manual - socks port ({0})", socksProxyPort); //NOI18N
                
                return new NetworkProxySettings(httpProxyHost, httpProxyPort, 
                        httpsProxyHost, httpsProxyPort, 
                        socksProxyHost, socksProxyPort, getNoProxyHosts(noProxyHosts));
            }
        }
        
        return new NetworkProxySettings(false);
    }    
    
    /**
     * Checks if GSettings returns suitable response
     * (On Solaris 11 GSettings is available, but returns empty list)
     * 
     * @return true if GSettings returns suitable response
     */
    protected static boolean isGsettingsValid() {
        String command = GSETTINGS_PATH + GSETTINGS_ARGUMENT_LIST_RECURSIVELY + GSETTINGS_PROXY_SCHEMA;
        
        try {
            BufferedReader reader = executeCommand(command);
            if (reader.ready()) {
                return true;
            }
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "Cannot read line: " + command, ioe); //NOI18N
        }
        
        LOGGER.log(Level.INFO, "GSettings return empty list"); //NOI18N
        return false;
    }
    
    /**
     * Returns map of properties retrieved from GSettings.
     * 
     * Executes the command "/usr/bin/gsettings list-recursively org.gnome.system.proxy".
     * 
     * @return Map of properties retrieved from GSettings.
     */
    private static Map<String, String> getGsettingsMap() {
        Map<String, String> map = new HashMap<String, String>();
        
        String command = GSETTINGS_PATH + GSETTINGS_ARGUMENT_LIST_RECURSIVELY + GSETTINGS_PROXY_SCHEMA;
        
        try {
            BufferedReader reader = executeCommand(command);
            String line = reader.readLine();
            while (line != null) {
                String key = getKey(line).toLowerCase();                                                    
                if (key != null && !key.isEmpty()) {
                    String value = getValue(line);
                    map.put(key, value);
                }                   
                line = reader.readLine();
            }
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "Cannot read line: " + command, ioe); //NOI18N
        }

        return map;
    }    
    
    /**
     * Returns the key for one line response from GSettings.
     * 
     * @param line Line from GSettings response.
     * @return The key for one line response from GSettings.
     */
    private static String getKey(String line) {
        String[] splittedLine = line.split(SPACE);
        
        if (splittedLine.length >= 2) {
            return splittedLine[0] + DOT + splittedLine[1];
        } else {
            return null; 
        }
    }

    /**
     * Returns the value for one line response from GSettings.
     * 
     * @param line Line from GSettings response.
     * @return The value for one line response from GSettings.
     */
    private static String getValue(String line) {
        String[] splittedLine = line.split(SPACE);
        
        if (splittedLine.length > 2) {
            StringBuilder value = new StringBuilder();
            for (int i = 2; i < splittedLine.length; i++) {
                value.append(splittedLine[i]);
            }
            return value.toString().replaceAll(SINGLE_QUOTE, EMPTY_STRING);
        } else {
            return null;
        }
    }
    
    /**
     * Returns array of Strings of no proxy hosts.
     * 
     * The value responding to "ignore_hosts" key.
     * 
     * Parses the value returned from GSettings.
     * Usually ['host1', 'host2', 'host3']
     * But value is in form [host1,host2,host3]
     * 
     * @param noProxyHostsString The value returned from GSettingsc.
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
