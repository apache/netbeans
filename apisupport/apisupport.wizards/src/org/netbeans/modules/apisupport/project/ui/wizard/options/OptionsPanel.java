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

package org.netbeans.modules.apisupport.project.ui.wizard.options;

import java.awt.Component;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.netbeans.modules.apisupport.project.ui.wizard.common.WizardUtils;
import org.netbeans.modules.apisupport.project.ui.wizard.options.NewOptionsIterator.DataModel;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Radek Matous
 */
final class OptionsPanel extends BasicWizardIterator.Panel {
    
    private final DataModel data;
    private DocumentListener updateListener;
    
    /** Creates new NameAndLocationPanel */
    public OptionsPanel(final WizardDescriptor setting, final DataModel data) {
        super(setting);
        this.data = data;
        initComponents();
        initAccessibility();
        if (data.getPackageName() != null) {
            packageName.setSelectedItem(data.getPackageName());
        }
        putClientProperty("NewFileWizard_Title", getMessage("LBL_OptionsWizardTitle"));
        
    }
    
    private void addListeners() {
        if (updateListener == null) {
            updateListener = new UIUtil.DocumentAdapter() {
                public void insertUpdate(DocumentEvent e) {
                    updateData();
                }          
            };
            
            txtPrefix.getDocument().addDocumentListener(updateListener);
            Component editorComp = packageName.getEditor().getEditorComponent();
            if (editorComp instanceof JTextComponent) {
                ((JTextComponent) editorComp).getDocument().addDocumentListener(updateListener);
            }
        }
    }
    
    private void removeListeners() {
        if (updateListener != null) {
            txtPrefix.getDocument().removeDocumentListener(updateListener);            
            Component editorComp = packageName.getEditor().getEditorComponent();
            if (editorComp instanceof JTextComponent) {
                ((JTextComponent) editorComp).getDocument().removeDocumentListener(updateListener);
            }
            updateListener = null;
        }
    }
    
    protected void storeToDataModel() {
        removeListeners();
        updateData();
    }
    
    protected void readFromDataModel() {
        addListeners();
        txtPrefix.setText(data.getClassNamePrefix());
    }

    public void removeNotify() {
        super.removeNotify();
        removeListeners();
    }
    
    public void addNotify() {
        super.addNotify();
        addListeners();
        updateData();
    }
    
