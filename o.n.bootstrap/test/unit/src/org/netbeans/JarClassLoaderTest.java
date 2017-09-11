/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
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
import java.util.Map;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.TestFileUtils;

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
            if (System.getSecurityManager() instanceof BlockingSecurityManager) {
            // ok
            } else {
                System.setSecurityManager(new BlockingSecurityManager());
            }
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
}
