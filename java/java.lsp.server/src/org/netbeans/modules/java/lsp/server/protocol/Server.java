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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.jsonrpc.JsonRpcException;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.MessageIssueException;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
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
import org.netbeans.modules.java.lsp.server.Utils;
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
        
        public ConsumeWithLookup(Lookup sessionLookup) {
            this.sessionLookup = sessionLookup;
        }
        
        synchronized void attachClient(NbCodeLanguageClient client) {
            this.client = client;
        }
        
        public MessageConsumer attachLookup(MessageConsumer delegate) {
            return new MessageConsumer() {
                @Override
                public void consume(Message msg) throws MessageIssueException, JsonRpcException {
                    try {
                        DISPATCHERS.set(client);
                        Lookups.executeWith(sessionLookup, () -> {
                            delegate.consume(msg);
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
    
    
    private static class LanguageServerImpl implements LanguageServer, LanguageClientAware {

        private static final Logger LOG = Logger.getLogger(LanguageServerImpl.class.getName());
        private NbCodeClientWrapper client;
        private final TextDocumentService textDocumentService = new TextDocumentServiceImpl();
        private final WorkspaceService workspaceService = new WorkspaceServiceImpl();
        private final InstanceContent   sessionServices = new InstanceContent();
        private final Lookup sessionLookup = new ProxyLookup(
                new AbstractLookup(sessionServices),
                Lookup.getDefault()
        );
        
        Lookup getSessionLookup() {
            return sessionLookup;
        }
        
        private void asyncOpenSelectedProjects(CompletableFuture f, List<FileObject> projectCandidates) {
            List<Project> projects = new ArrayList<>();
            try {
                for (FileObject candidate : projectCandidates) {
                    Project prj = FileOwnerQuery.getOwner(candidate);
                    if (prj != null) {
                        projects.add(prj);
                    }
                }
                try {
                    Project[] previouslyOpened = OpenProjects.getDefault().openProjects().get();
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
                OpenProjects.getDefault().open(projects.toArray(new Project[0]), false);
                try {
                    OpenProjects.getDefault().openProjects().get();
                } catch (InterruptedException | ExecutionException ex) {
                    throw new IllegalStateException(ex);
                }
                for (Project prj : projects) {
                    //init source groups/FileOwnerQuery:
                    ProjectUtils.getSources(prj).getSourceGroups(Sources.TYPE_GENERIC);
                }
                Project[] prjs = projects.toArray(new Project[projects.size()]);
                f.complete(prjs);
            } catch (RuntimeException ex) {
                f.completeExceptionally(ex);
            }
        }
        
        private JavaSource showIndexingCompleted(Project[] opened) {
            try {
                final ClasspathInfo info = ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, ClassPath.EMPTY);
                final JavaSource source = JavaSource.create(info);
                if (source == null) {
                    SERVER_INIT_RP.post(() -> {
                        final String msg = NO_JAVA_SUPPORT + System.getProperty("java.version");
                        showStatusBarMessage(MessageType.Error, msg, 5000);
                    });
                } else {
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
                capabilities.setCodeActionProvider(true);
                capabilities.setDocumentSymbolProvider(true);
                capabilities.setDefinitionProvider(true);
                capabilities.setDocumentHighlightProvider(true);
                capabilities.setReferencesProvider(true);
                capabilities.setExecuteCommandProvider(new ExecuteCommandOptions(Arrays.asList(JAVA_BUILD_WORKSPACE, GRAALVM_PAUSE_SCRIPT)));
                capabilities.setWorkspaceSymbolProvider(true);
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
            CompletableFuture<Project[]> fProjects = new CompletableFuture<>();
            SERVER_INIT_RP.post(() -> asyncOpenSelectedProjects(fProjects, projectCandidates));
            
            return fProjects.
                    thenApply(this::showIndexingCompleted).
                    thenApply(this::constructInitResponse);
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
        public void connect(LanguageClient aClient) {
            this.client = new NbCodeClientWrapper((NbCodeLanguageClient)aClient);
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
