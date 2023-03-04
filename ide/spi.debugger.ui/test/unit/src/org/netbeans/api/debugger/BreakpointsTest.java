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
