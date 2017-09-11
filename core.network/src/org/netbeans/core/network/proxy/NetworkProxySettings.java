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
    
    private final static Logger LOGGER = Logger.getLogger(NetworkProxySettings.class.getName());
    private final static String COLON = ":"; //NOI18N
    private final static String SLASH = "/"; //NOI18N
    private final static String EMPTY_STRING = ""; //NOI18N
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
                string = string.substring(string.length() - 1, string.length());
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
