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
package org.netbeans.modules.project.dependency;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.lsp.ResourceModificationException;
import org.netbeans.api.lsp.ResourceOperation;
import org.netbeans.api.lsp.TextDocumentEdit;
import org.netbeans.api.lsp.WorkspaceEdit;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.dependency.reload.ProjectReloadInternal;
import org.netbeans.modules.project.dependency.reload.ProjectReloadInternal.StateParts;
import org.netbeans.modules.project.dependency.reload.ProjectReloadInternal.StateRef;
import org.netbeans.modules.project.dependency.reload.ReloadApiAccessor;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation.ProjectStateData;
import org.openide.*;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.Union2;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Utilities that relate to project (re)loading and refreshes. This API abstracts and wraps
 * different project implementations so they can be used uniformly by clients that need 
 * project information such as classpath or dependencies. The API offers support for following
 * use cases:
 * <ul>
 * <li>determine the quality of project metadata. Differentiate unloadable projects, broken settings
 * and projects that require to download externals.
 * <li>execute action only with 'good enough' project state. Eventually re-read the project.
 * <li>inform that project metadata is obsolete (inconsistent) or even superseded (new version is loaded)
 * <li>fire events when a project (re)loads
 * <li>handle project load including file saving
 * </ul>
 * Project metadata is implementation-dependent (i.e. GradleBaseProject, MavenProject), but bridge modules
 * may transform that into useful information (classpath, dependencies, build actions) usable by the client.
 * The project may suffer from several types of issues ranging from being completely broken, to missing 
 * external artifacts, which is described by {@link Quality}. 
 * <p>
 * The API client may ask that project ({@link #withProjectState}) loads with a minimum quality to carry out an operation. If the project
 * file contents are to be processed (loaded by the project system), the API checks for editor modifications,
 * saving the files (with user confirmation) if necessary. The project information is then (re)loaded.
 * If the load fails, or does not reach the specified minimum quality, the client receives an error otherwise
 * the client's action will be executed. The API allows the client to specify what level of consistency and quality the project needs to have for 
 * action execution.
 * <p>
 * The current state of the project can be obtained from {@link #getProjectState} without actual loading. After the project
 * information loads, older {@link ProjectState}s will be marked as invalid (and appropriate events fired). If a file reported during
 * project load becomes modified in memory, or on the disk, the relevant ProjectStates will become <b>inconsistent</b> and events will
 * be fired. The client should not expect that data contained in {@link ProjectState#getLookup} refresh after a new project state is loaded,
 * though it is implementation and feature-dependent: the client should obtain a new ProjectState for fresh information.
 * <p>
 * Do not keep ProjectState instances unnecessarily: they aggregate potentially large project metadata information. The ProjectReload API
 * performs a reasonable caching with some timeout before it evicts an unused ProjectState from the memory.
 * <p>
 * Further notes:
 * <ul>
 * <li>Multiple requests to load the project may be carried out by an already pending load, provided it loads the same or better information. 
 * <li>A given project can be reloaded just once at a time. Other requests are queued and will be executed in the fifo order.
 * <li>Change events are delivered on a separate thread, in the same order as they were reported. Their processing does not block project loading, but clients may block other clients; 
 * do not perform lengthy blocking or interactive tasks in event handlers.
 * <li>Multiple change events may be coalesced to one: always check ProjectState's properties, not the event.
 * <li>
 * The API provides a threading model for project loading. Project loads happen sequentially, in a non-blocking manner ({@link CompletableFuture} is used). 
 * Events generated when a new project state is created, or when the underlying data is changed are coalesced and serialized, so clients
 * do not get excess notifications. 
 * <li>Events are suppressed during project (re)load to avoid premature queries that would result in
 * nested / additional project (re)loads from greedy clients. 
 * <li>Existing project implementations may fire events through this API.
 * </ul>
 * <b>Implementation note:</b> this is a staging place for this API. When it matures, it will ultimately go to {@code ide.projects}. Use at your own
 * risk !
 * @author sdedic
 */
public final class ProjectReload {
    private static final Logger LOG = Logger.getLogger(ProjectReloadInternal.class.getName());
  
    /**
     * Timeout to coalesce partial state changes into one ProjectState change. Events are
     * for ProjectReload clients are postponed at least this time.
     */
    private static final int STATE_COALESCE_TIMEOUT_MS = 100;
    
    /**
     * Returns a project state report. The method returns the last-known state for the project,
     * If the project has not been yet loaded through this API, the method may return a stub {@link ProjectState}
     * indicating {@link Quality#NONE}, although the underlying project infrastructure might be already initialized.
     * 
     * The project <b>current state</b> can be for example inspected as follows:
     * <div class="nonnormative">
     * {@snippet file="org/netbeans/modules/project/dependency/reload/ProjectReloadExamples.java" region="getProjectStateExample"}
     * </div>
     *
     * @param p the project
     * @return current known ProjectState 
     * @throws ProjectOperationException 
     */
    public static ProjectState getProjectState(Project p) {
        return getProjectState(p, false);
    }
    
    /**
     * Returns the current project's state. If the quality is NONE, the method may attempt to
     * load the project. Loading the project is time-consuming, and this method blocks, 
     * so it is highly advised to use {@link #withProjectState ProjectReload.withProjectState} which runs asynchronously.
     * Calling the method with {@code attemptLoad = true} from the project loading thread itself
     * may cause a deadlock.
     * <p>
     * If {@code attemptLoad} is true, the method may throw {@link ProjectOperationException} since it
     * attempts actually load the project.
     * 
     * @param p the project
     * @param attemptLoad true, if the method should attempt to load the project from NONE quality.
     * @return the project's state.
     * @throws ProjectOperationException from the actual loading process.
     */
    public static ProjectState getProjectState(Project p, boolean attemptLoad) throws ProjectOperationException {
        Pair<StateRef, ProjectState> pair = ProjectReloadInternal.getInstance().getProjectState0(p, Lookup.EMPTY, true);
        if (pair != null) {
            if (!attemptLoad || pair.second().getQuality() != Quality.NONE) {
                return pair.second();
            }
        }
        // do not attempt a blocking load, if 
        boolean blockingLoadAttempted = ProjectReloadInternal.RELOAD_RP.isRequestProcessorThread();
        if (!attemptLoad || blockingLoadAttempted) {
            if (attemptLoad) {
                LOG.log(Level.WARNING, "Attempt to call getProjectState() synchronously from a project loader with no known project state. To avoid a deadlock, NONE is returned.");
                LOG.log(Level.WARNING, "Check the calling operation:", new Throwable());
            }
            return ProjectReloadInternal.getInstance().getProjectState0(p, Lookup.EMPTY, false).second();
        }
        try {
            return withProjectState(p, StateRequest.load().toQuality(Quality.NONE).tryQuality(Quality.SIMPLE).consistent(false).offline()).get();
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof ProjectOperationException) {
                throw (ProjectOperationException)ex.getCause();
            } else {
                throw new ProjectOperationException(p, ProjectOperationException.State.ERROR, ex.getMessage(), ex.getCause());
            }
        } catch (InterruptedException ex) {
            throw new ProjectOperationException(p, ProjectOperationException.State.CANCELLED, ex.getMessage(), ex);
        }
    }
    
    /**
     * Describes the project state. The state describes the {@link Quality} of the project metadata and may carry
     * the metadata itself in {@link #getLookup}:specialized APIs must be used to obtain project or feature-specific information.
     * <p>
     * {@link #getLoadedFiles Files loaded by the project }. Loaded files  may be part of the project, belong to other local 
     * projects or may represent global settings. The API currently does not classify  file types. The state reports if the loaded 
     * files {@link #getChangedFiles() become modified on the disk}, or the set of loaded files change. Added or removed 
     * files will be always reported as modified. Whenever loaded files are modified, or the set of files change, the state becomes
     * inconsistent.
     * <p>
     * The ProjectState reports whether it is still <b>consistent</b>: in general, if any of the files loaded to
     * the project is edited or modified, the {@link ProjectState} becomes inconsistent. The state
     * might eventually become consistent again, if memory modifications are undone or thrown away. Project implementations may
     * define its own consistency rules. It is advised to work with <b>consistent</b> project to make file changes. Inconsistent
     * projects may be still used when computing just display data.
     * <p>
     * The ProjectState becomes <b>invalid</b> if a part or all of the project metadata is loaded anew or is known to be obsolete. Old
     * states are not updated, they only indicate they are no longer valid. {@link ChangeEvent} is fired when the state becomes
     * invalid. After that the client should not expect any other state changes, the state never becomes valid again.
     * <p>
     * Project-type specific models or other information can be accessed from {@link #getLookup getLookup()}.
     */
    public static final class ProjectState {
        private final Project project;
        private final Quality status;
        private final Collection<FileObject> loaded;
        private final long timestamp;
        private final StateParts parts;
        private final Object track;
        
        /**
         * Lazily initialized.
         */
        private volatile Lookup lookup;
        
        /**
         * Modifiable if merging states. May only increase.
         */
        private volatile Quality target;
        
        /**
         * Modified by listeners through package accessor.
         */
        private volatile boolean consistent;
        private volatile Collection<FileObject> edited;
        private volatile Collection<FileObject> modified;
        private volatile boolean valid;
        
        // @GuardedBy(this)
        private List<ChangeListener> changeListeners;
        private List<Reference<ProjectState>> previous;

        ProjectState(Project project, long timestamp, StateParts parts, Quality status, Quality target, boolean consistent, boolean valid, 
                Collection<FileObject> loaded, Collection<FileObject> modified, Collection<FileObject> edited, Object track) {
            this.track = track;
            this.timestamp = timestamp;
            this.valid = valid;
            this.project = project;
            this.status = status;
            this.target = target.isAtLeast(status) ? target : status;
            this.consistent = consistent;
            this.modified = modified;
            this.loaded = loaded;
            this.parts = parts;
            this.edited = edited;
        }

        /**
         * Returns the time when the project was loaded. 
         * @return UTC time value
         */
        public long getTimestamp() {
            return timestamp;
        }
        
        /**
         * Determines if this state is still valid. If part or all of metadata was reloaded or refreshed after this
         * ProjectState was created, this state resets to invalid, firing a {@link ChangeEvent}.
         * @return true, if the state is valid.
         */
        public boolean isValid() {
            return valid;
        }

        /**
         * Provides list of files loaded by the project
         * @return list of files.
         */
        public Collection<FileObject> getLoadedFiles() {
            return Collections.unmodifiableCollection(loaded);
        }

        /**
         * Provides additional, project-specific metadata. Individual project loaders compose the
         * Lookup from their specific data.
         * @return project metadata.
         */
        public Lookup getLookup() {
            if (lookup != null) {
                return lookup;
            }
            Collection fixed = new ArrayList();
            List<Lookup> lkps = new ArrayList<>();
            for (ProjectStateData part : parts.values()) {
                if (part == null) {
                    continue;
                }
                Object pd = part.getProjectData();
                if (pd != null) {
                    fixed.add(pd);
                }
                Lookup partL = part.getLookup();
                if (!(partL == null || partL == Lookup.EMPTY)) {
                    lkps.add(partL);
                }
            }
            Lookup lkp;
            
            if (lkps.isEmpty()) {
                lkp = fixed.isEmpty() ? Lookup.EMPTY : Lookups.fixed(fixed.toArray(Object[]::new));
            } else {
                if (!fixed.isEmpty()) {
                    lkps.add(0, Lookups.fixed(fixed.toArray(Object[]::new)));
                }
                lkp = new ProxyLookup(lkps.toArray(Lookup[]::new));
                
            }
            this.lookup = lkp;
            return lkp;
        }

        /**
         * The project.
         * @return project instance
         */
        public Project getProject() {
            return project;
        }

        /**
         * Reports status of the project metadata.
         * @return status of metadata.
         */
        public Quality getQuality() {
            return status;
        }

        /**
         * Indicates whether metadata is consistent with editors and disk files.
         * @return true, if the metadata seems consistent.
         */
        public boolean isConsistent() {
            return consistent;
        }
        
        /**
         * Identifies loaded files that have been modified since load. It includes
         * all {@link #getEditedFiles()}, but also files modified on disk and newly
         * discovered files not yet loaded.
         * @return set of files modified since load.
         */
        public Collection<FileObject> getChangedFiles() {
            return Collections.unmodifiableCollection(modified);
        }

        /**
         * Identifies loaded files that have been edited. It may report files that 
         * have been saved after edit, or the changes were undone or discarded.
         * @return set of files modified in memory.
         */
        public Collection<FileObject> getEditedFiles() {
            return Collections.unmodifiableCollection(edited);
        }

        /**
         * True, if some of the loaded files was modified, or new files have
         * appeared that were not loaded but should be.
         * @return true, if files were added or modified.
         */
        public boolean isModified() {
            return !modified.isEmpty();
        }
        
        /**
         * Attaches a Listener.
         * @param l listener instance
         */
        public void addChangeListener(ChangeListener l) {
            synchronized (this) {
                if (changeListeners == null) {
                    changeListeners = new ArrayList<>();
                }
                changeListeners.add(l);
            }
        }
        
        /**
         * Removes a listener registered earlier.
         * @param l listener instance
         */
        public void removeChangeListener(ChangeListener l) {
            synchronized (this) {
                if (changeListeners != null) {
                    changeListeners.remove(l);
                }
            }
        }
        
        void fireChange() {
            List<ChangeListener> ll = new ArrayList<>();
            List<ProjectState> olds = null;
            synchronized (this) {
                if (previous != null) {
                    for (Iterator<Reference<ProjectState>> it = this.previous.iterator(); it.hasNext(); ) {
                        Reference<ProjectState> ps = it.next();
                        ProjectState p = ps.get();
                        if (p == null) {
                            it.remove();
                        } else {
                            if (olds == null) {
                                olds = new ArrayList<>();
                            }
                            olds.add(p);
                        }
                    }
                }
                if (changeListeners != null) {
                    ll.addAll(changeListeners);
                } else if (olds == null) {
                    return;
                }
            }
            if (olds != null) {
                for (ProjectState p : olds) {
                    p.fireChange();
                }
            }
            if (!ll.isEmpty()) {
                ChangeEvent e = new ChangeEvent(this);
                ll.forEach(l -> l.stateChanged(e));
            }
        }
        
        /**
         * Description for diagnostic purposes.
         * @return description string.
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(project).
            append("@").append(Integer.toHexString(System.identityHashCode(this))).
            append("[");
            sb.append("quality=").append(getQuality());
            sb.append(", consistent=").append(isConsistent());
            sb.append(", valid=").append(isValid());
            sb.append(", loaded=").append(getLoadedFiles());
            sb.append(", modified=").append(getLoadedFiles());
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * Request to load to a defined project state. The request defines the minimum quality
     * required for the operation to proceed further ({@link #getMinQuality}). If the project
     * metadata fails to reach that minimum quality, the {@link ProjectReload#withProjectState} 
     * will complete exceptionally and a possible {@link CompletableFuture#thenAccept} and other variants
     * will not be called.
     * <p>
     * The request can skip consistency check and will accept existing metadata although the relevant
     * project files may be already modified in memory or even saved and changed. By default, a request will
     * fail if a project file is modified in an editor. The Request may configure the modified files to be 
     * saved prior to project metadata load. The save may require user confirmation.
     * <p>
     * For network/security purposes it is possible to ban connections to the network which may be necessary.
     * In the case the network connection is actually needed to complete the request, it will fail and not
     * proceed with further project operations.
     * <p>
     * In addition to loading the metadata, the request may instruct the implementation to treat the project
     * as trusted, and to record the trust status. Project trust checks are implementation dependent and
     * optional.
     */
    public final static class StateRequest {
        /**
         * Atempted quality
         */
        private Quality tryQuality;
        
        /**
         * Minimum required quality.
         */
        private Quality minQuality;
        
        /**
         * True, if the metadata should be consistent with the buildsystem
         */
        private boolean consistent = true;
        
        /**
         * Force project reload.
         */
        private boolean forceReload;
        
        /**
         * Save modifications, asking the user. If false, fails on modified files.
         */
        private boolean saveModifications;
        
        /**
         * Do not make network requests.
         */
        private boolean offlineOperation;
        
        /**
         * Reason of the reload.
         */
        private String reason;
        
        /**
         * True to grant project trust.
         */
        private boolean grantTrust;
        
        /**
         * Additional context.
         */
        private Lookup context;
        
        StateRequest(Quality quality, boolean forceReload, boolean saveModifications, boolean consistent, boolean offlineOperation) {
            this.minQuality = tryQuality = quality;
            this.forceReload = forceReload;
            this.consistent = consistent;
            this.saveModifications = saveModifications;
            this.offlineOperation = offlineOperation;
        }

        /**
         * Minimum desired quality of the project data. If the project does not load to that quality, the operation fails.
         * @return minimum quality.
         */
        public Quality getMinQuality() {
            return minQuality;
        }

        /**
         * Target desired quality of the project data. It is not required to attempt to load to a better quality,
         * that this. Allows loaders to optimize operations. The loader should not spend any effor to load higher quality
         * than this. This quality must be at least equal to {@link #getMinQuality()}.
         * @return target desired quality
         */
        public Quality getTargetQuality() {
            return tryQuality;
        }

        /**
         * True, if the project should be granted trust. With false value loading of untrusted project might fail.
         * @return true, if trust should be granted.
         */
        public boolean isGrantTrust() {
            return grantTrust;
        }
        
        /**
         * True, if the loaded data should be consistent with files and/or editor buffers. If set to false, 
         * any existing data that satisfy quality levels will be returned, even though it is stale.
         * @return true, if the data should be reloaded after modifications.
         */
        public boolean isConsistent() {
            return consistent;
        }

        /**
         * Returns true, if project load will be forced regardless of the project's current ready state.
         * @return true, if the project load is forced.
         */
        public boolean isForceReload() {
            return forceReload;
        }

        /**
         * Requests to save the modified files. It consistency is requested (= refresh) and some of the project files
         * are modified/unsaved, this indicates the files should be saved. If {@code false}, the operation will fail
         * as there are unsaved changes.
         * @return true, if modifications should be saved.
         */
        public boolean isSaveModifications() {
            return saveModifications;
        }

        /**
         * Returns true, if the operation is restricted to offline mode. If artifact needs to be resolved, the
         * operation fails.
         * @return True, if the operation must be offline. Default false.
         */
        public boolean isOfflineOperation() {
            return offlineOperation;
        }

        /**
         * @return  Human-readable reason for the possible project load. May be displayed in progress indicators.
         */
        public String getReason() {
            return reason;
        }

        /**
         * Sets the minimum project's quality. The project reload Future will complete exceptionally,
         * with {@link ProjectOperationException} and state {@link ProjectOperationException.State#BROKEN},
         * if the reloaded project data does not meet the minimum quality. <b>Also sets the target quality</b>
         * @param q minimal quality.
         * @return this instance
         */
        public StateRequest toQuality(Quality q) {
            this.minQuality = tryQuality = q;
            return this;
        }
        
        /**
         * Attempt to load to this quality. The request will not fail, if the actual quality is lower, but
         * at leaast the {@link #toQuality}.
         * @param q the attempted quality level.
         * @return this instance
         */
        public StateRequest tryQuality(Quality q) {
            this.tryQuality = q;
            if (q.isWorseThan(minQuality)) {
                this.minQuality = q;
            }
            return this;
        }

        /**
         * Permits to trust the project while loading the data. The trust should be recorded
         * by implementations that check project trust before execution. Use with caution !
         * @return this instance.
         */
        public StateRequest grantTrust() {
            this.grantTrust = true;
            return this;
        }
        
        /**
         * Specifies if the metadata should be consistent with build system itself.
         * If the consistency is not required, the project metadata will not be refreshed if
         * the (stale) metadata is good enough.
         * <p>
         * Use {@code consistent(false)} to indicate that stale data is OK.
         * @param c true to ensure consistency (refresh).
         * @return this instance
         */
        public StateRequest consistent(boolean c) {
            this.consistent = c;
            return this;
        }
        
        /**
         * Requests that project is loaded unconditionally. Will be loaded to at least the
         * minimum quality.
         * 
         * @return this instance.
         */
        public StateRequest forceReload() {
            forceReload = true;
            return this;
        }
        
        /**
         * Requests that possible in-memory changes to project files are ingored.
         * @return this instance.
         */
        public StateRequest saveModifications() {
            saveModifications = true;
            return this;
        }
        
        /**
         * Permits online operation, allowing downloads.
         * @return this instance.
         */
        public StateRequest online() {
            offlineOperation = false;
            return this;
        }

        /**
         * Disables online operations. If the project system needs to reach to the 
         * Internet for metadata or artifacts, it should fail the operation.
         * 
         * @return this instance.
         */
        public StateRequest offline() {
            offlineOperation = true;
            return this;
        }
        
        /**
         * Sets a reload reason. This reason may be displayed in progress indicators that might be 
         * displayed during the project load. Set it to a description of the intended operation.
         * @param reason
         * @return 
         */
        public StateRequest reloadReason(String reason) {
            this.reason = reason;
            return this;
        }

        /**
         * Makes a copy of the request. 
         * @return copy of the request.
         */
        public StateRequest copy() {
            StateRequest sr = new StateRequest(minQuality, forceReload, saveModifications, consistent, offlineOperation);
            sr.tryQuality = this.tryQuality;
            return sr;
        }
        
        /**
         * Passes additional context for the request. The Lookup may contain further parameters
         * for project loading that individual implementations may reflect during the operation.
         * <p>
         * The Lookup should be thought of as a snapshot: project implementations is not required to monitor
         * changes in the Lookup, they can just pick data at the start.
         * 
         * @param ctx additional context
         * @return this instance.
         */
        public StateRequest context(Lookup ctx) {
            this.context = ctx;
            return this;
        }

        public Lookup getContext() {
            return context;
        }
        
        /**
         * @return Diagnostic description of the request.
         */
        @Override
        public String toString() {
            return String.format(
                "Request@%s[quality=%s, force=%b, consistent=%b, offline=%b, save=%b, reason:%s]",
                    Integer.toHexString(System.identityHashCode(this)),
                    minQuality.name(), forceReload, consistent, offlineOperation, saveModifications, reason
            );
        }
        
        /**
         * Requests that project is loaded to the desired state. It does not matter, if the project metadata is obsolete, because
         * the project files have been modified or changed on the disk and the project was not reloaded. This should be the default
         * state for read operations. Loads to {@link Quality#SIMPLE SIMPLE} quality.
         * 
         * @return configured request
         */
        public static StateRequest load() {
            return new StateRequest(Quality.SIMPLE, false, false, false, false);
        }
        
        /**
         * Creates a request that attempts to refresh the project, if it has been modified or changed. The request
         * fails, if there are some unsaved files and the request is going to be executed. If the project reloads, 
         * it is permitted to download external content. Loads to {@link Quality#SIMPLE SIMPLE} quality.
         * 
         * @return configured request
         */
        public static StateRequest refresh() {
            return new StateRequest(Quality.SIMPLE, false, false, true, false);
        }

        /**
         * Requests to reload project's metadata and download external content for the project. The project is reloaded
         * even though the current state is still consistent. If some of the project files are modified in memory, the request 
         * will fail. Loads to {@link Quality#SIMPLE SIMPLE} quality.
         * 
         * @return configured request
         */
        public static StateRequest reload() {
            return new StateRequest(Quality.SIMPLE, true, false, true, false);
        }
    }
    
    /**
     * Status describes the quality of project metadata. For some operations, partial data may be sufficient,
     * for other operations the data must be complete and accurate. This represents overall project quality.
     * If the project system supports partial metadata loading, individual parts may have their own quality
     * indicators which are not part of {@link ProjectReload} API.
     * <table border="1">
     * <tr>
     *   <th colspan="4">Summary</th><th>Quality</th><th>Used in request</th><th>Reported in ProjectState</th>
     * </tr>
     * <tr>
     *   <td colspan="4"></td>
     *   <td>{@link #NONE NONE}</td>
     *   <td>No-op, will always succeed with the current state, ignores consistency or validity.</td>
     *   <td>The project has not been loaded yet, or its metadata was GCed.</td>
     * </tr>
     * <tr>
     *   <td rowspan="4" style="writing-mode: vertical-lr; background-color: lightpink;">Misconfiguration</td>
     *   <td rowspan="3" style="writing-mode: vertical-lr;">Missing buildsystem parts</td>
     *   <td rowspan="2" style="writing-mode: vertical-lr;">Fatal errors</td>
     *   <td style="writing-mode: vertical-lr;">Untrusted</td>
     *   <td>{@link #UNTRUSTED UNTRUSTED}</td>
     *   <td>If not trusted, will return at least some information, comparable to {@link FALLBACK}.</td>
     *   <td>The project is not trusted. Loaded information must not require actual execution of project parts (scripts, code), other data is missing.</td>
     * </tr>
     * <tr>
     *   <td rowspan="5"></td>
     *   <td>{@link #FALLBACK FALLBACK}</td>
     *   <td>Fail on untrusted projects. Anything else is OK, no matter how little data I get.</td>
     *   <td>The data of this project is unreliable, based on heuristics. Possibly the project is broken so much  the build system can not load it even partially.</td>
     * </tr>
     * <tr>
     *   <td rowspan="3"></td>
     *   <td>{@link #INCOMPLETE INCOMPLETE}</td>
     *   <td>Partial information is acceptable even though whole sections are missing.</td>
     *   <td>Components of build system are missing, their data cannot be interpreted. Data essential to load the project data is missing (i.e. property values from parent POM)</td>
     * </tr>
     * <tr>
     *   <td rowspan="1"></td>
     *   <td>{@link #BROKEN BROKEN}</td>
     *   <td>Errors in the project are acceptable, structure may be partial.</td>
     *   <td>Project contains errors that prevent build system from operating. Undefined property values, wrong configuration or missing configuration values, 
     *      invalid combination of settings</td>
     * </tr>
     * <tr>
     *   <td colspan="2" rowspan="4" style="writing-mode: vertical-lr; background-color: palegreen;">Project structure OK</td>
     *   <td>{@link #SIMPLE SIMPLE}</td>
     *   <td>Correct project structure is required. Configuration of project operations may be missing.</td>
     *   <td>Project structure and specification of resources is OK. Resources may be missing locally, configuration of project operations may be missing.</td>
     * </tr>
     * <tr>
     *   <td rowspan="3" style="writing-mode: vertical-lr;">Locally available data</td>
     *   <td>{@link #LOADED LOADED}</td>
     *   <td>Require correct project information. Resources need not to be present.</td>
     *   <td>All except external resources is correct and ready to operate.</td>
     * </tr>
     * <tr>
     *   <td rowspan="2" style="writing-mode: vertical-lr;">Externals resolved</td>
     *   <td>{@link #RESOLVED RESOLVED}</td>
     *   <td>Require all resources to be present.</td>
     *   <td>References external resources are available locally.</td>
     * </tr>
     * <tr>
     *   <td>{@link #CONSISTENT CONSISTENT}</td>
     *   <td>Require all resources to be present and metadata consistent with on-disk state.</td>
     *   <td>The data of this project is unreliable, based on heuristics. Possibly the project is broken so much  the build system can not load it even partially.</td>
     * </tr>
     * </table>
     */
    public static enum Quality {
        /**
         * The project metadata has not been loaded yet. Requesting project in NONE status always
         * succeeds and completes immediately, with last-known whatever ProjectState.
         */
        NONE,
        
        /**
         * The project is not trusted. Loaded information must not require actual execution of 
         * project parts (scripts, code), other data is missing.
         */
        UNTRUSTED,
        
        /**
         * The data of this project is unreliable, based on heuristics. Possibly the project 
         * is broken so much the build system can not load it even partially
         */
        FALLBACK,
        
        /**
         * Project is broken. Components of build system are missing. Data essential to load the project data is missing.
         */
        INCOMPLETE,

        /**
         * The project state is broken. The most common cause is a broken configuration
         * of something in the project. Project contains errors that prevent build system from executing operations.
         */
        BROKEN,
        
        /**
         * The project itself for its core structure should be OK. Resources may be missing locally, configuration of 
         * project operations may be missing. Some project actions may be executed.
         */
        SIMPLE,
        
        /**
         * The project metadata has been loaded. External resources may be missing locally. Note that if a plugin is 
         * missing, the project may be in state {@link #BROKEN} or {@link #INCOMPLETE}. n complete with some dependencies locally missing.
         */
        LOADED,
        
        /**
         * Project metadata are complete. External resources are available locally.
         */
        RESOLVED,
        
        /**
         * The project is resolved, and metadata are consistent with the on-disk files.
         */
        CONSISTENT;
        
        /**
         * Determines if this Quality is at least as good as the passed one. Use this check
         * to test, if the project's quality is 'good enough'.
         * @param s the tested quality
         * @return true, if this quality is same or better.
         */
        public boolean isAtLeast(Quality s) {
            return this.ordinal() >= s.ordinal();
        }
        
        /**
         * Determines if this quality is worse than the passed one. Use this check to test 
         * if the project quality does not meet your criteria.
         * @param s the tested quality
         * @return true, if this quality is worse
         */
        public boolean isWorseThan(Quality s) {
            return this.ordinal() < s.ordinal();
        }
    }
    
    /**
     * Ensures the desired project state. 
     * This call ensures the project metadata is up-to-date or forces reload, depending on the {@code stateRequest}. The project infrastructure
     * performs necessary (and permitted) tasks, including artifact download and after the project load completes, the returned {@code Future}
     * becomes completed. If the project metadata is current and satisfy the {@code stateRequest}, the method may return immediately with an already finished
     * {@code CompletableFuture}.
     * <p>
     * If the project reload fails, the returned {@link CompletableFuture} completes exceptionally with the failure as the exception.
     * <p>
     * The {@link StateRequest} may specify level of consistency, whether modified files should be saved and the minimum quality for the subsequent
     * operation to proceed further. If the requirements specified by the request are not met after the reload is complete, the Future completes with {@link ProjectOperationException}
     * and a relevant {@link ProjectOperationException#getState()} error code. 
     * <p>
     * It is possible to request cancel of the project preparation by calling {@link CompletableFuture#cancel} on the returned Future. 
     * The request may be ignored.
     * <p>
     * The following example shows a scenario when we need to ensure the project is loaded or 
     * refreshed, before performing an operation (dependency modification in this case):
     * <div class="nonnormative">
     * {@snippet file="org/netbeans/modules/project/dependency/reload/ProjectReloadExamples.java" region="withProjectStateExample"}
     * </div>
     *
     * @param p the project.
     * @param stateRequest the request.
     * @return future which completes after project reloads
     */
    @NbBundle.Messages({
        "# {0} - project name",
        "# {1} - number of modified files",
        "ERR_ProjectFilesModified=Project {0} has (1) unsaved files.",
        "# {0} - project name",
        "TEXT_RefreshProject=Reloading project {0}",
        "# {0} - project name",
        "TEXT_ConcurrentlyModified=Project {0} has been concurrently modified",
        "# {0} - project name",
        "# {1} - reload reason",
        "# {2} - number of files",
        "CONFIRM_ProjectFileSave={1}: Loading project {0} requires {2} file(s) to be saved. Proceed ?",
        "# {0} - project name",
        "# {1} - file name",
        "ERR_SaveFailed=Error saving file {1}",
        "# {0} - project name",
        "ERR_SaveProjectFailed=Error saving project {1}"
    })
    public static CompletableFuture<ProjectState> withProjectState(Project p, final StateRequest stateRequest) {
        Throwable origin = new Throwable();
        
        LOG.log(Level.FINE, "REQUESTED: Reload {0}, request: {1}", new Object[] { p, stateRequest });
        Pair<ProjectReloadInternal.StateRef, ProjectState> projectData = ProjectReloadInternal.getInstance().getProjectState0(p, 
                stateRequest.getContext() == null ? Lookup.EMPTY : stateRequest.getContext(), false);
        ProjectState lastKnown = projectData.second();
        boolean doReload = ProjectReloadInternal.checkConsistency(lastKnown, 
                ReloadApiAccessor.get().getParts(lastKnown), stateRequest);
        
        // special case if the state matches & is consistent: if the attempted quality is LESS 
        // than this request's target, the ReloadImplementation might give up 
        if (!doReload && lastKnown.target.isAtLeast(stateRequest.getTargetQuality())) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "FINISHED: Reload {0}, request: {1}, state {2} - NOOP, finished", new Object[] { p, stateRequest, lastKnown.toString() });
            }
            return CompletableFuture.completedFuture(lastKnown);
        }
        String reason = stateRequest.getReason();
        if (reason == null) {
            reason = Bundle.TEXT_RefreshProject(projectName(p));
            // configure a default reason, so it is always defined.
            stateRequest.reloadReason(reason);
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Reload {0}, last known state: {1}", new Object[] { p, lastKnown == null ? "null" : lastKnown.toString() });
        }
        
        if (lastKnown.getQuality() == Quality.NONE && stateRequest.isConsistent()) {
            // we do not have ANY state, but there may be files modified known to the project. Let's read to the lowest possible quality:
            LOG.log(Level.FINE, "Reload {0}: Have NONE but need to have files for consistency check", lastKnown);
            CompletableFuture<ProjectState> initialF = withProjectState1(p, StateRequest.load().toQuality(Quality.NONE).consistent(false).offline(), lastKnown, projectData, origin);
            AtomicReference<CompletableFuture> nested = new AtomicReference<>();
            
            CompletableFuture<ProjectState> toReturn = initialF.thenCompose(initS -> {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Reload {0}: got initial state {1}", new Object[] { p, initS.toString() });
                }
                CompletableFuture<ProjectState> n;
                // if the project was loaded to better quality than NONE, retry the whole process, as that one will be returned from getProjectState0 now, and everything will be checked again.
                if (Quality.NONE.isWorseThan(initS.getQuality())) {
                    n = withProjectState(p, stateRequest);
                } else {
                    // quality is still NONE, so avoid recursion and proceed further. The old projectData Pair cannot be used, need to refresh
                    Pair<ProjectReloadInternal.StateRef, ProjectState> newData = ProjectReloadInternal.getInstance().getProjectState0(p, 
                        stateRequest.getContext() == null ? Lookup.EMPTY : stateRequest.getContext(), false);
                    // now the state either contains set of files, or is miserable empty, so what.
                    n = withProjectState1(p, stateRequest, newData.second(), newData, origin);
                }
                nested.set(n);
                return n;
            });
            toReturn.whenComplete((s, e) -> {
                if (e instanceof CompletionException) {
                    e = e.getCause();
                }
                if (e instanceof CancellationException) {
                    // forward the cancel
                    initialF.cancel(true);
                    CompletableFuture n = nested.get();
                    if (n != null) {
                        n.cancel(true);
                    }
                }
            });
            
            return toReturn;
        }
        
        return withProjectState1(p, stateRequest, lastKnown, projectData, origin);
    }

    /**
     * Second phase of project state. Checks in-memory modified files, asks the user to save files and saves them, and then proceeds.
     * lastKnown p the project
     * @param stateRequest request
     * @param ps last-known project state
     * @param projectData
     * @return Future that completes with project state.
     */
    static CompletableFuture<ProjectState> withProjectState1(Project p, StateRequest stateRequest, ProjectState lastKnown, Pair<ProjectReloadInternal.StateRef, ProjectState> projectData, Throwable origin) {    
        final StateRequest fRequest = stateRequest;
        Set<FileObject> nowEdited = new HashSet<>();
        for (FileObject f : lastKnown.getEditedFiles()) {
            if (f.getLookup().lookup(SaveCookie.class) != null) {
                nowEdited.add(f);
            }
        }
        if (!nowEdited.isEmpty() && stateRequest.isConsistent()) {
            if (stateRequest.isSaveModifications()) {
                LOG.log(Level.FINER, "Reload {0}, request: {1} - prompt save files: {2}", new Object[] { p, stateRequest, nowEdited });
                NotifyDescriptor.Confirmation confirm = new NotifyDescriptor.Confirmation(
                        Bundle.CONFIRM_ProjectFileSave(projectName(p), stateRequest.getReason(), lastKnown.getEditedFiles().size()), 
                        stateRequest.getReason(),
                        NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
                // handle the response asynchronously, not blocking EDT.
                return DialogDisplayer.getDefault().notifyFuture(confirm).thenComposeAsync((nd) -> {
                    if (nd.getValue() == NotifyDescriptor.OK_OPTION) {
                        // must save through this API instead of SaveCookie.
                        List<Union2<TextDocumentEdit, ResourceOperation>> documentChanges = new ArrayList<>();
                        final List<FileObject> mods = new ArrayList<>(nowEdited);
                        mods.forEach(f -> {
                            documentChanges.add(Union2.createFirst(new TextDocumentEdit(URLMapper.findURL(f, URLMapper.EXTERNAL).toExternalForm(), Collections.emptyList())));
                        });
                        LOG.log(Level.FINER, "Reload {0}, request: {1} - saving files: {2}", new Object[] { p, fRequest, nowEdited });
                        // PENDING: this is a workaround. In order to work in LSP/server environment, the CLIENT must be asked to save any file.
                        // It would be far better, if this remote scenario was handled by CloneableEditorSupport somehow, but it is not.
                        WorkspaceEdit wkEdit = new WorkspaceEdit(documentChanges);
                        
                        CompletableFuture<ProjectState> f = WorkspaceEdit.applyEdits(Collections.singletonList(wkEdit), true).
                            exceptionallyCompose(t -> {
                                ProjectOperationException pex;
                                LOG.log(Level.FINER, "Reload {0}, request: {1} - save ERROR", new Object[] { p, fRequest });
                                LOG.log(Level.FINER, "Save exception:", t);
                                if (t instanceof ResourceModificationException) {
                                    ResourceModificationException ex = (ResourceModificationException)t;
                                    FileObject failedFile = mods.get(ex.getFailedEditIndex());
                                    pex = new ProjectOperationException(p, ProjectOperationException.State.OUT_OF_SYNC, 
                                        Bundle.ERR_SaveFailed(projectName(p), Collections.singleton(failedFile)));
                                } else {
                                    pex = new ProjectOperationException(p, ProjectOperationException.State.ERROR, 
                                        Bundle.ERR_SaveProjectFailed(projectName(p)));
                                }
                                pex.initCause(t);
                                return CompletableFuture.failedFuture(pex);
                            }).thenCompose((list) -> ProjectReloadInternal.getInstance().withProjectState2(projectData.first(), p, fRequest, origin));
                        return f;
                    } else {
                        LOG.log(Level.FINER, "Reload {0}, request: {1} - save CANCELLED", new Object[] { p, fRequest });
                        // fail with OUT_OF_SYNC
                        ProjectOperationException ex = new ProjectOperationException(p, ProjectOperationException.State.OUT_OF_SYNC, 
                                Bundle.ERR_ProjectFilesModified(projectName(p), lastKnown.getEditedFiles().size()),
                                new HashSet<>(lastKnown.getEditedFiles()));
                        return CompletableFuture.<ProjectState>failedFuture(ex);
                    }
                }, ProjectReloadInternal.RELOAD_RP);
            } else {
                LOG.log(Level.FINER, "Reload {0}, request: {1} - fail on modified files", new Object[] { p, fRequest });
                ProjectOperationException ex = new ProjectOperationException(p, ProjectOperationException.State.OUT_OF_SYNC, 
                        Bundle.ERR_ProjectFilesModified(projectName(p), nowEdited.size()),
                        nowEdited);
                return CompletableFuture.<ProjectState>failedFuture(ex);
            }
        }
        // do the hard stuff with caching.
        return ProjectReloadInternal.getInstance().withProjectState2(projectData.first(), p, stateRequest, origin);
    }
    
    /**
     * Notifiers that dispatch events for individual cached ProjectStates. The events are delayed and coalesced.
     */
    private static final HashMap<ProjectState, RequestProcessor.Task> notifiers = new HashMap<>();
    
    /**
     * Change task id, for diagnostic purposes only.
     */
    private static AtomicInteger eventId = new AtomicInteger(0);

    /**
     * Fires a delayed state change. If a change is fired before the queued one is dispatched, the dispatch
     * is just postponed by another {@link #STATE_COALESCE_TIMEOUT_MS} timeout to avoid multiple changes for
     * fast-changing state.
     * 
     * @param s state to fire events.
     */
    private static void queueStateChange(ProjectState s) {
        AtomicReference<RequestProcessor.Task> cur = new AtomicReference<>();

        class R implements Runnable {
            final int id = eventId.incrementAndGet();
            @Override
            public void run() {
                // Postpone the actual fire until after project is unlocked
                boolean[] processed = new boolean[1];
                ProjectReloadInternal.getInstance().runProjectAction(s.getProject(), () -> {
                    processed[0] = true;
                    synchronized (notifiers) {
                        notifiers.remove(s, cur.get());
                    }
                    LOG.log(Level.FINE, "Firing state change {1} for {0}", new Object[] { s, this });
                    ProjectReloadInternal.RELOAD_RP.post(s::fireChange);
                });
                if (!processed[0]) {
                    LOG.log(Level.FINE, "Postponed state change {1} for {0}", new Object[] { s, this });
                }
            }
            
            @Override
            public String toString() {
                return "" + id;
            }
        }
        synchronized (notifiers) {
            RequestProcessor.Task t = notifiers.get(s);
            if (t != null) {
                if (t.isFinished()) {
                    // if task exists and is finished, it means that R.run() had executed and have just queued the action.
                    // in addition, the runProjectAction did not run the action at all or it did not yet reach the synchronized 
                    // block and did not start to fire events. It is OK just to return, as the fire is going to happen anyway.
                    LOG.log(Level.FINER, "State change {1} for {0} - piggyback on project operation", new Object[] { s, t });
                    return;
                }
            } else {
                t = ProjectReloadInternal.NOTIFIER.create(new R(), false);
                notifiers.put(s, t);
            }
            LOG.log(Level.FINE, "Queue state change {1} for {0}", new Object[] { s, t });
            // schedule or reschedule a pending task.
            t.schedule(STATE_COALESCE_TIMEOUT_MS);
            cur.set(t);
        }
    }
    
    private static String projectName(Project p) {
        return ProjectUtils.getInformation(p).getDisplayName();
    }
    
    static {
        ReloadApiAccessor.set(new ReloadApiAccessor() {
            @Override
            public ProjectState createState(Project project, long timestamp, StateParts parts, Quality status, Quality target, boolean consistent, boolean valid, Collection<FileObject> loaded, Collection<FileObject> modified, Collection<FileObject> edited, Object track) {
                return new ProjectReload.ProjectState(project, timestamp, parts, status, target, consistent, valid, loaded, modified, edited, track);
            }

            @Override
            public void chainPrevious(ProjectState ps, ProjectState old, Collection<ProjectState> collector) {
                if (old == null) {
                    return;
                }
                List<Reference<ProjectState>> olds = new ArrayList<>();
                olds.add(new WeakReference<>(old));
                List<Reference<ProjectState>> ocol;
                synchronized (old) {
                    ocol = old.previous;
                    old.previous = null;
                }
                if (ocol != null) {
                    for (Reference<ProjectState> r : ocol) {
                        ProjectState s = r.get();
                        if (s != null) {
                            collector.add(s);
                            olds.add(r);
                        }
                    }
                }
                synchronized (ps) {
                    if (ps.previous != null) {
                        olds.addAll(ps.previous);
                    }
                    ps.previous = olds;
                }
            }

            /**
             * This deliberately does not check the state for valid = false, and fires unconditionally.
             * It is only used in a situation when a ProjectState replaces another: all previous States fire 
             * so the client gets an alert and hopefully stops using the invalid state.
             */
            @Override
            public void fireInvalid(ProjectState ps) {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "State " + ps + " invalidated", new Throwable());
                }
                ps.valid = false;
                queueStateChange(ps);
            }
            
            @Override
            public void updateProjectState(ProjectReload.ProjectState ps, boolean inconsistent, boolean invalid, Collection<FileObject> modified, Collection<FileObject> edited, ProjectState targetQualityFrom) {
                boolean fire = false;
                synchronized (ps) {
                    if (inconsistent) {
                        fire |= ps.consistent;
                        ps.consistent = false;
                    }
                    if (invalid) {
                        fire |= ps.valid;
                        ps.valid = false;
                    }
                    if (modified != null) {
                        fire = true;
                        ps.modified = modified;
                    }
                    if (edited != null) {
                        fire = true;
                        ps.edited = edited;
                    }
                    if (targetQualityFrom != null && !ps.target.isWorseThan(targetQualityFrom.target)) {
                        ps.target = targetQualityFrom.target;
                        fire = true;
                    }
                }
                if (fire) {
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.log(Level.FINER, "State {0} changed - consistent={1}, valid={2}", new Object[] { ps.toString(), ps.isConsistent(), ps.isValid() });
                    }
                    queueStateChange(ps);
                }
            }

            @Override
            public StateParts getParts(ProjectState state) {
                return state.parts;
            }
        });
    }
}
