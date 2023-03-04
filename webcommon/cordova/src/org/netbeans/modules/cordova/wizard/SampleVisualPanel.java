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
package org.netbeans.modules.cordova.wizard;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

class SampleVisualPanel extends JPanel {

    private static final long serialVersionUID = 6783546871135477L;
    private final List<ChangeListener> listeners;


    public SampleVisualPanel(WizardDescriptor descriptor) {
        initComponents();
        initProjectNameAndLocation(descriptor);
        listeners = new CopyOnWriteArrayList<ChangeListener>();
    }

    private void initProjectNameAndLocation(WizardDescriptor descriptor) {
        // default name & location
        File projectLocation = ProjectChooser.getProjectsFolder();
        projectLocationTextField.setText(projectLocation.getAbsolutePath());

        FileObject template = Templates.getTemplate(descriptor);
        String projectName = template.getName();
        String templateName = projectName;
        int index = 0;
        while ((new File(projectLocation, projectName)).exists()) {
            index++;
            projectName = templateName + index;
        }
        projectNameTextField.setText(projectName);
        projectNameTextField.selectAll();
        updateProjectFolder();

        // listeners
        DocumentListener documentListener = new DefaultDocumentListener();
        projectNameTextField.getDocument().addDocumentListener(documentListener);
        projectLocationTextField.getDocument().addDocumentListener(documentListener);
        setName(NbBundle.getMessage(SampleVisualPanel.class, "LBL_NameAndLocation"));// NOI18N
        
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // same problem as in #31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
    }

    public String getProjectName() {
        return projectNameTextField.getText().trim();
    }

    public String getProjectLocation() {
        return projectLocationTextField.getText().trim();
    }

    public File getProjectDirectory() {
        return FileUtil.normalizeFile(new File(createdFolderTextField.getText()));
    }

    final void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    final void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    String getErrorMessage() {
        String error = validateProjectName();
        if (error != null) {
            return error;
        }
        error = validateProjectLocation();
        if (error != null) {
            return error;
        }
        return null;
    }

    private String validateProjectName() {
        String projectName = getProjectName();
        if (projectName.isEmpty()) {
            return NbBundle.getMessage(SampleVisualPanel.class, "ERR_NameMissing"); //NOI18N
        }
        return null;
    }

    private String validateProjectLocation() {
        File projectLocation = FileUtil.normalizeFile(new File(getProjectLocation()).getAbsoluteFile());
        if (!projectLocation.isDirectory()) {
            return NbBundle.getMessage(SampleVisualPanel.class, "ERR_LocationInvalid"); // NOI18N
        }
        final File destFolder = getProjectDirectory();
        try {
            destFolder.getCanonicalPath();
        } catch (IOException e) {
            return NbBundle.getMessage(SampleVisualPanel.class, "ERR_LocationNotWritable"); // NOI18N
        }

        File projLoc = destFolder;
        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            return NbBundle.getMessage(SampleVisualPanel.class, "ERR_LocationNotWritable"); // NOI18N
        }

        if (FileUtil.toFileObject(projLoc) == null) {
            return NbBundle.getMessage(SampleVisualPanel.class, "ERR_LocationInvalid"); // NOI18N
        }

        File[] kids = destFolder.listFiles();
        if (destFolder.exists() && kids != null && kids.length > 0) {
            return NbBundle.getMessage(SampleVisualPanel.class, "ERR_LocationNotEmpty"); // NOI18N
        }
        return null;
    }

    private void updateProjectFolder() {
        createdFolderTextField.setText(getProjectLocation() + File.separatorChar + getProjectName());
    }

    private void fireChange() {
        for (ChangeListener listener : listeners) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
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

        projectNameLabel.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(SampleVisualPanel.class, "LBL_ProjectName")); // NOI18N

        projectLocationLabel.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(SampleVisualPanel.class, "LBL_ProjectLocation")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(SampleVisualPanel.class, "LBL_Browse")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        createdFolderLabel.setLabelFor(createdFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdFolderLabel, org.openide.util.NbBundle.getMessage(SampleVisualPanel.class, "LBL_ProjectFolder")); // NOI18N

        createdFolderTextField.setEditable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(projectNameLabel)
                    .addComponent(projectLocationLabel)
                    .addComponent(createdFolderLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(projectNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                    .addComponent(projectLocationTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(createdFolderTextField, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browseButton))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectNameLabel)
                    .addComponent(projectNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLocationLabel)
                    .addComponent(projectLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createdFolderLabel)
                    .addComponent(createdFolderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        projectNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SampleVisualPanel.class, "SampleVisualPanel.projectNameLabel.AccessibleContext.accessibleName")); // NOI18N
        projectLocationLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SampleVisualPanel.class, "SampleVisualPanel.projectLocationLabel.AccessibleContext.accessibleName")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SampleVisualPanel.class, "LBL_Browse")); // NOI18N
        createdFolderLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SampleVisualPanel.class, "LBL_ProjectFolder")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SampleVisualPanel.class, "LBL_ProjectLocation")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        File workDir = null;
        String projectLocation = getProjectLocation();
        if (projectLocation != null && !projectLocation.isEmpty()) {
            File projDir = new File(projectLocation);
            if (projDir.isDirectory()) {
                workDir = projDir;
            }
        }
        if (workDir == null) {
            workDir = ProjectChooser.getProjectsFolder();
        }
        File projectDir = new FileChooserBuilder(SampleVisualPanel.class)
                .setTitle(NbBundle.getMessage(SampleVisualPanel.class, "TTL_DialogLocation"))   // NOI18N
                .setDirectoriesOnly(true)
                .setDefaultWorkingDirectory(workDir)
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (projectDir != null) {
            projectLocationTextField.setText(FileUtil.normalizeFile(projectDir).getAbsolutePath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class DefaultDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processChange();
        }

        private void processChange() {
            updateProjectFolder();
            fireChange();
        }

    }
}
