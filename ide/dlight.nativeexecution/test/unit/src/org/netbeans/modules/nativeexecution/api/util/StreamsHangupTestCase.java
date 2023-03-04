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
package org.netbeans.modules.nativeexecution.api.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import junit.framework.Test;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import static org.netbeans.modules.nativeexecution.api.util.ProcessUtils.getReader;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;

/**
 *
 * @author vkvashin
 */
public class StreamsHangupTestCase extends NativeExecutionBaseTestCase {

    static {
      System.setProperty("nativeexecution.support.logger.level", "0");
    }

    private String remoteScriptPath = null;
    private static final String scriptName = "err_and_out.sh";
    private final String prefix;
    private String remoteTmpDir = null;

    public StreamsHangupTestCase(String name, ExecutionEnvironment testExecutionEnvironment) {
        super(name, testExecutionEnvironment);
        prefix = "[" + testExecutionEnvironment + "] ";
    }

    private class DelegateImpl implements StreamsHangup.Delegate {

        private volatile File scriptFile;

        @Override
        public void setup(File scriptFile) throws Exception {
            this.scriptFile = scriptFile;
            ExecutionEnvironment env = getTestExecutionEnvironment();
            if (env.isLocal()) {
                scriptFile.setExecutable(true); // just in case
                remoteScriptPath = scriptFile.getAbsolutePath();
                remoteTmpDir = File.createTempFile(getClass().getSimpleName(), ".dat").getAbsolutePath();
            } else {
                ConnectionManager.getInstance().connectTo(env);
                remoteTmpDir = createRemoteTmpDir();
                int rc = CommonTasksSupport.rmDir(env, remoteTmpDir, true, new PrintWriter(System.err)).get();
                remoteScriptPath = remoteTmpDir + "/" + scriptFile.getName();
                CommonTasksSupport.UploadStatus res = CommonTasksSupport.uploadFile(scriptFile, env, remoteScriptPath, 0777, true).get();
                assertEquals("Error uploading file " + scriptFile.getAbsolutePath() + " to " + getTestExecutionEnvironment() + ":" + remoteScriptPath, 0, rc);
            }
        }


        @Override
        public Process createProcess(String... arguments) throws IOException {
            assertNotNull(remoteScriptPath);
            final ExecutionEnvironment env = getTestExecutionEnvironment();
            NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(env);
            pb.setExecutable(remoteScriptPath);
            pb.setArguments(arguments);
            NativeProcess process = pb.call();
            return process;
        }

        @Override
        public void cleanup() throws Exception {
            if (remoteTmpDir != null) {
                ExecutionEnvironment env = getTestExecutionEnvironment();
                if (env.isRemote()) { // in the case of local, we don't copy anything
                    CommonTasksSupport.rmDir(env, remoteTmpDir, true, new PrintWriter(System.err)).get();
                }
            }
        }

        @Override
        public String getScriptName() {
            return scriptName;
        }

        @Override
        public String getPrefix() {
            return prefix;
        }

        @Override
        public void kill(Process process) throws IOException {
            final ExecutionEnvironment env = getTestExecutionEnvironment();
            int pid = ((NativeProcess) process).getPID();
            ProcessUtils.execute(env, "kill", "-9", "" + pid);        }
    }


    @ForAllEnvironments(section = "remote.platforms")
    public void testHangup() throws Exception {
        new StreamsHangup(new DelegateImpl()).test();
    }
    
    public static Test suite() {
        return new NativeExecutionBaseTestSuite(StreamsHangupTestCase.class);
    }
}
