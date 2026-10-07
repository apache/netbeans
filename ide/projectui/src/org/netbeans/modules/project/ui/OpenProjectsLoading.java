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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.project.ui.groups.Group;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

final class OpenProjectsLoading implements Runnable, LookupListener {
    static final RequestProcessor RP = new RequestProcessor("Load Open Projects"); // NOI18N
    private final RequestProcessor.Task TASK = RP.create(this);
    private volatile int action;
    private final LinkedList<Project> toOpenProjects = new LinkedList<>();
    private List<Project> lazilyOpenedProjects;
    private Project lazyMainProject;
    private Lookup.Result<FileObject> currentFiles;
    private int entered;
    private final Lock enteredGuard = new ReentrantLock();
    private final Condition enteredZeroed = enteredGuard.newCondition();
    private final ProgressHandle progress;
    private final Callback outer;

    @NbBundle.Messages(value = "CAP_Opening_Projects=Opening Projects")
    @SuppressWarnings(value = "LeakingThisInConstructor")
    public OpenProjectsLoading(int action, Callback callback) {
        this.outer = callback;
        this.action = action;
        currentFiles = Utilities.actionsGlobalContext().lookupResult(FileObject.class);
        currentFiles.addLookupListener(WeakListeners.create(LookupListener.class, this, currentFiles));
        progress = ProgressHandle.createHandle(Bundle.CAP_Opening_Projects());
    }

    final boolean waitFinished(long timeout) {
        OpenProjectsLogging.log(Level.FINER, "waitFinished, action {0}", action); // NOI18N
        if (action == 0) {
            run();
        }
        OpenProjectsLogging.log(Level.FINER, "waitFinished, before wait"); // NOI18N
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
        OpenProjectsLogging.log(Level.FINER, "waitFinished, after wait"); // NOI18N
        return true;
    }

    @Override
    public void run() {
        OpenProjectsLogging.log(Level.FINE, "LoadOpenProjects.run: {0}", action); // NOI18N
        switch (action) {
            case 0 -> {
                action = 1;
                TASK.schedule(0);
                resultChanged(null);
                return;
            }
            case 1 -> {
                if (!RP.isRequestProcessorThread()) {
                    return;
                }
                action = 2;
                try {
                    progress.start();
                    loadInBackground();
                } finally {
                    progress.finish();
                }
                updateGlobalState();
                ProjectsRootNode.checkNoLazyNode();
                Group.projectsLoaded();
                return;
            }
            case 2 -> {
                // finished, oK
                return;
            }
            default -> throw new IllegalStateException("unknown action: " + action);
        }
    }

    final void preferredProject(final Set<FileObject> lazyPDirs) {
        OpenProjectList.MUTEX.writeAccess((Mutex.Action<Void>) () -> {
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
        });
    }

    private void updateGlobalState() {
        OpenProjectsLogging.log(Level.FINER, "updateGlobalState"); // NOI18N
        OpenProjectList.MUTEX.writeAccess(new Mutex.Action<Void>() {
            @Override
            public Void run() {
                OpenProjectsLogging.log(Level.FINER, "openProjects changed: {0}", lazilyOpenedProjects); // NOI18N
                outer.updateGlobalState(lazilyOpenedProjects, lazyMainProject, checkFirstRun());
                OpenProjectsLogging.log(Level.FINER, "updateGlobalState, applied"); // NOI18N
                return null;
            }
        });
    }

    private boolean checkFirstRun() {
        Preferences prefs = OpenProjectListSettings.getInstance().getPreferences();
        String prefKey = "projectListVersion"; // NOI18N
        String build = System.getProperty("netbeans.buildnumber", "0"); // NOI18N
        if (!prefs.get(prefKey, "").equals(build)) {
            prefs.put(prefKey, build);
            return true;
        } else {
            return false;
        }
    }

