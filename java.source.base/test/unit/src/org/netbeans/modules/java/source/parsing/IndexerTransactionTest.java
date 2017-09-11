/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.parsing;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClassIndexListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.RootsEvent;
import org.netbeans.api.java.source.TypesEvent;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * This test checks isolation of java custom indexer.
 * 
 * Note - semaphores are used instead of plain wait(), so that premature notify()
 * is not missed by the observer.
 * 
 * @author sdedic
 */
public class IndexerTransactionTest extends NbTestCase {
    
    private static final Logger TEST_LOGGER = Logger.getLogger(RepositoryUpdater.class.getName() + ".tests"); //NOI18N
    private static final Logger TRANS_LOGGER = Logger.getLogger(WriteBackTransaction.class.getName());    
    
    private static FileObject srcRoot;
    private static FileObject srcRoot2;
    private static FileObject binRoot;
    private static FileObject binRoot2;
    private static FileObject libSrc2;
    
    private static ClassPath sourcePath;
    private static ClassPath compilePath;
    private static ClassPath bootPath;
    private static MutableCp spiCp;
    private static MutableCp spiSrc;
    
    /**
     * Simple semaphore, which will block the parser thread until the test allows
     * the inspected parsing process to continue
     */
    private Semaphore   parserBlocker = new Semaphore(0);
    
    /**
     * Blocker for the test, to wait until the parser does something interesting.
     */
    private Semaphore   testBlocker = new Semaphore(0);
    
    private Object blocker = new String("blocker");
    
    private TxLogHandler    logHandler = new TxLogHandler();

    private final Map<String, Set<ClassPath>> registeredClasspaths = new HashMap<String, Set<ClassPath>>();

    public IndexerTransactionTest(String name) {
        super(name);
    }
    
    protected final void registerGlobalPath(String id, ClassPath [] classpaths) {
        Set<ClassPath> set = registeredClasspaths.get(id);
        if (set == null) {
            set = new HashSet<ClassPath>();
            registeredClasspaths.put(id, set);
        }
        set.addAll(Arrays.asList(classpaths));
        GlobalPathRegistry.getDefault().register(id, classpaths);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        File cache = new File(getWorkDir(), "cache");       //NOI18N
        cache.mkdirs();
        IndexUtil.setCacheFolder(cache);
        
        File dataRoot = new File(getDataDir(), "indexing");

        File src = new File(getWorkDir(), "src");           //NOI18N
        src.mkdirs();
        srcRoot = FileUtil.toFileObject(src);
    
        TestUtil.copyContents(new File(dataRoot, "root1"), src);
        
        src = new File(getWorkDir(), "src2");               //NOI18N
        src.mkdirs();
        srcRoot2 = FileUtil.toFileObject(src);
        
        TestUtil.copyContents(new File(dataRoot, "root2"), src);

        src = new File(getWorkDir(), "lib");               //NOI18N
        src.mkdirs();
        binRoot = FileUtil.toFileObject(src);
        
        src = new File(getWorkDir(), "lib2");               //NOI18N
        src.mkdirs();
        binRoot2 = FileUtil.toFileObject(src);
        
        src = new File(getWorkDir(), "lib2Src");            //NOI18N
        src.mkdirs();
        libSrc2 = FileUtil.toFileObject(src);
        
        spiSrc = new MutableCp (Collections.singletonList(ClassPathSupport.createResource(srcRoot.getURL())));
        sourcePath = ClassPathFactory.createClassPath(spiSrc);
        spiCp = new MutableCp ();
        compilePath = ClassPathFactory.createClassPath(spiCp);
        bootPath = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        
        TEST_LOGGER.addHandler(logHandler);
        TEST_LOGGER.setLevel(Level.ALL);
        
        TRANS_LOGGER.addHandler(logHandler);
        TRANS_LOGGER.setLevel(Level.ALL);

        SFBQImpl.register (binRoot, srcRoot);
        SFBQImpl.register (binRoot2, srcRoot2);

        MockServices.setServices(
                ClassPathProviderImpl.class, 
                SFBQImpl.class,
                JavaPathRecognizer.class
        );
    }

