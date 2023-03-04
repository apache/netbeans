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

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;

/**
 * Retrieves the TaskData for all given issue ids
 * 
 * @author Tomas Stupka
 */
public class GetMultiTaskDataCommand extends BugtrackingCommand {

    private final AbstractRepositoryConnector repositoryConnector;
    private final TaskRepository taskRepository;
    private final Set<String> ids;
    private final TaskDataCollector collector;

    public GetMultiTaskDataCommand(AbstractRepositoryConnector repositoryConnector, TaskRepository taskRepository, TaskDataCollector collector, Set<String> ids) {
        this.taskRepository = taskRepository;
        this.repositoryConnector = repositoryConnector;
        this.ids = ids;
        this.collector = collector;
    }

    @Override
    public void execute() throws CoreException {
        
        Logger log = Logger.getLogger(this.getClass().getName());
        if(log.isLoggable(Level.FINE)) {
            log.log(
                Level.FINE, 
                "executing GetMultiTaskDataCommand for tasks: {0}", // NOI18N
                print(ids));    
        }
        
        repositoryConnector.getTaskDataHandler().getMultiTaskData(
                taskRepository,
                ids,
                collector,
                new NullProgressMonitor());
    }

    private String print(Set<String> ids) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String string : ids) {
            sb.append(string);
            if(++i < ids.size()) {
                sb.append(",");                                                 // NOI18N
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GetMultiTaskDataCommand [repository=");                      // NOI18N
        sb.append(taskRepository.getUrl());
        sb.append(",...]");                                                     // NOI18N
        return sb.toString();
    }

}
