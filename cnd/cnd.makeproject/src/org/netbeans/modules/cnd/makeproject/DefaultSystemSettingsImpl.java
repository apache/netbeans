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

package org.netbeans.modules.cnd.makeproject;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.project.DefaultSystemSettings;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileSearch;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectSupport.NativeExitStatus;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.Platforms;
import org.netbeans.modules.cnd.spi.project.NativeFileSearchProvider;
import org.netbeans.modules.cnd.spi.project.NativeProjectExecutionProvider;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * This is an implementation of DefaultSystemSetting.
 * It provides  
 */
@ServiceProviders({
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.project.DefaultSystemSettings.class),
@ServiceProvider(service = NativeProjectExecutionProvider.class, path = NativeProjectExecutionProvider.PATH, position = 1000),
@ServiceProvider(service = NativeFileSearchProvider.class, path = NativeFileSearchProvider.PATH, position = 1000)
})
public class DefaultSystemSettingsImpl extends DefaultSystemSettings implements NativeProjectExecutionProvider, NativeFileSearchProvider {
    private static AbstractCompiler getDefaultCompiler(NativeFileItem.Language language, NativeProject project) {
        PredefinedToolKind kind;
        switch (language) {
            case C:
                kind = PredefinedToolKind.CCompiler;
                break;
            case CPP:
            case C_HEADER: // use CC for all headers
                kind = PredefinedToolKind.CCCompiler;
                break;
            case FORTRAN:
                kind = PredefinedToolKind.FortranCompiler;
                break;
            default:
                return null;
        }
        ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(project.getFileSystem());
        if (env == null) {
            env = ExecutionEnvironmentFactory.getLocal();
        }
        CompilerSet compilerSet = CompilerSetManager.get(env).getDefaultCompilerSet();
        if (compilerSet != null) {
            Tool compiler = compilerSet.getTool(kind);
            if (compiler instanceof AbstractCompiler) {
                return (AbstractCompiler)compiler;
            }
        }
        return null;
    }

    static String getStdFlagsForFlavor(NativeFileItem.LanguageFlavor languageFlavor) {
        switch(languageFlavor) {
            case C89:   return "-std=c89"; //NOI18N
            case C99:   return "-std=c99"; //NOI18N
            case C11:   return "-std=c11"; //NOI18N
            case C17:   return "-std=c17"; //NOI18N
            case C23:   return "-std=c2x"; //NOI18N
            case CPP98: return "-std=c++98"; //NOI18N
            case CPP11: return "-std=c++11"; //NOI18N
            case CPP14: return "-std=c++14"; //NOI18N
            case CPP17: return "-std=c++17"; //NOI18N
            case CPP20: return "-std=c++20"; //NOI18N
            case CPP23: return "-std=c++23"; //NOI18N
            default:    return ""; // NOI18N
        }
    }

    @Override
    public List<String> getSystemIncludes(NativeFileItem.Language language, NativeFileItem.LanguageFlavor flavor, NativeProject project) {
        AbstractCompiler compiler = getDefaultCompiler(language, project);
        if (compiler != null) {            
            return Collections.unmodifiableList(compiler.getSystemIncludeDirectories(getStdFlagsForFlavor(flavor)));
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * List of pre-included system headers
     * 
     * @param language
     * @param project
     * @return list <String> of pre-included system headers
     */
    @Override
    public List<String> getSystemIncludeHeaders(NativeFileItem.Language language, NativeFileItem.LanguageFlavor flavor, NativeProject project) {
        AbstractCompiler compiler = getDefaultCompiler(language, project);
        if (compiler != null) {
            return Collections.unmodifiableList(compiler.getSystemIncludeHeaders(getStdFlagsForFlavor(flavor)));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getSystemMacros(NativeFileItem.Language language, NativeFileItem.LanguageFlavor flavor, NativeProject project) {
        AbstractCompiler compiler = getDefaultCompiler(language, project);
        if (compiler != null) {            
            return Collections.unmodifiableList(compiler.getSystemPreprocessorSymbols(getStdFlagsForFlavor(flavor)));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public NativeExitStatus execute(NativeProject project, String executable, String[] env, String... args) throws IOException {
        ExecutionEnvironment ee = FileSystemProvider.getExecutionEnvironment(project.getFileSystem());
        return NativeProjectProvider.execute(ee, executable, env, args);
    }

    @Override
    public String getPlatformName(NativeProject project) {
        ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(project.getFileSystem());
        if (env == null) {
            env = ExecutionEnvironmentFactory.getLocal();
        }
        int platform = CompilerSetManager.get(env).getPlatform();
        return Platforms.getPlatform(platform).getName();
    }

    @Override
    public NativeFileSearch getNativeFileSearch(NativeProject project) {
        return Lookup.getDefault().lookup(NativeFileSearch.class);
    }
}
