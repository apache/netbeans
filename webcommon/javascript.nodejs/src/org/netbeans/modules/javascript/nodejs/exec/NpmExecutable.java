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
package org.netbeans.modules.javascript.nodejs.exec;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.file.PackageJson;
import org.netbeans.modules.javascript.nodejs.options.NodeJsOptions;
import org.netbeans.modules.javascript.nodejs.options.NodeJsOptionsValidator;
import org.netbeans.modules.javascript.nodejs.ui.options.NodeJsOptionsPanelController;
import org.netbeans.modules.javascript.nodejs.util.FileUtils;
import org.netbeans.modules.javascript.nodejs.util.NodeJsUtils;
import org.netbeans.modules.javascript.nodejs.util.StringUtils;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.ui.api.ExternalExecutable;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;


public class NpmExecutable {

    private static final Logger LOGGER = Logger.getLogger(NpmExecutable.class.getName());

    public static final String NPM_NAME;

    public static final String SAVE_PARAM = "--save"; // NOI18N
    public static final String SAVE_DEV_PARAM = "--save-dev"; // NOI18N
    public static final String SAVE_OPTIONAL_PARAM = "--save-optional"; // NOI18N

    private static final String INSTALL_PARAM = "install"; // NOI18N
    private static final String UNINSTALL_PARAM = "uninstall"; // NOI18N
    private static final String RUN_SCRIPT_PARAM = "run-script"; // NOI18N

    protected final Project project;
    protected final String npmPath;


    static {
        if (Utilities.isWindows()) {
            NPM_NAME = "npm.cmd"; // NOI18N
        } else {
            NPM_NAME = "npm"; // NOI18N
        }
    }

    NpmExecutable(String npmPath, @NullAllowed Project project) {
        assert npmPath != null;
        this.npmPath = npmPath;
        this.project = project;
    }

    @CheckForNull
    public static NpmExecutable getDefault(@NullAllowed Project project, boolean showOptions) {
        ValidationResult result = new NodeJsOptionsValidator()
                .validateNpm()
                .getResult();
        if (validateResult(result) != null) {
            if (showOptions) {
                OptionsDisplayer.getDefault().open(NodeJsOptionsPanelController.OPTIONS_PATH);
            }
            return null;
        }
        return createExecutable(NodeJsOptions.getInstance().getNpm(), project);
    }

    private static NpmExecutable createExecutable(String npm, Project project) {
        if (Utilities.isMac()) {
            return new MacNpmExecutable(npm, project);
        }
        return new NpmExecutable(npm, project);
    }

    public String getExecutable() {
        return new ExternalExecutable(npmPath).getExecutable();
    }

