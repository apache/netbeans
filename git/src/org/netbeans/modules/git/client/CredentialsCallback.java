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
