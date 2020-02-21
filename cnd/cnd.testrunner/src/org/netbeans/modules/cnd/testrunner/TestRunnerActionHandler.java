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

package org.netbeans.modules.cnd.testrunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor.LineConvertorFactory;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetUtils;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider.OutputStreamHandler;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent.Type;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.testrunner.spi.TestHandlerFactory;
import org.netbeans.modules.cnd.testrunner.spi.TestHandlerFactoryProvider;
import org.netbeans.modules.cnd.testrunner.ui.CndTestRunnerNodeFactory;
import org.netbeans.modules.cnd.testrunner.ui.CndUnitHandlerFactory;
import org.netbeans.modules.cnd.testrunner.ui.TestRunnerLineConvertor;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.RerunHandler;
import org.netbeans.modules.gsf.testrunner.api.RerunType;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSession.SessionType;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.NativeProcessChangeEvent;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

/**
 *
 */
public class TestRunnerActionHandler implements ProjectActionHandler, ExecutionListener, RerunHandler {

    private ProjectActionEvent pae;
    private volatile Future<Integer> executorTask;
    private final List<ExecutionListener> listeners = new CopyOnWriteArrayList<ExecutionListener>();
    private NativeExecutionService execution;
    private TestRunnerLineConvertor convertor;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private TestSession session;
    private Manager manager;
    private InputOutput lastIO = null;
    private static final RequestProcessor RP = new RequestProcessor("TestRunnerActionHandler", 1); // NOI18N

    @Override
    public void init(ProjectActionEvent pae, ProjectActionEvent[] paes, Collection<OutputStreamHandler> outputHandlers) {
        this.pae = pae;
    }

    @Override
    public void execute(final InputOutput io) {
        lastIO = io;
        if (SwingUtilities.isEventDispatchThread()) {
            RP.post(new Runnable() {

                @Override
                public void run() {
                    _execute(io);
                }
            });
        } else {
            _execute(io);
        }
    }

    private void _execute(final InputOutput io) {

        final Type actionType = pae.getType();

        if (actionType != ProjectActionEvent.PredefinedType.TEST) {
            assert false;
        }

        final String runDirectory = pae.getProfile().getRunDirectory();
        final MakeConfiguration conf = pae.getConfiguration();
        final PlatformInfo pi = conf.getPlatformInfo();
        final ExecutionEnvironment execEnv = conf.getDevelopmentHost().getExecutionEnvironment();

        String exe = pae.getExecutable(); // we don't need quoting - it's execution responsibility
        // we don't need quoting - it's execution responsibility
        ArrayList<String> args = new ArrayList<String>(Arrays.asList(pae.getProfile().getArgsArray()));
        Map<String, String> env = pae.getProfile().getEnvironment().getenvAsMap();
        boolean showInput = actionType == ProjectActionEvent.PredefinedType.RUN;
        boolean unbuffer = true;

        final CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        String csdirs = compilerSet.getDirectory();
        String commands = CompilerSetUtils.getCommandFolder(compilerSet);
        if (commands != null && commands.length() > 0) {
            // Also add msys to path. Thet's where sh, mkdir, ... are.
            csdirs = csdirs + pi.pathSeparator() + commands;
        }
        String path = env.get(pi.getPathName());
        if (path == null) {
            path = csdirs + pi.pathSeparator() + pi.getPathAsString();
        } else {
            path = csdirs + pi.pathSeparator() + path;
        }
        env.put(pi.getPathName(), path);

        // TODO: this is actual only for sun studio compiler
        env.put("SPRO_EXPAND_ERRORS", ""); // NOI18N
        
        CompilerSet cs = conf.getCompilerSet().getCompilerSet();

        if (conf.getDevelopmentHost().isLocalhost() && Utilities.isWindows()
                && pae.getExecutable().contains("make") // NOI18N
                && CompilerSetUtils.isMsysBased(cs)) {
            env.put("MAKE", WindowsSupport.getInstance().convertToMSysPath(pae.getExecutable())); // NOI18N
        }

        String workingDirectory = convertToRemoteIfNeeded(execEnv, pae.getProject(), runDirectory);

        if (workingDirectory == null) {
            // TODO: fix me
            // return null;
        }

        ProcessChangeListener processChangeListener = new ProcessChangeListener(this);

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv)
                .setWorkingDirectory(workingDirectory)
                .unbufferOutput(unbuffer)
                .setExecutable(exe)
                .setArguments(args.toArray(new String[args.size()]))
                .addNativeProcessListener(processChangeListener);

