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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution;

import java.util.List;
import junit.framework.Test;
import org.junit.After;
import org.junit.Before;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;

/**
 *
 * @author ak119685
 */
public class RegressionTests extends NativeExecutionBaseTestCase {

    public RegressionTests(String name) {
        super(name);
    }

    public RegressionTests(String name, ExecutionEnvironment execEnv) {
        super(name, execEnv);
    }

    public static Test suite() {
        return new NativeExecutionBaseTestSuite(RegressionTests.class);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @org.junit.Test
    @ForAllEnvironments(section = "remote.platforms")
    public void testIZ177401_remote() throws Exception {
        String goodPath = "/"; // NOI18N
        String badPath = "/some/wrong/path"; // NOI18N

        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        // Make sure that env is connected

        ConnectionManager.getInstance().connectTo(execEnv);

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setWorkingDirectory(goodPath); // NOI18N
        npb.setExecutable("ls"); // NOI18N
        Process lsProcess = npb.call();
        int rc = lsProcess.waitFor();

        System.out.println("rc is " + rc);

        assertTrue(rc == 0);
        List<String> output = ProcessUtils.readProcessOutput(lsProcess);
        System.out.println("Output: " + output);
        System.out.println("Error: " + ProcessUtils.readProcessErrorLine(lsProcess));
        assertTrue(output.contains("bin")); // NOI18N

        npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setWorkingDirectory(badPath); // NOI18N
        npb.setExecutable("ls"); // NOI18N
        lsProcess = npb.call();
        rc = lsProcess.waitFor();

        assertFalse(rc == 0);

        System.out.println("rc is " + rc);
        output = ProcessUtils.readProcessOutput(lsProcess);
        System.out.println("Output: " + output);
        System.out.println("Error: " + ProcessUtils.readProcessErrorLine(lsProcess));
    }

    @org.junit.Test
    public void testIZ177401_local() throws Exception {
        NativeProcessBuilder npb = NativeProcessBuilder.newLocalProcessBuilder();
        String goodPath = "/"; // NOI18N
        String badPath = "/some/wrong/path"; // NOI18N
        npb.setExecutable("ls").setWorkingDirectory(goodPath);

        Process lsProcess = npb.call();
        int rc = lsProcess.waitFor();
        assertTrue(rc == 0);

        System.out.println("rc is " + rc);
        List<String> output = ProcessUtils.readProcessOutput(lsProcess);
        List<String> error = ProcessUtils.readProcessError(lsProcess);
        System.out.println("Output: " + output);
        System.out.println("Error: " + error);

        npb.setExecutable("ls").setWorkingDirectory(badPath);

        lsProcess = npb.call();
        rc = lsProcess.waitFor();
        assertFalse(rc == 0);

        System.out.println("rc is " + rc);
        output = ProcessUtils.readProcessOutput(lsProcess);
        error = ProcessUtils.readProcessError(lsProcess);
        System.out.println("Output: " + output);
        System.out.println("Error: " + error);

        assertFalse(error.isEmpty());
    }
}
