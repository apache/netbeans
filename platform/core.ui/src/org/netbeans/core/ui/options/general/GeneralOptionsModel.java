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

package org.netbeans.core.ui.options.general;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.core.ProxySettings;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

class GeneralOptionsModel {
    
    enum TestingStatus {
        OK,
        FAILED,
        WAITING,
        NOT_TESTED
    }
    
    private static final Logger LOGGER = Logger.getLogger(GeneralOptionsModel.class.getName()); 
    
    private static final String NON_PROXY_HOSTS_DELIMITER = "|"; //NOI18N
    
    private static final RequestProcessor rp = new RequestProcessor(GeneralOptionsModel.class);
    
    private static Preferences getProxyPreferences () {
        return NbPreferences.root ().node ("org/netbeans/core");
    }
    
    boolean getUsageStatistics () {
        String key = System.getProperty("nb.show.statistics.ui");
        if (key != null) {
            return getProxyPreferences ().getBoolean(key, Boolean.FALSE);
        } else {
            return false;
        }
    }

    void setUsageStatistics (boolean use) {
        String key = System.getProperty("nb.show.statistics.ui");
        if ((key != null) && (use != getUsageStatistics())) {
            getProxyPreferences  ().putBoolean(key, use);
        }
    }
    
    int getProxyType () {
        return getProxyPreferences ().getInt (ProxySettings.PROXY_TYPE, ProxySettings.AUTO_DETECT_PROXY);
    }
    
    void setProxyType (int proxyType) {
        if (proxyType != getProxyType ()) {
            if (ProxySettings.AUTO_DETECT_PROXY == proxyType) {
                getProxyPreferences  ().putInt (ProxySettings.PROXY_TYPE, usePAC() ? ProxySettings.AUTO_DETECT_PAC : ProxySettings.AUTO_DETECT_PROXY);
            } else {
                getProxyPreferences  ().putInt (ProxySettings.PROXY_TYPE, proxyType);
            }
        }
    }
    
    String getHttpProxyHost () {
        return ProxySettings.getHttpHost ();
    }
    
    void setHttpProxyHost (String proxyHost) {
        if (!proxyHost.equals(getHttpProxyHost ())) {
            getProxyPreferences ().put (ProxySettings.PROXY_HTTP_HOST, proxyHost);
        }
    }
    
    String getHttpProxyPort () {
        return ProxySettings.getHttpPort ();
    }
    
    void setHttpProxyPort (String proxyPort) {
        if (! proxyPort.equals (getHttpProxyPort())) {
            getProxyPreferences().put(ProxySettings.PROXY_HTTP_PORT, validatePort (proxyPort) ? proxyPort : "");
        }
    }
    
    String getHttpsProxyHost () {
        return ProxySettings.getHttpsHost ();
    }
    
    void setHttpsProxyHost (String proxyHost) {
        if (!proxyHost.equals(getHttpsProxyHost ())) {
            getProxyPreferences ().put (ProxySettings.PROXY_HTTPS_HOST, proxyHost);
        }
    }
    
    String getHttpsProxyPort () {
        return ProxySettings.getHttpsPort ();
    }
    
    void setHttpsProxyPort (String proxyPort) {
        if (! proxyPort.equals (getHttpsProxyPort())) {
            getProxyPreferences().put(ProxySettings.PROXY_HTTPS_PORT, validatePort (proxyPort) ? proxyPort : "");
        }
    }
    
    String getSocksHost () {
        return ProxySettings.getSocksHost ();
    }
    
    void setSocksHost (String socksHost) {
        if (! socksHost.equals (getSocksHost())) {
            getProxyPreferences ().put (ProxySettings.PROXY_SOCKS_HOST, socksHost);
        }
    }
    
    String getSocksPort () {
        return ProxySettings.getSocksPort ();
    }
    
    void setSocksPort (String socksPort) {
        if (! socksPort.equals (getSocksPort())) {
            getProxyPreferences ().put (ProxySettings.PROXY_SOCKS_PORT, validatePort (socksPort) ? socksPort : "");
        }
    }
    
    String getOriginalHttpsHost () {
        return getProxyPreferences ().get (ProxySettings.PROXY_HTTPS_HOST, "");
    }
    
    String getOriginalHttpsPort () {
        return getProxyPreferences ().get (ProxySettings.PROXY_HTTPS_PORT, "");
    }
    
    String getOriginalSocksHost () {
        return getProxyPreferences ().get (ProxySettings.PROXY_SOCKS_HOST, "");
    }
    
    String getOriginalSocksPort () {
        return getProxyPreferences ().get (ProxySettings.PROXY_SOCKS_PORT, "");
    }
    
    String getNonProxyHosts () {
        return code2view (ProxySettings.getNonProxyHosts ());
    }
    
    void setNonProxyHosts (String nonProxy) {
        if (!nonProxy.equals(getNonProxyHosts())) {
            getProxyPreferences ().put (ProxySettings.NOT_PROXY_HOSTS, view2code (nonProxy));
        }
    }
    
    boolean useProxyAuthentication () {
        return ProxySettings.useAuthentication ();
    }
    
    void setUseProxyAuthentication (boolean use) {
        if (use != useProxyAuthentication()) {
            getProxyPreferences ().putBoolean (ProxySettings.USE_PROXY_AUTHENTICATION, use);
        }
    }
    
    boolean useProxyAllProtocols () {
        return ProxySettings.useProxyAllProtocols ();
    }
    
    void setUseProxyAllProtocols (boolean use) {
        if (use != useProxyAllProtocols ()) {
            getProxyPreferences ().putBoolean (ProxySettings.USE_PROXY_ALL_PROTOCOLS, use);
        }
    }
    
