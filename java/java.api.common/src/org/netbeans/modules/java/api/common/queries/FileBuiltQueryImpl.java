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
package org.netbeans.modules.java.api.common.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex.Action;


/**
 * Default implementation of {@link FileBuiltQueryImplementation}.
 * @author Jesse Glick, Tomas Zezula
 */
final class FileBuiltQueryImpl implements FileBuiltQueryImplementation, PropertyChangeListener {

    private FileBuiltQueryImplementation delegate;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;
    private File buildGeneratedDir = null;
    private final FileChangeListener buildGeneratedDirListener = new FileChangeAdapter() {
        public @Override void fileFolderCreated(FileEvent fe) {
            invalidate();
        }
        public @Override void fileDeleted(FileEvent fe) {
            invalidate();
        }
        public @Override void fileRenamed(FileRenameEvent fe) {
            invalidate();
        }
    };

    FileBuiltQueryImpl(AntProjectHelper helper, PropertyEvaluator evaluator, SourceRoots sourceRoots,
            SourceRoots testRoots) {
        assert helper != null;
        assert evaluator != null;
        assert sourceRoots != null;
        assert testRoots != null;

        this.helper = helper;
        this.evaluator = evaluator;
        this.sourceRoots = sourceRoots;
        this.testRoots = testRoots;
        this.sourceRoots.addPropertyChangeListener(this);
        this.testRoots.addPropertyChangeListener(this);
    }

    public FileBuiltQuery.Status getStatus(final FileObject file) {
        return ProjectManager.mutex().readAccess(new Action<FileBuiltQuery.Status>() {
            public FileBuiltQuery.Status run() {
                return getStatusImpl(file);
            }
        });
    }

    private synchronized FileBuiltQuery.Status getStatusImpl(FileObject file) {
        if (delegate == null) {
            delegate = createDelegate();
        }
        return delegate.getStatus(file);
    }


    private FileBuiltQueryImplementation createDelegate() {
        List<String> from = new ArrayList<String>();
        List<String> to = new ArrayList<String>();
        for (String r : sourceRoots.getRootProperties()) {
            from.add("${" + r + "}/*.java"); // NOI18N
            to.add("${build.classes.dir}/*.class"); // NOI18N
        }
        for (String r : testRoots.getRootProperties()) {
            from.add("${" + r + "}/*.java"); // NOI18N
            to.add("${build.test.classes.dir}/*.class"); // NOI18N
        }
        String buildGeneratedDirS = evaluator.getProperty("build.generated.sources.dir"); // NOI18N
        if (buildGeneratedDirS != null) { // #105645
            File _buildGeneratedDir = helper.resolveFile(buildGeneratedDirS);
            if (!_buildGeneratedDir.equals(buildGeneratedDir)) {
                if (buildGeneratedDir != null) {
                    FileUtil.removeFileChangeListener(buildGeneratedDirListener, buildGeneratedDir);
                }
                buildGeneratedDir = _buildGeneratedDir;
                FileUtil.addFileChangeListener(buildGeneratedDirListener, buildGeneratedDir);
            }
            if (buildGeneratedDir.isDirectory()) {
                for (File root : buildGeneratedDir.listFiles()) {
                    if (!root.isDirectory()) {
                        continue;
                    }
                    from.add(root + "/*.java"); // NOI18N
                    to.add("${build.classes.dir}/*.class"); // NOI18N
                }
            }
        }
        return helper.createGlobFileBuiltQuery(evaluator,
                from.toArray(new String[0]),
                to.toArray(new String[0]));
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (SourceRoots.PROP_ROOT_PROPERTIES.equals(evt.getPropertyName())) {
            invalidate();
        }
    }

    private synchronized void invalidate() {
        delegate = null;
        // XXX: what to do with already returned Statuses
    }

}
