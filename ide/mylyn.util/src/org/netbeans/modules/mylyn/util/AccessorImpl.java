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

import java.util.Collection;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.modules.mylyn.util.internal.Accessor;

/**
 *
 * @author Ondrej Vrabec
 */
class AccessorImpl extends Accessor {
    
    private static AccessorImpl instance;
    
    public static AccessorImpl getInstance () {
        if (instance == null) {
            instance = new AccessorImpl();
            Accessor.setInstance(instance);
        }
        return instance;
    }

    @Override
    public void finishMylyn () throws CoreException {
        MylynSupport.getInstance().finish();
    }

    @Override
    public Collection<NbTask> toNbTasks (Set<ITask> tasks) {
        return MylynSupport.getInstance().toNbTasks(tasks);
    }

    @Override
    public NbTask toNbTask (ITask task) {
        return MylynSupport.getInstance().toNbTask(task);
    }

    @Override
    public Set<ITask> toMylynTasks (Set<NbTask> tasks) {
        return MylynSupport.toMylynTasks(tasks);
    }

    @Override
    public ITask getITask (NbTaskDataModel model) {
        return model.getDelegateTask();
    }

    @Override
    public TaskRepository getTaskRepositoryFor (ITask task) {
        return MylynSupport.getInstance().getTaskRepositoryFor(task);
    }

    @Override
    public ITask getDelegate (NbTask task) {
        return task.getDelegate();
    }

    @Override
    public NbTask getOrCreateTask (TaskRepository taskRepository, String taskId, boolean addToTasklist) throws CoreException {
        return MylynSupport.getInstance().getOrCreateTask(taskRepository, taskId, addToTasklist);
    }

    @Override
    public void taskModified (NbTask nbTask) {
        MylynSupport.getInstance().taskModified(getDelegate(nbTask));
    }
}
