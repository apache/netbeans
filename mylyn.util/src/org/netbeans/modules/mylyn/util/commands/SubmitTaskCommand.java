/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
