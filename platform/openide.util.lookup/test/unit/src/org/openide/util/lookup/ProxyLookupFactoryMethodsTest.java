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

import java.util.Arrays;
import static java.util.Arrays.asList;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.ProxyLookupFactoryMethodsTest.TThreadFactory.TThread;

/**
 *
 * @author Tim Boudreau
 */
public class ProxyLookupFactoryMethodsTest {

    private ProxyLookup.Controller controller1;
    private ProxyLookup.Controller controller2;

    private Lookup createWithSingleConsumer(Lookup... lookups) {
        ProxyLookup.Controller controller = new ProxyLookup.Controller();
        controller1 = controller;
        ProxyLookup result = new ProxyLookup(controller);
        result.setLookups(lookups);
        return result;
    }

    private Lookup createWithBiConsumer(Lookup... lookups) {
        ProxyLookup.Controller controller = new ProxyLookup.Controller();
        controller2 = controller;
        ProxyLookup result = new ProxyLookup(controller);
        result.setLookups(lookups);
        return result;
    }

    @Test
    public void testCannotUseControllerOnMultipleLookups() {
        ProxyLookup.Controller ctrllr = new ProxyLookup.Controller();
        ProxyLookup first = new ProxyLookup(ctrllr);
        assertTrue(first.lookupAll(String.class).isEmpty());
        try {
            ProxyLookup second = new ProxyLookup(ctrllr);
            fail("Exception should have been thrown using controller more than "
                    + "once but was able to create " + second);
        } catch (IllegalStateException ex) {
            // ok
        }
    }

    @Test
    public void testStartWithEmptyController() {
        ProxyLookup.Controller ctrllr = new ProxyLookup.Controller();
        ProxyLookup lkp = new ProxyLookup(ctrllr);
        assertTrue(lkp.lookupAll(String.class).isEmpty());
        ctrllr.setLookups(Lookups.fixed("a"), Lookups.fixed("b"));
        assertEquals(new HashSet<>(Arrays.asList("a", "b")),
                new HashSet<>(lkp.lookupAll(String.class)));
    }

    @Test
    public void testSimpleFactory() {
        Lookup a = Lookups.fixed("a");
        Lookup b = Lookups.fixed("b");
        Lookup c = Lookups.fixed("c");

        Lookup target = createWithSingleConsumer(a, b);
        assertNotNull(controller1);
        assertTrue(target.lookupAll(String.class).containsAll(asList("a", "b")));
        assertFalse(target.lookupAll(String.class).contains("c"));

        controller1.setLookups(new Lookup[]{a, b, c});
        assertTrue(target.lookupAll(String.class).containsAll(asList("a", "b", "c")));

        controller1.setLookups(new Lookup[0]);
        assertTrue(target.lookupAll(String.class).isEmpty());
    }

    @Test
    public void testThreadedFactory() throws Throwable {
        ExecutorService svc = Executors.newSingleThreadExecutor(new TThreadFactory());
        Lookup a = Lookups.fixed("a");
        Lookup b = Lookups.fixed("b");
        Lookup c = Lookups.fixed("c");

        Lookup target = createWithBiConsumer(a, b);
        Lookup.Result<String> result = target.lookupResult(String.class);

        LL lis = new LL();
        result.addLookupListener(lis);
        // Ugh, ProxyLookup.LazyList does not implement the contract
        // of Collection.equals().
        assertEquals(new HashSet<>(result.allInstances()), new HashSet<>(Arrays.asList("a", "b")));

        assertNotNull(controller2);
        assertTrue(target.lookupAll(String.class).containsAll(asList("a", "b")));
        assertFalse(target.lookupAll(String.class).contains("c"));

        controller2.setLookups(svc, new Lookup[]{a, b, c});

        lis.assertNotifiedInExecutor();

        assertTrue(target.lookupAll(String.class).containsAll(asList("a", "b", "c")));
        assertEquals(new HashSet<>(result.allInstances()), new HashSet<>(Arrays.asList("a", "b", "c")));

        controller2.setLookups(svc, new Lookup[0]);
        lis.assertNotifiedInExecutor();
        assertTrue(target.lookupAll(String.class).isEmpty());
        assertTrue(result.allInstances().isEmpty());

        controller2.setLookups(null, new Lookup[]{b, c});
        assertTrue(target.lookupAll(String.class).containsAll(asList("b", "c")));
        assertEquals(new HashSet<>(result.allInstances()), new HashSet<>(Arrays.asList("b", "c")));
        lis.assertNotifiedSynchronously();
    }

    static final class TThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {
            return new TThread(r);
        }

        static class TThread extends Thread {

            TThread(Runnable r) {
                super(r);
                setName("test-thread");
                setDaemon(true);
            }
        }
    }

    static class LL implements LookupListener {

        private Thread notifyThread;
        private CountDownLatch latch = new CountDownLatch(1);

        void assertNotifiedInExecutor() throws InterruptedException {
            CountDownLatch l;
            synchronized (this) {
                l = latch;
            }
            latch.await(10, TimeUnit.SECONDS);
            Thread t;
            synchronized (this) {
                t = notifyThread;
                notifyThread = null;
            }
            assertNotNull(t);
            assertTrue(t instanceof TThread);
        }

        void assertNotifiedSynchronously() throws InterruptedException {
            assertSame(Thread.currentThread(), notifyThread);
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            CountDownLatch l;
            synchronized (this) {
                notifyThread = Thread.currentThread();
                l = latch;
                latch = new CountDownLatch(1);
                assert l != null;
            }
            l.countDown();
        }

    }

}
