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

/*
 * CustomizerGeneral.java
 *
 * Created on 19.07.2010, 17:30:53
 */

package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import java.awt.Font;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.netbeans.api.progress.BaseProgressUtils;


import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.WLTrustHandler;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLJpa2SwitchSupport;
import org.netbeans.modules.weblogic.common.api.WebLogicConfiguration;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author ads
 * @author Petr Hejl
 */
class CustomizerGeneral extends javax.swing.JPanel {

    private static final long serialVersionUID = 748111929912200475L;
    
    private final transient WLDeploymentManager manager;
    
    private final transient WLJpa2SwitchSupport support;
    
    private boolean passwordVisible;
    
    private char originalEchoChar;
    
    private Font originalFont;
    
    CustomizerGeneral(WLDeploymentManager manager) {
        this.manager = manager;
        this.support = new WLJpa2SwitchSupport(manager);

        initComponents();
        
        initValues();
    }

    private void initValues() {
        String userNameValue = manager.getInstanceProperties().getProperty(
                InstanceProperties.USERNAME_ATTR);
        userName.setText(userNameValue);
        userName.getDocument().addDocumentListener( 
                new PropertyDocumentListener(manager, InstanceProperties.USERNAME_ATTR,
                        userName));
        String passwd = manager.getInstanceProperties().getProperty(
                InstanceProperties.PASSWORD_ATTR);
        passwordField.setText( passwd );
        passwordField.getDocument().addDocumentListener( 
                new PropertyDocumentListener(manager, InstanceProperties.PASSWORD_ATTR,
                        passwordField));
        
        String domainRoot = manager.getInstanceProperties().getProperty( 
                WLPluginProperties.DOMAIN_ROOT_ATTR);
        domainFolder.setText( domainRoot );
        String domain = manager.getInstanceProperties().getProperty(WLPluginProperties.DOMAIN_NAME);
        String host = manager.getInstanceProperties().getProperty(WLPluginProperties.HOST_ATTR);
        String port = manager.getInstanceProperties().getProperty(WLPluginProperties.PORT_ATTR);
        String securedString = manager.getInstanceProperties().getProperty(WLPluginProperties.SECURED_ATTR);
        Boolean secured = securedString == null ? null : Boolean.valueOf(securedString);
        WebLogicConfiguration config = null;
        if (domain == null || host == null || port == null || secured == null) {
            config = manager.getCommonConfiguration();
        }
        if (domain == null) {
            domain = config.getDomainName();
        }
        if (host == null) {
            host = config.getHost();
        }
        if (port == null) {
            port = Integer.toString(config.getPort());
        }
        if (domain != null) {
            domainName.setText(domain);
        }
        if (host != null) {
            serverHost.setText(host);
        }
        if (port != null) {
            serverPort.setText(port);
        }
        if (secured == null) {
            secured = config.isSecured();
        }

        sslCheckBox.setSelected(secured);
        certificateButton.setEnabled(sslCheckBox.isSelected());

        boolean statusVisible = support.isSwitchSupported();
        boolean buttonVisible = statusVisible
                && !support.isEnabledViaSmartUpdate();

        jpa2SwitchLabel.setVisible(statusVisible);
        jpa2Status.setVisible(statusVisible);
        jpa2SwitchButton.setVisible(buttonVisible);
        updateJpa2Status();

        noteChangesLabel.setVisible(!manager.isRemote());

        if (manager.isRemote()) {
            addAncestorListener(new AncestorListener() {

                @Override
                public void ancestorRemoved(AncestorEvent event) {
                    manager.getInstanceProperties().refreshServerInstance();
                }

                @Override
                public void ancestorAdded(AncestorEvent event) {
                }

                @Override
                public void ancestorMoved(AncestorEvent event) {
                }
            });
        }
    }

