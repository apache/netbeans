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

 /*
 * NewJPanel.java
 *
 * Created on 27.04.2010, 22:10:44
 */
package org.netbeans.modules.nativeexecution.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
//import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;
import org.netbeans.modules.nativeexecution.api.ui.util.ValidateablePanel;
import org.netbeans.modules.nativeexecution.api.util.Authentication;
import org.netbeans.modules.nativeexecution.api.util.AuthenticationUtils;
import org.netbeans.modules.nativeexecution.support.ui.api.FileSelectorField;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;

/**
 *
 * @author ak119685
 */
public class AuthenticationSettingsPanel extends ValidateablePanel {

    private final ExecutionEnvironment env;
    private final Task validationTask;
    private String problem;
    private final Authentication auth;
    private final AuthenticationListModel authListModel;

    public AuthenticationSettingsPanel(Authentication auth, boolean showClearPwdButton) {
        this.env = auth.getEnv();
        this.auth = auth;
        initComponents();

        pwdClearButton.setVisible(showClearPwdButton);
        pwdStoredLbl.setVisible(showClearPwdButton);

        if (env != null) {
            loginLabel.setText(env.getUser() + "@" + env.getHost() + // NOI18N
                    ((env.getSSHPort() == 22) ? "" : env.getSSHPort())); // NOI18N
        } else {
            loginPanel.setVisible(false);
        }

        if (auth.getType() == Authentication.Type.SSH_KEY) {
            keyRadioButton.setSelected(true);
            pwdRadioButton.setSelected(false);
        } else {
            keyRadioButton.setSelected(false);
            pwdRadioButton.setSelected(true);
        }

        keyFileFld.setText(auth.getSSHKeyFile());

        if (env != null) {
            boolean stored = PasswordManager.getInstance().isRememberPassword(env);
            pwdClearButton.setEnabled(stored);
            pwdStoredLbl.setVisible(stored);
        }

        validationTask = new RequestProcessor("", 1).create(new ValidationTask(), true);

        keyFileFld.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                validationTask.schedule(0);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validationTask.schedule(0);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validationTask.schedule(0);
            }
        });
        
        connectionSettingsPanel.setVisible(false);
        connectionSettings.setSelected(false);
        connectionSettings.setIcon(getCollapsedIcon());
        
        authListModel = new AuthenticationListModel(auth.getAuthenticationMethods());
        authenticationsList.setModel(authListModel);
        authenticationsList.setCellRenderer(new AuthentificationCheckboxListRenderer());
        authenticationsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                JList<AuthenticationCheckboxListItem> list = (JList<AuthenticationCheckboxListItem>) event.getSource();
                int index = list.locationToIndex(event.getPoint());
                if (index != -1) {
                    Rectangle checkBounds = ((AuthentificationCheckboxListRenderer)list.getCellRenderer()).check.getBounds();
                    if (event.getPoint().x <= checkBounds.width) {
                        AuthenticationCheckboxListItem item = list.getModel().getElementAt(index);
                        item.setSelected(!item.isSelected());
                        list.repaint(list.getCellBounds(index, index));
                    }
                }
            }
        });
        authenticationsList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_SPACE) {
                    AuthenticationCheckboxListItem item = authenticationsList.getSelectedValue();
                    if (item != null) {
                        int index = authenticationsList.getSelectedIndex();
                        item.setSelected(!item.isSelected());
                        authenticationsList.repaint(authenticationsList.getCellBounds(index, index));
                    }
                }
            }
        });
        authenticationsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                int index = authenticationsList.getSelectedIndex();
                if (index == -1) {
                    upButton.setEnabled(false);
                    downButton.setEnabled(false);
                    return;
                }
                if (index >= 0 && index < authListModel.getSize() - 1) {
                    downButton.setEnabled(true);
                } else {
                    downButton.setEnabled(false);
                }
                if (index >= 1 && index < authListModel.getSize()) {
                    upButton.setEnabled(true);
                } else {
                    upButton.setEnabled(false);
                }
            }
        });
        timeoutSpinner.setModel(new SpinnerNumberModel(auth.getTimeout(), 1, 500, 1));
        timeoutSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Number number = ((SpinnerNumberModel)timeoutSpinner.getModel()).getNumber();
                AuthenticationSettingsPanel.this.auth.setTimeout(number.intValue());
            }
        });
        enableControls();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        pwdRadioButton.setEnabled(enabled);
        keyRadioButton.setEnabled(enabled);
        enableKeyField();
    }

    private void resetAuthentificationListModel(Authentication.MethodList methods) {
        authListModel.resetModel(methods);
        authenticationsList.clearSelection();
        upButton.setEnabled(false);
        downButton.setEnabled(false);
        authenticationsList.repaint(authenticationsList.getCellBounds(0, authListModel.getSize()-1));
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        loginPanel = new javax.swing.JPanel();
        loginLabel = new javax.swing.JLabel();
        authPanel = new javax.swing.JPanel();
        pwdRadioButton = new javax.swing.JRadioButton();
        pwdStoredLbl = new javax.swing.JLabel();
        pwdClearButton = new javax.swing.JButton();
        keyRadioButton = new javax.swing.JRadioButton();
        keyFileFld = Utilities.isWindows() ? new JTextField() : new FileSelectorField(new SSHKeyFileCompletionProvider());
        keyBrowseButton = new javax.swing.JButton();
        connectionSettings = new javax.swing.JCheckBox();
        connectionSettingsPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        authenticationsList = new javax.swing.JList<>();
        downButton = new javax.swing.JButton();
        preferredAuthenticationsLabel = new javax.swing.JLabel();
        upButton = new javax.swing.JButton();
        timeoutLabel = new javax.swing.JLabel();
        timeoutSpinner = new javax.swing.JSpinner();

        loginPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.loginPanel.border.title"))); // NOI18N

        loginLabel.setText(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.loginLabel.text")); // NOI18N

        javax.swing.GroupLayout loginPanelLayout = new javax.swing.GroupLayout(loginPanel);
        loginPanel.setLayout(loginPanelLayout);
        loginPanelLayout.setHorizontalGroup(
            loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(loginLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        loginPanelLayout.setVerticalGroup(
            loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginPanelLayout.createSequentialGroup()
                .addComponent(loginLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        authPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.authPanel.border.title"))); // NOI18N

        buttonGroup1.add(pwdRadioButton);
        pwdRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(pwdRadioButton, org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.pwdRadioButton.text")); // NOI18N
        pwdRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pwdRadioButtonActionPerformed(evt);
            }
        });

        pwdStoredLbl.setText(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.pwdStoredLbl.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(pwdClearButton, org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.pwdClearButton.text_1")); // NOI18N
        pwdClearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pwdClearButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(keyRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(keyRadioButton, org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.keyRadioButton.text")); // NOI18N
        keyRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyRadioButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(keyBrowseButton, org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.keyBrowseButton.text_1")); // NOI18N
        keyBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyBrowseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(connectionSettings, org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.connectionSettings.text")); // NOI18N
        connectionSettings.setToolTipText(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.connectionSettings.toolTipText")); // NOI18N
        connectionSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectionSettingsActionPerformed(evt);
            }
        });

        authenticationsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        authenticationsList.setToolTipText(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.authenticationsList.toolTipText")); // NOI18N
        jScrollPane1.setViewportView(authenticationsList);

        org.openide.awt.Mnemonics.setLocalizedText(downButton, org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.downButton.text")); // NOI18N
        downButton.setToolTipText(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.downButton.toolTipText")); // NOI18N
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        preferredAuthenticationsLabel.setLabelFor(authenticationsList);
        org.openide.awt.Mnemonics.setLocalizedText(preferredAuthenticationsLabel, org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.preferredAuthenticationsLabel.text")); // NOI18N
        preferredAuthenticationsLabel.setToolTipText(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.preferredAuthenticationsLabel.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(upButton, org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.upButton.text")); // NOI18N
        upButton.setToolTipText(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.upButton.toolTipText")); // NOI18N
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        timeoutLabel.setLabelFor(timeoutSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(timeoutLabel, org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.timeoutLabel.text")); // NOI18N
        timeoutLabel.setToolTipText(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.timeoutLabel.toolTipText")); // NOI18N

        timeoutSpinner.setToolTipText(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.timeoutSpinner.toolTipText")); // NOI18N

        javax.swing.GroupLayout connectionSettingsPanelLayout = new javax.swing.GroupLayout(connectionSettingsPanel);
        connectionSettingsPanel.setLayout(connectionSettingsPanelLayout);
        connectionSettingsPanelLayout.setHorizontalGroup(
            connectionSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connectionSettingsPanelLayout.createSequentialGroup()
                .addComponent(preferredAuthenticationsLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(connectionSettingsPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane1)
                .addGap(18, 18, 18)
                .addGroup(connectionSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(upButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(downButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addGroup(connectionSettingsPanelLayout.createSequentialGroup()
                .addComponent(timeoutLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timeoutSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        connectionSettingsPanelLayout.setVerticalGroup(
            connectionSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connectionSettingsPanelLayout.createSequentialGroup()
                .addComponent(preferredAuthenticationsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(connectionSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(connectionSettingsPanelLayout.createSequentialGroup()
                        .addComponent(upButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(downButton))
                    .addGroup(connectionSettingsPanelLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(connectionSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeoutLabel)
                    .addComponent(timeoutSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 12, Short.MAX_VALUE))
        );

        downButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.downButton.AccessibleContext.accessibleName")); // NOI18N
        downButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.downButton.AccessibleContext.accessibleDescription")); // NOI18N
        preferredAuthenticationsLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.preferredAuthenticationsLabel.AccessibleContext.accessibleName")); // NOI18N
        preferredAuthenticationsLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.preferredAuthenticationsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        upButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.upButton.AccessibleContext.accessibleName")); // NOI18N
        upButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.upButton.AccessibleContext.accessibleDescription")); // NOI18N
        timeoutLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.timeoutLabel.AccessibleContext.accessibleName")); // NOI18N
        timeoutLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.timeoutLabel.AccessibleContext.accessibleDescription")); // NOI18N
        timeoutSpinner.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.timeoutSpinner.AccessibleContext.accessibleName")); // NOI18N
        timeoutSpinner.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.timeoutSpinner.AccessibleContext.accessibleDescription")); // NOI18N

        javax.swing.GroupLayout authPanelLayout = new javax.swing.GroupLayout(authPanel);
        authPanel.setLayout(authPanelLayout);
        authPanelLayout.setHorizontalGroup(
            authPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(authPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(authPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(connectionSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(authPanelLayout.createSequentialGroup()
                        .addGroup(authPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(authPanelLayout.createSequentialGroup()
                                .addGroup(authPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(pwdRadioButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(keyRadioButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(authPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(authPanelLayout.createSequentialGroup()
                                        .addGap(184, 184, 184)
                                        .addComponent(pwdStoredLbl, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE))
                                    .addGroup(authPanelLayout.createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addComponent(keyFileFld))))
                            .addComponent(connectionSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(authPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(pwdClearButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(keyBrowseButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        authPanelLayout.setVerticalGroup(
            authPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(authPanelLayout.createSequentialGroup()
                .addGroup(authPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pwdRadioButton)
                    .addComponent(pwdClearButton)
                    .addComponent(pwdStoredLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(authPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keyRadioButton)
                    .addComponent(keyBrowseButton)
                    .addComponent(keyFileFld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(connectionSettings)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(connectionSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        connectionSettings.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.connectionSettings.AccessibleContext.accessibleName")); // NOI18N
        connectionSettings.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.connectionSettings.AccessibleContext.accessibleDescription")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(loginPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(authPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(loginPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(authPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void keyRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyRadioButtonActionPerformed
        resetAuthentificationListModel(Authentication.SSH_KEY_METHODS);
        enableControls();
        keyFileFld.requestFocus();
    }//GEN-LAST:event_keyRadioButtonActionPerformed

    private void pwdClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pwdClearButtonActionPerformed
        PasswordManager.getInstance().forceClearPassword(env);
        pwdStoredLbl.setVisible(false);
        pwdClearButton.setEnabled(false);
    }//GEN-LAST:event_pwdClearButtonActionPerformed

    private void keyBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyBrowseButtonActionPerformed
        JFileChooser chooser = new SSHKeyFileChooser(keyFileFld.getText());
        int result = chooser.showOpenDialog(this);

        if (JFileChooser.APPROVE_OPTION == result) {
            keyFileFld.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_keyBrowseButtonActionPerformed

    private void pwdRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pwdRadioButtonActionPerformed
        resetAuthentificationListModel(Authentication.PASSWORD_METHODS);
        enableControls();
    }//GEN-LAST:event_pwdRadioButtonActionPerformed

    private void connectionSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectionSettingsActionPerformed
        connectionSettingsPanel.setVisible(connectionSettings.isSelected());
        connectionSettings.setIcon(connectionSettings.isSelected() ? getExpandedIcon() : getCollapsedIcon());
    }//GEN-LAST:event_connectionSettingsActionPerformed

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        int selectedIndex = authenticationsList.getSelectedIndex();
        if (selectedIndex <= 0) {
            return;
        }
        authListModel.swapElement(selectedIndex, selectedIndex-1);
        authenticationsList.repaint(authenticationsList.getCellBounds(selectedIndex-1, selectedIndex));
        authenticationsList.setSelectedIndex(selectedIndex-1);
    }//GEN-LAST:event_upButtonActionPerformed

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        int selectedIndex = authenticationsList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= authListModel.getSize() -1 ) {
            return;
        }
        authListModel.swapElement(selectedIndex, selectedIndex+1);
        authenticationsList.repaint(authenticationsList.getCellBounds(selectedIndex, selectedIndex+1));
        authenticationsList.setSelectedIndex(selectedIndex+1);
    }//GEN-LAST:event_downButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel authPanel;
    private javax.swing.JList<AuthenticationCheckboxListItem> authenticationsList;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox connectionSettings;
    private javax.swing.JPanel connectionSettingsPanel;
    private javax.swing.JButton downButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton keyBrowseButton;
    private javax.swing.JTextField keyFileFld;
    private javax.swing.JRadioButton keyRadioButton;
    private javax.swing.JLabel loginLabel;
    private javax.swing.JPanel loginPanel;
    private javax.swing.JLabel preferredAuthenticationsLabel;
    private javax.swing.JButton pwdClearButton;
    private javax.swing.JRadioButton pwdRadioButton;
    private javax.swing.JLabel pwdStoredLbl;
    private javax.swing.JLabel timeoutLabel;
    private javax.swing.JSpinner timeoutSpinner;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables

    private void enableControls() {
        enableKeyField();
        validationTask.schedule(0);
    }

    private void enableKeyField() {
        enableKeyField(authListModel.hasPublicKey());
    }

    private void enableKeyField(boolean enable) {
        keyFileFld.setEnabled(enable);
        keyBrowseButton.setEnabled(enable);
    }

    @Override
    public boolean hasProblem() {
        return problem != null;
    }

    @Override
    public String getProblem() {
        return problem;
    }

    @Override
    public void applyChanges(Object customData) {
        //ConnectionManagerAccessor access = ConnectionManagerAccessor.getDefault();

        if (customData instanceof ExecutionEnvironment) {
            ExecutionEnvironment e = (ExecutionEnvironment) customData;
            Authentication a = Authentication.getFor(e);
            a.setTimeout(auth.getTimeout());
            authListModel.storeAuthenticationMethods(auth);
            a.setAuthenticationMethods(auth.getAuthenticationMethods());
            if (auth.getType() == Authentication.Type.SSH_KEY) {
                a.setSSHKeyFile(auth.getSSHKeyFile());
            } else {
                a.setPassword();
            }
            a.store();
            AuthenticationUtils.changeAuth(e, a);
        } else if (env != null) {
            authListModel.storeAuthenticationMethods(auth);
            auth.store();
            AuthenticationUtils.changeAuth(env, auth);
        }
    }

    private class ValidationTask implements Runnable {

        @Override
        public void run() {
            validate();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    fireChange();
                }
            });
        }

        private boolean validate() {
            authListModel.storeAuthenticationMethods(auth);
// It would be fine to have 3-rd "Custom" button, then the code below would work as a charm
//            if (auth.getAuthenticationMethods().equals(Authentication.PASSWORD_METHODS)) {
//                AuthenticationSettingsPanel.this.pwdRadioButton.setSelected(true);
//            } else if (auth.getAuthenticationMethods().equals(Authentication.SSH_KEY_METHODS)) {
//                AuthenticationSettingsPanel.this.keyRadioButton.setSelected(true);
//            } else
            if (authListModel.hasPublicKey()) {
                AuthenticationSettingsPanel.this.keyRadioButton.setSelected(true);
            } else {
                AuthenticationSettingsPanel.this.pwdRadioButton.setSelected(true);
            }
            if (authListModel.isEmpty()) {
                problem = NbBundle.getMessage(AuthenticationSettingsPanel.class,
                        "AuthenticationSettingsPanel.validationError.emptyMethodList.text"); //NOI18N
                return false;
            }
            boolean needskeyFile = authListModel.hasPublicKey();
            if (!needskeyFile) {
                problem = null;
                auth.setPassword();
                return true;
            }

            String keyFile = keyFileFld.getText();
            if (keyFile.length() == 0) {
                problem = NbBundle.getMessage(AuthenticationSettingsPanel.class,
                        "AuthenticationSettingsPanel.validationError.emptyKey.text");//NOI18N
                return false;
            }

            final File file = new File(keyFile);

            if (file.isDirectory()) {
                problem = NbBundle.getMessage(AuthenticationSettingsPanel.class,
                        "AuthenticationSettingsPanel.validationError.isDirectory.text", keyFile);//NOI18N
                return false;
            }

            if (!file.exists()) {
                problem = NbBundle.getMessage(AuthenticationSettingsPanel.class,
                        "AuthenticationSettingsPanel.validationError.fileNotFound.text", keyFile);//NOI18N
                return false;
            }

            if (!file.canRead()) {
                problem = NbBundle.getMessage(AuthenticationSettingsPanel.class,
                        "AuthenticationSettingsPanel.validationError.fileNotReadable.text", keyFile);//NOI18N
                return false;
            }

            if (!Authentication.isValidSSHKeyFile(file.getAbsolutePath())) {
                problem = NbBundle.getMessage(AuthenticationSettingsPanel.class,
                        "AuthenticationSettingsPanel.validationError.invalidKey.text", keyFile);//NOI18N
                return false;
            }

            auth.setSSHKeyFile(file.getAbsolutePath());
            problem = null;
            return true;
        }
    }

    private static final boolean isGtk = "GTK".equals(UIManager.getLookAndFeel().getID()); //NOI18N

    static Icon getExpandedIcon() {
        return UIManager.getIcon(isGtk ? "Tree.gtk_expandedIcon" : "Tree.expandedIcon"); //NOI18N
    }

    static Icon getCollapsedIcon() {
        return UIManager.getIcon(isGtk ? "Tree.gtk_collapsedIcon" : "Tree.collapsedIcon"); //NOI18N
    }

    private class AuthenticationCheckboxListItem {
        private final Authentication.Method method;
        private boolean isSelected;

        public AuthenticationCheckboxListItem(Authentication.Method method, boolean selected) {
            this.method = method;
            this.isSelected = selected;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
            if (method.hasKeyFile()) {
                enableKeyField(isSelected);
            }
            validationTask.schedule(0);
        }
        
        public String getDisplayName() {
            return method.getDisplayName();
        }

        @Override
        public String toString() {
            return method.toString() + ' ' + isSelected; //NOI18N
        }

        public Pair<Authentication.Method, Boolean> toPair() {
            return Pair.of(method, isSelected);
        }
    }

    private static class AuthentificationCheckboxListRenderer extends JPanel implements ListCellRenderer<AuthenticationCheckboxListItem> {
        private final JCheckBox check;
        private final JLabel label;
        private final Border noFocusBorder = new EmptyBorder(1,1,1,1);
        private final Border focusBorder = new EtchedBorder();
        
        private AuthentificationCheckboxListRenderer() {
            check = new JCheckBox();
            label = new JLabel();
            this.setLayout(new BorderLayout());
            this.add(check, BorderLayout.WEST);
            this.add(label, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends AuthenticationCheckboxListItem> list, AuthenticationCheckboxListItem value,
                int index, boolean isSelected, boolean cellHasFocus) {
            final Color bc;
            final Color fc;
            if (isSelected) {
                bc = UIManager.getColor("List.selectionBackground"); //NOI18N
                fc = UIManager.getColor("List.selectionForeground"); //NOI18N
            } else {
                bc = list.getBackground();
                fc = list.getForeground();
            }
            setBackground(bc); // NOI18N
            setForeground(fc); // NOI18N
            
            label.setBackground(bc);
            label.setForeground(fc);
            label.setText(value.getDisplayName());
            label.setFont(list.getFont());
            
            check.setSelected(value.isSelected());
            check.setBackground(bc);
            check.setForeground(fc);
            check.setEnabled(list.isEnabled());
            
            Border border;
            if (cellHasFocus) {
                border = focusBorder;
            } else {
                border = noFocusBorder;
            }
            setBorder(border);
            
            return this;
        }
    }
    
    private final class AuthenticationListModel extends AbstractListModel<AuthenticationCheckboxListItem> {
        private final ArrayList<AuthenticationCheckboxListItem> list = new ArrayList<>(4);
        private AuthenticationListModel(Authentication.MethodList methodsList) {
            if (methodsList == null || methodsList.isEmpty()) {
                resetModel(Authentication.DEFAULT_METHODS);
            } else {
                resetModel(methodsList);
            }
        }

        @Override
        public int getSize() {
            return list.size();
        }

        public boolean isEmpty() {
            for (AuthenticationCheckboxListItem item : list) {
                if (item.isSelected) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public AuthenticationCheckboxListItem getElementAt(int index) {
            return list.get(index);
        }
        
        public void swapElement(int from, int to) {
            AuthenticationCheckboxListItem fromItem = list.get(from);
            list.set(from, list.get(to));
            list.set(to, fromItem);
        }

        public boolean hasPublicKey() {
            for (AuthenticationCheckboxListItem item : list) {
                if (item.isSelected && item.method.hasKeyFile()) {
                    return true;
                }
            }
            return false;
        }

        public void resetModel(Authentication.MethodList methodsList) {
            Authentication.Method[] methods = methodsList.getMethods();
            for(int i = 0; i < methods.length; i++) {
                Authentication.Method method = methods[i];
                boolean enabled = methodsList.isEnabled(method);
                AuthenticationCheckboxListItem authenticationCheckboxListItem = new AuthenticationCheckboxListItem(method, enabled);
                if (i == list.size()) {
                    list.add(authenticationCheckboxListItem);
                } else {
                    list.set(i, authenticationCheckboxListItem);
                }
            }
        }

        private Authentication.MethodList toMethodList() {
            List<Pair<Authentication.Method, Boolean>> pairs = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                AuthenticationCheckboxListItem m = list.get(i);
                pairs.add(Pair.of(m.method, m.isSelected));
            }
            return new Authentication.MethodList(pairs);
        }

        public void storeAuthenticationMethods(Authentication auth) {
            auth.setAuthenticationMethods(toMethodList());
        }
    }
}
