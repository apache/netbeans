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
package org.netbeans.modules.remote.impl.fs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import static org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase.removeDirectory;

/**
 *
 */
public class RemoteFileSystemParallelTestBase extends RemoteFileTestBase {

    static {
        System.setProperty("jsch.connection.timeout", "30000");
    }

    public RemoteFileSystemParallelTestBase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    protected void doTest(final ParallelTestWorker worker) throws Exception {

        removeDirectory(fs.getCache());
        final int threadCount = worker.getThreadCount();

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(worker);
            threads[i].start();
        }
        for (int i = 0; i < threadCount; i++) {
            threads[i].join();
        }
        List<Exception> exceptions = worker.getExceptions();
        if (!exceptions.isEmpty()) {
            System.err.printf("There were %d exceptions; throwing first one.\n", exceptions.size());
            throw exceptions.iterator().next();
        }
    }

    protected abstract class ParallelTestWorker implements Runnable {

        private final String name;
        private final List<Exception> exceptions;
        private final int threadCount;
        private final CyclicBarrier barrier;

        ParallelTestWorker(String name, int threadCount) {
            this.name = name;
            this.threadCount = threadCount;
            this.barrier = new CyclicBarrier(threadCount);
            this.exceptions = Collections.synchronizedList(new ArrayList<Exception>());
        }

        public int getThreadCount() {
            return threadCount;
        }

        public List<Exception> getExceptions() {
            return exceptions;
        }

        @Override
        public void run() {            
            Thread.currentThread().setName(name);
            try {
                barrier.await();
                work();
            } catch (InterruptedException ex) {
                ex.printStackTrace(System.err);
                exceptions.add(ex);
            } catch (BrokenBarrierException ex) {
                ex.printStackTrace(System.err);
                exceptions.add(ex);
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
                exceptions.add(ex);
            }
        }
        protected abstract void work() throws Exception;
    }
}
