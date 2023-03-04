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
package org.netbeans.modules.project.ui.problems;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.project.ui.ProjectProblems;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
public class BrokenProjectNotifier {
    
    private static final BrokenProjectNotifier instance =
            new BrokenProjectNotifier();
    private static final RequestProcessor NOTIFIER =
            new RequestProcessor(BrokenProjectNotifier.class.getName(), 1, false, false);   //NOI18N
    private static final Logger LOG = Logger.getLogger(BrokenProjectNotifier.class.getName());

    private final AtomicBoolean started = new AtomicBoolean();
    private final PropertyChangeListener listener = new PropertyChangeListener() {
        @Override
        public void propertyChange(@NonNull final PropertyChangeEvent evt) {
            if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
                final Object oldValue = evt.getOldValue();
                final Object newValue = evt.getNewValue();
                if (oldValue instanceof Project[] && newValue instanceof Project[]) {
                    checkBrokenProjects((Project[])oldValue, (Project[]) newValue);
                }
            }
        }
    };


    private BrokenProjectNotifier() {}


    public void start() {
        if (started.compareAndSet(false, true)) {
            OpenProjects.getDefault().addPropertyChangeListener(listener);
            LOG.fine("Started");    //NOI18N
        } else {
            throw new IllegalStateException("Already started.");    //NOI18N
        }
    }

    public void stop () {
        if (started.compareAndSet(true, false)) {
            OpenProjects.getDefault().removePropertyChangeListener(listener);
            LOG.fine("Stopped");    //NOI18N
        } else {
            throw new IllegalStateException("Not started.");      //NOI18N
        }
    }

    private void checkBrokenProjects(
            @NonNull final Project[] oldOpened,
            @NonNull final Project[] newOpened) {
        final Set<Project> justAdded = new HashSet<Project>(Arrays.asList(newOpened));
        justAdded.removeAll(Arrays.asList(oldOpened));
        if (!justAdded.isEmpty()) {
            LOG.log(Level.FINE, "New projects to check: {0}", justAdded);   //NOI18N
            NOTIFIER.execute(new Runnable(){
                @Override
                public void run() {
                    final Queue<Project> toCheck = new ArrayDeque<Project>();
                    for (Project project : justAdded) {
                        final ProjectProblemsProvider ppp = project.getLookup().lookup(ProjectProblemsProvider.class);
                        if (ppp != null) {
                            for (ProjectProblemsProvider.ProjectProblem problem : ppp.getProblems()) {
                                if (problem.getSeverity() == ProjectProblemsProvider.Severity.ERROR) {
                                    toCheck.offer(project);
                                    break;
                                }
                            }
                        }
                    }
                    if (!toCheck.isEmpty()) {
                        scheduleAlert(toCheck);
                    }
                }
            });
        }
    }

    private void scheduleAlert(@NonNull final Collection<? extends Project> prjs) {
        final Future<Project[]> projects = OpenProjects.getDefault().openProjects();
        try {
            projects.get();
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InterruptedException ie) {
            Exceptions.printStackTrace(ie);
        }
        for (Project prj : prjs) {
            LOG.log(Level.FINE, "Showing alert for project: {0}", prjs);  //NOI18N
            ProjectProblems.showAlert(prj);
        }
    }

    public static BrokenProjectNotifier getInstnace() {
        return instance;
    }
}
