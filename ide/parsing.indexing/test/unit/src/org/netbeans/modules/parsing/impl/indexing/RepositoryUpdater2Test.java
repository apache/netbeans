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
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.core.startup.TopLogging;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * todo:
 * - test indexing empty roots (with no files)
 *
 *
 * @author vita
 */
public class RepositoryUpdater2Test extends IndexingTestBase {

    public RepositoryUpdater2Test(String name) {
        super(name);
    }

    private FileObject workDir = null;
    private RepositoryUpdaterTest.TestHandler ruSync;
    private final Map<String, Set<ClassPath>> registeredClasspaths = new HashMap<String, Set<ClassPath>>();

    @Override
    protected void getAdditionalServices(List<Class> clazz) {
        super.getAdditionalServices(clazz);
        clazz.add(FixedClassPathProvider.class);
        clazz.add(testRootsWorkCancelling_PathRecognizer.class);
        clazz.add(testClasspathDeps1_PathRecognizer.class);
        clazz.add(testAddIndexingJob_PathRecognizer.class);
    }
    
    

    @Override
    protected void setUp() throws Exception {
        TopLogging.initializeQuietly();
        
        super.setUp();

        this.clearWorkDir();
        final File _wd = this.getWorkDir();
        workDir = FileUtil.toFileObject(_wd);

        FileObject cache = workDir.createFolder("cache");
        CacheFolder.setCacheFolder(cache);
        RootsListener.setUseAsyncListneres(false);

        ruSync = new RepositoryUpdaterTest.TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName() + ".tests");
        logger.setLevel(Level.FINEST);
        logger.addHandler(ruSync);

