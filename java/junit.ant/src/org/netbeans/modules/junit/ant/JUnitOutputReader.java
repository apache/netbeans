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

package org.netbeans.modules.junit.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntSession;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.CommonUtils;
import org.netbeans.modules.gsf.testrunner.api.CoreManager;
import org.netbeans.modules.gsf.testrunner.api.OutputLine;
import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSession.SessionType;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.java.testrunner.JavaRegexpUtils;
import org.netbeans.modules.java.testrunner.ant.utils.AntProject;
import org.netbeans.modules.junit.api.JUnitTestSuite;
import org.netbeans.modules.junit.api.JUnitTestcase;
import org.netbeans.modules.junit.api.JUnitUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.xml.sax.SAXException;

import static java.util.logging.Level.FINER;
import static org.netbeans.modules.java.testrunner.JavaRegexpUtils.ADD_ERROR_PREFIX;
import static org.netbeans.modules.java.testrunner.JavaRegexpUtils.ADD_FAILURE_PREFIX;
import static org.netbeans.modules.java.testrunner.JavaRegexpUtils.END_OF_TEST_PREFIX;
import static org.netbeans.modules.java.testrunner.JavaRegexpUtils.START_OF_TEST_PREFIX;
import static org.netbeans.modules.java.testrunner.JavaRegexpUtils.TESTCASE_PREFIX;
import static org.netbeans.modules.java.testrunner.JavaRegexpUtils.TESTSUITE_PREFIX;
import static org.netbeans.modules.java.testrunner.JavaRegexpUtils.TESTSUITE_STATS_PREFIX;
import static org.netbeans.modules.java.testrunner.JavaRegexpUtils.TESTS_COUNT_PREFIX;
import static org.netbeans.modules.java.testrunner.JavaRegexpUtils.TEST_LISTENER_PREFIX;

/**
 * Obtains events from a single session of an Ant <code>junit</code> task
 * and builds a {@link Report}.
 * The events are delivered by the {@link JUnitAntLogger}.
 *
 * @see  JUnitAntLogger
 * @see  Report
 * @author  Marian Petras
 */
final class JUnitOutputReader {

    private static final int MAX_REPORT_FILE_SIZE = 1 << 22;    //2 MiB
    /** */
    private static final String XML_FORMATTER_CLASS_NAME
            = "org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter";//NOI18N

    /** */
    private final NumberFormat numberFormat = NumberFormat.getInstance();
    
    /** */
    private final SessionType sessionType;

    /** whether XML report is expected */
    private boolean expectXmlReport;
    /** */
    private final long timeOfSessionStart;
    
    /** */
    private JavaRegexpUtils regexp = JavaRegexpUtils.getInstance();
    
    /** */
    private boolean lastHeaderBrief;    
    /** */
    private ClassPath platformSources;
    
    private TestSession testSession;

    private File resultsDir;

    private JUnitTestcase testcase;

    private Report report;

    enum State {DEFAULT, SUITE_STARTED, TESTCASE_STARTED, SUITE_FINISHED, TESTCASE_ISSUE};

    private State state = State.DEFAULT;
    private String testSuite;

    private final String OUTPUT = "------------- Standard Output ---------------";//NOI18N
    private final String ERROR = "------------- Standard Error -----------------";//NOI18N
    private final String END = "------------- ---------------- ---------------";//NOI18N
    private boolean outputStarted = false;
    private boolean errorStarted = false;

