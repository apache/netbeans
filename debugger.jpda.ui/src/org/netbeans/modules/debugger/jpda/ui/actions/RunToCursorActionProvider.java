/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
