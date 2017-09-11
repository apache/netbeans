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

package org.netbeans.api.debugger.jpda;

import java.util.List;
import junit.framework.Test;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.jpda.EditorContext.MethodArgument;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.openide.util.Exceptions;


/**
 * Tests JPDA expression stepping action.
 *
 * @author Martin Entlicher, Jan Jancura
 */
public class ExpressionStepTest extends NbTestCase {

    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();
    private String          sourceRoot = System.getProperty ("test.dir.src");
    private JPDASupport     support;

    public ExpressionStepTest (String s) {
        super (s);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(ExpressionStepTest.class);
    }
    
    public void testExpressionStep() throws Exception {
        try {
            JPDASupport.removeAllBreakpoints ();
            LineBreakpoint lb = Utils.getBreakPositions(sourceRoot + 
                    "org/netbeans/api/debugger/jpda/testapps/ExpressionStepApp.java").getLineBreakpoints().get(0);
            dm.addBreakpoint (lb);
            support = JPDASupport.attach
                ("org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp");
            support.waitState (JPDADebugger.STATE_STOPPED);
            dm.removeBreakpoint (lb);
            assertEquals (
                "Execution stopped in wrong class", 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getClassName (), 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp"
            );
            int line = lb.getLineNumber();
            assertEquals (
                "Execution stopped at wrong line", 
                line, 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getLineNumber (null)
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                line, 14,
                "factorial",
                null,
                new Object[] { "10" }
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                line+1, 14,
                "factorial",
                new Object[] {"3628800"},
                new Object[] { "20" }
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                line+1, 30,
                "factorial",
                new Object[] {"2432902008176640000"},
                new Object[] { "30" }
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                line+2, 14,
                "factorial",
                new Object[] {"2432902008176640000", "-8764578968847253504"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                line+2, 34,
                "factorial",
                new Object[] {"-70609262346240000"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                line+3, 37,
                "<init>", // "ExpressionStepApp",
                new Object[] {"-70609262346240000", "-3258495067890909184"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                line+4, 24,
                "m2",
                null,
                new Object[] { "(int)x" }
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                line+4, 17,
                "m1",
                new Object[] {"-899453552"},
                new Object[] { "exs.m2((int)x)" }
            );
            
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                line+5, 31,
                "m2",
                new Object[] {"-899453552", "-404600928"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                line+5, 24,
                "m1",
                new Object[] {"497916032"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                line+5, 49,
                "m1",
                new Object[] {"497916032", "684193024"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                line+5, 17,
                "m3",
                new Object[] {"497916032", "684193024", "248958016"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                line+5, 62,
                "intValue",
                new Object[] {"497916032", "684193024", "248958016", "933151070"}
            );
            
            support.doContinue ();
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
        } finally {
            support.doFinish ();
        }
    }

    private void stepCheck (
        Object stepType, 
        String clsExpected, 
        int lineExpected,
        int column,
        String methodName
    ) {
        try {
            // We need to wait for all listeners to be notified and appropriate
            // actions to be enabled/disabled
            Thread.currentThread().sleep(10);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        support.step (stepType);
        assertEquals(
            "Execution stopped in wrong class", 
            clsExpected, 
            support.getDebugger ().getCurrentCallStackFrame ().getClassName ()
        );
        assertEquals (
            "Execution stopped at wrong line", 
            lineExpected, 
            support.getDebugger ().getCurrentCallStackFrame ().
                getLineNumber (null)
        );
        if (column > 0) {
            Operation op = support.getDebugger ().getCurrentCallStackFrame ().getCurrentOperation(null);
            assertNotNull(op);
            assertEquals("Execution stopped at a wrong column", column, op.getMethodStartPosition().getColumn());
        }
        if (methodName != null) {
            Operation op = support.getDebugger ().getCurrentCallStackFrame ().getCurrentOperation(null);
            assertNotNull(op);
            assertEquals("Execution stopped at a wrong method call", methodName, op.getMethodName());
        }
    }
    
    private void stepCheck (
        Object stepType, 
        String clsExpected, 
        int lineExpected,
        int column,
        String methodName,
        Object[] returnValues
    ) {
        stepCheck(stepType, clsExpected, lineExpected, column, methodName);
        if (returnValues != null) {
            List<Operation> ops = support.getDebugger ().getCurrentThread().getLastOperations();
            assertEquals("Different count of last operations and expected return values.", returnValues.length, ops.size());
            for (int i = 0; i < returnValues.length; i++) {
                Variable rv = ops.get(i).getReturnValue();
                String retValue;
                if (rv instanceof ObjectVariable) {
                    try {
                        retValue = ((ObjectVariable) rv).getToStringValue();
                    } catch (InvalidExpressionException ex) {
                        Exceptions.printStackTrace(ex);
                        retValue = rv.getValue();
                    }
                } else {
                    retValue = rv.getValue();
                }
                if (rv != null) {
                    assertEquals("Bad return value", returnValues[i], retValue);
                }
            }
        }
    }
    
    private void stepCheck (
        Object stepType, 
        String clsExpected, 
        int lineExpected,
        int column,
        String methodName,
        Object[] returnValues,
        Object[] opArguments
    ) {
        stepCheck(stepType, clsExpected, lineExpected, column, methodName, returnValues);
        Operation currentOp = support.getDebugger ().getCurrentThread().getCurrentOperation();
        MethodArgument[] arguments = getContext().getArguments(
                Utils.getURL(sourceRoot + "org/netbeans/api/debugger/jpda/testapps/ExpressionStepApp.java"),
                currentOp);
        assertEquals("Different count of operation arguments.", opArguments.length, arguments.length);
        for (int i = 0; i < opArguments.length; i++) {
            assertEquals("Bad method argument", opArguments[i], arguments[i].getName());
        }
    }
    
    private static EditorContext getContext () {
        // XXX lookupFirst?
        List l = DebuggerManager.getDebuggerManager ().lookup 
            (null, EditorContext.class);
        EditorContext context = (EditorContext) l.get (0);
        return context;
    }
}
