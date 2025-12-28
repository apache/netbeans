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
package org.netbeans.modules.testng.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
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
import org.netbeans.modules.gsf.testrunner.api.*;
import org.netbeans.modules.gsf.testrunner.api.TestSession.SessionType;
import org.netbeans.modules.testng.api.TestNGTestSuite;
import org.netbeans.modules.testng.api.TestNGTestcase;
import org.netbeans.modules.testng.api.XmlOutputParser;
import org.netbeans.modules.testng.api.XmlResult;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.xml.sax.SAXException;

/**
 * Obtains events from a single session of an Ant
 * <code>junit</code> task and builds a {@link Report}. The events are delivered
 * by the {@link TestNGAntLogger}.
 *
 * @see TestNGAntLogger
 * @see Report
 * @author Marian Petras
 * @author Lukas Jungmann
 */
final class TestNGOutputReader {

    private static final Logger LOG = Logger.getLogger(TestNGOutputReader.class.getName());
    private static final Logger progressLogger = Logger.getLogger(
            "org.netbeans.modules.testng.outputreader.progress");
    /**
     *
     */
    private final NumberFormat numberFormat = NumberFormat.getInstance();
    /**
     *
     */
    private final SessionType sessionType;
    /**
     * whether XML report is expected
     */
    private boolean offline;
    private boolean noresults = true;
    /**
     *
     */
    private final File antScript;
    /**
     *
     */
    private final long timeOfSessionStart;
    private long lastSuiteTime = 0;
    private ClassPath platformSources;
    private TestNGTestSession testSession;
    private Project project;
    private File resultsDir;
    private Map<String, Report> reports;
    private int successPercentage;
    private int passedWithErrorsFailure;
    private boolean descriptionInPassedWithErrors;
    private String suiteName;
    private String testCase;
    private String parameters;
    private String values;
    private String duration;
    private int msDuration;
    private boolean failedInConfigurationMethod;
    private boolean failedInAfterClass;
    private boolean failedInBeforeClass;
    private boolean failedInAfterMethod;
    private boolean failedInBeforeMethod;
    private boolean canAddToStackTrace;
    private long currentTime;
    private String currentSuitename;

    /**
     * Creates a new instance of TestNGOutputReader
     */
    TestNGOutputReader(final AntSession session,
            final AntSessionInfo sessionInfo,
            final Project project,
            final Properties props) {
        CommonUtils.getInstance().setTestingFramework(CommonUtils.TESTNG_TF);
        this.project = project;
        this.sessionType = sessionInfo.getSessionType();
        this.antScript = FileUtil.normalizeFile(session.getOriginatingScript());
        this.timeOfSessionStart = sessionInfo.getTimeOfTestTaskStart();
        if (project == null) {
            FileObject fileObj = FileUtil.toFileObject(antScript);
            this.project = FileOwnerQuery.getOwner(fileObj);
        }
        CoreManager testngManager = getManagerProvider();
        if(testngManager != null) {
            testngManager.registerNodeFactory();
        }
        this.testSession = new TestNGTestSession(sessionInfo.getSessionName(), this.project, sessionType);
        testSession.setRerunHandler(new TestNGExecutionManager(session, testSession, props));
        reports = new HashMap<String, Report>();
    }

    /**
     * for tests
     */
    TestNGOutputReader(TestNGTestSession session) {
        testSession = session;
        sessionType = session.getSessionType();
        antScript = null;
        timeOfSessionStart = System.currentTimeMillis();
        project = session.getProject();
        reports = new HashMap<String, Report>();
    }
    
    private CoreManager getManagerProvider() {
        Collection<? extends Lookup.Item<CoreManager>> providers = Lookup.getDefault().lookupResult(CoreManager.class).allItems();
        for (Lookup.Item<CoreManager> provider : providers) {
            if(provider.getDisplayName().endsWith("_".concat(org.netbeans.modules.gsf.testrunner.api.CommonUtils.TESTNG_TF))) {
                return provider.getInstance();
            }
        }
        return null;
    }

    Project getProject() {
        return project;
    }

    TestSession getTestSession() {
        return testSession;
    }

