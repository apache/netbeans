/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
    private final static ConcurrentMap<Session, SessionProgress> INSTANCE = new ConcurrentHashMap<>();
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