    /** Creates a new instance of JUnitOutputReader */
    JUnitOutputReader(final AntSession session,
                      final AntSessionInfo sessionInfo,
                      Project project,
                      final Properties props) {
        CommonUtils.getInstance().setTestingFramework(CommonUtils.JUNIT_TF);
        this.sessionType = sessionInfo.getSessionType();
        File antScript = FileUtil.normalizeFile(session.getOriginatingScript());
        this.timeOfSessionStart = sessionInfo.getTimeOfTestTaskStart();
        if (project == null){
            FileObject fileObj = FileUtil.toFileObject(antScript);
            project = FileOwnerQuery.getOwner(fileObj);
            if (project == null) {
                project = new Project() {
                    public @Override FileObject getProjectDirectory() {
                        return FileUtil.createMemoryFileSystem().getRoot();
                    }
                    public @Override Lookup getLookup() {
                        return Lookup.EMPTY;
                    }
                };
            }
        }
        String className = props.getProperty("classname", "");      //NOI18N
        String methodName = props.getProperty("methodname");        //NOI18N
        String sName = JUnitExecutionManager.JUNIT_CUSTOM_FILENAME.equals(FileUtil.toFileObject(antScript).getName()) ?
                    NbBundle.getMessage(JUnitOutputReader.class, "LBL_RerunFailedTests") :      //NOI18N
                    methodName != null ?
                        className + "." + methodName : className;
        
        CoreManager junitManager = getManagerProvider();
        if(junitManager != null) {
            junitManager.registerNodeFactory();
        }
        this.testSession = new JUnitTestSession(sName, project, sessionType); //NOI18N
        testSession.setRerunHandler(new JUnitExecutionManager(session, testSession, props));
        File projectFile = FileUtil.toFile(project.getProjectDirectory());
        if(projectFile != null) {
            JUnitUtils.logJUnitUsage(Utilities.toURI(projectFile));
        }
    }

    TestSession getTestSession() {
        return testSession;
    }

    void verboseMessageLogged(final AntEvent event) {
        final String msg = event.getMessage();
        if (msg == null) {
            return;
        }
        verboseMessageLogged(msg);
    }

    synchronized void verboseMessageLogged(String msg) {
        switch(state){
            case SUITE_STARTED: {
                if (msg.startsWith(TEST_LISTENER_PREFIX)) {
                    String testListenerMsg = msg.substring(TEST_LISTENER_PREFIX.length());
                    if (testListenerMsg.startsWith(TESTS_COUNT_PREFIX)) {
//                        String countStr = testListenerMsg.substring(TESTS_COUNT_PREFIX.length());
                        return;
                    }

                    int leftBracketIndex = testListenerMsg.indexOf('(');
                    if (leftBracketIndex == -1) {
                        return;
                    }

                    final String shortMsg = testListenerMsg.substring(0, leftBracketIndex);
                    if (shortMsg.equals(START_OF_TEST_PREFIX)) {
                        String restOfMsg = testListenerMsg.substring(START_OF_TEST_PREFIX.length());
                        if (restOfMsg.length() != 0) {
                            char firstChar = restOfMsg.charAt(0);
                            char lastChar = restOfMsg.charAt(restOfMsg.length() - 1);
                            if ((firstChar == '(') && (lastChar == ')')) {
                                testCaseStarted(restOfMsg.substring(1, restOfMsg.length() - 1));
                            }
                        }
                        return;
                    }
                }
                break;
            }
            case TESTCASE_STARTED: {
                if (msg.startsWith(TEST_LISTENER_PREFIX)) {
                    String testListenerMsg = msg.substring(TEST_LISTENER_PREFIX.length());
                    int leftBracketIndex = testListenerMsg.indexOf('(');
                    if (leftBracketIndex == -1) {
                        return;
                    }
                    final String shortMsg = testListenerMsg.substring(0, leftBracketIndex);

                    if (shortMsg.equals(END_OF_TEST_PREFIX)) {
                        String restOfMsg = testListenerMsg.substring(END_OF_TEST_PREFIX.length());
                        if (restOfMsg.length() != 0) {
                            char firstChar = restOfMsg.charAt(0);
                            char lastChar = restOfMsg.charAt(restOfMsg.length() - 1);
                            if ((firstChar == '(') && (lastChar == ')')) {
                                String name = restOfMsg.substring(1, restOfMsg.length() - 1);
                                if (name.equals(testSession.getCurrentTestCase().getName())) {
                                    testCaseFinished();
                                }
                            }
                        }
                        return;
                    } else if (shortMsg.equals(ADD_FAILURE_PREFIX)
                            || shortMsg.equals(ADD_ERROR_PREFIX)) {
                        int lastCharIndex = testListenerMsg.length() - 1;

                        String insideBrackets = testListenerMsg.substring(
                                                                shortMsg.length() + 1,
                                                                lastCharIndex);
                        int commaIndex = insideBrackets.indexOf(',');
                        String testName = (commaIndex == -1)
                                          ? insideBrackets
                                          : insideBrackets.substring(0, commaIndex);
                        if (!testName.equals(testSession.getCurrentTestCase().getName())) {
                            return;
                        }
                        testSession.getCurrentTestCase().setTrouble(new Trouble(shortMsg.equals(ADD_ERROR_PREFIX)));
                        boolean hasErrMsg = (commaIndex != -1) &&
                                ((commaIndex + 2) <= insideBrackets.length()); // #166912
                        if (hasErrMsg) {
                            int errMsgStart;
                            if (Character.isSpaceChar(insideBrackets.charAt(commaIndex + 1))) {
                                errMsgStart = commaIndex + 2;
                            } else {
                                errMsgStart = commaIndex + 1;
                            }
                            String troubleMsg = insideBrackets.substring(errMsgStart);
                            if (!troubleMsg.equals("null")) {                   //NOI18N
                                addStackTraceLine(testSession.getCurrentTestCase(), troubleMsg, false);
                            }
                        }
                        return;
                    }
                }
                break;
            }
            case DEFAULT:
            case SUITE_FINISHED:
            case TESTCASE_ISSUE:
            {
                Matcher matcher = JavaRegexpUtils.JAVA_EXECUTABLE.matcher(msg);
                if (matcher.find()) {
                    String executable = matcher.group(1);
                    ClassPath platformSrcs = findPlatformSources(executable);
                    if (platformSrcs != null) {
                        this.platformSources = platformSrcs;
                    }
                }
                break;
            }
        }
    }

