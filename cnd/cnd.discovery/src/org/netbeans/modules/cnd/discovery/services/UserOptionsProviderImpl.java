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

package org.netbeans.modules.cnd.discovery.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.regex.Pattern;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.api.project.NativeFileItem.LanguageFlavor;
import org.netbeans.modules.cnd.api.project.NativeFileSearch;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.discovery.api.DriverFactory;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.QtInfoProvider;
import org.netbeans.modules.cnd.dwarfdump.source.Artifacts;
import org.netbeans.modules.cnd.dwarfdump.source.CompileLineOrigin;
import org.netbeans.modules.cnd.dwarfdump.source.Driver;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.spi.configurations.AllOptionsProvider;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.PackageConfiguration;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.PkgConfig;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.ResolvedPath;
import org.netbeans.modules.cnd.makeproject.spi.configurations.UserOptionsProvider;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileSystem;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.makeproject.spi.configurations.UserOptionsProvider.class)
public class UserOptionsProviderImpl implements UserOptionsProvider {
    private final Map<String,PkgConfig> pkgConfigs = new HashMap<>();
    private final Map<ExecutionEnvironment, Map<String,PackageConfiguration>> commandCache = new WeakHashMap<>();

    public UserOptionsProviderImpl(){
    }

    @Override
    public List<IncludePath> getItemUserIncludePaths(List<IncludePath> includes, AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
        List<IncludePath> res =new ArrayList<>();
        if (makeConfiguration.getConfigurationType().getValue() != MakeConfiguration.TYPE_MAKEFILE){
            ExecutionEnvironment env = getExecutionEnvironment(makeConfiguration);
            FileSystem fs = FileSystemProvider.getFileSystem(env);
            for(PackageConfiguration pc : getPackages(compilerOptions.getAllOptions(compiler), makeConfiguration)) {
                for (String path : pc.getIncludePaths()) {
                    res.add(new IncludePath(fs, path));
                }
            }
        }
        if (makeConfiguration.isQmakeConfiguration()) {
            res.addAll(QtInfoProvider.getDefault().getQtIncludeDirectories(makeConfiguration));
        }
        return res;
    }

    private ExecutionEnvironment getExecutionEnvironment(MakeConfiguration makeConfiguration) {
        return makeConfiguration.getDevelopmentHost().getExecutionEnvironment();
    }

    @Override
    public List<String> getItemUserMacros(List<String> macros, AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
        List<String> res = new ArrayList<>();
        if (makeConfiguration.getConfigurationType().getValue() != MakeConfiguration.TYPE_MAKEFILE){
            String options = compilerOptions.getAllOptions(compiler);
            for(PackageConfiguration pc : getPackages(options, makeConfiguration)) {
                res.addAll(pc.getMacros());
            }
        }
        if (makeConfiguration.isQmakeConfiguration()) {
            res.addAll(QtInfoProvider.getDefault().getQtAdditionalMacros(makeConfiguration));
        }
        return res;
    }

    @Override
    public String getItemImportantFlags(AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
        if (makeConfiguration.getConfigurationType().getValue() != MakeConfiguration.TYPE_MAKEFILE) {
            if (compiler != null && compiler.getDescriptor() != null) {
                String importantFlags = compiler.getDescriptor().getImportantFlags();
                if (importantFlags != null && importantFlags.length() > 0) {
                    StringBuilder buf = new StringBuilder();
                    Pattern pattern = Pattern.compile(importantFlags);
                    String options = compilerOptions.getAllOptions(compiler);
                    String[] split = options.split(" "); //NOI18N
                    for (int i = 0; i < split.length; i++) {
                        String s = split[i];
                        if (s.startsWith("-")) { //NOI18N
                            // handle user specified language "x c" & "x c++"
                            if (s.equals("-x") && (i+1 < split.length)) { //NOI18N
                                i++;
                                s += split[i];
                            }
                            if (pattern.matcher(s).find()) {
                                if (buf.length() > 0) {
                                    buf.append(' ');
                                }
                                buf.append(s);
                                if (Driver.ISYSROOT_FLAG.equals(s) && i+1 < split.length) { // NOI18N
                                    buf.append(' ');
                                    buf.append(split[i+1]);
                                }
                            }
                        }
                    }
                    return buf.toString();
                }
            }
        }
        return null;
    }

    @Override
    public LanguageFlavor getLanguageFlavor(AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
        if (makeConfiguration.getConfigurationType().getValue() != MakeConfiguration.TYPE_MAKEFILE){
            String options = compilerOptions.getAllOptions(compiler);
            if (compiler.getKind() == PredefinedToolKind.CCompiler) {
                Driver driver = DriverFactory.getDriver(null);
                Artifacts artifacts = driver.gatherCompilerLine("gcc "+options, CompileLineOrigin.BuildLog, false); //NOI18N
                ItemProperties.LanguageStandard languageStandard = DriverFactory.getLanguageStandard(ItemProperties.LanguageStandard.Unknown, artifacts);
                switch (languageStandard) {
                    case C89: return LanguageFlavor.C89;
                    case C99: return LanguageFlavor.C99;
                    case C11: return LanguageFlavor.C11;
                }
            } else if (compiler.getKind() == PredefinedToolKind.CCCompiler) {
                Driver driver = DriverFactory.getDriver(null);
                Artifacts artifacts = driver.gatherCompilerLine("g++ "+options, CompileLineOrigin.BuildLog, true); //NOI18N
                ItemProperties.LanguageStandard languageStandard = DriverFactory.getLanguageStandard(ItemProperties.LanguageStandard.Unknown, artifacts);
                switch (languageStandard) {
                    case CPP11: return LanguageFlavor.CPP11;
                    case CPP14: return LanguageFlavor.CPP14;
                    case CPP17: return LanguageFlavor.CPP17;
                }
            } else if (compiler.getKind() == PredefinedToolKind.FortranCompiler) {
                // TODO
            }
        }
        return LanguageFlavor.UNKNOWN;
    }

