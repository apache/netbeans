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
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;

/**
 *
 * @author Tomas Stupka
 */
public class SubmitCommand extends BugtrackingCommand {

    private final AbstractRepositoryConnector repositoryConnector;
    private final TaskRepository taskRepository;
    private final TaskData data;
    private RepositoryResponse rr;
    private boolean wasNew;
    private String stringValue;

    public SubmitCommand(AbstractRepositoryConnector repositoryConnector, TaskRepository taskRepository, TaskData data) {
        this.taskRepository = taskRepository;
        this.repositoryConnector = repositoryConnector;
        this.data = data;
        wasNew = data.isNew();
    }

    @Override
    public void execute() throws CoreException, IOException, MalformedURLException {
        
        Logger log = Logger.getLogger(this.getClass().getName());
        if(log.isLoggable(Level.FINE)) {
            log.log(
                Level.FINE, 
                "executing SubmitCommand for taskData with id {0} ", // NOI18N
                data.getTaskId());
        }
        
        rr = repositoryConnector.getTaskDataHandler().postTaskData(taskRepository, data, null, new NullProgressMonitor());
        // XXX evaluate rr
    }

    public RepositoryResponse getRepositoryResponse() {
        return rr;
    }

    @Override
    public String toString() {
        if(stringValue == null) {
            StringBuilder sb = new StringBuilder();
            if(wasNew) {
                sb.append("SubmitCommand new issue [repository=");              // NOI18N
                sb.append(taskRepository.getUrl());
                sb.append("]");                                                 // NOI18N
            } else {
                sb.append("SubmitCommand [issue #");                            // NOI18N
                sb.append(data.getTaskId());
                sb.append(",repository=");                                      // NOI18N
                sb.append(taskRepository.getUrl());
                sb.append("]");                                                 // NOI18N
            }
            stringValue = sb.toString();
        }
        return stringValue;
    }
}