    void verboseMessageLogged(final AntEvent event) {
        final String msg = event.getMessage();
        if (msg == null) {
            return;
        }
        // msg could contain arbitrary charachters printed by the user,
        // so TestNG specific output could exist e.g. in the middle of msg.
        if (!msg.contains(RegexpUtils.TEST_LISTENER_PREFIX) || offline) {
            //this message is not for us...
            return;
        }
        if (noresults) noresults = false;
        verboseMessageLogged(msg);
//        displayOutput(msg, event.getLogLevel() == AntEvent.LOG_WARN);
    }
    private boolean suiteSummary = false;
    private long elapsedTime = 0;
    // TestNG version > 6.5.2 does not by default add the org.testng.reporters.VerboseReporter listener to the testng task,
    // see https://github.com/cbeust/testng/commit/6fc192911c8c58f38583a657c1edac20e6508004
    // This brakes the "Test" project action as the TestNGAntLogger does not get notified about any output. This leads to
    // waiting to read the results from the build/test/results/testng-results.xml file, which leads to no on-line results 
    // visualization in the Test Results Window. So, the org.testng.reporters.VerboseReporter listener is added to build-impl.xsl
    // file for all project types. This fixes the "Test" project action for TestNG version > 6.5.2 but produces dublicate output
    // for version <= 6.5.2 that need to be escaped. This will happen if old project uses "Dedicated Folder for Storing Libraries",
    // which means that the testng dependency for that project will not be automatically updated while the build-impl.xml file will.
    // These two new variables come to the rescue.
    private String lastMessageLogged = "";
    private boolean suiteFinished = false;

    private class SuiteStats {

        private String name = null;
        private int testRun = -1;
        private int testFail = -1;
        private int testSkip = -1;
        private int confFail = 0;
        private int confSkip = 0;
    }
    private SuiteStats suiteStat;
    private List<String> txt = new ArrayList<String>();

    private static int x = 0;

