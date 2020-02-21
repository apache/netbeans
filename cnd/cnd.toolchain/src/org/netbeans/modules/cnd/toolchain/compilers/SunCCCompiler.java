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
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.ErrorManager;

/*package*/ class SunCCCompiler extends SunCCCCompiler {
    /** 
     * Creates a new instance of SunCCCompiler
     */
    protected SunCCCompiler(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
        super(env, flavor, kind, name, displayName, path);
    }
    
    @Override
    public SunCCCompiler createCopy(CompilerFlavor flavor) {
        SunCCCompiler copy = new SunCCCompiler(getExecutionEnvironment(), flavor, getKind(), getName(), getDisplayName(), getPath());
        if (isReady()) {
            copy.copySystemIncludeDirectories(getSystemIncludeDirectories());
            copy.copySystemPreprocessorSymbols(getSystemPreprocessorSymbols());
            copy.copySystemIncludeHeaders(getSystemIncludeHeaders());
        }
        return copy;
    }

    public static SunCCCompiler create(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
        return new SunCCCompiler(env, flavor, kind, name, displayName, path);
    }

    @Override
    public CompilerDescriptor getDescriptor() {
        return getFlavor().getToolchainDescriptor().getCpp();
    }
    
    @Override
    protected void parseCompilerOutput(BufferedReader reader, CompilerDefinitions pair) {
        
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                for(String token : getSystemPaths(line)){
                    addUnique(pair.systemIncludeDirectoriesList, applyPathPrefix(token));
                    if (token.endsWith("Cstd")) { // NOI18N
                        // See 89872 "Parser Settings" for Sun Compilers Collection are incorrect
                        addUnique(pair.systemIncludeDirectoriesList, applyPathPrefix(token.substring(0, token.length()-4) + "std")); // NOI18N
                    }
                }
                parseUserMacros(line, pair.systemPreprocessorSymbolsList);
                if (line.startsWith("#define ")) { // NOI18N
                   String[] macro = CCCCompiler.getMacro(line.substring(8).trim());
                   if (CCCCompiler.isValidMacroName(macro[0])) {
                       String token;
                       if (macro[1] != null) {
                            token = macro[0] + "=" + macro[1]; // NOI18N
                       } else {
                           token = macro[0];
                       }
                        addUnique(pair.systemPreprocessorSymbolsList, token);
                    }
                }
            }
            reader.close();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe); // FIXUP
        }
    }   
}