    @SuppressWarnings("fallthrough")
    synchronized void messageLogged(final AntEvent event) {
        final String msg = event.getMessage();
        if (msg == null) {
            return;
        }
        if(msg.startsWith(TESTSUITE_PREFIX) && state == State.DEFAULT) {
            testSuite = msg;
        }
        if(msg.startsWith(TESTSUITE_PREFIX) && !msg.equals(testSuite) && state != State.SUITE_FINISHED) {
            // previous testsuite finished abnormally and state was not set correctly
            state = State.SUITE_FINISHED;
        }

	handleMessage(msg, event.getLogLevel());

        switch (state){
            case TESTCASE_ISSUE:
            case SUITE_FINISHED:{
                if (msg.startsWith(TESTCASE_PREFIX)) {
                    String header = msg.substring(TESTCASE_PREFIX.length());
                    boolean success =
                        lastHeaderBrief
                        ? tryParseBriefHeader(header)
                            || !(lastHeaderBrief = !tryParsePlainHeader(header))
                        : tryParsePlainHeader(header)
                            || (lastHeaderBrief = tryParseBriefHeader(header));
                    if (success) {
                        state = State.TESTCASE_ISSUE;
                    }
                    break;
                }
            }
            case DEFAULT: {
                if (msg.startsWith(TESTSUITE_PREFIX)) {
                    String suiteName = msg.substring(TESTSUITE_PREFIX.length());
                    if (regexp.getFullJavaIdPattern().matcher(suiteName).matches()){
                        suiteStarted(suiteName);
                        resultsDir = determineResultsDir(event);
                    }
                }

                if (state == State.TESTCASE_ISSUE && !msg.equals("")){
                    addStackTraceLine(testcase, msg, true);
                }
                break;
            }
            case SUITE_STARTED: {
                if (msg.startsWith(TESTSUITE_STATS_PREFIX)) {
                    Matcher matcher = regexp.getSuiteStatsPattern().matcher(msg);
                    if (matcher.matches()) {
                        try {
                            suiteFinished(Integer.parseInt(matcher.group(1)),
                                          Integer.parseInt(matcher.group(2)),
                                          Integer.parseInt(matcher.group(3)),
                                          parseTime(matcher.group(4)));
                        } catch (NumberFormatException ex) {
                            assert false : ex; // #175298
                        }
                    } else {
			matcher = regexp.getSuiteStats190Pattern().matcher(msg);
			if (matcher.matches()) {
			    try {
				suiteFinished(Integer.parseInt(matcher.group(1)),
					Integer.parseInt(matcher.group(2)),
					Integer.parseInt(matcher.group(3)),
					parseTime(matcher.group(6)));
			    } catch (NumberFormatException ex) {
				assert false : ex; // #175298
			    }
			} else {
			    assert false :
				    "See bug #185544 \n"
				    + "Please, provide details about your test run: \n"
				    + "JUnit version, a way how tests are launched, \n"
				    + "kind of the project and so on. \n"
				    + "Description of your test environment, a sample \n"
				    + "project and steps to reproduce this bug will be \n"
				    + "highly appreciated.\n"
				    + "Cause of this error is the JUnit message about \n"
				    + "execution of the tests doesn't match for usual "
				    + "regexp pattern: \n"
				    + "message: \"" + msg + "\"\n"
				    + "pattern: \"" + regexp.getSuiteStats190Pattern();
			}
		    }
                    break;
                }
            }
            case TESTCASE_STARTED: {
		int posTestListener = msg.indexOf(TEST_LISTENER_PREFIX);
		if (posTestListener != -1) {
		    displayOutput(msg.substring(0, posTestListener), event.getLogLevel() == AntEvent.LOG_WARN);
		    verboseMessageLogged(msg.substring(posTestListener));
		} else {
		    displayOutput(msg, event.getLogLevel() == AntEvent.LOG_WARN);
		}
                break;
            }
        }
    }

