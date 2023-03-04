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

import java.awt.EventQueue;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.phpmodule.ImportantFilesImplementation;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
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
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

@NodeFactory.Registration(projectType = "org-netbeans-modules-php-project", position = 250)
public final class FrameworkConfigFilesNodeFactory implements NodeFactory {

    static final Logger LOGGER = Logger.getLogger(FrameworkConfigFilesNodeFactory.class.getName());


    public FrameworkConfigFilesNodeFactory() {
    }

    @Override
    public NodeList<?> createNodes(Project p) {
        final PhpProject project = p.getLookup().lookup(PhpProject.class);
        return new ConfigFilesNodeList(project);
    }

    //~ Inner classes

    private static final class ConfigFilesNodeList implements NodeList<Node>, PropertyChangeListener, ChangeListener {

        private final PhpProject project;
        private final List<ImportantFilesImplementation> configFiles = new CopyOnWriteArrayList<>();
        private final ConfigFilesChildren configFilesChildren;
        final ChangeSupport changeSupport = new ChangeSupport(this);

        // @GuardedBy("thread")
        private Node configFilesNode;


        ConfigFilesNodeList(PhpProject project) {
            assert project != null;
            this.project = project;
            configFilesChildren = new ConfigFilesChildren(project, configFiles);
        }

