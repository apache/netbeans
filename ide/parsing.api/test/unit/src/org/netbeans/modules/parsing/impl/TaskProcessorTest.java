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

package org.netbeans.modules.parsing.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.text.StyledDocument;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.editor.plain.PlainKit;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.IndexingAwareTestCase;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.TestEnvironmentFactory;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tomas Zezula
 */
public class TaskProcessorTest extends IndexingAwareTestCase {

    private static final int TIMEOUT = Integer.getInteger("TaskProcessorTest.timeout", 10); //NOI18N
    
    public TaskProcessorTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new TaskProcessorTest("testWarningWhenRunUserTaskCalledFromAWT"));        //NOI18N
        suite.addTest(new TaskProcessorTest("testDeadlock"));                                   //NOI18N
        suite.addTest(new TaskProcessorTest("testCancelCall"));                                 //NOI18N
        suite.addTest(new TaskProcessorTest("testTaskCall"));                                   //NOI18N
        suite.addTest(new TaskProcessorTest("testParserCall"));                                 //NOI18N
        suite.addTest(new TaskProcessorTest("testSlowCancelSampler"));                          //NOI18N
        suite.addTest(new TaskProcessorTest("testParserCancelInPRT"));                          //NOI18N
        suite.addTest(new TaskProcessorTest("testParserCancelInUT"));                           //NOI18N
        return suite;
    }
    
    private IndexerEmulator emu = new IndexerEmulator();
    
    @Override
    protected void setUp() throws Exception {        
        super.setUp();
        clearWorkDir();
        MockLookup.setInstances(new MockMimeLookup(), new TestEnvironmentFactory(), emu);
    }
    
    public void testWarningWhenRunUserTaskCalledFromAWT() throws Exception {
        this.clearWorkDir();
        final File _wd = this.getWorkDir();
        final FileObject wd = FileUtil.toFileObject(_wd);

        FileUtil.setMIMEType("foo", "text/foo");
        final FileObject foo = wd.createData("file.foo");
        final LogRecord[] warning = new LogRecord[1];
        final String msgTemplate = "ParserManager.parse called in AWT event thread by: {0}";  //NOI18N

        MockMimeLookup.setInstances(MimePath.parse("text/foo"), new FooParserFactory());
        Logger.getLogger(TaskProcessor.class.getName()).addHandler(new Handler() {
            public @Override void publish(LogRecord record) {
                if (record.getMessage().startsWith(msgTemplate)) {
                    warning[0] = record;
                }
            }

            public @Override void flush() {
            }

            public @Override void close() throws SecurityException {
            }
        });

        final StackTraceUserTask stackTraceUserTask = new StackTraceUserTask();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    ParserManager.parse(Collections.singleton(Source.create(foo)), stackTraceUserTask);
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        assertNotNull("No warning when calling ParserManager.parse from AWT", warning[0]);
        assertEquals("Wrong message", msgTemplate, warning[0].getMessage());
        assertEquals("Suspiciosly wrong warning message (is the caller identified correctly?)", stackTraceUserTask.caller, warning[0].getParameters()[0]);
    }
    
    public void testDeadlock() throws Exception {
        FileUtil.setMIMEType("foo", "text/foo");
        MockMimeLookup.setInstances(MimePath.parse("text/foo"), new FooParserFactory(), new PlainKit());
        MockMimeLookup.setInstances(MimePath.parse("text/plain"), new FooParserFactory(), new PlainKit());
        final File workingDir = getWorkDir();        
        final FileObject file = FileUtil.createData(new File(workingDir,"test.foo"));
        final Source src = Source.create(file);
        final DataObject dobj = DataObject.find(file);
        final EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
        final StyledDocument doc = ec.openDocument();
        final CountDownLatch start_a = new CountDownLatch(1);
        final CountDownLatch start_b = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(1);
        final CountDownLatch taskEnded = new CountDownLatch(1);
        final Collection<Pair<SchedulerTask,Class<? extends Scheduler>>> tasks = Collections.<Pair<SchedulerTask,Class<? extends Scheduler>>>singleton(
                Pair.<SchedulerTask,Class<? extends Scheduler>>of(
            new ParserResultTask<Parser.Result>() {
                @Override
                public void run(Result result, SchedulerEvent event) {
                    taskEnded.countDown();
                }

                @Override
                public int getPriority() {
                    return 1000;
                }

                @Override
                public Class<? extends Scheduler> getSchedulerClass() {
                    return null;
                }

                @Override
                public void cancel() {
                }                    
            }, null));
        TaskProcessor.addPhaseCompletionTasks(
                tasks,
                SourceAccessor.getINSTANCE().getCache(src),
                true);
        taskEnded.await();
        final Thread t = new Thread () {
            @Override
            public void run() {
                NbDocument.runAtomic(doc, new Runnable() {
                    @Override
                    public void run() {
                        start_a.countDown();
                        try {
                            start_b.await();
                            synchronized(TaskProcessor.INTERNAL_LOCK) {
                                end.await();
                            }
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });                    
            }
        };
        t.start();        
        synchronized(TaskProcessor.INTERNAL_LOCK) {
            start_b.countDown();
            start_a.await();
            SourceAccessor.getINSTANCE().getCache(src).invalidate();
            TaskProcessor.removePhaseCompletionTasks(Collections.<SchedulerTask>singleton(tasks.iterator().next().first()), src);
        }
        end.countDown();
    }

    public void testCancelCall () {                
        final FooTask task = new FooTask();
        final FooParser parser = new FooParser(Collections.<FooParserFactory.Target,Runnable>emptyMap());
        boolean success = false;
        try {
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                TaskProcessor.cancelTask(task, Parser.CancelReason.USER_TASK);
            }
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling cancelTask under INTERNAL_LOCK", success);       //NOI18N

        success = false;
        try {
            TaskProcessor.cancelTask(task, Parser.CancelReason.USER_TASK);
            success = true;
        } catch (AssertionError ae) {}
        assertTrue("AssertionError not expected when calling cancelTask without INTERNAL_LOCK", success); //NOI18N
        assertEquals(1, task.cancelCount);

        success = false;
        try {
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                TaskProcessor.cancelParser(parser, true, Parser.CancelReason.USER_TASK, null);
            }
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling cancelParser under INTERNAL_LOCK", success);       //NOI18N

        success = false;
        try {
            TaskProcessor.cancelParser(parser, true, Parser.CancelReason.USER_TASK, null);
            success = true;
        } catch (AssertionError ae) {}
        assertTrue("AssertionError not expected when calling cancelParser without INTERNAL_LOCK", success); //NOI18N
        assertEquals(1, parser.cancelCount);
    }

    public void testTaskCall () throws Exception {
        FileUtil.setMIMEType("foo", "text/foo");
        MockMimeLookup.setInstances(MimePath.parse("text/foo"), new FooParserFactory(), new PlainKit());
        final File workingDir = getWorkDir();
        final FileObject file = FileUtil.createData(new File(workingDir,"test.foo"));
        final Source src = Source.create(file);

        final FooTask task = new FooTask();
        final FooUserTask userTask = new FooUserTask();
        final FooEmbeddingProvider embProv = new FooEmbeddingProvider();
        final Parser.Result result = new FooParserResult(src.createSnapshot());
        final ResultIterator[] it = new ResultIterator[1];
        ParserManager.parse(Collections.singleton(src), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                it[0] = resultIterator;
            }
        });

        boolean success = false;
        try {
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                TaskProcessor.callParserResultTask(task, result, null);
            }
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling callParserResultTask under INTERNAL_LOCK", success);       //NOI18N

        success = false;
        try {
            TaskProcessor.callParserResultTask(task, result, null);
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling callParserResultTask without parser lock", success); //NOI18N

        success = false;
        try {
            Utilities.acquireParserLock();
            try {
                TaskProcessor.callParserResultTask(task, result, null);
                success = true;
            } finally {
                Utilities.releaseParserLock();
            }
        } catch (AssertionError ae) {}
        assertTrue("AssertionError not expected when calling callParserResultTask with parser lock", success); //NOI18N
        assertEquals(1, task.runCount);


        success = false;
        try {
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                TaskProcessor.callEmbeddingProvider(embProv, src.createSnapshot());
            }
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling callEmbeddingProvider under INTERNAL_LOCK", success);       //NOI18N

        success = false;
        try {
            TaskProcessor.callEmbeddingProvider(embProv, src.createSnapshot());
            success = true;
        } catch (AssertionError ae) {}
        assertTrue("AssertionError not expected when calling callEmbeddingProvider without INTERNAL_LOCK", success); //NOI18N
        assertEquals(1, embProv.runCount);


        success = false;
        try {
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                TaskProcessor.callUserTask(userTask, it[0]);
            }
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling callUserTask under INTERNAL_LOCK", success);       //NOI18N

        success = false;
        try {
            TaskProcessor.callUserTask(userTask, it[0]);
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling callUserTask without parser lock", success); //NOI18N

        success = false;
        try {
            Utilities.acquireParserLock();
            try {
                TaskProcessor.callUserTask(userTask, it[0]);
                success = true;
            } finally {
                Utilities.releaseParserLock();
            }
        } catch (AssertionError ae) {}
        assertTrue("AssertionError not expected when calling callUserTask with parser lock", success); //NOI18N
        assertEquals(1, userTask.runCount);
        
    }

    public void testParserCall () throws Exception {
        FileUtil.setMIMEType("foo", "text/foo");
        MockMimeLookup.setInstances(MimePath.parse("text/foo"), new FooParserFactory(), new PlainKit());
        final File workingDir = getWorkDir();
        final FileObject file = FileUtil.createData(new File(workingDir,"test.foo"));
        final Source src = Source.create(file);
        final FooParser parser = new FooParser(Collections.<FooParserFactory.Target,Runnable>emptyMap());
        final FooTask task = new FooTask();

        boolean success = false;
        try {
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                TaskProcessor.callParse(parser, src.createSnapshot(), task, null);
            }
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling callParse under INTERNAL_LOCK", success);       //NOI18N

        success = false;
        try {
            TaskProcessor.callParse(parser, src.createSnapshot(), task, null);
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling callParse without parser lock", success); //NOI18N

        success = false;
        try {
            Utilities.acquireParserLock();
            try {
                TaskProcessor.callParse(parser, src.createSnapshot(), task, null);
                success = true;
            } finally {
                Utilities.releaseParserLock();
            }
        } catch (AssertionError ae) {}
        assertTrue("AssertionError not expected when calling callParse with parser lock", success); //NOI18N
        assertEquals(1, parser.parseCount);

        success = false;
        try {
            synchronized (TaskProcessor.INTERNAL_LOCK) {
                TaskProcessor.callGetResult(parser, task);
            }
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling callGetResult under INTERNAL_LOCK", success);       //NOI18N

        success = false;
        try {
            TaskProcessor.callGetResult(parser, task);
            success = true;
        } catch (AssertionError ae) {}
        assertFalse("AssertionError expected when calling callGetResult without parser lock", success); //NOI18N

        success = false;
        try {
            Utilities.acquireParserLock();
            try {
                TaskProcessor.callGetResult(parser, task);
                success = true;
            } finally {
                Utilities.releaseParserLock();
            }
        } catch (AssertionError ae) {}
        assertTrue("AssertionError not expected when calling callGetResult with parser lock", success); //NOI18N
        assertEquals(1, parser.resultCount);
        
    }

    @RandomlyFails
    public void testSlowCancelSampler() throws Exception {
        //Enable sampling
        TaskProcessor.SAMPLING_ENABLED = true;
        //Set bigger profiling start report time outs to make the test deterministic
        //on loaded build machenes
        System.setProperty("org.netbeans.modules.parsing.api.taskcancel.slowness.start","3000");    //NOI18N
        System.setProperty("org.netbeans.modules.parsing.api.taskcancel.slowness.report","3000");  //NOI18N
        final File wd = getWorkDir();
        FileUtil.setMIMEType("foo", "text/foo");
        MockMimeLookup.setInstances(MimePath.parse("text/foo"), new FooParserFactory(), new PlainKit());
        final File srcFolder = new File (wd,"src");
        final FileObject srcRoot = FileUtil.createFolder(srcFolder);
        final FileObject srcFile = srcRoot.createData("test.foo");  //NOI18N
        final Source source = Source.create(srcFile);
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger timeToWait = new AtomicInteger(0);
        final MockProfiler mockProfiler = new MockProfiler();
        final Logger log = Logger.getLogger("org.netbeans.modules.parsing.impl.SelfProfile");   //NOI18N
        log.addHandler(mockProfiler);
        log.setLevel(Level.FINEST);
        try {
            final Callable<Void> runCB = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    latch.countDown();
                    return null;
                }
            };
            final Callable<Void> cancelCB = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    try {
                        Thread.sleep(timeToWait.get());
                    } catch (InterruptedException ie) {}
                    return null;
                }
            };

            //No profiling should be started when cancel is immediate
            SlowCancelTask sct = new SlowCancelTask(runCB, cancelCB);
            mockProfiler.expect(EnumSet.noneOf(MockProfiler.Event.class));
            TaskProcessor.addPhaseCompletionTasks(
                    Arrays.<Pair<SchedulerTask,Class<? extends Scheduler>>>asList(Pair.<SchedulerTask,Class<? extends Scheduler>>of(sct,sct.getSchedulerClass())),
                    SourceAccessor.getINSTANCE().getCache(source),
                    false);
            assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
            ParserManager.parse(Arrays.asList(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                }
            });
            assertTrue(mockProfiler.verify(5000));

            //Profiling should be started when cancel is delayed when cancel is slower
            timeToWait.set(3500);
            sct = new SlowCancelTask(runCB, cancelCB);
            mockProfiler.expect(EnumSet.of(MockProfiler.Event.STARTED, MockProfiler.Event.CANCELED));
            TaskProcessor.addPhaseCompletionTasks(
                    Arrays.<Pair<SchedulerTask,Class<? extends Scheduler>>>asList(Pair.<SchedulerTask,Class<? extends Scheduler>>of(sct,sct.getSchedulerClass())),
                    SourceAccessor.getINSTANCE().getCache(source),
                    false);
            assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
            ParserManager.parse(Arrays.asList(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                }
            });
            assertTrue(mockProfiler.verify(5000));

            //Profiling should be started and report sent when cancel is very slow
            timeToWait.set(6500);
            sct = new SlowCancelTask(runCB, cancelCB);
            mockProfiler.expect(EnumSet.of(MockProfiler.Event.STARTED, MockProfiler.Event.LOGGED));
            TaskProcessor.addPhaseCompletionTasks(
                    Arrays.<Pair<SchedulerTask,Class<? extends Scheduler>>>asList(Pair.<SchedulerTask,Class<? extends Scheduler>>of(sct,sct.getSchedulerClass())),
                    SourceAccessor.getINSTANCE().getCache(source),
                    false);
            assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
            ParserManager.parse(Arrays.asList(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                }
            });
            assertTrue(mockProfiler.verify(8000));
        } finally {
            log.removeHandler(mockProfiler);
        }
    }

    public void testParserCancelInPRT() throws Exception {
        FileUtil.setMIMEType("foo", "text/foo");    //NOI18N
        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        final FileObject file = wd.createData("test.foo");  //NOI18N
        final FooParserFactory factory = new FooParserFactory();
        final ParserResultTask<FooParserResult> task = new ParserResultTask<FooParserResult>() {
            @Override
            public void run(FooParserResult result, SchedulerEvent event) {
            }

            @Override
            public int getPriority() {
                return 0;
            }

            @Override
            public Class<? extends Scheduler> getSchedulerClass() {
                return Scheduler.SELECTED_NODES_SENSITIVE_TASK_SCHEDULER;
            }

            @Override
            public void cancel() {
            }
        };
        final CountDownLatch parserCalled = new CountDownLatch(1);
        final CountDownLatch cancelCalled = new CountDownLatch(1);
        final BlockingQueue<Boolean> result = new ArrayBlockingQueue<Boolean>(1);
        final Runnable parseCallback = new Runnable() {
            @Override
            public void run() {
                parserCalled.countDown();
                try {
                    final boolean canceled = cancelCalled.await(TIMEOUT, TimeUnit.SECONDS);
                    result.offer(canceled);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        final Runnable cancelCallback = new Runnable() {
            @Override
            public void run() {
                cancelCalled.countDown();
            }
        };
        factory.setCallback(FooParserFactory.Target.PARSE, parseCallback);
        factory.setCallback(FooParserFactory.Target.CANCEL, cancelCallback);
        try {
            MockMimeLookup.setInstances(MimePath.parse("text/foo"), factory);  //NOI18N
            final Source src = Source.create(file);
            TaskProcessor.addPhaseCompletionTasks(
                    Arrays.<Pair<SchedulerTask,Class<? extends Scheduler>>>asList(Pair.<SchedulerTask,Class<? extends Scheduler>>of(
                        task,
                        task.getSchedulerClass())),
                    SourceAccessor.getINSTANCE().getCache(src),
                    false);
            assertTrue(parserCalled.await(TIMEOUT, TimeUnit.SECONDS));
            ParserManager.parse(
                Collections.singleton(src),
                new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                    }
                });
            final Boolean res = result.poll(TIMEOUT, TimeUnit.SECONDS);
            assertNotNull(res);
            assertTrue(res);
        } finally {
            factory.setCallback(FooParserFactory.Target.PARSE,null);
            factory.setCallback(FooParserFactory.Target.CANCEL,null);
        }
    }

    public void testParserCancelInUT() throws Exception {
        FileUtil.setMIMEType("foo", "text/foo");    //NOI18N
        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        final FileObject file = wd.createData("test.foo");  //NOI18N
        final FooParserFactory factory = new FooParserFactory();        
        final CountDownLatch parserCalled = new CountDownLatch(1);
        final CountDownLatch cancelCalled = new CountDownLatch(1);
        final BlockingQueue<Boolean> result = new ArrayBlockingQueue<Boolean>(1);
        final Runnable parseCallback = new Runnable() {
            @Override
            public void run() {
                parserCalled.countDown();
                try {
                    final boolean canceled = cancelCalled.await(TIMEOUT, TimeUnit.SECONDS);
                    result.offer(canceled);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        final Runnable cancelCallback = new Runnable() {
            @Override
            public void run() {
                cancelCalled.countDown();
            }
        };
        factory.setCallback(FooParserFactory.Target.PARSE, parseCallback);
        factory.setCallback(FooParserFactory.Target.CANCEL, cancelCallback);
        try {
            MockMimeLookup.setInstances(MimePath.parse("text/foo"), factory);  //NOI18N
            final Source src = Source.create(file);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ParserManager.parse(
                            Collections.singleton(src),
                            new UserTask() {
                                @Override
                                public void run(ResultIterator resultIterator) throws Exception {
                                    resultIterator.getParserResult();
                                }
                            });
                    } catch (ParseException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }).start();
            assertTrue(parserCalled.await(TIMEOUT, TimeUnit.SECONDS));
            ParserManager.parse(
                Collections.singleton(src),
                new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                    }
                });
            final Boolean res = result.poll(TIMEOUT, TimeUnit.SECONDS);
            assertNotNull(res);
            assertFalse(res);
        } finally {
            factory.setCallback(FooParserFactory.Target.PARSE,null);
            factory.setCallback(FooParserFactory.Target.CANCEL,null);
        }
    }

    public static final class FooParserFactory extends ParserFactory {

        enum Target {
            PARSE,
            CANCEL
        }

        private final Map<Target,Runnable> callbacks = Collections.synchronizedMap(
                new EnumMap<Target, Runnable>(Target.class));

        public void setCallback(
            @NonNull final Target target,
            @NullAllowed final Runnable callback) {
            Parameters.notNull("target", target);   //NOI18N
            callbacks.put(target, callback);
        }

        @Override
        public Parser createParser(Collection<Snapshot> snapshots) {
            return new FooParser(callbacks);
        }
    }

    protected static final class FooParser extends Parser {
        private final Map<FooParserFactory.Target,Runnable> callbacks;
        private FooParserResult result;
        private int cancelCount;
        private int parseCount;
        private int resultCount;

        FooParser(@NonNull final Map<FooParserFactory.Target,Runnable> callbacks) {
            Parameters.notNull("callbacks", callbacks); //NOI18N
            this.callbacks = callbacks;
        }

        public @Override void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
            callCallBack(FooParserFactory.Target.PARSE);
            parseCount++;
            result = new FooParserResult((snapshot));
        }

        public @Override Result getResult(Task task) throws ParseException {
            resultCount++;
            return result;
        }

        public @Override void cancel() {            
            cancelCount++;
        }

        @Override
        public void cancel(CancelReason reason, SourceModificationEvent event) {
           callCallBack(FooParserFactory.Target.CANCEL);
        }

        public @Override void addChangeListener(ChangeListener changeListener) {
        }

        public @Override void removeChangeListener(ChangeListener changeListener) {
        }

        private void callCallBack(@NonNull final FooParserFactory.Target target) {
            Parameters.notNull("target", target);   //NOI18N
            final Runnable r = callbacks.get(target);
            if (r != null) {
                r.run();
            }
        }
    }

    private static final class FooParserResult extends Parser.Result {
        public FooParserResult(Snapshot snapshot) {
            super(snapshot);
        }

        protected @Override void invalidate() {
        }
    }

    private static final class StackTraceUserTask extends UserTask {
        public StackTraceElement caller;
        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            ArrayList<StackTraceElement> filteredStackTrace = new ArrayList<StackTraceElement>();
            StackTraceElement [] stackTrace = Thread.currentThread().getStackTrace();
            boolean active = false;
            for(StackTraceElement e : stackTrace) {
                if (!active) {
                    if (e.getClassName().equals(TaskProcessor.class.getName()) && e.getMethodName().equals("runUserTask")) {
                        active = true;
                    } else {
                        continue;
                    }
                }
                filteredStackTrace.add(e);
            }
            caller = Utilities.findCaller(filteredStackTrace.toArray(new StackTraceElement[0]));
        }
    }

    private static class FooTask extends  ParserResultTask  {
        
        private int cancelCount;
        private int runCount;

        @Override
        public void run(Result result, SchedulerEvent event) {
            runCount++;
        }

        @Override
        public int getPriority() {
            return 10;
        }

        @Override
        public Class<? extends Scheduler> getSchedulerClass() {
            return Scheduler.SELECTED_NODES_SENSITIVE_TASK_SCHEDULER;
        }

        @Override
        public void cancel() {
            cancelCount++;
        }
    }

    private static class FooEmbeddingProvider extends EmbeddingProvider {

        private int runCount;

        @Override
        public List<Embedding> getEmbeddings(Snapshot snapshot) {
            runCount++;
            return Collections.<Embedding>emptyList();
        }

        @Override
        public int getPriority() {
            return 10;
        }

        @Override
        public void cancel() {
        }

    }

    private static class FooUserTask extends UserTask {

        int runCount;

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            runCount++;
        }
    }

    private static class SlowCancelTask extends ParserResultTask {

        private final Semaphore sem = new Semaphore(0);
        private final Callable<Void> cancelStartCallBack;
        private final Callable<Void> runStartCallBack;

        public SlowCancelTask (
                final @NonNull Callable<Void> runStartCallBack,
                final @NonNull Callable<Void> cancelStartCallBack) {
            assert runStartCallBack != null;
            assert cancelStartCallBack != null;
            this.runStartCallBack = runStartCallBack;
            this.cancelStartCallBack = cancelStartCallBack;
        }

        public void inc() {
            sem.release();
        }

        public void dec() throws InterruptedException {
            sem.acquire();
        }

        @Override
        public void run(Result result, SchedulerEvent event) {
            try {
                runStartCallBack.call();
                dec();
            } catch (Exception ie) {}
        }

        @Override
        public void cancel() {
            try {
                cancelStartCallBack.call();
                inc();
            } catch (Exception e) {}
        }

        @Override
        public int getPriority() {
            return 1;
        }

        @Override
        public Class<? extends Scheduler> getSchedulerClass() {
            return Scheduler.SELECTED_NODES_SENSITIVE_TASK_SCHEDULER;
        }
    }

    private static class MockProfiler extends Handler {

        private enum Event {
            STARTED,
            CANCELED,
            LOGGED
        };

        private final Lock lck = new ReentrantLock();
        private final Condition cnd = lck.newCondition();

        private volatile Set<Event> expectedEvents;

        public void expect (final @NonNull Set<Event> expectedEvents) {
            lck.lock();
            try {
                this.expectedEvents = EnumSet.copyOf(expectedEvents);
            } finally {
                lck.unlock();
            }
        }

        public boolean verify(int ms) throws InterruptedException {
            final long st = System.currentTimeMillis();
            lck.lock();
            try {
                while (!expectedEvents.isEmpty()) {
                    if (System.currentTimeMillis()-st >= ms) {
                        return false;
                    }
                    cnd.await(ms, TimeUnit.MILLISECONDS);
                }
            } finally {
                lck.unlock();
            }
            return true;
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        @Override
        public void publish(LogRecord record) {
            final String msg = record.getMessage();
            if ("STARTED".equals(msg)) {        //NOI18N
                handleEvent(Event.STARTED);
            } else if ("LOGGED".equals(msg)) {  //NOI18N
                handleEvent(Event.LOGGED);
            } else if ("CANCEL".equals(msg)) {    //NOI18N
                handleEvent(Event.CANCELED);
            }
        }

        private void handleEvent(final @NonNull Event event) {
            lck.lock();
            try {
                expectedEvents.remove(event);                
                if (expectedEvents.isEmpty()) {
                    cnd.signalAll();
                }
            } finally {
                lck.unlock();
            }
        }

    }
}
