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

import junit.framework.TestCase;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Test case which demonstrates that ProxyLookup does not fire
 * an event when it should.
 */
public class ProxyLookupEventIssue136866Test extends TestCase {

    public ProxyLookupEventIssue136866Test(String testName) {
        super(testName);
    }

    public void testAbstractLookupFiresEventWhenContentChanged() {
        InstanceContent ic = new InstanceContent();
        AbstractLookup al = new AbstractLookup(ic);

        final int[] counts = {0}; // Number of items observed upon a LookupEvent
        final Lookup.Result<String> result = al.lookupResult(String.class);

        result.addLookupListener(new LookupListener() {
            public void resultChanged(LookupEvent ev) {
                // this gets called as expected
                assertSame(result, ev.getSource());
                counts[0] = result.allInstances().size();
            }
        });
        
        ic.add("hello1");
        assertEquals(1, counts[0]);
    }
    
    public void testProxyLookupFailsToFireEventWhenProxiedLookupChanged() {
        InstanceContent ic = new InstanceContent();
//        AbstractLookup al = new AbstractLookup(ic);
        Lookup proxy = new AbstractLookup(ic);

        final int[] counts = {0}; // Number of items observed upon a LookupEvent
        final Lookup.Result<String> result = proxy.lookupResult(String.class);

        result.addLookupListener(new LookupListener() {
            public void resultChanged(LookupEvent ev) {
                // this should be called but never is
                assertSame(result, ev.getSource());
                counts[0] = result.allInstances().size();
            }
        });
        
        ic.add("hello1");
        assertEquals(1, counts[0]);
    }
}
