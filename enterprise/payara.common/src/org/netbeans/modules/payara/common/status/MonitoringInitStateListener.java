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
 * Notification about server state check results.
 * <p/>
 * Handles initial period of time after adding new server into status
 * monitoring.
 * <p/>
 * Should receive all state change events except <code>UNKNOWN</code>.
 * <p/>
 * @author Tomas Kraus
 */
public class MonitoringInitStateListener extends WakeUpStateListener {

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of state check results notification.
     */
    public MonitoringInitStateListener() {
        super();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Callback to notify about current server status after every check
     * when enabled.
     * <p/>
     * Wait for more checking cycles to make sure server status monitoring
     * has settled down.
     * <p/>
     * @param server Payara server instance being monitored.
     * @param status Current server status.
     * @param task   Last Payara server status check task details.
     */
    @Override
    public void currentState(final PayaraServer server,
            final PayaraStatus status, final PayaraStatusTask task) {
        // Not used yet.
    }
}
