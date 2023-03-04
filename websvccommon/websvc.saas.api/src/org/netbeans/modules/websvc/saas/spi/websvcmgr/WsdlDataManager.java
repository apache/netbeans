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

package org.netbeans.modules.websvc.saas.spi.websvcmgr;

import org.netbeans.modules.websvc.saas.*;

/**
 * Hook to reuse websvc.manager retrieval, compiling and persistence facilityes.
 * Only to be implemented by websvc.manager.
 * 
 * @author nam
 */
public interface WsdlDataManager {
    /**
     * Find the WSDL data for the given WSDL URL and service name.
     * 
     * @param wsdlUrl
     * @param serviceName  optional service name; if null return default service
     * @return WsdlData object or null if does not exist in repository.
     */
    WsdlData findWsdlData(String wsdlUrl, String serviceName);
    
    /**
     * Get the WSDL data for the given WSDL URL.
     * 
     * @param wsdlUrl
     * @param serviceName  optional service name; if null return default service
     * @param synchronuous whether the call is synchronous.
     * @return WsdlData object, in ready state for consumer editor, if synchronous.
     */
    WsdlData getWsdlData(String wsdlUrl, String serviceName, boolean synchronuous);
    
    /**
     * Asynchronously add the WSDL data for given WSDL URL from persistence.
     * @param wsdlUrl
     * @param packageName
     * @return a wsdl data object, would not be in ready state, so attach a listener.
     */
    WsdlData addWsdlData(String wsdlUrl, String packageName);

    /**
     * Remove the WSDL data for given WSDL URL from persistence.
     * @param wsdlUrl
     * @param serviceName
     */
  
    void removeWsdlData(String wsdlUrl, String serviceName);
    
    /**
     * Refresh WSDL artifacts from given data.
     */
    void refresh(WsdlData data);

    /**
     * Sets the priority for this implementation in the Lookup
     * @param precedence The lower value will have precedence over a higher one.
     */
    void setPrecedence(int precedence);

    /**
     * Gets the priority for this implementation in the Lookup
     * @return  The precedence integer. The lower value will have precedence over a higher one.
     */
    int getPrecedence();
}
