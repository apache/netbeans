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
package org.netbeans.modules.payara.common;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.tooling.TaskEvent;
import org.netbeans.modules.payara.tooling.TaskState;
import org.netbeans.modules.payara.tooling.TaskStateListener;
import org.netbeans.modules.payara.common.utils.ServerUtils;

/**
 * Asynchronous Payara server termination task.
 * @author Tomas Kraus
 */
public class KillTask extends BasicTask<TaskState> {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER = PayaraLogger.get(StartTask.class);

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara server termination task.
     * <p/>
     * @param instance Payara instance accessed in this task.
     * @param stateListener Callback listeners used to retrieve state changes.
     */
    public KillTask(PayaraInstance instance,
            TaskStateListener... stateListener) {
        super(instance, stateListener);
        taskThread = null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // ExecutorService call() Method                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Asynchronous task method started by {@see Executors}.
     * <p/>
     * @return Task execution result.
     */
    @Override
    public TaskState call() {
        setTaskThread();
        TaskState state;
        LOGGER.log(Level.FINEST,
                "[0] Payara server termination task started",
                taskThread.getName());
        Process process = instance.getProcess();
        if (process == null) {
            return fireOperationStateChanged(
                    TaskState.FAILED, TaskEvent.PROCESS_NOT_EXISTS,
                    "KillTask.call.noProcess", instanceName);
        }
        if (!ServerUtils.isProcessRunning(process)) {
            // Clear process stored in instance when already finished.
            return fireOperationStateChanged(
                    TaskState.FAILED, TaskEvent.PROCESS_NOT_RUNNING,
                    "KillTask.call.finished", instanceName);
        }
        fireOperationStateChanged(
                TaskState.RUNNING, TaskEvent.CMD_RUNNING,
                "KillTask.call.running", instanceName);
        state = kill(process);
        clearTaskThread();
        return state;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Terminate running Payara server task.
     * <p/>
     * @param process Payara server running process to be terminated.
     */
    private TaskState kill(final Process process) {
        process.destroy();
        StateChange stateChange = waitShutDown();
        if (stateChange != null) {
            return stateChange.fireOperationStateChanged();
        }
        // Clear process stored in instance after being killed.
        instance.setProcess(null);
        return fireOperationStateChanged(
                TaskState.COMPLETED, TaskEvent.CMD_COMPLETED,
                "KillTask.kill.completed", instanceName);
    }

}
