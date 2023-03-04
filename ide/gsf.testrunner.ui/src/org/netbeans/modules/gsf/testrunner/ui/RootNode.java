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
package org.netbeans.modules.gsf.testrunner.ui;

import org.netbeans.modules.gsf.testrunner.ui.api.TestsuiteNode;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import static org.netbeans.modules.gsf.testrunner.ui.Bundle.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Marian Petras, Erno Mononen
 */
final class RootNode extends AbstractNode {


    /** constant meaning "information about passed tests not displayed" */
    static final int ALL_PASSED_ABSENT = 0;
    /** constant meaning "information about some passed tests not displayed" */
    static final int SOME_PASSED_ABSENT = 1;
    /** constant meaning "information about all passed tests displayed */
    static final int ALL_PASSED_DISPLAYED = 2;
    /**
     */
    private RootNodeChildren children;
    /**
     */
    private volatile int filterMask;
    /** */
    private volatile String message;
    private volatile int totalTests = 0;
    private volatile int failures = 0;
    private volatile int errors = 0;
    private volatile int pending = 0;
    private volatile int skipped = 0;
    private volatile int aborted = 0;
    private volatile long elapsedTimeMillis = 0;
    private volatile int detectedPassedTests = 0;
    private boolean sessionFinished;
    private final TestSession session;

    private static final Logger LOGGER = Logger.getLogger(RootNode.class.getName());

    /**
     * Creates a new instance of RootNode
     */
    @Messages("MSG_RunningTests=Running tests, please wait...")
    RootNode(TestSession session, int filterMask) {
        super(Children.LEAF);
        this.session = session;
        this.filterMask = filterMask;
        setName(MSG_RunningTests());

        setIconBaseWithExtension(
                "org/netbeans/modules/gsf/testrunner/resources/empty.gif");     //NOI18N

    }
    
    private RootNodeChildren getRootNodeChildren() {
        if (children == null) {
            children = new RootNodeChildren(session, filterMask);
            setChildren(Children.create(children, true));
        }
        return children;
    }

    int getTotalTests() {
        return totalTests;
    }

    /**
     */
    void displayMessage(final String msg) {
        assert EventQueue.isDispatchThread();

        /* Called from the EventDispatch thread */

        this.message = msg;
        updateDisplayName();
    }

    /**
     * Updates the display when the session is finished.
     * 
     * @param  msg  optional message to be displayed (e.g. notice that
     *              the sessions has been interrupted); or {@code null}
     */
    void displayMessageSessionFinished(final String msg) {
        sessionFinished = true;
        displayMessage(msg);
        getRootNodeChildren().notifyTestSuiteFinished();
    }

    /**
     * Displays a message that a given test suite is running.
     *
     * @param  suiteName  name of the running test suite,
     *                    or {@code ANONYMOUS_SUITE} for anonymous suites
     *
     * @see  ResultDisplayHandler#ANONYMOUS_SUITE
     */
    void displaySuiteRunning(final String suiteName) {
        assert EventQueue.isDispatchThread();

        /* Called from the EventDispatch thread */
        
        getRootNodeChildren().displaySuiteRunning(suiteName);
    }

    /**
     * Displays a message that a given test suite is running.
     *
     * @param  suite  running test suite,
     *                    or {@code ANONYMOUS_TEST_SUITE} for anonymous suites
     *
     * @see  ResultDisplayHandler#ANONYMOUS_TEST_SUITE
     */
    void displaySuiteRunning(final TestSuite suite) {
        assert EventQueue.isDispatchThread();

        /* Called from the EventDispatch thread */

        getRootNodeChildren().displaySuiteRunning(suite);
    }

    /**
     */
    public TestsuiteNode displayReport(final Report report) {
        assert EventQueue.isDispatchThread();

        /* Called from the EventDispatch thread */

        TestsuiteNode suiteNode = getRootNodeChildren().displayReport(report);
        updateStatistics();
        updateDisplayName();
        return suiteNode;
    }

    /**
     */
    void displayReports(final Collection<Report> reports) {
        assert EventQueue.isDispatchThread();

        /* Called from the EventDispatch thread */

        getRootNodeChildren().displayReports(reports);
        updateStatistics();
        updateDisplayName();
    }

