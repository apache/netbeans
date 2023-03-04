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

import java.io.File;
import java.util.Collections;
import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;

/**
 * Test class add couple of body statements. It test statements creation and
 * addition to body.
 * 
 * @author Pavel Flaska
 */
public class BodyStatementTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of BodyStatementTest */
    public BodyStatementTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(BodyStatementTest.class);
//        suite.addTest(new BodyStatementTest("testNullLiteral"));
//        suite.addTest(new BodyStatementTest("testBooleanLiteral"));
//        suite.addTest(new BodyStatementTest("testRenameInIfStatement"));
//        suite.addTest(new BodyStatementTest("testRenameInLocalDecl"));
//        suite.addTest(new BodyStatementTest("testRenameInInvocationPars"));
//        suite.addTest(new BodyStatementTest("testAddMethodToAnnInTry"));
//        suite.addTest(new BodyStatementTest("testReturnNotDoubled"));
//        suite.addTest(new BodyStatementTest("testForNotRegen"));
//        suite.addTest(new BodyStatementTest("testAssignLeft"));
//        suite.addTest(new BodyStatementTest("testAssignRight"));
//        suite.addTest(new BodyStatementTest("testAssignBoth"));
//        suite.addTest(new BodyStatementTest("testReturn"));
//        suite.addTest(new BodyStatementTest("testPlusBinary"));
//        suite.addTest(new BodyStatementTest("testRenameInWhile"));
//        suite.addTest(new BodyStatementTest("testRenameInDoWhile"));
//        suite.addTest(new BodyStatementTest("testRenameInForEach"));
//        suite.addTest(new BodyStatementTest("testRenameInSyncro"));
//        suite.addTest(new BodyStatementTest("testRenameInCatch"));
//        suite.addTest(new BodyStatementTest("testRenameInAssignOp"));
//        suite.addTest(new BodyStatementTest("testRenameInArrayIndex"));
//        suite.addTest(new BodyStatementTest("testRenameInTypeCast"));
//        suite.addTest(new BodyStatementTest("testRenameInAssert"));
//        suite.addTest(new BodyStatementTest("testRenameInThrowSt"));
//        suite.addTest(new BodyStatementTest("testRenameInConditional"));
//        suite.addTest(new BodyStatementTest("testRenameInLabelled"));
//        suite.addTest(new BodyStatementTest("testRenameInContinue"));
//        suite.addTest(new BodyStatementTest("testRenameInBreak"));
//        suite.addTest(new BodyStatementTest("testRenameLocVarTypePar"));
//        suite.addTest(new BodyStatementTest("testRenameInSwitch"));
//        suite.addTest(new BodyStatementTest("testRenameInTypeNewArr"));
//        suite.addTest(new BodyStatementTest("testRenameInTypeTest"));
//        suite.addTest(new BodyStatementTest("testRenameInTypeTestII"));
//        suite.addTest(new BodyStatementTest("testChangeLiteral"));
//        suite.addTest(new BodyStatementTest("testRenameInArrInit"));
//        suite.addTest(new BodyStatementTest("testRenameClazz"));
//        suite.addTest(new BodyStatementTest("testRenameInCase"));
//        suite.addTest(new BodyStatementTest("testRenameClazzInNewParameter"));
//        suite.addTest(new BodyStatementTest("test99445"));
//        suite.addTest(new BodyStatementTest("test101717"));
//        suite.addTest(new BodyStatementTest("testModifyingIf"));
//        suite.addTest(new BodyStatementTest("testRenameInParens"));
//        suite.addTest(new BodyStatementTest("test111983"));
//        suite.addTest(new BodyStatementTest("test112290_1"));
//        suite.addTest(new BodyStatementTest("test112290_2"));
//        suite.addTest(new BodyStatementTest("test112290_3"));
//        suite.addTest(new BodyStatementTest("test126460a"));
//        suite.addTest(new BodyStatementTest("test126460b"));
//        suite.addTest(new BodyStatementTest("test126460c"));
//        suite.addTest(new BodyStatementTest("test159671a"));
//        suite.addTest(new BodyStatementTest("test159671b"));
//        suite.addTest(new BodyStatementTest("test182542"));
        return suite;
    }

    /**
     * Adds 'System.err.println(null);' statement to the method body. 
     */
    public void testNullLiteral() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        ;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        ;\n" +
            "        System.err.println(null);\n" + 
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree statement = make.ExpressionStatement(
                    make.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(),
                        make.MemberSelect(
                            make.MemberSelect(
                                make.Identifier("System"),
                                "err"
                            ),
                            "println"
                        ),
                        Collections.singletonList(
                            make.Literal(null)
                        )
                    )
                );
                BlockTree copy = make.addBlockStatement(method.getBody(), statement);
                workingCopy.rewrite(method.getBody(), copy);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Adds 'System.err.println(true);' statement to the method body. 
     */
    public void testBooleanLiteral() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        ;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        ;\n" +
            "        System.err.println(true);\n" + 
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree statement = make.ExpressionStatement(
                    make.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(),
                        make.MemberSelect(
                            make.MemberSelect(
                                make.Identifier("System"),
                                "err"
                            ),
                            "println"
                        ),
                        Collections.singletonList(
                            make.Literal(Boolean.TRUE)
                        )
                    )
                );
                BlockTree copy = make.addBlockStatement(method.getBody(), statement);
                workingCopy.rewrite(method.getBody(), copy);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Renames el to element in method parameter and if statement
     */
    public void testRenameInIfStatement() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void action666(Element el) {\n" +
            "        if (el.getName().equalsIgnoreCase(\"flaskuvElement\")) {\n" +
            "            System.err.println(\"Win!\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n");
        String golden = 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void action666(Element element) {\n" +
            "        if (element.getName().equalsIgnoreCase(\"flaskuvElement\")) {\n" +
            "            System.err.println(\"Win!\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                // rename in parameter
                VariableTree vt = method.getParameters().get(0);
                VariableTree parCopy = make.setLabel(vt, "element");
                workingCopy.rewrite(vt, parCopy);
                // no need to check kind
                // rename in if
                IfTree statementTree = (IfTree) method.getBody().getStatements().get(0);
                ParenthesizedTree condition = (ParenthesizedTree) statementTree.getCondition();
                MethodInvocationTree invocation = (MethodInvocationTree) condition.getExpression();
                MemberSelectTree select = (MemberSelectTree) invocation.getMethodSelect();
                invocation = (MethodInvocationTree) select.getExpression();
                select = (MemberSelectTree) invocation.getMethodSelect();
                IdentifierTree identToRename = (IdentifierTree) select.getExpression();
                IdentifierTree copy = make.setLabel(identToRename, "element");
                workingCopy.rewrite(identToRename, copy);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Renames el to element in method parameter and if statement
     */
    public void testRenameInLocalDecl() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void action666(Element el) {\n" +
            "        String name = el.getName();\n" +
            "    }\n" +
            "}\n");
        String golden = 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void action666(Element element) {\n" +
            "        String name = element.getName();\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                // rename in parameter
                VariableTree vt = method.getParameters().get(0);
                VariableTree parCopy = make.setLabel(vt, "element");
                workingCopy.rewrite(vt, parCopy);
                // no need to check kind
                VariableTree statementTree = (VariableTree) method.getBody().getStatements().get(0);
                MethodInvocationTree invocation = (MethodInvocationTree) statementTree.getInitializer();
                MemberSelectTree select = (MemberSelectTree) invocation.getMethodSelect();
                IdentifierTree identToRename = (IdentifierTree) select.getExpression();
                IdentifierTree copy = make.setLabel(identToRename, "element");
                workingCopy.rewrite(identToRename, copy);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Renames el to element in method parameter and if statement
     */
    public void testRenameInInvocationPars() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void action666(Element el) {\n" +
            "        Collections.singleton(el);\n" +
            "    }\n" +
            "}\n");
        String golden = 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void action666(Element element) {\n" +
            "        Collections.singleton(element);\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                // rename in parameter
                VariableTree vt = method.getParameters().get(0);
                VariableTree parCopy = make.setLabel(vt, "element");
                workingCopy.rewrite(vt, parCopy);
                // no need to check kind
                // rename in if
                ExpressionStatementTree expressionStmt = (ExpressionStatementTree) method.getBody().getStatements().get(0);
                MethodInvocationTree invocation = (MethodInvocationTree) expressionStmt.getExpression();
                IdentifierTree identToRename = (IdentifierTree) invocation.getArguments().get(0);
                IdentifierTree copy = make.setLabel(identToRename, "element");
                workingCopy.rewrite(identToRename, copy);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * Adds method to anonymous class declared in try section.
     */
    public void testAddMethodToAnnInTry() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "   public void method() {\n" +
            "        try {\n" +
            "            new Runnable() {\n" +
            "            };\n" +
            "        } finally {\n" +
            "            System.err.println(\"Got a problem.\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "   public void method() {\n" +
            "        try {\n" +
            "            new Runnable() {\n" +
            "                public void run() {\n" +
            "                }\n" +
            "            };\n" +
            "        } finally {\n" +
            "            System.err.println(\"Got a problem.\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                // rename in parameter
                TryTree tryStmt = (TryTree) method.getBody().getStatements().get(0);
                ExpressionStatementTree exprStmt = (ExpressionStatementTree) tryStmt.getBlock().getStatements().get(0);
                NewClassTree newClassTree = (NewClassTree) exprStmt.getExpression();
                ClassTree anonClassTree = newClassTree.getClassBody();
                MethodTree methodToAdd = make.Method(
                    make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)),
                    "run",
                    make.PrimitiveType(TypeKind.VOID),
                    Collections.<TypeParameterTree>emptyList(),
                    Collections.<VariableTree>emptyList(),
                    Collections.<ExpressionTree>emptyList(),
                    make.Block(Collections.<StatementTree>emptyList(), false),
                    null
                );
                ClassTree copy = make.addClassMember(anonClassTree, methodToAdd);
                workingCopy.rewrite(anonClassTree, copy);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Check return statement is not doubled. (#90806)
     */
    public void testReturnNotDoubled() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "   public Object method() {\n" +
            "        try {\n" +
            "            new Runnable() {\n" +
            "            }\n" +
            "            return null;\n" +
            "        } finally {\n" +
            "            System.err.println(\"Got a problem.\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "   public Object method() {\n" +
            "        try {\n" +
            "            new Runnable() {\n" +
            "                public void run() {\n" +
            "                }\n" +
            "            }\n" +
            "            return null;\n" +
            "        } finally {\n" +
            "            System.err.println(\"Got a problem.\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                // rename in parameter
                TryTree tryStmt = (TryTree) method.getBody().getStatements().get(0);
                BlockTree tryBlock = tryStmt.getBlock();
                ExpressionStatementTree exprStmt = (ExpressionStatementTree) tryStmt.getBlock().getStatements().get(0);
                NewClassTree newClassTree = (NewClassTree) exprStmt.getExpression();
                ClassTree anonClassTree = newClassTree.getClassBody();
                MethodTree methodToAdd = make.Method(
                    make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)),
                    "run",
                    make.PrimitiveType(TypeKind.VOID),
                    Collections.<TypeParameterTree>emptyList(),
                    Collections.<VariableTree>emptyList(),
                    Collections.<ExpressionTree>emptyList(),
                    make.Block(Collections.<StatementTree>emptyList(), false),
                    null
                );
                ClassTree copy = make.addClassMember(anonClassTree, methodToAdd);
                workingCopy.rewrite(anonClassTree, copy);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Check 'for' body is not regenerated. (#91061)
     */
    public void testForNotRegen() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "   public Object method() {\n" +
            "        for (int var2 = 0; var2 < 10; var2++) {\n" +
            "           // comment\n" +
            "           System.out.println(var2); // What a ... comment\n" +
            "           // comment\n" +
            "           List l;\n" +
            "        }\n" +
            "    }\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "import javax.swing.text.Element;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "   public Object method() {\n" +
            "        for (int newVar = 0; newVar < 10; newVar++) {\n" +
            "           // comment\n" +
            "           System.out.println(newVar); // What a ... comment\n" +
            "           // comment\n" +
            "           List l;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ForLoopTree forLoop = (ForLoopTree) method.getBody().getStatements().get(0);
                // rewrite in initializer
                VariableTree initalizer = (VariableTree) forLoop.getInitializer().get(0);
                workingCopy.rewrite(initalizer, make.setLabel(initalizer, "newVar"));
                
                // rewrite in condition
                BinaryTree condition = (BinaryTree) forLoop.getCondition();
                IdentifierTree ident = (IdentifierTree) condition.getLeftOperand();
                workingCopy.rewrite(ident, make.setLabel(ident, "newVar"));
                
                ExpressionStatementTree update = (ExpressionStatementTree) forLoop.getUpdate().get(0);
                UnaryTree unary = (UnaryTree) update.getExpression();
                ident = (IdentifierTree) unary.getExpression();
                workingCopy.rewrite(ident, make.setLabel(ident, "newVar"));
                
                // and finally in the body
                BlockTree block = (BlockTree) forLoop.getStatement();
                ExpressionStatementTree systemOut = (ExpressionStatementTree) block.getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) systemOut.getExpression();
                ident = (IdentifierTree) mit.getArguments().get(0);
                workingCopy.rewrite(ident, make.setLabel(ident, "newVar"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Test for left right side of assignment
     */
    public void testAssignLeft() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        this.key = key;\n" +
            "    }\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        this.key2 = key;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree est = (ExpressionStatementTree) method.getBody().getStatements().get(0);
                AssignmentTree assignment = (AssignmentTree) est.getExpression();
                MemberSelectTree mstCopy = make.setLabel((MemberSelectTree) assignment.getVariable(), "key2");
                workingCopy.rewrite(assignment.getVariable(), mstCopy);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Test for right side of assignment
     */
    public void testAssignRight() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        this.key = key;\n" +
            "    }\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        this.key = key2;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree est = (ExpressionStatementTree) method.getBody().getStatements().get(0);
                AssignmentTree assignment = (AssignmentTree) est.getExpression();
                IdentifierTree copy = make.setLabel((IdentifierTree) assignment.getExpression(), "key2");
                workingCopy.rewrite(assignment.getExpression(), copy);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Test for right side of assignment
     */
    public void testAssignBoth() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        this.key = key;\n" +
            "    }\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        this.key2 = key2;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree est = (ExpressionStatementTree) method.getBody().getStatements().get(0);
                AssignmentTree assignment = (AssignmentTree) est.getExpression();
                MemberSelectTree mstCopy = make.setLabel((MemberSelectTree) assignment.getVariable(), "key2");
                workingCopy.rewrite(assignment.getVariable(), mstCopy);
                IdentifierTree copy = make.setLabel((IdentifierTree) assignment.getExpression(), "key2");
                workingCopy.rewrite(assignment.getExpression(), copy);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Test for return rename
     */
    public void testReturn() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        return nullanen;\n" +
            "    }\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        return nullanen2;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ReturnTree rejturn = (ReturnTree) method.getBody().getStatements().get(0);
                workingCopy.rewrite(rejturn.getExpression(), make.Identifier("nullanen2"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Test in PLUS rename
     */
    public void testPlusBinary() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        return \"[\" + key + \"; \" + value + \"]\"\n" +
            "    }\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        return \"[\" + key2 + \"; \" + value + \"]\"\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ReturnTree rejturn = (ReturnTree) method.getBody().getStatements().get(0);
                BinaryTree in = (BinaryTree) rejturn.getExpression();
                for (int i = 0; i < 3; i++) {
                    in = (BinaryTree) in.getLeftOperand();
                }
                IdentifierTree ident = (IdentifierTree) in.getRightOperand();
                workingCopy.rewrite(ident, make.Identifier("key2"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Rename in while
     */
    public void testRenameInWhile() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        int i = 0;\n" +
            "        while (i < 10) {\n" +
            "            i = i + 1;\n" +
            "        }\n" +
            "    }\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        int counter = 0;\n" +
            "        while (counter < 10) {\n" +
            "            counter = counter + 1;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);
                workingCopy.rewrite(var, make.setLabel(var, "counter"));
                
                WhileLoopTree whileLoop = (WhileLoopTree) method.getBody().getStatements().get(1);
                ParenthesizedTree paren = (ParenthesizedTree) whileLoop.getCondition();
                BinaryTree lessThan = (BinaryTree) paren.getExpression();
                IdentifierTree left = (IdentifierTree) lessThan.getLeftOperand();
                workingCopy.rewrite(left, make.setLabel(left, "counter"));
                
                ExpressionStatementTree expr = (ExpressionStatementTree) ((BlockTree) whileLoop.getStatement()).getStatements().get(0);
                AssignmentTree assign = (AssignmentTree) expr.getExpression();
                left = (IdentifierTree) assign.getVariable();
                workingCopy.rewrite(left, make.setLabel(left, "counter"));
                BinaryTree right = (BinaryTree) assign.getExpression();
                left = (IdentifierTree) right.getLeftOperand();
                workingCopy.rewrite(left, make.setLabel(left, "counter"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Rename in do while
     */
    public void testRenameInDoWhile() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        int i = 0;\n" +
            "        do {\n" +
            "            i++;\n" +
            "        } while (i > 10);\n" +
            "    }\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        int counter = 0;\n" +
            "        do {\n" +
            "            counter++;\n" +
            "        } while (counter > 10);\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);
                workingCopy.rewrite(var, make.setLabel(var, "counter"));
                
                DoWhileLoopTree doWhileLoop = (DoWhileLoopTree) method.getBody().getStatements().get(1);
                ParenthesizedTree paren = (ParenthesizedTree) doWhileLoop.getCondition();
                BinaryTree lessThan = (BinaryTree) paren.getExpression();
                IdentifierTree left = (IdentifierTree) lessThan.getLeftOperand();
                workingCopy.rewrite(left, make.setLabel(left, "counter"));
                
                ExpressionStatementTree expr = (ExpressionStatementTree) ((BlockTree) doWhileLoop.getStatement()).getStatements().get(0);
                UnaryTree unary = (UnaryTree) expr.getExpression();
                workingCopy.rewrite(unary.getExpression(), make.setLabel(unary.getExpression(), "counter"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Rename in for each
     */
    public void testRenameInForEach() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        List l = new ArrayList();\n" +
            "        for (Object o : l) {\n" +
            "            o.toString();\n" +
            "        }\n" +
            "    }\n" +
            "}\n");

         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        List list = new ArrayList();\n" +
            "        for (Object object : list) {\n" +
            "            object.toString();\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);
                workingCopy.rewrite(var, make.setLabel(var, "list"));
                
                EnhancedForLoopTree forEach = (EnhancedForLoopTree) method.getBody().getStatements().get(1);
                var = forEach.getVariable();
                workingCopy.rewrite(var, make.setLabel(var, "object"));
                IdentifierTree ident = (IdentifierTree) forEach.getExpression();
                workingCopy.rewrite(ident, make.setLabel(ident, "list"));
                BlockTree body = (BlockTree) forEach.getStatement();
                ExpressionStatementTree est = (ExpressionStatementTree) body.getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                MemberSelectTree mst = (MemberSelectTree) mit.getMethodSelect();
                
                workingCopy.rewrite(mst.getExpression(), make.setLabel(mst.getExpression(), "object"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * #92187: Test rename in synchronized
     */
    public void testRenameInSyncro() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        Object lock = new Object();\n" +
            "        \n" +
            "        synchronized(lock) {\n" +
            "            int a = 20;\n" +
            "            lock.wait();\n" +
            "        }\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        Object zamek = new Object();\n" +
            "        \n" +
            "        synchronized(zamek) {\n" +
            "            int a = 20;\n" +
            "            zamek.wait();\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);
                workingCopy.rewrite(var, make.setLabel(var, "zamek"));
                
                SynchronizedTree syncro = (SynchronizedTree) method.getBody().getStatements().get(1);
                ParenthesizedTree petecko = (ParenthesizedTree) syncro.getExpression();
                IdentifierTree ident = (IdentifierTree) petecko.getExpression();
                workingCopy.rewrite(ident, make.setLabel(ident, "zamek"));
                BlockTree body = syncro.getBlock();
                ExpressionStatementTree est = (ExpressionStatementTree) body.getStatements().get(1);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                MemberSelectTree mst = (MemberSelectTree) mit.getMethodSelect();
                
                workingCopy.rewrite(mst.getExpression(), make.setLabel(mst.getExpression(), "zamek"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Test rename in catch
     */
    public void testRenameInCatch() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        Object zamek = new Object();\n" +
            "        try {\n" +
            "            zamek.wait();\n" +
            "        } catch (InterruptedException ex) {\n" +
            "            ex.printStackTrace();\n" +
            "        }\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        Object zamek = new Object();\n" +
            "        try {\n" +
            "            zamek.wait();\n" +
            "        } catch (InterruptedException vyjimka) {\n" +
            "            vyjimka.printStackTrace();\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);
                workingCopy.rewrite(var, make.setLabel(var, "zamek"));
                TryTree tryTree = (TryTree) method.getBody().getStatements().get(1);
                CatchTree ct = tryTree.getCatches().get(0);
                workingCopy.rewrite(ct.getParameter(), make.setLabel(ct.getParameter(), "vyjimka"));
                BlockTree body = ct.getBlock();
                ExpressionStatementTree est = (ExpressionStatementTree) body.getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                MemberSelectTree mst = (MemberSelectTree) mit.getMethodSelect();
                workingCopy.rewrite(mst.getExpression(), make.setLabel(mst.getExpression(), "vyjimka"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Test rename in assign op. bit or
     * todo (#pf): extend test to replace right side and operator too!
     */
    public void testRenameInAssignOp() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        int bits2 = 0;\n" +
            "        bits2 |= 0x12;\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        int bits = 0;\n" +
            "        bits |= 0x12;\n" +
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
                VariableTree var = (VariableTree) block.getStatements().get(0);
                workingCopy.rewrite(var, make.setLabel(var, "bits"));
                ExpressionStatementTree est = (ExpressionStatementTree) block.getStatements().get(1);
                CompoundAssignmentTree cat = (CompoundAssignmentTree) est.getExpression();
                IdentifierTree ident = (IdentifierTree) cat.getVariable();
                workingCopy.rewrite(ident, make.setLabel(ident, "bits"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Test rename in assign op. bit or
     * both, var and index are renamed in this test.
     * does not rename in new array
     */
    public void testRenameInArrayIndex() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        int pos = 10;\n" +
            "        int[] i = new int[10];\n" +
            "        System.err.println(i[pos-1]);\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        int position = 10;\n" +
            "        int[] icko = new int[10];\n" +
            "        System.err.println(icko[position-1]);\n" +
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
                VariableTree var1 = (VariableTree) block.getStatements().get(0);
                VariableTree var2 = (VariableTree) block.getStatements().get(1);
                workingCopy.rewrite(var1, make.setLabel(var1, "position"));
                workingCopy.rewrite(var2, make.setLabel(var2, "icko"));
                ExpressionStatementTree est = (ExpressionStatementTree) block.getStatements().get(2);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                ArrayAccessTree aat = (ArrayAccessTree) mit.getArguments().get(0);
                IdentifierTree ident = (IdentifierTree) aat.getExpression();
                workingCopy.rewrite(ident, make.setLabel(ident, "icko"));
                BinaryTree binary = (BinaryTree) aat.getIndex();
                ident = (IdentifierTree) binary.getLeftOperand();
                workingCopy.rewrite(ident, make.setLabel(ident, "position"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Test rename in type cast
     */
    public void testRenameInTypeCast() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    static class Item {}\n" +
            "    public Object method() {\n" +
            "        Object o = null;\n" +
            "        Item item = (Item) o;\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    static class It {}\n" +
            "    public Object method() {\n" +
            "        Object object = null;\n" +
            "        It it = (It) object;\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ClassTree clazzIn = (ClassTree) clazz.getMembers().get(1);
                workingCopy.rewrite(clazzIn, make.setLabel(clazzIn, "It"));
                MethodTree method = (MethodTree) clazz.getMembers().get(2);
                BlockTree block = method.getBody();
                VariableTree var1 = (VariableTree) block.getStatements().get(0);
                VariableTree var2 = (VariableTree) block.getStatements().get(1);
                workingCopy.rewrite(var1, make.setLabel(var1, "object"));
                VariableTree var2copy = make.Variable(
                        var2.getModifiers(),
                        "it",
                        make.Identifier("It"),
                        var2.getInitializer());
                workingCopy.rewrite(var2, var2copy);
                TypeCastTree tct = (TypeCastTree) var2.getInitializer();
                IdentifierTree ident = (IdentifierTree) tct.getType();
                workingCopy.rewrite(ident, make.setLabel(ident, "It"));
                ident = (IdentifierTree) tct.getExpression();
                workingCopy.rewrite(ident, make.setLabel(ident, "object"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Test rename in assert
     */
    public void testRenameInAssert() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(int a) {\n" +
            "        assert a == 12 : a;\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(int ada) {\n" +
            "        assert ada == 12 : ada;\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree vtecko = method.getParameters().get(0);
                workingCopy.rewrite(vtecko, make.setLabel(vtecko, "ada"));
                BlockTree block = method.getBody();
                AssertTree ass = (AssertTree) block.getStatements().get(0);
                BinaryTree cond = (BinaryTree) ass.getCondition();
                IdentifierTree ident = (IdentifierTree) cond.getLeftOperand();
                workingCopy.rewrite(ident, make.setLabel(ident, "ada"));
                ident = (IdentifierTree) ass.getDetail();
                workingCopy.rewrite(ident, make.setLabel(ident, "ada"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Test rename in throw statement
     */
    public void testRenameInThrowSt() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        throw new NullPointerException();\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        throw new EnpeEcko();\n" +
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
                ThrowTree ttecko = (ThrowTree) block.getStatements().get(0);
                NewClassTree nct = (NewClassTree) ttecko.getExpression();
                IdentifierTree ident = (IdentifierTree) nct.getIdentifier();
                workingCopy.rewrite(ident, make.setLabel(ident, "EnpeEcko"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Test rename in conditional
     */
    public void testRenameInConditional() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(int ada) {\n" +
            "        int result = ada == 10 ? ada++ : --ada;\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(int alda) {\n" +
            "        int result = alda == 10 ? alda++ : --alda;\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree vtecko = method.getParameters().get(0);
                workingCopy.rewrite(vtecko, make.setLabel(vtecko, "alda"));
                BlockTree block = method.getBody();
                VariableTree var = (VariableTree) block.getStatements().get(0);
                ConditionalExpressionTree cet = (ConditionalExpressionTree) var.getInitializer();
                BinaryTree cond = (BinaryTree) cet.getCondition();
                IdentifierTree ident = (IdentifierTree) cond.getLeftOperand();
                workingCopy.rewrite(ident, make.setLabel(ident, "alda"));
                UnaryTree truePart = (UnaryTree) cet.getTrueExpression();
                ident = (IdentifierTree) truePart.getExpression();
                workingCopy.rewrite(ident, make.setLabel(ident, "alda"));
                UnaryTree falsePart = (UnaryTree) cet.getFalseExpression();
                ident = (IdentifierTree) falsePart.getExpression();
                workingCopy.rewrite(ident, make.setLabel(ident, "alda"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * #92187: Test rename in labelled
     */
    public void testRenameInLabelled() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "package personal;\n\npublic class Test {\n    public Object method() {\n        cycle_start: for (int i = 0; i < 10; i++) {\n        }\n    }\n}\n");
        String golden = "package personal;\n\npublic class Test {\n    public Object method() {\n        zacatek_smycky: for (int i = 0; i < 10; i++) {\n        }\n    }\n}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                LabeledStatementTree lst = (LabeledStatementTree)block.getStatements().get(0);
                workingCopy.rewrite(lst, make.setLabel(lst, "zacatek_smycky"));
            }
            
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92187: Test rename in continue
     */
    public void testRenameInContinue()
            throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "package personal;\n\npublic class Test {\n    public Object method() {\n        cycle_start: for (int i = 0; i < 10; i++) {\n            continue cycle_start;\n        }\n    }\n}\n");
        String golden = "package personal;\n\npublic class Test {\n    public Object method() {\n        zacatek_smycky: for (int i = 0; i < 10; i++) {\n            continue zacatek_smycky;\n        }\n    }\n}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                LabeledStatementTree lst = (LabeledStatementTree)block.getStatements().get(0);
                workingCopy.rewrite(lst, make.setLabel(lst, "zacatek_smycky"));
                ForLoopTree flt = (ForLoopTree)lst.getStatement();
                BlockTree forTree = (BlockTree)flt.getStatement();
                ContinueTree ct = (ContinueTree)forTree.getStatements().get(0);
                workingCopy.rewrite(ct, make.setLabel(ct, "zacatek_smycky"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * #92187: Test rename in break
     */
    public void testRenameInBreak() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "package personal;\n\npublic class Test {\n    public Object method() {\n        cycle_start: for (int i = 0; i < 10; i++) {\n            break cycle_start;\n        }\n    }\n}\n");
        String golden = "package personal;\n\npublic class Test {\n    public Object method() {\n        zacatek_smycky: for (int i = 0; i < 10; i++) {\n            break zacatek_smycky;\n        }\n    }\n}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy)
                    throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                LabeledStatementTree lst = (LabeledStatementTree)block.getStatements().get(0);
                workingCopy.rewrite(lst, make.setLabel(lst, "zacatek_smycky"));
                ForLoopTree flt = (ForLoopTree)lst.getStatement();
                BlockTree forTree = (BlockTree)flt.getStatement();
                BreakTree bt = (BreakTree)forTree.getStatements().get(0);
                workingCopy.rewrite(bt, make.setLabel(bt, "zacatek_smycky"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #88073: Test rename in loc. var type param.
     */
    public void testRenameLocVarTypePar() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n\n" +
            "import java.util.*;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(int a) {\n" +
            "        Map<String,Data> map1 = new HashMap<String,Data>();\n" +
            "        Map<Data,String> map2 = new TreeMap<Data, String>();\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n\n" +
            "import java.util.*;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(int a) {\n" +
            "        Map<String,DataRen> map1 = new HashMap<String,DataRen>();\n" +
            "        Map<DataRen,String> map2 = new TreeMap<DataRen, String>();\n" +
            "    }\n" +
            "}\n";
         
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy)
                    throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                
                VariableTree vt = (VariableTree) block.getStatements().get(0);
                ParameterizedTypeTree ptt = (ParameterizedTypeTree) vt.getType();
                IdentifierTree it = (IdentifierTree) ptt.getTypeArguments().get(1);
                workingCopy.rewrite(it, make.setLabel(it, "DataRen"));
                
                NewClassTree nct = (NewClassTree) vt.getInitializer();
                ptt = (ParameterizedTypeTree) nct.getIdentifier();
                it = (IdentifierTree) ptt.getTypeArguments().get(1);
                workingCopy.rewrite(it, make.setLabel(it, "DataRen"));
                
                vt = (VariableTree) block.getStatements().get(1);
                ptt = (ParameterizedTypeTree) vt.getType();
                it = (IdentifierTree) ptt.getTypeArguments().get(0);
                workingCopy.rewrite(it, make.setLabel(it, "DataRen"));
                nct = (NewClassTree) vt.getInitializer();
                ptt = (ParameterizedTypeTree) nct.getIdentifier();
                it = (IdentifierTree) ptt.getTypeArguments().get(0);
                workingCopy.rewrite(it, make.setLabel(it, "DataRen"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Test rename in switch
     */
    public void testRenameInSwitch() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(int la) {\n" +
            "        switch (la) {\n" +
            "            case 0:\n" +
            "                break;\n" +
            "            case 1:\n" +
            "                break;\n" +
            "           default:\n" +
            "                // do nothing\n" +
            "        }\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(int renamed) {\n" +
            "        switch (renamed) {\n" +
            "            case 0:\n" +
            "                break;\n" +
            "            case 1:\n" +
            "                break;\n" +
            "           default:\n" +
            "                // do nothing\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                // parameter rename
                VariableTree vt = method.getParameters().get(0);
                workingCopy.rewrite(vt, make.setLabel(vt, "renamed"));
                // body rename
                BlockTree block = method.getBody();
                SwitchTree swicStrom = (SwitchTree) block.getStatements().get(0);
                ParenthesizedTree pTree = (ParenthesizedTree) swicStrom.getExpression();
                IdentifierTree ident = (IdentifierTree) pTree.getExpression();
                workingCopy.rewrite(ident, make.setLabel(ident, "renamed"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * diffNewArray
     * diffTypeArray
     */
    public void testRenameInTypeNewArr() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        int dim1 = 10;\n" +
            "        int dim2 = 15;\n" +
            "        Test[][] obj = new Test[dim1][dim2];\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class RenamedTest {\n" +
            "    public Object method() {\n" +
            "        int dim1 = 10;\n" +
            "        int dim2 = 15;\n" +
            "        RenamedTest[][] obj = new RenamedTest[dim1][dim2];\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                workingCopy.rewrite(clazz, make.setLabel(clazz, "RenamedTest"));
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                // type rename
                VariableTree vt = (VariableTree) block.getStatements().get(2);
                ArrayTypeTree att = (ArrayTypeTree) vt.getType();
                att = (ArrayTypeTree) att.getType(); // go inside, two dimensional array
                workingCopy.rewrite(att.getType(), make.Identifier("RenamedTest"));
                // new array rename
                NewArrayTree nat = (NewArrayTree) vt.getInitializer();
                workingCopy.rewrite(nat.getType(), make.Identifier("RenamedTest"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * diffTypeTest
     */
    public void testRenameInTypeTest() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(Object o) {\n" +
            "        if (o instanceof Test) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(Object obj) {\n" +
            "        if (obj instanceof Test) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                // parameter rename
                VariableTree vt = method.getParameters().get(0);
                workingCopy.rewrite(vt, make.setLabel(vt, "obj"));
                // body rename
                BlockTree block = method.getBody();
                IfTree iv = (IfTree) block.getStatements().get(0);
                ParenthesizedTree pt = (ParenthesizedTree) iv.getCondition();
                InstanceOfTree iot = (InstanceOfTree) pt.getExpression();
                IdentifierTree ident = (IdentifierTree) iot.getExpression();
                workingCopy.rewrite(ident, make.Identifier("obj"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * diffTypeTestII
     */
    public void testRenameInTypeTestII() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(Object o) {\n" +
            "        if (o instanceof Test) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class RenamedTest {\n" +
            "    public Object method(Object o) {\n" +
            "        if (o instanceof RenamedTest) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                workingCopy.rewrite(clazz, make.setLabel(clazz, "RenamedTest"));
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                // body rename
                BlockTree block = method.getBody();
                IfTree iv = (IfTree) block.getStatements().get(0);
                ParenthesizedTree pt = (ParenthesizedTree) iv.getCondition();
                InstanceOfTree iot = (InstanceOfTree) pt.getExpression();
                IdentifierTree ident = (IdentifierTree) iot.getType();
                workingCopy.rewrite(ident, make.Identifier("RenamedTest"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Changing literal test - #95614
     */
    public void testChangeLiteral() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(Object o) {\n" +
            "        System.err.println(\"Karel\");\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class RenamedTest {\n" +
            "    public Object method(Object o) {\n" +
            "        System.err.println(\"Hrebejk\");\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                workingCopy.rewrite(clazz, make.setLabel(clazz, "RenamedTest"));
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                // body rename
                BlockTree block = method.getBody();
                ExpressionStatementTree expr = (ExpressionStatementTree) block.getStatements().get(0);
                MethodInvocationTree invocation = (MethodInvocationTree) expr.getExpression();
                LiteralTree literal = (LiteralTree) invocation.getArguments().get(0);
                workingCopy.rewrite(literal, make.Literal("Hrebejk"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Changing names in array init - #92610
     */
    public void testRenameInArrInit() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(Object o) {\n" +
            "        Inner inInst = new Inner();\n" +
            "        Inner[] inArr = new Inner[] { inInst, new Inner() };\n" +
            "    }\n" +
            "    private static class Inner {\n" +
            "        public Inner() {\n" + 
            "        }\n" +
            "    }\n" +
            "}\n");
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class RenamedTest {\n" +
            "    public Object method(Object o) {\n" +
            "        Inner inInst = new Inner();\n" +
            "        Inner[] inArr = new Inner[] { inInst, new Inner() };\n" +
            "    }\n" +
            "    private static class Inner {\n" +
            "        public Inner() {\n" + 
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                workingCopy.rewrite(clazz, make.setLabel(clazz, "RenamedTest"));
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                // body rename
                BlockTree block = method.getBody();
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Rename clazz... Test.class -> RenamedTest.class is not correctly generated
     * in method parameter (#92610)
     * 
     */
    public void testRenameClazz() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(Class o) {\n" +
            "        method(Test.class);\n" +
            "    }\n" +
            "}\n");
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class RenamedTest {\n" +
            "    public Object method(Class o) {\n" +
            "        method(RenamedTest.class);\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                workingCopy.rewrite(clazz, make.setLabel(clazz, "RenamedTest"));
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                // body rename
                BlockTree block = method.getBody();
                ExpressionStatementTree est = (ExpressionStatementTree) block.getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                MemberSelectTree mst = (MemberSelectTree) mit.getArguments().get(0);
                workingCopy.rewrite(mst.getExpression(), make.Identifier("RenamedTest"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * Rename in case
     */
    public void testRenameInCase() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    void method() {\n" +
            "        int i = 10;\n" +
            "        switch (i) {\n" +
            "            case 0: {\n" +
            "                System.err.println(i);\n" +
            "            }\n" +
            "            case 1:\n" +
            "                i = 12;\n" +
            "            default:\n" +
            "                i += 7;\n" +
            "                break;\n" +
            "        }\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    void method() {\n" +
            "        int ycko = 10;\n" +
            "        switch (ycko) {\n" +
            "            case 0: {\n" +
            "                System.err.println(ycko);\n" +
            "            }\n" +
            "            case 1:\n" +
            "                ycko = 12;\n" +
            "            default:\n" +
            "                ycko += 7;\n" +
            "                break;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                BlockTree bt = method.getBody();
                VariableTree vt = (VariableTree) bt.getStatements().get(0);
                workingCopy.rewrite(vt, make.setLabel(vt, "ycko"));
                SwitchTree st = (SwitchTree) bt.getStatements().get(1);
                ParenthesizedTree pt = (ParenthesizedTree) st.getExpression();
                workingCopy.rewrite(pt.getExpression(), make.setLabel(pt.getExpression(), "ycko"));
                CaseTree kejs = st.getCases().get(0);
                bt = (BlockTree) kejs.getStatements().get(0);
                ExpressionStatementTree est = (ExpressionStatementTree) bt.getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                workingCopy.rewrite(mit.getArguments().get(0), make.Identifier("ycko"));
                kejs = st.getCases().get(1);
                est = (ExpressionStatementTree) kejs.getStatements().get(0);
                AssignmentTree at = (AssignmentTree) est.getExpression();
                workingCopy.rewrite(at.getVariable(), make.setLabel(at.getVariable(), "ycko"));
                kejs = st.getCases().get(2);
                est = (ExpressionStatementTree) kejs.getStatements().get(0);
                CompoundAssignmentTree cat = (CompoundAssignmentTree) est.getExpression();
                workingCopy.rewrite(cat.getVariable(), make.setLabel(cat.getVariable(), "ycko"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Rename in new in parameter. (Test that issue #98438 is not caused by
     * generator.)
     */
    public void testRenameClazzInNewParameter() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(Class o) {\n" +
            "        method(new Test());\n" +
            "    }\n" +
            "}\n");
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class RenamedTest {\n" +
            "    public Object method(Class o) {\n" +
            "        method(new RenamedTest());\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                workingCopy.rewrite(clazz, make.setLabel(clazz, "RenamedTest"));
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                // body rename
                BlockTree block = method.getBody();
                ExpressionStatementTree est = (ExpressionStatementTree) block.getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                NewClassTree nct = (NewClassTree) mit.getArguments().get(0);
                workingCopy.rewrite(nct.getIdentifier(), make.Identifier("RenamedTest"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #99445: Not well formatted statements when adding statement to body.
     */
    public void test99445() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(Class o) {\n" +
            "        method(new Test());\n" +
            "    }\n" +
            "}\n");
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(Class o) {\n" +
            "        method(new Test());\n" +
            "        System.out.println(\"Test\");\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                ExpressionStatementTree est = make.ExpressionStatement(
                    make.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(),
                        make.MemberSelect(
                            make.MemberSelect(
                                make.Identifier("System"),
                                "out"
                            ),
                            "println"
                        ),
                        Collections.<ExpressionTree>singletonList(
                            make.Literal("Test")
                        )
                    )
                );
                workingCopy.rewrite(block, make.addBlockStatement(block, est));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * #101717: When rename of parameter, -1 was changed to 1.
     */
    public void test101717() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(Class o) {\n" +
            "        method(abcd, -1);\n" +
            "    }\n" +
            "}\n");
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(Class o) {\n" +
            "        method(abcde, -1);\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                MethodInvocationTree mit = (MethodInvocationTree) ((ExpressionStatementTree) block.getStatements().get(0)).getExpression();
                workingCopy.rewrite(mit.getArguments().get(0), make.Identifier("abcde"));
            }
            
        };
        testSource.runModificationTask(task).commit();
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
            "        if (i == 0) {y = 2;} else {y = 9;}\n" +
            "        return y == 8;\n" +
            "    }\n" +
            "}\n");
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public boolean method(int i) {\n" +
            "        int y = 0;\n" +
            "        if (method(null)) {return true;\n" + 
            "}\n" +
            "        return y == 8;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                IfTree mit = (IfTree) block.getStatements().get(1);
                IfTree nue = make.If(
                    make.Parenthesized(
                        make.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(), 
                            make.Identifier("method"), 
                            Arrays.asList(make.Literal(null))
                        )
                    ), 
                    make.Block(Collections.<StatementTree>singletonList(make.Return(make.Literal(true))), false),
                    null
                );
                workingCopy.rewrite(mit, nue);
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRenameInParens() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "import java.io.File;\n" +
            "import java.io.FileInputStream;\n" +
            "import java.io.FileNotFoundException;\n" +
            "\n" +
            "class FileType {\n" +
            "\n" +
            "    File f = new File(\"aaa\");\n" +
            "\n" +
            "    void m() throws FileNotFoundException {\n" +
            "        boolean b = false;\n" +
            "        new FileInputStream((f));\n" +
            "    }\n" +
            "}\n");
         String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "import java.io.File;\n" +
            "import java.io.FileInputStream;\n" +
            "import java.io.FileNotFoundException;\n" +
            "\n" +
            "class FileType {\n" +
            "\n" +
            "    File f = new File(\"aaa\");\n" +
            "\n" +
            "    void m() throws FileNotFoundException {\n" +
            "        boolean b = false;\n" +
            "        new FileInputStream((file));\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(2);
                BlockTree block = method.getBody();
                ExpressionStatementTree est = (ExpressionStatementTree) block.getStatements().get(1);
                NewClassTree nct = (NewClassTree) est.getExpression();
                ParenthesizedTree pareni = (ParenthesizedTree) nct.getArguments().get(0);
                workingCopy.rewrite(pareni.getExpression(), make.Identifier("file"));
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test111983() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "public class NewClass {\n" +
            "    public static <T> T method() {\n" +
            "        return null;\n" +
            "    }\n" +
            "    \n" +
            "    static void m() {\n" +
            "        NewClass.<Class>method();\n" +
            "    }\n" +
            "}\n");
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "public class NewClass {\n" +
            "    public static <T> T metoda() {\n" +
            "        return null;\n" +
            "    }\n" +
            "    \n" +
            "    static void m() {\n" +
            "        NewClass.<Class>metoda();\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(1);
                workingCopy.rewrite(method, make.setLabel(method, "metoda"));
                
                method = (MethodTree)clazz.getMembers().get(2);
                BlockTree block = method.getBody();
                ExpressionStatementTree est = (ExpressionStatementTree) block.getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                MemberSelectTree mst = (MemberSelectTree) mit.getMethodSelect();
                workingCopy.rewrite(mst, make.setLabel(mst, "metoda"));
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    // test 112290
    public void test112290_1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "public class NewClass {\n" +
            "    static void m() {\n" +
            "        int size = 5;\n" +
            "        int[][][] array = new int[size][size][size];\n" +
            "    }\n" +
            "}\n");
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "public class NewClass {\n" +
            "    static void m() {\n" +
            "        int velikost = 5;\n" +
            "        int[][][] array = new int[velikost][velikost][velikost];\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                List<? extends StatementTree> statements = method.getBody().getStatements();
                VariableTree var = (VariableTree) statements.get(0);
                workingCopy.rewrite(var, make.setLabel(var, "velikost"));
                var = (VariableTree) statements.get(1);
                NewArrayTree newArr = (NewArrayTree) var.getInitializer();
                for (ExpressionTree t : newArr.getDimensions()) {
                    workingCopy.rewrite(t, make.Identifier("velikost"));
                }
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    // test 112290
    public void test112290_2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "public class NewClass {\n" +
            "    static void m() {\n" +
            "        int size = 5;\n" +
            "        int[][][] array = new int[size][size][size];\n" +
            "    }\n" +
            "}\n");
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "public class NewClass {\n" +
            "    static void m() {\n" +
            "        int velikost = 5;\n" +
            "        int[][][] array = new int[size][size][velikost];\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                List<? extends StatementTree> statements = method.getBody().getStatements();
                VariableTree var = (VariableTree) statements.get(0);
                workingCopy.rewrite(var, make.setLabel(var, "velikost"));
                var = (VariableTree) statements.get(1);
                NewArrayTree newArr = (NewArrayTree) var.getInitializer();
                workingCopy.rewrite(newArr.getDimensions().get(2), make.Identifier("velikost"));
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    // test 112290
    public void test112290_3() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "public class NewClass {\n" +
            "    static void m() {\n" +
            "        int size = 5;\n" +
            "        int[][][] array = new int[size][size][size];\n" +
            "    }\n" +
            "}\n");
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "public class NewClass {\n" +
            "    static void m() {\n" +
            "        int velikost = 5;\n" +
            "        int[][][] array = new int[velikost][size][size];\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                List<? extends StatementTree> statements = method.getBody().getStatements();
                VariableTree var = (VariableTree) statements.get(0);
                workingCopy.rewrite(var, make.setLabel(var, "velikost"));
                var = (VariableTree) statements.get(1);
                NewArrayTree newArr = (NewArrayTree) var.getInitializer();
                workingCopy.rewrite(newArr.getDimensions().get(0), make.Identifier("velikost"));
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test126460a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        java.util.List<String> l = null;\n" +
            "        assert l.get(0) == 12;\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        java.util.List<String> l = null;\n" +
            "        String name = l.get(0);\n" +
            "        assert name == 12;\n" +
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
                AssertTree ass = (AssertTree) block.getStatements().get(1);
                BinaryTree cond = (BinaryTree) ass.getCondition();
                workingCopy.rewrite(cond.getLeftOperand(), make.Identifier("name"));
                MethodInvocationTree mit = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.Identifier("l"), "get"), Collections.singletonList(make.Literal(0)));
                BlockTree nueBlock = make.insertBlockStatement(block, 1, make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "name", make.Identifier("String"), mit));
                workingCopy.rewrite(block, nueBlock);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test126460b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(int ada) {\n" +
            "        java.util.List<String> l = null;\n" +
            "        assert l.get(0) == 12;\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(int ada) {\n" +
            "        java.util.List<String> l = null;\n" +
            "        String name = l.get(0);\n" +
            "        assert name == 12 : ada;\n" +
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
                AssertTree ass = (AssertTree) block.getStatements().get(1);
                BinaryTree cond = (BinaryTree) ass.getCondition();
                workingCopy.rewrite(cond.getLeftOperand(), make.Identifier("name"));
                workingCopy.rewrite(ass, make.Assert(ass.getCondition(), make.Identifier("ada")));
                MethodInvocationTree mit = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.Identifier("l"), "get"), Collections.singletonList(make.Literal(0)));
                BlockTree nueBlock = make.insertBlockStatement(block, 1, make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "name", make.Identifier("String"), mit));
                workingCopy.rewrite(block, nueBlock);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test126460c() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(int ada) {\n" +
            "        java.util.List<String> l = null;\n" +
            "        assert l.get(0) == 12 : ada;\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(int ada) {\n" +
            "        java.util.List<String> l = null;\n" +
            "        String name = l.get(0);\n" +
            "        assert name == 12;\n" +
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
                AssertTree ass = (AssertTree) block.getStatements().get(1);
                BinaryTree cond = (BinaryTree) ass.getCondition();
                workingCopy.rewrite(cond.getLeftOperand(), make.Identifier("name"));
                workingCopy.rewrite(ass, make.Assert(ass.getCondition(), null));
                MethodInvocationTree mit = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.Identifier("l"), "get"), Collections.singletonList(make.Literal(0)));
                BlockTree nueBlock = make.insertBlockStatement(block, 1, make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "name", make.Identifier("String"), mit));
                workingCopy.rewrite(block, nueBlock);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test159671a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(int p) {\n" +
            "        assert p == 0 : \"p == 0\";\n" +
            "    }\n" +
            "}\n");

         String golden =
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(int p) {\n" +
            "        assert p == 0;\n" +
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
                AssertTree ass = (AssertTree) block.getStatements().get(0);
                AssertTree nue = make.Assert(ass.getCondition(), null);
                workingCopy.rewrite(ass, nue);
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test159671b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(int p) {\n" +
            "        assert p == 0;\n" +
            "    }\n" +
            "}\n");

         String golden =
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(int p) {\n" +
            "        assert p == 0 : \"p == 0\";\n" +
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
                AssertTree ass = (AssertTree) block.getStatements().get(0);
                AssertTree nue = make.Assert(ass.getCondition(), make.Literal("p == 0"));
                workingCopy.rewrite(ass, nue);
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test182542() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void m() { System.err.println(); }\n" +
            "}\n");

         String golden =
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void m() {}\n" +
            "}\n";

        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                workingCopy.rewrite(block, make.removeBlockStatement(block, 0));
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test229144() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void m() {\n" +
            "        System.err.println(java.lang.String.class);\n" +
            "    }\n" +
            "}\n");

         String golden =
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void m() {\n" +
            "        System.err.println(java.lang.CharSequence.class);\n" +
            "    }\n" +
            "}\n";

        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.PARSED);//must be parsed, so that the member selects don't have symbols!
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(0);
                BlockTree block = method.getBody();
                List<StatementTree> nueStatements = Arrays.asList(workingCopy.getTreeUtilities().parseStatement("System.err.println(java.lang.CharSequence.class);", new SourcePositions[1]));
                workingCopy.rewrite(block, make.Block(nueStatements, false));
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    // methods not used in this test.
    String getGoldenPckg() {
        return "";
    }
    
    String getSourcePckg() {
        return "";
    }
    
}
