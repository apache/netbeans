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
package org.netbeans.modules.java.j2seembedded.wizard;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.java.j2seembedded.platform.ConnectionMethod;
import org.netbeans.modules.java.j2seembedded.platform.RemotePlatformProbe;
import org.netbeans.modules.java.j2seembedded.platform.RemotePlatformProvider;
import org.netbeans.modules.java.j2seembedded.ui.CreateJREPanel;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 * @author Roman Svitanic
 */
public class SetUpRemotePlatform extends javax.swing.JPanel {

    private static final String HELP_ID = "java.j2seembedded.setup-remote-platform";    //NOI18N
    private static final String ENV_JAVA_HOME = "JAVA_HOME";    //NOI18N    
    private final ChangeSupport cs = new ChangeSupport(this);
    private String currentDefaultWorkDir;
    private WizardDescriptor wizardDescriptor;

    /**
     * Creates new form SetUpRemotePlatform
     */
    private SetUpRemotePlatform() {
        initComponents();
        postInitComponents();
    }

    private void postInitComponents() {
        for (int i = 1;; i++) {
            displayName.setText("Remote Platform " + i); //NOI18N
            String antName = PropertyUtils.getUsablePropertyName(displayName.getText());
            if (RemotePlatformProvider.isValidPlatformAntName(antName)) {
                break;
            }
        }
        displayName.selectAll();

        final ChangeListener radioChangeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                toggleAuthMethod(radioButtonPassword.isSelected());
                cs.fireChange();
            }
        };
        this.radioButtonKey.addChangeListener(radioChangeListener);
        this.radioButtonPassword.addChangeListener(radioChangeListener);
        this.radioButtonPassword.setSelected(true);

        final DocumentListener docListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateDefaultDirectory(e);
                cs.fireChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateDefaultDirectory(e);
                cs.fireChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateDefaultDirectory(e);
                cs.fireChange();
            }
        };
        this.displayName.getDocument().addDocumentListener(docListener);
        this.host.getDocument().addDocumentListener(docListener);
        this.username.getDocument().addDocumentListener(docListener);
        this.password.getDocument().addDocumentListener(docListener);
        this.keyFilePath.getDocument().addDocumentListener(docListener);
        this.passphrase.getDocument().addDocumentListener(docListener);
        this.jreLocation.getDocument().addDocumentListener(docListener);
        this.workingDir.getDocument().addDocumentListener(docListener);
    }

    private void toggleAuthMethod(boolean usePasswordAuth) {
        password.setEnabled(usePasswordAuth);
        keyFilePath.setEnabled(!usePasswordAuth);
        passphrase.setEnabled(!usePasswordAuth);
        buttonBrowse.setEnabled(!usePasswordAuth);
    }

    public final synchronized void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    public final synchronized void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

    private void updateDefaultDirectory(DocumentEvent e) {
        Document doc = e.getDocument();
        if (doc.equals(username.getDocument())) {
            String usernameText = username.getText();
            if (!usernameText.isEmpty()) {
                String workdirText = workingDir.getText();
                if (workdirText.isEmpty() || (currentDefaultWorkDir != null && workdirText.equals(currentDefaultWorkDir))) {
                    String updatedDefaultworkDir = "/home/" + usernameText + "/NetBeansProjects/"; //NOI18N
                    workingDir.setText(updatedDefaultworkDir);
                    currentDefaultWorkDir = updatedDefaultworkDir;
                }
            } else {
                if (currentDefaultWorkDir != null && workingDir.getText().equals(currentDefaultWorkDir)) {
                    workingDir.setText(""); //NOI18N
                    currentDefaultWorkDir = null;
                }
            }
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
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroupAuth = new javax.swing.ButtonGroup();
        hostLabel = new javax.swing.JLabel();
        host = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        port = new javax.swing.JSpinner();
        usernameLabel = new javax.swing.JLabel();
        username = new javax.swing.JTextField();
        radioButtonPassword = new javax.swing.JRadioButton();
        passwordLabel = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();
        radioButtonKey = new javax.swing.JRadioButton();
        keyFileLabel = new javax.swing.JLabel();
        keyFilePath = new javax.swing.JTextField();
        buttonBrowse = new javax.swing.JButton();
        passphraseLabel = new javax.swing.JLabel();
        passphrase = new javax.swing.JPasswordField();
        jSeparator1 = new javax.swing.JSeparator();
        jreLocationLabel = new javax.swing.JLabel();
        jreLocation = new javax.swing.JTextField();
        workingDirLabel = new javax.swing.JLabel();
        workingDir = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();
        displayNameLabel = new javax.swing.JLabel();
        displayName = new javax.swing.JTextField();
        fillerBottomVertical = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        buttonCreate = new javax.swing.JButton();

        setName(org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "TXT_SetUpRemotePlatform")); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        hostLabel.setLabelFor(host);
        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.hostLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(hostLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(host, gridBagConstraints);

        portLabel.setLabelFor(port);
        org.openide.awt.Mnemonics.setLocalizedText(portLabel, NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.portLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 0);
        add(portLabel, gridBagConstraints);

        port.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        port.setEditor(new javax.swing.JSpinner.NumberEditor(port, "#0"));
        port.setPreferredSize(new java.awt.Dimension(54, 20));
        port.setValue(22);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(port, gridBagConstraints);

        usernameLabel.setLabelFor(username);
        org.openide.awt.Mnemonics.setLocalizedText(usernameLabel, NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.usernameLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(usernameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(username, gridBagConstraints);

        buttonGroupAuth.add(radioButtonPassword);
        org.openide.awt.Mnemonics.setLocalizedText(radioButtonPassword, NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.radioButtonPassword.text")); // NOI18N
        radioButtonPassword.setActionCommand(org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.radioButtonPassword.actionCommand")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(radioButtonPassword, gridBagConstraints);

        passwordLabel.setLabelFor(password);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.passwordLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(passwordLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(password, gridBagConstraints);

        buttonGroupAuth.add(radioButtonKey);
        org.openide.awt.Mnemonics.setLocalizedText(radioButtonKey, NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.radioButtonKey.text")); // NOI18N
        radioButtonKey.setActionCommand(org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.radioButtonKey.actionCommand")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(radioButtonKey, gridBagConstraints);

        keyFileLabel.setLabelFor(keyFilePath);
        org.openide.awt.Mnemonics.setLocalizedText(keyFileLabel, NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.keyFileLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(keyFileLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(keyFilePath, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(buttonBrowse, NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.buttonBrowse.text")); // NOI18N
        buttonBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(buttonBrowse, gridBagConstraints);

        passphraseLabel.setLabelFor(passphrase);
        org.openide.awt.Mnemonics.setLocalizedText(passphraseLabel, NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.passphraseLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(passphraseLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(passphrase, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        add(jSeparator1, gridBagConstraints);

        jreLocationLabel.setLabelFor(jreLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jreLocationLabel, org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.jreLocationLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jreLocationLabel, gridBagConstraints);

        jreLocation.setText(org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.jreLocation.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(jreLocation, gridBagConstraints);

        workingDirLabel.setLabelFor(workingDir);
        org.openide.awt.Mnemonics.setLocalizedText(workingDirLabel, org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.workingDirLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        add(workingDirLabel, gridBagConstraints);

        workingDir.setText(org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.workingDir.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        add(workingDir, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        add(jSeparator2, gridBagConstraints);

        displayNameLabel.setLabelFor(displayName);
        org.openide.awt.Mnemonics.setLocalizedText(displayNameLabel, org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.displayNameLabel.text")); // NOI18N
        displayNameLabel.setToolTipText(org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.displayNameLabel.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(displayNameLabel, gridBagConstraints);

        displayName.setText(org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.displayName.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(displayName, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        add(fillerBottomVertical, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(buttonCreate, org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.buttonCreate.text")); // NOI18N
        buttonCreate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCreateActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(buttonCreate, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void buttonBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonBrowseActionPerformed
        final String oldValue = keyFilePath.getText();
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (oldValue != null) {
            chooser.setSelectedFile(new File(oldValue));
        }
        chooser.setDialogTitle(NbBundle.getMessage(SetUpRemotePlatform.class, "Title_Chooser_SelectKeyfile")); //NOI18N
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            keyFilePath.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_buttonBrowseActionPerformed

    private void buttonCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCreateActionPerformed
        final File ejreTmp = createJRETempDir();
        final Pair<List<String>,String> data = CreateJREPanel.configure(
            username.getText(),
            host.getText(),
            ejreTmp);
        if (data != null) {
            final ConnectionMethod cm;
            if (radioButtonPassword.isSelected()) {
                cm = ConnectionMethod.sshPassword(
                        host.getText(),
                        ((Integer) port.getValue()).intValue(),
                        username.getText(),
                        String.valueOf(password.getPassword()));
            } else {
                cm = ConnectionMethod.sshKey(
                        host.getText(),
                        ((Integer) port.getValue()).intValue(),
                        username.getText(),
                        new File(keyFilePath.getText()),
                        String.valueOf(passphrase.getPassword()));
            }
            ProgressUtils.showProgressDialogAndRun(
                    new ProgressRunnable<Void>() {
                @Override
                public Void run(ProgressHandle handle) {
                    handle.switchToDeterminate(2);
                    handle.progress(NbBundle.getMessage(SetUpRemotePlatform.class, "LBL_JRECreate"),0);
                    try {
                        int res = jreCreate(data.first());
                        if (res != 0) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    DialogDisplayer.getDefault().notify(
                                            new NotifyDescriptor.Message(
                                            NbBundle.getMessage(SetUpRemotePlatform.class, "ERROR_JRECreate"),
                                            NotifyDescriptor.ERROR_MESSAGE));
                                }
                            });
                            return null;
                        };
                        handle.progress(NbBundle.getMessage(SetUpRemotePlatform.class, "LBL_JREUpload"), 1);
                        res = upload(ejreTmp, data.second(), cm, wizardDescriptor);
                        if (res != 0) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    DialogDisplayer.getDefault().notify(
                                            new NotifyDescriptor.Message(
                                            NbBundle.getMessage(SetUpRemotePlatform.class, "ERROR_JREUpload"),
                                            NotifyDescriptor.ERROR_MESSAGE));
                                }
                            });
                            return null;
                        }
                        handle.finish();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                jreLocation.setText(data.second());
                            }
                        });
                        return null;
                    } finally {
                        deleteAll(ejreTmp);
                    }
                }
            },
                    NbBundle.getMessage(SetUpRemotePlatform.class, "LBL_CreatingNewPlatform"),
                    true);
        }
    }//GEN-LAST:event_buttonCreateActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonBrowse;
    private javax.swing.JButton buttonCreate;
    private javax.swing.ButtonGroup buttonGroupAuth;
    private javax.swing.JTextField displayName;
    private javax.swing.JLabel displayNameLabel;
    private javax.swing.Box.Filler fillerBottomVertical;
    private javax.swing.JTextField host;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jreLocation;
    private javax.swing.JLabel jreLocationLabel;
    private javax.swing.JLabel keyFileLabel;
    private javax.swing.JTextField keyFilePath;
    private javax.swing.JPasswordField passphrase;
    private javax.swing.JLabel passphraseLabel;
    private javax.swing.JPasswordField password;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JSpinner port;
    private javax.swing.JLabel portLabel;
    private javax.swing.JRadioButton radioButtonKey;
    private javax.swing.JRadioButton radioButtonPassword;
    private javax.swing.JTextField username;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JTextField workingDir;
    private javax.swing.JLabel workingDirLabel;
    // End of variables declaration//GEN-END:variables

    private static File createJRETempDir() {
        final File tmpDir = new File(System.getProperty("java.io.tmpdir")); //NOI18N
        final String namePattern = "nb-jrecreate-%d";  //NOI18N
        File tmpJreDir;
        int i = -1;
        do {
            i++;
            tmpJreDir = new File (tmpDir,String.format(namePattern, i));
        } while (tmpJreDir.exists());
        return tmpJreDir;
    }

    private static void deleteAll(File file) {
        if (file.isDirectory()) {
            final File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteAll(child);
                }
            }
        }
        file.delete();
    }

    private static int jreCreate(@NonNull final List<String> cmdLine) {
        ExternalProcessBuilder pb = new ExternalProcessBuilder(cmdLine.get(0));
        pb = pb.addEnvironmentVariable(
            ENV_JAVA_HOME,
            FileUtil.toFile(JavaPlatform.getDefault().getInstallFolders().iterator().next()).getAbsolutePath());
        for (String arg : cmdLine.subList(1, cmdLine.size())) {
            pb = pb.addArgument(arg);
        }
        int res;
        try {
            final Process process = pb.call();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = in.readLine())!= null) {
                    System.out.println(line);
                }
            }
            process.waitFor();
            res = process.exitValue();
        } catch (IOException | InterruptedException e) {
            res = -1;
        }
        return res;
    }

    private static int upload(
            @NonNull final File folder,
            @NonNull final String path,
            @NonNull final ConnectionMethod cm,
            @NonNull final WizardDescriptor wd) {
        final Object scriptObject = wd.getProperty(RemotePlatformIt.PROP_BUILDSCRIPT);
        File buildScript = scriptObject != null ? (File) scriptObject : null;
        if (buildScript == null || !buildScript.exists()) {
            buildScript = RemotePlatformProbe.createBuildScript();
            wd.putProperty(RemotePlatformIt.PROP_BUILDSCRIPT, buildScript);
        }
        return RemotePlatformProbe.uploadJRE(folder.getAbsolutePath(), path, cm, buildScript);
    }

    public void setWizardDescriptor(WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor = wizardDescriptor;
    }

    static class Panel implements WizardDescriptor.AsynchronousValidatingPanel<WizardDescriptor>, ChangeListener {

        private final ChangeSupport changeSupport;
        private SetUpRemotePlatform ui;
        private boolean valid = false;
        private volatile WizardDescriptor wizardDescriptor;
        private volatile ConnectionValidator connectionValidator;

        public Panel() {
            changeSupport = new ChangeSupport(this);
        }

        @Override
        public Component getComponent() {
            if (ui == null) {
                ui = new SetUpRemotePlatform();
                ui.addChangeListener(this);
            }
            checkPanelValidity();
            return ui;
        }

        @Override
        public HelpCtx getHelp() {
            return new HelpCtx(HELP_ID);
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
            wizardDescriptor = settings;
            ui.setWizardDescriptor(wizardDescriptor);

            if (settings.getProperty(RemotePlatformIt.PROP_DISPLAYNAME) != null) {
                ui.displayName.setText((String) settings.getProperty(RemotePlatformIt.PROP_DISPLAYNAME));
            }
            if (settings.getProperty(RemotePlatformIt.PROP_HOST) != null) {
                ui.host.setText((String) settings.getProperty(RemotePlatformIt.PROP_HOST));
            }
            if (settings.getProperty(RemotePlatformIt.PROP_PORT) != null) {
                ui.port.setValue((Integer) settings.getProperty(RemotePlatformIt.PROP_PORT));
            }
            if (settings.getProperty(RemotePlatformIt.PROP_USERNAME) != null) {
                ui.username.setText((String) settings.getProperty(RemotePlatformIt.PROP_USERNAME));
            }            
            if (settings.getProperty(RemotePlatformIt.PROP_PASSWORD) != null) {
                ui.password.setText((String) settings.getProperty(RemotePlatformIt.PROP_PASSWORD));
                ui.radioButtonPassword.setSelected(true);
            }
            if (settings.getProperty(RemotePlatformIt.PROP_KEYFILE) != null) {
                ui.keyFilePath.setText((String) settings.getProperty(RemotePlatformIt.PROP_KEYFILE));
                ui.radioButtonKey.setSelected(true);
            }
            if (settings.getProperty(RemotePlatformIt.PROP_PASSPHRASE) != null) {
                ui.passphrase.setText((String) settings.getProperty(RemotePlatformIt.PROP_PASSPHRASE));
            }
            if (settings.getProperty(RemotePlatformIt.PROP_JREPATH) != null) {
                ui.jreLocation.setText((String) settings.getProperty(RemotePlatformIt.PROP_JREPATH));
            }
            if (settings.getProperty(RemotePlatformIt.PROP_WORKINGDIR) != null) {
                ui.workingDir.setText((String) settings.getProperty(RemotePlatformIt.PROP_WORKINGDIR));
            }
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
            settings.putProperty(RemotePlatformIt.PROP_DISPLAYNAME, ui.displayName.getText());
            settings.putProperty(RemotePlatformIt.PROP_HOST, ui.host.getText());
            settings.putProperty(RemotePlatformIt.PROP_PORT, (Integer) ui.port.getValue());
            settings.putProperty(RemotePlatformIt.PROP_USERNAME, ui.username.getText());
            if (ui.radioButtonPassword.isSelected()) {
                settings.putProperty(RemotePlatformIt.PROP_PASSWORD, String.valueOf(ui.password.getPassword()));
                settings.putProperty(RemotePlatformIt.PROP_KEYFILE, null);
                settings.putProperty(RemotePlatformIt.PROP_PASSPHRASE, null);
            } else {
                settings.putProperty(RemotePlatformIt.PROP_KEYFILE, ui.keyFilePath.getText());
                settings.putProperty(RemotePlatformIt.PROP_PASSPHRASE, String.valueOf(ui.passphrase.getPassword()));
                settings.putProperty(RemotePlatformIt.PROP_PASSWORD, null);
            }
            settings.putProperty(RemotePlatformIt.PROP_JREPATH, ui.jreLocation.getText());
            settings.putProperty(RemotePlatformIt.PROP_WORKINGDIR, ui.workingDir.getText());
        }

        @Override
        public boolean isValid() {
            return valid;
        }

        @Override
        public void addChangeListener(@NonNull final ChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            changeSupport.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(@NonNull final ChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            changeSupport.removeChangeListener(listener);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            valid = checkPanelValidity();
            changeSupport.fireChange();
        }

        private boolean checkPanelValidity() {
            ui.buttonCreate.setEnabled(false);
            if (ui.displayName.getText().isEmpty()) {
                displayNotification(NbBundle.getMessage(SetUpRemotePlatform.class, "ERROR_Empty_DisplayName")); // NOI18N
                return false;
            }
            if (!RemotePlatformProvider.isValidPlatformAntName(PropertyUtils.getUsablePropertyName(ui.displayName.getText()))) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(SetUpRemotePlatform.class, "ERROR_PlatformAlreadyExists")); // NOI18N
                return false;
            }
            if (ui.host.getText().isEmpty()) {
                displayNotification(NbBundle.getMessage(SetUpRemotePlatform.class, "ERROR_Empty_Host")); // NOI18N
                return false;
            }
            if (ui.username.getText().isEmpty()) {
                displayNotification(NbBundle.getMessage(SetUpRemotePlatform.class, "ERROR_Empty_Username")); // NOI18N
                return false;
            }
            if (ui.radioButtonPassword.isSelected() && ui.password.getPassword().length == 0) {
                displayNotification(NbBundle.getMessage(SetUpRemotePlatform.class, "ERROR_Empty_Password")); // NOI18N
                return false;
            } else if (ui.radioButtonKey.isSelected() && ui.keyFilePath.getText().isEmpty()) {
                displayNotification(NbBundle.getMessage(SetUpRemotePlatform.class, "ERROR_Empty_KeyFile")); // NOI18N
                return false;
            }
            ui.buttonCreate.setEnabled(true);
            if (ui.jreLocation.getText().isEmpty()) {
                displayNotification(NbBundle.getMessage(SetUpRemotePlatform.class, "ERROR_Empty_JRE")); // NOI18N
                return false;
            }
            if (ui.workingDir.getText().isEmpty()) {
                displayNotification(NbBundle.getMessage(SetUpRemotePlatform.class, "MSG_Empty_WorkingDir")); // NOI18N
                return false;
            }

            if (!valid) {
                //Wasn't valid before, now will be
                displayNotification(""); // NOI18N
            }
            return true;
        }

        private void displayNotification(final String message) {
            if (wizardDescriptor != null) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, message); // NOI18N
            }
        }

        @Override
        public void prepareValidation() {
            if (ui != null) {
                final ConnectionMethod cm;
                if (ui.radioButtonPassword.isSelected()) {
                    cm = ConnectionMethod.sshPassword(
                        ui.host.getText(),
                        ((Integer)ui.port.getValue()).intValue(),
                        ui.username.getText(),
                        String.valueOf(ui.password.getPassword()));
                } else {
                    cm = ConnectionMethod.sshKey(
                       ui.host.getText(),
                       ((Integer)ui.port.getValue()).intValue(),
                       ui.username.getText(),
                       new File(ui.keyFilePath.getText()),
                       String.valueOf(ui.passphrase.getPassword()));
                }
                final File buildScript = (File) wizardDescriptor.getProperty(RemotePlatformIt.PROP_BUILDSCRIPT);
                connectionValidator = new ConnectionValidator(
                    ui.jreLocation.getText(),
                    ui.workingDir.getText(),
                    cm,
                    buildScript);
            }
        }

        @Override
        public void validate() throws WizardValidationException {
            final ConnectionValidator cv = connectionValidator;
            if (cv != null) {
                final Properties props = cv.call();
                wizardDescriptor.putProperty(RemotePlatformIt.PROP_SYS_PROPERTIES, props);
            }
        }
    }

    private static class ConnectionValidator implements Callable<Properties> {

        private final String jreLocation;
        private final String workingDir;
        private final ConnectionMethod connectionMethod;
        private final File buildScript;

        ConnectionValidator(
            @NonNull final String jreLocation,
            @NonNull final String workingDir,
            @NonNull final ConnectionMethod connectionMethod,
            @NullAllowed final File buildScript) {
            Parameters.notNull("jreLocation", jreLocation); //NOI18N
            Parameters.notNull("workingDir", workingDir);   //NOI18N
            Parameters.notNull("connectionMethod", connectionMethod);   //NOI18N
            this.jreLocation = jreLocation;
            this.workingDir = workingDir;
            this.connectionMethod = connectionMethod;
            this.buildScript = buildScript;
        }

        @Override
        public Properties call() throws WizardValidationException {
            return RemotePlatformProbe.verifyPlatform(jreLocation, null, workingDir, connectionMethod, buildScript);
        }
    }
}
