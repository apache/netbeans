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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.swing.SwingUtilities;

/**
 *
 */
public class ProgressObjectImpl implements ProgressObject {

    private DeploymentStatusImpl status;
    
    private static final Logger LOG = Logger.getLogger(ProgressObjectImpl.class.getSimpleName());
    
    private final List<ProgressListener> listeners = new CopyOnWriteArrayList<ProgressListener>();
    
    private String url;
    
    public ProgressObjectImpl(String message, boolean completed) {
        setStatus(new DeploymentStatusImpl(
            CommandType.DISTRIBUTE, completed ? StateType.COMPLETED : StateType.RUNNING, ActionType.EXECUTE, message));
    }
    
    @Override
    public synchronized DeploymentStatus getDeploymentStatus() {
        return status;
    }

    private synchronized void setStatus(DeploymentStatusImpl status) {
        this.status = status;
        LOG.log(Level.INFO, "status: {0}", status);
    }
    
    @Override
    public TargetModuleID[] getResultTargetModuleIDs() {
        return new TargetModuleID[]{new TargetModuleIDImpl(url, "some-id", TargetImpl.SOME,
                // this is hack: when EAR is deployed Deployment API asks EAR's child module for URL:
                new TargetModuleIDImpl(url, "some-id-child", TargetImpl.SOME, null))};
    }

    @Override
    public ClientConfiguration getClientConfiguration(TargetModuleID tmid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCancelSupported() {
        return false;
    }

    @Override
    public void cancel() throws OperationUnsupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isStopSupported() {
        return false;
    }

    @Override
    public void stop() throws OperationUnsupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addProgressListener(ProgressListener pl) {
        listeners.add(pl);
    }

    @Override
    public void removeProgressListener(ProgressListener pl) {
        listeners.remove(pl);
    }

    public void updateDepoymentStage(String message) {
        DeploymentStatusImpl st = new DeploymentStatusImpl(
            CommandType.DISTRIBUTE, StateType.RUNNING, ActionType.EXECUTE, message);
        setStatus(st);
        fireChange(st);
    }
    
    public void updateDepoymentResult(org.netbeans.modules.cloud.common.spi.support.serverplugin.DeploymentStatus result, String url) {
        StateType st = StateType.FAILED;
        if (result == org.netbeans.modules.cloud.common.spi.support.serverplugin.DeploymentStatus.SUCCESS) {
            st = StateType.COMPLETED;
            this.url = url;
        } else if (result == org.netbeans.modules.cloud.common.spi.support.serverplugin.DeploymentStatus.FAILED ||
                result == org.netbeans.modules.cloud.common.spi.support.serverplugin.DeploymentStatus.EXCEPTION ||
                result == org.netbeans.modules.cloud.common.spi.support.serverplugin.DeploymentStatus.UNKNOWN) {
            st = StateType.FAILED;
        }
        DeploymentStatusImpl st2 = new DeploymentStatusImpl(
            CommandType.DISTRIBUTE, st, ActionType.EXECUTE, "Deployment finished.");
        setStatus(st2);
        fireChange(st2);
    }
    
    private void fireChange(final DeploymentStatusImpl st) {
        final List<ProgressListener> ls = new ArrayList<ProgressListener>(listeners);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (ProgressListener listener : ls) {
                    listener.handleProgressEvent(new ProgressEvent(this, null, st));
                }
            }
        });
    }
    
    private static class TargetModuleIDImpl implements TargetModuleID {

        private String url;
        private String id;
        private Target target;
        private TargetModuleID child;

        public TargetModuleIDImpl(String url, String id, Target target, TargetModuleID child) {
            this.url = url;
            this.id = id;
            this.target = target;
            this.child = child;
        }
        
        @Override
        public Target getTarget() {
            return target;
        }

        @Override
        public String getModuleID() {
            return id;
        }

        @Override
        public String getWebURL() {
            return url;
        }

        @Override
        public TargetModuleID getParentTargetModuleID() {
            return null;
        }

        @Override
        public TargetModuleID[] getChildTargetModuleID() {
            if (child == null) {
                return null;
            } else {
                return new TargetModuleID[]{child};
            }
        }
        
    }
    
}
