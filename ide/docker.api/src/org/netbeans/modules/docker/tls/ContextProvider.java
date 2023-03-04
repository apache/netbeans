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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.netbeans.modules.docker.api.DockerInstance;

/**
 *
 * @author Petr Hejl
 */
public final class ContextProvider {

    private static final Logger LOGGER = Logger.getLogger(ContextProvider.class.getName());

    private static final ContextProvider INSTANCE = new ContextProvider();

    private final SecureRandom random = new SecureRandom();

    private final Map<DockerInstance, ContextHolder> cache = new WeakHashMap<>();

    private ContextProvider() {
        super();
    }

    public static ContextProvider getInstance() {
        return INSTANCE;
    }

    public SSLContext getSSLContext(DockerInstance instance) {
        synchronized (this) {
            // FIXME make this atomic ?
            File caCertificateFile = instance.getCaCertificateFile();
            File certificateFile = instance.getCertificateFile();
            File keyFile = instance.getKeyFile();

            ContextHolder holder = cache.get(instance);
            if (holder != null) {
                if (Objects.equals(holder.getCaCertificateFile(), caCertificateFile)
                        && Objects.equals(holder.getCertificateFile(), certificateFile)
                        && Objects.equals(holder.getKeyFile(), keyFile)) {
                    // FIXME file modifications; timestamps or listeners
                    LOGGER.log(Level.FINE, "SSLContext cache hit");
                    return holder.getContext();
                }
            }

            SSLContext context;
            try {
                try {
                    // FIXME nonexisting files
                    if (caCertificateFile == null && certificateFile == null && keyFile == null) {
                        context = SSLContext.getInstance("TLS");
                    } else {
                        if (certificateFile == null && keyFile != null) {
                            LOGGER.log(Level.INFO, "Certificate file is null; ignoring key file");
                            context = createSSLContext(caCertificateFile, null, null);
                        }
                        if (certificateFile != null && keyFile == null) {
                            LOGGER.log(Level.INFO, "Key file is null; ignoring certificate file");
                            context = createSSLContext(caCertificateFile, null, null);
                        } else {
                            context = createSSLContext(caCertificateFile, certificateFile, keyFile);
                        }
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    context = SSLContext.getInstance("TLS");
                }
            } catch (NoSuchAlgorithmException ex) {
                LOGGER.log(Level.INFO, null, ex);
                try {
                    // this must be implemented in every java platform
                    context = SSLContext.getInstance("TLSv1");
                } catch (NoSuchAlgorithmException ex1) {
                    throw new UnknownError("Platform does not support TLSv1 required by spec");
                }
            }
            holder = new ContextHolder(caCertificateFile, certificateFile, keyFile, context);
            cache.put(instance, holder);
            return context;
        }
    }

    private SSLContext createSSLContext(File caCert, File clientCert, File clientKey) throws IOException {
        assert (clientCert != null && clientKey != null) || (clientCert == null && clientKey == null);
        try {
            char[] keyStorePassword = new BigInteger(130, random).toString(32).toCharArray();
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate caCertObject;
            try (InputStream is = new BufferedInputStream(new FileInputStream(caCert))) {
                caCertObject = cf.generateCertificate(is);
            }
            Certificate clientCertObject = null;
            if (clientCert != null) {
                try (InputStream is = new BufferedInputStream(new FileInputStream(clientCert))) {
                    clientCertObject = cf.generateCertificate(is);
                }
            }

            PrivateKey clientKeyObject = null;
            if (clientKey != null) {
                clientKeyObject = new PrivateKeyParser(clientKey).parse();
            }

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            trustStore.setEntry("ca", new KeyStore.TrustedCertificateEntry(caCertObject), null);
            TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            tmfactory.init(trustStore);

            KeyManagerFactory kmfactory = null;
            if (clientCertObject != null && clientKeyObject != null) {
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null, null);
                keyStore.setCertificateEntry("client", clientCertObject);
                keyStore.setKeyEntry("key", clientKeyObject,
                        keyStorePassword, new Certificate[]{clientCertObject});

                kmfactory = KeyManagerFactory.getInstance(
                        KeyManagerFactory.getDefaultAlgorithm());
                kmfactory.init(keyStore, keyStorePassword);
            }

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(kmfactory != null ? kmfactory.getKeyManagers() : null, tmfactory.getTrustManagers(), null);
            return context;
        } catch (GeneralSecurityException ex) {
            throw new IOException(ex);
        }
    }

    private static class ContextHolder {

        private final File caCertificateFile;

        private final File certificateFile;

        private final File keyFile;

        private final SSLContext context;

        public ContextHolder(File caCertificateFile, File certificateFile, File keyFile, SSLContext context) {
            this.caCertificateFile = caCertificateFile;
            this.certificateFile = certificateFile;
            this.keyFile = keyFile;
            this.context = context;
        }

        public File getCaCertificateFile() {
            return caCertificateFile;
        }

        public File getCertificateFile() {
            return certificateFile;
        }

        public File getKeyFile() {
            return keyFile;
        }

        public SSLContext getContext() {
            return context;
        }

    }
}
