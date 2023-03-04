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

package org.netbeans.modules.web.javascript.debugger;

import static org.netbeans.modules.web.javascript.debugger.DebuggerConstants.SESSION;
import static org.netbeans.modules.web.javascript.debugger.DebuggerConstants.SESSION_LOCATION_NAME;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.SessionProvider;

/**
 *
 * @author Sandip V. Chitale <sandipchitale@netbeans.org>
 */
@SessionProvider.Registration(path="javascript-debuggerinfo")
public class SessionProviderImpl extends SessionProvider {
	
    private WebKitDebugging webkit;

    public SessionProviderImpl(ContextProvider contextProvider) {
        webkit = contextProvider.lookupFirst(null, WebKitDebugging.class);
    }

    @Override
    public String getSessionName() {
        String name = webkit.getConnectionName();
        if (name == null) {
            name = "...";
        }
        return name;
    }

    @Override
    public String getLocationName() {
        return SESSION_LOCATION_NAME;
    }

    @Override
    public String getTypeID() {
        return SESSION;
    }

    @Override
    public Object[] getServices() {
        return new Object[0];
    }

}
