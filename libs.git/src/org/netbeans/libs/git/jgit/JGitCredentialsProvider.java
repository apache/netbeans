/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
