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

/*package*/ final class SunCCompiler extends SunCCCCompiler {
    /**
     * Creates a new instance of SunCCompiler
     * private: use factory methods instead
     */
    private SunCCompiler(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
        super(env, flavor, kind, name, displayName, path);
    }
    
    @Override
    public SunCCompiler createCopy(CompilerFlavor flavor) {
        SunCCompiler copy = new SunCCompiler(getExecutionEnvironment(), flavor, getKind(), getName(), getDisplayName(), getPath());
        if (isReady()) {
            copy.copySystemIncludeDirectories(getSystemIncludeDirectories());
            copy.copySystemPreprocessorSymbols(getSystemPreprocessorSymbols());
            copy.copySystemIncludeHeaders(getSystemIncludeHeaders());
        }
        return copy;
    }

    public static SunCCompiler create(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
        return new SunCCompiler(env, flavor, kind, name, displayName, path);
    }

    @Override
    public CompilerDescriptor getDescriptor() {
        return getFlavor().getToolchainDescriptor().getC();
    }

    @Override
    protected void parseCompilerOutput(BufferedReader reader, CompilerDefinitions pair) {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                for(String token : getSystemPaths(line)){
                    addUnique(pair.systemIncludeDirectoriesList, applyPathPrefix(token));
                }
                parseUserMacros(line, pair.systemPreprocessorSymbolsList);
            }
            reader.close();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe); // FIXUP
        }
    }

    @Override
    protected String getCompilerStderrCommand2() {
        return null;
    }
}
