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
package org.netbeans.modules.glassfish.tooling;

import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.data.GlassFishStatusTask;

/**
 * GlassFish server status listener.
 * <p/>
 * Receives notifications about every GlassFish server status check result
 * or about GlassFish server status changes.
 * <p/>
 * @author Tomas Kraus
 */
public interface GlassFishStatusListener {

    /**
     * Callback to notify about current server status after every check
     * when enabled.
     * <p/>
     * @param server GlassFish server instance being monitored.
     * @param status Current server status.
     * @param task   Last GlassFish server status check task details.
     */
    public void currentState(final GlassFishServer server,
            final GlassFishStatus status, final GlassFishStatusTask task);

    /**
     * Callback to notify about server status change when enabled.
     * <p/>
     * @param server GlassFish server instance being monitored.
     * @param status Current server status.
     * @param task   Last GlassFish server status check task details.
     */    
    public void newState(final GlassFishServer server,
            final GlassFishStatus status, final GlassFishStatusTask task);

    /**
     * Callback to notify about server status check failures.
     * <p/>
     * @param server GlassFish server instance being monitored.
     * @param task   GlassFish server status check task details.
     */
    public void error(final GlassFishServer server,
            final GlassFishStatusTask task);

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
