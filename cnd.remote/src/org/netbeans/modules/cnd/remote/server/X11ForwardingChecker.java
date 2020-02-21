/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
