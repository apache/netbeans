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

package org.netbeans.modules.keyring.win32;

import org.netbeans.junit.NbTestCase;

public class Win32ProtectTest extends NbTestCase {

    public Win32ProtectTest(String n) {
        super(n);
    }

    public void testEncryption() throws Exception {
        Win32Protect p = new Win32Protect();
        if (!p.enabled()) {
            System.err.println("Skipping Win32ProtectTest on " + System.getProperty("os.name"));
            return;
        }
        doTestEncryption(p, "my password");
        doTestEncryption(p, "something pretty long here for whatever reason...");
        doTestEncryption(p, "hezky česky");
        doTestEncryption(p, "ॐ");
    }
    private void doTestEncryption(Win32Protect p, String password) throws Exception {
        byte[] ciphertext = p.encrypt(password.toCharArray());
        //System.err.println(password + " -> " + Arrays.toString(ciphertext));
        assertEquals(password, new String(p.decrypt(ciphertext)));
    }

}
