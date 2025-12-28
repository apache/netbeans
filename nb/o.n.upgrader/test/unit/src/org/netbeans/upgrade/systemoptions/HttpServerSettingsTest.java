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

package org.netbeans.upgrade.systemoptions;

/**
 * @author Radek Matous
 */
public class HttpServerSettingsTest extends BasicTestForImport {
    public HttpServerSettingsTest(String testName) {
        super(testName, "org-netbeans-modules-httpserver-HttpServerSettings.settings");
    }

    @Override
    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/modules/httpserver");
    }
    
    @Override
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {
        "host", "showGrantAccess", "grantedAddresses", "port"
        });
    }
    
    public void testShowGrantAccess() throws Exception {
        assertPropertyType("showGrantAccess", "java.lang.Boolean");
        assertProperty("showGrantAccess", "true");
    }

    public void testHost() throws Exception {
        assertProperty("host", "local");
    }

    public void testGrantedAddresses() throws Exception {
        assertProperty("grantedAddresses", "my.org,your.org");
    }    
    
    public void testPort() throws Exception {
        assertProperty("port", "8083");
    }    
    
}
