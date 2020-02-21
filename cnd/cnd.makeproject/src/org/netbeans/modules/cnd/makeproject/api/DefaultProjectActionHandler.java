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
package org.netbeans.modules.cnd.makeproject.api;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor.LineConvertorFactory;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetUtils;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider.OutputStreamHandler;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent.PredefinedType;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent.Type;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompileConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.makeproject.api.wizards.PreBuildSupport;
import org.netbeans.modules.cnd.makeproject.configurations.CppUtils;
import org.netbeans.modules.cnd.spi.toolchain.CompilerLineConvertor;
import org.netbeans.modules.cnd.spi.utils.CndNotifier;
import org.netbeans.modules.cnd.toolchain.support.ToolchainUtilities;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.NativeProcessChangeEvent;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService;
import org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminalProvider;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

public class DefaultProjectActionHandler implements ProjectActionHandler {

    private ProjectActionEvent pae;
    private Collection<OutputStreamHandler> outputHandlers;
    //private volatile ExecutorTask executorTask;
    private volatile Future<Integer> executorTask;
    private final List<ExecutionListener> listeners = new CopyOnWriteArrayList<>();
    // VK: this is just to tie two pieces of logic together:
    // first is in determining the type of console for remote;
    // second is in canCancel
    private static final boolean RUN_REMOTE_IN_OUTPUT_WINDOW = false;
    private static final RequestProcessor RP = new RequestProcessor("DefaultProjectActionHandler", 1); // NOI18N

    @Override
    public void init(ProjectActionEvent pae, ProjectActionEvent[] paes, Collection<OutputStreamHandler> outputHandlers) {
        this.pae = pae;
        this.outputHandlers = outputHandlers;
    }

