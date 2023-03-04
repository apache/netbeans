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

package org.netbeans.modules.projectapi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.AnnotationProcessorTestUtils;
import org.openide.util.test.MockLookup;

public class LazyLookupProvidersTest extends NbTestCase {

    public LazyLookupProvidersTest(String n) {
        super(n);
    }

    public void testLazyProviders() throws Exception {
        TrackingLoader l = new TrackingLoader();
        MockLookup.setInstances(l);
        l.assertLoadedClasses();
        Lookup all = LookupProviderSupport.createCompositeLookup(Lookups.fixed("hello"), "Projects/x/Lookup");
        Lookup all2 = LookupProviderSupport.createCompositeLookup(Lookup.EMPTY, "Projects/x/Lookup");
        l.assertLoadedClasses();
        assertEquals("hello", all.lookup(String.class));
        l.assertLoadedClasses();
        Collection<?> svcs2 = all.lookupAll(l.loadClass(Service2.class.getName()));
        assertEquals(1, svcs2.size());
        assertEquals(ServiceImpl2.class.getName(), svcs2.iterator().next().getClass().getName());
        l.assertLoadedClasses("Service2", "ServiceImpl2");
        Collection<?> svcs1 = all.lookupAll(l.loadClass(Service1.class.getName()));
        assertEquals(svcs1.toString(), 1, svcs1.size());
        l.assertLoadedClasses("MergedServiceImpl1", "Merger", "Service1", "Service2", "ServiceImpl1a", "ServiceImpl1b", "ServiceImpl2");
        assertTrue(svcs1.toString(), svcs1.toString().contains("ServiceImpl1a@"));
        assertTrue(svcs1.toString(), svcs1.toString().contains("ServiceImpl1b@"));
        assertTrue(svcs1.toString(), svcs1.toString().contains("Merge["));
        // #166910: also test subsequent independent lookups (i.e. other projects)
        svcs1 = all2.lookupAll(l.loadClass(Service1.class.getName()));
        assertEquals(svcs1.toString(), 1, svcs1.size());
        assertTrue(svcs1.toString(), svcs1.toString().contains("ServiceImpl1a@"));
        assertTrue(svcs1.toString(), svcs1.toString().contains("ServiceImpl1b@"));
        assertTrue(svcs1.toString(), svcs1.toString().contains("Merge["));
    }

    public interface Service1 {}

    public interface Service2 {}

    @ProjectServiceProvider(projectType="x", service=Service1.class)
    public static class ServiceImpl1a implements Service1 {}

    public static class ServiceImpl1b implements Service1 {
        private ServiceImpl1b(boolean x) {assert x;}
        public void someUnrelatedMethod() {}
        @ProjectServiceProvider(projectType="x", service=Service1.class)
        public static Service1 makeService() {return new ServiceImpl1b(true);}
    }

    @ProjectServiceProvider(projectType="x", service=Service2.class)
    public static class ServiceImpl2 implements Service2 {
        public ServiceImpl2(Lookup base) {
            assertNotNull(base.lookup(String.class));
        }
    }

    @LookupMerger.Registration(projectType="x")
    public static class Merger implements LookupMerger<Service1> {
        public Class<Service1> getMergeableClass() {
            return Service1.class;
        }
        public Service1 merge(final Lookup lkp) {
            return new MergedServiceImpl1(lkp.lookupAll(Service1.class));
        }
    }
    private static class MergedServiceImpl1 implements Service1 {
        private final Collection<? extends Service1> delegates;
        MergedServiceImpl1(Collection<? extends Service1> delegates) {
            this.delegates = delegates;
        }
        public @Override String toString() {
            return "Merge" + delegates;
        }
    }

    public void testMultiplyImplementedService() throws Exception {
        TrackingLoader l = new TrackingLoader();
        MockLookup.setInstances(l);
        l.assertLoadedClasses();
        Lookup all = LookupProviderSupport.createCompositeLookup(Lookup.EMPTY, "Projects/y/Lookup");
        l.assertLoadedClasses();
        Collection<?> instances = all.lookupAll(l.loadClass(Service3.class.getName()));
        assertEquals(1, instances.size());
        l.assertLoadedClasses("Service3", "Service34Impl", "Service4");
        assertEquals(instances, all.lookupAll(l.loadClass(Service4.class.getName())));
        l.assertLoadedClasses("Service3", "Service34Impl", "Service4");
    }
    
