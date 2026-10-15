/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;

/**
 * The interface that should serverplugin should implement in order to
 * support the server library management.
 *
 * @since 1.68
 * @author Petr Hejl
 * @see org.netbeans.modules.j2ee.deployment.plugins.spi.config.ServerLibraryConfiguration
 */
public interface ServerLibraryManager {

    /**
     * Returns the set of libraries the server has access to and can be deployed
     * on request (by call to {@link org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance.LibraryManager#deployLibraries(java.util.Set)}.
     *
     * @return the set of libraries which can be deployed on server
     */
    @NonNull
    Set<ServerLibrary> getDeployableLibraries();

    /**
     * Returns the set of libraries already deployed to the server.
     *
     * @return the set of libraries already deployed to the server
     */
    @NonNull
    Set<ServerLibrary> getDeployedLibraries();

    @NonNull
    Set<ServerLibraryDependency> getMissingDependencies(
            @NonNull Set<ServerLibraryDependency> dependencies);

    @NonNull
    Set<ServerLibraryDependency> getDeployableDependencies(
            @NonNull Set<ServerLibraryDependency> dependencies);

    /**
     * Deploys all the required libraries passed to the method. The libraries
     * passed to the method may be already deployed and it is up to implementor
     * to handle such case.
     *
     * @param libraries the libraries to deploy
     * @throws ConfigurationException if there was a problem during
     *             the deployment
     */
    void deployLibraries(@NonNull Set<ServerLibraryDependency> libraries)
            throws ConfigurationException;

    /**
     * Exception there are missing libraries which cannot be deployed.
     *
     * @since 1.105
     */
    public static class MissingLibrariesException extends ConfigurationException {

        private final Set<ServerLibraryDependency> missingLibraries;

        public MissingLibrariesException(String message, Set<ServerLibraryDependency> missingLibraries) {
            super(message);
            this.missingLibraries = missingLibraries;
        }

        public Set<ServerLibraryDependency> getMissingLibraries() {
            return missingLibraries;
        }
    }
}
