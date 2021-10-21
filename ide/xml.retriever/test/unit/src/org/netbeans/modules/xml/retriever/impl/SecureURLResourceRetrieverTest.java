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
package org.netbeans.modules.xml.retriever.impl;

import javax.net.ssl.SSLHandshakeException;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.junit.MockServices;
import org.openide.util.Lookup;

/**
 *
 * @author Benjamin Asbach
 */
public class SecureURLResourceRetrieverTest {

    @BeforeClass
    public static void mockDialogDisplayer() {
        MockServices.setServices(MockDialogDisplayer.class);
    }

    public void resetMockDialogDisplayer() {
        System.setProperty("javax.net.ssl.keysStore", null);
        System.setProperty("javax.net.ssl.keyStorePassword", null);
        System.setProperty("javax.net.ssl.keyStoreType", null);
        lookupMockDialogDisplayer().reset();
    }

    @Test
    public void shouldAcceptValidCertificate() throws Exception {
        SecureURLResourceRetriever resourceRetriever = new SecureURLResourceRetriever();
        resourceRetriever.retrieveDocument(null, "https://letsencrypt.org/");

        assertFalse(lookupMockDialogDisplayer().invoked);
    }

    @Test
    public void shouldAcceptWildcardCertificate() throws Exception {
        SecureURLResourceRetriever resourceRetriever = new SecureURLResourceRetriever();
        resourceRetriever.retrieveDocument(null, "https://badssl.com/");

        assertFalse(lookupMockDialogDisplayer().invoked);
    }

    @Test(expected = SSLHandshakeException.class)
    public void shouldAskForExpiredCertificate() throws Exception {
        SecureURLResourceRetriever resourceRetriever = new SecureURLResourceRetriever();
        resourceRetriever.retrieveDocument(null, "https://expired.badssl.com/");

        assertTrue(lookupMockDialogDisplayer().invoked);
    }

    @Test(expected = SSLHandshakeException.class)
    public void shouldAskForSelfSignedCertificate() throws Exception {
        SecureURLResourceRetriever resourceRetriever = new SecureURLResourceRetriever();
        resourceRetriever.retrieveDocument(null, "https://self-signed.badssl.com/");

        assertTrue(lookupMockDialogDisplayer().invoked);
    }

    @Test
    public void shouldUseKeyStoreFromSystemProperties() throws Exception {
        System.setProperty("javax.net.debug", "ssl,keystore");
        System.setProperty("javax.net.ssl.keyStore", SecureURLResourceRetrieverTest.class.getResource("badssl.com-client.p12").getPath());
        System.setProperty("javax.net.ssl.keyStorePassword", "badssl.com");
        System.setProperty("javax.net.ssl.keyStoreType", "pkcs12");

        SecureURLResourceRetriever resourceRetriever = new SecureURLResourceRetriever();
        resourceRetriever.retrieveDocument(null, "https://client.badssl.com/");

        assertFalse(lookupMockDialogDisplayer().invoked);
    }

    private MockDialogDisplayer lookupMockDialogDisplayer() {
        return Lookup.getDefault().lookup(MockDialogDisplayer.class);
    }
}
