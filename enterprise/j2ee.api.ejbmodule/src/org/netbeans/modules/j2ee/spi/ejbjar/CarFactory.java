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
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.ejbjar.EarAccessor;
import org.netbeans.modules.j2ee.ejbjar.CarAccessor;

/**
 * Most general way to create {@link Car} and {@link Ear} instances.
 * You are not permitted to create them directly; instead you implement
 * {@link CarImplementation} or {@link EarImplementation} and use this factory.
 *
 * @author  Pavel Buzek
 * @author  Lukas Jungmann
 */
public final class CarFactory {

    private CarFactory () {
    }

    /**
     * Create API application client module (carmodule)
     * instance for the given SPI carmodule.
     * @param spiCarmodule instance of SPI carmodule
     * @return instance of API car module
     */
    public static Car createCar(CarImplementation spiCarmodule) {
        return CarAccessor.DEFAULT.createCar (spiCarmodule);
    }

    public static Car createCar(CarImplementation2 spiCarmodule) {
        return CarAccessor.DEFAULT.createCar (spiCarmodule);
    }

    /**
     * Create API Ear instance for the given SPI carmodule.
     * @param spiEar instance of SPI Ear
     * @return instance of API Ear
     */
    public static Ear createEar(EarImplementation spiEar) {
        return EarAccessor.DEFAULT.createEar (spiEar);
    }
}
