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
package org.netbeans.modules.selenium2.webclient.api;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.netbeans.modules.selenium2.webclient.spi.JumpToCallStackCallback;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 * @author Theofanis Oikonomou
 */
@NbBundle.Messages("TestRunner.noName=<no name>")
public final class TestRunnerReporter {

    private static final Logger LOGGER = Logger.getLogger(TestRunnerReporter.class.getName());

    private final String NB_LINE;
    
    static final Pattern MULTI_CAPABILITIES = Pattern.compile("\\[launcher\\] Running (?<MULTICAPABILITIES>[\\d]+) instances of WebDriver"); // NOI18N
    static final String CAPABILITY = "(\\[(?<BROWSER>.*) #(?<CAPABILITY>[\\d]+)\\] )?"; // NOI18N
    static final Pattern OK_PATTERN = Pattern.compile("^" + CAPABILITY + "([^(not )]*)ok (?<INDEX>[\\d]+) (?<FULLTITLE>.*), suite=(?<SUITE>.*), testcase=(?<TESTCASE>.*), duration=(?<DURATION>[\\d]+)"); // NOI18N
    static final Pattern OK_SKIP_PATTERN = Pattern.compile("^" + CAPABILITY + "([^(not )]*)ok (?<INDEX>[\\d]+) (?<FULLTITLE>.*) # SKIP -, suite=(?<SUITE>.*), testcase=(?<TESTCASE>.*)"); // NOI18N
    static final Pattern NOT_OK_PATTERN = Pattern.compile("^" + CAPABILITY + "(.*)not ok (?<INDEX>[\\d]+) (?<FULLTITLE>.*), suite=(?<SUITE>.*), testcase=(?<TESTCASE>.*), duration=(?<DURATION>[\\d]+)"); // NOI18N
    static final Pattern SESSION_START_PATTERN = Pattern.compile("^" + CAPABILITY + "1\\.\\.(?<TOTAL>[\\d]+)"); // NOI18N
    static final Pattern SESSION_END_PATTERN = Pattern.compile("^" + CAPABILITY + "(.*)tests (?<TOTAL>[\\d]+), pass (?<PASS>[\\d]+), fail (?<FAIL>[\\d]+), skip (?<SKIP>[\\d]+)"); // NOI18N
    static final String SKIP = " # SKIP -"; // NOI18N
    static final Pattern DONE_PATTERN = Pattern.compile("^(.*)Done."); // NOI18N

    private final RunInfo runInfo;

    private TestSession testSession;
    private TestSuite testSuite;
    private long testSuiteRuntime = 0;
    private boolean hasTests = false;
    private int testIndex = 0;
    private Trouble trouble;
    private final ArrayList<String> stackTrace = new ArrayList<>();
    private String runningSuite;
    private String testcase;
    private long duration;
    private final boolean showOutput;
    private int multiCapabilities = 0;
    private String browser;
    private boolean normalSessionEnd = false;

    public TestRunnerReporter(RunInfo runInfo, String reporterSuffix) {
        assert runInfo != null;
        this.runInfo = runInfo;
        this.showOutput = runInfo.isShowOutput();
        this.NB_LINE = reporterSuffix;
    }
    
