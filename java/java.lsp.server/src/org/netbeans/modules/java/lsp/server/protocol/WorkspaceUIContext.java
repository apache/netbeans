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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
import org.netbeans.modules.java.lsp.server.input.ShowQuickPickParams;
import org.netbeans.modules.java.lsp.server.input.ShowInputBoxParams;
import org.netbeans.modules.java.lsp.server.input.ShowMutliStepInputParams;
import org.openide.awt.StatusDisplayer;

/**
 *
 * @author sdedic
 */
class WorkspaceUIContext extends UIContext {
    private static final Logger LOG = Logger.getLogger(WorkspaceUIContext.class.getName());
    
    private final NbCodeLanguageClient client;

    public WorkspaceUIContext(NbCodeLanguageClient client) {
        this.client = client;
        LOG.log(Level.FINE, "Starting WorkspaceUIContext for: {0}, context instance: {1}", new Object[] { client, this });
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams msg) {
        return client.showMessageRequest(msg);
    }

    @Override
    public void showMessage(MessageParams msg) {
        client.showMessage(msg);
    }

    @Override
    public CompletableFuture<String> showInputBox(ShowInputBoxParams params) {
        return client.showInputBox(params);
    }

    @Override
    public CompletableFuture<List<QuickPickItem>> showQuickPick(ShowQuickPickParams params) {
        return client.showQuickPick(params);
    }

    @Override
    public CompletableFuture<Map<String, Either<List<QuickPickItem>, String>>> showMultiStepInput(ShowMutliStepInputParams params) {
        return client.showMultiStepInput(params);
    }

    @Override
    public void logMessage(MessageParams msg) {
        client.logMessage(msg);
    }

    @Override
    public StatusDisplayer.Message showStatusMessage(ShowStatusMessageParams msg) {
        if (client.getNbCodeCapabilities().hasStatusBarMessageSupport()) {
            client.showStatusBarMessage(msg);
        } else {
            client.showMessage(msg);
        }
        return null;
    }

    @Override
    public CompletableFuture<String> showHtmlPage(HtmlPageParams msg) {
        return client.showHtmlPage(msg);
    }

    public final NbCodeClientCapabilities getNbCodeCapabilities() {
        return client.getNbCodeCapabilities();
    }
}
