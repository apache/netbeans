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
package org.netbeans.modules.glassfish.tooling.server.config;

/**
 * GlassFish tool.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public abstract class GlassFishTool {

    // Instance attributes                                                    //
    /** Tools library directory (relative under GlassFish home). */
    private final String lib;

    // Constructors                                                           //
    /**
     * Creates an instance of GlassFish tool.
     * <p/>
     * @param lib Tools library directory (relative under GlassFish home).
     */
    public GlassFishTool(final String lib) {
        this.lib = lib;
    }

    // Getters and setters                                                    //
    /**
     * Get tools library directory (relative under GlassFish home).
     * <p/>
     * @return Tools library directory (relative under GlassFish home).
     */
    public String getLib() {
        return lib;
    }

}
