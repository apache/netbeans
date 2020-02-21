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
package org.netbeans.modules.cnd.discovery.api;

import org.netbeans.modules.cnd.discovery.buildsupport.ToolsWrapperUtility;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.discovery.wizard.api.support.ProjectBridge;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.cnd.toolchain.support.ToolchainUtilities;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HelperLibraryUtility;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.util.Pair;

/**
 *
 */
public final class BuildTraceSupport {

    public static final String CND_TOOLS = "__CND_TOOLS__"; //NOI18N
    public static final String CND_BUILD_LOG = "__CND_BUILD_LOG__"; //NOI18N
    public static final String CND_TOOL_WRAPPER = "__CND_TOOL_WRAPPER__"; //NOI18N
    public static final String CND_C_WRAPPER = "__CND_C_WRAPPER__"; //NOI18N
    public static final String CND_CPP_WRAPPER = "__CND_CPP_WRAPPER__"; //NOI18N

    private static final String SEPARATOR = ":"; //NOI18N

    // Prefer wrapper instead of preload
    private static final boolean USE_WRAPPER = Boolean.getBoolean("cnd.discovery.use.wrapper"); // NOI18N
    
    public static enum BuildTraceKind {
        Preload,
        Wrapper
    }

    public static final class BuildTrace {

        private final BuildTraceKind kind;
        private final ExecutionEnvironment execEnv;
        private final MakeConfiguration conf;
        private final Project project;

        private BuildTrace(BuildTraceKind kind, ExecutionEnvironment execEnv, MakeConfiguration conf, Project project) {
            this.kind = kind;
            this.execEnv = execEnv;
            this.conf = conf;
            this.project = project;
        }

        public void modifyEnv(Env env) {
            if (kind == BuildTraceKind.Wrapper) {
                CompilerSet wrapper = getToolsWrapper();
                if (wrapper != null) {
                    CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
                    Map<String, String> envAsMap = env.getenvAsMap();
                    Pair<String, String> modifyPathEnvVariable;
                    if (conf.getPrependToolCollectionPath().getValue()) {
                        modifyPathEnvVariable = ToolchainUtilities.modifyPathEnvVariable(execEnv, envAsMap, compilerSet, "build"); //NOI18N
                    } else {
                        modifyPathEnvVariable = ToolchainUtilities.defaultPathEnvVariable(execEnv, envAsMap);
                    }
                    PlatformInfo pi = conf.getPlatformInfo();
                    String defaultPath = wrapper.getDirectory() + pi.pathSeparator() + modifyPathEnvVariable.second();
                    env.putenv(modifyPathEnvVariable.first(), defaultPath);
                    
                    env.putenv(CND_TOOL_WRAPPER, wrapper.getDirectory());
                    Tool tool = wrapper.getTool(PredefinedToolKind.CCompiler);
                    if (tool != null) {
                        env.putenv(CND_C_WRAPPER, tool.getPath());
                    }
                    tool = wrapper.getTool(PredefinedToolKind.CCCompiler);
                    if (tool != null) {
                        env.putenv(CND_CPP_WRAPPER, tool.getPath());
                    }
                }
            }
        }
        
        public void modifyEnv(Map<String, String> env) {
            if (kind == BuildTraceKind.Wrapper) {
                CompilerSet wrapper = getToolsWrapper();
                if (wrapper != null) {
                    CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
                    Pair<String, String> modifyPathEnvVariable;
                    if (conf.getPrependToolCollectionPath().getValue()) {
                        modifyPathEnvVariable = ToolchainUtilities.modifyPathEnvVariable(execEnv, env, compilerSet, "build"); //NOI18N
                    } else {
                        modifyPathEnvVariable = ToolchainUtilities.defaultPathEnvVariable(execEnv, env);
                    }
                    PlatformInfo pi = conf.getPlatformInfo();
                    String defaultPath = modifyPathEnvVariable.second();
                    defaultPath = wrapper.getDirectory() + pi.pathSeparator() + defaultPath;
                    env.put(modifyPathEnvVariable.first(), defaultPath);
                    
                    env.put(CND_TOOL_WRAPPER, wrapper.getDirectory());
                    Tool tool = wrapper.getTool(PredefinedToolKind.CCompiler);
                    if (tool != null) {
                        env.put(CND_C_WRAPPER, tool.getPath());
                    }
                    tool = wrapper.getTool(PredefinedToolKind.CCCompiler);
                    if (tool != null) {
                        env.put(CND_CPP_WRAPPER, tool.getPath());
                    }
                }
            }
        }

