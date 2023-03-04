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
package org.netbeans.modules.docker.tls;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.Base64;

/**
 *
 * @author Petr Hejl
 */
public class PrivateKeyParser {

    private final File pemFile;

    public PrivateKeyParser(File pemFile) {
        this.pemFile = pemFile;
    }

    // PKCS#8 format
    private static final String PEM_PRIVATE_START = "-----BEGIN PRIVATE KEY-----"; // NOI18N

    private static final String PEM_PRIVATE_END = "-----END PRIVATE KEY-----"; // NOI18N

    // PKCS#1 format
    private static final String PEM_RSA_PRIVATE_START = "-----BEGIN RSA PRIVATE KEY-----"; // NOI18N

    private static final String PEM_RSA_PRIVATE_END = "-----END RSA PRIVATE KEY-----"; // NOI18N

    public PrivateKey parse() throws GeneralSecurityException, IOException {
        Path path = pemFile.toPath();

        String privateKeyPem = new String(Files.readAllBytes(path));

        if (privateKeyPem.contains(PEM_PRIVATE_START)) { // PKCS#8 format
            privateKeyPem = privateKeyPem.replace(PEM_PRIVATE_START, "")
                    .replace(PEM_PRIVATE_END, "")
                    .replaceAll("\\s", "");

            byte[] pkcs8EncodedKey = Base64.getDecoder().decode(privateKeyPem);

            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePrivate(new PKCS8EncodedKeySpec(pkcs8EncodedKey));
        } else if (privateKeyPem.contains(PEM_RSA_PRIVATE_START)) {  // PKCS#1 format
            privateKeyPem = privateKeyPem.replace(PEM_RSA_PRIVATE_START, "")
                    .replace(PEM_RSA_PRIVATE_END, "")
                    .replaceAll("\\s", "");

            DerParser parser = new DerParser(new ByteArrayInputStream(
                    Base64.getDecoder().decode(privateKeyPem)));

            Asn1Object sequence = parser.read();
            parser = sequence.read();

            parser.read(); // Skip version
            BigInteger modulus = parser.read().getBigInteger();
            BigInteger publicExp = parser.read().getBigInteger();
            BigInteger privateExp = parser.read().getBigInteger();
            BigInteger prime1 = parser.read().getBigInteger();
            BigInteger prime2 = parser.read().getBigInteger();
            BigInteger exp1 = parser.read().getBigInteger();
            BigInteger exp2 = parser.read().getBigInteger();
            BigInteger crtCoef = parser.read().getBigInteger();

            RSAPrivateCrtKeySpec keySpec = new RSAPrivateCrtKeySpec(modulus, publicExp,
                    privateExp, prime1, prime2, exp1, exp2, crtCoef);

            KeyFactory factory = KeyFactory.getInstance("RSA");

            return factory.generatePrivate(keySpec);
        } else {
            throw new GeneralSecurityException("The format of the key is not supported");
        }
    }
}
