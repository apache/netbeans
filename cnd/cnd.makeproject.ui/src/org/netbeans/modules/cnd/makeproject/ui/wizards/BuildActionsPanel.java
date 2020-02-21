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
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.html.HTMLEditorKit;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.wizards.BuildSupport;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.ui.DocumentAdapter;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class BuildActionsPanel extends javax.swing.JPanel implements HelpCtx.Provider{
    
    private final DocumentListener documentListener;
    private final BuildActionsDescriptorPanel controller;
    private String makefileName = null;
    
    private static final String DEF_WORKING_DIR = ""; // NOI18N
    private static final String DEF_BUILD_COMMAND = MakeArtifact.MAKE_MACRO;
    private static final String DEF_CLEAN_COMMAND = MakeArtifact.MAKE_MACRO+" clean"; // NOI18N
    private static final String DEF_COMMAND_CLEAN_COMMAND = "echo clean"; // NOI18N
    private static final String DEF_COMMAND_BUILD_COMMAND = "echo build"; // NOI18N
    
    /*package-local*/ BuildActionsPanel(BuildActionsDescriptorPanel buildActionsDescriptorPanel) {
        initComponents();
        instructionsTextPane.setEditorKit(new HTMLEditorKit());
        instructionsTextPane.setBackground(instructionPanel.getBackground());
        instructionsTextPane.setForeground(instructionPanel.getForeground());
        instructionsTextPane.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        this.controller = buildActionsDescriptorPanel;
        documentListener = new DocumentAdapter() {
            @Override
            protected void update(DocumentEvent e) {
                BuildActionsPanel.this.update();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                BuildActionsPanel.this.update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                BuildActionsPanel.this.update();
            }
        };
        
        // Add change listeners
        buildCommandWorkingDirTextField.getDocument().addDocumentListener(documentListener);
        buildCommandTextField.getDocument().addDocumentListener(documentListener);
        cleanCommandTextField.getDocument().addDocumentListener(documentListener);
        buildLogTextField.getDocument().addDocumentListener(documentListener);
        
        // init focus
        buildCommandWorkingDirTextField.selectAll();
        buildCommandWorkingDirTextField.requestFocus();
        
        // Accessibility
        getAccessibleContext().setAccessibleDescription(getString("BUILD_ACTIONS_PANEL_AD"));
        buildCommandTextField.getAccessibleContext().setAccessibleDescription(getString("BUILD_COMMAND_AD"));
        buildCommandWorkingDirTextField.getAccessibleContext().setAccessibleDescription(getString("WORKING_DIR_AD"));
        cleanCommandTextField.getAccessibleContext().setAccessibleDescription(getString("CLEAN_COMMAND_AD"));
        buildCommandWorkingDirBrowseButton.getAccessibleContext().setAccessibleDescription(getString("WORKING_DIR_BROWSE_BUTTON_AD"));
    }
    
    private void makefileFieldChanged() {
        if (makefileName == null || makefileName.isEmpty()) {
            String root = WizardConstants.PROPERTY_SIMPLE_MODE_FOLDER.get(controller.getWizardDescriptor());
            buildCommandWorkingDirTextField.setText(CndPathUtilities.normalizeSlashes(root));
            buildCommandTextField.setText(DEF_COMMAND_BUILD_COMMAND);
            cleanCommandTextField.setText(DEF_COMMAND_CLEAN_COMMAND);
        } else {
            String workinDir = CndPathUtilities.getDirName(makefileName);
            if (workinDir != null) {
                buildCommandWorkingDirTextField.setText(workinDir);
                BuildSupport.BuildFile scriptToBuildFile = BuildSupport.scriptToBuildFile(makefileName);
                if (scriptToBuildFile != null) {
                    buildCommandTextField.setText(scriptToBuildFile.getBuildCommandLine(null, workinDir));
                    cleanCommandTextField.setText(scriptToBuildFile.getCleanCommandLine(null, workinDir));
                }
            }
        }
        updateInstriction();
    }
    
    private void initFields() {
        // Set default values
        buildCommandWorkingDirTextField.setText(DEF_WORKING_DIR);
        buildCommandTextField.setText(DEF_BUILD_COMMAND);
        cleanCommandTextField.setText(DEF_CLEAN_COMMAND);
        makeCheckBox.setSelected(true);
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(BuildActionsPanel.class);
    }
    
    private void update() {
        controller.stateChanged(null);
    }
    
    void read(WizardDescriptor wizardDescriptor) {
        String mn = initMakeFile(wizardDescriptor);
        if (mn == null || mn.isEmpty()) {
            if (makefileName == null) {
                initFields();
                makefileName = "";
                makefileFieldChanged();
            }
        } else {
            if (makefileName == null || !makefileName.equals(mn)) {
                initFields();
                makefileName = mn;
                makefileFieldChanged();
            }
        }
        makeCheckBox.setEnabled(true);
    }
    
    private String initMakeFile(WizardDescriptor wizardDescriptor) {
        String res = null;
        String path = WizardConstants.PROPERTY_SIMPLE_MODE_FOLDER.get(wizardDescriptor);
        if (path != null) {
            if (Boolean.TRUE.equals(WizardConstants.PROPERTY_RUN_CONFIGURE.get(wizardDescriptor))) {
                String folder = WizardConstants.PROPERTY_CONFIGURE_RUN_FOLDER.get(wizardDescriptor);
                res = folder+"/Makefile"; //NOI18N
                ExecutionEnvironment env = WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV.get(wizardDescriptor);
                if (env != null) {
                    res = RemoteFileUtil.normalizeAbsolutePath(res, env);
                }
            }
            if (res == null) {
                ExecutionEnvironment ee = NewProjectWizardUtils.getExecutionEnvironment(wizardDescriptor);
                CompilerSet cs = WizardConstants.PROPERTY_TOOLCHAIN.get(wizardDescriptor);
                BuildSupport.BuildFile buildFile = BuildSupport.findBuildFileInFolder(WizardConstants.PROPERTY_NATIVE_PROJ_FO.get(wizardDescriptor), ee, cs);
                if (buildFile != null) {
                    res = buildFile.getFile();
                }
            }
        }
        return res;
    }
    
    void store(WizardDescriptor wizardDescriptor) {
        WizardConstants.PROPERTY_WORKING_DIR.put(wizardDescriptor, buildCommandWorkingDirTextField.getText()); 
        WizardConstants.PROPERTY_BUILD_COMMAND.put(wizardDescriptor, buildCommandTextField.getText()); 
        WizardConstants.PROPERTY_CLEAN_COMMAND.put(wizardDescriptor, cleanCommandTextField.getText()); 
        WizardConstants.PROPERTY_BUILD_RESULT.put(wizardDescriptor, "");
        WizardConstants.PROPERTY_BUILD_LOG.put(wizardDescriptor, buildLogTextField.getText());
        WizardConstants.PROPERTY_RUN_REBUILD.put(wizardDescriptor, makeCheckBox.isSelected() ? Boolean.TRUE : Boolean.FALSE);
        WizardConstants.PROPERTY_USER_MAKEFILE_PATH.put(wizardDescriptor, makefileName);
    }
    
    boolean valid(WizardDescriptor settings) {
        if (buildCommandWorkingDirTextField.getText().length() == 0) {
            String msg = NbBundle.getMessage(BuildActionsPanel.class, "NOWORKINGDIR"); // NOI18N
            controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg);
            //return false;
        }
        if (buildCommandWorkingDirTextField.getText().length() > 0) {
            if (!CndPathUtilities.isPathAbsolute(buildCommandWorkingDirTextField.getText()) 
                    || !NewProjectWizardUtils.fileExists(buildCommandWorkingDirTextField.getText(), controller.getWizardDescriptor())) {
                String msg = NbBundle.getMessage(BuildActionsPanel.class, "WORKINGDIRDOESNOTEXIST"); // NOI18N
                controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg);
                //return false;
            }
        }
        if (!makeCheckBox.isSelected()) {
            String path = buildLogTextField.getText().trim();
            if (!path.isEmpty()) {
                FileObject file = NewProjectWizardUtils.getFileObject(path, settings);
                boolean exists = file != null && file.isValid() && file.isData() && file.canRead();
                if (!exists) {
                    settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, getString("BUILD_LOG_NOT_EXISTS")); // NOI18N
                }
                return exists;
            }
        }
        
        return true;
    }

    private void updateInstriction() {
        if (makeCheckBox.isSelected()) {
            instructionsTextPane.setText(NbBundle.getMessage(BuildActionsPanel.class, "BuildActionsInstructions"));
        } else {
            instructionsTextPane.setText(NbBundle.getMessage(BuildActionsPanel.class, "BuildActionsInstructionsNoBuild"));
        }
    }
    
    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buildCommandWorkingDirLabel = new javax.swing.JLabel();
        buildCommandWorkingDirTextField = new javax.swing.JTextField();
        buildCommandWorkingDirBrowseButton = new javax.swing.JButton();
        buildCommandLabel = new javax.swing.JLabel();
        buildCommandTextField = new javax.swing.JTextField();
        cleanCommandLabel = new javax.swing.JLabel();
        cleanCommandTextField = new javax.swing.JTextField();
        instructionPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        instructionsTextPane = new javax.swing.JTextPane();
        group2Label = new javax.swing.JLabel();
        makeCheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        buildLogLabel = new javax.swing.JLabel();
        buildLogTextField = new javax.swing.JTextField();
        buildLogButton = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(450, 350));
        setLayout(new java.awt.GridBagLayout());

        buildCommandWorkingDirLabel.setLabelFor(buildCommandWorkingDirTextField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(buildCommandWorkingDirLabel, bundle.getString("WORKING_DIR_LBL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        add(buildCommandWorkingDirLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 0);
        add(buildCommandWorkingDirTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(buildCommandWorkingDirBrowseButton, bundle.getString("WORKING_DIR_BROWSE_BUTTON_TXT")); // NOI18N
        buildCommandWorkingDirBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buildCommandWorkingDirBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 0);
        add(buildCommandWorkingDirBrowseButton, gridBagConstraints);

        buildCommandLabel.setLabelFor(buildCommandTextField);
        org.openide.awt.Mnemonics.setLocalizedText(buildCommandLabel, bundle.getString("BUILD_COMMAND_LBL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 16, 0, 0);
        add(buildCommandLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(buildCommandTextField, gridBagConstraints);

        cleanCommandLabel.setLabelFor(cleanCommandTextField);
        org.openide.awt.Mnemonics.setLocalizedText(cleanCommandLabel, bundle.getString("CLEAN_COMMAND_LBL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 16, 0, 0);
        add(cleanCommandLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(cleanCommandTextField, gridBagConstraints);

        instructionPanel.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(null);
        jScrollPane1.setViewportView(instructionsTextPane);

        instructionPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(instructionPanel, gridBagConstraints);

        group2Label.setText(bundle.getString("GROUP2_LBL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(group2Label, gridBagConstraints);

        makeCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(makeCheckBox, bundle.getString("CLEAN_BUILD_CHECKBOX")); // NOI18N
        makeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makeCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(makeCheckBox, gridBagConstraints);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(BuildActionsPanel.class, "AdditionalBuildAtrifacts")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jLabel1, gridBagConstraints);

        buildLogLabel.setLabelFor(buildLogTextField);
        org.openide.awt.Mnemonics.setLocalizedText(buildLogLabel, org.openide.util.NbBundle.getMessage(BuildActionsPanel.class, "BUILD_LOG_TEXT_FIELD")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        add(buildLogLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(buildLogTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(buildLogButton, org.openide.util.NbBundle.getMessage(BuildActionsPanel.class, "BUILD_LOG_BROWSE_BUTTON")); // NOI18N
        buildLogButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buildLogButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(buildLogButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
        
    private void buildCommandWorkingDirBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buildCommandWorkingDirBrowseButtonActionPerformed
        String seed;
        if (!buildCommandWorkingDirTextField.getText().isEmpty()) {
            seed = buildCommandWorkingDirTextField.getText();
//        } else if (makefileNameTextField.getText().length() > 0) {
//            seed = makefileNameTextField.getText();
        } else if (FileChooser.getCurrentChooserFile() != null) {
            seed = FileChooser.getCurrentChooserFile().getPath();
        } else {
            seed = System.getProperty("user.home"); // NOI18N
        }
        
        JFileChooser fileChooser = NewProjectWizardUtils.createFileChooser(
                controller.getWizardDescriptor(),
                getString("WORKING_DIR_CHOOSER_TITLE_TXT"),
                getString("WORKING_DIR_BUTTON_TXT"),
                JFileChooser.DIRECTORIES_ONLY,
                null,
                seed,
                false
                );
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        String path = fileChooser.getSelectedFile().getPath();
        path = CndPathUtilities.normalizeSlashes(path);
        buildCommandWorkingDirTextField.setText(path);
    }//GEN-LAST:event_buildCommandWorkingDirBrowseButtonActionPerformed

    private void buildLogButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buildLogButtonActionPerformed
        String seed;
        if (buildLogTextField.getText().length() > 0) {
            seed = buildLogTextField.getText();
        } else if (FileChooser.getCurrentChooserFile() != null) {
            seed = FileChooser.getCurrentChooserFile().getPath();
        } else {
            seed = System.getProperty("user.home"); // NOI18N
        }
        JFileChooser fileChooser = NewProjectWizardUtils.createFileChooser(
            controller.getWizardDescriptor(),
            getString("BUILD_LOG_CHOOSER_TITLE_TXT"),
            getString("BUILD_LOG_CHOOSER_BUTTON_TXT"),
            JFileChooser.FILES_ONLY,
            new FileFilter[] {new LogFileFilter()},
            seed,
            false
        );
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        String path = fileChooser.getSelectedFile().getPath();
        path = CndPathUtilities.normalizeSlashes(path);
        buildLogTextField.setText(path);
    }//GEN-LAST:event_buildLogButtonActionPerformed

    private void makeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makeCheckBoxActionPerformed
        updateInstriction();
    }//GEN-LAST:event_makeCheckBoxActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel buildCommandLabel;
    private javax.swing.JTextField buildCommandTextField;
    private javax.swing.JButton buildCommandWorkingDirBrowseButton;
    private javax.swing.JLabel buildCommandWorkingDirLabel;
    private javax.swing.JTextField buildCommandWorkingDirTextField;
    private javax.swing.JButton buildLogButton;
    private javax.swing.JLabel buildLogLabel;
    private javax.swing.JTextField buildLogTextField;
    private javax.swing.JLabel cleanCommandLabel;
    private javax.swing.JTextField cleanCommandTextField;
    private javax.swing.JLabel group2Label;
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JTextPane instructionsTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JCheckBox makeCheckBox;
    // End of variables declaration//GEN-END:variables
    
    private static String getString(String s) {
        return NbBundle.getBundle(BuildActionsPanel.class).getString(s);
    }

    private static class LogFileFilter extends javax.swing.filechooser.FileFilter {
        public LogFileFilter() {
        }
        @Override
        public String getDescription() {
            return(getString("FILECHOOSER_BUILD_LOG_FILEFILTER")); // NOI18N
        }
        @Override
        public boolean accept(File f) {
            if (f != null) {
                if (f.isDirectory()) {
                    return true;
                }
                String name = f.getName();
                return name.endsWith(".log") || name.endsWith(".json"); // NOI18N
            }
            return false;
        }
    }
}