        public void modifyPreloadEnv(Env env) throws IOException, CancellationException {
            if (BuildTraceHelper.isMac(execEnv)) {
                String ldPreliad = BuildTraceHelper.getLDPreloadEnvName(execEnv);
                String merge = env.getenv(ldPreliad);
                String what = BuildTraceHelper.INSTANCE.getLibraryName(execEnv);
                if (what.indexOf(':') > 0) {
                    what = what.substring(0, what.indexOf(':'));
                }
                String where = BuildTraceHelper.INSTANCE.getLDPaths(execEnv);
                if (where.indexOf(':') > 0) {
                    where = where.substring(0, where.indexOf(':'));
                }
                String lib = where + '/' + what;
                if (merge != null && !merge.isEmpty()) {
                    merge = lib + ":" + merge; // NOI18N
                } else {
                    merge = lib;
                }
                env.putenv(ldPreliad, merge);
            } else {
                String ldPreliad = BuildTraceHelper.getLDPreloadEnvName(execEnv);
                String merge = env.getenv(ldPreliad);
                if (merge != null && !merge.isEmpty()) {
                    merge = BuildTraceHelper.INSTANCE.getLibraryName(execEnv) + ":" + merge; // NOI18N
                } else {
                    merge = BuildTraceHelper.INSTANCE.getLibraryName(execEnv);
                }
                env.putenv(ldPreliad, merge);

                String ldPath = BuildTraceHelper.getLDPathEnvName(execEnv);
                merge = env.getenv(ldPath);
                if (merge == null || merge.isEmpty()) {
                    merge = HostInfoUtils.getHostInfo(execEnv).getEnvironment().get(ldPath);
                }
                if (merge != null && !merge.isEmpty()) {
                    merge = BuildTraceHelper.INSTANCE.getLDPaths(execEnv) + ":" + merge; // NOI18N
                } else {
                    merge = BuildTraceHelper.INSTANCE.getLDPaths(execEnv);
                }
                env.putenv(ldPath, merge);
            }
        }

        public BuildTraceKind getKind() {
            return kind;
        }

        public CompilerSet getToolsWrapper() {
            ToolsWrapperUtility util = new ToolsWrapperUtility(execEnv, conf, project);
            return util.getToolsWrapper();
        }
    }

    private BuildTraceSupport() {
    }

    public static boolean useBuildTrace(MakeConfiguration conf) {
        return conf.getCodeAssistanceConfiguration().getBuildAnalyzer().getValue();
    }

    public static boolean resolveSymbolicLinks(MakeConfiguration conf) {
        return conf.getCodeAssistanceConfiguration().getResolveSymbolicLinks().getValue();
    }

    public static String getTools(MakeConfiguration conf, ExecutionEnvironment execEnv) {
        String res = conf.getCodeAssistanceConfiguration().getTools().getValue();
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        res = prepengTool(compilerSet, execEnv, PredefinedToolKind.CCompiler, res);
        res = prepengTool(compilerSet, execEnv, PredefinedToolKind.CCCompiler, res);
        res = prepengTool(compilerSet, execEnv, PredefinedToolKind.FortranCompiler, res);
        return res;
    }

