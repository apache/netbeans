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

package org.netbeans.modules.debugger.jpda.truffle.breakpoints.impl;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.UnsupportedOperationExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.truffle.PersistentValues;
import static org.netbeans.modules.debugger.jpda.truffle.TruffleDebugManager.configureTruffleBreakpoint;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.source.Source;
import org.netbeans.modules.debugger.jpda.truffle.source.SourceBinaryTranslator;
import org.netbeans.modules.javascript2.debug.breakpoints.JSBreakpointStatus;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;

/**
 * Handler of guest language breakpoints.
 */
public class TruffleBreakpointsHandler {
    
    private static final Logger LOG = Logger.getLogger(TruffleBreakpointsHandler.class.getName());
    
    private static final String ACCESSOR_SET_LINE_BREAKPOINT = "setLineBreakpoint"; // NOI18N
    private static final String ACCESSOR_SET_LINE_BREAKPOINT_SIGNAT =
            "(Ljava/lang/String;IILjava/lang/String;)[Lcom/oracle/truffle/api/debug/Breakpoint;";   // NOI18N
    private static final String ACCESSOR_SET_LINE_BREAKPOINT_MGR_SIGNAT =
            "(Lorg/netbeans/modules/debugger/jpda/backend/truffle/JPDATruffleDebugManager;Ljava/lang/String;IILjava/lang/String;)Lcom/oracle/truffle/api/debug/Breakpoint;";   // NOI18N
    public static final String ACCESSOR_REMOVE_BREAKPOINT = "removeBreakpoint"; // NOI18N
    public static final String ACCESSOR_REMOVE_BREAKPOINT_SIGNAT = "(Ljava/lang/Object;)V";    // NOI18N
    private static final String ACCESSOR_LINE_BREAKPOINT_RESOLVED = "breakpointResolvedAccess";
    
    private final JPDADebugger debugger;
    private ClassType accessorClass;
    
    private volatile boolean initialBreakpointsSubmitted = false;
    private final Map<JSLineBreakpoint, Set<ObjectReference>> breakpointsMap = new HashMap<>();
    private final JSBreakpointPropertyChangeListener breakpointsChangeListener = new JSBreakpointPropertyChangeListener();
    private final Object breakpointResolvedHandlerLock = new Object();
    private Breakpoint breakpointResolvedHandler;
    
    public TruffleBreakpointsHandler(JPDADebugger debugger) {
        this.debugger = debugger;
    }

    public void destroy() {
        synchronized (breakpointsMap) {
            for (JSLineBreakpoint jsbp : breakpointsMap.keySet()) {
                jsbp.removePropertyChangeListener(breakpointsChangeListener);
            }
        }
        if (breakpointResolvedHandler != null) {
            DebuggerManager.getDebuggerManager().removeBreakpoint(breakpointResolvedHandler);
        }
        TruffleBreakpointsRegistry.getDefault().dispose(debugger);
    }
    
    private void setBreakpointResolvedHandler(ClassType accessorClass) {
        synchronized (breakpointResolvedHandlerLock) {
            if (this.breakpointResolvedHandler == null) {
                MethodBreakpoint methodBreakpoint = MethodBreakpoint.create(accessorClass.name(), ACCESSOR_LINE_BREAKPOINT_RESOLVED);
                methodBreakpoint.setSession(debugger);
                configureTruffleBreakpoint(methodBreakpoint);
                methodBreakpoint.setSuspend(JPDABreakpoint.SUSPEND_EVENT_THREAD);
                methodBreakpoint.addJPDABreakpointListener(new JPDABreakpointListener() {
                    @Override
                    public void breakpointReached(JPDABreakpointEvent event) {
                        LocalVariable[] localVariables;
                        try {
                            CallStackFrame[] topFrame = event.getThread().getCallStack(0, 1);
                            localVariables = topFrame[0].getLocalVariables();
                        } catch (AbsentInformationException ex) {
                            localVariables = null;
                        }
                        if (localVariables != null) {
                            JSLineBreakpoint breakpoint = findBreakpoint((ObjectReference) ((JDIVariable) localVariables[0]).getJDIValue());
                            if (breakpoint != null) {
                                int line = (int) localVariables[1].createMirrorObject();
                                breakpoint.setLine(line);
                                JSBreakpointStatus.setValid(breakpoint, "resolved");
                            }
                        }
                        event.resume();
                    }
                });
                DebuggerManager.getDebuggerManager().addBreakpoint(methodBreakpoint);
                this.breakpointResolvedHandler = methodBreakpoint;
            }
        }
    }

