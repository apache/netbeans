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
package org.netbeans.core.network.proxy.fallback;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.network.proxy.NetworkProxyResolver;
import org.netbeans.core.network.proxy.NetworkProxySettings;
import org.openide.util.NbBundle;

/**
 * Fallback resolver tries to retrieve proxy setting from environment variables.
 *
 * It is looking for: http_proxy, https_proxy, socks_proxy and no_proxy
 * variables. It cannot resolve if PAC is set up. Also environment variables may
 * be set but in system there are not used. Fallback cannot resolve it.
 *
 * @author lfischme
 */
public class FallbackNetworkProxy implements NetworkProxyResolver {
    
    private static final Logger LOGGER = Logger.getLogger(FallbackNetworkProxy.class.getName());

    private static final String AT = "@"; //NOI18N
    private static final String COMMA = ","; //NOI18N
    private static final String SLASH = "/"; //NOI18N
    private static final String PROTOCOL_PREXIF_SEPARATOR = "://"; //NOI18N
    private static final String EMPTY_STRING = ""; //NOI18N
    
    private static final String HTTP_PROXY_SYS_PROPERTY = "http_proxy"; //NOI18N
    private static final String HTTPS_PROXY_SYS_PROPERTY = "https_proxy"; //NOI18N
    private static final String SOCKS_PROXY_SYS_PROPERTY = "socks_proxy"; //NOI18N
    private static final String NO_PROXY_SYS_PROPERTY = "no_proxy"; //NOI18N
    
    private static final String DEFAULT_NO_PROXY_HOSTS = NbBundle.getMessage(FallbackNetworkProxy.class, "DefaulNoProxyHosts");

    @Override
    public NetworkProxySettings getNetworkProxySettings() {
        LOGGER.log(Level.FINE, "Fallback system proxy resolver started."); //NOI18N
        String httpProxyRaw = System.getenv(HTTP_PROXY_SYS_PROPERTY);
        if (httpProxyRaw != null && !httpProxyRaw.isEmpty()) {
            String httpsProxyRaw = System.getenv(HTTPS_PROXY_SYS_PROPERTY);
            String socksProxyRaw = System.getenv(SOCKS_PROXY_SYS_PROPERTY);
            String noProxyRaw = System.getenv(NO_PROXY_SYS_PROPERTY);
            
            LOGGER.log(Level.INFO, "Fallback system proxy resolver: http_proxy={0}", httpProxyRaw); //NOI18N
            LOGGER.log(Level.INFO, "Fallback system proxy resolver: https_proxy={0}", httpsProxyRaw); //NOI18N
            LOGGER.log(Level.INFO, "Fallback system proxy resolver: socks_proxy={0}", socksProxyRaw); //NOI18N
            LOGGER.log(Level.INFO, "Fallback system proxy resolver: no_proxy={0}", noProxyRaw); //NOI18N
            
            String httpProxy = prepareVariable(httpProxyRaw);
            String httpsProxy = prepareVariable(httpsProxyRaw);
            String socksProxy = prepareVariable(socksProxyRaw);
            String[] noProxyHosts;
            if (noProxyRaw == null) {
                noProxyHosts = DEFAULT_NO_PROXY_HOSTS.split(COMMA);
                LOGGER.log(Level.INFO, "Fallback system proxy resolver: no proxy set to default"); //NOI18N
            } else {
                noProxyHosts = noProxyRaw.split(COMMA);
            }
            
            return new NetworkProxySettings(httpProxy, httpsProxy, socksProxy, noProxyHosts);
        }
        
        LOGGER.log(Level.INFO, "Fallback system proxy resolver: no http_proxy variable found"); //NOI18N
        return new NetworkProxySettings();
    }

    private String prepareVariable(String variable) {
        if (variable == null) {
            return EMPTY_STRING;
        }

        // remove slash at the end if present
        if (variable.endsWith(SLASH)) {
            variable = variable.substring(0, variable.length() - 1);
        }

        // remove username and password if present
        if (variable.contains(AT)) {
            variable = variable.substring(variable.lastIndexOf(AT) + 1);
        }
        
        // remove protocol prefix if presented
        if (variable.contains(PROTOCOL_PREXIF_SEPARATOR)) {
            variable = variable.substring(variable.indexOf(PROTOCOL_PREXIF_SEPARATOR) + 3);
        }

        return variable;
    }       
}
