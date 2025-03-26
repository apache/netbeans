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

import java.lang.reflect.Method;
import java.net.URL;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.net.URLClassLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.test.AnnotationProcessorTestUtils;
import org.netbeans.junit.NbTestCase;
import org.openide.util.NbBundle.Messages;
import org.openide.util.test.TestFileUtils;
import static org.netbeans.modules.openide.util.Bundle.*;
import org.openide.util.Utilities;

@Messages("k3=value #3")
public class NbBundleProcessorTest extends NbTestCase {

    public NbBundleProcessorTest(String n) {
        super(n);
    }

    private File src;
    private File dest;
    protected @Override void setUp() throws Exception {
        clearWorkDir();
        src = new File(getWorkDir(), "src");
        dest = new File(getWorkDir(), "classes");
    }

    @Messages({
        "k1=value #1",
        "k2=value #2"
    })
    public void testBasicUsage() throws Exception {
        assertEquals("value #1", k1());
        assertEquals("value #2", k2());
        assertEquals("value #3", k3());
    }

    @Messages({
        "f1=problem with {0}",
        "# {0} - input file",
        "# {1} - pattern",
        "f2={0} did not match {1}",
        "LBL_BuildMainProjectAction_Name=&Build {0,choice,-1#Main Project|0#Project|1#Project ({1})|1<{0} Projects}"
    })
    public void testMessageFormats() throws Exception {
        assertEquals("problem with stuff", f1("stuff"));
        assertEquals("1 did not match 2", f2(1, 2));
        assertEquals("&Build Main Project", LBL_BuildMainProjectAction_Name(-1, "whatever"));
        assertEquals("&Build Project", LBL_BuildMainProjectAction_Name(0, "whatever"));
        assertEquals("&Build Project (whatever)", LBL_BuildMainProjectAction_Name(1, "whatever"));
        assertEquals("&Build 2 Projects", LBL_BuildMainProjectAction_Name(2, "whatever"));
    }

