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
package org.netbeans.modules.project.dependency.reload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.event.ChangeEvent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.dependency.ProjectOperationException;
import org.netbeans.modules.project.dependency.ProjectReload;
import org.netbeans.modules.project.dependency.ProjectReload.ProjectState;
import org.netbeans.modules.project.dependency.ProjectReload.StateRequest;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation.ExtendedQuery;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation.LoadContext;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation.ProjectStateData;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 * Reloader orchestrates the project loading itself. The code was factored out
 * from ProjectReloadInternal to make the class more manageable.
 *
 * The Reloader first creates a {@link LoaderContextImpl} for each of the
 * {@link ProjectReloadImplementation}. These objects will communicate with the
 * implementation, and will be reset before each operation (why they are not
 * recycled ??)
 *
 *
 */
public final class Reloader {

    static final Logger LOG = Logger.getLogger(Reloader.class.getName());

    final Project project;
    final StateRequest request;
    final ProjectState originalState;
    final Throwable originTrace;

    /**
     * Future that completes when the reload completes, either normally or
     * exceptionally. It is used to reuse a pending request.
     */
    final CompletableFuture<ProjectReload.ProjectState> completePending = new CompletableFuture<>();

    final CompletableFuture<ProjectReload.ProjectState> clientFuture;

    private final Collection<LoadContextImpl> loadData = new ArrayList<>();
    private final ProjectReloadInternal registry;

    /**
     * Key of the loaded varint
     */
    private final Collection variantKey;

    /**
     * Initialized from ProjectReloadInternal, when scheduled.
     */
    private RequestProcessor reloadProcessor;
    private Executor reloadExecutor;

    /**
     * CancellationException from the cancelled (external) future which will be
     * thrown from ProjectOperationException.
     */
    private volatile Throwable cancelled;
    /**
     * The currently executing load, for possible cancellation of the process.
     */
    private volatile CompletableFuture<ProjectReloadImplementation.ProjectStateData<?>> currentStage;

    private volatile LoadContextImpl currentStageContext;

    // @GuardedBy(this)
    private ProjectReloadInternal.StateParts parts;
    private volatile boolean ownCompleted = false;
    private boolean forced;
    private boolean forcedRound;
    private int reloadRound = 1;

    private Iterator<LoadContextImpl> implIter;
    private Collection<LoadContextImpl> lastRetries = Collections.emptyList();
    private Collection<Pair<ProjectReloadImplementation<?>, ProjectReloadImplementation.ProjectStateData>> reportedStates = new ArrayList<>();

    public Reloader(Project p, StateRequest request, ProjectReloadInternal.StateRef currentRef, Collection<? extends ProjectReloadImplementation> impls, final ProjectReloadInternal registry, Throwable origin) {
        this.originTrace = origin;
        this.registry = registry;
        this.project = p;
        this.request = request;
        this.originalState = currentRef == null ? null : currentRef.get();
        this.forced = request.isForceReload();
        this.variantKey = currentRef == null ? null : currentRef.variantKey;
        /*
        this.completePending.exceptionally(t -> {
            if (t instanceof CompletionException) {
                t = t.getCause();
            }
            if (t instanceof CancellationException) {
                CompletableFuture f;
                synchronized (this) {
                    if (ownCompleted) {
                        return null;
                    }
                    cancelled = t;
                    f = currentStage;
                }
                if (f != null) {
                    f.cancel(true);
                }
            }
            // does not matter, we do not allow anyone to chain.
            return null;
        });
         */
        for (ProjectReloadImplementation impl : impls) {
            loadData.add(new LoadContextImpl(impl, originalState));
        }

        this.clientFuture = completePending.copy();
        this.clientFuture.exceptionally(t -> {
            if (t instanceof CompletionException) {
                t = t.getCause();
            }
            if (t instanceof CancellationException) {
                // if the client cancels the request, try to propagate it
                cancel((CancellationException)t);
            }
            return null;
        });
    }
    
    public Throwable getOriginTrace() {
        return originTrace;
    }

    @Override
    public String toString() {
        return "Reload[" + (variantKey == null ? project : variantKey) + "]@" + Integer.toHexString(System.identityHashCode(this)) + " #" + reloadRound;
    }

    public CompletableFuture<ProjectReload.ProjectState> getPending() {
        return completePending;
    }
    
