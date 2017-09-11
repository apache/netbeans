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
package org.netbeans.modules.mylyn.util.commands;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener;
import org.eclipse.mylyn.internal.tasks.core.RepositoryModel;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.internal.tasks.core.sync.SynchronizeQueriesJob;
import org.eclipse.mylyn.internal.tasks.core.sync.SynchronizeTasksJob;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.modules.team.commons.LogUtils;
import org.netbeans.modules.mylyn.util.BugtrackingCommand;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.internal.Accessor;

/**
 *
 * @author Ondrej Vrabec
 */
public class SynchronizeQueryCommand extends BugtrackingCommand {
    private String stringValue;
    private final TaskRepository taskRepository;
    private final RepositoryQuery query;
    private final IProgressMonitor monitor;
    private final List<CommandProgressListener> listeners = new CopyOnWriteArrayList<CommandProgressListener>();
    private final TaskList taskList;
    private final AbstractRepositoryConnector repositoryConnector;
    private final RepositoryModel repositoryModel;
    private final TaskDataManager taskDataManager;
    private IStatus status;
    
    SynchronizeQueryCommand (RepositoryModel repositoryModel, 
            AbstractRepositoryConnector repositoryConnector, TaskRepository taskRepository,
            TaskList taskList, TaskDataManager taskDataManager, RepositoryQuery query, IProgressMonitor monitor) 
    {
        this.repositoryModel = repositoryModel;
        this.repositoryConnector = repositoryConnector;
        this.taskRepository = taskRepository;
        this.taskList = taskList;
        this.taskDataManager = taskDataManager;
        this.query = query;
        this.monitor = monitor;
    }

