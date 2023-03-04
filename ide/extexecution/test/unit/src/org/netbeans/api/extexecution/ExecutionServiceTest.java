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

package org.netbeans.api.extexecution;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.extexecution.InputOutputManager;
import org.netbeans.api.extexecution.input.TestInputUtils;
import org.netbeans.api.extexecution.input.TestLineProcessor;
import org.netbeans.junit.RandomlyFails;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Petr Hejl
 */
public class ExecutionServiceTest extends NbTestCase {

    private static final int PROCESS_TIMEOUT = 30000;

    public ExecutionServiceTest(String name) {
        super(name);
    }

    @Override
    protected void tearDown() throws Exception {
        InputOutputManager.clear();
        super.tearDown();
    }

    public void testSimpleRun() throws InterruptedException {
        TestProcess process = new TestProcess(0);
        TestCallable callable = new TestCallable();
        callable.addProcess(process);

        ExecutionDescriptor descriptor = new ExecutionDescriptor();
        ExecutionService service = ExecutionService.newService(
                callable, descriptor, "Test");

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

        ExecutionDescriptor descriptor = new ExecutionDescriptor();
        ExecutionService service = ExecutionService.newService(
                callable, descriptor, "Test");

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

        ExecutionDescriptor descriptor = new ExecutionDescriptor();
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

        ExecutionService service = ExecutionService.newService(
                callable, descriptor, "Test");

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

        ExecutionDescriptor descriptor = new ExecutionDescriptor();
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

        ExecutionService service = ExecutionService.newService(
                callable, descriptor, "Test");

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

        TestRunnable preRunnable = new TestRunnable();
        TestRunnable postRunnable = new TestRunnable();

        ExecutionDescriptor descriptor = new ExecutionDescriptor();
        descriptor = descriptor.preExecution(preRunnable).postExecution(postRunnable);

        ExecutionService service = ExecutionService.newService(
                callable, descriptor, "Test");

        Future<Integer> task = service.run();
        assertNotNull(task);

        process.waitStarted();
        assertTrue(preRunnable.executed);

        process.destroy();
        assertEquals(0, task.get().intValue());
        assertTrue(postRunnable.executed);
    }

    @RandomlyFails // NB-Core-Build #7910: second assertNull
    public void testIOHandling() throws InterruptedException, InvocationTargetException, ExecutionException {
        TestProcess process = new TestProcess(0);
        TestCallable callable = new TestCallable();
        callable.addProcess(process);

        ExecutionDescriptor descriptor = new ExecutionDescriptor();
        ExecutionService service = ExecutionService.newService(
                callable, descriptor, "Test");

        Future<Integer> task = service.run();
        assertNotNull(task);

        assertNull(getInputOutput("Test", false, null));
        process.destroy();
        assertEquals(0, task.get().intValue());

        assertNotNull(getInputOutput("Test", false, null));

        // rerun once again
        process = new TestProcess(0);
        callable.addProcess(process);

        task = service.run();
        assertNotNull(task);

        assertNull(getInputOutput("Test", false, null));
        process.destroy();
        task.get();

        assertNotNull(getInputOutput("Test", false, null));
    }

    public void testIOHandlingMulti() throws InterruptedException, InvocationTargetException,
            ExecutionException {

        TestProcess process1 = new TestProcess(0);
        TestProcess process2 = new TestProcess(0);
        TestCallable callable = new TestCallable();

        callable.addProcess(process1);

        ExecutionDescriptor descriptor = new ExecutionDescriptor();
        ExecutionService service = ExecutionService.newService(
                callable, descriptor, "Test");

        Future<Integer> task1 = service.run();
        assertNotNull(task1);

        assertNull(getInputOutput("Test", false, null));
        assertNull(getInputOutput("Test #2", false, null));

        process1.waitStarted();

        callable.addProcess(process2);

        Future<Integer> task2 = service.run();
        assertNotNull(task2);

        assertNull(getInputOutput("Test", false, null));
        assertNull(getInputOutput("Test #2", false, null));

        process2.waitStarted();

        process1.destroy();
        process2.destroy();

        assertEquals(0, task1.get().intValue());
        assertEquals(0, task2.get().intValue());

        assertNotNull(getInputOutput("Test", false, null));
        assertNotNull(getInputOutput("Test #2", false, null));
    }

