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
