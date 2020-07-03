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

package org.netbeans.modules.gradle.nodes;

import org.netbeans.modules.gradle.spi.nodes.NodeUtils;
import org.netbeans.modules.gradle.NbGradleProjectImpl;
import org.netbeans.modules.gradle.api.NbGradleProject;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

import static org.netbeans.modules.gradle.nodes.Bundle.*;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.gradle.spi.Utils;
import org.openide.ErrorManager;

/**
 *
 * @author Laszlo Kishalmi
 */
public class SubProjectsNode extends AbstractNode {

    @StaticResource
    private static final String SP_BADGE
            = "org/netbeans/modules/gradle/resources/gradle-large-badge.png";

    @NbBundle.Messages("LBL_SubProjects=Sub Projects")
    public SubProjectsNode(NbGradleProjectImpl proj, String path) {
        super(FilterNode.Children.create(new SubProjectsChildFactory(proj, path), true));
        if (":".equals(path)) {     //NOI18N
            setName("SubProjects"); //NOI18N
            setDisplayName(LBL_SubProjects());
        } else {
            int colon = path.lastIndexOf(':', path.length() - 2);
            String partName = path.substring(colon + 1, path.length() - 1);
            setDisplayName(Utils.capitalize(partName));
            setName(path);
        }
    }

    public SubProjectsNode(NbGradleProjectImpl proj) {
        this(proj, ":"); //NOI18N
    }

    @Override
    public Action[] getActions(boolean bool) {
        return new Action[]{};
    }

    private static Image getIcon(boolean opened) {
        Image badge = ImageUtilities.loadImage(SP_BADGE, true); //NOI18N
        return ImageUtilities.mergeImages(NodeUtils.getTreeFolderIcon(opened), badge, 4, 4);
    }

    @Override
    public Image getIcon(int type) {
        return getIcon(false);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(true);
    }

    private static class SubProjectsChildFactory extends ChildFactory<String> {

        private final NbGradleProjectImpl project;
        private final PropertyChangeListener listener;
        private final String rootPath;

        SubProjectsChildFactory(NbGradleProjectImpl proj, String rootPath) {
            project = proj;
            this.rootPath = rootPath;
            NbGradleProject watcher = project.getProjectWatcher();
            listener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                        refresh(false);
                    }
                }
            };

            watcher.addPropertyChangeListener(WeakListeners.propertyChange(listener, watcher));

        }

        @Override
        protected boolean createKeys(final List<String> paths) {
            Map<String, File> subProjects = project.getGradleProject().getBaseProject().getSubProjects();
            Set<String> components = new TreeSet<String>();
            Set<String> projects = new TreeSet<>();
            for (String path : subProjects.keySet()) {
                if (path.startsWith(rootPath)) {
                    String relPath = path.substring(rootPath.length());
                    int firstColon = relPath.indexOf(':');
                    int lastColon = relPath.lastIndexOf(':');
                    if ((firstColon >= 0) && (firstColon == lastColon)) {
                        components.add(path.substring(0, rootPath.length() + firstColon + 1));
                    }
                    if (firstColon < 0 ) {
                        projects.add(path);
                    }
                }
            }
            paths.addAll(components);
            paths.addAll(projects);
            return true;
        }

        @Override
        protected Node createNodeForKey(String path) {
            Node ret = null;
            Map<String, File> subProjects = project.getGradleProject().getBaseProject().getSubProjects();
            File projectDir = subProjects.get(path);
            if (projectDir != null) {
                FileObject fo = FileUtil.toFileObject(projectDir);
                if (fo != null) {
                    try {
                        Project prj = ProjectManager.getDefault().findProject(fo);
                        if (prj != null && prj.getLookup().lookup(NbGradleProjectImpl.class) != null) {
                            NbGradleProjectImpl proj = (NbGradleProjectImpl) prj;
                            assert prj.getLookup().lookup(LogicalViewProvider.class) != null;
                            Node original = proj.getLookup().lookup(LogicalViewProvider.class).createLogicalView();
                            ret = new ProjectFilterNode(proj, original);
                        }
                    } catch (IllegalArgumentException | IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                } else {
                    //TODO broken module reference.. show as such..
                }
            } else {
                ret = new SubProjectsNode(project, path);
            }
            return ret;
        }

    }

    public static class ProjectFilterNode extends FilterNode {

        private final NbGradleProjectImpl project;

        ProjectFilterNode(NbGradleProjectImpl proj, Node original) {
            super(original, FilterNode.Children.LEAF);
//            disableDelegation(DELEGATE_GET_ACTIONS);
            project = proj;
        }

        @Override
        public Action[] getActions(boolean b) {
            ArrayList<Action> lst = new ArrayList<Action>();
            lst.add(OpenProjectAction.SINGLETON);
            return lst.toArray(new Action[lst.size()]);
        }

        @Override
        public Action getPreferredAction() {
            return OpenProjectAction.SINGLETON;
        }
    }

    private static class OpenProjectAction extends AbstractAction implements ContextAwareAction {

        static final OpenProjectAction SINGLETON = new OpenProjectAction();

        private OpenProjectAction() {
        }

        public @Override
        void actionPerformed(ActionEvent e) {
            assert false;
        }

        @NbBundle.Messages("BTN_Open_Project=Open Project")
        public @Override
        Action createContextAwareInstance(final Lookup context) {
            return new AbstractAction(BTN_Open_Project()) {
                public @Override
                void actionPerformed(ActionEvent e) {
                    Collection<? extends NbGradleProjectImpl> projects = context.lookupAll(NbGradleProjectImpl.class);
                    final NbGradleProjectImpl[] projectsArray = projects.toArray(new NbGradleProjectImpl[0]);
                    OpenProjects.getDefault().open(projectsArray, false, true);
                    if (projectsArray.length > 0) {
                        RequestProcessor.getDefault().post(new Runnable() {
                            public @Override
                            void run() {
                                OpenProjects.getDefault().open(projectsArray, false, true);
                            }
                        }, 500);
                    }
                }
            };
        }
    }

}
