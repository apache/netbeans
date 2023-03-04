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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

public class AbstractLookupAsynchExecutorTest extends NbTestCase implements Executor {
    private List<Runnable> toRun = new ArrayList<Runnable>();


    public AbstractLookupAsynchExecutorTest(java.lang.String testName) {
        super(testName);
    }

    public void testCanProxyLookupHaveWrongResults() {
        final InstanceContent ic = new InstanceContent(this);
        final AbstractLookup lookup = new AbstractLookup(ic);

        class L implements LookupListener {
            ProxyLookup pl;
            Lookup.Result<String> original;
            Lookup.Result<String> wrapped;
            boolean ok;

            public void test() {
                pl = new ProxyLookup(lookup);
                original = lookup.lookupResult(String.class);

                original.addLookupListener(this);

                wrapped = pl.lookupResult(String.class);

                assertEquals("Original empty", 0, original.allInstances().size());
                assertEquals("Wrapped empty", 0, wrapped.allInstances().size());

                ic.add("Hello!");
            }

            public void resultChanged(LookupEvent ev) {
                ok = true;
                assertContainsHello();
            }

            public void assertContainsHello() {
                assertEquals("Original has hello", 1, original.allInstances().size());
                assertEquals("Wrapped has hello", 1, wrapped.allInstances().size());
            }

        }
        L listener = new L();
        listener.test();
        listener.assertContainsHello();
        for (Runnable r : toRun) {
            r.run();
        }
        assertTrue("Listener called", listener.ok);
    }

    public void execute(Runnable command) {
        toRun.add(command);
    }

}