    public void testLazyLookupToString() throws Exception {
        Lookup all = LookupProviderSupport.createCompositeLookup(Lookup.EMPTY, "Projects/y/Lookup");
        final String str = all.toString();
        if (str.contains("ProxyLookup(class=class org.netbeans.modules.projectapi.LazyLookupProviders")) {
            fail("ProxyLookup from LazyLookupProviders should have better name\n" + str);
        }
        if (!str.contains("service=org.netbeans.modules.projectapi.LazyLookupProvidersTest$Service3")) {
            fail("Name of service lookup delivers should be visible:\n" + str);
        }
        if (!str.contains("class=" + Service34Impl.class.getName())) {
            fail("Name of impl class should be visible:\n" + str);
        }
        if (!str.contains("service=org.netbeans.modules.projectapi.LazyLookupProvidersTest$Service3")) {
            fail("Name of service lookup delivers should be visible:\n" + str);
        }
        if (!str.contains("Projects/y/Lookup/org-netbeans-modules-projectapi-LazyLookupProvidersTest$Service34Impl.instance")) {
            fail("We should see the file object lookup is coming from:\n" + str);
        }
    }

    public interface Service3 {}

    public interface Service4 {}

    @ProjectServiceProvider(projectType="y", service={Service3.class, Service4.class})
    public static class Service34Impl implements Service3, Service4 {}

    /**
     * Cannot simply use static initializers to tell when classes are loaded;
     * these will not be run in case a service is loaded but not yet initialized.
     */
    private static class TrackingLoader extends URLClassLoader {
        private final Set<Class<?>> loadedClasses = new HashSet<Class<?>>();
        TrackingLoader() {
            super(new URL[] {LazyLookupProvidersTest.class.getProtectionDomain().getCodeSource().getLocation()},
                  LazyLookupProvidersTest.class.getClassLoader());
        }
        protected @Override synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith(LazyLookupProvidersTest.class.getName() + "$")) {
                Class c = findLoadedClass(name);
                if (c == null) {
                    // do not delegate to parent, i.e. be sure we have loaded it
                    c = findClass(name);
                    if (resolve) {
                        resolveClass(c);
                    }
                    loadedClasses.add(c);
                }
                return c;
            } else {
                return super.loadClass(name, resolve);
            }
        }
        void assertLoadedClasses(String... names) {
            SortedSet<String> actual = new TreeSet<String>();
            for (Class<?> clazz : loadedClasses) {
                actual.add(clazz.getName().replaceFirst("^\\Q" + LazyLookupProvidersTest.class.getName() + "$\\E", ""));
            }
            assertEquals(Arrays.toString(names), actual.toString());
        }
    }

    public void testAnnotationAccessibility() throws Exception {
        clearWorkDir();
        File src = new File(getWorkDir(), "src");
        File dest = new File(getWorkDir(), "classes");
        AnnotationProcessorTestUtils.makeSource(src, "p.C",
                "@org.netbeans.spi.project.ProjectServiceProvider(service=Runnable.class, projectType=\"test\")",
                "public class C implements Runnable {",
                " public C(org.netbeans.api.project.Project p) {}",
                " public void run() {}",
                "}");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        AnnotationProcessorTestUtils.makeSource(src, "p.C",
                "@org.netbeans.spi.project.ProjectServiceProvider(service=Runnable.class, projectType=\"test\")",
                "public class C implements Runnable {",
                " C(org.netbeans.api.project.Project p) {}",
                " public void run() {}",
                "}");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, baos));
        assertTrue(baos.toString(), baos.toString().contains("public"));
        AnnotationProcessorTestUtils.makeSource(src, "p.C",
                "@org.netbeans.spi.project.ProjectServiceProvider(service=Runnable.class, projectType=\"test\")",
                "class C implements Runnable {",
                " public C(org.netbeans.api.project.Project p) {}",
                " public void run() {}",
                "}");
        baos = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, baos));
        assertTrue(baos.toString(), baos.toString().contains("public"));
        AnnotationProcessorTestUtils.makeSource(src, "p.C",
                "public class C {",
                " @org.netbeans.spi.project.ProjectServiceProvider(service=Runnable.class, projectType=\"test\")",
                " public static Runnable m(org.netbeans.api.project.Project p) {return null;}",
                "}");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        AnnotationProcessorTestUtils.makeSource(src, "p.C",
                "public class C {",
                " @org.netbeans.spi.project.ProjectServiceProvider(service=Runnable.class, projectType=\"test\")",
                " static Runnable m(org.netbeans.api.project.Project p) {return null;}",
                "}");
        baos = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, baos));
        assertTrue(baos.toString(), baos.toString().contains("public"));
        AnnotationProcessorTestUtils.makeSource(src, "p.C",
                "class C {",
                " @org.netbeans.spi.project.ProjectServiceProvider(service=Runnable.class, projectType=\"test\")",
                " public static Runnable m(org.netbeans.api.project.Project p) {return null;}",
                "}");
        baos = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, baos));
        assertTrue(baos.toString(), baos.toString().contains("public"));
        AnnotationProcessorTestUtils.makeSource(src, "p.C",
                "public class C {",
                " @org.netbeans.spi.project.ProjectServiceProvider(service=Runnable.class, projectType=\"test\")",
                " public class Inner implements Runnable {public void run() {}}",
                "}");
        baos = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, baos));
        assertTrue(baos.toString(), baos.toString().contains("inner"));
    }

}
