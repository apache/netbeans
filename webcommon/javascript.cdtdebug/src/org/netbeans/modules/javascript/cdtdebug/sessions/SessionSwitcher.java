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
package org.netbeans.modules.javascript.cdtdebug.sessions;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.lib.chrome_devtools_protocol.debugger.CallFrame;
import org.netbeans.modules.javascript.cdtdebug.CDTDebugger;
import org.netbeans.modules.javascript.cdtdebug.CDTDebuggerEngineProvider;
import org.netbeans.spi.debugger.ContextProvider;

/**
 * Sets the current session when the debugger is paused.
 */
@LazyActionsManagerListener.Registration(path=CDTDebuggerEngineProvider.ENGINE_NAME)
public class SessionSwitcher extends LazyActionsManagerListener implements CDTDebugger.Listener {

    private CDTDebugger d;
    private Session s;

    @SuppressWarnings("LeakingThisInConstructor")
    public SessionSwitcher(ContextProvider lookupProvider) {
        d = lookupProvider.lookupFirst(null, CDTDebugger.class);
        s = lookupProvider.lookupFirst(null, Session.class);
        d.addListener(this);
    }

    @Override
    public void notifySuspended(boolean suspended) {
        if (suspended) {
            DebuggerManager.getDebuggerManager().setCurrentSession(s);
        }
    }

    @Override
    public void notifyCurrentFrame(CallFrame cf) {
    }

    @Override
    public void notifyFinished() {
        d.removeListener(this);
    }

    @Override
    protected void destroy() {
        d.removeListener(this);
        d = null;
        s = null;
    }

    @Override
    public String[] getProperties() {
        return new String[] {};
    }

}
