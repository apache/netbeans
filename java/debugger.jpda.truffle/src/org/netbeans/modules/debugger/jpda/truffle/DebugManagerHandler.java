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

package org.netbeans.modules.debugger.jpda.truffle;

import com.sun.jdi.BooleanValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.InvocationExceptionTranslated;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.UnsupportedOperationExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import static org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess.BASIC_CLASS_NAME;
import org.netbeans.modules.debugger.jpda.truffle.breakpoints.impl.TruffleBreakpointsHandler;
import org.netbeans.modules.debugger.jpda.truffle.options.TruffleOptions;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.openide.util.Exceptions;

/**
 * Handles remote JPDATruffleDebugManager.
 */
final class DebugManagerHandler {
    
    private static final Logger LOG = Logger.getLogger(DebugManagerHandler.class.getName());
    
    private static final String ACCESSOR_START_ACCESS_LOOP = "startAccessLoop"; // NOI18N
    private static final String ACCESSOR_LOOP_RUNNING_FIELD = "accessLoopRunning";  // NOI18N
    private static final String ACCESSOR_SET_UP_DEBUG_MANAGER_FOR = "setUpDebugManagerFor"; // NOI18N
    private static final String ACCESSOR_SET_UP_DEBUG_MANAGER_FOR_SGN =
            "(L"+Object.class.getName().replace('.', '/')+";ZZ)"+                // NOI18N
            "Lorg/netbeans/modules/debugger/jpda/backend/truffle/JPDATruffleDebugManager;"; // NOI18N
    private static final String ACCESSOR_SET_INCLUDE_INTERNAL = "setIncludeInternal"; // NOI18N
    private static final String ACCESSOR_SET_INCLUDE_INTERNAL_SGN = "(Z)V"; // NOI18N
    
    private static final Map<JPDADebugger, Boolean> dbgStepInto = Collections.synchronizedMap(new WeakHashMap<JPDADebugger, Boolean>());

    private final JPDADebugger debugger;
    private final AtomicBoolean inited = new AtomicBoolean(false);
    private ClassType accessorClass;
    private JPDAClassType accessorJPDAClass;
    private final Object accessorClassLock = new Object();
    //private ObjectReference debugManager;
    private final TruffleBreakpointsHandler breakpointsHandler;
    private final PropertyChangeListener optionsChangeListener = new OptionsChangeListener();
    
    public DebugManagerHandler(JPDADebugger debugger) {
        this.debugger = debugger;
        this.breakpointsHandler = new TruffleBreakpointsHandler(debugger);
        TruffleOptions.onLanguageDeveloperModeChange(optionsChangeListener);
    }
    
    static void execStepInto(JPDADebugger debugger, boolean doStepInto) {
        if (doStepInto) {
            dbgStepInto.put(debugger, doStepInto);
        } else {
            dbgStepInto.remove(debugger);
        }
    }
    
    private boolean isStepInto() {
        Boolean stepInto = dbgStepInto.get(debugger);
        return stepInto != null && stepInto;
    }
    
    void newPolyglotEngineInstance(ObjectReference engine, JPDAThreadImpl thread) {
        LOG.log(Level.FINE, "Engine created breakpoint hit: engine = {0} in thread = {1}", new Object[] { engine, thread.getThreadReference()});
        assert inited.get(): "The remote services should have been initialized already from a Truffle class.";
        if (accessorClass == null) {
            // No accessor
            return ;
        }
        InvocationExceptionTranslated iextr = null;
        try {
            // Create an instance of JPDATruffleDebugManager in the backend
            // and submit breakpoints:
            thread.notifyMethodInvoking();
            VirtualMachine vm = ((JPDADebuggerImpl) debugger).getVirtualMachine();
            if (vm == null) {
                return ;
            }
            BooleanValue includeInternal = vm.mirrorOf(TruffleOptions.isLanguageDeveloperMode());
            BooleanValue doStepInto = vm.mirrorOf(isStepInto());
            Method debugManagerMethod = ClassTypeWrapper.concreteMethodByName(
                    accessorClass,
                    ACCESSOR_SET_UP_DEBUG_MANAGER_FOR,
                    ACCESSOR_SET_UP_DEBUG_MANAGER_FOR_SGN);
            ThreadReference tr = thread.getThreadReference();
            List<Value> dmArgs = Arrays.asList(engine, includeInternal, doStepInto);
            LOG.log(Level.FINE, "Setting engine and step into = {0}", isStepInto());
            Object ret = ClassTypeWrapper.invokeMethod(accessorClass, tr, debugManagerMethod, dmArgs, ObjectReference.INVOKE_SINGLE_THREADED);
            if (ret instanceof ObjectReference) {   // Can be null when an existing debug manager is reused.
                //debugManager = (ObjectReference) ret;
                breakpointsHandler.submitBreakpoints(accessorClass, (ObjectReference) ret, thread);
            }
        } catch (VMDisconnectedExceptionWrapper vmd) {
        } catch (InvocationException iex) {
            iextr = new InvocationExceptionTranslated(iex, thread.getDebugger());
            Exceptions.printStackTrace(iex);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            thread.notifyMethodInvokeDone();
        }
        if (iextr != null) {
            iextr.preload(thread);
            Exceptions.printStackTrace(iextr);
        }
    }

