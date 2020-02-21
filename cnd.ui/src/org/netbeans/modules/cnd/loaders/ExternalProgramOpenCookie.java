/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
