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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import java.util.Set;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.openide.util.Exceptions;

/**
 * The class allowing the client to query the instance identified by
 * the instance ID. Because corresponding instance can be removed anytime
 * each method can throw {@link InstanceRemovedException}.
 *
 * @author Petr Hejl
 * @since 1.45
 */
public final class ServerInstance {

    /*
     * There is bit wider synchronization on ServerRegistry in following
     * methods. This is because even getters on instance implementation
     * are backed by other objects that can disappear asynchronously.
     * To avoid this we are doing this synchronization (remove is using registry).
     */

    private final String serverInstanceId;

    ServerInstance(String serverInstanceId) {
        assert serverInstanceId != null : "Server instance id is null"; // NOI18N
        this.serverInstanceId = serverInstanceId;
    }

    /**
     * Returns the display name of this instance.
     *
     * @return the display name of this instance
     * @throws InstanceRemovedException if the instance is not available anymore
     */
    public String getDisplayName() throws InstanceRemovedException {
        final ServerRegistry registry = ServerRegistry.getInstance();
        // see comment at the beginning of the class
        synchronized (registry) {
            org.netbeans.modules.j2ee.deployment.impl.ServerInstance inst =
                    getInstanceFromRegistry(registry);
            return inst.getDisplayName();
        }
    }

    /**
     * Returns the display name of the server.
     *
     * @return the display name of the server
     * @throws InstanceRemovedException if the instance is not available anymore
     */
    public String getServerDisplayName() throws InstanceRemovedException {
        final ServerRegistry registry = ServerRegistry.getInstance();
        // see comment at the beginning of the class
        synchronized (registry) {
            org.netbeans.modules.j2ee.deployment.impl.ServerInstance inst = getInstanceFromRegistry(registry);
            return inst.getServer().getDisplayName();
        }
    }

    /**
     * Returns the ID of the server associated with this instance.
     *
     * @return the ID of the server associated with this instance
     * @throws InstanceRemovedException if the instance is not available anymore
     */
    public String getServerID() throws InstanceRemovedException {
        final ServerRegistry registry = ServerRegistry.getInstance();
        // see comment at the beginning of the class
        synchronized (registry) {
            org.netbeans.modules.j2ee.deployment.impl.ServerInstance inst = getInstanceFromRegistry(registry);
            return inst.getServer().getShortName();
        }
    }

    /**
     * Returns <code>true</code> if this instance is running, <code>false</code>
     * otherwise.
     *
     * @return <code>true</code> if this instance is running, <code>false</code>
     *             otherwise
     * @throws InstanceRemovedException if the instance is not available anymore
     */
    public boolean isRunning() throws InstanceRemovedException {
        final org.netbeans.modules.j2ee.deployment.impl.ServerInstance inst =
                getInstanceFromRegistry(ServerRegistry.getInstance());

        return (inst.isReallyRunning() || inst.isSuspended());
    }

    /**
     * Returns the platform for this instance.
     *
     * @return the platform for this instance
     * @throws InstanceRemovedException if the instance is not available anymore
     */
    public J2eePlatform getJ2eePlatform() throws InstanceRemovedException {
         return J2eePlatform.create(getInstanceFromRegistry(ServerRegistry.getInstance()));
    }

    /**
     * Returns <code>true</code> when the deploy on save is supported by the
     * server for the given module.
     *
     * @param module module representing the application
     * @return <code>true</code> when the deploy on save is supported by the
     *             server for the given module
     * @throws InstanceRemovedException if the instance is not available anymore
     * @since 1.49
     */
    public boolean isDeployOnSaveSupported(J2eeModule module) throws InstanceRemovedException {
        return isDeployOnSaveSupported();
    }

