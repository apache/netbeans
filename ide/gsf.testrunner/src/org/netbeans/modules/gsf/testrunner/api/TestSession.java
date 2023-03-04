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
package org.netbeans.modules.gsf.testrunner.api;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.util.Parameters;

/**
 * Represents a test session, i.e. a single run of tests (e.g. all the tests
 * in the project, a single suite, a single test case etc).
 *
 * @author Erno Mononen
 */
public class TestSession {

    public enum SessionType {
        TEST,
        DEBUG
    }
    /**
     * Counter for failures/errors.
     */
    private long failuresCount = 0;
    
    private final FileLocator fileLocator;
    private final SessionType sessionType;
    /**
     * The name of this session. Will be used as the display 
     * name for the output tab.
     */
    private final String name;
    /**
     * The project where this session is invoked.
     */
    private WeakReference<Project> project;
    private final URI projectURI;
    /**
     * The suites that were executed.
     */
    private final List<TestSuite> testSuites = new ArrayList<TestSuite>();
    /**
     * Current suite indexes.
     */
    private final Stack<Integer> suiteIdxs = new Stack<Integer>();
    /**
     * Holds output for testcases. Since a testcase is created only after 
     * a test finishes, the output of that testcase needs to be associated 
     * with it after it has been created.
     */
    private final List<String> output = new ArrayList<String>();
    /*
     * The message to display when this session is starting.
     */
    private String startingMsg;

    /**
     * Handles re-running of this session's execution.
     */
    private RerunHandler rerunHandler;
    
    /**
     * Constructs a new session. 
     * 
     * @param name the name for the session.
     * @param project the project where the session is invoked.
     * @param sessionType the type of the session.
     */
    public TestSession(String name, Project project, SessionType sessionType) {
        Parameters.notNull("name", name);
        Parameters.notNull("project", project);
        this.name = name;
        this.project = new WeakReference<Project>(project);
        this.projectURI = project.getProjectDirectory().toURI();
        this.fileLocator = project.getLookup().lookup(FileLocator.class);
        this.sessionType = sessionType;
    }

    /**
     * @return the handler for this session or <code>null</code>.
     */
    public RerunHandler getRerunHandler() {
        return rerunHandler;
    }

    /**
     * Sets the rerun handler for this session.
     * @param rerunHandler
     */
    public void setRerunHandler(RerunHandler rerunHandler) {
        Parameters.notNull("rerunHandler", rerunHandler);
        this.rerunHandler = rerunHandler;
    }

    /**
     * @see #startingMsg
     */
    public void setStartingMsg(String startingMsg) {
        this.startingMsg = startingMsg;
    }

    /**
     * @see #startingMsg
     */
    public String getStartingMsg() {
        return startingMsg;
    }

    /**
     * @return the project where this session for invoked.
     */
    public Project getProject() {
        Project prj = project.get();
        if (prj == null) {
            prj = FileOwnerQuery.getOwner(projectURI);
	    assert prj != null : "Project was null for projectURI: " + projectURI; //NOI18N
            project = new WeakReference<Project>(prj);
        }
        return prj;
    }

    /**
     * @return the currently running test case or <code>null</code>
     * if there is no test case running.
     */
    public Testcase getCurrentTestCase() {
        if (getCurrentSuite() == null) {
            return null;
        }
        List<Testcase> testcases = getCurrentSuite().getTestcases();
        return testcases.isEmpty() ? null : testcases.get(testcases.size() - 1);
     }

    /**
     * @return the all test cases in this session or an empty list
     * if there are none.
     */
    public List<Testcase> getAllTestCases() {
        List<Testcase> all = new ArrayList<Testcase>();
        for (TestSuite suite : testSuites) {
            all.addAll(suite.getTestcases());
        }
        return all;
    }

    /**
     * Adds the given suite to this session. The lastly added
     * suite is considered as the currently running one (see {@link #getCurrentSuite() }.
     * 
     * @param suite the suite to add.
     */
    public void addSuite(TestSuite suite) {
        Parameters.notNull("suite", suite);
        if (!output.isEmpty() && getCurrentSuite() != null) {
            Testcase testcase = getCurrentSuite().getLastTestCase();
            if (testcase != null) {
                testcase.addOutputLines(output);
                output.clear();
            }
        }
        synchronized (this) {
            suiteIdxs.push(testSuites.size());
            testSuites.add(suite);
        }
    }

    /**
     * Marks the currently running test suite as finished.
     *
     * @param suite the suite to mark as finished
     * @since 2.26
     */
    public synchronized void finishSuite(TestSuite suite) {
        if (!suiteIdxs.isEmpty() && suite == getCurrentSuite()) {
            suiteIdxs.pop();
        }
    }

    /**
     * Adds the given line as output of the current testcase.
     * @param line
     */
    public void addOutput(String line) {
        output.add(line);
    }

    /**
     * Add a test case to the currently running test suite.
     * 
     * @param testCase the test case to add.
     */
    public void addTestCase(Testcase testCase) {
        assert !testSuites.isEmpty() : "No suites running";
        // add pending output to the newly created testcase
        testCase.addOutputLines(output);
        output.clear();
        getCurrentSuite().addTestcase(testCase);
    }

    /**
     * @return the suite that is currently running or <code>null</code> if 
     * no suite is running.
     */
    public synchronized TestSuite getCurrentSuite() {
        return testSuites.isEmpty() ? null : testSuites.get(suiteIdxs.isEmpty() ? testSuites.size() -1 : suiteIdxs.peek());
    }

