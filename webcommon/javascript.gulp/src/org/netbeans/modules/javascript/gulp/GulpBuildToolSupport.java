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
package org.netbeans.modules.javascript.gulp;

import java.awt.EventQueue;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.gulp.exec.GulpExecutable;
import org.netbeans.modules.javascript.gulp.file.GulpTasks;
import org.netbeans.modules.javascript.gulp.util.GulpUtils;
import org.netbeans.modules.web.clientproject.api.build.BuildTools;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class GulpBuildToolSupport implements BuildTools.BuildToolSupport {

    protected final Project project;
    @NullAllowed
    protected final FileObject gulpfile;
    protected final GulpTasks gulpTasks;


    public GulpBuildToolSupport(Project project, @NullAllowed FileObject gulpfile) {
        assert project != null;
        this.project = project;
        this.gulpfile = gulpfile;
        gulpTasks = GulpBuildTool.forProject(project).getGulpTasks(gulpfile);
    }

    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public FileObject getWorkDir() {
        if (gulpfile == null) {
            return project.getProjectDirectory();
        }
        return gulpfile.getParent();
    }

    @Override
    public String getIdentifier() {
        return GulpBuildTool.IDENTIFIER;
    }

    @Override
    public String getBuildToolExecName() {
        return GulpExecutable.GULP_NAME;
    }

    @Override
    public Future<List<String>> getTasks() {
        return new TasksFuture(gulpTasks);
    }

    @Override
    public void runTask(String... args) {
        assert !EventQueue.isDispatchThread();
        GulpExecutable gulp = getGulpExecutable();
        if (gulp != null) {
            GulpUtils.logUsageGulpBuild();
            gulp.run(args);
        }
    }

    public void addChangeListener(ChangeListener listener) {
        gulpTasks.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        gulpTasks.removeChangeListener(listener);
    }

    @CheckForNull
    private GulpExecutable getGulpExecutable() {
        if (gulpfile == null) {
            return GulpExecutable.getDefault(project, true);
        }
        return GulpExecutable.getDefault(project, FileUtil.toFile(gulpfile).getParentFile(), true);
    }

    //~ Inner classes

    private static final class TasksFuture implements Future<List<String>> {

        private final GulpTasks gulpTasks;


        public TasksFuture(GulpTasks gulpTasks) {
            assert gulpTasks != null;
            this.gulpTasks = gulpTasks;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return gulpTasks.getTasks() != null;
        }

        @Override
        public List<String> get() throws InterruptedException, ExecutionException {
            try {
                return gulpTasks.loadTasks(null, null);
            } catch (TimeoutException ex) {
                assert false;
            }
            return null;
        }

        @Override
        public List<String> get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return gulpTasks.loadTasks(timeout, unit);
        }

    }


}
