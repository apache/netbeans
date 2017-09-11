/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun Microsystems, Inc. All
 * Rights Reserved.
 *  
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import org.netbeans.installer.utils.helper.ErrorLevel;

/**
 * @author Kirill Sorokin
 * @author Dmitry Lipin
 */
class NetworkUtils {
    public static String getHostName() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            if (hostName != null) {
                return hostName;
            }
        } catch (UnknownHostException e) {
            LogManager.log(ErrorLevel.MESSAGE, e);
        }
        
        return "localhost"; //NOI18N
    }
    public static boolean isPortAvailable(int port, int... forbiddenPorts) {
        // check whether the port is in the restricted list, if it is, there is no
        // sense to check whether it is physically available
        for (int forbidden: forbiddenPorts) {
            if (port == forbidden) {
                return false;
            }
        }
        
        // if the port is not in the allowed range - return false
        if ((port < 0) && (port > 65535)) {
            return false;
        }
        
        // if the port is not in the restricted list, we'll try to open a server
        // socket on it, if we fail, then someone is already listening on this port
        // and it is occupied
        synchronized (Integer.toString(port).intern()) {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(port);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    ErrorManager.notifyError(
                            "Could not close server socket on port " + port,
                            e);
                }
            }
        }
        }
    }
    
    public static int getAvailablePort(int basePort, int... forbiddenPorts) {
        // increment the port value until we find an available port or stumble into
        // the upper bound
        int port = basePort;
        while ((port < 65535) && !isPortAvailable(port, forbiddenPorts)) {
            port++;
        }
        
        if (port == 65535) {
            port = 0;
            while ((port < basePort) && !isPortAvailable(port, forbiddenPorts)) {
                port++;
            }
            
            if (port == basePort) {
                return -1;
            } else {
                return port;
            }
        } else {
            return port;
        }
    }
}
