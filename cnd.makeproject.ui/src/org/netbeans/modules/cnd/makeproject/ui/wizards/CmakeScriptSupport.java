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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.makeproject.api.wizards.PreBuildSupport;
import org.netbeans.modules.cnd.makeproject.api.wizards.PreBuildSupport.PreBuildArtifact;
import org.netbeans.modules.cnd.makeproject.api.wizards.PreBuildSupport.PreBuildArtifactProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = PreBuildArtifactProvider.class, position = 3000)
public class CmakeScriptSupport implements PreBuildArtifactProvider {

    @Override
    public PreBuildArtifact findScriptInFolder(FileObject folder, ExecutionEnvironment ee, CompilerSet compilerSet) {
        if (folder == null) {
            return null;
        }
        FileObject configure = folder.getFileObject("CMakeLists.txt"); // NOI18N
        if (configure != null && configure.isValid()) {
            CmakeScriptArtifact res = new CmakeScriptArtifact(configure);
            if (res.validate(ee, compilerSet) == null) {
                return res;
            }
        }
        return null;
    }

    @Override
    public PreBuildArtifact scriptToArtifact(FileObject script) {
        if ("CMakeLists.txt".equals(script.getNameExt())){ // NOI18N
            return new CmakeScriptArtifact(script);
        }
        return null;
    }

    @Override
    public String getToolName() {
        return "cmake"; //NOI18N
    }

    @Override
    public boolean isSupported(PreBuildArtifact script) {
        return script instanceof CmakeScriptArtifact;
    }

    @Override
    public String getDisplayName() {
         return NbBundle.getMessage(CmakeScriptSupport.class, "ScriptTypeDisplayName_cmake"); //NOI18N
    }

    @Override
    public String getHint() {
        return NbBundle.getMessage(CmakeScriptSupport.class, "SelectModeSimpleInstructionExtraText_CMake"); // NOI18N
    }

    @Override
    public String getFileChooserTitle() {
        return NbBundle.getMessage(CmakeScriptSupport.class, "ScriptTypeFileChooser_cmake"); //NOI18N
    }

    @Override
    public FileFilter[] getFileFilter() {
        return new FileFilter[]{FileFilterFactory.getCMakeFileFilter()};
    }

    private static final class CmakeScriptArtifact implements PreBuildArtifact {
        private final FileObject script;
        
        CmakeScriptArtifact(FileObject script) {
            this.script = script;
        }

        @Override
        public FileObject getScript() {
            return script;
        }

        @Override
        public String getArguments(ExecutionEnvironment ee, CompilerSet def, String flags) {
            ee = (ee != null) ? ee : ServerList.getDefaultRecord().getExecutionEnvironment();
            def = (def != null) ? def : CompilerSetManager.get(ee).getDefaultCompilerSet();
            StringBuilder buf = new StringBuilder(flags);
            String cCompilerFlags = ConfigureScriptSupport.getCompilerFlags(def);
            String cppCompilerFlags = ConfigureScriptSupport.getCompilerFlags(def);
            ConfigureScriptSupport.appendIfNeed("-G ", flags, buf, "\"Unix Makefiles\""); // NOI18N
            ConfigureScriptSupport.appendIfNeed("-DCMAKE_BUILD_TYPE=", flags, buf, "Debug"); // NOI18N
            ConfigureScriptSupport.appendIfNeed("-DCMAKE_C_COMPILER=", flags, buf, PreBuildSupport.C_COMPILER_MACRO); // NOI18N
            ConfigureScriptSupport.appendIfNeed("-DCMAKE_CXX_COMPILER=", flags, buf, PreBuildSupport.CPP_COMPILER_MACRO); // NOI18N
            ConfigureScriptSupport.appendIfNeed("-DCMAKE_C_FLAGS_DEBUG=", flags, buf, cCompilerFlags); // NOI18N
            ConfigureScriptSupport.appendIfNeed("-DCMAKE_CXX_FLAGS_DEBUG=", flags, buf, cppCompilerFlags); // NOI18N
            ConfigureScriptSupport.appendIfNeed("-DCMAKE_EXPORT_COMPILE_COMMANDS=", flags, buf, "ON"); // NOI18N
            return buf.toString();
        }

        @Override
        public String getCommandLine(String arguments, String workingDir) {
            StringBuilder buf = new StringBuilder();
            buf.append(PreBuildSupport.CMAKE_MACRO+" "); //NOI18N
            buf.append(arguments);
            FileObject parent = script.getParent();
            if (parent != null) {
                buf.append(' '); //NOI18N
                buf.append(CndPathUtilities.toRelativePath(workingDir, parent.getPath()));
            }
            return buf.toString();
        }

        @Override
        public String validate(ExecutionEnvironment ee, CompilerSet compilerSet) {
            if (script.isValid() && script.isData() && script.canRead()) {
                DataObject dObj;
                try {
                    dObj = DataObject.find(script);
                } catch (DataObjectNotFoundException ex) {
                    return NbBundle.getMessage(CmakeScriptSupport.class, "CONFIGUREFILEISNOTEXECUTABLE"); //NOI18N
                }
                if (dObj == null) {
                    return NbBundle.getMessage(CmakeScriptSupport.class, "CONFIGUREFILEISNOTEXECUTABLE"); //NOI18N
                }
                Node node = dObj.getNodeDelegate();
                if (node == null) {
                    return NbBundle.getMessage(CmakeScriptSupport.class, "CONFIGUREFILEISNOTEXECUTABLE"); //NOI18N
                }
                if (compilerSet != null) {
                    Tool tool = compilerSet.findTool(PredefinedToolKind.CMakeTool);
                    if (tool != null && !tool.getPath().isEmpty()) {
                        return null;
                    }
                    return NbBundle.getMessage(QmakeScriptSupport.class, "NotFoundCMakeTool", compilerSet.getName()); //NOI18N
                }
                return NbBundle.getMessage(QmakeScriptSupport.class, "NotFoundCMakeTool", ""); //NOI18N
            }
            return NbBundle.getMessage(CmakeScriptSupport.class, "CONFIGUREFILEISNOTEXECUTABLE"); //NOI18N
        }
    }
}
