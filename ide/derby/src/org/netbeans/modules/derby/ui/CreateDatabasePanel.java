/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.derby.ui;

import java.io.File;
import java.util.Arrays;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.db.api.sql.SQLKeywords;
import org.netbeans.modules.derby.DerbyOptions;
import org.netbeans.modules.derby.api.DerbyDatabases;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
/**
 *
 * @author Andrei Badea
 */
public class CreateDatabasePanel extends javax.swing.JPanel {

    private File derbySystemHome;
    private DialogDescriptor descriptor;

    private DocumentListener docListener = new DocumentListener() {
        
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            validateInput();
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            validateInput();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            validateInput();
        }
    };
    
    public CreateDatabasePanel(String derbySystemHome) {
        this.derbySystemHome = new File(derbySystemHome);
        initComponents();
        databaseNameTextField.getDocument().addDocumentListener(docListener);
        userTextField.getDocument().addDocumentListener(docListener);
        password.getDocument().addDocumentListener(docListener);
        retypePassword.getDocument().addDocumentListener(docListener);
        updateLocation();
    }
    
    public void setDialogDescriptor(DialogDescriptor descriptor) {
        this.descriptor = descriptor;
        validateInput();
    }

    public String getDatabaseName() {
        return databaseNameTextField.getText().trim();
    }
    
    public String getUser() {
        String user = userTextField.getText().trim();
        return user.length() > 0 ? user : null;
    }
    
    public String getPassword() {
        String pw = new String(this.password.getPassword());
        return pw.length() > 0 ? pw : null;
    }

    public String getRetypePassword() {
        String pw2 = new String(this.retypePassword.getPassword());
        return pw2.length() > 0 ? pw2 : null;
    }

    public boolean matchPasswords() {
        return Arrays.equals(this.retypePassword.getPassword(), this.password.getPassword());
    }

    public void setIntroduction() {
        String info = NbBundle.getMessage(CreateDatabasePanel.class, "INFO_DatabaseNameEmpty");
        descriptor.getNotificationLineSupport().setInformationMessage(info);
        descriptor.setValid(false);
    }

    private void validateInput() {
        if (descriptor == null) {
            return;
        }
        
        String error = null;
        String warning = null;
        String info = null;
        
        String databaseName = getDatabaseName();
        String user = getUser();
        int illegalChar = DerbyDatabases.getFirstIllegalCharacter(databaseName);
        // workaround for issue 69265
        int unsupportedChar = getFirstUnsupportedCharacter(databaseName);
        
        retypePassword.setEnabled(getPassword() != null);
        retypePasswordLabel.setEnabled(getPassword() != null);
        
        if (databaseName.length() <= 0) { // NOI18N
            warning = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DatabaseNameEmpty");
        } else if (illegalChar >= 0) {
            error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DatabaseNameIllegalChar", Character.toString((char)illegalChar));
        } else if (databaseName.length() > 0 && new File(derbySystemHome, databaseName).exists()) { // NOI18N
            error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DatabaseDirectoryExists", databaseName);
        } else if (unsupportedChar >= 0) {
            error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DatabaseNameUnsupportedChar", Character.toString((char)unsupportedChar));
        } else if (user != null && !isSql92Identifier(user)) { // NOI18N
            error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_UserNameNotSqlIdentifier", user); // NOI18N
        } else if (user != null && SQLKeywords.isSQL99ReservedKeyword(user)) {
            error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_UserNameIsSqlKeyword", user); // NOI18N
        } else if (getUser() == null || getPassword() == null) {
            info = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_UserNamePasswordRecommended");
        } else if (getUser() != null && getPassword() != null && getRetypePassword() == null) {
            warning = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_NeedRetypePassword");
        } else if (! matchPasswords()) {
            error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_PasswordsDontMatch");
        }
        
        if (error != null) {
            descriptor.getNotificationLineSupport().setErrorMessage(error);
            descriptor.setValid(false);
        } else if (warning != null) {
            descriptor.getNotificationLineSupport().setWarningMessage(warning);
            descriptor.setValid(false);
        } else if (info != null) {
            descriptor.getNotificationLineSupport().setInformationMessage(info);
            descriptor.setValid(true);
        } else {
            descriptor.getNotificationLineSupport().clearMessages();
            descriptor.setValid(true);
        }
    }
    
    static boolean isSql92Identifier(String s) {
        if (s == null || s.isEmpty()) {
            // empty value
            return false;
        } else if (s.matches("[\\w&&[^\\d_]](_|\\d|\\w)*")) { //NOI18N
            // ordinary identifier
            return true;
        } else if (s.matches("\\\".+\\\"")) { //NOI18N
            boolean unpairedQuite = false;
            // check all quotes are doubled
            for (int i = 1; i < s.length() - 1; i++) {
                if (s.charAt(i) == '"') {
                    unpairedQuite = !unpairedQuite;
                } else if (unpairedQuite) {
                    // invalid delemited identifier
                    return false;
                }
            }
            // delemited identifier
            return !unpairedQuite;
        } else {
            // other cases, not a identifier
            return false;
        }
    }

    private void updateLocation() {
        databaseLocationValueLabel.setText(derbySystemHome.getAbsolutePath());
    }
    
    private int getFirstUnsupportedCharacter(String databaseName) {
        for (int i = 0; i < databaseName.length(); i++) {
            char ch = databaseName.charAt(i);
            if (ch < '\u0020' || ch > '\u00ff') {
                return (int)ch;
            }
        }
        return -1;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        databaseNameLabel = new javax.swing.JLabel();
        databaseNameTextField = new javax.swing.JTextField();
        databaseLocationLabel = new javax.swing.JLabel();
        userLabel = new javax.swing.JLabel();
        userTextField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();
        propertiesButton = new javax.swing.JButton();
        databaseLocationValueLabel = new javax.swing.JLabel();
        retypePasswordLabel = new javax.swing.JLabel();
        retypePassword = new javax.swing.JPasswordField();

        databaseNameLabel.setLabelFor(databaseNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(databaseNameLabel, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "LBL_DatabaseName")); // NOI18N

        databaseLocationLabel.setLabelFor(databaseLocationValueLabel);
        org.openide.awt.Mnemonics.setLocalizedText(databaseLocationLabel, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "LBL_DatabaseLocation")); // NOI18N

        userLabel.setLabelFor(userTextField);
        org.openide.awt.Mnemonics.setLocalizedText(userLabel, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "LBL_UserName")); // NOI18N

        userTextField.setColumns(15);

        passwordLabel.setLabelFor(password);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "LBL_Password")); // NOI18N

        password.setColumns(15);

        org.openide.awt.Mnemonics.setLocalizedText(propertiesButton, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "LBL_Properties")); // NOI18N
        propertiesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                propertiesButtonActionPerformed(evt);
            }
        });

        databaseLocationValueLabel.setToolTipText(derbySystemHome.getAbsolutePath());

        retypePasswordLabel.setLabelFor(retypePassword);
        org.openide.awt.Mnemonics.setLocalizedText(retypePasswordLabel, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "LBL_RetypePassword")); // NOI18N

        retypePassword.setColumns(15);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(databaseNameLabel)
                    .addComponent(passwordLabel)
                    .addComponent(userLabel)
                    .addComponent(databaseLocationLabel)
                    .addComponent(retypePasswordLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(retypePassword, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                    .addComponent(databaseNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                    .addComponent(password, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                    .addComponent(userTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                    .addComponent(databaseLocationValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(propertiesButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(databaseNameLabel)
                    .addComponent(databaseNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(userLabel)
                    .addComponent(userTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(passwordLabel)
                    .addComponent(password, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER, false)
                    .addComponent(retypePasswordLabel)
                    .addComponent(retypePassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(databaseLocationLabel)
                    .addComponent(databaseLocationValueLabel)
                    .addComponent(propertiesButton))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {databaseNameTextField, password, userTextField});

        databaseNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "ACSD_CreateDatabasePanel_databaseNameTextField")); // NOI18N
        userTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "ACSD_CreateDatabasePanel_userTextField")); // NOI18N
        password.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "ACSD_CreateDatabasePanel_passwordTextField")); // NOI18N
        propertiesButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "ACSN_CreateDatabasePanel_propertiesButton")); // NOI18N
        propertiesButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "ACSD_CreateDatabasePanel_propertiesButton")); // NOI18N
        databaseLocationValueLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "ACSN_CreateDatabasePanel_databaseLocationValueLabel")); // NOI18N
        databaseLocationValueLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "ACSD_CreateDatabasePanel_databaseLocationValueLabel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void propertiesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_propertiesButtonActionPerformed
        DerbyPropertiesPanel.showDerbyProperties();
        String newLocation = DerbyOptions.getDefault().getSystemHome();
        databaseLocationValueLabel.setText(newLocation);
        databaseLocationValueLabel.setToolTipText(newLocation);
}//GEN-LAST:event_propertiesButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel databaseLocationLabel;
    public javax.swing.JLabel databaseLocationValueLabel;
    public javax.swing.JLabel databaseNameLabel;
    public javax.swing.JTextField databaseNameTextField;
    public javax.swing.JPasswordField password;
    public javax.swing.JLabel passwordLabel;
    public javax.swing.JButton propertiesButton;
    public javax.swing.JPasswordField retypePassword;
    public javax.swing.JLabel retypePasswordLabel;
    public javax.swing.JLabel userLabel;
    public javax.swing.JTextField userTextField;
    // End of variables declaration//GEN-END:variables
    
}
