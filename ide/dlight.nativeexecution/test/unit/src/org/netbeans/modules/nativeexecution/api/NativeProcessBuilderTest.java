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
package org.netbeans.modules.nativeexecution.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.ConcurrentTasksSupport.Counters;
import org.netbeans.modules.nativeexecution.api.NativeProcess.State;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.UserQuestionException;

public class NativeProcessBuilderTest extends NativeExecutionBaseTestCase {

    public NativeProcessBuilderTest(String name) {
        super(name);
    }

    public NativeProcessBuilderTest(String name, ExecutionEnvironment execEnv) {
        super(name, execEnv);
    }

    @SuppressWarnings("unchecked")
    public static Test suite() {
        return new NativeExecutionBaseTestSuite(NativeProcessBuilderTest.class);
    }

    public void testNewLocalProcessBuilder() throws Exception {
        NativeProcessBuilder npb = NativeProcessBuilder.newLocalProcessBuilder();
        assertNotNull(npb);
    }

    public void testSetExecutableLocal() throws Exception {
        doTestSetExecutable(ExecutionEnvironmentFactory.getLocal());
    }

    @ForAllEnvironments(section = "remote.platforms")
    public void testSetExecutable() throws Exception {
        doTestSetExecutable(getTestExecutionEnvironment());
    }

    public void testListenersLocal() throws Exception {
        doTestListeners(ExecutionEnvironmentFactory.getLocal(), true, true);
        doTestListeners(ExecutionEnvironmentFactory.getLocal(), false, true);
        doTestListeners(ExecutionEnvironmentFactory.getLocal(), true, false);
        doTestListeners(ExecutionEnvironmentFactory.getLocal(), false, false);
    }

