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

import java.io.File;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jirka Rechtacek
 */
public class ProxyAutoConfigTest extends NbTestCase {
    
    public static Test suite() {
        return NbModuleSuite
                .emptyConfiguration()
                .gui(false)
                .addTest(ProxyAutoConfigTest.class)
                .suite();
    }
    
    public ProxyAutoConfigTest(String name) {
        super(name);
    }
    
    public void testGetProxyAutoConfig() {
        assertNotNull(ProxyAutoConfig.get("http://pac/pac.txt"));
    }
    
    // #issue 201995
    public void testGetProxyAutoConfigWithMultipleURL() {
        assertNotNull(ProxyAutoConfig.get("http://pac/pac.txt http://pac/pac.txt http://pac/pac.txt"));
    }
    
    public void testGetProxyAutoConfigWithNewLineURL() {
        assertNotNull(ProxyAutoConfig.get("http://pac/pac.txt\nhttp://pac/pac.txt"));
    }
    
    public void testGetProxyAutoConfigWithLineTerminatorURL() {
        assertNotNull(ProxyAutoConfig.get("http://pac/pac.txt\rhttp://pac/pac.txt"));
    }
    
    public void testGetProxyAutoConfigWithBothTerminatorsURL() {
        assertNotNull(ProxyAutoConfig.get("http://pac/pac.txt\r\nhttp://pac/pac.txt"));
    }
    
    public void testGetProxyAutoConfigWithInvalidURL() {
        assertNotNull(ProxyAutoConfig.get("http:\\\\pac\\pac.txt"));
    }
    
    public void testGetProxyAutoConfigWithRelativePath() {
        assertNotNull(ProxyAutoConfig.get("http://pac/../pac/pac.txt"));
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
    
    public void XXXtestGetProxyAutoConfigWithLocalInvalidPAC() throws URISyntaxException {
        List<String> pacFileLocations = new LinkedList<String>();
        for (File pacFile : new File[] {
            new File(getDataDir(), "pacFiles"),
            //new File(getDataDir(), "doesNotExist")
        }) {
            pacFileLocations.add(pacFile.getAbsolutePath());
            pacFileLocations.add("file://" + pacFile.getAbsolutePath());
            pacFileLocations.add(pacFile.toURI().toString());
        }
        for (String pacFileLoc : pacFileLocations) {
            ProxyAutoConfig pac = ProxyAutoConfig.get(pacFileLoc);
            assertNotNull(pac);
            URI uri = pac.getPacURI();
            assertNotNull(uri);
            List<Proxy> proxies = pac.findProxyForURL(new URI("http://netbeans.org"));
            assertEquals(1, proxies.size());
            assertEquals("DIRECT", proxies.get(0).toString());
        }
    }
}
