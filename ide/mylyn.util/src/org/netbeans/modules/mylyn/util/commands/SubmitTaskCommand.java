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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.internal.tasks.core.sync.SubmitTaskJob;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.sync.SubmitJobListener;
import org.netbeans.modules.team.commons.LogUtils;
import org.netbeans.modules.mylyn.util.BugtrackingCommand;
import org.netbeans.modules.mylyn.util.CancelableProgressMonitor;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.internal.Accessor;

/**
 *
 * @author Ondra Vrabec
 */
public class SubmitTaskCommand extends BugtrackingCommand {

    private final CancelableProgressMonitor monitor;
    private RepositoryResponse rr;
    private NbTask submittedTask;
    private final Set<TaskAttribute> changedOldAttributes;
    private final TaskData taskData;
    private final ITask task;
    private final TaskRepository taskRepository;
    private final AbstractRepositoryConnector repositoryConnector;
    private final TaskDataManager taskDataManager;
    private String stringValue;
    private SubmitJobListener submitJobListener;

    SubmitTaskCommand (TaskDataManager taskDataManager,
            AbstractRepositoryConnector repositoryConnector,
            TaskRepository taskRepository, ITask task, TaskData taskData,
            Set<TaskAttribute> changedOldAttributes) {
        this.taskDataManager = taskDataManager;
        this.repositoryConnector = repositoryConnector;
        this.taskRepository = taskRepository;
        this.task = task;
        this.taskData = taskData;
        this.changedOldAttributes = changedOldAttributes;
        this.monitor = new CancelableProgressMonitor();
    }

    @Override
    public void execute() throws CoreException, IOException, MalformedURLException {
        
        LogUtils.logBugtrackingUsage(repositoryConnector.getConnectorKind(), "ISSUE_EDIT");
        
        MylynSubmitTaskJob job = new MylynSubmitTaskJob(taskDataManager, repositoryConnector, taskRepository,
                task, taskData, changedOldAttributes);
        if (submitJobListener != null) {
            job.addSubmitJobListener(submitJobListener);
        }
        
        Logger log = Logger.getLogger(this.getClass().getName());
        if(log.isLoggable(Level.FINE)) {
            log.log(
                Level.FINE, 
                "executing SubmitJobCommand for task with id {0}:{1} ", //NOI18N
                new Object[] { task.getTaskId(), taskRepository.getUrl() });
        }
        
        job.startJob(monitor);
        IStatus status = job.getStatus();
        rr = job.getResponse();
        submittedTask = Accessor.getInstance().toNbTask(job.getTask());
        if (status != null && status != Status.OK_STATUS) {
            log.log(Level.INFO, "Command failed with status: {0}", status); //NOI18N
            if (status.getException() instanceof CoreException) {
                throw (CoreException) status.getException();
            } else {
                throw new CoreException(status);
            }
        }
    }

    @Override
    public String toString() {
        if(stringValue == null) {
            StringBuilder sb = new StringBuilder();
            if (task.getSynchronizationState() == ITask.SynchronizationState.OUTGOING_NEW) {
                sb.append("SubmitTaskCommand new issue [repository="); //NOI18N
                sb.append(taskRepository.getUrl());
                sb.append("]"); //NOI18N
            } else {
                sb.append("SubmitTaskCommand [task #"); //NOI18N
                sb.append(taskData.getTaskId());
                sb.append(",repository="); //NOI18N
                sb.append(taskRepository.getUrl());
                sb.append("]"); //NOI18N
            }
            stringValue = sb.toString();
        }
        return stringValue;
    }

    @Override
    public void cancel () {
        monitor.setCanceled(true);
    }
    
    /**
     * Returns the persistent task if the task to submit was a fresh new task,
     * otherwise the same task.
     * 
     */
    public NbTask getSubmittedTask () {
        return submittedTask;
    }

    public RepositoryResponse getRepositoryResponse () {
        return rr;
    }

    void setSubmitJobListener (SubmitJobListener submitJobListener) {
        this.submitJobListener = submitJobListener;
    }
    
    private static class MylynSubmitTaskJob extends SubmitTaskJob {
        
        public MylynSubmitTaskJob (TaskDataManager taskDataManager, AbstractRepositoryConnector connector,
                TaskRepository taskRepository, ITask task, TaskData taskData, Set<TaskAttribute> oldAttributes) {
            super(taskDataManager, connector, taskRepository, task, taskData, oldAttributes);
        }

        public IStatus startJob (IProgressMonitor jobMonitor) {
            return run(jobMonitor);
        }
    };
}
