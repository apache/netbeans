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
package org.netbeans;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.Permission;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.TestFileUtils;

import static java.nio.charset.StandardCharsets.UTF_8;

/** Tests that cover some basic aspects of a Proxy/JarClassLoader.
 *
 * @author Petr Nejedly
 */
public class JarClassLoaderTest extends NbTestCase {

    private static Logger LOGGER = Logger.getLogger(ProxyClassLoader.class.getName());


    public JarClassLoaderTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.OFF);
        clearWorkDir();
    }


    public void testCanLoadFromDefaultPackage() throws Exception {
        File jar = new File(getWorkDir(), "default-package-resource.jar");
        TestFileUtils.writeZipFile(jar, "resource.txt:content", "package/resource.txt:content");
        JarClassLoader jcl = new JarClassLoader(Collections.singletonList(jar), new ProxyClassLoader[0]);
        
        assertStreamContent(jcl.getResourceAsStream("package/resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("/package/resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("/resource.txt"), "content");

        assertURLsContent(jcl.getResources("package/resource.txt"), "content");
        assertURLsContent(jcl.getResources("/package/resource.txt"), "content");
        assertURLsContent(jcl.getResources("resource.txt"), "content");
        assertURLsContent(jcl.getResources("/resource.txt"), "content");
    }


    public void testKnowsWhichJarsHaveDefaultPackage() throws Exception {
        final File nothing = new File(getWorkDir(), "nothing.jar");
        TestFileUtils.writeZipFile(nothing, "package/resource.txt:content");
        final File a1 = new File(getWorkDir(), "a1.jar");
        TestFileUtils.writeZipFile(a1, "A.txt:A", "package/resourceA.txt:content");
        final File a2 = new File(getWorkDir(), "a2.jar");
        TestFileUtils.writeZipFile(a2, "A.txt:A2", "package/resourceA2.txt:content");
        final File b = new File(getWorkDir(), "b.jar");
        TestFileUtils.writeZipFile(b, "B.txt:B", "package/resourceB.txt:content");
        
        
        class CntJCL extends JarClassLoader {
            int queried;

            public CntJCL(List<File> files, ClassLoader[] parents) {
                super(files, parents);
            }

            @Override
            public URL findResource(String name) {
                queried++;
                return super.findResource(name);
            }

            @Override
            public Enumeration<URL> findResources(String name) {
                queried++;
                return super.findResources(name);
            }
            
        }
        final CntJCL[] arr = new CntJCL[] {
            new CntJCL(Collections.singletonList(nothing), new ClassLoader[0]),
            new CntJCL(Collections.singletonList(a1), new ClassLoader[0]),
            new CntJCL(Collections.singletonList(a2), new ClassLoader[0]),
            new CntJCL(Collections.singletonList(b), new ClassLoader[0]),
        };
        
        ProxyClassLoader pcl = new ProxyClassLoader(arr, true);
        
        assertURLsContent(pcl.getResources("A.txt"), "A", "A2");
        
        assertEquals("No query to nothing.jar", 0, arr[0].queried);
        assertEquals("One query to a1.jar", 1, arr[1].queried);
        assertEquals("One query to a2.jar", 1, arr[2].queried);
        assertEquals("No query to b.jar", 0, arr[3].queried);
        
        assertURLsContent(pcl.getResources("B.txt"), "B");
        
        assertEquals("No query to nothing.jar", 0, arr[0].queried);
        assertEquals("Still One query to a1.jar", 1, arr[1].queried);
        assertEquals("Still One query to a2.jar", 1, arr[2].queried);
        assertEquals("One query to b.jar now", 1, arr[3].queried);
        
    }
    public void testCanLoadFromDefaultPackageCachedOldFormat() throws Exception {
        doCanLoadCached("META-INF,/MANIFEST.MF,package");
    }
    public void testCanLoadFromDefaultPackageCached() throws Exception {
        doCanLoadCached("META-INF,/MANIFEST.MF,package,default/resource.txt");
    }
    
    private void doCanLoadCached(String covPkg) throws Exception {
        final File jar = new File(getWorkDir(), "default-package-resource-cached.jar");
        TestFileUtils.writeZipFile(jar, "resource.txt:content", "package/resource.txt:content", 
            "META-INF/MANIFEST.MF:OpenIDE-Module: x.y.z\nCovered-Packages: " + covPkg + ",\n"
        );

        MockModuleInstaller inst = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mm = new ModuleManager(inst, ev);
        
        Module fake = new Module(mm, null, null, null) {
	    public List<File> getAllJars() {throw new UnsupportedOperationException();}
            public void setReloadable(boolean r) { throw new UnsupportedOperationException();}
            public void reload() throws IOException { throw new UnsupportedOperationException();}
            protected void classLoaderUp(Set<Module> parents) throws IOException {throw new UnsupportedOperationException();}
            protected void classLoaderDown() { throw new UnsupportedOperationException();}
            protected void cleanup() { throw new UnsupportedOperationException();}
            protected void destroy() { throw new UnsupportedOperationException("Not supported yet.");}
            public boolean isFixed() { throw new UnsupportedOperationException("Not supported yet.");}
            public Object getLocalizedAttribute(String attr) { throw new UnsupportedOperationException("Not supported yet.");}

            public Manifest getManifest() {
                try {
                    return new JarFile(jar, false).getManifest();
                } catch (IOException ex) {
                        throw new AssertionFailedError(ex.getMessage());
                }
            }

        };

        JarClassLoader jcl = new JarClassLoader(Collections.singletonList(jar), new ProxyClassLoader[0], false, fake);
        
        assertStreamContent(jcl.getResourceAsStream("package/resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("/package/resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("/resource.txt"), "content");

        assertURLsContent(jcl.getResources("package/resource.txt"), "content");
        assertURLsContent(jcl.getResources("/package/resource.txt"), "content");
        assertURLsContent(jcl.getResources("resource.txt"), "content");
        assertURLsContent(jcl.getResources("/resource.txt"), "content");
    }

    public void testCanLoadFromDefaultPackageDirs() throws Exception {
        File dir = getWorkDir();
        TestFileUtils.writeFile(new File(dir, "resource.txt"), "content");
        TestFileUtils.writeFile(new File(dir, "package/resource.txt"), "content");
        TestFileUtils.writeFile(new File(dir, "META-INF/services/resource.txt"), "content");
        JarClassLoader jcl = new JarClassLoader(Collections.singletonList(dir), new ProxyClassLoader[0]);
        
        assertStreamContent(jcl.getResourceAsStream("package/resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("/package/resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("/resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("META-INF/services/resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("/META-INF/services/resource.txt"), "content");

        assertURLsContent(jcl.getResources("package/resource.txt"), "content");
        assertURLsContent(jcl.getResources("/package/resource.txt"), "content");
        assertURLsContent(jcl.getResources("resource.txt"), "content");
        assertURLsContent(jcl.getResources("/resource.txt"), "content");
    }

    public void testJarURLConnection() throws Exception {
        File jar = new File(getWorkDir(), "default-package-resource.jar");
        TestFileUtils.writeZipFile(jar, "META-INF/MANIFEST.MF:Manifest-Version: 1.0\nfoo: bar\n\n", "package/re source++.txt:content");
        JarClassLoader jcl = new JarClassLoader(Collections.singletonList(jar), new ProxyClassLoader[0]);
        URL url = jcl.getResource("package/re source++.txt");
        assertTrue(url.toString(), url.toString().endsWith("default-package-resource.jar!/package/re%20source++.txt"));
        URLConnection conn = url.openConnection();
        assertEquals(7, conn.getContentLength());
        assertTrue(conn instanceof JarURLConnection);
        JarURLConnection jconn = (JarURLConnection) conn;
        assertEquals("package/re source++.txt", jconn.getEntryName());
        assertEquals(Utilities.toURI(jar).toURL(), jconn.getJarFileURL());
        assertEquals("bar", jconn.getMainAttributes().getValue("foo"));
        assertEquals(jar.getAbsolutePath(), jconn.getJarFile().getName());
    }

    public void testAddURLMethod() throws Exception {
        File jar = new File(getWorkDir(), "default-package-resource.jar");
        TestFileUtils.writeZipFile(jar, "META-INF/MANIFEST.MF:Manifest-Version: 1.0\nfoo: bar\n\n", "package/re source++.txt:content");
        JarClassLoader jcl = new JarClassLoader(Collections.<File>emptyList(), new ProxyClassLoader[0]);
        jcl.addURL(Utilities.toURI(jar).toURL());
        URL url = jcl.getResource("package/re source++.txt");
        assertTrue(url.toString(), url.toString().endsWith("default-package-resource.jar!/package/re%20source++.txt"));
        URLConnection conn = url.openConnection();
        assertEquals(7, conn.getContentLength());
        assertTrue(conn instanceof JarURLConnection);
        JarURLConnection jconn = (JarURLConnection) conn;
        assertEquals("package/re source++.txt", jconn.getEntryName());
        assertEquals(Utilities.toURI(jar).toURL(), jconn.getJarFileURL());
        assertEquals("bar", jconn.getMainAttributes().getValue("foo"));
        assertEquals(jar.getAbsolutePath(), jconn.getJarFile().getName());
    }

    public void testResourceDefinition() throws Exception { // #196595
        File jar = new File(getWorkDir(), "some.jar");
        TestFileUtils.writeZipFile(jar, "package/resource.txt:content");
        ClassLoader cl = new JarClassLoader(Collections.singletonList(jar), new ProxyClassLoader[0]);
        URL r = cl.getResource("package/resource.txt");
        assertNotNull(r);
        assertStreamContent(r.openStream(), "content");
        assertEquals(cl, r.getContent(new Class<?>[] {ClassLoader.class}));
    }

    public void testMetaInfServicesUsesGetContentCL() throws Exception {
        final ClassLoader parent = MetaInfServicesToken.class.getClassLoader().getParent();
        class JDKOnly extends ClassLoader {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                return parent.loadClass(name);
            }

            @Override
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                return parent.loadClass(name);
            }

            @Override
            public URL getResource(String name) {
                return parent.getResource(name);
            }

            @Override
            public InputStream getResourceAsStream(String name) {
                return parent.getResourceAsStream(name);
            }

            @Override
            public Enumeration<URL> getResources(String name) throws IOException {
                return parent.getResources(name);
            }
        }
        ClassLoader jdkonly = new JDKOnly();
        
        File jar = new File(getWorkDir(), "some.jar");
        TestFileUtils.writeZipFile(
            jar, 
            "META-INF/services/java.io.Serializable:org.netbeans.MetaInfServicesToken"
        );
        URL url = MetaInfServicesToken.class.getProtectionDomain().getCodeSource().getLocation();

        URLClassLoader one = new URLClassLoader(new URL[] { url }, jdkonly);
        URLClassLoader two = new URLClassLoader(new URL[] { url }, jdkonly);
        
        final String name = MetaInfServicesToken.class.getName();
        Class<?> cOne = one.loadClass(name);
        Class<?> cTwo = two.loadClass(name);
        
        if (cOne == cTwo) {
            fail("Classes should be different, not loaded by: " + cOne.getClassLoader());
        }
        
        ClassLoader cl = new JarClassLoader(Collections.singletonList(jar), new ClassLoader[] { two });
        ProxyClassLoader all = new ProxyClassLoader(new ClassLoader[] { one, cl }, false);
        Object res = Lookups.metaInfServices(all).lookup(Serializable.class);
        assertNotNull("One serializable found", res);
        assertEquals("It is from the second classloader", cTwo, res.getClass());
    }

    private void assertURLsContent(Enumeration<URL> urls, String ... contents) throws IOException {
        for (String content : contents) {
            assertTrue("Enough entries", urls.hasMoreElements());
            assertStreamContent(urls.nextElement().openStream(), content);
        }
        assertFalse("Too many entries", urls.hasMoreElements());
    }
    
    private void assertStreamContent(InputStream str, String content) throws IOException {
        assertNotNull("Resource found", str);
        byte[] data = new byte[content.length()];
        DataInputStream dis = new DataInputStream(str);
        try {
            dis.readFully(data);
        } finally {
            dis.close();
        }
        assertEquals(new String(data), content);
    }

    public void interruptImpl(int toInterrupt) throws Exception {
        File jar = new File(getWorkDir(), "interrupted-reading.jar");
        TestFileUtils.writeZipFile(jar, "resource.txt:content");
        final JarClassLoader jcl = new JarClassLoader(Collections.singletonList(jar), new ProxyClassLoader[0]);
        jcl.releaseJars();
        // now we have a JarClassLoader with no jars open, let't catch
        // him opening a jar

        final Semaphore controlSemaphore = new Semaphore(0);
        final Object[] results = new Object[2];

        Semaphore readSemaphore = new Semaphore(0);
        BlockingSecurityManager.initialize(jar.toString(), readSemaphore);

        class Tester extends Thread {
            int slot;

            Tester(int slot) throws Exception {
                this.slot = slot;
                start();
                controlSemaphore.acquire();
            }

            @Override
            public void run() {
                controlSemaphore.release(); // we're about to start blocking
                try {
                    URL url = jcl.getResource("resource.txt");
                    assertNotNull(url);
                    results[slot] = url;
                } catch (Throwable t) {
                    results[slot] = t;
                }
            }
        };

        Thread[] threads = new Thread[] { new Tester(0), new Tester(1) };
        // threads[0] has reached the blocking point while opening the jar
        // threads[1] is blocking in callGet()

        Thread.sleep(100); // for sure

        threads[toInterrupt].interrupt(); // interrupt the selected thread
        readSemaphore.release(1000); // let the read proceed

        // wait for them to finish the work
        for (Thread t : threads) t.join();

        assertTrue("Should be URL: " + results[0], results[0] instanceof URL);
        assertTrue("Should be URL: " + results[1], results[1] instanceof URL);
    }

    public void testCanInterruptOpeningThread() throws Exception {
        interruptImpl(0); // try interrupting the opening thread
    }

    public void testCanInterruptWaitingThread() throws Exception {
        interruptImpl(1); // try interrupting the waiting thread
    }

    private static class BlockingSecurityManager extends SecurityManager {
        private static String path;
        private static Semaphore sync;
    
        public static void initialize(String path, Semaphore sync) {
            BlockingSecurityManager.path = path;
            BlockingSecurityManager.sync = sync;
            System.setSecurityManager(new BlockingSecurityManager());
        }
    
        public @Override void checkRead(String file) {
            if (file.equals(path)) {
                sync.acquireUninterruptibly();
            }
        }

        public @Override void checkRead(String file, Object context) {
            checkRead(file);
        }

    
        public @Override void checkPermission(Permission perm) {}

        public @Override void checkPermission(Permission perm, Object ctx) {}
    }

    public void testMultiReleaseJar() throws Exception {
        clearWorkDir();

        // Prepare multi-release jar file
        File classes = new File(getWorkDir(), "classes");
        classes.mkdirs();
        ToolProvider.getSystemJavaCompiler()
                    .getTask(null, null, d -> { throw new IllegalStateException(d.toString()); }, Arrays.asList("-d", classes.getAbsolutePath()), null,
                             Arrays.asList(new SourceFileObject("test/Impl.java", "package test; public class Impl { public static String get() { return \"base\"; } }"),
                                           new SourceFileObject("api/API.java", "package api; public class API { public static String run() { return test.Impl.get(); } }")))
                    .call();
        File classes9 = new File(new File(new File(classes, "META-INF"), "versions"), "9");
        classes9.mkdirs();
        ToolProvider.getSystemJavaCompiler()
                    .getTask(null, null, d -> { throw new IllegalStateException(d.toString()); }, Arrays.asList("-d", classes9.getAbsolutePath(), "-classpath", classes.getAbsolutePath()), null,
                             Arrays.asList(new SourceFileObject("test/Impl.java", "package test; public class Impl { public static String get() { return \"9\"; } }")))
                    .call();
        Map<String, byte[]> jarContent = new LinkedHashMap<>();
        jarContent.put("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\nMulti-Release: true\n\n".getBytes());
        Path classesPath = classes.toPath();
        Files.walk(classesPath)
             .filter(p -> Files.isRegularFile(p))
             .forEach(p -> {
                  try {
                      jarContent.put(classesPath.relativize(p).toString(), TestFileUtils.readFileBin(p.toFile()));
                  } catch (IOException ex) {
                      throw new IllegalStateException(ex);
                  }
             });
        jarContent.put("test/dummy.txt", "base".getBytes(UTF_8));
        jarContent.put("META-INF/versions/9/test/dummy.txt", "9".getBytes(UTF_8));
        File jar = new File(getWorkDir(), "multi-release.jar");
        try (OutputStream out = new FileOutputStream(jar)) {
            TestFileUtils.writeZipFile(out, jarContent);
        }

        // Check multi release class loading
        JarClassLoader jcl = new JarClassLoader(Arrays.asList(jar), new ProxyClassLoader[0]);
        Class<?> api = jcl.loadClass("api.API");
        Method run = api.getDeclaredMethod("run");
        String output = (String) run.invoke(null);
        String expected;
        try {
            Class.forName("java.lang.Runtime$Version");
            expected = "9";
        } catch (ClassNotFoundException ex) {
            expected = "base";
        }
        assertEquals(expected, output);

        // Check multi release resource loading
        try(InputStream is = jcl.getResourceAsStream("test/dummy.txt")) {
            assertEquals(expected, loadUTF8(is));
        }
    }

    private static String loadUTF8(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int read;
        while ((read = is.read(buffer)) > 0) {
            baos.write(buffer, 0, read);
        }
        return baos.toString("UTF-8");
    }

    private static final class SourceFileObject extends SimpleJavaFileObject {

        private final String content;

        public SourceFileObject(String path, String content) throws URISyntaxException {
            super(new URI("mem://" + path), Kind.SOURCE);
            this.content = content;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return content;
        }

    }
}
