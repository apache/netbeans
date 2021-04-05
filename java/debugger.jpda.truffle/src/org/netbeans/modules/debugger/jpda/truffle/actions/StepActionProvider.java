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
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.actions.JPDADebuggerActionProvider;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidRequestStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
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
                (TruffleAccess.getCurrentPCInfo(currentThread) != null)
            );
        }
    }

    @Override
    public void doAction(Object action) {
        LOG.fine("doAction("+action+")");
        JPDADebuggerImpl debugger = getDebuggerImpl();
        JPDAThread currentThread = debugger.getCurrentThread();
        CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(currentThread);
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
            currentPCInfo.getStepCommandVar().setFromMirrorObject((Integer) stepCmd);
        } catch (InvalidObjectException ex) {
            Exceptions.printStackTrace(ex);
        }
        killJavaStep(debugger);
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
    
}
