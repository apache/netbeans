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

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;

/**
 * Extension of {@link ModuleConfigurationFactory} providing also access to
 * server instance URL.
 * 
 * @author Petr Hejl
 * @since 1.74
 */
public interface ModuleConfigurationFactory2 extends ModuleConfigurationFactory {

    /**
     * Creates a {@link ModuleConfiguration} instance associated with the specified 
     * J2EE module. This method is strictly preferred over
     * {@link #create(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule)}
     * whenever the server instance is known.
     * 
     * @param j2eeModule J2EE module the created ModuleConfigucation should be 
     *        associated with
     * @param deployment URL of the target server instance
     * 
     * @return ModuleConfiguration associated with the specified J2EE module
     */    
    ModuleConfiguration create(@NonNull J2eeModule j2eeModule, @NonNull String instanceUrl) throws ConfigurationException;
}
