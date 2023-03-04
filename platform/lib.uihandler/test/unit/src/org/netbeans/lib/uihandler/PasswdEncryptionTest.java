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
package org.netbeans.lib.uihandler;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import junit.framework.TestCase;

/**
 *
 * @author Jindrich Sedek
 */
public class PasswdEncryptionTest extends TestCase {

    private String inputText = "THIS IS text \\$.-=/;[]1!/*-56";

    public PasswdEncryptionTest(String testName) {
        super(testName);
    }

    public void testEncryptAndDecryptStrings() throws Exception {
        KeyPair pair = generateKeys();
        String encoded = PasswdEncryption.encrypt(inputText, pair.getPublic());
        String decoded = PasswdEncryption.decrypt(encoded, pair.getPrivate());
        assertEquals("DECRIPTED", inputText, decoded);
    }

    public void testEncryptAndDecryptBytes() throws Exception {
        KeyPair pair = generateKeys();
        byte[] encoded = PasswdEncryption.encrypt(inputText.getBytes(), pair.getPublic());
        byte[] decoded = PasswdEncryption.decrypt(encoded, pair.getPrivate());
        assertEquals("DECRIPTED", inputText, new String(decoded));
    }

    public void testEncryptDefault() throws Exception {
        String encoded = PasswdEncryption.encrypt(inputText);
        assertFalse("IS ENCODED", encoded.equals(inputText));
    }

    public void testConvert() throws Exception {
        byte[] pole = inputText.getBytes();
        assertEquals(inputText, new String(pole));
    }

    private KeyPair generateKeys() throws Exception {
        /* Generate a RSA key pair */
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(1024, random);
        return keyGen.generateKeyPair();
    }
}
