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

package org.netbeans.modules.gradle.nodes;

import org.netbeans.modules.gradle.spi.nodes.NodeUtils;
import org.netbeans.modules.gradle.NbGradleProjectImpl;
import org.netbeans.modules.gradle.api.NbGradleProject;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import static org.netbeans.modules.gradle.nodes.Bundle.*;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.spi.GradleSettings;
import org.netbeans.modules.gradle.spi.Utils;
import org.openide.nodes.Children;
import org.openide.util.WeakListeners;

/**
 *
 * @author Laszlo Kishalmi
 */
public class SubProjectsNode extends AbstractNode {

    private static final Logger LOG = Logger.getLogger(SubProjectsNode.class.getName());
    
    @StaticResource
    private static final String SP_BADGE
            = "org/netbeans/modules/gradle/resources/gradle-large-badge.png";

    @NbBundle.Messages("LBL_SubProjects=Sub Projects")
    public SubProjectsNode(NbGradleProjectImpl proj, String path) {
        super(Children.create(new SubProjectsChildFactory(proj), true));
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

    private static class SubProjectsChildFactory extends ChildFactory<Project> {

        private final Project project;
        private final PropertyChangeListener propListener;
        private final PreferenceChangeListener prefListener;

        SubProjectsChildFactory(Project proj) {
            project = proj;
            propListener = (PropertyChangeEvent evt) -> {
                if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                    ProjectManager.getDefault().clearNonProjectCache();
                    refresh(false);
                }
            };
            NbGradleProject.addPropertyChangeListener(project, WeakListeners.propertyChange(propListener, NbGradleProject.get(project)));

            prefListener = (evt) -> {
                if (GradleSettings.PROP_DISPLAY_DESCRIPTION.equals(evt.getKey())) {
                    refresh(false);
                }
            };
            Preferences prefs = GradleSettings.getDefault().getPreferences();
            prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefListener, prefs));
        }

        @Override
        protected boolean createKeys(final List<Project> projects) {

            Set<Project> containedProjects = ProjectUtils.getContainedProjects(project, false);
            if (containedProjects != null) {
                ArrayList<Project> ret = new ArrayList<>(containedProjects);
                if (GradleSettings.getDefault().isDisplayDesctiption()) {
                    ret.sort(Comparator.comparing((Project p) -> ProjectUtils.getInformation(p).getDisplayName()));
                } else {
                    ret.sort(Comparator.comparing((Project p) -> ProjectUtils.getInformation(p).getName()));
                }
                projects.addAll(ret);
            } else {
                LOG.log(Level.FINE, "No ProjectContainerProvider in the lookup of: {0}", project);
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(Project key) {
            Set<Project> containedProjects = ProjectUtils.getContainedProjects(key, false);
            if (containedProjects == null) {
                containedProjects = Collections.emptySet();
                LOG.log(Level.FINE, "No ProjectContainerProvider in the lookup of: {0}", project);                
            }
            GradleBaseProject gbp = GradleBaseProject.get(project);
            String prefix = (gbp != null && !gbp.isRoot() ? gbp.getPath() : "") + ':';
            Children ch = containedProjects.isEmpty() ? Children.LEAF : Children.create(new SubProjectsChildFactory(key), true);
            return createSubProjectNode(key, prefix, ch);
        }

    }

    public static Node createSubProjectNode(Project prj) {
        return createSubProjectNode(prj, null, Children.LEAF);
    }

    public static Node createSubProjectNode(Project prj, String path, Children children) {
        Node ret = null;
        if (prj.getLookup().lookup(NbGradleProjectImpl.class) != null) {
            assert prj.getLookup().lookup(LogicalViewProvider.class) != null;
            Node original = prj.getLookup().lookup(LogicalViewProvider.class).createLogicalView();
            ret = new ProjectFilterNode(path, original, children);
        }
        return ret;
    }

    public static class ProjectFilterNode extends FilterNode {

        private final String prefix;

        ProjectFilterNode(String prefix, Node original, org.openide.nodes.Children children) {
            super(original, children);
            this.prefix = prefix;
        }

        ProjectFilterNode(Node original) {
            this(null, original, Children.LEAF);
        }

        @Override
        public String getDisplayName() {
            boolean usePath = super.getName().equals(super.getDisplayName());
            if (usePath && (prefix != null)) {
                GradleBaseProject gbp = GradleBaseProject.get(getLookup().lookup(Project.class));
                if (gbp != null) {
                    String path = gbp.getPath();
                    return path.startsWith(prefix)  && path.length() > prefix.length()
                    ? path.substring(prefix.length())
                    : path;
                }
            }
            return super.getDisplayName();
        }


        @Override
        public Action[] getActions(boolean b) {
            return new Action[]{OpenProjectAction.SINGLETON};
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
                        RequestProcessor.getDefault().post(() -> {
                            OpenProjects.getDefault().open(projectsArray, false, true);
                        }, 500);
                    }
                }
            };
        }
    }

}
