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

package org.netbeans.modules.debugger.jpda;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.InternalException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.TypeComponent;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.io.InvalidObjectException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Refreshable;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.AbstractDICookie;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.DeadlockDetector;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAStep;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.api.debugger.jpda.ListeningDICookie;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.io.InputOutput;
import org.netbeans.modules.debugger.jpda.actions.ActionErrorMessageCallback;
import org.netbeans.modules.debugger.jpda.actions.ActionMessageCallback;
import org.netbeans.modules.debugger.jpda.actions.ActionStatusDisplayCallback;
import org.netbeans.modules.debugger.jpda.actions.CompoundSmartSteppingListener;
import org.netbeans.modules.debugger.jpda.breakpoints.BreakpointsEngineListener;
import org.netbeans.modules.debugger.jpda.expr.EvaluatorExpression;
import org.netbeans.modules.debugger.jpda.expr.InvocationExceptionTranslated;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.expr.formatters.Formatters;
import org.netbeans.modules.debugger.jpda.expr.formatters.FormattersLoopControl;
import org.netbeans.modules.debugger.jpda.expr.formatters.VariablesFormatter;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IllegalArgumentExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IntegerValueWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocalVariableWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MirrorWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeComponentWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ValueWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.models.AbstractObjectVariable;
import org.netbeans.modules.debugger.jpda.models.AbstractVariable;
import org.netbeans.modules.debugger.jpda.models.CallStackFrameImpl;
import org.netbeans.modules.debugger.jpda.models.ClassVariableImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAClassTypeImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.models.ObjectTranslation;
import org.netbeans.modules.debugger.jpda.models.ThreadsCache;
import org.netbeans.modules.debugger.jpda.models.VariableMirrorTranslator;
import org.netbeans.modules.debugger.jpda.util.JPDAUtils;
import org.netbeans.modules.debugger.jpda.util.Operator;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.debugger.DelegatingSessionProvider;
import org.netbeans.spi.debugger.jpda.Evaluator;
import org.netbeans.spi.debugger.jpda.SmartSteppingCallback.StopOrStep;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
* Representation of a debugging session.
*
* @author   Jan Jancura
*/
@JPDADebugger.Registration(path="netbeans-JPDASession")
public class JPDADebuggerImpl extends JPDADebugger {

    private static final Logger logger = Logger.getLogger(JPDADebuggerImpl.class.getName());

    private static final boolean SINGLE_THREAD_STEPPING = !Boolean.getBoolean("netbeans.debugger.multiThreadStepping");


    // variables ...............................................................

    //private DebuggerEngine              debuggerEngine;
    private VirtualMachine              virtualMachine = null;
    private final Object                virtualMachineLock = new Object();
    private Throwable                   throwable;
    private int                         state = 0;
    private final Object                stateLock = new Object();
    private Operator                    operator;
    private PropertyChangeSupport       pcs;
    public  PropertyChangeSupport       varChangeSupport = new PropertyChangeSupport(this);
    private JPDAThreadImpl              currentThread;
    private CallStackFrame              currentCallStackFrame;
    private final Object                currentThreadAndFrameLock = new Object();
    private volatile JPDAThreadImpl     currentSuspendedNoFireThread;   // Used during initial event processing only
    private int                         suspend = (SINGLE_THREAD_STEPPING) ? SUSPEND_EVENT_THREAD : SUSPEND_ALL;
    public final ReentrantReadWriteLock accessLock = new DebuggerReentrantReadWriteLock(true);
    private final Object                LOCK2 = new Object ();
    private boolean                     starting;
    private AbstractDICookie            attachingCookie;
    private JavaEngineProvider          javaEngineProvider;
    private Set<String>                 languages;
    private ContextProvider             lookupProvider;
    private ObjectTranslation           threadsTranslation;
    private ObjectTranslation           localsTranslation;
    private ExpressionPool              expressionPool;
    private ThreadsCache                threadsCache;
    private DeadlockDetector            deadlockDetector;
    private ThreadsCollectorImpl        threadsCollector;
    private final Object                threadsCollectorLock = new Object();
    private final Map<Long, String>     markedObjects = new LinkedHashMap<Long, String>();
    private final Map<String, ObjectVariable> markedObjectLabels = new LinkedHashMap<String, ObjectVariable>();
    private final Map<ClassType, List>  allInterfacesMap = new WeakHashMap<ClassType, List>();
    private static final int            RP_THROUGHPUT = 10;
    private final RequestProcessor      rpCreateThreads = new RequestProcessor(JPDADebuggerImpl.class.getName()+"_newJPDAThreads", RP_THROUGHPUT); // NOI18N

    private StackFrame      altCSF = null;  //PATCH 48174

    private boolean                     doContinue = true; // Whether resume() will actually resume
    private Boolean                     singleThreadStepResumeDecision = null;
    private Boolean                     stepInterruptByBptResumeDecision = null;
    private boolean                     breakpointsActive = true;
    
    private final DebuggerConsoleIO     io;
    
    private PeriodicThreadsDump         ptd;
    private boolean                     vmSuspended = false; // true after VM.suspend() was called.

    // init ....................................................................

