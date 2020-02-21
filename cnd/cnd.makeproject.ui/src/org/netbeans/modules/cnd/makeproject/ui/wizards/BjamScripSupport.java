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

import java.io.File;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.wizards.BuildSupport.BuildFile;
import org.netbeans.modules.cnd.makeproject.api.wizards.BuildSupport.BuildFileProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FileAndFileObjectFilter;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = BuildFileProvider.class, position = 3000)
public class BjamScripSupport implements BuildFileProvider {
    private static final String pattern[] = new String[]{"b2"}; // NOI18N

    @Override
    public BuildFile findBuildFileInFolder(FileObject folder, ExecutionEnvironment ee, CompilerSet compilerSet) {
        if (folder == null || !(folder.isFolder() && folder.canRead())) {
            return null;
        }
        for (String name : pattern) {
            FileObject makeFO = folder.getFileObject(name);
            if (makeFO != null && makeFO.isValid() && makeFO.isData() && makeFO.canRead()) {
                return new BjamScriptArtifact(makeFO.getPath());
            }
        }
        return null;
    }

    @Override
    public BuildFile scriptToBuildFile(String script) {
        String name = CndPathUtilities.getBaseName(script);
        if (name != null) {
            for (String predefined : pattern) {
                if (predefined.equals(name)) {
                    return new BjamScriptArtifact(script);
                }
            }
        }
        return null;
    }

    @Override
    public boolean isSupported(BuildFile script) {
        return script instanceof BjamScriptArtifact;
    }

    @Override
    public FileFilter[] getFileFilter() {
        return new FileFilter[]{new BjamFileFilter()};
    }

    @Override
    public String getHint() {
        return NbBundle.getMessage(SelectModePanel.class, "SelectModeSimpleInstructionExtraText_Bjam"); // NOI18N
    }
    
    private static final class BjamScriptArtifact implements BuildFile {
        private final String script;
        
        BjamScriptArtifact(String script) {
            this.script = script;
        }

        @Override
        public String getFile() {
            return script;
        }

        @Override
        public String getCleanCommandLine(String arguments, String workingDir) {
            String res = "./b2 --clean"; // NOI18N
            if (arguments != null && !arguments.isEmpty()) {
                res+=" "+arguments; // NOI18N
            }
            return res;
        }

        @Override
        public String getBuildCommandLine(String arguments, String workingDir) {
            String res = "./b2 -a"; // NOI18N
            if (arguments != null && !arguments.isEmpty()) {
                res+=" "+arguments; // NOI18N
            }
            return res;
        }

        @Override
        public String validate(ExecutionEnvironment ee, CompilerSet compilerSet) {
            return null;
        }
    }
    
    private static final class BjamFileFilter extends FileAndFileObjectFilter {

        @Override
        public String getDescription() {
            return NbBundle.getMessage(BjamScripSupport.class, "BjamFileFilter"); // NOI18N
        }

        @Override
        protected boolean mimeAccept(File f) {
            return acceptName(f.getName());
        }

        @Override
        protected boolean mimeAccept(FileObject f) {
            return acceptName(f.getNameExt());
        }

        private boolean acceptName(String name) {
            if(name != null && name.equals("b2")) { // NOI18N
                return true;
            }
            return false;
        }

        @Override
        protected String[] getSuffixes() {
            return new String[]{};
        }
    }
}
