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
import java.util.Collection;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.spi.debugger.ui.BreakpointAnnotation;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 * Toggle Breakpoint action in Editor Gutter, which toggles off an existing breakpoint.
 * 
 * @author Martin Entlicher
 */
public class BreakpointToggleActionOnBreakpoint extends SystemAction implements ContextAwareAction {

    private boolean onBreakpoint;
    private Action action;

    public BreakpointToggleActionOnBreakpoint() {
        setEnabled(false);
    }

    private Action getAction() {
        if (action == null) {
            action = DebuggerAction.createToggleBreakpointAction();
        }
        return action;
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

    public boolean isOnBreakpoint() {
        return onBreakpoint;
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        Collection<? extends BreakpointAnnotation> ann = actionContext.lookupAll(BreakpointAnnotation.class);
        if (ann.size() > 0) {
            onBreakpoint = true;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    onBreakpoint = false;
                }
            });
            return getAction();
        } else {
            return this;
        }
    }

}
