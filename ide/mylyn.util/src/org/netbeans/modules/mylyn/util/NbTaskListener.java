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
import org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta;

/**
 *
 * @author Ondrej Vrabec
 */
public interface NbTaskListener extends EventListener {
    
    public void taskModified (TaskEvent event);
    
    public static final class TaskEvent extends EventObject {
        private final NbTask task;
        private final Kind kind;
        private boolean stateChanged;
        
        TaskEvent (NbTask task, TaskContainerDelta delta, boolean stateChanged) {
            super(task);
            this.task = task;
            this.stateChanged = stateChanged;
            switch (delta.getKind()) {
                case DELETED:
                    this.kind = Kind.DELETED;
                    break;
                default:
                    this.kind = Kind.MODIFIED;
            }
        }
        
        public NbTask getTask () {
            return task;
        }

        public Kind getKind () {
            return kind;
        }

        public boolean taskStateChanged () {
            return stateChanged;
        }
        
        public static enum Kind {
            DELETED,
            MODIFIED
        }
    }
    
}
