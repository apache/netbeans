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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.RunWhenScanFinishedSupport;
import org.netbeans.modules.parsing.impl.SourceAccessor;
import org.netbeans.modules.parsing.impl.SourceFlags;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.impl.indexing.friendapi.DownloadedIndexPatcher;
import org.netbeans.modules.parsing.impl.indexing.friendapi.IndexDownloader;
import org.netbeans.modules.parsing.impl.indexing.friendapi.IndexingController;
import org.netbeans.modules.parsing.impl.indexing.lucene.LayeredDocumentIndex;
import org.netbeans.modules.parsing.impl.indexing.lucene.LuceneIndexFactory;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.spi.*;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexer;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.IndexabilityQueryImplementation;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.classpath.support.PathResourceBase;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.queries.VisibilityQueryImplementation2;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.BaseUtilities;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 * TODO:
 * - test that modifying .zip/.jar triggeres rescan of this binary
 *
 * @author Tomas Zezula
 */
@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
public class RepositoryUpdaterTest extends IndexingTestBase {


    public static final int TIME = Integer.getInteger("RepositoryUpdaterTest.timeout", 5000);                 //NOI18N
    public static final int NEGATIVE_TIME = Integer.getInteger("RepositoryUpdaterTest.negative-timeout", 5000); //NOI18N
    private static final String SOURCES = "FOO_SOURCES";
    private static final String PLATFORM = "FOO_PLATFORM";
    private static final String LIBS = "FOO_LIBS";
    private static final String MIME = "text/foo";
    private static final String EMIME = "text/emb";
    private static final String JARMIME = "application/java-archive";

    private FileObject srcRoot1;
    private FileObject srcRoot2;
    private FileObject srcRoot3;
    private FileObject compRoot1;
    private FileObject compRoot2;
    private FileObject bootRoot1;
    private FileObject bootRoot2;
    private FileObject bootRoot3;
    private FileObject compSrc1;
    private FileObject compSrc2;
    private FileObject bootSrc1;
    private FileObject unknown1;
    private FileObject unknown2;
    private FileObject unknownSrc2;
    protected FileObject srcRootWithFiles1;
    
    private FileObject jarFile;

    FileObject f1;
    protected FileObject f3;

    private URL[] customFiles;
    private URL[] embeddedFiles;

    private final BinIndexerFactory binIndexerFactory = new BinIndexerFactory();
// Binary indexer have to be registered for MimePath.EMPTY, no mime-type specific binary indexers
//    private final BinIndexerFactory jarIndexerFactory = new BinIndexerFactory();
    private final FooIndexerFactory indexerFactory = new FooIndexerFactory();
    private final EmbIndexerFactory eindexerFactory = new EmbIndexerFactory();

    private final Map<String, Map<ClassPath,Void>> registeredClasspaths = new HashMap<>();

    public RepositoryUpdaterTest (String name) {
        super (name);
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(RepositoryUpdaterTest.class);
//        TestSuite suite = new NbTestSuite ();
//        suite.addTest(new RepositoryUpdaterTest("testPeerRootsCalculation"));
        return suite;
    }

    @Override
    protected void getAdditionalServices(List<Class> serv) {
        super.getAdditionalServices(serv);
        serv.addAll(
                Arrays.asList(new Class[] {
                    FooPathRecognizer.class,
                    EmbPathRecognizer.class,
                    SFBQImpl.class,
                    ClassPathProviderImpl.class,
                    Visibility.class,
                    Indexability.class,
                    IndexDownloaderImpl.class,
                    IndexPatcherImpl.class,
                })
        );
    }

    @Override
    protected void setUp() throws Exception {
//        TopLogging.initializeQuietly();
        super.setUp();
        this.clearWorkDir();
        final File _wd = this.getWorkDir();
        final FileObject wd = FileUtil.toFileObject(_wd);
        final FileObject cache = wd.createFolder("cache");
        CacheFolder.setCacheFolder(cache);
        RootsListener.setUseAsyncListneres(false);

        MockMimeLookup.setInstances(MimePath.EMPTY, binIndexerFactory);
//        MockMimeLookup.setInstances(MimePath.get(JARMIME), jarIndexerFactory);
        MockMimeLookup.setInstances(MimePath.get(MIME), indexerFactory);
        MockMimeLookup.setInstances(MimePath.get(EMIME), eindexerFactory, new EmbParserFactory());
        setMimeTypes(EMIME, MIME);

        assertNotNull("No masterfs",wd);
        srcRoot1 = wd.createFolder("src1");
        assertNotNull(srcRoot1);
        srcRoot2 = wd.createFolder("src2");
        assertNotNull(srcRoot2);
        srcRoot3 = wd.createFolder("src3");
        assertNotNull (srcRoot3);
        compRoot1 = wd.createFolder("comp1");
        assertNotNull (compRoot1);
        compRoot2 = wd.createFolder("comp2");
        assertNotNull (compRoot2);
        bootRoot1 = wd.createFolder("boot1");
        assertNotNull (bootRoot1);
        bootRoot2 = wd.createFolder("boot2");
        assertNotNull (bootRoot2);
        bootRoot3 = wd.createFolder("boot3");
        assertNotNull (bootRoot3);
        
        FileUtil.setMIMEType("jar", JARMIME);
        jarFile = FileUtil.createData(bootRoot3, "JavaApplication1.jar");
        assertNotNull(jarFile);
        zipFileObject(jarFile);
        assertTrue(FileUtil.isArchiveFile(jarFile));
        
        compSrc1 = wd.createFolder("cs1");
        assertNotNull (compSrc1);
        compSrc2 = wd.createFolder("cs2");
        assertNotNull (compSrc2);
        bootSrc1 = wd.createFolder("bs1");
        assertNotNull (bootSrc1);
        unknown1 = wd.createFolder("uknw1");
        assertNotNull (unknown1);
        unknown2 = wd.createFolder("uknw2");
        assertNotNull (unknown2);
        unknownSrc2 = wd.createFolder("uknwSrc2");
        assertNotNull(unknownSrc2);
        SFBQImpl.register (bootRoot1,bootSrc1);
        SFBQImpl.register (compRoot1,compSrc1);
        SFBQImpl.register (compRoot2,compSrc2);
        SFBQImpl.register (unknown2,unknownSrc2);

        srcRootWithFiles1 = wd.createFolder("srcwf1");
        assertNotNull(srcRootWithFiles1);
        FileUtil.setMIMEType("foo", MIME);
        f1 = FileUtil.createData(srcRootWithFiles1,"folder/a.foo");
        assertNotNull(f1);
        assertEquals(MIME, f1.getMIMEType());
        FileObject f2 = FileUtil.createData(srcRootWithFiles1,"folder/b.foo");
        assertNotNull(f2);
        assertEquals(MIME, f2.getMIMEType());
        customFiles = new URL[] {f1.toURL(), f2.toURL()};

        FileUtil.setMIMEType("emb", EMIME);
        f3 = FileUtil.createData(srcRootWithFiles1,"folder/a.emb");
        assertNotNull(f3);
        assertEquals(EMIME, f3.getMIMEType());
        FileObject f4 = FileUtil.createData(srcRootWithFiles1,"folder/b.emb");
        assertNotNull(f4);
        assertEquals(EMIME, f4.getMIMEType());
        embeddedFiles = new URL[] {f3.toURL(), f4.toURL()};


        waitForRepositoryUpdaterInit();
    }

    @Override
    protected void tearDown() throws Exception {
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
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

    protected final void globalPathRegistry_register(String id, ClassPath [] classpaths) {
        Map<ClassPath,Void> map = registeredClasspaths.get(id);
        if (map == null) {
            map = new IdentityHashMap<>();
            registeredClasspaths.put(id, map);
        }
        for (ClassPath cp :  classpaths) {
            map.put(cp,null);
        }
        GlobalPathRegistry.getDefault().register(id, classpaths);
    }

    protected final void globalPathRegistry_unregister(String id, ClassPath [] classpaths) {
        GlobalPathRegistry.getDefault().unregister(id, classpaths);
        final Map<ClassPath,Void> map = registeredClasspaths.get(id);
        if (map != null) {
            map.keySet().removeAll(Arrays.asList(classpaths));
        }
    }

    public static void waitForRepositoryUpdaterInit() throws Exception {
        RepositoryUpdater.getDefault().ignoreIndexerCacheEvents(true);
        RepositoryUpdater.getDefault().start(true);
        RepositoryUpdater.State state;
        long time = System.currentTimeMillis();
        do {
            if (System.currentTimeMillis() - time > 60000) {
                fail("Waiting for RepositoryUpdater.init() timed out");
            }
            Thread.sleep(100);
            state = RepositoryUpdater.getDefault().getState();
        } while (state != RepositoryUpdater.State.ACTIVE);

        // clear all data from previous test runs
        RepositoryUpdater.getDefault().getScannedBinaries().clear();
        RepositoryUpdater.getDefault().getScannedSources().clear();
        RepositoryUpdater.getDefault().getScannedUnknowns().clear();
    }

    @RandomlyFails
    public void testPathAddedRemovedChanged () throws Exception {
        //Empty regs test
        RepositoryUpdater ru = RepositoryUpdater.getDefault();
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedUnknowns().size());

        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);

        //Testing classpath registration
        MutableClassPathImplementation mcpi1 = new MutableClassPathImplementation ();
        mcpi1.addResource(this.srcRoot1);
        ClassPath cp1 = ClassPathFactory.createClassPath(mcpi1);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRoot1.toURL(), handler.getSources().get(0));

        //Nothing should be scanned if the same cp is registered again
        handler.reset();
        final MutableClassPathImplementation mcpi1clone = new MutableClassPathImplementation();
        mcpi1clone.addResource(this.srcRoot1);
        ClassPath cp1clone = ClassPathFactory.createClassPath(mcpi1clone);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1clone});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(0, handler.getSources().size());

        //Nothing should be scanned if the cp is unregistered
        handler.reset();
        globalPathRegistry_unregister(SOURCES,new ClassPath[]{cp1clone});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(0, handler.getSources().size());

        //Nothing should be scanned after classpath remove
        handler.reset();
        globalPathRegistry_unregister(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(0, handler.getSources().size());


        //Testing changes in registered classpath - add cp root
        handler.reset();
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRoot1.toURL(), handler.getSources().get(0));

        handler.reset();
        mcpi1.addResource(srcRoot2);
        assertTrue(handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRoot2.toURL(), handler.getSources().get(0));

        //Testing changes in registered classpath - remove cp root
        handler.reset();
        mcpi1.removeResource(srcRoot1);
        assertTrue(handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(0, handler.getSources().size());

        //Testing adding new ClassPath
        handler.reset();
        MutableClassPathImplementation mcpi2 = new MutableClassPathImplementation ();
        mcpi2.addResource(srcRoot1);
        ClassPath cp2 = ClassPathFactory.createClassPath(mcpi2);
        globalPathRegistry_register (SOURCES, new ClassPath[] {cp2});
        assertTrue(handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRoot1.toURL(), handler.getSources().get(0));

        //Testing changes in newly registered classpath - add cp root
        handler.reset();
        mcpi2.addResource(srcRoot3);
        assertTrue(handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRoot3.toURL(), handler.getSources().get(0));

        //Testing removing ClassPath
        handler.reset();
        globalPathRegistry_unregister(SOURCES,new ClassPath[] {cp2});
        assertTrue(handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(0, handler.getSources().size());

        //Testing registering classpath with SFBQ - register PLATFROM
        handler.reset();
        ClassPath cp3 = ClassPathSupport.createClassPath(new FileObject[] {bootRoot1,bootRoot2});
        globalPathRegistry_register(PLATFORM,new ClassPath[] {cp3});
        assertTrue(handler.await());
        assertEquals(1, handler.getBinaries().size());
        assertEquals(this.bootRoot2.toURL(), handler.getBinaries().iterator().next());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.bootSrc1.toURL(), handler.getSources().get(0));

        //Testing registering classpath with SFBQ - register LIBS
        handler.reset();
        MutableClassPathImplementation mcpi4 = new MutableClassPathImplementation ();
        mcpi4.addResource (compRoot1);
        ClassPath cp4 = ClassPathFactory.createClassPath(mcpi4);
        globalPathRegistry_register(LIBS,new ClassPath[] {cp4});
        assertTrue(handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.compSrc1.toURL(), handler.getSources().get(0));

        //Testing registering classpath with SFBQ - add into LIBS
        handler.reset();
        mcpi4.addResource(compRoot2);
        assertTrue(handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.compSrc2.toURL(), handler.getSources().get(0));


        //Testing registering classpath with SFBQ - remove from LIBS
        handler.reset();
        mcpi4.removeResource(compRoot1);
        assertTrue(handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(0, handler.getSources().size());

        //Testing registering classpath with SFBQ - unregister PLATFORM
        handler.reset();
        globalPathRegistry_unregister(PLATFORM,new ClassPath[] {cp3});
        assertTrue(handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(0, handler.getSources().size());

        //Testing listening on SFBQ.Results - bind source
        handler.reset();
        SFBQImpl.register(compRoot2,compSrc1);
        assertTrue(handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.compSrc1.toURL(), handler.getSources().get(0));

        //Testing listening on SFBQ.Results - rebind (change) source
        handler.reset();
        SFBQImpl.register(compRoot2,compSrc2);
        assertTrue(handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.compSrc2.toURL(), handler.getSources().get(0));
    }

    @RandomlyFails
    public void testIndexers () throws Exception {
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        indexerFactory.indexer.setExpectedFile(customFiles, new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(embeddedFiles, new URL[0], new URL[0]);
        MutableClassPathImplementation mcpi1 = new MutableClassPathImplementation ();
        mcpi1.addResource(this.srcRootWithFiles1);
        ClassPath cp1 = ClassPathFactory.createClassPath(mcpi1);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRootWithFiles1.toURL(), handler.getSources().get(0));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());

        handler.reset();
        globalPathRegistry_unregister(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());

        handler.reset();
        indexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[0],new URL[0], new URL[0]);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRootWithFiles1.toURL(), handler.getSources().get(0));
        assertEquals(0, indexerFactory.indexer.getIndexCount());
        assertEquals(0, eindexerFactory.indexer.getIndexCount());

        handler.reset();
        globalPathRegistry_unregister(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());

        Thread.sleep(5000); //Wait for file system time
        handler.reset();
        indexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[0],new URL[0], new URL[0]);
        File file = org.openide.util.Utilities.toFile(embeddedFiles[0].toURI());
        file.setLastModified(System.currentTimeMillis());
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRootWithFiles1.toURL(), handler.getSources().get(0));
        assertEquals(0, indexerFactory.indexer.getIndexCount());
        assertEquals(1, eindexerFactory.indexer.getIndexCount());

        handler.reset();
        globalPathRegistry_unregister(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());

        Thread.sleep(5000); //Wait for file system time
        handler.reset();
        indexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[0],new URL[0], new URL[0]);
        file = org.openide.util.Utilities.toFile(embeddedFiles[0].toURI());
        file.setLastModified(System.currentTimeMillis());
        file = org.openide.util.Utilities.toFile(embeddedFiles[1].toURI());
        file.delete();
        srcRootWithFiles1.getFileSystem().refresh(true);

        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRootWithFiles1.toURL(), handler.getSources().get(0));
        assertEquals(0, indexerFactory.indexer.getIndexCount());
        assertEquals(1, eindexerFactory.indexer.getIndexCount());
        assertEquals(0, eindexerFactory.indexer.expectedDeleted.size());
        assertEquals(0, eindexerFactory.indexer.expectedDirty.size());
    }

    @RandomlyFails
    public void testBinaryIndexers() throws Exception {
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);

        binIndexerFactory.indexer.setExpectedRoots(bootRoot2.toURL(), bootRoot3.toURL());
