/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.simpleunit.editor.filecreation;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.cnd.editor.filecreation.BrowseFolders;
import org.netbeans.modules.cnd.editor.filecreation.CndPanelGUI;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.simpleunit.utils.MakefileUtils;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * NewCndFileChooserPanelGUI is SimpleTargetChooserPanelGUI extended with extension selector and logic
 * 
 */
class NewTestSimplePanelGUI extends CndPanelGUI implements ActionListener{

    private final String baseTestName;
    private final Logger logger;
    private final String defaultExtension;
    private String expectedExtension;
    private final MIMEExtensions es;
    private final boolean fileWithoutExtension;

    protected static final String DEFAULT_TESTS_FOLDER = "tests"; // NOI18N

    protected static final String NEW_TEST_PREFIX = getMessage("LBL_NewTest_NewTestPrefix"); // NOI18N

    /** Creates new form NewCndFileChooserPanelGUI */
    NewTestSimplePanelGUI( Project project, SourceGroup[] folders, Component bottomPanel, MIMEExtensions es, String defaultExt, String baseTestName) {
        super(project, folders);

        this.baseTestName = baseTestName;

        this.logger = Logger.getLogger("cnd.editor.filecreation"); // NOI18N
        this.es = es;
        this.fileWithoutExtension = "".equals(defaultExt);
        
        initComponents();
        
        locationComboBox.setRenderer( CELL_RENDERER );
        
        if ( bottomPanel != null ) {
            bottomPanelContainer.add( bottomPanel, java.awt.BorderLayout.CENTER );
        }
        defaultExtension = defaultExt;
        initValues(null, null, null);
        
        browseButton.addActionListener( this );
        locationComboBox.addActionListener( this );
        sourceTextField.getDocument().addDocumentListener( this );
        folderTextField.getDocument().addDocumentListener( this );
        
        setName (NbBundle.getMessage(NewTestSimplePanelGUI.class, "LBL_SimpleTargetChooserPanel_Name")); // NOI18N
    }
    
    public void initValues( FileObject template, FileObject preselectedFolder, String documentName ) {
        assert project != null;
        
        projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());

