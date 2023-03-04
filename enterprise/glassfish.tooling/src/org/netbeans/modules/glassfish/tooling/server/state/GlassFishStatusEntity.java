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
package org.netbeans.modules.glassfish.tooling.server.state;

import org.netbeans.modules.glassfish.tooling.GlassFishStatus;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServerStatus;
import org.netbeans.modules.glassfish.tooling.logging.Logger;

/**
 * GlassFish server status entity.
 * <p/>
 * @author Tomas Kraus
 */
public class GlassFishStatusEntity implements GlassFishServerStatus {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER
            = new Logger(GlassFishStatusEntity.class);

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish server entity. */
    private GlassFishServer server;

    /** Current GlassFish server status. */
    private GlassFishStatus status;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server status entity.
     * <p/>
     * Initial server status value is set as unknown.
     * <p/>
     * @param server GlassFish server entity.
     */
    public GlassFishStatusEntity(final GlassFishServer server) {
        this.server = server;
        this.status = GlassFishStatus.UNKNOWN;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish server entity.
     * <p/>
     * @return GlassFish server entity.
     */
    @Override
    public GlassFishServer getServer() {
        return server;
    }

    /**
     * Set GlassFish server entity.
     * <p/>
     * @param server GlassFish server entity.
     */
    void setServer(final GlassFishServer server) {
        this.server = server;
    }

    /**
     * Get current GlassFish server status.
     * <p/>
     * @return Current GlassFish server status.
     */
    @Override
    public GlassFishStatus getStatus() {
        return status;
    }

    /**
     * Set current GlassFish server status.
     * <p/>
     * @param status Current GlassFish server status.
     */
    void setStatus(final GlassFishStatus status) {
        this.status = status;
    }

}
