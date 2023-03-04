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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.spi.nodes.AbstractMavenNodeList;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/**
 *
 * @author mkleint
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-maven",position=600)
public class SiteDocsNodeFactory implements NodeFactory {
    private static final String KEY_SITE = "SITE"; //NOI18N
    
    /** Creates a new instance of SiteDocsNodeFactory */
    public SiteDocsNodeFactory() {
    }
    
    @Override
    public NodeList createNodes(Project project) {
        NbMavenProjectImpl prj = project.getLookup().lookup(NbMavenProjectImpl.class);
        return new NList(prj);
    }
    
    
    private static class NList extends AbstractMavenNodeList<String> implements FileChangeListener, PropertyChangeListener {
        private final NbMavenProjectImpl project;
        
        private NList(NbMavenProjectImpl prj) {
            project = prj;
        }
        
        @Override
        public List<String> keys() {
            FileObject fo = FileUtil.toFileObject(Utilities.toFile(project.getSiteDirectory()));
            if (fo != null && fo.isValid()) {
                return Collections.singletonList(KEY_SITE);
            }
            return Collections.emptyList();
        }
        
        @Override
        public Node node(String key) {
            return createSiteDocsNode();
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
            FileUtil.addFileChangeListener(this, Utilities.toFile(project.getSiteDirectory()));
        }
        
        @Override
        public void removeNotify() {
            NbMavenProject.removePropertyChangeListener(project, this);
        }
        
        private Node createSiteDocsNode() {
            Node n =  null;
            FileObject fo = FileUtil.toFileObject(Utilities.toFile(project.getSiteDirectory()));
            if (fo != null) {
                DataFolder fold = DataFolder.findFolder(fo);
                if (fold != null) {
                    n = new SiteDocsNode(project, fold.getNodeDelegate().cloneNode());
                }
            }
            return n;
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            fireChange();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {           
        }

        @Override
        public void fileChanged(FileEvent fe) {           
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            fireChange();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) { 
            fireChange();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    }
}
