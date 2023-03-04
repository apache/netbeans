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
package org.netbeans.modules.gsf.testrunner.ui.api;

import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.ui.ResultDisplayHandler;
import org.openide.util.Lookup;

/**
 * API class for handlers displaying test results.
 *
 * @since 1.22
 * @author Dusan Balek
 */
public abstract class TestResultDisplayHandler {

    /**
     * Creates the {@link TestResultDisplayHandler} instance for the test session.
     *
     * @param session test session
     * @return {@link TestResultDisplayHandler} instance
     */
    public static final TestResultDisplayHandler create(TestSession session) {
        Spi provider = Lookup.getDefault().lookup(Spi.class);
        if (provider == null) {
            provider = ResultDisplayHandler.getProvider();
        }
        return new Impl<>(provider.create(session), provider);
    }

    private TestResultDisplayHandler() {
    }

    /**
     * Display output produced by running test.
     *
     * @param text output text
     * @param error mark the output text as error
     */
    public abstract void displayOutput(String text, boolean error);

    /**
     * Display information that a test suite is running.
     *
     * @param suiteName name of the running suite; or {@code null} in the case
     *                  of anonymous suite
     */
    public abstract void displaySuiteRunning(String suiteName);

    /**
     * Display information that a test suite is running.
     *
     * @param suite the running suite
     */
    public abstract void displaySuiteRunning(TestSuite suite);

    /**
     * Display test results.
     *
     * @param report summary report to display
     */
    public abstract void displayReport(Report report);

    /**
     * Display message produced by running test.
     *
     * @param message message to display
     */
    public abstract void displayMessage(String message);

    /**
     * Display information that a test session has finished.
     *
     * @param message message to display
     */
    public abstract void displayMessageSessionFinished(String message);

    /**
     * Return total number of tests in session if known.
     *
     * @return number of tests
     */
    public abstract int getTotalTests();

    /**
     * Interface providing SPI for {@link TestResultDisplayHandler}s.
     * Instances should be registered in the default lookup.
     */
    public static interface Spi<T> {

        /**
         * Creates {@link Spi} instance for the test session.
         * @param session test session
         * @return {@link Spi} instance
         */
        public T create(TestSession session);

        /**
         * Display output produced by running test.
         *
         * @param text output text
         * @param error mark the output text as error
         */
        public void displayOutput(T token, String text, boolean error);

            /**
         * Display information that a test suite is running.
         *
         * @param suiteName name of the running suite; or {@code null} in the case
         *                  of anonymous suite
         */
        public void displaySuiteRunning(T token, String suiteName);

        /**
         * Display information that a test suite is running.
         *
         * @param suite the running suite
         */
        public void displaySuiteRunning(T token, TestSuite suite);

        /**
         * Display test results.
         *
         * @param report summary report to display
         */
        public void displayReport(T token, Report report);

        /**
         * Display message produced by running test.
         *
         * @param message message to display
         */
        public void displayMessage(T token, String message);

        /**
         * Display information that a test session has finished.
         *
         * @param message message to display
         */
        public void displayMessageSessionFinished(T token, String message);

        /**
         * Return total number of tests in session if known.
         *
         * @return number of tests
         */
        public int getTotalTests(T token);
    }

    private static final class Impl<T> extends TestResultDisplayHandler {

        private final T token;
        private final Spi<T> spi;

        private Impl(T token, Spi<T> spi) {
            this.token = token;
            this.spi = spi;
        }

        public void displayOutput(String text, boolean error) {
            spi.displayOutput(token, text, error);
        }

        @Override
        public void displaySuiteRunning(String suiteName) {
            spi.displaySuiteRunning(token, suiteName);
        }

        @Override
        public void displaySuiteRunning(TestSuite suite) {
            spi.displaySuiteRunning(token, suite);
        }

        @Override
        public void displayReport(Report report) {
            spi.displayReport(token, report);
        }

        @Override
        public void displayMessage(String message) {
            spi.displayMessage(token, message);
        }

        @Override
        public void displayMessageSessionFinished(String message) {
            spi.displayMessageSessionFinished(token, message);
        }

        @Override
        public int getTotalTests() {
            return spi.getTotalTests(token);
        }

        @Override
        T getToken() {
            return token;
        }
    }

    abstract Object getToken();
}
