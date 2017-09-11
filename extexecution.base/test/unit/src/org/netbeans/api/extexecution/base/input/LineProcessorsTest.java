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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.api.extexecution.base.input;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;

/**
 *
 * @author Petr Hejl
 */
public class LineProcessorsTest extends NbTestCase {

    private static final String WAIT_RELEASE_STRING = "test"; // NOI18N

    private static final long DEADLOCK_TIMEOUT = 1000;

    private static final int WAIT_THREAD_COUNT = 5;

    private static final int PRODUCER_THREAD_COUNT = 5;

    private static final long TEST_TIMEOUT = 5000;

    private static final List<String> PROXY_TEST_LINES = new ArrayList<String>();

    private static final List<String> PRINTING_TEST_LINES = new ArrayList<String>(5);

    static {
        Collections.addAll(PROXY_TEST_LINES, "test1", "test2");

        Collections.addAll(PRINTING_TEST_LINES,
                "the first test line",
                "the second test line",
                "the third test line",
                "the fourth test line",
                "the fifth test line");
    }

    private ExecutorService executor;

    public LineProcessorsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        executor = Executors.newCachedThreadPool();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        executor.shutdownNow();
    }

    public void testProxy() {
        TestLineProcessor processor1 = new TestLineProcessor(false);
        TestLineProcessor processor2 = new TestLineProcessor(false);

        LineProcessor proxy = LineProcessors.proxy(processor1, processor2);
        for (String line : PROXY_TEST_LINES) {
            proxy.processLine(line);
        }

        assertEquals(0, processor1.getResetCount());
        assertEquals(0, processor2.getResetCount());

        assertEquals(PROXY_TEST_LINES, processor1.getLinesProcessed());
        assertEquals(PROXY_TEST_LINES, processor2.getLinesProcessed());

        proxy.reset();

        assertEquals(1, processor1.getResetCount());
        assertEquals(1, processor2.getResetCount());

        proxy.close();
        assertClosedConditions(proxy);

        assertTrue(processor1.isClosed());
        assertTrue(processor2.isClosed());
    }

    public void testPrinting() {
        TestInputWriter writer = new TestInputWriter(new PrintWriter(new ByteArrayOutputStream()));
        LineProcessor lineProcessor = LineProcessors.printing(writer);
        for (String line : PRINTING_TEST_LINES) {
            lineProcessor.processLine(line);
        }
        assertEquals(PRINTING_TEST_LINES, writer.getPrinted());

        lineProcessor.close();
        assertClosedConditions(lineProcessor);
    }

    public void testPrintingCloseOrdering() {
        final TestInputWriter writer = new TestInputWriter(new PrintWriter(new ByteArrayOutputStream()));
        final LineProcessor delegate = LineProcessors.printing(writer);

        LineProcessor lineProcessor = new LineProcessor() {

            public void processLine(String line) {
                delegate.processLine(line);
            }

            public void reset() {
                delegate.reset();
            }

            public void close() {
                delegate.processLine("closing mark");
                delegate.close();
            }
        };

        for (String line : PRINTING_TEST_LINES) {
            lineProcessor.processLine(line);
        }
        assertEquals(PRINTING_TEST_LINES, writer.getPrinted());

        lineProcessor.close();
        List<String> printed = new ArrayList<String>(PRINTING_TEST_LINES);
        printed.add("closing mark");
        assertEquals(printed, writer.getPrinted());
        assertClosedConditions(lineProcessor);
    }

    public void testWaiting() throws InterruptedException, BrokenBarrierException {
        final CountDownLatch latch = new CountDownLatch(1);
        final LineProcessor lineProcessor = LineProcessors.patternWaiting(
                Pattern.compile(WAIT_RELEASE_STRING), latch);
        CyclicBarrier barrier = new CyclicBarrier(2);

        executor.execute(new WaitRunnable(latch, barrier));
        barrier.await();
        lineProcessor.processLine(WAIT_RELEASE_STRING);

        try {
            barrier.await(DEADLOCK_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            fail("Deadlock occurs");
        }

        executor.execute(new WaitRunnable(latch, barrier));
        barrier.await();
        try {
            barrier.await(DEADLOCK_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            fail("Deadlock occurs");
        }

        lineProcessor.close();
        assertClosedConditions(lineProcessor);
    }

    @RandomlyFails // NB-Core-Build #8029
    public void testWaitingThreadSafety() throws InterruptedException, BrokenBarrierException {
        final CountDownLatch latch = new CountDownLatch(1);
        final LineProcessor lineProcessor = LineProcessors.patternWaiting(
                Pattern.compile(WAIT_RELEASE_STRING), latch);
        CyclicBarrier barrier = new CyclicBarrier(WAIT_THREAD_COUNT + 1);

        for (int i = 0; i < WAIT_THREAD_COUNT; i++) {
            executor.execute(new WaitRunnable(latch, barrier));
        }

        barrier.await();

        Random random = new Random();
        for (int i = 0; i < PRODUCER_THREAD_COUNT; i++) {
            executor.execute(new ProducerRunnable(lineProcessor, WAIT_RELEASE_STRING, random.nextInt(5)));
        }

        // guarantee finish
        executor.execute(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(TEST_TIMEOUT);
                    lineProcessor.processLine(WAIT_RELEASE_STRING);
                } catch (InterruptedException ex) {
                    //throw new RuntimeException(ex);
                }
            }
        });

        try {
            barrier.await(TEST_TIMEOUT + DEADLOCK_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            fail("Deadlock occurs");
        }
    }

    private static <T> void assertEquals(List<T> expected, List<T> value) {
        assertEquals(expected.size(), value.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), value.get(i));
        }
    }

    private static void assertClosedConditions(LineProcessor lineProcessor) {
        try {
            lineProcessor.processLine("something");
            fail("Does not throw IllegalStateException after close");
        } catch (IllegalStateException ex) {
            // expected
        }

        try {
            lineProcessor.reset();
            fail("Does not throw IllegalStateException after close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    private static class WaitRunnable implements Runnable {

        private final CountDownLatch latch;

        private final CyclicBarrier barrier;

        public WaitRunnable(CountDownLatch latch, CyclicBarrier barrier) {
            this.latch = latch;
            this.barrier = barrier;
        }

        public void run() {
            try {
                barrier.await();
                latch.await();
                barrier.await();
            } catch (InterruptedException ex) {
                // timeouted test
                Thread.currentThread().interrupt();
            } catch (BrokenBarrierException ex) {
                // timeouted test
            }
        }

    }

    private static class ProducerRunnable implements Runnable {

        private final LineProcessor lineProcessor;

        private final String releaseString;

        private final Random random = new Random();

        private final int iterations;

        public ProducerRunnable(LineProcessor lineProcessor, String releaseString, int iterations) {
            this.lineProcessor = lineProcessor;
            this.releaseString = releaseString;
            this.iterations = iterations;
        }

        public void run() {
            for (int i = 0; i < iterations; i++) {
                if (Thread.interrupted()) {
                    return;
                }

                int val = random.nextInt(10);
                if (val == 0) {
                    lineProcessor.processLine(releaseString);
                    return;
                } else {
                    lineProcessor.processLine("generated " + val);
                }

                try {
                    Thread.sleep(random.nextInt(300));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