    public JPDADebuggerImpl (ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        
        Properties p = Properties.getDefault().getProperties("debugger.options.JPDA");
        int stepResume = p.getInt("StepResume", (SINGLE_THREAD_STEPPING) ? 1 : 0);
        suspend = (stepResume == 1) ? SUSPEND_EVENT_THREAD : SUSPEND_ALL;

        pcs = new PropertyChangeSupport (this);
        List l = lookupProvider.lookup (null, DebuggerEngineProvider.class);
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            if (l.get (i) instanceof JavaEngineProvider) {
                javaEngineProvider = (JavaEngineProvider) l.get (i);
            }
        }
        if (javaEngineProvider == null) {
            throw new IllegalArgumentException
                ("JavaEngineProvider have to be used to start JPDADebugger!");
        }
        languages = new HashSet<String>();
        languages.add ("Java");
        threadsTranslation = ObjectTranslation.createThreadTranslation(this);
        localsTranslation = ObjectTranslation.createLocalsTranslation(this);
        this.expressionPool = new ExpressionPool();
        io = new DebuggerConsoleIO(this, lookupProvider);
    }


    // JPDADebugger methods ....................................................

    /**
     * Returns current state of JPDA debugger.
     *
     * @return current state of JPDA debugger
     * @see #STATE_STARTING
     * @see #STATE_RUNNING
     * @see #STATE_STOPPED
     * @see #STATE_DISCONNECTED
     */
    @Override
    public int getState () {
        synchronized (stateLock) {
            return state;
        }
    }

    /**
     * Gets value of suspend property.
     *
     * @return value of suspend property
     */
    @Override
    public int getSuspend () {
        synchronized (stateLock) {
            return suspend;
        }
    }

    /**
     * Sets value of suspend property.
     *
     * @param s a new value of suspend property
     */
    @Override
    public void setSuspend (int s) {
        int old;
        synchronized (stateLock) {
            if (s == suspend) {
                return;
            }
            old = suspend;
            suspend = s;
        }
        firePropertyChange (PROP_SUSPEND, Integer.valueOf(old), Integer.valueOf(s));
    }

    /**
     * Returns current thread or null.
     *
     * @return current thread or null
     */
    @Override
    public JPDAThread getCurrentThread () {
        synchronized (currentThreadAndFrameLock) {
            return currentThread;
        }
    }

    /**
     * Returns current stack frame or null.
     *
     * @return current stack frame or null
     */
    @Override
    public CallStackFrame getCurrentCallStackFrame () {
        synchronized (currentThreadAndFrameLock) {
            return currentCallStackFrame;
        }
    }

    /**
     * Evaluates given expression in the current context.
     *
     * @param expression a expression to be evaluated
     *
     * @return current value of given expression
     */
    @Override
    public Variable evaluate (String expression) throws InvalidExpressionException {
        return evaluate(expression, null, null);
    }

    /**
     * Evaluates given expression in the context of the variable.
     *
     * @param expression a expression to be evaluated
     *
     * @return current value of given expression
     */
    public Variable evaluate (String expression, ObjectVariable var)
    throws InvalidExpressionException {
        return evaluate(expression, null, var);
    }

    /**
     * Evaluates given expression in the current context.
     *
     * @param expression a expression to be evaluated
     *
     * @return current value of given expression
     */
    public Variable evaluate (String expression, CallStackFrame csf)
    throws InvalidExpressionException {
        return evaluate(expression, csf, null);
    }

    /**
     * Evaluates given expression in the current context.
     *
     * @param expression a expression to be evaluated
     *
     * @return current value of given expression
     */
    public Variable evaluate (String expression, CallStackFrame csf, ObjectVariable var)
    throws InvalidExpressionException {
        return evaluateGeneric(new EvaluatorExpression(expression), csf, var);
    }

    /**
     * Waits till the Virtual Machine is started and returns
     * {@link DebuggerStartException} if any.
     *
     * @throws DebuggerStartException if some problems occurs during debugger
     *         start
     *
     * @see AbstractDICookie#getVirtualMachine()
     */
    @Override
    public void waitRunning () throws DebuggerStartException {
        waitRunning(0);
    }

    /**
     * Waits till the Virtual Machine is started.
     *
     * @return <code>true</code> when debugger started or finished in the mean time,
     *         <code>false</code> when the debugger is still starting
     * @throws DebuggerStartException if some problems occurs during debugger
     *         start
     *
     * @see AbstractDICookie#getVirtualMachine()
     */
    private boolean waitRunning(long timeout) throws DebuggerStartException {
        synchronized (LOCK2) {
            int state = getState();
            if (state == STATE_DISCONNECTED) {
                if (throwable != null) {
                    throw new DebuggerStartException (throwable);
                } else {
                    return true;
                }
            }
            if (!starting && state > STATE_STARTING || throwable != null) {
                return true; // We're already running
            }
            try {
                logger.log(Level.FINE, "JPDADebuggerImpl.waitRunning(): starting = {0}, state = {1}, exception = {2}", new Object[]{starting, state, throwable});
                LOCK2.wait (timeout);
                if ((starting || state == STATE_STARTING) && throwable == null) {
                    return false; // We're still not running and the time expired
                }
            } catch (InterruptedException e) {
                 throw new DebuggerStartException (e);
            }

            if (throwable != null) {
                throw new DebuggerStartException (throwable);
            } else {
                return true;
            }
        }
    }
    
    public DebuggerConsoleIO getConsoleIO() {
        return io;
    }

    /**
     * Returns <code>true</code> if this debugger supports Pop action.
     *
     * @return <code>true</code> if this debugger supports Pop action
     */
    @Override
    public boolean canPopFrames () {
        VirtualMachine vm = getVirtualMachine ();
        if (vm == null) {
            return false;
        }
        return VirtualMachineWrapper.canPopFrames0(vm);
    }

    /**
     * Returns <code>true</code> if this debugger supports fix & continue
     * (HotSwap).
     *
     * @return <code>true</code> if this debugger supports fix & continue
     */
    @Override
    public boolean canFixClasses () {
        VirtualMachine vm = getVirtualMachine ();
        if (vm == null) {
            return false;
        }
        return VirtualMachineWrapper.canRedefineClasses0(vm);
    }

    /**
     * Implements fix & continue (HotSwap). Map should contain class names
     * as a keys, and byte[] arrays as a values.
     *
     * @param classes a map from class names to be fixed to byte[]
     */
    @Override
    public void fixClasses (Map<String, byte[]> classes) {
        PropertyChangeEvent evt = null;
        accessLock.writeLock().lock();
        try {
            // 1) redefine classes
            Map<ReferenceType, byte[]> map = new HashMap<ReferenceType, byte[]>();
            Iterator<Map.Entry<String, byte[]>> e = classes.entrySet ().iterator ();
            VirtualMachine vm = getVirtualMachine();
            if (vm == null) {
                return ; // The session has finished
            }
            try {
                while (e.hasNext ()) {
                    Map.Entry<String, byte[]> classEntry = e.next();
                    String className = classEntry.getKey();
                    byte[] bytes = classEntry.getValue();
                    List<ReferenceType> classRefs = VirtualMachineWrapper.classesByName (vm, className);
                    int j, jj = classRefs.size ();
                    for (j = 0; j < jj; j++) {
                        map.put (
                            classRefs.get (j),
                            bytes
                        );
                    }
                }
                VirtualMachineWrapper.redefineClasses (vm, map);
            } catch (InternalExceptionWrapper ex) {
            } catch (VMDisconnectedExceptionWrapper ex) {
            }

            // update breakpoints
            fixBreakpoints();

            firePropertyChange(JPDADebugger.PROP_CLASSES_FIXED, null, null);

            // 2) pop obsoleted frames
            JPDAThread t = getCurrentThread ();
            if (t != null && t.isSuspended()) {
                try {
                    if (t.getStackDepth () < 2) {
                        return;
                    }
                    CallStackFrame frame;
                    try {
                        CallStackFrame[] frames = t.getCallStack(0, 1);
                        if (frames.length == 0) {
                            return ;
                        }
                        frame = frames[0]; // Retrieve the new, possibly obsoleted frame and check it.
                    } catch (AbsentInformationException ex) {
                        return;
                    }

                    //PATCH #52209
                    if (frame.isObsolete () && ((CallStackFrameImpl) frame).canPop()) {
                        frame.popFrame ();
                        setState (STATE_RUNNING);
                        evt = updateCurrentCallStackFrameNoFire (t);
                        setState (STATE_STOPPED);
                    }
                } catch (InvalidStackFrameException ex) {
                }
            }

        } finally {
            accessLock.writeLock().unlock();
        }
        if (evt != null) {
            firePropertyChange(evt);
        }
    }

    public void fixBreakpoints() {
        // Just reset the time stamp so that new line numbers are taken.
        EditorContextBridge.getContext().disposeTimeStamp(this);
        EditorContextBridge.getContext().createTimeStamp(this);
        Session s = getSession();
        DebuggerEngine de = s.getEngineForLanguage ("Java");
        if (de == null) {
            de = s.getCurrentEngine();
        }
        if (de != null) {
            BreakpointsEngineListener bel = null;
            List lazyListeners = de.lookup(null, LazyActionsManagerListener.class);
            for (int li = 0; li < lazyListeners.size(); li++) {
                Object service = lazyListeners.get(li);
                if (service instanceof BreakpointsEngineListener) {
                    bel = (BreakpointsEngineListener) service;
                    break;
                }
            }
            if (bel != null) {
                bel.fixBreakpointImpls ();
            }
        }
        expressionPool.clear();
    }

    @Override
    public boolean getBreakpointsActive() {
        return breakpointsActive;
    }

    @Override
    public void setBreakpointsActive(boolean active) {
        synchronized (this) {
            if (breakpointsActive == active) {
                return ;
            }
            breakpointsActive = active;
        }
        firePropertyChange(PROP_BREAKPOINTS_ACTIVE, !active, active);
    }
    
    public Session getSession() {
        return lookupProvider.lookupFirst(null, Session.class);
    }

    public RequestProcessor getRequestProcessor() {
        return javaEngineProvider.getRequestProcessor();
    }

    private Boolean canBeModified;
    private final Object canBeModifiedLock = new Object();

    @Override
    public boolean canBeModified() {
        VirtualMachine vm = getVirtualMachine ();
        if (vm == null) {
            return false;
        }
        synchronized (canBeModifiedLock) {
            if (canBeModified == null) {
                canBeModified = vm.canBeModified();
            }
            return canBeModified.booleanValue();
        }
    }

    private SmartSteppingFilter smartSteppingFilter;

    /**
     * Returns instance of SmartSteppingFilter.
     *
     * @return instance of SmartSteppingFilter
     */
    @Override
    public SmartSteppingFilter getSmartSteppingFilter () {
        if (smartSteppingFilter == null) {
            smartSteppingFilter = lookupProvider.lookupFirst(null, SmartSteppingFilter.class);
        }
        return smartSteppingFilter;
    }

    CompoundSmartSteppingListener compoundSmartSteppingListener;

    private CompoundSmartSteppingListener getCompoundSmartSteppingListener () {
        if (compoundSmartSteppingListener == null) {
            compoundSmartSteppingListener = lookupProvider.lookupFirst(null, CompoundSmartSteppingListener.class);
        }
        return compoundSmartSteppingListener;
    }

    /**
     * Test whether we should stop here according to the smart-stepping rules.
     */
    StopOrStep stopHere(JPDAThread t, SmartSteppingFilter filter) {
        CallStackFrame topFrame = null;
        try {
            CallStackFrame[] topFrameArr = t.getCallStack(0, 1);
            if (topFrameArr.length > 0) {
                topFrame = topFrameArr[0];
            }
        } catch (AbsentInformationException aiex) {}
        if (topFrame != null) {
            return getCompoundSmartSteppingListener().stopAt(lookupProvider, topFrame, filter);
        } else {
            return getCompoundSmartSteppingListener().stopHere(lookupProvider, t, filter) ?
                    StopOrStep.stop() :
                    StopOrStep.skip();
        }
    }

    /**
     * Helper method that fires JPDABreakpointEvent on JPDABreakpoints.
     *
     * @param breakpoint a breakpoint to be changed
     * @param event a event to be fired
     */
    @Override
    public void fireBreakpointEvent (
        JPDABreakpoint breakpoint,
        JPDABreakpointEvent event
    ) {
        super.fireBreakpointEvent (breakpoint, event);
    }

    /**
    * Adds property change listener.
    *
    * @param l new listener.
    */
    @Override
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    /**
    * Removes property change listener.
    *
    * @param l removed listener.
    */
    @Override
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }

    /**
    * Adds property change listener.
    *
    * @param l new listener.
    */
    @Override
    public void addPropertyChangeListener (String propertyName, PropertyChangeListener l) {
        pcs.addPropertyChangeListener (propertyName, l);
    }

    /**
    * Removes property change listener.
    *
    * @param l removed listener.
    */
    @Override
    public void removePropertyChangeListener (String propertyName, PropertyChangeListener l) {
        pcs.removePropertyChangeListener (propertyName, l);
    }


    // internal interface ......................................................

    public void popFrames (ThreadReference thread, StackFrame frame) {
        PropertyChangeEvent evt = null;
        accessLock.readLock().lock();
        try {
            JPDAThreadImpl threadImpl = getThread(thread);
            setState (STATE_RUNNING);
            try {
                threadImpl.popFrames(frame);
                evt = updateCurrentCallStackFrameNoFire(threadImpl);
            } catch (IncompatibleThreadStateException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                setState (STATE_STOPPED);
            }
        } finally {
            accessLock.readLock().unlock();
        }
        if (evt != null) {
            firePropertyChange(evt);
        }
    }

    public void setException (Throwable t) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("JPDADebuggerImpl.setException("+t+")");
        }
        synchronized (LOCK2) {
            throwable = t;
            starting = false;
            LOCK2.notifyAll ();
        }
    }

    public void setCurrentThread (JPDAThread thread) {
        Object oldT;
        PropertyChangeEvent event;
        CallStackFrame topFrame = getTopFrame(thread);
        synchronized (currentThreadAndFrameLock) {
            oldT = currentThread;
            currentThread = (JPDAThreadImpl) thread;
            event = updateCurrentCallStackFrameNoFire(topFrame);
        }
        checkJSR45Languages(thread);
        if (thread != oldT) {
            firePropertyChange (PROP_CURRENT_THREAD, oldT, thread);
        }
        if (event != null) {
            firePropertyChange(event);
        }
        setState(thread.isSuspended() ? STATE_STOPPED : STATE_RUNNING);
    }

    /**
     * Set the current thread and call stack, but do not fire changes.
     * @return The PropertyChangeEvent associated with this change, it can have
     *         attached other PropertyChangeEvents as a propagation ID.
     */
    private PropertyChangeEvent setCurrentThreadNoFire(JPDAThread thread) {
        Object oldT;
        PropertyChangeEvent evt = null;
        PropertyChangeEvent evt2;
        CallStackFrame topFrame = getTopFrame(thread);
        synchronized (currentThreadAndFrameLock) {
            oldT = currentThread;
            currentThread = (JPDAThreadImpl) thread;
            evt2 = updateCurrentCallStackFrameNoFire(topFrame);
        }
        if (thread != oldT) {
            evt = new PropertyChangeEvent(this, PROP_CURRENT_THREAD, oldT, thread);
        }
        if (evt == null) {
            evt = evt2;
        } else if (evt2 != null) {
            evt.setPropagationId(evt2);
        }
        return evt;
    }

    public void setCurrentCallStackFrame (CallStackFrame callStackFrame) {
        CallStackFrame old = setCurrentCallStackFrameNoFire(callStackFrame);
        if (old == callStackFrame) {
            return ;
        }
        if (callStackFrame != null) {
            checkJSR45Languages(callStackFrame);
        }
        firePropertyChange (
            PROP_CURRENT_CALL_STACK_FRAME,
            old,
            callStackFrame
        );
    }

    private CallStackFrame setCurrentCallStackFrameNoFire (CallStackFrame callStackFrame) {
        CallStackFrame old;
        synchronized (currentThreadAndFrameLock) {
            if (callStackFrame == currentCallStackFrame) {
                return callStackFrame;
            }
            old = currentCallStackFrame;
            currentCallStackFrame = callStackFrame;
        }
        return old;
    }

    /**
     * Used by AbstractVariable and watches.
     */
    // * Might be changed to return a variable with disabled collection. When not used any more,
    // * it's collection must be enabled again.
    public Value evaluateIn (String expression) throws InvalidExpressionException {
        return evaluateIn(new EvaluatorExpression(expression), null);
    }

    /**
     * Used by BreakpointImpl.
     */
    // * Might be changed to return a variable with disabled collection. When not used any more,
    // * it's collection must be enabled again.
    public Value evaluateIn (EvaluatorExpression expression, CallStackFrame csf) throws InvalidExpressionException {
        return evaluateIn(expression, csf, null);
    }

    // * Might be changed to return a variable with disabled collection. When not used any more,
    // * it's collection must be enabled again.
    public Value evaluateIn (EvaluatorExpression expression, CallStackFrame csf, ObjectVariable var) throws InvalidExpressionException {
        Variable variable = evaluateGeneric(expression, csf, var);
        if (variable instanceof JDIVariable) {
            return ((JDIVariable) variable).getJDIValue();
        } else {
            return null;
        }
    }

    //PATCH 48174
    public void setAltCSF(StackFrame sf) {
        altCSF = sf;
    }

    public StackFrame getAltCSF() {
        return altCSF;
    }

    /**
     * Used by WatchesModel & BreakpointImpl.
     */
    // * Might be changed to return a variable with disabled collection. When not used any more,
    // * it's collection must be enabled again.
    public Value evaluateIn (EvaluatorExpression expression) throws InvalidExpressionException {
        return evaluateIn(expression, null, null);
    }


    InvalidExpressionException methodCallsUnsupportedExc;

    /**
     * Evaluates given expression in the current context.
     *
     * @param expression a expression to be evaluated
     *
     * @return current value of given expression
     */
    private Variable evaluateGeneric(EvaluatorExpression expression, CallStackFrame c, ObjectVariable var)
            throws InvalidExpressionException {

        Session s = getSession();
        Evaluator e = s.lookupFirst(s.getCurrentLanguage(), Evaluator.class);
        logger.log(Level.FINE, "Have evaluator {0} for language {1}", new Object[]{e, s.getCurrentLanguage()});   // NOI18N

        if (e == null) {
            e = s.lookupFirst("Java", Evaluator.class);
            if (e == null) {
                Exceptions.printStackTrace(new IllegalStateException("No evaluator registered for Java language!"));
                e = new JavaEvaluator(s);
            }
        }

        CallStackFrameImpl csf;
        if (c instanceof CallStackFrameImpl) {
            csf = (CallStackFrameImpl) c;
        } else {
            csf = (CallStackFrameImpl) getCurrentCallStackFrame ();
        }
        if (csf == null) {
            StackFrame sf = getAltCSF();
            if (sf != null) {
                try {
                    ThreadReference t = StackFrameWrapper.thread(sf);
                    JPDAThread tr = getThread(t);
                    CallStackFrame[] stackFrames = tr.getCallStack(0, 1);
                    if (stackFrames.length > 0) {
                        c = stackFrames[0];
                        csf = (CallStackFrameImpl) c;
                    }
                } catch (InternalExceptionWrapper ex) {
                } catch (InvalidStackFrameExceptionWrapper ex) {
                } catch (VMDisconnectedExceptionWrapper ex) {
                } catch (AbsentInformationException aiex) {
                }
            }
        }
        JPDAThread frameThread;
        if (csf != null) {
            frameThread = csf.getThread();
        } else {
            frameThread = getCurrentThread();
        }
        if (frameThread == null) {
            throw new InvalidExpressionException
                (NbBundle.getMessage(JPDADebuggerImpl.class, "MSG_NoCurrentContext"));
        }
        JPDAThreadImpl frameThreadImpl = (JPDAThreadImpl) frameThread;
        //Object pendingAction = frameThreadImpl.getPendingAction();
        //if (pendingAction != null) { For the case that evaluation should be blocked by pending action
        //    //return frameThreadImpl.getPendingVariable(pendingActions);
        //    throw new InvalidExpressionException(frameThreadImpl.getPendingString(pendingAction));
        //}
        Lock lock = frameThreadImpl.accessLock.writeLock();
        lock.lock();
        Variable vr;
        try {
            if (!frameThread.isSuspended() && !((JPDAThreadImpl) frameThread).isSuspendedNoFire()) {
                // Thread not suspended => Can not start evaluation
                throw new InvalidExpressionException
                    (NbBundle.getMessage(JPDADebuggerImpl.class, "MSG_NoCurrentContextStackFrame"));
            }
            ObjectReference v = null;
            if (var instanceof JDIVariable) {
                v = (ObjectReference) ((JDIVariable) var).getJDIValue();
                if (v == null) {
                    throw new InvalidExpressionException
                        (NbBundle.getMessage(JPDADebuggerImpl.class,
                                             "MSG_CanNotEvaluateInContextOfNull",
                                             expression.getExpression()));
                }
            }
            Evaluator.Result result;
            final JPDAThreadImpl[] resumedThread = new JPDAThreadImpl[] { null };
            try {
                StackFrame sf = csf != null ? csf.getStackFrame() : null;
                int stackDepth = csf != null ? csf.getFrameDepth() : -1;
                final ThreadReference tr = frameThreadImpl.getThreadReference();
                Runnable methodToBeInvokedNotifier = new Runnable() {
                        @Override
                        public void run() {
                            if (resumedThread[0] == null) {
                                JPDAThreadImpl theResumedThread = getThread(tr);
                                try {
                                    theResumedThread.notifyMethodInvoking();
                                } catch (PropertyVetoException pvex) {
                                    throw new RuntimeException(
                                        new InvalidExpressionException (pvex.getMessage()));
                                } catch (ThreadDeath td) {
                                    throw td;
                                } catch (Throwable t) {
                                    Exceptions.printStackTrace(t);
                                }
                                resumedThread[0] = theResumedThread;
                            }
                        }
                    };
                List<Object> lookupVars = new ArrayList<>();
                lookupVars.add(frameThread);
                lookupVars.add(stackDepth);
                if (csf != null) {
                    lookupVars.add(csf);
                    lookupVars.add(sf);
                }
                if (var != null) {
                    lookupVars.add(var);
                }
                if (v != null) {
                    lookupVars.add(v);
                }
                lookupVars.add(methodToBeInvokedNotifier);
                Lookup context = Lookups.fixed(lookupVars.toArray());
                result = expression.evaluate(e, new Evaluator.Context(context));
            } catch (InternalExceptionWrapper ex) {
                return null;
            } catch (InvalidStackFrameExceptionWrapper ex) {
                throw new InvalidExpressionException
                    (NbBundle.getMessage(JPDADebuggerImpl.class, "MSG_NoCurrentContextStackFrame"));
            } catch (VMDisconnectedExceptionWrapper ex) {
                // Causes kill action when something is being evaluated.
                return null;
            } catch (InternalException ex) {
                InvalidExpressionException isex = new InvalidExpressionException(ex.getLocalizedMessage());
                isex.initCause(ex);
                Exceptions.attachMessage(isex, "Expression = '"+expression+"'");
                throw isex;
            } catch (RuntimeException rex) {
                Throwable cause = rex.getCause();
                if (cause instanceof InvalidExpressionException) {
                    Exceptions.attachMessage(cause, "Expression = '"+expression+"'");
                    throw (InvalidExpressionException) cause;
                } else {
                    throw rex;
                }
            } finally {
                if (resumedThread[0] != null) {
                    resumedThread[0].notifyMethodInvokeDone();
                }
            }
            vr = result.getVariable();
            if (vr == null) {
                vr = createVariable(result.getValue());
            }
        } finally {
            lock.unlock();
        }
        return vr;
    }

    /**
     * Used by AbstractVariable.
     */
    public Value invokeMethod (
        ObjectReference reference,
        Method method,
        Value[] arguments
    ) throws InvalidExpressionException {
        return invokeMethod(null, reference, null, method, arguments, 0, null);
    }

    public Value invokeMethod (
        ObjectReference reference,
        Method method,
        Value[] arguments,
        int maxLength
    ) throws InvalidExpressionException {
        return invokeMethod(null, reference, null, method, arguments, maxLength, null);
    }

    /**
     * Used by AbstractVariable.
     */
    public Value invokeMethod (
        JPDAThreadImpl thread,
        ObjectReference reference,
        Method method,
        Value[] arguments
    ) throws InvalidExpressionException {
        return invokeMethod(thread, reference, method, arguments, null);
    }

    public Value invokeMethod (
        JPDAThreadImpl thread,
        ObjectReference reference,
        Method method,
        Value[] arguments,
        InvocationExceptionTranslated existingInvocationException
    ) throws InvalidExpressionException {
        return invokeMethod(thread, reference, null, method, arguments, 0, existingInvocationException);
    }

    /**
     * Used by JPDAClassTypeImpl.
     */
    public Value invokeMethod (
        JPDAThreadImpl thread,
        ClassType classType,
        Method method,
        Value[] arguments
    ) throws InvalidExpressionException {
        return invokeMethod(thread, null, classType, method, arguments, 0, null);
    }

    private Value invokeMethod (
        JPDAThreadImpl thread,
        ObjectReference reference,
        ClassType classType,
        Method method,
        Value[] arguments,
        int maxLength,
        InvocationExceptionTranslated existingInvocationException
    ) throws InvalidExpressionException {
        synchronized (currentThreadAndFrameLock) {
            if (thread == null && currentThread == null) {
                thread = currentSuspendedNoFireThread;
                if (thread == null) {
                    throw new InvalidExpressionException
                            (NbBundle.getMessage(JPDADebuggerImpl.class, "MSG_NoCurrentContext"));
                }
            }
            if (thread == null) {
                thread = currentThread;
            }
        }
        thread.accessLock.writeLock().lock();
        try {
            if (methodCallsUnsupportedExc != null) {
                throw methodCallsUnsupportedExc;
            }
            boolean threadSuspended = false;
            JPDAThread frameThread = null;
            CallStackFrameImpl csf = null;
            try {
                // Remember the current stack frame, it might be necessary to re-set.
                csf = (CallStackFrameImpl) getCurrentCallStackFrame ();
                if (csf != null) {
                    try {
                        frameThread = csf.getThread();
                    } catch (InvalidStackFrameException isfex) {}
                }
                ThreadReference tr = thread.getThreadReference();
                try {
                    thread.notifyMethodInvoking();
                    threadSuspended = true;
                } catch (PropertyVetoException pvex) {
                    throw new InvalidExpressionException (pvex.getMessage());
                } catch (RuntimeException rex) {
                    // Give up
                    thread.notifyMethodInvokeDone();
                    throw rex;
                } catch (ThreadDeath td) {
                    throw td;
                } catch (Error e) {
                    // Give up
                    thread.notifyMethodInvokeDone();
                    throw e;
                }
                try {
                    Value v;
                    if (reference != null) {
                        v = org.netbeans.modules.debugger.jpda.expr.TreeEvaluator.
                            invokeVirtual (
                                reference,
                                method,
                                tr,
                                Arrays.asList (arguments),
                                this,
                                existingInvocationException
                            );
                    } else {
                        v = org.netbeans.modules.debugger.jpda.expr.TreeEvaluator.
                            invokeVirtual (
                                classType,
                                method,
                                tr,
                                Arrays.asList (arguments),
                                this,
                                existingInvocationException
                            );
                    }
                    if (maxLength > 0 && maxLength < Integer.MAX_VALUE && (v instanceof StringReference)) {
                        v = cutLength((StringReference) v, maxLength, tr);
                    }
                    return v;
                } catch (InternalException e) {
                    throw new InvalidExpressionException(e);
                }
            } catch (InvalidExpressionException ieex) {
                if (ieex.getTargetException() instanceof UnsupportedOperationException) {
                    methodCallsUnsupportedExc = ieex;
                }
                throw ieex;
            } finally {
                if (threadSuspended) {
                    thread.notifyMethodInvokeDone();
                }
                if (frameThread != null && csf != null) {
                    try {
                        csf.getThread();
                    } catch (InvalidStackFrameException isfex) {
                        // The current frame is invalidated, set the new current...
                        int depth = csf.getFrameDepth();
                        try {
                            CallStackFrame csf2 = frameThread.getCallStack(depth, depth + 1)[0];
                            setCurrentCallStackFrameNoFire(csf2);
                        } catch (AbsentInformationException aiex) {
                            setCurrentCallStackFrame(null);
                        }
                    }
                }
            }
        } finally {
            thread.accessLock.writeLock().unlock();
        }
    }

    private Method stringLengthMethod;
    private Method stringSubstringMethod;
    private final Object stringMethodsLock = new Object();

    private Value cutLength(StringReference sr, int maxLength, ThreadReference tr) throws InvalidExpressionException {
        try {
            Method stringLengthMethod;
            synchronized (stringMethodsLock) {
                stringLengthMethod = this.stringLengthMethod;
                if (stringLengthMethod == null) {
                    stringLengthMethod = ClassTypeWrapper.concreteMethodByName(
                            (ClassType) ValueWrapper.type (sr), "length", "()I"); // NOI18N
                    this.stringLengthMethod = stringLengthMethod;
                }
            }
            List<Value> emptyArgs = Collections.emptyList();
            IntegerValue lengthValue = (IntegerValue) org.netbeans.modules.debugger.jpda.expr.TreeEvaluator.
                invokeVirtual (
                    sr,
                    stringLengthMethod,
                    tr,
                    emptyArgs,
                    this
                );
                if (IntegerValueWrapper.value(lengthValue) > maxLength) {
                    Method subStringMethod;
                    synchronized (stringMethodsLock) {
                        subStringMethod = this.stringSubstringMethod;
                        if (subStringMethod == null) {
                            subStringMethod = ClassTypeWrapper.concreteMethodByName(
                                (ClassType) ValueWrapper.type (sr), "substring", "(II)Ljava/lang/String;");  // NOI18N
                            this.stringSubstringMethod = subStringMethod;
                        }
                    }
                    if (subStringMethod != null) {
                        sr = (StringReference) org.netbeans.modules.debugger.jpda.expr.TreeEvaluator.
                            invokeVirtual (
                                sr,
                                subStringMethod,
                                tr,
                                Arrays.asList(new Value [] { VirtualMachineWrapper.mirrorOf(MirrorWrapper.virtualMachine(sr), 0),
                                               VirtualMachineWrapper.mirrorOf(MirrorWrapper.virtualMachine(sr), maxLength) }),
                                this
                            );
                    }
                }
        } catch (InternalExceptionWrapper ex) {
        } catch (ObjectCollectedExceptionWrapper ex) {
        } catch (VMDisconnectedExceptionWrapper ex) {
        } catch (ClassNotPreparedExceptionWrapper ex) {
        }
        return sr;
    }

    public static String getGenericSignature (TypeComponent component) {
        try {
            return TypeComponentWrapper.genericSignature(component);
        } catch (InternalExceptionWrapper ex) {
            Exceptions.printStackTrace(ex);
        } catch (VMDisconnectedExceptionWrapper ex) {
            // Disconnected
        }
        return null;
    }

    public static String getGenericSignature (LocalVariable component) {
        try {
            return LocalVariableWrapper.genericSignature(component);
        } catch (InternalExceptionWrapper ex) {
            Exceptions.printStackTrace(ex);
        } catch (VMDisconnectedExceptionWrapper ex) {
            // Disconnected
        }
        return null;
    }

    public VirtualMachine getVirtualMachine () {
        synchronized (virtualMachineLock) {
            return virtualMachine;
        }
    }

    public Operator getOperator () {
        synchronized (virtualMachineLock) {
            return operator;
        }
    }

    public void setStarting () {
        setState (STATE_STARTING);
    }

    public synchronized void setAttaching(AbstractDICookie cookie) {
        this.attachingCookie = cookie;
    }

    public void setRunning (VirtualMachine vm, Operator o) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Start - JPDADebuggerImpl.setRunning ()");
            JPDAUtils.printFeatures (logger, vm);
        }
        synchronized (LOCK2) {
            starting = true;
        }
        synchronized (virtualMachineLock) {
            virtualMachine = vm;
            operator = o;
        }
        if (logger.isLoggable(Level.FINEST)) {
            ptd = new PeriodicThreadsDump(vm);
        }
        synchronized (canBeModifiedLock) {
            canBeModified = null; // Reset the can be modified flag
        }

        EditorContextBridge.getContext().createTimeStamp(this);


