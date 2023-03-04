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
package org.netbeans.modules.javascript.grunt;

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
import org.netbeans.modules.javascript.grunt.exec.GruntExecutable;
import org.netbeans.modules.javascript.grunt.file.GruntTasks;
import org.netbeans.modules.javascript.grunt.util.GruntUtils;
import org.netbeans.modules.web.clientproject.api.build.BuildTools;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class GruntBuildToolSupport implements BuildTools.BuildToolSupport {

    protected final Project project;
    @NullAllowed
    protected final FileObject gruntfile;
    protected final GruntTasks gruntTasks;


    public GruntBuildToolSupport(Project project, @NullAllowed FileObject gruntfile) {
        assert project != null;
        this.project = project;
        this.gruntfile = gruntfile;
        gruntTasks = GruntBuildTool.forProject(project).getGruntTasks(gruntfile);
    }

    @Override
    public String getIdentifier() {
        return GruntBuildTool.IDENTIFIER;
    }

    @Override
    public String getBuildToolExecName() {
        return GruntExecutable.GRUNT_NAME;
    }

    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public FileObject getWorkDir() {
        if (gruntfile == null) {
            return project.getProjectDirectory();
        }
        return gruntfile.getParent();
    }

    @Override
    public Future<List<String>> getTasks() {
        return new TasksFuture(gruntTasks);
    }

    @Override
    public void runTask(String... args) {
        assert !EventQueue.isDispatchThread();
        GruntExecutable grunt = getGruntExecutable();
        if (grunt != null) {
            GruntUtils.logUsageGruntBuild();
            grunt.run(args);
        }
    }

    public void addChangeListener(ChangeListener listener) {
        gruntTasks.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        gruntTasks.removeChangeListener(listener);
    }

    @CheckForNull
    private GruntExecutable getGruntExecutable() {
        if (gruntfile == null) {
            return GruntExecutable.getDefault(project, true);
        }
        return GruntExecutable.getDefault(project, FileUtil.toFile(gruntfile).getParentFile(), true);
    }

    //~ Inner classes

    private static final class TasksFuture implements Future<List<String>> {

        private final GruntTasks gruntTasks;


        public TasksFuture(GruntTasks gruntTasks) {
            assert gruntTasks != null;
            this.gruntTasks = gruntTasks;
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
            return gruntTasks.getTasks() != null;
        }

        @Override
        public List<String> get() throws InterruptedException, ExecutionException {
            try {
                return gruntTasks.loadTasks(null, null);
            } catch (TimeoutException ex) {
                assert false;
            }
            return null;
        }

        @Override
        public List<String> get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return gruntTasks.loadTasks(timeout, unit);
        }

    }

}
