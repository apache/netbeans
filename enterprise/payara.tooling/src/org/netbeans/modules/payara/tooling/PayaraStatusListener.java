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
package org.netbeans.modules.payara.tooling;

import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.netbeans.modules.payara.tooling.data.PayaraStatusTask;

/**
 * Payara server status listener.
 * <p/>
 * Receives notifications about every Payara server status check result
 * or about Payara server status changes.
 * <p/>
 * @author Tomas Kraus
 */
public interface PayaraStatusListener {

    /**
     * Callback to notify about current server status after every check
     * when enabled.
     * <p/>
     * @param server Payara server instance being monitored.
     * @param status Current server status.
     * @param task   Last Payara server status check task details.
     */
    public void currentState(final PayaraServer server,
            final PayaraStatus status, final PayaraStatusTask task);

    /**
     * Callback to notify about server status change when enabled.
     * <p/>
     * @param server Payara server instance being monitored.
     * @param status Current server status.
     * @param task   Last Payara server status check task details.
     */    
    public void newState(final PayaraServer server,
            final PayaraStatus status, final PayaraStatusTask task);

    /**
     * Callback to notify about server status check failures.
     * <p/>
     * @param server Payara server instance being monitored.
     * @param task   Payara server status check task details.
     */
    public void error(final PayaraServer server,
            final PayaraStatusTask task);

    /**
     * Callback to notify about status listener being registered.
     * <p/>
     * May be called multiple times for individual event sets during
     * registration phase.
     */
    public void added();

    /**
     * Callback to notify about status listener being unregistered.
     * <p/>
     * Will be called once during listener removal phase when was found
     * registered for at least one event set.
     */
    public void removed();

}
