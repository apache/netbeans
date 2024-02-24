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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.modules.java.lsp.server.explorer.api.NodeChangedParams;
import org.netbeans.modules.java.lsp.server.protocol.DecorationRenderOptions;
import org.netbeans.modules.java.lsp.server.protocol.HtmlPageParams;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeClientCapabilities;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
import org.netbeans.modules.java.lsp.server.protocol.SetTextEditorDecorationParams;
import org.netbeans.modules.java.lsp.server.input.ShowInputBoxParams;
import org.netbeans.modules.java.lsp.server.input.ShowMutliStepInputParams;
import org.netbeans.modules.java.lsp.server.input.ShowQuickPickParams;
import org.netbeans.modules.java.lsp.server.protocol.SaveDocumentRequestParams;
import org.netbeans.modules.java.lsp.server.protocol.ShowStatusMessageParams;
import org.netbeans.modules.java.lsp.server.protocol.TestProgressParams;
import org.netbeans.modules.java.lsp.server.protocol.UpdateConfigParams;

public abstract class TestCodeLanguageClient implements NbCodeLanguageClient {

    @Override
    public CompletableFuture<Void> createProgress(WorkDoneProgressCreateParams params) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void notifyProgress(ProgressParams params) {
    }

    @Override
    public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<List<Object>> configuration(ConfigurationParams params) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public void showStatusBarMessage(ShowStatusMessageParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<String> showHtmlPage(HtmlPageParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<String> execInHtmlPage(HtmlPageParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<List<QuickPickItem>> showQuickPick(ShowQuickPickParams params) {
        return CompletableFuture.completedFuture(params.getItems().stream().filter(item -> item.isPicked()).collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<String> showInputBox(ShowInputBoxParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Map<String, Either<List<QuickPickItem>, String>>> showMultiStepInput(ShowMutliStepInputParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void notifyTestProgress(TestProgressParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<String> createTextEditorDecoration(DecorationRenderOptions params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTextEditorDecoration(SetTextEditorDecorationParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disposeTextEditorDecoration(String params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NbCodeClientCapabilities getNbCodeCapabilities() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void telemetryEvent(Object params) {
    }

    @Override
    public void publishDiagnostics(PublishDiagnosticsParams params) {
    }

    @Override
    public void showMessage(MessageParams params) {
    }

    @Override
    public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams params) {
        return CompletableFuture.completedFuture(new MessageActionItem(params.getActions().get(0).getTitle()));
    }

    @Override
    public void logMessage(MessageParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void notifyNodeChange(NodeChangedParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Void> configurationUpdate(UpdateConfigParams params) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public CompletableFuture<Boolean> requestDocumentSave(SaveDocumentRequestParams documentUris) {
        return CompletableFuture.completedFuture(false);
    }
}