    String getProxyAuthenticationUsername () {
        return ProxySettings.getAuthenticationUsername ();
    }

    void setAuthenticationUsername (String username) {
        getProxyPreferences ().put (ProxySettings.PROXY_AUTHENTICATION_USERNAME, username);
    }
    
    char [] getProxyAuthenticationPassword () {
        return ProxySettings.getAuthenticationPassword ();
    }
    
    void setAuthenticationPassword(char [] password) {
        ProxySettings.setAuthenticationPassword(password);
    }
    
    static boolean usePAC() {
        String pacUrl = getProxyPreferences().get(ProxySettings.SYSTEM_PAC, ""); // NOI18N
        return pacUrl != null && pacUrl.length() > 0;
    }
    
    static void testConnection(final GeneralOptionsPanel panel, final int proxyType, 
           final String proxyHost, final String proxyPortString, final String nonProxyHosts){
        rp.post(new Runnable() {

            @Override
            public void run() {
                testProxy(panel, proxyType, proxyHost, proxyPortString, nonProxyHosts);
            }
        });
    }    
        
    // private helper methods ..................................................
    
    private static void testProxy(GeneralOptionsPanel panel, int proxyType,
            String proxyHost, String proxyPortString, String nonProxyHosts) {
        panel.updateTestConnectionStatus(TestingStatus.WAITING, null);
        
        TestingStatus status = TestingStatus.FAILED;
        String message = null;
        String testingUrlHost;
        URL testingUrl;
        Proxy testingProxy;             
        
        try {
            testingUrl = new URL(ProxySettings.HTTP_CONNECTION_TEST_URL);
            testingUrlHost = testingUrl.getHost();
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, "Cannot create url from string.", ex);
            panel.updateTestConnectionStatus(status, message);
            return;
        }
             
        switch(proxyType) {
            case ProxySettings.DIRECT_CONNECTION:
                testingProxy = Proxy.NO_PROXY;
                break;
            case ProxySettings.AUTO_DETECT_PROXY:
            case ProxySettings.AUTO_DETECT_PAC:
                nonProxyHosts = ProxySettings.getSystemNonProxyHosts();            
                if (isNonProxy(testingUrlHost, nonProxyHosts)) {
                    testingProxy = Proxy.NO_PROXY;
                } else {
                    String host = ProxySettings.getTestSystemHttpHost();
                    if (host == null || host.isEmpty()) {
                        testingProxy = Proxy.NO_PROXY;
                    } else {
                        int port = 0;
                        try {
                            port = Integer.valueOf(ProxySettings.getTestSystemHttpPort());
                        } catch (NumberFormatException ex) {
                            LOGGER.log(Level.INFO, "Cannot parse port number", ex); //NOI18N
                        }
                        testingProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
                    }
                }
                break;
            case ProxySettings.MANUAL_SET_PROXY:
                nonProxyHosts = view2code(nonProxyHosts);
                if (isNonProxy(testingUrl.getHost(), nonProxyHosts)) {
                    testingProxy = Proxy.NO_PROXY;
                } else {
                    try {
                        int proxyPort = Integer.valueOf(proxyPortString);
                        testingProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                    } catch (NumberFormatException ex) {
                        // shouldn't fall into this code
                        LOGGER.log(Level.INFO, "Cannot parse port number", ex);
                        status = TestingStatus.FAILED;
                        message = NbBundle.getMessage(GeneralOptionsModel.class, "LBL_GeneralOptionsPanel_PortError");
                        panel.updateTestConnectionStatus(status, message);
                        return;
                    }                    
                }
                break;
            case ProxySettings.MANUAL_SET_PAC:
                // Never should get here, user cannot set up PAC manualy from IDE
            default:
                testingProxy = Proxy.NO_PROXY;
        }        
            
        try {
            status = testHttpConnection(testingUrl, testingProxy) ? TestingStatus.OK : TestingStatus.FAILED;
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Cannot connect via http protocol.", ex); //NOI18N
            message = ex.getLocalizedMessage();
        }
        
        panel.updateTestConnectionStatus(status, message);
    }        
    
    private static boolean testHttpConnection(URL url, Proxy proxy) throws IOException{
        boolean result = false;
        
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection(proxy);
        // Timeout shorten to 5s
        httpConnection.setConnectTimeout(5000);
        httpConnection.connect();

        if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK || 
                httpConnection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
            result = true;
        }

        httpConnection.disconnect();
        
        return result;
    }
    
    // Simplified to use only with supposed netbeans.org host
    private static boolean isNonProxy(String host, String nonProxyHosts) {
        boolean isNonProxy = false;
        
        if (host != null && nonProxyHosts != null) {
            StringTokenizer st = new StringTokenizer(nonProxyHosts, NON_PROXY_HOSTS_DELIMITER, false);
            while (st.hasMoreTokens()) {
                if (st.nextToken().equals(host)) {
                    isNonProxy = true;
                    break;
                }
            }            
        }
            
        return isNonProxy;
    }

    private static boolean validatePort (String port) {
        if (port.trim ().length () == 0) return true;
        
        boolean ok = false;
        try {
            Integer.parseInt (port);
            ok = true;
        } catch (NumberFormatException nfe) {
            assert false : nfe;
        }
        return ok;
    }
    
    private static String code2view (String code) {
        return code == null ? code : code.replace ("|", ", ");
    }
    
    private static String view2code (String view) {
        return view == null ? view : view.replace (", ", "|");
    }
}
