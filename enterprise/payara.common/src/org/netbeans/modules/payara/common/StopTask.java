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

import org.netbeans.modules.payara.tooling.admin.CommandStopDAS;
import org.netbeans.modules.payara.tooling.admin.CommandStopCluster;
import org.netbeans.modules.payara.tooling.admin.ResultString;
import org.netbeans.modules.payara.tooling.admin.CommandStopInstance;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.tooling.TaskEvent;
import org.netbeans.modules.payara.tooling.TaskState;
import org.netbeans.modules.payara.tooling.TaskStateListener;
import org.netbeans.modules.payara.common.utils.Util;
import org.netbeans.modules.payara.spi.PayaraModule;


/**
 * @author Ludovic Chamenois
 * @author Peter Williams
 */
public class StopTask extends BasicTask<TaskState> {

    private final CommonServerSupport support;

    /**
     * 
     * @param support common support object for the server instance being stopped
     * @param stateListener state monitor to track start progress
     */
    public StopTask(CommonServerSupport support,
            TaskStateListener... stateListener) {
        super(support.getInstance(), stateListener);
        this.support = support;
    }
    
    /**
     * 
     */
    @SuppressWarnings("SleepWhileInLoop")
    @Override
    public TaskState call() {
        // save the current time so that we can deduct that the startup
        // failed due to timeout
        Logger.getLogger("payara").log(Level.FINEST,
                "StopTask.call() called on thread \"{0}\"",
                Thread.currentThread().getName()); // NOI18N
        long start = System.currentTimeMillis();
        
        String host; // = null;
        int port;
        
        host = instance.getProperty(PayaraModule.HOSTNAME_ATTR);
        if(host == null || host.length() == 0) {
            return fireOperationStateChanged(TaskState.FAILED,
                    TaskEvent.CMD_FAILED,
                    "MSG_START_SERVER_FAILED_NOHOST", instanceName);
        }
               
        try {
            port = Integer.valueOf(instance.getProperty(PayaraModule.ADMINPORT_ATTR));
            if(port < 0 || port > 65535) {
                return fireOperationStateChanged(TaskState.FAILED,
                        TaskEvent.CMD_FAILED,
                        "MSG_START_SERVER_FAILED_BADPORT", instanceName);
            }
        } catch(NumberFormatException ex) {
            return fireOperationStateChanged(TaskState.FAILED,
                    TaskEvent.CMD_FAILED,
                    "MSG_START_SERVER_FAILED_BADPORT", instanceName);
        }

        String target = Util.computeTarget(instance.getProperties());

        if (!Util.isDefaultOrServerTarget(instance.getProperties())) {
            // stop an instance/cluster
            return stopClusterOrInstance(target);
        }

        // stop a domain

        // !PW Can we have a single manager instance per instance, available on
        // demand through lookup?
        // !PW FIXME this uses doubly nested runnables.  Can we fix?
        ResultString result = CommandStopDAS.stopDAS(instance);
        if (TaskState.FAILED.equals(result.getState())) {
             fireOperationStateChanged(TaskState.FAILED, TaskEvent.CMD_FAILED,
                     "MSG_STOP_SERVER_FAILED", instanceName);
        }
        
        fireOperationStateChanged(TaskState.RUNNING, TaskEvent.CMD_RUNNING,
                "MSG_STOP_SERVER_IN_PROGRESS", instanceName); // NOI18N
        
        StateChange stateChange = waitShutDown();
        if (stateChange != null) {
            return stateChange.fireOperationStateChanged();
        }
        return fireOperationStateChanged(TaskState.COMPLETED,
                TaskEvent.CMD_COMPLETED,
                "MSG_SERVER_STOPPED", instanceName);
    }
    
    private TaskState stopClusterOrInstance(String target) {
        ResultString result = CommandStopCluster.stopCluster(instance, target);

        if (TaskState.FAILED.equals(result.getState())) {
            // if start-cluster not successful, try start-instance
            result = CommandStopInstance.stopInstance(instance, target);
            if (TaskState.FAILED.equals(result.getState())) {
                // if start instance not suscessful fail
                return fireOperationStateChanged(TaskState.FAILED,
                        TaskEvent.CMD_FAILED,
                        "MSG_STOP_TARGET_FAILED", instanceName, target);
            }
        }

        return fireOperationStateChanged(TaskState.COMPLETED,
                TaskEvent.CMD_COMPLETED,
                "MSG_SERVER_STOPPED", instanceName);
    }
}