    @ForAllEnvironments(section = "remote.platforms")
    public void testListeners() throws Exception {
        int count = 5;
        RequestProcessor rp = new RequestProcessor("testListeners", 50);
        final CountDownLatch done = new CountDownLatch(count * 4);
        final CountDownLatch start = new CountDownLatch(1);

        for (int i = 0; i < count * 4; i++) {
            final int x = i;
            rp.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        start.await();

                        switch (x % 4) {
                            case 0:
                                doTestListeners(getTestExecutionEnvironment(), true, true);
                                break;
                            case 1:
                                doTestListeners(getTestExecutionEnvironment(), true, false);
                                break;
                            case 2:
                                doTestListeners(getTestExecutionEnvironment(), false, true);
                                break;
                            case 3:
                                doTestListeners(getTestExecutionEnvironment(), false, false);
                                break;

                        }
                    } catch (Throwable ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        done.countDown();
                    }
                }
            });
        }

        start.countDown();
        done.await();
    }

    private void doTestListeners(ExecutionEnvironment env, final boolean startLongProcessAndTerminateIt, final boolean usePty) {
        log("Starts doTestListeners(" + env.getDisplayName() + ", " + startLongProcessAndTerminateIt + ", " + usePty + ")"); // NOI18N
        
        try {
            ConnectionManager.getInstance().connectTo(env);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (CancellationException ex) {
            Exceptions.printStackTrace(ex);
        }

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);

        if (startLongProcessAndTerminateIt) {
            npb.setExecutable("sleep").setArguments("1000"); // NOI18N
        } else {
            npb.setExecutable("echo").setArguments("1000"); // NOI18N
        }

        npb.setUsePty(usePty);

        final Counters counters = new Counters();
        final AtomicInteger result = new AtomicInteger();
        final AtomicReference<NativeProcess> processRef = new AtomicReference<>();

        npb.addNativeProcessListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if (!(e instanceof NativeProcessChangeEvent)) {
                    fail("Unexpected event");
                    return;
                }

                assertTrue("Source must be a subclass of NativeProcess (" + e.getSource().getClass() + ")", NativeProcess.class.isAssignableFrom(e.getSource().getClass()));

                final NativeProcessChangeEvent event = (NativeProcessChangeEvent) e;
                final NativeProcess process = (NativeProcess) event.getSource();
                final State state = event.state;
                processRef.compareAndSet(null, process);
                assertEquals("There must be only one source of events", processRef.get(), process);

                counters.getCounter(state.name()).incrementAndGet();

                if (state == State.FINISHED || state == State.CANCELLED || state == State.ERROR) {
                    result.set(processRef.get().exitValue());
                }

                switch (state) {
                    case STARTING:
                        assertEquals(0, counters.getCounter(State.RUNNING.name()).get());
                        assertEquals(0, counters.getCounter(State.FINISHED.name()).get());
                        assertEquals(0, counters.getCounter(State.CANCELLED.name()).get());
                        assertEquals(0, counters.getCounter(State.ERROR.name()).get());
                        break;
                    case RUNNING:
                        assertEquals(1, counters.getCounter(State.STARTING.name()).get());
                        assertEquals(0, counters.getCounter(State.FINISHED.name()).get());
                        assertEquals(0, counters.getCounter(State.CANCELLED.name()).get());
                        assertEquals(0, counters.getCounter(State.ERROR.name()).get());
                        break;
                    case CANCELLED:
                        if (!startLongProcessAndTerminateIt) {
                            fail("CANCELLED state is NOT expected as process was terminated");
                        }
                        assertEquals(1, counters.getCounter(State.STARTING.name()).get());
                        assertEquals(1, counters.getCounter(State.RUNNING.name()).get());
                        assertEquals(1, counters.getCounter(State.CANCELLED.name()).get());
                        assertEquals(0, counters.getCounter(State.FINISHED.name()).get());
                        assertEquals(0, counters.getCounter(State.ERROR.name()).get());
                        break;
                    case FINISHED:
                        if (startLongProcessAndTerminateIt) {
                            fail("FINISHED state is NOT expected as process was terminated");
                        }
                        assertEquals(1, counters.getCounter(State.STARTING.name()).get());
                        assertEquals(1, counters.getCounter(State.RUNNING.name()).get());
                        assertEquals(1, counters.getCounter(State.FINISHED.name()).get());
                        assertEquals(0, counters.getCounter(State.CANCELLED.name()).get());
                        assertEquals(0, counters.getCounter(State.ERROR.name()).get());
                        break;
                }
            }
        });

        try {
            NativeProcess process = npb.call();
            assertEquals("It is expected that a process returned from call() is the source of all events", processRef.get(), process);

            if (startLongProcessAndTerminateIt) {
                process.destroy();
            }
            int exitCode = process.waitFor();
            assertEquals("It is expected that exitCode is the same as reported for listeners", result.get(), exitCode);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void testSetCommandLineLocal() throws Exception {
        doTestSetCommandLine(ExecutionEnvironmentFactory.getLocal());
    }

    @ForAllEnvironments(section = "remote.platforms")
    public void testSetCommandLine() throws Exception {
        doTestSetCommandLine(getTestExecutionEnvironment());
    }
    
    public void testAsksForconnection() {
        ExecutionEnvironment ee = ExecutionEnvironmentFactory.createNew("never", "existed.com");
        NativeProcessBuilder b = NativeProcessBuilder.newProcessBuilder(ee);
        b.setExecutable("ade");
        b.setArguments("lsviews");
        try {
            b.call();
            fail("The previous call should thrown an exception");
        } catch (UserQuestionException ex) {
            // OK, expected
            assertEquals(
                "Localized message as expected",
                Bundle.EXC_NotConnectedQuestion(ee.getDisplayName()), 
                ex.getLocalizedMessage()
            );
            assertEquals("No connection to never@existed.com", ex.getMessage());
        } catch (IOException ex) {
            fail("we should not get IOException: " + ex);
        }
    }

    private void doTestSetCommandLine(ExecutionEnvironment execEnv) throws Exception {
        HostInfo hostinfo = HostInfoUtils.getHostInfo(execEnv);
        String tmpDir = hostinfo.getTempDir();
        String testDir = tmpDir + "/path with a space";
        NativeProcessBuilder npb;

        // Don't use CommonTasksSupport.{rmDir,mkDir} in this test

        npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setCommandLine("rm -rf \"" + testDir + "\"");
        assertEquals("rm -rf \"" + testDir + "\"", 0, npb.call().waitFor());

        npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setCommandLine("mkdir \"" + testDir + "\""); // NOI18N
        assertEquals("mkdir \"" + testDir + "\"", 0, npb.call().waitFor());

        if (execEnv.isLocal() && hostinfo.getOS().getFamily() == HostInfo.OSFamily.WINDOWS) {
            String realDir = WindowsSupport.getInstance().convertToWindowsPath(testDir);
            assertTrue(new File(realDir).isDirectory());
        } else {
            assertTrue(testDir + " does not exist", HostInfoUtils.fileExists(execEnv, testDir));
        }

        String lsCmd;
        if (execEnv.isLocal() && hostinfo.getOS().getFamily() == HostInfo.OSFamily.WINDOWS) {
            lsCmd = findComand("ls.exe");
            assertNotNull("Cannot find ls.exe in paths", lsCmd);
            lsCmd = WindowsSupport.getInstance().convertToShellPath(lsCmd);
        } else {
            lsCmd = "/bin/ls";
        }

        npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setCommandLine("cp " + lsCmd + " \"" + testDir + "/copied ls\""); // NOI18N
        assertEquals("cp " + lsCmd + " \"" + testDir + "/copied ls\"", 0, npb.call().waitFor());

        npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        if (execEnv.isLocal() && hostinfo.getOS().getFamily() == HostInfo.OSFamily.WINDOWS) {
            npb.setCommandLine("\"" + WindowsSupport.getInstance().convertToWindowsPath(testDir + "/copied ls") + "\" \"" + testDir + "\"");
        } else {
            npb.setCommandLine("\"" + testDir + "/copied ls\" \"" + testDir + "\"");
        }

        List<String> lsOut = ProcessUtils.readProcessOutput(npb.call());
        boolean found = false;
        for (String line : lsOut) {
            if (line.equals("copied ls")) {
                found = true;
                break;
            }
        }
        assertTrue("\"copied ls\" not found in ls output", found);
    }

    private void doTestSetExecutable(ExecutionEnvironment execEnv) throws Exception {
        HostInfo hostinfo = HostInfoUtils.getHostInfo(execEnv);
        String tmpDir = hostinfo.getTempDir();
        String testDir = tmpDir + "/path with a space"; // NOI18N
        NativeProcessBuilder npb;

        // Don't use CommonTasksSupport.{rmDir,mkDir} in this test

        npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setExecutable("rm").setArguments("-rf", testDir);
        assertEquals("rm -rf \"" + testDir + "\"", 0, npb.call().waitFor());

        npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setExecutable("mkdir").setArguments(testDir);
        assertEquals("mkdir \"" + testDir + "\"", 0, npb.call().waitFor());

        if (execEnv.isLocal() && hostinfo.getOS().getFamily() == HostInfo.OSFamily.WINDOWS) {
            String realDir = WindowsSupport.getInstance().convertToWindowsPath(testDir);
            assertTrue(new File(realDir).isDirectory());
        } else {
            assertTrue(testDir + " does not exist", HostInfoUtils.fileExists(execEnv, testDir));
        }

        String lsCmd;
        if (execEnv.isLocal() && hostinfo.getOS().getFamily() == HostInfo.OSFamily.WINDOWS) {
            lsCmd = findComand("ls.exe");
            assertNotNull("Cannot find ls.exe in paths", lsCmd);
            lsCmd = WindowsSupport.getInstance().convertToShellPath(lsCmd);
        } else {
            lsCmd = "/bin/ls";
        }

        npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setCommandLine("cp " + lsCmd + " \"" + testDir + "/copied ls\""); // NOI18N
        assertEquals("cp " + lsCmd + " \"" + testDir + "/copied ls\"", 0, npb.call().waitFor());

        npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        if (execEnv.isLocal() && hostinfo.getOS().getFamily() == HostInfo.OSFamily.WINDOWS) {
            npb.setExecutable(WindowsSupport.getInstance().convertToWindowsPath(testDir + "/copied ls"));
        } else {
            npb.setExecutable(testDir + "/copied ls");
        }
        npb.setArguments(testDir);

        List<String> lsOut = ProcessUtils.readProcessOutput(npb.call());
        boolean found = false;
        for (String line : lsOut) {
            if (line.equals("copied ls")) {
                found = true;
                break;
            }
        }
        assertTrue("\"copied ls\" not found in ls output", found);
    }

    private String findComand(String command) {
        ArrayList<String> list = new ArrayList<>();
        String path = System.getenv("PATH"); // NOI18N
        if (path != null) {
            StringTokenizer st = new StringTokenizer(path, File.pathSeparator); // NOI18N
            while (st.hasMoreTokens()) {
                String dir = st.nextToken();
                list.add(dir);
            }
        } else {
            list.add("C:/WINDOWS/System32"); // NOI18N
            list.add("C:/WINDOWS"); // NOI18N
            list.add("C:/cygwin/bin"); // NOI18N
        }
        for (String p : list) {
            String task = p + File.separatorChar + command;
            File tool = new File(task);
            if (tool.exists() && tool.isFile()) {
                return tool.getAbsolutePath();
            }
        }
        return null;
    }
}