    /**
     */
    private int parseTime(String timeString) {
        int timeMillis;
        try {
            double seconds = numberFormat.parse(timeString).doubleValue();
            timeMillis = Math.round((float) (seconds * 1000.0));
        } catch (ParseException ex) {
            timeMillis = -1;
        }
        return timeMillis;
    }
    
    /**
     * Tries to determine test results directory.
     *
     * @param  event  Ant event serving as a source of information
     * @return  <code>File<code> object representing the results directory,
     *          or <code>null</code> if the results directory could not be
     *          determined
     */
    private static File determineResultsDir(final AntEvent event) {
        File resultsDir = null;
        
        final String taskName = event.getTaskName();
        if (taskName != null) {
            if (taskName.equals("junit")) {                             //NOI18N
                resultsDir = determineJunitTaskResultsDir(event);
            } else if (taskName.equals("java")) {                       //NOI18N
                resultsDir = determineJavaTaskResultsDir(event);
            }
        }
        
        if ((resultsDir != null) && resultsDir.exists() && resultsDir.isDirectory()) {
            return resultsDir;
        } else {
            return null;
        }
    }
    
    /**
     */
    private static File determineJunitTaskResultsDir(final AntEvent event) {
        final TaskStructure taskStruct = event.getTaskStructure();
        if (taskStruct == null) {
            return null;
        }
        
        String todirAttr = null;
        boolean hasXmlFileOutput = false;
        
        for (TaskStructure taskChild : taskStruct.getChildren()) {
            String taskChildName = taskChild.getName();
            if (taskChildName.equals("batchtest")                       //NOI18N
                    || taskChildName.equals("test")) {                  //NOI18N
                todirAttr = taskChild.getAttribute("todir");            //NOI18N
                
            } else if (taskChildName.equals("formatter")) {             //NOI18N
                if (hasXmlFileOutput) {
                    continue;
                }
                String typeAttr = taskChild.getAttribute("type");       //NOI18N
                if ((typeAttr != null)
                        && "xml".equals(event.evaluate(typeAttr))) {    //NOI18N
                    String useFileAttr
                            = taskChild.getAttribute("usefile");        //NOI18N
                    if ((useFileAttr == null)
                        || AntProject.toBoolean(event.evaluate(useFileAttr))) {
                        hasXmlFileOutput = true;
                    }
                }
            }
        }

        if (!hasXmlFileOutput) {
            return null;
        }

        File resultsDir = (todirAttr != null) ? getFile(todirAttr, event)
                                              : getBaseDir(event);
        return findAbsolutePath(resultsDir, taskStruct, event);
    }

