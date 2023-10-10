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

import java.util.concurrent.Future;

import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.netbeans.modules.java.lsp.server.LspSession;
import org.netbeans.modules.java.lsp.server.files.OpenedDocuments;
import org.openide.util.Lookup;

/**
 * Representation of LSP server, which is available through {@link LspSession}.
 *
 * @author martin
 */
public final class NbLspServer implements LspSession.ScheduledServer {

    private final Server.LanguageServerImpl impl;
    private final Future<Void> runningFuture;

    NbLspServer(Server.LanguageServerImpl impl, Future<Void> runningFuture) {
        this.impl = impl;
        this.runningFuture = runningFuture;
    }

    @Override
    public Lookup getServerLookup() {
        return impl.getSessionOnlyLookup();
    }

    public TextDocumentService getTextDocumentService() {
        return impl.getTextDocumentService();
    }

    public WorkspaceService getWorkspaceService() {
        return impl.getWorkspaceService();
    }

    @Override
    public Future<Void> getRunningFuture() {
        return runningFuture;
    }

    public OpenedDocuments getOpenedDocuments() {
        return impl.getOpenedDocuments();
    }
}
