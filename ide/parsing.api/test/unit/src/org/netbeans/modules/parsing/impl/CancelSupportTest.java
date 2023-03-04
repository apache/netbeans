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

package org.netbeans.modules.parsing.impl;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.modules.parsing.api.ParsingTestBase;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.parsing.spi.support.CancelSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 * @author Tomas Zezula
 */
public class CancelSupportTest extends ParsingTestBase {

    public static final int TIME = Integer.getInteger("RepositoryUpdaterTest.timeout", 5000);                 //NOI18N
    public static final int NEGATIVE_TIME = Integer.getInteger("RepositoryUpdaterTest.negative-timeout", 5000); //NOI18N
    /*
    private static final int TIME = RepositoryUpdaterTest.TIME;
    public static final int NEGATIVE_TIME = RepositoryUpdaterTest.NEGATIVE_TIME;
    */
    private static final String EXTENSION = "test"; //NOI18N
    private static final String MIME = "text/x-test";   //NOI18N
    private static final RequestProcessor RP = new RequestProcessor(CancelSupportTest.class);

    private Source src;
    private SourceCache cache;
    private TestTask task;
    private CancelTask cancelTask;
    private Logger tpLogger;
    private TestHandler tpHandler;
    private Level tpLevel;

    public CancelSupportTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockMimeLookup.setInstances(MimePath.get(MIME), new TestParser.Factory());
        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        final FileObject testFile = FileUtil.createData(
            wd,
            String.format("file.%s",    //NOI18N
                EXTENSION));
        FileUtil.setMIMEType(EXTENSION,MIME);
        src = Source.create(testFile);
        cache = SourceAccessor.getINSTANCE().getCache(src);
        task = new TestTask();
        cancelTask = new CancelTask();
        tpLogger = Logger.getLogger(TaskProcessor.class.getName());
        tpLevel = tpLogger.getLevel();
        tpLogger.setLevel(Level.FINE);
        tpHandler = new TestHandler();
        tpLogger.addHandler(tpHandler);
    }

    @Override
    protected void tearDown() throws Exception {
        task.signalProgress();
        tpLogger.removeHandler(tpHandler);
        tpLogger.setLevel(tpLevel);
        TaskProcessor.removePhaseCompletionTasks(Arrays.asList(task, cancelTask), src);
        super.tearDown();        
    }



    public void testCancel_cancel_in_task_run() throws Exception {        
        TaskProcessor.addPhaseCompletionTasks(
            Collections.singleton(Pair.<SchedulerTask,Class<? extends Scheduler>>of(
                task,
                task.getSchedulerClass())),
            cache,
            false);
        assertTrue("TestTask started.", task.awaitStart(true));  //NOI18N
        TaskProcessor.addPhaseCompletionTasks(
            Collections.singleton(Pair.<SchedulerTask,Class<? extends Scheduler>>of(
                cancelTask,
                cancelTask.getSchedulerClass())),
            cache,
            false);
        assertTrue("Task cancelled", task.isABCancelled()); //NOI18N
        assertTrue("Task cancelled", task.isCSCancelled()); //NOI18N
        task.signalProgress();
        assertTrue("Test task finished", task.awaitDone(true)); //NOI18N
    }

    public void testCancel_cancel_before_should_run() throws Exception {
        final AtomicBoolean added = new AtomicBoolean();
        tpHandler.on(
            "Set current request to: {0}",  //NOI18N
            new Action() {
                @Override
                public void run(final Object[] params) throws Exception {
                    if (getTaskFromRequest((TaskProcessor.Request)params[0]) == task) {
                        if (added.compareAndSet(false, true)) {
                            RP.submit(new Runnable() {
                                @Override
                                public void run() {
                                    TaskProcessor.addPhaseCompletionTasks(
                                        Collections.singleton(Pair.<SchedulerTask,Class<? extends Scheduler>>of(
                                            cancelTask,
                                            cancelTask.getSchedulerClass())),
                                        cache,
                                        false);
                                    }
                            }).get();
                        } else {
                            task.setRerun(true);
                        }
                    }
                }
            });
        TaskProcessor.addPhaseCompletionTasks(
            Collections.singleton(Pair.<SchedulerTask,Class<? extends Scheduler>>of(
                task,
                task.getSchedulerClass())),
            cache,
            false);
        assertFalse("TestTask started.", task.awaitStart(false));  //NOI18N
    }

    public void testCancel_cancel_after_should_run() throws Exception {
        final AtomicBoolean added = new AtomicBoolean();
        tpHandler.on(
            "Running Task: {0}",  //NOI18N
            new Action() {
                @Override
                public void run(final Object[] params) throws Exception {
                    if (added.compareAndSet(false, true)) {
                        RP.submit(new Runnable() {
                            @Override
                            public void run() {
                                TaskProcessor.addPhaseCompletionTasks(
                                    Collections.singleton(Pair.<SchedulerTask,Class<? extends Scheduler>>of(
                                        cancelTask,
                                        cancelTask.getSchedulerClass())),
                                    cache,
                                    false);
                            }
                        }).get();                        
                    }
                }
            });
        TaskProcessor.addPhaseCompletionTasks(
            Collections.singleton(Pair.<SchedulerTask,Class<? extends Scheduler>>of(
                task,
                task.getSchedulerClass())),
            cache,
            false);
        assertTrue("TestTask started.", task.awaitStart(true));  //NOI18N
        assertFalse("Task cancelled", task.isABCancelled()); //NOI18N
        assertTrue("Task cancelled", task.isCSCancelled()); //NOI18N
        task.signalProgress();
        assertTrue("Test task finished", task.awaitDone(true)); //NOI18N
    }


    public static final class TestParser extends Parser {

        private volatile Result r;

        private TestParser() {}

        @Override
        public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
            r = new Result(snapshot) {
                @Override
                protected void invalidate() {
                }
            };
        }

        @Override
        public Result getResult(Task task) throws ParseException {
            return r;
        }

        @Override
        public void addChangeListener(ChangeListener changeListener) {
        }

        @Override
        public void removeChangeListener(ChangeListener changeListener) {
        }

        public static final class Factory extends ParserFactory {
            @Override
            public Parser createParser(Collection<Snapshot> snapshots) {
                return new TestParser();
            }
        }
    }

    private static final class TestTask extends ParserResultTask<Result> {

        private final AtomicBoolean rerun = new AtomicBoolean();
        private final AtomicBoolean abCancel = new AtomicBoolean();
        private final CancelSupport csCancel = CancelSupport.create(this);
        private final CountDownLatch start = new CountDownLatch(1);
        private final CountDownLatch progress = new CountDownLatch(1);
        private final CountDownLatch done = new CountDownLatch(1);        

        private TestTask() {}

        @Override
        public void run(Result result, SchedulerEvent event) {
            if (rerun.get()) {
                return;
            }
            abCancel.set(false);
            start.countDown();
            try {
                progress.await();
            } catch (InterruptedException ie) {
                throw new RuntimeException(ie);
            } finally {
                done.countDown();
            }
        }

        @Override
        public int getPriority() {
            return 2;
        }

        @Override
        public Class<? extends Scheduler> getSchedulerClass() {
            return Scheduler.SELECTED_NODES_SENSITIVE_TASK_SCHEDULER;
        }

        @Override
        public void cancel() {
            abCancel.set(true);
        }

        boolean awaitStart(boolean positive) throws InterruptedException {
            return start.await(
                positive ? TIME : NEGATIVE_TIME,
                TimeUnit.MILLISECONDS);
        }

        boolean awaitDone(boolean positive) throws InterruptedException {
            return done.await(TIME, TimeUnit.MILLISECONDS);
        }

        void signalProgress() {
            progress.countDown();
        }

        boolean isABCancelled() {
            return abCancel.get();
        }

        boolean isCSCancelled() {
            return csCancel.isCancelled();
        }

        void setRerun(boolean b) {
            rerun.set(b);
        }
    }

    private static final class CancelTask extends ParserResultTask<Result> {
        @Override
        public void run(Result result, SchedulerEvent event) {
        }

        @Override
        public int getPriority() {
            return 1;
        }

        @Override
        public Class<? extends Scheduler> getSchedulerClass() {
            return Scheduler.SELECTED_NODES_SENSITIVE_TASK_SCHEDULER;
        }

        @Override
        public void cancel() {
        }
    }

    /*
    private static final class IS implements Utilities.IndexingStatus {
        @Override
        public Set<? extends RepositoryUpdater.IndexingState> getIndexingState() {
            return Collections.<RepositoryUpdater.IndexingState>emptySet();
        }
    }
    */

    private static interface Action {
        void run(Object[] params) throws Exception;
    }

    private static final class TestHandler extends Handler {

        private final Map<String,Action> actions;

        TestHandler() {
            actions = new ConcurrentHashMap<String, Action>();
        }

        void on(String pattern, Action action) {
            actions.put(pattern,action);
        }

        @Override
        public void publish(LogRecord record) {
            final String msg = record.getMessage();
            if (msg != null) {
                final Action action = actions.get(msg);
                if (action != null) {
                    try {
                        action.run(record.getParameters());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

    }

    private static SchedulerTask getTaskFromRequest(TaskProcessor.Request r) {
        try {
            final Field f = r.getClass().getDeclaredField("task");  //NOI18N
            f.setAccessible(true);
            return (SchedulerTask) f.get(r);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }
}