    void initDebuggerRemoteService(JPDAThread thread) {
        if (inited.compareAndSet(false, true)) {
            long now = System.currentTimeMillis();
            doInitDebuggerRemoteService(thread);
            long took = System.currentTimeMillis() - now;
            LOG.log(Level.FINE, "doInitDebuggerRemoteService took {0} ms", took);
        }
    }

    private void doInitDebuggerRemoteService(JPDAThread thread) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "initDebuggerRemoteService({0})", thread);
        }
        JPDAThreadImpl t = (JPDAThreadImpl) thread;
        Lock writeLock = t.accessLock.writeLock();
        writeLock.lock();
        try {
            ClassObjectReference cor = null;
            try {
                cor = RemoteServices.uploadBasicClasses(t, BASIC_CLASS_NAME);
            } catch (PropertyVetoException |
                     InvalidTypeException |
                     ClassNotLoadedException |
                     IncompatibleThreadStateException |
                     IOException pvex) {
                Exceptions.printStackTrace(pvex);
            } catch (InvocationException ex) {
                Exceptions.printStackTrace(ex);
                final InvocationExceptionTranslated iextr = new InvocationExceptionTranslated(ex, t.getDebugger());
                iextr.preload(t);
                Exceptions.printStackTrace(iextr);
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Uploaded class = {0}", cor);
            }
            if (cor == null) {
                return ;
            }
            ThreadReference tr = t.getThreadReference();
            
            ClassType serviceClass = (ClassType) ClassObjectReferenceWrapper.reflectedType(cor);//RemoteServices.getClass(vm, "org.netbeans.modules.debugger.jpda.visual.remote.RemoteService");

            InvocationExceptionTranslated iextr = null;
            Method startMethod = ClassTypeWrapper.concreteMethodByName(serviceClass, ACCESSOR_START_ACCESS_LOOP, "()Ljava/lang/Thread;");
            if (startMethod == null) {
                LOG.log(Level.WARNING, "Could not start the access loop of "+serviceClass+", no "+ACCESSOR_START_ACCESS_LOOP+" method.");
                return ;
            }
            try {
                t.notifyMethodInvoking();
                Value ret = ClassTypeWrapper.invokeMethod(serviceClass, tr, startMethod, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                if (ret instanceof ThreadReference) {
                    RemoteServices.setAccessLoopStarted(t.getDebugger(), (ThreadReference) ret);
                } else {
                    LOG.log(Level.WARNING, "Could not start the access loop of "+serviceClass);
                    return ;
                }
                TruffleAccess.assureBPSet(debugger, serviceClass);
                JPDAClassType serviceJPDAClass = ((JPDADebuggerImpl) debugger).getClassType(serviceClass);
                synchronized (accessorClassLock) {
                    accessorClass = serviceClass;
                    accessorJPDAClass = serviceJPDAClass;
                }
            } catch (InvocationException iex) {
                iextr = new InvocationExceptionTranslated(iex, t.getDebugger());
                Exceptions.printStackTrace(iex);
            } catch (ClassNotLoadedException |
                     IncompatibleThreadStateException |
                     InvalidTypeException |
                     PropertyVetoException |
                     InternalExceptionWrapper |
                     ObjectCollectedExceptionWrapper ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                t.notifyMethodInvokeDone();
                ObjectReferenceWrapper.enableCollection(cor);
            }
            if (iextr != null) {
                iextr.preload(t);
                Exceptions.printStackTrace(iextr);
            }
        } catch (InternalExceptionWrapper iex) {
        } catch (ClassNotPreparedExceptionWrapper cnpex) {
            Exceptions.printStackTrace(cnpex);
        } catch (ObjectCollectedExceptionWrapper collex) {
        } catch (UnsupportedOperationExceptionWrapper uex) {
            LOG.log(Level.INFO, uex.getLocalizedMessage(), uex);
        } catch (VMDisconnectedExceptionWrapper vmd) {
        } finally {
            writeLock.unlock();
        }
        if (LOG.isLoggable(Level.FINE)) {
            try {
                LOG.fine("The JPDATruffleAccessor is there: "+
                            RemoteServices.getClass(t.getThreadReference().virtualMachine(),
                         BASIC_CLASS_NAME));
            } catch (Exception ex) {
                LOG.log(Level.FINE, "", ex);
            }
        }
    }
    
    ClassType getAccessorClass() {
        synchronized (accessorClassLock) {
            return accessorClass;
        }
    }
    
    JPDAClassType getAccessorJPDAClass() {
        synchronized (accessorClassLock) {
            return accessorJPDAClass;
        }
    }
    
    void destroy() {
        breakpointsHandler.destroy();
        if (accessorClass == null) {
            return ;
        }
        try {
            Field accessLoopRunning = ReferenceTypeWrapper.fieldByName(accessorClass, ACCESSOR_LOOP_RUNNING_FIELD);
            if (accessLoopRunning != null) {
                ClassTypeWrapper.setValue(accessorClass, accessLoopRunning,
                                          VirtualMachineWrapper.mirrorOf(accessorClass.virtualMachine(), false));
                RemoteServices.interruptServiceAccessThread(debugger);
            }
        } catch (VMDisconnectedExceptionWrapper vdex) {
            // Ignore
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    void breakpointAdded(JSLineBreakpoint jsLineBreakpoint) {
        breakpointsHandler.breakpointAdded(jsLineBreakpoint);
    }

    void breakpointRemoved(JSLineBreakpoint jsLineBreakpoint) {
        breakpointsHandler.breakpointRemoved(jsLineBreakpoint);
    }

    private final class OptionsChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (accessorClass == null) {
                // No accessor
                return ;
            }
            if (TruffleOptions.PROPERTY_LANG_DEV_MODE.equals(evt.getPropertyName())) {
                JPDADebuggerImpl debuggerImpl = (JPDADebuggerImpl) debugger;
                debuggerImpl.getRequestProcessor().post(() -> {
                    VirtualMachine vm = debuggerImpl.getVirtualMachine();
                    if (vm == null) {
                        return ;
                    }
                    BooleanValue includeInternal = vm.mirrorOf(TruffleOptions.isLanguageDeveloperMode());
                    try {
                        Method debugManagerMethod = ClassTypeWrapper.concreteMethodByName(
                                accessorClass,
                                ACCESSOR_SET_INCLUDE_INTERNAL,
                                ACCESSOR_SET_INCLUDE_INTERNAL_SGN);
                        TruffleAccess.methodCallingAccess(debugger, new TruffleAccess.MethodCallsAccess() {
                            @Override
                            public void callMethods(JPDAThread thread) throws InvocationException {
                                ThreadReference tr = ((JPDAThreadImpl) thread).getThreadReference();
                                List<Value> dmArgs = Arrays.asList(includeInternal);
                                LOG.log(Level.FINE, "Setting includeInternal to {0}", includeInternal.value());
                                try {
                                    ClassTypeWrapper.invokeMethod(accessorClass, tr, debugManagerMethod, dmArgs, ObjectReference.INVOKE_SINGLE_THREADED);
                                } catch (Exception ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        });
                    } catch (ClassNotPreparedExceptionWrapper | InternalExceptionWrapper | VMDisconnectedExceptionWrapper ex) {
                        Exceptions.printStackTrace(ex);
                    }
                });
            }
        }
        
    }
}
