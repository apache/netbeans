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
package org.openidex.search;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Node;

/**
 *
 * @author jhavlin
 */
public class SearchGroupTest extends NbTestCase {

    public SearchGroupTest(String name) {
        super(name);
    }

    /** Test that SearchGroup.onStopSearch is invoked properly.     
     */
    public void testOnStopSearchPositive() throws InterruptedException {

        Semaphore s = new Semaphore(0);
        FakeSearchGroup fsg = new FakeSearchGroup(s) {

            @Override
            protected void onStopSearch() {
                getInnerTaks().terminate();
            }
        };
        try {
            Thread searchThread = new Thread(new SearchRunner(fsg));
            searchThread.start();
            s.acquire();
            assertFalse("Search should be running now", fsg.isFinished());
            fsg.stopSearch();
            s.acquire();
            assertTrue("Search has not been stopped", fsg.isFinished());
        } finally {
            fsg.innerTask.terminate();
        }
    }

    /** Test that long-running internal task is not terminated unless
     * method SearchGroup.onStopSearch is overriden to manage it.
     */
    public void testOnStopSearchNegative() throws InterruptedException {

        Semaphore s = new Semaphore(0);
        FakeSearchGroup fsg = new FakeSearchGroup(s);
        try {
            Thread searchThread = new Thread(new SearchRunner(fsg));
            searchThread.start();
            s.acquire();
            assertFalse("Search should be running now", fsg.isFinished());
            fsg.stopSearch();
            assertFalse("acquire - nothing", s.tryAcquire(1, TimeUnit.SECONDS));
            assertFalse("Search should be still running", fsg.isFinished());
            fsg.getInnerTaks().terminate(); // terminate inner task explicitly
            s.acquire();
            assertTrue("Inner task wasn't stopped", fsg.isFinished());
        } finally {
            fsg.getInnerTaks().terminate();
        }
    }

    /** Helper class for simulating internal long-running job. 
     * 
     *  The tasks releases its semaphore twice. After start and after finish.
     */
    private static class TerminatableLongTask {

        private AtomicBoolean stopped = new AtomicBoolean(false);
        private volatile boolean finished = false;
        Semaphore s;

        public TerminatableLongTask(Semaphore s) {
            this.s = s;
        }

        public void start() {
            s.release(); // release - start
            while (!stopped.get()) {
            }
            finished = true;
            s.release(); // release - end
        }

        public final void terminate() {
            stopped.set(true);
        }

        public boolean isFinished() {
            return finished;
        }
    }

    /** Helper trivial implementation of SearchGroup that contains internal
     * long-running task. */
    private static class FakeSearchGroup extends SearchGroup {

        private TerminatableLongTask innerTask;

        public FakeSearchGroup(Semaphore s) {
            innerTask = new TerminatableLongTask(s);
        }

        @Override
        protected void doSearch() {
            innerTask.start();
        }

        @Override
        public Node getNodeForFoundObject(Object object) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public TerminatableLongTask getInnerTaks() {
            return innerTask;
        }

        public boolean isFinished() {
            return stopped && innerTask.isFinished();
        }
    }

    /** Helper Runnable for starting a search group in a new thread.     
     */
    private static class SearchRunner implements Runnable {

        private FakeSearchGroup sg;

        public SearchRunner(FakeSearchGroup sg) {
            this.sg = sg;
        }

        @Override
        public void run() {
            sg.prepareSearch();
            sg.doSearch();
        }
    }
}
