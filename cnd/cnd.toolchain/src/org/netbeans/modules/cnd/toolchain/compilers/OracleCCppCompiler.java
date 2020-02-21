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