    public void testCharset() throws InterruptedException, ExecutionException, TimeoutException {
        Charset charset = StandardCharsets.UTF_16LE;
        final String[] lines = new String[] {"Process line \u1234", "Process line \u1235", "Process line \u1236"};

        TestInputStream is = new TestInputStream(TestInputUtils.prepareInputStream(lines, "\n", charset, true));
        TestProcess process = new TestProcess(0, is);
        is.setProcess(process);

        TestCallable callable = new TestCallable();
        callable.addProcess(process);

        final TestLineProcessor processor = new TestLineProcessor(false);
        ExecutionDescriptor descriptor = new ExecutionDescriptor().charset(charset).outProcessorFactory(
                new ExecutionDescriptor.InputProcessorFactory() {

            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return InputProcessors.bridge(processor);
            }
        });

        ExecutionService service = ExecutionService.newService(
                callable, descriptor, "Test");

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

    public void testIOReset() throws InterruptedException, InvocationTargetException, ExecutionException {
        TestProcess process = new TestProcess(0);
        TestCallable callable = new TestCallable();
        callable.addProcess(process);

        InputOutput io = IOProvider.getDefault().getIO("Test", new Action[] {});
        TestInputOutput testIO = new TestInputOutput(io);

        ExecutionDescriptor descriptor = new ExecutionDescriptor()
                .inputOutput(testIO).noReset(true);
        ExecutionService service = ExecutionService.newService(
                callable, descriptor, "Test");

        Future<Integer> task = service.run();
        assertNotNull(task);
        process.destroy();
        assertEquals(0, task.get().intValue());

        assertFalse(testIO.isReset());

        // now with enabled
        process = new TestProcess(0);
        callable = new TestCallable();
        callable.addProcess(process);

        testIO = new TestInputOutput(io);
        descriptor = new ExecutionDescriptor().inputOutput(testIO);
        service = ExecutionService.newService(callable, descriptor, "Test");

        task = service.run();
        assertNotNull(task);
        process.destroy();
        assertEquals(0, task.get().intValue());

        assertTrue(testIO.isReset());
    }

