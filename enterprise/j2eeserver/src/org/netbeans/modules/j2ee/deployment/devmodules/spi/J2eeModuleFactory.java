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

package org.netbeans.modules.j2ee.deployment.devmodules.spi;

import org.netbeans.modules.j2ee.deployment.config.J2eeModuleAccessor;
import org.netbeans.modules.j2ee.deployment.config.J2eeApplicationAccessor;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication;

/**
 * A factory class to create {@link J2eeModule} and {@link J2eeApplication} 
 * instances. You are not permitted to create them directly; instead you implement 
 * {@link J2eeModuleImplementation} or {@link J2eeApplicationImplementation} 
 * and use this factory.
 * 
 * 
 * @author sherold
 * @since 1.23
 */
public class J2eeModuleFactory {
    
    /** Creates a new instance of J2eeModuleFactory */
    private J2eeModuleFactory() {
    }
    
    /**
     * Creates a J2eeModule for the specified J2eeModuleImplementation.
     * 
     * @param impl the J2eeModule SPI object
     * 
     * @return J2eeModule API instance.
     * @deprecated use {@link #createJ2eeApplication(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationImplementation2)}
     */
    @Deprecated
    public static J2eeModule createJ2eeModule(J2eeModuleImplementation impl) {
        if (impl == null) {
            throw new NullPointerException();
        }
        return J2eeModuleAccessor.getDefault().createJ2eeModule(impl);
    }

    /**
     * Creates a J2eeModule for the specified J2eeModuleImplementation2.
     *
     * @param impl the J2eeModule SPI object
     *
     * @return J2eeModule API instance.
     */
    public static J2eeModule createJ2eeModule(J2eeModuleImplementation2 impl) {
        if (impl == null) {
            throw new NullPointerException();
        }
        return J2eeModuleAccessor.getDefault().createJ2eeModule(impl);
    }
    
    /**
     * Creates a J2eeApplication for the specified J2eeApplicationImplementation.
     * 
     * 
     * @param impl the J2eeApplication SPI object
     * @return J2eJ2eeApplicationI instance.
     * @deprecated use {@link #createJ2eeApplication(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationImplementation2)}
     */
    @Deprecated
    public static J2eeApplication createJ2eeApplication(J2eeApplicationImplementation impl) {
        if (impl == null) {
            throw new NullPointerException();
        }
        return J2eeApplicationAccessor.getDefault().createJ2eeApplication(impl);
    }

    /**
     * Creates a J2eeApplication for the specified J2eeApplicationImplementation.
     *
     *
     * @param impl the J2eeApplication SPI object
     * @return J2eJ2eeApplicationI instance.
     */
    public static J2eeApplication createJ2eeApplication(J2eeApplicationImplementation2 impl) {
        if (impl == null) {
            throw new NullPointerException();
        }
        return J2eeApplicationAccessor.getDefault().createJ2eeApplication(impl);
    }
}
