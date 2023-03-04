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

package org.openide.util.test;

import java.beans.PropertyChangeSupport;
import java.util.Collections;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

// copy of openide.util
public class MockPropertyChangeListenerTest extends TestCase {

    public MockPropertyChangeListenerTest(String n) {
        super(n);
    }

    Object source;
    PropertyChangeSupport pcs;
    MockPropertyChangeListener l;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        source = new Object();
        pcs = new PropertyChangeSupport(source);
        l = new MockPropertyChangeListener();
        pcs.addPropertyChangeListener(l);
    }

    // XXX test expect

    public void testBasicUsage() throws Exception {
        l.assertEvents();
        try {
            l.assertEvents("whatever");
            assert false;
        } catch (AssertionFailedError e) {}
        pcs.firePropertyChange("foo", null, null);
        l.assertEvents("foo");
        try {
            l.assertEvents("foo");
            assert false;
        } catch (AssertionFailedError e) {}
        l.assertEventCount(0);
        pcs.firePropertyChange("bar", null, null);
        pcs.firePropertyChange("baz", null, null);
        l.assertEventCount(2);
        try {
            l.assertEventCount(2);
            assert false;
        } catch (AssertionFailedError e) {}
        assertEquals(0, l.allEvents().size());
        pcs.firePropertyChange("bar", null, null);
        pcs.firePropertyChange("baz", null, null);
        assertEquals(2, l.allEvents().size());
        assertEquals(0, l.allEvents().size());
        pcs.firePropertyChange("foo", "old", "new");
        l.assertEventsAndValues(null, Collections.singletonMap("foo", "new"));
        pcs.firePropertyChange("foo", "old2", "new2");
        l.assertEventsAndValues(Collections.singletonMap("foo", "old2"), Collections.singletonMap("foo", "new2"));
        try {
            l.assertEventsAndValues(null, Collections.singletonMap("foo", "new2"));
            assert false;
        } catch (AssertionFailedError e) {}
        pcs.firePropertyChange("foo", null, null);
        l.reset();
        l.assertEvents();
        pcs.firePropertyChange("x", null, null);
        try {
            l.assertEvents();
            assert false;
        } catch (AssertionFailedError e) {}
        l.assertEvents();
    }

    public void testMessages() throws Exception {
        pcs.firePropertyChange("foo", null, null);
        try {
            l.assertEvents();
            assert false;
        } catch (AssertionFailedError e) {}
        pcs.firePropertyChange("foo", null, null);
        try {
            l.msg("stuff").assertEvents();
            assert false;
        } catch (AssertionFailedError e) {
            assertTrue(e.getMessage().contains("stuff"));
        }
        pcs.firePropertyChange("foo", null, null);
        try {
            l.assertEvents();
            assert false;
        } catch (AssertionFailedError e) {
            assertFalse(e.getMessage().contains("stuff"));
        }
    }

    public void testPropertyNameFiltering() throws Exception {
        l.ignore("irrelevant");
        l.blacklist("bad", "worse");
        pcs.firePropertyChange("relevant", null, null);
        l.assertEvents("relevant");
        pcs.firePropertyChange("irrelevant", null, null);
        l.assertEvents();
        try {
            pcs.firePropertyChange("bad", null, null);
            assert false;
        } catch (AssertionFailedError e) {}
        try {
            pcs.firePropertyChange("worse", null, null);
            assert false;
        } catch (AssertionFailedError e) {}
        pcs.removePropertyChangeListener(l);
        l = new MockPropertyChangeListener("expected1", "expected2");
        pcs.addPropertyChangeListener(l);
        l.ignore("irrelevant");
        pcs.firePropertyChange("expected1", null, null);
        pcs.firePropertyChange("expected2", null, null);
        l.assertEvents("expected1", "expected2");
        pcs.firePropertyChange("irrelevant", null, null);
        l.assertEvents();
        try {
            pcs.firePropertyChange("other", null, null);
            assert false;
        } catch (AssertionFailedError e) {}
    }

}