    String getCommand() {
        return npmPath;
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "NpmExecutable.run.script=npm ({0})",
    })
    @CheckForNull
    public Future<Integer> runScript(String... args) {
        assert !EventQueue.isDispatchThread();
        assert args.length > 0;
        assert project != null;
        final String script = args[0];
        stopRunningScript(script);
        String projectName = NodeJsUtils.getProjectDisplayName(project);
        AtomicReference<Future<Integer>> taskRef = new AtomicReference<>();
        Future<Integer> task = getExecutable(Bundle.NpmExecutable_run_script(projectName))
                .additionalParameters(getRunScriptParams(args))
                .run(getDescriptor(taskRef));
        taskRef.set(task);
        assert task != null : npmPath;
        setRunningScript(script, taskRef);
        return task;
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "NpmExecutable.install=npm ({0})",
    })
    @CheckForNull
    public Future<Integer> install(String... args) {
        assert !EventQueue.isDispatchThread();
        assert project != null;
        String projectName = NodeJsUtils.getProjectDisplayName(project);
        Future<Integer> task = getExecutable(Bundle.NpmExecutable_install(projectName))
                .additionalParameters(getInstallParams(args))
                .run(getDescriptor());
        assert task != null : npmPath;
        return task;
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "NpmExecutable.uninstall=npm ({0})",
    })
    @CheckForNull
    public Future<Integer> uninstall(String... args) {
        assert !EventQueue.isDispatchThread();
        assert project != null;
        String projectName = NodeJsUtils.getProjectDisplayName(project);
        Future<Integer> task = getExecutable(Bundle.NpmExecutable_uninstall(projectName))
                .additionalParameters(getUninstallParams(args))
                .run(getDescriptor());
        assert task != null : npmPath;
        return task;
    }

    @CheckForNull
    public JSONObject view(String packageName) {
        List<String> params = new ArrayList<>();
        params.add("view"); // NOI18N
        params.add("--json"); // NOI18N
        params.add(packageName);
        JSONObject info = null;
        try {
            StringBuilderInputProcessorFactory factory = new StringBuilderInputProcessorFactory();
            Integer exitCode = getExecutable("npm view").additionalParameters(getParams(params)).
                    redirectErrorStream(false).runAndWait(getSilentDescriptor(), factory, ""); // NOI18N
            String result = factory.getResult();
            if (exitCode != null && exitCode == 0) {
                info = (JSONObject)new JSONParser().parse(result);
            }
        } catch (ExecutionException | ParseException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return info;
    }

    @CheckForNull
    public JSONObject list(int depth) {
        List<String> params = new ArrayList<>();
        params.add("list"); // NOI18N
        params.add("--json"); // NOI18N
        params.add("--depth=" + depth); // NOI18N
        JSONObject info = null;
        try {
            StringBuilderInputProcessorFactory factory = new StringBuilderInputProcessorFactory();
            getExecutable("npm list").additionalParameters(getParams(params)).
                    redirectErrorStream(false).runAndWait(getSilentDescriptor(), factory, ""); // NOI18N
            String result = factory.getResult();
            info = (JSONObject)new JSONParser().parse(result);
        } catch (ExecutionException | ParseException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return info;
    }

    @CheckForNull
    public String search(String searchTerm) {
        List<String> params = new ArrayList<>();
        params.add("search"); // NOI18N
        params.add("--long"); // NOI18N
        params.add("--parseable"); // NOI18N
        params.add(searchTerm);
        String result = null;
        StringBuilderInputProcessorFactory factory = new StringBuilderInputProcessorFactory();
        try {
            Integer exitCode = getExecutable("npm search").additionalParameters(getParams(params)).
                    redirectErrorStream(false).runAndWait(getSilentDescriptor(), factory, ""); // NOI18N
            result = factory.getResult();
            if (result.length() == 0 && exitCode != null && exitCode != 0) {
                result = null;
            }
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return result;
    }

    private ExternalExecutable getExecutable(String title) {
        assert title != null;
        return new ExternalExecutable(getCommand())
                .workDir(getWorkDir())
                .displayName(title)
                .optionsPath(NodeJsOptionsPanelController.OPTIONS_PATH)
                .noOutput(false);
    }

    private ExecutionDescriptor getDescriptor() {
        return getDescriptor(null);
    }

    private ExecutionDescriptor getDescriptor(@NullAllowed final AtomicReference<Future<Integer>> taskRef) {
        assert project != null;
        ExecutionDescriptor descriptor = ExternalExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .showSuspended(true)
                .optionsPath(NodeJsOptionsPanelController.OPTIONS_PATH)
                .outLineBased(true)
                .errLineBased(true);
        if (taskRef != null) {
            descriptor = descriptor.rerunCallback(new ExecutionDescriptor.RerunCallback() {
                @Override
                public void performed(Future<Integer> task) {
                    taskRef.set(task);
                }
            });
        }
        return descriptor;
    }

    private static ExecutionDescriptor getSilentDescriptor() {
        return new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL)
                .inputVisible(false)
                .frontWindow(false)
                .showProgress(false)
                .charset(StandardCharsets.UTF_8);
    }

    private File getWorkDir() {
        if (project == null) {
            return FileUtils.TMP_DIR;
        }
        PackageJson packageJson = new PackageJson(project.getProjectDirectory());
        if (packageJson.exists()) {
            return new File(packageJson.getPath()).getParentFile();
        }
        File sourceRoot = NodeJsUtils.getSourceRoot(project);
        if (sourceRoot != null) {
            return sourceRoot;
        }
        File workDir = FileUtil.toFile(project.getProjectDirectory());
        assert workDir != null : project.getProjectDirectory();
        return workDir;
    }

    private List<String> getRunScriptParams(String... args) {
        List<String> params = new ArrayList<>(args.length + 1);
        params.add(RUN_SCRIPT_PARAM);
        params.addAll(getArgsParams(args));
        return getParams(params);
    }

    private List<String> getInstallParams(String... args) {
        List<String> params = new ArrayList<>(args.length + 1);
        params.add(INSTALL_PARAM);
        params.addAll(getArgsParams(args));
        return getParams(params);
    }

    private List<String> getUninstallParams(String... args) {
        List<String> params = new ArrayList<>(args.length + 1);
        params.add(UNINSTALL_PARAM);
        params.addAll(getArgsParams(args));
        return getParams(params);
    }

    private List<String> getArgsParams(String[] args) {
        return Arrays.asList(args);
    }

    List<String> getParams(List<String> params) {
        assert params != null;
        return params;
    }

    private void stopRunningScript(String script) {
        NodeProcesses.RunInfo npmScript = NodeProcesses.forProject(project).getNpmScript(script);
        if (!npmScript.isRunning()) {
            return;
        }
        // node is running
        assert npmScript.isRunning() : script;
        // force restart
        npmScript.stop();
    }

    private void setRunningScript(String script, AtomicReference<Future<Integer>> taskRef) {
        NodeProcesses.forProject(project).setNpmScript(script, NodeProcesses.RunInfo.run(taskRef));
    }

    @CheckForNull
    private static String validateResult(ValidationResult result) {
        if (result.isFaultless()) {
            return null;
        }
        if (result.hasErrors()) {
            return result.getErrors().get(0).getMessage();
        }
        return result.getWarnings().get(0).getMessage();
    }

    //~ Inner classes

    private static final class MacNpmExecutable extends NpmExecutable {

        private static final String BASH_COMMAND = "/bin/bash -lc"; // NOI18N


        MacNpmExecutable(String npmPath, Project project) {
            super(npmPath, project);
        }

        @Override
        String getCommand() {
            return BASH_COMMAND;
        }

        @Override
        List<String> getParams(List<String> params) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("\""); // NOI18N
            sb.append(npmPath);
            sb.append("\" \""); // NOI18N
            sb.append(StringUtils.implode(super.getParams(params), "\" \"")); // NOI18N
            sb.append("\""); // NOI18N
            return Collections.singletonList(sb.toString());
        }

    }

    private static final class StringBuilderInputProcessorFactory implements ExecutionDescriptor.InputProcessorFactory2 {
        private final StringBuilder result = new StringBuilder();

        @Override
        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return new InputProcessor() {
                @Override
                public void processInput(char[] chars) throws IOException {
                    result.append(chars);
                }
                @Override
                public void reset() throws IOException {
                    result.setLength(0);
                }
                @Override
                public void close() throws IOException {
                }
            };
        }

        String getResult() {
            return result.toString();
        }

    }

}
