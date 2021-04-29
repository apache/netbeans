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
package org.netbeans.modules.java.lsp.server.debugging.launch;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.extexecution.base.ExplicitProcessParameters;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.debugging.DebugAdapterContext;
import org.netbeans.modules.java.lsp.server.debugging.NbSourceProvider;
import org.netbeans.modules.java.lsp.server.debugging.utils.ErrorUtilities;
import org.netbeans.modules.java.lsp.server.progress.OperationContext;
import org.netbeans.modules.java.lsp.server.progress.ProgressOperationEvent;
import org.netbeans.modules.java.lsp.server.progress.ProgressOperationListener;
import org.netbeans.modules.java.lsp.server.progress.TestProgressHandler;
import org.netbeans.modules.java.lsp.server.protocol.NbLspServer;
import org.netbeans.modules.java.nativeimage.debugger.api.NIDebugRunner;
import org.netbeans.modules.nativeimage.api.debug.NIDebugger;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;

import org.openide.filesystems.FileObject;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author martin
 */
public abstract class NbLaunchDelegate {

    private final RequestProcessor requestProcessor = new RequestProcessor(NbLaunchDelegate.class);

    public abstract void preLaunch(Map<String, Object> launchArguments, DebugAdapterContext context);

    public abstract void postLaunch(Map<String, Object> launchArguments, DebugAdapterContext context);
    
    protected void notifyFinished(DebugAdapterContext ctx, boolean success) {
        // no op.
    }

