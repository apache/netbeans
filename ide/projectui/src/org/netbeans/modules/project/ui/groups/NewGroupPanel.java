/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.project.ui.groups;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import static org.netbeans.modules.project.ui.groups.Bundle.*;
import static org.netbeans.modules.project.ui.groups.ManageGroupsPanel.NONE_GOUP;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.NotificationLineSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 * Panel permitting user to create a new project group.
 * Applicable in advanced mode.
 * @author Jesse Glick
 */
public class NewGroupPanel extends JPanel {

    public static final String PROP_READY = "ready"; // NOI18N
    static final int MAX_NAME = 50;

    public NewGroupPanel() {
        initComponents();
        DocumentListener l = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                firePropertyChange(PROP_READY, null, null);
            }
            @Override public void removeUpdate(DocumentEvent e) {
                firePropertyChange(PROP_READY, null, null);
            }
            @Override public void changedUpdate(DocumentEvent e) {}
        };
        directoryField.getDocument().addDocumentListener(l);
        nameField.setText("Group " + new SimpleDateFormat("yyyyMMdd-hh:mm:ss").format(new Date()));
        nameField.getDocument().addDocumentListener(l);
        nameField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                nameConstraintsWarnings();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                nameConstraintsWarnings();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                nameConstraintsWarnings();
            }
        });
    }
    
    private void nameConstraintsWarnings() {
        updateNotifications();
    }

    public boolean isReady() {
        //sort of suboptional to have the isReady method to duplicate the checks in updateNotifications();
        String name = nameField.getText();
        if (name != null) {
            if (name.trim().length() <= 0 || name.trim().length() >= MAX_NAME) {
                return false;
            }
            if (name.equalsIgnoreCase(NONE_GOUP)) {
                return false;
            }
            for (Group group : Group.allGroups()) {
                if (name.equalsIgnoreCase(group.getName())) {
                    return false;
                }
            }
        }
        
        if (subprojectsKindRadio.isSelected()) {
            String s = masterProjectField.getText();
            if (s != null && s.length() > 0) {
                File f = FileUtil.normalizeFile(new File(s));
                FileObject fo = FileUtil.toFileObject(f);
                if (fo != null && fo.isFolder()) {
                    try {
                        return ProjectManager.getDefault().findProject(fo) != null;
                    } catch (IOException x) {
                        Exceptions.printStackTrace(x);
                    }
                }
            }
            return false;
        } else if (directoryKindRadio.isSelected()) {
            String s = directoryField.getText();
            if (s != null) {
                return new File(s.trim()).isDirectory();
            } else {
                return false;
            }
        }
        return true;
    }

    private void updateNameField() {
        if (adHocKindRadio.isSelected() && useOpenCheckbox.isSelected()) {
            Project p = OpenProjects.getDefault().getMainProject();
            if (p != null && nameField.getText().length() == 0) {
                nameField.setText(ProjectUtils.getInformation(p).getDisplayName());
            }
        } else if (subprojectsKindRadio.isSelected()) {
            String s = masterProjectField.getText();
            if (s != null && s.length() > 0) {
                File f = new File(s);
                FileObject fo = FileUtil.toFileObject(f);
                if (fo != null && fo.isFolder()) {
                    try {
                        Project p = ProjectManager.getDefault().findProject(fo);
                        if (p != null) {
                            nameField.setText(ProjectUtils.getInformation(p).getDisplayName());
                        }
                    } catch (IOException x) {
                        Exceptions.printStackTrace(x);
                    }
                }
            }
        } else if (directoryKindRadio.isSelected()) {
            String s = directoryField.getText();
            if (s != null && s.length() > 0) {
                File f = new File(s);
                nameField.setText(f.getName());
            }
        }
    }
    
    public enum Type {
        ADHOC, SUB, DIR
    }
    
    public Type getSelectedType() {
        if (adHocKindRadio.isSelected()) {
            return Type.ADHOC;
        }
        if (subprojectsKindRadio.isSelected()) {
            return Type.SUB;
        }
        if (directoryKindRadio.isSelected()) {
            return Type.DIR;
        }
        throw new IllegalStateException();
    }
    
    public String getNameField() {
        return nameField.getText().trim();
    }
    
    public boolean isAutoSyncField() {
        return autoSynchCheckbox.isSelected();
    }
    
    public boolean isUseOpenedField() {
        return useOpenCheckbox.isSelected();
    }
    
    public String getMasterProjectField() {
        return masterProjectField.getText();
    }
    
    public String getDirectoryField() {
        return directoryField.getText() != null ? directoryField.getText().trim() : null;
    }
    

    public static Group create(Type type, String name, boolean autoSync, boolean useOpen, String masterProject, String directory) {
        if (Type.ADHOC == type) {
            AdHocGroup g = AdHocGroup.create(name, autoSync);
            if (useOpen) {
                g.setProjects(new HashSet<Project>(Arrays.asList(OpenProjects.getDefault().getOpenProjects())));
                g.setMainProject(OpenProjects.getDefault().getMainProject());
            }
            return g;
        } else if (Type.SUB == type) {
            FileObject fo = FileUtil.toFileObject(new File(masterProject));
            try {
                return SubprojectsGroup.create(name, ProjectManager.getDefault().findProject(fo));
            } catch (IOException x) {
                throw new AssertionError(x);
            }
        } else {
            assert Type.DIR == type;
            FileObject f = FileUtil.toFileObject(FileUtil.normalizeFile(new File(directory)));
            return DirectoryGroup.create(name, f);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        kindButtonGroup = new javax.swing.ButtonGroup();
        adHocKindRadio = new javax.swing.JRadioButton();
        adHocKindLabel = new javax.swing.JLabel();
        useOpenCheckbox = new javax.swing.JCheckBox();
        autoSynchCheckbox = new javax.swing.JCheckBox();
        subprojectsKindRadio = new javax.swing.JRadioButton();
        subprojectsKindLabel = new javax.swing.JLabel();
        masterProjectLabel = new javax.swing.JLabel();
        masterProjectField = new javax.swing.JTextField();
        masterProjectButton = new javax.swing.JButton();
        directoryKindRadio = new javax.swing.JRadioButton();
        directoryKindLabel = new javax.swing.JLabel();
        directoryLabel = new javax.swing.JLabel();
        directoryField = new javax.swing.JTextField();
        directoryButton = new javax.swing.JButton();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();

        kindButtonGroup.add(adHocKindRadio);
        adHocKindRadio.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(adHocKindRadio, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.adHocKindRadio.text")); // NOI18N
        adHocKindRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        adHocKindRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adHocKindRadioActionPerformed(evt);
            }
        });

        adHocKindLabel.setLabelFor(adHocKindRadio);
        org.openide.awt.Mnemonics.setLocalizedText(adHocKindLabel, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.adHocKindLabel.text")); // NOI18N

        useOpenCheckbox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(useOpenCheckbox, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.useOpenCheckbox.text")); // NOI18N
        useOpenCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useOpenCheckboxActionPerformed(evt);
            }
        });

        autoSynchCheckbox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(autoSynchCheckbox, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.autoSynchCheckbox.text")); // NOI18N

        kindButtonGroup.add(subprojectsKindRadio);
        org.openide.awt.Mnemonics.setLocalizedText(subprojectsKindRadio, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.subprojectsKindRadio.text")); // NOI18N
        subprojectsKindRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        subprojectsKindRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subprojectsKindRadioActionPerformed(evt);
            }
        });

        subprojectsKindLabel.setLabelFor(subprojectsKindRadio);
        org.openide.awt.Mnemonics.setLocalizedText(subprojectsKindLabel, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.subprojectsKindLabel.text")); // NOI18N
        subprojectsKindLabel.setEnabled(false);

        masterProjectLabel.setLabelFor(masterProjectField);
        org.openide.awt.Mnemonics.setLocalizedText(masterProjectLabel, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.masterProjectLabel.text")); // NOI18N
        masterProjectLabel.setEnabled(false);

        masterProjectField.setEditable(false);
        masterProjectField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(masterProjectButton, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.masterProjectButton.text")); // NOI18N
        masterProjectButton.setEnabled(false);
        masterProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                masterProjectButtonActionPerformed(evt);
            }
        });

        kindButtonGroup.add(directoryKindRadio);
        org.openide.awt.Mnemonics.setLocalizedText(directoryKindRadio, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.directoryKindRadio.text")); // NOI18N
        directoryKindRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        directoryKindRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directoryKindRadioActionPerformed(evt);
            }
        });

        directoryKindLabel.setLabelFor(directoryKindRadio);
        org.openide.awt.Mnemonics.setLocalizedText(directoryKindLabel, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.directoryKindLabel.text")); // NOI18N
        directoryKindLabel.setEnabled(false);

        directoryLabel.setLabelFor(directoryField);
        org.openide.awt.Mnemonics.setLocalizedText(directoryLabel, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.directoryLabel.text")); // NOI18N
        directoryLabel.setEnabled(false);

        directoryField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(directoryButton, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.directoryButton.text")); // NOI18N
        directoryButton.setEnabled(false);
        directoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directoryButtonActionPerformed(evt);
            }
        });

        nameLabel.setLabelFor(nameField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.nameLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nameField))
                    .addComponent(directoryKindRadio)
                    .addComponent(adHocKindRadio)
                    .addComponent(subprojectsKindRadio)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(directoryLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(directoryField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(directoryButton))
                            .addComponent(directoryKindLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(masterProjectLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(masterProjectField))
                                    .addComponent(subprojectsKindLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(masterProjectButton))
                            .addComponent(adHocKindLabel)
                            .addComponent(autoSynchCheckbox)
                            .addComponent(useOpenCheckbox))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(adHocKindRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(adHocKindLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useOpenCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(autoSynchCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(subprojectsKindRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(subprojectsKindLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(masterProjectLabel)
                    .addComponent(masterProjectButton)
                    .addComponent(masterProjectField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(directoryKindRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(directoryKindLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(directoryLabel)
                    .addComponent(directoryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(directoryButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        adHocKindRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.adHocKindRadio.AccessibleContext.accessibleDescription")); // NOI18N
        adHocKindLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.adHocKindLabel.AccessibleContext.accessibleDescription")); // NOI18N
        useOpenCheckbox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.useOpenCheckbox.AccessibleContext.accessibleDescription")); // NOI18N
        autoSynchCheckbox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.autoSynchCheckbox.AccessibleContext.accessibleDescription")); // NOI18N
        subprojectsKindRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.subprojectsKindRadio.AccessibleContext.accessibleDescription")); // NOI18N
        subprojectsKindLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.subprojectsKindLabel.AccessibleContext.accessibleDescription")); // NOI18N
        masterProjectLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.masterProjectLabel.AccessibleContext.accessibleDescription")); // NOI18N
        masterProjectField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.masterProjectField.AccessibleContext.accessibleName")); // NOI18N
        masterProjectField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.masterProjectField.AccessibleContext.accessibleDescription")); // NOI18N
        masterProjectButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.masterProjectButton.AccessibleContext.accessibleDescription")); // NOI18N
        directoryKindRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.directoryKindRadio.AccessibleContext.accessibleDescription")); // NOI18N
        directoryKindLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.directoryKindLabel.AccessibleContext.accessibleDescription")); // NOI18N
        directoryLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.directoryLabel.AccessibleContext.accessibleDescription")); // NOI18N
        directoryField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.directoryField.AccessibleContext.accessibleName")); // NOI18N
        directoryField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.directoryField.AccessibleContext.accessibleDescription")); // NOI18N
        directoryButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.directoryButton.AccessibleContext.accessibleDescription")); // NOI18N
        nameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.nameLabel.AccessibleContext.accessibleDescription")); // NOI18N
        nameField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.nameField.AccessibleContext.accessibleName")); // NOI18N
        nameField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.nameField.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void directoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_directoryButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        File start = ProjectChooser.getProjectsFolder();
        if (directoryField.getText() != null && directoryField.getText().trim().length() > 0) {
            start = new File(directoryField.getText().trim());
        }
        chooser.setCurrentDirectory(start);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (f != null) {
                directoryField.setText(f.getAbsolutePath());
                updateNameField();
            }
        }
    }//GEN-LAST:event_directoryButtonActionPerformed

    private void masterProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_masterProjectButtonActionPerformed
        JFileChooser chooser = ProjectChooser.projectChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (f != null) {
                masterProjectField.setText(f.getAbsolutePath());
                updateNameField();
                firePropertyChange(PROP_READY, null, null);
            }
        }
    }//GEN-LAST:event_masterProjectButtonActionPerformed

    private void directoryKindRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_directoryKindRadioActionPerformed
        adHocKindLabel.setEnabled(false);
        useOpenCheckbox.setEnabled(false);
        autoSynchCheckbox.setEnabled(false);
        subprojectsKindLabel.setEnabled(false);
        masterProjectLabel.setEnabled(false);
        masterProjectField.setEnabled(false);
        masterProjectButton.setEnabled(false);
        directoryKindLabel.setEnabled(true);
        directoryLabel.setEnabled(true);
        directoryField.setEnabled(true);
        directoryButton.setEnabled(true);
        updateNameField();
        firePropertyChange(PROP_READY, null, null);
        updateNotifications();
    }//GEN-LAST:event_directoryKindRadioActionPerformed

    private void subprojectsKindRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subprojectsKindRadioActionPerformed
        adHocKindLabel.setEnabled(false);
        useOpenCheckbox.setEnabled(false);
        autoSynchCheckbox.setEnabled(false);
        subprojectsKindLabel.setEnabled(true);
        masterProjectLabel.setEnabled(true);
        masterProjectField.setEnabled(true);
        masterProjectButton.setEnabled(true);
        directoryKindLabel.setEnabled(false);
        directoryLabel.setEnabled(false);
        directoryField.setEnabled(false);
        directoryButton.setEnabled(false);
        updateNameField();
        firePropertyChange(PROP_READY, null, null);
        updateNotifications();
    }//GEN-LAST:event_subprojectsKindRadioActionPerformed

    private void adHocKindRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adHocKindRadioActionPerformed
        adHocKindLabel.setEnabled(true);
        useOpenCheckbox.setEnabled(true);
        autoSynchCheckbox.setEnabled(true);
        subprojectsKindLabel.setEnabled(false);
        masterProjectLabel.setEnabled(false);
        masterProjectField.setEnabled(false);
        masterProjectButton.setEnabled(false);
        directoryKindLabel.setEnabled(false);
        directoryLabel.setEnabled(false);
        directoryField.setEnabled(false);
        directoryButton.setEnabled(false);
        updateNameField();
        firePropertyChange(PROP_READY, null, null);
        updateNotifications();
    }//GEN-LAST:event_adHocKindRadioActionPerformed

    private void useOpenCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useOpenCheckboxActionPerformed
        updateNotifications();
    }//GEN-LAST:event_useOpenCheckboxActionPerformed

    private NotificationLineSupport notificationLineSupport;
    void setNotificationLineSupport(NotificationLineSupport notificationLineSupport) {
        this.notificationLineSupport = notificationLineSupport;
        updateNameField();
    }
    
    @Messages({"NewGroupPanel.open_project_warning=The list of projects currently open will be lost, unless you make a free group for them first.",
               "NewGroupPanel.too_long_warning=Group name is too long.",
               "NewGroupPanel.exists_warning=Name equal to existing group."})
    private void updateNotifications() { // #192899
        assert notificationLineSupport != null;
        notificationLineSupport.clearMessages();
        if (adHocKindRadio.isSelected() && useOpenCheckbox.isSelected() || OpenProjects.getDefault().getOpenProjects().length == 0) {
        } else {
            notificationLineSupport.setWarningMessage(NewGroupPanel_open_project_warning());
        }
        String name = nameField.getText();
        if (name != null) {
            if (name.length() > MAX_NAME) {
                notificationLineSupport.setErrorMessage(NewGroupPanel_too_long_warning());
            }
            if (name.equalsIgnoreCase(NONE_GOUP)) {
                notificationLineSupport.setErrorMessage(NewGroupPanel_exists_warning());
            }
            for (Group group : Group.allGroups()) {
                if (name.equalsIgnoreCase(group.getName())) {
                    notificationLineSupport.setErrorMessage(NewGroupPanel_exists_warning());
                    break;
                }
            }
        }
        
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel adHocKindLabel;
    private javax.swing.JRadioButton adHocKindRadio;
    private javax.swing.JCheckBox autoSynchCheckbox;
    private javax.swing.JButton directoryButton;
    private javax.swing.JTextField directoryField;
    private javax.swing.JLabel directoryKindLabel;
    private javax.swing.JRadioButton directoryKindRadio;
    private javax.swing.JLabel directoryLabel;
    private javax.swing.ButtonGroup kindButtonGroup;
    private javax.swing.JButton masterProjectButton;
    private javax.swing.JTextField masterProjectField;
    private javax.swing.JLabel masterProjectLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel subprojectsKindLabel;
    private javax.swing.JRadioButton subprojectsKindRadio;
    private javax.swing.JCheckBox useOpenCheckbox;
    // End of variables declaration//GEN-END:variables
    
}
