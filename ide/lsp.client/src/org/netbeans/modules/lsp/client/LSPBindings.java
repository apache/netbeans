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
package org.netbeans.modules.lsp.client;

import com.google.gson.InstanceCreator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeActionCapabilities;
import org.eclipse.lsp4j.CodeActionResolveSupportCapabilities;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionItemCapabilities;
import org.eclipse.lsp4j.CompletionItemResolveSupportCapabilities;
import org.eclipse.lsp4j.DiagnosticWorkspaceCapabilities;
import org.eclipse.lsp4j.DocumentSymbolCapabilities;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.PublishDiagnosticsCapabilities;
import org.eclipse.lsp4j.ResourceOperationKind;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensCapabilities;
import org.eclipse.lsp4j.SemanticTokensClientCapabilitiesRequests;
import org.eclipse.lsp4j.SemanticTokensLegend;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SymbolCapabilities;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.SymbolKindCapabilities;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceEditCapabilities;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.eclipse.lsp4j.util.Preconditions;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.progress.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.lsp.client.bindings.LanguageClientImpl;
import org.netbeans.modules.lsp.client.bindings.TextDocumentSyncServerCapabilityHandler;
import org.netbeans.modules.lsp.client.options.MimeTypeInfo;
import org.netbeans.modules.lsp.client.spi.ServerRestarter;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider.LanguageServerDescription;
import org.netbeans.modules.lsp.client.spi.MultiMimeLanguageServerProvider;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.OnStop;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lahvac
 */
public class LSPBindings {

    private static final Logger LOG = Logger.getLogger(LSPBindings.class.getName());
    private static final int DELAY = 500;
    private static final int LSP_KEEP_ALIVE_MINUTES = 10;
    private static final int INVALID_START_TIME = 1 * 60 * 1000;
    private static final int INVALID_START_MAX_COUNT = 5;
    private static final RequestProcessor WORKER = new RequestProcessor(LanguageClientImpl.class.getName(), 1, false, false);
    private static final ChangeSupport cs = new ChangeSupport(LSPBindings.class);
    private static final Map<LSPBindings,Long> lspKeepAlive = new IdentityHashMap<>();
    private static final Map<URI, Map<String, Map<LanguageServerProvider, ServerDescription>>> project2MimeType2Server = new HashMap<>();
    private static final Map<FileObject, Map<String, LSPBindings>> workspace2Extension2Server = new HashMap<>();

