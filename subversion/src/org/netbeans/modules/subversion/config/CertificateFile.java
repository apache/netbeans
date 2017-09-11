/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.subversion.config;

import java.io.File;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import org.netbeans.modules.proxy.Base64Encoder;
import org.netbeans.modules.subversion.config.KVFile.Key;
import org.openide.filesystems.FileUtil;

/**
 * Represents a Subversions file holding a X509Certificate for a realmstring.
 *
 * @author Tomas Stupka
 */
public class CertificateFile extends SVNCredentialFile {

    private final static Key CERT = new Key(0, "ascii_cert"); // NOI18N
    private final static Key FAILURES = new Key(1, "failures"); // NOI18N
    private final static Key REALMSTRING = new Key(2, "svn:realmstring"); // NOI18N

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
        String encodedCert = Base64Encoder.encode(cert.getEncoded());                
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
