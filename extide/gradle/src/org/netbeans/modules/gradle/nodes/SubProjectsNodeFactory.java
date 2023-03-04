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

import org.netbeans.modules.gradle.spi.nodes.AbstractGradleNodeList;
import org.netbeans.modules.gradle.NbGradleProjectImpl;
import org.netbeans.modules.gradle.api.NbGradleProject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.GradleProject;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;

/**
 *
 * @author Laszlo Kishalmi
 */
@NodeFactory.Registration(projectType = NbGradleProject.GRADLE_PROJECT_TYPE, position = 400)
public class SubProjectsNodeFactory implements NodeFactory {

    @Override
    public NodeList<?> createNodes(Project project) {
        NbGradleProjectImpl prj = project.getLookup().lookup(NbGradleProjectImpl.class);
        return new NList(prj);
    }

    private static class NList extends AbstractGradleNodeList<GradleProject> implements PropertyChangeListener {

        private final NbGradleProjectImpl project;

        NList(NbGradleProjectImpl prj) {
            project = prj;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                fireChange();
            }
        }

        @Override
        public List<GradleProject> keys() {
            Map<String, File> subProjects = project.getGradleProject().getBaseProject().getSubProjects();
            return subProjects.isEmpty() ? Collections.<GradleProject>emptyList() : Collections.singletonList(project.getGradleProject());
        }

        @Override
        public Node node(GradleProject key) {
            return new SubProjectsNode(project);
        }

        @Override
        public void addNotify() {
            NbGradleProject.addPropertyChangeListener(project, this);
        }

        @Override
        public void removeNotify() {
            NbGradleProject.removePropertyChangeListener(project, this);
        }
    }
}
