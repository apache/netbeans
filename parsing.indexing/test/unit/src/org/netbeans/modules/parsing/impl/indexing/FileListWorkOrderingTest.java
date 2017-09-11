/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.parsing.impl.indexing;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import static org.netbeans.modules.parsing.impl.indexing.FooPathRecognizer.FOO_EXT;
import static org.netbeans.modules.parsing.impl.indexing.FooPathRecognizer.FOO_MIME;
import static org.netbeans.modules.parsing.impl.indexing.FooPathRecognizer.FOO_SOURCES;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public class FileListWorkOrderingTest extends IndexingTestBase {

    private final Map<String, Map<ClassPath,Void>> registeredClasspaths = new HashMap<String, Map<ClassPath,Void>>();

    private FileObject src1;
    private FileObject src2;
    private FileObject src1file1;
    private FileObject src1file2;
    private FileObject src2file1;
    private FileObject src2file2;
    private ClassPath cp1;

    public FileListWorkOrderingTest(@NonNull final String name) {
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
        src2 = wdo.createFolder("src2");        //NOI18N
        assertNotNull(src2);
        src1file1 = src1.createData("test", FOO_EXT);   //NOI18N
        assertNotNull(src1file1);
        src1file2 = src1.createData("test2", FOO_EXT);  //NOI18N
        assertNotNull(src1file2);
        src2file1 = src2.createData("test", FOO_EXT);   //NOI18N
        assertNotNull(src2file1);
        src2file2 = src2.createData("test2", FOO_EXT);  //NOI18N
        assertNotNull(src2file2);

        FileUtil.setMIMEType(FOO_EXT, FOO_MIME);
        cp1 = ClassPathSupport.createClassPath(src1,src2);
        MockMimeLookup.setInstances(MimePath.get(FOO_MIME), new FooIndexerFactory());
        RepositoryUpdaterTest.setMimeTypes(FOO_MIME);
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


    /**
     * Tests that consequent file list works on single root are collapsed into single one.
     * Runs special work at start which in execution schedules 2 FLWs -> just 1 FLW should
     * be executed.
     */
    public void testFLWOnSingleRootAreAbsorbed() throws InterruptedException, IOException {

        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCES).isEmpty());
        final RepositoryUpdaterTest.TestHandler handler = new RepositoryUpdaterTest.TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        try {
            globalPathRegistry_register(FOO_SOURCES,new ClassPath[]{cp1});
            assertTrue (handler.await());
            assertEquals(0, handler.getBinaries().size());
            assertEquals(2, handler.getSources().size());

            handler.reset(RepositoryUpdaterTest.TestHandler.Type.FILELIST);
            MimeLookup.getLookup(MimePath.get(FOO_MIME)).
                    lookup(FooIndexerFactory.class).
                    reset();
            RepositoryUpdater.getDefault().runAsWork(
                new Runnable() {
                   @Override
                   public void run() {
                       //First schedule of refresh for src1
                       IndexingManager.getDefault().refreshIndex(
                          src1.toURL(),
                          Collections.<URL>emptyList(),
                          false);
                       //Second schedule of refresh for src1
                       IndexingManager.getDefault().refreshIndex(
                          src1.toURL(),
                          Collections.<URL>emptyList(),
                          false);
                   }
                });              
            assertTrue (handler.await());
            assertTrue (awaitRUSilence());
            assertEquals(1, MimeLookup.getLookup(MimePath.get(FOO_MIME)).
                            lookup(FooIndexerFactory.class).
                            getIndexingCount());
        } finally {
            logger.removeHandler(handler);
        }
    }

    /**
     * Tests that consequent file list works on different roots are not collapsed into single one.
     * Runs special work at start which in execution schedules FLW(src1) and FLW(src2) -> 2 FLWs should
     * be executed.
     */
    public void testFLWOnDifferentRootsAreNotAbsorbed() throws InterruptedException, IOException {

        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCES).isEmpty());
        final RepositoryUpdaterTest.TestHandler handler = new RepositoryUpdaterTest.TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        try {
            globalPathRegistry_register(FOO_SOURCES,new ClassPath[]{cp1});
            assertTrue (handler.await());
            assertEquals(0, handler.getBinaries().size());
            assertEquals(2, handler.getSources().size());

            handler.reset(RepositoryUpdaterTest.TestHandler.Type.FILELIST);
            MimeLookup.getLookup(MimePath.get(FOO_MIME)).
                    lookup(FooIndexerFactory.class).
                    reset();
            RepositoryUpdater.getDefault().runAsWork(
                new Runnable() {
                   @Override
                   public void run() {
                       //First schedule of refresh for src1
                       IndexingManager.getDefault().refreshIndex(
                          src1.toURL(),
                          Collections.<URL>emptyList(),
                          false);
                       //Second schedule of refresh for src1
                       IndexingManager.getDefault().refreshIndex(
                          src2.toURL(),
                          Collections.<URL>emptyList(),
                          false);
                   }
                });
            assertTrue (handler.await());
            assertTrue (awaitRUSilence());
            assertEquals(2, MimeLookup.getLookup(MimePath.get(FOO_MIME)).
                            lookup(FooIndexerFactory.class).
                            getIndexingCount());
        } finally {
            logger.removeHandler(handler);
        }
    }

    /**
     * Tests that consequent file list works on single roots with intermediate delete work on different root
     * are collapsed into single one.
     * Runs special work at start which in execution schedules FLW(src1), DEL(src2), FLW(src1) -> 1 FLWs should
     * be executed.
     */
    public void testFLWOnSingleRootsWithintermediateDelWorkOnOtherRootAreAbsorbed() throws InterruptedException, IOException {

        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCES).isEmpty());
        final RepositoryUpdaterTest.TestHandler handler = new RepositoryUpdaterTest.TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        try {
            globalPathRegistry_register(FOO_SOURCES,new ClassPath[]{cp1});
            assertTrue (handler.await());
            assertEquals(0, handler.getBinaries().size());
            assertEquals(2, handler.getSources().size());

            handler.reset(RepositoryUpdaterTest.TestHandler.Type.FILELIST);
            MimeLookup.getLookup(MimePath.get(FOO_MIME)).
                    lookup(FooIndexerFactory.class).
                    reset();
            RepositoryUpdater.getDefault().runAsWork(
                new Runnable() {
                   @Override
                   public void run() {
                       //First schedule of refresh for src1
                       IndexingManager.getDefault().refreshIndex(
                          src1.toURL(),
                          Collections.<URL>emptyList(),
                          false);
                       //Second schedulte some delete in src2
                       RepositoryUpdater.getDefault().addDeleteJob(
                          src2.toURL(),
                          Collections.<String>emptySet(),
                          LogContext.create(LogContext.EventType.PATH, "Test"));    //NOI18N
                       //Third schedule of refresh for src1
                       IndexingManager.getDefault().refreshIndex(
                          src1.toURL(),
                          Collections.<URL>emptyList(),
                          false);
                   }
                });
            assertTrue (handler.await());
            assertTrue (awaitRUSilence());
            assertEquals(1, MimeLookup.getLookup(MimePath.get(FOO_MIME)).
                            lookup(FooIndexerFactory.class).
                            getIndexingCount());
        } finally {
            logger.removeHandler(handler);
        }
    }

    /**
     * Tests that consequent file list works on single roots with intermediate delete work on same root
     * are not collapsed into single one.
     * Runs special work at start which in execution schedules FLW(src1), DEL(src1), FLW(src1) -> 2 FLWs should
     * be executed.
     */
    public void testFLWOnSingleRootsWithintermediateDelWorkOnSameRootAreNotAbsorbed() throws InterruptedException, IOException {

        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCES).isEmpty());
        final RepositoryUpdaterTest.TestHandler handler = new RepositoryUpdaterTest.TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        try {
            globalPathRegistry_register(FOO_SOURCES,new ClassPath[]{cp1});
            assertTrue (handler.await());
            assertEquals(0, handler.getBinaries().size());
            assertEquals(2, handler.getSources().size());

            handler.reset(RepositoryUpdaterTest.TestHandler.Type.FILELIST);
            MimeLookup.getLookup(MimePath.get(FOO_MIME)).
                    lookup(FooIndexerFactory.class).
                    reset();
            RepositoryUpdater.getDefault().runAsWork(
                new Runnable() {
                   @Override
                   public void run() {
                       //First schedule of refresh for src1
                       IndexingManager.getDefault().refreshIndex(
                          src1.toURL(),
                          Collections.<URL>emptyList(),
                          false);
                       //Second schedulte some delete in src2
                       RepositoryUpdater.getDefault().addDeleteJob(
                          src1.toURL(),
                          Collections.<String>emptySet(),
                          LogContext.create(LogContext.EventType.PATH, "Test"));    //NOI18N
                       //Third schedule of refresh for src1
                       IndexingManager.getDefault().refreshIndex(
                          src1.toURL(),
                          Collections.<URL>emptyList(),
                          false);
                   }
                });
            assertTrue (handler.await());
            assertTrue (awaitRUSilence());
            assertEquals(2, MimeLookup.getLookup(MimePath.get(FOO_MIME)).
                            lookup(FooIndexerFactory.class).
                            getIndexingCount());
        } finally {
            logger.removeHandler(handler);
        }
    }

    /**
     * Tests that consequent file list works on single roots with intermediate delete work on same root
     * are collapsed if there is at least one FLW following the delete work.
     * Runs special work at start which in execution schedules FLW(src1), DEL(src1), FLW(src1), FLW(1) -> 2 FLWs should
     * be executed.
     */
    public void testFLWOnSingleRootsWithIntermediateDelWorkFollowedByFLWOnSameRootAreAbsorbed() throws InterruptedException, IOException {

        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCES).isEmpty());
        final RepositoryUpdaterTest.TestHandler handler = new RepositoryUpdaterTest.TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        try {
            globalPathRegistry_register(FOO_SOURCES,new ClassPath[]{cp1});
            assertTrue (handler.await());
            assertEquals(0, handler.getBinaries().size());
            assertEquals(2, handler.getSources().size());

            handler.reset(RepositoryUpdaterTest.TestHandler.Type.FILELIST);
            MimeLookup.getLookup(MimePath.get(FOO_MIME)).
                    lookup(FooIndexerFactory.class).
                    reset();
            RepositoryUpdater.getDefault().runAsWork(
                new Runnable() {
                   @Override
                   public void run() {
                       //First schedule of refresh for src1
                       IndexingManager.getDefault().refreshIndex(
                          src1.toURL(),
                          Collections.<URL>emptyList(),
                          false);
                       //Second schedulte some delete in src2
                       RepositoryUpdater.getDefault().addDeleteJob(
                          src1.toURL(),
                          Collections.<String>emptySet(),
                          LogContext.create(LogContext.EventType.PATH, "Test"));    //NOI18N
                       //Third schedule of refresh for src1
                       IndexingManager.getDefault().refreshIndex(
                          src1.toURL(),
                          Collections.<URL>emptyList(),
                          false);
                       //Fourth schedule of refresh for src1
                       IndexingManager.getDefault().refreshIndex(
                          src1.toURL(),
                          Collections.<URL>emptyList(),
                          false);
                   }
                });
            assertTrue (handler.await());
            assertTrue (awaitRUSilence());
            assertEquals(2, MimeLookup.getLookup(MimePath.get(FOO_MIME)).
                            lookup(FooIndexerFactory.class).
                            getIndexingCount());
        } finally {
            logger.removeHandler(handler);
        }
    }


    /**
     * Tests that consequent file list works on different roots with intermediate delete work on first root
     * are not collapsed.
     * Runs special work at start which in execution schedules FLW(src1), DEL(src1), FLW(src2), FLW(src1) -> 3 FLWs should
     * be executed.
     */
    public void testFLWOnMultipleRootsWithIntermediateDelWorkFollowedByFLWNotAbsorbed() throws InterruptedException, IOException {

        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCES).isEmpty());
        final RepositoryUpdaterTest.TestHandler handler = new RepositoryUpdaterTest.TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        try {
            globalPathRegistry_register(FOO_SOURCES,new ClassPath[]{cp1});
            assertTrue (handler.await());
            assertEquals(0, handler.getBinaries().size());
            assertEquals(2, handler.getSources().size());

            handler.reset(RepositoryUpdaterTest.TestHandler.Type.FILELIST);
            MimeLookup.getLookup(MimePath.get(FOO_MIME)).
                    lookup(FooIndexerFactory.class).
                    reset();
            RepositoryUpdater.getDefault().runAsWork(
                new Runnable() {
                   @Override
                   public void run() {
                       //First schedule of refresh for src1
                       IndexingManager.getDefault().refreshIndex(
                          src1.toURL(),
                          Collections.<URL>emptyList(),
                          false);
                       //Second schedulte some delete in src2
                       RepositoryUpdater.getDefault().addDeleteJob(
                          src1.toURL(),
                          Collections.<String>emptySet(),
                          LogContext.create(LogContext.EventType.PATH, "Test"));    //NOI18N
                       //Third schedule of refresh for src1
                       IndexingManager.getDefault().refreshIndex(
                          src2.toURL(),
                          Collections.<URL>emptyList(),
                          false);
                       //Fourth schedule of refresh for src1
                       IndexingManager.getDefault().refreshIndex(
                          src1.toURL(),
                          Collections.<URL>emptyList(),
                          false);
                   }
                });
            assertTrue (handler.await());
            assertTrue (awaitRUSilence());
            assertEquals(3, MimeLookup.getLookup(MimePath.get(FOO_MIME)).
                            lookup(FooIndexerFactory.class).
                            getIndexingCount());
        } finally {
            logger.removeHandler(handler);
        }
    }

    /**
     * Tests that consequent file list works on same root with intermediate delete works are collapsed.
     * Runs special work at start which in execution schedules FLW(src1), DEL(src1), FLW(src1), DEL(src1), FLW(src1)
     * FLW, DEL+DEL, FLW+FLW -> 2 FLWs should be executed.
     */
    public void testFLWOnSingleRootsWithIntermediateDelWorksFollowedByFLWNotAbsorbed() throws InterruptedException, IOException {

        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCES).isEmpty());
        final RepositoryUpdaterTest.TestHandler handler = new RepositoryUpdaterTest.TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        try {
            globalPathRegistry_register(FOO_SOURCES,new ClassPath[]{cp1});
            assertTrue (handler.await());
            assertEquals(0, handler.getBinaries().size());
            assertEquals(2, handler.getSources().size());

            handler.reset(RepositoryUpdaterTest.TestHandler.Type.FILELIST);
            MimeLookup.getLookup(MimePath.get(FOO_MIME)).
                    lookup(FooIndexerFactory.class).
                    reset();
            RepositoryUpdater.getDefault().runAsWork(
                new Runnable() {
                   @Override
                   public void run() {
                       //First schedule of refresh for src1
                       IndexingManager.getDefault().refreshIndex(
                          src1.toURL(),
                          Collections.<URL>emptyList(),
                          false);
                       //Second schedulte some delete in src2
                       RepositoryUpdater.getDefault().addDeleteJob(
                          src1.toURL(),
                          Collections.<String>emptySet(),
                          LogContext.create(LogContext.EventType.PATH, "Test"));    //NOI18N
                       //Third schedule of refresh for src1
                       IndexingManager.getDefault().refreshIndex(
                          src1.toURL(),
                          Collections.<URL>emptyList(),
                          false);
                       //Second schedulte some delete in src2
                       RepositoryUpdater.getDefault().addDeleteJob(
                          src1.toURL(),
                          Collections.<String>emptySet(),
                          LogContext.create(LogContext.EventType.PATH, "Test"));    //NOI18N
                       //Fourth schedule of refresh for src1
                       IndexingManager.getDefault().refreshIndex(
                          src1.toURL(),
                          Collections.<URL>emptyList(),
                          false);
                   }
                });
            assertTrue (handler.await());
            assertTrue (awaitRUSilence());
            assertEquals(2, MimeLookup.getLookup(MimePath.get(FOO_MIME)).
                            lookup(FooIndexerFactory.class).
                            getIndexingCount());
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

    private boolean awaitRUSilence() throws InterruptedException {
        final CountDownLatch cond = new CountDownLatch(1);
        RepositoryUpdater.getDefault().runAsWork(new Runnable() {
            @Override
            public void run() {
                cond.countDown();
            }
        });
        return cond.await(RepositoryUpdaterTest.TIME, TimeUnit.MILLISECONDS);
    }


    public static final class FooPathRecognizer extends PathRecognizer {

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
            return Collections.<String>emptySet();
        }

        @Override
        public Set<String> getMimeTypes() {
            return Collections.<String>singleton(FOO_MIME);
        }
    }

    private static class FooIndexerFactory extends CustomIndexerFactory {

        private static final String NAME = "FooIndexer";    //NOI18N
        private static final int VERSION = 1;
        private final AtomicInteger count = new AtomicInteger();        
        @Override
        public CustomIndexer createIndexer() {
            return new CustomIndexer() {
                @Override
                protected void index(Iterable<? extends Indexable> files, Context context) {
                    count.incrementAndGet();                    
                }
            };
        }

        FooIndexerFactory reset() {
            count.set(0);
            return this;
        }        

        int getIndexingCount() {
            return count.get();
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
            return NAME;
        }

        @Override
        public int getIndexVersion() {
            return VERSION;
        }

        @Override
        public void scanFinished(Context context) {
        }               
    }    
}
