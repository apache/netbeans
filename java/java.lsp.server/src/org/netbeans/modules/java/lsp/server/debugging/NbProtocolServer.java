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
package org.netbeans.modules.java.lsp.server.debugging;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.lsp4j.debug.Capabilities;
import org.eclipse.lsp4j.debug.ConfigurationDoneArguments;
import org.eclipse.lsp4j.debug.ContinueArguments;
import org.eclipse.lsp4j.debug.ContinueResponse;
import org.eclipse.lsp4j.debug.DisconnectArguments;
import org.eclipse.lsp4j.debug.EvaluateArguments;
import org.eclipse.lsp4j.debug.EvaluateResponse;
import org.eclipse.lsp4j.debug.ExceptionBreakpointsFilter;
import org.eclipse.lsp4j.debug.ExceptionInfoArguments;
import org.eclipse.lsp4j.debug.ExceptionInfoResponse;
import org.eclipse.lsp4j.debug.InitializeRequestArguments;
import org.eclipse.lsp4j.debug.NextArguments;
import org.eclipse.lsp4j.debug.PauseArguments;
import org.eclipse.lsp4j.debug.RestartFrameArguments;
import org.eclipse.lsp4j.debug.Scope;
import org.eclipse.lsp4j.debug.ScopesArguments;
import org.eclipse.lsp4j.debug.ScopesResponse;
import org.eclipse.lsp4j.debug.SetBreakpointsArguments;
import org.eclipse.lsp4j.debug.SetBreakpointsResponse;
import org.eclipse.lsp4j.debug.SetExceptionBreakpointsArguments;
import org.eclipse.lsp4j.debug.SetExceptionBreakpointsResponse;
import org.eclipse.lsp4j.debug.SetVariableArguments;
import org.eclipse.lsp4j.debug.SetVariableResponse;
import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.SourceArguments;
import org.eclipse.lsp4j.debug.SourceResponse;
import org.eclipse.lsp4j.debug.StackFrame;
import org.eclipse.lsp4j.debug.StackTraceArguments;
import org.eclipse.lsp4j.debug.StackTraceResponse;
import org.eclipse.lsp4j.debug.StepInArguments;
import org.eclipse.lsp4j.debug.StepOutArguments;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.ThreadsResponse;
import org.eclipse.lsp4j.debug.VariablesArguments;
import org.eclipse.lsp4j.debug.VariablesResponse;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable;
import org.netbeans.modules.java.lsp.server.LspSession;
import org.netbeans.modules.java.lsp.server.URITranslator;
import org.netbeans.modules.java.lsp.server.debugging.breakpoints.NbBreakpointsRequestHandler;
import org.netbeans.modules.java.lsp.server.debugging.attach.NbAttachRequestHandler;
import org.netbeans.modules.java.lsp.server.debugging.launch.NbDebugSession;
import org.netbeans.modules.java.lsp.server.debugging.launch.NbDisconnectRequestHandler;
import org.netbeans.modules.java.lsp.server.debugging.launch.NbLaunchRequestHandler;
import org.netbeans.modules.java.lsp.server.debugging.variables.NbVariablesRequestHandler;
import org.netbeans.modules.java.lsp.server.debugging.utils.ErrorUtilities;
import org.netbeans.modules.nativeimage.api.debug.EvaluateException;
import org.netbeans.modules.nativeimage.api.debug.NIDebugger;
import org.netbeans.modules.nativeimage.api.debug.NIVariable;
import org.netbeans.spi.debugger.ui.DebuggingView;
import org.netbeans.spi.debugger.ui.DebuggingView.DVFrame;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Dusan Balek
 */
public final class NbProtocolServer implements IDebugProtocolServer, LspSession.ScheduledServer {

    private static final Logger LOGGER = Logger.getLogger(NbProtocolServer.class.getName());
    private static final Level LOGLEVEL = Level.FINE;

