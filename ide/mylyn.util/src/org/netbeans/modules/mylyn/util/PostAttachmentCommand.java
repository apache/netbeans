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
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

/**
 *
 * @author Tomas Stupka
 */
public class PostAttachmentCommand extends BugtrackingCommand {

    private final TaskAttribute attAttribute;
    private final TaskRepository taskRepository;
    private final AbstractRepositoryConnector repositoryConnector;
    private String stringValue;
    private final ITask task;
    private final AbstractTaskAttachmentSource taskAttachmentSource;
    private final String comment;

    public PostAttachmentCommand (AbstractRepositoryConnector repositoryConnector, 
            TaskRepository taskRepository,
            ITask task,
            TaskAttribute ta,
            AbstractTaskAttachmentSource taskAttachmentSource,
            String comment) {
        this.attAttribute = ta;
        this.task = task;
        this.taskRepository = taskRepository;
        this.repositoryConnector = repositoryConnector;
        this.taskAttachmentSource = taskAttachmentSource;
        this.comment = comment;
    }

    @Override
    public void execute() throws CoreException, IOException, MalformedURLException {
        Logger log = Logger.getLogger(this.getClass().getName());
        if(log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "executing PostTaskDataCommand for task: {0}", task.getTaskId()); // NOI18N
        }
        AbstractTaskAttachmentHandler taskAttachmentHandler = repositoryConnector.getTaskAttachmentHandler();
        if (!taskAttachmentHandler.canPostContent(taskRepository, task)) {
            throw new IOException("Cannot post attachment for task with id: " + task.getTaskId());
        }
        taskAttachmentHandler.postContent(taskRepository, task, taskAttachmentSource, comment, attAttribute, new NullProgressMonitor());
    }

    @Override
    public String toString() {
        if(stringValue == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("PostAttachmentCommand [repository=");                    // NOI18N
            sb.append(taskRepository.getUrl());
            sb.append(",id=");                                                  // NOI18N
            sb.append(task.getTaskId());
            sb.append(",comment=");                                             // NOI18N
            sb.append(comment);
            sb.append(",attachment=");                                          // NOI18N
            sb.append(taskAttachmentSource.getName());
            sb.append(",desc=");                                                // NOI18N
            TaskAttribute ta = attAttribute.getMappedAttribute(TaskAttribute.ATTACHMENT_DESCRIPTION);
            sb.append(ta != null ? ta.getValue() : "");                         // NOI18N
            sb.append(",filename=");                                            // NOI18N
            ta = attAttribute.getMappedAttribute(TaskAttribute.ATTACHMENT_FILENAME);
            sb.append(ta != null ? ta.getValue() : "");                         // NOI18N
            sb.append(",contentType=");                                         // NOI18N
            ta = attAttribute.getMappedAttribute(TaskAttribute.ATTACHMENT_CONTENT_TYPE);
            sb.append(ta != null ? ta.getValue() : "");                         // NOI18N
            sb.append("]");                                                     // NOI18N
            stringValue = sb.toString();
        }
        return stringValue;
        
    }

}
