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

package org.netbeans.modules.projectimport.eclipse.core.wizard;

import java.io.File;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.projectimport.eclipse.core.EclipseUtils;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;

/**
 * Represent "Selection" step(panel) in the Eclipse importer wizard.
 *
 * @author mkrauskopf
 */
final class SelectionPanel extends JPanel {

    private String errorMessage;

    /** Creates new form ProjectSelectionPanel */
    public SelectionPanel() {
        super();
        initComponents();
        workspaceDir.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { workspaceChanged(); }
            public void removeUpdate(DocumentEvent e) { workspaceChanged(); }
            public void changedUpdate(DocumentEvent e) {}
        });
        projectDir.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { projectChanged(); }
            public void removeUpdate(DocumentEvent e) { projectChanged(); }
            public void changedUpdate(DocumentEvent e) {}
        });
        projectDestDir.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { projectChanged(); }
            public void removeUpdate(DocumentEvent e) { projectChanged(); }
            public void changedUpdate(DocumentEvent e) {}
        });
        setWorkspaceEnabled(workspaceButton.isSelected());
    }
    
    /** Returns workspace directory chosen by user, or null. */
    File getWorkspaceDir() {
        String d = workspaceDir.getText();
        if (d != null && d.trim().length() > 0) {
            return FileUtil.normalizeFile(new File(d.trim()));
        } else {
            return null;
        }
    }
    
    private void workspaceChanged() {
        File workspace = getWorkspaceDir();
        if (workspace == null) {
            setErrorMessage(ProjectImporterWizard.getMessage(
                    "MSG_ChooseWorkspace")); // NOI18N
            return;
        }
        boolean wsValid = EclipseUtils.isRegularWorkSpace(workspace);
        setErrorMessage(wsValid ? null :
            EclipseUtils.isRegularProject(workspace) ?
                ProjectImporterWizard.getMessage(
                "MSG_NotRegularWorkspaceButProject", workspace):
            ProjectImporterWizard.getMessage(
                "MSG_NotRegularWorkspace", workspace)); // NOI18N
    }
    
    private void projectChanged() {
        // check Eclipse project directory
        String project = getProjectDir();
        if ("".equals(project)) { // NOI18N
            setErrorMessage(ProjectImporterWizard.getMessage(
                    "MSG_ChooseProject")); // NOI18N
            return;
        }
        File projectDirFile = new File(project);
        if (!EclipseUtils.isRegularProject(projectDirFile)) {
            setErrorMessage(ProjectImporterWizard.getMessage(
                    "MSG_NotRegularProject", project)); // NOI18N
            return;
        }
        
        // check destination directory
        String projectDest = projectDestDir.getText().trim();
        if ("".equals(projectDest)) { // NOI18N
            setErrorMessage(ProjectImporterWizard.getMessage(
                    "MSG_ChooseProjectDestination")); // NOI18N
            return;
        }
        File projectDestFile = new File(projectDest, projectDirFile.getName());
        if (!projectDestFile.equals(projectDirFile) && projectDestFile.exists()) {
            setErrorMessage(ProjectImporterWizard.getMessage(
                    "MSG_ProjectExist", projectDestFile.getName())); // NOI18N
            return;
        }
        
        // valid
        setErrorMessage(null);
    }
    
    void setErrorMessage(String newMessage) {
        String oldMessage = this.errorMessage;
        this.errorMessage = newMessage;
        firePropertyChange("errorMessage", oldMessage, newMessage); // NOI18N
    }
    
    boolean isWorkspaceChosen() {
        return workspaceButton.isSelected();
    }
    
    /** Returns project directory of single-selected project. */
    public String getProjectDir() {
        return projectDir.getText().trim();
    }
    
    /** Returns destination directory for single-selected project. */
    public String getProjectDestinationDir() {
        if (projectDestDir.getText().trim().equals(projectDir.getText().trim())) {
            return null;
        } else {
            return projectDestDir.getText().trim();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        workspaceDir = new javax.swing.JTextField();
        worskpaceBrowse = new javax.swing.JButton();
        workSpaceLBL = new javax.swing.JLabel();
        projectDir = new javax.swing.JTextField();
        projectBrowse = new javax.swing.JButton();
        projectLBL = new javax.swing.JLabel();
        projectButton = new javax.swing.JRadioButton();
        workspaceButton = new javax.swing.JRadioButton();
        projectDestLBL = new javax.swing.JLabel();
        projectDestDir = new javax.swing.JTextField();
        projectDestBrowse = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        note = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(worskpaceBrowse, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "CTL_BrowseButton_B")); // NOI18N
        worskpaceBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                worskpaceBrowseActionPerformed(evt);
            }
        });

        workSpaceLBL.setLabelFor(workspaceDir);
        org.openide.awt.Mnemonics.setLocalizedText(workSpaceLBL, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "LBL_Workspace")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(projectBrowse, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "CTL_BrowseButton_R")); // NOI18N
        projectBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectBrowseActionPerformed(evt);
            }
        });

        projectLBL.setLabelFor(projectDir);
        org.openide.awt.Mnemonics.setLocalizedText(projectLBL, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "LBL_Project")); // NOI18N

        buttonGroup.add(projectButton);
        org.openide.awt.Mnemonics.setLocalizedText(projectButton, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "CTL_ProjectButton")); // NOI18N
        projectButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        projectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectButtonActionPerformed(evt);
            }
        });

        buttonGroup.add(workspaceButton);
        workspaceButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(workspaceButton, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "CTL_WorkspaceButton")); // NOI18N
        workspaceButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        workspaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                workspaceButtonActionPerformed(evt);
            }
        });

        projectDestLBL.setLabelFor(projectDestDir);
        org.openide.awt.Mnemonics.setLocalizedText(projectDestLBL, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "LBL_ProjectDestination")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(projectDestBrowse, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "CTL_BrowseButton_S")); // NOI18N
        projectDestBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectDestBrowseActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "LBL_SpecifyWorkspaceDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(note, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "LBL_NoteAboutWorkspaceAdvantage")); // NOI18N
        note.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(note, javax.swing.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(workSpaceLBL)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(workspaceDir, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(worskpaceBrowse))
                    .addComponent(workspaceButton)
                    .addComponent(projectButton)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(projectDestLBL)
                            .addComponent(projectLBL))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(projectDir, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                            .addComponent(projectDestDir, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(projectBrowse, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(projectDestBrowse, javax.swing.GroupLayout.Alignment.TRAILING)))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(workspaceButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(workSpaceLBL, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(worskpaceBrowse)
                    .addComponent(workspaceDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(projectButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLBL, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(projectBrowse)
                    .addComponent(projectDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectDestLBL, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(projectDestBrowse)
                    .addComponent(projectDestDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(note, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        workspaceDir.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        worskpaceBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        workSpaceLBL.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        projectDir.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        projectBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        projectLBL.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        projectButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        workspaceButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        projectDestLBL.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        projectDestDir.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        projectDestBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        note.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    private void projectDestBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectDestBrowseActionPerformed
        File dir = new FileChooserBuilder(SelectionPanel.class).setDirectoriesOnly(true).showOpenDialog();
        if (dir != null) {
            projectDestDir.setText(dir.getAbsolutePath());
        }//GEN-LAST:event_projectDestBrowseActionPerformed
    }                                                 
            
    private void projectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectButtonActionPerformed
        setWorkspaceEnabled(false);
        projectChanged();
        projectDir.requestFocusInWindow();
        firePropertyChange("workspaceChoosen", true, false); // NOI18N//GEN-LAST:event_projectButtonActionPerformed
    }                                             
    
    private void workspaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_workspaceButtonActionPerformed
        setWorkspaceEnabled(true);
        workspaceChanged();
        firePropertyChange("workspaceChoosen", false, true); // NOI18N//GEN-LAST:event_workspaceButtonActionPerformed
    }                                               
    
    private void setWorkspaceEnabled(boolean enabled) {
        workSpaceLBL.setEnabled(enabled);
        worskpaceBrowse.setEnabled(enabled);
        workspaceDir.setEnabled(enabled);
        projectLBL.setEnabled(!enabled);
        projectBrowse.setEnabled(!enabled);
        projectDir.setEnabled(!enabled);
        projectDestBrowse.setEnabled(!enabled);
        projectDestDir.setEnabled(!enabled);
        projectDestLBL.setEnabled(!enabled);
        note.setVisible(!enabled);
    }
    
    private void projectBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectBrowseActionPerformed
        File file = new FileChooserBuilder(SelectionPanel.class).setDirectoriesOnly(true).forceUseOfDefaultWorkingDirectory(true).showOpenDialog();
        if (file != null) {
            projectDir.setText (file.getAbsolutePath());
        }//GEN-LAST:event_projectBrowseActionPerformed
    }                                             
    
    private void worskpaceBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_worskpaceBrowseActionPerformed
        File file = new FileChooserBuilder(SelectionPanel.class + "_wksp").setDirectoriesOnly(true).showOpenDialog(); //NOI18N
        if (file != null) {
            workspaceDir.setText(file.getAbsolutePath());
        }//GEN-LAST:event_worskpaceBrowseActionPerformed
    }                                               
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel note;
    private javax.swing.JButton projectBrowse;
    private javax.swing.JRadioButton projectButton;
    private javax.swing.JButton projectDestBrowse;
    private javax.swing.JTextField projectDestDir;
    private javax.swing.JLabel projectDestLBL;
    private javax.swing.JTextField projectDir;
    private javax.swing.JLabel projectLBL;
    private javax.swing.JLabel workSpaceLBL;
    private javax.swing.JRadioButton workspaceButton;
    private javax.swing.JTextField workspaceDir;
    private javax.swing.JButton worskpaceBrowse;
    // End of variables declaration//GEN-END:variables
}
