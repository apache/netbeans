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
package org.netbeans.core.network.proxy;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Immutable class representing all network proxy settings.
 * 
 * Holds all proxy settings values if they were successfully retrieved
 * or tells that they were not retrieved well.
 * 
 * @author lfischme
 */
public final class NetworkProxySettings {

    public static enum ProxyMode {
        DIRECT,
        AUTO,
        MANUAL
    }        
    
    private static final Logger LOGGER = Logger.getLogger(NetworkProxySettings.class.getName());
    private static final String COLON = ":"; //NOI18N
    private static final String SLASH = "/"; //NOI18N
    private static final String EMPTY_STRING = ""; //NOI18N
    private final boolean resolved;
    private final ProxyMode proxyMode;
    private final String httpProxyHost;
    private final String httpProxyPort;
    private final String httpsProxyHost;
    private final String httpsProxyPort;
    private final String socksProxyHost;
    private final String socksProxyPort;
    private final String pacFileUrl;
    private final String[] noProxyHosts;

    public NetworkProxySettings() {
        this.resolved = true;
        this.proxyMode = ProxyMode.DIRECT;
        this.pacFileUrl = null;
        this.httpProxyHost = null;
        this.httpProxyPort = null;
        this.httpsProxyHost = null;
        this.httpsProxyPort = null;
        this.socksProxyHost = null;
        this.socksProxyPort = null;
        this.noProxyHosts = new String[0];
    }
    
    public NetworkProxySettings(String httpProxy, String[] noProxyHosts) {
        String httpProxyHostChecked = getHost(httpProxy);
        String httpProxyPortChecked = getPort(httpProxy);
        
        this.resolved = true;
        this.proxyMode = ProxyMode.MANUAL;
        this.pacFileUrl = null;       
        this.httpProxyHost = httpProxyHostChecked;
        this.httpProxyPort = httpProxyPortChecked;
        this.httpsProxyHost = httpProxyHostChecked;
        this.httpsProxyPort = httpProxyPortChecked;
        this.socksProxyHost = httpProxyHostChecked;
        this.socksProxyPort = httpProxyPortChecked;
        this.noProxyHosts = checkArray(noProxyHosts);
    }

    public NetworkProxySettings(String httpProxy, String httpsProxy, String socksProxy, String[] noProxyHosts) {
        this.resolved = true;
        this.proxyMode = ProxyMode.MANUAL;
        this.pacFileUrl = null;
        this.httpProxyHost = getHost(httpProxy);
        this.httpProxyPort = getPort(httpProxy);
        this.httpsProxyHost = getHost(httpsProxy);
        this.httpsProxyPort = getPort(httpsProxy);
        this.socksProxyHost = getHost(socksProxy);
        this.socksProxyPort = getPort(socksProxy);
        this.noProxyHosts = checkArray(noProxyHosts);
    }

    public NetworkProxySettings(String httpProxyHost, String httpProxyPort, String[] noProxyHosts) {
        String httpProxyHostChecked = checkNull(httpProxyHost);
        String httpProxyPortChecked = checkNumber(httpProxyPort);
        
        this.resolved = true;
        this.proxyMode = ProxyMode.MANUAL;
        this.pacFileUrl = null;
        this.httpProxyHost = httpProxyHostChecked;
        this.httpProxyPort = httpProxyPortChecked;
        this.httpsProxyHost = httpProxyHostChecked;
        this.httpsProxyPort = httpProxyPortChecked;
        this.socksProxyHost = httpProxyHostChecked;
        this.socksProxyPort = httpProxyPortChecked;
        this.noProxyHosts = checkArray(noProxyHosts);
    }

    public NetworkProxySettings(String httpProxyHost, String httpProxyPort,
            String httpsProxyHost, String httpsProxyPort,
            String socksProxyHost, String socksProxyPort, String[] noProxyHosts) {
        this.resolved = true;
        this.proxyMode = ProxyMode.MANUAL;
        this.pacFileUrl = null;
        this.httpProxyHost = checkNull(httpProxyHost);
        this.httpProxyPort = checkNumber(httpProxyPort);
        this.httpsProxyHost = checkNull(httpsProxyHost);
        this.httpsProxyPort = checkNumber(httpsProxyPort);
        this.socksProxyHost = checkNull(socksProxyHost);
        this.socksProxyPort = checkNumber(socksProxyPort);
        this.noProxyHosts = checkArray(noProxyHosts);
    }

    public NetworkProxySettings(String pacFileUrl) {
        this.resolved = true;
        this.proxyMode = ProxyMode.AUTO;
        this.pacFileUrl = checkNull(pacFileUrl);
        this.httpProxyHost = null;
        this.httpProxyPort = null;
        this.httpsProxyHost = null;
        this.httpsProxyPort = null;
        this.socksProxyHost = null;
        this.socksProxyPort = null;
        this.noProxyHosts = new String[0];
    }

    public NetworkProxySettings(boolean resolved) {
        this.resolved = resolved;
        this.proxyMode = ProxyMode.DIRECT;
        this.pacFileUrl = null;
        this.httpProxyHost = null;
        this.httpProxyPort = null;
        this.httpsProxyHost = null;
        this.httpsProxyPort = null;
        this.socksProxyHost = null;
        this.socksProxyPort = null;
        this.noProxyHosts = new String[0];
    }
    
    

    private String getHost(String string) {
        if (string == null) {
            return EMPTY_STRING;
        } else {
            // the proxy string may possibly contain protocol part - strip it.
            if (string.contains("://")) { // NOI18N
                string = string.substring(string.indexOf("://") + 3); // NOI18N
            }
            if (string.contains(COLON)) {
                return string.substring(0, string.lastIndexOf(COLON));
            } else {
                return string;
            }
        }
    }

    private String getPort(String string) {
        if (string == null) {
            return EMPTY_STRING;
        } else {
            if (string.endsWith(SLASH)) {
                string = string.substring(string.length() - 1);
            }
            if (string.contains(COLON)) {
                return string.substring(string.lastIndexOf(COLON) + 1);
            } else {
                return EMPTY_STRING;
            }
        }
    }

    private String checkNull(String string) {
        return string == null ? EMPTY_STRING : string;
    }

    private String checkNumber(String string) {
        if (string != null) {
            try {
                Integer.parseInt(string);
                return string;
            } catch (NumberFormatException nfe) {
                LOGGER.log(Level.SEVERE, "Cannot parse number {0}", string); //NOI18N
            }
        }

        return EMPTY_STRING;
    }

    private String[] checkArray(String[] array) {
        return array == null ? new String[0] : array;
    }

    public boolean isResolved() {
        return resolved;
    }

    public ProxyMode getProxyMode() {
        return proxyMode;
    }

    public String getHttpProxyHost() {
        return httpProxyHost;
    }

    public String getHttpProxyPort() {
        return httpProxyPort;
    }

    public String getHttpsProxyHost() {
        return httpsProxyHost;
    }

    public String getHttpsProxyPort() {
        return httpsProxyPort;
    }

    public String getSocksProxyHost() {
        return socksProxyHost;
    }

    public String getSocksProxyPort() {
        return socksProxyPort;
    }

    public String getPacFileUrl() {
        return pacFileUrl;
    }

    public String[] getNoProxyHosts() {
        return noProxyHosts;
    }
}