        @Override
        public List<Node> keys() {
            if (!configFilesChildren.hasConfigFiles()) {
                return Collections.<Node>emptyList();
            }
            if (configFilesNode == null) {
                configFilesNode = new ConfigFilesNode(configFilesChildren);
            }
            return Collections.<Node>singletonList(configFilesNode);
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
        public Node node(Node key) {
            return key;
        }

        @Override
        public void addNotify() {
            ProjectPropertiesSupport.addWeakProjectPropertyChangeListener(project, this);
            listenOnFrameworks();
        }

        @Override
        public void removeNotify() {
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            configFilesChildren.refreshConfigFiles();
            fireChange();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (PhpProject.PROP_FRAMEWORKS.equals(evt.getPropertyName())) {
                listenOnFrameworks();
                fireChange();
            }
        }

        private void fireChange() {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    changeSupport.fireChange();
                }
            });
        }

        // hmm, one listener can be added more times but should not be problem since frameworks in project do not change often...
        private void listenOnFrameworks() {
            List<PhpFrameworkProvider> frameworks = project.getFrameworks();
            List<ImportantFilesImplementation> newConfigFiles = new ArrayList<>(frameworks.size());
            PhpModule phpModule = project.getPhpModule();
            for (PhpFrameworkProvider framework : frameworks) {
                ImportantFilesImplementation configurationFiles = framework.getConfigurationFiles2(phpModule);
                if (configurationFiles != null) {
                    newConfigFiles.add(configurationFiles);
                    configurationFiles.addChangeListener(WeakListeners.change(this, configurationFiles));
                } else {
                    File[] files = framework.getConfigurationFiles(phpModule);
                    if (files.length > 0) {
                        LOGGER.log(Level.INFO, "PHP framework {0} uses deprecated method, switch to PhpFrameworkProvider.getConfigurationFiles2()", framework.getIdentifier());
                        ImportantFilesImplementation dummyConfigFiles = new ImportantFilesImplementationImpl(project, framework.getIdentifier(), files);
                        newConfigFiles.add(dummyConfigFiles);
                    }
                }
            }
            configFiles.clear();
            configFiles.addAll(newConfigFiles);
        }

    }

    private static final class ConfigFilesNode extends AbstractNode {

        @StaticResource
        private static final String BADGE = "org/netbeans/modules/php/project/ui/resources/config-badge.gif"; // NOI18N

        private final Node iconDelegate;


        ConfigFilesNode(Children children) {
            super(children);
            iconDelegate = DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
        }

        @NbBundle.Messages("ConfigFilesNode.name=Configuration Files")
        @Override
        public String getDisplayName() {
            return Bundle.ConfigFilesNode_name();
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

    private static final class ConfigFilesChildren extends Children.Keys<ImportantFilesImplementation.FileInfo> {

        private static final Logger LOGGER = Logger.getLogger(ConfigFilesChildren.class.getName());

        private final PhpProject project;
        private final List<ImportantFilesImplementation> configFiles;


        ConfigFilesChildren(PhpProject project, List<ImportantFilesImplementation> configFiles) {
            super(true);
            assert project != null;
            assert configFiles != null;
            this.project = project;
            this.configFiles = configFiles;
        }

        public boolean hasConfigFiles() {
            return !getConfigFiles().isEmpty();
        }

        private void refreshConfigFiles() {
            setKeys();
        }

        @Override
        protected Node[] createNodes(ImportantFilesImplementation.FileInfo key) {
            assert key != null;
            try {
                return new Node[] {new ImportantFileNode(project, key)};
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
            setKeys(getConfigFiles());
        }

        private List<ImportantFilesImplementation.FileInfo> getConfigFiles() {
            Set<ImportantFilesImplementation.FileInfo> importantFiles = new LinkedHashSet<>();
            for (ImportantFilesImplementation provider : configFiles) {
                importantFiles.addAll(provider.getFiles());
            }
            return new ArrayList<>(importantFiles);
        }

    }

    @org.netbeans.api.annotations.common.SuppressWarnings(value = "EQ_DOESNT_OVERRIDE_EQUALS", justification = "No need to override these methods")
    private static final class ImportantFileNode extends FilterNode {

        private final PhpProject project;
        private final ImportantFilesImplementation.FileInfo fileInfo;


        ImportantFileNode(PhpProject project, ImportantFilesImplementation.FileInfo fileInfo) throws DataObjectNotFoundException {
            super(DataObject.find(fileInfo.getFile()).getNodeDelegate());
            assert project != null;
            this.project = project;
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
            FileObject file = fileInfo.getFile();
            String filepath = null;
            FileObject sourceDir = ProjectPropertiesSupport.getSourcesDirectory(project);
            if (sourceDir != null) {
                filepath = FileUtil.getRelativePath(sourceDir, file);
            }
            if (filepath == null) {
                filepath = FileUtil.getRelativePath(project.getProjectDirectory(), file);
            }
            if (filepath == null) {
                // should not happen usually
                filepath = FileUtil.getFileDisplayName(file);
            }
            return filepath;
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

    private static final class ImportantFilesImplementationImpl implements ImportantFilesImplementation {

        private final PhpVisibilityQuery phpVisibilityQuery;
        private final String frameworkIdent;
        private final List<File> files = new CopyOnWriteArrayList<>();


        public ImportantFilesImplementationImpl(PhpProject project, String frameworkIdent, File[] files) {
            assert project != null;
            assert frameworkIdent != null;
            assert files != null;
            assert files.length > 0;
            phpVisibilityQuery = PhpVisibilityQuery.forProject(project);
            this.frameworkIdent = frameworkIdent;
            this.files.addAll(Arrays.asList(files));
        }

        @Override
        public Collection<FileInfo> getFiles() {
            List<FileInfo> result = new ArrayList<>(files.size());
            for (File file : files) {
                FileObject fo = FileUtil.toFileObject(file);
                if (fo != null) {
                    if (fo.isFolder()) {
                        Exception ex = new IllegalStateException("No folders allowed among configuration files ["
                                + fo.getNameExt() + " for " + frameworkIdent + "]");
                        LOGGER.log(Level.INFO, ex.getMessage(), ex);
                        continue;
                    }
                    if (phpVisibilityQuery.isVisible(fo)) {
                        result.add(new FileInfo(fo));
                    } else {
                        LOGGER.log(Level.INFO, "File {0} ignored (not visible)", fo.getPath());
                    }
                }
            }
            return result;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            // noop
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            // noop
        }

    }

}
