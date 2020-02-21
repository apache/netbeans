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

package org.netbeans.modules.cnd.makeproject.ui.actions;

import org.netbeans.modules.cnd.makeproject.api.wizards.ProjectGenerator;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configurations;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.IteratorExtension;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import org.netbeans.modules.cnd.makeproject.api.wizards.DefaultMakeProjectLocationProvider;
import org.netbeans.modules.cnd.makeproject.ui.wizards.PanelProjectLocationVisual;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

public final class RunDialogPanel extends javax.swing.JPanel implements PropertyChangeListener {
    private static final boolean TRACE_REMOTE_CREATION = Boolean.getBoolean("cnd.discovery.trace.projectimport"); // NOI18N
    public static final Logger logger;
    static {
        logger = Logger.getLogger("org.netbeans.modules.cnd.makeproject.api.RunDialogPanel"); // NOI18N
        if (TRACE_REMOTE_CREATION) {
            logger.setLevel(Level.ALL);
        }
    }
    private DocumentListener modifiedValidateDocumentListener = null;
    private Project[] projectChoices = null;
    private final JButton actionButton;
    private final boolean isRun;
    private final FileSystem fileSystem;
    private RunProjectAction projectAction;
    private static final RequestProcessor RP = new RequestProcessor("RunDialogPanel Worker", 1); //NOI18N
    
    private String lastSelectedExecutable = null;
    private Project lastSelectedProject = null;
    
    private boolean isValidating = false;

    private void initAccessibility() {
        // Accessibility
        getAccessibleContext().setAccessibleDescription(getString("RUN_DIALOG_PANEL_AD"));
        executableTextField.getAccessibleContext().setAccessibleDescription(getString("EXECUTABLE_AD"));
        executableBrowseButton.getAccessibleContext().setAccessibleDescription(getString("BROWSE_BUTTON_AD"));
        projectComboBox.getAccessibleContext().setAccessibleDescription(getString("ASSOCIATED_PROJECT_AD"));
        runDirectoryTextField.getAccessibleContext().setAccessibleDescription(getString("RUN_DIRECTORY_LABEL_AD"));
        runDirectoryBrowseButton.getAccessibleContext().setAccessibleDescription(getString("RUN_DIRECTORY_BUTTON_AD"));
        argumentTextField.getAccessibleContext().setAccessibleDescription(getString("ARGUMENTS_LABEL_AD"));
        environmentTextField.getAccessibleContext().setAccessibleDescription(getString("ENVIRONMENT_LABEL_AD"));
    }
    
    public RunDialogPanel(FileObject executableFO, JButton actionButton, boolean isRun) throws FileStateInvalidException {
        this.actionButton = actionButton;
        fileSystem = executableFO.getFileSystem();
        this.isRun = isRun;
        initialize(executableFO);
        initAccessibility();
    }
    
