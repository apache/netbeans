/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.autoupdate.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSigner;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.TrustAnchor;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.TestUtils;
import org.netbeans.junit.NbTestCase;

import static org.netbeans.modules.autoupdate.services.Utilities.VERIFICATION_RESULT_COMPARATOR;

public class VerifyFileTest extends NbTestCase {

    private static final Logger LOG = Logger.getLogger(VerifyFileTest.class.getName());

    private KeyStore ks;
    private KeyStore validateKs;

    public VerifyFileTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        URL urlToKS = TestUtils.class.getResource("data/test-keystore.jks");
        assertNotNull(urlToKS);
        URL urlToValidateKS = TestUtils.class.getResource("data/test-validate-keystore.jks");
        assertNotNull(urlToValidateKS);
        File ksFile = org.openide.util.Utilities.toFile(urlToKS.toURI());
        assertTrue(ksFile.exists());
        File validateKsFile = org.openide.util.Utilities.toFile(urlToValidateKS.toURI());
        assertTrue(validateKsFile.exists());
        ks = getKeyStore(ksFile, "password");
        validateKs = getKeyStore(validateKsFile, "password");
    }

    private String doVerification(String path) throws URISyntaxException, IOException, KeyStoreException {
        URL urlToFile = TestUtils.class.getResource(path);
        assertNotNull(urlToFile);
        File jar = org.openide.util.Utilities.toFile(urlToFile.toURI());
        assertTrue(jar.exists());
        String res = null;
        try {
            Collection<CodeSigner> nbmCerts = Utilities.getNbmCertificates(jar);
            Collection<Certificate> trustedCerts = Utilities.getCertificates(ks);
            Set<TrustAnchor> trustedCACerts = Collections.EMPTY_SET;
            Collection<Certificate> validationAnchors = new HashSet<>(Utilities.getCertificates(validateKs));
            Set<TrustAnchor> validationCACerts = Collections.EMPTY_SET;
            if (nbmCerts == null) {
                res = Utilities.N_A;
            } else if (nbmCerts.isEmpty()) {
                res = Utilities.UNSIGNED;
            } else {
                // Iterate all certpaths that can be considered for the NBM
                // choose the certpath, that has the highest trust level
                // TRUSTED -> SIGNATURE_VERIFIED -> SIGNATURE_UNVERIFIED -> UNSIGNED
                // or comes first
                for (CodeSigner cp : nbmCerts) {
                    String localRes = Utilities.verifyCertificates(cp, trustedCerts, trustedCACerts, validationAnchors, validationCACerts);
                    if (res == null
                        || VERIFICATION_RESULT_COMPARATOR.compare(res, localRes) > 0) {
                        res = localRes;
                    }
                }
            }
        } catch (SecurityException ex) {
            LOG.log(Level.INFO, "The content of the jar/nbm has been modified or certificate paths were inconsistent - " + ex.getMessage(), ex);
            res = Utilities.MODIFIED;
        }

        return res;
    }

    @SuppressWarnings("unchecked")
    public void testEmptyJar() throws MalformedURLException, URISyntaxException, IOException, KeyStoreException {
        assertEquals(Utilities.N_A, doVerification("data/empty.jar"));
    }

    @SuppressWarnings("unchecked")
    public void testUnsigned() throws MalformedURLException, URISyntaxException, IOException, KeyStoreException {
        assertEquals(Utilities.UNSIGNED, doVerification("data/org-yourorghere-depending.nbm"));
    }

    @SuppressWarnings("unchecked")
    public void testSigned() throws MalformedURLException, URISyntaxException, IOException, KeyStoreException {
        assertFalse(Utilities.UNSIGNED.equals(doVerification("data/dummy-signed.jar")));
    }

    public void testTrusted() throws MalformedURLException, URISyntaxException, IOException, KeyStoreException {
        assertEquals(Utilities.TRUSTED, doVerification("data/dummy-signed.jar"));
    }

    public void testTrustedSignedTwice() throws MalformedURLException, URISyntaxException, IOException, KeyStoreException {
        assertEquals(Utilities.TRUSTED, doVerification("data/dummy-signed-twice.jar"));
    }

    public void testValidatedSigned() throws MalformedURLException, URISyntaxException, IOException, KeyStoreException {
        assertEquals(Utilities.SIGNATURE_VERIFIED, doVerification("data/dummy-validated.jar"));
    }

    public void testUnvalidatedSigned() throws MalformedURLException, URISyntaxException, IOException, KeyStoreException {
        assertEquals(Utilities.SIGNATURE_UNVERIFIED, doVerification("data/dummy-unvalidated.jar"));
    }

    public void testUnsignedPartiallySigned() throws MalformedURLException, URISyntaxException, IOException, KeyStoreException {
        assertEquals(Utilities.MODIFIED, doVerification("data/dummy-partial-signed.jar"));
    }

    private static KeyStore getKeyStore(File file, String password) {
        if (file == null) {
            return null;
        }
        KeyStore keyStore = null;
        InputStream is = null;

        try {

            is = new FileInputStream(file);

            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(is, password.toCharArray());

        } catch (Exception ex) {
            LOG.log(Level.INFO, ex.getMessage(), ex);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                assert false : ex;
            }
        }

        return keyStore;
    }
}
