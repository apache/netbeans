/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.core.network.proxy;

import java.io.File;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
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
            List<Proxy> proxies = pac.findProxyForURL(new URI("http://netbeans.org"));
            assertEquals(1, proxies.size());
            assertTrue(pacFileLoc + ": " + proxies.get(0).toString(), proxies.get(0).toString().startsWith("HTTP @ www-proxy.us.oracle.com/"));
            
            proxies = pac.findProxyForURL(new URI("https://netbeans.org"));
            assertEquals(1, proxies.size());
            assertTrue(pacFileLoc + ": " + proxies.get(0).toString(), proxies.get(0).toString().startsWith("HTTP @ www-proxy.us.oracle.com/"));
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
