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

package org.netbeans.modules.cnd.cncppunit.editor.filecreation;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
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
final class NewTestCppUnitPanelGUI extends CndPanelGUI implements ActionListener{

    private final String baseTestName;
    private String sourceExt;
    private String headerExt;
    private final MIMEExtensions sourceExtensions = MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE);
    private final MIMEExtensions headerExtensions = MIMEExtensions.get(MIMENames.HEADER_MIME_TYPE);

    private static final String NEW_TEST_PREFIX = getMessage("LBL_NewTest_NewTestPrefix"); // NOI18N

    private static final String DEFAULT_TESTS_FOLDER = "tests"; // NOI18N

    /** Creates new form NewCndFileChooserPanelGUI */
    NewTestCppUnitPanelGUI( Project project, SourceGroup[] folders, Component bottomPanel, String baseTestName) {
        super(project, folders);

        this.baseTestName = baseTestName;

        initComponents();

        locationComboBox.setRenderer( CELL_RENDERER );

        if ( bottomPanel != null ) {
            bottomPanelContainer.add( bottomPanel, java.awt.BorderLayout.CENTER );
        }
        initValues( null, null, null );

        browseButton.addActionListener( NewTestCppUnitPanelGUI.this );
        locationComboBox.addActionListener( NewTestCppUnitPanelGUI.this );
        classNameTextField.getDocument().addDocumentListener( NewTestCppUnitPanelGUI.this );
        runnerTextField.getDocument().addDocumentListener( NewTestCppUnitPanelGUI.this );
        folderTextField.getDocument().addDocumentListener( NewTestCppUnitPanelGUI.this );

        setName (NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_SimpleTargetChooserPanel_Name")); // NOI18N
    }

    @Override
    public void initValues( FileObject template, FileObject preselectedFolder, String documentName ) {
        assert project != null;

        projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());

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
        if(folderTextField.getText().isEmpty()) {
            folderTextField.setText(DEFAULT_TESTS_FOLDER);
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


        sourceExt = sourceExtensions.getDefaultExtension();
        sourceExtComboBox.setSelectedItem(sourceExt);

        headerExt = headerExtensions.getDefaultExtension();
        headerExtComboBox.setSelectedItem(headerExt);

        if (template != null) {
            if (documentName == null) {
                final String baseName = (baseTestName == null) ?
                    getMessage("NewClassSuggestedName") : // NOI18N
                    getMessage("TestClassSuggestedName", baseTestName).replaceAll(" ", "_").toLowerCase(Locale.getDefault()); // NOI18N
                documentName = baseName;
                FileObject currentFolder = preselectedFolder != null ? preselectedFolder : getTargetGroup().getRootFolder().getFileObject(DEFAULT_TESTS_FOLDER);
                if (currentFolder != null) {
                    documentName += generateUniqueSuffix(
                            currentFolder, getFileName(documentName),
                            sourceExt, headerExt);
                }

            }
            classNameTextField.setText (documentName);
        }

        if (template != null) {
            final String baseName = (baseTestName == null) ?
                    getMessage("NewRunnerSuggestedName") : // NOI18N
                    getMessage("TestRunnerSuggestedName", baseTestName).replaceAll(" ", "_").toLowerCase(Locale.getDefault()); // NOI18N
            String runnerName = baseName;
            FileObject currentFolder = preselectedFolder != null ? preselectedFolder : getTargetGroup().getRootFolder().getFileObject(DEFAULT_TESTS_FOLDER);
            if (currentFolder != null) {
                runnerName += generateUniqueSuffix(
                        currentFolder, getFileName(runnerName),
                        sourceExt, headerExt);
            }
            runnerTextField.setText(runnerName);
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
            modifiedFilesLabel.setVisible(false);
            modifiedFilesArea.setVisible(false);
        } else {
            modifiedFilesLabel.setVisible(true);
            modifiedFilesArea.setVisible(true);
            FileObject makefile = MakefileUtils.getMakefile(project);
            if(makefile != null) {
                modifiedFilesArea.setText(FileUtil.getFileDisplayName(makefile));
            }
        }
    }

    /*package*/ void setControlsEnabled(boolean enable) {
        testTextField.setEnabled(enable);
        projectTextField.setEnabled(enable);
        locationComboBox.setEnabled(enable);
        folderTextField.setEnabled(enable);
        browseButton.setEnabled(enable);
        classNameTextField.setEnabled(enable);
        sourceExtComboBox.setEnabled(enable);
        headerExtComboBox.setEnabled(enable);
        runnerTextField.setEnabled(enable);
        createdFilesArea.setEnabled(enable);
        modifiedFilesArea.setEnabled(enable);
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
            return folderName.replace( File.separatorChar, '/' ); // NOI18N
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
        String createdFileName = FileUtil.getFileDisplayName( root ) +
            ( folderName.startsWith("/") || folderName.startsWith( File.separator ) ? "" : "/" ) + // NOI18N
            folderName +
            ( folderName.endsWith("/") || folderName.endsWith( File.separator ) || folderName.length() == 0 ? "" : "/" );  // NOI18N
        return createdFileName.replace( '/', File.separatorChar );
    }

    @Override
    protected void updateCreatedFile() {
        String sourceFileName = createdFileName(folderTextField) + getSourceFileName();
        String headerFileName = createdFileName(folderTextField) + getHeaderFileName();
        String runnerFileName = createdFileName(folderTextField) + getRunnerFileName();
        String allTogether = sourceFileName + '\n' + headerFileName + '\n' + runnerFileName;
        if (!allTogether.equals(createdFilesArea.getText())) {
            createdFilesArea.setText(allTogether);
            changeSupport.fireChange();
        }
    }

    public String getTestName() {
        String documentName = testTextField.getText().trim();
        if ( documentName.length() == 0){
            return null;
        }
        return documentName;
    }

    public String getSourceFileName() {
        return getFileName(getClassName()) + "." + sourceExt; // NOI18N
    }

    private DefaultComboBoxModel getSourceExtensionsModel() {
        return new DefaultComboBoxModel(new Vector<String>(sourceExtensions.getValues()));
    }

    private DefaultComboBoxModel getExtensionsCBModel() {
        return new DefaultComboBoxModel(new Vector<String>(sourceExtensions.getValues()));
    }

    public String getHeaderFileName() {
        return getFileName(getClassName()) + "." + headerExt; // NOI18N
    }

    public String getHeaderFolder() {
        String folderName = folderTextField.getText().trim();
        if ( folderName.length() == 0 ) {
            return "";
        } else {
            return folderName.replace( File.separatorChar, '/' ); // NOI18N
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

    public String getRunnerFileName() {
        return runnerTextField.getText().trim() + "." + sourceExt; // NOI18N
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

        testLabel = new javax.swing.JLabel();
        testTextField = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationComboBox = new javax.swing.JComboBox();
        folderLabel = new javax.swing.JLabel();
        folderTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        classNameLbl = new javax.swing.JLabel();
        classNameTextField = new javax.swing.JTextField();
        sourceExtLabel = new javax.swing.JLabel();
        sourceExtComboBox = new javax.swing.JComboBox();
        headerExtLabel = new javax.swing.JLabel();
        headerExtComboBox = new javax.swing.JComboBox();
        runnerLabel = new javax.swing.JLabel();
        runnerTextField = new javax.swing.JTextField();
        createdFilesLabel = new javax.swing.JLabel();
        createdFilesArea = new javax.swing.JTextArea();
        modifiedFilesLabel = new javax.swing.JLabel();
        modifiedFilesArea = new javax.swing.JTextArea();
        targetSeparator = new javax.swing.JSeparator();
        bottomPanelContainer = new javax.swing.JPanel();

        testLabel.setLabelFor(testTextField);
        org.openide.awt.Mnemonics.setLocalizedText(testLabel, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_TargetChooser_Test_Name_Label")); // NOI18N

        projectLabel.setLabelFor(projectTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_TargetChooser_Project_Label")); // NOI18N

        projectTextField.setEditable(false);
        projectTextField.setFocusable(false);

        locationLabel.setLabelFor(locationComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_TargetChooser_Location_Label")); // NOI18N

        folderLabel.setLabelFor(folderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(folderLabel, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_TargetChooser_Folder_Label")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_TargetChooser_Browse_Button")); // NOI18N

        classNameLbl.setLabelFor(classNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(classNameLbl, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_TargetChooser_TestClassName_Label")); // NOI18N

        sourceExtLabel.setLabelFor(sourceExtComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(sourceExtLabel, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_TargetChooser_Extension_Label")); // NOI18N

        sourceExtComboBox.setModel(getSourceExtensionsModel());
        sourceExtComboBox.setMinimumSize(new java.awt.Dimension(100, 25));
        sourceExtComboBox.setPreferredSize(new java.awt.Dimension(100, 25));
        sourceExtComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceExtComboBoxActionPerformed(evt);
            }
        });

        headerExtLabel.setLabelFor(headerExtComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(headerExtLabel, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_HeaderChooser_Extension_Label")); // NOI18N

        headerExtComboBox.setModel(getHeaderExtensionsModel());
        headerExtComboBox.setMinimumSize(new java.awt.Dimension(100, 25));
        headerExtComboBox.setPreferredSize(new java.awt.Dimension(100, 25));
        headerExtComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headerExtComboBoxActionPerformed(evt);
            }
        });

        runnerLabel.setLabelFor(runnerTextField);
        org.openide.awt.Mnemonics.setLocalizedText(runnerLabel, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_TargetChooser_Test_Runner_File_Name_Label")); // NOI18N

        createdFilesLabel.setLabelFor(createdFilesArea);
        org.openide.awt.Mnemonics.setLocalizedText(createdFilesLabel, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_TargetChooser_CreatedFiles_Label")); // NOI18N

        createdFilesArea.setColumns(20);
        createdFilesArea.setEditable(false);
        createdFilesArea.setRows(3);
        createdFilesArea.setFocusable(false);
        createdFilesArea.setOpaque(false);

        modifiedFilesLabel.setLabelFor(modifiedFilesArea);
        org.openide.awt.Mnemonics.setLocalizedText(modifiedFilesLabel, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_TargetChooser_ModifiedFiles_Label")); // NOI18N

        modifiedFilesArea.setColumns(20);
        modifiedFilesArea.setEditable(false);
        modifiedFilesArea.setRows(1);
        modifiedFilesArea.setFocusable(false);
        modifiedFilesArea.setOpaque(false);

        bottomPanelContainer.setFocusable(false);
        bottomPanelContainer.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(targetSeparator, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
            .addComponent(bottomPanelContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(testLabel)
                    .addComponent(projectLabel)
                    .addComponent(locationLabel)
                    .addComponent(folderLabel)
                    .addComponent(classNameLbl)
                    .addComponent(sourceExtLabel)
                    .addComponent(runnerLabel)
                    .addComponent(createdFilesLabel)
                    .addComponent(modifiedFilesLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(modifiedFilesArea, 0, 345, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sourceExtComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(headerExtLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(headerExtComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(folderTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton)
                        .addGap(1, 1, 1))
                    .addComponent(projectTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                    .addComponent(locationComboBox, 0, 345, Short.MAX_VALUE)
                    .addComponent(classNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                    .addComponent(testTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                    .addComponent(runnerTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                    .addComponent(createdFilesArea, 0, 345, Short.MAX_VALUE)))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationLabel)
                    .addComponent(locationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(folderLabel)
                    .addComponent(folderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(classNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(classNameLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceExtLabel)
                    .addComponent(sourceExtComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(headerExtLabel)
                    .addComponent(headerExtComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runnerLabel)
                    .addComponent(runnerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createdFilesLabel)
                    .addComponent(createdFilesArea, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modifiedFilesArea, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modifiedFilesLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(targetSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bottomPanelContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE))
        );

        projectTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewTestCppUnitPanelGUI.class).getString("AD_projectTextField")); // NOI18N
        locationComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewTestCppUnitPanelGUI.class).getString("AD_locationComboBox")); // NOI18N
        folderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewTestCppUnitPanelGUI.class).getString("AD_folderTextField")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleName("");
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewTestCppUnitPanelGUI.class).getString("AD_browseButton")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewTestCppUnitPanelGUI.class).getString("AD_SimpleTargetChooserPanelGUI_1")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void sourceExtComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceExtComboBoxActionPerformed
        sourceExt = (String)sourceExtComboBox.getSelectedItem();
        updateCreatedFile();
}//GEN-LAST:event_sourceExtComboBoxActionPerformed

    private void headerExtComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_headerExtComboBoxActionPerformed
        headerExt = (String)headerExtComboBox.getSelectedItem();
        updateCreatedFile();
}//GEN-LAST:event_headerExtComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanelContainer;
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel classNameLbl;
    private javax.swing.JTextField classNameTextField;
    private javax.swing.JTextArea createdFilesArea;
    private javax.swing.JLabel createdFilesLabel;
    private javax.swing.JLabel folderLabel;
    private javax.swing.JTextField folderTextField;
    private javax.swing.JComboBox headerExtComboBox;
    private javax.swing.JLabel headerExtLabel;
    private javax.swing.JComboBox locationComboBox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JTextArea modifiedFilesArea;
    private javax.swing.JLabel modifiedFilesLabel;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JLabel runnerLabel;
    private javax.swing.JTextField runnerTextField;
    private javax.swing.JComboBox sourceExtComboBox;
    private javax.swing.JLabel sourceExtLabel;
    private javax.swing.JSeparator targetSeparator;
    private javax.swing.JLabel testLabel;
    private javax.swing.JTextField testTextField;
    // End of variables declaration//GEN-END:variables

    // ActionListener implementation -------------------------------------------
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if ( browseButton == e.getSource() ) {
            // Show the browse dialog
            SourceGroup group = getTargetGroup();
            FileObject fo = BrowseFolders.showDialog( new SourceGroup[] { group },
                                           project,
                                           folderTextField.getText().replace( File.separatorChar, '/' ) ); // NOI18N

            if ( fo != null && fo.isFolder() ) {
                String relPath = FileUtil.getRelativePath( group.getRootFolder(), fo );
                folderTextField.setText( relPath.replace( '/', File.separatorChar ) ); // NOI18N
            }
        } else if ( locationComboBox == e.getSource() )  {
            updateCreatedFile();
        }
    }

    protected static String getMessage(String name) {
        return NbBundle.getMessage( NewTestCUnitPanelGUI.class, name);
    }

    protected static String getMessage(String name, String param) {
        return NbBundle.getMessage( NewTestCUnitPanelGUI.class, name, param);
    }
}
