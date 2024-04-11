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
package org.netbeans.modules.ide.dashboard;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.project.ui.api.RecentProjects;
import org.netbeans.modules.project.ui.api.UnloadedProjectInformation;
import org.netbeans.spi.dashboard.DashboardDisplayer;
import org.netbeans.spi.dashboard.DashboardWidget;
import org.netbeans.spi.dashboard.WidgetElement;
import org.openide.*;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.*;
import org.openide.util.NbBundle.Messages;

/**
 * List of recent projects.
 */
@Messages({
    "TITLE_RecentProjects=Recent Projects",
    "LBL_NoRecentProjects=<no recent projects>",
    "# {0} - Project name",
    "ERR_InvalidProject={0} is not a valid NetBeans project.",
    "LBL_OpenRecentProject=Open Recent Project"
})
public class RecentProjectsWidget implements DashboardWidget {

    private static final int MAX_PROJECTS = 5;

    private static final RequestProcessor RP = new RequestProcessor("RecentProjects");

    private final List<WidgetElement> elements;
    private final List<UnloadedProjectInformation> projects;
    private final Set<DashboardDisplayer.Panel> active;
    private final PropertyChangeListener projectsListener;

    private final Action newProject;
    private final Action openProject;
    private final Action projectGroups;

    public RecentProjectsWidget() {
        elements = new ArrayList<>();
        projects = new ArrayList<>();
        active = new HashSet<>();
        projectsListener = e -> {
            if (RecentProjects.PROP_RECENT_PROJECT_INFO.equals(e.getPropertyName())) {
                loadProjects();
            }
        };
        Action newProjectOriginal = Actions.forID("Project", "org.netbeans.modules.project.ui.NewProject");
        if (newProjectOriginal != null) {
            newProject = new ProjectDelegateAction(newProjectOriginal);
        } else {
            newProject = null;
        }
        Action openProjectOriginal = Actions.forID("Project", "org.netbeans.modules.project.ui.OpenProject");
        if (openProjectOriginal != null) {
            openProject = new ProjectDelegateAction(openProjectOriginal);
        } else {
            openProject = null;
        }
        Action projectGroupsOriginal = Actions.forID("Project", "org.netbeans.modules.project.ui.groups.GroupsMenu");
        if (projectGroupsOriginal != null) {
            projectGroups = new ProjectDelegateAction(projectGroupsOriginal);
        } else {
            projectGroups = null;
        }
        buildElements();
        loadProjects();
    }

    @Override
    public String title(DashboardDisplayer.Panel panel) {
        return Bundle.TITLE_RecentProjects();
    }

    @Override
    public List<WidgetElement> elements(DashboardDisplayer.Panel panel) {
        return List.copyOf(elements);
    }

    @Override
    public void showing(DashboardDisplayer.Panel panel) {
        if (active.isEmpty()) {
            RecentProjects.getDefault().addPropertyChangeListener(projectsListener);
        }
        active.add(panel);
        panel.refresh();
        loadProjects();
    }

    @Override
    public void hidden(DashboardDisplayer.Panel panel) {
        active.remove(panel);
        if (active.isEmpty()) {
            RecentProjects.getDefault().removePropertyChangeListener(projectsListener);
        }
    }

    // derived from RecentProjectsPanel in Welcome module
    private void loadProjects() {
        RP.execute(() -> {
            List<UnloadedProjectInformation> existingProjects = new ArrayList<>(MAX_PROJECTS);
            for (UnloadedProjectInformation p : RecentProjects.getDefault().getRecentProjectInformation()) {
                try {
                    File projectDir = Utilities.toFile(p.getURL().toURI());
                    if (!projectDir.exists() || !projectDir.isDirectory()) {
                        continue;
                    }
                    existingProjects.add(p);
                    if (existingProjects.size() >= MAX_PROJECTS) {
                        break;
                    }
                } catch (Exception ex) {
                    Logger.getLogger(RecentProjectsWidget.class.getName()).log(Level.FINE, null, ex);
                }
            }
            EventQueue.invokeLater(() -> {
                updateProjectsList(existingProjects);
            });

        });

    }

    private void buildElements() {
        elements.clear();
        if (projects.isEmpty()) {
            elements.add(WidgetElement.unavailable(Bundle.LBL_NoRecentProjects()));
        } else {
            for (UnloadedProjectInformation project : projects) {
                elements.add(WidgetElement.actionLink(new OpenProjectAction(project)));
            }
        }
        if (newProject != null || openProject != null || projectGroups != null) {
            elements.add(WidgetElement.separator());
        }
        if (newProject != null) {
            elements.add(WidgetElement.actionLink(newProject));
        }
        if (openProject != null) {
            elements.add(WidgetElement.actionLink(openProject));
        }
        if (projectGroups != null) {
            elements.add(WidgetElement.actionLink(projectGroups));
        }
    }

    private void updateProjectsList(List<UnloadedProjectInformation> projects) {
        if (!this.projects.equals(projects)) {
            this.projects.clear();
            this.projects.addAll(projects);
            buildElements();
            active.forEach(DashboardDisplayer.Panel::refresh);
        }
    }

    private static class ProjectDelegateAction extends AbstractAction {

        private final Action delegate;

        private ProjectDelegateAction(Action delegate) {
            super(Actions.cutAmpersand(String.valueOf(delegate.getValue(NAME)).replace("...", "")));
            this.delegate = delegate;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            delegate.actionPerformed(e);
        }

    }

    // derived from RecentProjectsPanel in Welcome panel
    private static class OpenProjectAction extends AbstractAction {

        private final UnloadedProjectInformation project;

        private OpenProjectAction(UnloadedProjectInformation project) {
            super(project.getDisplayName(), project.getIcon());
            this.project = project;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Runnable task = () -> {
                URL url = project.getURL();
                Project prj = null;

                FileObject dir = URLMapper.findFileObject(url);
                if (dir != null && dir.isFolder()) {
                    try {
                        prj = ProjectManager.getDefault().findProject(dir);
                    } catch (IOException ioEx) {
                        // Ignore invalid folders
                    }
                }

                if (prj != null) {
                    OpenProjects.getDefault().open(new Project[]{prj}, false, true);
                } else {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(
                            Bundle.ERR_InvalidProject(project.getDisplayName()));
                    DialogDisplayer.getDefault().notify(nd);
                }
            };
            BaseProgressUtils.runOffEventDispatchThread(task, Bundle.LBL_OpenRecentProject(),
                    new AtomicBoolean(false), false);
        }
    }

}
