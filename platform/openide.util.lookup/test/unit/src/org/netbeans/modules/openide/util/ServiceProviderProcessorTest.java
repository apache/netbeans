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

package org.netbeans.modules.openide.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.openide.util.test.AnnotationProcessorTestUtils;

public class ServiceProviderProcessorTest extends NbTestCase {

    public ServiceProviderProcessorTest(String n) {
        super(n);
    }

    private static List<Class<?>> classesOf(Iterable<?> objects) {
        List<Class<?>> cs = new ArrayList<Class<?>>();
        for (Object o : objects) {
            cs.add(o.getClass());
        }
        return cs;
    }

    private static List<Class<?>> classesOfLookup(Class<?> xface) {
        return classesOf(Lookup.getDefault().lookupAll(xface));
    }

    private static List<Class<?>> sortClassList(List<Class<?>> classes) {
        List<Class<?>> sorted = new ArrayList<Class<?>>(classes);
        sorted.sort(new Comparator<Class<?>>() {
            public int compare(Class<?> c1, Class<?> c2) {
                return c1.getName().compareTo(c2.getName());
            }
        });
        return sorted;
    }

    public void testBasicUsage() throws Exception {
        assertEquals(Collections.singletonList(Implementation.class), classesOfLookup(Interface.class));
    }
    public interface Interface {}
    @ServiceProvider(service=Interface.class)
    public static class Implementation implements Interface {}

    public void testPosition() throws Exception {
        assertEquals(Arrays.<Class<?>>asList(OrderedImpl3.class, OrderedImpl2.class, OrderedImpl1.class), classesOfLookup(OrderedInterface.class));
        // #181095: order in file should also be fixed, for benefit of ServiceLoader.
        BufferedReader r = new BufferedReader(new InputStreamReader(ServiceProviderProcessorTest.class.getResourceAsStream(
                "/META-INF/services/" + OrderedInterface.class.getName())));
        List<String> lines = new ArrayList<String>();
        String line;
        while ((line = r.readLine()) != null) {
            lines.add(line);
        }
        assertEquals(Arrays.asList(
                OrderedImpl3.class.getName(),
                "#position=100",
                OrderedImpl2.class.getName(),
                "#position=200",
                OrderedImpl1.class.getName()
                ), lines);
    }
    public interface OrderedInterface {}
    @ServiceProvider(service=OrderedInterface.class)
    public static class OrderedImpl1 implements OrderedInterface {}
    @ServiceProvider(service=OrderedInterface.class, position=200)
    public static class OrderedImpl2 implements OrderedInterface {}
    @ServiceProvider(service=OrderedInterface.class, position=100)
    public static class OrderedImpl3 implements OrderedInterface {}

    public void testPath() throws Exception {
        assertEquals(Collections.singletonList(PathImplementation.class), classesOf(Lookups.forPath("some/path").lookupAll(Interface.class)));
    }
    @ServiceProvider(service=Interface.class, path="some/path")
    public static class PathImplementation implements Interface {}

    public void testSupersedes() throws Exception {
        assertEquals(Arrays.<Class<?>>asList(Overrider.class, Unrelated.class), sortClassList(classesOfLookup(CancellableInterface.class)));
    }
    public interface CancellableInterface {}
    @ServiceProvider(service=CancellableInterface.class)
    public static class Overridden implements CancellableInterface {}
    @ServiceProvider(service=CancellableInterface.class, supersedes="org.netbeans.modules.openide.util.ServiceProviderProcessorTest$Overridden")
    public static class Overrider implements CancellableInterface {}
    @ServiceProvider(service=CancellableInterface.class)
    public static class Unrelated implements CancellableInterface {}

    public void testMultipleRegistrations() throws Exception {
        assertEquals(Collections.singletonList(Multitasking.class), classesOfLookup(Interface1.class));
        assertEquals(Collections.singletonList(Multitasking.class), classesOfLookup(Interface2.class));
    }
    public interface Interface1 {}
    public interface Interface2 {}
    @ServiceProviders({@ServiceProvider(service=Interface1.class), @ServiceProvider(service=Interface2.class)})
    public static class Multitasking implements Interface1, Interface2 {}

