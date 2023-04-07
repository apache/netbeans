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
package org.netbeans.core.network.utils;

import com.sun.jna.Platform;

/**
 * Utility functions for finding the local computer's name. 
 * 
 *
 * @author lbruun
 */
public class HostnameUtils {

    private HostnameUtils() {}
    
    
    /**
     * Gets the name which is likely to be the local host's primary
     * name on the network.
     * 
     * <p>
     * IMPLEMENTATION: 
     * <ul>
     *   <li>On Unix-like OSes (incl Mac OS X) this is the value as returned from
     *       the {@code gethostname()} function from the standard C Library. </li>
     *   <li>On Windows it is the value as returned from the
     *       {@code gethostname()} function from {@code Ws2_32} library.
     *       (without domain name). Note that this Windows function will do a 
     *       name service lookup and the method is therefore potentially blocking, 
     *       although it is more than likely that Windows has cached this result 
     *       on computer startup in its DNS Client Cache and therefore the 
     *       result will be returned very fast.</li>
     * </ul>
     * 
     * @return host name
     * @throws NativeException if there was an error executing the
     *    system call.
     */
    public static String getNetworkHostname() throws NativeException {
        switch(Platform.getOSType()) {
            case Platform.WINDOWS:
                return org.netbeans.core.network.utils.hname.win.HostnameUtilsWin.getHostName(true);
            case Platform.MAC:
                return org.netbeans.core.network.utils.hname.mac.HostnameUtilsMac.cLibGetHostname();
            case Platform.LINUX:
                return org.netbeans.core.network.utils.hname.linux.HostnameUtilsLinux.cLibGetHostname();
            case Platform.SOLARIS:
                return org.netbeans.core.network.utils.hname.solaris.HostnameUtilsSolaris.cLibGetHostname();
            default:
                return org.netbeans.core.network.utils.hname.unix.HostnameUtilsUnix.cLibGetHostname();
        }
    }

}
