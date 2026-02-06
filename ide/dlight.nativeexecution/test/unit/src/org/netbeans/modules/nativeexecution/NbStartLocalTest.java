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
package org.netbeans.modules.nativeexecution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import junit.framework.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.Signal;
import org.netbeans.modules.nativeexecution.pty.NbStartUtility;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Andrew
 */
public class NbStartLocalTest extends NativeExecutionBaseTestCase {

    public static final ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();

    public NbStartLocalTest(String name) {
        super(name);
    }

    public NbStartLocalTest(String name, ExecutionEnvironment execEnv) {
        super(name, execEnv);
    }

    public static Test suite() {
        return new NativeExecutionBaseTestSuite(NbStartLocalTest.class);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public boolean canRun() {
        boolean res = super.canRun();
        res = res && NbStartUtility.getInstance(true).isSupported(env);
        return res;
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @org.junit.Test
    public void testSimpleShortProcess() throws Exception {
        NativeProcessBuilder npb = NativeProcessBuilder.newLocalProcessBuilder();
        Properties properties = System.getProperties();
        String user = properties.getProperty("user.name");

        File echo = new File("/bin/echo");
        if (!echo.exists()) {
            echo = new File("/usr/bin/echo");
            if (!echo.exists()) {
                    System.out.println("Unable to find local echo executable");
                    return;
            }
        }

        npb.setExecutable(echo.getAbsolutePath()).setArguments(user).setStatusEx(true);
        NativeProcess process = npb.call();
        int rc = process.waitFor();
        assertEquals(echo + " exit status", 0, rc);
        String userName = ProcessUtils.readProcessOutputLine(process);
        assertEquals(user, userName);
    }

    @org.junit.Test
    public void testNonexistentProcess() throws Exception {
        NativeProcessBuilder npb = NativeProcessBuilder.newLocalProcessBuilder();
        npb.setExecutable("wrong-process");
        NativeProcess process = npb.call();
        int rc = process.waitFor();
        assertTrue("wrong-process exit status != 0", rc != 0);
        String error = ProcessUtils.readProcessErrorLine(process);
        System.out.println(error);
    }

    @org.junit.Test
    public void testShellCommandProcess() throws Exception {
        NativeProcessBuilder npb = NativeProcessBuilder.newLocalProcessBuilder();
        File out = new File(getWorkDirPath(), "testShellCommandProcess");
        out.mkdirs();
        out.delete();
        try {
            npb.setCommandLine("echo TEST > \"" + out.getAbsolutePath() + "\"");
            NativeProcess process = npb.call();
            int rc = process.waitFor();

            if (!(process instanceof NbNativeProcess)) {
                System.out.println("Test testShellCommandProcess is not applicable for " + process.getClass().getName() + " - skipped");
                return;
            }


            assertEquals("echo with redirection status", 0, rc);

            BufferedReader br = new BufferedReader(new FileReader(out));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            assertEquals("TEST is expected to be written in " + out.getAbsolutePath(), "TEST", sb.toString().trim());
        } finally {
            out.delete();
        }
    }

//    @org.junit.Test
    public void _testShellCommandProcess() throws Exception {
        NativeProcessBuilder npb = NativeProcessBuilder.newLocalProcessBuilder();
        npb.setCommandLine("echo $$ && (echo \"TEST\" | sed 's/E/O/')");
        NativeProcess process = npb.call();
        int rc = process.waitFor();

        String error = ProcessUtils.readProcessErrorLine(process);
        System.out.println(error);

        assertEquals("echo $$ exit status", 0, rc);

        List<String> output = ProcessUtils.readProcessOutput(process);
        int pid = Integer.parseInt(output.get(0));
        assertEquals("shell pid", pid, process.getPID());
        assertEquals("TOST", output.get(1));
    }

    @org.junit.Test
    public void testStartSuspendedProcess() throws Exception {
        RequestProcessor rp = new RequestProcessor("testStartSuspendedProcess", 1);

        NativeProcessBuilder npb = NativeProcessBuilder.newLocalProcessBuilder();
        String tempDir = HostInfoUtils.getHostInfo(env).getTempDirFile().getAbsolutePath();
        npb.setWorkingDirectory(tempDir);
        npb.setExecutable("pwd");
        npb.setInitialSuspend(true);

        final AtomicReference<Integer> status = new AtomicReference<>(null);

        final NativeProcess process = npb.call();

        if (!(process instanceof NbNativeProcess)) {
            System.out.println("Test testStartSuspendedProcess is not applicable for " + process.getClass().getName() + " - skipped");
            return;
        }

        Task waitTask = rp.post(new Runnable() {
            @Override
            public void run() {
                try {
                    status.set(process.waitFor());
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                    status.set(-1);
                }
            }
        });

        Thread.sleep(1000);
        assertTrue("Short process should be already running", status.get() == null);
        CommonTasksSupport.sendSignal(env, process.getPID(), Signal.SIGCONT, null);

        try {
            assertTrue("waitTask must be finished at this point", waitTask.waitFinished(1500));
        } catch (InterruptedException ex) {
            fail("Process must continue after senging the SIGCONT");
        }

        String error = ProcessUtils.readProcessErrorLine(process);
        System.out.println(error);
        String output = ProcessUtils.readProcessOutputLine(process);

        assertEquals("pwd", tempDir, output);
        assertEquals("rc", Integer.valueOf(0), status.get());
    }
}
