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

package org.netbeans.modules.mylyn.util.localtasks.internal;

import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;

/**
 *
 * @author Ondrej Vrabec
 */
public final class LocalTaskDataHandler extends AbstractTaskDataHandler {
    public static final String ATTRIBUTE_KEY_SUMMARY = TaskAttribute.SUMMARY;
    public static final String ATTRIBUTE_KEY_NB_ATTACHMENTS = "nb.attachments"; //NOI18N
    public static final String ATTRIBUTE_KEY_NB_REFERENCES = "nb.taskreferences"; //NOI18N
    private final TaskAttributeMapper mapper;

    public LocalTaskDataHandler (TaskRepository taskRepository) {
        this.mapper = new TaskAttributeMapper(taskRepository);
    }
    
    @Override
    public RepositoryResponse postTaskData (TaskRepository repository, TaskData taskData, Set<TaskAttribute> oldAttributes, IProgressMonitor monitor) throws CoreException {
        return null;
    }

    @Override
    public boolean initializeTaskData (TaskRepository repository, TaskData data, ITaskMapping initializationData, IProgressMonitor monitor) throws CoreException {
        TaskAttribute ta = data.getRoot().createAttribute(ATTRIBUTE_KEY_SUMMARY);
        String value = initializationData.getSummary();
        if (value != null) {
            ta.setValue(value);
        }
        data.getRoot().createAttribute(ATTRIBUTE_KEY_NB_ATTACHMENTS);
        data.getRoot().createAttribute(ATTRIBUTE_KEY_NB_REFERENCES);
        return true;
    }

    @Override
    public TaskAttributeMapper getAttributeMapper (TaskRepository repository) {
        return mapper;
    }
    
}
