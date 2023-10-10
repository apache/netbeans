/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.nativeexecution;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;

/**
 *
 * @author ak119685
 */
public final class ConcurrentTasksSupport {

    private static final Logger log = Logger.getLogger(ConcurrentTasksSupport.class.getName());
    final int concurrentTasks;
    final ArrayList<TaskFactory> factories = new ArrayList<>();
    final CyclicBarrier startSignal;
    final Thread[] threads;

    public ConcurrentTasksSupport(int concurrentTasks) {
        this.concurrentTasks = concurrentTasks;
        startSignal = new CyclicBarrier(concurrentTasks + 1);
        threads = new Thread[concurrentTasks];
    }

    public void addFactory(TaskFactory taskFactory) {
        factories.add(taskFactory);
    }

    public void init() {
        final AtomicInteger counter = new AtomicInteger(0);
        final Random r = new Random();
        final AtomicInteger idx = new AtomicInteger(0);

        for (int i = 0; i < concurrentTasks; i++) {
            threads[i] = new Thread(new Runnable() {

                final int id = counter.incrementAndGet();

                @Override
                public void run() {
                    int fidx;
                    synchronized (idx) {
                        fidx = idx.getAndIncrement();
                        if (fidx >= factories.size()) {
                            fidx = 0;
                            idx.set(0);
                        }
                    }
                    TaskFactory factoryToUse = factories.get(fidx);


                    Runnable task = factoryToUse.newTask();

                    if (task == null) {
                        throw new NullPointerException("TaskFactory " + factoryToUse + " returned null task!"); // NOI18N
                    }


                    try {
                        startSignal.await();
                    } catch (BrokenBarrierException ex) {
                        log.log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        log.info("InterruptedException while waiting startSignal");
                    }

                    try {
                        task.run();
                    } catch (Throwable th) {
                        log.log(Level.INFO, "Exception in task {0}", th.toString());
                    }
                }
            });

            threads[i].start();
        }
    }

    public void start() {
        try {
            startSignal.await();
        } catch (InterruptedException ex) {
        } catch (BrokenBarrierException ex) {
        }

    }

    public void waitCompletion() {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                log.info("InterruptedException while waiting doneSignal"); // NOI18N
            }
        }
    }

    public static interface TaskFactory {

        public Runnable newTask();
    }

    public static class Counters {

        private final ConcurrentMap<String, AtomicInteger> counters =
                new ConcurrentHashMap<>();

        public AtomicInteger getCounter(String id) {
            AtomicInteger newCounter = new AtomicInteger(0);
            AtomicInteger existentCounter = counters.putIfAbsent(id, newCounter);

            if (existentCounter != null) {
                return existentCounter;
            } else {
                return newCounter;
            }
        }

        public void dump(PrintStream stream) {
            TreeMap<String, AtomicInteger> map = new TreeMap<>(counters);

            for (String id : map.keySet()) {
                stream.println(id + ": " + map.get(id)); // NOI18N
            }
        }

        public void assertEquals(String descr, String id, int expected) {
            Assert.assertEquals(descr, expected, getCounter(id).intValue()); // NOI18N
        }
    }
}
