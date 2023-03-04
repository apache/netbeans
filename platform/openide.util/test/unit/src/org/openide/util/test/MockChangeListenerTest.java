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

import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.ChangeSupport;

// copy of openide.util.ui
public class MockChangeListenerTest extends NbTestCase {

    public MockChangeListenerTest(String n) {
        super(n);
    }

    Object source;
    ChangeSupport cs;
    MockChangeListener l;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        source = new Object();
        cs = new ChangeSupport(source);
        l = new MockChangeListener();
        cs.addChangeListener(l);
    }

    public void testBasicUsage() throws Exception {
        l.assertNoEvents();
        l.assertEventCount(0);
        try {
            l.assertEvent();
            assert false;
        } catch (AssertionFailedError e) {}
        try {
            l.assertEventCount(1);
            assert false;
        } catch (AssertionFailedError e) {}
        cs.fireChange();
        l.assertEvent();
        l.assertNoEvents();
        l.assertEventCount(0);
        cs.fireChange();
        cs.fireChange();
        l.assertEventCount(2);
        cs.fireChange();
        l.assertEvent();
        l.assertNoEvents();
        l.assertNoEvents();
        cs.fireChange();
        l.reset();
        l.assertNoEvents();
        cs.fireChange();
        cs.fireChange();
        assertEquals(2, l.allEvents().size());
    }

    public void testMessages() throws Exception {
        try {
            l.assertEvent();
            assert false;
        } catch (AssertionFailedError e) {}
        try {
            l.msg("stuff").assertEvent();
            assert false;
        } catch (AssertionFailedError e) {
            assertTrue(e.getMessage().contains("stuff"));
        }
        try {
            l.assertEvent();
            assert false;
        } catch (AssertionFailedError e) {
            assertFalse(String.valueOf(e.getMessage()).contains("stuff"));
        }
    }

    @RandomlyFails // NB-Core-Build #8154
    public void testExpect() throws Exception {
        l.expectNoEvents(1000);
        cs.fireChange();
        l.expectEvent(1000);
        l.assertNoEvents();
        new Thread() {
            @Override public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException x) {
                    assert false;
                }
                cs.fireChange();
            }
        }.start();
        try {
            l.expectEvent(1000);
            assert false;
        } catch (AssertionFailedError e) {}
        l.expectEvent(2000);
        l.assertNoEvents();
        l.expectNoEvents(1000);
    }

}
