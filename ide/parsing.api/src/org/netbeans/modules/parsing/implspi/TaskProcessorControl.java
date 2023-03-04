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

package org.netbeans.modules.parsing.implspi;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.impl.SourceAccessor;
import org.netbeans.modules.parsing.impl.TaskProcessor;
import org.openide.util.Parameters;

/**
 * Allows to control the parsing susbsytem operation.
 * @author sdedic
 * @since 9.2
 */
public final class TaskProcessorControl {
    /**
     * Initialize the parsing and scheduling system. The method should be called 
     * at "appropriate time", for example when the UI starts and is ready to accept
     * user input.
     */
    public static void initialize() {
        SourceAccessor.getINSTANCE().init();
    }

    /**
     * Suspends {@link SchedulerTask}s execution.
     * Cancels currently running {@link SchedulerTask} and do
     * not schedule any ready {@link SchedulerTask}.
     */
    public static void suspendSchedulerTasks(@NonNull final Source source) {
        Parameters.notNull("source", source);   //NOI18N
        TaskProcessor.resetState(source, true, true);
    }

    /**
     * Resumes {@link SchedulerTask}s execution.
     * Schedules ready {@link SchedulerTask}s.
     */
    public static void resumeSchedulerTasks() {
        TaskProcessor.resetStateImpl(null);
    }
}
