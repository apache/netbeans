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
package org.netbeans.modules.php.phing.exec;

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
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.phing.options.PhingOptions;
import org.netbeans.modules.php.phing.options.PhingOptionsValidator;
import org.netbeans.modules.php.phing.ui.options.PhingOptionsPanelController;
import org.netbeans.modules.php.phing.util.PhingUtils;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

public class PhingExecutable {

    static final Logger LOGGER = Logger.getLogger(PhingExecutable.class.getName());

    public static final String PHING_NAME = "phing"; // NOI18N
    @org.netbeans.api.annotations.common.SuppressWarnings(value = "MS_MUTABLE_ARRAY", justification = "No need to worry, noone will change it") // NOI18N
    public static final String[] PHING_NAMES;

    private static final String LIST_PARAM = "-list"; // NOI18N
    private static final String QUIET_PARAM = "-quiet"; // NOI18N

    private final Project project;
    private final String phingPath;

    @NullAllowed
    private final File workDir;


    static {
        PHING_NAMES = new String[] {
            Utilities.isWindows() ? PHING_NAME + ".bat" : PHING_NAME, // NOI18N
            PHING_NAME + ".php", // NOI18N
            PHING_NAME + "-latest.phar", // NOI18N
        };
    }


    PhingExecutable(String phingPath, Project project, @NullAllowed File workDir) {
        assert phingPath != null;
        assert project != null;
        this.phingPath = phingPath;
        this.project = project;
        this.workDir = workDir;
    }

    @CheckForNull
    public static PhingExecutable getDefault(Project project, boolean showOptions) {
        return createExecutable(project, null, showOptions);
    }

    @CheckForNull
    public static PhingExecutable getDefault(Project project, File workDir, boolean showOptions) {
        assert workDir != null;
        assert workDir.exists() : workDir;
        return createExecutable(project, workDir, showOptions);
    }

    @CheckForNull
    private static PhingExecutable createExecutable(Project project, @NullAllowed File workDir, boolean showOptions) {
        assert project != null;
        ValidationResult result = new PhingOptionsValidator()
                .validatePhing()
                .getResult();
        if (validateResult(result) != null) {
            if (showOptions) {
                UiUtils.showOptions(PhingOptionsPanelController.OPTIONS_SUBPATH);
            }
            return null;
        }
        return new PhingExecutable(PhingOptions.getInstance().getPhing(), project, workDir);
    }

    private String getCommand() {
        return phingPath;
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "PhingExecutable.run=Phing ({0})",
    })
    public Future<Integer> run(String... args) {
        assert !EventQueue.isDispatchThread();
        assert project != null;
        String projectName = PhingUtils.getProjectDisplayName(project);
        Future<Integer> task = getExecutable(Bundle.PhingExecutable_run(projectName))
                .additionalParameters(getRunParams(args))
                .run(getDescriptor());
        assert task != null : phingPath;
        return task;
    }

    public Future<List<String>> listTargets() {
        final PhingTargetsLineProcessor phingTargetsLineProcessor = new PhingTargetsLineProcessor();
        Future<Integer> task = getExecutable("list phing targets") // NOI18N
                .noInfo(true)
                .additionalParameters(getListTargetsParams())
                .redirectErrorStream(false)
                .run(getSilentDescriptor(), new ExecutionDescriptor.InputProcessorFactory2() {
                    @Override
                    public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                        return InputProcessors.bridge(phingTargetsLineProcessor);
                    }
                });
        assert task != null : phingPath;
        return new TargetList(this, task, phingTargetsLineProcessor);
    }

    private PhpExecutable getExecutable(String title) {
        assert title != null;
        return new PhpExecutable(getCommand())
                .workDir(getWorkDir())
                .displayName(title)
                .optionsSubcategory(PhingOptionsPanelController.OPTIONS_SUBPATH);
    }

    private List<String> getRunParams(String[] args) {
        List<String> params = new ArrayList<>(Arrays.asList(args));
        return params;
    }

    private ExecutionDescriptor getDescriptor() {
        assert project != null;
        return PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .showSuspended(true)
                .optionsPath(PhingOptionsPanelController.OPTIONS_PATH)
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

    static List<String> getListTargetsParams() {
        return Arrays.asList(QUIET_PARAM, LIST_PARAM);
    }

    //~ Inner classes

    private static final class PhingTargetsLineProcessor implements LineProcessor {

        private static final String MAIN_TARGETS = "Main targets:"; // NOI18N
        private static final String SUBTARGETS = "Subtargets:"; // NOI18N

        // @GuardedBy("targets")
        final List<String> targets = new ArrayList<>();

        private boolean collecting = false;


        @Override
        public void processLine(String line) {
            String trimmed = line.trim();
            if (collecting) {
                if (!SUBTARGETS.equals(trimmed)) {
                    if (StringUtilities.hasText(trimmed.replace('-', ' '))) { // NOI18N
                        synchronized (targets) {
                            targets.add(StringUtilities.explode(trimmed, " ").get(0)); // NOI18N
                        }
                    }
                }
            } else {
                collecting = MAIN_TARGETS.equals(trimmed)
                        || SUBTARGETS.equals(trimmed);
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

        public List<String> getTargets() {
            synchronized (targets) {
                return new ArrayList<>(targets);
            }
        }

    }

    private static final class TargetList implements Future<List<String>> {

        private static final RequestProcessor RP = new RequestProcessor(TargetList.class);

        final PhingExecutable phing;
        private final Future<Integer> task;
        private final PhingTargetsLineProcessor processor;

        private volatile Integer result;
        // @GuardedBy("this")
        private List<String> phingTargets = null;


        TargetList(PhingExecutable phing, Future<Integer> task, PhingTargetsLineProcessor processor) {
            assert phing != null;
            assert task != null;
            assert processor != null;
            this.phing = phing;
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
            return getPhingTargets();
        }

        @Override
        public List<String> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            try {
                result = task.get(timeout, unit);
            } catch (CancellationException ex) {
                // cancelled by user
                LOGGER.log(Level.FINE, null, ex);
            }
            return getPhingTargets();
        }

        @NbBundle.Messages("TargetList.error=Cannot get Phing targets.")
        private synchronized List<String> getPhingTargets() {
            if (phingTargets != null) {
                return Collections.unmodifiableList(phingTargets);
            }
            if (result == null
                    || result == 1) {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        phing.run(getListTargetsParams().toArray(new String[0]));
                    }
                });
                StatusDisplayer.getDefault().setStatusText(Bundle.TargetList_error());
                phingTargets = Collections.emptyList();
                return Collections.unmodifiableList(phingTargets);
            }
            List<String> targets = new ArrayList<>(processor.getTargets());
            Collections.sort(targets);
            phingTargets = new CopyOnWriteArrayList<>(targets);
            return Collections.unmodifiableList(phingTargets);
        }

    }

}
