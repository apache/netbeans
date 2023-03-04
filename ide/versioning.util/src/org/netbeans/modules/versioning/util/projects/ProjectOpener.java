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

package org.netbeans.modules.versioning.util.projects;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.favorites.api.Favorites;
import org.netbeans.modules.versioning.util.ProjectUtilities;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka, Ondra Vrabec
 */
public class ProjectOpener implements ActionListener, PropertyChangeListener {
    private final ProjectOpenerType type;

    public enum ProjectOpenerType {
        EXPORT("EXPORT_"),
        CHECKOUT("CHECKOUT_"),
        CLONE("CLONE_");
        private final String prefix;
        private ProjectOpenerType(String prefix) {
            this.prefix = prefix;
        }
        @Override
        public String toString() {
            return prefix;
        }
        String getMessage(String key) {
            return NbBundle.getMessage(ProjectOpener.class, prefix + key);
        }
        String getMessage(String key, Object... params) {
            return NbBundle.getMessage(ProjectOpener.class, prefix + key, params);
        }
    }

    private CheckoutCompletedPanel panel;
    private Dialog dialog;
    private final Map<Project, Set<Project>> checkedOutProjects; // null key is for root projects
    private final File workingFolder;
    private OpenProjectsPanel projectsPanel;
    private JButton okButton;
    private int numberOfProjects;  // number of checkedout projects

    /**
     *
     * @param checkedOutProjects <strong>All projects must have its key/value pair present.</strong> Root projects are in a set under the null key
     * @param workingFolder
     */
    public ProjectOpener (ProjectOpenerType type, Map<Project, Set<Project>> checkedOutProjects, File workingFolder) {
        this.checkedOutProjects = checkedOutProjects;
        this.workingFolder = workingFolder;
        this.type = type;
        numberOfProjects = checkedOutProjects.size() - 1;
    }

    public void openProjects () {
        panel = new CheckoutCompletedPanel(type);
        panel.openButton.addActionListener(this);
        panel.createButton.addActionListener(this);
        panel.closeButton.addActionListener(this);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panel.cbOpenInFavorites.setToolTipText(type.getMessage("CheckoutCompletedPanel.cbOpenInFavorites.TT")); //NOI18N
        panel.cbOpenInFavorites.getAccessibleContext().setAccessibleDescription(type.getMessage("CheckoutCompletedPanel.cbOpenInFavorites.TT")); //NOI18N
        String title = type.getMessage("BK3008"); // NOI18N
        DialogDescriptor descriptor = new DialogDescriptor(panel, title);
        descriptor.setModal(true);

        // move buttons from dialog to descriptor
        panel.remove(panel.openButton);
        panel.remove(panel.createButton);
        panel.remove(panel.closeButton);

        Object[] options = null;
        if (numberOfProjects > 1) {
            // more that one project
            String msg = type.getMessage("BK3009", Integer.valueOf(numberOfProjects));   // NOI18N
            panel.jLabel1.setText(msg);
            options = new Object[]{panel.openButton, panel.closeButton};
        } else if (numberOfProjects == 1) {
            // only one root project
            Project project = checkedOutProjects.get(null).iterator().next();
            ProjectInformation projectInformation = ProjectUtils.getInformation(project);
            String projectName = projectInformation.getDisplayName();
            String msg = type.getMessage("BK3011", projectName);                              // NOI18N
            panel.jLabel1.setText(msg);
            panel.openButton.setText(NbBundle.getMessage(ProjectOpener.class, "BK3012"));                              // NOI18N
            options = new Object[]{panel.openButton, panel.closeButton};
        } else {
            String msg = type.getMessage("BK3010");                                  // NOI18N
            panel.jLabel1.setText(msg);
            options = new Object[]{panel.createButton, panel.closeButton};
        }

        descriptor.setMessageType(DialogDescriptor.INFORMATION_MESSAGE);
        descriptor.setOptions(options);
        descriptor.setClosingOptions(options);
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleDescription(type.getMessage("ACSD_Completed_Dialog")); // NOI18N
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                dialog.setVisible(true);
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        dialog.setVisible(false);
        if (panel.cbOpenInFavorites.isSelected()) {
            Utils.post(new Runnable() {
                @Override
                public void run () {
                    try {
                        Favorites.getDefault().selectWithAddition(FileUtil.toFileObject(workingFolder));
                    } catch (DataObjectNotFoundException ex) {
                        Logger.getLogger(ProjectOpener.class.getName()).log(Level.INFO, null, ex);
                    }
                }
            });
        }
        if (panel.openButton.equals(src)) {
            // show project chooser
            if (numberOfProjects > 1) {
                selectAndOpenProjects(checkedOutProjects);
            } else {
                openProject(checkedOutProjects.get(null).iterator().next());
            }
        } else if (panel.createButton.equals(src)) {
            if (workingFolder.isDirectory()) {
                ProjectUtilities.newProjectWizard(workingFolder);
            }
        }
    }

    private void openProject(Project p) {
        Project[] projects = new Project[]{p};
        OpenProjects.getDefault().open(projects, false, true);
        ProjectUtilities.selectAndExpandProject(p);
    }

    /**
     * Opens a dialog with projects selection and then opens selected projects
     */
    private void selectAndOpenProjects (Map<Project, Set<Project>> projects) {
        projectsPanel = new OpenProjectsPanel();
        ProjectsView view = new ProjectsView(projects);
        projectsPanel.jPanel1.setLayout(new BorderLayout());
        projectsPanel.jPanel1.add(view, BorderLayout.CENTER);
        view.addSelectionChangeListener(this);
        okButton = new JButton(NbBundle.getMessage(CheckoutCompletedPanel.class, "LBL_ButtonOpen")); // NOI18N
        okButton.setEnabled(false);
        JButton cancelButton = new JButton(NbBundle.getMessage(CheckoutCompletedPanel.class, "LBL_ButtonCancel"));   // NOI18N
        DialogDescriptor dd = new DialogDescriptor(projectsPanel, NbBundle.getMessage(CheckoutCompletedPanel.class, "LBL_OpenPanelLabel"), true, // NOI18N
                new Object[]{okButton, cancelButton}, okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(this.getClass()), null);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.setVisible(true);
        if (dd.getValue() == okButton) {
            // get selected projects and open them
            final Set<Project> selectedProjects = view.getSelectedProjects();
            if (projectsPanel.cbOpenRequired.isSelected()) {
                // scan all subprojects recursively and open all
                Utils.postParallel(new Runnable() {
                    @Override
                    public void run() {
                        final Set<Project> toOpen = new HashSet<Project>();
                        final HashMap<Project, Set<? extends Project>> cache = new HashMap<Project, Set<? extends Project>>();
                        toOpen.addAll(selectedProjects);
                        for (Project p : selectedProjects) {
                            ProjectUtilities.addSubprojects(p, toOpen, cache);
                        }
                        EventQueue.invokeLater(new Runnable () {
                            public void run() {
                                for (Project p : toOpen) {
                                    openProject(p);
                                }
                            }
                        });
                    }
                }, 0);
            } else {
                for (Project p : selectedProjects) {
                    openProject(p);
                }
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            // no selection - disable the Open button
            final Node [] nodes = (Node[]) evt.getNewValue();
            if (nodes.length == 0) {
                okButton.setEnabled(false);
            } else {
                okButton.setEnabled(true);
            }
        }
    }
}
    
