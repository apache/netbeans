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

package org.netbeans.core.execution;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.windows.IOProvider;

/**
 * Test that a task thread group is cleared when it is done.
 * @see "#36395"
 * @author Jesse Glick
 */
public class TaskThreadGroupGCTest extends NbTestCase {

    public TaskThreadGroupGCTest(String name) {
        super(name);
    }

    @Override
    protected int timeOut() {
        return 60000;
    }

    public void testTTGGC() throws Exception {
        IOProvider.getDefault(); // initialize stdio streams to default, else we will get stack overflow later
        final List<Reference<Thread>> t = new ArrayList<Reference<Thread>>();
        Runnable r = new Runnable() {
            public void run() {
                System.out.println("Running a task in the execution engine...");
                t.add(new WeakReference<Thread>(Thread.currentThread()));
                Runnable r1 = new Runnable() {
                    public void run() {
                        System.out.println("Ran second thread.");
                    }
                };
                Thread nue1 = new Thread(r1);
                nue1.start();
                try {
                    nue1.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                t.add(new WeakReference<Thread>(nue1));
                Runnable r2 = new Runnable() {
                    public void run() {
                        System.out.println("Ran third thread.");
                    }
                };
                Thread nue2 = new Thread(r2);
                nue2.start();
                t.add(new WeakReference<Thread>(nue2));
                Runnable r3 = new Runnable() {
                    public void run() {
                        fail("Should not have even run.");
                    }
                };
                Thread nue3 = new Thread(r3);
                t.add(new WeakReference<Thread>(nue3));
                System.out.println("done.");
            }
        };
        ExecutorTask task = ExecutionEngine.getDefault().execute("foo", r, null);
        assertEquals(0, task.result());
        assertFalse(t.toString(), t.contains(null));
        r = null;
        task = null;
        assertGC("Collected secondary task thread too", t.get(1));
        assertGC("Collected forked task thread too", t.get(2));
        assertGC("Collected unstarted task thread too", t.get(3));
        /*
        Thread main = t.get(0).get();
        if (main != null) {
            assertFalse(main.isAlive());
            main = null;
        }
         */
        assertGC("Collected task thread " + t.get(0).get(), t.get(0));
    }

}
