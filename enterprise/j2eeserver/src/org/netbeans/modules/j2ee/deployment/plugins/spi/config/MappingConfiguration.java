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

package org.netbeans.modules.j2ee.deployment.plugins.spi.config;

import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;

/**
 * Configuration CMP mapping.
 * <p>
 * Implementation of this interface should be registered in the {@link ModuleConfiguration}
 * lookup.
 * 
 * @since 1.23
 * @author sherold
 */
public interface MappingConfiguration {
    
    
    /**
     * Sets the resource for the specified CMP bean. Some containers may not
     * support fine-grained per bean resource definition, in which case global
     * EJB module CMP resource is set.
     *
     * @param ejbName   name of the CMP bean.
     * @param jndiName  the JNDI name of the resource.
     *
     * @throws ConfigurationException reports errors in setting the CMP resource.
     * @throws NullPointerException if any of the parameters is <code>null</code>.
     * 
     * @since 1.30
     */
    void setCMPResource(String ejbName, String jndiName) throws ConfigurationException;
    
    /**    
     * Sets the CMP mapping info for the EJB by the given name.
     * 
     * @param mappings All the mapping info needed to be pushed in one batch.
     * 
     * @throws ConfigurationException reports errors in setting the CMP mapping info.
     */
    void setMappingInfo(OriginalCMPMapping[] mappings) throws ConfigurationException;
}
