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

package org.netbeans.modules.parsing.impl.indexing;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import javax.swing.text.StyledDocument;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.editor.plain.PlainKit;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.RunWhenScanFinishedSupport;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater.IndexingState;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.CancelReason;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Pair;
import org.openide.util.Parameters;

/**
 *
 * @author sdedic
 */
public class ParsinApiInteractionTest extends IndexingTestBase {

    private static final int NEGATIVE_TIME = Integer.getInteger("RunWhenScanFinishedSupportTest.negative.timeout",5000);
    
    private static final String MIME_FOO = "text/x-foo";    //NOI18N
    private static final String FOO_EXT = "foo";    //NOI18N
    
    private Source src;
    private TestHandler handler;
    private Logger log;

    public ParsinApiInteractionTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        Field f = IndexingUtils.class.getDeclaredField("status");
        f.setAccessible(true);
        f.set(null, null);
        FileUtil.setMIMEType(FOO_EXT, MIME_FOO);
        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        final FileObject file = FileUtil.createData(wd, "test.foo");    //NOI18N
        assertNotNull(file);
        src = Source.create(file);
        assertNotNull(src);
        handler = new TestHandler();
        log = Logger.getLogger(RunWhenScanFinishedSupport.class.getName());
        log.setLevel(Level.FINE);
        log.addHandler(handler);
    }

    @RandomlyFails
    public void testParseWhenScanFinished () throws Exception {
        RUEmulator emulator = new RUEmulator();
        IndexingUtils.setIndexingStatus(emulator);
        emulator.setScanningInProgress(EnumSet.of(IndexingState.STARTING));

        FileUtil.setMIMEType ("foo", "text/foo");
        final FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        final FileObject testFile = FileUtil.createData (workDir, "test.foo");
        final Source source = Source.create (testFile);
        final Collection<Source> sources = Collections.singleton(source);
        final TestTask tt = new TestTask();
        ParserManager.parse(sources, tt);
        assertEquals(1, tt.called);
        final Future<Void> future = ParserManager.parseWhenScanFinished(sources, tt);
        assertEquals(1, tt.called);
        assertFalse (future.isDone());
        future.cancel(false);
        assertFalse (future.isDone());
        assertTrue(future.isCancelled());

        final TestTask tt2 = new TestTask();
        final Future<Void> future2 = ParserManager.parseWhenScanFinished(sources, tt2);
        assertEquals(0, tt2.called);
        assertFalse (future2.isDone());

        final CountDownLatch countDown = new CountDownLatch(1);
        final TestTask tt3 = new TestTask(countDown);
        final Future<Void> future3 = ParserManager.parseWhenScanFinished(sources, tt3);
        assertEquals(0, tt3.called);
        assertFalse (future3.isDone());
        emulator.scan();
        assertTrue(countDown.await(10, TimeUnit.SECONDS));
        assertFalse (future.isDone());
        assertTrue (future2.isDone());
        assertTrue (future3.isDone());

        final TestTask tt4 = new TestTask();
        final Future<Void> future4 = ParserManager.parseWhenScanFinished(sources, tt4);
        assertEquals(1, tt4.called);
        assertTrue(future4.isDone());
    }

    public void testPerformScanExclusiveToRunWhenScanFinished2() throws Exception {
        setupIndexerState();
        
        final AtomicBoolean notCalledInConcurrent = new AtomicBoolean();
        final Runnable scan = new ScanTask(null, -1, null);
        final CountDownLatch scannerRunning = handler.condition("performScan:entry", scan); //NOI18N        
        final TestTask2 task = new TestTask2("task",scannerRunning,NEGATIVE_TIME,notCalledInConcurrent);    //NOI18N        
        final CountDownLatch taskRunning = handler.condition("runWhenScanFinished:entry", task);    //NOI18N
        
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    RunWhenScanFinishedSupport.runWhenScanFinished(task, Collections.singleton(src));
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        t.start();
        taskRunning.await();
        RunWhenScanFinishedSupport.performScan(scan, Lookup.getDefault());
        t.join();
        assertTrue(notCalledInConcurrent.get());
    }
    
    private void setupIndexerState() {
        if (!IndexingUtils.getIndexingState().isEmpty()) {
            IndexingUtils.setIndexingStatus(new IndexingUtils.IndexingStatus() {
                @Override
                public Set<? extends IndexingState> getIndexingState() {
                    return Collections.<IndexingState>emptySet();
                }
            });
        }
        
    }
    
    public void testMultipleConcurrentRunWhenScanFinished() throws Exception {
        setupIndexerState();
        
        final TestTask2 task2 = new TestTask2("task2",null,-1,null);  //NOI18N
        final CountDownLatch task2ExitingRWSF = handler.condition("runWhenScanFinished:entry", task2);  //NOI18N
        final TestTask2 task1 = new TestTask2("task1",task2ExitingRWSF,-1,null);  //NOI18N
        final CountDownLatch task1EnteredRWSF = handler.condition("runWhenScanFinished:entry", task1);  //NOI18N
        
        
        
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {                    
                    RunWhenScanFinishedSupport.runWhenScanFinished(task1, Collections.singleton(src));
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }
        });
        t.start();
        task1EnteredRWSF.await();
        RunWhenScanFinishedSupport.runWhenScanFinished(task2, Collections.singleton(src));
        t.join();
    }
    
    private static class RUEmulator implements Runnable, IndexingUtils.IndexingStatus {

        private final Set<IndexingState> scanning = EnumSet.noneOf(IndexingState.class);

        public void setScanningInProgress(final Set<? extends IndexingState> state) {
            this.scanning.addAll(state);
        }
        
        public void scan () {
            scanning.add(IndexingState.WORKING);
            RepositoryUpdater.getDefault().runAsWork(this);
        }

        @Override
        public Set<? extends IndexingState> getIndexingState() {
            return scanning;
        }

        @Override
        public void run() {
            try {
                // just to simulate that indexing takes some time
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                // ignore
            }
            scanning.clear();
        }

    }

    private static class TestTask extends UserTask {

        long called = 0;
        final CountDownLatch latch;

        public TestTask () {
            latch = null;
        }

        public TestTask (final CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            called++;
            if (latch != null) {
                latch.countDown();
            }
        }

    }
    
    public void testRunWhenScanFinishGetCalledUnderCCLock() throws Exception {
        final File wd = getWorkDir();
        final File srcDir = new File (wd,"src");
        srcDir.mkdirs();
        final File file = new File (srcDir,"test.foo");
        file.createNewFile();
        FileUtil.setMIMEType("foo", "text/foo");
        MockMimeLookup.setInstances(MimePath.parse("text/foo"), new FooParserFactory(), new PlainKit());
        final FileObject fo = FileUtil.toFileObject(file);
        final DataObject dobj = DataObject.find(fo);
        final EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
        final StyledDocument doc = ec.openDocument();
        final Source src = Source.create(doc);
        final CountDownLatch ruRunning = new CountDownLatch(1);
        final CountDownLatch rwsfCalled = new CountDownLatch(1);
        final AtomicReference<Set<RepositoryUpdater.IndexingState>> indexing = new AtomicReference<Set<RepositoryUpdater.IndexingState>>();
        final IndexingUtils.IndexingStatus is = new IndexingUtils.IndexingStatus() {
            @Override
            public Set<? extends RepositoryUpdater.IndexingState> getIndexingState() {
                return indexing.get();
            }
        };
        IndexingUtils.setIndexingStatus(is);
        RepositoryUpdaterTestSupport.runAsWork(
                new Runnable(){
                    @Override
                    public void run() {
                        indexing.set(EnumSet.of(RepositoryUpdater.IndexingState.WORKING));
                        try {
                            ruRunning.countDown();
                            rwsfCalled.await();
                        } catch (InterruptedException ie) {
                        } finally {
                            indexing.set(EnumSet.noneOf(RepositoryUpdater.IndexingState.class));
                        }
                    }
                });
        ruRunning.await();
        doc.putProperty("completion-active", Boolean.TRUE);
        try {
            final Future<Void> done = ParserManager.parseWhenScanFinished(Collections.<Source>singleton(src),new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {                    
                }
        });
        assertFalse(done.isDone());
        assertFalse(done.isCancelled());
        rwsfCalled.countDown();
        try {
            done.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            assertTrue("Deadlock",false);
        }
        } finally {
            doc.putProperty("completion-active", null);
        }
    }

    private static final class FooParserFactory extends ParserFactory {

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

    private static final class FooParser extends Parser {
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
    
    private static final class TestTask2 implements Mutex.ExceptionAction<Void> {
        
        private final String name;
        private final CountDownLatch latch;
        private final int timeOut;
        private final AtomicBoolean resHolder;
        
        private TestTask2(
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
    
}
