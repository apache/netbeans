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
package org.netbeans.modules.web.wizards.targetpanel.providers;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.target.iterator.api.BrowseFolders;
import org.netbeans.modules.target.iterator.api.TargetChooserPanel;
import org.netbeans.modules.target.iterator.api.TargetChooserPanelGUI;
import org.netbeans.modules.target.iterator.spi.TargetPanelProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.Util;
import org.netbeans.modules.web.taglib.TLDDataObject;
import org.netbeans.modules.web.wizards.FileType;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author ads
 *
 */
@ServiceProvider(service=TargetPanelProvider.class)
public class TagTargetPanelProvider extends WebTargetPanelProvider<FileType> {
    
    static final String TAG_FILE = "tag_file";                   // NOI18N
    
    public static final String IS_TLD_SELECTED = "isTldSelected";// NOI18N
    
    public static final String TLD_FILE_OBJECT = "tldFileObject";// NOI18N
    
    public static final String TAG_NAME        = "tagName";      // NOI18N
    
    public TagTargetPanelProvider() {
        myUIManager = new TagUIManager();
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.wizards.targetpanel.providers.WebTargetPanelProvider#getResultExtension(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    public String getResultExtension( TargetChooserPanel<FileType> panel ) {
        String ext= super.getResultExtension(panel);
        if (getUIManager().isSegment()) {
            ext+="f"; //NOI18N
        }
        else if (getUIManager().isXml()) {
            ext+="x"; //NOI18N
        }
        return ext;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#getNewFileName()
     */
    public String getNewFileName() {
        return super.getNewFileName()+TAG_FILE;   // NOI18N
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#getUIManager()
     */
    public TagUIManager getUIManager() {
        return myUIManager;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#getWizardTitle()
     */
    public String getWizardTitle() {
        return NbBundle.getMessage(TagTargetPanelProvider.class, 
                "TITLE_TagFile"); //NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#init(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    public void init( TargetChooserPanel<FileType> panel ) {
        myJ2eeVersion = Profile.J2EE_14;
        if (panel.getSourceGroups()!=null && panel.getSourceGroups().length>0) {
            WebModule wm = WebModule.getWebModule(panel.getSourceGroups()[0].
                    getRootFolder());
            if (wm!=null) {
                myJ2eeVersion=wm.getJ2eeProfile();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#isValid(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    public boolean isValid( TargetChooserPanel<FileType> panel ) {
        if (Profile.J2EE_13.equals(myJ2eeVersion)) {
            panel.getTemplateWizard().putProperty (WizardDescriptor.PROP_ERROR_MESSAGE, 
                NbBundle.getMessage(TagTargetPanelProvider.class, 
                        "MSG_13notSupported"));     // NOI18N
            return false;
        }
        
        if ( !panel.checkValid() ){
            return false;
        }
        
        //  check if the TLD info is correct
        if (getUIManager().isTldCheckBoxSelected()) {
            String mes=null;
            FileObject tldFo = getUIManager().getTldFileObject();
            String tagName = getUIManager().getTagName();
            if (tldFo==null) {
                mes = NbBundle.getMessage(TagTargetPanelProvider.class,
                        "MSG_noTldSelectedForTagFile");     // NOI18N
            } else if (getUIManager().isTagNameEmpty(tagName)) {
                mes = NbBundle.getMessage(TagTargetPanelProvider.class,
                        "TXT_missingTagName");              // NOI18N
            } else if (!getUIManager().isValidTagName(tagName)) {
                mes = NbBundle.getMessage(TagTargetPanelProvider.class,
                        "TXT_wrongTagName",tagName);        // NOI18N
            } else if (getUIManager().tagNameExists(tagName)) {
                mes = NbBundle.getMessage(TagTargetPanelProvider.class,
                         "TXT_tagNameExists",tagName);      // NOI18N
            }
            if (mes!=null) {
                panel.getTemplateWizard().putProperty (
                        WizardDescriptor.PROP_ERROR_MESSAGE, mes); // NOI18N
                return false;
            }
        }
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#isApplicable(java.lang.Object)
     */
    public boolean isApplicable( FileType id ) {
        return id == FileType.TAG;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.wizards.targetpanel.providers.WebTargetPanelProvider#storeSettings(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    @Override
    public void storeSettings( TargetChooserPanel<FileType> panel ) {
        panel.getTemplateWizard().putProperty(FileType.IS_XML, getUIManager().isXml());
        panel.getTemplateWizard().putProperty(FileType.IS_SEGMENT, getUIManager().isSegment());
        
        panel.getTemplateWizard().putProperty(IS_TLD_SELECTED, 
                getUIManager().isTldCheckBoxSelected());
        panel.getTemplateWizard().putProperty(TLD_FILE_OBJECT, 
                getUIManager().getTldFileObject());
        panel.getTemplateWizard().putProperty(TAG_NAME, 
                getUIManager().getTagName());
    }
    
    
    protected WebModule getWebModule(){
        return myUIManager.getWebModule();
    }

    private Profile myJ2eeVersion;
    private TagUIManager myUIManager;

}

class TagUIManager extends XmlOptionPanelManager {
    
    
    private static final String TAG_FILE_IN_JAVALIB_FOLDER="META-INF/tags"; //NOI18N
    private static final String TAG_FILE_FOLDER="tags"; //NOI18N
    private static final Logger LOG = Logger.getLogger(TagUIManager.class.getName());
    
    @Override
    public void initComponents( JPanel mainPanel , TargetChooserPanel<FileType> panel ,
            final TargetChooserPanelGUI<FileType> uiPanel ) 
    {
        super.initComponents(mainPanel, panel , uiPanel );
        
        uiPanel.setNameLabel(NbBundle.getMessage(
                TagTargetPanelProvider.class, "LBL_TagFileName"));
        getJspSyntaxButton().setText(NbBundle.getMessage(
                TagTargetPanelProvider.class, "OPT_TagFileJsp"));
        getJspSyntaxButton().getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(TagTargetPanelProvider.class, "DESC_TagFile"));
        getXmlSyntaxButton().setText(NbBundle.getMessage(
                TagTargetPanelProvider.class, "OPT_TagFileXml"));
        getXmlSyntaxButton().getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(TagTargetPanelProvider.class, "DESC_TagFileXml"));
        getSegmentBox().setText(NbBundle.getMessage(
                TagTargetPanelProvider.class, "OPT_TagFileSegment"));
        getSegmentBox().getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(TagTargetPanelProvider.class, "A11Y_DESC_TagFile_segment"));
        getDescription().setText(NbBundle.getMessage(
                TagTargetPanelProvider.class,"DESC_TagFile"));
        //listener to update fileTextField
        uiPanel.addLocationListener(
                new ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            uiPanel.changedUpdate(null);
                }
        });
    }

    @Override
    protected int doInitComponents( JPanel mainPanel , 
            final TargetChooserPanel<FileType> panel,
            final TargetChooserPanelGUI<FileType> uiPanel) 
    {
        int gridy = super.doInitComponents(mainPanel, panel, uiPanel );
        
      //remove(fillerPanel);
        myTldCheckBox = new JCheckBox();
        myTldCheckBox.setMnemonic(NbBundle.getMessage( TagTargetPanelProvider.class,
                "A11Y_AddToTLD_mnem").charAt(0));
        myTldCheckBox.setText(NbBundle.getMessage(TagTargetPanelProvider.class, 
                "OPT_addTagFileToTLD"));

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 2, 0);
        mainPanel.add(myTldCheckBox, gridBagConstraints);
        myTldCheckBox.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage( TagTargetPanelProvider.class, "OPT_addToTLD"));
        /*
        javax.swing.JLabel tldDescriptionLabel = new javax.swing.JLabel();
        tldDescriptionLabel.setText(org.openide.util.NbBundle.getMessage(TargetChooserPanelGUI.class, "HINT_tldFile"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        mainPanel.add(tldDescriptionLabel, gridBagConstraints);
        */
        JLabel tldFileLabel = new JLabel();
        tldFileLabel.setDisplayedMnemonic(NbBundle.getMessage( TagTargetPanelProvider.class,
                "A11Y_TLDName_mnem").charAt(0));
        tldFileLabel.setText(NbBundle.getMessage(
                TagTargetPanelProvider.class, "LBL_tldFile"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
        mainPanel.add(tldFileLabel, gridBagConstraints);
        
        myTldTextField = new javax.swing.JTextField();
        myTldTextField.setEditable(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        mainPanel.add(myTldTextField, gridBagConstraints);
        myTldTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage( 
                TagTargetPanelProvider.class,"A11Y_DESC_TLDFile"));
        tldFileLabel.setLabelFor(myTldTextField);

        myBrowseButton = new javax.swing.JButton();
        myBrowseButton.setMnemonic(NbBundle.getMessage( 
                TagTargetPanelProvider.class,"LBL_Browse1_Mnemonic").charAt(0));
        myBrowseButton.setText(NbBundle.getMessage( 
                TagTargetPanelProvider.class, "LBL_Browse"));
        myBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browse(evt, panel,uiPanel);
            }
        });
        myBrowseButton.setEnabled(false);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        mainPanel.add(myBrowseButton, gridBagConstraints);
        
        myBrowseButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage( TagTargetPanelProvider.class,"LBL_Browse"));
        myTldCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                myTagNameTextField.setEditable(myTldCheckBox.isSelected());
                myBrowseButton.setEnabled(myTldCheckBox.isSelected());
                if (myTldCheckBox.isSelected()) {
                    if (myTagName==null) {
                        String name = uiPanel.getDocumentName();
                        if (name.length()>0) {
                            myTagNameTextField.setText(name);
                            myTagName = name;
                        }
                    }
                }
                panel.fireChange();
            }
        });
        
        JLabel tagNameLabel = new JLabel();
        tagNameLabel.setDisplayedMnemonic(
                NbBundle.getMessage( TagTargetPanelProvider.class,
                        "A11Y_TagName_mnem").charAt(0));          
        tagNameLabel.setText(NbBundle.getMessage( TagTargetPanelProvider.class,
                "LBL_tagName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        mainPanel.add(tagNameLabel, gridBagConstraints);
        
        myTagNameTextField = new javax.swing.JTextField();
        //tagNameTextField.setColumns(10);
        myTagNameTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        mainPanel.add(myTagNameTextField, gridBagConstraints);
        myTagNameTextField.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage( TagTargetPanelProvider.class,
                        "A11Y_DESC_TagName"));
        tagNameLabel.setLabelFor(myTagNameTextField);
        myTagNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                myTagName = myTagNameTextField.getText().trim();
                panel.fireChange();
            }
        });
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        mainPanel.add(new JPanel(), gridBagConstraints);
        
        return gridy;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.wizards.targetpanel.providers.XmlOptionPanelManager#initSyntaxButton(int, org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    @Override
    protected int initSyntaxButton( int grid , TargetChooserPanel<FileType> panel, 
            TargetChooserPanelGUI<FileType> uiPanel) 
    {
        int result= super.initSyntaxButton(grid,  panel, uiPanel );
        getJspSyntaxButton().setMnemonic(NbBundle.getMessage(TagTargetPanelProvider.class, 
                "A11Y_TagStandard_mnem").charAt(0));
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#getAccessibleDescription()
     */
    public String getAccessibleDescription() {
        return TagTargetPanelProvider.TAG_FILE;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#getErrorMessage(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    public String getErrorMessage(TargetChooserPanel<FileType> panel) {
        isTagFileValid=true;
        if (getWebModule()!=null) {
            boolean isWebInfLocation = panel.getComponent().getSelectedFolder().
                equals("WEB-INF");  // NOI18N
            if (!panel.getComponent().getNormalizedFolder().startsWith(
                    "WEB-INF/"+TAG_FILE_FOLDER) &&              // NOI18N
                    !(panel.getComponent().getNormalizedFolder().startsWith(
                            TAG_FILE_FOLDER) && isWebInfLocation)) 
            {
                isTagFileValid=false;
                return NbBundle.getMessage(TagTargetPanelProvider.class,
                        "MSG_TagFile");                         // NOI18N
            }
        } else {
            if (!panel.getComponent().getNormalizedFolder().startsWith(
                    TAG_FILE_IN_JAVALIB_FOLDER)) 
            {
                isTagFileValid=false;
                return NbBundle.getMessage(TagTargetPanelProvider.class,
                        "MSG_TagFileInJavalib");
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#initFolderValue(org.netbeans.modules.target.iterator.api.TargetChooserPanel, java.lang.String, javax.swing.JTextField)
     */
    public void initFolderValue( TargetChooserPanel<FileType> panel, String target , 
            JTextField field) {
        
        if (getWebModule()!=null) {
            boolean isWebInfLocation = panel.getComponent().getSelectedFolder().
                equals("WEB-INF");  // NOI18N
            String folder = "";
            if (!isWebInfLocation) {
                folder = "WEB-INF/";    //NOI18N
            }
            if (target==null || !target.startsWith(folder+TAG_FILE_FOLDER))
                field.setText(folder+TAG_FILE_FOLDER+"/"); // NOI18N
            else
                field.setText(target);
        } else {
             if (target==null || !target.startsWith(TAG_FILE_IN_JAVALIB_FOLDER))
                 field.setText(TAG_FILE_IN_JAVALIB_FOLDER+"/"); // NOI18N
             else
                 field.setText(target);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#isPanelValid()
     */
    public boolean isPanelValid() {
        return isTagFileValid;
    }
    
    @Override
    protected void checkBoxChanged( ItemEvent evt , TargetChooserPanel<FileType> panel ,
            TargetChooserPanelGUI<FileType> uiPanel) 
    {
        if (isSegment()) {
            if (isXml()) {
                getDescription().setText(NbBundle.getMessage(TagTargetPanelProvider.class,
                        "DESC_TagFileSegmentXml"));
            } else {
                getDescription().setText(NbBundle.getMessage(TagTargetPanelProvider.class,
                        "DESC_TagFileSegment"));
            }
            String createdFile = uiPanel.getFile();
            if (createdFile.endsWith("tagx")) {//NOI18N
                uiPanel.setFile(
                        createdFile.substring(0,createdFile.length()-1)+"f"); //NOI18N
            }
            else if (createdFile.endsWith("tag")) {//NOI18N 
                uiPanel.setFile(createdFile+"f"); //NOI18N
            }
        } else {
            String createdFile = uiPanel.getFile();
            if (isXml()) {
                getDescription().setText(NbBundle.getMessage(
                        TagTargetPanelProvider.class,"DESC_TagFileXml"));
                if (createdFile.endsWith("tagf")) { //NOI18N
                    uiPanel.setFile(
                            createdFile.substring(0,createdFile.length()-1)+"x"); //NOI18N
                } else if (createdFile.endsWith("tag")) { //NOI18N
                    uiPanel.setFile(createdFile+"x"); //NOI18N
                }
            } else {
                getDescription().setText(NbBundle.getMessage(TagTargetPanelProvider.class,
                        "DESC_TagFile"));
                if (createdFile.endsWith("tagf") || createdFile.endsWith("tagx")) { //NOI18N
                    uiPanel.setFile(
                            createdFile.substring(0,createdFile.length()-1)); //NOI18N
                }
            }
        }
        panel.fireChange();
    }


    String getTagName() {
        return myTagName;
    }

    FileObject getTldFileObject() {
        return myTldFileObject;
    }

    boolean isTldCheckBoxSelected() {
        return myTldCheckBox.isSelected();
    }
    
    boolean tagNameExists( String name ) {
        if (myTagValues!=null && myTagValues.contains(name)) {
            return true; 
        }
        else {
            return false;
        }
    }

    boolean isValidTagName( String name ) {
        if (name==null) {
            return false;
        }
        return org.apache.xerces.util.XMLChar.isValidNCName(name);
    }
    
    boolean isTagNameEmpty( String name ) {
        if (name == null) {
            return true;
        }
        return "".equals(name); // NOI18N
    }
    
    private void browse(ActionEvent evt, TargetChooserPanel<FileType> panel,
            TargetChooserPanelGUI<FileType> uiPanel ) 
    {                                             
        FileObject fo=null;
        // Show the browse dialog 
        if (panel.getSourceGroups()!=null) {
            fo = BrowseFolders.showDialog(
                    panel.getSourceGroups(), TLDDataObject.class,
                    uiPanel.getFolder().replace( File.separatorChar, '/' ) );
        }
        else {       
            Sources sources = ProjectUtils.getSources( panel.getProject());
            fo = BrowseFolders.showDialog( sources.getSourceGroups( Sources.TYPE_GENERIC ),
                                           org.openide.loaders.DataFolder.class,
                                           uiPanel.getFolder()
                                           .replace( File.separatorChar, '/' ) );
        }

        if ( fo != null) {
            myTldFileObject=fo;
            FileObject targetFolder=Templates.getTargetFolder(panel.
                    getTemplateWizard());
            WebModule wm = (targetFolder==null?null:WebModule.getWebModule(targetFolder));
            myTldTextField.setText( FileUtil.getRelativePath( 
                    (wm==null? panel.getProject().getProjectDirectory():
                        wm.getDocumentBase()), fo ) );
            try {
                java.io.InputStream is = myTldFileObject.getInputStream();
                // get existing tag names for testing duplicity
                myTagValues = Util.getTagValues(is, new String[]{"tag","tag-file"},
                        "name"); //NOI18N
                is.close();
            }
            catch (java.io.IOException ex) {
                LOG.log(Level.FINE, "error", ex);
            }
            catch (org.xml.sax.SAXException ex){
                LOG.log(Level.FINE, "error", ex);
            }
            panel.fireChange();
        }
    }
    
    private JCheckBox myTldCheckBox;
    private JTextField myTldTextField;
    private JButton myBrowseButton;
    private JTextField myTagNameTextField;
    private String myTagName;
    private FileObject myTldFileObject;
    private Set<?> myTagValues;
    boolean isTagFileValid=true;
    
}
