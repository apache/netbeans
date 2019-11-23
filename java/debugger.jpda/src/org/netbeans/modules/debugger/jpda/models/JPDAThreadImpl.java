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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.api.debugger.jpda.MonitorInfo;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.SingleThreadWatcher;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidRequestStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MethodWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MirrorWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MonitorInfoWrapper;
import org.netbeans.modules.debugger.jpda.jdi.NativeMethodExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeComponentWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMOutOfMemoryExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.EventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.BreakpointRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.MonitorContendedEnteredRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.StepRequestWrapper;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.modules.debugger.jpda.util.Operator;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;

import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 * The implementation of JPDAThread.
 */
public final class JPDAThreadImpl implements JPDAThread, Customizer, BeanContextChild {

    private static final String PROP_LOCKER_THREADS = "lockerThreads"; // NOI18N
    private static final String PROP_STEP_SUSPENDED_BY_BREAKPOINT = "stepSuspendedByBreakpoint"; // NOI18N
    /**
     * Name of a property change event, that is fired when the current operation
     * or last operations change. It's intentionally fired to dedicated property
     * change listeners only, when added for this property specifically.
     * It's intentionally fired synchronously with the operation change, under
     * the thread lock.
     */
    public static final String PROP_OPERATIONS_SET = "operationsSet"; // NOI18N
    public static final String PROP_OPERATIONS_UPDATE = "operationsUpdate"; // NOI18N

    private static final Logger logger = Logger.getLogger(JPDAThreadImpl.class.getName()); // NOI18N
    private static final Logger loggerS = Logger.getLogger(JPDAThreadImpl.class.getName()+".suspend"); // NOI18N
    
    private ThreadReference     threadReference;
    private JPDADebuggerImpl    debugger;
    /** Thread is suspended and everybody know about this. */
    private boolean             suspended;
    private boolean             suspendedOnAnEvent; // Suspended by an event that occured in this thread
    /** Thread is suspended, but only this class knows it.
        A notification about real suspend or resume is expected to come soon.
        Typically just some evaluation, which will decide what's going to be done next, is just being performed.
        We do not notify anyone about this, in order not to trigger unnecessary work (refreshing variables view, thread stack frames, etc.).*/
    private boolean             suspendedNoFire;
    /** Suspend was requested while this thread was suspended, but looked like running to others. */
    private boolean             suspendRequested;
    private boolean             initiallySuspended;
    private int                 suspendCount;
    private Operation           currentOperation;
    private List<Operation>     lastOperations;
    private boolean             doKeepLastOperations;
    private ReturnVariableImpl  returnVariable;
    private PropertyChangeSupport pch = new PropertyChangeSupport(this);
    private PropertyChangeSupport operationsPch = new PropertyChangeSupport(this);
    // There's a caching mechanism in ThreadReferenceImpl in JDK 7
    // However, the stack depth query is not synchronized, therefore we cache it
    // for efficiency here.
    private int                 stackDepth = -1;
    private CallStackFrame[]    cachedFrames;
    private int                 cachedFramesFrom = -1;
    private int                 cachedFramesTo = -1;
    private final Object        cachedFramesLock = new Object();
    private JPDABreakpoint      currentBreakpoint;
    private JPDABreakpointEvent breakpointEvent;
    private String              threadName;
    private final Object        lockerThreadsLock = new Object();
    //private Map<JPDAThread, Variable> lockerThreads;
    //private Map<ThreadReference, ObjectReference> lockerThreads2;
    private ObjectReference     lockerThreadsMonitor;
    private List<JPDAThread>    lockerThreadsList;
    private List<ThreadReference> resumedBlockingThreads;
    private final Object        stepBreakpointLock = new Object();
    /** Step in some thread got suspended by this breakpoint hit in this thread. */
    private JPDABreakpoint      stepSuspendedByBreakpoint;
    /** A set of threads in which a step is pending and which got suspended
     *  by a breakpoint hit in this thread. */
    private Set<JPDAThreadImpl> suspendedSteppingThreads;
    /** A set of threads which hit breakpoints that suspended a step in this thread. */
    private Set<JPDAThreadImpl> steppingSuspendedByBptsInThreads;
    private VirtualMachine      vm;
    /** Lock under which we're safe to suspend this thread for the purpose of checking for monitors. */
    private final Object        suspendToCheckForMonitorsLock = new Object();
    private boolean             canSuspendToCheckForMonitors;

    public final ReadWriteLock  accessLock = new ThreadReentrantReadWriteLock();
    private final Object ownedMonitorsAndFramesSingleAccessLock = new Object();

    public JPDAThreadImpl (
        ThreadReference     threadReference,
        JPDADebuggerImpl    debugger
    ) {
        this.threadReference = threadReference;
        this.debugger = debugger;
        boolean initFailed = false;
        threadName = "";
        try {
            vm = MirrorWrapper.virtualMachine(threadReference);
            threadName = ThreadReferenceWrapper.name(threadReference);
            suspended = ThreadReferenceWrapper.isSuspended(threadReference);
            if (suspended) {
                suspendCount = ThreadReferenceWrapper.suspendCount(threadReference);
            } else {
                suspendCount = 0;
            }
            initiallySuspended = suspended;
        } catch (IllegalThreadStateExceptionWrapper itsex) {
            initFailed = true;
        } catch (ObjectCollectedExceptionWrapper ex) {
            initFailed = true;
        } catch (VMDisconnectedExceptionWrapper ex) {
            initFailed = true;
        } catch (InternalExceptionWrapper ex) {
            initFailed = true;
        }
        if (initFailed) {
            suspended = false;
            suspendCount = 0;
        }
    }

    @Override
    public Lock getReadAccessLock() {
        return accessLock.readLock();
    }

    /**
     * Getter for the name of thread property.
     *
     * @return name of thread.
     */
    @Override
    public String getName () {
        return threadName;
    }
    
    /**
    * Returns parent thread group.
    *
    * @return parent thread group.
    */
    @Override
    public JPDAThreadGroup getParentThreadGroup () {
        try {
            ThreadGroupReference tgr = ThreadReferenceWrapper.threadGroup (threadReference);
            if (tgr == null) return null;
            return debugger.getThreadGroup(tgr);
        } catch (IllegalThreadStateExceptionWrapper ex) {
            return null; // Thrown when thread has exited
        } catch (ObjectCollectedExceptionWrapper ex) {
            return null;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return null;
        } catch (VMOutOfMemoryExceptionWrapper ex) {
            return null;
        } catch (InternalExceptionWrapper ex) {
            return null;
        }
    }

    /**
     * Returns line number of the location this thread stopped at.
     * The thread should be suspended at the moment this method is called.
     *
     * @return  line number of the current location if the thread is suspended,
     *          contains at least one frame and the topmost frame does not
     *          represent a native method invocation; <CODE>-1</CODE> otherwise
     * @see  CallStackFrame
    */
    @Override
    public int getLineNumber (String stratum) {
        accessLock.readLock().lock();
        try {
            if (suspended || suspendedNoFire) {
                try {
                    CallStackFrame[] topStackRef = getCallStack(0, 1);
                    if (topStackRef.length > 0) {
                        return topStackRef[0].getLineNumber(stratum);
                    }
                } catch (AbsentInformationException aiex) {}
                synchronized (cachedFramesLock) {
                    if (stackDepth < 0) {
                        stackDepth = ThreadReferenceWrapper.frameCount0(threadReference);
                    }
                    if (stackDepth < 1) return -1;
                }
                return LocationWrapper.lineNumber(StackFrameWrapper.location(
                        ThreadReferenceWrapper.frame(threadReference, 0)), stratum);
            }
        } catch (ObjectCollectedExceptionWrapper ex) {
        } catch (InvalidStackFrameExceptionWrapper ex) {
        } catch (IncompatibleThreadStateException ex) {
        } catch (IllegalThreadStateExceptionWrapper ex) {
            // Thrown when thread has exited
        } catch (VMDisconnectedExceptionWrapper ex) {
        } catch (InternalExceptionWrapper ex) {
        } finally {
            accessLock.readLock().unlock();
        }
        return -1;
    }

    public Method getTopMethod() {
        accessLock.readLock().lock();
        try {
            if (suspended || suspendedNoFire) {
                synchronized (cachedFramesLock) {
                    if (stackDepth < 0) {
                        stackDepth = ThreadReferenceWrapper.frameCount0(threadReference);
                    }
                    if (stackDepth < 1) return null;
                }
                return LocationWrapper.method(StackFrameWrapper.location(
                        ThreadReferenceWrapper.frame(threadReference, 0)));
            }
        } catch (ObjectCollectedExceptionWrapper ex) {
        } catch (InvalidStackFrameExceptionWrapper ex) {
        } catch (IncompatibleThreadStateException ex) {
        } catch (IllegalThreadStateExceptionWrapper ex) {
            // Thrown when thread has exited
        } catch (VMDisconnectedExceptionWrapper ex) {
        } catch (InternalExceptionWrapper ex) {
        } finally {
            accessLock.readLock().unlock();
        }
        return null;
    }

    @Override
    public synchronized Operation getCurrentOperation() {
        return currentOperation;
    }
    
    public synchronized void setCurrentOperation(Operation operation) { // Set the current operation for the default stratum.
        this.currentOperation = operation;
        fireOperationsChanged(PROP_OPERATIONS_SET);
    }

    @Override
    public synchronized List<Operation> getLastOperations() {
        return lastOperations;
    }
    
    public synchronized void addLastOperation(Operation operation) {
        if (lastOperations == null) {
            lastOperations = new ArrayList<Operation>();
        }
        lastOperations.add(operation);
        fireOperationsChanged(PROP_OPERATIONS_SET);
    }
    
    public synchronized void updateLastOperation(Operation operation) {
        this.currentOperation = operation;
        if (lastOperations == null) {
            lastOperations = new ArrayList<Operation>();
        }
        fireOperationsChanged(PROP_OPERATIONS_UPDATE);
    }
    
    public synchronized void clearLastOperations() {
        if (lastOperations != null) {
            for (Operation last : lastOperations) {
                last.setReturnValue(null); // reset the returned value.
                // Operation might be reused, but the execution path is gone.
            }
        }
        lastOperations = null;
        fireOperationsChanged(PROP_OPERATIONS_SET);
    }
    
    public synchronized void holdLastOperations(boolean doHold) {
        doKeepLastOperations = doHold;
    }

    @Override
    public synchronized JPDABreakpoint getCurrentBreakpoint() {
        if (currentBreakpoint != null && currentBreakpoint.isHidden()) {
            return null;
        } else {
            return currentBreakpoint;
        }
    }
    
    public synchronized JPDABreakpointEvent getCurrentBreakpointEvent() {
        return breakpointEvent;
    }

    public void setCurrentBreakpoint(JPDABreakpoint currentBreakpoint, JPDABreakpointEvent breakpointEvent) {
        JPDABreakpoint oldBreakpoint;
        synchronized (this) {
            oldBreakpoint = this.currentBreakpoint;
            this.currentBreakpoint = currentBreakpoint;
            this.breakpointEvent = breakpointEvent;
        }
        pch.firePropertyChange(JPDAThread.PROP_BREAKPOINT, oldBreakpoint, currentBreakpoint);
    }


    /**
     * Returns current state of this thread.
     *
     * @return current state of this thread
     */
    @Override
    public int getState () {
        try {
            return ThreadReferenceWrapper.status (threadReference);
        } catch (IllegalThreadStateExceptionWrapper ex) {
            // Thrown when thread has exited
        } catch (ObjectCollectedExceptionWrapper ex) {
        } catch (VMDisconnectedExceptionWrapper ex) {
        } catch (InternalExceptionWrapper ex) {
        }
        return STATE_UNKNOWN;
    }
    
