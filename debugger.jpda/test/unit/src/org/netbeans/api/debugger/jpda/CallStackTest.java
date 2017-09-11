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
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;


/**
 * Tests information about stack call stacks.
 *
 * @author Maros Sandor
 */
public class CallStackTest extends NbTestCase {


    public CallStackTest (String s) {
        super (s);
    }
    
    public static Test suite() {
        return JPDASupport.createTestSuite(CallStackTest.class);
    }

    public void testInstanceCallStackInfo () throws Exception {
        JPDASupport support = null;
        try {
            JPDASupport.removeAllBreakpoints ();
            Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src") + 
                    "org/netbeans/api/debugger/jpda/testapps/CallStackApp.java");
            LineBreakpoint lb = bp.getLineBreakpoints().get(0);
            //lb.setPreferredClassName("org.netbeans.api.debugger.jpda.testapps.CallStackApp");
            DebuggerManager.getDebuggerManager ().addBreakpoint (lb);
            support = JPDASupport.attach (
                "org.netbeans.api.debugger.jpda.testapps.CallStackApp"
            );
            support.waitState (JPDADebugger.STATE_STOPPED);
            support.stepOver ();
            support.stepInto ();
            support.stepOver ();
            support.stepInto ();
            support.stepOver ();

            CallStackFrame sf = support.getDebugger ().
                getCurrentCallStackFrame ();

            List strata = sf.getAvailableStrata ();
            assertEquals (
                "Available strata", 
                1, 
                strata.size ()
            );
            assertEquals (
                "Java stratum is not available", 
                "Java", 
                strata.get (0)
            );
            assertEquals (
                "Java stratum is not default", 
                "Java", 
                sf.getDefaultStratum ()
            );
            assertEquals (
                "Wrong class name", 
                "org.netbeans.api.debugger.jpda.testapps.CallStackApp", 
                sf.getClassName ()
            );
            assertEquals (
                "Wrong line number", 
                bp.getStopLine("CallStackEval"), 
                sf.getLineNumber (null)
            );
            LocalVariable [] vars = sf.getLocalVariables ();
            assertEquals (
                "Wrong number of local variables", 
                1, vars.length
            );
            assertEquals (
                "Wrong info about local variables", 
                "im2", 
                vars [0].getName ()
            );
            assertEquals (
                "Wrong info about current method", 
                "m2", 
                sf.getMethodName ()
            );
            assertNotNull (
                "Wrong info about this object", 
                sf.getThisVariable ()
            );
            assertFalse (
                "Wrong info about obsolete method", 
                sf.isObsolete ()
            );

            JPDAThread thread = sf.getThread ();
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getCallStack () [0], 
                sf
            );
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getClassName (), 
                sf.getClassName ()
            );
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getMethodName (), 
                sf.getMethodName ()
            );
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getSourceName (null), 
                sf.getSourceName (null)
            );
        } finally {
            if (support != null)
                support.doFinish ();
        }
    }

    public void testStaticCallStackInfo() throws Exception {
        JPDASupport support = null;
        try {
            JPDASupport.removeAllBreakpoints ();
            Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src") + 
                    "org/netbeans/api/debugger/jpda/testapps/CallStackApp.java");
            LineBreakpoint lb = bp.getLineBreakpoints().get(0);
            lb.setPreferredClassName("org.netbeans.api.debugger.jpda.testapps.CallStackApp");
            DebuggerManager.getDebuggerManager ().addBreakpoint (lb);
            support = JPDASupport.attach (
                "org.netbeans.api.debugger.jpda.testapps.CallStackApp"
            );
            support.waitState (JPDADebugger.STATE_STOPPED);
            CallStackFrame sf = support.getDebugger ().
                getCurrentCallStackFrame ();

            List strata = sf.getAvailableStrata ();
            assertEquals (
                "Available strata", 1, strata.size ()
            );
            assertEquals (
                "Java stratum is not available", "Java", strata.get (0)
            );
            assertEquals (
                "Java stratum is not default", "Java", sf.getDefaultStratum ()
            );
            assertEquals (
                "Wrong class name", 
                "org.netbeans.api.debugger.jpda.testapps.CallStackApp", 
                sf.getClassName ()
            );
            assertEquals (
                "Wrong line number", 
                lb.getLineNumber(), 
                sf.getLineNumber (null)
            );

            LocalVariable [] vars = sf.getLocalVariables ();
            assertEquals (
                "Wrong number of local variables", 1, vars.length
            );
            assertEquals (
                "Wrong info about local variables", 
                "args", 
                vars[0].getName ()
            );
            assertEquals (
                "Wrong info about current method", 
                "main", 
                sf.getMethodName ()
            );
            assertNull (
                "Wrong info about this object", sf.getThisVariable ()
            );
            assertFalse (
                "Wrong info about obsolete method", sf.isObsolete ()
            );

            JPDAThread thread = sf.getThread();
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getCallStack () [0], 
                sf
            );
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getClassName (), 
                sf.getClassName ()
            );
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getMethodName (), 
                sf.getMethodName ()
            );
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getSourceName (null), 
                sf.getSourceName (null)
            );
        } finally {
            if (support != null)
                support.doFinish ();
        }
    }
}
