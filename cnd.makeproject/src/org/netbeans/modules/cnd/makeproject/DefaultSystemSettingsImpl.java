/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
            case C11:
                // see also org.netbeans.modules.cnd.discovery.api.DriverFactory.DriverImpl.C11
                return "-std=c11"; //NOI18N
            case CPP11:
                // see also org.netbeans.modules.cnd.discovery.api.DriverFactory.DriverImpl.CPP11
                return "-std=c++11"; //NOI18N
            case CPP14:
                // see also org.netbeans.modules.cnd.discovery.api.DriverFactory.DriverImpl.CPP14
                return "-std=c++14"; //NOI18N
            case CPP17:
                // see also org.netbeans.modules.cnd.discovery.api.DriverFactory.DriverImpl.CPP17
                return "-std=c++17"; //NOI18N
            default:
                return ""; // NOI18N
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
