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
package org.netbeans.core.network.proxy.windows;

import com.sun.jna.Pointer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.network.proxy.NetworkProxyResolver;
import org.netbeans.core.network.proxy.NetworkProxySettings;

/**
 *
 * @author lfischme
 */
public class WindowsNetworkProxy implements NetworkProxyResolver {
    
    private final static Logger LOGGER = Logger.getLogger(WindowsNetworkProxy.class.getName());
    
    private final static String HTTP_PROPERTY_NAME = "http="; //NOI18N
    private final static String HTTPS_PROPERTY_NAME = "https="; //NOI18N
    private final static String SOCKS_PROPERTY_NAME = "socks="; //NOI18N
    
    private final static String SPACE = " "; //NOI18N
    private final static String COLON = ":"; //NOI18N
    private final static String SEMI_COLON = ";"; //NOI18N
    

    @Override
    public NetworkProxySettings getNetworkProxySettings() {
        LOGGER.log(Level.FINE, "Windows system proxy resolver started."); //NOI18N
        WindowsNetworkProxyLibrary.ProxyConfig.ByReference prxCnf = new WindowsNetworkProxyLibrary.ProxyConfig.ByReference();

        boolean result = WindowsNetworkProxyLibrary.LIBRARY.WinHttpGetIEProxyConfigForCurrentUser(prxCnf);

        if (result) {
            LOGGER.log(Level.FINE, "Windows system proxy resolver successfully retrieved proxy settings."); //NOI18N
            
            if (prxCnf.autoDetect) {
                LOGGER.log(Level.INFO, "Windows system proxy resolver: auto detect"); //NOI18N
            }

            Pointer pacFilePointer = prxCnf.pacFile;
            if (pacFilePointer != null) {
                String pacFileUrl = pacFilePointer.getWideString(0);
                
                LOGGER.log(Level.INFO, "Windows system proxy resolver: auto - PAC ({0})", pacFileUrl); //NOI18N                
                return new NetworkProxySettings(pacFileUrl);
            }

            Pointer proxyPointer = prxCnf.proxy;
            Pointer proxyBypassPointer = prxCnf.proxyBypass;
            if (proxyPointer != null) {
                String proxyString = proxyPointer.getWideString(0);
                
                LOGGER.log(Level.INFO, "Windows system proxy resolver: manual ({0})", proxyString); //NOI18N
                
                String httpProxy = null;
                String httpsProxy = null;
                String socksProxy = null;
                String[] noProxyHosts;
                
                if (proxyString != null) {
                    proxyString = proxyString.toLowerCase();
                }
                
                if (proxyString.contains(SEMI_COLON)) {
                    String[] proxies = proxyString.split(SEMI_COLON);
                    for (String singleProxy : proxies) {
                        if (singleProxy.startsWith(HTTP_PROPERTY_NAME)) {
                            httpProxy = singleProxy.substring(HTTP_PROPERTY_NAME.length());
                        } else if (singleProxy.startsWith(HTTPS_PROPERTY_NAME)) {
                            httpsProxy = singleProxy.substring(HTTPS_PROPERTY_NAME.length()); 
                        } else if (singleProxy.startsWith(SOCKS_PROPERTY_NAME)) {
                            socksProxy = singleProxy.substring(SOCKS_PROPERTY_NAME.length());
                        }
                    }
                } else {
                    if (proxyString.startsWith(HTTP_PROPERTY_NAME)) {
                        proxyString = proxyString.substring(HTTP_PROPERTY_NAME.length());
                        httpProxy = proxyString.replace(SPACE, COLON);
                    } else if (proxyString.startsWith(HTTPS_PROPERTY_NAME)) {
                        proxyString = proxyString.substring(HTTPS_PROPERTY_NAME.length());
                        httpsProxy = proxyString.replace(SPACE, COLON);
                    } else if (proxyString.startsWith(SOCKS_PROPERTY_NAME)) {
                        proxyString = proxyString.substring(SOCKS_PROPERTY_NAME.length());
                        socksProxy = proxyString.replace(SPACE, COLON);
                    } else {
                        httpProxy = proxyString;
                        httpsProxy = proxyString;                        
                    }
                }

                if (proxyBypassPointer != null) {
                    String proxyBypass = proxyBypassPointer.getWideString(0);
                    
                    LOGGER.log(Level.INFO, "Windows system proxy resolver: manual - no proxy hosts ({0})", proxyBypass); //NOI18N
                    
                    noProxyHosts = proxyBypass.split(SEMI_COLON);
                } else {
                    noProxyHosts = new String[0];
                }

                return new NetworkProxySettings(httpProxy, httpsProxy, socksProxy, noProxyHosts);
            }
            
            LOGGER.log(Level.FINE, "Windows system proxy resolver: no proxy"); //NOI18N
            return new NetworkProxySettings();
        } else {
            LOGGER.log(Level.SEVERE, "Windows system proxy resolver cannot retrieve proxy settings from Windows API!"); //NOI18N
        }

        return new NetworkProxySettings(false);
    }
}
