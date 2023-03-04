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
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import static org.netbeans.modules.parsing.impl.indexing.FooPathRecognizer.FOO_BINARY;
import static org.netbeans.modules.parsing.impl.indexing.FooPathRecognizer.FOO_EXT;
import static org.netbeans.modules.parsing.impl.indexing.FooPathRecognizer.FOO_MIME;
import static org.netbeans.modules.parsing.impl.indexing.FooPathRecognizer.FOO_SOURCES;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexer;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
public class ScanStartedTest extends IndexingTestBase {
    
    private final Map<String, Map<ClassPath,Void>> registeredClasspaths = new HashMap<String, Map<ClassPath,Void>>();
    
    private static final Logger LOG = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
    
    private TestHandler handler;
    
    private IndexerFactory factory1;
    private IndexerFactory factory2;
    private IndexerFactory factory3;

    private BinIndexerFactory binFactory1;
    private BinIndexerFactory binFactory2;
    private BinIndexerFactory binFactory3;

    private FileObject src1;
    private FileObject file1;
    private FileObject bin1;
    private FileObject file2;
    private ClassPath cp1;
    private ClassPath bcp1;

    public ScanStartedTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void getAdditionalServices(List<Class> clazz) {
        super.getAdditionalServices(clazz);
        clazz.add(FooCPP.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        handler = new TestHandler(); 

        this.clearWorkDir();
        final File _wd = this.getWorkDir();
        final FileObject wd = FileUtil.toFileObject(_wd);
        assertNotNull("No masterfs",wd);                    //NOI18N
        final FileObject cache = wd.createFolder("cache");  //NOI18N
        CacheFolder.setCacheFolder(cache);
        src1 = wd.createFolder("src1");     //NOI18N
        assertNotNull(src1);
        file1 = src1.createData("test", FOO_EXT);   //NOI18N
        assertNotNull(file1);
        FileUtil.setMIMEType(FOO_EXT, FOO_MIME);
        bin1 = wd.createFolder("bin1"); //NOI18N
        file2 = bin1.createData("test", "bin"); //NOI18N

        factory1 = new IndexerFactory("factory1", 1);   //NOI18N
        factory2 = new IndexerFactory("factory2", 1);   //NOI18N
        factory3 = new IndexerFactory("factory3", 1);   //NOI18N

        binFactory1 = new BinIndexerFactory("binFactory1", 1);  //NOI18N
        binFactory2 = new BinIndexerFactory("binFactory2", 1);  //NOI18N
        binFactory3 = new BinIndexerFactory("binFactory3", 1);  //NOI18N

        cp1 = ClassPathSupport.createClassPath(src1);
        bcp1 = ClassPathSupport.createClassPath(bin1);
        FooCPP.roots2cps = Collections.unmodifiableMap(
                new HashMap() {
                    {
                        put(src1, Collections.singletonMap(FOO_SOURCES, cp1));
                        put(bin1, Collections.singletonMap(FOO_BINARY, bcp1));
                    }
                });
        MockMimeLookup.setInstances(MimePath.EMPTY, binFactory1, binFactory2, binFactory3);
        MockMimeLookup.setInstances(MimePath.get(FOO_MIME), factory1, factory2, factory3);
        RepositoryUpdaterTest.setMimeTypes(FOO_MIME);
        
        RepositoryUpdaterTest.waitForRepositoryUpdaterInit();  
        
        LOG.setLevel (Level.FINEST);
        LOG.addHandler(handler);

    }

    @Override
    protected void tearDown() throws Exception {
        try {
            for(String id : registeredClasspaths.keySet()) {
                final Map<ClassPath,Void> classpaths = registeredClasspaths.get(id);
                GlobalPathRegistry.getDefault().unregister(id, classpaths.keySet().toArray(new ClassPath[classpaths.size()]));
            }
            handler.await();
        } finally {
            LOG.removeHandler(handler);
        }
        super.tearDown();
    }


    public void testScanFinishedAfterScanStarted() throws Exception {
        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCES).isEmpty());

        //Testing classpath registration
        globalPathRegistry_register(FOO_SOURCES,new ClassPath[]{cp1});

