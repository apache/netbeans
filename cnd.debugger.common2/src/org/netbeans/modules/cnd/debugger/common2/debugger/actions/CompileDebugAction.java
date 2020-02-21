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
package org.netbeans.modules.cnd.debugger.common2.debugger.actions;

import java.io.IOException;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.actions.CompileRunActionBase;
import org.netbeans.modules.cnd.api.utils.ImportUtils;
import org.netbeans.modules.cnd.debugger.common2.DbgActionHandler;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.execution.CompileExecSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.openide.nodes.Node;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 */
public class CompileDebugAction extends CompileRunActionBase {
    public CompileDebugAction() {
        super.putValue("key", "CndCompileDebugAction");// NOI18N
    }
    
    @Override
    public String getName() {
        return getString("BTN_CompileDebug_File"); // NOI18N
    }

    @Override
    protected String getTabName(Node node, ExecutionEnvironment execEnv) {
        return execEnv.isLocal() ? getString("COMPILE_DEBUG_LABEL", node.getName()) : getString("COMPILE_DEBUG_REMOTE_LABEL", node.getName(), execEnv.getDisplayName()); // NOI18N
    }
    
    @Override
    protected Runnable getRunnable(String tabName, InputOutput tab, ExecutionEnvironment execEnv, String buildDir, String executable, CompileExecSupport ces) {
        return new DebugContext(tabName, tab, execEnv, buildDir, executable, ces);
    }
    
    private static final class DebugContext implements Runnable {

        private InputOutput tab;
        private final ExecutionEnvironment execEnv;
        private final String buildDir;
        private final String executable;
        private final CompileExecSupport ces;
        private final String tabName;

        private DebugContext(String tabName, InputOutput tab, ExecutionEnvironment execEnv, String buildDir, String executable, CompileExecSupport ces) {
            this.tabName = tabName;
            this.tab = tab;
            this.execEnv = execEnv;
            this.buildDir = buildDir;
            this.executable = executable;
            this.ces = ces;
        }

        @Override
        public void run() {
            final MakeConfiguration configuration = MakeConfiguration.createDefaultHostMakefileConfiguration(buildDir, "Default");// NOI18N
            configuration.getMakefileConfiguration().getOutput().setValue(buildDir + "/" + executable);// NOI18N
            final RunProfile profile = new RunProfile(configuration, null);
            StringBuilder buf = new StringBuilder();
            for (String arg : ces.getArguments()) {
                if (buf.length() > 0) {
                    buf.append(' ');
                }
                buf.append(arg);
            }
            List<String> list = ImportUtils.parseArgs(buf.toString());
            list = ImportUtils.normalizeParameters(list);
            profile.setArgs(list.toArray(new String[list.size()]));
            profile.setBaseDir(buildDir);
            profile.setRunDir(buildDir);
            tab.closeInputOutput();
            tab = IOProvider.get("Terminal").getIO(tabName, false); //NOI18N
            try {
                tab.getOut().reset();
            } catch (IOException ex) {
            }
            
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    DbgActionHandler handler = new DbgActionHandler();
                    handler.addExecutionListener(new ExecutionListener() {

                        @Override
                        public void executionStarted(int pid) {
                            //tab.getOut().println("Start "+executable);
                        }

                        @Override
                        public void executionFinished(int rc) {
                            //tab.getOut().println("Finish "+executable);
                            tab.getOut().close();
                        }
                    });
                    NativeDebuggerManager.get().debug(buildDir + "/" + executable, null, // NOI18N
                            configuration, ExecutionEnvironmentFactory.toUniqueID(execEnv), tab, handler, profile);
                }
            });
        }
    }
}
