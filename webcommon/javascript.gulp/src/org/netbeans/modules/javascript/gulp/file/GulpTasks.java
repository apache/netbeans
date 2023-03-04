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
package org.netbeans.modules.javascript.gulp.file;

import java.awt.EventQueue;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.gulp.exec.GulpExecutable;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;

public final class GulpTasks implements ChangeListener {

    private static final Logger LOGGER = Logger.getLogger(GulpTasks.class.getName());

    public static final String DEFAULT_TASK = "default"; // NOI18N

    private final Project project;
    private final Gulpfile gulpfile;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    final FileChangeListener nodeModulesListener = new NodeModulesListener();

    private volatile List<String> tasks;


    private GulpTasks(Project project, Gulpfile gulpfile) {
        assert project != null;
        assert gulpfile != null : project.getProjectDirectory();
        this.project = project;
        this.gulpfile = gulpfile;
    }

    public static GulpTasks create(Project project, Gulpfile gulpfile) {
        assert project != null;
        assert gulpfile != null;
        GulpTasks gulpTasks = new GulpTasks(project, gulpfile);
        // listeners
        gulpfile.addChangeListener(gulpTasks);
        FileUtil.addFileChangeListener(gulpTasks.nodeModulesListener, new File(gulpfile.getFile().getParent(), "node_modules")); // NOI18N
        return gulpTasks;
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @CheckForNull
    public List<String> getTasks() {
        List<String> tasksRef = tasks;
        return tasksRef == null ? null : Collections.unmodifiableList(tasksRef);
    }

    public List<String> loadTasks(@NullAllowed Long timeout, @NullAllowed TimeUnit unit) throws ExecutionException, TimeoutException {
        List<String> tasksRef = tasks;
        if (tasksRef != null) {
            return Collections.unmodifiableList(tasksRef);
        }
        assert !EventQueue.isDispatchThread();
        Future<List<String>> tasksJob = getTasksJob();
        if (tasksJob == null) {
            // some error
            return null;
        }
        try {
            List<String> allTasks;
            if (timeout != null) {
                assert unit != null;
                allTasks = tasksJob.get(timeout, unit);
            } else {
                allTasks = tasksJob.get();
            }
            tasks = new CopyOnWriteArrayList<>(allTasks);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return getTasks();
    }

    @CheckForNull
    private Future<List<String>> getTasksJob() {
        GulpExecutable gulp = GulpExecutable.getDefault(project, gulpfile.getFile().getParentFile(), false);
        if (gulp == null) {
            return null;
        }
        return gulp.listTasks();
    }

    public void reset() {
        tasks = null;
        changeSupport.fireChange();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        reset();
    }

    //~ Inner classes

    private final class NodeModulesListener extends FileChangeAdapter {

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            reset();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            reset();
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            reset();
        }

    }

}
