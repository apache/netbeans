/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
        res = res && NbStartUtility.getInstance().isSupported(env);
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
