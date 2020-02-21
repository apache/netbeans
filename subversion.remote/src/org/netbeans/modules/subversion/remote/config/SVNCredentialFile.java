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
package org.netbeans.modules.subversion.remote.config;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.Exceptions;

/**
 * Handles the Subversion credential files.
 *
 * 
 *
 */
public abstract class SVNCredentialFile extends KVFile {


    /**
     * Creates sa new instance
     */
    protected SVNCredentialFile(VCSFileProxy file) {
        super(file);
    }

    /**
     * Returns the filename for a realmString as a MD5 value in hex form.
     */
    @org.netbeans.api.annotations.common.SuppressWarnings("Dm")
    protected static String getFileName(String realmString) {
        assert realmString != null;        
        StringBuilder fileName = new StringBuilder();
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5"); // NOI18N
            try {
                md5.update(realmString.getBytes("UTF-8")); // NOI18N
            } catch (UnsupportedEncodingException ex) {
                md5.update(realmString.getBytes());
            }
            byte[] md5digest = md5.digest();
            for (int i = 0; i < md5digest.length; i++) {
                String hex = Integer.toHexString(md5digest[i] & 0x000000FF);
                fileName.append(hex);
                if(hex.length()==1) {
                    fileName.append('0');
                }
            }            
        } catch (NoSuchAlgorithmException e) {
            Subversion.LOG.log(Level.INFO, null, e); // should not happen
        }                        
        
        return fileName.toString();
    }    
    
    protected abstract String getRealmString();    
    protected abstract void setRealmString(String realm);        
}
