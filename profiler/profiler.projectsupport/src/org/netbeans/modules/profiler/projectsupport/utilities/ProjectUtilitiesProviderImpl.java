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
package org.netbeans.modules.profiler.projectsupport.utilities;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.profiler.spi.ProjectUtilitiesProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup.Provider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 */
@ServiceProvider(service = ProjectUtilitiesProvider.class)
public class ProjectUtilitiesProviderImpl extends ProjectUtilitiesProvider {
    
    private Set<ChangeListener> listeners;

    @Override
    public Icon getIcon(Provider project) {
        return ProjectUtilities.getProjectIcon((Project)project);
    }

    @Override
    public Provider getMainProject() {
        return ProjectUtilities.getMainProject();
    }

    @Override
    public String getDisplayName(Provider project) {
        return ProjectUtilities.getProjectName((Project)project);
    }

    @Override
    public FileObject getProjectDirectory(Provider project) {
        return ((Project)project).getProjectDirectory();
    }

    @Override
    public Provider[] getOpenedProjects() {
        return ProjectUtilities.getOpenedProjects();
    }
    
    @Override
    public boolean hasSubprojects(Provider project) {
        return ProjectUtilities.hasSubprojects((Project)project);
    }

    @Override
    public void fetchSubprojects(Provider project, Set<Provider> subprojects) {
        ProjectUtilities.fetchSubprojects((Project)project, (Set)subprojects);
    }
    
    @Override
    public Provider getProject(FileObject fobj) {
        return FileOwnerQuery.getOwner(fobj);
    }
    
    /**
     * Adds a listener to be notified when set of open projects changes.
     * @param listener listener to be added
     */
    @Override
    public synchronized void addOpenProjectsListener(ChangeListener listener) {
        listeners().add(listener);
    }
    
    /**
     * Removes a listener to be notified when set of open projects changes.
     * @param listener listener to be removed
     */
    @Override
    public synchronized void removeOpenProjectsListener(ChangeListener listener) {
        if (hasListeners()) listeners.remove(listener);
        if (!hasListeners()) listeners = null;
    }
    
    private synchronized Set<ChangeListener> listeners() {
        if (!hasListeners()) listeners = new HashSet<ChangeListener>();
        return listeners;
    }
    
    private synchronized boolean hasListeners() {
        return listeners != null;
    }
    
    
    public ProjectUtilitiesProviderImpl() {
        OpenProjects.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                synchronized(ProjectUtilitiesProviderImpl.this) {
                    if (!hasListeners()) return;

                    String prop = evt.getPropertyName();
                    if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(prop) ||
                        OpenProjects.PROPERTY_MAIN_PROJECT.equals(prop)) {
                        for (ChangeListener listener : listeners)
                            listener.stateChanged(new ChangeEvent(evt));
                    }
                }
            }
        });
    }
}
