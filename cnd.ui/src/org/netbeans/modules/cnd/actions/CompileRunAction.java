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
