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
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.lsp4j.services.LanguageClient;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
import org.netbeans.modules.java.lsp.server.input.ShowQuickPickParams;
import org.netbeans.modules.java.lsp.server.input.ShowMutliStepInputParams;
import org.netbeans.modules.java.lsp.server.input.ShowInputBoxParams;
import org.netbeans.modules.java.lsp.server.explorer.api.NodeChangedParams;

/**
 * An extension to the standard LanguageClient that adds several messages missing
 * from the official LSP protocol.s
 * @author sdedic
 */
public interface NbCodeLanguageClient extends LanguageClient {

    /**
     * Shows a message in the status bar. Log- and Info-type messages are shown "as is".
     * The other message types can be decorated by an icon according to {@link MessageParams#getType}.
     * The message will be hidden after specified number of milliseconds; 0 means the client
     * controls when the message is hidden.
     *
     * @param params message type and text.
     */
    @JsonNotification("window/showStatusBarMessage")
    public void showStatusBarMessage(@NonNull ShowStatusMessageParams params);
    /**
     * Shows an HTML based UI.
     * @param params the page to show
     */
    @JsonRequest("window/showHtmlPage")
    public CompletableFuture<String> showHtmlPage(@NonNull HtmlPageParams params);

    /**
     * Execute script in an HTML based UI.
     * @param params the script to execute
     */
    @JsonRequest("window/execInHtmlPage")
    public CompletableFuture<String> execInHtmlPage(@NonNull HtmlPageParams params);

    /**
     * Shows a selection list allowing multiple selections.
     *
     * @param params input parameters
     * @return selected items
     */
    @JsonRequest("window/showQuickPick")
    public CompletableFuture<List<QuickPickItem>> showQuickPick(@NonNull ShowQuickPickParams params);

    /**
     * Shows an input box to ask the user for input.
     *
     * @param params input parameters
     * @return input value
     */
    @JsonRequest("window/showInputBox")
    public CompletableFuture<String> showInputBox(@NonNull ShowInputBoxParams params);

    /**
     * Shows a mutli-step input using QuickPicks and InputBoxes.
     *
     * @param params input parameters
     * @return collected input values and selected items
     */
    @JsonRequest("window/showMultiStepInput")
    public CompletableFuture<Map<String, Either<List<QuickPickItem>, String>>> showMultiStepInput(@NonNull ShowMutliStepInputParams params);

    /**
     * Notifies client of running tests progress. Provides information about a test suite being loaded,
     * started, completed or skipped during a test run.
     *
     * @param params test run information
     */
    @JsonNotification("window/notifyTestProgress")
    public void notifyTestProgress(@NonNull TestProgressParams params);

    /**
     * Create a text editor decoration.
     *
     * @param params the decoration render options
     * @return a key of the created decoration
     */
    @JsonRequest("window/createTextEditorDecoration")
    public CompletableFuture<String> createTextEditorDecoration(@NonNull DecorationRenderOptions params);

    /**
     * Set text editor decoration to an array of code ranges.
     *
     * @param params
     */
    @JsonNotification("window/setTextEditorDecoration")
    public void setTextEditorDecoration(@NonNull SetTextEditorDecorationParams params);

    /**
     * Notifies client about disposal of the text editor decoration.
     *
     * @param params the decoration key
     */
    @JsonNotification("window/disposeTextEditorDecoration")
    public void disposeTextEditorDecoration(@NonNull String params);

    /**
     * Notifies client about change in a node.
     *
     * @param params the id of the node
     */
    @JsonNotification("nodes/nodeChanged")
    public void notifyNodeChange(@NonNull NodeChangedParams params);

    /**
     * Returns extended code capabilities.
     * @return code capabilities.
     */
    public NbCodeClientCapabilities getNbCodeCapabilities();

    public default boolean isRequestDispatcherThread() {
        return Boolean.TRUE.equals(Server.DISPATCHERS.get());
    }
    
    /**
     * Update a configuration value. The updated configuration values are persisted.
     * 
     * @param params configuration update information.
     */
    @JsonRequest("config/update")
    public CompletableFuture<Void> configurationUpdate(@NonNull UpdateConfigParams params);
    
    @JsonRequest("window/documentSave")
    public CompletableFuture<Boolean> requestDocumentSave(@NonNull SaveDocumentRequestParams documentUri);
    
}
