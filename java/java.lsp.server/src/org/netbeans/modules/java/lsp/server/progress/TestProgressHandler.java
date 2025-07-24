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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.ui.api.TestResultDisplayHandler;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.protocol.TestProgressParams;
import org.netbeans.modules.java.lsp.server.protocol.TestSuiteInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Dusan Balek
 */
public final class TestProgressHandler implements TestResultDisplayHandler.Spi<ModuleInfo> {
    private static final Logger LOG = Logger.getLogger(TestProgressHandler.class.getName());
    
    private final NbCodeLanguageClient lspClient;
    private final IDebugProtocolClient debugClient;
    private final String uri;

    public TestProgressHandler(NbCodeLanguageClient lspClient, IDebugProtocolClient debugClient, String uri) {
        this.lspClient = lspClient;
        this.debugClient = debugClient;
        this.uri = uri;
    }

    @Override
    public ModuleInfo create(TestSession session) {
        return getModuleInfo(session);
    }

    @Override
    public void displayOutput(ModuleInfo token, String text, boolean error) {
        if (text != null) {
            OutputEventArguments output = new OutputEventArguments();
            output.setOutput(text.trim() + "\n");
            debugClient.output(output);
        }
    }
    
    private FileObject findModulePath(ModuleInfo token, FileObject testLocation) {
        List<FileObject> paths = token.getTestRoots();

        return TestUtils.findModulePath(token.getModuleName(), paths, testLocation);
    }

    @Override
    public void displaySuiteRunning(ModuleInfo token, String suiteName) {
        FileObject modulePath = findModulePath(token, null/*can anything be done here?*/);
        lspClient.notifyTestProgress(new TestProgressParams(uri, new TestSuiteInfo(suiteName, TestSuiteInfo.State.Started).setModuleName(token.getModuleName()).setModulePath(TestUtils.toPath(modulePath))));
    }

    @Override
    public void displaySuiteRunning(ModuleInfo token, TestSuite suite) {
        FileObject modulePath = findModulePath(token, getFileFromTestCases(suite.getTestcases()));
        //TODO: set relative path?
        lspClient.notifyTestProgress(new TestProgressParams(uri, new TestSuiteInfo(suite.getName(), TestSuiteInfo.State.Started).setModuleName(token.getModuleName()).setModulePath(TestUtils.toPath(modulePath))));
    }

    @Override
    public void displayReport(ModuleInfo token, Report report) {
        Map<String, FileObject> fileLocations = new HashMap<>();
        Map<String, TestSuiteInfo.TestCaseInfo> testCases = new LinkedHashMap<>();
        String className = report.getSuiteClassName();
        for (Testcase test : report.getTests()) {
            String name = test.getDisplayName();
            String id = className + ':' + name;
            String state = statusToState(test.getStatus());
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
            int cnt = 1;
            String base = id;
            while (testCases.containsKey(id)) {
                id = base + '-' + cnt++;
            }
            TestSuiteInfo.TestCaseInfo info = new TestSuiteInfo.TestCaseInfo(id, name, fo != null ? Utils.toUri(fo) : null, null, state, stackTrace);
            testCases.put(id, info);
        }
        String state = statusToState(report.getStatus());
        FileObject fo = fileLocations.size() == 1 ? fileLocations.values().iterator().next() : null;
        FileObject modulePath = findModulePath(token, fo);
        TestSuiteInfo testSuiteInfo = new TestSuiteInfo(report.getSuiteClassName(), token.getModuleName(), TestUtils.toPath(modulePath),
                fo != null ? Utils.toUri(fo) : null, null, state, new ArrayList<>(testCases.values()));
        
        setRelativePath(testSuiteInfo, modulePath, fo);
        lspClient.notifyTestProgress(new TestProgressParams(uri, testSuiteInfo));
    }

    private FileObject getFileFromTestCases(Iterable<? extends Testcase> tests) {
        String location = null;
        boolean locationSet = false;
        for (Testcase test : tests) {
            if (!locationSet) {
                location = test.getLocation();
                locationSet = true;
            } else if (!Objects.equals(location, test.getLocation())) {
                //TODO: log?
                return null;
            }
        }
        if (location != null) {
            LineConvertors.FileLocator fileLocator = tests.iterator().next().getSession().getProject().getLookup().lookup(LineConvertors.FileLocator.class);
            int i = location.indexOf(':'); //TODO: windows??
            if (i > 0) {
                location = location.substring(0, i);
            }
            return fileLocator != null ? fileLocator.find(location) : null;
        }

        return null;
    }

    private void setRelativePath(TestSuiteInfo suiteInfo, FileObject modulePath, FileObject fo) {
        if (modulePath != null) {
            suiteInfo.setRelativePath(FileUtil.getRelativePath(modulePath, fo));
        }
    }

    @Override
    public void displayMessage(ModuleInfo token, String message) {
    }

    @Override
    public void displayMessageSessionFinished(ModuleInfo token, String message) {
    }

    @Override
    public int getTotalTests(ModuleInfo token) {
        return 0;
    }

    private String statusToState(Status status) {
        switch (status) {
            case PASSED:
            case PASSEDWITHERRORS:
                return TestSuiteInfo.State.Passed;
            case ERROR:
                return TestSuiteInfo.State.Errored;
            case FAILED:
                return TestSuiteInfo.State.Failed;
            case SKIPPED:
            case ABORTED:
            case IGNORED:
                return TestSuiteInfo.State.Skipped;
            case PENDING:
                return TestSuiteInfo.State.Started;
            default:
                throw new IllegalStateException("Unexpected testsuite status: " + status);
        }
    }

    private static ModuleInfo getModuleInfo(TestSession session) {
        Project project = session.getProject();
        String moduleName = project != null ? ProjectUtils.getInformation(project).getDisplayName() : null;
        List<FileObject> testPaths = TestUtils.getModuleTestPaths(project);
        return new ModuleInfo(moduleName, testPaths);
    }
    
}
