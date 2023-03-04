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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ObjectReference;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.visual.JavaComponentInfo;

/**
 *
 * @author Martin Entlicher
 */
public abstract class ComponentBreakpointImpl {
    
    protected final List<JPDABreakpoint> serviceBreakpoints = new LinkedList<JPDABreakpoint>();

    void notifyRemoved() {
        for (Breakpoint b : serviceBreakpoints) {
            DebuggerManager.getDebuggerManager().removeBreakpoint(b);
        }
        serviceBreakpoints.clear();
    }

    void enable() {
        for (Breakpoint b : serviceBreakpoints) {
            b.enable();
        }
    }

    void disable() {
        for (Breakpoint b : serviceBreakpoints) {
            b.disable();
        }
        
    }
    
    void setSuspend(int suspend) {
        for (JPDABreakpoint b : serviceBreakpoints) {
            b.setSuspend(suspend);
        }
    }
}
