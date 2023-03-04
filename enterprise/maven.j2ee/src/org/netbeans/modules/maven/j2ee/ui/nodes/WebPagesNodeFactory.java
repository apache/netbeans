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

package org.netbeans.modules.maven.j2ee.ui.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.spi.nodes.AbstractMavenNodeList;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.spi.ProjectWebRootProvider;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;

/**
 * This class is <i>immutable</i> and thus <i>thread safe</i>.
 *
 * @author mkleint
 * @author Martin Janicek <mjanicek@netbeans.org>
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-maven",position=50)
public class WebPagesNodeFactory implements NodeFactory {

    public WebPagesNodeFactory() {
    }

    @Override
    public NodeList createNodes(Project project) {
        return new WebRootNodeList(project);
    }


    /**
     * This class is <i>immutable</i> and thus <i>thread safe</i>.
     */
    private static class WebRootNodeList extends AbstractMavenNodeList<String> implements PropertyChangeListener{

        private final Project project;


        private WebRootNodeList(Project project) {
            this.project = project;
        }
        
        @Override
        public List<String> keys() {
            List<String> keys = new ArrayList<>();

            for (FileObject webRoot : getWebRoots()) {
                String webRootPath = webRoot.getPath();
                if (webRootPath.endsWith("/")) { // NOI18N
                    webRootPath = webRootPath.substring(0, webRootPath.length() - 1);
                }
                keys.add(webRootPath);
            }
            return keys;
        }
        
        @Override
        public Node node(String key) {
            for (FileObject webRoot : getWebRoots()) {
                if (key.equals(webRoot.getPath())) {
                    DataFolder fold = DataFolder.findFolder(webRoot);
                    File webAppFolder = FileUtil.toFile(webRoot);
                    if (fold != null) {
                        return new WebPagesNode(project, fold.getNodeDelegate().cloneNode(), webAppFolder);
                    }
                }
            }
            return null;
        }

        private Collection<FileObject> getWebRoots() {
            ProjectWebRootProvider webRootProvider = project.getLookup().lookup(ProjectWebRootProvider.class);
            if (webRootProvider != null) {
                return webRootProvider.getWebRoots();
            }
            return Collections.emptyList();
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                fireChange();
            }
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
