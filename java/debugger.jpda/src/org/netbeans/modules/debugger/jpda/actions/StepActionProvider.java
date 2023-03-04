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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAStep;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.ExpressionPool;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.JPDAStepImpl;
import org.netbeans.modules.debugger.jpda.JPDAStepImpl.MethodExitBreakpointListener;
import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.modules.debugger.jpda.impl.StepUtils;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidRequestStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocatableWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MethodWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MirrorWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeComponentWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.EventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.LocatableEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.StepRequestWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.netbeans.spi.debugger.jpda.SmartSteppingCallback;
import org.netbeans.spi.debugger.jpda.SmartSteppingCallback.StopOrStep;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;


/**
 * Implements non visual part of stepping through code in JPDA debugger.
 * It supports standard debugging actions StepInto, Over, Out, RunToCursor, 
 * and Go. And advanced "smart tracing" action.
 *
 * @author  Jan Jancura
 */
@ActionsProvider.Registration(path="netbeans-JPDASession", actions={"stepOver", "stepOut"})
public class StepActionProvider extends JPDADebuggerActionProvider 
implements Executor {

    private final ContextProvider   lookupProvider;
    private MethodExitBreakpointListener lastMethodExitBreakpointListener;
    private int                     syntheticStep;
    //private SingleThreadedStepWatch stepWatch;
    private boolean smartSteppingStepOut;
    private final Properties p;
    private String className;
    private String methodName;
    private int depth;
    private boolean steppingFromFilteredLocation = false;
    private boolean steppingFromCompoundFilteredLocation = false;
    private static final RequestProcessor operationsRP = new RequestProcessor("Debugger Operations Computation", 1);    // NOI18N
    
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.jdievents"); // NOI18N
    private static final Logger loggerStep = Logger.getLogger("org.netbeans.modules.debugger.jpda.step"); // NOI18N


    private static int getJDIAction (Object action) {
        if (action == ActionsManager.ACTION_STEP_OUT) 
            return StepRequest.STEP_OUT;
        if (action == ActionsManager.ACTION_STEP_OVER) 
            return StepRequest.STEP_OVER;
        throw new IllegalArgumentException ();
    }
    
    
    public StepActionProvider (ContextProvider lookupProvider) {
        super (
            (JPDADebuggerImpl) lookupProvider.lookupFirst 
                (null, JPDADebugger.class)
        );
        this.lookupProvider = lookupProvider;
        setProviderToDisableOnLazyAction(this);
        Map properties = lookupProvider.lookupFirst(null, Map.class);
        if (properties != null) {
            smartSteppingStepOut = properties.containsKey (StepIntoActionProvider.SS_STEP_OUT);
        }
        p = Properties.getDefault().getProperties("debugger.options.JPDA"); // NOI18N
    }


    // ActionProviderSupport ...................................................
    
    @Override
    public Set getActions () {
        return new HashSet<Object>(Arrays.asList (new Object[] {
            ActionsManager.ACTION_STEP_OUT,
            ActionsManager.ACTION_STEP_OVER
        }));
    }
    
    @Override
    public void doAction (final Object action) {
        runAction(action);
    }
    
    @Override
    public void postAction(final Object action,
                           final Runnable actionPerformedNotifier) {
        doLazyAction(action, new Runnable() {
            @Override
            public void run() {
                try {
                    runAction(action);
                } finally {
                    actionPerformedNotifier.run();
                }
            }
        });
    }

    /**
     * Returns whether we can stop in the class based on the smart stepping filter.
     */
    public static boolean stopInClass(String className, SmartSteppingFilter filter) {
        for (String pattern : filter.getExclusionPatterns()) {
            if (pattern.startsWith ("*")) {
                String end = pattern.substring(1);
                if (className.endsWith(end)) {
                    return false;
                }
            } else if (pattern.endsWith ("*")) {
                String start = pattern.substring(0, pattern.length() - 1);
                if (className.startsWith(start)) {
                    return false;
                }
            } else {
                // exact match
                if (className.equals(pattern)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void runAction(final Object action) {
        runAction(getJDIAction(action), StepRequest.STEP_LINE, true, null, null, null);
    }

    private void runAction(final int stepDepth, final int stepSize, boolean doResume, Lock lock,
                           Boolean isSteppingFromFilteredLocation,
                           Boolean isSteppingFromCompoundFilteredLocation) {
        //S ystem.out.println("\nStepAction.doAction");
        int suspendPolicy = getDebuggerImpl().getSuspend();
        JPDAThreadImpl resumeThread = (JPDAThreadImpl) getDebuggerImpl().getCurrentThread();
        boolean locked = lock == null;
        try {
            if (lock == null) {
                if (suspendPolicy == JPDADebugger.SUSPEND_EVENT_THREAD) {
                    lock = resumeThread.accessLock.writeLock();
                } else {
                    lock = getDebuggerImpl().accessLock.writeLock();
                }
                lock.lock();

                // 1) init info about current state & remove old
                //    requests in the current thread
                // We have to assure that the thread is suspended so that we
                // do not randomly resume threads
                while (!resumeThread.isSuspended()) {
                    // The thread is not suspended, release the lock so that others
                    // can process what they want with the resumed thread...
                    lock.unlock();
                    Thread.yield();
                    try {
                        Thread.sleep(100); // Wait for a moment
                    } catch (InterruptedException iex) {}
                    lock.lock(); // Take the lock again to repeat the test
                    if (!resumeThread.isSuspended() && !resumeThread.isInStep() && !resumeThread.isMethodInvoking()) {
                        // Explicitely suspend the thread if it's not in a step
                        // or in a method invocation:
                        resumeThread.suspend();
                    }
                }
            }
            smartSteppingFilter = new SmartSteppingFilterWrapper(debugger.getSmartSteppingFilter());
            resumeThread.waitUntilMethodInvokeDone();
            ThreadReference tr = resumeThread.getThreadReference ();
            removeStepRequests (tr);

            // 2) create new step request
            VirtualMachine vm = getDebuggerImpl ().getVirtualMachine ();
            if (vm == null) return ; // There's nothing to do without the VM.
            StepRequest stepRequest = EventRequestManagerWrapper.createStepRequest (
                    VirtualMachineWrapper.eventRequestManager (vm),
                    tr,
                    stepSize,
                    stepDepth
                );
            StepUtils.markOriginalStepDepth(stepRequest, tr);
            EventRequestWrapper.addCountFilter (stepRequest, 1);
            getDebuggerImpl ().getOperator ().register (stepRequest, StepActionProvider.this);
            EventRequestWrapper.setSuspendPolicy (stepRequest, suspendPolicy);
            try {
                EventRequestWrapper.enable (stepRequest);
            } catch (IllegalThreadStateException itsex) {
                // the thread named in the request has died.
                // Or suspend count > 1 !
                //itsex.printStackTrace();
                //System.err.println("Thread: "+tr.name()+", suspended = "+tr.isSuspended()+", suspend count = "+tr.suspendCount()+", status = "+tr.status());
                if (logger.isLoggable(Level.WARNING)) {
                    try {
                        logger.warning(itsex.getLocalizedMessage()+"\nThread: "+ThreadReferenceWrapper.name(tr)+", suspended = "+ThreadReferenceWrapper.isSuspended(tr)+", status = "+ThreadReferenceWrapper.status(tr));
                    } catch (Exception e) {
                        logger.warning(e.getLocalizedMessage());
                    }
                }
                getDebuggerImpl ().getOperator ().unregister(stepRequest);
                return ;
            } catch (ObjectCollectedExceptionWrapper ocex) {
                // Thread was collected...
                getDebuggerImpl ().getOperator ().unregister(stepRequest);
                return ;
            } catch (InvalidRequestStateExceptionWrapper irse) {
                Exceptions.printStackTrace(irse);
                getDebuggerImpl ().getOperator ().unregister(stepRequest);
                return ;
            }
            className = resumeThread.getClassName ();
            methodName = resumeThread.getMethodName ();
            depth = resumeThread.getStackDepth();
            CallStackFrame topFrame = getTopFrame(resumeThread);
            if (isSteppingFromFilteredLocation != null) {
                steppingFromFilteredLocation = isSteppingFromFilteredLocation;
            } else {
                steppingFromFilteredLocation = !stopInClass(className, smartSteppingFilter);
            }
            if (isSteppingFromCompoundFilteredLocation != null) {
                steppingFromCompoundFilteredLocation = isSteppingFromCompoundFilteredLocation;
            } else {
                if (topFrame != null) {
                    steppingFromCompoundFilteredLocation = !getCompoundSmartSteppingListener ().stopAt
                             (lookupProvider, topFrame, smartSteppingFilter).isStop();
                } else {
                    steppingFromCompoundFilteredLocation = !getCompoundSmartSteppingListener ().stopHere
                             (lookupProvider, resumeThread, smartSteppingFilter);
                }
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("JDI Request (step "+stepDepth+"): " + stepRequest);
            }
            if (stepDepth == JPDAStep.STEP_OUT) {
                addMethodExitBP(tr, resumeThread);
            }
            resumeThread.disableMethodInvokeUntilResumed();
            // 3) resume JVM
            resumeThread.setInStep(true, stepRequest);
            if (doResume) {
                if (suspendPolicy == JPDADebugger.SUSPEND_EVENT_THREAD) {
                    //stepWatch = new SingleThreadedStepWatch(getDebuggerImpl(), stepRequest);
                    getDebuggerImpl().resumeCurrentThread();
                    //resumeThread.resume();
                } else {
                    getDebuggerImpl ().resume ();
                }
            }
        } catch (VMDisconnectedExceptionWrapper e) {
            // Debugger is disconnected => the action will be ignored.
        } catch (InternalExceptionWrapper e) {
            // Debugger is damaged => the action will be ignored.
        } catch (InvalidStackFrameExceptionWrapper e) {
            Exceptions.printStackTrace(e);
        } catch (ObjectCollectedExceptionWrapper e) {
            // Thread was collected - ignore the step
        } finally {
            if (locked && lock != null) {
                lock.unlock();
            }
        }
        //S ystem.out.println("/nStepAction.doAction end");
    }
    
    static CallStackFrame getTopFrame(JPDAThread thread) {
        CallStackFrame topFrame = null;
        try {
            CallStackFrame[] topFrameArr = thread.getCallStack(0, 1);
            if (topFrameArr.length > 0) {
                topFrame = topFrameArr[0];
            }
        } catch (AbsentInformationException aiex) {}
        return topFrame;
    }
    
    private void addMethodExitBP(ThreadReference tr, JPDAThread jtr) throws VMDisconnectedExceptionWrapper, InternalExceptionWrapper, InvalidStackFrameExceptionWrapper, ObjectCollectedExceptionWrapper {
        if (!MirrorWrapper.virtualMachine(tr).canGetMethodReturnValues()) {
            return ;
        }
        Location loc;
        try {
            loc = StackFrameWrapper.location(ThreadReferenceWrapper.frame(tr, 0));
        } catch (IncompatibleThreadStateException ex) {
            logger.fine("Incompatible Thread State: "+ex.getLocalizedMessage());
            return ;
        } catch (IllegalThreadStateExceptionWrapper ex) {
            return ;
        } catch (IndexOutOfBoundsException ex) {
            // No frames on the thread => nowhere to exit to.
            return ;
        }
        String classType = ReferenceTypeWrapper.name(LocationWrapper.declaringType(loc));
        String methodName = TypeComponentWrapper.name(LocationWrapper.method(loc));
        MethodBreakpoint mb = MethodBreakpoint.create(classType, methodName);
        //mb.setMethodName(methodName);
        mb.setBreakpointType(MethodBreakpoint.TYPE_METHOD_EXIT);
        mb.setHidden(true);
        mb.setSuspend(JPDABreakpoint.SUSPEND_NONE);
        mb.setThreadFilters(getDebuggerImpl(), new JPDAThread[] { jtr });
        lastMethodExitBreakpointListener = new MethodExitBreakpointListener(mb);
        mb.addJPDABreakpointListener(lastMethodExitBreakpointListener);
        mb.setSession(debugger);
        DebuggerManager.getDebuggerManager().addBreakpoint(mb);
    }
    
    @Override
    protected void checkEnabled (int debuggerState) {
        Iterator i = getActions ().iterator ();
        while (i.hasNext ())
            setEnabled (
                i.next (),
                (debuggerState == JPDADebugger.STATE_STOPPED) &&
                (getDebuggerImpl ().getCurrentThread () != null)
            );
    }
    
    // Executor ................................................................
    
    /**
     * Executes all step actions and smart stepping. 
     *
     * Should be called from Operator only.
     */
    @Override
    public boolean exec (Event ev) {
        try {
        // TODO: fetch current engine from the Event
        // 1) init info about current state
        LocatableEvent event = (LocatableEvent) ev;
        ThreadReference tr = LocatableEventWrapper.thread(event);
        StepRequest sr = (StepRequest) EventWrapper.request(ev);
        JPDAThreadImpl st = getDebuggerImpl().getThread(tr); // StepRequestWrapper.thread(sr));
        Lock lock;
        if (getDebuggerImpl().getSuspend() == JPDADebugger.SUSPEND_EVENT_THREAD) {
            lock = st.accessLock.writeLock();
        } else {
            lock = getDebuggerImpl().accessLock.writeLock();
        }
        lock.lock();
        try {
            st.setInStep(false, null);
            boolean suspended = false;
            try {
                suspended = ThreadReferenceWrapper.isSuspended0(tr);
            } catch (IllegalThreadStateExceptionWrapper itsex) {
            }
            if (!suspended) {
                // The thread was already resumed in the mean time by someone else.
                removeStepRequests (tr);
                return false;
            }
            /*if (stepWatch != null) {
                stepWatch.done();
                stepWatch = null;
            }*/
            String className = ReferenceTypeWrapper.name(LocationWrapper.declaringType(LocatableWrapper.location(event)));
            setLastOperation(tr);
            removeStepRequests (tr);
            //S ystem.out.println("/nStepAction.exec");

            int suspendPolicy = getDebuggerImpl().getSuspend();
            
            // Synthetic method?
            if (syntheticStep != 0) {
                //S ystem.out.println("In synthetic method -> STEP OVER/OUT again");

                int step = (syntheticStep > 0) ? syntheticStep : StepRequestWrapper.depth(sr);
                VirtualMachine vm = getDebuggerImpl ().getVirtualMachine ();
                if (vm == null) {
                    removeBPListener();
                    return false; // The session has finished
                }
                StepRequest stepRequest = EventRequestManagerWrapper.createStepRequest(VirtualMachineWrapper.eventRequestManager(vm),
                    tr,
                    StepRequest.STEP_LINE,
                    step
                );
                StepUtils.markOriginalStepDepth(stepRequest, tr);
                EventRequestWrapper.addCountFilter(stepRequest, 1);
                getDebuggerImpl ().getOperator ().register (stepRequest, this);
                EventRequestWrapper.setSuspendPolicy(stepRequest, suspendPolicy);
                try {
                    EventRequestWrapper.enable(stepRequest);
                } catch (IllegalThreadStateException itsex) {
                    // the thread named in the request has died.
                    getDebuggerImpl ().getOperator ().unregister(stepRequest);
                    stepRequest = null;
                    removeBPListener();
                } catch (InvalidRequestStateExceptionWrapper irse) {
                    Exceptions.printStackTrace(irse);
                    getDebuggerImpl ().getOperator ().unregister(stepRequest);
                    stepRequest = null;
                    removeBPListener();
                }
                loggerStep.log(Level.FINE, "Further step in a synthetic location, depth = {0}", step);
                return true;
            }
            if (depth == 1 && "main".equals(methodName) && !"java.lang.Thread".equals(this.className) &&
                "java.lang.Thread".equals(className) && "exit".equals(getDebuggerImpl ().getThread (tr).getMethodName())) {
                // Hack for not stepping into Thread.exit() when stepping *over* from main() method.
                return true; // Resume to let the thread die and the app to finish.
            }
            
            // Stop execution here?
            StopOrStep fsh;
            if (steppingFromFilteredLocation) {
                fsh = StopOrStep.stop();
            } else {
                fsh = stopInClass(className, smartSteppingFilter) ? StopOrStep.stop() : StopOrStep.skip();
            }
            if (loggerStep.isLoggable(Level.FINE))
                loggerStep.fine("SS  SmartSteppingFilter.stopHere (" + className + ") ? " + fsh);
            if (fsh.isStop()) {
                if (steppingFromCompoundFilteredLocation) {
                    // fsh is true
                } else {
                    JPDAThread t = getDebuggerImpl ().getThread (tr);
                    CallStackFrame topFrame = getTopFrame(t);
                    if (topFrame != null) {
                        fsh = getCompoundSmartSteppingListener ().stopAt
                             (lookupProvider, topFrame, smartSteppingFilter);
                    } else {
                        fsh = getCompoundSmartSteppingListener ().stopHere
                             (lookupProvider, t, smartSteppingFilter) ? StopOrStep.stop() : StopOrStep.skip();
                    }
                }
                if (fsh.isStop()) {
                    // YES!
                    //S ystem.out.println("/nStepAction.exec end - do not resume");
                    loggerStep.fine("Can stop here.");
                    return false; // do not resume
                }
            }

            // do not stop here -> start smart stepping!
            loggerStep.fine("\nSS:  SMART STEPPING START! ********** ");
            int stepDepth = fsh.getStepDepth();
            int stepSize = fsh.getStepSize();
            if (stepSize == 0) {
                stepSize = StepRequestWrapper.size(sr);
            }
            if (stepDepth == 0) {
                boolean useStepFilters = p.getBoolean("UseStepFilters", true);
                boolean stepThrough = useStepFilters && p.getBoolean("StepThroughFilters", false);
                if (!stepThrough || smartSteppingStepOut) {
                    stepDepth = JPDAStep.STEP_OUT;
                } else {
                    stepDepth = StepRequestWrapper.depth(sr);
                }
            }
            boolean actionRun = false;
            if (stepSize == JPDAStep.STEP_LINE) {
                if (stepDepth == JPDAStep.STEP_INTO) {
                    getStepIntoActionProvider ().runAction(StepIntoActionProvider.ACTION_SMART_STEP_INTO, false, lock,
                                                           steppingFromFilteredLocation,
                                                           steppingFromCompoundFilteredLocation);
                    actionRun = true;
                } else if (stepDepth == JPDAStep.STEP_OUT && stepDepth != StepRequestWrapper.depth(sr)) {
                    getStepIntoActionProvider ().runAction(ActionsManager.ACTION_STEP_OUT, false, lock,
                                                           steppingFromFilteredLocation,
                                                           steppingFromCompoundFilteredLocation);
                    actionRun = true;
                }
            }
            if (!actionRun) {
                runAction(stepDepth, stepSize, false, lock,
                          steppingFromFilteredLocation,
                          steppingFromCompoundFilteredLocation);
            }
            /*
            if (!stepThrough || smartSteppingStepOut) {
                loggerStep.fine("Issuing step out, due to smart-stepping.");
                // Assure that the action does not resume anything. Resume is done by Operator.
                getStepIntoActionProvider ().runAction(ActionsManager.ACTION_STEP_OUT, false, lock,
                                                       steppingFromFilteredLocation,
                                                       steppingFromCompoundFilteredLocation);
            } else {
                // Assure that the action does not resume anything. Resume is done by Operator.
                int origDepth = StepRequestWrapper.depth(sr);
                loggerStep.log(Level.FINE, "Issuing step {0}, due to smart-stepping.", origDepth);
                if (origDepth == StepRequest.STEP_OVER) {
                    runAction(ActionsManager.ACTION_STEP_OVER, false, lock);
                    //getStepIntoActionProvider ().runAction(StepIntoActionProvider.ACTION_SMART_STEP_INTO, false);
                } else if (origDepth == StepRequest.STEP_OUT) {
                    runAction(ActionsManager.ACTION_STEP_OUT, false, lock);
                } else { // if (origDepth == StepRequest.STEP_INTO) {
                    getStepIntoActionProvider ().runAction(StepIntoActionProvider.ACTION_SMART_STEP_INTO, false, lock,
                                                           steppingFromFilteredLocation,
                                                           steppingFromCompoundFilteredLocation);
                }
            }
            */
            //S ystem.out.println("/nStepAction.exec end - resume");
            return true; // resume
        } finally {
            lock.unlock();
        }
        } catch (InternalExceptionWrapper e) {
            return false; // Do not resume when something bad happened
        } catch (VMDisconnectedExceptionWrapper e) {
            return false; // Do not resume when disconnected
        } catch (ObjectCollectedExceptionWrapper e) {
            return false; // Do not resume when collected
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
        if (syntheticStep == 0 && lastMethodExitBreakpointListener != null) {
            lastMethodExitBreakpointListener.destroy();
            lastMethodExitBreakpointListener = null;
        }
    }

    private void removeBPListener() {
        syntheticStep = 0;
        if (lastMethodExitBreakpointListener != null) {
            lastMethodExitBreakpointListener.destroy();
            lastMethodExitBreakpointListener = null;
        }
    }
    
    private void setLastOperation(ThreadReference tr) throws VMDisconnectedExceptionWrapper {
        Variable returnValue = null;
        if (lastMethodExitBreakpointListener != null) {
            returnValue = lastMethodExitBreakpointListener.getReturnValue();
        }
        syntheticStep = setLastOperation(tr, getDebuggerImpl(), returnValue);
        if (syntheticStep == 0 && lastMethodExitBreakpointListener != null) {
            lastMethodExitBreakpointListener.destroy();
            lastMethodExitBreakpointListener = null;
        }
    }

    public static int setLastOperation(ThreadReference tr, JPDADebuggerImpl debugger, Variable returnValue) throws VMDisconnectedExceptionWrapper {
        Location loc;
        try {
            loc = StackFrameWrapper.location(ThreadReferenceWrapper.frame(tr, 0));
        } catch (IncompatibleThreadStateException itsex) {
            try {
                int status = ThreadReferenceWrapper.status0(tr);
                if (!(status == ThreadReference.THREAD_STATUS_UNKNOWN ||
                    status == ThreadReference.THREAD_STATUS_ZOMBIE ||
                    status == ThreadReference.THREAD_STATUS_NOT_STARTED)) {
                    
                    Exceptions.printStackTrace(Exceptions.attachMessage(itsex, "Thread's status = "+status+", suspended = "+ThreadReferenceWrapper.isSuspended0(tr)));
                }
            } catch (IllegalThreadStateExceptionWrapper ex) {
                // A bad state - ignore
            }
            logger.log(Level.FINE, "Incompatible Thread State: {0}", itsex.getLocalizedMessage());
            return 0;
        } catch (IllegalThreadStateExceptionWrapper itsex) {
            return 0;
        } catch (InternalExceptionWrapper iex) {
            return 0;
        } catch (IndexOutOfBoundsException iobe) {
            // no frames on stack
            return 0;
        } catch (InvalidStackFrameExceptionWrapper iex) {
            return 0;
        } catch (ObjectCollectedExceptionWrapper iex) {
            return 0;
        }
        try {
            int syntheticStep = JPDAStepImpl.isSyntheticMethod(LocationWrapper.method(loc), loc);
            if (syntheticStep != 0) {
                // Ignore synthetic methods
                return syntheticStep;
            }
        } catch (InternalExceptionWrapper ex) {
        }
        Session currentSession = DebuggerManager.getDebuggerManager().getCurrentSession();
        String language = currentSession == null ? null : currentSession.getCurrentLanguage();
        SourcePath sourcePath = debugger.getEngineContext();
        String url;
        try {
            url = sourcePath.getURL(loc, language);
        } catch (InternalExceptionWrapper iex) {
            return 0;
        } catch (ObjectCollectedExceptionWrapper iex) {
            return 0;
        }
        setOperationsLazily(debugger.getThread(tr), returnValue, debugger, loc, url);
        return 0;
    }
    
    private static final long OPERATION_TIMEOUT = 200;
    
    private static void setOperationsLazily(final JPDAThreadImpl jtr,
                                            final Variable returnValue,
                                            final JPDADebuggerImpl debugger,
                                            final Location loc,
                                            final String url) throws VMDisconnectedExceptionWrapper {
        
        final Object[] finishedInTime = new Object[] { null };
        final Task task = operationsRP.create(new Runnable() {
            @Override
            public void run() {
                ExpressionPool exprPool = debugger.getExpressionPool();
                ExpressionPool.Expression expr = exprPool.getExpressionAt(loc, url);
                if (expr == null) {
                    return ;
                }
                Operation[] ops = expr.getOperations();
                // code index right after the method call (step out)
                int codeIndex;
                byte[] bytecodes;
                try {
                    codeIndex = (int) LocationWrapper.codeIndex(loc);
                    bytecodes = MethodWrapper.bytecodes(LocationWrapper.method(loc));
                } catch (InternalExceptionWrapper iex) {
                    return ;
                } catch (VMDisconnectedExceptionWrapper vmdex) {
                    synchronized (finishedInTime) {
                        finishedInTime[0] = vmdex;
                    }
                    return;
                }
                if (codeIndex >= 5 && (bytecodes[codeIndex - 5] & 0xFF) == 185) { // invokeinterface
                    codeIndex -= 5;
                } else {
                    codeIndex -= 3; // invokevirtual, invokespecial, invokestatic
                }
                int opIndex = expr.findNextOperationIndex(codeIndex - 1);
                Operation lastOperation;
                if (opIndex >= 0 && ops[opIndex].getBytecodeIndex() == codeIndex) {
                    lastOperation = ops[opIndex];
                } else {
                    return ;
                }
                synchronized (jtr) {
                    boolean needsUpdate;
                    synchronized (finishedInTime) {
                        if (finishedInTime[0] == Boolean.FALSE) {
                            // Too late
                            //logger.severe("Operations computed, but it's too late.");
                            return;
                        }
                        needsUpdate = finishedInTime[0] == Boolean.TRUE;
                    }
                    lastOperation.setReturnValue(returnValue);
                    if (needsUpdate) {
                        jtr.updateLastOperation(lastOperation);
                        //logger.severe("Operations computed, updated.");
                    } else {
                        jtr.addLastOperation(lastOperation);
                        jtr.setCurrentOperation(lastOperation);
                        //logger.severe("Operations computed, set.");
                    }
                }
            }
        });
        PropertyChangeListener threadChangeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                synchronized (finishedInTime) {
                    finishedInTime[0] = Boolean.FALSE;
                    task.cancel();
                }
                jtr.removePropertyChangeListener(JPDAThreadImpl.PROP_OPERATIONS_SET, this);
                //logger.severe("Operations were SET. Listener removed.");
            }
        };
        jtr.addPropertyChangeListener(JPDAThreadImpl.PROP_OPERATIONS_SET, threadChangeListener);
        task.schedule(0);
        try {
            boolean finished = task.waitFinished(OPERATION_TIMEOUT);
            synchronized (finishedInTime) {
                if (finishedInTime[0] instanceof VMDisconnectedExceptionWrapper) {
                    throw (VMDisconnectedExceptionWrapper) finishedInTime[0];
                }
                if (!finished) {
                    finishedInTime[0] = Boolean.TRUE;
                }
            }
        } catch (InterruptedException ex) {
        }
    }
    
    private StepIntoActionProvider stepIntoActionProvider;
    
    private StepIntoActionProvider getStepIntoActionProvider () {
        if (stepIntoActionProvider == null) {
            stepIntoActionProvider = StepIntoActionProvider.instanceByContext.get(lookupProvider).get();
        }
        return stepIntoActionProvider;
    }

    private SmartSteppingFilterWrapper smartSteppingFilter;
    
    private CompoundSmartSteppingListener compoundSmartSteppingListener;
    
    private CompoundSmartSteppingListener getCompoundSmartSteppingListener () {
        if (compoundSmartSteppingListener == null)
            compoundSmartSteppingListener = lookupProvider.lookupFirst(null, CompoundSmartSteppingListener.class);
        return compoundSmartSteppingListener;
    }
}
