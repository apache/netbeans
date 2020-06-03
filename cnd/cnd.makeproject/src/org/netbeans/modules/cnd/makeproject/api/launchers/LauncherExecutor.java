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
package org.netbeans.modules.cnd.makeproject.api.launchers;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent.Type;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionSupport;
import org.netbeans.modules.cnd.makeproject.api.TempEnv;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.UIGesturesSupport;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public final class LauncherExecutor {

    private final Launcher launcher;
    private final ProjectActionEvent.PredefinedType actionType;
    private static final Logger LOG = Logger.getLogger("LauncherExecutor");//NOI18N
    private final ExecutionListener listener;
    private enum State{RUNNING, STOPPED};
    private State state = State.STOPPED;

    private static final String USG_CND_LAUNCHER_EXECUTOR = "USG_CND_LAUNCHER_EXECUTOR";    //NOI18N

    /**
     * Creates launcher for the project to be executed as project action of
     * actionType
     *
     * @param launcher
     * @param actionType
     * @param project
     * @return
     */
    public static LauncherExecutor createExecutor(Launcher launcher, ProjectActionEvent.PredefinedType actionType, ExecutionListener listener) {
        return new LauncherExecutor(launcher, actionType, listener);
    }

    private LauncherExecutor(Launcher launcher, ProjectActionEvent.PredefinedType actionType, ExecutionListener listener) {
        this.launcher = launcher;
        this.actionType = actionType;
        this.listener = listener;
    }
    
    // Preprocessing commands inside `` and macroses
    private static String preprocessValueField(String value, MakeConfiguration conf, MacroConverter converter, String defaultPWD) {
        value = value.trim();
        value = conf.expandMacros(value);
        if (value.indexOf('$')>=0) {
            value = converter.expand(value);
        }
        if (value.indexOf('`') >= 0) {
            StringBuilder line = new StringBuilder();
            StringBuilder subCommand = new StringBuilder();
            int state = 0; // in line
            for(int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (c == '`') {
                    if (state == 0) {
                        state = 1; // in command
                    } else if (state == 1) { // end command
                        state = 0;
                        final String command = subCommand.toString();
                        final String[] execAndArgs = command.split(" ");    //NOI18N
                        final String exec = execAndArgs[0];
                        final String[] args = Arrays.copyOfRange(execAndArgs, 1, execAndArgs.length);

                        final NativeProcessBuilder builder = NativeProcessBuilder.newProcessBuilder(
                                conf.getFileSystemHost()).setExecutable(exec).setArguments(args).setWorkingDirectory(defaultPWD);
                        final ProcessUtils.ExitStatus status = ProcessUtils.execute(builder);
                        if (!status.isOK()) {
                            LOG.info(status.getErrorString());
                        }
                        List<String> outputLines = status.getOutputLines();
                        if (!outputLines.isEmpty()) {
                            line.append(outputLines.get(0));
                        }
                    }
                } else {
                    if (state == 0) {  // in line
                        line.append(c);
                    } else if (state == 1) { // in sub command
                        subCommand.append(c);
                    }
                }
            }
            if (state == 1) {
                value = line.toString()+"`"+subCommand.toString(); //NOI18N
            } else {
                value = line.toString();
            }
        }
        return value;
    }
    
    private static MakeConfigurationDescriptor getProjectDescriptor(Project project) {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        return pdp.getConfigurationDescriptor();
    }
    
    private static String getMakeCommand(MakeConfigurationDescriptor pd, MakeConfiguration conf) {
        String cmd;
        CompilerSet cs = conf.getCompilerSet().getCompilerSet();
        if (cs != null) {
            cmd = cs.getTool(PredefinedToolKind.MakeTool).getPath();
        } else {
            CndUtils.assertFalse(true, "Null compiler collection"); //NOI18N
            cmd = "make"; // NOI18N
        }
        //cmd = cmd + " " + MakeOptions.getInstance().getMakeOptions(); // NOI18N
        return cmd;
    }
    
    private static String removeQuotes(String command) {
        if (command.startsWith("\"") && command.endsWith("\"")) { // NOI18N
            return command.substring(1, command.length() - 1);
        } else if (command.startsWith("'") && command.endsWith("'")) { // NOI18N
            return command.substring(1, command.length() - 1);
        }
        return command;
    }

    private static int getArgsIndex(String command) {
        boolean inQuote = false;
        int quote = 0;
        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);
            switch (c) {
                case ' ':
                    if (!inQuote) {
                        return i;
                    }
                    break;
                case '\'':
                case '"':
                    if (inQuote) {
                        if (quote == c) {
                            quote = 0;
                            inQuote = false;
                        }
                    } else {
                        quote = c;
                        inQuote = true;
                    }
                    break;
            }
        }
        // launcher without index is for common
        return LaunchersRegistry.COMMON_LAUNCHER_INDEX;
    }
    
    private void onBuild(final Project project) {
        MakeConfigurationDescriptor pd = getProjectDescriptor(project);
        MakeConfiguration conf = ConfigurationSupport.getProjectActiveConfiguration(project).clone();
        MakeArtifact makeArtifact = new MakeArtifact(pd, conf);

        Map<String, String> env = launcher.getEnv();    //Environment
        MacroConverter converter = new MacroConverter(conf.getDevelopmentHost().getExecutionEnvironment());
        Env buildEnv = new Env();
        if (env != null && !env.isEmpty()) {
            env.keySet().forEach((key) -> {
                String value = env.get(key);
                value = preprocessValueField(value, conf, converter, makeArtifact.getWorkingDirectory());
                converter.addVariable(key, value);
                buildEnv.putenv(key, value);
            });
        }
        
        HashMap lookupMap = new HashMap();
        RunProfile profile = new RunProfile(makeArtifact.getWorkingDirectory(), conf.getDevelopmentHost().getBuildPlatform(), conf);
        String buildCommand = launcher.getBuildCommand();
        if (buildCommand == null) {
            String makeCommand = getMakeCommand(pd, conf);
            buildCommand = makeArtifact.getBuildCommand(makeCommand, ""); // NOI18N
            String args = ""; // NOI18N
            int index = getArgsIndex(buildCommand);
            if (index >= 0) {
                args = buildCommand.substring(index + 1);
                buildCommand = removeQuotes(buildCommand.substring(0, index));
            }
            profile.setArgs(args);
        } else {
            //expand macros if presented
            buildCommand = preprocessValueField(buildCommand, conf, converter, makeArtifact.getWorkingDirectory());
            profile.getRunCommand().setValue(buildCommand);
            lookupMap.put("UseCommandLine", "true"); // NOI18N
        }
        
        profile.setEnvironment(buildEnv);
        Lookup context = Lookups.fixed(new ExecutionListenerImpl(), lookupMap);
        ProjectActionEvent projectActionEvent = new ProjectActionEvent(
                project, 
                actionType, 
                buildCommand, conf, 
                profile, 
                true, context);
        ProjectActionSupport.getInstance().fireActionPerformed(new ProjectActionEvent[]{projectActionEvent});
    }
    
    private void onDefault(Project project) {
        MakeConfiguration conf = ConfigurationSupport.getProjectActiveConfiguration(project).clone();
        if (conf != null) {
            RunProfile profile = conf.getProfile();
            
            MacroConverter converter = new MacroConverter(conf.getDevelopmentHost().getExecutionEnvironment());
            Map<String, String> env = launcher.getEnv();    //Environment
            Env runEnv = new Env();
            if (env != null && !env.isEmpty()) {
                env.keySet().forEach((key) -> {
                    String value = env.get(key);
                    value = preprocessValueField(value, conf, converter, profile.getBaseDir());
                    converter.addVariable(key, value);
                    runEnv.putenv(key, value);
                });
            }

            String runCommand = launcher.getCommand();
            runCommand = preprocessValueField(runCommand, conf, converter, profile.getBaseDir());
            profile.getRunCommand().setValue(runCommand);     //RunCommand
            String runDir;    //RunDir
            //use run dir from the launcher if exists, use default from RunProfile otherwise
            if (launcher.getRunDir() != null) {
                runDir = launcher.getRunDir();
            } else {
                runDir = profile.getBaseDir();
            }
            runDir = preprocessValueField(runDir, conf, converter, profile.getBaseDir());
            profile.setRunDir(runDir);
            profile.setEnvironment(runEnv);
            String executable = ""; //NOI18N
            if (launcher.getSymbolFiles() != null) {
                // SymbolFiles (now the single symbol file is only supported!!)
                executable = launcher.getSymbolFiles().split(",")[0]; //NOI18N
                
                //expand macros if presented
                executable = preprocessValueField(executable, conf, converter, runDir);
            }
            Lookup context;
            if (launcher.runInOwnTab()) {
                context = Lookups.fixed(new ExecutionListenerImpl(), executable, new MyType(actionType, launcher.getName()));
            } else {
                context = Lookups.fixed(new ExecutionListenerImpl(), executable);
            }
            ProjectActionEvent projectActionEvent = new ProjectActionEvent(
                    project,
                    actionType,
                    executable, conf,
                    profile,
                    false, context);
            ProjectActionSupport.getInstance().fireActionPerformed(new ProjectActionEvent[]{projectActionEvent});
        }
    }

    public void execute(final Project project) {
        RequestProcessor.getDefault().post(() -> {
            switch (actionType) {
                case BUILD:
                    onBuild(project);
                    break;
                default:
                    onDefault(project);
                    break;
            }
        });
        UIGesturesSupport.submit(USG_CND_LAUNCHER_EXECUTOR, actionType);
    }
    
    public boolean isRunning() {
        return state.equals(State.RUNNING);
    }

    private final class ExecutionListenerImpl implements ExecutionListener {

        @Override
        public void executionStarted(int pid) {
            state = State.RUNNING;
            listener.executionStarted(pid);
        }

        @Override
        public void executionFinished(int rc) {
            state = State.STOPPED;
            listener.executionFinished(rc);
        }
    }
    private static final class MyType implements Type {
        private final Type delegate;
        private final String name;
        
        private MyType(Type delegate, String name) {
            this.delegate = delegate;
            this.name = name;
        }

        @Override
        public int ordinal() {
            return delegate.ordinal();
        }

        @Override
        public String name() {
            return delegate.name();
        }

        @Override
        public String getLocalizedName() {
            if (name == null || name.isEmpty()) {
                return delegate.getLocalizedName();
            } else {
                return delegate.getLocalizedName()+" "+name; //NOI18N
            }
        }

        @Override
        public void setLocalizedName(String name) {
            throw new UnsupportedOperationException();
        }
                
    }
    
    private static final class MacroConverter {

        private final MacroExpanderFactory.MacroExpander expander;
        private final Map<String, String> envVariables;
        private String homeDir;

        public MacroConverter(ExecutionEnvironment env) {
            envVariables = new HashMap<>();
            if (HostInfoUtils.isHostInfoAvailable(env)) {
                try {
                    HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                    envVariables.putAll(hostInfo.getEnvironment());
                    homeDir = hostInfo.getUserDir();
                } catch (IOException | ConnectionManager.CancellationException ex) {
                    // should never == null occur if isHostInfoAvailable(env) => report
                    Exceptions.printStackTrace(ex);
                }
            } else {
                LOG.log(Level.INFO, "Host info should be available here!", new Exception());
            }
            TempEnv.getInstance(env).addTemporaryEnv(envVariables);
            this.expander = (envVariables == null) ? null : MacroExpanderFactory.getExpander(env, false);
        }
        
        public void addVariable(String key, String value) {
            envVariables.put(key, value);
        }

        public String expand(String in) {
            try {
                if (in.startsWith("~") && homeDir != null) { //NOI18N
                    in = homeDir+in.substring(1);
                }
                return expander != null ? expander.expandMacros(in, envVariables) : in;
            } catch (ParseException ex) {
                //nothing to do
            }
            return in;
        }
    }


}
