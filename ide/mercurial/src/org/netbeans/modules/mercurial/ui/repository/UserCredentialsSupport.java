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

import java.awt.Dialog;
import java.io.File;
import java.net.PasswordAuthentication;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondra Vrabec
 */
public class UserCredentialsSupport {

    private boolean saveValues;
    private boolean showSaveOption;

    /**
     * Opens a dialog asking user for his credentials and returns these credentials.
     * @param repositoryRoot repository root folder with .hg/hgrc as a child
     * @param url URL being contacted and requiring authentication
     * @param userName initial value of a username field
     * @return username and password credentials or null if user canceled the dialog
     */
    public PasswordAuthentication getUsernamePasswordCredentials(File repositoryRoot, String url, String userName) {
        PasswordAuthentication credentials = null;
        UserPasswordPanel panel = new UserPasswordPanel(showSaveOption);
        if (userName != null) {
            panel.tbUserName.setText(userName);
        }
        panel.lblMessage.setText(NbBundle.getMessage(UserPasswordPanel.class, "MSG_UserPasswordPanel_AuthRequired", url));
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(UserPasswordPanel.class, "CTL_UserPasswordPanel_AuthRequired"), //NOI18N
                true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            credentials = new PasswordAuthentication(panel.tbUserName.getText(), panel.tbPassword.getPassword());
            saveValues = showSaveOption && panel.cbRememberPassword.isSelected();
        }
        return credentials;
    }

    /**
     * Is save values checkbox checked?
     * @return
     */
    public boolean shallSaveValues () {
        return saveValues;
    }

    /**
     * 
     * @param showSaveOption if true then the dialog will show an option to enable saving the password.
     */
    public void setShowSaveOption(boolean showSaveOption) {
        this.showSaveOption = showSaveOption;
    }

}
