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

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.netbeans.modules.php.spi.testing.coverage.Coverage;
import org.netbeans.modules.php.spi.testing.run.OutputLineHandler;
import org.netbeans.modules.php.spi.testing.run.TestSession;
import org.netbeans.modules.php.spi.testing.run.TestSuite;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.windows.OutputWriter;

public class TestSessionImpl implements TestSession {

    private final Manager manager;
    private final org.netbeans.modules.gsf.testrunner.api.TestSession testSession;
    private final PhpTestingProvider testingProvider;

    private volatile Coverage coverage;
    private volatile boolean coverageSet = false;
    private volatile boolean frozen = false;
    private volatile boolean testException = false;
    private volatile Report report;


    TestSessionImpl(Manager manager, org.netbeans.modules.gsf.testrunner.api.TestSession testSession, PhpTestingProvider testingProvider) {
        assert manager != null;
        assert testSession != null;
        assert testingProvider != null;
        this.manager = manager;
        this.testSession = testSession;
        this.testingProvider = testingProvider;
    }

    @NbBundle.Messages({
        "# {0} - provider name",
        "# {1} - suite name",
        "TestSessionImpl.suite.name=[{0}] {1}",
    })
    @Override
    public TestSuite addTestSuite(String name, FileObject location) {
        Parameters.notWhitespace("name", name); // NOI18N
        checkFrozen();
        String suiteName = Bundle.TestSessionImpl_suite_name(testingProvider.getDisplayName(), name);
        org.netbeans.modules.gsf.testrunner.api.TestSuite testSuite = new org.netbeans.modules.gsf.testrunner.api.TestSuite(suiteName);
        testSession.addSuite(testSuite);
        report = testSession.getReport(0);
        manager.displaySuiteRunning(testSession, testSuite);
        return new TestSuiteImpl(this, testSuite, location);
    }

    @Override
    public void setOutputLineHandler(OutputLineHandler outputLineHandler) {
        Parameters.notNull("outputLineHandler", outputLineHandler); // NOI18N
        Manager.getInstance().setOutputLineHandler(map(outputLineHandler));
    }

    @Override
    public void printMessage(String message, boolean error) {
        Parameters.notNull("message", message); // NOI18N
        manager.displayOutput(testSession, message, error);
    }

    public PhpTestingProvider getTestingProvider() {
        return testingProvider;
    }

    @Override
    public void setCoverage(Coverage coverage) {
        coverageSet = true;
        this.coverage = coverage;
    }

    @CheckForNull
    public Coverage getCoverage() {
        return coverage;
    }

    public boolean isCoverageSet() {
        return coverageSet;
    }

    public Manager getManager() {
        return manager;
    }

    public org.netbeans.modules.gsf.testrunner.api.TestSession getTestSession() {
        return testSession;
    }

    public Report getReport() {
        assert report != null;
        return report;
    }

    public boolean isTestException() {
        return testException;
    }

    public void setTestException(boolean testException) {
        this.testException = testException;
    }

    void freeze() {
        frozen = true;
    }

    void checkFrozen() {
        if (frozen) {
            throw new IllegalStateException("Test session is already frozen (PhpTestingProvider.runTests() already finished)");
        }
    }

    //~ Mappers

    private org.netbeans.modules.gsf.testrunner.ui.api.OutputLineHandler map(final OutputLineHandler outputLineHandler) {
        return new org.netbeans.modules.gsf.testrunner.ui.api.OutputLineHandler() {
            @Override
            public void handleLine(OutputWriter out, String text) {
                outputLineHandler.handleLine(out, text);
            }
        };
    }

}
