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

/**
 * Configuration for EJB resources.
 * <p>
 * Implementation of this interface should be registered in the {@link ModuleConfiguration}
 * lookup.
 * 
 * @since 1.23
 * @author sherold 
 */
public interface EjbResourceConfiguration {
    
    /**
     * Returns a JNDI name for the given EJB or <code>null</code> if the EJB has 
     * no JNDI name assigned.
     *
     * @param  ejbName EJB name
     * 
     * @return JNDI name bound to the EJB or <code>null</code> if the EJB has no 
     *         JNDI name assigned.
     * 
     * @throws ConfigurationException if there is some problem with EJB configuration.
     * 
     * @since 1.31
     */
     public String findJndiNameForEjb(String ejbName) throws ConfigurationException;
    
    
    /**
     * Binds an EJB reference name with an EJB JNDI name.
     * 
     * @param referenceName name used to identify the EJB
     * @param jndiName JNDI name of the referenced EJB
     * 
     * @throws ConfigurationException if there is some problem with EJB configuration
     * 
     * @since 1.26
     */
    public void bindEjbReference(String referenceName, String jndiName) throws ConfigurationException;

    /**
     * Binds an EJB reference name with an EJB name within the EJB scope.
     * 
     * @param ejbName EJB name
     * @param ejbType EJB type - the possible values are 
     *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.SESSION,
     *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.ENTITY and
     *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.MESSAGE_DRIVEN
     * @param referenceName name used to identify the referenced EJB
     * @param jndiName JNDI name of the referenced EJB
     * 
     * @throws NullPointerException if any of parameters is null
     * @throws ConfigurationException if there is some problem with EJB configuration
     * @throws IllegalArgumentException if ejbType doesn't have one of allowed values
     * 
     * @since 1.26
     */
    public void bindEjbReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String jndiName) throws ConfigurationException;
}
