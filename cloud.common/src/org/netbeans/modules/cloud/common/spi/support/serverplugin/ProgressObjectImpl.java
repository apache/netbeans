/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