    /**
     */
    private static File determineJavaTaskResultsDir(final AntEvent event) {
        final TaskStructure taskStruct = event.getTaskStructure();
        if (taskStruct == null) {
            return null;
        }

        String todirPath = null;
        
        for (TaskStructure taskChild : taskStruct.getChildren()) {
            String taskChildName = taskChild.getName();
            if (taskChildName.equals("arg")) {                          //NOI18N
                String valueAttr = taskChild.getAttribute("value");     //NOI18N
                if (valueAttr == null) {
                    valueAttr = taskChild.getAttribute("line");         //NOI18N
                }
                if (valueAttr != null) {
                    valueAttr = event.evaluate(valueAttr);
                    if (valueAttr.startsWith("formatter=")) {           //NOI18N
                        String formatter = valueAttr.substring("formatter=".length());//NOI18N
                        int commaIndex = formatter.indexOf(',');
                        if ((commaIndex != -1)
                                && formatter.substring(0, commaIndex).equals(XML_FORMATTER_CLASS_NAME)) {
                            String fullReportFileName = formatter.substring(commaIndex + 1);
                            todirPath = new File(fullReportFileName).getParent();
                            if (todirPath == null) {
                                todirPath = ".";                        //NOI18N
                            }
                        }
                    }
                }
            }
        }
            
        if (todirPath == null) {
            return null;
        }

        File resultsDir = (!todirPath.equals(".")) ? new File(todirPath)      //NOI18N
                                             : null;
        return findAbsolutePath(resultsDir, taskStruct, event);
    }

    private static File findAbsolutePath(File path, TaskStructure taskStruct, AntEvent event) {
        if (isAbsolute(path)) {
            return path;
        }

        String forkAttr = taskStruct.getAttribute("fork");              //NOI18N
        if ((forkAttr != null) && AntProject.toBoolean(event.evaluate(forkAttr))) {
            String dirAttr = taskStruct.getAttribute("dir");            //NOI18N
            if (dirAttr != null) {
                path = combine(getFile(dirAttr, event), path);
                if (isAbsolute(path)) {
                    return path;
                }
            }
        }

        return combine(getBaseDir(event), path);
    }
    
    private static File combine(File parentPath, File path) {
        return (path != null) ? new File(parentPath, path.getPath())
                              : parentPath;
    }

    private static boolean isAbsolute(File path) {
        return (path != null) && path.isAbsolute();
    }

    private static File getFile(String attrValue, AntEvent event) {
        return new File(event.evaluate(attrValue));
    }

    private static File getBaseDir(AntEvent event) {
        return new File(event.getProperty("basedir"));                  //NOI18N
    }

    /**
     */
    private ClassPath findPlatformSources(final String javaExecutable) {
        
        /* Copied from JavaAntLogger */
        
        final JavaPlatform[] platforms = JavaPlatformManager.getDefault()
                                         .getInstalledPlatforms();
        for (int i = 0; i < platforms.length; i++) {
            FileObject fo = platforms[i].findTool("java");              //NOI18N
            if (fo != null) {
                File f = FileUtil.toFile(fo);
                if (f.getAbsolutePath().startsWith(javaExecutable)) {
                    return platforms[i].getSourceFolders();
                }
            }
        }
        return null;
    }
    
    private CoreManager getManagerProvider() {
        Collection<? extends Lookup.Item<CoreManager>> providers = Lookup.getDefault().lookupResult(CoreManager.class).allItems();
        for (Lookup.Item<CoreManager> provider : providers) {
            if(provider.getDisplayName().equals(org.netbeans.modules.gsf.testrunner.api.CommonUtils.ANT_PROJECT_TYPE.concat("_").concat(org.netbeans.modules.gsf.testrunner.api.CommonUtils.JUNIT_TF))) {
                return provider.getInstance();
            }
        }
        return null;
    }
    
    /**
     * Notifies that a test (Ant) task was just started.
     *
     * @param  expectedSuitesCount  expected number of test suites going to be
     *                              executed by this task
     */
    void testTaskStarted(int expectedSuitesCount, boolean expectXmlOutput) {
        this.expectXmlReport = expectXmlOutput;
        CoreManager junitManager = getManagerProvider();
        if(junitManager != null) {
            junitManager.testStarted(testSession);
        }
    }
    
    /**
     */
    void testTaskFinished() {
            closePereviousReport(); // #171050
    }

