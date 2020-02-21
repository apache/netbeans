/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
