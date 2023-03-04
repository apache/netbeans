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

package org.netbeans.modules.gradle.javaee.nodes;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.nodes.AbstractGradleNodeList;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.spi.ProjectWebRootProvider;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;

/**
 *
 * @author Laszlo Kishalmi
 */
@NodeFactory.Registration(projectType = NbGradleProject.GRADLE_PROJECT_TYPE, position = 50)
public class WebPagesNodeFactory implements NodeFactory {

    @Override
    public NodeList<?> createNodes(Project p) {
        return new WebRootNodeList(p);
    }

    private static class WebRootNodeList extends AbstractGradleNodeList<FileObject> implements PropertyChangeListener {

        final Project project;

        public WebRootNodeList(Project project) {
            this.project = project;
        }

        @Override
        public List<FileObject> keys() {
            ProjectWebRootProvider pvd = project.getLookup().lookup(ProjectWebRootProvider.class);
            return pvd != null ? new ArrayList<>(pvd.getWebRoots()) : Collections.<FileObject>emptyList();
        }

        @Override
        public Node node(FileObject key) {
            DataFolder df = DataFolder.findFolder(key);
            return new WebPagesNode(df.getNodeDelegate().cloneNode(), key);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                fireChange();
            }
        }

        @Override
        public void removeChangeListener(ChangeListener list) {
            NbGradleProject.removePropertyChangeListener(project, this);
        }

        @Override
        public void addChangeListener(ChangeListener list) {
            NbGradleProject.addPropertyChangeListener(project, this);
        }

    }
}
