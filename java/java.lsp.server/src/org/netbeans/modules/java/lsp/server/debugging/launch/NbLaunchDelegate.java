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
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.debug.TerminatedEventArguments;

import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.eclipse.lsp4j.services.LanguageClient;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.DebuggerManagerListener;
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
import org.netbeans.modules.java.lsp.server.debugging.ni.NILocationVisualizer;
import org.netbeans.modules.java.lsp.server.debugging.utils.ErrorUtilities;
import org.netbeans.modules.java.lsp.server.progress.OperationContext;
import org.netbeans.modules.java.lsp.server.progress.ProgressOperationEvent;
import org.netbeans.modules.java.lsp.server.progress.ProgressOperationListener;
import org.netbeans.modules.java.lsp.server.progress.TestProgressHandler;
import org.netbeans.modules.java.nativeimage.debugger.api.NIDebugRunner;
import org.netbeans.modules.nativeimage.api.debug.NIDebugger;
import org.netbeans.modules.nativeimage.api.debug.StartDebugParameters;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.api.project.ContainedProjectFilter;
import org.netbeans.spi.project.NestedClass;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.SingleMethod;

import org.openide.filesystems.FileObject;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author martin
 */
@NbBundle.Messages({
    "ERR_UnsupportedLaunchDebug=Debugging is not supported in this project.",
    "ERR_UnsupportedLaunch=Running  is not supported in this project.",
    "# {0} - the selected configuration",
    "# {1} - the suggested configuration",
    "ERR_UnsupportedLaunchDebugConfig=Debugging is not supported in configuration \"{0}\", please switch to {1}",
    "# {0} - the selected configuration",
    "# {1} - the suggested configuration",
    "ERR_UnsupportedLaunchConfig=Running is not supported in configuration \"{0}\", please switch to {1}.",
    "ERR_LaunchDefaultConfiguration=the default one.",
    "# {0} - the recommended configuration",
    "ERR_LaunchSupportiveConfigName=\"{0}\"",
    "CTL_NativeImageDebugger=Native Image Debugger"
})
public abstract class NbLaunchDelegate {

    private final RequestProcessor requestProcessor = new RequestProcessor(NbLaunchDelegate.class);
    private final Map<DebugAdapterContext, DebuggerManagerListener> debuggerListeners = new ConcurrentHashMap<>();

    public abstract void preLaunch(Map<String, Object> launchArguments, DebugAdapterContext context);

    public abstract void postLaunch(Map<String, Object> launchArguments, DebugAdapterContext context);

    protected void notifyFinished(DebugAdapterContext ctx, boolean success) {
        // Remove a possibly staled debugger listener
        DebuggerManagerListener listener = debuggerListeners.remove(ctx);
        if (listener != null) {
            DebuggerManager.getDebuggerManager().removeDebuggerListener(listener);
        }
    }

