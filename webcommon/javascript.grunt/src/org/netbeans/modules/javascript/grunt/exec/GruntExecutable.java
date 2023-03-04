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
package org.netbeans.modules.javascript.grunt.exec;

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
import org.netbeans.modules.javascript.grunt.GruntBuildTool;
import org.netbeans.modules.javascript.grunt.options.GruntOptions;
import org.netbeans.modules.javascript.grunt.options.GruntOptionsValidator;
import org.netbeans.modules.javascript.grunt.ui.options.GruntOptionsPanelController;
import org.netbeans.modules.javascript.grunt.util.GruntUtils;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.ui.api.ExternalExecutable;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

public class GruntExecutable {

    static final Logger LOGGER = Logger.getLogger(GruntExecutable.class.getName());

    public static final String GRUNT_NAME;

    private static final String HELP_PARAM = "--help"; // NOI18N
    private static final String NO_COLOR_PARAM = "--no-color"; // NOI18N
    private static final String TASKS_PARAM = "--tasks"; // NOI18N

    protected final Project project;
    protected final String gruntPath;
    private final List<String> tasks;

    @NullAllowed
    private final File workDir;


    static {
        if (Utilities.isWindows()) {
            GRUNT_NAME = "grunt.cmd"; // NOI18N
        } else {
            GRUNT_NAME = "grunt"; // NOI18N
        }
    }


    GruntExecutable(String gruntPath, Project project, @NullAllowed File workDir) {
        assert gruntPath != null;
        assert project != null;
        this.gruntPath = gruntPath;
        this.tasks = getTasks(project, workDir);
        this.project = project;
        this.workDir = workDir;
    }

    @CheckForNull
    public static GruntExecutable getDefault(Project project, boolean showOptions) {
        return createExecutable(project, null, showOptions);
    }

    @CheckForNull
    public static GruntExecutable getDefault(Project project, File workDir, boolean showOptions) {
        assert workDir != null;
        assert workDir.exists() : workDir;
        return createExecutable(project, workDir, showOptions);
    }

    @CheckForNull
    private static GruntExecutable createExecutable(Project project, @NullAllowed File workDir, boolean showOptions) {
        assert project != null;
        ValidationResult result = new GruntOptionsValidator()
                .validateGrunt()
                .getResult();
        if (validateResult(result) != null) {
            if (showOptions) {
                OptionsDisplayer.getDefault().open(GruntOptionsPanelController.OPTIONS_PATH);
            }
            return null;
        }
        String grunt = GruntOptions.getInstance().getGrunt();
        if (Utilities.isMac()) {
            return new MacGruntExecutable(grunt, project, workDir);
        }
        return new GruntExecutable(grunt, project, workDir);
    }

