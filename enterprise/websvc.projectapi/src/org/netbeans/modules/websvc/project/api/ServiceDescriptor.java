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
package org.netbeans.modules.websvc.project.api;

import java.net.URI;
import java.net.URL;
import org.netbeans.modules.websvc.project.WebServiceAccessor;
import org.netbeans.modules.websvc.project.spi.ServiceDescriptorImplementation;

/**
 * Encapsulation of a Service descriptor
 * Clients make calls to this class which are then delegated to the particular (@link ServiceDescriptorImplementation)
 * @author mkuchtiak
 */
public final class ServiceDescriptor {

    private final ServiceDescriptorImplementation impl;
    

    static {
        WebServiceAccessor.DescriptorAccessor.DEFAULT = new WebServiceAccessor.DescriptorAccessor() {

            @Override
            public ServiceDescriptor createWebServiceDescriptor(ServiceDescriptorImplementation impl) {
                return new ServiceDescriptor(impl);
            }
        };
    }

    private ServiceDescriptor(ServiceDescriptorImplementation impl) {
        this.impl = impl;
    }

    /**
     * Returns the location of the deployed descriptor, if any.
     * @return URL of the deployed descriptor artifact or null.
     */
    public URL getRuntimeLocation() {
        return impl.getRuntimeLocation();
    }

    /**
     * Returns the location of the descriptor in the project, if any. If this descriptor exists, the URI should be relative to the project
     * directory's location.
     * @return URI of the descriptor's location in the project or null.
     */
    public URI getRelativeURI() {
        return impl.getRelativeURI();
    }
}