    /**
     * Wraps a RequestProcessor, and executes commands immediately, if already in the request processor's thread and there's no need to
     * reschedule to other thread.
     */
    static class RPExecutor implements Executor {
        private final RequestProcessor rp;

        public RPExecutor(RequestProcessor rp) {
            this.rp = rp;
        }

        @Override
        public void execute(Runnable command) {
            if (rp.isRequestProcessorThread()) {
                command.run();
            } else {
                rp.post(command);
            }
        }
    }

    /**
     * Context data for each of the loading participants. It accumulates
     * information during the loading proces, and is wrapped by
     * {@link LoadContext} to make a client API.
     */
    public class LoadContextImpl {
        final ProjectReloadImplementation impl;
        final ProjectReloadImplementation.ProjectStateData origData;
        
        // @GuardedBy(this)
        /**
         * The current clientContext. Cleared after load, together with client context's reference to this Impl.
         */
        ProjectReloadImplementation.LoadContext clientContext;
        
        // @GuardedBy(this)
        private Cancellable cancellable;
        // @GuardedBy(this)
        private volatile CancellationException cancel;
        /**
         * Last loaded data. Either origData, or the data loaded in this
         * operation before some implementation requested reload
         */
        ProjectReloadImplementation.ProjectStateData loadedData;
        
        /**
         * Implementation-specific load context
         */
        Object contextData;
        /**
         * True, if the implementation loaded / completed at least once
         */
        boolean loadedOnce;
        /**
         * State parts that will form a partial state.
         */
        ProjectReloadInternal.StateParts parts;
        /**
         * True, if the implmentation has requested a reload THIS ROUND
         */
        volatile boolean reloadRequested;
        /**
         * Error reported from the implementation
         */
        volatile Throwable reloadError;
        /**
         * Partial state requested by the implementation, lazy initialized
         */
        ProjectReload.ProjectState partialState;
        /**
         * Inconsistencies reported by the implementation.
         */
        Set<Class> inconsistencies;

        public LoadContextImpl(ProjectReloadImplementation impl, ProjectReload.ProjectState previousState) {
            this.impl = impl;
            this.origData = previousState == null ? null : ReloadApiAccessor.get().getParts(previousState).get(impl);
            this.loadedData = origData;
        }

        public Project getProject() {
            return project;
        }

        public ProjectReload.StateRequest getRequest() {
            return request;
        }

        public void reinit(ProjectReloadInternal.StateParts parts) {
            ReloadSpiAccessor.get().clear(clientContext);
            this.clientContext = null;
            this.parts = parts;
            this.partialState = null;
            this.reloadRequested = false;
            this.reloadError = null;
            this.inconsistencies = null;
            this.cancel = null;
            this.cancellable = null;
            LOG.log(Level.FINER, "Project {0}: Load context reset for {1}", new Object[]{project, impl});
        }

        public CancellationException getCancelled() {
            return cancel;
        }

        public void setCancellable(Cancellable c) throws CancellationException {
            Throwable t;
            synchronized (this) {
                t = cancel;
                if (t == null) {
                    this.cancellable = c;
                    return;
                }
            }
            if (t instanceof CancellationException) {
                throw (CancellationException) t;
            } else {
                CancellationException e = new CancellationException();
                e.initCause(t);
                throw e;
            }
        }

        public boolean cancel(CancellationException t) {
            Cancellable c;
            synchronized (this) {
                if (cancel != null) {
                    return false;
                }
                cancel = t;
                c = cancellable;
                if (c == null) {
                    return false;
                }
            }
            return c.cancel();
        }

        public ProjectReload.ProjectState getOriginalState() {
            return originalState;
        }

        public ProjectReloadImplementation.ProjectStateData getProjectData() {
            return loadedData;
        }

        public ProjectReloadImplementation.ProjectStateData getOriginalData() {
            return origData;
        }

