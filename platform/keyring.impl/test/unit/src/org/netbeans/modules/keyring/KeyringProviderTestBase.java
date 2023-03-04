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

package org.netbeans.modules.keyring;

import java.security.SecureRandom;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.keyring.KeyringProvider;

public abstract class KeyringProviderTestBase extends NbTestCase {

    protected KeyringProviderTestBase(String n) {
        super(n);
    }

    protected abstract KeyringProvider createProvider();

    public void testStorage() throws Exception {
        KeyringProvider p = createProvider();
        if (!p.enabled()) {
            System.err.println(p + " disabled on " + System.getProperty("os.name") + ", skipping");
            return;
        }
        
        byte[] randomArray = new byte[36];
        new SecureRandom().nextBytes(randomArray);
        doTestStorage(p, "something", "secret stuff " + new String(randomArray), null);
        doTestStorage(p, "more", "secret stuff", "a description here");
        doTestStorage(p, "klíč", "hezky česky", "můj heslo");
        doTestStorage(p, "klā′vē ər", "ॐ", "κρυπτός");
    }
    
    private void doTestStorage(KeyringProvider p, String key, String password, String description) throws Exception {
        byte[] randomArray = new byte[36];
        new SecureRandom().nextBytes(randomArray);
        key = "KeyringProviderTestBase." + new String(randomArray) + key; // avoid interfering with anything real
        assertEquals(null, p.read(key));
        try {
            p.save(key, password.toCharArray(), description);
            char[] loaded = p.read(key);
            assertEquals(password, loaded != null ? new String(loaded) : null);
            password += " (edited)";
            p.save(key, password.toCharArray(), description);
            loaded = p.read(key);
            assertEquals(password, loaded != null ? new String(loaded) : null);
        } finally {
            p.delete(key);
            assertEquals(null, p.read(key));
        }
    }

}
