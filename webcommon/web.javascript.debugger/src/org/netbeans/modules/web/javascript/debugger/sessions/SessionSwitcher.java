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
package org.netbeans.modules.web.javascript.debugger.sessions;

import java.util.List;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.spi.debugger.ContextProvider;

/**
 * Sets the current session when the debugger is paused.
 * 
 * @author Martin Entlicher
 */
@LazyActionsManagerListener.Registration(path="javascript-debuggerengine")
public class SessionSwitcher extends LazyActionsManagerListener implements Debugger.Listener {
    
    private Debugger d;
    private Session s;
    
    public SessionSwitcher(ContextProvider lookupProvider) {
        d = lookupProvider.lookupFirst(null, Debugger.class);
        s = lookupProvider.lookupFirst(null, Session.class);
        d.addListener(this);
    }

    @Override
    public void paused(List<CallFrame> callStack, String reason) {
        DebuggerManager.getDebuggerManager().setCurrentSession(s);
    }

    @Override
    public void resumed() {}

    @Override
    public void reset() {}

    @Override
    public void enabled(boolean enabled) {}
    
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
