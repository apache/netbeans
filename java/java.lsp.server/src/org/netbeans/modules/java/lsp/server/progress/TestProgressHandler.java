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
package org.netbeans.modules.java.lsp.server.progress;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.ui.api.TestResultDisplayHandler;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.protocol.TestProgressParams;
import org.netbeans.modules.java.lsp.server.protocol.TestSuiteInfo;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Dusan Balek
 */
public final class TestProgressHandler implements TestResultDisplayHandler.Spi<TestProgressHandler> {

    private final NbCodeLanguageClient client;
    private final String uri;

    public TestProgressHandler(NbCodeLanguageClient client, String uri) {
        this.client = client;
        this.uri = uri;
    }

    @Override
    public TestProgressHandler create(TestSession session) {
        return this;
    }

    @Override
    public void displayOutput(TestProgressHandler token, String text, boolean error) {
    }

    @Override
    public void displaySuiteRunning(TestProgressHandler token, String suiteName) {
        client.notifyTestProgress(new TestProgressParams(uri, new TestSuiteInfo(suiteName, TestSuiteInfo.State.Running)));
    }

    @Override
    public void displaySuiteRunning(TestProgressHandler token, TestSuite suite) {
        client.notifyTestProgress(new TestProgressParams(uri, new TestSuiteInfo(suite.getName(), TestSuiteInfo.State.Running)));
    }

    @Override
    public void displayReport(TestProgressHandler token, Report report) {
        Map<String, FileObject> fileLocations = new HashMap<>();
        List<TestSuiteInfo.TestCaseInfo> tests = report.getTests().stream().map((test) -> {
            String className = test.getClassName();
            String displayName = test.getDisplayName();
            String shortName = displayName.startsWith(className + '.') ? displayName.substring(className.length() + 1) : displayName;
            int idx = shortName.indexOf('(');
            if (idx > 0) {
                shortName = shortName.substring(0, idx);
            }
            String status;
            switch (test.getStatus()) {
                case PASSED:
                    status = TestSuiteInfo.State.Passed;
                    break;
                case ERROR:
                    status = TestSuiteInfo.State.Errored;
                    break;
                case FAILED:
                    status = TestSuiteInfo.State.Failed;
                    break;
                case SKIPPED:
                    status = TestSuiteInfo.State.Skipped;
                    break;
                default:
                    throw new IllegalStateException("Unexpected testcase status: " + test.getStatus());
            }
            List<String> stackTrace = test.getTrouble() != null ? Arrays.asList(test.getTrouble().getStackTrace()) : null;
            String location = test.getLocation();
            FileObject fo = location != null ? fileLocations.computeIfAbsent(location, loc -> {
                LineConvertors.FileLocator fileLocator = test.getSession().getProject().getLookup().lookup(LineConvertors.FileLocator.class);
                int i = loc.indexOf(':');
                if (i > 0) {
                    loc = loc.substring(0, i);
                }
                return fileLocator != null ? fileLocator.find(loc) : null;
            }) : null;
            return new TestSuiteInfo.TestCaseInfo(className + ':' + shortName, shortName, displayName,
                    fo != null ? Utils.toUri(fo) : null, null, status, stackTrace);
        }).collect(Collectors.toList());
        String status;
        switch (report.getStatus()) {
            case PASSED:
            case FAILED:
                status = TestSuiteInfo.State.Completed;
                break;
            case ERROR:
                status = TestSuiteInfo.State.Errored;
                break;
            default:
                throw new IllegalStateException("Unexpected testsuite status: " + report.getStatus());
        }
        FileObject fo = fileLocations.size() == 1 ? fileLocations.values().iterator().next() : null;
        client.notifyTestProgress(new TestProgressParams(uri, new TestSuiteInfo(report.getSuiteClassName(),
                fo != null ? Utils.toUri(fo) : null, null, status, tests)));
    }

    @Override
    public void displayMessage(TestProgressHandler token, String message) {
    }

    @Override
    public void displayMessageSessionFinished(TestProgressHandler token, String message) {
    }

    @Override
    public int getTotalTests(TestProgressHandler token) {
        return 0;
    }
}
