/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
