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

package org.netbeans.modules.target.iterator.api;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;


/**
 *
 * @author  ads
 */
public final class TargetChooserPanelGUI<T> extends JPanel 
    implements ActionListener, DocumentListener  
{
    private static final long serialVersionUID = -1977566644151991912L;
    /** Creates new form TargetChooserGUI */
    public TargetChooserPanelGUI(final TargetChooserPanel<T> wizardPanel) {
        myWizardPanel = wizardPanel;
        initComponents();
        
        getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_DESC_TargetPanel",
                    wizardPanel.getProvider().getUIManager().
                    getAccessibleDescription()));       // NOI18N
        
        wizardPanel.getProvider().getUIManager().initComponents( customPanel, 
                wizardPanel , this );
        
        browseButton.addActionListener( this );
        documentNameTextField.getDocument().addDocumentListener( this );
        folderTextField.getDocument().addDocumentListener( this );
        
        setName( NbBundle.getMessage(TargetChooserPanelGUI.class,
                "TITLE_name_location"));                // NOI18N
        
    }
    
    public String getDocumentName(){
        return documentNameTextField.getText().trim();
    }
    
    public String getFolder(){
        return folderTextField.getText();
    }
    
    public void setFile( String text){
        fileTextField.setText(text );
    }
    
    public String getFile(){
        return fileTextField.getText();
    }
    
    public void setNameLabel(String name ){
        nameLabel.setText( name );
    }

    public void addLocationListener( ActionListener listener ){
        locationCB.addActionListener(listener);
    }
    
    public String getSelectedFolder(){
        return getLocationRoot().getName();
    }
    
    public void initValues(  ) {
        getPanel().getProvider().getUIManager().initValues( getPanel() , this );
        projectTextField.setText(ProjectUtils.getInformation(getPanel().getProject()).
                getDisplayName());
        // set the location field and find web module
        if (getPanel().getSourceGroups()!=null && 
                getPanel().getSourceGroups().length>0) 
        {
            locationCB.setModel(new javax.swing.DefaultComboBoxModel(
                    getLocations(getPanel().getSourceGroups())));
        } else {
            locationCB.setModel(new javax.swing.DefaultComboBoxModel(
                new Object[]{new LocationItem(getPanel().getProject().
                        getProjectDirectory())}));
        }
        
        // filling the folder field
        String target=null;
        FileObject preselectedFolder =Templates.getTargetFolder( getPanel().
                getTemplateWizard() );
        if (preselectedFolder != null) {
            for(int item = 0; target == null && item < locationCB.getModel().getSize(); 
                item++) 
            {
                FileObject docBase = ((LocationItem)locationCB.getModel().
                            getElementAt(item)).getFileObject();
                if (preselectedFolder.equals(docBase) || 
                        FileUtil.isParentOf(docBase, preselectedFolder)) 
                {
                    target = FileUtil.getRelativePath(docBase, preselectedFolder);
                    locationCB.getModel().setSelectedItem(locationCB.getModel().
                            getElementAt(item));
                    break;
                }
            }
        }
        
        getPanel().getProvider().getUIManager().initFolderValue( getPanel(), target ,
                folderTextField);
        
        //set default new file name
        String documentName = getPanel().getProvider().getNewFileName();
        String newDocumentName = documentName;
        File targetFolder = getFileCreationRoot();
        if (targetFolder != null) {
            FileObject folder = FileUtil.toFileObject(targetFolder);
            if (folder != null) {
                int index = 0;
                while (true) {
                    FileObject _tmp = folder.getFileObject(documentName, 
                            getPanel().getProvider().getExpectedExtension(
                                    getPanel()));
                    if (_tmp == null) {
                        break;
                    }
                    documentName = newDocumentName + (++index);
                }
            }
        }
        documentNameTextField.setText(documentName);
    }
    
    private Object[] getLocations(SourceGroup[] folders) {
        Object[] loc = new Object[folders.length];
        for (int i=0;i<folders.length;i++) loc[i] = new LocationItem(folders[i]);
        return loc;
    }
    
    private String getRelativeSourcesFolder() {
        FileObject sourcesBase = ((LocationItem)locationCB.getModel().
                getSelectedItem()).getFileObject();
        String sourceDir = getPanel().getProvider().getRelativeSourcesFolder( 
                getPanel() , sourcesBase );
        if ( sourceDir == null ){
            sourceDir = "";
        }
        return sourceDir.length()==0?"":sourceDir+'/';        
    }
    
    public String getRelativeTargetFolder() {
        return getRelativeSourcesFolder()+getNormalizedFolder();
    }
    
    public String getNormalizedFolder() {
        String norm = folderTextField.getText().trim();
        if (norm.length()==0) return "";       
        norm = norm.replace('\\','/');
        // removing leading slashes
        int i=0;
        while (i<norm.length() && norm.charAt(i)=='/') {
            i++;
        }
        if (i==norm.length()) {
            return ""; //only slashes  
        }
        norm = norm.substring(i);

        // removing multiple slashes
        java.util.StringTokenizer tokens = new java.util.StringTokenizer(norm,"/");//NOI18N
        StringBuilder buf = new StringBuilder(tokens.nextToken());
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if (token.length()>0) {
                buf.append('/');
                buf.append(token);
            }
        }     
        return buf.toString();
    }
    
    public String getTargetFolder() {
        return getTargetFile().getPath();
    }
    
    public File getTargetFile() {
        String text = getRelativeTargetFolder();
        
        return getPanel().getProvider().getTargetFile( getPanel() , 
                getLocationRoot(), text );
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
    
    // ActionListener implementation -------------------------------------------
    
    public void actionPerformed(ActionEvent e) {
        if ( browseButton == e.getSource() ) {
            FileObject fo=null;
            // Show the browse dialog 
            if (getPanel().getSourceGroups()!=null) fo = BrowseFolders.showDialog(
                    getPanel().getSourceGroups(),
                    org.openide.loaders.DataFolder.class,
                    folderTextField.getText().replace( File.separatorChar, '/' ) );
            else {                 
                Sources sources = ProjectUtils.getSources(getPanel().getProject());
                fo = BrowseFolders.showDialog( sources.getSourceGroups( 
                        Sources.TYPE_GENERIC ),DataFolder.class,
                        folderTextField.getText().replace( File.separatorChar, '/' ) );
            }
            
            if ( fo != null && fo.isFolder() ) {
                FileObject root = ((LocationItem)locationCB.getSelectedItem()).
                    getFileObject();
                folderTextField.setText( FileUtil.getRelativePath( root, fo ) );
            }
                        
        }
    }    
    
    // DocumentListener implementation -----------------------------------------
    
    public void changedUpdate(DocumentEvent e) {

        File rootDirFile = FileUtil.toFile(((LocationItem)locationCB.
                    getSelectedItem()).getFileObject());
        if (rootDirFile != null) {
            String documentName = documentNameTextField.getText().trim();
            if (documentName.length() == 0) {
                fileTextField.setText(""); // NOI18N
            } else {
                StringBuilder name = new StringBuilder(documentName);
                String ext = getPanel().getProvider().getResultExtension( getPanel());
                if ( ext != null && ext.length()>0 ){
                    name.append('.');
                    name.append( ext );
                }
                File newFile = new File(new File(rootDirFile, 
                        folderTextField.getText().replace('/', File.separatorChar)),
                                        name.toString()); //NOI18N
                fileTextField.setText(newFile.getAbsolutePath());
            }
        } else {
            // Not on disk.
            fileTextField.setText(""); // NOI18N
        }
        getPanel().getProvider().getUIManager().changeUpdate( e , getPanel());
        myWizardPanel.fireChange();
    }
    
    public void insertUpdate(DocumentEvent e) {
        changedUpdate( e );
    }
    
    public void removeUpdate(DocumentEvent e) {
        changedUpdate( e );
    }
    
    String getErrorMessage() {
        return getPanel().getProvider().getUIManager().getErrorMessage( getPanel());
    }
    
    boolean isPanelValid() {
        return getPanel().getProvider().getUIManager().isPanelValid();
    }
    
    public static class LocationItem {
        public LocationItem(FileObject fo) {
            myFileObject=fo;
        }
        public LocationItem(SourceGroup group) {
            myFileObject=group.getRootFolder();
            myGroup=group;
        }        
        public FileObject getFileObject() {
            return myFileObject;
        }
        
        public String toString() {
            return (myGroup==null?myFileObject.getName():myGroup.getDisplayName());
        }
        
        FileObject myFileObject;
        SourceGroup myGroup;
    }
    
    FileObject getLocationRoot() {
        return ((LocationItem)locationCB.getModel().getSelectedItem()).getFileObject();
    }
    
    String getCreatedFilePath() {
        return fileTextField.getText();
    }

    private File getFileCreationRoot() {
        File rootDirFile = FileUtil.toFile(((LocationItem) locationCB.
                getSelectedItem()).getFileObject());
        if (rootDirFile != null) {
            return new File(rootDirFile, folderTextField.getText().replace('/', 
                    File.separatorChar));
        } else {
            return null;
        }
    }
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        nameLabel = new javax.swing.JLabel();
        documentNameTextField = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationCB = new javax.swing.JComboBox();
        folderLabel = new javax.swing.JLabel();
        folderTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        pathLabel = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        targetSeparator = new javax.swing.JSeparator();
        customPanel = new javax.swing.JPanel();
        fillerPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        nameLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_FileName_mnem").charAt(0));
        nameLabel.setLabelFor(documentNameTextField);
        nameLabel.setText(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_JspName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(nameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(documentNameTextField, gridBagConstraints);
        documentNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_DESC_FileName")); // NOI18N

        projectLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_Project_mnem").charAt(0));
        projectLabel.setLabelFor(projectTextField);
        projectLabel.setText(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_Project")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(projectLabel, gridBagConstraints);

        projectTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(projectTextField, gridBagConstraints);
        projectTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_DESC_Project")); // NOI18N

        locationLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_Location_mnem").charAt(0));
        locationLabel.setLabelFor(locationCB);
        locationLabel.setText(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_Location")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(locationLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(locationCB, gridBagConstraints);
        locationCB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_DESC_Location")); // NOI18N

        folderLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_Folder_mnem").charAt(0));
        folderLabel.setLabelFor(folderTextField);
        folderLabel.setText(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_Folder")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(folderLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(folderTextField, gridBagConstraints);
        folderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_DESC_Folder")); // NOI18N

        browseButton.setMnemonic(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_Browse_Mnemonic").charAt(0));
        browseButton.setText(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_Browse")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(browseButton, gridBagConstraints);
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "ACSD_Browse")); // NOI18N

        pathLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_CreatedFile_mnem").charAt(0));
        pathLabel.setLabelFor(fileTextField);
        pathLabel.setText(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "LBL_CreatedFile")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(pathLabel, gridBagConstraints);

        fileTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(fileTextField, gridBagConstraints);
        fileTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "A11Y_DESC_CreatedFile")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(targetSeparator, gridBagConstraints);

        customPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(customPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(fillerPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private TargetChooserPanel<T>  getPanel(){
        return myWizardPanel;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JPanel customPanel;
    private javax.swing.JTextField documentNameTextField;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JLabel folderLabel;
    private javax.swing.JTextField folderTextField;
    private javax.swing.JComboBox locationCB;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JSeparator targetSeparator;
    // End of variables declaration//GEN-END:variables
    
    private TargetChooserPanel<T> myWizardPanel;

}
