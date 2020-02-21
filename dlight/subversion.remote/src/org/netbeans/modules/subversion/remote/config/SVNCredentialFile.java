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
