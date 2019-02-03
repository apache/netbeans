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

import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidRequestStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MethodWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.LocatableEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAClassTypeImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.actions.PauseInGraalScriptActionProvider;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.util.Exceptions;

/**
 * Initiates guest language debugging, detects Engine in the JVM.
 */
@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public class TruffleDebugManager extends DebuggerManagerAdapter {
    
    private static final Logger LOG = Logger.getLogger(TruffleDebugManager.class.getName());
    
    private static final String SESSION_CREATION_BP_CLASS = "org.graalvm.polyglot.Engine";
    // Breakpoint on this class triggers search of existing engines
    private static final String EXISTING_ENGINES_TRIGGER = "com.oracle.truffle.api.frame.FrameSlot";
    
    private JPDABreakpoint debugManagerLoadBP;
    private static final Map<JPDADebugger, Boolean> haveExistingEnginesTrigger = new WeakHashMap<>();
    private static final Map<JPDADebugger, DebugManagerHandler> dmHandlers = new HashMap<>();
    private static final Map<JPDADebugger, JPDABreakpointListener> debugBPListeners = new HashMap<>();
    
    public TruffleDebugManager() {
    }
    
    @Override
    public Breakpoint[] initBreakpoints() {
        initLoadBP();
        return new Breakpoint[] { debugManagerLoadBP };
    }
    
    private synchronized void initLoadBP() {
        if (debugManagerLoadBP != null) {
            return ;
        }
        /* Must NOT use a method exit breakpoint! It caused a massive degradation of application performance.
        debugManagerLoadBP = MethodBreakpoint.create(SESSION_CREATION_BP_CLASS, SESSION_CREATION_BP_METHOD);
        ((MethodBreakpoint) debugManagerLoadBP).setBreakpointType(MethodBreakpoint.TYPE_METHOD_EXIT);
        */
        debugManagerLoadBP = ClassLoadUnloadBreakpoint.create(SESSION_CREATION_BP_CLASS, false, ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED);
        debugManagerLoadBP.setHidden(true);
        
        LOG.log(Level.FINE, "TruffleDebugManager.initBreakpoints(): submitted BP {0}", debugManagerLoadBP);
        TruffleAccess.init();
    }

    @Override
    public void sessionAdded(Session session) {
        JPDADebugger debugger = session.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            return ;
        }
        synchronized (dmHandlers) {
            if (dmHandlers.containsKey(debugger)) {
                // A new session for the same debugger?
                return ;
            }
        }
        initLoadBP();
        JPDABreakpointListener bpl = addPolyglotEngineCreationBP(debugger);
        LOG.log(Level.FINE, "TruffleDebugManager.sessionAdded({0}), adding BP listener to {1}", new Object[]{session, debugManagerLoadBP});
        synchronized (debugBPListeners) {
            debugBPListeners.put(debugger, bpl);
        }
    }

    @Override
    public void sessionRemoved(Session session) {
        JPDADebugger debugger = session.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            return ;
        }
        JPDABreakpointListener bpl;
        synchronized (debugBPListeners) {
            bpl = debugBPListeners.remove(debugger);
        }
        if (bpl != null) {
            LOG.log(Level.FINE, "TruffleDebugManager.engineRemoved({0}), removing BP listener from {1}", new Object[]{session, debugManagerLoadBP});
            debugManagerLoadBP.removeJPDABreakpointListener(bpl);
        }
        DebugManagerHandler dmh;
        synchronized (dmHandlers) {
            dmh = dmHandlers.remove(debugger);
        }
        if (dmh != null) {
            LOG.log(Level.FINE, "TruffleDebugManager.engineRemoved({0}), destroying {1}", new Object[]{session, dmh});
            dmh.destroy();
        }
    }

    private JPDABreakpointListener addPolyglotEngineCreationBP(final JPDADebugger debugger) {
        JPDABreakpointListener bpl = new JPDABreakpointListener() {
            @Override
            public void breakpointReached(JPDABreakpointEvent event) {
                try {
                    submitPECreationBP(debugger, event.getReferenceType());
                } finally {
                    event.resume();
                }
            }
        };
        debugManagerLoadBP.addJPDABreakpointListener(bpl);
        // Submit creation BPs for existing engine classes:
        Runnable submitEngineCreation = () -> {
            List<JPDAClassType> polyglotEngines = new ArrayList<>();
            //polyglotEngines.addAll(debugger.getClassesByName(SESSION_CREATION_BP_CLASS[0]));
            List<JPDAClassType> enginePe = debugger.getClassesByName(SESSION_CREATION_BP_CLASS);
            polyglotEngines.addAll(enginePe);
            for (JPDAClassType pe : polyglotEngines) {
                submitPECreationBP(debugger, ((JPDAClassTypeImpl) pe).getType());
                // TODO: Find possible existing instances of the engine
                // List<ObjectVariable> engines = pe.getInstances(0);
                // We have no suspended thread... :-(
            }
            // Find possible existing instances of the engine
            if (!enginePe.isEmpty() && debugger.canGetInstanceInfo()) {
                long engineInstances = 0;
                for (JPDAClassType pe : enginePe) {
                    engineInstances += pe.getInstanceCount();
                }
                if (engineInstances > 0) {
                    submitExistingEnginesProbe(debugger, enginePe);
                }
            }
        };
        if (debugger.getState() > 1) {
            submitEngineCreation.run();
        } else {
            debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (debugger.getState() > 1) {
                        submitEngineCreation.run();
                        debugger.removePropertyChangeListener(JPDADebugger.PROP_STATE, this);
                    }
                }
            });
        }
        return bpl;
    }

    private void submitPECreationBP(final JPDADebugger debugger, ReferenceType type) {
        try {
            List<Method> constructors = ReferenceTypeWrapper.methodsByName(type, "<init>");
            for (Method c : constructors) {
                if (!c.argumentTypeNames().isEmpty()) {
                    Location lastLocation = null;
                    Location l;
                    int i = 0;
                    // Search for the last (return) statement:
                    while ((l = MethodWrapper.locationOfCodeIndex(c, i)) != null) {
                        lastLocation = l;
                        i++;
                    }
                    BreakpointRequest bp = EventRequestManagerWrapper.createBreakpointRequest(lastLocation.virtualMachine().eventRequestManager(), lastLocation);
                    EventRequestWrapper.setSuspendPolicy(bp, EventRequest.SUSPEND_EVENT_THREAD);
                    ((JPDADebuggerImpl) debugger).getOperator().register(bp, new Executor() {
                        @Override
                        public boolean exec(Event event) {
                            try {
                                ThreadReference threadReference = LocatableEventWrapper.thread((LocatableEvent) event);
                                JPDAThreadImpl thread = ((JPDADebuggerImpl) debugger).getThread(threadReference);
                                StackFrame topFrame = ThreadReferenceWrapper.frame(threadReference, 0);
                                List<Value> argumentValues = topFrame.getArgumentValues();
                                if (argumentValues.get(0) == null) {
                                    // An empty constructor used for the builder only.
                                    return true;
                                }
                                ObjectReference engine = StackFrameWrapper.thisObject(topFrame);
                                haveNewPE(debugger, thread, engine);
                            } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper |
                                     ObjectCollectedExceptionWrapper ex) {
                            } catch (IllegalThreadStateExceptionWrapper |
                                     IncompatibleThreadStateException |
                                     InvalidStackFrameExceptionWrapper ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            return true;
                        }

                        @Override
                        public void removed(EventRequest eventRequest) {
                        }

                    });
                    try {
                        EventRequestWrapper.enable(bp);
                    } catch (InvalidRequestStateExceptionWrapper irsx) {
                        Exceptions.printStackTrace(irsx);
                    }
                }
            }
        } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper |
                 ObjectCollectedExceptionWrapper | ClassNotPreparedExceptionWrapper ex) {
        }
    }

    private void submitExistingEnginesProbe(final JPDADebugger debugger, List<JPDAClassType> enginePe) {
        synchronized (haveExistingEnginesTrigger) {
            if (Boolean.TRUE.equals(haveExistingEnginesTrigger.get(debugger))) {
                return ;
            }
            haveExistingEnginesTrigger.put(debugger, Boolean.TRUE);
        }
        MethodBreakpoint execTrigger = MethodBreakpoint.create(EXISTING_ENGINES_TRIGGER, "*");
        execTrigger.setHidden(true);
        execTrigger.setSession(debugger);
        execTrigger.addJPDABreakpointListener((event) -> {
            DebuggerManager.getDebuggerManager().removeBreakpoint(execTrigger);
            try {
                JPDAThreadImpl thread = (JPDAThreadImpl) event.getThread();
                boolean haveSomeEngine = false;
                for (JPDAClassType pe : enginePe) {
                    List<ObjectVariable> instances = pe.getInstances(0);
                    for (ObjectVariable obj : instances) {
                        Value value = ((JDIVariable) obj).getJDIValue();
                        if (value instanceof ObjectReference) {
                            haveNewPE(debugger, thread, (ObjectReference) value);
                            haveSomeEngine = true;
                        }
                    }
                }
                if (haveSomeEngine) {
                    for (ActionsProvider ap : ((JPDADebuggerImpl) debugger).getSession().lookup(null, ActionsProvider.class)) {
                        if (ap.getActions().contains(PauseInGraalScriptActionProvider.NAME)) {
                            // Trigger the enabling of the action as there are engines now:
                            ap.isEnabled(PauseInGraalScriptActionProvider.NAME);
                        }
                    }
                }
            } finally {
                event.resume();
            }
        });
        DebuggerManager.getDebuggerManager().addBreakpoint(execTrigger);
    }

    private void haveNewPE(JPDADebugger debugger, JPDAThreadImpl thread, ObjectReference engine) {
        DebugManagerHandler dmh;
        synchronized (dmHandlers) {
            dmh = dmHandlers.get(debugger);
            if (dmh == null) {
                dmh = new DebugManagerHandler(debugger);
                dmHandlers.put(debugger, dmh);
            }
        }
        dmh.newPolyglotEngineInstance(engine, thread);
    }

    @Override
    public void breakpointAdded(Breakpoint breakpoint) {
        if (breakpoint instanceof JSLineBreakpoint) {
            Collection<DebugManagerHandler> handlers;
            synchronized (dmHandlers) {
                handlers = new ArrayList<>(dmHandlers.values());
            }
            for (DebugManagerHandler dmh : handlers) {
                dmh.breakpointAdded((JSLineBreakpoint) breakpoint);
            }
        }
    }

    @Override
    public void breakpointRemoved(Breakpoint breakpoint) {
        if (breakpoint instanceof JSLineBreakpoint) {
            Collection<DebugManagerHandler> handlers;
            synchronized (dmHandlers) {
                handlers = new ArrayList<>(dmHandlers.values());
            }
            for (DebugManagerHandler dmh : handlers) {
                dmh.breakpointRemoved((JSLineBreakpoint) breakpoint);
            }
        }
    }
    
    public static ClassType getDebugAccessorClass(JPDADebugger debugger) {
        synchronized (dmHandlers) {
            DebugManagerHandler dmh = dmHandlers.get(debugger);
            if (dmh != null) {
                return dmh.getAccessorClass();
            } else {
                return null;
            }
        }
    }
    
    public static JPDAClassType getDebugAccessorJPDAClass(JPDADebugger debugger) {
        synchronized (dmHandlers) {
            DebugManagerHandler dmh = dmHandlers.get(debugger);
            if (dmh != null) {
                return dmh.getAccessorJPDAClass();
            } else {
                return null;
            }
        }
    }

}
