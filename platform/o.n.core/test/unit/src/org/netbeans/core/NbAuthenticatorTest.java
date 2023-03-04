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
package org.netbeans.core;

import java.net.Authenticator;
import java.net.Inet4Address;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.concurrent.Callable;
import org.netbeans.junit.NbTestCase;
import org.openide.util.NetworkSettings;

/**
 *
 * @author Ondrej Vrabec
 */
public class NbAuthenticatorTest extends NbTestCase {
    
    public NbAuthenticatorTest(String name) {
        super(name);
    }

    public void testUserInfoInUrl () throws Exception {
        NbAuthenticator.install4test();
        PasswordAuthentication auth = Authenticator.requestPasswordAuthentication("wher.ev.er", Inet4Address.getByName("1.2.3.4"), 1234, "http", null, "http",
                new URL("http://user:password@wher.ev.er/resource"), Authenticator.RequestorType.SERVER);
        
        assertNotNull(auth);
        assertEquals("user", auth.getUserName());
        assertEquals("password", new String(auth.getPassword()));
    }

    public void testSupressedAuthenticator () throws Exception {
        NbAuthenticator.install4test();
        PasswordAuthentication auth =
            NetworkSettings.suppressAuthenticationDialog(new Callable<PasswordAuthentication>() {
                @Override
                public PasswordAuthentication call () throws Exception {
                    return Authenticator.requestPasswordAuthentication("wher.ev.er", Inet4Address.getByName("1.2.3.4"), 1234, "http", null, "http",
                            new URL("http://user:password@wher.ev.er/resource"), Authenticator.RequestorType.SERVER);
                }
            });
        assertNull(auth);
    }
}
