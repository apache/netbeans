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
import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.keyring.utils.Utils;
import org.netbeans.modules.keyring.spi.EncryptionProvider;
import org.openide.util.Mutex;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * Encrypts data using a master password which the user must enter for each NetBeans session.
 */
@ServiceProvider(service=EncryptionProvider.class, position=1000)
public class MasterPasswordEncryption implements EncryptionProvider {

    private static final Logger LOG = Logger.getLogger(MasterPasswordEncryption.class.getName());
    private static final String ENCRYPTION_ALGORITHM = "PBEWithSHA1AndDESede"; // NOI18N
    private SecretKeyFactory KEY_FACTORY;
    private AlgorithmParameterSpec PARAM_SPEC;

    private Cipher encrypt, decrypt;
    private boolean unlocked;
    private Callable<Void> encryptionChanging;
    private char[] newMasterPassword;
    private boolean fresh;

    public @Override boolean enabled() {
        if (Boolean.getBoolean("netbeans.keyring.no.master")) {
            LOG.fine("master password encryption disabled");
            return false;
        }
        if (GraphicsEnvironment.isHeadless()) {
            LOG.fine("disabling master password encryption in headless mode");
            return false;
        }
        try {
            KEY_FACTORY = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM);
            encrypt = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            decrypt = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            Preferences prefs = NbPreferences.forModule(Keyring.class);
            Utils.goMinusR(prefs);
            String saltKey = "salt"; // NOI18N
            byte[] salt = prefs.getByteArray(saltKey, null);
            if (salt == null) {
                salt = new byte[36];
                new SecureRandom().nextBytes(salt);
                prefs.putByteArray(saltKey, salt);
            }
            PARAM_SPEC = new PBEParameterSpec(salt, 20);
            LOG.warning("Falling back to master password encryption; " +
                    "add -J-Dorg.netbeans.modules.keyring.level=0 to netbeans.conf to see why native keyrings could not be loaded");
            return true;
        } catch (Exception x) {
            LOG.log(Level.INFO, "Cannot initialize security using " + ENCRYPTION_ALGORITHM, x);
            return false;
        }
    }

    public @Override String id() {
        return "general"; // NOI18N
    }

    public @Override byte[] encrypt(char[] cleartext) throws Exception {
        if (!unlockIfNecessary()) {
            throw new Exception("cannot unlock");
        }
        try {
            return doEncrypt(cleartext);
        } catch (Exception x) {
            unlocked = false; // reset
            throw x;
        }
    }

    public @Override char[] decrypt(byte[] ciphertext) throws Exception {
        AtomicBoolean callEncryptionChanging = new AtomicBoolean();
        if (!unlockIfNecessary(callEncryptionChanging)) {
            throw new Exception("cannot unlock");
        }
        try {
            return doDecrypt(ciphertext);
        } catch (Exception x) {
            unlocked = false; // reset
            throw x;
        } finally {
            if (callEncryptionChanging.get()) {
                try {
                    encryptionChanging.call();
                } catch (Exception x) {
                    LOG.log(Level.FINE, "failed to change encryption", x);
                }
            }
        }
    }

    private boolean unlockIfNecessary() {
        AtomicBoolean callEncryptionChanging = new AtomicBoolean();
        boolean result = unlockIfNecessary(callEncryptionChanging);
        if (callEncryptionChanging.get()) {
            try {
                encryptionChanging.call();
            } catch (Exception x) {
                LOG.log(Level.FINE, "failed to change encryption", x);
            }
        }
        return result;
    }
    private boolean unlockIfNecessary(AtomicBoolean callEncryptionChanging) {
        if (unlocked) {
            return true;
        }
        char[][] passwords = Mutex.EVENT.readAccess(new Mutex.Action<char[][]>() {
            public @Override char[][] run() {
                return new MasterPasswordPanel().display(fresh);
            }
        });
        if (passwords == null) {
            LOG.fine("cancelled master password dialog");
            return false;
        }
        try {
            unlock(passwords[0]);
            Arrays.fill(passwords[0], '\0');
            if (passwords.length == 2) {
                newMasterPassword = passwords[1];
                LOG.fine("will set new master password");
                callEncryptionChanging.set(true);
            }
            return true;
        } catch (Exception x) {
            LOG.log(Level.FINE, "failed to initialize ciphers", x);
            return false;
        }
    }

    void unlock(char[] masterPassword) throws Exception {
        LOG.fine("switching to new master password");
        KeySpec keySpec = new PBEKeySpec(masterPassword);
        Key key = KEY_FACTORY.generateSecret(keySpec);
        encrypt.init(Cipher.ENCRYPT_MODE, key, PARAM_SPEC);
        decrypt.init(Cipher.DECRYPT_MODE, key, PARAM_SPEC);
        unlocked = true;
    }

    byte[] doEncrypt(char[] cleartext) throws Exception {
        assert unlocked;
        byte[] cleartextB = Utils.chars2Bytes(cleartext);
        byte[] result = encrypt.doFinal(cleartextB);
        Arrays.fill(cleartextB, (byte) 0);
        return result;
    }

    char[] doDecrypt(byte[] ciphertext) throws Exception {
        assert unlocked;
        byte[] result = decrypt.doFinal(ciphertext);
        char[] cleartext = Utils.bytes2Chars(result);
        Arrays.fill(result, (byte) 0);
        return cleartext;
    }

    public @Override boolean decryptionFailed() {
        unlocked = false;
        return unlockIfNecessary();
    }

    public @Override void encryptionChangingCallback(Callable<Void> callback) {
        encryptionChanging = callback;
    }

    public @Override void encryptionChanged() {
        assert newMasterPassword != null;
        LOG.fine("encryption changed");
        try {
            unlock(newMasterPassword);
        } catch (Exception x) {
            LOG.log(Level.FINE, "failed to initialize ciphers", x);
        }
        Arrays.fill(newMasterPassword, '\0');
        newMasterPassword = null;
    }

    public @Override void freshKeyring(boolean fresh) {
        this.fresh = fresh;
    }

}
