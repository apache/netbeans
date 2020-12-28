/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.testrunner.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.cnd.testrunner.spi.TestRecognizerHandler;
import org.netbeans.modules.cnd.testrunner.spi.TestHandlerFactory;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.openide.util.NbBundle;

/**
 * An output recognizer for parsing output of the PyUnit test runner
 * script, <code>nb_test_runner.py</code>. Updates the test result UI.
 *
 * Closely based on the TestUnitHandlerFactory for Ruby by Erno.
 *
 * @author Erno Mononen
 * @author Tor Norbye
 */
public class CndUnitHandlerFactory implements TestHandlerFactory {

    private static final Logger LOGGER = Logger.getLogger(CndUnitHandlerFactory.class.getName());

    private static String CPP_UNIT = "Cpp Unit Test"; // NOI18N

    @Override
    public boolean printSummary() {
        return true;
    }

    @Override
    public List<TestRecognizerHandler> createHandlers() {
        List<TestRecognizerHandler> result = new ArrayList<TestRecognizerHandler>();

        // Simple
        result.add(new SimpleSuiteStartingHandler());
        result.add(new SimpleSuiteStartedHandler());
        result.add(new SimpleSuiteFinishedHandler());
        result.add(new SimpleSuiteErrorOutputHandler());
        result.add(new SimpleTestStartedHandler());
        result.add(new SimpleTestFailedHandler());
        result.add(new SimpleTestErrorHandler());
        result.add(new SimpleTestFinishedHandler());
        result.add(new SimpleTestLoggerHandler());
        result.add(new SimpleTestMiscHandler());
        result.add(new SimpleSuiteMiscHandler());

        return result;
    }

    private static String errorMsg(long failureCount) {
        return NbBundle.getMessage(CndUnitHandlerFactory.class, "MSG_Error", failureCount); // NOI18N
    }

    private static String failureMsg(long failureCount) {
        return NbBundle.getMessage(CndUnitHandlerFactory.class, "MSG_Failure", failureCount); // NOI18N
    }

    static String[] getStackTrace(String message, String stackTrace) {
        List<String> stackTraceList = new ArrayList<String>();
        stackTraceList.add(message);
        for (String location : stackTrace.split("%BR%")) { //NOI18N
//            if (/*!location.contains(CndUnitRunner.MEDIATOR_SCRIPT_NAME) &&*/ !location.contains(CndUnitRunner.RUNNER_SCRIPT_NAME)) { //NOI18N
//                stackTraceList.add(location);
//            }
        }
        return stackTraceList.toArray(new String[stackTraceList.size()]);
    }

    // Doctest message defined in nb_test_runner.py, the other one is from unittest
    private static final Pattern STRING_COMPARISON = Pattern.compile("(Expected (.+) but got (.+))|((.+) != (.+))", Pattern.DOTALL); // NOI18N

    // Package private for tests
    static Trouble.ComparisonFailure getComparisonFailure(String msg) {
        Matcher comparisonMatcher = STRING_COMPARISON.matcher(msg);
        if (!comparisonMatcher.matches()) {
            return null;
        }
        String expected;
        String actual;

        boolean isDocTest = false;
        expected = comparisonMatcher.group(2);
        if (expected == null) {
            expected = comparisonMatcher.group(5);
            actual = comparisonMatcher.group(6);
        } else {
            isDocTest = true;
            actual = comparisonMatcher.group(3);
        }

        // Convert back to multiline strings, if applicable
        expected = expected.replace("\\n", "\n"); // NOI18N
        actual = actual.replace("\\n", "\n"); // NOI18N

//        if (isDocTest) {
//            // We know the doc test output is in python console format which generally
//            // can be highlighted by the python lexer (for string literals, etc.)
//            return new Trouble.ComparisonFailure(expected, actual, CndTokenId.PYTHON_MIME_TYPE);
//        } else {
            return new Trouble.ComparisonFailure(expected, actual);
//        }
    }


