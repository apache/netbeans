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

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Provider;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Jan Lahoda
 */
@SuppressWarnings("unchecked") // XXX ought to be corrected, just a lot of them
public class SimpleProxyLookupTest extends NbTestCase {

    public SimpleProxyLookupTest(String testName) {
        super(testName);
    }

    public void test69810() throws Exception {
        Lookup.Template t = new Lookup.Template(String.class);
        SimpleProxyLookup spl = new SimpleProxyLookup(new Provider() {
            public Lookup getLookup() {
                return Lookups.fixed(new Object[] {"test1", "test2"});
            }
        });

        assertGC("", new WeakReference(spl.lookup(t)));

        spl.lookup(new Lookup.Template(Object.class)).allInstances();
    }

    public void testNumberOfListenersAtTheEnd() throws Exception {
        InstanceContent ic = new InstanceContent();
        ic.add(1);
        ic.add(2);
        ic.add(3);
        final Lookup fixed = new AbstractLookup(ic);
        class BlockingResult<T> extends Lookup.Result<T> {
            final List<LookupListener> listeners = new CopyOnWriteArrayList<LookupListener>();
            final Set<LookupListener> allListeners = Collections.synchronizedSet(new HashSet<LookupListener>());
            final Class<T> type;

            public BlockingResult(Class<T> type) {
                this.type = type;
            }

            public void fire() {
                for (LookupListener l : listeners) {
                    l.resultChanged(new LookupEvent(this));
                }
            }


            @Override
            public void addLookupListener(LookupListener l) {
                listeners.add(l);
                allListeners.add(l);
                if (allListeners.size() > 1) {
                    fail("Too many listeners added " + allListeners);
                }
            }

            @Override
            public void removeLookupListener(LookupListener l) {
                listeners.remove(l);
            }

            @Override
            public Collection<? extends T> allInstances() {
                return fixed.lookupAll(type);
            }
        }
        class BlockingLookup extends Lookup implements Lookup.Provider {
            BlockingLookup delegate;

            BlockingResult<Integer> ints = new BlockingResult<Integer>(Integer.class);
            Runnable parallel;

            @Override
            public <T> T lookup(Class<T> clazz) {
                return null;
            }

            @Override
            public <T> Result<T> lookup(Template<T> template) {
                Runnable r = parallel;
                if (r != null) {
                    parallel = null;
                    Thread t = new Thread(r, "parallel query");
                    t.start();
                    try {
                        t.join();
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
                if (template.getType() == Integer.class) {
                    return (Result<T>) ints;
                }
                fail("Only works on ints: " + template.getType());
                return null;
            }

            @Override
            public Lookup getLookup() {
                if (delegate == null) {
                    return Lookup.EMPTY;
                }
                return delegate;
            }
        }

        final BlockingLookup blocking = new BlockingLookup();
        final BlockingLookup blocking2 = new BlockingLookup();
        final SimpleProxyLookup spl = new SimpleProxyLookup(blocking);

        class InParallel implements Runnable {
            boolean running;
            Result<Integer> res;
            Collection<? extends Integer> recompute;
            LL ll = new LL();

            @Override
            public void run() {
                running = true;
                blocking.delegate = blocking2;
                res = spl.lookupResult(Integer.class);
                recompute = res.allInstances();
                res.addLookupListener(ll);
            }
        }
        InParallel ip = new InParallel();

        Result<Integer> res = spl.lookupResult(Integer.class);
        LL ll = new LL();
        res.addLookupListener(ll);
        assertNotNull(res);
        assertTrue(res.allInstances().isEmpty());

        blocking.delegate = blocking;
        blocking.parallel = ip;

        Collection<? extends Integer> recompute = res.allInstances();

        assertTrue("In parallel query started", ip.running);
        assertNotNull("In parallel result obtained", ip.res);

        ll.getCount();
        ip.ll.getCount();
        assertEquals(3, recompute.size());
        assertEquals(3, ip.recompute.size());

        ic.add(5);
        blocking.ints.fire();
        assertEquals("No change", 0, ll.getCount());
        assertEquals("No change", 0, ip.ll.getCount());
        blocking2.ints.fire();

        assertEquals("One more change", 1, ll.getCount());
        assertEquals("One more change", 1, ip.ll.getCount());

        assertEquals("No listener on no longer used BlockingLookup", 0, blocking.ints.listeners.size());
        assertEquals("One listener on the one still in use", 1, blocking2.ints.listeners.size());

        assertTrue("At most one listener added", 1 >= blocking.ints.allListeners.size());
        assertEquals("Only one listener added", 1, blocking2.ints.allListeners.size());
    }

    private static class LL implements LookupListener {
        private int count;

        public LL() {
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            ++count;
        }

        public int getCount() {
            int i = count;
            count = 0;
            return i;
        }
    };

}
