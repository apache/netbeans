/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.util.test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import static junit.framework.TestCase.*;
import junit.framework.AssertionFailedError;

/**
 * A scriptable property change listener.
 */
public class MockPropertyChangeListener implements PropertyChangeListener {

    private final List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
    private final Set<String> whitelist;
    private final Set<String> blacklist = new HashSet<String>();
    private final Set<String> ignored = new HashSet<String>();
    private String msg;

    /**
     * Makes a fresh listener.
     * @param whiteListedPropertyNames an optional list of property names; if any others are received, an assertion will be thrown
     */
    public MockPropertyChangeListener(String... whitelistedPropertyNames) {
        whitelist = whitelistedPropertyNames.length > 0 ? new HashSet<String>(Arrays.asList(whitelistedPropertyNames)) : null;
    }

    /**
     * Marks certain property names as being definitely not expected.
     * If any are received henceforth, an assertion will be thrown.
     */
    public synchronized void blacklist(String... blacklistedPropertyNames) {
        assertNull("Meaningless to blacklist some properties while there is an active whitelist", whitelist);
        blacklist.addAll(Arrays.asList(blacklistedPropertyNames));
    }

    /**
     * Marks certain property names as being ignored.
     * If any are received henceforth, no action will be taken.
     */
    public synchronized void ignore(String... ignoredPropertyNames) {
        ignored.addAll(Arrays.asList(ignoredPropertyNames));
    }

    public synchronized void propertyChange(PropertyChangeEvent ev) {
        String propname = ev.getPropertyName();
        if (ignored.contains(propname)) {
            return;
        }
        assertTrue("Property name " + propname + " not expected", whitelist == null || whitelist.contains(propname));
        assertFalse("Property name " + propname + " not expected", blacklist.contains(propname));
        if (propname == null) {
            assertNull("null prop name -> null old value", ev.getOldValue());
            assertNull("null prop name -> null new value", ev.getNewValue());
        }
        events.add(ev);
        notifyAll();
    }

    /**
     * Specifies a failure message to use for the next assertion.
     * @return this object, for convenient chaining
     */
    public MockPropertyChangeListener msg(String msg) {
        this.msg = msg;
        return this;
    }

    private String compose(String msg) {
        return this.msg == null ? msg : msg + ": " + this.msg;
    }

    /**
     * Asserts that the set of property change event names fired matches an expected list.
     * (Order and multiplicity of events is not considered.)
     * After this call, the list of received events is reset, so each call checks events since the last call.
     */
    public synchronized void assertEvents(String... expectedPropertyNames) throws AssertionFailedError {
        try {
            Set<String> actualEvents = new TreeSet<String>();
            for (PropertyChangeEvent ev : events) {
                actualEvents.add(ev.getPropertyName());
            }
            assertEquals(msg, new TreeSet<String>(Arrays.asList(expectedPropertyNames)).toString(), actualEvents.toString());
        } finally {
            reset();
        }
    }

    /**
     * Asserts that a certain number of events have been received.
     * (Property name is not considered.)
     * After this call, the list of received events is reset, so each call checks events since the last call.
     */
    public synchronized void assertEventCount(int expectedCount) throws AssertionFailedError {
        try {
            assertEquals(msg, expectedCount, events.size());
        } finally {
            reset();
        }
    }

    /**
     * Asserts that some events were received with particular old and new values.
     * After this call, the list of received events is reset, so each call checks events since the last call.
     * @param expectedOldValues mapping from expected property names to old values (may be left null to skip this check)
     * @param expectedNewValues mapping from expected property names to new values
     */
    public synchronized void assertEventsAndValues(Map<String,?> expectedOldValues, Map<String,?> expectedNewValues) throws AssertionFailedError {
        try {
            if (expectedOldValues != null) {
                Map<String,Object> actualOldValues = new HashMap<String,Object>();
                for (PropertyChangeEvent ev : events) {
                    actualOldValues.put(ev.getPropertyName(), ev.getOldValue());
                }
                assertEquals(msg, expectedOldValues, actualOldValues);
            }
            Map<String,Object> actualNewValues = new HashMap<String,Object>();
            for (PropertyChangeEvent ev : events) {
                actualNewValues.put(ev.getPropertyName(), ev.getNewValue());
            }
            assertEquals(msg, expectedNewValues, actualNewValues);
        } finally {
            reset();
        }
    }

    /**
     * Expects a single event to be fired.
     * After this call, the list of received events is reset, so each call checks events since the last call.
     * @param propertyName optional property name which is expected (or null for any)
     * @param timeout a timeout in milliseconds (zero means no timeout)
     */
    public synchronized void expectEvent(String expectedPropertyName, long timeout) throws AssertionFailedError {
        try {
            if (events.isEmpty()) {
                try {
                    wait(timeout);
                } catch (InterruptedException x) {
                    fail(compose("Timed out waiting for event"));
                }
            }
            assertFalse(compose("Did not get event"), events.isEmpty());
            assertFalse(compose("Got too many events"), events.size() > 1);
            PropertyChangeEvent received = events.iterator().next();
            if (expectedPropertyName != null) {
                assertEquals(msg, expectedPropertyName, received.getPropertyName());
            }
        } finally {
            reset();
        }
    }

    /**
     * Gets a list of all received events, for special processing.
     * (For example, checking of source, ...)
     * After this call, the list of received events is reset, so each call checks events since the last call.
     */
    public synchronized List<PropertyChangeEvent> allEvents() {
        try {
            return new ArrayList<PropertyChangeEvent>(events);
        } finally {
            reset();
        }
    }

    /**
     * Simply resets the list of events without checking anything.
     * Also resets any failure message.
     */
    public synchronized void reset() {
        msg = null;
        events.clear();
    }

}
