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


package org.netbeans.modules.tomcat5.progress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.openide.util.Parameters;

/**
 * MultiProgressObjectWrapper wraps multiple progress objects into a single one.
 * <p>
 * If all wrapped objects are in COMPLETED or FAILED state the state object
 * will be changed to:
 * <ul>
 *     <li>{@link StateType.COMPLETED} if all wrapped objects reached the COMPLETED state
 *     <li>{@link StateType.FAILED} if any of wrapped objects reached the FAILED state
 * </ul>
 * <p>
 * Note that all wrapped objects have to be in COMPLETED or FAILED state to
 * invoke the change of the state of this object. However this does not mean
 * the events from the wrapped objects are not propagated to the listeners of
 * this object.
 * <p>
 * The behaviour of {@link StateType.RELEASED} is quite unsure from JSR-88.
 * This implementation does not consider it as end state of the ProgressObject.
 *
 * @author herolds
 * @author Petr Hejl
 */
public class MultiProgressObjectWrapper implements ProgressObject, ProgressListener {

    private final ProgressEventSupport pes = new ProgressEventSupport(this);

    private final ProgressObject[] progressObjects;

    private String message = ""; // NOI18N

    private StateType state = StateType.RUNNING;

    /** Creates a new instance of MultipleOpsProgressObject */
    public MultiProgressObjectWrapper(ProgressObject[] objects) {
        Parameters.notNull("progObjs", state);

        if (objects.length == 0) {
            throw new IllegalArgumentException("At least one progress object must be passed."); // NOI18N
        }

        progressObjects = new ProgressObject[objects.length];
        System.arraycopy(objects, 0, progressObjects, 0, objects.length);

        for (int i = 0; i < objects.length; i++) {
            ProgressObject po = objects[i];
            // XXX unsafe publication
            po.addProgressListener(this);
        }

        updateState(null);
    }

    /** JSR88 method. */
    @Override
    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null; // PENDING
    }

    /** JSR88 method. */
    @Override
    public synchronized DeploymentStatus getDeploymentStatus() {
        DeploymentStatus ds = progressObjects[0].getDeploymentStatus();
        // all deployment objects are supposed to be of the same action and command type
        return new Status(ds.getAction(), ds.getCommand(), message, state);
    }

    /** JSR88 method. */
    @Override
    public TargetModuleID[] getResultTargetModuleIDs() {
        List<TargetModuleID> returnVal = new ArrayList<>();
        for (int i = 0; i < progressObjects.length; i++) {
            ProgressObject po = progressObjects[i];
            if (po.getDeploymentStatus().isCompleted()) {
                returnVal.addAll(Arrays.asList(po.getResultTargetModuleIDs()));
            }
        }
        return returnVal.toArray(new TargetModuleID[0]);
    }

    /** JSR88 method. */
    @Override
    public boolean isCancelSupported() {
        return false;
    }

    /** JSR88 method. */
    @Override
    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("cancel not supported in Tomcat deployment"); // NOI18N
    }

    /** JSR88 method. */
    @Override
    public boolean isStopSupported() {
        return false;
    }

    /** JSR88 method. */
    @Override
    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("stop not supported in Tomcat deployment"); // NOI18N
    }

    /** JSR88 method. */
    @Override
    public void addProgressListener(ProgressListener l) {
        pes.addProgressListener(l);
    }

    /** JSR88 method. */
    @Override
    public void removeProgressListener(ProgressListener l) {
        pes.removeProgressListener(l);
    }

    /**
     * Handles the progress events from wrapped objects.
     */
    @Override
    public void handleProgressEvent(ProgressEvent progressEvent) {
        updateState(progressEvent.getDeploymentStatus().getMessage());

        pes.fireHandleProgressEvent(progressEvent.getTargetModuleID(), progressEvent.getDeploymentStatus());
    }

    private synchronized void updateState(String receivedMessage) {
        if (state == StateType.COMPLETED || state == StateType.FAILED) {
            return;
        }

        boolean completed = true;
        boolean failed = false;

        for (ProgressObject progress : progressObjects) {
            DeploymentStatus status = progress.getDeploymentStatus();

            if (status == null || (!status.isCompleted() && !status.isFailed())) {
                completed = false;
                break;
            }

            if (status.isFailed()) {
                failed = true;
            }
        }

        if (completed) {
            state = failed ? StateType.FAILED : StateType.COMPLETED;
            message = receivedMessage == null ? "" : receivedMessage;
        }
    }
}
