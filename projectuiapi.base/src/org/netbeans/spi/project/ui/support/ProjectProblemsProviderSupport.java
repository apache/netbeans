/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
