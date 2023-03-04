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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminal;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminalProvider;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.openide.util.Utilities;

/**
 *
 * @author ak119685
 */
public class EnvironmentTest extends NativeExecutionBaseTestCase {

    private static final String nonexistentVarName = "SOME_NEW_VAR";

    public EnvironmentTest(String name) {
        super(name);
    }

    public EnvironmentTest(String name, ExecutionEnvironment execEnv) {
        super(name, execEnv);
    }

    public static Test suite() {
        return new NativeExecutionBaseTestSuite(EnvironmentTest.class);
    }

    @org.junit.Test
    public void testVarsLocal() throws Exception {
        ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.getLocal();

//        _testVars(execEnv, true, true, null);
//        _testVars(execEnv, true, false, null);
        _testVars(execEnv, false, true, null);
        _testVars(execEnv, false, false, null);

        Collection<String> supportedTerminalIDs = Utilities.isWindows()
                ? Arrays.asList("cmd.exe") // NOI18N
                : ExternalTerminalProvider.getSupportedTerminalIDs();

        for (String terminalID : supportedTerminalIDs) {
            ExternalTerminal terminal = ExternalTerminalProvider.getTerminal(execEnv, terminalID);
            if (terminal != null && terminal.isAvailable(execEnv)) {
                terminal = terminal.setPrompt("NO");
                _testVars(execEnv, false, true, terminal);
                _testVars(execEnv, false, false, terminal);
            }
        }
    }

    @org.junit.Test
    @ForAllEnvironments(section = "remote.platforms")
    public void testVars() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        ConnectionManager.getInstance().connectTo(execEnv);

//        _testVars(execEnv, true, true, null);
//        _testVars(execEnv, true, false, null);
        _testVars(execEnv, false, true, null);
        _testVars(execEnv, false, false, null);
    }

    public void _testVars(ExecutionEnvironment execEnv, boolean inPtyMode, boolean unbufferOutput, ExternalTerminal terminal) throws Exception {
        final String id = String.format("=== testVars [@ %s; ptyMode: %d, unbuffer: %d, term: %s] ===",
                execEnv.getDisplayName(),
                inPtyMode ? 1 : 0,
                unbufferOutput ? 1 : 0,
                terminal == null ? "null" : terminal.getID());

        System.out.println("=== START " + id);

        boolean isWindows = execEnv.isLocal() && Utilities.isWindows();

        try {
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            MacroMap env = npb.getEnvironment();

            String home = isWindows ? env.get("USERPROFILE") : env.get("HOME");
            String path = env.get("PATH");

            assertNotNull("Initially environment should be filled with user's home", home);
            assertNotNull("Initially environment should be filled with user's env", path);

            assertNull("Will use var " + nonexistentVarName + " for testing. Should not exist in initial user's env", env.get(nonexistentVarName));
            env.put(nonexistentVarName, "test");

            NativeProcessBuilder npb2 = NativeProcessBuilder.newProcessBuilder(execEnv);
            assertNull("Adding env variabl " + nonexistentVarName + " to one builder should not have effect on other builders.", npb2.getEnvironment().get(nonexistentVarName));

            assertTrue("Touched variables must be marked for export", env.getExportVariablesSet().contains(nonexistentVarName));

            env.prependPathVariable("PATH", "additional path");
            assertTrue("PATH should start with 'additional path'", env.get("PATH").startsWith("additional path"));

            env.put(nonexistentVarName, "$" + nonexistentVarName + " value");

            String cmd = "echo " + nonexistentVarName + "=$" + nonexistentVarName + " && echo PATH=$PATH";

            String tmpFile = null;
            File tmpFileFile = null;
            if (terminal != null) {
                tmpFileFile = File.createTempFile("testVars", "result");
                tmpFile = tmpFileFile.getAbsolutePath();
                tmpFileFile.deleteOnExit();

                if (isWindows) {
                    tmpFile = WindowsSupport.getInstance().convertToShellPath(tmpFile);
                }

                System.out.println("Use tempfile: " + tmpFile);

                cmd = "(" + cmd + ") | tee \"" + tmpFile + "\"";
            }

            HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
            final String shell = hostInfo.getShell();
            npb.setExecutable(shell).setArguments("-c", cmd);
            npb.setUsePty(inPtyMode);
            npb.unbufferOutput(unbufferOutput);
            if (terminal != null) {
                npb.useExternalTerminal(terminal);
            }

            ProcessUtils.ExitStatus res = ProcessUtils.execute(npb);
            if (!res.isOK()) {
                fail("Failed to execute " + shell + " -c " + cmd + " rc=" + res.exitCode + " stderr=" + res.getErrorString());
            }

            List<String> result;

            if (terminal == null) {
                result = res.getOutputLines();
            } else {
                // read result from tmpFile

                if (execEnv.isLocal()) {
                    BufferedReader br = new BufferedReader(new FileReader(tmpFileFile));
                    String s;
                    result = new ArrayList<>();

                    while ((s = br.readLine()) != null) {
                        result.add(s);
                    }
                } else {
                    ExitStatus status = ProcessUtils.execute(execEnv, "cat", tmpFile);
                    assertTrue(status.isOK());
                    result = Arrays.asList(status.getOutputString().split("\n"));
                }
            }

            Iterator<String> it = result.iterator();
            String line;

            line = it.next();
            System.out.println(line);
            assertEquals(nonexistentVarName + "=test value", line);
            line = it.next();
            System.out.println(line);
            if (isWindows) {
                assertTrue(line.startsWith("PATH=additional path"));
            } else {
                assertEquals("PATH=" + env.get("PATH"), line);
            }

        } finally {
            System.out.println("=== DONE " + id);
        }
    }
}
