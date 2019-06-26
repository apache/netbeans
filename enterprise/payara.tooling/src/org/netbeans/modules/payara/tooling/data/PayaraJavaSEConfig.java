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
package org.netbeans.modules.payara.tooling.data;

import java.util.Set;
import org.netbeans.modules.payara.tooling.server.config.JavaSEPlatform;
import org.netbeans.modules.payara.tooling.server.config.JavaSESet;

/**
 * Container of Payara JavaSE features configuration.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class PayaraJavaSEConfig {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Platforms retrieved from XML elements. */
    private final Set<JavaSEPlatform> platforms;

    /** Highest JavaEE specification version implemented. */
    private final String version;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of avaEE configuration entity using JavaEE set
     * for Payara features configuration as source of instance content.
     * <p/>
     * @param javaSEconfig Container of Payara JavaEE features configuration.
     */
    public PayaraJavaSEConfig(final JavaSESet javaSEconfig) {
        platforms = javaSEconfig.platforms();
        version = javaSEconfig.getVersion();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get highest JavaSE specification version implemented.
     * <p/>
     * @return Highest JavaSE specification version implemented.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get supported JavaSE platforms.
     * <p/>
     * @return Supported JavaSE platforms.
     */
    public Set<JavaSEPlatform> getPlatforms() {
        return platforms;
    }

}