    public void testErrorReporting() throws Exception {
        clearWorkDir();
        File src = new File(getWorkDir(), "src");
        File dest = new File(getWorkDir(), "classes");
        String xfaceName = Interface.class.getCanonicalName();

        AnnotationProcessorTestUtils.makeSource(src, "p.C1",
                "@org.openide.util.lookup.ServiceProvider(service=" + xfaceName + ".class)",
                "public class C1 implements " + xfaceName + " {}");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, "C1", dest, null, baos));

        AnnotationProcessorTestUtils.makeSource(src, "p.C2",
                "@org.openide.util.lookup.ServiceProvider(service=" + xfaceName + ".class)",
                "class C2 implements " + xfaceName + " {}");
        baos = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, "C2", dest, null, baos));
        assertTrue(baos.toString(), baos.toString().contains("public"));

        AnnotationProcessorTestUtils.makeSource(src, "p.C3",
                "@org.openide.util.lookup.ServiceProvider(service=" + xfaceName + ".class)",
                "public class C3 implements " + xfaceName + " {",
                "public C3(boolean x) {}",
                "}");
        baos = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, "C3", dest, null, baos));
        assertTrue(baos.toString(), baos.toString().contains("constructor"));

        AnnotationProcessorTestUtils.makeSource(src, "p.C4",
                "@org.openide.util.lookup.ServiceProvider(service=" + xfaceName + ".class)",
                "public class C4 implements " + xfaceName + " {",
                "C4() {}",
                "}");
        baos = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, "C4", dest, null, baos));
        assertTrue(baos.toString(), baos.toString().contains("constructor"));

        AnnotationProcessorTestUtils.makeSource(src, "p.C5",
                "@org.openide.util.lookup.ServiceProvider(service=" + xfaceName + ".class)",
                "public abstract class C5 implements " + xfaceName + " {}");
        baos = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, "C5", dest, null, baos));
        assertTrue(baos.toString(), baos.toString().contains("abstract"));

        AnnotationProcessorTestUtils.makeSource(src, "p.C6",
                "@org.openide.util.lookup.ServiceProvider(service=" + xfaceName + ".class)",
                "public class C6 {}");
        baos = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, "C6", dest, null, baos));
        assertTrue(baos.toString(), baos.toString().contains("assignable"));

        AnnotationProcessorTestUtils.makeSource(src, "p.C7",
                "@org.openide.util.lookup.ServiceProvider(service=" + xfaceName + ".class)",
                "public interface C7 extends " + xfaceName + " {}");
        baos = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, "C7", dest, null, baos));
        assertTrue(baos.toString(), baos.toString().contains("not applicable"));

        AnnotationProcessorTestUtils.makeSource(src, "p.C8",
                "class C8 {",
                "@org.openide.util.lookup.ServiceProvider(service=" + xfaceName + ".class)",
                "public static class Inner implements " + xfaceName + " {}",
                "}");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, "C8", dest, null, baos));

        AnnotationProcessorTestUtils.makeSource(src, "p.C9",
                "class C9 {",
                "@org.openide.util.lookup.ServiceProvider(service=" + xfaceName + ".class)",
                "public class Inner implements " + xfaceName + " {}",
                "}");
        baos = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, "C9", dest, null, baos));
        assertTrue(baos.toString(), baos.toString().contains("static"));
    }

    public void testInvalidInput() throws Exception { // #195983
        clearWorkDir();
        File src = new File(getWorkDir(), "src");
        File dest = new File(getWorkDir(), "classes");
        String xfaceName = Interface.class.getCanonicalName();

        AnnotationProcessorTestUtils.makeSource(src, "p.C1",
                "public class C1 {",
                "@org.openide.util.lookup.ServiceProvider(service=" + xfaceName + ".class) public static " + xfaceName + " m() {return null;}",
                "}");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, "C1", dest, null, baos));
        assertTrue(baos.toString(), baos.toString().contains("not applicable"));
    }
    
    public void testInnerClassError() throws Exception {
        clearWorkDir();
        File src = new File(getWorkDir(), "src");
        File dest = new File(getWorkDir(), "classes");

        AnnotationProcessorTestUtils.makeSource(src, "p.C1",
                "public class C1 {",
                "  @org.openide.util.lookup.ServiceProvider(service=java.io.Serializable.class)",
                "  public class Inner implements java.io.Serializable {}",
                "}");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertFalse("Compilation should fail", AnnotationProcessorTestUtils.runJavac(src, "C1", dest, null, baos));
        assertTrue("Error should contain warning about static:\n" + baos.toString(), baos.toString().contains("needs to be static"));
    }

}
