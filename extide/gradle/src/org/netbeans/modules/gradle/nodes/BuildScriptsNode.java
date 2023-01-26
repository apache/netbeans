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
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.netbeans.modules.gradle.NbGradleProjectImpl;
import org.netbeans.modules.gradle.api.NbGradleProject;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.gradle.spi.GradleFiles.Kind;
import org.netbeans.modules.gradle.spi.GradleSettings;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Pair;
import org.openide.util.lookup.Lookups;

import static org.netbeans.modules.gradle.spi.GradleFiles.Kind.*;
import org.netbeans.spi.project.ui.PathFinder;
import org.openide.util.Exceptions;
/**
 *
 * @author Laszlo Kishalmi
 */
public final class BuildScriptsNode extends AnnotatedAbstractNode {

    private static final Logger LOG = Logger.getLogger(BuildScriptsNode.class.getName());
    
    @StaticResource
    private static final String BS_BADGE
            = "org/netbeans/modules/gradle/resources/buildscripts-badge.png";

    @NbBundle.Messages("LBL_Build_Scripts=Build Scripts")
    public BuildScriptsNode(NbGradleProjectImpl prj) {
        super(Children.create(new ProjectFilesChildren(prj), true),
                Lookups.fixed(prj.getProjectDirectory(), new Finder(prj)));
        setName("buildscripts"); //NOI18N
        setDisplayName(Bundle.LBL_Build_Scripts());
    }

    // The order in this array determines the order of the nodes under Build Scripts
    private static final Kind[] SCRIPTS = new Kind[] {
        BUILD_SRC, VERSION_CATALOG, USER_PROPERTIES, SETTINGS_SCRIPT, ROOT_SCRIPT, ROOT_PROPERTIES, BUILD_SCRIPT, PROJECT_PROPERTIES
    };

    @Override
    protected Image getIconImpl(int param) {
        return getIcon(false);
    }

    @Override
    protected Image getOpenedIconImpl(int param) {
        return getIcon(true);
    }

    private Image getIcon(boolean opened) {
        Image badge = ImageUtilities.loadImage(BS_BADGE, true); //NOI18N
        Image img = ImageUtilities.mergeImages(NodeUtils.getTreeFolderIcon(opened), badge, 8, 8);
        return img;
    }

    private static class Finder implements PathFinder {

        final Project project;

        public Finder(Project project) {
            this.project = project;
        }
        
        @Override
        public Node findPath(Node node, Object target) {
        if (target instanceof FileObject) {
            FileObject fo = (FileObject) target;
            if (project != FileOwnerQuery.getOwner(fo)) {
                return null; // Don't waste time if project does not own the fo
            }
            Node[] nodes = node.getChildren().getNodes(true);
            for (Node n : nodes) {
                FileObject nf = n.getLookup().lookup(FileObject.class);
                if ((nf != null) && (nf.equals(fo))) {
                   return n;
                }
            }
        }

        return null;
        }
    }
    
    private static class ProjectFilesChildren extends ChildFactory.Detachable<Pair<FileObject, GradleFiles.Kind>> implements PropertyChangeListener {

        private final NbGradleProjectImpl project;
        private final FileChangeAdapter fileChangeListener;
        private final PreferenceChangeListener prefChangeListener;

        ProjectFilesChildren(NbGradleProjectImpl proj) {
            project = proj;
            fileChangeListener = new FileChangeAdapter() {
                @Override
                public void fileDataCreated(FileEvent fe) {
                    refresh(false);
                }

                @Override
                public void fileDeleted(FileEvent fe) {
                    refresh(false);
                }
            };
            prefChangeListener = (PreferenceChangeEvent evt) -> {
                if (GradleSettings.PROP_GRADLE_USER_HOME.equals(evt.getKey())) {
                    refresh(false);
                }
            };
        }

        @Messages({
            "LBL_ProjectSuffixt=project",
            "LBL_RootSuffix=root",
            "LBL_UserSuffix=user"
        })
        @Override
        protected Node createNodeForKey(Pair<FileObject, GradleFiles.Kind> key) {
            // Do not show root script and property nodes on root project.
            boolean isRoot = project.getGradleProject().getBaseProject().isRoot();
            FileObject fo = key.first();
            switch (key.second()) {
                case ROOT_SCRIPT:
                case ROOT_PROPERTIES:
                    return isRoot ? null : createBuildFileNode(fo, Bundle.LBL_RootSuffix());
                case BUILD_SCRIPT:
                case PROJECT_PROPERTIES:
                    return createBuildFileNode(fo, isRoot ? null : Bundle.LBL_ProjectSuffixt());
                case USER_PROPERTIES:
                    return createBuildFileNode(fo, Bundle.LBL_UserSuffix());
                case SETTINGS_SCRIPT:
                case VERSION_CATALOG:
                    return createBuildFileNode(fo, null);
                case BUILD_SRC:
                    return createSubProjectNode(fo);
                default:
                    return null;
            }
        }

        private static Node createSubProjectNode(FileObject fo) {
            try {
                Project prj = ProjectManager.getDefault().findProject(fo);
                if (prj != null) {
                    return SubProjectsNode.createSubProjectNode(prj);
                } else {
                    LOG.log(Level.WARNING, "It seems {0} was not identified as a buildSrc project.", fo.getPath());
                }
            } catch (IOException | IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        private static Node createBuildFileNode(FileObject fo, String nameSuffix) {
            Node ret = null;
            try {
                ret = DataObject.find(fo).getNodeDelegate().cloneNode();
                if (nameSuffix != null) {
                    ret.setDisplayName(fo.getNameExt() + " [" + nameSuffix + "]");
                }
            } catch (DataObjectNotFoundException ex) {}
            return ret;
        }

        public @Override
        void propertyChange(PropertyChangeEvent evt) {
            if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                refresh(false);
            }
        }

        @Override
        protected void addNotify() {
            NbGradleProject.addPropertyChangeListener(project, this);
            project.getProjectDirectory().addFileChangeListener(fileChangeListener);
            GradleSettings.getDefault().getPreferences().addPreferenceChangeListener(prefChangeListener);
        }

        @Override
        protected void removeNotify() {
            NbGradleProject.removePropertyChangeListener(project, this);
            project.getProjectDirectory().removeFileChangeListener(fileChangeListener);
            GradleSettings.getDefault().getPreferences().removePreferenceChangeListener(prefChangeListener);
        }

        @Override
        protected boolean createKeys(List<Pair<FileObject, GradleFiles.Kind>> keys) {
            GradleFiles gf = project.getGradleFiles();
            for (GradleFiles.Kind kind : SCRIPTS) {
                File f = gf.getFile(kind);
                if ((f != null) && f.exists()) {
                    keys.add(Pair.of(FileUtil.toFileObject(f), kind));
                }
            }
            return true;
        }
    }

}
