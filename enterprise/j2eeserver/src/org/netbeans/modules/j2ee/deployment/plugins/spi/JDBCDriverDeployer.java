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
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;

/**
 * JDBC driver deployer useful for deploying drivers to the server.
 * <p>
 * Implementation of JDBC driver deployer should be registerd via the {@link 
 * OptionalDeploymentManagerFactory}.
 * 
 * @author sherold
 * 
 * @since 1.24
 */
public interface JDBCDriverDeployer {
    
    /**
     * Returns true if the specified target supports deployment of JDBC drivers,
     * false otherwise.
     * 
     * @param target the JDBC drivers maight be deployed to.
     * 
     * @return true if the specified target supports deployment of JDBC drivers,
     *         false otherwise.
     */
    boolean supportsDeployJDBCDrivers(Target target);
    
    /**
     * Deploys JDBC drivers for all the specified resources to the specified target
     * server if the drivers have not been deployed yet.
     * 
     * @param target where the drivers should be deployed to.
     * @param datasources 
     */
    ProgressObject deployJDBCDrivers(Target target, Set<Datasource> datasources);
    
}
