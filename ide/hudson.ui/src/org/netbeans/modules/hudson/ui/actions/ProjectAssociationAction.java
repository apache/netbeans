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

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.ui.spi.ProjectHudsonProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle.Messages;

/**
 * Associates or dissociates a job with a project.
 */
public class ProjectAssociationAction extends AbstractAction {

    private final ProjectHudsonProvider.Association assoc;
    private final Project alreadyAssociatedProject;

    @Messages({
        "ProjectAssociationAction.associate=Associate with Project...",
        "# {0} - project display name", "ProjectAssociationAction.dissociate=Dissociate from Project \"{0}\""
    })
    public ProjectAssociationAction(HudsonJob job) {
        assoc = ProjectHudsonProvider.Association.forJob(job);
        this.alreadyAssociatedProject = ProjectHudsonProvider.getDefault().findAssociatedProject(assoc);
        if (alreadyAssociatedProject == null) {
            putValue(NAME, Bundle.ProjectAssociationAction_associate());
        } else {
            putValue(NAME, Bundle.ProjectAssociationAction_dissociate(ProjectUtils.getInformation(alreadyAssociatedProject).getDisplayName()));
        }
    }

    @Messages({
        "ProjectAssociationAction.open_some_projects=Open some projects to choose from.",
        "ProjectAssociationAction.title_select_project=Select Project",
        "ProjectAssociationAction.could_not_associate=Failed to record a Jenkins job association.",
        "ProjectAssociationAction.could_not_dissociate=Failed to find the Jenkins job association to be removed."
    })
    @Override public void actionPerformed(ActionEvent e) {
        if (alreadyAssociatedProject == null) {
            SortedSet<Project> projects = new TreeSet<Project>(ProjectRenderer.comparator());
            projects.addAll(Arrays.asList(OpenProjects.getDefault().getOpenProjects()));
            if (projects.isEmpty()) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(Bundle.ProjectAssociationAction_open_some_projects(), NotifyDescriptor.INFORMATION_MESSAGE));
                return;
            }
            JComboBox box = new JComboBox(new DefaultComboBoxModel(projects.toArray(new Project[0])));
            box.setRenderer(new ProjectRenderer());
            if (DialogDisplayer.getDefault().notify(new NotifyDescriptor(box, Bundle.ProjectAssociationAction_title_select_project(), NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE, null, null)) != NotifyDescriptor.OK_OPTION) {
                return;
            }
            if (!ProjectHudsonProvider.getDefault().recordAssociation((Project) box.getSelectedItem(), assoc)) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(Bundle.ProjectAssociationAction_could_not_associate(), NotifyDescriptor.WARNING_MESSAGE));
            }
        } else {
            if (!ProjectHudsonProvider.getDefault().recordAssociation(alreadyAssociatedProject, null)) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(Bundle.ProjectAssociationAction_could_not_dissociate(), NotifyDescriptor.WARNING_MESSAGE));
            }
        }
    }

}
