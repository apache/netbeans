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
package org.netbeans.modules.payara.tooling.server.config;

import org.netbeans.modules.payara.tooling.data.ToolsConfig;

/**
 * Payara tools.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class Tools implements ToolsConfig {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Asadmin tool. */
    private final AsadminTool asadmin;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Payara tools.
     * <p/>
     * @param asadmin Payara asadmin tool.
     */
    public Tools(AsadminTool asadmin) {
        this.asadmin = asadmin;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get asadmin tool.
     * <p/>
     * @return Asadmin tool.
     */
    @Override
    public AsadminTool getAsadmin() {
        return asadmin;
    }

}
