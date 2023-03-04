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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

/**
 *
 * @author Tomas Stupka
 */
public class GetAttachmentCommand extends BugtrackingCommand {

    private final TaskAttribute ta;
    private final TaskRepository taskRepository;
    private final AbstractRepositoryConnector repositoryConnector;
    private final OutputStream os;
    private String stringValue;
    private final ITask task;

    public GetAttachmentCommand(AbstractRepositoryConnector repositoryConnector, 
            TaskRepository taskRepository,
            ITask task,
            TaskAttribute ta,
            OutputStream os) {
        this.ta = ta;
        this.task = task;
        this.taskRepository = taskRepository;
        this.repositoryConnector = repositoryConnector;
        this.os = os;
    }

    @Override
    public void execute() throws CoreException, IOException, MalformedURLException {
        Logger log = Logger.getLogger(this.getClass().getName());
        if(log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "executing GetTaskDataCommand for attachment id: {0}", ta.getValue()); // NOI18N
        }
        AbstractTaskAttachmentHandler taskAttachmentHandler = repositoryConnector.getTaskAttachmentHandler();
        if (!taskAttachmentHandler.canGetContent(taskRepository, task)) {
            throw new IOException("Cannot get content for attachment with id: " + ta.getValue());
        }
        InputStream is = taskAttachmentHandler.getContent(taskRepository, task, ta, new NullProgressMonitor());
        try {
            byte [] buffer = new byte[4096];
            int n;
            while ((n = is.read(buffer)) != -1) {
                os.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }
    }

    @Override
    public String toString() {
        if (stringValue == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("GetAttachmentCommand [repository=");                     // NOI18N
            sb.append(taskRepository.getUrl());
            sb.append(",attachmentID=");                                        // NOI18N
            sb.append(ta.getValue());
            sb.append("]");                                                     // NOI18N
            stringValue = sb.toString();
        }
        return stringValue;
        
    }

}
