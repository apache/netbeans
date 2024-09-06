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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.dependency.ProjectReload;
import org.netbeans.modules.project.dependency.ProjectReload.ProjectState;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation.ProjectStateData;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Pair;
import org.openide.util.WeakListeners;

/**
 * Adapter that listens for files and ProjectStateData changes on behalf of
 * ProjectState. When ProjectStateData gets a change event, it pulls
 * contents from the ProjectStateData and sets the linked ProjectState as
 * invalid / inconsistent, if needed. It also compares the set of files
 * loaded into ProjectState with files reported by ProjectStateData; if a
 * new file appears, it sets the ProjectState to inconsistent (as it was not
 * loaded for sure).
 * <p>
 * It also monitors changed files - each file change, deletion or (for
 * directory) addition will result in making ProjectState inconsistent.
 */
class StateDataListener extends FileChangeAdapter implements ProjectStateListener {
    private static final Logger LOG = Logger.getLogger(StateDataListener.class.getName());
    
    final Project p;
    final ProjectReloadInternal.StateRef tracker;
    final ProjectReloadInternal.StateParts parts;
    /**
     * The files currently being watched.
     */
    // @GuardedBy(this)
    Map<FileObject, Collection<ProjectStateData>> watchedFiles;
    /**
     * File listeners. The order is the same as for watchedFiles.
     */
    // @GuardedBy(this)
    Collection<FileChangeListener> fcls = new ArrayList<>();
    /**
     * LookupListeners, looking for SaveCookie do (dis)appear.
     */
    // @GuardedBy(this)
    Collection<Pair<Lookup.Result, LookupListener[]>> lcls = new ArrayList<>();

    public StateDataListener(Project p, ProjectReloadInternal.StateParts parts, ProjectReloadInternal.StateRef ref) {
        this.p = p;
        this.tracker = ref;
        this.parts = parts;
    }

    void init() {
        updateFileListeners();
        for (ProjectReloadImplementation.ProjectStateData<?> d : parts.values()) {
            ReloadSpiAccessor.get().addProjectStateListener(d, this);
        }
    }

    @Override
    public void fireDataInconsistent(ProjectStateData d, Class<?> dataClass) {
        ProjectReload.ProjectState state = this.tracker.get();
        if (state == null) {
            detachListeners();
            return;
        }
        Lookup.Template t = new Lookup.Template<>(dataClass);
        for (ProjectReloadImplementation.ProjectStateData sd : parts.values()) {
            if (sd != null && dataClass.isInstance(sd.getProjectData()) || (sd.getLookup() != null && sd.getLookup().lookupItem(t) != null)) {
                sd.fireChanged(false, true);
            }
        }
    }

    private synchronized boolean updateFileListeners() {
        Map<FileObject, Collection<ProjectStateData>> updatedFiles = new LinkedHashMap<>();
        for (ProjectReloadImplementation.ProjectStateData sd : parts.values()) {
            if (sd != null) {
                Collection<FileObject> c = sd.getFiles();
                c.forEach(f -> updatedFiles.computeIfAbsent(f, f2 -> new ArrayList<>(1)).add(sd));
            }
        }
        Collection<FileObject> obsoletes;
        Collection<FileObject> newFiles = new HashSet<>(updatedFiles.keySet());
        if (this.watchedFiles != null) {
            obsoletes = new HashSet<>(this.watchedFiles.keySet());
            obsoletes.removeAll(updatedFiles.keySet());
            newFiles.removeAll(watchedFiles.keySet());
        } else {
            obsoletes = Collections.emptySet();
        }
        LOG.log(Level.FINER, "{0}: UpdateListeners called. Added: {1}, removed: {2}", new Object[]{p, newFiles, obsoletes});
        if (obsoletes.isEmpty() && newFiles.isEmpty()) {
            // initially, watchedFiles is null, this will update it to empty col.
            this.watchedFiles = updatedFiles;
            return false;
        }
        List<FileChangeListener> listeners = new ArrayList<>();
        Collection<Pair<Lookup.Result, LookupListener[]>> lookupListeners = new ArrayList<>();
        if (!obsoletes.isEmpty()) {
            Iterator<Pair<Lookup.Result, LookupListener[]>> llit = this.lcls.iterator();
            Iterator<FileChangeListener> lit = this.fcls.iterator();
            for (Iterator<FileObject> fit = this.watchedFiles.keySet().iterator(); fit.hasNext();) {
                Pair<Lookup.Result, LookupListener[]> ll = llit.next();
                FileChangeListener l = lit.next();
                FileObject f = fit.next();
                if (obsoletes.contains(f)) {
                    // f.removeFileChangeListener(l);
                    //ll.first().removeLookupListener(ll.second()[0]);
                    f.removeFileChangeListener(this);
                    ll.first().removeLookupListener(ll.second()[1]);
                } else {
                    lookupListeners.add(ll);
                    listeners.add(l);
                }
            }
        } else if (this.watchedFiles != null && !this.watchedFiles.isEmpty()) {
            listeners.addAll(fcls);
        }
        for (FileObject f : newFiles) {
            FileChangeListener l = FileUtil.weakFileChangeListener(this, f);
            //f.addFileChangeListener(l);
            listeners.add(l);
            f.addFileChangeListener(this);
            Lookup.Result<SaveCookie> lr = f.getLookup().lookupResult(SaveCookie.class);
            LookupListener ll2 = e -> this.cookiesChanged(f, e);
            LookupListener ll = WeakListeners.create(LookupListener.class, ll2, lr);
            lookupListeners.add(Pair.of(lr, new LookupListener[]{ll, ll2}));
            //lr.addLookupListener(ll);
            lr.addLookupListener(ll2);
        }
        this.watchedFiles = updatedFiles;
        this.fcls = listeners;
        this.lcls = lookupListeners;
        return true;
    }

