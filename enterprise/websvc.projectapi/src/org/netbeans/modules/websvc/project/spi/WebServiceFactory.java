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

package org.netbeans.modules.websvc.project.spi;

import org.netbeans.modules.websvc.project.WebServiceAccessor;
import org.netbeans.modules.websvc.project.api.WebService;
import org.netbeans.modules.websvc.project.api.ServiceDescriptor;


/**
 * Factory for creating  {@link WebService} and {@link ServiceDescriptor} instances.
 * Instances of these classes cannot be created directly. Instead this factory should be used to create
 * the instances, passing your implementations of {@link WebServiceImplementation} and {@link ServiceDescriptorImplementation}.
 * @author Milan Kuchtiak
 */
public final class WebServiceFactory {

    private WebServiceFactory() {
    }
    
    /**
     * Creates a WebService instance for the given WebServiceImplementation (spi).
     * @param  spi an instance of a WebServiceImplementation.
     * @return an instance of a WebService.
     */
    public static WebService createWebService(WebServiceImplementation spi) {
        return WebServiceAccessor.getDefault().createWebService(spi);
    }

    /**
     * Creates a ServiceDescriptor instance for the given ServiceDescriptorImplementation (spi).
     * @param  spi an instance of a ServiceDescriptorImplementation.
     * @return an instance of ServiceDescriptor.
     */
    public static ServiceDescriptor createWebServiceDescriptor(ServiceDescriptorImplementation spi) {
        return WebServiceAccessor.DescriptorAccessor.getDefault().createWebServiceDescriptor(spi);
    }

}
