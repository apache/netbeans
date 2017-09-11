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
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.junit.NbTestCase;

/**
 * Tests thread breakpoints.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class ThreadBreakpointTest extends NbTestCase {

    private JPDASupport     support;
    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();

    private static final String CLASS_NAME = 
        "org.netbeans.api.debugger.jpda.testapps.ThreadBreakpointApp";

    
    public ThreadBreakpointTest (String s) {
        super (s);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(ThreadBreakpointTest.class);
    }
    
    public void testThreadBreakpoints () throws Exception {
        try {
            ThreadBreakpoint tb1 = ThreadBreakpoint.create ();
            tb1.setBreakpointType (ThreadBreakpoint.TYPE_THREAD_STARTED_OR_DEATH);
            TestBreakpointListener tbl = new TestBreakpointListener (10);
            tb1.addJPDABreakpointListener (tbl);
            dm.addBreakpoint (tb1);

            support = JPDASupport.attach (CLASS_NAME);

            for (int i = 0; ; i++) {
                if (tbl.getFailure() != null) {
                    throw tbl.getFailure();
                }
                if (i > 100) {
                    throw new AssertionError("Too many cycles of resume, continue does not seem to resume the app to finish.");
                }
                support.waitState (JPDADebugger.STATE_STOPPED);
                if (support.getDebugger ().getState () == 
                    JPDADebugger.STATE_DISCONNECTED
                ) break;
                support.doContinue ();
                Thread.sleep(100);
            }
            tbl.assertFailure ();

            dm.removeBreakpoint (tb1);
        } finally {
            support.doFinish();
        }
    }

    private class TestBreakpointListener implements JPDABreakpointListener {

        private int                 hitCount;
        private AssertionError      failure;
        private int                 expectedHitCount;

        public TestBreakpointListener (int expectedHitCount) {
            this.expectedHitCount = expectedHitCount;
        }

        public void breakpointReached (JPDABreakpointEvent event) {
            //System.err.println("Thread Breakpoint hit, hitCount was = "+hitCount+", failure was "+failure);
            try {
                checkEvent (event);
            } catch (AssertionError e) {
                failure = e;
            } catch (Throwable e) {
                failure = new AssertionError (e);
            }
        }
        
        public AssertionError getFailure() {
            return failure;
        }

        private void checkEvent (JPDABreakpointEvent event) {
//            ThreadBreakpoint tb = (ThreadBreakpoint) event.getSource();
//            assertEquals (
//                "Breakpoint event: Condition evaluation failed", 
//                JPDABreakpointEvent.CONDITION_NONE, 
//                event.getConditionResult ()
//            );
            assertNotNull (
                "Breakpoint event: Context thread is null", 
                event.getThread ()
            );
            JPDAThread thread = event.getThread ();
            if (thread.getName ().startsWith ("test-")) {
                JPDAThreadGroup group = thread.getParentThreadGroup ();
                assertEquals (
                    "Wrong thread group", 
                    "testgroup", 
                    group.getName ()
                );
                assertEquals (
                    "Wrong parent thread group", 
                    "main", 
                    group.getParentThreadGroup ().getName ()
                );
                assertEquals (
                    "Wrong number of child thread groups", 
                    0, 
                    group.getThreadGroups ().length
                );
                JPDAThread [] threads = group.getThreads ();
                for (int i = 0; i < threads.length; i++) {
                    JPDAThread jpdaThread = threads [i];
                    if ( !jpdaThread.getName ().startsWith ("test-")) 
                        throw new AssertionError 
                            ("Thread group contains an alien thread");
                    assertSame (
                        "Child/parent mismatch", 
                        jpdaThread.getParentThreadGroup (), 
                        group
                    );
                }
                hitCount++;
            }
            if (thread.getName ().startsWith ("DestroyJavaVM")) {
                // Wait a while to gather all events.
                try {
                    Thread.sleep(500);
                } catch (InterruptedException iex) {}
            }
        }

        public void assertFailure () {
            if (failure != null) throw failure;
            assertEquals (
                "Breakpoint hit count mismatch", 
                expectedHitCount, 
                hitCount
            );
        }
    }
}
