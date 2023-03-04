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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;

/**
 * Perfoms a repository query
 * 
 * @author Tomas Stupka
 */
public class PerformQueryCommand extends BugtrackingCommand {

    private final AbstractRepositoryConnector repositoryConnector;
    private final TaskRepository taskRepository;
    private final IRepositoryQuery query;
    private final TaskDataCollector collector;
    private IStatus status;
    
    public PerformQueryCommand(AbstractRepositoryConnector repositoryConnector, TaskRepository taskRepository, TaskDataCollector collector, IRepositoryQuery query) {
        this.taskRepository = taskRepository;
        this.repositoryConnector = repositoryConnector;
        this.query = query;
        this.collector = collector;
    }

    @Override
    public void execute() throws CoreException {
        
        Logger log = Logger.getLogger(this.getClass().getName());
        if(log.isLoggable(Level.FINE)) {
            Map<String, String> attrs = query.getAttributes();
            log.log(
                Level.FINE, 
                "executing PerformQueryCommand for query {0} on repository {1} with url \n\t{2} and parameters \n\t{3}", // NOI18N
                new Object[] {query.getSummary(), taskRepository.getUrl(), query.getUrl(), attrs != null ? attrs : null});
        }
        
        status = repositoryConnector.performQuery(taskRepository, query, collector, null, new NullProgressMonitor());
    }

    public IStatus getStatus() {
        return status;
    }

    public IRepositoryQuery getQuery() {
        return query;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PerformQueryCommand [repository=");
        sb.append(taskRepository.getUrl());
        sb.append(", summary=");
        sb.append(query.getSummary()); 
        sb.append(", url=");
        sb.append(query.getUrl()); // XXX won't work for all queries
        sb.append("]");
        return super.toString();
    }


}
