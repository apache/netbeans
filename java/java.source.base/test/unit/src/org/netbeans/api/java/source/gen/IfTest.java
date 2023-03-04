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

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 * Tests if statement creation.
 */
public class IfTest extends GeneratorTestBase {

    public IfTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(IfTest.class);
//        suite.addTest(new IfTest("testEmptyThenBlock"));
//        suite.addTest(new IfTest("testEmptyElseBlock"));
//        suite.addTest(new IfTest("testReplaceCondition"));
//        suite.addTest(new IfTest("testModifyingIf"));
//        suite.addTest(new IfTest("test158463a"));
//        suite.addTest(new IfTest("test158463b"));
//        suite.addTest(new IfTest("test158154OneIf"));
//        suite.addTest(new IfTest("test158154TwoIfs"));
        return suite;
    }

    /**
     * Test replacing then statement with empty block.
     */
    public void testEmptyThenBlock() throws Exception {
        testFile = new File(getWorkDir(), "IfTest.java");        
        TestUtilities.copyStringToFile(testFile, 
            "package foo.bar;\n" +
            "\n" +
            "public class IfTest {\n" +
            "    public void test(boolean b) {\n" +
            "        if( b )\n" +
            "            System.out.println();\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package foo.bar;\n" +
            "\n" +
            "public class IfTest {\n" +
            "    public void test(boolean b) {\n" +
            "        if( b ) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                IfTree oldIf = (IfTree)method.getBody().getStatements().get(0);
                BlockTree blk = make.Block(Collections.<StatementTree>emptyList(), false);
                IfTree newIf = make.If(oldIf.getCondition(), blk, null);
                workingCopy.rewrite(oldIf, newIf);
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testEmptyElseBlock() throws Exception {
        testFile = new File(getWorkDir(), "IfTest.java");        
        TestUtilities.copyStringToFile(testFile, 
            "package foo.bar;\n" +
            "\n" +
            "public class IfTest {\n" +
            "    public void test(boolean b) {\n" +
            "        if( b ) {\n" +
            "        } else\n" +
            "            System.err.println(\"Hrebejk je hrebec.\");\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package foo.bar;\n" +
            "\n" +
            "public class IfTest {\n" +
            "    public void test(boolean b) {\n" +
            "        if( b ) {\n" +
            "        } else {\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                IfTree oldIf = (IfTree)method.getBody().getStatements().get(0);
                BlockTree block = make.Block(Collections.<StatementTree>emptyList(), false);
                StatementTree oldElse = oldIf.getElseStatement();
                workingCopy.rewrite(oldElse, block);
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testReplaceCondition() throws Exception {
        testFile = new File(getWorkDir(), "IfTest.java");        
        TestUtilities.copyStringToFile(testFile, 
            "package foo.bar;\n" +
            "\n" +
            "public class IfTest {\n" +
            "    public void test(boolean b) {\n" +
            "        if (prec == treeinfo.notExpression)\n" +
            "            print(';');\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package foo.bar;\n" +
            "\n" +
            "public class IfTest {\n" +
            "    public void test(boolean b) {\n" +
            "        if (prec == TreeInfo.notExpression)\n" +
            "            print(';');\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                IfTree oldIf = (IfTree) method.getBody().getStatements().get(0);
                BinaryTree zatvorka = (BinaryTree) ((ParenthesizedTree) oldIf.getCondition()).getExpression();
                MemberSelectTree mst = (MemberSelectTree) zatvorka.getRightOperand();
                workingCopy.rewrite(mst.getExpression(), make.Identifier("TreeInfo"));
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testModifyingIf() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public boolean method(int i) {\n" +
            "        int y = 0;\n" +
            "        if (i == 0) {\n" +
            "            y = 2;\n" +
            "        } else {y = 9;}\n" +
            "        return y == 8;\n" +
            "    }\n" +
            "}\n");
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public boolean method(int i) {\n" +
            "        int y = 0;\n" +
            "        if (method(null)) {\n" + 
            "            return true;\n" +
            "        }\n" +
            "        return y == 8;\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                IfTree mit = (IfTree) block.getStatements().get(1);
                IfTree nue = make.If(
                    make.Parenthesized(make.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(),
                        make.Identifier("method"),
                        Arrays.asList(make.Literal(null)))
                    ),
                    make.Block(
                        Collections.<StatementTree>singletonList(make.Return(make.Literal(true))),
                        false
                    ),
                    null
                );
                workingCopy.rewrite(mit, nue);
            }
            
            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test158463a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    void m1(int p, int q) {\n" +
            "        if (p > 0)\n" +
            "            if (q > 0) { p++; }\n" +
            "            else { p--; }\n" +
            "    }\n" +
            "}\n");
         String golden =
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    void m1(int p, int q) {\n" +
            "        if ((p > 0) && (q > 0)) {\n" +
            "            p++;\n" +
            "        } else {\n" +
            "            p--;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                IfTree original = (IfTree) block.getStatements().get(0);
                IfTree original2 = (IfTree) original.getThenStatement();
                IfTree modified = make.If(
                        make.Parenthesized(
                            make.Binary(Kind.CONDITIONAL_AND,
                                original.getCondition(),
                                original2.getCondition())),
                        original2.getThenStatement(),
                        original2.getElseStatement());
                workingCopy.rewrite(original, modified);
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test158463b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    void m1(int p, int q) {\n" +
            "        if (p > 0)\n" +
            "            if (q > 0) p++; \n" +
            "            else p--;\n" +
            "    }\n" +
            "}\n");
         String golden =
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    void m1(int p, int q) {\n" +
            "        if ((p > 0) && (q > 0))\n" +
            "            p++;\n" +
            "        else {\n" + //TODO: brackets (#158154)
            "            p--;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                IfTree original = (IfTree) block.getStatements().get(0);
                IfTree original2 = (IfTree) original.getThenStatement();
                IfTree modified = make.If(
                        make.Parenthesized(
                            make.Binary(Kind.CONDITIONAL_AND,
                                original.getCondition(),
                                original2.getCondition())),
                        original2.getThenStatement(),
                        original2.getElseStatement());
                workingCopy.rewrite(original, modified);
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test159940() throws Exception {
        String test =
                "class Test {\n" +
                "    void m(int p) {\n" +
                "        i|f (p > 5);\n" +
                "    }\n" +
                "}";
        String golden = test.replace("|", "");
        testFile = new File(getWorkDir(), "Test.java");
        final int indexA = test.indexOf("|");
        assertTrue(indexA != -1);
        TestUtilities.copyStringToFile(testFile, test.replace("|", ""));
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy copy) throws Exception {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                Tree node = copy.getTreeUtilities().pathFor(indexA).getLeaf();
                assertEquals(Kind.IF, node.getKind());
                TreeMaker make = copy.getTreeMaker();
                StatementTree original = ((IfTree) node).getThenStatement();
                StatementTree modified = make.EmptyStatement();
                System.out.println("original: " + original);
                System.out.println("modified: " + modified);
                copy.rewrite(original, modified);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testDuplicateIfContent() throws Exception {
        String test =
                "class Test {\n" +
                "    private static void t(int i) {\n" +
                "        i^f (i == 0 || i == 1) {\n" +
                "            System.err.println();\n" +
                "        }\n" +
                "    }\n" +
                "}";
        String golden =
                "class Test {\n" +
                "    private static void t(int i) {\n" +
                "        if (i == 0) {\n" +
                "            System.err.println();\n" +
                "        } else if (i == 1) {\n" +
                "            System.err.println();\n" +
                "        }\n" +
                "    }\n" +
                "}";        testFile = new File(getWorkDir(), "Test.java");
        final int indexA = test.indexOf("^");
        assertTrue(indexA != -1);
        TestUtilities.copyStringToFile(testFile, test.replace("^", ""));
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy copy) throws Exception {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                Tree node = copy.getTreeUtilities().pathFor(indexA).getLeaf();
                assertEquals(Kind.IF, node.getKind());
                TreeMaker make = copy.getTreeMaker();
                IfTree it = (IfTree) node;
                BinaryTree cond = (BinaryTree) ((ParenthesizedTree) it.getCondition()).getExpression();
                IfTree newIf = make.If(make.Parenthesized(cond.getLeftOperand()), it.getThenStatement(), make.If(make.Parenthesized(cond.getRightOperand()), it.getThenStatement(), null));
                copy.rewrite(it, newIf);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }

    public void test158154OneIf() throws Exception {
        String source = "class Test {\n"
                + "    void m1(boolean b) {\n"
                + "        if (b) ; else System.out.println(\"hi\");\n"
                + "    }\n"
                + "}";
        String golden = "class Test {\n"
                + "    void m1(boolean b) {\n"
                + "        if (!(b)) System.out.println(\"hi\");\n"
                + "    }\n"
                + "}";
        testFile = new File(getWorkDir(), "Test.java");

        TestUtilities.copyStringToFile(testFile, source);
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }

                TreeMaker make = copy.getTreeMaker();
                ClassTree clazz = (ClassTree) copy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                IfTree original = (IfTree) block.getStatements().get(0);

                IfTree modified = make.If(
                        make.Parenthesized(
                        make.Unary(Kind.LOGICAL_COMPLEMENT, original.getCondition())),
                        original.getElseStatement(), null);
                copy.rewrite(original, modified);
            }
        };

        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
        assertEquals(golden, res);
    }

    public void test158154TwoIfs() throws Exception {
        String source = "class Test {\n"
                + "    void m1(boolean b) {\n"
                + "        if (b) ; else System.out.println(\"first hi\");\n"
                + "        if (b) ; else System.out.println(\"second hi\");\n"
                + "        System.out.println();\n"
                + "    }\n"
                + "}";
        String golden = "class Test {\n"
                + "    void m1(boolean b) {\n"
                + "        if (!(b)) System.out.println(\"first hi\");\n"
                + "        if (!(b)) System.out.println(\"second hi\");\n"
                + "        System.err.println();\n"
                + "    }\n"
                + "}";
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, source);
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }

                TreeMaker make = copy.getTreeMaker();
                ClassTree clazz = (ClassTree) copy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                IfTree originalA = (IfTree) block.getStatements().get(0);
                IfTree originalB = (IfTree) block.getStatements().get(1);
                IfTree modifiedA = make.If(
                        make.Parenthesized(
                        make.Unary(Kind.LOGICAL_COMPLEMENT, originalA.getCondition())),
                        originalA.getElseStatement(), null);
                copy.rewrite(originalA, modifiedA);
                IfTree modifiedB = make.If(
                        make.Parenthesized(
                        make.Unary(Kind.LOGICAL_COMPLEMENT, originalB.getCondition())),
                        originalB.getElseStatement(), null);
                copy.rewrite(originalB, modifiedB);
                
                Tree originalC = block.getStatements().get(2);
                Tree modifiedC = make.ExpressionStatement(make.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(), 
                            make.MemberSelect(
                                make.MemberSelect(make.QualIdent("java.lang.System"), "err"), 
                                "println"), Collections.<ExpressionTree>emptyList()));
                copy.rewrite(originalC, modifiedC);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
        assertEquals(golden, res);
    }
    
    public void test257910NestedIfs() throws Exception {
        String source = "public class Test {\n"
                + "    public void test() {\n"
                + "        if (true) {\n"
                + "            System.out.println(2);\n"
                + "        } else if (false) {\n"
                + "            System.out.println(1);\n"
                + "        }\n"
                + "    }\n"
                + "}";
        
        String golden = "public class Test {\n"
                + "    public void test() {\n"
                + "        if (false) {\n"
                + "            if (false) {\n"
                + "                System.out.println(1);\n"
                + "            }\n"
                + "        } else {\n"
                + "            System.out.println(2);\n"
                + "        }\n"
                + "    }\n"
                + "}";
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, source);
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }

                TreeMaker make = copy.getTreeMaker();
                ClassTree clazz = (ClassTree) copy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                
                BlockTree block = method.getBody();
                IfTree originalA = (IfTree) block.getStatements().get(0);
                
                // swap branches
                IfTree rewrite = make.If(
                        make.Parenthesized(
                            make.Literal(Boolean.FALSE)
                        ),
                        originalA.getElseStatement(), originalA.getThenStatement()
                );
                copy.rewrite(originalA, rewrite);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
        assertEquals(golden, res);
    }

    public void test257910NestedIfsCorrectlyPaired() throws Exception {
        String source = "public class Test {\n"
                + "    public void test() {\n"
                + "        if (true) {\n"
                + "            System.out.println(2);\n"
                + "        } else if (false) {\n"
                + "            System.out.println(1);\n"
                + "        } else \n" 
                + "            System.out.println(3);\n"
                + "    }\n"
                + "}";
        
        String golden = "public class Test {\n"
                + "    public void test() {\n"
                + "        if (false) if (false) {\n"
                + "            System.out.println(1);\n"
                + "        } else\n"
                + "            System.out.println(3); else {\n"
                + "            System.out.println(2);\n"
                + "        }\n"
                + "    }\n"
                + "}";
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, source);
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }

                TreeMaker make = copy.getTreeMaker();
                ClassTree clazz = (ClassTree) copy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                
                BlockTree block = method.getBody();
                IfTree originalA = (IfTree) block.getStatements().get(0);
                
                // swap branches
                IfTree rewrite = make.If(
                        make.Parenthesized(
                            make.Literal(Boolean.FALSE)
                        ),
                        originalA.getElseStatement(), originalA.getThenStatement()
                );
                copy.rewrite(originalA, rewrite);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
        assertEquals(golden, res);
    }


    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
}
