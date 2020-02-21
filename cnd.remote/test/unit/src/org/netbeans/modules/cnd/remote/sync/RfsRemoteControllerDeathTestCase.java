/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
