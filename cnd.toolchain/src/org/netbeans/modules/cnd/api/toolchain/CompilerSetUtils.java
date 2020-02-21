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

package org.netbeans.modules.cnd.api.toolchain;

import org.netbeans.modules.cnd.toolchain.compilerset.ToolUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.Utilities;

/**
 *
 */
public final class CompilerSetUtils {
    private CompilerSetUtils() {
    }

    /**
     * Get the Cygwin base directory from Cygwin.xml (toolchain definition, which users the Windows registry) or the user's path
     */
    public static String getCygwinBase() {
        return ToolUtils.getCygwinBase();
    }

    /**
     * Find command folder by toolchain definitions, which users the Windows registry or the user's path
     */
    public static String getCommandFolder(CompilerSet cs) {
        String res = null;
        if (cs != null) {
            if (cs.getCompilerFlavor().isCygwinCompiler()) {
                return res;
            }
            res = cs.getCommandFolder();
        }
        if (res != null) {
            return res;
        }
        return ToolUtils.getCommandFolder();
    }
    
    /**
     * 
     * @param cs tool collection
     * @return true if tool collection uses msys
     */
    public static boolean isMsysBased(CompilerSet cs) {
        if (!Utilities.isWindows()) {
            return false;
        }
        if (cs.getCompilerFlavor().isMinGWCompiler()) {
            return true;
        }
        String commandFolder = getCommandFolder(cs);
        if (commandFolder != null && commandFolder.toLowerCase().replace('\\', '/').contains("/msys")) { // NOI18N
            return true;
        }
        return false;
    }

    /**
     * Find MinGW toolchain folder.
     * Actual for tool collection based on MinGW tool collection.
     * For example: Clang reused MinGW linker.
     */
    public static String getMinGWBaseFolder(CompilerSet cs) {
        if (!Utilities.isWindows()) {
            return null;
        }
        if (cs.getCompilerFlavor().isMinGWCompiler()) {
            return null;
        }
        Tool tool = cs.getTool(PredefinedToolKind.CCompiler);
        if (tool != null && tool.getName().contains("clang")) { // NOI18N
            CompilerSetManager csm = CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal());
            for(CompilerSet acs : csm.getCompilerSets()) {
                if (acs.getCompilerFlavor().isMinGWCompiler()) {
                    return acs.getDirectory();
                }
            }
        }
        return null;
    }
}
