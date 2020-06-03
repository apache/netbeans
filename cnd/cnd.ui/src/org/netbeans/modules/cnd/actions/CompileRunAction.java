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
package org.netbeans.modules.cnd.actions;

import java.util.List;
import org.netbeans.modules.cnd.api.utils.ImportUtils;
import org.netbeans.modules.cnd.execution.CompileExecSupport;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService;
import org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer;
import org.openide.nodes.Node;
import org.openide.windows.InputOutput;

/**
 *
 */
public class CompileRunAction extends CompileRunActionBase {

    public CompileRunAction() {
        super.putValue("key", "CndCompileRunAction");// NOI18N
    }
    
    @Override
    public String getName() {
        return getString("BTN_CompileRun_File"); // NOI18N
    }

    @Override
    protected String getTabName(Node node, ExecutionEnvironment execEnv) {
        return execEnv.isLocal() ? getString("COMPILE_RUN_LABEL", node.getName()) : getString("COMPILE_RUN_REMOTE_LABEL", node.getName(), execEnv.getDisplayName()); // NOI18N
    }
    
    @Override
    protected Runnable getRunnable(String tabName, InputOutput tab, ExecutionEnvironment execEnv, String buildDir, String executable, CompileExecSupport ces) {
        return new RunContext(tab, execEnv, buildDir, executable, ces);
    }
    
    private static final class RunContext implements Runnable {

        private final InputOutput tab;
        private final ExecutionEnvironment execEnv;
        private final String buildDir;
        private final String executable;
        private final CompileExecSupport ces;

        private RunContext(InputOutput tab, ExecutionEnvironment execEnv, String buildDir, String executable, CompileExecSupport ces) {
            this.tab = tab;
            this.execEnv = execEnv;
            this.buildDir = buildDir;
            this.executable = executable;
            this.ces = ces;
        }

        @Override
        public void run() {
            NativeProcessBuilder npb = NativeProcessBuilder.
                    newProcessBuilder(execEnv).
                    setWorkingDirectory(buildDir).
                    unbufferOutput(false).
                    setExecutable(buildDir + "/" + executable); // NOI18N
            StringBuilder buf = new StringBuilder();
            for (String arg : ces.getArguments()) {
                if (buf.length() > 0) {
                    buf.append(' ');
                }
                buf.append(arg);
            }
            List<String> list = ImportUtils.parseArgs(buf.toString());
            list = ImportUtils.normalizeParameters(list);
            npb.setArguments(list.toArray(new String[list.size()]));
            NativeExecutionDescriptor descr = new NativeExecutionDescriptor().
                    controllable(true).
                    frontWindow(true).
                    inputVisible(true).
                    inputOutput(tab).
                    outLineBased(true).
                    showProgress(!CndUtils.isStandalone()).
                    postMessageDisplayer(new PostMessageDisplayer.Default("Run")); // NOI18N
            NativeExecutionService.newService(npb, descr, "Run").run(); // NOI18N
        }
    }
}
