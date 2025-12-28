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
package org.netbeans.modules.java.editor.base.semantic;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import javax.lang.model.SourceVersion;
import javax.swing.text.Document;
import org.junit.Test;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author lahvac
 */
public class UnusedDetectorTest extends NbTestCase {

    public UnusedDetectorTest(String name) {
        super(name);
    }

    @Test
    public void testUnusedMethod() throws Exception {
        performTest("test/Test.java",
                    "package test;\n" +
                    "public class Test {\n" +
                    "    private void unusedMethod() {}\n" +
                    "}\n",
                    "3:unusedMethod:NOT_USED");
    }

    @Test
    public void testUnusedParameters() throws Exception {
        performTest("test/Test.java",
                    "package test;\n" +
                    "public abstract class Test {\n" +
                    "    private void method(int unused) {}\n" +
                    "    public void api(String notUnused1) {\n" +
                    "        method(0);\n" +
                    "        l(notUnused2 -> {});\n" +
                    "        EffectivellyPrivate p = x -> x;\n" +
                    "        nativeMethod(p.abstractMethod(0));\n" +
                    "    }\n" +
                    "    {\n" +
                    "        l(notUnused3 -> {});\n" +
                    "    }\n" +
                    "    public String t1 = l(notUnused4 -> {});\n" +
                    "    static {\n" +
                    "        l(notUnused5 -> {});\n" +
                    "    }\n" +
                    "    public static String t2 = l(notUnused6 -> {});\n" +
                    "    private native void nativeMethod(int notUnused7);\n" +
                    "    private interface EffectivellyPrivate {\n" +
                    "       int abstractMethod(int notUnused8);\n" +
                    "    }\n" +
//                    "    private native brokenMethod(int notUnused9);\n" +
                    "    public void l(I i) { }\n" +
                    "    interface I {\n" +
                    "        public String run(int i);\n" +
                    "    }\n" +
                    "}\n",
                    "3:unused:NOT_READ"/*,
                    TODO: javac error recovery recognizes the broken method as a constructor, and the unused detection does not work for it correctly:
                    "22:brokenMethod:NOT_USED"*/);
    }

    @Test
    public void testMethodRecursion() throws Exception {
        performTest("test/Test.java",
                    "package test;\n" +
                    "public class Test {\n" +
                    "    private void unusedRecursive() {\n" +
                    "        unusedRecursive();\n" +
                    "        delay(Test::unusedRecursive);\n" +
                    "        delay(() -> unusedRecursive());\n" +
                    "    }\n" +
                    "    private void used() {}\n" +
                    "    public void use() { delay(Test::used); }\n" +
                    "    public void delay(Runnable r) {}\n" +
                    "}\n",
                    "3:unusedRecursive:NOT_USED");
    }

    @Test
    public void testForEachLoop() throws Exception {
        performTest("test/Test.java",
                    "package test;\n" +
                    "public class Test {\n" +
                    "    public void test(String[] args) {\n" +
                    "        for (String a : args) {\n" +
                    "            System.err.println(a);\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n");
    }

    @Test
    public void testCompoundAssignment() throws Exception {
        performTest("test/Test.java",
                    "package test;\n" +
                    "public class Test {\n" +
                    "    public int compound(int i) {\n" +
                    "        int unused = 0;\n" +
                    "        unused += 9;\n" +
                    "        int used = 0;\n" +
                    "        compound(used += 9);\n" +
                    "        int unused2 = 0;\n" +
                    "        int used2 = 0;\n" +
                    "        unused2 += used2;\n" +
                    "        int unused3 = 0;\n" +
                    "        int u1 = unused3 += 4;\n" +
                    "        int unused4 = 0;\n" +
                    "        int u2 = 0;\n" +
                    "        u2 = unused4 += 4;\n" +
                    "        return u1 + u2;\n" +
                    "    }\n" +
                    "}\n",
                    "4:unused:NOT_READ",
                    "8:unused2:NOT_READ");
    }