    /**
     *
     * @return {@code null} if logMessage should not be shown in the output window
     */
    public String processLine(String logMessage) {
        int index = logMessage.indexOf(NB_LINE);
        String line = removeEscapeCharachters(logMessage).replace(NB_LINE, "");
        
        Matcher matcher;
        matcher = MULTI_CAPABILITIES.matcher(line);
        if(matcher.find()) {
            multiCapabilities = Integer.parseInt(matcher.group("MULTICAPABILITIES"));
            return logMessage;
        }
        
        matcher = DONE_PATTERN.matcher(line);
        if (matcher.find()) {
            if (!normalSessionEnd) {
                // something unexpected has happened
                sessionFinishedAbnormally(line);
            }
            return logMessage;
        }
        
        if(index == -1) {
            if (trouble != null) { // stacktrace from javascript/selenium mocha runner
                stackTrace.add(getStacktrace(line));
                return showOutput ? line : null;
            }
            return logMessage;
        }
        
        matcher = SESSION_START_PATTERN.matcher(line);
        if (matcher.find()) {
            // in "multi capability" mode session is started only once
            if (multiCapabilities == 0 || // capabilities is used
                    (multiCapabilities > 0 && matcher.group("CAPABILITY") == null) || // multiCapabilities is used with only one browser
                    (multiCapabilities > 0 && Integer.parseInt(matcher.group("CAPABILITY")) == 1)) { // multiCapabilities is used with more than one browser
                sessionStarted(line);
                normalSessionEnd = false;
            }
            return showOutput ? line : null;
        }

        matcher = SESSION_END_PATTERN.matcher(line);
        if (matcher.find()) {
            handleTrouble();
            // in "multi capability" mode session is finished only once
            if (multiCapabilities == 0 || // capabilities is used
                    (multiCapabilities > 0 && matcher.group("CAPABILITY") == null) || // multiCapabilities is used with only one browser
                    (multiCapabilities > 0 && Integer.parseInt(matcher.group("CAPABILITY")) == multiCapabilities)) { // multiCapabilities is used with more than one browser
                sessionFinished(line);
                normalSessionEnd = true;
            }
            return showOutput ? "" : null;
        }

        matcher = OK_PATTERN.matcher(line);
        if (matcher.find()) {
            String output2display = parseTestResult(matcher);
            addTestCase(testcase, Status.PASSED, duration);
            if(!showOutput) {
                return null;
            }
            getManager().displayOutput(testSession, output2display, false);
            return output2display;
        }

        matcher = OK_SKIP_PATTERN.matcher(line);
        if (matcher.find()) {
            String output2display = parseTestResult(matcher).concat(SKIP);
            addTestCase(testcase, Status.SKIPPED, duration);
            if(!showOutput) {
                return null;
            }
            getManager().displayOutput(testSession, output2display, false);
            return output2display;
        }

        matcher = NOT_OK_PATTERN.matcher(line);
        if (matcher.find()) {
            String output2display = parseTestResult(matcher);
            if(!showOutput) {
                return null;
            }
            getManager().displayOutput(testSession, output2display, false);
            return output2display;
        }
        if(trouble != null) { // stacktrace from selenium jasmine runner
            stackTrace.add(getStacktrace(line));
        }
        return showOutput ? line : null;
    }
    
    private String getStacktrace(String line) {
        if (multiCapabilities > 0) { // remove browser info from stacktrace
            return line.replaceFirst(CAPABILITY, "");
        }
        return line;
    }
    
    private String getSuiteName(String suite) {
        if(browser == null) {
            return suite;
        }
        return "[" + browser + "] " + suite;
    }
    
    private String parseTestResult(Matcher matcher) {
        handleTrouble();
        testIndex = Integer.parseInt(matcher.group("INDEX"));
        browser = matcher.group("BROWSER");
        String suite = getSuiteName(matcher.group("SUITE"));
        testcase = matcher.group("TESTCASE");
        if (matcher.pattern().pattern().equals(NOT_OK_PATTERN.pattern())) {
            trouble = new Trouble(false);
        }
        if(matcher.pattern().pattern().equals(OK_SKIP_PATTERN.pattern())) {
            duration = 0;
        } else {
            duration = Long.parseLong(matcher.group("DURATION"));
        }
        if (runningSuite == null) {
            runningSuite = suite;
            suiteStarted(runningSuite);
        } else {
            if (!runningSuite.equals(suite)) {
                suiteFinished(runningSuite);
                runningSuite = suite;
                suiteStarted(runningSuite);
            }
        }
        return (matcher.pattern().pattern().equals(NOT_OK_PATTERN.pattern()) ? "not ok " : "ok ") + testIndex + " " + runningSuite + " " + testcase;
    }
    
