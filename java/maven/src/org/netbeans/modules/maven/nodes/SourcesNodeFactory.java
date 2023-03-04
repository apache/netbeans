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

package org.netbeans.modules.maven.nodes;

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
import org.netbeans.modules.maven.NbMavenProjectImpl;
import static org.netbeans.modules.maven.nodes.Bundle.*;
import org.netbeans.modules.maven.spi.nodes.AbstractMavenNodeList;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-maven",position=100)
public class SourcesNodeFactory implements NodeFactory {
    
    @Override
    public NodeList<?> createNodes(Project project) {
        NbMavenProjectImpl prj = project.getLookup().lookup(NbMavenProjectImpl.class);
        return new NList(prj);
    }
    
    private static class NList extends AbstractMavenNodeList<SourceGroup> implements ChangeListener {
        private static final RequestProcessor RP = new RequestProcessor(SourcesNodeFactory.NList.class);
        private final NbMavenProjectImpl project;
        private NList(NbMavenProjectImpl prj) {
            project = prj;
        }
        
        @Override
        public List<SourceGroup> keys() {
            Sources srcs = ProjectUtils.getSources(project);
            SourceGroup[] javagroup = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            return Arrays.asList(javagroup);
        }
        
        @Messages({"# {0} - label of source group", "# {1} - project name", "ERR_WrongSG={0} is owned by project {1}, cannot be used here, see issue #138310 for details."})
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
                erroNode.setDisplayName(ERR_WrongSG(group.getDisplayName(), prjText));
                return erroNode;
            }
            return PackageView.createPackageView(group);
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
}
