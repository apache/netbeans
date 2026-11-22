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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.prefs.Preferences;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.eclipse.lsp4j.CallHierarchyRegistrationOptions;
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
import org.eclipse.lsp4j.SaveOptions;
import org.eclipse.lsp4j.SemanticTokensCapabilities;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SetTraceParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.SignatureHelpOptions;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TextDocumentSyncOptions;
import org.eclipse.lsp4j.WorkDoneProgressCancelParams;
import org.eclipse.lsp4j.WorkDoneProgressParams;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.WorkspaceFoldersOptions;
import org.eclipse.lsp4j.WorkspaceServerCapabilities;
import org.eclipse.lsp4j.WorkspaceSymbolOptions;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.JsonRpcException;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.MessageIssueException;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.NotificationMessage;
import org.eclipse.lsp4j.jsonrpc.messages.RequestMessage;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.eclipse.lsp4j.jsonrpc.services.JsonDelegate;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import static org.netbeans.api.project.ProjectUtils.parentOf;
import org.netbeans.modules.java.lsp.server.LspGsonSetup;
import org.netbeans.modules.java.lsp.server.LspServerState;
import org.netbeans.modules.java.lsp.server.LspSession;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.explorer.LspTreeViewServiceImpl;
import org.netbeans.modules.java.lsp.server.explorer.api.NodeChangedParams;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeViewService;
import org.netbeans.modules.java.lsp.server.files.OpenedDocuments;
import org.netbeans.modules.java.lsp.server.input.InputService;
import org.netbeans.modules.java.lsp.server.input.LspInputServiceImpl;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
import org.netbeans.modules.java.lsp.server.input.ShowQuickPickParams;
import org.netbeans.modules.java.lsp.server.input.ShowMutliStepInputParams;
import org.netbeans.modules.java.lsp.server.input.ShowInputBoxParams;
import org.netbeans.modules.java.lsp.server.progress.OperationContext;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.progress.spi.InternalHandle;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.Pair;
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
    static final Logger LOG = Logger.getLogger(Server.class.getName());

    /**
     * Special logger that logs LSP in/out messages.
     */
    static final Logger LSP_LOG = Logger.getLogger("org.netbeans.modules.java.lsp.server.lsptrace"); // NOI18N
    static final LspServerTelemetryManager LSP_SERVER_TELEMETRY = new LspServerTelemetryManager();
    // change to a greater throughput if the initialization waits on more processes than just (serialized) project open.
    static final RequestProcessor SERVER_INIT_RP = new RequestProcessor(LanguageServerImpl.class.getName());

    private static final ErrorsNotifier ERR_NOTIFIER = new ErrorsNotifier();
    
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

    public static NbLspServer launchServer(Pair<InputStream, OutputStream> io, LspSession session) {
        LanguageServerImpl server = new LanguageServerImpl(session);
        ConsumeWithLookup msgProcessor = new ConsumeWithLookup(server.getSessionLookup());
        Launcher<NbCodeLanguageClient> serverLauncher = createLauncher(server, io, msgProcessor::attachLookup, msgProcessor::addService);
        NbCodeLanguageClient remote = serverLauncher.getRemoteProxy();
        ((LanguageClientAware) server).connect(remote);
        msgProcessor.attachClient(server.client);
        Future<Void> runningServer = serverLauncher.startListening();
        LSP_SERVER_TELEMETRY.connect(server.client, runningServer);
        ERR_NOTIFIER.connect(server, runningServer);
        return new NbLspServer(server, runningServer);
    }
    
    private static Launcher<NbCodeLanguageClient> createLauncher(LanguageServerImpl server, Pair<InputStream, OutputStream> io,
            Function<MessageConsumer, MessageConsumer> processor, Consumer<Object> addService) {
        return new LSPLauncher.Builder<NbCodeLanguageClient>() {
                @Override
                protected MessageJsonHandler createJsonHandler() {
                    MessageJsonHandler h = super.createJsonHandler(); 
                    if (addService != null) {
                        addService.accept(h.getGson());
                    }
                    return h;
                }
            }
            .setLocalService(server)
            .setRemoteInterface(NbCodeLanguageClient.class)
            .setInput(io.first())
            .setOutput(io.second())
            .wrapMessages(processor)
            .configureGson(gb -> {
                Lookup.getDefault().lookupAll(LspGsonSetup.class).forEach(s -> s.configureBuilder(gb));
                
                gb.registerTypeAdapter(SemanticTokensCapabilities.class, new InstanceCreator<SemanticTokensCapabilities>() {
                    @Override public SemanticTokensCapabilities createInstance(Type type) {
                        return new SemanticTokensCapabilities(null);
                    }
                });
                gb.registerTypeAdapter(SemanticTokensParams.class, new InstanceCreator<SemanticTokensParams>() {
                    @Override public SemanticTokensParams createInstance(Type type) {
                        return new SemanticTokensParams(new TextDocumentIdentifier(""));
                    }
                });
            })
            .setExceptionHandler((t) -> {
                LOG.log(Level.WARNING, "Error occurred during LSP message dispatch", t);
                if (t instanceof CompletionException) {
                    if (t.getCause() instanceof ResponseErrorException) {
                        return ((ResponseErrorException)t.getCause()).getResponseError();
                    }
                    Throwable cause = t.getCause();
                    ResponseError error = new ResponseError();
                    error.setMessage(cause.getMessage());
                    error.setCode(ResponseErrorCode.InternalError);
                    error.setData(cause);
                    
                    return error;
                }
                return RemoteEndpoint.DEFAULT_EXCEPTION_HANDLER.apply(t);
            })
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
        private List<Object> additionalServices = new ArrayList<>();

        public ConsumeWithLookup(Lookup sessionLookup) {
            this.sessionLookup = sessionLookup;
        }
        
        public void addService(Object o) {
            this.additionalServices.add(o);
        }

        synchronized void attachClient(NbCodeLanguageClient client) {
            this.client = client;
        }

        public MessageConsumer attachLookup(MessageConsumer delegate) {
            // PENDING: allow for message consumer wrappers to be registered to add pre/post processing for
            // the request plus build the request's default Lookup contents.
            if (!(delegate instanceof Endpoint)) {
                return new MessageConsumer() {
                    @Override
                    public void consume(Message msg) throws MessageIssueException, JsonRpcException {
                        if (LSP_LOG.isLoggable(Level.FINEST)) {
                            LSP_LOG.log(Level.FINEST, "OUT: {0}", 
                                    msg.toString().replace("\n", "\n\t"));
                        }
                        delegate.consume(msg);
                    }
                
                };
            }
            return new MessageConsumer() {
                @Override
                public void consume(Message msg) throws MessageIssueException, JsonRpcException {
                    InstanceContent ic = new InstanceContent();
                    ProxyLookup ll = new ProxyLookup(new AbstractLookup(ic), sessionLookup);
                    final OperationContext ctx;

                    if (LSP_LOG.isLoggable(Level.FINEST)) {
                        LSP_LOG.log(Level.FINEST, "IN : {0}", 
                                msg.toString().replace("\n", "\n\t"));
                    }
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
                    if (additionalServices != null) {
                        additionalServices.forEach(ic::add);
                    }
                    final InternalHandle ftoCancel = toCancel;
                    try {
                        DISPATCHERS.set(client);
                        Lookups.executeWith(ll, () -> {
                            try {
                                delegate.consume(msg);
                            } catch (RuntimeException | Error e) {
                                LOG.log(Level.WARNING, "Error occurred during message dispatch", e);
                                throw e;
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


    /**
     * Returns a sequence of parents of the given project, leading to the {@link #rootOf} that
     * project. If `{@code excludeSelf}` is true, the sequence does not contain the project itself.
     * Note that if the project has no parent, then {@code excludeSelf = true} may return an
     * empty sequence.
     * <p>
     * The sequence starts at the project (or its immediate parent, if excludeSelf is true), and
     * iterate towards the root of the project.
     *
     * @param project inspected project
     * @return path from the project to the root
     * @since
     */
    public static Iterable<Project> projectPath(@NonNull Project project, boolean excludeSelf) {
        return new Iterable<Project>() {
            @Override
            public Iterator<Project> iterator() {
                return new Iterator<Project>() {
                    Project next = excludeSelf ? project : parentOf(project);
                    @Override
                    public boolean hasNext() {
                        return next != null;
                    }

                    @Override
                    public Project next() {
                        if (next == null) {
                            throw new NoSuchElementException();
                        }
                        Project r = next;
                        next = parentOf(r);
                        return r;
                    }
                };
            }
        };
    }

    public static class LanguageServerImpl implements LanguageServer, LanguageClientAware, LspServerState, NbLanguageServer {

        private static final String NETBEANS_FORMAT = "format";
        private static final String NETBEANS_JAVA_IMPORTS = "java.imports";
        private static final String NETBEANS_MAVEN_USER_SETTINGS = "maven.userSettings";
        private static final String NETBEANS_PROJECT_JDKHOME = "project.jdkhome";
        private static final String NETBEANS_JAVA_HINTS = "hints";

        private NbCodeClientWrapper client;
        private final TextDocumentServiceImpl textDocumentService = new TextDocumentServiceImpl(this);
        private final WorkspaceServiceImpl workspaceService = new WorkspaceServiceImpl(this);
        private final InstanceContent   sessionServices = new InstanceContent();
        private final AbstractLookup sessionOnly = new AbstractLookup(sessionServices);
        private final Lookup sessionLookup = new ProxyLookup(
                sessionOnly,
                Lookup.getDefault()
        );

        private final LspTreeViewServiceImpl treeService = new LspTreeViewServiceImpl(sessionLookup);
        private final LspInputServiceImpl inputService = new LspInputServiceImpl();
        private final OpenedDocuments openedDocuments = new OpenedDocuments();
        
        private final LspSession lspSession;
        private boolean shutdownReqReceived = false;
        private final WorkspaceProjects workspaceProjects;
        
        LanguageServerImpl(LspSession session) {
            this.lspSession = session;
            this.workspaceProjects = new WorkspaceProjects(workspaceService, () -> client);
        }

        private Lookup getSessionLookup() {
            return lspSession.getLookup();
        }
        
        /**
         * Returns a Lookup specific for this LSP server's session. Does not include the default Lookup contents,
         * it is suitable for composing into a ProxyLookup with other parts + the default one.
         * @return 
         */
        Lookup getSessionOnlyLookup() {
            return sessionOnly;
        }

        /**
         * Open projects that own the `projectCandidates` files asynchronously.
         * Returns immediately, results or errors are reported through the Future.
         *
         * @param projectCandidates files whose projects should be opened.
         * @return future that yields the opened project instances.
         */
        @Override
        public CompletableFuture<Project[]> asyncOpenSelectedProjects(List<FileObject> projectCandidates, boolean addWorkspace) {
            return workspaceProjects.asyncOpenSelectedProjects(projectCandidates, addWorkspace);
        }

        @Override
        public CompletableFuture<Project> asyncOpenFileOwner(FileObject file) {
            return workspaceProjects.asyncOpenFileOwner(file);
        }

        public List<FileObject> getAcceptedWorkspaceFolders() {
            return workspaceProjects.getAcceptedWorkspaceFolders();
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
            return workspaceProjects.getWorkspaceProjects();
        }

        @Override
        public OpenedDocuments getOpenedDocuments() {
            return openedDocuments;
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

        private InitializeResult constructInitResponse(InitializeParams init, JavaSource src, NbCodeClientCapabilities capa) {
            ServerCapabilities capabilities = new ServerCapabilities();
            if (src != null) {
                TextDocumentSyncOptions textDocumentSyncOptions = new TextDocumentSyncOptions();
                textDocumentSyncOptions.setChange(TextDocumentSyncKind.Incremental);
                textDocumentSyncOptions.setOpenClose(true);
                textDocumentSyncOptions.setWillSaveWaitUntil(true);
                // TODO: we now do not request to send saved contents, but in case of client side applied text edits, it could be cool to 
                // receive the current document contents at a savepoint.
                textDocumentSyncOptions.setSave(new SaveOptions(false));
                capabilities.setTextDocumentSync(textDocumentSyncOptions);
                CompletionOptions completionOptions = new CompletionOptions();
                completionOptions.setResolveProvider(true);
                completionOptions.setTriggerCharacters(Arrays.asList(".", "#", "@", "*"));
                capabilities.setCompletionProvider(completionOptions);
                SignatureHelpOptions signatureHelpOptions = new SignatureHelpOptions();
                signatureHelpOptions.setTriggerCharacters(Arrays.asList("("));
                signatureHelpOptions.setRetriggerCharacters(Arrays.asList(","));
                capabilities.setSignatureHelpProvider(signatureHelpOptions);
                capabilities.setHoverProvider(true);
                CodeActionOptions codeActionOptions = new CodeActionOptions(Arrays.asList(CodeActionKind.QuickFix, CodeActionKind.Source, CodeActionKind.SourceOrganizeImports, CodeActionKind.Refactor));
                codeActionOptions.setResolveProvider(true);
                capabilities.setCodeActionProvider(codeActionOptions);
                capabilities.setDocumentSymbolProvider(true);
                capabilities.setDefinitionProvider(true);
                capabilities.setTypeDefinitionProvider(true);
                capabilities.setImplementationProvider(true);
                capabilities.setDocumentHighlightProvider(true);
                capabilities.setDocumentFormattingProvider(true);
                capabilities.setDocumentRangeFormattingProvider(true);
                capabilities.setReferencesProvider(true);
                capabilities.setInlineValueProvider(true);

                CallHierarchyRegistrationOptions chOpts = new CallHierarchyRegistrationOptions();
                chOpts.setWorkDoneProgress(true);
                capabilities.setCallHierarchyProvider(chOpts);
                Set<String> commands = new LinkedHashSet<>(Arrays.asList(NBLS_GRAALVM_PAUSE_SCRIPT,
                        NBLS_BUILD_WORKSPACE,
                        NBLS_CLEAN_WORKSPACE,
                        NBLS_GET_ARCHIVE_FILE_CONTENT,
                        NBLS_RUN_PROJECT_ACTION,
                        JAVA_FIND_DEBUG_ATTACH_CONFIGURATIONS,
                        JAVA_FIND_DEBUG_PROCESS_TO_ATTACH,
                        NBLS_FIND_PROJECT_CONFIGURATIONS,
                        JAVA_GET_PROJECT_CLASSPATH,
                        JAVA_GET_PROJECT_PACKAGES,
                        JAVA_GET_PROJECT_SOURCE_ROOTS,
                        NBLS_LOAD_WORKSPACE_TESTS,
                        NBLS_RESOLVE_STACKTRACE_LOCATION,
                        NBLS_NEW_FROM_TEMPLATE,
                        NBLS_NEW_PROJECT,
                        NBLS_PROJECT_CONFIGURATION_COMPLETION,
                        NBLS_PROJECT_RESOLVE_PROJECT_PROBLEMS,
                        JAVA_SUPER_IMPLEMENTATION,
                        NBLS_CLEAR_PROJECT_CACHES,
                        NATIVE_IMAGE_FIND_DEBUG_PROCESS_TO_ATTACH,
                        NBLS_PROJECT_INFO,
                        JAVA_ENABLE_PREVIEW,
                        NBLS_DOCUMENT_SYMBOLS,
                        NBLS_GET_DIAGNOSTICS,
                        NBLS_GET_SERVER_DIRECTORIES
                ));
                for (CodeActionsProvider codeActionsProvider : Lookup.getDefault().lookupAll(CodeActionsProvider.class)) {
                    commands.addAll(codeActionsProvider.getCommands());
                }
                Utils.ensureCommandsPrefixed(commands);
                commands = commands.stream().map(cmd -> Utils.encodeCommand(cmd, capa)).collect(Collectors.toSet());
                capabilities.setExecuteCommandProvider(new ExecuteCommandOptions(new ArrayList<>(commands)));
                WorkspaceSymbolOptions wsOpts = new WorkspaceSymbolOptions();
                wsOpts.setResolveProvider(true);
                capabilities.setWorkspaceSymbolProvider(wsOpts);
                capabilities.setCodeLensProvider(new CodeLensOptions(false));
                RenameOptions renOpt = new RenameOptions();
                renOpt.setPrepareProvider(true);
                capabilities.setRenameProvider(renOpt);
                FoldingRangeProviderOptions foldingOptions = new FoldingRangeProviderOptions();
                capabilities.setFoldingRangeProvider(foldingOptions);
                capabilities.setInlayHintProvider(true);
                textDocumentService.init(init.getCapabilities(), capabilities);

                // register for workspace changess
                WorkspaceServerCapabilities wcaps = new WorkspaceServerCapabilities();
                WorkspaceFoldersOptions wfopts = new WorkspaceFoldersOptions();
                wfopts.setSupported(true);
                wfopts.setChangeNotifications(true);
                wcaps.setWorkspaceFolders(wfopts);
                capabilities.setWorkspace(wcaps);
            }
            return new InitializeResult(capabilities);
        }

        @Override
        public CompletableFuture<InitializeResult> initialize(InitializeParams init) {
            NbCodeClientCapabilities capa = NbCodeClientCapabilities.get(init);
            client.setClientCaps(capa);
            workspaceService.registerConfigChangeListeners();
            hackConfigureGroovySupport(capa);
            hackNoReuseOfOutputsForAntProjects();
            List<FileObject> projectCandidates = new ArrayList<>();
            List<WorkspaceFolder> folders = init.getWorkspaceFolders();
            if (folders != null && !folders.isEmpty()) {
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
            CompletableFuture<Project[]> possibleWaitedPrjs = workspaceProjects.getWorkspaceProjects();
            // this Future will receive candidates, some of them possibly null. Cannot complete directly the existing `workspaceProjects` wit the returned candidates,
            // as this could return nulls to clients that do not expect any.
            CompletableFuture<Project[]> prjs = new CompletableFuture<Project[]>();
            SERVER_INIT_RP.post(() -> {
                List<FileObject> additionalCandidates = new ArrayList<>();
                AtomicBoolean cancel = new AtomicBoolean();
                ProgressHandle h = ProgressHandle.createHandle("Collecting workspace projects...", () -> {
                    cancel.set(true);
                    return true;
                });
                h.start();
                try {
                    for (FileObject candidate : projectCandidates) {
                        if (cancel.get()) {
                            break;
                        }
                        Project prj = FileOwnerQuery.getOwner(candidate);
                        if (prj == null) {
                            collectProjectCandidates(candidate, additionalCandidates, cancel);
                        }
                    }
                } catch (IOException ex) {
                    LOG.log(Level.FINE, null, ex);
                } finally {
                    h.finish();
                }
                if (!cancel.get()) {
                    projectCandidates.addAll(additionalCandidates);
                }
                workspaceProjects.asyncOpenSelectedProjects0(prjs, projectCandidates, true, true);
            });

            // chain showIndexingComplete message after initial project open.
            prjs.thenApply((candidates) -> {
                Project[] nonNulls = Arrays.asList(candidates).stream().filter(Objects::nonNull).toArray(Project[]::new);
                possibleWaitedPrjs.complete(nonNulls);
                return nonNulls;
            }).thenApply(this::showIndexingCompleted);

            initializeOptions();

            workspaceService.setClientWorkspaceFolders(init.getWorkspaceFolders());

            // but complete the InitializationRequest independently of the project initialization.
            return CompletableFuture.completedFuture(
                    finishInitialization(
                        constructInitResponse(init, checkJavaSupport(), capa)
                    )
            );
        }

        private void collectProjectCandidates(FileObject fo, List<FileObject> candidates, AtomicBoolean cancel) throws IOException {
            for (FileObject chld : fo.getChildren()) {
                if (cancel.get()) {
                    return;
                }
                if (chld.isFolder() && !chld.isSymbolicLink()) {
                    Project prj = FileOwnerQuery.getOwner(chld);
                    if (prj != null) {
                        candidates.add(chld);
                    } else {
                        collectProjectCandidates(chld, candidates, cancel);
                    }
                }
            }
        }

        private void initializeOptions() {
            // Request maven user settings from the client as early as possible.
            // Project opening may already be in progress, so the first embedder
            // can still be created with the default settings.xml path.
            client.getClientConfigurationManager().getConfiguration(NETBEANS_MAVEN_USER_SETTINGS).thenAccept(c -> {
                JsonPrimitive newMavenUserSettingsPath = c instanceof JsonPrimitive jp? jp : null;
                workspaceService.updateMavenUserSettingsPreferences(newMavenUserSettingsPath);
            });

            workspaceProjects.getWorkspaceProjects().thenAccept(projects -> {
                List<String> defaultConfigs = List.of(NETBEANS_JAVA_HINTS, NETBEANS_PROJECT_JDKHOME);
                List<String> projectConfigs = List.of(NETBEANS_FORMAT, NETBEANS_JAVA_IMPORTS);

                final FileObject projectDirectory = projects != null && projects.length > 0 ? projects[0].getProjectDirectory(): null;
                
                List<String> allConfigs = new ArrayList<>(defaultConfigs);
                if (projectDirectory != null) {
                    allConfigs.addAll(projectConfigs);
                }

                client.getClientConfigurationManager().getConfigurations(
                        allConfigs,
                        projectDirectory != null ? Utils.toUri(projectDirectory) : null
                ).thenAccept(configs -> {
                    if (configs != null && !configs.isEmpty()) {
                        if (configs.get(0) instanceof JsonObject) {
                            textDocumentService.updateJavaHintPreferences((JsonObject) configs.get(0));
                        } else {
                            textDocumentService.hintsSettingsRead = true;
                            textDocumentService.reRunDiagnostics();
                        }

                        JsonPrimitive newProjectJDKHomePath = null;
                        if (configs.size() > 1 && configs.get(1) instanceof JsonPrimitive) {
                            newProjectJDKHomePath = (JsonPrimitive) configs.get(1);
                        }
                        textDocumentService.updateProjectJDKHome(newProjectJDKHomePath);

                        if (projectDirectory != null) {
                            if (configs.size() > 2 && configs.get(2) instanceof JsonObject) {
                                workspaceService.updateJavaFormatPreferences(projectDirectory, (JsonObject) configs.get(2));
                            }

                            if (configs.size() > 3 && configs.get(3) instanceof JsonObject) {
                                workspaceService.updateJavaImportPreferences(projectDirectory, (JsonObject) configs.get(3));
                            }
                        }
                    } else {
                        textDocumentService.hintsSettingsRead = true;
                        textDocumentService.reRunDiagnostics();
                        textDocumentService.updateProjectJDKHome(null);
                    }
                });
            });
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
            shutdownReqReceived = true; 
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void exit() {
            int exitCode = shutdownReqReceived ? 0 : 1;
            LifecycleManager.getDefault().exit(exitCode);
        }

        @JsonDelegate
        public TreeViewService getTreeViewService() {
            return treeService;
        }

        @JsonDelegate
        public InputService getInputService() {
            return inputService;
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
            sessionServices.add(treeService.getNodeRegistry());
            sessionServices.add(inputService.getRegistry());
            sessionServices.add(workspaceService.getWorkspace());
            ((LanguageClientAware) getTextDocumentService()).connect(client);
            ((LanguageClientAware) getWorkspaceService()).connect(client);
            ((LanguageClientAware) treeService).connect(client);
        }

        @Override
        public void setTrace(SetTraceParams params) {
            // no op: there's already a lot of noise in the log, and the console log
            // can be controlled by a commandline parameter to the NBLS.
        }

        @Override
        public List<FileObject> getClientWorkspaceFolders() {
            return workspaceService.getClientWorkspaceFolders();
        }
    }

    public static final String NBLS_BUILD_WORKSPACE =  "nbls.build.workspace";
    public static final String NBLS_CLEAN_WORKSPACE =  "nbls.clean.workspace";
    public static final String NBLS_NEW_FROM_TEMPLATE =  "nbls.new.from.template";
    public static final String NBLS_NEW_PROJECT =  "nbls.new.project";
    public static final String JAVA_GET_PROJECT_SOURCE_ROOTS = "nbls.java.get.project.source.roots";
    public static final String JAVA_GET_PROJECT_CLASSPATH = "nbls.java.get.project.classpath";
    public static final String JAVA_GET_PROJECT_PACKAGES = "nbls.java.get.project.packages";
    public static final String NBLS_LOAD_WORKSPACE_TESTS =  "nbls.load.workspace.tests";
    public static final String NBLS_RESOLVE_STACKTRACE_LOCATION = "nbls.resolve.stacktrace.location";
    public static final String JAVA_SUPER_IMPLEMENTATION = "nbls.java.super.implementation";
    public static final String NBLS_GRAALVM_PAUSE_SCRIPT = "nbls.graalvm.pause.script";
    public static final String NBLS_RUN_PROJECT_ACTION = "nbls.project.run.action";
    public static final String NBLS_GET_ARCHIVE_FILE_CONTENT = "nbls.get.archive.file.content";

    /**
     * Enumerates project configurations.
     */
    public static final String NBLS_FIND_PROJECT_CONFIGURATIONS = "nbls.project.configurations";
    /**
     * Enumerates attach debugger configurations.
     */
    public static final String JAVA_FIND_DEBUG_ATTACH_CONFIGURATIONS = "nbls.java.attachDebugger.configurations";
    /**
     * Enumerates JVM processes eligible for debugger attach.
     */
    public static final String JAVA_FIND_DEBUG_PROCESS_TO_ATTACH = "nbls.java.attachDebugger.pickProcess";
    /**
     * Enumerates native processes eligible for debugger attach.
     */
    public static final String NATIVE_IMAGE_FIND_DEBUG_PROCESS_TO_ATTACH = "nbls.nativeImage.attachDebugger.pickProcess";
    /**
     * Provides code-completion of configurations.
     */
    public static final String NBLS_PROJECT_CONFIGURATION_COMPLETION = "nbls.project.configuration.completion";
    /**
     * Provides resolution of project problems.
     */
    public static final String NBLS_PROJECT_RESOLVE_PROJECT_PROBLEMS = "nbls.project.resolveProjectProblems";


    /**
     * Diagnostic / test command: clears NBLS internal project caches. Useful between testcases and after
     * new project files were generated into workspace subtree.
     */
    public static final String NBLS_CLEAR_PROJECT_CACHES =  "nbls.clear.project.caches";
    
    /**
     * For a project directory, returns basic project information and structure.
     * Syntax: nbls.project.info(locations : String | String[], options? : { projectStructure? : boolean; actions? : boolean; recursive? : boolean }) : LspProjectInfo
     */
    public static final String NBLS_PROJECT_INFO = "nbls.project.info";

    /**
     * Provides enable preview for given project
     */
    public static final String JAVA_ENABLE_PREVIEW = "nbls.java.project.enable.preview";

    /**
     * Provides symbols for the given document
     */
    public static final String NBLS_DOCUMENT_SYMBOLS =  "nbls.document.symbols";
    
    /**
     * Returns diagnostics as they would be published by the asynchronous diagnotic task triggered by
     * text changes.
     */
    public static final String NBLS_GET_DIAGNOSTICS = "nbls.get.diagnostics";
    
    /**
     * Returns the directories of NBLS. Returns userdir and the cluster directories.
     */
    public static final String NBLS_GET_SERVER_DIRECTORIES = "nbls.server.directories";

    static final String INDEXING_COMPLETED = "Indexing completed.";
    static final String NO_JAVA_SUPPORT = "Cannot initialize Java support on JDK ";

    static final NbCodeLanguageClient STUB_CLIENT = new NbCodeLanguageClient() {
        private final NbCodeClientCapabilities caps = new NbCodeClientCapabilities();
        private final ClientConfigurationManager confManager = new ClientConfigurationManager(this);

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
        public CompletableFuture<Map<String, Either<List<QuickPickItem>, String>>> showMultiStepInput(ShowMutliStepInputParams params) {
            logWarning(params);
            return CompletableFuture.completedFuture(null);
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
        public CompletableFuture<String> createTextEditorDecoration(DecorationRenderOptions params) {
            logWarning(params);
            CompletableFuture<String> x = new CompletableFuture<>();
            x.complete(null);
            return x;
        }

        @Override
        public void setTextEditorDecoration(SetTextEditorDecorationParams params) {
            logWarning(params);
        }

        @Override
        public void disposeTextEditorDecoration(String params) {
            logWarning(params);
        }

        @Override
        public void logMessage(MessageParams message) {
            logWarning(message);
        }

        @Override
        public void notifyNodeChange(NodeChangedParams params) {
            logWarning(params);
        }

        @Override
        public CompletableFuture<String> showHtmlPage(HtmlPageParams params) {
            logWarning(params);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<String> execInHtmlPage(HtmlPageParams params) {
            logWarning(params);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> configurationUpdate(UpdateConfigParams params) {
            logWarning(params);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Boolean> requestDocumentSave(SaveDocumentRequestParams documentUris) {
            logWarning(Arrays.asList(documentUris));
            return CompletableFuture.completedFuture(false);
        }

        @Override
        public CompletableFuture<Void> writeOutput(OutputMessage lm) {
            logWarning(lm.message);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> showOutput(String outputName) {
            logWarning("Show output: " + outputName); //NOI18N
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> closeOutput(String outputName) {
            logWarning("Close output: " + outputName); //NOI18N
            return CompletableFuture.completedFuture(null);
        }
        
        @Override
        public CompletableFuture<Void> resetOutput(String outputName) {
            logWarning("Reset output: " + outputName); //NOI18N
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public ClientConfigurationManager getClientConfigurationManager() {
            return confManager;
        }
    };


    private static boolean groovyClassWarningLogged;

    /**
     * Hacky way to enable or disable Groovy support. Since it is hack, it will disable Groovy for the whole NBJLS, not just a specific client / project. Should
     * be revisited after NetBeans 12.5, after Groovy parsing improves
     * @param caps
     */
    private static void hackConfigureGroovySupport(NbCodeClientCapabilities caps) {
        boolean b = caps != null && caps.wantsGroovySupport();
        try {
            Class<?> clazz = Lookup.getDefault().lookup(ClassLoader.class).loadClass("org.netbeans.modules.groovy.editor.api.GroovyIndexer");
            Method m = clazz.getDeclaredMethod("setIndexingEnabled", Boolean.TYPE);
            m.setAccessible(true);
            m.invoke(null, b);
        } catch (ClassNotFoundException ex) {
            // java.lang.ClassNotFoundException is expected when Groovy support is not activated / enabled. Do not log, if the
            // client wants groovy disabled, which is obviuously true in this case :)
            if (b && !groovyClassWarningLogged) {
                groovyClassWarningLogged = true;
                LOG.log(Level.WARNING, "Unable to configure Groovy indexing: Groovy support is not enabled");
            }
        } catch (ReflectiveOperationException ex) {
            if (!groovyClassWarningLogged) {
                groovyClassWarningLogged = true;
                LOG.log(Level.WARNING, "Unable to configure Groovy support", ex);
            }
        }
    }

    private static boolean antClassWarningLogged;
    private static void hackNoReuseOfOutputsForAntProjects() {
        final String PROP_AUTO_CLOSE_TABS = "autoCloseTabs"; // NOI18N
        try {
            Class antSettings = Lookup.getDefault().lookup(ClassLoader.class).loadClass("org.apache.tools.ant.module.AntSettings");
            Preferences prefs = NbPreferences.forModule(antSettings);
            prefs.putBoolean(PROP_AUTO_CLOSE_TABS, false);
        } catch (ReflectiveOperationException ex) {
            if (!antClassWarningLogged) {
                antClassWarningLogged = true;
                LOG.log(Level.WARNING, "Unable to configure Ant support", ex);
            }
        }
    }

    public static class CustomIndexerTelemetryFactory extends CustomIndexerFactory {

        private static CustomIndexerTelemetryFactory INSTANCE;

        private final CustomIndexer noOp = new CustomIndexer() {
            @Override
            protected void index(Iterable<? extends Indexable> files, Context context) {
            }
        };

        @MimeRegistration(mimeType="", service=CustomIndexerFactory.class)
        public static CustomIndexerTelemetryFactory getDefault() {
            if (INSTANCE == null) {
                INSTANCE = new CustomIndexerTelemetryFactory();
            }
            return INSTANCE;
        }

        private CustomIndexerTelemetryFactory() {
        }

        @Override
        public synchronized boolean scanStarted(Context context) {
            LSP_SERVER_TELEMETRY.sendTelemetry(new TelemetryEvent(MessageType.Info.toString(), LSP_SERVER_TELEMETRY.SCAN_START_EVT, "nbls.scanStarted")); 
	    return true;
        }

        @Override
        public synchronized void scanFinished(Context context) {
            LSP_SERVER_TELEMETRY.sendTelemetry(new TelemetryEvent(MessageType.Info.toString(),LSP_SERVER_TELEMETRY.SCAN_END_EVT,"nbls.scanFinished"));
            ERR_NOTIFIER.notifyErrors(context.getRootURI());
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
        }

        @Override
        public String getIndexerName() {
            return "CustomIndexerTelemetryFactory";
        }

        @Override
        public int getIndexVersion() {
            return 1;
        }

        @Override
        public CustomIndexer createIndexer() {
            return noOp;
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return false;
        }
    }
}
