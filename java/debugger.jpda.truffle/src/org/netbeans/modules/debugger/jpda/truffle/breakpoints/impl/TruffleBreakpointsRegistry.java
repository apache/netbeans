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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;

import org.openide.util.Pair;

/**
 * Registry of Truffle breakpoints. It helps to identify hit breakpoints via {@link TruffleBreakpointsRegistry#get(ObjectReference)}
 */
final class TruffleBreakpointsRegistry {

    private static final TruffleBreakpointsRegistry DEFAULT = new TruffleBreakpointsRegistry();

    private final Map<JPDADebugger, Map<ObjectReference, JSLineBreakpoint>> breakpoints = new ConcurrentHashMap<>();

    private TruffleBreakpointsRegistry() {
    }

    static TruffleBreakpointsRegistry getDefault() {
        return DEFAULT;
    }

    void add(JPDADebugger debugger, JSLineBreakpoint breakpoint, ObjectReference truffleBreakpoint) {
        Map<ObjectReference, JSLineBreakpoint> bps = breakpoints.get(debugger);
        if (bps == null) {
            bps = breakpoints.computeIfAbsent(debugger, (key) -> new ConcurrentHashMap<>());
        }
        bps.put(truffleBreakpoint, breakpoint);
    }

    void remove(JPDADebugger debugger, ObjectReference truffleBreakpoint) {
        Map<ObjectReference, JSLineBreakpoint> bps = breakpoints.get(debugger);
        if (bps != null) {
            bps.remove(truffleBreakpoint);
        }
    }

    void dispose(JPDADebugger debugger) {
        breakpoints.remove(debugger);
    }

    Pair<JPDADebugger, JSLineBreakpoint> get(ObjectReference truffleBreakpoint) {
        for (Map.Entry<JPDADebugger, Map<ObjectReference, JSLineBreakpoint>> bpsEntry : breakpoints.entrySet()) {
            JSLineBreakpoint bp = bpsEntry.getValue().get(truffleBreakpoint);
            if (bp != null) {
                return Pair.of(bpsEntry.getKey(), bp);
            }
        }
        return null;
    }
}
