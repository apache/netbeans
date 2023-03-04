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
 * GlassFish Cloud Entity Interface.
 * <p/>
 * GlassFish Cloud entity interface allows to use foreign entity classes.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public interface GlassFishCloud {

    ////////////////////////////////////////////////////////////////////////////
    // Interface Methods                                                      //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish cloud name.
     * <p/>
     * This is display name given to the cloud.
     * <p/>
     * @return GlassFish cloud name.
     */
    public String getName();

    /**
     * Get GlassFish cloud (CPAS) host.
     * <p/>
     * @return GlassFish cloud (CPAS) host.
     */
    public String getHost();

    /**
     * Get GlassFish cloud port.
     * <p/>
     * @return GlassFish cloud port.
     */
    public int getPort();

    /**
     * Get GlassFish cloud local server.
     * <p/>
     * @return GlassFish cloud local server.
     */
    public GlassFishServer getLocalServer();

}
