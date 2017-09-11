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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

