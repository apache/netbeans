/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.tls;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
