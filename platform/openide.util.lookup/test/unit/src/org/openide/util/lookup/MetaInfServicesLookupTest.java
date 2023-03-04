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

package org.openide.util.lookup;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bar.Comparator2;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.test.MockLookup;

/** Test finding services from manifest.
 * @author Jesse Glick
 */
public class MetaInfServicesLookupTest extends NbTestCase {
    private Logger LOG;
    
    public MetaInfServicesLookupTest(String name) {
        super(name);
        LOG = Logger.getLogger("Test." + name);
    }
    
    protected String prefix() {
        return "META-INF/services/";
    }
    
    protected Lookup createLookup(ClassLoader c) {
        return Lookups.metaInfServices(c);
    }
    
    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    private Lookup getTestedLookup(ClassLoader c) {
        MockServices.setServices();
        return createLookup(c);
    }

    private URL findJar(String n) throws IOException {
        return findJar(n, "^(org\\.(foo|bar)\\..*)$");
    }

    private URL findJar(String n, String classPattern) throws IOException {
        LOG.log(Level.INFO, "Looking for {0}", n);
        File jarDir = new File(getWorkDir(), "jars");
        jarDir.mkdirs();
        File jar = new File(jarDir, n);
        if (jar.exists()) {
            return jar.toURI().toURL();
        }
        
        LOG.info("generating " + jar);
        
        URL data = MetaInfServicesLookupTest.class.getResource(n.replace(".jar", ".txt"));
        assertNotNull("Data found", data);
        StringBuffer sb = new StringBuffer();
        InputStreamReader r = new InputStreamReader(data.openStream());
        for(;;) {
            int ch = r.read();
            if (ch == -1) {
                break;
            }
            sb.append((char)ch);
        }
        
        JarOutputStream os = new JarOutputStream(new FileOutputStream(jar));
        
        Pattern p = Pattern.compile(":([^:]+):([^:]*)", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher(sb);
        Pattern foobar = Pattern.compile(classPattern, Pattern.MULTILINE);
        Set<String> names = new TreeSet<String>();
        while (m.find()) {
            assert m.groupCount() == 2;
            String entryName = prefix() + m.group(1);
            LOG.info("putting there entry: " + entryName);
            os.putNextEntry(new JarEntry(entryName));
            os.write(m.group(2).getBytes());
            os.closeEntry();
            
            Matcher fb = foobar.matcher(m.group(2));
            while (fb.find()) {
                String clazz = fb.group(1).replace('.', '/') + ".class";
                LOG.info("will copy " + clazz);
                names.add(clazz);
            }
        }
        
        for (String copy : names) {
            os.putNextEntry(new JarEntry(copy));
            LOG.info("copying " + copy);
            InputStream from = MetaInfServicesLookupTest.class.getResourceAsStream("/" + copy);
            assertNotNull(copy, from);
            for (;;) {
                int ch = from.read();
                if (ch == -1) {
                    break;
                }
                os.write(ch);
            }
            from.close();
            os.closeEntry();
        }
        os.close();
        LOG.info("done " + jar);
        return jar.toURI().toURL();
    }

    ClassLoader c1, c2, c2a, c3, c4;

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        ClassLoader app = getClass().getClassLoader().getParent();
        ClassLoader c0 = app;
        
        c1 = new URLClassLoader(new URL[] {
            findJar("services-jar-1.jar"),
        }, c0);
        c2 = new URLClassLoader(new URL[] {
            findJar("services-jar-2.jar"),
        }, c1);
        c2a = new URLClassLoader(new URL[] {
            findJar("services-jar-2.jar"),
        }, c1);
        c3 = new URLClassLoader(new URL[] { findJar("services-jar-2.jar") },
            c0
        );
        c4 = new URLClassLoader(new URL[] {
            findJar("services-jar-1.jar"),
            findJar("services-jar-2.jar"),
        }, c0);
    }

    public void testBasicUsage() throws Exception {
        Lookup l = getTestedLookup(c2);
        Class<?> xface = c1.loadClass("org.foo.Interface");
        NoChange nc = new NoChange(l, xface);
        LOG.log(Level.INFO, "tested lookup: {0}", l);
        LOG.log(Level.INFO, "search for {0}", xface);
        List<?> results = new ArrayList<Object>(l.lookupAll(xface));
        LOG.log(Level.INFO, "results: {0}", results);
        assertEquals("Two items in result: " + results, 2, results.size());
        // Note that they have to be in order:
        assertEquals("org.foo.impl.Implementation1", results.get(0).getClass().getName());
        assertEquals("org.bar.Implementation2", results.get(1).getClass().getName());
        // Make sure it does not gratuitously replace items:
        List<?> results2 = new ArrayList<Object>(l.lookupAll(xface));
        assertEquals(results, results2);
        nc.waitNoChange();
    }