    private void handleTrouble() {
        if (trouble != null) {
            trouble.setStackTrace(stackTrace.toArray(new String[0]));
            addTestCase(testcase, Status.FAILED, duration, trouble);
            stackTrace.clear();
            trouble = null;
        }
    }
    
    static String removeEscapeCharachters(String line) {
        return line.replace("[?25l", "").replace("[2K", "").replace("\u001B", "").replaceAll("\\[[;\\d]*m", "").trim();
    }

    private Manager getManager() {
        return Manager.getInstance();
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "TestRunner.selenium.runner.title={0} (Selenium)",
        "# {0} - project name",
        "TestRunner.unit.runner.title={0} (Unit)",
    })
    private String getOutputTitle() {
        StringBuilder sb = new StringBuilder(30);
        sb.append(ProjectUtils.getInformation(runInfo.getProject()).getDisplayName());
        if (!runInfo.isTestingProject()) {
            String testFile = runInfo.getTestFile();
            if (testFile != null) {
                sb.append(":"); // NOI18N
                sb.append(new File(testFile).getName());
            }
        }
        return runInfo.isSelenium() ? Bundle.TestRunner_selenium_runner_title(sb.toString()) : Bundle.TestRunner_unit_runner_title(sb.toString());
    }

    private void sessionStarted(String line) {
        assert testSession == null;
        getManager().setNodeFactory(Utilities.getTestRunnerNodeFactory(new CallStackCallback(runInfo.getProject())));
        testSession = new TestSession(getOutputTitle(), runInfo.getProject(), TestSession.SessionType.TEST);
        testSession.setRerunHandler(runInfo.getRerunHandler());
        getManager().testStarted(testSession);
        if(showOutput) {
            getManager().displayOutput(testSession, line, false);
        }
    }

    @NbBundle.Messages({
        "TestRunner.tests.none.1=No tests executed - perhaps an error occured?",
        "TestRunner.tests.none.2=\nFull output can be verified in Output window.",
        "TestRunner.output.full=\nFull output can be found in Output window.",
    })
    private void sessionFinished(String line) {
        assert testSession != null;
        if (testSuite != null) {
            // can happen for qunit
            suiteFinished(null);
        }
        if (!hasTests) {
            getManager().displayOutput(testSession, Bundle.TestRunner_tests_none_1(), true);
            getManager().displayOutput(testSession, Bundle.TestRunner_tests_none_2(), true);
        } else {
            getManager().displayOutput(testSession, Bundle.TestRunner_output_full(), false);
        }
        getManager().sessionFinished(testSession);
        testSession = null;
        runningSuite = null;
        hasTests = false;
    }
    
    @NbBundle.Messages({
        "TestRunner.session.finished.abnormally=Test session terminated abnormally - perhaps an error occured?"
    })
    private void sessionFinishedAbnormally(String line) {
        getManager().displayOutput(testSession, Bundle.TestRunner_session_finished_abnormally(), false);
        getManager().sessionFinished(testSession);
        testSession = null;
        runningSuite = null;
        hasTests = false;
    }

    @NbBundle.Messages({
        "# {0} - suite name",
        "TestRunner.suite.name={0}",
    })
    private void suiteStarted(String line) {
        assert testSession != null;
        if (testSuite != null) {
            // can happen for more browsers and last browser suite
            suiteFinished(null);
        }
        assert testSuite == null;
        assert testSuiteRuntime == 0;
        String name = Bundle.TestRunner_suite_name(line);
        if (!StringUtilities.hasText(name)) {
            name = Bundle.TestRunner_noName();
        }
        testSuite = new TestSuite(name);
        testSession.addSuite(testSuite);
        getManager().displaySuiteRunning(testSession, testSuite.getName());
    }

    private void suiteFinished(@NullAllowed String line) {
        assert testSession != null;
        if (testSuite == null) {
            // current suite already finished
            return;
        }
        getManager().displayReport(testSession, testSession.getReport(testSuiteRuntime), true);
        testSuite = null;
        testSuiteRuntime = 0;
    }

    private void addTestCase(String name, Status status) {
        addTestCase(name, status, 0L);
    }

    private void addTestCase(String name, Status status, long runtime) {
        addTestCase(name, status, runtime, null);
    }

    private void addTestCase(String name, Status status, long runtime, Trouble trouble) {
        hasTests = true;
        Testcase testCase = new Testcase(name, "Selenium", testSession); // NOI18N
        testCase.setStatus(status);
        testSuiteRuntime += runtime;
        testCase.setTimeMillis(runtime);
        if (trouble != null) {
            testCase.setTrouble(trouble);
        }
        testSession.addTestCase(testCase);
    }

    // ~ Inner classes
    
    public static final class CallStackCallback implements JumpToCallStackCallback {

        // at /Users/fanis/selenium2_work/NodeJsApplication/node_modules/selenium-webdriver/lib/webdriver/promise.js:1425:29
        // at notify (/Users/fanis/selenium2_work/NodeJsApplication/node_modules/selenium-webdriver/lib/webdriver/promise.js:465:12)
        // at C:\Users\toikonom\AppData\Local\Temp\AngularJSPhoneCat\node_modules\protractor\lib\protractor.js:1041:17
        // at [object Object].webdriver.promise.ControlFlow.runInNewFrame_ (C:\Users\toikonom\AppData\Local\Temp\AngularJSPhoneCat\node_modules\protractor\node_modules\selenium-webdriver\lib\webdriver\promise.js:1539:20)
        // at Context.<anonymous> (test/test.js:8:24)
        static final Pattern FILE_LINE_PATTERN_UNIX = Pattern.compile("^" + CAPABILITY + "(.*)at ([^/(]*)(?<FILE>[^:]+):(?<LINE>\\d+):(?<COLUMN>\\d+)"); // NOI18N
        static final Pattern FILE_LINE_PATTERN_WINDOWS = Pattern.compile("^" + CAPABILITY + "(.*)at (.*)(?<DRIVE>[a-zA-Z]:)(?<FILE>[^:]+):(?<LINE>\\d+):(?<COLUMN>\\d+)"); // NOI18N

        final Project project;


        public CallStackCallback(Project project) {
            assert project != null;
            this.project = project;
        }

        @Override
        public Pair<File, int[]> parseLocation(String callStack, boolean underTestRoot) {
            Matcher matcher = FILE_LINE_PATTERN_WINDOWS.matcher(callStack.trim());
            boolean matchFound = matcher.find();
            String drive = null;
            if(matchFound) {
                drive = matcher.group("DRIVE"); // NOI18N
            } else {
                matcher = FILE_LINE_PATTERN_UNIX.matcher(callStack.trim());
                matchFound = matcher.find();
            }
            if (matchFound) {
                String pathname = drive == null ? matcher.group("FILE") : drive.concat(matcher.group("FILE")); // NOI18N
                // mocha changed the way it might report failures' stack traces after 2.2.3
                // This might result in pathname being '(...', so we need to ommit the starting '('
                File path = new File(pathname.replace("(", ""));
                File file;
                FileObject projectDir = project.getProjectDirectory();
                if (path.isAbsolute()) {
                    file = path;
                } else {
                    file = new File(FileUtil.toFile(projectDir), path.getPath());
                    if (!file.isFile()) {
                        return null;
                    }
                }
                FileObject parent = underTestRoot ? Utilities.getTestsSeleniumFolder(project, false) : projectDir;
                FileObject fo = FileUtil.toFileObject(file);
                if(fo == null || (underTestRoot && !FileUtil.isParentOf(parent, fo))) {
                    return null;
                }
                return Pair.of(file, new int[] {Integer.parseInt(matcher.group("LINE")), Integer.parseInt(matcher.group("COLUMN"))}); // NOI18N
            }
            return null;
        }

    }

}

