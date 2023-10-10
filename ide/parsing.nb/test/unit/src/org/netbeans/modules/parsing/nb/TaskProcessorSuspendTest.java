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

package org.netbeans.modules.parsing.nb;

import org.netbeans.modules.parsing.nb.EventSupport;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.modules.editor.plain.PlainKit;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.impl.SourceAccessor;
import org.netbeans.modules.parsing.impl.SourceCache;
import org.netbeans.modules.parsing.impl.TaskProcessor;
import org.netbeans.modules.parsing.impl.TaskProcessorTest;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Pair;

/**
 *
 * @author sdedic
 */
public class TaskProcessorSuspendTest extends TaskProcessorTest {

    public TaskProcessorSuspendTest(String testName) {
        super(testName);
    }
    
    public void testRunLoopSuspend() throws Exception {
        FileUtil.setMIMEType("foo", "text/foo");    //NOI18N
        MockMimeLookup.setInstances(MimePath.parse("text/foo"), new TaskProcessorTest.FooParserFactory(), new PlainKit());    //NOI18N
        final File wd = getWorkDir();
        final File srcFolder = new File (wd,"src");
        final FileObject srcRoot = FileUtil.createFolder(srcFolder);
        final FileObject srcFile = srcRoot.createData("test.foo");  //NOI18N        
        final Source source = Source.create(srcFile);
        final SourceCache cache = SourceAccessor.getINSTANCE().getCache(source);
        final CountDownLatch taskStarted = new CountDownLatch(1);
        final CountDownLatch cancelCalled = new CountDownLatch(1);
        final CountDownLatch taskDone = new CountDownLatch(1);
        final CountDownLatch secondTaskCalled = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean();        
        final SchedulerTask task1 = new ParserResultTask() {

            @Override
            public void run(Parser.Result pr, SchedulerEvent event) {
                taskStarted.countDown();
                try {
                    result.set(cancelCalled.await(5000, TimeUnit.MILLISECONDS));
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    taskDone.countDown();
                }
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
                cancelCalled.countDown();
            }
        };
        final SchedulerTask task2 = new ParserResultTask() {
            @Override
            public void run(Parser.Result result, SchedulerEvent event) {
                secondTaskCalled.countDown();
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
            }
        };
        TaskProcessor.addPhaseCompletionTasks(
                Arrays.asList(
                    Pair.<SchedulerTask,Class<? extends Scheduler>>of(task1,task1.getSchedulerClass()),
                    Pair.<SchedulerTask,Class<? extends Scheduler>>of(task2,task2.getSchedulerClass())),
                cache,
                false);
        assertTrue(taskStarted.await(5000, TimeUnit.MILLISECONDS));
        runLoop(source, true);
        try {
            assertTrue(taskDone.await(5000, TimeUnit.MILLISECONDS));
            assertTrue(result.get());
            assertFalse(secondTaskCalled.await(2000, TimeUnit.MILLISECONDS));
        } finally {
            runLoop(source, false);
        }
        assertTrue(secondTaskCalled.await(5000, TimeUnit.MILLISECONDS));
    }

    public void testRunLoopSuspend2() throws Exception {
        FileUtil.setMIMEType("foo", "text/foo");    //NOI18N
        MockMimeLookup.setInstances(MimePath.parse("text/foo"), new TaskProcessorTest.FooParserFactory(), new PlainKit());    //NOI18N
        final File wd = getWorkDir();
        final File srcFolder = new File (wd,"src");
        final FileObject srcRoot = FileUtil.createFolder(srcFolder);
        final FileObject srcFile = srcRoot.createData("test.foo");  //NOI18N
        final Source source = Source.create(srcFile);
        final SourceCache cache = SourceAccessor.getINSTANCE().getCache(source);
        final CountDownLatch taskCalled = new CountDownLatch(1);
        final SchedulerTask task = new ParserResultTask() {
            @Override
            public void run(Parser.Result result, SchedulerEvent event) {
                taskCalled.countDown();
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
            }
        };

        runLoop(source, true);
        try {
            TaskProcessor.addPhaseCompletionTasks(
                Arrays.asList(Pair.<SchedulerTask,Class<? extends Scheduler>>of(task,task.getSchedulerClass())),
                cache,
                false);
            assertFalse(taskCalled.await(2000, TimeUnit.MILLISECONDS));
        } finally {
            runLoop(source, false);
        }
        assertTrue(taskCalled.await(5000, TimeUnit.MILLISECONDS));
    }

    private void runLoop(
            final @NonNull Source source,
            final boolean suspend) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final Field editorRegistryListenerField = EventSupport.class.getDeclaredField("editorRegistryListener");    //NOI18N
        assertNotNull(editorRegistryListenerField);
        editorRegistryListenerField.setAccessible(true);
        final Object erl = editorRegistryListenerField.get(null);
        assertNotNull(erl);
        final Method handleCompletionActive = erl.getClass().getDeclaredMethod("handleCompletionActive", Source.class, Object.class); //NOI18N
        assertNotNull(handleCompletionActive);
        handleCompletionActive.setAccessible(true);
        handleCompletionActive.invoke(erl, source, suspend);
    }

}
