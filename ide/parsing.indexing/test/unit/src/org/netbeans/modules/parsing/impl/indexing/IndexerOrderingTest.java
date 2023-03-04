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

import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.SourceIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.support.EmbeddedPathRecognizer;
import static org.netbeans.modules.parsing.spi.indexing.support.EmbeddedPathRecognizer.EXT_EMB;
import static org.netbeans.modules.parsing.spi.indexing.support.EmbeddedPathRecognizer.SRC_EMB;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;
import org.openide.util.test.TestFileUtils;

/**
 *
 * @author Tomas Zezula
 */
public class IndexerOrderingTest extends IndexingTestBase {

    private static final String MIME_FOO = FooPathRecognizer.FOO_MIME;    //NOI18N
    private static final String EXT_FOO = FooPathRecognizer.FOO_EXT;            //NOI18N
    private static final String SRC_FOO = FooPathRecognizer.FOO_SOURCES;        //NOI18N
    private static final String MIME_EMB = EmbeddedPathRecognizer.EMB_MIME;    //NOI18N

    private static final Queue<SourceIndexerFactory> calls =
        new ConcurrentLinkedQueue<SourceIndexerFactory>();

    private final Map<String, Map<ClassPath,Void>> registeredClasspaths =
        new HashMap<String, Map<ClassPath,Void>>();
    private FileObject froot;
    private FileObject eroot;

    public IndexerOrderingTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        froot = FileUtil.createFolder(wd, "froot");  //NOI18N
        final FileObject foo = FileUtil.createData(froot, "test.foo");  //NOI18N
        TestFileUtils.writeFile(FileUtil.toFile(foo), "foo");   //NOI18N
        eroot = FileUtil.createFolder(wd, "eroot");  //NOI18N
        final FileObject emb = FileUtil.createData(eroot, "test.emb");  //NOI18N
        TestFileUtils.writeFile(FileUtil.toFile(emb), "emb");   //NOI18N

