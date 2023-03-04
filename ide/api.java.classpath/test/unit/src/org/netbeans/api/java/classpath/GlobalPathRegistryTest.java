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

package org.netbeans.api.java.classpath;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.support.PathResourceBase;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;

/**
 * Test functionality of GlobalPathRegistry.
 * @author Jesse Glick
 */
public class GlobalPathRegistryTest extends NbTestCase {
    
    public GlobalPathRegistryTest(String name) {
        super(name);
        MockServices.setServices(SFBQImpl.class, DeadLockSFBQImpl.class);
    }
    
    private GlobalPathRegistry r;
    private FileObject root;
    private ClassPath cp1, cp2, cp3, cp4, cp5;
    protected @Override void setUp() throws Exception {
        super.setUp();
        r = GlobalPathRegistry.getDefault();
        r.clear();
        root = FileUtil.createMemoryFileSystem().getRoot();
        cp1 = ClassPathSupport.createClassPath(new FileObject[] {root.createFolder("1")});
        cp2 = ClassPathSupport.createClassPath(new FileObject[] {root.createFolder("2")});
        cp3 = ClassPathSupport.createClassPath(new FileObject[] {root.createFolder("3")});
        cp4 = ClassPathSupport.createClassPath(new FileObject[] {root.createFolder("4")});
        cp5 = ClassPathSupport.createClassPath(new FileObject[] {root.createFolder("5")});
    }
    
    public void testBasicOperation() throws Exception {
        assertEquals("initially no paths of type a", Collections.<ClassPath>emptySet(), r.getPaths("a"));
        r.register("a", new ClassPath[] {cp1, cp2});
        assertEquals("added some paths of type a", new HashSet<ClassPath>(Arrays.asList(new ClassPath[] {cp1, cp2})), r.getPaths("a"));
        r.register("a", new ClassPath[0]);
        assertEquals("did not add any new paths to a", new HashSet<ClassPath>(Arrays.asList(new ClassPath[] {cp1, cp2})), r.getPaths("a"));
        assertEquals("initially no paths of type b", Collections.<ClassPath>emptySet(), r.getPaths("b"));
        r.register("b", new ClassPath[] {cp3, cp4, cp5});
        assertEquals("added some paths of type b", new HashSet<ClassPath>(Arrays.asList(new ClassPath[] {cp3, cp4, cp5})), r.getPaths("b"));
        r.unregister("a", new ClassPath[] {cp1});
        assertEquals("only one path left of type a", Collections.<ClassPath>singleton(cp2), r.getPaths("a"));
        r.register("a", new ClassPath[] {cp2, cp3});
        assertEquals("only one new path added of type a", new HashSet<ClassPath>(Arrays.asList(new ClassPath[] {cp2, cp3})), r.getPaths("a"));
        r.unregister("a", new ClassPath[] {cp2});
        assertEquals("still have extra cp2 in a", new HashSet<ClassPath>(Arrays.asList(new ClassPath[] {cp2, cp3})), r.getPaths("a"));
        r.unregister("a", new ClassPath[] {cp2});
        assertEquals("last cp2 removed from a", Collections.<ClassPath>singleton(cp3), r.getPaths("a"));
        r.unregister("a", new ClassPath[] {cp3});
        assertEquals("a now empty", Collections.<ClassPath>emptySet(), r.getPaths("a"));
        r.unregister("a", new ClassPath[0]);
        assertEquals("a still empty", Collections.<ClassPath>emptySet(), r.getPaths("a"));
        try {
            r.unregister("a", new ClassPath[] {cp3});
            fail("should not have been permitted to unregister a nonexistent entry");
        } catch (IllegalArgumentException x) {
            // Good.
        }
    }
    
