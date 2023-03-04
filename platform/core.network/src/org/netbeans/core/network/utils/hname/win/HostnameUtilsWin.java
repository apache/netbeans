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
package org.netbeans.core.network.utils.hname.win;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.Win32Exception;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.network.utils.IpAddressUtils;
import org.netbeans.core.network.utils.NativeException;

/**
 * Hostname utilities for Microsoft Windows OS.
 */
public class HostnameUtilsWin {
    
    private static final Logger LOGGER = Logger.getLogger(HostnameUtilsWin.class.getName());
    
    /**
     * Gets the computer name.
     * 
     * <p>This is the also known as the NetBIOS name, although NetBIOS is 
     * hardly used anymore. It is the same value as can be seen from the
     * {@code COMPUTERNAME} environment variable.
     * 
     * <p>
     * Windows API equivalent: {@code GetComputerName()} function from 
     * {@code Kernel32} library.
     * 
     * @return computer name
     * @throws NativeException if there was an error executing the
     *    system call.
     */
    public static String getComputerName() throws NativeException {
        try {
            return Kernel32Util.getComputerName();
        } catch (Win32Exception ex) {
            LOGGER.log(Level.FINE, "Kernel32.GetComputerName error : {0}", ex.getHR().intValue());
            String env = System.getenv("COMPUTERNAME");
            if (env != null) {
                return env;
            }
            throw new NativeException(ex.getHR().intValue(), "error calling 'GetComputerName()' function");
        }
    }

    /**
     * Gets the local host name.
     * 
     * <p>
     * The underlying Windows function may return a simple host name (e.g.
     * {@code chicago}) or it may return a fully qualified host name (e.g.
     * {@code chicago.us.internal.net}). The {@code noQualify} parameter can
     * remove the domain part if it exists.
     *
     * <p>
     * Note that the underlying Windows function will do a name service lookup
     * and the method is therefore potentially blocking, although it is more
     * than likely that Windows has cached this result in the DNS Client Cache
     * and therefore the result will be returned very fast.
     *
     * <p>
     * Windows API equivalent: {@code gethostname()} function from 
     * {@code Ws2_32} library.
     * 
     * @param noQualify if {@code true} the result is never qualified with domain,
     *   if {@code false} the result is <i>potentially</i> a fully qualified
     *   host name.
     * 
     * @return host name
     * @throws NativeException if there was an error executing the
     *    system call.
     */
    public static String getHostName(boolean noQualify) throws NativeException {
        
        byte[] buf = new byte[256];
        
        int returnCode = Winsock2Lib.INSTANCE.gethostname(buf, buf.length);
        if (returnCode == 0) {
            String result = Native.toString(buf);
            if (noQualify) {
                return IpAddressUtils.removeDomain(result);
            } else {
                return result;
            }
        } else {
            throw new NativeException(returnCode, "error calling 'gethostname()' function");
        }
    }
    

    
}
