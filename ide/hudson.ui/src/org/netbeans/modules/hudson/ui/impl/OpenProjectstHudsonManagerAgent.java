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

package org.netbeans.modules.hudson.ui.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.hudson.api.HudsonChangeAdapter;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.netbeans.modules.hudson.api.Utilities;
import org.netbeans.modules.hudson.ui.spi.ProjectHudsonProvider;
import org.netbeans.modules.hudson.spi.HudsonManagerAgent;
import org.netbeans.modules.hudson.ui.nodes.HudsonInstanceNode;
import org.netbeans.modules.hudson.ui.notification.ProblemNotificationController;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author jhavlin
 */
@ServiceProvider(service = HudsonManagerAgent.class)
public class OpenProjectstHudsonManagerAgent extends HudsonManagerAgent {

    private static final RequestProcessor RP
            = new RequestProcessor(OpenProjectstHudsonManagerAgent.class);
    private final PropertyChangeListener projectsListener;
    private final Map<Project, HudsonInstance> projectInstances
            = new HashMap<Project, HudsonInstance>();
    private final Map<HudsonInstance, ProblemNotificationController> notifications
            = new HashMap<HudsonInstance, ProblemNotificationController>();

    /**
     * Mapping from Hudson instance to one or more providers (projects).
     */
    private final Map<HudsonInstance, List<Project>> instanceProviders
            = new HashMap<HudsonInstance, List<Project>>();
    private final RequestProcessor.Task checkOpenProjects = RP.create(new Runnable() {
        public @Override
        void run() {
            checkOpenProjects();
        }
    });

    public OpenProjectstHudsonManagerAgent() {
        projectsListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(
                        evt.getPropertyName())) {
                    checkOpenProjects.schedule(0);
                }
            }
        };
    }

    @Override
    public void start() {
        OpenProjects.getDefault().addPropertyChangeListener(projectsListener);
        checkOpenProjects.schedule(0);
    }

    @Override
    public void terminate() {
        OpenProjects.getDefault().removePropertyChangeListener(projectsListener);
        projectInstances.clear();
    }

    private synchronized void checkOpenProjects() {
        try {
            Future<Project[]> fut = OpenProjects.getDefault().openProjects();
            Project[] prjs = fut.get();
            for (Project project : prjs) {
                boolean exists = projectInstances.containsKey(project);
                ProjectHudsonProvider.Association assoc
                        = ProjectHudsonProvider.getDefault().findAssociation(project);
                if (assoc != null && !exists) {
                    String url = assoc.getServerUrl();
                    if (!Utilities.checkHudsonURL(url).isOK()) {
                        continue;
                    }
                    HudsonInstance in = HudsonManager.getInstance(url);
                    if (in == null) {
                        String n = HudsonManager.simplifyServerLocation(url, false);
                        in = HudsonManager.addInstance(n, url, 60, false);
                        if (assoc.getViewName() != null) {
                            in.prefs().put(HudsonInstanceNode.SELECTED_VIEW,
                                    assoc.getViewName());
                        }
                    }
                    addProvider(in, project);
                    projectInstances.put(project, in);
                } else if (assoc == null && exists) {
                    HudsonInstance remove = projectInstances.remove(project);
                    if (remove != null) {
                        removeProvider(remove, project);
                        if (!hasProvider(remove)) {
                            HudsonManager.removeInstance(remove);
                        }
                    }
                }
            }
            ArrayList<Project> newprjs = new ArrayList<Project>(projectInstances.keySet());
            newprjs.removeAll(Arrays.asList(prjs));
            for (Project project : newprjs) {
                HudsonInstance remove = projectInstances.remove(project);
                if (remove != null && hasProvider(remove)
                        && !remove.isPersisted()) {
                    removeProvider(remove, project);
                    if (!hasProvider(remove)) {
                        HudsonManager.removeInstance(remove);
                    }
                }
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void addProvider(HudsonInstance instance, Project project) {
        List<Project> providers = instanceProviders.get(instance);
        if (providers == null) {
            providers = new ArrayList<Project>();
            instanceProviders.put(instance, providers);
        }
        if (!providers.contains(project)) {
            providers.add(project);
        }
        setPreferredJobs(instance);
    }

    private void removeProvider(HudsonInstance instance, Project project) {
        List<Project> providers = instanceProviders.get(instance);
        if (providers != null) {
            providers.remove(project);
            if (providers.isEmpty()) {
                instanceProviders.remove(instance);
            }
        }
        setPreferredJobs(instance);
    }

    private boolean hasProvider(HudsonInstance instance) {
        List<Project> providers = instanceProviders.get(instance);
        if (providers == null) {
            return false;
        } else {
            if (providers.isEmpty()) {
                instanceProviders.remove(instance);
                return false;
            } else {
                return true;
            }
        }
    }

    private List<Project> getProviders(HudsonInstance instance) {
        return instanceProviders.get(instance);
    }

    private void setPreferredJobs(HudsonInstance instance) {
        List<Project> providers = getProviders(instance);
        if (providers == null) {
            return;
        }
        List<String> names = new ArrayList<String>();
        for (Project prj : providers) {
            ProjectHudsonProvider.Association assoc
                    = ProjectHudsonProvider.getDefault().findAssociation(prj);
            if (assoc != null) {
                String name = assoc.getJobName();
                if (name != null) {
                    names.add(name);
                }
            }
        }
        instance.setPreferredJobs(names);
    }

    @Override
    public void instanceAdded(HudsonInstance instance) {
        final ProblemNotificationController controller =
                new ProblemNotificationController(instance);
        instance.addHudsonChangeListener(new HudsonChangeAdapter() {

            @Override
            public void contentChanged() {
                controller.updateNotifications();
            }
        });
        notifications.put(instance, controller);
    }

    @Override
    public void instanceRemoved(HudsonInstance instance) {
        ProblemNotificationController c = notifications.remove(instance);
        if (c != null) {
            c.clearNotifications();
        }
    }
}
