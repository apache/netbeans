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
package org.netbeans.modules.glassfish.tooling.data.cloud;

import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;

/**
 * GlassFish Cloud Entity.
 * <p/>
 * GlassFish cloud entity instance which is used when not defined externally
 * in IDE.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class GlassFishCloudEntity implements GlassFishCloud {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish cloud name (display name in IDE). */
    protected String name;

    /** GlassFish cloud host. */
    protected String host;

    /** GlassFish cloud port. */
    protected int port;

    /** GlassFish cloud local server. */
    protected GlassFishServer localServer;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs empty class instance. No default values are set.
     */
    public GlassFishCloudEntity() {
    }

    /**
     * Constructs class instance with ALL values set.
     * <p/>
     * @param name        GlassFish cloud name to set.
     * @param host        GlassFish cloud host to set.
     * @param port        GlassFish server port to set.
     * @param localServer GlassFish cloud local server to set.
     */
    public GlassFishCloudEntity(String name, String host, int port,
            GlassFishServer localServer) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.localServer = localServer;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish cloud name (display name in IDE).
     * <p/>
     * @return GlassFish cloud name (display name in IDE).
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Set GlassFish cloud name (display name in IDE).
     * <p/>
     * @param name GlassFish cloud name to set (display name in IDE).
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get GlassFish cloud host.
     * <p/>
     * @return GlassFish cloud host.
     */
    @Override
    public String getHost() {
        return host;
    }

    /**
     * Set GlassFish cloud host.
     * <p/>
     * @param host GlassFish cloud host to set.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Get GlassFish server port.
     * <p/>
     * @return GlassFish server port.
     */
    @Override
    public int getPort() {
        return port;
    }

    /**
     * Set GlassFish server port.
     * <p/>
     * @param port GlassFish server port to set.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Get GlassFish cloud local server.
     * <p/>
     * @return GlassFish cloud local server.
     */
    @Override
    public GlassFishServer getLocalServer() {
        return localServer;
    }

    /**
     * Set GlassFish cloud local server.
     * <p/>
     * @param localServer GlassFish cloud local server to set.
     */
    public void setLocalServer(GlassFishServer localServer) {
        this.localServer = localServer;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * String representation of this GlassFish cloud entity.
     * <p/>
     * @return String representation of this GlassFish cloud entity.
     */
    @Override
    public String toString() {
        return name;
    }

}
