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
package org.netbeans.modules.java.lsp.server.debugging.breakpoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.Breakpoint;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.ExceptionBreakpoint;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.java.lsp.server.debugging.NbThreads;

public final class BreakpointsManager {

    private static final Logger LOGGER = Logger.getLogger(BreakpointsManager.class.getName());

    private final NbThreads threadsProvider;
    private final List<NbBreakpoint> breakpoints;
    private final HashMap<String, HashMap<Integer, NbBreakpoint>> sourceToBreakpoints;
    private final AtomicInteger nextBreakpointId = new AtomicInteger(1);
    private final AtomicReference<ExceptionBreakpoint> exceptionBreakpoint = new AtomicReference<>(null);
    private final ExceptionBreakpointListener exceptionBreakpointListener = new ExceptionBreakpointListener();
    private final Map<Integer, Breakpoint> hitBreakpoints = new ConcurrentHashMap<>();
    private final Map<Integer, Variable> exceptionsByThreads = new ConcurrentHashMap<>();

    /**
     * Constructor.
     */
    public BreakpointsManager(NbThreads threadsProvider) {
        this.threadsProvider = threadsProvider;
        this.breakpoints = Collections.synchronizedList(new ArrayList<>(5));
        this.sourceToBreakpoints = new HashMap<>();
    }

    /**
     * Set breakpoints to the given source.
     * <p>
     * Deletes all old breakpoints from the source.
     *
     * @return a new list of breakpoints in that source
     */
    public NbBreakpoint[] setBreakpoints(String source, NbBreakpoint[] breakpoints, boolean sourceModified) {
        List<NbBreakpoint> result = new ArrayList<>();
        HashMap<Integer, NbBreakpoint> breakpointMap = this.sourceToBreakpoints.get(source);
        // When source file is modified, delete all previously added breakpoints.
        if (sourceModified && breakpointMap != null) {
            for (NbBreakpoint bp : breakpointMap.values()) {
                try {
                    // Destroy the breakpoint on the debugee VM.
                    bp.close();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, String.format("Remove breakpoint exception: %s", e.toString()), e);
                }
                this.breakpoints.remove(bp);
            }
            this.sourceToBreakpoints.put(source, null);
            breakpointMap = null;
        }
        if (breakpointMap == null) {
            breakpointMap = new HashMap<>();
            this.sourceToBreakpoints.put(source, breakpointMap);
        }

        // Compute the breakpoints that are newly added.
        List<NbBreakpoint> toAdd = new ArrayList<>();
        List<Integer> visitedLineNumbers = new ArrayList<>();
        for (NbBreakpoint breakpoint : breakpoints) {
            NbBreakpoint existingBP = breakpointMap.get(breakpoint.getLineNumber());
            if (existingBP != null) {
                result.add(existingBP);
                visitedLineNumbers.add(existingBP.getLineNumber());
                continue;
            } else {
                result.add(breakpoint);
            }
            toAdd.add(breakpoint);
        }

        // Compute the breakpoints that are no longer listed.
        List<NbBreakpoint> toRemove = new ArrayList<>();
        for (NbBreakpoint breakpoint : breakpointMap.values()) {
            if (!visitedLineNumbers.contains(breakpoint.getLineNumber())) {
                toRemove.add(breakpoint);
            }
        }

        removeBreakpointsInternally(source, toRemove.toArray(new NbBreakpoint[0]));
        addBreakpointsInternally(source, toAdd.toArray(new NbBreakpoint[0]));