    /**
     * Returns true if this thread is suspended by debugger.
     *
     * @return true if this thread is suspended by debugger
     */
    @Override
    public boolean isSuspended () {
        return suspended;
    }
    
    /**
     * Returns true if this thread is temporarily suspended by debugger to process events.
     *
     * @return true if this thread is suspended by debugger
     */
    public boolean isSuspendedNoFire () {
        return suspendedNoFire;
    }

    /**
     * Returns true if the JPDA thread is suspended by debugger.
     *
     * @return true if this thread is suspended by debugger
     */
    public boolean isThreadSuspended () {
        try {
            return ThreadReferenceWrapper.isSuspended (threadReference);
        } catch (IllegalThreadStateExceptionWrapper ex) {
            // Thrown when thread has exited
        } catch (ObjectCollectedExceptionWrapper ex) {
        } catch (InternalExceptionWrapper ex) {
        } catch (VMDisconnectedExceptionWrapper ex) {
        }
        return false;
    }
    
    /**
     * Test if this thread was suspended on an event that occurred in this thread.
     * This flag survives method invocations.
     * 
     * @return true if this thread is suspended on an event.
     */
    public boolean isSuspendedOnAnEvent() {
        return suspendedOnAnEvent;
    }

    /**
    * If this thread is suspended returns class name where this thread is stopped.
    *
    * @return class name where this thread is stopped.
    */
    @Override
    public String getClassName () {
        accessLock.readLock().lock();
        try {
            if (suspended || suspendedNoFire) {
                synchronized (cachedFramesLock) {
                    if (stackDepth < 0) {
                        stackDepth = ThreadReferenceWrapper.frameCount0(threadReference);
                    }
                    if (stackDepth < 1) return "";
                }
                return ReferenceTypeWrapper.name(LocationWrapper.declaringType(
                        StackFrameWrapper.location(ThreadReferenceWrapper.frame(threadReference, 0))));
            }
        } catch (InternalExceptionWrapper ex) {
        } catch (ObjectCollectedExceptionWrapper ex) {
        } catch (InvalidStackFrameExceptionWrapper ex) {
        } catch (IncompatibleThreadStateException ex) {
        } catch (IllegalThreadStateExceptionWrapper ex) {
            // Thrown when thread has exited
        } catch (VMDisconnectedExceptionWrapper ex) {
        } finally {
            accessLock.readLock().unlock();
        }
        return "";
    }

    /**
    * If this thread is suspended returns method name where this thread is stopped.
    *
    * @return method name where this thread is stopped.
    */
    @Override
    public String getMethodName () {
        accessLock.readLock().lock();
        try {
            if (suspended || suspendedNoFire) {
                synchronized (cachedFramesLock) {
                    if (stackDepth < 0) {
                        stackDepth = ThreadReferenceWrapper.frameCount0(threadReference);
                    }
                    if (stackDepth < 1) return "";
                }
                return TypeComponentWrapper.name(LocationWrapper.method(
                        StackFrameWrapper.location(ThreadReferenceWrapper.frame(threadReference, 0))));
            }
        } catch (InternalExceptionWrapper ex) {
        } catch (ObjectCollectedExceptionWrapper ex) {
        } catch (InvalidStackFrameExceptionWrapper ex) {
        } catch (IncompatibleThreadStateException ex) {
        } catch (IllegalThreadStateExceptionWrapper ex) {
            // Thrown when thread has exited
        } catch (VMDisconnectedExceptionWrapper ex) {
        } finally {
            accessLock.readLock().unlock();
        }
        return "";
    }
    
    /**
    * Returns name of file of this frame or null if thread has no frame.
    *
    * @return Returns name of file of this frame.
    */
    @Override
    public String getSourceName (String stratum) throws AbsentInformationException {
        accessLock.readLock().lock();
        try {
            if (suspended || suspendedNoFire) {
                synchronized (cachedFramesLock) {
                    if (stackDepth < 0) {
                        stackDepth = ThreadReferenceWrapper.frameCount0(threadReference);
                    }
                    if (stackDepth < 1) return "";
                }
                return LocationWrapper.sourceName(StackFrameWrapper.location(ThreadReferenceWrapper.frame(threadReference, 0)), stratum);
            }
        } catch (ObjectCollectedExceptionWrapper ex) {
        } catch (InvalidStackFrameExceptionWrapper ex) {
        } catch (IncompatibleThreadStateException ex) {
        } catch (IllegalThreadStateExceptionWrapper ex) {
            // Thrown when thread has exited
        } catch (VMDisconnectedExceptionWrapper ex) {
        } catch (InternalExceptionWrapper ex) {
        } finally {
            accessLock.readLock().unlock();
        }
        return "";
    }
    
    /**
    * Returns name of file of this frame or null if thread has no frame.
    *
    * @return Returns name of file of this frame.
    */
    @Override
    public String getSourcePath (String stratum) 
    throws AbsentInformationException {
        accessLock.readLock().lock();
        try {
            if (suspended || suspendedNoFire) {
                synchronized (cachedFramesLock) {
                    if (stackDepth < 0) {
                        stackDepth = ThreadReferenceWrapper.frameCount0(threadReference);
                    }
                    if (stackDepth < 1) return "";
                }
                return LocationWrapper.sourcePath(StackFrameWrapper.location(ThreadReferenceWrapper.frame(threadReference, 0)), stratum);
            }
        } catch (ObjectCollectedExceptionWrapper ex) {
        } catch (InvalidStackFrameExceptionWrapper ex) {
        } catch (IncompatibleThreadStateException ex) {
        } catch (IllegalThreadStateExceptionWrapper ex) {
            // Thrown when thread has exited
        } catch (VMDisconnectedExceptionWrapper ex) {
        } catch (InternalExceptionWrapper ex) {
        } finally {
            accessLock.readLock().unlock();
        }
        return "";
    }
    
    /**
     * Returns call stack for this thread.
     *
     * @throws AbsentInformationException if the thread is running or not able
     *         to return callstack. If the thread is in an incompatible state
     *         (e.g. running), the AbsentInformationException has
     *         IncompatibleThreadStateException as a cause.
     *         If the thread is collected, the AbsentInformationException has
     *         ObjectCollectedException as a cause.
     * @return call stack
     */
    @Override
    public CallStackFrame[] getCallStack () throws AbsentInformationException {
        accessLock.readLock().lock();
        try {
            return getCallStack (0, getStackDepth ());
        } finally {
            accessLock.readLock().unlock();
        }
    }
    
    /**
     * Returns call stack for this thread on the given indexes.
     *
     * @param from a from index, inclusive
     * @param to a to index, exclusive
     * @throws AbsentInformationException if the thread is running or not able
     *         to return callstack. If the thread is in an incompatible state
     *         (e.g. running), the AbsentInformationException has
     *         IncompatibleThreadStateException as a cause.
     *         If the thread is collected, the AbsentInformationException has
     *         ObjectCollectedException as a cause.
     * @return call stack
     */
    @Override
    public CallStackFrame[] getCallStack (int from, int to) 
    throws AbsentInformationException {
        accessLock.readLock().lock();
        try {
            if (!(suspended || suspendedNoFire)) {
                return new CallStackFrame[0];
            }
            List l;
            CallStackFrame[] theCachedFrames = null;
                int max;
                synchronized (cachedFramesLock) {
                    if (stackDepth < 0) {
                        stackDepth = ThreadReferenceWrapper.frameCount(threadReference);
                    }
                    max = stackDepth;
                }
                if (to < 0) to = max; // Fight strange negative frame counts from http://www.netbeans.org/issues/show_bug.cgi?id=162448
                from = Math.min(from, max);
                to = Math.min(to, max);
                if (to - from > 1) {  /*TODO: Frame caching cause problems with invalid frames. Some fix is necessary...
                 *  as a workaround, frames caching is disabled.*/
                    synchronized (cachedFramesLock) {
                        if (from == cachedFramesFrom && to == cachedFramesTo) {
                            return cachedFrames;
                        }
                        if (from >= cachedFramesFrom && to <= cachedFramesTo) {
                            // TODO: Arrays.copyOfRange(cachedFrames, from - cachedFramesFrom, to);
                            return copyOfRange(cachedFrames, from - cachedFramesFrom, to - cachedFramesFrom);
                        }
                        if (cachedFramesFrom >= 0 && cachedFramesTo > cachedFramesFrom) {
                            int length = to - from;
                            theCachedFrames = new CallStackFrame[length];
                            for (int i = 0; i < length; i++) {
                                if (i >= cachedFramesFrom && i < cachedFramesTo) {
                                    theCachedFrames[i] = cachedFrames[i - cachedFramesFrom];
                                } else {
                                    theCachedFrames[i] = null;
                                }
                            }
                        }
                    }
                }
                if (from < 0) {
                    throw new IndexOutOfBoundsException("from = "+from);
                }
                if (from == to) {
                    return new CallStackFrame[0];
                }
                if (from >= max) {
                    throw new IndexOutOfBoundsException("from = "+from+" is too high, frame count = "+max);
                }
                int length = to - from;
                if (length < 0 || (from+length) > max) {
                    throw new IndexOutOfBoundsException("from = "+from+", to = "+to+", frame count = "+max);
                }
                l = null;
                try {
                    l = ThreadReferenceWrapper.frames (threadReference, from, length);
                } catch (IndexOutOfBoundsException ioobex) {
                    ioobex = Exceptions.attachMessage(ioobex, "from = "+from+", to = "+to+", frame count = "+max+", length = "+length+", fresh frame count = "+ThreadReferenceWrapper.frameCount(threadReference));
                    // Terrible attempt to hack a magic issue
                    while (length > 0) {
                        // Try to obtain at least something...
                        length--;
                        to--;
                        try {
                            l = ThreadReferenceWrapper.frames (threadReference, from, length);
                            break;
                        } catch (IndexOutOfBoundsException ioobex2) {
                        }
                    }
                    ioobex = Exceptions.attachMessage(ioobex, "Finally got "+length+" frames from "+threadReference);
                    logger.log(Level.INFO, "Stack frames "+to+" - "+max+" can not be retrieved from thread "+threadReference, ioobex);
                }
                if (l == null) {
                    l = java.util.Collections.emptyList();
                }
            int n = l.size();
            CallStackFrame[] frames = new CallStackFrame[n];
            for (int i = 0; i < n; i++) {
                if (theCachedFrames != null && theCachedFrames[i] != null) {
                    frames[i] = theCachedFrames[i];
                } else {
                    frames[i] = new CallStackFrameImpl(this, (StackFrame) l.get(i), from + i, debugger);
                }
                if (from == 0 && i == 0 && currentOperation != null) {
                    ((CallStackFrameImpl) frames[i]).setCurrentOperation(currentOperation);
                }
            }
            //if (to - from > 1) {
                synchronized (cachedFramesLock) {
                    cachedFrames = frames;
                    cachedFramesFrom = from;
                    cachedFramesTo = to;
                }
            //}
            return frames;
        } catch (IncompatibleThreadStateException ex) {
            String msg = ex.getLocalizedMessage() + " " + getThreadStateLog();
            AbsentInformationException aiex = new AbsentInformationException(msg);
            aiex.initCause(ex);
            throw aiex;
        } catch (InvalidStackFrameException ex) {
            AbsentInformationException aiex = new AbsentInformationException(ex.getLocalizedMessage());
            aiex.initCause(ex);
            throw aiex;
        } catch (InvalidStackFrameExceptionWrapper ex) {
            AbsentInformationException aiex = new AbsentInformationException(ex.getLocalizedMessage());
            aiex.initCause(ex);
            throw aiex;
        } catch (ObjectCollectedExceptionWrapper ocex) {
            AbsentInformationException aiex = new AbsentInformationException(ocex.getLocalizedMessage());
            aiex.initCause(ocex);
            throw aiex;
        } catch (IllegalThreadStateExceptionWrapper itsex) {
            // Thrown when thread has exited
            AbsentInformationException aiex = new AbsentInformationException(itsex.getLocalizedMessage());
            aiex.initCause(itsex);
            throw aiex;
        } catch (InternalExceptionWrapper ex) {
            return new CallStackFrame [0];
        } catch (VMDisconnectedExceptionWrapper ex) {
            return new CallStackFrame [0];
        } finally {
            accessLock.readLock().unlock();
        }
    }
    