    private List<PackageConfiguration> getPackages(String s, MakeConfiguration conf){
        List<PackageConfiguration> res = new ArrayList<>();
        while(true){
            int i = s.indexOf('`'); // NOI18N
            if (i >= 0) {
                String pkg = s.substring(i+1);
                int j = pkg.indexOf('`'); // NOI18N
                if (j > 0) {
                    final String executable = pkg.substring(0, j);
                    s = s.substring(i+executable.length()+2);
                    if (executable.startsWith("pkg-config ")) { //NOI18N
                        PackageConfiguration config = getPkgConfigOutput(conf, executable);
                        if (config != null){
                            res.add(config);
                        }
                    } else {
                        PackageConfiguration config = getCommandOutput(conf, executable);
                        if (config != null) {
                            res.add(config);
                        }
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return res;
    }

    private PkgConfig getPkgConfig(ExecutionEnvironment env, MakeConfiguration conf){
        String hostKey = ExecutionEnvironmentFactory.toUniqueID(env);
        PkgConfig pkg;
        synchronized(pkgConfigs){
            pkg = pkgConfigs.get(hostKey);
            if (pkg == null) {
                if (ConnectionManager.getInstance().isConnectedTo(env)) {
                    pkg = PkgConfigManager.getDefault().getPkgConfig(env, conf); //pass conf
                    pkgConfigs.put(hostKey, pkg);
                }
            }
        }
        return pkg;
    }

    @Override
    public NativeFileSearch getPackageFileSearch(final ExecutionEnvironment env, MakeConfiguration conf) {
        final PkgConfig pkg = getPkgConfig(env, conf);
        if (pkg != null) {
            return new NativeFileSearch() {
                @Override
                public Collection<FSPath> searchFile(NativeProject project, String fileName) {
                    Collection<ResolvedPath> resolvedPath = pkg.getResolvedPath(fileName);
                    ArrayList<FSPath> res = new ArrayList<>(1);
                    if (resolvedPath != null) {
                        FileSystem fileSystem = FileSystemProvider.getFileSystem(env);
                        char fileSeparatorChar = FileSystemProvider.getFileSeparatorChar(fileSystem);
                        for(ResolvedPath path : resolvedPath) {
                            String absPath = path.getIncludePath()+fileSeparatorChar+fileName;
                            res.add(new FSPath(fileSystem, absPath));
                        }
                    }
                    return res;
                }
            };
        }
        return null;
    }

    private PackageConfiguration getPkgConfigOutput(MakeConfiguration conf, String executable){
        String pkg = executable.substring(11).trim();
        StringTokenizer st = new StringTokenizer(pkg);
        boolean readFlags = false;
        String findPkg = null;
        while(st.hasMoreTokens()) {
            String aPkg = st.nextToken();
            if (aPkg.equals("--cflags")) { //NOI18N
                readFlags = true;
                continue;
            }
            if (aPkg.startsWith("-")) { //NOI18N
                readFlags = false;
                continue;
            }
            findPkg = aPkg;
        }
        if (readFlags && findPkg != null) {
            PkgConfig configs = getPkgConfig(getExecutionEnvironment(conf), conf);
            if (configs != null) {
                PackageConfiguration config = configs.getPkgConfig(findPkg);
                if (config != null){
                    return config;
                }
            }
        }
        return null;
    }

    private synchronized PackageConfiguration getCommandOutput(MakeConfiguration conf, String command) {
        ExecutionEnvironment env = getExecutionEnvironment(conf);
        Map<String, PackageConfiguration> map = commandCache.get(env);
        if (map == null) {
            map = new HashMap<>();
            commandCache.put(env, map);
        }
        if (map.containsKey(command)) {
            return map.get(command);
        }
        ArrayList<String> args = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(command," "); // NOI18N
        String executable = null;
        while(st.hasMoreTokens()) {
            if (executable == null) {
                executable = st.nextToken();
            } else {
                args.add(st.nextToken());
            }
        }
        ExitStatus status = ProcessUtils.executeInDir(conf.getMakefileConfiguration().getAbsBuildCommandWorkingDir(), env, executable, args.toArray(new String[args.size()]));
        final String flags = status.getOutputString();
        PackageConfiguration config = null;
        if (flags != null) {
            config = new MyPackageConfiguration(executable, flags);
        }
        map.put(command, config);
        return config;
    }

    private static final class MyPackageConfiguration implements PackageConfiguration {
        private final String executable;
        private final List<String> macros = new ArrayList<>();
        private final List<String> paths = new ArrayList<>();

        private MyPackageConfiguration(String executable, String flags) {
            this.executable = executable;
            StringTokenizer st = new StringTokenizer(flags, " "); //NOI18N
            while(st.hasMoreElements()) {
                String t = st.nextToken();
                if (t.startsWith("-I")) { //NOI18N
                    paths.add(t.substring(2));
                } else if (t.startsWith("-D")) { //NOI18N
                    macros.add(t.substring(2));
                }
            }
        }

        @Override
        public String getName() {
            return executable;
        }

        @Override
        public Collection<String> getIncludePaths() {
            return paths;
        }

        @Override
        public Collection<String> getMacros() {
            return macros;
        }

        @Override
        public String getDisplayName() {
            return executable;
        }

        @Override
        public String getLibs() {
            return ""; //NOI18N
        }

        @Override
        public String getVersion() {
            return ""; //NOI18N
        }
    }
}
