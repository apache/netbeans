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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.openide.util.ProxyURLStreamHandlerFactory;
import static org.netbeans.modules.parsing.impl.indexing.FooPathRecognizer.FOO_BINARY;
import static org.netbeans.modules.parsing.impl.indexing.FooPathRecognizer.FOO_PLATFORM;
import static org.netbeans.modules.parsing.impl.indexing.FooPathRecognizer.FOO_SOURCES;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public class PathRegistryTest extends IndexingTestBase {

    private FileObject srcRoot1;
    private FileObject srcRoot2;
    private FileObject srcRoot3;
    private FileObject compRoot1;
    private FileObject compRoot2;
    private FileObject bootRoot1;
    private FileObject bootRoot2;
    private FileObject compSrc1;
    private FileObject compSrc2;
    private FileObject bootSrc1;
    private FileObject unknown1;
    private FileObject unknown2;
    private FileObject unknownSrc2;

    public PathRegistryTest (String name) {
        super (name);
    }

    @Override
    protected void getAdditionalServices(List<Class> clazz) {
        clazz.add(SFBQImpl.class);
        clazz.add(DeadLockSFBQImpl.class);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PathRegistryTest("testPathRegistry"));
        suite.addTest(new PathRegistryTest("testProjectMutexDeadlock"));
        suite.addTest(new PathRegistryTest("testRaceCondition"));
        suite.addTest(new PathRegistryTest("testRaceCondition2"));
        suite.addTest(new PathRegistryTest("testBinaryPath"));
        suite.addTest(new PathRegistryTest("testExcludeEvents"));
        suite.addTest(new PathRegistryTest("testURLNoHostPart"));
        return suite;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.clearWorkDir();
        try {
            URL.setURLStreamHandlerFactory(new ProxyURLStreamHandlerFactory());
        } catch (Error ex) {
            // nop 
        }
        final File _wd = this.getWorkDir();
        final FileObject wd = FileUtil.toFileObject(_wd);

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
    }

    public void testPathRegistry () throws Exception {
        //Empty regs test
        final GlobalPathRegistry regs = GlobalPathRegistry.getDefault();
        Collection<? extends URL> result = collectResults ();
        assertEquals(0,result.size());

        //Testing classpath registration
        MutableClassPathImplementation mcpi1 = new MutableClassPathImplementation ();
        mcpi1.addResource(this.srcRoot1);
        PRListener l = new PRListener();
        ClassPath cp1 = ClassPathFactory.createClassPath(mcpi1);
        regs.register(FOO_SOURCES,new ClassPath[]{cp1});
        assertTrue(l.await());
        result = collectResults ();
        assertEquals(1,result.size());
        assertEquals(srcRoot1.getURL(),result.iterator().next());

        //Testing changes in registered classpath - add cp root
        l = new PRListener();
        mcpi1.addResource(srcRoot2);
        assertTrue(l.await());
        result = collectResults ();
        assertEquals(2,result.size());
        assertEquals(new FileObject[] {srcRoot1, srcRoot2},result);
        
        //Testing changes in registered classpath - remove cp root
        l = new PRListener();
        mcpi1.removeResource(srcRoot1);
        assertTrue(l.await());
        result = collectResults ();        
        assertEquals(1,result.size());
        assertEquals(srcRoot2.getURL(),result.iterator().next());
        
        //Testing adding new ClassPath
        l = new PRListener();
        MutableClassPathImplementation mcpi2 = new MutableClassPathImplementation ();
        mcpi2.addResource(srcRoot1);
        ClassPath cp2 = ClassPathFactory.createClassPath(mcpi2);
        regs.register (FOO_SOURCES, new ClassPath[] {cp2});
        assertTrue(l.await());
        result = collectResults ();        
        assertEquals(2,result.size());
        assertEquals(new FileObject[] {srcRoot2, srcRoot1},result);
        
        //Testing changes in newly registered classpath - add cp root
        l = new PRListener();
        mcpi2.addResource(srcRoot3);
        assertTrue(l.await());
        result = collectResults ();        
        assertEquals(3,result.size());
        assertEquals(new FileObject[] {srcRoot2, srcRoot1, srcRoot3},result);
        
        //Testing removing ClassPath
        l = new PRListener();
        regs.unregister(FOO_SOURCES,new ClassPath[] {cp2});
        assertTrue(l.await());
        result = collectResults ();
        assertEquals(1,result.size());
        assertEquals(new FileObject[] {srcRoot2},result);
        
        //Testing registering classpath with SFBQ - register PLATFROM
        l = new PRListener();
        ClassPath cp3 = ClassPathSupport.createClassPath(new FileObject[] {bootRoot1,bootRoot2});
        regs.register(FOO_PLATFORM,new ClassPath[] {cp3});
        assertTrue(l.await());        
        result = PathRegistry.getDefault().getSources();
        assertNotNull (result);
        assertEquals("Wrong source roots: " + result, 1, result.size());
        assertEquals(new FileObject[] {srcRoot2},result);
        result = PathRegistry.getDefault().getLibraries();
        assertNotNull (result);
        assertEquals(1,result.size());
        assertEquals(new FileObject[] {bootSrc1},result);
        
        //Testing registering classpath with SFBQ - register FOO_BINARY
        l = new PRListener();
        MutableClassPathImplementation mcpi4 = new MutableClassPathImplementation ();
        mcpi4.addResource (compRoot1);
        ClassPath cp4 = ClassPathFactory.createClassPath(mcpi4);
        regs.register(FOO_BINARY,new ClassPath[] {cp4});
        assertTrue(l.await());
        result = PathRegistry.getDefault().getSources();
        assertNotNull (result);
        assertEquals(1,result.size());
        assertEquals(new FileObject[] {srcRoot2},result);
        result = PathRegistry.getDefault().getLibraries();
        assertNotNull (result);
        assertEquals(2,result.size());
        assertEquals(new FileObject[] {bootSrc1, compSrc1},result);

        //Testing registering classpath with SFBQ - add into FOO_BINARY
        l = new PRListener();
        mcpi4.addResource(compRoot2);
        assertTrue(l.await());
        result = PathRegistry.getDefault().getLibraries();
        assertNotNull (result);
        assertEquals(3,result.size());
        assertEquals(new FileObject[] {bootSrc1, compSrc1, compSrc2},result);
        
        //Testing registering classpath with SFBQ - remove from FOO_BINARY
        l = new PRListener();
        mcpi4.removeResource(compRoot1);
        assertTrue(l.await());
        result = PathRegistry.getDefault().getLibraries();
        assertNotNull (result);
        assertEquals(2,result.size());
        assertEquals(new FileObject[] {bootSrc1, compSrc2},result);
        
        //Testing registering classpath with SFBQ - unregister FOO_PLATFORM
        l = new PRListener();
        regs.unregister(FOO_PLATFORM,new ClassPath[] {cp3});
        result = PathRegistry.getDefault().getSources();
        assertNotNull (result);
        assertEquals(1,result.size());
        assertEquals(new FileObject[] {srcRoot2},result);
        result = PathRegistry.getDefault().getLibraries();
        assertNotNull (result);
        assertEquals(1,result.size());
        assertEquals(new FileObject[] {compSrc2},result);
        
        //Testing listening on SFBQ.Results - bind source
        l = new PRListener();
        SFBQImpl.register(compRoot2,compSrc1);
        assertTrue(l.await());
        result = PathRegistry.getDefault().getLibraries();
        assertNotNull (result);
        assertEquals(1,result.size());
        assertEquals(new FileObject[] {compSrc1},result);
        
        //Testing listening on SFBQ.Results - rebind (change) source
        l = new PRListener();
        SFBQImpl.register(compRoot2,compSrc2);
        assertTrue(l.await());
        result = PathRegistry.getDefault().getLibraries();
        assertNotNull (result);
        assertEquals(1,result.size());
        assertEquals(new FileObject[] {compSrc2},result);       
        
        RepositoryUpdaterTest.waitForRepositoryUpdaterInit();
    }
    
    /**
     * Simulates a project mutex deadlock
     */
    public void testProjectMutexDeadlock () throws Exception {
        final ExecutorService es = Executors.newSingleThreadExecutor();
        final CountDownLatch wk_ready = new CountDownLatch (1);
        final CountDownLatch mt_ready = new CountDownLatch (1);
        try {
            es.submit(new Runnable () {
                public void run () {
                    try {
                        Object lock = DeadLockSFBQImpl.getLock();
                        ClassPath cp = ClassPathSupport.createClassPath(new FileObject[] {unknown1});
                        synchronized (lock) {
                            wk_ready.countDown();
                            mt_ready.await();
                            Thread.sleep(1000);
                            GlobalPathRegistry.getDefault().register(FOO_BINARY, new ClassPath[] {cp});
                        } 
                    } catch (InterruptedException e) {                        
                    }
                }
            });
            ClassPath cp = ClassPathSupport.createClassPath(new FileObject[] {unknown1});
            wk_ready.await();
            mt_ready.countDown();
            GlobalPathRegistry.getDefault().register(FOO_BINARY, new ClassPath[] {cp});
        } finally {
            es.shutdownNow();
        }
    }
    
    public void testRaceCondition () throws Exception {
        final GlobalPathRegistry regs = GlobalPathRegistry.getDefault();
        final PathRegistry gcp = PathRegistry.getDefault();        
        final int initialSize = gcp.getSources().size();
        final CountDownLatch state_1 = new CountDownLatch (1);
        final CountDownLatch state_2 = new CountDownLatch (1);
        final CountDownLatch state_3 = new CountDownLatch (1);        
        final CountDownLatch state_4 = new CountDownLatch (1);        
        final ExecutorService es = Executors.newSingleThreadExecutor();
        gcp.setDebugCallBack(new Runnable (){
            public void run () {
                try {
                    state_2.countDown();
                    state_3.await();
                } catch (InterruptedException ie) {}
            }
        });
        try {
            es.submit(new Runnable() {
                public void run () {
                    try {
                        state_1.await();
                        gcp.getSources();
                        gcp.getSources();
                        state_4.countDown();
                    } catch (InterruptedException ie) {}
                }
            });
            regs.register(FOO_SOURCES, new ClassPath[] {ClassPathSupport.createClassPath(new URL[] {new URL("file:///foo1/")})});
            state_1.countDown();
            state_2.await();
            regs.register(FOO_SOURCES, new ClassPath[] {ClassPathSupport.createClassPath(new URL[] {new URL("file:///foo2/")})});
            state_3.countDown();
            state_4.await();
            assertEquals("Race condition",initialSize+2,gcp.getSources().size());
        } finally {
            es.shutdownNow();
        }
    }

    public void testRaceCondition2 () throws Exception {
        final GlobalPathRegistry regs = GlobalPathRegistry.getDefault();
        final PathRegistry gcp = PathRegistry.getDefault();
        final int initialSize = gcp.getSources().size();
        final CountDownLatch state_1 = new CountDownLatch (1);
        final CountDownLatch state_2 = new CountDownLatch (1);
        final CountDownLatch state_3 = new CountDownLatch (1);
        final CountDownLatch state_4 = new CountDownLatch (1);
        final ExecutorService es = Executors.newSingleThreadExecutor();
        gcp.setDebugCallBack(new Runnable (){
            public void run () {
                try {
                    state_2.countDown();
                    state_3.await();
                } catch (InterruptedException ie) {}
            }
        });
        try {
            es.submit(new Runnable() {
                public void run () {
                    try {
                        state_1.await();
                        gcp.getSources();
                        gcp.getSources();
                        state_4.countDown();
                    } catch (InterruptedException ie) {}
                }
            });
            regs.register(FOO_SOURCES, new ClassPath[] {ClassPathSupport.createClassPath(new URL[] {new URL("file:///foo3/")})});
            state_1.countDown();
            state_2.await();
            regs.register(FOO_SOURCES, new ClassPath[] {ClassPathSupport.createClassPath(new URL[] {new URL("file:///foo4/")})});
            state_3.countDown();
            state_4.await();
            assertEquals("Race condition",initialSize+2, gcp.getSources().size());
        } finally {
            es.shutdownNow();
        }
    }

    public void testBinaryPath () throws Exception {
        Set<ClassPath> cps = GlobalPathRegistry.getDefault().getPaths(FOO_SOURCES);
        GlobalPathRegistry.getDefault().unregister(FOO_SOURCES, cps.toArray(new ClassPath[0]));
        cps = GlobalPathRegistry.getDefault().getPaths(FOO_PLATFORM);
        GlobalPathRegistry.getDefault().unregister(FOO_PLATFORM, cps.toArray(new ClassPath[0]));
        cps = GlobalPathRegistry.getDefault().getPaths(FOO_BINARY);
        GlobalPathRegistry.getDefault().unregister(FOO_BINARY, cps.toArray(new ClassPath[0]));
        Collection<? extends URL> sources = PathRegistry.getDefault().getSources();
        Collection<? extends URL> binaryLibraries = PathRegistry.getDefault().getBinaryLibraries();
        assertEquals (0,sources.size());
        assertEquals (0,binaryLibraries.size());


        ClassPath src = ClassPathSupport.createClassPath(new FileObject[] {srcRoot1, srcRoot2, srcRoot3});
        ClassPath libs = ClassPathSupport.createClassPath(new FileObject[] {compRoot1, compRoot2});
        ClassPath platform = ClassPathSupport.createClassPath(new FileObject[] {bootRoot1, bootRoot2});

        GlobalPathRegistry.getDefault().register(FOO_SOURCES, new ClassPath[] {src});
        GlobalPathRegistry.getDefault().register(FOO_BINARY, new ClassPath[] {libs});
        GlobalPathRegistry.getDefault().register(FOO_PLATFORM, new ClassPath[] {platform});

        Collection <? extends URL>  res = PathRegistry.getDefault().getSources();
        assertEquals(new FileObject[] {srcRoot1, srcRoot2, srcRoot3}, res);
        res = PathRegistry.getDefault().getLibraries();
        assertEquals(new FileObject[] {compSrc1, compSrc2, bootSrc1}, res);
        res = PathRegistry.getDefault().getBinaryLibraries();
        assertEquals(new FileObject[] {bootRoot2}, res);

        ClassPath compile2 = ClassPathSupport.createClassPath(new FileObject[] {unknown1, unknown2});
        GlobalPathRegistry.getDefault().register(FOO_BINARY, new ClassPath[] {compile2});

        res = PathRegistry.getDefault().getSources();
        assertEquals(new FileObject[] {srcRoot1, srcRoot2, srcRoot3}, res);
        res = PathRegistry.getDefault().getLibraries();
        assertEquals(new FileObject[] {compSrc1, compSrc2, bootSrc1, unknownSrc2}, res);
        res = PathRegistry.getDefault().getBinaryLibraries();
        assertEquals(new FileObject[] {bootRoot2, unknown1}, res);
    }

    public void testExcludeEvents () throws Exception {
        List<PRI> resources = new ArrayList<PRI>(2);
        resources.add(new PRI (srcRoot1.getURL()));
        resources.add(new PRI (srcRoot2.getURL()));
        ClassPath cp = ClassPathSupport.createClassPath(resources);

        class L implements PropertyChangeListener {

            Set<Object> ids = new HashSet<Object> ();

            public void propertyChange(PropertyChangeEvent e) {
                if (ClassPath.PROP_INCLUDES.equals(e.getPropertyName())) {
                    ids.add (e.getPropagationId());
                }
            }
        }
        L l = new L ();
        cp.addPropertyChangeListener(l);
        Object propId = "ID0";
        for (PRI pri : resources) {
            pri.firePropertyChange(propId);
        }
        cp.getRoots();
        propId = "ID1";
        for (PRI pri : resources) {
            pri.firePropertyChange(propId);
        }
        assertEquals(1, l.ids.size());
        propId = "ID2";
        for (PRI pri : resources) {
            pri.firePropertyChange(propId);
        }
        assertEquals(2, l.ids.size());
    }

    public void testURLNoHostPart() throws Exception {
        URL url = new URL ("file:///tmp/foo/");     //NOI18N
        assertTrue(PathRegistry.noHostPart(url));
        url = new URL ("jar:file:///tmp/foo.zip!/");     //NOI18N
        assertTrue(PathRegistry.noHostPart(url));
        url = new URL ("nbfs://nbhost/SystemFileSystem/PHP/RuntimeLibraries/");     //NOI18N
        assertTrue(PathRegistry.noHostPart(url));
        url = new URL ("jar:nbfs://nbhost/SystemFileSystem/PHP/RuntimeLibraries.zip!/");     //NOI18N
        assertTrue(PathRegistry.noHostPart(url));
        url = new URL ("http://myhost/foo/");     //NOI18N
        assertFalse(PathRegistry.noHostPart(url));
    }

    private static Collection<URL> collectResults () {
        final PathRegistry pr = PathRegistry.getDefault();
        final List<URL> result = new LinkedList<URL>();
        result.addAll(pr.getSources());
        result.addAll(pr.getBinaryLibraries());
        result.addAll(pr.getUnknownRoots());
        return result;
    }

    public static class PRI implements FilteringPathResourceImplementation {


        private final URL root;
        private final PropertyChangeSupport support;


        public PRI (URL root) {
            this.root = root;
            this.support = new PropertyChangeSupport (this);
        }


        public boolean includes(URL root, String resource) {
            return true;
        }

        public URL[] getRoots() {
            return new URL[] {root};
        }

        public ClassPathImplementation getContent() {
            return null;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            this.support.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            this.support.removePropertyChangeListener(listener);
        }

        public void firePropertyChange (final Object propId) {
            PropertyChangeEvent event = new PropertyChangeEvent (this,FilteringPathResourceImplementation.PROP_INCLUDES,null,null);
            event.setPropagationId(propId);
            this.support.firePropertyChange(event);
        }
    }

    private static void gc () {
        System.gc();
        long[][] spaces = new long[100][];
        for (int i=0; i<spaces.length; i++) {
            spaces[i] = new long[1000];
        }
        System.gc();
        spaces = null;
        System.gc();
    }

    private void assertEquals (FileObject[] expected, Collection<? extends URL> result) throws IOException {
        assertEquals(expected.length, result.size());
        Set<URL> expectedUrls = new HashSet<URL> ();
        for (FileObject fo : expected) {
            expectedUrls.add(fo.getURL());
        }
        for (URL url : result) {
            if (!expectedUrls.remove (url)) {
                assertTrue (String.format("Unknown URL: %s in: %s", url, expectedUrls),false);
            }
        }
        assertEquals(expectedUrls.toString(),0,expectedUrls.size());
    }
    

    private static class MutableClassPathImplementation implements ClassPathImplementation {

        private final List<PathResourceImplementation> res;
        private final PropertyChangeSupport support;

        public MutableClassPathImplementation () {
            res = new ArrayList<PathResourceImplementation> ();
            support = new PropertyChangeSupport (this);
        }

        public void addResource (FileObject fo) throws IOException {
            res.add(ClassPathSupport.createResource(fo.getURL()));
            this.support.firePropertyChange(PROP_RESOURCES,null,null);
        }

        public void removeResource (FileObject fo) throws IOException {
            URL url = fo.getURL();
            for (Iterator<PathResourceImplementation> it = res.iterator(); it.hasNext(); ) {
                PathResourceImplementation r = it.next();
                if (url.equals(r.getRoots()[0])) {
                    it.remove();
                    this.support.firePropertyChange(PROP_RESOURCES,null,null);
                }
            }
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            support.addPropertyChangeListener(listener);
        }

        public List<PathResourceImplementation> getResources() {
            return res;
        }

    }

    public static class SFBQImpl implements SourceForBinaryQueryImplementation {

        static final Map<URL,FileObject> map = new HashMap<URL,FileObject> ();
        static final Map<URL,Result> results = new HashMap<URL,Result> ();

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
            FileObject srcRoot = SFBQImpl.map.get(binaryRoot);
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

            public FileObject[] getRoots() {
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

    public static class DeadLockSFBQImpl implements SourceForBinaryQueryImplementation {

        private static final Object lock = new String ("Lock");

        public DeadLockSFBQImpl () {
        }

        public static Object getLock () {
            return lock;
        }

        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            synchronized (lock) {
                lock.toString();
            }
            return null;
        }
    }

    public static class FooPathRecognizer extends PathRecognizer {
        
        @Override
        public Set<String> getSourcePathIds() {
            return Collections.singleton(FOO_SOURCES);
        }

        @Override
        public Set<String> getBinaryLibraryPathIds() {
            final Set<String> res = new HashSet<String>();
            res.add(FOO_PLATFORM);
            res.add(FOO_BINARY);
            return res;
        }

        @Override
        public Set<String> getLibraryPathIds() {
            return null;
        }

        @Override
        public Set<String> getMimeTypes() {
            return Collections.singleton("text/foo");
        }        

    }

    private static class PRListener implements PathRegistryListener {

        private final CountDownLatch latch = new CountDownLatch(1);
        private final PathRegistry pr = PathRegistry.getDefault();

        public PRListener () {
            this.pr.addPathRegistryListener(this);
        }
                    
        public boolean await () throws InterruptedException {
            try {
                return this.latch.await(5000, TimeUnit.MILLISECONDS);
            } finally {
                this.pr.removePathRegistryListener(this);
            }
        }

        public void pathsChanged(PathRegistryEvent event) {
            if (this.pr == event.getSource()) {
                latch.countDown();
            }
        }
    }

}
