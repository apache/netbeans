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

package org.netbeans.modules.project.ui;

import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.ProjectGroup;
import org.netbeans.api.project.ui.ProjectGroupChangeEvent;
import org.netbeans.api.project.ui.ProjectGroupChangeListener;
import static org.netbeans.modules.project.ui.Bundle.*;
import org.netbeans.modules.project.ui.api.UnloadedProjectInformation;
import org.netbeans.modules.project.ui.groups.Group;
import org.netbeans.modules.project.uiapi.ProjectOpenedTrampoline;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectContainerProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.Mutex.Action;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.WindowManager;

/**
 * List of projects open in the GUI.
 * @author Petr Hrebejk
 */
public final class OpenProjectList {
    /** 
     * a mutex protecting just the private parts of this class, 
     * WARNING the mutex read or write access section SHOULD NEVER include anything that eventually aquires ProjectManager.MUTEX
     * otherwise we get a deadlock fairly fast 
     */
    static final Mutex MUTEX = new Mutex();
    
    public static Comparator<Project> projectByDisplayName() {
        return new ProjectByDisplayNameComparator();
    }
    
    // Property names
    public static final String PROPERTY_OPEN_PROJECTS = "OpenProjects";
    public static final String PROPERTY_WILL_OPEN_PROJECTS = "willOpenProjects"; // NOI18N
    public static final String PROPERTY_MAIN_PROJECT = "MainProject";
    public static final String PROPERTY_RECENT_PROJECTS = "RecentProjects";
    public static final String PROPERTY_REPLACE = "ReplaceProject";
    
    private static OpenProjectList INSTANCE;
    
    // number of templates in LRU list
    private static final int NUM_TEMPLATES = 15;
    
    public static final RequestProcessor OPENING_RP = new RequestProcessor("Opening projects", 1);
    private static final RequestProcessor FILE_DELETED_RP = new RequestProcessor(OpenProjectList.class);
    private static final RequestProcessor RP3 = new RequestProcessor(OpenProjectList.class);

    static final Logger LOGGER = Logger.getLogger(OpenProjectList.class.getName());
    static void log(LogRecord r) {
        LOGGER.log(r);
    }
    static void log(Level l, String msg, Object... params) {
        LOGGER.log(l, msg, params);
    }
    static void log(Level l, String msg, Throwable e) {
        LOGGER.log(l, msg, e);
    }


    /** List which holds the open projects */
    private List<Project> openProjects;
    private final HashMap<ModuleInfo, List<Project>> openProjectsModuleInfos;
    
    /** Main project */
    private Project mainProject;
    
    /** List of recently closed projects */
    private final RecentProjectList recentProjects;

    /** LRU List of recently used templates */
    private final List<String> recentTemplates;
    
    /** Property change listeners */
    private final PropertyChangeSupport pchSupport;
    
    private final ProjectDeletionListener deleteListener = new ProjectDeletionListener();
    private final NbProjectDeletionListener nbprojectDeleteListener = new NbProjectDeletionListener();
    
    private final PropertyChangeListener infoListener;
    private final LoadOpenProjects LOAD;
    private final ArrayList<ProjectGroupChangeListener> projectGroupSupport;
    private final AtomicBoolean groupChanging = new AtomicBoolean(false);
    
