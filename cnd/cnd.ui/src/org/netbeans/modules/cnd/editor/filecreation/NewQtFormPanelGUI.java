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
package org.netbeans.modules.cnd.editor.filecreation;

import java.awt.Component;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.cnd.editor.filecreation.NewQtFormPanel.FormType;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 */
/*package*/ class NewQtFormPanelGUI extends CndPanelGUI implements ActionListener {

    private static final String FORM_EXT = "ui"; // NOI18N
    private String sourceExt;
    private String headerExt;
    private final FormType[] formTypes;

    /*package*/ NewQtFormPanelGUI(Project project, SourceGroup[] folders, Component bottomPanel, FormType[] formTypes) {
        super(project, folders);
        this.formTypes = formTypes;

        initComponents();

        if (bottomPanel != null) {
            bottomPanelContainer.add(bottomPanel, java.awt.BorderLayout.CENTER);
        }
        initValues(null, null, null);

        /* handled by parent class */
        tfFormName.getDocument().addDocumentListener(this);
        tfFolder.getDocument().addDocumentListener(this);

        /* handled by this class */
        cbCreateClass.addActionListener(this);
        cbLocation.addActionListener(this);
        browseButton.addActionListener(this);
    }

    @Override
    public void initValues(FileObject template, FileObject preselectedFolder, String formName) {

        tfProject.setText(ProjectUtils.getInformation(project).getDisplayName());

        Sources sources = ProjectUtils.getSources(project);
        folders = sources.getSourceGroups(Sources.TYPE_GENERIC);

        if (folders.length < 2) {
            // one source group i.e. hide Location
            lbLocation.setVisible(false);
            cbLocation.setVisible(false);
        } else {
            // more source groups user needs to select location
            lbLocation.setVisible(true);
            cbLocation.setVisible(true);

        }

        cbLocation.setModel(new DefaultComboBoxModel(folders));
        // Guess the group we want to create the file in
        SourceGroup preselectedGroup = getPreselectedGroup(folders, preselectedFolder);
        cbLocation.setSelectedItem(preselectedGroup);
        // Create OS dependent relative name
        tfFolder.setText(getRelativeNativeName(preselectedGroup.getRootFolder(), preselectedFolder));

        String displayName = null;
        try {
            if (template != null) {
                DataObject templateDo = DataObject.find(template);
                displayName = templateDo.getNodeDelegate().getDisplayName();
            }
        } catch (DataObjectNotFoundException ex) {
            displayName = template.getName();
        }
        putClientProperty("NewFileWizard_Title", displayName);// NOI18N        

        sourceExt = MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE).getDefaultExtension();
        headerExt = MIMEExtensions.get(MIMENames.HEADER_MIME_TYPE).getDefaultExtension();

        if (template != null) {
            if (formName == null) {
                formName = getMessage("NewFormSuggestedName");
                FileObject currentFolder = preselectedFolder != null ? preselectedFolder : getTargetGroup().getRootFolder();
                if (currentFolder != null) {
                    formName += generateUniqueSuffix(
                            currentFolder, getFileName(formName),
                            FORM_EXT, sourceExt, headerExt);
                }
            }
            tfFormName.setText(formName);
            tfFormName.selectAll();
        }

    }

    @Override
    public SourceGroup getTargetGroup() {
        Object selectedItem = cbLocation.getSelectedItem();
        if (selectedItem == null) {
            // workaround for MacOS, see IZ 175457
            selectedItem = cbLocation.getItemAt(cbLocation.getSelectedIndex());
            if (selectedItem == null) {
                selectedItem = cbLocation.getItemAt(0);
            }
        }
        return (SourceGroup) selectedItem;
    }

    @Override
    public String getTargetFolder() {
        String folderName = tfFolder.getText().trim();
        return folderName.replace(fileSeparatorChar, '/'); // NOI18N
    }

    @Override
    public String getTargetName() {
        String formName = getFileName(getFormName());
        if (formName.length() == 0 || formName.charAt(formName.length() - 1) == '.') {
            return null;
        } else {
            return formName;
        }
    }

    @Override
    protected void updateCreatedFile() {
        FileObject root = getTargetGroup().getRootFolder();
        String folderName = tfFolder.getText().trim();
        String folderDisplayName = root.getPath() +
                (folderName.startsWith("/") || folderName.startsWith(fileSeparator) ? "" : "/") + // NOI18N
                folderName +
                (folderName.endsWith("/") || folderName.endsWith(fileSeparator) || folderName.length() == 0 ? "" : "/");  // NOI18N
        folderDisplayName = folderDisplayName.replace('/', fileSeparatorChar);

        String formFileName = folderDisplayName + getFormFileName();

        tfFormFile.setText(formFileName);
        if (cbCreateClass.isSelected()) {
            String sourceFileName = folderDisplayName + getSourceFileName();
            tfClassFile.setText(sourceFileName);

            String headerFileName = folderDisplayName + getHeaderFileName();
            tfHeaderFile.setText(headerFileName);
        } else {
            tfClassFile.setText(""); // NOI18N
            tfHeaderFile.setText(""); // NOI18N
        }

        tfClassFile.setEnabled(cbCreateClass.isSelected());
        tfHeaderFile.setEnabled(cbCreateClass.isSelected());

        changeSupport.fireChange();
    }

    public String getFormFileName() {
        return getFileName(getFormName()) + "." + FORM_EXT; // NOI18N
    }

    public String getSourceFileName() {
        return cbCreateClass.isSelected()? getFileName(getFormName()) + "." + sourceExt : null; // NOI18N
    }

    public String getHeaderFileName() {
        return cbCreateClass.isSelected()? getFileName(getFormName()) + "." + headerExt : null; // NOI18N
    }

    public FormType getFormType() {
        return (FormType)cbFormType.getSelectedItem();
    }

    private static String getFileName(String className) {
        return className;
    }

    public String getFormName() {
        return tfFormName.getText().trim();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        lbFormName = new javax.swing.JLabel();
        tfFormName = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        cbFormType = new javax.swing.JComboBox();
        cbCreateClass = new javax.swing.JCheckBox();
        lbProject = new javax.swing.JLabel();
        tfProject = new javax.swing.JTextField();
        lbLocation = new javax.swing.JLabel();
        cbLocation = new javax.swing.JComboBox();
        lbFolder = new javax.swing.JLabel();
        tfFolder = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        lbFormFile = new javax.swing.JLabel();
        tfFormFile = new javax.swing.JTextField();
        lbClassFile = new javax.swing.JLabel();
        tfClassFile = new javax.swing.JTextField();
        lbHeaderFile = new javax.swing.JLabel();
        tfHeaderFile = new javax.swing.JTextField();
        targetSeparator = new javax.swing.JSeparator();
        bottomPanelContainer = new javax.swing.JPanel();

        setName(org.openide.util.NbBundle.getMessage(NewQtFormPanelGUI.class, "LBL_SimpleTargetChooserPanel_Name")); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        lbFormName.setLabelFor(tfFormName);
        org.openide.awt.Mnemonics.setLocalizedText(lbFormName, org.openide.util.NbBundle.getMessage(NewQtFormPanelGUI.class, "LBL_TargetChooser_FormName_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(lbFormName, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(tfFormName, gridBagConstraints);
        tfFormName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewQtFormPanelGUI.class).getString("AD_documentNameTextField")); // NOI18N

        jLabel1.setLabelFor(cbFormType);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(NewQtFormPanelGUI.class, "LBL_TargetChooser_FormType_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(jLabel1, gridBagConstraints);

        cbFormType.setModel(new DefaultComboBoxModel(formTypes));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(cbFormType, gridBagConstraints);

        cbCreateClass.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cbCreateClass, org.openide.util.NbBundle.getMessage(NewQtFormPanelGUI.class, "LBL_TargetChooser_CreateWrapperClass")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(cbCreateClass, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 24, 0);
        add(jPanel1, gridBagConstraints);

        lbProject.setLabelFor(tfProject);
        org.openide.awt.Mnemonics.setLocalizedText(lbProject, org.openide.util.NbBundle.getMessage(NewQtFormPanelGUI.class, "LBL_TargetChooser_Project_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(lbProject, gridBagConstraints);

        tfProject.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(tfProject, gridBagConstraints);
        tfProject.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewQtFormPanelGUI.class).getString("AD_projectTextField")); // NOI18N

        lbLocation.setLabelFor(cbLocation);
        org.openide.awt.Mnemonics.setLocalizedText(lbLocation, org.openide.util.NbBundle.getMessage(NewQtFormPanelGUI.class, "LBL_TargetChooser_Location_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(lbLocation, gridBagConstraints);

        cbLocation.setRenderer(CELL_RENDERER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 4, 0);
        add(cbLocation, gridBagConstraints);
        cbLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewQtFormPanelGUI.class).getString("AD_locationComboBox")); // NOI18N

        lbFolder.setLabelFor(tfFolder);
        org.openide.awt.Mnemonics.setLocalizedText(lbFolder, org.openide.util.NbBundle.getMessage(NewQtFormPanelGUI.class, "LBL_TargetChooser_Folder_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(lbFolder, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(tfFolder, gridBagConstraints);
        tfFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewQtFormPanelGUI.class).getString("AD_folderTextField")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(NewQtFormPanelGUI.class, "LBL_TargetChooser_Browse_Button")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(browseButton, gridBagConstraints);
        browseButton.getAccessibleContext().setAccessibleName("");
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewQtFormPanelGUI.class).getString("AD_browseButton")); // NOI18N

        lbFormFile.setLabelFor(tfFormFile);
        org.openide.awt.Mnemonics.setLocalizedText(lbFormFile, org.openide.util.NbBundle.getMessage(NewQtFormPanelGUI.class, "LBL_TargetChooser_FormFile_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(lbFormFile, gridBagConstraints);

        tfFormFile.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(tfFormFile, gridBagConstraints);

        lbClassFile.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/editor/filecreation/Bundle").getString("LBL_TargetChooser_CreatedFile_Label_Mnemonic").charAt(0));
        lbClassFile.setLabelFor(tfClassFile);
        org.openide.awt.Mnemonics.setLocalizedText(lbClassFile, org.openide.util.NbBundle.getMessage(NewQtFormPanelGUI.class, "LBL_TargetChooser_ClassFile_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(lbClassFile, gridBagConstraints);

        tfClassFile.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(tfClassFile, gridBagConstraints);
        tfClassFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewQtFormPanelGUI.class).getString("AD_fileTextField")); // NOI18N

        lbHeaderFile.setLabelFor(tfHeaderFile);
        org.openide.awt.Mnemonics.setLocalizedText(lbHeaderFile, org.openide.util.NbBundle.getMessage(NewQtFormPanelGUI.class, "LBL_TargetChooser_HeaderFile_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(lbHeaderFile, gridBagConstraints);

        tfHeaderFile.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(tfHeaderFile, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(targetSeparator, gridBagConstraints);

        bottomPanelContainer.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(bottomPanelContainer, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewQtFormPanelGUI.class).getString("AD_SimpleTargetChooserPanelGUI_1")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanelContainer;
    private javax.swing.JButton browseButton;
    private javax.swing.JCheckBox cbCreateClass;
    private javax.swing.JComboBox cbFormType;
    private javax.swing.JComboBox cbLocation;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lbClassFile;
    private javax.swing.JLabel lbFolder;
    private javax.swing.JLabel lbFormFile;
    private javax.swing.JLabel lbFormName;
    private javax.swing.JLabel lbHeaderFile;
    private javax.swing.JLabel lbLocation;
    private javax.swing.JLabel lbProject;
    private javax.swing.JSeparator targetSeparator;
    private javax.swing.JTextField tfClassFile;
    private javax.swing.JTextField tfFolder;
    private javax.swing.JTextField tfFormFile;
    private javax.swing.JTextField tfFormName;
    private javax.swing.JTextField tfHeaderFile;
    private javax.swing.JTextField tfProject;
    // End of variables declaration//GEN-END:variables

    // ActionListener implementation -------------------------------------------
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (browseButton == e.getSource()) {
            FileObject fo = null;
            // Show the browse dialog             

            SourceGroup group = getTargetGroup();

            fo = BrowseFolders.showDialog(new SourceGroup[]{group},
                    project,
                    tfFolder.getText().replace(fileSeparatorChar, '/')); // NOI18N

            if (fo != null && fo.isFolder()) {
                String relPath = FileUtil.getRelativePath(group.getRootFolder(), fo);
                tfFolder.setText(relPath.replace('/', fileSeparatorChar)); // NOI18N
            }
        } else if (cbLocation == e.getSource()) {
            updateCreatedFile();
        } else if (cbCreateClass == e.getSource()) {
            updateCreatedFile();
        }
    }

}
