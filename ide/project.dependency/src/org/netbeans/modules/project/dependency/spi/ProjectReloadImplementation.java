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
package org.netbeans.modules.project.dependency.spi;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.dependency.ProjectReload;
import org.netbeans.modules.project.dependency.ProjectReload.ProjectState;
import org.netbeans.modules.project.dependency.ProjectReload.StateRequest;
import org.netbeans.modules.project.dependency.ProjectReload.Quality;
import org.netbeans.modules.project.dependency.reload.ProjectStateListener;
import org.netbeans.modules.project.dependency.reload.Reloader.LoadContextImpl;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * Provides information on files affecting the project reload, and allows to reload project metadata. 
 * The implementation must produce a {@link ProjectState} according to its internal state:
 * <ul>
 * <li>It <b>may</b>  report files that were loaded by the project system; in that case, these files will be automatically
 * monitored for changes. 
 * <li>it <b>must</b> report timestamp of the project metadata load. If the Implementation can not determine a timestamp, it must
 * return -1. Timestamps are used to determine consistency.
 * <li>it <b>must</b> evaluate quality of data. See {@link ProjectReload.Quality} for detailed levels.
 * </ul>
 * The implementation can degrade {@code consistency} property on an existing {@link ProjectState} indicating the state is out-of-date.
 * It can also degrade {@code valid} property, indicating a more recent data has been already loaded.
 * <p>
 * The implementation must produce a data from its {@link #getProjectData()}, possibly {@code null} indicating that it does not
 * participate in this project's metadata. The returned metadata should be whatever is currently available, the method should not 
 * inspect the project system, or execute scripts: it is expected it is fast. 
 * If it returns non-{@code null}, it must extract {@link ProjectStateData} from its internal
 * data representation. The API stores and manages lifecycle of the returned internal data. {@link Object#hashCode} and {@link Object#equals} 
 * must be defined for the internal representation, otherwise object identity is used.
 * <p>
 * The implementation is asked to {@code reload} when the API client asks for data in higher quality than available, or when some of the
 * {@link ProjectReloadImplementation} does not agree the current data is valid/good enough in its {@link #checkState} method.
 * <p>
 * The procedure {@link ProjectReload#withProjectState} is as follows:
 * <ol>
 * <li>The infrastructure forms a {@link ProjectState} from the request. Assuming {@link ProjectReloadImplementation} cache and return 
 * the same {@link #ProjectStateData} (if still valid), caching of {@link ProjectState} may be cached.
 * <li>{@link ProjectStateData#isConsistent}, modified files, changes to the set of files are data timestamps are checked.
 * <li>{@link ProjectReloadImplementation#checkState} determines if implementations are satisfied with the selected data.
 * <li>If {@link ProjectState} is consistent and all accept()s returned true, finish successfully.
 * <li>otherwise, project metadata are reloaded
 * </ol>
 * The reload procedure itself is done as follows:
 * <ol>
 * <li>{@link #reload} is called with the <b>last obtained</b> {@link ProjectStateData}. 
 * <li>The returned reload data is remembered. 
 * <li>The returned{@code ProjectStateData} will become part of the partial {@link ProjectState} and will be remembered for the possible next round
 * <li>The above repeats for each {@link ProjectReloadImplementation}, as long as {@link LoadContext#retryReload} is not called.
 * <li>If any implementation triggers {@code retryReload}, the above steps repeats.
 * <ul>
 * Any of the {@link ProjectReloadImplementation#reload} may throw an exception or complete its {@link CompletableFuture} exceptionally, aborting
 * the operation. 
 * 
 * @param <D> internal data type.
 * 
 * @author sdedic
 */
public interface ProjectReloadImplementation<D> {
    /**
     * Project state report. It contains
     * <ul>
     * <li>files loaded, that contains project definitions and settings
     * <li>timestamp when the report was created (will be used for change checks)
     * <li>custom metadata or project-related services in Lookup
     * <li>consistency and validity flag
     * </ul>
     * This object is not directly exposed to Reload API clients, multiple ProjectStateData are merged into the final client-facing ProjectState.
     * The reported files and timestamps will be used by the infrastructure to monitor file changes and editor modifications, which will then
     * make the resulting {@link ProjectState} inconsistent; ProjectReloadImplementation does not need to track/monitor files it had reported
     * to the infrastructure.
     * The {@link ProjectReloadImplementation} produces this data using {@link ProjectStateBuilder}, and then may indicate that some changes 
     * happened:
     * <ul>
     * <li>{@link #fireChanged} to indicate that the data is no longer consistent, or no longer valid (i.e. a new state is loaded, or the current one is osbolete for some reason)
     * <li>{@link #fireFileSetChanged} to indicate the set of loaded files has changed.
     * <li>{@link #fireDataInconsistent} to indicate that some data produced by other {@code ProjectStateData} become inconsistent
     * </ul>
     * These change reports will affect the resulting {@link ProjectStateData} and will fire appropriate events to the clients.
     * <p>
     * The implementation can publish arbitrary project metadata in {@link ProjectStateBuilder#data(java.lang.Object)}: non-null values will become part of {@link ProjectState#getLookup}. 
     * Additional data or services may be published in {@link ProjectStateBuilder#attachLookup} and will be merged into the ProjectState's Lookup. API clients will see this data.
     * <p>
     * For bookkeeping, the implementation <b>may attach></b> its private data, e.g. to ensure a listener is not garbage-collected before ProjectStateData goes out of scope. For clean-up,
     * the implementation will be called when the ProjectStateData is released from all ProjectStates:s
     * <ul>
     * <li>The private data <b>may implement {@link Closeable}</b>: in that case, {@link Closeabl#close} will be called
     * <li>{@link #projectDataReleased} will be called to clean up.
     * </ul>
     * The implementation may then detach the ProjectStateData from files, services etc. The release callbacks will NOT be called while project (re)loading is in progress.
     * This structure is not directly exposed to API users, it communicates data and events from
     * SPI to API utility methods.
     */
    public final class ProjectStateData<D> {
        private final Collection<FileObject> files;
        private final Quality quality;
        private final long timestamp;
        
        // The following fields will be cleared upon release, so that referenced data can be GCed.
        private Lookup lookup;
        private D projectData;
        private Object privateData;
        private Throwable error;

        // The following fields are updated by fire* methods.
        private boolean consistent = true;
        private boolean valid;
        private Collection<FileObject> changedFiles;
        private Set<Class> inconsistencies;
        
        private final List<ProjectStateListener> listeners = new ArrayList<>();

        ProjectStateData(Collection<FileObject> files, boolean valid, Quality quality, long timestamp, Lookup lkp, Throwable error, D projectData) {
            this.lookup = lkp;
            this.valid = valid;
            this.files = files;
            this.quality = quality;
            this.timestamp = timestamp;
            this.projectData = projectData;
            this.error = error;
        }

        /**
         * Returns project data that should be part of ProjectState's lookup.
         * @return project data
         */
        public @CheckForNull D getProjectData() {
            return projectData;
        }

        /**
         * Additional project-related data.
         * @return Lookup with project-related data.
         */
        public Lookup getLookup() {
            return lookup;
        }
        
        /**
         * @return Returns the set of files loaded into project metadata.
         */
        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public Collection<FileObject> getFiles() {
            return files;
        }

        /**
         * @return project's data quality
         */
        public Quality getQuality() {
            return quality;
        }

        /**
         * @return timestamp of project's metadata.
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * @return true, if the no new metadata has been loaded yet.
         */
        public boolean isValid() {
            return valid;
        }

        /**
         * @return true, if the metadata is consistent with on-disk state or other
         * configuration sources.
         */
        public boolean isConsistent() {
            return consistent;
        }
        
        private void fire() {
            ChangeListener[] ll;
            synchronized (this) {
                if (listeners.isEmpty()) {
                    return;
                }
                ll = listeners.toArray(new ChangeListener[0]);
            }
            ChangeEvent e = new ChangeEvent(this);
            for (ChangeListener l : ll) {
                l.stateChanged(e);
            }
        }
        
        /**
         * @return the set of changed, added or deleted files.
         */
        public Collection<FileObject> getChangedFiles() {
            return changedFiles == null ? Collections.emptySet() : changedFiles;
        }

        /**
         * Reports a change in the set of files
         * @param files new set of files.
         */
        public void fireFileSetChanged(Collection<FileObject> files) {
            if (this.files != null && files.containsAll(this.files) && this.files.size() == files.size()) {
                return;
            }
            Set<FileObject> n = new HashSet<>(files);
            n.removeAll(this.files);
            if (n.isEmpty()) {
                return;
            }
            synchronized (this) {
                Collection<FileObject> changed = this.changedFiles;
                Collection<FileObject> newChanged;
                
                if (changed != null) {
                    if (changed.containsAll(n)) {
                        return;
                    }
                    newChanged = new HashSet<>(changed);
                } else {
                    newChanged = new HashSet<>();
                }
                newChanged.addAll(files);
                this.changedFiles = newChanged;
                this.consistent = false;
            }
            fire();
        }
        
        /**
         * 
         * @param <X>
         * @return 
         */
        public <X> X getPrivateData() {
            return (X)privateData;
        }
        
        /**
         * Records that a certain data in the project state should be treated as inconsistent.
         * The data may be produced by different {@link ProjectReloadImplementation}. If present
         * in a {@link ProjectState}, the state becomes inconsistent.
         * @param dataClass data that should be made inconsistent.
         */
        public void fireDataInconsistent(Class dataClass) {
            synchronized (this) {
                // during initial data collection and before ProjectState is formed, the inconsistencies
                // must be buffered.
                if (inconsistencies == null) {
                    inconsistencies = new HashSet<>();
                }
                inconsistencies.add(dataClass);
            }
            ProjectStateListener[] ll;
            synchronized (this) {
                if (listeners.isEmpty()) {
                    return;
                }
                ll = listeners.toArray(new ProjectStateListener[0]);
            }
            ChangeEvent e = new ChangeEvent(this);
            for (ProjectStateListener l : ll) {
                l.fireDataInconsistent(this, dataClass);
            }
        }
        
        /**
         * Sets validity and consistency. To make the StateData inconsistent because of a file change or 
         * a new file appearance, use {@link #fireFileSetChanged(java.util.Collection)} instead.
         * @param invalidate true, to mark the data invalid
         * @param inconsistent true, to mark the data inconsistent. 
      */
        public void fireChanged(boolean invalidate, boolean inconsistent) {
            if (invalidate) {
                this.valid = false;
            }
            if (inconsistent) {
                this.consistent = false;
            }
            fire();
        }
        
        Set<Class> getInconsistencies() {
            return inconsistencies;
        }

        synchronized void addListener(ProjectStateListener l) {
            // do not attach to the immutable constant, it is pointless.
            if (this == NOT_LOADED) {
                return;
            }
            this.listeners.add(l);
        }
        
        synchronized void removeListener(ProjectStateListener l) {
            this.listeners.remove(l);
        }
        
        void clear() {
            if (privateData instanceof Closeable) {
                try {
                    ((Closeable)privateData).close();
                } catch (IOException ex) {
                    // ignore
                }
            }
            
            this.projectData = null;
            this.changedFiles = null;
            this.privateData = null;
            this.lookup = null;
            this.projectData = null;
            this.valid = false;
            this.listeners.clear();
        }
        
        public String toString() {
            return "[quality=" + quality + ", consistent=" + consistent + ", valid=" + valid + ", files: " + this.files + "]";
        }
        
        public static ProjectStateBuilder builder(Quality q) {
            return new ProjectStateBuilder(null, q);
        }

        public static ProjectStateBuilder builder(Object k, Quality q) {
            return new ProjectStateBuilder(k, q);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 11 * hash + (int) (this.timestamp ^ (this.timestamp >>> 32));
            hash = 11 * hash + (int) (this.timestamp ^ (this.timestamp >>> 32));
            hash = 11 * hash + Objects.hashCode(this.projectData);
            hash = 11 * hash + Objects.hashCode(this.quality);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ProjectStateData<?> other = (ProjectStateData<?>) obj;
            if (this.timestamp != other.timestamp) {
                return false;
            }
            return  Objects.equals(this.quality, other.quality) &&
                    Objects.equals(this.projectData, other.projectData);
        }
    }
    
    /**
     * Creates a {@link ProjectStateData} instance.
     */
    public static final class ProjectStateBuilder<D> {
        private long time = -1;
        private boolean valid = true;
        private boolean consistent = true;
        private Quality q;
        private Object key;
        private Lookup lkp;
        private Collection<FileObject> files;
        private D data;
        private Throwable error;
        private Function<ProjectStateData<D>, ?> privateDataSupplier;
        
        ProjectStateBuilder(Object key, Quality q) {
            this.key = key;
            this.q = q;
        }
        
        public ProjectStateBuilder error(Throwable t) {
            this.error = t;
            return this;
        }
        
        public ProjectStateBuilder files(FileObject... files) {
            return files(Arrays.asList(files).stream().filter(Objects::nonNull).toList());
        }
        
        public ProjectStateBuilder files(Collection<FileObject> files) {
            if (this.files == null) {
                this.files = Collections.unmodifiableCollection(new LinkedHashSet<>(files));
            } else {
                this.files.addAll(files);
            }
            return this;
        }
        
        public ProjectStateBuilder data(D data) {
            this.data = data;
            return this;
        }
        
        public ProjectStateBuilder timestamp(long time) {
            this.time = time;
            return this;
        }
        
        public ProjectStateBuilder state(boolean valid, boolean consistent) {
            this.valid = valid;
            this.consistent = consistent;
            return this;
        }

        public ProjectStateBuilder attachLookup(Lookup lkp) {
            this.lkp = lkp;
            return this;
        }
        
        /**
         * Ensures that a private data is attached to the {@link ProjectStateData}.
         * This may be a adapter that monitors underlying system and fires events
         * on this ProjectStateData so that the implementation does not need to do
         * its own bookkeeping.
         * <p>
         * If the private data implements {@link Closeable}, its {@link Closeable#close}
         * will be called when the {@link ProjectStateData} instance becomes obsolete
         * (all {@link ProjectStates} linked to it will be collected}.<
         * 
         * @param <X> custom data type
         * @param creator factory for content
         * @return the builder instance.
         */
        public <X> ProjectStateBuilder privateData(Function<ProjectStateData<D>, X> creator) {
            this.privateDataSupplier = creator;
            return this;
        }
        
        public ProjectStateData build() {
            ProjectStateData d = new ProjectStateData(files == null ? Collections.emptyList() : files, valid, q, time, lkp, error, data);
            if (privateDataSupplier != null) {
                d.privateData = privateDataSupplier.apply(d);
            }
            return d;
        }
    }
    
    /**
     * Determines if modifications to the file make the state inconsistent. If the implementation 
     * returns  {@code false}, the infrastructure will not watch out for modified flag on this 
     * file. The implementation must then call {@link ProjectStateData#fireChanged} appropriately.
     * This helper method allows to disable modification checks, if the implementation is able to
     * reload modified data from the memory representation.
     * 
     * @param sd project state
     * @param f file to be checked
     * @return true, if infrastructure should track memory modifications for the file, false otherwise.
     */
    public default boolean checkForModifications(ProjectStateData sd, FileObject f) {
        return true;
    }
    
    /**
     * A special value which indicates the data has not been loaded.
     */
    public static final ProjectStateData NOT_LOADED = ProjectStateData.builder(Quality.NONE).state(false, false).build();

    /**
     * Callback to release resources and listeners attached to ProjectStateData. The method will be called by Reload API
     * after all references to the ProjectStateData is gone.
     * 
     * @param data 
     */
    public default void projectDataReleased(ProjectStateData<D> data) {
        // no op
    }
    
    /**
     * Allows the Implementation to maintain several states in parallel depending on 
     * contents of {@link StateRequest} and provide more data checking.
     * 
     * @param <D> type of state data 
     */
    public interface ExtendedQuery<D> {
        /**
         * Creates a custom key from the request context data. Only override, if your implementation
         * takes additional parameters and loads different sets or metadata for different queries: the default
         * implementation simply returns {@code null} indicating just a single flavor of metadata is supported.
         * <p>
         * The returned non-null Object must properly define {@link Object#hashCode} and {@link Object#equals} 
         * to select cached state information appropriate for the context data.
         * <p>
         * @param context context data to search
         * @return custom key, or {@code null}.
         */
        public Object createVariant(Lookup context);

        /**
         * Checks if the current state request might be solved by the pending one. The implementation should not check quality levels or
         * file timestamps, this is already done by the infrastructure. It should check whatever additional implementation-dependent conditions
         * that may vary between these two requests.
         * <p>
         * The convenience default implementation returns true, meaning there are not any data affecting the decision except file timestamps and
         * overall project quality.
         * 
         * @param pending the request already pending
         * @param current the request about to be executed
         * @return true, if {@code pending} request can satisfy the {@code current} one.
         */
        public boolean satisfies(StateRequest pending, StateRequest current);

        /**
         * Checks if the {@link ProjectStateData} are acceptable for the request. 
         * Before reload is commenced, the cached state is checked against the request. Consistency, required
         * quality level and timestamps are checked by the infrastructure. If all implementations
         * agrees that their data is consistent with the requested information, the cached data is returned
         * without a reload. The default value is {@code true}, the implementation must focus on
         * the implementation-specific request query and verify the cached data contains requested information.
         * <b>
         * Also called, if two instances happen to be created in parallel, to check if the already published 
         * instance is valid.
         * 
         * @param request load request
         * @param current proposed state data
         * @return false, if the project reload should be initiated or the data is unsuitable.
         */
        public boolean checkState(StateRequest request, ProjectStateData<D> data);
    }
    
    /**
     * Context information for project loading. 
     * The LoadContext holds project's last ProjectStateData, either from the last-known
     * state (in the first loading round), or the data from the previous loading round,
     * if the load was restarted. The {@link ProjectReloadImplementation} may store
     * its private data in this LoadContext and reuse them during the loading process.
     * The LoadContext is discarded after load completes.
     * <p>
     * The load may happen several times for one operation, if a participant
     * triggers a retry.
     */
    public static final class LoadContext<D> {
        final Project project;
        final StateRequest request;
        final ProjectStateData<D> originalData;

        volatile boolean reloadRequested;
        
        volatile Object loadContext;
        
        /**
         * This will be cleared after load, just to break possible dangling refs.
         */
        LoadContextImpl impl;
        
        LoadContext(LoadContextImpl impl, Project project, StateRequest request, ProjectStateData<D> original) {
            this.impl = impl;
            this.project = project;
            this.request = request;
            this.originalData = original;
        }
        
        /**
         * Project that should be loaded.
         * @return the project
         */
        public Project getProject() {
            return project;
        }
        
        /**
         * The request that initiated this operation.
         * @return load request
         */
        public StateRequest getRequest() {
            return request;
        }

        /**
         * Provides access to the project information lookup, as it is 
         * incrementally loaded. After each {@link ProjectReloadImplementation} completes,
         * this Lookup will contain contents of that implementation's {@link ProjectStateData}'s Lookup.
         * 
         * @return project state lookup
         */
        public Lookup stateLookup() {
            return impl.partialStateImpl().getLookup();
        }
        
        /**
         * Establish a callback to cancel the operation. If set, the Reload API may invoke its {@link Cancellable#cancel} in
         * response to {@link CompletableFuture#cancel} as an attempt to interrupt the load operation. The Cancellable implementation
         * is required to complete the {@link CompletableFuture} returned from the {@link ProjectReloadImplementation#reload}, but even though
         * this Future completes normally, the operation will be cancelled. After cancel is received, the ProjectReloadImplementation must not
         * replace older cached data (i.e. {@link ProjectStateData}) which will be still monitored for changes.
         * <p>
         * This must be called for each {@link ProjectReloadImplementation#reload} separately; if the load is retried, Cancellable is cleared before
         * the retry attempt starts.
         * <p>
         * In the case a cancel request was already received by Reload API and cancel is pending this method throws {@link CancellationException}; the
         * implementation should handle it and not invoke long running operations.
         * 
         * @param c cancel callback.
         * @throws CancellationException 
         */
        public void setCancellable(Cancellable c) throws CancellationException {
            impl.setCancellable(c);
        }
        
        /**
         * True, if cancel was requested for this operation.
         * @return true, if this operation was cancelled.
         */
        public boolean isCancelled() {
            return impl.getCancelled() != null;
        }
        
        public CancellationException getCancelled() {
            return impl.getCancelled();
        }
        
        /**
         * Returns partial state constructed so far. The state only contains information from
         * {@link ProjectReloadImplementation}s processed so far. Allows to inspect metadata
         * structures and load further parts accordingly.
         * @return partial state.
         */
        public @NonNull ProjectState getPartialState() {
            return impl.partialStateImpl();
        }
        
        /**
         * Returns the state last known at the time the load started.
         * @return the previous project state.
         */
        public @CheckForNull ProjectState getPreviousState() {
            return impl.getOriginalState();
        }
        
        /**
         * Restarts the loading sequence. This can be used if a participant fills in some data that earlier
         * steps were failing on. This request will force <b>all participants</b> to repeat their {@link ProjectReloadImplementation#reload}
         * operation. It is crude, but ensures that everything is loaded anew, i.e. after files are downloaded or environment prepared on
         * a local machine.
         */
        public void retryReload() {
            impl.retryReloadImpl();
        }
        
        /**
         * Marks specific data as inconsistent. The Reloader will check if any of {@link ProjectReloadImplementation}s produces such
         * data and if so, the state produced by that implementor will be marked as inconsistent and the reload will repeat. Other ProjectReloadImplementations
         * may still revalidate their data and step in during reload, but typically need not to be invoked.
         * <p>
         * If no ProjectReloadImplementation 
         * <p>
         * Currently only project data and Lookup from the {@link ProjectStateData} is checked for produced data.
         * @param c type of data to mark as inconsistent.
         */
        public void markForReload(Class c) {
            impl.markForReload(c);
        }
        
        /**
         * Returns the last loaded data. This might be either {@link #getOriginalData} or during a restarted (retried) reload an instance
         * created during previous {@link #reload} invocation.
         * @return last loaded data.
         */
        public @CheckForNull ProjectStateData<D> getProjectData() {
            return (ProjectStateData<D>)impl.getProjectData();
        }
        
        /**
         * Access to {@link ProjectStateData} incorporated into {@linK #getPreviousState}.
         * @return ProjectStateData instance.
         */
        public @CheckForNull ProjectStateData<D> getOriginalData() {
            return originalData;
        }
        
        /**
         * Returns data stored previously during this reload operation by {@link #saveLoadContext}.
         * {@link ProjectReloadImplementation} may 
         */
        public <T> T getLoadContext(Class<T> clazz) {
            return (T)loadContext;
        }
        
        public <T> T ensureLoadContext(Class<T> clazz, Supplier<T> factory) {
            if (loadContext == null) {
                loadContext = factory.get();
            }
            return (T)loadContext;
        }
        
        /**
         * Stores context across reloads.
         * @param ctx the context.
         */
        public <T> void saveLoadContext(T ctx) {
            this.loadContext = ctx;
        }
    }
    
    /**
     * Loads the project metadata, possibly asynchronously. The last known {@link #getProjectData} produced by this implementation is
     * passed as a reference point, if it is known. {@code null} can be passed to indicate that the load should be forced, but the implementation
     * is free to decide on using cached information. File and modification consistency have been already checked by the API.
     * <p>
     * The returned data must reflect the reloaded (or cached) project metadata quality. The same instance ought be
     * from now on returned from {@link #getProjectData} for the same query conditions. 
     * The implementation may return {@code null} to indicate it will not participate on this project loads. In that state, it should also return 
     * {@code null} from {@link #getPojectData}.
     * <p/>
     * The reload operation may be <b>invoked multiple times</b> if some of the participants requests a reload. In that case, the "prevState" will
     * be the data returned from the last {@link #reload} invocation for this request. 
     * <p>
     * If the returned Future completes exceptionally, the project's quality will degrade to Broken. The `lastState` data will be used as implementation's
     * outcome, except for {@link ProjectStateData#getProjectData()} and {@link ProjectStateData#getLookup()}, and BROKEN status will be forced. Consistency
     * and validity set for the `lastState` will be still reflected in the new ProjectState.
     * <b>Do not block the calling thread for long blocking operations or external processes</b>, use a {@link RequestProcessor}.
     */
    public @CheckForNull CompletableFuture<ProjectStateData<D>> reload(Project project, StateRequest request, LoadContext<D> context);

    /**
     * An exception that allows to signal a failure to load project metadata specified by the request.
     * If this exception is thrown by {@link #reload} (or Future completes with that), the project loading operation will continue loading
     * other {@link ProjectReloadImplementation}s, but the operation fails at the end, with the exception
     * reported as {@link #getCause()}.
     * <p>
     * Implementations should use this exception to allow to load as much data as possible, but deny to
     * execute the operations chained after project metadata load.
     */
    public final static class PartialLoadException extends IllegalStateException {
        private final ProjectStateData partialData;
        
        /**
         * Creates an exception with partial data, which should become part of the ProjectState.
         * @param partialData partial data.
         */
        public PartialLoadException(ProjectStateData partialData) {
            this.partialData = partialData;
        }

        /**
         * Creates an exception with partial data, which should become part of the ProjectState.
         * @param partialData project state
         * @param message error message
         * @param cause underlying error
         */
        public PartialLoadException(ProjectStateData partialData, String message, Throwable cause) {
            super(message, cause);
            this.partialData = partialData;
        }

        public ProjectStateData getPartialData() {
            return partialData;
        }
    }
}
