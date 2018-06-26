/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
