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

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationImplementation;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationImplementation2;
import org.openide.util.Exceptions;

/**
 * Utility class for accessing non-public constructor of the J2eeApplication.
 *
 *
 * @author sherold
 */
public abstract class J2eeApplicationAccessor {

    private static volatile J2eeApplicationAccessor accessor;

    public static void setDefault(J2eeApplicationAccessor accessor) {
        if (J2eeApplicationAccessor.accessor != null) {
            throw new IllegalStateException("Already initialized accessor"); // NOI18N
        }
        J2eeApplicationAccessor.accessor = accessor;
    }

    public static J2eeApplicationAccessor getDefault() {
        if (accessor != null) {
            return accessor;
        }

        Class c = J2eeApplication.class;
        try {
            Class.forName(c.getName(), true, J2eeApplicationAccessor.class.getClassLoader()); // NOI18N
        } catch (ClassNotFoundException cnf) {
            Exceptions.printStackTrace(cnf);
        }

        return accessor;
    }

    /**
     * Factory method that creates a J2eeApplication for the J2eeApplicationImplementation.
     *
     * @param impl SPI J2eeApplicationImplementation object
     * @return J2eeApplication
     * @deprecated use {@link #createJ2eeApplication(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationImplementation2)}
     */
    @Deprecated
    public abstract J2eeApplication createJ2eeApplication(J2eeApplicationImplementation impl);

    /**
     * Factory method that creates a J2eeApplication for the J2eeApplicationImplementation2.
     *
     * @param impl SPI J2eeApplicationImplementation2 object
     * @return J2eeApplication
     */
    public abstract J2eeApplication createJ2eeApplication(J2eeApplicationImplementation2 impl);
}
