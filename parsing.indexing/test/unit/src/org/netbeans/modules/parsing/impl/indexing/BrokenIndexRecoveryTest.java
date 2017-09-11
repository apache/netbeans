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
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import static junit.framework.Assert.assertTrue;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.junit.RandomlyFails;
import static org.netbeans.modules.parsing.impl.indexing.FooPathRecognizer.FOO_EXT;
import static org.netbeans.modules.parsing.impl.indexing.FooPathRecognizer.FOO_MIME;
import static org.netbeans.modules.parsing.impl.indexing.FooPathRecognizer.FOO_SOURCES;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public class BrokenIndexRecoveryTest extends IndexingTestBase {

    private final Map<String, Map<ClassPath,Void>> registeredClasspaths = new HashMap<String, Map<ClassPath,Void>>();

    private FileObject src1;
    private FileObject src1file1;
    private FileObject src1file2;
    private ClassPath cp1;

    public BrokenIndexRecoveryTest(@NonNull final String name) {
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
        src1file1 = src1.createData("test", FOO_EXT);   //NOI18N
        assertNotNull(src1file1);
        src1file2 = src1.createData("test2", FOO_EXT);  //NOI18N
        assertNotNull(src1file2);

        FileUtil.setMIMEType(FOO_EXT, FOO_MIME);
        cp1 = ClassPathSupport.createClassPath(src1);
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
     * Tests that root with broken index is reindexed.
     */
    @RandomlyFails
    public void testRootReindexIfIndexBroken() throws InterruptedException, IOException {

        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCES).isEmpty());
        final RepositoryUpdaterTest.TestHandler handler = new RepositoryUpdaterTest.TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        try {
            globalPathRegistry_register(FOO_SOURCES, new ClassPath[]{cp1});
            assertTrue (handler.await());
            assertEquals(0, handler.getBinaries().size());
            assertEquals(1, handler.getSources().size());
            assertEquals(1,MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).getIndexingCount());
            QuerySupport qs = QuerySupport.forRoots(FooIndexerFactory.NAME, FooIndexerFactory.VERSION, src1);
            assertEquals(2, qs.query(FooIndexerFactory.KEY_PATH, "", QuerySupport.Kind.PREFIX, FooIndexerFactory.KEY_PATH).size());    //NOI18N

            handler.reset();
            globalPathRegistry_unregister(FOO_SOURCES, new ClassPath[]{cp1});
            assertTrue (handler.await());
            assertEquals(0, handler.getBinaries().size());
            assertEquals(0, handler.getSources().size());

            //touch files
            touch(src1file1);
            touch(src1file2);
            waitForFSMetadata();
            //Break the index by deleting some file from it
            final FileObject cacheFolder = CacheFolder.getDataFolder(src1.toURL());
            final FileObject indexFolder = cacheFolder.getFileObject(String.format("%s/%d/%d", FooIndexerFactory.NAME, FooIndexerFactory.VERSION, 1));
            FileObject delCandidate = null;
            for (FileObject fo : indexFolder.getChildren()) {
                if (fo.getName().startsWith("nb-lock")) {   //NOI18N
                    continue;
                }
                if (delCandidate == null || fo.getExt().equals("cfs")) {    //NOI18N
                    delCandidate = fo;
                }
            }            
            delCandidate.delete();

            handler.reset();
            MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).reset();
            globalPathRegistry_register(FOO_SOURCES, new ClassPath[]{cp1});
            assertTrue (handler.await());
            awaitRUSilence();
            assertEquals(2,MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).getIndexingCount()); // 2 = broken + recover
            qs = QuerySupport.forRoots(FooIndexerFactory.NAME, FooIndexerFactory.VERSION, src1);
            assertEquals(2, qs.query(FooIndexerFactory.KEY_PATH, "", QuerySupport.Kind.PREFIX, FooIndexerFactory.KEY_PATH).size());    //NOI18N
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

    protected final void globalPathRegistry_unregister(String id, ClassPath [] classpaths) {
        GlobalPathRegistry.getDefault().unregister(id, classpaths);
        final Map<ClassPath,Void> map = registeredClasspaths.get(id);
        if (map != null) {
            map.keySet().removeAll(Arrays.asList(classpaths));
        }
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

    private void touch (@NonNull FileObject fo) {
        FileUtil.toFile(fo).setLastModified(System.currentTimeMillis());
    }

    private void waitForFSMetadata() throws InterruptedException {
        Thread.sleep(4000);
    }



    private static class FooIndexerFactory extends CustomIndexerFactory {

        private static final String NAME = "FooIndexer";    //NOI18N
        private static final int VERSION = 1;
        private static final String KEY_PATH = "path";      //NOI18N
        private final AtomicInteger count = new AtomicInteger();        
        @Override
        public CustomIndexer createIndexer() {
            return new CustomIndexer() {
                @Override
                protected void index(Iterable<? extends Indexable> files, Context context) {
                    count.incrementAndGet();
                    try {
                        final IndexingSupport is = IndexingSupport.getInstance(context);
                        for (Indexable file : files) {
                            final IndexDocument doc = is.createDocument(file);
                            doc.addPair(KEY_PATH, file.getRelativePath(), true, true);
                            is.addDocument(doc);
                        }
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
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
