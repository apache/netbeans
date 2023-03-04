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

package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.InvalidRequestStateException;
import com.sun.jdi.request.StepRequest;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
//import org.netbeans.modules.debugger.jpda.JPDAStepImpl.SingleThreadedStepWatch;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidRequestStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.StepRequestWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.spi.debugger.ActionsProviderSupport;

import org.openide.util.WeakSet;


/**
* Representation of a debugging session.
*
* @author   Jan Jancura
* @author  Marian Petras
*/
public abstract class JPDADebuggerActionProvider extends ActionsProviderSupport 
implements PropertyChangeListener {
    
    protected JPDADebuggerImpl debugger;
    
    private static final Set<JPDADebuggerActionProvider> providersToDisableOnLazyActions = new WeakSet<JPDADebuggerActionProvider>();
    
    private volatile boolean disabled;
    
    protected JPDADebuggerActionProvider (JPDADebuggerImpl debugger) {
        this.debugger = debugger;
        debugger.addPropertyChangeListener (JPDADebugger.PROP_STATE, this);
    }
    
    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if ("methodInvoke".equals(evt.getPropagationId())) {
            return ; // Ignore events associated with method invocations
        }
        checkEnabled (debugger.getState ());
    }
    
    protected abstract void checkEnabled (int debuggerState);
    
    @Override
    public boolean isEnabled (Object action) {
        if (!disabled) {
            checkEnabled (debugger.getState ());
        }
        return super.isEnabled (action);
    }
    
    protected JPDADebuggerImpl getDebuggerImpl () {
        return debugger;
    }
    
    protected void removeStepRequests (ThreadReference tr) {
        removeStepRequests(getDebuggerImpl(), tr);
    }

    static void removeStepRequests(JPDADebuggerImpl debugger, ThreadReference tr) {
        //S ystem.out.println ("removeStepRequests");
        try {
            VirtualMachine vm = debugger.getVirtualMachine ();
            if (vm == null) return;
            EventRequestManager erm = VirtualMachineWrapper.eventRequestManager (vm);
            List<StepRequest> l = EventRequestManagerWrapper.stepRequests (erm);
            Iterator<StepRequest> it = l.iterator ();
            while (it.hasNext ()) {
                StepRequest stepRequest = it.next ();
                if (StepRequestWrapper.thread(stepRequest).equals (tr)) {
                    try {
                        //S ystem.out.println("  remove request " + stepRequest);
                        EventRequestManagerWrapper.deleteEventRequest(erm, stepRequest);
                    } catch (InvalidRequestStateExceptionWrapper ex) {}
                    //SingleThreadedStepWatch.stepRequestDeleted(stepRequest);
                    debugger.getOperator().unregister(stepRequest);
                    break;
                }
                //S ystem.out.println("  do not remove " + stepRequest + " : " + stepRequest.thread ());
            }
        } catch (VMDisconnectedExceptionWrapper e) {
        } catch (InternalExceptionWrapper e) {
        } catch (IllegalThreadStateException e) {
        } catch (InvalidRequestStateException e) {
        }
    }
    
    /**
     * Mark the provided action provider to be disabled when a lazy action is to be performed.
     */
    final void setProviderToDisableOnLazyAction(JPDADebuggerActionProvider provider) {
        synchronized (JPDADebuggerActionProvider.class) {
            providersToDisableOnLazyActions.add(provider);
        }
    }
    
    /**
     * Do the action lazily in a RequestProcessor.
     * @param run The action to perform.
     */
    protected final void doLazyAction(final Object action, final Runnable run) {
        //System.err.println("doLazyAction() in "+this);
        //Logger.getLogger(JPDADebuggerActionProvider.class.getName()).fine("doLazyAction() in "+this);
        //final long start = System.nanoTime();
        final ActionsSynchronizer as = ActionsSynchronizer.get(debugger);
        as.actionScheduled(action);
        final JPDAThreadImpl threadWithActionsPending;
        JPDAThread ct = debugger.getCurrentThread();
        if (ct instanceof JPDAThreadImpl && action != ActionsManager.ACTION_PAUSE) {
            threadWithActionsPending = (JPDAThreadImpl) ct;
            threadWithActionsPending.setPendingAction(action);
        } else {
            threadWithActionsPending = null;
        }
        final Set<JPDADebuggerActionProvider> disabledActions;
        synchronized (JPDADebuggerActionProvider.class) {
            disabledActions = new HashSet<JPDADebuggerActionProvider>(providersToDisableOnLazyActions);
        }
        for (Iterator<JPDADebuggerActionProvider> it = disabledActions.iterator(); it.hasNext(); ) {
            JPDADebuggerActionProvider ap = it.next();
            Set actions = ap.getActions();
            ap.disabled = true;
            for (Iterator ait = actions.iterator(); ait.hasNext(); ) {
                Object a = ait.next();
                ap.setEnabled (a, false);
                //System.out.println(ap+".setEnabled("+action+", "+false+")");
            }
        }
        debugger.getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                try {
                    //long end = System.nanoTime();
                    //System.err.println("  run in RP after "+(end - start)+" ns ("+((end - start)/1000000)+" ms) in "+this);
                    //Logger.getLogger(JPDADebuggerActionProvider.class.getName()).fine("  run in RP after "+(end - start)+" ns ("+((end - start)/1000000)+" ms) in "+this);
                    as.actionStarts(action);
                    run.run();
                    if (threadWithActionsPending != null) {
                        threadWithActionsPending.setPendingAction(null);
                    }
                    for (Iterator<JPDADebuggerActionProvider> it = disabledActions.iterator(); it.hasNext(); ) {
                        JPDADebuggerActionProvider ap = it.next();
                        Set actions = ap.getActions();
                        ap.disabled = false;
                        ap.checkEnabled (debugger.getState ());
                    }
                } catch (com.sun.jdi.VMDisconnectedException e) {
                    // Causes kill action when something is being evaluated
                } finally {
                    as.actionEnds(action);
                }
            }
        });
    }
}
