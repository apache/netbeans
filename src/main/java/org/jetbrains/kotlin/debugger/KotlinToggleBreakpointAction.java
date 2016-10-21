/**
 * *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************
 */
package org.jetbrains.kotlin.debugger;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;

/**
 *
 * @author Alexander.Baratynski
 */
@ActionID(id = "org.jetbrains.kotlin.debugger.KotlinToggleBreakpointAction", category = "Debug")
@ActionRegistration(displayName = "KotlinBreakpoint", lazy = false)
public class KotlinToggleBreakpointAction extends AbstractAction {

    private static final String[] BREAKPOINT_ANNOTATION_TYPES = new String[] {
        "Breakpoint_broken",
        "Breakpoint",
        "CondBreakpoint_broken",
        "CondBreakpoint",
        "DisabledBreakpoint",
        "DisabledCondBreakpoint",
        "ClassBreakpoint",
        "DisabledClassBreakpoint",
        "DisabledFieldBreakpoint",
        "DisabledMethodBreakpoint",
        "FieldBreakpoint",
        "MethodBreakpoint",
    };
    
    private Object action;
    
    public KotlinToggleBreakpointAction() {
        super.setEnabled (true);
        super.putValue("default-action", true);
        super.putValue("supported-annotation-types", BREAKPOINT_ANNOTATION_TYPES);
        super.putValue("default-action-excluded-annotation-types", BREAKPOINT_ANNOTATION_TYPES);
    }
    
    public Object getAction () {
        return action;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        DebuggerManager.getDebuggerManager().getActionsManager().doAction(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }
    
}
