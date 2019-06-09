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
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;

/**
 * ProgressObject implementation that is in a permanent completed state.
 * For returning from methods that must return a ProgressObject, but do not
 * need to implement any asynchronous functionality.
 *
 * @author Peter Williams
 */
public class DummyProgressObject implements ProgressObject {

    private final TargetModuleID [] moduleIDs;
    private final DeploymentStatus status = new Hk2DeploymentStatus(
            CommandType.DISTRIBUTE, StateType.COMPLETED, ActionType.EXECUTE, "");

    public DummyProgressObject(final TargetModuleID moduleID) {
        moduleIDs = new TargetModuleID [] { moduleID };
    }

    public DeploymentStatus getDeploymentStatus() {
        return status;
    }

    public TargetModuleID [] getResultTargetModuleIDs() {
        return moduleIDs.clone();
    }

    public ClientConfiguration getClientConfiguration(TargetModuleID tmid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isCancelSupported() {
        return true;
    }

    public void cancel() throws OperationUnsupportedException {
    }

    public boolean isStopSupported() {
        return true;
    }

    public void stop() throws OperationUnsupportedException {
    }

    public void addProgressListener(ProgressListener listener) {
    }

    public void removeProgressListener(ProgressListener listener) {
    }

}
