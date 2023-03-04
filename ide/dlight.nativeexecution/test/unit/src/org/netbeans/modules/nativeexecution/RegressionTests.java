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
