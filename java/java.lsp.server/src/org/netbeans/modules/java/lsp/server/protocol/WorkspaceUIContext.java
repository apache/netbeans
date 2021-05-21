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

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.netbeans.modules.java.lsp.server.ui.UIContext;
import org.openide.awt.StatusDisplayer;

/**
 *
 * @author sdedic
 */
class WorkspaceUIContext extends UIContext {
    private final NbCodeLanguageClient client;

    public WorkspaceUIContext(NbCodeLanguageClient client) {
        this.client = client;
    }

    @Override
    protected boolean isValid() {
        return true;
    }

    @Override
    protected CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams msg) {
        return client.showMessageRequest(msg);
    }

    @Override
    protected void showMessage(MessageParams msg) {
        client.showMessage(msg);
    }

    @Override
    protected void logMessage(MessageParams msg) {
        client.logMessage(msg);
    }

    @Override
    protected StatusDisplayer.Message showStatusMessage(ShowStatusMessageParams msg) {
        if (client.getNbCodeCapabilities().hasStatusBarMessageSupport()) {
            client.showStatusBarMessage(msg);
        } else {
            client.showMessage(msg);
        }
        return null;
    }
    
}
