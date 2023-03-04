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

package org.netbeans.modules.debugger.jpda.breakpoints;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.Session;

import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.EvaluatorExpression;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidRequestStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MirrorWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.PrimitiveValueWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ValueWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.EventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.WatchpointEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.StepRequestWrapper;
import org.netbeans.modules.debugger.jpda.models.AbstractObjectVariable;
import org.netbeans.modules.debugger.jpda.models.ExceptionVariableImpl;
import org.netbeans.modules.debugger.jpda.models.FieldReadVariableImpl;
import org.netbeans.modules.debugger.jpda.models.FieldToBeVariableImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.models.ReturnVariableImpl;
import org.netbeans.modules.debugger.jpda.util.ConditionedExecutor;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;


/**
 *
 * @author   Jan Jancura
 */
public abstract class BreakpointImpl implements ConditionedExecutor, PropertyChangeListener {
    
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.breakpoints"); // NOI18N

    private final JPDADebuggerImpl    debugger;
    private final JPDABreakpoint      breakpoint;
    private final BreakpointsReader   reader;
    private EvaluatorExpression compiledCondition;
    private List<EventRequest>  requests = new LinkedList<EventRequest>();
    private int                 hitCountFilter = 0;
    private int                 customHitCount;
    private int                 customHitCountFilter = 0;
    private List<HitCountListener> hcListeners = new CopyOnWriteArrayList<HitCountListener>();
    private volatile int        breakCount = 0;

    protected BreakpointImpl (JPDABreakpoint p, BreakpointsReader reader, JPDADebuggerImpl debugger, Session session) {
        this.debugger = debugger;
        this.reader = reader;
        breakpoint = p;
    }

    /**
     * Called from XXXBreakpointImpl constructor only.
     */
    final void set () {
        breakpoint.addPropertyChangeListener (this);
        debugger.addPropertyChangeListener(JPDADebugger.PROP_BREAKPOINTS_ACTIVE, this);
        if (breakpoint instanceof PropertyChangeListener && isApplicable()) {
            Session s = debugger.getSession();
            DebuggerEngine de = s.getEngineForLanguage ("Java");
            ((PropertyChangeListener) breakpoint).propertyChange(new PropertyChangeEvent(this, DebuggerEngine.class.getName(), null, de));
        }
        update ();
    }
    
    /**
     * Called when Fix&Continue is invoked. Reqritten in LineBreakpointImpl.
     */
    void fixed () {
        if (reader != null) {
            reader.storeCachedClassName(breakpoint, null);
        }
        update ();
    }
    
    /**
     * Called from set () and propertyChanged.
     */
    final void update () {
        if ( (getVirtualMachine () == null) ||
             (getDebugger ().getState () == JPDADebugger.STATE_DISCONNECTED)
        ) return;
        removeAllEventRequests ();
        if (canSetRequests()) {
            setRequests ();
        }
    }
    
    protected final boolean canSetRequests() {
        return breakpoint.isEnabled () &&
               isEnabled() &&
               debugger.getBreakpointsActive();
    }

    protected boolean isApplicable() {
        return true;
    }
    
    protected boolean isEnabled() {
        return true;
    }
    
