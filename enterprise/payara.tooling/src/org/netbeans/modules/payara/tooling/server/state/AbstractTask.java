/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.payara.tooling.server.state;

import org.netbeans.modules.payara.tooling.TaskState;
import org.netbeans.modules.payara.tooling.TaskStateListener;
import org.netbeans.modules.payara.tooling.data.PayaraStatusCheck;
import org.netbeans.modules.payara.tooling.TaskEvent;
import org.netbeans.modules.payara.tooling.logging.Logger;

/**
 * Abstract task for server status verification.
 * <p/>
 * @author Tomas Kraus
 */
public abstract class AbstractTask implements Runnable {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(AbstractTask.class);

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Server status check job internal data. */
    final StatusJob job;

    /** Individual status check task data. */
    final StatusJob.Task task;

    /** Internal job status when this task was created. */
    final StatusJobState jobState;
    
    /** Server status check type. */
    final PayaraStatusCheck type;

    /** Listeners that want to know about command state. */
    final TaskStateListener[] stateListeners;

    /** Cancellation notification. */
    boolean cancelled;

    /**
     * Creates an instance of abstract task for server status verification.
     * <p/>
     * @param job  Server status check job internal data.
     * @param task Individual status check task data.
     * @param type Server status check type.
     */
    AbstractTask(final StatusJob job, final StatusJob.Task task,
            final PayaraStatusCheck type) {
        this.job = job;
        this.task = task;
        this.jobState = job.getState();
        this.type = type;
        this.stateListeners = task.getListeners();
        this.cancelled = false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Mark this task as canceled.
     * <p/>
     * Listeners won't be notified about server status verification task state
     * change after task was canceled.
     */
    void cancel() {
        cancelled = true;
    }

    /**
     * Notify all registered task state listeners server status verification
     * task state change.
     * <p/>
     * This method should be used after task is submitted into
     * <code>ExecutorService</code>.
     * <p/>
     * @param taskState New task execution state.
     * @param taskEvent Event related to execution state change.
     * @param args      Additional arguments.
     */
    void handleStateChange(final TaskState taskState,
            final TaskEvent taskEvent, final String... args) {
        if (stateListeners != null && !cancelled) {
            for (int i = 0; i < stateListeners.length; i++) {
                if (stateListeners[i] != null) {
                    stateListeners[i].operationStateChanged(taskState,
                            taskEvent, args);
                }
            }
        }
    }

}