        npb.getEnvironment().putAll(env);

        NativeExecutionDescriptor descr = null;

        convertor = createTestRunnerConvertor(pae.getProject());
        
        final LineConvertorFactory lcf = new LineConvertorFactory() {
            LineConvertor c = convertor;

            @Override
            public LineConvertor newLineConvertor() {
                return c;
            }
        };

        descr = new NativeExecutionDescriptor().controllable(true).
                frontWindow(false).
                inputVisible(showInput).
                inputOutput(io).
                outLineBased(true).
                showProgress(!CndUtils.isStandalone()).
                postExecution(processChangeListener).
                errConvertorFactory(lcf).
                outConvertorFactory(lcf);

        execution = NativeExecutionService.newService(npb, descr, pae.getActionName());
        runExecution();
    }

    private void runExecution() {
        if (SwingUtilities.isEventDispatchThread()) {
            RP.post(new Runnable() {

                @Override
                public void run() {
                    executorTask = execution.run();
                }
            });
        } else {
            executorTask = execution.run();
        }
    }

    private TestRunnerLineConvertor createTestRunnerConvertor(Project project) {
        Manager.getInstance().setNodeFactory(new CndTestRunnerNodeFactory());
        session = new TestSession("Test", // NOI18N
                project,
                SessionType.TEST);

        session.setRerunHandler(this);

        manager = Manager.getInstance();
        CndUnitHandlerFactory predefinedFactory = new CndUnitHandlerFactory();

        List<TestHandlerFactory> factories = new ArrayList<TestHandlerFactory>();
        
        Collection<? extends TestHandlerFactoryProvider> providers = Lookup.getDefault().lookupAll(TestHandlerFactoryProvider.class);
        for (TestHandlerFactoryProvider provider : providers) {
            factories.add(provider.getFactory());
        }
        factories.add(predefinedFactory);
        
        return new TestRunnerLineConvertor(manager, session, factories);
    }

    /**
     * Refreshes the current session, i.e. clears all currently
     * computed test statuses.
     */
    public synchronized void refresh() {
        if (convertor != null) {
            convertor.refreshSession();
        }
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
        RP.post(new Runnable() {

            @Override
            public void run() {
                Future<Integer> et = executorTask;
                if (et != null) {
                    executorTask.cancel(true);
                }
            }
        });
    }

    protected static String convertToRemoteIfNeeded(ExecutionEnvironment execEnv, Project project, String localDir) {
        if (!checkConnection(execEnv)) {
            return null;
        }
        if (execEnv.isRemote()) {
            return RemoteSyncSupport.getPathMap(execEnv, project).getRemotePath(localDir, false);
        }
        return localDir;
    }

    protected static boolean checkConnection(ExecutionEnvironment execEnv) {
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

    /**
     * Converts absolute Windows paths to paths without the ':'.
     * Example: C:/abc/def.c -> /cygdrive/c/def/c
     */
    public static String normalizeDriveLetter(CompilerSet cs, String path) {
        if (path.length() > 1 && path.charAt(1) == ':') { // NOI18N
            return cs.getCompilerFlavor().getToolchainDescriptor().getDriveLetterPrefix() + path.charAt(0) + path.substring(2); // NOI18N
        }
        return path;
    }

    @Override
    public void executionStarted(int pid) {
        for (ExecutionListener l : listeners) {
            l.executionStarted(pid);
        }
        changeSupport.fireChange();
    }

    @Override
    public void executionFinished(int rc) {
        for (ExecutionListener l : listeners) {
            l.executionFinished(rc);
        }
        changeSupport.fireChange();
    }

    @Override
    public void rerun() {
        if(lastIO != null) {
            refresh();
            execute(lastIO);
        }
    }

    @Override
    public void rerun(Set<Testcase> tests) {
        //not implemented yet
    }

    @Override
    public boolean enabled(RerunType type) {
        return RerunType.ALL.equals(type);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private static final class ProcessChangeListener implements ChangeListener, Runnable {

        private final AtomicReference<NativeProcess> processRef = new AtomicReference<NativeProcess>();
        private final ExecutionListener listener;

        public ProcessChangeListener(ExecutionListener listener) {
            this.listener = listener;
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

            if (!(e instanceof NativeProcessChangeEvent)) {
                return;
            }
        }

        @Override
        public void run() {
            NativeProcess process = processRef.get();
            
            if (process != null && listener != null) {
                listener.executionFinished(process.exitValue());
            }
        }
    }

}
