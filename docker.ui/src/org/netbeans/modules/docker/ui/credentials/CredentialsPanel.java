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
package org.netbeans.modules.docker.ui.credentials;

import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.docker.api.Credentials;
import org.netbeans.modules.docker.ui.UiUtils;
import org.openide.NotificationLineSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class CredentialsPanel extends javax.swing.JPanel {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

    private final JPasswordField reference = new JPasswordField();

    private final JButton actionButton;

    private final Set<String> registries;

    private NotificationLineSupport messageLine;

    /**
     * Creates new form CredentialsDetailPanel
     */
    public CredentialsPanel(JButton actionButton, Set<String> registries) {
        initComponents();

        this.actionButton = actionButton;
        this.registries = registries;

        DefaultDocumentListener listener = new DefaultDocumentListener();
        registryTextField.getDocument().addDocumentListener(listener);
        usernameTextField.getDocument().addDocumentListener(listener);
        emailTextField.getDocument().addDocumentListener(listener);
    }

    public void setMessageLine(NotificationLineSupport messageLine) {
        this.messageLine = messageLine;
        validateInput();
    }

    @NbBundle.Messages({
        "MSG_EmptyRegistry=Registry must not be empty.",
        "MSG_ExistingRegistry=This registry is already defined.",
        "MSG_EmptyUsername=Username must not be empty.",
        "MSG_InvalidEmail=Email address does not seem to be valid"
    })
    private void validateInput() {
        if (messageLine == null) {
            return;
        }

        messageLine.clearMessages();
        actionButton.setEnabled(true);

        String registry = UiUtils.getValue(registryTextField);
        if (registry == null) {
            messageLine.setErrorMessage(Bundle.MSG_EmptyRegistry());
            actionButton.setEnabled(false);
            return;
        } else if (registries.contains(registry)) {
            messageLine.setErrorMessage(Bundle.MSG_ExistingRegistry());
            actionButton.setEnabled(false);
            return;
        }
        String username = UiUtils.getValue(usernameTextField);
        if (username == null) {
            messageLine.setErrorMessage(Bundle.MSG_EmptyUsername());
            actionButton.setEnabled(false);
            return;
        }
        String email = UiUtils.getValue(emailTextField);
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            messageLine.setWarningMessage(Bundle.MSG_InvalidEmail());
        }
    }

    public Credentials getCredentials() {
        return new Credentials(UiUtils.getValue(registryTextField),
                UiUtils.getValue(usernameTextField),
                passwordPasswordField.getPassword(),
                UiUtils.getValue(emailTextField));
    }

    public void setCredentials(Credentials credentials) {
        registryTextField.setEditable(false);
        registryTextField.setText(credentials.getRegistry());
        usernameTextField.setText(credentials.getUsername());
        if (credentials.getPassword() != null) {
            passwordPasswordField.setText(new String(credentials.getPassword()));
        }
        emailTextField.setText(credentials.getEmail());
    }

    private class DefaultDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            validateInput();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            validateInput();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            validateInput();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        registryLabel = new javax.swing.JLabel();
        registryTextField = new javax.swing.JTextField();
        usernameLabel = new javax.swing.JLabel();
        usernameTextField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordPasswordField = new javax.swing.JPasswordField();
        emailLabel = new javax.swing.JLabel();
        emailTextField = new javax.swing.JTextField();
        showPasswordCheckBox = new javax.swing.JCheckBox();

        registryLabel.setLabelFor(registryTextField);
        org.openide.awt.Mnemonics.setLocalizedText(registryLabel, org.openide.util.NbBundle.getMessage(CredentialsPanel.class, "CredentialsPanel.registryLabel.text")); // NOI18N

        usernameLabel.setLabelFor(usernameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(usernameLabel, org.openide.util.NbBundle.getMessage(CredentialsPanel.class, "CredentialsPanel.usernameLabel.text")); // NOI18N

        usernameTextField.setColumns(10);

        passwordLabel.setLabelFor(passwordPasswordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(CredentialsPanel.class, "CredentialsPanel.passwordLabel.text")); // NOI18N

        emailLabel.setLabelFor(emailTextField);
        org.openide.awt.Mnemonics.setLocalizedText(emailLabel, org.openide.util.NbBundle.getMessage(CredentialsPanel.class, "CredentialsPanel.emailLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(showPasswordCheckBox, org.openide.util.NbBundle.getMessage(CredentialsPanel.class, "CredentialsPanel.showPasswordCheckBox.text")); // NOI18N
        showPasswordCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                showPasswordCheckBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(usernameLabel)
                    .addComponent(registryLabel)
                    .addComponent(passwordLabel)
                    .addComponent(emailLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(registryTextField)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(passwordPasswordField, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(usernameTextField, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(showPasswordCheckBox)
                        .addGap(0, 101, Short.MAX_VALUE))
                    .addComponent(emailTextField))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(registryLabel)
                    .addComponent(registryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameLabel)
                    .addComponent(usernameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(showPasswordCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(emailLabel)
                    .addComponent(emailTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void showPasswordCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_showPasswordCheckBoxItemStateChanged
        if (showPasswordCheckBox.isSelected()) {
            passwordPasswordField.setEchoChar((char) 0);
        } else {
            passwordPasswordField.setEchoChar(reference.getEchoChar());
        }
    }//GEN-LAST:event_showPasswordCheckBoxItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel emailLabel;
    private javax.swing.JTextField emailTextField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JPasswordField passwordPasswordField;
    private javax.swing.JLabel registryLabel;
    private javax.swing.JTextField registryTextField;
    private javax.swing.JCheckBox showPasswordCheckBox;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JTextField usernameTextField;
    // End of variables declaration//GEN-END:variables
}
