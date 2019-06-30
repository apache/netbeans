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
package org.netbeans.modules.payara.tooling.data.cloud;

import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Payara Cloud Entity.
 * <p/>
 * Payara cloud entity instance which is used when not defined externally
 * in IDE.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class PayaraCloudEntity implements PayaraCloud {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara cloud name (display name in IDE). */
    protected String name;

    /** Payara cloud host. */
    protected String host;

    /** Payara cloud port. */
    protected int port;

    /** Payara cloud local server. */
    protected PayaraServer localServer;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs empty class instance. No default values are set.
     */
    public PayaraCloudEntity() {
    }

    /**
     * Constructs class instance with ALL values set.
     * <p/>
     * @param name        Payara cloud name to set.
     * @param host        Payara cloud host to set.
     * @param port        Payara server port to set.
     * @param localServer Payara cloud local server to set.
     */
    public PayaraCloudEntity(String name, String host, int port,
            PayaraServer localServer) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.localServer = localServer;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get Payara cloud name (display name in IDE).
     * <p/>
     * @return Payara cloud name (display name in IDE).
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Set Payara cloud name (display name in IDE).
     * <p/>
     * @param name Payara cloud name to set (display name in IDE).
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get Payara cloud host.
     * <p/>
     * @return Payara cloud host.
     */
    @Override
    public String getHost() {
        return host;
    }

    /**
     * Set Payara cloud host.
     * <p/>
     * @param host Payara cloud host to set.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Get Payara server port.
     * <p/>
     * @return Payara server port.
     */
    @Override
    public int getPort() {
        return port;
    }

    /**
     * Set Payara server port.
     * <p/>
     * @param port Payara server port to set.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Get Payara cloud local server.
     * <p/>
     * @return Payara cloud local server.
     */
    @Override
    public PayaraServer getLocalServer() {
        return localServer;
    }

    /**
     * Set Payara cloud local server.
     * <p/>
     * @param localServer Payara cloud local server to set.
     */
    public void setLocalServer(PayaraServer localServer) {
        this.localServer = localServer;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * String representation of this Payara cloud entity.
     * <p/>
     * @return String representation of this Payara cloud entity.
     */
    @Override
    public String toString() {
        return name;
    }

}