    /**
     * Returns <code>true</code> when the deploy on save is supported by the
     * server.
     *
     * @return <code>true</code> when the deploy on save is supported by the
     *             server
     * @throws InstanceRemovedException if the instance is not available anymore
     * @since 1.53
     */
    public boolean isDeployOnSaveSupported() throws InstanceRemovedException {
        final ServerRegistry registry = ServerRegistry.getInstance();
        org.netbeans.modules.j2ee.deployment.impl.ServerInstance inst = getInstanceFromRegistry(registry);
        try {
            IncrementalDeployment incremental = inst.getServer().getOptionalFactory()
                    .getIncrementalDeployment(inst.getDisconnectedDeploymentManager());
            return incremental != null && incremental.isDeployOnSaveSupported();
        } catch (DeploymentManagerCreationException ex) {
            // this would throw InstanceRemovedException
            inst = getInstanceFromRegistry(registry);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns <code>true</code> when debugging is supported by the server.
     *
     * @return <code>true</code> when debugging is supported by the server
     * @throws InstanceRemovedException if the instance is not available anymore
     * @since 1.103
     */
    public boolean isDebuggingSupported() throws InstanceRemovedException {
        return getInstanceFromRegistry(ServerRegistry.getInstance()).isDebugSupported();
    }

    /**
     * Returns <code>true</code> when profiling is supported by the server.
     *
     * @return <code>true</code> when profiling is supported by the server
     * @throws InstanceRemovedException if the instance is not available anymore
     * @since 1.103
     */
    public boolean isProfilingSupported() throws InstanceRemovedException {
        return getInstanceFromRegistry(ServerRegistry.getInstance()).isProfileSupported();
    }

    /**
     * Returns descriptor providing extra information about the instance. May
     * return <code>null</code> if the server does not support this.
     *
     * @return descriptor providing extra information about the instance or <code>null</code>
     * @throws InstanceRemovedException if the instance is not available anymore
     * @since 1.46
     */
    public Descriptor getDescriptor() throws InstanceRemovedException {
        if (getInstanceFromRegistry(ServerRegistry.getInstance()).getServerInstanceDescriptor() != null) {
            return new Descriptor();
        }
        return null;
    }

    /**
     * Returns manager providing the access to server libraries. May
     * return <code>null</code> if the server does not support this.
     *
     * @return manager providing the access to server libraries or <code>null</code>
     * @throws InstanceRemovedException if the instance is not available anymore
     * @since 1.68
     */
    public LibraryManager getLibraryManager() throws InstanceRemovedException {
        final ServerRegistry registry = ServerRegistry.getInstance();

        org.netbeans.modules.j2ee.deployment.impl.ServerInstance inst = getInstanceFromRegistry(registry);
        try {
            if (inst.isServerLibraryManagementSupported()) {
                return new LibraryManager();
            }
            return null;
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof DeploymentManagerCreationException) {
                // this would throws InstanceRemovedException
                inst = getInstanceFromRegistry(registry);
            }
            throw ex;
        }
    }

    private org.netbeans.modules.j2ee.deployment.impl.ServerInstance getInstanceFromRegistry(ServerRegistry registry)
            throws InstanceRemovedException {

        synchronized (registry) {
            org.netbeans.modules.j2ee.deployment.impl.ServerInstance inst =
                    registry.getServerInstance(serverInstanceId);
            if (inst == null) {
                throw new InstanceRemovedException(serverInstanceId);
            }
            return inst;
        }
    }

    /**
     * Descriptor providing extra (and optional) information about the server instance.
     * @since 1.46
     */
    public final class Descriptor {

        /**
         * Returns the HTTP port of the server.
         *
         * @return the HTTP port of the server
         * @throws InstanceRemovedException if the instance is not available anymore
         */
        public int getHttpPort() throws InstanceRemovedException {
            return getInstanceFromRegistry(ServerRegistry.getInstance()).getServerInstanceDescriptor().getHttpPort();
        }

        /**
         * Returns the hostname of the server. Returned name is usable to reach
         * the server from the computer where IDE runs.
         *
         * @return the hostname of the server
         * @throws InstanceRemovedException if the instance is not available anymore
         */
        public String getHostname() throws InstanceRemovedException {
            return getInstanceFromRegistry(ServerRegistry.getInstance()).getServerInstanceDescriptor().getHostname();
        }

        /**
         * Returns <code>true</code> if the server is installed locally,
         * <code>false</code> otherwise.
         *
         * @return <code>true</code> if the server is installed locally
         * @throws InstanceRemovedException if the instance is not available anymore
         */
        public boolean isLocal() throws InstanceRemovedException {
            return getInstanceFromRegistry(ServerRegistry.getInstance()).getServerInstanceDescriptor().isLocal();
        }
    }

    /**
     * The manager providing the access to server libraries.
     *
     * @since 1.68
     */
    public final class LibraryManager {

        /**
         * Returns the set of libraries the server has access to and can be deployed
         * on request (by call to {@link org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance.LibraryManager#deployLibraries(java.util.Set)}.
         *
         * @return the set of libraries which can be deployed on server
         */
        @NonNull
        public Set<ServerLibrary> getDeployableLibraries() throws InstanceRemovedException {
            return getInstanceFromRegistry(ServerRegistry.getInstance()).getDeployableLibraries();
        }

        /**
         * Returns the set of libraries already deployed to the server.
         *
         * @return the set of libraries already deployed to the server
         */
        @NonNull
        public Set<ServerLibrary> getDeployedLibraries() throws InstanceRemovedException {
            return getInstanceFromRegistry(ServerRegistry.getInstance()).getDeployedLibraries();
        }

        @NonNull
        public Set<ServerLibraryDependency> getMissingDependencies(
                @NonNull Set<ServerLibraryDependency> libraries) throws InstanceRemovedException {
            return getInstanceFromRegistry(ServerRegistry.getInstance()).getMissingDependencies(libraries);
        }

        @NonNull
        public Set<ServerLibraryDependency> getDeployableDependencies(
                @NonNull Set<ServerLibraryDependency> libraries) throws InstanceRemovedException {
            return getInstanceFromRegistry(ServerRegistry.getInstance()).getDeployableDependencies(libraries);
        }

        public void deployLibraries(@NonNull Set<ServerLibraryDependency> libraries) throws InstanceRemovedException, ConfigurationException {
            getInstanceFromRegistry(ServerRegistry.getInstance()).deployLibraries(libraries);
        }
    }
}
