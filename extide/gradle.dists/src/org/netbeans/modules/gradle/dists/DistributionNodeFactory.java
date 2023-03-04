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
package org.netbeans.modules.gradle.dists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.nodes.AbstractGradleNodeList;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 *
 * @author lkishalmi
 */
@NodeFactory.Registration(projectType = NbGradleProject.GRADLE_PROJECT_TYPE, position = 150)
public class DistributionNodeFactory implements NodeFactory {

    @Override
    public NodeList<?> createNodes(Project project) {
        return new NList(project);
    }
    
    private static class NList extends AbstractGradleNodeList<SourceGroup> {
        private final Project project;
        private NList(Project prj) {
            project = prj;
        }

        @Override
        public List<SourceGroup> keys() {
            Sources srcs = ProjectUtils.getSources(project);
            List<SourceGroup> ret = new ArrayList<>(2);
            ret.addAll(Arrays.asList(srcs.getSourceGroups(DistributionSourcesImpl.DISTRIBUTION_SOURCES)));
            ret.sort(Comparator.comparing(SourceGroup::getName));
            return ret;
        }

        @Override
        public Node node(SourceGroup key) {
            return DistributionFolderNode.createResourcesFolderNode(key);
        }
        
    }
    
    private static class DistributionFolderNode extends FilterNode {

        final SourceGroup group;

        private DistributionFolderNode(SourceGroup group, Node original) {
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
                return new DistributionFolderNode(group, root.getNodeDelegate());
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
        public boolean canCut() {
            return false;
        }
        
    }
    
}
