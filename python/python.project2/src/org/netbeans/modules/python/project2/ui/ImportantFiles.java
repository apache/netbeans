/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.python.project2.ui;

import java.awt.EventQueue;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.python.project2.ImportantFilesImplementation;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.StatusDecorator;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 *
 * @see org.netbeans.modules.web.common.ui.ImportantFiles
 */
public class ImportantFiles {
    static final Logger LOGGER = Logger.getLogger(ImportantFiles.class.getName());
    static final String[] PYTHON_IMPORTANT_FILES = {
        "setup.py", // NOI18N
        "development.ini", // NOI18N, Pyramid config file
        "production.ini", // NOI18N, Pyramid config file
        ".hgignore", // NOI18N
        ".gitignore", // NOI18N
        "MANIFEST", // NOI18N
        "MANIFEST.in", // NOI18N
        "README", // NOI18N
        "README.md", // NOI18N
        "README.rst", // NOI18N
        "LICENSE", // NOI18N
        "COPYING", // NOI18N
        // Add various files related to CI
        "tox.ini", // NOI18N, https://pypi.python.org/pypi/tox
        ".gitlab-ci.yml", // NOI18N, https://about.gitlab.com/gitlab-ci/
        "circle.yml", // NOI18N, https://circleci.com/
        ".travis.yml", // NOI18N, http://travis-ci.org/
    };

    private ImportantFiles() {
    }

    public static NodeFactory forPython2Project() {
        return new ImportantFilesNodeFactory();
    }

    public static class ImportantFilesNodeFactory implements NodeFactory {
        @Override
        public NodeList<?> createNodes(@NonNull Project project) {
            return new ImportantFilesNodeList(project);
        }
    }

    private static class ImportantFilesNodeList implements NodeList<Object>, LookupListener, ChangeListener {
        private static final Object IMPORTANT_FILES_KEY = new Object();

        private final Lookup.Result<ImportantFilesImplementation> lookupResult;
        private final ImportantFilesChildren importantFilesChildren;
        final ChangeSupport changeSupport = new ChangeSupport(this);

        private Node importantFilesNode;

        ImportantFilesNodeList(@NonNull Project project) {
            importantFilesChildren = new ImportantFilesChildren(project, changeSupport);
            lookupResult = project.getLookup().lookupResult(ImportantFilesImplementation.class);
        }

        @Override
        public List<Object> keys() {
            if (!importantFilesChildren.hasImportantFiles()) {
                return Collections.emptyList();
            }
            return Collections.singletonList(IMPORTANT_FILES_KEY);
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }

        @Override
        public synchronized Node node(Object key) {
            assert key == IMPORTANT_FILES_KEY : "Unexpected key " + key;//NOI18N
            if (importantFilesNode == null) {
                importantFilesNode = new ImportantFilesNode(importantFilesChildren);
            }
            return importantFilesNode;
        }

        @Override
        public void addNotify() {
            lookupResult.addLookupListener(WeakListeners.create(LookupListener.class, this, lookupResult));
            for (ImportantFilesImplementation provider : lookupResult.allInstances()) {
                provider.addChangeListener(WeakListeners.change(this, provider));
            }
        }

        @Override
        public void removeNotify() {
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            fireChange();
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            importantFilesChildren.refreshImportantFiles();
            fireChange();
        }