    private void updateJpa2Status() {
        if (support.isEnabled() || support.isEnabledViaSmartUpdate()) {
            jpa2Status.setText(NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.jpa2Status.enabledText"));
            Mnemonics.setLocalizedText(jpa2SwitchButton, NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.jpa2SwitchButton.disableText"));
        } else {
            jpa2Status.setText(NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.jpa2Status.disabledText"));
            Mnemonics.setLocalizedText(jpa2SwitchButton, NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.jpa2SwitchButton.enableText"));
        }         
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        domainNameLabel = new javax.swing.JLabel();
        domainName = new javax.swing.JTextField();
        domainFolderLabel = new javax.swing.JLabel();
        domainFolder = new javax.swing.JTextField();
        adminInfoLabel = new javax.swing.JLabel();
        userNameLabel = new javax.swing.JLabel();
        userName = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        showButton = new javax.swing.JButton();
        serverPortLabel = new javax.swing.JLabel();
        noteChangesLabel = new javax.swing.JLabel();
        serverPort = new javax.swing.JTextField();
        jpa2SwitchLabel = new javax.swing.JLabel();
        jpa2Status = new javax.swing.JLabel();
        jpa2SwitchButton = new javax.swing.JButton();
        serverHostLabel = new javax.swing.JLabel();
        serverHost = new javax.swing.JTextField();
        sslCheckBox = new javax.swing.JCheckBox();
        certificateButton = new javax.swing.JButton();

        domainNameLabel.setLabelFor(domainName);
        org.openide.awt.Mnemonics.setLocalizedText(domainNameLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_CustomizerDomainName")); // NOI18N

        domainName.setEditable(false);

        domainFolderLabel.setLabelFor(domainFolder);
        org.openide.awt.Mnemonics.setLocalizedText(domainFolderLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_DomainFolder")); // NOI18N

        domainFolder.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(adminInfoLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_AdminInfo")); // NOI18N

        userNameLabel.setLabelFor(userName);
        org.openide.awt.Mnemonics.setLocalizedText(userNameLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_UserName")); // NOI18N

        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_Password")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(showButton, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_ShowButton")); // NOI18N
        showButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showButtonActionPerformed(evt);
            }
        });

        serverPortLabel.setLabelFor(serverPort);
        org.openide.awt.Mnemonics.setLocalizedText(serverPortLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_ServerPort")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(noteChangesLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_Note")); // NOI18N

        serverPort.setEditable(false);

        jpa2SwitchLabel.setLabelFor(jpa2SwitchLabel);
        org.openide.awt.Mnemonics.setLocalizedText(jpa2SwitchLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.jpa2SwitchLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jpa2Status, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.jpa2Status.disabledText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jpa2SwitchButton, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.jpa2SwitchButton.enableText")); // NOI18N
        jpa2SwitchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jpa2SwitchButtonActionPerformed(evt);
            }
        });

        serverHostLabel.setLabelFor(serverHost);
        org.openide.awt.Mnemonics.setLocalizedText(serverHostLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.serverHostLabel.text")); // NOI18N

        serverHost.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(sslCheckBox, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.sslCheckBox.text")); // NOI18N
        sslCheckBox.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(certificateButton, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.certificateButton.text")); // NOI18N
        certificateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                certificateButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(noteChangesLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(domainNameLabel)
                            .addComponent(domainFolderLabel)
                            .addComponent(userNameLabel)
                            .addComponent(passwordLabel)
                            .addComponent(serverHostLabel)
                            .addComponent(serverPortLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(domainName)
                            .addComponent(domainFolder)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(serverPort, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(sslCheckBox))
                                    .addComponent(passwordField, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                                    .addComponent(userName)
                                    .addComponent(serverHost))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(certificateButton)
                                    .addComponent(showButton)
                                    .addComponent(jpa2SwitchButton))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(adminInfoLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jpa2SwitchLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jpa2Status)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(domainNameLabel)
                    .addComponent(domainName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(domainFolderLabel)
                    .addComponent(domainFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addComponent(adminInfoLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userNameLabel)
                    .addComponent(userName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(showButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverHostLabel)
                    .addComponent(serverHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverPortLabel)
                    .addComponent(serverPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sslCheckBox)
                    .addComponent(certificateButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jpa2SwitchLabel)
                    .addComponent(jpa2Status)
                    .addComponent(jpa2SwitchButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(noteChangesLabel)
                .addContainerGap())
        );

        domainNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACN_CustomizerDomainName")); // NOI18N
        domainNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_DomainName")); // NOI18N
        domainName.getAccessibleContext().setAccessibleName(domainNameLabel.getAccessibleContext().getAccessibleName());
        domainName.getAccessibleContext().setAccessibleDescription(domainNameLabel.getAccessibleContext().getAccessibleDescription());
        domainFolderLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_DomainFolder")); // NOI18N
        domainFolder.getAccessibleContext().setAccessibleName(domainFolderLabel.getAccessibleContext().getAccessibleName());
        domainFolder.getAccessibleContext().setAccessibleDescription(domainFolderLabel.getAccessibleContext().getAccessibleDescription());
        userNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_UserName")); // NOI18N
        userNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_Username")); // NOI18N
        userName.getAccessibleContext().setAccessibleName(userNameLabel.getAccessibleContext().getAccessibleName());
        userName.getAccessibleContext().setAccessibleDescription(userNameLabel.getAccessibleContext().getAccessibleDescription());
        passwordLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_Password")); // NOI18N
        passwordLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_Password")); // NOI18N
        passwordField.getAccessibleContext().setAccessibleName(passwordLabel.getAccessibleContext().getAccessibleName());
        passwordField.getAccessibleContext().setAccessibleDescription(passwordLabel.getAccessibleContext().getAccessibleDescription());
        showButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_ShowButton")); // NOI18N
        showButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_ShowButton")); // NOI18N
        serverPortLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_ServerPort")); // NOI18N
        serverPortLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_ServerPort")); // NOI18N
        noteChangesLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_Note")); // NOI18N
        noteChangesLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_Note")); // NOI18N
        serverPort.getAccessibleContext().setAccessibleName(serverPortLabel.getAccessibleContext().getAccessibleName());
        serverPort.getAccessibleContext().setAccessibleDescription(serverPortLabel.getAccessibleContext().getAccessibleDescription());
    }// </editor-fold>//GEN-END:initComponents

    private void showButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showButtonActionPerformed
        if (!passwordVisible) {
            passwordVisible = true;
            originalFont = passwordField.getFont();
            passwordField.setFont(userName.getFont());
            originalEchoChar = passwordField.getEchoChar();
            passwordField.setEchoChar((char) 0);
            Mnemonics.setLocalizedText(showButton, 
                    NbBundle.getMessage(CustomizerGeneral.class, 
                    "LBL_ShowButtonHide"));                         // NOI18N
            showButton.setToolTipText(NbBundle.getMessage(CustomizerGeneral.class, 
                    "LBL_ShowButtonHide_ToolTip"));                 // NOI18N
        } else {
            passwordVisible = false;
            passwordField.setFont(originalFont);
            passwordField.setEchoChar(originalEchoChar);
            Mnemonics.setLocalizedText(showButton, NbBundle.getMessage(
                    CustomizerGeneral.class, "LBL_ShowButton"));    // NOI18N
            showButton.setToolTipText(NbBundle.getMessage(CustomizerGeneral.class, 
                    "LBL_ShowButton_ToolTip"));                     // NOI18N

        }
    }//GEN-LAST:event_showButtonActionPerformed

    private void jpa2SwitchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jpa2SwitchButtonActionPerformed
        if (!support.isEnabled()) {
            support.enable();
        } else {
            support.disable();
        }
        updateJpa2Status();
    }//GEN-LAST:event_jpa2SwitchButtonActionPerformed

    @NbBundle.Messages({
        "MSG_ContactingServer=Contacting the server",
        "MSG_ConnectionFailed=Could not connect to the server. It is either not running or not accessible at the moment."
    })
    private void certificateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_certificateButtonActionPerformed
        manager.getInstanceProperties().setProperty(WLTrustHandler.TRUST_EXCEPTION_PROPERTY, null);
        BaseProgressUtils.showProgressDialogAndRun(new Runnable() {

            @Override
            public void run() {
                boolean connected = WLTrustHandler.check(manager.getCommonConfiguration());
                if (!connected) {
                    NotifyDescriptor desc = new NotifyDescriptor.Message(Bundle.MSG_ConnectionFailed(),
                            NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                }
            }
        }, Bundle.MSG_ContactingServer());

    }//GEN-LAST:event_certificateButtonActionPerformed
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel adminInfoLabel;
    private javax.swing.JButton certificateButton;
    private javax.swing.JTextField domainFolder;
    private javax.swing.JLabel domainFolderLabel;
    private javax.swing.JTextField domainName;
    private javax.swing.JLabel domainNameLabel;
    private javax.swing.JLabel jpa2Status;
    private javax.swing.JButton jpa2SwitchButton;
    private javax.swing.JLabel jpa2SwitchLabel;
    private javax.swing.JLabel noteChangesLabel;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField serverHost;
    private javax.swing.JLabel serverHostLabel;
    private javax.swing.JTextField serverPort;
    private javax.swing.JLabel serverPortLabel;
    private javax.swing.JButton showButton;
    private javax.swing.JCheckBox sslCheckBox;
    private javax.swing.JTextField userName;
    private javax.swing.JLabel userNameLabel;
    // End of variables declaration//GEN-END:variables

}
