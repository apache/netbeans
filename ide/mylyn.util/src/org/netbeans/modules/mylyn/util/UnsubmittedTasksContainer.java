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
