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
package org.netbeans.modules.git.client;

import java.util.Arrays;
import org.netbeans.libs.git.GitClientCallback;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.ui.repository.remote.ConnectionSettings;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class CredentialsCallback extends GitClientCallback {

    private String username;
    private char[] password;
    private String identityFile;
    private char[] passphrase;
    private boolean credentialsReady;
    private String lastUri = null;
    
    @Override
    public String askQuestion (String uri, String prompt) {
        String retval = null;
        if (prompt.toLowerCase().startsWith("password:")) { //NOI18N
            char[] pwd = getPassword(uri, prompt);
            if (pwd != null) {
                retval = new String(pwd);
                Arrays.fill(pwd, (char) 0);
            }
        } else {
            NotifyDescriptor.InputLine desc = new NotifyDescriptor.InputLine(prompt, 
                    NbBundle.getMessage(CredentialsCallback.class, "LBL_CredentialsCallback.question.title", uri) //NOI18N
            );
            Object dlgResult = DialogDisplayer.getDefault().notify(desc);
            retval = NotifyDescriptor.OK_OPTION == dlgResult ? desc.getInputText() : null;
        }
        return retval;
    }

    @Override
    public String getUsername (String uri, String prompt) {
        getCredentials(uri);
        return username;
    }

    private void getCredentials (String uri) {
        if (!credentialsReady || !uri.equals(lastUri)) {
            fetchCredentials(uri);
        }
    }

    @Override
    public char[] getPassword (String uri, String prompt) {
        getCredentials(uri);
        char[] pwd = null;
        if (password != null) {
            pwd = password.clone();
            Arrays.fill(password, (char) 0);
            credentialsReady = false;
        }
        return pwd;
    }

    @Override
    public char[] getPassphrase (String uri, String prompt) {
        getCredentials(uri);
        char[] pwd = null;
        if (passphrase != null) {
            pwd = passphrase.clone();
            Arrays.fill(passphrase, (char) 0);
            credentialsReady = false;
        }
        return pwd;
    }

    @Override
    public String getIdentityFile (String uri, String prompt) {
        getCredentials(uri);
        return identityFile;
    }

    @Override
    public Boolean askYesNoQuestion (String uri, String prompt) {
        return NotifyDescriptor.YES_OPTION == DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(prompt, 
                NbBundle.getMessage(CredentialsCallback.class, "LBL_CredentialsCallback.question.title", uri), //NOI18N
                NotifyDescriptor.YES_NO_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE));
    }
    
    private void fetchCredentials (String uri) {
        lastUri = uri;
        ConnectionSettings settings = GitModuleConfig.getDefault().getConnectionSettings(uri);
        if (settings == null) {
            if (uri.endsWith("/")) {
                uri = uri.substring(0, uri.length() - 1);
            } else {
                uri = uri + "/";
            }
            settings = GitModuleConfig.getDefault().getConnectionSettings(uri);
        }
        if (settings != null) {
            username = settings.getUser();
            if (settings.isPrivateKeyAuth()) {
                identityFile = settings.getIdentityFile();
                passphrase = settings.getPassphrase();
                password = null;
            } else {
                password = settings.getPassword();
                identityFile = null;
                passphrase = null;
            }
            credentialsReady = true;
        }
    }
}
