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
