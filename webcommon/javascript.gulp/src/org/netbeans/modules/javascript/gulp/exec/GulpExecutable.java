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
package org.netbeans.modules.javascript.gulp.exec;

import java.awt.EventQueue;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputProcessors;
import org.netbeans.api.extexecution.base.input.LineProcessor;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.gulp.options.GulpOptions;
import org.netbeans.modules.javascript.gulp.options.GulpOptionsValidator;
import org.netbeans.modules.javascript.gulp.ui.options.GulpOptionsPanelController;
import org.netbeans.modules.javascript.gulp.util.GulpUtils;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.ui.api.ExternalExecutable;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

public class GulpExecutable {

    static final Logger LOGGER = Logger.getLogger(GulpExecutable.class.getName());

    public static final String GULP_NAME;

    private static final String NO_COLOR_PARAM = "--no-color"; // NOI18N
    private static final String COLOR_PARAM = "--color"; // NOI18N
    private static final String TASKS_PARAM = "--tasks-simple"; // NOI18N
    private static final String SILENT_PARAM = "--silent"; // NOI18N

    protected final Project project;
    protected final String gulpPath;

    @NullAllowed
    private final File workDir;


    static {
        if (Utilities.isWindows()) {
            GULP_NAME = "gulp.cmd"; // NOI18N
        } else {
            GULP_NAME = "gulp"; // NOI18N
        }
    }


    GulpExecutable(String gulpPath, Project project, @NullAllowed File workDir) {
        assert gulpPath != null;
        assert project != null;
        this.gulpPath = gulpPath;
        this.project = project;
        this.workDir = workDir;
    }

    @CheckForNull
    public static GulpExecutable getDefault(Project project, boolean showOptions) {
        return createExecutable(project, null, showOptions);
    }

    @CheckForNull
    public static GulpExecutable getDefault(Project project, File workDir, boolean showOptions) {
        assert workDir != null;
        assert workDir.exists() : workDir;
        return createExecutable(project, workDir, showOptions);
    }

    @CheckForNull
    private static GulpExecutable createExecutable(Project project, @NullAllowed File workDir, boolean showOptions) {
        assert project != null;
        ValidationResult result = new GulpOptionsValidator()
                .validateGulp()
                .getResult();
        if (validateResult(result) != null) {
            if (showOptions) {
                OptionsDisplayer.getDefault().open(GulpOptionsPanelController.OPTIONS_PATH);
            }
            return null;
        }
        String gulp = GulpOptions.getInstance().getGulp();
        if (Utilities.isMac()) {
            return new MacGulpExecutable(gulp, project, workDir);
        }
        return new GulpExecutable(gulp, project, workDir);
    }

