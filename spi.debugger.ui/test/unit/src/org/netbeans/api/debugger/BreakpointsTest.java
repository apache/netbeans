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

package org.netbeans.api.debugger;

import org.netbeans.api.debugger.test.TestDebuggerManagerListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Tests adding and removing of breakpoints and firing of breakpoint events.
 *
 * @author Maros Sandor
 */
public class BreakpointsTest extends DebuggerApiTestBase {

    public BreakpointsTest(String s) {
        super(s);
    }

    public void teBreakpoints() throws Exception {
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        TestBreakpoint tb = new TestBreakpoint();
        TestDebuggerManagerListener dml = new TestDebuggerManagerListener();
        dm.addDebuggerListener(dml);

        initBreakpoints(dm, dml);
        addBreakpoint(dm, tb, dml);
        addBreakpoint(dm, tb, dml);
        addBreakpoint(dm, tb, dml);
        removeBreakpoint(dm, tb, dml);
        removeBreakpoint(dm, tb, dml);
        addBreakpoint(dm, tb, dml);
        removeBreakpoint(dm, tb, dml);
        addBreakpoint(dm, tb, dml);
        removeBreakpoint(dm, tb, dml);
        removeBreakpoint(dm, tb, dml);

        dm.removeDebuggerListener(dml);
    }
    
    public void testBreakpointValidity() throws Exception {
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        TestBreakpoint tb = new TestBreakpoint();
        TestDebuggerManagerListener dml = new TestDebuggerManagerListener();
        dm.addDebuggerListener(dml);

        initBreakpoints(dm, dml);
        addBreakpoint(dm, tb, dml);
        assertEquals("Wrong initial validity", Breakpoint.VALIDITY.UNKNOWN, tb.getValidity());
        final PropertyChangeEvent[] propEventPtr = new PropertyChangeEvent[] { null };
        tb.addPropertyChangeListener(Breakpoint.PROP_VALIDITY, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                propEventPtr[0] = evt;
            }
        });
        tb.doSetValidity(Breakpoint.VALIDITY.VALID, null);
        assertNotNull("Got no prop change event!", propEventPtr[0]);
        assertEquals("Bad event, unexpected new validity", Breakpoint.VALIDITY.VALID, propEventPtr[0].getNewValue());
        assertEquals("Unexpected validity", Breakpoint.VALIDITY.VALID, tb.getValidity());
        propEventPtr[0] = null;
        tb.doSetValidity(Breakpoint.VALIDITY.INVALID, "Some crazy breakpoint");
        assertNotNull("Got no prop change event!", propEventPtr[0]);
        assertEquals("Bad event, unexpected new validity", Breakpoint.VALIDITY.INVALID, propEventPtr[0].getNewValue());
        assertEquals("Unexpected validity", Breakpoint.VALIDITY.INVALID, tb.getValidity());
        assertEquals("Unexpected reason", "Some crazy breakpoint", tb.getValidityMessage());
    }
    
    public void testDependentBreakpoints() throws Exception {
        Breakpoint noDependentBP = new TestBreakpoint();
        assertFalse(noDependentBP.canHaveDependentBreakpoints());
        try {
            noDependentBP.setBreakpointsToDisable(Collections.singleton(noDependentBP));
            assert false : "An UnsupportedOperationException was not thrown.";
        } catch (UnsupportedOperationException uoex) {
            // O.K.
        }
        Breakpoint b1 = new TestDependentBreakpoint();
        assertTrue(b1.canHaveDependentBreakpoints());
        Breakpoint b2 = new TestDependentBreakpoint();
        b1.setBreakpointsToEnable(Collections.singleton(b2));
        b2.setBreakpointsToDisable(new HashSet<Breakpoint>(Arrays.asList(b1, b2)));
        assertEquals("BreakpointsToEnable size", 1, b1.getBreakpointsToEnable().size());
        assertEquals("BreakpointsToEnable is correct", b2, b1.getBreakpointsToEnable().iterator().next());
        assertEquals("BreakpointsToDisable size", 0, b1.getBreakpointsToDisable().size());
        assertEquals("BreakpointsToDisable size", 2, b2.getBreakpointsToDisable().size());
    }

    private void initBreakpoints(DebuggerManager dm, TestDebuggerManagerListener dml) {
        dm.getBreakpoints();    // trigger the "breakpointsInit" property change
        TestDebuggerManagerListener.Event event;
        List events = dml.getEvents();
        assertEquals("Wrong PCS", 0, events.size());
        /*
        event = (TestDebuggerManagerListener.Event) events.get(0);
        assertEquals("Wrong PCS", "propertyChange", event.getName());
        PropertyChangeEvent pce = (PropertyChangeEvent) event.getParam();
        assertEquals("Wrong PCE name", "breakpointsInit", pce.getPropertyName());
         */
    }

    private void removeBreakpoint(DebuggerManager dm, TestBreakpoint tb, TestDebuggerManagerListener dml) {
        List events;
        TestDebuggerManagerListener.Event event;
        Breakpoint [] bpts;

        int bptSize = dm.getBreakpoints().length;
        dm.removeBreakpoint(tb);
        events = dml.getEvents();
        assertEquals("Wrong PCS", 2, events.size());
        assertTrue("Wrong PCS", events.remove(new TestDebuggerManagerListener.Event("breakpointRemoved", tb)));
        event = (TestDebuggerManagerListener.Event) events.get(0);
        assertEquals("Wrong PCS", "propertyChange", event.getName());
        PropertyChangeEvent pce = (PropertyChangeEvent) event.getParam();
        assertEquals("Wrong PCE name", "breakpoints", pce.getPropertyName());
        bpts = dm.getBreakpoints();
        assertEquals("Wrong number of installed breakpoionts", bptSize - 1, bpts.length);
    }

    private void addBreakpoint(DebuggerManager dm, TestBreakpoint tb, TestDebuggerManagerListener dml) {
        List events;
        TestDebuggerManagerListener.Event event;
        Breakpoint [] bpts;

        int bptSize = dm.getBreakpoints().length;
        dm.addBreakpoint(tb);
        events = dml.getEvents();
        assertEquals("Wrong PCS", 2, events.size());
        assertTrue("Wrong PCS", events.remove(new TestDebuggerManagerListener.Event("breakpointAdded", tb)));
        event = (TestDebuggerManagerListener.Event) events.get(0);
        assertEquals("Wrong PCS", "propertyChange", event.getName());
        PropertyChangeEvent pce = (PropertyChangeEvent) event.getParam();
        assertEquals("Wrong PCE name", "breakpoints", pce.getPropertyName());
        bpts = dm.getBreakpoints();
        assertEquals("Wrong number of installed breakpoints", bptSize + 1, bpts.length);
    }

    class TestBreakpoint extends Breakpoint
    {
        private boolean isEnabled;

        public boolean isEnabled() {
            return isEnabled;
        }

        public void disable() {
            isEnabled = false;
        }

        public void enable() {
            isEnabled = true;
        }
        
        public void doSetValidity(Breakpoint.VALIDITY validity, String reason) {
            setValidity(validity, reason);
        }

    }
    
    private class TestDependentBreakpoint extends Breakpoint {

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void disable() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void enable() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean canHaveDependentBreakpoints() {
            return true;
        }
        
    }
}
