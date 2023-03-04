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
package org.netbeans.modules.cpplite.debugger.ni;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cpplite.debugger.breakpoints.CPPLiteBreakpoint;
import org.netbeans.modules.nativeimage.api.debug.NILineBreakpointDescriptor;

/**
 *
 * @author martin
 */
final class NIBreakpoints {

    private final Map<Object, CPPLiteBreakpoint> ni2C = new IdentityHashMap<>();

    Breakpoint addLineBreakpoint(Object key, NILineBreakpointDescriptor bd) {
        CPPLiteBreakpoint breakpoint;
        boolean isNew;
        synchronized (ni2C) {
            breakpoint = ni2C.get(key);
            isNew = breakpoint == null;
            if (isNew) {
                breakpoint = CPPLiteBreakpoint.create(bd.getFilePath(), bd.getLine());
                ni2C.put(key, breakpoint);
            }
        }
        // TODO Update fileUrl and line number
        if (bd.isEnabled()) {
            breakpoint.enable();
        } else {
            breakpoint.disable();
        }
        breakpoint.setCondition(bd.getCondition());
        breakpoint.setHidden(bd.isHidden());
        if (isNew) {
            DebuggerManager.getDebuggerManager().addBreakpoint(breakpoint);
        }
        return breakpoint;
    }

    void removeBreakpoint(Object key) {
        CPPLiteBreakpoint breakpoint;
        synchronized (ni2C) {
            breakpoint = ni2C.remove(key);
        }
        if (breakpoint != null) {
            DebuggerManager.getDebuggerManager().removeBreakpoint(breakpoint);
        }
    }

    void dispose() {
        List<CPPLiteBreakpoint> cbs;
        synchronized (ni2C) {
            cbs = new ArrayList<>(ni2C.size());
            cbs.addAll(ni2C.values());
            ni2C.clear();
        }
        for (CPPLiteBreakpoint cb : cbs) {
            DebuggerManager.getDebuggerManager().removeBreakpoint(cb);
        }
    }
}
