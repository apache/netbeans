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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.project.ui.ProjectGroup;
import org.netbeans.api.project.ui.ProjectGroupChangeListener;
import org.netbeans.insane.live.CancelException;
import org.netbeans.modules.project.uiapi.OpenProjectsTrampoline;
import org.netbeans.modules.project.uiapi.ProjectOpenedTrampoline;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.explorer.ExplorerManager;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service = OpenProjectsTrampoline.class)
public final class TestTrampoline implements OpenProjectsTrampoline {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Set<Project> opened = new HashSet<>();
    private volatile Project mainProject;

    @Override
    public Project[] getOpenProjectsAPI() {
        rwLock.readLock().lock();
        try {
            return opened.toArray(new Project[0]);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void openAPI(Project[] projects, boolean openRequiredProjects, boolean showProgress) {
        final Set<Project> justOpened = new HashSet<>();
        rwLock.writeLock().lock();
        try {
            for (Project p : projects) {
                if (opened.add(p)) {
                    justOpened.add(p);
                }
            }
        } finally {
           rwLock.writeLock().unlock();
        }
        for (Project p : justOpened) {
            for (ProjectOpenedHook hook : p.getLookup().lookupAll(ProjectOpenedHook.class)) {
                ProjectOpenedTrampoline.DEFAULT.projectOpened(hook);
            }
        }
        if (!justOpened.isEmpty()) {
            support.firePropertyChange(OpenProjects.PROPERTY_OPEN_PROJECTS, null, null);
        }
    }

    @Override
    public void closeAPI(Project[] projects) {
        final Set<Project> justClosed = new HashSet<>();
        rwLock.writeLock().lock();
        try {
            for (Project p : projects) {
                if (opened.remove(p)) {
                    justClosed.add(p);
                }
            }
        } finally {
            rwLock.writeLock().unlock();
        }
        for (Project p : justClosed) {
            for (ProjectOpenedHook hook : p.getLookup().lookupAll(ProjectOpenedHook.class)) {
                ProjectOpenedTrampoline.DEFAULT.projectClosed(hook);
            }
        }
        if (!justClosed.isEmpty()) {
            support.firePropertyChange(OpenProjects.PROPERTY_OPEN_PROJECTS, null, null);
        }
    }

    @Override
    public void addPropertyChangeListenerAPI(PropertyChangeListener listener, Object source) {
        this.support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListenerAPI(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener(listener);
    }

    @Override
    public Future<Project[]> openProjectsAPI() {
        return new F();
    }


    @Override
    public Project getMainProject() {
        return mainProject;
    }

    @Override
    public void setMainProject(Project project) {
        mainProject = project;
        support.firePropertyChange(OpenProjects.PROPERTY_MAIN_PROJECT, null, null);
    }

    @Override
    public ProjectGroup getActiveProjectGroupAPI() {
        return null;
    }

    @Override
    public void addProjectGroupChangeListenerAPI(ProjectGroupChangeListener listener) {
    }

    @Override
    public void removeProjectGroupChangeListenerAPI(ProjectGroupChangeListener listener) {
    }

    @Override
    public ExplorerManager createLogicalView() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExplorerManager createPhysicalView() {
        throw new UnsupportedOperationException();
    }

    private final class F implements Future<Project[]> {

        private volatile Project[] res;
        private volatile boolean canceled;

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return canceled = true;
        }

        @Override
        public boolean isCancelled() {
            return canceled;
        }

        @Override
        public boolean isDone() {
            return canceled || res != null;
        }

        @Override
        public Project[] get() throws InterruptedException, ExecutionException {
            if (canceled) {
                throw new CancelException();
            }
            if (res != null) {
                return res;
            }
            return res = getOpenProjectsAPI();
        }

        @Override
        public Project[] get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            if (canceled) {
                throw new CancelException();
            }
            if (res != null) {
                return res;
            }
            if (rwLock.readLock().tryLock(timeout, unit)) {
                try {
                    return res = getOpenProjectsAPI();
                }finally {
                    rwLock.readLock().unlock();
                }
            }
            throw new TimeoutException();
        }
    }
}