        RepositoryUpdaterTest.waitForRepositoryUpdaterInit();
    }

    @Override
    protected void tearDown() throws Exception {
        for(Map.Entry<String, Set<ClassPath>> entry : registeredClasspaths.entrySet()) {
            String id = entry.getKey();
            Set<ClassPath> classpaths = entry.getValue();
            GlobalPathRegistry.getDefault().unregister(id, classpaths.toArray(new ClassPath[0]));
        }

        MockMimeLookup.setInstances(MimePath.parse("text/plain"));
        dependencies.clear();;
        super.tearDown();
    }

    protected final void globalPathRegistry_register(String id, ClassPath [] classpaths) {
        Set<ClassPath> set = registeredClasspaths.get(id);
        if (set == null) {
            set = new HashSet<ClassPath>();
            registeredClasspaths.put(id, set);
        }
        set.addAll(Arrays.asList(classpaths));
        GlobalPathRegistry.getDefault().register(id, classpaths);
    }

    public void testAddIndexingJob() throws Exception {
        FileUtil.setMIMEType("txt", "text/plain");
        final FileObject srcRoot1 = workDir.createFolder("src1");
        final FileObject file1 = srcRoot1.createData("file1.txt");

        final FileObject srcRoot2 = workDir.createFolder("src2");
        final FileObject file2 = srcRoot2.createData("file2.txt");

        final testAddIndexingJob_CustomIndexer indexer = new testAddIndexingJob_CustomIndexer(srcRoot2.getURL(), file2.getURL());
        final CustomIndexerFactory factory = new CustomIndexerFactory() {
            public @Override CustomIndexer createIndexer() {
                return indexer;
            }

            public @Override void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            }

            @Override
            public void rootsRemoved (final Iterable<? extends URL> rr) {

            }

            public @Override void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            }

            public @Override String getIndexerName() {
                return indexer.getClass().getName();
            }

            public @Override boolean supportsEmbeddedIndexers() {
                return true;
            }

            public @Override int getIndexVersion() {
                return 0;
            }
        };
        MockMimeLookup.setInstances(MimePath.parse("text/plain"), factory);
        RepositoryUpdaterTest.setMimeTypes("text/plain");

        ruSync.reset(RepositoryUpdaterTest.TestHandler.Type.FILELIST, 2);
        RepositoryUpdater.getDefault().addIndexingJob(srcRoot1.getURL(), Collections.singleton(file1.getURL()), false, false, false, true, true, null);
        ruSync.await();

        assertEquals("Wrong number of ordinary scans", 1, indexer.cnt);
        assertEquals("Wrong ordinary root", srcRoot1, indexer.indexedRoot);
        assertEquals("Wrong ordinary files", Collections.singleton(file1), indexer.indexedFiles);
        assertEquals("Wrong number of supplementary scans", 1, indexer.cntSupplementary);
        assertEquals("Wrong supplementary root", srcRoot2, indexer.indexedSupplementaryRoot);
        assertEquals("Wrong supplementary files", Collections.singleton(file2), indexer.indexedSupplementaryFiles);
    }

    protected final void assertEquals(String message, Iterable<?> expected, Iterable<?> actual) {
        assertNotNull(expected);
        
        if (actual == null) {
            assertEquals(message, expected, null);
        }

        Iterator<? extends Object> expectedI = expected.iterator();
        Iterator<? extends Object> actualI = actual.iterator();
        boolean failed = false;
        
        for( ; actualI.hasNext() ; ) {
            if (expectedI.hasNext()) {
                Object expectedO = expectedI.next();
                Object actualO = actualI.next();

                try {
                    assertEquals(expectedO, actualO);
                } catch (AssertionError e) {
                    failed = true;
                    break;
                }
            } else {
                failed = true;
                break;
            }
        }

        if (failed) {
            fail(message + "Expected <" + expected + ">, but was: <" + actual + ">");
        }
    }

    private static final class testAddIndexingJob_CustomIndexer extends CustomIndexer {
        public int cnt = 0;
        public FileObject indexedRoot;
        public Iterable<? extends FileObject> indexedFiles;
        
        public int cntSupplementary = 0;
        public FileObject indexedSupplementaryRoot;
        public Iterable<? extends FileObject> indexedSupplementaryFiles;

        private final URL supplementaryRoot;
        private final URL supplementaryFile;

        public testAddIndexingJob_CustomIndexer(URL supplementaryRoot, URL supplementaryFile) {
            this.supplementaryRoot = supplementaryRoot;
            this.supplementaryFile = supplementaryFile;
        }

        protected @Override void index(Iterable<? extends Indexable> files, Context context) {
            if (context.isSupplementaryFilesIndexing()) {
                indexedSupplementaryRoot = context.getRoot();
                indexedSupplementaryFiles = new Convertor(files);
                cntSupplementary++;
            } else {
                indexedRoot = context.getRoot();
                indexedFiles = new Convertor(files);
                cnt++;
                
                context.addSupplementaryFiles(supplementaryRoot, Collections.singleton(supplementaryFile));
            }
        }

        private static final class Convertor implements Iterable<FileObject> {
            private final Iterable<? extends Indexable> indexables;
            public Convertor(Iterable<? extends Indexable> indexables) {
                this.indexables = indexables;
            }
            public Iterator<FileObject> iterator() {
                final Iterator<? extends Indexable> i = indexables.iterator();
                return new Iterator<FileObject>() {
                    public boolean hasNext() {
                        return i.hasNext();
                    }

                    public FileObject next() {
                        return URLMapper.findFileObject(i.next().getURL());
                    }

                    public void remove() {
                        i.remove();
                    }
                };
            }
        }
    } // End of testAddIndexingJob_CustomIndexer class

    public static final class testAddIndexingJob_PathRecognizer extends PathRecognizer {
        public @Override Set<String> getSourcePathIds() {
            return null;
        }

        public @Override Set<String> getLibraryPathIds() {
            return null;
        }

        public @Override Set<String> getBinaryLibraryPathIds() {
            return null;
        }

        public @Override Set<String> getMimeTypes() {
            return Collections.singleton("text/plain");
        }
    } // End of testAddIndexingJob_PathRecognizer class

    public void testRootsWorkCancelling() throws Exception {
        FileUtil.setMIMEType("txt", "text/plain");
        final Map<URL, FileObject> url2file = new HashMap<URL, FileObject>();
        final FileObject srcRoot1 = workDir.createFolder("src1");
        final FileObject file1 = srcRoot1.createData("file1.txt");
        url2file.put(srcRoot1.getURL(), srcRoot1);

        final FileObject srcRoot2 = workDir.createFolder("src2");
        final FileObject file2 = srcRoot2.createData("file2.txt");
        url2file.put(srcRoot2.getURL(), srcRoot2);

        final FileObject srcRoot3 = workDir.createFolder("src3");
        final FileObject file3 = srcRoot3.createData("file3.txt");
        url2file.put(srcRoot3.getURL(), srcRoot3);

        final testRootsWorkCancelling_CustomIndexer indexer = new testRootsWorkCancelling_CustomIndexer();
        MockMimeLookup.setInstances(MimePath.parse("text/plain"), new testRootsWorkCancelling_CustomIndexerFactory(indexer));
        RepositoryUpdaterTest.setMimeTypes("text/plain");

        assertEquals("No roots should be indexed yet", 0, indexer.indexedRoots.size());
        final RepositoryUpdaterTest.MutableClassPathImplementation mcpi = new RepositoryUpdaterTest.MutableClassPathImplementation();
        mcpi.addResource(srcRoot1, srcRoot2, srcRoot3);
        globalPathRegistry_register(testRootsWorkCancelling_PathRecognizer.SOURCEPATH, new ClassPath[] { ClassPathFactory.createClassPath(mcpi) });

        // wait for the first root to arrive in the indexer
        long tm = System.currentTimeMillis();
        for( ;System.currentTimeMillis() - tm < 5000; ) {
            if (indexer.indexedRoots.size() > 0) {
                break;
            } else {
                try {
                    Thread.sleep(345);
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }
        assertEquals("First root should have already arrived", 1, indexer.indexedRoots.size());

        // remove the other two roots from the classpath; this should trigger another RootsWork
        url2file.keySet().removeAll(indexer.indexedRoots);
        mcpi.removeResource(url2file.values().toArray(new FileObject[0]));

        // unblock the indexer
        try {
            Thread.sleep(PathRegistry.FIRER_EVT_COLLAPSE_WINDOW << 1);
        } catch (InterruptedException ex) {
            throw new AssertionError(ex);
        }
        indexer.blocking.set(false);
        RepositoryUpdater.getDefault().waitUntilFinished(-1);

        assertEquals("Running RootsWork wasn't cancelled by the new one: " + indexer.indexedRoots, 1, indexer.indexedRoots.size());
        assertEquals("Wrong scanned sources", new HashSet<URL>(indexer.indexedRoots), RepositoryUpdater.getDefault().getScannedSources());
        assertEquals("Wrong scanned binaries", 0, RepositoryUpdater.getDefault().getScannedBinaries().size());
        assertEquals("Wrong scanned unknowns", 0, RepositoryUpdater.getDefault().getScannedBinaries().size());
    }

    public void testRootsWorkInterruptible() throws Exception {
        FileUtil.setMIMEType("txt", "text/plain");
        final Map<URL, FileObject> url2file = new HashMap<URL, FileObject>();
        final FileObject srcRoot1 = workDir.createFolder("src1");
        final FileObject file1 = srcRoot1.createData("file1.txt");
        url2file.put(srcRoot1.getURL(), srcRoot1);

        final FileObject srcRoot2 = workDir.createFolder("src2");
        final FileObject file2 = srcRoot2.createData("file2.txt");
        url2file.put(srcRoot2.getURL(), srcRoot2);

        final FileObject srcRoot3 = workDir.createFolder("src3");
        final FileObject file3 = srcRoot3.createData("file3.txt");
        url2file.put(srcRoot3.getURL(), srcRoot3);

        final AtomicInteger counter = new AtomicInteger(0);
        final CountDownLatch[] awaitIndexer = new CountDownLatch[] {
            new CountDownLatch(1),
            new CountDownLatch(1),
            new CountDownLatch(1)
        };
        final CountDownLatch[] awaitUserTask = new CountDownLatch[] {
            new CountDownLatch(1),
            new CountDownLatch(1),
            new CountDownLatch(1)
        };
        final Runnable action = new Runnable() {
            @Override
            public void run() {
                int c = counter.get();
                awaitIndexer[c].countDown();
                try {
                    awaitUserTask[c].await();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        final ActionCustomIndexer indexer = new ActionCustomIndexer(action);
        MockMimeLookup.setInstances(MimePath.parse("text/plain"), new FixedCustomIndexerFactory(indexer), new FixedParserFactory(new EmptyParser()));
        try {
            RepositoryUpdaterTest.setMimeTypes("text/plain");

            assertEquals("No roots should be indexed yet", 0, indexer.indexedRoots.size());
            final RepositoryUpdaterTest.MutableClassPathImplementation mcpi = new RepositoryUpdaterTest.MutableClassPathImplementation();
            mcpi.addResource(srcRoot1, srcRoot2, srcRoot3);
            globalPathRegistry_register(testRootsWorkCancelling_PathRecognizer.SOURCEPATH, new ClassPath[] { ClassPathFactory.createClassPath(mcpi) });

            for(int cnt = 1; cnt <= 3; cnt++) {
                awaitIndexer[counter.get()].await(10, TimeUnit.SECONDS);
                assertEquals("Wrong number of roots indexed", cnt, indexer.indexedRoots.size());

                final int fcnt = cnt;
                final boolean [] taskCalled = new boolean [] { false };
                ParserManager.parse("text/plain", new UserTask() {
                    public @Override void run(ResultIterator resultIterator) throws Exception {
                        awaitUserTask[counter.getAndIncrement()].countDown();
                        Thread.sleep(2000); //Give RU.worker chance to displatch next root - should not happen should be blocked.
                        assertEquals("No more roots should be indexed", fcnt, indexer.indexedRoots.size());
                        taskCalled[0] = true;
                    }
                });
                assertTrue("UserTask not called", taskCalled[0]);
            }

            RepositoryUpdater.getDefault().waitUntilFinished(-1);

            assertEquals("All roots should be indexed: " + indexer.indexedRoots, 3, indexer.indexedRoots.size());
            ArrayList<URL> expectedSourceRoots = new ArrayList<URL>(indexer.indexedRoots);
            expectedSourceRoots.sort(new RepositoryUpdater.LexicographicComparator(true));
            assertEquals("Wrong scanned sources", expectedSourceRoots, RepositoryUpdater.getDefault().getScannedSources());
            assertEquals("Wrong scanned binaries", 0, RepositoryUpdater.getDefault().getScannedBinaries().size());
            assertEquals("Wrong scanned unknowns", 0, RepositoryUpdater.getDefault().getScannedUnknowns().size());
        } finally {
            // unfreeze everything which could be waiting
            for (int i = 0; i < awaitUserTask.length; i++) {
                awaitUserTask[i].countDown();
            }
            for (int i = 0; i < awaitIndexer.length; i++) {
                awaitIndexer[i].countDown();
            }
        }
    }

    public static final class testRootsWorkCancelling_PathRecognizer extends PathRecognizer {
        public static final String SOURCEPATH = "testRootsWorkCancelling/SOURCES";

        public @Override Set<String> getSourcePathIds() {
            return Collections.<String>singleton(SOURCEPATH);
        }

        public @Override Set<String> getLibraryPathIds() {
            return null;
        }

        public @Override Set<String> getBinaryLibraryPathIds() {
            return null;
        }

        public @Override Set<String> getMimeTypes() {
            return Collections.singleton("text/plain");
        }
    } // End of testAddIndexingJob_PathRecognizer class

    private static final class testRootsWorkCancelling_CustomIndexer extends CustomIndexer {

        public final AtomicBoolean blocking = new AtomicBoolean(true);
        public final List<URL> indexedRoots = Collections.synchronizedList(new ArrayList<URL>());
        
        @Override
        protected void index(Iterable<? extends Indexable> files, Context context) {
            indexedRoots.add(context.getRootURI());
            
            for( ; blocking.get(); ) {
                try {
                    Thread.sleep(123);
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }

    } // End of testRootsWorkCancelling_CustomIndexer class

    private static final class testRootsWorkCancelling_CustomIndexerFactory extends CustomIndexerFactory {

        private final testRootsWorkCancelling_CustomIndexer indexer;

        public testRootsWorkCancelling_CustomIndexerFactory(@NonNull final testRootsWorkCancelling_CustomIndexer indexer) {
            Parameters.notNull("indexer", indexer); //NOI18N
            this.indexer = indexer;
        }

        @Override
        public CustomIndexer createIndexer() {
            return indexer;
        }

        @Override
        public void scanFinished(Context context) {
            if (context.isCancelled()) {
                indexer.indexedRoots.remove(context.getRootURI());
            }
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return true;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
        }

        @Override
        public String getIndexerName() {
            return indexer.toString();
        }

        @Override
        public int getIndexVersion() {
            return 1;
        }
    }

    private static final class TimeoutCustomIndexer extends CustomIndexer {

        private final long timeout;
        public final List<URL> indexedRoots = Collections.synchronizedList(new ArrayList<URL>());

        public TimeoutCustomIndexer(long timeout) {
            this.timeout = timeout;
        }
        
        protected @Override void index(Iterable<? extends Indexable> files, Context context) {
            indexedRoots.add(context.getRootURI());
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException ex) {
            }
        }

    } // End of TimeoutCustomIndexer class
    
    private static final class ActionCustomIndexer extends CustomIndexer {
        
        public final List<URL> indexedRoots = Collections.synchronizedList(new ArrayList<URL>());
        private final Runnable action;
        
        ActionCustomIndexer(@NonNull final Runnable action) {
            action.getClass();
            this.action = action;
        }

        @Override
        protected void index(Iterable<? extends Indexable> files, Context context) {
            indexedRoots.add(context.getRootURI());
            this.action.run();
        }
    } // End of ActionCustomIndexer class

    private static final class FixedCustomIndexerFactory<T extends CustomIndexer> extends CustomIndexerFactory {

        private final Class<T> customIndexerClass;
        private final T customIndexerInstance;
        private final String indexerName;
        private final int indexerVersion;

        public FixedCustomIndexerFactory(Class<T> customIndexerClass) {
            this.customIndexerClass = customIndexerClass;
            this.customIndexerInstance = null;
            this.indexerName = customIndexerClass.getName();
            this.indexerVersion = 1;
        }

        public FixedCustomIndexerFactory(T customIndexerInstance) {
            this.customIndexerClass = null;
            this.customIndexerInstance = customIndexerInstance;
            this.indexerName = customIndexerInstance.toString();
            this.indexerVersion = 1;
        }

        @Override
        public T createIndexer() {
            if (customIndexerInstance != null) {
                return customIndexerInstance;
            } else {
                try {
                    return customIndexerClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
        }

        @Override
        public void rootsRemoved(final Iterable<? extends URL> removedRoots) {

        }

        @Override
        public String getIndexerName() {
            return indexerName;
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return true;
        }

        @Override
        public int getIndexVersion() {
            return indexerVersion;
        }
    } // End of FixedCustomIndexerFactory class

    private static final class FixedParserFactory<T extends Parser> extends ParserFactory {

        private final Class<T> parserClass;
        private final T parserInstance;

        public FixedParserFactory(Class<T> customIndexerClass) {
            this.parserClass = customIndexerClass;
            this.parserInstance = null;
        }

        public FixedParserFactory(T parserInstance) {
            this.parserClass = null;
            this.parserInstance = parserInstance;
        }

        public @Override T createParser(Collection<Snapshot> snapshots) {
            if (parserInstance != null) {
                return parserInstance;
            } else {
                try {
                    return parserClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
    } // End of FixedParserFactory class

    private static final class EmptyParser extends Parser {

        private Snapshot snapshot;

        public @Override void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        }

        public @Override Result getResult(Task task) throws ParseException {
            return new Result(snapshot) {
                protected @Override void invalidate() {
                }
            };
        }

        public @Override void cancel() {
        }

        public @Override void addChangeListener(ChangeListener changeListener) {
        }

        public @Override void removeChangeListener(ChangeListener changeListener) {
        }
    } // End of EmptyParser class

    public void testClasspathDeps1() throws IOException, InterruptedException {
        FileUtil.setMIMEType("txt", "text/plain");
        final FileObject srcRoot1 = workDir.createFolder("src1");
        srcRoot1.createData("file1.txt");
        final FileObject srcRoot2 = workDir.createFolder("src2");
        srcRoot2.createData("file2.txt");
        final FileObject srcRoot3 = workDir.createFolder("src3");
        srcRoot3.createData("file3.txt");
        final FileObject srcRoot4 = workDir.createFolder("src4");
        srcRoot4.createData("file4.txt");

        final RepositoryUpdaterTest.MutableClassPathImplementation mcpi1 = new RepositoryUpdaterTest.MutableClassPathImplementation();
        final ClassPath cp1 = ClassPathFactory.createClassPath(mcpi1);
        mcpi1.addResource(srcRoot1);
        final RepositoryUpdaterTest.MutableClassPathImplementation mcpi2 = new RepositoryUpdaterTest.MutableClassPathImplementation();
        final ClassPath cp2 = ClassPathFactory.createClassPath(mcpi2);
        mcpi2.addResource(srcRoot2);
        final RepositoryUpdaterTest.MutableClassPathImplementation mcpi3 = new RepositoryUpdaterTest.MutableClassPathImplementation();
        final ClassPath cp3 = ClassPathFactory.createClassPath(mcpi3);
        mcpi3.addResource(srcRoot3);

        Map<String, ClassPath> deps1 = new HashMap<String, ClassPath>();
        deps1.put(testClasspathDeps1_PathRecognizer.CP1, cp1);
        deps1.put(testClasspathDeps1_PathRecognizer.CP2, cp2);
//        deps1.put(testClasspathDeps1_PathRecognizer.CP3, cp3);

        Map<String, ClassPath> deps2 = new HashMap<String, ClassPath>();
        deps2.put(testClasspathDeps1_PathRecognizer.CP2, cp2);
//        deps2.put(testClasspathDeps1_PathRecognizer.CP3, cp3);

//        Map<String, ClassPath> deps3 = new HashMap<String, ClassPath>();
//        deps3.put(testClasspathDeps1_PathRecognizer.CP3, cp3);

        Map<FileObject, Map<String, ClassPath>> allDeps = new HashMap<FileObject, Map<String, ClassPath>>();
        allDeps.put(srcRoot1, deps1);
        allDeps.put(srcRoot2, deps2);
//        allDeps.put(srcRoot3, deps3);
        
        dependencies.clear();
        dependencies.putAll(allDeps);

        final testClasspathDeps1_Indexer indexer = new testClasspathDeps1_Indexer();
        MockMimeLookup.setInstances(MimePath.parse("text/plain"), new FixedCustomIndexerFactory(indexer));
        RepositoryUpdaterTest.setMimeTypes("text/plain");

        assertEquals("No roots should be indexed yet", 0, indexer.indexedRoots.size());

        globalPathRegistry_register(testClasspathDeps1_PathRecognizer.CP1, new ClassPath[] { cp1 });
        globalPathRegistry_register(testClasspathDeps1_PathRecognizer.CP2, new ClassPath[] { cp2 });
        globalPathRegistry_register(testClasspathDeps1_PathRecognizer.CP3, new ClassPath[] { cp3 });

        Collection<? extends PathRecognizer> pathRecognizers = Lookup.getDefault().lookupAll(PathRecognizer.class);
        Logger.getLogger(RepositoryUpdater.class.getName()).fine("PathRecognizers: " + pathRecognizers);

        Thread.sleep(2000);
        RepositoryUpdater.getDefault().waitUntilFinished(-1);

        //The srcRoot1, srcRoot2 and srcRoot3 should be indexed
        //The srcRoot2 should be indexed before srcRoot1 because of dependency
        //The srcRoot3 has no ordering to srcRoot1 and srcRoot2 as there is no dependency
        assertEquals("All roots should be indexed now", 3, indexer.indexedRoots.size());
        assertTrue(indexer.indexedRoots.contains(srcRoot1.getURL()));
        assertTrue(indexer.indexedRoots.contains(srcRoot2.getURL()));
        assertTrue(indexer.indexedRoots.contains(srcRoot3.getURL()));
        assertTrue(indexer.indexedRoots.indexOf(srcRoot2.getURL()) < indexer.indexedRoots.indexOf(srcRoot1.getURL()));

        indexer.indexedRoots.clear();
        mcpi2.addResource(srcRoot4);
        
        Thread.sleep(2000);
        RepositoryUpdater.getDefault().waitUntilFinished(-1);

        //The srcRoot1, srcRoot2 and srcRoot4 should be indexed.
        //The srcRoot2 and srcRoot4 should be indexed before srcRoot1 because of dependency
        assertEquals("Additional and dependent roots not indexed", 3, indexer.indexedRoots.size());
        assertTrue(indexer.indexedRoots.contains(srcRoot1.getURL()));
        assertTrue(indexer.indexedRoots.contains(srcRoot2.getURL()));
        assertTrue(indexer.indexedRoots.contains(srcRoot4.getURL()));
        assertTrue(indexer.indexedRoots.indexOf(srcRoot2.getURL()) < indexer.indexedRoots.indexOf(srcRoot1.getURL()));
        assertTrue(indexer.indexedRoots.indexOf(srcRoot4.getURL()) < indexer.indexedRoots.indexOf(srcRoot1.getURL()));

    }

    public static final class testClasspathDeps1_PathRecognizer extends PathRecognizer {

        public static final String CP1 = "testClasspathDeps1_PathRecognizer/CP1";
        public static final String CP2 = "testClasspathDeps1_PathRecognizer/CP2";
        public static final String CP3 = "testClasspathDeps1_PathRecognizer/CP3";

        @Override
        public Set<String> getSourcePathIds() {
            return new HashSet<String>(Arrays.asList(new String [] {
                CP1, CP3
            }));
        }

        @Override
        public Set<String> getLibraryPathIds() {
            return Collections.singleton(CP2);
        }

        @Override
        public Set<String> getBinaryLibraryPathIds() {
            return null;
        }

        @Override
        public Set<String> getMimeTypes() {
            return Collections.singleton("text/plain");
        }
    } // End of testClasspathDeps1_PathRecognizer class

    private static final class testClasspathDeps1_Indexer extends CustomIndexer {

        public final List<URL> indexedRoots = new LinkedList<URL>();

        protected @Override void index(Iterable<? extends Indexable> files, Context context) {
            indexedRoots.add(context.getRootURI());
        }
    
    } // End of testClasspathDeps1_Indexer class
    
    private static Map<FileObject, Map<String, ClassPath>> dependencies = new HashMap<>();

    public static final class FixedClassPathProvider implements ClassPathProvider {

        public FixedClassPathProvider() {
        }
        
        public ClassPath findClassPath(FileObject file, String type) {
            Map<String, ClassPath> classpaths = dependencies.get(file);
            if (classpaths != null) {
                return classpaths.get(type);
            } else {
                return null;
            }
        }
    } // End of FixedClassPathProvider class

    @RandomlyFails
    public void testNestedRoots() throws Exception {
        FileUtil.setMIMEType("txt", "text/plain");
        final FileObject root = workDir.createFolder("root");
        final FileObject file1 = root.createData("file1.txt");

        final FileObject nestedRoot = root.createFolder("nestedRoot");
        final FileObject file2 = nestedRoot.createData("file2.txt");

        final RepositoryUpdaterTest.MutableClassPathImplementation mcpi = new RepositoryUpdaterTest.MutableClassPathImplementation();
        mcpi.addFilteredResource(root, "nestedRoot.*");
        mcpi.addResource(nestedRoot);
        ClassPath cp = ClassPathFactory.createClassPath(mcpi);

        Map<FileObject, Map<String, ClassPath>> allDeps = new HashMap<FileObject, Map<String, ClassPath>>();
        allDeps.put(root, Collections.singletonMap(testRootsWorkCancelling_PathRecognizer.SOURCEPATH, cp));
        allDeps.put(nestedRoot, Collections.singletonMap(testRootsWorkCancelling_PathRecognizer.SOURCEPATH, cp));

        dependencies.clear();
        dependencies.putAll(allDeps);

        final TestCustomIndexer indexer = new TestCustomIndexer();
        MockMimeLookup.setInstances(MimePath.parse("text/plain"), new FixedCustomIndexerFactory(indexer), new FixedParserFactory(new EmptyParser()));
        RepositoryUpdaterTest.setMimeTypes("text/plain");

        assertEquals("No roots should be indexed yet", 0, indexer.indexed.size());

        ruSync.reset(RepositoryUpdaterTest.TestHandler.Type.BATCH);
        globalPathRegistry_register(testRootsWorkCancelling_PathRecognizer.SOURCEPATH, new ClassPath[] { cp });
        ruSync.await();

        assertEquals("Wrong number of roots", 2, indexer.indexed.size());
//        assertEquals("Expecting nested root", nestedRoot.getURL(), indexer.indexed.get(0).first.getRootURI());
//        assertEquals("Wrong number of files under nestedRoot", 1, indexer.indexed.get(0).second.size());
//        assertEquals("Expecting root", root.getURL(), indexer.indexed.get(1).first.getRootURI());
//        assertEquals("Wrong number of files under root", 1, indexer.indexed.get(1).second.size());

        indexer.indexed.clear();
        ruSync.reset(RepositoryUpdaterTest.TestHandler.Type.FILELIST, 1);
        OutputStream os = file2.getOutputStream();
        try {
            os.write('\n');
        } finally {
            os.close();
        }
        ruSync.await();

        assertEquals("Wrong number of roots", 1, indexer.indexed.size());
        assertEquals("Expecting nested root", nestedRoot.getURL(), indexer.indexed.get(0).first().getRootURI());
        assertEquals("Wrong number of files under nestedRoot", 1, indexer.indexed.get(0).second().size());
        assertEquals("Wring file indexed", file2.getURL(), indexer.indexed.get(0).second().get(0).getURL());
    }

    public void testMarkingIndexesDirty() throws Exception {
        FileUtil.setMIMEType("txt", "text/plain");
        final FileObject root = workDir.createFolder("root");
        final FileObject file1 = root.createData("file1.txt");

        final RepositoryUpdaterTest.MutableClassPathImplementation mcpi = new RepositoryUpdaterTest.MutableClassPathImplementation();
        mcpi.addResource(root);
        ClassPath cp = ClassPathFactory.createClassPath(mcpi);

        Map<FileObject, Map<String, ClassPath>> allDeps = new HashMap<FileObject, Map<String, ClassPath>>();
        allDeps.put(root, Collections.singletonMap(testRootsWorkCancelling_PathRecognizer.SOURCEPATH, cp));
        
        dependencies.clear();
        dependencies.putAll(allDeps);

        final TestCustomIndexer indexer = new TestCustomIndexer();
        MockMimeLookup.setInstances(MimePath.parse("text/plain"), new FixedCustomIndexerFactory(indexer), new FixedParserFactory(new EmptyParser()));
        RepositoryUpdaterTest.setMimeTypes("text/plain");

        assertEquals("No roots should be indexed yet", 0, indexer.indexed.size());

        ruSync.reset(RepositoryUpdaterTest.TestHandler.Type.BATCH);
        globalPathRegistry_register(testRootsWorkCancelling_PathRecognizer.SOURCEPATH, new ClassPath[] { cp });
        ruSync.await();

        assertEquals("Wrong number of roots", 1, indexer.indexed.size());

        // the indexer does not store any data and so physically there is no lucene index on the disk
        // therefore we have to forcibly create one
        Context ctx = indexer.indexed.get(0).first();
        DocumentIndex ii = SPIAccessor.getInstance().getIndexFactory(ctx).createIndex(ctx);
        ii.markKeyDirty("file1.txt"); // marks index as dirty and adds file1.txt among stale files
        assertEquals("Wrong number of stale files", 1, ii.getDirtyKeys().size());
        assertEquals("file1.txt should be a stale file", "file1.txt", ii.getDirtyKeys().iterator().next());

        indexer.indexed.clear();
        ruSync.reset(RepositoryUpdaterTest.TestHandler.Type.FILELIST, 1);
        RepositoryUpdater.getDefault().addIndexingJob(root.getURL(), Collections.singleton(file1.getURL()), false, false, true, true, true, null);
        ruSync.await();

        assertEquals("Wrong number of roots", 1, indexer.indexed.size());
        assertEquals("Expecting root", root.getURL(), indexer.indexed.get(0).first().getRootURI());
        assertEquals("Wrong number of files under root", 1, indexer.indexed.get(0).second().size());
        assertEquals("Wrong file indexed", file1.getURL(), indexer.indexed.get(0).second().get(0).getURL());
        assertEquals("Expecting no stale files", 0, ii.getDirtyKeys().size());
    }

    private static final class TestCustomIndexer extends CustomIndexer {

        public final List<Pair<Context, List<Indexable>>> indexed = new ArrayList<Pair<Context, List<Indexable>>>();
        
        @Override
        protected void index(Iterable<? extends Indexable> files, Context context) {
            List<Indexable> indexables = new ArrayList<Indexable>();
            for(Indexable i : files) {
                indexables.add(i);
            }
            Pair<Context, List<Indexable>> pair = Pair.of(context, indexables);
            indexed.add(pair);
        }
    }

    // ---------- !!!!!! This MUST be the last test in the suite,
    // ---------- !!!!!! because it shuts down RepositoryUpdater

    @RandomlyFails
    public void testShuttdown() throws InterruptedException {
        testShuttdown_TimedWork work1 = new testShuttdown_TimedWork();
        testShuttdown_TimedWork work2 = new testShuttdown_TimedWork();
        testShuttdown_TimedWork work3 = new testShuttdown_TimedWork();
        testShuttdown_TimedWork work4 = new testShuttdown_TimedWork();
        testShuttdown_TimedWork work5 = new testShuttdown_TimedWork();
        RepositoryUpdater.getDefault().scheduleWork(work1, false);
        RepositoryUpdater.getDefault().scheduleWork(work2, false);
        RepositoryUpdater.getDefault().scheduleWork(work3, false);
        RepositoryUpdater.getDefault().scheduleWork(work4, false);
        RepositoryUpdater.getDefault().scheduleWork(work5, false);

        boolean successfullyStopped = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    RepositoryUpdater.getDefault().stop(null);
                } catch (TimeoutException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }).waitFinished(5000);

        assertTrue("RepositoryUpdate.stop() timed out", successfullyStopped);
        assertTrue("work1's getDone method not called", work1.getDoneCalled);
        assertTrue("work1 was not cancelled", work1.workCancelled);
        assertFalse("work2's getDone method should not be called", work2.getDoneCalled);
        assertFalse("work2 was should not be cancelled", work2.workCancelled);
        assertFalse("work3's getDone method should not be called", work3.getDoneCalled);
        assertFalse("work3 was should not be cancelled", work3.workCancelled);
        assertFalse("work4's getDone method should not be called", work4.getDoneCalled);
        assertFalse("work4 was should not be cancelled", work4.workCancelled);
        assertFalse("work5's getDone method should not be called", work5.getDoneCalled);
        assertFalse("work5 was should not be cancelled", work5.workCancelled);
    }

    private static final class testShuttdown_TimedWork extends RepositoryUpdater.Work {
        public boolean getDoneCalled = false;
        public boolean workCancelled = false;

        public testShuttdown_TimedWork() {
            super(false, false, false, true, SuspendSupport.NOP, null);
        }

        protected @Override boolean getDone() {
            getDoneCalled = true;
            while(!getCancelRequest().isRaised()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    //ignore
                }
            }
            workCancelled = getCancelRequest().isRaised();
            return true;
        }
    } // End of testShuttdown_TimedWork class

}
