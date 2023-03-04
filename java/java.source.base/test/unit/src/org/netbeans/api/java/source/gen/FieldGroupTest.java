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
import com.sun.source.tree.*;
import com.sun.source.tree.Tree.Kind;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbCollections;

/**
 *
 * @author Pavel Flaska
 */
public class FieldGroupTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of FieldGroupTest */
    public FieldGroupTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(FieldGroupTest.class);
//        suite.addTest(new FieldGroupTest("testFieldGroup1"));
//        suite.addTest(new FieldGroupTest("testFieldGroup2"));
//        suite.addTest(new FieldGroupTest("testFieldGroup3"));
//        suite.addTest(new FieldGroupTest("testFieldGroup4"));
//        suite.addTest(new FieldGroupTest("testFieldGroup5"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBody1"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBody2"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBody3"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBody4"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBody5"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBody6"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBody7"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBodyCast1"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBodyCast2"));
//        suite.addTest(new FieldGroupTest("test114571"));
//        suite.addTest(new FieldGroupTest("testFieldGroupModifiers"));
//        suite.addTest(new FieldGroupTest("testNoFieldGroup"));
//        suite.addTest(new FieldGroupTest("testRemoveFromFieldGroup"));
//        suite.addTest(new FieldGroupTest("testRenameInVariableGroupInFor175866a"));
//        suite.addTest(new FieldGroupTest("testRenameInVariableGroupInFor175866b"));
//        suite.addTest(new FieldGroupTest("testRenameInVariableGroupInFor175866c"));
//        suite.addTest(new FieldGroupTest("testRenameInVariableGroupInFor175866f"));
//        suite.addTest(new FieldGroupTest("testRemoveFirstVariable"));
        return suite;
    }
    
    /**
     */
    public void testFieldGroup1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int a, b, c, d;\n" +
            "    int e;\n" +
            "    \n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int a, b, c, d;\n" +
            "    int ecko;\n" +
            "    \n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree vt = (VariableTree) clazz.getMembers().get(5);
                workingCopy.rewrite(vt, make.setLabel(vt, "ecko"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     */
    public void testFieldGroup2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int e;\n" +
            "    int a, b, c, d;\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int ecko;\n" +
            "    int a, b, c, d;\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree vt = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(vt, make.setLabel(vt, "ecko"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
       
    public void testFieldGroup3() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    int a, becko = 10, c = 25;\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    int a, what = 10, c = 25;\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree vt = (VariableTree) clazz.getMembers().get(2);
                workingCopy.rewrite(vt, make.setLabel(vt, "what"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroup4() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    int a, becko = 10, c = 25;\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    int acko, becko = 10, c = 25;\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree vt = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(vt, make.setLabel(vt, "acko"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroup5() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    int a, becko = 10, c = 25;\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    int a, becko = 10, cecko = 25;\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree vt = (VariableTree) clazz.getMembers().get(3);
                workingCopy.rewrite(vt, make.setLabel(vt, "cecko"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroup6() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    /** Javadoc */\n" +
            "    int a, becko = 10, c = 25;\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    /** Javadoc */\n" +
            "    int a, what = 10, c = 25;\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                GeneratorUtilities.get(workingCopy).importComments(workingCopy.getCompilationUnit(), workingCopy.getCompilationUnit());
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree vt = (VariableTree) clazz.getMembers().get(2);
                workingCopy.rewrite(vt, make.setLabel(vt, "what"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupInBody1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        System.out.println(\"Test\");\n" +
            "        int a, becko = 10, c = 25;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        System.out.println(\"Test\");\n" +
            "        int a, becko = 10, cecko = 25;\n" +
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
                VariableTree vt = (VariableTree) block.getStatements().get(3);
                workingCopy.rewrite(vt, make.setLabel(vt, "cecko"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupInBody2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        System.out.println(\"Test\");\n" +
            "        int a, becko = 10, c = 25;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        System.out.println(\"Test\");\n" +
            "        int a, b = 10, c = 25;\n" +
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
                VariableTree vt = (VariableTree) block.getStatements().get(2);
                workingCopy.rewrite(vt, make.setLabel(vt, "b"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupInBody3() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        System.out.println(\"Test\");\n" +
            "        int a, becko = 10, c = 25;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        System.out.println(\"Test\");\n" +
            "        int acko, becko = 10, c = 25;\n" +
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
                VariableTree vt = (VariableTree) block.getStatements().get(1);
                workingCopy.rewrite(vt, make.setLabel(vt, "acko"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    
    public void testFieldGroupInBody4() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        int a, becko = 10, c = 25;\n" +
            "        System.out.println(\"Test\");\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        int a, becko = 10, cecko = 25;\n" +
            "        System.out.println(\"Test\");\n" +
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
                VariableTree vt = (VariableTree) block.getStatements().get(2);
                workingCopy.rewrite(vt, make.setLabel(vt, "cecko"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupInBody5() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        int a, becko = 10, c = 25;\n" +
            "        System.out.println(\"Test\");\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        int a, b = 10, c = 25;\n" +
            "        System.out.println(\"Test\");\n" +
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
                VariableTree vt = (VariableTree) block.getStatements().get(1);
                workingCopy.rewrite(vt, make.setLabel(vt, "b"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupInBody6() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        int a, becko = 10, c = 25;\n" +
            "        System.out.println(\"Test\");\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        int acko, becko = 10, c = 25;\n" +
            "        System.out.println(\"Test\");\n" +
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
                VariableTree vt = (VariableTree) block.getStatements().get(0);
                workingCopy.rewrite(vt, make.setLabel(vt, "acko"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupInBodyCast1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        Object o11=null; Object o21=null;\n" +
            "        Widget w1=o11,w2=o21;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        Object o11=null; Object o21=null;\n" +
            "        Widget w1=(Widget) o11,w2=o21;\n" +
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
                VariableTree vt = (VariableTree) block.getStatements().get(2);
                ExpressionTree init = vt.getInitializer();
                workingCopy.rewrite(init, make.TypeCast(make.Identifier("Widget"), init));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupInBodyCast2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        Object o11=null; Object o21=null;\n" +
            "        Widget w1=o11,w2=o21;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        Object o11=null; Object o21=null;\n" +
            "        Widget w1=o11,w2=(Widget) o21;\n" +
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
                VariableTree vt = (VariableTree) block.getStatements().get(3);
                ExpressionTree init = vt.getInitializer();
                workingCopy.rewrite(init, make.TypeCast(make.Identifier("Widget"), init));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    // test 114571
    public void test114571() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class MyOuterClass {\n" +
            "    public MyOuterClass c = new MyOuterClass(), b= new MyOuterClass();\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class MOC {\n" +
            "    public MOC c = new MOC(), b= new MOC();\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                
                workingCopy.rewrite(clazz, make.setLabel(clazz, "MOC"));
                
                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                NewClassTree nct = (NewClassTree) var.getInitializer();
                ExpressionTree ident = nct.getIdentifier();
                workingCopy.rewrite(ident, make.setLabel(ident, "MOC"));
                workingCopy.rewrite(var.getType(), make.Identifier("MOC"));
                
                var = (VariableTree) clazz.getMembers().get(2);
                nct = (NewClassTree) var.getInitializer();
                ident = nct.getIdentifier();
                workingCopy.rewrite(ident, make.setLabel(ident, "MOC"));
                workingCopy.rewrite(var.getType(), make.Identifier("MOC"));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void XtestFieldGroupModifiers() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class MyOuterClass {\n" +
            "    public boolean a, b, c;\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class MyOuterClass {\n" +
            "    private boolean a;\n" + 
            "    public boolean b, c;\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                
                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(var.getModifiers(), make.Modifiers(EnumSet.of(Modifier.PRIVATE)));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testNoFieldGroup() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class MyOuterClass {\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class MyOuterClass {\n" +
            "\n" +
            "    private Exception a;\n" + 
            "    private String b;\n" + 
            "    private Object c;\n" + 
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PRIVATE));
                Elements e = workingCopy.getElements();
                
                ClassTree nue = make.insertClassMember(clazz, 0, make.Variable(mods, "c", make.QualIdent(e.getTypeElement("java.lang.Object")), null));
                nue = make.insertClassMember(nue, 0, make.Variable(mods, "b", make.QualIdent(e.getTypeElement("java.lang.String")), null));
                nue = make.insertClassMember(nue, 0, make.Variable(mods, "a", make.QualIdent(e.getTypeElement("java.lang.Exception")), null));
                workingCopy.rewrite(clazz, nue);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveFromFieldGroup() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class MyOuterClass {\n" +
            "    public boolean a, b, c;\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class MyOuterClass {\n" +
            "    public boolean b, c;\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                
                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(clazz, make.removeClassMember(clazz, var));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRenameInVariableGroupInFor175866a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (int j=0, k=0; j<1; j++);\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (int j=0, l=0; j<1; j++);\n" +
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
                ForLoopTree flt = (ForLoopTree) block.getStatements().get(0);
                VariableTree secondVar = (VariableTree) flt.getInitializer().get(1);
                workingCopy.rewrite(secondVar, make.setLabel(secondVar, "l"));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRenameInVariableGroupInFor175866b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (int j=0; j<1; j++);\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (int j=0, l = 0; j<1; j++);\n" +
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
                ForLoopTree flt = (ForLoopTree) block.getStatements().get(0);
                VariableTree firstVar = (VariableTree) flt.getInitializer().get(0);
                VariableTree secondVar = make.Variable(firstVar.getModifiers(), "l", firstVar.getType(), make.Literal(0));

                workingCopy.rewrite(flt, make.addForLoopInitializer(flt, secondVar));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRenameInVariableGroupInFor175866c() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (int j=0; j<1; j++);\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (int l = 0, j=0; j<1; j++);\n" +
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
                ForLoopTree flt = (ForLoopTree) block.getStatements().get(0);
                VariableTree firstVar = (VariableTree) flt.getInitializer().get(0);
                VariableTree secondVar = make.Variable(firstVar.getModifiers(), "l", firstVar.getType(), make.Literal(0));

                workingCopy.rewrite(flt, make.insertForLoopInitializer(flt, 0, secondVar));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRenameInVariableGroupInFor175866d() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (int j=0, k=0, l=0; j<1; j++);\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (int j=0, k=0; j<1; j++);\n" +
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
                ForLoopTree flt = (ForLoopTree) block.getStatements().get(0);
                workingCopy.rewrite(flt, make.removeForLoopInitializer(flt, 2));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRenameInVariableGroupInFor175866e() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (int j=0, k=0, l=0; j<1; j++);\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (int j=0, l=0; j<1; j++);\n" +
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
                ForLoopTree flt = (ForLoopTree) block.getStatements().get(0);
                workingCopy.rewrite(flt, make.removeForLoopInitializer(flt, 1));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRenameInVariableGroupInFor175866f() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (int j=0, k=0, l=0; j<1; j++);\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        for (int k=0, l=0; j<1; j++);\n" +
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
                ForLoopTree flt = (ForLoopTree) block.getStatements().get(0);
                workingCopy.rewrite(flt, make.removeForLoopInitializer(flt, 0));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveFirstVariable() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        int i,j,k;\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        int j,k;\n" +
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
                workingCopy.rewrite(block, make.removeBlockStatement(block, 0));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveLastVariable() throws Exception { // #213252
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public int j,k = 1;\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public int j;\n" +
            "    private int k = 1;\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                
                VariableTree var2 = (VariableTree) clazz.getMembers().get(2);
                VariableTree newNode = make.Variable(
                                     make.Modifiers(EnumSet.of(Modifier.PRIVATE)),
                                     var2.getName(), var2.getType(), var2.getInitializer());
                workingCopy.rewrite(var2, newNode);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testMove187766() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    public void method() {\n" +
            "        int i,j,k;\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    int i,j,k;\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                ClassTree nueClazz = make.removeClassMember(clazz, 1);

                nueClazz = make.addClassMember(nueClazz, block.getStatements().get(0));
                nueClazz = make.addClassMember(nueClazz, block.getStatements().get(1));
                nueClazz = make.addClassMember(nueClazz, block.getStatements().get(2));

                workingCopy.rewrite(clazz, nueClazz);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupComments213529a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    int i,j,k;\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    /* i */\n" +
            "    int i,\n" +
            "    /* j */\n" +
            "    j,\n" +
            "    /* k */\n" +
            "    k;\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                for (Tree m : clazz.getMembers()) {
                    if (m.getKind() != Kind.VARIABLE) continue;
                    VariableTree vt = (VariableTree) m;
                    Tree nue = make.setLabel(m, vt.getName());
                    make.addComment(nue, Comment.create(vt.getName().toString()), true);
                    workingCopy.rewrite(m, nue);
                }
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupComments213529b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    int i = 1,\n" +
            "        j = 2,\n" +
            "        k = 3;\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    /* i */\n" +
            "    int i = 1,\n" +
            "        /* j */\n" +
            "        j = 2,\n" +
            "        /* k */\n" +
            "        k = 3;\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                for (Tree m : clazz.getMembers()) {
                    if (m.getKind() != Kind.VARIABLE) continue;
                    VariableTree vt = (VariableTree) m;
                    Tree nue = make.setLabel(m, vt.getName());
                    make.addComment(nue, Comment.create(vt.getName().toString()), true);
                    workingCopy.rewrite(m, nue);
                }
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testMultipleClasses1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication1;\n" +
            "\n" +
            "class Test {\n" +
            "}\n\n" +
            "class A {\n" +
            "    int a,b;\n" +
            "    Object o;\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "class Test {\n" +
            "\n" +
            "    Object l;\n" +
            "}\n\n" +
            "class A {\n" +
            "    int a,b;\n" +
            "    Object o;\n" +
            "    Object l;\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                List<ClassTree> classes = NbCollections.checkedListByCopy(workingCopy.getCompilationUnit().getTypeDecls(), ClassTree.class, true);
                for (ClassTree clazz  : classes) {
                    workingCopy.rewrite(clazz, make.addClassMember(clazz, make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)),
                                                                                        "l",
                                                                                        make.Type("java.lang.Object"),
                                                                                        null)));
                }
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testMultipleClasses2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication1;\n" +
            "\n" +
            "class Test {\n" +
            "    int a,b;\n" +
            "    Object o;\n" +
            "}\n"
            );
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "class Test {\n" +
            "    Object o;\n\n" +
            "    class I {\n\n" +
//            "        int a,b;\n" + //XXX: this would be better (but much harder)
            "        int a;\n" +
            "        int b;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                Tree var1 = clazz.getMembers().get(1);
                Tree var2 = clazz.getMembers().get(2);
                workingCopy.rewrite(clazz, make.addClassMember(make.removeClassMember(make.removeClassMember(clazz, 1), 1),
                                                               make.Class(make.Modifiers(EnumSet.noneOf(Modifier.class)),
                                                                          "I",
                                                                          Collections.<TypeParameterTree>emptyList(),
                                                                          null,
                                                                          Collections.<Tree>emptyList(),
                                                                          Arrays.asList(var1, var2))));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Checks that comments are not duplicated when the field group is torn apart
     * @throws Exception 
     */
    public void testFieldGroupComments215629() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package test;\n" +
            "\n" +
            "class Source {\n" +
            "    void foo() {\n" +
            "        // Some comment1\n" +
            "        int i1 = throwSomething(), i2 = throwSomething2();\n" +
            "        // Some comment2\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package test;\n" +
            "\n" +
            "class Source {\n" +
            "    void foo() {\n" +
            "        // Some comment1\n" +
            "        int i1 = throwSomething();\n" +
            "        ;\n" +
            "        int i2 = throwSomething2();\n" +
            "        // Some comment2\n" +
            "        \n" + // TODO, should not be here, caused by diff-ing even i2 against the original field group
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree mt = (MethodTree)clazz.getMembers().get(1);
                BlockTree oldB = mt.getBody();
                GeneratorUtilities.get(workingCopy).importComments(oldB, workingCopy.getCompilationUnit());
                List<StatementTree> stmts = new ArrayList<StatementTree>();
                stmts.add(oldB.getStatements().get(0));
                stmts.add(make.EmptyStatement());
                stmts.add(oldB.getStatements().get(1));
                workingCopy.rewrite(oldB, make.Block(stmts, false));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Checks that variable decls copied out from the original VarGroupTree form a variable group.
     * d,e,f are copied verbatim, so they should form a variable group. The code copies the matching variables out from
     * the original place as a block of text.
     * 
     * @throws Exception 
     */
    public void testFieldGroupSplitInTwo() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package test;\n" +
            "\n" +
            "class Source {\n" +
            "    void foo() {\n" +
            "        int a, b, c = 3 * 8, d, e, f;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = "package test;\n" +
            "\n" +
            "class Source {\n" +
            "    void foo() {\n" +
            "        int a, b, c;\n" +
            "        {\n" +
            "            c = 3 * 8;\n" +
            "        }\n" +
            "        int d, e, f;\n" +
            "        int eeee;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            @Override
            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(Phase.RESOLVED);
                ClassTree ct = (ClassTree)wc.getCompilationUnit().getTypeDecls().get(0);
                MethodTree mt = (MethodTree)ct.getMembers().get(1);
                List<? extends StatementTree> stmts = mt.getBody().getStatements();
                
                TreeMaker mk = wc.getTreeMaker();
                List<StatementTree> newStats = new ArrayList<>(4);
                VariableTree aDecl = (VariableTree)stmts.get(0);
                newStats.add(stmts.get(0)); // int a,
                newStats.add(stmts.get(1)); // int b,
                
                VariableTree origCDecl = (VariableTree)stmts.get(2);
                
                VariableTree cDecl = mk.Variable(aDecl.getModifiers(), origCDecl.getName(), aDecl.getType(), null);
                newStats.add(cDecl);
                // add a block to separate the variable groups
                newStats.add(mk.Block(Collections.<StatementTree>singletonList(
                        mk.ExpressionStatement(
                            mk.Assignment(mk.Identifier("c"), origCDecl.getInitializer()))), false));
                // rest of original field group
                newStats.add(stmts.get(3));
                newStats.add(stmts.get(4));
                newStats.add(stmts.get(5));
                newStats.add(mk.Variable(origCDecl.getModifiers(), "eeee", aDecl.getType(), null));
                
                wc.rewrite(mt.getBody(), mk.Block(newStats, false));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupFirstComment255568() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package test;\n" +
            "public enum Test {\n" +
            "    /**\n" +
            "     * Double\n"
            + "     */\n"
            + "    ONE,\n"
            + "    /**\n"
            + "     * Javadoc1\n"
            + "     */\n"
            + "    TWO,\n"
            + "    \n"
            + "    /**\n"
            + "     * Javadoc2\n"
            + "     */\n"
            + "    TRI"
            + "}\n"            
        );
        String golden = "package test;\n" +
            "public enum Test {\n" +
            "    /**\n" +
            "     * Double\n"
            + "     */\n"
            + "    ONE,\n"
            + "    /**\n"
            + "     * Javadoc1\n"
            + "     */\n"
            + "    Dva,\n"
            + "    \n"
            + "    /**\n"
            + "     * Javadoc2\n"
            + "     */\n"
            + "    TRI"
            + "}\n";

        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            @Override
            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(Phase.RESOLVED);
                ClassTree ct = (ClassTree)wc.getCompilationUnit().getTypeDecls().get(0);
                // 0 is the ctor
                VariableTree twoField = (VariableTree) ct.getMembers().get(2);
                wc.rewrite(twoField, 
                        wc.getTreeMaker().Variable(
                                twoField.getModifiers(), 
                                "Dva", 
                                twoField.getType(), 
                                twoField.getInitializer()
                        )
                );
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testMultiFileCopy234570() throws Exception {
        File source = new File(getWorkDir(), "Source.java");
        TestUtilities.copyStringToFile(source,
            "package test;\n" +
            "\n" +
            "class Source {\n" +
            "    int a,b;\n" +
            "}\n"
            );
        File target = new File(getWorkDir(), "Target.java");
        TestUtilities.copyStringToFile(target,
            "package test;\n" +
            "\n" +
            "class Target {\n" +
            "}\n"
            );
        String golden =
            "package test;\n" +
            "\n" +
            "class Target {\n" +
                //TODO: ideally should be:
//            "    int a,b;\n" +
            "    int a;\n" +
            "    int b;\n" +
            "}\n";
        JavaSource testSource = JavaSource.create(ClasspathInfo.create(FileUtil.toFileObject(source)), FileUtil.toFileObject(source), FileUtil.toFileObject(target));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                SourceUtils.forceSource(workingCopy, FileUtil.toFileObject(source));
                assertEquals(Phase.UP_TO_DATE, workingCopy.toPhase(Phase.UP_TO_DATE));
                if ("Target".equals(workingCopy.getFileObject().getName())) {
                    TypeElement source = workingCopy.getElements().getTypeElement("test.Source");
                    ClassTree sourceClass = workingCopy.getTrees().getTree(source);

                    assertNotNull(sourceClass);

                    TreeMaker make = workingCopy.getTreeMaker();
                    ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                    Tree var1 = sourceClass.getMembers().get(1);
                    Tree var2 = sourceClass.getMembers().get(2);

                    workingCopy.rewrite(clazz, make.addClassMember(make.addClassMember(clazz, var1), var2));
                }
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(target);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

}
