/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.core.ui.options.general;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDescriptor;
import org.openide.NotificationLineSupport;

/**
 *
 * @author  Jiri Rechtacek
 */
@OptionsPanelController.Keywords(keywords={"advanced proxy", "#KW_AdvancedProxy"}, location=OptionsDisplayer.GENERAL)
public class AdvancedProxyPanel extends javax.swing.JPanel {
    private GeneralOptionsModel options;
    private String oldHttpsHost;
    private String oldHttpsPort;
    private String oldSocksHost;
    private String oldSocksPort;
    private DialogDescriptor dd = null;
    
    /** Creates new form AdvancedProxyPanel */
    AdvancedProxyPanel (GeneralOptionsModel model) {
        options = model;
        initComponents ();
        tfHttpProxyHost.getDocument().addDocumentListener (new DocumentListener () {
            public void insertUpdate(DocumentEvent arg0) {
                followHttpHostIfDemand ();
            }

            public void removeUpdate(DocumentEvent arg0) {
                followHttpHostIfDemand ();
            }

            public void changedUpdate(DocumentEvent arg0) {
                followHttpHostIfDemand ();
            }
        });
        tfHttpProxyPort.getDocument().addDocumentListener (new DocumentListener () {
            public void insertUpdate(DocumentEvent arg0) {
                followHttpPortIfDemand ();
            }

            public void removeUpdate(DocumentEvent arg0) {
                followHttpPortIfDemand ();
            }

            public void changedUpdate(DocumentEvent arg0) {
                followHttpPortIfDemand ();
            }
        });
        tfHttpsProxyPort.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent arg0) {
                validatePortValue(tfHttpsProxyPort.getText());
            }

            public void removeUpdate(DocumentEvent arg0) {
                validatePortValue(tfHttpsProxyPort.getText());
            }

