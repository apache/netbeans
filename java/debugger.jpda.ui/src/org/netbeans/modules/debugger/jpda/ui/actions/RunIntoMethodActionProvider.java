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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.actions.RunIntoMethodActionSupport;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author  Martin Entlicher
 */
@ActionsProvider.Registration(path="netbeans-JPDASession", actions={"runIntoMethod"})
public class RunIntoMethodActionProvider extends ActionsProviderSupport 
                                         implements PropertyChangeListener,
                                                    ActionsManagerListener {

    private static final Logger logger = Logger.getLogger(RunIntoMethodActionProvider.class.getName());

    private final JPDADebuggerImpl debugger;
    private ActionsManager lastActionsManager;
    
    public RunIntoMethodActionProvider(ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.lookupFirst 
                (null, JPDADebugger.class);
        debugger.addPropertyChangeListener (JPDADebuggerImpl.PROP_STATE, this);
        EditorContextBridge.getContext().addPropertyChangeListener (this);
    }
    
    private void destroy () {
        debugger.removePropertyChangeListener (JPDADebuggerImpl.PROP_STATE, this);
        EditorContextBridge.getContext().removePropertyChangeListener (this);
        if (lastActionsManager != null) {
            lastActionsManager.removeActionsManagerListener(ActionsManagerListener.PROP_ACTION_STATE_CHANGED, this);
            lastActionsManager = null;
        }
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
    public void propertyChange (PropertyChangeEvent evt) {
        setEnabled (
            ActionsManager.ACTION_RUN_INTO_METHOD,
            getActionsManager().isEnabled(ActionsManager.ACTION_CONTINUE) &&
            (debugger.getState () == JPDADebugger.STATE_STOPPED) &&
            (debugger.getCurrentThread() != null) &&
            (EditorContextBridge.getContext().getCurrentLineNumber () >= 0) && 
            (EditorContextBridge.getContext().getCurrentURL ().endsWith (".java"))
        );
        if (debugger.getState () == JPDADebugger.STATE_DISCONNECTED) 
            destroy ();
    }
    
    @Override
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_RUN_INTO_METHOD);
    }
    
    @Override
    public void doAction (Object action) {
        final String[] methodPtr = new String[1];
        final String[] urlPtr = new String[1];
        final String[] classPtr = new String[1];
        final java.awt.IllegalComponentStateException[] cnex = new java.awt.IllegalComponentStateException[] { null };
        final int[] linePtr = new int[1];
        final int[] offsetPtr = new int[1];
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    EditorContext context = EditorContextBridge.getContext();
                    methodPtr[0] = context.getSelectedMethodName ();
                    linePtr[0] = context.getCurrentLineNumber();
                    offsetPtr[0] = EditorContextBridge.getCurrentOffset();
                    urlPtr[0] = context.getCurrentURL();
                    try {
                        classPtr[0] = context.getCurrentClassName();
                    } catch (java.awt.IllegalComponentStateException icsex) {
                        cnex[0] = icsex;
                    }
                }
            });
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex.getTargetException());
            return;
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }
        final String method = methodPtr[0];
        if (method.length () < 1) {
            debugger.actionMessageCallback(
                    ActionsManager.ACTION_RUN_INTO_METHOD,
                    NbBundle.getMessage(RunIntoMethodActionProvider.class,
                                    "MSG_Put_cursor_on_some_method_call")
            );
            return;
        }
        final int methodLine = linePtr[0];
        final int methodOffset = offsetPtr[0];
        final String url = urlPtr[0];
        String className;
        if (cnex[0] == null) {
            className = classPtr[0]; //debugger.getCurrentThread().getClassName();
        } else {
            className = cnex[0].getMessage();
        }
        RunIntoMethodActionSupport.runIntoMethod(debugger, url, className,
                                                 method, methodLine, methodOffset);
    }
    
    @Override
    public void actionPerformed(Object action) {
        // Is never called
    }

    @Override
    public void actionStateChanged(Object action, boolean enabled) {
        if (ActionsManager.ACTION_CONTINUE == action) {
            setEnabled (
                ActionsManager.ACTION_RUN_INTO_METHOD,
                enabled &&
                (debugger.getState () == JPDADebugger.STATE_STOPPED) &&
                (EditorContextBridge.getContext().getCurrentLineNumber () >= 0) && 
                (EditorContextBridge.getContext().getCurrentURL ().endsWith (".java"))
            );
        }
    }
}
