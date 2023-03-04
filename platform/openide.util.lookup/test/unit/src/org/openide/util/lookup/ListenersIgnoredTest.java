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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

public class ListenersIgnoredTest extends NbTestCase {
    private static final Logger LOG = Logger.getLogger(ListenersIgnoredTest.class.getName());

    public ListenersIgnoredTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    public void testLookupBugTest() {
        class C0 {
        }
        class C1 {
        }
        InstanceContent content = new InstanceContent();
        AbstractLookup lookup = new AbstractLookup(content);
//force lookup to use InheritanceTree as Storage
        for (int i = 0; i < 12; i++) {
            content.add(i);
        }

        Result<C0> r0 = lookup.lookupResult(C0.class);

        final AtomicInteger cnt = new AtomicInteger();
        r0.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                cnt.incrementAndGet();
                LOG.fine("r0 notified");
            }
        });
        
        C0 o0 = new C0();
        C1 o1 = new C1();

        LOG.fine("Add o0");
        content.add(o0);
        assertEquals("One change", 1, cnt.getAndSet(0));

        LOG.fine("Remove o0");
        content.remove(o0);
        assertEquals("Another change change", 1, cnt.getAndSet(0));

        LOG.fine("Add o1");
        content.add(o1);
        assertEquals("No change", 0, cnt.getAndSet(0));

        LOG.fine("Remove o1");
        content.remove(o1);
        assertEquals("No change", 0, cnt.getAndSet(0));

        LOG.fine("Add o0");
        content.add(o0);
        LOG.fine("Line before should read 'r0 notified' ?");
        assertEquals("One change", 1, cnt.getAndSet(0));

    }

}
