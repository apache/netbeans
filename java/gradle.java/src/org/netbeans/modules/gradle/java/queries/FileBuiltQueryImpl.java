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

package org.netbeans.modules.gradle.java.queries;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileBuiltQuery;
import static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet.SourceType.JAVA;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

/**
 *
 * @author Laszlo Kishalmi
 */
public class FileBuiltQueryImpl extends ProjectOpenedHook implements FileBuiltQueryImplementation {

    final Project project;
    final Map<FileObject, FileBuiltQuery.Status> cache = new WeakHashMap<>();
    private static final Set<String> SUPPORTED_EXTS = new HashSet<>();
    private final PropertyChangeListener pcl;
    private final AtomicBoolean enabled = new AtomicBoolean();

    static {
        SUPPORTED_EXTS.addAll(Arrays.asList("java", "groovy", "scala")); //NOI18N
    }

    private static final FileBuiltQuery.Status NONE = new FileBuiltQuery.Status() {
        @Override
        public boolean isBuilt() {
            return true;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }
    };

    public FileBuiltQueryImpl(Project project) {
        this.project = project;
        this.pcl = (PropertyChangeEvent evt) -> {
            if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                cache.clear();
            }
        };
    }

    @Override
    public FileBuiltQuery.Status getStatus(FileObject file) {
        if (!enabled.get()) return null;
        FileBuiltQuery.Status ret;
        synchronized(cache) {
            ret = cache.get(file);
        }
        if (ret == null) {
            ret = createStatus(file);
            synchronized(cache) {
                cache.put(file, ret);
            }
        }
        return ret != NONE ? ret : null;
    }

    private FileBuiltQuery.Status createStatus(FileObject file) {
        FileBuiltQuery.Status ret = NONE;
        GradleJavaProject gjp = GradleJavaProject.get(project);
        if (SUPPORTED_EXTS.contains(file.getExt()) && (gjp != null)) {
            File f = FileUtil.toFile(file);
            GradleJavaSourceSet sourceSet = gjp.containingSourceSet(f);
            if (sourceSet != null) {
                String relFile = sourceSet.relativePath(f);
                if (relFile != null) {
                    String relClass = relFile.substring(0, relFile.lastIndexOf('.')) + ".class"; //NOI18N
                    String moduleRoot = null;
                    File moduleInfo = sourceSet.findResource("module-info.java", false, JAVA); //NOI18N
                    if (moduleInfo != null && sourceSet.getCompilerArgs(JAVA).contains("--module-source-path")) {
                        moduleRoot = SourceUtils.parseModuleName(FileUtil.toFileObject(moduleInfo));
                    }
                    try {
                        ret = new StatusImpl(file, sourceSet.getOutputClassDirs(), relClass, moduleRoot);
                    } catch (DataObjectNotFoundException ex) {}
                }
            }

        }
        return ret;
    }

    @Override
    protected void projectOpened() {
        enabled.set(true);
        NbGradleProject.addPropertyChangeListener(project, pcl);
    }

    @Override
    protected void projectClosed() {
        enabled.set(false);
        NbGradleProject.removePropertyChangeListener(project, pcl);
        cache.clear();
    }

    private static class StatusImpl implements FileBuiltQuery.Status {

        private final ChangeSupport cs = new ChangeSupport(this);
        private final DataObject source;
        private final Set<File> roots;
        private final String relClass;
        private final String moduleName;
        private final PropertyChangeListener pcl  = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (DataObject.PROP_MODIFIED.equals(evt.getPropertyName())) {
                    checkBuilt();
                }
            }
        };

        private final FileChangeListener listener = new FileChangeAdapter() {

            @Override
            public void fileDataCreated(FileEvent fe) {
                checkBuilt();
            }

            @Override
            public void fileChanged(FileEvent fe) {
                checkBuilt();
            }

            @Override
            public void fileDeleted(FileEvent fe) {
                checkBuilt();
            }

            @Override
            public void fileAttributeChanged(FileAttributeEvent fe) {
                checkBuilt();
            }
        };
        boolean status;

        public StatusImpl(FileObject source, Set<File> roots, String relClass, String moduleName) throws DataObjectNotFoundException {
            this.roots = roots;
            this.relClass = relClass;
            this.source = DataObject.find(source);
            this.moduleName = moduleName;
            this.source.addPropertyChangeListener(WeakListeners.propertyChange(pcl, this.source));
            for (File root : roots) {
                File moduleRoot = moduleName == null ? root : new File(root, moduleName);
                FileUtil.addFileChangeListener(listener, FileUtil.normalizeFile(new File(moduleRoot, relClass)));
            }
            checkBuilt();
        }

        @Override
        public boolean isBuilt() {
            return status;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

        private void checkBuilt() {
            FileObject fo = source.getPrimaryFile();
            boolean built = false;
            if (fo != null) {
                for (File root : roots) {
                    File moduleRoot = moduleName == null ? root : new File(root, moduleName);
                    File target = FileUtil.normalizeFile(new File(moduleRoot, relClass));
                    if (target.exists()) {
                        long sourceTime = fo.lastModified().getTime();
                        long targetTime = target.lastModified();
                        built = !source.isModified() && targetTime > sourceTime;
                        if (built) break;
                    }
                }
            }
            if (built != status) {
                status = built;
                cs.fireChange();
            }
        }
    }
}