    void detachListeners() {
        synchronized (this) {
            if (watchedFiles != null) {
                watchedFiles.keySet().forEach(f -> f.removeFileChangeListener(this));
                watchedFiles.clear();
            }
            lcls.forEach(p -> p.first().removeLookupListener(p.second()[1]));
            // clear all references from here outside to allow GC.
            lcls.clear();
            fcls.clear();
        }
        parts.values().forEach(sd -> ReloadSpiAccessor.get().removeProjectStateListener(sd, this));
    }

    private void reportFile(FileObject f, long t) {
        ProjectReload.ProjectState state = this.tracker.get();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "{0} received file report: {1}, time {2}, file time {4}, state time: {3}", new Object[] { state == null ? "null" : state.toString(), f, t, 
                state == null ? -3 : state.getTimestamp(), f.lastModified().getTime() });
        }
        if (state == null) {
            detachListeners();
            return;
        }
        long t2 = t == -1 ? f.lastModified().getTime() : t;
        if (t2 < state.getTimestamp()) {
            return;
        }
        ReloadApiAccessor.get().updateProjectState(state, true, false, Collections.singleton(f), null, null);
        watchedFiles.getOrDefault(f, Collections.emptyList()).forEach(d -> d.fireChanged(false, true));
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        ProjectReload.ProjectState state = this.tracker.get();
        ProjectReloadImplementation.ProjectStateData d = (ProjectReloadImplementation.ProjectStateData) e.getSource();
        boolean c = d.isConsistent();
        boolean v = d.isValid();
        if (state == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "DETACHING on stateData from {1}", new Object[] { d.toString() });
            }
            detachListeners();
            return;
        }
        Collection<FileObject> obs = new HashSet<>(d.getFiles());
        boolean fire = false;
        boolean invalid = false;
        boolean inconsistent = false;
        Collection<FileObject> setModified = null;
        obs.removeAll(state.getLoadedFiles());
        if (!obs.isEmpty()) {
            Set<FileObject> s = new LinkedHashSet<>(state.getChangedFiles());
            if (s.addAll(obs)) {
                setModified = s;
                inconsistent = true;
                fire = true;
            }
        }
        if (state.isConsistent() && !c) {
            inconsistent = true;
            fire = true;
        }
        if (state.isValid() && !v) {
            invalid = true;
            fire = true;
        }
        if (v) {
            updateFileListeners();
        }
        if (!v) {
            // some of the providers invalidated the data/status
            // invalid states
            detachListeners();
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "{0} received stateData from {1}, updating: {2}", new Object[] { state.toString(), d.toString(), fire });
        }
        if (fire) {
            ReloadApiAccessor.get().updateProjectState(state, inconsistent, invalid, setModified, null, null);
        }
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        reportFile(fe.getFile(), System.currentTimeMillis());
    }

    @Override
    public void fileChanged(FileEvent fe) {
        reportFile(fe.getFile(), -1);
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        reportFile(fe.getFile().getParent(), fe.getFile().lastModified().getTime());
    }

    public void cookiesChanged(FileObject f, LookupEvent ev) {
        ProjectReload.ProjectState state = this.tracker.get();
        if (state == null) {
            detachListeners();
            return;
        }
        Lookup.Result lr = (Lookup.Result) ev.getSource();
        if (!lr.allItems().isEmpty()) {
            Set<FileObject> ed = new HashSet<>(state.getEditedFiles());
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "{0} received SaveCookie on file {1}; present {2}", new Object[] { state.toString(), f, ed.contains(f) });
            }
            if (ed.add(f)) {
                Set<FileObject> ch = new HashSet<>(state.getChangedFiles());
                if (!ch.add(f)) {
                    ch = null;
                }
                ReloadApiAccessor.get().updateProjectState(state, true, false, ch, ed, null);
            }
        }
        watchedFiles.getOrDefault(f, Collections.emptyList()).forEach(d -> d.fireChanged(false, true));
    }
    
}
