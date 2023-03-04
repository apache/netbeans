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

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

/**
 * Password encryption/decryption utilities.
 * 
 * @author Jindrich Sedek
 */
public class PasswdEncryption {

    private static final String delimiter = ":"; //NOI18N
    public static final int MAX_ENCRYPTION_LENGHT = 100;
    
    /**
     * Encrypt a text with a default public key.
     * @param text text to encrypt
     * @return encrypted text
     * @throws IOException when I/O error occurs
     * @throws GeneralSecurityException when transformation error occurs
     */
    public static String encrypt(String text) throws IOException, GeneralSecurityException {
        return encrypt(text, getPublicKey());
    }

    /**
     * Encrypt a sequence of bytes a default public key.
     * @param text byte array to encrypt
     * @return encrypted bytes
     * @throws IOException when I/O error occurs
     * @throws GeneralSecurityException when transformation error occurs
     */
    public static byte[] encrypt(byte[] text) throws IOException, GeneralSecurityException {
        return encrypt(text, getPublicKey());
    }

    /**
     * Encrypt a sequence of bytes.
     * @param text byte array to encrypt
     * @param key the public key used for the encryption
     * @return encrypted bytes
     * @throws IOException when I/O error occurs
     * @throws GeneralSecurityException when transformation error occurs
     */
    public static byte[] encrypt(byte[] text, PublicKey key) throws IOException, GeneralSecurityException {
        assert (text.length <= MAX_ENCRYPTION_LENGHT);
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // NOI18N
        rsaCipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encoded = null;
        encoded = rsaCipher.doFinal(text);
        return encoded;
    }

    /**
     * Decrypt a sequence of bytes.
     * @param text byte array to decrypt
     * @param key the private key used for the decryption
     * @return decrypted bytes
     * @throws IOException when I/O error occurs
     * @throws GeneralSecurityException when transformation error occurs
     */
    public static byte[] decrypt(byte[] text, PrivateKey key) throws IOException, GeneralSecurityException {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // NOI18N
        rsaCipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decoded = null;
        decoded = rsaCipher.doFinal(text);
        return decoded;
    }

    /**
     * Encrypt a text.
     * @param text text to encrypt
     * @param key the public key used for the encryption
     * @return encrypted text
     * @throws IOException when I/O error occurs
     * @throws GeneralSecurityException when transformation error occurs
     */
    public static String encrypt(String text, PublicKey key) throws IOException, GeneralSecurityException {
        byte[] encrypted = encrypt(text.getBytes(), key);
        return arrayToString(encrypted);
    }

    /**
     * Decrypt a text.
     * @param text text to decrypt
     * @param key the private key used for the decryption
     * @return decrypted text
     * @throws IOException when I/O error occurs
     * @throws GeneralSecurityException when transformation error occurs
     */
    public static String decrypt(String text, PrivateKey key) throws IOException, GeneralSecurityException {
        byte[] decrypted = decrypt(stringToArray(text), key);
        return new String(decrypted);
    }

    private static String arrayToString(byte[] array) {
        String result = "";
        for (int i = 0; i < array.length; i++) {
            byte b = array[i];
            result = result.concat(Byte.toString(b) + delimiter);
        }
        return result;
    }

    private static byte[] stringToArray(String str) {
        String[] numbers = str.split(delimiter);
        byte[] result = new byte[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            result[i] = Byte.parseByte(numbers[i]);
        }
        return result;
    }

    private static PublicKey getPublicKey() throws IOException, GeneralSecurityException {
        InputStream inputStr = PasswdEncryption.class.getResourceAsStream("pubKey"); // NOI18N
        byte[] encodedKey = new byte[inputStr.available()];
        try {
            inputStr.read(encodedKey);
        } finally {
            inputStr.close();
        }
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedKey);
        KeyFactory kf = KeyFactory.getInstance("RSA"); // NOI18N
        PublicKey publicKey = kf.generatePublic(publicKeySpec);
        return publicKey;
    }
}
