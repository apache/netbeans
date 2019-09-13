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
package org.netbeans.core.network.proxy.windows;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author lfischme
 */
public interface WindowsNetworkProxyLibrary extends Library {
    WindowsNetworkProxyLibrary LIBRARY = Native.loadLibrary("winhttp.dll", WindowsNetworkProxyLibrary.class);
    
    public class ProxyConfig extends Structure {

        @Override
        protected List getFieldOrder() {
            return Arrays.asList( new String[] {
                "autoDetect",
                "pacFile",
                "proxy",
                "proxyBypass"
            } );
        }

        public static class ByReference extends ProxyConfig implements Structure.ByReference { }
        
        public boolean autoDetect;
        public Pointer pacFile;
        public Pointer proxy;
        public Pointer proxyBypass;        


    } 
    
    public boolean WinHttpGetIEProxyConfigForCurrentUser(
            ProxyConfig proxyConfig
            );
}