        public ProjectReload.ProjectState partialStateImpl() {
            if (partialState != null) {
                return partialState;
            }
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "Project {0}: Partial state created for {1}", new Object[]{project, parts.keySet().stream().map(i -> i.getClass().getName()).collect(Collectors.joining(", "))});
            }
            partialState = registry.doCreateState(project, parts, request);
            return partialState;
        }

        /**
         * Record that the reload process should be retried.
         */
        public void retryReloadImpl() {
            this.reloadRequested = true;
            LOG.log(Level.FINE, "Project {0}: Reload requested by {1}", new Object[]{project, impl});
        }

        public void markForReload(Class c) {
            if (inconsistencies == null) {
                inconsistencies = new HashSet<>();
            }
            inconsistencies.add(c);
            LOG.log(Level.FINE, "Project {0}: Inconsistency {1} recorded by {2}", new Object[]{project, c, impl});
        }

    }

    /**
     * Individual parts may become inconsistent during reloads, indicating the
     * reload should happen again. First process parts themselves and if any is
     * invalid/inconsistent, mark them as initiating the reload. Then process
     * potential inconsistent data recorded in states and mark inconsistent
     * their targets + mark initiator as requesting a reload.
     */
    private void markInconsistentParts() {
        for (LoadContextImpl ctx : loadData) {
            ProjectReloadImplementation.ProjectStateData d = ctx.loadedData;
            if (d == null) {
                continue;
            }
            if (!d.isValid() || (!d.isConsistent() && request.isConsistent())) {
                LOG.log(Level.FINE, "{0}: part {1} loaded inconsistent, implies reload", new Object[]{this, d});
                ctx.reloadRequested = true;
            }
        }
        for (LoadContextImpl ctx : loadData) {
            ProjectReloadImplementation.ProjectStateData d = ctx.loadedData;
            Set<Class> inc = ctx.inconsistencies;
            if (inc != null) {
                LOG.log(Level.FINE, "{0}: loader reports inconsistencies", new Object[]{this, inc});
            }
            if (d != null) {
                Set<Class> inc2 = ReloadSpiAccessor.get().getInconsistencies(d);
                if (inc != null) {
                    if (inc2 != null) {
                        inc.addAll(inc2);
                    }
                    LOG.log(Level.FINE, "{0}: part {1} reports inconsistencies", new Object[]{this, inc2});
                } else {
                    inc = inc2;
                }
            }
            if (inc != null && !inc.isEmpty()) {
                for (Class c : inc) {
                    Lookup.Template t = new Lookup.Template<>(c);
                    for (ProjectReloadImplementation.ProjectStateData d2 : parts.values()) {
                        if (d2 == null) {
                            continue;
                        }
                        if (d2.isConsistent() && d2.isValid()) {
                            if (c.isInstance(d2.getProjectData()) || (d2.getLookup() != null && d2.getLookup().lookupItem(t) != null)) {
                                LOG.log(Level.FINE, "{0}: part {1} provides {2}, mark inconsistent and reload", new Object[]{this, d2, c});
                                ctx.reloadRequested = true;
                                d2.fireChanged(false, true);
                            }
                        }
                    }
                }
            }
        }
    }

    @NbBundle.Messages(value = {"# {0} - project name", "# {1} - list of providers in loop", "ERR_ReloadingLoop=Project is reloading repeatedly. See the log for more details.", "# {0} - project name", "ERR_ProjectModifiedWhileLoading=Project {0} has been modified while reloading", "# {0} - project name", "ERR_ProjectQualityLow=Project {0} could not be loaded."})
    private synchronized CompletableFuture<ProjectReload.ProjectState> finishLoadingRound() {
        Collection<LoadContextImpl> retries;
        boolean forceReload = loadData.stream().anyMatch(d -> d.reloadRequested);
        markInconsistentParts();
        retries = loadData.stream().filter(d -> d.reloadRequested).collect(Collectors.toList());
        Throwable error = null;
        LOG.log(Level.FINE, "{0} load round completed.");
        if (!retries.isEmpty()) {
            // let's allow the reload if at least of the last reloaders was 'satisfied' this time.
            if (!lastRetries.isEmpty() && retries.containsAll(lastRetries)) {
                // too bad: we seem to be in a retry cycle. Bail out.
                LOG.log(Level.WARNING, "Project {0} is reloading repetadely. The following provider(s) reload in a loop: {1}", new Object[]{project.getProjectDirectory(), lastRetries});
                // FIXME: create a ProjectState from the data available so far.
                ProjectOperationException ex = new ProjectOperationException(project, ProjectOperationException.State.ERROR, Bundle.ERR_ReloadingLoop(ProjectUtils.getInformation(project).getDisplayName(), lastRetries));
                error = ex;
            } else {
                if (LOG.isLoggable(Level.FINE)) {
                    String s = retries.stream().map(c -> c.impl.getClass().getName()).collect(Collectors.joining(", "));
                    LOG.log(Level.FINE, "{0} reloads again because of {1}", new Object[]{this, s});
                }
                this.lastRetries = retries;
                initRound();
                // unspecific reload: make all providers to reload their data.
                this.forcedRound = forceReload;
                reloadRound++;
                return processOne();
            }
        }
        ProjectReload.ProjectState result;
        try {
            synchronized (this) {
                Collection variant = ProjectReloadInternal.variantKey(project, parts, request.getContext());
                if (this.variantKey != null && !this.variantKey.equals(variant)) {
                    LOG.log(Level.WARNING, "Variant key formed for {0} differs from cache key: {1}/{2}", new Object[]{
                        request, variant, variantKey
                    });
                }
                // state will be created even in the failed case, ensuring that the last-created ProjectStateData will
                // become cached and the old ones will be marked as invalid.
                result = registry.createState(originalState, project, variant, parts, true, request).second();
            }
            // post-check the result's quality and consistency to match the request
            if ((!result.isConsistent()) && request.isConsistent()) {
                LOG.log(Level.INFO, "{0} loaded as inconsistent, failing operation", this);
                ProjectOperationException ex = new ProjectOperationException(project, ProjectOperationException.State.OUT_OF_SYNC, Bundle.ERR_ProjectModifiedWhileLoading(ProjectUtils.getInformation(project).getDisplayName()));
                error = ex;
            } else if (result.getQuality().isWorseThan(request.getMinQuality())) {
                ProjectOperationException ex = null;
                Throwable first = null;
                for (LoadContextImpl d : loadData) {
                    if (d.reloadError != null) {
                        Throwable t = d.reloadError.getCause();
                        if (t instanceof ProjectOperationException) {
                            ex = (ProjectOperationException) t;
                            break;
                        } else if (first == null) {
                            first = t;
                        }
                    }
                }
                if (ex == null) {
                    ex = new ProjectOperationException(project, ProjectOperationException.State.BROKEN, Bundle.ERR_ProjectQualityLow(ProjectUtils.getInformation(project).getDisplayName()), first);
                }
                for (LoadContextImpl d : loadData) {
                    Throwable e = d.reloadError;
                    if (e != null) {
                        Throwable c = e.getCause();
                        if (ex != c && ex.getCause() != c && e != c) {
                            ex.addSuppressed(c);
                        }
                    }
                }
                error = ex;
                LOG.log(Level.FINE, "{0} loaded as low quality ({1}/{2}), failing operation", new Object[]{this, result.getQuality(), request.getMinQuality()});
                LOG.log(Level.FINE, "Error is: {0}", error);
            }
            if (error != null) {
                return CompletableFuture.failedFuture(error);
            } else {
                return CompletableFuture.completedFuture(result);
            }
        } finally {
            // go through all states acquired during the reload, and check which were registered for ProjectStates.
            // The ones which are registered will be cleaned up when their identity object is GCed.
            // But those that are NOT registered as identities need to be cleaned now.
            for (Iterator<Pair<ProjectReloadImplementation<?>, ProjectReloadImplementation.ProjectStateData>> it = reportedStates.iterator(); it.hasNext();) {
                Pair<ProjectReloadImplementation<?>, ProjectReloadImplementation.ProjectStateData> pair = it.next();
                if (parts.get(pair.first()) != pair.second()) {
                    LOG.log(Level.FINE, "{0}: Invalidating state {1}, final state has different instance", new Object[]{this, pair.second()});
                    pair.second().fireChanged(true, false);
                }
                if (registry.hasIdentity(pair.second())) {
                    it.remove();
                }
            }
            // run a delayed cleanup action for each of unknown states.
            if (!reportedStates.isEmpty()) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "{0}: Scheduling cleanup of dangling states", new Object[]{this,
                        reportedStates.stream().map(p -> p.second().toString()).collect(Collectors.joining(", "))
                    });
                }
                registry.runProjectAction(project, () -> {
                    reportedStates.forEach(pair -> {
                        LOG.log(Level.FINE, "{0}: Releasing dangling states");
                        pair.first().projectDataReleased(pair.second());
                        ReloadSpiAccessor.get().release(pair.second());
                    });
                });
            }
        }
    }

    /**
     * Clears per-load round state information. Prepares implementations
     * iterator.
     */
    synchronized void initRound() {
        LOG.log(Level.FINE, "Initializing loader {0}", this);
        implIter = loadData.iterator();
        parts = new ProjectReloadInternal.StatePartsImpl();
        loadData.forEach(c -> c.reinit(parts));
        this.forcedRound = false;
        Thread.currentThread().setName(toString());
    }

    public void cancel(CancellationException cancelled) {
        LOG.log(Level.FINE, "{0}: Cancelled", this);
        LOG.log(Level.FINE, "Stacktrace: ", cancelled);

        LoadContextImpl c;

        synchronized (this) {
            if (this.cancelled != null) {
                return;
            }
            this.cancelled = cancelled;
            c = this.currentStageContext;
        }
        if (c != null) {
            c.cancel(cancelled);
        }
    }

    private CompletableFuture<ProjectReload.ProjectState> loadStepDone(ProjectStateData<?> state, LoadContextImpl fd, Throwable t) {
        // record the returned data for subsequent rounds:
        if (t != null) {
            fd.reloadError = t;
        }
        fd.loadedData = state;
        fd.loadedOnce = true;
        synchronized (this) {
            parts.put(fd.impl, state);
            if (state != null) {
                reportedStates.add(Pair.of(fd.impl, state));
            }
        }
        LOG.log(Level.FINE, "{0} {1} loaded {2} with load-private data {3}", new Object[]{this, fd.impl, state, fd.contextData});
        currentStage = null;
        currentStageContext = null;
        return processOne();
    }

    /**
     * Reports a cancelled load. The issue is that implementations that have
     * succeeded so far may have replaced their ProjecStates with new instances,
     * which are now in this.parts - but will not be used to form a new
     * ProjectState. We use a Forwarder to forward changes to the active
     * ProjectStateDatas.
     *
     * @return
     */
    private CompletableFuture<ProjectReload.ProjectState> cancelLoadInProgress() {
        if (this.currentStageContext != null) {
            currentStageContext.reinit(null);
        }
        currentStage = null;
        currentStageContext = null;
        LOG.log(Level.FINE, "{0}: got cancel", this);
        LOG.log(Level.FINE, "Stacktrace: ", cancelled);
        Forwarder.create(originalState, parts, null, false);
        return CompletableFuture.failedFuture(cancelled);
    }

    /**
     * Unlike RequestProcessor, CompletableFuture always relays the work to the Executor in *Async methods. This wrapper
     * will execute the supplied handler directly, if it is invoked in the 'correct' thread. Just an optimization.
     */
    private <T, U> CompletableFuture<U> composeMaybeAsync(CompletableFuture<T> f, Function<T, CompletableFuture<U>> handler) {
        return f.thenComposeAsync(handler, reloadExecutor);
    }

    private <T> CompletableFuture<T> exceptionallyMaybeAsync(CompletableFuture<T> f, Function<Throwable, T> handler) {
        return f.exceptionallyAsync(handler, reloadExecutor);
    }
    
    public CompletableFuture<ProjectReload.ProjectState> start(RequestProcessor processor) {
        this.reloadProcessor = processor;
        this.reloadExecutor = new RPExecutor(processor);
        return processOne();
    }
    
    private static final ProjectStateData CANCEL = ProjectStateData.builder(ProjectReload.Quality.NONE).build();

    /**
     * Processes one implementation participant. Sort of, because if a {@link ProjectReloadImplementation}
     * {@link ProjectReloadImplementation#accept}s the state data, it's reload
     * will not be called at all.
     *
     * @return
     */
    synchronized CompletableFuture<ProjectReload.ProjectState> processOne() {
        LoadContextImpl d;
        if (cancelled != null) {
            return cancelLoadInProgress();
        }
        while (true) {
            if (!implIter.hasNext()) {
                return finishLoadingRound();
            }
            d = implIter.next();
            if (forcedRound) {
                LOG.log(Level.FINE, "{0}: This round is forced: loading {0}", new Object[]{this, d.impl});
                break;
            }
            // force load will only affect the 1st reload.
            if (d.loadedData == null || (forced && !d.loadedOnce)) {
                LOG.log(Level.FINE, "{0}: {1} data: {1}, once: {2}, force: {3}", new Object[]{this, d.impl, d.loadedOnce, forced});
                break;
            }
            ProjectReload.Quality q = d.loadedData.getQuality();
            LOG.log(Level.FINE, "{0}: Checking cached data {1}, request {2}", new Object[]{this, d.loadedData, request});
            if (q != null && q.isWorseThan(request.getMinQuality())) {
                break;
            }
            if (!d.loadedData.isConsistent() || !d.loadedData.isValid()) {
                break;
            }
            if (lastRetries.contains(d)) {
                break;
            }
            if ((d.impl instanceof ExtendedQuery) && !((ExtendedQuery) d.impl).checkState(request, d.loadedData)) {
                LOG.log(Level.FINE, "{0}: {1} rejected by {2}", new Object[]{this, d.loadedData, d.impl});
                break;
            }
        }
        final LoadContextImpl fd = d;
        try {
            CompletableFuture<ProjectReloadImplementation.ProjectStateData<?>> newData;
            LOG.log(Level.FINE, "{0} loading through {1}, last state {2}, load data {3}", new Object[]{this, d.impl, d.loadedData, d.contextData});
            currentStageContext = d;
            if (cancelled != null) {
                return cancelLoadInProgress();
            }
            fd.clientContext = ReloadSpiAccessor.get().createLoadContext(fd);
            newData = d.impl.reload(project, request, fd.clientContext);
            currentStage = newData;

            if (newData == null) {
                // the implementation is not willing to participate at all
                return loadStepDone(null, fd, null);
            }
            // synthesize a new ProjectStateData
            // refire all file and validity changes.
            // this will keep the listener alive :-/
            CompletableFuture<ProjectReloadImplementation.ProjectStateData<?>> res = exceptionallyMaybeAsync(newData, t -> {
                LOG.log(Level.FINE, "{0} got exceptional result from {1}", new Object[]{this, fd.impl});
                LOG.log(Level.FINE, "Stacktrace: ", t);
                if (t instanceof CompletionException) {
                    t = t.getCause();
                }
                if (t instanceof CancellationException) {
                    // return cancelLoadInProgress();
                    return CANCEL;
                }
                if (t instanceof ProjectReloadImplementation.PartialLoadException) {
                    ProjectReloadImplementation.PartialLoadException ple = (ProjectReloadImplementation.PartialLoadException) t;
                    fd.reloadError = ple;
                    return ple.getPartialData();
                } else {
                    // synthesize a new ProjectStateData
                    ProjectReloadImplementation.ProjectStateBuilder b = ProjectReloadImplementation.ProjectStateData.builder(fd.loadedData == null ? ProjectReload.Quality.NONE : ProjectReload.Quality.BROKEN);

                    // ForwardDataChanges refires changes on the last impl-created state data on this fake instance.
                    class ForwardDataChanges implements ProjectStateListener {

                        volatile ProjectReloadImplementation.ProjectStateData toFire;

                        @Override
                        public void stateChanged(ChangeEvent e) {
                            // refire all file and validity changes.
                            ProjectReloadImplementation.ProjectStateData orig = (ProjectReloadImplementation.ProjectStateData) e.getSource();
                            toFire.fireFileSetChanged(orig.getChangedFiles());
                            toFire.fireChanged(!orig.isValid(), !orig.isConsistent());
                        }

                        @Override
                        public void fireDataInconsistent(ProjectStateData d, Class<?> dataClass) {
                            toFire.fireDataInconsistent(dataClass);
                        }
                    }
                    ProjectReloadImplementation.ProjectStateData fakeD;
                    if (fd.loadedData != null) {
                        b.files(fd.loadedData.getFiles());
                        b.state(fd.loadedData.isConsistent(), true);
                        b.timestamp(fd.loadedData.getTimestamp());
                        ForwardDataChanges cl = new ForwardDataChanges();
                        // this will keep the listener alive :-/
                        b.attachLookup(Lookups.fixed(cl));
                        fakeD = b.build();
                        cl.toFire = fakeD;
                        ReloadSpiAccessor.get().addProjectStateListener(fd.loadedData, cl);
                    } else {
                        fakeD = b.build();
                    }
                    fd.reloadError = t;
                    return fakeD;
                }
            });

            CompletableFuture<ProjectState> res2 = composeMaybeAsync(res, data -> {
                if (data != CANCEL) {
                    return loadStepDone(data, fd, null);
                } else {
                    return cancelLoadInProgress();
                }
            });
            return res2;
        } catch (ProjectReloadImplementation.PartialLoadException ex) {
            LOG.log(Level.FINE, "{0} got PartialLoadException", this);
            LOG.log(Level.FINE, "Stacktrace", ex);
            ProjectReloadImplementation.PartialLoadException ple = (ProjectReloadImplementation.PartialLoadException) ex;
            return loadStepDone(ple.getPartialData(), fd, ple);
        }
    }
}
