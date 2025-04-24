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

import com.sun.source.tree.*;
import com.sun.source.tree.Tree.Kind;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import org.junit.Test;
import org.netbeans.api.java.source.*;
import org.netbeans.junit.NbTestSuite;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.Modifier;

/**
 * This test verifies issues from netbeans issueszila about rewriting trees.
 *
 * @author Rastislav Komara (<a href="mailto:moonko@netbeans.org">RKo</a>)
 */
public class RewriteOccasionalStatements extends GeneratorTestBase {
    private static final String TEST_CONTENT = "\n" +
            "public class NewArrayTest {\n" +
            "\n" +
            "int[] test = new int[3];" +
            "}\n";

    public RewriteOccasionalStatements(String aName) {
        super(aName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(RewriteOccasionalStatements.class);
        return suite;
    }

    @Test
    public void test158337regresion1() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, TEST_CONTENT);
        String golden = "\n" +
                "public class NewArrayTest {\n" +
                "\n" +
                "int[] test = new int[5];" +
                "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                Tree node = extractOriginalNode(cut);
                TreeMaker make = workingCopy.getTreeMaker();
                List<ExpressionTree> init = new ArrayList<ExpressionTree>();
                init.add(make.Literal(5));
                ExpressionTree modified = make.NewArray(
                        make.PrimitiveType(TypeKind.INT),
                        init, new ArrayList<ExpressionTree>());
                System.out.println("original: " + node);
                System.out.println("modified: " + modified);
                workingCopy.rewrite(node, modified);
            }

        };

        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
        assertEquals(golden, res);
    }


    @Test
    public void test158337regresion2() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "\n" +
                        "public class NewArrayTest {\n" +
                        "\n" +
                        "int[] test = new int[]{1,2,3};" +
                        "}\n");
        String golden = "\n" +
                "public class NewArrayTest {\n" +
                "\n" +
                "int[] test = new int[]{4,5,6};" +
                "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                Tree node = extractOriginalNode(cut);
                TreeMaker make = workingCopy.getTreeMaker();
                List<ExpressionTree> init = new ArrayList<ExpressionTree>();
                init.add(make.Literal(4));
                init.add(make.Literal(5));
                init.add(make.Literal(6));
                ExpressionTree modified = make.NewArray(
                        make.PrimitiveType(TypeKind.INT),
                        new ArrayList<ExpressionTree>(),
                        init);
                System.out.println("original: " + node);
                System.out.println("modified: " + modified);
                workingCopy.rewrite(node, modified);
            }

        };

        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
        assertEquals(golden, res);
    }


    @Test
    public void test158337() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, TEST_CONTENT);
        String golden = "\n" +
                "public class NewArrayTest {\n" +
                "\n" +
                "int[] test = new int[]{1, 2, 3};" +
                "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                Tree node = extractOriginalNode(cut);
                TreeMaker make = workingCopy.getTreeMaker();
                List<ExpressionTree> init = new ArrayList<ExpressionTree>();
                init.add(make.Literal(1));
                init.add(make.Literal(2));
                init.add(make.Literal(3));
                ExpressionTree modified = make.NewArray(
                        make.PrimitiveType(TypeKind.INT),
                        new ArrayList<ExpressionTree>(),
                        init);
                System.out.println("original: " + node);
                System.out.println("modified: " + modified);
                workingCopy.rewrite(node, modified);
            }
        };

        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
        assertEquals(golden, res);
    }

    private Tree extractOriginalNode(CompilationUnitTree cut) {
        List<? extends Tree> classes = cut.getTypeDecls();
        if (!classes.isEmpty()) {
            ClassTree clazz = (ClassTree) classes.get(0);
            List<? extends Tree> trees = clazz.getMembers();
//                    System.out.println("Trees:" + trees);
            if (trees.size() == 2) {
                VariableTree tree = (VariableTree) trees.get(1);
                return tree.getInitializer();
            }
        }

        throw new IllegalStateException("There is no array declaration in expected place.");

    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

    public void testExtractInterface117986() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        String source = "public class ExtractSuperInterface implements MyInterface1, MyInterface2, MyInterface3 {\n" +
                "}\n";
        TestUtilities.copyStringToFile(testFile, source);
        String golden = "public class ExtractSuperInterface implements SuperInterface {\n" +
                "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                Elements elements = wc.getElements();
                TypeElement typeElement = elements.getTypeElement("ExtractSuperInterface");
                ElementHandle<TypeElement> sourceType = ElementHandle.<TypeElement>create(typeElement);
                TypeElement clazz = sourceType.resolve(wc);
                assert clazz != null;
                ClassTree classTree = wc.getTrees().getTree(clazz);
                TreeMaker maker = wc.getTreeMaker();
                // fake interface since interface file does not exist yet
                Tree interfaceTree = maker.Identifier("SuperInterface");

                // filter out obsolete members
                List<Tree> members2Add = new ArrayList<Tree>();
                // filter out obsolete implements trees
                List<Tree> impls2Add = Collections.singletonList(interfaceTree);

                ClassTree nc;
                if (clazz.getKind() == ElementKind.CLASS) {
                    nc = maker.Class(
                            classTree.getModifiers(),
                            classTree.getSimpleName(),
                            classTree.getTypeParameters(),
                            classTree.getExtendsClause(),
                            impls2Add,
                            members2Add);
                } else if (clazz.getKind() == ElementKind.INTERFACE) {
                    nc = maker.Interface(
                            classTree.getModifiers(),
                            classTree.getSimpleName(),
                            classTree.getTypeParameters(),
                            impls2Add,
                            members2Add);
                } else if (clazz.getKind() == ElementKind.ENUM) {
                    nc = maker.Enum(
                            classTree.getModifiers(),
                            classTree.getSimpleName(),
                            impls2Add,
                            members2Add);
                } else if (clazz.getKind() == ElementKind.RECORD) {
                    nc = maker.Record(
                            classTree.getModifiers(),
                            classTree.getSimpleName(),
                            classTree.getTypeParameters(),
                            impls2Add,
                            members2Add);
                } else {
                    throw new IllegalStateException(classTree.toString());
                }

                wc.rewrite(classTree, nc);
            }
        };

        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
        assertEquals("Golden and result does not match", golden, res);
    }

    public void testExtractInterfaceRegresion1() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        String source = "public class ExtractSuperInterface implements MyInterface1 {\n" +
                "}\n";
        TestUtilities.copyStringToFile(testFile, source);
        String golden = "public class ExtractSuperInterface implements SuperInterface {\n" +
                "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                Elements elements = wc.getElements();
                TypeElement typeElement = elements.getTypeElement("ExtractSuperInterface");
                ElementHandle<TypeElement> sourceType = ElementHandle.<TypeElement>create(typeElement);
                TypeElement clazz = sourceType.resolve(wc);
                assert clazz != null;
                ClassTree classTree = wc.getTrees().getTree(clazz);
                TreeMaker maker = wc.getTreeMaker();
                // fake interface since interface file does not exist yet
                Tree interfaceTree = maker.Identifier("SuperInterface");

                // filter out obsolete members
                List<Tree> members2Add = new ArrayList<Tree>();
                // filter out obsolete implements trees
                List<Tree> impls2Add = Collections.singletonList(interfaceTree);

                ClassTree nc;
                if (clazz.getKind() == ElementKind.CLASS) {
                    nc = maker.Class(
                            classTree.getModifiers(),
                            classTree.getSimpleName(),
                            classTree.getTypeParameters(),
                            classTree.getExtendsClause(),
                            impls2Add,
                            members2Add);
                } else if (clazz.getKind() == ElementKind.INTERFACE) {
                    nc = maker.Interface(
                            classTree.getModifiers(),
                            classTree.getSimpleName(),
                            classTree.getTypeParameters(),
                            impls2Add,
                            members2Add);
                } else if (clazz.getKind() == ElementKind.ENUM) {
                    nc = maker.Enum(
                            classTree.getModifiers(),
                            classTree.getSimpleName(),
                            impls2Add,
                            members2Add);
                } else if (clazz.getKind() == ElementKind.RECORD) {
                    nc = maker.Record(
                            classTree.getModifiers(),
                            classTree.getSimpleName(),
                            classTree.getTypeParameters(),
                            impls2Add,
                            members2Add);
                } else {
                    throw new IllegalStateException(classTree.toString());
                }

                wc.rewrite(classTree, nc);
            }
        };

        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
        assertEquals("Golden and result does not match", golden, res);
    }

    public void XtestExtractInterfaceRegresion2() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        String source = "public class ExtractSuperInterface implements MyInterface1, MyInterface2 {\n" +
                "}\n";
        TestUtilities.copyStringToFile(testFile, source);
        String golden = "public class ExtractSuperInterface implements SuperInterface {\n" +
                "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                Elements elements = wc.getElements();
                TypeElement typeElement = elements.getTypeElement("ExtractSuperInterface");
                ElementHandle<TypeElement> sourceType = ElementHandle.<TypeElement>create(typeElement);
                TypeElement clazz = sourceType.resolve(wc);
                assert clazz != null;
                ClassTree classTree = wc.getTrees().getTree(clazz);
                TreeMaker maker = wc.getTreeMaker();
                // fake interface since interface file does not exist yet
                Tree interfaceTree = maker.Identifier("SuperInterface");

                // filter out obsolete members
                List<Tree> members2Add = new ArrayList<Tree>();
                // filter out obsolete implements trees
                List<Tree> impls2Add = Collections.singletonList(interfaceTree);

                ClassTree nc;
                if (clazz.getKind() == ElementKind.CLASS) {
                    nc = maker.Class(
                            classTree.getModifiers(),
                            classTree.getSimpleName(),
                            classTree.getTypeParameters(),
                            classTree.getExtendsClause(),
                            impls2Add,
                            members2Add);
                } else if (clazz.getKind() == ElementKind.INTERFACE) {
                    nc = maker.Interface(
                            classTree.getModifiers(),
                            classTree.getSimpleName(),
                            classTree.getTypeParameters(),
                            impls2Add,
                            members2Add);
                } else if (clazz.getKind() == ElementKind.ENUM) {
                    nc = maker.Enum(
                            classTree.getModifiers(),
                            classTree.getSimpleName(),
                            impls2Add,
                            members2Add);
                } else if (clazz.getKind() == ElementKind.RECORD) {
                    nc = maker.Record(
                            classTree.getModifiers(),
                            classTree.getSimpleName(),
                            classTree.getTypeParameters(),
                            impls2Add,
                            members2Add);
                } else {
                    throw new IllegalStateException(classTree.toString());
                }

                wc.rewrite(classTree, nc);
            }
        };

        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
        assertEquals("Golden and result does not match", golden, res);
    }


