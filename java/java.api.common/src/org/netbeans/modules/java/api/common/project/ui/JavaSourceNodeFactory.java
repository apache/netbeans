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

package org.netbeans.modules.java.api.common.project.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

/**
 * Java sources node factory.
 * @since org.netbeans.modules.java.api.common/1 1.5
 */
public final class JavaSourceNodeFactory implements NodeFactory {

    private static final Logger LOG = Logger.getLogger(JavaSourceNodeFactory.class.getName());

    public JavaSourceNodeFactory() {
    }
    
    @Override
    public NodeList createNodes(Project p) {
        Project project = p.getLookup().lookup(Project.class);
        assert project != null;
        return new SourcesNodeList(project);
    }
    
    private static class SourcesNodeList implements NodeList<SourceGroupKey>, ChangeListener {
        
        private final Project project;
        private final File genSrcDir;
        private final FileChangeListener genSrcDirListener;
        private final FileChangeListener genContentListener;
        private final List<File> listensOn = Collections.synchronizedList(new LinkedList<File>());
        private final Runnable changeTask;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        public SourcesNodeList(Project proj) {
            project = proj;
            changeTask = new Runnable() {
                @Override
                public void run() {
                    stateChanged(null);
                }
            };
            genSrcDirListener = new FileChangeAdapter() {
                public @Override void fileFolderCreated(FileEvent fe) {
                    fe.runWhenDeliveryOver(changeTask);
                }
                public @Override void fileDeleted(FileEvent fe) {
                    fe.runWhenDeliveryOver(changeTask);
                }
                public @Override void fileRenamed(FileRenameEvent fe) {
                    fe.runWhenDeliveryOver(changeTask);
                }
            };
            genContentListener = new FileChangeAdapter() {
                @Override
                public void fileFolderCreated(FileEvent fe) {
                    fe.runWhenDeliveryOver(changeTask);
                }
                @Override
                public void fileDataCreated(FileEvent fe) {
                    fe.runWhenDeliveryOver(changeTask);
                }
                @Override
                public void fileDeleted(FileEvent fe) {
                    fe.runWhenDeliveryOver(changeTask);
                }
            };
            File d = FileUtil.toFile(proj.getProjectDirectory());
            // XXX hardcodes the value of ${build.generated.sources.dir}, since we have no access to evaluator
            genSrcDir = d != null ? new File(d, "build/generated-sources") : null;
        }

        @Override
        public List<SourceGroupKey> keys() {
            if (this.project.getProjectDirectory() == null || !this.project.getProjectDirectory().isValid()) {
                return Collections.<SourceGroupKey>emptyList();
            }
            List<SourceGroupKey> result =  new ArrayList<SourceGroupKey>();
            final SourceGroup[] groups = getSources().getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE,
                        "Java source groups: {0}",  //NOI18N
                        Arrays.toString(groups));
            }
            for (SourceGroup group : groups) {
                result.add(new SourceGroupKey(group, true));
            }
            File[] removeFrom;
            synchronized (listensOn) {
                removeFrom = listensOn.toArray(new File[0]);
                listensOn.clear();
            }
            for (File file : removeFrom) {
                FileUtil.removeFileChangeListener(genContentListener, file);
            }
            FileObject genSrc = FileUtil.toFileObject(genSrcDir);
            if (genSrc != null) {
                final VisibilityQuery vq = VisibilityQuery.getDefault();
                for (final FileObject child : genSrc.getChildren()) {
                    if (!child.isFolder()) {
                        continue;
                    }                    
                    if (!vq.isVisible(child)) {
                        continue;
                    }                    
                    final File childFile = FileUtil.toFile(child);
                    if (childFile == null) {
                        continue;
                    }
                    FileUtil.addFileChangeListener(genContentListener,childFile);
                    listensOn.add(childFile);
                    if (child.getChildren().length > 0) {
                        result.add(new SourceGroupKey(new GeneratedSourceGroup(child), false));
                    }
                }
            }
            return result;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            synchronized (changeSupport) {
                final boolean shouldAdd = !changeSupport.hasListeners();
                changeSupport.addChangeListener(l);
                if (shouldAdd) {
                    FileUtil.addFileChangeListener(genSrcDirListener, genSrcDir);
                }
            }
        }
        
        @Override
        public void removeChangeListener(ChangeListener l) {            
            synchronized (changeSupport) {
                final boolean hadListeners = changeSupport.hasListeners();
                changeSupport.removeChangeListener(l);            
                if (hadListeners ^ changeSupport.hasListeners())  {
                    FileUtil.removeFileChangeListener(genSrcDirListener, genSrcDir);
                }
            }
        }
        
        @Override
        public Node node(SourceGroupKey key) {
            return new PackageViewFilterNode(key.group, project, !key.trueSource);
        }
        
        @Override
        public void addNotify() {
            getSources().addChangeListener(this);
        }
        
        @Override
        public void removeNotify() {
            getSources().removeChangeListener(this);
        }
        
        @Override
        public void stateChanged(ChangeEvent e) {
            // setKeys(getKeys());
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    changeSupport.fireChange();
                }
            });
        }
        
        private Sources getSources() {
            return ProjectUtils.getSources(project);
        }

    }
    
    private static class SourceGroupKey {
        
        public final SourceGroup group;
        public final FileObject fileObject;
        public final boolean trueSource;
        
        SourceGroupKey(SourceGroup group, boolean trueSource) {
            this.group = group;
            this.fileObject = group.getRootFolder();
            this.trueSource = trueSource;
        }
        
        @Override
        public int hashCode() {
            int hash = 5;
            String disp = this.group.getDisplayName();
            hash = 79 * hash + (fileObject != null ? fileObject.hashCode() : 0);
            hash = 79 * hash + (disp != null ? disp.hashCode() : 0);
            return hash;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SourceGroupKey)) {
                return false;
            } else {
                SourceGroupKey otherKey = (SourceGroupKey) obj;
                
                if (fileObject != otherKey.fileObject && (fileObject == null || !fileObject.equals(otherKey.fileObject))) {
                    return false;
                }
                String thisDisplayName = this.group.getDisplayName();
                String otherDisplayName = otherKey.group.getDisplayName();
                boolean oneNull = thisDisplayName == null;
                boolean twoNull = otherDisplayName == null;
                if (oneNull != twoNull || !thisDisplayName.equals(otherDisplayName)) {
                    return false;
                }
                return true;
            }
        }
        
    }
    
    private static class GeneratedSourceGroup implements SourceGroup {

        private final FileObject child;

        GeneratedSourceGroup(FileObject child) {
            this.child = child;
        }

        @Override
        public FileObject getRootFolder() {
            return child;
        }

        @Override
        public String getName() {
            return child.getNameExt();
        }

        @Override
        public String getDisplayName() {
            try {
                // Modules can provide dedicated localizable labels for well-known root names.
                return NbBundle.getBundle("org.netbeans.modules.java.api.common.project.ui.gensrc-" + getName()).getString("label");
            } catch (MissingResourceException x) {
                // Fallback, including for user-defined root names.
                return NbBundle.getMessage(JavaSourceNodeFactory.class, "JavaSourceNodeFactory.gensrc", getName());
            }
        }

        @Override
        public Icon getIcon(boolean opened) {
            return null;
        }

        @Override
        public boolean contains(FileObject file) {
            return true;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {}

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {}

    }
}
