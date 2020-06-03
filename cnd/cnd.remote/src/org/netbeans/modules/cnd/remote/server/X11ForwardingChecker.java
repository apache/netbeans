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

package org.netbeans.modules.cnd.remote.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;

/**
 * Checks whether X11 forwarding from remote host to local one is possible
 */
public class X11ForwardingChecker {

    private final ExecutionEnvironment execEnv;

    public X11ForwardingChecker(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;
    }

    /**
     * Performs check.
     * This is a SLOW method - never call iot from UI thread.
     * It supposes that the host is connected
     * @return
     */
    public boolean check() throws IOException, CancellationException {
        CndUtils.assertNonUiThread();
        if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            ConnectionManager.getInstance().connectTo(execEnv);
        }
        if (!check6000()) {
            return false;
        }
        if (!checkSshdConfig()) {
            return false;
        }
        return true;
    }

    private boolean checkSshdConfig() {
        final String x11key = "X11Forwarding"; // NOI18N
        ProcessUtils.ExitStatus rc = ProcessUtils.execute(execEnv, "grep", "-i", x11key, "/etc/ssh/sshd_config"); // NOI18N
        if (rc.isOK()) {
            String[] lines = rc.getOutputString().split("\n"); //NOI18N
            for (String line : lines) {
                if (line.startsWith(x11key)) {
                    String rest = line.substring(x11key.length()).trim();
                    if ("no".equalsIgnoreCase(rest)) { // NOI18N
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean check6000() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            int port = 6000;
            SocketAddress sockaddr = new InetSocketAddress(addr, port);
            Socket sock = new Socket();
            sock.setReuseAddress(true);
            sock.setSoTimeout(1000); // 1 second timeout
            sock.connect(sockaddr, 1000); // 1 second timeout            
            boolean result = sock.isConnected();
            sock.close();
            return result;
        } catch (UnknownHostException e) {
            // nothing
        } catch (IOException e) {
            // nothing
        }
        return false;
    }
}
