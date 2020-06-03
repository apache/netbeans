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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.cnd.spi.utils.CndNotifier;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
//import org.openide.DialogDisplayer;
//import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/*package*/ abstract class SunCCCCompiler extends CCCCompiler {
    
    protected SunCCCCompiler(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
        super(env, flavor, kind, name, displayName, path);
    }

    protected String getCompilerStderrCommand() {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null) {
            return " " + compiler.getIncludeFlags(); // NOI18N
        }
        return null;
    }

    protected String getCompilerStderrCommand2() {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null) {
            return " " + compiler.getMacroFlags(); // NOI18N
        }
        return null;
    }
    
    @Override
    protected CompilerDefinitions getFreshCompilerDefinitions() {
        CompilerDefinitions res = new CompilerDefinitions();
        try {
            getSystemIncludesAndDefines(getCompilerStderrCommand(), false, res);
            if (getCompilerStderrCommand2() != null) {
                getSystemIncludesAndDefines(getCompilerStderrCommand2(), false, res);
            }
            addUnique(res.systemIncludeDirectoriesList, applyPathPrefix("/usr/include")); // NOI18N
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
                    getSystemIncludesAndDefines(getCompilerStderrCommand()+" "+p, false, tmp); // NOI18N
                    if (getCompilerStderrCommand2() != null) {
                        getSystemIncludesAndDefines(getCompilerStderrCommand2()+" "+p, false, tmp); // NOI18N
                    }
                    addUnique(tmp.systemIncludeDirectoriesList, applyPathPrefix("/usr/include")); // NOI18N
                    completePredefinedMacros(tmp);
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
                return tmp;
            }
        };
    }
    
    protected Collection<String> getSystemPaths(String line) {
        List<String> res =getIncludePaths(line, "-include"); // NOI18N
        res.addAll(getIncludePaths(line, "-I")); // NOI18N
        return res;
    }

    private List<String> getIncludePaths(String line, String prefix) {
        List<String> res = new ArrayList<String>();
        int includeIndex = line.indexOf(prefix); // NOI18N
        while (includeIndex > 0) {
            String token;
            int rest = includeIndex+prefix.length();
            if (line.charAt(includeIndex+prefix.length()) == ' ') {
                rest++;
            }
            int spaceIndex = line.indexOf(' ', rest); // NOI18N
            if (spaceIndex > 0) {
                token = line.substring(rest, spaceIndex);
            } else {
                token = line.substring(rest);
            }
            if (!token.equals("-xbuiltin")) { //NOI18N
                res.add(token);
            }
            if (spaceIndex < 0) {
                break;
            }
            includeIndex = line.indexOf(prefix, spaceIndex); // NOI18N
        }
        return res;
    }
}
