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

package org.netbeans.modules.parsing.impl.indexing;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Tomas Zezula
 */
public class IndexerVersionsTest extends IndexingTestBase {

    private static final String MIME = "text/test";
    private static final String INDEXER_NAME = "Test";
    private static final String SOURCES = "src";
    private static final long TIMEOUT = 5000L;

    private MockIndexerFactory indexerFactory;
    private FileObject srcRoot1;
    private FileObject srcRoot2;
    private FileObject f1;
    private FileObject f2;
    private final Map<String, Set<ClassPath>> registeredClasspaths = new HashMap<String, Set<ClassPath>>();

    public IndexerVersionsTest(final String name) {
        super (name);
    }

    @Override
    protected void getAdditionalServices(List<Class> clazz) {
        clazz.add(MockPathRecognizer.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.clearWorkDir();
        final File _wd = this.getWorkDir();
        final FileObject wd = FileUtil.toFileObject(_wd);
        final FileObject cache = wd.createFolder("cache");
        CacheFolder.setCacheFolder(cache);

        indexerFactory = new MockIndexerFactory(1);
        MockMimeLookup.setInstances(MimePath.get(MIME), indexerFactory);
        Set<String> mt = new HashSet<String>();
        mt.add(MIME);
        Util.allMimeTypes = mt;
        assertNotNull("No masterfs",wd);
        srcRoot1 = wd.createFolder("src1");
        assertNotNull(srcRoot1);
        srcRoot2 = wd.createFolder("src2");
        assertNotNull(srcRoot2);
        
        FileUtil.setMIMEType("tst", MIME);
        f1 = FileUtil.createData(srcRoot1,"a.tst");
        assertNotNull(f1);
        assertEquals(MIME, f1.getMIMEType());
        f2 = FileUtil.createData(srcRoot2,"b.tst");
        assertNotNull(f2);
        assertEquals(MIME, f2.getMIMEType());
        RepositoryUpdaterTest.waitForRepositoryUpdaterInit();
    }

    @Override
    protected void tearDown() throws Exception {
        for(Map.Entry<String, Set<ClassPath>> entry : registeredClasspaths.entrySet()) {
            String id = entry.getKey();
            Set<ClassPath> classpaths = entry.getValue();
            GlobalPathRegistry.getDefault().unregister(id, classpaths.toArray(new ClassPath[0]));
        }

        super.tearDown();
    }


    public void testIndexerVersioning () throws Exception {
        RepositoryUpdater ru = RepositoryUpdater.getDefault();
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedUnknowns().size());
        assertNull(CacheFolder.getDataFolder(srcRoot1.getURL()).getFileObject(indexerFactory.getIndexerName()));
        assertNull(CacheFolder.getDataFolder(srcRoot2.getURL()).getFileObject(indexerFactory.getIndexerName()));

        int firstVersion = indexerFactory.getIndexVersion();

        final MockHandler handler = new MockHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);

        ClassPath cp1 = ClassPathSupport.createClassPath(srcRoot1);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await(TIMEOUT));
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRoot1.getURL(), handler.getSources().get(0));
        assertNotNull(CacheFolder.getDataFolder(srcRoot1.getURL()).getFileObject(String.format("%s/%d", indexerFactory.getIndexerName(), firstVersion)));

        handler.reset();
        ClassPath cp2 = ClassPathSupport.createClassPath(srcRoot2);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp2});
        assertTrue (handler.await(TIMEOUT));
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRoot2.getURL(), handler.getSources().get(0));
        assertNotNull(CacheFolder.getDataFolder(srcRoot2.getURL()).getFileObject(String.format("%s/%d", indexerFactory.getIndexerName(), firstVersion)));

        handler.reset();
        globalPathRegistry_unregister(SOURCES, new ClassPath[]{cp1,cp2});
        assertTrue (handler.await(TIMEOUT));
        assertTrue(handler.getSources().isEmpty());


        RepositoryUpdater.getDefault().ignoreIndexerCacheEvents(false);     //Ugly but needed by NB pre 7.1 IndexerCache
        indexerFactory = new MockIndexerFactory(2);
        int secondVersion = indexerFactory.getIndexVersion();
        assertNull(CacheFolder.getDataFolder(srcRoot1.getURL()).getFileObject(String.format("%s/%d", indexerFactory.getIndexerName(), secondVersion)));
        assertNull(CacheFolder.getDataFolder(srcRoot2.getURL()).getFileObject(String.format("%s/%d", indexerFactory.getIndexerName(), secondVersion)));        
        MockMimeLookup.setInstances(MimePath.get(MIME), indexerFactory);
        Thread.sleep(2000);

        handler.reset();
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await(TIMEOUT));
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRoot1.getURL(), handler.getSources().get(0));
        assertNotNull(CacheFolder.getDataFolder(srcRoot1.getURL()).getFileObject(String.format("%s/%d", indexerFactory.getIndexerName(), firstVersion)));
        assertNotNull(CacheFolder.getDataFolder(srcRoot1.getURL()).getFileObject(String.format("%s/%d", indexerFactory.getIndexerName(), secondVersion)));
        awaitRepositoryUpdaterSilence(TIMEOUT);
        assertEquals(Collections.singleton(f1),indexerFactory.reset());

        handler.reset();
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp2});
        assertTrue (handler.await(TIMEOUT));
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRoot2.getURL(), handler.getSources().get(0));
        assertNotNull(CacheFolder.getDataFolder(srcRoot2.getURL()).getFileObject(String.format("%s/%d", indexerFactory.getIndexerName(), firstVersion)));
        assertNotNull(CacheFolder.getDataFolder(srcRoot2.getURL()).getFileObject(String.format("%s/%d", indexerFactory.getIndexerName(), secondVersion)));
        awaitRepositoryUpdaterSilence(TIMEOUT);
