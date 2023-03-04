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

import org.netbeans.junit.*;

/** Tests HTTP Proxy settings.
 *
 * @author Jiri Rechtacek
 * @see http://www.netbeans.org/issues/show_bug.cgi?id=125588
 */
public class DontUseProxyTest extends NbTestCase {
    public DontUseProxyTest (String name) {
        super (name);
    }
    
    public void testFullIpAdress () {
        String nonProxyHosts = "192.168.1.0|200.100.200.100";
        assertTrue (NbProxySelector.dontUseProxy (nonProxyHosts, "192.168.1.0"));
        assertFalse (NbProxySelector.dontUseProxy (nonProxyHosts, "192.168.1.1"));
        assertFalse (NbProxySelector.dontUseProxy (nonProxyHosts, "192.168.2.0"));
        assertFalse (NbProxySelector.dontUseProxy (nonProxyHosts, "192.169.1.0"));
        assertFalse (NbProxySelector.dontUseProxy (nonProxyHosts, "193.168.1.0"));
        assertFalse (NbProxySelector.dontUseProxy (nonProxyHosts, "192.168"));
        assertFalse (NbProxySelector.dontUseProxy (nonProxyHosts, "192.168.1.0/1"));
        
        assertTrue (NbProxySelector.dontUseProxy (nonProxyHosts, "200.100.200.100"));
        assertFalse (NbProxySelector.dontUseProxy (nonProxyHosts, "200.100.200.200"));
    }
    
    public void testWildcardIpAdress () {
        String nonProxyHosts = "192.*|200.100.200.*";
        assertTrue (NbProxySelector.dontUseProxy (nonProxyHosts, "192.168.1.0"));
        assertTrue (NbProxySelector.dontUseProxy (nonProxyHosts, "192.168.1.1"));
        assertTrue (NbProxySelector.dontUseProxy (nonProxyHosts, "192.168.2.0"));
        assertTrue (NbProxySelector.dontUseProxy (nonProxyHosts, "192.169.1.0"));
        assertFalse (NbProxySelector.dontUseProxy (nonProxyHosts, "193.168.1.0"));
        assertTrue (NbProxySelector.dontUseProxy (nonProxyHosts, "192.168"));
        assertTrue (NbProxySelector.dontUseProxy (nonProxyHosts, "192.168.1.0/1"));
        
        assertTrue (NbProxySelector.dontUseProxy (nonProxyHosts, "200.100.200.100"));
        assertTrue (NbProxySelector.dontUseProxy (nonProxyHosts, "200.100.200.200"));
    }
    
    public void testWildcardHostName () {
        String nonProxyHosts = "*.netbeans.org|*.other.org|*netbeans.com|netbeans.org|*.our.intranet|private.*";
        assertTrue (NbProxySelector.dontUseProxy (nonProxyHosts, "www.netbeans.org"));
        assertTrue (NbProxySelector.dontUseProxy (nonProxyHosts, "www.netbeans.com"));
        assertFalse (NbProxySelector.dontUseProxy (nonProxyHosts, "www.dummynetbeans.org"));
        assertTrue (NbProxySelector.dontUseProxy (nonProxyHosts, "subversion.our.intranet"));
        assertTrue (NbProxySelector.dontUseProxy (nonProxyHosts, "private"));
        assertTrue (NbProxySelector.dontUseProxy (nonProxyHosts, "private.all"));
    }

}
