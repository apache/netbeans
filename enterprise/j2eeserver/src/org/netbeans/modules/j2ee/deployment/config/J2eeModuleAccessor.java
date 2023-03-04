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

package org.netbeans.modules.j2ee.deployment.config;

import javax.enterprise.deploy.shared.ModuleType;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.util.Exceptions;

/**
 * Utility class for accessing some of the non-public methods of the J2eeModule.
 *
 * @author sherold
 */
public abstract class J2eeModuleAccessor {

    private static volatile J2eeModuleAccessor accessor;

    public static void setDefault(J2eeModuleAccessor accessor) {
        if (J2eeModuleAccessor.accessor != null) {
            throw new IllegalStateException("Already initialized accessor"); // NOI18N
        }
        J2eeModuleAccessor.accessor = accessor;
    }

    public static J2eeModuleAccessor getDefault() {
        if (accessor != null) {
            return accessor;
        }

        Class c = J2eeModule.class;
        try {
            Class.forName(c.getName(), true, J2eeModuleAccessor.class.getClassLoader());
        } catch (ClassNotFoundException cnf) {
            Exceptions.printStackTrace(cnf);
        }

        return accessor;
    }

    /**
     * Factory method that creates a J2eeModule for the J2eeModuleImplementation.
     *
     * @param impl SPI J2eeModuleImplementation object
     *
     * @return J2eeModule for the J2eeModuleImplementation.
     * @deprecated use {@link #createJ2eeModule(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2)}
     */
    @Deprecated
    public abstract J2eeModule createJ2eeModule(J2eeModuleImplementation impl);

    /**
     * Factory method that creates a J2eeModule for the J2eeModuleImplementation2.
     *
     * @param impl SPI J2eeModuleImplementation2 object
     *
     * @return J2eeModule for the J2eeModuleImplementation2.
     */
    public abstract J2eeModule createJ2eeModule(J2eeModuleImplementation2 impl);

    /**
     * Returns the J2eeModuleProvider that belongs to the given j2eeModule.
     *
     * @param j2eeModule J2eeModuleObject
     *
     * @return J2eeModuleProvider that belongs to the given j2eeModule.
     */
    public abstract J2eeModuleProvider getJ2eeModuleProvider(J2eeModule j2eeModule);

    /**
     * Associates the J2eeModuleProvider with the spcecified J2eeModule.
     *
     * @param j2eeModule J2eeModule
     * @param J2eeModuleProvider J2eeModuleProvider that belongs to the given J2eeModule.
     */
    public abstract void setJ2eeModuleProvider(J2eeModule j2eeModule, J2eeModuleProvider j2eeModuleProvider);

    public abstract ModuleType getJsrModuleType(J2eeModule.Type type);

}
