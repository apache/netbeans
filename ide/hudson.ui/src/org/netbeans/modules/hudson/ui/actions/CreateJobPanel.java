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

package org.netbeans.modules.hudson.ui.actions;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.netbeans.modules.hudson.ui.spi.ProjectHudsonJobCreatorFactory;
import org.netbeans.modules.hudson.spi.HudsonSCM.ConfigurationStatus;
import org.netbeans.modules.hudson.ui.spi.ProjectHudsonJobCreatorFactory.ProjectHudsonJobCreator;
import org.netbeans.modules.hudson.ui.spi.ProjectHudsonProvider;
import org.netbeans.modules.hudson.ui.wizard.InstanceDialog;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.NotificationLineSupport;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Visual configuration of {@link CreateJob}.
 */
public class CreateJobPanel extends JPanel implements ChangeListener {

    private Set<String> takenNames;
    private NotificationLineSupport notifications;
    private DialogDescriptor descriptor;
    private Set<Project> manuallyAddedProjects = new HashSet<Project>();
    ProjectHudsonJobCreator creator;
    HudsonInstance instance;

    CreateJobPanel() {}

    void init(DialogDescriptor descriptor, HudsonInstance instance) {
        this.descriptor = descriptor;
        this.notifications = descriptor.createNotificationLineSupport();
        initComponents();
        updateServerModel();
        this.instance = instance;
        server.setSelectedItem(instance);
        server.setRenderer(new ServerRenderer());
        updateProjectModel();
        project.setSelectedItem(project.getItemCount() > 0 ? project.getItemAt(0) : null);
        project.setRenderer(new ProjectRenderer());
        name.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                check();
            }
            public void removeUpdate(DocumentEvent e) {
                check();
            }
            public void changedUpdate(DocumentEvent e) {}
        });
    }

    public @Override void addNotify() {
        super.addNotify();
        project.requestFocusInWindow();
        check();
    }

    @NbBundle.Messages({
        "CreateJobPanel.emptyName=Please enter a Build Name"
    })
    private void check() {
        descriptor.setValid(false);
        notifications.clearMessages();
        if (name.getText() == null || name.getText().trim().isEmpty()) {
            notifications.setInformationMessage(Bundle.CreateJobPanel_emptyName());
            return;
        }
        if (instance == null) {
            notifications.setInformationMessage(NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.pick_server"));
            return;
        }
        Project p = selectedProject();
        if (p == null) {
            notifications.setInformationMessage(NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.pick_project"));
            return;
        }
        if (creator == null) {
            notifications.setErrorMessage(NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.unknown_project_type"));
            return;
        }
        if (takenNames.contains(name())) {
            notifications.setErrorMessage(NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.name_taken"));
            return;
        }
        if (ProjectHudsonProvider.getDefault().findAssociation(p) != null) {
            notifications.setWarningMessage(NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.already_associated"));
        }
        ConfigurationStatus status = creator.status();
        if (status.getErrorMessage() != null) {
            notifications.setErrorMessage(status.getErrorMessage());
        } else {
            if (status.getWarningMessage() != null) {
                notifications.setWarningMessage(status.getWarningMessage());
            }
            descriptor.setValid(true);
        }
        JButton button = status.getExtraButton();
        if (button != null) {
            descriptor.setAdditionalOptions(new Object[] {button});
            descriptor.setClosingOptions(new Object[] {button, NotifyDescriptor.CANCEL_OPTION});
        } else {
            descriptor.setAdditionalOptions(new Object[0]);
            descriptor.setClosingOptions(new Object[] {NotifyDescriptor.CANCEL_OPTION});
        }
    }

    String name() {
        return name.getText();
    }

    Project selectedProject() {
        return (Project) project.getSelectedItem();
    }

    private void updateServerModel() {
        server.setModel(new DefaultComboBoxModel(HudsonManager.getAllInstances().toArray()));
    }

    private void computeTakenNames() {
        takenNames = new HashSet<String>();
        if (instance != null) {
            for (HudsonJob job : instance.getJobs()) {
                takenNames.add(job.getName());
            }
        }
    }

    private void updateProjectModel() {
        SortedSet<Project> projects = new TreeSet<Project>(ProjectRenderer.comparator());
        projects.addAll(Arrays.asList(OpenProjects.getDefault().getOpenProjects()));
        projects.addAll(manuallyAddedProjects);
        project.setModel(new DefaultComboBoxModel(projects.toArray(new Project[0])));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        serverLabel = new javax.swing.JLabel();
        server = new javax.swing.JComboBox();
        addServer = new javax.swing.JButton();
        nameLabel = new javax.swing.JLabel();
        name = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        project = new javax.swing.JComboBox();
        browse = new javax.swing.JButton();
        custom = new javax.swing.JPanel();
        explanationLabel = new javax.swing.JLabel();

        serverLabel.setLabelFor(server);
        org.openide.awt.Mnemonics.setLocalizedText(serverLabel, org.openide.util.NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.serverLabel.text")); // NOI18N

        server.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(addServer, org.openide.util.NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.addServer.text")); // NOI18N
        addServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addServerActionPerformed(evt);
            }
        });

        nameLabel.setLabelFor(name);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.nameLabel.text")); // NOI18N

        projectLabel.setLabelFor(project);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.projectLabel.text")); // NOI18N

        project.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(browse, org.openide.util.NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.browse.text")); // NOI18N
        browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseActionPerformed(evt);
            }
        });

        custom.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(explanationLabel, org.openide.util.NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.explanationLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(explanationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(serverLabel)
                            .addComponent(nameLabel)
                            .addComponent(projectLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(name, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                            .addComponent(project, javax.swing.GroupLayout.Alignment.TRAILING, 0, 278, Short.MAX_VALUE)
                            .addComponent(server, 0, 278, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(addServer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(browse, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(custom, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverLabel)
                    .addComponent(server, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addServer))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLabel)
                    .addComponent(project, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browse))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(custom, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(explanationLabel)
                .addContainerGap())
        );

        server.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.server.AccessibleContext.accessibleDescription")); // NOI18N
        addServer.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.addServer.AccessibleContext.accessibleDescription")); // NOI18N
        name.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.name.AccessibleContext.accessibleDescription")); // NOI18N
        project.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.project.AccessibleContext.accessibleDescription")); // NOI18N
        browse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.browse.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseActionPerformed
        JFileChooser chooser = ProjectChooser.projectChooser();
        chooser.showOpenDialog(this);
        File dir = chooser.getSelectedFile();
        if (dir != null) {
            FileObject d = FileUtil.toFileObject(dir);
            if (d != null) {
                try {
                    Project p = ProjectManager.getDefault().findProject(d);
                    if (p != null) {
                        manuallyAddedProjects.add(p);
                        updateProjectModel();
                        project.setSelectedItem(p);
                    }
                } catch (IOException x) {
                    Exceptions.printStackTrace(x);
                }
            }
        }
    }//GEN-LAST:event_browseActionPerformed

    private void projectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectActionPerformed
        if (creator != null) {
            creator.removeChangeListener(this);
        }
        creator = null;
        Project p = selectedProject();
        if (p == null) {
            check();
            return;
        }
        if (p.getClass().getName().equals("org.netbeans.modules.project.ui.LazyProject")) { // NOI18N
            // XXX ugly but not obvious how better to handle this...
            updateProjectModel();
            project.setSelectedItem(null);
            return;
        }
        for (ProjectHudsonJobCreatorFactory factory : Lookup.getDefault().lookupAll(ProjectHudsonJobCreatorFactory.class)) {
            creator = factory.forProject(p);
            if (creator != null) {
                break;
            }
        }
        if (creator == null) {
            check();
            return;
        }
        name.setText(creator.jobName());
        custom.removeAll();
        custom.add(creator.customizer());
        creator.addChangeListener(this);
        check();
    }//GEN-LAST:event_projectActionPerformed

    private void serverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverActionPerformed
        instance = (HudsonInstance) server.getSelectedItem();
        computeTakenNames();
        check();
    }//GEN-LAST:event_serverActionPerformed

    private void addServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addServerActionPerformed
        HudsonInstance created = new InstanceDialog().show();
        if (created != null) {
            updateServerModel();
            instance = created;
            server.setSelectedItem(instance);
            check();
        }
    }//GEN-LAST:event_addServerActionPerformed

    public void stateChanged(ChangeEvent event) {
        check();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addServer;
    private javax.swing.JButton browse;
    private javax.swing.JPanel custom;
    private javax.swing.JLabel explanationLabel;
    private javax.swing.JTextField name;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JComboBox project;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JComboBox server;
    private javax.swing.JLabel serverLabel;
    // End of variables declaration//GEN-END:variables

    private static class ServerRenderer extends DefaultListCellRenderer {
        public @Override Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null || /* #180088 */ value instanceof String) {
                return super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
            }
            return super.getListCellRendererComponent(list, ((HudsonInstance) value).getName(), index, isSelected, cellHasFocus);
        }
    }

}
