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

package org.openide.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.openide.util.ChangeSupport;
import org.openide.util.test.MockChangeListener;

/**
 *
 * @author Andrei Badea
 */
public class ChangeSupportTest extends NbTestCase {

    public ChangeSupportTest(String testName) {
        super(testName);
    }

    public void testChangeSupport() {
        final int[] changeCount = { 0 };
        ChangeSupport support = new ChangeSupport(this);
        MockChangeListener listener1 = new MockChangeListener(), listener2 = new MockChangeListener();

        support.addChangeListener(null);
        assertFalse(support.hasListeners());

        support.removeChangeListener(null);
        assertFalse(support.hasListeners());

        support.addChangeListener(listener1);
        support.addChangeListener(listener2);
        assertTrue(support.hasListeners());
        Set<ChangeListener> listeners = new HashSet<ChangeListener>(support.listeners);
        assertEquals(2, listeners.size());
        assertTrue(listeners.contains(listener1));
        assertTrue(listeners.contains(listener2));

        support.fireChange();
        List<ChangeEvent> events = listener1.allEvents();
        assertEquals(1, events.size());
        listener2.assertEventCount(1);
        assertSame(this, events.iterator().next().getSource());

        support.removeChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
            }
        });
        support.fireChange();
        listener1.assertEventCount(1);
        listener2.assertEventCount(1);

        support.removeChangeListener(listener1);
        support.fireChange();
        listener1.assertEventCount(0);
        listener2.assertEventCount(1);

        support.addChangeListener(listener2);
        support.fireChange();
        listener2.assertEventCount(2);

        support.removeChangeListener(listener2);
        support.fireChange();
        listener2.assertEventCount(1);

        support.removeChangeListener(listener2);
        support.fireChange();
        listener2.assertEventCount(0);
        assertFalse(support.hasListeners());
    }
}
