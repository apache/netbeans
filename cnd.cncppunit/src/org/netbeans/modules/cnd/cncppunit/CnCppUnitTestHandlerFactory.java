/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
