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

package org.netbeans.modules.j2ee.deployment.devmodules.spi;

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleListener;

/** 
 * Base SPI interface for {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication}.
 * Implementation of this interface is used to create
 * {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication}
 * instance using the {@link J2eeModuleFactory}.
 * 
 * @author sherold, Petr Hejl
 * 
 * @since 1.59
 */
public interface J2eeApplicationImplementation2 extends J2eeModuleImplementation2 {

    /**
     * Returns a list of all the J2EEModules which this J2eeApplication contains.
     * 
     * @return list of all the child J2EEModules
     */
    J2eeModule[] getModules();

    /**
     * Registers the specified ModuleListener for notification about the module
     * changes.
     * 
     * @param listener ModuleListener
     */
    void addModuleListener(ModuleListener listener);

    /**
     * Unregister the specified ModuleListener.
     * 
     * @param listener ModuleListener
     */
    void removeModuleListener(ModuleListener listener);
    
}
