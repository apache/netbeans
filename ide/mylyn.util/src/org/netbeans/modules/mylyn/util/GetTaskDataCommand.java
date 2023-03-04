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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;

/**
 *
 * @author Tomas Stupka
 */
public class GetTaskDataCommand extends BugtrackingCommand {

    private final String id;
    private final TaskRepository taskRepository;
    private final AbstractRepositoryConnector repositoryConnector;
    private TaskData taskData;

    public GetTaskDataCommand(AbstractRepositoryConnector repositoryConnector, TaskRepository taskRepository, String id) {
        this.id = id;
        this.taskRepository = taskRepository;
        this.repositoryConnector = repositoryConnector;
    }

    @Override
    public void execute() throws CoreException, IOException, MalformedURLException {
        
        Logger log = Logger.getLogger(this.getClass().getName());
        if(log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "executing GetTaskDataCommand for task: {0}", id); // NOI18N
        }
        
        taskData = repositoryConnector.getTaskData(taskRepository, id, new NullProgressMonitor());
    }

    public TaskData getTaskData() {
        return taskData;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GetTaskDataCommand [repository=");                       // NOI18N
        sb.append(taskRepository.getUrl());
        sb.append(",id=");                                                  // NOI18N
        sb.append(id);
        sb.append("]");                                                     // NOI18N
        return  sb.toString();
        
    }

}
