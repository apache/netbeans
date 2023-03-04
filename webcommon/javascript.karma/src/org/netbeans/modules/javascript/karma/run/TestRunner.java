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

package org.netbeans.modules.javascript.karma.run;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.javascript.karma.util.StringUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

// XXX create Handler that will process test results so this class can be tested
@NbBundle.Messages("TestRunner.noName=<no name>")
public final class TestRunner {

    static final Logger LOGGER = Logger.getLogger(TestRunner.class.getName());

    public static final String NB_LINE = "$NB$netbeans "; // NOI18N

    private static final String NEW_LINE_REGEX = "\\s*\\\\n\\s*"; // NOI18N
    private static final String UNCAUGHT_ERROR_PREFIX = "Uncaught Error: "; // NOI18N
    private static final String BROWSER_START = "$NB$netbeans browserStart"; // NOI18N
    private static final String BROWSER_END = "$NB$netbeans browserEnd"; // NOI18N
    private static final String BROWSER_ERROR = "$NB$netbeans browserError"; // NOI18N
    private static final String SUITE_START = "$NB$netbeans suiteStart"; // NOI18N
    private static final String SUITE_END = "$NB$netbeans suiteEnd"; // NOI18N
    private static final String TEST = "$NB$netbeans test"; // NOI18N
    private static final String TEST_PASS = "$NB$netbeans testPass"; // NOI18N
    private static final String TEST_IGNORE = "$NB$netbeans testIgnore"; // NOI18N
    private static final String TEST_FAILURE = "$NB$netbeans testFailure"; // NOI18N
    private static final String NB_VALUE_REGEX = "\\$NB\\$(.*)\\$NB\\$"; // NOI18N
    private static final String NAME_REGEX = "name=" + NB_VALUE_REGEX; // NOI18N
    private static final String BROWSER_REGEX = "browser=" + NB_VALUE_REGEX; // NOI18N
    private static final String DURATION_REGEX = "duration=" + NB_VALUE_REGEX; // NOI18N
    private static final String DETAILS_REGEX = "details=" + NB_VALUE_REGEX; // NOI18N
    private static final String ERROR_REGEX = "error=" + NB_VALUE_REGEX; // NOI18N
    private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);
    private static final Pattern BROWSER_NAME_PATTERN = Pattern.compile(BROWSER_REGEX + " " + NAME_REGEX); // NOI18N
    private static final Pattern BROWSER_ERROR_PATTERN = Pattern.compile(BROWSER_REGEX + " " + ERROR_REGEX); // NOI18N
    private static final Pattern NAME_DURATION_PATTERN = Pattern.compile(NAME_REGEX + " " + DURATION_REGEX); // NOI18N
    private static final Pattern NAME_DETAILS_DURATION_PATTERN = Pattern.compile(NAME_REGEX + " " + DETAILS_REGEX + " " + DURATION_REGEX); // NOI18N

    private static final Pattern STACK_TRACE_FILE_LINE_PATTERN = Pattern.compile("http://[^/]+/(?:base|absolute)(?<FILE>[^?\\s]*)(?:\\?[^:]*)?"); // NOI18N

    private final KarmaRunInfo karmaRunInfo;
    private final AtomicLong browserCount = new AtomicLong();
    private final Map<String, List<String>> browserErrors = new HashMap<>();

    private TestSession testSession;
    private TestSuite testSuite;
    private long testSuiteRuntime = 0;
    private boolean hasTests = false;
    private final ArrayList<String> logMessages = new ArrayList<>();


    public TestRunner(KarmaRunInfo karmaRunInfo) {
        assert karmaRunInfo != null;
        this.karmaRunInfo = karmaRunInfo;
    }

    public void process(String line) {
        LOGGER.finest(line);
        if (line.startsWith(TEST)) {
            testFinished(line);
        } else if (line.startsWith(SUITE_START)) {
            suiteStarted(line);
        } else if (line.startsWith(SUITE_END)) {
            suiteFinished(line);
        } else if (line.startsWith(BROWSER_START)) {
            if (browserCount.incrementAndGet() == 1) {
                sessionStarted(line);
            }
        } else if (line.startsWith(BROWSER_END)) {
            if (browserCount.decrementAndGet() == 0) {
                sessionFinished(line);
            }
        } else if (line.startsWith(BROWSER_ERROR)) {
            browserError(line);
        } else {
            LOGGER.log(Level.FINE, "Unexpected line: {0}", line);
            assert false : line;
        }
    }
    
    public void logMessageToTestResultsWindowOutputView(String line) {
        if(testSession == null) {
            // cache log message
            logMessages.add(line);
            return;
        }
        if(!logMessages.isEmpty()) {
            // flush all previously cached log messages
            for (String log : logMessages) {
                getManager().displayOutput(testSession, log, false);
            }
            logMessages.clear();
        }
        getManager().displayOutput(testSession, line, false);
    }

    private Manager getManager() {
        return Manager.getInstance();
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "TestRunner.runner.title={0} (Karma)",
    })
    private String getOutputTitle() {
        StringBuilder sb = new StringBuilder(30);
        sb.append(ProjectUtils.getInformation(karmaRunInfo.getProject()).getDisplayName());
        String testFile = karmaRunInfo.getTestFile();
        if (testFile != null) {
            sb.append(":"); // NOI18N
            sb.append(new File(testFile).getName());
        }
        return Bundle.TestRunner_runner_title(sb.toString());
    }

    private void initTestSession() {
        if (testSession != null) {
            return;
        }
        Manager.getInstance().setNodeFactory(new KarmaTestRunnerNodeFactory(new CallStackCallback(karmaRunInfo.getProject())));
        testSession = new TestSession(getOutputTitle(), karmaRunInfo.getProject(), TestSession.SessionType.TEST);
        testSession.setRerunHandler(karmaRunInfo.getRerunHandler());
        getManager().testStarted(testSession);
    }

    private void sessionStarted(String line) {
        initTestSession();
    }

    @NbBundle.Messages({
        "TestRunner.tests.error=Uncaught errors occured.",
        "TestRunner.tests.none=No tests executed - perhaps an error occured?",
        "TestRunner.output.verify=Full output can be verified in Output window.",
        "TestRunner.output.view=Full output can be found in Output window.",
    })
    private void sessionFinished(String line) {
        assert testSession != null;
        if (testSuite != null) {
            // can happen for qunit
            suiteFinished(null);
        }
        if (!browserErrors.isEmpty()) {
            processErrors();
            getManager().displayOutput(testSession, Bundle.TestRunner_tests_error(), true);
            getManager().displayOutput(testSession, Bundle.TestRunner_output_verify(), true);
        } else if (!hasTests) {
            getManager().displayOutput(testSession, Bundle.TestRunner_tests_none(), true);
            getManager().displayOutput(testSession, Bundle.TestRunner_output_verify(), true);
        } else {
            getManager().displayOutput(testSession, Bundle.TestRunner_output_view(), false);
        }
        getManager().sessionFinished(testSession);
        testSession = null;
        hasTests = false;
        browserErrors.clear();
    }

    @NbBundle.Messages({
        "# {0} - browser name",
        "TestRunner.browser.error=[{0}] ERROR:",
        "TestRunner.browser.unknown=Unknown",
    })
    private void browserError(String line) {
        initTestSession();
        Matcher matcher = BROWSER_ERROR_PATTERN.matcher(line);
        String browser;
        String error;
        if (matcher.find()) {
            browser = matcher.group(1);
            getManager().displayOutput(testSession, Bundle.TestRunner_browser_error(browser), true);
            error = matcher.group(2);
            if (error.startsWith("\"")) { // NOI18N
                error = error.substring(1);
                if (error.endsWith("\"")) { // NOI18N
                    error = error.substring(0, error.length() - 1);
                }
            }
            String[] errorLines = error.split(NEW_LINE_REGEX);
            for (String errorLine : errorLines) {
                getManager().displayOutput(testSession, errorLine, true);
            }
        } else {
            LOGGER.log(Level.FINE, "Unexpected browser error line: {0}", line);
            getManager().displayOutput(testSession, line, true);
            browser = Bundle.TestRunner_browser_unknown();
            error = line;
            // to work around FindBugs
            assert assertLine(line);
        }
        getManager().displayOutput(testSession, "", false); // NOI18N
        List<String> errors = browserErrors.get(browser);
        if (errors == null) {
            errors = new ArrayList<>();
            browserErrors.put(browser, errors);
        }
        errors.add(error);
    }

    private boolean assertLine(String line) {
        assert false : line;
        return true;
    }

    @NbBundle.Messages({
        "# {0} - browser name",
        "TestRunner.error.suite=[{0}] Uncaught Errors",
    })
    private void processErrors() {
        if (!karmaRunInfo.isFailOnBrowserError()) {
            return;
        }
        for (Map.Entry<String, List<String>> entry : browserErrors.entrySet()) {
            // suite
            TestSuite errorTestSuite = new TestSuite(Bundle.TestRunner_error_suite(entry.getKey()));
            testSession.addSuite(errorTestSuite);
            getManager().displaySuiteRunning(testSession, errorTestSuite.getName());
            // tests
            for (String info : entry.getValue()) {
                String[] details = processDetails(info);
                String name = details[0];
                if (name.startsWith(UNCAUGHT_ERROR_PREFIX)) {
                    name = name.substring(UNCAUGHT_ERROR_PREFIX.length()).trim();
                    if (!StringUtils.hasText(name)) {
                        name = details[0];
                    }
                }
                Trouble trouble = new Trouble(true);
                if (details.length > 1) {
                    String[] stackTrace = new String[details.length - 1];
                    System.arraycopy(details, 1, stackTrace, 0, stackTrace.length);
                    trouble.setStackTrace(stackTrace);
                }
                addTestCase(name, Status.ERROR, 0, trouble);
            }
            getManager().displayReport(testSession, testSession.getReport(0), true);
        }
    }

    @NbBundle.Messages({
        "# {0} - browser name",
        "# {1} - suite name",
        "TestRunner.suite.name=[{0}] {1}",
    })
    private void suiteStarted(String line) {
        assert testSession != null;
        if (testSuite != null) {
            // can happen for more browsers and last browser suite
            suiteFinished(null);
        }
        assert testSuite == null;
        assert testSuiteRuntime == 0;
        String name = line;
        Matcher matcher = BROWSER_NAME_PATTERN.matcher(line);
        if (matcher.find()) {
            name = Bundle.TestRunner_suite_name(matcher.group(1), matcher.group(2));
        } else {
            LOGGER.log(Level.FINE, "Unexpected suite line: {0}", line);
            assert false : line;
        }
        if (!StringUtils.hasText(name)) {
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

    private void testFinished(String line) {
        assert testSession != null;
        hasTests = true;
        if (line.startsWith(TEST_PASS)) {
            testPass(line);
        } else if (line.startsWith(TEST_FAILURE)) {
            testFailure(line);
        } else if (line.startsWith(TEST_IGNORE)) {
            // #259119
            //testIgnore(line);
        } else {
            LOGGER.log(Level.FINE, "Unexpected test line: {0}", line);
            assert false : line;
        }
    }

    private void testPass(String line) {
        Matcher matcher = NAME_DURATION_PATTERN.matcher(line);
        if (matcher.find()) {
            String name = matcher.group(1);
            if (!StringUtils.hasText(name)) {
                name = Bundle.TestRunner_noName();
            }
            long runtime = Long.parseLong(matcher.group(2));
            addTestCase(name, Status.PASSED, runtime);
        } else {
            LOGGER.log(Level.FINE, "Unexpected test PASS line: {0}", line);
            assert false : line;
        }
    }

    private void testFailure(String line) {
        Matcher matcher = NAME_DETAILS_DURATION_PATTERN.matcher(line);
        if (matcher.find()) {
            String name = matcher.group(1);
            if (!StringUtils.hasText(name)) {
                name = Bundle.TestRunner_noName();
            }
            Trouble trouble = new Trouble(false);
            trouble.setStackTrace(processDetails(matcher.group(2)));
            long runtime = Long.parseLong(matcher.group(3));
            addTestCase(name, Status.FAILED, runtime, trouble);
        } else {
            LOGGER.log(Level.FINE, "Unexpected test FAILURE line: {0}", line);
            assert false : line;
        }
    }

    static String[] processDetails(String details) {
        if (details.startsWith("[\"")) { // NOI18N
            details = details.substring(2);
        }
        if (details.endsWith("\"]")) { // NOI18N
            details = details.substring(0, details.length() - 2);
        }
        return STACK_TRACE_FILE_LINE_PATTERN.matcher(details)
                .replaceAll("${FILE}") // NOI18N
                .split(NEW_LINE_REGEX);
    }

    private void testIgnore(String line) {
        Matcher matcher = NAME_PATTERN.matcher(line);
        if (matcher.find()) {
            String name = matcher.group(1);
            if (!StringUtils.hasText(name)) {
                name = Bundle.TestRunner_noName();
            }
            addTestCase(name, Status.IGNORED);
        } else {
            LOGGER.log(Level.FINE, "Unexpected test IGNORE line: {0}", line);
            assert false : line;
        }
    }

    private void addTestCase(String name, Status status) {
        addTestCase(name, status, 0L);
    }

    private void addTestCase(String name, Status status, long runtime) {
        addTestCase(name, status, runtime, null);
    }

    private void addTestCase(String name, Status status, long runtime, Trouble trouble) {
        Testcase testCase = new Testcase(name, "Karma", testSession); // NOI18N
        testCase.setStatus(status);
        testSuiteRuntime += runtime;
        testCase.setTimeMillis(runtime);
        if (trouble != null) {
            testCase.setTrouble(trouble);
        }
        testSession.addTestCase(testCase);
    }

    //~ Inner classes

    private static final class CallStackCallback implements JumpToCallStackAction.Callback {

        private static final Pattern FILE_LINE_PATTERN = Pattern.compile("/(?<FILE>[^:]+):(?<LINE>\\d+)"); // NOI18N

        private final File projectDir;


        public CallStackCallback(Project project) {
            assert project != null;
            projectDir = FileUtil.toFile(project.getProjectDirectory());
        }

        @Override
        public Pair<File, Integer> parseLocation(String callStack) {
            Matcher matcher = FILE_LINE_PATTERN.matcher(callStack);
            if (matcher.find()) {
                File path = new File(matcher.group("FILE").replace('/', File.separatorChar)); // NOI18N
                File file;
                if (path.isAbsolute()) {
                    file = path;
                } else {
                    file = new File(projectDir, path.getPath());
                    if (!file.isFile()) {
                        return null;
                    }
                }
                return Pair.of(file, Integer.parseInt(matcher.group("LINE"))); // NOI18N
            }
            return null;
        }

    }

}