    protected final void setValidity(Breakpoint.VALIDITY validity, String reason) {
        if (breakpoint instanceof ChangeListener) {
            ((ChangeListener) breakpoint).stateChanged(new ValidityChangeEvent(validity, reason));
        }
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (Breakpoint.PROP_DISPOSED.equals(propertyName)) {
            remove();
        } else if (!Breakpoint.PROP_VALIDITY.equals(propertyName) &&
                   !Breakpoint.PROP_GROUP_NAME.equals(propertyName) &&
                   !Breakpoint.PROP_GROUP_PROPERTIES.equals(propertyName)) {
            if (reader != null && !JPDADebugger.PROP_BREAKPOINTS_ACTIVE.equals(propertyName)) {
                reader.storeCachedClassName(breakpoint, null);
            }
            debugger.getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    // Update lazily in RP. We'll access java source parsing and JDI.
                    update();
                }
            });
        }
    }

    protected abstract void setRequests ();
    
    protected void remove () {
        if (Mutex.EVENT.isReadAccess()) {
            // One can not want to access the requests in AWT EQ
            debugger.getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    removeAllEventRequests ();
                }
            });
        } else {
            removeAllEventRequests ();
        }
        breakpoint.removePropertyChangeListener(this);
        debugger.removePropertyChangeListener(JPDADebugger.PROP_BREAKPOINTS_ACTIVE, this);
        setValidity(Breakpoint.VALIDITY.UNKNOWN, null);
        if (breakpoint instanceof PropertyChangeListener) {
            Session s = debugger.getSession();
            DebuggerEngine de = s.getEngineForLanguage ("Java");
            ((PropertyChangeListener) breakpoint).propertyChange(new PropertyChangeEvent(this, DebuggerEngine.class.getName(), de, null));
        }
        compiledCondition = null;
    }

    protected JPDABreakpoint getBreakpoint () {
        return breakpoint;
    }

    protected JPDADebuggerImpl getDebugger () {
        return debugger;
    }

    protected VirtualMachine getVirtualMachine () {
        return getDebugger ().getVirtualMachine ();
    }
    
    protected EventRequestManager getEventRequestManager () throws VMDisconnectedExceptionWrapper, InternalExceptionWrapper {
        VirtualMachine vm = getVirtualMachine();
        if (vm == null) {
            // Already disconnected
            throw new VMDisconnectedExceptionWrapper(new VMDisconnectedException());
        }
        return VirtualMachineWrapper.eventRequestManager (vm);
    }

    void addEventRequest (EventRequest r) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ObjectCollectedExceptionWrapper, InvalidRequestStateExceptionWrapper, RequestNotSupportedException {
        addEventRequest(r, customHitCountFilter != 0);
    }
    
    protected final void setCustomHitCountFilter(int customHitCountFilter) {
        this.customHitCountFilter = customHitCountFilter;
    }
    
    synchronized void addEventRequest (EventRequest r, boolean ignoreHitCount) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ObjectCollectedExceptionWrapper, InvalidRequestStateExceptionWrapper, RequestNotSupportedException {
        logger.log(Level.FINE, "BreakpointImpl addEventRequest: {0}", r);
        requests.add (r);
        getDebugger ().getOperator ().register (r, this);
       
        // PATCH #48174
        // if this is breakpoint with SUSPEND_NONE we stop EVENT_THREAD to print output line
        if (getBreakpoint().getSuspend() == JPDABreakpoint.SUSPEND_ALL)
            EventRequestWrapper.setSuspendPolicy (r, JPDABreakpoint.SUSPEND_ALL);
        else
            EventRequestWrapper.setSuspendPolicy (r, JPDABreakpoint.SUSPEND_EVENT_THREAD);
        r.putProperty("brkpSuspend", getBreakpoint().getSuspend()); // Remember the original breakpoint suspend property
        int hitCountFilter = getBreakpoint().getHitCountFilter();
        if (!ignoreHitCount && hitCountFilter > 0) {
            switch (getBreakpoint().getHitCountFilteringStyle()) {
                case MULTIPLE:
                    this.hitCountFilter = hitCountFilter;
                    break;
                case EQUAL:
                    this.hitCountFilter = 0;
                    break;
                case GREATER:
                    this.hitCountFilter = -1;
                    hitCountFilter++;
                    break;
                default:
                    throw new IllegalStateException(getBreakpoint().getHitCountFilteringStyle().name());
            }
            EventRequestWrapper.addCountFilter(r, hitCountFilter);
        } else {
            this.hitCountFilter = 0;
        }
        try {
            EventRequestWrapper.enable (r);
        } catch (InternalExceptionWrapper e) {
            getDebugger ().getOperator ().unregister (r);
            throw e;
        } catch (ObjectCollectedExceptionWrapper e) {
            getDebugger ().getOperator ().unregister (r);
            throw e;
        } catch (VMDisconnectedExceptionWrapper e) {
            getDebugger ().getOperator ().unregister (r);
            throw e;
        } catch (InvalidRequestStateExceptionWrapper e) {
            getDebugger ().getOperator ().unregister (r);
            throw e;
        } catch (UnsupportedOperationException uoex) {
            // see https://netbeans.org/bugzilla/show_bug.cgi?id=241333
            throw new RequestNotSupportedException(r);
        }
    }

    protected synchronized void removeAllEventRequests () {
        if (requests.isEmpty()) return;
        VirtualMachine vm = getDebugger().getVirtualMachine();
        if (vm == null) return; 
        int i, k = requests.size ();
        try {
            for (i = 0; i < k; i++) { 
                EventRequest r = requests.get (i);
                logger.log(Level.FINE, "BreakpointImpl removeEventRequest: {0}", r);
                try {
                    EventRequestManagerWrapper.deleteEventRequest(
                            VirtualMachineWrapper.eventRequestManager(vm),
                            r);
                } catch (InvalidRequestStateExceptionWrapper irex) {}
                getDebugger ().getOperator ().unregister (r);
            }
            
        } catch (VMDisconnectedExceptionWrapper e) {
        } catch (InternalExceptionWrapper e) {
        }
        requests = new LinkedList<EventRequest>();
    }
    
    private synchronized void removeEventRequest(EventRequest r) {
        VirtualMachine vm = getDebugger().getVirtualMachine();
        if (vm == null) return; 
        try {
            logger.log(Level.FINE, "BreakpointImpl removeEventRequest: {0}", r);
            try {
                EventRequestManagerWrapper.deleteEventRequest(
                        VirtualMachineWrapper.eventRequestManager(vm),
                        r);
            } catch (InvalidRequestStateExceptionWrapper irex) {}
            getDebugger ().getOperator ().unregister (r);
        } catch (VMDisconnectedExceptionWrapper e) {
        } catch (InternalExceptionWrapper e) {
        }
        requests.remove(r);
    }
    
    protected final List<EventRequest> getEventRequests() {
        List<EventRequest> ers;
        synchronized (this) {
            ers = new LinkedList<EventRequest>(requests);
            ers = Collections.unmodifiableList(ers);
        }
        return ers;
    }
    
    /** Called when a new event request needs to be created, e.g. after hit count
     * was met and hit count style is "greater than".
     */
    protected abstract EventRequest createEventRequest(EventRequest oldRequest) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper;

    private final Map<Event, Variable> processedReturnVariable = new HashMap<Event, Variable>();
    private final Map<Event, Throwable> conditionException = new HashMap<Event, Throwable>();
    
    private Boolean processCustomHitCount() {
        customHitCount++;
        fireHitCountChanged();
        if (customHitCountFilter > 0) {
            switch (breakpoint.getHitCountFilteringStyle()) {
                case MULTIPLE:
                    if ((customHitCount % customHitCountFilter) != 0) {
                        return false;
                    }
                    break;
                case EQUAL:
                    if (customHitCountFilter != customHitCount) {
                        return false;
                    }
                    //customHitCountFilter = 0;
                    removeAllEventRequests();
                    break;
                case GREATER:
                    if (customHitCount <= customHitCountFilter) {
                        return false;
                    }
                    break;
                default:
                    throw new IllegalStateException(getBreakpoint().getHitCountFilteringStyle().name());
            }
        }
        return null;
    }

    boolean processCondition(
            Event event,
            String condition,
            ThreadReference threadReference,
            Value returnValue) {
        
        return processCondition(event, condition, threadReference, returnValue, null);
    }

    boolean processCondition(
            Event event,
            String condition,
            ThreadReference threadReference,
            Value returnValue,
            ObjectReference contextValue) {

        Boolean CHCprocessed = processCustomHitCount();
        if (CHCprocessed != null) {
            return CHCprocessed.booleanValue();
        }
        try {
            EventRequest request = EventWrapper.request(event);
            if (customHitCountFilter == 0) {
                if (hitCountFilter > 0) {
                    EventRequestWrapper.disable(request);
                    //event.request().addCountFilter(hitCountFilter);
                    // This submits the event with the filter again
                    // The request was enabled before, we should not get UnsupportedOperationException
                    EventRequestWrapper.enable(request);
                }
                if (hitCountFilter == -1) {
                    EventRequestWrapper.disable(request);
                    removeEventRequest(request);
                    try {
                        addEventRequest(createEventRequest(request), true);
                    } catch (RequestNotSupportedException ex) {
                        Exceptions.printStackTrace(ex);
                        return true; // Stop
                    }
                }
            }

            Variable variable = null;
            if (getBreakpoint() instanceof MethodBreakpoint &&
                    (((MethodBreakpoint) getBreakpoint()).getBreakpointType()
                     & MethodBreakpoint.TYPE_METHOD_EXIT) != 0) {
                if (returnValue != null) {
                    JPDAThreadImpl jt = getDebugger().getThread(threadReference);
                    ReturnVariableImpl retVariable = new ReturnVariableImpl(getDebugger(), returnValue, "", jt.getMethodName());
                    jt.setReturnVariable(retVariable);
                    variable = retVariable;
                }
            }
            boolean success;
            if (condition != null && condition.length() > 0) {
                //PATCH 48174
                try {
                    getDebugger().setAltCSF(ThreadReferenceWrapper.frame(threadReference, 0));
                } catch (com.sun.jdi.IncompatibleThreadStateException e) {
                    String msg = JPDAThreadImpl.getThreadStateLog(threadReference);
                    Logger.getLogger(BreakpointImpl.class.getName()).log(Level.INFO, msg, e);
                } catch (ObjectCollectedExceptionWrapper e) {
                } catch (IllegalThreadStateExceptionWrapper e) {
                    return false; // Let it go, the thread is dead.
                } catch (java.lang.IndexOutOfBoundsException e) {
                    // No frame in case of Thread and "Main" class breakpoints, PATCH 56540
                }
                AbstractObjectVariable contextVar = (contextValue != null) ?
                        new AbstractObjectVariable(getDebugger(), contextValue, null) :
                        null;
                success = evaluateCondition (
                        event,
                        condition,
                        threadReference,
                        contextVar
                    );
                getDebugger().setAltCSF(null);
            } else {
                compiledCondition = null;
                success = true;
            }
            if (success) { // perform() will be called, store the data
                processedReturnVariable.put(event, variable);
            }
            return success;
        } catch (InternalExceptionWrapper iex) {
            return true; // Stop here
        } catch (ObjectCollectedExceptionWrapper iex) {
            return true; // Stop here
        } catch (VMDisconnectedExceptionWrapper iex) {
            return false; // Let it go
        } catch (InvalidRequestStateExceptionWrapper irsex) {
            return false; // Deleted - let it go
        }
    }

    protected boolean perform (
        Event event,
        ThreadReference threadReference,
        ReferenceType referenceType,
        Value value
    ) {
        //S ystem.out.println("BreakpointImpl.perform");
        boolean resume;
        
        Variable variable = processedReturnVariable.remove(event);
        if (variable == null) {
            try {
                variable = createBreakpointVariable(event, value, referenceType);
            } catch (InternalExceptionWrapper ex) {
            } catch (VMDisconnectedExceptionWrapper ex) {
            } catch (ObjectCollectedExceptionWrapper ex) {
            }
        }
        JPDABreakpointEvent e;
        Throwable cEx = conditionException.remove(event);
        if (cEx == null) {
            e = new JPDABreakpointEvent (
                getBreakpoint (),
                debugger,
                (compiledCondition == null) ? JPDABreakpointEvent.CONDITION_NONE :
                                              JPDABreakpointEvent.CONDITION_TRUE,
                debugger.getThread (threadReference), 
                referenceType, 
                variable
            );
        } else {
            e = new JPDABreakpointEvent (
                getBreakpoint (),
                debugger,
                cEx,
                debugger.getThread (threadReference),
                referenceType,
                variable
            );
        }
        try {
            java.lang.reflect.Field f = e.getClass().getDeclaredField("event"); // NOI18N
            f.setAccessible(true);
            f.set(e, event);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        breakCount++;
        fireHitCountChanged();
        getDebugger ().fireBreakpointEvent (
            getBreakpoint (),
            e
        );
        enableDisableDependentBreakpoints();
        Integer brkpSuspend = (Integer) event.request().getProperty("brkpSuspend");
        if (brkpSuspend == null) {
            brkpSuspend = getBreakpoint().getSuspend();
        }
        resume = brkpSuspend.intValue() == JPDABreakpoint.SUSPEND_NONE || e.getResume ();
        logger.log(Level.FINE, "BreakpointImpl: perform breakpoint: {0} resume: {1}", new Object[]{this, resume});
        if (threadReference != null) {
            if (!resume) {
                try {
                    resume = checkWhetherResumeToFinishStep(threadReference);
                } catch (InternalExceptionWrapper ex) {
                    return false;
                } catch (VMDisconnectedExceptionWrapper ex) {
                    return false;
                }
            }
            if (!resume) {
                getDebugger().getThread(threadReference).setCurrentBreakpoint(breakpoint, e);
            }
        }
        //S ystem.out.println("BreakpointImpl.perform end");
        return resume; 
    }
    
    private Variable createBreakpointVariable(Event event, Value value, ReferenceType referenceType)
                                              throws InternalExceptionWrapper,
                                                     VMDisconnectedExceptionWrapper,
                                                     ObjectCollectedExceptionWrapper {
        Variable var;
        if (event instanceof ExceptionEvent && value instanceof ObjectReference) {
            String exceptionClassName;
            exceptionClassName = ReferenceTypeWrapper.name(ObjectReferenceWrapper.referenceType((ObjectReference) value));
            var = new ExceptionVariableImpl(debugger, value, null, exceptionClassName);
        } else if (event instanceof AccessWatchpointEvent) {
            AccessWatchpointEvent aevent = (AccessWatchpointEvent) event;
            Field field = WatchpointEventWrapper.field(aevent);
            ObjectReference or = WatchpointEventWrapper.object(aevent);
            org.netbeans.api.debugger.jpda.Field fieldVar = AbstractObjectVariable.getField(debugger, field, or, null);
            var = new FieldReadVariableImpl(debugger, value, null, fieldVar);
        } else if (event instanceof ModificationWatchpointEvent) {
            ModificationWatchpointEvent mevent = (ModificationWatchpointEvent) event;
            Field field = WatchpointEventWrapper.field(mevent);
            ObjectReference or = WatchpointEventWrapper.object(mevent);
            org.netbeans.api.debugger.jpda.Field fieldVar = AbstractObjectVariable.getField(debugger, field, or, null);
            var = new FieldToBeVariableImpl(debugger, value, null, fieldVar);
        } else {
            var = debugger.getVariable(value);
        }
        return var;
    }
    
    private void enableDisableDependentBreakpoints() {
        Set<Breakpoint> breakpoints = breakpoint.getBreakpointsToEnable();
        for (Breakpoint b : breakpoints) {
            b.enable();
        }
        breakpoints = breakpoint.getBreakpointsToDisable();
        for (Breakpoint b : breakpoints) {
            b.disable();
        }
    }
    
    private boolean checkWhetherResumeToFinishStep(ThreadReference thread) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        List<StepRequest> stepRequests = EventRequestManagerWrapper.stepRequests(
                VirtualMachineWrapper.eventRequestManager(MirrorWrapper.virtualMachine(thread)));
        if (stepRequests.size() > 0) {
            logger.log(Level.FINE, "checkWhetherResumeToFinishStep() stepRequests = {0}", stepRequests);
            int suspendState = breakpoint.getSuspend();
            if (suspendState == JPDABreakpoint.SUSPEND_ALL ||
                suspendState == JPDABreakpoint.SUSPEND_EVENT_THREAD) {

                boolean thisThreadHasStep = false;
                List<StepRequest> activeStepRequests = new ArrayList<StepRequest>(stepRequests);
                List<ThreadReference> steppingThreads = new ArrayList<>(stepRequests.size());
                for (int i = 0; i < activeStepRequests.size(); i++) {
                    StepRequest step = activeStepRequests.get(i);
                    ThreadReference stepThread = StepRequestWrapper.thread(step);
                    if (!EventRequestWrapper.isEnabled(step)) {
                        activeStepRequests.remove(i);
                        continue;
                    }
                    int stepThreadStatus;
                    try {
                        stepThreadStatus = ThreadReferenceWrapper.status(StepRequestWrapper.thread(step));
                    } catch (ObjectCollectedExceptionWrapper ocex) {
                        stepThreadStatus = ThreadReference.THREAD_STATUS_ZOMBIE;
                    } catch (IllegalThreadStateExceptionWrapper ex) {
                        stepThreadStatus = ThreadReference.THREAD_STATUS_ZOMBIE;
                    }
                    if (stepThreadStatus == ThreadReference.THREAD_STATUS_ZOMBIE) {
                        try {
                            EventRequestManagerWrapper.deleteEventRequest(
                                    VirtualMachineWrapper.eventRequestManager(MirrorWrapper.virtualMachine(thread)),
                                    step);
                        } catch (InvalidRequestStateExceptionWrapper irex) {}
                        debugger.getOperator().unregister(step);
                        activeStepRequests.remove(i);
                        continue;
                    }
                    if (thread.equals(stepThread)) {
                        thisThreadHasStep = true;
                    }
                    steppingThreads.add(stepThread);
                }
                if (thisThreadHasStep) { // remove this if the debugger should warn you in the same thread as well. See #104101.
                    return false;
                }
                if (activeStepRequests.size() > 0 && (thisThreadHasStep || suspendState == JPDABreakpoint.SUSPEND_ALL)) {
                    boolean resume;
                    Boolean resumeDecision = debugger.getStepInterruptByBptResumeDecision();
                    if (resumeDecision != null) {
                        resume = resumeDecision.booleanValue();
                    } else {
                        resume = false;
                    }
                    if (!resume) {
                        List<JPDAThreadImpl> jsts = new ArrayList<JPDAThreadImpl>(steppingThreads.size());
                        for (ThreadReference tr : steppingThreads) {
                            jsts.add(debugger.getThread(tr));
                        }
                        JPDAThreadImpl tr = debugger.getThread(thread);
                        tr.setStepSuspendedBy(breakpoint, resumeDecision == null, jsts);
                    }
                    return resume;
                    /*final String message;
                    if (thisThreadHasStep) {
                        message = NbBundle.getMessage(BreakpointImpl.class,
                                "MSG_StepThreadInterruptedByBR",
                                breakpoint.toString());
                    } else {
                        message = NbBundle.getMessage(BreakpointImpl.class,
                                "MSG_StepInterruptedByBR",
                                breakpoint.toString(),
                                thread.name(),
                                activeStepRequests.get(0).thread().name());
                    }
                    final ThreadInfoPanel[] tiPanelRef = new ThreadInfoPanel[] { null };
                    try {
                        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                tiPanelRef[0] = ThreadInfoPanel.create(message,
                                        NbBundle.getMessage(BreakpointImpl.class, "StepInterruptedByBR_Btn1"),
                                        NbBundle.getMessage(BreakpointImpl.class, "StepInterruptedByBR_Btn1_TIP"),
                                        NbBundle.getMessage(BreakpointImpl.class, "StepInterruptedByBR_Btn2"),
                                        NbBundle.getMessage(BreakpointImpl.class, "StepInterruptedByBR_Btn2_TIP"));
                            }
                        });
                    } catch (InterruptedException iex) {
                    } catch (java.lang.reflect.InvocationTargetException itex) {
                        ErrorManager.getDefault().notify(itex);
                    }
                    if (tiPanelRef[0] == null) {
                        return false;
                    }
                    tiPanelRef[0].setButtonListener(new ThreadInfoPanel.ButtonListener() {
                        public void buttonPressed(int n) {
                            if (n == 2) {
                                debugger.setStepInterruptByBptResumeDecision(Boolean.TRUE);
                            }
                            debugger.resume();
                        }
                    });
                    debugger.addPropertyChangeListener(new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent pe) {
                            if (pe.getPropertyName().equals(debugger.PROP_STATE)) {
                                if (pe.getNewValue().equals(debugger.STATE_RUNNING) ||
                                    pe.getNewValue().equals(debugger.STATE_DISCONNECTED)) {
                                    debugger.removePropertyChangeListener(this);
                                    tiPanelRef[0].dismiss();
                                }
                            }
                        }
                    });*/
                    
                    /*
                    JCheckBox cb = new JCheckBox(NbBundle.getMessage(BreakpointImpl.class, "RememberDecision"));
                    DialogDescriptor dd = new DialogDescriptor(
                            //message,
                            createDlgPanel(message, cb),
                            new NotifyDescriptor.Confirmation(message, NotifyDescriptor.YES_NO_OPTION).getTitle(),
                            true,
                            NotifyDescriptor.YES_NO_OPTION,
                            null,
                            null);
                    dd.setMessageType(NotifyDescriptor.QUESTION_MESSAGE);
                    // Set the stopped state to show the breakpoint location
                    DebuggerManager.getDebuggerManager().setCurrentSession(session);
                    getDebugger ().setStoppedState (thread);
                    Object option = org.openide.DialogDisplayer.getDefault().notify(dd);
                    boolean yes = option == NotifyDescriptor.YES_OPTION;
                    boolean no  = option == NotifyDescriptor.NO_OPTION;
                    if (cb.isSelected() && (yes || no)) {
                        debugger.setStepInterruptByBptResumeDecision(Boolean.valueOf(yes));
                    }
                    if (yes) {
                        // We'll resume...
                        getDebugger ().setRunningState();
                    }
                    if (no) {
                        // The user wants to stop on the breakpoint, remove
                        // the step requests to prevent confusion
                        for (StepRequest step : activeStepRequests) {
                            thread.virtualMachine().eventRequestManager().deleteEventRequest(step);
                            debugger.getOperator().unregister(step);
                        }
                    }
                    return yes;
                     */
                }
            }
        }
        return false;
    }

    /*
    private static JPanel createDlgPanel(String message, JCheckBox cb) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        JTextArea area = new JTextArea(message);
        Color color = UIManager.getColor("Label.background"); // NOI18N
        if (color != null) {
            area.setBackground(color);
        }
        //area.setLineWrap(true);
        //area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setTabSize(4); // looks better for module sys messages than 8
        panel.add(area, c);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new java.awt.Insets(12, 0, 0, 0);
        panel.add(cb, c);
        return panel;
    }
     */
    
    private boolean evaluateCondition (
        Event event,
        String condition,
        ThreadReference thread,
        ObjectVariable contextVar
    ) {
        try {
            try {
                boolean success;
                JPDAThreadImpl jtr = debugger.getThread(thread);
                jtr.accessLock.writeLock().lock();
                try {
                    CallStackFrame[] csfs = jtr.getCallStack(0, 1);
                    if (csfs.length > 0) {
                        success = evaluateConditionIn (condition, csfs[0], contextVar);
                    } else {
                        // Can not evaluate any condition without the top stack frame.
                        success = true;
                    }
                } finally {
                    jtr.accessLock.writeLock().unlock();
                }
                // condition true => stop here (do not resume)
                // condition false => resume
                logger.log(Level.FINE,
                           "BreakpointImpl: perform breakpoint (condition = {0}): {1} resume: {2}",
                           new Object[]{success, this, !success});
                return success;
            } catch (InvalidExpressionException ex) {
                conditionException.put(event, ex);
                logger.log(Level.FINE,
                           "BreakpointImpl: perform breakpoint (bad condition): ''{0}'', got {1}",
                           new Object[]{condition, ex.getMessage()});
                return true; // Act as if the condition was satisfied when it's invalid
            }
        /*} catch (IncompatibleThreadStateException ex) {
            // should not occurre
            Exceptions.printStackTrace(ex);
            return true; // Act as if the condition was satisfied when an error occurs
        } catch (IllegalThreadStateExceptionWrapper ex) {
            return true;
        } catch (ObjectCollectedExceptionWrapper ex) {
            return true;
        } catch (InternalExceptionWrapper ex) {
            return true;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return true;*/
        } catch (AbsentInformationException abex) {
            logger.log(Level.INFO, condition, abex);
            return true;
        }
    }

    /*private boolean evaluateCondition (
        String condition,
        ThreadReference thread,
        ReferenceType referenceType,
        Variable variable
    ) {
        try {
            try {
                boolean result;
                JPDABreakpointEvent ev;
                synchronized (debugger.LOCK) {
                    StackFrame sf = thread.frame (0);
                    result = evaluateConditionIn (condition, sf, 0);
                    ev = new JPDABreakpointEvent (
                        getBreakpoint (),
                        debugger,
                        result ?
                            JPDABreakpointEvent.CONDITION_TRUE :
                            JPDABreakpointEvent.CONDITION_FALSE,
                        debugger.getThread (thread),
                        referenceType,
                        variable
                    );
                }
                getDebugger ().fireBreakpointEvent (
                    getBreakpoint (),
                    ev
                );

                // condition true => stop here (do not resume)
                // condition false => resume
                logger.fine("BreakpointImpl: perform breakpoint (condition = " + result + "): " + this + " resume: " + (!result || ev.getResume ()));
                return !result || ev.getResume ();
            } catch (ParseException ex) {
                JPDABreakpointEvent ev = new JPDABreakpointEvent (
                    getBreakpoint (),
                    debugger,
                    ex,
                    debugger.getThread (thread),
                    referenceType,
                    variable
                );
                getDebugger ().fireBreakpointEvent (
                    getBreakpoint (),
                    ev
                );
                logger.fine("BreakpointImpl: perform breakpoint (bad condition): " + this + " resume: " + ev.getResume ());
                return ev.getResume ();
            } catch (InvalidExpressionException ex) {
                JPDABreakpointEvent ev = new JPDABreakpointEvent (
                    getBreakpoint (),
                    debugger,
                    ex,
                    debugger.getThread (thread),
                    referenceType,
                    variable
                );
                getDebugger ().fireBreakpointEvent (
                    getBreakpoint (),
                    ev
                );
                logger.fine("BreakpointImpl: perform breakpoint (invalid condition): " + this + " resume: " + ev.getResume ());
                return ev.getResume ();
            }
        } catch (IncompatibleThreadStateException ex) {
            // should not occurre
            ex.printStackTrace ();
        }
        // some error occured during evaluation of expression => do not resume
        return false; // do not resume
    }*/

    /**
     * Evaluates given condition. Returns value of condition evaluation. 
     * Returns true othervise (bad expression).
     */
    private boolean evaluateConditionIn (
        String condExpr,
        CallStackFrame csf,
        ObjectVariable contextVariable
        /*StackFrame frame,
        int frameDepth*/
    ) throws InvalidExpressionException {
        // 1) compile expression
        if ( compiledCondition == null ||
             !compiledCondition.getExpression ().equals (condExpr)
        ) {
            compiledCondition = new EvaluatorExpression(condExpr);
        }
        
        // 2) evaluate expression
        // already synchronized (debugger.LOCK)
        com.sun.jdi.Value value = getDebugger().evaluateIn(compiledCondition, csf, contextVariable);
        /* Uncomment if evaluator returns a variable with disabled collection.
           When not used any more, it's collection must be enabled again.
        if (value instanceof ObjectReference) {
            try {
                ObjectReferenceWrapper.enableCollection((ObjectReference) value);
            } catch (Exception ex) {}
        }*/
        try {
            return PrimitiveValueWrapper.booleanValue((com.sun.jdi.BooleanValue) value);
        } catch (ClassCastException e) {
            try {
                throw new InvalidExpressionException("Expecting boolean value instead of " + ValueWrapper.type(value));
            } catch (InternalExceptionWrapper ex) {
                throw new InvalidExpressionException("Expecting boolean value");
            } catch (VMDisconnectedExceptionWrapper ex) {
                throw new InvalidExpressionException("Expecting boolean value");
            } catch (ObjectCollectedExceptionWrapper ex) {
                throw new InvalidExpressionException("Expecting boolean value");
            }
        } catch (NullPointerException npe) {
            throw new InvalidExpressionException (npe);
        } catch (InternalExceptionWrapper ex) {
            return true;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return true;
        }
    }
    
    /**
     * Support method for simple patterns.
     */
    static boolean match (String name, String pattern) {
        if (pattern.startsWith ("*"))
            return name.endsWith (pattern.substring (1));
        else
        if (pattern.endsWith ("*"))
            return name.startsWith (
                pattern.substring (0, pattern.length () - 1)
            );
        return name.equals (pattern);
    }
    
    /**
     * @return The current hit count, or <code>-1</code> when unknown.
     */
    public int getCurrentHitCount() {
        if (customHitCountFilter > 0) {
            return customHitCount;
        } else if (breakpoint.getHitCountFilter() <= 0) {
            // No JDI hit count involvement
            return customHitCount;
        } else {
            // JDI hit count filter applied. We do not know how many times is the breakpoint hit actually.
            return -1;
        }
    }
    
    public int getCurrentBreakCounts() {
        return breakCount;
    }
    
    /**
     * @return The remaining hit counts till the breakpoint breaks execution, or <code>-1</code> when unknown.
     */
    public int getHitCountsTillBreak() {
        if (customHitCountFilter > 0) {
            switch (breakpoint.getHitCountFilteringStyle()) {
                case MULTIPLE:
                    return customHitCountFilter - (customHitCount % customHitCountFilter);
                case EQUAL:
                    int tb = customHitCountFilter - customHitCount;
                    if (tb < 0) {
                        tb = 0;
                    }
                    return tb;
                case GREATER:
                    tb = customHitCountFilter - customHitCount;
                    if (tb <= 0) {
                        tb = 1;
                    }
                    return tb;
                default:
                    throw new IllegalStateException(getBreakpoint().getHitCountFilteringStyle().name());
            }
        } else if (breakpoint.getHitCountFilter() <= 0) {
            // No JDI hit count involvement
            return 1;
        } else {
            // JDI hit count filter applied. We do not know how many times is the breakpoint hit actually.
            return -1;
        }
    }
    
    public void resetHitCounts() {
        if (customHitCountFilter > 0) {
            customHitCount = 0;
        } else if (breakpoint.getHitCountFilter() <= 0) {
            // No JDI hit count involvement
            customHitCount = 0;
        } else {
            // JDI hit count filter applied. We do not know how many times is the breakpoint hit actually.
            // Ignore
        }
    }
    
    private void fireHitCountChanged() {
        for (HitCountListener hcl : hcListeners) {
            hcl.hitCountChanged(breakpoint);
        }
    }
    
    public void addHitCountListener(HitCountListener hcl) {
        hcListeners.add(hcl);
    }
    
    public void removeHitCountListener(HitCountListener hcl) {
        hcListeners.remove(hcl);
    }
    
    
    private static final class ValidityChangeEvent extends ChangeEvent {
        
        private String reason;
        
        public ValidityChangeEvent(Breakpoint.VALIDITY validity, String reason) {
            super(validity);
            this.reason = reason;
        }
        
        @Override
        public String toString() {
            return reason;
        }
    }

    private static final class EngineChangeEvent extends ChangeEvent {

        private final DebuggerEngine newEngine;

        public EngineChangeEvent(DebuggerEngine e, DebuggerEngine newEngine) {
            super(e);
            this.newEngine = newEngine;
        }

        @Override
        public Object getSource() {
            return newEngine;
        }

    }
    
    public static interface HitCountListener {
        
        void hitCountChanged(JPDABreakpoint bp);
    }
}
