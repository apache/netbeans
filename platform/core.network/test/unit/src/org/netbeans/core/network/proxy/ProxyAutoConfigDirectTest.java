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
package org.netbeans.core.network.proxy;

import java.io.File;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

public class ProxyAutoConfigDirectTest extends NbTestCase {
    static {
        System.setProperty("netbeans.security.nocheck", "true");
    }
    public ProxyAutoConfigDirectTest(String name) {
        super(name);
    }
    
    public static final junit.framework.Test suite() {
        NbModuleSuite.Configuration cfg = NbModuleSuite.emptyConfiguration().
                honorAutoloadEager(true).
                enableClasspathModules(false).
                gui(false);
        
        return cfg.clusters("platform|webcommon|ide").addTest(ProxyAutoConfigDirectTest.class).suite();
    }

    public void testGetProxyAutoConfigWithLocalPAC() throws URISyntaxException {
        List<String> pacFileLocations = new LinkedList<String>();
        for (File pacFile : new File(getDataDir(), "pacFiles").listFiles()) {
            pacFileLocations.add(pacFile.getAbsolutePath());
            pacFileLocations.add("file://" + pacFile.getAbsolutePath());
            pacFileLocations.add(pacFile.toURI().toString());
        }
        for (String pacFileLoc : pacFileLocations) {
            ProxyAutoConfig pac = ProxyAutoConfig.get(pacFileLoc);
            assertNotNull(pac);
            URI uri = pac.getPacURI();
            assertNotNull(uri);
            assertNull(uri.getHost());
            List<Proxy> proxies = pac.findProxyForURL(new URI("http://apache.org"));
            assertEquals(1, proxies.size());
            Proxy proxy = proxies.get(0);
            assertEquals(pacFileLoc, Proxy.Type.HTTP, proxy.type());

            final Pattern pattern = Pattern.compile("www-proxy\\.us\\.oracle\\.com.*:80");

            assertTrue(
                "Configuration " + pacFileLoc + " contains the proxy, but was: " + proxy.address(),
                pattern.matcher(proxy.address().toString()).matches()
            );

            proxies = pac.findProxyForURL(new URI("https://apache.org"));
            assertEquals(1, proxies.size());
            proxy = proxies.get(0);
            assertEquals(pacFileLoc, Proxy.Type.HTTP, proxy.type());
            assertTrue(
                "Configuration " + pacFileLoc + " contains the proxy, but was: " + proxy.address(),
                pattern.matcher(proxy.address().toString()).matches()
            );
        }
    }

}
