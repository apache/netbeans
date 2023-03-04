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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import static com.sun.source.tree.Tree.*;
import java.io.File;
import java.util.prefs.Preferences;
import org.netbeans.api.java.source.*;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.ui.FmtOptions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Modifying operator through the API methods.
 * 
 * @author Pavel Flaska
 */
public class OperatorsTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of OperatorsTest 
     * 
     * @param name 
     */
    public OperatorsTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(OperatorsTest.class);
//        suite.addTest(new OperatorsTest("testAndToOrOperAssign"));
//        suite.addTest(new OperatorsTest("testChangeBinaryOperator"));
//        suite.addTest(new OperatorsTest("testChangeUnaryOperator"));
        return suite;
    }

    /**
     *
     */
    public void testAndToOrOperAssign() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int a = 10;\n" +
            "        int b = 20;\n" +
            "        a &= b;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int a = 10;\n" +
            "        int b = 20;\n" +
            "        a |= b;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree est = (ExpressionStatementTree) method.getBody().getStatements().get(2);
                CompoundAssignmentTree t = (CompoundAssignmentTree) est.getExpression();
                workingCopy.rewrite(t, make.CompoundAssignment(Kind.OR_ASSIGNMENT, t.getVariable(), t.getExpression()));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     *
     */
    public void testChangeBinaryOperator() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int c = (0x0f | 7);\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int c = (0x0f & 7);\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree lvd = (VariableTree) method.getBody().getStatements().get(0);
                ParenthesizedTree pt = (ParenthesizedTree) lvd.getInitializer();
                BinaryTree bt = (BinaryTree) pt.getExpression();
                workingCopy.rewrite(bt, make.Binary(Kind.AND, bt.getLeftOperand(), bt.getRightOperand()));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     *
     */
    public void testChangeUnaryOperator() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int c;\n" +
            "        c++;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int c;\n" +
            "        c--;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree est = (ExpressionStatementTree) method.getBody().getStatements().get(1);
                UnaryTree ut = (UnaryTree) est.getExpression();
                workingCopy.rewrite(ut, make.Unary(Kind.POSTFIX_DECREMENT, ut.getExpression()));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testChangeBinaryOperator2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        Object o = null;\n" +
            "        boolean c = o == null && o instanceof String;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        Object o = null;\n" +
            "        boolean c = o != null || !(o instanceof String);\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree lvd = (VariableTree) method.getBody().getStatements().get(1);
                ExpressionTree orig = lvd.getInitializer();
                ExpressionTree nue = negate(workingCopy, orig);
                workingCopy.rewrite(orig, nue);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    private static ExpressionTree negate(WorkingCopy wc, ExpressionTree input) {
        TreeMaker make = wc.getTreeMaker();

        switch (input.getKind()) {
            case CONDITIONAL_AND:
                BinaryTree andT = (BinaryTree) input;

                return make.Binary(Kind.CONDITIONAL_OR, negate(wc, andT.getLeftOperand()), negate(wc, andT.getRightOperand()));
            case CONDITIONAL_OR:
                BinaryTree orT = (BinaryTree) input;

                return make.Binary(Kind.CONDITIONAL_AND, negate(wc, orT.getLeftOperand()), negate(wc, orT.getRightOperand()));
                
            case EQUAL_TO:
                BinaryTree eqT = (BinaryTree) input;

                return make.Binary(Kind.NOT_EQUAL_TO, eqT.getLeftOperand(), eqT.getRightOperand());

            case PARENTHESIZED:
                return make.Parenthesized(negate(wc, ((ParenthesizedTree) input).getExpression()));

            case LOGICAL_COMPLEMENT:
                ExpressionTree withoutComplement = ((UnaryTree) input).getExpression();

                if (withoutComplement.getKind() == Kind.PARENTHESIZED) {
                    withoutComplement = ((ParenthesizedTree) withoutComplement).getExpression();
                }

                return withoutComplement;

            default:
                return make.Unary(Kind.LOGICAL_COMPLEMENT, make.Parenthesized(input));
        }
    }

    public void testChangeUnary2() throws Exception {
        String test = "public class Test { void m(int x) { int y = x+|+; } }";
        String golden = "public class Test { void m(int x) { int y = --x; } }";
        testFile = new File(getWorkDir(), "Test.java");
        final int index = test.indexOf("|");
        assertTrue(index != -1);
        TestUtilities.copyStringToFile(testFile, test.replace("|", ""));
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy copy) throws Exception {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                Tree node = copy.getTreeUtilities().pathFor(index).getLeaf();
                assertEquals(Kind.POSTFIX_INCREMENT, node.getKind());
                UnaryTree node2 = (UnaryTree) node;
                IdentifierTree original = (IdentifierTree) node2.getExpression();
                System.out.println("node: " + node);
                TreeMaker make = copy.getTreeMaker();
                UnaryTree modified = make.Unary(Kind.PREFIX_DECREMENT, original);
                System.out.println("modified: " + modified);
                copy.rewrite(node, modified);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }

    public void testUnary158150() throws Exception {
        String test = "public class Test { void m(int x) { int y = -| - x; } }";
        String golden = "public class Test { void m(int x) { int y = +x; } }";
        testFile = new File(getWorkDir(), "Test.java");
        final int index = test.indexOf("|");
        assertTrue(index != -1);
        TestUtilities.copyStringToFile(testFile, test.replace("|", ""));
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy copy) throws Exception {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                Tree node = copy.getTreeUtilities().pathFor(index).getLeaf();
                assertEquals(Kind.UNARY_MINUS, node.getKind());
                UnaryTree node2 = (UnaryTree) node;
                UnaryTree original = (UnaryTree) node2.getExpression();
                System.out.println("node: " + node);
                TreeMaker make = copy.getTreeMaker();
                UnaryTree modified = make.Unary(Kind.UNARY_PLUS, original.getExpression());
                System.out.println("modified: " + modified);
                copy.rewrite(node, modified);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }

    public void testUnary187556a() throws Exception {
        performUnaryTest187556(Kind.UNARY_MINUS, Kind.UNARY_MINUS, "- -");
    }

    public void testUnary187556b() throws Exception {
        performUnaryTest187556(Kind.UNARY_PLUS, Kind.UNARY_MINUS, "+-");
    }

    public void testUnary187556c() throws Exception {
        performUnaryTest187556(Kind.UNARY_MINUS, Kind.UNARY_PLUS, "-+");
    }

    public void testUnary187556d() throws Exception {
        performUnaryTest187556(Kind.UNARY_PLUS, Kind.UNARY_PLUS, "+ +");
    }

    public void testUnary187556e() throws Exception {
        performUnaryTest187556(Kind.UNARY_PLUS, Kind.PREFIX_INCREMENT, "+ ++");
    }

    public void testUnary187556f() throws Exception {
        performUnaryTest187556(Kind.UNARY_PLUS, Kind.PREFIX_DECREMENT, "+--");
    }

    private void performUnaryTest187556(final Kind first, final Kind second, String goldenSnippet) throws Exception {
        String test = "public class Test { void m(int x) { int y = (|2 + 1); } }";
        String golden = "public class Test { void m(int x) { int y = " + goldenSnippet + "(2 + 1); } }";
        testFile = new File(getWorkDir(), "Test.java");
        final int index = test.indexOf("|");
        assertTrue(index != -1);
        TestUtilities.copyStringToFile(testFile, test.replace("|", ""));
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy copy) throws Exception {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                Tree node = copy.getTreeUtilities().pathFor(index).getLeaf();
                assertEquals(Kind.PARENTHESIZED, node.getKind());
                System.out.println("node: " + node);
                TreeMaker make = copy.getTreeMaker();
                UnaryTree modified = make.Unary(first, make.Unary(second, (ExpressionTree) node));
                System.out.println("modified: " + modified);
                copy.rewrite(node, modified);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }

    public void testUnary187556g() throws Exception {
        performUnaryVsBinaryTest187556(Kind.PLUS, Kind.UNARY_PLUS, "0+ +");
    }

    public void testUnary187556h() throws Exception {
        performUnaryVsBinaryTest187556(Kind.PLUS, Kind.UNARY_MINUS, "0+-");
    }

    public void testUnary187556i() throws Exception {
        performUnaryVsBinaryTest187556(Kind.MINUS, Kind.PREFIX_DECREMENT, "0- --");
    }

    private void performUnaryVsBinaryTest187556(final Kind first, final Kind second, String goldenSnippet) throws Exception {
        String test = "public class Test { void m(int x) { int y = (|2 + 1); } }";
        String golden = "public class Test { void m(int x) { int y = " + goldenSnippet + "(2 + 1); } }";
        testFile = new File(getWorkDir(), "Test.java");
        final int index = test.indexOf("|");
        assertTrue(index != -1);
        TestUtilities.copyStringToFile(testFile, test.replace("|", ""));

        Preferences prefs = CodeStylePreferences.get(FileUtil.toFileObject(testFile), JavacParser.MIME_TYPE).getPreferences();
        boolean orig = prefs.getBoolean(FmtOptions.spaceAroundBinaryOps, FmtOptions.getDefaultAsBoolean(FmtOptions.spaceAroundBinaryOps));

        prefs.putBoolean(FmtOptions.spaceAroundBinaryOps, false);

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy copy) throws Exception {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                Tree node = copy.getTreeUtilities().pathFor(index).getLeaf();
                assertEquals(Kind.PARENTHESIZED, node.getKind());
                System.out.println("node: " + node);
                TreeMaker make = copy.getTreeMaker();
                Tree modified = make.Binary(first, make.Literal(0), make.Unary(second, (ExpressionTree) node));
                System.out.println("modified: " + modified);
                copy.rewrite(node, modified);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);

        prefs.putBoolean(FmtOptions.spaceAroundBinaryOps, orig);
        
        assertEquals(golden, res);
    }
            
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

    
    
}
