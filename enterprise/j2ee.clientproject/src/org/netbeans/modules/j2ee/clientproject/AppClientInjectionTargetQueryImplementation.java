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

package org.netbeans.modules.j2ee.clientproject;

import javax.lang.model.element.TypeElement;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.javaee.injection.spi.InjectionTargetQueryImplementation;

/**
 *
 * @author jungi
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.javaee.injection.spi.InjectionTargetQueryImplementation.class)
public class AppClientInjectionTargetQueryImplementation implements InjectionTargetQueryImplementation {
    
    public AppClientInjectionTargetQueryImplementation() {
    }
    
    public boolean isInjectionTarget(CompilationController controller, TypeElement typeElement) {
        Car apiCar = Car.getCar(controller.getFileObject());
        if (apiCar != null && 
                !Profile.J2EE_13.equals(apiCar.getJ2eeProfile()) &&
                !Profile.J2EE_14.equals(apiCar.getJ2eeProfile())) {
            return SourceUtils.isMainClass(typeElement.getQualifiedName().toString(), controller.getClasspathInfo());
        }
        return false;
    }

    public boolean isStaticReferenceRequired(CompilationController controller, TypeElement typeElement) {
        // all injection references must be static in appclient
        return isInjectionTarget(controller, typeElement);
    }
}