    private void closePereviousReport(){
        TestSuite currentSuite = testSession.getCurrentSuite();
        if (currentSuite != null){
            //try to get results from report xml file
            if (resultsDir != null) {
                File reportFile = findReportFile();
                if ((reportFile != null) && isValidReportFile(reportFile)) {
                    JUnitTestSuite reportSuite = parseReportFile(reportFile);
                    if ((reportSuite != null) && (reportSuite.getName().equals(currentSuite.getName()))) {
                        lastSuiteTime = reportSuite.getElapsedTime();
                        for(Testcase tc: currentSuite.getTestcases()){
                            if (!tc.getOutput().isEmpty()){
                                List<String> output = new ArrayList<>();
                                for(OutputLine l: tc.getOutput()){
                                    output.add(l.getLine());
                                }
                                Testcase rtc = findTest(reportSuite, tc.getName());

                                if (rtc != null)
                                    rtc.addOutputLines(output);
                            }
                        }
                        if (!reportSuite.getTestcases().isEmpty()){
                            currentSuite.getTestcases().clear();
                            currentSuite.getTestcases().addAll(reportSuite.getTestcases());
                        }
                    }
                }
            }
            if (report == null){
                report = testSession.getReport(lastSuiteTime);
            }else{
                report.update(testSession.getReport(lastSuiteTime));
            }
            switch(state){
                case SUITE_STARTED:
                case TESTCASE_STARTED:
		    if (report.getTotalTests() == report.getPassed() + report.getErrors() + report.getFailures()) {
			Logger.getLogger(JUnitOutputReader.class.getName()).log(Level.WARNING, "Ensure that the output-stream is not closed.");
		    } else {
			report.setAborted(true);
		    }
                default:
                    CoreManager junitManager = getManagerProvider();
                    if (junitManager != null) {
                        junitManager.displayReport(testSession, report, true);
                    } else { // update report status as a minimum
                        report.setCompleted(true);
                    }
            }
            report = null;
            lastSuiteTime = 0;
        }

    }

    /**
     */
    void buildFinished(final AntEvent event) {
        CoreManager junitManager = getManagerProvider();
        if(junitManager != null) {
            junitManager.sessionFinished(testSession);
        }
    }

    private long lastSuiteTime = 0;

    /**
     * Notifies that a test suite was just started.
     *
     * @param  suiteName  name of the suite; or {@code null}
     *                    if the suite name is unknown
     */
    private void suiteStarted(final String suiteName) {
        closePereviousReport();
        TestSuite suite = new JUnitTestSuite(suiteName, testSession);
        testSession.addSuite(suite);
        CoreManager junitManager = getManagerProvider();
        if(junitManager != null) {
            junitManager.displaySuiteRunning(testSession, suite);
        }
        state = State.SUITE_STARTED;
        platformSources = null;
    }
    
    private void suiteFinished(int total, int failures, int errors ,long time) {
        int addFail = failures;
        int addError = errors;
        int addPass = total - failures - errors;
        TestSuite suite = testSession.getCurrentSuite();
        for(Testcase tc: suite.getTestcases()){
            switch(tc.getStatus()){
                case ERROR: addError--;break;
                case FAILED: addFail--;break;
                default: addPass--;
            }
        }
        for(int i=0; i<addPass; i++){
            JUnitTestcase tc = new JUnitTestcase("Unknown", "Unknown", testSession); //NOI18N
            tc.setStatus(Status.PASSED);
            testSession.addTestCase(tc);
        }
        for(int i=0; i<addFail; i++){
            JUnitTestcase tc = new JUnitTestcase("Unknown", "Unknown", testSession); //NOI18N
            tc.setStatus(Status.FAILED);
            testSession.addTestCase(tc);
        }
        for(int i=0; i<addError; i++){
            JUnitTestcase tc = new JUnitTestcase("Unknown", "Unknown", testSession); //NOI18N
            tc.setStatus(Status.ERROR);
            testSession.addTestCase(tc);
        }

        lastSuiteTime = time;
        state = State.SUITE_FINISHED;
        testSession.finishSuite(suite);
    }

    private void testCaseStarted(String name){
        JUnitTestcase tc = new JUnitTestcase(name, "JUnit Test", testSession);
        testSession.addTestCase(tc); //NOI18N
        state = State.TESTCASE_STARTED;
    }

