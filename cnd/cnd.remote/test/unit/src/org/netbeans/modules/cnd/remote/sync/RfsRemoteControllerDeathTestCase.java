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

package org.netbeans.modules.cnd.remote.sync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import junit.framework.Test;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.remote.test.RemoteTestBase;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class RfsRemoteControllerDeathTestCase extends RemoteTestBase {

    public RfsRemoteControllerDeathTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupHost(getTestExecutionEnvironment());
    }

    @ForAllEnvironments
    public void testRfsRemoteControllerDeath() throws Exception {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        String rcPath = RfsSetupProvider.getControllerPath(env);
        assertTrue("null remote rfs_controller path", rcPath != null);

        NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(env);
        pb.setExecutable(rcPath); //I18N
        pb.setWorkingDirectory("/tmp");
        pb.getEnvironment().put("RFS_CONTROLLER_TRACE", "1"); // NOI18N
        NativeProcess controller = pb.call(); // ProcessUtils.execute doesn't work here

        RequestProcessor.getDefault().post(new ProcessReader(controller.getErrorStream(),
                ProcessUtils.getWriter(System.err, true)));

        RequestProcessor.getDefault().post(new ProcessReader(controller.getInputStream(),
                ProcessUtils.getWriter(System.err, true)));

        final PrintWriter rcWriter = new PrintWriter(controller.getOutputStream());

        int pid = controller.getPID();
        printf("launched rfs_controller at %s: PID %d PATH %s\n", env, pid, rcPath);
        rcWriter.printf("\n");
        rcWriter.flush();
        sleep(500);
        ConnectionManager.getInstance().disconnect(env);
        char[] passwd = NativeExecutionTestSupport.getTestPassword(env);
        assertNotNull("Test password should not be null", passwd);
        PasswordManager.getInstance().storePassword(env, passwd, false);
        ConnectionManager.getInstance().connectTo(env);
        waitDeath(env, rcPath, pid, 22000,
                "The process " + pid + ' ' + rcPath + " at " + env + " did not die after disconnect");
    }

    private static void printf(String format, Object... args) {
        format = String.format("RFS_CONTROLLER_DEATH_TEST: %s", format);
        System.err.printf(format, args);
    }

    private static class ProcessReader implements Runnable {

        private final BufferedReader errorReader;
        private final PrintWriter errorWriter;

        public ProcessReader(InputStream errorStream, PrintWriter errorWriter) {
            this.errorReader = new BufferedReader(new InputStreamReader(errorStream));
            this.errorWriter = errorWriter;
        }
        @Override
        public void run() {
            try {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    if (errorWriter != null) {
                         errorWriter.println(line);
                    }
                    RemoteUtil.LOGGER.fine(line);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private boolean isAlive(ExecutionEnvironment env, String rcPath, int pid) {
        ExitStatus pgrepRes = ProcessUtils.execute(env, "sh", "-c", "ps -ef | grep " + rcPath);
        //printf("ps -ef | grep %s OUT:\n%s\n", rcPath, pgrepRes.output);
        //printf("ps -ef | grep %s ERR:\n%s\n", rcPath, pgrepRes.error);
        //printf("ps -ef | grep %s RC: %d\n", rcPath, pgrepRes.exitCode);
        if (pgrepRes.getOutputString().contains(rcPath)) {
            boolean found = false;
            for (String line : pgrepRes.getOutputString().split("\n")) {
                String[] parts = line.split(" +");
                if (parts.length > 1 && parts[1].equals(Integer.toString(pid))) {
                    found = true;
                    break;
                }
            }
            return found;
        }
        return false;
    }

    private void waitDeath(ExecutionEnvironment env, String rcPath, int pid, long timeout, String failureMessage) {
        long now = System.currentTimeMillis();
        long end = now + timeout;
        while (now < end && isAlive(env, rcPath, pid)) {
            sleep(1000);
            now = System.currentTimeMillis();
        }
        if (now >= end) {
            ProcessUtils.execute(env, "kill", Integer.toString(pid));
            fail(failureMessage);
        }
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(RfsRemoteControllerDeathTestCase.class);
    }
}
