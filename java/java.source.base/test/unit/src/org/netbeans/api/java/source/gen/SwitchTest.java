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
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 * The following shell script was used to generate the code snippets
 * <code>cat test/unit/data/test/Test.java | tr '\n' ' ' | tr '\t' ' ' | sed -E 's| +| |g' | sed 's|"|\\"|g'</code>
 * @author Samuel Halliday
 */
public class SwitchTest extends GeneratorTestBase {

    public SwitchTest(String name) {
        super(name);
    }

    public void test158129() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        String test = "public class Test { void m(int p) { switch (p) { ca|se 0: } } }";
        // XXX whitespace "public class Test { void m(int p) { switch (p) { case 0: break; } } }"
        String golden = "public class Test { void m(int p) { switch (p) { case 0:break;\n } } }";
        final int index = test.indexOf("|");
        assertTrue(index != -1);
        TestUtilities.copyStringToFile(testFile, test.replace("|", ""));
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy copy) throws IOException {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                TreeMaker make = copy.getTreeMaker();
                TreePath node = copy.getTreeUtilities().pathFor(index);
                assertTrue(node.getLeaf().getKind() == Kind.CASE);
                CaseTree original = (CaseTree) node.getLeaf();
                List<StatementTree> st = new ArrayList<StatementTree>();
                st.addAll(original.getStatements());
                st.add(make.Break(null));
                CaseTree modified = make.Case(original.getExpression(), st);
                copy.rewrite(original, modified);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddCase1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        String test = "public class Test {\n" +
                      "    void m(int p) {\n" +
                      "        switch (p) {\n" +
                      "            case 0:\n" +
                      "                System.err.println(1);\n" +
                      "                break;\n" +
                      "            ca|se 2:\n" +
                      "                System.err.println(2);\n" +
                      "                break;\n" +
                      "        }\n" +
                      "    }\n" +
                      "}\n";
        String golden = "public class Test {\n" +
                        "    void m(int p) {\n" +
                        "        switch (p) {\n" +
                        "            case 0:\n" +
                        "                System.err.println(1);\n" +
                        "                break;\n" +
                        "            case 1:\n" +
                        "            case 2:\n" +
                        "            case 3:\n" +
                        "                System.err.println(2);\n" +
                        "                break;\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n";
        final int index = test.indexOf("|");
        assertTrue(index != -1);
        TestUtilities.copyStringToFile(testFile, test.replace("|", ""));
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy copy) throws IOException {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                TreeMaker make = copy.getTreeMaker();
                TreePath node = copy.getTreeUtilities().pathFor(index);
                assertTrue(node.getLeaf().getKind() == Kind.CASE);
                SwitchTree st = (SwitchTree) node.getParentPath().getLeaf();
                List<CaseTree> newCases = new LinkedList<CaseTree>();
                newCases.add(st.getCases().get(0));
                newCases.add(make.Case(make.Literal(1), Collections.<StatementTree>emptyList()));
                newCases.add(make.Case(make.Literal(2), Collections.<StatementTree>emptyList()));
                newCases.add(make.Case(make.Literal(3), st.getCases().get(1).getStatements()));
                copy.rewrite(st, make.Switch(st.getExpression(), newCases));
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testIf2Switch() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        String test = "public class Test {\n" +
                      "    void m(int p, int thmbPaneWidth) {\n" +
                      "        if (p == 0) {\n" +
                      "            int width = thmbPaneWidth,\n" +
                      "                    height = (int) (thmbPaneWidth * 1.2),\n" +
                      "                    left = 0,\n" +
                      "                    top = 0;\n" +
                      "        }\n" +
                      "    }\n" +
                      "}\n";
        String golden = "public class Test {\n" +
                        "    void m(int p, int thmbPaneWidth) {\n" +
                        "        switch (p) {\n" +
                        "            case 0:\n" +
                        "                int width = thmbPaneWidth,\n" +
                        "                        height = (int) (thmbPaneWidth * 1.2),\n" +
                        "                        left = 0,\n" +
                        "                        top = 0;\n" +
                        "                break;\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n";
        TestUtilities.copyStringToFile(testFile, test.replace("|", ""));
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy copy) throws IOException {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                final TreeMaker make = copy.getTreeMaker();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override public Void visitIf(IfTree node, Void p) {
                        List<StatementTree> statements = new ArrayList<StatementTree>(((BlockTree) node.getThenStatement()).getStatements());
                        statements.add(make.Break(null));
                        copy.rewrite(node, make.Switch(make.Identifier("p"), Collections.singletonList(make.Case(make.Literal(0), statements))));
                        return super.visitIf(node, p);
                    }
                }.scan(copy.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testSwitch2If() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        String test = "public class Test {\n" +
                      "    void m(int p, int thmbPaneWidth) {\n" +
                        "        switch (p) {\n" +
                        "            case 0:\n" +
                        "                int width = thmbPaneWidth,\n" +
                        "                        height = (int) (thmbPaneWidth * 1.2),\n" +
                        "                        left = 0,\n" +
                        "                        top = 0;\n" +
                        "                break;\n" +
                        "        }\n" +
                      "    }\n" +
                      "}\n";
        String golden = "public class Test {\n" +
                        "    void m(int p, int thmbPaneWidth) {\n" +
                      "        if (p == 0) {\n" +
                      "            int width = thmbPaneWidth,\n" +
                      "                    height = (int) (thmbPaneWidth * 1.2),\n" +
                      "                    left = 0,\n" +
                      "                    top = 0;\n" +
                      "        }\n" +
                        "    }\n" +
                        "}\n";
        TestUtilities.copyStringToFile(testFile, test.replace("|", ""));
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy copy) throws IOException {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                final TreeMaker make = copy.getTreeMaker();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override public Void visitCase(CaseTree node, Void p) {
                        IfTree nue = make.If(make.Binary(Kind.EQUAL_TO, make.Identifier("p"), make.Literal(0)), make.Block(node.getStatements().subList(0, node.getStatements().size() - 1), false), null);
                        copy.rewrite(getCurrentPath().getParentPath().getLeaf(), nue);
                        return super.visitCase(node, p);
                    }
                }.scan(copy.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testCaseMultiTest() throws Exception {
        class TestCase {
            public final String caseText;
            public final BiFunction<WorkingCopy, CaseTree, CaseTree> convertCase;
            public final String expectedCaseText;

            public TestCase(String caseText, BiFunction<WorkingCopy, CaseTree, CaseTree> convertCase, String expectedCaseText) {
                this.caseText = caseText;
                this.convertCase = convertCase;
                this.expectedCaseText = expectedCaseText;
            }

        }
        TestCase[] testCases = new TestCase[] {
            new TestCase("case 0:",
                         (copy, tree) -> {
                             List<ExpressionTree> labels = new ArrayList<>(tree.getExpressions());
                             labels.add(copy.getTreeMaker().Literal(1));
                             return copy.getTreeMaker().CaseMultipleLabels(labels, tree.getStatements());
                         },
                         "case 0, 1:"),
            new TestCase("case 0:",
                         (copy, tree) -> {
                             List<ExpressionTree> labels = new ArrayList<>(tree.getExpressions());
                             labels.add(0, copy.getTreeMaker().Literal(-1));
                             return copy.getTreeMaker().CaseMultipleLabels(labels, tree.getStatements());
                         },
                         "case -1, 0:"),
            new TestCase("case 0:",
                         (copy, tree) -> {
                             List<ExpressionTree> labels = new ArrayList<>(tree.getExpressions());
                             labels.add(0, copy.getTreeMaker().Literal(-1));
                             labels.add(copy.getTreeMaker().Literal(1));
                             return copy.getTreeMaker().CaseMultipleLabels(labels, tree.getStatements());
                         },
                         "case -1, 0, 1:"),
            new TestCase("case -1, 1:",
                         (copy, tree) -> {
                             List<ExpressionTree> labels = new ArrayList<>(tree.getExpressions());
                             labels.add(1, copy.getTreeMaker().Literal(0));
                             return copy.getTreeMaker().CaseMultipleLabels(labels, tree.getStatements());
                         },
                         "case -1, 0, 1:"),
            new TestCase("case -1, 0, 1:",
                         (copy, tree) -> {
                             List<ExpressionTree> labels = new ArrayList<>(tree.getExpressions());
                             labels.remove(0);
                             return copy.getTreeMaker().CaseMultipleLabels(labels, tree.getStatements());
                         },
                         "case 0, 1:"),
            new TestCase("case -1, 0, 1:",
                         (copy, tree) -> {
                             List<ExpressionTree> labels = new ArrayList<>(tree.getExpressions());
                             labels.remove(1);
                             return copy.getTreeMaker().CaseMultipleLabels(labels, tree.getStatements());
                         },
                         "case -1, 1:"),
            new TestCase("case -1, 0, 1:",
                         (copy, tree) -> {
                             List<ExpressionTree> labels = new ArrayList<>(tree.getExpressions());
                             labels.remove(2);
                             return copy.getTreeMaker().CaseMultipleLabels(labels, tree.getStatements());
                         },
                         "case -1, 0:"),
            new TestCase("default:",
                         (copy, tree) -> {
                             List<ExpressionTree> labels = new ArrayList<>(tree.getExpressions());
                             labels.add(copy.getTreeMaker().Literal(0));
                             return copy.getTreeMaker().CaseMultipleLabels(labels, tree.getStatements());
                         },
                         "case 0:"),
            new TestCase("case 0:",
                         (copy, tree) -> {
                             List<ExpressionTree> labels = new ArrayList<>();
                             return copy.getTreeMaker().CaseMultipleLabels(labels, tree.getStatements());
                         },
                         "default:"),
        };
        int idx = 0;
        for (TestCase tc : testCases) {
            testFile = new File(getWorkDir(), "Test" + idx + ".java");
            String test = "public class Test" + idx + " {\n" +
                          "    void m(int p) {\n" +
                          "        switch (p) {\n" +
                          "            " + tc.caseText + "\n" +
                          "                System.err.println(0);\n" +
                          "                break;\n" +
                          "        }\n" +
                          "    }\n" +
                          "}\n";
            idx++;
            TestUtilities.copyStringToFile(testFile, test);
            JavaSource src = getJavaSource(testFile);
            Task<WorkingCopy> task = new Task<WorkingCopy>() {
                public void run(final WorkingCopy copy) throws IOException {
                    if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                        return;
                    }
                    new ErrorAwareTreePathScanner<Void, Void>() {
                        @Override public Void visitCase(CaseTree node, Void p) {
                            copy.rewrite(node, tc.convertCase.apply(copy, node));
                            return super.visitCase(node, p);
                        }
                    }.scan(copy.getCompilationUnit(), null);
                }
            };
            src.runModificationTask(task).commit();
            String res = TestUtilities.copyFileToString(testFile);
            src.runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(Phase.RESOLVED);
                    new ErrorAwareTreePathScanner<Void, Void>() {
                        @Override public Void visitCase(CaseTree node, Void p) {
                            String actual = cc.getText()
                                              .substring((int) cc.getTrees().getSourcePositions().getStartPosition(cc.getCompilationUnit(), node),
                                                         (int) cc.getTrees().getSourcePositions().getStartPosition(cc.getCompilationUnit(), node.getStatements().get(0)))
                                              .trim();
                            String expected = tc.expectedCaseText;
                            assertEquals(expected, actual);
                            return super.visitCase(node, p);
                        }
                    }.scan(cc.getCompilationUnit(), null);
                }
            }, true);
        }
    }

    public void testStatement2Rule() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        String test = "public class Test {\n" +
                      "    void m(int p) {\n" +
                      "        switch (p) {\n" +
                      "            case 0:\n" +
                      "                System.err.println(0);\n" +
                      "                System.err.println(1);\n" +
                      "                break;\n" +
                      "        }\n" +
                      "    }\n" +
                      "}\n";
        String golden = "public class Test {\n" +
                        "    void m(int p) {\n" +
                        "        switch (p) {\n" +
                        "            case 0 -> {\n" +
                        "                System.err.println(0);\n" +
                        "                System.err.println(1);\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n";
        TestUtilities.copyStringToFile(testFile, test.replace("|", ""));
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy copy) throws IOException {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                final TreeMaker make = copy.getTreeMaker();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override public Void visitCase(CaseTree node, Void p) {
                        copy.rewrite(getCurrentPath().getLeaf(),
                                     make.Case(node.getExpressions(), make.Block(node.getStatements().subList(0, 2), false)));
                        return super.visitCase(node, p);
                    }
                }.scan(copy.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRule2Statement() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        String test = "public class Test {\n" +
                      "    void m(int p) {\n" +
                      "        switch (p) {\n" +
                      "            case 0 -> {\n" +
                      "                System.err.println(0);\n" +
                      "                System.err.println(1);\n" +
                      "            }\n" +
                      "        }\n" +
                      "    }\n" +
                      "}\n";
        String golden = "public class Test {\n" +
                        "    void m(int p) {\n" +
                        "        switch (p) {\n" +
                        "            case 0:\n" +
                        "                System.err.println(0);\n" +
                        "                System.err.println(1);\n" +
                        "                break;\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n";
        TestUtilities.copyStringToFile(testFile, test.replace("|", ""));
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy copy) throws IOException {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                final TreeMaker make = copy.getTreeMaker();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override public Void visitCase(CaseTree node, Void p) {
                        List<StatementTree> statements = new ArrayList<>(((BlockTree) node.getBody()).getStatements());
                        statements.add(make.Break(null));
                        copy.rewrite(getCurrentPath().getLeaf(),
                                     make.CaseMultipleLabels(node.getExpressions(), statements));
                        return super.visitCase(node, p);
                    }
                }.scan(copy.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testDefaultRewrite() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        String test = "public class Test {\n" +
                      "    void m(int p) {\n" +
                      "        switch (p) {\n" +
                      "            default: \n" +
                      "                System.err.println(0);\n" +
                      "                break;\n" +
                      "        }\n" +
                      "    }\n" +
                      "}\n";
        String golden = "public class Test {\n" +
                        "    void m(int p) {\n" +
                        "        switch (p) {\n" +
                        "            default -> System.err.println(0);\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n";
        TestUtilities.copyStringToFile(testFile, test.replace("|", ""));
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy copy) throws IOException {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                final TreeMaker make = copy.getTreeMaker();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override public Void visitCase(CaseTree node, Void p) {
                        copy.rewrite(getCurrentPath().getLeaf(),
                                     make.Case(node.getExpressions(), node.getStatements().get(0)));
                        return super.visitCase(node, p);
                    }
                }.scan(copy.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testStatement2RuleNoSpace() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        String test = "public class Test {\n" +
                      "    void m(int p) {\n" +
                      "        switch (p) {\n" +
                      "            case 0:{\n" +
                      "                System.err.println(0);\n" +
                      "                System.err.println(1);\n" +
                      "                break;\n" +
                      "            }\n" +
                      "        }\n" +
                      "    }\n" +
                      "}\n";
        String golden = "public class Test {\n" +
                        "    void m(int p) {\n" +
                        "        switch (p) {\n" +
                        "            case 0 -> {\n" +
                        "                System.err.println(0);\n" +
                        "                System.err.println(1);\n" +
                        "                break;\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n";
        TestUtilities.copyStringToFile(testFile, test.replace("|", ""));
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy copy) throws IOException {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                final TreeMaker make = copy.getTreeMaker();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override public Void visitCase(CaseTree node, Void p) {
                        copy.rewrite(getCurrentPath().getLeaf(),
                                     make.Case(node.getExpressions(), node.getStatements().get(0)));
                        return super.visitCase(node, p);
                    }
                }.scan(copy.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testNestedSwitches() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        String test = "public class Test {\n" +
                      "    void m(int p) {\n" +
                      "        switch (p) {\n" +
                      "            case 0:\n" +
                      "                switch (p) {\n" +
                      "                    case 0: break;\n" +
                      "                }\n" +
                      "                break;\n" +
                      "        }\n" +
                      "    }\n" +
                      "}\n";
        String golden = "public class Test {\n" +
                        "    void m(int p) {\n" +
                        "        switch (p) {\n" +
                        "            case 0 -> {\n" +
                        "                switch (p) {\n" +
                        "                    case 0 -> {\n" +
                        "                        break;\n" +
                        "                    }\n" +
                        "                }\n" +
                        "                break;\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n";
        TestUtilities.copyStringToFile(testFile, test.replace("|", ""));
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy copy) throws IOException {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                final TreeMaker make = copy.getTreeMaker();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override public Void visitCase(CaseTree node, Void p) {
                        copy.rewrite(getCurrentPath().getLeaf(),
                                     make.Case(node.getExpressions(), make.Block(node.getStatements(), false)));
                        return super.visitCase(node, p);
                    }
                }.scan(copy.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    //github issue 8296:
    public void testPatternSwitch1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        String test = """
                      public class Test {
                          void m(Object o) {
                              switch (o) {
                                  case String s -> s.getClass();
                              }
                          }
                      }
                      """;
        String golden = """
                        public class Test {
                            void m(Object o) {
                                switch (o) {
                                    case String nue -> nue.getClass();
                                    default -> {
                                    }
                                }
                            }
                        }
                        """;
        TestUtilities.copyStringToFile(testFile, test.replace("|", ""));
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy copy) throws IOException {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                final TreeMaker make = copy.getTreeMaker();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitVariable(VariableTree node, Void p) {
                        if (node.getName().contentEquals("s")) {
                            copy.rewrite(node, make.setLabel(node, "nue"));
                        }
                        return super.visitVariable(node, p);
                    }
                    @Override
                    public Void visitIdentifier(IdentifierTree node, Void p) {
                        if (node.getName().contentEquals("s")) {
                            copy.rewrite(node, make.setLabel(node, "nue"));
                        }
                        return super.visitIdentifier(node, p);
                    }
                    @Override
                    public Void visitSwitch(SwitchTree node, Void p) {
                        copy.rewrite(node, make.addSwitchCase(node, make.Case(List.of(), make.Block(List.of(), false))));
                        return super.visitSwitch(node, p);
                    }
                }.scan(copy.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    // XXX I don't understand what these are used for
    @Override
    String getSourcePckg() {
        return "";
    }

    @Override
    String getGoldenPckg() {
        return "";
    }
}
