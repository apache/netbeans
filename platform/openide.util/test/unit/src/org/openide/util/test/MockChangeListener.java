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

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import static junit.framework.TestCase.*;
import junit.framework.AssertionFailedError;

/**
 * A scriptable change listener.
 */
public class MockChangeListener implements ChangeListener {

    private final List<ChangeEvent> events = new ArrayList<ChangeEvent>();
    private String msg;

    /**
     * Makes a fresh listener.
     */
    public MockChangeListener() {}

    public synchronized void stateChanged(ChangeEvent ev) {
        events.add(ev);
        notifyAll();
    }

    /**
     * Specifies a failure message to use for the next assertion.
     * @return this object, for convenient chaining
     */
    public synchronized MockChangeListener msg(String msg) {
        this.msg = msg;
        return this;
    }

    private String compose(String msg) {
        assert Thread.holdsLock(this);
        return this.msg == null ? msg : msg + ": " + this.msg;
    }

    /**
     * Asserts that at least one change event has been fired.
     * After this call, the count is reset, so each call checks events since the last call.
     */
    public synchronized void assertEvent() throws AssertionFailedError {
        try {
            assertFalse(msg, events.isEmpty());
        } finally {
            reset();
        }
    }

    /**
     * Asserts that no change events have been fired.
     */
    public synchronized void assertNoEvents() throws AssertionFailedError {
        try {
            assertTrue(msg, events.isEmpty());
        } finally {
            reset();
        }
    }

    /**
     * Asserts that a certain number of change events has been fired.
     * After this call, the count is reset, so each call checks events since the last call.
     */
    public synchronized void assertEventCount(int expectedCount) throws AssertionFailedError {
        try {
            assertEquals(msg, expectedCount, events.size());
        } finally {
            reset();
        }
    }

    /**
     * Expects a single event to be fired.
     * After this call, the list of received events is reset, so each call checks events since the last call.
     * @param timeout a timeout in milliseconds (zero means no timeout)
     */
    public synchronized void expectEvent(long timeout) throws AssertionFailedError {
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
        } finally {
            reset();
        }
    }

    /**
     * Expects no further events to be fired.
     * After this call, the list of received events is reset, so each call checks events since the last call.
     * @param timeout a timeout in milliseconds (will fail if an event is received within this time)
     */
    public synchronized void expectNoEvents(long timeout) throws AssertionFailedError {
        if (timeout == 0) {
            throw new IllegalArgumentException("Cannot use zero timeout");
        }
        try {
            if (!events.isEmpty()) {
                fail(compose("Already had an event"));
            }
            try {
                wait(timeout);
            } catch (InterruptedException x) {
                fail(compose("Interrupted"));
            }
            if (!events.isEmpty()) {
                fail(compose("Got an event"));
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
    public synchronized List<ChangeEvent> allEvents() {
        try {
            return new ArrayList<ChangeEvent>(events);
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