        MockMimeLookup.setInstances(MimePath.get(MIME_FOO),
            new CustomIndexerFactoryImpl("CI1",1),
            new CustomIndexerFactoryImpl("CI2",2),
            new CustomIndexerFactoryImpl("CI3",3),
            new CustomIndexerFactoryImpl("CI4",4),
            new CustomIndexerFactoryImpl("CI5",5),
            new FooParserFactory());
        MockMimeLookup.setInstances(MimePath.get(MIME_EMB),
            new EmbeddingIndexerFactoryImpl("EI5",5),
            new EmbeddingIndexerFactoryImpl("EI3",3),
            new EmbeddingIndexerFactoryImpl("EI2",2),
            new EmbeddingIndexerFactoryImpl("EI1",1),
            new EmbeddingIndexerFactoryImpl("EI4",4),
            new EmbParserFactory());
        FileUtil.setMIMEType(EXT_FOO, MIME_FOO);
        FileUtil.setMIMEType(EXT_EMB, MIME_EMB);
        RepositoryUpdaterTest.setMimeTypes(MIME_FOO, MIME_EMB);
        RepositoryUpdaterTest.waitForRepositoryUpdaterInit();
    }

    @Override
    protected void tearDown() throws Exception {
        final RepositoryUpdaterTest.TestHandler handler = new RepositoryUpdaterTest.TestHandler();
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



    public void testCustomIndexerOrdering() throws InterruptedException {
        assertTrue(GlobalPathRegistry.getDefault().getPaths(SRC_FOO).isEmpty());
        final RepositoryUpdaterTest.TestHandler handler = new RepositoryUpdaterTest.TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        try {
            final ClassPath cp = ClassPathSupport.createClassPath(froot);
            calls.clear();
            globalPathRegistry_register(SRC_FOO, new ClassPath[]{cp});
            assertTrue (handler.await());
            assertEquals(5, calls.size());
            final Iterator<SourceIndexerFactory> callsIt = calls.iterator();
            for (int i=1; i<=5; i++) {
                assertEquals(
                    String.format("CI%d",i),    //NOI18N
                    callsIt.next().getIndexerName());
            }
        } finally {
            logger.removeHandler(handler);
        }
    }

    public void testEmbeddingIndexerOrdering() throws InterruptedException {
        assertTrue(GlobalPathRegistry.getDefault().getPaths(SRC_EMB).isEmpty());
        final RepositoryUpdaterTest.TestHandler handler = new RepositoryUpdaterTest.TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        try {
            final ClassPath cp = ClassPathSupport.createClassPath(eroot);
            calls.clear();
            globalPathRegistry_register(SRC_EMB, new ClassPath[]{cp});
            assertTrue (handler.await());
            assertEquals(5, calls.size());
            final Iterator<SourceIndexerFactory> callsIt = calls.iterator();
            for (int i=1; i<=5; i++) {
                assertEquals(
                    String.format("EI%d",i),    //NOI18N
                    callsIt.next().getIndexerName());
            }
            
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


    private static final class CustomIndexerImpl<T extends CustomIndexerFactory> extends CustomIndexer {

        private final T owner;

        CustomIndexerImpl(@NonNull final T owner) {
            Parameters.notNull("owner", owner); //NOI18N
            this.owner = owner;
        }

        @Override
        protected void index(Iterable<? extends Indexable> files, Context context) {
            calls.offer(owner);
        }
    }

    private static final class EmbeddingIndexerImpl<T extends EmbeddingIndexerFactory> extends EmbeddingIndexer {

        private final T owner;

        EmbeddingIndexerImpl(@NonNull final T owner) {
            Parameters.notNull("owner", owner); //NOI18N
            this.owner = owner;
        }

        @Override
        protected void index(Indexable indexable, Parser.Result parserResult, Context context) {
            calls.offer(owner);
        }
    }

    public static final class CustomIndexerFactoryImpl extends CustomIndexerFactory {

        private final String name;
        private final int priority;

        CustomIndexerFactoryImpl(@NonNull final String name, final int priority) {
            this.name = name;
            this.priority = priority;
        }

        @Override
        public CustomIndexer createIndexer() {
            return new CustomIndexerImpl(this);
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
            return 1;
        }
    }    

    public static final class EmbeddingIndexerFactoryImpl extends EmbeddingIndexerFactory {

        private final String name;
        private final int priority;

        EmbeddingIndexerFactoryImpl(String name, int priority) {
            this.name = name;
            this.priority = priority;
        }

        @Override
        public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
            return new EmbeddingIndexerImpl(this);
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
            return 1;
        }

        @Override
        public int getPriority() {
            return priority;
        }


    }

    public static final class FooParserFactory extends ParserFactory {

        private static final class FooParser extends Parser {

            private volatile Snapshot currentSnapshot;

            @Override
            public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                Parameters.notNull("snapshot", snapshot);   //NOI18N
                this.currentSnapshot = snapshot;
            }

            @Override
            public Result getResult(Task task) throws ParseException {
                final Snapshot snapshot = this.currentSnapshot;
                if (snapshot == null) {
                    throw new IllegalStateException("No Snapshot"); //NOI18N
                }
                return new Result(snapshot) {
                    @Override
                    protected void invalidate() {
                    }
                };
            }

            @Override
            public void addChangeListener(ChangeListener changeListener) {
            }

            @Override
            public void removeChangeListener(ChangeListener changeListener) {
            }
        }

        @Override
        public Parser createParser(Collection<Snapshot> snapshots) {
            return new FooParser();
        }
    }

    public static final class EmbParserFactory extends ParserFactory {

        private static final class EmbParser extends Parser {

            private volatile Snapshot currentSnapshot;

            @Override
            public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                Parameters.notNull("snapshot", snapshot);   //NOI18N
                this.currentSnapshot = snapshot;
            }

            @Override
            public Result getResult(Task task) throws ParseException {
                final Snapshot snapshot = currentSnapshot;
                if (snapshot == null) {
                    throw new IllegalStateException("No Snapshot"); //NOI18N
                }
                return new Result(snapshot) {
                    @Override
                    protected void invalidate() {
                    }
                };
            }

            @Override
            public void addChangeListener(ChangeListener changeListener) {
            }

            @Override
            public void removeChangeListener(ChangeListener changeListener) {
            }
        }

        @Override
        public Parser createParser(Collection<Snapshot> snapshots) {
            return new EmbParser();
        }
    }

}
