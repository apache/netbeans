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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import static org.netbeans.modules.parsing.impl.indexing.FooPathRecognizer.FOO_EXT;
import static org.netbeans.modules.parsing.impl.indexing.FooPathRecognizer.FOO_MIME;
import static org.netbeans.modules.parsing.impl.indexing.FooPathRecognizer.FOO_SOURCES;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.queries.VisibilityQueryChangeEvent;
import org.netbeans.spi.queries.VisibilityQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Zezula
 */
public class VisibilityChangeTest extends IndexingTestBase {

    private final Map<String, Map<ClassPath,Void>> registeredClasspaths = new HashMap<String, Map<ClassPath,Void>>();

    private FileObject src1;
    private FileObject src2;
    private FileObject src3;
    private FileObject src4;
    private FileObject src2fld;
    private FileObject outside;
    private FileObject src1file1;
    private FileObject src1file2;
    private FileObject src2file1;
    private FileObject src2file2;
    private FileObject src3file1;
    private FileObject src3file2;
    private FileObject src4file1;
    private FileObject src4file2;
    private ClassPath cp1;

    public VisibilityChangeTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void getAdditionalServices(List<Class> clazz) {
        super.getAdditionalServices(clazz); 
        clazz.add(MockVisibilityQuery.class);
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
        src3 = wdo.createFolder("src3");        //NOI18N
        assertNotNull(src3);
        src4 = wdo.createFolder("src4");        //NOI18N
        assertNotNull(src4);
        outside = wdo.createFolder("outside");    //NOI18N
        assertNotNull(outside);
        src1file1 = src1.createData("test", FOO_EXT);   //NOI18N
        assertNotNull(src1file1);
        src1file2 = src1.createData("test2", FOO_EXT);  //NOI18N
        assertNotNull(src1file2);
        src2fld = src2.createFolder("folder"); //NOI18N
        assertNotNull(src2fld);
        src2file1 = src2fld.createData("test", FOO_EXT);   //NOI18N
        assertNotNull(src2file1);
        src2file2 = src2fld.createData("test2", FOO_EXT);  //NOI18N
        assertNotNull(src2file2);
        src3file1 = src3.createData("test", FOO_EXT);   //NOI18N
        assertNotNull(src3file1);
        src3file2 = src3.createData("test2", FOO_EXT);  //NOI18N
        assertNotNull(src3file2);
        src4file1 = src4.createData("test", FOO_EXT);   //NOI18N
        assertNotNull(src4file1);
        src4file2 = src4.createData("test2", FOO_EXT);  //NOI18N
        assertNotNull(src4file2);

        FileUtil.setMIMEType(FOO_EXT, FOO_MIME);
        cp1 = ClassPathSupport.createClassPath(src1,src2,src3,src4);
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
     * Tests unspecific VisibilityChange.
     * No specific file (folder) given -> everything should be refreshed.
     */
    public void testGlobalVisibilityChange() throws InterruptedException, IOException {

        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCES).isEmpty());
        final RepositoryUpdaterTest.TestHandler handler = new RepositoryUpdaterTest.TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        try {
            globalPathRegistry_register(FOO_SOURCES,new ClassPath[]{cp1});
            assertTrue (handler.await());
            assertEquals(0, handler.getBinaries().size());
            assertEquals(4, handler.getSources().size());
            assertEquals(
                new URI[] {
                    src1file1.toURI(),
                    src1file2.toURI(),
                    src2file1.toURI(),
                    src2file2.toURI(),
                    src3file1.toURI(),
                    src3file2.toURI(),
                    src4file1.toURI(),
                    src4file2.toURI()
                },
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearIndexedFiles());
            assertEquals(
                new URI[0],
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearRemovedFiles());
            handler.reset();
            Lookup.getDefault().lookup(MockVisibilityQuery.class).globalChange();
            assertTrue (handler.await());
            assertEquals(0, handler.getBinaries().size());
            assertEquals(4, handler.getSources().size());
            assertEquals(
                new URI[0],
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearIndexedFiles());
            assertEquals(
                new URI[0],
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearRemovedFiles());
        } finally {
            logger.removeHandler(handler);
        }
    }

    /**
     * Tests specific VisibilityChange but outside of the source roots.
     * Change to file (folder) outside of source roots -> no scan.
     */
    public void testVisibilityChangeOutsideOfRoot() throws InterruptedException, IOException {

        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCES).isEmpty());
        final RepositoryUpdaterTest.TestHandler handler = new RepositoryUpdaterTest.TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        try {
            globalPathRegistry_register(FOO_SOURCES,new ClassPath[]{cp1});
            assertTrue (handler.await());
            assertEquals(0, handler.getBinaries().size());
            assertEquals(4, handler.getSources().size());
            assertEquals(
                new URI[] {
                    src1file1.toURI(),
                    src1file2.toURI(),
                    src2file1.toURI(),
                    src2file2.toURI(),
                    src3file1.toURI(),
                    src3file2.toURI(),
                    src4file1.toURI(),
                    src4file2.toURI()
                },
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearIndexedFiles());
            assertEquals(
                new URI[0],
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearRemovedFiles());
            handler.reset();
            Lookup.getDefault().lookup(MockVisibilityQuery.class).hide(outside);
            assertFalse(handler.await(RepositoryUpdaterTest.NEGATIVE_TIME));
            assertNull(handler.getBinaries());
            assertNull(handler.getSources());
        } finally {
            logger.removeHandler(handler);
        }
    }

    public void testVisibilityChangeInSingleRoot() throws InterruptedException, IOException {

        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCES).isEmpty());
        final RepositoryUpdaterTest.TestHandler handler = new RepositoryUpdaterTest.TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        try {
            globalPathRegistry_register(FOO_SOURCES,new ClassPath[]{cp1});
            assertTrue (handler.await());
            assertEquals(0, handler.getBinaries().size());
            assertEquals(4, handler.getSources().size());
            assertEquals(
                new URI[] {
                    src1file1.toURI(),
                    src1file2.toURI(),
                    src2file1.toURI(),
                    src2file2.toURI(),
                    src3file1.toURI(),
                    src3file2.toURI(),
                    src4file1.toURI(),
                    src4file2.toURI()
                },
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearIndexedFiles());
            assertEquals(
                new URI[0],
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearRemovedFiles());
            handler.reset(RepositoryUpdaterTest.TestHandler.Type.DELETE);
            Lookup.getDefault().lookup(MockVisibilityQuery.class).hide(src1file1);
            assertTrue (handler.await());
            assertEquals(
                new URI[0],
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearIndexedFiles());
            assertEquals(
                new URI[]{
                    src1file1.toURI()
                },
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearRemovedFiles());
            handler.reset(RepositoryUpdaterTest.TestHandler.Type.FILELIST);
            Lookup.getDefault().lookup(MockVisibilityQuery.class).show(src1file1);
            assertTrue (handler.await());
            assertEquals(
                new URI[]{
                    src1file1.toURI()
                },
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearIndexedFiles());
            assertEquals(
                new URI[0],
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearRemovedFiles());
        } finally {
            logger.removeHandler(handler);
        }
    }

    public void testMultipleVisibilityChangesInSingleRoot() throws InterruptedException, IOException {

        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCES).isEmpty());
        final RepositoryUpdaterTest.TestHandler handler = new RepositoryUpdaterTest.TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        try {
            globalPathRegistry_register(FOO_SOURCES,new ClassPath[]{cp1});
            assertTrue (handler.await());
            assertEquals(0, handler.getBinaries().size());
            assertEquals(4, handler.getSources().size());
            assertEquals(
                new URI[] {
                    src1file1.toURI(),
                    src1file2.toURI(),
                    src2file1.toURI(),
                    src2file2.toURI(),
                    src3file1.toURI(),
                    src3file2.toURI(),
                    src4file1.toURI(),
                    src4file2.toURI()
                },
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearIndexedFiles());
            assertEquals(
                new URI[0],
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearRemovedFiles());
            handler.reset(RepositoryUpdaterTest.TestHandler.Type.DELETE);
            Lookup.getDefault().lookup(MockVisibilityQuery.class).hide(src1file1, src1file2);
            assertTrue (handler.await());
            assertEquals(
                new URI[0],
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearIndexedFiles());
            assertEquals(
                new URI[]{
                    src1file1.toURI(),
                    src1file2.toURI()
                },
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearRemovedFiles());
            handler.reset(RepositoryUpdaterTest.TestHandler.Type.FILELIST);
            Lookup.getDefault().lookup(MockVisibilityQuery.class).show(src1file1, src1file2);
            assertTrue (handler.await());
            assertEquals(
                new URI[]{
                    src1file1.toURI(),
                    src1file2.toURI()
                },
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearIndexedFiles());
            assertEquals(
                new URI[0],
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearRemovedFiles());
        } finally {
            logger.removeHandler(handler);
        }
    }

    public void testFolderVisibilityChangeInSingleRoot() throws InterruptedException, IOException {

        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCES).isEmpty());
        final RepositoryUpdaterTest.TestHandler handler = new RepositoryUpdaterTest.TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        try {
            globalPathRegistry_register(FOO_SOURCES,new ClassPath[]{cp1});
            assertTrue (handler.await());
            assertEquals(0, handler.getBinaries().size());
            assertEquals(4, handler.getSources().size());
            assertEquals(
                new URI[] {
                    src1file1.toURI(),
                    src1file2.toURI(),
                    src2file1.toURI(),
                    src2file2.toURI(),
                    src3file1.toURI(),
                    src3file2.toURI(),
                    src4file1.toURI(),
                    src4file2.toURI()
                },
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearIndexedFiles());
            assertEquals(
                new URI[0],
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearRemovedFiles());
            handler.reset(RepositoryUpdaterTest.TestHandler.Type.DELETE);
            Lookup.getDefault().lookup(MockVisibilityQuery.class).hide(src2fld);
            assertTrue (handler.await());
            assertEquals(
                new URI[0],
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearIndexedFiles());
            assertEquals(
                new URI[]{
                    src2file1.toURI(),
                    src2file2.toURI()
                },
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearRemovedFiles());
            handler.reset(RepositoryUpdaterTest.TestHandler.Type.FILELIST);
            Lookup.getDefault().lookup(MockVisibilityQuery.class).show(src2fld);
            assertTrue (handler.await());
            assertEquals(
                new URI[]{
                    src2file1.toURI(),
                    src2file2.toURI()
                },
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearIndexedFiles());
            assertEquals(
                new URI[0],
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearRemovedFiles());
        } finally {
            logger.removeHandler(handler);
        }
    }

    public void testRootVisibilityChangeInSingleRoot() throws InterruptedException, IOException {

        assertTrue(GlobalPathRegistry.getDefault().getPaths(FOO_SOURCES).isEmpty());
        final RepositoryUpdaterTest.TestHandler handler = new RepositoryUpdaterTest.TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        try {
            globalPathRegistry_register(FOO_SOURCES,new ClassPath[]{cp1});
            assertTrue (handler.await());
            assertEquals(0, handler.getBinaries().size());
            assertEquals(4, handler.getSources().size());
            assertEquals(
                new URI[] {
                    src1file1.toURI(),
                    src1file2.toURI(),
                    src2file1.toURI(),
                    src2file2.toURI(),
                    src3file1.toURI(),
                    src3file2.toURI(),
                    src4file1.toURI(),
                    src4file2.toURI()
                },
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearIndexedFiles());
            assertEquals(
                new URI[0],
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearRemovedFiles());



            handler.reset(RepositoryUpdaterTest.TestHandler.Type.DELETE);
            Lookup.getDefault().lookup(MockVisibilityQuery.class).hide(src1);
            assertTrue (handler.await());
            assertEquals(
                new URI[0],
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearIndexedFiles());
            assertEquals(
                new URI[]{
                    src1file1.toURI(),
                    src1file2.toURI()
                },
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearRemovedFiles());
            handler.reset(RepositoryUpdaterTest.TestHandler.Type.FILELIST);
            Lookup.getDefault().lookup(MockVisibilityQuery.class).show(src1);
            assertTrue (handler.await());
            assertEquals(
                new URI[]{
                    src1file1.toURI(),
                    src1file2.toURI()
                },
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearIndexedFiles());
            assertEquals(
                new URI[0],
                MimeLookup.getLookup(MimePath.get(FOO_MIME)).lookup(FooIndexerFactory.class).clearRemovedFiles());
        } finally {
            logger.removeHandler(handler);
        }
    }
    
    private void assertEquals(URI[] expected, URI[] result) {
        final Set<URI> es = new HashSet<URI>(Arrays.asList(expected));
        final Set<URI> rs = new HashSet<URI>(Arrays.asList(result));
        assertEquals(es, rs);
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
        private final Set<URI> indexedFiles = Collections.synchronizedSet(new HashSet<URI>());
        private final Set<URI> removedFiles = Collections.synchronizedSet(new HashSet<URI>());

        URI[] clearIndexedFiles() {
            synchronized (indexedFiles) {
                try {
                    return indexedFiles.toArray(new URI[0]);
                } finally {
                    indexedFiles.clear();
                }
            }
        }

        URI[] clearRemovedFiles() {
            synchronized (removedFiles) {
                try {
                    return removedFiles.toArray(new URI[0]);
                } finally {
                    removedFiles.clear();
                }
            }
        }

        @Override
        public CustomIndexer createIndexer() {
            return new CustomIndexer() {
                @Override
                protected void index(Iterable<? extends Indexable> files, Context context) {
                    for (Indexable f : files) {
                        try {
                            indexedFiles.add(f.getURL().toURI());
                        } catch (URISyntaxException ex) {
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
            for (Indexable i : deleted) {
                try {
                    removedFiles.add(i.getURL().toURI());
                } catch (URISyntaxException ex) {
                    Exceptions.printStackTrace(ex);
                }
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

        @Override
        public void scanFinished(Context context) {
        }               
    }

    public static class MockVisibilityQuery implements VisibilityQueryImplementation2 {

        private final Set<FileObject> invisibles = Collections.synchronizedSet(new HashSet<FileObject>());
        private final List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();

        void hide(FileObject... fos) {
            synchronized (invisibles) {
                invisibles.addAll(Arrays.asList(fos));
            }
            fire(new VisibilityQueryChangeEvent(this, fos));
        }

        void show(FileObject... fos) {
            synchronized (invisibles) {
                invisibles.removeAll(Arrays.asList(fos));
            }
            fire(new VisibilityQueryChangeEvent(this, fos));
        }

        void globalChange() {
            invisibles.clear();
            fire(new ChangeEvent(this));
        }

        @Override
        public boolean isVisible(File file) {
            return isVisible(FileUtil.toFileObject(file));
        }

        @Override
        public boolean isVisible(FileObject file) {
            return !invisibles.contains(file);
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }

        private void fire(@NonNull final ChangeEvent e) {
            for (ChangeListener l : listeners) {
                l.stateChanged(e);
            }
        }

    }
}
