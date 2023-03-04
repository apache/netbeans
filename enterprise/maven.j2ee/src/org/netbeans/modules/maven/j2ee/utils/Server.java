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

import java.util.Objects;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.maven.j2ee.execution.ExecutionChecker;
import static org.netbeans.modules.maven.j2ee.utils.Bundle.*;
import org.openide.util.NbBundle.Messages;

/**
 * Server representation. It contains two important information:
 * <ul>
 *  <li>Server ID</li> --> general identifier for a certain application server (e.g. gfv3ee6 for GlassFish V3)
 *  <li>Server instance ID</li> --> concrete identifier pointing directly to one server instance
 * (this is because user can have multiple instances of the same application server but with a different
 * versions installed on his/her computer)
 * </ul>
 *
 * @author Martin Janicek
 */
public final class Server implements Comparable<Server> {

    /**
     * Constant representing project without application server set.
     */
    public static final Server NO_SERVER_SELECTED = new Server();

    private final String serverInstanceId;
    private final String serverID;


    private Server() {
        this(ExecutionChecker.DEV_NULL, ExecutionChecker.DEV_NULL);
    }

    public Server(String serverInstanceId) {
        this(serverInstanceId, MavenProjectSupport.obtainServerID(serverInstanceId));
    }

    public Server(String serverInstanceID, String serverID) {
        this.serverInstanceId = serverInstanceID;
        this.serverID = serverID;
    }

    public String getServerInstanceID() {
        return serverInstanceId;
    }

    public String getServerID() {
        return serverID;
    }

    @Override
    public int compareTo(Server wrapper) {
        // <No Server> option should be always the last one
        if (ExecutionChecker.DEV_NULL.equals(this.serverInstanceId)) {
            return 1;
        }

        // If one server is an GF instance and the second one is not, always return GF
        if (this.serverInstanceId.contains("gf") && !wrapper.serverInstanceId.contains("gf")) { //NOI18N
            return -1;
        }
        if (!this.serverInstanceId.contains("gf") && wrapper.serverInstanceId.contains("gf")) { //NOI18N
            return 1;
        }

        // Otherwise compare just by String name
        String displayName = this.toString();
        String displayName2 = wrapper != null ? wrapper.toString() : "";

        displayName = displayName != null ? displayName : "";
        displayName2 = displayName2 != null ? displayName2 : "";
        
        return displayName.compareTo(displayName2);
    }

    @Messages({
        "MSG_Invalid_Server=<Invalid Server>",
        "MSG_No_Server=<No Server Selected>"
    })
    @Override
    public String toString() {
        if (serverInstanceId == null || ExecutionChecker.DEV_NULL.equals(serverInstanceId)) {
            return MSG_No_Server();
        }

        ServerInstance si = Deployment.getDefault().getServerInstance(serverInstanceId);
        if (si != null) {
            try {
                return si.getDisplayName();
            } catch (InstanceRemovedException ex) {
                return MSG_Invalid_Server();
            }
        }
        return serverInstanceId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Server other = (Server) obj;
        if (!Objects.equals(this.serverInstanceId, other.serverInstanceId)) {
            return false;
        }
        if (!Objects.equals(this.serverID, other.serverID)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.serverInstanceId);
        hash = 59 * hash + Objects.hashCode(this.serverID);
        return hash;
    }
}