    public void testIOFrontOnError() throws InterruptedException, InvocationTargetException, ExecutionException {
        TestProcess process = new TestProcess(0);
        TestCallable callable = new TestCallable();
        callable.addProcess(process);

        InputOutput io = IOProvider.getDefault().getIO("Test", new Action[] {});
        final TestInputOutput testIO1 = new TestInputOutput(io);
        ExecutionDescriptor descriptor = new ExecutionDescriptor()
                .inputOutput(testIO1).frontWindowOnError(true);
        ExecutionService service = ExecutionService.newService(
                callable, descriptor, "Test");

        Future<Integer> task = service.run();
        assertNotNull(task);
        process.destroy();
        assertEquals(0, task.get().intValue());

        final CountDownLatch edtLatch1 = new CountDownLatch(1);
        final AtomicBoolean val1 = new AtomicBoolean();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                val1.set(testIO1.isSelect());
                edtLatch1.countDown();
            }
        });
        edtLatch1.await();
        assertFalse(val1.get());

        // now with error exit code
        process = new TestProcess(-1);
        callable = new TestCallable();
        callable.addProcess(process);

        final TestInputOutput testIO2 = new TestInputOutput(io);
        descriptor = new ExecutionDescriptor()
                .inputOutput(testIO2).frontWindowOnError(true);
        service = ExecutionService.newService(callable, descriptor, "Test");

        task = service.run();
        assertNotNull(task);
        process.destroy();
        assertEquals(-1, task.get().intValue());

        final CountDownLatch edtLatch2 = new CountDownLatch(1);
        final AtomicBoolean val2 = new AtomicBoolean();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                val2.set(testIO2.isSelect());
                edtLatch2.countDown();
            }
        });
        edtLatch2.await();
        assertTrue(val2.get());
    }

    public void testIOFrontOnErrorCancelled() throws InterruptedException {
        TestProcess process = new TestProcess(0);
        TestCallable callable = new TestCallable();
        callable.addProcess(process);

        final InputOutput io = IOProvider.getDefault().getIO("Test", new Action[] {});
        final TestInputOutput testIO = new TestInputOutput(io);

        ExecutionDescriptor descriptor = new ExecutionDescriptor()
                .inputOutput(testIO).frontWindowOnError(true);
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

        ExecutionService service = ExecutionService.newService(
                callable, descriptor, "Test");

        Future<Integer> task = service.run();
        assertNotNull(task);
        assertFalse(process.isFinished());

        task.cancel(true);
        // guaranteed process was not executed
        latch.countDown();

        assertTrue(task.isCancelled());
        assertFalse(process.isStarted());
        assertFalse(process.isFinished());

        final CountDownLatch edtLatch = new CountDownLatch(1);
        final AtomicBoolean val = new AtomicBoolean();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                val.set(testIO.isSelect());
                edtLatch.countDown();
            }
        });
        edtLatch.await();
        assertFalse(val.get());
    }

    private static InputOutputManager.InputOutputData getInputOutput(String name,
            boolean actions, String optionsPath) {

        synchronized (InputOutputManager.class) {
            InputOutputManager.InputOutputData data = InputOutputManager.getInputOutput(name, actions, optionsPath);
            // put it back
            if (data != null) {
                InputOutputManager.addInputOutput(data);
            }
            return data;
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

        private boolean finished;

        private boolean started;

        public TestProcess(int returnValue) {
            this(returnValue, TestInputUtils.prepareInputStream(
                    new String[] {"Process line 1", "Process line 2", "Process line 3"}, "\n",
                    Charset.defaultCharset(), true));
        }

        public TestProcess(int returnValue, InputStream is) {
            this.returnValue = returnValue;
            this.is = is;
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

    private static class TestInputOutput implements InputOutput {

        private final InputOutput io;

        private volatile boolean reset;

        private volatile boolean select;

        public TestInputOutput(InputOutput io) {
            this.io = io;
        }

        public boolean isReset() {
            return reset;
        }

        public void reset() {
            this.reset = true;
        }

        public boolean isSelect() {
            return select;
        }

        public void select() {
            this.select = true;
        }

        public void setOutputVisible(boolean value) {
            io.setOutputVisible(value);
        }

        public void setInputVisible(boolean value) {
            io.setInputVisible(value);
        }

        public void setFocusTaken(boolean value) {
            io.setFocusTaken(value);
        }

        public void setErrVisible(boolean value) {
            io.setErrVisible(value);
        }

        public void setErrSeparated(boolean value) {
            io.setErrSeparated(value);
        }

        public boolean isFocusTaken() {
            return io.isFocusTaken();
        }

        public boolean isErrSeparated() {
            return io.isErrSeparated();
        }

        public boolean isClosed() {
            return io.isClosed();
        }

        public OutputWriter getOut() {
            return new TestOutputWriter(this, io.getOut());
        }

        public Reader getIn() {
            return io.getIn();
        }

        public OutputWriter getErr() {
            return io.getErr();
        }

        public Reader flushReader() {
            return io.flushReader();
        }

        public void closeInputOutput() {
            io.closeInputOutput();
        }
    }

    private static class TestOutputWriter extends OutputWriter {

        private final TestInputOutput io;

        private final OutputWriter ow;

        public TestOutputWriter(TestInputOutput io, OutputWriter ow) {
            super(ow);
            this.io = io;
            this.ow = ow;
        }

        public void reset() throws IOException {
            ow.reset();
            io.reset();
        }

        public void println(String s, OutputListener l, boolean important) throws IOException {
            ow.println(s, l, important);
        }

        public void println(String s, OutputListener l) throws IOException {
            ow.println(s, l);
        }
    }
}