    private void testCaseFinished(){
        if (report == null){
            report = testSession.getReport(0);
        }else{
            report.update(testSession.getReport(0));
        }
        CoreManager junitManager = getManagerProvider();
        if(junitManager != null) {
            junitManager.displayReport(testSession, report, false);
        } else { // update report status as a minimum
            report.setCompleted(false);
        }
        state = State.SUITE_STARTED;
    }

    //------------------ UPDATE OF DISPLAY -------------------

    private void handleMessage(String msg, int logLevel) {
	if (msg.equals(END)) {
	    if (outputStarted) {
		outputStarted = false;
	    } else if (errorStarted) {
		errorStarted = false;
	    }
	}
	if (outputStarted || errorStarted) {
	    if (logLevel == AntEvent.LOG_INFO) {
		displayOutput(msg, outputStarted ? false : true);
	    }
	}
	if (msg.equals(OUTPUT)) {
	    outputStarted = true;
	}
	if (msg.equals(ERROR)) {
	    errorStarted = true;
	}
    }
    
    /**
     */
    private void displayOutput(final String text, final boolean error) {
	if (outputStarted || errorStarted) {
	    CoreManager junitManager = getManagerProvider();
            if(junitManager != null) {
                junitManager.displayOutput(testSession, text, error);
            }
	} else {
	    if (!error) {
		List<String> addedLines = new ArrayList<String>();
		addedLines.add(text);
		Testcase tc = testSession.getCurrentTestCase();
		if (tc != null) {
		    tc.addOutputLines(addedLines);
		}
	    }
	}
    }
    
    //--------------------------------------------------------
    
    /**
     * Parses given plain header and sets the time of the test case if possible.
     * @param testcaseHeader the test case header.
     * @return {@code true} is success, otherwise {@code false}.
     */
    private boolean tryParsePlainHeader(String testcaseHeader) {
        final Matcher matcher = regexp.getTestcaseHeaderPlainPattern()
                                .matcher(testcaseHeader);
        if (matcher.matches()) {
            String methodName = matcher.group(1);
            String timeString = matcher.group(2);
            testcase = findTest(testSession.getCurrentSuite(), methodName);
            if(testcase != null) { // #187035
                testcase.setTimeMillis(parseTime(timeString));
                return true;
            }
        }
        return false;
    }

    private JUnitTestcase findTest(TestSuite suite, String methodName){
        JUnitTestcase ret = null;
        for(Testcase tcase: suite.getTestcases()){
            if (tcase.getName().equals(methodName)){
                ret = (JUnitTestcase)tcase;
                break;
            }
        }
        return ret;
    }

    /**
     */
    private boolean tryParseBriefHeader(String testcaseHeader) {
        final Matcher matcher = regexp.getTestcaseHeaderBriefPattern()
                                .matcher(testcaseHeader);
        if (matcher.matches()) {
            String methodName = matcher.group(1);
            String clsName = matcher.group(2);
            boolean error = (matcher.group(3) == null);

            testcase = findTest(testSession.getCurrentSuite(), methodName);
            if (testcase == null){ // probably TestListener interface not reported test progress for some reason (for ex. debug mode)
                testcase = new JUnitTestcase(methodName, "JUnit test", testSession);
                testSession.addTestCase(testcase);
            }
            testcase.setClassName(clsName);
            Trouble trouble = testcase.getTrouble();
            if (trouble == null){
                trouble = new Trouble(error);
                testcase.setTrouble(trouble);
            }else{
                trouble.setError(error);
                trouble.setStackTrace(null);
            }

            return true;
        } else {
            return false;
        }
    }

    private File findReportFile() {
        File file = new File(resultsDir,
                             "TEST-" + testSession.getCurrentSuite().getName() + ".xml"); //NOI18N
        return (file.isFile() ? file : null);
    }

