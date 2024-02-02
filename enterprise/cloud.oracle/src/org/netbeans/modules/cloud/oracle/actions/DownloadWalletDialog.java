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
package org.netbeans.modules.cloud.oracle.actions;

import org.netbeans.modules.cloud.oracle.items.OCIItem;
import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "WalletText=Database connections to your Autonomous Database use a secure connection. \n" +
            "The wallet file will be required to configure your database clients and tools to access Autonomous Database.\n" +
            "Please create a password for this wallet. Some database clients will require that you provide both the wallet \n" +
            "and password to connect to your database (other clients will auto-login using the wallet without a password).",
    "DownloadTitle=Download Database Wallet",
    "SelectButton=Select",
    "Lenght=The wallet download password should be at least 8 characters long.",
    "Match=Passwords don't match.",
    "OneNumber=The wallet download password should contain at least 1 number.",
    "OneSpecial=The wallet download password should contain at least 1 special character.",
    "OneLetter=The wallet download password should contain at least 1 letter.",
    "JDBCUsername=Enter the connection username",
    "JDBCPassword=Enter the conenction password"
})
final class DownloadWalletDialog extends AbstractPasswordPanel {

    public static final String WALLETS_PATH = "Databases/Wallets"; // NOI18N
    private static final String LAST_USED_DIR = "lastUsedDir";
    
    /**
     * Creates new DownloadWalletDialog form
     */
    DownloadWalletDialog() {
        initComponents();
        DocumentListener docListener = new TextFieldListener();
        jTextFieldLocation.getDocument().addDocumentListener(docListener);
        dbUserField.getDocument().addDocumentListener(docListener);
        dbPasswordField.getDocument().addDocumentListener(docListener);
    }
    
