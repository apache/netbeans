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

package org.netbeans.modules.keyring.fallback;

import java.awt.GraphicsEnvironment;
import java.util.logging.Level;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;

public class MasterPasswordEncryptionTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(MasterPasswordEncryptionTest.class);
    }

    public MasterPasswordEncryptionTest(String n) {
        super(n);
    }

    @Override protected Level logLevel() {
        return Level.FINE;
    }

    public void testEncryption() throws Exception {
        doTestEncryption("Top Secret!", "my password");
        doTestEncryption("some extra secret pass phrase", "something pretty long here for whatever reason...");
        // Acc. to PBEKeySpec, using PKCS #5 (not #12) only ASCII is supported for master passwords:
        doTestEncryption("muj heslo", "hezky česky");
        // Some extra chars
        doTestEncryption("muj heslo", "!§$%&/()=?\\}][{+*#~");
        doTestEncryption("muj heslo", "ॐ");
        // Test fr all characters
        for (char c = 0; c < Character.MAX_VALUE; ++c) {
            doTestEncryption("muj heslo", Character.toString(c));
        }
    }
    
    private void doTestEncryption(String masterPassword, String password) throws Exception {
        MasterPasswordEncryption p = new MasterPasswordEncryption();
        assertTrue(p.enabled());
        p.unlock(masterPassword.toCharArray());
        assertEquals(password, new String(p.decrypt(p.encrypt(password.toCharArray()))));
    }

    public void testWrongPassword() throws Exception {
        MasterPasswordEncryption p = new MasterPasswordEncryption();
        assertTrue(p.enabled());
        p.unlock("first password".toCharArray());
        byte[] ciphertext = p.encrypt("secret".toCharArray());
        p.unlock("second password".toCharArray());
        char[] result = new char[0];
        try {
            result = p.decrypt(ciphertext);
        } catch (Exception x) {
            // expected: "BadPaddingException: Given final block not properly padded"
        }
        assertFalse("should not be able to decrypt with incorrect password", new String(result).equals("secret"));
    }

}