//        jarIndexerFactory.indexer.setExpectedRoots(bootRoot3.getURL());
        ClassPath cp = ClassPathSupport.createClassPath(new FileObject[] {bootRoot2, bootRoot3});
        globalPathRegistry_register(PLATFORM,new ClassPath[] {cp});
        assertTrue(handler.await());
        assertEquals(2, handler.getBinaries().size());
//        assertEquals(1, jarIndexerFactory.indexer.getCount());
        assertEquals(2, binIndexerFactory.indexer.getCount());
    }

    @RandomlyFails
    public void testBinaryDeletedAdded() throws Exception {
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);

        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        final FileObject[] jar2Delete = new FileObject[] {jarFile.copy(wd, "test", "jar")};
        ClassPath cp = ClassPathSupport.createClassPath(new FileObject[] {FileUtil.getArchiveRoot(jar2Delete[0])});

        globalPathRegistry_register(PLATFORM,new ClassPath[] {cp});
        assertTrue(handler.await());
        assertEquals(1, handler.getBinaries().size());

        handler.reset(TestHandler.Type.BINARY);

        final long timeStamp = jar2Delete[0].lastModified().getTime();

        jar2Delete[0].delete();
        handler.await();

        binIndexerFactory.indexer.indexedAllFilesIndexing.clear();
        handler.reset(TestHandler.Type.BINARY);
        FileUtil.runAtomicAction(new Runnable(){
            @Override
            public void run() {
                try {
                    jar2Delete[0] = jarFile.copy(wd, "test", "jar");
                    FileUtil.toFile(jar2Delete[0]).setLastModified(timeStamp);
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            }
        });
        assertTrue(handler.await());
        assertTrue(binIndexerFactory.indexer.indexedAllFilesIndexing.toString(), binIndexerFactory.indexer.indexedAllFilesIndexing.contains(FileUtil.getArchiveRoot(jar2Delete[0]).toURL()));

    }

    @RandomlyFails
    public void testFileChanges() throws Exception {
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        indexerFactory.indexer.setExpectedFile(customFiles, new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(embeddedFiles, new URL[0], new URL[0]);
        MutableClassPathImplementation mcpi1 = new MutableClassPathImplementation ();
        mcpi1.addResource(this.srcRootWithFiles1);
        ClassPath cp1 = ClassPathFactory.createClassPath(mcpi1);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRootWithFiles1.toURL(), handler.getSources().get(0));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());

        //Test modifications
        indexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[]{f3.toURL()}, new URL[0], new URL[0]);
        try (OutputStream out = f3.getOutputStream()) {
            out.write(0);
        }
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
        assertEquals(1, eindexerFactory.indexer.indexCounter);

        //Test file creation
        File f = FileUtil.toFile(f3);
        final File container = f.getParentFile();
        File newFile = new File (container,"c.emb");
        indexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[]{org.openide.util.Utilities.toURI(newFile).toURL()}, new URL[0], new URL[0]);
        assertNotNull(FileUtil.createData(newFile));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
        assertEquals(1, eindexerFactory.indexer.indexCounter);

        //Test folder creation
        final FileObject containerFo = FileUtil.toFileObject(container);
        containerFo.getChildren();
        File newFolder = new File (container,"subfolder");
        newFile = new File (newFolder,"d.emb");
        File newFile2 = new File (newFolder,"e.emb");
        indexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[]{org.openide.util.Utilities.toURI(newFile).toURL(), org.openide.util.Utilities.toURI(newFile2).toURL()}, new URL[0], new URL[0]);
        newFolder.mkdirs();
        touchFile (newFile);
        touchFile (newFile2);
        assertEquals(2,newFolder.list().length);
        FileUtil.toFileObject(newFolder);   //Refresh fs
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
        assertEquals(2, eindexerFactory.indexer.indexCounter);

        //Test file deleted
        handler.reset(TestHandler.Type.DELETE);
        indexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[0], new URL[]{f3.toURL()}, new URL[0]);
        f3.delete();
        assertTrue (handler.await());
        assertTrue(indexerFactory.indexer.awaitDeleted(TIME));
        assertTrue(eindexerFactory.indexer.awaitDeleted());
        assertEquals(0, eindexerFactory.indexer.indexCounter);
        assertEquals(0,eindexerFactory.indexer.expectedDeleted.size());
        assertEquals(0, eindexerFactory.indexer.expectedDirty.size());

        // test file created and immediatelly deleted in an AtomicAction
        handler.reset(TestHandler.Type.DELETE);
        containerFo.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
                File newFile = new File (container, "xyz.emb");
                indexerFactory.indexer.setExpectedFile(new URL[0], new URL[] {BaseUtilities.toURI(newFile).toURL()}, new URL[0]);
                eindexerFactory.indexer.setExpectedFile(new URL[0], new URL[] {BaseUtilities.toURI(newFile).toURL()}, new URL[0]);

                FileObject newFileFo = FileUtil.createData(newFile);
                assertNotNull(newFileFo);
                newFileFo.delete();
                assertFalse(newFileFo.isValid());
            }
        });
        assertTrue(indexerFactory.indexer.awaitDeleted(TIME));
        assertTrue(eindexerFactory.indexer.awaitDeleted());
        assertEquals(0, indexerFactory.indexer.getIndexCount());
        assertEquals(0, indexerFactory.indexer.getDirtyCount());
        assertEquals(1, indexerFactory.indexer.getDeletedCount());
        assertEquals(0, eindexerFactory.indexer.getIndexCount());
        assertEquals(0, eindexerFactory.indexer.getDirtyCount());
        assertEquals(1, eindexerFactory.indexer.getDeletedCount());
    }

    @RandomlyFails
    public void testFileRenamed() throws Exception {
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        MutableClassPathImplementation mcpi1 = new MutableClassPathImplementation ();
        mcpi1.addResource(this.srcRootWithFiles1);
        ClassPath cp1 = ClassPathFactory.createClassPath(mcpi1);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());

        final URL newURL = new URL(f3.getParent().toURL(), "newName.emb");
        final URL oldURL = f3.toURL();

        indexerFactory.indexer.setExpectedFile(new URL[0], new URL[]{oldURL}, new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[]{newURL}, new URL[]{oldURL}, new URL[0]);
        FileLock lock = f3.lock();
        try {
            FileObject f = f3.move(lock, f3.getParent(), "newName", "emb");
        } finally {
            lock.releaseLock();
        }
        assertTrue(indexerFactory.indexer.awaitDeleted(TIME));
        assertTrue(eindexerFactory.indexer.awaitDeleted());
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
        assertEquals(1, eindexerFactory.indexer.getIndexCount());
        assertEquals(1, eindexerFactory.indexer.getDeletedCount());
        assertEquals(0, eindexerFactory.indexer.getDirtyCount());
        assertEquals(0, indexerFactory.indexer.getIndexCount());
        assertEquals(1, indexerFactory.indexer.getDeletedCount());
        assertEquals(0, indexerFactory.indexer.getDirtyCount());
    }

    public void testFileUpdateRename() throws Exception {
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        MutableClassPathImplementation mcpi1 = new MutableClassPathImplementation ();
        mcpi1.addResource(this.srcRootWithFiles1);
        ClassPath cp1 = ClassPathFactory.createClassPath(mcpi1);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());

        final URL newURL = new URL(f1.getParent().toURL(), "newName.foo");
        final URL oldURL = f1.toURL();

        indexerFactory.indexer.setExpectedFile(new URL[0], new URL[]{oldURL}, new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[]{newURL}, new URL[]{oldURL}, new URL[0]);
        final CountDownLatch waitFor = new CountDownLatch(1);
        IndexingController.getDefault().enterProtectedMode();
        try {
            f1.getOutputStream().close();
            FileLock lock = f1.lock();
            try {
                FileObject f = f1.move(lock, f1.getParent(), "newName", "foo");
            } finally {
                lock.releaseLock();
            }
        } finally {
            IndexingController.getDefault().exitProtectedMode(new Runnable() {
                @Override public void run() {
                    waitFor.countDown();
                }
            });
        }
        assertTrue(waitFor.await(5, TimeUnit.SECONDS));
        assertEquals("INDEX: " + newURL + "\nDELETE: " + oldURL + "\nINDEX: " + newURL + "\n", indexerFactory.indexer.log.toString());
    }

    private void touchFile (final File file) throws IOException {
        OutputStream out = new FileOutputStream (file);
        out.close();
    }

    public void testFileListWork164622() throws FileStateInvalidException {
        final RepositoryUpdater ru = RepositoryUpdater.getDefault();
        RepositoryUpdater.FileListWork flw1 = new RepositoryUpdater.FileListWork(ru.getScannedRoots2Dependencies(),srcRootWithFiles1.toURL(), false, false, true, false, SuspendSupport.NOP, null);
        RepositoryUpdater.FileListWork flw2 = new RepositoryUpdater.FileListWork(ru.getScannedRoots2Dependencies(),srcRootWithFiles1.toURL(), false, false, true, false, SuspendSupport.NOP, null);
        assertTrue("The flw2 job was not absorbed", flw1.absorb(flw2));

        FileObject [] children = srcRootWithFiles1.getChildren();
        assertTrue(children.length > 0);
        RepositoryUpdater.FileListWork flw3 = new RepositoryUpdater.FileListWork(ru.getScannedRoots2Dependencies(),srcRootWithFiles1.toURL(), Collections.singleton(children[0]), false, false, true, false, true, SuspendSupport.NOP, null);
        assertTrue("The flw3 job was not absorbed", flw1.absorb(flw3));

        RepositoryUpdater.FileListWork flw4 = new RepositoryUpdater.FileListWork(ru.getScannedRoots2Dependencies(),srcRoot1.toURL(), false, false, true, false, SuspendSupport.NOP, null);
        assertFalse("The flw4 job should not have been absorbed", flw1.absorb(flw4));
    }

    public void testScanStartScanFinishedCalled() throws Exception {
        indexerFactory.scanStartedFor.clear();
        indexerFactory.scanFinishedFor.clear();
        eindexerFactory.scanStartedFor.clear();
        eindexerFactory.scanFinishedFor.clear();
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        indexerFactory.indexer.setExpectedFile(customFiles, new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(embeddedFiles, new URL[0], new URL[0]);
        MutableClassPathImplementation mcpi1 = new MutableClassPathImplementation ();
        mcpi1.addResource(this.srcRootWithFiles1);
        ClassPath cp1 = ClassPathFactory.createClassPath(mcpi1);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRootWithFiles1.toURL(), handler.getSources().get(0));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
        assertEquals(1, indexerFactory.scanStartedFor.size());
        assertEquals(srcRootWithFiles1.toURL(), indexerFactory.scanStartedFor.get(0));
        assertEquals(1, indexerFactory.scanFinishedFor.size());
        assertEquals(srcRootWithFiles1.toURL(), indexerFactory.scanFinishedFor.get(0));
        assertEquals(1, eindexerFactory.scanStartedFor.size());
        assertEquals(srcRootWithFiles1.toURL(), eindexerFactory.scanStartedFor.get(0));
        assertEquals(1, eindexerFactory.scanFinishedFor.size());
        assertEquals(srcRootWithFiles1.toURL(), eindexerFactory.scanFinishedFor.get(0));


        indexerFactory.scanStartedFor.clear();
        indexerFactory.scanFinishedFor.clear();
        eindexerFactory.scanStartedFor.clear();
        eindexerFactory.scanFinishedFor.clear();
        handler.reset();
        indexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[0],new URL[0], new URL[0]);
        mcpi1.addResource(srcRoot1);
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRoot1.toURL(), handler.getSources().get(0));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
        assertEquals(1, indexerFactory.scanStartedFor.size());
        assertEquals(srcRoot1.toURL(), indexerFactory.scanStartedFor.get(0));
        assertEquals(1, indexerFactory.scanFinishedFor.size());
        assertEquals(srcRoot1.toURL(), indexerFactory.scanFinishedFor.get(0));
        assertEquals(1, eindexerFactory.scanStartedFor.size());
        assertEquals(srcRoot1.toURL(), eindexerFactory.scanStartedFor.get(0));
        assertEquals(1, eindexerFactory.scanFinishedFor.size());
        assertEquals(srcRoot1.toURL(), eindexerFactory.scanFinishedFor.get(0));



        indexerFactory.scanStartedFor.clear();
        indexerFactory.scanFinishedFor.clear();
        eindexerFactory.scanStartedFor.clear();
        eindexerFactory.scanFinishedFor.clear();
        handler.reset();
        indexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[0],new URL[0], new URL[0]);
        globalPathRegistry_unregister(SOURCES,new ClassPath[]{cp1});
        //Give RU a time for refresh - nothing to wait for
        Thread.sleep(2000);
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(0, handler.getSources().size());
        assertEquals(0, indexerFactory.indexer.getIndexCount());
        assertEquals(0, eindexerFactory.indexer.getIndexCount());
        assertEquals(0, indexerFactory.scanStartedFor.size());
        assertEquals(0, indexerFactory.scanFinishedFor.size());
        assertEquals(0, eindexerFactory.scanStartedFor.size());
        assertEquals(0, eindexerFactory.scanFinishedFor.size());

        handler.reset();
        indexerFactory.scanStartedFor.clear();
        indexerFactory.scanFinishedFor.clear();
        eindexerFactory.scanStartedFor.clear();
        eindexerFactory.scanFinishedFor.clear();
        indexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(2, handler.getSources().size());
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
        assertEquals(2, indexerFactory.scanStartedFor.size());
        assertEquals(new URL[] {srcRoot1.toURL(), srcRootWithFiles1.toURL()}, indexerFactory.scanStartedFor);
        assertEquals(2, indexerFactory.scanFinishedFor.size());
        assertEquals(new URL[] {srcRoot1.toURL(), srcRootWithFiles1.toURL()}, indexerFactory.scanFinishedFor);
        assertEquals(2, eindexerFactory.scanStartedFor.size());
        assertEquals(new URL[] {srcRoot1.toURL(), srcRootWithFiles1.toURL()}, eindexerFactory.scanStartedFor);
        assertEquals(2, eindexerFactory.scanFinishedFor.size());
        assertEquals(new URL[] {srcRoot1.toURL(), srcRootWithFiles1.toURL()}, eindexerFactory.scanFinishedFor);
    }

    public void testBinaryScanStartFinishedCalled() throws Exception {
        binIndexerFactory.scanStartedFor.clear();
        binIndexerFactory.scanFinishedFor.clear();
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        binIndexerFactory.indexer.setExpectedRoots(bootRoot2.toURL());
        MutableClassPathImplementation mcpi1 = new MutableClassPathImplementation ();
        mcpi1.addResource(bootRoot2);
        ClassPath cp = ClassPathFactory.createClassPath(mcpi1);
        globalPathRegistry_register(PLATFORM,new ClassPath[] {cp});
        assertTrue (handler.await());
        assertEquals(1, handler.getBinaries().size());
        assertEquals(0, handler.getSources().size());
        assertEquals(bootRoot2.toURL(), handler.getBinaries().iterator().next());
        assertTrue(binIndexerFactory.indexer.await());
        assertEquals(1, binIndexerFactory.scanStartedFor.size());
        assertEquals(bootRoot2.toURL(), binIndexerFactory.scanStartedFor.get(0));
        assertEquals(1, binIndexerFactory.scanFinishedFor.size());
        assertEquals(bootRoot2.toURL(), binIndexerFactory.scanFinishedFor.get(0));

        binIndexerFactory.scanStartedFor.clear();
        binIndexerFactory.scanFinishedFor.clear();
        handler.reset();
        binIndexerFactory.indexer.setExpectedRoots(bootRoot3.toURL());
        mcpi1.addResource(bootRoot3);
        assertTrue (handler.await());
        assertEquals(1, handler.getBinaries().size());
        assertEquals(0, handler.getSources().size());
        assertEquals(bootRoot3.toURL(), handler.getBinaries().iterator().next());
        assertTrue(binIndexerFactory.indexer.await());
        assertEquals(1, binIndexerFactory.scanStartedFor.size());
        assertEquals(bootRoot3.toURL(), binIndexerFactory.scanStartedFor.get(0));
        assertEquals(1, binIndexerFactory.scanFinishedFor.size());
        assertEquals(bootRoot3.toURL(), binIndexerFactory.scanFinishedFor.get(0));

        binIndexerFactory.scanStartedFor.clear();
        binIndexerFactory.scanFinishedFor.clear();
        handler.reset();
        binIndexerFactory.indexer.setExpectedRoots(new URL[0]);
        globalPathRegistry_unregister(PLATFORM,new ClassPath[]{cp});
        //Give RU a time for refresh - nothing to wait for
        Thread.sleep(2000);
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(0, handler.getSources().size());
        assertEquals(0, binIndexerFactory.indexer.getCount());
        assertEquals(0, binIndexerFactory.scanStartedFor.size());
        assertEquals(0, binIndexerFactory.scanFinishedFor.size());

        binIndexerFactory.scanStartedFor.clear();
        binIndexerFactory.scanFinishedFor.clear();
        handler.reset();
        binIndexerFactory.indexer.setExpectedRoots(bootRoot2.toURL(), bootRoot3.toURL());
        globalPathRegistry_register(PLATFORM,new ClassPath[]{cp});
        assertTrue (handler.await());
        assertEquals(2, handler.getBinaries().size());
        assertEquals(0, handler.getSources().size());
        assertTrue(binIndexerFactory.indexer.await());
        assertEquals(2, binIndexerFactory.scanStartedFor.size());
        assertEquals(new URL[] {bootRoot2.toURL(), bootRoot3.toURL()}, binIndexerFactory.scanStartedFor);
        assertEquals(2, binIndexerFactory.scanFinishedFor.size());
        assertEquals(new URL[] {bootRoot2.toURL(), bootRoot3.toURL()}, binIndexerFactory.scanFinishedFor);
    }

    //where
    private void assertEquals(final URL[] expected, final Collection<URL> data) throws AssertionError {
        assertEquals(expected.length, data.size());
        final Set<URL> expectedSet = new HashSet<>(Arrays.asList(expected));
        final Set<URL> dataSet = new HashSet<>(data);
        assertEquals(expectedSet.size(), dataSet.size());
        for (URL url : dataSet) {
            assertTrue(expectedSet.remove(url));
        }
    }

    public void testIssue171719() throws Exception {
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        indexerFactory.indexer.setExpectedFile(customFiles, new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(embeddedFiles, new URL[0], new URL[0]);
        MutableClassPathImplementation mcpi1 = new MutableClassPathImplementation ();
        mcpi1.addResource(this.srcRootWithFiles1);
        ClassPath cp1 = ClassPathFactory.createClassPath(mcpi1);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRootWithFiles1.toURL(), handler.getSources().get(0));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());

        File root = FileUtil.toFile(srcRootWithFiles1);
        File fdf = new File (root, "direct.emb");   //NOI18N
        eindexerFactory.indexer.setExpectedFile(new URL[]{org.openide.util.Utilities.toURI(fdf).toURL()}, new URL[0], new URL[0]);
        FileObject df = FileUtil.createData(fdf);
        assertNotNull(df);
        assertEquals(EMIME, df.getMIMEType());
        eindexerFactory.indexer.awaitIndex();

        File newfdf = new File (root, "new_direct.emb");   //NOI18N
        eindexerFactory.indexer.setExpectedFile(new URL[]{org.openide.util.Utilities.toURI(newfdf).toURL()}, new URL[]{org.openide.util.Utilities.toURI(fdf).toURL()}, new URL[0]);
        FileLock lock = df.lock();
        try {
            df.rename(lock, "new_direct", "emb");
        } finally {
            lock.releaseLock();
        }
        eindexerFactory.indexer.awaitIndex();
        eindexerFactory.indexer.awaitDeleted();
        assertFalse(eindexerFactory.indexer.broken);
    }

    public void testFileListWorkVsRefreshWork() throws IOException {
        File root1 = new File(getWorkDir(), "root1");
        {
        RepositoryUpdater.FileListWork flw = new RepositoryUpdater.FileListWork(Collections.<URL, List<URL>>emptyMap(), root1.toURL(), false, false, false, true, SuspendSupport.NOP, null);
        RepositoryUpdater.RefreshWork rw = new RepositoryUpdater.RefreshWork(
                Collections.<URL, List<URL>>emptyMap(),
                Collections.<URL,List<URL>>emptyMap(),
                Collections.<URL,List<URL>>emptyMap(),
                Collections.<URL>emptySet(),
                Collections.<URL>emptySet(),
                false,
                false,
                null,
                new RepositoryUpdater.FSRefreshInterceptor(),
                SuspendSupport.NOP,
                null);
        assertTrue("RefreshWork didn't absorb FileListWork", rw.absorb(flw));
        }
        {
        RepositoryUpdater.FileListWork flw = new RepositoryUpdater.FileListWork(Collections.<URL, List<URL>>emptyMap(), root1.toURL(), false, false, true, true, SuspendSupport.NOP, null);
        RepositoryUpdater.RefreshWork rw = new RepositoryUpdater.RefreshWork(
                Collections.<URL, List<URL>>emptyMap(),
                Collections.<URL,List<URL>>emptyMap(),
                Collections.<URL,List<URL>>emptyMap(),
                Collections.<URL>emptySet(),
                Collections.<URL>emptySet(),
                true,
                false,
                null,
                new RepositoryUpdater.FSRefreshInterceptor(),
                SuspendSupport.NOP,
                null);
        assertTrue("RefreshWork didn't absorb FileListWork", rw.absorb(flw));
        }
        {
        RepositoryUpdater.FileListWork flw = new RepositoryUpdater.FileListWork(Collections.<URL, List<URL>>emptyMap(), root1.toURL(), false, false, false, true, SuspendSupport.NOP, null);
        RepositoryUpdater.RefreshWork rw = new RepositoryUpdater.RefreshWork(
                Collections.<URL, List<URL>>emptyMap(),
                Collections.<URL,List<URL>>emptyMap(),
                Collections.<URL,List<URL>>emptyMap(),
                Collections.<URL>emptySet(),
                Collections.<URL>emptySet(),
                true,
                false,
                null,
                new RepositoryUpdater.FSRefreshInterceptor(),
                SuspendSupport.NOP,
                null);
        assertTrue("RefreshWork didn't absorb FileListWork", rw.absorb(flw));
        }
        {
        RepositoryUpdater.FileListWork flw = new RepositoryUpdater.FileListWork(Collections.<URL, List<URL>>emptyMap(), root1.toURL(), false, false, true, true, SuspendSupport.NOP, null);
        RepositoryUpdater.RefreshWork rw = new RepositoryUpdater.RefreshWork(
                Collections.<URL, List<URL>>emptyMap(),
                Collections.<URL,List<URL>>emptyMap(),
                Collections.<URL,List<URL>>emptyMap(),
                Collections.<URL>emptySet(),
                Collections.<URL>emptySet(),
                false,
                false,
                null,
                new RepositoryUpdater.FSRefreshInterceptor(),
                SuspendSupport.NOP,
                null);
        assertTrue("RefreshWork didn't absorb FileListWork", rw.absorb(flw));
        }
    }

    public void testRefreshWork() throws IOException {
        File root1 = new File(getWorkDir(), "root1");
        File root2 = new File(getWorkDir(), "root2");
        {
        RepositoryUpdater.RefreshWork rw1 = new RepositoryUpdater.RefreshWork(
                Collections.<URL, List<URL>>emptyMap(),
                Collections.<URL,List<URL>>emptyMap(),
                Collections.<URL,List<URL>>emptyMap(),
                Collections.<URL>emptySet(),
                Collections.<URL>emptySet(),
                false,
                false,
                Collections.singleton(root1),
                new RepositoryUpdater.FSRefreshInterceptor(),
                SuspendSupport.NOP,
                null);
        RepositoryUpdater.RefreshWork rw2 = new RepositoryUpdater.RefreshWork(
                Collections.<URL, List<URL>>emptyMap(),
                Collections.<URL,List<URL>>emptyMap(),
                Collections.<URL, List<URL>>emptyMap(),
                Collections.<URL>emptySet(),
                Collections.<URL>emptySet(),
                false,
                false,
                Collections.singleton(root2),
                new RepositoryUpdater.FSRefreshInterceptor(),
                SuspendSupport.NOP,
                null);
        assertFalse("RefreshWork should not be cancelled by other RefreshWork", rw1.isCancelledBy(rw2, new ArrayList<RepositoryUpdater.Work>()));
        assertTrue("RefreshWork should absorb other RefreshWork", rw1.absorb(rw2));
        }
    }

    @RandomlyFails
    public void testMissedChanges() throws Exception {
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        final CountDownLatch pauseRU = new CountDownLatch(1);
        indexerFactory.indexer.setExpectedFile(customFiles, new URL[0], new URL[0]);
        indexerFactory.indexer.setPostCallBack(new Runnable() {
            @Override
            public void run () {
                try {
                    try (OutputStream out = f1.getOutputStream()) {
                        out.write("Buena Suerte".getBytes());
                    }
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
                indexerFactory.indexer.setPreCallBack(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            pauseRU.await(TIME, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException ie) {
                            Exceptions.printStackTrace(ie);
                        }
                    }
                });
            }
        });
        eindexerFactory.indexer.setExpectedFile(embeddedFiles, new URL[0], new URL[0]);

        //Run RootsWork which does file modification of f1 and FileListWork shold follow
        MutableClassPathImplementation mcpi1 = new MutableClassPathImplementation ();
        mcpi1.addResource(srcRootWithFiles1);
        ClassPath cp1 = ClassPathFactory.createClassPath(mcpi1);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});

        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(srcRootWithFiles1.toURL(), handler.getSources().get(0));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());

        //Assert FileListWork
        indexerFactory.indexer.setExpectedFile(new URL[]{f1.toURL()}, new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        pauseRU.countDown();    //Unpause RU
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
    }

    public void testFileChangedInEditorReparsedOnce191885() throws Exception {
        //Prepare
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        handler.reset(TestHandler.Type.ROOTS_WORK_FINISHED);
        MutableClassPathImplementation mcpi1 = new MutableClassPathImplementation ();
        mcpi1.addResource(this.srcRootWithFiles1);
        ClassPath cp1 = ClassPathFactory.createClassPath(mcpi1);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        final Source src = Source.create(f1);
        assertNotNull(src);
        assertFalse ("Created source should be valid", SourceAccessor.getINSTANCE().testFlag(src, SourceFlags.INVALID));
        RepositoryUpdater.getDefault().waitUntilFinished(-1);
        RepositoryUpdater.unitTestActiveSource = src;
        TransientUpdateSupport.setTransientUpdate(true);
        try {
            RepositoryUpdater.getDefault().enforcedFileListUpdate(this.srcRootWithFiles1.toURL(),Collections.singleton(f1.toURL()));
            assertFalse("Active shource should not be invalidated",SourceAccessor.getINSTANCE().testFlag(src, SourceFlags.INVALID));
        } finally {
            RepositoryUpdater.unitTestActiveSource=null;
            TransientUpdateSupport.setTransientUpdate(false);
        }
        IndexingManager.getDefault().refreshIndexAndWait(this.srcRootWithFiles1.toURL(),
                    Collections.singleton(f1.toURL()));
        assertTrue("Non active shource should be invalidated",SourceAccessor.getINSTANCE().testFlag(src, SourceFlags.INVALID));
    }

    public void testFilesScannedAfterRenameOfFolder193243() throws Exception {
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        final FileObject testFo = FileUtil.createData(this.srcRootWithFiles1, "rename/A.foo");
        final MutableClassPathImplementation mcpi1 = new MutableClassPathImplementation ();
        mcpi1.addResource(this.srcRootWithFiles1);
        final ClassPath cp1 = ClassPathFactory.createClassPath(mcpi1);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        indexerFactory.indexer.setExpectedFile(new URL[]{new URL(this.srcRootWithFiles1.toURL()+"renamed/A.foo")}, new URL[]{testFo.toURL()}, new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[0], new URL[]{testFo.toURL()}, new URL[0]);
        final FileObject parent = testFo.getParent();
        final FileLock lock = parent.lock();
        try {
            parent.rename(lock, "renamed", null);
        } finally {
            lock.releaseLock();
        }
        assertTrue(indexerFactory.indexer.awaitDeleted(TIME));
        assertTrue(eindexerFactory.indexer.awaitDeleted());
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
        assertEquals(0, eindexerFactory.indexer.getIndexCount());
        assertEquals(1, eindexerFactory.indexer.getDeletedCount());
        assertEquals(0, eindexerFactory.indexer.getDirtyCount());
        assertEquals(1, indexerFactory.indexer.getIndexCount());
        assertEquals(1, indexerFactory.indexer.getDeletedCount());
        assertEquals(0, indexerFactory.indexer.getDirtyCount());
    }

    public void testFilesScannedAferSourceRootCreatedDeleted() throws Exception {
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        final File srcRoot1File = FileUtil.toFile(srcRoot1);
        final ClassPath cp1 = ClassPathSupport.createClassPath(FileUtil.urlForArchiveOrDir(srcRoot1File));
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        indexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        assertTrue(indexerFactory.indexer.awaitDeleted(TIME));
        assertTrue(eindexerFactory.indexer.awaitDeleted());
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());

        indexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        srcRoot1.delete();

        final File a = new File(srcRoot1File,"folder/a.foo");
        final File b = new File(srcRoot1File,"folder/b.emb");
        indexerFactory.indexer.setExpectedFile(new URL[]{org.openide.util.Utilities.toURI(a).toURL()}, new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[]{org.openide.util.Utilities.toURI(b).toURL()}, new URL[0], new URL[0]);
        FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
                FileUtil.createFolder(srcRoot1File);
                FileUtil.createData(a);
                FileUtil.createData(b);
            }
        });
        assertTrue(indexerFactory.indexer.awaitDeleted(TIME));
        assertTrue(eindexerFactory.indexer.awaitDeleted());
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
    }

    public void testIncludesChanges() throws Exception {
        //Prepare
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        MutableFilteringResourceImpl r = new MutableFilteringResourceImpl(srcRootWithFiles1.toURL());
        ClassPath cp1 = ClassPathSupport.createClassPath(Collections.singletonList(r));
        ClassPathProviderImpl.register(srcRootWithFiles1, SOURCES, cp1);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());

        //exclude a file:
        indexerFactory.indexer.setExpectedFile(new URL[0], new URL[] {f1.toURL()}, new URL[0]);
        r.setExcludes(".*/a\\.foo");

        indexerFactory.indexer.awaitDeleted(TIME);
        indexerFactory.indexer.awaitIndex(TIME);

        assertEquals(1, indexerFactory.indexer.deletedCounter);
        assertEquals(Collections.emptySet(), indexerFactory.indexer.expectedDeleted);
        assertEquals(0, indexerFactory.indexer.indexCounter);

        //include the file back:
        indexerFactory.indexer.setExpectedFile(new URL[] {f1.toURL()}, new URL[0], new URL[0]);
        r.setExcludes();

        indexerFactory.indexer.awaitDeleted(TIME);
        indexerFactory.indexer.awaitIndex(TIME);

        assertEquals(0, indexerFactory.indexer.deletedCounter);
        assertEquals(1, indexerFactory.indexer.indexCounter);
        assertEquals(Collections.emptySet(), indexerFactory.indexer.expectedIndex);
    }

    public void testPeerRootsCalculation() throws Exception {
        //Prepare
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        ClassPathProviderImpl.reset();

        //Register src path {srcRoot1, srcRootWithFiles1}
        handler.reset(TestHandler.Type.ROOTS_WORK_FINISHED);
        final MutableClassPathImplementation mcpi = new MutableClassPathImplementation ();
        mcpi.addResource(this.srcRoot1);
        mcpi.addResource(this.srcRootWithFiles1);
        final ClassPath cp = ClassPathFactory.createClassPath(mcpi);
        ClassPathProviderImpl.register(srcRoot1, SOURCES, cp);
        ClassPathProviderImpl.register(srcRoot2, SOURCES, cp);
        ClassPathProviderImpl.register(srcRootWithFiles1, SOURCES, cp);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp});
        assertTrue (handler.await());
        Map<URL,List<URL>> peersMap = IndexingController.getDefault().getRootPeers();
        assertEquals(2, peersMap.size());
        List<URL> peers = peersMap.get(srcRoot1.toURL());
        assertEquals(Collections.singletonList(srcRootWithFiles1.toURL()), peers);
        peers = peersMap.get(srcRootWithFiles1.toURL());
        assertEquals(Collections.singletonList(srcRoot1.toURL()), peers);

        //Remove srcRootWithFiles1 from src path
        handler.reset(TestHandler.Type.ROOTS_WORK_FINISHED);
        mcpi.removeResource(this.srcRootWithFiles1);
        assertTrue (handler.await());
        peersMap = IndexingController.getDefault().getRootPeers();
        assertEquals(1, peersMap.size());
        peers = peersMap.get(srcRoot1.toURL());
        assertEquals(Collections.<URL>emptyList(), peers);

        //Readd the srcRootWithFiles1 to src path
        handler.reset(TestHandler.Type.ROOTS_WORK_FINISHED);
        mcpi.addResource(this.srcRootWithFiles1);
        assertTrue (handler.await());
        peersMap = IndexingController.getDefault().getRootPeers();
        assertEquals(2, peersMap.size());
        peers = peersMap.get(srcRoot1.toURL());
        assertEquals(Collections.singletonList(srcRootWithFiles1.toURL()), peers);
        peers = peersMap.get(srcRootWithFiles1.toURL());
        assertEquals(Collections.singletonList(srcRoot1.toURL()), peers);

        //Add srcRoot2 to src path
        handler.reset(TestHandler.Type.ROOTS_WORK_FINISHED);
        mcpi.addResource(this.srcRoot2);
        assertTrue (handler.await());
        peersMap = IndexingController.getDefault().getRootPeers();
        assertEquals(3, peersMap.size());
        List<URL> returnedPeers = new ArrayList<> (peersMap.get(srcRoot1.toURL()));
        returnedPeers.sort(new URLComparator());
        List<URL> expectedPeers = new ArrayList<URL> () {{add(srcRootWithFiles1.toURL()); add(srcRoot2.toURL());}};
        expectedPeers.sort(new URLComparator());
        assertEquals(expectedPeers, returnedPeers);
        returnedPeers = new ArrayList<> (peersMap.get(srcRoot2.toURL()));
        returnedPeers.sort(new URLComparator());
        expectedPeers = new ArrayList<URL> () {{add(srcRootWithFiles1.toURL()); add(srcRoot1.toURL());}};
        expectedPeers.sort(new URLComparator());
        assertEquals(expectedPeers, returnedPeers);
        returnedPeers = new ArrayList<> (peersMap.get(srcRootWithFiles1.toURL()));
        returnedPeers.sort(new URLComparator());
        expectedPeers = new ArrayList<URL> () {{add(srcRoot1.toURL()); add(srcRoot2.toURL());}};
        expectedPeers.sort(new URLComparator());
        assertEquals(expectedPeers, returnedPeers);

        //Remove srcRoot2 from src path
        handler.reset(TestHandler.Type.ROOTS_WORK_FINISHED);
        mcpi.removeResource(this.srcRoot2);
        assertTrue (handler.await());
        peersMap = IndexingController.getDefault().getRootPeers();
        assertEquals(2, peersMap.size());
        peers = peersMap.get(srcRoot1.toURL());
        assertEquals(Collections.singletonList(srcRootWithFiles1.toURL()), peers);
        peers = peersMap.get(srcRootWithFiles1.toURL());
        assertEquals(Collections.singletonList(srcRoot1.toURL()), peers);

        //Remove srcRootWithFiles1
        handler.reset(TestHandler.Type.ROOTS_WORK_FINISHED);
        mcpi.removeResource(this.srcRootWithFiles1);
        assertTrue (handler.await());
        peersMap = IndexingController.getDefault().getRootPeers();
        assertEquals(1, peersMap.size());
        peers = peersMap.get(srcRoot1.toURL());
        assertEquals(Collections.<URL>emptyList(), peers);

    }

    public void testCheckAllFiles() throws Exception {
        RepositoryUpdater ru = RepositoryUpdater.getDefault();
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedUnknowns().size());

        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);

        //1st) Source root seen for first time (allFiles should be true)
        indexerFactory.indexer.setExpectedFile(customFiles, new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(embeddedFiles, new URL[0], new URL[0]);
        final MutableClassPathImplementation mcpi1 = new MutableClassPathImplementation ();
        mcpi1.addResource(this.srcRootWithFiles1);
        final ClassPath cp1 = ClassPathFactory.createClassPath(mcpi1);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRootWithFiles1.toURL(), handler.getSources().get(0));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
        Map<URL,Pair<Boolean,Boolean>> contextState = indexerFactory.indexer.getContextState();
        assertEquals(1, contextState.size());
        Pair<Boolean,Boolean> state = contextState.get(this.srcRootWithFiles1.toURL());
        assertNotNull(state);
        assertTrue(state.first());
        assertFalse(state.second());
        contextState = eindexerFactory.indexer.getContextState();
        assertEquals(embeddedFiles.length, contextState.size());
        for (URL url : embeddedFiles) {
            state = contextState.get(url);
            assertNotNull(state);
            assertTrue(state.first());
            assertFalse(state.second());
        }

        //2nd) Clean up - unregister
        handler.reset();
        globalPathRegistry_unregister(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedUnknowns().size());


        //3rd) Source root seen for second time (allFiles should be false)
        indexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        handler.reset();
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRootWithFiles1.toURL(), handler.getSources().get(0));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
        contextState = indexerFactory.indexer.getContextState();
        assertEquals(1, contextState.size());
        state = contextState.get(this.srcRootWithFiles1.toURL());
        assertNotNull(state);
        assertFalse(state.first());
        assertFalse(state.second());
        contextState = eindexerFactory.indexer.getContextState();
        assertEquals(0, contextState.size());

        //4th) Clean up - unregister
        handler.reset();
        globalPathRegistry_unregister(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedUnknowns().size());


        //5th Do some modification and reopen the project (allFiles should be false)
        fsWait();
        touch(customFiles[0]);
        touch(embeddedFiles[0]);
        indexerFactory.indexer.setExpectedFile(new URL[] {customFiles[0]}, new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[] {embeddedFiles[0]}, new URL[0], new URL[0]);
        handler.reset();
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRootWithFiles1.toURL(), handler.getSources().get(0));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
        contextState = indexerFactory.indexer.getContextState();
        assertEquals(1, contextState.size());
        state = contextState.get(this.srcRootWithFiles1.toURL());
        assertNotNull(state);
        assertFalse(state.first());
        assertFalse(state.second());
        contextState = eindexerFactory.indexer.getContextState();
        assertEquals(1, contextState.size());
        state = contextState.get(embeddedFiles[0]);
        assertNotNull(state);
        assertFalse(state.first());
        assertFalse(state.second());

        //6th Do some modification when source are registered (allFiles should be false)
        indexerFactory.indexer.setExpectedFile(new URL[] {customFiles[0]}, new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[] {embeddedFiles[0]}, new URL[0], new URL[0]);
        fsWait();
        touch(customFiles[0]);
        touch(embeddedFiles[0]);
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
        contextState = indexerFactory.indexer.getContextState();
        assertEquals(1, contextState.size());
        state = contextState.get(this.srcRootWithFiles1.toURL());
        assertNotNull(state);
        assertFalse(state.first());
        assertFalse(state.second());
        contextState = eindexerFactory.indexer.getContextState();
        assertEquals(1, contextState.size());
        state = contextState.get(embeddedFiles[0]);
        assertNotNull(state);
        assertFalse(state.first());
        assertFalse(state.second());

        //7th IndexingManager.refreshIndex(root, all_files, fullRescan==true) (allFiles should be true)
        indexerFactory.indexer.setExpectedFile(customFiles, new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(embeddedFiles, new URL[0], new URL[0]);
        IndexingManager.getDefault().refreshIndex(srcRootWithFiles1.toURL(), Collections.<URL>emptySet(), true);
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
        contextState = indexerFactory.indexer.getContextState();
        assertEquals(1, contextState.size());
        state = contextState.get(this.srcRootWithFiles1.toURL());
        assertNotNull(state);
        assertTrue(state.first());
        assertFalse(state.second());
        contextState = eindexerFactory.indexer.getContextState();
        assertEquals(embeddedFiles.length, contextState.size());
        for (URL url : embeddedFiles) {
            state = contextState.get(url);
            assertNotNull(state);
            assertTrue(state.first());
            assertFalse(state.second());
        }

        //8th IndexingManager.refreshIndex(root, specifoc_file, fullRescan==true, checkEditor==true) (allFiles should be false, checkForEditorModifications should be true)
        indexerFactory.indexer.setExpectedFile(new URL[] {customFiles[0]}, new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[] {embeddedFiles[0]}, new URL[0], new URL[0]);
        IndexingManager.getDefault().refreshIndex(srcRootWithFiles1.toURL(), Arrays.asList(new URL[] {customFiles[0], embeddedFiles[0]}), true, true);
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
        contextState = indexerFactory.indexer.getContextState();
        assertEquals(1, contextState.size());
        state = contextState.get(this.srcRootWithFiles1.toURL());
        assertNotNull(state);
        assertFalse(state.first());
        assertTrue(state.second());
        contextState = eindexerFactory.indexer.getContextState();
        assertEquals(1, contextState.size());
        state = contextState.get(embeddedFiles[0]);
        assertNotNull(state);
        assertFalse(state.first());
        assertTrue(state.second());
    }

    public void testVisibilityQueryAmongIDERestarts() throws Exception {
        //1st) Default visibility everything should be scanned
        final RepositoryUpdater ru = RepositoryUpdater.getDefault();
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedUnknowns().size());

        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);

        //2nd) Source root seen for first time (allFiles should be true)
        indexerFactory.indexer.setExpectedFile(customFiles, new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(embeddedFiles, new URL[0], new URL[0]);
        final MutableClassPathImplementation mcpi1 = new MutableClassPathImplementation ();
        mcpi1.addResource(this.srcRootWithFiles1);
        final ClassPath cp1 = ClassPathFactory.createClassPath(mcpi1);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRootWithFiles1.toURL(), handler.getSources().get(0));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());

        //3rd) Unregister - IDE closed
        handler.reset();
        globalPathRegistry_unregister(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedUnknowns().size());
        Crawler.setListenOnVisibility(false);

        //4th) Making customFiles[1] invisible & touching the files to be indexed
        fsWait();
        touch (customFiles);
        touch (embeddedFiles);
        final Visibility visibility = Lookup.getDefault().lookup(Visibility.class);
        assertNotNull(visibility);
        visibility.registerInvisibles(Collections.singleton(URLMapper.findFileObject(customFiles[1])));
        handler.reset();
        indexerFactory.indexer.setExpectedFile(new URL[] {customFiles[0]}, new URL[] {customFiles[1]}, new URL[0]);
        eindexerFactory.indexer.setExpectedFile(embeddedFiles, new URL[0], new URL[0]);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRootWithFiles1.toURL(), handler.getSources().get(0));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
        assertTrue(indexerFactory.indexer.awaitDeleted(TIME));

        //5th) Unregister - IDE closed
        handler.reset();
        globalPathRegistry_unregister(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedUnknowns().size());
        Crawler.setListenOnVisibility(false);

        //6th) Making customFiles[1] visible again & touching the files to be indexed
        fsWait();
        touch (customFiles);
        touch (embeddedFiles);
        visibility.registerInvisibles(Collections.<FileObject>emptySet());
        handler.reset();
        indexerFactory.indexer.setExpectedFile(customFiles, new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(embeddedFiles, new URL[0], new URL[0]);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRootWithFiles1.toURL(), handler.getSources().get(0));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
    }

    public void testVisibilityQueryInIDERun() throws Exception {
        final RepositoryUpdater ru = RepositoryUpdater.getDefault();
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedUnknowns().size());

        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);

        ////1st) Default visibility everything should be scanned
        indexerFactory.indexer.setExpectedFile(customFiles, new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(embeddedFiles, new URL[0], new URL[0]);
        final MutableClassPathImplementation mcpi1 = new MutableClassPathImplementation ();
        mcpi1.addResource(this.srcRootWithFiles1);
        final ClassPath cp1 = ClassPathFactory.createClassPath(mcpi1);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRootWithFiles1.toURL(), handler.getSources().get(0));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());

        //2nd) Change of VisibilityQuery should trigger rescan, customFiles[1] should be invisible
        final Visibility visibility = Lookup.getDefault().lookup(Visibility.class);
        assertNotNull(visibility);
        visibility.registerInvisibles(Collections.singleton(URLMapper.findFileObject(customFiles[1])));
        handler.reset();
        indexerFactory.indexer.setExpectedFile(new URL[0], new URL[] {customFiles[1]}, new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRootWithFiles1.toURL(), handler.getSources().get(0));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
        assertTrue(indexerFactory.indexer.awaitDeleted(TIME));

        //3rd) Change of VisibilityQuery should trigger rescan, customFiles[1] should be visible again
        visibility.registerInvisibles(Collections.<FileObject>emptySet());
        handler.reset();
        indexerFactory.indexer.setExpectedFile(new URL[]{customFiles[1]}, new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRootWithFiles1.toURL(), handler.getSources().get(0));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
        assertTrue(indexerFactory.indexer.awaitDeleted(TIME));
    }

    public void testIndexabilityQueryInIDERun() throws Exception {
        final RepositoryUpdater ru = RepositoryUpdater.getDefault();
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedUnknowns().size());

        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);

        ////1st) Default visibility everything should be scanned
        indexerFactory.indexer.setExpectedFile(customFiles, new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(embeddedFiles, new URL[0], new URL[0]);
        final MutableClassPathImplementation mcpi1 = new MutableClassPathImplementation         ();
        mcpi1.addResource(this.srcRootWithFiles1);
        final ClassPath cp1 = ClassPathFactory.createClassPath(mcpi1);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRootWithFiles1.toURL(), handler.getSources().get(0));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());

        //2nd) Change of VisibilityQuery should trigger rescan,
        //     customFiles[1] should be invisible and have been removed from
        //     index
        final Indexability indexability = Lookup.getDefault().lookup(Indexability.class);
        assertNotNull(indexability);
        indexability.registerInvisibles(Collections.singleton(URLMapper.findFileObject(customFiles[1])));
        handler.reset();
        indexerFactory.indexer.deletedCounter = 0;
        eindexerFactory.indexer.deletedCounter = 0;
        indexerFactory.indexer.setExpectedFile(new URL[0], new URL[] {customFiles[1]}, new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRootWithFiles1.toURL(), handler.getSources().get(0));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
        assertTrue(indexerFactory.indexer.awaitDeleted(TIME));
        assertEquals(1, eindexerFactory.indexer.deletedCounter);

        //3rd) Change of VisibilityQuery should trigger rescan, customFiles[1] should be visible again
        indexability.registerInvisibles(Collections.<FileObject>emptySet());
        handler.reset();
        indexerFactory.indexer.setExpectedFile(new URL[]{customFiles[1]}, new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRootWithFiles1.toURL(), handler.getSources().get(0));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
        assertTrue(indexerFactory.indexer.awaitDeleted(TIME));
    }
    
    public void testScannerBasedFilter() throws Exception {
        final RepositoryUpdater ru = RepositoryUpdater.getDefault();
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedUnknowns().size());

        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        
        // Block indexing for the "emb" indexer only
        final Indexability indexability = Lookup.getDefault().lookup(Indexability.class);
        assertNotNull(indexability);
        indexability.registerInvisiblesByIndexer(Collections.singleton("emb"));
        
        indexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        eindexerFactory.indexer.setExpectedFile(new URL[0], new URL[0], new URL[0]);
        final MutableClassPathImplementation mcpi1 = new MutableClassPathImplementation         ();
        mcpi1.addResource(this.srcRootWithFiles1);
        final ClassPath cp1 = ClassPathFactory.createClassPath(mcpi1);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRootWithFiles1.toURL(), handler.getSources().get(0));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(eindexerFactory.indexer.awaitIndex());
        
        // srcRootWithFiles1 has to files that are scanned by the foo indexer
        // and two that are scanned by the emb indexer. The latter is blocked,
        // so should report 0 files indexed
        assertEquals(2, indexerFactory.indexer.getIndexCount());
        assertEquals(0, eindexerFactory.indexer.getIndexCount());
    }
    
    /**
     * Test that unknown source roots are registered in the scannedRoots2Dependencies with EMPTY_DEPS
     * and when the unknown root registers it's changed to regular dependencies.
     * When the root is unregistered it's removed from scannedRoots2Dependencies
     *
     * The test was also extended to test Issue 196985. Unknown root which becomes known was lost
     * @throws Exception
     */
    public void testIndexManagerRefreshIndexListensOnChanges_And_Issue196985() throws Exception {
        final File _wd = this.getWorkDir();
        final FileObject wd = FileUtil.toFileObject(_wd);
        final FileObject refreshedRoot = wd.createFolder("refreshedRoot");
        final RepositoryUpdater ru = RepositoryUpdater.getDefault();
        assertNotNull(refreshedRoot);
        assertFalse (ru.getScannedRoots2Dependencies().containsKey(refreshedRoot.toURL()));
        IndexingManager.getDefault().refreshIndexAndWait(refreshedRoot.toURL(), Collections.<URL>emptyList());
        assertSame(RepositoryUpdater.UNKNOWN_ROOT, ru.getScannedRoots2Dependencies().get(refreshedRoot.toURL()));
        //Register the root => EMPTY_DEPS changes to regular deps
        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        final ClassPath cp = ClassPathSupport.createClassPath(refreshedRoot);
        handler.reset(RepositoryUpdaterTest.TestHandler.Type.ROOTS_WORK_FINISHED);
        globalPathRegistry_register(SOURCES, new ClassPath[]{cp});
        handler.await();
        assertNotNull(ru.getScannedRoots2Dependencies().get(refreshedRoot.toURL()));
        assertNotSame(RepositoryUpdater.UNKNOWN_ROOT, ru.getScannedRoots2Dependencies().get(refreshedRoot.toURL()));
        handler.reset(RepositoryUpdaterTest.TestHandler.Type.ROOTS_WORK_FINISHED);
        globalPathRegistry_unregister(SOURCES, new ClassPath[]{cp});
        handler.await();
        assertFalse(ru.getScannedRoots2Dependencies().containsKey(refreshedRoot.toURL()));
    }


    public void testRUSuspendedByUserTask() throws Exception {
        final Source source = Source.create(f1);
        final RequestProcessor worker = new RequestProcessor("testRUNotInterruptableBySourceChanges", 1);   //NOI18N
        class H extends Handler {
            private final Semaphore sem = new Semaphore(0);
            private final CountDownLatch latch = new CountDownLatch(1);
            private final AtomicBoolean didCancel = new AtomicBoolean();
            private final AtomicBoolean wasCanceled = new AtomicBoolean();
            @Override
            public void publish(LogRecord record) {
                if (null != record.getMessage()){
                    switch (record.getMessage()) {
                        case "RootsWork-started":   //NOI18N
                            if (!didCancel.getAndSet(true)) {
                                final RequestProcessor.Task task = worker.create(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            ParserManager.parse(Collections.singleton(source), new UserTask() {
                                                @Override
                                                public void run(ResultIterator resultIterator) throws Exception {
                                                }
                                            });
                                        } catch (ParseException ex) {
                                            Exceptions.printStackTrace(ex);
                                        }
                                    }
                                });
                                task.schedule(0);
                                try {
                                    latch.await(TIME, TimeUnit.MILLISECONDS);
                                } catch (InterruptedException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }   break;
                        case "SUSPEND: {0}":    //NOI18N
                        wasCanceled.set(true);
                            latch.countDown();
                            break;
                        case "scanSources": //NOI18N
                            final List<URL> roots = (List<URL>) record.getParameters()[0];
                            sem.release(roots.size());
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
            public boolean await(int number, long timeout, TimeUnit unit) throws InterruptedException {
                return sem.tryAcquire(number, timeout, unit);
            }
            public boolean wasCanceled() {
                return this.wasCanceled.get();
            }
        }
        final H h = new H();
        Logger log1 = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");  //NOI18N
        Logger log2 = Logger.getLogger(SuspendSupport.class.getName());
        log1.setLevel(Level.FINEST);
        log1.addHandler(h);
        log2.setLevel(Level.FINEST);
        log2.addHandler(h);
        final ClassPath cp = ClassPathSupport.createClassPath(srcRoot1, srcRoot2, srcRootWithFiles1);
        globalPathRegistry_register(SOURCES, new ClassPath[]{cp});
        assertTrue(h.await(3, TIME, TimeUnit.MILLISECONDS));
        assertTrue(h.wasCanceled());
    }

    public void testRUSuspendedByIndexReadAccess() throws Exception {
        final RequestProcessor worker = new RequestProcessor("testRUNotInterruptableBySourceChanges", 1);   //NOI18N
        class H extends Handler {
            private final Semaphore sem = new Semaphore(0);
            private final CountDownLatch latch = new CountDownLatch(1);
            private final AtomicBoolean didCancel = new AtomicBoolean();
            private final AtomicBoolean wasCanceled = new AtomicBoolean();
            @Override
            public void publish(LogRecord record) {
                if (null != record.getMessage())
                {   switch (record.getMessage()) {
                        case "RootsWork-started": //NOI18N
                            if (!didCancel.getAndSet(true)) {
                                final RequestProcessor.Task task = worker.create(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            IndexManager.priorityAccess(new IndexManager.Action<Void>() {
                                                @Override
                                                public Void run() throws IOException, InterruptedException {
                                                    return null;
                                                }
                                            });
                                        } catch (IOException ex) {
                                            Exceptions.printStackTrace(ex);
                                        } catch (InterruptedException ex) {
                                            Exceptions.printStackTrace(ex);
                                        }
                                    }
                                });
                                task.schedule(0);
                                try {
                                    latch.await(TIME, TimeUnit.MILLISECONDS);
                                } catch (InterruptedException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }   break;
                        case "SUSPEND: {0}":    //NOI18N
                        wasCanceled.set(true);
                        latch.countDown();
                        break;
                        case "scanSources":     //NOI18N
                            final List<URL> roots = (List<URL>) record.getParameters()[0];
                            sem.release(roots.size());
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
            public boolean await(int number, long timeout, TimeUnit unit) throws InterruptedException {
                return sem.tryAcquire(number, timeout, unit);
            }
            public boolean wasCanceled() {
                return this.wasCanceled.get();
            }
        }
        final H h = new H();
        Logger log1 = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");  //NOI18N
        Logger log2 = Logger.getLogger(SuspendSupport.class.getName());
        log1.setLevel(Level.FINEST);
        log1.addHandler(h);
        log2.setLevel(Level.FINEST);
        log2.addHandler(h);
        final ClassPath cp = ClassPathSupport.createClassPath(srcRoot1, srcRoot2, srcRootWithFiles1);
        globalPathRegistry_register(SOURCES, new ClassPath[]{cp});
        assertTrue(h.await(3, TIME, TimeUnit.MILLISECONDS));
        assertTrue(h.wasCanceled());
    }

    public void testRUSuspendedByEditorParsingThread() throws Exception {
        final Source source = Source.create(f1);
        final RequestProcessor worker = new RequestProcessor("testRUNotInterruptableBySourceChanges", 1);   //NOI18N
        class H extends Handler {
            private final Semaphore sem = new Semaphore(0);
            private final CountDownLatch latch = new CountDownLatch(1);
            private final AtomicBoolean didCancel = new AtomicBoolean();
            private final AtomicBoolean wasCanceled = new AtomicBoolean();
            @Override
            public void publish(LogRecord record) {
                if (null != record.getMessage()) {
                    switch (record.getMessage()) {
                        case "RootsWork-started":   //NOI18N
                            if (!didCancel.getAndSet(true)) {
                                final RequestProcessor.Task task = worker.create(new Runnable() {
                                    @Override
                                    public void run() {
                                        final ParserResultTask task = new ParserResultTask() {
                                            @Override
                                            public void run(Result result, SchedulerEvent event) {
                                                //NOP
                                            }

                                            @Override
                                            public int getPriority() {
                                                return 0;
                                            }

                                            @Override
                                            public Class<? extends Scheduler> getSchedulerClass() {
                                                return null;
                                            }

                                            @Override
                                            public void cancel() {
                                            }
                                        };
                                        Utilities.addParserResultTask(task, source);
                                    }
                                });
                                task.schedule(0);
                                try {
                                    latch.await(TIME, TimeUnit.MILLISECONDS);
                                } catch (InterruptedException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }   break;
                        case "SUSPEND: {0}":    //NOI18N
                            wasCanceled.set(true);
                            latch.countDown();
                            break;
                        case "scanSources":     //NOI18N
                        final List<URL> roots = (List<URL>) record.getParameters()[0];
                            sem.release(roots.size());
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
            public boolean await(int number, long timeout, TimeUnit unit) throws InterruptedException {
                return sem.tryAcquire(number, timeout, unit);
            }
            public boolean wasCanceled() {
                return this.wasCanceled.get();
            }
        }
        final H h = new H();
        Logger log1 = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");  //NOI18N
        Logger log2 = Logger.getLogger(SuspendSupport.class.getName());
        log1.setLevel(Level.FINEST);
        log1.addHandler(h);
        log2.setLevel(Level.FINEST);
        log2.addHandler(h);
        final ClassPath cp = ClassPathSupport.createClassPath(srcRoot1, srcRoot2, srcRootWithFiles1);
        globalPathRegistry_register(SOURCES, new ClassPath[]{cp});
        assertTrue(h.await(3, TIME, TimeUnit.MILLISECONDS));
        assertTrue(h.wasCanceled());
    }

    public void testPerfLogger() throws Exception {
        final File workdDir  =  getWorkDir();
        final File root = new File (workdDir,"loggerTest");             //NOI18N
        final FileObject rootFo = FileUtil.createFolder(root);
        FileUtil.createData(rootFo, "a.foo");   //NOI18N
        FileUtil.createData(rootFo, "a.emb");   //NOI18N
        final ClassPath cp = ClassPathSupport.createClassPath(rootFo);
        class PerfLoghandler extends Handler {

            class R {
                private final Queue<String> toExpect;
                private final Map<String,Object[]> values;

                private R(final String... toExpect) {
                    this.toExpect = new LinkedList<>(Arrays.asList(toExpect));
                    this.values = new HashMap<>();
                }

                Object[] getParams(String expectedKey) {
                    return values.get(expectedKey);
                }
            }

            private R r;

            public synchronized void expect(final String... expect) {
                r = new R(expect);
            }

            public synchronized R await(long timeOut) throws InterruptedException {
                final long st = System.currentTimeMillis();
                while (!r.toExpect.isEmpty()) {
                    if (System.currentTimeMillis()-st > timeOut) {
                        return null;
                    }
                    wait(timeOut);
                }
                return r;
            }


            @Override
            public synchronized void publish(LogRecord record) {
                if (record.getMessage() != null && record.getMessage().startsWith(r.toExpect.peek())) {
                    r.values.put(r.toExpect.peek(),record.getParameters());
                    r.toExpect.poll();
                }
                if (r.toExpect.isEmpty()) {
                    notifyAll();
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        }
        final PerfLoghandler h = new PerfLoghandler();
        final Logger perfLogger = Logger.getLogger(RepositoryUpdater.class.getName() + ".perf");    //NOI18N
        perfLogger.addHandler(h);
        try {
            perfLogger.setLevel(Level.FINE);
            h.expect("reportScanOfFile:","INDEXING_FINISHED");   //NOI18N
            globalPathRegistry_register(SOURCES, new ClassPath[]{cp});
            final PerfLoghandler.R r = h.await(TIME);
            assertNotNull(r);
            assertEquals(rootFo.toURL(), r.getParams("reportScanOfFile:")[0]);     //NOI18N
            final Object[] data = r.getParams("INDEXING_FINISHED"); //NOI18N
            assertEquals(7, data.length);
            assertTrue((Long)data[0] >= 0);
            assertTrue("emb".equals(data[1]) || "foo".equals(data[1])); //NOI18N
            assertTrue((Integer)data[2] == 1);
            assertTrue((Integer)data[3] >= 0);
            assertTrue("emb".equals(data[4]) || "foo".equals(data[4])); //NOI18N
            assertTrue((Integer)data[5] == 1);
            assertTrue((Integer)data[6] >= 0);
        } finally {
            perfLogger.removeHandler(h);
        }
    }

    public void testUILogger() throws Exception {
        final File workdDir  =  getWorkDir();
        final File root = new File (workdDir,"loggerTest");             //NOI18N
        final FileObject rootFo = FileUtil.createFolder(root);
        final FileObject afoo = FileUtil.createData(rootFo, "a.foo");   //NOI18N
        final FileObject aemb = FileUtil.createData(rootFo, "a.emb");   //NOI18N
        final ClassPath cp = ClassPathSupport.createClassPath(rootFo);
        class PerfLoghandler extends Handler {

            class R {
                private final Queue<String> toExpect;
                private final Map<String,Object[]> values;

                private R(final String... toExpect) {
                    this.toExpect = new LinkedList<>(Arrays.asList(toExpect));
                    this.values = new HashMap<>();
                }

                Object[] getParams(String expectedKey) {
                    return values.get(expectedKey);
                }
            }

            private R r;

            public synchronized void expect(final String... expect) {
                r = new R(expect);
            }

            public synchronized R await(long timeOut) throws InterruptedException {
                final long st = System.currentTimeMillis();
                while (!r.toExpect.isEmpty()) {
                    if (System.currentTimeMillis()-st > timeOut) {
                        return null;
                    }
                    wait(timeOut);
                }
                return r;
            }


            @Override
            public synchronized void publish(LogRecord record) {
                if (record.getMessage() != null && record.getMessage().startsWith(r.toExpect.peek())) {
                    r.values.put(r.toExpect.peek(),record.getParameters());
                    r.toExpect.poll();
                }
                if (r.toExpect.isEmpty()) {
                    notifyAll();
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        }
        final PerfLoghandler h = new PerfLoghandler();
        final Logger uiLogger = Logger.getLogger("org.netbeans.ui.indexing");    //NOI18N
        uiLogger.addHandler(h);
        final Class<?> c = Class.forName("org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater$Work"); //NOI18N
        final Field f = c.getDeclaredField("lastScanEnded");
        f.setAccessible(true);
        f.setLong(null, -1L);
        try {
            uiLogger.setLevel(Level.FINE);
            h.expect("INDEXING_STARTED","INDEXING_FINISHED");   //NOI18N
            globalPathRegistry_register(SOURCES, new ClassPath[]{cp});
            PerfLoghandler.R r = h.await(TIME);
            assertNotNull(r);
            assertEquals(0L, r.getParams("INDEXING_STARTED")[0]);     //NOI18N
            Object[] data = r.getParams("INDEXING_FINISHED"); //NOI18N
            assertEquals(7, data.length);
            assertTrue((Long)data[0] >= 0);
            assertTrue("emb".equals(data[1]) || "foo".equals(data[1])); //NOI18N
            assertTrue((Integer)data[2] == 1);
            assertTrue((Integer)data[3] >= 0);
            assertTrue("emb".equals(data[4]) || "foo".equals(data[4])); //NOI18N
            assertTrue((Integer)data[5] == 1);
            assertTrue((Integer)data[6] >= 0);
            Thread.sleep(1000);
            h.expect("INDEXING_STARTED","INDEXING_FINISHED");   //NOI18N
            touch(afoo.toURL());
            r = h.await(TIME);
            assertNotNull(r);
            assertTrue(1000L <= (Long)r.getParams("INDEXING_STARTED")[0]);     //NOI18N
            data = r.getParams("INDEXING_FINISHED"); //NOI18N
            assertEquals(4, data.length);
            assertTrue((Long)data[0] >= 0);
            assertTrue("foo".equals(data[1])); //NOI18N
            assertTrue((Integer)data[2] == 1);
            assertTrue((Integer)data[3] >= 0);
        } finally {
            uiLogger.removeHandler(h);
        }
    }

    public void testIndexDownloader() throws Exception {
        final File workDir = getWorkDir();
        final File root = new File (workDir,"testIndexDownloader"); //NOI18N
        final File folder = new File (root,"folder");               //NOI18N
        final File a = new File(folder,"a.foo");                    //NOI18N
        final File b = new File(folder,"b.foo");                    //NOI18N
        final File index = new File(workDir,"index.zip");           //NOI18N
        folder.mkdirs();
        a.createNewFile();
        b.createNewFile();
        final ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(index));
        try {
            zout.putNextEntry(new ZipEntry("timestamps.properties"));   //NOI18N
            zout.write("folder/a.foo=10\n".getBytes());                 //NOI18N
            zout.write("folder/b.foo=10\n".getBytes());                 //NOI18N
        } finally {
            zout.close();
        }

        //Unsee root - index should be donwloaded and no indexer should be called
        final URL rootURL = FileUtil.urlForArchiveOrDir(root);
        final ClassPath cp1 = ClassPathSupport.createClassPath(rootURL);
        IndexDownloaderImpl.expect(rootURL, org.openide.util.Utilities.toURI(index).toURL());
        indexerFactory.indexer.setExpectedFile(
                new URL[]{
                    org.openide.util.Utilities.toURI(a).toURL(),
                    org.openide.util.Utilities.toURI(b).toURL()},
                new URL[0],
                new URL[0]);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue(IndexDownloaderImpl.await(TIME));
        assertFalse(indexerFactory.indexer.awaitIndex(NEGATIVE_TIME));
        assertEquals(0, indexerFactory.indexer.getIndexCount());

        globalPathRegistry_unregister(SOURCES, new ClassPath[]{cp1});
        RepositoryUpdater.getDefault().waitUntilFinished(TIME);
        touchFile(a);

        //Seen root - index should NOT be donwloaded and indexer should be called on modified file
        IndexDownloaderImpl.expect(rootURL, org.openide.util.Utilities.toURI(index).toURL());
        indexerFactory.indexer.setExpectedFile(
                new URL[]{
                    org.openide.util.Utilities.toURI(a).toURL(),
                    org.openide.util.Utilities.toURI(b).toURL(),  //Should be removed if timestamps maps
                },
                new URL[0],
                new URL[0]);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertFalse(IndexDownloaderImpl.await(NEGATIVE_TIME));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertEquals(2, indexerFactory.indexer.getIndexCount());

        //Simulate the index download error - indexer should be started
        globalPathRegistry_unregister(SOURCES, new ClassPath[]{cp1});
        RepositoryUpdater.getDefault().waitUntilFinished(TIME);
        FileObject fo = CacheFolder.getDataFolder(org.openide.util.Utilities.toURI(root).toURL());
        fo.delete();
        IndexDownloaderImpl.expect(rootURL, org.openide.util.Utilities.toURI(new File(workDir,"non_existent_index.zip")).toURL());
        indexerFactory.indexer.setExpectedFile(
                new URL[]{
                    org.openide.util.Utilities.toURI(a).toURL(),
                    org.openide.util.Utilities.toURI(b).toURL()},
                new URL[0],
                new URL[0]);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue(IndexDownloaderImpl.await(TIME));
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertEquals(2, indexerFactory.indexer.getIndexCount());

        //Test DownloadedIndexPatcher - votes false -> IndexDownloader should be called and then Indexers should be called
        globalPathRegistry_unregister(SOURCES, new ClassPath[]{cp1});
        RepositoryUpdater.getDefault().waitUntilFinished(TIME);
        fo = CacheFolder.getDataFolder(org.openide.util.Utilities.toURI(root).toURL());
        fo.delete();
        IndexDownloaderImpl.expect(rootURL, org.openide.util.Utilities.toURI(index).toURL());
        IndexPatcherImpl.expect(rootURL, false);
        indexerFactory.indexer.setExpectedFile(
                new URL[]{
                    org.openide.util.Utilities.toURI(a).toURL(),
                    org.openide.util.Utilities.toURI(b).toURL()},
                new URL[0],
                new URL[0]);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue(IndexDownloaderImpl.await(TIME));
        assertTrue(IndexPatcherImpl.await());
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertEquals(2, indexerFactory.indexer.getIndexCount());

        //Test DownloadedIndexPatcher - votes true -> IndexDownloader should be called and NO Indexers should be called
        globalPathRegistry_unregister(SOURCES, new ClassPath[]{cp1});
        RepositoryUpdater.getDefault().waitUntilFinished(TIME);
        fo = CacheFolder.getDataFolder(org.openide.util.Utilities.toURI(root).toURL());
        fo.delete();
        IndexDownloaderImpl.expect(rootURL, org.openide.util.Utilities.toURI(index).toURL());
        IndexPatcherImpl.expect(rootURL, true);
        indexerFactory.indexer.setExpectedFile(
                new URL[]{
                    org.openide.util.Utilities.toURI(a).toURL(),
                    org.openide.util.Utilities.toURI(b).toURL()},
                new URL[0],
                new URL[0]);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue(IndexDownloaderImpl.await(TIME));
        assertTrue(IndexPatcherImpl.await());
        assertFalse(indexerFactory.indexer.awaitIndex(NEGATIVE_TIME));
        assertEquals(0, indexerFactory.indexer.getIndexCount());
    }

    public void testVisibilityQueryInFileChangeListener() throws Exception {
        final FileObject workDir = FileUtil.toFileObject(getWorkDir());
        final FileObject root = FileUtil.createFolder(workDir,"visibilitySrc"); //NOI18N
        final FileObject folder1 = FileUtil.createFolder(root,"folder");               //NOI18N
        final FileObject a = FileUtil.createData(folder1,"a.foo");                    //NOI18N
        final FileObject folder2 = FileUtil.createFolder(folder1,"invisible");          //NOI18N
        final FileObject b = FileUtil.createData(folder2,"b.foo");                    //NOI18N
        final Visibility vis = Lookup.getDefault().lookup(Visibility.class);
        assertNotNull(vis);
        vis.registerInvisibles(Collections.<FileObject>singleton(folder2));
        final ClassPath cp = ClassPathSupport.createClassPath(root.toURL());
        indexerFactory.indexer.setExpectedFile(
                new URL[]{a.toURL()},
                new URL[0],
                new URL[0]);
        globalPathRegistry_register(SOURCES, new ClassPath[]{cp});
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertEquals(1, indexerFactory.indexer.getIndexCount());

        //Modify a.foo - should trigger indexer
        indexerFactory.indexer.setExpectedFile(
            new URL[]{a.toURL()},
            new URL[0],
            new URL[0]);
        touch(a.toURL());
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertEquals(1, indexerFactory.indexer.getIndexCount());

        //Modify b.foo - should NOT trigger indexer
        indexerFactory.indexer.setExpectedFile(
            new URL[]{b.toURL()},
            new URL[0],
            new URL[0]);
        touch(b.toURL());
        assertFalse(indexerFactory.indexer.awaitIndex(NEGATIVE_TIME));
        assertEquals(0, indexerFactory.indexer.getIndexCount());

        //Rename a.foo - should trigger indexer
        File af = FileUtil.toFile(a);
        File anf = new File (af.getParentFile(),"an.foo");  //NOI18N
        indexerFactory.indexer.setExpectedFile(
            new URL[]{org.openide.util.Utilities.toURI(anf).toURL()},
            new URL[]{org.openide.util.Utilities.toURI(af).toURL()},
            new URL[0]);
        FileLock l = a.lock();
        try {
            a.rename(l, "an", a.getExt());  //NOI18N
        } finally {
            l.releaseLock();
        }
        assertTrue(indexerFactory.indexer.awaitIndex(TIME));
        assertTrue(indexerFactory.indexer.awaitDeleted(TIME));
        assertEquals(1, indexerFactory.indexer.getIndexCount());

        //Rename b.foo - should trigger indexer
        File bf = FileUtil.toFile(b);
        File bnf = new File (bf.getParentFile(),"bn.foo");  //NOI18N
        indexerFactory.indexer.setExpectedFile(
            new URL[]{org.openide.util.Utilities.toURI(bnf).toURL()},
            new URL[]{org.openide.util.Utilities.toURI(bf).toURL()},
            new URL[0]);
        l = b.lock();
        try {
            b.rename(l, "bn", b.getExt());  //NOI18N
        } finally {
            l.releaseLock();
        }
        assertFalse(indexerFactory.indexer.awaitIndex(NEGATIVE_TIME));
        assertEquals(0, indexerFactory.indexer.getIndexCount());

        //Delete a.foo - should trigger indexed
        indexerFactory.indexer.setExpectedFile(
            new URL[0],
            new URL[]{a.toURL()},
            new URL[0]);
        a.delete();
        assertTrue(indexerFactory.indexer.awaitDeleted(TIME));

        //Delete b.foo - should NOT trigger indexed
        indexerFactory.indexer.setExpectedFile(
            new URL[0],
            new URL[]{b.toURL()},
            new URL[0]);
        b.delete();
        assertFalse(indexerFactory.indexer.awaitDeleted(NEGATIVE_TIME));
    }

    public void testExceptionFromScanStarted() throws Exception {
        final FooExceptionIndexerFactory fooExcPactory = new FooExceptionIndexerFactory();

        RepositoryUpdater ru = RepositoryUpdater.getDefault();
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedUnknowns().size());

        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);


        MockMimeLookup.setInstances(MimePath.get(MIME), fooExcPactory);
        fooExcPactory.finishedRoots.clear();
        ClassPath cp1 = ClassPathSupport.createClassPath(srcRoot1);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRoot1.toURL(), handler.getSources().get(0));
        assertEquals(1, fooExcPactory.finishedRoots.size());
        assertEquals(this.srcRoot1.toURI(), fooExcPactory.finishedRoots.iterator().next());
    }

    public void testQuerySupportFromIndexerDoesNotSeeModifiedFiles () throws Exception {
        final FooQueryIndexerFactory fooQueryPactory = new FooQueryIndexerFactory();

        RepositoryUpdater ru = RepositoryUpdater.getDefault();
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedUnknowns().size());

        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);

        MockMimeLookup.setInstances(MimePath.get(MIME), fooQueryPactory);
        final ClassPath cp1 = ClassPathSupport.createClassPath(srcRootWithFiles1);
        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(1,fooQueryPactory.maxDepth.get());
    }

//    public void testVisibilityQueryPerformance() throws Exception {
//        final FileObject workDir = FileUtil.toFileObject(getWorkDir());
//        final FileObject root = FileUtil.createFolder(workDir,"visibilitySrc"); //NOI18N
//        FileObject folder = root;
//        for (int i=0; i<10; i++) {
//            folder = FileUtil.createFolder(folder,"folder");    //folder
//        }
//        final FileObject a = FileUtil.createData(folder,"a.foo"); //NOI18N
//        final ClassPath cp = ClassPathSupport.createClassPath(root.getURL());
//        indexerFactory.indexer.setExpectedFile(
//                new URL[]{a.getURL()},
//                new URL[0],
//                new URL[0]);
//        globalPathRegistry_register(SOURCES, new ClassPath[]{cp});
//        assertTrue(indexerFactory.indexer.awaitIndex());
//        assertEquals(1, indexerFactory.indexer.getIndexCount());
//        class H extends Handler {
//            long time = 0L;
//            @Override
//            public void publish(LogRecord record) {
//                if (record.getMessage() != null && record.getMessage().startsWith("reportVisibilityOverhead:")) {   //NOI18N
//                    time+=(Long)record.getParameters()[0];
//                }
//            }
//            @Override
//            public void flush() {
//            }
//            @Override
//            public void close() throws SecurityException {
//            }
//        }
//        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName() + ".perf");  //NOI18N
//        final H h = new H();
//        logger.setLevel(Level.FINE);
//        logger.addHandler(h);
//        try {
//            for (int i=0; i< 10000; i++) {
//                FileUtil.createData(folder, String.format("b%d.foo",i));    //NOI18N
//            }
//        } finally {
//            logger.removeHandler(h);
//        }
//        System.out.println("Time: " + h.time);
//    }

    public static void setMimeTypes(final String... mimes) {
        Set<String> mt = new HashSet<>(Arrays.asList(mimes));
        Util.allMimeTypes = mt;
    }
    
    protected static void zipFileObject(FileObject fileObject) throws FileNotFoundException, IOException {
        File file = FileUtil.toFile(fileObject);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        ZipOutputStream os = new ZipOutputStream(fileOutputStream);
        os.close();
    }

    // <editor-fold defaultstate="collapsed" desc="Mock Services">
    public static class TestHandler extends Handler {

        public static enum Type {BATCH, DELETE, FILELIST, ROOTS_WORK_FINISHED, BINARY};

        private Type type;
        private CountDownLatch latch;
        private List<URL> sources;
        private Set<URL> binaries;

        public TestHandler () {
            reset();
        }

        public void reset () {
            reset (Type.BATCH);
        }

        public void reset(final Type t) {
            if (t == Type.BATCH) {
                reset(t, 2);
            } else {
                reset(t, 1);
            }
        }

        public void reset(final Type t, int initialCount) {
            sources = null;
            binaries = null;
            type = t;
            latch = new CountDownLatch(initialCount);
        }

        public boolean await () throws InterruptedException {
            return await(TIME);
        }

        public boolean await (long time) throws InterruptedException {
            return latch.await(time, TimeUnit.MILLISECONDS);
        }

        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public Set<URL> getBinaries () {
            return this.binaries;
        }

        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public List<URL> getSources() {
            return this.sources;
        }

        @Override
        public void publish(LogRecord record) {
            String msg = record.getMessage();
            if (type == Type.BATCH) {
                if ("scanBinary".equals(msg)) {
                    @SuppressWarnings("unchecked")
                    Set<URL> b = (Set<URL>) record.getParameters()[0];
                    binaries = b;
                    latch.countDown();
                }
                else if ("scanSources".equals(msg)) {
                    @SuppressWarnings("unchecked")
                    List<URL> s =(List<URL>) record.getParameters()[0];
                    sources = s;
                    latch.countDown();
                }
            } else if (type == Type.DELETE) {
                if ("delete".equals(msg)) {
                    latch.countDown();
                }
            } else if (type == Type.FILELIST) {
                if ("filelist".equals(msg)) {
                    latch.countDown();
                }
            } else if (type == Type.ROOTS_WORK_FINISHED) {
                if ("RootsWork-finished".equals(msg)) { //NOI18N
                    latch.countDown();
                }
            } else if (type == Type.BINARY) {
                if ("binary".equals(msg)) { //NOI18N
                    latch.countDown();
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

    public static class PRI implements FilteringPathResourceImplementation {

        private final URL root;
        private final PropertyChangeSupport support;

        public PRI (URL root) {
            this.root = root;
            this.support = new PropertyChangeSupport (this);
        }

        @Override
        public boolean includes(URL root, String resource) {
            return true;
        }

        @Override
        public URL[] getRoots() {
            return new URL[] {root};
        }

        @Override
        public ClassPathImplementation getContent() {
            return null;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            this.support.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            this.support.removePropertyChangeListener(listener);
        }

        public void firePropertyChange (final Object propId) {
            PropertyChangeEvent event = new PropertyChangeEvent (this,FilteringPathResourceImplementation.PROP_INCLUDES,null,null);
            event.setPropagationId(propId);
            this.support.firePropertyChange(event);
        }
    }


    public static class MutableClassPathImplementation implements ClassPathImplementation {

        private final List<PathResourceImplementation> res;
        private final PropertyChangeSupport support;

        public MutableClassPathImplementation () {
            res = new ArrayList<> ();
            support = new PropertyChangeSupport (this);
        }

        public void addResource (FileObject... fos) throws IOException {
            synchronized (res) {
                for(FileObject f : fos) {
                    res.add(ClassPathSupport.createResource(f.toURL()));
                }
            }
            this.support.firePropertyChange(PROP_RESOURCES,null,null);
        }

        public void addFilteredResource (FileObject root, String... excludes) throws IOException {
            synchronized (res) {
                res.add(new SimpleFilteringResourceImpl(root.toURL(), excludes));
            }
        }

        public void removeResource (FileObject... fos) throws IOException {
            boolean fire = false;

            synchronized (res) {
                for(FileObject f : fos) {
                    URL url = f.toURL();
                    for (Iterator<PathResourceImplementation> it = res.iterator(); it.hasNext(); ) {
                        PathResourceImplementation r = it.next();
                        if (url.equals(r.getRoots()[0])) {
                            it.remove();
                            fire = true;
                        }
                    }
                }
            }

            if (fire) {
                this.support.firePropertyChange(PROP_RESOURCES, null, null);
            }
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            support.addPropertyChangeListener(listener);
        }

        @Override
        public List<? extends PathResourceImplementation> getResources() {
            synchronized (res) {
                return new LinkedList<>(res);
            }
        }

    }

    private static final class SimpleFilteringResourceImpl extends PathResourceBase implements FilteringPathResourceImplementation {

        private final URL[] url;
        private final Pattern[] excludes;

        public SimpleFilteringResourceImpl(URL root, String... excludes) {
            this.url = new URL[] {root};
            this.excludes = new Pattern[excludes.length];
            for(int i = 0; i < excludes.length; i++) {
                this.excludes[i] = Pattern.compile(excludes[i]);
            }
        }

        @Override
        public URL[] getRoots() {
            return this.url;
        }

        @Override
        public ClassPathImplementation getContent() {
            return null;
        }

        @Override
        public boolean includes(URL root, String resource) {
            for(Pattern e : excludes) {
                if (e.matcher(resource).matches()) {
                    System.out.println("'" + resource + "' matches exclude pattern " + e.pattern());
                    return false;
                }
            }
            System.out.println("'" + resource + "' matches no exclude pattern");
            return true;
        }

        @Override
        public String toString () {
            return "FilteringResourceImpl{" + this.getRoots()[0] + "}";   //NOI18N
        }

        @Override
        public int hashCode () {
            int hash = this.url[0].hashCode();
            for(Pattern e : excludes) {
                hash += 7 * e.pattern().hashCode();
            }
            return hash;
        }

        @Override
        public boolean equals (Object other) {
            if (other instanceof SimpleFilteringResourceImpl) {
                SimpleFilteringResourceImpl opr = (SimpleFilteringResourceImpl) other;
                if (this.url[0].equals(opr.url[0])) {
                    if (excludes.length == opr.excludes.length) {
                        for(int i = 0; i < excludes.length; i++) {
                            if (!excludes[i].pattern().equals(opr.excludes[i].pattern())) {
                                return false;
                            }
                        }

                        return true;
                    }
                }
            }

            return false;
        }
    }

    private static final class MutableFilteringResourceImpl extends PathResourceBase implements FilteringPathResourceImplementation {

        private final URL[] url;
        private final List<Pattern> excludes;

        public MutableFilteringResourceImpl(URL root) {
            this.url = new URL[] {root};
            this.excludes = new ArrayList<>();
        }

        public synchronized void setExcludes(String... patterns) {
            excludes.clear();

            for (String p : patterns) {
                excludes.add(Pattern.compile(p));
            }

            firePropertyChange(PROP_INCLUDES, null, null);
        }

        public URL[] getRoots() {
            return this.url;
        }

        public ClassPathImplementation getContent() {
            return null;
        }

        @Override
        public synchronized boolean includes(URL root, String resource) {
            for(Pattern e : excludes) {
                if (e.matcher(resource).matches()) {
                    System.out.println("'" + resource + "' matches exclude pattern " + e.pattern());
                    return false;
                }
            }
            System.out.println("'" + resource + "' matches no exclude pattern");
            return true;
        }

        @Override
        public String toString () {
            return "FilteringResourceImpl{" + this.getRoots()[0] + "}";   //NOI18N
        }

        @Override
        public int hashCode () {
            int hash = this.url[0].hashCode();
            for(Pattern e : excludes) {
                hash += 7 * e.pattern().hashCode();
            }
            return hash;
        }

        @Override
        public boolean equals (Object other) {
            if (other instanceof MutableFilteringResourceImpl) {
                MutableFilteringResourceImpl opr = (MutableFilteringResourceImpl) other;
                if (!this.url[0].equals(opr.url[0]) || !excludes.equals(opr.excludes)) {
                    return false;
                }
            }

            return false;
        }
    }

    public static class SFBQImpl implements SourceForBinaryQueryImplementation {

        static final Map<URL,FileObject> map = new HashMap<> ();
        static final Map<URL,Result> results = new HashMap<> ();

        public SFBQImpl () {

        }

        public static void register (FileObject binRoot, FileObject sourceRoot) throws IOException {
            URL url = binRoot.toURL();
            map.put (url,sourceRoot);
            Result r = results.get (url);
            if (r != null) {
                r.update (sourceRoot);
            }
        }

        public static void unregister (FileObject binRoot) throws IOException {
            URL url = binRoot.toURL();
            map.remove(url);
            Result r = results.get (url);
            if (r != null) {
                r.update (null);
            }
        }

        public static void clean () {
            map.clear();
            results.clear();
        }

        @Override
        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            FileObject srcRoot = map.get(binaryRoot);
            if (srcRoot == null) {
                return null;
            }
            Result r = results.get (binaryRoot);
            if (r == null) {
                r = new Result (srcRoot);
                results.put(binaryRoot, r);
            }
            return r;
        }

        public static class Result implements SourceForBinaryQuery.Result {

            private FileObject root;
            private final List<ChangeListener> listeners;

            public Result (FileObject root) {
                this.root = root;
                this.listeners = new LinkedList<> ();
            }

            public void update (FileObject root) {
                this.root = root;
                fireChange ();
            }

            @Override
            public synchronized void addChangeListener(ChangeListener l) {
                this.listeners.add(l);
            }

            @Override
            public synchronized void removeChangeListener(ChangeListener l) {
                this.listeners.remove(l);
            }

            public @Override FileObject[] getRoots() {
                if (this.root == null) {
                    return new FileObject[0];
                }
                else {
                    return new FileObject[] {this.root};
                }
            }

            private void fireChange () {
                ChangeListener[] _listeners;
                synchronized (this) {
                    _listeners = this.listeners.toArray(new ChangeListener[0]);
                }
                ChangeEvent event = new ChangeEvent (this);
                for (ChangeListener l : _listeners) {
                    l.stateChanged (event);
                }
            }
        }

    }


    public static class FooPathRecognizer extends PathRecognizer {

        @Override
        public Set<String> getSourcePathIds() {
            return Collections.singleton(SOURCES);
        }

        @Override
        public Set<String> getBinaryLibraryPathIds() {
            final Set<String> res = new HashSet<>();
            res.add(PLATFORM);
            res.add(LIBS);
            return res;
        }

        @Override
        public Set<String> getLibraryPathIds() {
            return null;
        }

        @Override
        public Set<String> getMimeTypes() {
            return Collections.singleton(MIME);
        }

    }

    public static class EmbPathRecognizer extends PathRecognizer {

        @Override
        public Set<String> getSourcePathIds() {
            return Collections.singleton(SOURCES);
        }

        @Override
        public Set<String> getBinaryLibraryPathIds() {
            final Set<String> res = new HashSet<>();
            res.add(PLATFORM);
            res.add(LIBS);
            return res;
        }

        @Override
        public Set<String> getLibraryPathIds() {
            return null;
        }

        @Override
        public Set<String> getMimeTypes() {
            return Collections.singleton(EMIME);
        }

    }

    private static class BinIndexerFactory extends BinaryIndexerFactory {

        private final BinIndexer indexer = new BinIndexer();
        final List<URL> scanStartedFor = new LinkedList<>();
        final List<URL> scanFinishedFor = new LinkedList<>();

        @Override
        public BinaryIndexer createIndexer() {
            return this.indexer;
        }

        @Override
        public void rootsRemoved(final Iterable<? extends URL> rr) {

        }

        @Override
        public String getIndexerName() {
            return "jar";
        }

        @Override
        public int getIndexVersion() {
            return 1;
        }

        @Override
        public boolean scanStarted(final Context ctx) {
            scanStartedFor.add(ctx.getRootURI());
            return true;
        }

        @Override
        public void scanFinished(final Context ctx) {
            scanFinishedFor.add(ctx.getRootURI());
        }
    }

    private static class BinIndexer extends BinaryIndexer {

        private Set<URL> expectedRoots = new HashSet<>();
        private final Set<URL> indexedAllFilesIndexing = new HashSet<>();
        private CountDownLatch latch;
        private volatile int counter;

        public void setExpectedRoots (URL... roots) {
            expectedRoots.clear();
            expectedRoots.addAll(Arrays.asList(roots));
            counter = 0;
            latch = new CountDownLatch(expectedRoots.size());
        }

        public boolean await () throws InterruptedException {
            return this.latch.await(TIME, TimeUnit.MILLISECONDS);
        }

        public int getCount () {
            return this.counter;
        }

        @Override
        protected void index(Context context) {
            if (expectedRoots.remove(context.getRootURI())) {
                counter++;
                latch.countDown();
            }

            if (context.isAllFilesIndexing()) {
                indexedAllFilesIndexing.add(context.getRootURI());
            }
        }
    }

    private static class FooIndexerFactory extends CustomIndexerFactory {

        final List<URL> scanStartedFor = new LinkedList<>();
        final List<URL> scanFinishedFor = new LinkedList<>();

        private final FooIndexer indexer = new FooIndexer();

        @Override
        public CustomIndexer createIndexer() {
            return this.indexer;
        }

        @Override
        public String getIndexerName() {
            return "foo";
        }

        @Override
        public int getIndexVersion() {
            return 1;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            for (Indexable i : deleted) {
                //System.out.println("FooIndexerFactory.filesDeleted: " + i.getURL());
                indexer.deletedCounter++;
                if (indexer.expectedDeleted.remove(i.getURL())) {
                    indexer.deletedFilesLatch.countDown();
                }
                indexer.log.append("DELETE: " + i.getURL() + "\n");
            }
        }

        @Override
        public void rootsRemoved(final Iterable<? extends URL> rr) {

        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            for (Indexable i : dirty) {
                //System.out.println("FooIndexerFactory.filesDirty: " + i.getURL());
                indexer.dirtyCounter++;
                if (indexer.expectedDirty.remove(i.getURL())) {
                    indexer.dirtyFilesLatch.countDown();
                }
            }
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return false;
        }

        @Override
        public boolean scanStarted(final Context ctx) {
            scanStartedFor.add(ctx.getRootURI());
            return true;
        }

        @Override
        public void scanFinished(final Context ctx) {
            scanFinishedFor.add(ctx.getRootURI());
        }
    }

    private static class FooIndexer extends CustomIndexer {

        private Set<URL> expectedIndex = new HashSet<>();
        private CountDownLatch indexFilesLatch;
        private CountDownLatch deletedFilesLatch;
        private CountDownLatch dirtyFilesLatch;
        private volatile int indexCounter;
        private volatile int deletedCounter;
        private volatile int dirtyCounter;
        private final Set<URL> expectedDeleted = new HashSet<>();
        private final Set<URL> expectedDirty = new HashSet<>();
        private final Map<URL,Pair<Boolean,Boolean>> contextState = new HashMap<>();
        private Runnable preCallBack;
        private Runnable postCallBack;
        private final StringBuilder log = new StringBuilder();

        public void setExpectedFile (URL[] files, URL[] deleted, URL[] dirty) {
            expectedIndex.clear();
            expectedIndex.addAll(Arrays.asList(files));
            expectedDeleted.clear();
            expectedDeleted.addAll(Arrays.asList(deleted));
            expectedDirty.clear();
            expectedDirty.addAll(Arrays.asList(dirty));
            contextState.clear();
            indexCounter = 0;
            deletedCounter = 0;
            dirtyCounter = 0;
            indexFilesLatch = new CountDownLatch(expectedIndex.size());
            deletedFilesLatch = new CountDownLatch(expectedDeleted.size());
            dirtyFilesLatch = new CountDownLatch(expectedDirty.size());
            log.delete(0, log.length());
        }

        public void setPreCallBack(final Runnable callBack) {
            this.preCallBack = callBack;
        }

        public void setPostCallBack (final Runnable callBack) {
            this.postCallBack = callBack;
        }

        public boolean awaitIndex(final long time) throws InterruptedException {
            return this.indexFilesLatch.await(time, TimeUnit.MILLISECONDS);
        }

        public boolean awaitDeleted(final long time) throws InterruptedException {
            return this.deletedFilesLatch.await(time, TimeUnit.MILLISECONDS);
        }

        public boolean awaitDirty() throws InterruptedException {
            return this.dirtyFilesLatch.await(TIME, TimeUnit.MILLISECONDS);
        }

        public int getIndexCount() {
            return this.indexCounter;
        }

        public int getDeletedCount() {
            return this.deletedCounter;
        }

        public int getDirtyCount() {
            return this.dirtyCounter;
        }

        public Map<URL,Pair<Boolean,Boolean>> getContextState() {
            return this.contextState;
        }

        @Override
        protected void index(Iterable<? extends Indexable> files, Context context) {
            if (preCallBack != null) {
                preCallBack.run();
                preCallBack = null;
            }
            contextState.put(context.getRootURI(),Pair.<Boolean,Boolean>of(context.isAllFilesIndexing(),context.checkForEditorModifications()));
            for (Indexable i : files) {
                indexCounter++;
                if (expectedIndex.remove(i.getURL())) {
                    indexFilesLatch.countDown();
                }
                log.append("INDEX: " + i.getURL() + "\n");
            }
            if (postCallBack != null) {
                postCallBack.run();
                postCallBack = null;
            }
        }
    }

    private static final class FooExceptionIndexerFactory extends CustomIndexerFactory {

        private final Set<URI> finishedRoots = new HashSet<>();

        @Override
        public CustomIndexer createIndexer() {
            return new CustomIndexer() {
                @Override
                protected void index(Iterable<? extends Indexable> files, Context context) {
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
            return "fooexcp";   //NOI18N
        }

        @Override
        public int getIndexVersion() {
            return 1;
        }

        @Override
        public boolean scanStarted(Context context) {
            throw new NullPointerException();
        }

        @Override
        public void scanFinished(Context context) {
            try {
                finishedRoots.add(context.getRootURI().toURI());
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    private static final class FooQueryIndexerFactory extends CustomIndexerFactory {

        private final AtomicInteger maxDepth = new AtomicInteger();

        @Override
        public CustomIndexer createIndexer() {
            return new CustomIndexer() {
                @Override
                protected void index(
                        @NonNull final Iterable<? extends Indexable> files,
                        @NonNull final Context context) {
                    assertTrue(RunWhenScanFinishedSupport.isScanningThread());
                    if (maxDepth.getAndIncrement() == 0) {
                        try {
                            LayeredDocumentIndex index = LuceneIndexFactory.getDefault().createIndex(context);
                            index.markKeyDirty(files.iterator().next().getRelativePath());
                            final QuerySupport qs = QuerySupport.forRoots(
                                    getIndexerName(),
                                    getIndexVersion(),
                                    context.getRoot());
                            qs.query(
                                "name", //NOI18N
                                "",     //NOI18N
                                QuerySupport.Kind.PREFIX);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
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
            return "fooquery";
        }

        @Override
        public int getIndexVersion() {
            return 1;
        }

    }

    private static class EmbIndexerFactory extends EmbeddingIndexerFactory {

        private EmbIndexer indexer = new EmbIndexer ();

        final List<URL> scanStartedFor = new LinkedList<>();
        final List<URL> scanFinishedFor = new LinkedList<>();

        @Override
        public EmbeddingIndexer createIndexer(final Indexable indexable, final Snapshot snapshot) {
            return indexer;
        }

        @Override
        public String getIndexerName() {
            return "emb";
        }

        @Override
        public int getIndexVersion() {
            return 1;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            for (Indexable i : deleted) {
                //System.out.println("EmbIndexerFactory.filesDeleted: " + i.getURL());
                indexer.deletedCounter++;
                if (indexer.expectedDeleted.remove(i.getURL())) {
                    final String relPath = i.getRelativePath();
                    if (relPath.charAt(0) == '/') {
                        indexer.broken = true;
                    }
                    indexer.deletedFilesLatch.countDown();
                }
            }
        }

        @Override
        public void rootsRemoved(final Iterable<? extends URL> removedRoots) {

        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            for (Indexable i : dirty) {
                //System.out.println("EmbIndexerFactory.filesDirty: " + i.getURL());
                indexer.dirtyCounter++;
                if (indexer.expectedDirty.remove(i.getURL())) {
                    indexer.dirtyFilesLatch.countDown();
                }
            }
        }

        @Override
        public boolean scanStarted(final Context ctx) {
            scanStartedFor.add(ctx.getRootURI());
            return true;
        }

        @Override
        public void scanFinished(final Context ctx) {
            scanFinishedFor.add(ctx.getRootURI());
        }
    }

    private static class EmbIndexer extends EmbeddingIndexer {

        private Set<URL> expectedIndex = new HashSet<>();
        private CountDownLatch indexFilesLatch;
        private CountDownLatch deletedFilesLatch;
        private CountDownLatch dirtyFilesLatch;
        private volatile int indexCounter;
        private volatile int deletedCounter;
        private volatile int dirtyCounter;
        private Set<URL> expectedDeleted = new HashSet<>();
        private Set<URL> expectedDirty = new HashSet<>();
        private final Map<URL,Pair<Boolean,Boolean>> contextState = new HashMap<>();
        private boolean broken;

        public void setExpectedFile (URL[] files, URL[] deleted, URL[] dirty) {
            broken = false;
            expectedIndex.clear();
            expectedIndex.addAll(Arrays.asList(files));
            expectedDeleted.clear();
            expectedDeleted.addAll(Arrays.asList(deleted));
            expectedDirty.clear();
            expectedDirty.addAll(Arrays.asList(dirty));
            contextState.clear();
            indexCounter = 0;
            deletedCounter = 0;
            dirtyCounter = 0;
            indexFilesLatch = new CountDownLatch(expectedIndex.size());
            deletedFilesLatch = new CountDownLatch(expectedDeleted.size());
            dirtyFilesLatch = new CountDownLatch(expectedDirty.size());
        }

        public boolean awaitIndex() throws InterruptedException {
            return this.indexFilesLatch.await(TIME, TimeUnit.MILLISECONDS);
        }

        public boolean awaitDeleted() throws InterruptedException {
            return this.deletedFilesLatch.await(TIME, TimeUnit.MILLISECONDS);
        }

        public boolean awaitDirty() throws InterruptedException {
            return this.dirtyFilesLatch.await(TIME, TimeUnit.MILLISECONDS);
        }

        public int getIndexCount() {
            return this.indexCounter;
        }

        public int getDeletedCount() {
            return this.deletedCounter;
        }

        public int getDirtyCount() {
            return this.dirtyCounter;
        }

        public Map<URL,Pair<Boolean,Boolean>> getContextState() {
            return contextState;
        }

        @Override
        protected void index(Indexable indexable, Result parserResult, Context context) {
            final URL url = parserResult.getSnapshot().getSource().getFileObject().toURL();
            //System.out.println("EmbIndexer.index: " + url);
            indexCounter++;
            if (expectedIndex.remove(url)) {
                contextState.put(url, Pair.<Boolean,Boolean>of(context.isAllFilesIndexing(),context.checkForEditorModifications()));
                indexFilesLatch.countDown();
            }
        }

    }

    private static class EmbParserFactory extends ParserFactory {

        @Override
        public Parser createParser(Collection<Snapshot> snapshots) {
            return new EmbParser();
        }

    }

    private static class EmbParser extends Parser {

        private EmbResult result;

        @Override
        public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
            result = new EmbResult(snapshot);
        }

        @Override
        public Result getResult(Task task) throws ParseException {
            return result;
        }

        @Override
        public void cancel() {

        }

        @Override
        public void addChangeListener(ChangeListener changeListener) {

        }

        @Override
        public void removeChangeListener(ChangeListener changeListener) {

        }

    }

    private static class EmbResult extends Parser.Result {

        public EmbResult(final Snapshot snapshot) {
            super(snapshot);
        }


        @Override
        protected void invalidate() {
        }

    }

    public static final class ClassPathProviderImpl implements ClassPathProvider {

        private static final Map<Entry<FileObject, String>, ClassPath> classPathSpec = new HashMap<>();

        public static void reset() {
            classPathSpec.clear();
        }

        public static void register(FileObject root, String type, ClassPath cp) {
            classPathSpec.put(new SimpleEntry<>(root, type), cp);
        }

        public static void unregister(FileObject root, String type) {
            classPathSpec.remove(new SimpleEntry<>(root, type));
        }

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            while (!file.isRoot()) {
                for (Entry<Entry<FileObject, String>, ClassPath> e : classPathSpec.entrySet()) {
                    if (e.getKey().getKey() == file && e.getKey().getValue().equals(type)) {
                        return e.getValue();
                    }
                }
                file = file.getParent();
            }

            return null;
        }

    }

    private static class URLComparator implements Comparator<URL> {

        @Override
        public int compare(URL o1, URL o2) {
            return o1.toExternalForm().compareTo(o2.toExternalForm());
        }

    }

    public static class Visibility implements VisibilityQueryImplementation2 {

        private final Collection<FileObject> invisible =
                Collections.synchronizedList( new ArrayList<FileObject>());

        private final ChangeSupport changeSupport = new ChangeSupport(this);

        public Visibility() {
        }

        public void registerInvisibles (final Collection<? extends FileObject> invisibles) {
            synchronized (invisible) {
                invisible.clear();
                invisible.addAll(invisibles);
            }
            changeSupport.fireChange();
        }

        @Override
        public boolean isVisible(File file) {
            return isVisible(FileUtil.toFileObject(file));
        }

        @Override
        public boolean isVisible(FileObject file) {
            return !invisible.contains(file);
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }
    }
    
    public static class Indexability implements IndexabilityQueryImplementation {

        private final Collection<FileObject> invisible =
                Collections.synchronizedList( new ArrayList<>());

        private final Collection<String> blockedIndexers =
                Collections.synchronizedList( new ArrayList<>());
        
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        public Indexability() {
        }

        public void registerInvisibles (final Collection<? extends FileObject> invisibles) {
            synchronized (invisible) {
                invisible.clear();
                invisible.addAll(invisibles);
            }
            changeSupport.fireChange();
        }
        
        public void registerInvisiblesByIndexer (final Collection<? extends String> blockedIndexersNew) {
            synchronized (blockedIndexers) {
                blockedIndexers.clear();
                blockedIndexers.addAll(blockedIndexersNew);
            }
            changeSupport.fireChange();
        }

        @Override
        public boolean preventIndexing(IndexabilityQueryContext iqc) {
            return invisible.contains(URLMapper.findFileObject(iqc.getIndexable()))
                    || blockedIndexers.contains(iqc.getIndexerName());
        }
        
        @Override
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        @Override
        public String getName() {
            return getClass().getName();
        }

        @Override
        public int getVersion() {
            return 1;
        }

        @Override
        public String getStateIdentifier() {
            return "NO_CONFIGURATION";
        }
    }

    public static class IndexDownloaderImpl implements IndexDownloader {

        private static final Lock lck = new ReentrantLock();
        private static final Condition cnd = lck.newCondition();
        /*@GuardedBy("lck")*/
        private static URL expectedURL;
        /*@GuardedBy("lck")*/
        private static URL indexURL;

        @Override
        public URL getIndexURL(URL root) {
            URL index = null;
            lck.lock();
            try {
                if (root.equals(expectedURL)) {
                    index = indexURL;
                    expectedURL = null;
                    cnd.signal();
                }
            } finally {
                lck.unlock();
            }
            return index;
        }

        public static void expect(URL url, URL index) {
            lck.lock();
            try {
                expectedURL = url;
                indexURL = index;
            } finally {
                lck.unlock();
            }
        }

        public static boolean await(final long time) throws InterruptedException {
            lck.lock();
            try {
                if (expectedURL != null) {
                    return cnd.await(time, TimeUnit.MILLISECONDS) && expectedURL == null;
                } else {
                    return true;
                }
            } finally {
                lck.unlock();
            }
        }
    }

    public static class IndexPatcherImpl implements DownloadedIndexPatcher {

        private static final Lock lck = new ReentrantLock();
        private static final Condition cnd = lck.newCondition();
        //@GuardedBy("lck")
        private static URL expectedSourceRoot;
        //@GuardedBy("lck")
        private static boolean expectedVote;

        public static void expect (final URL sourceRoot, final boolean vote) {
            lck.lock();
            try {
                expectedSourceRoot = sourceRoot;
                expectedVote = vote;
            } finally {
                lck.unlock();
            }
        }

        public static boolean await() throws InterruptedException {
            lck.lock();
            try {
                if (expectedSourceRoot != null) {
                    return cnd.await(TIME, TimeUnit.MILLISECONDS) && expectedSourceRoot == null;
                } else {
                    return true;
                }
            } finally {
                lck.unlock();
            }
        }

        @Override
        public boolean updateIndex(URL sourceRoot, URL indexFolder) {
            boolean vote = true;
            lck.lock();
            try {
                if (sourceRoot.equals(expectedSourceRoot)) {
                    expectedSourceRoot = null;
                    vote = expectedVote;
                    cnd.signal();
                }
            } finally {
                lck.unlock();
            }
            return vote;
        }
    }

    private static void touch (final URL... urls) throws IOException {
        for(URL url : urls) {
            final FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                final OutputStream out = fo.getOutputStream();
                try {
                } finally {
                    out.close();
                }
            }
        }
    }

    /**
     * Waits for file system mtime changes
     */
    private static void fsWait() throws InterruptedException {
        Thread.sleep(3000);
    }

    //</editor-fold>
}
