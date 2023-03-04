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

package org.netbeans.modules.javascript.karma.exec;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.javascript.karma.browsers.Browser;
import org.netbeans.modules.javascript.karma.browsers.Browsers;
import org.netbeans.modules.javascript.karma.options.KarmaOptions;
import org.netbeans.modules.javascript.karma.options.KarmaOptionsValidator;
import org.netbeans.modules.javascript.karma.preferences.KarmaPreferencesValidator;
import org.netbeans.modules.javascript.karma.run.KarmaRunInfo;
import org.netbeans.modules.javascript.karma.run.TestRunner;
import org.netbeans.modules.javascript.karma.ui.KarmaErrorsDialog;
import org.netbeans.modules.javascript.karma.ui.options.KarmaOptionsPanelController;
import org.netbeans.modules.javascript.karma.util.FileUtils;
import org.netbeans.modules.javascript.karma.util.StringUtils;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProviders;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.ui.api.ExternalExecutable;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

public class KarmaExecutable {

    private static final Logger LOGGER = Logger.getLogger(KarmaExecutable.class.getName());

    public static final String KARMA_NAME;

    private static final String START_COMMAND = "start";
    private static final String RUN_COMMAND = "run";
    private static final String PORT_PARAMETER = "--port";

    protected final String karmaPath;
    protected final Project project;


    static {
        if (Utilities.isWindows()) {
            KARMA_NAME = "karma.cmd"; // NOI18N
        } else {
            KARMA_NAME = "karma"; // NOI18N
        }
    }


    KarmaExecutable(String karmaPath, Project project) {
        assert karmaPath != null;
        assert project != null;
        this.karmaPath = karmaPath;
        this.project = project;
    }

    @CheckForNull
    public static KarmaExecutable getDefault(Project project, boolean showOptionsOrCustomizer) {
        assert project != null;
        // options
        ValidationResult result = new KarmaOptionsValidator()
                .validateKarma()
                .getResult();
        if (validateResult(result) != null) {
            if (showOptionsOrCustomizer) {
                OptionsDisplayer.getDefault().open(KarmaOptionsPanelController.OPTIONS_PATH);
            }
            return null;
        }
        // customizer
        result = new KarmaPreferencesValidator()
                .validate(project)
                .getResult();
        if (validateResult(result) != null) {
            if (showOptionsOrCustomizer) {
                project.getLookup().lookup(CustomizerProvider2.class).showCustomizer(JsTestingProviders.CUSTOMIZER_IDENT, null);
            }
            return null;
        }
        return createExecutable(KarmaOptions.getInstance().getKarma(), project);
    }

