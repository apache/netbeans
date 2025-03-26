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

package org.netbeans.modules.javascript2.debug.ui.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.javascript2.debug.ui.JSUtils;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.text.Line;
import org.openide.util.WeakListeners;

/**
 *
 * @author Martin
 */
@ActionsProvider.Registration(
        actions = {"toggleBreakpoint"},
        activateForMIMETypes = {JSUtils.JS_MIME_TYPE, JSUtils.TS_MIME_TYPE}
)
public class ToggleBreakpointActionProvider extends ActionsProviderSupport
                                            implements PropertyChangeListener {

    public ToggleBreakpointActionProvider() {
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, false);
        EditorContextDispatcher.getDefault().addPropertyChangeListener(
                JSUtils.JS_MIME_TYPE,
                WeakListeners.propertyChange(this, EditorContextDispatcher.getDefault()));
        EditorContextDispatcher.getDefault().addPropertyChangeListener(
                JSUtils.TS_MIME_TYPE,
                WeakListeners.propertyChange(this, EditorContextDispatcher.getDefault()));
    }

    @Override
    public void doAction(Object action) {
        Line line = JSUtils.getCurrentLine();
        if (line == null) {
            return ;
        }
        DebuggerManager d = DebuggerManager.getDebuggerManager();
        boolean add = true;
        for (Breakpoint breakpoint : d.getBreakpoints()) {
            if (breakpoint instanceof JSLineBreakpoint &&
                JSUtils.getLine((JSLineBreakpoint) breakpoint).equals(line)) {

                d.removeBreakpoint(breakpoint);
                add = false;
                break;
            }
        }
        if (add) {
            d.addBreakpoint(JSUtils.createLineBreakpoint(line));
        }
    }

    @Override
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_TOGGLE_BREAKPOINT );
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        boolean enabled = JSUtils.getCurrentLine() != null;
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, enabled);
    }
}
