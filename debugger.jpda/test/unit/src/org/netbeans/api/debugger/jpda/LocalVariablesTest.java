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

import java.util.*;
import junit.framework.Test;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;

/**
 * Tests information about local variables.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class LocalVariablesTest extends NbTestCase {

    private JPDASupport     support;
    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();

    private static final String CLASS_NAME =
        "org.netbeans.api.debugger.jpda.testapps.LocalVariablesApp";


    public LocalVariablesTest(String s) {
        super(s);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(LocalVariablesTest.class);
    }
    
    public void testWatches () throws Exception {
        try {
            Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src") + 
                    "org/netbeans/api/debugger/jpda/testapps/LocalVariablesApp.java");
            LineBreakpoint lb = bp.getLineBreakpoints().get(0);
            dm.addBreakpoint (lb);

            support = JPDASupport.attach (CLASS_NAME);

            support.waitState (JPDADebugger.STATE_STOPPED);  // breakpoint hit

            CallStackFrame sf = support.getDebugger ().getCurrentCallStackFrame ();
            assertEquals (
                "Debugger stopped at wrong line", 
                lb.getLineNumber (), 
                sf.getLineNumber (null)
            );

            LocalVariable [] vars = sf.getLocalVariables ();
            assertEquals (
                "Wrong number of local variables", 
                4, 
                vars.length
            );
            Arrays.sort (vars, new Comparator () {
                public int compare (Object o1, Object o2) {
                    return ((LocalVariable) o1).getName ().compareTo (
                        ((LocalVariable) o2).getName ()
                    );
                }
            });
            assertEquals (
                "Wrong info about local variables", 
                "g", 
                vars [0].getName ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "20", 
                vars [0].getValue ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "int", 
                vars [0].getDeclaredType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "int", 
                vars [0].getType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                CLASS_NAME, 
                vars [0].getClassName ()
            );

            assertEquals (
                "Wrong info about local variables", 
                "s", 
                vars [1].getName ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "\"asdfghjkl\"", 
                vars [1].getValue ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "java.lang.Object", 
                vars [1].getDeclaredType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "java.lang.String", 
                vars [1].getType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                CLASS_NAME, 
                vars [1].getClassName ()
            );

            assertEquals (
                "Wrong info about local variables", 
                "x", 
                vars [2].getName ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "40", 
                vars [2].getValue ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "int", 
                vars [2].getDeclaredType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "int", 
                vars [2].getType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                CLASS_NAME, 
                vars [2].getClassName ()
            );

            assertEquals (
                "Wrong info about local variables", 
                "y", 
                vars [3].getName ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "50.5", 
                vars [3].getValue ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "float", 
                vars [3].getDeclaredType ()
             );
            assertEquals (
                "Wrong info about local variables", 
                "float", 
                vars [3].getType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                CLASS_NAME, 
                vars [3].getClassName ()
            );

            support.stepOver ();
            support.stepOver ();

            sf = support.getDebugger ().getCurrentCallStackFrame ();
            vars = sf.getLocalVariables ();
            assertEquals ("Wrong number of local variables", 4, vars.length);
            Arrays.sort (vars, new Comparator () {
                public int compare (Object o1, Object o2) {
                    return ((LocalVariable) o1).getName ().compareTo (
                        ((LocalVariable) o2).getName ()
                    );
                }
            });
            assertEquals (
                "Wrong info about local variables", 
                "g", 
                vars [0].getName ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "\"ad\"", 
                vars [0].getValue ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "java.lang.CharSequence", 
                vars [0].getDeclaredType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "java.lang.String", 
                vars [0].getType ()
             );
            assertEquals (
                "Wrong info about local variables", 
                CLASS_NAME, 
                vars [0].getClassName ()
            );

        } finally {
            support.doFinish ();
        }
    }
}
