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

package org.netbeans.modules.debugger.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;

/**
 * Toggle Breakpoint action in Editor Gutter, which creates a new breakpoint if none is present.
 * 
 * @author Martin Entlicher
 */
public class BreakpointToggleActionToggleNew extends SystemAction {

    private Action action;
    
    public BreakpointToggleActionToggleNew() {
        setEnabled(true);
    }

    private Action getAction() {
        if (action == null) {
            action = DebuggerAction.createToggleBreakpointAction();
        }
        return action;
    }

    @Override
    public boolean isEnabled() {
        boolean onBreakpoint = SystemAction.get(BreakpointToggleActionOnBreakpoint.class).isOnBreakpoint();
        return !onBreakpoint;
    }

    @Override
    public String getName() {
        return (String) getAction().getValue(NAME);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        getAction().actionPerformed(ev);
    }

}
