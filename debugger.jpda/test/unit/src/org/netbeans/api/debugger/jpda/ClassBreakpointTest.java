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
 * Tests class breakpoints.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class ClassBreakpointTest extends NbTestCase {

    private JPDASupport     support;
    private JPDADebugger    debugger;
    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();

    private static final String CLASS_NAME = "org.netbeans.api.debugger.jpda.testapps.ClassBreakpointApp";

    public static Test suite() {
        return JPDASupport.createTestSuite(ClassBreakpointTest.class);
    }

    public ClassBreakpointTest (String s) {
        super (s);
    }

    public void testMethodBreakpoints() throws Exception {
        try {
            ClassLoadUnloadBreakpoint cb1 = ClassLoadUnloadBreakpoint.create("org.netbeans.api.debugger.jpda.testapps.ClassBreakpointTest1", false, ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED);
            TestBreakpointListener tbl = new TestBreakpointListener("org.netbeans.api.debugger.jpda.testapps.ClassBreakpointTest1", 1);
            cb1.addJPDABreakpointListener(tbl);
            dm.addBreakpoint(cb1);

            ClassLoadUnloadBreakpoint cb2 = ClassLoadUnloadBreakpoint.create("*", false, ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED);
            TestBreakpointListener tb2 = new TestBreakpointListener(null, -1);
            cb2.addJPDABreakpointListener(tb2);
            dm.addBreakpoint(cb2);

            support = JPDASupport.attach (CLASS_NAME);
            debugger = support.getDebugger();

            for (;;) {
                support.waitState (JPDADebugger.STATE_STOPPED);
                if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED) break;
                support.doContinue();
            }
            tbl.assertFailure();
            tb2.assertFailure();

            dm.removeBreakpoint(cb1);
            dm.removeBreakpoint(cb2);
        } finally {
            support.doFinish();
        }
    }

    private class TestBreakpointListener implements JPDABreakpointListener {

        private int                 hitCount;
        private AssertionError      failure;
        private String              className;
        private int                 expectedHitCount;

        public TestBreakpointListener(String className, int expectedHitCount) {
            this.className = className;
            this.expectedHitCount = expectedHitCount;
        }

        public void breakpointReached(JPDABreakpointEvent event) {
            try {
                checkEvent(event);
            } catch (AssertionError e) {
                failure = e;
            } catch (Throwable e) {
                failure = new AssertionError(e);
            }
        }

        private void checkEvent(JPDABreakpointEvent event) {
            ClassLoadUnloadBreakpoint cb = (ClassLoadUnloadBreakpoint) event.getSource();

            assertEquals("Breakpoint event: Condition evaluation failed", JPDABreakpointEvent.CONDITION_NONE, event.getConditionResult());
            assertNotNull("Breakpoint event: Context thread is null", event.getThread());
            assertNotNull("Breakpoint event: Reference type is null", event.getReferenceType());
            if (className != null)
            {
                assertEquals("Breakpoint event: Hit at wrong class", className, event.getReferenceType().name());
            }

            hitCount++;
        }

        public void assertFailure() {
            if (failure != null) throw failure;
            if (expectedHitCount != -1)
            {
                assertEquals("Breakpoint hit count mismatch for: " + className, expectedHitCount, hitCount);
            }
        }
    }
}
