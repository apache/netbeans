/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.lsp.server.debugging.attach;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;

import org.netbeans.modules.java.lsp.server.debugging.attach.Processes.ProcessInfo;
import org.netbeans.modules.java.lsp.server.debugging.utils.ErrorUtilities;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
import org.netbeans.modules.java.lsp.server.input.ShowQuickPickParams;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Native Image Debugger attach configurations provider.
 *
 * @author Martin Entlicher
 */
public final class AttachNativeConfigurations {

    static final String CONFIG_TYPE = "nativeimage";    // NOI18N
    static final String CONFIG_REQUEST = "attach";      // NOI18N

    static final RequestProcessor RP = new RequestProcessor(AttachNativeConfigurations.class);

    public static CompletableFuture<Object> findProcessAttachTo(NbCodeLanguageClient client) {
        return CompletableFuture.supplyAsync(() -> {
            return listProcessesToAttachTo(client);
        }, RP).thenCompose(params -> client.showQuickPick(params)).thenApply(itemsList -> {
            if (itemsList == null || itemsList.isEmpty()) {
                return null;
            } else {
                return itemsList.get(0).getUserData();
            }
        });
    }

    @NbBundle.Messages("LBL_PickNativeProcessAttach=Pick Native Image process to attach to:")
    private static ShowQuickPickParams listProcessesToAttachTo(NbCodeLanguageClient client) {
        List<QuickPickItem> items = new ArrayList<>();
        List<ProcessInfo> processes = Processes.getAllProcesses();
        for (ProcessInfo info : processes) {
            items.add(createQuickPickItem(info));
        }
        if (items.isEmpty()) {
            throw ErrorUtilities.createResponseErrorException("No debuggable native process found.", ResponseErrorCode.RequestCancelled);  // NOI18N
        }
        return new ShowQuickPickParams(Bundle.LBL_PickNativeProcessAttach(), items);
    }

    private static QuickPickItem createQuickPickItem(ProcessInfo info) {
        String label = Long.toString(info.getPid());
        String description = info.getCommand();
        Object userData = info.getPid() + " " + info.getExecutable();
        return new QuickPickItem(label, description, null, false, userData);
    }

}
