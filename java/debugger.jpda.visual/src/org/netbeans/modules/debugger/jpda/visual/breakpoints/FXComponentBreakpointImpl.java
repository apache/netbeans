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
package org.netbeans.modules.debugger.jpda.visual.breakpoints;

import com.sun.jdi.ObjectReference;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

/**
 *
 * @author martin
 */
public class FXComponentBreakpointImpl extends BaseComponentBreakpointImpl {
    public FXComponentBreakpointImpl(ComponentBreakpoint cb, JPDADebugger debugger) {
        super(cb, debugger);
    }

    @Override
    protected void initServiceBreakpoints() {
        //MethodBreakpoint mb = MethodBreakpoint.create("", "");
        ObjectReference component = cb.getComponent().getComponent(debugger);
        Variable variableComponent = ((JPDADebuggerImpl) debugger).getVariable(component);
        //mb.setInstanceFilters(debugger, new ObjectVariable[] { (ObjectVariable) variableComponent });
        
        int type = cb.getType();
        if (((type & AWTComponentBreakpoint.TYPE_ADD) != 0) || ((type & AWTComponentBreakpoint.TYPE_REMOVE) != 0)) {
            MethodBreakpoint mb = MethodBreakpoint.create("javafx.scene.Node", "setParent");
            mb.setMethodSignature("(Ljavafx/scene/Parent)V");
            if (!cb.isEnabled()) {
                mb.disable();
            }
            addMethodBreakpoint(mb, (ObjectVariable)variableComponent);
        }
        if (((type & AWTComponentBreakpoint.TYPE_SHOW) != 0) || ((type & AWTComponentBreakpoint.TYPE_HIDE) != 0)) {
            MethodBreakpoint mb = MethodBreakpoint.create("javafx.scene.Node", "setVisible");
            mb.setMethodSignature("(Z)V");
            if (!cb.isEnabled()) {
                mb.disable();
            }
            addMethodBreakpoint(mb, (ObjectVariable) variableComponent);
        }
//        if (((type & AWTComponentBreakpoint.TYPE_REPAINT) != 0)) {
//            MethodBreakpoint mbShow = MethodBreakpoint.create("java.awt.Component", "repaint");
//            // void repaint(long tm, int x, int y, int width, int height)
//            mbShow.setMethodSignature("(JIIII)V");
//            mbShow.setHidden(true);
//            mbShow.setInstanceFilters(debugger, new ObjectVariable[] { (ObjectVariable) variableComponent });
//            mbShow.addJPDABreakpointListener(new JPDABreakpointListener() {
//                @Override
//                public void breakpointReached(JPDABreakpointEvent event) {
//
//                }
//            });
//            DebuggerManager.getDebuggerManager().addBreakpoint(mbShow);
//            serviceBreakpoints.add(mbShow);
//        }
    }
}
