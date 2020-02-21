/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
