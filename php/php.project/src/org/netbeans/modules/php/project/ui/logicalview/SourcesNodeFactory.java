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
package org.netbeans.modules.php.project.ui.logicalview;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

/**
 *
 * @author Radek Matous
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-php-project", position=100)
public class SourcesNodeFactory implements NodeFactory {
    private static final Logger LOGGER = Logger.getLogger(SourcesNodeFactory.class.getName());

    public SourcesNodeFactory() {
    }

    @Override
    public NodeList<SourceGroup> createNodes(Project p) {
        PhpProject prj = p.getLookup().lookup(PhpProject.class);
        return new SourceChildrenList(prj);
    }

    private static final class SourceChildrenList implements NodeList<SourceGroup>, ChangeListener {

        private final PhpProject project;
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private final Sources projectSources;
        private final ChangeListener changeListener;


        private SourceChildrenList(PhpProject project) {
            this.project = project;
            projectSources = ProjectUtils.getSources(project);
            changeListener = WeakListeners.change(this, projectSources);
        }

        @Override
        public void addNotify() {
            projectSources.addChangeListener(changeListener);
        }

        @Override
        public void removeNotify() {
            projectSources.removeChangeListener(changeListener);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            // #132877 - discussed with tomas zezula
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    fireChange();
                }
            });
        }

        private void fireChange() {
            changeSupport.fireChange();
        }

        private DataFolder getFolder(FileObject fileObject) {
            if (fileObject != null && fileObject.isValid()) {
                try {
                    DataFolder dataFolder = DataFolder.findFolder(fileObject);
                    return dataFolder;
                } catch (Exception ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
            return null;
        }

        @Override
        public List<SourceGroup> keys() {
            // parse SG
            // update SG listeners
            // XXX check if this is necessary
            final SourceGroup[] sourceGroups = PhpProjectUtils.getSourceGroups(project);
            final SourceGroup[] groups = new SourceGroup[sourceGroups.length];
            System.arraycopy(sourceGroups, 0, groups, 0, sourceGroups.length);

            List<SourceGroup> keysList = new ArrayList<>(groups.length);
            //Set<FileObject> roots = new HashSet<FileObject>();
            FileObject fileObject;
            for (int i = 0; i < groups.length; i++) {
                fileObject = groups[i].getRootFolder();
                DataFolder srcDir = getFolder(fileObject);

                if (srcDir != null) {
                    keysList.add(groups[i]);
                }
            //roots.add(fileObject);
            }
            return keysList;
        // Seems that we do not need to implement FileStatusListener
        // to listen to source groups root folders changes.
        // look at RubyLogicalViewRootNode for example.
        //updateSourceRootsListeners(roots);
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        @Override
        public Node node(SourceGroup key) {
            Node node = null;
            if (key != null) {
                FileObject rootFolder = key.getRootFolder();
                DataFolder folder = getFolder(rootFolder);
                if (folder != null) {
                    boolean isTest = !folder.getPrimaryFile().equals(ProjectPropertiesSupport.getSourcesDirectory(project));
                    node = new SrcNode(project, folder, new PhpSourcesFilter(project, rootFolder), key.getDisplayName(), isTest);
                }
            }
            return node;
        }
    }
}
