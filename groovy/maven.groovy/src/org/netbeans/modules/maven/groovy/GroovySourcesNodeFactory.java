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

package org.netbeans.modules.maven.groovy;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.spi.nodes.AbstractMavenNodeList;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

/**
 *
 * @author mkleint
 * @author Martin Janicek
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-maven",position=139)
public class GroovySourcesNodeFactory implements NodeFactory {

    // Copied from NbMavenProjectImpl because it's not part of the Maven API !!
    private static final String PROP_PROJECT = "MavenProject";   //NOI18N
    private static final String PROP_RESOURCE = "RESOURCES";      //NOI18N
    
    private static final String MAIN_GROOVY = "src/main/groovy"; //NOI18N
    private static final String TEST_GROOVY = "src/test/groovy"; //NOI18N


    @Override
    public NodeList<?> createNodes(Project project) {
        return new GroovyNodeList(project);
    }
    
    private static class GroovyNodeList extends AbstractMavenNodeList<SourceGroup> implements FileChangeListener, PropertyChangeListener {

        private final FileChangeListener changeListener = FileUtil.weakFileChangeListener(this, null);
        private final Project project;
        private final Sources sources;

        
        private GroovyNodeList(Project project) {
            this.project = project;
            this.sources = ProjectUtils.getSources(project);
        }
        
        @Override
        public List<SourceGroup> keys() {
            //#169192 check roots against java roots and if the same don't show twice.
            Set<FileObject> javaRoots = getJavaRoots();
            
            List<SourceGroup> groovySourceGroups = new ArrayList<SourceGroup>();
            for (SourceGroup sourceGroup : sources.getSourceGroups(GroovySourcesImpl.TYPE_GROOVY)) {
                if (!javaRoots.contains(sourceGroup.getRootFolder())) {
                    groovySourceGroups.add(sourceGroup);
                }
            }
            return groovySourceGroups;
        }

        private Set<FileObject> getJavaRoots() {
            Set<FileObject> javaRoots = new HashSet<FileObject>();
            for (SourceGroup sourceGroup : sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                javaRoots.add(sourceGroup.getRootFolder());
            }
            return javaRoots;
        }
        
        @Override
        public Node node(SourceGroup group) {
            return PackageView.createPackageView(group);
        }
        
        @Override
        public void addNotify() {
            NbMavenProject watch = project.getLookup().lookup(NbMavenProject.class);
            watch.addPropertyChangeListener(this);
            watch.addWatchedPath(MAIN_GROOVY);
            watch.addWatchedPath(TEST_GROOVY);
            checkFileObject(MAIN_GROOVY);
            checkFileObject(TEST_GROOVY);
        }
        
        private void checkFileObject(String path) {
            FileObject fo = project.getProjectDirectory().getFileObject(path);
            if (fo != null) {
                fo.removeFileChangeListener(changeListener);
                fo.addFileChangeListener(changeListener);
            }
        }

        @Override
        public void removeNotify() {
            NbMavenProject watch = project.getLookup().lookup(NbMavenProject.class);
            watch.removePropertyChangeListener(this);
            watch.removeWatchedPath(MAIN_GROOVY);
            watch.removeWatchedPath(TEST_GROOVY);
            removeListener(MAIN_GROOVY);
            removeListener(TEST_GROOVY);
        }

        private void removeListener(String rootPath) {
            FileObject fo = project.getProjectDirectory().getFileObject(rootPath);
            if (fo != null) {
                fo.removeFileChangeListener(changeListener);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String propertyName = evt.getPropertyName();
            final Object newValue = evt.getNewValue();
            if (PROP_PROJECT.equals(propertyName) || PROP_RESOURCE.equals(propertyName)) {
                if (newValue != null) {
                    if (newValue.toString().contains(MAIN_GROOVY)) {
                        fireChange();
                        checkFileObject(MAIN_GROOVY);
                    } else if (newValue.toString().contains(TEST_GROOVY)) {
                        fireChange();
                        checkFileObject(TEST_GROOVY);
                    }
                }
            }
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
            fe.getFile().removeFileChangeListener(this);
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
