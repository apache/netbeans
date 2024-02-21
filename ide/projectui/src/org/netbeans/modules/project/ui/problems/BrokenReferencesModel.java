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

package org.netbeans.modules.project.ui.problems;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.openide.util.ChangeSupport;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.ProjectProblemsProvider.ProjectProblem;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

public final class BrokenReferencesModel extends AbstractListModel implements PropertyChangeListener, ChangeListener {

    private static final Logger LOG = Logger.getLogger(BrokenReferencesModel.class.getName());

    private final Context ctx;
    private final boolean global;
    private final Object lock = new Object();
    //@GuardedBy("lock")
    private final Map<ProjectProblemsProvider,PropertyChangeListener> providers;
    //@GuardedBy("lock")
    private final List<ProblemReference> problems;


    BrokenReferencesModel(@NonNull final Context ctx, boolean global) {
        assert ctx != null;
        this.ctx = ctx;
        this.global = global;
        problems = new ArrayList<ProblemReference>();
        providers = new WeakHashMap<ProjectProblemsProvider, PropertyChangeListener>();
        refresh();
        ctx.addChangeListener(this);
    }

    BrokenReferencesModel(@NonNull final Project project) {
        this(new Context(), false);
        this.ctx.offer(project);
    }

    @Override
    public Object getElementAt(int index) {
        return getOneReference(index);
    }

    @Override
    public int getSize() {
        synchronized (lock) {
            return problems.size();
        }
    }

    void refresh() {
        final int size = ProjectManager.mutex().readAccess(new Mutex.Action<Integer>() {
            @Override
            public Integer run() {
                synchronized (lock) {
                    final Map<ProjectProblemsProvider,Project> newProviders = new LinkedHashMap<ProjectProblemsProvider,Project>();
                    for (Project bprj : ctx.getBrokenProjects()) {
                        final ProjectProblemsProvider provider = bprj.getLookup().lookup(ProjectProblemsProvider.class);
                        if (provider != null) {
                            newProviders.put(provider, bprj);
                        }
                    }
                    for (Iterator<Map.Entry<ProjectProblemsProvider,PropertyChangeListener>> it = providers.entrySet().iterator(); it.hasNext();) {
                        final Map.Entry<ProjectProblemsProvider,PropertyChangeListener> e = it.next();
                        if (!newProviders.containsKey(e.getKey())) {
                            e.getKey().removePropertyChangeListener(e.getValue());
                            it.remove();
                        }
                    }
                    final Set<ProblemReference> all = new LinkedHashSet<ProblemReference>();
                    for (Map.Entry<ProjectProblemsProvider,Project> ne : newProviders.entrySet()) {
                        final ProjectProblemsProvider ppp = ne.getKey();
                        final Project bprj = ne.getValue();
                        if (!providers.containsKey(ppp)) {
                            final PropertyChangeListener l = WeakListeners.propertyChange(BrokenReferencesModel.this, ppp);
                            ppp.addPropertyChangeListener(l);
                            providers.put(ppp, l);
                        }
                        for (ProjectProblem problem : ppp.getProblems()) {
                            all.add(new ProblemReference(problem, bprj, global));
                        }
                    }
                    updateReferencesList(problems, all);
                    return getSize();
                }
            }
        });
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                fireContentsChanged(BrokenReferencesModel.this, 0, size);
            }
        });
    }

    private ProblemReference getOneReference(int index) {
        synchronized (lock) {
            assert index>=0 && index<problems.size();
            return problems.get(index);
        }
    }

    private static void updateReferencesList(List<ProblemReference> oldBroken, Set<ProblemReference> newBroken) {
        LOG.log(Level.FINE, "References updated from {0} to {1}", new Object[] {oldBroken, newBroken});
        for (ProblemReference or : oldBroken) {
            or.resolved = !newBroken.contains(or);
        }
        for (ProblemReference or : newBroken) {
            if (!oldBroken.contains(or)) {
                oldBroken.add(or);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ProjectProblemsProvider.PROP_PROBLEMS.equals(evt.getPropertyName())) {
            refresh();
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        refresh();
    }

    static final class ProblemReference {
        private final boolean global;
        private final Project project;
        final ProjectProblem problem;


        volatile boolean resolved;


        ProblemReference(
                @NonNull final ProjectProblem problem,
                @NonNull final Project project,
                final boolean global) {
            assert problem != null;
            this.problem = problem;
            this.project = project;
            this.global = global;
        }


        String getDisplayName() {
            final String displayName = problem.getDisplayName();
            String message;
            if (global) {
                final String projectName = ProjectUtils.getInformation(project).getDisplayName();
                message = NbBundle.getMessage(
                        BrokenReferencesModel.class,
                        "FMT_ProblemInProject",
                        projectName,
                        displayName);

            } else {
                message = displayName;
            }
            return message;
        }

        @Override
        @NonNull
        public String toString() {
            return String.format(
              "Problem: %s %s", //NOI18N
              problem,
              resolved ? "resolved" : "unresolved");    //NOI18N
        }

        @Override
        public int hashCode() {
            int hash = 17;
            hash = 31 * hash + problem.hashCode();
            hash = 31 * hash + project.hashCode();
            return hash;
        }

        @Override
        public boolean equals (Object other) {
            if (!(other instanceof ProblemReference)) {
                return false;
            }
            final ProblemReference otherRef = (ProblemReference) other;
            return problem.equals(otherRef.problem) &&
                   project.equals(otherRef.project);
        }

    }

    static final class Context {
        private final List<Project> toResolve;
        private final ChangeSupport support;

        public Context() {
            toResolve = Collections.synchronizedList(new LinkedList<Project>());
            support = new ChangeSupport(this);
        }

        public void offer(@NonNull final Project broken) {
            assert broken != null;
            if (broken.getLookup().lookup(ProjectProblemsProvider.class) != null) {
                this.toResolve.add(broken);
                support.fireChange();
            }
        }

        public boolean isEmpty() {
            return this.toResolve.isEmpty();
        }

        public Project[] getBrokenProjects() {
            synchronized (toResolve) {
                return toResolve.toArray(new Project[0]);
            }
        }

        public void addChangeListener(final @NonNull ChangeListener listener) {
            assert listener != null;
            support.addChangeListener(listener);
        }

        public void removeChangeListener(final @NonNull ChangeListener listener) {
            assert listener != null;
            support.removeChangeListener(listener);
        }
    }

}
