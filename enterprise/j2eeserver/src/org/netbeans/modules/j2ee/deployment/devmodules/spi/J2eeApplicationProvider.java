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
package org.netbeans.modules.j2ee.deployment.devmodules.spi;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;

/**
 * This interface must be implemented by J2EE Application support and an instance 
 * added into project lookup.
 *
 * @author sherold
 * 
 * @since 1.23
 */
public abstract class J2eeApplicationProvider extends J2eeModuleProvider {

    /**
     * Returns the provider for the child module specified by given URI.
     * 
     * @param uri the child module URI within the J2EE application.
     * 
     * @return J2eeModuleProvider object
     */
    public abstract J2eeModuleProvider getChildModuleProvider(String uri);

    /**
     * Returns list of providers of every child J2EE module of this J2EE app.
     * 
     * @return array of J2eeModuleProvider objects.
     */
    public abstract J2eeModuleProvider[] getChildModuleProviders();
    
    /**
     * Overrides the <code>J2eeModuleProvider's</code> implementation so that 
     * the data sources from the child modules are returned
     * 
     * @throws ConfigurationException when an error occured while retrieving 
     *         module data sources.
     */
    public Set<Datasource> getModuleDatasources() throws ConfigurationException {
        
        Set<Datasource> projectDS = new HashSet<Datasource>();
        
        for (J2eeModuleProvider modProvider : getChildModuleProviders()) {
            projectDS.addAll(modProvider.getModuleDatasources());
        }
        
        return projectDS;
    }
    
}