    public final CompletableFuture<Void> nbLaunch(FileObject toRun, File nativeImageFile, String method, Map<String, Object> launchArguments, DebugAdapterContext context, boolean debug, boolean testRun, Consumer<NbProcessConsole.ConsoleMessage> consoleMessages) {
        CompletableFuture<Void> launchFuture = new CompletableFuture<>();
        NbProcessConsole ioContext = new NbProcessConsole(consoleMessages);
        SingleMethod singleMethod;
        if (method != null) {
            singleMethod = new SingleMethod(toRun, method);
        } else {
            singleMethod = null;
        }
        ActionProgress progress = new ActionProgress() {
            private final AtomicInteger count = new AtomicInteger(0);
            private final AtomicBoolean finalSuccess = new AtomicBoolean(true);
            @Override
            protected void started() {
                count.incrementAndGet();
            }

            @Override
            public void finished(boolean success) {
                if (count.decrementAndGet() <= 0) {
                    ioContext.stop();
                    notifyFinished(context, success && finalSuccess.get());
                } else if (!success) {
                    finalSuccess.set(success);
                }
            }
        };
        if (toRun != null) {
            CompletableFuture<Pair<ActionProvider, String>> commandFuture = findTargetWithPossibleRebuild(toRun, singleMethod, debug, testRun, ioContext);
            commandFuture.thenAccept((providerAndCommand) -> {
                if (debug) {
                    DebuggerManager.getDebuggerManager().addDebuggerListener(new DebuggerManagerAdapter() {
                        @Override
                        public void sessionAdded(Session session) {
                            JPDADebugger debugger = session.lookupFirst(null, JPDADebugger.class);
                            if (debugger != null) {
                                DebuggerManager.getDebuggerManager().removeDebuggerListener(this);
                                Map properties = session.lookupFirst(null, Map.class);
                                NbSourceProvider sourceProvider = context.getSourceProvider();
                                sourceProvider.setSourcePath(properties != null ? (ClassPath) properties.getOrDefault("sourcepath", ClassPath.EMPTY) : ClassPath.EMPTY);
                                debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, new PropertyChangeListener() {
                                    @Override
                                    public void propertyChange(PropertyChangeEvent evt) {
                                        int newState = (int) evt.getNewValue();
                                        if (newState == JPDADebugger.STATE_RUNNING) {
                                            debugger.removePropertyChangeListener(JPDADebugger.PROP_STATE, this);
                                            NbDebugSession debugSession = new NbDebugSession(session);
                                            context.setDebugSession(debugSession);
                                            launchFuture.complete(null);
                                            context.getConfigurationSemaphore().waitForConfigurationDone();
                                        }
                                    }
                                });
                            }
                        }
                    });
                } else {
                    launchFuture.complete(null);
                }
                List<String> args = argsToStringList(launchArguments.get("args"));
                List<String> vmArgs = argsToStringList(launchArguments.get("vmArgs"));
                ExplicitProcessParameters params = ExplicitProcessParameters.empty();
                if (!(args.isEmpty() && vmArgs.isEmpty())) {
                    ExplicitProcessParameters.Builder bld = ExplicitProcessParameters.builder();
                    bld.launcherArgs(vmArgs);
                    bld.args(args);
                    bld.replaceArgs(false);
                    params = bld.build();
                }
                OperationContext ctx = OperationContext.find(Lookup.getDefault());
                ctx.addProgressOperationListener(null, new ProgressOperationListener() {
                    @Override
                    public void progressHandleCreated(ProgressOperationEvent e) {
                        context.setProcessExecutorHandle(e.getProgressHandle());
                    }
                });
                TestProgressHandler testProgressHandler = ctx.getClient().getNbCodeCapabilities().hasTestResultsSupport() ? new TestProgressHandler(ctx.getClient(), context.getClient(), Utils.toUri(toRun)) : null;
                Lookup launchCtx = new ProxyLookup(
                        testProgressHandler != null ? Lookups.fixed(toRun, ioContext, progress, testProgressHandler) : Lookups.fixed(toRun, ioContext, progress),
                        Lookup.getDefault()
                );

                Lookup lookup;
                if (singleMethod != null) {
                    lookup = Lookups.fixed(toRun, singleMethod, params, ioContext, progress);
                } else {
                    lookup = Lookups.fixed(toRun, ioContext, params, progress);
                }
                Lookups.executeWith(launchCtx, () -> {
                    providerAndCommand.first().invokeAction(providerAndCommand.second(), lookup);

                });
            }).exceptionally((t) -> {
                launchFuture.completeExceptionally(t);
                return null;
            });
        } else {
            ExecutionDescriptor executionDescriptor = new ExecutionDescriptor()
                    .showProgress(true)
                    .showSuspended(true)
                    .frontWindowOnError(true)
                    .controllable(true);
            Lookup launchCtx = new ProxyLookup(
                    Lookups.fixed(ioContext, progress),
                    Lookup.getDefault()
            );
            List<String> args = argsToStringList(launchArguments.get("args"));
            if (debug) {
                requestProcessor.post(() -> {
                    Lookups.executeWith(launchCtx, () -> {
                        String miDebugger = (String) launchArguments.get("miDebugger");
                        startNativeDebug(nativeImageFile, args, miDebugger, context, executionDescriptor, launchFuture);
                    });
                });
            } else {
                Lookups.executeWith(launchCtx, () -> {
                    execNative(nativeImageFile, args, context, executionDescriptor, launchFuture);//, success);
                });
            }
        }
        return launchFuture;
    }

    private static void execNative(File nativeImageFile, List<String> args, DebugAdapterContext context, ExecutionDescriptor executionDescriptor, CompletableFuture<Void> launchFuture) {
        ExecutionService.newService(() -> {
            launchFuture.complete(null);
            List<String> command = args.isEmpty() ? Collections.singletonList(nativeImageFile.getAbsolutePath()) : join(nativeImageFile.getAbsolutePath(), args);
            try {
                return new ProcessBuilder(command).start();
            } catch (IOException ex) {
                ErrorUtilities.completeExceptionally(launchFuture,
                    "Failed to run debuggee native image: " + ex.getLocalizedMessage(),
                    ResponseErrorCode.serverErrorStart);
                throw ex;
            }
        }, executionDescriptor, "Run - " + nativeImageFile.getName()).run();
    }

    private static List<String> join(String first, List<String> next) {
        List<String> joined = new ArrayList<>(next.size() + 1);
        joined.add(first);
        joined.addAll(next);
        return joined;
    }

    private static void startNativeDebug(File nativeImageFile, List<String> args, String miDebugger, DebugAdapterContext context, ExecutionDescriptor executionDescriptor, CompletableFuture<Void> launchFuture) {
        AtomicReference<NbDebugSession> debugSessionRef = new AtomicReference<>();
        Runnable start = () -> {
            NIDebugger niDebugger;
            try {
                niDebugger = NIDebugRunner.start(nativeImageFile, args, miDebugger, null, null, executionDescriptor, engine -> {
                    Session session = engine.lookupFirst(null, Session.class);
                    NbDebugSession debugSession = new NbDebugSession(session);
                    debugSessionRef.set(debugSession);
                    context.setDebugSession(debugSession);
                    launchFuture.complete(null);
                    context.getConfigurationSemaphore().waitForConfigurationDone();
                });
            } catch (IllegalStateException ex) {
                ErrorUtilities.completeExceptionally(launchFuture,
                    "Failed to launch debuggee native image. " + ex.getLocalizedMessage(),
                    ResponseErrorCode.serverErrorStart);
                return ;
            }
            NbDebugSession debugSession = debugSessionRef.get();
            debugSession.setNIDebugger(niDebugger);
        };
        // Start debugger in the session's lookup. It may override dialog displayer, etc.
        Lookups.executeWith(context.getLspSession().getLookup(), start);
    }

    @NonNull
    private List<String> argsToStringList(Object o) {
        if (o == null) {
            return Collections.emptyList();
        }
        if (o instanceof List) {
            for (Object item : (List)o) {
                if (!(o instanceof String)) {
                    throw new IllegalArgumentException("Only string parameters expected");
                }
            }
            return (List<String>)o;
        } else if (o instanceof String) {
            List<String> res = new ArrayList<>();
            return Arrays.asList(BaseUtilities.parseParameters(o.toString()));
        } else {
            throw new IllegalArgumentException("Expected String or String list");
        }
    }

    private static CompletableFuture<Pair<ActionProvider, String>> findTargetWithPossibleRebuild(FileObject toRun, SingleMethod singleMethod, boolean debug, boolean testRun, NbProcessConsole ioContext) throws IllegalArgumentException {
        Pair<ActionProvider, String> providerAndCommand = findTarget(toRun, singleMethod, debug, testRun);
        if (providerAndCommand != null) {
            return CompletableFuture.completedFuture(providerAndCommand);
        }
        CompletableFuture<Pair<ActionProvider,String>> afterBuild = new CompletableFuture<>();
        class CheckBuildProgress extends ActionProgress {
            boolean running;

            @Override
            protected void started() {
                running = true;
            }

            @Override
            public void finished(boolean success) {
                if (success) {
                    Pair<ActionProvider, String> providerAndCommand = findTarget(toRun, singleMethod, debug, testRun);
                    if (providerAndCommand != null) {
                        afterBuild.complete(providerAndCommand);
                        return;
                    }
                }
                afterBuild.completeExceptionally(new ResponseErrorException(new ResponseError(
                        ResponseErrorCode.MethodNotFound,
                        "Cannot find " + (debug ? "debug" : "run") + " action!", null)));
            }
        };
        CheckBuildProgress progress = new CheckBuildProgress();
        Lookup launchCtx = new ProxyLookup(
            Lookups.fixed(
                toRun, ioContext, progress
            ), Lookup.getDefault()
        );

        Collection<ActionProvider> providers = findActionProviders(toRun);
        for (ActionProvider ap : providers) {
            if (ap.isActionEnabled(ActionProvider.COMMAND_BUILD, launchCtx)) {
                Lookups.executeWith(launchCtx, () -> {
                    ap.invokeAction(ActionProvider.COMMAND_BUILD, launchCtx);
                });
                break;
            }
        }
        if (!progress.running) {
            progress.finished(true);
        }
        return afterBuild;
    }

    protected static @CheckForNull Pair<ActionProvider, String> findTarget(FileObject toRun, SingleMethod singleMethod, boolean debug, boolean testRun) {
        ClassPath sourceCP = ClassPath.getClassPath(toRun, ClassPath.SOURCE);
        FileObject fileRoot = sourceCP != null ? sourceCP.findOwnerRoot(toRun) : null;
        boolean mainSource;
        if (fileRoot != null) {
            mainSource = UnitTestForSourceQuery.findUnitTests(fileRoot).length > 0;
        } else {
            mainSource = !testRun;
        }
        ActionProvider provider = null;
        String command = null;
        Collection<ActionProvider> actionProviders = findActionProviders(toRun);
        Lookup testLookup = Lookups.singleton(toRun);
        String[] actions;
        if (!mainSource && singleMethod != null) {
            actions = debug ? new String[] {SingleMethod.COMMAND_DEBUG_SINGLE_METHOD}
                            : new String[] {SingleMethod.COMMAND_RUN_SINGLE_METHOD};
        } else {
            actions = debug ? mainSource ? new String[] {ActionProvider.COMMAND_DEBUG_SINGLE}
                                         : new String[] {ActionProvider.COMMAND_DEBUG_TEST_SINGLE, ActionProvider.COMMAND_DEBUG_SINGLE}
                            : mainSource ? new String[] {ActionProvider.COMMAND_RUN_SINGLE}
                                         : new String[] {ActionProvider.COMMAND_TEST_SINGLE, ActionProvider.COMMAND_RUN_SINGLE};
        }

        for (String commandCandidate : actions) {
            provider = findActionProvider(commandCandidate, actionProviders, testLookup);
            if (provider != null) {
                command = commandCandidate;
                break;
            }
        }

        if (provider == null) {
            command = debug ? mainSource ? ActionProvider.COMMAND_DEBUG
                                         : ActionProvider.COMMAND_DEBUG // DEBUG_TEST is missing?
                            : mainSource ? ActionProvider.COMMAND_RUN
                                         : ActionProvider.COMMAND_TEST;
            provider = findActionProvider(command, actionProviders, testLookup);
            if (!mainSource) {
                final Collection<ActionProvider> nestedAPs = findNestedActionProviders(toRun, command, testLookup);
                if (!nestedAPs.isEmpty()) {
                    final String finalCommand = command;
                    final ActionProvider finalProvider = provider;
                    provider = new ActionProvider() {
                        @Override
                        public String[] getSupportedActions() {
                            return new String[] {finalCommand};
                        }

                        @Override
                        public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
                            return finalCommand.equals(command);
                        }

                        @Override
                        public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
                            if (finalCommand.equals(command)) {
                                if (finalProvider != null) {
                                    finalProvider.invokeAction(command, context);
                                }
                                for (ActionProvider nestedAP : nestedAPs) {
                                    nestedAP.invokeAction(command, context);
                                }
                            }
                        }
                    };
                }
            }
        }
        if (provider == null) {
            return null;
        }
        return Pair.of(provider, command);
    }

    private static Collection<ActionProvider> findActionProviders(FileObject toRun) {
        Collection<ActionProvider> actionProviders = new ArrayList<>();
        Project prj = FileOwnerQuery.getOwner(toRun);
        if (prj != null) {
            ActionProvider ap = prj.getLookup().lookup(ActionProvider.class);
            actionProviders.add(ap);
        }
        actionProviders.addAll(Lookup.getDefault().lookupAll(ActionProvider.class));
        return actionProviders;
    }

    private static boolean supportsAction(ActionProvider ap, String action) {
        for (String supportedAction : ap.getSupportedActions()) {
            if (supportedAction.equals(action)) {
                return true;
            }
        }
        return false;
    }

    private static ActionProvider findActionProvider(String action, Collection<ActionProvider> actionProviders, Lookup enabledOnLookup) {
        for (ActionProvider ap : actionProviders) {
            if (supportsAction(ap, action) && ap.isActionEnabled(action, enabledOnLookup)) {
                return ap;
            }
        }
        return null;
    }

    private static Collection<ActionProvider> findNestedActionProviders(FileObject toRun, String action, Lookup enabledOnLookup) {
        Collection<ActionProvider> actionProviders = new ArrayList<>();
        Project prj = FileOwnerQuery.getOwner(toRun);
        if (prj != null) {
            for (Project containedPrj : ProjectUtils.getContainedProjects(prj, true)) {
                ActionProvider ap = containedPrj.getLookup().lookup(ActionProvider.class);
                if (supportsAction(ap, action) && ap.isActionEnabled(action, enabledOnLookup)) {
                    actionProviders.add(ap);
                }
            }
        }
        return actionProviders;
    }
}