    private static KarmaExecutable createExecutable(String karma, Project project) {
        if (Utilities.isMac()) {
            return new MacKarmaExecutable(karma, project);
        }
        return new KarmaExecutable(karma, project);
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "KarmaExecutable.start=Karma ({0})",
    })
    @CheckForNull
    public Future<Integer> start(int port, KarmaRunInfo karmaRunInfo) {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Runnable countDownTask = new Runnable() {
            @Override
            public void run() {
                countDownLatch.countDown();
            }
        };
        Future<Integer> task = getExecutable(Bundle.KarmaExecutable_start(ProjectUtils.getInformation(project).getDisplayName()), getProjectDir())
                .additionalParameters(getStartParams(port, karmaRunInfo))
                .environmentVariables(karmaRunInfo.getEnvVars())
                .run(getStartDescriptor(karmaRunInfo, countDownTask));
        assert task != null : karmaPath;
        try {
            countDownLatch.await(15, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if (task.isDone()) {
            // some error, task is not running
            return null;
        }
        return task;
    }

    public void runTests(int port) {
        getExecutable("karma run...", getProjectDir()) // NOI18N
                .additionalParameters(getRunParams(port))
                // XXX wait?
                .run(getRunDescriptor());
    }

    private File getProjectDir() {
        return FileUtil.toFile(project.getProjectDirectory());
    }

    String getCommand() {
        return karmaPath;
    }

    private ExternalExecutable getExecutable(String title, File workDir) {
        return new ExternalExecutable(getCommand())
                .workDir(workDir)
                .displayName(title)
                .noOutput(false);
    }

    private ExecutionDescriptor getStartDescriptor(KarmaRunInfo karmaRunInfo, Runnable serverStartTask) {
        return new ExecutionDescriptor()
                .frontWindow(false)
                .frontWindowOnError(false)
                .outLineBased(true)
                .errLineBased(true)
                .outConvertorFactory(new ServerLineConvertorFactory(karmaRunInfo, serverStartTask))
                .postExecution(serverStartTask);
    }

    private ExecutionDescriptor getRunDescriptor() {
        return new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL);
    }

    List<String> getStartParams(int port, KarmaRunInfo karmaRunInfo) {
        List<String> params = new ArrayList<>(4);
        params.add(START_COMMAND);
        params.add(karmaRunInfo.getNbConfigFile());
        params.add(PORT_PARAMETER);
        params.add(Integer.toString(port));
        return params;
    }

    List<String> getRunParams(int port) {
        List<String> params = new ArrayList<>(3);
        params.add(RUN_COMMAND);
        params.add(PORT_PARAMETER);
        params.add(Integer.toString(port));
        return params;
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

    // #238974
    private static final class MacKarmaExecutable extends KarmaExecutable {

        private static final String BASH_COMMAND = "/bin/bash -lc"; // NOI18N


        MacKarmaExecutable(String karmaPath, Project project) {
            super(karmaPath, project);
        }

        @Override
        String getCommand() {
            return BASH_COMMAND;
        }

        @Override
        List<String> getStartParams(int port, KarmaRunInfo karmaRunInfo) {
            return getParams(super.getStartParams(port, karmaRunInfo));
        }

        @Override
        List<String> getRunParams(int port) {
            return getParams(super.getRunParams(port));
        }

        private List<String> getParams(List<String> originalParams) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("\""); // NOI18N
            sb.append(karmaPath);
            sb.append("\" \""); // NOI18N
            sb.append(StringUtils.implode(originalParams, "\" \"")); // NOI18N
            sb.append("\""); // NOI18N
            return Collections.singletonList(sb.toString());
        }

    }

    private static final class ServerLineConvertorFactory implements ExecutionDescriptor.LineConvertorFactory {

        private final LineConvertor serverLineConvertor;


        public ServerLineConvertorFactory(KarmaRunInfo karmaRunInfo, Runnable startFinishedTask) {
            assert karmaRunInfo != null;
            assert startFinishedTask != null;
            serverLineConvertor = new ServerLineConvertor(karmaRunInfo, startFinishedTask);
        }

        @Override
        public LineConvertor newLineConvertor() {
            return serverLineConvertor;
        }

    }

    private static final class ServerLineConvertor implements LineConvertor {

        private static final boolean DEBUG = Boolean.getBoolean("nb.karma.debug"); // NOI18N

        private static final String NB_BROWSERS = "$NB$netbeans browsers "; // NOI18N
        private static final String KARMA_ERROR = "[31mERROR ["; // NOI18N
        private static final String KARMA_WARN = "[33mWARN ["; // NOI18N

        private final KarmaRunInfo karmaRunInfo;
        private final Runnable startFinishedTask;
        private final TestRunner testRunner;

        private boolean firstLine = true;
        private boolean startFinishedTaskRun = false;
        private Collection<Browser> browsers = null;
        private int browserCount = -1;
        private int connectedBrowsers = 0;


        public ServerLineConvertor(KarmaRunInfo karmaRunInfo, Runnable startFinishedTask) {
            assert karmaRunInfo != null;
            assert startFinishedTask != null;
            this.karmaRunInfo = karmaRunInfo;
            this.startFinishedTask = startFinishedTask;
            testRunner = new TestRunner(karmaRunInfo);
        }

        @Override
        public List<ConvertedLine> convert(String line) {
            // info
            if (firstLine
                    && line.contains(karmaRunInfo.getNbConfigFile())) {
                firstLine = false;
                return Collections.singletonList(ConvertedLine.forText(
                        line.replace(karmaRunInfo.getNbConfigFile(), karmaRunInfo.getProjectConfigFile()), null));
            }
            // startup
            if (browsers == null) {
                // server start
                if (line.startsWith(NB_BROWSERS)) {
                    List<String> allBrowsers = StringUtils.explode(line.substring(NB_BROWSERS.length()), ","); // NOI18N
                    browserCount = allBrowsers.size();
                    browsers = Browsers.getBrowsers(allBrowsers);
                    return Collections.emptyList();
                }
            }
            if (startFinishedTask != null
                    && !startFinishedTaskRun
                    && line.contains("Connected on socket")) { // NOI18N
                assert browsers != null;
                connectedBrowsers++;
                if (connectedBrowsers == browserCount) {
                    startFinishedTask.run();
                    startFinishedTaskRun = true;
                }
            } else if (line.startsWith(TestRunner.NB_LINE)) {
                // test result
                testRunner.process(line);
                if (DEBUG) {
                    return Collections.singletonList(ConvertedLine.forText(line, null));
                }
                return Collections.emptyList();
            }
            // some error before browser startup?
            if (connectedBrowsers < browserCount
                    && (line.contains(KARMA_ERROR) || line.contains(KARMA_WARN))) {
                KarmaErrorsDialog.getInstance().show();
            }
            // process output
            OutputListener outputListener = null;
            if (browsers == null) {
                // some error?
                Pair<String, Integer> fileLine = FileLineParser.getOutputFileLine(line);
                if (fileLine != null) {
                    outputListener = new FileOutputListener(fileLine.first(), fileLine.second());
                }
                return Collections.singletonList(ConvertedLine.forText(line, outputListener));
            } else {
                // karma log
                for (Browser browser : browsers) {
                    Pair<String, Integer> fileLine = browser.getOutputFileLine(line);
                    if (fileLine != null) {
                        outputListener = new FileOutputListener(fileLine.first(), fileLine.second());
                        break;
                    }
                }
            }
            testRunner.logMessageToTestResultsWindowOutputView(line);
            return Collections.singletonList(ConvertedLine.forText(line, outputListener));
        }

    }

    private static final class FileOutputListener implements OutputListener {

        final String file;
        final int line;


        public FileOutputListener(String file, int line) {
            assert file != null;
            this.file = file;
            this.line = line;
        }

        @Override
        public void outputLineSelected(OutputEvent ev) {
            // noop
        }

        @Override
        public void outputLineAction(OutputEvent ev) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    FileUtils.openFile(new File(file), line);
                }
            });
        }

        @Override
        public void outputLineCleared(OutputEvent ev) {
            // noop
        }

    }

    static final class FileLineParser {

        // (/usr/lib/node_modules/karma/node_modules/coffee-script/lib/coffee-script/coffee-script.js:211:36)
        // ^/home/gapon/NetBeansProjects/Calculator-PHPUnit5/README.md:1$
        static final Pattern OUTPUT_FILE_LINE_PATTERN = Pattern.compile("(?:^|\\()(?<FILE>[^(]+?):(?<LINE>\\d+)(?::\\d+)?(?:$|\\))"); // NOI18N


        static Pair<String, Integer> getOutputFileLine(String line) {
            Matcher matcher = OUTPUT_FILE_LINE_PATTERN.matcher(line);
            if (!matcher.find()) {
                return null;
            }
            String file = matcher.group("FILE"); // NOI18N
            if (!new File(file).isFile()) {
                // incomplete path
                return null;
            }
            return Pair.of(file, Integer.valueOf(matcher.group("LINE"))); // NOI18N
        }

    }

}
