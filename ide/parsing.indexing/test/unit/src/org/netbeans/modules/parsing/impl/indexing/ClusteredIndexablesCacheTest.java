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
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import static org.netbeans.modules.parsing.impl.indexing.FooPathRecognizer.FOO_EXT;
import static org.netbeans.modules.parsing.impl.indexing.FooPathRecognizer.FOO_MIME;
import static org.netbeans.modules.parsing.impl.indexing.FooPathRecognizer.FOO_SOURCES;
import org.netbeans.modules.parsing.impl.indexing.lucene.DocumentBasedIndexManager;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.DocumentIndexCache;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public class ClusteredIndexablesCacheTest extends IndexingTestBase {

    private static final String EMB_EXT = "emb";            //NOI18N
    private static final String EMB_MIME = "text/x-emb";    //NOI18N
    private static final String EMB_SOURCES = "emb-src";    //NOI18N

    private final Map<String, Map<ClassPath,Void>> registeredClasspaths = new HashMap<String, Map<ClassPath,Void>>();

    private FileObject src1;
    private FileObject src2;
    private FileObject file1;
    private FileObject file2;
    private FileObject file3;
    private FileObject file4;
    private FileObject file5;
    private FileObject file6;
    private FileObject file7;
    private FileObject file8;
    private ClassPath cp1;
    private ClassPath cp2;

    public ClusteredIndexablesCacheTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final File wd = getWorkDir();
        final FileObject wdo = FileUtil.toFileObject(wd);
        assertNotNull("No masterfs",wdo);   //NOI18N
        final FileObject cache = wdo.createFolder("cache"); //NOI18N
        CacheFolder.setCacheFolder(cache);
        src1 = wdo.createFolder("src1");        //NOI18N
        assertNotNull(src1);        
        file1 = src1.createData("test", FOO_EXT);   //NOI18N
        assertNotNull(file1);
        file2 = src1.createData("test2", FOO_EXT);  //NOI18N
        assertNotNull(file2);
        file3 = src1.createData("test3", FOO_EXT);  //NOI18N
        assertNotNull(file3);
        file4 = src1.createData("test4", FOO_EXT);  //NOI18N
        assertNotNull(file4);
        FileUtil.setMIMEType(FOO_EXT, FOO_MIME);
        src2 = wdo.createFolder("src2");    //NOI18N
        assertNotNull(src2);
        file5 = src2.createData("test5", EMB_EXT);   //NOI18N
        assertNotNull(file5);
        file6 = src2.createData("test6", EMB_EXT);   //NOI18N
        assertNotNull(file6);
        file7 = src2.createData("test7", EMB_EXT);   //NOI18N
        assertNotNull(file7);
        file8 = src2.createData("test8", EMB_EXT);   //NOI18N
        assertNotNull(file8);
        FileUtil.setMIMEType(EMB_EXT, EMB_MIME);
        cp1 = ClassPathSupport.createClassPath(src1);
        cp2 = ClassPathSupport.createClassPath(src2);
        MockMimeLookup.setInstances(MimePath.get(FOO_MIME), new FooIndexerFactory());
        MockMimeLookup.setInstances(MimePath.get(EMB_MIME), new EmbIndexerFactory(), new EmbParserFactory());
        RepositoryUpdaterTest.setMimeTypes(FOO_MIME, EMB_MIME);
        RepositoryUpdaterTest.waitForRepositoryUpdaterInit();
    }

    @Override
    protected void tearDown() throws Exception {
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        try {
            logger.setLevel (Level.FINEST);
            logger.addHandler(handler);
            for(String id : registeredClasspaths.keySet()) {
                final Map<ClassPath,Void> classpaths = registeredClasspaths.get(id);
                GlobalPathRegistry.getDefault().unregister(id, classpaths.keySet().toArray(new ClassPath[classpaths.size()]));
            }
            handler.await();
        } finally {
            logger.removeHandler(handler);
        }
        super.tearDown();
    }


    public void testNoOutOfOrderFilesCustomIndexer() throws InterruptedException, IOException {

        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCES).isEmpty());
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        try {
            handler.beforeScanFinishedAction = new NoOutOfOrderPredicate(FooIndexerFactory.NAME);
            globalPathRegistry_register(FOO_SOURCES,new ClassPath[]{cp1});
            assertTrue (handler.await());
            assertEquals(0, handler.getBinaries().size());
            assertEquals(1, handler.getSources().size());
            assertEquals(this.src1.toURL(), handler.getSources().get(0));
            assertEquals(Boolean.TRUE, handler.res);
            QuerySupport qs = QuerySupport.forRoots(FooIndexerFactory.NAME, FooIndexerFactory.VERSION, src1);
            Collection<? extends IndexResult> res = qs.query("_sn", "", QuerySupport.Kind.PREFIX, (String[]) null);   //NOI18N
            assertEquals(4, res.size());

            handler.reset();
            globalPathRegistry_unregister(FOO_SOURCES,new ClassPath[]{cp1});
            assertTrue (handler.await());

            file3.delete();
            file4.delete();
            handler.reset();
            handler.beforeScanFinishedAction = new NoOutOfOrderPredicate(FooIndexerFactory.NAME);
            globalPathRegistry_register(FOO_SOURCES,new ClassPath[]{cp1});
            assertTrue (handler.await());
            assertEquals(0, handler.getBinaries().size());
            assertEquals(1, handler.getSources().size());
            assertEquals(this.src1.toURL(), handler.getSources().get(0));
            assertEquals(Boolean.TRUE, handler.res);
            qs = QuerySupport.forRoots(FooIndexerFactory.NAME, FooIndexerFactory.VERSION, src1);
            res = qs.query("_sn", "", QuerySupport.Kind.PREFIX, (String[]) null);   //NOI18N
            assertEquals(2, res.size());
        } finally {
            logger.removeHandler(handler);
        }

    }

    public void testNoOutOfOrderFilesEmbeddingIndexer() throws InterruptedException, IOException {

        assertTrue(GlobalPathRegistry.getDefault().getPaths(EMB_SOURCES).isEmpty());
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        try {
            handler.beforeScanFinishedAction = new NoOutOfOrderPredicate(EmbIndexerFactory.NAME);
            globalPathRegistry_register(EMB_SOURCES,new ClassPath[]{cp2});
            assertTrue (handler.await());
            assertEquals(0, handler.getBinaries().size());
            assertEquals(1, handler.getSources().size());
            assertEquals(this.src2.toURL(), handler.getSources().get(0));
            assertEquals(Boolean.TRUE, handler.res);
            QuerySupport qs = QuerySupport.forRoots(EmbIndexerFactory.NAME, FooIndexerFactory.VERSION, src2);
            Collection<? extends IndexResult> res = qs.query("_sn", "", QuerySupport.Kind.PREFIX, (String[]) null);   //NOI18N
            assertEquals(4, res.size());

            handler.reset();
            globalPathRegistry_unregister(EMB_SOURCES,new ClassPath[]{cp2});
            assertTrue (handler.await());

            file5.delete();
            file6.delete();
            handler.reset();
            handler.beforeScanFinishedAction = new NoOutOfOrderPredicate(EmbIndexerFactory.NAME);
            globalPathRegistry_register(EMB_SOURCES,new ClassPath[]{cp2});
            assertTrue (handler.await());
            assertEquals(0, handler.getBinaries().size());
            assertEquals(1, handler.getSources().size());
            assertEquals(this.src2.toURL(), handler.getSources().get(0));
            assertEquals(Boolean.TRUE, handler.res);
            qs = QuerySupport.forRoots(EmbIndexerFactory.NAME, FooIndexerFactory.VERSION, src2);
            res = qs.query("_sn", "", QuerySupport.Kind.PREFIX, (String[]) null);   //NOI18N
            assertEquals(2, res.size());
        } finally {
            logger.removeHandler(handler);
        }
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

    private final void globalPathRegistry_unregister(String id, ClassPath [] classpaths) {
        GlobalPathRegistry.getDefault().unregister(id, classpaths);
        final Map<ClassPath,Void> map = registeredClasspaths.get(id);
        if (map != null) {
            map.keySet().removeAll(Arrays.asList(classpaths));
        }
    }


    private static class FooIndexerFactory extends CustomIndexerFactory {

        private static final String NAME = "FooIndexer";    //NOI18N
        private static final int VERSION = 1;
        

        @Override
        public CustomIndexer createIndexer() {
            return new CustomIndexer() {
                @Override
                protected void index(Iterable<? extends Indexable> files, Context context) {
                    try {
                        final IndexingSupport is = IndexingSupport.getInstance(context);
                        for (Indexable i : files) {
                            final IndexDocument doc = is.createDocument(i);
                            is.addDocument(doc);
                        }
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
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
            try {
                final IndexingSupport is = IndexingSupport.getInstance(context);
                for (Indexable i : deleted) {
                    is.removeDocuments(i);
                }
            } catch (IOException  ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
        }

        @Override
        public String getIndexerName() {
            return NAME;
        }

        @Override
        public int getIndexVersion() {
            return VERSION;
        }        
    }

    private static class EmbIndexerFactory extends EmbeddingIndexerFactory {

        private static final String NAME = "EmbIndexer";    //NOI18N
        private static final int VERSION = 1;

        @Override
        public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
            return new EmbeddingIndexer() {
                @Override
                protected void index(Indexable indexable, Result parserResult, Context context) {
                    try {
                        final IndexingSupport is = IndexingSupport.getInstance(context);
                        final IndexDocument doc = is.createDocument(indexable);
                        is.addDocument(doc);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
            };
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            try {
                final IndexingSupport is = IndexingSupport.getInstance(context);
                for (Indexable i : deleted) {
                    is.removeDocuments(i);
                }
            } catch (IOException  ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
        }

        @Override
        public String getIndexerName() {
            return NAME;
        }

        @Override
        public int getIndexVersion() {
            return VERSION;
        }

    }

    private static class EmbParserFactory extends ParserFactory {

        @Override
        public Parser createParser(Collection<Snapshot> snapshots) {
            return new Parser() {

                private Result res;

                @Override
                public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                    res = new Result(snapshot) {
                        @Override
                        protected void invalidate() {
                        }
                    };
                }

                @Override
                public Result getResult(Task task) throws ParseException {
                    assert res != null;
                    return res;
                }

                @Override
                public void addChangeListener(ChangeListener changeListener) {
                }

                @Override
                public void removeChangeListener(ChangeListener changeListener) {
                }
            };
        }

    }

    private static class NoOutOfOrderPredicate implements Convertor<Pair<URL,String>, Boolean> {

        private final String forIndexer;

        NoOutOfOrderPredicate(@NonNull final String forIndexer) {
            Parameters.notNull("forIndexer", forIndexer);   //NOI18N
            this.forIndexer = forIndexer;
        }

        @Override
        public Boolean convert(@NonNull final Pair<URL,String> p) {
            if (!forIndexer.equals(p.second())) {
                return null;
            }
            try {
                final FileObject cacheFolder = CacheFolder.getDataFolder(p.first());
                final FileObject indexer = cacheFolder.getFileObject(p.second());
                final FileObject indexFolder = indexer.getFileObject("1/1");
                final DocumentIndexCache cache = DocumentBasedIndexManager.getDefault().getCache(indexFolder.toURL());
                final Class<?> c = Class.forName("org.netbeans.modules.parsing.impl.indexing.ClusteredIndexables$DocumentIndexCacheImpl");  //NOI18N
                final Field f = c.getDeclaredField("toDeleteOutOfOrder");   //NOI18N
                f.setAccessible(true);
                final Collection<?> data = (Collection<?>) f.get(cache);
                return data.isEmpty();
            } catch (Exception e) {
                return Boolean.FALSE;
            }
        }

    }

    private class TestHandler extends RepositoryUpdaterTest.TestHandler {

        volatile Convertor<Pair<URL,String>,Boolean> beforeScanFinishedAction;
        volatile Boolean res;

        @Override
        public void reset () {
            beforeScanFinishedAction = null;
            res = null;
            super.reset();
        }

        @Override
        public void reset(final Type t) {
            beforeScanFinishedAction = null;
            res = null;
            super.reset(t);
        }

        @Override
        public void reset(final Type t, int initialCount) {
            beforeScanFinishedAction = null;
            res = null;
            super.reset(t, initialCount);
        }

        @Override
        public void publish(LogRecord record) {
            final Convertor<Pair<URL,String>,Boolean> action = beforeScanFinishedAction;
            final String message = record.getMessage();
            if (action != null && "scanFinishing:{0}:{1}".equals(message)) {    //NOI18N
                try {
                    Boolean tmpRes = action.convert(
                        Pair.<URL,String>of(
                            new URL ((String)record.getParameters()[1]),
                            (String)record.getParameters()[0]));
                    if (tmpRes != null) {
                        res = tmpRes;
                    }
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            super.publish(record);
        }

    }
}
