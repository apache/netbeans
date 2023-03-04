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

package org.netbeans.modules.parsing.spi.indexing.support;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdaterTest;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Zezula
 */
public class FileQueryTest extends NbTestCase {

    private FileObject wd;
    private FileObject sources;
    private FileObject srcFile;
    private FileObject srcFileInFolder;
    private FileObject nonIndexedFile;

    public FileQueryTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        wd = FileUtil.toFileObject(getWorkDir());
        final FileObject cache = FileUtil.createFolder(wd, "cache"); //NOI18N
        CacheFolder.setCacheFolder(cache);
        MockServices.setServices(TestPathRecognizer.class, ClassPathProviderImpl.class);
        MockMimeLookup.setInstances(MimePath.get(TestPathRecognizer.MIME), new TestIndexer.Factory());
        FileUtil.setMIMEType(TestPathRecognizer.EXT, TestPathRecognizer.MIME);

        sources = FileUtil.createFolder(wd, "src");         //NOI18N
        srcFile = FileUtil.createData(sources, "file1.test"); //NOI18N
        srcFileInFolder = FileUtil.createData(sources, "a/b/file2.test"); //NOI18N
        nonIndexedFile = FileUtil.createData(sources, "file2.nidx"); //NOI18N
        Lookup.getDefault().lookup(ClassPathProviderImpl.class).setSourceRoot(sources);
        RepositoryUpdaterTest.setMimeTypes(TestPathRecognizer.MIME);
        RepositoryUpdaterTest.waitForRepositoryUpdaterInit();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRelativePath() throws IOException {
        IndexingManager.getDefault().refreshIndexAndWait(
             sources.toURL(),
             null,
             true,
             false);
        final QuerySupport querySupport = QuerySupport.forRoots(TestIndexer.NAME, TestIndexer.VERSION, sources);
        assertNotNull(querySupport);
        final QuerySupport.Query.Factory factory = querySupport.getQueryFactory();
        assertNotNull(factory);
        QuerySupport.Query query = factory.file(FileUtil.getRelativePath(sources, srcFile));
        assertNotNull(query);
        Collection<? extends IndexResult> res = query.execute((String[])null);
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(srcFile, res.iterator().next().getFile());
        query =  factory.file(FileUtil.getRelativePath(sources, srcFileInFolder));
        assertNotNull(query);
        res = query.execute((String[])null);
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(srcFileInFolder, res.iterator().next().getFile());
        query =  factory.file(FileUtil.getRelativePath(sources, nonIndexedFile));
        assertNotNull(query);
        res = query.execute((String[])null);
        assertNotNull(res);
        assertEquals(0, res.size());
    }

    public void testFileObject() throws IOException {
        IndexingManager.getDefault().refreshIndexAndWait(
             sources.toURL(),
             null,
             true,
             false);
        final QuerySupport querySupport = QuerySupport.forRoots(TestIndexer.NAME, TestIndexer.VERSION, sources);
        assertNotNull(querySupport);
        final QuerySupport.Query.Factory factory = querySupport.getQueryFactory();
        assertNotNull(factory);
        QuerySupport.Query query = factory.file(srcFile);
        assertNotNull(query);
        Collection<? extends IndexResult> res = query.execute((String[])null);
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(srcFile, res.iterator().next().getFile());
        query =  factory.file(srcFileInFolder);
        assertNotNull(query);
        res = query.execute((String[])null);
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(srcFileInFolder, res.iterator().next().getFile());
        query =  factory.file(nonIndexedFile);
        assertNotNull(query);
        res = query.execute((String[])null);
        assertNotNull(res);
        assertEquals(0, res.size());
        boolean tlm = false;
        try {
            query =  factory.file(wd);
        } catch (IllegalArgumentException iae) {
            tlm = true;
        }
        assertTrue(tlm);
    }

    public static final class ClassPathProviderImpl implements ClassPathProvider {

        //@GuardedBy("this")
        private FileObject srcRoot;
        //@GuardedBy("this")
        private ClassPath srcCp;

        @Override
        public synchronized ClassPath findClassPath(FileObject file, String type) {
            if (srcRoot != null && (srcRoot.equals(file) || FileUtil.isParentOf(srcRoot, file))) {
                return srcCp;
            }
            return null;
        }

        synchronized void setSourceRoot(FileObject sourceRoot) {
            this.srcRoot = sourceRoot;
            this.srcCp = ClassPathSupport.createClassPath(sourceRoot);
        }
    }

    public static final class TestPathRecognizer extends PathRecognizer {

        static final String EXT = "test";           //NOI18N
        static final String MIME = "text/x-test";   //NOI18N
        static final String PATH_SRC = "sources";   //NOI18N

        @Override
        public Set<String> getSourcePathIds() {
            return Collections.singleton(PATH_SRC);
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
            return Collections.singleton(MIME);
        }
    }

    private static final class TestIndexer extends CustomIndexer {
        static final String NAME = "TestIndexer";   //NOI18N
        static final int VERSION = 1;

        @Override
        protected void index(Iterable<? extends Indexable> files, Context context) {
            try {
                final IndexingSupport is = IndexingSupport.getInstance(context);
                for (Indexable indexable : files) {
                    final IndexDocument doc = is.createDocument(indexable);
                    is.addDocument(doc);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        private static final class Factory extends CustomIndexerFactory {

            @Override
            public CustomIndexer createIndexer() {
                return new TestIndexer();
            }

            @Override
            public boolean supportsEmbeddedIndexers() {
                return false;
            }

            @Override
            public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
                try {
                    final IndexingSupport is = IndexingSupport.getInstance(context);
                    for (Indexable indexable : deleted) {
                        is.removeDocuments(indexable);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            @Override
            public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
                try {
                    final IndexingSupport is = IndexingSupport.getInstance(context);
                    for (Indexable indexable : dirty) {
                        is.markDirtyDocuments(indexable);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
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
    }
}
