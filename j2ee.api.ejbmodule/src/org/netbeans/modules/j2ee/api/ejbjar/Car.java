/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
