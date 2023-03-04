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

package org.netbeans.modules.debugger.jpda.projectsui;

import org.netbeans.modules.debugger.jpda.projectsui.MainProjectManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.project.Project;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.project.ActionProvider;
import org.openide.ErrorManager;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;


/**
*
* @author   Jan Jancura
*/
@ActionsProvider.Registration(actions="stepInto")
public class StepIntoActionProvider extends ActionsProviderSupport {

//    private MethodBreakpoint breakpoint;
    Listener listener;
    
    {
        listener = new Listener ();
        MainProjectManager.getDefault ().addPropertyChangeListener(
                WeakListeners.propertyChange(listener, MainProjectManager.getDefault()));
        DebuggerManager.getDebuggerManager ().addDebuggerListener(
                WeakListeners.create(DebuggerManagerListener.class, listener, DebuggerManager.getDebuggerManager()));
        
        setEnabled (
            ActionsManager.ACTION_STEP_INTO,
            shouldBeEnabled ()
        );
    }
    
    @Override
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_STEP_INTO);
    }
    
    @Override
    public void doAction (final Object action) {
        // start debugging of project
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        invokeAction();
                    }
                });
            } catch (InterruptedException iex) {
                // Procceed
            } catch (java.lang.reflect.InvocationTargetException itex) {
                ErrorManager.getDefault().notify(itex);
            }
        } else {
            invokeAction();
        }
    }
    
    @Override
    public void postAction(Object action, Runnable actionPerformedNotifier) {
        // start debugging of project
        invokeAction();
        actionPerformedNotifier.run();
    }
    
    private void invokeAction() {
        Project p = MainProjectManager.getDefault ().getMainProject ();
        ActionProvider actionProvider = p.getLookup ().lookup (
                ActionProvider.class
            );
        if (Arrays.asList(actionProvider.getSupportedActions ()).contains(ActionProvider.COMMAND_DEBUG_STEP_INTO) &&
            actionProvider.isActionEnabled(ActionProvider.COMMAND_DEBUG_STEP_INTO, p.getLookup())) {

            actionProvider.invokeAction (
                    ActionProvider.COMMAND_DEBUG_STEP_INTO,
                    p.getLookup ()
                );
        } else {
            Utilities.disabledActionBeep();
            setEnabled (
                ActionsManager.ACTION_STEP_INTO,
                false
            );
        }
    }
    
    private boolean shouldBeEnabled () {
        
        // check if current project supports this action
        Project p = MainProjectManager.getDefault ().getMainProject ();
        if (p == null) {
            return false;
        }
        ActionProvider actionProvider = p.getLookup().lookup(ActionProvider.class);
        if (actionProvider == null) {
            return false;
        }
        String[] sa = actionProvider.getSupportedActions ();
        int i, k = sa.length;
        for (i = 0; i < k; i++) {
            if (ActionProvider.COMMAND_DEBUG_STEP_INTO.equals (sa [i])) {
                break;
            }
        }
        if (i == k) {
            return false;
        }
        
        if (DebuggerManager.getDebuggerManager().getDebuggerEngines().length > 0) {
            // Do not enable this non-contextual action when some debugging session is already running.
            return false;
        }
        // check if this action should be enabled
        return actionProvider.isActionEnabled (
            ActionProvider.COMMAND_DEBUG_STEP_INTO,
            p.getLookup ()
        );
    }
    
    
    private class Listener implements PropertyChangeListener, DebuggerManagerListener {
        
        @Override
        public void propertyChange (PropertyChangeEvent e) {
            if (e.getSource() instanceof MainProjectManager) {
                doSetEnabled();
            }
        }
        
        @Override
        public void sessionRemoved (Session session) {}
        @Override
        public void breakpointAdded (Breakpoint breakpoint) {}
        @Override
        public void breakpointRemoved (Breakpoint breakpoint) {}
        @Override
        public Breakpoint[] initBreakpoints () {
            return new Breakpoint [0];
        }
        @Override
        public void initWatches () {}
        @Override
        public void sessionAdded (Session session) {}
        @Override
        public void watchAdded (Watch watch) {}
        @Override
        public void watchRemoved (Watch watch) {}
        
        @Override
        public void engineAdded(DebuggerEngine engine) {
            doSetEnabled();
        }
        @Override
        public void engineRemoved(DebuggerEngine engine) {
            doSetEnabled();
        }
        
        private void doSetEnabled() {
            setEnabled (
                ActionsManager.ACTION_STEP_INTO,
                shouldBeEnabled ()
            );
        }
    }
}
