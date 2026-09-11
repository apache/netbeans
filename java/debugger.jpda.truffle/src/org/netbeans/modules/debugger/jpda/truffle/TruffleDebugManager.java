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
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
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
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.actions.PauseInGraalScriptActionProvider;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;

/**
 * Initiates guest language debugging, detects Engine in the JVM.
 */
@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public class TruffleDebugManager extends DebuggerManagerAdapter {

    private static final Logger LOG = Logger.getLogger(TruffleDebugManager.class.getName());

    private static final String ENGINE_CLASS = "org.graalvm.polyglot.Engine";   // NOI18N
    private static final String ENGINE_BUILDER_CLASS = "org.graalvm.polyglot.Engine$Builder";   // NOI18N
    private static final String POLY_IMPL_CLASS = "com.oracle.truffle.polyglot.PolyglotImpl";   // NOI18N
    private static final String POLY_IMPL_BUILD_METHOD = "buildEngine";   // NOI18N
    private static final String REMOTE_SERVICES_TRIGGER_CLASS = "com.oracle.truffle.api.Truffle";   // NOI18N
    private static final String REMOTE_SERVICES_TRIGGER_METHOD = "getRuntime";                      // NOI18N
    // Breakpoint on this class triggers search of existing engines
    private static final String EXISTING_ENGINES_TRIGGER = "com.oracle.truffle.api.frame.Frame";    // NOI18N

    private MethodBreakpoint debugManagerLoadBP;
    private Map<JPDADebugger, JPDABreakpoint> initServiceBPs = new HashMap<>();
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
        /*
          - Must NOT use a method exit breakpoint!
          - It caused a massive degradation of application performance.
          - we will attach an exit breakpoint, but the later the better
          - we need a method that's called the "latest" before returning
          - from `Engine.Builder.build()`
          - as of GraalVM 25.0.1 it is:
          - [this one](https://github.com/oracle/graal/blob/05ec02567a77a30cc41b8ad9174de0dc5737ceaf/sdk/src/org.graalvm.polyglot/src/org/graalvm/polyglot/Engine.java#L843)
        */
        debugManagerLoadBP = MethodBreakpoint.create(POLY_IMPL_CLASS, POLY_IMPL_BUILD_METHOD);
        debugManagerLoadBP.setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY);
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
        JPDABreakpoint bpService = addRemoteServiceInitBP(debugger);
        DebuggerManager.getDebuggerManager().addBreakpoint(bpService);
        JPDABreakpointListener bpl = addPolyglotEngineCreationBP(debugger);
        LOG.log(Level.FINE, "TruffleDebugManager.sessionAdded({0}), adding BP listener to {1}", new Object[]{session, debugManagerLoadBP});
        synchronized (debugBPListeners) {
            initServiceBPs.put(debugger, bpService);
            debugBPListeners.put(debugger, bpl);
        }
    }

    @Override
    public void sessionRemoved(Session session) {
        JPDADebugger debugger = session.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            return ;
        }
        JPDABreakpoint bpService;
        JPDABreakpointListener bpl;
        synchronized (debugBPListeners) {
            bpService = initServiceBPs.remove(debugger);
            bpl = debugBPListeners.remove(debugger);
        }
        if (bpService != null) {
            DebuggerManager.getDebuggerManager().removeBreakpoint(bpService);
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

    private JPDABreakpoint addRemoteServiceInitBP(final JPDADebugger debugger) {
        MethodBreakpoint bp = MethodBreakpoint.create(REMOTE_SERVICES_TRIGGER_CLASS, REMOTE_SERVICES_TRIGGER_METHOD);
        bp.setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY);
        bp.setHidden(true);
        bp.setSession(debugger);

        JPDABreakpointListener bpl = new JPDABreakpointListener() {
            @Override
            public void breakpointReached(JPDABreakpointEvent event) {
                try {
                    if (event.getDebugger() == debugger) {
                        DebugManagerHandler dmh = getDebugManagerHandler(debugger);
                        dmh.initDebuggerRemoteService(event.getThread());
                        DebuggerManager.getDebuggerManager().removeBreakpoint(bp);
                    }
                } finally {
                    event.resume();
                }
            }
        };
        bp.addJPDABreakpointListener(bpl);
        return bp;
    }

    private JPDABreakpointListener addPolyglotEngineCreationBP(final JPDADebugger debugger) {
        JPDABreakpointListener bpl = new JPDABreakpointListener() {
            @Override
            public void breakpointReached(JPDABreakpointEvent event) {
                try {
                    if (event.getDebugger() == debugger) {
                        LOG.log(Level.FINE, "Building of an Engine is in progress");
                        handleJustBuiltEngine(debugger, event.getThread());
                    }
                } finally {
                    event.resume();
                }
            }
        };
        debugManagerLoadBP.addJPDABreakpointListener(bpl);
        // Submit creation BPs for existing engine classes:
        Runnable submitEngineCreation = () -> {
            List<JPDAClassType> enginePe = debugger.getClassesByName(ENGINE_CLASS);
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

    /**
     * Called from a method entry breakpoint on Engine$Builder.build().
     * We need to submit a temporary method-exit breakpoint on the build method.
     * We must not keep the method exit breakpoint active as it causes a significant performance degradation.
     */
    private void handleJustBuiltEngine(final JPDADebugger debugger, JPDAThread onThread) {
        MethodBreakpoint builderExitBreakpoint = MethodBreakpoint.create(ENGINE_BUILDER_CLASS, "build");
        builderExitBreakpoint.setBreakpointType(MethodBreakpoint.TYPE_METHOD_EXIT);
        builderExitBreakpoint.setThreadFilters(debugger, new JPDAThread[]{onThread});
        builderExitBreakpoint.setSuspend(JPDABreakpoint.SUSPEND_EVENT_THREAD);
        builderExitBreakpoint.setSession(debugger);
        builderExitBreakpoint.setHidden(true);
        builderExitBreakpoint.addJPDABreakpointListener(exitEvent -> {
            try {
                builderExitBreakpoint.disable();
                DebuggerManager.getDebuggerManager().removeBreakpoint(builderExitBreakpoint);
                final ObjectReference ref = (ObjectReference) ((JDIVariable) exitEvent.getVariable()).getJDIValue();
                LOG.log(Level.FINE, "New Engine has just been built: {0}", ref);
                haveNewPE(debugger, (JPDAThreadImpl) exitEvent.getThread(), ref);
            } finally {
                exitEvent.resume();
            }
        });
        DebuggerManager.getDebuggerManager().addBreakpoint(builderExitBreakpoint);
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

    private DebugManagerHandler getDebugManagerHandler(JPDADebugger debugger) {
        DebugManagerHandler dmh;
        synchronized (dmHandlers) {
            dmh = dmHandlers.get(debugger);
            if (dmh == null) {
                dmh = new DebugManagerHandler(debugger);
                dmHandlers.put(debugger, dmh);
            }
        }
        return dmh;
    }

    private void haveNewPE(JPDADebugger debugger, JPDAThreadImpl thread, ObjectReference engine) {
        DebugManagerHandler dmh = getDebugManagerHandler(debugger);
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
