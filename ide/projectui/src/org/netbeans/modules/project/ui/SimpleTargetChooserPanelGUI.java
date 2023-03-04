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

package org.netbeans.modules.project.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ChangeSupport;
import static org.netbeans.modules.project.ui.Bundle.*;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author  phrebejk
 */
public class SimpleTargetChooserPanelGUI extends javax.swing.JPanel implements ActionListener, DocumentListener {
  
    /** preferred dimension of the panels */
    private static final Dimension PREF_DIM = new Dimension(500, 340);
    
    private final ListCellRenderer CELL_RENDERER = new GroupCellRenderer();

    @NullAllowed
    private Project project;
    private String expectedExtension;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    @NonNull
    private final SourceGroup[] folders;
    private boolean isFolder;
    private boolean freeFileExtension;
    private final char separatorChar;

    @SuppressWarnings("LeakingThisInConstructor")
    @Messages("LBL_SimpleTargetChooserPanel_Name=Name and Location")
    public SimpleTargetChooserPanelGUI( @NullAllowed Project project, @NonNull SourceGroup[] folders, Component bottomPanel, boolean isFolder, boolean freeFileExtension) {
        this.project = project;
        this.folders = folders.clone();
        this.isFolder = isFolder;
        this.freeFileExtension = freeFileExtension;
        this.separatorChar = getPathNameSeparator(project, folders);
        initComponents();
        
        locationComboBox.setRenderer( CELL_RENDERER );
        
        if ( bottomPanel != null ) {
            bottomPanelContainer.add( bottomPanel, java.awt.BorderLayout.CENTER );
        }
        initValues( null, null, null , true);

        setPreferredSize(PREF_DIM);

        browseButton.addActionListener( this );
        locationComboBox.addActionListener( this );
        documentNameTextField.getDocument().addDocumentListener( this );
        folderTextField.getDocument().addDocumentListener( this );
        
        setName(LBL_SimpleTargetChooserPanel_Name());
    }

    private static char getPathNameSeparator(@NullAllowed Project project, @NonNull SourceGroup[] folders) {
        FileObject fo = null;
        if (folders != null && folders.length > 0) {
            fo = folders[0].getRootFolder();
        } else if (project != null) {
            fo = project.getProjectDirectory();
        }
        String separator = null;
        if (fo != null) {
            separator = (String) fo.getAttribute(FileObject.DEFAULT_PATHNAME_SEPARATOR_ATTR);
        }
        return (separator == null || separator.isEmpty()) ? File.separatorChar : separator.charAt(0);
    }
    
    @Messages({
        "# 0 - original template name",
        "LBL_SimpleTargetChooserPanelGUI_NewFileName=new{0}",
        "LBL_TargetChooser_FolderName_Label=Folder &Name:",
        "LBL_TargetChooser_ParentFolder_Label=Pa&rent Folder:",
        "LBL_TargetChooser_CreatedFolder_Label=&Created Folder:",
        "LBL_TargetChooser_FileName_Label=File &Name:",
        "LBL_TargetChooser_Folder_Label=Fo&lder:",
        "LBL_TargetChooser_CreatedFile_Label=&Created File:",
        "# sample folder name", "LBL_folder_name=folder",
        "LBL_TargetChooser_NoProject=None"
    })
    final void initValues(FileObject template, @NullAllowed FileObject preselectedFolder, String documentName, boolean includesTemplatesWithProject) {
        //TODO the number of possible code paths is overwhelming here. needs rewrite to cater for all in a sane way..
        if (project != null) {
            projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());
        } else {
            projectTextField.setText(LBL_TargetChooser_NoProject());
        }

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
        // Create OS dependent relative name
        if (preselectedGroup != null) {
            locationComboBox.setSelectedItem( preselectedGroup );
            FileObject rootFolder = preselectedGroup.getRootFolder();
            if (rootFolder == null) {
                throw new NullPointerException("#173645: null returned illegally from " + preselectedGroup.getClass().getName() + ".getRootFolder()");
            }
            folderTextField.setText(getRelativeNativeName(rootFolder, preselectedFolder));
        }
        else if (project == null && preselectedFolder != null) {
            folderTextField.setText(preselectedFolder.getPath().replace('/', this.separatorChar));
        }

        String ext = template == null ? "" : template.getExt(); // NOI18N
        expectedExtension = ext.length() == 0 ? "" : "." + ext; // NOI18N
        
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
        if (template != null) {
            final String baseName;
            if (isFolder) {
                baseName = LBL_folder_name();
            } else {
                Object targetName = template.getAttribute("targetName");//NOI18N
                baseName = targetName instanceof String
                        ? (String) targetName
                        : LBL_SimpleTargetChooserPanelGUI_NewFileName(template.getName());
            }
            if (documentName == null) {
                documentName = baseName;
            }
            if (preselectedFolder != null) {
                String documentNameBase = documentName;
                int index = 0;
                while (true) {
                    FileObject _tmp = preselectedFolder.getFileObject(documentName, template.getExt());
                    if (_tmp == null) {
                        break;
                    }
                    documentName = documentNameBase + ++index;
                }
            }
                
            documentNameTextField.setText (documentName);
            documentNameTextField.selectAll ();
        }
        
