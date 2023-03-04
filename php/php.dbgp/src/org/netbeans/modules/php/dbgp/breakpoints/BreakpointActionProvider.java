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
package org.netbeans.modules.php.dbgp.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.text.Line;
import org.openide.util.WeakListeners;

/**
 *
 * @author ads
 */
@ActionsProvider.Registration(actions = {"toggleBreakpoint"}, activateForMIMETypes = {Utils.MIME_TYPE})
public class BreakpointActionProvider extends ActionsProviderSupport implements PropertyChangeListener {

    public BreakpointActionProvider() {
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, false);
        EditorContextDispatcher.getDefault().addPropertyChangeListener(Utils.MIME_TYPE, WeakListeners.propertyChange(this, EditorContextDispatcher.getDefault()));
    }

    @Override
    public void doAction(Object action) {
        if (SwingUtilities.isEventDispatchThread()) {
            addBreakpoints();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    addBreakpoints();
                }
            });
        }
    }

    @Override
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }

    private void addBreakpoints() {
        Line line = Utils.getCurrentLine();
        if (line == null) {
            return;
        }
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        boolean add = true;
        for (Breakpoint breakpoint : breakpoints) {
            if (breakpoint instanceof LineBreakpoint && ((LineBreakpoint) breakpoint).getLine().equals(line)) {
                DebuggerManager.getDebuggerManager().removeBreakpoint(breakpoint);
                add = false;
                break;
            }
        }
        LineBreakpoint lineBreakpoint = new LineBreakpoint(line);
        lineBreakpoint.refreshValidity();
        if (add) {
            DebuggerManager.getDebuggerManager().addBreakpoint(lineBreakpoint);
        }
    }

    @Override
    public boolean isEnabled(Object action) {
        boolean enabled = super.isEnabled(action);
        if (ActionsManager.ACTION_TOGGLE_BREAKPOINT.equals(action)) {
            if (enabled) {
                // Check if the current line is also PHP:
                Line line = Utils.getCurrentLine();
                return line != null && Utils.isInPhpScript(line);
            }
        }
        return enabled;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // We need to push the state there :-(( instead of wait for someone to be interested in...
        boolean enabled = Utils.getCurrentLine() != null;
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, enabled);
    }

}
