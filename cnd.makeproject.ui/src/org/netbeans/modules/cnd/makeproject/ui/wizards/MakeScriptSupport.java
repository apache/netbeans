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
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.wizards.BuildSupport.BuildFile;
import org.netbeans.modules.cnd.makeproject.api.wizards.BuildSupport.BuildFileProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = BuildFileProvider.class, position = 5000)
public class MakeScriptSupport implements BuildFileProvider {
    private static final String pattern[] = new String[]{"GNUmakefile", "makefile", "Makefile"}; // NOI18N

    @Override
    public BuildFile findBuildFileInFolder(FileObject folder, ExecutionEnvironment ee, CompilerSet compilerSet) {
        if (folder == null || !(folder.isFolder() && folder.canRead())) {
            return null;
        }
        for (String name : pattern) {
            FileObject makeFO = folder.getFileObject(name);
            if (makeFO != null && makeFO.isValid() && makeFO.isData() && makeFO.canRead()) {
                return new MakeScriptArtifact(makeFO.getPath());
            }
        }
        final FileObject[] listFiles = folder.getChildren();
        if (listFiles == null) {
            return null;
        }
        for(FileObject file : listFiles){
            if (file.getExt().equals("mk")){ // NOI18N
                return new MakeScriptArtifact(file.getPath());
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
                    return new MakeScriptArtifact(script);
                }
            }
            if (name.endsWith(".mk")) { // NOI18N
                return new MakeScriptArtifact(script);
            }
        }
        return null;
    }

    @Override
    public boolean isSupported(BuildFile script) {
        return script instanceof MakeScriptArtifact;
    }

    @Override
    public FileFilter[] getFileFilter() {
        return new FileFilter[]{FileFilterFactory.getMakefileFileFilter()};
    }

    @Override
    public String getHint() {
        return NbBundle.getMessage(SelectModePanel.class, "SelectModeSimpleInstructionExtraText_Make"); // NOI18N
    }

    private static final class MakeScriptArtifact implements BuildFile {
        private final String script;
        
        MakeScriptArtifact(String script) {
            this.script = script;
        }

        @Override
        public String getFile() {
            return script;
        }

        @Override
        public String getCleanCommandLine(String arguments, String workingDir) {
            String res;
            String name = CndPathUtilities.getBaseName(script);
            if (name == null) {
                res = MakeArtifact.MAKE_MACRO+" clean"; // NOI18N
            } else {
                res = MakeArtifact.MAKE_MACRO+" -f "+name+" clean"; // NOI18N
            }
            if (arguments != null && !arguments.isEmpty()) {
                res+=" "+arguments; // NOI18N
            }
            return res;
        }

        @Override
        public String getBuildCommandLine(String arguments, String workingDir) {
            String res;
            String name = CndPathUtilities.getBaseName(script);
            if (name == null) {
                res = MakeArtifact.MAKE_MACRO;
            } else {
                res = MakeArtifact.MAKE_MACRO+" -f "+name; // NOI18N
            }
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
}
