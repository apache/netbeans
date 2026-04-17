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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.dependency.ProjectOperationException;
import org.netbeans.modules.project.dependency.ProjectReload;
import org.netbeans.modules.project.dependency.ProjectReload.ProjectState;
import org.netbeans.modules.project.dependency.ProjectReload.Quality;
import org.netbeans.modules.project.dependency.ProjectReload.StateRequest;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation.ExtendedQuery;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation.ProjectStateData;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 * The real implementation of the ProjectReload API process. This class provides a cache for
 * project states. It implements identity tracking for ProjectStates to help implementors detach
 * listeners and/or clean up project data for obsolete ProjectStates even though they are still reachable.
 * It also implements events and operation queue that postpones processing until after the project (re)load completes,
 * to avoid unnecessary queries while a reload is in progress.
 * <p>
 * The design anticipates GC issues with {@link ProjectStateData} that are typically bound to files, and keep (large) project structures.
 * Ideally the ProjectReloadImplementation uses WeakRefs to ProjectStateData, but ProjectReloadInternal keeps a separate track: Each ProjectState
 * holds special "identity objects" for each of its ProjectStateData. When all ProjectStates that use specific ProjectStateData are GCed, the "identity object"
 * is GCed and its reference queued. This indicates the ProjectStateData should be released unless it happens to be GCed as well. These cleanup actions
 * are only executed when the project is NOT reloading.
 * <p>
 * {@link Reloader} is created for each reload request. Just one request per project is processed at a time, pending or current actions for a project are tracked
 * in {@link ProjectOperations}. When project operation ends ({@link #endOperation}), it may immediately start another reload. Postponed actions are executed after
 * end of reload: this allows to fire events either after project is stable, or at least before the next reload starts. 
 * <p>
 * Number of projects being reload concurrently is limited. Each reload happens in its dedicated RequestProcessor/thread, although the actions themselves are asynchronous. The
 * Reloader schedules continuations into that dedicated RP, trying to achieve that each {@link ProjectReloadImplementation#reload} will happen in that RP - the 
 * implementation is free to schedule the future's work to any thread.
 * 
 * @author sdedic
 */
public class ProjectReloadInternal {
    public static final Logger LOG = Logger.getLogger(ProjectReloadInternal.class.getName());
    
    /**
     * Default concurrency
     */
    private static final int DEFAULT_PROJECT_RELOAD_CONCURRENCY = 10;

    /**
     * How many reload operations can run concurrently.
     */
    private static final int PROJECT_RELOAD_CONCURRENCY = Integer.getInteger(ProjectReloadInternal.class.getName() + ".reload.concurrency", DEFAULT_PROJECT_RELOAD_CONCURRENCY); // NOI18N
    
    /**
     * Timeout to clear stale ProjectStates from the cache.
     */
    static final int STATE_TIMEOUT_MS = 5 * 1000;

    /**
     * Instance of this support.
     */
    private static ProjectReloadInternal INSTANCE;

    /**
     * A RequestProcessor dedicated for reloading. This RP will deliver completions and failures of client-facing 
     * CompletableFutures. Subsequent client actions will not obscure the project loading process that much, but will
     * block other clients.
     */
    public static final RequestProcessor RELOAD_RP = new RequestProcessor(ProjectReloadInternal.class.getName() + ".reload");

    /**
     * This RP is used to broadcast events from {@link ProjectStates}. It is
     * deliberately separate from the loading RP and any event queue threads. All events are delivered 
     * sequentially.
     */
    public static final RequestProcessor NOTIFIER = new RequestProcessor(ProjectReloadInternal.class.getName() + ".events"); // NOI18N

    /**
     * Dedicated thread to fire state listeners.
     */
    static final RequestProcessor STATE_CLEANER = new RequestProcessor(ProjectReload.class.getName());

    /**
     * Caches ProjectStates. States are kept by reference, so forgetting
     * ProjectState will eventually evict it + its project from this Cache. Only
     * <b>last known</b> state is kept here - each request to reload will
     * replace the entry for the project.
     */
    private static final Map<Collection, StateRef> STATE_CACHE = new HashMap<>();

    /**
     * Reloads currently pending for the project. The ProjectOperations contains queues for
     * events, state releases and other stuff that should not be processed during project reload.
     */
    private final Map<Project, ProjectOperations> pendingOperations = new WeakHashMap<>();
    private final Set<ProjectOperations> terminatingOperations = new HashSet<>();

    /**
     * Identity map for ProjectStateData. Each is assigned a special Object held in the handle
     * by a Reference to that Object. 
     */
    
    private final WeakIdentityMap<ProjectStateData, IdentityHolder> stateIdentity = WeakIdentityMap.newHashMap();
    /**
     * Returns the singleton instance of the implementation.
     */
    public static synchronized ProjectReloadInternal getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProjectReloadInternal();
        }
        return INSTANCE;
    }
    
    /**
     * For testing: allows to inject a mock ProjectReloadImplementation for a project. 
     * @param p the project
     * @return reload implementations.
     */
    Lookup.Result<? extends ProjectReloadImplementation<Object>> findImplementations(Project p) {
        return p.getLookup().lookupResult(ProjectReloadImplementation.class);
    }
    
    /**
     * Timed reference, that expires after 30 seconds and turns into weak. When cleared/enqueued, it
     * removes itself from STATE_CACHE.
     */
    public static final class StateRef extends WeakReference<ProjectReload.ProjectState> implements Runnable {
        private final RequestProcessor.Task evictTask = STATE_CLEANER.create(this);
        
        final Collection variantKey;

        /**
         * Last time the reference a client has obtained the ProjectState from the API
         */
        private volatile long lastAccessed;

        /**
         * Keeps the ProjectState in memory for a limited time.
         */
        private volatile ProjectReload.ProjectState hard;

        /**
         * State listener that propagates changes from ProjectStateData to ProjectState.
         */
        // @GuardedBy(this)
        StateDataListener toDetach;

        StateRef(Collection variant, ProjectReload.ProjectState referent) {
            super(referent, BaseUtilities.activeReferenceQueue());
            this.variantKey = variant;
        }
        
        public ProjectReload.ProjectState touch() {
            ProjectReload.ProjectState o = hard;
            if (o == null) {
                o = super.get();
            }
            if (o != null) {
                long m = System.currentTimeMillis();
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "Project touched: {0}@{3} at {1}, lastAccessed = {2}", new Object[] { o.toString(), m, lastAccessed, 
                        Integer.toHexString(System.identityHashCode(this)) });
                }
                hard = o;
                if (lastAccessed == 0) {
                    evictTask.schedule(STATE_TIMEOUT_MS);
                }
                lastAccessed = m;
            }
            return o;
        }

        /**
         * Weakens or expunges the reference. Note - the reference is called
         * from a RequestProcessor on hardref timeout, or by active reference
         * queue after GC collects the referrent.
         */
        @Override
        public void run() {
            if (hard == null) {
                synchronized (STATE_CACHE) {
                    STATE_CACHE.remove(variantKey);
                }
                synchronized (this) {
                    if (toDetach == null) {
                        LOG.log(Level.FINER, "Project state GCed: {0}", Integer.toHexString(System.identityHashCode(this)));
                        return;
                    }
                }
                LOG.log(Level.FINER, "Project state {0}: detaching listeners", Integer.toHexString(System.identityHashCode(this)));
                toDetach.detachListeners();
            } else {
                long unused = System.currentTimeMillis() - lastAccessed;

                if (unused > (STATE_TIMEOUT_MS / 2)) {
                    hard = null;
                    lastAccessed = 0;
                } else {
                    evictTask.schedule(STATE_TIMEOUT_MS - (int) unused);
                }
            }
        }
    }
    
    /**
     * Just a typedef. I was tired of repeating the generic declaration over and
     * over.
     */
    public interface StateParts extends Map<ProjectReloadImplementation<?>, ProjectStateData<?>> {}

    /**
     * Default implementation of the StateParts. 
     */
    static class StatePartsImpl extends LinkedHashMap<ProjectReloadImplementation<?>, ProjectStateData<?>> implements StateParts {
        public StatePartsImpl() {
        }

        public StatePartsImpl(Map<? extends ProjectReloadImplementation<?>, ? extends ProjectStateData<?>> m) {
            super(m);
        }
    }
    
    public static final StateParts EMPTY_PARTS = new StatePartsImpl();
    
    static Collection variantKey(Project p, StateParts parts, Lookup context) {
        Collection c = new HashList();
        c.add(p);
        if (parts == null) {
            return c;
        }
        for (Map.Entry<ProjectReloadImplementation<?>, ProjectStateData<?>> en : parts.entrySet()) {
            ProjectStateData d = en.getValue();
            if (context != null && context != Lookup.EMPTY) {
                Object k = en.getKey() instanceof ExtendedQuery ? ((ExtendedQuery)en.getKey()).createVariant(context) : null;
                if (k != null) {
                    c.add(en.getKey());
                    c.add(k);
                }
            }
        }
        return c;
    }

    /**
     * Creates a State instance, but does not register it in the cache. Used in real state construction, or when a state that is just partial and temporary,
     * created by LoadContextImpl is requested by some ProjectReloadImplementation.
     * 
     * @param p project
     * @param parts state parts
     * @param req the request
     * @return created state
     */
    ProjectState doCreateState(Project p, StateParts parts, StateRequest req) {
        ProjectReload.Quality status = null;
        boolean consistent = true;
        Set<FileObject> edited = new LinkedHashSet<>();
        Set<FileObject> modified = new LinkedHashSet<>();
        Set<FileObject> loadedFiles = new LinkedHashSet<>();
        long timestamp = Long.MAX_VALUE;
        boolean valid = true;
        List<Object> ids = new ArrayList<>();

        for (Map.Entry<ProjectReloadImplementation<?>, ProjectStateData<?>>  en : parts.entrySet()) {
            ProjectStateData<?> data = en.getValue();
            if (data == null) {
                continue;
            }
            // establish an identity and put it into the track list
            ids.add(identity(p, en.getKey(), data));
            ProjectReload.Quality q = data.getQuality();
            if (status == null || q.isWorseThan(status)) {
                status = q;
            }
            Collection<FileObject> mods = data.getFiles();
            if (mods != null) {
                loadedFiles.addAll(mods);
            }
            long time = data.getTimestamp();
            if (time > 0 && time < timestamp) {
                timestamp = time;
            }
            if (!data.isValid()) {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "New state invalid because of {0}", data.toString());
                }
            }
            valid &= data.isValid();
        }

        for (FileObject f : loadedFiles) {
            long t = f.lastModified().getTime();
            if (f.getLookup().lookup(SaveCookie.class) != null) {
                edited.add(f);
                LOG.log(Level.FINER, "New state inconsistent because of {0} is edited", f);
                consistent = false;
                break;
            }
            if (timestamp > 0 && t > timestamp) {
                LOG.log(Level.FINER, "New state inconsistent because of {0} is newer: file time: {1}, timestamp: {2}", new Object[] { 
                    f, t, timestamp
                });
                consistent = false;
                modified.add(f);
            }
        }
        if (parts.isEmpty()) {
            status = ProjectReload.Quality.NONE;
            timestamp = -1;
        }
        Quality tq = req == null ? status : req.getTargetQuality();
        ProjectState ps = ReloadApiAccessor.get().createState(p, timestamp, parts, status, tq, consistent, valid, loadedFiles, modified, edited, ids);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Created state {0} from {1}", new Object[] { ps.toString(), parts.toString() });
        }
        return ps;
    }
    
    boolean mergeStates(ProjectReload.ProjectState cached, ProjectReload.ProjectState now, StateParts parts, StateRequest req) {
        if ((cached.isValid() != now.isValid()) || (cached.isConsistent() != now.isConsistent()) || cached.getTimestamp() != now.getTimestamp()) {
            return false;
        }
        if (cached.getQuality().isWorseThan(now.getQuality())) {
            return false;
        }
        StateParts cachedParts = ReloadApiAccessor.get().getParts(cached);
        if (cachedParts.size() != parts.size()) {
            return false;
        }
        for (ProjectReloadImplementation impl : parts.keySet()) {
            ProjectStateData cData = cachedParts.get(impl);
            ProjectStateData nData = parts.get(impl);
            if (impl instanceof ExtendedQuery) {
                if (!((ExtendedQuery)impl).checkState(req, cData)) {
                    return false;
                }
            } else {
                if (!Objects.equals(cData, nData)) {
                    return false;
                }
            }
        }
        Forwarder fwd = Forwarder.create(cached, parts, now, true);
        return fwd != null;
    }

    Pair<StateRef, ProjectState> createState(ProjectReload.ProjectState previous, Project p, Collection variant, StateParts parts,
            boolean rejectInconsistent, StateRequest req) {
        ProjectReload.ProjectState state = doCreateState(p, parts, req);

        ProjectReload.ProjectState cur = null;
        Collection<ProjectState> obsolete = new HashSet<>();
        StateRef ref;
        
        synchronized (STATE_CACHE) {
            StateRef oldRef = STATE_CACHE.get(variant);
            if (oldRef != null) {
                cur = oldRef.get();
                if (cur != null) {
                    if (mergeStates(cur, state, parts, req)) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, "Project {0}: reused & merged state {1} in place of {2}", new Object[] { p, cur.toString(), state.toString() });
                        }
                        return Pair.of(oldRef, cur);
                    }
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "Project {0}: obsolete state {1}", new Object[] { p, cur.toString() });
                    }
                    ReloadApiAccessor.get().chainPrevious(state, cur, obsolete);
                }
            }
            
            ref = new StateRef(variant, state);
            StateDataListener l = new StateDataListener(p, parts, ref);
            ref.toDetach = l;
            // must replace asynchronously created state, as the last ProjectStateData instance may become
            // registered in Implementations.
            if (!parts.isEmpty()) {
                l.init();
            }
            if (!state.isConsistent() && rejectInconsistent) {
                return Pair.of(null, state);
            }
            if (cur != previous && previous != null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Project {0}: obsolete previous state {1}", new Object[] { p, previous.toString() });
                }
                ReloadApiAccessor.get().chainPrevious(state, previous, obsolete);
            }
            STATE_CACHE.put(variant, ref);
        }
        if (!obsolete.isEmpty() && LOG.isLoggable(Level.FINE)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Project {0}: invalidate states", new Object[] { p, obsolete.toString() });
            }
        }
        // new changes on the current state will fire events on the 'forgotten' ones,
        // but now we need to invalidate+fire them manually.
        obsolete.forEach(s -> 
                ReloadApiAccessor.get().fireInvalid(s)
        );
        // but for the just-replaced states, make sure their StateData are invalidaed,
        // if they do not match the current one.
        if (cur != null) {
            invalidatePreviousState(parts, cur);
        }
        if (previous != cur) {
            invalidatePreviousState(parts, previous);
        }
        return Pair.of(ref, state);
    }

    private void invalidatePreviousState(StateParts parts, ProjectState previous) {
        if (previous == null) {
            return;
        }
        StateParts oparts = ReloadApiAccessor.get().getParts(previous);
        for (ProjectReloadImplementation k : parts.keySet()) {
            ProjectStateData nd = parts.get(k);
            ProjectStateData od = oparts.get(k);
            if (!Objects.equals(nd, od)) {
                // invalidate obsolete state part data, if the implementation did not do it itself.
                if (od != null) {
                    if (od.isValid()) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINER, "Invalidate project state {0}", od.toString());
                        }
                    }
                    od.fireChanged(true, false);
                }
            }
        }
        for (ProjectReloadImplementation k : oparts.keySet()) {
            if (parts.get(k) == null) {
                ProjectStateData od = oparts.get(k);
                if (od != null) {
                    if (od.isValid()) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINER, "Invalidate project state {0}", od.toString());
                        }
                    }
                    od.fireChanged(true, false);
                }
            }
        }
        // invalidate the state anyway even though all state data are current. There can be only
        // one valid state.
        ReloadApiAccessor.get().updateProjectState(previous, false, true, null, null, null);
    }
    
    /**
     * ArrayList that caches the hashcode of its contents. Must not be mutated.
     */
    private static class HashList extends ArrayList {
        private int h;

        public HashList() {
        }

        @Override
        public int hashCode() {
            if (h == 0) {
                int h2 = super.hashCode();
                if (h2 == 0) {
                    h = -1;
                } else {
                    h = h2;
                }
            }
            return h;
        }
    }
    
    public ProjectState createNoneState(Project p) {
        StateParts parts = new StatePartsImpl();
        ProjectState none = doCreateState(p, parts, null);
        return none;
    }

    public Pair<StateRef, ProjectState> getProjectState0(Project p, Lookup context, boolean nullIfUnknown) {
        Lookup.Result<? extends ProjectReloadImplementation<Object>> res = findImplementations(p);
        Collection<? extends ProjectReloadImplementation<?>> col = res.allInstances();
        
        Collection variant = new HashList();
        variant.add(p);
        for (ProjectReloadImplementation impl : col) {
            Object k = impl instanceof ExtendedQuery ? ((ExtendedQuery)impl).createVariant(context) : null;
            if (k != null) {
                variant.add(impl);
                variant.add(k);
            }
        }
        
        StateRef ref = null;
        
        // try to keep alive for the length of the operation.
        ProjectState oldS = null;
        // from now on, even though the ReleaseRef reference is enqueued, it won't be cleared as the clear
        // will be postponed until after endOperation ... and by that time the ProjectStateData will have
        // a new ReleaseRef for it.
        lockOperation(p);
        try {
            synchronized (STATE_CACHE) {
                ref = STATE_CACHE.get(variant);
                if (ref != null) {
                    // get and keep the State alive.
                    oldS = ref.touch();
                }
                if (oldS != null) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "STATE: project {0}: cached state returned: {1}", new Object[] { p, oldS.toString() });
                    }
                    return Pair.of(ref, oldS);
                } else if (nullIfUnknown) {
                    return null;
                }
                ProjectState none = createNoneState(p);
                ref = new StateRef(variant, none);
                LOG.log(Level.FINE, "STATE: Project {0}: NONE state created: {1}", new Object[] { p, none });
                STATE_CACHE.put(variant, ref);
                return Pair.of(ref, none);
            }
        } finally {
            if (oldS != null && ref.toDetach != null) {
                // assume ref must not be null
                ref.toDetach.checkFileTimestamps();
            }
            endOperation(p, null, null);
        }
    }

    public static boolean checkConsistency(ProjectState ps, StateParts parts, StateRequest stateRequest) {
        boolean doReload = !ps.isValid() || stateRequest.isForceReload();

        if (stateRequest.isConsistent() && !ps.isConsistent()) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINER, "{0}: CHECK: State inconsistent: {1}", new Object[] { stateRequest, ps.toString() });
            }
            doReload = true;
        }
        if (ps.getQuality().isWorseThan(stateRequest.getMinQuality())) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINER, "{0}: CHECK:State low quality: {1}", new Object[] { stateRequest, ps.toString() });
            }
            doReload = true;
        }
        if (!doReload) {
            for (ProjectReloadImplementation pi : parts.keySet()) {
                ProjectStateData psd = parts.get(pi);
                if (pi instanceof ExtendedQuery && !((ExtendedQuery)pi).checkState(stateRequest, psd)) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINER, "{0} CHECK: state data rejected: {0} / {1}", new Object[] { stateRequest, ps.toString(), psd.toString() });
                    }
                    return false;
                }
            }
        }
        return doReload;
    }

    private static boolean satisfies(Project p, StateRequest next, StateRequest pending, ProjectState state) {
        if (next.isForceReload()) {
            return false;
        }
        if (!next.isOfflineOperation() && pending.isOfflineOperation()) {
            return false;
        }
        if (next.isSaveModifications() && !pending.isSaveModifications()) {
            return false;
        }
        if (pending.getMinQuality().isWorseThan(next.getMinQuality())) {
            return false;
        }
        StateParts parts = state == null ? null
                : ReloadApiAccessor.get().getParts(state);

        for (ProjectReloadImplementation impl : p.getLookup().lookupAll(ProjectReloadImplementation.class)) {
            if ((impl instanceof ExtendedQuery) && !((ExtendedQuery)impl).satisfies(pending, next)) {
                LOG.log(Level.FINER, "CHECK: pending rejected: {0} / {1}", new Object[] { pending, next });
                return false;
            }
        }
        return true;
    }

    /**
     * This thread just processes load request queue. It is suspended on {@link #loaderProcessors} if no processors
     * are available. The thread itself does not execute load requests, but relays them to a loadProcessor
     */
    private final RequestProcessor dispatcher = new RequestProcessor(ProjectReloadInternal.class + " Scheduler");
    
    /**
     * Internal bounded thread pool of loaders. Each loader processes one project reload request at a time. 
     */
    private final BlockingQueue<RequestProcessor> loaderProcessors = new ArrayBlockingQueue<>(PROJECT_RELOAD_CONCURRENCY);
    
    public ProjectReloadInternal() {
        for (int i = 0; i < PROJECT_RELOAD_CONCURRENCY; i++) {
            loaderProcessors.add(new RequestProcessor(getClass().getName() + "-1"));
        }
    }
    
    @NbBundle.Messages({
        "# {0} - project name",
        "TEXT_ConcurrentlyModified=Project {0} has been concurrently modified",
        "# {0} - project name",
        "TEXT_UnsupportedReload=Project {0} does not support reloads"
    })
    public CompletableFuture<ProjectState> withProjectState2(StateRef refCurrent, Project p, StateRequest stateRequest, Throwable origin) {
        Lookup.Result<? extends ProjectReloadImplementation> impls = findImplementations(p);
        
        // short path, if no implementations are registered.
        if (impls.allItems().isEmpty()) {
            // fake a project state, as there's no implemenation.
            StateParts parts = new StatePartsImpl();
            Collection variant = variantKey(p, null, stateRequest.getContext());
            Pair<StateRef, ProjectState> s = createState(null, p, variant, parts, false, null);
            
            if (Quality.NONE.isAtLeast(stateRequest.getMinQuality())) {
                return CompletableFuture.completedFuture(s.second());
            } else {
                ProjectOperationException ex = new ProjectOperationException(p, ProjectOperationException.State.UNSUPPORTED, Bundle.TEXT_UnsupportedReload(ProjectUtils.getInformation(p).getDisplayName()));
                return CompletableFuture.failedFuture(ex);
            }
        }

        ProjectOperations op = lockOperation(p);
        try  {
            Collection<Reloader> requests;
            synchronized (this) {
                // cleanup garbage
                stateIdentity.reap();
                requests = new ArrayList<>(op.pendingReloads);
            }
            for (Reloader pr : requests) {
                if (satisfies(p, stateRequest, pr.request, pr.originalState)) {
                    LOG.log(Level.FINE, "Request {0} coalesced with {1}", new Object[] { pr, stateRequest });
                    return pr.clientFuture;
                }
            }
            Reloader reload = new Reloader(p, stateRequest, refCurrent, impls.allInstances(), this, origin);
            
            synchronized (this) {
                // PENDING: maybe take this add out of the synchronized block, so satisfies() check need not to be done under the lock.
                op.pendingReloads.add(reload);
                LOG.log(Level.FINE, "START: project {0} load with request {1}, reload {2}", new Object[] { p, stateRequest, reload });
            }
            return reload.clientFuture;
        } finally {
            endOperation(p, null, null);
        }
    }
    
    /**
     * There are two project operations. getState and withState. At the beginning,
     * the operations must acquire this lock object, incrementing the usage. At the end,
     * the lock will be released, decrementing the usage. 
     * <p>
     * ProjectState events and obsolete ProjectStateData detachments are held
     * while the ProjectOperationss register at least one usage. This helps
     * to stabilize the system while projects are reloading (events) and allows
     * to NOT release ProjectStateData between the implementation supplies it,
     * and this API core accepts and turns into ProjectState slaves. 
     * 
     * Access to all members must be synchronized by ProjectReloadInternal instance.
     */
    private static class ProjectOperations {
        /**
         * Usage count
         */
        int usage;
        
        /**
         * Handles to ProjectStateData being released.
         */
        Collection<IdentityHolder>   releases = new ArrayList<>();
        
        /**
         * Reloads of projects underway for piggyback.
         */
        Collection<Reloader>   pendingReloads = new ArrayList<>();
        
        /**
         * Postponed actions. Right now just events fires
         */
        Collection<Runnable> postponedActions = new ArrayList<>();
        
        /**
         * The reload being currently executed. Must be one of {@link #pendingReloads}.
         */
        Reloader currentReload;
        
        // @GuardedBy(ProjectReloadInternal.this)
        Reloader nextReloader() {
            if (pendingReloads.isEmpty()) {
                return null;
            } else {
                currentReload = pendingReloads.iterator().next();
                return currentReload;
            }
        }
        
        // @GuardedBy(ProjectReloadInternal.this)
        boolean removeReloader(Reloader r) {
            if (r != null && currentReload == r) {
                currentReload = null;
            }
            return r == null || pendingReloads.remove(r);
        }
    }
    
    /**
     * Starts an operation.
     * @param p the project
     * @return Lock object.
     */
    private synchronized ProjectOperations lockOperation(Project p) {
        ProjectOperations op = pendingOperations.computeIfAbsent(p, x -> new ProjectOperations());
        op.usage++;
        return op;
    }
    
    /**
     * Registers or runs project actions. Postpones the action, if the project lock
     * is active, or runs immediately if it is not.
     * @param p the project
     * @param r the operation.
     */
    public void runProjectAction(Project p, Runnable r) {
        ProjectOperations op;
        synchronized (this) {
            op = pendingOperations.get(p);
            if (op != null && op.usage > 0) {
                LOG.log(Level.FINE, "ACTION: Postponed {0} / {1}", new Object[] { p, r });
                op.postponedActions.add(r);
                return;
            }
        }
        r.run();
    }
    
    public void assertNoOperations() {
        synchronized (this) {
            Map<Project, ProjectOperations> ops = new HashMap<>(this.pendingOperations);
            ops.values().removeAll(this.terminatingOperations);
            if (!ops.isEmpty()) {
                System.err.println("Pending operations detected");
                for (Map.Entry<Project, ProjectOperations> en : ops.entrySet()) {
                    ProjectOperations op = en.getValue();
                    System.err.println(en.getKey() + ": usage " + op.usage + ", pendingReloads: " + op.pendingReloads.size() + ", actions: " + op.postponedActions.size());
                    for (Reloader r : op.pendingReloads) {
                        r.getOriginTrace().printStackTrace();
                    }
                }
            }
            if (!ops.isEmpty() || this.loaderProcessors.size() != PROJECT_RELOAD_CONCURRENCY) {
                throw new IllegalStateException();
            }
        }
    }
    
    private Collection<IdentityHolder> collectReleases(ProjectOperations op) {
        Collection<IdentityHolder> releases = op.releases;
        // this is copied from postCleanup, but can be done in batch without 
        // checking the project operation is in progress for each reference. These are already removed
        // from stateIdentity, so just check they did not obtain another one:
        releases.removeIf(expired -> {
            ProjectStateData d = expired.state.get();
            if (d == null) {
                return true;
            }
            IdentityHolder h = stateIdentity.get(d);
            return h != null && h != expired;
        });
        op.releases = new ArrayList<>();
        return releases;
    }
    
    private void notityReleased(IdentityHolder h) {
        ProjectStateData d = h.state.get();
        if (d != null) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "CLEAN: Cleaning state: {0} with impl {1}", new Object[] { d.toString(), h.impl });
            }
            h.impl.projectDataReleased(d);
            ReloadSpiAccessor.get().release(d);
        }
    }
    
    /**
     * Ends the project operation. Optionally unregisters a reload. If more reloads are pending,
     * it starts another one. It ONLY dispatches one reload per project at a time. Multiple
     * reloads (for different projects) may be waiting in the dispatcher's queue, if the 
     * number of reloads exceeds {@link #PROJECT_RELOAD_CONCURRENCY}.
     * <p>
     * If `reload` is NOT null, the caller MUST execute the returned Runnable.
     * 
     * @param p the project.
     * @param reload if not null, specifies the ending reload.
     * @return runnable that sends out events and potentially continues next reload.
     */
    private void endOperation(Project p, Reloader reload, Runnable futureCompleter) {
        Reloader nextReloader;
        Collection<IdentityHolder>   releases = Collections.emptyList();
        Collection<Runnable> postponedActions;
        
        ProjectOperations op;
        synchronized (this) {
            op = pendingOperations.get(p);
            if (op == null) {
                throw new IllegalArgumentException();
            }
            if (reload != null && !op.removeReloader(reload)) {
                return;
            }
            --op.usage;
            if (op.usage > 0) {
                return;
            }
            // temporary increment
            ++op.usage;
            postponedActions = op.postponedActions;
            op.postponedActions = new ArrayList<>();
            nextReloader = op.nextReloader();

            if (nextReloader != null) {
                // schedule the next reload from the same project.
                ++op.usage;
                // will not be releasing ProjectStateData in op.releases now, since they will only queue up again.
            } else {
                // do not remove from the pendingOperations YET, we want to capture potential reload requests
                // until after the events are fired off, so they do not interleave.
                releases = collectReleases(op);
            }
            terminatingOperations.add(op);
        }
        LOG.log(Level.FINE, "ACTION: {0}: processing postponed actions", p);
        
        // note: this will eventually queue the cleanup again, if the project enter locked operation in the meantime.
        releases.forEach(this::notityReleased);
       
        if (futureCompleter != null) {
            futureCompleter.run();
        }

        postponedActions.forEach(Runnable::run);
        
        releases = null;
        postponedActions = null;

        synchronized (this) {
            terminatingOperations.remove(op);
            if (nextReloader == null) {
                nextReloader = op.nextReloader();
                // if a reload magically appeared, do NOT decrement the usage, as we didn't go through the usage++ in nextReloader != null above.
                if (nextReloader == null) {
                    if (--op.usage == 0) {
                        // finally remove, but still must process leftovers again
                        pendingOperations.remove(p);
                        releases = op.releases;
                        postponedActions = op.postponedActions;
                    }
                }
            } else {
                // decrement the temporary inc
                op.usage--;
            }
        }
        if (releases != null) {
            releases.forEach(this::notityReleased);
            postponedActions.forEach(Runnable::run);
        }
        
        if (nextReloader == null) {
            return;
        }
        
        Reloader fNextReloader = nextReloader;
        
        // start (first or next) project reload
        LOG.log(Level.FINE, "RELOAD-START: Project {0}: starting reload {1} with request {2}", new Object[] { p, nextReloader, nextReloader.request });
        dispatcher.post(() -> {
            RequestProcessor loader = null;
            while (loader == null) {
                try {
                    // will block if no RPs from loaderThreads are available
                    loader = loaderProcessors.take();
                } catch (InterruptedException ex) {
                }
            }

            RequestProcessor floader = loader;

            CompletableFuture<ProjectState> f = CompletableFuture.runAsync(() -> fNextReloader.initRound(), loader).
                    thenCompose((v) -> fNextReloader.start(floader));
            // run this cleanup in the dispatcher thread
            f.whenCompleteAsync((result, err) -> {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "ACTION: Return RP {0} to the pool after {1}", new Object[] { floader, fNextReloader });
                }
                loaderProcessors.offer(floader);
                try {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "COMPLETING: project {0} with request {1}, loader {2}", new Object[] { fNextReloader.project, fNextReloader.request, fNextReloader });
                    }
                    // postpone event delivery so that the events observers see the Future as completed.
                    endOperation(fNextReloader.project, fNextReloader, () -> {
                        if (err == null) {
                            fNextReloader.completePending.completeAsync(() -> result, RELOAD_RP);
                        } else {
                            RELOAD_RP.post(() -> {
                                fNextReloader.completePending.completeExceptionally(err);
                            });
                        }
                    });
                } catch (ThreadDeath td) {
                    throw td;
                } catch (Throwable t) {
                    Exceptions.printStackTrace(t);
                }
            }, floader);
        });
    }

    private void postCleanup(IdentityHolder expired) {
        ProjectStateData d = expired.state.get();
        if (d == null) {
            return;
        }
        IdentityHolder h;
        synchronized (this) {
            h = stateIdentity.get(d);
            if (h != null && h != expired) {
                // the state has obtained a new identity, different from 'r'
                // DO NOT release.
                return;
            }
            stateIdentity.remove(d);
            ProjectOperations op = pendingOperations.get(h.project);
            if (op != null && op.usage > 0) {
                op.releases.add(h);
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "ACTION: Postponing cleanup: {0} with impl {1}", new Object[] { d.toString(), h.impl });
                }
                return;
            }
        }
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "ACTION: cleaning {0} with impl {1}", new Object[] { d.toString(), h.impl });
        }
        // proceed with the release operation
        h.impl.projectDataReleased(d);
        ReloadSpiAccessor.get().release(d);
    }

    /**
     * Represents the registration of a ProjectStateData identity. It has to carry
     * enough information so that {@link ProjectReloadImplementation#projectDataReleased} can
     * be called.
     */
    private class IdentityHolder extends WeakReference implements Runnable {
        private final Project project;
        private final ProjectReloadImplementation impl;
        private final Reference<ProjectStateData> state;

        public IdentityHolder(Project p, ProjectReloadImplementation impl, ProjectStateData state, Object o) {
            super(o, BaseUtilities.activeReferenceQueue());
            this.project = p;
            this.impl = impl;
            this.state = new WeakReference<>(state);
        }

        @Override
        public void run() {
            postCleanup(this);
        }
    }
    
    /**
     * Creates an identity record for the ProjectStateData. Returns an existing identity if its
     * Object is still alive. Each PSD will get an unique Object in this identity map that will serve
     * as a beacon. It is not safe to track ProjectStateData directly as they may be held by the 
     * implementations. We track references from ProjectState to identity, and if identity is released,
     * the cleanup operation is enqueued.
     * 
     * @param p project
     * @param impl owner implementation
     * @param sd state data
     * @return identity Object
     */
    synchronized Object identity(Project p, ProjectReloadImplementation impl, ProjectStateData sd) {
        IdentityHolder h = stateIdentity.get(sd);
        if (h != null) {
            Object o = h.get();
            if (o != null) {
                return o;
            }
            // the identity object was released, so there's no ProjectState that references it. Replace the entry 
            // with a new identity.
        }
        Object o = new Object();
        stateIdentity.put(sd, new IdentityHolder(p, impl, sd, o));
        return o;
    }
    
    synchronized boolean hasIdentity(ProjectStateData sd) {
        return stateIdentity.get(sd) != null;
    }
}
