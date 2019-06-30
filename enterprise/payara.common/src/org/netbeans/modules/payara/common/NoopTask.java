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

import org.netbeans.modules.payara.tooling.TaskEvent;
import org.netbeans.modules.payara.tooling.TaskState;
import org.netbeans.modules.payara.tooling.TaskStateListener;

/**
 * Empty command execution.
 * <p/>
 * Running this task does nothing.
 * <p/>
 * @author vkraemer
 */
class NoopTask extends BasicTask<TaskState> {

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of empty command execution class.
     * <p/>
     * @param serverSupport Payara server 
     * @param stopServerListener
     * @param stateListener 
     */
    public NoopTask(CommonServerSupport serverSupport,
            TaskStateListener stopServerListener,
            TaskStateListener stateListener) {
        super(serverSupport.getInstance(), stopServerListener, stateListener);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Callable call() Method                                                 //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Executes nothing on remote Payara instance.
     * <p/>
     * @return Always returns <code>OperationState.COMPLETED</code> value.
     */
    @Override
    public TaskState call() {
        return fireOperationStateChanged(TaskState.COMPLETED,
                TaskEvent.CMD_COMPLETED, "MSG_NOOP");
    }

}