        return result.toArray(new NbBreakpoint[0]);
    }

    private void addBreakpointsInternally(String source, NbBreakpoint[] breakpoints) {
        Map<Integer, NbBreakpoint> breakpointMap = this.sourceToBreakpoints.computeIfAbsent(source, k -> new HashMap<>());

        if (breakpoints != null && breakpoints.length > 0) {
            for (NbBreakpoint breakpoint : breakpoints) {
                breakpoint.putProperty("id", this.nextBreakpointId.getAndIncrement());
                this.breakpoints.add(breakpoint);
                breakpointMap.put(breakpoint.getLineNumber(), breakpoint);
            }
        }
    }

    /**
     * Removes the specified breakpoints from breakpoint manager.
     */
    private void removeBreakpointsInternally(String source, NbBreakpoint[] breakpoints) {
        Map<Integer, NbBreakpoint> breakpointMap = this.sourceToBreakpoints.get(source);
        if (breakpointMap == null || breakpointMap.isEmpty() || breakpoints.length == 0) {
            return;
        }

        for (NbBreakpoint breakpoint : breakpoints) {
            if (this.breakpoints.contains(breakpoint)) {
                try {
                    // Destroy the breakpoint on the debugee VM.
                    breakpoint.close();
                    this.breakpoints.remove(breakpoint);
                    breakpointMap.remove(breakpoint.getLineNumber());
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, String.format("Remove breakpoint exception: %s", e.toString()), e);
                }
            }
        }
    }

    public NbBreakpoint[] getBreakpoints() {
        return this.breakpoints.toArray(new NbBreakpoint[0]);
    }

    /**
     * Gets the registered breakpoints at the source file.
     */
    public NbBreakpoint[] getBreakpoints(String source) {
        HashMap<Integer, NbBreakpoint> breakpointMap = this.sourceToBreakpoints.get(source);
        if (breakpointMap == null) {
            return new NbBreakpoint[0];
        }
        return breakpointMap.values().toArray(new NbBreakpoint[0]);
    }

    void setExceptionBreakpoints(boolean notifyCaught, boolean notifyUncaught) {
        ExceptionBreakpoint newEP = null;
        if (notifyCaught || notifyUncaught) {
            int catchType = notifyCaught ? notifyUncaught ? ExceptionBreakpoint.TYPE_EXCEPTION_CAUGHT_UNCAUGHT : ExceptionBreakpoint.TYPE_EXCEPTION_CAUGHT : ExceptionBreakpoint.TYPE_EXCEPTION_UNCAUGHT;
            newEP = ExceptionBreakpoint.create("*", catchType);
            DebuggerManager.getDebuggerManager().addBreakpoint(newEP);
        }
        ExceptionBreakpoint oldEP = exceptionBreakpoint.getAndSet(newEP);
        if (oldEP != null) {
            DebuggerManager.getDebuggerManager().removeBreakpoint(oldEP);
        }
    }

    public void notifyBreakpointHit(int threadId, Breakpoint currentBreakpoint) {
        if (currentBreakpoint != null) {
            hitBreakpoints.put(threadId, currentBreakpoint);
        } else {
            hitBreakpoints.remove(threadId);
        }
    }

    public Variable getExceptionOn(int threadId) {
        return exceptionsByThreads.get(threadId);
    }

    /**
     * Breakpoints are always being set from the client. We must clean them so that
     * they are not duplicated on the next start.
     */
    public void disposeBreakpoints() {
        DebuggerManager debuggerManager = DebuggerManager.getDebuggerManager();
        for (NbBreakpoint breakpoint : breakpoints) {
            debuggerManager.removeBreakpoint(breakpoint.getNBBreakpoint());
        }
        ExceptionBreakpoint ep = exceptionBreakpoint.getAndSet(null);
        if (ep != null) {
            debuggerManager.removeBreakpoint(ep);
        }
        debuggerManager.removeAllWatches();
        this.sourceToBreakpoints.clear();
        this.breakpoints.clear();
        this.nextBreakpointId.set(1);
    }

    private class ExceptionBreakpointListener implements JPDABreakpointListener {

        @Override
        public void breakpointReached(JPDABreakpointEvent event) {
            Variable exceptionVariable = event.getVariable();
            if (exceptionVariable != null) {
                int threadId = threadsProvider.getId(event.getThread());
                exceptionsByThreads.put(threadId, exceptionVariable);
            }
        }
        
    }
}
