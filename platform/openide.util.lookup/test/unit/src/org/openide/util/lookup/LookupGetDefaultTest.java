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
package org.openide.util.lookup;

import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

public class LookupGetDefaultTest extends NbTestCase {

    public LookupGetDefaultTest(String name) {
        super(name);
    }
    
    public void testCanChangeDefaultLookup() throws Exception {
        Lookup prev = Lookup.getDefault();
        final Lookup my = new AbstractLookup();
        final Thread myThread = Thread.currentThread();
        final boolean[] ok = { false };
        
        Lookups.executeWith(my, new Runnable() {
            @Override
            public void run() {
                assertSame("Default lookup has been changed", my, Lookup.getDefault());
                assertSame("We are being executed in the same thread", myThread, Thread.currentThread());
                ok[0] = true;
            }
        });
        assertTrue("In my lookup code executed OK", ok[0]);
        assertEquals("Current lookup back to normal", prev, Lookup.getDefault());
    }


    public void testCanAccessGlobalLookup() throws Exception {
        final Lookup prev = Lookup.getDefault();
        final Lookup my = new AbstractLookup();
        final Thread myThread = Thread.currentThread();
        final boolean[] ok = { false };
        
        Lookups.executeWith(my, new Runnable() {
            @Override
            public void run() {
                assertSame("Default lookup has been changed", my, Lookup.getDefault());
                assertSame("We are being executed in the same thread", myThread, Thread.currentThread());
                ok[0] = true;
                
                Lookups.executeWith(null, new Runnable() {
                   public void run() {
                         assertSame("Running again with default Lookup", prev, Lookup.getDefault());
                   }   
                });
            }
        });
        assertTrue("In my lookup code executed OK", ok[0]);
        assertEquals("Current lookup back to normal", prev, Lookup.getDefault());
    }
}
