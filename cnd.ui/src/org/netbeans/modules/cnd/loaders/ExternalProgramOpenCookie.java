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
package org.netbeans.modules.cnd.loaders;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.spi.toolchain.ToolchainProject;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * {@link OpenCookie} implementation that launches external program
 * with DataObject's primary file.
 *
 */
/*package*/ final class ExternalProgramOpenCookie implements OpenCookie {

    private final DataObject dao;
    private final String[] programs;
    private final String failmsg;

    public ExternalProgramOpenCookie(DataObject dao, String[] programs, String failmsg) {
        Parameters.notNull("dao", dao); // NOI18N
        Parameters.notNull("program", programs); // NOI18N
        this.dao = dao;
        this.programs = programs;
        this.failmsg = failmsg;
    }

    @Override
    public void open() {
        String qmakePath = getQmakePath();
        String tool = null;
        if (qmakePath != null) {
            File dir = new File(qmakePath);
            for(String program : programs) {
                File file;
                if (Utilities.isWindows()) {
                    file = new File(dir, program+".exe"); // NOI18N
                } else {
                    file = new File(dir, program);
                }
                if (file.exists()) {
                    tool = file.getAbsolutePath();
                    break;
                }
            }
        }
        List<String> list = new ArrayList<String>(Arrays.asList(programs));
        if (tool != null) {
            list.add(tool);
        }
        boolean success = false;
        for(String program : list) {
            ProcessBuilder pb = new ProcessBuilder(program, dao.getPrimaryFile().getPath());
            success = ProcessUtils.execute(pb).exitCode >= 0; // previously was true if the process has started

            if (!success && Utilities.isMac()) {
                // On Mac the built-in "open" command can launch installed
                // applications without having them in PATH. This fixes
                // bug #178742 - NetBeans can't launch Qt Designer
                pb = new ProcessBuilder("open", "-a", program, dao.getPrimaryFile().getPath()); // NOI18N
                // "open" exits immediately, it does not wait until
                // launched application finishes, so waitFor() can be safely used
                success = ProcessUtils.execute(pb).isOK();
            }
            if (success) {
                break;
            }
        }
        if (!success && failmsg != null) {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(failmsg));
        }
    }
    
    private String getQmakePath() {
        Project project = FileOwnerQuery.getOwner(dao.getPrimaryFile());
        CompilerSet set = null;
        if (project != null) {
            ToolchainProject toolchain = project.getLookup().lookup(ToolchainProject.class);
            if (toolchain != null) {
                set = toolchain.getCompilerSet();
            }
        }
        if (set == null) {
            set = CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal()).getDefaultCompilerSet();
        }
        if (set != null) {
            Tool qmake = set.findTool(PredefinedToolKind.QMakeTool);
            if (qmake != null) {
                return CndPathUtilities.getDirName(qmake.getPath());
            }
        }
        return null;
    }
}