        Sources sources = ProjectUtils.getSources( project );
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "sources is {0}", sources); // NOI18N
        }

        folders = sources.getSourceGroups( Sources.TYPE_GENERIC );
        if (logger.isLoggable(Level.FINE)) {
            for (int i = 0; i < folders.length; ++i) {
                logger.log(Level.FINE, "folders[{0}] = {1}", new Object[]{i, folders[i]}); // NOI18N
            }
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
        locationComboBox.setSelectedItem( preselectedGroup );               
        // Create OS dependent relative name
        folderTextField.setText( getRelativeNativeName( preselectedGroup.getRootFolder(), preselectedFolder ) );
        if(folderTextField.getText().isEmpty()) {
            folderTextField.setText(DEFAULT_TESTS_FOLDER);
        }
        
        String ext = defaultExtension == null? es.getDefaultExtension() : defaultExtension;
        extensionComboBox.setSelectedItem(ext);
        extensionComboBox.enableInputMethods(true);
        expectedExtension = ext;

        Object editorComp = extensionComboBox.getEditor().getEditorComponent();
        if(editorComp instanceof JTextField)
        {
           ((JTextField)editorComp).getDocument().addDocumentListener(new DocumentListener() {
                private void update(Document doc) {
                    try {
                        expectedExtension = doc.getText(0, doc.getLength() );
                        updateCreatedFile();
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
               
                public void insertUpdate(DocumentEvent e) {
                    update(e.getDocument());
                }

                public void removeUpdate(DocumentEvent e) {
                    update(e.getDocument());
                }

                public void changedUpdate(DocumentEvent e) {
                    update(e.getDocument());
                }
            });
        }
        
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
            if (documentName == null) {
                String baseName = (baseTestName == null) ? 
                    NEW_FILE_PREFIX + template.getName() :
                    getMessage("TestFileSuggestedName", baseTestName).replaceAll(" ", "_").toLowerCase(); // NOI18N
                documentName = baseName;
                FileObject currentFolder = preselectedFolder != null ? preselectedFolder : getTargetGroup().getRootFolder().getFileObject(DEFAULT_TESTS_FOLDER);
                if (currentFolder != null) {
                    int index = 0;
                    while (true) {
                        FileObject _tmp = currentFolder.getFileObject(documentName, ext);
                        if (_tmp == null) {
                            break;
                        }
                        documentName = baseName + ++index;
                    }
                }
                
            }
            sourceTextField.setText (documentName);
        }

        if (template != null) {
            String testName;
            final String baseName = (baseTestName == null) ?
                    NEW_TEST_PREFIX + displayName :
                    getMessage("TestSuggestedName", baseTestName); // NOI18N

            testName = baseName;
            Folder testsRoot = getTestsRootFolder(project);
            if (testsRoot != null) {
                int index = 0;
                while (true) {
                    boolean exist = false;
                    for (Folder folder : testsRoot.getFolders()) {
                        if(folder.getDisplayName().equals(testName)) {
                            exist = true;
                        }
                    }
                    if (!exist) {
                        break;
                    }
                    testName = baseName + " " + (++index); // NOI18N
                }
            }
            testTextField.setText(testName);
            testTextField.selectAll();
        }

        if (MakefileUtils.hasTestTargets(project)) {
            modifiedFileLabel.setVisible(false);
            modifiedFileArea.setVisible(false);
        } else {
            modifiedFileLabel.setVisible(true);
            modifiedFileArea.setVisible(true);
            FileObject makefile = MakefileUtils.getMakefile(project);
            if(makefile != null) {
                modifiedFileArea.setText(FileUtil.getFileDisplayName(makefile));
            }
        }
    }

    private static Folder getTestsRootFolder(Project project) {
        ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor projectDescriptor = cdp.getConfigurationDescriptor();

        Folder root = projectDescriptor.getLogicalFolders();
        Folder testRootFolder = null;
        for (Folder folder : root.getFolders()) {
            if(folder.isTestRootFolder()) {
                testRootFolder = folder;
                break;
            }
        }
        return testRootFolder;
    }

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

    public String getTargetName() {
        return getTestFileName();
    }

    public String getTargetFolder() {
        return getTestFolder();
    }

    public String getTestFolder() {
        
        String folderName = folderTextField.getText().trim();
        
        if ( folderName.length() == 0 ) {
            return "";
        }
        else {           
            return folderName.replace( File.separatorChar, '/' ); // NOI18N
        }
    }
    
    public String getTestFileName() {
        String documentName = sourceTextField.getText().trim();
        
        if ( documentName.length() == 0){
            return null;
        }
        else {
            String docExt = FileUtil.getExtension( documentName );
            if (docExt.length() == 0 && expectedExtension.length() > 0) {
                documentName += '.' + expectedExtension;
            } else {
                assert docExt.equals(expectedExtension);
            }
            return documentName;
        }
    }

    public String getTestName() {
        String documentName = testTextField.getText().trim();
        if ( documentName.length() == 0){
            return null;
        }
        return documentName;
    }
        
    public String getTargetExtension() {
        return expectedExtension;
    }

    public boolean useTargetExtensionAsDefault() {
        return false;
    }
        
    protected void updateCreatedFile() {

        FileObject root = getTargetGroup().getRootFolder();

        String folderName = folderTextField.getText().trim();
        String documentName = sourceTextField.getText().trim();
        String docExt = FileUtil.getExtension( documentName );
        
        String createdFileName = FileUtil.getFileDisplayName( root ) + 
            ( folderName.startsWith("/") || folderName.startsWith( File.separator ) ? "" : "/" ) + // NOI18N
            folderName + 
            ( folderName.endsWith("/") || folderName.endsWith( File.separator ) || folderName.length() == 0 ? "" : "/" ) + // NOI18N
            documentName ; // NOI18N
        
//      Use Cases:        
//        1) User wants to use different (but known) extension then the default one (for example "cpp")
//        1.1 He chooses this extension from the list in the combo box.
//           This extension becomes a new default.
//
//        1.2 He types the full file name with the extension.
//           The extension becomes a new default.
//           No extension is added to the file name.
//
//        2) User wants to use different (but unknown) extension then the default one (for example "abc")
//        2.1) He types the extension into the combo box (which is editable)
//           The notification is displayed at the bottom part of the panel.
//
//        2.2) He types the full name with the extension.
//           The "Extension" combo box gets disabled and no extension is added to the file name.
//           The notification is displayed at the bottom part of the panel.           
        
        if (docExt.length() == 0) {
            extensionComboBox.setEnabled(true);
            createdFileName += "." + expectedExtension; // NOI18N
        } else {
            extensionComboBox.setEnabled(false);
            extensionComboBox.setSelectedItem(docExt);
            expectedExtension = docExt;
        }
            
        createdFileName = createdFileName.replace( '/', File.separatorChar );
        if (!createdFileName.equals(createdFileArea.getText())) {
            createdFileArea.setText( createdFileName );
            changeSupport.fireChange();
        }
    }

    private DefaultComboBoxModel getExtensionsCBModel() {
        Vector<String> vExt = new Vector<String>(es.getValues());
        return new javax.swing.DefaultComboBoxModel(vExt);
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        testLabel = new javax.swing.JLabel();
        testTextField = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationComboBox = new javax.swing.JComboBox();
        folderLabel = new javax.swing.JLabel();
        folderTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        sourceLabel = new javax.swing.JLabel();
        sourceTextField = new javax.swing.JTextField();
        extensionLabel = new javax.swing.JLabel();
        extensionComboBox = new javax.swing.JComboBox();
        createdFileLabel = new javax.swing.JLabel();
        createdFileArea = new javax.swing.JTextArea();
        modifiedFileLabel = new javax.swing.JLabel();
        modifiedFileArea = new javax.swing.JTextArea();
        targetSeparator = new javax.swing.JSeparator();
        bottomPanelContainer = new javax.swing.JPanel();

        testLabel.setLabelFor(testTextField);
        org.openide.awt.Mnemonics.setLocalizedText(testLabel, org.openide.util.NbBundle.getMessage(NewTestSimplePanelGUI.class, "LBL_TargetChooser_Test_Name_Label")); // NOI18N

        projectLabel.setLabelFor(projectTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(NewTestSimplePanelGUI.class, "LBL_TargetChooser_Project_Label")); // NOI18N

        projectTextField.setEditable(false);

        locationLabel.setLabelFor(locationComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(NewTestSimplePanelGUI.class, "LBL_TargetChooser_Location_Label")); // NOI18N

        folderLabel.setLabelFor(folderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(folderLabel, org.openide.util.NbBundle.getMessage(NewTestSimplePanelGUI.class, "LBL_TargetChooser_Folder_Label")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(NewTestSimplePanelGUI.class, "LBL_TargetChooser_Browse_Button")); // NOI18N

        sourceLabel.setLabelFor(sourceTextField);
        org.openide.awt.Mnemonics.setLocalizedText(sourceLabel, org.openide.util.NbBundle.getMessage(NewTestSimplePanelGUI.class, "LBL_TargetChooser_Test_File_Name_Label")); // NOI18N

        extensionLabel.setLabelFor(extensionComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(extensionLabel, org.openide.util.NbBundle.getMessage(NewTestSimplePanelGUI.class, "LBL_TargetChooser_Extension_Label")); // NOI18N

        extensionComboBox.setEditable(true);
        extensionComboBox.setModel(getExtensionsCBModel());
        extensionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extensionComboBoxActionPerformed(evt);
            }
        });

        createdFileLabel.setDisplayedMnemonic('C');
        createdFileLabel.setLabelFor(createdFileArea);
        org.openide.awt.Mnemonics.setLocalizedText(createdFileLabel, org.openide.util.NbBundle.getMessage(NewTestSimplePanelGUI.class, "LBL_TargetChooser_CreatedFile_Label")); // NOI18N

        createdFileArea.setColumns(20);
        createdFileArea.setEditable(false);
        createdFileArea.setRows(1);
        createdFileArea.setFocusable(false);
        createdFileArea.setOpaque(false);

        modifiedFileLabel.setLabelFor(modifiedFileArea);
        org.openide.awt.Mnemonics.setLocalizedText(modifiedFileLabel, org.openide.util.NbBundle.getMessage(NewTestSimplePanelGUI.class, "LBL_TargetChooser_ModifiedFile_Label")); // NOI18N

        modifiedFileArea.setColumns(20);
        modifiedFileArea.setEditable(false);
        modifiedFileArea.setRows(1);
        modifiedFileArea.setFocusable(false);
        modifiedFileArea.setOpaque(false);

        bottomPanelContainer.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(testLabel)
                    .addComponent(projectLabel)
                    .addComponent(locationLabel)
                    .addComponent(folderLabel)
                    .addComponent(sourceLabel)
                    .addComponent(extensionLabel)
                    .addComponent(createdFileLabel)
                    .addComponent(modifiedFileLabel))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(createdFileArea, 0, 231, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(extensionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(folderTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton))
                    .addComponent(locationComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, 231, Short.MAX_VALUE)
                    .addComponent(testTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .addComponent(projectTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .addComponent(sourceTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .addComponent(modifiedFileArea, 0, 231, Short.MAX_VALUE)))
            .addComponent(targetSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
            .addComponent(bottomPanelContainer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(testLabel)
                    .addComponent(testTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLabel)
                    .addComponent(projectTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationLabel)
                    .addComponent(locationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(folderLabel)
                    .addComponent(browseButton)
                    .addComponent(folderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceLabel)
                    .addComponent(sourceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(extensionLabel)
                    .addComponent(extensionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createdFileLabel)
                    .addComponent(createdFileArea, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modifiedFileLabel)
                    .addComponent(modifiedFileArea, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(targetSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(bottomPanelContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE))
        );

        projectTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewTestSimplePanelGUI.class).getString("AD_projectTextField")); // NOI18N
        locationComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewTestSimplePanelGUI.class).getString("AD_locationComboBox")); // NOI18N
        folderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewTestSimplePanelGUI.class).getString("AD_folderTextField")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleName("");
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewTestSimplePanelGUI.class).getString("AD_browseButton")); // NOI18N
        sourceTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewTestSimplePanelGUI.class).getString("AD_documentNameTextField")); // NOI18N
        extensionComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewTestSimplePanelGUI.class, "AD_ExtensionTextField")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewTestSimplePanelGUI.class).getString("AD_SimpleTargetChooserPanelGUI_1")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void extensionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extensionComboBoxActionPerformed
        expectedExtension = (String)extensionComboBox.getSelectedItem();
        updateCreatedFile();
    }//GEN-LAST:event_extensionComboBoxActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanelContainer;
    private javax.swing.JButton browseButton;
    private javax.swing.JTextArea createdFileArea;
    private javax.swing.JLabel createdFileLabel;
    private javax.swing.JComboBox extensionComboBox;
    private javax.swing.JLabel extensionLabel;
    private javax.swing.JLabel folderLabel;
    private javax.swing.JTextField folderTextField;
    private javax.swing.JComboBox locationComboBox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JTextArea modifiedFileArea;
    private javax.swing.JLabel modifiedFileLabel;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JLabel sourceLabel;
    private javax.swing.JTextField sourceTextField;
    private javax.swing.JSeparator targetSeparator;
    private javax.swing.JLabel testLabel;
    private javax.swing.JTextField testTextField;
    // End of variables declaration//GEN-END:variables

    // ActionListener implementation -------------------------------------------
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if ( browseButton == e.getSource() ) {
            FileObject fo=null;
            // Show the browse dialog             

            SourceGroup group = getTargetGroup();

            fo = BrowseFolders.showDialog( new SourceGroup[] { group }, 
                                           project, 
                                           folderTextField.getText().replace( File.separatorChar, '/' ) ); // NOI18N
                        
            if ( fo != null && fo.isFolder() ) {
                String relPath = FileUtil.getRelativePath( group.getRootFolder(), fo );
                folderTextField.setText( relPath.replace( '/', File.separatorChar ) ); // NOI18N
            }                        
        }
        else if ( locationComboBox == e.getSource() )  {
            updateCreatedFile();
        } else if ( extensionComboBox.getEditor() == e.getSource() ) {
            expectedExtension = (String)extensionComboBox.getEditor().getItem();
            updateCreatedFile();
        }
    }    

    protected static String getMessage(String name) {
        return NbBundle.getMessage( NewTestSimplePanelGUI.class, name);
    }

    protected static String getMessage(String name, String param) {
        return NbBundle.getMessage( NewTestSimplePanelGUI.class, name, param);
    }

}
