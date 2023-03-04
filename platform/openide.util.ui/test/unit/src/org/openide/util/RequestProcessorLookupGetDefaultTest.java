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

import java.util.concurrent.TimeUnit;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

public class RequestProcessorLookupGetDefaultTest extends NbTestCase {

    public RequestProcessorLookupGetDefaultTest(String name) {
        super(name);
    }
    
    public void testChangeOfDefaultLookupAppliedToRPTask() throws Exception {
        Lookup prev = Lookup.getDefault();
        final Lookup my = new AbstractLookup(new InstanceContent());
        final Thread myThread = Thread.currentThread();
        final RequestProcessor.Task[] task = { null };
        final boolean[] ok = { false };
        
        Lookups.executeWith(my, new Runnable() {
            @Override
            public void run() {
                assertSame("Default lookup has been changed", my, Lookup.getDefault());

                if (task[0] == null) {
                    assertSame("We are being executed in the same thread", myThread, Thread.currentThread());
                    // once again in the RP
                    task[0] = RequestProcessor.getDefault().post(this, 500);
                } else {
                    ok[0] = true;
                }
            }
        });
        assertNotNull("In my lookup code executed OK", task[0]);
        assertEquals("Current lookup back to normal", prev, Lookup.getDefault());
        task[0].waitFinished();
        assertTrue("Even RP task had the right lookup", ok[0]);
    }
}
