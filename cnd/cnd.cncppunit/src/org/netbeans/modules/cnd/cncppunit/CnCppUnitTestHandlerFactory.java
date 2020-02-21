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

package org.netbeans.modules.cnd.cncppunit;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.netbeans.modules.cnd.testrunner.spi.TestHandlerFactory;
import org.netbeans.modules.cnd.testrunner.spi.TestRecognizerHandler;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;

/**
 * Sample factory.
 *
 */
public class CnCppUnitTestHandlerFactory implements TestHandlerFactory {

    private static String C_UNIT = "C Unit Test"; // NOI18N
    private static String CPP_UNIT = "Cpp Unit Test"; // NOI18N
    private static boolean firstSuite = true;

    @Override
    public List<TestRecognizerHandler> createHandlers() {
        List<TestRecognizerHandler> result = new ArrayList<TestRecognizerHandler>();
        final CppUnitHandler cppUnitHandler = new CppUnitHandler();

        // CppUnit
        result.add(cppUnitHandler.new CppUnitTestFinishedHandler());
        result.add(cppUnitHandler.new CppUnitTestFailedHandler());
        result.add(cppUnitHandler.new CppUnitSuiteFinishedHandler());
        // CUnit
        result.add(new CUnitSuiteStartingHandler());
        result.add(new CUnitSuiteFinishedHandler());
        result.add(new CUnitTestFinishedHandler());
        result.add(new CUnitTestFailedHandler());

        return result;
    }

    public boolean printSummary() {
        return true;
    }

    //
    // CUnit tests output support
    //

    static class CUnitSuiteStartingHandler extends TestRecognizerHandler {

        public CUnitSuiteStartingHandler() {
            super("Suite: (.+)"); //NOI18N
        }

        @Override
        public void updateUI(Manager manager, TestSession session) {
            if (firstSuite) {
                firstSuite = false;
                manager.testStarted(session);
            } else {
                manager.displayReport(session, session.getReport(0));
            }
            String suiteName = getMatcher().group(1);
            session.addSuite(new TestSuite(suiteName));
            manager.displaySuiteRunning(session, suiteName);
        }
    }

    static class CUnitTestFinishedHandler extends MultilineOutputHandler {

        public CUnitTestFinishedHandler() {
            super(" *Test: (?<test>.*)\\.\\.\\.(.*)", "(.*)passed"); //NOI18N
        }

        @Override
        public void updateUI(Manager manager, TestSession session) {
            Testcase testcase = new Testcase(getMatcher().group("test"), C_UNIT, session); //NOI18N
            if(!(session.getCurrentTestCase() != null && session.getCurrentTestCase().getName().equals(testcase.getName()) &&
                    session.getCurrentTestCase().getTrouble() != null)) {
                testcase.setTimeMillis(0);
                testcase.setClassName(session.getCurrentSuite().getName());
                session.addTestCase(testcase);
            }
        }
    }

    static class CUnitTestFailedHandler extends MultilineOutputHandler {

        public CUnitTestFailedHandler() {
            super(" *Test: (?<test>.*)\\.\\.\\.(.*)", "(.*)FAILED"); //NOI18N
        }

        @Override
        public void updateUI(Manager manager, TestSession session) {
            Testcase testcase = new Testcase(getMatcher().group("test"), C_UNIT, session); //NOI18N
            testcase.setTimeMillis(0);
            testcase.setClassName(session.getCurrentSuite().getName());

            testcase.setTrouble(new Trouble(true));

            session.addTestCase(testcase);
        }
    }

    static class CUnitSuiteFinishedHandler extends TestRecognizerHandler {

        public CUnitSuiteFinishedHandler() {
            super("(--)?Run Summary: "); //NOI18N
        }

        @Override
        public void updateUI(Manager manager, TestSession session) {
            manager.displayReport(session, session.getReport(0));
            manager.sessionFinished(session);
            firstSuite = true;
        }
    }

