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

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.netbeans.core.ProxySettings;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.core.network.proxy.fallback.FallbackNetworkProxy;
import org.netbeans.core.network.proxy.gnome.GnomeNetworkProxy;
import org.netbeans.core.network.proxy.kde.KdeNetworkProxy;
import org.netbeans.core.network.proxy.mac.MacNetworkProxy;
import org.netbeans.core.network.proxy.windows.WindowsNetworkProxy;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * This class allows user to reload system network proxy settings.
 * 
 * @author lfischme
 */
@ServiceProvider(service = ProxySettings.Reloader.class, position = 1000)
public class NetworkProxyReloader extends ProxySettings.Reloader {
    
    private static final Logger LOGGER = Logger.getLogger(NetworkProxyReloader.class.getName());
    
    private static final String EMPTY_STRING = ""; //NOI18N
    private static final String NON_PROXY_HOSTS_DELIMITER = "|"; //NOI18N
    
    private static final String GNOME = "gnome"; //NOI18N
    private static final String KDE = "kde"; //NOI18N
    private static final String RUNNING_ENV_SYS_PROPERTY = "netbeans.running.environment"; //NOI18N
    
    private static final NetworkProxyResolver NETWORK_PROXY_RESOLVER = getNetworkProxyResolver();
    private static final NetworkProxyResolver FALLBACK_NETWORK_PROXY_RESOLVER = getFallbackProxyResolver();
    
