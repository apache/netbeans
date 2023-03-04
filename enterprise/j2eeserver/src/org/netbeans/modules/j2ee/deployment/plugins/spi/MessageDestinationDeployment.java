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

package org.netbeans.modules.j2ee.deployment.plugins.spi;

import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;

/**
 * MessageDestinationDeployment is responsible for retrieving message destinations
 * configured on the server and for message destination deployment.
 *
 * @author Libor Kotouc
 *
 * @since 1.25
 */
public interface MessageDestinationDeployment {
    
    /**
     * Retrieves message destinations configured on the target server instance.
     *
     * @return set of message destinations
     * 
     * @throws ConfigurationException if there is some problem with message destination configuration
     */
    Set<MessageDestination> getMessageDestinations() throws ConfigurationException;

    /**
     * Deploys message destinations saved in the module.
     *
     * @param destinations set of message destinations
     * 
     * @exception ConfigurationException if there is some problem with message destination configuration
     */
    void deployMessageDestinations(Set<MessageDestination> destinations) throws ConfigurationException;
}
