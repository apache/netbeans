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
package org.netbeans.core.network.utils.hname.unix;

import com.sun.jna.Native;
import org.netbeans.core.network.utils.NativeException;

/**
 * Host name utilities for "general" Unix. 
 */
public class HostnameUtilsUnix {
    
    /**
     * Max length of a Unix hostname including 
     * null terminator.
     */
    private static final int MAXHOSTNAMELEN = 256;   
    
    
    /**
     * Returns the result of {@code gethostname()} function 
     * from the standard Unix/Linux C Library.
     * 
     * @return host name 
     * @throws NativeException if there was an error executing the
     *    system call.
     */
    public static String cLibGetHostname() throws NativeException {
        byte[] buf = new byte[MAXHOSTNAMELEN];
        int retCode = CLib.INSTANCE.gethostname(buf, buf.length);
        
        if (retCode == 0) {
            return Native.toString(buf);
        }
        throw new NativeException(retCode, "error calling 'gethostname()' function");
    }
    
}
