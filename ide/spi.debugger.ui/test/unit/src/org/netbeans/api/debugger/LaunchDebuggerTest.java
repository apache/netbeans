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

import org.netbeans.api.debugger.test.TestDICookie;
import org.netbeans.api.debugger.test.TestDebuggerManagerListener;
import org.netbeans.api.debugger.test.TestLazyDebuggerManagerListener;

import java.util.*;
import java.beans.PropertyChangeEvent;

/**
 * Launches and finishes a debugger session. Tests services registration and lookup and event firing.
 *
 * @author Maros Sandor
 */
public class LaunchDebuggerTest extends DebuggerApiTestBase {

    public LaunchDebuggerTest(String s) {
        super(s);
    }

    public void testLookup () throws Exception {

        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        TestDebuggerManagerListener dml = new TestDebuggerManagerListener();
        dm.addDebuggerListener(dml);

        TestLazyDebuggerManagerListener ldml = null;
        for (LazyDebuggerManagerListener _ldml : dm.lookup(null, LazyDebuggerManagerListener.class)) {
            if (_ldml instanceof TestLazyDebuggerManagerListener) {
                ldml = (TestLazyDebuggerManagerListener) _ldml;
                break;
            }
        }
        assertNotNull("Lazy debugger manager listener not loaded", ldml);

        Map args = new HashMap();
        TestDICookie tdi = TestDICookie.create(args);

        Object [] services = new Object[] { tdi, this };
        DebuggerInfo di = DebuggerInfo.create(TestDICookie.ID, services);

        DebuggerEngine engines [] = dm.startDebugging(di);
        assertEquals("Wrong number of debugger engines started", engines.length, 1);

        testStartEvents(dml, engines);
        testStartEvents(ldml, engines);

        DebuggerEngine debugger = engines[0];
        DebuggerInfo dic = debugger.lookupFirst(null, DebuggerInfo.class);
        assertSame("Wrong debugger info in engine lookup", dic, di);
        assertTrue("Engine did not start", tdi.hasInfo(ActionsManager.ACTION_START));

        DebuggerActionsTest.kill(dm.getCurrentSession());
        assertTrue("Engine did not finish", tdi.hasInfo(ActionsManager.ACTION_KILL));

        testKillEvents(dml);
        testKillEvents(ldml);

        dm.removeDebuggerListener(dml);
    }

    private void testKillEvents(TestDebuggerManagerListener dml) {
        List events;
        TestDebuggerManagerListener.Event event;
        events = dml.getEvents();
        assertEquals("Wrong number of events generated", events.size(), 5);
        for (Iterator i = events.iterator(); i.hasNext();) {
            event = (TestDebuggerManagerListener.Event) i.next();
            if (event.getName().equals("sessionRemoved")) {
                i.remove();
            } else if (event.getName().equals("propertyChange")) {
                PropertyChangeEvent pce = (PropertyChangeEvent) event.getParam();
                if (pce.getPropertyName().equals("sessions")) {
                    i.remove();
                } else if (pce.getPropertyName().equals("debuggerEngines")) {
                    i.remove();
                } else if (pce.getPropertyName().equals("currentEngine")) {
                    assertNull("Bad current engine", pce.getNewValue());
                    i.remove();
                } else if (pce.getPropertyName().equals("currentSession")) {
                    assertNull("Bad current session", pce.getNewValue());
                    i.remove();
                }
            }
        }
        assertEquals("Wrong events generated", events.size(), 0);
    }

    private void testStartEvents(TestDebuggerManagerListener dml, DebuggerEngine[] engines) {
        List events;
        TestDebuggerManagerListener.Event event;
        events = dml.getEvents();
        assertEquals("Wrong number of events generated", events.size(), 5);
        for (Iterator i = events.iterator(); i.hasNext();) {
            event = (TestDebuggerManagerListener.Event) i.next();
            if (event.getName().equals("sessionAdded")) {
                i.remove();
            } else if (event.getName().equals("propertyChange")) {
                PropertyChangeEvent pce = (PropertyChangeEvent) event.getParam();
                if (pce.getPropertyName().equals("sessions")) {
                    i.remove();
                } else if (pce.getPropertyName().equals("debuggerEngines")) {
                    i.remove();
                } else if (pce.getPropertyName().equals("currentEngine")) {
                    assertSame("Bad PCE new current engine", pce.getNewValue(), engines[0]);
                    i.remove();
                } else if (pce.getPropertyName().equals("currentSession")) {
                    i.remove();
                }
            }
        }
        assertEquals("Wrong events generated", events.size(), 0);
    }
}
