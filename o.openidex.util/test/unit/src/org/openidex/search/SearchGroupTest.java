/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