    static Optional<WalletInfo> showDialog(OCIItem db) {
        File home = new File(System.getProperty("user.home")); //NOI18N
        String lastUsedDir = NbPreferences.forModule(DownloadWalletAction.class).get(LAST_USED_DIR, home.getAbsolutePath()); //NOI18N
                
        if (!GraphicsEnvironment.isHeadless()) {
            DownloadWalletDialog dlgPanel = new DownloadWalletDialog();
            DialogDescriptor descriptor = new DialogDescriptor(dlgPanel, Bundle.DownloadTitle()); //NOI18N
            dlgPanel.setDescriptor(descriptor);
            descriptor.createNotificationLineSupport();
            Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setMinimumSize(dlgPanel.getPreferredSize());
            dlgPanel.jTextFieldLocation.setText(lastUsedDir);
            dialog.setVisible(true);
            if (DialogDescriptor.OK_OPTION == descriptor.getValue()) {
                String path = dlgPanel.jTextFieldLocation.getText();
                String dbUser = dlgPanel.dbUserField.getText();
                char[] dbPasswd = dlgPanel.dbPasswordField.getPassword();
                NbPreferences.forModule(DownloadWalletAction.class).put(LAST_USED_DIR, path); //NOI18N
                return Optional.of(new WalletInfo(path, generatePassword(), dbUser, dbPasswd, db.getKey().getValue()));
            }
        } else {
            try {
                File walletsDir = getWalletsDir();
                NotifyDescriptor.InputLine inp = new NotifyDescriptor.InputLine(Bundle.JDBCUsername(), Bundle.JDBCUsername());
                Object selected = DialogDisplayer.getDefault().notify(inp);
                
                if (DialogDescriptor.OK_OPTION != selected) {
                    return Optional.empty();
                }
                String username = inp.getInputText().toUpperCase(Locale.US);
                
                inp = new NotifyDescriptor.PasswordLine(Bundle.JDBCPassword(), Bundle.JDBCPassword());
                selected = DialogDisplayer.getDefault().notify(inp);
                if (DialogDescriptor.OK_OPTION != selected) {
                    return Optional.empty();
                }
                char[] password = inp.getInputText().toCharArray();
                return Optional.of(new WalletInfo(walletsDir.getAbsolutePath(), generatePassword(), username, password, db.getKey().getValue()));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return Optional.empty();
    }
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextArea1 = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldLocation = new javax.swing.JTextField();
        jButtonBrowse = new javax.swing.JButton();
        dbUserLabel = new javax.swing.JLabel();
        dbPasswordLabel = new javax.swing.JLabel();
        dbPasswordField = new javax.swing.JPasswordField();
        dbUserField = new javax.swing.JTextField();
        addDBCheckbox = new javax.swing.JCheckBox();

        setMaximumSize(null);
        setMinimumSize(new java.awt.Dimension(714, 234));

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(1);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText(org.openide.util.NbBundle.getMessage(DownloadWalletDialog.class, "DownloadWalletDialog.jTextArea1.text")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setBorder(null);
        jTextArea1.setFocusTraversalKeysEnabled(false);
        jTextArea1.setFocusable(false);
        jTextArea1.setMinimumSize(new java.awt.Dimension(702, 113));
        jTextArea1.setOpaque(false);
        jTextArea1.setPreferredSize(new java.awt.Dimension(702, 113));
        jTextArea1.setRequestFocusEnabled(false);

        jLabel3.setLabelFor(jTextFieldLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(DownloadWalletDialog.class, "DownloadWalletDialog.jLabel3.text")); // NOI18N

        jTextFieldLocation.setText(org.openide.util.NbBundle.getMessage(DownloadWalletDialog.class, "DownloadWalletDialog.jTextFieldLocation.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonBrowse, org.openide.util.NbBundle.getMessage(DownloadWalletDialog.class, "DownloadWalletDialog.jButtonBrowse.text")); // NOI18N
        jButtonBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowseActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(dbUserLabel, org.openide.util.NbBundle.getMessage(DownloadWalletDialog.class, "DownloadWalletDialog.dbUserLabel.text")); // NOI18N
        dbUserLabel.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(dbPasswordLabel, org.openide.util.NbBundle.getMessage(DownloadWalletDialog.class, "DownloadWalletDialog.dbPasswordLabel.text")); // NOI18N
        dbPasswordLabel.setEnabled(false);

        dbPasswordField.setText(org.openide.util.NbBundle.getMessage(DownloadWalletDialog.class, "DownloadWalletDialog.dbPasswordField.text")); // NOI18N
        dbPasswordField.setEnabled(false);

        dbUserField.setText(org.openide.util.NbBundle.getMessage(DownloadWalletDialog.class, "DownloadWalletDialog.dbUserField.text")); // NOI18N
        dbUserField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(addDBCheckbox, org.openide.util.NbBundle.getMessage(DownloadWalletDialog.class, "DownloadWalletDialog.addDBCheckbox.text")); // NOI18N
        addDBCheckbox.setToolTipText(org.openide.util.NbBundle.getMessage(DownloadWalletDialog.class, "DownloadWalletDialog.addDBCheckbox.toolltip")); // NOI18N
        addDBCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDBCheckboxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextArea1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(dbUserLabel)
                            .addComponent(dbPasswordLabel))
                        .addGap(27, 27, 27)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTextFieldLocation, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dbPasswordField)
                            .addComponent(dbUserField))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonBrowse))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addDBCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextArea1, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextFieldLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonBrowse))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addDBCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dbUserLabel)
                    .addComponent(dbUserField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dbPasswordLabel)
                    .addComponent(dbPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(Bundle.LBL_SaveWallet());
        File folder = new File(jTextFieldLocation.getText());
        if (!folder.exists()) {
            folder = folder.getParentFile();
        }
        chooser.setCurrentDirectory(folder);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if( JFileChooser.APPROVE_OPTION == chooser.showDialog(WindowManager.getDefault().getMainWindow(),  Bundle.SelectButton())) {
            jTextFieldLocation.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonBrowseActionPerformed

    private void addDBCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDBCheckboxActionPerformed
        boolean selected = addDBCheckbox.isSelected();
        dbUserLabel.setEnabled(selected);
        dbUserField.setEnabled(selected);
        dbPasswordLabel.setEnabled(selected);
        dbPasswordField.setEnabled(selected);
        validateDialog();
    }//GEN-LAST:event_addDBCheckboxActionPerformed

    @Override
    protected void checkPassword() {
    }
    
    private boolean isValidDialog() {
        if (jTextFieldLocation.getText().isEmpty()) return false;
        if (!addDBCheckbox.isSelected()) return true;
        if (dbUserField.getText().isEmpty()) return false;
        if (dbPasswordField.getPassword() == null) return false;
        if (dbPasswordField.getPassword().length == 0) return false;
        return true;
    }

    private void validateDialog() {
        descriptor.setValid(isValidDialog());
    }

    static File getWalletsDir() throws IOException {
        FileObject fo = FileUtil.createFolder(FileUtil.getConfigRoot(), WALLETS_PATH);
        return FileUtil.toFile(fo);
    }

    protected class TextFieldListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            validateDialog();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            validateDialog();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            validateDialog();
        }
    }
    
    static class WalletInfo {
        private String path;
        private char[] walletPassword;
        private String dbUser;
        private char[] dbPassword;
        private String ocid;

        public WalletInfo(String path, char[] walletPassword, String dbUser, char[] dbPassword, String ocid) {
            this.path = path;
            this.walletPassword = walletPassword;
            this.dbUser = dbUser;
            this.dbPassword = dbPassword;
            this.ocid = ocid;
        }

        public String getPath() {
            return path;
        }

        public char[] getWalletPassword() {
            return walletPassword;
        }

        public String getDbUser() {
            return dbUser;
        }

        public char[] getDbPassword() {
            return dbPassword;
        }

        public String getOcid() {
            return ocid;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox addDBCheckbox;
    private javax.swing.JPasswordField dbPasswordField;
    private javax.swing.JLabel dbPasswordLabel;
    private javax.swing.JTextField dbUserField;
    private javax.swing.JLabel dbUserLabel;
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextFieldLocation;
    // End of variables declaration//GEN-END:variables
}