    /**
     * Builds a report for the suite of this session.
     * 
     * @return
     */
    public Report getReport(long timeInMillis) {
        TestSuite currentSuite = getCurrentSuite();
        assert currentSuite != null : "Currently running suite was null for projectURI: " + projectURI; //NOI18N
        Report report = new Report(currentSuite.getName(), getProject());
        report.setElapsedTimeMillis(timeInMillis);
	boolean isTestNG = CommonUtils.getInstance().getTestingFramework().equals(CommonUtils.TESTNG_TF);
        for (Testcase testcase : currentSuite.getTestcases()) {
            report.reportTest(testcase);
            if (!isTestNGConfigMethod(testcase, isTestNG)) {
                report.setTotalTests(report.getTotalTests() + 1);
                if (testcase.getStatus() == Status.PASSED) {
                    report.setPassed(report.getPassed() + 1);
                } else if (testcase.getStatus() == Status.PASSEDWITHERRORS) {
                    report.setPassedWithErrors(report.getPassedWithErrors() + 1);
                } else if (testcase.getStatus() == Status.ERROR) {
                    report.setErrors(report.getErrors() + 1);
                } else if (testcase.getStatus() == Status.FAILED) {
                    report.setFailures(report.getFailures() + 1);
                } else if (testcase.getStatus() == Status.PENDING) {
                    report.setPending(report.getPending() + 1);
                } else if (testcase.getStatus() == Status.SKIPPED) {
                    report.setSkipped(report.getSkipped() + 1);
                    report.setSkipped(true);
                } else if (testcase.getStatus() == Status.ABORTED) {
                    report.setAborted(report.getAborted()+ 1);
                    report.setAborted(true);
                }
            }
        }
        return report;
    }

    private boolean isTestNGConfigMethod(Testcase testcase, boolean isTestNG) {
	return (isTestNG && (testcase.getName().startsWith("@AfterMethod ") || testcase.getName().startsWith("@BeforeMethod ") //NOI18N
		    || testcase.getName().startsWith("@AfterClass ") || testcase.getName().startsWith("@BeforeClass "))); //NOI18N
    }
    
    private SessionResult getUpdatedSessionStats() {
	boolean isTestNG = CommonUtils.getInstance().getTestingFramework().equals(CommonUtils.TESTNG_TF);
        long totalTime = 0;
        SessionResult result = new SessionResult();
        for (Testcase testcase : getAllTestCases()) {
            if(isTestNGConfigMethod(testcase, isTestNG)) {
                continue;
            }
            totalTime += testcase.getTimeMillis();
            if (testcase.getStatus() == Status.PASSED) {
                result.passed(1);
            } else if (testcase.getStatus() == Status.PASSEDWITHERRORS) {
                result.passedWithErrors(1);
            } else if (testcase.getStatus() == Status.ERROR) {
                result.errors(1);
            } else if (testcase.getStatus() == Status.FAILED) {
                result.failed(1);
            } else if (testcase.getStatus() == Status.PENDING) {
                result.pending(1);
            } else if (testcase.getStatus() == Status.SKIPPED) {
                result.skipped(1);
            } else if (testcase.getStatus() == Status.ABORTED) {
                result.aborted(1);
            }
        }
        result.elapsedTime(totalTime);
        return result;
    }

    /**
     * @return the type of this session.
     */
    public SessionType getSessionType() {
        return sessionType;
    }

    /**
     *
     * @return number of failures/errors incremented by one
     */
    public synchronized long incrementFailuresCount() {
        return ++failuresCount;
    }

    public FileLocator getFileLocator() {
        return fileLocator;
    }
    
    /**
     * @return the name of this session.
     * @see #name
     */
    public String getName() {
        return name.length() != 0 ? name : ProjectUtils.getInformation(getProject()).getDisplayName();
    }

    /**
     * @return the complete results for this session.
     */
    public SessionResult getSessionResult() {
        return getUpdatedSessionStats();
    }

    /**
     * The results for the whole session, i.e. the cumulative result 
     * of all reports that were generated for the session.
     */
    public static final class SessionResult {

        private int passed;
        private int passedWithErrors;
        private int failed;
        private int errors;
        private int pending;
        private int skipped;
        private int aborted;
        private long elapsedTime;
        
        private int failed(int failedCount) {
            return failed += failedCount;
        }

        private int errors(int errorCount) {
            return errors += errorCount;
        }

        private int passed(int passedCount) {
            return passed += passedCount;
        }

        private int passedWithErrors(int passedWithErrorsCount) {
            return passedWithErrors += passedWithErrorsCount;
        }

        private int pending(int pendingCount) {
            return pending += pendingCount;
        }

        private int skipped(int skippedCount) {
            return skipped += skippedCount;
        }

        private int aborted(int abortedCount) {
            return aborted += abortedCount;
        }

        private long elapsedTime(long time) {
            return elapsedTime += time;
        }

        public int getErrors() {
            return errors;
        }

        public int getFailed() {
            return failed;
        }

        public int getPassed() {
            return passed;
        }

        public int getPassedWithErrors() {
            return passedWithErrors;
        }

        public int getPending() {
            return pending;
        }

        public int getTotal() {
            return getPassed() + getPassedWithErrors() + getFailed() + getErrors() + getPending() + skipped + aborted;
        }

        public long getElapsedTime() {
            return elapsedTime;
        }
    }
}
