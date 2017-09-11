/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
import junit.framework.Assert;

/**
 *
 * @author ak119685
 */
public final class ConcurrentTasksSupport {

    final private static Logger log = Logger.getLogger(ConcurrentTasksSupport.class.getName());
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