    /**
     */
    private boolean isValidReportFile(File reportFile) {
        if (!reportFile.canRead()) {
            return false;
        }
        
        long lastModified = reportFile.lastModified();
        long timeDelta = lastModified - timeOfSessionStart;
        
        final Logger logger = Logger.getLogger("org.netbeans.modules.junit.outputreader.timestamps");//NOI18N
        final Level logLevel = FINER;
        if (logger.isLoggable(logLevel)) {
            logger.log(logLevel, "Report file: " + reportFile.getPath());//NOI18N
            
            final GregorianCalendar timeStamp = new GregorianCalendar();
            
            timeStamp.setTimeInMillis(timeOfSessionStart);
            logger.log(logLevel, "Session start:    " + String.format("%1$tT.%2$03d", timeStamp, timeStamp.get(GregorianCalendar.MILLISECOND)));//NOI18N
            
            timeStamp.setTimeInMillis(lastModified);
            logger.log(logLevel, "Report timestamp: " + String.format("%1$tT.%2$03d", timeStamp, timeStamp.get(GregorianCalendar.MILLISECOND)));//NOI18N
        }
        
        if (timeDelta >= 0) {
            return true;
        }
        
        return -timeDelta <= timeOfSessionStart % 1000;
        
    }

    private JUnitTestSuite parseReportFile(File reportFile) {
        final long fileSize = reportFile.length();
        if ((fileSize < 0l) || (fileSize > MAX_REPORT_FILE_SIZE)) {
            return null;
        }

        JUnitTestSuite suite = null;
        try {
            suite = XmlOutputParser.parseXmlOutput(
                    new InputStreamReader(new FileInputStream(reportFile), StandardCharsets.UTF_8), testSession);
        } catch (SAXException ex) {
            /* This exception has already been handled. */
        } catch (IOException ex) {
            /*
             * Failed to read the report file - but we still have
             * the report built from the Ant output.
             */
            int severity = ErrorManager.INFORMATIONAL;
            ErrorManager errMgr = ErrorManager.getDefault();
            if (errMgr.isLoggable(severity)) {
                errMgr.notify(
                        severity,
                        errMgr.annotate(
                                ex,
                                "I/O exception while reading JUnit XML report file from JUnit: "));//NOI18N
            }
        }
        return suite;
    }

    /**
     */
    private static int parseNonNegativeInteger(String str)
            throws NumberFormatException {
        final int len = str.length();
        if ((len == 0) || (len > 8)) {
            throw new NumberFormatException();
        }
        
        char c = str.charAt(0);
        if ((c < '0') || (c > '9')) {
            throw new NumberFormatException();
        }
        int result = c - '0';
        
        if (len > 1) {
            for (char d : str.substring(1).toCharArray()) {
                if ((d < '0') || (d > '9')) {
                    throw new NumberFormatException();
                }
                result = 10 * result + (d - '0');
            }
        }
        
        return result;
    }

    private void addStackTraceLine(Testcase testcase, String line, boolean validateST){
        Trouble trouble = testcase.getTrouble();
        if ((trouble == null) || (line == null) || (line.length() == 0) || (line.equals("null"))){  //NOI18N
            return;
        }

        if (validateST){
            boolean valid = false;
            Pattern[] patterns = new Pattern[]{regexp.getCallstackLinePattern(),
                                               regexp.getComparisonHiddenPattern(),
                                               regexp.getFullJavaIdPattern()};
            for(Pattern pattern: patterns){
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()){
                    valid = true;
                    break;
                }
            }
            if (!valid){
                return;
            }
        }
 
        String[] stArray = trouble.getStackTrace();
        if (stArray == null){
            trouble.setStackTrace(new String[]{line});
            Matcher matcher = regexp.getComparisonPattern().matcher(line);
            if (matcher.matches()){
                trouble.setComparisonFailure(
                        new Trouble.ComparisonFailure(
                            matcher.group(1)+matcher.group(2)+matcher.group(3),
                            matcher.group(4)+matcher.group(5)+matcher.group(6))
                );
                return;
            }
            matcher = regexp.getComparisonHiddenPattern().matcher(line);
            if (matcher.matches()){
                trouble.setComparisonFailure(
                        new Trouble.ComparisonFailure(
                            matcher.group(1),
                            matcher.group(2))
                );
                return;
            }

        } else {
            List<String> stList = new ArrayList<>(Arrays.asList(testcase.getTrouble().getStackTrace()));
            if (!line.startsWith(stList.get(stList.size()-1))){
                stList.add(line);
                trouble.setStackTrace(stList.toArray(new String[0]));
            }
        }
    }
}