            public void changedUpdate(DocumentEvent arg0) {
                validatePortValue(tfHttpsProxyPort.getText());
            }
        });
        tfSocksPort.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent arg0) {
                validatePortValue(tfSocksPort.getText());
            }

            public void removeUpdate(DocumentEvent arg0) {
                validatePortValue(tfSocksPort.getText());
            }

            public void changedUpdate(DocumentEvent arg0) {
                validatePortValue(tfSocksPort.getText());
            }
        });
    }
    
    // helps implement OptionsPanelController
    
    public void update (String httpHost, String httpPort) {
        readOptions ();
        
        if (! options.getHttpProxyHost ().equals (httpHost)) {
            tfHttpProxyHost.setText (httpHost);
        }
        if (! options.getHttpProxyPort ().equals (httpPort)) {
            tfHttpProxyPort.setText (httpPort);
        }
    }

    public void applyChanges() {
        writeOptions ();
    }

    // helpers

    private void readOptions () {
        cbSameProxySettings.setSelected (options.useProxyAllProtocols ());
        cbUseProxyAuthentication.setSelected(options.useProxyAuthentication ());
        tfHttpProxyHost.setText (options.getHttpProxyHost ());
        tfHttpProxyPort.setText (options.getHttpProxyPort ());
        tfHttpsProxyHost.setText (options.getHttpsProxyHost ());
        tfHttpsProxyPort.setText (options.getHttpsProxyPort ());
        tfSocksHost.setText (options.getSocksHost ());
        tfSocksPort.setText (options.getSocksPort ());
        tfUserName.setText (options.getProxyAuthenticationUsername ());
        pfUserPassword.setText (new String (options.getProxyAuthenticationPassword ()));
        tfNonProxyHosts.setText (options.getNonProxyHosts ());
        
        oldHttpsHost = options.getOriginalHttpsHost ();
        oldHttpsPort = options.getOriginalHttpsPort ();
        oldSocksHost = options.getOriginalSocksHost ();
        oldSocksPort = options.getOriginalSocksPort ();
        
        followHttpProxyIfDemand();
        updateAuthentication ();
    }
    
    private void writeOptions () {
        options.setUseProxyAllProtocols (cbSameProxySettings.isSelected ());
        options.setUseProxyAuthentication(cbUseProxyAuthentication.isSelected ());
        options.setHttpProxyHost (tfHttpProxyHost.getText ());
        options.setHttpProxyPort (tfHttpProxyPort.getText ());
        if (! cbSameProxySettings.isSelected ()) {
            options.setHttpsProxyHost (tfHttpsProxyHost.getText ());
            options.setHttpsProxyPort (tfHttpsProxyPort.getText ());
            options.setSocksHost (tfSocksHost.getText ());
            options.setSocksPort (tfSocksPort.getText ());
        }
        options.setNonProxyHosts (tfNonProxyHosts.getText ());
        options.setAuthenticationUsername (tfUserName.getText ());
        options.setAuthenticationPassword (pfUserPassword.getPassword ());
    }
    
    private void followHttpProxyIfDemand () {
        boolean same = cbSameProxySettings.isSelected ();
        tfHttpsProxyHost.setEnabled (! same);
        tfHttpsProxyPort.setEnabled (! same);
        tfSocksHost.setEnabled (! same);
        tfSocksPort.setEnabled (! same);
        lHttpsProxyHost.setEnabled (! same);
        lHttpsProxyPort.setEnabled (! same);
        lSocksHost.setEnabled (! same);
        lSocksPort.setEnabled (! same);
        
        followHttpHostIfDemand ();
        followHttpPortIfDemand ();
    }
    
    private void updateAuthentication () {
        boolean use = cbUseProxyAuthentication.isSelected ();
        tfUserName.setEnabled (use);
        lUserName.setEnabled (use);
        pfUserPassword.setEnabled (use);
        lUserPassword.setEnabled (use);
    }
    
    private void followHttpHostIfDemand () {
        if (! cbSameProxySettings.isSelected ()) {
            return ;
        }
        String host = tfHttpProxyHost.getText ();
        tfHttpsProxyHost.setText (host);
        tfSocksHost.setText (host);
    }
    
    private void followHttpPortIfDemand () {
        String port = tfHttpProxyPort.getText();
        validatePortValue(port);

        if (! cbSameProxySettings.isSelected ()) {
            return ;
        }

        tfHttpsProxyPort.setText (port);
        tfSocksPort.setText (port);
    }

    private void validatePortValue(String port) {
        clearError();
        if (port != null && port.length() > 0) {
            try {
                Integer.parseInt(port);
            } catch (NumberFormatException nfex) {
                showError(org.openide.util.NbBundle.getMessage(
                        AdvancedProxyPanel.class,
                        "LBL_AdvancedProxyPanel_PortError")); // NOI18N
            }
        }
    }

    private void showError(String message) {
        if (dd != null) {
            NotificationLineSupport notificationLineSupport =
                    dd.getNotificationLineSupport();
            if (notificationLineSupport != null) {
                notificationLineSupport.setErrorMessage(message);
            }
            dd.setValid(false);
        }
    }

    private void clearError() {
        if (dd != null) {
            NotificationLineSupport notificationLineSupport =
                    dd.getNotificationLineSupport();
            if (notificationLineSupport != null) {
                notificationLineSupport.clearMessages();
            }
            dd.setValid(true);
        }
    }

    public void setDialogDescriptor(DialogDescriptor dd) {
        this.dd = dd;
    }
    
    protected String getNonProxyHosts() {
        return tfNonProxyHosts.getText();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lHttpProxyHost = new javax.swing.JLabel();
        tfHttpProxyHost = new javax.swing.JTextField();
        cbSameProxySettings = new javax.swing.JCheckBox();
        lHttpsProxyHost = new javax.swing.JLabel();
        tfHttpsProxyHost = new javax.swing.JTextField();
        lSocksHost = new javax.swing.JLabel();
        tfSocksHost = new javax.swing.JTextField();
        lHttpProxyPort = new javax.swing.JLabel();
        lHttpsProxyPort = new javax.swing.JLabel();
        lSocksPort = new javax.swing.JLabel();
        tfHttpProxyPort = new javax.swing.JTextField();
        tfHttpsProxyPort = new javax.swing.JTextField();
        tfSocksPort = new javax.swing.JTextField();
        lNonProxyHosts = new javax.swing.JLabel();
        tfNonProxyHosts = new javax.swing.JTextField();
        lNonProxyHostsDescription = new javax.swing.JLabel();
        sSeparator = new javax.swing.JSeparator();
        cbUseProxyAuthentication = new javax.swing.JCheckBox();
        lUserName = new javax.swing.JLabel();
        lUserPassword = new javax.swing.JLabel();
        tfUserName = new javax.swing.JTextField();
        pfUserPassword = new javax.swing.JPasswordField();

        lHttpProxyHost.setLabelFor(tfHttpProxyHost);
        org.openide.awt.Mnemonics.setLocalizedText(lHttpProxyHost, org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "LBL_AdvancedProxyPanel_lHttpProxyHost")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbSameProxySettings, org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "LBL_AdvancedProxyPanel_cbSameProxySettings")); // NOI18N
        cbSameProxySettings.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbSameProxySettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbSameProxySettingsActionPerformed(evt);
            }
        });

        lHttpsProxyHost.setLabelFor(tfHttpsProxyHost);
        org.openide.awt.Mnemonics.setLocalizedText(lHttpsProxyHost, org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "LBL_AdvancedProxyPanel_lHttpsProxyHots")); // NOI18N

        lSocksHost.setLabelFor(tfSocksHost);
        org.openide.awt.Mnemonics.setLocalizedText(lSocksHost, org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "LBL_AdvancedProxyPanel_lSocksHost")); // NOI18N

        lHttpProxyPort.setLabelFor(tfHttpProxyPort);
        org.openide.awt.Mnemonics.setLocalizedText(lHttpProxyPort, org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "LBL_AdvancedProxyPanel_lHttpProxyPort")); // NOI18N

        lHttpsProxyPort.setLabelFor(tfHttpsProxyPort);
        org.openide.awt.Mnemonics.setLocalizedText(lHttpsProxyPort, org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "LBL_AdvancedProxyPanel_lHttpsProxyPort")); // NOI18N

        lSocksPort.setLabelFor(tfSocksPort);
        org.openide.awt.Mnemonics.setLocalizedText(lSocksPort, org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "LBL_AdvancedProxyPanel_lSocksPort")); // NOI18N

        tfHttpProxyPort.setColumns(4);

        tfHttpsProxyPort.setColumns(4);

        tfSocksPort.setColumns(4);

        lNonProxyHosts.setLabelFor(tfNonProxyHosts);
        org.openide.awt.Mnemonics.setLocalizedText(lNonProxyHosts, org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "LBL_AdvancedProxyPanel_lNonProxyHosts")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lNonProxyHostsDescription, org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "LBL_AdvancedProxyPanel_lNonProxyHostsDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbUseProxyAuthentication, org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "LBL_AdvancedProxyPanel_cbUseProxyAuthentication")); // NOI18N
        cbUseProxyAuthentication.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbUseProxyAuthentication.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbUseProxyAuthenticationActionPerformed(evt);
            }
        });

        lUserName.setLabelFor(tfUserName);
        org.openide.awt.Mnemonics.setLocalizedText(lUserName, org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "LBL_AdvancedProxyPanel_lUserName")); // NOI18N

        lUserPassword.setLabelFor(pfUserPassword);
        org.openide.awt.Mnemonics.setLocalizedText(lUserPassword, org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "LBL_AdvancedProxyPanel_lUserPassword")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(cbUseProxyAuthentication))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(sSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lUserName)
                            .addComponent(lUserPassword))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pfUserPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)
                            .addComponent(tfUserName, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lHttpProxyHost)
                            .addComponent(lHttpsProxyHost)
                            .addComponent(lSocksHost)
                            .addComponent(lNonProxyHosts))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lNonProxyHostsDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbSameProxySettings, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(tfHttpProxyHost, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                                    .addComponent(tfSocksHost, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                                    .addComponent(tfHttpsProxyHost, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lHttpProxyPort)
                                    .addComponent(lHttpsProxyPort)
                                    .addComponent(lSocksPort))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(tfHttpProxyPort)
                                    .addComponent(tfSocksPort)
                                    .addComponent(tfHttpsProxyPort)))
                            .addComponent(tfNonProxyHosts, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE))))
                .addGap(18, 18, 18))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lHttpProxyHost)
                    .addComponent(tfHttpProxyHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lHttpProxyPort)
                    .addComponent(tfHttpProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbSameProxySettings)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lHttpsProxyHost)
                    .addComponent(tfHttpsProxyHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lHttpsProxyPort)
                    .addComponent(tfHttpsProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lSocksHost)
                    .addComponent(tfSocksHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lSocksPort)
                    .addComponent(tfSocksPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lNonProxyHosts)
                    .addComponent(tfNonProxyHosts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lNonProxyHostsDescription)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbUseProxyAuthentication)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lUserName)
                    .addComponent(tfUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lUserPassword)
                    .addComponent(pfUserPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        tfHttpProxyHost.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "ACD_AdvancedProxyPanel_tfHttpProxyHost")); // NOI18N
        cbSameProxySettings.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "ACD_AdvancedProxyPanel_cbSameProxySettings")); // NOI18N
        tfHttpsProxyHost.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "ACD_AdvancedProxyPanel_tfHttpsProxyHost")); // NOI18N
        tfSocksHost.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "ACD_AdvancedProxyPanel_tfSocksHost")); // NOI18N
        tfHttpProxyPort.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "ACD_AdvancedProxyPanel_tfHttpProxyPort")); // NOI18N
        tfHttpsProxyPort.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "ACD_AdvancedProxyPanel_tfHttpsProxyPort")); // NOI18N
        tfSocksPort.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "ACD_AdvancedProxyPanel_tfSocksPort")); // NOI18N
        tfNonProxyHosts.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "ACD_AdvancedProxyPanel_tfNonProxyHosts")); // NOI18N
        cbUseProxyAuthentication.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "ACD_AdvancedProxyPanel_cbUseProxyAuthentication")); // NOI18N
        tfUserName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "ACD_AdvancedProxyPanel_tfUserName")); // NOI18N
        pfUserPassword.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "ACD_AdvancedProxyPanel_pfUserPassword")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedProxyPanel.class, "ACD_AdvancedProxyPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void cbUseProxyAuthenticationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbUseProxyAuthenticationActionPerformed
    updateAuthentication ();
}//GEN-LAST:event_cbUseProxyAuthenticationActionPerformed