        private void fireChange() {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    changeSupport.fireChange();
                }
            });
        }
    }

    private static final class ImportantFilesNode extends AbstractNode {

        @StaticResource
        private static final String BADGE = "org/netbeans/modules/python/project2/resources/config-badge.gif"; // NOI18N

        private final Node iconDelegate;

        ImportantFilesNode(Children children) {
            super(children);
            iconDelegate = DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
        }

        @Override
        public String getDisplayName() {
            return "Important Files";
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.mergeImages(iconDelegate.getIcon(type), ImageUtilities.loadImage(BADGE), 7, 7);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[0];
        }

    }

    private static final class ImportantFilesChildren extends Children.Keys<ImportantFilesImplementation.FileInfo> {

        private static final Logger LOGGER = Logger.getLogger(ImportantFilesChildren.class.getName());
        private final FileObject projectDirectory;
        private final ChangeSupport changeSupport;

        ImportantFilesChildren(@NonNull Project project, ChangeSupport changeSupport) {
            super(true);
            this.projectDirectory = project.getProjectDirectory();
            this.changeSupport = changeSupport;

            FileUtil.addFileChangeListener(new FileChangeListener() {
                @Override
                public void fileFolderCreated(FileEvent fe) {
                }

                @Override
                public void fileDataCreated(FileEvent fe) {
                    refreshImportantFiles();
                    fireChange();
                }

                @Override
                public void fileChanged(FileEvent fe) {
                }

                @Override
                public void fileDeleted(FileEvent fe) {
                    refreshImportantFiles();
                    fireChange();
                }

                @Override
                public void fileRenamed(FileRenameEvent fe) {
                    refreshImportantFiles();
                    fireChange();
                }

                @Override
                public void fileAttributeChanged(FileAttributeEvent fe) {
                }
            });
        }

        public boolean hasImportantFiles() {
            return !getImportantFiles().isEmpty();
        }

        private void refreshImportantFiles() {
            setKeys();
        }

        private void fireChange() {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    changeSupport.fireChange();
                }
            });
        }

        @Override
        protected Node[] createNodes(ImportantFilesImplementation.FileInfo key) {
            assert key != null;
            try {
                return new Node[]{new ImportantFileNode(key)};
            } catch (DataObjectNotFoundException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return new Node[0];
        }

        @Override
        protected void addNotify() {
            setKeys();
        }

        @Override
        protected void removeNotify() {
            setKeys(Collections.<ImportantFilesImplementation.FileInfo>emptyList());
        }

        private void setKeys() {
            List<ImportantFilesImplementation.FileInfo> importantFiles = getImportantFiles();
            Collections.sort(importantFiles, new FileInfoComparator());
            if (!ProjectManager.mutex().isReadAccess() && !ProjectManager.mutex().isWriteAccess()) {
                setKeys(importantFiles);
            }
        }

        private List<ImportantFilesImplementation.FileInfo> getImportantFiles() {
            Set<ImportantFilesImplementation.FileInfo> importantFiles = new LinkedHashSet<>();
            for (String fileName : PYTHON_IMPORTANT_FILES) {
                final FileObject file = projectDirectory.getFileObject(fileName);
                if (file != null) {
                    importantFiles.add(new ImportantFilesImplementation.FileInfo(file));
                }
            }

            return new ArrayList<>(importantFiles);
        }

    }

    private static final class ImportantFileNode extends FilterNode {

        private final ImportantFilesImplementation.FileInfo fileInfo;

        ImportantFileNode(ImportantFilesImplementation.FileInfo fileInfo) throws DataObjectNotFoundException {
            super(DataObject.find(fileInfo.getFile()).getNodeDelegate());
            this.fileInfo = fileInfo;
        }

        @Override
        public String getDisplayName() {
            String displayName = fileInfo.getDisplayName();
            if (displayName != null) {
                return displayName;
            }
            return super.getDisplayName();
        }

        @Override
        public String getHtmlDisplayName() {
            String displayName = getDisplayName();
            assert displayName != null : fileInfo;
            StatusDecorator statusDecorator = getStatusDecorator();
            if (statusDecorator != null) {
                return statusDecorator.annotateNameHtml(displayName, Collections.singleton(fileInfo.getFile()));
            }
            return displayName;
        }

        @Override
        public String getShortDescription() {
            String description = fileInfo.getDescription();
            if (description != null) {
                return description;
            }
            return super.getShortDescription();
        }

        @CheckForNull
        private StatusDecorator getStatusDecorator() {
            try {
                return fileInfo.getFile().getFileSystem().getDecorator();
            } catch (FileStateInvalidException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
            return null;
        }

    }

    private static final class FileInfoComparator implements Comparator<ImportantFilesImplementation.FileInfo> {

        @Override
        public int compare(ImportantFilesImplementation.FileInfo fileInfo1, ImportantFilesImplementation.FileInfo fileInfo2) {
            FileObject file1 = fileInfo1.getFile();
            FileObject file2 = fileInfo2.getFile();
            try {
                return DataFolder.SortMode.FOLDER_NAMES.compare(DataObject.find(file1), DataObject.find(file2));
            } catch (DataObjectNotFoundException ex) {
                return file1.getNameExt().compareToIgnoreCase(file2.getNameExt());
            }
        }

    }

}
