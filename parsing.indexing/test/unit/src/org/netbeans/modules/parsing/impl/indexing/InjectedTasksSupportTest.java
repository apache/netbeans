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
import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.impl.indexing.lucene.DocumentBasedIndexManager;
import org.netbeans.modules.parsing.lucene.support.DocumentIndexCache;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public class InjectedTasksSupportTest extends IndexingTestBase {

    private static final String KEY_NAME = "name";  //NOI18N
    private static final String KEY_EXT = "ext";    //NOI18N

    private static final String EXT_FOO = "foo";    //NOI18N
    private static final String EXT_BAR = "bar";    //NOI18N
    private static final String EXT_DMY = "dmy";    //NOI18N

    private static final String MIME_FOO = "text/x-foo";    //NOI18N
    private static final String MIME_BAR = "text/x-bar";    //NOI18N
    private static final String MIME_DMY = "text/x-dmy";    //NOI18N

    private static final String SOURCES = "foo-bar-dmy-src";     //NOI18N

    private final Map<String, Set<ClassPath>> registeredClasspaths = new HashMap<String, Set<ClassPath>>();

    private FileObject src1;
    private FileObject foo1;
    private FileObject foo2;
    private FileObject bar1;
    private FileObject bar2;
    private FileObject dmy1;
    private FileObject dmy2;
    private ClassPath cp1;


    public InjectedTasksSupportTest (@NonNull final String name) {
        super (name);
    }

    @Override
    protected void getAdditionalServices(List<Class> clazz) {
        clazz.add(FooBarDmyPathRecognizer.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final File wd = getWorkDir();
        final FileObject wdo = FileUtil.toFileObject(wd);
        assertNotNull("No masterfs", wdo);  //NOI18N

        final FileObject cache = wdo.createFolder("cache");  //NOI18N
        assertNotNull(cache);
        CacheFolder.setCacheFolder(cache);

        src1 = wdo.createFolder("src1");        //NOI18N
        assertNotNull(src1);
        foo1 = src1.createData("foo1",EXT_FOO); //NOI18N
        assertNotNull(foo1);
        foo2 = src1.createData("foo2",EXT_FOO); //NOI18N
        assertNotNull(foo2);
        bar1 = src1.createData("bar1",EXT_BAR); //NOI18N
        assertNotNull(bar1);
        bar2 = src1.createData("bar2",EXT_BAR); //NOI18N
        assertNotNull(bar2);
        dmy1 = src1.createData("dmy1",EXT_DMY); //NOI18N
        assertNotNull(dmy1);
        dmy2 = src1.createData("dmy2",EXT_DMY); //NOI18N
        assertNotNull(dmy2);
        FileUtil.setMIMEType(EXT_FOO, MIME_FOO);
        FileUtil.setMIMEType(EXT_BAR, MIME_BAR);
        FileUtil.setMIMEType(EXT_DMY, MIME_DMY);
        cp1 = ClassPathSupport.createClassPath(src1);

        MockMimeLookup.setInstances(MimePath.get(MIME_FOO), new CustomIndexerFactoryImpl(EXT_FOO, 1));
        MockMimeLookup.setInstances(MimePath.get(MIME_BAR), new CustomIndexerFactoryImpl(EXT_BAR, 1));
        MockMimeLookup.setInstances(MimePath.get(MIME_DMY), new CustomIndexerFactoryImpl(EXT_DMY, 1));
        RepositoryUpdaterTest.setMimeTypes(MIME_FOO, MIME_BAR, MIME_DMY);
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
                final Set<ClassPath> classpaths = registeredClasspaths.get(id);
                GlobalPathRegistry.getDefault().unregister(id, classpaths.toArray(new ClassPath[classpaths.size()]));
            }
            handler.await();
        } finally {
            logger.removeHandler(handler);
        }
        super.tearDown();
    }

    public void testInjectedStore() throws Exception {
        assertTrue(GlobalPathRegistry.getDefault().getPaths(SOURCES).isEmpty());
        final RepositoryUpdaterTest.TestHandler handler = new RepositoryUpdaterTest.TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests"); //NOI18N
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);

        final CIHandler ciHandler = new CIHandler();
        final Logger ciLogger = Logger.getLogger(ClusteredIndexables.class.getName());
        ciLogger.setLevel (Level.FINEST);
        ciLogger.addHandler(ciHandler);

        MimeLookup.getLookup(MimePath.get(MIME_BAR)).lookup(CustomIndexerFactoryImpl.class).postAction =
            new Callable<Void>() {
                @Override
                public Void call() throws Exception {                    
                    final DocumentIndexCache cache = findCache(EXT_FOO);
                    assertNotNull(cache);
                    final Class<?> c = Class.forName("org.netbeans.modules.parsing.impl.indexing.ClusteredIndexables$DocumentIndexCacheImpl");  //NOI18N
                    final Field field = c.getDeclaredField("dataRef");   //NOI18N
                    field.setAccessible(true);
                    final Reference r = (Reference) field.get(cache);
                    ciHandler.expect(CIHandler.Event.ENQUEUE);
                    r.clear();
                    r.enqueue();
                    assertTrue(ciHandler.await(RepositoryUpdaterTest.TIME));
                    ciHandler.expect(CIHandler.Event.EXEXUTE);  //Before next idexer runs the Injected task should be executed
                    return null;
                }                    
            };

        MimeLookup.getLookup(MimePath.get(MIME_DMY)).lookup(CustomIndexerFactoryImpl.class).preAction =
            new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    assertTrue(ciHandler.await(RepositoryUpdaterTest.TIME));
                    final DocumentIndexCache cache = findCache(EXT_FOO);
                    assertNotNull(cache);
                    final Class<?> c = Class.forName("org.netbeans.modules.parsing.impl.indexing.ClusteredIndexables$DocumentIndexCacheImpl");  //NOI18N
                    final Field field = c.getDeclaredField("toAdd");   //NOI18N
                    field.setAccessible(true);
                    assertNull(field.get(cache));   //Cache is TX stored
                    final QuerySupport qs = QuerySupport.forRoots(EXT_FOO, 1, src1);
                    final Collection<? extends IndexResult> res = qs.query("_sn", "", QuerySupport.Kind.PREFIX, (String[]) null);   //NOI18N
                    assertEquals(0, res.size());    //Cache is not TX commited
                    return null;
                }
            };

        globalPathRegistry_register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await(500000));
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.src1.toURL(), handler.getSources().get(0));
        QuerySupport qs = QuerySupport.forRoots(EXT_FOO, 1, src1);
        Collection<? extends IndexResult> res = qs.query("_sn", "", QuerySupport.Kind.PREFIX, (String[]) null);   //NOI18N
        assertEquals(2, res.size());
        qs = QuerySupport.forRoots(EXT_BAR, 1, src1);
        res = qs.query("_sn", "", QuerySupport.Kind.PREFIX, (String[]) null);   //NOI18N
        assertEquals(2, res.size());
        qs = QuerySupport.forRoots(EXT_DMY, 1, src1);
        res = qs.query("_sn", "", QuerySupport.Kind.PREFIX, (String[]) null);   //NOI18N
        assertEquals(2, res.size());
        
    }

    private void globalPathRegistry_register(String id, ClassPath [] classpaths) {
        Set<ClassPath> set = registeredClasspaths.get(id);
        if (set == null) {
            set = Collections.newSetFromMap(new IdentityHashMap<ClassPath, Boolean>());
            registeredClasspaths.put(id, set);
        }
        set.addAll(Arrays.asList(classpaths));
        GlobalPathRegistry.getDefault().register(id, classpaths);
    }

    @CheckForNull
    private DocumentIndexCache findCache (@NonNull final String indexerName) throws IOException {
        for (File f : IndexManager.getOpenIndexes().keySet()) {
            if (f.getParentFile().getParentFile().getName().equals(indexerName)) {
                return DocumentBasedIndexManager.getDefault().getCache(f.toURI().toURL());
            }
        }
        return null;
    }


    private static final class CustomIndexerImpl extends CustomIndexer {
        
        private final Callable<?> preAction;
        private final Callable<?> postAction;

        public CustomIndexerImpl(
                @NullAllowed final Callable<?> preAction,
                @NullAllowed final Callable<?> postAction) {
            this.preAction = preAction;
            this.postAction = postAction;
        }

        @Override
        protected void index(Iterable<? extends Indexable> files, Context context) {
            try {
                if (preAction != null) {
                    preAction.call();
                }
                final IndexingSupport is = IndexingSupport.getInstance(context);
                for (Indexable file : files) {
                    final IndexDocument doc = is.createDocument(file);
                    final FileObject fo = URLMapper.findFileObject(file.getURL());
                    doc.addPair(KEY_NAME, fo.getName(), true, true);
                    doc.addPair(KEY_EXT, fo.getExt(), true, true);
                    is.addDocument(doc);
                }
                if (postAction != null) {
                    postAction.call();
                }
            } catch (Exception ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

    }

    private static final class CustomIndexerFactoryImpl extends CustomIndexerFactory {

        private final String name;
        private final int version;

        volatile Callable<?> preAction;
        volatile Callable<?> postAction;

        CustomIndexerFactoryImpl(
            @NonNull final String name,
            final int version) {
            Parameters.notNull("name", name);   //NOI18N
            this.name = name;
            this.version = version;
        }


        @Override
        public CustomIndexer createIndexer() {
            return new CustomIndexerImpl(preAction, postAction);
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

    }

    public static final class FooBarDmyPathRecognizer extends PathRecognizer {

        @Override
        public Set<String> getSourcePathIds() {
            return Collections.<String>singleton(SOURCES);
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
            return Collections.<String>unmodifiableSet(new HashSet<String>(Arrays.asList(MIME_FOO, MIME_BAR, MIME_DMY)));
        }

    }

    private static final class CIHandler extends Handler {


        enum Event {
            ENQUEUE,
            EXEXUTE
        };
        
        private final Set<Event> expectedEvents = EnumSet.noneOf(Event.class);

        public synchronized void expect (@NonNull final Event... events) {
            expectedEvents.addAll(Arrays.asList(events));
        }

        @Override
        public synchronized void publish(LogRecord record) {
            final String message = record.getMessage();
            if (message == null) {
                return;
            }
            if (message.startsWith("Reference Task Enqueued")) {    //NOI18N
                expectedEvents.remove(Event.ENQUEUE);
            } else if (message.startsWith("Reference Task Executed")) { //NOI18N
                expectedEvents.remove(Event.EXEXUTE);
            }
            if (expectedEvents.isEmpty()) {
                notifyAll();
            }
        }

        public synchronized boolean await(final long millis) throws InterruptedException {
            final long start = System.currentTimeMillis();
            while (!expectedEvents.isEmpty()) {
                final long now = System.currentTimeMillis();
                final long delta = now - start;
                if (delta >= millis) {
                    return false;
                }
                wait(millis - delta);
            }
            return true;
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

    }
}