    OpenProjectList() {
        LOAD = new LoadOpenProjects(0);
        openProjects = new ArrayList<Project>();
        openProjectsModuleInfos = new HashMap<ModuleInfo, List<Project>>();
        infoListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evn) {
                if (ModuleInfo.PROP_ENABLED.equals(evn.getPropertyName())) {
                    checkModuleInfo((ModuleInfo)evn.getSource());
                }
            }
        };
        pchSupport = new PropertyChangeSupport( this );
        recentProjects = new RecentProjectList(10); // #47134
        recentTemplates = new ArrayList<String>();
        projectGroupSupport = new ArrayList<ProjectGroupChangeListener>();
    }
    
           
    // Implementation of the class ---------------------------------------------
    
    public static OpenProjectList getDefault() {
        return MUTEX.readAccess(new Mutex.Action<OpenProjectList>() {
            public @Override OpenProjectList run() {
                synchronized (OpenProjectList.class) { // must be read access, but must not run concurrently
                    if (INSTANCE == null) {
                        INSTANCE = new OpenProjectList();
                        INSTANCE.openProjects = loadProjectList();
                        // Load recent project list
                        INSTANCE.recentProjects.load();
                        WindowManager.getDefault().invokeWhenUIReady(INSTANCE.LOAD);
                    }
                }
                return INSTANCE;
            }
        });
    }
    
    public static void waitProjectsFullyOpen() {
        getDefault().LOAD.waitFinished(0);
    }

    static void preferredProject(final Project lazyP) {
        if (lazyP != null) {
            OPENING_RP.post(new Runnable() {
                @Override public void run() {
                    getDefault().LOAD.preferredProject(Collections.singleton(lazyP.getProjectDirectory()));
                }
            });
        }
    }

    public Future<Project[]> openProjectsAPI() {
        return LOAD;
    }

    final Project unwrapProject(Project wrap) {
        Project[] now = getOpenProjects();

        if (wrap instanceof LazyProject) {
            LazyProject lp = (LazyProject)wrap;
            for (Project p : now) {
                if (lp.getProjectDirectory().equals(p.getProjectDirectory())) {
                    return p;
                }
            }
        }
        return wrap;
    }

    /** Modifications to the recentTemplates variables shall be done only 
     * when holding a lock.
     * @return the list
     */
    private List<String> getRecentTemplates() {
        assert MUTEX.isReadAccess() || MUTEX.isWriteAccess();
        return recentTemplates;
    }
    

    void addProjectGroupChangeListener(ProjectGroupChangeListener listener) {
        synchronized (projectGroupSupport) {
            projectGroupSupport.add(listener);
        }
    }
    
    void removeProjectGroupChangeListener(ProjectGroupChangeListener listener) {
        synchronized (projectGroupSupport) {
            projectGroupSupport.remove(listener);
        }
    }
    
    public void fireProjectGroupChanging(Group oldGroup, Group newGroup) {
        groupChanging();
        List<ProjectGroupChangeListener> list = new ArrayList<ProjectGroupChangeListener>();
        synchronized (projectGroupSupport) {
            list.addAll(projectGroupSupport);
        }
        ProjectGroup o = oldGroup != null ? org.netbeans.modules.project.uiapi.BaseUtilities.ACCESSOR.createGroup(oldGroup.getName(), oldGroup.prefs()) : null;
        ProjectGroup n = newGroup != null ? org.netbeans.modules.project.uiapi.BaseUtilities.ACCESSOR.createGroup(newGroup.getName(), newGroup.prefs()) : null;
        ProjectGroupChangeEvent event = new ProjectGroupChangeEvent(o, n);
        for (ProjectGroupChangeListener l : list) {
            l.projectGroupChanging(event);
        }
    }
    
    public void fireProjectGroupChanged(Group oldGroup, Group newGroup) {
        groupChanged();
        List<ProjectGroupChangeListener> list = new ArrayList<ProjectGroupChangeListener>();
        synchronized (projectGroupSupport) {
            list.addAll(projectGroupSupport);
        }
        ProjectGroup o = oldGroup != null ? org.netbeans.modules.project.uiapi.BaseUtilities.ACCESSOR.createGroup(oldGroup.getName(), oldGroup.prefs()) : null;
        ProjectGroup n = newGroup != null ? org.netbeans.modules.project.uiapi.BaseUtilities.ACCESSOR.createGroup(newGroup.getName(), newGroup.prefs()) : null;
        ProjectGroupChangeEvent event = new ProjectGroupChangeEvent(o, n);
        for (ProjectGroupChangeListener l : list) {
            l.projectGroupChanged(event);
        }
    }

    private void groupChanged() {
        groupChanging.compareAndSet(true, false);
        MUTEX.writeAccess(new Mutex.Action<Void>() {
                public @Override Void run() {
        recentProjects.load();
                    return null;
                }
        });
        pchSupport.firePropertyChange( PROPERTY_RECENT_PROJECTS, null, null );

        MUTEX.writeAccess(new Mutex.Action<Void>() {
                public @Override Void run() {
                    List<String> rt = OpenProjectListSettings.getInstance().getRecentTemplates();
                    getRecentTemplates().clear();
                    getRecentTemplates().addAll(rt);
                    return null;
                }
        });
    }

    private void groupChanging() {
        groupChanging.compareAndSet(false, true);
    }
    
    private final class LoadOpenProjects implements Runnable, LookupListener, Future<Project[]> {
        final RequestProcessor RP = new RequestProcessor("Load Open Projects"); // NOI18N
        final RequestProcessor.Task TASK = RP.create(this);
        private int action;
        private final LinkedList<Project> toOpenProjects = new LinkedList<Project>();
        private List<Project> lazilyOpenedProjects;
        private List<String> recentTemplates;
        private Project lazyMainProject;
        private Lookup.Result<FileObject> currentFiles;
        private int entered;
        private final Lock enteredGuard = new ReentrantLock();
        private final Condition enteredZeroed = enteredGuard.newCondition();
        private final ProgressHandle progress;
        
        @Messages("CAP_Opening_Projects=Opening Projects")
        @SuppressWarnings("LeakingThisInConstructor")
        public LoadOpenProjects(int a) {
            action = a;
            currentFiles = Utilities.actionsGlobalContext().lookupResult(FileObject.class);
            currentFiles.addLookupListener(WeakListeners.create(LookupListener.class, this, currentFiles));
            progress = ProgressHandle.createHandle(CAP_Opening_Projects());
        }

        final boolean waitFinished(long timeout) {
            log(Level.FINER, "waitFinished, action {0}", action); // NOI18N
            if (action == 0) {
                run();
            }
            log(Level.FINER, "waitFinished, before wait"); // NOI18N
            if (timeout == 0) {
                TASK.waitFinished();
            } else {
                try {
                    if (!TASK.waitFinished(timeout)) {
                        return false;
                    }
                } catch (InterruptedException ex) {
                    return false;
                }
            }
            log(Level.FINER, "waitFinished, after wait"); // NOI18N
            return true;
        }
        
        @Override
        public void run() {
            log(Level.FINE, "LoadOpenProjects.run: {0}", action); // NOI18N
            switch (action) {
                case 0: 
                    action = 1;
                    TASK.schedule(0);
                    resultChanged(null);
                    return;
                case 1:
                    if (!RP.isRequestProcessorThread()) {
                        return;
                    }
                    action = 2;
                    try {
                        progress.start();
                        loadOnBackground();
                    } finally {
                        progress.finish();
                    }
                    updateGlobalState();
                    ProjectsRootNode.checkNoLazyNode();
                    Group.projectsLoaded();
                    return;
                case 2:
                    // finished, oK
                    return;
                default:
                    throw new IllegalStateException("unknown action: " + action);
            }
        }

        final void preferredProject(final Set<FileObject> lazyPDirs) {
            OpenProjectList.MUTEX.writeAccess(new Mutex.Action<Void>() {
                public @Override Void run() {
                for (Project p : new ArrayList<Project>(toOpenProjects)) {
                    FileObject dir = p.getProjectDirectory();
                    assert dir != null : "Project has real directory " + p;
                    if (lazyPDirs.contains(dir)) {
                        toOpenProjects.remove(p);
                        toOpenProjects.addFirst(p);
                        return null;
                    }
                }
                    return null;
                }
            });
        }

        private void updateGlobalState() {
            log(Level.FINER, "updateGlobalState"); // NOI18N
            OpenProjectList.MUTEX.writeAccess(new Mutex.Action<Void>() {
                public @Override Void run() {
                INSTANCE.openProjects = lazilyOpenedProjects;
                log(Level.FINER, "openProjects changed: {0}", lazilyOpenedProjects); // NOI18N
                if (lazyMainProject != null) {
                    INSTANCE.mainProject = lazyMainProject;
                }
                INSTANCE.mainProject = unwrapProject(INSTANCE.mainProject);
                INSTANCE.getRecentTemplates().addAll(recentTemplates);
                log(Level.FINER, "updateGlobalState, applied"); // NOI18N
                return null;
            }
            });
            
            INSTANCE.pchSupport.firePropertyChange(PROPERTY_OPEN_PROJECTS, new Project[0], lazilyOpenedProjects.toArray(new Project[0]));
            Project main = INSTANCE.mainProject;
            if (main != null) { // else PROPERTY_MAIN_PROJECT would be fired spuriously
                INSTANCE.pchSupport.firePropertyChange(PROPERTY_MAIN_PROJECT, null, main);
            }

            log(Level.FINER, "updateGlobalState, done, notified"); // NOI18N
        }

        boolean closeBeforeOpen(final Project[] arr) {
            return OpenProjectList.MUTEX.writeAccess(new Mutex.Action<Boolean>() {
                public @Override Boolean run() {
                    NEXT: for (Project p : arr) {
                        FileObject dir = p.getProjectDirectory();
                        for (Iterator<Project> it = toOpenProjects.iterator(); it.hasNext();) {
                            if (dir.equals(it.next().getProjectDirectory())) {
                                it.remove();
                                continue NEXT;
                            }
                        }
                        return false;
                    }
                    return true;
                }
            });
        }

        @NbBundle.Messages({
            "#NOI18N",
            "LOAD_PROJECTS_ON_START=true"
        })
        private void loadOnBackground() {
            lazilyOpenedProjects = new ArrayList<>();
            final boolean loadProjectsOnStart = "true".equals(Bundle.LOAD_PROJECTS_ON_START());
            List<URL> urls = loadProjectsOnStart ?
                    OpenProjectListSettings.getInstance().getOpenProjectsURLs() :
                    Collections.emptyList();
            final List<Project> initial = new ArrayList<>();
            final LinkedList<Project> projects = URLs2Projects(urls);
            OpenProjectList.MUTEX.writeAccess(new Mutex.Action<Void>() {
                public @Override Void run() {
                    toOpenProjects.addAll(projects);
                    log(Level.FINER, "loadOnBackground {0}", toOpenProjects); // NOI18N
                    initial.addAll(toOpenProjects);
                    return null;
            }
            });
            recentTemplates = new ArrayList<String>( OpenProjectListSettings.getInstance().getRecentTemplates() );
            final URL mainProjectURL = OpenProjectListSettings.getInstance().getMainProjectURL();
            int max = OpenProjectList.MUTEX.writeAccess(new Mutex.Action<Integer>() {
                public @Override Integer run() {
                for (Project p : toOpenProjects) {
                    INSTANCE.addModuleInfo(p);
                    // Set main project
                        if ( mainProjectURL != null && 
                             mainProjectURL.equals( p.getProjectDirectory().toURL() ) ) {
                            lazyMainProject = p;
                        }
                }
                return toOpenProjects.size();
                }
            });
            progress.switchToDeterminate(max);
            for (;;) {
                final AtomicInteger openPrjSize = new AtomicInteger();
                Project p = OpenProjectList.MUTEX.writeAccess(new Mutex.Action<Project>() {
                    public @Override Project run() {
                        if (toOpenProjects.isEmpty()) {
                            return null;
                        }
                        Project p = toOpenProjects.remove();
                        log(Level.FINER, "after remove {0}", toOpenProjects); // NOI18N
                        openPrjSize.set(toOpenProjects.size());
                        return p;
                    }
                });
                if (p == null) {
                    break;
                }
                log(Level.FINE, "about to open a project {0}", p); // NOI18N
                if (notifyOpened(p)) {
                    lazilyOpenedProjects.add(p);
                    log(Level.FINE, "notify opened {0}", p); // NOI18N
                    PropertyChangeEvent ev = new PropertyChangeEvent(this, PROPERTY_REPLACE, null, p);
                    try {
                        pchSupport.firePropertyChange(ev);
                    } catch (Throwable t) {
                        log(Level.WARNING, "broken node for {0}", t);
                    }
                    log(Level.FINE, "property change notified {0}", p); // NOI18N
                    ///same as in doOpenProject() but here for initially opened projects
                    p.getProjectDirectory().addFileChangeListener(INSTANCE.deleteListener);
                    p.getProjectDirectory().addFileChangeListener(INSTANCE.nbprojectDeleteListener);
                } else {
                    // opened failed, remove main project if same.
                    if (lazyMainProject == p) {
                        lazyMainProject = null;
                    }
                }
                progress.progress(max - openPrjSize.get());
            }

            if (initial != null) {
                Project[] initialA = initial.toArray(new Project[0]);
                log(createRecord("UI_INIT_PROJECTS", initialA),"org.netbeans.ui.projects");
                log(createRecordMetrics("USG_PROJECT_OPEN", initialA),"org.netbeans.ui.metrics.projects");
            }

        }

        private final RequestProcessor.Task resChangedTask = Hacks.RP.create(new Runnable() {
                public @Override void run() {
                    Set<FileObject> lazyPDirs = new HashSet<FileObject>();
                    for (FileObject fileObject : currentFiles.allInstances()) {
                        Project p = FileOwnerQuery.getOwner(fileObject);
                        if (p != null) {
                            lazyPDirs.add(p.getProjectDirectory());
                        }
                    }
                    if (!lazyPDirs.isEmpty()) {
                        getDefault().LOAD.preferredProject(lazyPDirs);
                    }
                }
            });
        public @Override void resultChanged(LookupEvent ev) {
            resChangedTask.schedule(50);
        }

        final void enter() {
            try {
                enteredGuard.lock();
                entered++;
            } finally {
                enteredGuard.unlock();
            }
        }
    
        final void exit() {
            try {
                enteredGuard.lock();
                if (--entered == 0) {
                    enteredZeroed.signalAll();
                }
            } finally {
                enteredGuard.unlock();
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return TASK.isFinished() && entered == 0;
        }

        @Override
        public Project[] get() throws InterruptedException, ExecutionException {
            waitFinished(0);
            try {
                enteredGuard.lock();
                while (entered > 0) {
                    enteredZeroed.await();
                }
            } finally {
                enteredGuard.unlock();
            }
            return getDefault().getOpenProjects();
        }

        @Override
        public Project[] get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            long ms = unit.convert(timeout, TimeUnit.MILLISECONDS);
            if (!waitFinished(timeout)) {
                throw new TimeoutException();
            } 
            try {
                enteredGuard.lock();
                if (entered > 0) {
                    if (!enteredZeroed.await(ms, TimeUnit.MILLISECONDS)) {
                        throw new TimeoutException();
                    }
                }
            } finally {
                enteredGuard.unlock();
            }
            return getDefault().getOpenProjects();
        }
    }
    
    public void open( Project p ) {
        open( new Project[] {p}, false );
    }

    public void open (Project p, boolean openSubprojects ) {
        open( new Project[] {p}, openSubprojects );
    }

    public void open( Project[] projects, boolean openSubprojects ) {
	open(projects, openSubprojects, false);
    }
    
    public void open(final Project[] projects, final boolean openSubprojects, final boolean asynchronously) {
        open(projects, false, openSubprojects, asynchronously, null);
    }

    public void open(final Project[] projects, boolean prime, final boolean openSubprojects, final boolean asynchronously, final Project/*|null*/ mainProject) {
        if (projects.length == 0) {
            //nothing to do:
            return ;
        }
        
        long start = System.currentTimeMillis();
        
        if (asynchronously) {
            class Cancellation extends AtomicBoolean implements Cancellable {
                Thread t;
                @Override public boolean cancel() {
                    if (t != null) {
                        t.interrupt();
                    }
                    return compareAndSet(false, true);
                }
            }
            final Cancellation cancellation = new Cancellation();
            final ProgressHandle handle = ProgressHandle.createHandle(CAP_Opening_Projects(), cancellation);
            handle.start();
            handle.progress(projects[0].getProjectDirectory().getNameExt());
            OPENING_RP.post(new Runnable() {
                @Override public void run() {
                    cancellation.t = Thread.currentThread();
                    try {
                        open(projects, prime, openSubprojects, handle, cancellation);
                    } finally {
                        handle.finish();
                    }
                    if (mainProject != null && Arrays.asList(projects).contains(mainProject) && openProjects.contains(mainProject)) {
                        setMainProject(mainProject);
                    }
                }
            });
        } else {
            open(projects, prime, openSubprojects, null, null);
            if (mainProject != null && Arrays.asList(projects).contains(mainProject) && openProjects.contains(mainProject)) {
                setMainProject(mainProject);
            }
        }
        
        long end = System.currentTimeMillis();
        
        if (LOGGER.isLoggable(Level.FINE)) {
            log(Level.FINE, "opening projects took: " + (end - start) + "ms");
        }
    }

    @Messages({
        "# {0} - project display name", "OpenProjectList.finding_subprojects=Finding required projects of {0}",
        "# {0} - project path", "OpenProjectList.deleted_project={0} seems to have been deleted."
    })
    public void open(Project[] projects, boolean prime, boolean openSubprojects, ProgressHandle handle, AtomicBoolean canceled) {
        LOAD.waitFinished(0);
            
        List<Project> toHandle = new LinkedList<Project>();

        pchSupport.firePropertyChange(PROPERTY_WILL_OPEN_PROJECTS, null, projects);
        for (Project p : projects) {
            Parameters.notNull("projects", p);
            try {
                Project p2 = ProjectManager.getDefault().findProject(p.getProjectDirectory());
                if (p2 != null) {
                    toHandle.add(p2);
                } else {
                    LOGGER.log(Level.WARNING, "Project in {0} disappeared", p.getProjectDirectory());
                }
                if (prime) {
                    ActionProvider ap = p2.getLookup().lookup(ActionProvider.class);
                    if (ap != null && 
                        Arrays.asList(ap.getSupportedActions()).contains(ActionProvider.COMMAND_PRIME) &&
                        ap.isActionEnabled(ActionProvider.COMMAND_PRIME, p2.getLookup())) {
                        final CountDownLatch[] await = new CountDownLatch[1];
                        ActionProgress awaitPriming = new ActionProgress() {
                            @Override
                            protected void started() {
                                if (await[0] == null) {
                                    await[0] = new CountDownLatch(1);
                                }
                            }

                            @Override
                            public void finished(boolean success) {
                                if (await[0] != null) {
                                    await[0].countDown();
                                }
                            }
                        };
                        Lookup waitAndProject = new ProxyLookup(
                            Lookups.singleton(awaitPriming), p2.getLookup()
                        );
                        ap.invokeAction(ActionProvider.COMMAND_PRIME, waitAndProject);
                        if (await[0] != null) {
                            await[0].await();
                        }
                    }
                }
            } catch (InterruptedException | IOException | IllegalArgumentException ex) {
                LOGGER.log(Level.INFO, "Cannot convert " + p.getProjectDirectory(), ex);
            }
        }
            
        try {
            LOAD.enter();
        boolean recentProjectsChanged = false;
        int  maxWork = 1000;
        double workForSubprojects = maxWork / (openSubprojects ? 2.0 : 10.0);
        double currentWork = 0;
        Collection<Project> projectsToOpen = new LinkedHashSet<Project>();
        
	if (handle != null) {
	    handle.switchToDeterminate(maxWork);
	    handle.progress(0);
	}
        
        Map<Project,Set<? extends Project>> subprojectsCache = new HashMap<Project,Set<? extends Project>>(); // #59098
        while (!toHandle.isEmpty()) {
            if (canceled != null && canceled.get()) {
                break;
            }

            Project p = toHandle.remove(0);
            assert p != null;
            if (!p.getProjectDirectory().isValid()) {
                StatusDisplayer.getDefault().setStatusText(OpenProjectList_deleted_project(FileUtil.getFileDisplayName(p.getProjectDirectory())));
                continue;
            }
            Set<? extends Project> subprojects = openSubprojects ? subprojectsCache.get(p) : Collections.<Project>emptySet();
            boolean recurse = true;
            if (subprojects == null) {
                ProjectContainerProvider pcp = p.getLookup().lookup(ProjectContainerProvider.class);
                if (pcp != null) {
                    if (handle != null) {
                        handle.progress(OpenProjectList_finding_subprojects(ProjectUtils.getInformation(p).getDisplayName()));
                    }
                    ProjectContainerProvider.Result res = pcp.getContainedProjects();
                    subprojects = res.getProjects();
                    if (res.isRecursive()) {
                        recurse = false;
                    }
                } else {
                    SubprojectProvider spp = p.getLookup().lookup(SubprojectProvider.class);
                    if (spp != null) {
                        if (handle != null) {
                            handle.progress(OpenProjectList_finding_subprojects(ProjectUtils.getInformation(p).getDisplayName()));
                        }
                        subprojects = spp.getSubprojects();
                    } else {
                        subprojects = Collections.emptySet();
                    }
                }
                subprojectsCache.put(p, subprojects);
            }
            
            projectsToOpen.add(p);
            
            for (Project sub : subprojects) {
                assert sub != null;
                if (sub != null && /** #224592 we need to test for null sub as some subprojectProvider implementations could be faulty and return null and with final releases assert won't fire */
                    !projectsToOpen.contains(sub) && !toHandle.contains(sub)) {
                    if (recurse) {
                        toHandle.add(sub);
                    } else {
                        projectsToOpen.add(sub);
                    }
                }
            }
            
            double workPerOneProject = (workForSubprojects - currentWork) / (toHandle.size() + 1);
            int lastState = (int) currentWork;
            
            currentWork += workPerOneProject;
            
            if (handle != null && lastState < (int) currentWork) {
                handle.progress((int) currentWork);
            }
        }

        if (projectsToOpen.isEmpty()) {
            return;
        }
        
        double workPerProject = (maxWork - workForSubprojects) / projectsToOpen.size();
        
        final List<Project> oldprjs = new ArrayList<Project>();
        final List<Project> newprjs = new ArrayList<Project>();
            MUTEX.writeAccess(new Mutex.Action<Void>() {
            public @Override Void run() {
                oldprjs.addAll(openProjects);
                return null;
            }
        });
        
        for (Project p: projectsToOpen) {
            if (canceled != null && canceled.get()) {
                break;
            }
            if (handle != null) {
                handle.progress(ProjectUtils.getInformation(p).getDisplayName());
            }
            
            recentProjectsChanged |= doOpenProject(p);
            
            int lastState = (int) currentWork;
            
            currentWork += workPerProject;
            
            if (handle != null && lastState < (int) currentWork) {
                handle.progress((int) currentWork);
            }
        }

        final List<Project> openprjs = new ArrayList<Project>();
        final boolean _recentProjectsChanged = recentProjectsChanged;
        MUTEX.readAccess(new Mutex.Action<Void>() {
            public @Override Void run() {
                openprjs.addAll(openProjects);
                return null;
            }
        });
        final List<UnloadedProjectInformation> openProjectsData = projects2Unloaded(openprjs);
        Thread.interrupted(); // just to clear status

        MUTEX.writeAccess(new Mutex.Action<Void>() {
            public @Override Void run() {
                newprjs.addAll(openProjects);
                saveProjectList(openProjectsData);
                if (_recentProjectsChanged) {
                    recentProjects.save();
                }
                return null;
            }
        });
        
        final boolean recentProjectsChangedCopy = recentProjectsChanged;
        
        LogRecord[] addedRec = createRecord("UI_OPEN_PROJECTS", projectsToOpen.toArray(new Project[0])); // NOI18N
        log(addedRec,"org.netbeans.ui.projects");
        addedRec = createRecordMetrics("USG_PROJECT_OPEN", projectsToOpen.toArray(new Project[0])); // NOI18N
        log(addedRec,"org.netbeans.ui.metrics.projects");
        
        Mutex.EVENT.readAccess(new Action<Void>() {
            @Override
            public Void run() {
                pchSupport.firePropertyChange(PROPERTY_OPEN_PROJECTS, oldprjs.toArray(new Project[0]), newprjs.toArray(new Project[0]));
                if ( recentProjectsChangedCopy ) {
                    pchSupport.firePropertyChange( PROPERTY_RECENT_PROJECTS, null, null );
                }
                
                return null;
            }
        });
        } finally {
            LOAD.exit();
    }
    }
    
    public void close( Project someProjects[], boolean notifyUI) {
        Group act = Group.getActiveGroup();
        close(someProjects, notifyUI, act != null ? act.getName() : null);
    }
       
    public void close( Project someProjects[], boolean notifyUI, String groupName ) {
        boolean doSave = false;
        if (!LOAD.closeBeforeOpen(someProjects)) {
            doSave = true;
            LOAD.waitFinished(0);
        }
        
        final Project[] projects = new Project[someProjects.length];
        for (int i = 0; i < someProjects.length; i++) {
            projects[i] = unwrapProject(someProjects[i]);
        }
        
        
        if (!ProjectUtilities.closeAllDocuments (projects, notifyUI, groupName )) {
            return;
        }
        
        try {
            LOAD.enter();
            ProjectUtilities.WaitCursor.show();
            logProjects("close(): closing project: ", projects);
            final AtomicBoolean mainClosed = new AtomicBoolean();
            final AtomicBoolean someClosed = new AtomicBoolean();
            final List<Project> oldprjs = new ArrayList<Project>();
            final List<Project> newprjs = new ArrayList<Project>();
            final List<Project> notifyList = new ArrayList<Project>();
            MUTEX.writeAccess(new Mutex.Action<Void>() {
                public @Override Void run() {
                oldprjs.addAll(openProjects);
                    for (Project p : projects) {
                    Iterator<Project> it = openProjects.iterator();
                    boolean found = false;
                    while (it.hasNext()) {
                        if (it.next().equals(p)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        continue; // Nothing to remove
                    }
                    if (!mainClosed.get()) {
                        mainClosed.set(isMainProject(p));
                    }
                    // remove the project from openProjects
                    it.remove();
                    removeModuleInfo(p);

                    p.getProjectDirectory().removeFileChangeListener(deleteListener);

                    notifyList.add(p);

                    someClosed.set(true);
                }
                if (someClosed.get()) {
                    newprjs.addAll(openProjects);
                }
                if (mainClosed.get()) {
                    mainProject = null;
                    saveMainProject( mainProject );
                }
                return null;
            }
            });
            if (someClosed.get()) {
                final List<UnloadedProjectInformation> uns = projects2Unloaded(newprjs);
                MUTEX.writeAccess(new Mutex.Action<Void>() {
                public @Override Void run() {
                    saveProjectList(uns);
                    return null;
                }
                });
            }
            if (!notifyList.isEmpty() && !groupChanging.get()) {
                MUTEX.writeAccess(new Mutex.Action<Void>() {
                public @Override Void run() {
                for (Project p : notifyList) {
                    recentProjects.add(p); // #183681: call outside of lock
                }
                recentProjects.save();
                return null;
            }
                });
            }
            //#125750 not necessary to call notifyClosed() under synchronized lock.
            LOAD.enter();
            OPENING_RP.post(new Runnable() { // #177427 - this can be slow, better to do asynch
                @Override
                public void run() {
                    try {
                        for (Project closed : notifyList) {
                            notifyClosed(closed);
                        }
                    } finally {
                        LOAD.exit();
                    }
                }
            });
            logProjects("close(): openProjects == ", openProjects.toArray(new Project[0])); // NOI18N
            if (someClosed.get()) {
                pchSupport.firePropertyChange(PROPERTY_OPEN_PROJECTS,
                                oldprjs.toArray(new Project[0]), newprjs.toArray(new Project[0]) );
            }
            if (mainClosed.get()) {
                pchSupport.firePropertyChange( PROPERTY_MAIN_PROJECT, null, null );
            }
            if (someClosed.get()) {
                pchSupport.firePropertyChange( PROPERTY_RECENT_PROJECTS, null, null );
            }
            if (doSave) {
                // Noticed in #72006: save them, in case e.g. editor stored bookmarks when receiving PROPERTY_OPEN_PROJECTS.
                for (int i = 0; i < projects.length; i++) {
                    if (projects[i] instanceof LazyProject) {
                        //#147819 we need to ignore lazyProjects when saving, oh well.
                        continue;
                    }
                    try {
                        ProjectManager.getDefault().saveProject(projects[i]);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
            LogRecord[] removedRec = createRecord("UI_CLOSED_PROJECTS", projects); // NOI18N
            log(removedRec, "org.netbeans.ui.projects");
            removedRec = createRecordMetrics("USG_PROJECT_CLOSE", projects); // NOI18N
            log(removedRec, "org.netbeans.ui.metrics.projects");
        } finally {
            ProjectUtilities.WaitCursor.hide();
            LOAD.exit();
        }
    }

    @NonNull
    public Project[] getOpenProjects() {
        return MUTEX.readAccess(new Mutex.Action<Project[]>() {
            public @Override Project[] run() {
                return openProjects.toArray(new Project[0]);
            }
        });
    }
    
    public boolean isOpen(final Project p) {
        return MUTEX.readAccess(new Mutex.Action<Boolean>() {
            public @Override Boolean run() {
                for (Project cp : openProjects) {
                    //check for folder quity is necessary because of the lazy projects initially populating the list
                    if (p.getProjectDirectory().equals(cp.getProjectDirectory())) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public boolean isMainProject(final Project p) {
        return MUTEX.readAccess(new Mutex.Action<Boolean>() {
            public @Override Boolean run() {
                if (mainProject != null && p != null && mainProject.getProjectDirectory().equals(p.getProjectDirectory())) {
                    return true;
                } else {
                    return false;
                }
            }
        });
    }
    
    public Project getMainProject() {
        return MUTEX.readAccess(new Mutex.Action<Project>() {
            public @Override Project run() {
                return mainProject;
            }
        });
    }
    
    public void setMainProject( Project project ) {
        LOGGER.log(Level.FINER, "Setting main project: {0}", project); // NOI18N
        logProjects("setMainProject(): openProjects == ", openProjects.toArray(new Project[0])); // NOI18N
        //called here to avoid wrapping projectManager.MUTEX within PrivateMutex.MUTEX
                //#139965 the project passed in here can be different from the current one.
                // eg when the ManProjectAction shows a list of opened projects, it lists the "non-loaded skeletons"
                // but when the user eventually selects one, the openProjects list already might hold the 
                // correct loaded list.
                try {
        final Project prj = project != null ? ProjectManager.getDefault().findProject(project.getProjectDirectory()) : null;
        final String dn = project != null ? ProjectUtils.getInformation(project).getDisplayName() : "<none>";
        
            MUTEX.writeAccess(new Mutex.Action<Void>() {
            public @Override Void run() {
            Project main = prj;
            if (main != null && !openProjects.contains(main)) {
                        boolean fail = true;
                        for (Project p : openProjects) {
                            if (p.equals(main)) {
                                fail = false;
                                break;
                            }
                            if (p instanceof LazyProject) {
                                if (p.getProjectDirectory().equals(main.getProjectDirectory())) {
                                    main = p;
                                    fail = false;
                                    break;
                                }
                            }
                        }
                        if (fail) {
                            LOGGER.log(Level.WARNING, "Project {0} is not open and cannot be set as main.", dn);
                            logProjects("setMainProject(): openProjects == ", openProjects.toArray(new Project[0])); // NOI18N
                            return null;
                        }
            }
        
            mainProject = main;
            saveMainProject(main);
            return null;
        }
        });
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
        pchSupport.firePropertyChange( PROPERTY_MAIN_PROJECT, null, null );
    }
    
    public List<Project> getRecentProjects() {
        return MUTEX.readAccess(new Mutex.Action<List<Project>>() {
            @Override
            public List<Project> run() {
                return recentProjects.getProjects();
            }
        });
    }
    
    public boolean isRecentProjectsEmpty() {
        return MUTEX.readAccess(new Mutex.Action<Boolean>() {
            @Override
            public Boolean run() {
                return recentProjects.isEmpty();
            }
        });         
    }
    
    public List<UnloadedProjectInformation> getRecentProjectsInformation() {
        return MUTEX.readAccess(new Mutex.Action<List<UnloadedProjectInformation>>() {
            @Override
            public List<UnloadedProjectInformation> run() {
                return recentProjects.getRecentProjectsInfo();
            }
        });
    }
    
    /** As this class is singletnon, which is not GCed it is good idea to 
     *add WeakListeners or remove the listeners properly.
     */
    
    public void addPropertyChangeListener( PropertyChangeListener l ) {
        pchSupport.addPropertyChangeListener( l );        
    }
    
    public void removePropertyChangeListener( PropertyChangeListener l ) {
        pchSupport.removePropertyChangeListener( l );        
    }

               
    // Used from NewFile action        
    public List<DataObject> getTemplatesLRU( @NullAllowed Project project,  PrivilegedTemplates priv ) {
        List<FileObject> pLRU = getTemplateNamesLRU( project,  priv );
        List<DataObject> templates = new ArrayList<DataObject>();
        // Using folder is preferred option
        try {     
            FileObject fo = FileUtil.getConfigFile( "Templates/Other/Folder" ); //NOI18N
            if ( fo != null ) {
                DataObject dobj = DataObject.find( fo );
                templates.add(dobj);
                pLRU.remove(fo);
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        for( Iterator<FileObject> it = pLRU.iterator(); it.hasNext(); ) {
            FileObject fo = it.next();
            if ( fo != null ) {
                try {
                    DataObject dobj = DataObject.find( fo );                    
                    templates.add( dobj );
                }
                catch ( DataObjectNotFoundException e ) {
                    it.remove();
                    org.openide.ErrorManager.getDefault().notify( org.openide.ErrorManager.INFORMATIONAL, e );
                }
            }
            else {
                it.remove();
            }
        }
        
        return templates;
    }
        
    
    // Used from NewFile action    
    public void updateTemplatesLRU(final FileObject template) {
        MUTEX.writeAccess(new Mutex.Action<Void>() {
            public @Override Void run() {
        
        String templateName = template.getPath();
        
        getRecentTemplates().remove(templateName);
        getRecentTemplates().add( 0, templateName );
        
        if ( getRecentTemplates().size() > 100 ) {
            getRecentTemplates().remove( 100 );
        }
        
        OpenProjectListSettings.getInstance().setRecentTemplates( new ArrayList<String>( getRecentTemplates() )  );
                return null;
            }
        });
    }
    
    
    // Package private methods -------------------------------------------------

    // Used from ProjectUiModule
    static void shutdown() {
        if (INSTANCE != null) {
            try {
                //a bit on magic here. We want to do the goup document persistence before notifyClosed in hope of the 
                // ant projects saving their project data before being closed. (ant ptojects call saveProjct() in the openclose hook.
                // the caller of this method calls saveAllProjectt() later. 
                Group.onShutdown(new HashSet<Project>(INSTANCE.openProjects));
                for (Project p : INSTANCE.openProjects) {                    
                    notifyClosed(p);                    
                }
            } catch (ConcurrentModificationException x) {
                LOGGER.log(Level.INFO, "#198097: could not get list of projects to close", x);
            }
        }
    }
        
    // Used from OpenProjectAction
    public static Project fileToProject( File projectDir ) {
        
        try {
            
            FileObject fo = FileUtil.toFileObject(projectDir);
            if (fo != null && /* #60518 */ fo.isFolder()) {
                return ProjectManager.getDefault().findProject(fo);
            } else {
                return null;
            }
                        
        }
        catch ( IOException e ) {
            /* Ignore; will be reported e.g. by ProjectChooserAccessory:
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
             */
            return null;
        }
        
    }
    
    
    
    // Private methods ---------------------------------------------------------
    
    private static LinkedList<Project> URLs2Projects( Collection<URL> URLs ) {
        LinkedList<Project> result = new LinkedList<Project>();
            
        for(URL url: URLs) {
            FileObject dir = URLMapper.findFileObject( url );
            if ( dir != null && dir.isFolder() ) {
                try {
                    Project p = ProjectManager.getDefault().findProject( dir );
                    if ( p != null && !result.contains(p)) { //#238093, #238811 if multiple entries point to the same project we end up with the same instance multiple times in the linked list. That's wrong.
                        result.add( p );
                    }
                }       
                catch ( Throwable t ) {
                    //something bad happened during loading the project.
                    //log the problem, but allow the other projects to be load
                    //see issue #65900
                    if (t instanceof ThreadDeath) {
                        throw (ThreadDeath) t;
                    }
                    
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);
                }
            }
        }
        
        return result;
    }
    
    private static List<URL> projects2URLs( Collection<Project> projects ) {
        ArrayList<URL> URLs = new ArrayList<URL>( projects.size() );
        for(Project p: projects) {
                URL root = p.getProjectDirectory().toURL();
                if ( root != null ) {
                    URLs.add( root );
                }
        }        
        
        return URLs;
    }
    
    
    private static boolean notifyOpened(Project p) {
        boolean ok = true;
        for (ProjectOpenedHook hook : p.getLookup().lookupAll(ProjectOpenedHook.class)) {
            try {
                ProjectOpenedTrampoline.DEFAULT.projectOpened(hook);
            } catch (RuntimeException e) {
                log(Level.WARNING, null, e);
                // Do not try to call its close hook if its open hook already failed:
                INSTANCE.openProjects.remove(p);
                INSTANCE.removeModuleInfo(p);
                ok = false;
            } catch (Error e) {
                log(Level.WARNING, null, e);
                INSTANCE.openProjects.remove(p);
                INSTANCE.removeModuleInfo(p);
                ok = false;
            }
        }
        if (System.getProperty("test.whitelist.stage") == null) { // NOI18N
            // disable warming up of templates when running ide.kit/test/whitelist
            prepareTemplates(p, p.getLookup());
        }
        return ok;
    }
    
    private static void notifyClosed(Project p) {
        for (ProjectOpenedHook hook : p.getLookup().lookupAll(ProjectOpenedHook.class)) {
            try {
                ProjectOpenedTrampoline.DEFAULT.projectClosed(hook);
            } catch (RuntimeException e) {
                log(Level.WARNING, null, e);
            } catch (Error e) {
                log(Level.WARNING, null, e);
            }
        }
    }

    /** @see #prepareTemplates */
    public static final class TemplateItem {
        public final DataObject template;
        public final String displayName;
        public final Icon icon;
        TemplateItem(DataObject template, String displayName, Icon icon) {
            this.template = template;
            this.displayName = displayName;
            this.icon = icon;
        }
    }
    public static List<TemplateItem> prepareTemplates(@NullAllowed Project project, @NonNull Lookup lookup) {
        // check the action context for recommmended/privileged templates..
        PrivilegedTemplates privs = lookup.lookup(PrivilegedTemplates.class);
        final List<TemplateItem> items = new ArrayList<TemplateItem>();
        for (DataObject template : OpenProjectList.getDefault().getTemplatesLRU(project, privs)) {
            Node delegate = template.getNodeDelegate();
            items.add(new TemplateItem(template, delegate.getDisplayName(), ImageUtilities.image2Icon(delegate.getIcon(BeanInfo.ICON_COLOR_16x16))));
        }
        return items;
    }

    private boolean doOpenProject(final @NonNull Project p) {
        LOGGER.log(Level.FINER, "doOpenProject: {0}", p);
        final AtomicBoolean alreadyOpen = new AtomicBoolean();
        boolean recentProjectsChanged = MUTEX.writeAccess(new Mutex.Action<Boolean>() {
            public @Override Boolean run() {
            log(Level.FINER, "already opened: {0} ", openProjects);
            for (Project existing : openProjects) {
                if (p.equals(existing) || existing.equals(p)) {
                    alreadyOpen.set(true);
                    return false;
                }
            }
            openProjects.add(p);
            addModuleInfo(p);
            //initially opened projects need to have these listeners also added.
            p.getProjectDirectory().addFileChangeListener(deleteListener);
            p.getProjectDirectory().addFileChangeListener(nbprojectDeleteListener);
            
            return recentProjects.remove(p);
        }
        });
        if (alreadyOpen.get()) {
            return false;
        }
        logProjects("doOpenProject(): openProjects == ", openProjects.toArray(new Project[0])); // NOI18N
        // Notify projects opened
        notifyOpened(p);

        OPENING_RP.post(new Runnable() {
            public @Override void run() {
                ProjectUtilities.openProjectFiles(p);
            }
        });
        
        return recentProjectsChanged;
    }
    
    private static List<Project> loadProjectList() {
        assert MUTEX.isReadAccess() || MUTEX.isWriteAccess();
        List<URL> URLs = OpenProjectListSettings.getInstance().getOpenProjectsURLs();
        List<String> names = OpenProjectListSettings.getInstance().getOpenProjectsDisplayNames();
        List<ExtIcon> icons = OpenProjectListSettings.getInstance().getOpenProjectsIcons();
        List<Project> projects = new ArrayList<Project>();
        
        Iterator<URL> urlIt = URLs.iterator();
        Iterator<String> namesIt = names.iterator();
        Iterator<ExtIcon> iconIt = icons.iterator();
        
        while(urlIt.hasNext() && namesIt.hasNext() && iconIt.hasNext()) {
            projects.add(new LazyProject(urlIt.next(), namesIt.next(), iconIt.next()));
        }
        
        //List<Project> projects = URLs2Projects( URLs );
        
        return projects;
    }
    
    private static List<UnloadedProjectInformation> projects2Unloaded( List<Project> projects ) {
        assert !MUTEX.isReadAccess() && !MUTEX.isWriteAccess(); //using ProjectUtils.getInformation() - aquires project mutex
        List<UnloadedProjectInformation> toRet = new ArrayList<UnloadedProjectInformation>();
        for (Project p : projects) {
            ProjectInformation prjInfo = ProjectUtils.getInformation(p);
            URL u = p.getProjectDirectory().toURL();
            if (u != null) {
                toRet.add(ProjectInfoAccessor.DEFAULT.getProjectInfo(prjInfo.getDisplayName(), prjInfo.getIcon(), u));
            }
        }
        return toRet;
    }
  
  
    private static void saveProjectList( List<UnloadedProjectInformation> projects ) {
        assert MUTEX.isWriteAccess();
        List<URL> URLs = new ArrayList<URL>();
        List<String> names = new ArrayList<String>();
        List<ExtIcon> icons = new ArrayList<ExtIcon>();
        for (UnloadedProjectInformation p : projects) {
            names.add(p.getDisplayName());
            URLs.add(p.getURL());
            ExtIcon extIcon = new ExtIcon();
            extIcon.setIcon(p.getIcon());
            icons.add(extIcon);
        }
        OpenProjectListSettings.getInstance().setOpenProjectsURLs( URLs );
        OpenProjectListSettings.getInstance().setOpenProjectsDisplayNames(names);
        OpenProjectListSettings.getInstance().setOpenProjectsIcons(icons);
    }
    
    private static void saveMainProject( Project mainProject ) { 
        assert MUTEX.isWriteAccess();
            URL mainRoot = mainProject == null ? null : mainProject.getProjectDirectory().toURL();
            OpenProjectListSettings.getInstance().setMainProjectURL( mainRoot );
    }
        
    private ArrayList<FileObject> getTemplateNamesLRU( @NullAllowed final Project project, PrivilegedTemplates priv ) {
        // First take recently used templates and try to find those which
        // are supported by the project.
        
        final ArrayList<FileObject> result = new ArrayList<FileObject>(NUM_TEMPLATES);
        
        PrivilegedTemplates pt = priv != null ? priv : project != null ? project.getLookup().lookup( PrivilegedTemplates.class ) : null;
        String ptNames[] = pt == null ? null : pt.getPrivilegedTemplates();        
        final ArrayList<String> privilegedTemplates = new ArrayList<String>( Arrays.asList( pt == null ? new String[0]: ptNames ) );
        final ArrayList<String> toRemove = new ArrayList<String>();
        if (priv == null) {
            // when the privileged templates are part of the active lookup,
            // do not mix them with the recent templates, but use only the privileged ones.
            // eg. on Webservices node, one is not interested in a recent "jsp" file template..
            
            MUTEX.readAccess(new Mutex.Action<Void>() { //#201355 changed from writeAccess to readAccess no apparent data modification going on with exception of 
                                                                         //invalid recent templates removal.. postpone that to a later async time
                public @Override Void run() {
                String[] rtNames = getRecommendedTypes(project);
                
                Iterator<String> it = getRecentTemplates().iterator();
                for( int i = 0; i < NUM_TEMPLATES && it.hasNext(); i++ ) {
                    String templateName = it.next();
                    FileObject fo = FileUtil.getConfigFile( templateName );
                    if ( fo == null ) {
                        toRemove.add(templateName);
                        // Does not exists remove
                    }
                    else if ( isRecommended( project, rtNames, fo ) ) {
                        result.add( fo );
                        privilegedTemplates.remove( templateName ); // Not to have it twice
                    }
                }
                return null;
            }
            });
        }
        if (!toRemove.isEmpty()) {
            //remove obsolete templates async to avoid using writeAccess in main body
            RP3.post(new Runnable() {

                @Override
                public void run() {
                    OpenProjectList.MUTEX.writeAccess(new Mutex.Action<Void>() { //#201355 changed from writeAccess to readAccess no apparent data modification going on.                
                        public @Override Void run() {
                            getRecentTemplates().removeAll(toRemove);
                            return null;
                        }
                    });
                }
            });
        }
        
        // If necessary fill the list with the rest of privileged templates
        Iterator<String> it = privilegedTemplates.iterator();
        for( int i = result.size(); i < NUM_TEMPLATES && it.hasNext(); i++ ) {
            String path = it.next();
            FileObject fo = FileUtil.getConfigFile( path );
            if ( fo != null ) {
                result.add( fo );
            }
        }
                
        return result;
               
    }

    static boolean isRecommended(@NonNull String[] recommendedTypes, @NonNull FileObject primaryFile) {
        if (recommendedTypes.length == 0) {
            // if no recommendedTypes are supported (i.e. freeform) -> disaply all templates
            return true;
        }
        
        Object o = primaryFile.getAttribute ("templateCategory"); // NOI18N
        if (o != null) {
            assert o instanceof String : primaryFile + " attr templateCategory = " + o;
            List<String> recommendedTypesList = Arrays.asList(recommendedTypes);
            for (String category : getCategories((String) o)) {
                if (recommendedTypesList.contains (category)) {
                    return true;
                }
            }
            return false;
        } else {
            // issue 44871, if attr 'templateCategorized' is not set => all is ok
            // no category set, ok display it
            return true;
        }
    }

    static boolean isRecommended(@NullAllowed Project project, @NonNull String[] recommendedTypes, @NonNull FileObject primaryFile) {
        if (project != null) {
            return isRecommended(recommendedTypes, primaryFile);
        }

        if (primaryFile.isFolder()) {
            // folders of templates do not require a project for display
            return true;
        }

        Object requireProject = primaryFile.getAttribute("requireProject");
        return Boolean.FALSE.equals(requireProject);
    }

    /**
     * Returns list of recommended template types for project. Do not call in
     * loop because it may scan project files to resolve its type which is time
     * consuming.
     */
    static @NonNull String[] getRecommendedTypes(@NullAllowed Project project) {
        if (project == null) {
            return new String[0];
        }
        RecommendedTemplates rt = project.getLookup().lookup(RecommendedTemplates.class);
        return rt == null ? new String[0] : rt.getRecommendedTypes();
    }
    
    private static List<String> getCategories (String source) {
        ArrayList<String> categories = new ArrayList<String> ();
        StringTokenizer cattok = new StringTokenizer (source, ","); // NOI18N
        while (cattok.hasMoreTokens ()) {
            categories.add (cattok.nextToken ().trim ());
        }
        return categories;
    }
    
    // Private innerclasses ----------------------------------------------------
    
    /** Maintains recent project list
     */    
    private class RecentProjectList {
       
        private final List<ProjectReference> recentProjects;
        private final List<UnloadedProjectInformation> recentProjectsInfos;
        
        private final int size;
        
        /**
         *@size Max number of the project list.
         */
        public RecentProjectList( int size ) {
            this.size = size;
            recentProjects = new ArrayList<ProjectReference>( size );
            recentProjectsInfos = new ArrayList<UnloadedProjectInformation>(size);
            if (LOGGER.isLoggable(Level.FINE)) {
                log(Level.FINE, "created a RecentProjectList: size=" + size);
            }
        }
        
        public void add(final Project p) {
            final UnloadedProjectInformation projectInfo;
            // #183681: call outside of lock
                projectInfo = ProjectInfoAccessor.DEFAULT.getProjectInfo(
                        ProjectUtils.getInformation(p).getDisplayName(),
                        ProjectUtils.getInformation(p).getIcon(),
                        p.getProjectDirectory().toURL());
            OpenProjectList.MUTEX.writeAccess(new Mutex.Action<Void>() {
                public @Override Void run() {
                int index = getIndex(p);
                if (index == -1) {
                    // Project not in list
                    if (LOGGER.isLoggable(Level.FINE)) {
                        log(Level.FINE, "add new recent project: " + p);
                    }
                    if (recentProjects.size() == size) {
                        // Need some space for the newly added project
                        recentProjects.remove(size - 1);
                        recentProjectsInfos.remove(size - 1);
                    }
                } else {
                    LOGGER.log(Level.FINE, "re-add recent project: {0} @{1}", new Object[] {p, index});
                    // Project is in list => just move it to first place
                    recentProjects.remove(index);
                    recentProjectsInfos.remove(index);
                }
                recentProjects.add(0, new ProjectReference(p));
                recentProjectsInfos.add(0, projectInfo);
                return null;
            }
            });
        }
        
        public boolean remove(final Project p) {
            return OpenProjectList.MUTEX.writeAccess(new Mutex.Action<Boolean>() {
                public @Override Boolean run() {
            int index = getIndex( p );
            if ( index != -1 ) {
                LOGGER.log(Level.FINE, "remove recent project: {0} @{1}", new Object[] {p, index});
                recentProjects.remove( index );
                recentProjectsInfos.remove(index);
                return true;
            }
            return false;
                }
            });
        }

        public void refresh() {
            FILE_DELETED_RP.post(new Runnable() {
                @Override
                public void run() {
                    final List<ProjectReference> refs = new ArrayList<ProjectReference>();
                    final List<UnloadedProjectInformation> unloadedRefs = new ArrayList<UnloadedProjectInformation>();
                    final List<ProjectReference> refsToRemove = new ArrayList<ProjectReference>();
                    final List<UnloadedProjectInformation> unloadedRefsToRemove = new ArrayList<UnloadedProjectInformation>();
                    
                    //this is split into readMutex-noMutex-WriteMutex section because we want to avoid the situation when OPL.Mutex is wrapping
                    //projectManager.Mutex that could prove to be a major source of deadlocks in the codebase.
                    
                    OpenProjectList.MUTEX.readAccess(new Runnable() {
                        @Override
                        public void run() {
                            assert recentProjects.size() == recentProjectsInfos.size();
                            refs.addAll(recentProjects);
                            unloadedRefs.addAll(recentProjectsInfos);
                        }
                    });
                    Iterator<ProjectReference> recentProjectsIter = refs.iterator();
                    Iterator<UnloadedProjectInformation> recentProjectsInfosIter = unloadedRefs.iterator();
                    while (recentProjectsIter.hasNext() && recentProjectsInfosIter.hasNext()) {
                        ProjectReference prjRef = recentProjectsIter.next();
                        UnloadedProjectInformation unloaded = recentProjectsInfosIter.next();
                        URL url = prjRef.getURL();
                        FileObject prjDir = URLMapper.findFileObject(url);
                        ProjectManager.Result prj = null;
                        if (prjDir != null && prjDir.isFolder()) {
                            prj = ProjectManager.getDefault().isProject2(prjDir); //#230545 avoid loadProject(), can take a lot of time and will halt other threads because running under writemutex
                        }

                        if (prj == null) { // externally deleted project probably
                            if (prjDir != null && prjDir.isFolder()) {
                                prjDir.removeFileChangeListener(nbprojectDeleteListener);
                            }
                            refsToRemove.add(prjRef);
                            unloadedRefsToRemove.add(unloaded);
                        }
                    }
                    if (!refsToRemove.isEmpty()) {
                        OpenProjectList.MUTEX.writeAccess(new Runnable() {
                            @Override
                            public void run() {
                                boolean changed = recentProjects.removeAll(refsToRemove);
                                changed = recentProjectsInfos.removeAll(unloadedRefsToRemove) || changed;
                                if (changed) {
                                    save();
                                    pchSupport.firePropertyChange(PROPERTY_RECENT_PROJECTS, null, null);
                                }
                            }
                        });
                    }
                }
            });
        }
        
        public List<Project> getProjects() {
            assert OpenProjectList.MUTEX.isReadAccess();
            List<Project> result = new ArrayList<Project>( recentProjects.size() );
            // Copy the list
            List<ProjectReference> references = new ArrayList<ProjectReference>( recentProjects );
            for (ProjectReference pRef : references) {
                Project p = pRef.getProject();
                if ( p == null || !p.getProjectDirectory().isValid() ) {
                    remove( p );        // Folder does not exist any more => remove from
                    if (LOGGER.isLoggable(Level.FINE)) {
                        log(Level.FINE, "removing dead recent project: " + p);
                    }
                }
                else {
                    result.add( p );
                }
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                log(Level.FINE, "recent projects: " + result);
            }
            return result;
        }
        
        public boolean isEmpty() {
            assert OpenProjectList.MUTEX.isReadAccess();
            boolean empty = recentProjects.isEmpty();
            if (LOGGER.isLoggable(Level.FINE)) {
                log(Level.FINE, "recent projects empty? " + empty);
            }
            return empty;
        }
        
        public void load() {
            //read mutex only in the case of OPL.getDefault(), otherwise needs to be write, as it's mutating content.
            assert OpenProjectList.MUTEX.isReadAccess() || OpenProjectList.MUTEX.isWriteAccess();
            
            List<URL> URLs = OpenProjectListSettings.getInstance().getRecentProjectsURLs();
            List<String> names = OpenProjectListSettings.getInstance().getRecentProjectsDisplayNames();
            List<ExtIcon> icons = OpenProjectListSettings.getInstance().getRecentProjectsIcons();
            if (LOGGER.isLoggable(Level.FINE)) {
                log(Level.FINE, "recent project list load: " + URLs);
            }
            recentProjects.clear();
            for (URL url : URLs) {
                recentProjects.add(new ProjectReference(url));
            }
            recentProjectsInfos.clear();
            Iterator<String> iterNames = names.iterator();
            Iterator<URL> iterURLs = URLs.iterator();
            Iterator<ExtIcon> iterIcons = icons.iterator();
            while (iterNames.hasNext() && iterURLs.hasNext() && iterIcons.hasNext()) {
                String name = iterNames.next();
                URL url = iterURLs.next();
                Icon icon = iterIcons.next().getIcon();
                recentProjectsInfos.add(ProjectInfoAccessor.DEFAULT.getProjectInfo(name, icon, url));
            }
            // if following is true then there was either some problem with serialization
            // or user started new IDE on userdir with only partial information saved - only URLs
            // then both list should be cleared - recent project information will be lost
            if (recentProjects.size() != recentProjectsInfos.size()) {
                recentProjects.clear();
                recentProjectsInfos.clear();
            }
            // register project delete listener to all open projects
                for (Project p : openProjects) {
                    assert p != null : "There is null in " + openProjects;
                    assert p.getProjectDirectory() != null : "Project " + p + " has null project directory";
                    p.getProjectDirectory().addFileChangeListener(nbprojectDeleteListener);
                }
        }
        
        public void save() {
            assert OpenProjectList.MUTEX.isWriteAccess();
            List<URL> URLs = new ArrayList<URL>( recentProjects.size() );
            for (ProjectReference pRef: recentProjects) {
                URL pURL = pRef.getURL();
                if ( pURL != null ) {
                    URLs.add( pURL );
                }
            }
            List<UnloadedProjectInformation> _recentProjectsInfos = getRecentProjectsInfo();
            LOGGER.log(Level.FINE, "save recent project list: recentProjects={0} recentProjectsInfos={1} URLs={2}",
                    new Object[] {recentProjects, _recentProjectsInfos, URLs});
            OpenProjectListSettings.getInstance().setRecentProjectsURLs( URLs );
            int listSize = _recentProjectsInfos.size();
            List<String> names = new ArrayList<String>(listSize);
            List<ExtIcon> icons = new ArrayList<ExtIcon>(listSize);
            for (UnloadedProjectInformation prjInfo : _recentProjectsInfos) {
                names.add(prjInfo.getDisplayName());
                ExtIcon extIcon = new ExtIcon();
                extIcon.setIcon(prjInfo.getIcon());
                icons.add(extIcon);
            }
            OpenProjectListSettings.getInstance().setRecentProjectsDisplayNames(names);
            OpenProjectListSettings.getInstance().setRecentProjectsIcons(icons);
        }
        
        private int getIndex( Project p ) {
            if (p == null || p.getProjectDirectory() == null) {
                return -1;
            }
            URL pURL = p.getProjectDirectory().toURL();
            
            int i = 0;
            
            for (ProjectReference pRef : recentProjects) {
                URL p2URL = pRef.getURL();
                if ( pURL.equals( p2URL ) ) {
                    return i;
                } else {
                    i++;
                }
            }
            
            return -1;
        }
        
        private List<UnloadedProjectInformation> getRecentProjectsInfo() {
            // #166408: refreshing is too time expensive and we want to be fast, not correct
            //refresh();
            return OpenProjectList.MUTEX.readAccess(new Mutex.Action<List<UnloadedProjectInformation>>() {
                public @Override List<UnloadedProjectInformation> run() {
                    return new ArrayList<UnloadedProjectInformation>(recentProjectsInfos);
                }
            });
        }
        
        private class ProjectReference {
            
            private WeakReference<Project> projectReference;
            private final URL projectURL;
            
            public ProjectReference( URL url ) {                
                this.projectURL = url;
            }
            
            public ProjectReference( Project p ) {
                this.projectReference = new WeakReference<Project>( p );
                projectURL = p.getProjectDirectory().toURL();
            }
            
            public Project getProject() {
                
                Project p = null; 
                
                if ( projectReference != null ) { // Reference to project exists
                    p = projectReference.get();
                    if ( p != null ) {
                        // And refers to some project, check for validity:
                        if ( ProjectManager.getDefault().isValid( p ) )
                            return p; 
                        else
                            return null;
                    }
                }
                
                if (LOGGER.isLoggable(Level.FINE)) {
                    log(Level.FINE, "no active project reference for " + projectURL);
                }
                if ( projectURL != null ) {                    
                    FileObject dir = URLMapper.findFileObject( projectURL );
                    if ( dir != null && dir.isFolder() ) {
                        try {
                            p = ProjectManager.getDefault().findProject( dir );
                            if ( p != null ) {
                                projectReference = new WeakReference<Project>( p ); 
                                if (LOGGER.isLoggable(Level.FINE)) {
                                    log(Level.FINE, "found " + p);
                                }
                                return p;
                            }
                        }       
                        catch ( IOException e ) {
                            // Ignore invalid folders
                            if (LOGGER.isLoggable(Level.FINE)) {
                                log(Level.FINE, "could not load recent project from " + projectURL);
                            }
                        }
                    }
                }
                
                if (LOGGER.isLoggable(Level.FINE)) {
                    log(Level.FINE, "no recent project in " + projectURL);
                }
                return null; // Empty reference                
            }
            
            public URL getURL() {
                return projectURL;
            }

            public @Override String toString() {
                return projectURL.toString();
            }
            
        }
        
    }
    
    private static class ProjectByDisplayNameComparator implements Comparator<Project> {
        
        private static final Comparator<Object> COLLATOR = Collator.getInstance();

        // memoize results since it could be called >1 time per project:
        private final Map<Project,String> names = new HashMap<Project,String>();
        private String getDisplayName(Project p) {
            String n = names.get(p);
            if (n == null) {
                n = ProjectUtils.getInformation(p).getDisplayName();
                names.put(p, n);
            }
            return n;
        }
        
        @Override
        public int compare(Project p1, Project p2) {
//            Uncoment to make the main project be the first one
//            but then needs to listen to main project change
//            if ( OpenProjectList.getDefault().isMainProject( p1 ) ) {
//                return -1;
//            }
//            
//            if ( OpenProjectList.getDefault().isMainProject( p2 ) ) {
//                return 1;
//            }
            
            String n1 = getDisplayName(p1);
            String n2 = getDisplayName(p2);
            if (n1 != null && n2 != null) {
                return COLLATOR.compare(n1, n2);
            } else if (n1 == null && n2 != null) {
                log(Level.WARNING, p1 + ": ProjectInformation.getDisplayName() should not return null!");
                return -1;
            } else if (n1 != null && n2 == null) {
                log(Level.WARNING, p2 + ": ProjectInformation.getDisplayName() should not return null!");
                return 1;
            }
            return 0; // both null
            
        }
        
    }
    
    private final class NbProjectDeletionListener extends FileChangeAdapter {
        
        public NbProjectDeletionListener() {}
        
        @Override
        public void fileDeleted(FileEvent fe) {
            recentProjects.refresh();
        }
        
    }
    
    /**
     * Closes deleted projects.
     */
    private final class ProjectDeletionListener extends FileChangeAdapter {
        
        public ProjectDeletionListener() {}

        public @Override void fileDeleted(final FileEvent fe) {
            OpenProjectList.MUTEX.readAccess(new Mutex.Action<Void>() {
                public @Override Void run() {
                Project toRemove = null;
                for (Project prj : openProjects) {
                    if (fe.getFile().equals(prj.getProjectDirectory())) {
                        toRemove = prj;
                        break;
                    }
                }
                final Project fRemove = toRemove;
                if (fRemove != null) {
                    //#108376 avoid deadlock in org.netbeans.modules.project.ui.ProjectUtilities$1.close(ProjectUtilities.java:106)
                    // alternatively removing the close() metod from synchronized block could help as well..
                    FILE_DELETED_RP.post(new Runnable() { //#236956 FILE_DELETED_RP instead of SwingUtilities.invokeLater
                            @Override
                        public void run () {
                            close(new Project[] {fRemove}, false);
                        }
                    });
                }
                    return null;
                }
            });
        }
        
    }
    
    
    private void addModuleInfo(final Project prj) {
        final ModuleInfo info = Modules.getDefault().ownerOf(prj.getClass());
        if (info != null) {
            // is null in tests..
            MUTEX.writeAccess(new Mutex.Action<Void>() {
                public @Override Void run() {
                if (!openProjectsModuleInfos.containsKey(info)) {
                    openProjectsModuleInfos.put(info, new ArrayList<Project>());
                    info.addPropertyChangeListener(infoListener);
                }
                openProjectsModuleInfos.get(info).add(prj);
                return null;
            }
            });
        }
    }
    
    private void removeModuleInfo(Project prj) {
        removeModuleInfo(prj, Modules.getDefault().ownerOf(prj.getClass()));
    }
    
    private void removeModuleInfo(final Project prj, final ModuleInfo info) {
        // info can be null in case we are closing a project from disabled module
        if (info != null) {
            MUTEX.writeAccess(new Mutex.Action<Void>() {
                public @Override Void run() {
                List<Project> prjlist = openProjectsModuleInfos.get(info);
                if (prjlist != null) {
                    prjlist.remove(prj);
                    if (prjlist.isEmpty()) {
                        info.removePropertyChangeListener(infoListener);
                        openProjectsModuleInfos.remove(info);
                    }
                }
                return null;
            }
            });
        }
    }

    private void checkModuleInfo(ModuleInfo info) {
        if (info.isEnabled())  {
            return;
        }
        Collection<Project> toRemove = new ArrayList<Project>(openProjectsModuleInfos.get(info));
        if (toRemove.size() > 0) {
            for (Project prj : toRemove) {
                removeModuleInfo(prj, info);
            }
            close(toRemove.toArray(new Project[0]), false);
        }
    }
    
    private static LogRecord[] createRecord(String msg, Project[] projects) {
        if (projects.length == 0) {
            return null;
        }
        
        Map<String,int[]> counts = new HashMap<String,int[]>();
        for (Project p : projects) {
            String n = p.getClass().getName();
            int[] cnt = counts.get(n);
            if (cnt == null) {
                cnt = new int[1];
                counts.put(n, cnt);
            }
            cnt[0]++;
        }
        
        Logger logger = Logger.getLogger("org.netbeans.ui.projects"); // NOI18N
        LogRecord[] arr = new LogRecord[counts.size()];
        int i = 0;
        for (Map.Entry<String,int[]> entry : counts.entrySet()) {
            LogRecord rec = new LogRecord(Level.CONFIG, msg);
            rec.setParameters(new Object[] { entry.getKey(), afterLastDot(entry.getKey()), entry.getValue()[0] });
            rec.setLoggerName(logger.getName());
            rec.setResourceBundle(NbBundle.getBundle(OpenProjectList.class));
            rec.setResourceBundleName(OpenProjectList.class.getPackage().getName()+".Bundle");
            
            arr[i++] = rec;
        }
        
        return arr;
    }

   private static LogRecord[] createRecordMetrics (String msg, Project[] projects) {
        if (projects.length == 0) {
            return null;
        }

        Logger logger = Logger.getLogger("org.netbeans.ui.metrics.projects"); // NOI18N

        LogRecord[] arr = new LogRecord[projects.length];
        int i = 0;
        for (Project p : projects) {
            LogRecord rec = new LogRecord(Level.INFO, msg);
            rec.setParameters(new Object[] { p.getClass().getName() });
            rec.setLoggerName(logger.getName());

            arr[i++] = rec;
        }

        return arr;
    }
    
    private static void log(LogRecord[] arr, String loggerName) {
        if (arr == null) {
            return;
        }
        Logger logger = Logger.getLogger(loggerName); // NOI18N
        for (LogRecord r : arr) {
            logger.log(r);
        }
    }
    
    private static String afterLastDot(String s) {
        int index = s.lastIndexOf('.');
        if (index == -1) {
            return s;
        }
        return s.substring(index + 1);
    }
    
    private static void logProjects(String message, Project[] projects) {
        if (projects.length == 0) {
            return;
        }
        for (Project p : projects) {
            LOGGER.log(Level.FINER, "{0} {1}", new Object[]{ message, p == null ? null : p.toString()});
        }
    }
    
}
