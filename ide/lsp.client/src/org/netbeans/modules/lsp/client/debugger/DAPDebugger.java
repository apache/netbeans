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
package org.netbeans.modules.lsp.client.debugger;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URI;
import org.netbeans.modules.lsp.client.debugger.api.DAPConfiguration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
import org.eclipse.lsp4j.debug.Capabilities;
import org.eclipse.lsp4j.debug.ConfigurationDoneArguments;
import org.eclipse.lsp4j.debug.ContinueArguments;
import org.eclipse.lsp4j.debug.ContinuedEventArguments;
import org.eclipse.lsp4j.debug.DisconnectArguments;
import org.eclipse.lsp4j.debug.EvaluateArguments;
import org.eclipse.lsp4j.debug.InitializeRequestArguments;
import org.eclipse.lsp4j.debug.NextArguments;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.OutputEventArgumentsCategory;
import org.eclipse.lsp4j.debug.PauseArguments;
import org.eclipse.lsp4j.debug.ScopesArguments;
import org.eclipse.lsp4j.debug.SetBreakpointsArguments;
import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.SourceBreakpoint;
import org.eclipse.lsp4j.debug.StackTraceArguments;
import org.eclipse.lsp4j.debug.StepInArguments;
import org.eclipse.lsp4j.debug.StepOutArguments;
import org.eclipse.lsp4j.debug.SteppingGranularity;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.TerminateArguments;
import org.eclipse.lsp4j.debug.TerminatedEventArguments;
import org.eclipse.lsp4j.debug.Thread;
import org.eclipse.lsp4j.debug.ThreadEventArguments;
import org.eclipse.lsp4j.debug.ThreadEventArgumentsReason;
import org.eclipse.lsp4j.debug.VariablesArguments;
import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.io.InputOutput;
import org.netbeans.modules.lsp.client.debugger.spi.BreakpointConvertor;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.debugger.SessionProvider;
import org.openide.text.Line;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.lsp.client.debugger.spi.BreakpointConvertor.ConvertedBreakpointConsumer;

public final class DAPDebugger implements IDebugProtocolClient {
    public static final String ENGINE_TYPE_ID = "DAPDebuggerEngine";
    public static final String SESSION_TYPE_ID = "DAPDebuggerSession";
    public static final String DEBUGGER_INFO_TYPE_ID = "DAPDebuggerInfo";

    private static final Logger LOG = Logger.getLogger(DAPDebugger.class.getName());
    private static final RequestProcessor WORKER = new RequestProcessor(DAPDebugger.class.getName(), 1, false, false);

    private final DAPDebuggerEngineProvider   engineProvider;
    private final Session                     session;
    private final ContextProvider             contextProvider;
    private final DebuggerManagerListener     updateBreakpointsListener;

    private final ChangeSupport cs = new ChangeSupport(this);
    private final CompletableFuture<Void> initialized = new CompletableFuture<>();
    private final CompletableFuture<Void> terminated = new CompletableFuture<>();
    private final AtomicBoolean suspended = new AtomicBoolean();
    private final Map<Integer, DAPThread> id2Thread = new HashMap<>(); //TODO: concurrent/synchronization!!!
    private final AtomicReference<Runnable> runAfterConfigureDone = new AtomicReference<>();
    private URLPathConvertor fileConvertor;
    private InputStream in;
    private Future<Void> launched;
    private IDebugProtocolServer server;
    private int currentThreadId = -1;

