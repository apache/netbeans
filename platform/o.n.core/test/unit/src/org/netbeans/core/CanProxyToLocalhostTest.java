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
package org.netbeans.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;



/**
 * Check whether we can proxy to localhost.
 */
public class CanProxyToLocalhostTest extends NbTestCase {
    
    private static final MyPS MY_PS;

    static {
        MY_PS = new MyPS();
        ProxySelector.setDefault(MY_PS);
    }

    private static final String USER_PROXY_HOST = "my.webcache";
    private static final int USER_PROXY_PORT = 8080;
    private ProxySelector selector;
    private static URI TO_LOCALHOST;
    private static URI TO_NB;

    public CanProxyToLocalhostTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MY_PS.called = 0;
        selector = Lookup.getDefault().lookup(ProxySelector.class);
        if (selector != null) {
            // install java.net.ProxySelector
            ProxySelector.setDefault(selector);
        }
        TO_LOCALHOST = new URI("http://localhost");
        TO_NB = new URI("http://netbeans.org");
    }

    public void testNoProxyForLocalhost() {
        Locale.setDefault(Locale.US);
        String staticNonProxyHosts = NbBundle.getMessage(ProxySettings.class, "StaticNonProxyHosts"); // NOI18N
        assertNotNull(staticNonProxyHosts);
        assertEquals("The default non proxy hosts", "localhost|127.0.0.1", staticNonProxyHosts);
        assertEquals("Connect TO_LOCALHOST DIRECT.", "DIRECT", selector.select(TO_LOCALHOST).get(0).toString());
    }

    public void testProxyForLocalhost() {
        Locale.setDefault(new Locale("te", "ST"));
        String staticNonProxyHosts = NbBundle.getMessage(ProxySettings.class, "StaticNonProxyHosts"); // NOI18N
        assertNotNull(staticNonProxyHosts);
        assertEquals("The default non proxy hosts", "", staticNonProxyHosts);
        assertEquals("Connect TO_LOCALHOST provided by MyPS", "HTTP @ my.webcache/<unresolved>:8080", selector.select(TO_LOCALHOST).get(0).toString());
        assertEquals("One call to my ps", 1, MY_PS.called);
    }

    public void testAlwaysProxyForNonLocalhost() {
        Locale.setDefault(Locale.US);
        assertEquals("Connect TO_NB provided by MyPS", "HTTP @ my.webcache/<unresolved>:8080", selector.select(TO_NB).get(0).toString());
        assertEquals("One call to my ps", 1, MY_PS.called);
    }

    private static class MyPS extends ProxySelector {

        int called;

        @Override
        public List<Proxy> select(URI uri) {
            called++;
            return Collections.singletonList(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(USER_PROXY_HOST, USER_PROXY_PORT)));
        }

        @Override
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        }
    }
}