        assertTrue (handler.await());

        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.src1.toURL(), handler.getSources().get(0));
        assertEquals(1, factory1.scanStartedCount.get());
        assertEquals(1, factory2.scanStartedCount.get());
        assertEquals(1, factory3.scanStartedCount.get());
        assertEquals(1, factory1.scanFinishedCount.get());
        assertEquals(1, factory2.scanFinishedCount.get());
        assertEquals(1, factory3.scanFinishedCount.get());
    }


    public void testScanFinishedAfterScanStartedWithException() throws Exception {
        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCES).isEmpty());

        factory2.throwFromScanStarted = new RuntimeException();

        //Testing classpath registration
        globalPathRegistry_register(FOO_SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.src1.toURL(), handler.getSources().get(0));
        assertEquals(1, factory1.scanStartedCount.get());
        assertEquals(1, factory2.scanStartedCount.get());
        assertEquals(1, factory3.scanStartedCount.get());
        assertEquals(1, factory1.scanFinishedCount.get());
        assertEquals(1, factory2.scanFinishedCount.get());
        assertEquals(1, factory3.scanFinishedCount.get());
    }

    public void testScanFinishedWithExceptionAfterScanStarted() throws Exception {
        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCES).isEmpty());

        factory2.throwFromScanFinished = new RuntimeException();

        //Testing classpath registration
        globalPathRegistry_register(FOO_SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.src1.toURL(), handler.getSources().get(0));
        assertEquals(1, factory1.scanStartedCount.get());
        assertEquals(1, factory2.scanStartedCount.get());
        assertEquals(1, factory3.scanStartedCount.get());
        assertEquals(1, factory1.scanFinishedCount.get());
        assertEquals(1, factory2.scanFinishedCount.get());
        assertEquals(1, factory3.scanFinishedCount.get());
    }


    public void testScanFinishedAfterScanStartedWithInternalException() throws Exception {
        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCES).isEmpty());

        handler.internalException = Pair.<String,RuntimeException>of(
                factory2.getIndexerName(),
                new RuntimeException());    //Symulate internal exception

        //Testing classpath registration
        globalPathRegistry_register(FOO_SOURCES,new ClassPath[]{cp1});
        assertFalse(handler.await(RepositoryUpdaterTest.NEGATIVE_TIME));
        assertEquals(0, handler.getBinaries().size());
        assertNull(handler.getSources());
        assertEquals(1, factory1.scanStartedCount.get());
        assertEquals(0, factory2.scanStartedCount.get());
        assertEquals(0, factory3.scanStartedCount.get());
        assertEquals(1, factory1.scanFinishedCount.get());
        assertEquals(0, factory2.scanFinishedCount.get());
        assertEquals(0, factory3.scanFinishedCount.get());
    }

    public void testBinaryScanFinishedAfterScanStarted() throws Exception {
        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_BINARY).isEmpty());

        //Testing classpath registration
        globalPathRegistry_register(FOO_BINARY,new ClassPath[]{bcp1});
        assertTrue (handler.await());
        
        assertEquals(1, handler.getBinaries().size());
        assertEquals(0, handler.getSources().size());
        assertEquals(this.bin1.toURL(), handler.getBinaries().iterator().next());
        assertEquals(1, binFactory1.scanStartedCount.get());
        assertEquals(1, binFactory2.scanStartedCount.get());
        assertEquals(1, binFactory3.scanStartedCount.get());
        assertEquals(1, binFactory1.scanFinishedCount.get());
        assertEquals(1, binFactory2.scanFinishedCount.get());
        assertEquals(1, binFactory3.scanFinishedCount.get());
    }

    public void testBinaryScanFinishedAfterScanStartedWithException() throws Exception {
        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_BINARY).isEmpty());
        binFactory2.throwFromScanStarted = new RuntimeException();

        //Testing classpath registration
        globalPathRegistry_register(FOO_BINARY,new ClassPath[]{bcp1});
        assertTrue (handler.await());
        assertEquals(1, handler.getBinaries().size());
        assertEquals(0, handler.getSources().size());
        assertEquals(this.bin1.toURL(), handler.getBinaries().iterator().next());
        assertEquals(1, binFactory1.scanStartedCount.get());
        assertEquals(1, binFactory2.scanStartedCount.get());
        assertEquals(1, binFactory3.scanStartedCount.get());
        assertEquals(1, binFactory1.scanFinishedCount.get());
        assertEquals(1, binFactory2.scanFinishedCount.get());
        assertEquals(1, binFactory3.scanFinishedCount.get());
    }

    public void testBinaryScanFinishedWithExceptionAfterScanStarted() throws Exception {
        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_BINARY).isEmpty());

        binFactory2.throwFromScanFinished = new RuntimeException();

        //Testing classpath registration
        globalPathRegistry_register(FOO_BINARY,new ClassPath[]{bcp1});
        assertTrue (handler.await());
        assertEquals(1, handler.getBinaries().size());
        assertEquals(0, handler.getSources().size());
        assertEquals(this.bin1.toURL(), handler.getBinaries().iterator().next());
        assertEquals(1, binFactory1.scanStartedCount.get());
        assertEquals(1, binFactory2.scanStartedCount.get());
        assertEquals(1, binFactory3.scanStartedCount.get());
        assertEquals(1, binFactory1.scanFinishedCount.get());
        assertEquals(1, binFactory2.scanFinishedCount.get());
        assertEquals(1, binFactory3.scanFinishedCount.get());
    }


    private void globalPathRegistry_register(String id, ClassPath [] classpaths) {
        Map<ClassPath,Void> map = registeredClasspaths.get(id);
        if (map == null) {
            map = new IdentityHashMap<ClassPath, Void>();
            registeredClasspaths.put(id, map);
        }
        for (ClassPath cp :  classpaths) {
            map.put(cp,null);
        }
        GlobalPathRegistry.getDefault().register(id, classpaths);
    }


    public static class FooCPP implements ClassPathProvider {

        static volatile Map<FileObject,Map<String,ClassPath>> roots2cps;

        @Override
        public ClassPath findClassPath(FileObject file, String type) {            
            final Map<FileObject,Map<String,ClassPath>> m = roots2cps;
            if (m != null) {
                for (Map.Entry<FileObject,Map<String,ClassPath>> p : m.entrySet()) {
                    if (p.getKey().equals(file) || FileUtil.isParentOf(p.getKey(), file)) {
                        return p.getValue().get(type);
                    }
                }
            }            
            return null;
        }

    }


    public static class FooPathRecognizer extends PathRecognizer {

        @Override
        public Set<String> getSourcePathIds() {
            return Collections.<String>singleton(FOO_SOURCES);
        }

        @Override
        public Set<String> getLibraryPathIds() {
            return Collections.<String>emptySet();
        }

        @Override
        public Set<String> getBinaryLibraryPathIds() {
            return Collections.<String>singleton(FOO_BINARY);
        }

        @Override
        public Set<String> getMimeTypes() {
            return Collections.<String>singleton(FOO_MIME);
        }

    }


    private static class Indexer extends CustomIndexer {
        @Override
        protected void index(Iterable<? extends Indexable> files, Context context) {
        }
    }

    private static class IndexerFactory extends CustomIndexerFactory {

        private final String name;
        private final int version;

        private final AtomicInteger scanStartedCount = new AtomicInteger();
        private final AtomicInteger scanFinishedCount = new AtomicInteger();

        volatile RuntimeException throwFromScanStarted;
        volatile RuntimeException throwFromScanFinished;

        IndexerFactory(
            @NonNull final String name,
            final int version) {
            this.name = name;
            this.version = version;
        }

        @Override
        public CustomIndexer createIndexer() {
            return new Indexer();
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
            return name;
        }

        @Override
        public int getIndexVersion() {
            return version;
        }

        @Override
        public boolean scanStarted(Context context) {
            scanStartedCount.incrementAndGet();
            RuntimeException re = throwFromScanStarted;
            if (re != null) {
                throw re;
            }
            return true;
        }

        @Override
        public void scanFinished(Context context) {
            scanFinishedCount.incrementAndGet();
            final RuntimeException re = throwFromScanFinished;
            if (re != null) {
                throw re;
            }
        }

    }

    private static class BinIndexer extends BinaryIndexer {
        @Override
        protected void index(Context context) {
        }
    }

    private static class BinIndexerFactory extends BinaryIndexerFactory {

        private final String name;
        private final int version;

        private final AtomicInteger scanStartedCount = new AtomicInteger();
        private final AtomicInteger scanFinishedCount = new AtomicInteger();

        volatile RuntimeException throwFromScanStarted;
        volatile RuntimeException throwFromScanFinished;

        BinIndexerFactory(
                @NonNull final String name,
                final int version) {
            this.name = name;
            this.version = version;
        }

        @Override
        public BinaryIndexer createIndexer() {
            return new BinIndexer();
        }

        @Override
        public void rootsRemoved(Iterable<? extends URL> removedRoots) {
        }

        @Override
        public String getIndexerName() {
            return name;
        }

        @Override
        public int getIndexVersion() {
            return version;
        }

        @Override
        public boolean scanStarted(Context context) {
            scanStartedCount.incrementAndGet();
            final RuntimeException re = throwFromScanStarted;
            if (re != null) {
                throw re;
            }
            return true;
        }

        @Override
        public void scanFinished(Context context) {
            scanFinishedCount.incrementAndGet();
            final RuntimeException re = throwFromScanFinished;
            if (re != null) {
                throw re;
            }
        }



    }

    private static class TestHandler extends RepositoryUpdaterTest.TestHandler {
        volatile Pair<String,RuntimeException> internalException;

        @Override
        public void publish(LogRecord record) {
            final String msg = record.getMessage();
            final Pair<String,RuntimeException> ie  = internalException;
            if (ie != null &&
                msg != null &&
                msg.startsWith("scanStarting:") &&  //NOI18N
                ie.first().equals(record.getParameters()[0])) {
                throw ie.second();
            } else {
                super.publish(record);
            }
        }


    }

}