    static class SimpleTestFailedHandler extends TestRecognizerHandler {
        private List<String> output;

        public SimpleTestFailedHandler(String regex) {
            super(regex);
        }

        public SimpleTestFailedHandler() {
            super("%TEST_FAILED%\\stime=([0-9]+).*\\stestname=(.+) \\((.+)\\)\\smessage=(.*)"); //NOI18N
        }

        @Override
        public void updateUI( Manager manager, TestSession session) {
            Testcase testcase = new Testcase(getMatcher().group(2), CPP_UNIT, session);
            testcase.setTimeMillis(toMillis(getMatcher().group(1)));
            testcase.setClassName(getMatcher().group(3));
            testcase.setTrouble(new Trouble(false));
            String message = getMatcher().group(4).replace("%BR%", "\n"); // NOI18N
            testcase.getTrouble().setStackTrace(getStackTrace(message, "")); // NOI18N
            testcase.getTrouble().setComparisonFailure(getComparisonFailure(message));

            session.addTestCase(testcase);

            String failureMsg = failureMsg(session.incrementFailuresCount());
            String testCase = testcase.getName() + "(" + testcase.getClassName() + "):"; // NOI18N
            output = new ArrayList<String>();
            output.add("");
            output.add(failureMsg);
            output.add(testCase);
            output.addAll(Arrays.asList(testcase.getTrouble().getStackTrace()));
            output.add("");
            
            manager.displayOutput(session, "", false);
            manager.displayOutput(session, failureMsg, false);
            manager.displayOutput(session, testCase, false); //NOI18N
            for (String line : testcase.getTrouble().getStackTrace()) {
                manager.displayOutput(session, line, false);
            }
            manager.displayOutput(session, "", false);
            testcase.addOutputLines(output);
        }

        @Override
        public List<String> getRecognizedOutput() {
            return new ArrayList<String>(output);
        }
    }

    static class SimpleTestErrorHandler extends TestRecognizerHandler {

        private List<String> output;

        public SimpleTestErrorHandler() {
            super("%TEST_ERROR%\\stime=([0-9]+).*\\stestname=(.+) \\((.+)\\)\\smessage=(.*)"); //NOI18N
        }

        @Override
        public void updateUI( Manager manager, TestSession session) {
            Testcase testcase = new Testcase(getMatcher().group(2), CPP_UNIT, session);
            testcase.setTimeMillis(toMillis(getMatcher().group(1)));
            testcase.setClassName(getMatcher().group(3));
            testcase.setTrouble(new Trouble(true));
            testcase.getTrouble().setStackTrace(getStackTrace(getMatcher().group(4).replace("%BR%", "\n"), "")); // NOI18N
            session.addTestCase(testcase);

            String errorMsg = errorMsg(session.incrementFailuresCount());
            String testCase = testcase.getName() + "(" + testcase.getClassName() + "):"; // NOI18N
            output = new ArrayList<String>();
            output.add("");
            output.add(errorMsg);
            output.add(testCase);
            output.addAll(Arrays.asList(testcase.getTrouble().getStackTrace()));
            output.add("");

            manager.displayOutput(session, "", false);
            manager.displayOutput(session, errorMsg, false);
            manager.displayOutput(session, testCase, false); //NOI18N
            for (String line : testcase.getTrouble().getStackTrace()) {
                manager.displayOutput(session, line, true);
            }
            manager.displayOutput(session, "", false);
            testcase.addOutputLines(output);
        }

        @Override
        public List<String> getRecognizedOutput() {
            return new ArrayList<String>(output);
        }
    }

    static class SimpleTestStartedHandler extends TestRecognizerHandler {

        public SimpleTestStartedHandler() {
            super("%TEST_STARTED%\\s*(.+) \\((.+)\\)"); //NOI18N
        }

        @Override
        public void updateUI( Manager manager, TestSession session) {
        }
    }

    static class SimpleTestFinishedHandler extends TestRecognizerHandler {

