/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.debugger.jpda.truffle.access;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassType;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.EventRequest;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.InvalidObjectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

import org.netbeans.modules.debugger.jpda.expr.InvocationExceptionTranslated;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocatableWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.ClassPrepareEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.EventSetWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.EventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.LocatableEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAClassTypeImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.truffle.LanguageName;
import org.netbeans.modules.debugger.jpda.truffle.RemoteServices;
import org.netbeans.modules.debugger.jpda.truffle.TruffleDebugManager;
import org.netbeans.modules.debugger.jpda.truffle.Utils;
import org.netbeans.modules.debugger.jpda.truffle.actions.StepActionProvider;
import org.netbeans.modules.debugger.jpda.truffle.ast.TruffleNode;
import org.netbeans.modules.debugger.jpda.truffle.breakpoints.impl.HitBreakpointInfo;
import org.netbeans.modules.debugger.jpda.truffle.breakpoints.impl.TruffleBreakpointOutput;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackInfo;
import org.netbeans.modules.debugger.jpda.truffle.source.Source;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;
import org.netbeans.modules.debugger.jpda.truffle.vars.impl.TruffleScope;
import org.netbeans.modules.debugger.jpda.truffle.vars.impl.TruffleStackVariable;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.modules.debugger.jpda.util.WeakHashMapActive;
import org.openide.util.Exceptions;

/**
 * Access to the backend <code>JPDATruffleAccessor</code> class.
 */
public class TruffleAccess implements JPDABreakpointListener {
    
    private static final Logger LOG = Logger.getLogger(TruffleAccess.class.getName());
    
    public static final String BASIC_CLASS_NAME = "org.netbeans.modules.debugger.jpda.backend.truffle.JPDATruffleAccessor";    // NOI18N
    
    private static final String METHOD_EXEC_HALTED = "executionHalted";         // NOI18N
    private static final String METHOD_EXEC_STEP_INTO = "executionStepInto";    // NOI18N
    private static final String METHOD_DEBUGGER_ACCESS = "debuggerAccess";      // NOI18N
    
    private static final String VAR_NODE = "astNode";                           // NOI18N
    private static final String VAR_FRAME = "frame";                            // NOI18N
    private static final String VAR_SRC_ID = "id";                              // NOI18N
    private static final String VAR_SRC_URI = "uri";                            // NOI18N
    private static final String VAR_SRC_MIMETYPE = "mimeType";                  // NOI18N
    private static final String VAR_SRC_NAME = "name";                          // NOI18N
    private static final String VAR_SRC_HOST_METHOD = "hostMethodName";         // NOI18N
    private static final String VAR_SRC_PATH = "path";                          // NOI18N
    private static final String VAR_SRC_SOURCESECTION = "sourceSection";        // NOI18N
    private static final String VAR_SRC_CODE = "code";
    private static final String VAR_STACK_TRACE = "stackTrace";
    private static final String VAR_TOP_FRAME = "topFrame";                     // NOI18N
    private static final String VAR_TOP_VARS = "topVariables";                  // NOI18N
    private static final String VAR_THIS_OBJECT = "thisObject";                 // NOI18N
    
    private static final String METHOD_GET_VARIABLES = "getVariables";          // NOI18N
    private static final String METHOD_GET_VARIABLES_SGN = "(Lcom/oracle/truffle/api/debug/DebugStackFrame;)[Ljava/lang/Object;";  // NOI18N
    private static final String METHOD_GET_SCOPE_VARIABLES = "getScopeVariables";// NOI18N
    private static final String METHOD_GET_SCOPE_VARIABLES_SGN = "(Lcom/oracle/truffle/api/debug/DebugScope;)[Ljava/lang/Object;"; // NOI18N
    private static final String METHOD_SUSPEND_HERE = "suspendHere";            // NOI18N
    private static final String METHOD_SUSPEND_HERE_SGN = "()[Ljava/lang/Object;";// NOI18N
    private static final String METHOD_SET_UNWIND = "setUnwind";// NOI18N
    private static final String METHOD_SET_UNWIND_SGN = "(I)Z"; // NOI18N
    private static final String METHOD_GET_AST = "getTruffleAST";               // NOI18N
    private static final String METHOD_GET_AST_SGN = "(I)[Ljava/lang/Object;";
    
