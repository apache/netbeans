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
package org.netbeans.modules.gsf.testrunner.ui.api;

import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.ui.ResultDisplayHandler;
import org.openide.util.Lookup;

/**
 * Common base class for handlers displaying test results.
 *
 * @since 1.22
 * @author Dusan Balek
 */
public abstract class TestResultDisplayHandler {

    /**
     * Get the {@link TestResultDisplayHandler} for the test session.
     *
     * @param session test session
     * @return {@link TestResultDisplayHandler} instance
     */
    public static final TestResultDisplayHandler get(TestSession session) {
        TestResultDisplayHandler.Provider provider = Lookup.getDefault().lookup(TestResultDisplayHandler.Provider.class);
        if (provider != null) {
            return provider.create(session);
        }
        return new ResultDisplayHandler(session);
    }

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
     * Interface providing factory method for creating {@link TestResultDisplayHandler}s.
     * Instances should be registered in the default lookup.
     */
    public static interface Provider {
        TestResultDisplayHandler create(TestSession session);
    }
}
