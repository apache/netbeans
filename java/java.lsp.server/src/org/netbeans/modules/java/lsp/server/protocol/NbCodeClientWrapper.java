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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.SemanticHighlightingParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.UnregistrationParams;
import org.eclipse.lsp4j.WorkspaceFolder;

/**
 * Convenience wrapper that binds language client's remote proxy together with
 * other useful methods. Will be sent out as THE client by the server core code.
 * 
 * @author sdedic
 */
class NbCodeClientWrapper implements NbCodeLanguageClient {
    private final NbCodeLanguageClient remote;
    private volatile NbCodeClientCapabilities  clientCaps;

    public NbCodeClientWrapper(NbCodeLanguageClient remote) {
        this.remote = remote;
        this.clientCaps = new NbCodeClientCapabilities();
    }

    public void setClientCaps(NbCodeClientCapabilities clientCaps) {
        if (clientCaps != null) {
            this.clientCaps = clientCaps;
        }
    }
    
    @Override
    public NbCodeClientCapabilities getNbCodeCapabilities() {
        return clientCaps;
    }

    @Override
    public void showStatusBarMessage(ShowStatusMessageParams params) {
        remote.showStatusBarMessage(params);
    }

    @Override
    public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
        return remote.applyEdit(params);
    }

    @Override
    public CompletableFuture<Void> registerCapability(RegistrationParams params) {
        return remote.registerCapability(params);
    }

    @Override
    public CompletableFuture<Void> unregisterCapability(UnregistrationParams params) {
        return remote.unregisterCapability(params);
    }

    @Override
    public void telemetryEvent(Object object) {
        remote.telemetryEvent(object);
    }

    @Override
    public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
        remote.publishDiagnostics(diagnostics);
    }

    @Override
    public void showMessage(MessageParams messageParams) {
        remote.showMessage(messageParams);
    }

    @Override
    public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
        return remote.showMessageRequest(requestParams);
    }

    @Override
    public void logMessage(MessageParams message) {
        remote.logMessage(message);
    }

    @Override
    public CompletableFuture<List<WorkspaceFolder>> workspaceFolders() {
        return remote.workspaceFolders();
    }

    @Override
    public CompletableFuture<List<Object>> configuration(ConfigurationParams configurationParams) {
        return remote.configuration(configurationParams);
    }

    @Override
    public void semanticHighlighting(SemanticHighlightingParams params) {
        remote.semanticHighlighting(params);
    }
}
