/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
            return opened.toArray(new Project[opened.size()]);
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