    private static final Map<JPDAThread, ThreadInfo> currentPCInfos = new WeakHashMap<>();
    private static final Map<JPDAThread, ThreadInfo> suspendHerePCInfos = new WeakHashMap<>();
    private static final PropertyChangeListener threadResumeListener = new ThreadResumeListener();
    
    private static final TruffleAccess DEFAULT = new TruffleAccess();

    private final Map<JPDADebugger, JPDABreakpoint> execHaltedBP = new WeakHashMapActive<>();
    private final Map<JPDADebugger, JPDABreakpoint> execStepIntoBP = new WeakHashMapActive<>();
    private final Map<JPDADebugger, JPDABreakpoint> dbgAccessBP = new WeakHashMapActive<>();
    
    private final Object methodCallAccessLock = new Object();//new ReentrantReadWriteLock(true).writeLock();
    private MethodCallsAccess methodCallsRunnable;
    private static final MethodCallsAccess METHOD_CALLS_SUCCESSFUL = new MethodCallsAccess(){@Override public void callMethods(JPDAThread thread) {}};
    
    private TruffleAccess() {}
    
    public static void init() {
        DEFAULT.initBPs();
    }
    
    public static void assureBPSet(JPDADebugger debugger, ClassType accessorClass) {
        DEFAULT.execHaltedBP.put(debugger, DEFAULT.createBP(accessorClass.name(), METHOD_EXEC_HALTED, debugger));
        DEFAULT.execStepIntoBP.put(debugger, DEFAULT.createBP(accessorClass.name(), METHOD_EXEC_STEP_INTO, debugger));
        DEFAULT.dbgAccessBP.put(debugger, DEFAULT.createBP(accessorClass.name(), METHOD_DEBUGGER_ACCESS, debugger));
    }
    
    private void initBPs() {
        // Init debugger session-independent breakpoints
    }
    
