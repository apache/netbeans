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
package org.netbeans.modules.versioning.util;

import org.junit.Assert;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Ondrej Vrabec
 */
public class KeyringSupportTest extends NbTestCase {
    private String prefix;
    private String key;
    
    public KeyringSupportTest (String name) {
        super(name);
    }

    @Override
    protected void setUp () throws Exception {
        super.setUp();
        prefix = "myvcs_uri_password";
        key = "https://ovrabec@myserver.mydomain.com/path/to/repository.repo";
        Keyring.delete(KeyringSupport.getKeyringKeyHashed(prefix, key));
        Keyring.delete(KeyringSupport.getKeyringKey(prefix, key));
    }
        
    public void testReplaceHashedKeyOldRecord () throws Exception {
        String fullKey = KeyringSupport.getKeyringKeyHashed(prefix, key);
        char[] password = "password".toCharArray();
        Keyring.save(fullKey, password.clone(), "test record");
        
        // old key exists
        Assert.assertArrayEquals(password, Keyring.read(fullKey));
        // old key can be obtained back
        Assert.assertArrayEquals(password, KeyringSupport.read(prefix, key));
        // old key should be no longer present
        assertNull(Keyring.read(fullKey));
        // new key should be present
        Assert.assertArrayEquals(password, Keyring.read(KeyringSupport.getKeyringKey(prefix, key)));
    }
    
    public void testSaveNoHash () throws Exception {
        String fullKey = KeyringSupport.getKeyringKey(prefix, key);
        char[] password = "password".toCharArray();
        
        assertNull(Keyring.read(fullKey));
        
        KeyringSupport.save(prefix, key, password.clone(), "test record");
        
        Assert.assertArrayEquals(password, Keyring.read(fullKey));
        Assert.assertArrayEquals(password, KeyringSupport.read(prefix, key));
    }
    
}
