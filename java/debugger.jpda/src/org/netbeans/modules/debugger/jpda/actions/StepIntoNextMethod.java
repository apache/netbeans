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

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.InvalidRequestStateException;
import com.sun.jdi.request.StepRequest;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.JPDAStepImpl;
import org.netbeans.modules.debugger.jpda.SourcePath;
import static org.netbeans.modules.debugger.jpda.actions.StepActionProvider.getTopFrame;
import org.netbeans.modules.debugger.jpda.impl.StepUtils;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidRequestStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MethodWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.EventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.StepRequestWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.SmartSteppingCallback;
import org.netbeans.spi.debugger.jpda.SmartSteppingCallback.StopOrStep;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;

/**
 * Extracted from StepIntoActionProvider and StepIntoNextMethodActionProvider
 *
 * @author Martin Entlicher
 */
public class StepIntoNextMethod implements Executor, PropertyChangeListener {

    private static final Logger smartLogger = Logger.getLogger("org.netbeans.modules.debugger.jpda.smartstepping"); // NOI18N
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.jdievents"); // NOI18N

    private volatile StepRequest stepIntoRequest;
    private String position;
    private int depth;
    private final JPDADebuggerImpl debugger;
    private final ContextProvider contextProvider;
    private final boolean smartSteppingStepOut;
    private boolean steppingFromFilteredLocation;
    private boolean steppingFromCompoundFilteredLocation;
    private SmartSteppingFilterWrapper smartSteppingFilter;
    private boolean didStepThrough;
    private final Properties p;

    public StepIntoNextMethod(ContextProvider contextProvider) {
        this.debugger = (JPDADebuggerImpl) contextProvider.lookupFirst(null, JPDADebugger.class);
        this.contextProvider = contextProvider;
        debugger.getSmartSteppingFilter().addPropertyChangeListener (this);
        SourcePath ec = contextProvider.lookupFirst(null, SourcePath.class);
        ec.addPropertyChangeListener (this);
        Map properties = contextProvider.lookupFirst(null, Map.class);
        smartSteppingStepOut = (properties != null) ?
                properties.containsKey (StepIntoActionProvider.SS_STEP_OUT) : false;
        p = Properties.getDefault().getProperties("debugger.options.JPDA"); // NOI18N
    }

    private JPDADebuggerImpl getDebuggerImpl() {
        return debugger;
    }

    public void runAction() {
        runAction(true);
    }

    public void runAction(boolean doResume) {
        runAction(null, doResume, null, null, null);
    }

