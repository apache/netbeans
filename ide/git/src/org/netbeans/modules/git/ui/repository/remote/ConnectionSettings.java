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
package org.netbeans.modules.git.ui.repository.remote;

import org.netbeans.libs.git.GitURI;

/**
 *
 * @author ondra
 */
public class ConnectionSettings {
    private char[] password = new char[0];
    private char[] passphrase = new char[0];
    private GitURI uri;
    private String identityFile;
    private boolean saveCredentials;
    private boolean privateKeyAuth;

    public ConnectionSettings (GitURI gitURI) {
        setUri(gitURI);
    }

    public String getUser () {
        return uri.getUser();
    }

    public void setUser (String username) {
        setUri(getUri().setUser(username));
    }

    public char[] getPassword () {
        return password;
    }

    public void setPassword (char[] password) {
        this.password = password;
    }

    public char[] getPassphrase () {
        return passphrase;
    }

    public void setPassphrase (char[] passphrase) {
        this.passphrase = passphrase;
    }

    public String getIdentityFile () {
        return identityFile;
    }

    public void setIdentityFile (String identityFilePath) {
        this.identityFile = identityFilePath;
    }

    public boolean isPrivateKeyAuth () {
        return privateKeyAuth;
    }

    public void setPrivateKeyAuth (boolean privateKeyAuth) {
        this.privateKeyAuth = privateKeyAuth;
    }

    public GitURI getUri () {
        return uri;
    }

    private void setUri (GitURI newUri) {
        uri = newUri;
    }

    public boolean isSaveCredentials () {
        return saveCredentials;
    }

    public void setSaveCredentials (boolean saveCredentials) {
        this.saveCredentials = saveCredentials;
    }

    public ConnectionSettings copy () {
        ConnectionSettings copy = new ConnectionSettings(getUri().setUser(null).setPass(null));
        copy.setIdentityFile(getIdentityFile());
        copy.setPassphrase(getPassphrase() == null ? null : getPassphrase().clone());
        copy.setPassword(getPassword() == null ? null : getPassword().clone());
        copy.setPrivateKeyAuth(isPrivateKeyAuth());
        copy.setSaveCredentials(isSaveCredentials());
        copy.setUser(getUser());
        return copy;
    }
    
}
