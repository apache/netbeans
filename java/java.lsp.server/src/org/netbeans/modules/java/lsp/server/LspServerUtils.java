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
package org.netbeans.modules.java.lsp.server;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.protocol.Server;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public class LspServerUtils {
    
    /**
     * Locates the client associated with the current context. Use this method as a
     * last resort, for testing & all other practical purposes it is always better to
     * have own LSP client reference or a context Lookup instance.
     * @param context the processing context.
     * @return LanguageClient instance or {@code null}, if no client associated with the context or thread.
     */
    @CheckForNull
    public static final NbCodeLanguageClient findLspClient(Lookup context) {
        NbCodeLanguageClient client = context != null ? context.lookup(NbCodeLanguageClient.class) : null;
        if (client == null && context != Lookup.getDefault()) {
            client = Lookup.getDefault().lookup(NbCodeLanguageClient.class);
        }
        return client;
    }

    /**
     * Locates the client associated with the current context. Use this method as a
     * last resort, for testing & all other practical purposes it is always better to
     * have own LSP client reference or a context Lookup instance.
     * <p>
     * This method always return a client, but if no real context is given, the client returns
     * just stub values and logs all method calls as warnings.
     * 
     * @param context the processing context.
     * @return LanguageClient instance, never null.
     */
    @NonNull
    public static final NbCodeLanguageClient requireLspClient(Lookup context) {
        NbCodeLanguageClient client = findLspClient(context);
        return client != null ? client : Server.getStubClient();
    }
    
    /**
     * Checks whether the calling thread is the one serving client's communication.
     * Such thread cannot be blocked. If {@code null} is passed, returns {@code true}
     * if the calling thread serves any client.
     * 
     * @param client client instance or {@code null}.
     * @return true, if communication with the client would be broken
     */
    public static final boolean isClientResponseThread(NbCodeLanguageClient client) {
        return Server.isClientResponseThread(client);
    }
    
    /**
     * Ensures that the caller does not serve the client associated with the context. If it does,
     * throws IllegalStateException. Call to avoid blocking communication with the client before
     * waiting on some remote response.
     * 
     * @param context execution context
     */
    public static final void avoidClientMessageThread(Lookup context) {
        NbCodeLanguageClient client = LspServerUtils.findLspClient(context);
        if (LspServerUtils.isClientResponseThread(client)) {
            throw new IllegalStateException("Cannot block LSP server message loop. Use RequestProcessor to run the calling code, or use notifyLater()");
        }
    }
}
