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
package org.netbeans.modules.java.lsp.server.debugging.launch;

import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.nativeimage.api.debug.NIDebugger;

/**
 *
 * @author martin
 */
public final class NbDebugSession {

    private final Session session;
    private volatile NIDebugger niDebugger;

    public NbDebugSession(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    public JPDADebugger getJPDADebugger() {
        return session.lookupFirst(null, JPDADebugger.class);
    }

    public NIDebugger getNIDebugger() {
        return niDebugger;
    }

    public void setNIDebugger(NIDebugger niDebugger) {
        assert this.niDebugger == null;
        this.niDebugger = niDebugger;
    }

    public void detach() {
        terminate(); // NetBeans takes care about not killing the debuggee when attached.
    }

    public void terminate() {
        session.kill();
    }

    public void setExceptionBreakpoints(boolean notifyCaught, boolean notifyUncaught) {
        throw new UnsupportedOperationException("Not supported.");
    }
}
