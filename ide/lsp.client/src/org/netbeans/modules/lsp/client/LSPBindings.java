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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
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

    public static LSPBindings getBindings(FileObject file) {
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
                                                       InitializeParams initParams = new InitializeParams();
                                                       initParams.setRootUri(prj.getProjectDirectory().toURI().toString()); //XXX: what if a different root is expected????
                                                       initParams.setRootPath(FileUtil.toFile(prj.getProjectDirectory()).getAbsolutePath()); //some servers still expect root path
                                                       initParams.setProcessId(0);
                                                       InitializeResult result = server.initialize(initParams).get();
                                                       LSPBindings b = new LSPBindings(server, result);
                                                       lci.setBindings(b);
                                                       return b;
                                                   } catch (InterruptedException | ExecutionException ex) {
                                                       LOG.log(Level.FINE, null, ex);
                                                   }
                                               }
                                           }
                                           return new LSPBindings(null, null);
                                       });

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

                InitializeParams initParams = new InitializeParams();
                initParams.setRootUri(root.toURI().toString());
                initParams.setProcessId(0);
                InitializeResult result = server.initialize(initParams).get();
                LSPBindings bindings = new LSPBindings(server, result);

                lc.setBindings(bindings);

                workspace2Extension2Server.put(root, Arrays.stream(extensions).collect(Collectors.toMap(k -> k, v -> bindings)));
            } catch (InterruptedException | ExecutionException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }, Bundle.LBL_Connecting());
    }

    private final LanguageServer server;
    private final InitializeResult initResult;

    private LSPBindings(LanguageServer server, InitializeResult initResult) {
        this.server = server;
        this.initResult = initResult;
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
}
