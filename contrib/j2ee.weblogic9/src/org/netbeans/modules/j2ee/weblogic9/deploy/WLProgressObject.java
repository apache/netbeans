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

package org.netbeans.modules.j2ee.weblogic9.deploy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;

/**
 *
 * @author Petr Hejl
 */
public class WLProgressObject implements ProgressObject {

    // TODO in future we could make it cancellable

    private final List<ProgressListener> listeners = new CopyOnWriteArrayList<ProgressListener>();

    private final TargetModuleID[] moduleIds;

    /* GuardedBy("this") */
    private DeploymentStatus deploymentStatus;

    public WLProgressObject(TargetModuleID... moduleIds) {
        this.moduleIds = moduleIds;
    }

    public void addProgressListener(ProgressListener pl) {
        listeners.add(pl);
    }

    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Operation not supported");
    }

    public ClientConfiguration getClientConfiguration(TargetModuleID tmid) {
        return null;
    }

    public synchronized DeploymentStatus getDeploymentStatus() {
        return deploymentStatus;
    }

    public TargetModuleID[] getResultTargetModuleIDs() {
        return moduleIds.clone();
    }

    public boolean isCancelSupported() {
        return false;
    }

    public boolean isStopSupported() {
        return false;
    }

    public void removeProgressListener(ProgressListener pl) {
        listeners.remove(pl);
    }

    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Operation not supported");
    }

    public void fireProgressEvent(TargetModuleID targetModuleID, DeploymentStatus deploymentStatus) {
        ProgressEvent evt = new ProgressEvent(this, targetModuleID, deploymentStatus);

        synchronized (this) {
            this.deploymentStatus = deploymentStatus;
        }

        for (ProgressListener target : listeners) {
            target.handleProgressEvent(evt);
        }
    }
}
