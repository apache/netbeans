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

package org.netbeans.modules.derby.ui;

import java.awt.Color;
import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverListener;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.derby.DerbyOptions;
import org.netbeans.modules.derby.RegisterDerby;
import org.netbeans.modules.derby.Util;
import org.netbeans.modules.derby.api.DerbyDatabases;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Despite the name, serves as a settings dialog for Derby (not only
 * system home, but also database location).
 * 
 * @author Andrei Badea
 */
public class DerbyPropertiesPanel extends javax.swing.JPanel {
    
    private DialogDescriptor descriptor;
    private Color nbErrorForeground;
   
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
    
    public static boolean showDerbyProperties() {
        assert SwingUtilities.isEventDispatchThread();
        
        DerbyPropertiesPanel panel = new DerbyPropertiesPanel();
        String title = NbBundle.getMessage(DerbyPropertiesPanel.class, "LBL_SetDerbySystemHome");

        DialogDescriptor desc = new DialogDescriptor(panel, title);
        desc.createNotificationLineSupport();
        panel.setDialogDescriptor(desc);

        for (;;) {                    
            Dialog dialog = DialogDisplayer.getDefault().createDialog(desc);
            if (panel.getInstallLocation().length() == 0) {
                panel.setIntroduction();
            }
            String acsd = NbBundle.getMessage(DerbyPropertiesPanel.class, "ACSD_DerbySystemHomePanel");
            dialog.getAccessibleContext().setAccessibleDescription(acsd);
            dialog.setVisible(true);
            dialog.dispose();

            if (!DialogDescriptor.OK_OPTION.equals(desc.getValue())) {
                return false; // NOI18N
            }

            File derbySystemHome = new File(panel.getDerbySystemHome());
            if (!derbySystemHome.exists()) {
                boolean success = derbySystemHome.mkdirs();
                if (!success) {
                    String message = NbBundle.getMessage(DerbyPropertiesPanel.class, "ERR_DerbySystemHomeCantCreate");
                    NotifyDescriptor ndesc = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(ndesc);
                    continue;
                }
            }
            new RegisterSampleDatabase();
            if (! panel.getDerbySystemHome().equals(DerbyOptions.getDefault().getSystemHome())) {
                DerbyOptions.getDefault().setSystemHome(panel.getDerbySystemHome());
            }
            if (! panel.getInstallLocation().equals(DerbyOptions.getDefault().getLocation())) {
                DerbyOptions.getDefault().setLocation(panel.getInstallLocation());
            }
            return true;
        }
    }
    
    private DerbyPropertiesPanel() {
        // copied from WizardDescriptor
        nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (nbErrorForeground == null) {
            //nbErrorForeground = new Color(89, 79, 191); // RGB suggested by Bruce in #28466
            nbErrorForeground = new Color(255, 0, 0); // RGB suggested by jdinga in #65358
        }
        
        initComponents();
        derbyInstallInfo.setBackground(getBackground());
        derbySystemHomeTextField.getDocument().addDocumentListener(docListener);
        derbySystemHomeTextField.setText(DerbyOptions.getDefault().getSystemHome());
        derbyInstall.getDocument().addDocumentListener(docListener);
        derbyInstall.setText(DerbyOptions.getDefault().getLocation());
    }
    