    private JPDABreakpoint createBP(String className, String methodName, JPDADebugger debugger) {
        final MethodBreakpoint mb = MethodBreakpoint.create(className, methodName);
        mb.setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY);
        mb.setHidden(true);
        mb.setSession(debugger);
        mb.addJPDABreakpointListener(this);
        DebuggerManager.getDebuggerManager().addBreakpoint(mb);
        return mb;
    }
    
    public static CurrentPCInfo getCurrentPCInfo(JPDAThread thread) {
        CurrentPCInfo cpi = getCurrentGuestPCInfo(thread);
        if (cpi == null) {
            cpi = getCurrentSuspendHereInfo(thread);
        }
        return cpi;
    }

    public static CurrentPCInfo getCurrentGuestPCInfo(JPDAThread thread) {
        ThreadInfo info;
        synchronized (currentPCInfos) {
            info = currentPCInfos.get(thread);
        }
        if (info != null) {
            return info.cpi;
        } else {
            return null;
        }
    }

    private static CurrentPCInfo getSomePCInfo(JPDADebugger dbg) {
        synchronized (currentPCInfos) {
            for (Map.Entry<JPDAThread, ThreadInfo> pce : currentPCInfos.entrySet()) {
                if (((JPDAThreadImpl) pce.getKey()).getDebugger() == dbg) {
                    CurrentPCInfo cpi = pce.getValue().cpi;
                    if (cpi != null) {
                        return cpi;
                    }
                }
            }
        }
        return null;
    }

    public static CurrentPCInfo getCurrentSuspendHereInfo(JPDAThread thread) {
        synchronized (suspendHerePCInfos) {
            ThreadInfo info = suspendHerePCInfos.get(thread);
            if (info != null) {
                return info.cpi;
            }
        }
        return null;
    }

    public static CurrentPCInfo getSuspendHere(JPDAThread thread) {
        if (getCurrentGuestPCInfo(thread) != null) {
            // Suspended in the guest language already.
            return null;
        }
        ThreadInfo info;
        synchronized (suspendHerePCInfos) {
            info = suspendHerePCInfos.get(thread);
            if (info != null) {
                if (info.cpi != null) {
                    return info.cpi;
                } else if (info.noCpi) {
                    return null;
                }
            }
            if (info == null) {
                ((JPDAThreadImpl) thread).addPropertyChangeListener(JPDAThreadImpl.PROP_SUSPENDED, threadResumeListener);
                info = new ThreadInfo();
                suspendHerePCInfos.put(thread, info);
            }
        }
        synchronized (info) {
            if (info.cpi != null) {
                return info.cpi;
            }
            if (info.noCpi) {
                return null;
            }
            JPDADebugger debugger = ((JPDAThreadImpl) thread).getDebugger();
            ClassType debugAccessorClass = TruffleDebugManager.getDebugAccessorClass(debugger);
            if (debugAccessorClass == null) {
                info.noCpi = true;
                return null;
            }
            ObjectVariable haltInfo = invokeSuspendHere(debugAccessorClass, thread);
            if (haltInfo == null) {
                info.noCpi = true;
                return null;
            }
            CurrentPCInfo cpi = getCurrentPosition(debugger, thread, ((ObjectVariable) haltInfo).getFields(0, Integer.MAX_VALUE));
            cpi.setSelectedStackFrame(null);
            info.cpi = cpi;
            return cpi;
        }
    }

    private static ObjectVariable invokeSuspendHere(ClassType debugAccessorClass, JPDAThread thread) {
        JPDAThreadImpl threadImpl = (JPDAThreadImpl) thread;
        ThreadReference tr = threadImpl.getThreadReference();
        try {
            Method suspendHereMethod = ClassTypeWrapper.concreteMethodByName(debugAccessorClass, METHOD_SUSPEND_HERE, METHOD_SUSPEND_HERE_SGN);
            JPDADebuggerImpl debugger = threadImpl.getDebugger();
            Value haltInfo;
            threadImpl.notifyMethodInvoking();
            Runnable cleanup = null;
            try {
                cleanup = skipSuspendedEventClearLeakingReferences(debugger, thread);
                haltInfo = ClassTypeWrapper.invokeMethod(debugAccessorClass, tr, suspendHereMethod, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
            } finally {
                try {
                    cleanup.run();
                } finally {
                    threadImpl.notifyMethodInvokeDone();
                }
            }
            if (haltInfo instanceof ObjectReference) {
                return (ObjectVariable) debugger.getVariable(haltInfo);
            }
        } catch (InvocationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (Exception ex) {
            LOG.log(Level.CONFIG, "Invoking " + METHOD_SUSPEND_HERE, ex);
        }
        return null;
    }

    private static final String CLEAR_REFERENCES_CLASS = "com.oracle.truffle.api.debug.SuspendedEvent";
    private static final String CLEAR_REFERENCES_METHOD = "clearLeakingReferences";

    private static Runnable skipSuspendedEventClearLeakingReferences(JPDADebugger debugger, JPDAThread thread) {
        ThreadReference tr = ((JPDAThreadImpl) thread).getThreadReference();
        MethodBreakpoint clearLeakingReferencesBreakpoint = MethodBreakpoint.create(CLEAR_REFERENCES_CLASS, CLEAR_REFERENCES_METHOD);
        clearLeakingReferencesBreakpoint.setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY);
        clearLeakingReferencesBreakpoint.setThreadFilters(debugger, new JPDAThread[] { thread });
        clearLeakingReferencesBreakpoint.setHidden(true);
        Function<EventSet, Boolean> breakpointEventInterceptor = eventSet -> {
            try {
                ThreadReference etr = null;
                Method method = null;
                for (Event e: eventSet) {
                    if (e instanceof ClassPrepareEvent) {
                        etr = ClassPrepareEventWrapper.thread((ClassPrepareEvent) e);
                    } else if (e instanceof LocatableEvent) {
                        etr = LocatableEventWrapper.thread((LocatableEvent) e);
                        method = LocationWrapper.method(LocatableWrapper.location((LocatableEvent) e));
                    }
                }
                if (tr.equals(etr) && method != null && CLEAR_REFERENCES_METHOD.equals(method.name()) && CLEAR_REFERENCES_CLASS.equals(method.declaringType().name())) {
                    boolean resume = true;
                    for (Event e: eventSet) {
                        EventRequest r = EventWrapper.request(e);
                        Executor exec = (r != null) ? (Executor) EventRequestWrapper.getProperty (r, "executor") : null;
                        resume = resume & exec.exec(e);
                    }
                    if (resume) {
                        try {
                            EventSetWrapper.resume(eventSet);
                        } catch (IllegalThreadStateExceptionWrapper | ObjectCollectedExceptionWrapper ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper ex) {
                return false;
            }
        };
        clearLeakingReferencesBreakpoint.addJPDABreakpointListener(event -> {
            DebuggerManager.getDebuggerManager().removeBreakpoint(clearLeakingReferencesBreakpoint);
            try {
                ThreadReferenceWrapper.forceEarlyReturn(tr, tr.virtualMachine().mirrorOfVoid());
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            event.resume();
            ((JPDADebuggerImpl) debugger).getOperator().removeEventInterceptor(breakpointEventInterceptor);
        });
        ((JPDADebuggerImpl) debugger).getOperator().addEventInterceptor(breakpointEventInterceptor);
        DebuggerManager.getDebuggerManager().addBreakpoint(clearLeakingReferencesBreakpoint);
        return () -> { // Cleanup
            ((JPDADebuggerImpl) debugger).getOperator().removeEventInterceptor(breakpointEventInterceptor);
            DebuggerManager.getDebuggerManager().removeBreakpoint(clearLeakingReferencesBreakpoint);
        };
    }

    @Override
    public void breakpointReached(JPDABreakpointEvent event) {
        Object bp = event.getSource();
        JPDADebugger debugger = event.getDebugger();
        if (execHaltedBP.get(debugger) == bp) {
            LOG.log(Level.FINE, "TruffleAccessBreakpoints.breakpointReached({0}), exec halted.", event);
            if (!setCurrentPosition(debugger, event.getThread())) {
                event.resume();
            } else {
                StepActionProvider.killJavaStep(debugger);
            }
        } else if (execStepIntoBP.get(debugger) == bp) {
            LOG.log(Level.FINE, "TruffleAccessBreakpoints.breakpointReached({0}), exec step into.", event);
            if (!setCurrentPosition(debugger, event.getThread())) {
                event.resume();
            } else {
                StepActionProvider.killJavaStep(debugger);
            }
        } else if (dbgAccessBP.get(debugger) == bp) {
            LOG.log(Level.FINE, "TruffleAccessBreakpoints.breakpointReached({0}), debugger access.", event);
            try {
                synchronized (methodCallAccessLock) {
                    if (methodCallsRunnable != null) {
                        invokeMethodCalls(event.getThread(), methodCallsRunnable);
                    }
                    methodCallsRunnable = METHOD_CALLS_SUCCESSFUL;
                    methodCallAccessLock.notifyAll();
                }
            } finally {
                event.resume();
            }
        }
    }
    
    private boolean setCurrentPosition(JPDADebugger debugger, JPDAThread thread) {
        CurrentPCInfo cpci = getCurrentPosition(debugger, thread);
        if (cpci == null) {
            return false;
        }
        synchronized (currentPCInfos) {
            ThreadInfo info = currentPCInfos.get(thread);
            if (info == null) {
                ((JPDAThreadImpl) thread).addPropertyChangeListener(JPDAThreadImpl.PROP_SUSPENDED, threadResumeListener);
                info = new ThreadInfo();
                currentPCInfos.put(thread, info);
            }
            info.cpi = cpci;
        }
        return true;
    }

    private static CurrentPCInfo getCurrentPosition(JPDADebugger debugger, JPDAThread thread) {
        try {
            CallStackFrame csf = thread.getCallStack(0, 1)[0];
            LocalVariable[] localVariables = csf.getLocalVariables();
            return getCurrentPosition(debugger, thread, localVariables);
        } catch (AbsentInformationException | IllegalStateException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    private static CurrentPCInfo getCurrentPosition(JPDADebugger debugger, JPDAThread thread, Variable[] haltVars) {
        try {
            ExecutionHaltedInfo haltedInfo = ExecutionHaltedInfo.get(haltVars);
            ObjectVariable sourcePositionVar = haltedInfo.sourcePositions;
            SourcePosition sp = getSourcePosition(debugger, sourcePositionVar);
            
            ObjectVariable frameInfoVar = haltedInfo.frameInfo;
            ObjectVariable frame = (ObjectVariable) frameInfoVar.getField(VAR_FRAME);
            ObjectVariable topVars = (ObjectVariable) frameInfoVar.getField(VAR_TOP_VARS);
            TruffleScope[] scopes = createScopes(debugger, topVars);
            ObjectVariable stackTrace = (ObjectVariable) frameInfoVar.getField(VAR_STACK_TRACE);
            String topFrameDescription = (String) frameInfoVar.getField(VAR_TOP_FRAME).createMirrorObject();
            ObjectVariable thisObject = null;// TODO: (ObjectVariable) frameInfoVar.getField("thisObject");
            TruffleStackFrame topFrame = new TruffleStackFrame(debugger, thread, 0, frame, topFrameDescription, null/*code*/, scopes, thisObject, true);
            TruffleStackInfo stack = new TruffleStackInfo(debugger, thread, stackTrace, haltedInfo.supportsJavaFrames);
            HitBreakpointInfo[] breakpointInfos = getBreakpointInfos(haltedInfo, thread);
            CurrentPCInfo cpi = new CurrentPCInfo(haltedInfo.stepCmd, thread, sp, scopes, topFrame,stack, depth -> {
                return getTruffleAST(debugger, (JPDAThreadImpl) thread, depth, sp, stack);
            });
            if (breakpointInfos != null) {
                TruffleBreakpointOutput.breakpointsHit(breakpointInfos, cpi);
            }
            return cpi;
        } catch (IllegalStateException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    private static HitBreakpointInfo[] getBreakpointInfos(ExecutionHaltedInfo haltedInfo, JPDAThread thread) {
        ObjectVariable[] breakpointsHit = haltedInfo.breakpointsHit;
        ObjectVariable[] breakpointConditionExceptions = haltedInfo.breakpointConditionExceptions;
        int n = (breakpointsHit != null) ? breakpointsHit.length : 0;
        HitBreakpointInfo[] breakpointInfos = null;
        for (int i = 0; i < n; i++) {
            ObjectVariable exception = (breakpointConditionExceptions != null) ? breakpointConditionExceptions[i] : null;
            HitBreakpointInfo breakpointInfo = HitBreakpointInfo.create(breakpointsHit[i], exception);
            if (breakpointInfo != null) {
                if (breakpointInfos == null) {
                    breakpointInfos = new HitBreakpointInfo[] { breakpointInfo };
                } else {
                    // There will rarely be more than one breakpoint hit
                    breakpointInfos = Arrays.copyOf(breakpointInfos, breakpointInfos.length + 1);
                    breakpointInfos[breakpointInfos.length - 1] = breakpointInfo;
                }
            }
        }
        return breakpointInfos;
    }

    private static TruffleNode getTruffleAST(JPDADebugger debugger, JPDAThreadImpl thread, int depth, SourcePosition topPosition, TruffleStackInfo stack) {
        JPDAClassType debugAccessor = TruffleDebugManager.getDebugAccessorJPDAClass(debugger);
        Lock lock = thread.accessLock.writeLock();
        lock.lock();
        try {
            if (thread.getState() == JPDAThread.STATE_ZOMBIE) {
                return null;
            }
            final boolean[] suspended = new boolean[] { false };
            PropertyChangeListener threadChange = (event) -> {
                synchronized (suspended) {
                    suspended[0] = (Boolean) event.getNewValue();
                    suspended.notifyAll();
                }
            };
            thread.addPropertyChangeListener(JPDAThreadImpl.PROP_SUSPENDED, threadChange);
            while (!thread.isSuspended()) {
                lock.unlock();
                lock = null;
                synchronized (suspended) {
                    if (!suspended[0]) {
                        try {
                            suspended.wait();
                        } catch (InterruptedException ex) {}
                    }
                }
                lock = thread.accessLock.writeLock();
                lock.lock();
            }
            thread.removePropertyChangeListener(JPDAThreadImpl.PROP_SUSPENDED, threadChange);
            Variable ast = ((JPDAClassTypeImpl) debugAccessor).invokeMethod(thread, METHOD_GET_AST,
                                                      METHOD_GET_AST_SGN,
                                                      new Variable[] { debugger.createMirrorVar(depth, true) });
            Variable[] astInfo = ((ObjectVariable) ast).getFields(0, Integer.MAX_VALUE);
            SourcePosition position;
            if (depth == 0) {
                position = topPosition;
            } else {
                TruffleStackFrame[] stackFrames = stack.getStackFrames(true);
                if (depth >= stackFrames.length) {
                    return null;
                }
                position = stackFrames[depth].getSourcePosition();
            }
            return TruffleNode.newBuilder().nodes((String) astInfo[0].createMirrorObject()).currentPosition(position).build();
        } catch (InvalidExpressionException | InvalidObjectException | NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    public static SourcePosition getSourcePosition(JPDADebugger debugger, ObjectVariable sourcePositionVar) {
        Field varSrcId = sourcePositionVar.getField(VAR_SRC_ID);
        if (varSrcId == null) {
            // sourcePositionVar represents null
            return null;
        }
        long id = (Long) varSrcId.createMirrorObject();
        Field varSourceSection = sourcePositionVar.getField(VAR_SRC_SOURCESECTION);
        String sourceSection = (String) varSourceSection.createMirrorObject();
        if (sourceSection == null) {
            // No source section information
            return null;
        }
        Source src = Source.getExistingSource(debugger, id);
        if (src == null) {
            String name = (String) sourcePositionVar.getField(VAR_SRC_NAME).createMirrorObject();
            String hostMethodName = (String) sourcePositionVar.getField(VAR_SRC_HOST_METHOD).createMirrorObject();
            String path = (String) sourcePositionVar.getField(VAR_SRC_PATH).createMirrorObject();
            URI uri = (URI) sourcePositionVar.getField(VAR_SRC_URI).createMirrorObject();
            String mimeType = (String) sourcePositionVar.getField(VAR_SRC_MIMETYPE).createMirrorObject();
            StringReference codeRef = (StringReference) ((JDIVariable) sourcePositionVar.getField(VAR_SRC_CODE)).getJDIValue();
            src = Source.getSource(debugger, id, name, hostMethodName, path, uri, mimeType, codeRef);
        }
        return new SourcePosition(debugger, id, src, sourceSection);
    }
    
    private static TruffleScope[] createScopes(JPDADebugger debugger, ObjectVariable varsArrVar) {
        Field[] varsArr = varsArrVar.getFields(0, Integer.MAX_VALUE);
        List<TruffleScope> scopes = new LinkedList<>();
        int n = varsArr.length;
        int i = 0;
        while (i < n) {
            String scopeName = (String) varsArr[i++].createMirrorObject();
            boolean hasReceiver = (Boolean) varsArr[i++].createMirrorObject();
            int numVars = (Integer) varsArr[i++].createMirrorObject();
            TruffleVariable[] variables = new TruffleVariable[numVars];
            i = fillVars(debugger, variables, varsArr, hasReceiver, i);
            scopes.add(new TruffleScope(scopeName, variables));
        }
        return scopes.toArray(new TruffleScope[0]);
    }
    
    private static int fillVars(JPDADebugger debugger, TruffleVariable[] vars, Field[] varsArr, boolean hasReceiver, int i) {
        for (int vi = 0; vi < vars.length; vi++) {
            String name = (String) varsArr[i++].createMirrorObject();
            LanguageName language = LanguageName.parse((String) varsArr[i++].createMirrorObject());
            String type = (String) varsArr[i++].createMirrorObject();
            boolean readable = (Boolean) varsArr[i++].createMirrorObject();
            boolean writable = (Boolean) varsArr[i++].createMirrorObject();
            boolean internal = (Boolean) varsArr[i++].createMirrorObject();
            String valueStr = (String) varsArr[i++].createMirrorObject();
            ObjectVariable valueSourceDef = (ObjectVariable) varsArr[i++];
            Supplier<SourcePosition> valueSource = parseSourceLazy(debugger,
                                                                   valueSourceDef,
                                                                   (JDIVariable) varsArr[i++]);
            ObjectVariable typeSourceDef = (ObjectVariable) varsArr[i++];
            Supplier<SourcePosition> typeSource = parseSourceLazy(debugger,
                                                                  typeSourceDef,
                                                                  (JDIVariable) varsArr[i++]);
            ObjectVariable value = (ObjectVariable) varsArr[i++];
            vars[vi] = new TruffleStackVariable(debugger, name, language, type, readable,
                                                writable, internal, valueStr,
                                                valueSourceDef.getUniqueID() != 0L,
                                                valueSource,
                                                typeSourceDef.getUniqueID() != 0L,
                                                typeSource, hasReceiver, value);
            hasReceiver = false;
        }
        return i;
    }

    private static Supplier<SourcePosition> parseSourceLazy(JPDADebugger debugger, Variable sourceDefVar, JDIVariable codeRefVar) {
        return () -> parseSource(debugger,
                                 (String) sourceDefVar.createMirrorObject(),
                                 (StringReference) codeRefVar.getJDIValue());
    }
    
    private static SourcePosition parseSource(JPDADebugger debugger, String sourceDef, StringReference codeRef) {
        if (sourceDef == null) {
            return null;
        }
        int sourceId;
        String sourceName;
        String hostMethodName;
        String sourcePath;
        URI sourceURI;
        String mimeType;
        String sourceSection;
        try {
            int i1 = 0;
            int i2 = sourceDef.indexOf('\n', i1);
            sourceId = Integer.parseInt(sourceDef.substring(i1, i2));
            i1 = i2 + 1;
            i2 = sourceDef.indexOf('\n', i1);
            sourceName = sourceDef.substring(i1, i2);
            i1 = i2 + 1;
            i2 = sourceDef.indexOf('\n', i1);
            hostMethodName = Utils.stringOrNull(sourceDef.substring(i1, i2));
            i1 = i2 + 1;
            i2 = sourceDef.indexOf('\n', i1);
            sourcePath = sourceDef.substring(i1, i2);
            i1 = i2 + 1;
            i2 = sourceDef.indexOf('\n', i1);
            String uriStr = Utils.stringOrNull(sourceDef.substring(i1, i2));
            if (uriStr != null) {
                try {
                    sourceURI = new URI(uriStr);
                } catch (URISyntaxException usex) {
                    Exceptions.printStackTrace(new IllegalStateException("Bad URI: "+sourceDef.substring(i1, i2), usex));
                    sourceURI = null;
                }
            } else {
                sourceURI = null;
            }
            i1 = i2 + 1;
            i2 = sourceDef.indexOf('\n', i1);
            mimeType = Utils.stringOrNull(sourceDef.substring(i1, i2));
            i1 = i2 + 1;
            i2 = sourceDef.indexOf('\n', i1);
            if (i2 < 0) {
                i2 = sourceDef.length();
            }
            sourceSection = sourceDef.substring(i1, i2);
        } catch (IndexOutOfBoundsException ioob) {
            throw new IllegalStateException("var source definition='"+sourceDef+"'", ioob);
        }
        Source src = Source.getSource(debugger, sourceId, sourceName, hostMethodName, sourcePath, sourceURI, mimeType, codeRef);
        return new SourcePosition(debugger, sourceId, src, sourceSection);
    }
    
    public static TruffleScope[] createFrameScopes(final JPDADebugger debugger,
                                                    //final Variable suspendedInfo,
                                                    final Variable frameInstance) {
        JPDAClassType debugAccessor = TruffleDebugManager.getDebugAccessorJPDAClass(debugger);
        try {
            Variable frameVars = debugAccessor.invokeMethod(METHOD_GET_VARIABLES,
                                                            METHOD_GET_VARIABLES_SGN,
                                                            new Variable[] { frameInstance });
            TruffleScope[] scopes = createScopes(debugger, (ObjectVariable) frameVars);
            return scopes;
        } catch (InvalidExpressionException | NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
            return new TruffleScope[] {};
        }
    }
    
    public static boolean unwind(JPDADebugger debugger, JPDAThread thread, int depth) {
        JPDAClassType debugAccessor = TruffleDebugManager.getDebugAccessorJPDAClass(debugger);
        try {
            Variable arg = debugger.createMirrorVar(depth);
            Variable res = ((JPDAClassTypeImpl) debugAccessor).invokeMethod(thread, METHOD_SET_UNWIND, METHOD_SET_UNWIND_SGN, new Variable[] { arg });
            return (Boolean) res.createMirrorObject();
        } catch (InvalidExpressionException | InvalidObjectException | NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    /**
     * Safe access to method calls in the backend accessor class.
     * @param methodCalls The runnable, that is called under write lock on the current thread.
     * @return <code>true</code> when the runnable with method calls is executed,
     *         <code>false</code> when method execution is not possible.
     */
    public static boolean methodCallingAccess(final JPDADebugger debugger, MethodCallsAccess methodCalls) {
        synchronized (DEFAULT.methodCallAccessLock) {
            while (DEFAULT.methodCallsRunnable != null) {
                // we're already processing some method calls...
                try {
                    DEFAULT.methodCallAccessLock.wait();
                } catch (InterruptedException ex) {
                    return false;
                }
            }
            CurrentPCInfo currentPCInfo = getSomePCInfo(debugger);
            if (currentPCInfo != null) {
                JPDAThread thread = currentPCInfo.getThread();
                if (thread != null) {
                    boolean success = invokeMethodCalls(thread, methodCalls);
                    if (success) {
                        return true;
                    }
                }
            }
            // Was not able to invoke methods
            boolean interrupted = RemoteServices.interruptServiceAccessThread(debugger);
            if (!interrupted) {
                return false;
            }
            PropertyChangeListener finishListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (JPDADebugger.STATE_DISCONNECTED == debugger.getState()) {
                        synchronized (DEFAULT.methodCallAccessLock) {
                            DEFAULT.methodCallAccessLock.notifyAll();
                        }
                    }
                }
            };
            debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, finishListener);
            DEFAULT.methodCallsRunnable = methodCalls;
            try {
                DEFAULT.methodCallAccessLock.wait();
            } catch (InterruptedException ex) {
                return false;
            } finally {
                debugger.removePropertyChangeListener(JPDADebugger.PROP_STATE, finishListener);
            }
            boolean success = (DEFAULT.methodCallsRunnable == METHOD_CALLS_SUCCESSFUL);
            DEFAULT.methodCallsRunnable = null;
            return success;
        }
    }
    
    private static boolean invokeMethodCalls(JPDAThread thread, MethodCallsAccess methodCalls) {
        assert Thread.holdsLock(DEFAULT.methodCallAccessLock);
        boolean invoking = false;
        InvocationException iex = null;
        try {
            ((JPDAThreadImpl) thread).notifyMethodInvoking();
            invoking = true;
            methodCalls.callMethods(thread);
            return true;
        } catch (PropertyVetoException pvex) {
            return false;
        } catch (InvocationException ex) {
            iex = ex;
        } finally {
            if (invoking) {
                ((JPDAThreadImpl) thread).notifyMethodInvokeDone();
            }
        }
        if (iex != null) {
            Throwable ex = new InvocationExceptionTranslated(iex, ((JPDAThreadImpl) thread).getDebugger()).preload((JPDAThreadImpl) thread);
            Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Invoking "+methodCalls));
        }
        return false;
    }

    public static interface MethodCallsAccess {
        
        void callMethods(JPDAThread thread) throws InvocationException;
        
    }

    private static final class ThreadInfo {
        volatile CurrentPCInfo cpi;
        volatile boolean noCpi;
    }

    private static final class ThreadResumeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            JPDAThreadImpl t = (JPDAThreadImpl) evt.getSource();
            if (!(Boolean) evt.getNewValue() && !t.isMethodInvoking()) { // not suspended, resumed
                synchronized (currentPCInfos) {
                    clear(currentPCInfos.get(t));
                }
                synchronized (suspendHerePCInfos) {
                    clear(suspendHerePCInfos.get(t));
                }
            }
        }

        private void clear(ThreadInfo info) {
            if (info != null) {
                info.cpi = null;
                info.noCpi = false;
            }
        }
    }
}
