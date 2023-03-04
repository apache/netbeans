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

import junit.framework.Test;
import org.junit.After;
import org.junit.Before;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;

/**
 *
 * @author akrasny
 */
public final class RedirectErrorTest extends NativeExecutionBaseTestCase {

    public RedirectErrorTest(String name) {
        super(name);
    }

    public RedirectErrorTest(String name, ExecutionEnvironment execEnv) {
        super(name, execEnv);
    }

    public static Test suite() {
        return new NativeExecutionBaseTestSuite(RedirectErrorTest.class);
    }

    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ExecutionEnvironment env = getTestExecutionEnvironment();
        if (env != null) {
            ConnectionManager.getInstance().connect(env);
        }
    }

    @After
    @Override
    protected void tearDown() throws Exception {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        if (env != null) {
            ConnectionManager.getInstance().disconnect(env);
        }
        super.tearDown();
    }

    @org.junit.Test
    @ForAllEnvironments(section = "remote.platforms")
    public void testRedirectError_remote() throws Exception {
        doTestRedirectError(getTestExecutionEnvironment());
    }

    @org.junit.Test
    public void testRedirectError_local() throws Exception {
        doTestRedirectError(ExecutionEnvironmentFactory.getLocal());
    }

    private void doTestRedirectError(ExecutionEnvironment env, boolean cmdLine, boolean tty, boolean redirectError) {
        if (env == null) {
            return;
        }

        System.out.println();
        System.out.println("RedirectErrorTest @ " + env.getDisplayName()); // NOI18N
        System.out.println("\tconfigure builder using " + (cmdLine ? "setCommandLine()" : "setExecutable()")); // NOI18N
        System.out.println("\tconfigure builder " + (redirectError ? "" : "not ") + "to do redirectError()"); // NOI18N
        System.out.println("\tconfigure builder " + (tty ? "" : "not ") + "to be started in a pseudo-terminal"); // NOI18N

        boolean errorRedirect = (tty || redirectError) ? true : false;
        System.out.println("Extected that error goes to " + (errorRedirect ? "output" : "error")); // NOI18N

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);

        if (cmdLine) {
            npb.setCommandLine("wrong"); // NOI18N
        } else {
            npb.setExecutable("wrong"); // NOI18N
        }

        npb.setUsePty(tty);

        if (redirectError) {
            npb.redirectError();
        }

        ExitStatus status = ProcessUtils.execute(npb);
        System.out.println("Result is: "); // NOI18N
        System.out.println(status.toString());

        if (errorRedirect) {
            assertTrue("Output is expected to be in the output stream", !status.getOutputString().isEmpty() && status.getErrorString().isEmpty()); // NOI18N
        } else {
            assertTrue("Output is expected to be in the error stream", status.getOutputString().isEmpty() && !status.getErrorString().isEmpty()); // NOI18N
        }
    }

    private void doTestRedirectError(ExecutionEnvironment env) {
        doTestRedirectError(env, false, false, false);
        doTestRedirectError(env, false, false, true);
        doTestRedirectError(env, false, true, false);
        doTestRedirectError(env, false, true, true);
        doTestRedirectError(env, true, false, false);
        doTestRedirectError(env, true, false, true);
        doTestRedirectError(env, true, true, false);
        doTestRedirectError(env, true, true, true);
    }
}
