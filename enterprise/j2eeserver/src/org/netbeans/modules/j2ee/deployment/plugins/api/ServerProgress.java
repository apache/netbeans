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

package org.netbeans.modules.j2ee.deployment.plugins.api;

import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;

/**
 * This is an utility class to avoid exposing deployment interface
 * {@link javax.enterprise.deploy.spi.status.ProgressObject} directly in 
 * server management SPI {@link org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer}.
 * <P>
 * Typical usage is for plugin StartServer implementation to create
 * instance of ServerProgress and return it to caller of 
 * startDeploymentManager, stopDeploymentManager and startDebugging.  
 * Plugin will update caller on progress of the operation through
 * method calls to set status.
 * <P>
 * @author  nn136682
 */

public class ServerProgress implements ProgressObject {
    private Object server;
    private List<ProgressListener> listeners = new CopyOnWriteArrayList<>();
    private DeploymentStatus status;
    
    /** Creates a new instance of StartServerProgress */
    public ServerProgress(Object server) {
        this.server = server;
        createRunningProgressEvent(CommandType.START, ""); //NOI18N
    }

    public static final Command START_SERVER = new Command(25, "START SERVER"); //NOI18N
    public static final Command STOP_SERVER = new Command(26, "STOP SERVER"); //NOI18N
   
    public static class Command extends CommandType {
        String commandString;
        public Command(int val, String commandString) {
            super(val);
            this.commandString = commandString;
        }
        public String toString() {
            return commandString;
        }
    }
    
    public void setStatusStartRunning(String message) {
        notify(createRunningProgressEvent(START_SERVER, message));
    }
    public void setStatusStartFailed(String message) {
        notify(createFailedProgressEvent(START_SERVER, message));
    }
    public void setStatusStartCompleted(String message) {
        notify(createCompletedProgressEvent(START_SERVER, message)); 
    }
    public void setStatusStopRunning(String message) {
        notify(createRunningProgressEvent(STOP_SERVER, message));
    }
    public void setStatusStopFailed(String message) {
        notify(createFailedProgressEvent(STOP_SERVER, message));
    }
    public void setStatusStopCompleted(String message) {
        notify(createCompletedProgressEvent(CommandType.STOP, message)); 
    }
    protected void notify(ProgressEvent pe) {
        for (ProgressListener listener : listeners) {
            listener.handleProgressEvent(pe);
        }
    } 

    protected DeploymentStatus createDeploymentStatus(final CommandType comtype, final String msg, final StateType state) {
        return new DeploymentStatus() {
            public ActionType getAction() { return ActionType.EXECUTE; }
            public CommandType getCommand() { return comtype; }
            public String getMessage() { return msg; }
            public StateType getState() { return state; }

            public boolean isCompleted () {
                return StateType.COMPLETED.equals(state);
            }

            public boolean isFailed () {
                return StateType.FAILED.equals(state);
            }

            public boolean isRunning () {
                return StateType.RUNNING.equals(state);
            }
        };
    }        
    protected ProgressEvent createCompletedProgressEvent(CommandType command, String message) {
        status = createDeploymentStatus(command, message, StateType.COMPLETED);
        return new ProgressEvent(server, null, status);
    }
    
    protected ProgressEvent createFailedProgressEvent(CommandType command, String message) {
        status = createDeploymentStatus(command, message, StateType.FAILED);
        return new ProgressEvent(server, null, status);
    }

    protected ProgressEvent createRunningProgressEvent(CommandType command, String message) {
        status = createDeploymentStatus(command, message, StateType.RUNNING);
        return new ProgressEvent(server, null, status);
    }    
//-------------- JSR88 ProgressObject -----------------
    public void addProgressListener(ProgressListener pol) {
        listeners.add(pol);
    }
    public void removeProgressListener(ProgressListener pol) {
        listeners.remove(pol);
    }
    
    public boolean isCancelSupported() { return true; }
    public void cancel() throws OperationUnsupportedException {
        //noop
    }
    public boolean isStopSupported() { return false; }
    public void stop() throws OperationUnsupportedException {
        //noop
    }
    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null;
    }
    public DeploymentStatus getDeploymentStatus() {
        return status;
    }
    public TargetModuleID[] getResultTargetModuleIDs() {
        return new TargetModuleID[0];
    }   
}