    String getCommand() {
        return gulpPath;
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "GulpExecutable.run=Gulp ({0})",
    })
    public Future<Integer> run(String... args) {
        assert !EventQueue.isDispatchThread();
        assert project != null;
        String projectName = GulpUtils.getProjectDisplayName(project);
        Future<Integer> task = getExecutable(Bundle.GulpExecutable_run(projectName))
                .additionalParameters(getRunParams(args))
                .run(getDescriptor());
        assert task != null : gulpPath;
        return task;
    }

    public Future<List<String>> listTasks() {
        final GulpTasksLineProcessor gulpTasksLineProcessor = new GulpTasksLineProcessor();
        Future<Integer> task = getExecutable("list gulp tasks") // NOI18N
                .noInfo(true)
                .additionalParameters(getParams(getListTasksParams()))
                .redirectErrorStream(false)
                .run(getSilentDescriptor(), new ExecutionDescriptor.InputProcessorFactory2() {
                    @Override
                    public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                        return InputProcessors.bridge(gulpTasksLineProcessor);
                    }
                });
        assert task != null : gulpPath;
        return new TaskList(this, task, gulpTasksLineProcessor);
    }

    private ExternalExecutable getExecutable(String title) {
        assert title != null;
        return new ExternalExecutable(getCommand())
                .workDir(getWorkDir())
                .displayName(title)
                .optionsPath(GulpOptionsPanelController.OPTIONS_PATH)
                .noOutput(false);
    }

    private ExecutionDescriptor getDescriptor() {
        assert project != null;
        return ExternalExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .showSuspended(true)
                .optionsPath(GulpOptionsPanelController.OPTIONS_PATH)
                .outLineBased(true)
                .errLineBased(true)
                .postExecution(new Runnable() {
                    @Override
                    public void run() {
                        // #246886
                        FileUtil.refreshFor(getWorkDir());
                    }
                });
    }

    private static ExecutionDescriptor getSilentDescriptor() {
        return new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL)
                .inputVisible(false)
                .frontWindow(false)
                .showProgress(false)
                .charset(StandardCharsets.UTF_8)
                .outLineBased(true);
    }

    private File getWorkDir() {
        if (workDir != null
                && workDir.exists()) {
            return workDir;
        }
        File dir = FileUtil.toFile(project.getProjectDirectory());
        assert dir != null : project.getProjectDirectory();
        return dir;
    }

    private List<String> getRunParams(String... args) {
        List<String> params = new ArrayList<>(args.length + 1);
        params.addAll(Arrays.asList(args));
        params.add(COLOR_PARAM);
        return getParams(params);
    }

    List<String> getParams(List<String> params) {
        assert params != null;
        return params;
    }

    @CheckForNull
    private static String validateResult(ValidationResult result) {
        if (result.isFaultless()) {
            return null;
        }
        if (result.hasErrors()) {
            return result.getFirstErrorMessage();
        }
        return result.getFirstWarningMessage();
    }

    static List<String> getListTasksParams() {
        return Arrays.asList(NO_COLOR_PARAM, SILENT_PARAM, TASKS_PARAM);
    }

    //~ Inner classes

    private static final class MacGulpExecutable extends GulpExecutable {

        private static final String BASH_COMMAND = "/bin/bash -lc"; // NOI18N


        MacGulpExecutable(String gulpPath, Project project, File workDir) {
            super(gulpPath, project, workDir);
        }

        @Override
        String getCommand() {
            return BASH_COMMAND;
        }

        @Override
        List<String> getParams(List<String> params) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("\""); // NOI18N
            sb.append(gulpPath);
            sb.append("\" \""); // NOI18N
            sb.append(StringUtilities.implode(super.getParams(params), "\" \"")); // NOI18N
            sb.append("\""); // NOI18N
            return Collections.singletonList(sb.toString());
        }

    }

    private static final class GulpTasksLineProcessor implements LineProcessor {

        // @GuardedBy("tasks")
        final List<String> tasks = new ArrayList<>();


        @Override
        public void processLine(String line) {
            if (StringUtilities.hasText(line)) {
                synchronized (tasks) {
                    tasks.add(line);
                }
            }
        }

        @Override
        public void reset() {
            // noop
        }

        @Override
        public void close() {
            // noop
        }

        public List<String> getTasks() {
            synchronized (tasks) {
                return new ArrayList<>(tasks);
            }
        }

    }

    private static final class TaskList implements Future<List<String>> {

        private static final RequestProcessor RP = new RequestProcessor(TaskList.class);

        final GulpExecutable gulp;
        private final Future<Integer> task;
        private final GulpTasksLineProcessor processor;

        private volatile Integer result;
        // @GuardedBy("this")
        private List<String> gulpTasks = null;


        TaskList(GulpExecutable gulp, Future<Integer> task, GulpTasksLineProcessor processor) {
            assert gulp != null;
            assert task != null;
            assert processor != null;
            this.gulp = gulp;
            this.task = task;
            this.processor = processor;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return task.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }

        @Override
        public boolean isDone() {
            return task.isDone();
        }

        @Override
        public List<String> get() throws InterruptedException, ExecutionException {
            try {
                result = task.get();
            } catch (CancellationException ex) {
                // cancelled by user
                LOGGER.log(Level.FINE, null, ex);
            }
            return getGulpTasks();
        }

        @Override
        public List<String> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            try {
                result = task.get(timeout, unit);
            } catch (CancellationException ex) {
                // cancelled by user
                LOGGER.log(Level.FINE, null, ex);
            }
            return getGulpTasks();
        }

        @NbBundle.Messages("TaskList.error=Cannot get Gulp tasks.")
        private synchronized List<String> getGulpTasks() {
            if (gulpTasks != null) {
                return Collections.unmodifiableList(gulpTasks);
            }
            if (result == null
                    || result == 1) {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        gulp.run(getListTasksParams().toArray(new String[0]));
                    }
                });
                StatusDisplayer.getDefault().setStatusText(Bundle.TaskList_error());
                gulpTasks = Collections.emptyList();
                return Collections.unmodifiableList(gulpTasks);
            }
            List<String> tasks = new ArrayList<>(processor.getTasks());
            Collections.sort(tasks);
            gulpTasks = new CopyOnWriteArrayList<>(tasks);
            return Collections.unmodifiableList(gulpTasks);
        }

    }

}
