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
package org.netbeans.modules.cnd.remote.support;

import org.netbeans.modules.cnd.remote.test.RemoteTestBase;
import java.util.Map;
import junit.framework.Test;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class TransportTestCase extends RemoteTestBase {

    public TransportTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testRun() throws Exception {
        final String randomString = "i am just a random string, it does not matter that I mean";
        ProcessUtils.ExitStatus rcs = ProcessUtils.execute(getTestExecutionEnvironment(), "echo", randomString);
        assert rcs.exitCode == 0 : "echo command on remote server '" + getTestExecutionEnvironment() + "' returned " + rcs.exitCode;
        assert randomString.equals( rcs.getOutputString().trim()) : "echo command on remote server '" + getTestExecutionEnvironment() + "' produced unexpected output: " + rcs.getOutputString();
    }

    @ForAllEnvironments
    public void testFileExistst() throws Exception {
        assert HostInfoProvider.fileExists(getTestExecutionEnvironment(), "/etc/passwd");
        assert !HostInfoProvider.fileExists(getTestExecutionEnvironment(), "/etc/passwd/noway");
    }

    @ForAllEnvironments
    public void testGetEnv() throws Exception {
        Map<String, String> env = HostInfoProvider.getEnv(getTestExecutionEnvironment());
        System.err.println("Environment: " + env);
        assert env != null && env.size() > 0;
        assert env.containsKey("PATH") || env.containsKey("Path") || env.containsKey("path");
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(TransportTestCase.class);
    }
}
