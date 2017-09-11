/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        URL urlToKS = TestUtils.class.getResource("data/foo.jks");
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
