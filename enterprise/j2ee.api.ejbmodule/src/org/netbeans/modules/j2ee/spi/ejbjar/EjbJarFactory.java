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

package org.netbeans.modules.j2ee.spi.ejbjar;

import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.ejbjar.EjbJarAccessor;
import org.netbeans.modules.j2ee.ejbjar.EarAccessor;

/**
 * Most general way to create {@link EjbJar} and {@link Ear} instances.
 * You are not permitted to create them directly; instead you implement
 * {@link EjbJarImplementation} or {@link EarImplementation} and use this factory.
 *
 * @author  Pavel Buzek
 */
public final class EjbJarFactory {

    private EjbJarFactory () {
    }

    /**
     * Create API ejbmodule instance for the given SPI webmodule.
     * @param spiWebmodule instance of SPI webmodule
     * @return instance of API webmodule
     * @deprecated use {@link #create }
     */
    @Deprecated
    public static EjbJar createEjbJar(EjbJarImplementation spiWebmodule) {
        return EjbJarAccessor.getDefault().createEjbJar (spiWebmodule);
    }

    /**
     * Create API ejbmodule instance for the given SPI ejbmodule.
     *
     * @param spiWebmodule instance of SPI ejbmodule
     * @return instance of API ejbmodule
     */
    public static EjbJar createEjbJar(EjbJarImplementation2 spiWebmodule) {
        return EjbJarAccessor.getDefault().createEjbJar (spiWebmodule);
    }

    /**
     * Create API Ear instance for the given SPI webmodule.
     * @param spiEar instance of SPI Ear
     * @return instance of API Ear
     */
    public static Ear createEar(EarImplementation spiEar) {
        return EarAccessor.DEFAULT.createEar(spiEar);
    }

    public static Ear createEar(EarImplementation2 spiEar) {
        return EarAccessor.DEFAULT.createEar(spiEar);
    }
}
