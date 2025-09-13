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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.ui.FmtOptions;
import org.openide.filesystems.FileUtil;

/**
 * Tests method type parameters changes.
 * 
 * @author Pavel Flaska
 */
public class MoveTreeTest extends GeneratorTestBase {

    static {
        System.setProperty("org.netbeans.api.java.source.WorkingCopy.keep-old-trees", "true");
    }
    
    /** Creates a new instance of MethodParametersTest */
    public MoveTreeTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(MoveTreeTest.class);
        return suite;
    }

    private Map<String, String> origValues;
    
    private void setCodePreferences(Map<String, String> values) {
        origValues = Utils.setCodePreferences(values);
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        
        if (origValues != null) {
            Utils.setCodePreferences(origValues);
        }
    }
    
    public void testMoveExpression1() throws Exception {
        performMoveExpressionTest(
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int i1 = 1+    2    *3;\n" +
            "        int i2 = 0;\n" +
            "    }\n" +
            "}\n",
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int i1 = 1+    2    *3;\n" +
            "        int i2 = 1+    2    *3;\n" +
            "    }\n" +
            "}\n");
    }

    public void testMoveExpression2() throws Exception {
        setCodePreferences(Utils.MapBuilder.<String, String>create()
                                           .add(FmtOptions.alignMultilineBinaryOp, "true")
                                           .build());
        performMoveExpressionTest(
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int i1 = 1+    \n" +
            "                 2    *3;\n" +
            "        int foo = 0;\n" +
            "    }\n" +
            "}\n",
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int i1 = 1+    \n" +
            "                 2    *3;\n" +
            "        int foo = 1+    \n" +
            "                  2    *3;\n" +
            "    }\n" +
            "}\n");
    }

    public void testMoveExpression3() throws Exception {
        setCodePreferences(Utils.MapBuilder.<String, String>create()
                                           .add(FmtOptions.alignMultilineBinaryOp, "true")
                                           .build());
        performMoveExpressionTest(
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int i1 =\n" +
            "            1+    \n" +
            "            2    *3;\n" +
            "        int foo = 0;\n" +
            "    }\n" +
            "}\n",
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int i1 =\n" +
            "            1+    \n" +
            "            2    *3;\n" +
            "        int foo = 1+    \n" +
            "                  2    *3;\n" +
            "    }\n" +
            "}\n");
    }

    public void testMoveExpression4() throws Exception {
        setCodePreferences(Utils.MapBuilder.<String, String>create()
                                           .add(FmtOptions.alignMultilineBinaryOp, "true")
                                           .build());
        performMoveExpressionTest(
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int i1 =\n" +
            "                        1+    \n" +
            "                         2    *3;\n" +
            "        int foo = 0;\n" +
            "    }\n" +
            "}\n",
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int i1 =\n" +
            "                        1+    \n" +
            "                         2    *3;\n" +
            "        int foo = 1+    \n" +
            "                  2    *3;\n" +
            "    }\n" +
            "}\n");
    }

    private static final Map<String, String> TAB_SIZE_PREFERENCES =
            Utils.MapBuilder.<String, String>create().add(FmtOptions.indentSize, "4")
                                                     .add(FmtOptions.tabSize, "8")
                                                     .add(FmtOptions.alignMultilineBinaryOp, "true")
                                                     .build();

    public void testMoveExpression2Tab() throws Exception {
        setCodePreferences(TAB_SIZE_PREFERENCES);
        performMoveExpressionTest(
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "\tint i1 = 1+    \n" +
            "\t         2    *3;\n" +
            "        int foo = 0;\n" +
            "    }\n" +
            "}\n",
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "\tint i1 = 1+    \n" +
            "\t         2    *3;\n" +
            "        int foo = 1+    \n" +
            "                  2    *3;\n" +
            "    }\n" +
            "}\n");
    }

    public void testMoveExpression3Tab() throws Exception {
        setCodePreferences(TAB_SIZE_PREFERENCES);
        performMoveExpressionTest(
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "\tint i1 =\n" +
            "\t    1+    \n" +
            "\t    2    *3;\n" +
            "        int foo = 0;\n" +
            "    }\n" +
            "}\n",
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "\tint i1 =\n" +
            "\t    1+    \n" +
            "\t    2    *3;\n" +
            "        int foo = 1+    \n" +
            "                  2    *3;\n" +
            "    }\n" +
            "}\n");
    }

    private void performMoveExpressionTest(String code, String golden) throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, code);

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree var1 = (VariableTree) method.getBody().getStatements().get(0);
                VariableTree var2 = (VariableTree) method.getBody().getStatements().get(1);

                workingCopy.rewrite(var2.getInitializer(), var1.getInitializer());
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testMoveExpressionToStatement() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public int taragui(String s1, String s2) {\n" +
            "        int i1 = taragui(\"foo\",\n" +
            "                         \"bar\");\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public int taragui(String s1, String s2) {\n" +
            "        taragui(\"foo\",\n" +
            "                \"bar\");\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);

                workingCopy.rewrite(var, workingCopy.getTreeMaker().ExpressionStatement(var.getInitializer()));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testMoveExpressionToStatementTab() throws Exception {
        Utils.setCodePreferences(TAB_SIZE_PREFERENCES);
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public int taragui(String s1, String s2) {\n" +
            "\tint i1 = taragui(\"foo\",\n" +
            "\t\t\t \"bar\");\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public int taragui(String s1, String s2) {\n" +
            "        taragui(\"foo\",\n" +
            "                \"bar\");\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);

                workingCopy.rewrite(var, workingCopy.getTreeMaker().ExpressionStatement(var.getInitializer()));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testMoveMethod() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    private static class A {\n" +
            "        public void taragui() {\n" +
            "            int i1 = 1+    2    *3;\n" +
            "            int i2 = 1+    2    *3;\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    private static class A {\n" +
            "    }\n" +
            "\n" +
            "    public void taragui() {\n" +
            "        int i1 = 1+    2    *3;\n" +
            "        int i2 = 1+    2    *3;\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                ClassTree clazzInner = (ClassTree) clazz.getMembers().get(1);
                MethodTree method = (MethodTree) clazzInner.getMembers().get(1);

                workingCopy.rewrite(clazz, make.addClassMember(clazz, method));
                workingCopy.rewrite(clazzInner, make.removeClassMember(clazzInner, method));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testMoveStatements() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        System.err.println(1);\n" +
            "        System.err.println(2);\n" +
            "\n" +
            "\n" +
            "        System.err.println(3);System.err.println(3.5);\n" +
            "        System.     err.\n" +
            "                        println(4);\n" +
            "        System.err.println(5);\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        System.err.println(1);\n" +
            "        {\n" +
            "            System.err.println(2);\n" +
            "            \n" +
            "            \n" +
            "            System.err.println(3);System.err.println(3.5);\n" +
            "            System.     err.\n" +
            "                    println(4);\n" +
            "        }\n" +
            "        System.err.println(5);\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree body = method.getBody();
                BlockTree inner = make.Block(body.getStatements().subList(1, 5), false);
                BlockTree nue = make.Block(Arrays.asList(body.getStatements().get(0), inner, body.getStatements().get(5)), false);

                workingCopy.rewrite(body, nue);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testMoveStatements2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        System.err.println(1);\n" +
            "        {\n" +
            "            while (true) {\n" +
            "                System.err.println(2);\n" +
            "\n" +
            "\n" +
            "                System.err.println(3);System.err.println(3.5);\n" +
            "                System.     err.\n" +
            "                                println(4);\n" +
            "            }\n" +
            "        }\n" +
            "        if (true) {\n" +
            "            System.err.println(5);\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        System.err.println(1);\n" +
            "        if (true) {\n" +
            "            System.err.println(2);\n" +
            "            \n" +
            "            \n" +
            "            System.err.println(3);System.err.println(3.5);\n" +
            "            System.     err.\n" +
            "                    println(4);\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree body = method.getBody();
                BlockTree block = (BlockTree)body.getStatements().get(1);
                WhileLoopTree loop = (WhileLoopTree)block.getStatements().get(0);
                IfTree inner = make.If(make.Parenthesized(make.Literal(Boolean.TRUE)), loop.getStatement(), null);
                BlockTree nue = make.Block(Arrays.asList(body.getStatements().get(0), inner), false);

                workingCopy.rewrite(body, nue);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test187616() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(String str) {\n" +
            "        //blabla\n" +
            "\twhile(path.getLeaf().getKind() != Kind.CLASS) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(String str) {\n" +
            "        //blabla\n" +
            "\twhile(!TreeUtilities.SET.contains(path.getLeaf().getKind())) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.PARSED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = method.getBody();
                WhileLoopTree loop = (WhileLoopTree)body.getStatements().get(0);
                BinaryTree origCond = (BinaryTree) ((ParenthesizedTree) loop.getCondition()).getExpression();
                ExpressionTree nueCondition = make.Unary(Tree.Kind.LOGICAL_COMPLEMENT, make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.MemberSelect(make.Identifier("TreeUtilities"), "SET"), "contains"), Collections.singletonList(origCond.getLeftOperand())));

                workingCopy.rewrite(origCond, nueCondition);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    private static final Map<String, String> NO_TAB_EXPAND_PREFERENCES =
            Utils.MapBuilder.<String, String>create().add(FmtOptions.indentSize, "4")
                                                      .add(FmtOptions.tabSize, "8")
                                                      .add(FmtOptions.expandTabToSpaces, "false")
                                                      .build();
    public void test192753() throws Exception {
        setCodePreferences(NO_TAB_EXPAND_PREFERENCES);
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(String str) {\n" +
            "\tint a = 0;\n" +
            "\tSystem.err.println(1);\n" +
            "\tSystem.err.println(2);\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(String str) {\n" +
            "\tint a = 0;\n" +
            "    }\n\n" +
            "    void nue() {\n" +
            "\tSystem.err.println(1);\n" +
            "\tSystem.err.println(2);\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree body = method.getBody();
                BlockTree nueBlock = make.Block(body.getStatements().subList(1, body.getStatements().size()), false);
                MethodTree nueMethod = make.Method(make.Modifiers(EnumSet.noneOf(Modifier.class)), "nue", make.PrimitiveType(TypeKind.VOID), Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), nueBlock, null);

                workingCopy.rewrite(clazz, make.addClassMember(clazz, nueMethod));
                workingCopy.rewrite(body, make.Block(body.getStatements().subList(0, 1), false));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testCLikeArray() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public int taragui() {\n" +
            "        int ii[] = null;" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public int taragui(int[] a) {\n" +
            "        int ii[] = null;" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);
                TreeMaker make = workingCopy.getTreeMaker();
                VariableTree param = workingCopy.getTreeMaker().Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "a", var.getType(), null);

                workingCopy.rewrite(method, make.addMethodParameter(method, param));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testSkipBlockStatement225686() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public int taragui() {\n" +
            "        String str1 = null;\n" +
            "        String str2 = null;\n" +
            "        String str3 = null;\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public int taragui() {\n" +
            "        {\n" +
            "            String str1 = null;\n" +
            "            String str3 = null;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                TreeMaker make = workingCopy.getTreeMaker();

                workingCopy.rewrite(method.getBody(), make.Block(Collections.singletonList(make.removeBlockStatement(method.getBody(), 1)), false));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testMoveMultiLineExpression() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public int taragui(String str) {\n" +
            "        String str1 = (String) str.substring(1)\n" +
            "                .substring(2, 3);\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public int taragui(String str) {\n" +
            "        String str1 = str.substring(1)\n" +
            "                .substring(2, 3);\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree locVar = (VariableTree) method.getBody().getStatements().get(0);
                TypeCastTree tct = (TypeCastTree) locVar.getInitializer();
                
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
                        if (node.getArguments().size() == 1) {
                            workingCopy.tag(node, "test");
                        }
                        return super.visitMethodInvocation(node, p);
                    }
                    @Override public Void visitLiteral(LiteralTree node, Void p) {
                        if (Objects.equals(2, node.getValue())) {
                            workingCopy.tag(node, "dvojka");
                        }
                        return super.visitLiteral(node, p);
                    }
                }.scan(tct.getExpression(), null);

                workingCopy.rewrite(tct, tct.getExpression());
            }

        };
        ModificationResult mr = src.runModificationTask(task);
        mr.commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
        int[] testSpan = mr.getSpan("test");
        assertEquals("str.substring(1)", res.substring(testSpan[0], testSpan[1]));
        int[] dvojkaSpan = mr.getSpan("dvojka");
        assertEquals("2", res.substring(dvojkaSpan[0], dvojkaSpan[1]));
    }

    public void testTagSpans() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public int taragui(String str) {\n" +
            "        String str1 = str.substring(1)\n" +
            "                .substring(2, 3);\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public int taragui(String str) {\n" +
            "        {\n" +
            "            String str1 = str.substring(1)\n" +
            "                    .substring(2, 3);\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree locVar = (VariableTree) method.getBody().getStatements().get(0);
                TreeMaker make = workingCopy.getTreeMaker();
                
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
                        if (node.getArguments().size() == 1) {
                            workingCopy.tag(node, "test");
                        }
                        return super.visitMethodInvocation(node, p);
                    }
                    @Override public Void visitLiteral(LiteralTree node, Void p) {
                        if (Objects.equals(2, node.getValue())) {
                            workingCopy.tag(node, "dvojka");
                        }
                        return super.visitLiteral(node, p);
                    }
                }.scan(locVar, null);

                workingCopy.rewrite(locVar, make.Block(Collections.singletonList(locVar), false));
            }

        };
        ModificationResult mr = src.runModificationTask(task);
        mr.commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
        int[] testSpan = mr.getSpan("test");
        assertEquals("str.substring(1)", res.substring(testSpan[0], testSpan[1]));
        int[] dvojkaSpan = mr.getSpan("dvojka");
        assertEquals("2", res.substring(dvojkaSpan[0], dvojkaSpan[1]));
    }

    public void testMoveSwitchExpression() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        File sourceFile = new File(getWorkDir(), "Source.java");
        TestUtilities.copyStringToFile(testFile,
                """
                package hierbas.del.litoral;

                public class Test {
                }
                """);
        TestUtilities.copyStringToFile(sourceFile,
                """
                package hierbas.del.litoral;

                class Source {
                    public int taragui(String str) {
                        return switch (str.length()) {
                            case 0 -> 0;
                            default -> 1;
                        };
                    }
                }
                """);
        String golden =
                """
                package hierbas.del.litoral;

                public class Test {

                    public int taragui(String str) {
                        return switch (str.length()) {
                            case 0 -> 0;
                            default -> 1;
                        };
                    }
                }
                """;

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                SourceUtils.forceSource(workingCopy, FileUtil.toFileObject(sourceFile));
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree main = (ClassTree) cut.getTypeDecls().get(0);
                
                TypeElement sourceEl = workingCopy.getElements().getTypeElement("hierbas.del.litoral.Source");
                TreePath sourcePath = workingCopy.getTrees().getPath(sourceEl);
                ClassTree source = (ClassTree) sourcePath.getLeaf();
                MethodTree method = (MethodTree) source.getMembers().get(1);
                workingCopy.rewrite(main, make.removeClassMember(source, method));
                workingCopy.rewrite(main, make.addClassMember(main, method));
            }

        };
        ModificationResult mr = src.runModificationTask(task);
        mr.commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
    
}
