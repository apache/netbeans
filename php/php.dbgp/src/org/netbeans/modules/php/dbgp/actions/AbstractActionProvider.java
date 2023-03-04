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
package org.netbeans.modules.php.dbgp.actions;

import java.util.Set;

import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.dbgp.SessionManager;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;

/**
 * @author ads
 *
 */
public abstract class AbstractActionProvider extends ActionsProviderSupport {
    private ContextProvider myContextProvider;

    AbstractActionProvider(ContextProvider contextProvider) {
        myContextProvider = contextProvider;
    }

    public void setEnabled(boolean enabled) {
        Set set = getActions();
        for (Object object : set) {
            setEnabled(object, enabled);
        }
    }

    protected DebugSession getSession() {
        SessionId id = getSessionId();
        if (id == null) {
            return null;
        }
        return SessionManager.getInstance().getSession(id);
    }

    protected SessionId getSessionId() {
        return (SessionId) getContextProvider().lookupFirst(null, SessionId.class);
    }

    protected ContextProvider getContextProvider() {
        return myContextProvider;
    }

    protected void hideSuspendAnnotations() {
        DebugSession session = getSession();
        if (session != null) {
            session.getBridge().hideAnnotations();
        }
    }

}
