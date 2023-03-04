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

import java.util.EventListener;
import java.util.EventObject;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManagerEvent;
import org.eclipse.mylyn.tasks.core.data.TaskData;

/**
 *
 * @author Ondrej Vrabec
 */
public interface TaskDataListener extends EventListener {
    
    public void taskDataUpdated (TaskDataEvent event);
    
    public static final class TaskDataEvent extends EventObject {
        private final TaskDataManagerEvent event;

        TaskDataEvent (TaskDataManagerEvent event) {
            super(event.getSource());
            this.event = event;
        }
        
        public NbTask getTask () {
            return MylynSupport.getInstance().toNbTask(event.getTask());
        }

        /**
         * May be <code>null</code>
         * @return 
         */
        public TaskData getTaskData () {
            return event.getTaskData();
        }

        public boolean getTaskDataUpdated () {
            return event.getTaskDataUpdated();
        }
        
    }
}
