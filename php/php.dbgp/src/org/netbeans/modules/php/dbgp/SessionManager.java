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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.dbgp.breakpoints.AbstractBreakpoint;
import org.netbeans.modules.php.project.api.PhpProjectUtils;
import org.netbeans.modules.php.spi.executable.DebugStarter;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.openide.util.Cancellable;

/**
 * @author Radek Matous
 */
public class SessionManager {
    private static final String ID = "netbeans-PHP-DBGP-DebugInfo"; //NOI18N
    private static final ServerThread SERVER_THREAD;
    private static final SessionManager INSTANCE = new SessionManager();

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    static {
        SERVER_THREAD = new ServerThread();
    }

    public void startNewSession(Project project, Callable<Cancellable> run, DebugStarter.Properties properties) {
        assert properties.getStartFile() != null;
        SessionId sessionId = SessionManager.getSessionId(project);
        if (sessionId == null) {
            sessionId = new SessionId(properties.getStartFile(), project);
            DebuggerOptions options = new DebuggerOptions();
            options.debugForFirstPageOnly = properties.isCloseSession();
            options.pathMapping = properties.getPathMapping();
            options.debugProxy = properties.getDebugProxy();
            options.projectEncoding = properties.getEncoding();
            startSession(sessionId, options, run);
            long started = System.currentTimeMillis();
            if (!sessionId.isInitialized(true)) {
                ConnectionErrMessage.showMe(((int) (System.currentTimeMillis() - started) / 1000));
            }
        }
    }

    public boolean isAlreadyRunning() {
        return getPhpSession() != null;
    }

    public void stopCurrentSession(boolean wait) {
        Session session = getPhpSession();
        if (session != null) {
            DebugSession dbgSession = ConversionUtils.toDebugSession(session);
            SessionManager.getInstance().stopSession(session);
            if (wait) {
                dbgSession.waitFinished();
            }
        }
    }

    synchronized Session startSession(SessionId id, DebuggerOptions options, Callable<Cancellable> backendLauncher) {
        DebugSession dbgSession = new DebugSession(options, new BackendLauncher(backendLauncher));
        DebuggerInfo dInfo = DebuggerInfo.create(ID, new Object[]{id, dbgSession});
        DebuggerManager.getDebuggerManager().startDebugging(dInfo);
        for (Session session : DebuggerManager.getDebuggerManager().getSessions()) {
            DebugSession debugSession = session.lookupFirst(null, DebugSession.class);
            if (debugSession != null && debugSession == dbgSession) {
                dbgSession.setSession(session);
            }
        }
        Session session = dbgSession.getSession();
        if (session != null) {
            SERVER_THREAD.invokeLater();
        }
        return session;
    }

    public synchronized void stopSession(Session session) {
        SessionId id = session.lookupFirst(null, SessionId.class);
        DebugSession debSess = getSession(id);
        if (debSess != null) {
            debSess.stopSession();
        } else {
            stopEngines(session);
        }
    }

    public static void stopEngines(Session session) {
        String[] languages = session.getSupportedLanguages();
        for (String language : languages) {
            DebuggerEngine engine = session.getEngineForLanguage(language);
            ((DbgpEngineProvider) engine.lookupFirst(null, DebuggerEngineProvider.class)).getDestructor().killEngine();
        }
        SessionManager.closeServerThread(session);
        resetBreakpoints();
        // NETBEANS-5080 request cancellation to finish the task
        DebugSession debugSession = session.lookupFirst(null, DebugSession.class);
        if (debugSession != null) {
            debugSession.cancel();
        }
    }

    public static SessionId getSessionId(Project project) {
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        for (Session session : sessions) {
            SessionId sessionId = session.lookupFirst(null, SessionId.class);
            if (sessionId != null) {
                Project sessionProject = sessionId.getProject();
                if (project.equals(sessionProject)) {
                    return sessionId;
                }
            }
        }
        return null;
    }

    public List<DebugSession> findSessionsById(SessionId id) {
        return Collections.singletonList(getSession(id));
    }

    public DebugSession getSession(SessionId id) {
        return (id != null) ? ConversionUtils.toDebugSession(id) : null;
    }

    public static void closeServerThread(Session session) {
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        boolean last = true;
        for (Session sess : sessions) {
            if (sess.equals(session)) {
                continue;
            }
            if (sess.lookupFirst(null, SessionId.class) != null) {
                last = false;
            }
        }
        if (last) {
            SERVER_THREAD.cancel();
        }
    }

    private Session getPhpSession() {
        DebuggerManager manager = DebuggerManager.getDebuggerManager();
        Session currentSession = manager.getCurrentSession();
        Session retval = currentSession != null ? getPhpSession(new Session[]{currentSession}) : null;
        return retval != null ? retval : getPhpSession(manager.getSessions());
    }

    private Session getPhpSession(Session[] sessions) {
        for (Session session : sessions) {
            SessionId sessionId = session.lookupFirst(null, SessionId.class);
            if (sessionId != null) {
                Project sessionProject = sessionId.getProject();
                if (sessionProject != null && PhpProjectUtils.isPhpProject(sessionProject)) {
                    return session;
                }
            }
        }
        return null;
    }

    private static void resetBreakpoints() {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (Breakpoint breakpoint : breakpoints) {
            if (!(breakpoint instanceof AbstractBreakpoint)) {
                continue;
            }
            AbstractBreakpoint brkpnt = (AbstractBreakpoint) breakpoint;
            brkpnt.reset();
        }
    }

}
