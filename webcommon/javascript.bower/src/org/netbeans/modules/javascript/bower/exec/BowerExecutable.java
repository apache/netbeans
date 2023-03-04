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
package org.netbeans.modules.javascript.bower.exec;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.bower.file.BowerJson;
import org.netbeans.modules.javascript.bower.options.BowerOptions;
import org.netbeans.modules.javascript.bower.options.BowerOptionsValidator;
import org.netbeans.modules.javascript.bower.ui.options.BowerOptionsPanelController;
import org.netbeans.modules.javascript.bower.util.BowerUtils;
import org.netbeans.modules.javascript.bower.util.FileUtils;
import org.netbeans.modules.javascript.bower.util.StringUtils;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.ui.api.ExternalExecutable;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

public class BowerExecutable {
    private static final Logger LOGGER = Logger.getLogger(BowerExecutable.class.getName());

    public static final String BOWER_NAME;

    public static final String SAVE_PARAM = "--save"; // NOI18N
    public static final String SAVE_DEV_PARAM = "--save-dev"; // NOI18N

    private static final String INSTALL_PARAM = "install"; // NOI18N
    private static final String UNINSTALL_PARAM = "uninstall"; // NOI18N

    protected final Project project;
    protected final String bowerPath;


    static {
        if (Utilities.isWindows()) {
            BOWER_NAME = "bower.cmd"; // NOI18N
        } else {
            BOWER_NAME = "bower"; // NOI18N
        }
    }


    BowerExecutable(String bowerPath, @NullAllowed Project project) {
        assert bowerPath != null;
        this.bowerPath = bowerPath;
        this.project = project;
    }

    @CheckForNull
    public static BowerExecutable getDefault(@NullAllowed Project project, boolean showOptions) {
        ValidationResult result = new BowerOptionsValidator()
                .validateBower()
                .getResult();
        if (validateResult(result) != null) {
            if (showOptions) {
                OptionsDisplayer.getDefault().open(BowerOptionsPanelController.OPTIONS_PATH);
            }
            return null;
        }
        return createExecutable(BowerOptions.getInstance().getBower(), project);
    }

    private static BowerExecutable createExecutable(String bower, Project project) {
        if (Utilities.isMac()) {
            return new MacBowerExecutable(bower, project);
        }
        return new BowerExecutable(bower, project);
    }

    String getCommand() {
        return bowerPath;
    }

