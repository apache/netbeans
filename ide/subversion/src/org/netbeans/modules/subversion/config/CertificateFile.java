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

package org.netbeans.modules.subversion.config;

import java.io.File;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import org.netbeans.modules.subversion.config.KVFile.Key;
import org.openide.filesystems.FileUtil;

/**
 * Represents a Subversions file holding a X509Certificate for a realmstring.
 *
 * @author Tomas Stupka
 */
public class CertificateFile extends SVNCredentialFile {

    private static final Key CERT = new Key(0, "ascii_cert"); // NOI18N
    private static final Key FAILURES = new Key(1, "failures"); // NOI18N
    private static final Key REALMSTRING = new Key(2, "svn:realmstring"); // NOI18N

    private static final String NEWLINE = System.getProperty("line.separator"); // NOI18N
    
    public CertificateFile(X509Certificate cert, String realmString, int failures, boolean temporarily) throws CertificateEncodingException {
        super(getNBCertFile(realmString));
        setCert(cert);
        setFailures(failures);
        setRealmString(realmString);
        if(temporarily) {
            getFile().deleteOnExit();
        }
    }

    private void setCert(X509Certificate cert) throws CertificateEncodingException {
        String encodedCert = Base64.getEncoder().encodeToString(cert.getEncoded());                
        setValue(getCertKey(), encodedCert.getBytes());
    }
        
    protected void setRealmString(String realm) {
        setValue(getRealmstringKey(), realm);
    }

    protected String getRealmString() {
        return getStringValue(getRealmstringKey());
    }

    private void setFailures(int failures) {
        setValue(getFailuresKey(), String.valueOf(failures));
    }

    public static File getSystemCertFile(String realmString) {
        File file = new File(SvnConfigFiles.getUserConfigPath() + "auth/svn.ssl.server/" + getFileName(realmString)); // NOI18N
        return FileUtil.normalizeFile(file);
    }

    public static File getNBCertFile(String realmString) {
        File file = new File(SvnConfigFiles.getNBConfigPath() + "auth/svn.ssl.server/" + getFileName(realmString)); // NOI18N
        return FileUtil.normalizeFile(file);
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
