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
package org.netbeans.modules.payara.common.status;

import org.netbeans.modules.payara.tooling.PayaraStatus;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.netbeans.modules.payara.tooling.data.PayaraStatusTask;

/**
 * Notification about server state check results that will wake up waiting
 * thread to pass notification.
 * <p/>
 * @author Tomas Kraus
 */
public abstract class WakeUpStateListener extends BasicStateListener {
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Requested wake up of checking thread. */
    private volatile boolean wakeUp;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of state check results notification.
     */
    public WakeUpStateListener() {
        super();
        wakeUp = false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Wake up checking thread.
     */
    protected void wakeUp() {
        if (!wakeUp) synchronized(this) {
            wakeUp = true;
            this.notify();
        }
    }

    /**
     * Get status of wake up request of checking thread.
     * <p/>
     * @return Status of wake up request of checking thread.
     */
    public boolean isWakeUp() {
        return wakeUp;
    }

    /**
     * Wake up waiting thread immediately on registered state changes.
     * <p/>
     * @param server Payara server instance being monitored.
     * @param status Current server status.
     * @param task   Last Payara server status check task details.
     */    
    @Override
    public void newState(final PayaraServer server,
            final PayaraStatus status, final PayaraStatusTask task) {
        wakeUp();
    }

    /**
     * Do nothing.
     * <p/>
     * Error callback is expected to be unused.
     * <p/>
     * @param server Payara server instance being monitored.
     * @param task   Payara server status check task details.
     */
    @Override
    public void error(final PayaraServer server,
            final PayaraStatusTask task) {
        // Not used yet.
    }

}