    @CheckForNull
    public Future<Integer> install(String... args) {
        return install(false, args);
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "BowerExecutable.install=Bower ({0})",
        "# {0} - project name",
        "BowerExecutable.install.continous=Bower ({0}, continuous)",
    })
    @CheckForNull
    public Future<Integer> install(boolean continuous, String... args) {
        assert !EventQueue.isDispatchThread();
        assert project != null;
        String projectName = BowerUtils.getProjectDisplayName(project);
        String title = continuous ? Bundle.BowerExecutable_install_continous(projectName) : Bundle.BowerExecutable_install(projectName);
        Future<Integer> task = getExecutable(title)
                .additionalParameters(getInstallParams(args))
                .run(continuous ? getContinuousDescriptor(title) : getDescriptor());
        assert task != null : bowerPath;
        return task;
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "BowerExecutable.uninstall=Bower ({0})",
    })
    @CheckForNull
    public Future<Integer> uninstall(String... args) {
        assert !EventQueue.isDispatchThread();
        assert project != null;
        String projectName = BowerUtils.getProjectDisplayName(project);
        Future<Integer> task = getExecutable(Bundle.BowerExecutable_uninstall(projectName))
                .additionalParameters(getUninstallParams(args))
                .run(getDescriptor());
        assert task != null : bowerPath;
        return task;
    }

    @CheckForNull
    public JSONObject list() {
        List<String> params = new ArrayList<>();
        params.add("list"); // NOI18N
        params.add("--json"); // NOI18N
        params.add("--offline"); // NOI18N
        JSONObject info = null;
        try {
            StringBuilderInputProcessorFactory factory = new StringBuilderInputProcessorFactory();
            getExecutable("bower list").additionalParameters(getParams(params)).
                    redirectErrorStream(false).runAndWait(getSilentDescriptor(), factory, ""); // NOI18N
            String result = factory.getResult();
            info = (JSONObject)new JSONParser().parse(result);
        } catch (ExecutionException | ParseException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return info;
    }

    @CheckForNull
    public JSONArray search(String searchTerm) {
        List<String> params = new ArrayList<>();
        params.add("search"); // NOI18N
        params.add("--json"); // NOI18N
        params.add(searchTerm);
        JSONArray result = null;
        StringBuilderInputProcessorFactory factory = new StringBuilderInputProcessorFactory();
        try {
            Integer exitCode = getExecutable("bower search").additionalParameters(getParams(params)).
                    redirectErrorStream(false).runAndWait(getSilentDescriptor(), factory, ""); // NOI18N
            String rawResult = factory.getResult();
            if (exitCode != null && exitCode == 0) {
                result = (JSONArray)new JSONParser().parse(rawResult);
            }
        } catch (ExecutionException | ParseException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return result;
    }

    @CheckForNull
    public JSONObject info(String packageName) {
        List<String> params = new ArrayList<>();
        params.add("info"); // NOI18N
        params.add("--json"); // NOI18N
        params.add(packageName);
        JSONObject info = null;
        try {
            StringBuilderInputProcessorFactory factory = new StringBuilderInputProcessorFactory();
            Integer exitCode = getExecutable("bower info"). // NOI18N
                    additionalParameters(getParams(params)).
                    redirectErrorStream(false).
                    environmentVariables(doNotAskForPasswordEnvironmentVariables()).
                    runAndWait(getSilentDescriptor(), factory, ""); // NOI18N
                String result = factory.getResult();
                if (exitCode != null && exitCode == 0) {
                    info = (JSONObject)new JSONParser().parse(result);
                }
        } catch (ExecutionException | ParseException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return info;
    }

    /**
     * Returns environment variables that ensure that git (invoked by Bower)
     * doesn't ask for username and password.
     * 
     * @return map with environment variables that ensure that empty strings
     * are used as username and password for git.
     */
    private Map<String,String> doNotAskForPasswordEnvironmentVariables() {
        Map<String,String> map = new HashMap<>();
        map.put("SSH_ASKPASS", "echo"); // NOI18N
        map.put("GIT_ASKPASS", "echo"); // NOI18N
        return map;
    }

    private ExternalExecutable getExecutable(String title) {
        assert title != null;
        return new ExternalExecutable(getCommand())
                .workDir(getWorkDir())
                .displayName(title)
                .optionsPath(BowerOptionsPanelController.OPTIONS_PATH)
                .noOutput(false);
    }

    private ExecutionDescriptor getDescriptor() {
        assert project != null;
        return ExternalExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(BowerOptionsPanelController.OPTIONS_PATH)
                .outLineBased(true)
                .errLineBased(true);
    }

    private ExecutionDescriptor getContinuousDescriptor(String title) {
        return getDescriptor()
                .inputOutput(IOProvider.getDefault().getIO(title, false))
                .noReset(true);
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
        BowerJson bowerJson = new BowerJson(project.getProjectDirectory());
        if (bowerJson.exists()) {
            return new File(bowerJson.getPath()).getParentFile();
        }
        File workDir = FileUtil.toFile(project.getProjectDirectory());
        assert workDir != null : project.getProjectDirectory();
        return workDir;
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

    //~ Inner classes

    private static final class MacBowerExecutable extends BowerExecutable {

        private static final String BASH_COMMAND = "/bin/bash -lc"; // NOI18N


        MacBowerExecutable(String bowerPath, Project project) {
            super(bowerPath, project);
        }

        @Override
        String getCommand() {
            return BASH_COMMAND;
        }

        @Override
        List<String> getParams(List<String> params) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("\""); // NOI18N
            sb.append(bowerPath);
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
