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
import org.netbeans.modules.cnd.spi.toolchain.CompilerProvider;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * Override the cnd default compiler type "Tool". MakeProjects uses classes derived from Tool but cnd/core
 * can't depend on makeproject classes. So this allows makeproject to provide a tool creator factory.
 *
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.spi.toolchain.CompilerProvider.class, position=1000)
public class MakeProjectCompilerProvider extends CompilerProvider {

    /**
     * Create a class derived from Tool
     *
     * Thomas: If you want/need different information to choose which Tool derived class to create we can change
     * this method. We can also add others, if desired. This was mainly a proof-of-concept that tool creation
     * could be deferred to makeproject.
     */
    @Override
    public Tool createCompiler(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
        if (flavor.isSunStudioCompiler()) {
            if (kind == PredefinedToolKind.CCompiler) {
                if (flavor.getToolchainDescriptor().getC() != null && flavor.getToolchainDescriptor().getC().getFingerPrintFlags() != null) {
                    return OracleCCompiler.create(env, flavor, kind, name, displayName, path);
                } else {
                    return SunCCompiler.create(env, flavor, kind, name, displayName, path);
                }
            } else if (kind == PredefinedToolKind.CCCompiler) {
                if (flavor.getToolchainDescriptor().getCpp()!= null && flavor.getToolchainDescriptor().getCpp().getFingerPrintFlags() != null) {
                    return OracleCppCompiler.create(env, flavor, kind, name, displayName, path);
                } else {
                    return SunCCCompiler.create(env, flavor, kind, name, displayName, path);
                }
            } else if (kind == PredefinedToolKind.FortranCompiler) {
                return SunFortranCompiler.create(env, flavor, kind, name, displayName, path);
            } else if (kind == PredefinedToolKind.MakeTool) {
                return SunMaketool.create(env, flavor, name, displayName, path);
            } else if (kind == PredefinedToolKind.DebuggerTool) {
                return SunDebuggerTool.create(env, flavor, name, displayName, path);
            } else if (kind == PredefinedToolKind.Assembler) {
                return Assembler.create(env, flavor, kind, name, displayName, path);
            }
        } else /* if (flavor.isGnuCompiler()) */ { // Assume GNU (makeproject system doesn't handle Unknown)
           if (kind == PredefinedToolKind.CCompiler) {
               if ("MSVC".equals(flavor.getToolchainDescriptor().getName())) { // NOI18N
                   return MsvcCompiler.create(env, flavor, kind, name, displayName, path);
               } else {
                   return GNUCCompiler.create(env, flavor, kind, name, displayName, path);
               }
           } else if (kind == PredefinedToolKind.CCCompiler) {
               if ("MSVC".equals(flavor.getToolchainDescriptor().getName())) { // NOI18N
                   return new MsvcCompiler(env, flavor, kind, name, displayName, path);
               } else {
                   return GNUCCCompiler.create(env, flavor, kind, name, displayName, path);
               }
            } else if (kind == PredefinedToolKind.FortranCompiler) {
                return GNUFortranCompiler.create(env, flavor, kind, name, displayName, path);
            } else if (kind == PredefinedToolKind.MakeTool) {
                return GNUMaketool.create(env, flavor, name, displayName, path);
            } else if (kind == PredefinedToolKind.DebuggerTool) {
                return GNUDebuggerTool.create(env, flavor, name, displayName, path);
            } else if (kind == PredefinedToolKind.Assembler) {
                return Assembler.create(env, flavor, kind, name, displayName, path);
            }
        }
        if (kind == PredefinedToolKind.CustomTool) {
            return CustomTool.create(env);
        } else if (kind == PredefinedToolKind.QMakeTool || kind == PredefinedToolKind.CMakeTool) {
            return GeneralTool.create(env, flavor, kind, name, displayName, path);
        }
        return null;
    }
}
