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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Document;
import javax.swing.text.Element;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.wizards.PreBuildSupport;
import org.netbeans.modules.cnd.makeproject.api.wizards.PreBuildSupport.PreBuildArtifact;
import org.netbeans.modules.cnd.makeproject.api.wizards.PreBuildSupport.PreBuildArtifactProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class PreBuildActionPanel extends javax.swing.JPanel implements HelpCtx.Provider {

    private static final int VERIFY_DELAY = 300;
    private final DocumentListener documentListener;
    private final AtomicBoolean listenersDisabled = new AtomicBoolean(true);
    private final PreBuildActionDescriptorPanel controller;
    private final RefreshRunnable refreshRunnable;
    private static final RequestProcessor RP = new RequestProcessor("MakefileOrConfigure Validator", 1); // NOI18N
    private final RequestProcessor.Task refreshSourceFolderTask;
    private WizardDescriptor settings;

    PreBuildActionPanel(PreBuildActionDescriptorPanel buildActionsDescriptorPanel) {
        initComponents();
        instructionsTextArea.setBackground(instructionPanel.getBackground());
        this.controller = buildActionsDescriptorPanel;
        documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update(e);
            }
        };
        ScriptTypeItem.items.forEach((item) -> {
            scriptTypeComboBox.addItem(item);
        });
        
        // init focus
        runConfigureCheckBox.requestFocus();
        // Accessibility
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PreBuildActionPanel.class, "MakefileOrConfigureName_AD"));
        addDocumentLiseners();
        refreshRunnable = new RefreshRunnable();
        refreshSourceFolderTask = RP.create(refreshRunnable);

    }

    private void addDocumentLiseners() {
        // Add change listeners
        configureRunFolderTextField.getDocument().addDocumentListener(documentListener);
        customCommandTextField.getDocument().addDocumentListener(documentListener);
        configureNameTextField.getDocument().addDocumentListener(documentListener);
        configureArgumentsTextField.getDocument().addDocumentListener(documentListener);
        listenersDisabled.set(false);
    }

    private void removeDocumentLiseners() {
        listenersDisabled.set(true);
        configureRunFolderTextField.getDocument().removeDocumentListener(documentListener);
        customCommandTextField.getDocument().removeDocumentListener(documentListener);
        configureNameTextField.getDocument().removeDocumentListener(documentListener);
        configureArgumentsTextField.getDocument().removeDocumentListener(documentListener);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("NewMakeWizardP11"); // NOI18N
    }

    private void update(DocumentEvent e) {
        String source =null;
        if (e!= null) {
            if (e.getDocument().equals(configureNameTextField.getDocument())) {
                source = "script"; //NOI18N
            } else if (e.getDocument().equals(configureArgumentsTextField.getDocument())) {
                source = "arguments"; //NOI18N
            } else if  (e.getDocument().equals(configureRunFolderTextField.getDocument())) {
                source = "folder"; //NOI18N
            } else if  (e.getDocument().equals(configureRunFolderTextField.getDocument())) {
                source = "folder"; //NOI18N
            }
        }
        update(source);
    }

    private void update(String source) {
        refreshSourceFolderTask.cancel();
        controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PreBuildActionPanel.class, "SelectModeError0")); // NOI18N
        controller.stateChanged(null);
        refreshRunnable.start(settings, source);
        refreshSourceFolderTask.schedule(VERIFY_DELAY);
    }
    
    void read(WizardDescriptor wizardDescriptor) {
        settings = wizardDescriptor;
        String hostUID = WizardConstants.PROPERTY_HOST_UID.get(wizardDescriptor);
        ExecutionEnvironment ee = null;
        if (hostUID != null) {
            ee = ExecutionEnvironmentFactory.fromUniqueID(hostUID);
        }
        CompilerSet cs = null;
        if (ee != null) {
            cs = WizardConstants.PROPERTY_TOOLCHAIN.get(wizardDescriptor);
        }
        try {
            removeDocumentLiseners();
            String path = WizardConstants.PROPERTY_SIMPLE_MODE_FOLDER.get(wizardDescriptor); // NOI18N
            if (path != null) {
                PreBuildArtifact configureScript;
                ExecutionEnvironment env = WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV.get(wizardDescriptor);
                if (env == null) {
                    env = ExecutionEnvironmentFactory.getLocal();
                }
                FileSystem fileSystem = FileSystemProvider.getFileSystem(env);
                configureScript = PreBuildSupport.findArtifactInFolder(fileSystem.findResource(path), env, cs);
                if (configureScript != null) {
                    runConfigureCheckBox.setSelected(true);
                    configureRunFolderTextField.setText(path);
                    predefinedCommandRadioButton.setSelected(true);
                    configureNameTextField.setText(configureScript.getScript().getPath());
                    for(ScriptTypeItem item : ScriptTypeItem.items) {
                        if (item.isSupported(configureScript)) {
                            scriptTypeComboBox.setSelectedItem(item);
                            break;
                        }
                    }
                    String configureArguments = configureScript.getArguments(ee, cs, ""); // NOI18N
                    configureArgumentsTextField.setText(configureArguments);
                    String commandLine = configureScript.getCommandLine(configureArguments, path);
                    commandTextArea.setText(PreBuildSupport.expandMacros(commandLine, cs, null));
                } else {
                    runConfigureCheckBox.setSelected(false);
                    configureRunFolderTextField.setText(path);
                    customCommandRadioButton.setSelected(true);
                }
            }
            enableControls();
        } finally {
            addDocumentLiseners();
        }
        update((DocumentEvent)null);
    }
    
    void store(WizardDescriptor wizardDescriptor) {
        WizardConstants.PROPERTY_RUN_CONFIGURE.put(wizardDescriptor, runConfigureCheckBox.isSelected());
        WizardConstants.PROPERTY_CONFIGURE_RUN_FOLDER.put(wizardDescriptor, configureRunFolderTextField.getText());
        if (customCommandRadioButton.isSelected()) {
            WizardConstants.PROPERTY_CONFIGURE_COMMAND.put(wizardDescriptor, customCommandTextField.getText());
            WizardConstants.PROPERTY_CONFIGURE_SCRIPT_PATH.put(wizardDescriptor, null);
            WizardConstants.PROPERTY_CONFIGURE_SCRIPT_ARGS.put(wizardDescriptor, null);
        } else if (predefinedCommandRadioButton.isSelected()) {
            WizardConstants.PROPERTY_CONFIGURE_COMMAND.put(wizardDescriptor, null);
            FileObject file = NewProjectWizardUtils.getFileObject( configureNameTextField.getText(), controller.getWizardDescriptor());
            if (file != null && file.isValid()) {
                PreBuildArtifact configureScript = PreBuildSupport.scriptToArtifact(file);
                if (configureScript != null) {
                    String arguments = configureArgumentsTextField.getText();
                    WizardConstants.PROPERTY_CONFIGURE_COMMAND.put(wizardDescriptor,
                            configureScript.getCommandLine(arguments, configureRunFolderTextField.getText()));
                }
            }
            WizardConstants.PROPERTY_CONFIGURE_SCRIPT_PATH.put(wizardDescriptor, configureNameTextField.getText());
            WizardConstants.PROPERTY_CONFIGURE_SCRIPT_ARGS.put(wizardDescriptor, configureArgumentsTextField.getText());
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        runConfigureCheckBox = new javax.swing.JCheckBox();
        configureRunFolderLabel = new javax.swing.JLabel();
        configureRunFolderTextField = new javax.swing.JTextField();
        runInFolderBrowseButton = new javax.swing.JButton();
        customCommandRadioButton = new javax.swing.JRadioButton();
        customCommandLabel = new javax.swing.JLabel();
        customCommandTextField = new javax.swing.JTextField();
        predefinedCommandRadioButton = new javax.swing.JRadioButton();
        scriptTypeLabel = new javax.swing.JLabel();
        scriptTypeComboBox = new javax.swing.JComboBox();
        configureNameLabel = new javax.swing.JLabel();
        configureNameTextField = new javax.swing.JTextField();
        configureBrowseButton = new javax.swing.JButton();
        configureArgumentsLabel = new javax.swing.JLabel();
        configureArgumentsTextField = new javax.swing.JTextField();
        commandLabel = new javax.swing.JLabel();
        commandScrollPane = new javax.swing.JScrollPane();
        commandTextArea = new javax.swing.JTextArea();
        instructionPanel = new javax.swing.JPanel();
        instructionsTextArea = new javax.swing.JTextArea();

        setPreferredSize(new java.awt.Dimension(450, 350));
        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(runConfigureCheckBox, bundle.getString("RUN_CONFIGURE_CHECKBOX")); // NOI18N
        runConfigureCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runConfigureCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(runConfigureCheckBox, gridBagConstraints);

        configureRunFolderLabel.setLabelFor(configureRunFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(configureRunFolderLabel, org.openide.util.NbBundle.getMessage(PreBuildActionPanel.class, "RunInFolderLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(configureRunFolderLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(configureRunFolderTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(runInFolderBrowseButton, org.openide.util.NbBundle.getMessage(PreBuildActionPanel.class, "RunInFolderBrowse")); // NOI18N
        runInFolderBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runInFolderBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(runInFolderBrowseButton, gridBagConstraints);

        buttonGroup1.add(customCommandRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(customCommandRadioButton, org.openide.util.NbBundle.getMessage(PreBuildActionPanel.class, "CustomCommandRadioButton")); // NOI18N
        customCommandRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customCommandRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(customCommandRadioButton, gridBagConstraints);

        customCommandLabel.setLabelFor(customCommandTextField);
        org.openide.awt.Mnemonics.setLocalizedText(customCommandLabel, org.openide.util.NbBundle.getMessage(PreBuildActionPanel.class, "CustomCommandLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 16, 0, 0);
        add(customCommandLabel, gridBagConstraints);

        customCommandTextField.setToolTipText(org.openide.util.NbBundle.getMessage(PreBuildActionPanel.class, "customCommandTextField_TOOLTIP")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(customCommandTextField, gridBagConstraints);

        buttonGroup1.add(predefinedCommandRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(predefinedCommandRadioButton, org.openide.util.NbBundle.getMessage(PreBuildActionPanel.class, "PredefinedCommandRadioButton")); // NOI18N
        predefinedCommandRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                predefinedCommandRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(predefinedCommandRadioButton, gridBagConstraints);

        scriptTypeLabel.setLabelFor(scriptTypeComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(scriptTypeLabel, org.openide.util.NbBundle.getMessage(PreBuildActionPanel.class, "ScriptType")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 16, 0, 0);
        add(scriptTypeLabel, gridBagConstraints);

        scriptTypeComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                scriptTypeComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(scriptTypeComboBox, gridBagConstraints);

        configureNameLabel.setLabelFor(configureNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(configureNameLabel, bundle.getString("CONFIGURE_NAME_LBL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 16, 0, 0);
        add(configureNameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(configureNameTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(configureBrowseButton, bundle.getString("CONFIGURE_BROWSE_BUTTON")); // NOI18N
        configureBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configureBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(configureBrowseButton, gridBagConstraints);

        configureArgumentsLabel.setLabelFor(configureArgumentsTextField);
        org.openide.awt.Mnemonics.setLocalizedText(configureArgumentsLabel, bundle.getString("CONFIGURE_ARGUMENT_LABEL_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 16, 0, 0);
        add(configureArgumentsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(configureArgumentsTextField, gridBagConstraints);

        commandLabel.setLabelFor(commandTextArea);
        org.openide.awt.Mnemonics.setLocalizedText(commandLabel, org.openide.util.NbBundle.getMessage(PreBuildActionPanel.class, "PreviewLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(commandLabel, gridBagConstraints);

        commandTextArea.setColumns(20);
        commandTextArea.setLineWrap(true);
        commandTextArea.setRows(4);
        commandScrollPane.setViewportView(commandTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(commandScrollPane, gridBagConstraints);

        instructionPanel.setLayout(new java.awt.GridBagLayout());

        instructionsTextArea.setEditable(false);
        instructionsTextArea.setLineWrap(true);
        instructionsTextArea.setText(bundle.getString("MakefileOrConfigureInstructions")); // NOI18N
        instructionsTextArea.setWrapStyleWord(true);
        instructionsTextArea.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        instructionPanel.add(instructionsTextArea, gridBagConstraints);
        instructionsTextArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PreBuildActionPanel.class, "CONFIGURE_HELP")); // NOI18N
        instructionsTextArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PreBuildActionPanel.class, "CONFIGURE_HELP_AD")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(instructionPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void configureBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureBrowseButtonActionPerformed
        String seed = configureNameTextField.getText();
        if (seed.isEmpty()) {
            String root = WizardConstants.PROPERTY_SIMPLE_MODE_FOLDER.get(controller.getWizardDescriptor());
            if (root != null && !root.isEmpty()) {
                seed = root;
            } else if (FileChooser.getCurrentChooserFile() != null) {
                seed = FileChooser.getCurrentChooserFile().getPath();
            } else {
                seed = System.getProperty("user.home"); // NOI18N
            }
        }
        ScriptTypeItem selectedItem = (ScriptTypeItem) scriptTypeComboBox.getSelectedItem();
        JFileChooser fileChooser = NewProjectWizardUtils.createFileChooser(controller.getWizardDescriptor(),
                selectedItem.getFileChooserTitle(),
                NbBundle.getMessage(PreBuildActionPanel.class, "MAKEFILE_CHOOSER_BUTTON_TXT"),
                JFileChooser.FILES_ONLY,
                selectedItem.getFileFilter(),
                seed,
                false);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        String path = fileChooser.getSelectedFile().getPath();
        path = CndPathUtilities.normalizeSlashes(path);
        configureNameTextField.setText(path);
        update(new DocumentEvent() {

            @Override
            public int getOffset() {
                return 0;
            }

            @Override
            public int getLength() {
                return configureNameTextField.getText().length();
            }

            @Override
            public Document getDocument() {
                return configureNameTextField.getDocument();
            }

            @Override
            public DocumentEvent.EventType getType() {
                return DocumentEvent.EventType.CHANGE;
            }

            @Override
            public DocumentEvent.ElementChange getChange(Element elem) {
                return null;
            }
        });
    }//GEN-LAST:event_configureBrowseButtonActionPerformed

    private void runConfigureCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runConfigureCheckBoxActionPerformed
        enableControls();
        update((String)null);
    }//GEN-LAST:event_runConfigureCheckBoxActionPerformed

    private void customCommandRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customCommandRadioButtonActionPerformed
        enableControls();
        update("type"); //NOI18N
    }//GEN-LAST:event_customCommandRadioButtonActionPerformed

    private void predefinedCommandRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_predefinedCommandRadioButtonActionPerformed
        enableControls();
        update("type"); //NOI18N
    }//GEN-LAST:event_predefinedCommandRadioButtonActionPerformed

    private void runInFolderBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runInFolderBrowseButtonActionPerformed
        String seed = configureRunFolderTextField.getText();
        if (seed.isEmpty()) {
            String root = WizardConstants.PROPERTY_SIMPLE_MODE_FOLDER.get(controller.getWizardDescriptor());
            if (root != null && !root.isEmpty()) {
                seed = root;
            } else if (FileChooser.getCurrentChooserFile() != null) {
                seed = FileChooser.getCurrentChooserFile().getPath();
            } else {
                seed = System.getProperty("user.home"); // NOI18N
            }
        }
        JFileChooser fileChooser = NewProjectWizardUtils.createFileChooser(controller.getWizardDescriptor(),
                NbBundle.getMessage(PreBuildActionPanel.class, "SelectRunInFolder"),
                NbBundle.getMessage(PreBuildActionPanel.class, "MAKEFILE_CHOOSER_BUTTON_TXT"),
                JFileChooser.DIRECTORIES_ONLY,
                null,
                seed,
                false);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        String path = fileChooser.getSelectedFile().getPath();
        path = CndPathUtilities.normalizeSlashes(path);
        configureRunFolderTextField.setText(path);
        update(new DocumentEvent() {

            @Override
            public int getOffset() {
                return 0;
            }

            @Override
            public int getLength() {
                return configureRunFolderTextField.getText().length();
            }

            @Override
            public Document getDocument() {
                return configureRunFolderTextField.getDocument();
            }

            @Override
            public DocumentEvent.EventType getType() {
                return DocumentEvent.EventType.CHANGE;
            }

            @Override
            public DocumentEvent.ElementChange getChange(Element elem) {
                return null;
            }
        });
    }//GEN-LAST:event_runInFolderBrowseButtonActionPerformed

    private void scriptTypeComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_scriptTypeComboBoxItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            if (!listenersDisabled.get()) {
                update("provider"); // NOI18N
            }
        }
    }//GEN-LAST:event_scriptTypeComboBoxItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel commandLabel;
    private javax.swing.JScrollPane commandScrollPane;
    private javax.swing.JTextArea commandTextArea;
    private javax.swing.JLabel configureArgumentsLabel;
    private javax.swing.JTextField configureArgumentsTextField;
    private javax.swing.JButton configureBrowseButton;
    private javax.swing.JLabel configureNameLabel;
    private javax.swing.JTextField configureNameTextField;
    private javax.swing.JLabel configureRunFolderLabel;
    private javax.swing.JTextField configureRunFolderTextField;
    private javax.swing.JLabel customCommandLabel;
    private javax.swing.JRadioButton customCommandRadioButton;
    private javax.swing.JTextField customCommandTextField;
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JTextArea instructionsTextArea;
    private javax.swing.JRadioButton predefinedCommandRadioButton;
    private javax.swing.JCheckBox runConfigureCheckBox;
    private javax.swing.JButton runInFolderBrowseButton;
    private javax.swing.JComboBox scriptTypeComboBox;
    private javax.swing.JLabel scriptTypeLabel;
    // End of variables declaration//GEN-END:variables

    private void enableControls() {
        runConfigureCheckBox.setEnabled(true);
        boolean selected = runConfigureCheckBox.isSelected();
        configureRunFolderLabel.setEnabled(selected);
        configureRunFolderTextField.setEnabled(selected);
        
        customCommandRadioButton.setEnabled(selected);
        boolean custom = customCommandRadioButton.isSelected();
        customCommandLabel.setEnabled(selected && custom);
        customCommandTextField.setEnabled(selected && custom);

        predefinedCommandRadioButton.setEnabled(selected);
        boolean predefined = predefinedCommandRadioButton.isSelected();
        scriptTypeLabel.setEnabled(selected && predefined);
        scriptTypeComboBox.setEnabled(selected && predefined);
        configureNameLabel.setEnabled(selected && predefined);
        configureNameTextField.setEnabled(selected && predefined);
        configureBrowseButton.setEnabled(selected && predefined);
        configureArgumentsLabel.setEnabled(selected && predefined);
        configureArgumentsTextField.setEnabled(selected && predefined);
        
        commandLabel.setEnabled(selected);
        commandTextArea.setEnabled(selected);
        commandTextArea.setEditable(false);
    }

    private class RefreshRunnable implements Runnable {

        private final AtomicInteger generation = new AtomicInteger(0);
        private WizardDescriptor settings;
        private String changedField; 

        public RefreshRunnable() {
        }

        private void start(WizardDescriptor settings, String changedField) {
            this.settings = settings;
            this.changedField = changedField;
            generation.incrementAndGet();
        }

        @Override
        public void run() {
            final int startCount = generation.get();
            // Validate fields
            String newConfigureNameTextField = null;
            String newConfigureArgumentsTextField = null;
            String newConfigureRunFolderTextField = null;
            String newCommandText = null;
            if (runConfigureCheckBox.isSelected()) {
                String hostUID = WizardConstants.PROPERTY_HOST_UID.get(settings);
                ExecutionEnvironment ee = null;
                if (hostUID != null) {
                    ee = ExecutionEnvironmentFactory.fromUniqueID(hostUID);
                }
                CompilerSet cs = null;
                if (ee != null) {
                    cs = WizardConstants.PROPERTY_TOOLCHAIN.get(settings);
                }
                if (predefinedCommandRadioButton.isSelected()) {
                    if ("provider".equals(changedField)) { // NOI18N
                        ScriptTypeItem item = (ScriptTypeItem) scriptTypeComboBox.getSelectedItem();
                        FileObject folder = NewProjectWizardUtils.getFileObject(configureRunFolderTextField.getText(), controller.getWizardDescriptor());
                        PreBuildArtifact otherArtifact = item.provider.findScriptInFolder(folder, ee, cs);
                        if (otherArtifact == null) {
                            // look at source root
                            FileObject root = NewProjectWizardUtils.getFileObject(WizardConstants.PROPERTY_SIMPLE_MODE_FOLDER.get(controller.getWizardDescriptor()), controller.getWizardDescriptor());
                            otherArtifact = item.provider.findScriptInFolder(root, ee, cs);
                            if (otherArtifact == null) {
                                String msg = NbBundle.getMessage(BuildActionsPanel.class, "NOTFOUNDCONFIGUREFILE", item.provider.getDisplayName(), cs == null? "" : cs.getName()); // NOI18N
                                controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg);
                                return;
                            }
                        }
                        newConfigureNameTextField = otherArtifact.getScript().getPath();
                    } else {
                        newConfigureNameTextField = configureNameTextField.getText();
                    }

                    if (newConfigureNameTextField.isEmpty()) {
                        String msg = NbBundle.getMessage(BuildActionsPanel.class, "NOCONFIGUREFILE"); // NOI18N
                        controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg);
                        return;//false;
                    }
                    if (!CndPathUtilities.isPathAbsolute(newConfigureNameTextField)) {
                        String msg = NbBundle.getMessage(BuildActionsPanel.class, "CONFIGUREFILEDOESNOTEXIST"); // NOI18N
                        controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg);
                        return;//false;
                    }
                    FileObject file = NewProjectWizardUtils.getFileObject(newConfigureNameTextField, controller.getWizardDescriptor());
                    if (file == null || !file.isValid() || file.isFolder()) {
                        String msg = NbBundle.getMessage(BuildActionsPanel.class, "CONFIGUREFILEDOESNOTEXIST"); // NOI18N
                        controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg);
                        return;//false;
                    }
                    PreBuildArtifact configureScript = PreBuildSupport.scriptToArtifact(file);
                    if (configureScript == null) {
                        String msg = NbBundle.getMessage(BuildActionsPanel.class, "UndefinedScriptMessage"); // NOI18N
                        controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg);
                        return;//false;
                    }
                    String message = configureScript.validate(ee, cs);
                    if (message != null) {
                        controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
                        return;//false;
                    }
                    if ("script".equals(changedField) || // NOI18N
                        "provider".equals(changedField)) { //NOI18N
                        if (configureRunFolderTextField.getText().isEmpty()) {
                            FileObject parent = file.getParent();
                            if (parent != null) {
                                newConfigureRunFolderTextField = parent.getPath();
                            }
                        }
                        newConfigureArgumentsTextField = configureScript.getArguments(ee, cs, "");
                    }
                    if ("script".equals(changedField) || //NOI18N
                        "folder".equals(changedField) || //NOI18N
                        "arguments".equals(changedField) || //NOI18N
                        "type".equals(changedField) || //NOI18N
                        "provider".equals(changedField)) { //NOI18N
                        String commandLine = configureScript.getCommandLine(
                                newConfigureArgumentsTextField == null ? configureArgumentsTextField.getText() : newConfigureArgumentsTextField,
                                newConfigureRunFolderTextField == null ? configureRunFolderTextField.getText() : newConfigureRunFolderTextField);
                        newCommandText = PreBuildSupport.expandMacros(commandLine, cs, null);
                    }
                } else if (customCommandRadioButton.isSelected()) {
                    if (customCommandTextField.getText().trim().isEmpty()) {
                        SwingUtilities.invokeLater(() -> {
                            commandTextArea.setText("");
                        });
                        String msg = NbBundle.getMessage(BuildActionsPanel.class, "CUSTOM_COMMAND_EMPTY"); // NOI18N
                        controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg);
                        return;//false;
                    }
                    newCommandText = PreBuildSupport.expandMacros(customCommandTextField.getText().trim(), cs, null);
                }
            }
            if (startCount < generation.get()) {
                return;
            }                

            final String finalConfigureNameTextField = newConfigureNameTextField;
            final String finalConfigureArgumentsTextField = newConfigureArgumentsTextField;
            final String finalConfigureRunFolderTextField = newConfigureRunFolderTextField;
            final String finalCommandText = newCommandText;
            SwingUtilities.invokeLater(() -> {
                if (startCount < generation.get()) {
                    return;
                }
                controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); // NOI18N
                try {
                    removeDocumentLiseners();
                    if (finalConfigureRunFolderTextField != null && !configureRunFolderTextField.getText().equals(finalConfigureArgumentsTextField)) {
                        configureRunFolderTextField.setText(finalConfigureRunFolderTextField);
                    }
                    if (finalConfigureNameTextField != null && !configureNameTextField.getText().equals(finalConfigureNameTextField)) {
                        configureNameTextField.setText(finalConfigureNameTextField);
                    }
                    if (finalConfigureArgumentsTextField != null && !configureArgumentsTextField.getText().equals(finalConfigureArgumentsTextField)) {
                        configureArgumentsTextField.setText(finalConfigureArgumentsTextField);
                    }
                    if (finalCommandText != null && !commandTextArea.getText().equals(finalCommandText)) {
                        commandTextArea.setText(finalCommandText);
                    }
                } finally {
                    addDocumentLiseners();
                }
                controller.stateChanged(null);
            });
        }
    }
    
    private static final class ScriptTypeItem {
        static final List<ScriptTypeItem> items;
        static {
            items = new ArrayList<>();
            PreBuildSupport.getPreBuildProviders().forEach((provider) -> {
                items.add(new ScriptTypeItem(provider));
            });
        }
        
        private final PreBuildArtifactProvider provider;

        private ScriptTypeItem(PreBuildArtifactProvider provider) {
            this.provider = provider;
        }

        public String getFileChooserTitle() {
            return provider.getFileChooserTitle();
        }

        FileFilter[] getFileFilter() {
            return provider.getFileFilter();
        }
        
        @Override
        public String toString() {
            return provider.getDisplayName();
        }

        private boolean isSupported(PreBuildArtifact script) {
            return provider.isSupported(script);
        }
    }
}
