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
package org.netbeans.modules.glassfish.tooling.server.config;

import org.netbeans.modules.glassfish.tooling.data.ToolConfig;
import org.netbeans.modules.glassfish.tooling.utils.OsUtils;

/**
 * GlassFish asadmin tool.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class AsadminTool extends GlassFishTool implements ToolConfig {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Asadmin tool JAR path (relative under GlassFish home). */
    private final String jar;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of GlassFish asadmin tool.
     * <p/>
     * @param lib Tools library directory (relative under GlassFish home).
     * @param jar Asadmin tool JAR (relative under tools library directory).
     */
    public AsadminTool(final String lib, final String jar) {
        super(lib);
        this.jar = OsUtils.joinPaths(lib, jar);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get asadmin tool JAR path (relative under GlassFish home)
     * <p/>
     * @return Asadmin tool JAR path (relative under GlassFish home)
     */
    @Override
    public String getJar() {
        return jar;
    }

}
