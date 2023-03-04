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

package org.netbeans.modules.javascript.v8debug;

import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.SessionProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Entlicher
 */
@SessionProvider.Registration(path=V8DebuggerSessionProvider.DEBUG_INFO)
public class V8DebuggerSessionProvider extends SessionProvider {
    
    public static final String SESSION_NAME = "javascript-v8session";           // NOI18N
    static final String DEBUG_INFO = "javascript-v8debuginfo";                  // NOI18N
    
    private final ContextProvider context;
    
    public V8DebuggerSessionProvider(ContextProvider context) {
        this.context = context;
    }

    @NbBundle.Messages({"# {0} - host name", "# {1} - port number",
                        "CTL_V8RemoteAttach=Node.js at {0}:{1}",
                        "# {0} - port number",
                        "CTL_V8LocalAttach=Node.js at port {0}"})
    public static String getSessionName(@NullAllowed String host, int port) {
        if (host != null && !host.isEmpty()) {
            return Bundle.CTL_V8RemoteAttach(host, Integer.toString(port));
        } else {
            return Bundle.CTL_V8LocalAttach(Integer.toString(port));
        }
    }
    
    @Override
    public String getSessionName() {
        V8Debugger dbg = context.lookupFirst(null, V8Debugger.class);
        return getSessionName(dbg.getHost(), dbg.getPort());
    }

    @Override
    public String getLocationName() {
        return null;
    }

    @Override
    public String getTypeID() {
        return SESSION_NAME;
    }

    @Override
    public Object[] getServices() {
        return new Object[] {};
    }
}
