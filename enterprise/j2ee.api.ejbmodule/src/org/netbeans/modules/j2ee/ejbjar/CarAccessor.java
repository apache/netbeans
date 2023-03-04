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
package org.netbeans.modules.j2ee.ejbjar;

import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.spi.ejbjar.CarImplementation;
import org.netbeans.modules.j2ee.spi.ejbjar.CarImplementation2;

/* This class provides access to the {@link Car}'s private constructor 
 * from outside in the way that this class is implemented by an inner class of 
 * {@link Car} and the instance is set into the {@link DEFAULT}.
 */
public abstract class CarAccessor {

    public static CarAccessor DEFAULT;
    
    // force loading of Car class. That will set DEFAULT variable.
    static {
        Class c = Car.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Deprecated
    public abstract Car createCar(CarImplementation spiJar);

    public abstract Car createCar(CarImplementation2 spiJar);

}
