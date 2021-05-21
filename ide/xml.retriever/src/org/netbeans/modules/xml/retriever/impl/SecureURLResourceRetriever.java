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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.net.ssl.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;
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
    
    public HashMap<String, InputStream> retrieveDocument(String baseAddress, String documentAddress) throws IOException,URISyntaxException{
        String effAddr = getEffectiveAddress(baseAddress, documentAddress);
        if(effAddr == null)
            return null;
        URI currURI = new URI(effAddr);
        HashMap<String, InputStream> result = null;
        if (acceptedCertificates==null) acceptedCertificates = new HashSet();
        InputStream is = getInputStreamOfURL(currURI.toURL(), ProxySelector.getDefault().select(currURI).get(0));
        result = new HashMap<String, InputStream>();
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
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
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

        // #208324: proper key managers need to be passed, so let's configure at least the defaults...
        KeyManager[] mgrs;
        if (System.getProperty("javax.net.ssl.keyStorePassword") != null &&  // NOI18N
            System.getProperty("javax.net.ssl.keyStore") != null) { // NOI18N
            try {
                KeyStore ks = KeyStore.getInstance("JKS"); // NOI18N
                    ks.load(new FileInputStream(System.getProperty("javax.net.ssl.keyStore")), //NOI18N
                    System.getProperty("javax.net.ssl.keyStorePassword").toCharArray() //NOI18N
                );
                // Set up key manager factory to use our key store
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(ks,System.getProperty("javax.net.ssl.keyStorePassword").toCharArray()); // NOI18N
                mgrs = kmf.getKeyManagers();
            } catch (IOException ex) {
                // this is somewhat expected, i.e. JKS file not present
                mgrs = null;
            } catch (java.security.GeneralSecurityException e) {
                ErrorManager.getDefault().notify(e);
                return;
            }
        } else {
            mgrs = null;
        }
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL"); //NOI18N
            sslContext.init(mgrs, trustAllCerts, new java.security.SecureRandom());
            con.setSSLSocketFactory(sslContext.getSocketFactory());
            con.setHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String string, SSLSession sSLSession) {
                    // accept all hosts
                    return true;
                }
            });
        } catch (java.security.GeneralSecurityException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
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
