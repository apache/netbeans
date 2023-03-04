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

package org.netbeans.modules.j2ee.weblogic9;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.api.annotations.common.CheckReturnValue;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;

/**
 *
 * @author Petr Hejl
 */
public final class ProgressObjectSupport {

    private ProgressObjectSupport() {
        super();
    }

    @CheckReturnValue
    public static boolean waitFor(ProgressObject obj) {
        final CountDownLatch latch = new CountDownLatch(1);
        obj.addProgressListener(new ProgressListener() {

            @Override
            public void handleProgressEvent(ProgressEvent pe) {
                if (pe.getDeploymentStatus().isCompleted() || pe.getDeploymentStatus().isFailed()) {
                    latch.countDown();
                }
            }
        });
        if (obj.getDeploymentStatus().isCompleted() || obj.getDeploymentStatus().isFailed()) {
            return true;
        }
        try {
            return latch.await(WLDeploymentManager.MANAGER_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return false;
    }
}
