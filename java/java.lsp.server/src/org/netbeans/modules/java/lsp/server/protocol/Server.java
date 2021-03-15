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
package org.netbeans.modules.java.lsp.server.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionOptions;
import org.eclipse.lsp4j.CodeLensOptions;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.FoldingRangeProviderOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.RenameOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.WorkDoneProgressCancelParams;
import org.eclipse.lsp4j.WorkDoneProgressParams;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.jsonrpc.JsonRpcException;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.MessageIssueException;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.NotificationMessage;
import org.eclipse.lsp4j.jsonrpc.messages.RequestMessage;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.java.lsp.server.LspServerState;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.progress.OperationContext;
import org.netbeans.modules.progress.spi.InternalHandle;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author lahvac
 */
public final class Server {
    private static final Logger LOG = Logger.getLogger(Server.class.getName());
    
    private Server() {
    }
    
    public static NbCodeLanguageClient getStubClient() {
        return STUB_CLIENT;
    }
    
    public static boolean isClientResponseThread(NbCodeLanguageClient client) {
        return client != null ? 
                DISPATCHERS.get() == client :
                DISPATCHERS.get() != null;
    }
    
    public static void launchServer(InputStream in, OutputStream out) {
        LanguageServerImpl server = new LanguageServerImpl();
        ConsumeWithLookup msgProcessor = new ConsumeWithLookup(server.getSessionLookup());
        Launcher<NbCodeLanguageClient> serverLauncher = createLauncher(server, in, out, msgProcessor::attachLookup);
        NbCodeLanguageClient remote = serverLauncher.getRemoteProxy();
        ((LanguageClientAware) server).connect(remote);
        msgProcessor.attachClient(server.client);
        Future<Void> runningServer = serverLauncher.startListening();
        try {
            runningServer.get();
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private static Launcher<NbCodeLanguageClient> createLauncher(LanguageServerImpl server, InputStream in, OutputStream out,
            Function<MessageConsumer, MessageConsumer> processor) {
        return new LSPLauncher.Builder<NbCodeLanguageClient>()
            .setLocalService(server)
            .setRemoteInterface(NbCodeLanguageClient.class)
            .setInput(in)
            .setOutput(out)
            .wrapMessages(processor)
//                .traceMessages(new java.io.PrintWriter(System.err))
            .create();
    }
    
    static final ThreadLocal<NbCodeLanguageClient>   DISPATCHERS = new ThreadLocal<>();
    
    /**
     * Processes message while the default Lookup is set to 
     * {@link LanguageServerImpl#getSessionLookup()}.
     */
    private static class ConsumeWithLookup {
        private final Lookup sessionLookup;
        private NbCodeLanguageClient client;
        private OperationContext initialContext;
        
        public ConsumeWithLookup(Lookup sessionLookup) {
            this.sessionLookup = sessionLookup;
        }
        
        synchronized void attachClient(NbCodeLanguageClient client) {
            this.client = client;
        }
        
        public MessageConsumer attachLookup(MessageConsumer delegate) {
            // PENDING: allow for message consumer wrappers to be registered to add pre/post processing for
            // the request plus build the request's default Lookup contents.
            return new MessageConsumer() {
                @Override
                public void consume(Message msg) throws MessageIssueException, JsonRpcException {
                    InstanceContent ic = new InstanceContent();
                    ProxyLookup ll = new ProxyLookup(new AbstractLookup(ic), sessionLookup);
                    final OperationContext ctx;
                    
                    // Intercept client REQUESTS; take the progress token from them, if it is
                    // attached.
                    Runnable r;
                    InternalHandle toCancel = null;
                    if (msg instanceof RequestMessage) {
                        RequestMessage rq = (RequestMessage)msg;
                        Object p = rq.getParams();
                        if (initialContext == null) {
                            initialContext = OperationContext.create(client);
                            ctx = initialContext;
                        } else {
                            ctx = initialContext.operationContext();
                        }
                        // PENDING: this ought to be somehow registered, so different services
                        // may enrich lookup/pre/postprocess the processing, not just the progress support.
                        if (p instanceof WorkDoneProgressParams) {
                            ctx.setProgressToken(((WorkDoneProgressParams)p).getWorkDoneToken());
                        }
                    } else if (msg instanceof NotificationMessage) {
                        NotificationMessage not = (NotificationMessage)msg;
                        Object p = not.getParams();
                        OperationContext selected = null;
                        if (p instanceof WorkDoneProgressCancelParams && initialContext != null) {
                            WorkDoneProgressCancelParams wdc = (WorkDoneProgressCancelParams)p;
                            toCancel = initialContext.findActiveHandle(wdc.getToken());
                            selected = OperationContext.getHandleContext(toCancel);
                        }
                        ctx = selected;
                    } else {
                        ctx = null;
                    }
                    if (ctx != null) {
                        ic.add(ctx);
                    }
                    final InternalHandle ftoCancel = toCancel;
                    try {
                        DISPATCHERS.set(client);
                        Lookups.executeWith(ll, () -> {
                            try {
                                delegate.consume(msg);
                            } finally {
                                // cancel while the OperationContext is still active.
                                if (ftoCancel != null) {
                                    ftoCancel.requestCancel();
                                }
                                if (ctx != null) {
                                    // if initialized (for requests only), discards the token,
                                    // as it becomes invalid at the end of this message. Further progresses
                                    // must do their own processing.
                                    ctx.stop();
                                }
                            }
                        });
                    } finally {
                        DISPATCHERS.remove();
                    }
                }
            };
        }
    }
    
    // change to a greater throughput if the initialization waits on more processes than just (serialized) project open.
    private static final RequestProcessor SERVER_INIT_RP = new RequestProcessor(LanguageServerImpl.class.getName());
    
    
    static class LanguageServerImpl implements LanguageServer, LanguageClientAware, LspServerState {

        private static final Logger LOG = Logger.getLogger(LanguageServerImpl.class.getName());
        private NbCodeClientWrapper client;
        private final TextDocumentService textDocumentService = new TextDocumentServiceImpl(this);
        private final WorkspaceService workspaceService = new WorkspaceServiceImpl(this);
        private final InstanceContent   sessionServices = new InstanceContent();
        private final Lookup sessionLookup = new ProxyLookup(
                new AbstractLookup(sessionServices),
                Lookup.getDefault()
        );
        private final CompletableFuture<Project[]> workspaceProjects = new CompletableFuture<>();
        
        /**
         * Projects that are being opened and primed right now.
         */
        private final Map<Project, CompletableFuture<Void>> beingOpened = new HashMap<>();
        private final CompletableFuture<Project[]> initialOpenedProjects = new CompletableFuture<>();
        
        Lookup getSessionLookup() {
            return sessionLookup;
        }
        
        /**
         * Open projects that own the `projectCandidates` files asynchronously.
         * Returns immediately, results or errors are reported through the Future.
         * 
         * @param projectCandidates files whose projects should be opened.
         * @return future that yields the opened project instances.
         */
        @Override
        public CompletableFuture<Project[]> asyncOpenSelectedProjects(List<FileObject> projectCandidates) {
            System.err.println("Called asyncOpenProjects for " + projectCandidates);
            CompletableFuture<Project[]> f = new CompletableFuture<>();
            SERVER_INIT_RP.post(() -> {
                asyncOpenSelectedProjects0(f, projectCandidates);
            });
            return f;
        }
        
        /**
         * For diagnostic purposes
         */
        private AtomicInteger openRequestId = new AtomicInteger(1);

        private void asyncOpenSelectedProjects0(CompletableFuture<Project[]> f, List<FileObject> projectCandidates) {
            List<Project> projects = new ArrayList<>();
            try {
                for (FileObject candidate : projectCandidates) {
                    Project prj = FileOwnerQuery.getOwner(candidate);
                    if (prj != null) {
                        projects.add(prj);
                    }
                }
                Project[] previouslyOpened;
                try {
                    previouslyOpened = OpenProjects.getDefault().openProjects().get();
                    if (previouslyOpened.length > 0) {
                        Level level = Level.FINEST;
                        assert (level = Level.CONFIG) != null;
                        for (Project p : previouslyOpened) {
                            LOG.log(level, "Previously opened project at {0}", p.getProjectDirectory());
                        }
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    throw new IllegalStateException(ex);
                
                }
                asyncOpenSelectedProjects1(f, previouslyOpened, projects);
            } catch (RuntimeException ex) {
                f.completeExceptionally(ex);
            }
        }
        
        private void asyncOpenSelectedProjects1(CompletableFuture<Project[]> f, Project[] previouslyOpened, List<Project> projects) {
            int id = this.openRequestId.getAndIncrement();
            
            List<CompletableFuture> primingBuilds = new ArrayList<>();
            List<Project> toOpen = new ArrayList<>();
            Map<Project, CompletableFuture<Void>> local = new HashMap<>();
            synchronized (this) {
                LOG.log(Level.FINER, "{0}: Asked to open project(s): {1}", new Object[]{ id, Arrays.asList(projects) });
                for (Project p : projects) { 
                    CompletableFuture<Void> pending = beingOpened.get(p);
                    if (pending != null) {
                        primingBuilds.add(pending);
                    } else {
                        toOpen.add(p);
                        local.put(p, new CompletableFuture<Void>());
                    }
                }
                beingOpened.putAll(local);
            }
            
            LOG.log(Level.FINER, id + ": Opening projects: {0}", Arrays.asList(toOpen));

            // before the projects are officialy 'opened', try to prime the projects
            for (Project p : toOpen) {
                ActionProvider pap = p.getLookup().lookup(ActionProvider.class);
                if (pap == null) {
                    LOG.log(Level.FINER, "{0}: No action provider at all !", id);
                    continue;
                }
                if (!Arrays.asList(pap.getSupportedActions()).contains(ActionProvider.COMMAND_PRIME)) {
                    LOG.log(Level.FINER, "{0}: No action provider gives PRIME", id);
                    // this may take some while; so better call outside of any locks.
                    continue;
                }
                LOG.log(Level.FINER, "{0}: Found Priming action: {1}", new Object[]{id, p});
                if (pap.isActionEnabled(ActionProvider.COMMAND_PRIME, Lookup.EMPTY)) {
                    final CompletableFuture<Void> primeF = local.get(p);
                    LOG.log(Level.FINER, "{0}: Found enabled Priming build for: {1}", new Object[]{id, p});
                    ActionProgress progress = new ActionProgress() {
                        @Override
                        protected void started() {}

                        @Override
                        public void finished(boolean success) {
                            LOG.log(Level.FINER, id + ": Priming build completed for project " + p);
                            primeF.complete(null);
                        }
                    };
                    primingBuilds.add(primeF);

                    pap.invokeAction(ActionProvider.COMMAND_PRIME, Lookups.fixed(progress));
                }
            }
            
            // Wait for all priming builds, even those already pending, to finish:
            CompletableFuture.allOf(primingBuilds.toArray(new CompletableFuture[primingBuilds.size()])).thenRun(() -> {
                OpenProjects.getDefault().open(projects.toArray(new Project[0]), false);
                try {
                    LOG.log(Level.FINER, "{0}: Calling openProjects() for : {1}", new Object[]{id, Arrays.asList(projects)});
                    OpenProjects.getDefault().openProjects().get();
                } catch (InterruptedException | ExecutionException ex) {
                    throw new IllegalStateException(ex);
                }
                for (Project prj : projects) {
                    //init source groups/FileOwnerQuery:
                    ProjectUtils.getSources(prj).getSourceGroups(Sources.TYPE_GENERIC);
                }
                Project[] prjs = projects.toArray(new Project[projects.size()]);
                synchronized (this) {
                    LOG.log(Level.FINER, "{0}: Finished opening projects: {1}", new Object[]{id, Arrays.asList(projects)});
                    beingOpened.keySet().removeAll(toOpen);
                }
                f.complete(prjs);
            }).exceptionally(e -> {
                f.completeExceptionally(e);
                return null;
            });
        }
        
        private JavaSource checkJavaSupport() {
            final ClasspathInfo info = ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, ClassPath.EMPTY);
            final JavaSource source = JavaSource.create(info);
            if (source == null) {
                SERVER_INIT_RP.post(() -> {
                    final String msg = NO_JAVA_SUPPORT + System.getProperty("java.version");
                    showStatusBarMessage(MessageType.Error, msg, 5000);
                });
            }
            return source;
        }
        
        @Override
        public CompletableFuture<Project[]> openedProjects() {
            return initialOpenedProjects;
        }
        
        private JavaSource showIndexingCompleted(Project[] opened) {
            try {
                final JavaSource source = checkJavaSupport();
                if (source != null) {
                    source.runWhenScanFinished(cc -> {
                        showStatusBarMessage(MessageType.Info, INDEXING_COMPLETED, 0);
                    }, true);
                }
                return source;
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

        private void showStatusBarMessage(final MessageType type, final String msg, int timeout) {
            if (client.getNbCodeCapabilities().hasStatusBarMessageSupport()) {
                client.showStatusBarMessage(new ShowStatusMessageParams(type, msg, timeout));
            } else {
                client.showMessage(new ShowStatusMessageParams(type, msg, timeout));
            }
        }
        
        private InitializeResult constructInitResponse(JavaSource src) {
            ServerCapabilities capabilities = new ServerCapabilities();
            if (src != null) {
                capabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental);
                CompletionOptions completionOptions = new CompletionOptions();
                completionOptions.setResolveProvider(true);
                completionOptions.setTriggerCharacters(Collections.singletonList("."));
                capabilities.setCompletionProvider(completionOptions);
                capabilities.setHoverProvider(true);
                capabilities.setCodeActionProvider(new CodeActionOptions(Arrays.asList(CodeActionKind.QuickFix, CodeActionKind.Source)));
                capabilities.setDocumentSymbolProvider(true);
                capabilities.setDefinitionProvider(true);
                capabilities.setImplementationProvider(true);
                capabilities.setDocumentHighlightProvider(true);
                capabilities.setReferencesProvider(true);
                List<String> commands = new ArrayList<>(Arrays.asList(
                        JAVA_BUILD_WORKSPACE, JAVA_LOAD_WORKSPACE_TESTS, GRAALVM_PAUSE_SCRIPT, JAVA_SUPER_IMPLEMENTATION));
                for (CodeGenerator codeGenerator : Lookup.getDefault().lookupAll(CodeGenerator.class)) {
                    commands.addAll(codeGenerator.getCommands());
                }
                capabilities.setExecuteCommandProvider(new ExecuteCommandOptions(commands));
                capabilities.setWorkspaceSymbolProvider(true);
                capabilities.setCodeLensProvider(new CodeLensOptions(false));
                RenameOptions renOpt = new RenameOptions();
                renOpt.setPrepareProvider(true);
                capabilities.setRenameProvider(renOpt);
                FoldingRangeProviderOptions foldingOptions = new FoldingRangeProviderOptions();
                capabilities.setFoldingRangeProvider(foldingOptions);
            }
            return new InitializeResult(capabilities);
        }
        
        @Override
        public CompletableFuture<InitializeResult> initialize(InitializeParams init) {
            NbCodeClientCapabilities capa = NbCodeClientCapabilities.get(init);
            client.setClientCaps(capa);
            List<FileObject> projectCandidates = new ArrayList<>();
            List<WorkspaceFolder> folders = init.getWorkspaceFolders();
            if (folders != null) {
                for (WorkspaceFolder w : folders) {
                    try {
                        projectCandidates.add(Utils.fromUri(w.getUri()));
                    } catch (MalformedURLException ex) {
                        LOG.log(Level.FINE, null, ex);
                    }
                }
            } else {
                String root = init.getRootUri();

                if (root != null) {
                    try {
                        projectCandidates.add(Utils.fromUri(root));
                    } catch (MalformedURLException ex) {
                        LOG.log(Level.FINE, null, ex);
                    }
                } else {
                    //TODO: use getRootPath()?
                }
            }
            SERVER_INIT_RP.post(() -> asyncOpenSelectedProjects0(initialOpenedProjects, projectCandidates));
            
            // chain showIndexingComplete message after initial project open.
            initialOpenedProjects.
                    thenApply(this::showIndexingCompleted);
            
            // but complete the InitializationRequest independently of the project initialization.
            return CompletableFuture.completedFuture(
                    finishInitialization(
                        constructInitResponse(checkJavaSupport())
                    )
            );
        }

        public CompletableFuture<Project[]> getWorkspaceProjects() {
            return workspaceProjects;
        }

        public InitializeResult finishInitialization(InitializeResult res) {
            OperationContext c = OperationContext.find(sessionLookup);
            // discard the progress token as it is going to be invalid anyway. Further pending
            // initializations need to create its own tokens.
            c.acquireProgressToken();
            return res;
        }
        
        @Override
        public CompletableFuture<Object> shutdown() {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void exit() {
        }

        @Override
        public TextDocumentService getTextDocumentService() {
            return textDocumentService;
        }

        @Override
        public WorkspaceService getWorkspaceService() {
            return workspaceService;
        }

        @Override
        public void cancelProgress(WorkDoneProgressCancelParams params) {
            // handled in the interceptor, after the complete RPC call completes.
        }
        
        @Override
        public void connect(LanguageClient aClient) {
            this.client = new NbCodeClientWrapper((NbCodeLanguageClient)aClient);
            sessionServices.add(this);
            sessionServices.add(client);
            sessionServices.add(new WorkspaceIOContext() {
                @Override
                protected LanguageClient client() {
                    return client;
                }
            });
            sessionServices.add(new WorkspaceUIContext(client));
            ((LanguageClientAware) getTextDocumentService()).connect(aClient);
            ((LanguageClientAware) getWorkspaceService()).connect(aClient);
        }
    }
    
    public static final String JAVA_BUILD_WORKSPACE =  "java.build.workspace";
    public static final String JAVA_LOAD_WORKSPACE_TESTS =  "java.load.workspace.tests";
    public static final String JAVA_SUPER_IMPLEMENTATION =  "java.super.implementation";
    public static final String GRAALVM_PAUSE_SCRIPT =  "graalvm.pause.script";
    static final String INDEXING_COMPLETED = "Indexing completed.";
    static final String NO_JAVA_SUPPORT = "Cannot initialize Java support on JDK ";

    static final NbCodeLanguageClient STUB_CLIENT = new NbCodeLanguageClient() {
        private final NbCodeClientCapabilities caps = new NbCodeClientCapabilities();
        
        private void logWarning(Object... args) {
            LOG.log(Level.WARNING, "LSP Client called without proper context with param(s): {0}", 
                    Arrays.asList(args));
        }
        
        @Override
        public void showStatusBarMessage(ShowStatusMessageParams params) {
            logWarning(params);
        }

        @Override
        public CompletableFuture<List<QuickPickItem>> showQuickPick(ShowQuickPickParams params) {
            logWarning(params);
            return CompletableFuture.completedFuture(params.getCanPickMany() || params.getItems().isEmpty() ? params.getItems() : Collections.singletonList(params.getItems().get(0)));
        }

        @Override
        public CompletableFuture<String> showInputBox(ShowInputBoxParams params) {
            logWarning(params);
            return CompletableFuture.completedFuture(params.getValue());
        }

        @Override
        public void notifyTestProgress(TestProgressParams params) {
            logWarning(params);
        }

        @Override
        public NbCodeClientCapabilities getNbCodeCapabilities() {
            logWarning();
            return caps;
        }

        @Override
        public void telemetryEvent(Object object) {
            logWarning(object);
        }

        @Override
        public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
            logWarning(diagnostics);
        }

        @Override
        public void showMessage(MessageParams messageParams) {
            logWarning(messageParams);
        }

        @Override
        public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
            logWarning(requestParams);
            CompletableFuture<MessageActionItem> x = new CompletableFuture<>();
            x.complete(null);
            return x;
        }

        @Override
        public void logMessage(MessageParams message) {
            logWarning(message);
        }
    };
}
