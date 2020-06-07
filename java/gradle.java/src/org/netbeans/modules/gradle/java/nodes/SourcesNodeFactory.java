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

package org.netbeans.modules.gradle.java.nodes;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.nodes.AbstractGradleNodeList;
import java.util.Arrays;
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
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Laszlo Kishalmi
 */
@NodeFactory.Registration(projectType=NbGradleProject.GRADLE_PROJECT_TYPE, position=100)
public final class SourcesNodeFactory implements NodeFactory {
    
    @Override
    public NodeList<?> createNodes(Project project) {
        return new NList(project);
    }
    
    private static class NList extends AbstractGradleNodeList<SourceGroup> implements ChangeListener {
        private static final RequestProcessor RP = new RequestProcessor(SourcesNodeFactory.NList.class);
        private final Project project;
        private NList(Project prj) {
            project = prj;
        }
        
        @Override
        public List<SourceGroup> keys() {
            Sources srcs = ProjectUtils.getSources(project);
            SourceGroup[] javagroup = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            return Arrays.asList(javagroup);
        }
        
        @NbBundle.Messages({"# {0} - label of source group", "# {1} - project name", "ERR_WrongSG={0} is owned by project {1}, cannot be used here, see issue #138310 for details."})
        @Override
        public Node node(SourceGroup group) {
            Project owner = FileOwnerQuery.getOwner(group.getRootFolder());
            if (owner != project) {
                if (owner == null) {
                    //#152418 if project for folder is not found, just look the other way..
                    Logger.getLogger(SourcesNodeFactory.class.getName()).log(Level.INFO, "Cannot find a project owner for folder {0}", group.getRootFolder()); //NOI18N
                    return null;
                }
                AbstractNode erroNode = new AbstractNode(Children.LEAF);
                String prjText = ProjectUtils.getInformation(owner).getDisplayName();
                //TODO: Could this happen? Use Bundle.
                erroNode.setDisplayName("Error Node: " + group.getDisplayName() + " " + prjText);
                return erroNode;
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
            ret.setShortDescription(FileUtil.getRelativePath(project.getProjectDirectory(), group.getRootFolder()));
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
            RP.post(new Runnable() {
                @Override
                public void run() {
                    fireChange();
                }
            });
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
