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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

/**
 * Tests DebuggerManager's Watches management.
 *
 * @author Maros Sandor
 */
public class WatchesTest extends DebuggerApiTestBase {

    public WatchesTest(String s) {
        super(s);
    }

    public void testWatches() throws Exception {
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        TestDebuggerManagerListener dml = new TestDebuggerManagerListener();

        dm.addDebuggerListener(dml);

        initWatches(dm, dml);
        Watch w1 = addWatch(dm, dml);
        Watch w2 = addWatch(dm, dml);
        Watch w3 = addWatch(dm, dml);
        removeWatch(dm, w2, dml);
        removeWatch(dm, w3, dml);
        Watch w4 = addWatch(dm, dml);
        removeWatch(dm, w1, dml);
        Watch w5 = addWatch(dm, dml);
        removeWatch(dm, w5, dml);
        removeWatch(dm, w4, dml);

        dm.removeDebuggerListener(dml);
    }

    private void initWatches(DebuggerManager dm, TestDebuggerManagerListener dml) {
        dm.getWatches();    // trigger the "watchesInit" property change
        TestDebuggerManagerListener.Event event;
        List events = dml.getEvents();
        assertEquals("Wrong PCS", 1, events.size());
        event = (TestDebuggerManagerListener.Event) events.get(0);
        assertEquals("Wrong PCS", "propertyChange", event.getName());
        PropertyChangeEvent pce = (PropertyChangeEvent) event.getParam();
        assertEquals("Wrong PCE name", "watchesInit", pce.getPropertyName());
    }

    private void removeWatch(DebuggerManager dm, Watch w, TestDebuggerManagerListener dml) {
        List events;
        TestDebuggerManagerListener.Event event;
        Watch [] watches = dm.getWatches();

        dm.removeWatch(w);
        events = dml.getEvents();
        assertEquals("Wrong PCS", 2, events.size());
        assertTrue("Wrong PCS", events.remove(new TestDebuggerManagerListener.Event("watchRemoved", w)));
        event = (TestDebuggerManagerListener.Event) events.get(0);
        assertEquals("Wrong PCS", "propertyChange", event.getName());
        PropertyChangeEvent pce = (PropertyChangeEvent) event.getParam();
        assertEquals("Wrong PCE name", "watches", pce.getPropertyName());
        Watch [] newWatches = dm.getWatches();
        for (int i = 0; i < newWatches.length; i++) {
            assertNotSame("Watch was not removed", newWatches[i], w);
        }
        assertEquals("Wrong number of installed watches", watches.length - 1, newWatches.length);
    }

    private Watch addWatch(DebuggerManager dm, TestDebuggerManagerListener dml) {
        List events;
        TestDebuggerManagerListener.Event event;

        int watchesSize = dm.getWatches().length;
        Watch newWatch = dm.createWatch("watch");
        events = dml.getEvents();
        assertEquals("Wrong PCS", 2, events.size());
        assertTrue("Wrong PCS", events.remove(new TestDebuggerManagerListener.Event("watchAdded", newWatch)));
        event = (TestDebuggerManagerListener.Event) events.get(0);
        assertEquals("Wrong PCS", "propertyChange", event.getName());
        PropertyChangeEvent pce = (PropertyChangeEvent) event.getParam();
        assertEquals("Wrong PCE name", "watches", pce.getPropertyName());
        Watch [] watches = dm.getWatches();
        assertEquals("Wrong number of installed watches", watchesSize + 1, watches.length);
        return newWatch;
    }

    public void testWatchesReorder() throws Exception {
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        dm.createWatch("w1");
        dm.createWatch(0, "w0");

        Watch[] watches = dm.getWatches();
        assertEquals("w0", watches[0].getExpression());

        boolean exThrown = false;
        try {
            dm.createWatch(100, "w100");
        } catch (ArrayIndexOutOfBoundsException aioobex) {
            exThrown = true;
        }
        assertTrue(exThrown);
        dm.removeAllWatches();

        for (int i = 0; i < 5; i++) {
            dm.createWatch(i, "w"+(i+1));
        }
        dm.reorderWatches(new int[] { 2, 0, 1, 4, 3 });
        String[] reorderedWatches = new String[] { "w2", "w3", "w1", "w5", "w4" };
        watches = dm.getWatches();
        String watchesStr = Arrays.toString(watches);
        for (int i = 0; i < 5; i++) {
            assertEquals(watchesStr, reorderedWatches[i], watches[i].getExpression());
        }

        exThrown = false;
        try {
            dm.reorderWatches(new int[] { 2, 0, 1, 4, 3, 5 });
        } catch (IllegalArgumentException iaex) {
            exThrown = true;
        }
        assertTrue(exThrown);

        exThrown = false;
        try {
            dm.reorderWatches(new int[] { 2, 0, 1 });
        } catch (IllegalArgumentException iaex) {
            exThrown = true;
        }
        assertTrue(exThrown);

        exThrown = false;
        try {
            dm.reorderWatches(new int[] { 2, 0, 1, 0, 3 });
        } catch (IllegalArgumentException iaex) {
            exThrown = true;
        }
        assertTrue(exThrown);
    }
    
    public void testEnableDisable() throws Exception {
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        Watch w1 = dm.createWatch("w1");
        assertTrue(w1.isEnabled()); // Watches are enabled by default.
        final PropertyChangeEvent[] eventPtr = new PropertyChangeEvent[] { null };
        w1.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                eventPtr[0] = evt;
            }
        });
        w1.setEnabled(false);
        assertNotNull("Event not fired when watch disabled.", eventPtr[0]);
        assertEquals(Watch.PROP_ENABLED, eventPtr[0].getPropertyName());
        assertEquals(false, eventPtr[0].getNewValue());
        assertFalse(w1.isEnabled());
        eventPtr[0] = null;
        w1.setEnabled(true);
        assertNotNull("Event not fired when watch enabled.", eventPtr[0]);
        assertEquals(Watch.PROP_ENABLED, eventPtr[0].getPropertyName());
        assertEquals(true, eventPtr[0].getNewValue());
        assertTrue(w1.isEnabled());
    }
    
    public void testWatchPersistence() throws Exception {
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        Watch w1 = dm.createWatch("w1");
        Properties p = Properties.getDefault();
        p.setObject("watch1", w1);
        Watch w2 = dm.createWatch("w2");
        w2.setEnabled(false);
        p.setObject("watch2", w2);
        
        w1 = (Watch) p.getObject("watch1", null);
        assertEquals("w1", w1.getExpression());
        assertEquals(true, w1.isEnabled());
        w2 = (Watch) p.getObject("watch2", null);
        assertEquals("w2", w2.getExpression());
        assertEquals(false, w2.isEnabled());
    }

}
