/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