//        assertEquals(Collections.singleton(f2),indexerFactory.reset());  - Fixme: BUG in pre 7.1 IndexerCache
    }



    // <editor-fold defaultstate="collapsed" desc="Helper methods">
    protected final void globalPathRegistry_register(String id, ClassPath [] classpaths) {
        Set<ClassPath> set = registeredClasspaths.get(id);
        if (set == null) {
            set = new HashSet<ClassPath>();
            registeredClasspaths.put(id, set);
        }
        set.addAll(Arrays.asList(classpaths));
        GlobalPathRegistry.getDefault().register(id, classpaths);
    }

    protected final void globalPathRegistry_unregister(String id, ClassPath [] classpaths) {
        GlobalPathRegistry.getDefault().unregister(id, classpaths);
        Set<ClassPath> set = registeredClasspaths.get(id);
        if (set != null) {
            set.removeAll(Arrays.asList(classpaths));
        }
    }

    private void assertEquals(final Collection<? extends FileObject> expected, final Iterable<? extends Indexable> result) {
        final Set<FileObject> expectedCopy = new HashSet<FileObject>(expected);
        for (Indexable i : result) {
            final FileObject fo = URLMapper.findFileObject(i.getURL());
            if (fo != null) {
                assertTrue("Expected: " + expected +" Result: " + result,expectedCopy.remove(fo));
            }
        }
        assertTrue("Expected: " + expected +" Result: " + result, expectedCopy.isEmpty());
    }

    private boolean awaitRepositoryUpdaterSilence(final long timeout) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AwaitWork awaitWork = new AwaitWork(latch);
        RepositoryUpdater.getDefault().scheduleWork(awaitWork, false);
        return latch.await(timeout, TimeUnit.MILLISECONDS);
    }
    // </editor-fold>

    public static class MockPathRecognizer extends PathRecognizer {

        @Override
        public Set<String> getSourcePathIds() {
            return Collections.<String>singleton(SOURCES);    //NOI18N
        }

        @Override
        public Set<String> getLibraryPathIds() {
            return Collections.<String>emptySet();
        }

        @Override
        public Set<String> getBinaryLibraryPathIds() {
            return Collections.<String>emptySet();
        }

        @Override
        public Set<String> getMimeTypes() {
            return Collections.<String>singleton(MIME);
        }
    }

    public static class MockIndexerFactory extends CustomIndexerFactory {

        private int version;
        private final List<Indexable> indexables = Collections.synchronizedList(new LinkedList<Indexable>());

        public MockIndexerFactory(int version) {
            this.version = version;
        }

        @Override
        public CustomIndexer createIndexer() {
            return new CustomIndexer() {
                @Override
                protected void index(Iterable<? extends Indexable> files, Context context) {
                    synchronized (indexables) {
                        for (Indexable i : files) {
                            indexables.add(i);
                        }
                    }
                }
            };
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return false;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
        }

        @Override
        public String getIndexerName() {
            return INDEXER_NAME;
        }

        @Override
        public int getIndexVersion() {
            return version;
        }

        public List<? extends Indexable> reset() {
            synchronized (indexables) {
                final List<Indexable> result = new ArrayList<Indexable>(indexables);
                indexables.clear();
                return result;
            }
        }
    }

    private static final class MockHandler extends Handler {

        private volatile CountDownLatch latch;
        private volatile List<URL> sources;

        private MockHandler() {
            reset();
        }

        public void reset() {
            latch = new CountDownLatch(1);
            sources = null;
        }

        public boolean await(long milis) throws InterruptedException {
            return latch.await(milis, TimeUnit.MILLISECONDS);
        }

        public List<? extends URL> getSources() {
            return sources;
        }

        @Override
        public void publish(LogRecord record) {
            String msg = record.getMessage();            
            if ("RootsWork-finished".equals(msg)) {
                latch.countDown();
            } else if ("scanSources".equals(msg)) {
                @SuppressWarnings("unchecked")
                List<URL> s =(List<URL>) record.getParameters()[0];
                sources = s;
            }
        }

        @Override
        public void flush() {            
        }

        @Override
        public void close() throws SecurityException {            
        }
    }

    private static final class AwaitWork extends RepositoryUpdater.Work {

        private final CountDownLatch latch;

        private AwaitWork(final CountDownLatch latch) {
            super(false,false,false,false,SuspendSupport.NOP,null);
            assert latch != null;
            this.latch = latch;
        }

        @Override
        protected boolean getDone() {
            latch.countDown();
            return true;
        }

    }
    //</editor-fold>

}
