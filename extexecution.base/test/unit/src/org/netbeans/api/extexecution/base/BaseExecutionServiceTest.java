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

package org.netbeans.api.extexecution.base;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputProcessors;
import org.netbeans.api.extexecution.base.input.TestInputUtils;
import org.netbeans.api.extexecution.base.input.TestLineProcessor;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class BaseExecutionServiceTest extends NbTestCase {

    private static final int PROCESS_TIMEOUT = 30000;

    public BaseExecutionServiceTest(String name) {
        super(name);
    }

    public void testSimpleRun() throws InterruptedException {
        TestProcess process = new TestProcess(0);
        TestCallable callable = new TestCallable();
        callable.addProcess(process);

        BaseExecutionDescriptor descriptor = new BaseExecutionDescriptor();
        BaseExecutionService service = BaseExecutionService.newService(
                callable, descriptor);

        Future<Integer> task = service.run();
        assertNotNull(task);

        process.waitStarted();

        process.destroy();
        process.waitFor();
        assertTrue(process.isFinished());
        assertEquals(0, process.exitValue());
    }

    public void testReRun() throws InvocationTargetException, InterruptedException {
        TestProcess process = new TestProcess(0);
        TestCallable callable = new TestCallable();
        callable.addProcess(process);

        BaseExecutionDescriptor descriptor = new BaseExecutionDescriptor();
        BaseExecutionService service = BaseExecutionService.newService(
                callable, descriptor);

        // first run
        Future<Integer> task = service.run();
        assertNotNull(task);
        assertFalse(process.isFinished());

        process.waitStarted();
        task.cancel(true);
        assertTrue(task.isCancelled());

        process.waitFor();
        assertTrue(process.isFinished());
        assertEquals(0, process.exitValue());

        // second run
        process = new TestProcess(1);
        callable.addProcess(process);

        task = service.run();
        assertNotNull(task);
        assertFalse(process.isFinished());

        // we want to test real started process
        process.waitStarted();
        task.cancel(true);
        assertTrue(task.isCancelled());

        process.waitFor();
        assertTrue(process.isFinished());
        assertEquals(1, process.exitValue());
    }

    public void testCancelRerun() throws InterruptedException {
        TestProcess process = new TestProcess(0);
        TestCallable callable = new TestCallable();
        callable.addProcess(process);

        BaseExecutionDescriptor descriptor = new BaseExecutionDescriptor();
        final CountDownLatch latch = new CountDownLatch(1);
        descriptor = descriptor.preExecution(new Runnable() {
            public void run() {
                try {
                    latch.await();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        BaseExecutionService service = BaseExecutionService.newService(
                callable, descriptor);

        // first run
        Future<Integer> task = service.run();
        assertNotNull(task);
        assertFalse(process.isFinished());

        task.cancel(true);
        // guaranteed process was not executed
        latch.countDown();

        assertTrue(task.isCancelled());
        assertFalse(process.isStarted());
        assertFalse(process.isFinished());

        // second run
        task = service.run();
        assertNotNull(task);
        assertFalse(process.isFinished());

        // we want to test real started process
        process.waitStarted();
        task.cancel(true);
        assertTrue(task.isCancelled());

        process.waitFor();
        assertTrue(process.isFinished());
        assertEquals(0, process.exitValue());
    }

    public void testConcurrentRun() throws InterruptedException, ExecutionException, BrokenBarrierException {
        TestProcess process1 = new TestProcess(0);
        TestProcess process2 = new TestProcess(1);
        TestCallable callable = new TestCallable();
        callable.addProcess(process1);
        callable.addProcess(process2);

        BaseExecutionDescriptor descriptor = new BaseExecutionDescriptor();
        final CyclicBarrier barrier = new CyclicBarrier(3);
        descriptor = descriptor.preExecution(new Runnable() {
            public void run() {
                try {
                    barrier.await();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (BrokenBarrierException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        BaseExecutionService service = BaseExecutionService.newService(
                callable, descriptor);

        Future<Integer> task1 = service.run();
        Future<Integer> task2 = service.run();

        // wait for both tasks
        barrier.await();

        process1.destroy();
        process2.destroy();

        // TODO can we check returns values somehow ?
        // task - process assignment is determined by the winner of the race :(
        task1.get().intValue();
        task2.get().intValue();

        assertTrue(task1.isDone());
        assertTrue(task2.isDone());

        assertFalse(task1.isCancelled());
        assertFalse(task2.isCancelled());
    }

    public void testHooks() throws InterruptedException, ExecutionException {
        TestProcess process = new TestProcess(0);
        TestCallable callable = new TestCallable();
        callable.addProcess(process);

        class TestRunnable implements Runnable {

            public volatile boolean executed;

            public void run() {
                executed = true;
            }
        }
        
        class TestParametrizedRunnable implements ParametrizedRunnable<Integer> {

            public volatile boolean executed;

            public void run(Integer parameter) {
                executed = true;
            }
        }

        TestRunnable preRunnable = new TestRunnable();
        TestParametrizedRunnable postRunnable = new TestParametrizedRunnable();

        BaseExecutionDescriptor descriptor = new BaseExecutionDescriptor();
        descriptor = descriptor.preExecution(preRunnable).postExecution(postRunnable);

        BaseExecutionService service = BaseExecutionService.newService(
                callable, descriptor);

        Future<Integer> task = service.run();
        assertNotNull(task);

        process.waitStarted();
        assertTrue(preRunnable.executed);

        process.destroy();
        assertEquals(0, task.get().intValue());
        assertTrue(postRunnable.executed);
    }

    public void testCharset() throws InterruptedException, ExecutionException, TimeoutException {
        Charset charset = Charset.forName("UTF-16LE");
        final String[] lines = new String[] {"Process line \u1234", "Process line \u1235", "Process line \u1236"};

        TestInputStream is = new TestInputStream(TestInputUtils.prepareInputStream(lines, "\n", charset, true));
        TestProcess process = new TestProcess(0, is, null);
        is.setProcess(process);

        TestCallable callable = new TestCallable();
        callable.addProcess(process);

        final TestLineProcessor processor = new TestLineProcessor(false);
        BaseExecutionDescriptor descriptor = new BaseExecutionDescriptor().charset(charset).outProcessorFactory(
                new BaseExecutionDescriptor.InputProcessorFactory() {

            public InputProcessor newInputProcessor() {
                return InputProcessors.bridge(processor);
            }
        });

        BaseExecutionService service = BaseExecutionService.newService(
                callable, descriptor);

        Future<Integer> task = service.run();
        assertNotNull(task);

        assertEquals(0, task.get(PROCESS_TIMEOUT, TimeUnit.MILLISECONDS).intValue());
        assertTrue(process.isFinished());
        assertEquals(0, process.exitValue());

        List<String> processed = processor.getLinesProcessed();
        assertEquals(lines.length, processed.size());
        for (int i = 0; i < lines.length; i++) {
            assertEquals(lines[i], processed.get(i));
        }
    }

    private static class TestCallable implements Callable<Process> {

        private final LinkedList<TestProcess> processes = new LinkedList<TestProcess>();

        public TestCallable() {
            super();
        }

        public synchronized void addProcess(TestProcess process) {
            processes.add(process);
        }

        public synchronized Process call() throws Exception {
            if (processes.isEmpty()) {
                throw new IllegalStateException("No process configured");
            }

            TestProcess ret = processes.removeFirst();
            ret.start();

            return ret;
        }
    }

    private static class TestProcess extends Process {

        private final int returnValue;

        private final InputStream is;

        private final InputStream err;

        private boolean finished;

        private boolean started;

        public TestProcess(int returnValue) {
            this(returnValue, TestInputUtils.prepareInputStream(
                    new String[] {"Process line 1", "Process line 2", "Process line 3"}, "\n",
                    Charset.defaultCharset(), true), null);
        }

        public TestProcess(int returnValue, InputStream is, InputStream err) {
            this.returnValue = returnValue;
            this.is = is;
            this.err = err;
        }

        public void start() {
            synchronized (this) {
                started = true;
                notifyAll();
            }
        }

        public boolean isStarted() {
            synchronized (this) {
                return started;
            }
        }

        public boolean isFinished() {
            synchronized (this) {
                return finished;
            }
        }

        @Override
        public void destroy() {
            synchronized (this) {
                if (finished) {
                    return;
                }

                finished = true;
                notifyAll();
            }
        }

        @Override
        public int exitValue() {
            synchronized (this) {
                if (!finished) {
                    throw new IllegalStateException("Not finished yet");
                }
            }
            return returnValue;
        }

        @Override
        public InputStream getErrorStream() {
            if (err != null) {
                return err;
            }
            return new InputStream() {
                @Override
                public int read() throws IOException {
                    return -1;
                }
            };
        }

        @Override
        public InputStream getInputStream() {
            return is;
        }

        @Override
        public OutputStream getOutputStream() {
            return new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    // throw it away
                }
            };
        }

        @Override
        public int waitFor() throws InterruptedException {
            synchronized (this) {
                while (!finished) {
                    wait();
                }
            }
            return returnValue;
        }

        public void waitStarted() throws InterruptedException {
            synchronized (this) {
                while (!started) {
                    wait();
                }
            }
        }
    }

    private static class TestInputStream extends FilterInputStream {

        private Process process;

        public TestInputStream(InputStream is) {
            super(is);
        }

        public synchronized Process getProcess() {
            return process;
        }

        public synchronized void setProcess(Process process) {
            this.process = process;
        }

        @Override
        public int available() throws IOException {
            int available = super.available();
            if (available <= 0) {
                Process toDestroy = getProcess();
                if (toDestroy != null) {
                    toDestroy.destroy();
                }
            }
            return available;
        }


        @Override
        public int read() throws IOException {
            int val = super.read();
            if (val < 0) {
                Process toDestroy = getProcess();
                if (toDestroy != null) {
                    toDestroy.destroy();
                }
            }
            return val;
        }

        @Override
        public int read(byte[] b) throws IOException {
            int val = super.read(b);
            if (val < 0) {
                Process toDestroy = getProcess();
                if (toDestroy != null) {
                    toDestroy.destroy();
                }
            }
            return val;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int val = super.read(b, off, len);
            if (val < 0) {
                Process toDestroy = getProcess();
                if (toDestroy != null) {
                    toDestroy.destroy();
                }
            }
            return val;
        }
    }
}
