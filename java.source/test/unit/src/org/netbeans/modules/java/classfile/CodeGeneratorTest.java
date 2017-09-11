/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008-2010 Sun Microsystems, Inc.
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

    public void testAnnotationDeclaration() throws Exception {
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

    public void testDeprecated1() throws Exception {
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
                             "package test;\n" +
                             "class Test {\n" +
                             "    Test() {\n" +
                             "        // <editor-fold defaultstate=\"collapsed\" desc=\"Compiled Code\">\n" +
                             "        /* 0: aload_0\n" +
                             "         * 1: invokespecial java/lang/Object.\"<init>\":()V\n" +
                             "         * 4: return\n" +
                             "         *  */\n" +
                             "        // </editor-fold>\n" +
                             "    }\n" +
                             "    private void test() {\n" +
                             "        // <editor-fold defaultstate=\"collapsed\" desc=\"Compiled Code\">\n" +
                             "        /* 0: getstatic     java/lang/System.out:Ljava/io/PrintStream;\n" +
                             "         * 3: ldc           100000\n" +
                             "         * 5: invokevirtual java/io/PrintStream.println:(I)V\n" +
                             "         * 8: return\n" +
                             "         *  */\n" +
                             "        // </editor-fold>\n" +
                             "    }\n" +
                             "}\n");
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
        final ClasspathInfo cpInfo = ClasspathInfoAccessor.getINSTANCE().create(testOutFile, null, true, true, false, true);
        JavaSource testSource = JavaSource.create(cpInfo, testOutFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);

                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TypeElement t = workingCopy.getElements().getTypeElement("test.Test");

                assertNotNull(t);

                workingCopy.rewrite(workingCopy.getCompilationUnit(), CodeGenerator.generateCode(workingCopy, t));
            }
        };

        ModificationResult mr = testSource.runModificationTask(task);

        mr.commit();

        assertEquals(normalizeWhitespaces(golden), normalizeWhitespaces(TestUtilities.copyFileToString(FileUtil.toFile(testOutFile))));

        testSource.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController cc) throws Exception {
                cc.toPhase(Phase.RESOLVED);
                assertTrue(cc.getDiagnostics().toString(), cc.getDiagnostics().isEmpty());
            }
        }, true);
    }

    private void performFromClassTest(String test, final String golden) throws Exception {
        clearWorkDir();
        beginTx();
        FileObject wd = FileUtil.toFileObject(getWorkDir());

        assertNotNull(wd);

        FileObject src   = FileUtil.createFolder(wd, "src");
        FileObject build = FileUtil.createFolder(wd, "build");
        FileObject cache = FileUtil.createFolder(wd, "cache");

        SourceUtilsTestUtil.prepareTest(src, build, cache);
        FileObject testFile = FileUtil.createData(src, "test/Test.java");
        TestUtilities.copyStringToFile(testFile, test);
        SourceUtilsTestUtil.compileRecursively(src);
        final FileObject testOutFile = FileUtil.createData(src, "out/Test.java");
        final ClasspathInfo cpInfo = ClasspathInfoAccessor.getINSTANCE().create(testOutFile, null, true, true, false, true);
        JavaSource testSource = JavaSource.create(cpInfo, testOutFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);

                TypeElement t = workingCopy.getElements().getTypeElement("test.Test");

                assertNotNull(t);

                workingCopy.rewrite(workingCopy.getCompilationUnit(), CodeGenerator.generateCode(workingCopy, t));
            }
        };

        ModificationResult mr = testSource.runModificationTask(task);

        mr.commit();

        assertEquals(normalizeWhitespaces(golden), normalizeWhitespaces(TestUtilities.copyFileToString(FileUtil.toFile(testOutFile))));
    }

    private static String normalizeWhitespaces(String text) {
        return text.replaceAll("[ \n\t]+", " ");
    }

}
