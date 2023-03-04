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

import org.netbeans.modules.websvc.project.api.WebService;
import org.netbeans.modules.websvc.project.api.ServiceDescriptor;
import org.openide.nodes.Node;

/**
 * SPI for {@link org.netbeans.modules.websvc.project.api.WebService}.
 * @see WebServiceFactory
 * @author mkuchtiak
 */
public interface WebServiceImplementation {

    /**
     * Returns an identifier for the service provider or consumer
     */
    String getIdentifier();
    
    /**
     * Says whether this is a service provider or service consumer
     * @return true if it is a service provider, false if consumer
     */
    boolean isServiceProvider();
    
    /**
     * Returns the type of service (e.g., SOAP, REST, etc)
     * @return Type of service
     */
    WebService.Type getServiceType();
    
    /**
     * Returns the service descriptor for this web service implementation
     * @return ServiceDescriptor
     */
    ServiceDescriptor getServiceDescriptor();
    
    /**
     * Creates Netbeans node representing the web service
     * @return Node representing the web service.
     */
    Node createNode();
}

