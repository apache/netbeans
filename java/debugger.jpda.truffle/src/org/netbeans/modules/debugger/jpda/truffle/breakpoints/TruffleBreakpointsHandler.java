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

package org.netbeans.modules.debugger.jpda.truffle.breakpoints;

import com.sun.jdi.ArrayReference;
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
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.source.Source;
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
    
    private final JPDADebugger debugger;
    private ClassType accessorClass;
    
    private volatile boolean initialBreakpointsSubmitted = false;
    private final Map<JSLineBreakpoint, Set<ObjectReference>> breakpointsMap = new HashMap<>();
    private final JSBreakpointPropertyChangeListener breakpointsChangeListener = new JSBreakpointPropertyChangeListener();
    
    public TruffleBreakpointsHandler(JPDADebugger debugger) {
        this.debugger = debugger;
    }

    public void destroy() {
        synchronized (breakpointsMap) {
            for (JSLineBreakpoint jsbp : breakpointsMap.keySet()) {
                jsbp.removePropertyChangeListener(breakpointsChangeListener);
            }
        }
    }
    
    /**
     * Call in method invoking
     */
    public void submitBreakpoints(ClassType accessorClass, ObjectReference debugManager, JPDAThreadImpl t) throws InvocationException {
        assert t.isMethodInvoking();
        this.accessorClass = accessorClass;
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
                uri = fileObject.toURI();
            }
            ObjectReference bpImpl;
            if (bp.isEnabled()) {
                bpImpl = setLineBreakpoint(debugManager, t, uri, bp.getLineNumber(),
                                           getIgnoreCount(bp), bp.getCondition());
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
        try {
            Method setLineBreakpointMethod = ClassTypeWrapper.concreteMethodByName(
                    accessorClass,
                    ACCESSOR_SET_LINE_BREAKPOINT,
                    ACCESSOR_SET_LINE_BREAKPOINT_MGR_SIGNAT);
            if (setLineBreakpointMethod == null) {
                throw new IllegalStateException("Method "+ACCESSOR_SET_LINE_BREAKPOINT+" with signature:\n"+ACCESSOR_SET_LINE_BREAKPOINT_MGR_SIGNAT+"\nis not present in accessor class "+accessorClass);
            }
            Value uriRef = vm.mirrorOf(uri.toString());
            IntegerValue lineRef = vm.mirrorOf(line);
            IntegerValue icRef = vm.mirrorOf(ignoreCount);
            StringReference conditionRef = (condition != null) ? vm.mirrorOf(condition) : null;
            List<? extends Value> args = Arrays.asList(new Value[] { debugManager, uriRef, lineRef, icRef, conditionRef });
            ObjectReference ret = (ObjectReference) ClassTypeWrapper.invokeMethod(
                    accessorClass,
                    tr,
                    setLineBreakpointMethod,
                    args,
                    ObjectReference.INVOKE_SINGLE_THREADED);
            return ret;
        } catch (VMDisconnectedExceptionWrapper | InternalExceptionWrapper |
                 ClassNotLoadedException | ClassNotPreparedExceptionWrapper |
                 IncompatibleThreadStateException | InvalidTypeException |
                 ObjectCollectedExceptionWrapper ex) {
            Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Setting breakpoint to "+uri+":"+line));
            return null;
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
            uri = fileObject.toURI();
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
                        StringReference uriRef = vm.mirrorOf(uri.toString());
                        IntegerValue lineRef = vm.mirrorOf(line);
                        IntegerValue icRef = vm.mirrorOf(ignoreCount);
                        StringReference conditionRef = (condition != null) ? vm.mirrorOf(condition) : null;
                        List<? extends Value> args = Arrays.asList(new Value[] { uriRef, lineRef, icRef, conditionRef });
                        try {
                            ArrayReference ret = (ArrayReference) ClassTypeWrapper.invokeMethod(
                                    accessorClass,
                                    tr,
                                    setLineBreakpointMethod,
                                    args,
                                    ObjectReference.INVOKE_SINGLE_THREADED);
                            bpRef[0] = ret;
                        } catch (InvalidTypeException | ClassNotLoadedException |
                                 IncompatibleThreadStateException |
                                 InternalExceptionWrapper | VMDisconnectedExceptionWrapper |
                                 ObjectCollectedExceptionWrapper ex) {
                            Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Setting breakpoint to "+uri+":"+line));
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
            switch (propertyName) {
                case JSLineBreakpoint.PROP_ENABLED:
                    method = TruffleBPMethods.setEnabled;
                    args = Collections.singletonList(vm.mirrorOf(jsbp.isEnabled()));
                    break;
                case JSLineBreakpoint.PROP_CONDITION:
                    method = TruffleBPMethods.setCondition;
                    String condition = jsbp.getCondition();
                    StringReference conditionRef = (condition != null) ? vm.mirrorOf(condition) : null;
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
                    setBreakpointProperty(jsbp, method, args);
                }
            });
        }
        
    }
}
