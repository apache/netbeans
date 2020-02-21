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

import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ToolDescriptor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 */
/*package*/ final class GeneralTool extends Tool {
    private GeneralTool(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
        super(env, flavor, kind, name, displayName, path); // NOI18N
    }

    @Override
    public GeneralTool createCopy(CompilerFlavor flavor) {
        return new GeneralTool(getExecutionEnvironment(), flavor,  getKind(), getName(), getDisplayName(), getPath());
    }

    public static GeneralTool create(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
        return new GeneralTool(env, flavor, kind, name, displayName, path);
    }

    @Override
    public ToolDescriptor getDescriptor() {
        if (getKind() == PredefinedToolKind.QMakeTool) {
            return getFlavor().getToolchainDescriptor().getQMake();
        } else if (getKind() == PredefinedToolKind.CMakeTool) {
            return getFlavor().getToolchainDescriptor().getCMake();
        }
        return null;
    }
}
