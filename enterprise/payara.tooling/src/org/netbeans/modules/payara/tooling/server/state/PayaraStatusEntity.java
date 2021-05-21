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
package org.netbeans.modules.payara.tooling.server.state;

import org.netbeans.modules.payara.tooling.PayaraStatus;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.netbeans.modules.payara.tooling.data.PayaraServerStatus;

/**
 * Payara server status entity.
 * <p/>
 * @author Tomas Kraus
 */
public class PayaraStatusEntity implements PayaraServerStatus {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER
            = new Logger(PayaraStatusEntity.class);

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara server entity. */
    private PayaraServer server;

    /** Current Payara server status. */
    private PayaraStatus status;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara server status entity.
     * <p/>
     * Initial server status value is set as unknown.
     * <p/>
     * @param server Payara server entity.
     */
    public PayaraStatusEntity(final PayaraServer server) {
        this.server = server;
        this.status = PayaraStatus.UNKNOWN;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get Payara server entity.
     * <p/>
     * @return Payara server entity.
     */
    @Override
    public PayaraServer getServer() {
        return server;
    }

    /**
     * Set Payara server entity.
     * <p/>
     * @param server Payara server entity.
     */
    void setServer(final PayaraServer server) {
        this.server = server;
    }

    /**
     * Get current Payara server status.
     * <p/>
     * @return Current Payara server status.
     */
    @Override
    public PayaraStatus getStatus() {
        return status;
    }

    /**
     * Set current Payara server status.
     * <p/>
     * @param status Current Payara server status.
     */
    void setStatus(final PayaraStatus status) {
        this.status = status;
    }

}
