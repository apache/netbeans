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

import java.io.OutputStream;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.RepositoryModel;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskMigrationEvent;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.sync.SubmitJob;
import org.eclipse.mylyn.tasks.core.sync.SubmitJobEvent;
import org.eclipse.mylyn.tasks.core.sync.SubmitJobListener;
import org.netbeans.modules.mylyn.util.CancelableProgressMonitor;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.NbTaskDataModel;
import org.netbeans.modules.mylyn.util.internal.Accessor;
import org.netbeans.modules.mylyn.util.internal.CommandsAccessor;

/**
 *
 * @author Ondrej Vrabec
 */
public final class CommandFactory {
    
    static {
        // see static initializer of CommandAccessor
        CommandsAccessor.INSTANCE = new CommandsAccessorImpl();
    }
    
    private static final Logger LOG = Logger.getLogger(CommandFactory.class.getName());
    private final TaskList taskList;
    private final TaskDataManager taskDataManager;
    private final TaskRepositoryManager taskRepositoryManager;
    private final RepositoryModel repositoryModel;

    CommandFactory (TaskList taskList,
            TaskDataManager taskDataManager, TaskRepositoryManager taskRepositoryManager,
            RepositoryModel repositoryModel) {
        this.taskList = taskList;
        this.taskDataManager = taskDataManager;
        this.taskRepositoryManager = taskRepositoryManager;
        this.repositoryModel = repositoryModel;
    }

    public SynchronizeQueryCommand createSynchronizeQueriesCommand (TaskRepository taskRepository, IRepositoryQuery iquery) {
        return createSynchronizeQueriesCommand(taskRepository, iquery, new CancelableProgressMonitor());
    }
    
    public SynchronizeQueryCommand createSynchronizeQueriesCommand (TaskRepository taskRepository, IRepositoryQuery iquery, IProgressMonitor monitor) {
        assert iquery instanceof RepositoryQuery;
        RepositoryQuery repositoryQuery;
        if (iquery instanceof RepositoryQuery) {
            repositoryQuery = (RepositoryQuery) iquery;
        } else {
            return null;
        }
        AbstractRepositoryConnector repositoryConnector = taskRepositoryManager.getRepositoryConnector(taskRepository.getConnectorKind());
        return new SynchronizeQueryCommand(repositoryModel, repositoryConnector,
                taskRepository, taskList, taskDataManager, repositoryQuery, monitor);
    }
    
    /**
     * Returns a bugtracking command submitting the given task to the remote
     * repository.
     *
     * @param model task data to submit
     * @return the command ready to be executed
     * @throws CoreException problem while submitting
     */
    public SubmitTaskCommand createSubmitTaskCommand (NbTaskDataModel model) throws CoreException {
        final AbstractRepositoryConnector repositoryConnector;
        final ITask task = Accessor.getInstance().getITask(model);
        TaskRepository taskRepository = Accessor.getInstance().getTaskRepositoryFor(task);
        if (task.getSynchronizationState() == ITask.SynchronizationState.OUTGOING_NEW) {
            repositoryConnector = taskRepositoryManager.getRepositoryConnector(
                    task.getAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_CONNECTOR_KIND));
        } else {
            repositoryConnector = taskRepositoryManager.getRepositoryConnector(task.getConnectorKind());
        }

        SubmitTaskCommand command = new SubmitTaskCommand(taskDataManager,
                repositoryConnector,
                taskRepository,
                task,
                model.getLocalTaskData(), model.getChangedOldAttributes() /*??? no idea what's this good for*/);
        command.setSubmitJobListener(new SubmitJobListener() {
            @Override
            public void taskSubmitted (SubmitJobEvent event, IProgressMonitor monitor) throws CoreException {
            }

            @Override
            public void taskSynchronized (SubmitJobEvent event, IProgressMonitor monitor) throws CoreException {
            }

            @Override
            public void done (SubmitJobEvent event) {
                // turn into full task
                SubmitJob job = event.getJob();
                ITask newTask = job.getTask();
                if (newTask != null && newTask != task) {
                    // copy anything you want
                    taskList.deleteTask(task);
                    taskList.addTask(newTask);
                    repositoryConnector.migrateTask(new TaskMigrationEvent(task, newTask));
                    try {
                        taskDataManager.deleteTaskData(task);
                    } catch (CoreException ex) {
                        LOG.log(Level.INFO, null, ex);
                    }
                }
            }
        });

        return command;
    }

    /**
     * 
     * @param taskRepository
     * @param tasks
     * @return
     * @deprecated run {@link #createSynchronizeTasksCommand(org.eclipse.mylyn.tasks.core.TaskRepository, java.util.Set, boolean) }
     */
    @Deprecated
    public SynchronizeTasksCommand createSynchronizeTasksCommand (TaskRepository taskRepository, Set<NbTask> tasks) {
        return createSynchronizeTasksCommand(taskRepository, tasks, true);
    }

    /**
     * Synchronizes given tasks with their state in a repository.
     *
     * @param taskRepository repository
     * @param tasks tasks to synchronize
     * @param isUserAction when set to <code>true</code> mylyn will force the
     * refresh and may run certain additional tasks like fetching subtasks and
     * parent tasks.
     * @return
     */
    public SynchronizeTasksCommand createSynchronizeTasksCommand (TaskRepository taskRepository, Set<NbTask> tasks, boolean isUserAction) {
        AbstractRepositoryConnector repositoryConnector = taskRepositoryManager.getRepositoryConnector(taskRepository.getConnectorKind());
        return new SynchronizeTasksCommand(repositoryConnector, taskRepository,
                repositoryModel, taskDataManager, taskList, tasks, isUserAction);
    }

    public GetRepositoryTasksCommand createGetRepositoryTasksCommand (TaskRepository taskRepository, Set<String> taskIds) throws CoreException {
        AbstractRepositoryConnector repositoryConnector = taskRepositoryManager.getRepositoryConnector(taskRepository.getConnectorKind());
        GetRepositoryTasksCommand cmd = new GetRepositoryTasksCommand(repositoryConnector,
                taskRepository, taskIds, taskDataManager);
        return cmd;
    }

    public SimpleQueryCommand createSimpleQueryCommand (TaskRepository taskRepository, IRepositoryQuery query) throws CoreException {
        AbstractRepositoryConnector repositoryConnector = taskRepositoryManager.getRepositoryConnector(taskRepository.getConnectorKind());
        return new SimpleQueryCommand(repositoryConnector, taskRepository, taskDataManager, query);
    }

    public PostAttachmentCommand createPostAttachmentCommand (TaskRepository taskRepository, NbTask task,
            TaskAttribute attAttribute, FileTaskAttachmentSource attachmentSource, String comment) {
        AbstractRepositoryConnector repositoryConnector = taskRepositoryManager.getRepositoryConnector(taskRepository.getConnectorKind());
        return new PostAttachmentCommand(repositoryConnector, taskRepository,
                Accessor.getInstance().getDelegate(task),
                attAttribute, attachmentSource, comment);
    }

    public GetAttachmentCommand createGetAttachmentCommand (TaskRepository taskRepository, 
            NbTask nbTask, TaskAttribute ta, OutputStream os) {
        AbstractRepositoryConnector repositoryConnector = taskRepositoryManager.getRepositoryConnector(taskRepository.getConnectorKind());
        return new GetAttachmentCommand(repositoryConnector, taskRepository,
                Accessor.getInstance().getDelegate(nbTask), ta, os);
    }
}
