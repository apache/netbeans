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
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.TestUtils;
import org.netbeans.junit.NbTestCase;

public class VerifyFileTest extends NbTestCase {

    private static final Logger LOG = Logger.getLogger(VerifyFileTest.class.getName());

    private KeyStore ks;

    public VerifyFileTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        URL urlToKS = TestUtils.class.getResource("data/test-keystore.jks");
        assertNotNull(urlToKS);
        File ksFile = org.openide.util.Utilities.toFile(urlToKS.toURI());
        assertTrue(ksFile.exists());
        ks = getKeyStore(ksFile, "password");
    }

    private String doVerification(String path) throws URISyntaxException, IOException, KeyStoreException {
        URL urlToFile = TestUtils.class.getResource(path);
        assertNotNull(urlToFile);
        File jar = org.openide.util.Utilities.toFile(urlToFile.toURI());
        assertTrue(jar.exists());
        Collection<Certificate> nbmCertificates = Utilities.getNbmCertificates(jar);
        Collection<Certificate> trustedCertificates = Utilities.getCertificates(ks);
        return Utilities.verifyCertificates(nbmCertificates, trustedCertificates);
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
