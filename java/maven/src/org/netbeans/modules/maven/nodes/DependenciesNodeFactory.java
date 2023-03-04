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

import org.netbeans.modules.maven.DependencyType;
import org.netbeans.modules.maven.spi.nodes.AbstractMavenNodeList;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;

/**
 *
 * @author mkleint
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-maven",position=500)
public class DependenciesNodeFactory implements NodeFactory {
    
    @Override public NodeList<?> createNodes(Project project) {
        NbMavenProjectImpl prj = project.getLookup().lookup(NbMavenProjectImpl.class);
        return new NList(prj);
    }
    
    private static class NList extends AbstractMavenNodeList<DependenciesNode.DependenciesSet> implements PropertyChangeListener {
        private final NbMavenProjectImpl project;
        private final DependenciesNode.DependenciesSet compile;
        private final DependenciesNode.DependenciesSet runtime;
        private final DependenciesNode.DependenciesSet test;
        private final DependenciesNode.DependenciesSet noncp;
        NList(NbMavenProjectImpl prj) {
            project = prj;
            compile = new DependenciesNode.DependenciesSet(project, DependencyType.COMPILE);
            runtime = new DependenciesNode.DependenciesSet(project, DependencyType.RUNTIME);
            test = new DependenciesNode.DependenciesSet(project, DependencyType.TEST);
            noncp = new DependenciesNode.DependenciesSet(project, DependencyType.NONCP);
        }
        
        @Override public void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                fireChange();
            }
        }
        
        @Override public List<DependenciesNode.DependenciesSet> keys() {
            List<DependenciesNode.DependenciesSet> list = new ArrayList<DependenciesNode.DependenciesSet>();
            list.add(compile);
            if (!runtime.list(false).isEmpty()) {
                list.add(runtime);
            }
            if (!test.list(false).isEmpty()) {
                list.add(test);
            }
            if (!noncp.list(false).isEmpty()) {
                list.add(noncp);
            }
            return list;
        }
        
        @Override public Node node(DependenciesNode.DependenciesSet key) {
            return new DependenciesNode(key);
        }
        
        @Override
        public void addNotify() {
            NbMavenProject.addPropertyChangeListener(project, this);
        }
        
        @Override
        public void removeNotify() {
            NbMavenProject.removePropertyChangeListener(project, this);
        }
    }
}
