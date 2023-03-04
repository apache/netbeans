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

package org.netbeans.modules.openide.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import org.netbeans.junit.NbTestCase;
import org.openide.util.lookup.implspi.ActiveQueue;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ActiveQueueTest extends NbTestCase{

    public ActiveQueueTest(String name) {
        super(name);
    }

    @Override
    protected int timeOut() {
        return 45000;
    }

    public void testMemoryLeak() throws Exception {
        final Class<?> u1 = ActiveQueue.class;
        class L extends URLClassLoader {
            public L() {
                super(new URL[] {u1.getProtectionDomain().getCodeSource().getLocation()}, u1.getClassLoader().getParent());
            }
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.equals(u1.getName()) || name.startsWith(u1.getName() + "$")) {
                    Class c = findLoadedClass(name);
                    if (c == null) {
                        c = findClass(name);
                    }
                    if (resolve) {
                        resolveClass(c);
                    }
                    return c;
                } else {
                    return super.loadClass(name, resolve);
                }
            }
        }
        ClassLoader l = new L();
        Class<?> u2 = l.loadClass(u1.getName());
        assertEquals(l, u2.getClassLoader());
        Object obj = new Object();
        @SuppressWarnings("unchecked")
        ReferenceQueue<Object> q = (ReferenceQueue<Object>) u2.getMethod("queue").invoke(null);
        RunnableRef ref = new RunnableRef(obj, q);
        synchronized (ref) {
            obj = null;
            assertGC("Ref should be GC'ed as usual", ref);
            ref.wait();
            assertTrue("Run method has been executed", ref.executed);
        }
        /* We can not expect to have the ActiveQueue GC'ed after fix of issue #256943.
        Reference<?> r = new WeakReference<Object>(u2);
        q = null;
        u2 = null;
        l = null;
        assertGC("#86625: Utilities.class can also be collected now", r);
        */
    }


    private static class RunnableRef extends WeakReference<Object>
    implements Runnable {
        public boolean wait;
        public boolean entered;
        public boolean executed;

        public RunnableRef (Object o) {
            this(o, ActiveQueue.queue());
        }

        public RunnableRef(Object o, ReferenceQueue<Object> q) {
            super(o, q);
        }

        public synchronized void run () {
            entered = true;
            if (wait) {
                // notify we are here
                notify ();
                try {
                    wait ();
                } catch (InterruptedException ex) {
                }
            }
            executed = true;

            notifyAll ();
        }
    }

}