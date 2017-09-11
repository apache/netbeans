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