    public final CompletableFuture<Void> nbLaunch(FileObject toRun, boolean preferProjActions, @NullAllowed File nativeImageFile,
                                                  @NullAllowed String method, @NullAllowed String nestedClassName, Map<String, Object> launchArguments, DebugAdapterContext context,
                                                  boolean debug, LaunchType launchType, Consumer<NbProcessConsole.ConsoleMessage> consoleMessages,
                                                  boolean testInParallel) {
        CompletableFuture<Void> launchFuture = new CompletableFuture<>();
        NbProcessConsole ioContext = new NbProcessConsole(consoleMessages);
        NestedClass nestedClass;
        if (nestedClassName != null) {
            int topLevelClassSeparatorIdx = nestedClassName.indexOf(".");
            String topLevelClassName = nestedClassName.substring(0, topLevelClassSeparatorIdx);
            String nestedName = nestedClassName.substring(topLevelClassSeparatorIdx + 1);
            nestedClass = new NestedClass(nestedName, topLevelClassName, toRun);
        } else {
            nestedClass = null;
        }
        SingleMethod singleMethod;
        if (method != null) {
            singleMethod = nestedClass != null ?
                    new SingleMethod(method, nestedClass)
                    : new SingleMethod(toRun, method);
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
        if (nativeImageFile == null) {
            Project prj = FileOwnerQuery.getOwner(toRun);
            ContainedProjectFilter projectFilter;
            if (testInParallel) {
                projectFilter = getProjectFilter(prj, launchArguments);
            } else {
                projectFilter = null;
            }
            
            class W extends Writer {
                @Override
                public void write(char[] cbuf, int off, int len) throws IOException {
                    write(String.copyValueOf(cbuf, off, len));
                }

                @Override
                public void write(String str) throws IOException {
                    ioContext.stdIn(str);
                }

                @Override
                public void flush() throws IOException {
                    // nop
                }

                @Override
                public void close() throws IOException {
                    // nop
                }
            }
            W writer = new W();
            CompletableFuture<Pair<ActionProvider, String>> commandFuture = findTargetWithPossibleRebuild(prj, preferProjActions, toRun, singleMethod, nestedClass, debug, launchType, ioContext, testInParallel, projectFilter);
            commandFuture.thenAccept((providerAndCommand) -> {
                ExplicitProcessParameters params = createExplicitProcessParameters(launchArguments);
                OperationContext ctx = OperationContext.find(Lookup.getDefault());
                ctx.addProgressOperationListener(null, new ProgressOperationListener() {
                    @Override
                    public void progressHandleCreated(ProgressOperationEvent e) {
                        context.setProcessExecutorHandle(e.getProgressHandle());
                    }
                });
                boolean singleFile = !(preferProjActions && prj != null);
                if (!singleFile) {
                    String command = providerAndCommand.second();
                    if (ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(command)) {
                        singleFile = true;
                    }
                }
                Object contextObject = (singleFile) ? toRun : prj;
                TestProgressHandler testProgressHandler = ctx.getClient().getNbCodeCapabilities().hasTestResultsSupport() ? new TestProgressHandler(ctx.getClient(), context.getClient(), Utils.toUri(toRun)) : null;
                Lookup launchCtx = new ProxyLookup(
                        testProgressHandler != null ? Lookups.fixed(contextObject, ioContext, progress, testProgressHandler) : Lookups.fixed(contextObject, ioContext, progress),
                        Lookup.getDefault()
                );

                ProjectConfiguration selectConfiguration = null;
                ProjectConfigurationProvider<ProjectConfiguration> pcp = null;

                Object o = launchArguments.get("launchConfiguration");
                if (o instanceof String) {
                    if (prj != null) {
                        pcp = prj.getLookup().lookup(ProjectConfigurationProvider.class);
                        if (pcp != null) {
                            String n = (String)o;
                            selectConfiguration = pcp.getConfigurations().stream().filter(c -> n.equals(c.getDisplayName())).findAny().orElse(null);
                        }
                    }
                }
                List<? super Object> runContext = new ArrayList<>();
                runContext.add(contextObject);
                runContext.add(params);
                runContext.add(ioContext);
                runContext.add(progress);

                if (selectConfiguration != null) {
                    runContext.add(selectConfiguration);
                }

                Lookup lookup = new ProxyLookup(
                    createTargetLookup(prj, singleMethod, nestedClass, toRun, projectFilter),
                    Lookups.fixed(runContext.toArray(new Object[runContext.size()]))
                );
                // the execution Lookup is fully populated now. If the Project supports Configurations,
                // check if the action is actually enabled in the prescribed configuration. If it is not,
                if (pcp != null) {
                    final ActionProvider ap = providerAndCommand.first();
                    final String cmd = providerAndCommand.second();
                    if (!ap.isActionEnabled(cmd, lookup)) {

                        // attempt to locate a different configuration that enables the action:
                        ProjectConfiguration supportive = null;
                        int confIndex = runContext.indexOf(selectConfiguration);
                        if (confIndex == -1) {
                            runContext.add(null);
                            confIndex = runContext.size() - 1;
                        }
                        boolean defConfig = true;
                        for (ProjectConfiguration c : pcp.getConfigurations()) {
                            runContext.set(confIndex, c);
                            Lookup tryConf = Lookups.fixed(runContext.toArray(new Object[0]));
                            if (ap.isActionEnabled(cmd, tryConf)) {
                                supportive = c;
                                break;
                            }
                            defConfig = false;
                        }
                        String msg;
                        String recommended = defConfig ? Bundle.ERR_LaunchDefaultConfiguration(): Bundle.ERR_LaunchSupportiveConfigName(supportive.getDisplayName());
                        if (debug) {
                            msg = supportive == null ?
                                 Bundle.ERR_UnsupportedLaunchDebug() : Bundle.ERR_UnsupportedLaunchDebugConfig(selectConfiguration.getDisplayName(),  recommended);
                        } else {
                            msg = supportive == null ?
                                 Bundle.ERR_UnsupportedLaunch() : Bundle.ERR_UnsupportedLaunchConfig(selectConfiguration.getDisplayName(), recommended);
                        }
                        LanguageClient client = context.getLspSession().getLookup().lookup(LanguageClient.class);
                        if (client != null) {
                            client.showMessage(new MessageParams(MessageType.Warning, msg));
                            // first complete the future
                            launchFuture.complete(null);
                            // and then fake debuggee termination.
                            context.getClient().terminated(new TerminatedEventArguments());
                        } else {
                            launchFuture.completeExceptionally(new CancellationException());
                        }
                        return;
                    }
                }

                context.setInputSinkProvider(() -> writer);
                if (debug) {
                    DebuggerManagerListener listener = new DebuggerManagerAdapter() {
                        @Override
                        public void sessionAdded(Session session) {
                            JPDADebugger debugger = session.lookupFirst(null, JPDADebugger.class);
                            if (debugger != null) {
                                DebuggerManager.getDebuggerManager().removeDebuggerListener(this);
                                debuggerListeners.remove(context);
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
                    };
                    DebuggerManager.getDebuggerManager().addDebuggerListener(listener);
                    debuggerListeners.put(context, listener);
                }
                Lookups.executeWith(launchCtx, () -> {
                    providerAndCommand.first().invokeAction(providerAndCommand.second(), lookup);

                });
                if (!debug) {
                    launchFuture.complete(null);
                }
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
            ExplicitProcessParameters params = createExplicitProcessParameters(launchArguments);
            Lookup launchCtx = new ProxyLookup(
                    Lookups.fixed(ioContext, progress),
                    Lookup.getDefault()
            );
            List<String> args = argsToStringList(launchArguments.get("args"));
            // Add session's lookup, it may override dialog displayer, etc.
            Lookup execLookup = new ProxyLookup(launchCtx, context.getLspSession().getLookup());
            if (debug) {
                requestProcessor.post(() -> {
                    ActionProgress debugProgress = ActionProgress.start(launchCtx);
                    ExecutionDescriptor ed = executionDescriptor.postExecution((@NullAllowed Integer exitCode) -> {
                        debugProgress.finished(exitCode != null && exitCode == 0);
                    });
                    Lookups.executeWith(execLookup, () -> {
                        String miDebugger = (String) launchArguments.get("miDebugger");
                        startNativeDebug(nativeImageFile, args, miDebugger, context, ed, Lookups.fixed(params), launchFuture, debugProgress);
                    });
                });
            } else {
                ExecutionDescriptor ed = executionDescriptor.postExecution((@NullAllowed Integer exitCode) -> {
                    ioContext.stop();
                    notifyFinished(context, exitCode != null && exitCode == 0);
                });
                Lookups.executeWith(execLookup, () -> {
                    execNative(nativeImageFile, args, context, ed, params, launchFuture);
                });
            }
        }
        return launchFuture;
    }

    private static ExplicitProcessParameters createExplicitProcessParameters(Map<String, Object> launchArguments) {
        List<String> args = argsToStringList(launchArguments.get("args"));
        List<String> vmArgs = argsToStringList(launchArguments.get("vmArgs"));

        String cwd = Objects.toString(launchArguments.get("cwd"), null);
        Object envObj = launchArguments.get("env");
        Map<String, String> env = envObj != null ? (Map<String, String>) envObj : Collections.emptyMap();
        ExplicitProcessParameters.Builder bld = ExplicitProcessParameters.builder();
        if (!vmArgs.isEmpty()) {
            bld.launcherArgs(vmArgs);
        }
        if (!args.isEmpty()) {
            bld.args(args);
        }
        bld.replaceArgs(false);
        if (cwd != null) {
            bld.workingDirectory(new File(cwd));
        }
        if (!env.isEmpty()) {
            bld.environmentVariables(env);
        }
        ExplicitProcessParameters params = bld.build();
        return params;
    }

    private static void execNative(File nativeImageFile, List<String> args,
                                   DebugAdapterContext context,
                                   ExecutionDescriptor executionDescriptor,
                                   ExplicitProcessParameters params,
                                   CompletableFuture<Void> launchFuture) {
        ExecutionService.newService(() -> {
            launchFuture.complete(null);
            List<String> command = join(nativeImageFile.getAbsolutePath(), args);
            try {
                ProcessBuilder pb = new ProcessBuilder(command);
                File workingDirectory = params.getWorkingDirectory();
                if (workingDirectory != null) {
                    pb.directory(workingDirectory);
                }
                if (!params.getEnvironmentVariables().isEmpty()) {
                    Map<String, String> environment = pb.environment();
                    for (Map.Entry<String, String> entry : params.getEnvironmentVariables().entrySet()) {
                        String env = entry.getKey();
                        String val = entry.getValue();
                        if (val != null) {
                            environment.put(env, val);
                        } else {
                            environment.remove(env);
                        }
                    }
                }
                return pb.start();
            } catch (IOException ex) {
                ErrorUtilities.completeExceptionally(launchFuture,
                    "Failed to run debuggee native image: " + ex.getLocalizedMessage(),
                    ResponseErrorCode.ServerNotInitialized);
                throw ex;
            }
        }, executionDescriptor, "Run - " + nativeImageFile.getName()).run();
    }

    private static List<String> join(String first, List<String> next) {
        if (next.isEmpty()) {
            return Collections.singletonList(first);
        }
        List<String> joined = new ArrayList<>(next.size() + 1);
        joined.add(first);
        joined.addAll(next);
        return joined;
    }

    private static void startNativeDebug(File nativeImageFile, List<String> args,
                                         String miDebugger, DebugAdapterContext context,
                                         ExecutionDescriptor executionDescriptor,
                                         Lookup contextLookup,
                                         CompletableFuture<Void> launchFuture,
                                         ActionProgress debugProgress) {
        AtomicReference<NbDebugSession> debugSessionRef = new AtomicReference<>();
        CompletableFuture<Void> finished = new CompletableFuture<>();
        List<String> command = join(nativeImageFile.getAbsolutePath(), args);
        StartDebugParameters.Builder parametersBuilder = StartDebugParameters.newBuilder(command)
                .debugger(miDebugger)
                .debuggerDisplayObjects(false)
                .displayName(Bundle.CTL_NativeImageDebugger())
                .executionDescriptor(executionDescriptor)
                .lookup(contextLookup);
        StartDebugParameters parameters = parametersBuilder.build();
        NIDebugger niDebugger;
        try {
            niDebugger = NIDebugRunner.start(nativeImageFile, parameters, null, engine -> {
                Session session = engine.lookupFirst(null, Session.class);
                NbDebugSession debugSession = new NbDebugSession(session);
                debugSessionRef.set(debugSession);
                context.setDebugSession(debugSession);
                launchFuture.complete(null);
                context.getConfigurationSemaphore().waitForConfigurationDone();
                session.addPropertyChangeListener(Session.PROP_CURRENT_LANGUAGE, evt -> {
                    if (evt.getNewValue() == null) {
                        // No current language => finished
                        finished.complete(null);
                    }
                });
            });
        } catch (IllegalStateException ex) {
            ErrorUtilities.completeExceptionally(launchFuture,
                "Failed to launch debuggee native image. " + ex.getLocalizedMessage(),
                ResponseErrorCode.ServerNotInitialized);
            debugProgress.finished(false);
            return ;
        }
        NbDebugSession debugSession = debugSessionRef.get();
        debugSession.setNIDebugger(niDebugger);
        NILocationVisualizer.handle(nativeImageFile, niDebugger, finished, context.getLspSession().getLspServer().getOpenedDocuments());
    }

    @NonNull
    static List<String> argsToStringList(Object o) {
        if (o == null) {
            return Collections.emptyList();
        }
        if (o instanceof List) {
            for (Object item : (List)o) {
                if (!(item instanceof String)) {
                    throw new IllegalArgumentException("Only string parameters expected");
                }
            }
            return (List<String>)o;
        } else if (o instanceof String) {
            return Arrays.asList(BaseUtilities.parseParameters(o.toString()));
        } else {
            throw new IllegalArgumentException("Expected String or String list");
        }
    }

    private static CompletableFuture<Pair<ActionProvider, String>> findTargetWithPossibleRebuild(Project proj, boolean preferProjActions, FileObject toRun, SingleMethod singleMethod, NestedClass nestedClass, boolean debug, LaunchType launchType, NbProcessConsole ioContext, boolean testInParallel, ContainedProjectFilter projectFilter) throws IllegalArgumentException {
        Pair<ActionProvider, String> providerAndCommand = findTarget(proj, preferProjActions, toRun, singleMethod, nestedClass, debug, launchType, testInParallel, projectFilter);
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
                    Pair<ActionProvider, String> providerAndCommand = findTarget(proj, preferProjActions, toRun, singleMethod, nestedClass, debug, launchType, testInParallel, projectFilter);
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

        Collection<ActionProvider> providers = findActionProviders(proj);
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

    protected static @CheckForNull Pair<ActionProvider, String> findTarget(Project prj, boolean preferProjActions, FileObject toRun, SingleMethod singleMethod, NestedClass nestedClass, boolean debug, LaunchType launchType, boolean testInParallel, ContainedProjectFilter projectFilter) {
        ClassPath sourceCP = ClassPath.getClassPath(toRun, ClassPath.SOURCE);
        boolean mainSource;
        if (launchType == LaunchType.RUN_MAIN) {
            mainSource = true;
        } else if (launchType == LaunchType.RUN_TEST) {
            mainSource = false;
        } else {
            FileObject fileRoot = sourceCP != null ? sourceCP.findOwnerRoot(toRun) : null;
            mainSource = fileRoot != null && UnitTestForSourceQuery.findUnitTests(fileRoot).length > 0;
        }
        ActionProvider provider = null;
        String command = null;
        Collection<ActionProvider> actionProviders = findActionProviders(prj);
        Lookup testLookup = createTargetLookup(preferProjActions ? prj : null, singleMethod, nestedClass, toRun, projectFilter);
        String[] actions;
       
        if (testInParallel) {
            actions = new String[] {ActionProvider.COMMAND_TEST_PARALLEL, ActionProvider.COMMAND_RUN};
        } else if (!mainSource && singleMethod != null) {
            actions = debug ? new String[] {SingleMethod.COMMAND_DEBUG_SINGLE_METHOD}
                                : new String[] {SingleMethod.COMMAND_RUN_SINGLE_METHOD};
        } else {
            if (preferProjActions && prj != null) {
                actions = debug ? mainSource ? new String[] {ActionProvider.COMMAND_DEBUG}
                                             : new String[] {ActionProvider.COMMAND_DEBUG_TEST_SINGLE, ActionProvider.COMMAND_DEBUG} //TODO: COMMAND_DEBUG_TEST is missing
                                : mainSource ? new String[] {ActionProvider.COMMAND_RUN}
                                             : new String[] {ActionProvider.COMMAND_TEST, ActionProvider.COMMAND_RUN};
                if (debug && !mainSource) {
                    // We are calling COMMAND_DEBUG_TEST_SINGLE instead of a missing COMMAND_DEBUG_TEST
                    // This is why we need to add the file to the lookup
                    testLookup = createTargetLookup(null, singleMethod, nestedClass, toRun, projectFilter);
                }
            } else {
                actions = debug ? mainSource ? new String[] {ActionProvider.COMMAND_DEBUG_SINGLE}
                                             : new String[] {ActionProvider.COMMAND_DEBUG_TEST_SINGLE, ActionProvider.COMMAND_DEBUG_SINGLE}
                                : mainSource ? new String[] {ActionProvider.COMMAND_RUN_SINGLE}
                                             : new String[] {ActionProvider.COMMAND_TEST_SINGLE, ActionProvider.COMMAND_RUN_SINGLE};
            }
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
                                         : ActionProvider.COMMAND_TEST //TODO: COMMAND_DEBUG_TEST is missing?
                            : mainSource ? ActionProvider.COMMAND_RUN
                                         : ActionProvider.COMMAND_TEST;
            provider = findActionProvider(command, actionProviders, testLookup);
            if (!mainSource) {
                final Collection<ActionProvider> nestedAPs = findNestedActionProviders(prj, command, testLookup);
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

    static Lookup createTargetLookup(Project prj, SingleMethod singleMethod, NestedClass nestedClass, FileObject toRun, ContainedProjectFilter projectFilter) {
        List<Lookup> arr = new ArrayList<>();
        if (prj != null) {
            arr.add(Lookups.singleton(prj));
        }
        if (singleMethod != null) {
            Lookup methodLookup = Lookups.singleton(singleMethod);
            arr.add(methodLookup);
        }
        if (nestedClass != null) {
            Lookup nestedClassLookup = Lookups.singleton(nestedClass);
            arr.add(nestedClassLookup);
        }
        if (projectFilter != null) {
            Lookup projectLookup = Lookups.singleton(projectFilter);
            arr.add(projectLookup);
        }
        if (toRun != null) {
            arr.add(toRun.getLookup());
        }
        return new ProxyLookup(arr.toArray(new Lookup[0]));
    }
    
    static ContainedProjectFilter getProjectFilter(Project prj, Map<String, Object> launchArguments) {
        List<String> projectsArg = argsToStringList(launchArguments.get("projects"));
        List<Project> projects = ProjectUtils.getContainedProjects(prj, false).stream()
            .filter(project -> projectsArg.contains(project.getProjectDirectory().getName()))
            .toList();
        return ContainedProjectFilter.of(projects).orElse(null);
    }

    static Collection<ActionProvider> findActionProviders(Project prj) {
        Collection<ActionProvider> actionProviders = new ArrayList<>();
        if (prj != null) {
            Collection<? extends ActionProvider> ap = prj.getLookup().lookupAll(ActionProvider.class);
            actionProviders.addAll(ap);
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

    private static Collection<ActionProvider> findNestedActionProviders(Project prj, String action, Lookup enabledOnLookup) {
        Collection<ActionProvider> actionProviders = new ArrayList<>();
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

    public enum LaunchType {
        AUTODETECT,
        RUN_MAIN,
        RUN_TEST;

        static LaunchType from(Map<String, Object> launchArguments) {
            Object testRunValue = launchArguments.get("testRun");

            if (testRunValue instanceof Boolean) {
                Boolean testRunSetting = (Boolean) testRunValue;
                return testRunSetting ? RUN_TEST : RUN_MAIN;
            } else {
                return AUTODETECT;
            }
        }
    }
}