    public void testListening() throws Exception {
        assertEquals("initially no paths of type b", Collections.<ClassPath>emptySet(), r.getPaths("b"));
        L l = new L();
        r.addGlobalPathRegistryListener(l);
        r.register("b", new ClassPath[] {cp1, cp2});
        GlobalPathRegistryEvent e = l.event();
        assertNotNull("got an event", e);
        assertTrue("was an addition", l.added());
        assertEquals("right registry", r, e.getRegistry());
        assertEquals("right ID", "b", e.getId());
        assertEquals("right changed paths", new HashSet<ClassPath>(Arrays.asList(new ClassPath[] {cp1, cp2})), e.getChangedPaths());
        r.register("b", new ClassPath[] {cp2, cp3});
        e = l.event();
        assertNotNull("got an event", e);
        assertTrue("was an addition", l.added());
        assertEquals("right changed paths", Collections.<ClassPath>singleton(cp3), e.getChangedPaths());
        r.register("b", new ClassPath[] {cp3});
        e = l.event();
        assertNull("no event for adding a dupe", e);
        r.unregister("b", new ClassPath[] {cp1, cp3, cp3});
        e = l.event();
        assertNotNull("got an event", e);
        assertFalse("was a removal", l.added());
        assertEquals("right changed paths", new HashSet<ClassPath>(Arrays.asList(new ClassPath[] {cp1, cp3})), e.getChangedPaths());
        r.unregister("b", new ClassPath[] {cp2});
        e = l.event();
        assertNull("no event for removing an extra", e);
        r.unregister("b", new ClassPath[] {cp2});
        e = l.event();
        assertNotNull("now an event for removing the last copy", e);
        assertFalse("was a removal", l.added());
        assertEquals("right changed paths", Collections.<ClassPath>singleton(cp2), e.getChangedPaths());
    }
    
    
    public void testGetSourceRoots () throws Exception {
        SFBQImpl query = Lookup.getDefault().lookup(SFBQImpl.class);
        assertNotNull ("SourceForBinaryQueryImplementation not found in lookup",query);                
        query.addPair(cp3.getRoots()[0].toURL(),new FileObject[0]);
        ClassPathTest.TestClassPathImplementation cpChangingImpl = new ClassPathTest.TestClassPathImplementation();
        ClassPath cpChanging = ClassPathFactory.createClassPath(cpChangingImpl);
        assertEquals("cpChangingImpl is empty", 0, cpChanging.getRoots().length);
        r.register(ClassPath.SOURCE, new ClassPath[] {cp1, cp2, cpChanging});
        r.register (ClassPath.COMPILE, new ClassPath[] {cp3});
        Set<FileObject> result = r.getSourceRoots();
        assertEquals ("Wrong number of source roots",result.size(),cp1.getRoots().length + cp2.getRoots().length);
        assertTrue ("Missing roots from cp1",result.containsAll (Arrays.asList(cp1.getRoots())));
        assertTrue ("Missing roots from cp2",result.containsAll (Arrays.asList(cp2.getRoots())));                
        // simulate classpath change:
        URL u = cp5.entries().get(0).getURL();
        cpChangingImpl.addResource(u);
        assertEquals("cpChangingImpl is not empty", 1, cpChanging.getRoots().length);
        result = r.getSourceRoots();
        assertEquals ("Wrong number of source roots",result.size(),cp1.getRoots().length + cp2.getRoots().length + cpChanging.getRoots().length);
        assertTrue ("Missing roots from cp1",result.containsAll (Arrays.asList(cp1.getRoots())));
        assertTrue ("Missing roots from cp2",result.containsAll (Arrays.asList(cp2.getRoots())));                
        cpChangingImpl.removeResource(u);
        
        query.addPair(cp3.getRoots()[0].toURL(),cp4.getRoots());
        result = r.getSourceRoots();
        assertEquals ("Wrong number of source roots",result.size(),cp1.getRoots().length + cp2.getRoots().length+cp4.getRoots().length);
        assertTrue ("Missing roots from cp1",result.containsAll (Arrays.asList(cp1.getRoots())));
        assertTrue ("Missing roots from cp2",result.containsAll (Arrays.asList(cp2.getRoots())));
        assertTrue ("Missing roots from cp4",result.containsAll (Arrays.asList(cp4.getRoots())));

        // #158105: findResource should also use the same set.
        FileObject res = FileUtil.createData(root, "4/some/resource");
        assertEquals(res, r.findResource("some/resource"));
    }
    
    /**
     * Tests issue: #60976:Deadlock between JavaFastOpen$Evaluator and AntProjectHelper$something
     */
    public void testGetSourceRootsDeadLock () throws Exception {        
        DeadLockSFBQImpl query = Lookup.getDefault().lookup(DeadLockSFBQImpl.class);
        assertNotNull ("SourceForBinaryQueryImplementation not found in lookup",query);        
        r.register (ClassPath.COMPILE, new ClassPath[] {cp1});
        try {            
            query.setSynchronizedJob (
                new Runnable () {
                    public void run () {
                        r.register(ClassPath.COMPILE, new ClassPath[] {cp2});
                    }
                }
            );
            r.getSourceRoots();
        } finally {
            query.setSynchronizedJob (null);
        }
    }

