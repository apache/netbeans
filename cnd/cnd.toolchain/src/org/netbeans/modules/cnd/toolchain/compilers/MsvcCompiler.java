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

import java.util.StringTokenizer;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 */
/*package*/ final class MsvcCompiler extends GNUCCompiler {
   /** Creates a new instance of GNUCCompiler */
   protected MsvcCompiler(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
       super(env, flavor, kind, name, displayName, path);
   }

   @Override
   public MsvcCompiler createCopy(CompilerFlavor flavor) {
       MsvcCompiler copy = new MsvcCompiler(getExecutionEnvironment(), flavor, getKind(), getName(), getDisplayName(), getPath()); // NOI18N
       if (isReady()) {
            copy.copySystemIncludeDirectories(getSystemIncludeDirectories());
            copy.copySystemPreprocessorSymbols(getSystemPreprocessorSymbols());
            copy.copySystemIncludeHeaders(getSystemIncludeHeaders());
       }
       return copy;
   }

   public static MsvcCompiler create(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
       return new MsvcCompiler(env, flavor, kind, name, displayName, path);
   }

    @Override
    public CompilerDescriptor getDescriptor() {
        if (getKind() == PredefinedToolKind.CCCompiler) {
            return getFlavor().getToolchainDescriptor().getCpp();
        } else {
            return getFlavor().getToolchainDescriptor().getC();
        }
    }

   @Override
   protected CompilerDefinitions getFreshCompilerDefinitions() {
        CompilerDefinitions res = new CompilerDefinitions();
        completePredefinedMacros(res);
        String list = System.getenv("INCLUDE"); // NOI18N
        if (list != null) {
            StringTokenizer st = new StringTokenizer(list, ";"); // NOI18N
            while (st.hasMoreTokens()) {
                res.systemIncludeDirectoriesList.add(st.nextToken());
            }
        }
        return res;
   }

    @Override
   protected String getUniqueID() {
       return ""+getKind()+super.getUniqueID();
    }

}
