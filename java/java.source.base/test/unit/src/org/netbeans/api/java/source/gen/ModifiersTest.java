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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;

/**
 * Tests modifiers changes.
 * 
 * @author Pavel Flaska
 */
public class ModifiersTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of ModifiersTEst */
    public ModifiersTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(ModifiersTest.class);
//        suite.addTest(new ModifiersTest("testChangeToFinalLocVar"));
//        suite.addTest(new ModifiersTest("testAddClassAbstract"));
//        suite.addTest(new ModifiersTest("testMethodMods1"));
//        suite.addTest(new ModifiersTest("testMethodMods2"));
//        suite.addTest(new ModifiersTest("testMethodMods3"));
//        suite.addTest(new ModifiersTest("testMethodMods4"));
//        suite.addTest(new ModifiersTest("testMethodMods5"));
//        suite.addTest(new ModifiersTest("testMethodMods6"));
//        suite.addTest(new ModifiersTest("testMethodMods7"));
//        suite.addTest(new ModifiersTest("testAnnRename"));
//        suite.addTest(new ModifiersTest("testAddArrayValue"));
//        suite.addTest(new ModifiersTest("testRenameAnnotationAttribute"));
//        suite.addTest(new ModifiersTest("testMakeClassAbstract"));
//        suite.addTest(new ModifiersTest("test106543"));
//        suite.addTest(new ModifiersTest("test106403"));
//        suite.addTest(new ModifiersTest("test106403_2"));
//        suite.addTest(new ModifiersTest("testAddMethodAnnotation1"));
//        suite.addTest(new ModifiersTest("testAddMethodAnnotation2"));
//        suite.addTest(new ModifiersTest("testAddMethodAnnotation3"));
//        suite.addTest(new ModifiersTest("testChangeInterfaceModifier"));
//        suite.addTest(new ModifiersTest("testRemoveClassAnnotation"));
//        suite.addTest(new ModifiersTest("testRemoveClassAnnotationAttribute1"));
//        suite.addTest(new ModifiersTest("testRemoveClassAnnotationAttribute2"));
//        suite.addTest(new ModifiersTest("testRemoveClassAnnotationAttribute3"));
//        suite.addTest(new ModifiersTest("testRemoveClassAnnotationAttribute4"));
//        suite.addTest(new ModifiersTest("testRemoveClassAnnotationAttribute5"));
//        suite.addTest(new ModifiersTest("testAddAnnotationToMethodPar"));
//        suite.addTest(new ModifiersTest("test124701"));
//        suite.addTest(new ModifiersTest("testRemoveVariableModifier"));
//        suite.addTest(new ModifiersTest("testRewriteModifiers146517"));
        return suite;
    }

    /**
     * Tests the change of modifier in local variable
     */
    public void testChangeToFinalLocVar() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        int i = 10;\n" +
                "    }\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        final int i = 10;\n" +
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
                BlockTree block = method.getBody();
                VariableTree vt = (VariableTree) block.getStatements().get(0);
                ModifiersTree mods = vt.getModifiers();
                workingCopy.rewrite(mods, make.Modifiers(Collections.<Modifier>singleton(Modifier.FINAL)));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Update top-level class modifiers.
     */
    public void testAddClassAbstract() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    public abstract void taragui();\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public abstract class Test {\n" +
                "    public abstract void taragui();\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                Set<Modifier> s = EnumSet.of(Modifier.ABSTRACT);
                s.addAll(mods.getFlags());
                workingCopy.rewrite(mods, make.Modifiers(s));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Original:
     * 
     * void method() {
     * }
     * 
     * Result:
     * 
     * public static void method() {
     * }
     */
    public void testMethodMods1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    void method() {\n" +
                "    }\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    public static void method() {\n" +
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
                ModifiersTree mods = method.getModifiers();
                workingCopy.rewrite(mods, make.Modifiers(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC)));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Original:
     * 
     * public static void method() {
     * }
     * 
     * Result:
     * 
     * void method() {
     * }
     */
    public void testMethodMods2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    public static void method() {\n" +
                "    }\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    void method() {\n" +
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
                ModifiersTree mods = method.getModifiers();
                workingCopy.rewrite(mods, make.Modifiers(Collections.<Modifier>emptySet()));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Original:
     * 
     * Test() {
     * }
     * 
     * Result:
     * 
     * public Test() {
     * }
     */
    public void testMethodMods3() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    Test() {\n" +
                "    }\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    public Test() {\n" +
                "    }\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(0);
                ModifiersTree mods = method.getModifiers();
                workingCopy.rewrite(mods, make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Original:
     * 
     * public Test() {
     * }
     * 
     * Result:
     * 
     * Test() {
     * }
     */
    public void testMethodMods4() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    public Test() {\n" +
                "    }\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    Test() {\n" +
                "    }\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(0);
                ModifiersTree mods = method.getModifiers();
                workingCopy.rewrite(mods, make.Modifiers(Collections.<Modifier>emptySet()));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Original:
     * 
     * public static void method() {
     * }
     * 
     * Result:
     * 
     * static void method() {
     * }
     */
    public void testMethodMods5() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    public static void method() {\n" +
                "    }\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    static void method() {\n" +
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
                ModifiersTree mods = method.getModifiers();
                workingCopy.rewrite(mods, make.Modifiers(Collections.<Modifier>singleton(Modifier.STATIC)));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Original:
     * 
     * public Test() {
     * }
     * 
     * Result:
     * 
     * protected Test() {
     * }
     */
    public void testMethodMods6() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    public Test() {\n" +
                "    }\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n\n" +
                "import java.io.*;\n\n" +
                "public class Test {\n" +
                "    protected Test() {\n" +
                "    }\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(0);
                ModifiersTree mods = method.getModifiers();
                workingCopy.rewrite(mods, make.Modifiers(Collections.<Modifier>singleton(Modifier.PROTECTED)));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Original:
     * 
     * @Anotace()
     * public Test() {
     * }
     * 
     * Result:
     * 
     * @Annotaition()
     * protected Test() {
     * }
     */
    public void testAnnRename() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "@Annotace()\n" +
                "public class Test {\n" +
                "    public Test() {\n" +
                "    }\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "@Annotation()\n" +
                "public class Test {\n" +
                "    public Test() {\n" +
                "    }\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                AnnotationTree ann = mods.getAnnotations().get(0);
                IdentifierTree ident = (IdentifierTree) ann.getAnnotationType();
                workingCopy.rewrite(ident, make.Identifier("Annotation"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Original:
     * 
     * public class Test {
     * ...
     * 
     * Result:
     * 
     * @Annotation(value = { "Lojza", "Karel" })
     * public class Test {
     * ...
     * 
     */
    public void testAddArrayValue() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "public class Test {\n" +
                "    public Test() {\n" +
                "    }\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "@Annotation(value = {\"Lojza\", \"Karel\"})\n" +
                "public class Test {\n" +
                "    public Test() {\n" +
                "    }\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                List<LiteralTree> l = new ArrayList<LiteralTree>();
                l.add(make.Literal("Lojza"));
                l.add(make.Literal("Karel"));
                NewArrayTree nat = make.NewArray(null, Collections.<ExpressionTree>emptyList(), l);
                AssignmentTree at = make.Assignment(make.Identifier("value"), nat);
                AnnotationTree ann = make.Annotation(make.Identifier("Annotation"), Collections.<ExpressionTree>singletonList(at));
                workingCopy.rewrite(mods, make.Modifiers(mods.getFlags(), Collections.<AnnotationTree>singletonList(ann)));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /*
     * Test rename annotation attribute, regression test for #99162
     */
    public void testRenameAnnotationAttribute() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "/**\n" +
                " *aa\n" +
                " */\n" +
                "@Annotation(val = 2)\n" +
                "public class Test {\n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "/**\n" +
                " *aa\n" +
                " */\n" +
                "@Annotation(value = 2)\n" +
                "public class Test {\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                AnnotationTree annotationTree = mods.getAnnotations().get(0);
                AssignmentTree assignementTree = (AssignmentTree) annotationTree.getArguments().get(0);
                workingCopy.rewrite(assignementTree.getVariable(), make.Identifier("value"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #95354
    public void testMakeClassAbstract() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package org.netbeans.test.java.hints;\n" +
            "\n" +
            "@Test1 @Test2(test=\"uuu\") class MakeClassAbstract3 {\n" +
            "\n" +
            "    public MakeClassAbstract3() {\n" +
            "    }\n" +
        "\n" +
            "    public abstract void test();\n" +
            "\n" +
            "}\n" +
            "\n" +
            "@interface Test1 {}\n" +
            "\n" +
            "@interface Test2 {\n" +
            "    public String test();\n" +
            "}\n"
        );
        String golden =
            "package org.netbeans.test.java.hints;\n" +
            "\n" +
            "@Test1 @Test2(test=\"uuu\") abstract class MakeClassAbstract3 {\n" +
            "\n" +
            "    public MakeClassAbstract3() {\n" +
            "    }\n" +
            "\n" +
            "    public abstract void test();\n" +
            "\n" +
            "}\n" +
            "\n" +
            "@interface Test1 {}\n" +
            "\n" +
            "@interface Test2 {\n" +
            "    public String test();\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                Set<Modifier> flags = EnumSet.of(Modifier.ABSTRACT);
                flags.addAll(mods.getFlags());
                workingCopy.rewrite(mods, make.Modifiers(flags, mods.getAnnotations()));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #106543 - Positions broken when removing annotation attribute value.
    public void test106543() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "/**\n" +
                " *aa\n" +
                " */\n" +
                "@Annotation(val = 2)\n" +
                "public class Test {\n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "/**\n" +
                " *aa\n" +
                " */\n" +
                "@Annotation\n" +
                "public class Test {\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                AnnotationTree annotationTree = mods.getAnnotations().get(0);
                AnnotationTree copy = make.removeAnnotationAttrValue(annotationTree, annotationTree.getArguments().get(0));
                workingCopy.rewrite(annotationTree, copy);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #106403
    public void test106403() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "/**\n" +
                " *aa\n" +
                " */\n" +
                "@Annotation\n" +
                "public class Test {\n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "/**\n" +
                " *aa\n" +
                " */\n" +
                "@Annotation(val = 2)\n" +
                "public class Test {\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                AnnotationTree annotationTree = mods.getAnnotations().get(0);
                AnnotationTree modified = make.addAnnotationAttrValue(
                        annotationTree, 
                        make.Assignment(make.Identifier("val"), make.Literal(2))
                );
                workingCopy.rewrite(annotationTree, modified);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #106403 -2-
    public void test106403_2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "/**\n" +
                " *aa\n" +
                " */\n" +
                "@Annotation()\n" +
                "public class Test {\n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "/**\n" +
                " *aa\n" +
                " */\n" +
                "@Annotation(val = 2)\n" +
                "public class Test {\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                AnnotationTree annotationTree = mods.getAnnotations().get(0);
                AnnotationTree modified = make.addAnnotationAttrValue(
                        annotationTree, 
                        make.Assignment(make.Identifier("val"), make.Literal(2))
                );
                workingCopy.rewrite(annotationTree, modified);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #105018 - bad formatting when adding annotation
    public void testAddMethodAnnotation1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "public class Test {\n" +
                "    public void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "public class Test {\n" +
                "    @Annotation\n" +
                "    public void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = ((MethodTree) clazz.getMembers().get(1)).getModifiers();
                AnnotationTree annotationTree = make.Annotation(
                        make.Identifier("Annotation"),
                        Collections.<ExpressionTree>emptyList()
                );
                ModifiersTree modified = make.addModifiersAnnotation(
                        mods,
                        annotationTree
                );
                workingCopy.rewrite(mods, modified);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #105018 - bad formatting when adding annotation
    public void testAddMethodAnnotation2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "public class Test {\n" +
                "    void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "public class Test {\n" +
                "    @Annotation\n" +
                "    void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = ((MethodTree) clazz.getMembers().get(1)).getModifiers();
                AnnotationTree annotationTree = make.Annotation(
                        make.Identifier("Annotation"),
                        Collections.<ExpressionTree>emptyList()
                );
                ModifiersTree modified = make.addModifiersAnnotation(
                        mods,
                        annotationTree
                );
                workingCopy.rewrite(mods, modified);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #109671 - bad formatting when adding annotation to method with comment
    public void testAddMethodAnnotation3() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "public class Test {\n" +
                "    // line comment\n" +
                "    void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "public class Test {\n" +
                "    // line comment\n" +
                "    @Annotation\n" +
                "    void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = ((MethodTree) clazz.getMembers().get(1)).getModifiers();
                AnnotationTree annotationTree = make.Annotation(
                        make.Identifier("Annotation"),
                        Collections.<ExpressionTree>emptyList()
                );
                ModifiersTree modified = make.addModifiersAnnotation(
                        mods,
                        annotationTree
                );
                workingCopy.rewrite(mods, modified);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #106252 - interface keyword doubled
    public void testChangeInterfaceModifier() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "public interface Test {\n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "interface Test {\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                Set<Modifier> flags = EnumSet.noneOf(Modifier.class);
                flags.addAll(mods.getFlags());
                flags.remove(Modifier.PUBLIC);
                ModifiersTree modified = make.Modifiers(flags);
                
                ClassTree copy = make.Interface(
                        modified,
                        clazz.getSimpleName(),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<Tree>emptyList(),
                        clazz.getMembers()
                );
                workingCopy.rewrite(clazz, copy);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveClassAnnotation() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "@Annotation\n" +
                "public class Test {\n" +
                "    void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "public class Test {\n" +
                "    void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                ModifiersTree modified = make.removeModifiersAnnotation(mods, 0);
                workingCopy.rewrite(mods, modified);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveClassAnnotationAttribute1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "@Annotation(attr1 = \"aa\", attr2 = \"bb\")\n" +
                "public class Test {\n" +
                "    void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "@Annotation(attr1 = \"aa\")\n" +
                "public class Test {\n" +
                "    void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                AnnotationTree ann = mods.getAnnotations().get(0);
                AnnotationTree modified = make.removeAnnotationAttrValue(ann, 1);
                workingCopy.rewrite(ann, modified);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveClassAnnotationAttribute2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "@Annotation(attr1 = \"aa\", attr2 = \"bb\")\n" +
                "public class Test {\n" +
                "    void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "@Annotation(attr2 = \"bb\")\n" +
                "public class Test {\n" +
                "    void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                AnnotationTree ann = mods.getAnnotations().get(0);
                AnnotationTree modified = make.removeAnnotationAttrValue(ann, 0);
                workingCopy.rewrite(ann, modified);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveClassAnnotationAttribute3() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "@Annotation(attr1 = \"aa\", attr2 = \"bb\", attr3 = \"cc\")\n" +
                "public class Test {\n" +
                "    void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "@Annotation(attr1 = \"aa\", attr3 = \"cc\")\n" +
                "public class Test {\n" +
                "    void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                AnnotationTree ann = mods.getAnnotations().get(0);
                AnnotationTree modified = make.removeAnnotationAttrValue(ann, 1);
                workingCopy.rewrite(ann, modified);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveClassAnnotationAttribute4() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "@Annotation(attr1 = \"aa\")\n" +
                "public class Test {\n" +
                "    void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "@Annotation\n" +
                "public class Test {\n" +
                "    void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                AnnotationTree ann = mods.getAnnotations().get(0);
                AnnotationTree modified = make.removeAnnotationAttrValue(ann, 0);
                workingCopy.rewrite(ann, modified);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveClassAnnotationAttribute5() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "@Annotation(attr1 = \"aa\", attr2 = \"bb\", attr3 = \"cc\")\n" +
                "public class Test {\n" +
                "    void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "@Annotation\n" +
                "public class Test {\n" +
                "    void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                AnnotationTree ann = mods.getAnnotations().get(0);
                AnnotationTree modified = make.removeAnnotationAttrValue(ann, 2);
                modified = make.removeAnnotationAttrValue(modified, 1);
                modified = make.removeAnnotationAttrValue(modified, 0);
                workingCopy.rewrite(ann, modified);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddAnnotationToMethodPar() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "public class Test {\n" +
                "    void alois(String a, String b) {\n" +
                "    }\n" +
                "    \n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "public class Test {\n" +
                "    void alois(String a, @Annotation String b) {\n" +
                "    }\n" +
                "    \n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ModifiersTree mods = method.getParameters().get(1).getModifiers();
                ModifiersTree copy = make.addModifiersAnnotation(
                        mods,
                        make.Annotation(
                                make.Identifier("Annotation"),
                                Collections.<ExpressionTree>emptyList()
                        )
                );
                workingCopy.rewrite(mods, copy);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test124701() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "public class Test {\n" +
                "    @SuppressWarnings(\"x\")\n" +
                "    private void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "public class Test {\n" +
                "    @SuppressWarnings(\"x\")\n" +
                "    public void alois() {\n" +
                "    }\n" +
                "    \n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ModifiersTree mods = method.getModifiers();
                ModifiersTree copy = make.Modifiers(EnumSet.of(Modifier.PUBLIC), mods.getAnnotations());
                workingCopy.rewrite(mods, copy);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveVariableModifier() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "public class Test {\n" +
                "    private int a;\n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "public class Test {\n" +
                "    int a;\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                ModifiersTree mods = var.getModifiers();
                ModifiersTree copy = make.Modifiers(EnumSet.noneOf(Modifier.class), mods.getAnnotations());
                workingCopy.rewrite(mods, copy);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRewriteModifiers146517() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "public class Test {\n" +
                "    private void test() {\n" +
                "        try {\n" +
                "        } catch (Exception e) {\n" +
                "        }\n" +
                "    }\n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "public class Test {\n" +
                "    private void test() {\n" +
                "        try {\n" +
                "        } catch (Exception e) {\n" +
                "        }\n" +
                "    }\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                new ErrorAwareTreeScanner() {
                    @Override
                    public Object visitVariable(VariableTree var, Object p) {
                        ModifiersTree mods = var.getModifiers();
                        ModifiersTree copy = make.Modifiers(EnumSet.noneOf(Modifier.class), mods.getAnnotations());
                        workingCopy.rewrite(mods, copy);
                        return null;
                    }
                }.scan(clazz, null);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddRemoveDefaultKeyword() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "public interface Test {\n" +
                "    public default void remove() { }\n" +
                "    public void add();\n" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "public interface Test {\n" +
                "    public void remove();\n" +
                "    public default void add() {\n" +
                "    }\n" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                new ErrorAwareTreeScanner() {
                    @Override public Object visitMethod(MethodTree node, Object p) {
                        if (node.getName().contentEquals("add")) {
                            workingCopy.rewrite(node.getModifiers(), make.addModifiersModifier(node.getModifiers(), Modifier.DEFAULT));
                            workingCopy.rewrite(node, make.Method(node.getModifiers(), node.getName(), node.getReturnType(), node.getTypeParameters(), node.getParameters(), node.getThrows(), "{}", null));
                        } else {
                            workingCopy.rewrite(node.getModifiers(), make.removeModifiersModifier(node.getModifiers(), Modifier.DEFAULT));
                            workingCopy.rewrite(node, make.Method(node.getModifiers(), node.getName(), node.getReturnType(), node.getTypeParameters(), node.getParameters(), node.getThrows(), (BlockTree) null, null));
                        }
                        return super.visitMethod(node, p);
                    }
                }.scan(clazz, null);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveModifiersWithTypeParameters() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package flaska;\n" +
                "\n" +
                "public class Test {\n" +
                "    public <T extends java.lang.Number> java.util.Set<T> find(){return null;}" +
                "}\n"
                );
        String golden =
                "package flaska;\n" +
                "\n" +
                "public class Test {\n" +
                "    <T extends java.lang.Number> java.util.Set<T> find(){return null;}" +
                "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                new ErrorAwareTreeScanner() {
                    @Override public Object visitMethod(MethodTree node, Object p) {
                        if (node.getName().contentEquals("find")) {
                            workingCopy.rewrite(node.getModifiers(), make.Modifiers(Collections.emptySet()));
                        }
                        return super.visitMethod(node, p);
                    }
                }.scan(clazz, null);
            }
        };
        testSource.runModificationTask(task).commit();
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
