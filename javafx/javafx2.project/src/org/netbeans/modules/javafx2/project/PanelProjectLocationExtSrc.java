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
package org.netbeans.modules.javafx2.project;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Sets up source and test folders for new Java project from existing sources.
 * @author Tomas Zezula et al.
 */
public class PanelProjectLocationExtSrc extends SettingsPanel {

    private PanelConfigureProject firer;
    private WizardDescriptor wizardDescriptor;
    private boolean calculatePF;
    private static final String DEFAULT_BUILD_SCRIPT_NAME = "build.xml"; // NOI18N
    private static final String NB_BUILD_SCRIPT_NAME = "nbbuild.xml"; // NOI18N

    /** Creates new form PanelSourceFolders */
    PanelProjectLocationExtSrc(PanelConfigureProject panel) {
        this.firer = panel;
        initComponents();
        this.projectName.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
                calculateProjectFolder();
                dataChanged();
                firePropertyChange(PanelProjectLocationVisual.PROP_PROJECT_NAME, null, projectName.getText());
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                calculateProjectFolder();
                dataChanged();
                firePropertyChange(PanelProjectLocationVisual.PROP_PROJECT_NAME, null, projectName.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                calculateProjectFolder();
                dataChanged();
                firePropertyChange(PanelProjectLocationVisual.PROP_PROJECT_NAME, null, projectName.getText());
            }
        });
        this.projectLocation.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
                setCalculateProjectFolder(false);
                checkBuildScriptName();
                dataChanged();
                firePropertyChange(PanelProjectLocationVisual.PROP_PROJECT_LOCATION, null, projectLocation.getText());
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                setCalculateProjectFolder(false);
                checkBuildScriptName();
                dataChanged();
                firePropertyChange(PanelProjectLocationVisual.PROP_PROJECT_LOCATION, null, projectLocation.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setCalculateProjectFolder(false);
                checkBuildScriptName();
                dataChanged();
                firePropertyChange(PanelProjectLocationVisual.PROP_PROJECT_LOCATION, null, projectLocation.getText());
            }
        });
        this.buildScriptName.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                dataChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                dataChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                dataChanged();
            }
        });
    }

    private synchronized void calculateProjectFolder() {
        if (this.calculatePF) {
            File f = ProjectChooser.getProjectsFolder();
            this.projectLocation.setText(f.getAbsolutePath() + File.separator + this.projectName.getText());
            this.calculatePF = true;
        }
    }

    private void checkBuildScriptName() {
        if (DEFAULT_BUILD_SCRIPT_NAME.equals(this.buildScriptName.getText())) {
            final String path = this.projectLocation.getText();
            final File projDir = new File(path);
            final File buildScript = new File(projDir, DEFAULT_BUILD_SCRIPT_NAME);
            if (buildScript.exists()) {
                this.buildScriptName.setText(NB_BUILD_SCRIPT_NAME);
            }
        }
    }

    private synchronized void setCalculateProjectFolder(boolean value) {
        this.calculatePF = value;
    }

    private void dataChanged() {
        this.firer.fireChangeEvent();
    }

    @Override
    void read(WizardDescriptor settings) {
        this.wizardDescriptor = settings;
        String path = null;
        String projectName = null;
        File projectLocation = (File) settings.getProperty("projdir"); // NOI18N
        if (projectLocation == null) {
            projectLocation = ProjectChooser.getProjectsFolder();
            int index = WizardSettings.getNewProjectCount();
            String formater = NbBundle.getMessage(PanelSourceFolders.class, "TXT_JavaProject"); // NOI18N
            File file;
            do {
                index++;
                projectName = MessageFormat.format(formater, new Object[]{index});
                file = new File(projectLocation, projectName);
            } while (file.exists());
            settings.putProperty(JavaFXProjectWizardIterator.PROP_NAME_INDEX, index);
            this.projectLocation.setText(projectLocation.getAbsolutePath());
            this.setCalculateProjectFolder(true);
        } else {
            projectName = (String) settings.getProperty("name"); // NOI18N
            boolean tmpFlag = this.calculatePF;
            this.projectLocation.setText(projectLocation.getAbsolutePath());
            this.setCalculateProjectFolder(tmpFlag);
        }
        this.projectName.setText(projectName);
        this.projectName.selectAll();
        String buildScriptName = (String) settings.getProperty("buildScriptName"); // NOI18N
        if (buildScriptName == null) {
            assert projectLocation != null;
            buildScriptName = DEFAULT_BUILD_SCRIPT_NAME;
            File bf = new File(projectLocation, buildScriptName);
            if (bf.exists()) {
                buildScriptName = NB_BUILD_SCRIPT_NAME;
            }
            //Todo: Mybe generate other name, like nb-build2.xml - not sure if it's desirable
        }
        this.buildScriptName.setText(buildScriptName);

    }

    @Override
    void store(WizardDescriptor settings) {
        settings.putProperty("name", this.projectName.getText()); // NOI18N
        File projectsDir = new File(this.projectLocation.getText());
        settings.putProperty("projdir", projectsDir); // NOI18N
        String buildScriptName = this.buildScriptName.getText();
        if (DEFAULT_BUILD_SCRIPT_NAME.equals(buildScriptName)) {
            buildScriptName = null;
        }
        settings.putProperty("buildScriptName", buildScriptName); // NOI18N
    }

    @Override
    boolean valid(WizardDescriptor settings) {
        String result = checkValidity(this.projectName.getText(), this.projectLocation.getText(), this.buildScriptName.getText());
        if (result == null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); // NOI18N
            return true;
        } else {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, result);
            return false;
        }
    }

    static String checkValidity(final String projectName, final String projectLocation, final String buildScriptName) {
        if (JavaFXProjectWizardIterator.isIllegalProjectName(projectName)) {
            // Display name not specified
            return NbBundle.getMessage(PanelSourceFolders.class, "MSG_IllegalProjectName"); // NOI18N
        }

        File projLoc = new File(projectLocation).getAbsoluteFile();

        if (JavaFXProjectWizardIterator.getCanonicalFile(projLoc) == null) {
            return NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalProjectLocation"); // NOI18N
        }

        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            return NbBundle.getMessage(PanelSourceFolders.class, "MSG_ProjectFolderReadOnly"); // NOI18N
        }

        if (buildScriptName.length() == 0 || buildScriptName.indexOf(File.separatorChar) >= 0 || !buildScriptName.endsWith(".xml")) {   //NOI18N
            return NbBundle.getMessage(PanelSourceFolders.class, "MSG_WrongBuildScriptName"); // NOI18N
        }

        File destFolder = FileUtil.normalizeFile(new File(projectLocation));
        File[] kids = destFolder.listFiles();
        if (destFolder.exists() && kids != null && kids.length > 0) {
            String file = null;
            for (int i = 0; i < kids.length; i++) {
                String childName = kids[i].getName();
                if ("nbproject".equals(childName)) {   //NOI18N
                    file = NbBundle.getMessage(PanelSourceFolders.class, "TXT_NetBeansProject"); // NOI18N
                } else if ("build".equals(childName)) {    //NOI18N
                    file = NbBundle.getMessage(PanelSourceFolders.class, "TXT_BuildFolder"); // NOI18N
                } else if ("dist".equals(childName)) {   //NOI18N
                    file = NbBundle.getMessage(PanelSourceFolders.class, "TXT_DistFolder"); // NOI18N
                } else if (buildScriptName.equals(childName)) {   //NOI18N
                    file = NbBundle.getMessage(PanelSourceFolders.class, "TXT_BuildXML"); // NOI18N
                }
                if (file != null) {
                    String format = NbBundle.getMessage(PanelSourceFolders.class, "MSG_ProjectFolderInvalid"); // NOI18N
                    return MessageFormat.format(format, new Object[]{file});
                }
            }
        }

        // #47611: if there is a live project still residing here, forbid project creation.
        if (destFolder.isDirectory()) {
            FileObject destFO = FileUtil.toFileObject(destFolder);
            assert destFO != null : "No FileObject for " + destFolder;
            Project owner = null;
            try {
                owner = ProjectManager.getDefault().findProject(destFO);
            } catch (IOException e) {
                // need not report here; clear remains false -> error
            }
            if (owner != null) {
                return NbBundle.getMessage(PanelSourceFolders.class, "MSG_ProjectFolderHasDeletedProject", ProjectUtils.getInformation(owner).getDisplayName()); // NOI18N
            }
        }
        return null;
    }

    @Override
    void validate(WizardDescriptor settings) throws WizardValidationException {
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        projectName = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        projectLocation = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        buildScriptName = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel4.setLabelFor(jPanel2);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "LBL_ProjectNameAndLocationLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jLabel4, gridBagConstraints);
        jLabel4.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSN_jLabel4")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSD_jLabel4")); // NOI18N

        jLabel5.setLabelFor(projectName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "LBL_NWP1_ProjectName_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        jPanel2.add(jLabel5, gridBagConstraints);
        jLabel5.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSN_projectNameLabel")); // NOI18N
        jLabel5.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSD_projectNameLabel")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        jPanel2.add(projectName, gridBagConstraints);

        jLabel6.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(PanelProjectLocationExtSrc.class).getString("LBL_NWP1_CreatedProjectFolder_LablelMnemonic").charAt(0));
        jLabel6.setLabelFor(projectLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "LBL_NWP1_CreatedProjectFolder_Lablel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        jPanel2.add(jLabel6, gridBagConstraints);
        jLabel6.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSN_projectLocationLabel")); // NOI18N
        jLabel6.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSD_projectLocationLabel")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        jPanel2.add(projectLocation, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "LBL_NWP1_BrowseLocation_Button3")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseProjectLocation(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        jPanel2.add(jButton3, gridBagConstraints);
        jButton3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSN_browseButton")); // NOI18N
        jButton3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSD_browseButton")); // NOI18N

        jLabel1.setLabelFor(buildScriptName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "MSG_BuildScriptName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        jPanel2.add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "AD_BuildScriptName")); // NOI18N

        buildScriptName.setText("\n");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        jPanel2.add(buildScriptName, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(jPanel2, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
        jPanel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSN_jPanel1")); // NOI18N
        jPanel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSD_jPanel1")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSN_PanelSourceFolders")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSD_PanelSourceFolders")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseProjectLocation(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseProjectLocation
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(NbBundle.getMessage(PanelSourceFolders.class, "LBL_NWP1_SelectProjectLocation")); // NOI18N
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String path = this.projectLocation.getText();
        if (path.length() > 0) {
            File f = new File(path);
            File owner = f.getParentFile();
            if (owner.exists()) {
                chooser.setCurrentDirectory(owner);
            }
            if (f.exists()) {
                chooser.setSelectedFile(f);
            }
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                this.projectLocation.setText(FileUtil.normalizeFile(file).getAbsolutePath());
            }
        }
    }//GEN-LAST:event_browseProjectLocation
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField buildScriptName;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField projectLocation;
    private javax.swing.JTextField projectName;
    // End of variables declaration//GEN-END:variables
}