    private static CallStackFrame[] copyOfRange(CallStackFrame[] original, int from, int to) {
        // TODO: Use Arrays.copyOfRange(cachedFrames, from, to);
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        CallStackFrame[] copy = new CallStackFrame[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }

    private void cleanCachedFrames() {
        synchronized (cachedFramesLock) {
            stackDepth = -1;
            cachedFrames = null;
            cachedFramesFrom = -1;
            cachedFramesTo = -1;
        }
    }
    
    /**
     * Returns length of current call stack.
     *
     * @return length of current call stack
     */
    @Override
    public int getStackDepth () {
        accessLock.readLock().lock();
        try {
            if (suspended || suspendedNoFire) {
                synchronized (cachedFramesLock) {
                    if (stackDepth < 0) {
                        stackDepth = ThreadReferenceWrapper.frameCount0(threadReference);
                    }
                    return stackDepth;
                }
            }
        } catch (IllegalThreadStateExceptionWrapper ex) {
            // Thrown when thread has exited
        } catch (IncompatibleThreadStateException e) {
        } catch (InvalidStackFrameExceptionWrapper e) {
        } finally {
            accessLock.readLock().unlock();
        }
        return 0;
    }
    
    public void popFrames(StackFrame sf) throws IncompatibleThreadStateException {
        try {
            notifyToBeResumed();
            accessLock.writeLock().lock();
            try {
                ThreadReferenceWrapper.popFrames(threadReference, sf);
            } finally {
                accessLock.writeLock().unlock();
            }
            cleanCachedFrames();
            setReturnVariable(null); // Clear the return var
        } catch (IllegalThreadStateExceptionWrapper ex) {
            throw new IncompatibleThreadStateException("Thread exited.");
        } catch (InvalidStackFrameExceptionWrapper ex) {
            Exceptions.printStackTrace(ex);
        } catch (ObjectCollectedExceptionWrapper ex) {
            throw new IncompatibleThreadStateException("Thread died.");
        } catch (NativeMethodExceptionWrapper nmex) {
            cleanCachedFrames();
            Exceptions.printStackTrace(
                    Exceptions.attachLocalizedMessage(nmex,
                        NbBundle.getMessage(JPDAThreadImpl.class, "MSG_NativeMethodPop")));
        } catch (InternalExceptionWrapper iex) {
            cleanCachedFrames();
        } catch (VMDisconnectedExceptionWrapper ex) {
            // Ignore
        } finally {
            notifySuspended();
        }
    }
    
    /**
     * Suspends thread.
     */
    @Override
    public void suspend () {
        logger.fine("JPDAThreadImpl.suspend() called.");
        Boolean suspendedToFire = null;
        boolean isCurrent = debugger.getCurrentThread() == this;
        accessLock.writeLock().lock();
        try {
            logger.fine("  write lock acquired, is suspended = "+suspended+", suspendedNoFire = "+suspendedNoFire);
            if (!isSuspended ()) {
                if (suspendedNoFire) {
                    loggerS.fine("["+threadName+"]: suspend(): SETTING suspendRequested = "+true);
                    // We were suspended just to process something, thus we do not want to be resumed then
                    suspendRequested = true;
                    return ;
                }
                logger.fine("Suspending thread "+threadName);
                ThreadReferenceWrapper.suspend (threadReference);
                suspendedToFire = Boolean.TRUE;
                suspendCount++;
                threadName = ThreadReferenceWrapper.name(threadReference);
            }
            //System.err.println("suspend("+getName()+") suspended = true");
            suspended = true;
            initiallySuspended = false;
        } catch (IllegalThreadStateExceptionWrapper ex) {
            // Thrown when thread has exited
        } catch (InternalExceptionWrapper ex) {
        } catch (ObjectCollectedExceptionWrapper ex) {
        } catch (VMDisconnectedExceptionWrapper ex) {
        } finally {
            accessLock.writeLock().unlock();
        }
        if (isCurrent) {
            debugger.setStoppedState(threadReference, false);
        }
        if (suspendedToFire != null) {
            pch.firePropertyChange(JPDAThread.PROP_SUSPENDED,
                    Boolean.valueOf(!suspendedToFire.booleanValue()),
                    suspendedToFire);
        }
    }
    
    /**
     * Unsuspends thread.
     */
    @Override
    public void resume () {
        boolean can = cleanBeforeResume();
        if (can) {
            try {
                resumeAfterClean();
                setAsResumed(false);
            } catch (InternalExceptionWrapper ex) {
            } catch (VMDisconnectedExceptionWrapper ex) {
            } finally {
                fireAfterResume();
            }
        }
        /* Original code split among 4 methods:
        if (this == debugger.getCurrentThread()) {
            boolean can = debugger.currentThreadToBeResumed();
            if (!can) return ;
        }
        Boolean suspendedToFire = null;
        accessLock.writeLock().lock();
        try {
            waitUntilMethodInvokeDone();
            setReturnVariable(null); // Clear the return var on resume
            setCurrentOperation(null);
            currentBreakpoint = null;
            if (!doKeepLastOperations) {
                clearLastOperations();
            }
            try {
                if (isSuspended ()) {
                    logger.fine("Resuming thread "+threadName);
                    int count = ThreadReferenceWrapper.suspendCount (threadReference);
                    while (count > 0) {
                        ThreadReferenceWrapper.resume (threadReference); count--;
                    }
                    suspendedToFire = Boolean.FALSE;
                }
                suspendCount = 0;
                //System.err.println("resume("+getName()+") suspended = false");
                suspended = false;
                methodInvokingDisabledUntilResumed = false;
            } catch (IllegalThreadStateExceptionWrapper ex) {
                // Thrown when thread has exited
            } catch (ObjectCollectedExceptionWrapper ex) {
            } catch (VMDisconnectedExceptionWrapper ex) {
            } catch (InternalExceptionWrapper ex) {
            }
        } finally {
            accessLock.writeLock().unlock();
        }
        JPDABreakpoint brkp = null;
        synchronized (stepBreakpointLock) {
            if (stepSuspendedByBreakpoint != null) {
                brkp = stepSuspendedByBreakpoint;
            }
        }
        if (brkp != null) {
            pch.firePropertyChange(PROP_STEP_SUSPENDED_BY_BREAKPOINT, brkp, null);
        }
        cleanCachedFrames();
        if (suspendedToFire != null) {
            pch.firePropertyChange(PROP_SUSPENDED,
                    Boolean.valueOf(!suspendedToFire.booleanValue()),
                    suspendedToFire);
        }
         */
    }

    private List<PropertyChangeEvent> resumeChangeEvents;

    /**
     * Acquires necessary locks and cleans the thread state before resume.
     * This method is expected to be followed by {@link #resumeAfterClean()} and {@link #fireAfterResume()}.
     * This method MUST be followed by {@link #fireAfterResume()} even if it fails with an exception or error
     * @return <code>true</code> if caller can procceed with {@link #resumeAfterClean()},
     *         <code>false</code> when the resume should be abandoned.
     */
    public boolean cleanBeforeResume() {
        if (this == debugger.getCurrentThread()) {
            boolean can = debugger.currentThreadToBeResumed();
            if (!can) {
                return false;
            }
        }
        accessLock.writeLock().lock();
        waitUntilMethodInvokeDone();
        setReturnVariable(null); // Clear the return var on resume
        setCurrentOperation(null);
        synchronized (this) {
            currentBreakpoint = null;
            breakpointEvent = null;
        }
        if (!doKeepLastOperations) {
            clearLastOperations();
        }
        cleanCachedFrames();
        JPDABreakpoint brkp = removeSuspendedByBreakpoint();
        PropertyChangeEvent suspEvt = new PropertyChangeEvent(this, JPDAThread.PROP_SUSPENDED, true, false);
        if (brkp != null) {
            PropertyChangeEvent brkpEvt = new PropertyChangeEvent(this, PROP_STEP_SUSPENDED_BY_BREAKPOINT,
                    brkp,
                    null);
            if (isSuspended()) {
                resumeChangeEvents = Arrays.asList(new PropertyChangeEvent[] {brkpEvt, suspEvt});
            } else {
                resumeChangeEvents = Collections.singletonList(brkpEvt);
            }
        } else {
            if (isSuspended()) {
                resumeChangeEvents = Collections.singletonList(suspEvt);
            } else {
                resumeChangeEvents = Collections.emptyList();
            }
        }
        return true;
    }

    public void resumeAfterClean() throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        logger.fine("Resuming thread "+threadName);
        boolean resumed = false;
        try {
            int count = ThreadReferenceWrapper.suspendCount (threadReference);
            while (count > 0) {
                ThreadReferenceWrapper.resume (threadReference); count--;
            }
            resumed = true;
        } catch (IllegalThreadStateExceptionWrapper ex) {
            // Thrown when thread has exited
        } catch (ObjectCollectedExceptionWrapper ex) {
        } finally {
            if (!resumed) {
                // Do not fire PROP_SUSPENDED when not resumed!
                for (PropertyChangeEvent pchEvt : resumeChangeEvents) {
                    if (JPDAThread.PROP_SUSPENDED.equals(pchEvt.getPropertyName())) {
                        resumeChangeEvents = new ArrayList<PropertyChangeEvent>(resumeChangeEvents);
                        resumeChangeEvents.remove(pchEvt);
                        break;
                    }
                }
            }
        }
    }

