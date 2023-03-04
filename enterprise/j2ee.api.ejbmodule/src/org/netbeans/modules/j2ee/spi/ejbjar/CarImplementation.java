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

import org.openide.filesystems.FileObject;

/**
 * SPI interface for {@link org.netbeans.modules.j2ee.api.ejbjar.Car}.
 * @see CarFactory
 * @author Pavel Buzek
 * @author Lukas Jungmann
 * @deprecated implement {@link CarImplementation2}
 */
@Deprecated
public interface CarImplementation {
    
    /** J2EE platform version - one of the constants 
     * defined in {@link org.netbeans.modules.j2ee.api.common.J2eeProjectConstants}.
     * @return J2EE platform version
     */
    String getJ2eePlatformVersion ();
    
    /**
     * META-INF folder for the car module.
     *
     * @return the {@link FileObject}; might be <code>null</code>
     */
    FileObject getMetaInf ();

    /**
     * Deployment descriptor (application-client.xml file) of the application
     * client (car) module.
     *
     * @return the {@link FileObject}; might be <code>null</code>
     */
    FileObject getDeploymentDescriptor();
    
    /** Source roots associated with the application client (car) module.
     * <div class="nonnormative">
     * Note that not all the java source roots in the project (e.g. in a freeform project)
     * belong to the Car module.
     * </div>
     */
    FileObject[] getJavaSources();
}
