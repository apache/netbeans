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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.YieldTree;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.tree.JCTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.swing.event.ChangeListener;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 * Test cases for SwitchExpression
 *
 */
public class SwitchExpressionTest extends TreeRewriteTestBase {

    public SwitchExpressionTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(SwitchExpressionTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sourceLevel = "17";
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = false;

    }

    public void testSwitchExpression() throws Exception {
        String code = "package test; \n"
                + "public class Test {\n"
                + "     private void test(int p) {\n"
                + "         var v = switch (p) {\n"
                + "             case 1: yield 1;\n"
                + "             case 2 -> 2;\n"
                + "             default -> 3;\n"
                + "         }\n"
                + "     }\n"
                + "}\n";
        String golden = "package test; \n"
                + "public class Test {\n"
                + "     private void test(int p) {\n"
                + "         var v = switch (p) {\n"
                + "             case 1 -> {\n"
                + "                 yield 1;\n"
                + "             }\n"
                + "             case 2 -> {\n"
                + "                 yield 2;\n"
                + "             }\n"
                + "             default -> {\n"
                + "                 yield 3;\n"
                + "             }\n"
                + "         }\n"
                + "     }\n"
                + "}\n";

        prepareTest("Test", code);

        rewriteSwitchExpression();
        String res = TestUtilities.copyFileToString(getTestFile());
        //System.err.println(res);
        assertEquals(golden, res);

    }

    /**
     * Rewrite Switch Expression cases.
     *
     * @throws IOException
     */
    private void rewriteSwitchExpression() throws IOException {

        JavaSource js = getJavaSource();
        assertNotNull(js);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                List<CaseTree> newCases = new ArrayList<>();
                VariableTree switcExpression = (VariableTree) ((BlockTree) method.getBody()).getStatements().get(0);
                Tree switchBlock = switcExpression.getInitializer();
                List<? extends CaseTree> cases;
                ExpressionTree selExpr;
                List<ExpressionTree> patterns = new ArrayList<>();
                boolean switchExpressionFlag = switchBlock.getKind() == Kind.SWITCH_EXPRESSION;
                if (switchExpressionFlag) {
                    selExpr = ((SwitchExpressionTree) switchBlock).getExpression();
                    cases = ((SwitchExpressionTree) switchBlock).getCases();
                } else {
                    selExpr = ((SwitchTree) switchBlock).getExpression();
                    cases = ((SwitchTree) switchBlock).getCases();
                }
                for (Iterator<? extends CaseTree> it = cases.iterator(); it.hasNext();) {
                    CaseTree ct = it.next();
                    patterns.addAll(ct.getExpressions());
                    List<StatementTree> statements;
                    if (ct.getStatements() == null) {
                        statements = new ArrayList<>(((JCTree.JCCase) ct).stats);
                    } else {
                        statements = new ArrayList<>(ct.getStatements());
                    }
                    if (statements.isEmpty()) {
                        if (it.hasNext()) {
                            continue;
                        }
                    }
                    Set<Element> seenVariables = new HashSet<>();
                    int idx = 0;
                    for (StatementTree statement : new ArrayList<>(statements)) {
                        Tree body = make.Block(statements, false);
                        if (statements.size() == 1) {
                            if (statements.get(0).getKind() == Tree.Kind.EXPRESSION_STATEMENT
                                    || statements.get(0).getKind() == Tree.Kind.THROW
                                    || statements.get(0).getKind() == Tree.Kind.BLOCK) {
                                body = statements.get(0);
                            }
                        }
                        newCases.add(make.Case(patterns, body));
                        patterns = new ArrayList<>();
                    }
                }
                workingCopy.rewrite(switchBlock, make.SwitchExpression(selExpr, newCases));
            }

        };

        js.runModificationTask(task).commit();
    }

    public void testYield1() throws Exception {
        String code = """
                      package test;
                      public class Test {
                           private int test(String str) {
                               return switch (str) {
                                   case "":
                                       yield "Hello";
                                   default -> "";
                               }
                           }
                      }
                      """;
        String golden = """
                        package test;
                        public class Test {
                             private int test(String str) {
                                 return switch (str) {
                                     case "":
                                         yield str;
                                     default -> "";
                                 }
                             }
                        }
                        """;

        prepareTest("Test", code);

        JavaSource js = getJavaSource();
        assertNotNull(js);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitYield(YieldTree node, Void p) {
                        workingCopy.rewrite(node.getValue(), make.Identifier("str"));
                        return super.visitYield(node, p);
                    }
                }.scan(cut, null);
            }
        };

        js.runModificationTask(task).commit();

        String res = TestUtilities.copyFileToString(getTestFile());
        //System.err.println(res);
        assertEquals(golden, res);

    }

    public void testAnythingToBlock() throws Exception {
        String code = """
                      package test; 
                      public class Test {
                           private void test(int p) {
                               var v = switch (p) {
                                   case 1 -> p = 0;
                                   default -> throw IllegalStateException();
                               }
                           }
                      }
                      """;
        String golden = """
                        package test; 
                        public class Test {
                             private void test(int p) {
                                 var v = switch (p) {
                                     case 1 -> {
                                     }
                                     default -> {
                                     }
                                 }
                             }
                        }
                        """;

        prepareTest("Test", code);


        JavaSource js = getJavaSource();
        assertNotNull(js);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);

                TreeMaker make = workingCopy.getTreeMaker();

                new TreePathScanner<>() {
                    @Override
                    public Object visitCase(CaseTree node, Object p) {
                        workingCopy.rewrite(node, make.CasePatterns(node.getLabels(), make.Block(List.of(), false)));
                        return super.visitCase(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };

        js.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(getTestFile());
        //System.err.println(res);
        assertEquals(golden, res);

    }

    public void testBlockToAnything() throws Exception {
        String code = """
                      package test; 
                      public class Test {
                           private void test(int p) {
                               var v = switch (p) {
                                   case 1 -> {}
                                   default -> { throw IllegalStateException(); }
                               }
                           }
                      }
                      """;
        String golden = """
                        package test; 
                        public class Test {
                             private void test(int p) {
                                 var v = switch (p) {
                                     case 1 -> 0;
                                     default -> throw IllegalStateException();
                                 }
                             }
                        }
                        """;

        prepareTest("Test", code);


        JavaSource js = getJavaSource();
        assertNotNull(js);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);

                TreeMaker make = workingCopy.getTreeMaker();

                new TreePathScanner<>() {
                    @Override
                    public Object visitCase(CaseTree node, Object p) {
                        BlockTree bt = (BlockTree) node.getBody();
                        Tree newBody = bt.getStatements().isEmpty() ? make.Literal(0)
                                                                    : bt.getStatements().get(0);
                        workingCopy.rewrite(node, make.CasePatterns(node.getLabels(), newBody));
                        return super.visitCase(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };

        js.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(getTestFile());
        //System.err.println(res);
        assertEquals(golden, res);
    }
}
