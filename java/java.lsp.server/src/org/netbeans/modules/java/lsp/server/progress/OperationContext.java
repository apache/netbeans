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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.progress.spi.Controller;
import org.netbeans.modules.progress.spi.InternalHandle;
import org.netbeans.modules.progress.spi.TaskModel;
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
    
    /**
     * The controller that collects tasks in progress.
     */
    private final Controller  progressController;
    
    /**
     * Handles created during the operation.
     */
    private final List<InternalHandle>  createdHandles = new ArrayList<>();
    
    private boolean finished;
    
    private final OperationContext top;
    
    private boolean disableCancels;
    
    OperationContext(OperationContext top, NbCodeLanguageClient client, Controller controller) {
        this.client = client;
        this.progressController = controller;
        this.top = top == null ? this : top;
    }
    
    private TaskModel getTaskModel() {
        return progressController.getModel();
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
    
    public OperationContext operationContext() {
        OperationContext ctx = new OperationContext(this, client, progressController);
        lastCtx = new WeakReference<>(ctx);
        return ctx;
    }
    
    public static synchronized OperationContext create(NbCodeLanguageClient client) {
        OperationContext ctx = new OperationContext(null, client, 
            new Controller(new LspProgressUIWorker()));
        lastCtx = new WeakReference<>(ctx);
        ctx.registerInitialContext();
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
                ctx = create(lkp.lookup(NbCodeLanguageClient.class));
            }
        }
        lastCtx = new WeakReference<>(ctx);
        return ctx;
    }
    
    public void stop() {
        acquireProgressToken();
        if (!isActive()) {
            return;
        }
        if (this != top) {
            finished = true;
        }
    }
    
    public boolean isActive() {
        return Lookup.getDefault().lookup(OperationContext.class) == this;
    }
    
    /**
     * Finds an active handle identified by client's progress token.
     * @param token token
     * @return handle instance or {@code null}
     */
    public InternalHandle  findActiveHandle(Either<String, Number> token) {
        if (top != this) {
            return top.findActiveHandle(token);
        }
        synchronized (this) {
            return handles.get(token);
        }
    }
    
    private Either<String, Number> addHandle(Either<String, Number> token, InternalHandle h) {
        top.registerHandle(token, h);
        return token;
    }
    
    void removeHandle(Either<String, Number> token, InternalHandle h) {
        if (top != this) {
            top.unregisterHandle(token, h);
        } else {
            unregisterHandle(token, h);
        }
    }
    
    private Map<Either<String, Number>, InternalHandle> handles = new HashMap<>();
    
    private synchronized void unregisterHandle(Either<String, Number> token, InternalHandle h) {
        if (token == null) {
            handles.values().remove(h);
        } else {
            handles.remove(token);
        }
    }
    
    private synchronized void registerHandle(Either<String, Number> token, InternalHandle h) {
        handles.put(token, h);
    }
    
    CompletableFuture<Either<String, Number>> acquireOrObtainToken(InternalHandle h) {
        Either<String, Number> t = acquireProgressToken();
        if (t != null) {
            return CompletableFuture.completedFuture(addHandle(t, h));
        } else {
            WorkDoneProgressCreateParams params = new WorkDoneProgressCreateParams(
                Either.forLeft(UUID.randomUUID().toString())
            );
            CompletableFuture<Either<String, Number>> tokenPromise = client.
                    createProgress(params).thenApply(v -> params.getToken());
            return tokenPromise.thenApply((p) -> {
               synchronized (this) {
                   return addHandle(p, h);
               } 
            });
        }
    }
    
    public void disableCancels() {
        if (top != this) {
            disableCancels = true;
        }
    }

    public boolean isDisableCancels() {
        return disableCancels;
    }
    
    public static OperationContext getHandleContext(InternalHandle h) {
        if (!(h instanceof LspInternalHandle)) {
            return null;
        }
        return ((LspInternalHandle)h).getContext();
    }
    
    public Collection<InternalHandle> getAllActiveHandles() {
        if (top != this) {
            return top.getAllActiveHandles();
        }
        synchronized (this) {
            return new ArrayList<>(handles.values());
        }
    }
    
    public synchronized List<InternalHandle> getOperationHandles() {
        return new ArrayList<>(createdHandles);
    }
    
    void internalHandleCreated(InternalHandle h) {
        synchronized (this) {
            createdHandles.add(h);
        }
    }
}