    private void updateData() {
        int errCode = data.setPackageAndPrefix(
                packageName.getEditor().getItem().toString(),txtPrefix.getText());
        
        String msg = data.getMessage(errCode);
        if (DataModel.isErrorCode(errCode)) {
            setError(msg);
            return;
        }  else if (DataModel.isWarningCode(errCode)) {
            setWarning(msg);
            // generate CMF
        } else if (DataModel.isInfoCode(errCode)) {
            setInfo(msg, false);
            return;
        }
        
        data.getCreatedModifiedFiles();
        createdFilesValue.setText(WizardUtils.generateTextAreaContent(
                data.getCreatedModifiedFiles().getCreatedPaths()));
        modifiedFilesValue.setText(WizardUtils.generateTextAreaContent(
                data.getCreatedModifiedFiles().getModifiedPaths()));
        
        //#68294 check if the paths for newly created files are valid or not..
        String[] invalid  = data.getCreatedModifiedFiles().getInvalidPaths();
         if (DataModel.isErrorCode(errCode)) {
            setError(data.getMessage(errCode));//NOI18N
        } else if (invalid.length > 0) {
            setError(NbBundle.getMessage(OptionsPanel.class, "ERR_ToBeCreateFileExists", invalid[0]));//NOI18N
        } else if (DataModel.isSuccessCode(errCode)) {
            markValid();            
        } 
    }
    
    
    protected String getPanelName() {
        return getMessage("LBL_OptionsPanel1_Title");
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(OptionsPanel.class);
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(OptionsPanel.class, key);
    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(getMessage("ACS_OptionsPanel"));        
        projectNameValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_ProjectName"));
        packageName.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_PackageName"));
        createdFilesValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_CreatedFilesValue"));
        modifiedFilesValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_ModifiedFilesValue"));
        txtPrefix.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL__ClassNamePrefix"));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        projectName = new javax.swing.JLabel();
        projectNameValue = new JTextField(ProjectUtils.getInformation(this.data.getProject()).getDisplayName());
        createdFiles = new javax.swing.JLabel();
        modifiedFiles = new javax.swing.JLabel();
        createdFilesValueS = new javax.swing.JScrollPane();
        createdFilesValue = new javax.swing.JTextArea();
        modifiedFilesValueS = new javax.swing.JScrollPane();
        modifiedFilesValue = new javax.swing.JTextArea();
        packageName = WizardUtils.createPackageComboBox(data.getSourceRootGroup());
        packageNameTxt = new javax.swing.JLabel();
        lblPrefix = new javax.swing.JLabel();
        txtPrefix = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        projectName.setLabelFor(projectNameValue);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/options/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(projectName, bundle.getString("LBL_ProjectName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 6, 12);
        add(projectName, gridBagConstraints);
        projectName.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel.projectName.AccessibleContext.accessibleDescription")); // NOI18N

        projectNameValue.setEditable(false);
        projectNameValue.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(projectNameValue, gridBagConstraints);
        projectNameValue.getAccessibleContext().setAccessibleName(getMessage("OptionsPanel.projectNameValue.AccessibleContext.accessibleName")); // NOI18N
        projectNameValue.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel.projectNameValue.AccessibleContext.accessibleDescription")); // NOI18N

        createdFiles.setLabelFor(createdFilesValue);
        org.openide.awt.Mnemonics.setLocalizedText(createdFiles, bundle.getString("LBL_CreatedFiles")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 12);
        add(createdFiles, gridBagConstraints);
        createdFiles.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel.createdFiles.AccessibleContext.accessibleDescription")); // NOI18N

        modifiedFiles.setLabelFor(modifiedFilesValue);
        org.openide.awt.Mnemonics.setLocalizedText(modifiedFiles, bundle.getString("LBL_ModifiedFiles")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(modifiedFiles, gridBagConstraints);
        modifiedFiles.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel.modifiedFiles.AccessibleContext.accessibleDescription")); // NOI18N

        createdFilesValueS.setBorder(null);

        createdFilesValue.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        createdFilesValue.setColumns(20);
        createdFilesValue.setEditable(false);
        createdFilesValue.setRows(5);
        createdFilesValue.setBorder(null);
        createdFilesValueS.setViewportView(createdFilesValue);
        createdFilesValue.getAccessibleContext().setAccessibleName(getMessage("OptionsPanel.createdFilesValue.AccessibleContext.accessibleName")); // NOI18N
        createdFilesValue.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel.createdFilesValue.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 0);
        add(createdFilesValueS, gridBagConstraints);
        createdFilesValueS.getAccessibleContext().setAccessibleName(getMessage("OptionsPanel.createdFilesValueS.AccessibleContext.accessibleName")); // NOI18N
        createdFilesValueS.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel.createdFilesValueS.AccessibleContext.accessibleDescription")); // NOI18N

        modifiedFilesValueS.setBorder(null);

        modifiedFilesValue.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        modifiedFilesValue.setColumns(20);
        modifiedFilesValue.setEditable(false);
        modifiedFilesValue.setRows(5);
        modifiedFilesValue.setToolTipText("modifiedFilesValue");
        modifiedFilesValue.setBorder(null);
        modifiedFilesValueS.setViewportView(modifiedFilesValue);
        modifiedFilesValue.getAccessibleContext().setAccessibleName(getMessage("OptionsPanel.modifiedFilesValue.AccessibleContext.accessibleName")); // NOI18N
        modifiedFilesValue.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel.modifiedFilesValue.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        add(modifiedFilesValueS, gridBagConstraints);
        modifiedFilesValueS.getAccessibleContext().setAccessibleName(getMessage("OptionsPanel.modifiedFilesValueS.AccessibleContext.accessibleName")); // NOI18N
        modifiedFilesValueS.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel.modifiedFilesValueS.AccessibleContext.accessibleDescription")); // NOI18N

        packageName.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(packageName, gridBagConstraints);
        packageName.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel.packageName.AccessibleContext.accessibleDescription")); // NOI18N

        packageNameTxt.setLabelFor(packageName);
        org.openide.awt.Mnemonics.setLocalizedText(packageNameTxt, org.openide.util.NbBundle.getMessage(OptionsPanel.class, "LBL_PackageName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(packageNameTxt, gridBagConstraints);
        packageNameTxt.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel.packageNameTxt.AccessibleContext.accessibleDescription")); // NOI18N

        lblPrefix.setLabelFor(txtPrefix);
        org.openide.awt.Mnemonics.setLocalizedText(lblPrefix, org.openide.util.NbBundle.getMessage(OptionsPanel.class, "LBL_Prefix")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 6, 12);
        add(lblPrefix, gridBagConstraints);
        lblPrefix.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel.lblPrefix.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 6, 0);
        add(txtPrefix, gridBagConstraints);
        txtPrefix.getAccessibleContext().setAccessibleName(getMessage("OptionsPanel.txtPrefix.AccessibleContext.accessibleName")); // NOI18N
        txtPrefix.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel.txtPrefix.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(getMessage("OptionsPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel createdFiles;
    private javax.swing.JTextArea createdFilesValue;
    private javax.swing.JScrollPane createdFilesValueS;
    private javax.swing.JLabel lblPrefix;
    private javax.swing.JLabel modifiedFiles;
    private javax.swing.JTextArea modifiedFilesValue;
    private javax.swing.JScrollPane modifiedFilesValueS;
    private javax.swing.JComboBox packageName;
    private javax.swing.JLabel packageNameTxt;
    private javax.swing.JLabel projectName;
    private javax.swing.JTextField projectNameValue;
    private javax.swing.JTextField txtPrefix;
    // End of variables declaration//GEN-END:variables
    
}
