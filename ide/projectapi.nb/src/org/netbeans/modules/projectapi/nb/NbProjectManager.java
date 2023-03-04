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

package org.netbeans.modules.projectapi.nb;

import java.awt.EventQueue;
import java.io.IOException;
import java.lang.ref.Reference;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.*;
import org.netbeans.api.project.ProjectManager.Result;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectManagerImplementation;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.Mutex.ExceptionAction;
import org.openide.util.MutexException;
import org.openide.util.Parameters;
import org.openide.util.Union2;
import org.openide.util.WeakSet;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.spi.MutexImplementation;

/**
 * Manages loaded projects.
 * @author Jesse Glick
 */
@ServiceProvider(service = ProjectManagerImplementation.class, position = 1000)
public final class NbProjectManager implements ProjectManagerImplementation {
    
    // XXX need to figure out how to convince the system that a Project object is modified
    // so that Save All and the exit dialog work... could temporarily use a DataLoader
    // which recognizes project dirs and gives them a SaveCookie, perhaps
    // see also #36280
    // (but currently customizers always save the project on exit, so not so high priority)
    
    // XXX change listeners?
    
    private static final Logger LOG = Logger.getLogger(NbProjectManager.class.getName());
    /** logger for timers/counters */
    private static final Logger TIMERS = Logger.getLogger("TIMER.projects"); // NOI18N
    
    private static final Lookup.Result<ProjectFactory> factories =
        Lookup.getDefault().lookupResult(ProjectFactory.class);
    
