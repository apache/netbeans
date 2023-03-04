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

package org.netbeans.modules.refactoring.api.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import static org.junit.Assert.*;
import org.netbeans.modules.refactoring.api.ProgressListener;

/**
 *
 * @author Jan Pokorsky
 */
public class ProgressSupportTest {

    public ProgressSupportTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDeterministicProgress() {
        System.out.println("testDeterministicProgress");
        PL listener = new PL();
        ProgressSupport instance = new ProgressSupport();
        instance.addProgressListener(listener);

        // start indeterminate progress
        instance.fireProgressListenerStart(this,ProgressEvent.START, 4);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.START, listener.event.getEventId());
        assertEquals("progress", 4, listener.event.getCount());
        listener.reset();

        // step
        instance.fireProgressListenerStep(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", 0, listener.event.getCount());
        listener.reset();

        // step
        instance.fireProgressListenerStep(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", 1, listener.event.getCount());
        listener.reset();

        // step to 3
        instance.fireProgressListenerStep(this, 3);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", 3, listener.event.getCount());
        listener.reset();

        // stop
        instance.fireProgressListenerStop(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STOP, listener.event.getEventId());
    }

    @Test
    public void testIndeterministicProgress() {
        System.out.println("testIndeterministicProgress");
        PL listener = new PL();
        ProgressSupport instance = new ProgressSupport();
        instance.addProgressListener(listener);

        // start indeterminate progress
        instance.fireProgressListenerStart(this,ProgressEvent.START, -1);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.START, listener.event.getEventId());
        assertEquals("progress", -1, listener.event.getCount());
        listener.reset();

        // step
        instance.fireProgressListenerStep(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", -1, listener.event.getCount());
        listener.reset();

        // step
        instance.fireProgressListenerStep(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", -1, listener.event.getCount());
        listener.reset();

        // stop
        instance.fireProgressListenerStop(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STOP, listener.event.getEventId());
    }

    @Test
    public void testIndeterministicToDeterministicProgress1() {
        System.out.println("testIndeterministicToDeterministicProgress1");
        PL listener = new PL();
        ProgressSupport instance = new ProgressSupport();
        instance.addProgressListener(listener);

        // start indeterminate progress
        instance.fireProgressListenerStart(this,ProgressEvent.START, -1);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.START, listener.event.getEventId());
        assertEquals("progress", -1, listener.event.getCount());
        listener.reset();

        // step
        instance.fireProgressListenerStep(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", -1, listener.event.getCount());
        listener.reset();

        // switch to deterministic progress of size 10
        instance.fireProgressListenerStep(this, 10);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", 10, listener.event.getCount());
        listener.reset();
        
        // step
        instance.fireProgressListenerStep(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", 0, listener.event.getCount());
        listener.reset();

        // stop
        instance.fireProgressListenerStop(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STOP, listener.event.getEventId());
    }

    @Test
    public void testIndeterministicToDeterministicProgress2() {
        System.out.println("testIndeterministicToDeterministicProgress2");
        PL listener = new PL();
        ProgressSupport instance = new ProgressSupport();
        instance.addProgressListener(listener);

        // start indeterminate progress
        instance.fireProgressListenerStart(this,ProgressEvent.START, -1);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.START, listener.event.getEventId());
        assertEquals("progress", -1, listener.event.getCount());
        listener.reset();

        // step
        instance.fireProgressListenerStep(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", -1, listener.event.getCount());
        listener.reset();

        // switch to deterministic progress of size 10
        instance.fireProgressListenerStep(this, 10);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", 10, listener.event.getCount());
        listener.reset();

        // step
        instance.fireProgressListenerStep(this, 5);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", 5, listener.event.getCount());
        listener.reset();

        // stop
        instance.fireProgressListenerStop(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STOP, listener.event.getEventId());
    }
    
    private static class PL implements ProgressListener {
        private ProgressEvent event;

        @Override
        public void start(ProgressEvent event) {
            this.event = event;
        }

        @Override
        public void step(ProgressEvent event) {
            this.event = event;
        }

        @Override
        public void stop(ProgressEvent event) {
            this.event = event;
        }
        
        public void reset() {
            this.event = null;
        }
    }

}
