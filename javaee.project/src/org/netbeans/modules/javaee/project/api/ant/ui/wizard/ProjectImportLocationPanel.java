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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.javaee.project.api.ant.ui.wizard;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.j2ee.common.FileSearchUtility;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.javaee.project.api.ui.UserProjectSettings;
import org.netbeans.spi.project.ui.support.ProjectChooser;

import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;

/**
 * @author  Pavel Buzek, Radko Najman, David Konecny
 */
final class ProjectImportLocationPanel extends JPanel implements HelpCtx.Provider {
    
    private final ProjectImportLocationWizardPanel wizard;
    private WizardDescriptor wizardDescriptor;
    
    private String currentLibrariesLocation;
    private String nameFormatter;
    private String lastModuleLocation = null;
    
    private Object j2eeModuleType;
    private boolean allowAlternativeBuildXml;
    
    /** Creates new form TestPanel */
    public ProjectImportLocationPanel (Object j2eeModuleType, String name, String title, 
            ProjectImportLocationWizardPanel wizard, String nameFormatter, String importLabel, boolean allowAlternativeBuildXml) {
        this.wizard = wizard;
        this.j2eeModuleType = j2eeModuleType;
        this.nameFormatter = nameFormatter;
        this.allowAlternativeBuildXml = allowAlternativeBuildXml;
        initComponents ();
        jLabelSrcLocationDesc.setText(importLabel);
        currentLibrariesLocation = "."+File.separatorChar+"lib"; // NOI18N
        librariesLocation.setText(currentLibrariesLocation);
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ProjectImportLocationPanel.class, "ACS_NWP1_NamePanel_A11YDesc"));  // NOI18N
        setName(name);
        putClientProperty ("NewProjectWizard_Title", title); // NOI18N
        
        moduleLocationTextField.getDocument().addDocumentListener(new DocumentListener () {
            public void changedUpdate(DocumentEvent e) {
                locationDataChanged(e);
            }
            public void insertUpdate(DocumentEvent e) {
                locationDataChanged(e);
            }
            public void removeUpdate(DocumentEvent e) {
                locationDataChanged(e);
            }
        });
        
        projectLocationTextField.getDocument().addDocumentListener (new DocumentListener () {
            public void changedUpdate(DocumentEvent e) {
                fireChanges();
            }
            public void insertUpdate(DocumentEvent e) {
                fireChanges();
            }
            public void removeUpdate(DocumentEvent e) {
                fireChanges();
            }
        });
        // update state:
        sharableProjectActionPerformed(null);
    }
    
    void read(WizardDescriptor settings) {
        wizardDescriptor = settings;
        
        File projectLocation = (File) settings.getProperty ("projdir");  //NOI18N
        if (projectLocation == null || projectLocation.getParentFile() == null || !projectLocation.getParentFile().isDirectory ())
            projectLocation = ProjectChooser.getProjectsFolder();
        else
            projectLocation = projectLocation.getParentFile();
        
        String projectName = (String) settings.getProperty ("name"); //NOI18N
        if (projectName == null && settings.getProperty ("projdir") != null) {
            int baseCount = 1;
            while ((projectName=validFreeProjectName(projectLocation, nameFormatter, baseCount))==null) {
                baseCount++;
            }
        }
        this.projectNameTextField.setText (projectName == null ? "" : projectName);                
        this.projectNameTextField.selectAll();
    }

    void store (WizardDescriptor settings) {
        File srcRoot = null;
        String srcPath = moduleLocationTextField.getText();
        if (srcPath.length() > 0) {
            srcRoot = FileUtil.normalizeFile(new File(srcPath));
        }
        if (srcRoot != null)
            UserProjectSettings.getDefault().setLastUsedImportLocation(srcRoot);
        settings.putProperty (ProjectImportLocationWizardPanel.SOURCE_ROOT, srcRoot);
        settings.putProperty (ProjectLocationWizardPanel.NAME, projectNameTextField.getText().trim());

        final String projectLocation = projectLocationTextField.getText().trim();
        if (projectLocation.length() >= 0) {
            settings.putProperty (ProjectLocationWizardPanel.PROJECT_DIR, new File(projectLocation));
        }

        settings.putProperty(ProjectLocationWizardPanel.SHARED_LIBRARIES, sharableProject.isSelected() ? librariesLocation.getText() : null);
    }

    boolean valid (WizardDescriptor settings) {
       String sourceLocationPath = moduleLocationTextField.getText().trim();
        if (sourceLocationPath.length() == 0) {
            setErrorMessage("MSG_ProvideExistingSourcesLocation"); //NOI18N
            return false;
        }
        File f = new File (sourceLocationPath);
        if (!f.isDirectory() || !f.canRead()) {
	    String format = NbBundle.getMessage(ProjectImportLocationPanel.class, "MSG_IllegalSources"); //NOI18N
	    wizardDescriptor.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE, MessageFormat.format(format, new Object[] {sourceLocationPath})); //NOI18N
            return false;
        }

        String projectLocationPath = projectLocationTextField.getText().trim();
        f = new File(projectLocationPath);
        String projectName = projectNameTextField.getText().trim();
        f = new File(f, projectName);
        f = ProjectLocationPanel.getCanonicalFile(f);
        if(f == null || !projectName.equals(f.getName())) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(ProjectImportLocationPanel.class, "MSG_ProvideProjectName"));
            return false; // Invalid project name
        }

        if(projectLocationPath.length() == 0) {
            setErrorMessage("MSG_ProvideProjectFolder"); //NOI18N
            return false;
        }
        File projectLocation = new File(projectLocationPath);
        if (projectLocation.exists() && !projectLocation.canWrite()) {
            // Read only project location
            setErrorMessage("MSG_ProjectLocationRO"); //NOI18N
            return false;
        }

        File destFolder = FileUtil.normalizeFile(new File(projectLocationPath));
	
        // #47611: if there is a live project still residing here, forbid project creation.
        if (destFolder.isDirectory()) {
            FileObject destFO = FileUtil.toFileObject(destFolder);
            assert destFO != null : "No FileObject for " + destFolder;
            try {
                if (ProjectManager.getDefault().findProject(destFO) != null) {
                    setErrorMessage("MSG_ProjectFolderHasNbProject"); //NOI18N
                    return false;
                }
            } catch (IOException e) {
                setErrorMessage("MSG_ProjectFolderHasDeletedProject"); //NOI18N
                return false;
            }
        }

	
        File[] kids = destFolder.listFiles();
        if ( destFolder.exists() && kids != null && kids.length > 0) {
            String file = null;
            for (int i=0; i< kids.length; i++) {
                String childName = kids[i].getName();
                if ("nbproject".equals(childName)) {   //NOI18N
                    file = NbBundle.getMessage (ProjectImportLocationPanel.class,"TXT_NetBeansProject");
                }
                else if ("build".equals(childName)) {    //NOI18N
                    file = NbBundle.getMessage (ProjectImportLocationPanel.class,"TXT_BuildFolder");
                }
                else if ("build.xml".equals(childName) && !allowAlternativeBuildXml) {    //NOI18N
                    file = NbBundle.getMessage(ProjectImportLocationPanel.class, "TXT_BuildXML");
                }
//                else if ("WEB-INF".equals(childName)) {    //NOI18N
//                    file = NbBundle.getMessage (ImportLocationVisual.class,"TXT_WebInfFolder");
//                }
                else if ("dist".equals(childName)) {   //NOI18N
                    file = NbBundle.getMessage (ProjectImportLocationPanel.class,"TXT_DistFolder");
                }
                else if ("manifest.mf".equals(childName)) { //NOI18N
                    file = NbBundle.getMessage (ProjectImportLocationPanel.class,"TXT_Manifest");
                }
                if (file != null) {
                    String format = NbBundle.getMessage (ProjectImportLocationPanel.class,"MSG_ProjectFolderInvalid");
                    wizardDescriptor.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE, MessageFormat.format(format, new Object[] {file}));  //NOI18N
                    return false;
                }
            }
        }
        if (j2eeModuleType == J2eeModule.CLIENT) {
            if (FileSearchUtility.guessJavaRoots(FileUtil.toFileObject(destFolder)) == null) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(ProjectImportLocationPanel.class,"MSG_NoAppClientModule")); //NOI18N
                return false; // No java project location
            }
        }
        
        if (sharableProject.isSelected()) {
            String location = librariesLocation.getText();
            if (new File(location).isAbsolute()) {
                wizardDescriptor.putProperty(ProjectLocationPanel.PROP_ERROR_MESSAGE, ProjectLocationPanel.decorateMessage(
                        NbBundle.getMessage(ProjectImportLocationPanel.class, "PanelSharability.absolutePathWarning.text")));

            } else {
                File libLoc = PropertyUtils.resolveFile(projectLocation, location);
                if (!CollocationQuery.areCollocated(projectLocation, libLoc)) {
                    wizardDescriptor.putProperty(ProjectLocationPanel.PROP_ERROR_MESSAGE, ProjectLocationPanel.decorateMessage(
                            NbBundle.getMessage(ProjectImportLocationPanel.class, "PanelSharability.relativePathWarning.text")));
                }
            }
        }
        
        setErrorMessage(null);
        return true;
    }

    private void setErrorMessage(String messageId) {
        wizardDescriptor.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE,
                messageId == null ? null : NbBundle.getMessage(ProjectImportLocationPanel.class, messageId));
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelSrcLocationDesc = new javax.swing.JLabel();
        jLabelSrcLocation = new javax.swing.JLabel();
        moduleLocationTextField = new javax.swing.JTextField();
        jButtonSrcLocation = new javax.swing.JButton();
        jLabelPrjLocationDesc = new javax.swing.JLabel();
        jLabelPrjName = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        jLabelPrjLocation = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        jButtonPrjLocation = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        sharableProject = new javax.swing.JCheckBox();
        librariesLabel = new javax.swing.JLabel();
        librariesLocation = new javax.swing.JTextField();
        browseLibraries = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabelSrcLocationDesc, NbBundle.getMessage(ProjectImportLocationPanel.class, "LBL_IW_LocationSrcDesc")); // NOI18N

        jLabelSrcLocation.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "LBL_IW_ImportLocation_LabelMnemonic").charAt(0));
        jLabelSrcLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelSrcLocation.setLabelFor(moduleLocationTextField);
        jLabelSrcLocation.setText(NbBundle.getMessage(ProjectImportLocationPanel.class, "LBL_IW_LocationSrc_Label")); // NOI18N

        jButtonSrcLocation.setMnemonic(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "LBL_BrowseLocation_MNE").charAt(0));
        jButtonSrcLocation.setText(NbBundle.getMessage(ProjectImportLocationPanel.class, "LBL_NWP1_BrowseLocation_Button")); // NOI18N
        jButtonSrcLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSrcLocationActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabelPrjLocationDesc, NbBundle.getMessage(ProjectImportLocationPanel.class, "LBL_IW_LocationPrjDesc")); // NOI18N

        jLabelPrjName.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "LBL_NWP1_ProjectName_LabelMnemonic").charAt(0));
        jLabelPrjName.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelPrjName.setLabelFor(projectNameTextField);
        jLabelPrjName.setText(NbBundle.getMessage(ProjectImportLocationPanel.class, "LBL_NWP1_ProjectName_Label")); // NOI18N

        jLabelPrjLocation.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "LBL_NWP1_CreatedProjectFolder_LabelMnemonic").charAt(0));
        jLabelPrjLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelPrjLocation.setLabelFor(projectLocationTextField);
        jLabelPrjLocation.setText(NbBundle.getMessage(ProjectImportLocationPanel.class, "LBL_NWP1_CreatedProjectFolder_Label")); // NOI18N

        jButtonPrjLocation.setMnemonic(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "LBL_BrowseProjectFolder_MNE").charAt(0));
        jButtonPrjLocation.setText(NbBundle.getMessage(ProjectImportLocationPanel.class, "LBL_IW_BrowseProjectLocation_Button")); // NOI18N
        jButtonPrjLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrjLocationActionPerformed(evt);
            }
        });

        sharableProject.setSelected(SharableLibrariesUtils.isLastProjectSharable());
        org.openide.awt.Mnemonics.setLocalizedText(sharableProject, org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "ProjectLocationPanel.sharableProject.text")); // NOI18N
        sharableProject.setMargin(new java.awt.Insets(2, 0, 2, 2));
        sharableProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sharableProjectActionPerformed(evt);
            }
        });

        librariesLabel.setLabelFor(librariesLocation);
        org.openide.awt.Mnemonics.setLocalizedText(librariesLabel, org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "ProjectLocationPanel.librariesLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseLibraries, org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "PanelSharabilityVisual.browseLibraries.text")); // NOI18N
        browseLibraries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLibrariesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelSrcLocation)
                    .addComponent(jLabelPrjLocation)
                    .addComponent(jLabelPrjName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(projectNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                    .addComponent(moduleLocationTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                    .addComponent(projectLocationTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonSrcLocation, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButtonPrjLocation, javax.swing.GroupLayout.Alignment.TRAILING)))
            .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 639, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(librariesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(librariesLocation, javax.swing.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browseLibraries))
            .addComponent(jLabelSrcLocationDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 639, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelPrjLocationDesc)
                    .addComponent(sharableProject))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabelSrcLocationDesc)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelSrcLocation)
                    .addComponent(jButtonSrcLocation)
                    .addComponent(moduleLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelPrjLocationDesc)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelPrjName)
                    .addComponent(projectNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelPrjLocation)
                    .addComponent(jButtonPrjLocation)
                    .addComponent(projectLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sharableProject)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(librariesLabel)
                    .addComponent(browseLibraries)
                    .addComponent(librariesLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(93, 93, 93))
        );

        jLabelSrcLocationDesc.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "ACSD_ProjectImportLocationPanel_NA")); // NOI18N
        jLabelSrcLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "ACSD_ProjectImportLocationPanel_NA")); // NOI18N
        moduleLocationTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "ACSD_ProjectImportLocationPanel_NA")); // NOI18N
        jButtonSrcLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "ACSD_ProjectImportLocationPanel_NA")); // NOI18N
        jLabelPrjLocationDesc.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "ACSD_ProjectImportLocationPanel_NA")); // NOI18N
        jLabelPrjName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "ACSD_ProjectImportLocationPanel_NA")); // NOI18N
        projectNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "ACSD_ProjectImportLocationPanel_NA")); // NOI18N
        jLabelPrjLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "ACSD_ProjectImportLocationPanel_NA")); // NOI18N
        projectLocationTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "ACSD_ProjectImportLocationPanel_NA")); // NOI18N
        jButtonPrjLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "ACSD_ProjectImportLocationPanel_NA")); // NOI18N
        jSeparator1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "ACSD_ProjectImportLocationPanel_NA")); // NOI18N
        jSeparator1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "ACSD_ProjectImportLocationPanel_NA")); // NOI18N
        sharableProject.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "ACSD_ProjectImportLocationPanel_NA")); // NOI18N
        librariesLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "ACSD_ProjectImportLocationPanel_NA")); // NOI18N
        librariesLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "ACSD_ProjectImportLocationPanel_NA")); // NOI18N
        browseLibraries.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "ACSD_ProjectImportLocationPanel_NA")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "ACSD_ProjectImportLocationPanel_NA")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectImportLocationPanel.class, "ACSD_ProjectImportLocationPanel_NA")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonPrjLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrjLocationActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setDialogTitle(NbBundle.getMessage(ProjectImportLocationPanel.class, "LBL_IW_BrowseProjectFolder"));
        File lastUsed = (File) UserProjectSettings.getDefault().getLastChooserLocation();
        if (lastUsed != null) {
            chooser.setCurrentDirectory(lastUsed.getParentFile());
        } else {
            chooser.setSelectedFile(ProjectChooser.getProjectsFolder());
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File projectDir = chooser.getSelectedFile();
            projectLocationTextField.setText( projectDir.getAbsolutePath());
            UserProjectSettings.getDefault().setLastChooserLocation(projectDir);
        }            
    }//GEN-LAST:event_jButtonPrjLocationActionPerformed

    private void jButtonSrcLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSrcLocationActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle(NbBundle.getMessage(ProjectImportLocationPanel.class, "LBL_IW_BrowseExistingSource"));
        
        if (moduleLocationTextField.getText().length() > 0 && getProjectLocation().exists()) {
            chooser.setSelectedFile(getProjectLocation());
        } else {
            // honor the contract in issue 58987
            File currentDirectory = null;
            FileObject existingSourcesFO = Templates.getExistingSourcesFolder(wizardDescriptor);
            if (existingSourcesFO != null) {
                File existingSourcesFile = FileUtil.toFile(existingSourcesFO);
                if (existingSourcesFile != null && existingSourcesFile.isDirectory()) {
                    currentDirectory = existingSourcesFile;
                }
            }
            if (currentDirectory != null) {
                chooser.setCurrentDirectory(currentDirectory);
            } else {
                File lastUsedImportLoc = (File) UserProjectSettings.getDefault().getLastUsedImportLocation();
                if (lastUsedImportLoc != null)
                    chooser.setCurrentDirectory(lastUsedImportLoc.getParentFile());
                else                    
                    chooser.setSelectedFile(ProjectChooser.getProjectsFolder());
            }
        }
        
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File projectDir = FileUtil.normalizeFile(chooser.getSelectedFile());
            moduleLocationTextField.setText(projectDir.getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonSrcLocationActionPerformed

private void sharableProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sharableProjectActionPerformed
        librariesLocation.setEnabled(sharableProject.isSelected());
        browseLibraries.setEnabled(sharableProject.isSelected());
        librariesLabel.setEnabled(sharableProject.isSelected());
        if (sharableProject.isSelected()) {
           librariesLocation.setText(currentLibrariesLocation);
        } else {
            librariesLocation.setText("");
        }
        wizard.fireChangeEvent();
}//GEN-LAST:event_sharableProjectActionPerformed