    private void initialize(FileObject executableFO) {
        initComponents();
        errorLabel.setForeground(javax.swing.UIManager.getColor("nb.errorForeground")); // NOI18N
        errorLabel.setText(""); //NOI18N
        modifiedValidateDocumentListener = new ModifiedValidateDocumentListener();
        //modifiedRunDirectoryListener = new ModifiedRunDirectoryListener();
        if (executableFO != null) {
            executableTextField.setText(executableFO.getPath());
        }
        if (isRun) {
            guidanceTextarea.setText(getString("DIALOG_GUIDANCETEXT"));
        } else {
            guidanceTextarea.setText(getString("DIALOG_GUIDANCETEXT_CREATE"));
        }
        
        executableTextField.getDocument().addDocumentListener(modifiedValidateDocumentListener);
        runDirectoryTextField.getDocument().addDocumentListener(modifiedValidateDocumentListener);
        projectNameField.getDocument().addDocumentListener(modifiedValidateDocumentListener);
        projectLocationField.getDocument().addDocumentListener(modifiedValidateDocumentListener);
        initGui();
        
        guidanceTextarea.setBackground(getBackground());
        setPreferredSize(new java.awt.Dimension(700, (int)getPreferredSize().getHeight()));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        guidanceTextarea = new javax.swing.JTextArea();
        executableLabel1 = new javax.swing.JLabel();
        executableBrowseButton = new javax.swing.JButton();
        executableTextField = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        projectComboBox = new javax.swing.JComboBox();
        errorLabel = new javax.swing.JLabel();
        runDirectoryLabel = new javax.swing.JLabel();
        runDirectoryTextField = new javax.swing.JTextField();
        runDirectoryBrowseButton = new javax.swing.JButton();
        argumentLabel = new javax.swing.JLabel();
        argumentTextField = new javax.swing.JTextField();
        environmentLabel = new javax.swing.JLabel();
        environmentTextField = new javax.swing.JTextField();
        projectkindLabel = new javax.swing.JLabel();
        projectKind = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        projectNameLabel = new javax.swing.JLabel();
        projectLocationLabel = new javax.swing.JLabel();
        projectFolderLabel = new javax.swing.JLabel();
        projectNameField = new javax.swing.JTextField();
        projectLocationField = new javax.swing.JTextField();
        projectFolderField = new javax.swing.JTextField();
        projectLocationButton = new javax.swing.JButton();
        configurationLabel = new javax.swing.JLabel();
        configurationCombobox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        guidanceTextarea.setEditable(false);
        guidanceTextarea.setLineWrap(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/actions/Bundle"); // NOI18N
        guidanceTextarea.setText(bundle.getString("DIALOG_GUIDANCETEXT")); // NOI18N
        guidanceTextarea.setWrapStyleWord(true);
        guidanceTextarea.setMinimumSize(new java.awt.Dimension(0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        add(guidanceTextarea, gridBagConstraints);

        executableLabel1.setLabelFor(executableTextField);
        org.openide.awt.Mnemonics.setLocalizedText(executableLabel1, bundle.getString("EXECUTABLE_LBL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(executableLabel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(executableBrowseButton, bundle.getString("BROWSE_BUTTON_TXT")); // NOI18N
        executableBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executableBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 12);
        add(executableBrowseButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(executableTextField, gridBagConstraints);

        projectLabel.setLabelFor(projectComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, bundle.getString("ASSOCIATED_PROJECT_LBL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 0);
        add(projectLabel, gridBagConstraints);

        projectComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 6, 12);
        add(projectComboBox, gridBagConstraints);

        errorLabel.setText(bundle.getString("ERROR_NOTAEXEFILE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 12, 0, 12);
        add(errorLabel, gridBagConstraints);

        runDirectoryLabel.setLabelFor(runDirectoryTextField);
        org.openide.awt.Mnemonics.setLocalizedText(runDirectoryLabel, bundle.getString("RUN_DIRECTORY_LABEL_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(runDirectoryLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(runDirectoryTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(runDirectoryBrowseButton, bundle.getString("RUN_DIRECTORY_BUTTON_TXT")); // NOI18N
        runDirectoryBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runDirectoryBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 12);
        add(runDirectoryBrowseButton, gridBagConstraints);

        argumentLabel.setLabelFor(argumentTextField);
        org.openide.awt.Mnemonics.setLocalizedText(argumentLabel, bundle.getString("ARGUMENTS_LABEL_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(argumentLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 12);
        add(argumentTextField, gridBagConstraints);

        environmentLabel.setLabelFor(environmentTextField);
        org.openide.awt.Mnemonics.setLocalizedText(environmentLabel, bundle.getString("ENVIRONMENT_LABEL_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(environmentLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 12);
        add(environmentTextField, gridBagConstraints);

        projectkindLabel.setLabelFor(projectKind);
        org.openide.awt.Mnemonics.setLocalizedText(projectkindLabel, org.openide.util.NbBundle.getMessage(RunDialogPanel.class, "ProjectKindName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(projectkindLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 12);
        add(projectKind, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 12);
        add(jSeparator1, gridBagConstraints);

        projectNameLabel.setLabelFor(projectNameField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(RunDialogPanel.class, "RunDialogPanel.project.name.label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(projectNameLabel, gridBagConstraints);

        projectLocationLabel.setLabelFor(projectLocationField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(RunDialogPanel.class, "RunDialogPanel.project.location.label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(projectLocationLabel, gridBagConstraints);

        projectFolderLabel.setLabelFor(projectFolderField);
        projectFolderLabel.setText(org.openide.util.NbBundle.getMessage(RunDialogPanel.class, "RunDialogPanel.project.folder.label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(projectFolderLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 12);
        add(projectNameField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(projectLocationField, gridBagConstraints);

        projectFolderField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 12);
        add(projectFolderField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(projectLocationButton, org.openide.util.NbBundle.getMessage(RunDialogPanel.class, "RunDialogPanel.project.location.button")); // NOI18N
        projectLocationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectLocationButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 12);
        add(projectLocationButton, gridBagConstraints);

        configurationLabel.setLabelFor(configurationCombobox);
        org.openide.awt.Mnemonics.setLocalizedText(configurationLabel, org.openide.util.NbBundle.getMessage(RunDialogPanel.class, "Configuration.Text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(configurationLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 12);
        add(configurationCombobox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void runDirectoryBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runDirectoryBrowseButtonActionPerformed
        String seed;
        if (runDirectoryTextField.getText().length() > 0) {
            seed = runDirectoryTextField.getText();
        }
        else {
            seed = getExecutablePath();
        }
        // Show the file chooser
        JFileChooser fileChooser = RemoteFileChooserUtil.createFileChooser(fileSystem,
                getString("SelectWorkingDir"),
                getString("SelectLabel"),
                JFileChooser.DIRECTORIES_ONLY,
                null,
                seed,
                true
                );
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        runDirectoryTextField.setText(fileChooser.getSelectedFile().getPath());
    }//GEN-LAST:event_runDirectoryBrowseButtonActionPerformed
    
    private void projectComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectComboBoxActionPerformed
        int selectedIndex = projectComboBox.getSelectedIndex();
        clearError();
        validateExecutable();
        if (selectedIndex == 0) {
            validateProjectLocation();
            FileObject executable =  fileSystem.findResource(getExecutablePath());
            if (executable != null && executable.isValid() && executable.getParent() != null) {
                runDirectoryTextField.setText(executable.getParent().getPath());
            } else {
                if (!isValidating) {
                    executableTextField.setText(""); // NOI18N
                }
            }
            argumentTextField.setText(""); // NOI18N
            environmentTextField.setText(""); // NOI18N
            configurationCombobox.setEnabled(false);
            projectKind.setEnabled(true);
            projectNameField.setEnabled(true);
            projectLocationField.setEnabled(true);
            projectLocationButton.setEnabled(true);
            projectFolderField.setEnabled(true);
            projectLocationField.setText(DefaultMakeProjectLocationProvider.getDefault().getDefaultProjectFolder(FileSystemProvider.getExecutionEnvironment(fileSystem)));
            if (executable != null && executable.isValid()) {
                projectNameField.setText(ProjectGenerator.getDefault().getValidProjectName(projectLocationField.getText(), executable.getNameExt()));
            } else {
                projectNameField.setText(ProjectGenerator.getDefault().getValidProjectName(projectLocationField.getText(), "")); //NOI18N
            }
        } else {
            configurationCombobox.setEnabled(true);
            projectKind.setEnabled(false);
            projectNameField.setEnabled(false);
            projectLocationField.setEnabled(false);
            projectLocationButton.setEnabled(false);
            projectFolderField.setEnabled(false);
            Project project = projectChoices[projectComboBox.getSelectedIndex()-1];
            ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            if (pdp == null) {
                return;
            }
            MakeConfigurationDescriptor projectDescriptor = pdp.getConfigurationDescriptor();
            MakeConfiguration conf = projectDescriptor.getActiveConfiguration();
            RunProfile runProfile = conf.getProfile();
            runDirectoryTextField.setText(runProfile.getRunDirectory());
            argumentTextField.setText(runProfile.getArgsFlat());
            environmentTextField.setText(runProfile.getEnvironment().toString());
        }
    }//GEN-LAST:event_projectComboBoxActionPerformed
    
    private void executableBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_executableBrowseButtonActionPerformed
        ExecutionEnvironment executionEnvironment = FileSystemProvider.getExecutionEnvironment(fileSystem);
        String seed = getExecutablePath();
        final String chooser_key = "makeproject.run.executable"; //NOI18N
        if (seed.length() == 0 && RemoteFileChooserUtil.getCurrentChooserFile(chooser_key, executionEnvironment) != null) {
            String s = RemoteFileChooserUtil.getCurrentChooserFile(chooser_key, executionEnvironment);
            if (s != null) {
                seed = s;
            }
        }
        if (seed.length() == 0) {
            seed = System.getProperty("user.home"); // NOI18N
        }
        
        FileFilter[] filter = null;
        OSFamily oSFamily = null;
        try {
            oSFamily = HostInfoUtils.getHostInfo(executionEnvironment).getOSFamily();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (CancellationException ex) {
            // don't report cancellation exception
        }
        if (oSFamily != null) {
            switch (oSFamily) {
                case MACOSX:
                    filter = new FileFilter[] {FileFilterFactory.getMacOSXExecutableFileFilter()};
                    break;
                case WINDOWS:
                    filter = new FileFilter[] {FileFilterFactory.getPeExecutableFileFilter()};
                    break;
                case SUNOS:
                case LINUX:
                case FREEBSD:
                case UNKNOWN:
                    filter = new FileFilter[] {FileFilterFactory.getElfExecutableFileFilter()};
                    break;
            }
        }
        if (filter == null) {
            if (Utilities.isWindows()){
                filter = new FileFilter[] {FileFilterFactory.getPeExecutableFileFilter()};
            } else if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                filter = new FileFilter[] {FileFilterFactory.getMacOSXExecutableFileFilter()};
            } else {
                filter = new FileFilter[] {FileFilterFactory.getElfExecutableFileFilter()};
            }
        }
        // Show the file chooser
        JFileChooser fileChooser = RemoteFileChooserUtil.createFileChooser(fileSystem,
                getString("SelectExecutable"),
                getString("SelectLabel"),
                JFileChooser.FILES_ONLY,
                filter,
                seed,
                false
                );
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        final File selectedFile = fileChooser.getSelectedFile();
        RemoteFileChooserUtil.setCurrentChooserFile(chooser_key, selectedFile.getParentFile().getPath(), executionEnvironment);
        executableTextField.setText(selectedFile.getPath());
    }//GEN-LAST:event_executableBrowseButtonActionPerformed

    private void projectLocationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectLocationButtonActionPerformed
        String path = this.projectLocationField.getText();
        JFileChooser chooser = RemoteFileChooserUtil.createFileChooser(fileSystem,
                getString("RunDialogPanel.Title_SelectProjectLocation"),
                null, JFileChooser.DIRECTORIES_ONLY, null, path, true);
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            projectLocationField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_projectLocationButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel argumentLabel;
    private javax.swing.JTextField argumentTextField;
    private javax.swing.JComboBox configurationCombobox;
    private javax.swing.JLabel configurationLabel;
    private javax.swing.JLabel environmentLabel;
    private javax.swing.JTextField environmentTextField;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JButton executableBrowseButton;
    private javax.swing.JLabel executableLabel1;
    private javax.swing.JTextField executableTextField;
    private javax.swing.JTextArea guidanceTextarea;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JComboBox projectComboBox;
    private javax.swing.JTextField projectFolderField;
    private javax.swing.JLabel projectFolderLabel;
    private javax.swing.JComboBox projectKind;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JButton projectLocationButton;
    private javax.swing.JTextField projectLocationField;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectNameField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JLabel projectkindLabel;
    private javax.swing.JButton runDirectoryBrowseButton;
    private javax.swing.JLabel runDirectoryLabel;
    private javax.swing.JTextField runDirectoryTextField;
    // End of variables declaration//GEN-END:variables
    
    private Project[] getOpenedProjects() {
        List<Project> res = new ArrayList<>();
        for(Project p :OpenProjects.getDefault().getOpenProjects()) {
            ConfigurationDescriptorProvider conf = p.getLookup().lookup(ConfigurationDescriptorProvider.class);
            if (conf != null && conf.gotDescriptor()) {
                if (fileSystem.equals(conf.getConfigurationDescriptor().getBaseDirFileSystem())) {
                    res.add(p);
                }
            }
        }
        return res.toArray(new Project[res.size()]);
    }

    private void initGui() {
        ActionListener projectComboBoxActionListener = projectComboBox.getActionListeners()[0];
        projectComboBox.removeActionListener(projectComboBoxActionListener);
        projectComboBox.removeAllItems();
        projectComboBox.addItem(getString("NO_PROJECT")); // always first
        int index = 0;
        //if (isRun) {
            projectChoices = getOpenedProjects();
            for (int i = 0; i < projectChoices.length; i++) {
                projectComboBox.addItem(ProjectUtils.getInformation(projectChoices[i]).getName());
            }

            // preselect project ???
            if (lastSelectedExecutable != null && getExecutablePath().equals(lastSelectedExecutable) && lastSelectedProject != null) {
                for (int i = 0; i < projectChoices.length; i++) {
                    if (projectChoices[i] == lastSelectedProject) {
                        index = i+1;
                        break;
                    }
                }
            }
        //}
        if (!isRun) {
            index = 0;
        }
        boolean allowSelectProject = projectComboBox.getModel().getSize() > 1;
        projectComboBox.setVisible(allowSelectProject);
        projectLabel.setVisible(allowSelectProject);
        configurationLabel.setVisible(allowSelectProject);
        configurationCombobox.setVisible(allowSelectProject);
        projectComboBox.setSelectedIndex(index);
        projectComboBox.addActionListener(projectComboBoxActionListener);
        projectComboBoxActionPerformed(null);
        //validateRunDirectory();
        projectKind.removeAllItems();
        ExecutionEnvironment executionEnvironment = FileSystemProvider.getExecutionEnvironment(fileSystem);
        if (executionEnvironment.isLocal()) {
            projectKind.addItem(new ProjectKindItem(IteratorExtension.ProjectKind.Minimal));
            projectKind.addItem(new ProjectKindItem(IteratorExtension.ProjectKind.IncludeDependencies));
            projectKind.addItem(new ProjectKindItem(IteratorExtension.ProjectKind.CreateDependencies));
            projectKind.setSelectedIndex(1);
        } else {
            projectKind.addItem(new ProjectKindItem(IteratorExtension.ProjectKind.IncludeDependencies));
            projectKind.setSelectedIndex(0);
        }
        configurationCombobox.removeAllItems();
        configurationCombobox.addItem(new ConfigurationType(ConfigurationType.USE_CURRENT_CONFIGURATION));
        configurationCombobox.addItem(new ConfigurationType(ConfigurationType.UPDATE_CURRENT_CONFIGURATION));
        configurationCombobox.addItem(new ConfigurationType(ConfigurationType.CREATE_NEW_CONFIGURATION));
        configurationCombobox.setSelectedIndex(0);
    }
    
    private boolean validateExecutable() {
        String exePath = getExecutablePath();
        FileObject exeFile = fileSystem.findResource(exePath);
        if (exeFile == null || !exeFile.isValid()) {
            setError("ERROR_DONTEXIST", true); // NOI18N
            return false;
        }
        if (exeFile.isFolder()) {
            setError("ERROR_NOTAEXEFILE", true); // NOI18N
            return false;
        }
        return true;
    }
    
    private FileObject getExistingParent(String path) {
        path = PathUtilities.getDirName(path);
        FileObject fo = fileSystem.findResource(path);
        while (fo == null) {
            path = PathUtilities.getDirName(path);
            if (path == null || path.length() == 0) {
                return null;
            } else {
                fo = fileSystem.findResource(path);
            }
        }
        return fo;
    }
    
    private boolean validateProjectLocation() {
        if (!PanelProjectLocationVisual.isValidProjectName(projectNameField.getText())) {
            setError("RunDialogPanel.MSG_IllegalProjectName", false); // NOI18N
            return false;
        }
        if (!CndPathUtilities.isPathAbsolute(projectLocationField.getText())) {
            setError("RunDialogPanel.MSG_IllegalProjectLocation", false); // NOI18N
            return false;
        }
        // never use canonical as a file name validity check - it "eats" constructs like new File("*"), new File("<"), etc
        FileObject projectDirFO = fileSystem.findResource(projectFolderField.getText()); // can be null
        if (projectDirFO != null && projectDirFO.isValid()) {
            if (projectDirFO.isData()) {
                setError("RunDialogPanel.MSG_NotAFolder", false); // NOI18N
                return false;
            }
            FileObject nbProjFO = projectDirFO.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
            if (nbProjFO != null && nbProjFO.isValid()) {
                setError("RunDialogPanel.MSG_ProjectfolderNotEmpty", false, MakeConfiguration.NBPROJECT_FOLDER); // NOI18N
                return false;
            }
        } else {
            FileObject existingParent = getExistingParent(projectFolderField.getText());
            if (existingParent == null) {
                setError("RunDialogPanel.MSG_IllegalProjectLocation", false); // NOI18N
                return false;
            }
            if (!existingParent.canWrite()) {
                setError("RunDialogPanel.MSG_ProjectFolderReadOnly", false); // NOI18N
                return false;
            }
        }
        return true;
    }
    
    private void setError(String errorMsg, boolean disable, String ... args) {
        setErrorMsg(getString(errorMsg, args));
        if (disable) {
            runDirectoryBrowseButton.setEnabled(false);
            runDirectoryLabel.setEnabled(false);
            runDirectoryTextField.setEnabled(false);
            argumentLabel.setEnabled(false);
            argumentTextField.setEnabled(false);
            environmentLabel.setEnabled(false);
            environmentTextField.setEnabled(false);
            projectComboBox.setEnabled(false);
            projectKind.setEnabled(false);
            projectNameField.setEnabled(false);
            projectLocationField.setEnabled(false);
            projectLocationButton.setEnabled(false);
            projectFolderField.setEnabled(false);
        }
        actionButton.setEnabled(false);
    }
    
    private void clearError() {
        setErrorMsg(" "); // NOI18N
        
        runDirectoryBrowseButton.setEnabled(true);
        runDirectoryLabel.setEnabled(true);
        runDirectoryTextField.setEnabled(true);
        argumentLabel.setEnabled(true);
        argumentTextField.setEnabled(true);
        environmentLabel.setEnabled(true);
        environmentTextField.setEnabled(true);
        projectComboBox.setEnabled(true);
        if (projectComboBox.getSelectedIndex() == 0) {
            projectKind.setEnabled(true);
            projectNameField.setEnabled(true);
            projectLocationField.setEnabled(true);
            projectLocationButton.setEnabled(true);
            projectFolderField.setEnabled(true);
        } else {
            projectKind.setEnabled(false);
            projectNameField.setEnabled(false);
            projectLocationField.setEnabled(false);
            projectLocationButton.setEnabled(false);
            projectFolderField.setEnabled(false);
        }
        
        actionButton.setEnabled(true);
    }
    
    private void validateFields(javax.swing.event.DocumentEvent documentEvent) {
        isValidating = true;
        try {
            if (documentEvent.getDocument() == executableTextField.getDocument()) {
                clearError();
                projectComboBox.setSelectedIndex(0);
                if (!validateExecutable()) {
                    return;
                }
                FileObject executable =  fileSystem.findResource(getExecutablePath());
                if (executable != null && executable.isValid() && executable.getParent() != null) {
                    runDirectoryTextField.setText(executable.getParent().getPath());
                }
            } else if (documentEvent.getDocument() == projectNameField.getDocument() ||
                       documentEvent.getDocument() == projectLocationField.getDocument()) {
                clearError();
                if (projectComboBox.getSelectedIndex() == 0) {
                    String projectName = projectNameField.getText().trim();
                    String projectFolder = projectLocationField.getText().trim();
                    while (projectFolder.endsWith("/") || projectFolder.endsWith("\\")) { // NOI18N
                        projectFolder = projectFolder.substring(0, projectFolder.length() - 1);
                    }

                    projectFolderField.setText(projectFolder + CndFileUtils.getFileSeparatorChar(fileSystem) + projectName);
                    if (!validateProjectLocation()) {
                        return;
                    }
                }
            }
        } finally {
            isValidating = false;
        }
    }

    // ModifiedDocumentListener
    private final class ModifiedValidateDocumentListener implements DocumentListener {
        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent documentEvent) {
            validateFields(documentEvent);
        }
        
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent documentEvent) {
            validateFields(documentEvent);
        }
        
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent documentEvent) {
            validateFields(documentEvent);
        }
    }
    
    public void getSelectedProject(final RunProjectAction action) {
        lastSelectedExecutable = getExecutablePath();
        if (projectComboBox.getSelectedIndex() > 0) {
            lastSelectedProject = projectChoices[projectComboBox.getSelectedIndex()-1];
            Project project = lastSelectedProject;
            ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            MakeConfigurationDescriptor projectDescriptor = pdp.getConfigurationDescriptor();
            ConfigurationType confType = (ConfigurationType) configurationCombobox.getSelectedItem();
            switch(confType.getType()) {
                case ConfigurationType.CREATE_NEW_CONFIGURATION:
                {
                    MakeConfiguration active = projectDescriptor.getActiveConfiguration();
                    ExecutionEnvironment executionEnvironment = FileSystemProvider.getExecutionEnvironment(fileSystem);
                    String baseDir = active.getBaseDir();
                    MakeConfiguration conf = MakeConfiguration.createMakefileConfiguration(active.getBaseFSPath(), 
                            getConfigurationName(projectDescriptor),
                        executionEnvironment.getHost());
                    // Working dir
                    String wd = fileSystem.findResource(getExecutablePath()).getParent().getPath();
                    wd = CndPathUtilities.toRelativePath(baseDir, wd);
                    wd = CndPathUtilities.normalizeSlashes(wd);
                    conf.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(wd);
                    // Executable
                    String exe =  getExecutablePath();
                    exe = CndPathUtilities.toRelativePath(baseDir, exe);
                    exe = CndPathUtilities.normalizeSlashes(exe);
                    conf.getMakefileConfiguration().getOutput().setValue(exe);
                    updateRunProfile(baseDir, conf.getProfile());
                    conf.getProfile().setBuildFirst(false);
                    List<Configuration> list = new ArrayList<>(projectDescriptor.getConfs().getConfigurations());
                    list.add(conf);
                    conf.setDefault(false);
                    Configurations c = new Configurations();
                    c.init(list.toArray(new Configuration[list.size()]), 0);
                    projectDescriptor.setConfs(c);
                    projectDescriptor.getConfs().setActive(conf);
                    fillConfiguration();
                    break;
                }
                case ConfigurationType.UPDATE_CURRENT_CONFIGURATION:
                {
                    MakeConfiguration conf = projectDescriptor.getActiveConfiguration();
                    updateRunProfile(conf.getBaseDir(), conf.getProfile());
                    fillConfiguration();
                    break;
                }
                case ConfigurationType.USE_CURRENT_CONFIGURATION:
                {
                    MakeConfiguration conf = projectDescriptor.getActiveConfiguration();
                    updateRunProfile(conf.getBaseDir(), conf.getProfile());
                    if (action != null) {
                        RP.post(() -> {
                            action.run(lastSelectedProject);
                        });
                    }
                    break;
                }
            }
        } else {
            RP.post(() -> {
                createNewProject(action);
            });
        }
    }

    private String getConfigurationName(MakeConfigurationDescriptor projectDescriptor) {
        String exe =  getExecutablePath();
        String[] split = exe.split("\\/"); // NOI18N
        String name = split[split.length - 1];
        Configuration[] clonedConfs = projectDescriptor.getConfs().toArray();
        String aName = name;
        for(int i = 1; ;i++) {
            boolean found = false;
            for(Configuration c : clonedConfs) {
                if (aName.equals(c.getName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return aName;
            }
            aName = name+"_"+i; // NOI18N
        }
    }
    
    private void createNewProject(final RunProjectAction action) {
        try {
            ProgressHandle progress = ProgressHandleFactory.createHandle(getString("CREATING_PROJECT_PROGRESS")); // NOI18N
            progress.start();
            try {
                ExecutionEnvironment executionEnvironment = FileSystemProvider.getExecutionEnvironment(fileSystem);
                FileUtil.createFolder(fileSystem.getRoot(), projectFolderField.getText().trim());
                projectAction = action;
                if (executionEnvironment.isLocal()) {
                    createLocalProject(true);
                } else {
                    FileObject projectCreator = findProjectCreator();
                    if (projectCreator == null) {
                         createLocalProject(false);
                    } else {
                        Project project = createRemoteProject(projectCreator);
                        if (project == null) {
                            createLocalProject(false);
                        }
                    }
                }
            } finally {
                progress.finish();
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private Project createLocalProject(boolean isLocal) throws IOException, IllegalArgumentException {
        ExecutionEnvironment executionEnvironment = FileSystemProvider.getExecutionEnvironment(fileSystem);
        String projectName = projectNameField.getText().trim();
        String baseDir = projectFolderField.getText().trim();
        String hostID;
        if (isLocal) {
            hostID = ExecutionEnvironmentFactory.toUniqueID(executionEnvironment);
        } else {
            hostID = ExecutionEnvironmentFactory.toUniqueID(ExecutionEnvironmentFactory.getLocal());
        }
        MakeConfiguration conf = MakeConfiguration.createMakefileConfiguration(new FSPath(fileSystem, baseDir), "Default", hostID); // NOI18N
        // Working dir
        String wd = fileSystem.findResource(getExecutablePath()).getParent().getPath();
        wd = CndPathUtilities.toRelativePath(baseDir, wd);
        wd = CndPathUtilities.normalizeSlashes(wd);
        conf.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(wd);
        // Executable
        String exe = getExecutablePath();
        exe = CndPathUtilities.toRelativePath(baseDir, exe);
        exe = CndPathUtilities.normalizeSlashes(exe);
        conf.getMakefileConfiguration().getOutput().setValue(exe);
        updateRunProfile(baseDir, conf.getProfile());
        ProjectGenerator.ProjectParameters prjParams = new ProjectGenerator.ProjectParameters(projectName, new FSPath(fileSystem, baseDir));
        prjParams.setOpenFlag(false)
                 .setConfiguration(conf)
                 .setHostUID(hostID)
                 .setImportantFiles(Collections.<String>singletonList(exe).iterator())
                 .setMakefileName(""); //NOI18N
        Project project = ProjectGenerator.getDefault().createBlankProject(prjParams);
        lastSelectedProject = project;
        IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
        if (extension != null) {
            extension.disableModel(project);
        }
        OpenProjects.getDefault().addPropertyChangeListener(this);
        OpenProjects.getDefault().open(new Project[]{project}, false);
        return project;
    }

    private Project createRemoteProject(FileObject projectCreator) {
        ExecutionEnvironment executionEnvironment = FileSystemProvider.getExecutionEnvironment(fileSystem);
        if (TRACE_REMOTE_CREATION) {
            logger.log(Level.INFO, "#{0} --netbeans-project={1} --project-create binary={2} --sources=used", // NOI18N
                    new Object[]{projectCreator.getPath(), projectFolderField.getText().trim(), getExecutablePath()});
        }
        ExitStatus execute = ProcessUtils.execute(executionEnvironment, projectCreator.getPath()
                                     , "--netbeans-project="+projectFolderField.getText().trim() // NOI18N
                                     , "--project-create", "binary="+getExecutablePath() // NOI18N
                                     , "--sources=used" // NOI18N
                                     );
        if (TRACE_REMOTE_CREATION) {
            logger.log(Level.INFO, "#exitCode={0}", execute.exitCode); // NOI18N
            logger.log(Level.INFO, execute.getErrorString());
            logger.log(Level.INFO, execute.getOutputString());
        }
        if (!execute.isOK()) {
            // probably java does not found an
            // try to find java in environment variables
            String java = null; 
            try {
                java = HostInfoUtils.getHostInfo(executionEnvironment).getEnvironment().get("JDK_HOME"); // NOI18N
                if (java == null || java.isEmpty()) {
                    java = HostInfoUtils.getHostInfo(executionEnvironment).getEnvironment().get("JAVA_HOME"); // NOI18N
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (CancellationException ex) {
                // don't report cancellation exception
            }
            if (java != null) {
                execute = ProcessUtils.execute(executionEnvironment, projectCreator.getPath()
                                     , "--jdkhome", java // NOI18N
                                     , "--netbeans-project="+projectFolderField.getText().trim() // NOI18N
                                     , "--project-create", "binary="+getExecutablePath() // NOI18N
                                     , "--sources=used" // NOI18N
                                     );
                if (TRACE_REMOTE_CREATION) {
                    logger.log(Level.INFO, "#exitCode={0}", execute.exitCode); // NOI18N
                    logger.log(Level.INFO, execute.getErrorString());
                    logger.log(Level.INFO, execute.getOutputString());
                }
            }
        }
        String baseDir = projectFolderField.getText().trim();
        FileObject toRefresh = fileSystem.findResource(PathUtilities.getDirName(baseDir));
        if (toRefresh != null) {
            toRefresh.refresh();
        }
        FileObject projectFO = fileSystem.findResource(baseDir);
        if (projectFO == null) {
            return null;
        }
        Project project = null;
        try {
            project = ProjectManager.getDefault().findProject(projectFO);
            if (project == null) {
                return null;
            }
            lastSelectedProject = project;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
        OpenProjects.getDefault().addPropertyChangeListener(this);
        OpenProjects.getDefault().open(new Project[]{project}, false);
        return project;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS)) {
            if (evt.getNewValue() instanceof Project[]) {
                Project[] projects = (Project[])evt.getNewValue();
                if (projects.length == 0) {
                    return;
                }
                OpenProjects.getDefault().removePropertyChangeListener(this);
                if (lastSelectedProject == null) {
                    return;
                }
                fillConfiguration();
            }
        }
    }
    
    private void fillConfiguration() {
        if (projectAction != null) {
            RP.post(() -> {
                projectAction.run(lastSelectedProject);
                projectAction = null;
            });
        }
        ExecutionEnvironment executionEnvironment = FileSystemProvider.getExecutionEnvironment(fileSystem);
        if (executionEnvironment.isLocal()) {
            IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
            if (extension != null) {
                Map<String, Object> map = new HashMap<>();
                WizardConstants.DISCOVERY_BUILD_RESULT.toMap(map, getExecutablePath());
                WizardConstants.DISCOVERY_RESOLVE_LINKS.toMap(map, MakeProjectOptions.getResolveSymbolicLinks());
                WizardConstants.DISCOVERY_ROOT_FOLDER.toMap(map, lastSelectedProject.getProjectDirectory().getPath());
                IteratorExtension.ProjectKind kind = ((ProjectKindItem)projectKind.getSelectedItem()).kind;
                extension.discoverProject(map, lastSelectedProject, kind); // NOI18N
            }
        } else {
            IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
            if (extension != null) {
                extension.discoverHeadersByModel(lastSelectedProject);
            }
        }
    }

    private FileObject findProjectCreator() {
        ExecutionEnvironment executionEnvironment = FileSystemProvider.getExecutionEnvironment(fileSystem);
        for(CompilerSet set : CompilerSetManager.get(executionEnvironment).getCompilerSets()) {
            if (set.getCompilerFlavor().isSunStudioCompiler()) {
                String directory = set.getDirectory();
                FileObject projectCreator = fileSystem.findResource(directory+"/../lib/ide_project/bin/ide_project");
                if (projectCreator != null && projectCreator.isValid()) {
                    return projectCreator;
                }
            }
        }
        return null;
    }
            
    private void updateRunProfile(String baseDir, RunProfile runProfile) {
        // Arguments
        runProfile.setArgs(argumentTextField.getText());
        // Working dir
        String wd = runDirectoryTextField.getText();
        wd = CndPathUtilities.toRelativePath(baseDir, wd);
        wd = CndPathUtilities.normalizeSlashes(wd);
        runProfile.setRunDirectory(wd);
        // Environment
        Env env = runProfile.getEnvironment();
	env.removeAll();
        env.decode(environmentTextField.getText());
    }
    
    public String getExecutablePath() {
        return executableTextField.getText();
    }
    
    public String getArguments() {
        return argumentTextField.getText();
    }
    
    private void setErrorMsg(String msg) {
        errorLabel.setText(msg);
    }
    
    /** Look up i18n strings here */
    private String getString(String s, String ... args) {
        return NbBundle.getMessage(RunDialogPanel.class, s, args);
    }
    
    private final class ProjectKindItem {
        private final IteratorExtension.ProjectKind kind;
        ProjectKindItem(IteratorExtension.ProjectKind kind) {
            this.kind = kind;
        }

        @Override
        public String toString() {
            return RunDialogPanel.this.getString("ProjectItemKind_"+kind);
        }
    }
    
    public static interface RunProjectAction {
        void run(Project project);
    }
    
    private final class ConfigurationType {
        public static final int USE_CURRENT_CONFIGURATION = 0;
        public static final int UPDATE_CURRENT_CONFIGURATION = 1;
        public static final int CREATE_NEW_CONFIGURATION = 2;
        
        private final int type;
        ConfigurationType(int type){
            this.type = type;
        }

        public int getType() {
            return type;
        }
        
        @Override
        public String toString() {
            switch(type) {
                case USE_CURRENT_CONFIGURATION:
                    return RunDialogPanel.this.getString("USE_CURRENT_CONFIGURATION");
                case UPDATE_CURRENT_CONFIGURATION:
                    return RunDialogPanel.this.getString("UPDATE_CURRENT_CONFIGURATION");
                case CREATE_NEW_CONFIGURATION:
                    return RunDialogPanel.this.getString("CREATE_NEW_CONFIGURATION");
            }
            throw new IllegalStateException();
        }
    }
}
