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
package org.netbeans.installer.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.CodeSigner;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.swing.JOptionPane;

/**
 *
 * @author Kirill Sorokin
 * @author Dmitry Lipin
 */
final class SecurityUtils {

    private static KeyStore caStore;
    private static KeyStore permanentTrustedStore;
    private static KeyStore sessionTrustedStore;
    private static KeyStore deniedStore;
    private static String CACERTS_FILE_PATH = "lib/security/cacerts";//NOI18N
    private static final int BUFFER_SIZE = 4096;

    public static boolean isJarSignatureVeryfied(
            final File file,
            final String description) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        if (caStore == null) {
            caStore = KeyStore.getInstance(KeyStore.getDefaultType());
            final File cacertsFile = new File(SystemUtils.getCurrentJavaHome(), CACERTS_FILE_PATH);
            caStore.load(new FileInputStream(cacertsFile), null);

            permanentTrustedStore = KeyStore.getInstance(KeyStore.getDefaultType());
            permanentTrustedStore.load(null, null);

            sessionTrustedStore = KeyStore.getInstance(KeyStore.getDefaultType());
            sessionTrustedStore.load(null, null);

            deniedStore = KeyStore.getInstance(KeyStore.getDefaultType());
            deniedStore.load(null, null);
        }

        final JarFile jar = new JarFile(file);
        try {
            // first we should fetch all certificates that are present in the jar
            // file skipping duplicates
            Certificate[] certificates = null;
            CodeSigner[] codeSigners = null;
            for (JarEntry entry : Collections.list(jar.entries())) {
                readFully(jar.getInputStream(entry));

                certificates = entry.getCertificates();
                codeSigners = entry.getCodeSigners();

                if (certificates != null) {
                    break;
                }
            }

            // if there are no certificates -- we should pop up the dialog warning
            // that the jar is not signed and ask the user whether he wants to
            // accept this
            if (certificates == null) {
                // todo
            }

            // check the permanent and session trusted stores
            int chainStart = 0;
            int chainEnd = 0;
            int chainNum = 0;

            // iterate over the certificate chains that are present in the
            // certificate arrays
            while (chainEnd < certificates.length) {
                // determine the start and end of the current certificates chain
                int i = chainStart;
                while (i < certificates.length - 1) {
                    final boolean isIssuer = isIssuerOf(
                            (X509Certificate) certificates[i],
                            (X509Certificate) certificates[i + 1]);

                    if ((certificates[i] instanceof X509Certificate) && (certificates[i + 1] instanceof X509Certificate) && isIssuer) {
                        i++;
                    } else {
                        break;
                    }
                }
                chainEnd = i + 1;

                // if the denied certificates store contains the
                if (containsCertificate(deniedStore, certificates[chainStart])) {
                    return false;
                } else if (containsCertificate(permanentTrustedStore, certificates[chainStart]) ||
                        containsCertificate(sessionTrustedStore, certificates[chainStart])) {
                    return true;
                }

                chainStart = chainEnd;
                chainNum++;
            }

            // If we get here, no cert in chain has been stored in Session or Permanent store.
            // If they are not in Deny store either, we have to pop up security dialog box
            // for each signer's certificate one by one.
            boolean rootCANotValid = false;
            boolean timeNotValid = false;

            chainStart = 0;
            chainEnd = 0;
            chainNum = 0;
            while (chainEnd < certificates.length) {
                int i = chainStart;

                for (i = chainStart; i < certificates.length; i++) {
                    X509Certificate currentCert = null;
                    X509Certificate issuerCert = null;

                    if (certificates[i] instanceof X509Certificate) {
                        currentCert = (X509Certificate) certificates[i];
                    }
                    if ((i < certificates.length - 1) &&
                            (certificates[i + 1] instanceof X509Certificate)) {
                        issuerCert = (X509Certificate) certificates[i + 1];
                    } else {
                        issuerCert = currentCert;
                    }

                    // check if the certificate is valid and has not expired
                    try {
                        currentCert.checkValidity();
                    } catch (CertificateExpiredException e1) {
                        timeNotValid = true;
                    } catch (CertificateNotYetValidException e2) {
                        timeNotValid = true;
                    }

                    if (isIssuerOf(currentCert, issuerCert)) {
                        // check the current certificate's signature -- verify that
                        // this issuer did indeed sign the certificate.
                        try {
                            currentCert.verify(issuerCert.getPublicKey());
                        } catch (GeneralSecurityException e) {
                            return false;
                        }
                    } else {
                        break;
                    }
                }
                chainEnd = (i < certificates.length) ? (i + 1) : i;

                // we need to verify if the certificate chain is signed by a CA
                rootCANotValid = !verifyCertificate(caStore, certificates[chainEnd - 1]);

                Date timestamp = null;
                if (codeSigners[chainNum].getTimestamp() != null) {
                    timestamp = codeSigners[chainNum].getTimestamp().getTimestamp();
                }

                CertificateAcceptanceStatus status = showCertificateAcceptanceDialog(
                        certificates,
                        chainStart,
                        chainEnd,
                        rootCANotValid,
                        timeNotValid,
                        timestamp,
                        description);


                // If user Grant permission, just pass all security checks.
                // If user Deny first signer, pop up security box for second signer certs
                if (status == CertificateAcceptanceStatus.ACCEPT_PERMANENTLY) {
                    addCertificate(permanentTrustedStore, certificates[chainStart]);
                    return true;
                } else if (status == CertificateAcceptanceStatus.ACCEPT_FOR_THIS_SESSION) {
                    addCertificate(sessionTrustedStore, certificates[chainStart]);
                    return true;
                } else {
                    addCertificate(deniedStore, certificates[chainStart]);
                }

                chainStart = chainEnd;
                chainNum++;
            }

            return false;
        } finally {
            jar.close();
        }
    }

    private static void readFully(
            final InputStream stream) throws IOException {
        final byte[] buffer = new byte[BUFFER_SIZE];
        while (stream.read(buffer) != -1) {
            ; // do this!
        }
    }

    private static boolean isIssuerOf(
            final X509Certificate certificate1,
            final X509Certificate certificate2) {
        return certificate1.getIssuerDN().equals(certificate2.getSubjectDN());
    }

    private static boolean containsCertificate(
            final KeyStore store,
            final Certificate certificate) throws KeyStoreException {
        return store.getCertificateAlias(certificate) != null;
    }

    private static void addCertificate(
            final KeyStore store,
            final Certificate certificate) throws KeyStoreException {
        if (store.getCertificateAlias(certificate) == null) {
            store.setCertificateEntry(
                    "alias" + new Random().nextLong(),
                    certificate);
        }
    }

    private static boolean verifyCertificate(
            final KeyStore store,
            final Certificate certificate) throws KeyStoreException {
        for (String alias : Collections.list(store.aliases())) {
            try {
                certificate.verify(store.getCertificate(alias).getPublicKey());
                return true;
            } catch (GeneralSecurityException e) {
                // we must ignore this exception as it is VERY expected -- will
                // happen N-1 times at least
            }
        }

        return false;
    }

    private static CertificateAcceptanceStatus showCertificateAcceptanceDialog(
            final Certificate[] certificates,
            final int chainStart,
            final int chainEnd,
            final boolean rootCaIsNotValid,
            final boolean timeIsNotValid,
            final Date timestamp,
            final String description) {
        if (certificates[chainStart] instanceof X509Certificate && certificates[chainEnd - 1] instanceof X509Certificate) {
            final X509Certificate firstCert =
                    (X509Certificate) certificates[chainStart];
            final X509Certificate lastCert =
                    (X509Certificate) certificates[chainEnd - 1];

            final Principal subject = firstCert.getSubjectDN();
            final Principal issuer = lastCert.getIssuerDN();

            // extract subject & issuer's name
            final String subjectName = extractName(
                    subject.getName(),
                    "CN=",
                    "Unknown Subject");
            final String issuerName = extractName(
                    issuer.getName(),
                    "O=",
                    "Unknown Issuer");

            // dialog caption
            String caption = null;
            String body = "";

            // check if this is the case when both - the root CA and time of
            // signing is valid:
            if ((!rootCaIsNotValid) && (!timeIsNotValid)) {
                caption = StringUtils.format(
                        "The digital signature of {0} has been verified.",
                        description);

                body +=
                        "The digital signature has been validated by a trusted source. " +
                        "The security certificate was issued by a company that is trusted";

                // for timestamp info, add a message saying that certificate was
                // valid at the time of signing. And display date of signing.
                if (timestamp != null) {
                    // get the right date format for timestamp
                    final DateFormat df = DateFormat.getDateTimeInstance(
                            DateFormat.LONG,
                            DateFormat.LONG);
                    body += StringUtils.format(
                            " and was valid at the time of signing on {0}.",
                            df.format(timestamp));
                } else {
                    // add message about valid time of signing:
                    body +=
                            ", has not expired and is still valid.";
                }

                // we should add one more message here - disclaimer we used
                // to have.  This is to be displayed in the "All trusted"
                // case in the More Information dialog.
                body += StringUtils.format(
                        "Caution: \"{0}\" asserts that this content is safe.  " +
                        "You should only accept this content if you trust \"{1}\" to make that assertion.",
                        subjectName,
                        subjectName);
            } else {
                // this is the case when either publisher or time of signing
                // is invalid - check and add corresponding messages to
                // appropriate message arrays.

                // If root CA is not valid, add a caption and a message to the
                // securityAlerts array.
                if (rootCaIsNotValid) {
                    // Use different caption text for https and signed content
                    caption = StringUtils.format(
                            "The digital signature of {0} cannot be verified.",
                            description);

                    body += "The digital signature cannot be verified by a trusted source. " +
                            "Only continue if you trust the origin of the file. " +
                            "The security certificate was issued by a company that is not trusted.";
                } else {
                    caption = StringUtils.format(
                            "The digital signature of {0} has been verified.",
                            description);

                    // Same details for both
                    body += "The security certificate was issued by a company that is trusted.";
                }

                // now check if time of signing is valid.
                if (timeIsNotValid) {
                    // if no warnings yet, add the one that will show with the
                    // bullet in security warning dialog:
                    body += "The digital signature was generated with a trusted certificate but has expired or is not yet valid";
                } else {
                    // for timestamp info, add a message saying that certificate
                    // was valid at the time of signing
                    if (timestamp != null) {
                        // get the right date format for timestamp
                        final DateFormat df = DateFormat.getDateTimeInstance(
                                DateFormat.LONG,
                                DateFormat.LONG);
                        body += StringUtils.format(
                                "The security certificate was valid at the time of signing on {0}.",
                                df.format(timestamp));
                    } else {
                        body += "The security certificate has not expired and is still valid.";
                    }
                }
            }

            String message = StringUtils.format("<html><b>{0}</b><br>" +
                    "Subject: {1}<br>" +
                    "Issuer: {2}<br><br>" +
                    "{3}<br><br>" +
                    "Click OK to accept the certificate permanently, " +
                    "No to accept it temporary for this session, " +
                    "Cancel to reject the certificate.",
                    caption, subjectName, issuerName, body);

            int option = UiUtils.showYesNoCancelDialog(null, message, JOptionPane.NO_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                return CertificateAcceptanceStatus.ACCEPT_PERMANENTLY;
            } else if (option == JOptionPane.NO_OPTION) {
                return CertificateAcceptanceStatus.ACCEPT_FOR_THIS_SESSION;
            } else {
                return CertificateAcceptanceStatus.DENY;
            }
        }

        return CertificateAcceptanceStatus.DENY;
    }

    private static String extractName(
            final String nameString,
            final String prefix,
            final String defaultValue) {
        int i = nameString.indexOf(prefix);
        int j = 0;

        if (i < 0) {
            return defaultValue;
        } else {
            try {
                // shift to the beginning of the prefix text
                i = i + prefix.length();

                // check if it begins with a quote
                if (nameString.charAt(i) == '\"') {
                    // skip the quote
                    i = i + 1;

                    // search for another quote
                    j = nameString.indexOf('\"', i);
                } else {

                    // no quote, so search for comma
                    j = nameString.indexOf(',', i);
                }

                if (j < 0) {
                    return nameString.substring(i);
                } else {
                    return nameString.substring(i, j);
                }
            } catch (IndexOutOfBoundsException e) {
                return defaultValue;
            }
        }
    }

    public static enum CertificateAcceptanceStatus {

        ACCEPT_PERMANENTLY,
        ACCEPT_FOR_THIS_SESSION,
        DENY
    }
}
