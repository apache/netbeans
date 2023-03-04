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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.visual.JavaComponentInfo;

/**
 *
 * @author jbachorik
 */
public abstract class BaseComponentBreakpointImpl extends ComponentBreakpointImpl implements PropertyChangeListener {
    protected ComponentBreakpoint cb;
    protected JPDADebugger debugger;

    public BaseComponentBreakpointImpl(ComponentBreakpoint cb, JPDADebugger debugger) {
        this.cb = cb;
        this.debugger = debugger;
        initServiceBreakpoints();
        cb.addPropertyChangeListener(this);
    }
    
    protected abstract void initServiceBreakpoints();
    
    protected final void addMethodBreakpoint(MethodBreakpoint mb, ObjectVariable variableComponent) {
        mb.setHidden(true);
        mb.setInstanceFilters(debugger, new ObjectVariable[] { variableComponent });
        mb.addJPDABreakpointListener(new JPDABreakpointListener() {
            @Override
            public void breakpointReached(JPDABreakpointEvent event) {
                navigateToCustomCode(event.getThread());
            }
        });
        DebuggerManager.getDebuggerManager().addBreakpoint(mb);
        serviceBreakpoints.add(mb);
    }
    
    protected final void navigateToCustomCode(final JPDAThread thread) {
        CallStackFrame callStackFrame = null;
        try {
            CallStackFrame[] callStack = thread.getCallStack();
            for (CallStackFrame csf : callStack) {
                String cn = csf.getClassName();
                if (JavaComponentInfo.isCustomType(cn)) {
                    callStackFrame = csf;
                    break;
                }
            }
        } catch (AbsentInformationException ex) {
        }
        if (callStackFrame != null) {
            ((JPDAThreadImpl) thread).getDebugger().setPreferredTopFrame(callStackFrame);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (Breakpoint.PROP_ENABLED.equals(propertyName)) {
            if (cb.isEnabled()) {
                enable();
            } else {
                disable();
            }
        } else if (JPDABreakpoint.PROP_SUSPEND.equals(propertyName)) {
            setSuspend(cb.getSuspend());
        } else if (ComponentBreakpoint.PROP_TYPE.equals(propertyName)) {
            notifyRemoved();
            initServiceBreakpoints();
        }
    }
}
