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

package org.netbeans.modules.mylyn.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener;
import org.eclipse.mylyn.internal.tasks.core.ITaskListRunnable;
import org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 *
 * @author Ondrej Vrabec
 */
public final class UnsubmittedTasksContainer {
    private final TaskRepository repository;
    private final Set<NbTask> tasks = Collections.synchronizedSet(new LinkedHashSet<NbTask>());
    private final PropertyChangeSupport support;
    /**
     * List of unsubmitted tasks changed.
     */
    public static final String EVENT_ISSUES_CHANGED = "mylyn.unsubmitted_tasks.changed"; //NOI18N
    private static final Logger LOG = Logger.getLogger(UnsubmittedTasksContainer.class.getName());
    private final TaskList taskList;
    private final MylynSupport supp = MylynSupport.getInstance();
    private TaskListListener list;

    UnsubmittedTasksContainer (TaskRepository repository, TaskList taskList) {
        this.repository = repository;
        this.taskList = taskList;
        this.support = new PropertyChangeSupport(this);
        initialize();
    }

    public List<NbTask> getTasks () {
        return new ArrayList<NbTask>(tasks);
    }
    
    public void addPropertyChangeListener (PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener (PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    private void fireIssuesChanged() {
        support.firePropertyChange(EVENT_ISSUES_CHANGED, null, null);
    }
    
    private void initialize () {
        try {
            taskList.run(new ITaskListRunnable() {
                @Override
                public void execute (IProgressMonitor monitor) throws CoreException {
                    taskList.addChangeListener(list = new TaskListListener());
                    tasks.addAll(supp.toNbTasks(taskList.getUnsubmittedContainer(
                            repository.getRepositoryUrl()).getChildren()));
                    for (NbTask task : supp.getTasks(repository)) {
                        if (task.isOutgoing()) {
                            tasks.add(task);
                        }
                    }
                }
            });
        } catch (CoreException ex) {
            LOG.log(Level.INFO, null, ex);
        }
    }
    
    private class TaskListListener implements ITaskListChangeListener {
        
        @Override
        public void containersChanged (Set<TaskContainerDelta> deltas) {
            // listen on changes on all tasks
            boolean change = false;
            for (TaskContainerDelta delta : deltas) {
                if (delta.getElement() instanceof ITask) {
                    ITask task = (ITask) delta.getElement();
                    NbTask nbTask = supp.toNbTask(task);
                    if (delta.getKind() == TaskContainerDelta.Kind.CONTENT) {
                        if (repository.getRepositoryUrl().equals(task.getRepositoryUrl())) {
                            // the task may change its status
                            change |= nbTask.isOutgoing() ? tasks.add(nbTask) : tasks.remove(nbTask);
                        }
                    } else if (delta.getKind() == TaskContainerDelta.Kind.DELETED) {
                        // the task was deleted permanently
                        change |= tasks.remove(nbTask);
                    } else if (delta.getKind() == TaskContainerDelta.Kind.ADDED
                            && task.getSynchronizationState() == ITask.SynchronizationState.OUTGOING_NEW) {
                        // task may be added to the unsubmitted category
                        change |= taskList.getUnsubmittedContainer(repository.getRepositoryUrl())
                                .getChildren().contains(task) && tasks.add(nbTask);
                    }
                }
            }
            if (change) {
                fireIssuesChanged();
            }
        }
    
    }
}
