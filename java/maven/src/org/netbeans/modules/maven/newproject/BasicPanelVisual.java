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

package org.netbeans.modules.maven.newproject;

import java.awt.EventQueue;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import javax.swing.text.Document;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.BasicAggregateProgressFactory;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.modules.maven.api.MavenValidators;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.modules.maven.embedder.exec.ProgressTransferListener;
import org.netbeans.modules.maven.indexer.api.RepositoryIndexer;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import static org.netbeans.modules.maven.newproject.Bundle.*;
import org.netbeans.modules.maven.options.MavenOptionController;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.modules.options.java.api.JavaOptions;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.ValidatorUtils;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationListener;
import org.netbeans.validation.api.ui.ValidationUI;
import org.netbeans.validation.api.ui.swing.SwingValidationGroup;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author mkleint
 */
public class BasicPanelVisual extends JPanel implements DocumentListener, WindowFocusListener, Runnable {
    
    public static final String PROP_PROJECT_NAME = "projectName"; //NOI18N
    
    private static final String ERROR_MSG = "WizardPanel_errorMessage"; //NOI18N

    private static final ArtifactVersion BORDER_VERSION = new DefaultArtifactVersion("2.0.7"); //NOI18N

    private boolean askedForVersion;

    private ArtifactVersion mavenVersion;

    private static final Object MAVEN_VERSION_LOCK = new Object();

    private BasicWizardPanel panel;
    private final Archetype arch;

    private final ValidationGroup vg;

    private boolean changedPackage = false;
    
    private AggregateProgressHandle handle;
    private final Object HANDLE_LOCK = new Object();
    
    private static final RequestProcessor RPgetver = new RequestProcessor("BasicPanelVisual-getCommandLineMavenVersion"); //NOI18N
    private static final RequestProcessor RPprep = new RequestProcessor("BasicPanelVisual-prepareAdditionalProperties", 5); //NOI18N
    private Archetype currentArchetype;
    