    /** Must be called between {@link #cleanBeforeResume()} and {@link #fireAfterResume()} only. */
    public boolean reduceThreadSuspendCount() throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        boolean reduced = true;
        try {
            int count = ThreadReferenceWrapper.suspendCount (threadReference);
            reduced = count == 1;
            //if (!reduced) logger.severe("Reducing suspend count of existing "+getThreadStateLog());
            while (count > 1) {
                ThreadReferenceWrapper.resume (threadReference); count--;
            }
            reduced = true;
        } catch (IllegalThreadStateExceptionWrapper ex) {
            // Thrown when thread has exited
        } catch (ObjectCollectedExceptionWrapper ex) {
        } finally {
            if (!reduced) {
                // Do not fire PROP_SUSPENDED when not resumed!
                for (PropertyChangeEvent pchEvt : resumeChangeEvents) {
                    if (JPDAThread.PROP_SUSPENDED.equals(pchEvt.getPropertyName())) {
                        resumeChangeEvents = new ArrayList<PropertyChangeEvent>(resumeChangeEvents);
                        resumeChangeEvents.remove(pchEvt);
                        break;
                    }
                }
            }
        }
        return reduced;
    }

    public void setAsResumed(boolean reduceSuspendCountOnly) {
        if (reduceSuspendCountOnly) {
            suspendCount--;
        } else {
            suspendCount = 0;
        }
        if (suspendCount == 0) {
            //System.err.println("resume("+getName()+") suspended = false");
            suspended = false;
            suspendedOnAnEvent = false;
            suspendedNoFire = false;
            debugger.setCurrentSuspendedNoFireThread(null);
            methodInvokingDisabledUntilResumed = false;
        }
    }

    public void fireAfterResume() {
        List<PropertyChangeEvent> evts = resumeChangeEvents;
        resumeChangeEvents = null;
        boolean fire = suspendCount == 0;
        accessLock.writeLock().unlock();
        if (fire) {
            for (PropertyChangeEvent evt : evts) {
                pch.firePropertyChange(evt);
            }
        }
    }
    
    public void notifyToBeResumed() {
        //System.err.println("notifyToBeResumed("+getName()+")");
        List<PropertyChangeEvent> evts = notifyToBeRunning(true, true);
        for (PropertyChangeEvent evt : evts) {
            pch.firePropertyChange(evt);
        }
    }
    
    /**
     * Call only after {@link #notifyToBeResumedNoFire()} to fire the change events.
     */
    public void fireAfterNotifyToBeResumedNoFire() {
        List<PropertyChangeEvent> evts = resumeChangeEvents;
        resumeChangeEvents = null;
        if (evts != null) {
            for (PropertyChangeEvent evt : evts) {
                pch.firePropertyChange(evt);
            }
        }
    }
    
    public boolean notifyToBeResumedNoFire() {
        //System.err.println("notifyToBeResumed("+getName()+")");
        logger.fine("notifyToBeResumedNoFire("+getName()+")");
        accessLock.writeLock().lock();
        loggerS.fine("["+threadName+"]: "+"notifyToBeResumedNoFire() suspended = "+suspended+", suspendRequested = "+suspendRequested);
        try {
            logger.fine("   suspendRequested = "+suspendRequested);
            if (suspendRequested) {
                suspendRequested = false;
                return false;
            }
            resumeChangeEvents = notifyToBeRunning(true, true);
        } finally {
            accessLock.writeLock().unlock();
        }
        return true;
    }

    private List<PropertyChangeEvent> notifyToBeRunning(boolean clearVars, boolean resumed) {
        if (resumed) {
            // Reset the pending action when the thread is resumed.
            setPendingAction(null);
        }
        Boolean suspendedToFire = null;
        accessLock.writeLock().lock();
        try {
            if (resumed) {
                waitUntilMethodInvokeDone();
            }
            //System.err.println("notifyToBeRunning("+getName()+"), resumed = "+resumed+", suspendCount = "+suspendCount+", thread's suspendCount = "+threadReference.suspendCount());
            if (resumed && (--suspendCount > 0)) return Collections.emptyList();
            //System.err.println("  suspendCount = 0, var suspended = "+suspended);
            suspendCount = 0;
            if (clearVars) {
                setCurrentOperation(null);
                setReturnVariable(null); // Clear the return var on resume
                currentBreakpoint = null;
                if (!doKeepLastOperations) {
                    clearLastOperations();
                }
            }
            if (suspended) {
                //System.err.println("notifyToBeRunning("+getName()+") suspended = false");
                suspended = false;
                suspendedToFire = Boolean.FALSE;
                methodInvokingDisabledUntilResumed = false;
            }
            if (resumed) {
                suspendedNoFire = false;
                suspendedOnAnEvent = false;
                debugger.setCurrentSuspendedNoFireThread(null);
            }
        } finally {
            accessLock.writeLock().unlock();
        }
        cleanCachedFrames();
        PropertyChangeEvent stepBrkpEvt = null;
        JPDABreakpoint stepBrkp = removeSuspendedByBreakpoint();
        if (stepBrkp != null) {
            stepBrkpEvt = new PropertyChangeEvent(this, PROP_STEP_SUSPENDED_BY_BREAKPOINT,
                                                  stepBrkp, null);
        }
        if (suspendedToFire != null) {
            PropertyChangeEvent suspEvt = new PropertyChangeEvent(this, JPDAThread.PROP_SUSPENDED,
                    Boolean.valueOf(!suspendedToFire.booleanValue()),
                    suspendedToFire);
            if (!resumed) suspEvt.setPropagationId("methodInvoke"); // NOI18N
            if (stepBrkpEvt != null) {
                return Arrays.asList(new PropertyChangeEvent[] {stepBrkpEvt, suspEvt});
            } else {
                return Collections.singletonList(suspEvt);
            }
        } else {
            if (stepBrkpEvt != null) {
                return Collections.singletonList(stepBrkpEvt);
            }
            return Collections.emptyList();
        }
    }
    
    public void updateSuspendCount(int suspendCount) {
        accessLock.writeLock().lock();
        try {
            this.suspendCount = suspendCount;
        } finally {
            accessLock.writeLock().unlock();
        }
    }
    
    public void notifySuspended() {
        notifySuspended(true, false);
    }

    public void notifySuspendedNoFire(boolean eventInThisThread, boolean threadDied) {
        //notifySuspended(false);
        // Keep the thread look like running until we get a firing notification
        accessLock.writeLock().lock();
        try {
            if (eventInThisThread) {    // Do not reset to false when we do not know.
                suspendedOnAnEvent = eventInThisThread;
            }
            if (loggerS.isLoggable(Level.FINE)) {
                loggerS.fine("["+threadName+"]: "+"notifySuspendedNoFire() suspended = "+suspended+", suspendCount = "+suspendCount+", suspendedOnAnEvent = "+eventInThisThread);
            }
            if (suspended && suspendCount > 0 && !initiallySuspended) {
                if (loggerS.isLoggable(Level.FINE)) {
                    loggerS.fine("["+threadName+"]: notifySuspendedNoFire(): SETTING suspendRequested = "+true);
                }
                suspendRequested = true; // The thread was just suspended, leave it suspended afterwards.
            }
            if (!threadDied) {
                try {
                    suspendCount = ThreadReferenceWrapper.suspendCount(threadReference);
                    threadName = ThreadReferenceWrapper.name(threadReference);
                } catch (IllegalThreadStateExceptionWrapper ex) {
                    // Thrown when thread has exited
                } catch (ObjectCollectedExceptionWrapper ocex) {
                    // The thread is gone
                } catch (VMDisconnectedExceptionWrapper ex) {
                    // The VM is gone
                } catch (InternalExceptionWrapper ex) {
                    // Something is gone
                }
            } else {
                suspendCount = 1; // Suppose
            }
            suspendedNoFire = true;
            debugger.setCurrentSuspendedNoFireThread(this);
            if (loggerS.isLoggable(Level.FINE)) {
                loggerS.fine("["+threadName+"]: (notifySuspendedNoFire() END) suspended = "+suspended+", suspendedNoFire = "+suspendedNoFire+", suspendRequested = "+suspendRequested);
            }
        } finally {
            accessLock.writeLock().unlock();
        }
    }

    public PropertyChangeEvent notifySuspended(boolean doFire, boolean explicitelyPaused) {
        if (loggerS.isLoggable(Level.FINE)) {
            loggerS.fine("["+threadName+"]: "+"notifySuspended(doFire = "+doFire+", explicitelyPaused = "+explicitelyPaused+")");
        }
        Boolean suspendedToFire = null;
        accessLock.writeLock().lock();
        initiallySuspended = false;
        try {
            if (loggerS.isLoggable(Level.FINE)) {
                loggerS.fine("["+threadName+"]: (notifySuspended() BEGIN) suspended = "+suspended+", suspendedNoFire = "+suspendedNoFire);
            }
            if (explicitelyPaused && !suspended && suspendedNoFire) {
                suspendRequested = true;
                if (loggerS.isLoggable(Level.FINE)) {
                    loggerS.fine("["+threadName+"]: suspendRequested = "+suspendRequested);
                }
                return null;
            }
            try {
                suspendCount = ThreadReferenceWrapper.suspendCount(threadReference);
                threadName = ThreadReferenceWrapper.name(threadReference);
            } catch (IllegalThreadStateExceptionWrapper ex) {
                return null; // Thrown when thread has exited
            } catch (ObjectCollectedExceptionWrapper ocex) {
                return null; // The thread is gone
            } catch (VMDisconnectedExceptionWrapper ex) {
                return null; // The VM is gone
            } catch (InternalExceptionWrapper ex) {
                return null; // Something is gone
            }
            //System.err.println("notifySuspended("+getName()+") suspendCount = "+suspendCount+", var suspended = "+suspended);
            suspendedNoFire = false;
            debugger.setCurrentSuspendedNoFireThread(null);
            if ((!suspended || suspendedNoFire && doFire) && isThreadSuspended()) {
                //System.err.println("  setting suspended = true");
                suspended = true;
                suspendedToFire = Boolean.TRUE;
                if (doFire) {
                    try {
                        threadName = ThreadReferenceWrapper.name(threadReference);
                    } catch (InternalExceptionWrapper ex) {
                    } catch (VMDisconnectedExceptionWrapper ex) {
                    } catch (ObjectCollectedExceptionWrapper ex) {
                    } catch (IllegalThreadStateExceptionWrapper ex) {
                    }
                }
            }
            if (loggerS.isLoggable(Level.FINE)) {
                loggerS.fine("["+threadName+"]: (notifySuspended() END) suspended = "+suspended+", suspendedNoFire = "+suspendedNoFire+", suspendRequested = "+suspendRequested);
            }
        } finally {
            accessLock.writeLock().unlock();
        }
        if (doFire && suspendedToFire != null) {
            pch.firePropertyChange(JPDAThread.PROP_SUSPENDED,
                    Boolean.valueOf(!suspendedToFire.booleanValue()),
                    suspendedToFire);
        } else if (suspendedToFire != null) {
            return new PropertyChangeEvent(this, JPDAThread.PROP_SUSPENDED,
                    Boolean.valueOf(!suspendedToFire.booleanValue()),
                    suspendedToFire);
        }
        return null;
    }

    /**
     * Call ONLY with events obtained from {@link #notifySuspended()} or other
     * methods that return event(s).
     * @param event The event to fire.
     */
    public void fireEvent(PropertyChangeEvent event) {
        pch.firePropertyChange(event);
    }

    private SingleThreadWatcher watcher = null;
    
    private boolean methodInvoking;
    private boolean methodInvokingDisabledUntilResumed;
    private boolean resumedToFinishMethodInvocation;
    private boolean unsuspendedStateWhenInvoking;
    private List<StepRequest> stepsDeletedDuringMethodInvoke;
    
    public void notifyMethodInvoking() throws PropertyVetoException {
        SingleThreadWatcher watcherToDestroy = null;
        List<PropertyChangeEvent> evts;
        accessLock.writeLock().lock();
        try {
            logger.log(Level.FINE, "Invoking a method in thread {0}", threadName);
            if (loggerS.isLoggable(Level.FINE)) {
                loggerS.fine("["+threadName+"]: Invoking a method, suspended = "+suspended+", suspendedNoFire = "+suspendedNoFire+", suspendRequested = "+suspendRequested);
            }
            if (methodInvokingDisabledUntilResumed) {
                throw new PropertyVetoException(
                        NbBundle.getMessage(JPDAThreadImpl.class, "MSG_DisabledUntilResumed"), null);
            }
            if (methodInvoking) {
                throw new PropertyVetoException(
                        NbBundle.getMessage(JPDAThreadImpl.class, "MSG_AlreadyInvoking"), null);
            }
            if (!(suspended || suspendedNoFire)) {
                throw new PropertyVetoException(
                        NbBundle.getMessage(JPDAThreadImpl.class, "MSG_NoCurrentContext"), null);
            }
            if (loggerS.isLoggable(Level.FINE)) {
                loggerS.fine("Suspend count of "+this+" before notifyMethodInvoking() is: "+suspendCount);
            }
            try {
                int tsc = ThreadReferenceWrapper.suspendCount(threadReference);
                if (suspendCount != tsc) {
                    // Should not occur
                    logger.log(Level.INFO, getThreadStateLog(), new IllegalStateException(
                            "Different suspend counts! JPDA Thread = "+suspendCount+
                            ", thread "+threadReference+" = "+tsc+
                            ", thread's state: "+getThreadStateLog(threadReference)));
                    suspendCount = tsc;
                }
                // The thread needs to be only single-suspended, otherwise it can not invoke methods.
                for (int sc = 1; sc < suspendCount; sc++) {
                    ThreadReferenceWrapper.resume(threadReference);
                }
            } catch (InternalExceptionWrapper iew) {
            } catch (VMDisconnectedExceptionWrapper dew) {
            } catch (VMDisconnectedException de) { // from ThreadReference.toString()
            } catch (ObjectCollectedExceptionWrapper oce) {
            } catch (IllegalThreadStateExceptionWrapper itse) {
            }
            debugger.getOperator().notifyMethodInvoking(threadReference);
            if (vm != null) {
                // Check if there aren't any steps submitted, which would break method invocation:
                try {
                    EventRequestManager erm = VirtualMachineWrapper.eventRequestManager(vm);
                    List<StepRequest> srs = EventRequestManagerWrapper.stepRequests0(erm);
                    List<StepRequest> stepsToDelete = null;
                    for (StepRequest sr : srs) {
                        ThreadReference t = StepRequestWrapper.thread(sr);
                        if (threadReference.equals(t)) {
                            if (stepsToDelete == null) {
                                stepsToDelete = new ArrayList<StepRequest>();
                            }
                            stepsToDelete.add(sr);
                        }
                    }
                    if (stepsToDelete != null) {
                        for (StepRequest sr : stepsToDelete) {
                            //debugger.getOperator().unregister(sr);
                            //EventRequestManagerWrapper.deleteEventRequest(erm, sr);
                            boolean wasEnabled = EventRequestWrapper.isEnabled0(sr);
                            EventRequestWrapper.putProperty(sr, "methodInvoking.wasEnabled", wasEnabled);
                            EventRequestWrapper.disable(sr);
                            if (logger.isLoggable(Level.FINE)) logger.fine("DISABLED Step Request: "+sr);
                        }
                    }
                    stepsDeletedDuringMethodInvoke = stepsToDelete;
                } catch (InternalExceptionWrapper iew) {
                } catch (VMDisconnectedExceptionWrapper dew) {
                } catch (InvalidRequestStateExceptionWrapper irse) {
                } catch (ObjectCollectedExceptionWrapper oce) {}
            }
            methodInvoking = true;
            unsuspendedStateWhenInvoking = !isSuspended();
            if (unsuspendedStateWhenInvoking) {
                // Do not notify running state when was not suspended.
                evts = Collections.emptyList();
            } else {
                evts = notifyToBeRunning(false, false);
            }
            watcherToDestroy = watcher;
            watcher = new SingleThreadWatcher(this);
            //logger.severe("Before method invoke: "+getThreadStateLog());
        } finally {
            if (loggerS.isLoggable(Level.FINE)) {
                loggerS.fine("["+threadName+"]: unsuspendedStateWhenInvoking = "+unsuspendedStateWhenInvoking);
            }
            accessLock.writeLock().unlock();
        }
        if (watcherToDestroy != null) {
            watcherToDestroy.destroy();
        }
        for (PropertyChangeEvent evt : evts) {
            pch.firePropertyChange(evt);
        }
        synchronized (suspendToCheckForMonitorsLock) {
            canSuspendToCheckForMonitors = true;
        }
    }
    
    public void notifyMethodInvokeDone() {
        synchronized (suspendToCheckForMonitorsLock) {
            canSuspendToCheckForMonitors = false;
        }
        SingleThreadWatcher watcherToDestroy = null;
        boolean wasUnsuspendedStateWhenInvoking;
        accessLock.writeLock().lock();
        try {
            logger.log(Level.FINE, "Method invoke done in thread {0}", threadName);
            if (loggerS.isLoggable(Level.FINE)) {
                loggerS.fine("["+threadName+"]: Method invoke done, suspended = "+suspended+", suspendedNoFire = "+suspendedNoFire+", suspendRequested = "+suspendRequested+", unsuspendedStateWhenInvoking = "+unsuspendedStateWhenInvoking);
            }
            try {
                if (resumedToFinishMethodInvocation) {
                    // HACK becuase of JDI, we've resumed this thread so that method invocation can be finished.
                    // We need to suspend the thread immediately so that it does not continue after the invoke has finished.
                    ThreadReferenceWrapper.suspend(threadReference);
                    //System.err.println("\""+getName()+"\""+":  Suspended after method invocation.");
                    resumedToFinishMethodInvocation = false;
                }
                for (int sc = 1; sc < suspendCount; sc++) {
                    ThreadReferenceWrapper.suspend(threadReference);
                }
            } catch (InternalExceptionWrapper ex) {
            } catch (VMDisconnectedExceptionWrapper ex) {
            } catch (ObjectCollectedExceptionWrapper ex) {
            } catch (IllegalThreadStateExceptionWrapper ex) {
            }
            debugger.getOperator().notifyMethodInvokeDone(threadReference);
            if (stepsDeletedDuringMethodInvoke != null) {
                try {
                    for (StepRequest sr : stepsDeletedDuringMethodInvoke) {
                        try {
                            Object wasEnabled = EventRequestWrapper.getProperty(sr, "methodInvoking.wasEnabled");
                            if (Boolean.TRUE.equals(wasEnabled)) {
                                EventRequestWrapper.enable(sr);
                            }
                        } catch (ObjectCollectedExceptionWrapper ex) {
                            continue;
                        } catch (InvalidRequestStateExceptionWrapper irse) {
                            continue;
                        }
                        if (logger.isLoggable(Level.FINE)) logger.fine("ENABLED Step Request: "+sr);
                    }
                } catch (InternalExceptionWrapper iew) {
                } catch (VMDisconnectedExceptionWrapper dew) {}
                stepsDeletedDuringMethodInvoke = null;
            }
            methodInvoking = false;
            wasUnsuspendedStateWhenInvoking = unsuspendedStateWhenInvoking;
            unsuspendedStateWhenInvoking = false;
            //logger.severe("After method invoke: "+getThreadStateLog());
            synchronized (this) {
                this.notifyAll();
            }
            watcherToDestroy = watcher;
            watcher = null;
        } finally {
            accessLock.writeLock().unlock();
        }
        if (watcherToDestroy != null) {
            watcherToDestroy.destroy();
        }
        // Do not notify suspended state when was already unsuspended when started invoking.
        if (!wasUnsuspendedStateWhenInvoking) {
            PropertyChangeEvent evt = notifySuspended(false, false);
            if (evt != null) {
                evt.setPropagationId("methodInvoke"); // NOI18N
                pch.firePropertyChange(evt);
            }
        }
    }
    
    public boolean isMethodInvoking() {
        return methodInvoking;
    }
    
    public void waitUntilMethodInvokeDone() {
        accessLock.readLock().lock();
        try {
            while (methodInvoking) {
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException iex) {
                        break;
                    }
                }
            }
        } finally {
            accessLock.readLock().unlock();
        }
    }
    
    public void waitUntilMethodInvokeDone(long timeout) throws InterruptedException {
        if (!accessLock.readLock().tryLock(timeout, TimeUnit.MILLISECONDS)) {
            return ;
        }
        try {
            while (methodInvoking) {
                synchronized (this) {
                    this.wait(timeout);
                }
            }
        } finally {
            accessLock.readLock().unlock();
        }
    }

    public void disableMethodInvokeUntilResumed() {
        accessLock.writeLock().lock();
        methodInvokingDisabledUntilResumed = true;
        accessLock.writeLock().unlock();
    }

    private boolean inStep = false;

    public void setInStep(boolean inStep, EventRequest stepRequest) {
        SingleThreadWatcher watcherToDestroy = null;
        this.inStep = inStep;
        watcherToDestroy = watcher;
        if (inStep) {
            boolean suspendThread;
            try {
                suspendThread = EventRequestWrapper.suspendPolicy(stepRequest) == StepRequest.SUSPEND_EVENT_THREAD;
            } catch (InternalExceptionWrapper ex) {
                suspendThread = false;
            } catch (VMDisconnectedExceptionWrapper ex) {
                suspendThread = false;
            }
            if (suspendThread) {
                watcher = new SingleThreadWatcher(this);
            }
        } else {
            watcher = null;
        }
        if (watcherToDestroy != null) {
            watcherToDestroy.destroy();
        }
    }

    public boolean isInStep() {
        return inStep;
    }

    @Override
    public void interrupt() {
        try {
            if (isSuspended ()) return;
            ThreadReferenceWrapper.interrupt(threadReference);
        } catch (IllegalThreadStateExceptionWrapper ex) {
            // Thrown when thread has exited
        } catch (ObjectCollectedExceptionWrapper ex) {
        } catch (InternalExceptionWrapper ex) {
        } catch (VMDisconnectedExceptionWrapper ex) {
        }
    }
    
    /**
     * Sets this thread current.
     *
     * @see JPDADebugger#getCurrentThread
     */
    @Override
    public void makeCurrent () {
        if (Mutex.EVENT.isReadAccess()) {
            debugger.getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    doMakeCurrent();
                }
            });
        } else {
            doMakeCurrent();
        }
    }

    private void doMakeCurrent() {
        debugger.setCurrentThread (this);
        Session session = debugger.getSession();
        DebuggerManager manager = DebuggerManager.getDebuggerManager();
        if (session != manager.getCurrentSession()) {
            manager.setCurrentSession(session);
        }
    }
    
    /**
     * Returns monitor this thread is waiting on.
     *
     * @return monitor this thread is waiting on
     */
    @Override
    public ObjectVariable getContendedMonitor () {
        if (!VirtualMachineWrapper.canGetCurrentContendedMonitor0(vm)) {
            return null;
        }
        try {
            ObjectReference or;
            accessLock.readLock().lock();
            try {
                if (!(isSuspended() || suspendedNoFire)) return null;
                try {
                    if ("DestroyJavaVM".equals(threadName)) {
                        // See defect #6474293
                        return null;
                    }
                    or = ThreadReferenceWrapper.currentContendedMonitor(threadReference);
                } catch (IllegalThreadStateExceptionWrapper ex) {
                    // Thrown when thread has exited
                    return null;
                } catch (IncompatibleThreadStateException e) {
                    Logger.getLogger(JPDAThreadImpl.class.getName()).log(Level.INFO, getThreadStateLog(), e);
                    return null;
                } catch (com.sun.jdi.InternalException iex) {
                    Logger.getLogger(JPDAThreadImpl.class.getName()).log(Level.INFO, getThreadStateLog(), iex);
                    return null;
                }
            } finally {
                accessLock.readLock().unlock();
            }
            if (or == null) return null;
            return new ThisVariable (debugger, or, "" + ObjectReferenceWrapper.uniqueID(or));
        } catch (InternalExceptionWrapper e) {
            return null;
        } catch (ObjectCollectedExceptionWrapper e) {
            return null;
        } catch (VMDisconnectedExceptionWrapper e) {
            return null;
        }
    }
    
    @Override
    public MonitorInfo getContendedMonitorAndOwner() {
        ObjectVariable monitor = getContendedMonitor();
        if (monitor == null) return null;
        // Search for the owner:
        MonitorInfo monitorInfo = null;
        JPDAThread thread = null;
        List<JPDAThread> threads = debugger.getThreadsCollector().getAllThreads();
        for (JPDAThread t : threads) {
            if (this == t) continue;
            ObjectVariable[] ms = t.getOwnedMonitors();
            for (ObjectVariable m : ms) {
                if (monitor.equals(m)) {
                    thread = t;
                    List<MonitorInfo> mf = t.getOwnedMonitorsAndFrames();
                    for (MonitorInfo mi : mf) {
                        if (monitor.equals(mi.getMonitor())) {
                            monitorInfo = mi;
                            break;
                        }
                    }
                    break;
                }
            }
            if (thread != null) {
                break;
            }
        }
        if (monitorInfo != null) {
            return monitorInfo;
        }
        return new MonitorInfoImpl(thread, null, monitor);
    }
    
    /**
     * Returns monitors owned by this thread.
     *
     * @return monitors owned by this thread
     */
    @Override
    public ObjectVariable[] getOwnedMonitors () {
        if (!VirtualMachineWrapper.canGetOwnedMonitorInfo0(vm)) {
            return new ObjectVariable[0];
        }
        List<ObjectReference> l;
        accessLock.readLock().lock();
        try {
            if (!(isSuspended() || suspendedNoFire)) return new ObjectVariable [0];
            if ("DestroyJavaVM".equals(threadName)) {
                // See defect #6474293
                return new ObjectVariable[0];
            }
            try {
                l = ThreadReferenceWrapper.ownedMonitors (threadReference);
                if (l == null) l = Collections.emptyList();
            } catch (IllegalThreadStateExceptionWrapper ex) {
                // Thrown when thread has exited
                return new ObjectVariable [0];
            } catch (ObjectCollectedExceptionWrapper ex) {
                return new ObjectVariable [0];
            } catch (VMDisconnectedExceptionWrapper ex) {
                return new ObjectVariable [0];
            } catch (IncompatibleThreadStateException e) {
                Logger.getLogger(JPDAThreadImpl.class.getName()).log(Level.INFO, getThreadStateLog(), e);
                return new ObjectVariable [0];
            } catch (InternalExceptionWrapper iex) {
                Logger.getLogger(JPDAThreadImpl.class.getName()).log(Level.INFO, getThreadStateLog(), iex);
                return new ObjectVariable [0];
            }
        } finally {
            accessLock.readLock().unlock();
        }
        int i, k = l.size ();
        try {
            ObjectVariable[] vs = new ObjectVariable [k];
            for (i = 0; i < k; i++) {
                ObjectReference var = l.get (i);
                vs[i] = new ThisVariable(debugger, var, "" + ObjectReferenceWrapper.uniqueID(var));
            }
            return vs;
        } catch (InternalExceptionWrapper ex) {
            return new ObjectVariable [0];
        } catch (VMDisconnectedExceptionWrapper ex) {
            return new ObjectVariable [0];
        } catch (ObjectCollectedExceptionWrapper ex) {
            return new ObjectVariable [0];
        }
    }
    
    public ThreadReference getThreadReference () {
        return threadReference;
    }
    
    public synchronized ReturnVariableImpl getReturnVariable() {
        return returnVariable;
    }
    
    public synchronized void setReturnVariable(ReturnVariableImpl returnVariable) {
        this.returnVariable = returnVariable;
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pch.addPropertyChangeListener(l);
    }
    
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pch.removePropertyChangeListener(l);
    }
    
    @Override
    public void addPropertyChangeListener(String name, PropertyChangeListener pcl) {
        if (PROP_OPERATIONS_SET.equals(name) || PROP_OPERATIONS_UPDATE.equals(name)) {
            operationsPch.addPropertyChangeListener(name, pcl);
        } else {
            pch.addPropertyChangeListener(name, pcl);
        }
    }

    @Override
    public void removePropertyChangeListener(String name, PropertyChangeListener pcl) {
        if (PROP_OPERATIONS_SET.equals(name) || PROP_OPERATIONS_UPDATE.equals(name)) {
            operationsPch.removePropertyChangeListener(name, pcl);
        } else {
            pch.removePropertyChangeListener(name, pcl);
        }
    }

    private void fireSuspended(boolean suspended) {
        pch.firePropertyChange(JPDAThread.PROP_SUSPENDED,
                Boolean.valueOf(!suspended), Boolean.valueOf(suspended));
    }
    
    private void fireOperationsChanged(String name) {
        operationsPch.firePropertyChange(name, null, null);
    }

    @Override
    public void setObject(Object bean) {
        throw new UnsupportedOperationException("Not supported, do not call. Implementing Customizer interface just because of add/remove PropertyChangeListener.");
    }

    @Override
    public void setBeanContext(BeanContext bc) throws PropertyVetoException {
        throw new UnsupportedOperationException("Not supported, do not call. Implementing BeanContextChild interface just because of add/remove PropertyChangeListener.");
    }

    @Override
    public BeanContext getBeanContext() {
        throw new UnsupportedOperationException("Not supported, do not call. Implementing BeanContextChild interface just because of add/remove PropertyChangeListener.");
    }

    @Override
    public void addVetoableChangeListener(String name, VetoableChangeListener vcl) {
        throw new UnsupportedOperationException("Not supported, do not call. Implementing BeanContextChild interface just because of add/remove PropertyChangeListener.");
    }

    @Override
    public void removeVetoableChangeListener(String name, VetoableChangeListener vcl) {
        throw new UnsupportedOperationException("Not supported, do not call. Implementing BeanContextChild interface just because of add/remove PropertyChangeListener.");
    }

    @Override
    public List<MonitorInfo> getOwnedMonitorsAndFrames() {
        if (VirtualMachineWrapper.canGetMonitorFrameInfo0(vm)) {
            accessLock.readLock().lock();
            try {
                if (!(isSuspended() || suspendedNoFire) || getState() == ThreadReference.THREAD_STATUS_ZOMBIE) {
                    return Collections.emptyList();
                }
                synchronized (ownedMonitorsAndFramesSingleAccessLock) {
                    // Prevent from inconsistencies coming from unsynchronized implementation of
                    // com.sun.tools.jdi.ThreadReferenceImpl.ownedMonitorsAndFrames()
                    List<com.sun.jdi.MonitorInfo> monitorInfos = ThreadReferenceWrapper.ownedMonitorsAndFrames0(threadReference);
                    if (monitorInfos != null && monitorInfos.size() > 0) {
                        List<MonitorInfo> mis = new ArrayList<MonitorInfo>(monitorInfos.size());
                        for (com.sun.jdi.MonitorInfo monitorInfo : monitorInfos) {
                            MonitorInfo mi = createMonitorInfo(monitorInfo);
                            if (mi != null) {
                                mis.add(mi);
                            }
                        }
                        return Collections.unmodifiableList(mis);
                    }
                }
            } catch (IncompatibleThreadStateException ex) {
                Logger.getLogger(JPDAThreadImpl.class.getName()).log(Level.INFO, getThreadStateLog(), ex);
            } catch (IllegalThreadStateExceptionWrapper ex) {
                // Thrown when thread has exited
            } finally {
                accessLock.readLock().unlock();
            }
        }
        return Collections.emptyList();
    }

    /**
     * 
     * @param mi com.sun.jdi.MonitorInfo
     * @return monitor info
     */
    private MonitorInfo createMonitorInfo(com.sun.jdi.MonitorInfo mi) {
        try {
            int depth = MonitorInfoWrapper.stackDepth(mi);
            CallStackFrame frame = null;
            if (depth >= 0) {
                try {
                    CallStackFrame[] frames = getCallStack(depth, depth + 1);
                    //frame = new CallStackFrameImpl(this, threadReference.frame(depth), depth, debugger);
                    if (frames.length > 0) {
                        frame = frames[0];
                    }
                //} catch (IncompatibleThreadStateException ex) {
                } catch (AbsentInformationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            ObjectReference or = MonitorInfoWrapper.monitor(mi);
            ObjectVariable monitor = new ThisVariable (debugger, or, "" + ObjectReferenceWrapper.uniqueID(or));
            return new MonitorInfoImpl(this, frame, monitor);
        } catch (InvalidStackFrameExceptionWrapper ex) {
            Exceptions.printStackTrace(ex);
        } catch (InternalExceptionWrapper ex) {
        } catch (VMDisconnectedExceptionWrapper ex) {
        } catch (ObjectCollectedExceptionWrapper ex) {
        }
        return null;
    }

    public boolean checkForBlockingThreads() {
        try {
            if (!VirtualMachineWrapper.canGetCurrentContendedMonitor0(vm) ||
                !VirtualMachineWrapper.canGetMonitorInfo0(vm)) {
                return false;
            }
            //System.err.println("\""+getName()+"\".checkForBlockingThreads()");
            Map<ThreadReference, ObjectReference> lockedThreadsWithMonitors = null;
            //synchronized (t.getDebugger().LOCK) { - can not synchronize on that - method invocation uses this lock.
            // TODO: Need to be freed up and method invocation flag needs to be used instead.
            List<JPDAThread> oldLockerThreadsList;
            List<JPDAThread> newLockerThreadsList;
            // Do not wait for write lock if it's not available, since no one will get read lock!
            boolean locked = debugger.accessLock.writeLock().tryLock();
            if (!locked) {
                debugger.accessLock.readLock().lock();
                try {
                    // We can not suspend the whole VM, but can check currently suspended threads, at least:
                    locked = accessLock.writeLock().tryLock();
                    if (!locked) {
                        synchronized (suspendToCheckForMonitorsLock) {
                            if (!canSuspendToCheckForMonitors) {
                                return false;
                            }
                            ThreadReferenceWrapper.suspend(threadReference);
                            try {
                                ObjectReference waitingMonitor = ThreadReferenceWrapper.currentContendedMonitor(threadReference);
                                if (waitingMonitor != null) {
                                    synchronized (lockerThreadsLock) {
                                        if (waitingMonitor.equals(lockerThreadsMonitor)) {
                                            // We're still blocked at the monitor
                                            return true;
                                        }
                                    }
                                    lockedThreadsWithMonitors = findLockPath(vm, threadReference, false, waitingMonitor);
                                }
                                Pair<List<JPDAThread>, List<JPDAThread>> lockerThreadsLists =
                                        updateLockerThreads(lockedThreadsWithMonitors, waitingMonitor);
                                oldLockerThreadsList = lockerThreadsLists.first();
                                newLockerThreadsList = lockerThreadsLists.second();
                            } catch (IncompatibleThreadStateException ex) {
                                return false;
                            } finally {
                                ThreadReferenceWrapper.resume(threadReference);
                            }
                        }
                    } else {
                        try {
                            boolean wasSuspended = false;
                            try {
                                wasSuspended = ThreadReferenceWrapper.isSuspended0(threadReference);
                                if (!wasSuspended) {
                                    ThreadReferenceWrapper.suspend(threadReference);
                                }
                                ObjectReference waitingMonitor = ThreadReferenceWrapper.currentContendedMonitor(threadReference);
                                if (waitingMonitor != null) {
                                    synchronized (lockerThreadsLock) {
                                        if (waitingMonitor.equals(lockerThreadsMonitor)) {
                                            // We're still blocked at the monitor
                                            return true;
                                        }
                                    }
                                    lockedThreadsWithMonitors = findLockPath(vm, threadReference, false, waitingMonitor);
                                }
                                Pair<List<JPDAThread>, List<JPDAThread>> lockerThreadsLists =
                                        updateLockerThreads(lockedThreadsWithMonitors, waitingMonitor);
                                oldLockerThreadsList = lockerThreadsLists.first();
                                newLockerThreadsList = lockerThreadsLists.second();
                            } catch (IncompatibleThreadStateException ex) {
                                return false;
                            } finally {
                                if (wasSuspended) {
                                    ThreadReferenceWrapper.resume(threadReference);
                                }
                            }
                        } finally {
                            accessLock.writeLock().unlock();
                        }
                    }
                } finally {
                    debugger.accessLock.readLock().unlock();
                }
            } else {
                // locked debugger access
                try {
                    logger.fine("checkForBlockingThreads("+threadName+"): suspend all...");
                    VirtualMachineWrapper.suspend(vm);
                    try {
                        ObjectReference waitingMonitor = ThreadReferenceWrapper.currentContendedMonitor(threadReference);
                        if (waitingMonitor != null) {
                            synchronized (lockerThreadsLock) {
                                if (waitingMonitor.equals(lockerThreadsMonitor)) {
                                    // We're still blocked at the monitor
                                    return true;
                                }
                            }
                            lockedThreadsWithMonitors = findLockPath(vm, threadReference, true, waitingMonitor);
                        }
                        Pair<List<JPDAThread>, List<JPDAThread>> lockerThreadsLists =
                                updateLockerThreads(lockedThreadsWithMonitors, waitingMonitor);
                        oldLockerThreadsList = lockerThreadsLists.first();
                        newLockerThreadsList = lockerThreadsLists.second();
                        //System.err.println("Locker threads list = "+newLockerThreadsList);
                    } catch (IncompatibleThreadStateException ex) {
                        return false;
                    } finally {
                        logger.fine("checkForBlockingThreads("+threadName+"): resume all.");
                        VirtualMachineWrapper.resume(vm);
                    }
                } finally {
                    debugger.accessLock.writeLock().unlock();
                }
            }
            if (oldLockerThreadsList != newLockerThreadsList) { // Not fire when both null
                //System.err.println("Fire lockerThreads: "+(oldLockerThreadsList == null || !oldLockerThreadsList.equals(newLockerThreadsList)));
                pch.firePropertyChange(PROP_LOCKER_THREADS, oldLockerThreadsList, newLockerThreadsList); // NOI18N
            }
            //}
            //setLockerThreads(lockedThreadsWithMonitors);
            return lockedThreadsWithMonitors != null;
        } catch (VMDisconnectedExceptionWrapper e) {
        } catch (InternalExceptionWrapper e) {
        } catch (ObjectCollectedExceptionWrapper e) {
        } catch (IllegalThreadStateExceptionWrapper ex) {
            // Thrown when thread has exited
        }
        return false;
    }
    
    private Pair<List<JPDAThread>, List<JPDAThread>> updateLockerThreads(
            Map<ThreadReference, ObjectReference> lockedThreadsWithMonitors,
            ObjectReference waitingMonitor) throws InternalExceptionWrapper,
                                                   VMDisconnectedExceptionWrapper,
                                                   ObjectCollectedExceptionWrapper,
                                                   IllegalThreadStateExceptionWrapper {
        List<JPDAThread> oldLockerThreadsList;
        List<JPDAThread> newLockerThreadsList;
        synchronized (lockerThreadsLock) {
            oldLockerThreadsList = lockerThreadsList;
            if (lockedThreadsWithMonitors != null) {
                //lockerThreads2 = lockedThreadsWithMonitors;
                lockerThreadsMonitor = waitingMonitor;
                if (!submitMonitorEnteredFor(waitingMonitor)) {
                    submitCheckForMonitorEntered(waitingMonitor);
                }
                lockerThreadsList = new ThreadListDelegate(debugger, new ArrayList(lockedThreadsWithMonitors.keySet()));
            } else {
                //lockerThreads2 = null;
                lockerThreadsMonitor = null;
                lockerThreadsList = null;
            }
            newLockerThreadsList = lockerThreadsList;
        }
        return Pair.of(oldLockerThreadsList, newLockerThreadsList);
    }

    private static Map<ThreadReference, ObjectReference> findLockPath(VirtualMachine vm, ThreadReference tr,
                                                                      final boolean suspendedAll,
                                                                      ObjectReference waitingMonitor) throws IncompatibleThreadStateException,
                                                                                                             InternalExceptionWrapper,
                                                                                                             VMDisconnectedExceptionWrapper,
                                                                                                             ObjectCollectedExceptionWrapper,
                                                                                                             IllegalThreadStateExceptionWrapper {
        Map<ThreadReference, ObjectReference> threadsWithMonitors = new LinkedHashMap<ThreadReference, ObjectReference>();
        Map<ObjectReference, ThreadReference> monitorMap = new HashMap<ObjectReference, ThreadReference>();
        for (ThreadReference t : VirtualMachineWrapper.allThreads(vm)) {
            if (suspendedAll || ThreadReferenceWrapper.isSuspended0(t)) {
                List<ObjectReference> monitors;
                try {
                    monitors = ThreadReferenceWrapper.ownedMonitors0(t);
                } catch (IllegalThreadStateExceptionWrapper | IncompatibleThreadStateException ex) {
                    continue;
                }
                if (monitors != null) {
                    for (ObjectReference m : monitors) {
                        monitorMap.put(m, t);
                    }
                }
            }
        }
        while (waitingMonitor != null) {
            ThreadReference t = monitorMap.get(waitingMonitor);
            if (t != null) {
                if (ThreadReferenceWrapper.suspendCount(t) > (suspendedAll ? 1 : 0)) { // Add it if it was suspended before
                    threadsWithMonitors.put(t, waitingMonitor);
                }
                waitingMonitor = ThreadReferenceWrapper.currentContendedMonitor(t);
            } else {
                break;
            }
        }
        if (threadsWithMonitors.size() > 0) {
            return threadsWithMonitors;
        } else {
            return null;
        }
    }

    public synchronized List<JPDAThread> getLockerThreads() {
        return lockerThreadsList;
    }

    public JPDADebuggerImpl getDebugger() {
        return debugger;
    }

    /*public void resumeToFreeMonitor(Variable monitor) {
        synchronized (this) {
            if (!isSuspended()) {
                return ; // Already resumed
            }
        }
        threadReference.virtualMachine().eventRequestManager();
    }*/

    public void resumeBlockingThreads() {
        List<JPDAThread> blockingThreads;
        synchronized (lockerThreadsLock) {
            if (lockerThreadsList == null) {
                return ;//false;
            }
            blockingThreads = new ArrayList<>(lockerThreadsList);
        }
        List<ThreadReference> resumedThreads = new ArrayList<ThreadReference>(blockingThreads.size());
        for (JPDAThread t : blockingThreads) {
            if (t.isSuspended()) {
                t.resume();
                resumedThreads.add(((JPDAThreadImpl) t).getThreadReference());
            }
        }
        synchronized (lockerThreadsLock) {
            this.resumedBlockingThreads = resumedThreads;
        }
        return ;//true;
    }

    private void submitMonitorEnteredRequest(EventRequest monitorEnteredRequest) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ObjectCollectedExceptionWrapper {
        EventRequestWrapper.setSuspendPolicy(monitorEnteredRequest, EventRequest.SUSPEND_ALL);
        EventRequestWrapper.putProperty(monitorEnteredRequest, Operator.SILENT_EVENT_PROPERTY, Boolean.TRUE);
        debugger.getOperator().register(monitorEnteredRequest, new Executor() {

            @Override
            public boolean exec(Event event) {
                try {
                    try {
                        //MonitorContendedEnteredEvent monitorEnteredEvent = (MonitorContendedEnteredEvent) event;
                        EventRequestManagerWrapper.deleteEventRequest(
                                VirtualMachineWrapper.eventRequestManager(vm),
                                EventWrapper.request(event));
                    } catch (InvalidRequestStateExceptionWrapper ex) {}
                    debugger.getOperator().unregister(EventWrapper.request(event));
                } catch (InternalExceptionWrapper ex) {
                    return true;
                } catch (VMDisconnectedExceptionWrapper ex) {
                    return true;
                }
                List<JPDAThread> oldLockerThreadsList;
                List<ThreadReference> threadsToSuspend;
                synchronized (lockerThreadsLock) {
                    oldLockerThreadsList = lockerThreadsList;
                    //lockerThreads2 = null;
                    lockerThreadsMonitor = null;
                    lockerThreadsList = null;
                    threadsToSuspend = resumedBlockingThreads;
                }
                pch.firePropertyChange(PROP_LOCKER_THREADS, oldLockerThreadsList, null);
                //System.err.println("Monitor freed, threadsToSuspend = "+threadsToSuspend);
                if (threadsToSuspend != null) {
                    for (ThreadReference tr : threadsToSuspend) {
                        try {
                            ThreadReferenceWrapper.suspend(tr); // Increases the suspend count to 2 so that it's not resumed by EventSet.resume()
                        } catch (IllegalThreadStateExceptionWrapper iex) {
                            // The thread is gone
                        } catch (InternalExceptionWrapper iex) {
                            // ??
                        } catch (ObjectCollectedExceptionWrapper ocex) {
                            // The thread is gone
                        } catch (VMDisconnectedExceptionWrapper vdex) {}
                        JPDAThreadImpl t = debugger.getExistingThread(tr);
                        if (t != null) {
                            t.notifySuspended();
                        }
                        //System.err.println("  Suspending "+t.getName()+" after monitor obtained.");
                    }
                }
                if (isMethodInvoking()) {
                    // HACK because of JDI:
                    // When invoking a method, EventSet.resume() will not resume the invocation thread
                    // We have to do it explicitely a suspend the thread right after the invocation, 'resumedToFinishMethodInvocation' flag is used for that.
                    debugger.getRequestProcessor().post(new Runnable() {
                        @Override
                        public void run() {
                            accessLock.writeLock().lock();
                            try {
                                logger.fine("Resuming thread "+threadName+" to finish method invoke...");
                                resumedToFinishMethodInvocation = true;
                                    ThreadReferenceWrapper.resume(threadReference);
                            } catch (IllegalThreadStateExceptionWrapper iex) {
                                // The thread is gone
                            } catch (VMDisconnectedExceptionWrapper e) {
                                // Ignored
                            } catch (Exception e) {
                                Exceptions.printStackTrace(e);
                            } finally {
                                accessLock.writeLock().unlock();
                            }
                            //System.err.println("  Resuming "+getName()+" because of method invocation.");
                        }
                    }, 200);
                }
                return true;
            }

            @Override
            public void removed(EventRequest eventRequest) {
            }
        });
        try {
            EventRequestWrapper.enable(monitorEnteredRequest);
        } catch (InternalExceptionWrapper ex) {
            debugger.getOperator().unregister(monitorEnteredRequest);
            throw ex;
        } catch (ObjectCollectedExceptionWrapper ocex) {
            debugger.getOperator().unregister(monitorEnteredRequest);
            throw ocex;
        } catch (InvalidRequestStateExceptionWrapper irse) {
            Exceptions.printStackTrace(irse);
        }
    }

    private boolean submitMonitorEnteredFor(ObjectReference waitingMonitor) {
        if (!VirtualMachineWrapper.canRequestMonitorEvents0(vm)) {
            return false;
        }
        try {
            com.sun.jdi.request.MonitorContendedEnteredRequest monitorEnteredRequest =
                    EventRequestManagerWrapper.createMonitorContendedEnteredRequest(
                            VirtualMachineWrapper.eventRequestManager(vm));

            MonitorContendedEnteredRequestWrapper.addThreadFilter(monitorEnteredRequest, threadReference);
            submitMonitorEnteredRequest(monitorEnteredRequest);
        } catch (InternalExceptionWrapper e) {
            return false;
        } catch (ObjectCollectedExceptionWrapper e) {
            return false;
        } catch (VMDisconnectedExceptionWrapper e) {
            return false;
        }
        return true;
    }

    private void submitCheckForMonitorEntered(ObjectReference waitingMonitor) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ObjectCollectedExceptionWrapper, IllegalThreadStateExceptionWrapper {
        try {
            ThreadReferenceWrapper.suspend(threadReference);
            logger.fine("submitCheckForMonitorEntered(): suspending "+threadName);
            ObjectReference monitor = ThreadReferenceWrapper.currentContendedMonitor(threadReference);
            if (monitor == null) return ;
            Location loc = StackFrameWrapper.location(ThreadReferenceWrapper.frame(threadReference, 0));
            loc = MethodWrapper.locationOfCodeIndex(LocationWrapper.method(loc), LocationWrapper.codeIndex(loc) + 1);
            if (loc == null) return;
            BreakpointRequest br = EventRequestManagerWrapper.createBreakpointRequest(
                    VirtualMachineWrapper.eventRequestManager(MirrorWrapper.virtualMachine(threadReference)), loc);
            BreakpointRequestWrapper.addThreadFilter(br, threadReference);
            submitMonitorEnteredRequest(br);
        } catch (IncompatibleThreadStateException itex) {
            Exceptions.printStackTrace(itex);
        } catch (InvalidStackFrameExceptionWrapper isex) {
            Exceptions.printStackTrace(isex);
        } finally {
            logger.fine("submitCheckForMonitorEntered(): resuming "+threadName);
            ThreadReferenceWrapper.resume(threadReference);
        }
    }

    public void setStepSuspendedBy(JPDABreakpoint breakpoint, boolean fire, List<JPDAThreadImpl> steppingThreads) {
        synchronized (stepBreakpointLock) {
            this.stepSuspendedByBreakpoint = breakpoint;
            if (this.suspendedSteppingThreads == null) {
                this.suspendedSteppingThreads = new HashSet<>();
            }
            this.suspendedSteppingThreads.addAll(steppingThreads);
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("setStepSuspendedBy("+threadReference+"): stepSuspendedByBreakpoint = "+stepSuspendedByBreakpoint+", steppingThreads = "+steppingThreads);
            }
        }
        for (JPDAThreadImpl st : steppingThreads) {
            st.addSteppingSuspendedBy(this);
        }
        if (fire) {
            pch.firePropertyChange(PROP_STEP_SUSPENDED_BY_BREAKPOINT, null, breakpoint);
        }
    }
    
    void addSteppingSuspendedBy(JPDAThreadImpl thread) {
        synchronized (stepBreakpointLock) {
            if (this.steppingSuspendedByBptsInThreads == null) {
                this.steppingSuspendedByBptsInThreads = new HashSet<>();
            }
            this.steppingSuspendedByBptsInThreads.add(thread);
        }
    }
    
    public boolean unsetSteppingSuspendedByBpts() {
        Set<JPDAThreadImpl> suspByThreads;
        synchronized (stepBreakpointLock) {
            suspByThreads = this.steppingSuspendedByBptsInThreads;
            this.steppingSuspendedByBptsInThreads = null;
        }
        if (suspByThreads != null) {
            for (JPDAThreadImpl t : suspByThreads) {
                t.unsetStepSuspendedByBpIn(this);
            }
        }
        return suspByThreads != null;
    }
    
    void unsetStepSuspendedByBpIn(JPDAThreadImpl thread) {
        JPDABreakpoint oldBp;
        synchronized (stepBreakpointLock) {
            oldBp = this.stepSuspendedByBreakpoint;
            this.stepSuspendedByBreakpoint = null;
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("unsetStepSuspendedByBp("+threadReference+"): stepSuspendedByBreakpoint = "+stepSuspendedByBreakpoint);
            }
            if (this.suspendedSteppingThreads == null) {
                this.suspendedSteppingThreads.remove(thread);
                if (this.suspendedSteppingThreads.isEmpty()) {
                    this.suspendedSteppingThreads = null;
                }
            }
        }
        if (oldBp != null) {
            pch.firePropertyChange(PROP_STEP_SUSPENDED_BY_BREAKPOINT, oldBp, null);
        }
        //return oldBp != null;
    }
    
    private JPDABreakpoint removeSuspendedByBreakpoint() {
        JPDABreakpoint brkp = null;
        Set<JPDAThreadImpl> steppingThreads = null;
        synchronized (stepBreakpointLock) {
            if (stepSuspendedByBreakpoint != null) {
                brkp = stepSuspendedByBreakpoint;
                stepSuspendedByBreakpoint = null;
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("removeSuspendedByBreakpoint("+threadReference+"): stepSuspendedByBreakpoint = "+stepSuspendedByBreakpoint);
                }
            }
            if (this.suspendedSteppingThreads != null) {
                steppingThreads = this.suspendedSteppingThreads;
                this.suspendedSteppingThreads = null;
            }
        }
        if (steppingThreads != null) {
            for (JPDAThreadImpl t : steppingThreads) {
                t.removeSteppingSuspendedBy(this);
            }
        }
        return brkp;
    }
    
    private void removeSteppingSuspendedBy(JPDAThreadImpl thread) {
        synchronized (stepBreakpointLock) {
            if (this.steppingSuspendedByBptsInThreads != null) {
                this.steppingSuspendedByBptsInThreads.remove(thread);
                if (this.steppingSuspendedByBptsInThreads.isEmpty()) {
                    this.steppingSuspendedByBptsInThreads = null;
                }
            }
        }
    }

    public String getThreadStateLog() {
        return getThreadStateLog(threadReference)+", internal suspend status = "+suspended+", suspendedNoFire = "+suspendedNoFire+", suspendedOnAnEvent = "+suspendedOnAnEvent+", invoking a method = "+methodInvoking+", is in step = "+inStep;
    }

    public static String getThreadStateLog(ThreadReference threadReference) {
        String name;
        try {
            name = ThreadReferenceWrapper.name(threadReference);
        } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper |
                 ObjectCollectedExceptionWrapper | IllegalThreadStateExceptionWrapper ex) {
            Throwable t = ex.getCause();
            name = "<"+t.getClass() + ":" + t.getLocalizedMessage()+">";
        }
        String status;
        try {
            status = Integer.toString(ThreadReferenceWrapper.status(threadReference));
        } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper |
                 ObjectCollectedExceptionWrapper | IllegalThreadStateExceptionWrapper ex) {
            Throwable t = ex.getCause();
            status = "<"+t.getClass() + ":" + t.getLocalizedMessage()+">";
        }
        String isSuspended;
        try {
            isSuspended = Boolean.toString(ThreadReferenceWrapper.isSuspended(threadReference));
        } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper |
                 ObjectCollectedExceptionWrapper | IllegalThreadStateExceptionWrapper ex) {
            Throwable t = ex.getCause();
            isSuspended = "<"+t.getClass() + ":" + t.getLocalizedMessage()+">";
        }
        String suspendCount;
        try {
            suspendCount = Integer.toString(ThreadReferenceWrapper.suspendCount(threadReference));
        } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper |
                 ObjectCollectedExceptionWrapper | IllegalThreadStateExceptionWrapper ex) {
            Throwable t = ex.getCause();
            suspendCount = "<"+t.getClass() + ":" + t.getLocalizedMessage()+">";
        }
        String isAtBreakpoint;
        try {
            isAtBreakpoint = Boolean.toString(ThreadReferenceWrapper.isAtBreakpoint(threadReference));
        } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper |
                 ObjectCollectedExceptionWrapper | IllegalThreadStateExceptionWrapper ex) {
            Throwable t = ex.getCause();
            isAtBreakpoint = "<"+t.getClass() + ":" + t.getLocalizedMessage()+">";
        }
        String msg = "Thread '"+name+
                         "': status = "+status+
                         ", is suspended = "+isSuspended+
                         ", suspend count = "+suspendCount+
                         ", is at breakpoint = "+isAtBreakpoint;
        return msg;
    }

    @Override
    public String toString() {
        return "'"+getName()+"' ("+Integer.toHexString(System.identityHashCode(this))+") from DBG("+Integer.toHexString(debugger.hashCode())+")";
    }

    private final Object pendingActionsLock = new Object();
    private Object pendingAction;
    private Variable pendingVariable;

    public void setPendingAction(Object action) {
        synchronized (pendingActionsLock) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "{0} setPendingAction({1})", new Object[]{threadName, action});
            }
            this.pendingAction = action;
            this.pendingVariable = null;
        }
    }

    public Object getPendingAction() {
        synchronized (pendingActionsLock) {
            return pendingAction;
        }
    }

    public String getPendingString(Object action) {
        return NbBundle.getMessage(JPDAThreadImpl.class, "MSG_PendingAction", action);
    }

    public Variable getPendingVariable(Object action) {
        Variable var;
        synchronized (pendingActionsLock) {
            var = (action == pendingAction) ? pendingVariable : null;
        }
        if (var == null) {
            StringReference sr = threadReference.virtualMachine().mirrorOf(getPendingString(action));
            var = new AbstractObjectVariable (debugger, sr, null);
        }
        synchronized (pendingActionsLock) {
            if (action == pendingAction) {
                pendingVariable = var;
            }
        }
        return var;
    }

    private static class ThreadListDelegate extends AbstractList<JPDAThread> {

        private List<ThreadReference> threads;
        private JPDADebuggerImpl debugger;

        public ThreadListDelegate(JPDADebuggerImpl debugger, List<ThreadReference> threads) {
            this.debugger = debugger;
            this.threads = threads;
        }

        @Override
        public JPDAThread get(int index) {
            return debugger.getThread(threads.get(index));
        }

        @Override
        public int size() {
            return threads.size();
        }

    }

    private class ThreadReentrantReadWriteLock extends ReentrantReadWriteLock {

        private final ReentrantReadWriteLock.ReadLock readerLock;
        private final ReentrantReadWriteLock.WriteLock writerLock;

        private ThreadReentrantReadWriteLock() {
            super(true);
            readerLock = new ThreadReadLock();
            writerLock = new ThreadWriteLock();
        }

        @Override
        public ReadLock readLock() {
            return readerLock;
        }

        @Override
        public WriteLock writeLock() {
            return writerLock;
        }

        private class ThreadReadLock extends ReadLock {

            private ThreadReadLock() {
                super(ThreadReentrantReadWriteLock.this);
            }

            @Override
            public void lock() {
                debugger.accessLock.readLock().lock();
                super.lock();
            }

            @Override
            public void lockInterruptibly() throws InterruptedException {
                debugger.accessLock.readLock().lockInterruptibly();
                try {
                    super.lockInterruptibly();
                } catch (InterruptedException iex) {
                    debugger.accessLock.readLock().unlock();
                    throw iex;
                }
            }

            @Override
            public boolean tryLock() {
                boolean locked = debugger.accessLock.readLock().tryLock();
                if (locked) {
                    locked = super.tryLock();
                    if (!locked) {
                        debugger.accessLock.readLock().unlock();
                    }
                }
                return locked;
            }

            @Override
            public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
                boolean locked = debugger.accessLock.readLock().tryLock(timeout, unit);
                if (locked) {
                    locked = super.tryLock(timeout, unit);
                    if (!locked) {
                        debugger.accessLock.readLock().unlock();
                    }
                }
                return locked;
            }

            @Override
            public void unlock() {
                super.unlock();
                debugger.accessLock.readLock().unlock();
            }

        }

        private class ThreadWriteLock extends WriteLock {

            private ThreadWriteLock() {
                super(ThreadReentrantReadWriteLock.this);
            }
            
            @Override
            public void lock() {
                debugger.accessLock.readLock().lock();
                super.lock();
            }

            @Override
            public void lockInterruptibly() throws InterruptedException {
                debugger.accessLock.readLock().lockInterruptibly();
                try {
                    super.lockInterruptibly();
                } catch (InterruptedException iex) {
                    debugger.accessLock.readLock().unlock();
                    throw iex;
                }
            }

            @Override
            public boolean tryLock() {
                boolean locked = debugger.accessLock.readLock().tryLock();
                if (locked) {
                    locked = super.tryLock();
                    if (!locked) {
                        debugger.accessLock.readLock().unlock();
                    }
                }
                return locked;
            }

            @Override
            public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
                boolean locked = debugger.accessLock.readLock().tryLock(timeout, unit);
                if (locked) {
                    locked = super.tryLock(timeout, unit);
                    if (!locked) {
                        debugger.accessLock.readLock().unlock();
                    }
                }
                return locked;
            }

            @Override
            public void unlock() {
                super.unlock();
                debugger.accessLock.readLock().unlock();
            }

        }

    }

}
