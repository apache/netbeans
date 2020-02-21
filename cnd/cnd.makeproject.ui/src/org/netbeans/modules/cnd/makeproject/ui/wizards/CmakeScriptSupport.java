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
