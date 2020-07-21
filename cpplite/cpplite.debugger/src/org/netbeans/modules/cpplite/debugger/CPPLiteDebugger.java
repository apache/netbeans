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

package org.netbeans.modules.cpplite.debugger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MICommand;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MICommandInjector;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIProxy;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIRecord;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIResult;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MITList;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MITListItem;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIUserInteraction;
import org.netbeans.modules.cpplite.debugger.breakpoints.CPPLiteBreakpoint;
import org.netbeans.modules.cpplite.debugger.breakpoints.BreakpointModel;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.pty.Pty;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.debugger.SessionProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 * Ant debugger.
 *
 * @author  Honza
 */
public class CPPLiteDebugger extends ActionsProviderSupport {

    private static final Logger logger = Logger.getLogger(CPPLiteDebugger.class.getName());
    
    /** The ReqeustProcessor used by action performers. */
    private static RequestProcessor     actionsRequestProcessor;
    private static RequestProcessor     killRequestProcessor;
    
    private CPPLiteDebuggerConfig       configuration;
    private CPPLiteDebuggerEngineProvider   engineProvider;
    private ContextProvider             contextProvider;
    private Process                     debuggee;
    private MIProxy                     proxy;
    private Object                      currentLine;
    private LinkedList                  callStackList = new LinkedList();
    private volatile boolean            suspended = false;
    private final List<StateListener>   stateListeners = new CopyOnWriteArrayList<StateListener>();
    
    private VariablesModel              variablesModel;
    private WatchesModel                watchesModel;
    private BreakpointModel             breakpointModel;
    
    public CPPLiteDebugger (
        ContextProvider contextProvider
    ) {
        
        this.contextProvider = contextProvider;
        
        // init antCookie
        configuration = contextProvider.lookupFirst(null, CPPLiteDebuggerConfig.class);
        
        // init engineProvider
        engineProvider = (CPPLiteDebuggerEngineProvider) contextProvider.lookupFirst 
            (null, DebuggerEngineProvider.class);
                
        // init actions
        for (Iterator it = actions.iterator(); it.hasNext(); ) {
            setEnabled (it.next(), true);
        }
    }
    
