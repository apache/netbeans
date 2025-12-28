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
import java.net.URL;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import static org.netbeans.modules.parsing.impl.indexing.FooPathRecognizer.FOO_MIME;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.ConstrainedBinaryIndexer;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public class ConstrainedBinaryIndexerTest extends IndexingTestBase {

    private static final String MIME_JAR = "application/java-archive";  //NOI18N
    private static final String PATH_LIB = "lib";   //NOI18N
    private static final Logger LOG = Logger.getLogger(ConstrainedBinaryIndexerTest.class.getName());

    private final Map<String,Set<ClassPath>> registeredClasspaths = new HashMap<String, Set<ClassPath>>();

    public ConstrainedBinaryIndexerTest(final String name) {
        super (name);
    }

    @Override
    protected void getAdditionalServices(List<Class> clazz) {
        clazz.add(Recognizer.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.clearWorkDir();
        final File _wd = this.getWorkDir();
        final FileObject wd = FileUtil.toFileObject(_wd);
        final FileObject cache = wd.createFolder("cache");
        CacheFolder.setCacheFolder(cache);

        assertNotNull("No masterfs",wd);        //NOI18N
        FileUtil.setMIMEType("jar", MIME_JAR);  //NOI18N
        FileUtil.setMIMEType("foo", FOO_MIME);  //NOI18N

        RepositoryUpdaterTest.waitForRepositoryUpdaterInit();
    }

    @Override
    protected void tearDown() throws Exception {
        for(Map.Entry<String, Set<ClassPath>> entry : registeredClasspaths.entrySet()) {
            String id = entry.getKey();
            Set<ClassPath> classpaths = entry.getValue();
            GlobalPathRegistry.getDefault().unregister(id, classpaths.toArray(new ClassPath[0]));
        }

        super.tearDown();
    }

    /**
     * Tests ConstrainedBinaryIndexer without any predicate.
     */
    public void testNoPredicates() throws Exception {

        final MockConstrainedIndexer indexer = new MockConstrainedIndexer();
        registerProxyBinaryIndexer(indexer,null,null,null);
        final URL root = createArchive (getWorkDir(), "nopred.jar", new String[0]); //NOI18N
        final ClassPath cp = ClassPathSupport.createClassPath(root);

        //Jar registered for first time (never seen before) scanStarted, index, scanFinished shoud be called.
        indexer.expect(MockConstrainedIndexer.Event.STARTED, MockConstrainedIndexer.Event.INDEXED, MockConstrainedIndexer.Event.FINISHED);
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));
        assertTrue(indexer.hasResources(Collections.<String,Set<String>>emptyMap()));

        //Jar unregistered rootsRemoved should be called
        indexer.expect(MockConstrainedIndexer.Event.REMOVED);
        globalPathRegistry_unregister(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar registered for second time (seen before) scanStarted should be called.
        //The ConstrainedBinaryIndexer accepted the jar in prev scan -> the scanStarted
        //has to be called to give a chance to indexer to force rescan of it when needed.
        indexer.expect(MockConstrainedIndexer.Event.STARTED);
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar unregistered rootsRemoved should be called
        indexer.expect(MockConstrainedIndexer.Event.REMOVED);
        globalPathRegistry_unregister(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar registered for second time (seen before) scanStarted should be called and returns false (rescan).
        //The ConstrainedBinaryIndexer accepted the jar in prev scan -> the scanStarted returned false ->
        // scanStarted,index, scanFinished should be called.
        indexer.expect(MockConstrainedIndexer.Event.STARTED, MockConstrainedIndexer.Event.INDEXED, MockConstrainedIndexer.Event.FINISHED);
        indexer.setVote(false);
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));
        assertTrue(indexer.hasResources(Collections.<String,Set<String>>emptyMap()));

        //Jar unregistered rootsRemoved should be called
        indexer.expect(MockConstrainedIndexer.Event.REMOVED);
        globalPathRegistry_unregister(PATH_LIB, cp);
        assertTrue(indexer.await(5));

    }

    /**
     * Tests ConstrainedBinaryIndexer with required resource predicate,
     * the resource exists in archive.
     */
    public void testRequiredResourcePredicate_res_exists() throws Exception {
        final String[] reqRes = new String[]{"required_resource.txt"};  //NOI18N
        final MockConstrainedIndexer indexer = new MockConstrainedIndexer();
        registerProxyBinaryIndexer(indexer,reqRes,null,null);
        final URL root = createArchive (getWorkDir(), "resPred.jar", reqRes); //NOI18N
        final ClassPath cp = ClassPathSupport.createClassPath(root);

        //Jar registered for first time (never seen before) scanStarted, index, scanFinished shoud be called.
        indexer.expect(MockConstrainedIndexer.Event.STARTED, MockConstrainedIndexer.Event.INDEXED, MockConstrainedIndexer.Event.FINISHED);
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));
        assertTrue(indexer.hasResources(Collections.<String,Set<String>>emptyMap()));

        //Jar unregistered rootsRemoved should be called
        indexer.expect(MockConstrainedIndexer.Event.REMOVED);
        globalPathRegistry_unregister(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar registered for second time (seen before) scanStarted should be called.
        //The ConstrainedBinaryIndexer accepted the jar in prev scan -> the scanStarted
        //has to be called to give a chance to indexer to force rescan of it when needed.
        indexer.expect(MockConstrainedIndexer.Event.STARTED);
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar unregistered rootsRemoved should be called
        indexer.expect(MockConstrainedIndexer.Event.REMOVED);
        globalPathRegistry_unregister(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar registered for second time (seen before) scanStarted should be called and returns false (rescan).
        //The ConstrainedBinaryIndexer accepted the jar in prev scan -> the scanStarted returned false ->
        // scanStarted,index, scanFinished should be called.
        indexer.expect(MockConstrainedIndexer.Event.STARTED, MockConstrainedIndexer.Event.INDEXED, MockConstrainedIndexer.Event.FINISHED);
        indexer.setVote(false);
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));
        assertTrue(indexer.hasResources(Collections.<String,Set<String>>emptyMap()));

        //Jar unregistered rootsRemoved should be called
        indexer.expect(MockConstrainedIndexer.Event.REMOVED);
        globalPathRegistry_unregister(PATH_LIB, cp);
        assertTrue(indexer.await(5));
    }

    /**
     * Tests ConstrainedBinaryIndexer with required resource predicate,
     * the resource DOES NOT exist in archive.
     */
    public void testRequiredResourcePredicate_res_does_not_exist() throws Exception {
        final String[] reqRes = new String[]{"required_resource.txt"};  //NOI18N
        final MockConstrainedIndexer indexer = new MockConstrainedIndexer();
        registerProxyBinaryIndexer(indexer,reqRes,null,null);
        final URL root = createArchive (getWorkDir(), "resPredFalse.jar", new String[0]); //NOI18N
        final ClassPath cp = ClassPathSupport.createClassPath(root);

        //Jar registered for first time (never seen before), predicates evaluate to false
        // -> nothing should be called.
        indexer.expect();
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar unregistered, predicate was false -> nothing should be called
        indexer.expect();
        globalPathRegistry_unregister(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar registered for second time (seen before) and predicates evaluates to false
        // -> nothing should be started.
        indexer.expect();
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar unregistered, predicate was false -> nothing should be called
        indexer.expect();
        globalPathRegistry_unregister(PATH_LIB, cp);
        indexer.await(5);
    }

    /**
     * Tests ConstrainedBinaryIndexer with required mime predicate,
     * the mime exists in archive.
     */
    public void testRequiredMimePredicate_mime_exists() throws Exception {
        final MockConstrainedIndexer indexer = new MockConstrainedIndexer();
        registerProxyBinaryIndexer(indexer,null,new String[]{FOO_MIME},null);
        final URL root = createArchive (getWorkDir(), "mimePred.jar", new String[]{"test.foo"}); //NOI18N
        final ClassPath cp = ClassPathSupport.createClassPath(root);

        //Jar registered for first time (never seen before) scanStarted, index, scanFinished shoud be called.
        indexer.expect(MockConstrainedIndexer.Event.STARTED, MockConstrainedIndexer.Event.INDEXED, MockConstrainedIndexer.Event.FINISHED);
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));
        assertTrue(indexer.hasResources(new HashMap<String, Set<String>>(){{put(FOO_MIME,Collections.singleton("test.foo"));}}));   //NOI18N

        //Jar unregistered rootsRemoved should be called
        indexer.expect(MockConstrainedIndexer.Event.REMOVED);
        globalPathRegistry_unregister(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar registered for second time (seen before) scanStarted should be called.
        //The ConstrainedBinaryIndexer accepted the jar in prev scan -> the scanStarted
        //has to be called to give a chance to indexer to force rescan of it when needed.
        indexer.expect(MockConstrainedIndexer.Event.STARTED);
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar unregistered rootsRemoved should be called
        indexer.expect(MockConstrainedIndexer.Event.REMOVED);
        globalPathRegistry_unregister(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar registered for second time (seen before) scanStarted should be called and returns false (rescan).
        //The ConstrainedBinaryIndexer accepted the jar in prev scan -> the scanStarted returned false ->
        // scanStarted,index, scanFinished should be called.
        indexer.expect(MockConstrainedIndexer.Event.STARTED, MockConstrainedIndexer.Event.INDEXED, MockConstrainedIndexer.Event.FINISHED);
        indexer.setVote(false);
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));
        assertTrue(indexer.hasResources(new HashMap<String, Set<String>>(){{put(FOO_MIME,Collections.singleton("test.foo"));}}));   //NOI18N

        //Jar unregistered rootsRemoved should be called
        indexer.expect(MockConstrainedIndexer.Event.REMOVED);
        globalPathRegistry_unregister(PATH_LIB, cp);
        assertTrue(indexer.await(5));
    }

    /**
     * Tests ConstrainedBinaryIndexer with required mime predicate,
     * the resource DOES NOT exist in archive.
     */
    public void testRequiredMimePredicate_mime_does_not_exist() throws Exception {
        final MockConstrainedIndexer indexer = new MockConstrainedIndexer();
        registerProxyBinaryIndexer(indexer,null,new String[]{FOO_MIME},null);
        final URL root = createArchive (getWorkDir(), "mimePredFalse.jar", new String[0]); //NOI18N
        final ClassPath cp = ClassPathSupport.createClassPath(root);

        //Jar registered for first time (never seen before), predicates evaluate to false
        // -> nothing should be called.
        indexer.expect();
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar unregistered, predicate was false -> nothing should be called
        indexer.expect();
        globalPathRegistry_unregister(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar registered for second time (seen before) and predicates evaluates to false
        // -> nothing should be started.
        indexer.expect();
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar unregistered, predicate was false -> nothing should be called
        indexer.expect();
        globalPathRegistry_unregister(PATH_LIB, cp);
        indexer.await(5);
    }

    /**
     * Tests ConstrainedBinaryIndexer with required pattern predicate,
     * the pattern exists in archive.
     */
    public void testRequiredPatternPredicate_pattern_exists() throws Exception {
        final MockConstrainedIndexer indexer = new MockConstrainedIndexer();
        registerProxyBinaryIndexer(indexer,null,null, "k.+l\\.foo");
        final URL root = createArchive (getWorkDir(), "patternPred.jar", new String[]{"karel.foo"}); //NOI18N
        final ClassPath cp = ClassPathSupport.createClassPath(root);

        //Jar registered for first time (never seen before) scanStarted, index, scanFinished shoud be called.
        indexer.expect(MockConstrainedIndexer.Event.STARTED, MockConstrainedIndexer.Event.INDEXED, MockConstrainedIndexer.Event.FINISHED);
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));
        assertTrue(indexer.hasResources(new HashMap<String, Set<String>>(){{put("content/unknown",Collections.singleton("karel.foo"));}}));   //NOI18N

        //Jar unregistered rootsRemoved should be called
        indexer.expect(MockConstrainedIndexer.Event.REMOVED);
        globalPathRegistry_unregister(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar registered for second time (seen before) scanStarted should be called.
        //The ConstrainedBinaryIndexer accepted the jar in prev scan -> the scanStarted
        //has to be called to give a chance to indexer to force rescan of it when needed.
        indexer.expect(MockConstrainedIndexer.Event.STARTED);
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar unregistered rootsRemoved should be called
        indexer.expect(MockConstrainedIndexer.Event.REMOVED);
        globalPathRegistry_unregister(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar registered for second time (seen before) scanStarted should be called and returns false (rescan).
        //The ConstrainedBinaryIndexer accepted the jar in prev scan -> the scanStarted returned false ->
        // scanStarted,index, scanFinished should be called.
        indexer.expect(MockConstrainedIndexer.Event.STARTED, MockConstrainedIndexer.Event.INDEXED, MockConstrainedIndexer.Event.FINISHED);
        indexer.setVote(false);
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));
        assertTrue(indexer.hasResources(new HashMap<String, Set<String>>(){{put("content/unknown",Collections.singleton("karel.foo"));}}));   //NOI18N

        //Jar unregistered rootsRemoved should be called
        indexer.expect(MockConstrainedIndexer.Event.REMOVED);
        globalPathRegistry_unregister(PATH_LIB, cp);
        assertTrue(indexer.await(5));
    }

    /**
     * Tests ConstrainedBinaryIndexer with required pattern predicate,
     * the pattern DOES NOT exist in archive.
     */
    public void testRequiredPatternPredicate_pattern_does_not_exist() throws Exception {
        final MockConstrainedIndexer indexer = new MockConstrainedIndexer();
        registerProxyBinaryIndexer(indexer,null,null, "k.+l\\.foo");
        final URL root = createArchive (getWorkDir(), "mimePredFalse.jar", new String[0]); //NOI18N
        final ClassPath cp = ClassPathSupport.createClassPath(root);

        //Jar registered for first time (never seen before), predicates evaluate to false
        // -> nothing should be called.
        indexer.expect();
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar unregistered, predicate was false -> nothing should be called
        indexer.expect();
        globalPathRegistry_unregister(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar registered for second time (seen before) and predicates evaluates to false
        // -> nothing should be started.
        indexer.expect();
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar unregistered, predicate was false -> nothing should be called
        indexer.expect();
        globalPathRegistry_unregister(PATH_LIB, cp);
        indexer.await(5);
    }

    /**
     * Tests transitions among results of predicate evaluation
     */
    public void testTransitions() throws Exception {
        final String[] reqRes = new String[]{"required_resource.txt"};  //NOI18N
        final MockConstrainedIndexer indexer = new MockConstrainedIndexer();
        registerProxyBinaryIndexer(indexer,reqRes,null,null);
        URL root = createArchive (getWorkDir(), "tr.jar", new String[0]); //NOI18N
        ClassPath cp = ClassPathSupport.createClassPath(root);

        //Jar registered for first time (never seen before), predicates evaluate to false
        // -> nothing should be called.
        indexer.expect();
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar unregistered, predicate was false -> nothing should be called
        indexer.expect();
        globalPathRegistry_unregister(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar registered for second time (seen before but modified) predicate evaluates to false
        //-> nothing should be called
        waitFS();
        root = createArchive (getWorkDir(), "tr.jar", new String[0]); //NOI18N
        cp = ClassPathSupport.createClassPath(root);
        indexer.expect();
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar unregistered, predicate was false -> nothing should be called
        indexer.expect();
        globalPathRegistry_unregister(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar registered for second time (seen before but modified) now predicate evaluates to true
        //-> started, indexed, finished should be called
        waitFS();
        root = createArchive (getWorkDir(), "tr.jar", reqRes); //NOI18N
        cp = ClassPathSupport.createClassPath(root);
        indexer.expect(MockConstrainedIndexer.Event.STARTED, MockConstrainedIndexer.Event.INDEXED, MockConstrainedIndexer.Event.FINISHED);
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));
        assertTrue(indexer.hasResources(Collections.<String,Set<String>>emptyMap()));

        //Jar unregistered rootsRemoved should be called
        indexer.expect(MockConstrainedIndexer.Event.REMOVED);
        globalPathRegistry_unregister(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar registered for second time (seen before) scanStarted should be called.
        //The ConstrainedBinaryIndexer accepted the jar in prev scan -> the scanStarted
        //has to be called to give a chance to indexer to force rescan of it when needed.
        indexer.expect(MockConstrainedIndexer.Event.STARTED);
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar unregistered rootsRemoved should be called
        indexer.expect(MockConstrainedIndexer.Event.REMOVED);
        globalPathRegistry_unregister(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar modified and predicate is now false -> nothing should be called
        waitFS();
        root = createArchive (getWorkDir(), "tr.jar", new String[0]); //NOI18N
        cp = ClassPathSupport.createClassPath(root);
        indexer.expect();
        globalPathRegistry_register(PATH_LIB, cp);
        assertTrue(indexer.await(5));

        //Jar unregistered, predicate was false -> nothing should be called
        indexer.expect();
        globalPathRegistry_unregister(PATH_LIB, cp);
        assertTrue(indexer.await(5));
    }


    private void globalPathRegistry_register(String id, ClassPath... classpaths) {
        Set<ClassPath> set = registeredClasspaths.get(id);
        if (set == null) {
            set = new HashSet<ClassPath>();
            registeredClasspaths.put(id, set);
        }
        set.addAll(Arrays.asList(classpaths));
        GlobalPathRegistry.getDefault().register(id, classpaths);
    }

    private void globalPathRegistry_unregister(String id, ClassPath... classpaths) {
        GlobalPathRegistry.getDefault().unregister(id, classpaths);
        Set<ClassPath> set = registeredClasspaths.get(id);
        if (set != null) {
            set.removeAll(Arrays.asList(classpaths));
        }
    }

    private void registerProxyBinaryIndexer(
            @NonNull final ConstrainedBinaryIndexer indexer,
            @NullAllowed final String[] resourcePredicate,
            @NullAllowed final String[] mimePredicate,
            @NullAllowed final String regExpPredixate) {
        final Map<String,Object> attrs = new HashMap<String, Object>();
        attrs.put("name", "mockIndexer");   //NOI18N
        attrs.put("version", 1);            //NOI18N
        attrs.put("delegate",indexer);      //NOI18N
        attrs.put("requiredResource",asList(resourcePredicate));    //NOI18N
        attrs.put("mimeType", asList(mimePredicate));   //NOI18N
        attrs.put("namePattern", regExpPredixate);   //NOI18N
        final BinaryIndexerFactory proxyBinIndexerFactory = new ProxyBinaryIndexerFactory(attrs);
        MockMimeLookup.setInstances(MimePath.EMPTY, proxyBinIndexerFactory);
    }

    private String asList(@NullAllowed final String[] items) {
        if (items == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        for (String item : items) {
            sb.append(item).append(',');    //NOI18N
        }
        return sb.substring(0, sb.length()-1).toString();
    }

    private URL createArchive(
            @NonNull final File dir,
            @NonNull final String name,
            @NonNull String[] requiredResources) throws IOException {
        final FileObject folder = FileUtil.toFileObject(dir);
        FileObject tmp = folder.getFileObject(name);
        if (tmp != null) {
            tmp.delete();
        }
        tmp = folder.createData(name);
        final FileLock lock = tmp.lock();
        try {
            final ZipOutputStream out = new ZipOutputStream(tmp.getOutputStream(lock));
            try {
                //Put at least something.
                out.putNextEntry(new ZipEntry("nothing.txt"));  //NOI18N
                for (String res : requiredResources) {
                    out.putNextEntry(new ZipEntry(res));
                }
            } finally {
                out.close();
            }
        } finally {
            lock.releaseLock();
        }
        return FileUtil.getArchiveRoot(tmp.getURL());
    }

    /**
     * Waits for FS timestamp
     */
    public void waitFS() throws InterruptedException {
        Thread.sleep(2000);
    }


    private static class MockConstrainedIndexer extends ConstrainedBinaryIndexer {

        enum Event {STARTED, INDEXED, FINISHED, REMOVED};

        //@GuardedBy("this")
        private Set<Event> expectedEvents = EnumSet.<Event>noneOf(Event.class);
        //@GuardedBy("this")
        private Set<Event> unexpectedEvents = EnumSet.<Event>noneOf(Event.class);
        //@GuardedBy("this")
        private Map<String, ? extends Iterable<? extends FileObject>> resources;
        private volatile boolean vote = true;
        private final Semaphore sem = new Semaphore(0);
        private final Handler h;


        MockConstrainedIndexer() {
            h = new Handler() {
                @Override
                public void publish(LogRecord record) {
                    if (record.getMessage().equals("RootsWork-finished")) {
                        sem.release();
                    }
                }
                @Override
                public void flush() {
                }

                @Override
                public void close() throws SecurityException {
                }
            };
            final Logger log = Logger.getLogger(RepositoryUpdater.class.getName() + ".tests"); //NOI18N
            log.setLevel(Level.FINEST);
            log.addHandler(h);
        }

        synchronized void expect(final Event... events) {
            assert expectedEvents.isEmpty();
            expectedEvents.addAll(Arrays.asList(events));
            unexpectedEvents.clear();
            resources = Collections.<String,Iterable<? extends FileObject>>emptyMap();
            assert sem.availablePermits() == 0;
        }

        boolean await(final int timeout) throws InterruptedException {
            boolean res = sem.tryAcquire(timeout, TimeUnit.SECONDS);
            res &= expectedEvents.isEmpty();
            res &= unexpectedEvents.isEmpty();
            return res;
        }

        void setVote(final boolean v) {
            vote = v;
        }

        synchronized boolean hasResources(Map<String,Set<String>> er) {
            if (er.size() != resources.size()) {
                return false;
            }
            for (Map.Entry<String, ? extends Iterable<? extends FileObject>> res : resources.entrySet()) {
                Set<? extends String> names = er.get(res.getKey());
                if (names == null) {
                    return false;
                }
                names = new HashSet<String>(names);
                for (FileObject fo : res.getValue()) {
                    if (!names.remove(fo.getPath())) {
                        return false;
                    }
                }
                if (!names.isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        protected boolean scanStarted(Context context) {
            handleEvent(Event.STARTED);
            return vote;
        }

        @Override
        protected void scanFinished(Context context) {
            handleEvent(Event.FINISHED);
        }

        @Override
        protected void index(Map<String, ? extends Iterable<? extends FileObject>> files, Context context) {
            handleEvent(Event.INDEXED);
            resources = files;
        }

        @Override
        protected void rootsRemoved(Iterable<? extends URL> removedRoots) {
            handleEvent(Event.REMOVED);
        }

        private synchronized void handleEvent(final Event event) {
            final boolean change = expectedEvents.remove(event);
            if (change) {
                LOG.fine(event.name());
            } else {
                unexpectedEvents.add(event);
            }
        }
    }

    public static class Recognizer extends PathRecognizer {

        @Override
        public Set<String> getSourcePathIds() {
            return Collections.<String>emptySet();
        }

        @Override
        public Set<String> getBinaryLibraryPathIds() {
            return Collections.<String>singleton(PATH_LIB);
        }

        @Override
        public Set<String> getLibraryPathIds() {
            return null;
        }

        @Override
        public Set<String> getMimeTypes() {
            return Collections.<String>emptySet();
        }

    }
}
