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

import java.io.*;
import java.util.*;

import com.sun.source.tree.*;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.GeneratorUtilities;

import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.java.source.*;
import static org.netbeans.api.java.source.JavaSource.*;

import org.netbeans.junit.NbTestSuite;

/**
 * Tests indentation of newly generated body text in method.
 *
 * @author Pavel Flaska
  */
public class MethodBodyTextTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of MethodBodyTextTest */
    public MethodBodyTextTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
//        suite.addTestSuite(MethodBodyTextTest.class);
//        suite.addTest(new MethodBodyTextTest("testSetBodyText"));
//        suite.addTest(new MethodBodyTextTest("testCreateWithBodyText"));
        suite.addTest(new MethodBodyTextTest("testCreateReturnBooleanBodyText"));
//        suite.addTest(new MethodBodyTextTest("testModifyBodyText"));
        suite.addTest(new MethodBodyTextTest("testReplaceConstrBody"));
        suite.addTest(new MethodBodyTextTest("testReplaceMethod"));
        suite.addTest(new MethodBodyTextTest("testReplaceMethodBody1"));
        suite.addTest(new MethodBodyTextTest("testReplaceMethodBody2"));
        suite.addTest(new MethodBodyTextTest("testReplaceMethodBody3"));
        suite.addTest(new MethodBodyTextTest("testReplaceMethodBodyImports"));
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "MethodBodyText.java");
    }
    
    public void testSetBodyText() throws java.io.IOException, FileStateInvalidException {
        System.err.println("testSetBodyText");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        MethodTree node = (MethodTree) clazz.getMembers().get(1);
                        BlockTree newBody = make.createMethodBody(node, "{ System.err.println(\"Nothing.\"); }");
                        workingCopy.rewrite(node.getBody(), newBody);
                    }
                }
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        String golden = TestUtilities.copyFileToString(
            getFile(getGoldenDir(), getGoldenPckg() + "testSetBodyText_MethodBodyTextTest.pass")
        );
        assertEquals(golden, res);
    }
    
    public void testCreateWithBodyText() throws java.io.IOException, FileStateInvalidException {
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        StringBuffer body = new StringBuffer();
                        body.append("{ System.out.println(\"Again Nothing\"); }");
                        MethodTree method = make.Method(
                            make.Modifiers(Collections.singleton(Modifier.PUBLIC)),
                            "method2",
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            Collections.<VariableTree>emptyList(),
                            Collections.<ExpressionTree>emptyList(),
                            body.toString(),
                            null
                        );
                        ClassTree copy = make.addClassMember(clazz, method);
                        workingCopy.rewrite(clazz, copy);
                    }
                }
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        String golden = TestUtilities.copyFileToString(
            getFile(getGoldenDir(), getGoldenPckg() + "testCreateWithBodyText_MethodBodyTextTest.pass")
        );
        assertEquals(golden, res);
    }
    
    public void testCreateReturnBooleanBodyText() throws java.io.IOException, FileStateInvalidException {
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree node  = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                StringBuffer body = new StringBuffer();
                body.append("{ return false; }");
                MethodTree method = make.Method(
                        make.Modifiers(Collections.singleton(Modifier.PUBLIC)),
                        "equals",
                        make.PrimitiveType(TypeKind.BOOLEAN),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        body.toString(),
                        null
                        );
                ClassTree clazz = make.addClassMember(node, method);
                workingCopy.rewrite(node, clazz);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        // there is "return 0" instead
        String result = TestUtilities.copyFileToString(testFile);
        //System.err.println(result);
        assertTrue(result.contains("return false"));
    }
    
    public void testModifyBodyText() throws java.io.IOException, FileStateInvalidException {
        System.err.println("testModifyBodyText");
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        MethodTree node = (MethodTree) clazz.getMembers().get(1);
                        String body = "{ List l; }";
                        BlockTree copy = make.createMethodBody(node, body);
                        workingCopy.rewrite(node.getBody(), copy);
                    }
                }
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        String golden = TestUtilities.copyFileToString(
            getFile(getGoldenDir(), getGoldenPckg() + "testModifyBodyText_MethodBodyTextTest.pass")
        );
        assertEquals(golden, res);
    }

    /**
     * Replace constructor body. -- In old constructor, syntetic super()
     * was in the body, no syntetic element in new constructor body. 
     * 
     * #93740
     */
    public void testReplaceConstrBody() throws Exception {
        System.err.println("testReplaceConstrBody");
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "    public Object method() {\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "        System.err.println(null);\n" +
            "    }\n" +
            "    \n" +
            "    public Object method() {\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        MethodTree method = (MethodTree) clazz.getMembers().get(0);
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
                        BlockTree newBody = make.Block(
                                Collections.<StatementTree>singletonList(statement),
                                false
                        );
                        workingCopy.rewrite(method.getBody(), newBody);
                    }
                }
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #93730 - incorrectly diffed method invocation.
     */
    public void testReplaceMethod() throws Exception {
        System.err.println("testReplaceMethod");
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "    public Object method() {\n" +
            "        for(int i = 0; i < 10; i++) {\n" +
            "            System.out.println(\"In loop\");\n" +
            "        }\n" +
            "        Thread.currentThread();\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "    public Object method() {\n" +
            "        System.out.println(\"Ahoj svete!\");\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree meth = (MethodTree) clazz.getMembers().get(1);
                String bodyText = "{System.out.println(\"Ahoj svete!\");}";
                MethodTree newMeth = make.Method(
                        meth.getModifiers(),
                        meth.getName(),
                        meth.getReturnType(),
                        meth.getTypeParameters(),
                        meth.getParameters(),
                        meth.getThrows(),
                        bodyText,
                        (ExpressionTree) meth.getDefaultValue()
                );
                workingCopy.rewrite(meth.getBody(), newMeth.getBody());
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testReplaceMethodBody1() throws Exception {
        System.err.println("testReplaceMethodBody1");
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "    public Object method() {\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "    public Object method() {\n" +
            "        return new Integer(5);\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree meth = (MethodTree) clazz.getMembers().get(1);
                String bodyText = "{return new Integer(5);}";
                MethodTree newMeth = make.Method(
                        meth.getModifiers(),
                        meth.getName(),
                        meth.getReturnType(),
                        meth.getTypeParameters(),
                        meth.getParameters(),
                        meth.getThrows(),
                        bodyText,
                        (ExpressionTree) meth.getDefaultValue()
                );
                workingCopy.rewrite(meth.getBody(), newMeth.getBody());
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testReplaceMethodBody2() throws Exception {
        System.err.println("testReplaceMethodBody2");
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "    public float method() {\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "    public float method() {\n" +
            "        return 0.0F;\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree meth = (MethodTree) clazz.getMembers().get(1);
                String bodyText = "{return 0.0f;}";
                MethodTree newMeth = make.Method(
                        meth.getModifiers(),
                        meth.getName(),
                        meth.getReturnType(),
                        meth.getTypeParameters(),
                        meth.getParameters(),
                        meth.getThrows(),
                        bodyText,
                        (ExpressionTree) meth.getDefaultValue()
                );
                workingCopy.rewrite(meth.getBody(), newMeth.getBody());
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #90186 regression test
    public void testReplaceMethodBody3() throws Exception {
        System.err.println("testReplaceMethodBody3");
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "    public float method() {\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "    public float method() {\n" +
            "        int hash;\n" +
            "        hash += 2;\n" +
            "        return hash;\n" +
            "    }\n" +
            "}\n";
      
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree meth = (MethodTree) clazz.getMembers().get(1);
                String bodyText = "{ int hash; hash += 2; return hash; }";
                MethodTree newMeth = make.Method(
                        meth.getModifiers(),
                        meth.getName(),
                        meth.getReturnType(),
                        meth.getTypeParameters(),
                        meth.getParameters(),
                        meth.getThrows(),
                        bodyText,
                        (ExpressionTree) meth.getDefaultValue()
                );
                workingCopy.rewrite(meth.getBody(), newMeth.getBody());
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testReplaceMethodBodyImports() throws Exception {
        System.err.println("testReplaceMethodBody3");
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "    public float method() {\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "    public float method() {\n" +
            "        List list = new ArrayList();\n" +
            "        int hash;\n" +
            "        hash += 2;\n" +
            "        return hash;\n" +
            "    }\n" +
            "}\n";
      
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree meth = (MethodTree) clazz.getMembers().get(1);
                String bodyText = "{ java.util.List list = new java.util.ArrayList(); int hash; hash += 2; return hash; }";
                MethodTree newMeth = make.Method(
                        meth.getModifiers(),
                        meth.getName(),
                        meth.getReturnType(),
                        meth.getTypeParameters(),
                        meth.getParameters(),
                        meth.getThrows(),
                        bodyText,
                        (ExpressionTree) meth.getDefaultValue()
                );
                workingCopy.rewrite(meth.getBody(), GeneratorUtilities.get(workingCopy).importFQNs(newMeth.getBody()));
                
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    String getSourcePckg() {
        return "org/netbeans/test/codegen/indent/";
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/indent/MethodBodyTextTest/";
    }

}