    void runAction(Object action, boolean doResume, Lock lock,
                   Boolean isSteppingFromFilteredLocation,
                   Boolean isSteppingFromCompoundFilteredLocation) {
        smartLogger.finer("STEP INTO NEXT METHOD.");
        JPDAThread t = getDebuggerImpl ().getCurrentThread ();
        if (t == null) {
            // Can not step without current thread.
            smartLogger.finer("Can not step into next method! No current thread!");
            return ;
        }
        smartSteppingFilter = new SmartSteppingFilterWrapper(debugger.getSmartSteppingFilter());
        didStepThrough = false;
        boolean locked = lock == null;
        try {
            if (lock == null) {
                if (getDebuggerImpl().getSuspend() == JPDADebugger.SUSPEND_EVENT_THREAD) {
                    lock = ((JPDAThreadImpl) t).accessLock.writeLock();
                } else {
                    lock = getDebuggerImpl().accessLock.writeLock();
                }
                lock.lock();
                if (!(t.isSuspended() || ((JPDAThreadImpl) t).isSuspendedNoFire())) {
                    // Can not step when it's not suspended.
                    if (smartLogger.isLoggable(Level.FINER)) {
                        smartLogger.finer("Can not step into next method! Thread "+t+" not suspended!");
                    }
                    return ;
                }
            }
            JPDAThreadImpl[] resumeThreadPtr = new JPDAThreadImpl[] { null };
            int stepDepth;
            if (ActionsManager.ACTION_STEP_OUT.equals(action)) {
                stepDepth = StepRequest.STEP_OUT;
            } else {
                stepDepth = StepRequest.STEP_INTO;
            }
            if (isSteppingFromFilteredLocation != null) {
                steppingFromFilteredLocation = isSteppingFromFilteredLocation.booleanValue();
            } else {
                steppingFromFilteredLocation = !StepActionProvider.stopInClass(t.getClassName(), smartSteppingFilter);
            }
            if (isSteppingFromCompoundFilteredLocation != null) {
                steppingFromCompoundFilteredLocation = isSteppingFromCompoundFilteredLocation.booleanValue();
            } else {
                CallStackFrame topFrame = getTopFrame(t);
                if (topFrame != null) {
                    steppingFromCompoundFilteredLocation = !getCompoundSmartSteppingListener ().stopAt
                             (contextProvider, topFrame, smartSteppingFilter).isStop();
                } else {
                    steppingFromCompoundFilteredLocation = !getCompoundSmartSteppingListener ().stopHere
                                       (contextProvider, t, smartSteppingFilter);
                }
            }

            StepRequest stepRequest = setStepRequest (stepDepth, resumeThreadPtr);
            position = t.getClassName () + '.' +
                       t.getMethodName () + ':' +
                       t.getLineNumber (null);
            if (stepDepth == StepRequest.STEP_INTO) {
                // Special handling of stepping into in a nashorn script:
                if (position.startsWith("jdk.nashorn.internal.scripts.Script") &&
                    "JS".equals(debugger.getSession().getCurrentLanguage())) {
                    try {
                        EventRequestWrapper.disable (stepRequest);
                        stepRequest.addClassFilter("jdk.nashorn.internal.scripts.Script*");
                        EventRequestWrapper.enable (stepRequest);
                    } catch (InternalExceptionWrapper ex) {
                        stepRequest = null;
                    } catch (InvalidRequestStateExceptionWrapper ex) {
                        stepRequest = null;
                    } catch (ObjectCollectedExceptionWrapper ex) {
                        stepRequest = null;
                    } catch (VMDisconnectedExceptionWrapper ex) {
                        stepRequest = null;
                    }
                }
                stepIntoRequest = stepRequest;
            }
            depth = t.getStackDepth();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("JDI Request (action step into next method): " + stepRequest);
            }
            if (stepRequest == null) return ;
            ((JPDAThreadImpl) t).setInStep(true, stepRequest);
            if (doResume) {
                if (resumeThreadPtr[0] == null) {
                    getDebuggerImpl ().resume ();
                } else {
                    //resumeThread.resume();
                    //stepWatch = new SingleThreadedStepWatch(getDebuggerImpl(), stepRequest);
                    getDebuggerImpl().resumeCurrentThread();
                }
            }
        } finally {
            if (locked && lock != null) {
                lock.unlock();
            }
        }
    }

    @Override
    public void propertyChange (PropertyChangeEvent ev) {
        if (SmartSteppingFilter.PROP_EXCLUSION_PATTERNS.equals(ev.getPropertyName())) {
            if (ev.getOldValue () != null) {
                // remove some patterns
                smartLogger.finer("Exclusion patterns removed. Removing step requests.");
                JPDAThreadImpl currentThread = (JPDAThreadImpl) getDebuggerImpl().getCurrentThread();
                if (currentThread != null) {
                    ThreadReference tr = currentThread.getThreadReference ();
                    removeStepRequests (tr);
                }
            } else {
                if (smartLogger.isLoggable(Level.FINER)) {
                    if (stepIntoRequest == null)
                        smartLogger.finer("Exclusion patterns has been added");
                    else
                        smartLogger.finer("Add exclusion patterns: "+ev.getNewValue());
                }
                try {
                    addPatternsToRequest((String[]) ((Set<String>) ev.getNewValue ()).toArray (
                        new String [((Set) ev.getNewValue ()).size()]
                    ), stepIntoRequest);
                } catch (InternalExceptionWrapper ex) {
                    return ;
                } catch (VMDisconnectedExceptionWrapper ex) {
                    return ;
                }
            }
        } else
        if (SourcePathProvider.PROP_SOURCE_ROOTS.equals(ev.getPropertyName())) {
            smartLogger.finer("Source roots changed");
            JPDAThreadImpl jtr = (JPDAThreadImpl) getDebuggerImpl ().
                getCurrentThread ();
            if (jtr != null) {
                ThreadReference tr = jtr.getThreadReference ();
                removeStepRequests (tr);
            }
        }
    }


    // Executor ................................................................

    /**
     * Executes all step actions and smart stepping.
     *
     * Should be called from Operator only.
     */
    @Override
    public boolean exec (Event event) {
        ThreadReference tr;
        JPDAThreadImpl st;
        StepRequest sr;
        try {
            sr = (StepRequest) EventWrapper.request(event);
            tr = StepRequestWrapper.thread(sr);
            st = getDebuggerImpl().getThread(tr);
            st.setInStep(false, null);
        } catch (InternalExceptionWrapper ex) {
            return false;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return false;
        }
        /*if (stepWatch != null) {
            stepWatch.done();
            stepWatch = null;
        }*/
        st.accessLock.readLock().lock();
        try {
            //if (stepRequest != null) {
                try {
                    EventRequestWrapper.disable (sr);
                } catch (InternalExceptionWrapper ex) {
                    return false;
                } catch (VMDisconnectedExceptionWrapper ex) {
                    return false;
                } catch (InvalidRequestStateExceptionWrapper irse) {
                    return false;
                } catch (ObjectCollectedExceptionWrapper oce) {
                    return false;
                }
            //}

            boolean useStepFilters = p.getBoolean("UseStepFilters", true);
            boolean stepThrough = useStepFilters && p.getBoolean("StepThroughFilters", false)
                                  && !smartSteppingStepOut;
            if (!stepThrough && !didStepThrough && isFilteredClassOnStack(tr, depth)) {
                // There's a class that was skipped by step on the stack.
                // If step through is false, we should step out from here.
                smartLogger.finer(" stoped with a filtered class on stack and step through is false.");
                StepRequest newSR = setStepRequest (StepRequest.STEP_OUT);
                return newSR != null;
            }
            try {
                boolean filterSyntheticMethods = useStepFilters && p.getBoolean("FilterSyntheticMethods", true);
                boolean filterStaticInitializers = useStepFilters && p.getBoolean("FilterStaticInitializers", false);
                boolean filterConstructors = useStepFilters && p.getBoolean("FilterConstructors", false);
                Location loc = StackFrameWrapper.location(ThreadReferenceWrapper.frame(tr, 0));
                com.sun.jdi.Method m = LocationWrapper.method(loc);
                int syntheticStep = JPDAStepImpl.isSyntheticMethod(m, loc);
                if (filterSyntheticMethods && syntheticStep != 0) {
                    //S ystem.out.println("In synthetic method -> STEP INTO again");
                    smartLogger.finer(" stoped in a synthetic method.");
                    if (syntheticStep < 0) {
                        syntheticStep = StepRequest.STEP_INTO;
                    }
                    StepRequest newSR = setStepRequest (syntheticStep);
                    return newSR != null;
                }
                if (filterStaticInitializers && MethodWrapper.isStaticInitializer(m) ||
                    filterConstructors && MethodWrapper.isConstructor(m)) {

                    smartLogger.finer(" stoped in a static initializer or constructor.");
                    StepRequest newSR = setStepRequest (StepRequest.STEP_OUT);
                    return newSR != null;
                }
            } catch (IncompatibleThreadStateException e) {
                //ErrorManager.getDefault().notify(e);
                // This may happen while debugging a free form project
            } catch (IllegalThreadStateExceptionWrapper e) {
            } catch (InvalidStackFrameExceptionWrapper e) {
            } catch (InternalExceptionWrapper e) {
            } catch (ObjectCollectedExceptionWrapper e) {
            } catch (VMDisconnectedExceptionWrapper e) {
                return true;
            }

            JPDAThread t = getDebuggerImpl ().getThread (tr);
            StopOrStep stop;
            if (steppingFromCompoundFilteredLocation) {
                stop = StopOrStep.stop();
            } else {
                CallStackFrame topFrame = getTopFrame(t);
                if (topFrame != null) {
                    stop = getCompoundSmartSteppingListener().stopAt
                                       (contextProvider, topFrame, smartSteppingFilter);
                } else {
                    stop = getCompoundSmartSteppingListener().stopHere
                                       (contextProvider, t, smartSteppingFilter) ? StopOrStep.stop() : StopOrStep.skip();
                }
            }
            if (stop.isStop()) {
                String stopPosition = t.getClassName () + '.' +
                                      t.getMethodName () + ':' +
                                      t.getLineNumber (null);
                int stopDepth = t.getStackDepth();
                if (smartLogger.isLoggable(Level.FINER)) {
                    smartLogger.finer(" stop position = "+stopPosition+", original position = "+position+", stop depth = "+stopDepth+", original depth = "+depth+" => will stop: "+(!(position.equals(stopPosition) && depth == stopDepth)));
                }
                if (position.equals(stopPosition) && depth == stopDepth) {
                    // We are where we started!
                    stop = StopOrStep.skip();
                    StepRequest newSR = setStepRequest (StepRequest.STEP_INTO);
                    return newSR != null;
                }
            }
            if (stop.isStop()) {
                removeStepRequests (tr);
            } else {
                smartLogger.log(Level.FINER, " => do next step: {0}", stop);
                int stepDepth = stop.getStepDepth();
                int stepSize = stop.getStepSize();
                if (stepSize == 0) {
                    stepSize = StepRequest.STEP_LINE;
                }
                if (stepDepth == 0) {
                    if (!stepThrough || smartSteppingStepOut) {
                        stepDepth = StepRequest.STEP_OUT;
                    } else {
                        stepDepth = StepRequest.STEP_INTO;
                    }
                } else if (stepDepth == StepRequest.STEP_INTO) {
                    // We're stepping through in this case
                    didStepThrough = true;
                }
                StepRequest newSR = setStepRequest(stepDepth);
                if (newSR == null) {
                    return false; // Do not resume if something went wrong!
                }
            }

            if (stop.isStop()) {
                if (smartLogger.isLoggable(Level.FINER))
                    smartLogger.finer("FINISH IN CLASS " +
                        t.getClassName () + " ********"
                    );
                try {
                    StepActionProvider.setLastOperation(tr, debugger, null);
                } catch (VMDisconnectedExceptionWrapper e) {
                    return true;
                }
            }
            return !stop.isStop();
        } finally {
            st.accessLock.readLock().unlock();
        }
    }

    @Override
    public void removed(EventRequest eventRequest) {
        StepRequest sr = (StepRequest) eventRequest;
        try {
            JPDAThreadImpl st = getDebuggerImpl().getThread(StepRequestWrapper.thread(sr));
            st.setInStep(false, null);
        } catch (InternalExceptionWrapper ex) {
        } catch (VMDisconnectedExceptionWrapper ex) {
        }
        /*if (stepWatch != null) {
            stepWatch.done();
            stepWatch = null;
        }*/
    }


    private StepActionProvider stepActionProvider;

    private StepActionProvider getStepActionProvider () {
        if (stepActionProvider == null) {
            List l = contextProvider.lookup (null, ActionsProvider.class);
            int i, k = l.size ();
            for (i = 0; i < k; i++)
                if (l.get (i) instanceof StepActionProvider)
                    stepActionProvider = (StepActionProvider) l.get (i);
        }
        return stepActionProvider;
    }

    // other methods ...........................................................

    private void removeStepRequests (ThreadReference tr) {
        synchronized (this) {
            stepIntoRequest = null;
        }
        JPDADebuggerActionProvider.removeStepRequests (getDebuggerImpl(), tr);
        smartLogger.finer("removing all patterns, all step requests.");
    }

    private StepRequest setStepRequest (int step) {
        return setStepRequest(step, StepRequest.STEP_LINE);
    }
    
    private StepRequest setStepRequest (int step, int stepSize) {
        StepRequest enabledStepRequest = null;
        if (step == StepRequest.STEP_INTO) {
            synchronized (this) {
                if (stepIntoRequest != null) {
                    try {
                        addPatternsToRequest(smartSteppingFilter.getExclusionPatterns(), stepIntoRequest);
                        try {
                            EventRequestWrapper.enable (stepIntoRequest);
                            enabledStepRequest = stepIntoRequest;
                        } catch (IllegalThreadStateException itsex) {
                            // the thread named in the request has died.
                            getDebuggerImpl().getOperator().unregister(stepIntoRequest);
                            stepIntoRequest = null;
                            return null;
                        } catch (ObjectCollectedExceptionWrapper ocex) {
                            // Thread was collected...
                            getDebuggerImpl().getOperator().unregister(stepIntoRequest);
                            stepIntoRequest = null;
                            return null;
                        } catch (InvalidRequestStateExceptionWrapper irse) {
                            getDebuggerImpl().getOperator().unregister(stepIntoRequest);
                            stepIntoRequest = null;
                            return null;
                        }
                    } catch (VMDisconnectedExceptionWrapper e) {
                        stepIntoRequest = null;
                        return null;
                    } catch (InternalExceptionWrapper e) {
                        stepIntoRequest = null;
                        return null;
                    }
                }
            }
        }
        if (enabledStepRequest != null) {
            return enabledStepRequest;
        } else {
            return setStepRequest(step, stepSize, null);
        }
    }

    private StepRequest setStepRequest (int step, JPDAThreadImpl[] resumeThreadPtr) {
        return setStepRequest(step, StepRequest.STEP_LINE, resumeThreadPtr);
    }
    
    private StepRequest setStepRequest (int step, int stepSize, JPDAThreadImpl[] resumeThreadPtr) {
        JPDAThreadImpl thread = (JPDAThreadImpl) getDebuggerImpl().getCurrentThread();
        ThreadReference tr = thread.getThreadReference ();
        removeStepRequests (tr);
        VirtualMachine vm = getDebuggerImpl ().getVirtualMachine ();
        if (vm == null) return null;
        int suspendPolicy;
        StepRequest stepRequest;
        try {
            stepRequest = EventRequestManagerWrapper.createStepRequest(
                    VirtualMachineWrapper.eventRequestManager(vm),
                    tr,
                    stepSize,
                    step);
            StepUtils.markOriginalStepDepth(stepRequest, tr);
            getDebuggerImpl ().getOperator ().register (stepRequest, this);
            suspendPolicy = getDebuggerImpl().getSuspend();
            EventRequestWrapper.setSuspendPolicy (stepRequest, suspendPolicy);
        } catch (InternalExceptionWrapper ex) {
            return null;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return null;
        }

        if (smartLogger.isLoggable(Level.FINER)) {
            smartLogger.finer("Set step request("+step+") and patterns: ");
        }
        try {
            try {
                if (!steppingFromFilteredLocation) {
                    addPatternsToRequest (
                        smartSteppingFilter.getExclusionPatterns (),
                        stepRequest
                    );
                }
                EventRequestWrapper.enable (stepRequest);
            } catch (IllegalThreadStateException itsex) {
                // the thread named in the request has died.
                getDebuggerImpl().getOperator().unregister(stepRequest);
                stepRequest = null;
                return null;
            } catch (ObjectCollectedExceptionWrapper ocex) {
                // the thread named in the request was collected.
                getDebuggerImpl().getOperator().unregister(stepRequest);
                stepRequest = null;
                return null;
            } catch (InvalidRequestStateExceptionWrapper irse) {
                getDebuggerImpl().getOperator().unregister(stepRequest);
                stepRequest = null;
                return null;
            }
        } catch (InternalExceptionWrapper ex) {
            return null;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return null;
        }
        if (resumeThreadPtr != null) {
            if (suspendPolicy == JPDADebugger.SUSPEND_EVENT_THREAD) {
                resumeThreadPtr[0] = thread;
            } else {
                resumeThreadPtr[0] = null;
            }
        }
        return stepRequest;
    }


    private CompoundSmartSteppingListener compoundSmartSteppingListener;

    private CompoundSmartSteppingListener getCompoundSmartSteppingListener () {
        if (compoundSmartSteppingListener == null)
            compoundSmartSteppingListener = contextProvider.lookupFirst(null, CompoundSmartSteppingListener.class);
        return compoundSmartSteppingListener;
    }

    private void addPatternsToRequest (String[] patterns, StepRequest stepRequest) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        if (stepRequest == null) return;
        int i, k = patterns.length;
        for (i = 0; i < k; i++) {
            try {
                StepRequestWrapper.addClassExclusionFilter(stepRequest, patterns [i]);
            } catch (InvalidRequestStateException irex) {
                // The request is gone - ignore
                return ;
            }
            smartLogger.log(Level.FINER, "   add pattern: {0}", patterns[i]);
        }
    }

    private boolean isFilteredClassOnStack(ThreadReference tr, int depth) {
        if (steppingFromFilteredLocation) {
            return false;
        }
        String[] patterns = smartSteppingFilter.getExclusionPatterns();
        if (patterns.length == 0) return false;
        try {
            int n = ThreadReferenceWrapper.frameCount(tr);
            if (n <= (depth + 1)) {
                // There can not be anything in between up to (depth + 1)
                return false;
            }
            List<StackFrame> frames = ThreadReferenceWrapper.frames(tr, 0, n - depth);
            for (StackFrame f : frames) {
                /* TODO: use
                boolean canStop = getCompoundSmartSteppingListener ().stopHere
                                (contextProvider, f, getSmartSteppingFilterImpl ());
                 */
                String className = ReferenceTypeWrapper.name(LocationWrapper.declaringType(
                    StackFrameWrapper.location(f)));
                for (String pattern : patterns) {
                    if (match(className, pattern)) {
                        smartLogger.log(Level.FINER, " class ''{0}'' on stack.", className);
                        return true;
                    }
                }
            }
        } catch (IncompatibleThreadStateException ex) {
        } catch (InternalExceptionWrapper ex) {
        } catch (VMDisconnectedExceptionWrapper ex) {
        } catch (ObjectCollectedExceptionWrapper ex) {
        } catch (IllegalThreadStateExceptionWrapper ex) {
        } catch (InvalidStackFrameExceptionWrapper ex) {
        }
        return false;
    }

    private static boolean match(String name, String pattern) {
        if (pattern.startsWith("*")) {
            return name.endsWith(pattern.substring(1));
        } else if (pattern.endsWith("*")) {
            return name.startsWith(pattern.substring(0, pattern.length() - 1));
        }
        return name.contentEquals(pattern);
    }

}
