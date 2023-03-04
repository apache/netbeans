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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
public class AttachableDocumentIndexCacheTest extends IndexingTestBase {

    private static final String PATH_SRC = "SRC";               //NOI18N
    private static final String MIME_FOO = "text/x-foo";        //NOI18N

    private FileObject src;
    private Logger log;
    private Level level;
    private Handler handler;

    public AttachableDocumentIndexCacheTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void getAdditionalServices(List<Class> clazz) {
        clazz.add(CPProviderImpl.class);
        clazz.add(FooRecognizer.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockMimeLookup.setInstances(
            MimePath.get(MIME_FOO),
            new Indexer1.Factory(),
            new Indexer2.Factory());
        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        src = FileUtil.createFolder(wd,"src");                          //NOI18N
        FileUtil.createData(src, "file.foo"); //NOI18N
        Lookup.getDefault().lookup(CPProviderImpl.class).setRoot(src);
        FileUtil.setMIMEType("foo", MIME_FOO);  //NOI18N
        RepositoryUpdaterTest.setMimeTypes(MIME_FOO);
        RepositoryUpdaterTest.waitForRepositoryUpdaterInit();

        log = Logger.getLogger(RepositoryUpdater.class.getName() + ".tests"); //NOI18N
        level = log.getLevel();
        log.setLevel(Level.FINEST);
        handler = new LogHandler();
        log.addHandler(handler);
    }

    @Override
    protected void tearDown() throws Exception {
        log.removeHandler(handler);
        log.setLevel(level);
        super.tearDown();
    }

    public void testMissingDetach234616 () {
        //1st run - the logger throws some ugly exception during the save of Indexer1 index
        IndexingManager.getDefault().refreshIndexAndWait(src.toURL(), null, true);
        //2nd run - verify that both Indexer1 and Indexer2 indexes are detached from ClusteredIndexables.
        IndexingManager.getDefault().refreshIndexAndWait(src.toURL(), null, true);
    }

    public static final class Indexer1 extends CustomIndexer {

        private Indexer1(){}

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

        public static class Factory extends CustomIndexerFactory {

            @Override
            public CustomIndexer createIndexer() {
                return new Indexer1();
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
                return Indexer1.class.getSimpleName();
            }

            @Override
            public int getIndexVersion() {
                return 1;
            }
        }
    }

    public static final class Indexer2 extends CustomIndexer {

        private Indexer2() {
        }

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

        public static final class Factory extends CustomIndexerFactory {

            @Override
            public CustomIndexer createIndexer() {
                return new Indexer2();
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
                return Indexer2.class.getSimpleName();
            }

            @Override
            public int getIndexVersion() {
                return 1;
            }
        }

    }

    public static final class FooRecognizer extends PathRecognizer {

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
            return Collections.singleton(MIME_FOO);
        }

    }

    public static final class CPProviderImpl implements ClassPathProvider {

        private volatile Pair<FileObject,ClassPath> root;

        void setRoot(@NonNull final FileObject root) {
            this.root = Pair.<FileObject,ClassPath>of(
                root,
                ClassPathSupport.createClassPath(root));
        }

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            if (!PATH_SRC.equals(type)) {
                return null;
            }
            final Pair<FileObject,ClassPath> p = root;
            if (p == null) {
                return null;
            }
            final FileObject _root = p.first();
            if (FileUtil.isParentOf(_root, file) || _root.equals(file)) {
                return p.second();
            }
            return null;
        }
    }

    public static final class LogHandler extends Handler {

        private boolean first = true;

        @Override
        public void publish(LogRecord record) {
            if ("indexCommit:{0}:{1}".equals(record.getMessage()) &&    //NOI18N
                Indexer1.class.getSimpleName().equals(record.getParameters()[0]) &&
                first) {
                first = false;                
                throw new RuntimeException();   //Throw some ugly exception
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