private void browseLibrariesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseLibrariesActionPerformed
    File f = new File(projectLocationTextField.getText());
    String curr = SharableLibrariesUtils.browseForLibraryLocation(librariesLocation.getText().trim(), this, f);
    if (curr != null) {
        currentLibrariesLocation = curr;
        if (sharableProject.isSelected()) {
            librariesLocation.setText(currentLibrariesLocation);
        }
    }
}//GEN-LAST:event_browseLibrariesActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseLibraries;
    private javax.swing.JButton jButtonPrjLocation;
    private javax.swing.JButton jButtonSrcLocation;
    private javax.swing.JLabel jLabelPrjLocation;
    private javax.swing.JLabel jLabelPrjLocationDesc;
    private javax.swing.JLabel jLabelPrjName;
    private javax.swing.JLabel jLabelSrcLocation;
    private javax.swing.JLabel jLabelSrcLocationDesc;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel librariesLabel;
    private javax.swing.JTextField librariesLocation;
    public javax.swing.JTextField moduleLocationTextField;
    public javax.swing.JTextField projectLocationTextField;
    public javax.swing.JTextField projectNameTextField;
    private javax.swing.JCheckBox sharableProject;
    // End of variables declaration//GEN-END:variables

    private String computeProjectName() {
        if (getProjectLocation() == null) {
            return "";
        }
        FileObject fo = FileUtil.toFileObject(getProjectLocation());
        if (fo != null) {
            return fo.getName();
        }
        return "";
    }
    
    private String computeProjectFolder() {
        File f = getProjectLocation();
        if (f == null) {
            return "";
        }
        return f.getAbsolutePath();
    }
    
    // handles changes in Location
    private void locationDataChanged(DocumentEvent de) {
        if (lastModuleLocation == null || !lastModuleLocation.equals(moduleLocationTextField.getText())) {
            lastModuleLocation = moduleLocationTextField.getText();
            projectNameTextField.setText(computeProjectName());
            projectLocationTextField.setText(computeProjectFolder());
        }
        fireChanges();
    }
    
    private void fireChanges() {
        wizard.fireChangeEvent();
    }
    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(generateHelpID(ProjectImportLocationPanel.class, j2eeModuleType));
    }

    static String generateHelpID(Class clazz, J2eeModule.Type moduleType) {
        if (J2eeModule.Type.CAR.equals(moduleType)) {
            return clazz.getName()+"_APPCLIENT"; // NOI18N
        }
        if (J2eeModule.Type.EJB.equals(moduleType)) {
            return clazz.getName()+"_EJB"; // NOI18N
        }
        if (J2eeModule.Type.EAR.equals(moduleType)) {
            return clazz.getName()+"_EAR"; // NOI18N
        }
        if (J2eeModule.Type.WAR.equals(moduleType)) {
            return clazz.getName()+"_WAR"; // NOI18N
        }
        throw new AssertionError("Unknown module type: "+moduleType); // NOI18N
    }

    @Deprecated
    static String generateHelpID(Class clazz, Object moduleType) {
        return generateHelpID(clazz, J2eeModule.Type.fromJsrType(moduleType));
    }
    
    private String validFreeProjectName (final File parentFolder, final String formater, final int index) {
        String name = MessageFormat.format (formater, new Object[]{Integer.valueOf(index)});                
        File file = new File (parentFolder, name);
        return file.exists() ? null : name;
    }
    
    public File getProjectLocation() {
        if (moduleLocationTextField.getText().trim().length() == 0) {
            return null;
        }
        return getAsFile(moduleLocationTextField.getText());
    }
    
    private File getAsFile(String filename) {
        return FileUtil.normalizeFile(new File(filename));
    }

}