    private void handleInvocationCount(int invocationCount) {
        double actual = (double) (invocationCount - passedWithErrorsFailure) / (double) invocationCount;
        double expected = (double) successPercentage / 100.0;
	duration = Integer.toString(msDuration);
        if (actual == 100) {
            testFinished("PASSED", suiteName, testCase, parameters, values, duration);
        } else if (actual < expected) {
            testFinished("FAILED", suiteName, testCase, parameters, values, duration);
        } else {
            testFinished("PASSED with failures", suiteName, testCase, parameters, values, duration);
        }
    }
    /**
     */
    synchronized void verboseMessageLogged(String msg) {
        String in = getMessage(msg);
	if(in.equals(lastMessageLogged)) {
	    return;
	}
	lastMessageLogged = in;
        if(descriptionInPassedWithErrors) {
            Matcher m = Pattern.compile(RegexpUtils.TEST_REGEX_3).matcher(in);
            if(m.matches()) {
                int currentInvocation = Integer.parseInt(m.group(3));
                int invocationCount = Integer.parseInt(m.group(4));
                if (currentInvocation == invocationCount) {
                    handleInvocationCount(invocationCount);
                }
            } else {
                assert false : "Cannot match: '" + in + "'.";
            }
            descriptionInPassedWithErrors = false;
            addDescription(in.substring(0, in.lastIndexOf('(')).trim());
            return;
        }
        //suite starting
        if (in.startsWith("RUNNING: ")) {
            passedWithErrorsFailure = 0;
            successPercentage = -1;
	    msDuration = 0;
            descriptionInPassedWithErrors = false;
            failedInAfterClass = false;
            failedInBeforeClass = false;
            failedInAfterMethod = false;
            failedInBeforeMethod = false;
            failedInConfigurationMethod = false;
            canAddToStackTrace = true;
	    suiteFinished = false;
            Matcher m = Pattern.compile(RegexpUtils.RUNNING_SUITE_REGEX).matcher(in);
            if (m.matches()) {
                suiteStarted(m.group(1), Integer.valueOf(m.group(2)), m.group(3));
            } else {
                assert false : "Cannot match: '" + in + "'.";
            }
            return;
        }
        //suite finishing
        if (in.equals("===============================================")) {
	    if(suiteFinished) {
		return;
	    }
            suiteSummary = !suiteSummary;

            if(txt.size() > 0 && failedInAfterClass) {
                testStarted(suiteName, testCase, parameters, values);
                addStackTrace(txt);
                txt.clear();
                testFinished("FAILED", suiteName, testCase, parameters, values, duration);
            }
            if (suiteSummary) {
                suiteStat = new SuiteStats();
            } else {
                suiteFinished(suiteStat);
                suiteStat = null;
		suiteFinished = true;
		lastMessageLogged = "";
            }
            if(txt.size() > 0) {
                addStackTrace(txt);
                txt.clear();
            }
            return;
        } else if (suiteSummary) {
            if (suiteStat.name != null) {
                Matcher m = Pattern.compile(RegexpUtils.STATS_REGEX).matcher(in);
                if (suiteStat.testRun < 0) {
                    //Tests run/fail/skip
                    if (m.matches()) {
                        suiteStat.testRun = Integer.valueOf(m.group(1));
                        suiteStat.testFail = Integer.valueOf(m.group(2));
                        suiteStat.testSkip = Integer.valueOf(m.group(4));
                    } else {
                        assert false : "Cannot match: '" + in + "'.";
                    }
                } else {
                    //Configuration fail/skip
                    if (m.matches()) {
                        suiteStat.confFail = Integer.valueOf(m.group(1));
                        suiteStat.confSkip = Integer.valueOf(m.group(2));
                    } else {
                        assert false : "Cannot match: '" + in + "'.";
                    }
                }
            } else {
                suiteStat.name = in.trim();
            }
            return;
        }
        //test
        if (in.startsWith("INVOKING: ")) {
	    if(txt.size() > 0 && failedInBeforeMethod) {
		testStarted(suiteName, testCase, parameters, values);
		addStackTrace(txt);
		txt.clear();
		testFinished("FAILED", suiteName, testCase, parameters, values, duration);
	    }
            failedInAfterMethod = false;
            failedInBeforeMethod = false;
	    if(failedInBeforeClass) {
                return;
            }
            if(failedInConfigurationMethod) {
                canAddToStackTrace = false;
            }
            if (txt.size() > 0 && !failedInConfigurationMethod) {
                addStackTrace(txt);
                txt.clear();
            }
            Matcher m = Pattern.compile(RegexpUtils.TEST_REGEX).matcher(in);
            if (m.matches()) {
                testStarted(m.group(1), m.group(2), m.group(4), m.group(6));
                String percent = m.group(12);
                if (percent != null) {
                    percent = percent.substring(percent.indexOf(": ") + 2, percent.length() - 1);
                    successPercentage = Integer.parseInt(percent);
                }
            } else {
                Matcher m2 = Pattern.compile(RegexpUtils.TEST_REGEX_2).matcher(in);
                if (m2.matches()) {
                    x++;
                    testStarted(m2.group(1), m2.group(2), m2.group(4), "UNKNOWN#" + x);
                } else {
                    assert false : "Cannot match: '" + in + "'.";
                }
            }
            return;
        }

        Matcher m = Pattern.compile(RegexpUtils.TEST_REGEX).matcher(in);
        if (in.startsWith("PASSED: ")) {
            if (m.matches()) {
                if(successPercentage == -1) {
                    testFinished("PASSED", m.group(1), m.group(2), m.group(4), m.group(6), m.group(8));
                } else {
		    int currentInvocation;
		    int invocationCount;
		    int dur;

		    try {
			dur = Integer.parseInt(m.group(8));
			currentInvocation = Integer.parseInt(m.group(10));
			invocationCount = Integer.parseInt(m.group(11));
			msDuration += dur;
		    } catch (NumberFormatException ex) {
			return;
		    }
		    if (currentInvocation == invocationCount) {
			testFinished("PASSED", m.group(1), m.group(2), m.group(4), m.group(6), Integer.toString(msDuration));
			successPercentage = -1;
		    }
		}
            } else {
                Matcher m2 = Pattern.compile(RegexpUtils.TEST_REGEX_2).matcher(in);
                if (m2.matches()) {
                    testFinished("PASSED", m2.group(1), m2.group(2), m2.group(4), "UNKNOWN#" + x, "0");
                } else {
                    assert false : "Cannot match: '" + in + "'.";
                }
            }
            return;
        }

        if (in.startsWith("PASSED with failures: ")) {
            if (m.matches()) {
                passedWithErrorsFailure++;
                int currentInvocation;
                int invocationCount;
		int dur;

                suiteName = m.group(1);
                testCase = m.group(2);
                parameters = m.group(4);
                values = m.group(6);
                duration = m.group(8);

                try{
		    dur = Integer.parseInt(duration);
                    currentInvocation = Integer.parseInt(m.group(10));
                    invocationCount = Integer.parseInt(m.group(11));
		    msDuration += dur;
                } catch(NumberFormatException ex) {
                    descriptionInPassedWithErrors = true;
                    return;
                }
                if (currentInvocation == invocationCount) {
                    handleInvocationCount(invocationCount);
		    successPercentage = -1;
                }
            } else {
                Matcher m2 = Pattern.compile(RegexpUtils.TEST_REGEX_2).matcher(in);
                if (m2.matches()) {
                    testFinished("PASSED with failures", m2.group(1), m2.group(2), m2.group(4), "UNKNOWN#" + x, "0");
                } else {
                    assert false : "Cannot match: '" + in + "'.";
                }
            }
            return;
        }

        if (in.startsWith("SKIPPED: ")) {
            if (m.matches()) {
                if(failedInConfigurationMethod) {
                    if (txt.size() > 0) {
                        addStackTrace(txt);
                    }
                }
		testFinished("SKIPPED", m.group(1), m.group(2), m.group(4), m.group(6), m.group(8));
            } else {
                Matcher m2 = Pattern.compile(RegexpUtils.TEST_REGEX_2).matcher(in);
                if (m2.matches()) {
                    testFinished("SKIPPED", m2.group(1), m2.group(2), m2.group(4), "UNKNOWN#" + x, "0");
                } else {
                    assert false : "Cannot match: '" + in + "'.";
                }
            }
            return;
        }

        if (in.startsWith("FAILED: ")) {
            if (m.matches()) {
                if(successPercentage == -1) {
                    testFinished("FAILED", m.group(1), m.group(2), m.group(4), m.group(6), m.group(8));
                } else {
		    int currentInvocation;
		    int invocationCount;
		    int dur;

		    try {
			dur = Integer.parseInt(m.group(8));
			currentInvocation = Integer.parseInt(m.group(10));
			invocationCount = Integer.parseInt(m.group(11));
			msDuration += dur;
		    } catch (NumberFormatException ex) {
			return;
		    }
		    if (currentInvocation == invocationCount) {
			testFinished("FAILED", m.group(1), m.group(2), m.group(4), m.group(6), Integer.toString(msDuration));
			successPercentage = -1;
		    }
		}
            } else {
                Matcher m2 = Pattern.compile(RegexpUtils.TEST_REGEX_2).matcher(in);
                if (m2.matches()) {
                    testFinished("FAILED", m2.group(1), m2.group(2), m2.group(4), "UNKNOWN#" + x, "0");
                } else {
                    assert false : "Cannot match: '" + in + "'.";
                }
            }
            return;
        }

        //configuration methods
        if (in.startsWith("FAILED CONFIGURATION: ")) {
            if (m.matches()) {
                suiteName = m.group(1);
                testCase = m.group(2);
                parameters = m.group(4);
                values = m.group(6);
                duration = m.group(8);
            }
            if (in.contains(" - @AfterClass")) {
                failedInAfterClass = true;
                if (txt.size() > 0) {
                    addStackTrace(txt);
                }
            }
            if (in.contains(" - @BeforeClass")) {
                failedInBeforeClass = true;
            }
            if (in.contains(" - @AfterMethod")) {
                failedInAfterMethod = true;
            }
            if (in.contains(" - @BeforeMethod")) {
                failedInBeforeMethod = true;
            }
            failedInConfigurationMethod = true;
            // clear previous stacktrace of FAILED test method as we have a more serious problem in setUp/tearDown methods
            txt.clear();
            return;
        }
        if (in.startsWith("SKIPPED CONFIGURATION: ")) {
            if (failedInAfterMethod && txt.size() > 0) {
		testStarted(suiteName, testCase, parameters, values);
		addStackTrace(txt);
		txt.clear();
		testFinished("FAILED", suiteName, testCase, parameters, values, duration);
	    }
            if (!failedInBeforeClass && txt.size() > 0) {
		addStackTrace(txt);
		txt.clear();
	    }
            if (failedInBeforeClass && txt.size() > 0) {
		testStarted(suiteName, testCase, parameters, values);
		addStackTrace(txt);
		txt.clear();
		testFinished("FAILED", suiteName, testCase, parameters, values, duration);
	    }
            return;
        }
        if (in.startsWith("PASSED CONFIGURATION: ")) {
            if (txt.size() > 0) {
                addStackTrace(txt);
                txt.clear();
            }
            return;
        }
        if (in.contains(" CONFIGURATION: ")) {
	    if(txt.size() > 0 && failedInAfterClass) {
                testStarted(suiteName, testCase, parameters, values);
                addStackTrace(txt);
                txt.clear();
                testFinished("FAILED", suiteName, testCase, parameters, values, duration);
            }
            if (txt.size() > 0) {
                addStackTrace(txt);
                txt.clear();
            }
            return;
        }

        Matcher m1 = Pattern.compile(RegexpUtils.RUNNING_SUITE_REGEX).matcher(in);
        if (!(m.matches() || m1.matches())) {
            if (txt.isEmpty() && in.startsWith("       ")) {
                //we received test description
                addDescription(in.trim());
            } else if (in.trim().length() > 0) {
                //we have a stacktrace
                if(canAddToStackTrace || failedInConfigurationMethod) {
                    txt.add(in);
                }
            }
        }
    }

