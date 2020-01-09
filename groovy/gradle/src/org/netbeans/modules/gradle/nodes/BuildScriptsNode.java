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
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
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

/**
 *
 * @author Laszlo Kishalmi
 */
public final class BuildScriptsNode extends AnnotatedAbstractNode {

    @StaticResource
    private static final String BS_BADGE
            = "org/netbeans/modules/gradle/resources/buildscripts-badge.png";

    @NbBundle.Messages("LBL_Build_Scripts=Build Scripts")
    public BuildScriptsNode(NbGradleProjectImpl prj) {
        super(Children.create(new ProjectFilesChildren(prj), true),
                Lookups.fixed(prj.getProjectDirectory()));
        setName("buildscripts"); //NOI18N
        setDisplayName(Bundle.LBL_Build_Scripts());
    }

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
            if (isRoot
                    && ((key.second() == GradleFiles.Kind.ROOT_SCRIPT)
                    || (key.second() == GradleFiles.Kind.ROOT_PROPERTIES))) {
                return null;
            }
            try {
                Node node = DataObject.find(key.first()).getNodeDelegate().cloneNode();
                String nameSuffix = null;
                if (key.second() != null) {
                    if (key.second() == GradleFiles.Kind.USER_PROPERTIES) {
                        nameSuffix = Bundle.LBL_UserSuffix();
                    }
                    if (!isRoot) {
                        switch (key.second()) {
                            case BUILD_SCRIPT:
                            case PROJECT_PROPERTIES: {
                                nameSuffix = Bundle.LBL_ProjectSuffixt();
                                break;
                            }
                            case ROOT_SCRIPT:
                            case ROOT_PROPERTIES: {
                                nameSuffix = Bundle.LBL_RootSuffix();
                                break;
                            }
                        }
                    }
                }
                if (nameSuffix != null) {
                    node.setDisplayName(key.first().getNameExt() + " [" + nameSuffix + "]");
                }
                return node;
            } catch (DataObjectNotFoundException e) {
                return null;
            }
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
            for (GradleFiles.Kind kind : GradleFiles.Kind.values()) {
                File f = gf.getFile(kind);
                if ((f != null) && f.isFile()) {
                    keys.add(Pair.of(FileUtil.toFileObject(f), kind));
                }
            }
            return true;
        }
    }

}
