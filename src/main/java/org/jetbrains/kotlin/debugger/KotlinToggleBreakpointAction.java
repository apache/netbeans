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
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
//import org.netbeans.modules.debugger.jpda.EditorContextBridge;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;

/**
 *
 * @author Alexander.Baratynski
 */
@ActionID(id = "org.jetbrains.kotlin.debugger.KotlinToggleBreakpointAction", category = "Debug")
@ActionRegistration(displayName = "KotlinBreakpoint", lazy = false)
public class KotlinToggleBreakpointAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!submitFieldOrMethodOrClassBreakpoint()) {
            DebuggerManager.getDebuggerManager().getActionsManager().doAction(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
        }
    }
    
    private boolean submitFieldOrMethodOrClassBreakpoint() {
        DebuggerManager manager = DebuggerManager.getDebuggerManager();
        int lineNumber = KotlinEditorContextBridge.getContext().getCurrentLineNumber();
        String url = KotlinEditorContextBridge.getContext().getCurrentURL();
        
        if ("".equals(url.trim())) {
            return true;
        }
        
        LineBreakpoint lineBreakpoint = findBreakpoint(url, lineNumber);
        if (lineBreakpoint != null) {
            manager.removeBreakpoint(lineBreakpoint);
            return true;
        }
        
        lineBreakpoint = LineBreakpoint.create(url, lineNumber);
        lineBreakpoint.setPrintText("breakpoint");
        manager.addBreakpoint(lineBreakpoint);
        
        return false;
    }
    
    private static LineBreakpoint findBreakpoint(String url, int lineNumber) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (Breakpoint breakpoint : breakpoints) {
            if (!(breakpoint instanceof LineBreakpoint)) {
                continue;
            }
            LineBreakpoint lineBreakpoint = (LineBreakpoint) breakpoint;
            if (!lineBreakpoint.getURL().equals(url)) {
                continue;
            }
            if (lineBreakpoint.getLineNumber() == lineNumber) {
                return lineBreakpoint;
            }
        }
        
        return null;
    }
    
}