    public void testFieldUsage() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C",
                "public class C {",
                "@org.openide.util.NbBundle.Messages(\"k=v\")",
                "public static final Object X = new Object() {public String toString() {return Bundle.k();}};",
                "}");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        ClassLoader l = new URLClassLoader(new URL[] {Utilities.toURI(dest).toURL()});
        assertEquals("v", l.loadClass("p.C").getField("X").get(null).toString());
    }

    @Messages({
        "s1=Don't worry",
        "s2=Don''t worry about {0}",
        "s3=@camera Say \"cheese\"",
        "s4=<bra&ket>",
        "s5=Operators: +-*/=",
        "s6=One thing.\nAnd another."
    })
    public void testSpecialCharacters() throws Exception {
        assertEquals("Don't worry", s1());
        assertEquals("Don't worry about me", s2("me"));
        assertEquals("@camera Say \"cheese\"", s3());
        assertEquals("<bra&ket>", s4());
        assertEquals("Operators: +-*/=", s5());
        assertEquals("One thing.\nAnd another.", s6());
    }

    @Messages({
        "some key=some value",
        "public=property",
        "2+2=4"
    })
    public void testNonIdentifierKeys() throws Exception {
        assertEquals("some value", some_key());
        assertEquals("property", _public());
        assertEquals("4", _2_2());
    }

    public void testPackageKeys() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.package-info", "@org.openide.util.NbBundle.Messages(\"k=v\")", "package p;");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        ClassLoader l = new URLClassLoader(new URL[] {Utilities.toURI(dest).toURL()});
        Method m = l.loadClass("p.Bundle").getDeclaredMethod("k");
        m.setAccessible(true);
        assertEquals("v", m.invoke(null));
    }

    public void testDupeErrorSimple() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@org.openide.util.NbBundle.Messages({\"k=v1\", \"k=v2\"})", "class C {}");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("uplicate"));
    }

    public void testDupeErrorByIdentifier() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@org.openide.util.NbBundle.Messages({\"k.=v1\", \"k,=v2\"})", "class C {}");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("uplicate"));
    }

    public void testDupeErrorAcrossClasses() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C1", "@org.openide.util.NbBundle.Messages({\"k=v\"})", "class C1 {}");
        AnnotationProcessorTestUtils.makeSource(src, "p.C2", "@org.openide.util.NbBundle.Messages({\"k=v\"})", "class C2 {}");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("uplicate"));
        assertTrue(err.toString(), err.toString().contains("C1.java"));
        assertTrue(err.toString(), err.toString().contains("C2.java"));
    }

    public void testDupeErrorAcrossClassesIncremental() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C1", "@org.openide.util.NbBundle.Messages({\"k=v1\"})", "class C1 {}");
        AnnotationProcessorTestUtils.makeSource(src, "p.C2", "@org.openide.util.NbBundle.Messages({\"k=v2\"})", "class C2 {}");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, "C1.java", dest, null, err));
        assertTrue(err.toString(), err.toString().contains("uplicate"));
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, "C2.java", dest, null, err));
        assertTrue(err.toString(), err.toString().contains("uplicate"));
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, "C2.java", dest, null, err));
        assertTrue(err.toString(), err.toString().contains("uplicate"));
    }

    public void testNoEqualsError() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@org.openide.util.NbBundle.Messages(\"whatever\")", "class C {}");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("="));
    }

    public void testWhitespaceError() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@org.openide.util.NbBundle.Messages(\"key = value\")", "class C {}");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("="));
    }

    @Messages({
        "# {0} - in use", "# {1} - not in use", "unused_param_1=please remember {0}",
        "# {0} - not in use", "# {1} - in use", "unused_param_2=I will remember {1}"
    })
    public void testNonexistentParameter() throws Exception {
        assertEquals("please remember me", unused_param_1("me", "you"));
        assertEquals("I will remember you", unused_param_2("me", "you"));
    }

    public void testExistingBundle() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@org.openide.util.NbBundle.Messages(\"k=v\")", "class C {}");
        TestFileUtils.writeFile(new File(src, "p/Bundle.properties"), "# original comment\nold=stuff\n");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        assertEquals("k=v\n# original comment\nold=stuff\n", TestFileUtils.readFile(new File(dest, "p/Bundle.properties")).replace("\r\n", "\n"));
        // Also check that we can recompile:
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        assertEquals("k=v\n# original comment\nold=stuff\n", TestFileUtils.readFile(new File(dest, "p/Bundle.properties")).replace("\r\n", "\n"));
    }

    public void testDupeErrorWithExistingBundle() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@org.openide.util.NbBundle.Messages(\"k=v\")", "class C {}");
        TestFileUtils.writeFile(new File(src, "p/Bundle.properties"), "k=v\n");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("uplicate"));
    }

    public void testIncrementalCompilation() throws Exception {
        if (isJDK7EarlyBuild()) {
            System.err.println("Running on buggy JDK, skipping testIncrementalCompilation...");
            return;
        }
        AnnotationProcessorTestUtils.makeSource(src, "p.C1", "@org.openide.util.NbBundle.Messages(\"k1=v1\")", "public class C1 {public @Override String toString() {return Bundle.k1();}}");
        AnnotationProcessorTestUtils.makeSource(src, "p.C2", "@org.openide.util.NbBundle.Messages(\"k2=v2\")", "public class C2 {public @Override String toString() {return Bundle.k2();}}");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        ClassLoader l = new URLClassLoader(new URL[] {Utilities.toURI(dest).toURL()});
        assertEquals("v1", l.loadClass("p.C1").getDeclaredConstructor().newInstance().toString());
        assertEquals("v2", l.loadClass("p.C2").getDeclaredConstructor().newInstance().toString());
        AnnotationProcessorTestUtils.makeSource(src, "p.C1", "@org.openide.util.NbBundle.Messages(\"k1=v3\")", "public class C1 {public @Override String toString() {return Bundle.k1();}}");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, "C1.java", dest, null, null));
        l = new URLClassLoader(new URL[] {Utilities.toURI(dest).toURL()});
        assertEquals("v3", l.loadClass("p.C1").getDeclaredConstructor().newInstance().toString());
        assertEquals("v2", l.loadClass("p.C2").getDeclaredConstructor().newInstance().toString());
        AnnotationProcessorTestUtils.makeSource(src, "p.C1", "@org.openide.util.NbBundle.Messages(\"k3=v4\")", "public class C1 {public @Override String toString() {return Bundle.k3();}}");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, "C1.java", dest, null, null));
        l = new URLClassLoader(new URL[] {Utilities.toURI(dest).toURL()});
        assertEquals("v4", l.loadClass("p.C1").getDeclaredConstructor().newInstance().toString());
        assertEquals("v2", l.loadClass("p.C2").getDeclaredConstructor().newInstance().toString());
    }

    public void testIncrementalCompilationWithBrokenClassFiles() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C1", "@org.openide.util.NbBundle.Messages(\"k1=v1\")", "public class C1 {public @Override String toString() {return Bundle.k1();}}");
        AnnotationProcessorTestUtils.makeSource(src, "p.C2", "@org.openide.util.NbBundle.Messages(\"k2=v2\")", "public class C2 {public @Override String toString() {return Bundle.k2();}}");
        AnnotationProcessorTestUtils.makeSource(src, "p.C3", "class C3 {C3() {new Runnable() {public @Override void run() {new Runnable() {public @Override void run() {}};}};}}");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        ClassLoader l = new URLClassLoader(new URL[] {Utilities.toURI(dest).toURL()});
        assertEquals("v1", l.loadClass("p.C1").getDeclaredConstructor().newInstance().toString());
        assertEquals("v2", l.loadClass("p.C2").getDeclaredConstructor().newInstance().toString());
        assertTrue(new File(dest, "p/C3.class").delete());
        assertTrue(new File(dest, "p/C3$1.class").delete());
        assertTrue(new File(dest, "p/C3$1$1.class").isFile());
        AnnotationProcessorTestUtils.makeSource(src, "p.C1", "@org.openide.util.NbBundle.Messages(\"k1=v3\")", "public class C1 {public @Override String toString() {return Bundle.k1();}}");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, "C1.java", dest, null, null));
        l = new URLClassLoader(new URL[] {Utilities.toURI(dest).toURL()});
        assertEquals("v3", l.loadClass("p.C1").getDeclaredConstructor().newInstance().toString());
        assertEquals("v2", l.loadClass("p.C2").getDeclaredConstructor().newInstance().toString());
    }

    public void testIncrementalCompilationWithPackageInfo() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@org.openide.util.NbBundle.Messages(\"k1=v1\")", "public class C {public @Override String toString() {return Bundle.k1() + Bundle.k2();}}");
        AnnotationProcessorTestUtils.makeSource(src, "p.package-info", "@org.openide.util.NbBundle.Messages(\"k2=v2\")", "package p;");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        ClassLoader l = new URLClassLoader(new URL[] {Utilities.toURI(dest).toURL()});
        assertEquals("v1v2", l.loadClass("p.C").getDeclaredConstructor().newInstance().toString());
        assertTrue(new File(dest, "p/C.class").delete());
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, "C.java", dest, null, null));
        l = new URLClassLoader(new URL[] {Utilities.toURI(dest).toURL()});
        assertEquals("v1v2", l.loadClass("p.C").getDeclaredConstructor().newInstance().toString());
    }

    public void testComments() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@org.openide.util.NbBundle.Messages({\"# Something good to note.\", \"k=v\"})", "class C {}");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        assertEquals("# Something good to note.\nk=v\n", TestFileUtils.readFile(new File(dest, "p/Bundle.properties")).replace("\r\n", "\n"));
        // Also check that we can recompile:
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        assertEquals("# Something good to note.\nk=v\n", TestFileUtils.readFile(new File(dest, "p/Bundle.properties")).replace("\r\n", "\n"));
        // XXX also check non-ASCII chars in comments; works locally but fails on deadlock
    }

    public void testParameterDescriptions() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p1.C1", "@org.openide.util.NbBundle.Messages({\"# {0} - first\", \"k1={0}\"})", "class C1 {String s = Bundle.k1(null);}");
        AnnotationProcessorTestUtils.makeSource(src, "p2.C2", "@org.openide.util.NbBundle.Messages({\"# {0} - first\", \"k2={0} {1}\"})", "class C2 {String s = Bundle.k2(null, null);}");
        AnnotationProcessorTestUtils.makeSource(src, "p3.C3", "@org.openide.util.NbBundle.Messages(\"k3={0}\")", "class C3 {String s = Bundle.k3(null);}");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertTrue(err.toString(), AnnotationProcessorTestUtils.runJavac(src, "C1.java", dest, null, err));
        assertEquals("", err.toString());
        err.reset();
        assertTrue(err.toString(), AnnotationProcessorTestUtils.runJavac(src, "C2.java", dest, null, err));
        assertTrue(err.toString(), err.toString().contains("Undocumented format parameter {1}"));
        assertFalse(err.toString(), err.toString().contains("Undocumented format parameter {0}"));
        err.reset();
        assertTrue(err.toString(), AnnotationProcessorTestUtils.runJavac(src, "C3.java", dest, null, err));
        assertTrue(err.toString(), err.toString().contains("Undocumented format parameter {0}"));
    }

    /** @see org.openide.util.NbBundle.DebugLoader.DebugInputStream */
    public void testNOI18N() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C1", "@org.openide.util.NbBundle.Messages({\"#NOI18N\", \"k1=ON_EXIT\"})", "class C1 {String s = Bundle.k1();}");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertTrue(err.toString(), AnnotationProcessorTestUtils.runJavac(src, "C1.java", dest, null, err));
        assertEquals("", err.toString());
        err.reset();
        AnnotationProcessorTestUtils.makeSource(src, "p.C2", "@org.openide.util.NbBundle.Messages({\"# NOI18N\", \"k2=ON_EXIT\"})", "class C2 {String s = Bundle.k2();}");
        assertFalse(err.toString(), AnnotationProcessorTestUtils.runJavac(src, "C2.java", dest, null, err));
        assertTrue(err.toString(), err.toString().contains("NOI18N"));
    }

    private static boolean isJDK7EarlyBuild() {
        String run = System.getProperty("java.runtime.version");
        if ("1.7".equals(System.getProperty("java.specification.version")) && run != null) {
            if (run.startsWith("1.7.0-ea")) {
                return true;
            }
            // builds up until
            // java.runtime.version=1.7.0-b147
            // are known to fail testIncrementalCompilation
            // target release 7u2; 8-ea-b09 should also have fix; cf. #7068451
            Pattern buildNumber = Pattern.compile("1\\.7\\.0-b([0-9]+)");
            Matcher m = buildNumber.matcher(run);
            if (m.matches()) {
                if (Integer.parseInt(m.group(1)) <= 147) {
                    return true;
                }
            }
        }
        return false;
    }
}
