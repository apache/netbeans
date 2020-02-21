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
