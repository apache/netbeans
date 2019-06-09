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

import java.io.File;
import java.util.Set;
import org.netbeans.modules.payara.tooling.server.config.JavaEEProfile;
import org.netbeans.modules.payara.tooling.server.config.JavaEESet;
import org.netbeans.modules.payara.tooling.server.config.ModuleType;

/**
 * Payara JavaEE configuration entity.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class PayaraJavaEEConfig {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Supported module types. */
    private final Set<ModuleType> modules;

    /** Supported JavaEE profiles. */
    private final Set<JavaEEProfile> profiles;

    /** Highest JavaEE specification version implemented. */
    private final String version;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of avaEE configuration entity using JavaEE set
     * for Payara features configuration as source of instance content.
     * <p/>
     * @param javaEEconfig  Container of Payara JavaEE
     *                      features configuration.
     * @param classpathHome Classpath search prefix.
     */
    public PayaraJavaEEConfig(
            final JavaEESet javaEEconfig, final File classpathHome) {
        modules = javaEEconfig.moduleTypes(classpathHome);
        profiles = javaEEconfig.profiles(classpathHome);
        version = javaEEconfig.getVersion();
        javaEEconfig.reset();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get highest JavaEE specification version implemented.
     * <p/>
     * @return Highest JavaEE specification version implemented.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get supported JavaEE profiles.
     * <p/>
     * @return Supported JavaEE profiles.
     */
    public Set<JavaEEProfile> getProfiles() {
        return profiles;
    }

    /**
     * Get supported module types.
     * <p/>
     * @return Supported module types.
     */
    public Set<ModuleType> getModuleTypes() {
        return modules;
    }

}
