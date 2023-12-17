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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import static junit.framework.TestCase.fail;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.junit.Test;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.java.lsp.server.TestCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.protocol.DecorationRenderOptions;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeClientCapabilities;
import org.netbeans.modules.java.lsp.server.explorer.api.NodeChangedParams;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
import org.netbeans.modules.java.lsp.server.protocol.SetTextEditorDecorationParams;
import org.netbeans.modules.java.lsp.server.input.ShowInputBoxParams;
import org.netbeans.modules.java.lsp.server.input.ShowQuickPickParams;
import org.netbeans.modules.java.lsp.server.protocol.SaveDocumentRequestParams;
import org.netbeans.modules.java.lsp.server.protocol.ShowStatusMessageParams;
import org.netbeans.modules.java.lsp.server.protocol.TestProgressParams;
import org.netbeans.modules.java.lsp.server.protocol.TestSuiteInfo;
import org.netbeans.modules.java.lsp.server.protocol.UpdateConfigParams;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Dusan Balek
 */
public class TestProgressHandlerTest extends NbTestCase {

    public TestProgressHandlerTest(String name) {
        super(name);
    }

    @Test
    public void testProgress() {
        FileObject fo = null;
        try {
            fo = FileUtil.toFileObject(getWorkDir());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        assertNotNull(fo);
        List<TestProgressParams> msgs = new ArrayList<>();
        MockLanguageClient mlc = new MockLanguageClient(msgs);
        TestProgressHandler progressHandler = new TestProgressHandler(mlc, new IDebugProtocolClient() {}, fo.toURI().toString());
        progressHandler.displaySuiteRunning(progressHandler, "TestSuiteName");
        FileObject projectDir = fo;
        Project project = new Project() {
            @Override
            public FileObject getProjectDirectory() {
                return projectDir;
            }

            @Override
            public Lookup getLookup() {
                return Lookups.fixed(new LineConvertors.FileLocator() {
                    @Override
                    public FileObject find(String filename) {
                        return "TestSuiteName".equals(filename) ? projectDir : null;
                    }
                });
            }
        };
        Report report = new Report("TestSuiteName", project);
        TestSession session = new TestSession("TestSession", project, TestSession.SessionType.TEST);
        Testcase[] tests = new Testcase[] {
            new Testcase("test1", "TestSuiteName.test1", "TEST", session),
            new Testcase("test2", "TestSuiteName.test2", "TEST", session)
        };
        tests[0].setClassName("TestSuiteName");
        tests[0].setLocation("TestSuiteName:1");
        tests[0].setStatus(Status.PASSED);
        tests[1].setClassName("TestSuiteName");
        tests[1].setLocation("TestSuiteName:2");
        tests[1].setStatus(Status.FAILED);
        Trouble trouble = new Trouble(false);
        trouble.setStackTrace(new String[] {"TestSuiteName:2"});
        tests[1].setTrouble(trouble);
        report.setTests(Arrays.asList(tests));
        report.setTotalTests(2);
        report.setPassed(1);
        report.setFailures(1);
        progressHandler.displayReport(progressHandler, report);
        assertEquals("Two messages", 2, msgs.size());
        assertEquals(fo.toURI().toString(), msgs.get(0).getUri());
        TestSuiteInfo suite = msgs.get(0).getSuite();
        assertEquals("TestSuiteName", suite.getName());
        assertEquals(TestSuiteInfo.State.Started, suite.getState());
        assertEquals(fo.toURI().toString(), msgs.get(1).getUri());
        suite = msgs.get(1).getSuite();
        assertEquals("TestSuiteName", suite.getName());
        assertEquals(TestSuiteInfo.State.Failed, suite.getState());
        assertEquals(2, suite.getTests().size());
        TestSuiteInfo.TestCaseInfo testCase = suite.getTests().get(0);
        assertEquals("TestSuiteName:TestSuiteName.test1", testCase.getId());
        assertEquals("TestSuiteName.test1", testCase.getName());
        assertEquals(fo.toURI().toString(), testCase.getFile());
        assertEquals(TestSuiteInfo.State.Passed, testCase.getState());
        assertNull(testCase.getStackTrace());
        testCase = suite.getTests().get(1);
        assertEquals("TestSuiteName:TestSuiteName.test2", testCase.getId());
        assertEquals("TestSuiteName.test2", testCase.getName());
        assertEquals(fo.toURI().toString(), testCase.getFile());
        assertEquals(TestSuiteInfo.State.Failed, testCase.getState());
        assertNotNull(testCase.getStackTrace());
    }

    private static final class MockLanguageClient extends TestCodeLanguageClient {
        private final List<TestProgressParams> messages;

        MockLanguageClient(List<TestProgressParams> messages) {
            this.messages = messages;
        }

        @Override
        public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
            fail();
        }

        @Override
        public void showMessage(MessageParams messageParams) {
            fail();
        }

        @Override
        public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
            fail();
            return null;
        }

        @Override
        public void logMessage(MessageParams message) {
            fail();
        }

        @Override
        public void showStatusBarMessage(ShowStatusMessageParams params) {
            fail();
        }

        @Override
        public CompletableFuture<List<QuickPickItem>> showQuickPick(ShowQuickPickParams params) {
            fail();
            return null;
        }

        @Override
        public CompletableFuture<String> showInputBox(ShowInputBoxParams params) {
            fail();
            return null;
        }

        @Override
        public void notifyTestProgress(TestProgressParams params) {
            messages.add(params);
        }

        @Override
        public NbCodeClientCapabilities getNbCodeCapabilities() {
            fail();
            return null;
        }

        @Override
        public CompletableFuture<String> createTextEditorDecoration(DecorationRenderOptions params) {
            fail();
            return null;
        }

        @Override
        public void setTextEditorDecoration(SetTextEditorDecorationParams params) {
            fail();
        }

        @Override
        public void disposeTextEditorDecoration(String params) {
            fail();
        }

        @Override
        public void notifyNodeChange(NodeChangedParams params) {
            fail();
        }

        @Override
        public CompletableFuture<Void> configurationUpdate(UpdateConfigParams params) {
            return CompletableFuture.completedFuture(null);
        }
    }
}
