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

package org.netbeans.api.debugger.jpda;

import junit.framework.Test;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;

/**
 * Tests evaluation of various expressions.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class EvaluationTest extends NbTestCase {

    private JPDASupport     support;


    public EvaluationTest (String s) {
        super (s);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(EvaluationTest.class);
    }
    
    protected void setUp () throws Exception {
        super.setUp ();
        JPDASupport.removeAllBreakpoints ();
        Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src")+
                                  "org/netbeans/api/debugger/jpda/testapps/EvalApp.java");
        LineBreakpoint lb = bp.getLineBreakpoints().get(0);
        DebuggerManager.getDebuggerManager ().addBreakpoint (lb);
        support = JPDASupport.attach (
            "org.netbeans.api.debugger.jpda.testapps.EvalApp"
        );
        support.waitState (JPDADebugger.STATE_STOPPED);
    }

    public void testStaticEvaluation () throws Exception {
        try {
            checkEval ("1", 1);
            checkEval ("4.3", 4.3);
            checkEval ("ix", 74);

            checkEvalFails ("this");
            checkEvalFails ("NoSuchClass.class");
        } finally {
            support.doFinish ();
        }
    }

    public void testStaticExpressions () throws Exception {
        try {
            checkEval ("ix * fx", 740.0f);
            checkEval ("sx % 3", 1);

            checkEvalFails ("ix * fx ** fx");
        } finally {
            support.doFinish ();
        }
    }

    private void checkEval (String expression, int value) {
        try {
            Variable var = support.getDebugger ().evaluate (expression);
            assertEquals (
                "Evaluation of expression failed (wrong value): " + expression, 
                value, 
                Integer.parseInt (var.getValue ()), 0
            );
            assertEquals (
                "Evaluation of expression failed (wrong type of result): " + 
                    expression, 
                "int", 
                var.getType ()
            );
        } catch (InvalidExpressionException e) {
            fail (
                "Evaluation of expression was unsuccessful: " + e
            );
        }
    }

    private void checkEval(String expression, float value) {
        try {
            Variable var = support.getDebugger ().evaluate (expression);
            assertEquals (
                "Evaluation of expression failed (wrong value): " + expression, 
                value, 
                Float.parseFloat (var.getValue ()), 
                0
            );
            assertEquals (
                "Evaluation of expression failed (wrong type of result): " + 
                    expression, 
                "float", 
                var.getType ()
            );
        } catch (InvalidExpressionException e) {
            fail (
                "Evaluation of expression was unsuccessful: " + e
            );
        }
    }

    private void checkEval (String expression, double value) {
        try {
            Variable var = support.getDebugger ().evaluate (expression);
            assertEquals (
                "Evaluation of expression failed (wrong value): " + expression, 
                value, 
                Double.parseDouble (var.getValue ()), 
                0
            );
            assertEquals (
                "Evaluation of expression failed (wrong type of result): " + 
                    expression, 
                "double", 
                var.getType ()
            );
        } catch (InvalidExpressionException e) {
            fail (
                "Evaluation of expression was unsuccessful: " + e
            );
        }
    }

    private void checkEval (String expression, String type, String value) {
        try {
            Variable var = support.getDebugger ().evaluate (expression);
            assertEquals (
                "Evaluation of expression failed (wrong value): " + expression, 
                value, 
                var.getValue ()
            );
            assertEquals (
                "Evaluation of expression failed (wrong type of result): " +  
                    expression, 
                type, 
                var.getType ()
            );
        } catch (InvalidExpressionException e) {
            fail (
                "Evaluation of expression was unsuccessful: " + e
            );
        }
    }

    private void checkEvalFails (String expression) {
        try {
            Variable var = support.getDebugger ().evaluate (expression);
            fail (
                "Evaluation of expression was unexpectedly successful: " + 
                expression + " = " + var.getValue ()
            );
        } catch (InvalidExpressionException e) {
            // its ok
            return;
        }
    }
}
