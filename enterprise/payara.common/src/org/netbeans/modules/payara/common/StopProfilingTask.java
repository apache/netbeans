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
import org.openide.util.NbBundle;
import org.netbeans.modules.payara.spi.PayaraModule;

/**
 * Task for stopping server that was started in profile mode.
 * 
 * @author Peter Benedikovic
 */
public class StopProfilingTask extends BasicTask<TaskState> {

    private final CommonServerSupport support;

    /**
     * 
     * @param support common support object for the server instance being stopped
     * @param stateListener state monitor to track start progress
     */
    public StopProfilingTask(final CommonServerSupport support, TaskStateListener stateListener) {
        super(support.getInstance(), stateListener, new TaskStateListener() {

            @Override
           public void operationStateChanged(TaskState newState,
           TaskEvent event, String... args) {
                if(newState == TaskState.COMPLETED) {
                    support.setServerState(PayaraModule.ServerState.STOPPED);
                } else if(newState == TaskState.FAILED) {
                    support.setServerState(PayaraModule.ServerState.STOPPED_JVM_PROFILER);
                }
            }
        });
        this.support = support;
    }
    
    @Override
    public TaskState call() {
        Logger.getLogger("payara").log(Level.FINEST, "StopLocalTask.call() called on thread {0}", Thread.currentThread().getName()); // NOI18N
        
        if (support.getLocalStartProcess() != null) {
            LogViewMgr logger = LogViewMgr.getInstance(instance.getProperty(PayaraModule.URL_ATTR));
            String msg = NbBundle.getMessage(BasicTask.class, "MSG_SERVER_PROFILING_STOPPED", instanceName);
            logger.write(msg, false);
            logger.stopReaders();
            support.stopLocalStartProcess();
            return fireOperationStateChanged(TaskState.COMPLETED,
                    TaskEvent.CMD_COMPLETED,
                    "MSG_SERVER_PROFILING_STOPPED", instanceName);
        } else {
            return fireOperationStateChanged(TaskState.FAILED,
                    TaskEvent.CMD_FAILED,
                    "MSG_STOP_SERVER_FAILED", instanceName);
        }
    }
    
}
