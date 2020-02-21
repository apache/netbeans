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

import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;

public abstract class AbstractCompiler extends Tool {

    /** Creates a new instance of GenericCompiler */
    protected AbstractCompiler(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
        super(env, flavor, kind, name, displayName, path);
        includeFilePrefix = null;
    }
    private String includeFilePrefix;

    @Override
    public String getIncludeFilePathPrefix() {
        if (includeFilePrefix == null) {
            if (getExecutionEnvironment().isLocal()) {
                includeFilePrefix = ""; // NOI18N
                CompilerDescriptor c = getDescriptor();
                if (c != null) {
                    if (c.getRemoveIncludePathPrefix() != null) {
                        String path = getPath().replace('\\', '/'); // NOI18N
                        int i = path.toLowerCase().indexOf("/bin"); // NOI18N
                        if (i > 0) {
                            includeFilePrefix = path.substring(0, i);
                        }
                    }
                }
            }
        }
        return includeFilePrefix;
    }

    @Override
    public abstract CompilerDescriptor getDescriptor();

    public String getDevelopmentModeOptions(int value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getDevelopmentModeFlags() != null && compiler.getDevelopmentModeFlags().length > value){
            return compiler.getDevelopmentModeFlags()[value];
        }
        return ""; // NOI18N
    }

    public String getWarningLevelOptions(int value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getWarningLevelFlags() != null && compiler.getWarningLevelFlags().length > value){
            return compiler.getWarningLevelFlags()[value];
        }
        return ""; // NOI18N
    }

    public String getSixtyfourBitsOption(int value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getArchitectureFlags() != null && compiler.getArchitectureFlags().length > value){
            return compiler.getArchitectureFlags()[value];
        }
        return ""; // NOI18N
    }

    public String getCStandardOptions(int value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getCStandardFlags() != null && compiler.getCStandardFlags().length > value){
            return compiler.getCStandardFlags()[value];
        }
        return ""; //NOI18N
    }
    
    public String getCppStandardOptions(int value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getCppStandardFlags() != null && compiler.getCppStandardFlags().length > value){
            return compiler.getCppStandardFlags()[value];
        }
        return ""; //NOI18N
    }
    
    public String getStripOption(boolean value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && value){
            return compiler.getStripFlag();
        }
        return ""; // NOI18N
    }

    public String getDependencyGenerationOption() {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getDependencyGenerationFlags() != null) {
            return compiler.getDependencyGenerationFlags();
        }
        return ""; // NOI18N
    }

    public String getMTLevelOptions(int value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getMultithreadingFlags() != null && compiler.getMultithreadingFlags().length > value) {
            return compiler.getMultithreadingFlags()[value];
        }
        return ""; // NOI18N
    }

    public String getLanguageExtOptions(int value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getLanguageExtensionFlags() != null && compiler.getLanguageExtensionFlags().length > value) {
            return compiler.getLanguageExtensionFlags()[value];
        }
        return ""; // NOI18N
    }

    public String getLibraryLevelOptions(int value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getLibraryFlags() != null && compiler.getLibraryFlags().length > value) {
            return compiler.getLibraryFlags()[value];
        }
        return ""; // NOI18N
    }

    public String getStandardEvaluationOptions(int value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getStandardFlags() != null && compiler.getStandardFlags().length > value) {
            return compiler.getStandardFlags()[value];
        }
        return ""; // NOI18N
    }
    
    public List<String> getSystemPreprocessorSymbols() {
        return Collections.<String>emptyList();
    }

    public List<String> getSystemPreprocessorSymbols(String flags) {
        return Collections.<String>emptyList();
    }

    public List<String> getSystemIncludeDirectories() {
        return Collections.<String>emptyList();
    }

    public List<String> getSystemIncludeDirectories(String flags) {
        return Collections.<String>emptyList();
    }

    public List<String> getSystemIncludeHeaders() {
        return Collections.<String>emptyList();
    }

    public List<String> getSystemIncludeHeaders(String flags) {
        return Collections.<String>emptyList();
    }

    /**
     * @return true if settings were really replaced by new one
     */
    public boolean setSystemPreprocessorSymbols(List<String> values) {
        return false;
    }

    /**
     * @return true if settings were really replaced by new one
     */
    public boolean setSystemIncludeDirectories(List<String> values) {
        return false;
    }

    /**
     * @return true if settings were really replaced by new one
     */
    public boolean setSystemIncludeHeaders(List<String> values) {
        return false;
    }

    protected final void normalizePaths(List<String> paths) {
        for (int i = 0; i < paths.size(); i++) {
            paths.set(i, normalizePath(paths.get(i)));
        }
    }

    protected String normalizePath(String path) {
        // this call also fixes inambiguties at case insensitive systems when work
        // with case sensitive "path"s returned by remote compilers
        return CndFileUtils.normalizeAbsolutePath(FileSystemProvider.getFileSystem(getExecutionEnvironment()), path);
    }

    protected final String applyPathPrefix(String path) {
        String prefix = getIncludeFilePathPrefix();
        return normalizePath( prefix != null ? prefix + path : path );
    }
    
    /**
     * restore default compiler system properties,
     * i.e. default include paths, predefined macros, ...
     * Same as <code>resetCompilerDefinitions(false)</code>
     */
    public final void resetCompilerDefinitions() {
        String path = getPath();
        if (path == null || path.isEmpty()) {
            return;
        }
        resetCompilerDefinitions(false);
    }

    /**
     * @param lazy  when <code>true</code> postpone actual reset until
     *      {@link #getSystemPreprocessorSymbols()},
     *      {@link #getSystemIncludeDirectories()} or
     *      {@link #waitReady(boolean)} is called;
     *      when <code>false</code> do reset immediately
     */
    public void resetCompilerDefinitions(boolean lazy) {
    }

    public void saveSettings(Preferences prefs, String prefix) {
    }

    public void loadSettings(Preferences prefs, String prefix) {
    }
}
