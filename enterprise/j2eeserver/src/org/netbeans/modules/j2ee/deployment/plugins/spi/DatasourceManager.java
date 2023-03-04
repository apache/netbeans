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
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;

/**
 * DatasourceManager is responsible for retrieving data sources deployed on the server and
 * deploying data sources onto the server.
 *
 * @author Libor Kotouc
 *
 * @since 1.15
 */
public interface DatasourceManager {
    
    /**
     * Retrieves the data sources deployed on the server
     *
     * @return the set of data sources deployed on the server
     * 
     * @throws ConfigurationException reports problems in retrieving data source
     *         definitions.
     */
    Set<Datasource> getDatasources() throws ConfigurationException;

    /**
     * Deploys given set of data sources.
     *
     * @exception ConfigurationException if there is some problem with data source configuration
     * @exception DatasourceAlreadyExistsException if module data source(s) are conflicting
     * with data source(s) already deployed on the server
     */
    void deployDatasources(Set<Datasource> datasources) 
    throws ConfigurationException, DatasourceAlreadyExistsException;
}
