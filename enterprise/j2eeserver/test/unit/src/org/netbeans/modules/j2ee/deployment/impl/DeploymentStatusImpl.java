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

package org.netbeans.modules.j2ee.deployment.impl;

import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.status.DeploymentStatus;


/**
 *
 * @author sherold
 */
public class DeploymentStatusImpl implements DeploymentStatus {
    
    private final ActionType action;
    private final CommandType command;
    private final String message;
    private final StateType state;

    /**
     * Creates a new instance of Status
     */
    public DeploymentStatusImpl(CommandType command, String message, StateType state) {
        this.action = ActionType.EXECUTE;
        this.command = command;
        this.message = message;
        this.state = state;
    }

    public StateType getState() {
        return state;
    }

    public CommandType getCommand() {
        return command;
    }

    public ActionType getAction() {
        return action;
    }

    public String getMessage() {
        return message;
    }

    public boolean isCompleted () {
        return StateType.COMPLETED.equals(state);
    }
    
    public boolean isFailed () {
        return StateType.FAILED.equals(state);
    }
    
    public boolean isRunning () {
        return StateType.RUNNING.equals(state);
    }
    
    public String toString () {
        return "action=" + action + " command=" + command + " state=" + state + "message=" + message;   // NOI18N
    }
    
}
