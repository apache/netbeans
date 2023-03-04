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
package org.netbeans.modules.php.project.ui.testrunner;

import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.php.spi.testing.run.TestCase;
import org.netbeans.modules.php.spi.testing.run.TestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

public class TestSuiteImpl implements TestSuite {

    private final TestSessionImpl testSession;
    private final org.netbeans.modules.gsf.testrunner.api.TestSuite testSuite;
    private final FileObject location;

    private volatile boolean finished = false;
    private volatile long totalTime = 0;


    TestSuiteImpl(TestSessionImpl testSession, org.netbeans.modules.gsf.testrunner.api.TestSuite testSuite, FileObject location) {
        assert testSession != null;
        assert testSuite != null;
        this.testSession = testSession;
        this.testSuite = testSuite;
        this.location = location;
    }

    @Override
    public TestCase addTestCase(String name, String type) {
        checkFrozen();
        checkFinished();
        Parameters.notWhitespace("name", name); // NOI18N
        Parameters.notWhitespace("type", type); // NOI18N
        TestSession session = testSession.getTestSession();
        Testcase testCase = new Testcase(name, type, session);
        if (location != null) {
            testCase.setLocation(FileUtil.toFile(location).getAbsolutePath());
        }
        session.addTestCase(testCase);
        updateReport(0, false);
        return new TestCaseImpl(this, testCase);
    }

    @Override
    public void finish(long time) {
        checkFrozen();
        checkFinished();
        finished = true;
        totalTime = time;
        updateReport(0, true);
    }

    public TestSessionImpl getTestSession() {
        return testSession;
    }

    public org.netbeans.modules.gsf.testrunner.api.TestSuite getTestSuite() {
        return testSuite;
    }

    public FileObject getLocation() {
        return location;
    }

    void checkFrozen() {
        testSession.checkFrozen();
    }

    private void checkFinished() {
        if (finished) {
            throw new IllegalStateException("Test suite " + testSuite.getName() + " is already finished");
        }
    }

    void updateReport(long time, boolean completed) {
        totalTime += time;
        TestSession session = testSession.getTestSession();
        Report report = testSession.getReport();
        report.update(session.getReport(totalTime));
        testSession.getManager().displayReport(session, report, completed);
    }

}
