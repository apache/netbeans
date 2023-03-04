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

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;

/**
 *
 * @author rmatous
 */
public final class ConversionUtils {

    private ConversionUtils() {
    }

    private static DebugSession toDebugSession(DebuggerEngine debuggerEngine) {
        return (debuggerEngine != null) ? debuggerEngine.lookupFirst(null, DebugSession.class) : null;
    }

    public static DebugSession toDebugSession(SessionId id) {
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        DebugSession currentSession = toDebugSession(currentEngine);
        if (currentSession != null && id.equals(currentSession.getSessionId())) {
            return currentSession;
        }
        DebuggerEngine[] engines = DebuggerManager.getDebuggerManager().getDebuggerEngines();
        for (DebuggerEngine debuggerEngine : engines) {
            DebugSession debugSession = toDebugSession(debuggerEngine);
            if (debugSession != null && id.equals(debugSession.getSessionId())) {
                return debugSession;
            }
        }
        return null;
    }

    public static DebugSession toDebugSession(Session session) {
        return toDebugSession(session.getCurrentEngine());
    }

    public static Session toSession(DebugSession debugSession) {
        return toSession(debugSession.getSessionId());
    }

    public static Session toSession(SessionId sessionId) {
        Session currentSession = DebuggerManager.getDebuggerManager().getCurrentSession();
        DebugSession currentDebugSession = toDebugSession(currentSession);
        if (currentDebugSession != null && sessionId.equals(currentDebugSession.getSessionId())) {
            return currentSession;
        }
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        for (Session session : sessions) {
            DebugSession debugSession = toDebugSession(session);
            if (debugSession != null && sessionId.equals(debugSession.getSessionId())) {
                return session;
            }
        }
        return null;
    }

}
