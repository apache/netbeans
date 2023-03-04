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
package org.netbeans.modules.cloud.common.spi.support.serverplugin;

import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.status.DeploymentStatus;

/**
 *
 */
public class DeploymentStatusImpl implements DeploymentStatus {

    private CommandType command;
    private StateType state;
    private ActionType action;
    private String message;

    public DeploymentStatusImpl(CommandType command, StateType state, ActionType action, String message) {
        this.command = command;
        this.state = state;
        this.action = action;
        this.message = message;
    }
    
    @Override
    public StateType getState() {
        return state;
    }

    @Override
    public CommandType getCommand() {
        return command;
    }

    @Override
    public ActionType getAction() {
        return action;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean isCompleted() {
        return StateType.COMPLETED.equals(state);
    }

    @Override
    public boolean isFailed() {
        return StateType.FAILED.equals(state);
    }

    @Override
    public boolean isRunning() {
        return StateType.RUNNING.equals(state);
    }

    @Override
    public String toString() {
        return "DeploymentStatusImpl{" + "command=" + command + ", state=" + state + ", action=" + action + ", message=" + message + '}';
    }
    
}
