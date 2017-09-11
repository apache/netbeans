/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
