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

package org.netbeans.modules.xml.retriever.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.net.ssl.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Lukas Jungmann, Milan Kuchtiak
 */
public class SecureURLResourceRetriever extends URLResourceRetriever {
    
    private static Set<X509Certificate> acceptedCertificates;
    private static final String URI_SCHEME = "https";
    
    /** Creates a new instance of SecureURLResourceRetriever */
    public SecureURLResourceRetriever() {}
    private static final KeyStore DEFAULT_TRUST_STORE = null;

    @Override
    public boolean accept(String baseAddr, String currentAddr) throws URISyntaxException {
        URI currURI = new URI(currentAddr);
        if( (currURI.isAbsolute()) && (currURI.getScheme().equalsIgnoreCase(URI_SCHEME)))
            return true;
        if(baseAddr != null){
            URI baseURI = new URI(baseAddr);
            if(baseURI.getScheme().equalsIgnoreCase(URI_SCHEME))
                return true;
        }
        return false;
    }
    
    @Override
    public Map<String, InputStream> retrieveDocument(String baseAddress, String documentAddress) throws IOException,URISyntaxException{
        String effAddr = getEffectiveAddress(baseAddress, documentAddress);
        if(effAddr == null)
            return null;
        URI currURI = new URI(effAddr);
        HashMap<String, InputStream> result = null;
        if (acceptedCertificates==null) acceptedCertificates = new HashSet();
        InputStream is = getInputStreamOfURL(currURI.toURL(), ProxySelector.getDefault().select(currURI).get(0));
        result = new HashMap<>();
        result.put(effectiveURL.toString(), is);
        return result;
    }

    @Override
    protected void configureURLConnection(URLConnection ucn) {
        super.configureURLConnection(ucn);
        if (ucn instanceof HttpsURLConnection) {
            setRetrieverTrustManager((HttpsURLConnection)ucn);
        }
    }
    
    // Install the trust manager for retriever
    private void setRetrieverTrustManager(HttpsURLConnection con) {
        TrustManager[] defaultTrustManagers;
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(DEFAULT_TRUST_STORE);
            defaultTrustManagers = tmf.getTrustManagers();
        } catch (KeyStoreException | NoSuchAlgorithmException ex) {
            defaultTrustManagers = new TrustManager[0];
        }
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
                    // ask user to accept the unknown certificate
                    if (certs!=null) {
                        for (int i=0;i<certs.length;i++) {
                            if (!acceptedCertificates.contains(certs[i])) {
                                DialogDescriptor desc = new DialogDescriptor(new CertificationPanel(certs[i]),
                                        NbBundle.getMessage(SecureURLResourceRetriever.class,"TTL_CertifiedWebSite"),
                                        true,
                                        DialogDescriptor.YES_NO_OPTION,
                                        DialogDescriptor.YES_OPTION,
                                        null);
                                DialogDisplayer.getDefault().notify(desc);
                                if (DialogDescriptor.YES_OPTION.equals(desc.getValue())) {
                                    acceptedCertificates.add(certs[i]);
                                } else {
                                    throw new CertificateException(
                                            NbBundle.getMessage(SecureURLResourceRetriever.class,"ERR_NotTrustedCertificate"));
                                }
                            }
                        } // end for
                    }
                }
            }
        };
        TrustManager[] combinedTrustManagers = (TrustManager[]) Stream.of(defaultTrustManagers, trustAllCerts)
                .flatMap(Stream::of)
                .toArray(size -> new TrustManager[size]);

        KeyManager[] keyManagersFromSystemProperties = null;
        try {
            KeyStore keyStoreFromSystemProperties = null;
            char[] keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword", "").toCharArray();
            if (System.getProperty("javax.net.ssl.keyStore") != null) {
                File keyStoreFile = new File(System.getProperty("javax.net.ssl.keyStore"));
                if (keyStoreFile.exists()) {
                    KeyStore keyStore = KeyStore.getInstance(System.getProperty("javax.net.ssl.keyStoreType", KeyStore.getDefaultType()));
                    try ( InputStream keyStoreStream = new FileInputStream(keyStoreFile)) {
                        keyStore.load(keyStoreStream, keyStorePassword);
                    }

                    keyStoreFromSystemProperties = keyStore;
                }
            }

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStoreFromSystemProperties, keyStorePassword);
            keyManagersFromSystemProperties = keyManagerFactory.getKeyManagers();
        } catch (GeneralSecurityException | IOException ex) {
            keyManagersFromSystemProperties = new KeyManager[0];
        }

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL"); //NOI18N
            sslContext.init(keyManagersFromSystemProperties, combinedTrustManagers, new java.security.SecureRandom());
            con.setSSLSocketFactory(sslContext.getSocketFactory());
            con.setHostnameVerifier(this::acceptAllHosts);
        } catch (GeneralSecurityException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    private boolean acceptAllHosts(String host, SSLSession sslSession) {
        return true;
    }

    @Override
    public String getEffectiveAddress(String baseAddress, String documentAddress) throws IOException, URISyntaxException {
        URI currURI = new URI(documentAddress);
        String result = null;
        if(currURI.isAbsolute()){
            result = currURI.toString();
            return result;
        }else{
            //relative URI
            if(baseAddress != null){
                URI baseURI = new URI(baseAddress);
                URI finalURI = baseURI.resolve(currURI);
                result = finalURI.toString();
                return result;
            }else{
                //neither the current URI nor the base URI are absoulte. So, can not resolve this
                //path
                return null;
            }
        }
    }
}
