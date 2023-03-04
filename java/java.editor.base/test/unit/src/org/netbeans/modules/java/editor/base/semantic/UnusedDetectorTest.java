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
package org.netbeans.modules.java.editor.base.semantic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import javax.lang.model.SourceVersion;
import javax.swing.text.Document;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
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

    protected String sourceLevel = "1.8";

    protected void performTest(String fileName, String code, String... expected) throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {}, new Object[] {new TestBase.MIMEResolverImpl()});

	FileObject scratch = SourceUtilsTestUtil.makeScratchDir(this);
	FileObject cache   = scratch.createFolder("cache");

        File wd         = getWorkDir();
        File testSource = new File(wd, "test/" + fileName + ".java");

        testSource.getParentFile().mkdirs();
        TestUtilities.copyStringToFile(testSource, code);

        FileObject testSourceFO = FileUtil.toFileObject(testSource);

        assertNotNull(testSourceFO);

        if (sourceLevel != null) {
            SourceUtilsTestUtil.setSourceLevel(testSourceFO, sourceLevel);
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
                                                       .map(ud -> parameter.getCompilationUnit().getLineMap().getLineNumber(parameter.getTrees().getSourcePositions().getStartPosition(ud.unusedElementPath.getCompilationUnit(), ud.unusedElementPath.getLeaf())) + ":" + ud.unusedElement.getSimpleName() + ":" + ud.reason.name())
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