        public SimpleTestFinishedHandler(String regex) {
            super(regex);
        }

        public SimpleTestFinishedHandler() {
            super("%TEST_FINISHED%\\stime=([0-9]+).*\\s+(.+) \\((.+)\\)"); //NOI18N
        }

        @Override
        public void updateUI( Manager manager, TestSession session) {
            Testcase testcase = new Testcase(getMatcher().group(2), CPP_UNIT, session);
            if(!(session.getCurrentTestCase() != null && session.getCurrentTestCase().getName().equals(testcase.getName()) &&
                    session.getCurrentTestCase().getTrouble() != null)) {
                testcase.setTimeMillis(toMillis(getMatcher().group(1)));
                testcase.setClassName(getMatcher().group(3));
                session.addTestCase(testcase);
            }
        }
    }

    /**
     * Captures the rest of %TEST_* patterns that are not handled
     * otherwise (yet).
     */
    static class SimpleTestMiscHandler extends TestRecognizerHandler {

        public SimpleTestMiscHandler() {
            super("%TEST_.*"); //NOI18N
        }

        @Override
        public void updateUI( Manager manager, TestSession session) {
        }
    }

    static class SimpleSuiteFinishedHandler extends TestRecognizerHandler {

        public SimpleSuiteFinishedHandler() {
            super("%SUITE_FINISHED%\\s+time=(.+)"); //NOI18N
        }

        @Override
        public void updateUI( Manager manager, TestSession session) {
            manager.displayReport(session, session.getReport(toMillis(getMatcher().group(1))));
            manager.sessionFinished(session);
        }
    }

    static class SimpleSuiteStartedHandler extends TestRecognizerHandler {

        public SimpleSuiteStartedHandler() {
            super("%SUITE_STARTED%\\s.*"); //NOI18N
        }

        @Override
        public void updateUI( Manager manager, TestSession session) {
        }
    }

    static class SimpleSuiteErrorOutputHandler extends TestRecognizerHandler {

        public SimpleSuiteErrorOutputHandler() {
            super("%SUITE_ERROR_OUTPUT%\\serror=(.*)"); //NOI18N
        }

        @Override
        public void updateUI( Manager manager, TestSession session) {
            manager.displayOutput(session, getMatcher().group(1), true);
            manager.displayOutput(session, "", false);
        }

        @Override
        public List<String> getRecognizedOutput() {
            return Collections.<String>singletonList(getMatcher().group(1));
        }

    }

    static class SimpleSuiteStartingHandler extends TestRecognizerHandler {

        private boolean firstSuite = true;

        public SimpleSuiteStartingHandler() {
            super("%SUITE_STARTING%\\s+(.+)"); //NOI18N
        }

        @Override
        public void updateUI( Manager manager, TestSession session) {
            if (firstSuite) {
                firstSuite = false;
                manager.testStarted(session);
            }
            String suiteName = getMatcher().group(1);
            session.addSuite(new TestSuite(suiteName));
            manager.displaySuiteRunning(session, suiteName);
        }
    }

    /**
     * Captures the rest of %SUITE_* patterns that are not handled
     * otherwise (yet).
     */
    static class SimpleSuiteMiscHandler extends TestRecognizerHandler {

        public SimpleSuiteMiscHandler() {
            super("%SUITE_.*"); //NOI18N
        }

        @Override
        public void updateUI( Manager manager, TestSession session) {
        }
    }

    /**
     * Captures output meant for logging.
     */
    static class SimpleTestLoggerHandler extends TestRecognizerHandler {

        public SimpleTestLoggerHandler() {
            super("%TEST_LOGGER%\\slevel=(.+)\\smsg=(.*)"); //NOI18N
        }

        @Override
        public void updateUI( Manager manager, TestSession session) {
            Level level = Level.parse(getMatcher().group(1));
            if (LOGGER.isLoggable(level))
                LOGGER.log(level, getMatcher().group(2));
        }
    }

}
