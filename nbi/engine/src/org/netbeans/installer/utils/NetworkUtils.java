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