    @Override
    public void execute () throws CoreException, IOException, MalformedURLException {
        LogUtils.logBugtrackingUsage(repositoryConnector.getConnectorKind(), "ISSUE_QUERY");
        final Accessor accessor = Accessor.getInstance();
        Logger log = Logger.getLogger(this.getClass().getName());
        if(log.isLoggable(Level.FINE)) {
            log.log(
                Level.FINE, 
                "executing SynchronizeQueryCommand for query {0}:{1}", //NOI18N
                new Object[] { taskRepository.getUrl(), query.getSummary() });
        }
        
        SynchronizeQueriesJob job = new SynchronizeQueriesJob(taskList, taskDataManager, repositoryModel,
                repositoryConnector, taskRepository, Collections.<RepositoryQuery>singleton(query));
        
        final Set<ITask> tasksToSynchronize = Collections.synchronizedSet(new HashSet<ITask>());
        // in the end this will contain tasks removed from the query
        final Set<ITask> pendingToRefreshTasks = Collections.synchronizedSet(new HashSet<ITask>());
        final Set<ITask> toSync = new HashSet<ITask>();
        ITaskListChangeListener list = new ITaskListChangeListener() {
            @Override
            public void containersChanged (Set<TaskContainerDelta> deltas) {
                for (TaskContainerDelta delta : deltas) {
                    if (query == delta.getElement() && delta.getKind() == TaskContainerDelta.Kind.CONTENT) {
                        if (!query.isSynchronizing()) {
                            // if sync ended -> fire event, and collect tasks to refresh
                            tasksToSynchronize.addAll(query.getChildren());
                            // tasks newly added to the query
                            Set<ITask> addedTasks = new HashSet<ITask>(tasksToSynchronize);
                            addedTasks.removeAll(pendingToRefreshTasks);
                            // split tasks into tasks already present in the query -> tasksToSynchronize
                            // and tasks either removed or added to the query
                            tasksToSynchronize.removeAll(addedTasks);
                            pendingToRefreshTasks.removeAll(tasksToSynchronize);
                            pendingToRefreshTasks.addAll(addedTasks);
                            
                            toSync.addAll(tasksToSynchronize);
                            toSync.addAll(pendingToRefreshTasks);
                            Collection<NbTask> nbTasks = accessor.toNbTasks(toSync);
                            for (CommandProgressListener cmdList : listeners.toArray(new CommandProgressListener[listeners.size()])) {
                                cmdList.tasksRefreshStarted(nbTasks);
                            }
                        }
                    } else if (delta.getElement() instanceof ITask) {
                        ITask task = (ITask) delta.getElement();
                        if (delta.getKind() == TaskContainerDelta.Kind.CONTENT && task instanceof AbstractTask
                                && !((AbstractTask) task).isSynchronizing()) {
                            pendingToRefreshTasks.remove(task);
                            tasksToSynchronize.remove(task);
                            if (toSync.remove(task) && !monitor.isCanceled()) {
                                // task finished synchronize
                                for (CommandProgressListener cmdList : listeners.toArray(new CommandProgressListener[listeners.size()])) {
                                    cmdList.taskSynchronized(accessor.toNbTask(task));
                                }
                            }
                        } else if (!delta.isTransient() && delta.getParent() == query) {
                            if (delta.getKind() == TaskContainerDelta.Kind.REMOVED) {
                                for (CommandProgressListener cmdList : listeners.toArray(new CommandProgressListener[listeners.size()])) {
                                    cmdList.taskRemoved(accessor.toNbTask(task));
                                }
                            } else if (delta.getKind() == TaskContainerDelta.Kind.ADDED) {
                                for (CommandProgressListener cmdList : listeners.toArray(new CommandProgressListener[listeners.size()])) {
                                    cmdList.taskAdded(accessor.toNbTask(task));
                                }
                            }
                        }
                    }
                }
            }
        };
        taskList.addChangeListener(list);
        try {
            query.setSynchronizing(true);
            pendingToRefreshTasks.addAll(query.getChildren());
            for (CommandProgressListener cmdList : listeners.toArray(new CommandProgressListener[listeners.size()])) {
                cmdList.queryRefreshStarted(accessor.toNbTasks(pendingToRefreshTasks));
            }
            job.run(monitor);
            status = query.getStatus();
            if (status != null && status.getSeverity() == IStatus.ERROR) {
                if (status.getException() instanceof CoreException) {
                    throw (CoreException) status.getException();
                } else {
                    throw new CoreException(status);
                }
            }
            if (monitor.isCanceled()) {
                return;
            }
            // at this point caller was notified about modified tasks
            // but not about unchanged tasks
            for (ITask task : new ArrayList<ITask>(tasksToSynchronize)) {
                for (CommandProgressListener cmdList : listeners.toArray(new CommandProgressListener[listeners.size()])) {
                    cmdList.taskSynchronized(accessor.toNbTask(task));
                }
            }
            // now refresh also tasks not yet refreshed but either removed or added to the query
            if (!monitor.isCanceled() && !pendingToRefreshTasks.isEmpty()) {
                HashSet<ITask> tasks = new HashSet<ITask>(pendingToRefreshTasks);
                SynchronizeTasksJob syncTasksJob = new SynchronizeTasksJob(taskList,
                    taskDataManager,
                    repositoryModel,
                    repositoryConnector,
                    taskRepository,
                    tasks);
                for (ITask t : tasks) {
                    if (t instanceof AbstractTask) {
                        ((AbstractTask) t).setSynchronizing(true);
                    }
                }
                syncTasksJob.run(monitor);
            }
        } finally {
            taskList.removeChangeListener(list);
        }
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
            .append(query)
            .append(",repository=") //NOI18N
            .append(taskRepository.getUrl())
            .append("]"); //NOI18N
            stringValue = sb.toString();
        }
        return stringValue;
    }
    
    public void addCommandProgressListener (CommandProgressListener list) {
        listeners.add(list);
    }
    
    public void removeCommandProgressListener (CommandProgressListener list) {
        listeners.remove(list);
    }

    public IStatus getStatus () {
        return status;
    }
    
    public static interface CommandProgressListener extends EventListener {
        
        public void queryRefreshStarted (Collection<NbTask> tasks);
        
        public void tasksRefreshStarted (Collection<NbTask> tasks);
        
        public void taskAdded (NbTask task);
        
        public void taskRemoved (NbTask task);
        
        public void taskSynchronized (NbTask task);
    }
}
