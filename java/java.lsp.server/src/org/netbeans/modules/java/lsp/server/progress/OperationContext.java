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
package org.netbeans.modules.java.lsp.server.progress;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.openide.util.Lookup;

/**
 * Operation context for reporting progress. Initially the instance is
 * request-scoped in the initial operation request. Since it becomes part of
 * the default Lookup, it should be handed  into any RequestProcessor-forked
 * tasks initiated from the operation request.
 * 
 * @author sdedic
 */
public final class OperationContext {
    private static Reference<OperationContext> lastCtx = new WeakReference<>(null);
    
    /**
     * Initial context
     */
    private static Reference<OperationContext> initialCtx = new WeakReference<>(null);
    
    /**
     * LSP client that issues progress tokens.
     */
    private final NbCodeLanguageClient  client;
    
    /**
     * Progress token for reporting progress to the client. The token is valid
     * for a single ProgressHandle created.
     */
    private Either<String, Number> progressToken;
    
    /**
     * Token to report partial results to the client.
     */
    private String partialResultsToken;
    
    OperationContext(NbCodeLanguageClient client) {
        this.client = client;
    }
    
    /**
     * Acquires a progress token. The token is either issued by the client,
     * from its original operation request, or the server will initiate a 
     * progress on the client, asking it to issue a token.
     * @return 
     */
    public synchronized Either<String, Number> acquireProgressToken() {
        Either<String, Number> t = progressToken;
        progressToken = null;
        return t;
    }
    
    public String getPartialResultsToken() {
        return partialResultsToken;
    }
    
    /**
     * INTERNAL. Do not call this method; it will be likely removed
     * soon.
     * @param s 
     */
    public void setProgressToken(Either<String, Number> s) {
        progressToken = s;
    }
    
    public static synchronized OperationContext create(Lookup lkp, boolean initial) {
        NbCodeLanguageClient client = lkp.lookup(NbCodeLanguageClient.class);
        OperationContext ctx = new OperationContext(client);
        lastCtx = new WeakReference<>(ctx);
        if (initial) {
            ctx.registerInitialContext();
        }
        return ctx;
    }
    
    void registerInitialContext() {
        initialCtx = new WeakReference<>(this);
    }

    public NbCodeLanguageClient getClient() {
        return client;
    }
    
    public static synchronized OperationContext find(Lookup lkp) {
        OperationContext ctx;
        Lookup def = Lookup.getDefault();
        if (lkp == null) {
            lkp = def;
        }
        ctx = lkp.lookup(OperationContext.class);
        if (ctx != null) {
            return ctx;
        }
        ctx = lastCtx.get();
        if (ctx == null)  {
            ctx = initialCtx.get();
            if (ctx == null) {
                ctx = new OperationContext(lkp.lookup(NbCodeLanguageClient.class));
            }
        }
        lastCtx = new WeakReference<>(ctx);
        return ctx;
    }
}
