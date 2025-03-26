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

package org.netbeans.modules.javascript.cdtdebug.ui.callstack;

import java.awt.Image;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.api.debugger.Session;
import org.netbeans.lib.chrome_devtools_protocol.debugger.CallFrame;
import org.netbeans.modules.javascript.cdtdebug.CDTDebugger;
import org.netbeans.modules.javascript.cdtdebug.CDTDebuggerSessionProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.DebuggingView;

@DebuggingView.DVSupport.Registration(path=CDTDebuggerSessionProvider.SESSION_NAME)
public class DebuggingViewSupportImpl extends DebuggingView.DVSupport {

    private final Session session;
    private final CDTDebugger dbg;
    private final JSThread defaultThread;

    public DebuggingViewSupportImpl(ContextProvider lookupProvider) {
        session = lookupProvider.lookupFirst(null, Session.class);
        dbg = lookupProvider.lookupFirst(null, CDTDebugger.class);
        defaultThread = new JSThread(dbg, this);
        CDTDebugger.Listener chl = new ChangeListener();
        dbg.addListener(chl);
    }

    @Override
    public STATE getState() {
        if (dbg.isFinished()) {
            return STATE.DISCONNECTED;
        } else {
            return STATE.RUNNING;
        }
    }

    @Override
    public List<DebuggingView.DVThread> getAllThreads() {
        return Collections.<DebuggingView.DVThread>singletonList(defaultThread);
    }

    @Override
    public DebuggingView.DVThread getCurrentThread() {
        return defaultThread;
    }

    @Override
    public String getDisplayName(DebuggingView.DVThread thread) {
        return thread.getName();
    }

    @Override
    public Image getIcon(DebuggingView.DVThread thread) {
        return null;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public void resume() {
        dbg.resume();
    }

    @Override
    public Set<DebuggingView.Deadlock> getDeadlocks() {
        return null;
    }

    @Override
    protected List<DebuggingView.DVFilter> getFilters() {
        return Collections.EMPTY_LIST;
    }

    private class ChangeListener implements CDTDebugger.Listener {

        public ChangeListener() {}

        @Override
        public void notifySuspended(boolean suspended) {
        }

        @Override
        public void notifyCurrentFrame(CallFrame cf) {
        }

        @Override
        public void notifyFinished() {
            firePropertyChange(PROP_STATE, null, STATE.DISCONNECTED);
        }

    }

}