    void setDebuggee(Process debuggee) {
        this.debuggee = debuggee;

        //XXX: asynchronous?
        CPPLiteInjector injector = new CPPLiteInjector(debuggee.getOutputStream());
        
        CountDownLatch waitStarted = new CountDownLatch(1);

        this.proxy = new MIProxy(injector, "(gdb)", "UTF-8") {
            @Override
            protected void prompt() {
                waitStarted.countDown();
            }

            @Override
            protected void execAsyncOutput(MIRecord record) {
                if (record.token() == 0) {
                    switch (record.cls()) {
                        case "stopped":
                            String reason = record.results().getConstValue("reason", "");
                            switch (reason) {
                                case "exited-normally":
                                    finish();
                                    break;
                                default:
                                    Frame frame = new Frame((MITList) record.results().valueOf("frame")); //XXX: frame may be missing
                                    Line currentLine = frame.location();
                                    Utils.markCurrent(new Line[] {currentLine});
                                    Utils.showLine(new Line[] {currentLine});
                                    setSuspended(true);
                                    suspended();
                                    break;
                            }
                            break;
                        case "running":
                            setSuspended(false);
                            Utils.unmarkCurrent();
                            running();
                            break;
                        default:
                            //unknown class, ignore
                            System.err.println("Unknown class:" + record.cls());
                            break;
                    }
                    return;
                }
                super.execAsyncOutput(record);
            }
            
        };

        new Thread(() -> {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(debuggee.getInputStream()))) {
                String line;
            
                while ((line = r.readLine()) != null) {
                    proxy.processLine(line);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }).start();
        
        try {
            waitStarted.await();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        for (Breakpoint b : DebuggerManager.getDebuggerManager ().getBreakpoints ()) {
            if (b instanceof CPPLiteBreakpoint) {
                CPPLiteBreakpoint cpplineBreakpoint = (CPPLiteBreakpoint) b;
                Line l = cpplineBreakpoint.getLine();
                FileObject source = l.getLookup().lookup(FileObject.class);
                File sourceFile = source != null ? FileUtil.toFile(source) : null;
                if (sourceFile != null) {
                    proxy.send(new SimpleCommand("-break-insert " + sourceFile.getAbsolutePath() + ":" + (l.getLineNumber() + 1)));
                }
            }
        }

        proxy.send(new SimpleCommand("-exec-run"));
    }

    // ActionsProvider .........................................................
    
    private static final Set<Object> actions = new HashSet<>();
    private static final Set<Object> actionsToDisable = new HashSet<>();
    static {
        actions.add (ActionsManager.ACTION_KILL);
        actions.add (ActionsManager.ACTION_CONTINUE);
        actions.add (ActionsManager.ACTION_START);
        actions.add (ActionsManager.ACTION_STEP_INTO);
        actions.add (ActionsManager.ACTION_STEP_OVER);
        actions.add (ActionsManager.ACTION_STEP_OUT);
        actionsToDisable.addAll(actions);
        // Ignore the KILL action
        actionsToDisable.remove(ActionsManager.ACTION_KILL);
    }
    
    @Override
    public Set getActions () {
        return actions;
    }
        
    @Override
    public void doAction (Object action) {
        logger.log(Level.FINE, "CPPLiteDebugger.doAction({0}), is kill = {1}", new Object[]{action, action == ActionsManager.ACTION_KILL});
        if (action == ActionsManager.ACTION_KILL) {
            finish ();
        } else
        if (action == ActionsManager.ACTION_CONTINUE) {
            proxy.send(new SimpleCommand("-exec-continue"));
        } else
        if (action == ActionsManager.ACTION_START) {
            return ;
        } else
        if ( action == ActionsManager.ACTION_STEP_INTO ||
             action == ActionsManager.ACTION_STEP_OUT ||
             action == ActionsManager.ACTION_STEP_OVER
        ) {
            doStep (action);
        }
    }
    
    private static class CPPLiteInjector implements MICommandInjector {

        private final OutputStream out;

        public CPPLiteInjector(OutputStream out) {
            this.out = out;
        }
        
        @Override
        public void inject(String data) {
            try {
                out.write(data.getBytes());
                out.flush();
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public void log(String data) {
            System.err.println(data);
        }

    }
        
    @Override
    public void postAction(final Object action, final Runnable actionPerformedNotifier) {
        if (action == ActionsManager.ACTION_KILL) {
            synchronized (CPPLiteDebugger.class) {
                if (killRequestProcessor == null) {
                    killRequestProcessor = new RequestProcessor("CPPLite debugger finish RP", 1);
                }
            }
            killRequestProcessor.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        doAction(action);
                    } finally {
                        actionPerformedNotifier.run();
                    }
                }
            });
            return ;
        }
        setDebugActionsEnabled(false);
        synchronized (CPPLiteDebugger.class) {
            if (actionsRequestProcessor == null) {
                actionsRequestProcessor = new RequestProcessor("CPPLite debugger actions RP", 1);
            }
        }
        actionsRequestProcessor.post(new Runnable() {
            @Override
            public void run() {
                try {
                    doAction(action);
                } finally {
                    actionPerformedNotifier.run();
                    setDebugActionsEnabled(true);
                }
            }
        });
    }
    
    private void setDebugActionsEnabled(boolean enabled) {
        for (Object action : actionsToDisable) {
            setEnabled(action, enabled);
        }
    }
    
    
    // other methods ...........................................................
    
    public boolean isSuspended() {
        return suspended;
    }
    
    private void setSuspended(boolean suspended) {
        this.suspended = suspended;
        fireStateChanged(suspended);
    }
    
    private void fireStateChanged(boolean suspended) {
        for (StateListener sl : stateListeners) {
            sl.suspended(suspended);
        }
    }
    
    private void fireFinished() {
        for (StateListener sl : stateListeners) {
            sl.finished();
        }
    }
    
    public void addStateListener(StateListener sl) {
        stateListeners.add(sl);
    }
    
    public void removeStateListener(StateListener sl) {
        stateListeners.remove(sl);
    }
    
    private void suspended() {
        proxy.send(new SimpleCommand("-stack-list-frames") {
            @Override
            protected void onDone(MIRecord record) {
                callStackList.clear();
                for (MITListItem frame : record.results().valueOf("stack").asList()) {
                    callStackList.add(new Frame((MITList) ((MIResult) frame).value()));
                }
                getCallStackModel().fireChanges();
            }
        });
        fireWatches();
        fireBreakpoints();
    }

    private void running() {
        callStackList.clear();
        getCallStackModel().fireChanges();
    }
    public Object getCurrentLine () {
        return currentLine;
    }

    private volatile boolean finished = false; // When the debugger has finished.
    
    public boolean isFinished() {
        return finished;
    }

    /**
     * should define callStack based on callStackInternal & action.
     */
    private void doStep (Object action) {
        if (action == ActionsManager.ACTION_STEP_OVER) {
            proxy.send(new SimpleCommand("-exec-next"));
        } else if (action == ActionsManager.ACTION_STEP_INTO) {
            proxy.send(new SimpleCommand("-exec-step"));
        }
    }

    private void finish () {
        logger.fine("CPPLiteDebugger.finish()");
        if (finished) {
            logger.fine("finish(): already finished.");
            return ;
        }
        proxy.send(new SimpleCommand("-gdb-exit"));
        Utils.unmarkCurrent ();
        engineProvider.getDestructor().killEngine();
        finished = true;
        fireFinished();
        logger.fine("finish() done, build finished.");
    }
    
    
    // support for call stack ..................................................
    
    private CallStackModel              callStackModel;

    private CallStackModel getCallStackModel () {
        if (callStackModel == null) {
            callStackModel = (CallStackModel) contextProvider.lookupFirst 
                ("CallStackView", TreeModel.class);
        }
        return callStackModel;
    }
    
    
    Object[] getCallStack () {
        Object[] callStack;
        callStack = callStackList.toArray();
        return callStack;
    }

    // support for variables ...................................................
    
    synchronized void setVariablesModel(VariablesModel variablesModel) {
        this.variablesModel = variablesModel;
    }

    synchronized void setWatchesModel(WatchesModel watchesModel) {
        this.watchesModel = watchesModel;
    }

    private void fireVariables () {
        synchronized(this) {
            if (variablesModel == null) {
                return ;
            }
        }
        variablesModel.fireChanges();
    }
    
    private void fireWatches () {
        synchronized(this) {
            if (watchesModel == null) {
                return ;
            }
        }
        watchesModel.fireChanges();
    }
    
    private void fireBreakpoints () {
        synchronized(this) {
            if (breakpointModel == null) {
                List<? extends NodeModel> bpNodeModels = DebuggerManager.getDebuggerManager().lookup("BreakpointsView", NodeModel.class);
                for (NodeModel model : bpNodeModels) {
                    if (model instanceof BreakpointModel) {
                        breakpointModel = (BreakpointModel) model;
                        break;
                    }
                }
            }
        }
        breakpointModel.fireChanges();
    }
    
    String evaluate (String expression) {
        String value = getVariableValue (expression);
        if (value != null) {
            return value;
        }
        CountDownLatch done = new CountDownLatch(1);
        String[] varName = new String[1];
        String[] resultValue = new String[1];
        proxy.send(new SimpleCommand("-var-create - * " + expression) {
            @Override
            protected void onDone(MIRecord record) {
                MITList results = record.results();
                varName[0] = results.valueOf("name").asConst().value();
                resultValue[0] = results.valueOf("value").asConst().value();
                done.countDown();
            }
            @Override
            protected void onError(MIRecord record) {
                resultValue[0] = record.toString();
                done.countDown();
            }
        });
        try {
            done.await();
        } catch (InterruptedException ex) {
            return resultValue[0];
        }
        if (varName[0] != null) {
            proxy.send(new SimpleCommand("-var-delete " + varName[0]));
        }
        return resultValue[0];
    }

    private String[] variables = new String [0];
    
    String[] getVariables () {
        return variables;
    }
    
    String getVariableValue (String variableName) {
        synchronized (this) {
            return null;
        }
    }
    
    public interface StateListener {
        
        void suspended(boolean suspended);
        
        void finished();
        
    }

    private static class SimpleCommand extends MICommand {

        public SimpleCommand(String command) {
            super(0, command);
        }

        @Override
        protected void onDone(MIRecord record) {}

        @Override
        protected void onRunning(MIRecord record) {}

        @Override
        protected void onError(MIRecord record) {}

        @Override
        protected void onExit(MIRecord record) {}

        @Override
        protected void onStopped(MIRecord record) {}

        @Override
        protected void onOther(MIRecord record) {}

        @Override
        protected void onUserInteraction(MIUserInteraction ui) {}
    }

    public static class Frame {
        public final String shortFileName;
        public final String fullFileName;
        public final String functionName;
        public final int line;
        public final int level;

        public Frame(MITList frame) {
            if (frame == null || frame.valueOf("file") == null || frame.valueOf("file").asConst() == null) {
                System.err.println("!!!");
            }
            this.shortFileName = frame.valueOf("file").asConst().value();
            this.functionName = frame.valueOf("func").asConst().value();
            this.fullFileName = frame.valueOf("fullname").asConst().value();
            this.line = Integer.parseInt(frame.valueOf("line").asConst().value());
            if (frame.valueOf("level") != null) {
                this.level = Integer.parseInt(frame.valueOf("level").asConst().value());
            } else {
                this.level = -1;
            }
        }
    
        public @CheckForNull Line location() {
            FileObject file = FileUtil.toFileObject(FileUtil.normalizeFile(new File(fullFileName)));
            if (file == null) return null;
            LineCookie lc = file.getLookup().lookup(LineCookie.class);
            return lc.getLineSet().getOriginal(line - 1);
        }
    }

    public static @NonNull Pair<CPPLiteDebugger, Process> startDebugging (CPPLiteDebuggerConfig configuration) throws IOException {
        DebuggerInfo di = DebuggerInfo.create (
            "CPPLiteDebuggerInfo",
            new Object[] {
                new SessionProvider () {
                    @Override
                    public String getSessionName () {
                        return configuration.getDisplayName ();
                    }
                    
                    @Override
                    public String getLocationName () {
                        return "localhost";
                    }
                    
                    @Override
                    public String getTypeID () {
                        return "CPPLiteSession";
                    }

                    @Override
                    public Object[] getServices () {
                        return new Object[] {};
                    }
                },
                configuration
            }
        );
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager ().
            startDebugging (di);
        Pty pty = PtySupport.allocate(ExecutionEnvironmentFactory.getLocal());
        CPPLiteDebugger debugger = es[0].lookupFirst(null, CPPLiteDebugger.class);
        List<String> executable = new ArrayList<>();
        executable.add("gdb");
        executable.add("--interpreter=mi");
        executable.add("--tty=" + pty.getSlaveName());
        executable.addAll(configuration.getExecutable());
        Process debuggee = new ProcessBuilder(executable).start();
        new RequestProcessor(configuration.getDisplayName() + " (pty deallocator)").post(() -> {
            try {
                while (debuggee.isAlive()) {
                    try {
                        debuggee.waitFor();
                    } catch (InterruptedException ex) {
                        //ignore...
                    }
                }
            } finally {
                try {
                    PtySupport.deallocate(pty);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        debugger.setDebuggee(debuggee);

        return Pair.of(debugger, new Process() {
            @Override
            public OutputStream getOutputStream() {
                return pty.getOutputStream();
            }

            @Override
            public InputStream getInputStream() {
                return pty.getInputStream();
            }

            @Override
            public InputStream getErrorStream() {
                return pty.getErrorStream();
            }

            @Override
            public int waitFor() throws InterruptedException {
                return debuggee.waitFor();
            }

            @Override
            public int exitValue() {
                return debuggee.exitValue();
            }

            @Override
            public void destroy() {
                debuggee.destroy();
            }
        });
    }
}
