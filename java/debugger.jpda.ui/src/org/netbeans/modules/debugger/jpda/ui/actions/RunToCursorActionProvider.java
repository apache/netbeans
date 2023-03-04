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
package org.netbeans.modules.debugger.jpda.ui.actions;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.actions.JPDADebuggerActionProvider;
import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.spi.debugger.ActionsProvider;


/**
 *
 * @author  Jan Jancura
 */
@ActionsProvider.Registration(path="netbeans-JPDASession", actions="runToCursor")
public class RunToCursorActionProvider extends JPDADebuggerActionProvider
                                       implements PropertyChangeListener,
                                                  ActionsManagerListener {

    private Session session;
    private LineBreakpoint breakpoint;
    private ActionsManager lastActionsManager;
    
    
    public RunToCursorActionProvider (ContextProvider lookupProvider) {
        super((JPDADebuggerImpl) lookupProvider.lookupFirst(null, JPDADebugger.class));
        session = lookupProvider.lookupFirst(null, Session.class);
        EditorContextBridge.getContext().addPropertyChangeListener (this);
    }
    
    private void destroy () {
        debugger.removePropertyChangeListener (JPDADebugger.PROP_STATE, this);
        EditorContextBridge.getContext().removePropertyChangeListener (this);
    }
    
    static ActionsManager getCurrentActionsManager () {
        return DebuggerManager.getDebuggerManager ().
            getCurrentEngine () == null ? 
            DebuggerManager.getDebuggerManager ().getActionsManager () :
            DebuggerManager.getDebuggerManager ().getCurrentEngine ().
                getActionsManager ();
    }
    
    private ActionsManager getActionsManager () {
        ActionsManager current = getCurrentActionsManager();
        if (current != lastActionsManager) {
            if (lastActionsManager != null) {
                lastActionsManager.removeActionsManagerListener(
                        ActionsManagerListener.PROP_ACTION_STATE_CHANGED, this);
            }
            current.addActionsManagerListener(
                    ActionsManagerListener.PROP_ACTION_STATE_CHANGED, this);
            lastActionsManager = current;
        }
        return current;
    }

    @Override
    protected void checkEnabled(int debuggerState) {
        setEnabled (
            ActionsManager.ACTION_RUN_TO_CURSOR,
            getActionsManager().isEnabled(ActionsManager.ACTION_CONTINUE) &&
            (debuggerState== JPDADebugger.STATE_STOPPED) &&
            (EditorContextBridge.getContext().getCurrentLineNumber () >= 0) && 
            (EditorContextBridge.getContext().getCurrentURL ().endsWith (".java") || 
             EditorContextBridge.getContext().getCurrentURL ().endsWith (".scala"))
        );
        if (debuggerState != JPDADebugger.STATE_RUNNING && breakpoint != null) {
            DebuggerManager.getDebuggerManager ().removeBreakpoint (breakpoint);
            breakpoint = null;
        }
        if (debuggerState == JPDADebugger.STATE_DISCONNECTED) {
            destroy ();
        }
    }
    
    @Override
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_RUN_TO_CURSOR);
    }
    
    @Override
    public void doAction (Object action) {
        runToCursor();
    }
    
    @Override
    public void postAction(Object action, final Runnable actionPerformedNotifier) {
        doLazyAction(action, new Runnable() {
            @Override
            public void run() {
                try {
                    runToCursor();
                } finally {
                    actionPerformedNotifier.run();
                }
            }
        });
    }
    
    public void runToCursor() {
        if (breakpoint != null) {
            DebuggerManager.getDebuggerManager ().removeBreakpoint (breakpoint);
            breakpoint = null;
        }
        breakpoint = LineBreakpoint.create (
            EditorContextBridge.getContext().getCurrentURL (),
            EditorContextBridge.getContext().getCurrentLineNumber ()
        );
        breakpoint.setHidden (true);
        JPDAThread currentThread = debugger.getCurrentThread();
        if (currentThread != null) {
            breakpoint.setThreadFilters(debugger, new JPDAThread[] { currentThread });
        }
        breakpoint.setSession(debugger);
        DebuggerManager.getDebuggerManager ().addBreakpoint (breakpoint);
        if (currentThread != null) {
            currentThread.resume();
        } else {
            session.getEngineForLanguage ("Java").getActionsManager ().doAction (
                ActionsManager.ACTION_CONTINUE
            );
        }
    }

    @Override
    public void actionPerformed(Object action) {
        // Is never called
    }

    /** Sync up with continue action state. */
    @Override
    public void actionStateChanged(Object action, boolean enabled) {
        if (ActionsManager.ACTION_CONTINUE == action) {
            setEnabled (
                ActionsManager.ACTION_RUN_TO_CURSOR,
                enabled &&
                (debugger.getState () == JPDADebugger.STATE_STOPPED) &&
                (EditorContextBridge.getContext().getCurrentLineNumber () >= 0) && 
                (EditorContextBridge.getContext().getCurrentURL ().endsWith (".java") ||
                 EditorContextBridge.getContext().getCurrentURL ().endsWith (".scala"))
            );
        }
    }
}
