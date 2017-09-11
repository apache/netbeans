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
