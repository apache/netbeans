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

package org.netbeans.modules.gradle.execute.navigator;

import org.netbeans.modules.gradle.api.GradleTask;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.nodes.NodeUtils;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gradle.FavoriteTaskManager;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

import static org.netbeans.modules.gradle.execute.navigator.Bundle.*;
import org.netbeans.modules.gradle.nodes.TaskNode;
import org.netbeans.modules.gradle.spi.Utils;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Laszlo Kishalmi
 */
public class TasksPanel extends javax.swing.JPanel implements ExplorerManager.Provider {

    @StaticResource
    private static final String GRADLE_ICON = "org/netbeans/modules/gradle/resources/gradle.png"; //NOI18

    private final transient ExplorerManager manager = new ExplorerManager();
    private final BeanTreeView treeView;
    private NbGradleProject current;
    private Project currentP;
    private final PropertyChangeListener pchadapter = (PropertyChangeEvent evt) -> {
        if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
            RequestProcessor.getDefault().post(() -> update(currentP));
        }
    };

    /**
     * Creates new form TasksPanel
     */
    @Messages({
        "# {0} - The display name of the project",
        "LBL_Tasks=Tasks of {0}",
        "LBL_Favorites=Favorite Tasks"
    })
    public TasksPanel() {
        treeView = new BeanTreeView();
        setLayout(new java.awt.BorderLayout());

        add(treeView, java.awt.BorderLayout.CENTER);
        treeView.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    void navigate(DataObject d) {
        NbGradleProject n = null;

        FileObject f = d.getPrimaryFile();
        if (!f.isFolder()) {
            f = f.getParent();
        }
        Project p = null;
        try {
            p = ProjectManager.getDefault().findProject(f);
            if (p != null) {
                n = NbGradleProject.get(p);
            }
        } catch (IOException | IllegalArgumentException ex) {
            //Ignore we can't really do anything about this.
        }

        synchronized (this) {
            if (current != null) {
                current.removePropertyChangeListener(pchadapter);
            }
            if (n == null) {
                release();
                return;
            }
            current = n;
            currentP = p;
            current.addPropertyChangeListener(pchadapter);
        }
        RequestProcessor.getDefault().post(() -> update(currentP));
    }

    public void update(Project updateP) {
        synchronized (this) {
            if (updateP != this.currentP) {
                return;
            }
            if (updateP == null) {
                return;
            }
        }
        GradleBaseProject prj = GradleBaseProject.get(updateP);
        if (prj != null) {
            final Children taskGroups = new Children.Array();
            ArrayList<String> glist = new ArrayList<>(prj.getTaskGroups());
            glist.remove(GradleBaseProject.PRIVATE_TASK_GROUP);
            glist.sort(String.CASE_INSENSITIVE_ORDER);

            for (String group : glist) {
                taskGroups.add(new Node[]{new TaskGroupNode(group, prj, updateP)});
            }
            taskGroups.add(new Node[]{new TaskGroupNode(GradleBaseProject.PRIVATE_TASK_GROUP, prj, updateP)});

            AbstractNode tasksNode = new AbstractNode(taskGroups, Lookups.singleton(updateP)) {
                @Override
                public Action[] getActions(boolean context) {
                    return new Action[0];
                }

            };
            tasksNode.setName("tasks"); //NOI18N
            tasksNode.setDisplayName(Bundle.LBL_Tasks(ProjectUtils.getInformation(updateP).getDisplayName()));
            tasksNode.setIconBaseWithExtension(GRADLE_ICON);

            AbstractNode favoritesNode = new AbstractNode(new FavoritesChildren()) {
                @Override
                public Action[] getActions(boolean context) {
                    return new Action[0];
                }

            };
            favoritesNode.setName("favorites"); //NOI18N
            favoritesNode.setDisplayName(Bundle.LBL_Favorites());
            favoritesNode.setIconBaseWithExtension(GRADLE_ICON);

            final Children rootKids = new Children.Array();
            rootKids.add(new Node[]{favoritesNode, tasksNode});
            SwingUtilities.invokeLater(() -> {
                treeView.setScrollsOnExpand(false);
                treeView.setRootVisible(false);
                manager.setRootContext(new AbstractNode(rootKids));
                treeView.expandAll();
                treeView.setScrollsOnExpand(true);
            });
            return;
        }

        SwingUtilities.invokeLater(() -> {
            treeView.setRootVisible(false);
            manager.setRootContext(createEmptyNode());
        });
    }

    synchronized void release() {
        if (current != null) {
            current.removePropertyChangeListener(pchadapter);
        }
        current = null;
        currentP = null;
        SwingUtilities.invokeLater(() -> {
            treeView.setRootVisible(false);
            manager.setRootContext(createEmptyNode());
        });
    }

    private static Node createEmptyNode() {
        return new AbstractNode(Children.LEAF);
    }

    private class TaskGroupNode extends AbstractNode {
        
        @Messages({
            "LBL_PrivateTasks=Other Tasks"
        })
        @SuppressWarnings("OverridableMethodCallInConstructor")
        public TaskGroupNode(String group, GradleBaseProject project, Project genericProject) {
            super(new TaskGroupChildren(group, project, genericProject), Lookup.EMPTY);
            setName(group);
            String displayName = GradleBaseProject.PRIVATE_TASK_GROUP.equals(group)
                    ? LBL_PrivateTasks()
                    : Utils.capitalize(group);
            setDisplayName(displayName);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(true);
        }

        @Override
        public Image getIcon(int type) {
            return getIcon(false);
        }

        private Image getIcon(boolean opened) {
            return NodeUtils.getTreeFolderIcon(opened);
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[0];
        }

    }

    private FavoriteTaskManager getFavoriteTaskManager() {
        Project p = currentP;
        return p != null ? p.getLookup().lookup(FavoriteTaskManager.class) : null;
    }

    private class TaskGroupChildren extends Children.Keys<GradleTask> {
        final Project genericProject;
        
        public TaskGroupChildren(String group, GradleBaseProject project, Project genericProject) {
            this.genericProject = genericProject;
            ArrayList<GradleTask> keys = new ArrayList<>(project.getTasks(group));
            keys.sort(Comparator.comparing(GradleTask::getName, String.CASE_INSENSITIVE_ORDER));
            setKeys(keys);
        }

        @Override
        protected Node[] createNodes(GradleTask key) {
            return new Node[] {new TaskNode(genericProject, key)};
        }

    }

    private class FavoritesChildren extends Children.Keys<GradleTask> {
        private final ChangeListener listener;

        public FavoritesChildren() {
            assert currentP != null;
            FavoriteTaskManager fvm = getFavoriteTaskManager();
            listener = (e) -> {
                Set<GradleTask> favs = fvm.getFavoriteTasks();
                setKeys(favs);
            };
            fvm.addChangeListener(WeakListeners.change(listener, fvm));
            setKeys(fvm.getFavoriteTasks());
        }

        @Override
        protected Node[] createNodes(GradleTask key) {
            // NETBEANS-5340 It might happen that the currentP is null, but the listener
            // still active on the previous project.
            return currentP != null ? 
                    new Node[] {new TaskNode(currentP, key)} :
                    new Node[0];
        }

    }
}