    /**
     * Reloads system proxy network settings.
     * 
     * The first it tries to retrieve proxy settings directly from system,
     * if it is unsuccessful it tries fallback (from environment property http_proxy etc.).
     */
    public static void reloadNetworkProxy() {        
        LOGGER.log(Level.FINE, "System network proxy reloading started."); //NOI18N
        NetworkProxySettings networkProxySettings = NETWORK_PROXY_RESOLVER.getNetworkProxySettings();
        
        if (!networkProxySettings.isResolved()) {
            LOGGER.log(Level.INFO, "System network proxy reloading failed! Trying fallback resolver."); //NOI18N
            NetworkProxySettings fallbackNetworkProxySettings = FALLBACK_NETWORK_PROXY_RESOLVER.getNetworkProxySettings();
            if (fallbackNetworkProxySettings.isResolved()) {
                LOGGER.log(Level.INFO, "System network proxy reloading succeeded. Fallback provider was successful."); //NOI18N
                networkProxySettings = fallbackNetworkProxySettings;
            } else {
                LOGGER.log(Level.INFO, "System network proxy reloading failed! Fallback provider was unsuccessful."); //NOI18N
            }
        } else {
            LOGGER.log(Level.INFO, "System network proxy reloading succeeded."); //NOI18N
        }
                       
        switch (networkProxySettings.getProxyMode()) {
            case AUTO:
                final ProxyAutoConfig pacForTest = ProxyAutoConfig.get(networkProxySettings.getPacFileUrl());
                List<Proxy> testHttpProxy = null;
                final String testHttpProxyHost;
                final String testHttpProxyPort;

                try {
                    testHttpProxy = pacForTest.findProxyForURL(new URI(ProxySettings.HTTP_CONNECTION_TEST_URL));
                } catch (URISyntaxException ex) {
                    LOGGER.log(Level.WARNING, "Cannot create URI from: " + ProxySettings.HTTP_CONNECTION_TEST_URL, ex); //NOI18N
                }

                if (testHttpProxy != null && !testHttpProxy.isEmpty() && testHttpProxy.get(0).address() != null) {
                    testHttpProxyHost = ((InetSocketAddress) testHttpProxy.get(0).address()).getHostName();
                    testHttpProxyPort = Integer.toString(((InetSocketAddress) testHttpProxy.get(0).address()).getPort());
                } else {
                    testHttpProxyHost = EMPTY_STRING;
                    testHttpProxyPort = Integer.toString(0);
                }
                        
                LOGGER.log(Level.INFO, "System network proxy - mode: auto"); //NOI18N
                LOGGER.log(Level.INFO, "System network proxy - pac url: {0}", networkProxySettings.getPacFileUrl()); //NOI18N
                LOGGER.log(Level.INFO, "System network proxy TEST - http host: {0}", testHttpProxyHost); //NOI18N
                LOGGER.log(Level.INFO, "System network proxy TEST - http port: {0}", testHttpProxyPort); //NOI18N
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_HTTP_HOST);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_HTTP_PORT);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_HTTPS_HOST);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_HTTPS_PORT);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_SOCKS_HOST);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_SOCKS_PORT);
                getPreferences().remove(ProxySettings.SYSTEM_NON_PROXY_HOSTS);
                getPreferences().put(ProxySettings.TEST_SYSTEM_PROXY_HTTP_HOST, testHttpProxyHost);
                getPreferences().put(ProxySettings.TEST_SYSTEM_PROXY_HTTP_PORT, testHttpProxyPort);
                getPreferences().put(ProxySettings.SYSTEM_PAC, networkProxySettings.getPacFileUrl());
                break;
            case MANUAL:
                LOGGER.log(Level.INFO, "System network proxy - mode: manual"); //NOI18N
                LOGGER.log(Level.INFO, "System network proxy - http host: {0}", networkProxySettings.getHttpProxyHost()); //NOI18N
                LOGGER.log(Level.INFO, "System network proxy - http port: {0}", networkProxySettings.getHttpProxyPort()); //NOI18N
                LOGGER.log(Level.INFO, "System network proxy - https host: {0}", networkProxySettings.getHttpsProxyHost()); //NOI18N
                LOGGER.log(Level.INFO, "System network proxy - https port: {0}", networkProxySettings.getHttpsProxyPort()); //NOI18N
                LOGGER.log(Level.INFO, "System network proxy - socks host: {0}", networkProxySettings.getSocksProxyHost()); //NOI18N
                LOGGER.log(Level.INFO, "System network proxy - socks port: {0}", networkProxySettings.getSocksProxyPort()); //NOI18N
                LOGGER.log(Level.INFO, "System network proxy - no proxy hosts: {0}", getStringFromArray(networkProxySettings.getNoProxyHosts())); //NOI18N
                LOGGER.log(Level.INFO, "System network proxy TEST - http host: {0}", networkProxySettings.getHttpProxyHost()); //NOI18N
                LOGGER.log(Level.INFO, "System network proxy TEST - http port: {0}", networkProxySettings.getHttpProxyPort()); //NOI18N
                getPreferences().put(ProxySettings.SYSTEM_PROXY_HTTP_HOST, networkProxySettings.getHttpProxyHost());
                getPreferences().put(ProxySettings.SYSTEM_PROXY_HTTP_PORT, networkProxySettings.getHttpProxyPort());
                getPreferences().put(ProxySettings.SYSTEM_PROXY_HTTPS_HOST, networkProxySettings.getHttpsProxyHost());
                getPreferences().put(ProxySettings.SYSTEM_PROXY_HTTPS_PORT, networkProxySettings.getHttpsProxyPort());
                getPreferences().put(ProxySettings.SYSTEM_PROXY_SOCKS_HOST, networkProxySettings.getSocksProxyHost());
                getPreferences().put(ProxySettings.SYSTEM_PROXY_SOCKS_PORT, networkProxySettings.getSocksProxyPort());
                getPreferences().put(ProxySettings.SYSTEM_NON_PROXY_HOSTS, getStringFromArray(networkProxySettings.getNoProxyHosts()));
                getPreferences().put(ProxySettings.TEST_SYSTEM_PROXY_HTTP_HOST, networkProxySettings.getHttpProxyHost());
                getPreferences().put(ProxySettings.TEST_SYSTEM_PROXY_HTTP_PORT, networkProxySettings.getHttpProxyPort());
                getPreferences().remove(ProxySettings.SYSTEM_PAC);
                break;
            case DIRECT:
                LOGGER.log(Level.INFO, "System network proxy - mode: direct"); //NOI18N
            default:
                LOGGER.log(Level.INFO, "System network proxy: fell to default (correct if direct mode went before)"); //NOI18N
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_HTTP_HOST);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_HTTP_PORT);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_HTTPS_HOST);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_HTTPS_PORT);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_SOCKS_HOST);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_SOCKS_PORT);
                getPreferences().remove(ProxySettings.SYSTEM_NON_PROXY_HOSTS);
                getPreferences().remove(ProxySettings.SYSTEM_PAC);
                getPreferences().remove(ProxySettings.TEST_SYSTEM_PROXY_HTTP_HOST);
                getPreferences().remove(ProxySettings.TEST_SYSTEM_PROXY_HTTP_PORT);
        }        
        LOGGER.log(Level.FINE, "System network proxy reloading fineshed."); //NOI18N
    }
    
    /**
     * Returns string from array of strings. Strings are separated by comma.
     * 
     * @param stringArray
     * @return String from array of strings. Strings are separated by comma.
     */
    private static String getStringFromArray(String[] stringArray) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stringArray.length; i++) {
            sb.append(stringArray[i]);
            if (i < stringArray.length - 1) {
                sb.append(NON_PROXY_HOSTS_DELIMITER);
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Returns preferences for proxy settings.
     * 
     * @return Preferences for proxy settings.
     */
    private static Preferences getPreferences() {
        return NbPreferences.forModule(ProxySettings.class);
    }
    
    /**
     * Returns proper network resolver for running environment.
     * 
     * If not suitable proxy resolver found, the fallback is used.
     * 
     * @return Proper network resolver for running environment.
     */
    private static NetworkProxyResolver getNetworkProxyResolver() {
        if (NETWORK_PROXY_RESOLVER == null) {        
            if (Utilities.isWindows()) {
                LOGGER.log(Level.INFO, "System network proxy resolver: Windows"); //NOI18N
                return new WindowsNetworkProxy();
            } 
            
            if (Utilities.isMac()) {
                LOGGER.log(Level.INFO, "System network proxy resolver: Mac"); //NOI18N
                return new MacNetworkProxy();
            }
            
            if (Utilities.isUnix()){
                String env = System.getProperty(RUNNING_ENV_SYS_PROPERTY);
                if (env != null) {
                    if (env.toLowerCase().equals(GNOME)) {
                        LOGGER.log(Level.INFO, "System network proxy resolver: Gnome"); //NOI18N
                        return new GnomeNetworkProxy();
                    }
                    
                    if (env.toLowerCase().equals(KDE)) {
                        LOGGER.log(Level.INFO, "System network proxy resolver: KDE"); //NOI18N
                        return new KdeNetworkProxy();
                    }
                }
            }
            
            LOGGER.log(Level.INFO, "System network proxy resolver: no suitable found, using fallback."); //NOI18N
            return new FallbackNetworkProxy();
        } else {
            return NETWORK_PROXY_RESOLVER;
        }   
    }
    
    /**
     * Returns fallback proxy resolver.
     * 
     * @return Fallback proxy resolver.
     */
    private static NetworkProxyResolver getFallbackProxyResolver() {
        if (FALLBACK_NETWORK_PROXY_RESOLVER == null) {
            return new FallbackNetworkProxy();
        } else {
            return FALLBACK_NETWORK_PROXY_RESOLVER;
        }
    }
    
    @Override
    public void reload() {
        NetworkProxyReloader.reloadNetworkProxy();
    }
}
