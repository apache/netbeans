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
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.StringTemplateTree;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.JCTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
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

public class StringTemplateTest extends TreeRewriteTestBase {

    private static final List<String> EXTRA_OPTIONS = new ArrayList<>();

    public StringTemplateTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(StringTemplateTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sourceLevel = "1.21";
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
        EXTRA_OPTIONS.add("--enable-preview");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = false;

    }

    @ServiceProvider(service = CompilerOptionsQueryImplementation.class, position = 100)
    public static class TestCompilerOptionsQueryImplementation implements CompilerOptionsQueryImplementation {

        @Override
        public CompilerOptionsQueryImplementation.Result getOptions(FileObject file) {
            return new CompilerOptionsQueryImplementation.Result() {
                @Override
                public List<? extends String> getArguments() {
                    return EXTRA_OPTIONS;
                }

                @Override
                public void addChangeListener(ChangeListener listener) {
                }

                @Override
                public void removeChangeListener(ChangeListener listener) {
                }
            };
        }

    }

    public void testRenameInStringTemplate() throws Exception {
        String code = "package test; \n"
                + "public class Test {\n"
                + "     private String test(int a, int b) {\n"
                + "         return STR.\"\\{a}\";\n"
                + "     }\n"
                + "}\n";
        String golden = "package test; \n"
                + "public class Test {\n"
                + "     private String test(int a, int b) {\n"
                + "         return FMT.\"\\{b}\";\n"
                + "     }\n"
                + "}\n";

        prepareTest("Test", code);

        JavaSource js = getJavaSource();
        assertNotNull(js);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                new TreeScanner<Void, Void>() {
                    @Override
                    public Void visitIdentifier(IdentifierTree node, Void p) {
                        if (node.getName().contentEquals("a")) {
                            workingCopy.rewrite(node, make.Identifier("b"));
                            return null;
                        }
                        if (node.getName().contentEquals("STR")) {
                            workingCopy.rewrite(node, make.Identifier("FMT"));
                            return null;
                        }
                        return super.visitIdentifier(node, p);
                    }
                }.scan(cut, null);
            }

        };

        js.runModificationTask(task).commit();

        String res = TestUtilities.copyFileToString(getTestFile());
        //System.err.println(res);
        assertEquals(golden, res);

    }

    public void testRemoveStringTemplateFragment() throws Exception {
        String code = "package test; \n"
                + "public class Test {\n"
                + "     private String test(int a, int b, int c) {\n"
                + "         String s1 = STR.\"a\\{a}b\\{b}c\\{c}e\";\n"
                + "         String s2 = STR.\"a\\{a}b\\{b}c\\{c}e\";\n"
                + "         String s3 = STR.\"a\\{a}b\\{b}c\\{c}e\";\n"
                + "     }\n"
                + "}\n";
        String golden = "package test; \n"
                + "public class Test {\n"
                + "     private String test(int a, int b, int c) {\n"
                + "         String s1 = STR.\"b\\{b}c\\{c}e\";\n"
                + "         String s2 = STR.\"a\\{a}c\\{c}e\";\n"
                + "         String s3 = STR.\"a\\{a}b\\{b}e\";\n"
                + "     }\n"
                + "}\n";

        prepareTest("Test", code);

        JavaSource js = getJavaSource();
        assertNotNull(js);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                new TreeScanner<Void, Void>() {
                    @Override
                    public Void visitVariable(VariableTree node, Void p) {
                        CONT: if (node.getInitializer() != null) {
                            int toDelete;
                            switch (node.getName().toString()) {
                                case "s1": toDelete = 0; break;
                                case "s2": toDelete = 1; break;
                                case "s3": toDelete = 2; break;
                                default: break CONT;
                            }

                            StringTemplateTree template = (StringTemplateTree) node.getInitializer();
                            List<String> fragments = new ArrayList<>(template.getFragments());
                            List<? extends ExpressionTree> expressions = new ArrayList<>(template.getExpressions());

                            fragments.remove(toDelete);
                            expressions.remove(toDelete);

                            workingCopy.rewrite(template, make.StringTemplate(template.getProcessor(), fragments, expressions));
                        }
                        return super.visitVariable(node, p);
                    }
                }.scan(cut, null);
            }

        };

        js.runModificationTask(task).commit();

        String res = TestUtilities.copyFileToString(getTestFile());
        //System.err.println(res);
        assertEquals(golden, res);

    }

    public void testAddStringTemplateFragment() throws Exception {
        String code = "package test; \n"
                + "public class Test {\n"
                + "     private String test(int a, int b, int c) {\n"
                + "         String s1 = STR.\"0\\{0}1\\{1}e\";\n"
                + "         String s2 = STR.\"0\\{0}1\\{1}e\";\n"
                + "         String s3 = STR.\"0\\{0}1\\{1}e\";\n"
                + "     }\n"
                + "}\n";
        String golden = "package test; \n"
                + "public class Test {\n"
                + "     private String test(int a, int b, int c) {\n"
                + "         String s1 = STR.\"a\\{a}0\\{0}1\\{1}e\";\n"
                + "         String s2 = STR.\"0\\{0}a\\{a}1\\{1}e\";\n"
                + "         String s3 = STR.\"0\\{0}1\\{1}a\\{a}e\";\n"
                + "     }\n"
                + "}\n";

        prepareTest("Test", code);

        JavaSource js = getJavaSource();
        assertNotNull(js);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                new TreeScanner<Void, Void>() {
                    @Override
                    public Void visitVariable(VariableTree node, Void p) {
                        CONT: if (node.getInitializer() != null) {
                            int insertPos;
                            switch (node.getName().toString()) {
                                case "s1": insertPos = 0; break;
                                case "s2": insertPos = 1; break;
                                case "s3": insertPos = 2; break;
                                default: break CONT;
                            }

                            StringTemplateTree template = (StringTemplateTree) node.getInitializer();
                            List<String> fragments = new ArrayList<>(template.getFragments());
                            List<ExpressionTree> expressions = new ArrayList<>(template.getExpressions());

                            fragments.add(insertPos, "a");
                            expressions.add(insertPos, make.Identifier("a"));

                            workingCopy.rewrite(template, make.StringTemplate(template.getProcessor(), fragments, expressions));
                        }
                        return super.visitVariable(node, p);
                    }
                }.scan(cut, null);
            }

        };

        js.runModificationTask(task).commit();

        String res = TestUtilities.copyFileToString(getTestFile());
        //System.err.println(res);
        assertEquals(golden, res);

    }

    public void testNewStringTemplate() throws Exception {
        String code = "package test; \n"
                + "public class Test {\n"
                + "     private String test(int a, int b, int c) {\n"
                + "         String s;\n"
                + "     }\n"
                + "}\n";
        String golden = "package test; \n"
                + "public class Test {\n"
                + "     private String test(int a, int b, int c) {\n"
                + "         String s = STR.\"a\\{a}b\\{b}c\\{c}e\";\n"
                + "     }\n"
                + "}\n";

        prepareTest("Test", code);

        JavaSource js = getJavaSource();
        assertNotNull(js);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                new TreeScanner<Void, Void>() {
                    @Override
                    public Void visitVariable(VariableTree node, Void p) {
                        if (node.getName().contentEquals("s")) {
                            List<String> fragments = new ArrayList<>();
                            List<ExpressionTree> expressions = new ArrayList<>();

                            fragments.add("a");
                            expressions.add(make.Identifier("a"));
                            fragments.add("b");
                            expressions.add(make.Identifier("b"));
                            fragments.add("c");
                            expressions.add(make.Identifier("c"));
                            fragments.add("e");

                            workingCopy.rewrite(node, make.setInitialValue(node, make.StringTemplate(make.Identifier("STR"), fragments, expressions)));
                        }
                        return super.visitVariable(node, p);
                    }
                }.scan(cut, null);
            }

        };

        js.runModificationTask(task).commit();

        String res = TestUtilities.copyFileToString(getTestFile());
        //System.err.println(res);
        assertEquals(golden, res);

    }

}