    public DAPDebugger(ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        // init engineProvider
        this.engineProvider = (DAPDebuggerEngineProvider) contextProvider.lookupFirst(null, DebuggerEngineProvider.class);
        this.session = contextProvider.lookupFirst(null, Session.class);
        this.updateBreakpointsListener = new DebuggerManagerAdapter() {
            @Override
            public void breakpointAdded(Breakpoint breakpoint) {
                updateAfterBreakpointChange(breakpoint);
            }
            @Override
            public void breakpointRemoved(Breakpoint breakpoint) {
                updateAfterBreakpointChange(breakpoint);
            }
            private void updateAfterBreakpointChange(Breakpoint breakpoint) {
                Set<String> modifiedURLs =
                        convertBreakpoints(breakpoint).stream()
                                                      .map(b -> b.url())
                                                      .collect(Collectors.toSet());

                try {
                    setBreakpoints(d -> modifiedURLs.contains(d.url()));
                } catch (InterruptedException | ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        DebuggerManager.getDebuggerManager().addDebuggerListener(DebuggerManager.PROP_BREAKPOINTS, updateBreakpointsListener);
    }

    public CompletableFuture<Void> connect(DAPConfiguration config, Type type) throws Exception {
        fileConvertor = DEFAULT_CONVERTOR;
        in = DAPConfigurationAccessor.getInstance().getIn(config);
        Launcher<IDebugProtocolServer> serverLauncher = DSPLauncher.createClientLauncher(this, in, DAPConfigurationAccessor.getInstance().getOut(config));//, false, new PrintWriter(System.err));
        launched = serverLauncher.startListening();
        server = serverLauncher.getRemoteProxy();
        InitializeRequestArguments initialize = new InitializeRequestArguments();
        initialize.setAdapterID("dap");
        initialize.setLinesStartAt1(true);
        initialize.setColumnsStartAt1(true);
        Capabilities cap = server.initialize(initialize).get();
        CompletableFuture<Void> connection;
        if (!DAPConfigurationAccessor.getInstance().getDelayLaunch(config)) {
            connection = switch (type) {
                case ATTACH -> server.attach(DAPConfigurationAccessor.getInstance().getConfiguration(config));
                case LAUNCH -> server.launch(DAPConfigurationAccessor.getInstance().getConfiguration(config));
                default -> throw new UnsupportedOperationException("Unknown type: " + type);
            };
        } else {
            connection = new CompletableFuture<>();
            runAfterConfigureDone.set(() -> {
                (switch (type) {
                    case ATTACH -> server.attach(DAPConfigurationAccessor.getInstance().getConfiguration(config));
                    case LAUNCH -> server.launch(DAPConfigurationAccessor.getInstance().getConfiguration(config));
                    default -> throw new UnsupportedOperationException("Unknown type: " + type);
                }).handle((r, ex) -> {
                    if (ex != null) {
                        connection.completeExceptionally(ex);
                    } else {
                        connection.complete(r);
                    }
                    return null;
                });
            });
        }
        return CompletableFuture.allOf(connection, initialized);
    }

    @Override
    public void initialized() {
        WORKER.post(() -> {
            try {
                setBreakpoints(d -> true);
                server.configurationDone(new ConfigurationDoneArguments()).get();
                initialized.complete(null);
                Runnable r = runAfterConfigureDone.get();
                if (r != null) {
                    r.run();
                    runAfterConfigureDone.set(null);
                }
            } catch (ExecutionException | InterruptedException ex) {
                LOG.log(Level.FINE, null, ex);
                initialized.completeExceptionally(ex);
            }
        });
    }

    private void setBreakpoints(Predicate<LineBreakpointData> filter) throws InterruptedException, ExecutionException {
        Map<String, List<SourceBreakpoint>> url2Breakpoints = new HashMap<>();

        for (LineBreakpointData data : convertBreakpoints(DebuggerManager.getDebuggerManager().getBreakpoints())) {
            if (data != null && filter.test(data)) {
                SourceBreakpoint lb = new SourceBreakpoint();

                lb.setLine(data.lineNumber());
                lb.setCondition(data.condition());

                String path = fileConvertor.toPath(data.url());

                if (path != null) {
                    url2Breakpoints.computeIfAbsent(path, x -> new ArrayList<>())
                                   .add(lb);
                }
            }
        }

        for (Entry<String, List<SourceBreakpoint>> e : url2Breakpoints.entrySet()) {
            Source src = new Source();

            src.setPath(e.getKey());

            SetBreakpointsArguments breakpoints = new SetBreakpointsArguments();

            breakpoints.setSource(src);
            breakpoints.setBreakpoints(e.getValue().toArray(SourceBreakpoint[]::new));
            server.setBreakpoints(breakpoints).get(); //wait using .get()?
        }
    }

    private List<LineBreakpointData> convertBreakpoints(Breakpoint... breakpoints) {
        List<LineBreakpointData> lineBreakpoints = new ArrayList<>();
        ConvertedBreakpointConsumer consumer = SPIAccessor.getInstance().createConvertedBreakpointConsumer(lineBreakpoints);

        //TODO: could cache the convertors:
        for (BreakpointConvertor convertor : Lookup.getDefault().lookupAll(BreakpointConvertor.class)) {
            for (Breakpoint b : breakpoints) {
                convertor.covert(b, consumer);
            }
        }

        return lineBreakpoints;
    }

    @Override
    public void continued(ContinuedEventArguments args) {
        continued();
    }

    private void continued() {
        suspended.set(false);
        DAPThread prevThread = getCurrentThread();
        if (prevThread != null) {
            prevThread.setStack(new DAPFrame[0]);
            prevThread.setStatus(DAPThread.Status.RUNNING);
        }
        currentThreadId = -1;
        cs.fireChange(); //TODO: in a different thread?
        Utils.unmarkCurrent();
    }

    @Override
    public void stopped(StoppedEventArguments args) {
        //TODO: thread id is optional here(?!)
        currentThreadId = args.getThreadId();
        suspended.set(true);
        DAPThread currentThread = getCurrentThread();
        currentThread.setStatus(DAPThread.Status.SUSPENDED);
        //TODO: the focus hint! maybe we don't want to change the current thread?
        DebuggerManager.getDebuggerManager().setCurrentSession(session);
        cs.fireChange(); //TODO: in a different thread?
        StackTraceArguments sta = new StackTraceArguments();
        sta.setThreadId(args.getThreadId());
        server.stackTrace(sta).thenAccept(resp -> {
            DAPFrame[] frames =
                    Arrays.stream(resp.getStackFrames())
                          .map(frame -> new DAPFrame(fileConvertor, currentThread, frame))
                          .toArray(DAPFrame[]::new);
            currentThread.setStack(frames);
        });
    }

    @Override
    public void terminated(TerminatedEventArguments args) {
        initialized.complete(null);
        terminated.complete(null);
        WORKER.post(() -> { //TODO: what if something else is running in WORKER? And OK to coalescence all the below?
            cs.fireChange(); //TODO: in a different thread?
            engineProvider.getDestructor().killEngine();
            Utils.unmarkCurrent(); //TODO: can this be done cleaner?
            DebuggerManager.getDebuggerManager().removeDebuggerListener(DebuggerManager.PROP_BREAKPOINTS, updateBreakpointsListener);
            launched.cancel(true);
            try {
                //XXX: cleaner
                in.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

    public CompletableFuture<Void> getTerminated() {
        return terminated;
    }

    public boolean isSuspended() {
        return suspended.get();
    }

    public void resume() {
        ContinueArguments args = new ContinueArguments();
        //some servers (e.g. the GraalVM DAP server) require the threadId to be always set, even if singleThread is set to false
        args.setThreadId(currentThreadId);
        args.setSingleThread(Boolean.FALSE);

        continued();
        server.continue_(args);
    }

    public void stepInto() {
        StepInArguments args = new StepInArguments();

        args.setSingleThread(true);
        args.setThreadId(currentThreadId);
        args.setGranularity(SteppingGranularity.LINE);

        continued();
        server.stepIn(args);
    }

    public void stepOver() {
        NextArguments args = new NextArguments();

        args.setSingleThread(true);
        args.setThreadId(currentThreadId);
        args.setGranularity(SteppingGranularity.LINE);

        continued();
        server.next(args);
    }

    public void stepOut() {
        StepOutArguments args = new StepOutArguments();

        args.setSingleThread(true);
        args.setThreadId(currentThreadId);

        continued();
        server.stepOut(args);
    }

    public void pause() {
        server.threads().thenAccept(response -> {
            for (Thread t : response.getThreads()) {
                PauseArguments args = new PauseArguments();

                args.setThreadId(t.getId());
                server.pause(args);
            }
        });
    }

    public CompletableFuture<DAPVariable> evaluate(DAPFrame frame, String expression) {
        EvaluateArguments args = new EvaluateArguments();

        //TODO: context?
        args.setExpression(expression);

        if (frame != null) {
            args.setFrameId(frame.getId());
        }

        return server.evaluate(args).thenApply(evaluated -> {
            return new DAPVariable(this, frame, null, evaluated.getVariablesReference(), expression, evaluated.getType(), evaluated.getResult(), Integer.MAX_VALUE); //TODO: totalChildren
        });
    }

    public void finish() {
        DisconnectArguments args = new DisconnectArguments();
        server.disconnect(args).handle((result, exc) -> {
            if (!terminated.isDone()) {
                terminated(null);
            }
            return null;
        });
    }

    public DAPFrame getCurrentFrame() {
        DAPThread currentThread = getCurrentThread();

        if (currentThread == null) {
            return null;
        }

        return currentThread.getCurrentFrame();
    }

    public Line getCurrentLine() {
        DAPThread currentThread = getCurrentThread();

        if (currentThread == null) {
            return null;
        }

        return currentThread.getCurrentLine();
    }

    public CompletableFuture<List<DAPVariable>> getFrameVariables(DAPFrame frame) {
        ScopesArguments args = new ScopesArguments();

        args.setFrameId(frame.getId());

        //TODO: the various attributes:
        return server.scopes(args).thenApply(scopesResponse -> {
            return Arrays.stream(scopesResponse.getScopes())
                         .map(scope -> new DAPVariable(this, frame, null, scope.getVariablesReference(), scope.getName(), "", "", Integer.MAX_VALUE)) //TODO: totalChildren
                         .toList();
        });
    }

    public CompletableFuture<List<DAPVariable>> getVariableChildren(DAPFrame frame, DAPVariable parentVariable) {
        VariablesArguments args = new VariablesArguments();

        args.setVariablesReference(parentVariable.getVariableReference());

        return server.variables(args).thenApply(variablesResponse -> {
            return Arrays.stream(variablesResponse.getVariables())
                         .map(var -> new DAPVariable(this, frame, parentVariable, var.getVariablesReference(), var.getName(), var.getType(), var.getValue(), Integer.MAX_VALUE))
                         .toList();
        });
    }

    public DAPThread getCurrentThread() {
        if (currentThreadId == (-1)) {
            return null;
        }

        return getThread(currentThreadId);
    }

    public DAPThread[] getThreads() {
        return id2Thread.values().toArray(DAPThread[]::new);
    }

    public void thread(ThreadEventArguments args) {
        switch (args.getReason()) {
            case ThreadEventArgumentsReason.STARTED -> getThread(args.getThreadId()).setStatus(DAPThread.Status.CREATED);
            case ThreadEventArgumentsReason.EXITED -> getThread(args.getThreadId()).setStatus(DAPThread.Status.EXITED);
        }
        server.threads().thenAccept(allThreads -> {
            for (Thread t : allThreads.getThreads()) {
                getThread(t.getId()).setName(t.getName());
            }
        });
    }

    @Override
    public void output(OutputEventArguments args) {
        switch (args.getCategory()) {
            case OutputEventArgumentsCategory.CONSOLE,
                 OutputEventArgumentsCategory.IMPORTANT -> getConsoleIO().getOut().print(args.getOutput());
            case OutputEventArgumentsCategory.STDOUT -> getDebugeeIO().getOut().print(args.getOutput());
            case OutputEventArgumentsCategory.STDERR -> getDebugeeIO().getErr().print(args.getOutput());
            case OutputEventArgumentsCategory.TELEMETRY -> {}
        }
    }

    private InputOutput console;
    private InputOutput getConsoleIO() {
        if (console == null) { //TODO: synchronization!!
            console = InputOutput.get(session.getName() + ": Debugger console", false);
        }
        return console;
    }

    private InputOutput debugeeIO;
    private InputOutput getDebugeeIO() {
        //TODO: might be injected from the outside, presumably (for attach, although can we get here from attach?)
        if (debugeeIO == null) { //TODO: synchronization!!
            debugeeIO = InputOutput.get(session.getName(), false);
        }
        return debugeeIO;
    }

    private DAPThread getThread(int id) {
        return id2Thread.computeIfAbsent(id, _id -> new DAPThread(this, _id));
    }

    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }

    public static void startDebugger(DAPConfiguration config, Type type) throws Exception {
        SessionProvider sessionProvider = new SessionProvider () {
            @Override
            public String getSessionName () {
                return DAPConfigurationAccessor.getInstance().getSessionName(config);
            }

            @Override
            public String getLocationName () {
                return "localhost";
            }

            @Override
            public String getTypeID () {
                return SESSION_TYPE_ID;
            }

            @Override
            public Object[] getServices () {
                return new Object[] {};
            }
        };
        List<Object> allServices = new ArrayList<>();
        allServices.add(config);
        allServices.add(sessionProvider);
        DebuggerInfo di = DebuggerInfo.create(
            DEBUGGER_INFO_TYPE_ID,
            allServices.toArray(Object[]::new)
        );
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager ().
            startDebugging (di);
        DAPDebugger debugger = es[0].lookupFirst(null, DAPDebugger.class);
        debugger.connect(config, type);
    }

    //non-standard extension of vscode-js-debug:
    @JsonRequest
    public CompletableFuture<Object> attachedChildSession(Map<String, Object> args) {
        Map<String, Object> config = (Map<String, Object>) args.get("config");
        CompletableFuture<Object> result = new CompletableFuture<>();
        try {
            int port = Integer.parseInt((String) config.get("__jsDebugChildServer"));
            Socket newSocket = new Socket("localhost", port);
            DAPConfiguration.create(newSocket.getInputStream(), newSocket.getOutputStream()).setSessionName((String) config.get("name")).attach();
            result.complete(new HashMap<>());
        } catch (Exception ex) {
            LOG.log(Level.FINE, null, ex);
            result.completeExceptionally(ex);
        }
        return result;
    }

    public enum Type {LAUNCH, ATTACH}

    private static final URLPathConvertor DEFAULT_CONVERTOR = new URLPathConvertor() {
        @Override
        public String toPath(String file) {
            if (file.startsWith("file:")) {
                return URI.create(file).getPath();
            }

            return null;
        }

        @Override
        public String toURL(String path) {
            return "file:" + path;
        }
    };

    public interface URLPathConvertor {
        public String toPath(String url);
        public String toURL(String path);
    }
}
