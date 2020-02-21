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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.cncppunit;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.LinkerDescriptor;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.LinkSupport;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.openide.util.Utilities;

/**
 */
public class LibraryChecker {

    private LibraryChecker() {
    }

    /**
     * Checks if compiler can find given library, i.e. that compilation with
     * <code>-l<i>lib</i></code> can succeed.
     *
     * @param lib  library to check
     * @param compiler  compiler to check
     * @return <code>true</code> if compiler can find the library,
     *      <code>false</code> otherwise
     * @throws IOException if there is a problem launching compiler,
     *      or creating temp files, or connecting to remote host
     * @throws IllegalArgumentException if compiler is not a C or C++ compiler
     * @throws CancellationException if remote connection was required,
     *      but user cancelled it
     */
    public static boolean isLibraryAvailable(String lib, AbstractCompiler compiler) throws IOException {
        ExecutionEnvironment execEnv = compiler.getExecutionEnvironment();
        CompilerSet compilerSet = compiler.getCompilerSet();
        LinkerDescriptor linker = compilerSet.getCompilerFlavor().getToolchainDescriptor().getLinker();
        String dummySourceFile = null;
        try {
            dummySourceFile = createDummySourceFile(execEnv, compiler.getKind());
            String linkerPath = getLinker(compiler, compilerSet).getPath();
            String dummySourcePath = dummySourceFile;
            if (execEnv.isLocal() && Utilities.isWindows()) {
                linkerPath = LinkSupport.resolveWindowsLink(linkerPath);
                dummySourcePath = convertToCompilerPath(dummySourcePath, compilerSet);
                if (dummySourcePath == null) {
                    return false;
                }
            }

            List<String> args = new ArrayList<>();
            // linker.getOutputFileFlag() can actually give several flags separated with spaces,
            // e.g. for Solaris Studio on Linux it is "-compat=g -o"
            args.addAll(Arrays.asList(Utilities.parseParameters(linker.getOutputFileFlag())));
            args.addAll(Arrays.asList(dummySourcePath + ".out", linker.getLibraryFlag() + lib, dummySourcePath)); // NOI18N

            NativeProcessBuilder processBuilder = NativeProcessBuilder.newProcessBuilder(execEnv);
            processBuilder.setExecutable(linkerPath);
            processBuilder.setArguments(args.toArray(new String[args.size()]));

            ProcessUtils.ExitStatus res = ProcessUtils.execute(processBuilder);
            if (!res.isOK() && CndUtils.isUnitTestMode()) {
                StringBuilder buf = new StringBuilder("Command\n#"); // NOI18N
                buf.append(linkerPath).append(' ');
                for(String a : args) {
                    buf.append(a).append(' ');
                }
                buf.append('\n').append(res.toString());
                System.err.println(buf.toString());
            }
            return res.isOK();
        } catch (CancellationException ex) {
            return false; // TODO:CancellationException error processing
        } finally {
            if (dummySourceFile != null) {
                if (execEnv.isLocal()) {
                    new File(dummySourceFile).delete();
                    new File(dummySourceFile + ".out").delete(); // NOI18N
                } else {
                    CommonTasksSupport.rmFile(execEnv, dummySourceFile, null);
                    CommonTasksSupport.rmFile(execEnv, dummySourceFile + ".out", null); // NOI18N
                }
            }
        }
    }

    private static String createDummySourceFile(ExecutionEnvironment execEnv, ToolKind compilerKind) throws IOException, CancellationException {
        String ext;
        if (compilerKind == PredefinedToolKind.CCompiler) {
            ext = ".c"; // NOI18N
        } else if (compilerKind == PredefinedToolKind.CCCompiler) {
            ext = ".cpp"; // NOI18N
        } else {
            throw new IllegalArgumentException("Illegal tool kind " + compilerKind); // NOI18N
        }

        HostInfo localHostInfo = HostInfoUtils.getHostInfo(ExecutionEnvironmentFactory.getLocal());
        File dummyFile = File.createTempFile("dummy", ext, localHostInfo.getTempDirFile()); // NOI18N
        try {
            Writer writer = Files.newBufferedWriter(dummyFile.toPath(), Charset.forName("UTF-8")); //NOI18N
            try {
                writer.write("int main(int argc, char** argv) { return 0; }\n"); // NOI18N
            } finally {
                writer.close();
            }

            if (execEnv.isLocal()) {
                return dummyFile.getCanonicalPath();
            }

            HostInfo remoteHostInfo = HostInfoUtils.getHostInfo(execEnv);
            String remoteDummyPath = remoteHostInfo.getTempDir() + '/' + dummyFile.getName();
            CommonTasksSupport.uploadFile(dummyFile, execEnv, remoteDummyPath, 0644).get(); // is it OK not to check upload exit code?
            dummyFile.delete();
            return remoteDummyPath;

        } catch (Throwable ex) {
            dummyFile.delete();
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else if (ex instanceof Error) {
                throw (Error) ex;
            } else if (ex instanceof IOException) {
                throw (IOException) ex;
            } else {
                throw new IOException(ex);
            }
        }
    }

    private static AbstractCompiler getLinker(AbstractCompiler compiler, CompilerSet compilerSet) {
        String preferred = compilerSet.getCompilerFlavor().getToolchainDescriptor().getLinker().getPreferredCompiler();
        if ("c".equals(preferred)) { // NOI18N
            return (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCompiler);
        } else if ("cpp".equals(preferred)) { // NOI18N
            return (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCCompiler);
        } else {
            return compiler;
        }
    }

    private static String convertToCompilerPath(String path, CompilerSet compilerSet) {
        CompilerFlavor flavor = compilerSet.getCompilerFlavor();
        if (flavor.isCygwinCompiler()) {
            return WindowsSupport.getInstance().convertToCygwinPath(path);
            // looks like MinGW gcc does not need path conversion
            // } else if (flavor.isMinGWCompiler()) {
            // return WindowsSupport.getInstance().convertToMSysPath(path);
        } else {
            return path;
        }
    }
}