    private JSLineBreakpoint findBreakpoint(ObjectReference bpValue) {
        synchronized (breakpointsMap) {
            for (Map.Entry<JSLineBreakpoint, Set<ObjectReference>> bpEntry : breakpointsMap.entrySet()) {
                if (bpEntry.getValue().contains(bpValue)) {
                    return bpEntry.getKey();
                }
            }
        }
        return null;
    }

    /**
     * Call in method invoking
     */
    public void submitBreakpoints(ClassType accessorClass, ObjectReference debugManager, JPDAThreadImpl t) throws InvocationException {
        assert t.isMethodInvoking();
        this.accessorClass = accessorClass;
        setBreakpointResolvedHandler(accessorClass);
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        initialBreakpointsSubmitted = true;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "DebugManagerHandler: Breakpoints to submit = {0}", breakpoints);
        }
        Map<JSLineBreakpoint, ObjectReference> bpsMap = new HashMap<>();
        for (Breakpoint breakpoint : breakpoints) {
            if (!(breakpoint instanceof JSLineBreakpoint)) {
                continue;
            }
            JSLineBreakpoint bp = (JSLineBreakpoint) breakpoint;
            FileObject fileObject = bp.getFileObject();
            if (fileObject == null) {
                continue;
            }
            URI uri = Source.getTruffleInternalURI(fileObject);
            if (uri == null) {
                uri = SourceBinaryTranslator.source2Binary(fileObject);
            }
            ObjectReference bpImpl;
            if (bp.isEnabled()) {
                bpImpl = setLineBreakpoint(debugManager, t, uri, bp.getLineNumber(),
                                           getIgnoreCount(bp), bp.getCondition());
                TruffleBreakpointsRegistry.getDefault().add(debugger, bp, bpImpl);
                // Find out whether the breakpoint was resolved already during the submission:
                try {
                    updateResolved(bp, bpImpl, t.getThreadReference());
                } catch (Exception ex) {
                    Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Testing resolved breakpoint at "+uri+":"+bp.getLineNumber()));
                }
            } else {
                bpImpl = null;
            }
            bpsMap.put(bp, bpImpl);
            bp.addPropertyChangeListener(breakpointsChangeListener);
        }
        synchronized (breakpointsMap) {
            for (Map.Entry<JSLineBreakpoint, ObjectReference> bpEntry : bpsMap.entrySet()) {
                Set<ObjectReference> impls = breakpointsMap.get(bpEntry.getKey());
                if (impls == null) {
                    impls = new HashSet<ObjectReference>();
                    breakpointsMap.put(bpEntry.getKey(), impls);
                }
                impls.add(bpEntry.getValue());
            }
        }
    }

    private void updateResolved(JSLineBreakpoint breakpoint, ObjectReference bp, ThreadReference tr) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ClassNotPreparedExceptionWrapper, InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, ObjectCollectedExceptionWrapper {
        ClassType breakpointClass = (ClassType) bp.referenceType();
        Method isResolvedMethod = ClassTypeWrapper.concreteMethodByName(breakpointClass, "isResolved", "()Z");
        BooleanValue isResolvedValue = (BooleanValue) ObjectReferenceWrapper.invokeMethod(bp, tr, isResolvedMethod, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
        if (isResolvedValue.value()) {
            JSBreakpointStatus.setValid(breakpoint, "resolved");
        }
    }

    private static int getIgnoreCount(JSLineBreakpoint bp) {
        int ignoreCount = 0;
        if (Breakpoint.HIT_COUNT_FILTERING_STYLE.GREATER.equals(bp.getHitCountFilteringStyle())) {
            ignoreCount = bp.getHitCountFilter();
        }
        return ignoreCount;
    }
    
    private ObjectReference setLineBreakpoint(ObjectReference debugManager,
                                              JPDAThreadImpl t, URI uri, int line,
                                              int ignoreCount, String condition) throws InvocationException {
        assert t.isMethodInvoking();
        ThreadReference tr = t.getThreadReference();
        VirtualMachine vm = tr.virtualMachine();
        PersistentValues persistents = new PersistentValues(vm);
        try {
            Method setLineBreakpointMethod = ClassTypeWrapper.concreteMethodByName(
                    accessorClass,
                    ACCESSOR_SET_LINE_BREAKPOINT,
                    ACCESSOR_SET_LINE_BREAKPOINT_MGR_SIGNAT);
            if (setLineBreakpointMethod == null) {
                throw new IllegalStateException("Method "+ACCESSOR_SET_LINE_BREAKPOINT+" with signature:\n"+ACCESSOR_SET_LINE_BREAKPOINT_MGR_SIGNAT+"\nis not present in accessor class "+accessorClass);
            }
            Value uriRef = persistents.mirrorOf(uri.toString());
            IntegerValue lineRef = vm.mirrorOf(line);
            IntegerValue icRef = vm.mirrorOf(ignoreCount);
            StringReference conditionRef = (condition != null) ? persistents.mirrorOf(condition) : null;
            List<? extends Value> args = Arrays.asList(new Value[] { debugManager, uriRef, lineRef, icRef, conditionRef });
            ObjectReference ret = (ObjectReference) ClassTypeWrapper.invokeMethod(
                    accessorClass,
                    tr,
                    setLineBreakpointMethod,
                    args,
                    ObjectReference.INVOKE_SINGLE_THREADED);
            ret.disableCollection();
            return ret;
        } catch (VMDisconnectedExceptionWrapper | InternalExceptionWrapper |
                 ClassNotLoadedException | ClassNotPreparedExceptionWrapper |
                 IncompatibleThreadStateException | InvalidTypeException |
                 UnsupportedOperationExceptionWrapper | ObjectCollectedExceptionWrapper ex) {
            Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Setting breakpoint to "+uri+":"+line));
            return null;
        } finally {
            persistents.collect();
        }
    }
    
    private void submitBP(JSLineBreakpoint bp) {
        FileObject fileObject = bp.getFileObject();
        if (fileObject == null) {
            return ;
        }
        final URI uri;
        URI tiuri = Source.getTruffleInternalURI(fileObject);
        if (tiuri != null) {
            uri = tiuri;
        } else {
            uri = SourceBinaryTranslator.source2Binary(fileObject);
        }
        final int line = bp.getLineNumber();
        final int ignoreCount = getIgnoreCount(bp);
        final String condition = bp.getCondition();
        final ArrayReference[] bpRef = new ArrayReference[] { null };
        if (bp.isEnabled()) {
            try {
                final Method setLineBreakpointMethod = ClassTypeWrapper.concreteMethodByName(
                        accessorClass,
                        ACCESSOR_SET_LINE_BREAKPOINT,
                        ACCESSOR_SET_LINE_BREAKPOINT_SIGNAT);
                if (setLineBreakpointMethod == null) {
                    throw new IllegalStateException("Method "+ACCESSOR_SET_LINE_BREAKPOINT+" with signature:\n"+ACCESSOR_SET_LINE_BREAKPOINT_SIGNAT+"\nis not present in accessor class "+accessorClass);
                }
                TruffleAccess.methodCallingAccess(debugger, new TruffleAccess.MethodCallsAccess() {
                    @Override
                    public void callMethods(JPDAThread thread) throws InvocationException {
                        ThreadReference tr = ((JPDAThreadImpl) thread).getThreadReference();
                        VirtualMachine vm = tr.virtualMachine();
                        PersistentValues persistents = new PersistentValues(vm);
                        try {
                            StringReference uriRef = persistents.mirrorOf(uri.toString());
                            IntegerValue lineRef = vm.mirrorOf(line);
                            IntegerValue icRef = vm.mirrorOf(ignoreCount);
                            StringReference conditionRef = (condition != null) ? persistents.mirrorOf(condition) : null;
                            List<? extends Value> args = Arrays.asList(new Value[] { uriRef, lineRef, icRef, conditionRef });
                            ArrayReference ret = (ArrayReference) ClassTypeWrapper.invokeMethod(
                                    accessorClass,
                                    tr,
                                    setLineBreakpointMethod,
                                    args,
                                    ObjectReference.INVOKE_SINGLE_THREADED);
                            ret.disableCollection();
                            bpRef[0] = ret;
                            // Find out whether the breakpoint was resolved already during the submission:
                            for (Value v : ret.getValues()) {
                                if (v instanceof ObjectReference) {
                                    TruffleBreakpointsRegistry.getDefault().add(debugger, bp, (ObjectReference) v);
                                    updateResolved(bp, (ObjectReference) v, tr);
                                }
                            }
                        } catch (InvalidTypeException | ClassNotLoadedException |
                                 IncompatibleThreadStateException | UnsupportedOperationExceptionWrapper |
                                 InternalExceptionWrapper | VMDisconnectedExceptionWrapper |
                                 ObjectCollectedExceptionWrapper | ClassNotPreparedExceptionWrapper ex) {
                            Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Setting breakpoint to "+uri+":"+line));
                        } finally {
                            persistents.collect();
                        }
                    }
                });
            } catch (ClassNotPreparedExceptionWrapper | InternalExceptionWrapper |
                     VMDisconnectedExceptionWrapper ex) {
            }
        }
        bp.addPropertyChangeListener(breakpointsChangeListener);
        Set<ObjectReference> breakpoints = new HashSet<>();
        ArrayReference bpArray = bpRef[0];
        if (bpArray != null) {
            List<Value> values = bpArray.getValues();
            for (Value v : values) {
                if (v instanceof ObjectReference) {
                    breakpoints.add((ObjectReference) v);
                }
            }
            bpArray.enableCollection();
        }
        if (!breakpoints.isEmpty()) {
            synchronized (breakpointsMap) {
                breakpointsMap.put(bp, breakpoints);
            }
        }
    }
    
    private boolean removeBP(final JSLineBreakpoint bp) {
        bp.removePropertyChangeListener(breakpointsChangeListener);
        final Set<ObjectReference> bpImpls;
        synchronized (breakpointsMap) {
            bpImpls = breakpointsMap.remove(bp);
        }
        if (bpImpls == null) {
            return false;
        }
        final boolean[] successPtr = new boolean[] { false };
        try {
            final Method removeLineBreakpointMethod = ClassTypeWrapper.concreteMethodByName(
                    accessorClass,
                    ACCESSOR_REMOVE_BREAKPOINT,
                    ACCESSOR_REMOVE_BREAKPOINT_SIGNAT);
            TruffleAccess.methodCallingAccess(debugger, new TruffleAccess.MethodCallsAccess() {
                @Override
                public void callMethods(JPDAThread thread) throws InvocationException {
                    ThreadReference tr = ((JPDAThreadImpl) thread).getThreadReference();
                    try {
                        for (ObjectReference bpImpl : bpImpls) {
                            List<? extends Value> args = Arrays.asList(new Value[] { bpImpl });
                            try {
                                ClassTypeWrapper.invokeMethod(
                                        accessorClass,
                                        tr,
                                        removeLineBreakpointMethod,
                                        args,
                                        ObjectReference.INVOKE_SINGLE_THREADED);
                                successPtr[0] = true;
                            } catch (InvalidTypeException | ClassNotLoadedException |
                                     IncompatibleThreadStateException |
                                     InternalExceptionWrapper |
                                     ObjectCollectedExceptionWrapper ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            TruffleBreakpointsRegistry.getDefault().remove(debugger, bpImpl);
                            bpImpl.enableCollection();
                        }
                    } catch (VMDisconnectedExceptionWrapper ex) {}
                }
            });
        } catch (ClassNotPreparedExceptionWrapper | InternalExceptionWrapper |
                 VMDisconnectedExceptionWrapper ex) {
        }
        return successPtr[0];
    }
    
    private boolean setBreakpointProperty(JSLineBreakpoint bp,
                                          TruffleBPMethods method,
                                          final List<? extends Value> args) {
        final Set<ObjectReference> bpImpls;
        synchronized (breakpointsMap) {
            bpImpls = breakpointsMap.get(bp);
        }
        if (bpImpls == null) {
            if (bp.isEnabled()) {
                submitBP(bp);
                return true;
            } else {
                return false;
            }
        }
        final boolean[] successPtr = new boolean[] { false };
        try {
            final Method setBreakpointPropertyMethod = ClassTypeWrapper.concreteMethodByName(
                    (ClassType) ObjectReferenceWrapper.referenceType(bpImpls.iterator().next()),
                    method.getMethodName(),
                    method.getMethodSignature());
            TruffleAccess.methodCallingAccess(debugger, new TruffleAccess.MethodCallsAccess() {
                @Override
                public void callMethods(JPDAThread thread) throws InvocationException {
                    ThreadReference tr = ((JPDAThreadImpl) thread).getThreadReference();
                    try {
                        for (ObjectReference bpImpl : bpImpls) {
                            try {
                                ObjectReferenceWrapper.invokeMethod(
                                        bpImpl,
                                        tr,
                                        setBreakpointPropertyMethod,
                                        args,
                                        ObjectReference.INVOKE_SINGLE_THREADED);
                                successPtr[0] = true;
                            } catch (InvalidTypeException | ClassNotLoadedException |
                                     IncompatibleThreadStateException |
                                     InternalExceptionWrapper |
                                     ObjectCollectedExceptionWrapper ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    } catch (VMDisconnectedExceptionWrapper ex) {}
                }
            });
        } catch (ClassNotPreparedExceptionWrapper | InternalExceptionWrapper |
                 ObjectCollectedExceptionWrapper | VMDisconnectedExceptionWrapper ex) {
        }
        return successPtr[0];
    }

    public void breakpointAdded(JSLineBreakpoint jsLineBreakpoint) {
        if (initialBreakpointsSubmitted) {
            // Breakpoints were submitted already, submit this as well.
            if (Mutex.EVENT.isReadAccess()) {
                // Lazy submit when called from UI
                ((JPDADebuggerImpl) debugger).getRequestProcessor().post(() -> submitBP(jsLineBreakpoint));
            } else {
                submitBP(jsLineBreakpoint);
            }
        }
    }

    public void breakpointRemoved(JSLineBreakpoint jsLineBreakpoint) {
        if (initialBreakpointsSubmitted) {
            // Breakpoints were submitted already, remove this.
            if (Mutex.EVENT.isReadAccess()) {
                // Lazy remove when called from UI
                ((JPDADebuggerImpl) debugger).getRequestProcessor().post(() -> removeBP(jsLineBreakpoint));
            } else {
                removeBP(jsLineBreakpoint);
            }
        }
    }
    
    private static enum TruffleBPMethods {
        setEnabled,
        setIgnoreCount,
        setCondition;
        
        public String getMethodName() {
            return name();
        }
        
        public String getMethodSignature() {
            switch (this) {
                case setEnabled:
                    return "(Z)V";
                case setIgnoreCount:
                    return "(I)V";
                case setCondition:
                    return "(Ljava/lang/String;)V";
                default:
                    throw new IllegalStateException(this.name());
            }
        }
    }
    
    private class JSBreakpointPropertyChangeListener implements PropertyChangeListener {
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final JSLineBreakpoint jsbp = (JSLineBreakpoint) evt.getSource();
            String propertyName = evt.getPropertyName();
            final TruffleBPMethods method;
            final List<? extends Value> args;
            final VirtualMachine vm = ((JPDADebuggerImpl) debugger).getVirtualMachine();
            if (vm == null) {
                return ;
            }
            PersistentValues persistents = new PersistentValues(vm);
            switch (propertyName) {
                case JSLineBreakpoint.PROP_ENABLED:
                    method = TruffleBPMethods.setEnabled;
                    args = Collections.singletonList(vm.mirrorOf(jsbp.isEnabled()));
                    break;
                case JSLineBreakpoint.PROP_CONDITION:
                    method = TruffleBPMethods.setCondition;
                    String condition = jsbp.getCondition();
                    StringReference conditionRef = (condition != null) ? persistents.mirrorOf0(condition) : null;
                    args = Collections.singletonList(conditionRef);
                    break;
                case Breakpoint.PROP_HIT_COUNT_FILTER:
                    method = TruffleBPMethods.setIgnoreCount;
                    args = Collections.singletonList(vm.mirrorOf(getIgnoreCount(jsbp)));
                    break;
                default:
                    return ;
            }
            ((JPDADebuggerImpl) debugger).getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        setBreakpointProperty(jsbp, method, args);
                    } finally {
                        persistents.collect();
                    }
                }
            });
        }
        
    }
}
