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

package org.netbeans.modules.j2ee.weblogic9.optional;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentStatus;

/**
 *
 * @author Petr Hejl
 */
public final class WLServerProgress implements ProgressObject {

    /**
     * Progress events source
     */
    private final Object source;

    private final List<ProgressListener> listeners = new CopyOnWriteArrayList<ProgressListener>();

    /**
     * Current startus of the startup/shutdown process
     */
    private DeploymentStatus deploymentStatus;

    /**
     * Creates a new instance of WSServerProgress. The source supplied will
     * be used as the source for all the events. Ususally it is the parent
     * WSStartServerObject
     *
     * @param source source of events
     */
    public WLServerProgress(Object source) {
        this.source = source;
    }

    /**
     * Sends a startup event to the listeners.
     *
     * @param state the new state of the startup process
     * @param message the attached string message
     */
    public void notifyStart(StateType state, String message) {
        notify(new WLDeploymentStatus(ActionType.EXECUTE, CommandType.START, state, message));
    }

    /**
     * Sends a shutdown event to the listeners.
     *
     * @param state the new state of the shutdown process
     * @param message the attached string message
     */
    public void notifyStop(StateType state, String message) {
        notify(new WLDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, state, message));
    }

    /**
     * Notifies the listeners of the new process status.
     *
     * @param deploymentStatus the new status of the startup/shutdown
     *      process
     */
    public void notify(DeploymentStatus deploymentStatus) {
        // construct a new progress event from the source and the supplied
        // new process status
        ProgressEvent evt = new ProgressEvent(source, null, deploymentStatus);

        // update the saved process status
        this.deploymentStatus = deploymentStatus;

        for (ProgressListener listener : listeners) {
            listener.handleProgressEvent(evt);
        }
    }

    /**
     * A dummy implementation of the ProgressObject method, since this
     * method is not used anywhere, we omit the reasonable implementation.
     */
    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null;
    }

    /**
     * Removes the registered listener.
     *
     * @param progressListener the listener to be removed
     */
    public void removeProgressListener(ProgressListener progressListener) {
        listeners.remove(progressListener);
    }

    /**
     * Adds a new listener.
     *
     * @param progressListener the listener to be added
     */
    public void addProgressListener(ProgressListener progressListener) {
        listeners.add(progressListener);
    }

    /**
     * Returns the current state of the startup/shutdown process.
     *
     * @return current state of the process
     */
    public DeploymentStatus getDeploymentStatus() {
        return deploymentStatus;
    }

    /**
     * A dummy implementation of the ProgressObject method, since this
     * method is not used anywhere, we omit the reasonable implementation.
     */
    public TargetModuleID[] getResultTargetModuleIDs() {
        return new TargetModuleID[] {};
    }

    /**
     * A dummy implementation of the ProgressObject method, since this
     * method is not used anywhere, we omit the reasonable implementation.
     */
    public boolean isStopSupported() {
        return false;
    }

    /**
     * A dummy implementation of the ProgressObject method, since this
     * method is not used anywhere, we omit the reasonable implementation.
     */
    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Operation not supported"); // NOI18N
    }

    /**
     * A dummy implementation of the ProgressObject method, since this
     * method is not used anywhere, we omit the reasonable implementation.
     */
    public boolean isCancelSupported() {
        return false;
    }

    /**
     * A dummy implementation of the ProgressObject method, since this
     * method is not used anywhere, we omit the reasonable implementation.
     */
    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Operation not supported"); // NOI18N
    }
}
