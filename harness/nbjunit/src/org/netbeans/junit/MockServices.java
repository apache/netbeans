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

package org.netbeans.junit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lets you register mock implementations of global services.
 * You might for example do this in {@link junit.framework.TestCase#setUp()}.
 * <p>If you need to register individual instances, and are using the <code>Lookup</code>
 * framework, try <code>org.openide.util.test.MockLookup</code>.
 * @see <a href="https://github.com/apache/netbeans/tree/master/platform/openide.util.lookup/src/org/openide/util/Lookup.java"><code>Lookup</code></a>
 * @see java.util.ServiceLoader
 * @since org.netbeans.modules.nbjunit/1 1.30
 * @author Jesse Glick, Jaroslav Tulach
 */
public class MockServices {

    private MockServices() {}

    /**
     * Set (or reset) the set of mock services.
     * Clears any previous registration.
     * After this call, <code>Lookup</code> and <code>ServiceLoader</code> should both
     * "see" the newly registered classes.
     * (Other classes really registered in <code>META-INF/services/</code> will
     * also be available, but after the ones you have registered.)
     * Each class must be public and concrete with a public no-arg constructor.
     * @param services a set of service classes to register
     * @throws IllegalArgumentException if some classes are not instantiable as beans
     */
    public static void setServices(Class<?>... services) throws IllegalArgumentException {
        try {
            if (
                System.getProperty("netbeans.home") != null &&
                System.getProperty("netbeans.user") != null
            ) {
                Class<?> mainLookup = forName("org.netbeans.core.startup.MainLookup");
                ClassLoader l = new ServiceClassLoader(services, Thread.currentThread().getContextClassLoader(), false);
                Method sClsLoaderChanged = mainLookup.getDeclaredMethod("systemClassLoaderChanged", ClassLoader.class);
                sClsLoaderChanged.setAccessible(true);
                sClsLoaderChanged.invoke(null, l);
                return;
            }
        } catch (ClassNotFoundException ex) {
            // Fine, not using core.jar.
        } catch (Exception exc) {
            LOG.log(Level.WARNING, "MainLookup couldn't be notified about the context class loader change", exc);
        }

        ClassLoader l = new ServiceClassLoader(services);
        // Adapted from org.netbeans.ModuleManager.updateContextClassLoaders. See that class for comments.
        ThreadGroup g = Thread.currentThread().getThreadGroup();
        while (g.getParent() != null) {
            g = g.getParent();
        }
        while (true) {
            int s = g.activeCount() + 1;
            Thread[] ts = new Thread[s];
            int x = g.enumerate(ts, true);
            if (x < s) {
                for (int i = 0; i < x; i++) {
                    try {
                        ts[i].setContextClassLoader(l);
                    } catch (SecurityException e) {
                        LOG.log(Level.FINE, "Cannot set context classloader for " + ts[i].getName(), e);
                    }
                }
                LOG.log(Level.FINE, "Set context class loader on {0} threads", x);
                break;
            } else {
                LOG.fine("Race condition getting all threads, restarting...");
                continue;
            }
        }

        // Need to also reset global lookup since it caches the singleton and we need to change it.
        try {
            Class<?> mainLookup = Class.forName("org.netbeans.core.startup.MainLookup");
            Method sClsLoaderChanged = mainLookup.getDeclaredMethod("systemClassLoaderChanged",ClassLoader.class);
            sClsLoaderChanged.setAccessible(true);
            sClsLoaderChanged.invoke(null,l);
        } catch (ClassNotFoundException x) {
            // Fine, not using core.jar.
        } catch(Exception exc) {
            LOG.log(Level.WARNING, "MainLookup couldn't be notified about the context class loader change", exc);
        }

        try {
            Class<?> lookup = Class.forName("org.openide.util.Lookup");
            Method defaultLookup = lookup.getDeclaredMethod("resetDefaultLookup");
            defaultLookup.setAccessible(true);
            defaultLookup.invoke(null);
        } catch (ClassNotFoundException x) {
            // Fine, not using org-openide-lookup.jar.
        } catch (Exception x) {
            LOG.log(Level.WARNING, "Could not reset Lookup.getDefault()", x);
        }
    }

    private static Class<?> forName(String name) throws ClassNotFoundException {
        ClassLoader l = Thread.currentThread().getContextClassLoader();
        if (l != null) {
            try {
                return Class.forName(name, true, l);
            } catch (ClassNotFoundException ex) {
                // OK, try again
            }
        }
        return Class.forName(name);
    }


    private static final Logger LOG = Logger.getLogger(MockServices.class.getName());

    private static final class ServiceClassLoader extends ClassLoader {

        private final Class<?>[] services;

        public ServiceClassLoader(Class<?>[] services) {
            this(services, MockServices.class.getClassLoader(), true);
        }
        public ServiceClassLoader(Class<?>[] services, ClassLoader l, boolean test) {
            super(l);
            for (Class<?> c : services) {
                try {
                    if (test) {
                        final Class<?> real = getParent().loadClass(c.getName());
                        if (!c.equals(real)) {
                            throw new AssertionError("Service " + c + " isn't " + real);
                        }
                    }
                    int mods = c.getModifiers();
                    if (!Modifier.isPublic(mods) || Modifier.isAbstract(mods)) {
                        throw new IllegalArgumentException("Class " + c.getName() + " must be public");
                    }
                    c.getConstructor();
                } catch (IllegalArgumentException x) {
                    throw x;
                } catch (NoSuchMethodException x) {
                    throw (IllegalArgumentException) new IllegalArgumentException("Class " + c.getName() + " has no public no-arg constructor").initCause(x);
                } catch (Exception x) {
                    throw new AssertionError(x.toString(), x);
                }
            }
            this.services = services;
        }

        public URL getResource(String name) {
            Enumeration<URL> r;
            try {
                r = getResources(name);
            } catch (IOException x) {
                return null;
            }
            return r.hasMoreElements() ? r.nextElement() : null;
        }

        public Enumeration<URL> getResources(String name) throws IOException {
            if (name.equals("META-INF/services/org.openide.util.Lookup") || name.equals("META-INF/services/org.openide.util.Lookup$Provider")) {
                // Lookup.getDefault checks for these, and we need to really mask it.
                return Collections.enumeration(Collections.<URL>emptySet());
            }
            final Enumeration<URL> supe = super.getResources(name);
            String prefix = "META-INF/services/";
            if (name.startsWith(prefix)) {
                try {
                    Class<?> xface = loadClass(name.substring(prefix.length()));
                    List<String> impls = new ArrayList<String>();
                    for (Class<?> c : services) {
                        boolean assignable = xface.isAssignableFrom(c);
                        if (assignable) {
                            impls.add(c.getName());
                        }
                    }
                    if (!impls.isEmpty()) {
                        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        PrintWriter pw = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8));
                        for (String impl : impls) {
                            pw.println(impl);
                            pw.println("#position=100");
                        }
                        pw.close();
                        final URL u = new URL("metainfservices", null, 0, xface.getName(), new URLStreamHandler() {
                            protected URLConnection openConnection(URL _u) throws IOException {
                                return new URLConnection(_u) {
                                    public void connect() throws IOException {}
                                    public InputStream getInputStream() throws IOException {
                                        return new ByteArrayInputStream(baos.toByteArray());
                                    }
                                };
                            }
                        });
                        return new Enumeration<URL>() {
                            private boolean parent = false;
                            public boolean hasMoreElements() {
                                return !parent || supe.hasMoreElements();
                            }
                            public URL nextElement() throws NoSuchElementException {
                                if (parent) {
                                    return supe.nextElement();
                                } else {
                                    parent = true;
                                    return u;
                                }
                            }
                        };
                    }
                } catch (ClassNotFoundException x) {}
            }
            return supe;
        }

        /*
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            // XXX make sure services can be loaded
            return super.loadClass(name);
        }
         */

    }

}