    /**
     */
    private synchronized void updateStatistics() {
        totalTests = 0;
        failures = 0;
        errors = 0;
        pending = 0;
	skipped = 0;
	aborted = 0;
        detectedPassedTests = 0;
        elapsedTimeMillis = 0;
        for(Report rep: getRootNodeChildren().getReports()){
            totalTests += rep.getTotalTests();
            failures += rep.getFailures();
            errors += rep.getErrors();
            pending += rep.getPending();
	    skipped += rep.getSkipped();
	    aborted += rep.getAborted();
            detectedPassedTests += rep.getDetectedPassedTests();
            elapsedTimeMillis += rep.getElapsedTimeMillis();
        }
    }

    float getPassedPercentage() {
        return (float) detectedPassedTests / totalTests * 100;
    }
    
    float getSkippedPercentage() {
        return (float) skipped / totalTests * 100;
    }

    float getAbortedPercentage() {
        return (float) aborted / totalTests * 100;
    }

    /**
     */
    void setFilterMask(final int filterMask) {
        assert EventQueue.isDispatchThread();

        if (filterMask == this.filterMask) {
            return;
        }
        this.filterMask = filterMask;

        getRootNodeChildren().setFilterMask(filterMask);
        }

    @Messages({
        "MSG_TestsInfoNoTests=No tests executed.",
        "# e.g.: 'All 58 tests passed.'", "# {0} - number of tests", "MSG_TestsInfoAllOK={0,choice,1#The test|2#Both tests|2<All {0,number,integer} tests} passed.",
        "# {0} - number of tests", "MSG_PassedTestsInfo={0,choice,0#No test|1#1 test|1<{0,number,integer} tests} passed",
        "# {0} - number of tests", "MSG_PendingTestsInfo={0,choice,1#1 test|1<{0,number,integer} tests} pending",
        "# {0} - number of tests", "MSG_FailedTestsInfo={0,choice,1#1 test|1<{0,number,integer} tests} failed",
        "# {0} - number of tests", "MSG_ErrorTestsInfo={0,choice,1#1 test|1<{0,number,integer} tests} caused an error",
        "# {0} - number of tests", "MSG_SkippedTestsInfo={0,choice,1#1 test|1<{0,number,integer} tests} skipped",
        "# {0} - number of tests", "MSG_AbortedTestsInfo={0,choice,1#1 test|1<{0,number,integer} tests} aborted",
        "MSG_SomePassedNotDisplayed=Information about some passed tests is not displayed.",
        "MSG_PassedNotDisplayed=Information about passed tests is not displayed.",
        "# Elapsed time for a test suite", "# {0} - number of tests", "MSG_TestSuiteElapsedTime=({0,number,0.0##} s)"
    })
    private void updateDisplayName() {
        assert EventQueue.isDispatchThread();

        /* Called from the EventDispatch thread */

        String msg;

        if (totalTests == 0) {
            if (sessionFinished) {
                msg = MSG_TestsInfoNoTests();
            } else {
                msg = null;
            }
        } else if (failures == 0 && errors == 0 && pending == 0 && skipped == 0 && aborted == 0) {
            msg = MSG_TestsInfoAllOK(totalTests);
        } else {
            
            String passedTestsInfo = MSG_PassedTestsInfo(totalTests - failures - errors - pending - skipped - aborted);
            
            String pendingTestsInfo = (pending == 0)
                    ? null
                    : MSG_PendingTestsInfo(errors);

            String failedTestsInfo = (failures == 0)
                    ? null
                    : MSG_FailedTestsInfo(failures);
            String errorTestsInfo = (errors == 0)
                    ? null
                    : MSG_ErrorTestsInfo(errors);
            String skippedTestsInfo = (skipped == 0)
                    ? null
                    : MSG_SkippedTestsInfo(skipped);
            String abortedTestsInfo = (aborted == 0)
                    ? null
                    : MSG_AbortedTestsInfo(aborted);
            
            msg = constructMessage(passedTestsInfo, pendingTestsInfo, failedTestsInfo, errorTestsInfo, skippedTestsInfo, abortedTestsInfo);
            
        }

        if (totalTests != 0) {
            assert msg != null;
            final int successDisplayedLevel = getSuccessDisplayedLevel();
            switch (successDisplayedLevel) {
                case SOME_PASSED_ABSENT:
                    msg += ' ';
                    msg += MSG_SomePassedNotDisplayed();
                    break;
                case ALL_PASSED_ABSENT:
                    msg += ' ';
                    msg += MSG_PassedNotDisplayed();
                    break;
                case ALL_PASSED_DISPLAYED:
                    break;
                default:
                    assert false;
                    break;
            }
        }

        if (msg != null) {
            msg += ' ' + MSG_TestSuiteElapsedTime(elapsedTimeMillis / 1000d);
        }

        if (this.message != null) {
            if (msg == null) {
                msg = this.message;
            } else {
                msg = msg + ' ' + message;
            }
        }

        // #143508
        LOGGER.log(Level.FINE, "Setting display name to: ''{0}''. Total tests run: {1}. Session finished: {2}", new Object[] {msg, totalTests, sessionFinished});

        setDisplayName(msg);
    }
    
