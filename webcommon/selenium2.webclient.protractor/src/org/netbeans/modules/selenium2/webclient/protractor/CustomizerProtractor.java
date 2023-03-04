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
package org.netbeans.modules.selenium2.webclient.protractor;

import java.awt.EventQueue;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.netbeans.api.project.Project;
import org.netbeans.modules.selenium2.webclient.protractor.preferences.ProtractorPreferences;
import org.netbeans.modules.selenium2.webclient.protractor.preferences.ProtractorPreferencesValidator;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Theofanis Oikonomou
 */
public class CustomizerProtractor extends javax.swing.JPanel {

    private final Project project;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private boolean autoDiscovered = false;
    
    private volatile String protractor;
    private volatile String userConfigurationFile;

    // @GuardedBy("EDT")
    private ValidationResult validationResult;

    public CustomizerProtractor(Project project) {
        assert EventQueue.isDispatchThread();
        assert project != null;

        this.project = project;

        initComponents();
        init();
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public String getProtractor() {
        return protractor;
    }

    public String getUserConfigurationFile() {
        return userConfigurationFile;
    }

    public String getWarningMessage() {
        assert EventQueue.isDispatchThread();
        for (ValidationResult.Message message : validationResult.getWarnings()) {
            return message.getMessage();
        }
        return null;
    }

    public String getErrorMessage() {
        assert EventQueue.isDispatchThread();
        for (ValidationResult.Message message : validationResult.getErrors()) {
            return message.getMessage();
        }
        return null;
    }

    @NbBundle.Messages({"CustomizerProtractor.protractor.dir.info=Full path of protractor (typically node_modules/protractor/bin/protractor).",
    "CustomizerProtractor.user.configuration.file.info=Full path to configuration file."})
    private void init() {
        assert EventQueue.isDispatchThread();
        // get saved protractor executable if previously set from protractor preferences
        String protractorExec = ProtractorPreferences.getProtractor(project);
        if(protractorExec == null) {
            // not set yet, so search for it in user's PATH
            String userPath = System.getenv("PATH"); // NOI18N
            if(userPath != null) {
                List<String> paths = Arrays.asList(userPath.split(File.pathSeparator));
                final String execName = "protractor"; // NOI18N
                for (String path : paths) {
                    File file = new File(path);
                    if (file.isFile()) {
                        if (path.endsWith(execName)) {
                            if (isProtractorExecValid(path)) { // protractor executable is globally installed and in user's PATH
                                try {
                                    protractorExec = file.getCanonicalPath();
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                                break;
                            }
                        }
                    } else {
                        String[] list = file.list(new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                return name.endsWith(execName);
                            }
                        });
                        if(list != null && list.length == 1) { // protractor executable is globally installed and the containing directory is in user's PATH
                            autoDiscovered = true;
                            if(Utilities.isWindows()) {
                                // if C:\Users\name\AppData\Roaming\npm is in PATH
                                // C:\Users\name\AppData\Roaming\npm\protractor(.cmd) are available
                                // but both actually call C:\Users\name\AppData\Roaming\npm\node_modules\protractor\bin\protractor
                                protractorExec = new File(path, "node_modules/protractor/bin/protractor").getAbsolutePath();
                            } else {
                                protractorExec = path.endsWith(File.separator) ? path + execName : path + File.separator + execName;
                                File f = new File(protractorExec);
                                try {
                                    protractorExec = f.getCanonicalPath();
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        if(protractorExec == null) {
            // not found in user's PATH, so search for it in project's local node_modules dir
            String exec = new File(FileUtil.toFile(project.getProjectDirectory()), "node_modules/protractor/bin/protractor").getAbsolutePath();
            if(isProtractorExecValid(exec)) { // protractor executable is installed in project's local node_modules dir
                protractorExec = exec;
            }
        }
        protractorDirTextField.setText(protractorExec);
        protractorDirInfoLabel.setText(Bundle.CustomizerProtractor_protractor_dir_info());
        
        // get saved user configuration file if previously set from protractor preferences
        String configFile = ProtractorPreferences.getUserConfigurationFile(project);
        userConfigurationFileTextField.setText(configFile);
        userConfigurationFileInfoLabel.setText(Bundle.CustomizerProtractor_user_configuration_file_info());
        // listeners
        addListeners();
        // initial validation
        validateData();
    }
    
    private boolean isProtractorExecValid(String path) {
        ValidationResult result = new ProtractorPreferencesValidator()
                .validateProtractor(path)
                .getResult();
        if (result.isFaultless()) {
            autoDiscovered = true;
            return true;
        }
        return false;
    }

    private void addListeners() {
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        protractorDirTextField.getDocument().addDocumentListener(defaultDocumentListener);
        userConfigurationFileTextField.getDocument().addDocumentListener(defaultDocumentListener);
    }

    @NbBundle.Messages({"CustomizerProtractor.confirm.autodiscovered.info=Protractor executable was auto-discovered. Please confirm by clicking OK."})
    void validateData() {
        assert EventQueue.isDispatchThread();
        protractor = protractorDirTextField.getText();
        userConfigurationFile = userConfigurationFileTextField.getText();
        validationResult = new ProtractorPreferencesValidator()
                .validateProtractor(protractor)
                .validateUserConfigurationFile(project, userConfigurationFile)
                .getResult();
        if (autoDiscovered) { // auto-discovered, show confirmation message to the user
            validationResult.addWarning(new ValidationResult.Message("path", Bundle.CustomizerProtractor_confirm_autodiscovered_info())); // NOI18N
        }
        changeSupport.fireChange();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        protractorDirLabel = new javax.swing.JLabel();
        protractorDirTextField = new javax.swing.JTextField();
        protractorDirBrowseButton = new javax.swing.JButton();
        protractorDirInfoLabel = new javax.swing.JLabel();
        userConfigurationFileLabel = new javax.swing.JLabel();
        userConfigurationFileTextField = new javax.swing.JTextField();
        userConfigurationFileBrowseButton = new javax.swing.JButton();
        userConfigurationFileInfoLabel = new javax.swing.JLabel();

        protractorDirLabel.setLabelFor(protractorDirTextField);
        org.openide.awt.Mnemonics.setLocalizedText(protractorDirLabel, org.openide.util.NbBundle.getMessage(CustomizerProtractor.class, "CustomizerProtractor.protractorDirLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(protractorDirBrowseButton, org.openide.util.NbBundle.getMessage(CustomizerProtractor.class, "CustomizerProtractor.protractorDirBrowseButton.text")); // NOI18N
        protractorDirBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                protractorDirBrowseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(protractorDirInfoLabel, org.openide.util.NbBundle.getMessage(CustomizerProtractor.class, "CustomizerProtractor.protractorDirInfoLabel.text")); // NOI18N

        userConfigurationFileLabel.setLabelFor(protractorDirTextField);
        org.openide.awt.Mnemonics.setLocalizedText(userConfigurationFileLabel, org.openide.util.NbBundle.getMessage(CustomizerProtractor.class, "CustomizerProtractor.userConfigurationFileLabel.text")); // NOI18N

        userConfigurationFileTextField.setColumns(15);

        org.openide.awt.Mnemonics.setLocalizedText(userConfigurationFileBrowseButton, org.openide.util.NbBundle.getMessage(CustomizerProtractor.class, "CustomizerProtractor.userConfigurationFileBrowseButton.text")); // NOI18N
        userConfigurationFileBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userConfigurationFileBrowseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(userConfigurationFileInfoLabel, org.openide.util.NbBundle.getMessage(CustomizerProtractor.class, "CustomizerProtractor.userConfigurationFileInfoLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(userConfigurationFileLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(userConfigurationFileInfoLabel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(userConfigurationFileTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(userConfigurationFileBrowseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(protractorDirLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(protractorDirInfoLabel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(protractorDirTextField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(protractorDirBrowseButton)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(protractorDirLabel)
                    .addComponent(protractorDirTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(protractorDirBrowseButton))
                .addGap(6, 6, 6)
                .addComponent(protractorDirInfoLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userConfigurationFileLabel)
                    .addComponent(userConfigurationFileBrowseButton)
                    .addComponent(userConfigurationFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(userConfigurationFileInfoLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("CustomizerProtractor.chooser.protractor=Select Protractor file")
    private void protractorDirBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_protractorDirBrowseButtonActionPerformed
        assert EventQueue.isDispatchThread();
        File file = new FileChooserBuilder(CustomizerProtractor.class)
        .setTitle(Bundle.CustomizerProtractor_chooser_protractor())
        .setFilesOnly(true)
        .setDefaultWorkingDirectory(FileUtil.toFile(project.getProjectDirectory()))
        .forceUseOfDefaultWorkingDirectory(true)
        .showOpenDialog();
        if (file != null) {
            protractorDirTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_protractorDirBrowseButtonActionPerformed

    @NbBundle.Messages("CustomizerProtractor.chooser.userConfigurationFile=Select Configuration file")
    private void userConfigurationFileBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userConfigurationFileBrowseButtonActionPerformed
        assert EventQueue.isDispatchThread();
        File file = new FileChooserBuilder(CustomizerProtractor.class)
        .setTitle(Bundle.CustomizerProtractor_chooser_userConfigurationFile())
        .setFilesOnly(true)
        .setDefaultWorkingDirectory(FileUtil.toFile(project.getProjectDirectory()))
        .forceUseOfDefaultWorkingDirectory(true)
        .addFileFilter(new FileNameExtensionFilter("JS File", "js"))
        .setAcceptAllFileFilterUsed(false)
        .showOpenDialog();
        if (file != null) {
            userConfigurationFileTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_userConfigurationFileBrowseButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton protractorDirBrowseButton;
    private javax.swing.JLabel protractorDirInfoLabel;
    private javax.swing.JLabel protractorDirLabel;
    private javax.swing.JTextField protractorDirTextField;
    private javax.swing.JButton userConfigurationFileBrowseButton;
    private javax.swing.JLabel userConfigurationFileInfoLabel;
    private javax.swing.JLabel userConfigurationFileLabel;
    private javax.swing.JTextField userConfigurationFileTextField;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class DefaultDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processChange();
        }

        private void processChange() {
            validateData();
        }

    }

}
