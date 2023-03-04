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
import com.sun.source.util.SourcePositions;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CodeStyle.WrapStyle;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.save.Reformatter;
import org.netbeans.modules.java.ui.FmtOptions;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Pavel Flaska
 */
public class MethodBodyTest extends GeneratorTestBase {
    
    /** Creates a new instance of MethodBodyTest */
    public MethodBodyTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(MethodBodyTest.class);
//        suite.addTest(new MethodBodyTest("testAddFirstStatement"));
//        suite.addTest(new MethodBodyTest("testAddBodyText"));
//        suite.addTest(new MethodBodyTest("testAddVarDecl"));
//        suite.addTest(new MethodBodyTest("testReplaceConstructorBody"));
//        suite.addTest(new MethodBodyTest("testSwitchStatement"));
//        suite.addTest(new MethodBodyTest("test187557a"));
//        suite.addTest(new MethodBodyTest("test187557b"));
//        suite.addTest(new MethodBodyTest("test187557c"));
        return suite;
    }

    /**
     * Add first method body statement
     */
    public void testAddFirstStatement() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        System.out.println(\"test\");\n" +
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
                            make.Literal("test")
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
     * Add method body as a text
     */
    public void testAddBodyText() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        System.out.println(\"test\");\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree newBody = make.createMethodBody(method, "{ System.out.println(\"test\"); }");
                workingCopy.rewrite(method.getBody(), newBody);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * "Map env = new HashMap();"
     */
    public void testAddVarDecl() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "import java.util.HashMap;\n" +
            "import java.util.Map;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method() {\n" +
            "        Map env = new HashMap();\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker treeMaker = workingCopy.getTreeMaker();
                TypeElement hashMapClass = workingCopy.getElements().getTypeElement("java.util.HashMap"); // NOI18N
                ExpressionTree hashMapEx = treeMaker.QualIdent(hashMapClass);
                TypeElement mapClass = workingCopy.getElements().getTypeElement("java.util.Map");// NOI18N
                ExpressionTree mapEx = treeMaker.QualIdent(mapClass);
                NewClassTree mapConstructor = treeMaker.NewClass(
                        null,
                        Collections.<ExpressionTree>emptyList(),
                        hashMapEx,
                        Collections.<ExpressionTree>emptyList(), null
                );
                VariableTree vt = treeMaker.Variable( treeMaker.Modifiers(
                        Collections.<Modifier>emptySet(),
                        Collections.<AnnotationTree>emptyList()
                        ), "env", mapEx, mapConstructor
                );
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                workingCopy.rewrite(method.getBody(), treeMaker.addBlockStatement(method.getBody(), vt));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * diff switch statement
     */
    public void XtestSwitchStatement() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void method() {\n" +
            "        int i = 3;\n" +
            "        switch (i) {\n" +
            "            case 1: System.err.println(); break;\n" +
            "            default: break;\n" +
            "        }\n" + 
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void method() {\n" +
            "        int i = 3;\n" +
            "        switch (i) {\n" +
            "            case 1: System.err.println(); break;\n" +
            "            case 2: System.err.println(); break;\n" +
            "            default:  break;\n" +
            "        }\n" + 
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker treeMaker = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                SwitchTree switchStatement = (SwitchTree) method.getBody().getStatements().get(1);
                
                List<CaseTree> cases = new LinkedList<CaseTree>();
                
                cases.add(treeMaker.Case(treeMaker.Literal(1), switchStatement.getCases().get(0).getStatements()));
                cases.add(treeMaker.Case(treeMaker.Literal(2), switchStatement.getCases().get(0).getStatements()));
                cases.add(treeMaker.Case((ExpressionTree) null, Collections.singletonList(treeMaker.Break(null))));
                
                workingCopy.rewrite(switchStatement, treeMaker.Switch(switchStatement.getExpression(), cases));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test117054a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void method() {\n" +
            "        new Runnable() {}.\n" + 
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void method() {\n" +
            "        new Runnable() {\n" + 
            "            public void run() {\n" +
            "            }\n" +
            "        }.\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker treeMaker = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                final NewClassTree[] nctFin = new NewClassTree[1];
                
                new ErrorAwareTreeScanner() {
                    @Override
                    public Object visitNewClass(NewClassTree node, Object p) {
                        nctFin[0] = node;
                        return null;
                    }
                }.scan(method.getBody().getStatements().get(0), null);
                
                assertNotNull(nctFin[0]);
                
                NewClassTree nct = nctFin[0];
                ModifiersTree mods = treeMaker.Modifiers(EnumSet.of(Modifier.PUBLIC));
                Tree returnType = treeMaker.Type(workingCopy.getTypes().getNoType(TypeKind.VOID));
                MethodTree nueMethod = treeMaker.Method(mods, "run", returnType, Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), "{}", null);
                
                workingCopy.rewrite(nct.getClassBody(), treeMaker.addClassMember(nct.getClassBody(), nueMethod));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test117054b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void method() {\n" +
            "        Runnable r = new Runnable() {}.\n" + 
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void method() {\n" +
            "        Runnable r = new Runnable() {\n" + 
            "            public void run() {\n" +
            "            }\n" +
            "        }.\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker treeMaker = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                final NewClassTree[] nctFin = new NewClassTree[1];
                
                new ErrorAwareTreeScanner() {
                    @Override
                    public Object visitNewClass(NewClassTree node, Object p) {
                        nctFin[0] = node;
                        return null;
                    }
                }.scan(method.getBody().getStatements().get(0), null);
                
                assertNotNull(nctFin[0]);
                
                NewClassTree nct = nctFin[0];
                ModifiersTree mods = treeMaker.Modifiers(EnumSet.of(Modifier.PUBLIC));
                Tree returnType = treeMaker.Type(workingCopy.getTypes().getNoType(TypeKind.VOID));
                MethodTree nueMethod = treeMaker.Method(mods, "run", returnType, Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), "{}", null);
                
                workingCopy.rewrite(nct.getClassBody(), treeMaker.addClassMember(nct.getClassBody(), nueMethod));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test187557a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void method() {\n" +
            "        return ;\n" + 
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void method() {\n" +
            "        return 1;\n" + 
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker treeMaker = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ReturnTree rt = (ReturnTree) method.getBody().getStatements().get(0);
                
                workingCopy.rewrite(rt, treeMaker.Return(treeMaker.Literal(1)));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test187557b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void method() {\n" +
            "        return;\n" + 
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void method() {\n" +
            "        return 1;\n" + 
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker treeMaker = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ReturnTree rt = (ReturnTree) method.getBody().getStatements().get(0);
                
                workingCopy.rewrite(rt, treeMaker.Return(treeMaker.Literal(1)));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test187557c() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void method() {\n" +
            "        return 1;\n" + 
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void method() {\n" +
            "        return;\n" + 
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker treeMaker = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ReturnTree rt = (ReturnTree) method.getBody().getStatements().get(0);
                
                workingCopy.rewrite(rt, treeMaker.Return(null));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testSplitDeclarationAndAssignment208270() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void method() {\n" +
            "        Runnable r = new Runnable() {\n" +
            "\n" + //intentional empty line - this was duplicated
            "            @Override\n" +
            "            public void run() {\n" +
            "                throw new UnsupportedOperationException();\n" +
            "            }\n" +
            "        };\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void method() {\n" +
            "        Runnable r;\n" +
            "        r = new Runnable() {\n" +
            "            \n" + //only one line expected
            "            @Override\n" +
            "            public void run() {\n" +
            "                throw new UnsupportedOperationException();\n" +
            "            }\n" +
            "        };\n" +
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
                
                workingCopy.rewrite(method.getBody(), make.addBlockStatement(method.getBody(), make.ExpressionStatement(make.Assignment(make.Identifier("r"), var.getInitializer()))));
                workingCopy.rewrite(var, make.setInitialValue(var, null));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Replace constructor body, lhasik's test-case #111769
     */
    public void XtestReplaceConstructorBody() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "        super(1, \"Tester\");\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(0);
                TreeUtilities treeUtils = workingCopy.getTreeUtilities();
                Tree newBlock = treeUtils.parseStatement("{ super(1, \"Tester\"); }", new SourcePositions[1]);
                workingCopy.rewrite(method.getBody(), newBlock);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testChainedMethodCallsDotPlacement() throws Exception {
        performChainedMethodCallsDotPlacementTest();
        performChainedMethodCallsDotPlacementTest(FmtOptions.wrapChainedMethodCalls, WrapStyle.WRAP_ALWAYS.name(), FmtOptions.wrapAfterDotInChainedMethodCalls, Boolean.TRUE.toString());
        performChainedMethodCallsDotPlacementTest(FmtOptions.wrapChainedMethodCalls, WrapStyle.WRAP_ALWAYS.name(),
                                                  FmtOptions.wrapAfterDotInChainedMethodCalls, Boolean.FALSE.toString());
    }
    
    private void performChainedMethodCallsDotPlacementTest(String... settings) throws Exception {
        Map<String, String> originalSettings = alterSettings(settings);
        
        try {
            testFile = new File(getWorkDir(), "Test.java");
            TestUtilities.copyStringToFile(testFile, 
                "package personal;\n" +
                "\n" +
                "public class Test {\n\n" +
                "    public void method(String str) {\n" +
                "        return 1;\n" + 
                "    }\n" +
                "}\n");

             String golden = 
                "package personal;\n" +
                "\n" +
                "public class Test {\n\n" +
                "    public void method(String str) {\n" +
                "        return str.toString().length();\n" + 
                "    }\n" +
                "}\n";

            JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
            Task<WorkingCopy> task = new Task<WorkingCopy>() {

                public void run(final WorkingCopy workingCopy) throws java.io.IOException {
                    workingCopy.toPhase(Phase.RESOLVED);
                    TreeMaker treeMaker = workingCopy.getTreeMaker();
                    new ErrorAwareTreeScanner<Void, Void>() {
                        @Override public Void visitReturn(ReturnTree node, Void p) {
                            ExpressionTree parsed = workingCopy.getTreeUtilities().parseExpression("str.toString().length()", new SourcePositions[1]);
                            workingCopy.rewrite(node.getExpression(), parsed);
                            return super.visitReturn(node, p);
                        }
                    }.scan(workingCopy.getCompilationUnit(), null);
                }

            };
            testSource.runModificationTask(task).commit();
            String res = TestUtilities.copyFileToString(testFile);
            String formattedRes = Reformatter.reformat(res.replaceAll("[\\s]+", " "), CodeStyle.getDefault(FileUtil.toFileObject(testFile)));
            //System.err.println(res);
            //System.err.println(formattedRes);
            assertEquals(formattedRes, res);
            assertEquals(golden.replaceAll("\\s", ""), res.replaceAll("\\s", ""));
        } finally {
            reset(originalSettings);
        }
    }
    
    private Map<String, String> alterSettings(String... settings) {
        Map<String, String> adjustPreferences = new HashMap<String, String>();
        for (int i = 0; i < settings.length; i += 2) {
            adjustPreferences.put(settings[i], settings[i + 1]);
        }
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        Map<String, String> origValues = new HashMap<String, String>();
        for (String key : adjustPreferences.keySet()) {
            origValues.put(key, preferences.get(key, null));
        }
        setValues(preferences, adjustPreferences);
        return origValues;
    }
    
    private void reset(Map<String, String> values) {
        setValues(MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class), values);
    }
    
    private void setValues(Preferences p, Map<String, String> values) {
        for (Entry<String, String> e : values.entrySet()) {
            if (e.getValue() != null) {
                p.put(e.getKey(), e.getValue());
            } else {
                p.remove(e.getKey());
            }
        }
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

}
