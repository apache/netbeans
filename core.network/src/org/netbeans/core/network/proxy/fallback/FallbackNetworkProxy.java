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
    
    private final static Logger LOGGER = Logger.getLogger(FallbackNetworkProxy.class.getName());

    private final static String AT = "@"; //NOI18N
    private final static String COMMA = ","; //NOI18N
    private final static String SLASH = "/"; //NOI18N
    private final static String PROTOCOL_PREXIF_SEPARATOR = "://"; //NOI18N
    private final static String EMPTY_STRING = ""; //NOI18N
    
    private final static String HTTP_PROXY_SYS_PROPERTY = "http_proxy"; //NOI18N
    private final static String HTTPS_PROXY_SYS_PROPERTY = "https_proxy"; //NOI18N
    private final static String SOCKS_PROXY_SYS_PROPERTY = "socks_proxy"; //NOI18N
    private final static String NO_PROXY_SYS_PROPERTY = "no_proxy"; //NOI18N
    
    private final static String DEFAULT_NO_PROXY_HOSTS = NbBundle.getMessage(FallbackNetworkProxy.class, "DefaulNoProxyHosts");

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
