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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import org.netbeans.modules.j2ee.deployment.config.J2eeApplicationAccessor;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationImplementation;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationImplementation2;

/**
 * Abstraction of J2EE Application. Provides access to basic server-neutral properties
 * of the application: J2EE version, module type, deployment descriptor and its child
 * modules.
 * <p>
 * It is not possible to instantiate this class directly. Implementators have to
 * implement the {@link J2eeApplicationImplementation} first and then use the
 * {@link org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory}
 * to create a J2eeApplication instance.
 *
 * @author Pavel Buzek, Petr Hejl
 */
public class J2eeApplication extends J2eeModule {

    static {
        J2eeApplicationAccessor.setDefault(new J2eeApplicationAccessor() {

            public J2eeApplication createJ2eeApplication(J2eeApplicationImplementation impl) {
                return new J2eeApplication(impl);
            }

            public J2eeApplication createJ2eeApplication(J2eeApplicationImplementation2 impl) {
                return new J2eeApplication(impl);
            }
        });
    }

    private final J2eeApplicationImplementation impl;

    private final J2eeApplicationImplementation2 impl2;

    private J2eeApplication(J2eeApplicationImplementation impl) {
        super(impl);
        this.impl = impl;
        this.impl2 = null;
    }

    private J2eeApplication(J2eeApplicationImplementation2 impl2) {
        super(impl2);
        this.impl = null;
        this.impl2 = impl2;
    }

    /**
     * Returns a list of all the J2EEModules which this J2eeApplication contains.
     *
     * @return list of all the child J2EEModules
     */
    public J2eeModule[] getModules() {
        if (impl2 != null) {
            return impl2.getModules();
        }
        return impl.getModules();
    }

    /**
     * Registers the specified ModuleListener for notification about the module
     * changes.
     *
     * @param listener ModuleListener
     */
    public void addModuleListener(ModuleListener listener) {
        if (impl2 != null) {
            impl2.addModuleListener(listener);
        }
        impl.addModuleListener(listener);
    }

    /**
     * Unregister the specified ModuleListener.
     *
     * @param listener ModuleListener
     */
    public void removeModuleListener(ModuleListener listener) {
        if (impl2 != null) {
            impl2.removeModuleListener(listener);
        }
        impl.removeModuleListener(listener);
    }

}
