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
package org.netbeans.modules.j2ee.api.ejbjar;

import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.ejbjar.CarAccessor;
import org.netbeans.modules.j2ee.spi.ejbjar.CarImplementation;
import org.netbeans.modules.j2ee.spi.ejbjar.CarImplementation2;
import org.netbeans.modules.j2ee.spi.ejbjar.CarProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.CarsInProject;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Car should be used to access properties of an Enterprise application client module.
 * <p>
 * A client may obtain a Car instance using 
 * <code>Car.getCar(fileObject)</code> static method, for any 
 * FileObject in the application client module directory structure.
 * </p>
 * <div class="nonnormative">
 * Note that the particular directory structure for application client module
 * is not guaranteed by this API.
 * </div>
 * 
 * 
 * @author Pavel Buzek
 * @author Lukas Jungmann
 */
public final class Car {
    
    private static final Lookup.Result<CarProvider> implementations =
        Lookup.getDefault().lookup(new Lookup.Template<CarProvider>(CarProvider.class));
    
    static  {
        CarAccessor.DEFAULT = new CarAccessor() {

            @Override
            public Car createCar(CarImplementation spiEjbJar) {
                return new Car(spiEjbJar, null);
            }

            @Override
            public Car createCar(CarImplementation2 spiEjbJar) {
                return new Car(null, spiEjbJar);
            }
        };
    }

    private final CarImplementation impl;

    private final CarImplementation2 impl2;
    
    private Car (CarImplementation impl, CarImplementation2 impl2) {
        assert (impl != null && impl2 == null) || (impl == null && impl2 != null);
        this.impl = impl;
        this.impl2 = impl2;
    }
    
    /**
     * Find the Car for given file or null if the file does not belong
     * to any application client module.
     */
    public static Car getCar (FileObject f) {
        if (f == null) {
            throw new NullPointerException("Passed null to Car.getCar(FileObject)"); // NOI18N
        }
        for (CarProvider impl : implementations.allInstances()) {
            Car wm = impl.findCar (f);
            if (wm != null) {
                return wm;
            }
        }
        return null;
    }

    /** Find Car(s) for all application clients within a given project.
     * @return an array of Car instance (empty array if no instance are found).
     */
    public static Car[] getCars (Project project) {
        CarsInProject providers = project.getLookup().lookup(CarsInProject.class);
        if (providers != null) {
            Car jars [] = providers.getCars();
            if (jars != null) {
                return jars;
            }
        }
        return new Car[] {};
    }
    
    /** J2EE platform version - one of the constants 
     * defined in {@link org.netbeans.modules.j2ee.api.common.J2eeProjectConstants}.
     * @return J2EE platform version
     * @deprecated use {@link #getJ2eeProfile()}
     */
    @Deprecated
    public String getJ2eePlatformVersion () {
        if (impl2 != null) {
            return impl2.getJ2eeProfile().toPropertiesString();
        }
        return impl.getJ2eePlatformVersion();
    }

    public Profile getJ2eeProfile() {
        if (impl2 != null) {
            return impl2.getJ2eeProfile();
        }
        return Profile.fromPropertiesString(impl.getJ2eePlatformVersion());
    }
    
    /** Deployment descriptor (application-client.xml file) of the application client module.
     */
    public FileObject getDeploymentDescriptor () {
        if (impl2 != null) {
            return impl2.getDeploymentDescriptor();
        }
        return impl.getDeploymentDescriptor();
    }

    /** Source roots associated with the Car module.
     * <div class="nonnormative">
     * Note that not all the java source roots in the project (e.g. in a freeform project)
     * belong to the Car module.
     * </div>
     */
    public FileObject[] getJavaSources() {
        if (impl2 != null) {
            return impl2.getJavaSources();
        }
        return impl.getJavaSources();
    }
    
    /** Meta-inf
     */
    public FileObject getMetaInf() {
        if (impl2 != null) {
            return impl2.getMetaInf();
        }
        return impl.getMetaInf();
    }
}