    @Override
    protected void tearDown() throws Exception {
        MockServices.setServices();
        
        for (Handler h : TEST_LOGGER.getHandlers()) {
            TEST_LOGGER.removeHandler(h);
        }
        for (Handler h : TRANS_LOGGER.getHandlers()) {
            TRANS_LOGGER.removeHandler(h);
        }
        synchronized (blocker) {
            blocker.notifyAll();
        }
        logHandler.beforeCommitCallback = null;
        logHandler.beforeFileWriteCallback = null;
        logHandler.beforeFinishCallback = null;
        parserBlocker.release(1000);

        for(String id : registeredClasspaths.keySet()) {
            Set<ClassPath> classpaths = registeredClasspaths.get(id);
            GlobalPathRegistry.getDefault().unregister(id, classpaths.toArray(new ClassPath[classpaths.size()]));
        }
        registeredClasspaths.clear();
        RepositoryUpdater.getDefault().waitUntilFinished(-1);
        parserBlocker.drainPermits();
    }
    
    /**
     * Finds the segment, which was generated for the source directory 'dirName'.
     * 
     * @param dirName
     * @return
     * @throws IOException 
     */
    private File findSegmentDir(final FileObject dirName) throws IOException {
        final File root = JavaIndex.getClassFolder(dirName.toURL());
        return root;
    }
    