    @Messages({
        "# {0} - info about tests in one state", "# {1} - info about tests in another state", "MSG_TestResultSummary1={0}, {1}.",
        "# {0} - info about tests in one state", "# {1} - info about tests in another state", "# {2} - info about tests in yet another state", "MSG_TestResultSummary2={0}, {1}, {2}.",
        "# {0} - info about passed tests", "# {1} - info about pending tests", "# {2} - info about failed tests", "# {3} - info about erroneous tests", "MSG_TestResultSummary3={0}, {1}, {2}, {3}.",
        "# {0} - info about passed tests", "# {1} - info about pending tests", "# {2} - info about failed tests", "# {3} - info about erroneous tests", "# {4} - info about skipped tests", "MSG_TestResultSummary4={0}, {1}, {2}, {3}, {4}.",
        "# {0} - info about passed tests", "# {1} - info about pending tests", "# {2} - info about failed tests", "# {3} - info about erroneous tests", "# {4} - info about skipped tests", "# {5} - info about aborted tests", "MSG_TestResultSummary5={0}, {1}, {2}, {3}, {4}, {5}."
    })
    String constructMessage(String... subMessages) {
        List<String> messageList = new ArrayList<String>();
        for (String msg : subMessages) {
            if (msg != null) {
                messageList.add(msg);
            }
        }
        int size = messageList.size();
        switch (size) {
        case 2:
            return MSG_TestResultSummary1(messageList.get(0), messageList.get(1));
        case 3:
            return MSG_TestResultSummary2(messageList.get(0), messageList.get(1), messageList.get(2));
        case 4:
            return MSG_TestResultSummary3(messageList.get(0), messageList.get(1), messageList.get(2), messageList.get(3));
        case 5:
            return MSG_TestResultSummary4(messageList.get(0), messageList.get(1), messageList.get(2), messageList.get(3), messageList.get(4));
        case 6:
            return MSG_TestResultSummary5(messageList.get(0), messageList.get(1), messageList.get(2), messageList.get(3), messageList.get(4), messageList.get(5));
        default:
            throw new AssertionError(messageList);
        }
    }

    /**
     * Returns information whether information about passed tests is displayed.
     *
     * @return  one of constants <code>ALL_PASSED_DISPLAYED</code>,
     *                           <code>SOME_PASSED_ABSENT</code>,
     *                           <code>ALL_PASSED_ABSENT</code>
     */
    int getSuccessDisplayedLevel() {
        int reportedPassedTestsCount = totalTests - failures - errors - skipped - aborted;
        if (detectedPassedTests >= reportedPassedTestsCount) {
            return ALL_PASSED_DISPLAYED;
        } else if (detectedPassedTests == 0) {
            return ALL_PASSED_ABSENT;
        } else {
            return SOME_PASSED_ABSENT;
        }
    }

    @Override
    public SystemAction[] getActions(boolean context) {
        return new SystemAction[0];
    }
}
