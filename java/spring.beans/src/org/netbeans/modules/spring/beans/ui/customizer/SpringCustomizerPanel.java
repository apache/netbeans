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

package org.netbeans.modules.spring.beans.ui.customizer;

import java.io.File;
import java.util.*;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.netbeans.modules.spring.beans.ui.customizer.ConfigFilesUIs.FileDisplayName;
import org.netbeans.modules.spring.util.ConfigFiles;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.InputLine;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class SpringCustomizerPanel extends javax.swing.JPanel implements HelpCtx.Provider {

    private final Project project;
    private final List<File> files;
    private final List<ConfigFileGroup> groups;
    private final FileDisplayName fileDisplayName;

    // made static to be able to remember last location
    private static File basedir;

    private ConfigFileGroup currentGroup;
    private int currentGroupIndex;

    private List<File> detectedFiles;

    public SpringCustomizerPanel(Project project, List<File> files, List<ConfigFileGroup> groups) {
        this.project = project;
        this.files = files;
        this.groups = groups;
        basedir = FileUtil.toFile(project.getProjectDirectory());
        if (basedir == null) {
            throw new IllegalStateException("The directory of project " + project + " is null");
        }
        fileDisplayName = new RelativeDisplayName();
        initComponents();
        ConfigFilesUIs.setupFilesList(filesList, fileDisplayName);
        ConfigFilesUIs.setupGroupsList(groupsList);
        ConfigFilesUIs.setupFilesList(groupFilesList, fileDisplayName);
        ConfigFilesUIs.connectFilesList(files, filesList);
        ConfigFilesUIs.connectGroupsList(groups, groupsList);
        filesList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                filesListSelectionChanged();
            }
        });
        groupsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                groupsListSelectionChanged();
            }
        });
        groupFilesList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                groupFilesListSelectionChanged();
            }
        });
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(SpringCustomizerPanel.class);
    }

    public List<File> getConfigFiles() {
        return files;
    }

    public List<ConfigFileGroup> getConfigFileGroups() {
        return groups;
    }

    private void filesListSelectionChanged() {
        boolean selected = filesList.getSelectedIndex() != -1;
        removeFileButton.setEnabled(selected);
    }

    private void groupsListSelectionChanged() {
        currentGroupIndex = groupsList.getSelectedIndex();
        if (currentGroupIndex != -1) {
            currentGroup = (ConfigFileGroup)groupsList.getModel().getElementAt(currentGroupIndex);
            ConfigFilesUIs.connectFilesList(currentGroup.getFiles(), groupFilesList);
            editGroupButton.setEnabled(true);
            removeGroupButton.setEnabled(true);
            addGroupFilesButton.setEnabled(true);
            detectFilesButton.setEnabled(true);
            String currentGroupName = ConfigFilesUIs.getGroupName(currentGroup);
            groupFilesLabel.setText(NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_ConfigFilesInGroup", currentGroupName));
            groupFilesList.setSelectedIndices(new int[0]);
        } else {
            currentGroup = null;
            ConfigFilesUIs.disconnect(groupFilesList);
            editGroupButton.setEnabled(false);
            removeGroupButton.setEnabled(false);
            addGroupFilesButton.setEnabled(false);
            detectFilesButton.setEnabled(false);
            groupFilesLabel.setText(NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_ConfigFiles"));
        }
    }

    private void groupFilesListSelectionChanged() {
        boolean selected = groupFilesList.getSelectedIndex() != -1;
        removeGroupFileButton.setEnabled(selected);
    }

    private void replaceCurrentGroup(ConfigFileGroup newGroup) {
        groups.set(currentGroupIndex, newGroup);
        int selIndex = currentGroupIndex;
        ConfigFilesUIs.connectGroupsList(groups, groupsList);
        groupsList.setSelectedIndex(selIndex);
    }

    private void addFiles(List<File> newFiles) {
        files.addAll(newFiles);
        ConfigFilesUIs.connectFilesList(files, filesList);
        filesList.setSelectedIndex(filesList.getModel().getSize() - 1);
    }

    private void removeFiles() {
        List<File> removedFiles = new ArrayList<File>();
        int[] indices = filesList.getSelectedIndices();
        for (int i = 0; i < indices.length; i++) {
            removedFiles.add(files.remove(indices[i] - i));
        }
        for (int i = 0; i < groups.size(); i++) {
            ConfigFileGroup group = groups.get(i);
            List<File> groupFiles = group.getFiles();
            if (groupFiles.removeAll(removedFiles)) {
                ConfigFileGroup newGroup = ConfigFileGroup.create(group.getName(), groupFiles);
                if (currentGroup == group) {
                    replaceCurrentGroup(newGroup);
                } else {
                    groups.set(i, newGroup);
                }
            }
        }
        ConfigFilesUIs.connectFilesList(files, filesList);
        int selIndex = Math.min(indices[0], filesList.getModel().getSize() - 1);
        filesList.setSelectedIndex(selIndex);
    }

    private void addGroup(ConfigFileGroup group) {
        groups.add(group);
        ConfigFilesUIs.connectGroupsList(groups, groupsList);
        groupsList.setSelectedIndex(groupsList.getModel().getSize() - 1);
    }

    private void removeGroups() {
        int[] indices = groupsList.getSelectedIndices();
        for (int i = 0; i < indices.length; i++) {
            groups.remove(indices[i] - i);
        }
        ConfigFilesUIs.connectGroupsList(groups, groupsList);
        int selIndex = Math.min(indices[0], groupsList.getModel().getSize() - 1);
        groupsList.setSelectedIndex(selIndex);
    }

    private void addFilesToCurrentGroup(List<File> newFiles) {
        List<File> groupFiles = currentGroup.getFiles();
        groupFiles.addAll(newFiles);
        ConfigFileGroup newGroup = ConfigFileGroup.create(currentGroup.getName(), groupFiles);
        replaceCurrentGroup(newGroup);
        groupFilesList.setSelectedIndex(groupFilesList.getModel().getSize() - 1);
    }

    private void removeFilesFromCurrentGroup() {
        List<File> groupFiles = currentGroup.getFiles();
        int[] indices = groupFilesList.getSelectedIndices();
        for (int i = 0; i < indices.length; i++) {
            groupFiles.remove(indices[i] - i);
        }
        replaceCurrentGroup(ConfigFileGroup.create(currentGroup.getName(), groupFiles));
        int selIndex = Math.min(indices[0], groupFilesList.getModel().getSize() - 1);
        groupFilesList.setSelectedIndex(selIndex);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        springConfigPane = new javax.swing.JTabbedPane();
        configFilesPanel = new javax.swing.JPanel();
        filesLabel = new javax.swing.JLabel();
        filesScrollPane = new javax.swing.JScrollPane();
        filesList = new javax.swing.JList();
        addFileButton = new javax.swing.JButton();
        removeFileButton = new javax.swing.JButton();
        detectFilesButton = new javax.swing.JButton();
        configFileGroupsPanel = new javax.swing.JPanel();
        groupsLabel = new javax.swing.JLabel();
        groupsScrollPane = new javax.swing.JScrollPane();
        groupsList = new javax.swing.JList();
        addGroupButton = new javax.swing.JButton();
        editGroupButton = new javax.swing.JButton();
        removeGroupButton = new javax.swing.JButton();
        groupFilesLabel = new javax.swing.JLabel();
        groupFilesScrollPane = new javax.swing.JScrollPane();
        groupFilesList = new javax.swing.JList();
        addGroupFilesButton = new javax.swing.JButton();
        removeGroupFileButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(filesLabel, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_ConfigFiles")); // NOI18N

        filesScrollPane.setViewportView(filesList);

        org.openide.awt.Mnemonics.setLocalizedText(addFileButton, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_AddFile")); // NOI18N
        addFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFileButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeFileButton, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_RemoveFile")); // NOI18N
        removeFileButton.setEnabled(false);
        removeFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeFileButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(detectFilesButton, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_DetectFiles")); // NOI18N
        detectFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                detectFilesButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout configFilesPanelLayout = new javax.swing.GroupLayout(configFilesPanel);
        configFilesPanel.setLayout(configFilesPanelLayout);
        configFilesPanelLayout.setHorizontalGroup(
            configFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configFilesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(configFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(filesLabel)
                    .addComponent(filesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(removeFileButton, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                    .addComponent(detectFilesButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addFileButton, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE))
                .addContainerGap())
        );

        configFilesPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addFileButton, detectFilesButton, removeFileButton});

        configFilesPanelLayout.setVerticalGroup(
            configFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configFilesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(filesLabel)
                .addGap(6, 6, 6)
                .addGroup(configFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(configFilesPanelLayout.createSequentialGroup()
                        .addComponent(addFileButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(detectFilesButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeFileButton))
                    .addComponent(filesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE))
                .addContainerGap())
        );

        springConfigPane.addTab(org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_ConfigFilesTitle"), configFilesPanel); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(groupsLabel, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_ConfigFileGroups")); // NOI18N
        groupsLabel.setFocusable(false);

        groupsScrollPane.setViewportView(groupsList);

        org.openide.awt.Mnemonics.setLocalizedText(addGroupButton, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_AddGroup")); // NOI18N
        addGroupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addGroupButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(editGroupButton, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_EditGroup")); // NOI18N
        editGroupButton.setEnabled(false);
        editGroupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editGroupButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeGroupButton, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_RemoveGroup")); // NOI18N
        removeGroupButton.setEnabled(false);
        removeGroupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeGroupButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(groupFilesLabel, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_ConfigFiles")); // NOI18N
        groupFilesLabel.setFocusable(false);

        groupFilesScrollPane.setViewportView(groupFilesList);

        org.openide.awt.Mnemonics.setLocalizedText(addGroupFilesButton, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_AddFiles")); // NOI18N
        addGroupFilesButton.setEnabled(false);
        addGroupFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addGroupFilesButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeGroupFileButton, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_RemoveFile")); // NOI18N
        removeGroupFileButton.setEnabled(false);
        removeGroupFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeGroupFileButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout configFileGroupsPanelLayout = new javax.swing.GroupLayout(configFileGroupsPanel);
        configFileGroupsPanel.setLayout(configFileGroupsPanelLayout);
        configFileGroupsPanelLayout.setHorizontalGroup(
            configFileGroupsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configFileGroupsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(configFileGroupsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(groupFilesLabel)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, configFileGroupsPanelLayout.createSequentialGroup()
                        .addGroup(configFileGroupsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(groupFilesScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                            .addComponent(groupsLabel, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(groupsScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(configFileGroupsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(removeGroupButton, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(editGroupButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addGroupButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addGroupFilesButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(removeGroupFileButton))))
                .addContainerGap())
        );

        configFileGroupsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addGroupButton, addGroupFilesButton, editGroupButton, removeGroupButton, removeGroupFileButton});

        configFileGroupsPanelLayout.setVerticalGroup(
            configFileGroupsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configFileGroupsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(groupsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configFileGroupsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(configFileGroupsPanelLayout.createSequentialGroup()
                        .addComponent(addGroupButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editGroupButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeGroupButton))
                    .addGroup(configFileGroupsPanelLayout.createSequentialGroup()
                        .addComponent(groupsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(groupFilesLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configFileGroupsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(groupFilesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                    .addGroup(configFileGroupsPanelLayout.createSequentialGroup()
                        .addComponent(addGroupFilesButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeGroupFileButton)))
                .addContainerGap())
        );

        springConfigPane.addTab(org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_ConfigFileGroupsTitle"), configFileGroupsPanel); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(springConfigPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(springConfigPane)
        );
    }// </editor-fold>//GEN-END:initComponents

private void addFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFileButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        chooser.setDialogTitle(NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_ChooseFile")); //NOI18N
        chooser.setCurrentDirectory(basedir);
        int option = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(groupFilesList));
        if (option == JFileChooser.APPROVE_OPTION) {
            boolean showDialog = false;
            List<File> newFiles = new LinkedList<File>();
            StringBuilder existing = new StringBuilder(
                    NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_FileAlreadyAdded")).append("\n"); //NOI18N
            for (File file : chooser.getSelectedFiles()) {
                if (files.contains(file)) {
                    existing.append(file.getAbsolutePath()).append("\n"); //NOI18N
                    showDialog = true;
                } else {
                    newFiles.add(file);
                }
            }

            // remember last location
            basedir = chooser.getCurrentDirectory();
            addFiles(newFiles);
            if (showDialog) {
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(existing.toString(), NotifyDescriptor.ERROR_MESSAGE));
            }
        }
}//GEN-LAST:event_addFileButtonActionPerformed

private void removeFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeFileButtonActionPerformed
        removeFiles();
}//GEN-LAST:event_removeFileButtonActionPerformed

private void detectFilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_detectFilesButtonActionPerformed
        Set<File> alreadySelectedFiles = new HashSet<File>(files);
        SelectConfigFilesPanel panel;
        if (detectedFiles != null) {
            panel = SelectConfigFilesPanel.create(detectedFiles, alreadySelectedFiles, fileDisplayName);
        } else {
            panel = SelectConfigFilesPanel.create(project, alreadySelectedFiles, fileDisplayName);
        }
        if (panel.open()) {
            List<File> availableFiles = panel.getAvailableFiles();
            if (availableFiles != null) {
                this.detectedFiles = availableFiles;
            }
            addFiles(panel.getSelectedFiles());
        }
}//GEN-LAST:event_detectFilesButtonActionPerformed

private void addGroupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addGroupButtonActionPerformed
        InputLine input = new InputLine(
                NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_GroupName"),
                NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_NewConfigFileGroup"));
        DialogDisplayer.getDefault().notify(input);
        if (input.getValue() == NotifyDescriptor.OK_OPTION) {
            addGroup(ConfigFileGroup.create(input.getInputText(), Collections.<File>emptyList()));
        }
}//GEN-LAST:event_addGroupButtonActionPerformed

private void editGroupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editGroupButtonActionPerformed
        InputLine input = new InputLine(
                NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_GroupName"),
                NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_EditConfigFileGroup"));
        String currentName = currentGroup.getName();
        if (currentName != null) {
            input.setInputText(currentName);
        }
        DialogDisplayer.getDefault().notify(input);
        if (input.getValue() == NotifyDescriptor.OK_OPTION) {
            replaceCurrentGroup(ConfigFileGroup.create(input.getInputText(), currentGroup.getFiles()));
        }
}//GEN-LAST:event_editGroupButtonActionPerformed

private void removeGroupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeGroupButtonActionPerformed
        removeGroups();
}//GEN-LAST:event_removeGroupButtonActionPerformed

private void addGroupFilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addGroupFilesButtonActionPerformed
        if (files.size() == 0) {
            String message = NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_NoFilesAdded");
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
            return;
        }
        Set<File> alreadySelectedFiles = new HashSet<File>(groups.get(currentGroupIndex).getFiles());
        SelectConfigFilesPanel panel = SelectConfigFilesPanel.create(files, alreadySelectedFiles, fileDisplayName);
        if (panel.open()) {
            addFilesToCurrentGroup(panel.getSelectedFiles());
        }
}//GEN-LAST:event_addGroupFilesButtonActionPerformed

private void removeGroupFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeGroupFileButtonActionPerformed
        removeFilesFromCurrentGroup();
}//GEN-LAST:event_removeGroupFileButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFileButton;
    private javax.swing.JButton addGroupButton;
    private javax.swing.JButton addGroupFilesButton;
    private javax.swing.JPanel configFileGroupsPanel;
    private javax.swing.JPanel configFilesPanel;
    private javax.swing.JButton detectFilesButton;
    private javax.swing.JButton editGroupButton;
    private javax.swing.JLabel filesLabel;
    private javax.swing.JList filesList;
    private javax.swing.JScrollPane filesScrollPane;
    private javax.swing.JLabel groupFilesLabel;
    private javax.swing.JList groupFilesList;
    private javax.swing.JScrollPane groupFilesScrollPane;
    private javax.swing.JLabel groupsLabel;
    private javax.swing.JList groupsList;
    private javax.swing.JScrollPane groupsScrollPane;
    private javax.swing.JButton removeFileButton;
    private javax.swing.JButton removeGroupButton;
    private javax.swing.JButton removeGroupFileButton;
    private javax.swing.JTabbedPane springConfigPane;
    // End of variables declaration//GEN-END:variables

    private final class RelativeDisplayName implements FileDisplayName {

        private Map<File, String> abs2Rel = new HashMap<File, String>();

        public String getDisplayName(File absolute) {
            String relative = abs2Rel.get(absolute);
            if (relative == null) {
                relative = ConfigFiles.getRelativePath(basedir, absolute);
                abs2Rel.put(absolute, relative);
            }
            return relative;
        }
    }
}