/*  This issue has been waived for yet.
    @Test
    public void test159941() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "\n" +
                        "public class NewArrayTest {\n" +
                        "   void m4(int[] p) {\n" +
                        "        if (p[0] > 0) {\n" +
                        "            if (p[1] > 0) {\n" +
                        "                System.out.println(\"x\");\n" +
                        "            }\n" +
                        "            if (p[1] > 0) {\n" +
                        "                System.out.println(\"y\");\n" +
                        "            }\n" +
                        "        }\n" +
                        "        if (p[0] > 0) {\n" +
                        "            if (p[1] > 0) {\n" +
                        "                System.out.println(\"z\");\n" +
                        "            }\n" +
                        "            if (p[1] > 0) {\n" +
                        "                System.out.println(\"w\");\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }" +
                        "}\n");
        String golden = "\n" +
                "public class NewArrayTest {\n" +
                "   void m4(int[] p) {\n" +
                "        if (p[0] > 0) {\n" +
                "            if (p[1] > 0) {\n" +
                "                System.out.println(\"x\");\n" +
                "            }\n" +
                "        }\n" +
                "    }" +
                "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(JavaSource.Phase.RESOLVED);                
                SimpleScanner ss = new SimpleScanner(wc);
                ss.scan(wc.getCompilationUnit().getTypeDecls().get(0), null);
            }

        };

        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
        assertEquals(golden, res);
    }
*/


    class SimpleScanner extends ErrorAwareTreeScanner<Void, Void> {
        private final WorkingCopy wc;
        protected GeneratorUtilities gu;

        SimpleScanner(WorkingCopy wc) {
            this.wc = wc;
            gu = GeneratorUtilities.get(this.wc);
        }

        @Override
        public Void visitBlock(BlockTree node, Void p) {
            List<? extends StatementTree> st = node.getStatements();
            if (st.size() == 2) {
                List<StatementTree> st2 = new ArrayList<StatementTree>();
                st2.add(st.get(0));
                TreeMaker make = wc.getTreeMaker();
                BlockTree modified = make.Block(st2, node.isStatic());
                modified = gu.importFQNs(modified);                         
                System.out.println("original: " + node);
                System.out.println("modified: " + modified);
                wc.rewrite(node, modified);
            }
            return super.visitBlock(node, p);
        }

    }

    @Test
    public void test159492() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
                "public class Test {\n" +
                "    private void test() {\n" +
                "        String[] a = new String[a().length()];\n" +
                "    }\n" +
                "}\n");
        String golden = 
                "public class Test {\n" +
                "    private void test() {\n" +
                "        String v = null;\n" +
                "        String[] a = new String[v.length()];\n" +
                "    }\n" +
                "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override
                    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
                        if (node.getMethodSelect().getKind() == Kind.IDENTIFIER && ((IdentifierTree) node.getMethodSelect()).getName().contentEquals("a")) {
                            workingCopy.rewrite(node, make.Identifier("v"));
                            return null;
                        }
                        return super.visitMethodInvocation(node, p);
                    }
                    @Override
                    public Void visitMethod(MethodTree node, Void p) {
                        if (node.getName().contentEquals("test")) {
                            ExpressionTree type = make.QualIdent(workingCopy.getElements().getTypeElement("java.lang.String"));
                            VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "v", type, make.Literal(null));
                            workingCopy.rewrite(node.getBody(), make.insertBlockStatement(node.getBody(), 0, vt));
                        }
                        return super.visitMethod(node, p);
                    }
                }.scan(cut, null);
            }
        };

        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
        assertEquals(golden, res);
    }
}
