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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import junit.framework.Test;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.remote.test.RemoteTestBase;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class RfsRemoteControllerSkewTestCase extends RemoteTestBase {

    public RfsRemoteControllerSkewTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupHost(getTestExecutionEnvironment());
    }

    @ForAllEnvironments
    public void testRfsRemoteControllerSkew() throws Exception {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        String rcPath = RfsSetupProvider.getControllerPath(env);
        assertTrue("null remote rfs_controller path", rcPath != null);

        NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(env);
        pb.setExecutable(rcPath); //I18N
        pb.setWorkingDirectory("/tmp");
        pb.getEnvironment().put("RFS_CONTROLLER_TRACE", "1"); // NOI18N
        NativeProcess controller = pb.call();

        RequestProcessor.getDefault().post(new ProcessReader(controller.getErrorStream(),
                ProcessUtils.getWriter(System.err, true)));

        PrintWriter responseStream = new PrintWriter(controller.getOutputStream());

        int pid = controller.getPID();
        printf("launched rfs_controller at %s: PID %d PATH %s\n", env, pid, rcPath);
        responseStream.printf("VERSION=%c\n", RfsLocalController.testGetVersion());
        responseStream.flush();

        BufferedReader requestReader = new BufferedReader(new InputStreamReader(controller.getInputStream()));
        String line;
        line = requestReader.readLine();
        printf("%s\n",line);

        final int cnt = 10;
        responseStream.printf("SKEW_COUNT=%d\n", cnt); //NOI18N
        responseStream.flush();

        long skew;
        for (int i = 0; i < cnt; i++) {
            long localTime1 = System.currentTimeMillis();
            responseStream.printf("SKEW %d\n", i); //NOI18N
            responseStream.flush();
            line = requestReader.readLine();
            printf(line);
            long localTime2 = System.currentTimeMillis();
            long remoteTime = Long.parseLong(line);
            long travelTime = (localTime2 - localTime1) /2;
            skew = remoteTime - (localTime1 + localTime2)/ 2;
            printf("L1=%d L2=%d R=%d S=%d\n", localTime1, localTime2, remoteTime, skew);
        }
        responseStream.printf("SKEW_END\n"); //NOI18N
        responseStream.printf("\n"); //NOI18N
        responseStream.flush();

        line = requestReader.readLine();
        printf(line);

        sleep(1000);
        controller.destroy();
        sleep(1000);
    }

    private static void printf(String format, Object... args) {
        format = String.format("RFS_CONTROLLER_SKEW_TEST: %s", format);
        System.err.printf(format, args);
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(RfsRemoteControllerSkewTestCase.class);
    }
}
