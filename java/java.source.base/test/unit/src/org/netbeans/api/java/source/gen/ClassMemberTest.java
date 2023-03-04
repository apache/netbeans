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
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import org.netbeans.api.java.source.*;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Pavel Flaska
 */
public class ClassMemberTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of ClassMemberTest */
    public ClassMemberTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(ClassMemberTest.class);
//        suite.addTest(new ClassMemberTest("testAddAtIndex0"));
//        suite.addTest(new ClassMemberTest("testAddAtIndex2"));
//        suite.addTest(new ClassMemberTest("testAddToEmpty"));
//        suite.addTest(new ClassMemberTest("testAddConstructor"));
//        suite.addTest(new ClassMemberTest("testInsertFieldToIndex0"));
//        suite.addTest(new ClassMemberTest("testModifyFieldName"));
//        suite.addTest(new ClassMemberTest("testModifyModifiers"));
//        suite.addTest(new ClassMemberTest("testAddToEmptyInterface"));
//        suite.addTest(new ClassMemberTest("testAddNewClassWithNewMembers"));
//        suite.addTest(new ClassMemberTest("testAddInnerInterface"));
//        suite.addTest(new ClassMemberTest("testAddInnerAnnotationType"));
//        suite.addTest(new ClassMemberTest("testAddInnerEnum"));
//        suite.addTest(new ClassMemberTest("testAddMethodAndModifyConstr"));
//        suite.addTest(new ClassMemberTest("testAddAfterEmptyInit1"));
//        suite.addTest(new ClassMemberTest("testAddAfterEmptyInit2"));
//        suite.addTest(new ClassMemberTest("testMemberIndent93735_1"));
//        suite.addTest(new ClassMemberTest("testMemberIndent93735_2"));
//        suite.addTest(new ClassMemberTest("testAddArrayMember"));
//        suite.addTest(new ClassMemberTest("testAddCharMember"));
//        suite.addTest(new ClassMemberTest("testRenameReturnTypeInAbstract"));
//        suite.addTest(new ClassMemberTest("testAddInitToVar"));
//        suite.addTest(new ClassMemberTest("testAddMethodWithDoc"));
//        suite.addTest(new ClassMemberTest("testAddFieldWithDoc1"));
//        suite.addTest(new ClassMemberTest("testAddFieldWithDoc2"));
//        suite.addTest(new ClassMemberTest("test111024"));
//        suite.addTest(new ClassMemberTest("test196053a"));
//        suite.addTest(new ClassMemberTest("test196053b"));
//        suite.addTest(new ClassMemberTest("testShuffleConstructorMethod1"));
//        suite.addTest(new ClassMemberTest("testShuffleConstructorMethod2"));
        return suite;
    }
    
    public void testRemoveAll() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "    \n"
                + "    // fields\n"
                + "    Integer a;\n"
                + "    /* Comment */\n"
                + "    Integer b;\n"
                + "    /**\n"
                + "     * Comment 3\n"
                + "     */\n"
                + "    String s;\n"
                + "    // Comment 2\n"
                + "    Boolean t;\n"
                + "\n"
                + "    // Comment doSomething(int x)\n"
                + "    public void doSomething(int x) {\n"
                + "        // Do Something\n"
                + "    }\n"
                + "\n"
                + "    // Comment doSomethingElse()\n"
                + "    public String doSomethingElse() {\n"
                + "        return \"hello world\";\n"
                + "    }\n"
                + "\n"
                + "    /**\n"
                + "     *\n"
                + "     * Comment doStuff(String s)\n"
                + "     */\n"
                + "    public void doStuff(String s) {\n"
                + "        System.out.println(\"do stuff\");\n"
                + "        System.out.println(new EventListener() {\n"
                + "        });\n"
                + "    }\n"
                + "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
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
                        ClassTree classTree = (ClassTree) typeDecl;
                        ClassTree nju = classTree;
                        for (Tree tree : classTree.getMembers()) {
                            nju = make.removeClassMember(nju, tree);
                        }
                        workingCopy.rewrite(classTree, nju);
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddAtIndex0() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    \n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" + 
            "    }\n" +
            "    \n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "    \n" +
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
                        ClassTree classTree = (ClassTree) typeDecl;
                        ClassTree copy = make.insertClassMember(classTree, 0, m(make));
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddAtIndex2() throws Exception {
        //member position 2 is actually after the taragui method, as position 0 is the syntetic constructor:
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "\n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" +
            "    }\n" +
            "    \n" +
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
                        ClassTree classTree = (ClassTree) typeDecl;
                        ClassTree copy = make.insertClassMember(classTree, 2, m(make));
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddToEmpty() throws Exception {
        //member position 2 is actually after the taragui method, as position 0 is the syntetic constructor:
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" +
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
                        ClassTree classTree = (ClassTree) typeDecl;
                        ClassTree copy = make.addClassMember(classTree, m(make));
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddConstructor() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    String prefix;\n" +
            "    \n" +
            "    public void method() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    String prefix;\n" +
            "\n" +
            "    public Test(boolean prefix) {\n" +
            "    }\n" +
            "    \n" +
            "    public void method() {\n" +
            "    }\n" +
            "    \n" +
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
                        ClassTree classTree = (ClassTree) typeDecl;
                        ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
                        List<VariableTree> arguments = new ArrayList<VariableTree>();
                        arguments.add(make.Variable(
                            make.Modifiers(EnumSet.noneOf(Modifier.class)),
                            "prefix",
                            make.PrimitiveType(TypeKind.BOOLEAN), null)
                        );
                        MethodTree constructor = make.Method(
                            mods,
                            "<init>",
                            null,
                            Collections.<TypeParameterTree> emptyList(),
                            arguments,
                            Collections.<ExpressionTree>emptyList(),
                            make.Block(Collections.<StatementTree>emptyList(), false),
                            null
                        );
                        ClassTree copy = make.insertClassMember(classTree, 2, constructor);
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testInsertFieldToIndex0() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    int i = 0;\n" +
            "    \n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "\n" +
            "    String prefix;\n" +
            "    \n" +
            "    int i = 0;\n" +
            "    \n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        VariableTree member = make.Variable(
                                make.Modifiers(Collections.<Modifier>emptySet()),
                                "prefix",
                                make.Identifier("String"),
                                null
                            );
                        ClassTree modifiedClazz = make.insertClassMember(clazz, 0, member);
                        workingCopy.rewrite(clazz,modifiedClazz);
                    }
                }
            }
        };
        src.runModificationTask(task).commit();    
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testModifyFieldName() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    int i = 0;\n" +
            "    \n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    int newFieldName = 0;\n" +
            "    \n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        VariableTree variable = (VariableTree) ((ClassTree) typeDecl).getMembers().get(0);
                        VariableTree copy = make.setLabel(variable, "newFieldName");
                        workingCopy.rewrite(variable, copy);
                    }
                }
            }
        };
        src.runModificationTask(task).commit();    
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testModifyModifiers() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    private int i = 0;\n" +
            "    \n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    public int i = 0;\n" +
            "    \n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        VariableTree variable = (VariableTree) ((ClassTree) typeDecl).getMembers().get(0);
                        ModifiersTree mods = variable.getModifiers();
                        workingCopy.rewrite(mods, make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)));
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();    
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddToEmptyInterface() throws Exception {
        //member position 2 is actually after the taragui method, as position 0 is the syntetic constructor:
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public interface Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public interface Test {\n\n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException;\n" +
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
                        ClassTree classTree = (ClassTree) typeDecl;
                        MethodTree method = m(make);
                        MethodTree methodC = make.Method(method.getModifiers(),
                                method.getName(),
                                method.getReturnType(),
                                method.getTypeParameters(),
                                method.getParameters(),
                                method.getThrows(),
                                (BlockTree) null,
                                null
                        );
                        ClassTree copy = make.addClassMember(classTree, methodC);
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddNewClassWithNewMembers() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n\n" +
                "    public class X {\n\n" +
                "        private int i;\n\n" +
                "        public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" +
                "        }\n" +
                "    }\n" +
                "}\n";
        
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                
                ClassTree ct = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = m(make);
                MethodTree methodC = make.Method(method.getModifiers(),
                        method.getName(),
                        method.getReturnType(),
                        method.getTypeParameters(),
                        method.getParameters(),
                        method.getThrows(),
                        "{}",
                        null
                        );
                VariableTree var = make.Variable(make.Modifiers(EnumSet.of(Modifier.PRIVATE)), "i", make.Type(workingCopy.getTypes().getPrimitiveType(TypeKind.INT)), null);
                ClassTree nueClass = make.Class(make.Modifiers(EnumSet.of(Modifier.PUBLIC)), "X", Collections.<TypeParameterTree>emptyList(), null, Collections.<ExpressionTree>emptyList(), Arrays.asList(var, methodC));
                ClassTree copy = make.addClassMember(ct, nueClass);
                workingCopy.rewrite(ct, copy);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    private MethodTree m(TreeMaker make) {
        // create method modifiers
        ModifiersTree parMods = make.Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
        // create parameters
        VariableTree par1 = make.Variable(parMods, "a", make.PrimitiveType(TypeKind.INT), null);
        VariableTree par2 = make.Variable(parMods, "b", make.PrimitiveType(TypeKind.FLOAT), null);
        List<VariableTree> parList = new ArrayList<VariableTree>(2);
        parList.add(par1);
        parList.add(par2);
        // create method
        MethodTree newMethod = make.Method(
            make.Modifiers( 
                Collections.singleton(Modifier.PUBLIC), // modifiers
                Collections.<AnnotationTree>emptyList() // annotations
            ), // modifiers and annotations
            "newlyCreatedMethod", // name
            make.PrimitiveType(TypeKind.VOID), // return type
            Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
            parList, // parameters
            Collections.singletonList(make.Identifier("java.io.IOException")), // throws 
            make.Block(Collections.<StatementTree>emptyList(), false), // empty statement block
            null // default value - not applicable here, used by annotations
        );
        return newMethod;
    }
    
    /**
     * #92726, #92127: When semicolon is in class declaration, it is represented
     * as an empty initializer in the tree with position -1. This causes many
     * problems during generating. See issues for details.
     */
    public void testAddAfterEmptyInit1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    static enum Enumerace {\n" +
            "        A, B\n" +
            "    };\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    static enum Enumerace {\n" +
            "        A, B\n" +
            "    };\n" +
            "\n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" +
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
                        ClassTree classTree = (ClassTree) typeDecl;
                        ClassTree copy = make.addClassMember(classTree, m(make));
                        workingCopy.rewrite(classTree, copy);
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
     * #92726, #92127: When semicolon is in class declaration, it is represented
     * as an empty initializer in the tree with position -1. This causes many
     * problems during generating. See issues for details.
     */
    public void DISABLED190952testAddAfterEmptyInit2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    ;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    ;\n" +
            "\n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" +
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
                        ClassTree classTree = (ClassTree) typeDecl;
                        ClassTree copy = make.addClassMember(classTree, m(make));
                        workingCopy.rewrite(classTree, copy);
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
     * #96070
     */
    public void testAddInnerInterface() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    interface Honza {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree topLevel = (ClassTree) cut.getTypeDecls().get(0);
                ClassTree innerIntfc = make.Interface(make.Modifiers(
                        Collections.<Modifier>emptySet()),
                        "Honza",
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                workingCopy.rewrite(topLevel, make.addClassMember(topLevel, innerIntfc));
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #96070
     */
    public void testAddInnerAnnotationType() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public @interface Honza {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree topLevel = (ClassTree) cut.getTypeDecls().get(0);
                ClassTree innerIntfc = make.AnnotationType(make.Modifiers(
                        Collections.<Modifier>singleton(Modifier.PUBLIC)),
                        "Honza",
                        Collections.<Tree>emptyList()
                );
                workingCopy.rewrite(topLevel, make.addClassMember(topLevel, innerIntfc));
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #96070
     */
    public void testAddInnerEnum() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    protected enum Honza {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree topLevel = (ClassTree) cut.getTypeDecls().get(0);
                ClassTree innerIntfc = make.Enum(make.Modifiers(
                        Collections.<Modifier>singleton(Modifier.PROTECTED)),
                        "Honza",
                        Collections.<ExpressionTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                workingCopy.rewrite(topLevel, make.addClassMember(topLevel, innerIntfc));
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * 
     */
    public void DISABLEtestAddMethodAndModifyConstr() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    \n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    \n" +
            "    public Test() {\n" +
            "        // TODO: Make it!\n" +
            "        int i = 0;\n" +
            "    }\n" +
            "\n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree constr = (MethodTree) classTree.getMembers().get(0);
//                ModifiersTree parMods = make.Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
                // create parameters
//                VariableTree par1 = make.Variable(parMods, "a", make.PrimitiveType(TypeKind.INT), null);
//                VariableTree par2 = make.Variable(parMods, "b", make.PrimitiveType(TypeKind.INT), null);
//                MethodTree constrCopy = make.addMethodParameter(constr, par1);
//                constrCopy = make.addMethodParameter(constrCopy, par2);
                BlockTree newBody = make.createMethodBody(constr, "{\n // TODO: Make it!\nint i = 0; }");
                workingCopy.rewrite(constr.getBody(), newBody);
                ClassTree copy = make.insertClassMember(classTree, 1, m(make));
                workingCopy.rewrite(classTree, copy);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testMemberIndent93735_1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    // what a strange thing is this?\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" + 
            "    }\n" +
            "    // what a strange thing is this?\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "    \n" +
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
                        ClassTree classTree = (ClassTree) typeDecl;
                        ClassTree copy = make.insertClassMember(classTree, 0, m(make));
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testMemberIndent93735_2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int i = 0;\n" +
            "    // what a strange thing is this?\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int i = 0;\n" +
            "\n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" + 
            "    }\n" +
            "    // what a strange thing is this?\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "    \n" +
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
                        ClassTree classTree = (ClassTree) typeDecl;
                        ClassTree copy = make.insertClassMember(classTree, 2, m(make));
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddArrayMember() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    \n" +
            "    public java.util.List[] taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    public List<E>[] newlyCreatedMethod() {\n" + 
            "    }\n" +
            "    \n" +
            "    public java.util.List[] taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree classTree = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                TypeMirror mirror = workingCopy.getElements().getTypeElement("java.util.List").asType();
                mirror = workingCopy.getTypes().getArrayType(mirror);
                MethodTree njuMethod = (MethodTree) classTree.getMembers().get(1);
                njuMethod = make.Method(
                        njuMethod.getModifiers(),
                        "newlyCreatedMethod",
                        make.Type(mirror),
                        njuMethod.getTypeParameters(), 
                        njuMethod.getParameters(),
                        njuMethod.getThrows(),
                        njuMethod.getBody(),
                        (ExpressionTree) njuMethod.getDefaultValue()
                );
                ClassTree copy = make.insertClassMember(classTree, 0, njuMethod);
                workingCopy.rewrite(classTree, copy);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddCharMember() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    \n" +
            "    public char taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    public char newlyCreatedMethod() {\n" +
            "        char returnValue = taragui();\n" +
            "        char expectedValue = 'c';\n" + 
            "    }\n" +
            "    \n" +
            "    public char taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree classTree = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                TypeElement neterk = workingCopy.getElements().getTypeElement("hierbas.del.litoral.Test");
                List<? extends Element> members = workingCopy.getElements().getAllMembers(neterk);
                ExecutableElement konecny = null;
                for (Element e : members) {
                    if ("taragui".contentEquals(e.getSimpleName())) {
                        konecny = (ExecutableElement) e;
                        break;
                    }
                }
                List<ExpressionTree> empatik = Collections.<ExpressionTree>emptyList();
                MethodInvocationTree mit = make.MethodInvocation(empatik, make.Identifier("taragui"), empatik);
                VariableTree stmt = make.Variable(
                        make.Modifiers(Collections.<Modifier>emptySet()),
                        "returnValue",
                        make.Type(konecny.getReturnType()),
                        mit);
                BlockTree block = make.Block(Collections.<StatementTree>singletonList(stmt), false);
                stmt = make.Variable(
                        make.Modifiers(Collections.<Modifier>emptySet()),
                        "expectedValue",
                        make.Type(konecny.getReturnType()),
                        make.Literal(('c')));
                block = make.addBlockStatement(block, stmt);
                MethodTree njuMethod = (MethodTree) classTree.getMembers().get(1);
                njuMethod = make.Method(
                        njuMethod.getModifiers(),
                        "newlyCreatedMethod",
                        make.Type(konecny.getReturnType()),
                        njuMethod.getTypeParameters(), 
                        njuMethod.getParameters(),
                        njuMethod.getThrows(),
                        block,
                        (ExpressionTree) njuMethod.getDefaultValue()
                );
                ClassTree copy = make.insertClassMember(classTree, 0, njuMethod);
                workingCopy.rewrite(classTree, copy);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRenameReturnTypeInAbstract() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public abstract class Test {\n" +
            "    \n" +
            "    public Object taragui();\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public abstract class Test {\n" +
            "    \n" +
            "    public String taragui();\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree classTree = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) classTree.getMembers().get(1);
                workingCopy.rewrite(method.getReturnType(), make.Identifier("String"));
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddFirstFeatureField() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    boolean prefix;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree classTree = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree vt = make.Variable(
                    make.Modifiers(EnumSet.noneOf(Modifier.class)),
                    "prefix",
                    make.PrimitiveType(TypeKind.BOOLEAN), 
                    null
                );
                ClassTree copy = make.addClassMember(classTree, vt);
                workingCopy.rewrite(classTree, copy);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #109489
    public void testAddInitToVar() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    boolean prefix;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    boolean prefix = true;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree classTree = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree vetecko = (VariableTree) classTree.getMembers().get(1);
                VariableTree copy = make.Variable(
                        vetecko.getModifiers(),
                        vetecko.getName(),
                        vetecko.getType(),
                        make.Literal(Boolean.TRUE)
                );
                workingCopy.rewrite(vetecko, copy);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    // #110211
    public void testAddMethodWithDoc() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    boolean prefix;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    boolean prefix;\n" +
            "\n" +
            "    /**\n" +
            "     * Test comment\n" +
            "     */\n" +
            "    public void foo() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED); // is it neccessary?
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree m = make.Method(
                        make.Modifiers(java.lang.reflect.Modifier.PUBLIC, Collections.<AnnotationTree>emptyList()),
                        "foo",
                        make.PrimitiveType(TypeKind.VOID),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        make.Block(Collections.<StatementTree>emptyList(), false),
                        null
                );
                // Insert the class before constructor
                ClassTree modifiedClazz = make.addClassMember(clazz, m);
                Comment comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, "Test comment");
                make.addComment(m, comment, true);
                workingCopy.rewrite(clazz, modifiedClazz);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #110211
    public void testAddFieldWithDoc1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * Test comment\n" +
            "     */\n" +
            "    String prefix;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED); // is it neccessary?
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree member = make.Variable(
                        make.Modifiers(Collections.<Modifier>emptySet()),
                        "prefix",
                        make.Identifier("String"),
                        null
                    );
                // Insert the class before constructor
                ClassTree modifiedClazz = make.addClassMember(clazz, member);
                Comment comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, "Test comment");
                make.addComment(member, comment, true);
                workingCopy.rewrite(clazz, modifiedClazz);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #110211
    public void testAddFieldWithDoc2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * Test comment\n" +
            "     */\n" +
            "    public String prefix;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED); // is it neccessary?
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree member = make.Variable(
                        make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)),
                        "prefix",
                        make.Identifier("String"),
                        null
                    );
                // Insert the class before constructor
                ClassTree modifiedClazz = make.addClassMember(clazz, member);
                Comment comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, "Test comment");
                make.addComment(member, comment, true);
                workingCopy.rewrite(clazz, modifiedClazz);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test111024() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
             "/*\n" +
            " * TestClass.java\n" +
            " *\n" +
            " * Created on January 31, 2007, 11:23 AM\n" +
            " *\n" +
            " * To change this template, choose Tools | Template Manager\n" +
            " * and open the template in the editor.\n" +
            " */\n" +
            "\n" +
            "package org.netbeans.retouchebugs;\n" +
            "\n" +
            "/**\n" +
            " *\n" +
            " * @author jdeva\n" +
            " */\n" +
            "public class TestClass {\n" +
            "    // <editor-fold desc=\"VWP managed Component Definition\">\n" +
            "    private String foox = new String(\"Test\");\n" +
            "    String getFoo() {\n" +
            "        return this.foox;\n" +
            "    }\n" +
            "    \n" +
            "    void setFoo(String foox) {\n" +
            "        this.foox = foox;\n" +
            "    }\n" +
            "    \n" +
            "    private String foo = new String();\n" +
            "    //</editor-fold>\n" +
            "    \n" +
            "    /** Creates a new instance of TestClass */\n" +
            "    public TestClass() {\n" +
            "        getFoo().toCharArray();\n" +
            "        Thread.currentThread();\n" +
            "        System.out.println(\"Message\");\n" +
            "    }\n" +
            "    \n" +
            "    public void replace() {\n" +
            "        ;\n" +
            "        for(int i = 0; i < 10; i++) {\n" +
            "            System.out.println(\"In loop\");\n" +
            "        }\n" +
            "        Thread.currentThread();\n" +
            "        this.setFoo(\"Hello\");\n" +
            "        \n" +
            "        TestClass testClass = new TestClass();\n" +
            "        testClass.setFoo(\"World!\");\n" +
            "        testClass.getFoo().toString().toString();\n" +
            "    }\n" +
            " }\n");
        String golden =
             "/*\n" +
            " * TestClass.java\n" +
            " *\n" +
            " * Created on January 31, 2007, 11:23 AM\n" +
            " *\n" +
            " * To change this template, choose Tools | Template Manager\n" +
            " * and open the template in the editor.\n" +
            " */\n" +
            "\n" +
            "package org.netbeans.retouchebugs;\n" +
            "\n" +
            "import java.awt.List;\n" +
            "\n" +
            "/**\n" +
            " *\n" +
            " * @author jdeva\n" +
            " */\n" +
            "public class TestClass {\n" +
            "    // <editor-fold desc=\"VWP managed Component Definition\">\n" +
            "    private String foox = new String(\"Test\");\n" +
            "    String getFoo() {\n" +
            "        return this.foox;\n" +
            "    }\n" +
            "    \n" +
            "    void setFoo(String foox) {\n" +
            "        this.foox = foox;\n" +
            "    }\n" +
            "    \n" +
            "    private String foo = new String();\n" +
            "    //</editor-fold>\n" +
            "    \n" +
            "    /** Creates a new instance of TestClass */\n" +
            "    public TestClass() {\n" +
            "        getFoo().toCharArray();\n" +
            "        Thread.currentThread();\n" +
            "        System.out.println(\"Message\");\n" +
            "    }\n" +
            "    \n" +
            "    public void replace() {\n" +
            "        ;\n" +
            "        for(int i = 0; i < 10; i++) {\n" +
            "            System.out.println(\"In loop\");\n" +
            "        }\n" +
            "        Thread.currentThread();\n" +
            "        this.setFoo(\"Hello\");\n" +
            "        \n" +
            "        TestClass testClass = new TestClass();\n" +
            "        testClass.setFoo(\"World!\");\n" +
            "        testClass.getFoo().toString().toString();\n" +
            "    }\n" +
            "    public List foo = new List();\n" +
            "    public java.util.List foo = new java.util.List();\n" +
            " }\n";
        JavaSource testSource = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED); // is it neccessary?
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = null;
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        clazz = (ClassTree) typeDecl;
                        break;
                    }
                }

                TypeElement typeElement = workingCopy.getElements().getTypeElement("java.awt.List");
                Tree typeTree = make.QualIdent(typeElement);
                ExpressionTree initializer = make.NewClass(
                        null,
                        Collections.<ExpressionTree>emptyList(),
                        (ExpressionTree) typeTree,
                        Collections.<ExpressionTree>emptyList(), null
                );
                VariableTree vtree = make.Variable(
                    make.Modifiers(
                        java.lang.reflect.Modifier.PUBLIC,
                        Collections.<AnnotationTree>emptyList()
                    ),
                    "foo",
                    typeTree,
                    initializer
                );
                ClassTree newClazz = make.addClassMember(clazz, vtree);
                workingCopy.rewrite(clazz, newClazz);
            }
        };
        testSource.runModificationTask(task).commit();
        task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED); // is it neccessary?
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = null;
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        clazz = (ClassTree) typeDecl;
                        break;
                    }
                }

                TypeElement typeElement = workingCopy.getElements().getTypeElement("java.util.List");
                Tree typeTree = make.QualIdent(typeElement);
                ExpressionTree initializer = make.NewClass(
                        null,
                        Collections.<ExpressionTree>emptyList(),
                        (ExpressionTree) typeTree,
                        Collections.<ExpressionTree>emptyList(), null
                );
                VariableTree vtree = make.Variable(
                    make.Modifiers(
                        java.lang.reflect.Modifier.PUBLIC,
                        Collections.<AnnotationTree>emptyList()
                    ),
                    "foo",
                    typeTree,
                    initializer
                );
                ClassTree newClazz = make.addClassMember(clazz, vtree);
                workingCopy.rewrite(clazz, newClazz);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test196053a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    private @Deprecated java.util.List<? super String> b;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public java.util.List<? super String> b;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED); // is it neccessary?
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree var = (VariableTree) clazz.getMembers().get(1);

                workingCopy.rewrite(var.getModifiers(), make.Modifiers(EnumSet.of(Modifier.PUBLIC)));
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test196053b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    @Deprecated private java.util.List<? super String> b;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public java.util.List<? super String> c;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED); // is it neccessary?
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree var = (VariableTree) clazz.getMembers().get(1);

                workingCopy.rewrite(var.getModifiers(), make.Modifiers(EnumSet.of(Modifier.PUBLIC)));
                workingCopy.rewrite(var, make.setLabel(var, "c"));
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * In a special case when the whole annotation section is alone on the line the line SHOULD be removed
     * unlike test196053b. See defect #240072
     */
    public void test196053bWith240072() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    @Deprecated\n" + 
            "    private java.util.List<? super String> b;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public java.util.List<? super String> c;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED); // is it neccessary?
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree var = (VariableTree) clazz.getMembers().get(1);

                workingCopy.rewrite(var.getModifiers(), make.Modifiers(EnumSet.of(Modifier.PUBLIC)));
                workingCopy.rewrite(var, make.setLabel(var, "c"));
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testShuffleConstructorMethod1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "    }\n" +
            "    public static void m(String[] args) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public static void m(String[] args) {\n" +
            "    }\n" +
            "    public Test() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED); // is it neccessary?
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree m0 = (MethodTree) clazz.getMembers().get(0);
                m0 = make.setLabel(m0, m0.getName());
                MethodTree m1 = (MethodTree) clazz.getMembers().get(1);
                m1 = make.setLabel(m1, m1.getName());
                clazz = make.removeClassMember(clazz, 0);
                clazz = make.removeClassMember(clazz, 0);
                clazz = make.addClassMember(clazz, m1);
                clazz = make.addClassMember(clazz, m0);
                workingCopy.rewrite(cut.getTypeDecls().get(0), clazz);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testShuffleConstructorMethod2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "    }\n" +
            "    public static void m(String[] args) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class NewName {\n" +
            "    public static void m(String[] args) {\n" +
            "    }\n" +
            "    public NewName() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED); // is it neccessary?
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree m0 = (MethodTree) clazz.getMembers().get(0);
                m0 = make.setLabel(m0, m0.getName());
                MethodTree m1 = (MethodTree) clazz.getMembers().get(1);
                m1 = make.setLabel(m1, m1.getName());
                clazz = make.removeClassMember(clazz, 0);
                clazz = make.removeClassMember(clazz, 0);
                clazz = make.addClassMember(clazz, m1);
                clazz = make.addClassMember(clazz, m0);
                clazz = make.setLabel(clazz, "NewName");
                workingCopy.rewrite(cut.getTypeDecls().get(0), clazz);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddConstructorParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test(int i) {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED); // is it neccessary?
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree constr = (MethodTree) clazz.getMembers().get(0);
                VariableTree param = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "i", make.PrimitiveType(TypeKind.INT), null);
                MethodTree nueConstr = make.addMethodParameter(constr, param);
                workingCopy.rewrite(constr, nueConstr);
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
    
}
