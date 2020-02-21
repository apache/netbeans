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
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 * NewCndFileChooserPanelGUI is SimpleTargetChooserPanelGUI extended with extension selector and logic
 * 
 */
final class NewCndClassPanelGUI extends CndPanelGUI implements ActionListener{
    enum Kind {
        C,
        CPP,
        Class
    }
  
    private String sourceExt;
    private String headerExt;
    private final MIMEExtensions sourceExtensions;
    private final MIMEExtensions headerExtensions = MIMEExtensions.get(MIMENames.HEADER_MIME_TYPE);
    private final Kind kind;

    /** Creates new form NewCndFileChooserPanelGUI */
    NewCndClassPanelGUI( Project project, SourceGroup[] folders, Component bottomPanel, Kind kind) {
        super(project, folders);
        this.kind = kind;
        if (kind == Kind.Class || kind == Kind.CPP) {
            sourceExtensions = MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE);
        } else {
            sourceExtensions = MIMEExtensions.get(MIMENames.C_MIME_TYPE);
        }
        initComponents();
        initMnemonics();
        if (kind == Kind.C || kind == Kind.CPP) {
            org.openide.awt.Mnemonics.setLocalizedText(classNameLbl, org.openide.util.NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_TargetChooser_FileHeaderName_Label")); // NOI18N
        }
        
        locationComboBox.setRenderer( CELL_RENDERER );
        
        if ( bottomPanel != null ) {
            bottomPanelContainer.add( bottomPanel, java.awt.BorderLayout.CENTER );
        }
        initValues( null, null, null );
        
        browseButton.addActionListener( NewCndClassPanelGUI.this );
        headerBrowseButton.addActionListener( NewCndClassPanelGUI.this );
        locationComboBox.addActionListener( NewCndClassPanelGUI.this );
        classNameTextField.getDocument().addDocumentListener( NewCndClassPanelGUI.this );
        folderTextField.getDocument().addDocumentListener( NewCndClassPanelGUI.this );
        headerFolderTextField.getDocument().addDocumentListener( NewCndClassPanelGUI.this );
        
        setName (NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_SimpleTargetChooserPanel_Name")); // NOI18N
    }
    
    @Override
    public void initValues( FileObject template, FileObject preselectedFolder, String documentName ) {
        assert project != null;
        
        projectTextField.setText(getProjectDisplayName(project));

        Sources sources = ProjectUtils.getSources( project );
                        
        folders = sources.getSourceGroups( Sources.TYPE_GENERIC );
        
        if ( folders.length < 2 ) {
            // one source group i.e. hide Location
            locationLabel.setVisible( false );
            locationComboBox.setVisible( false );
        }
        else {
            // more source groups user needs to select location
            locationLabel.setVisible( true );
            locationComboBox.setVisible( true );
            
        }
        
        locationComboBox.setModel( new DefaultComboBoxModel( folders ) );
        // Guess the group we want to create the file in
        SourceGroup preselectedGroup = getPreselectedGroup( folders, preselectedFolder );        
        locationComboBox.setSelectedItem( preselectedGroup );               
        // Create OS dependent relative name
        String relPreselectedFolder = getRelativeNativeName(preselectedGroup.getRootFolder(), preselectedFolder);
        folderTextField.setText( relPreselectedFolder);
        headerFolderTextField.setText( relPreselectedFolder);
        
        String displayName = null;
        try {
            if (template != null) {
                DataObject templateDo = DataObject.find (template);
                displayName = templateDo.getNodeDelegate ().getDisplayName ();
            }
        } catch (DataObjectNotFoundException ex) {
            displayName = template.getName ();
        }
        putClientProperty ("NewFileWizard_Title", displayName);// NOI18N        
        
        
        sourceExt = sourceExtensions.getDefaultExtension();
        cbSourceExtension.setSelectedItem(sourceExt);

        headerExt = headerExtensions.getDefaultExtension();
        cbHeaderExtension.setSelectedItem(headerExt);
        
        if (template != null) {
            if (documentName == null) {
                if (kind == Kind.Class) {
                     documentName = getMessage("NewClassSuggestedName");
                } else {
                     documentName = getMessage("NewFileSuggestedName");
                }
                FileObject currentFolder = preselectedFolder != null ? preselectedFolder : getTargetGroup().getRootFolder();
                if (currentFolder != null) {
                    documentName += generateUniqueSuffix(
                            currentFolder, getFileName(documentName),
                            sourceExt, headerExt);
                }
                
            }
            classNameTextField.setText (documentName);
            classNameTextField.selectAll ();
        }

    }
    
    @Override
    public SourceGroup getTargetGroup() {
        Object selectedItem = locationComboBox.getSelectedItem();
        if (selectedItem == null) {
            // workaround for MacOS, see IZ 175457
            selectedItem = locationComboBox.getItemAt(locationComboBox.getSelectedIndex());
            if (selectedItem == null) {
                selectedItem = locationComboBox.getItemAt(0);
            }
        }
        return (SourceGroup) selectedItem;
    }
        
    @Override
    public String getTargetFolder() {
        
        String folderName = folderTextField.getText().trim();
        
        if ( folderName.length() == 0 ) {
            return "";
        } 
        else {           
            return folderName.replace( fileSeparatorChar, '/' ); // NOI18N
        }
    }
    
    @Override
    public String getTargetName() {
        String documentName = getSourceFileName();
        
        if ( documentName.length() == 0 || documentName.charAt(documentName.length() - 1) == '.') {
            return null;
        } else {
            return documentName;
        }
    }

    private String createdFileName(JTextField field){
        FileObject root = getTargetGroup().getRootFolder();
        String folderName = field.getText().trim();
        String createdFileName = root.getPath() +
            ( folderName.startsWith("/") || folderName.startsWith( fileSeparator ) ? "" : "/" ) + // NOI18N
            folderName +
            ( folderName.endsWith("/") || folderName.endsWith( fileSeparator ) || folderName.length() == 0 ? "" : "/" );  // NOI18N
        return createdFileName.replace( '/', fileSeparatorChar );
    }

    @Override
    protected void updateCreatedFile() {
        String sourceFileName = createdFileName(folderTextField) + getSourceFileName();
        String headerFileName = createdFileName(headerFolderTextField) + getHeaderFileName();

        if (!sourceFileName.equals(fileTextField.getText()) || !headerFileName.equals(headerTextField.getText())) {
            fileTextField.setText( sourceFileName );
            headerTextField.setText( headerFileName );
            changeSupport.fireChange();
        }
    }
    
    public String getSourceFileName() {
        return getFileName(getClassName()) + "." + sourceExt; // NOI18N
    }

    private DefaultComboBoxModel getSourceExtensionsModel() {
        return new DefaultComboBoxModel(new Vector<String>(sourceExtensions.getValues()));
    }

    public String getHeaderFileName() {
        return getFileName(getClassName()) + "." + headerExt; // NOI18N
    }

    public String getHeaderFolder() {
        String folderName = headerFolderTextField.getText().trim();
        if ( folderName.length() == 0 ) {
            return "";
        } else {
            return folderName.replace( fileSeparatorChar, '/' ); // NOI18N
        }
    }

    public String getHeaderName() {
        String documentName = getHeaderFileName();
        if ( documentName.length() == 0 || documentName.charAt(documentName.length() - 1) == '.') {
            return null;
        } else {
            return documentName;
        }
    }

    private DefaultComboBoxModel getHeaderExtensionsModel() {
        return new DefaultComboBoxModel(new Vector<String>(headerExtensions.getValues()));
    }
    
    private static String getFileName(String className) {
        return className;
    }

    public String getClassName() {
        return classNameTextField.getText().trim();
    }

    public String getHeaderExt() {
        return headerExt;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationComboBox = new javax.swing.JComboBox();
        targetSeparator = new javax.swing.JSeparator();
        bottomPanelContainer = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        folderTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        cbSourceExtension = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        headerFolderTextField = new javax.swing.JTextField();
        headerBrowseButton = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        cbHeaderExtension = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        headerTextField = new javax.swing.JTextField();
        classNameLbl = new javax.swing.JLabel();
        classNameTextField = new javax.swing.JTextField();

        jLabel1.setLabelFor(projectTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_TargetChooser_Project_Label")); // NOI18N

        projectTextField.setEditable(false);
        projectTextField.setFocusable(false);

        locationLabel.setLabelFor(locationComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_TargetChooser_Location_Label")); // NOI18N

        bottomPanelContainer.setFocusable(false);
        bottomPanelContainer.setMinimumSize(new java.awt.Dimension(0, 10));
        bottomPanelContainer.setPreferredSize(new java.awt.Dimension(0, 10));
        bottomPanelContainer.setLayout(new java.awt.BorderLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_Sorce_File_Section"))); // NOI18N

        jLabel2.setLabelFor(folderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_TargetChooser_Folder_Label")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_TargetChooser_Browse_Button")); // NOI18N

        jLabel5.setLabelFor(cbSourceExtension);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_TargetChooser_Extension_Label")); // NOI18N

        cbSourceExtension.setModel(getSourceExtensionsModel());
        cbSourceExtension.setMinimumSize(new java.awt.Dimension(100, 25));
        cbSourceExtension.setPreferredSize(new java.awt.Dimension(100, 25));
        cbSourceExtension.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbSourceExtensionActionPerformed(evt);
            }
        });

        jLabel4.setLabelFor(fileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_TargetChooser_CreatedFile_Label")); // NOI18N

        fileTextField.setEditable(false);
        fileTextField.setFocusable(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(folderTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton))
                    .addComponent(cbSourceExtension, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(folderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton)
                    .addComponent(jLabel2))
                .addGap(7, 7, 7)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbSourceExtension, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(6, 6, 6)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        folderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewCndClassPanelGUI.class).getString("AD_folderTextField")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleName("");
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewCndClassPanelGUI.class).getString("AD_browseButton")); // NOI18N
        fileTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewCndClassPanelGUI.class).getString("AD_fileTextField")); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_Heder_File_Section"))); // NOI18N

        jLabel8.setLabelFor(headerFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_HeaderChooser_Folder_Label")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(headerBrowseButton, org.openide.util.NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_HeaderChooser_Browse_Button")); // NOI18N

        jLabel7.setLabelFor(cbHeaderExtension);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_HeaderChooser_Extension_Label")); // NOI18N

        cbHeaderExtension.setModel(getHeaderExtensionsModel());
        cbHeaderExtension.setMinimumSize(new java.awt.Dimension(100, 25));
        cbHeaderExtension.setPreferredSize(new java.awt.Dimension(100, 25));
        cbHeaderExtension.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbHeaderExtensionActionPerformed(evt);
            }
        });

        jLabel6.setLabelFor(headerTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_TargetChooser_HeaderFile_Label")); // NOI18N

        headerTextField.setEditable(false);
        headerTextField.setFocusable(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addComponent(headerTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(28, 28, 28))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(51, 51, 51)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addComponent(headerFolderTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(headerBrowseButton))
                            .addComponent(cbHeaderExtension, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(headerBrowseButton)
                    .addComponent(headerFolderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(cbHeaderExtension, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(headerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        classNameLbl.setLabelFor(classNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(classNameLbl, org.openide.util.NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_TargetChooser_FileName_Label")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(classNameLbl)
                            .addComponent(locationLabel)
                            .addComponent(jLabel1))
                        .addGap(24, 24, 24)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(locationComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 315, Short.MAX_VALUE)
                            .addComponent(classNameTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                            .addComponent(projectTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)))
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bottomPanelContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                    .addComponent(targetSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(classNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(classNameLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(locationLabel))
                .addGap(4, 4, 4)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(targetSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bottomPanelContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                .addContainerGap())
        );

        projectTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewCndClassPanelGUI.class).getString("AD_projectTextField")); // NOI18N
        locationComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewCndClassPanelGUI.class).getString("AD_locationComboBox")); // NOI18N
        classNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewCndClassPanelGUI.class).getString("AD_documentNameTextField")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewCndClassPanelGUI.class).getString("AD_SimpleTargetChooserPanelGUI_1")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void cbSourceExtensionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbSourceExtensionActionPerformed
        sourceExt = (String)cbSourceExtension.getSelectedItem();
        updateCreatedFile();
}//GEN-LAST:event_cbSourceExtensionActionPerformed

    private void cbHeaderExtensionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbHeaderExtensionActionPerformed
        headerExt = (String)cbHeaderExtension.getSelectedItem();
        updateCreatedFile();
}//GEN-LAST:event_cbHeaderExtensionActionPerformed

    private void initMnemonics() {
    }
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanelContainer;
    private javax.swing.JButton browseButton;
    private javax.swing.JComboBox cbHeaderExtension;
    private javax.swing.JComboBox cbSourceExtension;
    private javax.swing.JLabel classNameLbl;
    private javax.swing.JTextField classNameTextField;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JTextField folderTextField;
    private javax.swing.JButton headerBrowseButton;
    private javax.swing.JTextField headerFolderTextField;
    private javax.swing.JTextField headerTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JComboBox locationComboBox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JSeparator targetSeparator;
    // End of variables declaration//GEN-END:variables

    // ActionListener implementation -------------------------------------------
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if ( browseButton == e.getSource() ) {
            // Show the browse dialog             
            SourceGroup group = getTargetGroup();
            FileObject fo = BrowseFolders.showDialog( new SourceGroup[] { group },
                                           project, 
                                           folderTextField.getText().replace( fileSeparatorChar, '/' ) ); // NOI18N
                        
            if ( fo != null && fo.isFolder() ) {
                String relPath = FileUtil.getRelativePath( group.getRootFolder(), fo );
                folderTextField.setText( relPath.replace( '/', fileSeparatorChar ) ); // NOI18N
            }                        
        } else if ( headerBrowseButton == e.getSource() ) {
            SourceGroup group = getTargetGroup();
            FileObject fo = BrowseFolders.showDialog( new SourceGroup[] { group },
                                           project,
                                           headerFolderTextField.getText().replace( fileSeparatorChar, '/' ) ); // NOI18N

            if ( fo != null && fo.isFolder() ) {
                String relPath = FileUtil.getRelativePath( group.getRootFolder(), fo );
                headerFolderTextField.setText( relPath.replace( '/', fileSeparatorChar ) ); // NOI18N
            }
        } else if ( locationComboBox == e.getSource() )  {
            updateCreatedFile();
        } 
    }    
    
}
