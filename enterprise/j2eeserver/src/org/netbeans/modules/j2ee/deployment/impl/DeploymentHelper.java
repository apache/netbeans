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

package org.netbeans.modules.j2ee.deployment.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer;

/**
 * Helper class containing helper methods to deploy various Java EE stuff.
 *
 * @author Petr Hejl
 */
public final class DeploymentHelper {

    private static final Logger LOGGER = Logger.getLogger(DeploymentHelper.class.getName());

    public DeploymentHelper() {
        super();
    }

    public static void deployMessageDestinations(J2eeModuleProvider jmp) throws ConfigurationException {
        ServerInstance si = ServerRegistry.getInstance ().getServerInstance (jmp.getServerInstanceID ());
        if (si != null) {
            si.deployMessageDestinations(jmp.getConfigSupport().getMessageDestinations());
        } else {
            LOGGER.log(Level.WARNING,
                    "The message destinations cannot be deployed because the server instance cannot be found."); // NOI18N
        }
    }

    public static void deployJdbcDrivers(J2eeModuleProvider jmp, ProgressUI progress)
            throws ConfigurationException, TimeoutException {

        ServerInstance si = ServerRegistry.getInstance ().getServerInstance (jmp.getServerInstanceID ());
        if (si != null) {
            Set<Datasource> moduleDatasources = jmp.getConfigSupport().getDatasources();
            if(moduleDatasources != null && moduleDatasources.size() > 0) {
                JDBCDriverDeployer jdbcDriverDeployer = si.getJDBCDriverDeployer();
                if(jdbcDriverDeployer != null) {
                    // Currently it is not possible to select target to which modules will
                    // be deployed. Lets use the first one.
                    ServerTarget targets[] = si.getTargets();
                    if (targets.length > 0) {
                        Target target = targets[0].getTarget();
                        if (jdbcDriverDeployer.supportsDeployJDBCDrivers(target)) {
                            ProgressObject po = jdbcDriverDeployer.deployJDBCDrivers(target, moduleDatasources);
                            ProgressObjectUtil.trackProgressObject(progress, po, Long.MAX_VALUE);
                        }
                    }
                }
            }
        } else {
            LOGGER.log(Level.WARNING,
                    "The JDBC drivers cannot be deployed because the server instance cannot be found."); // NOI18N
        }
    }

    public static void deployDatasources(J2eeModuleProvider jmp) throws ConfigurationException, DatasourceAlreadyExistsException {
        ServerInstance si = ServerRegistry.getInstance ().getServerInstance (jmp.getServerInstanceID ());
        if (si != null) {
            Set<Datasource> moduleDS = jmp.getConfigSupport().getDatasources();
            si.deployDatasources(moduleDS);
        } else {
            LOGGER.log(Level.WARNING,
                    "The data sources cannot be deployed because the server instance cannot be found.");
        }
    }

    public static void deployServerLibraries(J2eeModuleProvider jmp) throws ConfigurationException {
        ServerInstance si = ServerRegistry.getInstance ().getServerInstance (jmp.getServerInstanceID ());
        if (si != null) {
            Set<ServerLibraryDependency> libraries = jmp.getConfigSupport().getLibraries();
            if (jmp instanceof J2eeApplicationProvider) {
                libraries = new HashSet<ServerLibraryDependency>(libraries);
                J2eeApplicationProvider app = (J2eeApplicationProvider) jmp;
                for (J2eeModuleProvider p : app.getChildModuleProviders()) {
                    libraries.addAll(p.getConfigSupport().getLibraries());
                }
            }
            si.deployLibraries(libraries);
        } else {
            LOGGER.log(Level.WARNING,
                    "The libraries cannot be deployed because the server instance cannot be found.");
        }
    }
}
