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

package org.netbeans.core;

import java.awt.EventQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class TimableEventQueueTest extends NbTestCase {
    static {
        System.setProperty("org.netbeans.core.TimeableEventQueue.quantum", "300");
        System.setProperty("org.netbeans.core.TimeableEventQueue.pause", "10");
        System.setProperty("org.netbeans.core.TimeableEventQueue.report", "600");
        TimableEventQueue.initialize(null, false);
    }
    private CharSequence log;
    
    
    public TimableEventQueueTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        log = Log.enable(TimableEventQueue.class.getName(), Level.FINE);
    }

    public void testDispatchEvent() throws Exception {
        class Slow implements Runnable {
            private int ok;
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                ok++;
            }
        }
        Slow slow = new Slow();
        
        EventQueue.invokeAndWait(slow);
        EventQueue.invokeAndWait(slow);
        TimableEventQueue.RP.shutdown();
        TimableEventQueue.RP.awaitTermination(3, TimeUnit.SECONDS);
        
        assertEquals("called", 2, slow.ok);

        if (!log.toString().contains("too much time in AWT thread")) {
            fail("There shall be warning about too much time in AWT thread:\n" + log);
        }
    }
}
