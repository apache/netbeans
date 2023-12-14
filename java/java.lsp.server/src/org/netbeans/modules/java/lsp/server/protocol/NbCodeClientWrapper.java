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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.LogTraceParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.ShowDocumentParams;
import org.eclipse.lsp4j.ShowDocumentResult;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.UnregistrationParams;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
import org.netbeans.modules.java.lsp.server.input.ShowQuickPickParams;
import org.netbeans.modules.java.lsp.server.input.ShowMutliStepInputParams;
import org.netbeans.modules.java.lsp.server.input.ShowInputBoxParams;
import org.netbeans.modules.java.lsp.server.explorer.api.NodeChangedParams;

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
    public CompletableFuture<String> showHtmlPage(HtmlPageParams params) {
        return remote.showHtmlPage(params);
    }

    @Override
    public CompletableFuture<String> execInHtmlPage(HtmlPageParams params) {
        return remote.execInHtmlPage(params);
    }

    @Override
    public CompletableFuture<List<QuickPickItem>> showQuickPick(ShowQuickPickParams params) {
        // vscode from version 1.80.2 displays control characters in quickpicks. Let's strip them:
        ShowQuickPickParams copy = new ShowQuickPickParams(
                params.getTitle(), params.getPlaceHolder(), params.getCanPickMany(),
                params.getItems().stream().map(
                        i -> new QuickPickItem(
                                i.getLabel(), Utils.html2plain(i.getDescription(), true), Utils.html2plain(i.getDetail(), true), 
                                i.isPicked(), i.getUserData())
                        ).collect(Collectors.toList())
        );
        return remote.showQuickPick(copy);
    }

    @Override
    public CompletableFuture<String> showInputBox(ShowInputBoxParams params) {
        return remote.showInputBox(params);
    }

    @Override
    public CompletableFuture<Map<String, Either<List<QuickPickItem>, String>>> showMultiStepInput(ShowMutliStepInputParams params) {
        return remote.showMultiStepInput(params);
    }

    @Override
    public void notifyTestProgress(TestProgressParams params) {
        remote.notifyTestProgress(params);
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
    public CompletableFuture<ShowDocumentResult> showDocument(ShowDocumentParams params) {
        return remote.showDocument(params);
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
    public CompletableFuture<Void> createProgress(WorkDoneProgressCreateParams params) {
        return remote.createProgress(params);
    }

    @Override
    public void notifyProgress(ProgressParams params) {
        remote.notifyProgress(params);
    }

    @Override
    public CompletableFuture<String> createTextEditorDecoration(DecorationRenderOptions params) {
        return remote.createTextEditorDecoration(params);
    }

    @Override
    public void setTextEditorDecoration(SetTextEditorDecorationParams params) {
        remote.setTextEditorDecoration(params);
    }

    @Override
    public void disposeTextEditorDecoration(String params) {
        remote.disposeTextEditorDecoration(params);
    }

    @Override
    public void logTrace(LogTraceParams params) {
        remote.logTrace(params);
    }

    @Override
    public CompletableFuture<Void> refreshSemanticTokens() {
        return remote.refreshSemanticTokens();
    }

    @Override
    public CompletableFuture<Void> refreshCodeLenses() {
        return remote.refreshCodeLenses();
    }
    
    public void notifyNodeChange(NodeChangedParams params) {
        remote.notifyNodeChange(params);
    }
    
    @Override
    public CompletableFuture<Void> configurationUpdate(UpdateConfigParams params) {
        return remote.configurationUpdate(params);
    }
    
    @Override
    public CompletableFuture<Boolean> requestDocumentSave(SaveDocumentRequestParams documentUris) {
        return remote.requestDocumentSave(documentUris);
    }
}