//        Iterator i = getVirtualMachine ().allThreads ().iterator ();
//        while (i.hasNext ()) {
//            ThreadReference tr = (ThreadReference) i.next ();
//            if (tr.isSuspended ()) {
//                if (startVerbose)
//                    System.out.println("\nS JPDADebuggerImpl.setRunning () - " +
//                        "thread supended"
//                    );
//                setState (STATE_RUNNING);
//                synchronized (LOCK) {
//                    virtualMachine.resume ();
//                }
//                if (startVerbose)
//                    System.out.println("\nS JPDADebuggerImpl.setRunning () - " +
//                        "thread supended - VM resumed - end"
//                    );
//                synchronized (LOCK2) {
//                    LOCK2.notify ();
//                }
//                return;
//            }
//        }

        ThreadsCache tc;
        synchronized (threadsCollectorLock) {
            tc = threadsCache;
        }
        if (tc != null) {
            tc.setVirtualMachine(vm);
        }

        setState (STATE_RUNNING);
        synchronized (virtualMachineLock) {
            vm = virtualMachine; // re-take the VM, it can be nulled by finish()
        }
        if (vm != null) {
            notifyToBeResumedAll();
            accessLock.writeLock().lock();
            try {
                VirtualMachineWrapper.resume(vm);
            } catch (VMDisconnectedExceptionWrapper e) {
            } catch (InternalExceptionWrapper e) {
            } finally {
                accessLock.writeLock().unlock();
            }
        }

        logger.fine("   JPDADebuggerImpl.setRunning () finished, VM resumed.");
        synchronized (LOCK2) {
            starting = false;
            LOCK2.notifyAll ();
        }
    }

    /**
    * Performs stop action.
    */
    public void setStoppedState (ThreadReference thread, boolean stoppedAll) {
        setStoppedState(thread, true, false);
    }
    
    public void setStoppedState (ThreadReference thread, boolean stoppedAll, boolean forceThreadSwitch) {
        PropertyChangeEvent evt = null;
        accessLock.readLock().lock();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setStoppedState("+thread+", "+stoppedAll+", "+forceThreadSwitch+")");
        }
        try {
            // this method can be called in stopped state to switch
            // the current thread only
            JPDAThread t = getThread (thread);
            if (!forceThreadSwitch) {
                JPDAThread c = getCurrentThread();
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Have c = "+((c == null) ? null : ((JPDAThreadImpl) c).getThreadStateLog()));
                    logger.finer("Have t = "+((t == null) ? null : ((JPDAThreadImpl) t).getThreadStateLog()));
                }
                if (c != null && c != t
                    && (!stoppedAll || ((JPDAThreadImpl) c).isSuspendedOnAnEvent())
                    && c.isSuspended()) {
                    // We already have a suspended current thread, do not switch in that case.
                    // But update it's frames:
                    evt = updateCurrentCallStackFrameNoFire(c);
                    return ;
                }
            }
            logger.fine("  changing the current thread.");
            checkJSR45Languages (t);
            evt = setCurrentThreadNoFire(t);
            PropertyChangeEvent evt2 = setStateNoFire(STATE_STOPPED);

            if (evt == null) {
                evt = evt2;
            } else if (evt2 != null) {
                PropertyChangeEvent evt3 = evt;
                while(evt3.getPropagationId() != null) {
                    evt3 = (PropertyChangeEvent) evt3.getPropagationId();
                }
                evt3.setPropagationId(evt2);
            }
        } finally {
            accessLock.readLock().unlock();
            if (evt != null) {
                do {
                    firePropertyChange(evt);
                    evt = (PropertyChangeEvent) evt.getPropagationId();
                } while (evt != null);
            }
        }
    }

    /**
     * Can be called if the current thread is resumed after stop.
     */
    public void setRunningState() {
        setState(STATE_RUNNING);
    }

    /**
    * Performs stop action and disable a next call to resume()
    */
    public void setStoppedStateNoContinue (ThreadReference thread) {
        PropertyChangeEvent evt;
        accessLock.readLock().lock();
        try {
            // this method can be called in stopped state to switch
            // the current thread only
            evt = setStateNoFire(STATE_RUNNING);
            JPDAThread t = getThread (thread);
            checkJSR45Languages (t);
            PropertyChangeEvent evt2 = setCurrentThreadNoFire(t);

            if (evt == null) {
                evt = evt2;
            } else if (evt2 != null) {
                evt.setPropagationId(evt2);
            }

            evt2 = setStateNoFire(STATE_STOPPED);

            if (evt == null) {
                evt = evt2;
            } else if (evt2 != null) {
                PropertyChangeEvent evt3 = evt;
                while(evt3.getPropagationId() != null) {
                    evt3 = (PropertyChangeEvent) evt3.getPropagationId();
                }
                evt3.setPropagationId(evt2);
            }

            doContinue = false;
        } finally {
            accessLock.readLock().unlock();
        }
        if (evt != null) {
            do {
                firePropertyChange(evt);
                evt = (PropertyChangeEvent) evt.getPropagationId();
            } while (evt != null);
        }
    }


    private boolean finishing;

    /**
     * Used by KillActionProvider.
     */
    public void finish () {
        try {
            synchronized (this) {
                if (finishing) {
                    // Can easily be called twice - from the operator termination
                    return ;
                }
                finishing = true;
            }
            logger.fine("StartActionProvider.finish ()");
            if (getState () == STATE_DISCONNECTED) {
                return;
            }
            AbstractDICookie di = lookupProvider.lookupFirst(null, AbstractDICookie.class);
            Operator o = getOperator();
            if (o != null) {
                o.stop();
            }
            stopAttachListening();
            try {
                boolean startedUp;
                do {
                    startedUp = waitRunning(500); // First wait till the debugger comes up
                    if (!startedUp) {
                        stopAttachListening();
                        continue;
                    }
                } while (false);
            } catch (DebuggerStartException dsex) {
                // We do not want to start it anyway when we're finishing - do not bother
            }
            VirtualMachine vm;
            synchronized (virtualMachineLock) {
                vm = virtualMachine;
                virtualMachine = null;
            }
            if (ptd != null) {
                ptd.finish();
            }
            setState (STATE_DISCONNECTED);
            if (jsr45EngineProviders != null) {
                for (Iterator<JSR45DebuggerEngineProvider> i = jsr45EngineProviders.iterator(); i.hasNext();) {
                    JSR45DebuggerEngineProvider provider = i.next();
                    DebuggerEngine.Destructor d = provider.getDesctuctor();
                    if (d != null) {
                        d.killEngine();
                    }
                }
                jsr45EngineProviders = null;
            }
            DebuggerEngine.Destructor d = javaEngineProvider.getDestructor();
            if (d != null) {
                d.killEngine ();
            }
            if (vm != null) {
                try {
                    if (di instanceof AttachingDICookie) {
                        JPDAThreadImpl t;
                        synchronized (currentThreadAndFrameLock) {
                            t = currentThread;
                        }
                        if (t != null && t.isMethodInvoking()) {
                            try {
                                t.waitUntilMethodInvokeDone(5000); // Wait 5 seconds at most
                            } catch (InterruptedException ex) {}
                        }
                        logger.fine(" StartActionProvider.finish() VM dispose");
                        VirtualMachineWrapper.dispose (vm);
                    } else {
                        logger.fine(" StartActionProvider.finish() VM exit");
                        VirtualMachineWrapper.exit (vm, 0);
                    }
                } catch (InternalExceptionWrapper e) {
                    logger.log(Level.FINE, " StartActionProvider.finish() VM exception {0}", e);
                } catch (VMDisconnectedExceptionWrapper e) {
                    logger.log(Level.FINE, " StartActionProvider.finish() VM exception {0}", e);
                    // debugee VM is already disconnected (it finished normally)
                }
            }
            logger.fine (" StartActionProvider.finish() end.");

            //Notify LOCK2 so that no one is waiting forever
            synchronized (LOCK2) {
                starting = false;
                LOCK2.notifyAll ();
            }
            EditorContextBridge.getContext().disposeTimeStamp(this);
        } finally {
            finishing = false; // for safety reasons
        }
    }

    private synchronized void stopAttachListening() {
        if (attachingCookie != null) {
            if (attachingCookie instanceof ListeningDICookie) {
                ListeningDICookie listeningCookie = (ListeningDICookie) attachingCookie;
                try {
                    listeningCookie.getListeningConnector().stopListening(listeningCookie.getArgs());
                } catch (java.io.IOException ioex) {
                } catch (com.sun.jdi.connect.IllegalConnectorArgumentsException icaex) {
                } catch (IllegalArgumentException iaex) {
                }
            }
        }
    }
    
    public boolean isVMSuspended() {
        return vmSuspended;
    }

    /**
     * Suspends the target virtual machine (if any).
     * Used by PauseActionProvider.
     *
     * @see  com.sun.jdi.ThreadReference#suspend
     */
    public void suspend () {
        VirtualMachine vm;
        synchronized (virtualMachineLock) {
            vm = virtualMachine;
        }
        accessLock.writeLock().lock();
        try {
            if (vm != null) {
                logger.fine("VM suspend");
                try {
                    VirtualMachineWrapper.suspend (vm);
                    vmSuspended = true;
                    // Check the suspended count
                    List<ThreadReference> threads = VirtualMachineWrapper.allThreads(vm);
                    for (ThreadReference t : threads) {
                        try {
                            while (ThreadReferenceWrapper.suspendCount(t) > 1) {
                                ThreadReferenceWrapper.resume(t);
                            }
                            if (ThreadReferenceWrapper.name(t).contains(ThreadsCache.THREAD_NAME_FILTER_PATTERN)) {
                                // Do not suspend filtered threads, they might not be resumed.
                                ThreadReferenceWrapper.resume(t);
                            }
                        } catch (IllegalThreadStateExceptionWrapper e) {
                        } catch (ObjectCollectedExceptionWrapper e) {
                        } catch (InternalExceptionWrapper e) {
                        }
                    }
                } catch (VMDisconnectedExceptionWrapper e) {
                    return ;
                } catch (InternalExceptionWrapper e) {
                    return ;
                }
            }
            setState (STATE_STOPPED);
        } finally {
            accessLock.writeLock().unlock();
        }
        notifySuspendAll(true, true);
    }

    public List<PropertyChangeEvent> notifySuspendAll(boolean doFire, boolean explicitelyPaused) {
        return notifySuspendAll(doFire, explicitelyPaused, null);
    }

    public List<PropertyChangeEvent> notifySuspendAll(boolean doFire, boolean explicitelyPaused,
                                                      Set<ThreadReference> ignoredThreads) {
        Collection threads = threadsTranslation.getTranslated();
        List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>(threads.size());
        for (Iterator it = threads.iterator(); it.hasNext(); ) {
            Object threadOrGroup = it.next();
            if (threadOrGroup instanceof JPDAThreadImpl &&
                    (ignoredThreads == null || !ignoredThreads.contains(((JPDAThreadImpl) threadOrGroup).getThreadReference()))) {
                int status = ((JPDAThreadImpl) threadOrGroup).getState();
                boolean invalid = (status == JPDAThread.STATE_NOT_STARTED ||
                                   status == JPDAThread.STATE_UNKNOWN ||
                                   status == JPDAThread.STATE_ZOMBIE);
                if (!invalid) {
                    try {
                        PropertyChangeEvent event = ((JPDAThreadImpl) threadOrGroup).notifySuspended(doFire, explicitelyPaused);
                        if (event != null) {
                            events.add(event);
                        }
                    } catch (ObjectCollectedException ocex) {
                        invalid = true;
                    }
                } else if (status == JPDAThread.STATE_UNKNOWN || status == JPDAThread.STATE_ZOMBIE) {
                    threadsTranslation.remove(((JPDAThreadImpl) threadOrGroup).getThreadReference());
                }
            }
        }
        return events;
    }

    public void notifySuspendAllNoFire() {
        notifySuspendAllNoFire(null, null);
    }

    public void notifySuspendAllNoFire(Set<ThreadReference> ignoredThreads, ThreadReference eventThread) {
        Collection threads = threadsTranslation.getTranslated();
        for (Iterator it = threads.iterator(); it.hasNext(); ) {
            Object threadOrGroup = it.next();
            if (threadOrGroup instanceof JPDAThreadImpl) {
                JPDAThreadImpl thread = (JPDAThreadImpl) threadOrGroup;
                ThreadReference tr = thread.getThreadReference();
                if (ignoredThreads == null || !ignoredThreads.contains(tr)) {
                    int status = thread.getState();
                    boolean invalid = (status == JPDAThread.STATE_NOT_STARTED ||
                                       status == JPDAThread.STATE_UNKNOWN ||
                                       status == JPDAThread.STATE_ZOMBIE);
                    if (!invalid) {
                        try {
                            thread.notifySuspendedNoFire(tr == eventThread, false);
                        } catch (ObjectCollectedException ocex) {
                            invalid = true;
                        }
                    } else if (status == JPDAThread.STATE_UNKNOWN || status == JPDAThread.STATE_ZOMBIE) {
                        threadsTranslation.remove(tr);
                    }
                }
            }
        }
    }

    /**
     * Used by ContinueActionProvider & StepActionProvider.
     */
    public void resume () {
        try {
            getOperator().waitForParallelEventsToProcess();
        } catch (InterruptedException iex) {
            return;
        }
        accessLock.readLock().lock();
        try {
            if (!doContinue) {
                doContinue = true;
                // Continue the next time and do nothing now.
                return ;
            }
        } finally {
            accessLock.readLock().unlock();
        }
        PropertyChangeEvent stateChangeEvent;
        //notifyToBeResumedAll();
        VirtualMachine vm;
        synchronized (virtualMachineLock) {
            vm = virtualMachine;
        }
        if (vm != null) {
            logger.fine("VM resume");
            List<JPDAThread> allThreads = getAllThreads(true);
            accessLock.writeLock().lock();
            logger.finer("Debugger WRITE lock taken.");
            stateChangeEvent = setStateNoFire(STATE_RUNNING);
            // We must resume only threads which are regularly suspended.
            // Otherwise we may unexpectedly resume threads which just hit an event!
            
            // However, this can not be done right without an atomic resume of a set of threads.
            // Since this functionality is not available in the backend, we will
            // call VirtualMachine.resume() if all threads are suspended
            // (so that the model of of suspend all/resume all works correctly)
            // and call ThreadReference.resume() on individual suspended threads
            // (so that suspend of event thread together with continue works correctly).

            // Other cases (some threads suspended and some running with events suspending all threads)
            // will NOT WORK CORRECTLY. Because while resuming threads one at a time,
            // if the first one hits an event which suspends all, other resumes will resume the
            // suspended threads.
            // But this looks like a reasonable trade-off considering the available functionality.
            
            List<JPDAThreadImpl> threadsToResume = null;
            try {
                // Deal only with threads that are living
                {
                    boolean modifiableAllThreads = false;
                    int n = allThreads.size();
                    for (int i = 0; i < n; i++) {
                        JPDAThread t = allThreads.get(i);
                        int status = t.getState();
                        if (status == JPDAThread.STATE_ZOMBIE || status == JPDAThread.STATE_UNKNOWN ||
                            t.getName().contains(ThreadsCache.THREAD_NAME_FILTER_PATTERN) && !t.isSuspended()) {
                            if (!modifiableAllThreads) {
                                allThreads = new ArrayList<JPDAThread>(allThreads);
                                modifiableAllThreads = true;
                            }
                            allThreads.remove(i);
                            n--;
                            i--;
                        }
                    }
                }

                threadsToResume = new ArrayList<JPDAThreadImpl>();
                for (JPDAThread t : allThreads) {
                    if (t.isSuspended() || ((JPDAThreadImpl) t).getThreadReference().suspendCount() > 0) {
                        // There can be threads that have running state, are not suspended, but their suspend count is > 0. These need to be resumed I guess...
                        threadsToResume.add((JPDAThreadImpl) t);
                    }
                }

                for (int i = 0; i < threadsToResume.size(); i++) {
                    JPDAThreadImpl t = threadsToResume.get(i);
                    boolean can = t.cleanBeforeResume();
                    if (!can) {
                        threadsToResume.remove(i);
                        i--;
                    }
                }
                if (vmSuspended || allThreads.size() == threadsToResume.size()) {
                    // Resuming all
                    for (JPDAThreadImpl t : threadsToResume) {
                        t.setAsResumed(false);
                        t.reduceThreadSuspendCount();
                    }
                    // We also need to check for newly-born threads,
                    // that we do not know about yet,
                    // and that might be suspended multiple-times.
                    reduceThreadSuspendCountOfAllBut(vm, threadsToResume);
                    //logger.severe("Before VM.resume():");
                    //Operator.dumpThreadsStatus(vm, Level.SEVERE);
                    VirtualMachineWrapper.resume(vm);
                    vmSuspended = false;
                    logger.finer("All VM threads resumed.");
                    //logger.severe("After VM.resume():");
                    //Operator.dumpThreadsStatus(vm, Level.SEVERE);
                } else {
                    logger.finer("Resuming selected suspended threads.");
                    for (JPDAThreadImpl t : threadsToResume) {
                        t.setAsResumed(false);
                        t.resumeAfterClean();
                    }
                }
            } catch (VMDisconnectedExceptionWrapper e) {
            } catch (InternalExceptionWrapper e) {
            } finally {
                accessLock.writeLock().unlock();
                logger.finer("Debugger WRITE lock released.");
                if (stateChangeEvent != null) {
                    firePropertyChange(stateChangeEvent);
                }
                if (threadsToResume != null) {
                    for (JPDAThreadImpl t : threadsToResume) {
                        try {
                            t.fireAfterResume();
                        } catch (ThreadDeath td) {
                            throw td;
                        } catch (Throwable th) {
                            Exceptions.printStackTrace(th);
                        }
                    }
                }
            }
        }
    }
    
    private void reduceThreadSuspendCountOfAllBut(VirtualMachine vm, List<JPDAThreadImpl> threadsToIgnore) {
        List<ThreadReference> allThreads = VirtualMachineWrapper.allThreads0(vm);
        if (allThreads.size() == threadsToIgnore.size()) {
            return; // Everything is under control.
        }
        Set<Long> ignoredIDs = new HashSet<Long>(threadsToIgnore.size());
        for (JPDAThreadImpl t : threadsToIgnore) {
            ignoredIDs.add(t.getThreadReference().uniqueID());
        }
        for (ThreadReference t : allThreads) {
            if (ignoredIDs.contains(t.uniqueID())) {
                continue;
            }
            try {
                int count = ThreadReferenceWrapper.suspendCount (t);
                //logger.severe("Reducing suspend count of "+JPDAThreadImpl.getThreadStateLog(t));
                while (count > 1) {
                    ThreadReferenceWrapper.resume (t); count--;
                }
            } catch (IllegalThreadStateExceptionWrapper ex) {
                // Thrown when thread has exited
            } catch (ObjectCollectedExceptionWrapper ex) {
            } catch (InternalExceptionWrapper iex) {
            } catch (VMDisconnectedExceptionWrapper vmdex) {
            }
        }
    }

    /** DO NOT CALL FROM ANYWHERE BUT JPDAThreadImpl.resume(). */
    public boolean currentThreadToBeResumed() {
        accessLock.readLock().lock();
        try {
            if (!doContinue) {
                doContinue = true;
                // Continue the next time and do nothing now.
                return false;
            }
        } finally {
            accessLock.readLock().unlock();
        }
        setState (STATE_RUNNING);
        return true;
    }

    public void resumeCurrentThread() {
        accessLock.readLock().lock();
        try {
            if (!doContinue) {
                doContinue = true;
                // Continue the next time and do nothing now.
                return ;
            }
        } finally {
            accessLock.readLock().unlock();
        }
        setState (STATE_RUNNING);
        currentThread.resume();
    }

    public void notifyToBeResumedAll() {
        List<? extends JPDAThreadImpl> threads = threadsTranslation.getTranslated((o) -> (o instanceof JPDAThreadImpl) ? (JPDAThreadImpl) o : null);
        int n = threads.size();
        if (n > 0) {
            processInParallel(n, (i) -> {
                JPDAThreadImpl thread = threads.get(i);
                int status = thread.getState();
                boolean invalid = (status == JPDAThread.STATE_NOT_STARTED ||
                                   status == JPDAThread.STATE_UNKNOWN ||
                                   status == JPDAThread.STATE_ZOMBIE);
                if (!invalid) {
                    thread.notifyToBeResumed();
                } else if (status == JPDAThread.STATE_UNKNOWN || status == JPDAThread.STATE_ZOMBIE) {
                    threadsTranslation.remove(thread.getThreadReference());
                }
            });
        }
    }

    public void notifyToBeResumedAllNoFire() {
        notifyToBeResumedAllNoFire(null);
    }
    
    public void notifyToBeResumedAllNoFire(Set<ThreadReference> ignoredThreads) {
        List<? extends JPDAThreadImpl> threads = threadsTranslation.getTranslated((o) ->
                (o instanceof JPDAThreadImpl && (ignoredThreads == null || !ignoredThreads.contains(o))) ? (JPDAThreadImpl) o : null);
        for (JPDAThreadImpl thread : threads) {
            int status = thread.getState();
            boolean invalid = (status == JPDAThread.STATE_NOT_STARTED ||
                               status == JPDAThread.STATE_UNKNOWN ||
                               status == JPDAThread.STATE_ZOMBIE);
            if (!invalid) {
                thread.notifyToBeResumedNoFire();
            } else if (status == JPDAThread.STATE_UNKNOWN || status == JPDAThread.STATE_ZOMBIE) {
                threadsTranslation.remove(thread.getThreadReference());
            }
        }
    }
    
    public void setCurrentSuspendedNoFireThread(JPDAThreadImpl thread) {
        this.currentSuspendedNoFireThread = thread;
    }

    private Set<JPDAThreadGroup> interestedThreadGroups = Collections.newSetFromMap(new WeakHashMap<>());

    public void interestedInThreadGroup(JPDAThreadGroup tg) {
        interestedThreadGroups.add(tg);
    }

    public boolean isInterestedInThreadGroups() {
        return !interestedThreadGroups.isEmpty();
    }

    public ThreadsCache getThreadsCache() {
        synchronized (threadsCollectorLock) {
            if (threadsCache == null) {
                threadsCache = new ThreadsCache(this);
                threadsCache.addPropertyChangeListener(new PropertyChangeListener() {
                    //  Re-fire the changes
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        rpCreateThreads.post(() -> {
                            String propertyName = evt.getPropertyName();
                            if (ThreadsCache.PROP_THREAD_STARTED.equals(propertyName)) {
                                JPDAThreadImpl thread = getThread((ThreadReference) evt.getNewValue());
                                if (!thread.getName().contains(ThreadsCache.THREAD_NAME_FILTER_PATTERN)) {
                                    firePropertyChange(PROP_THREAD_STARTED, null, thread);
                                }
                            }
                            if (ThreadsCache.PROP_THREAD_DIED.equals(propertyName)) {
                                JPDAThreadImpl thread = getThread((ThreadReference) evt.getOldValue());
                                if (!thread.getName().contains(ThreadsCache.THREAD_NAME_FILTER_PATTERN)) {
                                    firePropertyChange(PROP_THREAD_DIED, thread, null);
                                }
                            }
                            if (ThreadsCache.PROP_GROUP_ADDED.equals(propertyName)) {
                                firePropertyChange(PROP_THREAD_GROUP_ADDED, null, getThreadGroup((ThreadGroupReference) evt.getNewValue()));
                            }
                        });
                    }
                });
            }
            return threadsCache;
        }
    }

    List<JPDAThread> getAllThreads() {
        return getAllThreads(false);
    }
    
    List<JPDAThread> getAllThreads(boolean findNewBornThreads) {
        ThreadsCache tc = getThreadsCache();
        if (tc == null) {
            return Collections.emptyList();
        }
        List<ThreadReference> threadList = tc.getAllThreads();
        if (findNewBornThreads) {
            Set<Long> threadIDs = new HashSet<Long>(threadList.size());
            for (ThreadReference t : threadList) {
                threadIDs.add(t.uniqueID());
            }
            List<ThreadReference> allThreads = VirtualMachineWrapper.allThreads0(virtualMachine);
            boolean added = false;
            for (ThreadReference t : allThreads) {
                if (!threadIDs.contains(t.uniqueID())) {
                    if (!added) {
                        threadList = new ArrayList<ThreadReference>(threadList);
                        added = true;
                    }
                    threadList.add(t);
                }
            }
        }
        int n = threadList.size();
        final List<ThreadReference> tl = threadList;

        // Create threads in parallel.
        // Constructor of JPDAThreadImpl calls getName() and getStatus(), which take time on slow connection during remote debugging.
        JPDAThread[] threads = new JPDAThread[n];
        if (n > 0) {
            processInParallel(n, (i) -> {
                JPDAThreadImpl thread = getThread(tl.get(i));
                if (!thread.getName().contains(ThreadsCache.THREAD_NAME_FILTER_PATTERN)) {
                    threads[i] = thread;
                } else {
                    threads[i] = null;
                }
            });
            List<JPDAThread> threadsFiltered = new ArrayList<>(n);
            for (JPDAThread t : threads) {
                if (t != null) {
                    threadsFiltered.add(t);
                }
            }
            return Collections.unmodifiableList(threadsFiltered);
        } else {
            return Collections.emptyList();
        }
    }

    private void processInParallel(int n, Consumer<Integer> task) {
        // Run by chunks:
        int chunks = Math.min(RP_THROUGHPUT, n);
        int chn = n / chunks + ((n % chunks) > 0 ? 1 : 0);
        class TranslateThreads implements Runnable {

            private final int ch;

            private TranslateThreads(int ch) {
                this.ch = ch;
            }

            @Override
            public void run() {
                int i0 = ch*chn;
                for (int i = i0; i < i0 + chn && i < n; i++) {
                    task.accept(i);
                }
            }
        }
        RequestProcessor.Task[] tasks = new RequestProcessor.Task[chunks];
        for (int ch = 0; ch < chunks; ch++) {
            tasks[ch] = rpCreateThreads.post(new TranslateThreads(ch));
        }
        for (int ch = 0; ch < chunks; ch++) {
            tasks[ch].waitFinished();
        }
    }

    public JPDAThreadGroup[] getTopLevelThreadGroups() {
        ThreadsCache tc = getThreadsCache();
        if (tc == null) {
            return new JPDAThreadGroup[0];
        }
        List<ThreadGroupReference> groupList = tc.getTopLevelThreadGroups();
        JPDAThreadGroup[] groups = new JPDAThreadGroup[groupList.size()];
        for (int i = 0; i < groups.length; i++) {
            groups[i] = getThreadGroup((ThreadGroupReference) groupList.get(i));
        }
        return groups;
    }

    public JPDAThreadImpl getThread (ThreadReference tr) {
        return (JPDAThreadImpl) threadsTranslation.translate (tr);
    }

    public JPDAThreadImpl getExistingThread (ThreadReference tr) {
        return (JPDAThreadImpl) threadsTranslation.translateExisting(tr);
    }

    public JPDAThreadGroup getThreadGroup (ThreadGroupReference tgr) {
        return (JPDAThreadGroup) threadsTranslation.translate (tgr);
    }

    public Variable getLocalVariable(JPDAThread thread, LocalVariable lv, Value v) {
        return (Variable) localsTranslation.translateOnThread(thread, lv, v);
    }

    public JPDAClassType getClassType(ReferenceType cr) {
        return (JPDAClassType) localsTranslation.translate (cr);
    }

    public Variable getVariable (Value value) {
        if (value instanceof ClassObjectReference) {
            return new ClassVariableImpl(this, (ClassObjectReference) value, null);
        }
        return createVariable(value);
    }
    
    private Variable createVariable(Value v) {
        if (v instanceof ObjectReference || v == null) {
            return new AbstractObjectVariable (
                this,
                (ObjectReference) v,
                null
            );
        } else {
            return new AbstractVariable (this, v, null);
        }
    }

    @Override
    public Variable createMirrorVar(Object obj, boolean isPrimitive) throws InvalidObjectException {
        if (obj == null) {
            return null;
        }
        Variable v;
        try {
            v = getVariable(VariableMirrorTranslator.createValueFromMirror(obj, !isPrimitive, this));
        } catch (IllegalArgumentExceptionWrapper ex) {
            InvalidObjectException ioex = new InvalidObjectException(ex.getLocalizedMessage());
            ioex.initCause(ex);
            throw ioex;
        } catch (InternalExceptionWrapper ex) {
            InvalidObjectException ioex = new InvalidObjectException(ex.getLocalizedMessage());
            ioex.initCause(ex);
            throw ioex;
        } catch (VMDisconnectedExceptionWrapper ex) {
            InvalidObjectException ioex = new InvalidObjectException(ex.getLocalizedMessage());
            ioex.initCause(ex);
            throw ioex;
        } catch (ObjectCollectedExceptionWrapper ex) {
            InvalidObjectException ioex = new InvalidObjectException(ex.getLocalizedMessage());
            ioex.initCause(ex);
            throw ioex;
        } catch (InvalidTypeException ex) {
            InvalidObjectException ioex = new InvalidObjectException(ex.getLocalizedMessage());
            ioex.initCause(ex);
            throw ioex;
        } catch (ClassNotLoadedException ex) {
            InvalidObjectException ioex = new InvalidObjectException(ex.getLocalizedMessage());
            ioex.initCause(ex);
            throw ioex;
        } catch (ClassNotPreparedExceptionWrapper ex) {
            InvalidObjectException ioex = new InvalidObjectException(ex.getLocalizedMessage());
            ioex.initCause(ex);
            throw ioex;
        }
        if (v == null) {
            throw new InvalidObjectException("No target value from "+obj);
        }
        return v;
    }
    
    public void markObject(ObjectVariable var, String label) {
        synchronized (markedObjects) {
            long uid = var.getUniqueID();
            String oldLabel = markedObjects.remove(uid);
            if (oldLabel != null) {
                markedObjectLabels.remove(oldLabel);
            }
            if (label != null) {
                markedObjects.put(uid, label);
                ObjectVariable markedVar = markedObjectLabels.get(label);
                if (markedVar != null) {
                    markedObjects.remove(markedVar.getUniqueID());
                }
                markedObjectLabels.put(label, var);
            }
        }
    }

    /*public Map<ObjectVariable, String> getMarkedObjects() {
        Map<ObjectVariable, String> map;
        synchronized (markedObjects) {
            map = new LinkedHashMap<ObjectVariable, String>(markedObjects);
        }
        return map;
    }*/

    public String getLabel(ObjectVariable var) {
        if (var instanceof Refreshable) {
            if (!((Refreshable) var).isCurrent()) {
                return null;
            }
        }
        synchronized (markedObjects) {
            return markedObjects.get(var.getUniqueID());
        }
    }

    public ObjectVariable getLabeledVariable(String label) {
        synchronized (markedObjects) {
            return markedObjectLabels.get(label);
        }
    }

    public Map<String, ObjectVariable> getAllLabels() {
        synchronized (markedObjects) {
            return new HashMap<String, ObjectVariable>(markedObjectLabels);
        }
    }

    public ExpressionPool getExpressionPool() {
        return expressionPool;
    }

    synchronized void setSingleThreadStepResumeDecision(Boolean decision) {
        singleThreadStepResumeDecision = decision;
    }

    synchronized Boolean getSingleThreadStepResumeDecision() {
        return singleThreadStepResumeDecision;
    }

    public synchronized void setStepInterruptByBptResumeDecision(Boolean decision) {
        stepInterruptByBptResumeDecision = decision;
    }

    public synchronized Boolean getStepInterruptByBptResumeDecision() {
        return stepInterruptByBptResumeDecision;
    }


    // private helper methods ..................................................

    private PropertyChangeEvent setStateNoFire (int state) {
        int o;
        synchronized (stateLock) {
            if (state == this.state) {
                return null;
            }
            o = this.state;
            this.state = state;
        }
        //PENDING HACK see issue 46287
        System.setProperty(
            "org.openide.awt.SwingBrowserImpl.do-not-block-awt",
            String.valueOf (state != STATE_DISCONNECTED)
        );
        return new PropertyChangeEvent(this, PROP_STATE, o, state);
    }

    private void setState (int state) {
        if (state == STATE_RUNNING) {
            CallStackFrame old;
            synchronized (currentThreadAndFrameLock) {
                old = currentCallStackFrame;
                currentCallStackFrame = null;
            }
            firePropertyChange (
                PROP_CURRENT_CALL_STACK_FRAME,
                old,
                null
            );
        }
        PropertyChangeEvent evt = setStateNoFire(state);
        if (evt != null) {
            firePropertyChange(evt);
        }
    }

    /**
     * Fires property change.
     */
    private void firePropertyChange (String name, Object o, Object n) {
        pcs.firePropertyChange (name, o, n);
        //System.err.println("ALL Change listeners count = "+pcs.getPropertyChangeListeners().length);
    }

    /**
     * Fires property change.
     */
    private void firePropertyChange (PropertyChangeEvent evt) {
        pcs.firePropertyChange (evt);
        //System.err.println("ALL Change listeners count = "+pcs.getPropertyChangeListeners().length);
    }

    private SourcePath engineContext;
    public synchronized SourcePath getEngineContext () {
        if (engineContext == null) {
            engineContext = lookupProvider.lookupFirst(null, SourcePath.class);
        }
        return engineContext;
    }

    private ThreadReference getEvaluationThread () {
        synchronized (currentThreadAndFrameLock) {
            if (currentThread != null) {
                return currentThread.getThreadReference ();
            }
        }
        VirtualMachine vm;
        synchronized (virtualMachineLock) {
            vm = virtualMachine;
        }
        if (vm == null) {
            return null;
        }
        List l;
        try {
            l = VirtualMachineWrapper.allThreads(vm);
        } catch (InternalExceptionWrapper ex) {
            return null;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return null;
        }
        if (l.size () < 1) {
            return null;
        }
        int i, k = l.size ();
        ThreadReference thread = null;
        for (i = 0; i < k; i++) {
            ThreadReference t = (ThreadReference) l.get (i);
            try {
                if (ThreadReferenceWrapper.isSuspended (t)) {
                    thread = t;
                    if (ThreadReferenceWrapper.name (t).equals ("Finalizer")) {
                        return t;
                    }
                }
            } catch (InternalExceptionWrapper ex) {
            } catch (IllegalThreadStateExceptionWrapper ex) {
            } catch (ObjectCollectedExceptionWrapper ex) {
            } catch (VMDisconnectedExceptionWrapper ex) {
            }
        }
        return thread;
    }

    private PropertyChangeEvent updateCurrentCallStackFrameNoFire (JPDAThread thread) {
        CallStackFrame oldSF;
        CallStackFrame newSF;
        if ((thread == null) || (thread.getStackDepth () < 1)) {
            newSF = null;
        } else {
            try {
                CallStackFrame[] csfs = thread.getCallStack(0, 1);
                if (csfs.length > 0) {
                    newSF = csfs[0];
                } else {
                    newSF = null;
                }
            } catch (AbsentInformationException e) {
                newSF = null;
            }
        }
        oldSF = setCurrentCallStackFrameNoFire(newSF);
        if (oldSF == newSF) {
            return null;
        } else {
            return new PropertyChangeEvent(this, PROP_CURRENT_CALL_STACK_FRAME,
                                           oldSF, newSF);
        }
    }
    
    private CallStackFrame preferredTopFrame;
    
    public void setPreferredTopFrame(CallStackFrame preferredTopFrame) {
        this.preferredTopFrame = preferredTopFrame;
    }

    private CallStackFrame getTopFrame(JPDAThread thread) {
        CallStackFrame callStackFrame;
        if (preferredTopFrame != null) {
            callStackFrame = preferredTopFrame;
            preferredTopFrame = null;
            return callStackFrame;
        }
        if ((thread == null) || (thread.getStackDepth () < 1)) {
            callStackFrame = null;
        } else {
            try {
                CallStackFrame[] csfs = thread.getCallStack(0, 1);
                if (csfs.length > 0) {
                    callStackFrame = csfs[0];
                } else {
                    callStackFrame = null;
                }
            } catch (AbsentInformationException e) {
                callStackFrame = null;
            }
        }
        return callStackFrame;
    }

    /**
     * @param thread The thread to take the top frame from
     * @return A PropertyChangeEvent or <code>null</code>.
     */
    private PropertyChangeEvent updateCurrentCallStackFrameNoFire(CallStackFrame callStackFrame) {
        CallStackFrame old;
        old = setCurrentCallStackFrameNoFire(callStackFrame);
        if (old == callStackFrame) {
            return null;
        } else {
            return new PropertyChangeEvent(this, PROP_CURRENT_CALL_STACK_FRAME,
                                           old, callStackFrame);
        }
    }

    private void checkJSR45Languages (JPDAThread t) {
        if (t.getStackDepth () > 0) {
            try {
                CallStackFrame[] frames = t.getCallStack (0, 1);
                if (frames.length < 1) {
                    return ; // Internal error or disconnected
                }
                checkJSR45Languages(frames[0]);
            } catch (AbsentInformationException e) {
            }
        }
    }
    
    private void checkJSR45Languages(CallStackFrame f) {
        List<String> l = f.getAvailableStrata ();
        String stratum = f.getDefaultStratum ();
        //String sourceDebugExtension;
            //sourceDebugExtension = (String) f.getClass().getMethod("getSourceDebugExtension").invoke(f);
        /* This was moved to CallStackFrameImpl.
        if (l.size() == 1 && "Java".equals(l.get(0))) {     // NOI18N
            // Hack for non-Java languages that do not define stratum:
            String sourceName = f.getSourceName(null);
            int ext = sourceName.lastIndexOf('.');
            if (ext > 0) {
                String extension = sourceName.substring(++ext);
                extension = extension.toUpperCase();
                if (!"JAVA".equals(extension)) {    // NOI18N
                    l = Collections.singletonList(extension);
                    stratum = extension;
                }
            } else if ("<eval>".equals(sourceName)) {
                // Check Nashorn:
                if ("jdk/nashorn/internal/scripts/<eval>".equals(f.getSourcePath(null))) {
                    l = Collections.singletonList("JS");
                    stratum = "JS";
                }
            }
        }*/
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            if (!languages.contains (l.get (i))) {
                String language = l.get (i);
                DebuggerManager.getDebuggerManager ().startDebugging (
                    createJSR45DI (language)
                );
                languages.add (language);
            }
        } // for
        if (stratum != null) {
            javaEngineProvider.getSession ().setCurrentLanguage (stratum);
        }
    }

    private Set<JSR45DebuggerEngineProvider> jsr45EngineProviders;

    private DebuggerInfo createJSR45DI (final String language) {
        if (jsr45EngineProviders == null) {
            jsr45EngineProviders = new HashSet<JSR45DebuggerEngineProvider>(1);
        }
        JSR45DebuggerEngineProvider provider = new JSR45DebuggerEngineProvider(language, getRequestProcessor());
        jsr45EngineProviders.add(provider);
        return DebuggerInfo.create (
            "netbeans-jpda-JSR45DICookie-" + language,
            new Object[] {
                new DelegatingSessionProvider () {
                    @Override
                    public Session getSession (
                        DebuggerInfo debuggerInfo
                    ) {
                        return javaEngineProvider.getSession ();
                    }
                },
                provider
            }
        );
    }

    @Override
    public JPDAStep createJPDAStep(int size, int depth) {
        Session session = lookupProvider.lookupFirst(null, Session.class);
        return new JPDAStepImpl(this, session, size, depth);
    }

    /*public synchronized Heap getHeap() {
        if (virtualMachine != null && canGetInstanceInfo(virtualMachine)) {
            return new HeapImpl(virtualMachine);
        } else {
            return null;
        }
    }*/

    @Override
    public List<JPDAClassType> getAllClasses() {
        //assert !java.awt.EventQueue.isDispatchThread() : "All classes retrieving in AWT Event Queue!";
        List<ReferenceType> classes;
        synchronized (virtualMachineLock) {
            if (virtualMachine == null) {
                classes = Collections.emptyList();
            } else {
                classes = VirtualMachineWrapper.allClasses0(virtualMachine);
            }
        }
        return new ClassTypeList(this, classes);
    }

    @Override
    public List<JPDAClassType> getClassesByName(String name) {
        List<ReferenceType> classes;
        synchronized (virtualMachineLock) {
            if (virtualMachine == null) {
                classes = Collections.emptyList();
            } else {
                classes = VirtualMachineWrapper.classesByName0(virtualMachine, name);
            }
        }
        return new ClassTypeList(this, classes);
    }

    @Override
    public long[] getInstanceCounts(List<JPDAClassType> classTypes) throws UnsupportedOperationException {
            //assert !java.awt.EventQueue.isDispatchThread() : "Instance counts retrieving in AWT Event Queue!";
            VirtualMachine vm;
            synchronized (virtualMachineLock) {
                vm = virtualMachine;
            }
            if (vm == null) {
                return new long[classTypes.size()];
            }
            List<ReferenceType> types;
            if (classTypes instanceof ClassTypeList) {
                ClassTypeList cl = (ClassTypeList) classTypes;
                types = cl.getTypes();
            } else {
                types = new ArrayList<ReferenceType>(classTypes.size());
                for (JPDAClassType clazz : classTypes) {
                    types.add(((JPDAClassTypeImpl) clazz).getType());
                }
            }
            try {
                return VirtualMachineWrapper.instanceCounts(vm, types);
            } catch (InternalExceptionWrapper e) {
                return new long[classTypes.size()];
            } catch (VMDisconnectedExceptionWrapper e) {
                return new long[classTypes.size()];
            }
    }

    @Override
    public boolean canGetInstanceInfo() {
        synchronized (virtualMachineLock) {
            return virtualMachine != null && virtualMachine.canGetInstanceInfo();
        }
    }

    @Override
    public ThreadsCollectorImpl getThreadsCollector() {
        synchronized (threadsCollectorLock) {
            if (threadsCollector == null) {
                threadsCollector = new ThreadsCollectorImpl(this);
            }
            return threadsCollector;
        }
    }

    DeadlockDetector getDeadlockDetector() {
        synchronized (threadsCollectorLock) {
            if (deadlockDetector == null) {
                deadlockDetector = new DeadlockDetectorImpl(this);
            }
            return deadlockDetector;
        }
    }
    
    private static final String COMPUTING_INTERFACES = "computing"; // NOI18N

    public List<JPDAClassType> getAllInterfaces(ClassType ct) {
        try {
            List allInterfaces;
            boolean toCompute = false;
            synchronized (allInterfacesMap) {
                allInterfaces = allInterfacesMap.get(ct);
                if (allInterfaces == null) {
                    allInterfaces = new ArrayList();
                    allInterfaces.add(COMPUTING_INTERFACES);
                    allInterfacesMap.put(ct, allInterfaces);
                    toCompute = true;
                }
            }
            if (toCompute) {
                List<InterfaceType> interfaces = null;
                try {
                    //assert !javax.swing.SwingUtilities.isEventDispatchThread();
                    interfaces = ClassTypeWrapper.allInterfaces0(ct);
                } finally {
                    if (interfaces == null) {
                        synchronized (allInterfacesMap) {
                            allInterfacesMap.remove(ct);
                        }
                    }
                    synchronized (allInterfaces) {
                        allInterfaces.clear();
                        if (interfaces != null) {
                            for (InterfaceType it : interfaces) {
                                allInterfaces.add(getClassType(it));
                            }
                        }
                        allInterfaces.notifyAll();
                    }
                }
            } else {
                synchronized (allInterfaces) {
                    if (allInterfaces.contains(COMPUTING_INTERFACES)) {
                        try {
                            allInterfaces.wait();
                        } catch (InterruptedException ex) {
                            return null;
                        }
                    }
                }
                if (allInterfaces.contains(COMPUTING_INTERFACES)) {
                    // some race-condition? (http://netbeans.org/bugzilla/show_bug.cgi?id=219823)
                    assert !allInterfaces.contains(COMPUTING_INTERFACES);
                    return null;
                }
            }
            return Collections.unmodifiableList(allInterfaces);
        } catch (ClassNotPreparedExceptionWrapper cnpex) {
            return null;
        }
    }

    public boolean hasAllInterfaces(ClassType ct) {
        List allInterfaces;
        synchronized (allInterfacesMap) {
            allInterfaces = allInterfacesMap.get(ct);
        }
        if (allInterfaces == null) {
            return false;
        } else {
            synchronized (allInterfaces) {
                if (allInterfaces.contains("computing")) {      // NOI18N
                    return false;
                }
            }
        }
        return true;
    }
    
    public Variable getFormattedValue(ObjectVariable ov) {
        return getFormattedValue(ov, new FormattersLoopControl());
    }
    
    private Variable getFormattedValue(ObjectVariable ov, FormattersLoopControl formattersLoopControl) {
        JPDAClassType ct = ov.getClassType();
        if (ct == null) {
            return ov;
        }
        VariablesFormatter f = Formatters.getFormatterForType(ct, formattersLoopControl.getFormatters());
        if (f != null && formattersLoopControl.canUse(f, ct.getName(), null)) {
            String code = f.getValueFormatCode();
            if (code != null && code.length() > 0) {
                try {
                    Variable ret = ((AbstractObjectVariable) ov).evaluate(code);
                    if (ret == null) {
                        return null;
                    }
                    if (ret instanceof ObjectVariable) {
                        return getFormattedValue((ObjectVariable) ret, formattersLoopControl);
                    } else {
                        return ret;
                    }
                } catch (InvalidExpressionException iex) {
                    //return VariablesTableModel.getMessage((InvalidExpressionException) t);
                }
            }
        }
        return ov;
    }

    public void actionMessageCallback(Object action, String message) {
        List<? extends ActionMessageCallback> amcList = lookupProvider.lookup(null, ActionMessageCallback.class);
        for (ActionMessageCallback amc : amcList) {
            amc.messageCallback(action, message);
        }
    }
    
    public void actionErrorMessageCallback(Object action, String message) {
        List<? extends ActionErrorMessageCallback> amcList = lookupProvider.lookup(null, ActionErrorMessageCallback.class);
        for (ActionErrorMessageCallback amc : amcList) {
            amc.errorMessageCallback(action, message);
        }
    }
    
    public void actionStatusDisplayCallback(Object action, String status) {
        List<? extends ActionStatusDisplayCallback> asdcList = lookupProvider.lookup(null, ActionStatusDisplayCallback.class);
        for (ActionStatusDisplayCallback asdc : asdcList) {
            asdc.statusDisplayCallback(action, status);
        }
    }
    

    private static class DebuggerReentrantReadWriteLock extends ReentrantReadWriteLock {

        private ReadLock readerLock;
        private WriteLock writerLock;

        public DebuggerReentrantReadWriteLock(boolean fair) {
            super(fair);
            readerLock = new DebuggerReadLock(this);
            writerLock = new DebuggerWriteLock(this);
        }

        @Override
        public ReadLock readLock() {
            return readerLock;
        }

        @Override
        public WriteLock writeLock() {
            return writerLock;
        }

        private static class DebuggerReadLock extends ReentrantReadWriteLock.ReadLock {

            protected DebuggerReadLock(ReentrantReadWriteLock lock) {
                super(lock);
            }

            @Override
            public void lock() {
                assert !Mutex.EVENT.isReadAccess() : "Debugger lock taken in AWT Event Queue!";
                super.lock();
            }

        }

        private static class DebuggerWriteLock extends ReentrantReadWriteLock.WriteLock {

            protected DebuggerWriteLock(ReentrantReadWriteLock lock) {
                super(lock);
            }

            @Override
            public void lock() {
                assert !Mutex.EVENT.isReadAccess() : "Debugger lock taken in AWT Event Queue!";
                super.lock();
            }

        }

    }
    
    private static class PeriodicThreadsDump implements Runnable {
        
        private static final int INTERVAL = 5000;
        private RequestProcessor rp = new RequestProcessor(PeriodicThreadsDump.class.getName());
        private VirtualMachine vm;
        private volatile boolean finish = false;
        
        public PeriodicThreadsDump(VirtualMachine vm) {
            this.vm = vm;
            rp.post(this, INTERVAL);
        }
        
        public void finish() {
            finish = true;
        }

        @Override
        public void run() {
            List<ThreadReference> allThreads = vm.allThreads();
            System.err.println("All Threads:");
            for (ThreadReference tr : allThreads) {
                String name = tr.name();
                boolean suspended = tr.isSuspended();
                int suspendCount = tr.suspendCount();
                int status = tr.status();
                System.err.println(name+"\t SUSP = "+suspended+", COUNT = "+suspendCount+", STATUS = "+status);
            }
            System.err.println("");
            if (!finish) {
                rp.post(this, INTERVAL);
            }
        }
    }

}
