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
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
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
@ServiceProvider(service = PreBuildArtifactProvider.class, position = 2000)
public class QmakeScriptSupport implements PreBuildArtifactProvider {

    @Override
    public PreBuildArtifact findScriptInFolder(FileObject folder, ExecutionEnvironment ee, CompilerSet compilerSet) {
        if (folder == null) {
            return null;
        }
        final FileObject[] listFiles = folder.getChildren();
        if (listFiles == null) {
            return null;
        }
        for(FileObject file : listFiles){
            if (file.getExt().equals("pro")){ // NOI18N
                QmakeScriptArtifact res = new QmakeScriptArtifact(file);
                if (res.validate(ee, compilerSet) == null) {
                    return res;
                }
                break;
            }
        }
        return null;
    }

    @Override
    public PreBuildArtifact scriptToArtifact(FileObject script) {
        if (script.getExt().equals("pro")){ // NOI18N
            return new QmakeScriptArtifact(script);
        }
        return null;
    }
    
    @Override
    public String getToolName() {
        return "qmake"; //NOI18N
    }
    
    @Override
    public boolean isSupported(PreBuildArtifact script) {
        return script instanceof QmakeScriptArtifact;
    }

    @Override
    public String getDisplayName() {
         return NbBundle.getMessage(QmakeScriptSupport.class, "ScriptTypeDisplayName_qmake"); //NOI18N
    }

    @Override
    public String getHint() {
        return NbBundle.getMessage(QmakeScriptSupport.class, "SelectModeSimpleInstructionExtraText_QMake"); // NOI18N
    }

    @Override
    public String getFileChooserTitle() {
        return NbBundle.getMessage(QmakeScriptSupport.class, "ScriptTypeFileChooser_qmake"); //NOI18N
    }

    @Override
    public FileFilter[] getFileFilter() {
        return new FileFilter[]{FileFilterFactory.getQMakeFileFilter()};
    }

    private static final class QmakeScriptArtifact implements PreBuildArtifact {
        private final FileObject script;
        
        QmakeScriptArtifact(FileObject script) {
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
            int platform = CompilerSetManager.get(ee).getPlatform();
            if (def.getCompilerFlavor().isSunStudioCompiler() && (platform == PlatformTypes.PLATFORM_SOLARIS_INTEL || platform == PlatformTypes.PLATFORM_SOLARIS_SPARC)) {
                ConfigureScriptSupport.appendIfNeed("-spec ", flags, buf, "solaris-cc"); // NOI18N
            }
            if (platform == PlatformTypes.PLATFORM_MACOSX) {
                ConfigureScriptSupport.appendIfNeed("-spec ", flags, buf, "macx-g++"); // NOI18N
            }
            ConfigureScriptSupport.appendIfNeed("QMAKE_CC=", flags, buf, PreBuildSupport.C_COMPILER_MACRO); // NOI18N
            ConfigureScriptSupport.appendIfNeed("QMAKE_CXX=", flags, buf, PreBuildSupport.CPP_COMPILER_MACRO); // NOI18N
            ConfigureScriptSupport.appendIfNeed("QMAKE_CFLAGS=", flags, buf, cCompilerFlags); // NOI18N
            ConfigureScriptSupport.appendIfNeed("QMAKE_CXXFLAGS=", flags, buf, cppCompilerFlags); // NOI18N
            return buf.toString();
        }

        @Override
        public String getCommandLine(String arguments, String workingDir) {
            StringBuilder buf = new StringBuilder();
            buf.append("qmake "); //NOI18N
            buf.append(CndPathUtilities.toRelativePath(workingDir, script.getPath()));
            buf.append(' ');
            buf.append(arguments);
            return buf.toString();
        }

        @Override
        public String validate(ExecutionEnvironment ee, CompilerSet compilerSet) {
            if (script.isValid() && script.isData() && script.canRead()) {
                DataObject dObj;
                try {
                    dObj = DataObject.find(script);
                } catch (DataObjectNotFoundException ex) {
                    return NbBundle.getMessage(QmakeScriptSupport.class, "CONFIGUREFILEISNOTEXECUTABLE"); //NOI18N
                }
                if (dObj == null) {
                    return NbBundle.getMessage(QmakeScriptSupport.class, "CONFIGUREFILEISNOTEXECUTABLE"); //NOI18N
                }
                Node node = dObj.getNodeDelegate();
                if (node == null) {
                    return NbBundle.getMessage(QmakeScriptSupport.class, "CONFIGUREFILEISNOTEXECUTABLE"); //NOI18N
                }
                if (compilerSet != null) {
                    Tool tool = compilerSet.findTool(PredefinedToolKind.QMakeTool);
                    if (tool != null && !tool.getPath().isEmpty()) {
                        return null;
                    }
                    return NbBundle.getMessage(QmakeScriptSupport.class, "NotFoundQMakeTool", compilerSet.getName()); //NOI18N
                }
                return NbBundle.getMessage(QmakeScriptSupport.class, "NotFoundQMakeTool", ""); //NOI18N
            }
            return NbBundle.getMessage(QmakeScriptSupport.class, "CONFIGUREFILEISNOTEXECUTABLE"); //NOI18N
        }
    }
}
