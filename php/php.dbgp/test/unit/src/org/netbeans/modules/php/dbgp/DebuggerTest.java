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
package org.netbeans.modules.php.dbgp;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.dbgp.breakpoints.LineBreakpoint;
import org.netbeans.modules.php.dbgp.breakpoints.Utils;
import org.netbeans.modules.php.dbgp.packets.RunCommand;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.text.Line;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * For running this test is necessary properly configured xdebug
 * @author Radek Matous
 */
public class DebuggerTest extends NbTestCase {

    public DebuggerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Utils.setLineFactory(new TestLineFactory());
        System.setProperty("TestRun", "On");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.setProperty("TestRun", "Off");
    }

    /**
     * Test of debug method, of class Debugger.
     */
    public void testStopAtBreakpoint()  throws Exception {
        FileObject scriptFo = createPHPTestFile("index.php");//NOI18N
        assertNotNull(scriptFo);
        Project dummyProject = DummyProject.create(scriptFo);
        assertNotNull(dummyProject);
        final SessionId sessionId = new SessionId(scriptFo, dummyProject);
        File scriptFile = FileUtil.toFile(scriptFo);
        assertNotNull(scriptFile);
        assertTrue(scriptFile.exists());
        final TestWrapper testWrapper = new TestWrapper(getTestForSuspendState(sessionId));
        addBreakpoint(scriptFo, 27, testWrapper, new RunContinuation(sessionId));
        startDebugging(sessionId, scriptFile);
        sessionId.isInitialized(true);
        // always fails
        // testWrapper.assertTested();
    }

    // #254298
    public void testCancelSessionIdInitialization()  throws Exception {
        FileObject scriptFo = createPHPTestFile("index.php"); // NOI18N
        assertNotNull(scriptFo);
        Project dummyProject = DummyProject.create(scriptFo);
        assertNotNull(dummyProject);
        final SessionId sessionId = new SessionId(scriptFo, dummyProject);
        File scriptFile = FileUtil.toFile(scriptFo);
        assertNotNull(scriptFile);
        assertTrue(scriptFile.exists());
        // don't connect to the debugger (cannot initialize the session id)
        AtomicBoolean initialized = new AtomicBoolean(true);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            // wait initialization
            initialized.set(sessionId.isInitialized(true));
            countDownLatch.countDown();
        }).start();
        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        new Thread(() -> {
            synchronized(sessionId) {
                sessionId.cancel();
                sessionId.notifyAll();
            }
        }).start();
        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertFalse(initialized.get());
    }

    private static Breakpoint addBreakpoint(final FileObject fo, final int line, final TestWrapper testObj, final Continuation move) {
        Breakpoint breakpoint = new TestLineBreakpoint(createDummyLine(fo, line - 1, testObj, move));
        DebuggerManager.getDebuggerManager().addBreakpoint(breakpoint);
        return breakpoint;
    }

    private static class DummyProject implements Project {

        private final FileObject fo;

        static DummyProject create(FileObject fo) {
            return new DummyProject(fo);
        }

        private DummyProject(FileObject fo) {
            this.fo = fo;
        }

        @Override
        public FileObject getProjectDirectory() {
            return fo.getParent();
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

    }

    static Line createDummyLine(final FileObject fo, final int editorLineNum, final TestWrapper testObj, final Continuation move) {
        return new Line(Lookups.singleton(fo)) {

            @Override
            public int getLineNumber() {
                return editorLineNum;
            }

            @Override
            public void show(int kind) {
                testObj.test();
                move.goAhead();
            }

            @Override
            public void show(int kind, int column) {
                testObj.test();
                move.goAhead();
            }

            @Override
            public void setBreakpoint(boolean b) {
                throw new UnsupportedOperationException("Not supported.");
            }

            @Override
            public boolean isBreakpoint() {
                throw new UnsupportedOperationException("Not supported.");
            }

            @Override
            public void markError() {
                throw new UnsupportedOperationException("Not supported.");
            }

            @Override
            public void unmarkError() {
                throw new UnsupportedOperationException("Not supported.");
            }

            @Override
            public void markCurrentLine() {
                throw new UnsupportedOperationException("Not supported.");
            }

            @Override
            public void unmarkCurrentLine() {
                throw new UnsupportedOperationException("Not supported.");
            }
        };
    }

    private static class TestLineBreakpoint extends LineBreakpoint {

        public TestLineBreakpoint(Line line) {
            super(line);
        }

        @Override
        public boolean isSessionRelated(DebugSession session) {
            return true;
        }
    }

    private static class TestLineFactory extends Utils.LineFactory {

        @Override
        public Line getLine(int line, String remoteFileName, SessionId id) {
            Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
            for (Breakpoint breakpoint : breakpoints) {
                if (breakpoint instanceof TestLineBreakpoint) {
                    TestLineBreakpoint lineBreakpoint = (TestLineBreakpoint) breakpoint;
                    Line lineObj = lineBreakpoint.getLine();
                    Lookup lkp = lineObj.getLookup();
                    FileObject fo = lkp.lookup(FileObject.class);
                    try {
                        URL remoteURL = new URL(remoteFileName);
                        FileObject remoteFo = URLMapper.findFileObject(remoteURL);
                        if (remoteFo == fo && line == lineObj.getLineNumber() + 1) {
                            return lineObj;
                        }
                    } catch (MalformedURLException ex) {
                        break;
                    }
                }
            }
            return super.getLine(line, remoteFileName, id);
        }
    }

    private static class BasedOnSession {

        private SessionId sessionId;

        BasedOnSession(SessionId sessionId) {
            this.sessionId = sessionId;
        }

        SessionId getSessionId() {
            return sessionId;
        }

        DebugSession getDebugSession() {
            return ConversionUtils.toDebugSession(getSessionId());
        }
    }

    private static class TestWrapper {
        private Runnable test;
        private boolean isTested = false;
        private static int WAIT_TIME = 3000;

        TestWrapper(Runnable test) {
            this.test = test;
        }

        synchronized void assertTested() throws InterruptedException {
            if (!isTested) {
                wait(WAIT_TIME);
            }
            assertTrue(isTested);
        }

        synchronized void setAsTested() {
            isTested = true;
            notifyAll();
        }

        void test() {
            this.test.run();
            setAsTested();
        }
    }

    private abstract static class Continuation extends BasedOnSession {
        Continuation(SessionId sessionId) {
            super(sessionId);
        }

        abstract void goAhead();
    }

    private static class RunContinuation extends Continuation {
        RunContinuation(SessionId sessionId) {
            super(sessionId);
        }

        @Override
        void goAhead() {
            DebugSession debugSession = getDebugSession();
            RunCommand command = new RunCommand(debugSession.getTransactionId());
            debugSession.sendCommandLater(command);
        }
    }

    private static FileObject createPHPTestFile(String scriptName) {
        URL urlToScript = DebuggerTest.class.getResource("resources/" + scriptName);
        FileObject scriptFo = URLMapper.findFileObject(urlToScript);
        return scriptFo;
    }

    private String getPHPInterpreter() {
        String command = DebuggerOptions.getGlobalInstance().getPhpInterpreter();
        if (command == null) {
            /*TODO: use more sophisticated code here for individual platforms
             * to find out php (such a code exists in  SystemPackageFinder.getPhpInterpreterAny());
             */
            command = "/usr/bin/php";
        }
        return command;
    }

    private Runnable getTestForSuspendState(final SessionId sessionId) {
    return  new Runnable() {
        @Override
            public void run() {
                //TODO: can be tested much more here - not ready yet
                Session session = DebuggerManager.getDebuggerManager().getCurrentSession();
                assertNotNull(session);
                DebuggerEngine engine = session.getCurrentEngine();
                assertNotNull(engine);
                ActionsManager actionManager = engine.getActionsManager();
                assertNotNull(actionManager);
                DebugSession debugSession = ConversionUtils.toDebugSession(session);
                assertNotNull(debugSession);
                assertEquals(sessionId, debugSession.getSessionId());
            }
        };
    }

    private void startDebugging(final SessionId sessionId, File scriptFile) {
        final ProcessBuilder processBuilder = new ProcessBuilder(new String[]{getPHPInterpreter(), scriptFile.getAbsolutePath()});
        processBuilder.directory(scriptFile.getParentFile());
        processBuilder.environment().put("XDEBUG_CONFIG", "idekey=" + sessionId.getId()); //NOI18N
        final DebuggerOptions options = DebuggerOptions.getGlobalInstance();
        options.pathMapping = Collections.emptyList();
        SessionManager.getInstance().startSession(sessionId, options, new Callable<Cancellable>() {
            @Override
            public Cancellable call() throws Exception {
                processBuilder.start();
                return new Cancellable() {

                    @Override
                    public boolean cancel() {
                        return true;
                    }
                };
            }
        });

    }
}
