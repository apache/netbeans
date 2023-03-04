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

package org.netbeans.api.debugger.jpda.event;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.event.Event;
import java.util.EventObject;
import org.netbeans.api.debugger.jpda.*;

/**
 * JPDABreakpoint event notification.
 *
 * @author   Jan Jancura
 */
public final class JPDABreakpointEvent extends EventObject {

    /** Condition result constant. */
    public static final int CONDITION_NONE = 0;
    /** Condition result constant. */
    public static final int CONDITION_TRUE = 1;
    /** Condition result constant. */
    public static final int CONDITION_FALSE = 2;
    /** Condition result constant. */
    public static final int CONDITION_FAILED = 3;
    
    
    private int             conditionResult = CONDITION_FAILED;
    private Throwable       conditionException = null;
    private JPDADebugger    debugger;
    private JPDAThread      thread;
    private ReferenceType   referenceType;
    private Variable        variable;
    private boolean         resume = false;
    private Event           event;  // The original event for special purposes
    

    /**
     * Creates a new instance of JPDABreakpointEvent. This method should be
     * called from debuggerjpda module only. Do not create a new instances
     * of this class!
     *
     * @param sourceBreakpoint  a breakpoint
     * @param debugger          a debugger this 
     * @param conditionResult   a result of condition
     * @param thread            a context thread
     * @param referenceType     a context class
     * @param variable          a context variable
     */
    public JPDABreakpointEvent (
        JPDABreakpoint  sourceBreakpoint, 
        JPDADebugger    debugger,
        int             conditionResult,
        JPDAThread      thread,
        ReferenceType   referenceType,
        Variable        variable
    ) {
        super (sourceBreakpoint);
        this.conditionResult = conditionResult;
        this.thread = thread;
        this.debugger = debugger;
        this.referenceType = referenceType;
        this.variable = variable;
    }
    
    /**
     * Creates a new instance of JPDABreakpointEvent.
     *
     * @param sourceBreakpoint a breakpoint
     * @param conditionException result of condition
     * @param thread            a context thread
     * @param debugger          a debugger this 
     * @param referenceType     a context class
     * @param variable          a context variable
     */
    public JPDABreakpointEvent (
        JPDABreakpoint sourceBreakpoint, 
        JPDADebugger    debugger,
        Throwable conditionException,
        JPDAThread      thread,
        ReferenceType   referenceType,
        Variable        variable
    ) {
        super (sourceBreakpoint);
        this.conditionResult = CONDITION_FAILED;
        this.conditionException = conditionException;
        this.thread = thread;
        this.debugger = debugger;
        this.referenceType = referenceType;
        this.variable = variable;
    }
    
    /**
     * Returns result of condition evaluation.
     *
     * @return result of condition evaluation
     */
    public int getConditionResult () {
        return conditionResult;
    }
    
    /**
     * Returns result of condition evaluation.
     *
     * @return result of condition evaluation
     */
    public Throwable getConditionException () {
        return conditionException;
    }
    
    /**
     * Returns context thread - thread stopped on breakpoint. This parameter 
     * is defined by class prepared breakpoint, exception breakpoint, 
     * field breakpoint, line breakpoint, method breakpoint and 
     * thread breakpoint.
     *
     * @return thread context
     */
    public JPDAThread getThread () {
        return thread;
    }
    
    /**
     * Returns context class. It means loaded class for class load breakpoint 
     * and exception class for exception breakpoint.
     *
     * @return context class
     */
    public ReferenceType getReferenceType () {
        return referenceType;
    }
    
    /**
     * Returns JPDADebugger instance this breakpoint has been reached in.
     *
     * @return JPDADebugger instance this breakpoint has been reached in
     */
    public JPDADebugger getDebugger () {
        return debugger;
    }
    
    /**
     * Returns context variable. It contains new value for field modification
     * breakpoint and instance of exception for exception breakpoint.
     *
     * @return context variable
     */
    public Variable getVariable () {
        return variable;
    }
    
    /**
     * Call this method to resume debugger after all events will be notified.
     * You should not call JPDADebugger.resume () during breakpoint event 
     * evaluation!
     */
    public void resume () {
        resume = true;
    }
    
    /**
     * Returns resume value.
     */
    public boolean getResume () {
        return resume;
    }
}