private void cbSameProxySettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbSameProxySettingsActionPerformed
    if (cbSameProxySettings.isSelected ()) {
        oldHttpsHost = tfHttpsProxyHost.getText ();
        oldHttpsPort = tfHttpsProxyPort.getText ();
        oldSocksHost = tfSocksHost.getText ();
        oldSocksPort = tfSocksPort.getText ();
    } else {
        tfHttpsProxyHost.setText (oldHttpsHost);
        tfHttpsProxyPort.setText (oldHttpsPort);
        tfSocksHost.setText (oldSocksHost);
        tfSocksPort.setText (oldSocksPort);
    }
    followHttpProxyIfDemand ();
}//GEN-LAST:event_cbSameProxySettingsActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbSameProxySettings;
    private javax.swing.JCheckBox cbUseProxyAuthentication;
    private javax.swing.JLabel lHttpProxyHost;
    private javax.swing.JLabel lHttpProxyPort;
    private javax.swing.JLabel lHttpsProxyHost;
    private javax.swing.JLabel lHttpsProxyPort;
    private javax.swing.JLabel lNonProxyHosts;
    private javax.swing.JLabel lNonProxyHostsDescription;
    private javax.swing.JLabel lSocksHost;
    private javax.swing.JLabel lSocksPort;
    private javax.swing.JLabel lUserName;
    private javax.swing.JLabel lUserPassword;
    private javax.swing.JPasswordField pfUserPassword;
    private javax.swing.JSeparator sSeparator;
    private javax.swing.JTextField tfHttpProxyHost;
    private javax.swing.JTextField tfHttpProxyPort;
    private javax.swing.JTextField tfHttpsProxyHost;
    private javax.swing.JTextField tfHttpsProxyPort;
    private javax.swing.JTextField tfNonProxyHosts;
    private javax.swing.JTextField tfSocksHost;
    private javax.swing.JTextField tfSocksPort;
    private javax.swing.JTextField tfUserName;
    // End of variables declaration//GEN-END:variables
    
}