    /**
     * Checks that during the initial scan, types from the source root are hidden,
     * until the source root is fully scanned.
     * 
     * Also check that files are not visible until the source root finishes.
     * 
     * Enforce flush of memory cache after each file, and each added document.
     * 
     * @throws Exception 
     */
    public void testInitialWorkRootHidden() throws Exception {
        registerGlobalPath(ClassPath.BOOT, new ClassPath[] {bootPath});
        registerGlobalPath(ClassPath.COMPILE, new ClassPath[] {compilePath});
        registerGlobalPath(ClassPath.SOURCE, new ClassPath[] {sourcePath});
        
        RepositoryUpdater.getDefault().start(true);
        
        // make the initial scan, with 'src'
        logHandler.waitForInitialScan();

        // ensure the cache will flush after each file:
        WriteBackTransaction.disableCache = true;
        // force Lucene index to fluhs after each document
        System.setProperty("test.org.netbeans.modules.parsing.lucene.cacheDisable", Boolean.TRUE.toString());
        
        final ClassPath scp = ClassPathSupport.createClassPath(srcRoot, srcRoot2);
        
        final ClasspathInfo cpInfo = ClasspathInfo.create(
            ClassPathSupport.createClassPath(new URL[0]),
            ClassPathSupport.createClassPath(new URL[0]),
            scp);
        final ClassIndex ci = cpInfo.getClassIndex();
        
        
        Set<ElementHandle<TypeElement>> handles = ci.getDeclaredTypes("TestFile", ClassIndex.NameKind.SIMPLE_NAME, Collections.singleton(
                SearchScope.SOURCE));

        assertEquals(1, handles.size());
        
        handles = ci.getDeclaredTypes("SuperClass", ClassIndex.NameKind.SIMPLE_NAME, Collections.singleton(
                SearchScope.SOURCE));
        assertTrue(handles.isEmpty());
        
        // now add 'src2' to the set of roots, block the completion (in log handler),
        // check that classes are not available
        final Object blocker = new String("blocker");
        
        logHandler.beforeFinishCallback = new ScanCallback() {

            @Override
            public void scanned(String indexer, String root) {
                if (!indexer.equals("java")) {
                    return;
                }
                if (!root.endsWith("src2/")) {
                    return;
                }
                
                // notify the test to proceeed, wait for 
                testBlocker.release();
                try {
                    // wait for the test to inspect
                    parserBlocker.acquire();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };

        List ll = new ArrayList<PathResourceImplementation>();
        ll.add(ClassPathSupport.createResource(srcRoot.getURL()));
        ll.add(ClassPathSupport.createResource(srcRoot2.getURL()));
        spiSrc.setImpls(ll);
        
        // wait till src2
        testBlocker.acquire();

        // check that SuperClass is STILL not present
        handles = ci.getDeclaredTypes("TestFile", ClassIndex.NameKind.SIMPLE_NAME, Collections.singleton(
                SearchScope.SOURCE));

        assertEquals(1, handles.size());
        
        handles = ci.getDeclaredTypes("SuperClass", ClassIndex.NameKind.SIMPLE_NAME, Collections.singleton(
                SearchScope.SOURCE));
        assertTrue(handles.isEmpty());
        

        Semaphore ss = new Semaphore(0);
        ci.addClassIndexListener(new RootWatcher(ss));
        
        parserBlocker.release();
        ss.acquire();
        
        handles = ci.getDeclaredTypes("SuperClass", ClassIndex.NameKind.SIMPLE_NAME, Collections.singleton(
                SearchScope.SOURCE));
        assertEquals(1, handles.size());    
    }
    
    /**
     * Will signal the semaphore when a root is added.
     */
    private static class RootWatcher implements ClassIndexListener {
        private Semaphore signal;

        public RootWatcher(Semaphore signal) {
            this.signal = signal;
        }
            @Override
            public void rootsAdded(RootsEvent event) {
                signal.release();
            }

            @Override
            public void rootsRemoved(RootsEvent event) {}

            @Override
            public void typesAdded(TypesEvent event) {}

            @Override
            public void typesChanged(TypesEvent event) {}

            @Override
            public void typesRemoved(TypesEvent event) {}
            
    }
    
    ClassIndex ci;
    
    void commonSetup() throws Exception {
        Logger l = Logger.getLogger(WriteBackTransaction.class.getName());
        l.setLevel(Level.FINE);
        l.addHandler(logHandler);
        
        TEST_LOGGER.setLevel(Level.ALL);
        
        List ll = new ArrayList<PathResourceImplementation>();
        ll.add(ClassPathSupport.createResource(srcRoot.getURL()));
        ll.add(ClassPathSupport.createResource(srcRoot2.getURL()));
        spiSrc.setImpls(ll);

        registerGlobalPath(ClassPath.BOOT, new ClassPath[] {bootPath});
        registerGlobalPath(ClassPath.COMPILE, new ClassPath[] {compilePath});
        registerGlobalPath(ClassPath.SOURCE, new ClassPath[] {sourcePath});

        final ClassPath scp = ClassPathSupport.createClassPath(srcRoot, srcRoot2);
        
        final ClasspathInfo cpInfo = ClasspathInfo.create(
            ClassPathSupport.createClassPath(new URL[0]),
            ClassPathSupport.createClassPath(new URL[0]),
            scp);
        ci = cpInfo.getClassIndex();

        RepositoryUpdater.getDefault().start(true);
        
        // make the initial scan, with 'src' and 'src2'
        logHandler.waitForInitialScan(2);

        // ensure the cache will flush after each file:
        WriteBackTransaction.disableCache = true;
        
    }
    
    /**
     * Checks that added files are not visible until their source root is added,
     * even if the memcache with file contents 
     * is flushed during the scan (will be flushed after each file)
     */
    public void testAddedClassesNotVisible() throws Exception {
        commonSetup();
        final Semaphore signal = new Semaphore(0);

        logHandler.beforeFinishCallback = new RootScannedCallback("java", "src/", signal);
        
        Set<ElementHandle<TypeElement>> handles;

        handles = ci.getDeclaredTypes("ConstructorTest", ClassIndex.NameKind.SIMPLE_NAME, Collections.singleton(
                SearchScope.SOURCE));
        // copy over some files
        TestUtil.copyFiles(
                new File(getDataDir(), "indexing/files"),
                new File(FileUtil.toFile(srcRoot), "org/netbeans/parsing/source1".replace('/', File.separatorChar)),
                        "ConstructorTest.java", "EmptyClass.java");

        // must force rescan, indexer does not notice the file-copy ?
        RepositoryUpdater.getDefault().refreshAll(false, false, true, null, 
                srcRoot, srcRoot2);
        try {
            signal.acquire();
            signal.drainPermits();
        } catch (InterruptedException ex) {
            fail("Should rescan the added files");
        }

        handles = ci.getDeclaredTypes("ConstructorTest", ClassIndex.NameKind.SIMPLE_NAME, Collections.singleton(
                SearchScope.SOURCE));
        assertEquals(0, handles.size());    
        
        // check that files STILL do not exist
        File dir = findSegmentDir(srcRoot);
        File targetDir = new File(dir, "org/netbeans/parsing/source1".replace('/', File.separatorChar));
        
        assertFalse(new File(targetDir, "ConstructorTest.sig").exists());
        assertFalse(new File(targetDir, "EmptyClass.sig").exists());
        
        parserBlocker.release();
        
        logHandler.waitForInitialScan();
        
        handles = ci.getDeclaredTypes("ConstructorTest", ClassIndex.NameKind.SIMPLE_NAME, Collections.singleton(
                SearchScope.SOURCE));
        assertEquals(1, handles.size());    

        assertTrue(new File(targetDir, "ConstructorTest.sig").exists());
        assertTrue(new File(targetDir, "EmptyClass.sig").exists());
    }

    private class RootScannedCallback implements ScanCallback {
        private String indexerName;
        private String rootSuffix;
        private Semaphore signal;

        public RootScannedCallback(String indexerName, String rootSuffix, Semaphore signal) {
            this.indexerName = indexerName;
            this.rootSuffix = rootSuffix;
            this.signal = signal;
        }
        
        
        @Override
        public void scanned(String indexer, String root) {
            if (indexerName != null && !indexerName.equals(indexer)) {
                return;
            }
            if (!root.endsWith(rootSuffix)) {
                return;
            }
            signal.release();
            try {
                parserBlocker.acquire();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        
    }
    
    /**
     * Checks that deleted classes remain visible until the source root is committed.
     * 
     * @throws Exception 
     */
    public void testDeletedClassesVisible() throws Exception {
        commonSetup();
        final Semaphore signal = new Semaphore(0);

        logHandler.beforeFinishCallback = new RootScannedCallback("java", "src/", signal);
        
        Set<ElementHandle<TypeElement>> handles;

        handles = ci.getDeclaredTypes("ClassWithInnerClass", ClassIndex.NameKind.SIMPLE_NAME, Collections.singleton(
                SearchScope.SOURCE));
        
        assertEquals(1, handles.size());
        
        File sourceDir = new File(FileUtil.toFile(srcRoot), "org/netbeans/parsing/source1".replace('/', File.separatorChar));
        new File(sourceDir, "ClassWithInnerClass.java").delete();
        
        // must force rescan, indexer does not notice the file-copy ?
        RepositoryUpdater.getDefault().refreshAll(false, false, true, null, 
                srcRoot, srcRoot2);
        try {
            signal.acquire();
            signal.drainPermits();
        } catch (InterruptedException ex) {
            fail("Should rescan the added files");
        }

        handles = ci.getDeclaredTypes("ClassWithInnerClass", ClassIndex.NameKind.SIMPLE_NAME, Collections.singleton(
                SearchScope.SOURCE));
        assertEquals(1, handles.size());    
        
        // check that files STILL do not exist
        File dir = findSegmentDir(srcRoot);
        File targetDir = new File(dir, "org/netbeans/parsing/source1".replace('/', File.separatorChar));
        
        assertTrue(new File(targetDir, "ClassWithInnerClass.sig").exists());
        
        parserBlocker.release();
        
        logHandler.waitForInitialScan();
        
        handles = ci.getDeclaredTypes("ClassWithInnerClass", ClassIndex.NameKind.SIMPLE_NAME, Collections.singleton(
                SearchScope.SOURCE));
        assertEquals(0, handles.size());    

        assertFalse(new File(targetDir, "ClassWithInnerClass.sig").exists());
    }
    
    interface ScanCallback {
        public void scanned(String indexer, String root);
    }
    
    /**
     * This is a log handler overloaded for various hook loggers
     */
    class TxLogHandler extends Handler {
        int flushed;
        int committed;
        int memory;
        int reference;
        int rootsToGo;
        Semaphore signal = new Semaphore(0);
        
        /**
         * Callback called by indexer's scanFinished(), before the actual action is invoked
         */
        ScanCallback beforeFinishCallback;
        
        /**
         * Called just before indexes are committed (storeChanges called)
         */
        ScanCallback beforeCommitCallback;
        
        /**
         * Called just before files are committed. Indexer will be always null for the callback.
         */
        ScanCallback beforeFileWriteCallback;
        
        
        public void waitForInitialScan() throws Exception {
            waitForInitialScan(1);
        }
        public void waitForInitialScan(int count) throws Exception {
            signal.acquire();
            signal.drainPermits();
        }
        
        @Override
        public void close() throws SecurityException {
        }

        @Override
        public void flush() {
        }
        
        private void handleTestLogger(LogRecord record) {
            String msg = record.getMessage();
            if (msg.contains("scanSources")) {
                signal.release();
            } else if (msg.contains("scanFinishing:")) {
                if (beforeFinishCallback != null) {
                    beforeFinishCallback.scanned((String)record.getParameters()[0], (String)record.getParameters()[1]);
                }
            } else if (msg.contains("indexCommit:")) {
                if (beforeCommitCallback != null) {
                    beforeCommitCallback.scanned((String)record.getParameters()[0], (String)record.getParameters()[1]);
                }
            }
        }

        @Override
        public void publish(LogRecord record) {
            if (record.getLevel().intValue() > Level.FINE.intValue()) {
                return;
            }
            if (record.getLoggerName().endsWith(".tests")) {
                handleTestLogger(record);
                return;
            }
            String msg = record.getMessage();
            if (msg.contains("Memory exhausted")) {
                memory++;
            }
            if (msg.contains("Committed")) {
                committed++;
                if (beforeFileWriteCallback != null) {
                    beforeFileWriteCallback.scanned(null, ((FileObject)record.getParameters()[0]).getPath());
                }
            }
            if (msg.contains("Flushing")) {
                flushed++;
            }
            if (msg.contains("Reference freed")) {
                reference++;
            }
        }
        
    }

    
    public static class ClassPathProviderImpl implements ClassPathProvider {

        public ClassPath findClassPath(final FileObject file, final String type) {
            final FileObject[] roots = sourcePath.getRoots();
            for (FileObject root : roots) {
                if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                    if (type == ClassPath.SOURCE) {
                        return sourcePath;
                    }
                    if (type == ClassPath.COMPILE) {
                        return compilePath;
                    }
                    if (type == ClassPath.BOOT) {
                        return bootPath;
                    }
                }
            }
            if (libSrc2.equals(file) || FileUtil.isParentOf(libSrc2, file)) {
                if (type == ClassPath.SOURCE) {
                        return ClassPathSupport.createClassPath(new FileObject[]{libSrc2});
                    }
                    if (type == ClassPath.COMPILE) {
                        return ClassPathSupport.createClassPath(new URL[0]);
                    }
                    if (type == ClassPath.BOOT) {
                        return bootPath;
                    }
            }
            return null;
        }        
    }

    public static class SFBQImpl implements SourceForBinaryQueryImplementation {

        final static Map<URL,FileObject> map = new HashMap<URL,FileObject> ();
        final static Map<URL,Result> results = new HashMap<URL,Result> ();

        public SFBQImpl () {

        }

        public static void register (FileObject binRoot, FileObject sourceRoot) throws IOException {
            URL url = binRoot.getURL();
            map.put (url,sourceRoot);
            Result r = results.get (url);
            if (r != null) {
                r.update (sourceRoot);
            }
        }

        public static void unregister (FileObject binRoot) throws IOException {
            URL url = binRoot.getURL();
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
                this.listeners = new LinkedList<ChangeListener> ();
            }

            public void update (FileObject root) {
                this.root = root;
                fireChange ();
            }

            public synchronized void addChangeListener(ChangeListener l) {
                this.listeners.add(l);
            }

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
                    _listeners = this.listeners.toArray(new ChangeListener[this.listeners.size()]);
                }
                ChangeEvent event = new ChangeEvent (this);
                for (ChangeListener l : _listeners) {
                    l.stateChanged (event);
                }
            }
        }

    }

    
    
}
