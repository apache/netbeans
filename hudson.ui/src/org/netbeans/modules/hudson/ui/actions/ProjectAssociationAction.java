/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
        "ProjectAssociationAction.could_not_associate=Failed to record a Hudson job association.",
        "ProjectAssociationAction.could_not_dissociate=Failed to find the Hudson job association to be removed."
    })
    @Override public void actionPerformed(ActionEvent e) {
        if (alreadyAssociatedProject == null) {
            SortedSet<Project> projects = new TreeSet<Project>(ProjectRenderer.comparator());
            projects.addAll(Arrays.asList(OpenProjects.getDefault().getOpenProjects()));
            if (projects.isEmpty()) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(Bundle.ProjectAssociationAction_open_some_projects(), NotifyDescriptor.INFORMATION_MESSAGE));
                return;
            }
            JComboBox box = new JComboBox(new DefaultComboBoxModel(projects.toArray(new Project[projects.size()])));
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
