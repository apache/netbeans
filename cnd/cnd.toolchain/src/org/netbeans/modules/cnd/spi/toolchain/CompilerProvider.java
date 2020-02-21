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

package org.netbeans.modules.cnd.spi.toolchain;

import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.cnd.toolchain.compilerset.APIAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class CompilerProvider {
    private static final CompilerProvider INSTANCE = new Default();
    
    public abstract Tool createCompiler(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path);

    protected CompilerProvider() {
    }

    /**
     * Static method to obtain the provider.
     * @return the provider
     */
    public static CompilerProvider getInstance() {
        return INSTANCE;
    }

    //
    // Implementation of the default provider
    //
    private static final class Default extends CompilerProvider {
        private final Lookup.Result<CompilerProvider> res;

        private Default() {
            res = Lookup.getDefault().lookupResult(CompilerProvider.class);
        }

        @Override
        public Tool createCompiler(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
            for (CompilerProvider resolver : res.allInstances()) {
                Tool out = resolver.createCompiler(env, flavor, kind, name, displayName, path);
                if (out != null) {
                    return out;
                }
            }
            return APIAccessor.get().createTool(env, flavor, kind, name, displayName, path);
        }
    }
}
