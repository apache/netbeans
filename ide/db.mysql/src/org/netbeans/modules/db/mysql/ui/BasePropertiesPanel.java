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

package org.netbeans.modules.db.mysql.ui;

import java.awt.Color;
import javax.swing.UIManager;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.impl.MySQLOptions;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author  David Van Couvering
 */
public class BasePropertiesPanel extends javax.swing.JPanel {
    MySQLOptions options = MySQLOptions.getDefault();
    DialogDescriptor descriptor;
    private Color nbErrorForeground;
    private String initMessage;

    private DocumentListener docListener = new DocumentListener() {
        
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            validatePanel();
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            validatePanel();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            validatePanel();
        }

    };
    
    private void validatePanel() {
        if (descriptor == null) {
            return;
        }

        if (initMessage != null && ! initMessage.isEmpty()) {
            messageLabel.setText(initMessage);
            messageLabel.setToolTipText(initMessage);
            descriptor.setValid(false);
            initMessage = null;
            return ;
        }
        
        String error = null;
        
        if ( getHost() == null || getHost().length() == 0) {
            error = NbBundle.getMessage(BasePropertiesPanel.class,
                        "BasePropertiesPanel.MSG_SpecifyHost");
        }
        if ( getUser() == null || getUser().length() == 0) {
            error = NbBundle.getMessage(BasePropertiesPanel.class,
                        "BasePropertiesPanel.MSG_SpecifyUser");
        }
        
        if (getPort() != null  && getPort().length() > 0) {
            try {
                Integer.valueOf(getPort());
            } catch (NumberFormatException nfe) {
                error = NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.MSG_InvalidPortNumber");
            }
         }

        
        if (error != null) {
            messageLabel.setText(error);
            descriptor.setValid(false);
        } else {
            messageLabel.setText(" "); // NOI18N
            descriptor.setValid(true);
        }
    }
    
    /** Creates new form BasePropertiesPanel
     * @param server database server
     */
    public BasePropertiesPanel(DatabaseServer server) {
        nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (nbErrorForeground == null) {
            //nbErrorForeground = new Color(89, 79, 191); // RGB suggested by Bruce in #28466
            nbErrorForeground = new Color(255, 0, 0); // RGB suggested by jdinga in #65358
        }
        
        initComponents();
        this.setBackground(getBackground());
        messageLabel.setBackground(getBackground());
        
        txtUser.getDocument().addDocumentListener(docListener);
        txtHost.getDocument().addDocumentListener(docListener);
        txtPort.getDocument().addDocumentListener(docListener);
        txtPassword.getDocument().addDocumentListener(docListener);
        
        String user = server.getUser();
        if ( user == null || user.equals("") ) {
            user = MySQLOptions.getDefaultAdminUser();
        }
        txtUser.setText(user);
        
        String host = server.getHost();
        if ( host == null || host.equals("")) {
            host = MySQLOptions.getDefaultHost();
        }
        txtHost.setText(host);
        
        String port = server.getPort();
        if ( port == null || port.equals("")) {
            port = MySQLOptions.getDefaultPort();
        }
        txtPort.setText(port);
        
        if (server.isSavePassword())
        {
            txtPassword.setText(server.getPassword());        
        }
        
        chkSavePassword.setSelected(server.isSavePassword());
    }

    String getHost() {
        return txtHost.getText().trim();
    }

    String getPassword() {
        return new String(txtPassword.getPassword()).trim();
    }

    String getPort() {
        return txtPort.getText().trim();
    }

    String getUser() {
        return txtUser.getText().trim();
    }

    boolean getSavePassword() {
        return chkSavePassword.isSelected();
    }
    void setDialogDescriptor(DialogDescriptor desc) {
        this.descriptor = desc;
        validatePanel();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        chkSavePassword = new javax.swing.JCheckBox();
        messageLabel = new javax.swing.JLabel();
        txtHost = new javax.swing.JTextField();
        labelHost = new javax.swing.JLabel();
        labelPort = new javax.swing.JLabel();
        txtPort = new javax.swing.JTextField();
        labelUser = new javax.swing.JLabel();
        txtUser = new javax.swing.JTextField();
        labelPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();

        setAutoscrolls(true);

        org.openide.awt.Mnemonics.setLocalizedText(chkSavePassword, org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.chkSavePassword.text")); // NOI18N
        chkSavePassword.setToolTipText(org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.chkSavePassword.AccessibleContext.accessibleDescription")); // NOI18N
        chkSavePassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkSavePasswordActionPerformed(evt);
            }
        });

        messageLabel.setForeground(new java.awt.Color(255, 0, 51));
        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, org.openide.util.NbBundle.getBundle(BasePropertiesPanel.class).getString("BasePropertiesPanel.messageLabel.text")); // NOI18N

        txtHost.setText(org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.txtHost.text")); // NOI18N
        txtHost.setToolTipText(org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.txtHost.AccessibleContext.accessibleDescription")); // NOI18N

        labelHost.setLabelFor(txtHost);
        org.openide.awt.Mnemonics.setLocalizedText(labelHost, org.openide.util.NbBundle.getBundle(BasePropertiesPanel.class).getString("BasePropertiesPanel.labelHost.text")); // NOI18N

        labelPort.setLabelFor(txtPort);
        org.openide.awt.Mnemonics.setLocalizedText(labelPort, org.openide.util.NbBundle.getBundle(BasePropertiesPanel.class).getString("BasePropertiesPanel.labelPort.text")); // NOI18N

        txtPort.setText(org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.txtPort.text")); // NOI18N
        txtPort.setToolTipText(org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.txtPort.AccessibleContext.accessibleDescription")); // NOI18N

        labelUser.setLabelFor(txtUser);
        org.openide.awt.Mnemonics.setLocalizedText(labelUser, org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.labelUser.text")); // NOI18N

        txtUser.setText(org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.txtUser.text")); // NOI18N
        txtUser.setToolTipText(org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.txtUser.AccessibleContext.accessibleDescription")); // NOI18N

        labelPassword.setLabelFor(txtPassword);
        org.openide.awt.Mnemonics.setLocalizedText(labelPassword, org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.labelPassword.text")); // NOI18N

        txtPassword.setText(org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.txtPassword.text")); // NOI18N
        txtPassword.setToolTipText(org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.txtPassword.AccessibleContext.accessibleDescription")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(labelHost, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(labelPort, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(labelPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(labelUser))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtUser, javax.swing.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE)
                                    .addComponent(txtPort, javax.swing.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE)
                                    .addComponent(txtPassword, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE)
                                    .addComponent(txtHost, javax.swing.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(chkSavePassword, javax.swing.GroupLayout.DEFAULT_SIZE, 684, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(messageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 696, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelHost)
                    .addComponent(txtHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelPort))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelUser)
                    .addComponent(txtUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelPassword)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkSavePassword)
                .addGap(12, 12, 12)
                .addComponent(messageLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        chkSavePassword.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.chkSavePassword.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


private void chkSavePasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkSavePasswordActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_chkSavePasswordActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkSavePassword;
    private javax.swing.JLabel labelHost;
    private javax.swing.JLabel labelPassword;
    private javax.swing.JLabel labelPort;
    private javax.swing.JLabel labelUser;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JTextField txtHost;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtPort;
    private javax.swing.JTextField txtUser;
    // End of variables declaration//GEN-END:variables

    void setErrorMessage(String msg) {
        initMessage = msg;
    }

}
