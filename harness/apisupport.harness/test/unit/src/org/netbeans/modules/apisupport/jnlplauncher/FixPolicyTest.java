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

package org.netbeans.modules.apisupport.jnlplauncher;

import java.net.URL;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import junit.framework.*;
import java.security.*;

/**
 * Test whether the system property netbeans.jnlp.fixPolicy
 * really grants everything to everybody.
 * @author David Strupl
 */
public class FixPolicyTest extends TestCase {

    private URL url = null;

    public FixPolicyTest(String testName) {
        super(testName);
    }

    public void testFixPolicy() throws Exception {

        url = new URL("http://boo.baa");
        System.setProperty("netbeans.jnlp.fixPolicy", "true");
        
        Main.fixPolicy();
        
        assertTrue(Policy.getPolicy().implies(
            new ProtectionDomain(
                new CodeSource(
                    url,
                    new java.security.cert.Certificate[0]
                ),
                new AllPermission().newPermissionCollection()),
                new AllPermission()
        ));
    }

}
