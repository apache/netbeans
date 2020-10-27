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
package org.netbeans.modules.java.lsp.server.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.project.uiapi.BrokenReferencesImplementation;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(service = BrokenReferencesImplementation.class, position = 10000)
public class BrokenReferencesSink implements BrokenReferencesImplementation, PropertyChangeListener {
    private Map<Project, BrokenProjectReferencesCollector> projectController = new HashMap<>();
    private Map<Project, Lookup> contextLookups = new HashMap<>();
    
    public BrokenReferencesSink() {
        OpenProjects.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(this, OpenProjects.getDefault()));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
            return;
        }
        Collection<BrokenProjectReferencesCollector> toDisable = new ArrayList<>();
        Set<Project> closedProjects;
        synchronized (this) {
            closedProjects = new HashSet<>(contextLookups.keySet());
            for (Project p : OpenProjects.getDefault().getOpenProjects()) {
                closedProjects.remove(p);
                if (!contextLookups.containsKey(p)) {
                    contextLookups.put(p, Lookup.getDefault());
                }
            }
            
            contextLookups.keySet().removeAll(closedProjects);
            projectController.keySet().removeAll(closedProjects);
        }
        toDisable.forEach(BrokenProjectReferencesCollector::cancel);
    }
    
    // @GuardedBy(this)
    private Lookup projectLookup(Project p) {
        return contextLookups.computeIfAbsent(p, (r) -> Lookup.getDefault());
    }

    @Override
    public void showAlert(Project project) {
        showCustomizer(project);
    }
    
    private void removeController(BrokenProjectReferencesCollector ctrl) {
        synchronized (this) {
            projectController.values().remove(ctrl);
        }
    }

    @Override
    public void showCustomizer(Project project) {
        BrokenProjectReferencesCollector ctrl;
        synchronized (this) {
            ctrl = projectController.get(project);
            if (ctrl == null) {
                ProjectProblemsProvider problems = project.getLookup().lookup(ProjectProblemsProvider.class);
                if (problems != null) {
                    ctrl = new BrokenProjectReferencesCollector(projectLookup(project), project, problems);
                    projectController.put(project, ctrl);
                } else {
                    return;
                }
            }
            ctrl.touch();
        }
        ctrl.activate(this::removeController);
    }
}
