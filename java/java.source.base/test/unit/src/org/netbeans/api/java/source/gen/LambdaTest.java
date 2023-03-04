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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberReferenceTree.ReferenceMode;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.prefs.Preferences;
import javax.lang.model.element.Modifier;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.ui.FmtOptions;

/**
 * Tests correct adding cast to statement.
 *
 * @author Pavel Flaska
 */
public class LambdaTest extends GeneratorTestMDRCompat {

    /** Creates a new instance of AddCastTest */
    public LambdaTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(LambdaTest.class);
        return suite;
    }

    public void testPrintMemberReference() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = null;\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = Test::taragui;\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitLiteral(LiteralTree node, Void p) {
                        workingCopy.rewrite(node, make.MemberReference(ReferenceMode.INVOKE, make.Identifier("Test"), "taragui", Collections.<ExpressionTree>emptyList()));
                        return super.visitLiteral(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testBasicLambdaDiff() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> {};\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (f) -> {};\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitVariable(VariableTree node, Void p) {
                        if (node.getName().contentEquals("e")) {
                            workingCopy.rewrite(node, make.setLabel(node, "f"));
                        }
                        return super.visitVariable(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testImplicitLambdaParam() throws Exception {
        for (boolean parens : new boolean[] {false, true}) {
            testFile = new File(getWorkDir(), "Test.java");
            TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public static void taragui() {\n" +
                "        ChangeListener l;\n" +
                "    }\n" +
                "}\n"
                );
            String golden =
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public static void taragui() {\n" +
                (parens ? "        ChangeListener l = (e) -> System.err.println();\n"
                        : "        ChangeListener l = e -> System.err.println();\n") +
                "    }\n" +
                "}\n";
            JavaSource src = getJavaSource(testFile);

            Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);

            try {
                preferences.putBoolean(FmtOptions.parensAroundSingularLambdaParam, parens);

                Task<WorkingCopy> task = new Task<WorkingCopy>() {

                    public void run(final WorkingCopy workingCopy) throws IOException {
                        workingCopy.toPhase(Phase.RESOLVED);
                        final TreeMaker make = workingCopy.getTreeMaker();
                        new ErrorAwareTreeScanner<Void, Void>() {
                            @Override
                            public Void visitVariable(VariableTree node, Void p) {
                                ExpressionTree stat = make.MethodInvocation(Collections.emptyList(), make.MemberSelect(make.MemberSelect(make.QualIdent("java.lang.System"), "err"), "println"), Collections.emptyList());
                                LambdaExpressionTree lambda = make.LambdaExpression(Collections.singletonList(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "e", null, null)), stat);
                                workingCopy.rewrite(node, make.Variable(node.getModifiers(), node.getName(), node.getType(), lambda));
                                return super.visitVariable(node, p);
                            }
                        }.scan(workingCopy.getCompilationUnit(), null);
                    }

                };
                src.runModificationTask(task).commit();
                String res = TestUtilities.copyFileToString(testFile);
                //System.err.println(res);
                assertEquals(golden, res);
            } finally {
                preferences.remove(FmtOptions.parensAroundSingularLambdaParam);
            }
        }
    }

    public void testExplicitLambdaParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l;\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (String e) -> System.err.println();\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override
                    public Void visitVariable(VariableTree node, Void p) {
                        ExpressionTree stat = make.MethodInvocation(Collections.emptyList(), make.MemberSelect(make.MemberSelect(make.QualIdent("java.lang.System"), "err"), "println"), Collections.emptyList());
                        LambdaExpressionTree lambda = make.LambdaExpression(Collections.singletonList(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "e", make.Type("java.lang.String"), null)), stat);
                        workingCopy.rewrite(node, make.Variable(node.getModifiers(), node.getName(), node.getType(), lambda));
                        return super.visitVariable(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddFirstLambdaParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = () -> {};\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> {};\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        workingCopy.rewrite(node, make.addLambdaParameter(node, make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "e", null, null)));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddSecondLambdaParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> {};\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e, f) -> {};\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        workingCopy.rewrite(node, make.addLambdaParameter(node, make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "f", null, null)));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddSecondLambdaParamNoParenthesis() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = e -> {};\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e, f) -> {};\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        workingCopy.rewrite(node, make.addLambdaParameter(node, make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "f", null, null)));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testPrependSecondLambdaParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> {};\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (f, e) -> {};\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        workingCopy.rewrite(node, make.insertLambdaParameter(node, 0, make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "f", null, null)));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveFirstLambdaParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e, f) -> {};\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (f) -> {};\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        workingCopy.rewrite(node, make.removeLambdaParameter(node, 0));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveSecondLambdaParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e, f) -> {};\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> {};\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        workingCopy.rewrite(node, make.removeLambdaParameter(node, 1));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testOnlyLambdaParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> {};\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = () -> {};\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        workingCopy.rewrite(node, make.removeLambdaParameter(node, 0));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testLambdaFullBody2Expression() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> {return 1;};\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> 1;\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        ReturnTree t = (ReturnTree) ((BlockTree) node.getBody()).getStatements().get(0);
                        workingCopy.rewrite(node, make.setLambdaBody(node, t.getExpression()));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testLambdaExpression2FullBody() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> 1;\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> {\n" +
            "            return 1;\n" +
            "        };\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        workingCopy.rewrite(node, make.setLambdaBody(node, make.Block(Collections.singletonList(make.Return((ExpressionTree) node.getBody())), false)));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testLambdaExpression2FullBodyTreeMatch() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "import java.util.Collections;\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Collections.sort(list, (l, r) -> l.compareTo(r));\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.Collections;\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Collections.sort(list, (l, r) -> {\n" +
            "            return l.compareTo(r);\n" +
            "        });\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        workingCopy.rewrite(node, make.setLambdaBody(node, make.Block(Collections.singletonList(make.Return((ExpressionTree) node.getBody())), false)));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testMethodReferenceDiff() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = hierbas.del.litoral.Test :: taragui;\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = Test :: taragui;\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitMemberReference(MemberReferenceTree node, Void p) {
                        workingCopy.rewrite(node, make.MemberReference(node.getMode(), make.Identifier("Test"), node.getName(), node.getTypeArguments()));
                        return super.visitMemberReference(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testMethodReferenceNameDiff() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = Test :: taragui;\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = Test :: taragui2;\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitMemberReference(MemberReferenceTree node, Void p) {
                        workingCopy.rewrite(node, make.setLabel(node, "taragui2"));
                        return super.visitMemberReference(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testMethodReferenceFirstTypeParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = Test::taragui;\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = Test::<String>taragui;\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitMemberReference(MemberReferenceTree node, Void p) {
                        workingCopy.rewrite(node, make.MemberReference(node.getMode(), node.getQualifierExpression(), node.getName(), Collections.singletonList(make.Identifier("String"))));
                        return super.visitMemberReference(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testMethodReferenceLastTypeParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = Test::<String>taragui;\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = Test::taragui;\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitMemberReference(MemberReferenceTree node, Void p) {
                        workingCopy.rewrite(node, make.MemberReference(node.getMode(), node.getQualifierExpression(), node.getName(), null));
                        return super.visitMemberReference(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testLambdaExpressionImplicit2ExplicitParamTypes() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "import java.util.Collections;\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Collections.sort(list, (l, r) -> l.compareTo(r));\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.Collections;\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Collections.sort(list, (String l, String r) -> l.compareTo(r));\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        for (VariableTree par : node.getParameters()) {
                            workingCopy.rewrite(par.getType(), make.Identifier("String"));
                        }
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testLambdaExpressionExplicit2ImplicitParamTypes() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "import java.util.Collections;\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Collections.sort(list, (String l, String r) -> l.compareTo(r));\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.Collections;\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Collections.sort(list, (l, r) -> l.compareTo(r));\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        for (VariableTree par : node.getParameters()) {
                            workingCopy.rewrite(par, make.Variable(par.getModifiers(), par.getName(), null, par.getInitializer()));
                        }
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * Replacing the statement inside lambda without parenthesis used to double curly braces surrounding lambda body
     */
    public void testChangeLambdaWithoutParenthesis236244() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "import javax.swing.JButton;\n" +
            "public class LambdaTest {\n" +
            "    private JButton jb = new JButton();\n" +
            "    private String onActionX;\n" +
            "    private static String f(ActionEvent e) {\n" +
            "        return e.paramString();\n" +
            "    }\n" +
            "    public void test() {\n" +
            "        jb.addActionListener(e -> { \n" +
            "            onActionX = e.paramString();\n" +
            "        });\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import javax.swing.JButton;\n" +
            "public class LambdaTest {\n" +
            "    private JButton jb = new JButton();\n" +
            "    private String onActionX;\n" +
            "    private static String f(ActionEvent e) {\n" +
            "        return e.paramString();\n" +
            "    }\n" +
            "    public void test() {\n" +
            "        jb.addActionListener(e -> { \n" +
            "            onActionX = f(e);\n" +
            "        });\n" +
            "    }\n" +
            "\n" +
            "    void bu() {\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker mk = workingCopy.getTreeMaker();
                int pos = workingCopy.getSnapshot().getText().toString().lastIndexOf("e.paramString()");
                TreePath identP = workingCopy.getTreeUtilities().pathFor(pos + 1);
                TreePath invPath = identP.getParentPath().getParentPath();
                MethodInvocationTree repl = mk.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(),
                        mk.Identifier("f"),
                        Collections.singletonList(mk.Identifier("e")));
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree mt = mk.Method(
                        mk.Modifiers(Collections.<Modifier>emptySet()),
                        "bu",
                        mk.Type("void"),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        mk.Block(Collections.<StatementTree>emptyList(), false),
                        null);
                workingCopy.rewrite(clazz, mk.addClassMember(clazz, mt));
                workingCopy.rewrite(invPath.getLeaf(), repl);
            }

        };
        src.runModificationTask(task).commit();
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

   public void testAddSecondLambdaParamWithType() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> {};\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e, f) -> {};\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        VariableTree vt = node.getParameters().get(0);
                        workingCopy.rewrite(node, make.addLambdaParameter(node, make.Variable(vt.getModifiers(), "f", vt.getType(), vt.getInitializer())));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeLambdaToImplicit() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (ChangeEvent e) -> {};\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> {};\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override
                    public Void scan(Tree node, Void p) {
                        return super.scan(node, p);
                    }
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        VariableTree vt = node.getParameters().get(0);
                        workingCopy.rewrite(node,
                                make.LambdaExpression(
                                    Collections.singletonList(
                                        make.Variable(
                                            make.Modifiers(Collections.<Modifier>emptySet()),
                                            vt.getName(), null, null)),
                                    node.getBody())
                        );
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeLambdaToExplicit() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> {};\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (ChangeEvent e) -> {};\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {

                    @Override
                    public Void scan(Tree node, Void p) {
                        return super.scan(node, p);
                    }

                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        VariableTree vt = node.getParameters().get(0);
                        workingCopy.rewrite(node,
                                make.LambdaExpression(
                                    Collections.singletonList(
                                        make.Variable(
                                            make.Modifiers(Collections.<Modifier>emptySet()),
                                            vt.getName(),
                                            make.Type("ChangeEvent"), null)),
                                    node.getBody())
                        );
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddReturnWithCommentsImported() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package test;\n" +
            "import java.util.Collection;\n" +
            "import java.util.concurrent.Callable;\n" +
            "public class Test {\n" +
            "    public static void test() {\n" +
            "        Callable<String> c = () -> {\n" +
            "        };\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package test;\n" +
            "import java.util.Collection;\n" +
            "import java.util.concurrent.Callable;\n" +
            "public class Test {\n" +
            "    public static void test() {\n" +
            "        Callable<String> c = () -> {\n" +
            "            return null;\n" +
            "        };\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {

                    @Override
                    public Void scan(Tree node, Void p) {
                        return super.scan(node, p);
                    }

                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        GeneratorUtilities.get(workingCopy).importComments(node, workingCopy.getCompilationUnit());
                        BlockTree block = (BlockTree) node.getBody();
                        workingCopy.rewrite(block, make.addBlockStatement(block, make.Return(make.Literal(null))));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddReturnWithCommentsImported2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package test;\n" +
            "import java.util.Collection;\n" +
            "import java.util.concurrent.Callable;\n" +
            "public class Test {\n" +
            "    public static void test() {\n" +
            "        Callable<String> c = () -> {};\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package test;\n" +
            "import java.util.Collection;\n" +
            "import java.util.concurrent.Callable;\n" +
            "public class Test {\n" +
            "    public static void test() {\n" +
            "        Callable<String> c = () -> {\n" +
            "            return null;\n" +
            "        };\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {

                    @Override
                    public Void scan(Tree node, Void p) {
                        return super.scan(node, p);
                    }

                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        GeneratorUtilities.get(workingCopy).importComments(node, workingCopy.getCompilationUnit());
                        BlockTree block = (BlockTree) node.getBody();
                        workingCopy.rewrite(block, make.addBlockStatement(block, make.Return(make.Literal(null))));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
}