    public static BuildTrace supportedPlatforms(ExecutionEnvironment execEnv, MakeConfiguration conf, Project project) {
        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
            HostInfo.OSFamily osFamily = hostInfo.getOSFamily();
            HostInfo.CpuFamily cpuFamily = hostInfo.getCpuFamily();
            if (!USE_WRAPPER){
                switch (osFamily) {
                    //case MACOSX:
                    //    if (cpuFamily == HostInfo.CpuFamily.X86) {
                    //        return new BuildTrace(BuildTraceKind.Preload, execEnv, conf, project);
                    //    }
                    case LINUX:
                        if (cpuFamily == HostInfo.CpuFamily.X86 || cpuFamily == HostInfo.CpuFamily.SPARC) {
                            return new BuildTrace(BuildTraceKind.Preload, execEnv, conf, project);
                        }
                    case SUNOS:
                        if (cpuFamily == HostInfo.CpuFamily.X86 || cpuFamily == HostInfo.CpuFamily.SPARC) {
                            return new BuildTrace(BuildTraceKind.Preload, execEnv, conf, project);
                        }
                }
            }
            return new BuildTrace(BuildTraceKind.Wrapper, execEnv, conf, project);
        } catch (IOException ex) {
        } catch (CancellationException ex) {
        }
        return null;
    }

    public static Set<String> getCompilerNames(Project project, PredefinedToolKind kind) {
        Set<String> res = new HashSet<>();
        switch (kind) {
            case CCompiler: {
                res.add("cc"); //NOI18N
                res.add("gcc"); //NOI18N
                res.add("xgcc"); //NOI18N
                res.add("clang"); //NOI18N
                res.add("icc"); //NOI18N
                addTool(project, kind, res);
                break;
            }
            case CCCompiler: {
                res.add("CC"); //NOI18N
                res.add("g++"); //NOI18N
                res.add("c++"); //NOI18N
                res.add("clang++"); //NOI18N
                res.add("icpc"); //NOI18N
                res.add("cl"); //NOI18N
                addTool(project, kind, res);
                break;
            }
            case FortranCompiler: {
                res.add("ffortran"); //NOI18N
                res.add("f77"); //NOI18N
                res.add("f90"); //NOI18N
                res.add("f95"); //NOI18N
                res.add("gfortran"); //NOI18N
                res.add("g77"); //NOI18N
                res.add("g90"); //NOI18N
                res.add("g95"); //NOI18N
                res.add("ifort"); //NOI18N
                addTool(project, kind, res);
            }
        }
        return res;
    }

    private static String prepengTool(CompilerSet compilerSet, ExecutionEnvironment execEnv, PredefinedToolKind kind, String res) {
        if (compilerSet == null) {
            return res;
        }
        Tool tool = compilerSet.getTool(kind);
        if (tool == null) {
            return res;
        }
        String name = tool.getName();
        if (name == null || name.isEmpty()) {
            return res;
        }
        res = addIfNeeded(name, res);
        String path = tool.getPath();
        try {
            String canonicalPath = FileSystemProvider.getCanonicalPath(execEnv, path);
            if (canonicalPath != null) {
                name = CndPathUtilities.getBaseName(canonicalPath);
                if (name != null && !name.isEmpty()) {
                    res = addIfNeeded(name, res);
                }
            }
        } catch (IOException ex) {
        }
        return res;
    }

    private static String addIfNeeded(String name, String res) {
        for (String s : res.split(SEPARATOR)) {
            if (s.equals(name)) {
                return res;
            }
        }
        if (res.isEmpty()) {
            res = name;
        } else {
            res = name + SEPARATOR + res;
        }
        return res;
    }

    private static void addTool(Project project, PredefinedToolKind kind, Set<String> res) {
        if (project != null) {
            ProjectBridge projectBridge = new ProjectBridge(project);
            if (projectBridge.isValid()) {
                CompilerSet compilerSet = projectBridge.getCompilerSet();
                if (compilerSet != null) {
                    ExecutionEnvironment execEnv = null;
                    ConfigurationDescriptorProvider provider = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
                    if (provider != null && provider.gotDescriptor()) {
                        MakeConfigurationDescriptor descriptor = provider.getConfigurationDescriptor();
                        if (descriptor != null) {
                            MakeConfiguration activeConfiguration = descriptor.getActiveConfiguration();
                            if (activeConfiguration != null) {
                                execEnv = activeConfiguration.getDevelopmentHost().getExecutionEnvironment();
                            }
                        }
                    }
                    Tool tool = compilerSet.getTool(kind);
                    if (tool != null) {
                        String name = tool.getName();
                        if (name != null && !name.isEmpty()) {
                            if (name.endsWith(".exe")) { //NOI18N
                                name = name.substring(0, name.length() - 4);
                            }
                            res.add(name);
                        }
                        if (execEnv != null) {
                            String path = tool.getPath();
                            try {
                                String canonicalPath = FileSystemProvider.getCanonicalPath(execEnv, path);
                                if (canonicalPath != null) {
                                    name = CndPathUtilities.getBaseName(canonicalPath);
                                    if (name != null && !name.isEmpty()) {
                                        if (name.endsWith(".exe")) { //NOI18N
                                            name = name.substring(0, name.length() - 4);
                                        }
                                        res.add(name);
                                    }
                                }
                            } catch (IOException ex) {
                            }
                        }
                    }
                }
            }
        }
    }

    private static final class BuildTraceHelper extends HelperLibraryUtility {

        private static final BuildTraceHelper INSTANCE = new BuildTraceHelper();

        private BuildTraceHelper() {
            super("org.netbeans.modules.cnd.actions", "bin/${osname}-${platform}${_isa}/libBuildTrace.${soext}"); // NOI18N
        }
    }
}
