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
