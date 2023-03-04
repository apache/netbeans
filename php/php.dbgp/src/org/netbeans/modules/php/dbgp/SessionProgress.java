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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;

/**
 *
 * @author Radek Matous
 */
public final class SessionProgress implements Cancellable {
    private static final ConcurrentMap<Session, SessionProgress> INSTANCE = new ConcurrentHashMap<>();
    private final ProgressHandle h;
    private volatile Session session;
    private volatile boolean isFinished;
    private volatile boolean isStarted;

    public static SessionProgress forSession(Session session) {
        SessionProgress newval = new SessionProgress(session);
        SessionProgress retval = INSTANCE.putIfAbsent(session, newval);
        return (retval == null) ? newval : retval;
    }

    public static SessionProgress forSessionId(SessionId sessionId) {
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        Session retval = null;
        for (Session session : sessions) {
            SessionId id = (SessionId) session.lookupFirst(null, SessionId.class);
            if (id != null && id.getId().equals(sessionId.getId())) {
                retval = session;
                break;
            }
        }
        return (retval != null) ? forSession(retval) : null;
    }

    private SessionProgress(Session session) {
        this.session = session;
        String displayName = NbBundle.getMessage(SessionProgress.class, "LBL_Progress_Connecting", session.getName());
        h = ProgressHandle.createHandle(displayName, this);
    }

    @Override
    public boolean cancel() {
        finish();
        SessionManager.getInstance().stopSession(session);
        return true;
    }

    void start() {
        h.start();
        isStarted = true;
    }

    public void notifyConnectionFinished() {
        String displayName = NbBundle.getMessage(SessionProgress.class, "LBL_Progress_Suspend");
        h.setDisplayName(session.getName());
        h.suspend(displayName);
    }

    @org.netbeans.api.annotations.common.SuppressWarnings({"NN_NAKED_NOTIFY"})
    void finish() {
        if (isStarted && !isFinished) {
            isFinished = true;
            h.finish();
            SessionId id = (SessionId) session.lookupFirst(null, SessionId.class);
            if (id != null) {
                synchronized (id) {
                    id.cancel();
                    id.notifyAll();
                }
            }
        }
        INSTANCE.remove(session);
    }

}
