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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
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
import org.openide.filesystems.URLMapper;

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
    
    private String firstModulePath(ModuleInfo token) {
        List<String> paths = token.getTestRoots();
        if (paths == null || paths.isEmpty()) {
            return null;
        } else if (paths.size() > 1) {
            LOG.log(Level.WARNING, "Mutliple test roots are not yet supported for module {0}", token.getModuleName());
        }
        return paths.iterator().next();
    }

    @Override
    public void displaySuiteRunning(ModuleInfo token, String suiteName) {
        lspClient.notifyTestProgress(new TestProgressParams(uri, new TestSuiteInfo(suiteName, TestSuiteInfo.State.Started).setModuleName(token.getModuleName()).setModulePath(firstModulePath(token))));
    }

    @Override
    public void displaySuiteRunning(ModuleInfo token, TestSuite suite) {
        lspClient.notifyTestProgress(new TestProgressParams(uri, new TestSuiteInfo(suite.getName(), TestSuiteInfo.State.Started).setModuleName(token.getModuleName()).setModulePath(firstModulePath(token))));
    }

    @Override
    public void displayReport(ModuleInfo token, Report report) {
        Map<String, FileObject> fileLocations = new HashMap<>();
        Map<String, TestSuiteInfo.TestCaseInfo> testCases = new LinkedHashMap<>();
        String className = report.getSuiteClassName();
        for (Testcase test : report.getTests()) {
            String name = test.getName();
            String id = className + ':' + name;
            String state = statusToState(test.getStatus());
            List<String> stackTrace = test.getTrouble() != null ? Arrays.asList(test.getTrouble().getStackTrace()) : null;
            String location = test.getLocation();
            FileObject fo = location != null ? fileLocations.computeIfAbsent(location, loc -> {
                LineConvertors.FileLocator fileLocator = test.getSession().getProject().getLookup().lookup(LineConvertors.FileLocator.class);
                int i = loc.indexOf(':'); //TODO: windows??
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
        TestSuiteInfo testSuiteInfo = new TestSuiteInfo(report.getSuiteClassName(), token.getModuleName(), firstModulePath(token),
                fo != null ? Utils.toUri(fo) : null, null, state, new ArrayList<>(testCases.values()));
        
        setRelativePath(testSuiteInfo, fo);
        lspClient.notifyTestProgress(new TestProgressParams(uri, testSuiteInfo));
    }

    private void setRelativePath(TestSuiteInfo suiteInfo, FileObject fo) {
        Project owner = fo != null ? FileOwnerQuery.getOwner(fo) : null;
        String relativePath = null;
        if (owner != null) {
            Sources sources = ProjectUtils.getSources(owner);

            for (String sourceGroupKind : new String[] {JavaProjectConstants.SOURCES_TYPE_JAVA, "jdk-project-sources-tests"}) { //XXX: hardcoded test root key
                SourceGroup[] groups = sources.getSourceGroups(sourceGroupKind);

                for (SourceGroup group : groups) {
                    if (FileUtil.isParentOf(group.getRootFolder(), fo)) {
                        relativePath = FileUtil.getRelativePath(group.getRootFolder(), fo);
                        break;
                    }
                }
            }
        }
        suiteInfo.setRelativePath(relativePath);
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
        List<String> testPaths = getModuleTestPaths(project);
        return new ModuleInfo(moduleName, testPaths);
    }
    
    private static List<String> getModuleTestPaths(Project project) {        
        if (project == null) {
            return null;
        }
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set<String> paths = new LinkedHashSet<>();
        for (SourceGroup sourceGroup : sourceGroups) {
            URL[] urls = UnitTestForSourceQuery.findUnitTests(sourceGroup.getRootFolder());
            for (URL u : urls) {
                FileObject f = URLMapper.findFileObject(u);
                if (f != null) {
                    paths.add(f.getPath());
                }
            }
        }
        return paths.isEmpty() ? null : new ArrayList<>(paths);
    }
}
