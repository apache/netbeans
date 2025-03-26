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

package org.netbeans.tests.j2eeserver.plugin.jsr88;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerProgress;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;

/**
 *
 * @author  gfink
 * @author nn136682
 */
public class TestProgressObject extends ServerProgress {

    private TargetModuleID[] tmIDs;

    public TestProgressObject(DeploymentManager dm, Target[] targets, File archive, Object plan, ModuleType type) {
        this(dm, createTargetModuleIDs(targets, archive, type));
    }

    public TestProgressObject(DeploymentManager dm, Target[] targets, File archive, Object plan) {
        this(dm, createTargetModuleIDs(targets, archive, null));
    }

    public TestProgressObject(DeploymentManager dm, Target[] targets, Object archive, Object plan) {
        this(dm, new TargetModuleID[0]);
    }

    public TestProgressObject(DeploymentManager dm, TargetModuleID[] modules) {
        super(dm);
        tmIDs = modules;
    }

    public static TargetModuleID[] createTargetModuleIDs(Target[] targets, File archive, ModuleType type) {
        TargetModuleID [] ret = new TargetModuleID[targets.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new TestTargetModuleID(targets[i], archive.getName(), type != null ? type : getType(archive.getName()));
            ((TestTarget)targets[i]).add(ret[i]);
        }
        return ret;
    }
    static ModuleType getType(String name) {
        if (name.endsWith(".ear")) {
            return ModuleType.EAR;
        } else if (name.endsWith(".jar") || name.equals("jar")) {
            return ModuleType.EJB;
        } else if (name.endsWith(".war") || name.equals("web")) {
            return ModuleType.WAR;
        } else if (name.endsWith(".rar")) {
            return ModuleType.RAR;
        } else {
            throw new IllegalArgumentException("Invalid archive name: " + name);
        }
    }

    public void setStatusDistributeRunning(String message) {
        notify(createRunningProgressEvent(CommandType.DISTRIBUTE, message));
    }

    public void setStatusDistributeFailed(String message) {
        notify(createFailedProgressEvent(CommandType.DISTRIBUTE, message));
    }

    public void setStatusDistributeCompleted(String message) {
        notify(createCompletedProgressEvent(CommandType.DISTRIBUTE, message));
    }

    public void setStatusRedeployRunning(String message) {
        notify(createRunningProgressEvent(CommandType.REDEPLOY, message));
    }

    public void setStatusRedeployFailed(String message) {
        notify(createFailedProgressEvent(CommandType.REDEPLOY, message));
    }

    public void setStatusRedeployCompleted(String message) {
        notify(createCompletedProgressEvent(CommandType.REDEPLOY, message));
    }

    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Test plugin does not support cancel!");
    }

    @Override
    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Test plugin does not support stop!");
    }

    @Override
    public boolean isCancelSupported() {
        return false;  // PENDING parameterize?
    }

    @Override
    public boolean isStopSupported() {
        return false; // PENDING see above
    }

    @Override
    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null; // PENDING client support
    }

    @Override
    public DeploymentStatus getDeploymentStatus() {
        return super.getDeploymentStatus();
    }

    @Override
    public TargetModuleID[] getResultTargetModuleIDs() {
        List ret = new Vector();
        for (int i = 0; i < tmIDs.length; i++) {
            if (tmIDs[i].getChildTargetModuleID() != null)
                ret.addAll(Arrays.asList(tmIDs[i].getChildTargetModuleID()));
            ret.add(tmIDs[i]);
        }
        return (TargetModuleID[]) ret.toArray(new TargetModuleID[0]);
    }
}
