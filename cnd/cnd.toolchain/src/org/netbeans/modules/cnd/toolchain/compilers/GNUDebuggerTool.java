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
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.DebuggerDescriptor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/*package*/ final class GNUDebuggerTool extends Tool {

    private GNUDebuggerTool(ExecutionEnvironment env, CompilerFlavor flavor, String name, String displayName, String path) { // GRP - FIXME
        super(env, flavor, PredefinedToolKind.DebuggerTool, name, displayName, path); // NOI18N
    }

    @Override
    public GNUDebuggerTool createCopy(CompilerFlavor flavor) {
        return new GNUDebuggerTool(getExecutionEnvironment(), flavor, getName(), getDisplayName(), getPath());
    }

    public static GNUDebuggerTool create(ExecutionEnvironment env, CompilerFlavor flavor, String name, String displayName, String path) {
        return new GNUDebuggerTool(env, flavor, name, displayName, path);
    }

    @Override
    public DebuggerDescriptor getDescriptor() {
        return getFlavor().getToolchainDescriptor().getDebugger();
    }

}
