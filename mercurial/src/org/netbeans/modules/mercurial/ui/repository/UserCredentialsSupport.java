/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
