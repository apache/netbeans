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

import com.sun.source.util.TreePath;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import com.sun.source.tree.*;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.*;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.ui.FmtOptions;
import static org.netbeans.api.java.source.JavaSource.*;

/**
 *
 * @author Pavel Flaska, Jan Becicka
 */
public class RefactoringRegressionsTest extends GeneratorTestMDRCompat {

    public RefactoringRegressionsTest(String aName) {
        super(aName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(RefactoringRegressionsTest.class);
//        suite.addTest(new RefactoringRegressionsTest("testRenameTypeParameterInInvocation"));
//        suite.addTest(new RefactoringRegressionsTest("testRenameInNewClassExpressionWithSpaces"));
//        suite.addTest(new RefactoringRegressionsTest("testMoveEmptyReturnStatement"));
//        suite.addTest(new RefactoringRegressionsTest("testAddNewClassInvocParameter1"));
//        suite.addTest(new RefactoringRegressionsTest("testAddNewClassInvocParameter2"));
//        suite.addTest(new RefactoringRegressionsTest("test121181"));
//        suite.addTest(new RefactoringRegressionsTest("test117913"));
//        suite.addTest(new RefactoringRegressionsTest("testDefaultAnnotationAttributeValue121873"));
//        suite.addTest(new RefactoringRegressionsTest("testSpaceAfterComma1"));
        return suite;
    }

    /**
     * http://www.netbeans.org/issues/show_bug.cgi?id=111981
     */
    public void testRenameTypeParameterInInvocation() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.Arrays;\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class Mnozina {\n" +
            "    \n" +
            "    static enum Prvek {\n" +
            "        PrvniPrvek,\n" +
            "        DruhyPrvek;\n" +
            "    }\n" +
            "    \n" +
            "    void metoda() {\n" +
            "        List<Prvek> required = new ArrayList<Prvek>();\n" +
            "        required.addAll(Arrays.<Prvek>asList());\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.Arrays;\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class Mnozina {\n" +
            "    \n" +
            "    static enum Unit {\n" +
            "        PrvniPrvek,\n" +
            "        DruhyPrvek;\n" +
            "    }\n" +
            "    \n" +
            "    void metoda() {\n" +
            "        List<Unit> required = new ArrayList<Unit>();\n" +
            "        required.addAll(Arrays.<Unit>asList());\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                ClassTree innerClazz = (ClassTree) clazz.getMembers().get(1);
                MethodTree method = (MethodTree) clazz.getMembers().get(2);
                workingCopy.rewrite(innerClazz, make.setLabel(innerClazz, "Unit"));
                
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);
                ParameterizedTypeTree ptt = (ParameterizedTypeTree) var.getType();
                IdentifierTree ident = (IdentifierTree) ptt.getTypeArguments().get(0);
                workingCopy.rewrite(ident, make.Identifier("Unit"));
                
                NewClassTree nct = (NewClassTree) var.getInitializer();
                ptt = (ParameterizedTypeTree) nct.getIdentifier();
                ident = (IdentifierTree) ptt.getTypeArguments().get(0);
                workingCopy.rewrite(ident, make.Identifier("Unit"));
                
                ExpressionStatementTree stat = (ExpressionStatementTree) method.getBody().getStatements().get(1);
                MethodInvocationTree mit = (MethodInvocationTree) stat.getExpression();
                mit = (MethodInvocationTree) mit.getArguments().get(0);
                ident = (IdentifierTree) mit.getTypeArguments().get(0);
                workingCopy.rewrite(ident, make.Identifier("Unit"));
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRenameMethodWithNewLine() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package javaapplication1;\n"
                + "\n"
                + "public class A {\n"
                + "    public static void main(String[] args) {\n"
                + "        new A().\n"
                + "            myMethod();\n"
                + "    }\n"
                + "\n"
                + "    public void myMethod() { }\n"
                + "}\n"
                );
        String golden
                = "package javaapplication1;\n"
                + "\n"
                + "public class A {\n"
                + "    public static void main(String[] args) {\n"
                + "        new A().yourMethod();\n"
                + "    }\n"
                + "\n"
                + "    public void yourMethod() { }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree myMethod = (MethodTree) clazz.getMembers().get(2);
                workingCopy.rewrite(myMethod, make.setLabel(myMethod, "yourMethod"));
                
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree est = (ExpressionStatementTree) method.getBody().getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                MemberSelectTree mst = (MemberSelectTree) mit.getMethodSelect();
                workingCopy.rewrite(mst, make.setLabel(mst, "yourMethod"));
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * http://www.netbeans.org/issues/show_bug.cgi?id=111966
     */
    public void testRenameInNewClassExpressionWithSpaces() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "public class A{\n" +
            "	A	( ){};\n" +
            "};\n" +
            "\n" +
            "class C{\n" +
            "	void s(){\n" +
            "	new javaapplication1 . A ( );\n" +
            "	}\n" +
            "};\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "public class B{\n" +
            "	B	( ){};\n" +
            "};\n" +
            "\n" +
            "class C{\n" +
            "	void s(){\n" +
            "	new javaapplication1 . B ( );\n" +
            "	}\n" +
            "};\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(0);
                workingCopy.rewrite(clazz, make.setLabel(clazz, "B"));
                workingCopy.rewrite(method, make.setLabel(method, "B"));
                
                method = (MethodTree) ((ClassTree) cut.getTypeDecls().get(2)).getMembers().get(1);
                ExpressionStatementTree est = (ExpressionStatementTree) method.getBody().getStatements().get(0);
                NewClassTree nct = (NewClassTree) est.getExpression();
                MemberSelectTree mst = (MemberSelectTree) nct.getIdentifier();
                workingCopy.rewrite(mst, make.setLabel(mst, "B"));
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * http://www.netbeans.org/issues/show_bug.cgi?id=111769
     */
    public void testMoveEmptyReturnStatement() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.Arrays;\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class Mnozina {\n" +
            "    \n" +
            "    void metoda() {\n" +
            "        List<Prvek> required = new ArrayList<Prvek>();\n" +
            "        return;\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.Arrays;\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class Mnozina {\n" +
            "    \n" +
            "    void metoda() {\n" +
            "        return;\n" +
            "    }\n" +
            "\n" +
            "    void m() {\n" +
            "        List<Prvek> required = new ArrayList<Prvek>();\n" +
            "        return;\n" +
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
                MethodTree nju = make.Method(
                        make.Modifiers(Collections.<Modifier>emptySet()),
                        "m",
                        make.PrimitiveType(TypeKind.VOID), // return type - void
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        make.Block(method.getBody().getStatements(), false),
                        null // default value - not applicable
                );
                workingCopy.rewrite(clazz, make.addClassMember(clazz, nju));
                workingCopy.rewrite(method.getBody(), make.Block(Collections.<StatementTree>singletonList(make.Return(null)), false));
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
      * http://java.netbeans.org/issues/show_bug.cgi?id=117326
     */
    public void testAddNewClassInvocParameter1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.Arrays;\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class Mnozina {\n" +
            "    \n" +
            "    void metoda(int a) {\n" +
            "        List l = new ArrayList();\n" +
            "        return;\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.Arrays;\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class Mnozina {\n" +
            "    \n" +
            "    void metoda(int a) {\n" +
            "        List l = new ArrayList(5);\n" +
            "        return;\n" +
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
                List<? extends StatementTree> stmts = method.getBody().getStatements();
                VariableTree stmt = (VariableTree) stmts.get(0);
                //ExpressionStatementTree stmt = (ExpressionStatementTree) stmts.get(0);
                NewClassTree nct = (NewClassTree) stmt.getInitializer();
                //NewClassTree nct = (NewClassTree) stmt.getExpression();
                workingCopy.rewrite(nct, make.addNewClassArgument(nct, make.Literal(5)));
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * http://java.netbeans.org/issues/show_bug.cgi?id=117326
     */
    public void testAddNewClassInvocParameter2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.Arrays;\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class Mnozina {\n" +
            "    \n" +
            "    void metoda(int a) {\n" +
            "        new java.util.ArrayList();\n" +
            "        return;\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.Arrays;\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class Mnozina {\n" +
            "    \n" +
            "    void metoda(int a) {\n" +
            "        new java.util.ArrayList(5);\n" +
            "        return;\n" +
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
                List<? extends StatementTree> stmts = method.getBody().getStatements();
                ExpressionStatementTree stmt = (ExpressionStatementTree) stmts.get(0);
                NewClassTree nct = (NewClassTree) stmt.getExpression();
                workingCopy.rewrite(nct, make.addNewClassArgument(nct, make.Literal(5)));
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * http://java.netbeans.org/issues/show_bug.cgi?id=121181
     */
    public void test121181() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package aloisovo;\n" +
            "\n" +
            "public class Traktor {\n" +
            "\n" +
            "    void zetor(String par0, String par1, String par2) {\n" +
            "    }\n" +
            "\n" +
            "    public void zetorBrno() {\n" +
            "        zetor(\"Crystal\", null, null);\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package aloisovo;\n" +
            "\n" +
            "public class Traktor {\n" +
            "\n" +
            "    void zetor(String par0, String par3, String par1, String par2) {\n" +
            "    }\n" +
            "\n" +
            "    public void zetorBrno() {\n" +
            "        zetor(\"Crystal\", null, null, null);\n" +
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
                VariableTree var = make.Variable(
                            make.Modifiers(Collections.<Modifier>emptySet()),
                            "par3", 
                            make.Identifier("String"),
                            null
                        );
                MethodTree copy = make.insertMethodParameter(method, 1, var);
                workingCopy.rewrite(method, copy);
                
                method = (MethodTree) clazz.getMembers().get(2);
                ExpressionStatementTree est = (ExpressionStatementTree) method.getBody().getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                MethodInvocationTree copyT = make.insertMethodInvocationArgument(mit, 1, make.Literal(null));
                workingCopy.rewrite(mit, copyT);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * http://java.netbeans.org/issues/show_bug.cgi?id=117913
     */
    public void test117913() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package aloisovo;\n" +
            "\n" +
            "public class Traktor {\n" +
            "\n" +
            "    public void zetorBrno() {\n" +
            "        return null;\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package aloisovo;\n" +
            "\n" +
            "public class Traktor {\n" +
            "\n" +
            "    public void zetorBrno() {\n" +
            "        String par3;\n" +
            "        return null;\n" +
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
                BlockTree body = method.getBody();
                BlockTree copy = make.createMethodBody(method, "{ String par3; return null; }");
                workingCopy.rewrite(body, copy);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * http://www.netbeans.org/issues/show_bug.cgi?id=121873
     */
    public void testDefaultAnnotationAttributeValue121873() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package aloisovo;\n" +
            "\n" +
            "public @interface Traktor {\n" +
            "    public void zetorBrno() default A.E; \n" +
            "}\n" +
            "enum A {E}");
        String golden =
            "package aloisovo;\n" +
            "\n" +
            "public @interface Traktor {\n" +
            "    public void zetorBrno() default A.X; \n" +
            "}\n" +
            "enum A {X}";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override
                    public Void visitMemberSelect(MemberSelectTree node, Void p) {
                        if ("E".equals(node.getIdentifier().toString())) {
                            workingCopy.rewrite(node, workingCopy.getTreeMaker().setLabel(node, "X"));
                        }
                        
                        return super.visitMemberSelect(node, p);
                    }
                    @Override
                    public Void visitVariable(VariableTree node, Void p) {
                        if ("E".equals(node.getName().toString())) {
                            workingCopy.rewrite(node, workingCopy.getTreeMaker().setLabel(node, "X"));
                        }
                        
                        return super.visitVariable(node, p);
                    }
                }.scan(cut, null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testSpaceAfterComma1() throws Exception {
        doTestSpaceAfterComma(" ");
    }

    public void testSpaceAfterComma2() throws Exception {
        Map<String, String> origValues = Utils.setCodePreferences(Collections.singletonMap(FmtOptions.spaceAfterComma, "false"));

        try {
            doTestSpaceAfterComma("");
        } finally {
            Utils.setCodePreferences(origValues);
        }
    }

    private void doTestSpaceAfterComma(String space) throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package aloisovo;\n" +
            "\n" +
            "public class Traktor {\n" +
            "\n" +
            "    void zetor(String par0, String par1) {\n" +
            "    }\n" +
            "\n" +
            "    public void zetorBrno() {\n" +
            "        zetor(\"Crystal\", null);\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package aloisovo;\n" +
            "\n" +
            "public class Traktor {\n" +
            "\n" +
            "    void zetor(String par0, String par1," + space + "String par2) {\n" +
            "    }\n" +
            "\n" +
            "    public void zetorBrno() {\n" +
            "        zetor(\"Crystal\", null," + space + "null);\n" +
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
                VariableTree var = make.Variable(
                            make.Modifiers(Collections.<Modifier>emptySet()),
                            "par2",
                            make.Identifier("String"),
                            null
                        );
                MethodTree copy = make.addMethodParameter(method, var);
                workingCopy.rewrite(method, copy);

                method = (MethodTree) clazz.getMembers().get(2);
                ExpressionStatementTree est = (ExpressionStatementTree) method.getBody().getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                MethodInvocationTree copyT = make.addMethodInvocationArgument(mit, make.Literal(null));
                workingCopy.rewrite(mit, copyT);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAnnotatedParameters196719a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package aloisovo;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void t(@SuppressWarnings(\"\") int aa, long bb) {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package aloisovo;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void t(@SuppressWarnings(\"\") int cc, long bb) {\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree mt = (MethodTree) clazz.getMembers().get(1);
                VariableTree param = mt.getParameters().get(0);

                workingCopy.rewrite(param, workingCopy.getTreeMaker().setLabel(param, "cc"));
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAnnotatedParameters196719b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package aloisovo;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void t(@SuppressWarnings(\"\") int aa, long bb) {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package aloisovo;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void x(@SuppressWarnings(\"\") int aa, long bb) {\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree mt = (MethodTree) clazz.getMembers().get(1);

                workingCopy.rewrite(mt, workingCopy.getTreeMaker().setLabel(mt, "x"));
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAnnotatedParameters196719c() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package aloisovo;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void t() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package aloisovo;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void x() {\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree mt = (MethodTree) clazz.getMembers().get(1);

                workingCopy.rewrite(mt, workingCopy.getTreeMaker().setLabel(mt, "x"));
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test197057() throws Exception {
        System.err.println("test197057");


        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    private void a() {\n" +
            "       //a\n" +
            "    }\n" +
            "    private void b() {\n" +
            "       //b\n" +
            "    }\n" +
            "}\n" +
            "class A {\n" +
            "}\n");



         String golden =
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    private void a() {\n" +
            "       //a\n" +
            "    }\n" +
            "    private void b() {\n" +
            "       //b\n" +
            "    }\n" +
            "}\n" +
            "class A {\n\n" +
            "    private void a() {\n" +
            "        //a\n" +
            "    }\n\n" +
            "    private void b() {\n" +
            "        //b\n" +
            "    }\n" +
            "}\n";



        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ClassTree dest = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(1);
                ClassTree newDest;

                MethodTree a = (MethodTree) clazz.getMembers().get(1);
                MethodTree b = (MethodTree) clazz.getMembers().get(2);

                GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy);
                TreePath mpath = workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), a);
                Tree newMethodTree = genUtils.importComments(mpath.getLeaf(), mpath.getCompilationUnit());
                newMethodTree = genUtils.importFQNs(newMethodTree);

                MethodTree oldOne = (MethodTree) newMethodTree;
                MethodTree newm = make.Method(
                        oldOne.getModifiers(),
                        oldOne.getName(),
                        oldOne.getReturnType(),
                        oldOne.getTypeParameters(),
                        oldOne.getParameters(),
                        oldOne.getThrows(),
                        oldOne.getBody(),
                        (ExpressionTree) oldOne.getDefaultValue());
                //RetoucheUtils.copyJavadoc(methodElm, m, workingCopy);
                newDest = genUtils.insertClassMember(dest, newm);

                mpath = workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), b);
                newMethodTree = genUtils.importComments(mpath.getLeaf(), mpath.getCompilationUnit());
                newMethodTree = genUtils.importFQNs(newMethodTree);

                oldOne = (MethodTree) newMethodTree;
                newm = make.Method(
                        oldOne.getModifiers(),
                        oldOne.getName(),
                        oldOne.getReturnType(),
                        oldOne.getTypeParameters(),
                        oldOne.getParameters(),
                        oldOne.getThrows(),
                        oldOne.getBody(),
                        (ExpressionTree) oldOne.getDefaultValue());
                //RetoucheUtils.copyJavadoc(methodElm, m, workingCopy);
                newDest = genUtils.insertClassMember(newDest, newm);

                workingCopy.rewrite(dest, newDest);
            }
        };
        testSource.runModificationTask(task).commit();
        DataObject d = DataObject.find(FileUtil.toFileObject(testFile));
        EditorCookie ec = d.getLookup().lookup(EditorCookie.class);
        ec.saveDocument();

        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testCopyLiteralTree() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "public class Test {\n" +
            "    \n" +
            "    public static int II = 0x00FF;\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "public class Test {\n" +
            "    \n" +
            "    public static int II = 0x00FF;\n" +
            "}\n";
        String nueGolden =
            "\n" +
            "public class Nue {\n" +
            "\n" +
            "    public static int II = 0x00FF;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree toCopy = (VariableTree) clazz.getMembers().get(1);
                ClassTree newClass = make.Class(make.Modifiers(EnumSet.of(Modifier.PUBLIC)), "Nue", Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), Collections.<Tree>singletonList(toCopy));
                workingCopy.rewrite(null, make.CompilationUnit(FileUtil.toFileObject(getWorkDir()), "Nue.java", Collections.<ImportTree>emptyList(), Collections.singletonList(newClass)));
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
        File newFile = new File(getWorkDir(), "Nue.java");
        assertTrue(newFile.canRead());
        String newRes = TestUtilities.copyFileToString(newFile);
        assertEquals(nueGolden, newRes);
    }
    
    public void testNonSyntheticSuper() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "public class Test {\n" +
            "    class ExceptionX extends Exception {\n" +
            "        public ExceptionX() {\n" +
            "            super(\"T\", new RuntimeException(\"T\"));\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "public class Test {\n" +
            "    class ExceptionX extends Exception {\n" +
            "        public ExceptionX() {\n" +
            "            super(\"T\", new RuntimeException(\"T\"));\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        String nueGolden =
            "\n" +
            "class ExceptionX extends Exception {\n" +
            "\n" +
            "    public ExceptionX() {\n" +
            "        super(\"T\", new RuntimeException(\"T\"));\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                ClassTree inner = (ClassTree) clazz.getMembers().get(1);
                inner = GeneratorUtilities.get(workingCopy).importFQNs(inner);
                workingCopy.rewrite(null, make.CompilationUnit(FileUtil.toFileObject(getWorkDir()), "ExceptionX.java", Collections.<ImportTree>emptyList(), Collections.singletonList(inner)));
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
        File newFile = new File(getWorkDir(), "ExceptionX.java");
        assertTrue(newFile.canRead());
        String newRes = TestUtilities.copyFileToString(newFile);
        assertEquals(nueGolden, newRes);
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
}
