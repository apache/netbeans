/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