    private final DebugAdapterContext context;
    private final NbLaunchRequestHandler launchRequestHandler = new NbLaunchRequestHandler();
    private final NbAttachRequestHandler attachRequestHandler = new NbAttachRequestHandler();
    private final NbDisconnectRequestHandler disconnectRequestHandler = new NbDisconnectRequestHandler();
    private final NbBreakpointsRequestHandler breakpointsRequestHandler = new NbBreakpointsRequestHandler();
    private final NbVariablesRequestHandler variablesRequestHandler = new NbVariablesRequestHandler();
    private final RequestProcessor evaluationRP = new RequestProcessor(NbProtocolServer.class.getName(), 3);
    private final RequestProcessor threadsRP = new RequestProcessor(NbProtocolServer.class.getName(), 1);
    private boolean initialized = false;
    private Future<Void> runningServer;

    public NbProtocolServer(DebugAdapterContext context) {
        this.context = context;
    }

    @Override
    public CompletableFuture<Capabilities> initialize(InitializeRequestArguments args) {
        if (!initialized) {
            initialized = true;
            // Called from postLaunch:
            context.getThreadsProvider().initialize(context, Collections.emptyMap());
        }
        context.setClientLinesStartAt1(args.getLinesStartAt1());
        context.setClientColumnsStartAt1(args.getColumnsStartAt1());
        String pathFormat = args.getPathFormat();
        if (pathFormat != null) {
            switch (pathFormat) {
                case "uri":
                    context.setClientPathsAreUri(true);
                    break;
                default:
                    context.setClientPathsAreUri(false);
            }
        }
        context.setSupportsRunInTerminalRequest(args.getSupportsRunInTerminalRequest());

        Capabilities caps = new Capabilities();
        caps.setSupportsConfigurationDoneRequest(true);
        caps.setSupportsHitConditionalBreakpoints(true);
        caps.setSupportsConditionalBreakpoints(true);
        caps.setSupportsSetVariable(true);
        caps.setSupportTerminateDebuggee(true);
        caps.setSupportsCompletionsRequest(true);
        caps.setSupportsRestartFrame(true);
        caps.setSupportsLogPoints(true);
        caps.setSupportsEvaluateForHovers(true);

        ExceptionBreakpointsFilter uncaught = new ExceptionBreakpointsFilter();
        uncaught.setFilter(NbBreakpointsRequestHandler.UNCAUGHT_EXCEPTION_FILTER_NAME);
        uncaught.setLabel("Uncaught Exceptions");
        ExceptionBreakpointsFilter caught = new ExceptionBreakpointsFilter();
        caught.setFilter(NbBreakpointsRequestHandler.CAUGHT_EXCEPTION_FILTER_NAME);
        caught.setLabel("Caught Exceptions");
        caps.setExceptionBreakpointFilters(new ExceptionBreakpointsFilter[]{uncaught, caught});
        caps.setSupportsExceptionInfoRequest(true);
        return CompletableFuture.completedFuture(caps);
    }