    synchronized void messageLogged(final AntEvent event) {
        final String msg = event.getMessage();
        if (msg == null) {
            return;
        }
        Testcase tc = testSession.getCurrentTestCase();
        if (tc != null) {
            tc.getOutput().add(new OutputLine(msg, false));
        }
        if (!offline) {
            // msg could contain arbitrary charachters printed by the user,
            // so display them in TRW before parsing TestNG specific output.
            int index = msg.indexOf(RegexpUtils.TEST_LISTENER_PREFIX);
            String message = msg;
            if(index != -1) {
                message = message.substring(0, index);
            }
            //log/verbose level = 0 so don't show output
            if (!message.isEmpty() && !message.startsWith(RegexpUtils.TEST_LISTENER_PREFIX)) {
                displayOutput(message, event.getLogLevel() == AntEvent.LOG_WARN);
            }
            if (index == -1) {
                //this message is not for us...
                return;
            }
            verboseMessageLogged(event);
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
     * @param event Ant event serving as a source of information
     * @return
     * <code>File<code> object representing the results directory,
     *          or
     * <code>null</code> if the results directory could not be determined
     */
    private static File determineResultsDir(final AntEvent event) {
        File resultsDir = null;

        final String taskName = event.getTaskName();
        if (taskName != null) {
            if (taskName.equals("testng")) {                             //NOI18N
                resultsDir = determineTestNGTaskResultsDir(event);
            } else if (taskName.equals("java")) {                       //NOI18N
                resultsDir = determineJavaTaskResultsDir(event);
            } else {
                assert false : "Unexpected task: " + taskName;
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
    private static File determineTestNGTaskResultsDir(final AntEvent event) {
        final TaskStructure taskStruct = event.getTaskStructure();
        if (taskStruct == null) {
            return null;
        }
        String todirAttr = (taskStruct.getAttribute("outputdir") != null) //NOI18N
                ? taskStruct.getAttribute("outputdir") //NOI18N
                : (taskStruct.getAttribute("workingDir") != null) //NOI18N
                ? taskStruct.getAttribute("workingDir") + "test-output" //NOI18N
                : "test-output"; //NOI18N
        File resultsDir = new File(event.evaluate(todirAttr));
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
                    int index = valueAttr.indexOf("-d "); //NOI18N
                    if (-1 < index) {
                        todirPath = valueAttr.substring(index + 3);
                        if (todirPath.contains(" ")) {
                            index = todirPath.startsWith("\"") //NOI18N
                                    ? todirPath.indexOf("\"", 1) + 1 //NOI18N
                                    : todirPath.indexOf(" "); //NOI18N
                            todirPath = todirPath.substring(0, index);
                            //found, let's finish
                            break;
                        }
                    }
                }
            }
        }

        if (todirPath == null) {
            //-d not set, what about parent java/exec's 'dir'?
            String dir = taskStruct.getAttribute("dir");
            if (dir != null) {
                todirPath = event.evaluate(dir) + "/test-output";
            } else {
                todirPath = "test-output";
            }
        }
        File resultsDir = new File(event.evaluate(todirPath));
        return findAbsolutePath(resultsDir, taskStruct, event);
    }

    private static File findAbsolutePath(File path, TaskStructure taskStruct, AntEvent event) {
        if (isAbsolute(path)) {
            return path;
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

        /*
         * Copied from JavaAntLogger
         */

        final JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
        for (int i = 0; i < platforms.length; i++) {
            FileObject fo = platforms[i].findTool("java");              //NOI18N
            if (fo != null) {
                File f = FileUtil.toFile(fo);
                //XXX - look for a "subpath" in case of forked JRE; is there a better way?
                String path = f.getAbsolutePath();
                if (path.startsWith(javaExecutable)
                        || javaExecutable.startsWith(path.substring(0, path.length() - 8))) {
                    return platforms[i].getSourceFolders();
                }
            }
        }
        return null;
    }

    /**
     * Notifies that a test (Ant) task was just started.
     */
    void testTaskStarted(boolean expectXmlOutput, AntEvent event) {
        this.offline = expectXmlOutput;
        if (!offline) {
            CoreManager testngManager = getManagerProvider();
            if (testngManager != null) {
                testngManager.testStarted(testSession);
            }
        }
        resultsDir = determineResultsDir(event);
    }

    /**
     */
    void testTaskFinished() {
        CoreManager testngManager = getManagerProvider();
        if (offline) {
            if (testngManager != null) {
                testngManager.testStarted(testSession);
            }
        }
        if (offline || noresults) {
            //get results from report xml file
            if (resultsDir != null) {
                File reportFile = findReportFile();
                if ((reportFile != null) && isValidReportFile(reportFile)) {
                    XmlResult reportSuite = parseReportFile(reportFile, testSession);
                    for (TestNGTestSuite ts : reportSuite.getTestSuites()) {
                        if (testngManager != null) {
                            testngManager.displaySuiteRunning(testSession, ts);
                        }
                        testSession.setCurrentSuite(ts.getName());
                        testSession.addSuite(ts);
                        Report report = testSession.getReport(ts.getElapsedTime());
                        if (testngManager != null) {
                            testngManager.displayReport(testSession, report, true);
                        } else { // update report status as a minimum
                            report.setCompleted(true);
                        }
                    }
                }
            }
        }
        x = 0;
    }

    /**
     */
    void buildFinished(final AntEvent event) {
        CoreManager testngManager = getManagerProvider();
        if (testngManager != null) {
            testngManager.sessionFinished(testSession);
        }
    }

    //------------------ UPDATE OF DISPLAY -------------------
    /**
     */
    private void displayOutput(final String text, final boolean error) {
        CoreManager testngManager = getManagerProvider();
        if (testngManager != null) {
            testngManager.displayOutput(testSession, text, error);
        }
//        if (state == State.TESTCASE_STARTED) {
//            List<String> addedLines = new ArrayList<String>();
//            addedLines.add(text);
//            Testcase tc = testSession.getCurrentTestCase();
//            if (tc != null) {
//                tc.addOutputLines(addedLines);
//            }
//        }
    }

    //--------------------------------------------------------
    private File findReportFile() {
        File file = new File(resultsDir, "testng-results.xml"); //NOI18N
        return (file.isFile() ? file : null);
    }

    /**
     */
    private boolean isValidReportFile(File reportFile) {
        if (!reportFile.canRead()) {
            return false;
        }

        if (reportFile.canRead()) {
            return true;
        }

        long lastModified = reportFile.lastModified();
        long timeDelta = lastModified - timeOfSessionStart;

        final Logger logger = Logger.getLogger("org.netbeans.modules.testng.outputreader.timestamps");//NOI18N
        final Level logLevel = Level.FINER;
        if (logger.isLoggable(logLevel)) {
            logger.log(logLevel, "Report file: " + reportFile.getPath());//NOI18N

            final GregorianCalendar timeStamp = new GregorianCalendar();

            timeStamp.setTimeInMillis(timeOfSessionStart);
            logger.log(logLevel, "Session start:    " + String.format("%1$tT.%2$03d", timeStamp, timeStamp.get(Calendar.MILLISECOND)));//NOI18N

            timeStamp.setTimeInMillis(lastModified);
            logger.log(logLevel, "Report timestamp: " + String.format("%1$tT.%2$03d", timeStamp, timeStamp.get(Calendar.MILLISECOND)));//NOI18N
        }

        if (timeDelta >= 0) {
            return true;
        }

        /*
         * Normally we would return 'false' here, but:
         *
         * We must take into account that modification timestamps of files
         * usually do not hold milliseconds, just seconds. The worst case we
         * must accept is that the session started on YYYY.MM.DD hh:mm:ss.999
         * and the file was saved exactly in the same millisecond but its time
         * stamp is just YYYY.MM.DD hh:mm:ss, i.e 999 milliseconds earlier.
         */
        return -timeDelta <= timeOfSessionStart % 1000;

//        if (timeDelta < -999) {
//            return false;
//        }
//
//        final GregorianCalendar sessStartCal = new GregorianCalendar();
//        sessStartCal.setTimeInMillis(timeOfSessionStart);
//        int sessStartMillis = sessStartCal.get(Calendar.MILLISECOND);
//        if (timeDelta < -sessStartMillis) {
//            return false;
//        }
//
//        final GregorianCalendar fileModCal = new GregorianCalendar();
//        fileModCal.setTimeInMillis(lastModified);
//        if (fileModCal.get(Calendar.MILLISECOND) != 0) {
//            /* So the file's timestamp does hold milliseconds! */
//            return false;
//        }
//
//        /*
//         * Now we know that milliseconds are not part of file's timestamp.
//         * Let's substract the milliseconds part and check whether the delta is
//         * non-negative, now that we only check seconds:
//         */
//        return lastModified >= (timeOfSessionStart - sessStartMillis);
    }

    private static XmlResult parseReportFile(File reportFile, TestSession session) {
        XmlResult reports = null;
        try {
            reports = XmlOutputParser.parseXmlOutput(
                    new InputStreamReader(new FileInputStream(reportFile), StandardCharsets.UTF_8), session);
        } catch (SAXException ex) {
            /*
             * This exception has already been handled.
             */
        } catch (IOException ex) {
            /*
             * Failed to read the report file - but we still have the report
             * built from the Ant output.
             */
            Logger.getLogger(TestNGOutputReader.class.getName()).log(Level.INFO, "I/O exception while reading TestNG XML report file from TestNG: ", ex);//NOI18N
        }
        return reports;
    }

    private void suiteStarted(String name, int expectedTCases, String config) {
        name = testSession.getSuiteName(name);
        TestSuite suite = new TestNGTestSuite(name, testSession, expectedTCases, config);
        testSession.addSuite(suite);
        testSession.setCurrentSuite(name);
        CoreManager testngManager = getManagerProvider();
        if (testngManager != null) {
            testngManager.displaySuiteRunning(testSession, suite);
        }
        platformSources = null;
        reports.put(name, new Report(name, project));
    }

    private void suiteFinished(SuiteStats stats) {
        stats.name = testSession.getCurrentSuite().getName();
        testSession.setCurrentSuite(stats.name);
        TestNGTestSuite s = (TestNGTestSuite) testSession.getCurrentSuite();
        s.setElapsedTime(elapsedTime);
        s.finish(stats.testRun, stats.testFail, stats.testSkip, stats.confFail, stats.confSkip);
        Report r = reports.get(stats.name);
        r.setElapsedTimeMillis(elapsedTime);
        CoreManager testngManager = getManagerProvider();
        if (testngManager != null) {
            testngManager.displayReport(testSession, r, true);
        } else { // update report status as a minimum
            r.setCompleted(true);
        }
        elapsedTime = 0;
    }

    private void testStarted(String suiteName, String testCase, String parameters, String values) {
        suiteName = testSession.getCurrentSuite().getName();
        testSession.setCurrentSuite(suiteName);
        TestNGTestcase tc = ((TestNGTestSuite)testSession.getCurrentSuite()).getTestCase(testCase, values);
        if (tc == null) {
            tc = new TestNGTestcase(testCase, parameters, values, testSession);
            testSession.addTestCase(tc);
            CoreManager testngManager = getManagerProvider();
            if (testngManager != null) {
                testngManager.testStarted(testSession);
            }
        } else {
            tc.addValues(values);
            //TODO: increment test case time
        }
    }

    private void testFinished(String st, String suiteName, String testCase, String parameters, String values, String duration) {
        suiteName = testSession.getCurrentSuite().getName();
        testSession.setCurrentSuite(suiteName);
        TestNGTestcase tc = ((TestNGTestSuite)testSession.getCurrentSuite()).getTestCase(testCase, values);
        CoreManager testngManager = getManagerProvider();
        if (tc == null) {
            //TestNG does not log invoke message for junit tests...
            tc = new TestNGTestcase(testCase, parameters, values, testSession);
            testSession.addTestCase(tc);
            if (testngManager != null) {
                testngManager.testStarted(testSession);
            }
        }
        assert tc != null;
        if ("PASSED".equals(st)) {
            tc.setStatus(Status.PASSED);
        } else if ("PASSED with failures".equals(st)) {
            tc.setStatus(Status.PASSEDWITHERRORS);
        } else if ("FAILED".equals(st)) {
            tc.setStatus(Status.FAILED);
        } else if ("SKIPPED".equals(st)) {
            tc.setStatus(Status.SKIPPED);
        }
        long dur = 0;
        if (duration != null) {
            dur = Long.valueOf(duration);
        }
        tc.setTimeMillis(dur);
        elapsedTime += dur;
	if("FAILED".equals(st)) {
	    currentTime = dur;
	    currentSuitename = suiteName;
	} else {
	    currentTime = -1;
	    currentSuitename = null;
	    Report r = reports.get(suiteName);
	    r.update(testSession.getReport(dur));
            if (testngManager != null) {
                testngManager.displayReport(testSession, r, false);
            } else { // update report status as a minimum
                r.setCompleted(false);
            }
	}
    }

    private String getMessage(String msg) {
        int prefixLength = RegexpUtils.TEST_LISTENER_PREFIX.length();
        int index = msg.indexOf(RegexpUtils.TEST_LISTENER_PREFIX);
        // msg could contain arbitrary charachters printed by the user,
        // so remove them before parsing TestNG specific output.
        return msg.substring(index + prefixLength).replace("\n", "");
    }

    private void addDescription(String in) {
        Testcase tc = testSession.getCurrentTestCase();
        //FIXME!!! tc should never be null
        //looks like some bug :-(
        if (tc != null) {
            ((TestNGTestcase) tc).setDescription(in);
        }
    }

    private void addStackTrace(List<String> txt) {
        Trouble t = new Trouble(false);
        Matcher matcher = RegexpUtils.getInstance().getComparisonPattern().matcher(txt.get(0));
        if (matcher.matches()) {
            t.setComparisonFailure(
                    new Trouble.ComparisonFailure(
                    matcher.group(1) + matcher.group(2) + matcher.group(3),
                    matcher.group(4) + matcher.group(5) + matcher.group(6)));
        } else {
            matcher = RegexpUtils.getInstance().getComparisonHiddenPattern().matcher(txt.get(0));
            if (matcher.matches()) {
                t.setComparisonFailure(
                        new Trouble.ComparisonFailure(
                        matcher.group(1),
                        matcher.group(2)));
            }
        }
	matcher = RegexpUtils.getInstance().getComparisonAfter65Pattern().matcher(txt.get(0));
	if (matcher.matches()) {
            t.setComparisonFailure(
                    new Trouble.ComparisonFailure(
                    matcher.group(1) + matcher.group(2) + matcher.group(3),
                    matcher.group(4) + matcher.group(5) + matcher.group(6)));
        } else {
            matcher = RegexpUtils.getInstance().getComparisonAfter65HiddenPattern().matcher(txt.get(0));
            if (matcher.matches()) {
                t.setComparisonFailure(
                        new Trouble.ComparisonFailure(
                        matcher.group(1),
                        matcher.group(2)));
            }
        }
        t.setStackTrace(txt.toArray(new String[0]));
        testSession.getCurrentTestCase().setTrouble(t);

	if (currentTime != -1 && currentSuitename != null) {
	    Report r = reports.get(currentSuitename);
	    r.update(testSession.getReport(currentTime));
            CoreManager testngManager = getManagerProvider();
            if (testngManager != null) {
                testngManager.displayReport(testSession, r, false);
            } else { // update report status as a minimum
                r.setCompleted(false);
            }
	}
    }
}
