/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.api.wizards;

import java.util.Collection;
import java.util.Map;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 */
public final class PreBuildSupport {
    public static final String CMAKE_MACRO = "${CMAKE}"; //NOI18N
    public static final String C_COMPILER_MACRO = "${IDE_CC}"; //NOI18N
    public static final String CPP_COMPILER_MACRO = "${IDE_CXX}"; //NOI18N
    
    public interface PreBuildArtifact {
        public FileObject getScript();
        public String getArguments(ExecutionEnvironment ee, CompilerSet def, String flags);
        public String getCommandLine(String arguments, String workingDir);
        public String validate(ExecutionEnvironment ee, CompilerSet compilerSet);
    }

    public interface PreBuildArtifactProvider {
        PreBuildArtifact findScriptInFolder(FileObject folder, ExecutionEnvironment ee, CompilerSet compilerSet);
        PreBuildArtifact scriptToArtifact(FileObject script);
        boolean isSupported(PreBuildArtifact script);
        String getToolName();
        String getDisplayName();
        String getHint();
        String getFileChooserTitle();
        FileFilter[] getFileFilter();
    }
    
    private PreBuildSupport() {
    }

    public static PreBuildArtifact findArtifactInFolder(FileObject folder, ExecutionEnvironment ee, CompilerSet compilerSet) {
        PreBuildArtifact findConfigureScript = null;
        for (PreBuildArtifactProvider provider : getPreBuildProviders()) {
            findConfigureScript = provider.findScriptInFolder(folder, ee, compilerSet);
            if (findConfigureScript != null) {
                break;
            }
        }
        return findConfigureScript;
    }

    public static PreBuildArtifact scriptToArtifact(FileObject script) {
        PreBuildArtifact findConfigureScript = null;
        for (PreBuildArtifactProvider provider : getPreBuildProviders()) {
            findConfigureScript = provider.scriptToArtifact(script);
            if (findConfigureScript != null) {
                break;
            }
        }
        return findConfigureScript;
    }

    public static PreBuildArtifactProvider getPreBuildProvider(PreBuildArtifact script) {
        for (PreBuildArtifactProvider provider : getPreBuildProviders()) {
            if (provider.isSupported(script)) {
                return provider;
            }
        }
        return null;
    }

    public static Collection<? extends PreBuildArtifactProvider> getPreBuildProviders() {
        Lookup.Result<PreBuildArtifactProvider> res = Lookup.getDefault().lookupResult(PreBuildArtifactProvider.class);
        return res.allInstances();
    }
    
    public static String expandMacros(String command, CompilerSet cs, Map<String, String> env) {
        if (command.contains(CMAKE_MACRO)) {
            String path = getCmakePath(cs);
            command = command.replace(CMAKE_MACRO, path);
        }
        if (command.contains(C_COMPILER_MACRO)) {
            String path = getDefaultC(cs);
            if (env != null && env.containsKey("__CND_C_WRAPPER__")) { //NOI18N
                // use path from wrapper
                path = env.get("__CND_C_WRAPPER__"); //NOI18N
            }
            if (cs.getCompilerFlavor().isCygwinCompiler()) {
                path = WindowsSupport.getInstance().convertToCygwinPath(path);
            }
            if (path != null) {
              command = command.replace(C_COMPILER_MACRO, path);
            }
        }
        if (command.contains(CPP_COMPILER_MACRO)) {
            String path = getDefaultCpp(cs);
            if (env != null && env.containsKey("__CND_CPP_WRAPPER__")) { //NOI18N
                // use path from wrapper
                path = env.get("__CND_CPP_WRAPPER__"); //NOI18N
            }
            if (cs.getCompilerFlavor().isCygwinCompiler()) {
                path = WindowsSupport.getInstance().convertToCygwinPath(path);
            }
            if (path != null) {
              command = command.replace(CPP_COMPILER_MACRO, path);
            }
        }
        return command;
    }
    
    public static String getCmakePath(CompilerSet cs) {
        String toolPath = getToolPath(cs, PredefinedToolKind.CMakeTool);
        if (toolPath == null || toolPath.isEmpty()) {
            toolPath = "cmake"; //NOI18N
        }
        return toolPath;
    }
    
    private static String getDefaultC(CompilerSet compilerSet){
        String cCompiler = getToolPath(compilerSet, PredefinedToolKind.CCompiler);
        if (cCompiler != null) {
            return cCompiler;
        }
        cCompiler = "gcc"; // NOI18N
        if (compilerSet != null) {
            CompilerFlavor flavor = compilerSet.getCompilerFlavor();
            if (flavor.isSunStudioCompiler()) {
                cCompiler = "cc"; // NOI18N
            }
        }
        return cCompiler;
    }

    private static String getDefaultCpp(CompilerSet compilerSet){
        String cppCompiler = getToolPath(compilerSet, PredefinedToolKind.CCCompiler);
        if (cppCompiler != null) {
            return cppCompiler;
        }
        cppCompiler = "g++"; // NOI18N
        if (compilerSet != null) {
            CompilerFlavor flavor = compilerSet.getCompilerFlavor();
            if (flavor.isSunStudioCompiler()) {
                cppCompiler = "CC"; // NOI18N
            }
        }
        return cppCompiler;
    }

    private static String getToolPath(CompilerSet compilerSet, PredefinedToolKind tool){
        if (compilerSet == null) {
            return null;
        }
        Tool compiler = compilerSet.findTool(tool);
        if (compiler == null) {
            return null;
        }
        return escapePath(compiler.getPath());
    }

    private static String escapePath(String path) {
        path = path.replace("\\", "/"); // NOI18N
        if ((path.indexOf(' ') > 0 || path.indexOf('=') > 0)&& !path.startsWith("\"")) { // NOI18N
            path = "\""+path+"\""; // NOI18N
        }
        return path;
    }
}
