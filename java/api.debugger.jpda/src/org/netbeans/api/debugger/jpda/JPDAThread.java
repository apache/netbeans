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

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ThreadReference;
import java.util.List;
import java.util.concurrent.locks.Lock;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;


/**
 * Represents one Java thread in debugged process.
 *
 * <pre style="background-color: rgb(255, 255, 102);">
 * Since JDI interfaces evolve from one version to another, it's strongly recommended
 * not to implement this interface in client code. New methods can be added to
 * this interface at any time to keep up with the JDI functionality.</pre>
 *
 * @author Jan Jancura
 */
public interface JPDAThread {

    /** Thread state constant. */
    public static final int STATE_UNKNOWN = ThreadReference.THREAD_STATUS_UNKNOWN;
    /** Thread state constant. */
    public static final int STATE_MONITOR = ThreadReference.THREAD_STATUS_MONITOR;
    /** Thread state constant. */
    public static final int STATE_NOT_STARTED = ThreadReference.THREAD_STATUS_NOT_STARTED;
    /** Thread state constant. */
    public static final int STATE_RUNNING = ThreadReference.THREAD_STATUS_RUNNING;
    /** Thread state constant. */
    public static final int STATE_SLEEPING = ThreadReference.THREAD_STATUS_SLEEPING;
    /** Thread state constant. */
    public static final int STATE_WAIT = ThreadReference.THREAD_STATUS_WAIT;
    /** Thread state constant. */
    public static final int STATE_ZOMBIE = ThreadReference.THREAD_STATUS_ZOMBIE;

    /**
     * Suspended property of the thread. Fired when isSuspended() changes.
     * @since 2.16
     */
    public static final String PROP_SUSPENDED = "suspended";
    /** Property name constant. */
    public static final String PROP_CALLSTACK = "callStack";
    /** Property name constant. */
    public static final String PROP_VARIABLES = "variables";
    /** Property name constant.
     * @since 2.16     */
    public static final String PROP_BREAKPOINT = "currentBreakpoint";


    /**
     * Getter for the "read" lock under which it's guaranteed that the thread
     * does not change it's suspended state.
     *
     * Multiple threads can acquire this "read" lock at the same time. But the
     * thread state can be changed only under an internal "write" lock.
     * Clients can use this lock while retrieving data to assure that the thread
     * is not resumed in the mean time.
     *
     * @return The read access lock.
     * @since 2.18
     */
    public Lock getReadAccessLock();
    
    /**
     * Getter for the name of thread property.
     *
     * @return name of thread
     */
    public abstract String getName ();
    
    /**
     * Returns parent thread group.
     *
     * @return parent thread group
     */
    public abstract JPDAThreadGroup getParentThreadGroup ();

    /**
     * Returns line number of the location this thread stopped at.
     * The thread should be suspended at the moment this method is called.
     *
     * @return  line number of the current location if the thread is suspended,
     *          contains at least one frame and the topmost frame does not
     *          represent a native method invocation; <CODE>-1</CODE> otherwise
     * @see CallStackFrame
     */
    public abstract int getLineNumber (String stratum);

    /**
     * Returns the operation that is being currently executed on this thread.
     * @return The current operation, or <CODE>null</CODE>.
     * @see CallStackFrame#getCurrentOperation(String)
     */
    public abstract Operation getCurrentOperation();
    
    /**
     * Returns the current breakpoint hit by this thread.
     * @return The current breakpoint, or <CODE>null</CODE>.
     * @since 2.16
     */
    public abstract JPDABreakpoint getCurrentBreakpoint();
    
    /**
     * Returns the list of the last operations, that were performed on this thread.
     * Typically just operations from the current expression are stored.
     * The thread should be suspended at the moment this method is called.
     *
     * @return  The list of last operations if available, the thread is suspended,
     *          contains at least one frame and the topmost frame does not
     *          represent a native method invocation; <CODE>null</CODE> otherwise
     * @see CallStackFrame
     */
    public abstract List<Operation> getLastOperations();

    /**
     * Returns current state of this thread.
     *
     * @return current state of this thread
     */
    public abstract int getState ();
    
    /**
     * Returns true if this thread is suspended by debugger.
     *
     * @return true if this thread is suspended by debugger
     */
    public abstract boolean isSuspended ();

    /**
     * If this thread is suspended returns class name this thread is 
     * stopped in.
     *
     * @return class name this thread is stopped in
     */
    public abstract String getClassName ();

    /**
     * If this thread is suspended returns method name this thread is 
     * stopped in.
     *
     * @return method name this thread is stopped in
     */
    public abstract String getMethodName ();
    
    /**
     * Suspends thread.
     */
    public abstract void suspend ();
    
    /**
     * Unsuspends thread.
     */
    public abstract void resume ();
    
    /**
     * Interrupts this thread unless the thread has been suspended.
     * @since 2.1
     */
    public abstract void interrupt();
    
    /**
     * Returns file name this frame is stopped in or null.
     *
     * @return file name this frame is stopped in
     */
    public abstract String getSourceName (String stratum) 
    throws AbsentInformationException;
    
    /**
     * Returns source path of file this frame is stopped in or null.
     *
     * @return source path of file this frame is stopped in or null
     */
    public abstract String getSourcePath (String stratum) 
    throws AbsentInformationException;
    
    /**
     * Returns call stack for this thread.
     *
     * @throws AbsentInformationException if the thread is running or not able
     *         to return callstack. If the thread is in an incompatible state
     *         (e.g. running), the AbsentInformationException has
     *         IncompatibleThreadStateException as a cause.
     * @return call stack
     */
    public abstract CallStackFrame[] getCallStack () 
    throws AbsentInformationException;
    
    /**
     * Returns call stack for this thread on the given indexes.
     *
     * @param from a from index, inclusive
     * @param to a to index, exclusive
     * @throws AbsentInformationException if the thread is running or not able
     *         to return callstack. If the thread is in an incompatible state
     *         (e.g. running), the AbsentInformationException has
     *         IncompatibleThreadStateException as a cause.
     * @return call stack
     */
    public abstract CallStackFrame[] getCallStack (int from, int to) 
    throws AbsentInformationException;
    
    /**
     * Returns length of current call stack.
     *
     * @return length of current call stack
     */
    public abstract int getStackDepth ();
    
    /**
     * Sets this thread current.
     *
     * @see JPDADebugger#getCurrentThread
     */
    public abstract void makeCurrent ();
    
    /**
     * Returns monitor this thread is waiting on.
     *
     * @return monitor this thread is waiting on
     */
    public abstract ObjectVariable getContendedMonitor ();
    
    /**
     * Returns monitor this thread is waiting on, with the information
     * about the owner of the monitor.
     *
     * @return monitor this thread is waiting on, with the owner.
     * @since 2.16
     */
    public abstract MonitorInfo getContendedMonitorAndOwner ();
    
    /**
     * Returns monitors owned by this thread.
     *
     * @return monitors owned by this thread
     */
    public abstract ObjectVariable[] getOwnedMonitors ();
    
    /**
     * Get the list of monitors with stack frame info owned by this thread.
     * 
     * @return the list of monitors with stack frame info
     * @since 2.16
     */
    List<MonitorInfo> getOwnedMonitorsAndFrames();
}
