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

package org.netbeans.modules.gradle.java.nodes;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.nodes.AbstractGradleNodeList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.gradle.java.classpath.GradleSourcesImpl;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Laszlo Kishalmi
 */
@NodeFactory.Registration(projectType=NbGradleProject.GRADLE_PROJECT_TYPE, position=100)
public final class SourcesNodeFactory implements NodeFactory {

    private static final String WARNING_BADGE = "org/netbeans/modules/gradle/resources/warning-badge.png"; //NOI18N


    @Override
    public NodeList<?> createNodes(Project project) {
        return new NList(project);
    }
    
    private static class NList extends AbstractGradleNodeList<SourceGroup> implements ChangeListener {
        private static final RequestProcessor RP = new RequestProcessor(SourcesNodeFactory.NList.class);
        private final Project project;

        private List<SourceGroup> generatedGroups = Collections.emptyList();
        private final PropertyChangeListener pcl = (evt) -> {
            if (NbGradleProject.PROP_RESOURCES.equals(evt.getPropertyName())) {
                String path = ((URI) evt.getNewValue()).getPath();
                for (SourceGroup group : generatedGroups) {
                    if (path.startsWith(group.getRootFolder().toURI().getPath())) {
                        RP.post(this::fireChange);
                    }
                }
            }
        };
        private NList(Project prj) {
            project = prj;
            NbGradleProject.addPropertyChangeListener(project, WeakListeners.propertyChange(pcl, this));
        }
        
        @Override
        public List<SourceGroup> keys() {
            Sources srcs = ProjectUtils.getSources(project);
            List<SourceGroup> ret = new ArrayList<>();
            // Every Groovy SourceGroup is a Java SourceGroup as well
            ret.addAll(Arrays.asList(srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)));
            ret.addAll(Arrays.asList(srcs.getSourceGroups(GradleSourcesImpl.SOURCE_TYPE_KOTLIN)));
            ret.addAll(Arrays.asList(srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES)));
            List<SourceGroup> generated = Arrays.asList(srcs.getSourceGroups(GradleSourcesImpl.SOURCE_TYPE_GENERATED));
            ret.addAll(generated);
            generatedGroups = generated;
            ret.sort(Comparator.comparing(SourceGroup::getName));
            return ret;
        }
        
        @NbBundle.Messages({
            "# {0} - path of the group root",
            "# {1} - project name",
            "# {2} - bage icon",
            "ERR_WrongSG=<html>{0}<br/><img src=\"{2}\"/>&nbsp;<b>Alien sources from  {1}</b>"})
        @Override
        public Node node(SourceGroup group) {
            Project owner = FileOwnerQuery.getOwner(group.getRootFolder());
            if (owner == null) {
                //#152418 if project for folder is not found, just look the other way..
                Logger.getLogger(SourcesNodeFactory.class.getName()).log(Level.INFO, "Cannot find a project owner for folder {0}", group.getRootFolder()); //NOI18N
                return null;
            }
            // Do not display empty Generated SourceGroups
            if (generatedGroups.contains(group) && (group.getRootFolder() != null) && group.getRootFolder().getChildren().length == 0) {
                return null;
            }
            String name = group.getName();
            Node ret;
            switch(name) {
                case "42gatling.data":
                case "43gatling.bodies":
                case "49gatling.resources":
                    ret = ResourcesFolderNode.createResourcesFolderNode(group);
                    break;
                default:
                    ret = PackageView.createPackageView(group);
            }
            Path projectPath = FileUtil.toFile(project.getProjectDirectory()).toPath();
            Path groupPath = FileUtil.toFile(group.getRootFolder()).toPath();
            String relPath = projectPath.relativize(groupPath).toString();
            ret.setShortDescription(relPath);
            if (owner != project) {
                ret = new FilterNode(ret) {
                    @Override
                    public Image getIcon(int type) {
                        Image warn = ImageUtilities.loadImage(WARNING_BADGE);
                        return ImageUtilities.mergeImages(super.getIcon(type), warn, 8, 0);
                    }

                    @Override
                    public Image getOpenedIcon(int type) {
                        return getIcon(type);
                    }


                };
                String prjText = ProjectUtils.getInformation(owner).getDisplayName();
                ret.setShortDescription(Bundle.ERR_WrongSG(relPath, prjText, NbGradleProject.class.getClassLoader().getResource(WARNING_BADGE)));
            }
            return ret;
        }
        
        @Override
        public void addNotify() {
            Sources srcs = ProjectUtils.getSources(project);
            srcs.addChangeListener(this);
        }
        
        @Override
        public void removeNotify() {
            Sources srcs = ProjectUtils.getSources(project);
            srcs.removeChangeListener(this);
        }

        @Override
        public void stateChanged(ChangeEvent arg0) {
            //#167372 break the stack trace chain to prevent deadlocks.
            RP.post(this::fireChange);
        }
    }
    
    private static class ResourcesFolderNode extends FilterNode {

        final SourceGroup group;

        private ResourcesFolderNode(SourceGroup group, Node original) {
            super(original);
            this.group = group;
        }

        @Override
        public String getName() {
            return group.getName();
        }
        
        @Override
        public String getDisplayName() {
            return group.getDisplayName();
        }

        static Node createResourcesFolderNode(SourceGroup group) {
            try {
                DataObject root = DataObject.find(group.getRootFolder());
                return new ResourcesFolderNode(group, root.getNodeDelegate());
            } catch(DataObjectNotFoundException ex) {
                //Shall not happen...
            }
            return null;
        }

        @Override
        public boolean canRename() {
            return false;
        }

        @Override
        public boolean canDestroy() {
            return false;
        }

        @Override
        public boolean canCut() {
            return false;
        }
        
    }
}
