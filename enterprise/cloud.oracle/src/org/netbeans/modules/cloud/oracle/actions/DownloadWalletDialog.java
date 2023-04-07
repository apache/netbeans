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
import java.util.Locale;
import java.util.Optional;
import javax.swing.JFileChooser;
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
    "WalletPassword=Enter the wallet password",
    "WalletReEnterPassword=Re-enter the wallet password",
    "JDBCUsername=Enter the connection username",
    "JDBCPassword=Enter the conenction password"
})
final class DownloadWalletDialog extends AbstractPasswordPanel {

    public static final String WALLETS_PATH = "Databases/Wallets"; // NOI18N
    private static final String LAST_USED_DIR = "lastUsedDir";
    
    /**
     * Creates new form NewJPanel
     */
    DownloadWalletDialog() {
        initComponents();
        DocumentListener docListener = new PasswordListener();
        jPasswordField.getDocument().addDocumentListener(docListener);
        jPasswordFieldConfirm.getDocument().addDocumentListener(docListener);
    }
    
    static Optional<WalletInfo> showDialog(OCIItem db) {
        File home = new File(System.getProperty("user.home")); //NOI18N
        String lastUsedDir = NbPreferences.forModule(DownloadWalletAction.class).get(LAST_USED_DIR, home.getAbsolutePath()); //NOI18N
                
        if (!GraphicsEnvironment.isHeadless()) {
            DownloadWalletDialog dlgPanel = new DownloadWalletDialog();
            dlgPanel.jTextFieldLocation.setText(lastUsedDir);
            DialogDescriptor descriptor = new DialogDescriptor(dlgPanel, Bundle.DownloadTitle()); //NOI18N
            dlgPanel.setDescriptor(descriptor);
            descriptor.createNotificationLineSupport();
            Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setMinimumSize(dlgPanel.getPreferredSize());
            dialog.setVisible(true);
            if (DialogDescriptor.OK_OPTION == descriptor.getValue()) {
                String path = dlgPanel.jTextFieldLocation.getText();
                char[] passwd = dlgPanel.jPasswordField.getPassword();
                NbPreferences.forModule(DownloadWalletAction.class).put(LAST_USED_DIR, path); //NOI18N
                return Optional.of(new WalletInfo(path, passwd, null, null));
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
                return Optional.of(new WalletInfo(walletsDir.getAbsolutePath(), generatePassword(), username, password));
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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldLocation = new javax.swing.JTextField();
        jPasswordFieldConfirm = new javax.swing.JPasswordField();
        jPasswordField = new javax.swing.JPasswordField();
        jButtonBrowse = new javax.swing.JButton();

        setMaximumSize(null);
        setMinimumSize(new java.awt.Dimension(714, 234));

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(1);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText(org.openide.util.NbBundle.getMessage(DownloadWalletDialog.class, "DownloadWalletDialog.jTextArea1.text")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setBorder(null);
        jTextArea1.setDragEnabled(false);
        jTextArea1.setFocusTraversalKeysEnabled(false);
        jTextArea1.setFocusable(false);
        jTextArea1.setMinimumSize(new java.awt.Dimension(702, 113));
        jTextArea1.setOpaque(false);
        jTextArea1.setPreferredSize(new java.awt.Dimension(702, 113));
        jTextArea1.setRequestFocusEnabled(false);

        jLabel1.setLabelFor(jPasswordField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DownloadWalletDialog.class, "DownloadWalletDialog.jLabel1.text")); // NOI18N

        jLabel2.setLabelFor(jPasswordFieldConfirm);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DownloadWalletDialog.class, "DownloadWalletDialog.jLabel2.text")); // NOI18N

        jLabel3.setLabelFor(jTextFieldLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(DownloadWalletDialog.class, "DownloadWalletDialog.jLabel3.text")); // NOI18N

        jTextFieldLocation.setText(org.openide.util.NbBundle.getMessage(DownloadWalletDialog.class, "DownloadWalletDialog.jTextFieldLocation.text")); // NOI18N

        jPasswordFieldConfirm.setText(org.openide.util.NbBundle.getMessage(DownloadWalletDialog.class, "DownloadWalletDialog.jPasswordFieldConfirm.text")); // NOI18N

        jPasswordField.setText(org.openide.util.NbBundle.getMessage(DownloadWalletDialog.class, "DownloadWalletDialog.jPasswordField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonBrowse, org.openide.util.NbBundle.getMessage(DownloadWalletDialog.class, "DownloadWalletDialog.jButtonBrowse.text")); // NOI18N
        jButtonBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowseActionPerformed(evt);
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
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPasswordField)
                            .addComponent(jTextFieldLocation)
                            .addComponent(jPasswordFieldConfirm))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonBrowse)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextArea1, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jPasswordFieldConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextFieldLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonBrowse))
                .addContainerGap(16, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(Bundle.LBL_SaveWallet()); //NOI18N
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

    @Override
    protected void checkPassword() {
        char[] passwd1 = jPasswordField.getPassword();
        char[] passwd2 = jPasswordFieldConfirm.getPassword();
        checkPasswordLogic(passwd1, passwd2, (m) -> errorMessage(m));
    }
    
    static File getWalletsDir() throws IOException {
        FileObject fo = FileUtil.createFolder(FileUtil.getConfigRoot(), WALLETS_PATH);
        return FileUtil.toFile(fo);
    }
    
    static class WalletInfo {
        private String path;
        private char[] walletPassword;
        private String dbUser;
        private char[] dbPassword;

        public WalletInfo(String path, char[] walletPassword, String dbUser, char[] dbPassword) {
            this.path = path;
            this.walletPassword = walletPassword;
            this.dbUser = dbUser;
            this.dbPassword = dbPassword;
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
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPasswordField jPasswordField;
    private javax.swing.JPasswordField jPasswordFieldConfirm;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextFieldLocation;
    // End of variables declaration//GEN-END:variables
}
