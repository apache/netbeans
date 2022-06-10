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

package org.netbeans.modules.javascript.cdtdebug;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.SessionProvider;
import org.openide.util.NbBundle;

@SessionProvider.Registration(path=CDTDebuggerSessionProvider.DEBUG_INFO)
public class CDTDebuggerSessionProvider extends SessionProvider {

    public static final String SESSION_NAME = "javascript-cdtsession"; // NOI18N
    static final String DEBUG_INFO = "javascript-cdtdebuginfo";        // NOI18N

    private final ContextProvider context;

    public CDTDebuggerSessionProvider(ContextProvider context) {
        this.context = context;
    }

    @NbBundle.Messages({
        "# {0} - Hostname",
        "# {1} - Port",
        "CTL_CDTRemoteAttach2=Chrome DevTools Endpoint at {0}:{1}"
    })
    public static String getSessionName(String hostname, int port) {
        return Bundle.CTL_CDTRemoteAttach2(hostname, port);
    }

    @NbBundle.Messages({
        "# {0} - WebSocket URL",
        "CTL_CDTRemoteAttach=Chrome DevTools Endpoint at {0}"
    })
    @Override
    public String getSessionName() {
        CDTDebugger dbg = context.lookupFirst(null, CDTDebugger.class);
        return Bundle.CTL_CDTRemoteAttach(dbg.getWebSocketDebuggerUrl().toASCIIString());
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
