/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.web.api.webmodule.WebModule;

/**
 * SPI interface for {@link org.netbeans.modules.j2ee.api.ejbjar.Ear}.
 * @see EjbJarFactory
 * @deprecated implement {@link EarImplementation2}
 */
@Deprecated
public interface EarImplementation {

    /** J2EE platform version - one of the constants
     * defined in {@link org.netbeans.modules.j2ee.api.common.EjbProjectConstants}.
     * @return J2EE platform version
     */
    String getJ2eePlatformVersion ();
    
    /** META-INF folder for the Ear.
     */
    FileObject getMetaInf ();

    /** Deployment descriptor (application.xml file) of the ejb module.
     */
    FileObject getDeploymentDescriptor ();
    
    /** Add j2ee webmodule into application.
     * @param module the module to be added
     */
    void addWebModule (WebModule module);
    
    /** Add j2ee ejbjar module into application.
     * @param module the module to be added
     */
    void addEjbJarModule (EjbJar module);
    
    /** Add j2ee application client module into application.
     * @param module the module to be added
     */
    void addCarModule (Car module);
    
}
