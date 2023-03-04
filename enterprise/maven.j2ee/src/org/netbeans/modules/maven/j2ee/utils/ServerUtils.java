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

package org.netbeans.modules.maven.j2ee.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;

/**
 *
 * @author Martin Janicek
 */
public final class ServerUtils {

    private ServerUtils() {
    }


    /**
     * Sets {@link Server} for the given {@link Project}.
     */
    public static void setServer(Project project, Server server) {
        if (server == Server.NO_SERVER_SELECTED) {
            MavenProjectSupport.setServerID(project, null);
            JavaEEProjectSettings.setServerInstanceID(project, null);
        } else {
            MavenProjectSupport.setServerID(project, server.getServerID());
            JavaEEProjectSettings.setServerInstanceID(project, server.getServerInstanceID());
        }
    }

    /**
     * Finds {@link Server} assigned to the given {@link Project}.
     *
     * @param project
     * @return corresponding server
     */
    public static Server findServer(Project project) {
        final Type moduleType = getModuleType(project);
        String instanceID;
        try {
            // Should not happen but obviously it happens from time to time --> #242399
            instanceID = JavaEEProjectSettings.getServerInstanceID(project);
        } catch (UnsupportedOperationException exception) {
            instanceID = null;
        }
        if (instanceID != null) {
            Server server = findServerByInstance(moduleType, instanceID);
            if (server != null) {
                return server;
            } else {
                return new Server(instanceID);
            }
        }

        // Try to read serverID directly from pom.xml properties configration
        final String serverID = MavenProjectSupport.readServerID(project);
        if (serverID != null) {
            Server server = findServerByType(moduleType, serverID);
            if (server != null) {
                return server;
            } else {
                return new Server(null, serverID);
            }
        }

        return Server.NO_SERVER_SELECTED;
    }
    
    private static Type getModuleType(Project project) {
        J2eeModuleProvider moduleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (moduleProvider != null && moduleProvider.getJ2eeModule() != null) {
            return moduleProvider.getJ2eeModule().getType();
        }
        return null;
    }

    private static Server findServerByInstance(Type moduleType, String instanceId) {
        for (Server server : findServersFor(moduleType)) {
            if (instanceId.equals(server.getServerInstanceID())) {
                return server;
            }
        }
        return null;
    }

    private static Server findServerByType(Type moduleType, String serverId) {
        for (Server server : findServersFor(moduleType)) {
            if (serverId.equals(server.getServerID())) {
                return server;
            }
        }
        return null;
    }

    /**
     * Finds all registered {@link Server}s that could be used for the given {@link Type}.
     *
     * For example when parameter is {@code Type.EJB} the method returns only servers
     * providing full Java EE specification (and e.g. Tomcat won't be present).
     *
     * @param moduleType
     * @return list of {@link Server}s that could be used for the given {@link Type}
     */
    public static List<Server> findServersFor(Type moduleType) {
        return convertToList(Deployment.getDefault().getServerInstanceIDs(Collections.singleton(moduleType)));
    }

    private static List<Server> convertToList(String[] serverInstanceIDs) {
        final List<Server> servers = new ArrayList<>();
        for (String instanceID : serverInstanceIDs) {
            servers.add(new Server(instanceID));
        }

        // Sort the server list
        Collections.sort(servers);

        // We want to provide Maven project without server
        servers.add(Server.NO_SERVER_SELECTED);

        return servers;
    }
}
