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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.tasks.core.RepositoryModel;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.internal.tasks.core.sync.SynchronizeTasksJob;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.modules.mylyn.util.BugtrackingCommand;
import org.netbeans.modules.mylyn.util.CancelableProgressMonitor;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.internal.Accessor;

/**
 *
 * @author Ondrej Vrabec
 */
public class SynchronizeTasksCommand extends BugtrackingCommand {
    private String stringValue;
    private final TaskRepository taskRepository;
    private final Set<NbTask> tasks;
    private final CancelableProgressMonitor monitor;
    private final AbstractRepositoryConnector repositoryConnector;
    private final TaskList taskList;
    private final TaskDataManager taskDataManager;
    private final RepositoryModel repositoryModel;
    private final boolean user;

    SynchronizeTasksCommand (AbstractRepositoryConnector repositoryConnector, TaskRepository taskRepository,
            RepositoryModel repositoryModel, TaskDataManager taskDataManager, TaskList taskList,
            Set<NbTask> tasks, boolean isUserAction) {
        this.repositoryConnector = repositoryConnector;
        this.taskRepository = taskRepository;
        this.repositoryModel = repositoryModel;
        this.taskDataManager = taskDataManager;
        this.taskList = taskList;
        this.tasks = tasks;
        this.monitor = new CancelableProgressMonitor();
        this.user = isUserAction;
    }

    @Override
    public void execute () throws CoreException, IOException, MalformedURLException {
        Logger log = Logger.getLogger(this.getClass().getName());
        if(log.isLoggable(Level.FINE)) {
            log.log(
                Level.FINE, 
                "executing SynchronizeTasksCommand for tasks {0}:{1}", //NOI18N
                new Object[] { taskRepository.getUrl(), tasks });
        }
        Set<ITask> mylynTasks = Accessor.getInstance().toMylynTasks(tasks);
        SynchronizeTasksJob job = new SynchronizeTasksJob(taskList,
                taskDataManager,
                repositoryModel,
                repositoryConnector,
                taskRepository,
                mylynTasks);
        job.setUser(user);
        job.run(monitor);
    }

    @Override
    public void cancel () {
        monitor.setCanceled(true);
    }
    
    @Override
    public String toString () {
        if(stringValue == null) {
            StringBuilder sb = new StringBuilder()
            .append("Synchronizing tasks ") //NOI18N
            .append(tasks)
            .append(",repository=") //NOI18N
            .append(taskRepository.getUrl())
            .append("]"); //NOI18N
            stringValue = sb.toString();
        }
        return stringValue;
    }
    
}
