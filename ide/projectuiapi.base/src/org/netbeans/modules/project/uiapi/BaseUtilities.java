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
package org.netbeans.modules.project.uiapi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.project.ui.ProjectGroup;
import org.netbeans.api.project.ui.ProjectGroupChangeListener;
import org.openide.explorer.ExplorerManager;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public final class BaseUtilities {
    
    private static final Logger LOG = Logger.getLogger(BaseUtilities.class.getName());
    
    /** 
     * Gets an object the OpenProjects can delegate to
     */
    public static OpenProjectsTrampoline getOpenProjectsTrampoline() {
        OpenProjectsTrampoline instance = Lookup.getDefault().lookup(OpenProjectsTrampoline.class);
        return instance != null ? instance : DefaultOpenProjectsTrampoline.getInstance();
    }
    
    // XXX anybody using this
    @org.netbeans.api.annotations.common.SuppressWarnings("MS_SHOULD_BE_FINAL")
    public static ProjectGroupAccessor ACCESSOR = null;

    static {
        // invokes static initializer of ModelHandle.class
        // that will assign value to the ACCESSOR field above
        Class<?> c = ProjectGroup.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "very wrong, very wrong, yes indeed", ex);
        }
    }

    public abstract static class ProjectGroupAccessor {

        public abstract ProjectGroup createGroup(String name, Preferences prefs);

    }

    private static final class DefaultOpenProjectsTrampoline implements OpenProjectsTrampoline {

        private static final AtomicReference<DefaultOpenProjectsTrampoline> INSTANCE = new AtomicReference<>();
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private final Collection<Project> open = new ArrayList<>();
        private Project main;

        private DefaultOpenProjectsTrampoline() {
        }

        @Override public Project[] getOpenProjectsAPI() {
            return open.toArray(new Project[0]);
        }
        @Override public void openAPI(Project[] projects, boolean openRequiredProjects, boolean showProgress) {
            open.addAll(Arrays.asList(projects));
            pcs.firePropertyChange(OpenProjects.PROPERTY_OPEN_PROJECTS, null, null);
        }
        @Override public void closeAPI(Project[] projects) {
            open.removeAll(Arrays.asList(projects));
            pcs.firePropertyChange(OpenProjects.PROPERTY_OPEN_PROJECTS, null, null);
        }
        @Override public Future<Project[]> openProjectsAPI() {
            return RequestProcessor.getDefault().submit(new Callable<Project[]>() {
                @Override public Project[] call() {
                    return getOpenProjectsAPI();
                }
            });
        }
        @Override public Project getMainProject() {
            return main;
        }
        @Override public void setMainProject(Project project) {
            main = project;
            pcs.firePropertyChange(OpenProjects.PROPERTY_MAIN_PROJECT, null, null);
        }
        @Override public void addPropertyChangeListenerAPI(PropertyChangeListener listener, Object source) {
            pcs.addPropertyChangeListener(listener);
        }
        @Override public void removePropertyChangeListenerAPI(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

        @Override
        public void addProjectGroupChangeListenerAPI(ProjectGroupChangeListener listener) {
        }

        @Override
        public void removeProjectGroupChangeListenerAPI(ProjectGroupChangeListener listener) {
        }

        @Override
        public ProjectGroup getActiveProjectGroupAPI() {
            return null;
        }

        @NonNull
        static DefaultOpenProjectsTrampoline getInstance() {
            DefaultOpenProjectsTrampoline res = INSTANCE.get();
            if (res == null) {
                res = new DefaultOpenProjectsTrampoline();
                if (!INSTANCE.compareAndSet(null, res)) {
                    res = INSTANCE.get();
                }
            }
            assert res != null;
            return res;
        }

        @Override
        public ExplorerManager createLogicalView() {
            return new ExplorerManager();
        }

        @Override
        public ExplorerManager createPhysicalView() {
            return new ExplorerManager();
        }
    }
}
