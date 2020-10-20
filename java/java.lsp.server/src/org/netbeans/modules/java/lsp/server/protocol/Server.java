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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ServerCapabilities;
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
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author lahvac
 */
public final class Server {
    private Server() {
    }
    
    public static void launchServer(InputStream in, OutputStream out) {
        LanguageServerImpl server = new LanguageServerImpl();
        Launcher<NbCodeLanguageClient> serverLauncher = createLauncher(server, in, out);
        ((LanguageClientAware) server).connect(serverLauncher.getRemoteProxy());
        Future<Void> runningServer = serverLauncher.startListening();
        try {
            runningServer.get();
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private static Launcher<NbCodeLanguageClient> createLauncher(LanguageServerImpl server, InputStream in, OutputStream out) {
        return new LSPLauncher.Builder<NbCodeLanguageClient>()
            .setLocalService(server)
            .setRemoteInterface(NbCodeLanguageClient.class)
            .setInput(in)
            .setOutput(out)
            .wrapMessages(new ConsumeWithLookup(server.getSessionLookup())::attachLookup)
            .create();
    }
    
    /**
     * Processes message while the default Lookup is set to 
     * {@link LanguageServerImpl#getSessionLookup()}.
     */
    private static class ConsumeWithLookup {
        private final Lookup sessionLookup;

        public ConsumeWithLookup(Lookup sessionLookup) {
            this.sessionLookup = sessionLookup;
        }
        
        public MessageConsumer attachLookup(MessageConsumer delegate) {
            return new MessageConsumer() {
                @Override
                public void consume(Message msg) throws MessageIssueException, JsonRpcException {
                    Lookups.executeWith(sessionLookup, () -> {
                        delegate.consume(msg);
                    });
                }
            };
        }
    }
    
    private static class LanguageServerImpl implements LanguageServer, LanguageClientAware {

        private static final Logger LOG = Logger.getLogger(LanguageServerImpl.class.getName());
        private LanguageClient client;
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
        
        @Override
        public CompletableFuture<InitializeResult> initialize(InitializeParams init) {
            List<FileObject> projectCandidates = new ArrayList<>();
            List<WorkspaceFolder> folders = init.getWorkspaceFolders();
            if (folders != null) {
                for (WorkspaceFolder w : folders) {
                    try {
                        projectCandidates.add(TextDocumentServiceImpl.fromUri(w.getUri()));
                    } catch (MalformedURLException ex) {
                        LOG.log(Level.FINE, null, ex);
                    }
                }
            } else {
                String root = init.getRootUri();

                if (root != null) {
                    try {
                        projectCandidates.add(TextDocumentServiceImpl.fromUri(root));
                    } catch (MalformedURLException ex) {
                        LOG.log(Level.FINE, null, ex);
                    }
                } else {
                    //TODO: use getRootPath()?
                }
            }
            List<Project> projects = new ArrayList<>();
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
            try {
                JavaSource.create(ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, ClassPath.EMPTY))
                          .runWhenScanFinished(cc -> {
                  ((NbCodeLanguageClient)client).showStatusBarMessage(
                          new ShowStatusMessageParams(MessageType.Info, INDEXING_COMPLETED, 0));
                  //todo: refresh diagnostics all open editor?
                }, true);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
            ServerCapabilities capabilities = new ServerCapabilities();
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
            return CompletableFuture.completedFuture(new InitializeResult(capabilities));
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
        public void connect(LanguageClient client) {
            this.client = client;
            
            sessionServices.add(new WorkspaceIOContext() {
                @Override
                protected LanguageClient client() {
                    return client;
                }
            });
            sessionServices.add(new WorkspaceUIContext(client));
            
            ((LanguageClientAware) getTextDocumentService()).connect(client);
            ((LanguageClientAware) getWorkspaceService()).connect(client);
        }
    }

    public static final String JAVA_BUILD_WORKSPACE =  "java.build.workspace";
    public static final String GRAALVM_PAUSE_SCRIPT =  "graalvm.pause.script";
    static final String INDEXING_COMPLETED = "Indexing completed.";
}
