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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.netbeans.spi.queries.VisibilityQueryChangeEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataObject;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Radek Matous
 */
public class PhpSourcesFilter implements  ChangeListener, ChangeableDataFilter {
        private static final long serialVersionUID = -743974567467955L;

        private final PhpProject project;
        private final FileObject rootFolder;
        private final PhpVisibilityQuery phpVisibilityQuery;
        // can be null when e.g. deleting project
        private final FileObject nbProject;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        public PhpSourcesFilter(PhpProject project) {
            this(project, null);
        }
        public PhpSourcesFilter(PhpProject project, FileObject rootFolder) {
            assert project != null;

            this.project = project;
            this.rootFolder = rootFolder;

            phpVisibilityQuery = PhpVisibilityQuery.forProject(project);
            nbProject = project.getProjectDirectory().getFileObject("nbproject"); // NOI18N

            ProjectPropertiesSupport.addWeakIgnoredFilesListener(project, this);
        }

        @Override
        public boolean acceptDataObject(DataObject object) {
            return acceptFileObject(object.getPrimaryFile());
        }

        public boolean acceptFileObject(FileObject file) {
            return !isNbProject(file)
                    && !isTestDirectory(file)
                    && !isSeleniumDirectory(file)
                    && phpVisibilityQuery.isVisible(file);
        }

        private boolean isNbProject(FileObject file) {
            return file.equals(nbProject);
        }

        private boolean isTestDirectory(FileObject file) {
            for (FileObject root : ProjectPropertiesSupport.getTestDirectories(project, false)) {
                if (isDirectory(file, root)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isSeleniumDirectory(FileObject file) {
            return isDirectory(file, ProjectPropertiesSupport.getSeleniumDirectory(project, false));
        }

        private boolean isDirectory(FileObject file, FileObject directory) {
            if (rootFolder == null || directory == null) {
                return false;
            }
            if (!directory.equals(rootFolder)) {
                // in sources or similar (but not in 'directory' definitely)
                return directory.equals(file);
            }
            return false;
        }

        @Override
        public void stateChanged(ChangeEvent event) {
            if (event instanceof VisibilityQueryChangeEvent) {
                FileObject[] fileObjects = ((VisibilityQueryChangeEvent) event).getFileObjects();
                for (FileObject fileObject : fileObjects) {
                    if (project.equals(ProjectConvertors.getNonConvertorOwner(fileObject))) {
                        changeSupport.fireChange();
                        return;
                    }
                }
                return;
            }
            changeSupport.fireChange();
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }
    }