    static {
        //Don't perform null checks. The servers may not adhere to the specification, and send illegal nulls.
        Preconditions.enableNullChecks(false);

        // Remove LSP Servers from strong reference tracking, that have not
        // been accessed more than LSP_KEEP_ALIVE_MINUTES minutes
        WORKER.scheduleAtFixedRate(
            () -> {
                synchronized (LSPBindings.class) {
                    long tooOld = System.currentTimeMillis() - (LSP_KEEP_ALIVE_MINUTES * 60L * 1000L);
                    Iterator<Entry<LSPBindings, Long>> iterator = lspKeepAlive.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Entry<LSPBindings, Long> entry = iterator.next();
                        if (entry.getValue() < tooOld) {
                            iterator.remove();
                        }
                    }
                }
            },
            Math.max(LSP_KEEP_ALIVE_MINUTES / 2, 1),
            Math.max(LSP_KEEP_ALIVE_MINUTES / 2, 1),
            TimeUnit.MINUTES);
    }

    private static final Map<FileObject, Map<Object/*BackgroundTask|SimpleBackgroundTask*/, RequestProcessor.Task>> backgroundTasks = new WeakHashMap<>();
    private final Set<FileObject> openedFiles = new HashSet<>();

    public static synchronized @NonNull List<LSPBindings> getBindings(FileObject file) {
        List<LSPBindings> fromWorkspace = List.of();

        for (Entry<FileObject, Map<String, LSPBindings>> e : workspace2Extension2Server.entrySet()) {
            if (FileUtil.isParentOf(e.getKey(), file)) {
                LSPBindings bindings = e.getValue().get(file.getExt());

                if (bindings != null) {
                    fromWorkspace = List.of(bindings);
                }

                break;
            }
        }

        String mimeType = FileUtil.getMIMEType(file);
        Project prj = FileOwnerQuery.getOwner(file);

        List<LSPBindings> fromProviders = mimeType != null ? getBindingsImpl(prj, file, mimeType) : List.of();
        if (fromWorkspace.isEmpty()) {
            return fromProviders;
        } else if (fromProviders.isEmpty()) {
            return fromWorkspace;
        } else {
            List<LSPBindings> result = new ArrayList<>();

            result.addAll(fromWorkspace);
            result.addAll(fromProviders);

            return result;
        }
    }

    public static void ensureServerRunning(Project prj, String mimeType) {
        getBindingsImpl(prj, prj.getProjectDirectory(), mimeType);
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public static synchronized List<LSPBindings> getBindingsImpl(Project prj, FileObject file, String mimeType) {
        FileObject dir;

        if (prj == null) {
            dir = file.getParent();
            File dirFile = FileUtil.toFile(dir);
            if (dirFile != null &&
                dirFile.getName().startsWith("vcs-") &&
                dirFile.getAbsolutePath().startsWith(System.getProperty("java.io.tmpdir"))) {
                //diff dir, don't start servers:
                return null;
            }
        } else {
            dir = prj.getProjectDirectory();
        }

        URI uri = dir.toURI();

        return buildBindings(prj, mimeType, dir, uri);
    }

    @Messages({
        "# {0} - the mime type for which the LSP server failed to start",
        "TITLE_FailedToStart=LSP Server for {0} failed to start too many times.",
        "DETAIL_FailedToStart=The LSP Server failed to start too many times in a short time, and will not be restarted anymore."
    })
    private static void startFailed(ServerDescription description, String mimeType) {
        long timeStamp = System.currentTimeMillis();
        if (timeStamp - description.lastStartTimeStamp < INVALID_START_TIME) {
            description.failedCount++;
            if (description.failedCount == INVALID_START_MAX_COUNT) {
                NotificationDisplayer.getDefault().notify(Bundle.TITLE_FailedToStart(mimeType),
                                                          ImageUtilities.loadIcon("org/netbeans/modules/lsp/client/resources/error_16.png"),
                                                          Bundle.DETAIL_FailedToStart(),
                                                          null);
            }
        } else {
            description.failedCount = 0;
        }
        description.lastStartTimeStamp = timeStamp;
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject", "ResultOfObjectAllocationIgnored"})
    private static List<LSPBindings> buildBindings(Project prj, String mt, FileObject dir, URI baseUri) {
        MimeTypeInfo mimeTypeInfo = new MimeTypeInfo(mt);
        List<LSPBindings> servers = new ArrayList<>();
        Map<LanguageServerProvider, ServerDescription> provider2Description =
                project2MimeType2Server.computeIfAbsent(baseUri, p -> new HashMap<>())
                                       .computeIfAbsent(mt, m -> new HashMap<>());

        for (LanguageServerProvider provider : MimeLookup.getLookup(mt).lookupAll(LanguageServerProvider.class)) {
            ServerDescription serverDescription = provider2Description.computeIfAbsent(provider, m -> new ServerDescription());
            LSPBindings existingBindings = null;

            if (serverDescription.bindings != null) {
                existingBindings = serverDescription.bindings.get();
            }

            if (existingBindings != null && existingBindings.process != null && !existingBindings.process.isAlive()) {
                startFailed(serverDescription, mt);
                existingBindings = null;
            }

            if (serverDescription.failedCount >= INVALID_START_MAX_COUNT) {
                continue;
            }

            if (existingBindings != null) {
                servers.add(existingBindings);
                continue;
            }

            ServerRestarter restarter = () -> {
                synchronized (LSPBindings.class) {
                    Map<LanguageServerProvider, ServerDescription> provider2Desc = project2MimeType2Server.getOrDefault(baseUri, Collections.emptyMap()).get(mt);
                    ServerDescription description = provider2Desc != null ? provider2Desc.remove(provider) : null;

                    // Remove any other mimetypes as well.
                    if (description != null) {
                        for(String anotherMT: description.mimeTypes) {
                            project2MimeType2Server.get(baseUri).remove(anotherMT);
                        }
                    }
                    Reference<LSPBindings> bRef = description != null ? description.bindings : null;
                    LSPBindings b = bRef != null ? bRef.get() : null;

                    if (b != null) {
                        lspKeepAlive.remove(b);

                        try {
                            b.server.shutdown().get();
                        } catch (InterruptedException | ExecutionException ex) {
                            LOG.log(Level.FINE, null, ex);
                        }
                        if (b.process != null) {
                            b.process.destroy();
                        }
                    }
                }
            };

            final Lookup lkp = prj != null ? Lookups.fixed(prj, mimeTypeInfo, restarter) : Lookups.fixed(mimeTypeInfo, restarter);
            serverDescription.mimeTypes = Collections.singleton(mt);
            // If this is a MultiMimeLanguageServerProvider, then retrieve all 
            // mime types handled by this server.
            if (provider instanceof MultiMimeLanguageServerProvider) {
                serverDescription.mimeTypes = new HashSet<>(((MultiMimeLanguageServerProvider)provider).getMimeTypes());
            }
            LanguageServerDescription desc = provider.startServer(lkp);

            if (desc != null) {
                LSPBindings b = LanguageServerProviderAccessor.getINSTANCE().getBindings(desc);

                if (b != null) {
                    servers.add(b);
                    continue;
                }

                try {
                    LanguageClientImpl lci = new LanguageClientImpl();
                    LanguageServer server = LanguageServerProviderAccessor.getINSTANCE().getServer(desc);
                    Process process;
                    if (server == null) {
                        InputStream in = LanguageServerProviderAccessor.getINSTANCE().getInputStream(desc);
                        OutputStream out = LanguageServerProviderAccessor.getINSTANCE().getOutputStream(desc);
                        process = LanguageServerProviderAccessor.getINSTANCE().getProcess(desc);
                        Launcher.Builder<LanguageServer> launcherBuilder = new LSPLauncher.Builder<LanguageServer>()
                                .setLocalService(lci)
                                .setRemoteInterface(LanguageServer.class)
                                .setInput(in)
                                .setOutput(out)
                                .configureGson(gson -> {
                                    gson.registerTypeAdapter(SemanticTokensLegend.class, new InstanceCreator<SemanticTokensLegend>() {
                                        @Override
                                        public SemanticTokensLegend createInstance(Type type) {
                                            return new SemanticTokensLegend(Collections.emptyList(), Collections.emptyList());
                                        }
                                    });
                                    gson.registerTypeAdapter(SemanticTokens.class, new InstanceCreator<SemanticTokens>() {
                                        @Override
                                        public SemanticTokens createInstance(Type type) {
                                            return new SemanticTokens(Collections.emptyList());
                                        }
                                    });
                                });

                        if (LOG.isLoggable(Level.FINER)) {
                            PrintWriter pw = new PrintWriter(new Writer() {
                                StringBuffer sb = new StringBuffer();

                                @Override
                                public void write(char[] cbuf, int off, int len) throws IOException {
                                    sb.append(cbuf, off, len);
                                }

                                @Override
                                public void flush() throws IOException {
                                    LOG.finer(sb.toString());
                                }

                                @Override
                                public void close() throws IOException {
                                    sb.setLength(0);
                                    sb.trimToSize();
                                }
                            });
                            launcherBuilder.traceMessages(pw);
                        }
                        Launcher<LanguageServer> launcher = launcherBuilder.create();
                        launcher.startListening();
                        server = launcher.getRemoteProxy();
                    } else {
                        process = null;
                        if (server instanceof LanguageClientAware aware) {
                            aware.connect(lci);
                        }
                    }
                    InitializeResult result = initServer(process, server, dir); //XXX: what if a different root is expected????
                    server.initialized(new InitializedParams());
                    b = new LSPBindings(server, result, LanguageServerProviderAccessor.getINSTANCE().getProcess(desc));
                    // Register cleanup via LSPReference#run
                    new LSPReference(b, Utilities.activeReferenceQueue());
                    lci.setBindings(b);
                    LanguageServerProviderAccessor.getINSTANCE().setBindings(desc, b);

                    serverDescription.bindings = new WeakReference<>(b);
                    serverDescription.lastStartTimeStamp = System.currentTimeMillis();
                    // If ServerDescription acknowledges another mimetypes, add these
                    // to project2MimeType2Server too.
                    Map<String, Map<LanguageServerProvider, ServerDescription>> mimeType2Server = project2MimeType2Server.get(baseUri);

                    for(String otherMimeType: serverDescription.mimeTypes) {
                        mimeType2Server.computeIfAbsent(otherMimeType, m -> new HashMap<>())
                                       .put(provider, serverDescription);
                    }
                    TextDocumentSyncServerCapabilityHandler.refreshOpenedFilesInServers();
                    WORKER.post(() -> cs.fireChange());

                    lspKeepAlive.put(b, System.currentTimeMillis());

                    servers.add(b);

                    continue;
                } catch (InterruptedException | ExecutionException ex) {
                    LOG.log(Level.WARNING, null, ex);
                }

                startFailed(serverDescription, mt);
            }
        }
        return servers;
    }

    @Messages("LBL_Connecting=Connecting to language server")
    public static void addBindings(FileObject root, int port, String... extensions) {
        BaseProgressUtils.showProgressDialogAndRun(() -> {
            try {
                Socket s = new Socket(InetAddress.getLocalHost(), port);
                LanguageClientImpl lc = new LanguageClientImpl();
                InputStream in = s.getInputStream();
                OutputStream out = s.getOutputStream();
                Launcher<LanguageServer> launcher = LSPLauncher.createClientLauncher(lc, in, new OutputStream() {
                    @Override
                    public void write(int w) throws IOException {
                        out.write(w);
                        if (w == '\n')
                            out.flush();
                    }
                });
                launcher.startListening();
                LanguageServer server = launcher.getRemoteProxy();
                InitializeResult result = initServer(null, server, root);
                server.initialized(new InitializedParams());
                LSPBindings bindings = new LSPBindings(server, result, null);

                lc.setBindings(bindings);

                synchronized(LSPBindings.class) {
                    workspace2Extension2Server.put(root, 
                        Arrays.stream(extensions)
                        .collect(Collectors.toMap(k -> k, v -> bindings)));
                }
                WORKER.post(() -> cs.fireChange());
            } catch (InterruptedException | ExecutionException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }, Bundle.LBL_Connecting());
    }

    @SuppressWarnings("deprecation")
    private static InitializeResult initServer(Process p, LanguageServer server, FileObject root) throws InterruptedException, ExecutionException {
       InitializeParams initParams = new InitializeParams();
       initParams.setRootUri(Utils.toURI(root));
       final File rootFile = FileUtil.toFile(root);
       if (rootFile != null) {
           initParams.setRootPath(rootFile.getAbsolutePath()); //some servers still expect root path
       }
       initParams.setProcessId(0);
       //TODO: rewrite this to JSon?
       TextDocumentClientCapabilities tdcc = new TextDocumentClientCapabilities();
       DocumentSymbolCapabilities dsc = new DocumentSymbolCapabilities();
       dsc.setHierarchicalDocumentSymbolSupport(true);
       dsc.setSymbolKind(new SymbolKindCapabilities(Arrays.asList(SymbolKind.values())));
       tdcc.setDocumentSymbol(dsc);
       tdcc.setSemanticTokens(new SemanticTokensCapabilities(new SemanticTokensClientCapabilitiesRequests(true), KNOWN_TOKEN_TYPES, KNOWN_TOKEN_MODIFIERS, Arrays.asList()));
       CodeActionCapabilities codeActionCapa = new CodeActionCapabilities();
       codeActionCapa.setResolveSupport(new CodeActionResolveSupportCapabilities(List.of("edit")));
       tdcc.setCodeAction(codeActionCapa);
       CompletionCapabilities completionCapa = new CompletionCapabilities();
       CompletionItemCapabilities completionItemCapa = new CompletionItemCapabilities();
       completionItemCapa.setLabelDetailsSupport(true);
       completionItemCapa.setSnippetSupport(true);
       completionItemCapa.setResolveSupport(new CompletionItemResolveSupportCapabilities(List.of("additionalTextEdits", "documentation", "textEdit")));
       completionCapa.setCompletionItem(completionItemCapa);
       tdcc.setCompletion(completionCapa);
       WorkspaceClientCapabilities wcc = new WorkspaceClientCapabilities();
       wcc.setWorkspaceEdit(new WorkspaceEditCapabilities());
       wcc.getWorkspaceEdit().setDocumentChanges(true);
       wcc.getWorkspaceEdit().setResourceOperations(Arrays.asList(ResourceOperationKind.Create, ResourceOperationKind.Delete, ResourceOperationKind.Rename));
       SymbolCapabilities sc = new SymbolCapabilities(new SymbolKindCapabilities(Arrays.asList(SymbolKind.values())));
       wcc.setSymbol(sc);
       PublishDiagnosticsCapabilities publishDiagnostics = new PublishDiagnosticsCapabilities();
       tdcc.setPublishDiagnostics(publishDiagnostics);
       initParams.setCapabilities(new ClientCapabilities(wcc, tdcc, null));
       CompletableFuture<InitializeResult> initResult = server.initialize(initParams);
       while (true) {
           try {
               return initResult.get(100, TimeUnit.MILLISECONDS);
           } catch (TimeoutException ex) {
               if (p != null && !p.isAlive()) {
                   InitializeResult emptyResult = new InitializeResult();
                   emptyResult.setCapabilities(new ServerCapabilities());
                   return emptyResult;
               }
           }
       }
    }
    private static final List<String> KNOWN_TOKEN_TYPES = Collections.unmodifiableList(Arrays.asList(
            "namespace", "package", "function", "method", "macro", "parameter",
            "variable", "struct", "enum", "class", "typeAlias", "typeParameter",
            "field", "enumMember", "keyword"
    ));

    private static final List<String> KNOWN_TOKEN_MODIFIERS = Collections.unmodifiableList(Arrays.asList(
            "static", "definition", "declaration"
    ));

    public static synchronized Set<LSPBindings> getAllBindings() {
        Set<LSPBindings> allBindings = Collections.newSetFromMap(new IdentityHashMap<>());

        project2MimeType2Server.values()
                               .stream()
                               .flatMap(n -> n.values().stream())
                               .flatMap(n -> n.values().stream())
                               .map(description -> description.bindings != null ? description.bindings.get() : null)
                               .filter(binding -> binding != null)
                               .forEach(allBindings::add);
        workspace2Extension2Server.values()
                                  .stream()
                                  .flatMap(n -> n.values().stream())
                                  .forEach(allBindings::add);

        return allBindings;
    }

    private final LanguageServer server;
    private final InitializeResult initResult;
    private final Process process;

    private LSPBindings(LanguageServer server, InitializeResult initResult, Process process) {
        this.server = server;
        this.initResult = initResult;
        this.process = process;
    }

    public TextDocumentService getTextDocumentService() {
        return server.getTextDocumentService();
    }

    public WorkspaceService getWorkspaceService() {
        return server.getWorkspaceService();
    }

    public InitializeResult getInitResult() {
        //XXX: defenzive copy?
        return initResult;
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public static synchronized void addBackgroundTask(FileObject file, BackgroundTask task) {
        RequestProcessor.Task req = WORKER.create(() -> {
            List<LSPBindings> bindingsList = getBindings(file);

            if (!bindingsList.isEmpty()) {
                task.run(bindingsList, file);
            }
        });

        backgroundTasks.computeIfAbsent(file, f -> new LinkedHashMap<>()).put(task, req);
        scheduleBackgroundTask(req);
    }

    public static synchronized void addBackgroundTask(FileObject file, SimpleBackgroundTask task) {
        RequestProcessor.Task req = WORKER.create(() -> {
            List<LSPBindings> bindingsList = getBindings(file);

            if (!bindingsList.isEmpty()) {
                task.run(file);
            }
        });

        backgroundTasks.computeIfAbsent(file, f -> new LinkedHashMap<>()).put(task, req);
        scheduleBackgroundTask(req);
    }

    public static synchronized void removeBackgroundTask(FileObject file, BackgroundTask task) {
        RequestProcessor.Task req = backgroundTasksMapFor(file).remove(task);

        if (req != null) {
            req.cancel();
        }
    }

    public static synchronized void removeBackgroundTask(FileObject file, SimpleBackgroundTask task) {
        RequestProcessor.Task req = backgroundTasksMapFor(file).remove(task);

        if (req != null) {
            req.cancel();
        }
    }

    public static void addChangeListener(ChangeListener l) {
        cs.addChangeListener(WeakListeners.change(l, cs));
    }

    public static void runOnBackground(Runnable r) {
        WORKER.post(r);
    }

    private static void scheduleBackgroundTask(RequestProcessor.Task req) {
        req.schedule(DELAY);
    }

    public static synchronized void rescheduleBackgroundTask(FileObject file, BackgroundTask task) {
        RequestProcessor.Task req = backgroundTasksMapFor(file).get(task);

        if (req != null) {
            scheduleBackgroundTask(req);
        }
    }

    public static synchronized void scheduleBackgroundTasks(FileObject file) {
        backgroundTasksMapFor(file).values().stream().forEach(LSPBindings::scheduleBackgroundTask);
    }

    private static Map<?, Task> backgroundTasksMapFor(FileObject file) {
        return backgroundTasks.computeIfAbsent(file, f -> new IdentityHashMap<>());
    }

    public Set<FileObject> getOpenedFiles() {
        return openedFiles;
    }

    public interface BackgroundTask {
        public void run(List<LSPBindings> bindings, FileObject file);
    }

    public interface SimpleBackgroundTask {
        public void run(FileObject file);
    }

    @OnStop
    public static class Cleanup implements Runnable {

        @Override
        @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
        public void run() {
            synchronized(LSPBindings.class) {
                for (Map<String, Map<LanguageServerProvider, ServerDescription>> mime2Bindings : project2MimeType2Server.values()) {
                    for (Map<LanguageServerProvider, ServerDescription> provider2Server : mime2Bindings.values()) {
                        for (ServerDescription description : provider2Server.values()) {
                            LSPBindings b = description.bindings != null ? description.bindings.get() : null;
                            if (b != null && b.process != null) {
                                b.process.destroy();
                            }
                        }
                    }
                }
                for (Map<String, LSPBindings> mime2Bindings : workspace2Extension2Server.values()) {
                    for (LSPBindings b : mime2Bindings.values()) {
                        if (b != null && b.process != null) {
                            b.process.destroy();
                        }
                    }
                }
            }
        }

    }

    /**
     * The {@code LSPReference} adds cleanup actions to LSP Bindings after the
     * bindings are GCed. The backing process is shutdown and the process
     * terminated.
     */
    private static class LSPReference extends WeakReference<LSPBindings> implements Runnable {
        private final LanguageServer server;
        private final Process process;

        @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
        public LSPReference(LSPBindings t, ReferenceQueue<? super LSPBindings> rq) {
            super(t, rq);
            this.server = t.server;
            this.process = t.process;
        }

        @Override
        public void run() {
            if(! process.isAlive()) {
                return;
            }
            CompletableFuture<Object> shutdownResult = server.shutdown();
            for (int i = 0; i < 300; i--) {
                try {
                    shutdownResult.get(100, TimeUnit.MILLISECONDS);
                    break;
                } catch (TimeoutException ex) {
                } catch (InterruptedException | ExecutionException ex) {
                    break;
                }
            }
            this.server.exit();
            try {
                if(! process.waitFor(30, TimeUnit.SECONDS)) {
                    process.destroy();
                }
            } catch (InterruptedException ex) {
                process.destroy();
            }

        }
    }
    private static class ServerDescription {
        public long lastStartTimeStamp;
        public int failedCount;
        public Reference<LSPBindings> bindings;
        public Set<String> mimeTypes;
    }
}
