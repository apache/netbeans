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

package org.netbeans.modules.debugger.jpda.truffle.actions;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAStep;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.actions.JPDADebuggerActionProvider;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidRequestStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.models.AbstractVariable;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleStrataProvider;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.Exceptions;

/**
 * Stepping in the guest language.
 */
@ActionsProvider.Registration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM,
                              actions={"stepInto", "stepOver", "stepOut", "continue"})
public class StepActionProvider extends JPDADebuggerActionProvider {
    
    private static final Logger LOG = Logger.getLogger(StepActionProvider.class.getName());
    
    private static final Set<Object> ACTIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(new Object[] {
            ActionsManager.ACTION_STEP_INTO,
            ActionsManager.ACTION_STEP_OUT,
            ActionsManager.ACTION_STEP_OVER,
            ActionsManager.ACTION_CONTINUE
    })));

    public static final String STEP2JAVA_CLASS = "com.oracle.truffle.polyglot.HostMethodDesc$SingleMethod$MHBase";
    public static final String STEP2JAVA_METHOD = "invokeHandle";

    public StepActionProvider (ContextProvider lookupProvider) {
        super (
            (JPDADebuggerImpl) lookupProvider.lookupFirst 
                (null, JPDADebugger.class)
        );
    }
    
    @Override
    protected void checkEnabled(int debuggerState) {
        Iterator i = getActions ().iterator ();
        JPDAThread currentThread = getDebuggerImpl().getCurrentThread();
        while (i.hasNext ()) {
            setEnabled (
                i.next (),
                (debuggerState == JPDADebugger.STATE_STOPPED) &&
                (currentThread != null) &&
                (TruffleAccess.getCurrentGuestPCInfo(currentThread) != null)
            );
        }
    }

    @Override
    public void doAction(Object action) {
        LOG.fine("doAction("+action+")");
        JPDADebuggerImpl debugger = getDebuggerImpl();
        JPDAThread currentThread = debugger.getCurrentThread();
        CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentGuestPCInfo(currentThread);
        int stepCmd = 0;
        if (ActionsManager.ACTION_CONTINUE.equals(action)) {
            stepCmd = 0;
        } else if (ActionsManager.ACTION_STEP_INTO.equals(action)) {
            stepCmd = 1;
        } else if (ActionsManager.ACTION_STEP_OVER.equals(action)) {
            stepCmd = 2;
        } else if (ActionsManager.ACTION_STEP_OUT.equals(action)) {
            stepCmd = 3;
        }
        try {
            // The `stepCmd` variable is not visible anywhere.
            // This change of it's value should not be followed by a refresh.
            // We're resuming anyway.
            ((AbstractVariable) currentPCInfo.getStepCommandVar()).setSilentChange(true);
            currentPCInfo.getStepCommandVar().setFromMirrorObject((Integer) stepCmd);
        } catch (InvalidObjectException ex) {
            Exceptions.printStackTrace(ex);
        }
        killJavaStep(debugger);
        if (ActionsManager.ACTION_STEP_INTO.equals(action)) {
            setBreakpoint2Java(currentThread);
        }
        if (stepCmd > 0) {
            debugger.resumeCurrentThread();
        } else {
            debugger.resume();
        }
    }
    
    /**
     * Kill any pending Java step.
     * @param debugger 
     */
    public static void killJavaStep(JPDADebugger debugger) {
        killJavaStep((JPDADebuggerImpl) debugger);
    }
    
    // Kill any pending Java step...
    private static void killJavaStep(JPDADebuggerImpl debugger) {
        VirtualMachine vm = debugger.getVirtualMachine();
        if (vm == null) {
            return ;
        }
        EventRequestManager erm = vm.eventRequestManager();
        List<StepRequest> stepRequests;
        try {
            stepRequests = EventRequestManagerWrapper.stepRequests(erm);
        } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper ex) {
            return ;
        }
        if (stepRequests.isEmpty()) {
            return ;
        }
        stepRequests = new ArrayList<>(stepRequests);
        for (StepRequest sr : stepRequests) {
            try {
                EventRequestManagerWrapper.deleteEventRequest(erm, sr);
                debugger.getOperator().unregister(sr);
            } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper |
                     InvalidRequestStateExceptionWrapper ex) {
            }
        }
    }

    @Override
    public Set getActions() {
        return ACTIONS;
    }

    private void setBreakpoint2Java(JPDAThread currentThread) {
        MethodBreakpoint stepIntoJavaBreakpoint = MethodBreakpoint.create(STEP2JAVA_CLASS, STEP2JAVA_METHOD);
        stepIntoJavaBreakpoint.setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY);
        stepIntoJavaBreakpoint.setThreadFilters(debugger, new JPDAThread[]{currentThread});
        stepIntoJavaBreakpoint.setHidden(true);
        stepIntoJavaBreakpoint.addJPDABreakpointListener(new JPDABreakpointListener() {
            @Override
            public void breakpointReached(JPDABreakpointEvent event) {
                stepIntoJavaBreakpoint.removeJPDABreakpointListener(this);
                JPDAStep step2Java = debugger.createJPDAStep(JPDAStep.STEP_LINE, JPDAStep.STEP_INTO);
                // Step through the reflection invocation onto a user Java code
                // Need to add the standard exclusion patterns as we're stepping from an excluded location
                step2Java.addSteppingFilters(debugger.getSmartSteppingFilter().getExclusionPatterns());
                // Additional invocation-specific patterns:
                step2Java.addSteppingFilters("java.lang.invoke.*", "sun.invoke.*", Class.class.getName(), System.class.getName());
                step2Java.setStepThroughFilters(true);
                step2Java.addStep(currentThread);
                event.resume();
            }
        });
        ((JPDAThreadImpl) currentThread).addPropertyChangeListener(JPDAThread.PROP_SUSPENDED, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ((Boolean) evt.getNewValue()) {
                    // Remove the step breakpoint on any suspend
                    DebuggerManager.getDebuggerManager().removeBreakpoint(stepIntoJavaBreakpoint);
                    ((JPDAThreadImpl) evt.getSource()).removePropertyChangeListener(JPDAThread.PROP_SUSPENDED, this);
                }
            }
        });
        DebuggerManager.getDebuggerManager().addBreakpoint(stepIntoJavaBreakpoint);
    }

}
