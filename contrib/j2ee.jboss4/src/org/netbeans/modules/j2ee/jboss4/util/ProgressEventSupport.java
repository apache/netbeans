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

package org.netbeans.modules.j2ee.jboss4.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;

/**
 * Progress event support
 *
 * @author sherold
 */
public final class ProgressEventSupport {

    private final Object eventSource;
    private final List<ProgressListener> listeners = new CopyOnWriteArrayList<ProgressListener>();
    private DeploymentStatus status;


    public ProgressEventSupport(Object eventSource) {
        if (eventSource == null) {
            throw new NullPointerException();
        }
        this.eventSource = eventSource;
    }

    public void addProgressListener(ProgressListener progressListener) {
        listeners.add(progressListener);
    }

    public void removeProgressListener(ProgressListener progressListener) {
        listeners.remove(progressListener);
    }

    public void fireProgressEvent(TargetModuleID targetModuleID, DeploymentStatus status) {
        synchronized (this) {
            this.status = status;
        }
        ProgressEvent evt = new ProgressEvent(eventSource, targetModuleID, status);
        for (ProgressListener listener : listeners) {
            listener.handleProgressEvent(evt);
        }
    }

    public synchronized DeploymentStatus getDeploymentStatus() {
        return status;
    }
}
