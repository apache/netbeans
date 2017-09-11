/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.test;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.api.ReplaceConstructorWithBuilderRefactoring;
import org.netbeans.modules.refactoring.java.api.ReplaceConstructorWithBuilderRefactoring.Setter;
import org.netbeans.modules.refactoring.spi.impl.UndoManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */
public class ReplaceConstructorWithBuilderTest extends RefTestBase {

    public ReplaceConstructorWithBuilderTest(String name) {
        super(name);
    }
    
    public void testReplaceWithBuilderImports() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("test/Test.java", "package test;\n import java.util.List;\n public class Test {\n public Test(List<String> i) {}\n private void t() {\n Test t = new Test(null);\n }\n }\n"),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test t = new Test(ll); } }"));

        performTest("test.TestBuilder", new ReplaceConstructorWithBuilderRefactoring.Setter("setI", "java.util.List<java.lang.String>", null, "i", false));

        assertContent(src,
                new File("test/Test.java", "package test;\n import java.util.List;\n public class Test {\n public Test(List<String> i) {}\n private void t() {\n Test t = new TestBuilder().setI(null).createTest();\n }\n }\n"),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test t = new TestBuilder().setI(ll).createTest(); } }"),
                new File("test/TestBuilder.java", "package test; import java.util.List; public class TestBuilder { private List<String> i; public TestBuilder() { } public TestBuilder setI(List<String> i) { this.i = i; return this; } public Test createTest() { return new Test(i); } } "));
    }
    
    public void test231638() throws Exception { // #231638 - [Replace constructor with builder] Refactoring produces non-compilable code when original constructor is private
        writeFilesAndWaitForScan(src,
                new File("test/Test.java", "package test;\n public class Test {\n private Test() {}\n }\n"));

        performTest("test.TestBuilder", new ReplaceConstructorWithBuilderRefactoring.Setter("setI", "T", "\"\"", "i", true), new Problem(true, "ERR_ReplacePrivate"));
    }
    
    public void testReplaceGenericWithBuilder2() throws Exception { // #222303, #227062
        writeFilesAndWaitForScan(src,
                new File("test/Test.java", "package test;\n public class Test<T> {\n public Test(T i) {}\n private void t() {\n Test<String> t = new Test<String>(\"\");\n }\n }\n"),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test<String> t = new Test<String>(\"\"); } }"));

        performTest("test.TestBuilder", new ReplaceConstructorWithBuilderRefactoring.Setter("setI", "T", null, "i", false));

        assertContent(src,
                new File("test/Test.java", "package test; public class Test<T> { public Test(T i) {} private void t() { Test<String> t = new TestBuilder<String>().setI(\"\").createTest(); } } "),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test<String> t = new TestBuilder<String>().setI(\"\").createTest(); } }"),
                new File("test/TestBuilder.java", "package test; public class TestBuilder<T> { private T i; public TestBuilder() { } public TestBuilder<T> setI(T i) { this.i = i; return this; } public Test<T> createTest() { return new Test<T>(i); } } "));
    }

    public void testReplaceWithBuilder() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("test/Test.java", "package test;\n public class Test {\n public Test(int i) {}\n private void t() {\n Test t = new Test(1);\n }\n }\n"),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test t = new Test(-1); } }"));

        performTest("test.TestBuilder", new ReplaceConstructorWithBuilderRefactoring.Setter("setI", "int", null, "i", false));

        assertContent(src,
                new File("test/Test.java", "package test;\n public class Test {\n public Test(int i) {}\n private void t() {\n Test t = new TestBuilder().setI(1).createTest();\n }\n }\n"),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test t = new TestBuilder().setI(-1).createTest(); } }"),
                new File("test/TestBuilder.java", "package test; public class TestBuilder { private int i; public TestBuilder() { } public TestBuilder setI(int i) { this.i = i; return this; } public Test createTest() { return new Test(i); } } "));
    }
    
    public void testReplaceWithBuilderUndo() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("test/Test.java", "package test;\n public class Test {\n public Test(int i) {}\n private void t() {\n Test t = new Test(1);\n }\n }\n"),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test t = new Test(-1); } }"));

        performTest("test.TestBuilder", new ReplaceConstructorWithBuilderRefactoring.Setter("setI", "int", null, "i", false));
        
        assertContent(src,
                new File("test/Test.java", "package test;\n public class Test {\n public Test(int i) {}\n private void t() {\n Test t = new TestBuilder().setI(1).createTest();\n }\n }\n"),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test t = new TestBuilder().setI(-1).createTest(); } }"),
                new File("test/TestBuilder.java", "package test; public class TestBuilder { private int i; public TestBuilder() { } public TestBuilder setI(int i) { this.i = i; return this; } public Test createTest() { return new Test(i); } } "));
        
        UndoManager undoManager = UndoManager.getDefault();
        undoManager.setAutoConfirm(true);
        undoManager.undo(null);

        assertContent(src,
                new File("test/Test.java", "package test;\n public class Test {\n public Test(int i) {}\n private void t() {\n Test t = new Test(1);\n }\n }\n"),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test t = new Test(-1); } }"));
        
        undoManager.redo(null);
        
        assertContent(src,
                new File("test/Test.java", "package test;\n public class Test {\n public Test(int i) {}\n private void t() {\n Test t = new TestBuilder().setI(1).createTest();\n }\n }\n"),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test t = new TestBuilder().setI(-1).createTest(); } }"),
                new File("test/TestBuilder.java", "package test; public class TestBuilder { private int i; public TestBuilder() { } public TestBuilder setI(int i) { this.i = i; return this; } public Test createTest() { return new Test(i); } } "));
    }
    
    public void testReplaceVarargsWithBuilder() throws Exception { // #222305
        writeFilesAndWaitForScan(src,
                new File("test/Test.java", "package test;\n public class Test {\n public Test(int... i) {}\n public Test(String s) {}\n private void t() {\n Test t = new Test(1);\n }\n }\n"),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test t = new Test(-1);\n Test s = new Test(\"\"); } }"));

        performTest("test.TestBuilder", new ReplaceConstructorWithBuilderRefactoring.Setter("setI", "int...", null, "i", false));

        assertContent(src,
                new File("test/Test.java", "package test;\n public class Test {\n public Test(int... i) {}\n public Test(String s) {}\n private void t() {\n Test t = new TestBuilder().setI(1).createTest();\n }\n }\n"),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test t = new TestBuilder().setI(-1).createTest();\n Test s = new Test(\"\"); } }"),
                new File("test/TestBuilder.java", "package test; public class TestBuilder { private int[] i; public TestBuilder() { } public TestBuilder setI(int... i) { this.i = i; return this; } public Test createTest() { return new Test(i); } } "));
    }
    
    public void testReplaceGenericWithBuilder() throws Exception { // #222303, #227062
        writeFilesAndWaitForScan(src,
                new File("test/Test.java", "package test;\n public class Test<T> {\n public Test(int i) {}\n private void t() {\n Test<String> t = new Test<String>(1);\n }\n }\n"),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test<Boolean> t = new Test<Boolean>(-1); } }"));

        performTest("test.TestBuilder", new ReplaceConstructorWithBuilderRefactoring.Setter("setI", "int", null, "i", false));

        assertContent(src,
                new File("test/Test.java", "package test; public class Test<T> { public Test(int i) {} private void t() { Test<String> t = new TestBuilder<String>().setI(1).createTest(); } } "),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test<Boolean> t = new TestBuilder<Boolean>().setI(-1).createTest(); } }"),
                new File("test/TestBuilder.java", "package test; public class TestBuilder<T> { private int i; public TestBuilder() { } public TestBuilder<T> setI(int i) { this.i = i; return this; } public Test<T> createTest() { return new Test<T>(i); } } "));
    }
    
    public void test226866() throws Exception { // #226866
        writeFilesAndWaitForScan(src,
                new File("test/Test.java", "package test;\n public class Test<T> {\n public Test(T i) {}\n private void t() {\n Test<String> t = new Test<String>(\"\");\n }\n }\n"),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test<Boolean> t = new Test<Boolean>(Boolean.FALSE); } }"));

        performTest("test.TestBuilder", new ReplaceConstructorWithBuilderRefactoring.Setter("setI", "T", "\"\"", "i", true), new Problem(true, "ERR_GenericOptional"));
    }
    
    public void test212135() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("test/Test.java", "package test; public class Test { public Test() { } }"),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test t = new Test(); } }"));

        performTest2("test.TestBuilder");

        assertContent(src,
                new File("test/Test.java", "package test; public class Test { public Test() { } }"),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test t = new TestBuilder().createTest(); } }"),
                new File("test/TestBuilder.java", "package test; public class TestBuilder { public TestBuilder() { } public Test createTest() { return new Test(); } } "));
    }
    
    public void test212136a() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("test/Test.java", "package test;\n public class Test {\n public Test(int i) {}\n private void t() {\n Test t = new Test(1);\n }\n }\n"),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test t = new Test(-1); } }"));

        performTest("test.TestBuilder", new ReplaceConstructorWithBuilderRefactoring.Setter("setI", "int", "-1", "i", true));

        assertContent(src,
                new File("test/Test.java", "package test;\n public class Test {\n public Test(int i) {}\n private void t() {\n Test t = new TestBuilder().setI(1).createTest();\n }\n }\n"),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test t = new TestBuilder().createTest(); } }"),
                new File("test/TestBuilder.java", "package test; public class TestBuilder { private int i = -1; public TestBuilder() { } public TestBuilder setI(int i) { this.i = i; return this; } public Test createTest() { return new Test(i); } } "));
    }
    
    public void test212136b() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("test/Test.java", "package test;\n public class Test {\n public Test(int i) {}\n private void t() {\n Test t = new Test(1);\n }\n }\n"),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test t = new Test(-1); } }"));

        performTest("test.TestBuilder", new ReplaceConstructorWithBuilderRefactoring.Setter("setI", "int", "-1", "i", false));

        assertContent(src,
                new File("test/Test.java", "package test;\n public class Test {\n public Test(int i) {}\n private void t() {\n Test t = new TestBuilder().setI(1).createTest();\n }\n }\n"),
                new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test t = new TestBuilder().setI(-1).createTest(); } }"),
                new File("test/TestBuilder.java", "package test; public class TestBuilder { private int i = -1; public TestBuilder() { } public TestBuilder setI(int i) { this.i = i; return this; } public Test createTest() { return new Test(i); } } "));
    }

    private void performTest(final String builderName, final Setter setter, Problem... expectedProblems) throws Exception {
        final ReplaceConstructorWithBuilderRefactoring[] r = new ReplaceConstructorWithBuilderRefactoring[1];
        FileObject testFile = src.getFileObject("test/Test.java");

        JavaSource.forFileObject(testFile).runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = parameter.getCompilationUnit();

                MethodTree var = (MethodTree) ((ClassTree) cut.getTypeDecls().get(0)).getMembers().get(0);

                TreePath tp = TreePath.getPath(cut, var);
                r[0] = new ReplaceConstructorWithBuilderRefactoring(TreePathHandle.create(tp, parameter));
                r[0].setBuilderName(builderName);

                r[0].setSetters(Collections.singletonList(setter));
            }
        }, true);

        RefactoringSession rs = RefactoringSession.create("Session");
        Thread.sleep(1000);
        List<Problem> problems = new LinkedList<Problem>();

        addAllProblems(problems, r[0].preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, rs.doRefactoring(true));
        }
        assertProblems(Arrays.asList(expectedProblems), problems);

        IndexingManager.getDefault().refreshIndex(src.toURL(), null);
        SourceUtils.waitScanFinished();
        //assertEquals(false, TaskCache.getDefault().isInError(src, true));
    }
    
    private void performTest2(final String builderName) throws Exception {
        final ReplaceConstructorWithBuilderRefactoring[] r = new ReplaceConstructorWithBuilderRefactoring[1];
        FileObject testFile = src.getFileObject("test/Test.java");

        JavaSource.forFileObject(testFile).runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = parameter.getCompilationUnit();

                MethodTree var = (MethodTree) ((ClassTree) cut.getTypeDecls().get(0)).getMembers().get(0);

                TreePath tp = TreePath.getPath(cut, var);
                r[0] = new ReplaceConstructorWithBuilderRefactoring(TreePathHandle.create(tp, parameter));
                r[0].setBuilderName(builderName);
            }
        }, true);

        RefactoringSession rs = RefactoringSession.create("Session");
        Thread.sleep(1000);
        r[0].prepare(rs);
        rs.doRefactoring(true);

        IndexingManager.getDefault().refreshIndex(src.toURL(), null);
        SourceUtils.waitScanFinished();
        //assertEquals(false, TaskCache.getDefault().isInError(src, true));
    }
}