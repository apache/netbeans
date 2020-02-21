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

package org.netbeans.modules.cnd.makeproject.execution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport;
import org.netbeans.modules.cnd.spi.toolchain.ErrorParserProvider;
import org.netbeans.modules.cnd.spi.toolchain.ErrorParserProvider.OutputListenerRegistry;
import org.netbeans.modules.cnd.spi.toolchain.ErrorParserProvider.Result;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 */
public final class LDErrorParser implements ErrorParserProvider.ErrorParser {

    // Linux message
    // ERROR: ld.so: object 'libBuildTrace.so' from LD_PRELOAD cannot be preloaded: ignored.
    // Solaris message
    // ld.so.1: ls: fatal: libBuildTrace.so: open failed: No such file or directory
    // Mac message
    // dyld: could not load inserted library: libBuildTrace.dylib

    //private Pattern LD_ERROR = Pattern.compile("ERROR.*ld\\.so.*\\'(.*)\\'.*LD_PRELOAD"); //NOI18N
    //private Pattern LD_FATAL = Pattern.compile("ld\\.so.*fatal: (.*\\.so):"); //NOI18N

    private static final Pattern LD_LIB_BUILD_TRACE = Pattern.compile(".*ld\\.so.*libBuildTrace.so"); //NOI18N
    private static final Pattern LD_LIB_BUILD_TRACE_MAC = Pattern.compile(".*dyld:.*libBuildTrace.dylib"); //NOI18N
    private static final Pattern LD_RFS_PRELOAD = Pattern.compile(".*ld\\.so.*rfs_preload.so"); //NOI18N

    private final ExecutionEnvironment execEnv;
    private final Project project;
    private boolean checkBuildTrace = false;
    private boolean checkRfs = false;
    private boolean isMac = false;

    public LDErrorParser(Project project, CompilerFlavor flavor, ExecutionEnvironment execEnv, FileObject relativeTo) {
        this.execEnv = execEnv;
        this.project = project;
        
        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
            switch (hostInfo.getOSFamily()) {
                case MACOSX:
                    isMac = true;
                    checkBuildTrace = true;
                    checkRfs = false;
                    break;
                case LINUX:
                case SUNOS:
                    checkBuildTrace = true;
                    checkRfs = execEnv.isRemote();
                    break;
                case UNKNOWN:
                case WINDOWS:
                    // unsuported
                    break;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (CancellationException ex) {
            // don't report cancellation exception
        }
    }

    @Override
    public void setOutputListenerRegistry(OutputListenerRegistry regestry) {
    }

    @Override
    public Result handleLine(final String line) {
        if (checkBuildTrace) {
            Matcher m;
            if (isMac) {
                m = LD_LIB_BUILD_TRACE_MAC.matcher(line);
            } else {
                m = LD_LIB_BUILD_TRACE.matcher(line);
            }
            if (m.find()) {
                return new Result() {

                    @Override
                    public boolean result() {
                        return true;
                    }

                    @Override
                    public List<ConvertedLine> converted() {
                        List<ConvertedLine> lines = new ArrayList<>();
                        lines.add(ConvertedLine.forText(line, new OutputListenerBuildTrace(execEnv, project)));
                        return lines;
                    }
                };
            }
        }
        if (checkRfs) {
            Matcher m = LD_RFS_PRELOAD.matcher(line);
            if (m.find()) {
                return new Result() {

                    @Override
                    public boolean result() {
                        return true;
                    }

                    @Override
                    public List<ConvertedLine> converted() {
                        List<ConvertedLine> lines = new ArrayList<>();
                        lines.add(ConvertedLine.forText(line, new OutputListenerRfs(execEnv)));
                        return lines;
                    }
                };
            }
        }
        return null;
    }

    private static final class OutputListenerBuildTrace implements OutputListener {
        private final ExecutionEnvironment execEnv;
        private final Project project;
        private OutputListenerBuildTrace(ExecutionEnvironment execEnv, Project project) {
            this.execEnv = execEnv;
            this.project = project;
        }

        @Override
        public void outputLineSelected(OutputEvent ev) {
        }

        @Override
        public void outputLineAction(OutputEvent ev) {
            ConfirmSupport.getForbidBuildAnalyzerFactory().show(project);
        }

        @Override
        public void outputLineCleared(OutputEvent ev) {
        }
    }

    private static final class OutputListenerRfs implements OutputListener {
        private final ExecutionEnvironment execEnv;
        private OutputListenerRfs(ExecutionEnvironment execEnv) {
            this.execEnv = execEnv;
        }

        @Override
        public void outputLineSelected(OutputEvent ev) {
        }

        @Override
        public void outputLineAction(OutputEvent ev) {
            ConfirmSupport.getResolveRfsLibraryFactory().show(execEnv);
        }

        @Override
        public void outputLineCleared(OutputEvent ev) {
        }
    }

}