    public void testLoaderSkew() throws Exception {
        Class<?> xface1 = c1.loadClass("org.foo.Interface");
        Lookup l3 = getTestedLookup(c3);
        // If we cannot load Interface, there should be no impls of course... quietly!
        assertEquals(Collections.emptyList(),
                new ArrayList<Object>(l3.lookupAll(xface1)));
        Lookup l4 = getTestedLookup(c4);
        // If we can load Interface but it is the wrong one, ignore it.
        assertEquals(Collections.emptyList(),
                new ArrayList<Object>(l4.lookupAll(xface1)));
        // Make sure l4 is really OK - it can load from its own JARs.
        Class<?> xface4 = c4.loadClass("org.foo.Interface");
        assertEquals(2, l4.lookupAll(xface4).size());
    }

    public void testStability() throws Exception {
        Lookup l = getTestedLookup(c2);
        Class<?> xface = c1.loadClass("org.foo.Interface");
        Object first = l.lookup(xface);
        assertEquals(first, l.lookupAll(xface).iterator().next());
        l = getTestedLookup(c2a);
        Object second = l.lookup(xface);
        assertEquals(first, second);
    }
    
    public void testDontCallMeUnderLock() throws Exception {
        final Lookup l = getTestedLookup(c2);
        ProxyLookup pl = new ProxyLookup(l) {
            @Override
            void beforeLookup(boolean call, Template<?> template) {
                super.beforeLookup(call, template);
                assertFalse("Don't hold MetaInfServicesLookup lock", Thread.holdsLock(l));
            }
        };
        Class<?> xface = c1.loadClass("org.foo.Interface");
        Result<?> res = pl.lookupResult(Object.class);
        res.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
            }
        });
        assertTrue("Empty now", res.allItems().isEmpty());
        
        Object first = l.lookup(xface);
        assertEquals(first, l.lookupAll(xface).iterator().next());
        Object second = pl.lookup(xface);
        assertEquals(first, second);
    }

    public void testMaskingOfResources() throws Exception {
        Lookup l1 = getTestedLookup(c1);
        Lookup l2 = getTestedLookup(c2);
        Lookup l4 = getTestedLookup(c4);

        assertNotNull("services1.jar defines a class that implements runnable", l1.lookup(Runnable.class));
        assertNull("services2.jar does not defines a class that implements runnable", l2.lookup(Runnable.class));
        assertNull("services1.jar defines Runnable, but services2.jar masks it out", l4.lookup(Runnable.class));
    }

    public void testOrdering() throws Exception {
        Lookup l = getTestedLookup(c1);
        Class<?> xface = c1.loadClass("java.util.Comparator");
        List<?> results = new ArrayList<Object>(l.lookupAll(xface));
        assertEquals(1, results.size());

        l = getTestedLookup(c2);
        xface = c2.loadClass("java.util.Comparator");
        results = new ArrayList<Object>(l.lookupAll(xface));
        assertEquals(2, results.size());
        // Test order:
        assertEquals("org.bar.Comparator2", results.get(0).getClass().getName());
        assertEquals("org.foo.impl.Comparator1", results.get(1).getClass().getName());

        // test that items without position are always at the end
        l = getTestedLookup(c2);
        xface = c2.loadClass("java.util.Iterator");
        results = new ArrayList<Object>(l.lookupAll(xface));
        assertEquals(2, results.size());
        // Test order:
        assertEquals("org.bar.Iterator2", results.get(0).getClass().getName());
        assertEquals("org.foo.impl.Iterator1", results.get(1).getClass().getName());
    }

    public void testNoCallToGetResourceForObjectIssue65124() throws Exception {
        class Loader extends ClassLoader {
            private int counter;

            @Override
            protected URL findResource(String name) {
                if (name.equals(prefix() + "java.lang.Object")) {
                    counter++;
                }

                URL retValue;

                retValue = super.findResource(name);
                return retValue;
            }

            @Override
            protected Enumeration<URL> findResources(String name) throws IOException {
                if (name.equals(prefix() + "java.lang.Object")) {
                    counter++;
                }
                return super.findResources(name);
            }
        }
        Loader loader = new Loader();
        Lookup l = getTestedLookup(loader);

        Object no = l.lookup(String.class);
        assertNull("Not found of course", no);
        assertEquals("No lookup of Object", 0, loader.counter);
    }

    public void testCanGarbageCollectClasses() throws Exception {
        class Loader extends ClassLoader {
            public Loader() {
                super(Loader.class.getClassLoader().getParent());
            }

            @Override
            protected URL findResource(String name) {
                if (name.equals(prefix() + "java.lang.Runnable")) {
                    return Loader.class.getResource("MetaInfServicesLookupTestRunnable.txt");
                }

                URL retValue;

                retValue = super.findResource(name);
                return retValue;
            }

            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                if (name.equals("org.openide.util.lookup.MetaInfServicesLookupTestRunnable")) {
                    try {
                        InputStream is = getClass().getResourceAsStream("MetaInfServicesLookupTestRunnable.class");
                        byte[] arr = new byte[is.available()];
                        int read = is.read(arr);
                        assertEquals("Fully read", arr.length, read);
                        return defineClass(name, arr, 0, arr.length);
                    } catch (IOException ex) {
                        throw new ClassNotFoundException("Cannot load", ex);
                    }
                }
                throw new ClassNotFoundException();
            }



            @Override
            protected Enumeration<URL> findResources(String name) throws IOException {
                if (name.equals(prefix() + "java.lang.Runnable")) {
                    return Collections.enumeration(Collections.singleton(findResource(name)));
                }
                return super.findResources(name);
            }
        }
        Loader loader = new Loader();
        Lookup l = getTestedLookup(loader);


        Object no = l.lookup(Runnable.class);
        assertNotNull("Found of course", no);
        assertEquals("The right name", "MetaInfServicesLookupTestRunnable", no.getClass().getSimpleName());
        if (no.getClass().getClassLoader() != loader) {
            fail("Wrong classloader: " + no.getClass().getClassLoader());
        }

        WeakReference<Object> ref = new WeakReference<Object>(no.getClass());
        loader = null;
        no = null;
        l = null;
        MockLookup.setInstances();
        Thread.currentThread().setContextClassLoader(null);
        assertGC("Class can be garbage collected", ref);
    }

    public void testSuperTypes() throws Exception {
        doTestSuperTypes(createLookup(c2));
        doTestSuperTypes(new ProxyLookup(createLookup(c2)));
    }
    private void doTestSuperTypes(Lookup l) throws Exception {
        final Class<?> xface = c1.loadClass("org.foo.Interface");
        final Lookup.Result<Object> res = l.lookupResult(Object.class);
        assertEquals("Nothing yet", 0, res.allInstances().size());
        final AtomicBoolean event = new AtomicBoolean();
        final Thread here = Thread.currentThread();
        res.addLookupListener(new LookupListener() {
            public void resultChanged(LookupEvent ev) {
                if (Thread.currentThread() == here) {
                    event.set(true);
                }
            }
        });
        assertNotNull("Interface found", l.lookup(xface));
        assertFalse(event.get());
        class W implements Runnable {
            boolean ok;
            public synchronized void run() {
                ok = true;
                notifyAll();
            }

            public synchronized void await() throws Exception {
                while (!ok) {
                    wait();
                }
            }
        }
        W w = new W();
        MetaInfServicesLookup.getRP().execute(w);
        w.await();
        assertEquals("Now two", 2, res.allInstances().size());
    }
    
    public void testWrongOrderAsInIssue100320() throws Exception {
        ClassLoader app = getClass().getClassLoader().getParent();
        ClassLoader c0 = app;
        ClassLoader ctmp = new URLClassLoader(new URL[] {
            findJar("problem100320.jar"),
        }, c0);
        Lookup lookup = Lookups.metaInfServices(ctmp, prefix());

        Collection<?> colAWT = lookup.lookupAll(IOException.class);
        assertEquals("There is enough objects to switch to InheritanceTree", 12, colAWT.size());
        
        
        List<?> col1 = new ArrayList<Object>(lookup.lookupAll(Comparator.class));
        assertEquals("Two", 2, col1.size());
        Collection<?> col2 = lookup.lookupAll(ctmp.loadClass(Comparator2.class.getName()));
        assertEquals("One", 1, col2.size());
        List<?> col3 = new ArrayList<Object>(lookup.lookupAll(Comparator.class));
        assertEquals("Two2", 2, col3.size());
        
        Iterator<?> it1 = col1.iterator();
        Iterator<?> it3 = col3.iterator();
        if (
            it1.next() != it3.next() || 
            it1.next() != it3.next() 
        ) {
            fail("Collections are different:\nFirst: " + col1 + "\nLast:  " + col3);
        }
    }

    public void testContentionWhenLoadingMetainfServices() throws Exception {
        class My extends ClassLoader implements Runnable {
            Lookup query;
            Integer value;
            boolean once;

            public void run() {
                value = query.lookup(Integer.class);
            }


            @Override
            protected URL findResource(String name) {
                waitForTask(name);
                return super.findResource(name);
            }

            @Override
            protected Enumeration<URL> findResources(String name) throws IOException {
                waitForTask(name);
                return super.findResources(name);
            }

            private synchronized void waitForTask(String name) {
                if (once) {
                    return;
                }
                once = true;
                if (name.startsWith(prefix()) && Thread.currentThread().getName().contains("block")) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger("global").log(Level.WARNING, "", ex);
                    }
                }
            }
        }

        My loader = new My();
        loader.query = createLookup(loader);
        Thread t = new Thread(loader, "block when querying");
        t.start();
        t.join(1000);

        // this blocks waiting for the waitForTask to finish
        // right now
        Float f = loader.query.lookup(Float.class);
        assertNull("Nothing found", f);

        synchronized (loader) {
            loader.notifyAll();
        }
        t.join();

        assertNull("Nothing found", loader.value);
    }
    
    public void testSubTypes() throws Exception {
        doTestSubTypes(createLookup(c2));
        doTestSubTypes(new ProxyLookup(createLookup(c2)));
    }
    private void doTestSubTypes(Lookup l) throws Exception {
        final Class<?> xface = c1.loadClass("org.foo.Interface");
        final Class<?> impl = c1.loadClass("org.foo.impl.Implementation1");
        
        Object result = l.lookup(impl);
        assertNotNull("result found", result);
        assertTrue("Right class", impl.isInstance(result));
    }
    public void testLookupObject() throws Exception {
        assertNotNull("Object.class can be looked up", createLookup(new ClassLoader() {
            @Override
            protected Enumeration<URL> findResources(String name) throws IOException {
                if (name.equals(prefix() + "java.lang.Object")) {
                    return singleton(new URL(null, "dummy:stuff", new URLStreamHandler() {
                        @Override
                        protected URLConnection openConnection(URL u) throws IOException {
                            return new URLConnection(u) {

                                @Override
                                public void connect() throws IOException {
                                }

                                @Override
                                public InputStream getInputStream() throws IOException {
                                    return new ByteArrayInputStream("java.lang.Object\n".getBytes(StandardCharsets.UTF_8));
                                }
                            };
                        }
                    }));
                } else {
                    return Collections.enumeration(Collections.<URL>emptyList());
                }
            }
        }).lookup(Object.class));
    }
    
    /**
     * Test for bug 249414 - java.lang.IllegalStateException: You cannot use
     * MetaInfServicesLookup.Item[otool.maven.queries.MavenFileOwnerQueryImpl]
     * in more than one AbstractLookup. Prev: 44 new: 44
     *
     * The exception occurs if one class provides two services (its own class
     * and its superclass) with different positions, and if the actual class
     * with higher position (lesser priority) is looked up first, then some
     * other implemetation is looked up, and then, finally, the superclass with
     * lowest position is looked up.
     *
     * Note: Some other conditions must be met, see comments in source code.
     *
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     * @throws java.lang.InterruptedException
     */
    public void testMultiRegWithDifferentPositions() throws IOException,
            ClassNotFoundException, InterruptedException {

        List<Object> preventGc = new ArrayList<Object>();

        ClassLoader loader = new URLClassLoader(new URL[]{
            findJar("problem249414.jar", "^(org\\.(baz)\\..*)$"),},
                Object.class.getClassLoader());

        Class<?> cPadding = loader.loadClass("org.baz.Padding");
        Class<?> cFilling = loader.loadClass("org.baz.Filling");

        Class<?> cIntr = loader.loadClass("org.baz.MyService");
        Class<?> cImpl = loader.loadClass("org.baz.impl.MyServiceImpl");
        Class<?> cOthr = loader.loadClass("org.baz.impl.OtherServiceImpl");

        Lookup lkp = getTestedLookup(loader);

        Collections.addAll(preventGc, cIntr, cImpl, cOthr, cPadding);

        // Lookup all instances of the Padding service (so that the lookup is
        // big enough to use InheritanceTree as storage).
        Collection<?> paddings = lkp.lookupResult(cPadding).allItems();
        int count = 0;
        for (Object p: paddings) {
            assertNotNull(p);
            preventGc.add(p);
            count++;
        }

        assertEquals(13, count);

        // Look up the implementing class first. Its position is 200.
        Object impl = lkp.lookupResult(cImpl).allItems().iterator().next();
        assertNotNull(impl);

        // Look up the other implementation. Its position is 100.
        Object othr = lkp.lookupResult(cOthr).allItems().iterator().next();
        assertNotNull(othr);

        // Finally, look up the interface. Its position is 90.
        Object intr = lkp.lookupResult(cIntr).allItems().iterator().next();
        assertNotNull(intr);

        Collections.addAll(preventGc, impl, othr, intr);
        assertNotNull(preventGc);

        // Check that instances implementing multiple unrelated services are
        // in the lookup only once.
        assertEquals(1, lkp.lookupAll(cFilling).size());
    }

    public static final class Err extends Object {
        public Err() {
            throw new UnsatisfiedLinkError();
        }
    }
    
    public void testHashCodeIsStable() {
        AbstractLookup.Pair p1 = MetaInfServicesLookup.createPair(Err.class);
        AbstractLookup.Pair p2 = MetaInfServicesLookup.createPair(Err.class);
        
        long hash = p1.hashCode();
        assertEquals("Both hash codes are the same", hash, p2.hashCode());
        assertNull("No instance created", p2.getInstance());
        assertEquals("Both hash codes remain the same", hash, p2.hashCode());
    }

    public void testEqualsIsStable() {
        AbstractLookup.Pair p1 = MetaInfServicesLookup.createPair(Err.class);
        AbstractLookup.Pair p2 = MetaInfServicesLookup.createPair(Err.class);
        
        assertTrue("Same class items are equal", p1.equals(p2));
        assertNull("No instance created", p2.getInstance());
        assertTrue("Same class items are still equal", p1.equals(p2));
    }

    public void testInitializerRobustness() throws Exception { // #174055
        check(Broken1.class.getName());
        check(Broken2.class.getName());
    }
    private void check(final String n) {
        assertNull(Lookups.metaInfServices(new ClassLoader() {
            protected @Override Enumeration<URL> findResources(String name) throws IOException {
                if (name.equals("META-INF/services/java.lang.Object")) {
                    return singleton(new URL(null, "dummy:stuff", new URLStreamHandler() {
                        protected URLConnection openConnection(URL u) throws IOException {
                            return new URLConnection(u) {
                                public void connect() throws IOException {}
                                public @Override InputStream getInputStream() throws IOException {
                                    return new ByteArrayInputStream(n.getBytes(StandardCharsets.UTF_8));
                                }
                            };
                        }
                    }));
                } else {
                    return Collections.enumeration(Collections.<URL>emptyList());
                }
            }

        }).lookup(Object.class));
    }
    public static class Broken1 {
        public Broken1() {
            throw new NullPointerException("broken1");
        }
    }
    public static class Broken2 {
        static {
            if (true) { // otherwise javac complains
                throw new NullPointerException("broken2");
            }
        }
    }

    static <T> Enumeration<T> singleton(T t) {
        return Collections.enumeration(Collections.singleton(t));
    }
    
    public static class NoChange implements LookupListener {
        private final Lookup.Result<?> res;
        private IllegalStateException stack;
        public NoChange(Lookup lkp, Class<?> type) {
            res = lkp.lookupResult(type);
            res.addLookupListener(this);
        }
        
        
        @Override
        public synchronized void resultChanged(LookupEvent ev) {
            stack = new IllegalStateException("Don't generate an event, please!");
            notifyAll();
        }
        
        public synchronized void waitNoChange() throws IllegalStateException, InterruptedException {
            if (stack == null) {
                wait(1000);
            }
            if (stack != null) {
                throw stack;
            }
        }
    }
}
