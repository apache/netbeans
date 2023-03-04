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
package org.netbeans.modules.mercurial.ui.repository;

import org.netbeans.modules.mercurial.config.Scrambler;
import java.net.URISyntaxException;

/**
 *
 * @author Tomas Stupka
 */
public class RepositoryConnection {
    
    private static final String RC_DELIMITER = "~=~"; // NOI18N
    
    private HgURL url;
    private String externalCommand;
    private boolean savePassword;
    
    public RepositoryConnection(String url) throws URISyntaxException {
        this(new HgURL(url), null, false);
    }
            
    public RepositoryConnection(String url,
                                String username,
                                String password,
                                String externalCommand,
                                boolean savePassword) throws URISyntaxException {
        this(new HgURL(url, username, password == null ? null : password.toCharArray()), externalCommand, savePassword);
    }

    public RepositoryConnection(HgURL url, String externalCommand, boolean savePassword) {
        this.url = url;
        this.externalCommand = externalCommand;
        this.savePassword = savePassword;
    }

    public HgURL getUrl() {
        return url;
    }

    String getUsername() {
        return url.getUsername();
    }

    char[] getPassword() {
        return url.getPassword();
    }

    public String getExternalCommand() {
        return externalCommand;
    }

    public boolean isSavePassword() {
        return savePassword;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;   
        }            
        if (getClass() != o.getClass()) {
            return false;
        }            
        
        final RepositoryConnection test = (RepositoryConnection) o;

        if (this.url != test.url && this.url != null && !this.url.equals(test.url)) {
            return false;
        }        
        return true;
    }
    
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + (this.url != null ? this.url.hashCode() : 0);        
        return hash;
    }

    @Override
    public String toString() {
        return url.toString();
    }

    public static String getString(RepositoryConnection rc) {
        String url = rc.url.toUrlStringWithoutUserInfo();
        String username = rc.getUsername();
        String extCommand = rc.getExternalCommand();

        StringBuffer sb = new StringBuffer();        
        sb.append(url);
        sb.append(RC_DELIMITER);
        if (username != null) {
            sb.append(username);
        }
        sb.append(RC_DELIMITER);
        sb.append("");                                              //NOI18N
        sb.append(RC_DELIMITER);
        if (extCommand != null) {
            sb.append(extCommand);
        }
        sb.append(RC_DELIMITER);        
        sb.append(RC_DELIMITER);
        return sb.toString();
    }
    
    public static RepositoryConnection parse(String str) throws URISyntaxException {
        String[] fields = str.split(RC_DELIMITER);
        int l = fields.length;
        String url          =           fields[0];
        String username     = l > 1 && !fields[1].equals("") ? fields[1] : null; // NOI18N
        String password     = l > 2 && !fields[2].equals("") ? Scrambler.getInstance().descramble(fields[2]) : null; // NOI18N
        String extCmd       = l > 3 && !fields[3].equals("") ? fields[3] : null; // NOI18N
        boolean save        = l > 4 && !fields[4].equals("") ? Boolean.parseBoolean(fields[4]) : true;
        return new RepositoryConnection(url,
                                        username,
                                        (username != null) ? password : null,
                                        extCmd,
                                        save);
    }
}
