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
package org.netbeans.modules.java.lsp.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.jsonrpc.Launcher;
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
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.java.lsp.server.text.TextDocumentServiceImpl;
import org.netbeans.modules.java.lsp.server.workspace.WorkspaceServiceImpl;
import org.netbeans.modules.parsing.impl.indexing.DefaultCacheFolderProvider;
import org.netbeans.modules.parsing.impl.indexing.implspi.CacheFolderProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author lahvac
 */
public class Server {
    public static void main(String... args) throws Exception {
        OutputStream debugIn = new FileOutputStream("/tmp/in-debug.txt");
        OutputStream debugOut = new FileOutputStream("/tmp/out-debug.txt");
        run(new InputStream() {
            @Override
            public int read() throws IOException {
                int r = System.in.read();
                debugIn.write(r);
                debugIn.flush();
                return r;
            }
        }, new OutputStream() {
            @Override
            public void write(int w) throws IOException {
                System.out.write(w);
                System.out.flush();
                debugOut.write(w);
                debugOut.flush();
            }
        });
    }

    public static void run(InputStream in, OutputStream out) throws Exception {
        Path tempDir = Files.createTempDirectory("lsp-server");
        File userdir = tempDir.resolve("scratch-user").toFile();
        File cachedir = tempDir.resolve("scratch-cache").toFile();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        File varLog = new File(new File(userdir, "var"), "log");
        varLog.mkdirs();
        System.setProperty("jdk.home", System.getProperty("java.home")); //for j2seplatform
        Class<?> main = Class.forName("org.netbeans.core.startup.Main");
        main.getDeclaredMethod("initializeURLFactory").invoke(null);
        new File(cachedir, "index").mkdirs();
        Class jsClass = JavaSource.class;
        File javaCluster = new File(jsClass.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getParentFile();
        System.setProperty("netbeans.dirs", javaCluster.getAbsolutePath());
        CacheFolderProvider.getCacheFolderForRoot(Places.getUserDirectory().toURI().toURL(), EnumSet.noneOf(CacheFolderProvider.Kind.class), CacheFolderProvider.Mode.EXISTENT);

        Lookup.getDefault().lookup(ModuleInfo.class); //start the module system

        LanguageServerImpl server = new LanguageServerImpl();
        Launcher<LanguageClient> serverLauncher = LSPLauncher.createServerLauncher(server, in, out);
        ((LanguageClientAware) server).connect(serverLauncher.getRemoteProxy());
        serverLauncher.startListening();

        while (true) {
            try {
                Thread.sleep(100000);
            } catch (InterruptedException ex) {
                //ignore
            }
        }
    }

    private static class LanguageServerImpl implements LanguageServer, LanguageClientAware {

        private static final Logger LOG = Logger.getLogger(LanguageServerImpl.class.getName());
        private LanguageClient client;
        private final TextDocumentService textDocumentService = new TextDocumentServiceImpl();

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
            //XXX: ensure project opened:
            try {
                Class.forName("org.netbeans.modules.project.ui.OpenProjectList", false, Lookup.getDefault().lookup(ClassLoader.class)).getDeclaredMethod("waitProjectsFullyOpen").invoke(null);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
            OpenProjects.getDefault().open(projects.toArray(new Project[0]), false);
            try {
                OpenProjects.getDefault().openProjects().get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new IllegalStateException(ex);
            }
            try {
                JavaSource.create(ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, ClassPath.EMPTY))
                          .runWhenScanFinished(cc -> {
                  client.showMessage(new MessageParams(MessageType.Info, INDEXING_COMPLETED));
                  //todo: refresh diagnostics all open editor?
                }, true);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
            ServerCapabilities capabilities = new ServerCapabilities();
            capabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental);
            capabilities.setCompletionProvider(new CompletionOptions());
            capabilities.setCodeActionProvider(true);
            capabilities.setDocumentSymbolProvider(true);
            capabilities.setDefinitionProvider(true);
            return CompletableFuture.completedFuture(new InitializeResult(capabilities));
        }

        @Override
        public CompletableFuture<Object> shutdown() {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void exit() {
            System.exit(1);
        }

        @Override
        public TextDocumentService getTextDocumentService() {
            return textDocumentService;
        }

        @Override
        public WorkspaceService getWorkspaceService() {
            return new WorkspaceServiceImpl();
        }

        @Override
        public void connect(LanguageClient client) {
            this.client = client;
            ((LanguageClientAware) getTextDocumentService()).connect(client);
        }
    }

    static final String INDEXING_COMPLETED = "Indexing completed.";
}
