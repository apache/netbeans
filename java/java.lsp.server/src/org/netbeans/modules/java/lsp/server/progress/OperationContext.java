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
package org.netbeans.modules.java.lsp.server.progress;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.api.progress.ProgressHandle;
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
    private Either<String, Integer> progressToken;
    
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
    
    /**
     * Listeners of InternalHandle operations. 
     */
    private final List<LR> operationListeners = new ArrayList<>();
    
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
    public synchronized Either<String, Integer> acquireProgressToken() {
        Either<String, Integer> t = progressToken;
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
    public void setProgressToken(Either<String, Integer> s) {
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
    public InternalHandle  findActiveHandle(Either<String, Integer> token) {
        if (top != this) {
            return top.findActiveHandle(token);
        }
        synchronized (this) {
            return handles.get(token);
        }
    }
    
    private Either<String, Integer> addHandle(Either<String, Integer> token, InternalHandle h) {
        top.registerHandle(token, h);
        return token;
    }
    
    void removeHandle(Either<String, Integer> token, LspInternalHandle h) {
        if (top != this) {
            top.unregisterHandle(token, h);
        } else {
            unregisterHandle(token, h);
        }
        notifyHandleFinished(h);
    }
    
    private Map<Either<String, Integer>, InternalHandle> handles = new HashMap<>();
    
    private synchronized void unregisterHandle(Either<String, Integer> token, InternalHandle h) {
        if (token == null) {
            handles.values().remove(h);
        } else {
            handles.remove(token);
        }
    }
    
    private synchronized void registerHandle(Either<String, Integer> token, InternalHandle h) {
        handles.put(token, h);
    }
    
    CompletableFuture<Either<String, Integer>> acquireOrObtainToken(InternalHandle h) {
        Either<String, Integer> t = acquireProgressToken();
        if (t != null) {
            return CompletableFuture.completedFuture(addHandle(t, h));
        } else {
            WorkDoneProgressCreateParams params = new WorkDoneProgressCreateParams(
                Either.forLeft(UUID.randomUUID().toString())
            );
            CompletableFuture<Either<String, Integer>> tokenPromise = client.
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
    
    void internalHandleCreated(LspInternalHandle h) {
        synchronized (this) {
            createdHandles.add(h);
        }
        dispatchProgressEvent(h, ProgressOperationListener::progressHandleCreated);
    }
    
    void notifyHandleFinished(LspInternalHandle h) {
        dispatchProgressEvent(h, ProgressOperationListener::progressHandleFinished);
    }

    private static class LR implements Predicate<String> {
        private final String regexp;
        private final ProgressOperationListener listener;
        private final Pattern compiled;

        public LR(String regexp, ProgressOperationListener listener) {
            this.compiled = regexp == null ? null : Pattern.compile(regexp);
            this.regexp = regexp;
            this.listener = listener;
        }

        @Override
        public boolean test(String t) {
            if (compiled == null) {
                return false;
            }
            return compiled.matcher(t).matches();
        }
    }
    
    /**
     * Adds a listener that will be informed about {@link ProgressHandle}s created
     * on behalf of the server call represented by this instance.
     * The called code may fork a thread, that will produce a {@link ProgressHandle} later,
     * after the server call completes and returns. If the forked thread preserves the Lookup
     * for the forked thread, this OperationContext will be looked up and the listener will
     * be informed when a handle is created, or finishes.
     * <p>
     * The caller may optionally specify a class filter; the listener will be called only if
     * the creation call stack contains a class that satisfies the filter. The filter string
     * is interpreted as a regular expression matched against class FQNs. {@code null} means
     * to accept all progresses.
     * 
     * @param originClassFilter 
     * @param ol listener instance
     */
    public void addProgressOperationListener(String originClassFilter, ProgressOperationListener ol) {
        synchronized (this) {
            operationListeners.add(new LR(originClassFilter, ol));
        }
    }
    
    public void removeProgressOperationListener(ProgressOperationListener ol) {
        synchronized (this) {
            for (Iterator<LR> it = operationListeners.iterator(); it.hasNext();) {
                LR r = it.next();
                if (r.listener == ol) {
                    it.remove();
                }
            }
        }
    }
    
    public void removeProgressOperationListener(String originClassFilter, ProgressOperationListener ol) {
        synchronized (this) {
            for (LR r : operationListeners) {
                if (Objects.equals(r.regexp, originClassFilter) && r.listener == ol) {
                    operationListeners.remove(r);
                    return;
                }
            }
        }
    }
    
    private void dispatchProgressEvent(LspInternalHandle h, BiConsumer<ProgressOperationListener, ProgressOperationEvent> m) {
        Collection<ProgressOperationListener> ll = new LinkedHashSet<>();
        synchronized (this) {
            FRAME: for (StackTraceElement ste : h.getCreatorTrace()) {
                String cn = ste.getClassName();
                for (LR reg : operationListeners) {
                    if (reg.test(cn)) {
                        ll.add(reg.listener);
                    }
                }
                for (LR reg : operationListeners) {
                    if (reg.regexp == null) {
                        ll.add(reg.listener);
                    }
                }
            }
        }
        if (ll.isEmpty()) {
            return;
        }
        ProgressOperationEvent ev = new ProgressOperationEvent(h, this);
        for (ProgressOperationListener l : ll) {
            m.accept(l, ev);
        }
    }
}
