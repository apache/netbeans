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

import java.beans.PropertyChangeListener;
import java.util.List;
import org.netbeans.modules.websvc.project.api.WebService;

/**
 * Provider interface for obtaining web service data in a project.
 * Each Web service stack (e.g., JAXRPC, JAXWS, JAXRS, AXIS) will implement this interface. 
 * Implementations of this interface will be registered in the project lookup.
 * When a client does a project lookup for this interface, the WebServiceDataProvider instance that is returned
 * will contain the merged result (service providers, service consumers) of all WebServiceDataProviders that are in the 
 * project's lookup.
 * 
 * @see LookupMergerSupport
 * @author mkuchtiak
 */
public interface WebServiceDataProvider {
    
    /**
     * Returns a list of WebServices that act as service providers
     * @return List of WebServices
     */
    List<WebService> getServiceProviders();
    
    /**
     * Returns a list of WebServices that act as service consumers
     * @return List of WebServices
     */
    List<WebService> getServiceConsumers();
    
    /**
     * Adds a PropertyChangeListener to the listener list.
     * @param pcl PropertyChangeListener to be added to the list.
     */
    void addPropertyChangeListener(PropertyChangeListener pcl);
    
    /**
     * Removes a PropertyChangeListener from the listener list.
     * @param pcl PropertyChangeListener to be removed from the list.
     */
    void removePropertyChangeListener(PropertyChangeListener pcl);
}
