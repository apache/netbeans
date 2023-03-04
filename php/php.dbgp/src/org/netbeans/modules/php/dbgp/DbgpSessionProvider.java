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
package org.netbeans.modules.php.dbgp;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.SessionProvider;

/**
 * @author ads
 *
 */
public class DbgpSessionProvider extends SessionProvider {
    private ContextProvider myContextProvider;

    public DbgpSessionProvider(ContextProvider contextProvider) {
        myContextProvider = contextProvider;
    }

    @Override
    public String getLocationName() {
        return "LOCATION NAME";
    }

    @Override
    public Object[] getServices() {
        return new Object[0];
    }

    @Override
    public String getSessionName() {
        SessionId id = (SessionId) getContextProvider().lookupFirst(null, SessionId.class);
        return id.getId();
    }

    @Override
    public String getTypeID() {
        return DebuggerImpl.SESSION_ID;
    }

    private ContextProvider getContextProvider() {
        return myContextProvider;
    }

}