    @Test
    public void testBindingPatterns() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_14"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_14, skip tests
            return;
        }
        sourceLevel = "14";
        performTest("test/Test.java",
                    "package test;\n" +
                    "public class Test {\n" +
                    "    public void compound(Object o1, Object o2) {\n" +
                    "        if (o1 instanceof String s1) {\n" +
                    "            return s1;\n" +
                    "        }\n" +
                    "        if (o2 instanceof String s2) {\n" +
                    "            return null;\n" +
                    "        }\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "}\n",
                    "7:s2:NOT_READ");
    }

    @Test
    public void testRecord() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_14"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_14, skip tests
            return;
        }
        sourceLevel = "14";
        performTest("test/Test.java",
                    "package test;\n" +
                    "public record Test(int i, long j) {\n" +
                    "    private void unused() {}\n" +
                    "}\n",
                    "3:unused:NOT_USED");
    }

    @Test
    public void testEnumConstructor() throws Exception {
        performTest("test/Test.java",
                    "package test;\n" +
                    "public enum E {\n" +
                    "    A,\n" +
                    "    B(1);\n" +
                    "    E() {}\n" +
                    "    E(int i) { System.err.println(s); }\n" +
                    "    E(String s) { System.err.println(s); }\n" +
                    "}\n",
                    "7:<init>:NOT_USED");
    }

    @Test
    public void testConstructor() throws Exception {
        performTest("test/Test.java",
                    "package test;\n" +
                    "public class Test {\n" +
                    "    private Test() {}\n" +
                    "    public static Test test() { return new Test(); }\n" +
                    "}\n");
    }

    @Test
    public void testCaught() throws Exception {
        //Ignore unread caught exceptions. There are valid reasons why it
        //could be unused; and there should be a separate hint checking if
        //it makes sense to no use it:
        performTest("test/Test.java",
                    "package test;\n" +
                    "public class Test {\n" +
                    "    public static Test test() {\n" +
                    "        try {" +
                    "            test();\n" +
                    "        } catch (RuntimeException ex) {\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n");
    }

    @Test
    public void testPrivateConstructorUtilityClass1() throws Exception {
        performTest("test/Test.java",
                    "package test;\n" +
                    "public class Test {\n" +
                    "    private Test() {}\n" +
                    "    public static void test() {\n" +
                    "    }\n" +
                    "}\n");
    }

    @Test
    public void testPrivateConstructorNotUtilityClass2() throws Exception {
        performTest("test/Test.java",
                    "package test;\n" +
                    "public class Test {\n" +
                    "    private Test() {}\n" +
                    "    public Test(int i) {}\n" +
                    "    }\n" +
                    "}\n",
                    "3:<init>:NOT_USED");
    }

    @Test
    public void testPrivateConstructorNotUtilityClass3() throws Exception {
        performTest("test/Test.java",
                    "package test;\n" +
                    "public abstract class Test {\n" +
                    "    private Test() {}\n" +
                    "    }\n" +
                    "}\n",
                    "3:<init>:NOT_USED");
    }

    @Test
    public void testPrivateConstructorNotUtilityClass4() throws Exception {
        performTest("test/Test.java",
                    "package test;\n" +
                    "public class Test extends Exception {\n" +
                    "    private Test() {}\n" +
                    "    }\n" +
                    "}\n",
                    "3:<init>:NOT_USED");
    }

    @Test
    public void testPrivateConstructorNotUtilityClass5() throws Exception {
        performTest("test/Test.java",
                    "package test;\n" +
                    "public class Test implements I {\n" +
                    "    private Test() {}\n" +
                    "    }\n" +
                    "}\n" +
                    "interface I {}\n" +
                    "}\n",
                    "3:<init>:NOT_USED");
    }

    @Test
    public void testNoUnusedWhenLookup() throws Exception {
        performTest("test/Test.java",
                    "package test;\n" +
                    "import java.lang.invoke.MethodHandles.Lookup;\n" +
                    "public class Test implements I {\n" +
                    "    public void lookup(Lookup l) {\n" +
                    "        l.findConstructor(T1.class, null);\n" +
                    "        l.findSpecial(Test.class, \"test1\", null, null);\n" +
                    "        l.findStatic(Test.class, \"test3\", null);\n" +
                    "        l.findVirtual(Test.class, \"test5\", null);\n" +
                    "        l.findStaticGetter(Test.class, \"f1\", null);\n" +
                    "        l.findStaticSetter(Test.class, \"f2\", null);\n" +
                    "        l.findStaticVarHandle(Test.class, \"f3\", null);\n" +
                    "        l.findGetter(Test.class, \"f5\", null);\n" +
                    "        l.findSetter(Test.class, \"f6\", null);\n" +
                    "        l.findVarHandle(Test.class, \"f7\", null);\n" +
                    "    }\n" +
                    "    private static void test1() {\n" +
                    "    }\n" +
                    "    private static void test2() {\n" +
                    "    }\n" +
                    "    private static void test3() {\n" +
                    "    }\n" +
                    "    private static void test4() {\n" +
                    "    }\n" +
                    "    private void test5() {\n" +
                    "    }\n" +
                    "    private void test6() {\n" +
                    "    }\n" +
                    "    public class T1 {\n" +
                    "        private T1(int i) { System.err.println(i); }\n" +
                    "    }\n" +
                    "    public class T2 {\n" +
                    "        private T2(int i) { System.err.println(i); }\n" +
                    "    }\n" +
                    "    private static int f1;\n" +
                    "    private static int f2;\n" +
                    "    private static int f3;\n" +
                    "    private static int f4;\n" +
                    "    private int f5;\n" +
                    "    private int f6;\n" +
                    "    private int f7;\n" +
                    "    private int f8;\n" +
                    "}\n" +
                    "interface I {}\n" +
                    "}\n",
                    "18:test2:NOT_USED",
                    "22:test4:NOT_USED",
                    "26:test6:NOT_USED",
                    "32:<init>:NOT_USED",
                    "37:f4:NOT_READ",
                    "41:f8:NOT_READ");
    }

    @Test
    public void testNoUnusedWhenLookupNoLiterals() throws Exception {
        performTest("test/Test.java",
                    "package test;\n" +
                    "import java.lang.invoke.MethodHandles.Lookup;\n" +
                    "public class Test implements I {\n" +
                    "    public void lookup(Lookup l, String name) {\n" +
                    "        doLookup(l, \"test1\");\n" +
                    "        doLookup(l, \"test3\");\n" +
                    "        doLookup(l, \"test5\");\n" +
                    "        doLookup(l, \"f1\");\n" +
                    "        doLookup(l, \"f2\");\n" +
                    "        doLookup(l, \"f3\");\n" +
                    "        doLookup(l, \"f5\");\n" +
                    "        doLookup(l, \"f6\");\n" +
                    "        doLookup(l, \"f7\");\n" +
                    "    }\n" +
                    "    public void doLookup(Lookup l, String name) {\n" +
                    "        l.findSpecial(Test.class, name, null, null);\n" +
                    "        l.findStatic(Test.class, name, null);\n" +
                    "        l.findVirtual(Test.class, name, null);\n" +
                    "        l.findStaticGetter(Test.class, name, null);\n" +
                    "        l.findStaticSetter(Test.class, name, null);\n" +
                    "        l.findStaticVarHandle(Test.class, name, null);\n" +
                    "        l.findGetter(Test.class, name, null);\n" +
                    "        l.findSetter(Test.class, name, null);\n" +
                    "        l.findVarHandle(Test.class, name, null);\n" +
                    "    }\n" +
                    "    private static void test1() {\n" +
                    "    }\n" +
                    "    private static void test2() {\n" +
                    "    }\n" +
                    "    private static void test3() {\n" +
                    "    }\n" +
                    "    private static void test4() {\n" +
                    "    }\n" +
                    "    private void test5() {\n" +
                    "    }\n" +
                    "    private void test6() {\n" +
                    "    }\n" +
                    "    private static int f1;\n" +
                    "    private static int f2;\n" +
                    "    private static int f3;\n" +
                    "    private static int f4;\n" +
                    "    private int f5;\n" +
                    "    private int f6;\n" +
                    "    private int f7;\n" +
                    "    private int f8;\n" +
                    "}\n" +
                    "interface I {}\n" +
                    "}\n",
                    "28:test2:NOT_USED",
                    "32:test4:NOT_USED",
                    "36:test6:NOT_USED",
                    "41:f4:NOT_READ",
                    "45:f8:NOT_READ");
    }

    @Test
    public void testNoUnusedWhenLookupNoClass() throws Exception {
        performTest("test/Test.java",
                    "package test;\n" +
                    "import java.lang.invoke.MethodHandles.Lookup;\n" +
                    "public class Test implements I {\n" +
                    "    public void lookup(Lookup l, Class<?> c) {\n" +
                    "        l.findConstructor(c, null);\n" +
                    "        l.findSpecial(c, \"test1\", null, null);\n" +
                    "        l.findStatic(c, \"test3\", null);\n" +
                    "        l.findVirtual(c, \"test5\", null);\n" +
                    "        l.findStaticGetter(c, \"f1\", null);\n" +
                    "        l.findStaticSetter(c, \"f2\", null);\n" +
                    "        l.findStaticVarHandle(c, \"f3\", null);\n" +
                    "        l.findGetter(c, \"f5\", null);\n" +
                    "        l.findSetter(c, \"f6\", null);\n" +
                    "        l.findVarHandle(c, \"f7\", null);\n" +
                    "    }\n" +
                    "    private static void test1() {\n" +
                    "    }\n" +
                    "    private static void test2() {\n" +
                    "    }\n" +
                    "    private static void test3() {\n" +
                    "    }\n" +
                    "    private static void test4() {\n" +
                    "    }\n" +
                    "    private void test5() {\n" +
                    "    }\n" +
                    "    private void test6() {\n" +
                    "    }\n" +
                    "    public class T1 {\n" +
                    "        private T1(int i) { System.err.println(i); }\n" +
                    "    }\n" +
                    "    public class T2 {\n" +
                    "        private T2(int i) { System.err.println(i); }\n" +
                    "    }\n" +
                    "    private static int f1;\n" +
                    "    private static int f2;\n" +
                    "    private static int f3;\n" +
                    "    private static int f4;\n" +
                    "    private int f5;\n" +
                    "    private int f6;\n" +
                    "    private int f7;\n" +
                    "    private int f8;\n" +
                    "}\n" +
                    "interface I {}\n" +
                    "}\n",
                    "18:test2:NOT_USED",
                    "22:test4:NOT_USED",
                    "26:test6:NOT_USED",
                    "37:f4:NOT_READ",
                    "41:f8:NOT_READ");
    }

    @Test
    public void testUnusedWhenNoLookup() throws Exception {
        performTest("test/Test.java",
                    "package test;\n" +
                    "public class Test implements I {\n" +
                    "    public void lookup() {\n" +
                    "        String[] s = new String[] {\n" +
                    "            \"test1\",\n" +
                    "            \"test3\",\n" +
                    "            \"test5\",\n" +
                    "            \"f1\",\n" +
                    "            \"f5\",\n" +
                    "        };\n" +
                    "    }\n" +
                    "    private static void test1() {\n" +
                    "    }\n" +
                    "    private static void test3() {\n" +
                    "    }\n" +
                    "    private void test5() {\n" +
                    "    }\n" +
                    "    public class T1 {\n" +
                    "        private T1(int i) { System.err.println(i); }\n" +
                    "    }\n" +
                    "    private static int f1;\n" +
                    "    private int f5;\n" +
                    "}\n" +
                    "interface I {}\n" +
                    "}\n",
                    "4:s:NOT_READ",
                    "12:test1:NOT_USED",
                    "14:test3:NOT_USED",
                    "16:test5:NOT_USED",
                    "19:<init>:NOT_USED",
                    "21:f1:NOT_READ",
                    "22:f5:NOT_READ");
    }

    @Test
    public void testUnusedWhenDifferentClass() throws Exception {
        performTest("test/Test.java",
                    "package test;\n" +
                    "import java.lang.invoke.MethodHandles.Lookup;\n" +
                    "public class Test implements I {\n" +
                    "    public void lookup(Lookup l) {\n" +
                    "        l.findStatic(T1.class, \"test1\", null);\n" +
                    "    }\n" +
                    "    private static void test1() {\n" +
                    "    }\n" +
                    "    public class T1 {\n" +
                    "        private static void test1() {\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n" +
                    "interface I {}\n" +
                    "}\n",
                    "7:test1:NOT_USED");
    }


    @Test
    public void testNoUnusedForMainMethod() throws Exception {
        sourceLevel = "21";
        options = Arrays.asList("--enable-preview");
        performTest("Test.java",
                    "package test;\n" +
                    "public class Test {\n" +
                    "    void main() {\n" +
                    "    }\n" +
                    "}\n");
    }

    protected String sourceLevel = "1.8";
    protected List<String> options = null;

    protected void performTest(String fileName, String code, String... expected) throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {}, new Object[] {new TestBase.MIMEResolverImpl()});

	FileObject scratch = SourceUtilsTestUtil.makeScratchDir(this);
	FileObject cache   = scratch.createFolder("cache");

        File wd         = getWorkDir();
        File srcDir     = new File(wd, "src");
        File testSource = new File(srcDir, fileName + ".java");

        testSource.getParentFile().mkdirs();
        TestUtilities.copyStringToFile(testSource, code);

        FileObject testSourceFO = FileUtil.toFileObject(testSource);

        assertNotNull(testSourceFO);

        if (sourceLevel != null) {
            SourceUtilsTestUtil.setSourceLevel(testSourceFO, sourceLevel);
        }

        if (options != null) {
            FileObject srcDirFO = FileUtil.toFileObject(srcDir);

            assertNotNull(srcDirFO);
            SourceUtilsTestUtil.setCompilerOptions(srcDirFO, options);
        }

        File testBuildTo = new File(wd, "test-build");

        testBuildTo.mkdirs();

        FileObject srcRoot = FileUtil.toFileObject(testSource.getParentFile());
        SourceUtilsTestUtil.prepareTest(srcRoot,FileUtil.toFileObject(testBuildTo), cache);

        final Document doc = getDocument(testSourceFO);

        JavaSource source = JavaSource.forFileObject(testSourceFO);

        assertNotNull(source);

	final CountDownLatch l = new CountDownLatch(1);

        source.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController parameter) {
                try {
                    parameter.toPhase(JavaSource.Phase.UP_TO_DATE);

                    Set<String> result = UnusedDetector.findUnused(parameter, () -> false)
                                                       .stream()
                                                       .map(ud -> parameter.getCompilationUnit().getLineMap().getLineNumber(parameter.getTrees().getSourcePositions().getStartPosition(ud.unusedElementPath().getCompilationUnit(), ud.unusedElementPath().getLeaf())) + ":" + ud.unusedElement().getSimpleName() + ":" + ud.reason().name())
                                                       .collect(Collectors.toSet());
                    assertEquals(new HashSet<>(Arrays.asList(expected)), result);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
		    l.countDown();
		}
            }
        }, true);

        l.await();
    }

    private final Document getDocument(FileObject file) throws IOException {
        DataObject od = DataObject.find(file);
        EditorCookie ec = (EditorCookie) od.getCookie(EditorCookie.class);

        if (ec != null) {
            Document doc = ec.openDocument();

            doc.putProperty(Language.class, JavaTokenId.language());
            doc.putProperty("mimeType", "text/x-java");

            return doc;
        } else {
            return null;
        }
    }
}
