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
package org.netbeans.modules.javaee.wildfly.ide;

import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.status.DeploymentStatus;

/**
 * An implementation of the DeploymentStatus interface used to track the
 * server start/stop progress.
 *
 * @author Kirill Sorokin
 */
public class WildflyDeploymentStatus implements DeploymentStatus {

    private final ActionType action;
    private final CommandType command;
    private final StateType state;
    private final String message;

    /** Creates a new instance of JBDeploymentStatus */
    public WildflyDeploymentStatus(ActionType action, CommandType command, StateType state, String message) {
        this.action = action;
        this.command = command;
        this.state = state;
        this.message = message;
    }

    public String getMessage() {
        return message;
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

    public boolean isRunning() {
        return StateType.RUNNING.equals(state);
    }

    public boolean isFailed() {
        return StateType.FAILED.equals(state);
    }

    public boolean isCompleted() {
        return StateType.COMPLETED.equals(state);
    }

}
