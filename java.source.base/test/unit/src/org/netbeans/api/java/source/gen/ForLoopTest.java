/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;
import java.io.*;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;

/**
 * For Loop generator tests.
 * 
 * @author Pavel Flaska
 */
public class ForLoopTest extends GeneratorTestMDRCompat {

    public ForLoopTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(ForLoopTest.class);
//        suite.addTest(new ForLoopTest("test117774_1"));
//        suite.addTest(new ForLoopTest("test117774_2"));
//        suite.addTest(new ForLoopTest("test117774_3"));
//        suite.addTest(new ForLoopTest("testDoWhileBlockReplacement"));
//        suite.addTest(new ForLoopTest("testRenameInInfiniteFor"));
//        suite.addTest(new ForLoopTest("testReplaceStmtWithBlock1"));
//        suite.addTest(new ForLoopTest("testReplaceStmtWithBlock2"));
//        suite.addTest(new ForLoopTest("test120270"));
//        suite.addTest(new ForLoopTest("testForEachLoop160488"));
//        suite.addTest(new ForLoopTest("testAddInitializer175866"));
//        suite.addTest(new ForLoopTest("testInitializerVariable185746"));
        return suite;
    }

    public void testReplaceStmtWithBlock1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public class Test<E> {\n" +
            "    public void taragui() {\n" +
            "        for (int i = 0; i < 10; i++)\n" +
            "            System.err.println(\"taragui() method\");\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public class Test<E> {\n" +
            "    public void taragui() {\n" +
            "        for (int i = 0; i < 10; i++) {\n" +
            "            System.err.println(\"taragui() method\");\n" + 
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ForLoopTree flt = (ForLoopTree) method.getBody().getStatements().get(0);
                StatementTree mst = flt.getStatement();
                BlockTree block = make.Block(Collections.<StatementTree>singletonList(mst), false);
                workingCopy.rewrite(mst, block);
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testReplaceStmtWithBlock2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public class Test<E> {\n" +
            "    public void taragui() {\n" +
            "        while (true)\n" +
            "            System.err.println(\"taragui() method\");\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public class Test<E> {\n" +
            "    public void taragui() {\n" +
            "        while (true) {\n" +
            "            System.err.println(\"taragui() method\");\n" + 
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                WhileLoopTree flt = (WhileLoopTree) method.getBody().getStatements().get(0);
                StatementTree mst = flt.getStatement();
                BlockTree block = make.Block(Collections.<StatementTree>singletonList(mst), false);
                workingCopy.rewrite(mst, block);
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRenameInInfiniteFor() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    public int a = 10;\n" +
            "    \n" +
            "    public void main(String[] args) {\n" +
            "        for (;;) {\n" +
            "            a = 12;\n" +
            "        }\n" +
            "    }\n" +
            "}\n" +
            "\n");
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    public int asdf = 10;\n" +
            "    \n" +
            "    public void main(String[] args) {\n" +
            "        for (;;) {\n" +
            "            asdf = 12;\n" +
            "        }\n" +
            "    }\n" +
            "}\n" +
            "\n";
        JavaSource src = getJavaSource(testFile);
        
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);

                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(var, make.setLabel(var, "asdf"));
                
                MethodTree method = (MethodTree) clazz.getMembers().get(2);
                ForLoopTree flt = (ForLoopTree) method.getBody().getStatements().get(0);
                BlockTree block = (BlockTree) flt.getStatement();
                AssignmentTree assign = (AssignmentTree) ((ExpressionStatementTree) block.getStatements().get(0)).getExpression();
                ExpressionTree et = assign.getVariable();
                workingCopy.rewrite(et, make.setLabel(et, "asdf"));
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testDoWhileBlockReplacement() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    public int a = 10;\n" +
            "    \n" +
            "    public void main(String[] args) {\n" +
            "        do\n" +
            "            a = 12;\n" +
            "        while (a == 10);\n" +
            "    }\n" +
            "}\n" +
            "\n");
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    public int asdf = 10;\n" +
            "    \n" +
            "    public void main(String[] args) {\n" +
            "        do {\n" +
            "            a = 12;\n" +
            "        } while (a == 10);\n" +
            "    }\n" +
            "}\n" +
            "\n";
        JavaSource src = getJavaSource(testFile);
        
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);

                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(var, make.setLabel(var, "asdf"));
                
                MethodTree method = (MethodTree) clazz.getMembers().get(2);
                DoWhileLoopTree flt = (DoWhileLoopTree) method.getBody().getStatements().get(0);
                StatementTree statement = flt.getStatement();
                BlockTree block = make.Block(Collections.<StatementTree>singletonList(statement), false);
                workingCopy.rewrite(statement, block);
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Regression test for 117774.
     * while statement test.
     * 
     * @throws java.lang.Exception
     */
    public void test117774_1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    public void actionPerformed(ActionEvent e) {\n" +
            "        while (true)\n" +
            "            System.getProperties();\n" +
            "    }\n" +
            "}\n" +
            "\n");
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    public void actionPerformed(ActionEvent e) {\n" +
            "        while (true)\n" +
            "            Properties properties = System.getProperties();\n" +
            "    }\n" +
            "}\n" +
            "\n";
        JavaSource src = getJavaSource(testFile);

        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                List<? extends StatementTree> stmts = method.getBody().getStatements();
                WhileLoopTree wlt = (WhileLoopTree) stmts.get(0);
                ExpressionStatementTree statement = (ExpressionStatementTree) wlt.getStatement();
                VariableTree var = make.Variable(
                        make.Modifiers(Collections.<Modifier>emptySet()), 
                        "properties",
                        make.Identifier("Properties"),
                        statement.getExpression()
                );
                workingCopy.rewrite(statement, var);
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Regression test for 117774.
     * for statement test.
     * 
     * @throws java.lang.Exception
     */
    public void test117774_2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    public void actionPerformed(ActionEvent e) {\n" +
            "        for (int i = 0; i < 10; i++)\n" +
            "            System.getProperties();\n" +
            "    }\n" +
            "}\n" +
            "\n");
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    public void actionPerformed(ActionEvent e) {\n" +
            "        for (int i = 0; i < 10; i++)\n" +
            "            Properties properties = System.getProperties();\n" +
            "    }\n" +
            "}\n" +
            "\n";
        JavaSource src = getJavaSource(testFile);

        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                List<? extends StatementTree> stmts = method.getBody().getStatements();
                ForLoopTree foor = (ForLoopTree) stmts.get(0);
                ExpressionStatementTree statement = (ExpressionStatementTree) foor.getStatement();
                VariableTree var = make.Variable(
                        make.Modifiers(Collections.<Modifier>emptySet()), 
                        "properties",
                        make.Identifier("Properties"),
                        statement.getExpression()
                );
                workingCopy.rewrite(statement, var);
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Regression test for 117774.
     * while statement test no whitespace.
     * 
     * @throws java.lang.Exception
     */
    public void test117774_3() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    public void actionPerformed(ActionEvent e) {\n" +
            "        while (true)System.getProperties();\n" +
            "    }\n" +
            "}\n" +
            "\n");
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    public void actionPerformed(ActionEvent e) {\n" +
            "        while (true)Properties properties = System.getProperties();\n" +
            "    }\n" +
            "}\n" +
            "\n";
        JavaSource src = getJavaSource(testFile);

        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                List<? extends StatementTree> stmts = method.getBody().getStatements();
                WhileLoopTree wlt = (WhileLoopTree) stmts.get(0);
                ExpressionStatementTree statement = (ExpressionStatementTree) wlt.getStatement();
                VariableTree var = make.Variable(
                        make.Modifiers(Collections.<Modifier>emptySet()), 
                        "properties",
                        make.Identifier("Properties"),
                        statement.getExpression()
                );
                workingCopy.rewrite(statement, var);
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test120270() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    public void actionPerformed(ActionEvent e) {\n" +
            "        for (int i; i < 10; i++)\n" +
            "            Properties properties = System.getProperties();\n" +
            "    }\n" +
            "}\n" +
            "\n");
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    public void actionPerformed(ActionEvent e) {\n" +
            "        for (int i = 0; i < 10; i++)\n" +
            "            Properties properties = System.getProperties();\n" +
            "    }\n" +
            "}\n" +
            "\n";
        JavaSource src = getJavaSource(testFile);

        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                List<? extends StatementTree> stmts = method.getBody().getStatements();
                ForLoopTree foor = (ForLoopTree) stmts.get(0);
                VariableTree vt = (VariableTree) foor.getInitializer().get(0);
                VariableTree newVt = make.Variable(
                        vt.getModifiers(),
                        vt.getName(),
                        vt.getType(),
                        make.Literal(0)
                );
                workingCopy.rewrite(vt, newVt);
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testForEachLoop160488() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication1;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    public void actionPerformed(ActionEvent e) {\n" +
            "        for (ttt : java.util.Collections.emptyList()) {}\n" +
            "    }\n" +
            "}\n" +
            "\n");
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    public void actionPerformed(ActionEvent e) {\n" +
            "        for (Object ttt : java.util.Collections.emptyList()) {}\n" +
            "    }\n" +
            "}\n" +
            "\n";
        JavaSource src = getJavaSource(testFile);

        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                List<? extends StatementTree> stmts = method.getBody().getStatements();
                EnhancedForLoopTree foor = (EnhancedForLoopTree) stmts.get(0);
                VariableTree vt = foor.getVariable();
                VariableTree newVt = make.Variable(
                        vt.getModifiers(),
                        "ttt",
                        make.QualIdent(workingCopy.getElements().getTypeElement("java.lang.Object")),
                        null
                );
                workingCopy.rewrite(vt, newVt);
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddInitializer175866() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (; j<1; j++);\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (int j = 0; j<1; j++);\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                ForLoopTree flt = (ForLoopTree) block.getStatements().get(0);
                VariableTree init = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)),
                                                  "j",
                                                  make.Type(workingCopy.getTypes().getPrimitiveType(TypeKind.INT)),
                                                  make.Literal(0));
                workingCopy.rewrite(flt, make.addForLoopInitializer(flt, init));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveInitializer175866() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (int j=0; j<1; j++);\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (; j<1; j++);\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                ForLoopTree flt = (ForLoopTree) block.getStatements().get(0);
                workingCopy.rewrite(flt, make.removeForLoopInitializer(flt, 0));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testInitializerAssignmentToVariable175866() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (j = 0; j<1; j++);\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (int j = 0; j<1; j++);\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                ForLoopTree flt = (ForLoopTree) block.getStatements().get(0);
                VariableTree init = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)),
                                                  "j",
                                                  make.Type(workingCopy.getTypes().getPrimitiveType(TypeKind.INT)),
                                                  make.Literal(0));
                workingCopy.rewrite(flt.getInitializer().get(0), init);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testInitializerVariableToAssignment175866() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (int j = 0; j<1; j++);\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (j = 0; j<1; j++);\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                ForLoopTree flt = (ForLoopTree) block.getStatements().get(0);
                VariableTree vt = (VariableTree) flt.getInitializer().get(0);
                AssignmentTree initAssign = make.Assignment(make.Identifier("j"), vt.getInitializer());
                ExpressionStatementTree est = make.ExpressionStatement(initAssign);
                workingCopy.rewrite(vt, est);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }


    public void testInitializerVariable185746() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (int p = 0, i; ; );\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (int p = 0, hh; ; );\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new TreeScanner<Void, Void>() {
                    @Override
                    public Void visitVariable(VariableTree node, Void p) {
                        if (node.getName().contentEquals("i"))
                            workingCopy.rewrite(node, make.setLabel(node, "hh"));
                        return super.visitVariable(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

}