    String getCommand() {
        return gruntPath;
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "GruntExecutable.run=Grunt ({0})",
    })
    public Future<Integer> run(String... args) {
        assert !EventQueue.isDispatchThread();
        assert project != null;
        String projectName = GruntUtils.getProjectDisplayName(project);
        Future<Integer> task = getExecutable(Bundle.GruntExecutable_run(projectName))
                .additionalParameters(getRunParams(args))
                .run(getDescriptor());
        assert task != null : gruntPath;
        return task;
    }

    public Future<List<String>> listTasks() {
        final GruntTasksLineProcessor gruntTasksLineProcessor = new GruntTasksLineProcessor();
        Future<Integer> task = getExecutable("list grunt tasks") // NOI18N
                .noInfo(true)
                .additionalParameters(getParams(getListTasksParams()))
                .redirectErrorStream(false)
                .run(getSilentDescriptor(), new ExecutionDescriptor.InputProcessorFactory2() {
                    @Override
                    public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                        return InputProcessors.bridge(gruntTasksLineProcessor);
                    }
                });
        assert task != null : gruntPath;
        return new TaskList(this, task, gruntTasksLineProcessor);
    }

    private ExternalExecutable getExecutable(String title) {
        assert title != null;
        return new ExternalExecutable(getCommand())
                .workDir(getWorkDir())
                .displayName(title)
                .optionsPath(GruntOptionsPanelController.OPTIONS_PATH)
                .noOutput(false);
    }

    private ExecutionDescriptor getDescriptor() {
        assert project != null;
        return ExternalExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .showSuspended(true)
                .optionsPath(GruntOptionsPanelController.OPTIONS_PATH)
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
        return getParams(Arrays.asList(args));
    }

    List<String> getParams(List<String> params) {
        assert params != null;
        List<String> allParams = new ArrayList<>(tasks.size() * 2 + params.size());
        for (String task : tasks) {
            allParams.add(TASKS_PARAM);
            allParams.add(task.trim());
        }
        allParams.addAll(params);
        return allParams;
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
        return Arrays.asList(NO_COLOR_PARAM, HELP_PARAM);
    }

    /**
     * Gets tasks but only for project Gruntfile.js.
     */
    private List<String> getTasks(Project project, @NullAllowed File workDir) {
        if (workDir == null
                || workDir.equals(FileUtil.toFile(project.getProjectDirectory()))) {
            return StringUtilities.explode(GruntBuildTool.forProject(project).getGruntPreferences().getTasks(), ","); // NOI18N
        }
        // not project grunt
        return Collections.emptyList();
    }

    //~ Inner classes

    private static final class MacGruntExecutable extends GruntExecutable {

        private static final String BASH_COMMAND = "/bin/bash -lc"; // NOI18N


        MacGruntExecutable(String gruntPath, Project project, File workDir) {
            super(gruntPath, project, workDir);
        }

        @Override
        String getCommand() {
            return BASH_COMMAND;
        }

        @Override
        List<String> getParams(List<String> params) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("\""); // NOI18N
            sb.append(gruntPath);
            sb.append("\" \""); // NOI18N
            sb.append(StringUtilities.implode(super.getParams(params), "\" \"")); // NOI18N
            sb.append("\""); // NOI18N
            return Collections.singletonList(sb.toString());
        }

    }

    static final class GruntTasksLineProcessor implements LineProcessor {

        private static final String AVAILABLE_TASKS = "Available tasks"; // NOI18N
        private static final String NO_TASKS = "(no tasks found)"; // NOI18N

        // @GuardedBy("tasks")
        final List<String> tasks = new ArrayList<>();

        private volatile int state = 0;
        private int spaceIndex = -1;


        @Override
        public void processLine(String line) {
            LOGGER.log(Level.FINE, line);
            switch (state) {
                case 0:
                    if (AVAILABLE_TASKS.equals(line)) {
                        state = 1;
                    }
                    break;
                case 1:
                    if (!StringUtilities.hasText(line)) {
                        state = 2;
                    } else if (NO_TASKS.equals(line.trim())) {
                        state = 2;
                    } else {
                        if (spaceIndex == -1) {
                            String task = StringUtilities.explode(line.trim(), "  ").get(0); // NOI18N
                            assert StringUtilities.hasText(task) : line;
                            spaceIndex = line.indexOf(task) + task.length();
                        }
                        String task = line.substring(0, spaceIndex).trim();
                        if (StringUtilities.hasText(task)) {
                            synchronized (tasks) {
                                tasks.add(task);
                            }
                        }
                    }
                    break;
                default:
                    // noop
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

        public boolean errorOccurred() {
            return state == 0;
        }

        public List<String> getTasks() {
            synchronized (tasks) {
                return new ArrayList<>(tasks);
            }
        }

    }

    private static final class TaskList implements Future<List<String>> {

        private static final RequestProcessor RP = new RequestProcessor(TaskList.class);

        final GruntExecutable grunt;
        private final Future<Integer> task;
        private final GruntTasksLineProcessor convertor;

        // @GuardedBy("this")
        private List<String> gruntTasks = null;


        TaskList(GruntExecutable grunt, Future<Integer> task, GruntTasksLineProcessor convertor) {
            assert grunt != null;
            assert task != null;
            assert convertor != null;
            this.grunt = grunt;
            this.task = task;
            this.convertor = convertor;
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
                task.get();
            } catch (CancellationException ex) {
                // cancelled by user
                LOGGER.log(Level.FINE, null, ex);
            }
            return getGruntTasks();
        }

        @Override
        public List<String> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            try {
                task.get(timeout, unit);
            } catch (CancellationException ex) {
                // cancelled by user
                LOGGER.log(Level.FINE, null, ex);
            }
            return getGruntTasks();
        }

        @NbBundle.Messages("TaskList.error=Cannot get Grunt tasks.")
        private synchronized List<String> getGruntTasks() {
            if (gruntTasks != null) {
                return Collections.unmodifiableList(gruntTasks);
            }
            if (convertor.errorOccurred()) {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        grunt.run(getListTasksParams().toArray(new String[0]));
                    }
                });
                StatusDisplayer.getDefault().setStatusText(Bundle.TaskList_error());
                gruntTasks = Collections.emptyList();
                return Collections.unmodifiableList(gruntTasks);
            }
            List<String> tasks = new ArrayList<>(convertor.getTasks());
            Collections.sort(tasks);
            gruntTasks = new CopyOnWriteArrayList<>(tasks);
            return Collections.unmodifiableList(gruntTasks);
        }

    }

}
