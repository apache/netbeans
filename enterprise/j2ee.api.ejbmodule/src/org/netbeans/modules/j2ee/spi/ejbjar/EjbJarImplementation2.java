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

import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.openide.filesystems.FileObject;

/**
 * SPI interface for {@link org.netbeans.modules.j2ee.api.ejbjar.EjbJar}.
 * @see EjbJarFactory
 */
public interface EjbJarImplementation2 {

    Profile getJ2eeProfile();

    /**
     * META-INF folder for the ejb module.
     *
     * @return the {@link FileObject}; might be <code>null</code>
     */
    FileObject getMetaInf ();

    /**
     * Deployment descriptor (ejb-jar.xml file) of the ejb module.
     *
     * @return the {@link FileObject}; might be <code>null</code>
     */
    FileObject getDeploymentDescriptor ();

    /** Source roots associated with the EJB module.
     * <div class="nonnormative">
     * Note that not all the java source roots in the project (e.g. in a freeform project)
     * belong to the EJB module.
     * </div>
     */
    FileObject[] getJavaSources();
    
    /**
     * Returns the metadata associated with this EJB module.
     */
    MetadataModel<EjbJarMetadata> getMetadataModel();

}