    @Override
    public void execute(final InputOutput io) {
        final ExecutionListener listener = new ExecutionListener() {

            @Override
            public void executionStarted(int pid) {
                listeners.forEach((l) -> {
                    l.executionStarted(pid);
                });
            }

            @Override
            public void executionFinished(int rc) {
                listeners.forEach((l) -> {
                    l.executionFinished(rc);
                });
            }
        };

        Runnable executor = () -> {
            try {
                _execute(io, listener);
            } catch (Throwable th) {
                try {
                    if (io != null && io.getErr() != null) {
                        io.getErr().println("Internal error occured. Please report a bug.", null, true); // NOI18N
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace(System.err);
                }
                try {
                    if (io != null && io.getOut() != null) {
                        io.getOut().close();
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace(System.err);
                }
                listener.executionFinished(-1);
                throw new RuntimeException(th);
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            RP.post(executor);
        } else {
            executor.run();
        }
    }

    private void _execute(final InputOutput io, final ExecutionListener listener) {
        final Type actionType = pae.getType();

        if (actionType != ProjectActionEvent.PredefinedType.RUN
                && actionType != ProjectActionEvent.PredefinedType.PRE_BUILD
                && actionType != ProjectActionEvent.PredefinedType.BUILD
                && actionType != ProjectActionEvent.PredefinedType.COMPILE_SINGLE
                && actionType != ProjectActionEvent.PredefinedType.CLEAN
                && actionType != ProjectActionEvent.PredefinedType.BUILD_TESTS
                && actionType != ProjectActionEvent.PredefinedType.TEST) {
            assert false;
        }
        
        final String origRunDir = pae.getProfile().getRunDir();
        boolean preventRunPathConvertion = origRunDir.startsWith("///"); // NOI18N
        final String runDirectory = RemoteFileUtil.normalizeAbsolutePath(pae.getProfile().getRunDirectory(), pae.getProject());
        final MakeConfiguration conf = pae.getConfiguration();
        final PlatformInfo pi = conf.getPlatformInfo();
        final ExecutionEnvironment execEnv = conf.getDevelopmentHost().getExecutionEnvironment();
        final CompilerSet cs = conf.getCompilerSet().getCompilerSet();
        
        Map<String, String> env = pae.getProfile().getEnvironment().getenvAsMap();
        boolean showInput = actionType == ProjectActionEvent.PredefinedType.RUN;
        boolean unbuffer = false;
        boolean runInInternalTerminal;
        boolean runInExternalTerminal;
        boolean statusEx = false;
        String commandLine = null;
        
        int consoleType = pae.getProfile().getConsoleType().getValue();
        ArrayList<String> args = null;
        // Used if not RUN. Also in case of QMake args are tweaked...

        if (actionType == ProjectActionEvent.PredefinedType.RUN) {
            statusEx = true;
            runInInternalTerminal = consoleType == RunProfile.CONSOLE_TYPE_INTERNAL;
            runInExternalTerminal = consoleType == RunProfile.CONSOLE_TYPE_EXTERNAL;
            if (runInExternalTerminal && (pae.getProfile().getTerminalType() == null || pae.getProfile().getTerminalPath() == null)) {
                String errmsg;
                if (Utilities.isMac()) {
                    errmsg = getString("Err_NoTermFoundMacOSX");
                } else {
                    errmsg = getString("Err_NoTermFound");
                }
                CndNotifier.getDefault().notifyInfo(errmsg);
                consoleType = RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW;
                runInExternalTerminal = runInInternalTerminal = false;
            }

            if (!conf.getDevelopmentHost().isLocalhost()) {
                if ((RUN_REMOTE_IN_OUTPUT_WINDOW && !runInInternalTerminal) || (runInExternalTerminal)) {
                    //use default consoly type for remote run
                    //the default is Internal Terminal
                    consoleType = RunProfile.getDefaultConsoleType();
                    runInInternalTerminal = RunProfile.CONSOLE_TYPE_INTERNAL == consoleType;
                }
            }

            if (consoleType == RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW) {
                unbuffer = true;
            } else if (!runInInternalTerminal) {
                showInput = false;
                if (consoleType == RunProfile.CONSOLE_TYPE_DEFAULT) {
                    consoleType = RunProfile.getDefaultConsoleType();
                }
            }

            // Append compilerset base to run path. (IZ 120836)
            if (conf.getPrependToolCollectionPath().getValue()) {
                if (cs != null && env.get("__CND_TOOL_WRAPPER__") == null) { //NOI18N
                    ToolchainUtilities.modifyPathEnvVariable(execEnv, env, cs, "run"); //NOI18N
                }
            }

            commandLine = pae.getRunCommandAsString();
        } else { // Build or Clean or compile
            // Build or Clean
            if (conf.getPrependToolCollectionPath().getValue()) {
                if (env.get("__CND_TOOL_WRAPPER__") == null) { //NOI18N
                    ToolchainUtilities.modifyPathEnvVariable(execEnv, env, cs, "build"); //NOI18N
                }
            }
            // Pass QMAKE from compiler set to the Makefile (IZ 174731)
            if (conf.isQmakeConfiguration()) {
                String qmakePath = cs.getTool(PredefinedToolKind.QMakeTool).getPath();
                qmakePath = CppUtils.normalizeDriveLetter(cs, qmakePath.replace('\\', '/')); // NOI18N
                args = pae.getArguments();
                args.add("QMAKE=" + CndPathUtilities.escapeOddCharacters(qmakePath)); // NOI18N
            }
            if (conf.isMakefileConfiguration() && !CompileConfiguration.AUTO_COMPILE.equals(conf.getCompileConfiguration().getCompileCommand().getValue())) {
                commandLine = pae.getRunCommandAsString();
            }
            
            Map lookup = pae.getContext().lookup(Map.class);
            if (lookup != null && lookup.containsKey("UseCommandLine")) { // NOI18N
                commandLine = pae.getRunCommandAsString();
            }
            
            // See bug #228730
            if (conf.getDevelopmentHost().isLocalhost() && Utilities.isWindows() 
                    && CompilerSetUtils.isMsysBased(cs)) {
                    String executable = pae.getExecutable();
                    String baseName = CndPathUtilities.getBaseName(executable);
                    if (baseName.startsWith("make")) { // NOI18N
                        env.put("MAKE", WindowsSupport.getInstance().convertToMSysPath(pae.getExecutable())); // NOI18N
                    }
            }
        }

        LineConvertor converter = null;

        if (actionType == ProjectActionEvent.PredefinedType.BUILD ||
            actionType == ProjectActionEvent.PredefinedType.COMPILE_SINGLE ||
            actionType == ProjectActionEvent.PredefinedType.BUILD_TESTS) {
            converter = new CompilerLineConvertor(
                    pae.getProject(), conf.getCompilerSet().getCompilerSet(),
                    execEnv, RemoteFileUtil.getFileObject(runDirectory, pae.getProject()), io);
        }

        // TODO: this is actual only for sun studio compiler
        env.put("SPRO_EXPAND_ERRORS", ""); // NOI18N
        
        String workingDirectory = preventRunPathConvertion ? runDirectory : ProjectSupport.convertWorkingDirToRemoteIfNeeded(pae, runDirectory);
        
        if (workingDirectory == null) {
            // TODO: fix me
            // return null;
        }
        
        WriterRedirector writer = null;
        if (outputHandlers != null && outputHandlers.size() > 0) {
            writer = new WriterRedirector(outputHandlers);
        }

        ProcessChangeListener processChangeListener =
                new ProcessChangeListener(listener, writer, converter, io);

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv).
                setWorkingDirectory(workingDirectory).
                unbufferOutput(unbuffer).setStatusEx(statusEx).
                addNativeProcessListener(processChangeListener);

        StringBuilder buf = new StringBuilder();
        if (commandLine != null) {
            buf.append(commandLine);
            npb.setCommandLine(commandLine);
        } else {
            String exe = getExecutable(cs);
            
            if (args == null) {
                args = pae.getArguments();
            }
            if (actionType == ProjectActionEvent.PredefinedType.PRE_BUILD) {
                ArrayList<String> expandedArgs = new ArrayList<>();
                for(String s :args) {
                    expandedArgs.add(PreBuildSupport.expandMacros(s, cs, env));
                }
                args = expandedArgs;
            }

            npb.setExecutable(exe).setArguments(args.toArray(new String[args.size()]));
            buf.append(exe);
            for(String a : args) {
                buf.append(' ');
                buf.append(a);
            }
        }
        
        if (actionType == ProjectActionEvent.PredefinedType.PRE_BUILD ||
            actionType == ProjectActionEvent.PredefinedType.BUILD ||
            actionType == ProjectActionEvent.PredefinedType.BUILD_TESTS) {
            npb.redirectError();
        }

        npb.getEnvironment().putAll(env);

        if (actionType == PredefinedType.RUN || actionType == PredefinedType.DEBUG) {
            if (ServerList.get(execEnv).getX11Forwarding() && !env.containsKey("DISPLAY")) { //NOI18N if DISPLAY is set, let it do its work
                npb.setX11Forwarding(true);
            }
        }

        if (actionType == ProjectActionEvent.PredefinedType.RUN && consoleType == RunProfile.CONSOLE_TYPE_EXTERNAL) {
            String termPath = pae.getProfile().getTerminalPath();
            CndUtils.assertNotNull(termPath, "null terminal path"); // NOI18N; should be checked above
            if (termPath != null) {
                String termBaseName = CndPathUtilities.getBaseName(termPath);
                if (ExternalTerminalProvider.getSupportedTerminalIDs().contains(termBaseName)) {
                    npb.useExternalTerminal(ExternalTerminalProvider.getTerminal(execEnv, termBaseName));
                }
            }
        }

        boolean requestFocus = (actionType == PredefinedType.RUN
                || actionType == PredefinedType.DEBUG
                || actionType == PredefinedType.DEBUG_STEPINTO
                || actionType == PredefinedType.DEBUG_TEST
                || actionType == PredefinedType.DEBUG_STEPINTO_TEST
                || actionType == PredefinedType.CUSTOM_ACTION);
        
        NativeExecutionDescriptor descr =
                new NativeExecutionDescriptor().controllable(true).
                frontWindow(true).
                requestFocus(requestFocus).
                inputVisible(showInput).
                inputOutput(io).
                outLineBased(!unbuffer).
                showProgress(false).
                postMessageDisplayer(new PostMessageDisplayer.Default(pae.getActionName())).
                postExecution(processChangeListener).
                errConvertorFactory(processChangeListener).
                outConvertorFactory(processChangeListener).
                keepInputOutputOnFinish();

        if (actionType == PredefinedType.PRE_BUILD ||
            actionType == PredefinedType.BUILD ||
            actionType == PredefinedType.COMPILE_SINGLE ||
            actionType == PredefinedType.CLEAN) {
            descr.noReset(true);
            if (cs != null) {
                descr.charset(cs.getEncoding());
            }
        }

        if (actionType == PredefinedType.RUN) {
            Project p = pae.getProject();
            if (p instanceof MakeProject) {
                descr.charset(Charset.forName(((MakeProject) p).getSourceEncoding()));
            }
        }
        
        if (actionType == PredefinedType.PRE_BUILD ||
            actionType == PredefinedType.COMPILE_SINGLE ||
            actionType == PredefinedType.BUILD ||
            actionType == PredefinedType.CLEAN) {
            // CR 19816163
            if (io != null && io.getOut() != null) {
                io.getOut().println("cd '"+workingDirectory+"'"); //NOI18N
                io.getOut().println(buf.toString());
            } else {
                System.out.println("cd '"+workingDirectory+"'"); //NOI18N
                System.out.println(buf.toString());
            }
        }

        NativeExecutionService es =
                NativeExecutionService.newService(npb,
                descr,
                pae.getActionName());

        executorTask = es.run();
    }

    private String getExecutable(CompilerSet cs) {
        String executable = pae.getExecutable();
        if (executable.contains("cmake")) { // NOI18N
            if (isCygwinCompilerSet(cs)) {
                return PreBuildSupport.getCmakePath(cs);
            }
        }
        return executable;
    }

    @Override
    public void addExecutionListener(ExecutionListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    @Override
    public void removeExecutionListener(ExecutionListener l) {
        listeners.remove(l);
    }

    @Override
    public boolean canCancel() {
        return true;
    }

    @Override
    public void cancel() {
        RP.post(() -> {
            Future<Integer> et = executorTask;
            if (et != null) {
                et.cancel(true);
            }
        });
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(DefaultProjectActionHandler.class, s);
    }

    protected static String getString(String key, String... a1) {
        return NbBundle.getMessage(DefaultProjectActionHandler.class, key, a1);
    }
    
    private static boolean isCygwinCompilerSet(CompilerSet compilerSet) {
        CompilerFlavor flavor = compilerSet.getCompilerFlavor();
        if (flavor.isCygwinCompiler()) return true;
        else return false;
    }
    
    private static final class ProcessChangeListener implements ChangeListener, Runnable, LineConvertorFactory {

        private final AtomicReference<NativeProcess> processRef = new AtomicReference<>();
        private final ExecutionListener listener;
        private Writer outputListener;
        private final LineConvertor lineConvertor;

        public ProcessChangeListener(ExecutionListener listener, Writer outputListener, LineConvertor lineConvertor,
                InputOutput tab) {
            this.listener = listener;
            this.outputListener = outputListener;
            this.lineConvertor = lineConvertor;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (!(e instanceof NativeProcessChangeEvent)) {
                return;
            }

            final NativeProcessChangeEvent event = (NativeProcessChangeEvent) e;
            processRef.compareAndSet(null, (NativeProcess) event.getSource());

            if (event.state == NativeProcess.State.RUNNING) {
                if (listener != null) {
                    listener.executionStarted(event.pid);
                }
            }
        }

        @Override
        // Started by Execution as postRunnable
        public void run() {
            closeOutputListener();

            NativeProcess process = processRef.get();
            if (process != null && listener != null) {
                listener.executionFinished(process.exitValue());
            }
        }

        @Override
        public LineConvertor newLineConvertor() {
            return ProcessChangeListener.this::convert;
        }

        private synchronized void closeOutputListener() {
            if (outputListener != null) {
                try {
                    outputListener.flush();
                    outputListener.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                outputListener = null;
            }
            if (lineConvertor instanceof ChangeListener) {
                ((ChangeListener)lineConvertor).stateChanged(new ChangeEvent(this));
            }
        }

        private synchronized List<ConvertedLine> convert(String line) {
            if (outputListener != null) {
                try {
                    outputListener.write(line);
                    outputListener.write("\n"); // NOI18N
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (lineConvertor != null) {
                return lineConvertor.convert(line);
            }
            return null;
        }
    }

    private static final class WriterRedirector extends Writer {

        private final Collection<OutputStreamHandler> handlers;

        WriterRedirector(Collection<BuildActionsProvider.OutputStreamHandler> handlers) {
            this.handlers = handlers;
        }

        @Override
        public void write(String line) throws IOException {
            handlers.forEach((outputStreamHandler) -> {
                outputStreamHandler.handleLine(line);
            });
        }

        @Override
        public void flush() throws IOException {
            handlers.forEach((outputStreamHandler) -> {
                outputStreamHandler.flush();
            });
        }

        @Override
        public void close() throws IOException {
            handlers.forEach((outputStreamHandler) -> {
                outputStreamHandler.close();
            });
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
    }

    private static final class MacroConverter {

        private final MacroExpanderFactory.MacroExpander expander;
        private final Map<String, String> envVariables;
        private String homeDir;

        public MacroConverter(ExecutionEnvironment env, Map<String, String> envVariables) {
            this.envVariables = new HashMap<>(envVariables);
            if (HostInfoUtils.isHostInfoAvailable(env)) {
                try {
                    HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                    this.envVariables.putAll(hostInfo.getEnvironment());
                    homeDir = hostInfo.getUserDir();
                } catch (IOException | ConnectionManager.CancellationException ex) {
                    // should never == null occur if isHostInfoAvailable(env) => report
                    Exceptions.printStackTrace(ex);
                }
            }
            this.expander = MacroExpanderFactory.getExpander(env, false);
        }

        private void updateUtilitiesPath(String utilitiesPath) {
            envVariables.put(CompilerSet.UTILITIES_PATH, utilitiesPath);
        }

        private void updateToolPath(String toolPath) {
            envVariables.put(CompilerSet.TOOLS_PATH, toolPath);
        }

        public String expand(String in) {
            try {
                if (homeDir != null) {
                    if (in.startsWith("~")) { //NOI18N
                        in = homeDir+in.substring(1);
                    }
                    in = in.replace(":~", ":"+homeDir); //NOI18N
                    in = in.replace(";~", ";"+homeDir); //NOI18N
                }
                return expander != null ? expander.expandMacros(in, envVariables) : in;
            } catch (ParseException ex) {
                //nothing to do
            }
            return in;
        }
    }
}
