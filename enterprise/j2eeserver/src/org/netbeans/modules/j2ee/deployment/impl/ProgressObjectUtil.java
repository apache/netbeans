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

package org.netbeans.modules.j2ee.deployment.impl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;

/**
 * A utility class for asynchronous monitoring of progress objects.
 * 
 * @author sherold
 */
public class ProgressObjectUtil {
    
    private static final Logger LOGGER = Logger.getLogger(ProgressObjectUtil.class.getName());
    
    private ProgressObjectUtil() {
    }
    
    /**
     * Waits till the progress object is in final state or till the timeout runs out.
     *
     * @param ui progress ui which will be notified about progress object changes .
     * @param po progress object which will be tracked.
     * @param timeout timeout in millis. Zero timeout means unlimited timeout.
     *
     * @return true if the progress object completed successfully, false otherwise.
     *         This is a workaround for issue 82428.
     * 
     * @throws TimedOutException when the task times out.
     */
    public static boolean trackProgressObject(ProgressUI ui, final ProgressObject po, long timeout) throws TimeoutException {
        assert po != null;
        assert ui != null;
        // There may be a problem if server reports RELEASED as final state
        // however there is no way how to handle it here, because some POs has
        // RELEASED as initial state
        final AtomicBoolean completed = new AtomicBoolean();
        ui.setProgressObject(po);
        try {
            final CountDownLatch progressFinished = new CountDownLatch(1);
            ProgressListener listener = new ProgressListener() {
                @Override
                public void handleProgressEvent(ProgressEvent progressEvent) {
                    DeploymentStatus status = progressEvent.getDeploymentStatus();
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, "Received progress state {0} from {1}",
                                new Object[] {status.getState(), progressEvent});
                    }
                    if (status.isCompleted()) {
                        completed.set(true);
                    }
                    if (status.isCompleted() || status.isFailed()) {
                        progressFinished.countDown();
                    }
                }
            };
            LOGGER.log(Level.FINEST, "Adding progress listener {0}", listener);
            po.addProgressListener(listener);
            try {
                // the completion event might have arrived before the progress listener 
                // was registered, wait only if not yet finished
                DeploymentStatus status = po.getDeploymentStatus();
                if (!status.isCompleted() && !status.isFailed()) {
                    try {
                        if (timeout == 0) {
                            progressFinished.await();
                        } else {
                            boolean passed = progressFinished.await(timeout, TimeUnit.MILLISECONDS);
                            if (!passed) {
                                throw new TimeoutException();
                            }
                        }
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.INFO, null, e);
                        Thread.currentThread().interrupt();
                        return false;
                    }
                } else if (status.isCompleted()) {
                    completed.set(true);
                }
            } finally {
                po.removeProgressListener(listener);
            }
        } finally {
            ui.setProgressObject(null);
        }
        return completed.get();
    }
    
}
