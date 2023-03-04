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
package org.netbeans.spi.project.ui.support;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.util.Parameters;

/**
 * Support for {@link ProjectProblemsProvider provider of project metadata problems}.
 * <p>
 * This class is thread-safe.
 * @author Tomas Mysik
 * @since 1.63
 */
public final class ProjectProblemsProviderSupport {

    private final PropertyChangeSupport propertyChangeSupport;
    private final Object problemsLock = new Object();

    // @GuardedBy("problemsLock")
    private Collection<ProjectProblemsProvider.ProjectProblem> problems;
    // @GuardedBy("problemsLock")
    private long changeId;


    /**
     * Create a new {@code ProjectProblemsProviderSupport}.
     *
     * @param source an instance to be given as the source for events, never {@code null}
     */
    public ProjectProblemsProviderSupport(@NonNull Object source) {
        Parameters.notNull("source", source);
        propertyChangeSupport = new PropertyChangeSupport(source);
    }

    /**
     * Add a listener to the listener list.
     * @param listener {@link PropertyChangeListener} to be added, never {@code null}
     * @see PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)
     */
    public void addPropertyChangeListener(@NonNull PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove a listener from the listener list.
     * @param listener {@link PropertyChangeListener} to be removed, never {@code null}
     * @see PropertyChangeSupport#removePropertyChangeListener(PropertyChangeListener)
     */
    public void removePropertyChangeListener(@NonNull PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Get project problems using the given {@link ProblemsCollector problems collector}.
     * @param problemsCollector {@link ProblemsCollector problems collector} to be used, never {@code null}
     * @return collection of project problems, never {@code null}
     */
    @NonNull
    public Collection<? extends ProjectProblemsProvider.ProjectProblem> getProblems(@NonNull ProblemsCollector problemsCollector) {
        Parameters.notNull("problemsCollector", problemsCollector);

        Collection<ProjectProblemsProvider.ProjectProblem> currentProblems;
        long currentChangeId;
        synchronized (problemsLock) {
            currentProblems = problems;
            currentChangeId = changeId;
        }
        if (currentProblems != null) {
            return Collections.unmodifiableCollection(currentProblems);
        }
        currentProblems = new ArrayList<ProjectProblemsProvider.ProjectProblem>();
        currentProblems.addAll(problemsCollector.collectProblems());
        if (currentProblems.isEmpty()) {
            currentProblems = Collections.<ProjectProblemsProvider.ProjectProblem>emptySet();
        }
        synchronized (problemsLock) {
            if (currentChangeId == changeId) {
                problems = currentProblems;
            } else if (problems != null) {
                currentProblems = problems;
            }
        }
        assert currentProblems != null;
        return Collections.unmodifiableCollection(currentProblems);
    }

    /**
     * Fire {@link ProjectProblemsProvider#PROP_PROBLEMS project problems property} change event to all registered listeners.
     */
    public void fireProblemsChange() {
        synchronized (problemsLock) {
            problems = null;
            changeId++;
        }
        propertyChangeSupport.firePropertyChange(ProjectProblemsProvider.PROP_PROBLEMS, null, null);
    }

    //~ Inner classes

    /**
     * Collector of current project problems.
     */
    public interface ProblemsCollector {

        /**
         * Collect current project problems.
         * @return list of current project problems, can be empty but never {@code null}
         */
        @NonNull
        Collection<? extends ProjectProblemsProvider.ProjectProblem> collectProblems();

    }

}
