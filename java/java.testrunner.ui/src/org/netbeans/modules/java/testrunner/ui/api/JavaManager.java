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
package org.netbeans.modules.java.testrunner.ui.api;

import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.CoreManager;

/**
 * Java common implementation of CoreManager. Merely delegates to {@link Manager}'s corresponding methods.
 *
 * @author Theofanis Oikonomou
 */
public class JavaManager extends CoreManager {

    @Override
    public void testStarted(TestSession session) {
        Manager.getInstance().testStarted(session);
    }

    @Override
    public void sessionFinished(TestSession session) {
        Manager.getInstance().sessionFinished(session);
    }

    @Override
    public void displayReport(TestSession session, Report report) {
        Manager.getInstance().displayReport(session, report);
    }

    @Override
    public void displayReport(TestSession session, Report report, boolean completed) {
        Manager.getInstance().displayReport(session, report, completed);
    }

    @Override
    public void displayOutput(TestSession session, String text, boolean error) {
        Manager.getInstance().displayOutput(session, text, error);
    }

    @Override
    public void displaySuiteRunning(TestSession session, TestSuite suite) {
        Manager.getInstance().displaySuiteRunning(session, suite);
    }

    @Override
    public void displaySuiteRunning(TestSession session, String suiteName) {
        Manager.getInstance().displaySuiteRunning(session, suiteName);
    }

}
