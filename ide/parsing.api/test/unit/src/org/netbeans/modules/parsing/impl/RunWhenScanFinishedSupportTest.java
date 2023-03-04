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


import java.util.Collections;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.TestEnvironmentFactory;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Pair;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tomas Zezula
 */
public class RunWhenScanFinishedSupportTest extends NbTestCase {
    
    private static final int NEGATIVE_TIME = Integer.getInteger("RunWhenScanFinishedSupportTest.negative.timeout",5000);
    
    private static final String MIME_FOO = "text/x-foo";    //NOI18N
    private static final String FOO_EXT = "foo";    //NOI18N
    
    private Source src;
    private TestHandler handler;
    private Logger log;
    
    public RunWhenScanFinishedSupportTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        FileUtil.setMIMEType(FOO_EXT, MIME_FOO);
        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        final FileObject file = FileUtil.createData(wd, "test.foo");    //NOI18N
        assertNotNull(file);
        MockLookup.setInstances(new MockMimeLookup(), new TestEnvironmentFactory(), new IndexerEmulator());
        src = Source.create(file);
        assertNotNull(src);
        handler = new TestHandler();
        log = Logger.getLogger(RunWhenScanFinishedSupport.class.getName());
        log.setLevel(Level.FINE);
        log.addHandler(handler);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        log.removeHandler(handler);
    }
    
    public void testPerformScanExclusiveToRunWhenScanFinished1() throws Exception {
        final TestTask task = new TestTask("task",null,-1,null);    //NOI18N
        final CountDownLatch taskRunning = handler.condition("runWhenScanFinished:entry", task);    //NOI18N
        final AtomicBoolean notCalledInConcurrent = new AtomicBoolean();
        final Runnable scan = new ScanTask(taskRunning, NEGATIVE_TIME, notCalledInConcurrent);
        final CountDownLatch scannerRunning = handler.condition("performScan:entry", scan); //NOI18N
        
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                RunWhenScanFinishedSupport.performScan(scan, Lookup.getDefault());
            }
        });
        t.start();
        scannerRunning.await();
        RunWhenScanFinishedSupport.runWhenScanFinished(task, Collections.singleton(src));
        t.join();
        assertTrue(notCalledInConcurrent.get());
    }
    
    private static final class TestTask implements Mutex.ExceptionAction<Void> {
        
        private final String name;
        private final CountDownLatch latch;
        private final int timeOut;
        private final AtomicBoolean resHolder;
        
        private TestTask(
                final String name,
                final CountDownLatch latch,
                final int timeOut,
                final AtomicBoolean res) {
            this.name = name;
            this.latch = latch;
            this.timeOut = timeOut;
            this.resHolder = res;
        }
        
        @Override
        public Void run() throws Exception {
            if (latch != null) {
                if (timeOut == -1) {
                    latch.await();
                } else {
                    final boolean res = !latch.await(timeOut, TimeUnit.MILLISECONDS);
                    if (resHolder != null) {
                        resHolder.set(res);
                    }
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return name;
        }

    }
    
    private static class ScanTask implements Runnable {
        
        private final CountDownLatch latch;
        private final int timeOut;
        private final AtomicBoolean resHolder;
        
        private ScanTask(
            final CountDownLatch latch,
            final int timeOut,
            final AtomicBoolean resHolder) {
            this.latch = latch;
            this.timeOut = timeOut;
            this.resHolder = resHolder;
        }
        
        @Override
        public void run() {
            if (latch != null) {
                try {
                    final boolean res = !latch.await(timeOut, TimeUnit.MILLISECONDS);
                    if (resHolder != null) {
                        resHolder.set(res);
                    }
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        @Override
        public String toString() {
            return "scan";  //NOI18N
        }
    }
    
    private static class TestHandler extends Handler {
        
        private final Queue<Pair<Pair<String,Object>,CountDownLatch>> condition =
                new ConcurrentLinkedQueue<Pair<Pair<String, Object>, CountDownLatch>>();
        
        
        public CountDownLatch condition(@NonNull final String message, @NonNull final Object param) throws InterruptedException {
            final CountDownLatch latch = new CountDownLatch(1);
            condition.offer(Pair.<Pair<String,Object>,CountDownLatch>of(Pair.<String,Object>of(message,param),latch));
            return latch;
        }

        @Override
        public void publish(LogRecord record) {            
            final String message = record.getMessage();
            final Object param = record.getParameters()[0];
            for (Iterator<Pair<Pair<String, Object>, CountDownLatch>> it = condition.iterator(); it.hasNext();) {
                final Pair<Pair<String,Object>,CountDownLatch> cnd = it.next();
                if (cnd != null && cnd.first().first().equals(message) && cnd.first().second().equals(param)) {
                    //System.out.println("GOT: " + cnd.first.first + " " + cnd.first.second);
                    it.remove();
                    cnd.second().countDown();
                    break;
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
    
}
