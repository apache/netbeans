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

package org.netbeans.modules.j2ee.deployment.plugins.spi.config;

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.openide.util.Lookup;

/**
 * An interface that defines a container for all the server-specific configuration 
 * information for a single top-level J2EE module. The ModuleConfiguration object 
 * could represent a single stand-alone module or a J2EE application that contains 
 * several sub-modules. The ModuleConfiguration object contains in its lookup a set 
 * of configurations that are used for managing the server-specific settings.
 *
 * @since 1.23
 * @author sherold
 */
public interface ModuleConfiguration extends Lookup.Provider {
    
    /**
     * Returns lookup associated with the object. This lookup should contain
     * implementations of all the supported configurations.
     * <p>
     * The configuration are:  {@link ContextRootConfiguration},  {@link DatasourceConfiguration}, 
     * {@link MappingConfiguration}, {@link EjbResourceConfiguration}, {@link DeploymentPlanConfiguration},
     * {@link MessageDestinationConfiguration}
     * <p>
     * Implementators are advised to use {@link org.openide.util.lookup.Lookups#fixed}
     * to implement this method.
     * 
     * @return lookup associated with the object containing all the supported
     *         ConfigurationProvider implementations.
     */
    Lookup getLookup();
    
    /**
     * Returns a J2EE module associated with this ModuleConfiguration instance.
     * 
     * @return a J2EE module associated with this ModuleConfiguration instance.
     */
    J2eeModule getJ2eeModule();
    
    /**
     * The j2eeserver calls this method when it is done using this ModuleConfiguration 
     * instance. The server plug-in should free all the associated resources -
     * listeners for example.
     */
    void dispose();
}
