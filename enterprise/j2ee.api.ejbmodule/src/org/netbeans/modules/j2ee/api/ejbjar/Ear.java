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
import org.netbeans.modules.j2ee.ejbjar.EarAccessor;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation2;
import org.netbeans.modules.j2ee.spi.ejbjar.EarProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Ear should be used to access properties of an ear module.
 * <p>
 * A client may obtain a Ear instance using
 * <code>Ear.getEar(fileObject)</code> static method, for any
 * FileObject in the ear module directory structure.
 * </p>
 * <div class="nonnormative">
 * Note that the particular directory structure for ear module is not guaranteed 
 * by this API.
 * </div>
 *
 * @author  Pavel Buzek
 */
public final class Ear {
    
    private static final Lookup.Result<EarProvider> implementations =
        Lookup.getDefault().lookup(new Lookup.Template<EarProvider>(EarProvider.class));
    
    static  {
        EarAccessor.DEFAULT = new EarAccessor() {

            @Override
            public Ear createEar(EarImplementation spiEar) {
                return new Ear(spiEar, null);
            }

            @Override
            public Ear createEar(EarImplementation2 spiEar) {
                return new Ear(null, spiEar);
            }
        };
    }

    @SuppressWarnings("deprecation")
    private final EarImplementation impl;

    private final EarImplementation2 impl2;

    @SuppressWarnings("deprecation")
    private Ear (EarImplementation impl, EarImplementation2 impl2) {
        assert (impl != null && impl2 == null) || (impl == null && impl2 != null);
        this.impl = impl;
        this.impl2 = impl2;
    }
    
    /**
     * Find the Ear for given file or <code>null</code> if the file does not
     * belong to any Enterprise Application.
     */
    public static Ear getEar (FileObject f) {
        if (f == null) {
            throw new NullPointerException("Passed null to Ear.getEar(FileObject)"); // NOI18N
        }
        for (EarProvider earProvider : implementations.allInstances()) {
            Ear wm = earProvider.findEar(f);
            if (wm != null) {
                return wm;
            }
        }
        return null;
    }

    
    /** J2EE platform version - one of the constants 
     * defined in {@link org.netbeans.modules.j2ee.api.common.EjbProjectConstants}.
     * @return J2EE platform version
     * @deprecated use {@link #getJ2eeProfile()}
     */
    @Deprecated
    public String getJ2eePlatformVersion () {
        if (impl2 != null) {
            // TODO null happens when EAR is deleted and getApplication is called
            // invent better fix #168399
            Profile profile = impl2.getJ2eeProfile();
            if (profile != null) {
                return profile.toPropertiesString();
            }
            return null;
        }
        return impl.getJ2eePlatformVersion();
    }

    public Profile getJ2eeProfile() {
        if (impl2 != null) {
            return impl2.getJ2eeProfile();
        }
        return Profile.fromPropertiesString(impl.getJ2eePlatformVersion());
    }
    
    /** Deployment descriptor (ejb-jar.xml file) of the ejb module.
     */
    public FileObject getDeploymentDescriptor () {
        if (impl2 != null) {
            impl2.getDeploymentDescriptor();
        }
        return impl.getDeploymentDescriptor();
    }
    
    /** Add j2ee webmodule into application.
     * @param module the module to be added
     */
    public void addWebModule (WebModule module) {
        if (impl2 != null) {
            impl2.addWebModule(module);
        } else {
            impl.addWebModule (module);
        }
    }
    
    /** Add j2ee Ejb module into application.
     * @param module the module to be added
     */
    public void addEjbJarModule (EjbJar module) {
        if (impl2 != null) {
            impl2.addEjbJarModule(module);
        } else {
            impl.addEjbJarModule (module);
        }
    }
    
    /** Add j2ee application client module into application.
     * @param module the module to be added
     */
    public void addCarModule(Car module) {
        if (impl2 != null) {
            impl2.addCarModule(module);
        } else {
            impl.addCarModule(module);
        }
    }
    
}