    public NbProjectManager() {
        factories.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent e) {
                clearNonProjectCache();
            }
        });
    }
    
    private static enum LoadStatus {
        /**
         * Marker for a directory which is known to not be a project.
         */
        NO_SUCH_PROJECT,
        /**
         * Marker for a directory which is known to (probably) be a project but is not loaded.
         */
        SOME_SUCH_PROJECT,
        /**
         * Marker for a directory which may currently be being loaded as a project.
         * When this is the value, other reader threads should wait for the result.
         */
        LOADING_PROJECT;
        
        public boolean is(Union2<Reference<Project>,LoadStatus> o) {
            return o != null && o.hasSecond() && o.second() == this;
        }
        
        public Union2<Reference<Project>,LoadStatus> wrap() {
            return Union2.createSecond(this);
        }
    }

    private final Mutex MUTEX = new Mutex();
    
    /**
     * Cache of loaded projects (modified or not).
     * Also caches a dir which is <em>not</em> a project.
     */
    private final Map<FileObject,Union2<Reference<Project>,LoadStatus>> dir2Proj = new WeakHashMap<FileObject,Union2<Reference<Project>,LoadStatus>>();
    
    /**
     * Set of modified projects (subset of loaded projects).
     */
    private final Set<Project> modifiedProjects = new HashSet<Project>();
    
    private final Set<Project> removedProjects = Collections.synchronizedSet(new WeakSet<Project>());
    
    /**
     * Mapping from projects to the factories that created them.
     */
    private final Map<Project,ProjectFactory> proj2Factory = Collections.synchronizedMap(new WeakHashMap<Project,ProjectFactory>());
    
    /**
     * Checks for deleted projects.
     */
    private final FileChangeListener projectDeletionListener = new ProjectDeletionListener();
    
    /**
     * Whether this thread is currently loading a project.
     */
    private ThreadLocal<Set<FileObject>> loadingThread = new ThreadLocal<Set<FileObject>>();

    /**
     * Callback to ProjectManager.
     */
    private volatile ProjectManagerCallBack callBack;

    @Override
    public void init(@NonNull final ProjectManagerCallBack callBack) {
        Parameters.notNull("callBack", callBack);   //NOI18N
        this.callBack = callBack;
    }

    @NonNull
    @Override
    public Mutex getMutex() {
        return MUTEX;
    }

    @NonNull
    @Override
    public Mutex getMutex(
        final boolean autoSave,
        @NonNull final Project project,
        @NonNull final Project... otherProjects) {
        return new Mutex(new MutexImpl(
            this,
            autoSave,
            project,
            otherProjects));
    }
    
    /**
     * Clear internal state.
     * Useful from unit tests.
     */
    /*test*/ void reset() {
        dir2Proj.clear();
        modifiedProjects.clear();
        proj2Factory.clear();
        removedProjects.clear();
    }
    
    /**
     * Find an open project corresponding to a given project directory.
     * Will be created in memory if necessary.
     * <p>
     * Acquires read access.
     * </p>
     * <p>
     * It is <em>not</em> guaranteed that the returned instance will be identical
     * to that which is created by the appropriate {@link ProjectFactory}. In
     * particular, the project manager is free to return only wrapper <code>Project</code>
     * instances which delegate to the factory's implementation. If you know your
     * factory created a particular project, you cannot safely cast the return value
     * of this method to your project type implementation class; you should instead
     * place an implementation of some suitable private interface into your project's
     * lookup, which would be safely proxied.
     * </p>
     * @param projectDirectory the project top directory
     * @return the project (object identity may or may not vary between calls)
     *         or null if the directory is not recognized as a project by any
     *         registered {@link ProjectFactory}
     *         (might be null even if {@link #isProject} returns true)
     * @throws IOException if the project was recognized but could not be loaded
     * @throws IllegalArgumentException if the supplied file object is null or not a folder
     */
    @Override
    public Project findProject(final FileObject projectDirectory) throws IOException, IllegalArgumentException {
        Parameters.notNull("projectDirectory", projectDirectory);   //NOI18N
        try {
            return getMutex().readAccess(new Mutex.ExceptionAction<Project>() {
                @Override
                public Project run() throws IOException {
                    // Read access, but still needs to synch on the cache since there
                    // may be >1 reader.
                    try {
                        boolean wasSomeSuchProject;
                    synchronized (dir2Proj) {
                        Union2<Reference<Project>,LoadStatus> o;
                        do {
                            o = dir2Proj.get(projectDirectory);
                            if (LoadStatus.LOADING_PROJECT.is(o)) {
                                try {
                                    Set<FileObject> ldng = loadingThread.get();
                                    if (ldng != null && ldng.contains(projectDirectory)) {
                                        throw new IllegalStateException("Attempt to call ProjectManager.findProject within the body of ProjectFactory.loadProject (hint: try using ProjectManager.mutex().postWriteRequest(...) within the body of your Project's constructor to prevent this)"); // NOI18N
                                    }
                                    if (LOG.isLoggable(Level.FINE)) {
                                        LOG.log(Level.FINE, "findProject({0}) in {1}: waiting for LOADING_PROJECT...", new Object[] {projectDirectory, Thread.currentThread().getName()});
                                    }
                                    if (LOG.isLoggable(Level.FINE) && EventQueue.isDispatchThread()) {
                                        LOG.log(Level.WARNING, "loading " + projectDirectory, new IllegalStateException("trying to load a prpject from EQ"));
                                    }
                                    dir2Proj.wait();
                                    if (LOG.isLoggable(Level.FINE)) {
                                        LOG.log(Level.FINE, "findProject({0}) in {1}: ...done waiting for LOADING_PROJECT", new Object[] {projectDirectory, Thread.currentThread().getName()});
                                    }
                                } catch (InterruptedException e) {
                                    LOG.log(Level.INFO, null, e);
                                    return null;
                                }
                            }
                        } while (LoadStatus.LOADING_PROJECT.is(o));
                        assert !LoadStatus.LOADING_PROJECT.is(o);
                        wasSomeSuchProject = LoadStatus.SOME_SUCH_PROJECT.is(o);
                        if (LoadStatus.NO_SUCH_PROJECT.is(o)) {
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.log(Level.FINE, "findProject({0}) in {1}: NO_SUCH_PROJECT", new Object[] {projectDirectory, Thread.currentThread().getName()});
                            }
                            return null;
                        } else if (o != null && !LoadStatus.SOME_SUCH_PROJECT.is(o)) {
                            Project p = o.first().get();
                            if (p != null) {
                                if (LOG.isLoggable(Level.FINE)) {
                                    LOG.log(Level.FINE, "findProject({0}) in {1}: cached project @{2}", new Object[] {projectDirectory, Thread.currentThread().getName(), p.hashCode()});
                                }
                                return p;
                            } else {
                                if (LOG.isLoggable(Level.FINE)) {
                                    LOG.log(Level.FINE, "findProject({0}) in {1}: null project reference", new Object[] {projectDirectory, Thread.currentThread().getName()});
                                }
                            }
                        } else {
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.log(Level.FINE, "findProject({0} in {1}: no entries among {2}", new Object[] {projectDirectory, Thread.currentThread().getName(), dir2Proj});
                            }
                        }
                        // not in cache
                        dir2Proj.put(projectDirectory, LoadStatus.LOADING_PROJECT.wrap());
                        Set<FileObject> ldng = loadingThread.get();
                        if (ldng == null) {
                            ldng = new HashSet<FileObject>();
                            loadingThread.set(ldng);
                        }
                        ldng.add(projectDirectory);
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, "findProject({0}) in {1}: may load new project...", new Object[] {projectDirectory, Thread.currentThread().getName()});
                        }
                    }
                    boolean resetLP = false;
                    try {
                        Project p = createProject(projectDirectory);
                        //Thread.dumpStack();
                        synchronized (dir2Proj) {
                            dir2Proj.notifyAll();
                            if (p != null) {
                                if (LOG.isLoggable(Level.FINE)) {
                                    LOG.log(Level.FINE, "findProject({0}) in {1}: created new project @{2}", new Object[] {projectDirectory, Thread.currentThread().getName(), p.hashCode()});
                                }
                                projectDirectory.addFileChangeListener(projectDeletionListener);
                                dir2Proj.put(projectDirectory, Union2.<Reference<Project>,LoadStatus>createFirst(new TimedWeakReference<Project>(p)));
                                resetLP = true;
                                return p;
                            } else {
                                dir2Proj.put(projectDirectory, LoadStatus.NO_SUCH_PROJECT.wrap());
                                resetLP = true;
                                if (wasSomeSuchProject) {
                                    if (LOG.isLoggable(Level.FINE)) {
                                        LOG.log(Level.FINE, "Directory {0} was initially claimed to be a project folder but really was not", FileUtil.getFileDisplayName(projectDirectory));
                                    }
                                }
                                return null;
                            }
                        }
                    } catch (IOException e) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, "findProject({0}) in {1}: error loading project: {2}", new Object[] {projectDirectory, Thread.currentThread().getName(), e});
                        }
                        // Do not cache the exception. Might be useful in some cases
                        // but would also cause problems if there were a project that was
                        // temporarily corrupted, fP is called, then it is fixed, then fP is
                        // called again (without anything being GC'd)
                        throw e;
                    } finally {
                        loadingThread.get().remove(projectDirectory);
                        if (!resetLP) {
                            // IOException or a runtime exception interrupted.
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.log(Level.FINE, "findProject({0}) in {1}: cleaning up after error", new Object[] {projectDirectory, Thread.currentThread().getName()});
                            }
                            synchronized (dir2Proj) {
                                assert LoadStatus.LOADING_PROJECT.is(dir2Proj.get(projectDirectory)) : dir2Proj.get(projectDirectory);
                                dir2Proj.remove(projectDirectory);
                                dir2Proj.notifyAll(); // make sure other threads can continue
                            }
                        }
                    }
    // Workaround for issue #51911:
    // Log project creation exception here otherwise it can get lost
    // in following scenario:
    // If project creation calls ProjectManager.postWriteRequest() (what for 
    // example FreeformSources.initSources does) and then it throws an 
    // exception then this exception can get lost because leaving read mutex
    // will immediately execute the runnable posted by 
    // ProjectManager.postWriteRequest() and if this runnable fails (what
    // for FreeformSources.initSources will happen because
    // AntBasedProjectFactorySingleton.getProjectFor() will not find project in
    // its helperRef cache) then only this second fail is logged, but the cause - 
    // the failure to create project - is never logged. So, better log it here:
                    } catch (Error e) {
                        LOG.log(Level.FINE, null, e);
                        throw e;
                    } catch (RuntimeException e) {
                        LOG.log(Level.FINE, null, e);
                        throw e;
                    } catch (IOException e) {
                        LOG.log(Level.FINE, null, e);
                        throw e;
                    }
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }
    
    /**
     * Create a project from a given directory.
     * @param dir the project dir
     * @return a project made from it, or null if it is not recognized
     * @throws IOException if there was a problem loading the project
     */
    private Project createProject(FileObject dir) throws IOException {
        assert dir != null;
        assert dir.isFolder();
        assert getMutex().isReadAccess();
        ProjectStateImpl state = new ProjectStateImpl();
        for (ProjectFactory factory : factories.allInstances()) {
            Project p = factory.loadProject(dir, state);
            if (p != null) {
                if (TIMERS.isLoggable(Level.FINE)) {
                    LogRecord rec = new LogRecord(Level.FINE, "Project"); // NOI18N
                    rec.setParameters(new Object[] { p });
                    TIMERS.log(rec);
                }
                proj2Factory.put(p, factory);
                state.attach(p);
                return p;
            }
        }
        return null;
    }
    

    @Override
    public Result isProject(final FileObject projectDirectory) throws IllegalArgumentException {
        Parameters.notNull("projectDirectory", projectDirectory);
        return getMutex().readAccess(new Mutex.Action<Result>() {
            @Override
            public Result run() {
                synchronized (dir2Proj) {
                    Union2<Reference<Project>,LoadStatus> o;
                    do {
                        o = dir2Proj.get(projectDirectory);
                        if (LoadStatus.LOADING_PROJECT.is(o)) {
                            if (EventQueue.isDispatchThread()) {
                                // #183192: permitted false positive; better than blocking EQ
                                return new Result(null);
                            }
                            try {
                                dir2Proj.wait();
                            } catch (InterruptedException e) {
                                LOG.log(Level.INFO, null, e);
                                return null;
                            }
                        }
                    } while (LoadStatus.LOADING_PROJECT.is(o));
                    assert !LoadStatus.LOADING_PROJECT.is(o);
                    if (LoadStatus.NO_SUCH_PROJECT.is(o)) {
                        return null;
                    } else if (o != null) {
                        // Reference<Project> or SOME_SUCH_PROJECT
                        // rather check for result than load project and lookup projectInformation for icon.
                        return checkForProject(projectDirectory);
                    }
                    // Not in cache.
                    dir2Proj.put(projectDirectory, LoadStatus.LOADING_PROJECT.wrap());
                }
                boolean resetLP = false;
                try {
                    Result p = checkForProject(projectDirectory);
                    synchronized (dir2Proj) {
                        resetLP = true;
                        dir2Proj.notifyAll();
                        if (p != null) {
                            dir2Proj.put(projectDirectory, LoadStatus.SOME_SUCH_PROJECT.wrap());
                            return p;
                        } else {
                            dir2Proj.put(projectDirectory, LoadStatus.NO_SUCH_PROJECT.wrap());
                            return null;
                        }
                    }
                } finally {
                    if (!resetLP) {
                        // some runtime exception interrupted.
                        synchronized (dir2Proj) {
                            assert LoadStatus.LOADING_PROJECT.is(dir2Proj.get(projectDirectory));
                            dir2Proj.remove(projectDirectory);
                        }
                    }
                }
            }
        });
    }

    /**
     *
     * @param dir
     * @param preferResult, if false will not actually call the factory methods with populated Results, but
     *                      create dummy ones and use the Result as boolean flag only.
     * @return
     */
    private Result checkForProject(FileObject dir) {
        assert dir != null;
        assert dir.isFolder() : dir;
        assert getMutex().isReadAccess();
        Iterator<? extends ProjectFactory> it = factories.allInstances().iterator();
        while (it.hasNext()) {
            ProjectFactory factory = it.next();
            if (factory instanceof ProjectFactory2) {
                Result res = ((ProjectFactory2)factory).isProject2(dir);
                if (res != null) {
                    return res;
                }
            } else {
                if (factory.isProject(dir)) {
                    return new Result((Icon)null);
                }
            }
        }
        return null;
    }
    
    /**
     * Clear the cached list of folders thought <em>not</em> to be projects.
     * This may be useful after creating project metadata in a folder, etc.
     * Cached project objects, i.e. folders that <em>are</em> known to be
     * projects, are not affected.
     */
    public void clearNonProjectCache() {
        synchronized (dir2Proj) {
            dir2Proj.values().removeAll(Arrays.asList(new Object[] {
                LoadStatus.NO_SUCH_PROJECT.wrap(),
                LoadStatus.SOME_SUCH_PROJECT.wrap(),
            }));
            // XXX remove everything too? but then e.g. AntProjectFactorySingleton
            // will stay while its delegates are changed, which does no good
            // XXX should there be any way to signal that a particular
            // folder should be "reloaded" by a new factory?
        }
    }
    
    private final class ProjectStateImpl implements ProjectState {
        
        private Project p;
        
        void attach(Project p) {
            assert p != null;
            assert this.p == null;
            this.p = p;
        }
        
        @Override
        public void markModified() {
            assert p != null;
            LOG.log(Level.FINE, "markModified({0})", p.getProjectDirectory());
            getMutex().writeAccess(new Mutex.Action<Void>() {
                @Override
                public Void run() {
                    if (proj2Factory.containsKey(p)) {
                        modifiedProjects.add(p);
                    } else {
                        LOG.log(Level.WARNING, "An attempt to call ProjectState.markModified on an unknown project: {0}", p.getProjectDirectory());
                    }
                    return null;
                }
            });
        }

        @Override
        public void notifyDeleted() throws IllegalStateException {
            assert p != null;
            final FileObject dir = p.getProjectDirectory();
            LOG.log(Level.FINE, "notifyDeleted: {0}", dir);
            getMutex().writeAccess(new Mutex.Action<Void>() {
                @Override
                public Void run() {
                    synchronized (dir2Proj) {
                        Union2<Reference<Project>,LoadStatus> o = dir2Proj.get(dir);
                        if (o != null && o.hasFirst() && o.first().get() == p) {
                            dir2Proj.remove(dir);
                        } else {
                            // #194046: project folder was moved, so now points to new project
                            LOG.log(Level.FINE, "notifyDeleted skipping dir2Proj update since {0} @{1} != {2}", new Object[] {p, p.hashCode(), o});
                        }
                    }
                    proj2Factory.remove(p);
                    modifiedProjects.remove(p);
                    if (!removedProjects.add(p)) {
                        LOG.log(Level.WARNING, "An attempt to call notifyDeleted more than once. Project: {0}", dir);
                    }
                    callBack.notifyDeleted(p);
                    return null;
                }
            });
        }

    }
    /**
     * Get a list of all projects which are modified and need to be saved.
     * <p>Acquires read access.
     * @return an immutable set of projects
     */
    public Set<Project> getModifiedProjects() {
        return getMutex().readAccess(new Mutex.Action<Set<Project>>() {
            @Override
            public Set<Project> run() {
                return new HashSet<Project>(modifiedProjects);
            }
        });
    }
    
    /**
     * Check whether a given project is current modified.
     * <p>Acquires read access.
     * @param p a project loaded by this manager
     * @return true if it is modified, false if has been saved since the last modification
     */
    public boolean isModified(final Project p) {
        return getMutex().readAccess(new Mutex.Action<Boolean>() {
            @Override
            public Boolean run() {
                synchronized (dir2Proj) {
                    if (!proj2Factory.containsKey(p)) {
                        LOG.log(Level.WARNING, "Project {0} was already deleted", p);
                    }
                }
                return modifiedProjects.contains(p);
            }
        });
    }
    
    /**
     * Save one project (if it was in fact modified).
     * <p>Acquires write access.</p>
     * <p class="nonnormative">
     * Although the project infrastructure permits a modified project to be saved
     * at any time, current UI principles dictate that the "save project" concept
     * should be internal only - i.e. a project customizer should automatically
     * save the project when it is closed e.g. with an "OK" button. Currently there
     * is no UI display of modified projects; this module does not ensure that modified projects
     * are saved at system exit time the way modified files are, though the Project UI
     * implementation module currently does this check.
     * </p>
     * @param p the project to save
     * @throws IOException if it cannot be saved
     * @see ProjectFactory#saveProject
     */
    public void saveProject(final Project p) throws IOException {
        try {
            getMutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    //removed projects are the ones that cannot be mapped to an existing project type anymore.
                    if (removedProjects.contains(p)) {
                        return null;
                    }
                    if (modifiedProjects.contains(p)) {
                        ProjectFactory f = proj2Factory.get(p);
                        if (f != null) {
                            f.saveProject(p);
                            LOG.log(Level.FINE, "saveProject({0})", p.getProjectDirectory());
                        } else {
                            LOG.log(Level.WARNING, "Project {0} was already deleted", p);
                        }
                        modifiedProjects.remove(p);
                    }
                    return null;
                }
            });
        } catch (MutexException e) {
            //##91398 have a more descriptive error message, in case of RO folders.
            // the correct reporting still up to the specific project type.
            if (!p.getProjectDirectory().canWrite()) {
                throw new IOException("Project folder is not writeable.");
            }
            throw (IOException)e.getException();
        }
    }
    
    /**
     * Save all modified projects.
     * <p>Acquires write access.
     * @throws IOException if any of them cannot be saved
     * @see ProjectFactory#saveProject
     */
    public void saveAllProjects() throws IOException {
        try {
            getMutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    Iterator<Project> it = modifiedProjects.iterator();
                    while (it.hasNext()) {
                        Project p = it.next();
                        ProjectFactory f = proj2Factory.get(p);
                        if (f != null) {
                            f.saveProject(p);
                            LOG.log(Level.FINE, "saveProject({0})", p.getProjectDirectory());
                        } else {
                            LOG.log(Level.WARNING, "Project {0} was already deleted", p);
                        }
                        it.remove();
                    }
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }
    
    /**
     * Checks whether a project is still valid.
     * <p>Acquires read access.</p>
     *
     * @since 1.6
     *
     * @param p a project
     * @return true if the project is still valid, false if it has been deleted
     */
    public boolean isValid(final Project p) {
        return getMutex().readAccess(new Mutex.Action<Boolean>() {
            @Override
            public Boolean run() {
                synchronized (dir2Proj) {
                    return proj2Factory.containsKey(p);
                }
            }
        });
    }
    
    /**
     * Removes cache entries for deleted projects.
     */
    private final class ProjectDeletionListener extends FileChangeAdapter {
        
        public ProjectDeletionListener() {}

        @Override
        public void fileDeleted(FileEvent fe) {
            synchronized (dir2Proj) {
                LOG.log(Level.FINE, "deleted: {0}", fe.getFile());
                final Union2<Reference<Project>, LoadStatus> prjOrLs = dir2Proj.remove(fe.getFile());
                callBack.notifyDeleted((prjOrLs != null && prjOrLs.hasFirst()) ?
                    prjOrLs.first().get() :
                    null);
            }
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            synchronized (dir2Proj) {
                LOG.log(Level.FINE, "renamed: {0}", fe.getFile());
                final Union2<Reference<Project>, LoadStatus> prjOrLs = dir2Proj.remove(fe.getFile());
                callBack.notifyDeleted((prjOrLs != null && prjOrLs.hasFirst()) ?
                    prjOrLs.first().get() :
                    null);
            }
        }
        
    }

    private static final class MutexImpl implements MutexImplementation {

        private final NbProjectManager owner;
        private final boolean autoSave;
        private final Project[] projects;
        private final AtomicInteger writeDepth = new AtomicInteger();

        MutexImpl(
            @NonNull final NbProjectManager owner,
            final boolean autoSave,
            @NonNull final Project project,
            @NonNull final Project... otherProjects) {
            Parameters.notNull("owner", owner);   //NOI18N
            Parameters.notNull("project", project); //NOI18N
            Parameters.notNull("otherProjects", otherProjects); //NOI18N
            this.owner = owner;
            this.autoSave = autoSave;
            this.projects = new Project[1+otherProjects.length];
            this.projects[0] = project;
            System.arraycopy(otherProjects, 0, projects, 1, otherProjects.length);
        }

        @Override
        public boolean isReadAccess() {
            return owner.MUTEX.isReadAccess();
        }

        @Override
        public boolean isWriteAccess() {
            return owner.MUTEX.isWriteAccess();
        }

        @Override
        public void writeAccess(Runnable runnable) {
            owner.MUTEX.writeAccess(wrap(runnable));
        }

        @Override
        public <T> T writeAccess(ExceptionAction<T> action) throws MutexException {
            return owner.MUTEX.writeAccess(wrap(action));
        }

        @Override
        public void readAccess(Runnable runnable) {
            owner.MUTEX.readAccess(wrap(runnable));
        }

        @Override
        public <T> T readAccess(ExceptionAction<T> action) throws MutexException {
            return owner.MUTEX.readAccess(action);
        }

        @Override
        public void postReadRequest(Runnable run) {
            owner.MUTEX.postReadRequest(run);
        }

        @Override
        public void postWriteRequest(Runnable run) {
            owner.MUTEX.postWriteRequest(wrap(run));
        }

        @NonNull
        private Runnable wrap (@NonNull final Runnable r) {
            return autoSave ?
                new Runnable() {
                    @Override
                    public void run() {
                        writeDepth.incrementAndGet();
                        try {
                            r.run();
                        } finally {
                            if(writeDepth.decrementAndGet() == 0) {
                                saveProjects(RuntimeException.class);
                            }
                        }
                    }
                } :
                r;
        }
        private <T> ExceptionAction<T> wrap(@NonNull final ExceptionAction<T> a) {
            return autoSave ?
                new ExceptionAction<T>() {
                    @Override
                    public T run() throws Exception {
                        writeDepth.incrementAndGet();
                        try {
                            return a.run();
                        } finally {
                            if (writeDepth.decrementAndGet() == 0) {
                                saveProjects(IOException.class);
                            }
                        }
                    }
                } :
                a;
        }
        private <E extends Exception> void saveProjects(@NonNull final Class<E> clz) throws E {
            final Queue<Exception> causes = new ArrayDeque<Exception>();
            for (Project prj : projects) {
                try {
                    owner.saveProject(prj);
                } catch (IOException ioe) {
                    causes.add(ioe);
                }
            }
            if (!causes.isEmpty()) {
                try {
                    final E exc = clz.getDeclaredConstructor().newInstance();
                    for (Exception cause : causes) {
                        exc.addSuppressed(cause);
                    }
                    throw  exc;
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }
}
