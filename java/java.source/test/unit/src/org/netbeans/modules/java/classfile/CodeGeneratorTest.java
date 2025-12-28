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

package org.netbeans.modules.java.classfile;

import com.sun.source.tree.CompilationUnitTree;
import java.io.IOException;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.source.ClassIndexTestCase;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class CodeGeneratorTest extends ClassIndexTestCase {

    private String sourceLevel;

    public CodeGeneratorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
    }

    public void testSimple1() throws Exception {
        performTest("package test; class Test { }",
                    "package test; class Test { } ");
    }

    //XXX: investigate this test!
    public void DISABLEDtestAnnotationDeclaration() throws Exception {
        performTest("package test; import test.Test.A; import test.Test.E; import java.lang.annotation.Retention; import java.lang.annotation.RetentionPolicy; class Test {\n" +
                    "@Retention(RetentionPolicy.CLASS)\n" +
                    "@interface TT {\n" +
                    "    public Class test1() default Object.class;\n" +
                    "    public Class test2();\n" +
                    "    public E     test3() default E.A;\n" +
                    "    public E     test4();\n" +
                    "    public A     test5() default @A(attr1=\"a\");\n" +
                    "    public A[]   test6() default {@A(attr1=\"a\", attr2=\"b\")};\n" +
                    "}\n" +
                    "enum E {\n" +
                    "    A, B;\n" +
                    "}\n" +
                    "@interface A {\n" +
                    "    public String attr1();\n" +
                    "    public String attr2() default \"\";\n" +
                    "}\n" +
                    "}\n",
                    "package test; import java.lang.annotation.Retention; import java.lang.annotation.RetentionPolicy; import test.Test.A; import test.Test.E; class Test {\n" +
                    "@Retention(value = RetentionPolicy.CLASS)\n" +
                    "@interface TT {\n" +
                    "    public Class test1() default Object.class;\n" +
                    "    public Class test2();\n" +
                    "    public E     test3() default E.A;\n" +
                    "    public E     test4();\n" +
                    "    public A     test5() default @A(attr1 = \"a\");\n" +
                    "    public A[]   test6() default {@A(attr1 = \"a\", attr2 = \"b\")};\n" +
                    "}\n" +
                    "enum E {\n" +
                    "    A, B\n" +
                    "}\n" +
                    "@interface A {\n" +
                    "    public String attr1();\n" +
                    "    public String attr2() default \"\";\n" +
                    "}\n" +
                    "}\n");
    }

    public void testInterface() throws Exception {
        performTest("package test; interface Test {\n" +
                    "    public Class test1();\n" +
                    "}\n",
                    "package test; interface Test {\n" +
                    "    public Class test1();\n" +
                    "}\n");
    }

    //injection of the deprecated javadoc annotations is disabled:
    public void DISABLEDtestDeprecated1() throws Exception {
        performTest("package test; interface Test {\n" +
                    "    /**@deprecated*/ public Class test1();\n" +
                    "}\n",
                    "package test; interface Test {\n" +
                    "    /** * @deprecated */ public Class test1();\n" +
                    "}\n");
    }

    public void testDeprecated2() throws Exception {
        performTest("package test; interface Test {\n" +
                    "    @Deprecated public Class test1();\n" +
                    "}\n",
                    "package test; interface Test {\n" +
                    "    @Deprecated public Class test1();\n" +
                    "}\n");
    }
    
    public void testDecompile1() throws Exception {
        performFromClassTest("package test; class Test {\n" +
                             "    private void test() {\n" +
                             "        System.out.println(100000);\n" +
                             "    }\n" +
                             "}\n",
                             "package test;",
                             "class Test {",
                             "Test() {",
                             "// <editor-fold defaultstate=\"collapsed\" desc=\"Compiled Code\">",
                             "aload_0",
                             "return",
                             "// </editor-fold>",
                             "}",
                             "private void test() {",
                             "// <editor-fold defaultstate=\"collapsed\" desc=\"Compiled Code\">",
                             "getstatic", "java/lang/System.out:Ljava/io/PrintStream;",
                             "ldc", "100000",
                             "invokevirtual", "java/io/PrintStream.println:(I)V",
                             "return",
                             "</editor-fold>",
                             "}",
                             "}");
    }

    public void testPermittedSubclasses() throws Exception {
        sourceLevel = "17";
        performTest("""
                    package test;
                    public sealed interface Test {
                        public static final class Impl implements Test {}
                    }
                    """,
                    """
                    package test;
                    public sealed interface Test {
                        public static final class Impl implements Test {
                        }
                    }
                    """);
    }

    private void performTest(String test, final String golden) throws Exception {
        clearWorkDir();
        beginTx();
        FileObject wd = FileUtil.toFileObject(getWorkDir());

        assertNotNull(wd);

        FileObject src   = FileUtil.createFolder(wd, "src");
        FileObject build = FileUtil.createFolder(wd, "build");
        FileObject cache = FileUtil.createFolder(wd, "cache");

        SourceUtilsTestUtil.prepareTest(src, build, cache);
        FileObject testFile = FileUtil.createData(src, "test/Test.java");
        final FileObject testOutFile = FileUtil.createData(src, "out/Test.java");
        TestUtilities.copyStringToFile(testFile, test);

        if (sourceLevel != null) {
            SourceUtilsTestUtil.setSourceLevel(testFile, sourceLevel);
        }

        final ClasspathInfo cpInfo = ClasspathInfoAccessor.getINSTANCE().create(testOutFile, null, true, true, false, true);
        JavaSource testSource = JavaSource.create(cpInfo, testOutFile);
        final String[] betterName = new String[1];
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);

                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TypeElement t = workingCopy.getElements().getTypeElement("test.Test");

                assertNotNull(t);

                workingCopy.rewrite(workingCopy.getCompilationUnit(), CodeGenerator.generateCode(workingCopy, t, betterName));
            }
        };

        ModificationResult mr = testSource.runModificationTask(task);

        mr.commit();

        assertEquals(normalizeWhitespaces(golden), normalizeWhitespaces(TestUtilities.copyFileToString(FileUtil.toFile(testOutFile))));
        assertNull("No better name suggested for source", betterName[0]);
        testSource.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController cc) throws Exception {
                cc.toPhase(Phase.RESOLVED);
                assertTrue(cc.getDiagnostics().toString(), cc.getDiagnostics().isEmpty());
            }
        }, true);
    }

    private void performFromClassTest(String test, final String... lines) throws Exception {
        clearWorkDir();
        beginTx();
        FileObject wd = FileUtil.toFileObject(getWorkDir());

        assertNotNull(wd);

        FileObject src   = FileUtil.createFolder(wd, "src");
        FileObject build = FileUtil.createFolder(wd, "build");
        FileObject cache = FileUtil.createFolder(wd, "cache");

        SourceUtilsTestUtil.prepareTest(src, build, cache);
        FileObject testFile = FileUtil.createData(src, "test/TestSourceToCompile.java");
        TestUtilities.copyStringToFile(testFile, test);
        SourceUtilsTestUtil.compileRecursively(src);
        final FileObject testOutFile = FileUtil.createData(src, "out/Test.java");
        final ClasspathInfo cpInfo = ClasspathInfoAccessor.getINSTANCE().create(testOutFile, null, true, true, false, true);
        JavaSource testSource = JavaSource.create(cpInfo, testOutFile);
        final String[] betterName = new String[1];
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);

                TypeElement t = workingCopy.getElements().getTypeElement("test.Test");

                assertNotNull(t);

                workingCopy.rewrite(workingCopy.getCompilationUnit(), CodeGenerator.generateCode(workingCopy, t, betterName));
            }
        };

        ModificationResult mr = testSource.runModificationTask(task);

        mr.commit();

        final String generatedText = normalizeWhitespaces(TestUtilities.copyFileToString(FileUtil.toFile(testOutFile)));
        int at = 0;
        for (String expLine : lines) {
            int found = generatedText.indexOf(expLine, at);
            if (found == -1) {
                fail("Expecting: " + expLine + ", but found:\n" + generatedText.substring(at));
            }
            at = found;
        }
        assertEquals(testFile.getNameExt(), betterName[0]);
    }

    private static String normalizeWhitespaces(String text) {
        return text.replaceAll("[ \n\t]+", " ");
    }

}
