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
package org.netbeans.modules.nativeexecution;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author ak119685
 */
public class IZ182478 extends NativeExecutionBaseTestCase {

    public IZ182478(String name) {
        super(name);
    }

    public void test_perform() {
        ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();
        HostInfo info = null;

        try {
            info = HostInfoUtils.getHostInfo(env);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        assertNotNull("HostInfo for localhost is unavailable", info); // NOI18N

        if (info.getOSFamily() != HostInfo.OSFamily.SUNOS) {
            System.out.println("Skip this test on " + info.getOSFamily().name());
            return;
        }

        String pidOfJVM = null;

        try {
            pidOfJVM = new File("/proc/self").getCanonicalFile().getName();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        assertNotNull("Cannot get PID of this JVM", pidOfJVM); // NOI18N

        ExitStatus result = ProcessUtils.execute(env, "/bin/ptree", pidOfJVM);
        assertEquals(true, result.isOK());

        int initialCount = countShells(info.getShell(), result.getOutputString());
        System.out.println("Before the test there are " + initialCount + " instances of " + info.getShell());

        startLoop();

        result = ProcessUtils.execute(env, "/bin/ptree", pidOfJVM);
        assertEquals(true, result.isOK());
        int finalCount = countShells(info.getShell(), result.getOutputString());

        System.out.println("After the test there are " + finalCount + " instances of " + info.getShell());

        assertEquals("Number of shells before and after the test should be equal", initialCount, finalCount);
    }

    private int countShells(String shell, String output) {
        int count = 0;
        int idx = 0;
        while (true) {
            int i = output.indexOf(shell, idx);
            if (i < 0) {
                break;
            }
            count++;
            idx = i + shell.length();
        }

        return count;
    }

    private void startLoop() {
        int count = 30;
        RequestProcessor rp = new RequestProcessor("IZ182478", 1);

        for (int i = 0; i < count; i++) {
            Future task = rp.submit(new Runnable() {

                @Override
                public void run() {
                    NativeProcessBuilder npb = NativeProcessBuilder.newLocalProcessBuilder();
                    npb.setExecutable("/bin/echo").setArguments("XXX");

                    try {
                        npb.call();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                }
            });

            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }

            // true is essential!
            task.cancel(true);
        }

        rp.shutdown();

        try {
            rp.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
