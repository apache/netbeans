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

package org.netbeans.modules.subversion.remote.config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import org.netbeans.modules.proxy.Base64Encoder;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;

/**
 * Represents a Subversions file holding a X509Certificate for a realmstring.
 *
 * 
 */
public class CertificateFile extends SVNCredentialFile {

    private final static Key CERT = new Key(0, "ascii_cert"); // NOI18N
    private final static Key FAILURES = new Key(1, "failures"); // NOI18N
    private final static Key REALMSTRING = new Key(2, "svn:realmstring"); // NOI18N

    private static final String NEWLINE = System.getProperty("line.separator"); // NOI18N
    
    public CertificateFile(FileSystem fileSystem, X509Certificate cert, String realmString, int failures, boolean temporarily) throws CertificateEncodingException, IOException {
        super(getNBCertFile(fileSystem, realmString));
        setCert(cert);
        setFailures(failures);
        setRealmString(realmString);
        if(temporarily) {
            VCSFileProxySupport.deleteOnExit(getFile());
        }
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("Dm")
    private void setCert(X509Certificate cert) throws CertificateEncodingException {
        String encodedCert = Base64Encoder.encode(cert.getEncoded());                
        try {
            setValue(getCertKey(), encodedCert.getBytes("UTF-8")); //NOI18N
        } catch (UnsupportedEncodingException ex) {
            setValue(getCertKey(), encodedCert.getBytes());
        }
    }
        
    @Override
    protected void setRealmString(String realm) {
        setValue(getRealmstringKey(), realm);
    }

    @Override
    protected String getRealmString() {
        return getStringValue(getRealmstringKey());
    }

    private void setFailures(int failures) {
        setValue(getFailuresKey(), String.valueOf(failures));
    }

    public static VCSFileProxy getSystemCertFile(FileSystem fileSystem, String realmString) {
        VCSFileProxy file = VCSFileProxy.createFileProxy(SvnConfigFiles.getUserConfigPath(fileSystem), "auth/svn.ssl.server/" + getFileName(realmString)); // NOI18N
        return file.normalizeFile();
    }

    public static VCSFileProxy getNBCertFile(FileSystem fileSystem, String realmString) throws IOException {
        if (SvnConfigFiles.COPY_CONFIG_FILES) {
            VCSFileProxy file = VCSFileProxy.createFileProxy(SvnConfigFiles.getNBConfigPath(fileSystem), "auth/svn.ssl.server/" + getFileName(realmString)); // NOI18N
            return file.normalizeFile();
        } else {
            VCSFileProxy file = VCSFileProxy.createFileProxy(SvnConfigFiles.getUserConfigPath(fileSystem), "auth/svn.ssl.server/" + getFileName(realmString)); // NOI18N
            return file.normalizeFile();
        }
    }

    private Key getCertKey() {
        return getKey(CERT);
    }

    private Key getFailuresKey() {
        return getKey(FAILURES);
    }

    private Key getRealmstringKey() {
        return getKey(REALMSTRING);
    }

}
