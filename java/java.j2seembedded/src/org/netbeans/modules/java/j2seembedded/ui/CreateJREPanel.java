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
package org.netbeans.modules.java.j2seembedded.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 *
 * @author Roman Svitanic
 * @author Tomas Zezula
 */
public class CreateJREPanel extends javax.swing.JPanel {

    private static final String HELP_ID = "java.j2seembedded.create-remote-platform";    //NOI18N
    private static final String KEY_EJDK = "ejdk.home"; //NOI18N

    private final JButton buttonCreate;
    private boolean valid = false;

    private CreateJREPanel(
            @NonNull JButton okOption,
            @NullAllowed final String username,
            @NullAllowed final String host) {
        assert username == null ? host == null : host != null;
        buttonCreate = okOption;
        buttonCreate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setEJDKHome(jreCreateLocation.getText());
            }
        });
        initComponents();

        final DocumentListener docListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validatePanel();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validatePanel();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validatePanel();
            }
        };
        jreCreateLocation.getDocument().addDocumentListener(docListener);
        remoteJREPath.getDocument().addDocumentListener(docListener);
        labelRemoteJREInfo.setText(NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.labelRemoteJREInfo.text", username, host)); //NOI18N
        if (username == null || host == null) {
            labelRemoteJREPath.setVisible(false);
            remoteJREPath.setVisible(false);
            labelRemoteJREInfo.setVisible(false);
        } else {
            remoteJREPath.setText(NbBundle.getMessage(CreateJREPanel.class, "LBL_JRE_Path_Default", username)); //NOI18N
        }
        final File path = getEJDKHome();
        if (path != null) {
            jreCreateLocation.setText(path.getAbsolutePath());
        }
        validatePanel();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                remoteJREPath.requestFocusInWindow();
                remoteJREPath.selectAll();
            }
        });
    }

    @CheckForNull
    public static List<String> configure(
            @NonNull File destFolder) {
        Parameters.notNull("destFolder", destFolder);   //NOI18N
        final Pair<List<String>,String> data = configureImpl(null, null, destFolder);
        return data == null ? null : data.first();
    }

    @CheckForNull
    public static Pair<List<String>,String> configure(
        @NonNull final String userName,
        @NonNull final String host,
        @NonNull File destFolder) {
        Parameters.notNull("destFolder", destFolder);   //NOI18N
        Parameters.notNull("userName", userName);   //NOI18N
        Parameters.notNull("host", host);   //NOI18N        
        return configureImpl(userName, host, destFolder);
    }

    @CheckForNull
    private static Pair<List<String>,String> configureImpl(
        @NullAllowed final String userName,
        @NullAllowed final String host,
        @NonNull final File destFolder) {
        JButton buttonCreate = new JButton(NbBundle.getMessage(CreateJREPanel.class, "LBL_Dialog_Button_Create"));
        final CreateJREPanel panel = new CreateJREPanel(buttonCreate, userName, host);
        DialogDescriptor dd = new DialogDescriptor(
                panel,
                NbBundle.getMessage(CreateJREPanel.class, "LBL_CreateJRETitle"), //NOI18N
                true,
                new Object[]{buttonCreate, DialogDescriptor.CANCEL_OPTION},
                0,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(HELP_ID),
                null);
        if (DialogDisplayer.getDefault().notify(dd).equals(buttonCreate)) {
            if (!panel.isPanelValid()) {
                DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                    NbBundle.getMessage(CreateJREPanel.class, "ERROR_Invalid_CreateJREPanel"), //NOI18N
                    NotifyDescriptor.WARNING_MESSAGE));
                return null;
            }
            final List<String> cmdLine = new ArrayList<>();
            final File ejdk = new File(panel.getJRECreateLocation());
            final File bin = new File(ejdk, "bin");   //NOI18N
            File jrecreate = null;
            if (Utilities.isWindows()) {
                cmdLine.add("cmd"); //NOI18N
                cmdLine.add("/c"); //NOI18N
                jrecreate = new File(bin, "jrecreate.bat"); //NOI18N
            } else {
                jrecreate = new File(bin, "jrecreate.sh");   //NOI18N
            }
            cmdLine.add(jrecreate.getAbsolutePath());
            cmdLine.add("--dest");          //NOI18N
            cmdLine.add(destFolder.getAbsolutePath());
            cmdLine.add("--ejdk-home");     //NOI18N
            cmdLine.add(ejdk.getAbsolutePath());
            final String profile = panel.getProfile();
            if (profile != null) {
                cmdLine.add("--profile");     //NOI18N
                cmdLine.add(profile);
            }
            cmdLine.add("--vm");     //NOI18N
            cmdLine.add(panel.getVirtualMachine());
            if (panel.isDebug()) {
                cmdLine.add("--debug");   //NOI18N
            }
            if (panel.isKeepDebugInfo()) {
                cmdLine.add("--keep-debug-info");   //NOI18N
            }
            if (panel.isNoCompression()) {
                cmdLine.add("--no-compression");   //NOI18N
            }
            final StringBuilder extensions = new StringBuilder();
            if (panel.isGcf()) {
                extensions.append("gcf");  //NOI18N
            }
            if (panel.isSunec()) {
                if (extensions.length() > 0) {
                    extensions.append(","); //NOI18N
                }
                extensions.append("sunec");        //NOI18N
            }
            if (panel.isSunpkcs11()) {
                if (extensions.length() > 0) {
                    extensions.append(","); //NOI18N
                }
                extensions.append("sunpkcs11");        //NOI18N
            }
            if (panel.isLocales()) {
                if (extensions.length() > 0) {
                    extensions.append(","); //NOI18N
                }
                extensions.append("locales");        //NOI18N
            }
            if (panel.isCharsets()) {
                if (extensions.length() > 0) {
                    extensions.append(","); //NOI18N
                }
                extensions.append("charsets");        //NOI18N
            }
            if (panel.isNashorn()) {
                if (extensions.length() > 0) {
                    extensions.append(","); //NOI18N
                }
                extensions.append("nashorn");        //NOI18N
            }
            if (extensions.length() > 0) {
                cmdLine.add("--extension"); //NOI18N
                cmdLine.add(extensions.toString());
            }
            return  Pair.<List<String>,String>of(cmdLine, panel.getRemoteJREPath());
        }
        return null;
    }

    private void validatePanel() {
        if (jreCreateLocation.getText().isEmpty()) {
            labelError.setText(NbBundle.getMessage(CreateJREPanel.class, "ERROR_JRE_Create")); //NOI18N
            valid = false;
            buttonCreate.setEnabled(false);
            return;
        }
        if (remoteJREPath.isVisible() && remoteJREPath.getText().isEmpty()) {
            labelError.setText(NbBundle.getMessage(CreateJREPanel.class, "ERROR_JRE_Path")); //NOI18N
            valid = false;
            buttonCreate.setEnabled(false);
            return;
        }
        labelError.setText(null);
        valid = true;
        buttonCreate.setEnabled(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelOptions = new javax.swing.JLabel();
        checkBoxDebug = new javax.swing.JCheckBox();
        checkBoxKeepDebugInfo = new javax.swing.JCheckBox();
        checkBoxNoCompression = new javax.swing.JCheckBox();
        labelProfile = new javax.swing.JLabel();
        comboBoxProfile = new javax.swing.JComboBox();
        comboBoxVM = new javax.swing.JComboBox();
        labelVM = new javax.swing.JLabel();
        labelExtensions = new javax.swing.JLabel();
        checkBoxSunec = new javax.swing.JCheckBox();
        checkBoxSunpkcs11 = new javax.swing.JCheckBox();
        checkBoxLocales = new javax.swing.JCheckBox();
        checkBoxCharsets = new javax.swing.JCheckBox();
        checkBoxNashorn = new javax.swing.JCheckBox();
        labelError = new javax.swing.JLabel();
        labelJRECreateLocation = new javax.swing.JLabel();
        jreCreateLocation = new javax.swing.JTextField();
        buttonBrowse = new javax.swing.JButton();
        labelJRECreateInfo = new javax.swing.JLabel();
        labelRemoteJREPath = new javax.swing.JLabel();
        remoteJREPath = new javax.swing.JTextField();
        labelRemoteJREInfo = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        checkBoxGcf = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(labelOptions, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.labelOptions.text")); // NOI18N

        checkBoxDebug.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(checkBoxDebug, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.checkBoxDebug.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxKeepDebugInfo, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.checkBoxKeepDebugInfo.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxNoCompression, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.checkBoxNoCompression.text")); // NOI18N

        labelProfile.setLabelFor(comboBoxProfile);
        org.openide.awt.Mnemonics.setLocalizedText(labelProfile, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.labelProfile.text")); // NOI18N

        comboBoxProfile.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Compact1", "Compact2", "Compact3", "Full JRE" }));
        comboBoxProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxProfileActionPerformed(evt);
            }
        });

        comboBoxVM.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Minimal", "Client", "Server", "All" }));

        labelVM.setLabelFor(comboBoxVM);
        org.openide.awt.Mnemonics.setLocalizedText(labelVM, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.labelVM.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelExtensions, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.labelExtensions.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxSunec, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.checkBoxSunec.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxSunpkcs11, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.checkBoxSunpkcs11.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxLocales, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.checkBoxLocales.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxCharsets, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.checkBoxCharsets.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxNashorn, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.checkBoxNashorn.text")); // NOI18N

        labelError.setForeground(new java.awt.Color(255, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(labelError, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.labelError.text")); // NOI18N

        labelJRECreateLocation.setLabelFor(labelJRECreateLocation);
        org.openide.awt.Mnemonics.setLocalizedText(labelJRECreateLocation, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.labelJRECreateLocation.text")); // NOI18N

        jreCreateLocation.setText(org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.jreCreateLocation.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(buttonBrowse, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.buttonBrowse.text")); // NOI18N
        buttonBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonBrowseActionPerformed(evt);
            }
        });

        labelJRECreateInfo.setFont(labelJRECreateInfo.getFont().deriveFont((labelJRECreateInfo.getFont().getStyle() | java.awt.Font.ITALIC)));
        org.openide.awt.Mnemonics.setLocalizedText(labelJRECreateInfo, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.labelJRECreateInfo.text")); // NOI18N

        labelRemoteJREPath.setLabelFor(remoteJREPath);
        org.openide.awt.Mnemonics.setLocalizedText(labelRemoteJREPath, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.labelRemoteJREPath.text")); // NOI18N

        remoteJREPath.setText(org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.remoteJREPath.text")); // NOI18N

        labelRemoteJREInfo.setFont(labelRemoteJREInfo.getFont().deriveFont((labelRemoteJREInfo.getFont().getStyle() | java.awt.Font.ITALIC)));
        org.openide.awt.Mnemonics.setLocalizedText(labelRemoteJREInfo, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.labelRemoteJREInfo.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxGcf, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.checkBoxGcf.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkBoxDebug)
                            .addComponent(checkBoxKeepDebugInfo)
                            .addComponent(checkBoxNoCompression))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelProfile)
                                    .addComponent(labelVM))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(comboBoxVM, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(comboBoxProfile, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelJRECreateLocation)
                                    .addComponent(labelRemoteJREPath))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jreCreateLocation)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(buttonBrowse))
                                    .addComponent(remoteJREPath)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelJRECreateInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                                    .addComponent(labelRemoteJREInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelOptions)
                                    .addComponent(labelExtensions)
                                    .addComponent(labelError)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(12, 12, 12)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(checkBoxGcf, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(checkBoxLocales)
                                            .addComponent(checkBoxSunpkcs11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(18, 18, 18)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(checkBoxNashorn)
                                            .addComponent(checkBoxCharsets)
                                            .addComponent(checkBoxSunec))))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelJRECreateLocation)
                    .addComponent(jreCreateLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonBrowse))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelJRECreateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelRemoteJREPath)
                    .addComponent(remoteJREPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelRemoteJREInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelOptions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(checkBoxDebug)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxKeepDebugInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxNoCompression)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelProfile)
                    .addComponent(comboBoxProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboBoxVM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelVM))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelExtensions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxSunpkcs11)
                    .addComponent(checkBoxCharsets))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxSunec)
                    .addComponent(checkBoxGcf))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxNashorn)
                    .addComponent(checkBoxLocales))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 59, Short.MAX_VALUE)
                .addComponent(labelError))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void buttonBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonBrowseActionPerformed
        final String oldValue = jreCreateLocation.getText();
        final JFileChooser chooser = new JFileChooser() {
            @Override
            public void approveSelection() {
                if (EJDKFileView.isEJDK(getSelectedFile())) {
                    super.approveSelection();
                } else {
                    DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(
                            NbBundle.getMessage(CreateJREPanel.class, "TXT_InvalidEJDKFolder", getSelectedFile().getName()),
                            NotifyDescriptor.ERROR_MESSAGE));
                }
            }
        };
        chooser.setFileView(new EJDKFileView(chooser.getFileSystemView()));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (oldValue != null) {
            chooser.setSelectedFile(new File(oldValue));
        }
        chooser.setDialogTitle(NbBundle.getMessage(CreateJREPanel.class, "Title_Chooser_SelectJRECreate")); //NOI18N
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            jreCreateLocation.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_buttonBrowseActionPerformed

    private void comboBoxProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxProfileActionPerformed
        if (comboBoxProfile.getSelectedIndex() == 0 || comboBoxProfile.getSelectedIndex() == 1) {
            comboBoxVM.setSelectedIndex(0);
        } else if (comboBoxProfile.getSelectedIndex() == 2) {
            comboBoxVM.setSelectedIndex(1);
        } else if (comboBoxProfile.getSelectedIndex() == 3) {
            comboBoxVM.setSelectedIndex(3);
        }
    }//GEN-LAST:event_comboBoxProfileActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonBrowse;
    private javax.swing.JCheckBox checkBoxCharsets;
    private javax.swing.JCheckBox checkBoxDebug;
    private javax.swing.JCheckBox checkBoxGcf;
    private javax.swing.JCheckBox checkBoxKeepDebugInfo;
    private javax.swing.JCheckBox checkBoxLocales;
    private javax.swing.JCheckBox checkBoxNashorn;
    private javax.swing.JCheckBox checkBoxNoCompression;
    private javax.swing.JCheckBox checkBoxSunec;
    private javax.swing.JCheckBox checkBoxSunpkcs11;
    private javax.swing.JComboBox comboBoxProfile;
    private javax.swing.JComboBox comboBoxVM;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jreCreateLocation;
    private javax.swing.JLabel labelError;
    private javax.swing.JLabel labelExtensions;
    private javax.swing.JLabel labelJRECreateInfo;
    private javax.swing.JLabel labelJRECreateLocation;
    private javax.swing.JLabel labelOptions;
    private javax.swing.JLabel labelProfile;
    private javax.swing.JLabel labelRemoteJREInfo;
    private javax.swing.JLabel labelRemoteJREPath;
    private javax.swing.JLabel labelVM;
    private javax.swing.JTextField remoteJREPath;
    // End of variables declaration//GEN-END:variables

    public String getJRECreateLocation() {
        return jreCreateLocation.getText();
    }

    public String getRemoteJREPath() {
        return remoteJREPath.getText();
    }

    public String getProfile() {
        String profile = (String) comboBoxProfile.getSelectedItem();
        if (profile == null ||
            profile.equals(comboBoxProfile.getModel().getElementAt(comboBoxProfile.getModel().getSize()-1))) {
            return null;
        }
        return profile.toLowerCase();
    }

    public String getVirtualMachine() {
        return ((String) comboBoxVM.getSelectedItem()).toLowerCase();
    }

    public boolean isDebug() {
        return checkBoxDebug.isSelected();
    }

    public boolean isKeepDebugInfo() {
        return checkBoxKeepDebugInfo.isSelected();
    }

    public boolean isNoCompression() {
        return checkBoxNoCompression.isSelected();
    }

    public boolean isGcf() {
        return checkBoxGcf.isSelected();
    }

    public boolean isSunec() {
        return checkBoxSunec.isSelected();
    }

    public boolean isSunpkcs11() {
        return checkBoxSunpkcs11.isSelected();
    }

    public boolean isLocales() {
        return checkBoxLocales.isSelected();
    }

    public boolean isCharsets() {
        return checkBoxCharsets.isSelected();
    }

    public boolean isNashorn() {
        return checkBoxNashorn.isSelected();
    }

    public boolean isPanelValid() {
        return valid;
    }

    @CheckForNull
    private static File getEJDKHome() {
        final Preferences prefs = NbPreferences.forModule(CreateJREPanel.class);
        final String path = prefs.get(KEY_EJDK, null);
        return path == null ? null : new File(path);
    }

    private static void setEJDKHome(@NullAllowed final String path) {
        final Preferences prefs = NbPreferences.forModule(CreateJREPanel.class);
        prefs.put(
            KEY_EJDK,
            path == null || path.isEmpty() ?
                null :
                path);
    }

    private static final class EJDKFileView extends FileView {
        private static final Icon BADGE = ImageUtilities.loadIcon("org/netbeans/modules/java/j2seembedded/resources/ejdkBadge.gif"); // NOI18N
        private static final Icon EMPTY = ImageUtilities.loadIcon("org/netbeans/modules/java/j2seembedded/resources/empty.gif"); // NOI18N

        private final FileSystemView fsv;
        private Icon lastOrig;
        private Icon lastMerged;

        public EJDKFileView(@NonNull final FileSystemView fsv) {
            this.fsv = fsv;
        }

        @Override
        public Icon getIcon(@NonNull final File file) {
            final File f = FileUtil.normalizeFile(file);
            Icon original = fsv.getSystemIcon(f);
            if (original == null) {
                // L&F (e.g. GTK) did not specify any icon.
                original = EMPTY;
            }
            if (isEJDK(f)) {
                if (original.equals(lastOrig)) {
                    assert lastMerged != null;
                    return lastMerged;
                }
                lastMerged = ImageUtilities.mergeIcons(
                        original,
                        BADGE,
                        original.getIconWidth() - BADGE.getIconWidth(),
                        original.getIconHeight()- BADGE.getIconHeight());
                lastOrig = original;
                return lastMerged;
            } else {
                return original;
            }
        }

        static boolean isEJDK(@NonNull final File folder) {
            //XXX: Workaround of hard NFS mounts on Solaris.
            final int osId = Utilities.getOperatingSystem();
            if (osId == Utilities.OS_SOLARIS || osId == Utilities.OS_SUNOS) {
                return false;
            }
            final String jrecreateName = Utilities.isWindows() ?
                "jrecreate.bat" :  //NOI18N
                "jrecreate.sh";    //NOI18N
            final File jrecreate = new File(
                new File(folder, "bin"),    //NOI18N
                jrecreateName);
            return jrecreate.exists();
        }        
    }
}
