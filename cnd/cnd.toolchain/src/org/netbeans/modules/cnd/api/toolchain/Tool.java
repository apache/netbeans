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

import java.nio.charset.Charset;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ToolDescriptor;
import org.netbeans.modules.cnd.toolchain.compilerset.APIAccessor;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetImpl;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Utilities;

/**
 *
 */
public class Tool {

    static {
        APIAccessor.register(new APIAccessorImpl());
    }
    
    private final ExecutionEnvironment executionEnvironment;
    private final CompilerFlavor flavor;
    private final ToolKind kind;
    private String name;
    private final String displayName;
    private String path;
    private CompilerSet compilerSet;

    /** Creates a new instance of GenericCompiler */
    protected Tool(ExecutionEnvironment executionEnvironment, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
        this.executionEnvironment = executionEnvironment;
        this.flavor = flavor;
        this.kind = kind;
        this.name = name;
        this.displayName = displayName;
        this.path = path;
    }

    public ToolDescriptor getDescriptor() {
        return null;
    }

    public Tool createCopy(CompilerFlavor flavor) {
        return new Tool(executionEnvironment, flavor, kind, name, displayName, path);
    }

    public final ExecutionEnvironment getExecutionEnvironment() {
        return executionEnvironment;
    }

    public final CompilerFlavor getFlavor() {
        return flavor;
    }

    /**
     * Some tools may require long initialization
     * (e.g. compiler tools call compilers to get include search path, etc).
     *
     * Such initialization is usually moved out of constructors.
     * This methods allows to check whether the tool was initialized or not.
     *
     * @return true in the case this tool is ready, otherwise false
     */
    public boolean isReady() {
        return true;
    }

    /**
     * Some tools may require long initialization
     * (e.g. compiler tools call compilers to get include search path, etc).
     * This method should
     * - check whether the tool is initialized
     * - if it is not, start initialization
     * - wait until it is done
     * NB: Should never be called from AWT thread
     * @param reset pass true if expect getting fresh
     */
    public void waitReady(boolean reset) {
    }

    public final ToolKind getKind() {
        return kind;
    }

    public final String getName() {
        return name;
    }

    public final String getPath() {
        return path;
    }

    public final String getDisplayName() {
        return displayName;
    }

    public String getIncludeFilePathPrefix() {
        // TODO: someone put this here only because OutputWindowWriter in core
        // wants to get information about compilers which are defined in makeprojects.
        // abstract Tool shouldn't care about include paths for compilers
        throw new UnsupportedOperationException();
    }

    public final CompilerSet getCompilerSet() {
        return compilerSet;
    }

    @Override
    public String toString() {
        String n = getName();
        if (Utilities.isWindows() && n.endsWith(".exe")) { // NOI18N
            return n.substring(0, n.length() - 4);
        } else {
            return n;
        }
    }

    private void setPath(String p) {
        if (p != null) {
            path = p;
            name = CndPathUtilities.getBaseName(path);
            if (Utilities.isWindows() && name.endsWith(".exe")) { // NOI18N
                name = name.substring(0, name.length() - 4);
            }
        }
    }

    private void setCompilerSet(CompilerSet compilerSet) {
        this.compilerSet = compilerSet;
    }

    private static Tool createTool(ExecutionEnvironment executionEnvironment, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
        return new Tool(executionEnvironment, flavor, kind, name, displayName, path);
    }

    private static final class APIAccessorImpl extends APIAccessor {

        @Override
        public Tool createTool(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
            return Tool.createTool(env, flavor, kind, name, displayName, path);
        }

        @Override
        public void setCompilerSet(Tool tool, CompilerSet cs) {
            tool.setCompilerSet(cs);
        }

        @Override
        public void setToolPath(Tool tool, String p) {
            tool.setPath(p);
        }

        @Override
        public void setCharset(Charset charset, CompilerSet cs) {
            ((CompilerSetImpl)cs).setEncoding(charset);
        }
    }
}
