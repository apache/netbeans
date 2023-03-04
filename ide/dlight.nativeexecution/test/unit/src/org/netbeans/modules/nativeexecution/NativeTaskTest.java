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
package org.netbeans.modules.nativeexecution;

import org.netbeans.modules.nativeexecution.ConcurrentTasksSupport.Counters;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.CountDownLatch;
import junit.framework.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.openide.util.Exceptions;

/**
 *
 * @author ak119685
 */
public class NativeTaskTest extends NativeExecutionBaseTestCase {

    public static Test suite() {
        return new NativeExecutionBaseTestSuite(NativeTaskTest.class);
    }

    public NativeTaskTest(String name) {
        super(name);
    }

    public NativeTaskTest(String name, ExecutionEnvironment env) {
        super(name, env);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

//    public void testSimple() {
//        ExternalTerminal term = ExternalTerminalProvider.getTerminal(ExecutionEnvironmentFactory.getLocal(), "gnome-terminal"); // NOI18N
//        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(ExecutionEnvironmentFactory.getLocal());
//        npb.setExecutable("/bin/ls").useExternalTerminal(term); // NOI18N
//        StringWriter result = new StringWriter();
//        ExecutionDescriptor descriptor = new ExecutionDescriptor().inputOutput(InputOutput.NULL).outProcessorFactory(new InputRedirectorFactory(result));
//        ExecutionService execService = ExecutionService.newService(
//                npb, descriptor, "Demangling function "); // NOI18N
//
//        Future<Integer> res = execService.run();
//
//        try {
//            System.out.println("Result: " + res.get()); // NOI18N
//        } catch (InterruptedException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (ExecutionException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//
//        System.out.println(result.toString());
//    }

//    @Test
    private static final String CNT_OUT_MATCH = "Output matches"; // NOI18N
    private static final String CNT_EXECUTION_SUCCESS = "Successful execution"; // NOI18N
    private static final String CNT_TASKS = "Tasks submitted"; // NOI18N

    public void testParallelExecution() {

        System.out.println("testParallelExecution"); // NOI18N
        final int threadsNum = 100;
        final CountDownLatch latch = new CountDownLatch(threadsNum);
        final CountDownLatch start = new CountDownLatch(1);
        final ConcurrentTasksSupport.Counters counters = new ConcurrentTasksSupport.Counters();

        Thread[] threads = new Thread[threadsNum];

        for (int i = 0; i < threadsNum; i++) {
            threads[i] = new Thread(new Worker(counters, latch, start));
            threads[i].start();
        }

        start.countDown();

        try {
            latch.await();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        for (int i = 0; i < threadsNum; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        counters.dump(System.out);

        assertEquals(threadsNum, counters.getCounter(CNT_OUT_MATCH).get());
        assertEquals(threadsNum, counters.getCounter(CNT_EXECUTION_SUCCESS).get());
    }

    private static final class Worker implements Runnable {

        private final CountDownLatch start;
        private final CountDownLatch latch;
        private Counters counters;

        public Worker(ConcurrentTasksSupport.Counters counters, CountDownLatch latch, CountDownLatch start) {
            this.start = start;
            this.latch = latch;
            this.counters = counters;
            counters.getCounter(CNT_TASKS).incrementAndGet();
        }

        private void doit() {
            final ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();
            final String cmd = "echo"; // NOI18N
            final String expectedResult = "TEST"; // NOI18N

            try {
                NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
                npb.setExecutable(cmd).setArguments(expectedResult);
                Process process = npb.call();

                String out = ProcessUtils.readProcessOutputLine(process);

                if (expectedResult.equals(out)) {
                    counters.getCounter(CNT_OUT_MATCH).incrementAndGet();
                }

                try {
                    int rc = process.waitFor();
                    if (rc == 0) {
                        counters.getCounter(CNT_EXECUTION_SUCCESS).incrementAndGet();
                    }
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public void run() {
            try {
                start.await();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }

            doit();

            latch.countDown();
        }
    }

    private static class InputRedirectorFactory implements ExecutionDescriptor.InputProcessorFactory {

        private final Writer writer;

        public InputRedirectorFactory(Writer writer) {
            this.writer = writer;
        }

        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.copying(writer);
        }
    }
    /**
     * Test of run method, of class NativeTask.
     */
//    @Test
//    public void testRun() {
//        System.out.println("run"); // NOI18N
//
//        final ExecutionEnvironment ee =
//                ExecutionEnvironmentFactory.createNew(System.getProperty("user.name"), "localhost", 22); // NOI18N
//
////        MacroExpander macroExpander = MacroExpanderFactory.getExpander(ee);
////        try {
////            String path = macroExpander.expandMacros("$osname-$platform"); // NOI18N
////            System.out.println("PATH IS " + path);
////        } catch (ParseException ex) {
////            System.out.println("Parse exception! Pos = " + ex.getErrorOffset());
////        }
//
//        final String cmd = "/export/home/ak119685/welcome.sh"; // NOI18N
//
//        ChangeListener l = new ChangeListener() {
//
//            public void stateChanged(ChangeEvent e) {
//                NativeProcess process = (NativeProcess) e.getSource();
//                State newState = process.getState();
//
//                if (newState == State.STARTING) {
//                    return;
//                }
//
//                if (newState == State.ERROR) {
//                    System.out.println("Unable to start process!"); // NOI18N
//                    return;
//                }
//                try {
//                    System.out.println("Process " + process.toString() + " [" + process.getPID() + "] -> " + newState); // NOI18N
//                } catch (IOException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }
//        };
//
//        ExternalTerminal term = ExternalTerminalProvider.getTerminal(ExecutionEnvironmentFactory.getLocal(), "gnome-terminal").setTitle("My favorite title"); // NOI18N
//        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(ee);
//        npb.setExecutable(cmd).setArguments("1", "2").addEnvironmentVariable("MY_VAR", "/temp/xx/$platform").setWorkingDirectory("/tmp").addNativeProcessListener(l).useExternalTerminal(term); // NOI18N
//        ExecutionDescriptor descr = new ExecutionDescriptor().outLineBased(true).outProcessorFactory(new ExecutionDescriptor.InputProcessorFactory() {
//
//            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
//                return InputProcessors.copying(new OutputStreamWriter(System.out));
//            }
//        });
//
//        ExecutionService service = ExecutionService.newService(npb, descr, "test"); // NOI18N
//
//        Future<Integer> result = service.run();
//        Integer res = null;
//        try {
//            res = result.get();
//        } catch (InterruptedException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (ExecutionException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//
//        System.out.println("RESULT == " + res); // NOI18N
//
////
////        final NativeTaskListener l = new NativeTaskListener() {
////
////            public void taskStarted(NativeTask task) {
////                System.out.println(task.getPID() + " - started");
////            }
////
////            public void taskFinished(NativeTask task, Integer result) {
////                System.out.println(task.getPID() + " - finished");
////            }
////
////            public void taskCancelled(NativeTask task, CancellationException cex) {
////                System.out.println(task.getPID() + " - cancelled");
////            }
////
////            public void taskError(NativeTask task, Throwable t) {
////                System.out.println(task.getPID() + " - error");
////            }
////        };
////
////        StringBuffer outBuffer = new StringBuffer();
////        final ExecutionEnvironment ee = new ExecutionEnvironment(null, null);
////
////        NativeTask nt = new NativeTask(ee, "/bin/uname", new String[]{"-s"});
////        nt.redirectOutTo(new StringBufferWriter(outBuffer));
////        nt.addListener(l);
////        nt.submit();
////        try {
////            nt.get();
////        } catch (InterruptedException ex) {
////            Exceptions.printStackTrace(ex);
////        } catch (ExecutionException ex) {
////            Exceptions.printStackTrace(ex);
////        }
////
////        System.out.println(outBuffer.toString());
////
////
////        Thread[] ts = new Thread[10];
////        for (int i = 0; i < 10; i++) {
////            ts[i] = new Thread(new Runnable() {
////                public void run() {
////                    StringBuffer outBuffer = new StringBuffer();
//////                    NativeTask nt = new NativeTask(ee, "/bin/uname", new String[]{"-s"});
////                    NativeTask nt = new NativeTask("/tmp/qq");
////                    nt.redirectOutTo(new StringBufferWriter(outBuffer));
////                    nt.addListener(l);
////                    nt.submit();
////                    try {
////                        nt.get();
////                    } catch (InterruptedException ex) {
////                        Exceptions.printStackTrace(ex);
////                    } catch (ExecutionException ex) {
////                        Exceptions.printStackTrace(ex);
////                    }
////
////                    System.out.println("" + (++count) + ") " + nt.getPID() + " - " + outBuffer.toString());
////                }
////            });
////        }
////
////        for (int i = 0; i < 10; i++) {
////            ts[i].start();
////        }
////
////        for (int i = 0; i < 10; i++) {
////            try {
////                ts[i].join();
////            } catch (InterruptedException ex) {
////                Exceptions.printStackTrace(ex);
////            }
////        }
////
////        final NativeTaskListener l = new NativeTaskListener() {
////
////            public void taskStarted(NativeTask task) {
////                System.out.println(task.getPID() + " - started");
////            }
////
////            public void taskFinished(NativeTask task, Integer result) {
////                System.out.println(task.getPID() + " - finished");
////            }
////
////            public void taskCancelled(NativeTask task, CancellationException cex) {
////                System.out.println(task.getPID() + " - cancelled");
////            }
////
////            public void taskError(NativeTask task, Throwable t) {
////                System.out.println(task.getPID() + " - error");
////            }
////        };
////
////        StringBuffer outBuffer = new StringBuffer();
////        final ExecutionEnvironment ee = new ExecutionEnvironment(null, null);
////
////        NativeTask nt = new NativeTask(ee, "/bin/uname", new String[]{"-s"});
////        nt.redirectOutTo(new StringBufferWriter(outBuffer));
////        nt.addListener(l);
////        nt.submit();
////        try {
////            nt.get();
////        } catch (InterruptedException ex) {
////            Exceptions.printStackTrace(ex);
////        } catch (ExecutionException ex) {
////            Exceptions.printStackTrace(ex);
////        }
////
////        System.out.println(outBuffer.toString());
////
////
////        Thread[] ts = new Thread[10];
////        for (int i = 0; i < 10; i++) {
////            ts[i] = new Thread(new Runnable() {
////                public void run() {
////                    StringBuffer outBuffer = new StringBuffer();
//////                    NativeTask nt = new NativeTask(ee, "/bin/uname", new String[]{"-s"});
////                    NativeTask nt = new NativeTask("/tmp/qq");
////                    nt.redirectOutTo(new StringBufferWriter(outBuffer));
////                    nt.addListener(l);
////                    nt.submit();
////                    try {
////                        nt.get();
////                    } catch (InterruptedException ex) {
////                        Exceptions.printStackTrace(ex);
////                    } catch (ExecutionException ex) {
////                        Exceptions.printStackTrace(ex);
////                    }
////
////                    System.out.println("" + (++count) + ") " + nt.getPID() + " - " + outBuffer.toString());
////                }
////            });
////        }
////
////        for (int i = 0; i < 10; i++) {
////            ts[i].start();
////        }
////
////        for (int i = 0; i < 10; i++) {
////            try {
////                ts[i].join();
////            } catch (InterruptedException ex) {
////                Exceptions.printStackTrace(ex);
////            }
////        }
//
////        NativeTask instance = new NativeTask("");
////        instance.run();
//        // TODO review the generated test code and remove the default call to fail.
//
//        final CharArrayWriter errWriter = new CharArrayWriter();
////
////        int tcount = 1;
////        Thread[] threads = new Thread[tcount];
////        for (int i = 0; i < tcount; i++) {
////            threads[i] = new Thread(new Runnable() {
////
////                public void run() {
////                    final NativeTask task = new NativeTask("/bin/lsss /");
////                    task.redirectErrTo(errWriter);
////                    task.submit(true, false);
////                    System.out.println("PID is " + task.getPID());
////
////                    try {
////                        System.out.println("Result: " + task.get());
////                    } catch (InterruptedException ex) {
////                        Exceptions.printStackTrace(ex);
////                    } catch (ExecutionException ex) {
////                        Exceptions.printStackTrace(ex);
////                    }
////
////                    System.out.println("ERROR: '" + errWriter.toString() + "'");
////
////                    try {
////                        System.out.println(task.invoke(false));
////                    } catch (Exception ex) {
////                        Exceptions.printStackTrace(ex);
////                    }
////
////                }
////            });
////        }
////
////        for (int i = 0; i < tcount; i++) {
////            threads[i].start();
////        }
////
////        for (int i = 0; i < tcount; i++) {
////            try {
////                threads[i].join();
////            } catch (InterruptedException ex) {
////                Exceptions.printStackTrace(ex);
////            }
////        }
//
//        System.out.println("Here we are!"); // NOI18N
//
////        fail("The test case is a prototype.");
//    }
}
