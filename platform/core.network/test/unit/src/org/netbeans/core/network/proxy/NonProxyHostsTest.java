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

package org.netbeans.core.network.proxy;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.core.ProxySettings;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.NbPreferences;

/** Tests Detect OS nonProxyHosts settings.
 *
 * @author Jiri Rechtacek
 * @see http://www.netbeans.org/issues/show_bug.cgi?id=77053
 */
@RandomlyFails // NB-Core-Build #1247, 1248
public class NonProxyHostsTest extends NbTestCase {
    private static String SYSTEM_PROXY_HOST = "system.cache.org";
    private static String SYSTEM_PROXY_PORT = "777";
    private static String USER_PROXY_HOST = "my.webcache";
    private static String USER_PROXY_PORT = "8080";

    private Preferences proxyPreferences;
    private ProxySelector selector;
    private static URI TO_LOCALHOST;
    private static URI TO_LOCAL_DOMAIN_1;
    private static URI TO_LOCAL_DOMAIN_2;
    private static URI TO_EXTERNAL;
    private static URI SOCKS_TO_LOCALHOST;
    private static URI SOCKS_TO_LOCAL_DOMAIN_1;
    private static URI SOCKS_TO_LOCAL_DOMAIN_2;
    private static URI SOCKS_TO_EXTERNAL;

    private boolean isWaiting = false;
    
    public NonProxyHostsTest (String name) {
        super (name);
    }
    
    @Override
    protected void setUp () throws Exception {
        super.setUp ();
        System.setProperty ("netbeans.system_http_proxy", SYSTEM_PROXY_HOST + ":" + SYSTEM_PROXY_PORT);
        System.setProperty ("netbeans.system_socks_proxy", SYSTEM_PROXY_HOST + ":" + SYSTEM_PROXY_PORT);
        System.setProperty ("netbeans.system_http_non_proxy_hosts", "*.other.org");
        System.setProperty ("http.nonProxyHosts", "*.netbeans.org");
        selector = ProxySelector.getDefault ();
        proxyPreferences  = NbPreferences.root ().node ("/org/netbeans/core");
        proxyPreferences.addPreferenceChangeListener (new PreferenceChangeListener () {
            public void preferenceChange (PreferenceChangeEvent arg0) {
                isWaiting = false;
            }
        });
        proxyPreferences.put ("proxyHttpHost", USER_PROXY_HOST);
        proxyPreferences.put ("proxyHttpPort", USER_PROXY_PORT);
        proxyPreferences.put ("proxySocksHost", USER_PROXY_HOST);
        proxyPreferences.put ("proxySocksPort", USER_PROXY_PORT);
        while (isWaiting);
        isWaiting = true;
        TO_LOCALHOST = new URI ("http://localhost");
        TO_LOCAL_DOMAIN_1 = new URI ("http://core.netbeans.org");
        TO_LOCAL_DOMAIN_2 = new URI ("http://core.other.org");
        TO_EXTERNAL = new URI ("http://worldwide.net");
        
        SOCKS_TO_LOCALHOST = new URI ("socket://localhost:8041");
        SOCKS_TO_LOCAL_DOMAIN_1 = new URI ("socket://core.netbeans.org");
        SOCKS_TO_LOCAL_DOMAIN_2 = new URI ("socket://core.other.org");
        SOCKS_TO_EXTERNAL = new URI ("socket://worldwide.net");
    }
    
    public void testDirectProxySetting () {
        proxyPreferences.putInt ("proxyType", ProxySettings.DIRECT_CONNECTION);
        while (isWaiting);
        assertEquals ("Proxy type DIRECT_CONNECTION.", ProxySettings.DIRECT_CONNECTION, ProxySettings.getProxyType ());
        assertEquals ("Connect " + TO_LOCALHOST + " DIRECT.", "[DIRECT]", selector.select (TO_LOCALHOST).toString ());
        assertEquals ("Connect " + SOCKS_TO_LOCALHOST + " DIRECT.", Proxy.NO_PROXY, selector.select (SOCKS_TO_LOCALHOST).get(0));
        assertEquals ("Connect " + TO_LOCAL_DOMAIN_1 + " DIRECT.", "[DIRECT]", selector.select (TO_LOCAL_DOMAIN_1).toString ());
        assertEquals ("Connect " + SOCKS_TO_LOCAL_DOMAIN_1 + " DIRECT.", "[DIRECT]", selector.select (SOCKS_TO_LOCAL_DOMAIN_1).toString ());
        assertEquals ("Connect " + TO_LOCAL_DOMAIN_2 + " DIRECT.", "[DIRECT]", selector.select (TO_LOCAL_DOMAIN_2).toString ());
        assertEquals ("Connect " + SOCKS_TO_LOCAL_DOMAIN_2 + " DIRECT.", "[DIRECT]", selector.select (SOCKS_TO_LOCAL_DOMAIN_2).toString ());
        assertEquals ("Connect " + TO_EXTERNAL + " DIRECT.", "[DIRECT]", selector.select (TO_EXTERNAL).toString ());
        assertEquals ("Connect " + SOCKS_TO_EXTERNAL + " DIRECT.", "[DIRECT]", selector.select (SOCKS_TO_EXTERNAL).toString ());
    }
}
