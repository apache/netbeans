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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.subversion.config.KVFile.Key;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Represents a file holding the username and password credentials for a realmstring.
 *
 * @author Tomas Stupka
 */
public class PasswordFile extends SVNCredentialFile {

    private static final Key PASSTYPE_KEY = new Key(0, "passtype"); // NOI18N
    private static final Key PASSWORD_KEY = new Key(1, "password"); // NOI18N
    private static final Key REALMSTRING_KEY = new Key(2, "svn:realmstring"); // NOI18N
    private static final Key USERNAME_KEY = new Key(3, "username"); // NOI18N
    
    private final static String PASSTYPE_SIMPLE = "simple"; // NOI18N
    private static Map<String, PasswordFile> files;
        
    public PasswordFile (String realmString) {
        super(getFile(realmString));
    }

    private PasswordFile (File file) {
        super(file);
    }

    /**
     * Goes through the NetBeans Subversion modules configuration directory and looks
     * for a file holding the username and password for the given url.
     *
     * @param svnUrl the url
     * @return the file holding the username and password for the given url or null
     *         if nothing was found    
     */
    public static PasswordFile findFileForUrl(SVNUrl svnUrl) {
        if(files == null) {
            files = new HashMap<String, PasswordFile>(1);
        }
        PasswordFile pf = files.get(svnUrl.toString());
        if(pf == null) {
            pf = findFileForUrlIntern(svnUrl);
            if(pf != null) {
                files.put(svnUrl.toString(), pf);
            }
        }
        return pf;
    }

    private static PasswordFile findFileForUrlIntern(SVNUrl svnUrl) {
        // create our own realmstring  -
        String urlString = SvnUtils.ripUserFromHost(svnUrl.getHost());
        String realmString = "<" + svnUrl.getProtocol() + "://" + urlString + ">"; // NOI18N
        PasswordFile nbPasswordFile = new PasswordFile(realmString);

        if(!nbPasswordFile.getFile().exists()) {

            File configDir = new File(SvnConfigFiles.getUserConfigPath() + "/auth/svn.simple"); // NOI18N
            File[] files = configDir.listFiles();
            if(files==null) {
                return null;
            }
            for (int i = 0; i < files.length; i++) {
                PasswordFile passwordFile = new PasswordFile(files[i]);
                if(passwordFile.acceptSvnUrl(svnUrl) &&
                   passwordFile.getPasstype().equals(PASSTYPE_SIMPLE)) // windows likes to use wincryp, but we can accept only plain text
                {
                    // overwrites the value given by svn with our own, but there is no chance to get
                    // the realm string as svn does.
                    passwordFile.setRealmString(realmString);
                    return passwordFile;
                }
            }

            // no password file - let's create an empty one then...
            nbPasswordFile.setRealmString(realmString);
            nbPasswordFile.setPasstype(PASSTYPE_SIMPLE);
            nbPasswordFile.setPassword(""); // NOI18N
            nbPasswordFile.setUsername(""); // NOI18N
            return nbPasswordFile;

        } else {
            return nbPasswordFile;
        }
    }

    public void store() throws IOException {
        store(getFile(getRealmString()));
    }

    public String getPassword() {
        return getStringValue(getPasswordKey());
    }

    public String getUsername() {
        return getStringValue(getUsernameKey());
    }

    public void setPassword(String password) {
        setValue(getPasswordKey(), password);
    }

    public void setUsername(String username) {
        setValue(getUsernameKey(), username);
    }
    
    protected String getRealmString() {       
        return getStringValue(getRealmstringKey());
    }

    protected void setRealmString(String realm) {
        setValue(getRealmstringKey(), realm.getBytes());
    }

    private void setPasstype(String passtype) {
        setValue(getPasstypeKey(), passtype);
    }

    private String getPasstype() {
        return getStringValue(getPasstypeKey());
    }

    private boolean acceptSvnUrl(SVNUrl svnUrl) {
        if(svnUrl==null) {
            return false;
        }        
        String realmStrig = getRealmString();
        if(realmStrig==null || realmStrig.length() < 6 ) {
            // at least 'svn://'
            return false;
        }
        String urlString = SvnUtils.ripUserFromHost(svnUrl.getHost());
        return realmStrig.substring(1).startsWith(svnUrl.getProtocol() + "://" + urlString); // NOI18N
    }
    
    private static File getFile(String realmString) {
        return new File(SvnConfigFiles.getNBConfigPath() + "auth/svn.simple/" + getFileName(realmString)); // NOI18N
    }
    
    private Key getPasstypeKey() {
        return getKey(PASSTYPE_KEY);
    }

    private Key getPasswordKey() {
        return getKey(PASSWORD_KEY);
    }

    private Key getRealmstringKey() {
        return getKey(REALMSTRING_KEY);
    }

    private Key getUsernameKey() {
        return getKey(USERNAME_KEY);
    }

}