    public void testFindResource() throws Exception {
        final FileObject src1 = root.createFolder("src1");
        FileObject src1included = FileUtil.createData(src1, "included/file");
        FileUtil.createData(src1, "excluded/file1");
        FileUtil.createData(src1, "excluded/file2");
        FileObject src2 = root.createFolder("src2");
        FileObject src2included = FileUtil.createData(src2, "included/file");
        FileObject src2excluded1 = FileUtil.createData(src2, "excluded/file1");
        class PRI extends PathResourceBase implements FilteringPathResourceImplementation {
            public URL[] getRoots() {
                    return new URL[] {src1.toURL()};
            }
            public boolean includes(URL root, String resource) {
                return resource.startsWith("incl");
            }
            public ClassPathImplementation getContent() {
                return null;
            }
        }
        r.register(ClassPath.SOURCE, new ClassPath[] {
            ClassPathSupport.createClassPath(Collections.singletonList(new PRI())),
            ClassPathSupport.createClassPath(new FileObject[] {src2})
        });
        assertTrue(Arrays.asList(src1included, src2included).contains(r.findResource("included/file")));
        assertEquals(src2excluded1, r.findResource("excluded/file1"));
        assertEquals(null, r.findResource("excluded/file2"));
        assertEquals(null, r.findResource("nonexistent"));
    }
    
    public void testMemoryLeak124055 () throws Exception {
        final GlobalPathRegistry reg = GlobalPathRegistry.getDefault();
        final Set<? extends ClassPath> src = reg.getPaths(ClassPath.SOURCE);
        final Set<? extends ClassPath> boot = reg.getPaths(ClassPath.BOOT);
        final Set<? extends ClassPath> compile = reg.getPaths(ClassPath.COMPILE);
        assertTrue(src.isEmpty());
        assertTrue(boot.isEmpty());
        assertTrue(compile.isEmpty());
        assertEquals(Collections.<FileObject>emptySet(), reg.getSourceRoots());
        r.register(ClassPath.COMPILE, new ClassPath[] {cp3});
        SFBQImpl query = Lookup.getDefault().lookup(SFBQImpl.class);
        query.addPair(cp3.getRoots()[0].toURL(),cp4.getRoots());
        //There should be one translated source root
        assertEquals(1, reg.getSourceRoots().size());
        assertEquals(1, reg.getResults().size());
        r.unregister(ClassPath.COMPILE, new ClassPath[] {cp3});
        //There shouldn't be registered source root
        assertTrue(reg.getSourceRoots().isEmpty());
        assertTrue(reg.getResults().isEmpty());
    }
    
    private static final class L implements GlobalPathRegistryListener {
        
        private GlobalPathRegistryEvent e;
        private boolean added;
        
        public L() {}
        
        public synchronized GlobalPathRegistryEvent event() {
            GlobalPathRegistryEvent _e = e;
            e = null;
            return _e;
        }
        
        public boolean added() {
            return added;
        }
        
        public synchronized void pathsAdded(GlobalPathRegistryEvent e) {
            assertNull("checked for last event", this.e);
            this.e = e;
            added = true;
        }
        
        public synchronized void pathsRemoved(GlobalPathRegistryEvent e) {
            assertNull("checked for last event", this.e);
            this.e = e;
            added = false;
        }
        
    }
    
    
    public static class SFBQImpl implements SourceForBinaryQueryImplementation {
        
        private Map<URL,SourceForBinaryQuery.Result> pairs = new HashMap<URL,SourceForBinaryQuery.Result> ();
        
        void addPair (URL binaryRoot, FileObject[] sourceRoots) {
            assert binaryRoot != null && sourceRoots != null;
            Result r = (Result) this.pairs.get (binaryRoot);
            if (r == null) {
                r = new Result (sourceRoots);
                this.pairs.put (binaryRoot, r);
            }
            else {
                r.setSources(sourceRoots);
            }
        }
                        
        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            Result result = (Result) this.pairs.get (binaryRoot);
            return result;
        }
        
        
        private static class Result implements SourceForBinaryQuery.Result {
            
            private FileObject[] sources;                        
            private final ChangeSupport changeSupport = new ChangeSupport(this);
            
            public Result (FileObject[] sources) {
                this.sources = sources;
            }
            
            
            void setSources (FileObject[] sources) {
                this.sources = sources;
                this.changeSupport.fireChange ();
            }
                        
            public void addChangeListener(javax.swing.event.ChangeListener l) {
                changeSupport.addChangeListener (l);
            }            
            
            public FileObject[] getRoots() {
                return this.sources;
            }
            
            public void removeChangeListener(javax.swing.event.ChangeListener l) {
                changeSupport.removeChangeListener (l);
            }
            
        }
        
    }
    
    public static class DeadLockSFBQImpl extends Thread implements SourceForBinaryQueryImplementation {
        
        private Runnable r;
        
        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            if (this.r != null) {                
                synchronized (this) {
                    this.start();
                    try {
                        this.wait ();
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }
            return null;
        }
        
        public @Override synchronized void run () {
            r.run();
            this.notify();
        }
        
        public void setSynchronizedJob (Runnable r) {
            this.r = r;
        }
        
    }
    
}