    private void setDialogDescriptor(DialogDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    private String getDerbySystemHome() {
        return derbySystemHomeTextField.getText().trim();
    }
    
    private String getInstallLocation() {
        return derbyInstall.getText().trim();
    }
    
    private void setInstallLocation(String location) {
        derbyInstall.setText(location);
    }
    
    private void setDerbySystemHome(String derbySystemHome) {
        derbySystemHomeTextField.setText(derbySystemHome);
    }

    public void setIntroduction() {
        String info = NbBundle.getMessage(CreateDatabasePanel.class, "INFO_EnterDerbyLocation");
        descriptor.getNotificationLineSupport().setInformationMessage(info);
        descriptor.setValid(false);
    }
    
    private void validatePanel() {
        if (descriptor == null) {
            return;
        }
        
        String error = null;
        String warning = null;
        String info = null;
        
        String location = getInstallLocation();
        if (location !=  null && location.length() > 0) {
            File locationFile = new File(location).getAbsoluteFile();
            if (!locationFile.exists()) {
                error = NbBundle.getMessage(DerbyOptions.class, "ERR_DirectoryDoesNotExist", locationFile);
            }
            if (!Util.isDerbyInstallLocation(locationFile)) {
                error = NbBundle.getMessage(DerbyOptions.class, "ERR_InvalidDerbyLocation", locationFile);
            }
        } else if (location.length() == 0) {
            info = NbBundle.getMessage(CreateDatabasePanel.class, "INFO_EnterDerbyLocation"); // NOI18N
        }

        if (error == null) {
            File derbySystemHome = new File(getDerbySystemHome());
            if (derbySystemHome.getPath().length() <= 0) {
                info = NbBundle.getMessage(CreateDatabasePanel.class, "INFO_DerbySystemHomeNotEntered");
            }

            if (derbySystemHome.exists() && !derbySystemHome.isDirectory()) {
                error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DerbySystemHomeNotDirectory");
            } else if ((derbySystemHome.getPath().length() > 0) && !derbySystemHome.isAbsolute()) {
                error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DerbySystemHomeNotAbsolute");
            }
        }
        
        if (error != null) {
            descriptor.setValid(false);
            descriptor.getNotificationLineSupport().setErrorMessage(error);
        } else if (warning != null) {
            descriptor.setValid(false);
            descriptor.getNotificationLineSupport().setWarningMessage(warning);
        } else if (info != null) {
            descriptor.setValid(false);
            descriptor.getNotificationLineSupport().setInformationMessage(info);
        } else {
            descriptor.setValid(true);
            descriptor.getNotificationLineSupport().clearMessages();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        derbySystemHomeLabel = new javax.swing.JLabel();
        derbySystemHomeTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        installLabel = new javax.swing.JLabel();
        derbyInstall = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        derbyInstallInfo = new javax.swing.JTextPane();

        derbySystemHomeLabel.setLabelFor(derbySystemHomeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(derbySystemHomeLabel, org.openide.util.NbBundle.getMessage(DerbyPropertiesPanel.class, "LBL_DerbySystemHome")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(DerbyPropertiesPanel.class, "LBL_Browse")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        installLabel.setLabelFor(derbyInstall);
        org.openide.awt.Mnemonics.setLocalizedText(installLabel, org.openide.util.NbBundle.getMessage(DerbyPropertiesPanel.class, "LBL_Install")); // NOI18N

        derbyInstall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                derbyInstallActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(DerbyPropertiesPanel.class, "LBL_Browse2")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        derbyInstallInfo.setEditable(false);
        derbyInstallInfo.setText(org.openide.util.NbBundle.getMessage(DerbyPropertiesPanel.class, "LBL_InstallationInfo")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(derbyInstallInfo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(derbySystemHomeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(derbySystemHomeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(installLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(derbyInstall, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(derbyInstallInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(installLabel)
                    .addComponent(derbyInstall, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(derbySystemHomeLabel)
                    .addComponent(derbySystemHomeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addContainerGap(79, Short.MAX_VALUE))
        );

        derbySystemHomeTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DerbyPropertiesPanel.class, "ACSN_CreateDatabasePanel_databaseLocationTextField")); // NOI18N
        derbySystemHomeTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DerbyPropertiesPanel.class, "ACSD_DerbySystemHomePanel_derbySystemHomeTextField")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DerbyPropertiesPanel.class, "ACSD_DerbySystemHomePanel_browseButton")); // NOI18N
        derbyInstall.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DerbyPropertiesPanel.class, "ACSD_DerbySystemHomePanel_derbySystemHomeTextField")); // NOI18N
        derbyInstallInfo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DerbyPropertiesPanel.class, "ACSN_DerbySystemHomePanel_derbyInstallInfoTextField")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        String location = getInstallLocation();
        if (location.length() > 0) {
            chooser.setSelectedFile(new File(location));
        } else {
            chooser.setCurrentDirectory(new File(System.getProperty("user.home"))); // NOI18N
        }
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        setInstallLocation(chooser.getSelectedFile().getAbsolutePath());
}//GEN-LAST:event_jButton1ActionPerformed

    private void derbyInstallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_derbyInstallActionPerformed
    // TODO add your handling code here:
    
}//GEN-LAST:event_derbyInstallActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        String derbySystemHome = getDerbySystemHome();
        if (derbySystemHome.length() > 0) {
            chooser.setSelectedFile(new File(derbySystemHome));
        } else {
            chooser.setCurrentDirectory(new File(System.getProperty("user.home"))); // NOI18N
        }
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        setDerbySystemHome(chooser.getSelectedFile().getAbsolutePath());
    }//GEN-LAST:event_browseButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton browseButton;
    public javax.swing.JTextField derbyInstall;
    public javax.swing.JTextPane derbyInstallInfo;
    public javax.swing.JLabel derbySystemHomeLabel;
    public javax.swing.JTextField derbySystemHomeTextField;
    public javax.swing.JLabel installLabel;
    public javax.swing.JButton jButton1;
    // End of variables declaration//GEN-END:variables
    
    
    private static class RegisterSampleDatabase {

        private static final Logger LOG = Logger.getLogger(RegisterSampleDatabase.class.getName());
        
        private static final String DRIVER_CLASS_NET = "org.apache.derby.jdbc.ClientDriver"; // NOI18N
        private static final String CONN_NAME = "jdbc:derby://localhost:" + RegisterDerby.getDefault().getPort() + "/sample [app on APP]";  // NOI18N
        private boolean registered;

        RegisterSampleDatabase() {
            if (JDBCDriverManager.getDefault().getDrivers(DRIVER_CLASS_NET).length == 0) {
                JDBCDriverManager.getDefault().addDriverListener(jdbcDriverListener);
            }
        }
        private final JDBCDriverListener jdbcDriverListener = new JDBCDriverListener() {
            @Override
            public void driversChanged() {
                registerDatabase();
            }
        };

        void registerDatabase() {
            synchronized (this) {
                if (registered) {
                    return;
                }

                // We do this ahead of time to prevent another thread from
                // double-registering the connections.
                registered = true;
            }

            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        JDBCDriver[] drvsArray = JDBCDriverManager.getDefault().getDrivers(DRIVER_CLASS_NET);
                        if ((drvsArray.length > 0) && (ConnectionManager.getDefault().getConnection(CONN_NAME) == null)) {
                            DerbyDatabases.createSampleDatabase();
                        }
                    } catch (IOException | DatabaseException ioe) {
                        LOG.log(Level.INFO, "", ioe);
                        NotifyDescriptor nd = new NotifyDescriptor.Message(
                                "Failed to ceate sample database:\n" + ioe.getLocalizedMessage(),
                                NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);
                    } finally {
                        JDBCDriverManager.getDefault().removeDriverListener(jdbcDriverListener);
                    }
                }
            });
        }
    }
}