        if (isFolder) {
            Mnemonics.setLocalizedText(jLabel3, LBL_TargetChooser_FolderName_Label());
            Mnemonics.setLocalizedText(jLabel2, LBL_TargetChooser_ParentFolder_Label());
            Mnemonics.setLocalizedText(jLabel4, LBL_TargetChooser_CreatedFolder_Label());
        } else {
            Mnemonics.setLocalizedText(jLabel3, LBL_TargetChooser_FileName_Label());
            Mnemonics.setLocalizedText(jLabel2, LBL_TargetChooser_Folder_Label());
            Mnemonics.setLocalizedText(jLabel4, LBL_TargetChooser_CreatedFile_Label());
        }
    }
    
    public SourceGroup getTargetGroup() {
        return (SourceGroup)locationComboBox.getSelectedItem();
    }

    @CheckForNull
    public String getTargetFolder() {
        
        String folderName = folderTextField.getText().trim();
        
        if ( folderName.isEmpty() ) {
            //TODO not the right place for default value in non-project space
//            if (project == null) {
//                String home = System.getProperty("user.home");
//                if (home != null && new File(home).isDirectory()) {
//                    return home;
//                }
//            }

            return null;
        }
        else {           
            //TODO not the right place for default value in non-project space
//            if (project == null && !new File(folderName).isAbsolute()) {
//                String home = System.getProperty("user.home");
//                if (home != null && new File(home).isDirectory()) {
//                    FileObject homeFileObject = FileUtil.toFileObject(FileUtil.normalizeFile(new File(home)));
//                    folderName = FileUtil.getFileDisplayName(homeFileObject) + this.separatorChar + folderName;
//                }
//            }
//
            return folderName.replace( this.separatorChar, '/' ); // NOI18N
        }
    }
    
    public String getTargetName() {
        
        String text = documentNameTextField.getText().trim();
        
        if ( text.length() == 0 ) {
            return null;
        }
        else {
            return text;
        }
    }
    
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
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
        jLabel3 = new javax.swing.JLabel();
        documentNameTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        folderTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        targetSeparator = new javax.swing.JSeparator();
        bottomPanelContainer = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel3.setLabelFor(documentNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "LBL_TargetChooser_FileName_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(jLabel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(documentNameTextField, gridBagConstraints);
        documentNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(SimpleTargetChooserPanelGUI.class).getString("AD_documentNameTextField")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 24, 0);
        add(jPanel1, gridBagConstraints);

        jLabel1.setLabelFor(projectTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "LBL_TargetChooser_Project_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(jLabel1, gridBagConstraints);

        projectTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(projectTextField, gridBagConstraints);
        projectTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(SimpleTargetChooserPanelGUI.class).getString("AD_projectTextField")); // NOI18N

        locationLabel.setLabelFor(locationComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "LBL_TargetChooser_Location_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(locationLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        add(locationComboBox, gridBagConstraints);
        locationComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(SimpleTargetChooserPanelGUI.class).getString("AD_locationComboBox")); // NOI18N

        jLabel2.setLabelFor(folderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "LBL_TargetChooser_Folder_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(folderTextField, gridBagConstraints);
        folderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(SimpleTargetChooserPanelGUI.class).getString("AD_folderTextField")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "LBL_TargetChooser_Browse_Button")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(browseButton, gridBagConstraints);
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(SimpleTargetChooserPanelGUI.class).getString("AD_browseButton")); // NOI18N

        jLabel4.setLabelFor(fileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "LBL_TargetChooser_CreatedFile_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabel4, gridBagConstraints);

        fileTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(fileTextField, gridBagConstraints);
        fileTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(SimpleTargetChooserPanelGUI.class).getString("AD_fileTextField")); // NOI18N

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

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(SimpleTargetChooserPanelGUI.class).getString("AD_SimpleTargetChooserPanelGUI")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanelContainer;
    private javax.swing.JButton browseButton;
    private javax.swing.JTextField documentNameTextField;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JTextField folderTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox locationComboBox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JSeparator targetSeparator;
    // End of variables declaration//GEN-END:variables

    @CheckForNull
    private SourceGroup getPreselectedGroup( SourceGroup[] groups, FileObject folder ) {        
        for( int i = 0; folder != null && i < groups.length; i++ ) {
            if( FileUtil.isParentOf( groups[i].getRootFolder(), folder )
                || groups[i].getRootFolder().equals(folder)) {
                return groups[i];
            }
        }
        if (groups.length > 0) {
            return groups[0];
        }
        return null;
    }
    
    private String getRelativeNativeName( FileObject root, FileObject folder ) {
        assert root != null;
        String path;
        
        if (folder == null) {
            path = ""; // NOI18N
        }
        else {
            path = FileUtil.getRelativePath( root, folder );            
        }
        
        return path == null ? "" : path.replace( '/', this.separatorChar ); // NOI18N
    }
    
    private void updateCreatedFolder() {
        
        SourceGroup sg = (SourceGroup)locationComboBox.getSelectedItem();
        FileObject root = sg != null ? sg.getRootFolder() : null;
        String documentName = documentNameTextField.getText().trim();
        String folderName = getTargetFolder();
        if (folderName == null) {
            folderName = "";
        }
        
        String createdFileName = (root != null ? FileUtil.getFileDisplayName( root ) : "") +
            ( root == null || folderName.startsWith("/") || folderName.startsWith( File.separator ) ? "" : "/" ) + // NOI18N
            folderName + 
            ( folderName.endsWith("/") || folderName.endsWith( File.separator ) || folderName.length() == 0 ? "" : "/" ) + // NOI18N
            documentName + (!freeFileExtension || documentName.indexOf('.') == -1 ? expectedExtension : "");
            
        fileTextField.setText( createdFileName.replace( '/', this.separatorChar ) ); // NOI18N
            
        changeSupport.fireChange();
    }
    
   
    // ActionListener implementation -------------------------------------------
    
    public @Override void actionPerformed(ActionEvent e) {
        if ( browseButton == e.getSource() ) {
            if (project != null) {
                FileObject fo;
                // Show the browse dialog

                SourceGroup group = (SourceGroup)locationComboBox.getSelectedItem();
                if (group == null) { // #161478
                    return;
                }

                fo = BrowseFolders.showDialog( new SourceGroup[] { group },
                                               project,
                                               folderTextField.getText().replace( this.separatorChar, '/' ) ); // NOI18N

                if ( fo != null && fo.isFolder() ) {
                    String relPath = FileUtil.getRelativePath( group.getRootFolder(), fo );
                    folderTextField.setText( relPath.replace( '/', this.separatorChar ) ); // NOI18N
                }
            }
            else {
                //non project space
                String previousTargetFolder = getTargetFolder(); //can be relative or absolute..
                SourceGroup group = (SourceGroup)locationComboBox.getSelectedItem();
                FileObject oldFo = null;
                if (group != null) {
                    oldFo = previousTargetFolder != null ? group.getRootFolder().getFileObject(previousTargetFolder) : group.getRootFolder();
                }
                if (oldFo == null && previousTargetFolder != null) {
                    oldFo = FileUtil.toFileObject(FileUtil.normalizeFile(new File(previousTargetFolder)));
                }
                File currFile = oldFo != null ? FileUtil.toFile(oldFo) : FileUtil.normalizeFile(new File("."));
                
                File targetFolder =
                    new FileChooserBuilder(SimpleTargetChooserPanel.class)
                        .setDirectoriesOnly(true)
                        .setDefaultWorkingDirectory(currFile)
                        .forceUseOfDefaultWorkingDirectory(group != null) //if no source group, allow other directories
                        .showSaveDialog();

                FileObject fo = targetFolder != null ? FileUtil.toFileObject(FileUtil.normalizeFile(targetFolder)) : null;

                if ( fo != null && fo.isFolder() ) {
                    String path =  group == null ? null : FileUtil.getRelativePath(group.getRootFolder(), fo);
                    if (path == null) {
                         path = fo.getPath();
                    }
                    folderTextField.setText( path.replace( '/', this.separatorChar ) ); // NOI18N
                }
            }
        }
        else if ( locationComboBox == e.getSource() )  {
            updateCreatedFolder();
        }
    }    
    
    // DocumentListener implementation -----------------------------------------
    
    public @Override void changedUpdate(DocumentEvent e) {
        updateCreatedFolder();
    }    
    
    public @Override void insertUpdate(DocumentEvent e) {
        updateCreatedFolder();
    }
    
    public @Override void removeUpdate(DocumentEvent e) {
        updateCreatedFolder();
    }
    
    
    // Rendering of the location combo box -------------------------------------
    
    private class GroupCellRenderer extends JLabel implements ListCellRenderer {
    
        public GroupCellRenderer() {
            setOpaque( true );
        }
        
        public @Override Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof SourceGroup) {
                SourceGroup group = (SourceGroup)value;
                String groupDisplayName = group.getDisplayName();
                String projectDisplayName = project != null ? ProjectUtils.getInformation( project ).getDisplayName() : groupDisplayName;
                if ( projectDisplayName.equals( groupDisplayName ) ) {
                    setText( groupDisplayName );
                }
                else {
                    setText(FMT_PhysicalView_GroupName(
                            groupDisplayName, projectDisplayName, group.getRootFolder().getName()));
                }
                
                setIcon( group.getIcon( false ) );
            } 
            else {
                setText( value.toString () );
                setIcon( null );
            }
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
             
            }
            return this;        
        }
                
    }
}
