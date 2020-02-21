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
package org.netbeans.modules.cnd.discovery.projectimport;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor.LineConvertorFactory;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.actions.AbstractExecutorRunAction;
import org.netbeans.modules.cnd.api.remote.*;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetUtils;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.discovery.api.BuildTraceSupport;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.wizards.PreBuildSupport;
import org.netbeans.modules.cnd.spi.toolchain.CompilerLineConvertor;
import org.netbeans.modules.cnd.spi.toolchain.ToolchainProject;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.*;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService;
import org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer;
import org.netbeans.modules.nativeexecution.api.util.*;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 */
public class ExecuteCommand {

    private final String runDir;
    private String command;
    private String cmdLine;
    private final Project project;
    private final ExecutionEnvironment execEnv;
    private String name;
    private String displayName;

    public ExecuteCommand(Project project, String runDir, String command) {
        this.runDir = runDir;
        this.command = command;
        this.cmdLine = command;
        this.project = project;
        this.execEnv = getExecutionEnvironment();

    }

    public void setName(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public Future<Integer> performAction(ExecutionListener listener, Writer outputListener, List<String> additionalEnvironment, BuildTraceSupport.BuildTrace buldTraceSupport) {
        NativeExecutionService es = prepare(listener, outputListener, additionalEnvironment, buldTraceSupport);
        if (es != null) {
            return es.run();
        }
        return null;
    }

    private NativeExecutionService prepare(ExecutionListener listener, Writer outputListener, List<String> additionalEnvironment, BuildTraceSupport.BuildTrace buldTraceSupport) {
        HostInfo hostInfo;
        try {
            hostInfo = HostInfoUtils.getHostInfo(execEnv);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } catch (CancellationException ex) {
            Logger.getLogger(ExecuteCommand.class.getName()).log(Level.INFO, "Connections was cancelled by user", ex);
            return null;
        }
        // Executable
        String executable = hostInfo.getShell();
        if (executable == null) {
            if (hostInfo.getOS().getFamily() == HostInfo.OSFamily.WINDOWS) {
                CompilerSet compilerSet = getCompilerSet();
                if (compilerSet != null && compilerSet.getCompilerFlavor().isMinGWCompiler()) {
                    String commandFolder = compilerSet.getCommandFolder();
                    if (commandFolder != null) {
                        // reinit shell
                        WindowsSupport.getInstance().init(commandFolder);
                        try {
                            HostInfoUtils.updateHostInfo(execEnv);
                            hostInfo = HostInfoUtils.getHostInfo(execEnv);
                            executable = hostInfo.getShell();
                        } catch (IOException ex) {
                        } catch (InterruptedException ex) {
                        } catch (CancellationException ex) {
                        }
                    }
                }
            }
            if (executable == null) {
                ImportProject.logger.log(Level.INFO, "Shell command is null"); //NOI18N
                return null;
            }
        }
        Map<String, String> map = new HashMap<>();
        expandMacros(hostInfo, MakeArtifact.MAKE_MACRO, PredefinedToolKind.MakeTool, map, "make"); //NOI18N
        if (buldTraceSupport != null && buldTraceSupport.getKind() == BuildTraceSupport.BuildTraceKind.Wrapper) {
            buldTraceSupport.modifyEnv(map);
        }
        expandMacros(hostInfo, PreBuildSupport.C_COMPILER_MACRO, PredefinedToolKind.CCompiler, map, "gcc"); //NOI18N
        expandMacros(hostInfo, PreBuildSupport.CPP_COMPILER_MACRO, PredefinedToolKind.CCCompiler, map, "g++"); //NOI18N
        
        // Arguments
        String[] args = new String[]{"-c", command}; // NOI18N
        // Build directory
        String buildDir = convertToRemoteIfNeeded(runDir);
        if (buildDir == null) {
            ImportProject.logger.log(Level.INFO, "Run folder is null"); //NOI18N
            return null;
        }
        Map<String, String> envMap = getEnv(additionalEnvironment);
        if (isSunStudio()) {
            envMap.put("SPRO_EXPAND_ERRORS", ""); // NOI18N
        }

        InputOutput _tab = IOProvider.getDefault().getIO(displayName, false); // This will (sometimes!) find an existing one.
        _tab.closeInputOutput(); // Close it...
        InputOutput inputOutput = IOProvider.getDefault().getIO(displayName, true); // Create a new ...
        try {
            inputOutput.getOut().reset();
        } catch (IOException ioe) {
        }

        RemoteSyncWorker syncWorker = RemoteSyncSupport.createSyncWorker(project, inputOutput.getOut(), inputOutput.getErr());
        if (syncWorker != null) {
            if (!syncWorker.startup(envMap)) {
                ImportProject.logger.log(Level.INFO, "RemoteSyncWorker is not started up"); //NOI18N
                return null;
            }
        }
        String wrapper = map.get(BuildTraceSupport.CND_TOOL_WRAPPER);
        if (wrapper != null) {
            for(Map.Entry<String,String> e : envMap.entrySet()) {
                if ("PATH".equals(e.getKey().toUpperCase())) { //NOI18N
                    if (execEnv.isLocal() && Utilities.isWindows()) {
                        envMap.put(e.getKey(), wrapper+";"+e.getValue()); //NOI18N
                    } else {
                        envMap.put(e.getKey(), wrapper+":"+e.getValue()); //NOI18N
                    }
                    break;
                }
            }
        }

        MacroMap mm = MacroMap.forExecEnv(execEnv);
        mm.putAll(envMap);
        if (buldTraceSupport != null && buldTraceSupport.getKind() == BuildTraceSupport.BuildTraceKind.Preload) {
            if (envMap.containsKey(BuildTraceSupport.CND_TOOLS)) { // NOI18N
                try {
                    if (BuildTraceHelper.isMac(execEnv)) {
                        String what = BuildTraceHelper.INSTANCE.getLibraryName(execEnv);
                        if (what.indexOf(':') > 0) {
                            what = what.substring(0,what.indexOf(':'));
                        }
                        String where = BuildTraceHelper.INSTANCE.getLDPaths(execEnv);
                        if (where.indexOf(':') > 0) {
                            where = where.substring(0,where.indexOf(':'));
                        }
                        String lib = where+'/'+what;
                        mm.prependPathVariable(BuildTraceHelper.getLDPreloadEnvName(execEnv),lib);
                    } else {
                        mm.prependPathVariable(BuildTraceHelper.getLDPreloadEnvName(execEnv), BuildTraceHelper.INSTANCE.getLibraryName(execEnv));
                        mm.prependPathVariable(BuildTraceHelper.getLDPathEnvName(execEnv), BuildTraceHelper.INSTANCE.getLDPaths(execEnv));
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        traceExecutable(executable, buildDir, args, execEnv.toString(), mm.toMap());
        AbstractExecutorRunAction.ProcessChangeListener processChangeListener = new AbstractExecutorRunAction.ProcessChangeListener(listener, outputListener,
                new CompilerLineConvertor(project, getCompilerSet(), execEnv, RemoteFileUtil.getFileObject(buildDir, execEnv), inputOutput), syncWorker); // NOI18N

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv).
                setExecutable(executable).
                setWorkingDirectory(buildDir).
                setArguments(args).
                unbufferOutput(false).
                addNativeProcessListener(processChangeListener);

        npb.getEnvironment().putAll(mm);
        npb.redirectError();

        NativeExecutionDescriptor descr = new NativeExecutionDescriptor().controllable(true).
                frontWindow(true).
                inputVisible(true).
                showProgress(!CndUtils.isStandalone()).
                inputOutput(inputOutput).
                outLineBased(true).
                postExecution(processChangeListener).
                postMessageDisplayer(new PostMessageDisplayer.Default(name)).
                errConvertorFactory(processChangeListener).
                outConvertorFactory(processChangeListener);

        descr.noReset(true);

        String cdLine = "cd '" + buildDir + "'"; //NOI18N

        inputOutput.getOut().println(cdLine);
        inputOutput.getOut().println(cmdLine);

        return NativeExecutionService.newService(npb, descr, name);
    }

    private void expandMacros(HostInfo hostInfo, String macro, PredefinedToolKind tool, Map<String, String> env, String defaultValue) {
        if (command.contains(macro)) {
            String path = defaultValue;
            CompilerSet compilerSet = getCompilerSet();
            if (compilerSet != null) {
                Tool findTool = compilerSet.findTool(tool);
                if (findTool != null && findTool.getPath() != null && findTool.getPath().length() > 0) {
                    if (tool == PredefinedToolKind.CCompiler) {
                        String wrapper = env.get(BuildTraceSupport.CND_C_WRAPPER);
                        if (wrapper != null) {
                            path = wrapper;
                        } else {
                            path = findTool.getPath();
                        }
                    } else if (tool == PredefinedToolKind.CCCompiler) {
                        String wrapper = env.get(BuildTraceSupport.CND_CPP_WRAPPER);
                        if (wrapper != null) {
                            path = wrapper;
                        } else {
                            path = findTool.getPath();
                        }
                    } else {
                        path = findTool.getPath();
                    }
                    cmdLine = cmdLine.replace(macro, path);
                    if (hostInfo.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
                        String aPath = WindowsSupport.getInstance().convertToShellPath(path);
                        if (aPath != null && aPath.length() > 0) {
                            path = aPath;
                        }
                    }
                }
            }
            command = command.replace(macro, path);
        }
    }

    private CompilerSet getCompilerSet() {
        CompilerSet set = null;
        ToolchainProject toolchain = project.getLookup().lookup(ToolchainProject.class);
        if (toolchain != null) {
            set = toolchain.getCompilerSet();
        }
        if (set == null) {
            set = CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal()).getDefaultCompilerSet();
        }
        return set;
    }

    private boolean isSunStudio() {
        CompilerSet set = getCompilerSet();
        if (set == null) {
            return false;
        }
        return set.getCompilerFlavor().isSunStudioCompiler();
    }

    private static final class BuildTraceHelper extends HelperLibraryUtility {

        private static final BuildTraceHelper INSTANCE = new BuildTraceHelper();

        private BuildTraceHelper() {
            super("org.netbeans.modules.cnd.actions", "bin/${osname}-${platform}${_isa}/libBuildTrace.${soext}"); // NOI18N
        }
    }

    public final ExecutionEnvironment getExecutionEnvironment() {
        RemoteProject info = project.getLookup().lookup(RemoteProject.class);
        if (info != null) {
            return info.getDevelopmentHost();
        }
        return ExecutionEnvironmentFactory.getLocal();
    }

    private String convertToRemoteIfNeeded(String localDir) {
        if (!checkConnection()) {
            return null;
        }
        if (execEnv.isRemote()) {
            final PathMap pathMap = RemoteSyncSupport.getPathMap(execEnv, project);
            String remotePath = pathMap.getRemotePath(localDir, false);
            if (remotePath == null) {
                if (!pathMap.checkRemotePaths(new File[]{new File(localDir)}, true)) {
                    return null;
                }
                remotePath = pathMap.getRemotePath(localDir, false);
            }
            return remotePath;
        }
        return localDir;
    }

    private boolean checkConnection() {
        if (execEnv.isRemote()) {
            try {
                ConnectionManager.getInstance().connectTo(execEnv);
                ServerRecord record = ServerList.get(execEnv);
                if (record.isOffline()) {
                    record.validate(true);
                }
                return record.isOnline();
            } catch (IOException ex) {
                return false;
            } catch (CancellationException ex) {
                return false;
            }
        } else {
            return true;
        }
    }

    private void traceExecutable(String executable, String buildDir, String[] args, String host, Map<String, String> envMap) {
        StringBuilder argsFlat = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            argsFlat.append(" "); // NOI18N
            argsFlat.append(args[i]);
        }
        traceExecutable(executable, buildDir, argsFlat, host, envMap);
    }

    private void traceExecutable(String executable, String buildDir, StringBuilder argsFlat, String host, Map<String, String> envMap) {
        StringBuilder buf = new StringBuilder("Run " + executable); // NOI18N
        buf.append("\n\tin folder   ").append(buildDir); // NOI18N
        buf.append("\n\targuments   ").append(argsFlat); // NOI18N
        buf.append("\n\thost        ").append(host); // NOI18N
        buf.append("\n\tenvironment "); // NOI18N
        for (Map.Entry<String, String> v : envMap.entrySet()) {
            buf.append("\n\t\t").append(v.getKey()).append("=").append(v.getValue()); // NOI18N
        }
        buf.append("\n"); // NOI18N
        ImportProject.logger.log(Level.INFO, buf.toString());
    }

    private Map<String, String> getEnv(List<String> additionalEnvironment) {
        Map<String, String> envMap = new HashMap<>(getDefaultEnvironment());
        if (additionalEnvironment != null) {
            envMap.putAll(parseEnvironmentVariables(additionalEnvironment));
        }
        return envMap;
    }

    private Map<String, String> parseEnvironmentVariables(Collection<String> vars) {
        if (vars.isEmpty()) {
            return Collections.emptyMap();
        } else {
            Map<String, String> envMap = new HashMap<>();
            for (String s : vars) {
                int i = s.indexOf('='); // NOI18N
                if (i > 0) {
                    String key = s.substring(0, i);
                    String value = s.substring(i + 1).trim();
                    if (value.length() > 1 && (value.startsWith("\"") && value.endsWith("\"") || // NOI18N
                            value.startsWith("'") && value.endsWith("'"))) { // NOI18N
                        value = value.substring(1, value.length() - 1);
                    }
                    envMap.put(key, value);
                }
            }
            return envMap;
        }
    }

    private Map<String, String> getDefaultEnvironment() {
        PlatformInfo pi = PlatformInfo.getDefault(execEnv);
        String defaultPath = pi.getPathAsString();
        CompilerSet cs = getCompilerSet();
        if (cs != null) {
            defaultPath = cs.getDirectory() + pi.pathSeparator() + defaultPath;
            // TODO Provide platform info
            String cmdDir = CompilerSetUtils.getCommandFolder(cs);
            if (cmdDir != null && 0 < cmdDir.length()) {
                // Also add msys to path. Thet's where sh, mkdir, ... are.
                defaultPath = cmdDir + pi.pathSeparator() + defaultPath;
            }
        }
        return Collections.singletonMap(pi.getPathName(), defaultPath);
    }
}
