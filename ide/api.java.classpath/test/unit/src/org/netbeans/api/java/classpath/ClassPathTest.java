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

package org.netbeans.api.java.classpath;

import java.io.File;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileOutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Utilities;
import org.openide.util.test.MockPropertyChangeListener;

public class ClassPathTest extends NbTestCase {

    public ClassPathTest(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    private File getBaseDir() throws Exception {
        return FileUtil.normalizeFile(getWorkDir());
    }

    /**
     * Tests ClassPath.getResourceName ();
     */
    public void testGetResourceName() throws Exception {
        File f = getBaseDir();        
        f = new File(f.getPath()+"/w.e.i.r.d/f  o  l  d  e  r");
        f.mkdirs();
        File f2 = new File(f, "org/netbeans/test");
        f2.mkdirs();
        File f3 = new File(f2, "Main.java");
        f3.createNewFile();
        File f4 = new File(f2, ".dotname");
        f4.createNewFile();
        File f5 = new File(f2, ".dotname.dotext");
        f5.createNewFile();

        FileObject cpRoot = FileUtil.toFileObject(f);
        FileObject cpItem = FileUtil.toFileObject(f2);
        FileObject clazz = FileUtil.toFileObject(f3);
        FileObject dotname = FileUtil.toFileObject(f4);
        FileObject dotnameDotext = FileUtil.toFileObject(f5);
        ClassPath cp = ClassPathSupport.createClassPath(new FileObject[]{cpRoot});
        String pkg = cp.getResourceName(cpItem);
        assertEquals("org/netbeans/test", pkg);
        
        pkg = cp.getResourceName(cpItem, '.', true);
        assertEquals("org.netbeans.test", pkg);
        
        pkg = cp.getResourceName(cpItem, '.', false);
        assertEquals("org.netbeans.test", pkg);
        
        pkg = cp.getResourceName(cpItem, '#', true);
        assertEquals("org#netbeans#test", pkg);
        
        pkg = cp.getResourceName(cpItem, '#', false);
        assertEquals("org#netbeans#test", pkg);
        
        pkg = cp.getResourceName(clazz);
        assertEquals("org/netbeans/test/Main.java", pkg);
        
        pkg = cp.getResourceName(clazz, '.', true);
        assertEquals("org.netbeans.test.Main.java", pkg);
        
        pkg = cp.getResourceName(clazz, '.', false);
        assertEquals("org.netbeans.test.Main", pkg);
        
        pkg = cp.getResourceName(clazz, '@', true);
        assertEquals("org@netbeans@test@Main.java", pkg);
        
        pkg = cp.getResourceName(clazz, '@', false);
        assertEquals("org@netbeans@test@Main", pkg);

        //bug #152427
        pkg = cp.getResourceName(dotname, '/', false);
        assertEquals("org/netbeans/test/.dotname", pkg);
        pkg = cp.getResourceName(dotnameDotext, '/', false);
        assertEquals("org/netbeans/test/.dotname", pkg);
    }
    
    /**
     * Tests ClassPath.findAllResources(), ClassPath.findResoruce(), 
     * ClassPath.contains (), ClassPath.findOwnerRoot(),
     * ClassPath.isResourceVisible ()
     */
    public void testGetResource () throws Exception {
        File root_1 = new File (getBaseDir(),"root_1");
        root_1.mkdir();
        File root_2 = new File (getBaseDir(),"root_2");
        root_2.mkdir();
        FileObject[] roots = new FileObject [] {
            FileUtil.toFileObject(root_1),
            FileUtil.toFileObject(root_2),
        };
        
        FileObject tmp = roots[0].createFolder("org");
        tmp = tmp.createFolder("me");
        FileObject testFo_1 = tmp.createData("Foo","txt");
        tmp = roots[1].createFolder("org");
        tmp = tmp.createFolder("me");
        FileObject testFo_2 = tmp.createData("Foo","txt");        
        ClassPath cp = ClassPathSupport.createClassPath(roots);        
        
        //findResource
        assertTrue(cp.findResource ("org/me/Foo.txt")==testFo_1);
        assertTrue (cp.findResource("org/me/None.txt")==null);
        
        //findAllResources
        List<FileObject> res = cp.findAllResources ("org/me/Foo.txt");
        assertTrue (res.size() == 2);
        assertTrue (res.contains(testFo_1));
        assertTrue (res.contains(testFo_2));
        
        //contains
        assertTrue (cp.contains (testFo_1));
        assertTrue (cp.contains (testFo_2));
        assertFalse (cp.contains (roots[0].getParent()));
        
        //findOwnerRoot
        assertTrue (cp.findOwnerRoot(testFo_1)==roots[0]);
        assertTrue (cp.findOwnerRoot(testFo_2)==roots[1]);

        /*
        //isResourceVisible
        assertTrue (cp.isResourceVisible(testFo_1));
        assertFalse (cp.isResourceVisible(testFo_2));
         */
        
        cp = null;
        roots[0].delete();
        roots[1].delete();
    }
    
    /**
     * Test ClassPath.getRoots(), ClassPath.addPropertyChangeListener (),
     * ClassPath.entries () and classpath SPI.
     */
    public void testListening() throws Exception {
        // XXX unreliable, would be improved by usage of TestFileUtils methods:

        File root_1 = new File (getBaseDir(),"root_1");
        root_1.mkdir();
        File root_2 = new File (getBaseDir(),"root_2");
        root_2.mkdir();
        File root_3 = new File (getBaseDir(),"root_3.jar");
        JarOutputStream out = new JarOutputStream ( new FileOutputStream (root_3));
        try {            
            out.putNextEntry(new ZipEntry("test.txt"));
            out.write ("test".getBytes());
        } finally {
            out.close ();
        }        
        assertNotNull("Cannot find file",FileUtil.toFileObject(root_1));
        assertNotNull("Cannot find file",FileUtil.toFileObject(root_2));
        assertNotNull("Cannot find file",FileUtil.toFileObject(root_3));
        TestClassPathImplementation impl = new TestClassPathImplementation();
	ClassPath cp = ClassPathFactory.createClassPath (impl);
        impl.addResource(Utilities.toURI(root_1).toURL());
        cp.addPropertyChangeListener (impl);
        impl.addResource (Utilities.toURI(root_2).toURL());
        impl.assertEvents(ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS);
        assertTrue (cp.getRoots().length==2);
        impl.removeResource (Utilities.toURI(root_2).toURL());
        impl.assertEvents(ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS);
        assertTrue (cp.getRoots().length==1);
        FileObject fo = cp.getRoots()[0];
        FileObject parentFolder = fo.getParent();        
        fo.delete();
        impl.assertEvents(ClassPath.PROP_ROOTS);
        assertTrue (cp.getRoots().length==0);
        parentFolder.createFolder("root_1");
        assertTrue (cp.getRoots().length==1);
        impl.assertEvents(ClassPath.PROP_ROOTS);
        FileObject archiveFile = FileUtil.toFileObject(root_3);
        impl.addResource(FileUtil.getArchiveRoot(archiveFile.toURL()));
        assertEquals (cp.getRoots().length,2);
        impl.assertEvents(ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS);
        root_3.delete();
        root_3 = new File (getBaseDir(),"root_3.jar");
        Thread.sleep(1000);
        out = new JarOutputStream ( new FileOutputStream (root_3));
        try {            
            out.putNextEntry(new ZipEntry("test2.txt"));
            out.write ("test2".getBytes());
        } finally {
            out.close ();
        }
        archiveFile.refresh();
        impl.assertEvents(ClassPath.PROP_ROOTS);
        root_1.delete();
        root_2.delete();
        root_3.delete();
        cp = null;
    }

    public void testListening2() throws Exception {
        // Checks that changes in PathResourceImplementation.PROP_ROOTS matter.
        class FiringPRI implements PathResourceImplementation {
            private URL[] roots = new URL[0];
            public URL[] getRoots() {
                return roots;
            }
            void changeRoots(URL[] nue) {
                roots = nue;
                pcs.firePropertyChange(PROP_ROOTS, null, null);
            }
            public ClassPathImplementation getContent() {
                return null;
            }
            PropertyChangeSupport pcs = new PropertyChangeSupport(this);
            public void addPropertyChangeListener(PropertyChangeListener listener) {
                pcs.addPropertyChangeListener(listener);
            }
            public void removePropertyChangeListener(PropertyChangeListener listener) {
                pcs.removePropertyChangeListener(listener);
            }
        }
        FiringPRI pri = new FiringPRI();
        TestClassPathImplementation impl = new TestClassPathImplementation();
        impl.addResource(pri);
        ClassPath cp = ClassPathFactory.createClassPath(impl);
        assertEquals(Collections.emptyList(), Arrays.asList(cp.getRoots()));
        cp.addPropertyChangeListener(impl);
        File d = new File(getBaseDir(), "d");
        d.mkdir();
        pri.changeRoots(new URL[] {Utilities.toURI(d).toURL()});
        impl.assertEvents(ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS);
        assertEquals(Collections.singletonList(FileUtil.toFileObject(d)), Arrays.asList(cp.getRoots()));
    }
    
    public void testChangesAcknowledgedWithoutListener() throws Exception {
        // Discovered in #72573.
        clearWorkDir();
        ClassPath cp = ClassPathSupport.createClassPath(FileUtil.urlForArchiveOrDir(new File(getWorkDir(), "root")));
        assertEquals("nothing there yet", null, cp.findResource("f"));
        FileObject f = FileUtil.createData(FileUtil.toFileObject(getWorkDir()), "root/f");
        assertEquals("found new file", f, cp.findResource("f"));
        f.delete();
        assertEquals("again empty", null, cp.findResource("f"));
    }

    public void testFlags() {
        final FlaggedClassPathImpl fcpImpl = new FlaggedClassPathImpl();
        final ClassPath cp = ClassPathFactory.createClassPath(fcpImpl);
        Set<ClassPath.Flag> flags = cp.getFlags();
        assertNotNull(flags);
        assertTrue(flags.isEmpty());
        fcpImpl.setFlags(EnumSet.of(ClassPath.Flag.INCOMPLETE));
        flags = cp.getFlags();
        assertNotNull(flags);
        assertEquals(1, flags.size());
        assertEquals(ClassPath.Flag.INCOMPLETE, flags.iterator().next());
        fcpImpl.setFlags(EnumSet.noneOf(ClassPath.Flag.class));
        flags = cp.getFlags();
        assertNotNull(flags);
        assertTrue(flags.isEmpty());
    }

    public void testFlagsFiring() {
        final FlaggedClassPathImpl fcpImpl = new FlaggedClassPathImpl();
        final ClassPath cp = ClassPathFactory.createClassPath(fcpImpl);
        Set<ClassPath.Flag> flags = cp.getFlags();
        assertNotNull(flags);
        assertTrue(flags.isEmpty());
        final MockPropertyChangeListener pl = new MockPropertyChangeListener(ClassPath.PROP_FLAGS);
        cp.addPropertyChangeListener(pl);
        fcpImpl.setFlags(EnumSet.of(ClassPath.Flag.INCOMPLETE));
        pl.assertEventCount(1);
        fcpImpl.setFlags(EnumSet.noneOf(ClassPath.Flag.class));
        pl.assertEventCount(1);
    }

    static final class TestClassPathImplementation implements ClassPathImplementation, PropertyChangeListener {

        private final PropertyChangeSupport support = new PropertyChangeSupport (this);
        private final List<PathResourceImplementation> resources = new ArrayList<PathResourceImplementation> ();
        private final SortedSet<String> events = new TreeSet<String>();

        public synchronized void addResource (URL resource) {
            PathResourceImplementation pr = ClassPathSupport.createResource (resource);
            addResource(pr);
        }

        public synchronized void addResource(PathResourceImplementation pr) {
            this.resources.add (pr);
            this.support.firePropertyChange (ClassPathImplementation.PROP_RESOURCES,null,null);
        }

        public synchronized void removeResource (URL resource) {
            for (Iterator<PathResourceImplementation> it = this.resources.iterator(); it.hasNext();) {
                PathResourceImplementation pr = it.next ();
                if (Arrays.asList(pr.getRoots()).contains (resource)) {
                    this.resources.remove (pr);
                    this.support.firePropertyChange (ClassPathImplementation.PROP_RESOURCES,null,null);
                    break;
                }
            }
        }

        public synchronized List<? extends PathResourceImplementation> getResources() {
            return this.resources;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            this.support.addPropertyChangeListener (listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            this.support.removePropertyChangeListener (listener);
        }

        public void propertyChange (PropertyChangeEvent event) {
            events.add(event.getPropertyName());
        }

        void assertEvents(String... events) {
            assertEquals(new TreeSet<String>(Arrays.asList(events)), this.events);
            this.events.clear();
        }
    }

    public void testFilteredClassPaths() throws Exception {
        FileObject bd = FileUtil.toFileObject(getBaseDir());
        FileObject u1fo = bd.createFolder("u1");
        FileObject u2fo = bd.createFolder("u2");
        final URL u1 = u1fo.toURL();
        final URL u2 = u2fo.toURL();
        class FPRI implements FilteringPathResourceImplementation {
            private int modulus = 2;
            public void changeIncludes(int modulus) {
                this.modulus = modulus;
                pcs.firePropertyChange(PROP_INCLUDES, null, null);
            }
            public URL[] getRoots() {
                return new URL[] {u1, u2};
            }
            public boolean includes(URL root, String resource) {
                int offset;
                if (root.equals(u1)) {
                    offset = 0;
                } else if (root.equals(u2)) {
                    offset = 1;
                } else {
                    throw new IllegalArgumentException(root.toString());
                }
                return (offset + resource.length()) % modulus == 0;
            }
            public ClassPathImplementation getContent() {
                return null;
            }
            private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
            public void addPropertyChangeListener(PropertyChangeListener listener) {
                pcs.addPropertyChangeListener(listener);
            }
            public void removePropertyChangeListener(PropertyChangeListener listener) {
                pcs.removePropertyChangeListener(listener);
            }
        }
        FPRI pr = new FPRI();
        TestClassPathImplementation impl = new TestClassPathImplementation();
        impl.addResource(pr);
        ClassPath cp = ClassPathFactory.createClassPath(impl);
        FileObject xx1 = u1fo.createData("xx");
        FileObject xxx1 = u1fo.createData("xxx");
        FileObject xy1 = FileUtil.createData(u1fo, "x/y");
        FileObject x_1 = u1fo.createData("x ");
        String cau = "\u010Dau";
        FileObject cau1 = u1fo.createData(cau);
        FileObject folder = u1fo.createFolder("folder");
        FileObject foldr = u1fo.createFolder("foldr");
        FileObject xx2 = u2fo.createData("xx");
        FileObject xxx2 = u2fo.createData("xxx");
        FileObject xy2 = FileUtil.createData(u2fo, "x/y");
        FileObject x_2 = u2fo.createData("x ");
        FileObject cau2 = u2fo.createData(cau);
        assertEquals(Arrays.asList(u1fo, u2fo), Arrays.asList(cp.getRoots()));
        assertTrue(cp.contains(xx1));
        assertTrue(cp.contains(x_1));
        assertFalse(cp.contains(xxx1));
        assertFalse(cp.contains(cau1));
        assertFalse(cp.contains(xy1));
        assertFalse(cp.contains(xx2));
        assertFalse(cp.contains(x_2));
        assertTrue(cp.contains(xxx2));
        assertTrue(cp.contains(cau2));
        assertTrue(cp.contains(xy2));
        assertFalse(cp.contains(folder));
        assertTrue(cp.contains(foldr));
        assertEquals(xx1, cp.findResource("xx"));
        assertEquals(x_1, cp.findResource("x "));
        assertEquals(xxx2, cp.findResource("xxx"));
        assertEquals(cau2, cp.findResource(cau));
        assertEquals(xy2, cp.findResource("x/y"));
        assertEquals(null, cp.findResource("folder"));
        assertEquals(foldr, cp.findResource("foldr"));
        assertEquals(Collections.singletonList(xx1), cp.findAllResources("xx"));
        assertEquals(Collections.singletonList(x_1), cp.findAllResources("x "));
        assertEquals(Collections.singletonList(xxx2), cp.findAllResources("xxx"));
        assertEquals(Collections.singletonList(cau2), cp.findAllResources(cau));
        assertEquals(Collections.singletonList(xy2), cp.findAllResources("x/y"));
        assertEquals(Collections.emptyList(), cp.findAllResources("folder"));
        assertEquals(Collections.singletonList(foldr), cp.findAllResources("foldr"));
        assertEquals("xx", cp.getResourceName(xx1));
        assertEquals("x ", cp.getResourceName(x_1));
        assertEquals("xxx", cp.getResourceName(xxx1));
        assertEquals(cau, cp.getResourceName(cau1));
        assertEquals("x/y", cp.getResourceName(xy1));
        assertEquals("folder", cp.getResourceName(folder));
        assertEquals("foldr", cp.getResourceName(foldr));
        assertEquals(u1fo, cp.findOwnerRoot(xx1));
        assertEquals(u1fo, cp.findOwnerRoot(x_1));
        assertEquals(u1fo, cp.findOwnerRoot(xxx1));
        assertEquals(u1fo, cp.findOwnerRoot(cau1));
        assertEquals(u1fo, cp.findOwnerRoot(xy1));
        assertEquals(u1fo, cp.findOwnerRoot(folder));
        assertEquals(u1fo, cp.findOwnerRoot(foldr));
        assertTrue(cp.isResourceVisible(xx1));
        assertTrue(cp.isResourceVisible(x_1));
        assertFalse(cp.isResourceVisible(xxx1));
        assertFalse(cp.isResourceVisible(cau1));
        assertFalse(cp.isResourceVisible(xy1));
        assertFalse(cp.isResourceVisible(folder));
        assertTrue(cp.isResourceVisible(foldr));
        ClassPath.Entry e1 = cp.entries().get(0);
        assertTrue(e1.includes("xx"));
        assertTrue(e1.includes("x "));
        assertFalse(e1.includes("xxx"));
        assertFalse(e1.includes(cau));
        assertFalse(e1.includes("x/y"));
        assertFalse(e1.includes("folder/"));
        assertTrue(e1.includes("foldr/"));
        assertTrue(e1.includes(xx1));
        assertTrue(e1.includes(x_1));
        assertFalse(e1.includes(xxx1));
        assertFalse(e1.includes(cau1));
        assertFalse(e1.includes(xy1));
        assertFalse(e1.includes(folder));
        assertTrue(e1.includes(foldr));
        try {
            e1.includes(xx2);
            fail();
        } catch (IllegalArgumentException iae) {}
        assertTrue(e1.includes(xx1.toURL()));
        assertTrue(e1.includes(x_1.toURL()));
        assertFalse(e1.includes(xxx1.toURL()));
        assertFalse(e1.includes(cau1.toURL()));
        assertFalse(e1.includes(xy1.toURL()));
        assertFalse(e1.includes(folder.toURL()));
        assertTrue(e1.includes(foldr.toURL()));
        try {
            e1.includes(xx2.toURL());
            fail();
        } catch (IllegalArgumentException iae) {}
        cp.addPropertyChangeListener(impl);
        pr.changeIncludes(3);
        impl.assertEvents(ClassPath.PROP_INCLUDES);
        assertFalse(cp.contains(xx1));
        assertFalse(cp.contains(x_1));
        assertTrue(cp.contains(xxx1));
        assertTrue(cp.contains(cau1));
        assertTrue(cp.contains(xy1));
        assertTrue(cp.contains(xx2));
        assertTrue(cp.contains(x_2));
        assertFalse(cp.contains(xxx2));
        assertFalse(cp.contains(cau2));
        assertFalse(cp.contains(xy2));
        assertEquals(xx2, cp.findResource("xx"));
        assertEquals(x_2, cp.findResource("x "));
        assertEquals(xxx1, cp.findResource("xxx"));
        assertEquals(cau1, cp.findResource(cau));
        assertEquals(xy1, cp.findResource("x/y"));
        e1 = cp.entries().get(0);
        assertFalse(e1.includes("xx"));
        assertFalse(e1.includes("x "));
        assertTrue(e1.includes("xxx"));
        assertTrue(e1.includes(cau));
        assertTrue(e1.includes("x/y"));
        assertFalse(e1.includes(xx1));
        assertFalse(e1.includes(x_1));
        assertTrue(e1.includes(xxx1));
        assertTrue(e1.includes(cau1));
        assertTrue(e1.includes(xy1));
        assertFalse(e1.includes(xx1.toURL()));
        assertFalse(e1.includes(x_1.toURL()));
        assertTrue(e1.includes(xxx1.toURL()));
        assertTrue(e1.includes(cau1.toURL()));
        assertTrue(e1.includes(xy1.toURL()));
    }

    public void testFpriChangeFiring() throws Exception {
        class FPRI implements FilteringPathResourceImplementation {
            URL root;
            PropertyChangeSupport pcs = new PropertyChangeSupport(this);
            FPRI(URL root) {
                this.root = root;
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
                pcs.addPropertyChangeListener(listener);
            }
            public void removePropertyChangeListener(PropertyChangeListener listener) {
                pcs.removePropertyChangeListener(listener);
            }
            void fire(Object propid) {
                PropertyChangeEvent e = new PropertyChangeEvent(this, FilteringPathResourceImplementation.PROP_INCLUDES, null, null);
                e.setPropagationId(propid);
                pcs.firePropertyChange(e);
            }
        }
        FPRI fpri1 = new FPRI(FileUtil.urlForArchiveOrDir(new File(getWorkDir(), "src1")));
        FPRI fpri2 = new FPRI(FileUtil.urlForArchiveOrDir(new File(getWorkDir(), "src2")));
        class L implements PropertyChangeListener {
            int cnt;
            public void propertyChange(PropertyChangeEvent e) {
                if (ClassPath.PROP_INCLUDES.equals(e.getPropertyName())) {
                    cnt++;
                }
            }
        }
        ClassPath cp = ClassPathSupport.createClassPath(Arrays.asList(fpri1, fpri2));
        L l = new L();
        cp.addPropertyChangeListener(l);
        fpri1.fire(null);
        assertEquals(0, l.cnt);
        cp.entries();
        fpri1.fire(null);
        assertEquals(1, l.cnt);
        fpri2.fire(null);
        assertEquals(2, l.cnt);
        fpri1.fire("hello");
        assertEquals(3, l.cnt);
        fpri2.fire("goodbye");
        assertEquals(4, l.cnt);
        fpri1.fire("fixed");
        assertEquals(5, l.cnt);
        fpri2.fire("fixed");
        assertEquals(5, l.cnt);
        fpri1.fire("new");
        assertEquals(6, l.cnt);
    }

    public void testLeakingClassPath() throws Exception {
        ClassPath cp = ClassPathSupport.createClassPath(new URL("file:///a/"), new URL("file:///b/"));
        ClassPath proxyCP = ClassPathSupport.createProxyClassPath(cp);
        Reference<ClassPath> proxy = new WeakReference<ClassPath>(proxyCP);
        
        proxyCP.entries();
        
        proxyCP = null;
        
        assertGC("the proxy classpath needs to GCable", proxy);
    }
    
    public void testGetClassLoaderPerf () throws Exception {
        final String bootPathProp = System.getProperty("sun.boot.class.path");  //NOI18N
        List<URL> roots = new ArrayList<URL> ();
        StringTokenizer tk = new StringTokenizer (bootPathProp,File.pathSeparator);
        if (tk.hasMoreTokens()) {
            final String path = tk.nextToken();
            final File f = FileUtil.normalizeFile(new File (path));
            if (f.canRead()) {
                roots.add(Utilities.toURI(f).toURL());
            }
        }
        final ClassLoader bootLoader = new URLClassLoader(roots.toArray(new URL[0]), null);
        
        final String classPathProp = System.getProperty("java.class.path");     //NOI18N
        roots = new ArrayList<URL> ();
        List<URL> roots2 = new ArrayList<URL>();
        tk = new StringTokenizer (classPathProp,File.pathSeparator);
        while (tk.hasMoreTokens()) {
            final String path = tk.nextToken();
            final File f = FileUtil.normalizeFile(new File (path));
            if (!f.canRead()) {
                continue;
            }
            URL url = Utilities.toURI(f).toURL();
            roots2.add(url);
            if (FileUtil.isArchiveFile(url)) {
                url = FileUtil.getArchiveRoot(url);
            }
            roots.add(url);
        }
        
        final ClassPath cp = ClassPathSupport.createClassPath(roots.toArray(new URL[0]));
//        final ClassLoader loader = ClassLoaderSupport.create(cp,bootLoader);
//        final ClassLoader loader = new URLClassLoader(roots.toArray(new URL[roots.size()]),bootLoader);
        final ClassLoader loader = new URLClassLoader(roots2.toArray(new URL[0]),bootLoader);
        
        final Set<String> classNames = getClassNames (cp);
        int noLoaded = 0;
        int noFailed = 0;
        long st = System.currentTimeMillis();
        for (String className : classNames) {
            try {
                final Class<?> c = loader.loadClass(className);
                noLoaded++;
            } catch (ClassNotFoundException | NoClassDefFoundError | SecurityException e) {
                noFailed++;
            }
        }
        long et = System.currentTimeMillis();
        System.out.println("Loaded: " + noLoaded + " in: " + (et-st)+"ms");
    }

    public void testMemoryLeak183370() throws Exception {
        final File  wd = getWorkDir();
        class TestResource implements PathResourceImplementation {
            private final File f;
            private final PropertyChangeSupport sup = new PropertyChangeSupport(this);
            public TestResource(final File f){
                this.f=f;
            }
            @Override
            public URL[] getRoots() {
                return new URL[]{FileUtil.urlForArchiveOrDir(f)};
            }
            @Override
            public ClassPathImplementation getContent() {
                return null;
            }
            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {
                sup.addPropertyChangeListener(listener);
            }
            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {
                sup.removePropertyChangeListener(listener);
            }
        };
        class TestCPImpl implements ClassPathImplementation {
            private final List<? extends PathResourceImplementation> result = Arrays.asList(new TestResource(new File(wd,"1")), new TestResource(new File(wd,"2")));
            private final PropertyChangeSupport sup = new PropertyChangeSupport(this);

            @Override
            public List<? extends PathResourceImplementation> getResources() {
                return result;
            }
            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {
                sup.addPropertyChangeListener(listener);
            }
            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {
                sup.removePropertyChangeListener(listener);
            }
        };
        final TestCPImpl impl = new TestCPImpl();
        final ClassPath cp = ClassPathFactory.createClassPath(impl);
        assertEquals(2 ,cp.entries().size());
        assertEquals(impl.getResources().get(0).getRoots()[0] ,cp.entries().get(0).getURL());
        assertEquals(impl.getResources().get(1).getRoots()[0] ,cp.entries().get(1).getURL());
        assertEquals(1,((TestResource)impl.getResources().get(0)).sup.getPropertyChangeListeners().length);
        assertEquals(1,((TestResource)impl.getResources().get(1)).sup.getPropertyChangeListeners().length);
        impl.sup.firePropertyChange(ClassPathImplementation.PROP_RESOURCES,null,null);
        assertEquals(2 ,cp.entries().size());
        assertEquals(impl.getResources().get(0).getRoots()[0] ,cp.entries().get(0).getURL());
        assertEquals(impl.getResources().get(1).getRoots()[0] ,cp.entries().get(1).getURL());
        assertEquals(1,((TestResource)impl.getResources().get(0)).sup.getPropertyChangeListeners().length);
        assertEquals(1,((TestResource)impl.getResources().get(1)).sup.getPropertyChangeListeners().length);
    }
    
    private Set<String> getClassNames (final ClassPath cp) {
        Set<String> classNames = new HashSet<String> ();
        for (FileObject root : cp.getRoots()) {
            Enumeration<? extends FileObject> fos = root.getChildren(true);
            while (fos.hasMoreElements()) {
                FileObject fo = fos.nextElement();
                if (isImportant (fo)) {
                    classNames.add(cp.getResourceName(fo, '.', false));
                }
            }
        }
        return classNames;
    }
    
    private boolean isImportant (final FileObject fo) {
        if (fo.isFolder()) {
            return false;
        }
        if (!"class".equals(fo.getExt())) {      //NOI18N
            return false;
        }
        return !fo.getName().contains("$");     //NOI18N
                
    }

    public void testJVMPathConversion() throws Exception {
        String root = Utilities.toURI(getWorkDir()).toString();
        ClassPath cp = ClassPathSupport.createClassPath(
                new URL(root + "folder/"),
                new URL("jar:" + root + "file.zip!/"),
                new URL("jar:" + root + "file.zip!/subdir/"));
        assertEquals(massagePath("<root>/folder:<root>/file.zip"), cp.toString(ClassPath.PathConversionMode.SKIP));
        assertEquals(massagePath("<root>/folder:<root>/file.zip:") + "jar:" + root + "file.zip!/subdir/", cp.toString(ClassPath.PathConversionMode.PRINT));
        try {
            cp.toString(ClassPath.PathConversionMode.FAIL);
            fail();
        } catch (IllegalArgumentException x) {/* OK */}
        CharSequence warnings = Log.enable(ClassPath.class.getName(), Level.WARNING);
        assertEquals(massagePath("<root>/folder:<root>/file.zip"), cp.toString(ClassPath.PathConversionMode.WARN));
        assertTrue(warnings.toString(), warnings.toString().contains("subdir"));

        cp = ClassPathSupport.createClassPath(
                new URL(root + "folder/"),
                new URL("jar:" + root + "file.zip!/"));
        assertEquals(cp.toString(), ClassPathSupport.createClassPath(cp.toString()).toString());
        // XXX could also test IAE (tricky - need to have a URLMapper in Lookup, etc.)
    }

    public void testInArchivePaths() throws Exception {
        final String root = Utilities.toURI(getWorkDir()).toString();
        //Test regular folder
        ClassPath cp = ClassPathSupport.createClassPath(new URL(root + "folder/"));
        assertEquals(massagePath("<root>/folder"), cp.toString(ClassPath.PathConversionMode.FAIL, ClassPath.PathEmbeddingMode.INCLUDE));
        assertEquals(massagePath("<root>/folder"), cp.toString(ClassPath.PathConversionMode.FAIL, ClassPath.PathEmbeddingMode.EXCLUDE));
        assertEquals(massagePath("<root>/folder"), cp.toString(ClassPath.PathConversionMode.FAIL, ClassPath.PathEmbeddingMode.FAIL));
        //Test archive with no in archive path
        cp = ClassPathSupport.createClassPath(new URL("jar:" + root + "file.zip!/"));
        assertEquals(massagePath("<root>/file.zip"), cp.toString(ClassPath.PathConversionMode.FAIL, ClassPath.PathEmbeddingMode.INCLUDE));
        assertEquals(massagePath("<root>/file.zip"), cp.toString(ClassPath.PathConversionMode.FAIL, ClassPath.PathEmbeddingMode.EXCLUDE));
        assertEquals(massagePath("<root>/file.zip"), cp.toString(ClassPath.PathConversionMode.FAIL, ClassPath.PathEmbeddingMode.FAIL));
        //Test archive with in archive path
        cp = ClassPathSupport.createClassPath(new URL("jar:" + root + "file.zip!/java.base/"));
        assertEquals(massagePath("<root>/file.zip!/java.base/"), cp.toString(ClassPath.PathConversionMode.FAIL, ClassPath.PathEmbeddingMode.INCLUDE));
        assertEquals(massagePath("<root>/file.zip"), cp.toString(ClassPath.PathConversionMode.FAIL, ClassPath.PathEmbeddingMode.EXCLUDE));
        try {
            cp.toString(ClassPath.PathConversionMode.FAIL, ClassPath.PathEmbeddingMode.FAIL);
            assertTrue("Should be unreachable",false);
        } catch (IllegalArgumentException iae) {
            //IAE should be thrown
        }
    }

    public void testEmptyClassPath() throws Exception {
        final ClassPath cp = ClassPath.EMPTY;
        assertNotNull(cp);
        assertTrue(cp.entries().isEmpty());
    }

    private String massagePath(String path) throws Exception {
        return path.replace('/', File.separatorChar).replace(':', File.pathSeparatorChar).replace("<root>", getWorkDir().getAbsolutePath());
    }

    public void testInvalidURLs() throws Exception {
        try {
            ClassPathSupport.createClassPath(new URL("file:/some/jar/without/correct/protocol.jar"));
            fail();
        } catch (IllegalArgumentException x) {/* right */}
        try {
            ClassPathSupport.createClassPath(new URL("file:/some/dir/without/final/slash"));
            fail();
        } catch (IllegalArgumentException x) {/* right */}
        try {
            File f = new File(getWorkDir(),"test.jar");
            f.createNewFile();
            ClassPathSupport.createClassPath(new URL(BaseUtilities.toURI(f).toString() + '/'));
            fail();
        } catch (IllegalArgumentException x) {/* right */}
    }
    
    public void testEntryGetRoot() throws Exception {
        final File wd = getWorkDir();
        final FileObject root = FileUtil.createFolder(new File(wd,"root"));
        final ClassPath cp = ClassPathSupport.createClassPath(root);
        final ClassPath.Entry entry = cp.entries().iterator().next();
        entry.isDataResult.set(Boolean.TRUE);
        final FileObject fo = entry.getRoot();
        assertNotNull(fo);
        assertEquals(root, fo);
    }

    public void testEquality() throws Exception {
        assertTrue(ClassPath.EMPTY.equals(ClassPath.EMPTY));
        assertFalse(ClassPath.EMPTY.equals(ClassPathSupport.createClassPath(new URL[0])));
        assertFalse(ClassPathSupport.createClassPath(new URL[0]).equals(ClassPathSupport.createClassPath(new URL[0]))); // TBD; could revisit
        class Custom implements ClassPathImplementation {
            @Override public List<? extends PathResourceImplementation> getResources() {
                return Collections.emptyList();
            }
            @Override public void addPropertyChangeListener(PropertyChangeListener listener) {}
            @Override public void removePropertyChangeListener(PropertyChangeListener listener) {}
            @Override public boolean equals(Object obj) {
                return obj instanceof Custom;
            }
            @Override public int hashCode() {
                return 0;
            }
        }
        ClassPathImplementation cpi = new Custom();
        assertTrue(ClassPathFactory.createClassPath(cpi).equals(ClassPathFactory.createClassPath(cpi)));
        assertTrue(ClassPathFactory.createClassPath(cpi).equals(ClassPathFactory.createClassPath(new Custom())));
        assertFalse(ClassPathFactory.createClassPath(cpi).equals(ClassPathSupport.createClassPath(new URL[0])));
    }

    public void testDataRoot () throws Exception {
        final File wd = getWorkDir();
        final ClassPath cp = ClassPathSupport.createClassPath(
                new URL(wd.toURI().toURL().toExternalForm()+"test1/"),    //NOI18N
                new URL(wd.toURI().toURL().toExternalForm()+"test2/")    //NOI18N
        );
        final FileObject test1 = FileUtil.createData(FileUtil.toFileObject(wd), "test1");   //NOI18N
        final FileObject test2 = FileUtil.createFolder(FileUtil.toFileObject(wd), "test2");   //NOI18N
        assertEquals(Collections.singletonList(test2),Arrays.asList(cp.getRoots()));
    }

    public void testGC() throws Exception {
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        final FileObject root1 = FileUtil.createFolder(wd, "src1"); //NOI18N
        assertNotNull(root1);
        FileObject root2 = FileUtil.createFolder(wd, "src2"); //NOI18N
        assertNotNull(root2);
        final MockPropertyChangeListener l = new MockPropertyChangeListener(ClassPath.PROP_ROOTS);
        ClassPath cp = ClassPathSupport.createClassPath(root1, root2);
        cp.addPropertyChangeListener(l);
        assertNotNull(cp);
        FileObject[] roots = cp.getRoots();
        assertEquals(2, roots.length);
        assertEquals(root1, roots[0]);
        assertEquals(root2, roots[1]);
        root2.delete();
        l.assertEventCount(1);
        roots = cp.getRoots();
        assertEquals(1, roots.length);
        assertEquals(root1, roots[0]);
        root2 = FileUtil.createFolder(wd, "src2"); //NOI18N
        l.assertEventCount(1);
        roots = cp.getRoots();
        assertEquals(2, roots.length);
        assertEquals(root1, roots[0]);
        assertEquals(root2, roots[1]);
        final Reference<ClassPath> cpRef = new WeakReference<>(cp);
        cp = null;
        assertGC("ClassPath freed", cpRef);
    }

}
