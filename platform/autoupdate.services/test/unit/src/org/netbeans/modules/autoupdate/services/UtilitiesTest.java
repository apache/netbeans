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
package org.netbeans.modules.autoupdate.services;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import static java.util.Arrays.asList;
import java.util.HashSet;
import static org.junit.Assert.*;
import org.junit.Test;
import org.netbeans.core.startup.MainLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.autoupdate.KeyStoreProvider;
import org.netbeans.spi.autoupdate.KeyStoreProvider.TrustLevel;

public class UtilitiesTest extends NbTestCase {

    private static final KeyStore trustedKeyStore;
    private static final KeyStoreProvider trustedKeyStoreProvider;
    private static final KeyStore validCAKeyStore;
    private static final KeyStoreProvider validCAKeyStoreProvider;
    private static final KeyStore defaultKeyStore;
    private static final KeyStoreProvider defaultKeyStoreProvider;
    static {
        try {
            trustedKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustedKeyStoreProvider = new KeyStoreProvider() {
                @Override
                public KeyStore getKeyStore() {
                    return trustedKeyStore;
                }

                @Override
                public TrustLevel getTrustLevel() {
                    return TrustLevel.TRUST;
                }
            };
            validCAKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            validCAKeyStoreProvider = new KeyStoreProvider() {
                @Override
                public KeyStore getKeyStore() {
                    return validCAKeyStore;
                }

                @Override
                public TrustLevel getTrustLevel() {
                    return KeyStoreProvider.TrustLevel.VALIDATE_CA;
                }
            };
            defaultKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            defaultKeyStoreProvider = new KeyStoreProvider() {
                @Override
                public KeyStore getKeyStore() {
                    return defaultKeyStore;
                }
            };
        } catch (KeyStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    public UtilitiesTest(String name) {
        super(name);
    }

    @Test
    public void testHexEncode() throws NoSuchAlgorithmException {
        MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
        byte[] hash = sha512.digest(new byte[]{});
        assertEquals(
            "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e",
            Utilities.hexEncode(hash));
    }

    @Test
    public void testHexDecode() throws NoSuchAlgorithmException {
        MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
        byte[] hash = sha512.digest(new byte[]{});
        assertArrayEquals(
            hash,
            Utilities.hexDecode("cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e"));
    }

    @Test
    public void testTrustLevel() {
        MainLookup.register(trustedKeyStoreProvider);
        MainLookup.register(validCAKeyStoreProvider);
        MainLookup.register(defaultKeyStoreProvider);
        try {
            // the default trust level is trusted - so both the keystore provider
            // with the level explicitly set to trusted and the one without a
            // trustlevel should be returned
            assertEquals(
                    new HashSet<>(asList(defaultKeyStore, trustedKeyStore)),
                    new HashSet<>(Utilities.getKeyStore(TrustLevel.TRUST))
            );
            // Only the validCAKeyStoreProvider is on level VALIDATE_CA so
            // the keystore validCAKeyStore should be returned
            assertEquals(
                    new HashSet<>(asList(validCAKeyStore)),
                    new HashSet<>(Utilities.getKeyStore(TrustLevel.VALIDATE_CA))
            );
        } finally {
            MainLookup.unregister(trustedKeyStoreProvider);
            MainLookup.unregister(validCAKeyStoreProvider);
            MainLookup.unregister(defaultKeyStoreProvider);
        }
    }
}
