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
                String pacFileUrl = pacFilePointer.getString(0L, true);
                
                LOGGER.log(Level.INFO, "Windows system proxy resolver: auto - PAC ({0})", pacFileUrl); //NOI18N                
                return new NetworkProxySettings(pacFileUrl);
            }

            Pointer proxyPointer = prxCnf.proxy;
            Pointer proxyBypassPointer = prxCnf.proxyBypass;
            if (proxyPointer != null) {
                String proxyString = proxyPointer.getString(0L, true);
                
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
                    String proxyBypass = proxyBypassPointer.getString(0L, true);
                    
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