    @Override
    public CompletableFuture<Void> configurationDone(ConfigurationDoneArguments args) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        NbDebugSession debugSession = context.getDebugSession();
        if (debugSession != null) {
            // Breakpoints were submitted, we can resume the debugger
            context.getConfigurationSemaphore().notifyCongigurationDone();
            future.complete(null);
        } else {
            ErrorUtilities.completeExceptionally(future, "Failed to launch debug session, the debugger will exit.", ResponseErrorCode.ServerNotInitialized);
        }
        return future;
    }

    @Override
    public CompletableFuture<Void> launch(Map<String, Object> args) {
        return launchRequestHandler.launch(args, context);
    }

    @Override
    public CompletableFuture<Void> attach(Map<String, Object> args) {
        return attachRequestHandler.attach(args, context);
    }

    @Override
    public CompletableFuture<Void> disconnect(DisconnectArguments args) {
        return disconnectRequestHandler.disconnect(args, context);
    }

    @Override
    public CompletableFuture<SetBreakpointsResponse> setBreakpoints(SetBreakpointsArguments args) {
        return breakpointsRequestHandler.setBreakpoints(args, context);
    }

    @Override
    public CompletableFuture<SetExceptionBreakpointsResponse> setExceptionBreakpoints(SetExceptionBreakpointsArguments args) {
        return breakpointsRequestHandler.setExceptionBreakpoints(args, context);
    }

    @Override
    public CompletableFuture<ContinueResponse> continue_(ContinueArguments args) {
        ContinueResponse response = new ContinueResponse();
        if (args.getThreadId() > 0) {
            DVThread dvThread = context.getThreadsProvider().getThread(args.getThreadId());
            if (dvThread != null) {
                dvThread.resume();
                context.getThreadsProvider().getThreadObjects().cleanObjects(args.getThreadId());
            }
            response.setAllThreadsContinued(false);
        } else {
            Session session = context.getDebugSession().getSession();
            session.getCurrentEngine().getActionsManager().doAction("continue");
            context.getThreadsProvider().getThreadObjects().cleanAll();
            response.setAllThreadsContinued(true);
        }
        return CompletableFuture.completedFuture(response);
    }

    @Override
    public CompletableFuture<Void> next(NextArguments args) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (context.getDebugSession() == null) {
            ErrorUtilities.completeExceptionally(future, "Debug Session doesn't exist.", ResponseErrorCode.InvalidParams);
        } else {
            ActionsManager am = DebuggerManager.getDebuggerManager().getCurrentEngine().getActionsManager();
            am.doAction("stepOver");
            future.complete(null);
        }
        return future;
    }

    @Override
    public CompletableFuture<Void> stepIn(StepInArguments args) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (context.getDebugSession() == null) {
            ErrorUtilities.completeExceptionally(future, "Debug Session doesn't exist.", ResponseErrorCode.InvalidParams);
        } else {
            ActionsManager am = DebuggerManager.getDebuggerManager().getCurrentEngine().getActionsManager();
            am.doAction("stepInto");
            future.complete(null);
        }
        return future;
    }

    @Override
    public CompletableFuture<Void> stepOut(StepOutArguments args) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (context.getDebugSession() == null) {
            ErrorUtilities.completeExceptionally(future, "Debug Session doesn't exist.", ResponseErrorCode.InvalidParams);
        } else {
            ActionsManager am = DebuggerManager.getDebuggerManager().getCurrentEngine().getActionsManager();
            am.doAction("stepOut");
            future.complete(null);
        }
        return future;
    }

    @Override
    public CompletableFuture<Void> pause(PauseArguments args) {
        final StoppedEventArguments ev;
        if (args.getThreadId() > 0) {
            DVThread dvThread = context.getThreadsProvider().getThread(args.getThreadId());
            if (dvThread != null) {
                dvThread.suspend();
                ev = new StoppedEventArguments();
                ev.setReason("pause");
                ev.setThreadId(args.getThreadId());
                ev.setAllThreadsStopped(false);
            } else {
                ev = null;
            }
        } else {
            Session session = context.getDebugSession().getSession();
            session.getCurrentEngine().getActionsManager().doAction("pause");
            ev = new StoppedEventArguments();
            ev.setReason("pause");
            ev.setThreadId(0);
            ev.setAllThreadsStopped(true);
        }
        if (ev != null) {
            context.getClient().stopped(ev);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<StackTraceResponse> stackTrace(StackTraceArguments args) {
        if (context.getDebugSession() == null) {
            CompletableFuture<StackTraceResponse> future = new CompletableFuture<>();
            ErrorUtilities.completeExceptionally(future, "Debug Session doesn't exist.", ResponseErrorCode.InvalidParams);
            return future;
        } else {
            return CompletableFuture.supplyAsync(() -> {
                LOGGER.log(LOGLEVEL, "stackTrace() START");
                long t1 = System.nanoTime();
                List<StackFrame> result = new ArrayList<>();
                int cnt = 0;
                DVThread dvThread = context.getThreadsProvider().getThread(args.getThreadId());
                if (dvThread != null) {
                    cnt = dvThread.getFrameCount();
                    int from = args.getStartFrame() != null ? args.getStartFrame() : 0;
                    int to = args.getLevels() != null ? from + args.getLevels() : Integer.MAX_VALUE;
                    List<DVFrame> stackFrames = dvThread.getFrames(from, to);
                    for (DVFrame frame : stackFrames) {
                        int frameId = context.getThreadsProvider().getThreadObjects().addObject(args.getThreadId(), new NbFrame(args.getThreadId(), frame));
                        int line = frame.getLine();
                        if (line < 0) { // unknown
                            line = 0;
                        }
                        int column = frame.getColumn();
                        if (column < 0) { // unknown
                            column = 0;
                        }
                        StackFrame stackFrame = new StackFrame();
                        stackFrame.setId(frameId);
                        stackFrame.setName(frame.getName());
                        URI sourceURI = frame.getSourceURI();
                        if (sourceURI != null) {
                            sourceURI = URI.create(URITranslator.getDefault().uriToLSP(sourceURI.toString()));
                            Source source = new Source();
                            String scheme = sourceURI.getScheme();
                            Path sourcePath = null;
                            if (null == scheme) {
                                sourcePath = Paths.get(sourceURI.getPath());
                            } else if ("file".equalsIgnoreCase(scheme)) {   // NOI18N
                                try {
                                    sourcePath = Paths.get(sourceURI);
                                } catch (FileSystemNotFoundException | SecurityException | IllegalArgumentException ex) {
                                    sourcePath = null;
                                }
                            }
                            if (sourcePath != null) {
                                source.setName(sourcePath.getFileName().toString());
                                source.setPath(sourcePath.toString());
                                source.setSourceReference(0);
                            } else {
                                int ref = context.createSourceReference(sourceURI, frame.getSourceMimeType());
                                String path = sourceURI.getPath();
                                if (path == null || path.isEmpty()) {
                                    path = sourceURI.getSchemeSpecificPart();
                                    while (path.startsWith("//")) {
                                        // Remove multiple initial slashes
                                        path = path.substring(1);
                                    }
                                }
                                if (path != null) {
                                    int sepIndex = Math.max(path.lastIndexOf('/'), path.lastIndexOf(File.separatorChar));
                                    source.setName(path.substring(sepIndex + 1));
                                    if ("jar".equalsIgnoreCase(scheme)) {
                                        try {
                                            path = new URI(path).getPath();
                                        } catch (URISyntaxException ex) {
                                            // ignore, we just tried
                                        }
                                    }
                                    source.setPath(path);
                                }
                                source.setSourceReference(ref);
                            }
                            stackFrame.setSource(source);
                        }
                        stackFrame.setLine(line);
                        stackFrame.setColumn(column);
                        result.add(stackFrame);
                    }
                }
                StackTraceResponse response = new StackTraceResponse();
                response.setStackFrames(result.toArray(new StackFrame[0]));
                response.setTotalFrames(cnt);
                long t2 = System.nanoTime();
                LOGGER.log(LOGLEVEL, "stackTrace() END after {0} ns", (t2 - t1));
                return response;
            }, threadsRP);
        }
    }

    @Override
    @NbBundle.Messages({"MSG_FrameRestartUnsupported=Restart of frames is not supported.",
                        "# {0} - frame pop error message",
                        "MSG_FrameRestartFailed=Unable to restart frame: {0}"})
    public CompletableFuture<Void> restartFrame(RestartFrameArguments args) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        NbFrame stackFrame = (NbFrame) context.getThreadsProvider().getThreadObjects().getObject(args.getFrameId());
        String popError = null;
        if (stackFrame != null) {
            try {
                stackFrame.getDVFrame().popOff();
                ActionsManager am = DebuggerManager.getDebuggerManager().getCurrentEngine().getActionsManager();
                am.doAction("stepInto");
                future.complete(null);
            } catch (UnsupportedOperationException ex) {
                popError = Bundle.MSG_FrameRestartUnsupported();
            } catch (DebuggingView.PopException ex) {
                popError = Bundle.MSG_FrameRestartFailed(ex.getLocalizedMessage());
            }
        }
        if (popError != null) {
            ErrorUtilities.completeExceptionally(future, popError, ResponseErrorCode.InvalidParams);
        }
        return future;
    }

    @Override
    public CompletableFuture<ScopesResponse> scopes(ScopesArguments args) {
        List<Scope> result = new ArrayList<>();
        NbFrame stackFrame = (NbFrame) context.getThreadsProvider().getThreadObjects().getObject(args.getFrameId());
        if (stackFrame != null) {
            stackFrame.getDVFrame().makeCurrent(); // The scopes and variables are always provided with respect to the current frame
            // TODO: Provide Truffle scopes.
            NbScope localScope = new NbScope(stackFrame, "Local");
            int localScopeId = context.getThreadsProvider().getThreadObjects().addObject(stackFrame.getThreadId(), localScope);
            Scope scope = new Scope();
            scope.setName(localScope.getName());
            scope.setVariablesReference(localScopeId);
            scope.setExpensive(false);
            result.add(scope);
        }
        ScopesResponse response = new ScopesResponse();
        response.setScopes(result.toArray(new Scope[0]));
        return CompletableFuture.completedFuture(response);
    }

    @Override
    public CompletableFuture<VariablesResponse> variables(VariablesArguments args) {
        return variablesRequestHandler.variables(args, context);
    }

    @Override
    public CompletableFuture<SetVariableResponse> setVariable(SetVariableArguments args) {
        return variablesRequestHandler.setVariable(args, context);
    }

    @Override
    public CompletableFuture<SourceResponse> source(SourceArguments args) {
        CompletableFuture<SourceResponse> future = new CompletableFuture<>();
        int sourceReference = args.getSourceReference();
        if (sourceReference <= 0) {
            ErrorUtilities.completeExceptionally(future, "SourceRequest: property 'sourceReference' is missing, null, or empty", ResponseErrorCode.InvalidParams);
        } else {
            URI uri = context.getSourceUri(sourceReference);
            NbSourceProvider sourceProvider = context.getSourceProvider();
            SourceResponse response = new SourceResponse();
            response.setMimeType(context.getSourceMimeType(sourceReference));
            response.setContent(sourceProvider.getSourceContents(uri));
            future.complete(response);
        }
        return future;
    }

    @Override
    public CompletableFuture<ThreadsResponse> threads() {
        if (context.getDebugSession() == null) {
            CompletableFuture<ThreadsResponse> future = new CompletableFuture<>();
            ErrorUtilities.completeExceptionally(future, "Debug Session doesn't exist.", ResponseErrorCode.InvalidParams);
            return future;
        } else {
            return CompletableFuture.supplyAsync(() -> {
                LOGGER.log(LOGLEVEL, "threads() START");
                long t1 = System.nanoTime();
                List<org.eclipse.lsp4j.debug.Thread> result = new ArrayList<>();
                context.getThreadsProvider().visitThreads((id, dvThread) -> {
                    org.eclipse.lsp4j.debug.Thread thread = new org.eclipse.lsp4j.debug.Thread();
                    thread.setId(id);
                    thread.setName(dvThread.getName());
                    result.add(thread);
                });
                ThreadsResponse response = new ThreadsResponse();
                response.setThreads(result.toArray(new org.eclipse.lsp4j.debug.Thread[0]));
                long t2 = System.nanoTime();
                LOGGER.log(LOGLEVEL, "threads() END after {0} ns", (t2 - t1));
                return response;
            }, threadsRP);
        }
    }
    
    private EvaluateResponse passToApplication(String args) {
        Writer w = context.getInputSink();
        if (w != null) {
            try {
                w.write(args);
            } catch (IOException ex) {
                // TBD: handle.
            }
        }
        EvaluateResponse resp = new EvaluateResponse();
        resp.setResult("");
        return resp;
    }

    @Override
    public CompletableFuture<EvaluateResponse> evaluate(EvaluateArguments args) {
        return CompletableFuture.supplyAsync(() -> {
            String expression = args.getExpression();
            ThreadObjects obs = context.getThreadsProvider().getThreadObjects();
            if (args.getFrameId() == null || obs == null) {
                return passToApplication(args.getExpression());
            }
            if (StringUtils.isBlank(expression)) {
                throw ErrorUtilities.createResponseErrorException(
                    "Empty expression cannot be evaluated.",
                    ResponseErrorCode.InvalidParams);
            }
            NbFrame stackFrame = (NbFrame)obs.getObject(args.getFrameId());
            if (stackFrame == null) {
                throw ErrorUtilities.createResponseErrorException(
                    "Unknown frame " + args.getFrameId(),
                    ResponseErrorCode.InvalidParams);
            }
            stackFrame.getDVFrame().makeCurrent(); // The evaluation is always performed with respect to the current frame
            DVThread dvThread = stackFrame.getDVFrame().getThread();
            int threadId = context.getThreadsProvider().getId(dvThread);
            EvaluateResponse response = new EvaluateResponse();
            JPDADebugger debugger = context.getDebugSession().getJPDADebugger();
            if (debugger != null) {
                evaluateJPDA(debugger, expression, threadId, response);
            } else {
                NIDebugger niDebugger = context.getDebugSession().getNIDebugger();
                if (niDebugger == null) {
                    throw ErrorUtilities.createResponseErrorException(
                        "No active debugger is found.",
                        ResponseErrorCode.RequestCancelled);
                }
                evaluateNative(niDebugger, expression, threadId, response);
            }
            return response;
        }, evaluationRP);
    }

    private void evaluateJPDA(JPDADebugger debugger, String expression, int threadId, EvaluateResponse response) {
        Variable variable;
        try {
            variable = debugger.evaluate(expression);
        } catch (InvalidExpressionException ex) {
            throw ErrorUtilities.createResponseErrorException(
                ex.getLocalizedMessage(),
                ResponseErrorCode.ParseError);
        }
        TruffleVariable truffleVariable = TruffleVariable.get(variable);
        if (truffleVariable != null) {
            int referenceId = context.getThreadsProvider().getThreadObjects().addObject(threadId, truffleVariable);
            response.setResult(truffleVariable.getDisplayValue());
            response.setVariablesReference(referenceId);
            response.setType(truffleVariable.getType());
            response.setIndexedVariables(truffleVariable.isLeaf() ? 0 : truffleVariable.getChildren().length);
        } else {
            if (variable instanceof ObjectVariable) {
                int referenceId = context.getThreadsProvider().getThreadObjects().addObject(threadId, variable);
                int indexedVariables = ((ObjectVariable) variable).getFieldsCount();
                String toString;
                try {
                    toString = ((ObjectVariable) variable).getToStringValue();
                } catch (InvalidExpressionException ex) {
                    toString = variable.getValue();
                }
                response.setResult(Objects.toString(toString));
                response.setVariablesReference(referenceId);
                response.setType(variable.getType());
                response.setIndexedVariables(Math.max(indexedVariables, 0));
            } else {
                response.setResult(variable.getValue());
                //response.setVariablesReference(0);
                response.setType(variable.getType());
                //response.setIndexedVariables(0);
            }
        }
    }

    private void evaluateNative(NIDebugger niDebugger, String expression, int threadId, EvaluateResponse response) {
        try {
            NIVariable variable = niDebugger.evaluate(expression, null, null);
            int numChildren = variable.getNumChildren();
            if (numChildren > 0) {
                int referenceId = context.getThreadsProvider().getThreadObjects().addObject(threadId, variable);
                response.setResult(variable.getValue());
                response.setVariablesReference(referenceId);
                response.setType(variable.getType());
                response.setIndexedVariables(numChildren);
            } else {
                response.setResult(variable.getValue());
                response.setType(variable.getType());
            }
        } catch (EvaluateException ex) {
            throw ErrorUtilities.createResponseErrorException(
                ex.getLocalizedMessage(),
                ResponseErrorCode.ParseError);
        }
    }

    @Override
    public CompletableFuture<ExceptionInfoResponse> exceptionInfo(ExceptionInfoArguments args) {
        CompletableFuture<ExceptionInfoResponse> future = new CompletableFuture<>();
        Variable exceptionVariable = context.getBreakpointManager().getExceptionOn(args.getThreadId());
        if (exceptionVariable == null) {
            ErrorUtilities.completeExceptionally(future, "No exception exists in thread " + args.getThreadId(), ResponseErrorCode.InvalidParams);
        } else {
            Throwable exception = (Throwable) exceptionVariable.createMirrorObject();
            String typeName = exception.getLocalizedMessage(); // TODO
            String exceptionToString = exception.toString();

            ExceptionInfoResponse response = new ExceptionInfoResponse();
            response.setExceptionId(typeName);
            response.setDescription(exceptionToString);
            //response.setBreakMode(exceptionInfo.isCaught() ? ExceptionBreakMode.ALWAYS : ExceptionBreakMode.USER_UNHANDLED);
            future.complete(response);
        }
        return future;
    }

    void setRunningFuture(Future<Void> runningServer) {
        this.runningServer = runningServer;
    }

    @Override
    public Future<Void> getRunningFuture() {
        return runningServer;
    }
}
