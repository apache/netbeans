/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.toolchain.compilers;

import java.io.BufferedReader;
import java.io.IOException;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager;
import org.netbeans.modules.cnd.spi.utils.CndNotifier;
import org.netbeans.modules.cnd.toolchain.compilers.FingerprintScanner.Result;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
//import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
//import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 */
/*package*/ abstract class OracleCCppCompiler  extends CCCCompiler {
    
    protected OracleCCppCompiler(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
        super(env, flavor, kind, name, displayName, path);
    }

    protected String getCompilerFingerPrintCommand() {
        ToolchainManager.CompilerDescriptor compiler = getDescriptor();
        if (compiler != null) {
            return " " + compiler.getFingerPrintFlags(); // NOI18N
        }
        return null;
    }

    @Override
    protected CCCCompiler.CompilerDefinitions getFreshCompilerDefinitions() {
        CCCCompiler.CompilerDefinitions res = new CCCCompiler.CompilerDefinitions();
        try {
            getSystemIncludesAndDefines(getCompilerFingerPrintCommand(), true, res);
            completePredefinedMacros(res);
        } catch (IOException ioe) {
            System.err.println("IOException " + ioe);
            String errormsg;
            if (getExecutionEnvironment().isLocal()) {
                errormsg = NbBundle.getMessage(getClass(), "CANTFINDCOMPILER", getPath()); // NOI18N
            } else {
                errormsg = NbBundle.getMessage(getClass(), "CANT_FIND_REMOTE_COMPILER", getPath(), getExecutionEnvironment().getDisplayName()); // NOI18N
            }
            CndNotifier.getDefault().notifyErrorLater(errormsg);
//            if (CndUtils.isStandalone()) {
//                System.err.println(errormsg);
//            } else {
//                DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
//            }
        }
        checkModel(res, getCallable());
        
        return res;
    }
    
    @Override
    protected MyCallable<CompilerDefinitions> getCallable(){
        return new MyCallable<CompilerDefinitions>() {

            @Override
            public CompilerDefinitions call(String p) {
                CompilerDefinitions tmp = new CompilerDefinitions();
                try {
                    getSystemIncludesAndDefines(getCompilerFingerPrintCommand()+" "+p, true, tmp); // NOI18N
                    completePredefinedMacros(tmp);
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
                return tmp;
            }
        };
    }

    @Override
    protected void parseCompilerOutput(BufferedReader reader, CCCCompiler.CompilerDefinitions pair) {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                Result res = FingerprintScanner.scaneLine(line);
                if (res != null) {
                    switch(res.getKind()) {
                        case SystemMacro:
                            addUnique(pair.systemPreprocessorSymbolsList, res.getResult());
                            break;
                        case SystemPath:
                            addUnique(pair.systemIncludeDirectoriesList, applyPathPrefix(res.getResult()));
                            break;
                        case SystemIncludeHeader:
                            addUnique(pair.systemIncludeHeadersList, applyPathPrefix(res.getResult()));
                            break;
                    }
                }
            }
            reader.close();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe); // FIXUP
        }
    }   
}
