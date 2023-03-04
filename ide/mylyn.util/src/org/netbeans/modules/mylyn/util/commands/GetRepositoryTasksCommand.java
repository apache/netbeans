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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.modules.mylyn.util.BugtrackingCommand;
import org.netbeans.modules.mylyn.util.CancelableProgressMonitor;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.internal.Accessor;

/**
 *
 * @author Ondrej Vrabec
 */
public class GetRepositoryTasksCommand extends BugtrackingCommand {

    private String stringValue;
    private final TaskRepository taskRepository;
    private final Set<String> taskIds;
    private final CancelableProgressMonitor monitor;
    private final TaskDataManager taskDataManager;
    private final AbstractRepositoryConnector connector;
    private final List<NbTask> tasks = new ArrayList<NbTask>();

    GetRepositoryTasksCommand (AbstractRepositoryConnector connector,
            TaskRepository taskRepository, Set<String> taskIds,
            TaskDataManager taskDataManager) {
        this.connector = connector;
        this.taskRepository = taskRepository;
        this.taskIds = taskIds;
        this.taskDataManager = taskDataManager;
        this.monitor = new CancelableProgressMonitor();
    }

    @Override
    public void execute () throws CoreException, IOException, MalformedURLException {
        Logger log = Logger.getLogger(this.getClass().getName());
        if (log.isLoggable(Level.FINE)) {
            log.log(
                    Level.FINE,
                    "executing GetRepositoryTasksCommand for task ids {0}:{1}", //NOI18N
                    new Object[] { taskRepository.getUrl(), taskIds });
        }
        if (taskIds.size() == 1 || !connector.getTaskDataHandler().canGetMultiTaskData(taskRepository)) {
            for (String taskId : taskIds) {
                TaskData taskData = connector.getTaskData(taskRepository, taskId, monitor);
                if (monitor.isCanceled()) {
                    return;
                }
                if (taskData != null) {
                    Accessor acc = Accessor.getInstance();
                    NbTask task = acc.getOrCreateTask(taskRepository, taskData.getTaskId(), true);
                    taskDataManager.putUpdatedTaskData(acc.getDelegate(task), taskData, true);
                    tasks.add(task);
                }
            }
        } else {
            connector.getTaskDataHandler().getMultiTaskData(taskRepository, taskIds,
                    new Collector(), monitor);
        }
    }

    @Override
    public void cancel () {
        monitor.setCanceled(true);
    }

    @Override
    public String toString () {
        if (stringValue == null) {
            StringBuilder sb = new StringBuilder()
                    .append("Getting tasks ") //NOI18N
                    .append(taskIds)
                    .append(",repository=") //NOI18N
                    .append(taskRepository.getUrl())
                    .append("]"); //NOI18N
            stringValue = sb.toString();
        }
        return stringValue;
    }

    public List<NbTask> getTasks () {
        return tasks;
    }

    private class Collector extends TaskDataCollector {

        @Override
        public void accept (TaskData taskData) {
            try {
                Accessor acc = Accessor.getInstance();
                NbTask task = acc.getOrCreateTask(taskRepository, taskData.getTaskId(), true);
                taskDataManager.putUpdatedTaskData(acc.getDelegate(task), taskData, true);
                tasks.add(task);
            } catch (CoreException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, ex);
            }
        }
        
    }
}
