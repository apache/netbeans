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
import java.awt.Toolkit;
import java.util.Locale;
import org.netbeans.junit.NbTestCase;

public class TimableEventQueueSuppressTest extends NbTestCase {
    static {
        Locale.setDefault(new Locale("te", "ST"));
    }
    
    
    public TimableEventQueueSuppressTest(String testName) {
        super(testName);
    }

    public void testNotInstalled() throws Exception {
        EventQueue old = Toolkit.getDefaultToolkit().getSystemEventQueue();
        if (old instanceof TimableEventQueue) {
            fail("Old queue is already TimableEventQueue: " + old);
        }
        TimableEventQueue.initialize();
        EventQueue now = Toolkit.getDefaultToolkit().getSystemEventQueue();
        assertSame("Old queue was not replaced", old, now);
    }
}
