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
package org.netbeans.modules.mylyn.util.commands;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.internal.tasks.core.sync.SynchronizationSession;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.modules.mylyn.util.BugtrackingCommand;
import org.netbeans.modules.mylyn.util.CancelableProgressMonitor;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.internal.Accessor;

/**
 * Performs a repository query. Finishes as soon as possible and does not
 * refresh tasks.
 *
 * @author Ondra Vrabec
 */
public class SimpleQueryCommand extends BugtrackingCommand {

    private final AbstractRepositoryConnector repositoryConnector;
    private final TaskRepository taskRepository;
    private final IRepositoryQuery query;
    private IStatus status;
    private final IProgressMonitor monitor;
    private final Set<NbTask> tasks;
    private final TaskDataManager taskDataManager;

    SimpleQueryCommand (AbstractRepositoryConnector repositoryConnector, 
            TaskRepository taskRepository, TaskDataManager taskDataManager,
            IRepositoryQuery query) {
        this.taskRepository = taskRepository;
        this.repositoryConnector = repositoryConnector;
        this.query = query;
        this.taskDataManager = taskDataManager;
        this.monitor = new CancelableProgressMonitor();        
        tasks = new HashSet<NbTask>();
    }

    @Override
    public void execute () throws CoreException {
        final Logger log = Logger.getLogger(this.getClass().getName());
        if (log.isLoggable(Level.FINE)) {
            Map<String, String> attrs = query.getAttributes();
            log.log(
                    Level.FINE,
                    "executing SimpleQueryCommand for query {0} on repository {1} with url \n\t{2} and parameters \n\t{3}", // NOI18N
                    new Object[]{query.getSummary(), taskRepository.getUrl(), query.getUrl(), attrs != null ? attrs : null});
        }
        TaskDataCollector collector = new TaskDataCollector() {
            @Override
            public void accept (TaskData taskData) {
                try {
                    Accessor acc = Accessor.getInstance();
                    NbTask task = acc.getOrCreateTask(taskRepository, taskData.getTaskId(), true);
                    taskDataManager.putUpdatedTaskData(acc.getDelegate(task), taskData, true);
                    tasks.add(task);
                } catch (CoreException ex) {
                    log.log(Level.INFO, "Cannot save task data " + taskData.getTaskId(), ex);
                }
            }
        };
        status = repositoryConnector.performQuery(taskRepository, query, collector, new SynchronizationSession(), monitor);
        if (status != null && status.getSeverity() == IStatus.ERROR) {
            if (status.getException() instanceof CoreException) {
                throw (CoreException) status.getException();
            } else {
                throw new CoreException(status);
            }
        }
    }

    public IStatus getStatus () {
        return status;
    }

    @Override
    public void cancel () {
        monitor.setCanceled(true);
    }

    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder();
        sb.append("SimpleQueryCommand [repository=");
        sb.append(taskRepository.getUrl());
        sb.append(", summary=");
        sb.append(query.getSummary());
        sb.append(", url=");
        sb.append(query.getUrl());
        sb.append("]");
        return super.toString();
    }

    public Collection<NbTask> getTasks () {
        return tasks;
    }
}
