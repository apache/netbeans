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

package org.netbeans.modules.apisupport.project.ui.wizard.action;

import java.awt.Component;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.common.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.netbeans.modules.apisupport.project.ui.wizard.common.WizardUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * The thrid panel in the <em>New Action Wizard</em>.
 *
 * @author Martin Krauskopf
 */
final class NameIconLocationPanel extends BasicWizardIterator.Panel {
    
    private static final String NONE_LABEL = getMessage("CTL_None");
    
    private final DataModel data;
    private final DocumentListener updateListener;
    
    private File smallIconPath;
    private File largeIconPath;
    
    /** Creates new NameIconLocationPanel */
    public NameIconLocationPanel(final WizardDescriptor setting, final DataModel data) {
        super(setting);
        this.data = data;
        initComponents();
        initAccessibility();
        if (data.getPackageName() != null) {
            packageName.setSelectedItem(data.getPackageName());
        }
        putClientProperty("NewFileWizard_Title", getMessage("LBL_ActionWizardTitle"));
        className.select(0, className.getText().length());
        updateListener = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                updateData();
            }
        };
    }
    
    private void addListeners() {
        className.getDocument().addDocumentListener(updateListener);
        displayName.getDocument().addDocumentListener(updateListener);
        Component editorComp = packageName.getEditor().getEditorComponent();
        if (editorComp instanceof JTextComponent) {
            ((JTextComponent) editorComp).getDocument().addDocumentListener(updateListener);
        }
    }
    
    private void removeListeners() {
        className.getDocument().removeDocumentListener(updateListener);
        displayName.getDocument().removeDocumentListener(updateListener);
        Component editorComp = packageName.getEditor().getEditorComponent();
        if (editorComp instanceof JTextComponent) {
            ((JTextComponent) editorComp).getDocument().removeDocumentListener(updateListener);
        }
    }
    
    protected String getPanelName() {
        return getMessage("LBL_NameIconLocation_Title");
    }
    
    protected void storeToDataModel() {
        removeListeners();
        storeBaseData();
    }
    
    protected void readFromDataModel() {
        updateData();
        addListeners();
    }
    
    private void updateData() {
        storeBaseData();
        if (checkValidity()) {
            CreatedModifiedFiles files = data.getCreatedModifiedFiles();
            createdFiles.setText(WizardUtils.generateTextAreaContent(files.getCreatedPaths()));
            modifiedFiles.setText(WizardUtils.generateTextAreaContent(files.getModifiedPaths()));
        }
    }
    
    /** Data needed to compute CMF. ClassName, packageName, icon. */
    private void storeBaseData() {
        data.setClassName(getClassName());
        data.setPackageName(packageName.getEditor().getItem().toString());
        data.setIconPath(smallIconPath);
        data.setLargeIconPath(largeIconPath);
        data.setDisplayName(displayName.getText());
    }
    
    private String getIconPath() {
        return icon.getText().equals(NONE_LABEL) ? null : icon.getText();
    }
    
    private boolean checkValidity() {
        String pName = packageName.getEditor().getItem() == null ? "" : packageName.getEditor().getItem().toString().trim();
        if (!Utilities.isJavaIdentifier(getClassName())) {
            setError(getMessage("MSG_ClassNameMustBeValidJavaIdentifier"));
        } else if (getDisplayName().trim().length() == 0) {
            setInfo(getMessage("MSG_DisplayNameMustBeEntered"), false);
        } else if (pName.length() == 0 || !WizardUtils.isValidPackageName(pName)) {
            setError(getMessage("ERR_Package_Invalid"));
        } else if (classAlreadyExists()) {
            setError(getMessage("MSG_ClassAlreadyExists"));
        } else if (data.isToolbarEnabled() && getIconPath() == null) {
            setError(getMessage("MSG_IconRequiredForToolbar"));
        } else {
            String[] invalid = data.getCreatedModifiedFiles().getInvalidPaths();
            if (invalid.length > 0) {
                setWarning(WizardUtils.getIconAlreadyExistsWarning(invalid[0]));
            } else {
                markValid();
                checkIconValidity();
                return true;
            }
        }
        return false;
    }

    private void checkIconValidity() {
        if (smallIconPath == null) {
            setWarning(WizardUtils.getNoIconSelectedWarning(16,16));
        } else if (!WizardUtils.isValidIcon(smallIconPath, 16, 16)) {
            setWarning(WizardUtils.getIconDimensionWarning(smallIconPath, 16, 16));
        } else if (data.isToolbarEnabled() && largeIconPath == null) {
            assert smallIconPath.getParentFile() != null;
            String name = getName(smallIconPath);
            String ext = getExt(smallIconPath);
            StringBuffer sb = new StringBuffer();
            sb.append(name).append("24"); // NOI18N
            if (ext != null) {
                sb.append('.').append(ext);
            }
            setWarning(NbBundle.getMessage(NameIconLocationPanel.class,
                    "MSG_NoLargeIconSelected", sb.toString(), smallIconPath.getParent())); // NOI18N
        }
    }

    private static String  getName(final File smallIconFile) {
        String name = smallIconFile.getName();
        int i = name.lastIndexOf('.');            
        return (i <= 0) ? name : name.substring(0, i);
    }

    private static String  getExt(final File smallIconFile) {
        String name = smallIconFile.getName();
        int i = name.lastIndexOf('.') + 1;
        return ((i <= 1) || (i == name.length())) ? "" : name.substring(i);
    }
    
    private static Set<File> getPossibleIcons(final String iconPath) {
        File icon = new File(iconPath);
        String[] resultSuffixes = { "16", "24", "" }; // NOI18N
        Set<File> results = new HashSet<File>();
        String iconName = icon.getName();
        int idx = iconName.lastIndexOf('.');
        String name = (idx != -1) ? iconName.substring(0,idx) : iconName;
        String extension = (idx != -1) ? iconName.substring(idx+1) : null;
        boolean hasSuffix = (name.endsWith("24")) || (name.endsWith("16"));//NOI18N
        name = hasSuffix ? name.substring(0,name.length()-2) : name;
        for (int i = 0; i < resultSuffixes.length; i++) {
            String resultSuffix = resultSuffixes[i];
            String resultName = name + resultSuffix;
            if (extension != null) {
                resultName = resultName + '.' + extension;
            }
            File f = new File(icon.getParentFile(),resultName);
            if (f.exists()) {
                results.add(FileUtil.normalizeFile(f));
            }
        }        
        return results;
    }
    
    private boolean classAlreadyExists() {
        return data.classExists();
    }
    
    private String getDisplayName() {
        return displayName.getText().trim();
    }
    
    private String getClassName() {
        return className.getText().trim();
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(NameIconLocationPanel.class);
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(NameIconLocationPanel.class, key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        classNameTxt = new javax.swing.JLabel();
        className = new javax.swing.JTextField();
        displayNameTxt = new javax.swing.JLabel();
        displayName = new javax.swing.JTextField();
        iconTxt = new javax.swing.JLabel();
        icon = new javax.swing.JTextField();
        iconButton = new javax.swing.JButton();
        projectTxt = new javax.swing.JLabel();
        project = new JTextField(ProjectUtils.getInformation(data.getProject()).getDisplayName());
        packageNameTxt = new javax.swing.JLabel();
        packageName = WizardUtils.createPackageComboBox(data.getSourceRootGroup());
        createdFilesTxt = new javax.swing.JLabel();
        createdFiles = new javax.swing.JTextArea();
        modifiedFilesTxt = new javax.swing.JLabel();
        modifiedFiles = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        classNameTxt.setLabelFor(className);
        org.openide.awt.Mnemonics.setLocalizedText(classNameTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_ClassName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 12);
        add(classNameTxt, gridBagConstraints);

        className.setText(org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "CTL_SampleClassName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(className, gridBagConstraints);

        displayNameTxt.setLabelFor(displayName);
        org.openide.awt.Mnemonics.setLocalizedText(displayNameTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_DisplayName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 12);
        add(displayNameTxt, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(displayName, gridBagConstraints);

        iconTxt.setLabelFor(icon);
        org.openide.awt.Mnemonics.setLocalizedText(iconTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_Icon")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        add(iconTxt, gridBagConstraints);

        icon.setEditable(false);
        icon.setText(org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "CTL_None")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(icon, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(iconButton, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_Icon_Browse")); // NOI18N
        iconButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iconButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(iconButton, gridBagConstraints);

        projectTxt.setLabelFor(project);
        org.openide.awt.Mnemonics.setLocalizedText(projectTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_ProjectName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 12);
        add(projectTxt, gridBagConstraints);

        project.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 0);
        add(project, gridBagConstraints);

        packageNameTxt.setLabelFor(packageName);
        org.openide.awt.Mnemonics.setLocalizedText(packageNameTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_PackageName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(packageNameTxt, gridBagConstraints);

        packageName.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(packageName, gridBagConstraints);

        createdFilesTxt.setLabelFor(createdFiles);
        org.openide.awt.Mnemonics.setLocalizedText(createdFilesTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_CreatedFiles")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 12);
        add(createdFilesTxt, gridBagConstraints);

        createdFiles.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        createdFiles.setColumns(20);
        createdFiles.setEditable(false);
        createdFiles.setRows(5);
        createdFiles.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 0);
        add(createdFiles, gridBagConstraints);

        modifiedFilesTxt.setLabelFor(modifiedFiles);
        org.openide.awt.Mnemonics.setLocalizedText(modifiedFilesTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_ModifiedFiles")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(modifiedFilesTxt, gridBagConstraints);

        modifiedFiles.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        modifiedFiles.setColumns(20);
        modifiedFiles.setEditable(false);
        modifiedFiles.setRows(5);
        modifiedFiles.setToolTipText("modifiedFilesValue");
        modifiedFiles.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        add(modifiedFiles, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void iconButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iconButtonActionPerformed
        JFileChooser chooser = WizardUtils.getIconFileChooser(icon.getText());
        int ret = chooser.showDialog(this, getMessage("LBL_Select"));
        if (ret == JFileChooser.APPROVE_OPTION) {
            File iconFile =  FileUtil.normalizeFile(chooser.getSelectedFile());
                icon.setText(iconFile.getAbsolutePath());
                Set<File> allFiles = getPossibleIcons(getIconPath());
                if (!allFiles.remove(iconFile)) {
                    return; // #186459: user somehow selected a directory
                }
                boolean isIconSmall = WizardUtils.isValidIcon(iconFile, 16, 16);
 
                File secondIcon = null;
                boolean isSecondIconSmall = false;
                for (Iterator<File> it = allFiles.iterator(); it.hasNext() && !isSecondIconSmall;) {
                    File f = it.next();
                    isSecondIconSmall = (isIconSmall) ? 
                        WizardUtils.isValidIcon(f, 24, 24) : WizardUtils.isValidIcon(f, 16, 16);
                    if (isSecondIconSmall) {
                        secondIcon = f;
                        break;
                    }
                }
                
                if (secondIcon != null) {
                    smallIconPath = (isIconSmall) ? iconFile : secondIcon;
                    largeIconPath = (isIconSmall) ? secondIcon : iconFile;
                } else {
                    smallIconPath = iconFile;
                    largeIconPath = null;
                }
                
            updateData();
        }
    }//GEN-LAST:event_iconButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField className;
    private javax.swing.JLabel classNameTxt;
    private javax.swing.JTextArea createdFiles;
    private javax.swing.JLabel createdFilesTxt;
    private javax.swing.JTextField displayName;
    private javax.swing.JLabel displayNameTxt;
    private javax.swing.JTextField icon;
    private javax.swing.JButton iconButton;
    private javax.swing.JLabel iconTxt;
    private javax.swing.JTextArea modifiedFiles;
    private javax.swing.JLabel modifiedFilesTxt;
    private javax.swing.JComboBox packageName;
    private javax.swing.JLabel packageNameTxt;
    private javax.swing.JTextField project;
    private javax.swing.JLabel projectTxt;
    // End of variables declaration//GEN-END:variables
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(getMessage("ACS_NameAndLocationPanel"));
        className.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_ClassName"));
        createdFiles.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_CreatedFiles"));
        displayName.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_DisplayName"));
        icon.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_Icon"));
        iconButton.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_IconButton"));
        modifiedFiles.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_ModifiedFiles"));
        packageName.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_PackageName"));
        project.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_Project"));
    }
    
}
