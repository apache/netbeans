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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.JavaEvaluator;
import org.netbeans.modules.debugger.jpda.expr.EvaluationContext;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IllegalArgumentExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeComponentWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.spi.debugger.jpda.Evaluator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Represents watch in JPDA debugger.
 *
 * @author   Jan Jancura
 */

class JPDAWatchImpl extends AbstractVariable implements JPDAWatch/*, Watch.Provider*/ {

    private JPDADebuggerImpl    debugger;
    private Watch               watch;
    private String              exceptionDescription;
    
    
    JPDAWatchImpl (JPDADebuggerImpl debugger, Watch watch, PrimitiveValue v) {
        super (
            debugger,
            v, 
            "" + watch +
                (v instanceof ObjectReference ? "^" : "")
        );
        this.debugger = debugger;
        this.watch = watch;
    }
    
    JPDAWatchImpl (
        JPDADebuggerImpl debugger, 
        Watch watch, 
        Throwable exception
    ) {
        super (
            debugger, 
            null, 
            "" + watch
        );
        this.debugger = debugger;
        this.watch = watch;
        this.exceptionDescription = exception.getLocalizedMessage ();
        if (exceptionDescription == null)
            exceptionDescription = exception.getMessage ();
        Throwable t = exception.getCause();
        if (t != null && exception instanceof InvalidExpressionException &&
            ((InvalidExpressionException) exception).hasApplicationTarget()) {
            java.io.StringWriter s = new java.io.StringWriter();
            java.io.PrintWriter p = new java.io.PrintWriter(s);
            t.printStackTrace(p);
            p.close();
            exceptionDescription += " \n"+s.toString();
        }
    }

    public Watch getWatch() {
        return watch;
    }

    /**
     * Watched expression.
     *
     * @return watched expression
     */
    public String getExpression () {
        return watch.getExpression ();
    }

    /**
     * Sets watched expression.
     *
     * @param expression a expression to be watched
     */
    public void setExpression (String expression) {
        watch.setExpression (expression);
    }
    
    /**
     * Remove the watch from the list of all watches in the system.
     */
    public void remove () {
        watch.remove ();
    }
    
    /**
     * Returns description of problem is this watch can not be evaluated
     * in current context.
     *
     * @return description of problem
     */
    public String getExceptionDescription () {
        return exceptionDescription;
    }

    /**
    * Sets string representation of value of this variable.
    *
    * @param value string representation of value of this variable.
    *
    public void setValue (String expression) throws InvalidExpressionException {
        // evaluate expression to Value
        Value value = model.getDebugger ().evaluateIn (expression);
        // set new value to remote veriable
        setValue (value);
        // set new value to this model
        setInnerValue (value);
        // refresh tree
        Object node = nodeRef.get();
        if (node != null) {
            model.fireTableValueChangedChanged (node, null);
        }
    }
     */
    
    protected void setInnerValue (Value v) {
        super.setInnerValue (v);
        exceptionDescription = null;
    }
    
    static EvaluationContext.VariableInfo getInfo(JPDADebuggerImpl debugger, Value v) {
        Session s = debugger.getSession();
        Evaluator e = s.lookupFirst(s.getCurrentLanguage(), Evaluator.class);
        if (e == null) {
            e = s.lookupFirst("Java", Evaluator.class);
        }
        if (!(e instanceof JavaEvaluator)) {
            return null;
        }
        return ((JavaEvaluator) e).getValueContainer(v);
    }
    
    protected void setValue (Value value) throws InvalidExpressionException {
        EvaluationContext.VariableInfo vi = getInfo(debugger, getInnerValue());
        if (vi != null) {
            try {
                vi.setValue(value);
            } catch (IllegalStateException isex) {
                if (isex.getCause() instanceof InvalidExpressionException) {
                    throw (InvalidExpressionException) isex.getCause();
                } else {
                    throw new InvalidExpressionException(isex);
                }
            }
        } else {
            throw new InvalidExpressionException (
                    NbBundle.getMessage(JPDAWatchImpl.class, "MSG_CanNotSetValue", getExpression()));
        }
    }
    
    public String getToStringValue() throws InvalidExpressionException {
        return AbstractObjectVariable.getToStringValue(getInnerValue(), getDebugger(), 0);
    }
    
    void setException (String exceptionDescription) {
        setInnerValue (null);
        this.exceptionDescription = exceptionDescription;
    }
    
    public JPDAWatchImpl clone() {
        JPDAWatchImpl clon;
        if (exceptionDescription == null) {
            clon = new JPDAWatchImpl(getDebugger(), watch, (PrimitiveValue) getJDIValue());
        } else {
            clon = new JPDAWatchImpl(getDebugger(), watch, new Exception(exceptionDescription));
        }
        return clon;
    }
    
}

