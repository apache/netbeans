/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.odcs.cnd.execution;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.odcs.api.ODCSManager;
import org.netbeans.modules.odcs.api.ODCSServer;
import org.netbeans.modules.odcs.client.api.ODCSClient;
import org.netbeans.modules.odcs.cnd.json.VMDescriptor;
import org.openide.util.Mutex;

// Thread safe
public class DevelopVMExecutionEnvironmentImpl extends DevelopVMExecutionEnvironment {

    private static String DEFAULT_IP = "0.0.0.0";

    private final Mutex mutex = new Mutex();

    // Immutable data
    private final String user;
    private final String machineId;
    private final String serverUrl;

    private String displayName;
    private String ip = DEFAULT_IP;
    private int port = 0;

    DevelopVMExecutionEnvironmentImpl(String user, String machineId, int port, String serverUrl, String displayName) {
        this.serverUrl = serverUrl;
        this.user = user;
        this.machineId = machineId;
        this.port = port;

        mutex.writeAccess(() -> {
            this.displayName = displayName;
        });
    }

    DevelopVMExecutionEnvironmentImpl(String user, String machineId, int port, String serverUrl) {
        this(user, machineId, port, serverUrl, encode(user, machineId, port, serverUrl));
    }

    @Override
    public String getHost() {
        return mutex.readAccess(() -> {
            return ip;
        });
    }

    @Override
    public String getHostAddress() {
        return mutex.readAccess(() -> {
            return ip;
        });
    }

    @Override
    public String getDisplayName() {
        return mutex.readAccess(() -> {
            return displayName;
        });
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public int getSSHPort() {
        return mutex.readAccess(() -> {
            return port;
        });
    }

    @Override
    public boolean isRemote() {
        return true;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public void prepareForConnection() throws IOException, ConnectionManager.CancellationException {
        CndUtils.assertNonUiThread();
        VMDescriptor vmDescriptor = new DevelopVMExecutionClient(this).getVMDescriptor();

        if (vmDescriptor == null) {
            throw new ConnectionManager.CancellationException("Cancelling connection: Oracle DCS server is not connected");
        }

        mutex.writeAccess(() -> {
            this.ip = vmDescriptor.getHostname();
            // this.port // Math.toIntExact(vmDescriptor.getPort());
            this.displayName = user + "@" + vmDescriptor.getDisplayName();
        });
    }

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public String getMachineId() {
        return machineId;
    }

    @Override
    public void initializeOrWait() {
        try {
            LOG.fine(() -> "Fetching data for cloud execution environment " + this.getDisplayName());
            prepareForConnection();
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Error initializing cloud execution environment", ex);
        } catch (ConnectionManager.CancellationException ex) {
            // ignore
        }
    }

    private static final Logger LOG = Logger.getLogger(DevelopVMExecutionEnvironmentImpl.class.getName());
}
