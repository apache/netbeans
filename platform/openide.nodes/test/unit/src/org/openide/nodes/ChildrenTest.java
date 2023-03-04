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

package org.openide.nodes;

import java.lang.ref.WeakReference;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Mutex;

/**
 *
 * @author Andrei Badea
 */
public class ChildrenTest extends NbTestCase {

    private static ClassLoader ccl;

    public ChildrenTest(String name) {
        super(name);
    }

    public void testCanCloneChildren() throws CloneNotSupportedException {
        Children ch = new Children.Array();
        Children nch = (Children)ch.clone();
        assertEquals("No nodes", 0, nch.getNodesCount());
    }

    public void testProjectManagerDeadlockDetector() throws Exception {
        boolean ea = false;
        assert ea = true;
        if (!ea) {
            return;
        }
        ccl = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = new PMClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try {
            class NoOp implements Runnable {
                public void run() {}
            }
            final int[] called = { 0 };
            Runnable tester = new Runnable() {
                public void run() {
                    try {
                        called[0]++;
                        Children.MUTEX.readAccess(new NoOp());
                        fail();
                    } catch (IllegalStateException e) {
                        // OK.
                    }
                    try {
                        called[0]++;
                        Children.MUTEX.writeAccess(new NoOp());
                        fail();
                    } catch (IllegalStateException e) {
                        // OK.
                    }
                }
            };
            PM.mutex().readAccess(tester);
            PM.mutex().writeAccess(tester);
            assertEquals(4, called[0]);
        } finally {
            Thread.currentThread().setContextClassLoader(ccl);
        }
        WeakReference<Mutex> mutexRef = new WeakReference<Mutex>(PM.mutex);
        PM.mutex = null;
        assertGC("Should GC PM.mutex", mutexRef);
    }

    private static final class PMClassLoader extends ClassLoader {

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (name.equals("org.netbeans.api.project.ProjectManager")) {
                return PM.class;
            }
            throw new ClassNotFoundException();
        }
    }

    private static final class PM {

        public static Mutex mutex = new Mutex();

        public static Mutex mutex() {
            return mutex;
        }
    }
}
