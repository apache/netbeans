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

package org.netbeans.modules.payara.jakartaee.ide;

import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.status.DeploymentStatus;

/** 
 * This should have been included in JSR-88 as a helper class.
 *
 * @author Peter Williams
 */
public class Hk2DeploymentStatus implements DeploymentStatus {

    private final CommandType command;
    private final StateType state;
    private final ActionType action;
    private final String message;
    
    /**
     * Create DeploymentStatus object
     * 
     * @param state State of command being performed (running, completed, failed)
     * @param command Command being performed (start, stop, deploy, etc.)
     * @param action Action represented by this status event (command is executing,
     *   cancelled, or stopped.)
     * @param message Informational message for the user describing this status object.
     */
    public Hk2DeploymentStatus(final CommandType command, final StateType state, 
            final ActionType action, final String message) {
        this.command = command;
        this.state = state;
        this.action = action;
        this.message = message;
    }
    
    public CommandType getCommand() {
        return command;
    }

    public StateType getState() {
        return state;
    }

    public ActionType getAction() {
        return action;
    }

    public String getMessage() {
        return message;
    }

    public boolean isCompleted() {
        return StateType.COMPLETED.equals(state);
    }

    public boolean isFailed() {
        return StateType.FAILED.equals(state);
    }

    public boolean isRunning() {
        return StateType.RUNNING.equals(state);
    }

    @Override
    public String toString() {
        return message;
    }

}