    /** Creates new form PanelProjectLocationVisual */
    @SuppressWarnings("unchecked")
    @Messages({
        "VAL_projectLocationTextField=Project Location",
        "VAL_projectNameTextField=Project Name",
        "VAL_ArtifactId=ArtifactId",
        "VAL_Version=Version",
        "VAL_GroupId=GroupId",
        "VAL_Package=Package",
        "ERR_Project_Folder_cannot_be_created=Project Folder cannot be created.",
        "ERR_Project_Folder_is_not_valid_path=Project Folder is not a valid path.",
        "ERR_Project_Folder_is_UNC=Project Folder cannot be located on UNC path.",
        "ERR_Package_ends_in_dot=Package name can not end in '.'.",
        "# {0} - version", "ERR_old_maven=Maven {0} is too old, version 2.0.7 or newer is needed.",
        "ERR_Project_Folder_exists=Project Folder already exists and is not empty.",
        "ERR_Project_Folder_not_directory=Project Folder is not a directory."
    })
    BasicPanelVisual(BasicWizardPanel panel, Archetype arch) {
        this.panel = panel;
        this.arch = arch;

        initComponents();

        SwingValidationGroup.setComponentName(projectLocationTextField, VAL_projectLocationTextField());
        SwingValidationGroup.setComponentName(projectNameTextField, VAL_projectNameTextField());
        SwingValidationGroup.setComponentName(txtArtifactId, VAL_ArtifactId());
        SwingValidationGroup.setComponentName(txtVersion, VAL_Version());
        SwingValidationGroup.setComponentName(txtGroupId, VAL_GroupId());
        SwingValidationGroup.setComponentName(txtPackage, VAL_Package());

        // Register listener on the textFields to make the automatic updates
        projectNameTextField.getDocument().addDocumentListener(this);
        projectLocationTextField.getDocument().addDocumentListener(this);
        txtArtifactId.getDocument().addDocumentListener(this);
        txtGroupId.getDocument().addDocumentListener(this);
        txtVersion.getDocument().addDocumentListener(this);
        txtPackage.getDocument().addDocumentListener(this);
        tblAdditionalProps.setVisible(false);
        lblAdditionalProps.setVisible(false);
        jScrollPane1.setVisible(false);

        btnSetupNewer.setVisible(false);

        getAccessibleContext().setAccessibleDescription(LBL_CreateProjectStep2());

        txtGroupId.setText(MavenSettings.getDefault().getLastArchetypeGroupId());
        txtVersion.setText(MavenSettings.getDefault().getLastArchetypeVersion());
        vg = ValidationGroup.create();
        runInAWT(new Runnable() {
            @Override
            public void run() {
                vg.add(txtGroupId, MavenValidators.createGroupIdValidators());
                vg.add(txtArtifactId, MavenValidators.createArtifactIdValidators());
                vg.add(txtVersion, MavenValidators.createVersionValidators());
                vg.add(txtPackage, ValidatorUtils.merge(
                        StringValidators.JAVA_PACKAGE_NAME,
                        new AbstractValidator<String>(String.class) {
                        @Override
                        public void validate(Problems problems, String compName, String model)
                        {
                            // MAY_NOT_END_WITH_PERIOD validator broken in NB's
                            // version (empty string); so copy current version' code.
                            if(model != null && !model.isEmpty() && model.charAt(model.length() - 1) == '.')
                                problems.add(ERR_Package_ends_in_dot());
                        }}));
                vg.add(projectNameTextField, ValidatorUtils.merge(
                        MavenValidators.createArtifactIdValidators(),
                        StringValidators.REQUIRE_VALID_FILENAME
                      ));

                vg.add(projectLocationTextField, 
        //                        ValidatorUtils.merge(Validators.FILE_MUST_BE_DIRECTORY,
                        new AbstractValidator<String>(String.class) {
                            @Override public void validate(Problems problems, String compName, String model) {
                                File fil = FileUtil.normalizeFile(new File(model));
                                File projLoc = fil;
                                while (projLoc != null && !projLoc.exists()) {
                                    projLoc = projLoc.getParentFile();
                                }
                                if (projLoc == null || !projLoc.canWrite()) {
                                    problems.add(ERR_Project_Folder_cannot_be_created());
                                    return;
                                }
                                if (FileUtil.toFileObject(projLoc) == null) {
                                    problems.add(ERR_Project_Folder_is_not_valid_path());
                                    return;
                                }
                                //#167136
                                if (Utilities.isWindows() && fil.getAbsolutePath().startsWith("\\\\")) {
                                    problems.add(ERR_Project_Folder_is_UNC());
                                }
                            }
                        });

                vg.addItem(new ValidationListener<Void>(Void.class, ValidationUI.NO_OP, null) {
                    @Override
                    protected void performValidation(Problems problems) {
                        boolean tooOld = isMavenTooOld();
                        btnSetupNewer.setVisible(tooOld);
                        if (tooOld) {
                            problems.add(ERR_old_maven(getCommandLineMavenVersion()));
                            return;
                        }
                        File destFolder = FileUtil.normalizeFile(new File(new File(projectLocationTextField.getText().trim()), projectNameTextField.getText().trim()).getAbsoluteFile());
                        if(destFolder.exists() && !destFolder.isDirectory()) {
                            problems.add(ERR_Project_Folder_not_directory());
                            return;
                        }
                        File[] kids = destFolder.listFiles();
                        if (destFolder.exists() && kids != null && kids.length > 0) {
                            // Folder exists and is not empty
                            problems.add(ERR_Project_Folder_exists());
                        }
                    }
                }, true);
            }
        });

    }
    
    
    public String getProjectName() {
        return this.projectNameTextField.getText();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();
        lblArtifactId = new javax.swing.JLabel();
        txtArtifactId = new javax.swing.JTextField();
        lblGroupId = new javax.swing.JLabel();
        txtGroupId = new javax.swing.JTextField();
        lblVersion = new javax.swing.JLabel();
        txtVersion = new javax.swing.JTextField();
        lblPackage = new javax.swing.JLabel();
        txtPackage = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        pnlAdditionals = new javax.swing.JPanel();
        lblAdditionalProps = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblAdditionalProps = new javax.swing.JTable();
        btnSetupNewer = new javax.swing.JButton();

        projectNameLabel.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "LBL_ProjectName")); // NOI18N

        projectLocationLabel.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "LBL_ProjectLocation")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BTN_Browse")); // NOI18N
        browseButton.setActionCommand("BROWSE");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        createdFolderLabel.setLabelFor(createdFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdFolderLabel, org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "LBL_ProjectFolder")); // NOI18N

        createdFolderTextField.setEditable(false);
        createdFolderTextField.setEnabled(false);

        lblArtifactId.setLabelFor(txtArtifactId);
        org.openide.awt.Mnemonics.setLocalizedText(lblArtifactId, org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "LBL_ArtifactId")); // NOI18N

        txtArtifactId.setEditable(false);
        txtArtifactId.setEnabled(false);

        lblGroupId.setLabelFor(txtGroupId);
        org.openide.awt.Mnemonics.setLocalizedText(lblGroupId, org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "LBL_GroupId")); // NOI18N

        lblVersion.setLabelFor(txtVersion);
        org.openide.awt.Mnemonics.setLocalizedText(lblVersion, org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "LBL_Version")); // NOI18N

        txtVersion.setText("1.0-SNAPSHOT");

        lblPackage.setLabelFor(txtPackage);
        org.openide.awt.Mnemonics.setLocalizedText(lblPackage, org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "LBL_Package")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "LBL_Optional")); // NOI18N

        lblAdditionalProps.setLabelFor(tblAdditionalProps);
        org.openide.awt.Mnemonics.setLocalizedText(lblAdditionalProps, "<additional properties>"); // NOI18N

        tblAdditionalProps.setModel(createPropModel());
        tblAdditionalProps.setColumnSelectionAllowed(true);
        jScrollPane1.setViewportView(tblAdditionalProps);
        tblAdditionalProps.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblAdditionalProps.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.tblAdditionalProps.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnSetupNewer, org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BTN_SetupNewer.text")); // NOI18N
        btnSetupNewer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetupNewerActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlAdditionalsLayout = new javax.swing.GroupLayout(pnlAdditionals);
        pnlAdditionals.setLayout(pnlAdditionalsLayout);
        pnlAdditionalsLayout.setHorizontalGroup(
            pnlAdditionalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdditionalsLayout.createSequentialGroup()
                .addComponent(lblAdditionalProps)
                .addContainerGap(379, Short.MAX_VALUE))
            .addGroup(pnlAdditionalsLayout.createSequentialGroup()
                .addComponent(btnSetupNewer)
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
        );
        pnlAdditionalsLayout.setVerticalGroup(
            pnlAdditionalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdditionalsLayout.createSequentialGroup()
                .addComponent(lblAdditionalProps)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSetupNewer))
        );

        btnSetupNewer.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.btnSetupNewer.AccessibleContext.accessibleDescription")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(createdFolderLabel)
                    .addComponent(projectLocationLabel)
                    .addComponent(projectNameLabel)
                    .addComponent(lblPackage)
                    .addComponent(lblVersion)
                    .addComponent(lblGroupId)
                    .addComponent(lblArtifactId))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtArtifactId, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                    .addComponent(txtGroupId, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                    .addComponent(txtVersion, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                    .addComponent(txtPackage, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                    .addComponent(createdFolderTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                    .addComponent(projectLocationTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                    .addComponent(projectNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(browseButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addContainerGap())))
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlAdditionals, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(12, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(projectNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton)
                    .addComponent(projectLocationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createdFolderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(createdFolderLabel))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtArtifactId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblArtifactId))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtGroupId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblGroupId))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblVersion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPackage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPackage)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlAdditionals, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        projectNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.projectNameTextField.AccessibleContext.accessibleDescription")); // NOI18N
        projectLocationTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.projectLocationTextField.AccessibleContext.accessibleDescription")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.browseButton.AccessibleContext.accessibleDescription")); // NOI18N
        createdFolderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.createdFolderTextField.AccessibleContext.accessibleDescription")); // NOI18N
        txtArtifactId.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.txtArtifactId.AccessibleContext.accessibleDescription")); // NOI18N
        txtGroupId.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.txtGroupId.AccessibleContext.accessibleDescription")); // NOI18N
        txtVersion.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.txtVersion.AccessibleContext.accessibleDescription")); // NOI18N
        txtPackage.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.txtPackage.AccessibleContext.accessibleDescription")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.jLabel1.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    @Messages("TIT_Select_Project_Location=Select Project Location")
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        String command = evt.getActionCommand();
        if ("BROWSE".equals(command)) { //NOI18N
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(null);
            chooser.setDialogTitle(TIT_Select_Project_Location());
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            String path = this.projectLocationTextField.getText();
            if (path.length() > 0) {
                File f = new File(path);
                if (f.exists()) {
                    chooser.setSelectedFile(f);
                }
            }
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
                File projectDir = chooser.getSelectedFile();
                projectLocationTextField.setText(FileUtil.normalizeFile(projectDir).getAbsolutePath());
            }
            panel.fireChangeEvent();
        }
        
    }//GEN-LAST:event_browseButtonActionPerformed

    private void btnSetupNewerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetupNewerActionPerformed
        OptionsDisplayer.getDefault().open(JavaOptions.JAVA + "/" + MavenOptionController.OPTIONS_SUBPATH); //NOI18N
        panel.getValidationGroup().performValidation();
    }//GEN-LAST:event_btnSetupNewerActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JButton btnSetupNewer;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAdditionalProps;
    private javax.swing.JLabel lblArtifactId;
    private javax.swing.JLabel lblGroupId;
    private javax.swing.JLabel lblPackage;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JPanel pnlAdditionals;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    private javax.swing.JTable tblAdditionalProps;
    private javax.swing.JTextField txtArtifactId;
    private javax.swing.JTextField txtGroupId;
    private javax.swing.JTextField txtPackage;
    private javax.swing.JTextField txtVersion;
    // End of variables declaration//GEN-END:variables
    
    @Override
    public void addNotify() {
        super.addNotify();
        //same problem as in 31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
        tblAdditionalProps.setVisible(false);
        lblAdditionalProps.setVisible(false);
        jScrollPane1.setVisible(false);
        // for maven version checking
        SwingUtilities.getWindowAncestor(this).addWindowFocusListener(this);
    }

    @Messages("ERR_multibyte=Multibyte chars not permitted in project name or Maven coordinates.")
    static boolean containsMultiByte (String text, WizardDescriptor wd) {
        char[] textChars = text.toCharArray();
        for (int i = 0; i < textChars.length; i++) {
            if ((int)textChars[i] > 255) {
                wd.putProperty(ERROR_MSG, ERR_multibyte());
                return true;
            }

        }
        return false;
    }

    private boolean isMavenTooOld () {
        ArtifactVersion version = getCommandLineMavenVersion();
        return version != null ? BORDER_VERSION.compareTo(version) > 0 : false;
    }

    private ArtifactVersion getCommandLineMavenVersion () {
        synchronized (MAVEN_VERSION_LOCK) {
            if (!askedForVersion) {
                askedForVersion = true;
                // obtain version asynchronously, as it takes some time
                RPgetver.post(this);
            }
            return mavenVersion;
        }
    }
    
    void store(WizardDescriptor d) {
        synchronized (HANDLE_LOCK) {
            if (handle != null) {
                handle.finish();
                handle = null;
            }
        }
        String name = projectNameTextField.getText().trim();
        String folder = createdFolderTextField.getText().trim();
        final File projectFolder = new File(folder);
        
        // PROJECT_PARENT_FOLDER confusing, better name is PROJECT_BASE_FOLDER
        d.putProperty(CommonProjectActions.PROJECT_PARENT_FOLDER, projectFolder);
        if (d instanceof TemplateWizard) {
            ((TemplateWizard) d).setTargetFolderLazy(() -> {
                projectFolder.mkdirs();
                return DataFolder.findFolder(FileUtil.toFileObject(projectFolder));
            });
        }
        d.putProperty("name", name); //NOI18N
        if (d instanceof TemplateWizard) {
            ((TemplateWizard) d).setTargetName(name);
        }
        d.putProperty("artifactId", txtArtifactId.getText().trim()); //NOI18N
        d.putProperty("groupId", txtGroupId.getText().trim()); //NOI18N
        MavenSettings.getDefault().setLastArchetypeGroupId(txtGroupId.getText().trim());
        d.putProperty("version", txtVersion.getText().trim()); //NOI18N
        MavenSettings.getDefault().setLastArchetypeVersion(txtVersion.getText().trim());
        d.putProperty("package", txtPackage.getText().trim()); //NOI18N
        if (tblAdditionalProps.isVisible()) {
            if (tblAdditionalProps.isEditing()) {
                TableCellEditor edito = tblAdditionalProps.getCellEditor();
                if (edito != null) {
                    edito.stopCellEditing();
                }
            }
            TableModel mdl = tblAdditionalProps.getModel();
            HashMap<String, String> map = new HashMap<String, String>();
            for (int i = 0; i < mdl.getRowCount(); i++) {
                map.put((String)mdl.getValueAt(i, 0), (String)mdl.getValueAt(i, 1));
            }
            d.putProperty(ArchetypeWizardUtils.ADDITIONAL_PROPS, map);
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                panel.getValidationGroup().remove(vg);
            }
        });
    }
    
    private static final String SETTINGS_HAVE_READ = "BasicPanelVisual-read-properties-before"; //NOI18N
    @Messages({
        "# {0} - project count", "TXT_MavenProjectName=mavenproject{0}",
        "TXT_Checking1=Checking additional creation properties..."
    })
    void read(WizardDescriptor settings, Map<String,String> defaultProps) {
        synchronized (HANDLE_LOCK) {
            if (handle != null) {
                handle.finish();
                handle = null;
            }
        }        
        // PROJECT_PARENT_FOLDER usage is confusing. Sometimes it's the
        // parent directory, sometimes it's the project directory.
        // Maybe introduce PROJECT_BASE_FOLDER, to clarify and differentiate.
        // But for local fix [NETBEANS-4206] keep track of whether
        // these properties have been read before.
        boolean haveRead =  Boolean.parseBoolean((String)settings.getProperty(SETTINGS_HAVE_READ)); //NOI18N
        File projectFolder = (File) settings.getProperty(CommonProjectActions.PROJECT_PARENT_FOLDER); //NOI18N
        File projectLocation;
        if(!haveRead && projectFolder != null) {
            // First time in here, dialog was started with project folder;
            // example is creating a project from pom parent
            projectLocation = projectFolder;
        } else {
            if (projectFolder == null || projectFolder.getParentFile() == null || !projectFolder.getParentFile().isDirectory()) {
                projectLocation = ProjectChooser.getProjectsFolder();
            } else {
                projectLocation = projectFolder.getParentFile();
            }
        }
        if(!haveRead) {
            settings.putProperty(SETTINGS_HAVE_READ, "true"); //NOI18N
        }
        this.projectLocationTextField.setText(projectLocation.getAbsolutePath());
        
        String projectName = (String) settings.getProperty("name"); //NOI18N

        if(projectName == null) {
            int baseCount = 1;
            while ((projectName = validFreeProjectName(projectLocation, TXT_MavenProjectName(baseCount))) == null) {
                baseCount++;                
            }
        }
        
        String gr = (String) settings.getProperty("groupId");
        if (gr != null) {
            txtGroupId.setText(gr);
        }
        String ver = (String) settings.getProperty("version");
        if (ver != null) {
            txtVersion.setText(ver);
        }
        
        this.projectNameTextField.setText(projectName);
        this.projectNameTextField.selectAll();
        // skip additional properties if direct known archetypes without additional props used
        if (panel.areAdditional()) {
            final Archetype archet = getArchetype(settings);
            this.currentArchetype = archet;
            lblAdditionalProps.setText(TXT_Checking1());
            lblAdditionalProps.setVisible(true);
            tblAdditionalProps.setVisible(false);
            jScrollPane1.setVisible(false);
            RPprep.post(new Runnable() {
                @Override
                public void run() {
                    prepareAdditionalProperties(archet, defaultProps);
                }
            });
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                panel.getValidationGroup().addItem(vg, true);
            }
        });
    }

    @Messages({
        "COL_Key=Key",
        "COL_Value=Value",
        "TXT_Checking2=A&dditional Creation Properties:"
    })
    private void prepareAdditionalProperties(Archetype arch, Map<String, String> defaultProps) {
        final DefaultTableModel dtm = new DefaultTableModel();
        dtm.addColumn(COL_Key());
        dtm.addColumn(COL_Value());
        try {
            Artifact art = downloadArchetype(arch);
            File fil = art.getFile();
            if (fil.exists()) {
                Map<String, String> props = arch.loadRequiredProperties();
                for (Map.Entry<String, String> entry : props.entrySet()) {
                    String key = entry.getKey();
                    String defVal = entry.getValue();
                    if ("groupId".equals(key) || "artifactId".equals(key) || "version".equals(key)) {
                        continue; //don't show the basic props as additionals..
                    }
                    if (defaultProps != null && defaultProps.containsKey(key)) {
                        defVal = defaultProps.get(key);
                    }
                    if (defVal == null) {
                        defVal = "";
                    }
                    dtm.addRow(new Object[] {key, defVal });
                }
            }
        } catch (ArtifactResolutionException ex) {
            //#143026
            Logger.getLogger( BasicPanelVisual.class.getName()).log( Level.FINE, "Cannot download archetype", ex);
        } catch (ArtifactNotFoundException ex) {
            //#143026
            Logger.getLogger( BasicPanelVisual.class.getName()).log( Level.FINE, "Cannot download archetype", ex);
        }
        if (arch != currentArchetype || !this.isVisible()) {
            //prevent old runnables from overwriting the ui.
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (dtm.getRowCount() > 0) {
                    Mnemonics.setLocalizedText(lblAdditionalProps, TXT_Checking2());
                    lblAdditionalProps.setVisible(true);
                    jScrollPane1.setVisible(true);
                    tblAdditionalProps.setModel(dtm);
                    tblAdditionalProps.setVisible(true);
                } else {
                    tblAdditionalProps.setVisible(false);
                    lblAdditionalProps.setVisible(false);
                    jScrollPane1.setVisible(false);
                }
            }
        });
    }

    private Archetype getArchetype (WizardDescriptor settings) {
        if (arch != null) {
            return arch;
        }
        return (Archetype) settings.getProperty(MavenWizardIterator.PROP_ARCHETYPE);
    }
    
    @Messages("Handle_Download=Downloading Archetype")
    private Artifact downloadArchetype(Archetype arch) throws ArtifactResolutionException, ArtifactNotFoundException {
        
        AggregateProgressHandle hndl = BasicAggregateProgressFactory.createHandle(Handle_Download(),
                new ProgressContributor[] {
                    BasicAggregateProgressFactory.createProgressContributor("zaloha") },  //NOI18N
                ProgressTransferListener.cancellable(), null);
        synchronized (HANDLE_LOCK) {
           handle = hndl;
        }
        try {
            arch.resolveArtifacts(hndl);
        } finally {
            synchronized (HANDLE_LOCK) {//prevent store()/read() methods to call finish - issue 236251
                if (hndl == handle) {
                    handle = null;
                }
            }
        }
        //#154913
        RepositoryIndexer.updateIndexWithArtifacts(RepositoryPreferences.getInstance().getLocalRepository(), Collections.singletonList(arch.getArtifact()));
        return arch.getArtifact();
    }
    
    private TableModel createPropModel() {
        return new DefaultTableModel();
    }
    
    
    // Implementation of DocumentListener --------------------------------------
    
    @Override
    public void changedUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME,null,this.projectNameTextField.getText());
        }
    }
    
    @Override
    public void insertUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME,null,this.projectNameTextField.getText());
        }
    }
    
    @Override
    public void removeUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME,null,this.projectNameTextField.getText());
        }
    }
    
    /** Handles changes in the Project name and project directory, */
    private void updateTexts(DocumentEvent e) {
        
        Document doc = e.getDocument();
        
        if (doc == projectNameTextField.getDocument() || doc == projectLocationTextField.getDocument()) {
            // Change in the project name
            
            String projectName = projectNameTextField.getText();
            String projectFolder = projectLocationTextField.getText();
            
            //if (projectFolder.trim().length() == 0 || projectFolder.equals(oldName)) {
            createdFolderTextField.setText(projectFolder + File.separatorChar + projectName);
            //}
            
        }
        
        if (projectNameTextField.getDocument() == doc) {
            String projName = projectNameTextField.getText().trim();
            txtArtifactId.setText(projName.replace(" ", ""));
        }
        
        if (!changedPackage && (projectNameTextField.getDocument() == doc || txtGroupId.getDocument() == doc)) {
            txtPackage.getDocument().removeDocumentListener(this);
            txtPackage.setText(getPackageName(txtGroupId.getText() + "." + txtArtifactId.getText().replace("-", "."))); //NOI18N
            txtPackage.getDocument().addDocumentListener(this);
        }
        
        if (txtPackage.getDocument() == doc) {
            changedPackage = txtPackage.getText().trim().length() != 0;
        }
        if (vg != null) {
            vg.performValidation(); // Notify that the panel changed
        }
    }
    
    private String validFreeProjectName(File parentFolder, String name) {
        File file = new File (parentFolder, name);
        return file.exists() ? null : name;
    }
    


    static String getPackageName (String displayName) {
        StringBuilder builder = new StringBuilder ();
        boolean firstLetter = true;
        for (int i=0; i< displayName.length(); i++) {
            char c = displayName.charAt(i);
            if ((!firstLetter && Character.isJavaIdentifierPart (c))
                    || (firstLetter && Character.isJavaIdentifierStart(c))) {
                firstLetter = false;
                if (Character.isUpperCase(c)) {
                    c = Character.toLowerCase(c);
                }
                builder.append(c);
                continue;
            }
            if (!firstLetter && c == '.') {
                firstLetter = true;
                builder.append(c);
                continue;
            }
        }
        String toRet =  builder.length() == 0 ? "pkg" : builder.toString(); //NOI18N
        return toRet;
    }

    /*** Implementation of WindowFocusListener ***/

    @Override
    public void windowGainedFocus(WindowEvent e) {
        // trigger re-check of maven version
        askedForVersion = false;
        getCommandLineMavenVersion();
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
    }

    /*** Implementation of Runnable, checks Maven version ***/

    @Override
    public void run() {
        if (!EventQueue.isDispatchThread()) {
            // phase one, outside EQ thread
            String version = MavenSettings.getCommandLineMavenVersion();
            mavenVersion = version != null ? new DefaultArtifactVersion(version.trim()) : null;
            // trigger revalidation -> phase two
            SwingUtilities.invokeLater(this);
        } else {
            // phase two, inside EQ thread
            vg.performValidation();
        }
    }

    private void runInAWT(Runnable runnable) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(runnable);
        } else {
            runnable.run();
        }
    }

}
