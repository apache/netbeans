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

package org.netbeans.modules.debugger.jpda.backend.truffle;

import com.oracle.truffle.api.debug.Breakpoint;
import com.oracle.truffle.api.debug.Debugger;
import com.oracle.truffle.api.debug.DebuggerSession;
import com.oracle.truffle.api.debug.SuspendAnchor;
import com.oracle.truffle.api.debug.SuspendedCallback;
import com.oracle.truffle.api.debug.SuspendedEvent;
import com.oracle.truffle.api.debug.SuspensionFilter;
import com.oracle.truffle.api.nodes.Node;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author martin
 */
class JPDATruffleDebugManager implements SuspendedCallback {
    
    private final Reference<Debugger> debugger;
    private final Reference<DebuggerSession> session;
    private final ThreadLocal<SuspendedEvent> suspendedEvents = new ThreadLocal<>();
    private final ThreadLocal<Object[]> suspendHere = new ThreadLocal<>();
    private final boolean supportsJavaFrames;

    public JPDATruffleDebugManager(Debugger debugger, boolean includeInternal, boolean doStepInto) {
        this.debugger = new WeakReference<>(debugger);
        DebuggerSession debuggerSession = debugger.startSession(this);
        debuggerSession.setSteppingFilter(createSteppingFilter(includeInternal));
        supportsJavaFrames = supportsJavaFrames(debuggerSession);
        if (doStepInto) {
            debuggerSession.suspendNextExecution();
        }
        this.session = new WeakReference<>(debuggerSession);
    }

    private static boolean supportsJavaFrames(DebuggerSession debuggerSession) {
        try {
            Method setShowHostStackFramesMethod = DebuggerSession.class.getMethod("setShowHostStackFrames", Boolean.TYPE);
            try {
                setShowHostStackFramesMethod.invoke(debuggerSession, true);
                return true;
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                LangErrors.exception("setShowHostStackFrames", ex);
                return false;
            }
        } catch (NoSuchMethodException | SecurityException ex) {
            return false;
        }
    }

    static SuspensionFilter createSteppingFilter(boolean includeInternal) {
        return SuspensionFilter.newBuilder().ignoreLanguageContextInitialization(true).includeInternal(includeInternal).build();
    }

    Debugger getDebugger() {
        return debugger.get();
    }
    
    DebuggerSession getDebuggerSession() {
        return session.get();
    }
    
    SuspendedEvent getCurrentSuspendedEvent() {
        return suspendedEvents.get();
    }
    
    void dispose() {
        DebuggerSession ds = session.get();
        if (ds != null) {
            ds.close();
            session.clear();
        }
    }
    
    void prepareExecStepInto() {
        session.get().suspendNextExecution();
    }

    void prepareExecContinue() {
        //System.err.println("prepareExecContinue()...");
        // TODO: HOW?
        //prepareStepInto = false;
        // Do not call methods on ExecutionEvent asynchronously.
        /* Rely on another ExecutionEvent comes when needed
        try {
            execEvent.prepareContinue();
        } catch (RuntimeException rex) {
            // Unable to use the event any more. A new should come when needed.
            // Report until there is some known contract:
            System.err.println("Ignoring prepareExecContinue():");
            rex.printStackTrace();
        }
        */
        //System.err.println("prepareExecContinue() DONE.");
    }

    @Override
    public void onSuspend(SuspendedEvent event) {
        JPDATruffleAccessor.trace("JPDATruffleDebugManager.onSuspend({0})", event);
        if (suspendHere.get() != null) {
            // A special 'suspendHere':
            SourcePosition position = new SourcePosition(event.getSourceSection(), event.getTopStackFrame().getLanguage());
            Object[] haltInfo = new Object[]{
                this, position,
                event.getSuspendAnchor() == SuspendAnchor.BEFORE,
                event.getReturnValue(),
                new FrameInfo(event.getTopStackFrame(), event.getStackFrames(), supportsJavaFrames),
                supportsJavaFrames
            };
            suspendHere.set(haltInfo);
            return ;
        }
        Breakpoint[] breakpointsHit = new Breakpoint[event.getBreakpoints().size()];
        breakpointsHit = event.getBreakpoints().toArray(breakpointsHit);
        Throwable[] breakpointConditionExceptions = new Throwable[breakpointsHit.length];
        for (int i = 0; i < breakpointsHit.length; i++) {
            breakpointConditionExceptions[i] = event.getBreakpointConditionException(breakpointsHit[i]);
        }
        suspendedEvents.set(event);
        try {
            SourcePosition position = new SourcePosition(event.getSourceSection(), event.getTopStackFrame().getLanguage());
            int stepCmd = JPDATruffleAccessor.executionHalted(
                    this, position,
                    event.getSuspendAnchor() == SuspendAnchor.BEFORE,
                    event.getReturnValue(),
                    new FrameInfo(event.getTopStackFrame(), event.getStackFrames(), supportsJavaFrames),
                    supportsJavaFrames,
                    breakpointsHit,
                    breakpointConditionExceptions,
                    0);
            switch (stepCmd) {
                case -1: break;
                case 0: event.prepareContinue();
                        break;
                case 1: event.prepareStepInto(1);
                        break;
                case 2: event.prepareStepOver(1);
                        break;
                case 3: event.prepareStepOut(1);
                        break;
                default:
                        throw new IllegalStateException("Unknown step command: "+stepCmd);
            }
        } finally {
            suspendedEvents.remove();
        }
    }

    Object[] suspendHere() {
        DebuggerSession debuggerSession = session.get();
        if (debuggerSession == null) {
            return null;
        }
        try {
            Method suspendHereMethod = DebuggerSession.class.getMethod("suspendHere", Node.class);
            try {
                suspendHere.set(new Object[]{});
                boolean success = (Boolean) suspendHereMethod.invoke(debuggerSession, new Object[]{null});
                if (success) {
                    return suspendHere.get();
                }
            } finally {
                suspendHere.remove();
            }
        } catch (NoSuchMethodException ex) {
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LangErrors.exception("suspendHere", ex);
        }
        return null;
    }
    
}
