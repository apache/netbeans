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

package org.netbeans.libs.git.jgit;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.netbeans.libs.git.GitClientCallback;

/**
 *
 * @author ondra
 */
public class JGitCredentialsProvider extends CredentialsProvider {
    private final GitClientCallback callback;
    private static final Logger LOG = Logger.getLogger(JGitCredentialsProvider.class.getName());

    public JGitCredentialsProvider (GitClientCallback callback) {
        this.callback = callback;
    }

    @Override
    public boolean isInteractive () {
        return true;
    }

    @Override
    public boolean supports (CredentialItem... credentialItems) {
        return true;
    }

    @Override
    public boolean get (URIish uriish, CredentialItem... items) throws UnsupportedCredentialItem {
        boolean retval = true;
        String uri = uriish.toString();
        for (CredentialItem item : items) {
            if (item instanceof CredentialItem.Username) {
                CredentialItem.Username credItem = (CredentialItem.Username) item;
                String username = callback.getUsername(uri, credItem.getPromptText());
                if (username == null) {
                    retval = false;
                } else {
                    credItem.setValue(username);
                }
            } else if (item instanceof CredentialItem.Password) {
                CredentialItem.Password credItem = (CredentialItem.Password) item;
                char[] password = callback.getPassword(uri, credItem.getPromptText());
                if (password == null) {
                    retval = false;
                } else {
                    credItem.setValue(password);
                }
            } else if (item instanceof CredentialItem.InformationalMessage) {
                LOG.log(Level.FINE, "Informational message: {0} - {1}", new Object[] { uri, item.getPromptText() });
            } else if (item instanceof CredentialItem.YesNoType) {
                CredentialItem.YesNoType credItem = (CredentialItem.YesNoType) item;
                Boolean value = callback.askYesNoQuestion(uri, credItem.getPromptText());
                if (value == null) {
                    retval = false;
                } else {
                    credItem.setValue(value);
                }
            } else if (item instanceof CredentialItem.StringType) {
                CredentialItem.StringType credItem = (CredentialItem.StringType) item;
                String answer;
                if (credItem instanceof IdentityFileItem) {
                    answer = callback.getIdentityFile(uri, credItem.getPromptText());
                } else if (credItem.getPromptText().toLowerCase().contains("password for")) { //NOI18N
                    char[] pwd = callback.getPassword(uri, credItem.getPromptText());
                    answer = pwd == null ? null : new String(pwd);
                } else if (credItem.getPromptText().toLowerCase().contains("passphrase for")) { //NOI18N
                    char[] pwd = callback.getPassphrase(uri, credItem.getPromptText());
                    answer = pwd == null ? null : new String(pwd);
                } else {
                    answer = callback.askQuestion(uri, credItem.getPromptText());
                }
                if (answer == null) {
                    retval = false;
                } else {
                    credItem.setValue(answer);
                }
            } else {
                LOG.log(Level.WARNING, "Unknown credential item: {0} - {1}:{2}", new Object[] { uri, item.getClass().getName(), item.getPromptText() });
            }
        }
        return retval;
    }

    public static class IdentityFileItem extends CredentialItem.StringType {

        public IdentityFileItem (String promptText, boolean maskValue) {
            super(promptText, maskValue);
        }
        
    }
}