    boolean closeBeforeOpen(final Project[] arr) {
        return OpenProjectList.MUTEX.writeAccess(new Mutex.Action<Boolean>() {
            @Override
            public Boolean run() {
                NEXT:
                for (Project p : arr) {
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

    @NbBundle.Messages(value = {"#NOI18N", "LOAD_PROJECTS_ON_START=true"})
    private void loadInBackground() {
        lazilyOpenedProjects = new ArrayList<>();
        final boolean loadProjectsOnStart = "true".equals(Bundle.LOAD_PROJECTS_ON_START());
        List<URL> urls = loadProjectsOnStart ? OpenProjectListSettings.getInstance().getOpenProjectsURLs() : Collections.emptyList();
        final List<Project> initial = new ArrayList<>();
        final Collection<Project> projects = urls2Projects(urls);
        OpenProjectList.MUTEX.writeAccess(new Mutex.Action<Void>() {
            @Override
            public Void run() {
                toOpenProjects.addAll(projects);
                OpenProjectsLogging.log(Level.FINER, "loadOnBackground {0}", toOpenProjects); // NOI18N
                initial.addAll(toOpenProjects);
                return null;
            }
        });
        final URL mainProjectURL = OpenProjectListSettings.getInstance().getMainProjectURL();
        int max = OpenProjectList.MUTEX.writeAccess(new Mutex.Action<Integer>() {
            @Override
            public Integer run() {
                for (Project p : toOpenProjects) {
                    outer.beginOpening(p);
                    // Set main project
                    if (mainProjectURL != null && mainProjectURL.equals(p.getProjectDirectory().toURL())) {
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
                @Override
                public Project run() {
                    if (toOpenProjects.isEmpty()) {
                        return null;
                    }
                    Project p = toOpenProjects.remove();
                    OpenProjectsLogging.log(Level.FINER, "after remove {0}", toOpenProjects); // NOI18N
                    openPrjSize.set(toOpenProjects.size());
                    return p;
                }
            });
            if (p == null) {
                break;
            }
            OpenProjectsLogging.log(Level.FINE, "about to open a project {0}", p); // NOI18N
            boolean successfullyOpened = outer.finishOpening(p);
            if (successfullyOpened) {
                lazilyOpenedProjects.add(p);
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
            OpenProjectsLogging.log(OpenProjectsLogging.createRecord("UI_INIT_PROJECTS", initialA), "org.netbeans.ui.projects");
            OpenProjectsLogging.log(OpenProjectsLogging.createRecordMetrics("USG_PROJECT_OPEN", initialA), "org.netbeans.ui.metrics.projects");
        }
    }
    private final RequestProcessor.Task resChangedTask = Hacks.RP.create(new Runnable() {
        @Override
        public void run() {
            Set<FileObject> lazyPDirs = new HashSet<FileObject>();
            for (FileObject fileObject : currentFiles.allInstances()) {
                Project p = FileOwnerQuery.getOwner(fileObject);
                if (p != null) {
                    lazyPDirs.add(p.getProjectDirectory());
                }
            }
            if (!lazyPDirs.isEmpty()) {
                preferredProject(lazyPDirs);
            }
        }
    });

    @Override
    public void resultChanged(LookupEvent ev) {
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

    public boolean isDone() {
        return TASK.isFinished() && entered == 0;
    }

    private static Set<Project> urls2Projects(Collection<URL> urls) {
        Set<Project> result = new LinkedHashSet<>();

        for (URL url : urls) {
            FileObject dir = URLMapper.findFileObject(url);
            if (dir != null && dir.isFolder()) {
                try {
                    Project p = ProjectManager.getDefault().findProject(dir);
                    if (p != null && !result.contains(p)) { //#238093, #238811 if multiple entries point to the same project we end up with the same instance multiple times in the linked list. That's wrong.
                        result.add(p);
                    }
                } catch (Throwable t) {
                    //something bad happened during loading the project.
                    //log the problem, but allow the other projects to be load
                    //see issue #65900
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);
                }
            }
        }
        return result;
    }

    /**
     * A currated interface to {@link OpenProjectList}.
     */
    sealed interface Callback permits OpenProjectList.LoadingCallback {
        /** Called when computation of project opening is finished */
        public void updateGlobalState(List<Project> lazilyOpenedProjects, Project lazyMainProject, boolean checkFirstRun);

        /** Notifies a project that's about to be open */
        public void beginOpening(Project p);

        /** Notifies that a project has been opened.
         *
         * @param p the project to finish opening
         * @return {@code true} if the project has successfully been opened, {@code false} if something failed}
         */
        public boolean finishOpening(Project p);
    }
}
