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
package org.netbeans.modules.debugger.jpda.truffle.breakpoints.impl;

import com.sun.jdi.ObjectReference;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;

import org.openide.util.Pair;

/**
 * Info about a hit breakpoint.
 */
public final class HitBreakpointInfo {

    private final JPDADebugger debugger;
    private final JSLineBreakpoint breakpoint;
    private final ObjectVariable conditionException;

    private HitBreakpointInfo(JPDADebugger debugger, JSLineBreakpoint breakpoint, ObjectVariable conditionException) {
        this.debugger = debugger;
        this.breakpoint = breakpoint;
        this.conditionException = conditionException;
    }

    public static HitBreakpointInfo create(ObjectVariable breakpointHit, ObjectVariable breakpointConditionException) {
        Pair<JPDADebugger, JSLineBreakpoint> breakpoint = TruffleBreakpointsRegistry.getDefault().get((ObjectReference) ((JDIVariable) breakpointHit).getJDIValue());
        if (breakpoint == null) {
            return null;
        }
        return new HitBreakpointInfo(breakpoint.first(), breakpoint.second(), breakpointConditionException);
    }

    public JPDADebugger getDebugger() {
        return debugger;
    }

    public JSLineBreakpoint getBreakpoint() {
        return breakpoint;
    }

    public ObjectVariable getConditionException() {
        return conditionException;
    }

}
