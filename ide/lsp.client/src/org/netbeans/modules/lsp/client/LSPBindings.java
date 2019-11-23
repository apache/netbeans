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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.DocumentSymbolCapabilities;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ResourceOperationKind;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceEditCapabilities;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.progress.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.lsp.client.bindings.LanguageClientImpl;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider.LanguageServerDescription;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.OnStop;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lahvac
 */
public class LSPBindings {

    private static final RequestProcessor WORKER = new RequestProcessor(LanguageClientImpl.class.getName(), 1, false, false);
    private static final int DELAY = 500;

    private static final Map<Project, Map<String, LSPBindings>> project2MimeType2Server = new WeakHashMap<>();
    private static final Map<FileObject, Map<String, LSPBindings>> workspace2Extension2Server = new HashMap<>();
    private final Map<FileObject, Map<BackgroundTask, RequestProcessor.Task>> backgroundTasks = new WeakHashMap<>();

    public static synchronized LSPBindings getBindings(FileObject file) {
        for (Entry<FileObject, Map<String, LSPBindings>> e : workspace2Extension2Server.entrySet()) {
            if (FileUtil.isParentOf(e.getKey(), file)) {
                LSPBindings bindings = e.getValue().get(file.getExt());

                if (bindings != null) {
                    return bindings;
                }

                break;
            }
        }
        Project prj = FileOwnerQuery.getOwner(file);

        if (prj == null)
            return null;

        String mimeType = FileUtil.getMIMEType(file);

        if (mimeType == null) {
            return null;
        }

        LSPBindings bindings =
                project2MimeType2Server.computeIfAbsent(prj, p -> new HashMap<>())
                                       .computeIfAbsent(mimeType, mt -> {
                                           for (LanguageServerProvider provider : MimeLookup.getLookup(mimeType).lookupAll(LanguageServerProvider.class)) {
                                               LanguageServerDescription desc = provider.startServer(Lookups.singleton(prj));

                                               if (desc != null) {
                                                   try {
                                                       LanguageClientImpl lci = new LanguageClientImpl();
                                                       InputStream in = LanguageServerProviderAccessor.getINSTANCE().getInputStream(desc);
                                                       OutputStream out = LanguageServerProviderAccessor.getINSTANCE().getOutputStream(desc);
                                                       Launcher<LanguageServer> launcher = LSPLauncher.createClientLauncher(lci, in, out);
                                                       launcher.startListening();
                                                       LanguageServer server = launcher.getRemoteProxy();
                                                       InitializeResult result = initServer(server, prj.getProjectDirectory()); //XXX: what if a different root is expected????
                                                       LSPBindings b = new LSPBindings(server, result, LanguageServerProviderAccessor.getINSTANCE().getProcess(desc));
                                                       lci.setBindings(b);
                                                       return b;
                                                   } catch (InterruptedException | ExecutionException ex) {
                                                       LOG.log(Level.WARNING, null, ex);
                                                   }
                                               }
                                           }
                                           return new LSPBindings(null, null, null);
                                       });

        if (bindings.process != null && !bindings.process.isAlive()) {
            //XXX: what now
            return null;
        }
        return bindings.server != null ? bindings : null;
    }
    private static final Logger LOG = Logger.getLogger(LSPBindings.class.getName());

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
                InitializeResult result = initServer(server, root);
                LSPBindings bindings = new LSPBindings(server, result, null);

                lc.setBindings(bindings);

                workspace2Extension2Server.put(root, Arrays.stream(extensions).collect(Collectors.toMap(k -> k, v -> bindings)));
            } catch (InterruptedException | ExecutionException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }, Bundle.LBL_Connecting());
    }

    private static InitializeResult initServer(LanguageServer server, FileObject root) throws InterruptedException, ExecutionException {
       InitializeParams initParams = new InitializeParams();
       initParams.setRootUri(Utils.toURI(root));
       initParams.setRootPath(FileUtil.toFile(root).getAbsolutePath()); //some servers still expect root path
       initParams.setProcessId(0);
       TextDocumentClientCapabilities tdcc = new TextDocumentClientCapabilities();
       DocumentSymbolCapabilities dsc = new DocumentSymbolCapabilities();
       dsc.setHierarchicalDocumentSymbolSupport(true);
       tdcc.setDocumentSymbol(dsc);
       WorkspaceClientCapabilities wcc = new WorkspaceClientCapabilities();
       wcc.setWorkspaceEdit(new WorkspaceEditCapabilities());
       wcc.getWorkspaceEdit().setDocumentChanges(true);
       wcc.getWorkspaceEdit().setResourceOperations(Arrays.asList(ResourceOperationKind.Create, ResourceOperationKind.Delete, ResourceOperationKind.Rename));
       initParams.setCapabilities(new ClientCapabilities(wcc, tdcc, null));
       return server.initialize(initParams).get();
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

    public static void addBackgroundTask(FileObject file, BackgroundTask task) {
        LSPBindings bindings = getBindings(file);

        if (bindings == null)
            return ;

        RequestProcessor.Task req = WORKER.create(() -> task.run(bindings, file));

        bindings.backgroundTasks.computeIfAbsent(file, f -> new LinkedHashMap<>()).put(task, req);
        bindings.scheduleBackgroundTask(req);
    }

    public static void removeBackgroundTask(FileObject file, BackgroundTask task) {
        LSPBindings bindings = getBindings(file);

        if (bindings == null)
            return ;

        RequestProcessor.Task req = bindings.backgroundTasks.computeIfAbsent(file, f -> new LinkedHashMap<>()).remove(task);

        if (req != null) {
            req.cancel();
        }
    }

    public void runOnBackground(Runnable r) {
        WORKER.post(r);
    }

    public void scheduleBackgroundTask(RequestProcessor.Task req) {
        WORKER.post(req, DELAY);
    }

    public void scheduleBackgroundTasks(FileObject file) {
        backgroundTasks.computeIfAbsent(file, f -> new IdentityHashMap<>()).values().stream().forEach(this::scheduleBackgroundTask);
    }

    public interface BackgroundTask {
        public void run(LSPBindings bindings, FileObject file);
    }

    @OnStop
    public static class Cleanup implements Runnable {

        @Override
        public void run() {
            for (Map<String, LSPBindings> mime2Bindings : project2MimeType2Server.values()) {
                for (LSPBindings b : mime2Bindings.values()) {
                    if (b != null && b.process != null) {
                        b.process.destroy();
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
