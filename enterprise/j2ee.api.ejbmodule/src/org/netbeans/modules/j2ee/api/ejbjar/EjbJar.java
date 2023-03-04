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
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.ejbjar.EjbJarAccessor;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.spi.ejbjar.*;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * EjbJar should be used to access properties of an ejb jar module.
 * <p>
 * A client may obtain an EjbJar instance using {@link EjbJar#getEjbJar} method
 * for any FileObject in the ejb jar module directory structure.
 * </p>
 * <div class="nonnormative">
 * Note that the particular directory structure for ejb jar module is not guaranteed 
 * by this API.
 * </div>
 *
 * @author  Pavel Buzek
 */
public final class EjbJar {

    private static final Lookup.Result<EjbJarProvider> implementations =
        Lookup.getDefault().lookup(new Lookup.Template<EjbJarProvider>(EjbJarProvider.class));
    
    static  {
        EjbJarAccessor.setDefault(new EjbJarAccessor() {

            @Override
            public EjbJar createEjbJar(EjbJarImplementation spiEjbJar) {
                return new EjbJar(spiEjbJar, null);
            }

            @Override
            public EjbJar createEjbJar(EjbJarImplementation2 spiEjbJar) {
                return new EjbJar(null, spiEjbJar);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private final EjbJarImplementation impl;

    private final EjbJarImplementation2 impl2;

    @SuppressWarnings("deprecation")
    private EjbJar (EjbJarImplementation impl, EjbJarImplementation2 impl2) {
        assert (impl != null && impl2 == null) || (impl == null && impl2 != null);
        this.impl = impl;
        this.impl2 = impl2;
    }
    
    /** Find the EjbJar for given file or null if the file does not belong
     * to any web module.
     */
    public static EjbJar getEjbJar (FileObject f) {
        if (f == null) {
            throw new NullPointerException("Passed null to EjbJar.getEjbJar(FileObject)"); // NOI18N
        }
        for (EjbJarProvider impl : implementations.allInstances()) {
            EjbJar wm = impl.findEjbJar (f);
            if (wm != null) {
                return wm;
            }
        }
        return null;
    }

    /** Find EjbJar(s) for all ejb modules within a given project.
     * @return an array of EjbJar instance (empty array if no instance are found).
     */
    public static EjbJar [] getEjbJars (Project project) {
        EjbJarsInProject providers = project.getLookup().lookup(EjbJarsInProject.class);
        if (providers != null) {
            EjbJar jars [] = providers.getEjbJars();
            if (jars != null) {
                return jars;
            }
        }
        return new EjbJar[] {};
    }
    
    /** J2EE platform version - one of the constants 
     * defined in {@link org.netbeans.modules.j2ee.api.common.EjbProjectConstants}.
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
    
    /**
     * Deployment descriptor (ejb-jar.xml file) of an ejb module.
     *
     * @return descriptor FileObject or <code>null</code> if not available.
     */
    public FileObject getDeploymentDescriptor () {
        if (impl2 != null) {
            return impl2.getDeploymentDescriptor();
        }
        return impl.getDeploymentDescriptor();
    }

    /** Source roots associated with the EJB module.
     * <div class="nonnormative">
     * Note that not all the java source roots in the project (e.g. in a freeform project)
     * belong to the EJB module.
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

    public MetadataModel<EjbJarMetadata> getMetadataModel() {
        if (impl2 != null) {
            return impl2.getMetadataModel();
        }
        return impl.getMetadataModel();
    }

}
