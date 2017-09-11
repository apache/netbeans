/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
