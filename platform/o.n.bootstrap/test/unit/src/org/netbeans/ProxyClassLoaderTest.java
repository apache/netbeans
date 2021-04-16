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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import org.fakepkg.FakeIfceHidden;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

public class ProxyClassLoaderTest extends SetupHid {

    public ProxyClassLoaderTest(String name) {
        super(name);
    }

    public void testAmbiguousDelegation() throws Exception {
        class CL extends ProxyClassLoader {
            final Class<?>[] owned;
            final String name;
            CL(ClassLoader[] parents, String _name, Class<?>... _owned) {
                super(parents, false);
                addCoveredPackages(Collections.singleton("org.netbeans"));
                name = _name;
                owned = _owned;
            }
            protected @Override Class<?> doLoadClass(String pkg, String name) {
                for (Class<?> c : owned) {
                    if (name.equals(c.getName())) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        InputStream is = CL.class.getClassLoader().getResourceAsStream(name.replace('.', '/') + ".class");
                        byte[] buf = new byte[4096];
                        int read;
                        try {
                            while ((read = is.read(buf)) != -1) {
                                baos.write(buf, 0, read);
                            }
                        } catch (IOException x) {
                            assert false : x;
                        }
                        return defineClass(name, baos.toByteArray(), 0, baos.size());
                    }
                }
                return null;
            }
            protected @Override boolean shouldDelegateResource(String pkg, ClassLoader parent) {
                return parent != null || !pkg.equals("org/netbeans/");
            }
            public @Override String toString() {
                return name;
            }
        }
        ClassLoader l1 = new CL(new ClassLoader[0], "l1", A.class);
        ClassLoader l2 = new CL(new ClassLoader[0], "l2", A.class);
        ClassLoader l3 = new CL(new ClassLoader[] {l1}, "l3", B.class);
        ClassLoader l4 = new CL(new ClassLoader[] {l1, l2}, "l4", B.class);
        assertEquals(l1, l1.loadClass(A.class.getName()).getClassLoader());
        assertEquals(l2, l2.loadClass(A.class.getName()).getClassLoader());
        assertEquals(l1, l3.loadClass(A.class.getName()).getClassLoader());
        assertEquals(l3, l3.loadClass(B.class.getName()).getClassLoader());
        assertEquals(l1, l3.loadClass(B.class.getName()).getMethod("a").invoke(null).getClass().getClassLoader());
        try {
            Class<?> c = l4.loadClass(A.class.getName());
            fail("arbitrarily loaded A from " + c.getClassLoader());
        } catch (ClassNotFoundException x) {/* OK */}
        try {
            ClassLoader delegate = l4.loadClass(B.class.getName()).getMethod("a").invoke(null).getClass().getClassLoader();
            fail("arbitrarily returned A instance from " + delegate);
        } catch (LinkageError x) {/* OK */}
        ClassLoader l5 = new CL(new ClassLoader[] {l1, l3}, "l5", C.class);
        assertEquals(l1, l5.loadClass(A.class.getName()).getClassLoader());
        assertEquals(l3, l5.loadClass(B.class.getName()).getClassLoader());
        assertEquals(l5, l5.loadClass(C.class.getName()).getClassLoader());
        assertEquals(l1, l5.loadClass(C.class.getName()).getMethod("a").invoke(null).getClass().getClassLoader());
    }

    public static class A {}
    public static class B {
        public static A a() {
            return new A();
        }
        private B() {}
    }
    public static class C {
        public static A a() {
            return new A();
        }
        private C() {}
    }

    public void testResourceDelegation() throws Exception { // #32576
        class CL extends ProxyClassLoader {
            final URL base1, base2;
            final String[] owned;
            CL(ClassLoader[] parents, URL _base1, URL _base2, String... _owned) {
                super(parents, false);
                base1 = _base1;
                base2 = _base2;
                owned = _owned;
                addCoveredPackages(Collections.singleton("p"));
            }
            @Override public URL findResource(String name) {
                if (Arrays.asList(owned).contains(name)) {
                    try {
                        return new URL(base1, name);
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                return null;
            }
            @Override public synchronized Enumeration<URL> findResources(String name) throws IOException {
                if (Arrays.asList(owned).contains(name)) {
                    return Enumerations.array(new URL(base1, name), new URL(base2, name));
                }
                return super.findResources(name);
            }
        }
        URL b = new URL("http://nowhere.net/");
        ProxyClassLoader cl1 = new CL(new ClassLoader[0], new URL(b, "1a/"), new URL(b, "1b/"), "p/1");
        ProxyClassLoader cl2 = new CL(new ClassLoader[] {cl1}, new URL(b, "2a/"), new URL(b, "2b/"), "p/2");
        ProxyClassLoader cl3 = new CL(new ClassLoader[] {cl1}, new URL(b, "3a/"), new URL(b, "3b/"), "p/1", "p/3");
        ProxyClassLoader cl4 = new CL(new ClassLoader[] {cl1, cl2, cl3}, new URL(b, "4a/"), new URL(b, "4b/"));
        assertEquals(new URL(b, "1a/p/1"), cl1.getResource("p/1"));
        assertEquals(null, cl1.getResource("p/1x"));
        assertEquals(Arrays.asList(new URL(b, "1a/p/1"), new URL(b, "1b/p/1")), Collections.list(cl1.getResources("p/1")));
        assertEquals(new URL(b, "1a/p/1"), cl2.getResource("p/1"));
        assertEquals(null, cl2.findResource("p/1"));
        assertEquals(new URL(b, "2a/p/2"), cl2.getResource("p/2"));
        assertEquals(new URL(b, "2a/p/2"), cl2.findResource("p/2"));
        assertEquals(Arrays.asList(new URL(b, "2a/p/2"), new URL(b, "2b/p/2")), Collections.list(cl2.getResources("p/2")));
        assertEquals(null, cl2.findResource("p/1"));
        assertEquals(new URL(b, "1a/p/1"), cl3.getResource("p/1"));
        assertEquals(new URL(b, "3a/p/1"), cl3.findResource("p/1"));
        assertEquals(Arrays.asList(new URL(b, "1a/p/1"), new URL(b, "1b/p/1"), new URL(b, "3a/p/1"), new URL(b, "3b/p/1")),
                Collections.list(cl3.getResources("p/1")));
        assertEquals(Arrays.asList(new URL(b, "3a/p/1"), new URL(b, "3b/p/1")), Collections.list(cl3.findResources("p/1")));
        assertEquals(new URL(b, "1a/p/1"), cl4.getResource("p/1"));
        assertEquals(new URL(b, "2a/p/2"), cl4.getResource("p/2"));
        assertEquals(new URL(b, "3a/p/3"), cl4.getResource("p/3"));
        assertEquals(Arrays.asList(new URL(b, "1a/p/1"), new URL(b, "1b/p/1"), new URL(b, "3a/p/1"), new URL(b, "3b/p/1")),
                Collections.list(cl4.getResources("p/1")));
        assertEquals(Arrays.asList(new URL(b, "2a/p/2"), new URL(b, "2b/p/2")), Collections.list(cl4.getResources("p/2")));
        assertEquals(Arrays.asList(new URL(b, "3a/p/3"), new URL(b, "3b/p/3")), Collections.list(cl4.getResources("p/3")));
    }

    public void testAlienClassloader() throws Exception {
        URL u;

        final class Loader extends ProxyClassLoader {

            ClassLoader l;

            public Loader(String... publicPackages) throws MalformedURLException {
                super(new ClassLoader[0], true);
                addCoveredPackages(Arrays.asList(publicPackages));
            }

            @Override
            public URL findResource(String name) {
                if ("org/fakepkg/Something.txt".equals(name)) {
                    URL u = ModuleFactoryAlienTest.class.getResource("/org/fakepkg/resource1.txt");
                    assertNotNull("text found", u);
                    return u;
                }
                return null;
            }

            @Override
            public Enumeration<URL> findResources(String name) {
                return Enumerations.empty();
            }

            @Override
            protected Class<?> doLoadClass(String pkg, String name) {
                if (name.equals("org.fakepkg.FakeIfce")) {
                    return FakeIfceHidden.class;
                }
                return null;
            }

            @Override
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                Class<?> c = findLoadedClass(name);
                if (c != null) {
                    return c;
                }
                if (l != null) {
                    try {
                        c = l.loadClass(name);
                        if (resolve) {
                            resolveClass(c);
                        }
                        return c;
                    } catch (ClassNotFoundException x) {}
                }
                return super.loadClass(name, resolve);
            }

            @Override
            public String toString() {
                return "Alien[test]";
            }
        }

        File j1 = new File(jars, "simple-module.jar");
        ClassLoader l1 = new URLClassLoader(new URL[] { Utilities.toURI(j1).toURL() });

        Loader loader = new Loader("org.bar", "org.fakepkg");
        File jar = new File(jars, "depends-on-simple-module.jar");
        loader.l = new URLClassLoader(new URL[] { Utilities.toURI(jar).toURL() }, l1);


        Class<?> clazz = loader.loadClass("org.bar.SomethingElse");
        Class<?> sprclass = loader.loadClass("org.foo.Something");

        assertEquals("Correct parent is used", sprclass, clazz.getSuperclass());

        u = loader.getResource("org/fakepkg/Something.txt");
        assertNotNull("Resource found", u);

        clazz = loader.loadClass("org.fakepkg.FakeIfce");
        assertNotNull("Class loaded", clazz);
        assertEquals("it is our fake class", FakeIfceHidden.class, clazz);

    }
}
