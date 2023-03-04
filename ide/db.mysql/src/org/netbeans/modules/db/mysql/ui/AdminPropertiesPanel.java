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
import java.io.File;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.UIManager;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.impl.MySQLOptions;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.DialogDescriptor;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  David Van Couvering
 */
public class AdminPropertiesPanel extends javax.swing.JPanel {
    private static final Logger LOGGER = Logger.getLogger(AdminPropertiesPanel.class.getName());

    MySQLOptions options = MySQLOptions.getDefault();
    DialogDescriptor descriptor;
    private Color nbErrorForeground;

    // the most recent directory where a new path was chosen
    private static String recentDirectory = null;    

    private void validatePanel() {
        descriptor.setValid(false);
        messageLabel.setText(NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.MSG_ValidatingCommandPaths"));

        revalidate();

        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                // Issue 142538
                // Run in background because this is doing I/O.  If the paths
                // we're validating are on a remote file system, things can hang up
                try {
                    if (descriptor == null) {
                        return;
                    }

                    String error = null;

                    String admin = getAdminPath();
                    String start = getStartPath();
                    String stop = getStopPath();

                    if ( ! Utils.isValidExecutable(start, true)) {
                        error = NbBundle.getMessage(AdminPropertiesPanel.class,
                                "AdminPropertiesPanel.MSG_InvalidStartPath");
                    }

                    if ( ! Utils.isValidExecutable(stop, true)) {
                        error = NbBundle.getMessage(AdminPropertiesPanel.class,
                                "AdminPropertiesPanel.MSG_InvalidStopPath");
                    }

                    if ( (!Utils.isValidURL(admin, true))  &&
                         (!Utils.isValidExecutable(admin, true))) {
                        error = NbBundle.getMessage(AdminPropertiesPanel.class,
                                "AdminPropertiesPanel.MSG_InvalidAdminPath");
                    }

                    final String finalError = error;

                    Mutex.EVENT.postReadRequest(new Runnable() {
                        @Override
                        public void run() {
                            if (finalError != null) {
                                messageLabel.setForeground(nbErrorForeground);
                                messageLabel.setText(finalError);
                                descriptor.setValid(false);
                            } else {
                                messageLabel.setText(" "); // NOI18N
                                descriptor.setValid(true);
                            }
                        }
                    });
                } catch (Throwable t) {
                    messageLabel.setForeground(nbErrorForeground);
                    messageLabel.setText(" "); // NOI18N
                    descriptor.setValid(true);
                    throw new RuntimeException(t);
                }
                
            }
        });
    }
    
    /** Creates new form PropertiesPanel */
    public AdminPropertiesPanel(DatabaseServer server) {
        nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (nbErrorForeground == null) {
            //nbErrorForeground = new Color(89, 79, 191); // RGB suggested by Bruce in #28466
            nbErrorForeground = new Color(255, 0, 0); // RGB suggested by jdinga in #65358
        }
        
        initComponents();
        this.setBackground(getBackground());
        messageLabel.setBackground(getBackground());
        messageLabel.setText(" "); // NOI18N
        
        txtAdmin.setText(server.getAdminPath());
        txtAdminArgs.setText(server.getAdminArgs());
        txtStart.setText(server.getStartPath());
        txtStartArgs.setText(server.getStartArgs());
        txtStop.setText(server.getStopPath());
        txtStopArgs.setText(server.getStopArgs());
    }
    
    public String getAdminPath() {
        return txtAdmin.getText().trim();
    }
    
    public String getAdminArgs() {
        return txtAdminArgs.getText().trim();
    }
    
    public String getStartPath() {
        return txtStart.getText().trim();
    }
    
    public String getStartArgs() {
        return txtStartArgs.getText().trim();
    }
    
    public String getStopPath() {
        return txtStop.getText().trim();
    }
    
    public String getStopArgs() {
        return txtStopArgs.getText().trim();
    }

    public void setDialogDescriptor(DialogDescriptor desc) {
        this.descriptor = desc;
    }
    
    private void chooseFile(JTextField txtField) {
        JFileChooser chooser = new JFileChooser();
        
        chooser.setCurrentDirectory(null);
        chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
        
        String path = txtField.getText().trim();
        if (path != null && path.length() > 0) {
            chooser.setSelectedFile(new File(path));
        } else if (recentDirectory != null) {
            chooser.setCurrentDirectory(new File(recentDirectory));
        }
        
        
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        File selectedFile = chooser.getSelectedFile();
        recentDirectory = selectedFile.getParentFile().getAbsolutePath();
        txtField.setText(selectedFile.getAbsolutePath());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtAdmin = new javax.swing.JTextField();
        btnAdminBrowse = new javax.swing.JButton();
        txtAdminArgs = new javax.swing.JTextField();
        messageLabel = new javax.swing.JLabel();
        labelAdmin = new javax.swing.JLabel();
        labelAdminArgs = new javax.swing.JLabel();
        labelStart = new javax.swing.JLabel();
        txtStart = new javax.swing.JTextField();
        btnStartBrowse = new javax.swing.JButton();
        txtStartArgs = new javax.swing.JTextField();
        labelStartArgs = new javax.swing.JLabel();
        labelStop = new javax.swing.JLabel();
        txtStop = new javax.swing.JTextField();
        btnStopBrowse = new javax.swing.JButton();
        txtStopArgs = new javax.swing.JTextField();
        labelStartArgs1 = new javax.swing.JLabel();

        txtAdmin.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.txtAdmin.text")); // NOI18N
        txtAdmin.setToolTipText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.txtAdmin.AccessibleContext.accessibleDescription")); // NOI18N
        txtAdmin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAdminActionPerformed(evt);
            }
        });
        txtAdmin.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtAdminFocusLost(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnAdminBrowse, org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.btnAdminBrowse.text")); // NOI18N
        btnAdminBrowse.setToolTipText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.btnAdminBrowse.AccessibleContext.accessibleDescription")); // NOI18N
        btnAdminBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdminBrowseActionPerformed(evt);
            }
        });

        txtAdminArgs.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.txtAdminArgs.text")); // NOI18N
        txtAdminArgs.setToolTipText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.txtAdminArgs.AccessibleContext.accessibleDescription")); // NOI18N

        messageLabel.setForeground(new java.awt.Color(255, 0, 51));
        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.messageLabel.text")); // NOI18N

        labelAdmin.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelAdmin.setLabelFor(txtAdmin);
        org.openide.awt.Mnemonics.setLocalizedText(labelAdmin, org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.labelAdmin.text")); // NOI18N

        labelAdminArgs.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelAdminArgs.setLabelFor(txtAdminArgs);
        org.openide.awt.Mnemonics.setLocalizedText(labelAdminArgs, org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.labelAdminArgs.text")); // NOI18N

        labelStart.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelStart.setLabelFor(txtStart);
        org.openide.awt.Mnemonics.setLocalizedText(labelStart, org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.labelStart.text")); // NOI18N

        txtStart.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.txtStart.text")); // NOI18N
        txtStart.setToolTipText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.txtStart.AccessibleContext.accessibleDescription")); // NOI18N
        txtStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtStartActionPerformed(evt);
            }
        });
        txtStart.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtStartFocusLost(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnStartBrowse, org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.btnStartBrowse.text")); // NOI18N
        btnStartBrowse.setToolTipText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.btnStartBrowse.AccessibleContext.accessibleDescription")); // NOI18N
        btnStartBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartBrowseActionPerformed(evt);
            }
        });

        txtStartArgs.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.txtStartArgs.text")); // NOI18N
        txtStartArgs.setToolTipText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.txtStartArgs.AccessibleContext.accessibleDescription")); // NOI18N

        labelStartArgs.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelStartArgs.setLabelFor(txtStartArgs);
        org.openide.awt.Mnemonics.setLocalizedText(labelStartArgs, org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.labelStartArgs.text")); // NOI18N

        labelStop.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelStop.setLabelFor(txtStop);
        org.openide.awt.Mnemonics.setLocalizedText(labelStop, org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.labelStop.text")); // NOI18N

        txtStop.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.txtStop.text")); // NOI18N
        txtStop.setToolTipText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.txtStop.AccessibleContext.accessibleDescription")); // NOI18N
        txtStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtStopActionPerformed(evt);
            }
        });
        txtStop.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtStopFocusLost(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnStopBrowse, org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.btnStopBrowse.text")); // NOI18N
        btnStopBrowse.setToolTipText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.btnStopBrowse.AccessibleContext.accessibleDescription")); // NOI18N
        btnStopBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopBrowseActionPerformed(evt);
            }
        });

        txtStopArgs.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.txtStopArgs.text")); // NOI18N
        txtStopArgs.setToolTipText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.txtStopArgs.AccessibleContext.accessibleDescription")); // NOI18N

        labelStartArgs1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelStartArgs1.setLabelFor(txtStopArgs);
        org.openide.awt.Mnemonics.setLocalizedText(labelStartArgs1, org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.labelStopArgs.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 734, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(labelAdminArgs, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(labelStartArgs, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(labelStart, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                                .addComponent(labelAdmin, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(labelStartArgs1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(labelStop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtStopArgs, javax.swing.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                            .addComponent(txtStop, javax.swing.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                            .addComponent(txtAdmin, javax.swing.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                            .addComponent(txtAdminArgs, javax.swing.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                            .addComponent(txtStart, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                            .addComponent(txtStartArgs, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnStopBrowse)
                            .addComponent(btnStartBrowse)
                            .addComponent(btnAdminBrowse))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdminBrowse)
                    .addComponent(labelAdmin)
                    .addComponent(txtAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelAdminArgs, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAdminArgs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelStart)
                    .addComponent(btnStartBrowse)
                    .addComponent(txtStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelStartArgs, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtStartArgs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnStopBrowse)
                    .addComponent(labelStop)
                    .addComponent(txtStop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelStartArgs1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtStopArgs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(messageLabel)
                .addGap(108, 108, 108))
        );

        txtAdmin.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.txtAdmin.AccessibleContext.accessibleDescription")); // NOI18N
        btnAdminBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.btnAdminBrowse.AccessibleContext.accessibleDescription")); // NOI18N
        txtAdminArgs.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.txtAdminArgs.AccessibleContext.accessibleName")); // NOI18N
        txtAdminArgs.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.txtAdminArgs.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void btnAdminBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdminBrowseActionPerformed
        chooseFile(txtAdmin);
    }//GEN-LAST:event_btnAdminBrowseActionPerformed

    private void txtAdminFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtAdminFocusLost
        validatePanel();
    }//GEN-LAST:event_txtAdminFocusLost

    private void txtAdminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAdminActionPerformed
        
    }//GEN-LAST:event_txtAdminActionPerformed

    private void txtStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtStartActionPerformed
        
}//GEN-LAST:event_txtStartActionPerformed

    private void txtStartFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtStartFocusLost
        validatePanel();
}//GEN-LAST:event_txtStartFocusLost

    private void btnStartBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartBrowseActionPerformed
        chooseFile(txtStart);
}//GEN-LAST:event_btnStartBrowseActionPerformed

    private void txtStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtStopActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_txtStopActionPerformed

    private void txtStopFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtStopFocusLost
        validatePanel();
}//GEN-LAST:event_txtStopFocusLost

    private void btnStopBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopBrowseActionPerformed
        chooseFile(txtStop);
}//GEN-LAST:event_btnStopBrowseActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdminBrowse;
    private javax.swing.JButton btnStartBrowse;
    private javax.swing.JButton btnStopBrowse;
    private javax.swing.JLabel labelAdmin;
    private javax.swing.JLabel labelAdminArgs;
    private javax.swing.JLabel labelStart;
    private javax.swing.JLabel labelStartArgs;
    private javax.swing.JLabel labelStartArgs1;
    private javax.swing.JLabel labelStop;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JTextField txtAdmin;
    private javax.swing.JTextField txtAdminArgs;
    private javax.swing.JTextField txtStart;
    private javax.swing.JTextField txtStartArgs;
    private javax.swing.JTextField txtStop;
    private javax.swing.JTextField txtStopArgs;
    // End of variables declaration//GEN-END:variables

}