    //
    // CppUnit tests output support
    //

    static class CppUnitHandler {

        private String currentSuiteName;
        private boolean currentSuiteFinished = false;

        public CppUnitHandler() {
        }
            
        class CppUnitTestFinishedHandler extends MultilineOutputHandler {

            public CppUnitTestFinishedHandler() {
                super("(?<suite>.*)::(?<test>.+)", ".* : OK"); //NOI18N
            }

            @Override
            public void updateUI(Manager manager, TestSession session) {

                String suiteName = getMatcher().group("suite"); //NOI18N

                final TestSuite currentSuite = session.getCurrentSuite();
                if (currentSuite == null) {
                    manager.testStarted(session);
                    session.addSuite(new TestSuite(suiteName));
                    manager.displaySuiteRunning(session, suiteName);
                    currentSuiteFinished = false;
                } else if(!currentSuite.getName().equals(suiteName)) {
                    if(currentSuite.getName().equals(currentSuiteName) &&
                            !currentSuiteFinished) {
                        manager.displayReport(session, session.getReport(0));
                    }

                    session.addSuite(new TestSuite(suiteName));
                    manager.displaySuiteRunning(session, suiteName);
                    currentSuiteFinished = false;
                }
                currentSuiteName = suiteName;

                Testcase testcase = new Testcase(getMatcher().group("test"), CPP_UNIT, session); //NOI18N
                if(!(session.getCurrentTestCase() != null && session.getCurrentTestCase().getName().equals(testcase.getName()) &&
                        session.getCurrentTestCase().getTrouble() != null)) {
                    testcase.setTimeMillis(0);
                    testcase.setClassName(suiteName);
                    session.addTestCase(testcase);
                }
            }
        }

        class CppUnitTestFailedHandler extends MultilineOutputHandler {

            public CppUnitTestFailedHandler() {
                super("(?<suite>.*)::(?<test>.+).*", ".* : (?<reason>.*)"); //NOI18N
    }

            @Override
            public void updateUI( Manager manager, TestSession session) {

                String suiteName = getMatcher().group("suite"); //NOI18N

                final TestSuite currentSuite = session.getCurrentSuite();
                if (currentSuite == null) {
                    manager.testStarted(session);
                    session.addSuite(new TestSuite(suiteName));
                    manager.displaySuiteRunning(session, suiteName);
                } else if(!currentSuite.getName().equals(suiteName)) {
                    if(currentSuite.getName().equals(currentSuiteName) &&
                            !currentSuiteFinished) {
                        manager.displayReport(session, session.getReport(0));
                        currentSuiteFinished = true;
                    }

                    session.addSuite(new TestSuite(suiteName));
                    manager.displaySuiteRunning(session, suiteName);
                }
                currentSuiteName = suiteName;

                Testcase testcase = new Testcase(getMatcher().group("test"), CPP_UNIT, session); //NOI18N
                testcase.setTimeMillis(0);
                testcase.setClassName(suiteName);

                testcase.setTrouble(new Trouble(true));
                String message = getMatcher().group("reason"); // NOI18N
                testcase.getTrouble().setStackTrace(getStackTrace(message ,"")); // NOI18N

                session.addTestCase(testcase);
            }
        }

        class CppUnitSuiteFinishedHandler extends TestRecognizerHandler {

            public CppUnitSuiteFinishedHandler() {
                super(".*((Run: )|(OK \\()).*"); //NOI18N
            }

            @Override
            public void updateUI( Manager manager, TestSession session) {
                manager.displayReport(session, session.getReport(0));
                manager.sessionFinished(session);
                currentSuiteFinished = true;
            }
        }
    }

    static String[] getStackTrace(String message, String stackTrace) {
        List<String> stackTraceList = new ArrayList<String>();
        stackTraceList.add(message);
        return stackTraceList.toArray(new String[stackTraceList.size()]);
    }

}
