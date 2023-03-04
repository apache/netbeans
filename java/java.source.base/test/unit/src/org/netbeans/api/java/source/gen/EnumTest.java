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

import java.util.Arrays;
import java.util.List;
import java.io.*;
import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.*;
import org.netbeans.junit.NbTestSuite;
import static org.netbeans.api.java.source.JavaSource.*;

/**
 * Test enum modifications.
 * 
 * @author Pavel Flaska
 */
public class EnumTest extends GeneratorTestBase {
    
    /** Creates a new instance of EnumTest */
    public EnumTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(EnumTest.class);
//        suite.addTest(new EnumTest("testConstantRename"));
//        suite.addTest(new EnumTest("testAddMethodAfterConstants"));
//        suite.addTest(new EnumTest("testRenameConstantCheckJavadoc"));
//        suite.addTest(new EnumTest("testRenameWithInit"));
//        suite.addTest(new EnumTest("testRenameMethodInEnum"));
//        suite.addTest(new EnumTest("testRenameConstantContainingBody1"));
//        suite.addTest(new EnumTest("testRenameConstantContainingBody2"));
//        suite.addTest(new EnumTest("testRenameConstantContainingBody3"));
//        suite.addTest(new EnumTest("testConstantAddition"));
//        suite.addTest(new EnumTest("testConstantAddition147590a"));
//        suite.addTest(new EnumTest("testConstantAddition147590b"));
//        suite.addTest(new EnumTest("testImplementsChange153066"));
//        suite.addTest(new EnumTest("testAddFirstMemberWithoutSemicolon"));
//        suite.addTest(new EnumTest("testAddFirstMemberWithSemicolonOneConstant"));
        return suite;
    }

    /**
     * Test renames 'A' constant to 'A2' constant in code written below:
     * 
     * <code>
     * public enum Test {
     *     A, B, C;
     *    
     *     public void enumMethod() {
     *     }
     * }
     * </code>
     */
    public void testConstantRename() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A, B, C;\n" +
            "    \n" +
            "    public void enumMethod() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A2, B, C;\n" +
            "    \n" +
            "    public void enumMethod() {\n" +
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
                        VariableTree vt = (VariableTree) clazz.getMembers().get(1);
                        VariableTree copy = make.setLabel(vt, "A2");
                        workingCopy.rewrite(vt, copy);
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
     * Test rename of last enum constant w/o semicolon on end.
     * Should produce semicolon.
     */
    public void testConstantRename2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A, B, C\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A, B, C2\n" +
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
                        ClassTree clazz = (ClassTree) typeDecl;
                        VariableTree vt = (VariableTree) clazz.getMembers().get(3);
                        VariableTree copy = make.setLabel(vt, "C2");
                        workingCopy.rewrite(vt, copy);
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
     * Test adds the method at the end of the class, demonstrates #104839
     * 
     * Original:
     * 
     * <code>
     * public enum Test {
     *     A, B, C;
     * }
     * </code>
     * 
     * Expected result:
     * 
     * <code>
     * public enum Test {
     *     A, B, C;
     *    
     *     public void enumMethod() {
     *     }
     * }
     * </code>
     */
    public void testAddMethodAfterConstants() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A, B, C;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A, B, C;\n" +
            "\n" +
            "    public void enumMethod() {\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                // create method.
                ClassTree copy = make.addClassMember(clazz, m(make));
                workingCopy.rewrite(clazz, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    private MethodTree m(TreeMaker make) {
        // create method
        MethodTree newMethod = make.Method(
            make.Modifiers( 
                Collections.singleton(Modifier.PUBLIC), // modifiers
                Collections.<AnnotationTree>emptyList() // annotations
            ), // modifiers and annotations
            "enumMethod", // name
            make.PrimitiveType(TypeKind.VOID), // return type
            Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
            Collections.<VariableTree>emptyList(), // parameters
            Collections.<ExpressionTree>emptyList(),  // throws 
            make.Block(Collections.<StatementTree>emptyList(), false), // empty statement block
            null // default value - not applicable here, used by annotations
        );
        return newMethod;
    }
    
    // #105959
    public void testRenameConstantCheckJavadoc() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
                "/*\n" +
                " * RequestType.java\n" +
                " *\n" +
                " * Created on January 6, 2007, 7:11 PM\n" +
                " */\n" +
                "\n" +
                "package quantum.protocol.version0;\n" +
                "\n" +
                "/**\n" +
                " * Protocol request types.\n" +
                " *\n" +
                " * @author Gili Tzabari\n" +
                " */\n" +
                "public enum RequestType\n" +
                "{\n" +
                "  /**\n" +
                "   * Create a new connection.\n" +
                "   */\n" +
                "  NEW_SESSION,\n" +
                "  /**\n" +
                "   * Return the id of a class.\n" +
                "   */\n" +
                "  GET_CLASS_ID,\n" +
                "  /**\n" +
                "   * Return the id of an object.\n" +
                "   */\n" +
                "  GET_OBJECT_ID,\n" +
                "  /**\n" +
                "   * Return the id of a method.\n" +
                "   */\n" +
                "  GET_METHOD_ID,\n" +
                "  /**\n" +
                "   * Invoke a method.\n" +
                "   */\n" +
                "  INVOKE_METHOD,\n" +
                "  /**\n" +
                "   * Retrieves a remote resource.\n" +
                "   */\n" +
                "  GET_RESOURCE;\n" +
                "}"
            );
        String golden =
                "/*\n" +
                " * RequestType.java\n" +
                " *\n" +
                " * Created on January 6, 2007, 7:11 PM\n" +
                " */\n" +
                "\n" +
                "package quantum.protocol.version0;\n" +
                "\n" +
                "/**\n" +
                " * Protocol request types.\n" +
                " *\n" +
                " * @author Gili Tzabari\n" +
                " */\n" +
                "public enum RequestType\n" +
                "{\n" +
                "  /**\n" +
                "   * Create a new connection.\n" +
                "   */\n" +
                "  NEW_SESSION_2,\n" +
                "  /**\n" +
                "   * Return the id of a class.\n" +
                "   */\n" +
                "  GET_CLASS_ID,\n" +
                "  /**\n" +
                "   * Return the id of an object.\n" +
                "   */\n" +
                "  GET_OBJECT_ID,\n" +
                "  /**\n" +
                "   * Return the id of a method.\n" +
                "   */\n" +
                "  GET_METHOD_ID,\n" +
                "  /**\n" +
                "   * Invoke a method.\n" +
                "   */\n" +
                "  INVOKE_METHOD,\n" +
                "  /**\n" +
                "   * Retrieves a remote resource.\n" +
                "   */\n" +
                "  GET_RESOURCE;\n" +
                "}";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree vt = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(vt, make.setLabel(vt, "NEW_SESSION_2"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Test adds the method at the end of the class, demonstrates #104839
     * 
     * Original:
     * 
     * <code>
     * public enum Test {
     *     A(1), B(2), C(3);
     * 
     *     public Test(int i) {
     *     }
     * }
     * </code>
     * 
     * Expected result:
     * 
     * <code>
     * public enum Test {
     *     A(1), B2(2), C(3);
     *    
     *     public Test(int i) {
     *     }
     * }
     * </code>
     */
    public void testRenameWithInit() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A(1), B(2), C(3);\n" +
            "\n" +
            "    public Test(int i) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A(1), B2(2), C(3);\n" +
            "\n" +
            "    public Test(int i) {\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree vt = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(vt, make.setLabel(vt, "B2"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Demonstrates #106932
     * 
     * Original:
     * 
     * <code>
     * public enum BugEnum {
     *   VALUE1{
     *     public String doSomeTest(){
     *       return "value1";
     *     }    
     *   }
     *   ,
     *   VALUE2{
     *     public String doSomeTest(){
     *       return "value2";
     *     }
     *   };
     *   
     *   public String doSomeTest(){
     *     return null;
     *   }
     * }
     * </code>
     * 
     * Expected result:
     * 
     * <code>
     * public enum Test {
     * public enum BugEnum {
     *   VALUE1{
     *     public String delejNecoHovado(){
     *       return "value1";
     *     }    
     *   }
     *   ,
     *   VALUE2{
     *     public String delejNecoHovado(){
     *       return "value2";
     *     }
     *   };
     *   
     *   public String delejNecoHovado(){
     *     return null;
     *   }
     * }
     * </code>
     */
    public void testRenameMethodInEnum() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "public enum BugEnum {\n" +
            "  VALUE1{\n" +
            "    public String doSomeTest(){\n" +
            "      return \"value1\";\n" +
            "    }    \n" +
            "  }\n" +
            "  ,\n" +
            "  VALUE2{\n" +
            "    public String doSomeTest(){\n" +
            "      return \"value2\";\n" +
            "    }\n" +
            "  };\n" +
            "  \n" +
            "  public String doSomeTest(){\n" +
            "    return null;\n" +
            "  }\n" +
            "}\n"
            );
        String golden =
            "public enum BugEnum {\n" +
            "  VALUE1{\n" +
            "    public String delejNecoHovado(){\n" +
            "      return \"value1\";\n" +
            "    }    \n" +
            "  }\n" +
            "  ,\n" +
            "  VALUE2{\n" +
            "    public String delejNecoHovado(){\n" +
            "      return \"value2\";\n" +
            "    }\n" +
            "  };\n" +
            "  \n" +
            "  public String delejNecoHovado(){\n" +
            "    return null;\n" +
            "  }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                // method in VALUE1
                VariableTree vt = (VariableTree) clazz.getMembers().get(1);
                NewClassTree nct = ((NewClassTree) vt.getInitializer());
                MethodTree method = (MethodTree) nct.getClassBody().getMembers().get(1);
                workingCopy.rewrite(method, make.setLabel(method, "delejNecoHovado"));
                
                // method in VALUE2
                vt = (VariableTree) clazz.getMembers().get(2);
                nct = ((NewClassTree) vt.getInitializer());
                method = (MethodTree) nct.getClassBody().getMembers().get(1);
                workingCopy.rewrite(method, make.setLabel(method, "delejNecoHovado"));
                
                method = (MethodTree) clazz.getMembers().get(3);
                workingCopy.rewrite(method, make.setLabel(method, "delejNecoHovado"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * Demonstrates #106932
     * 
     * Original:
     * 
     * <code>
     * public enum BugEnum {
     *   VALUE1{
     *     public String doSomeTest(){
     *       return "value1";
     *     }    
     *   }
     *   ,
     *   VALUE2{
     *     public String doSomeTest(){
     *       return "value2";
     *     }
     *   };
     *   
     *   public String doSomeTest(){
     *     return null;
     *   }
     * }
     * </code>
     * 
     * Expected result:
     * 
     * <code>
     * public enum Test {
     * public enum BugEnum {
     *   VALUE_F{
     *     public String doSomeTest(){
     *       return "value1";
     *     }    
     *   }
     *   ,
     *   VALUE2{
     *     public String doSomeTest(){
     *       return "value2";
     *     }
     *   };
     *   
     *   public String doSomeTest(){
     *     return null;
     *   }
     * }
     * </code>
     */
    public void testRenameConstantContainingBody1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "public enum BugEnum {\n" +
            "  VALUE1 {\n" +
            "    public String doSomeTest(){\n" +
            "      return \"value1\";\n" +
            "    }    \n" +
            "  }\n" +
            "  ,\n" +
            "  VALUE2{\n" +
            "    public String doSomeTest(){\n" +
            "      return \"value2\";\n" +
            "    }\n" +
            "  };\n" +
            "  \n" +
            "  public String doSomeTest(){\n" +
            "    return null;\n" +
            "  }\n" +
            "}\n"
            );
        String golden =
            "public enum BugEnum {\n" +
            "  VALUE_F {\n" +
            "    public String doSomeTest(){\n" +
            "      return \"value1\";\n" +
            "    }    \n" +
            "  }\n" +
            "  ,\n" +
            "  VALUE2{\n" +
            "    public String doSomeTest(){\n" +
            "      return \"value2\";\n" +
            "    }\n" +
            "  };\n" +
            "  \n" +
            "  public String doSomeTest(){\n" +
            "    return null;\n" +
            "  }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                // VALUE1 -> VALUE_F
                VariableTree vt = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(vt, make.setLabel(vt, "VALUE_F"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Demonstrates #106932
     * 
     * Original:
     * 
     * <code>
     * public enum BugEnum {
     *   VALUE1() {
     *     public String doSomeTest(){
     *       return "value1";
     *     }    
     *   }
     *   ,
     *   VALUE2{
     *     public String doSomeTest(){
     *       return "value2";
     *     }
     *   };
     *   
     *   public String doSomeTest(){
     *     return null;
     *   }
     * }
     * </code>
     * 
     * Expected result:
     * 
     * <code>
     * public enum Test {
     * public enum BugEnum {
     *   VALUE_F() {
     *     public String doSomeTest(){
     *       return "value1";
     *     }    
     *   }
     *   ,
     *   VALUE2{
     *     public String doSomeTest(){
     *       return "value2";
     *     }
     *   };
     *   
     *   public String doSomeTest(){
     *     return null;
     *   }
     * }
     * </code>
     */
    public void testRenameConstantContainingBody2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "public enum BugEnum {\n" +
            "  VALUE1() {\n" +
            "    public String doSomeTest(){\n" +
            "      return \"value1\";\n" +
            "    }    \n" +
            "  }\n" +
            "  ,\n" +
            "  VALUE2{\n" +
            "    public String doSomeTest(){\n" +
            "      return \"value2\";\n" +
            "    }\n" +
            "  };\n" +
            "  \n" +
            "  public String doSomeTest(){\n" +
            "    return null;\n" +
            "  }\n" +
            "}\n"
            );
        String golden =
            "public enum BugEnum {\n" +
            "  VALUE_F() {\n" +
            "    public String doSomeTest(){\n" +
            "      return \"value1\";\n" +
            "    }    \n" +
            "  }\n" +
            "  ,\n" +
            "  VALUE2{\n" +
            "    public String doSomeTest(){\n" +
            "      return \"value2\";\n" +
            "    }\n" +
            "  };\n" +
            "  \n" +
            "  public String doSomeTest(){\n" +
            "    return null;\n" +
            "  }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                // VALUE1 -> VALUE_F
                VariableTree vt = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(vt, make.setLabel(vt, "VALUE_F"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Demonstrates #106932
     * 
     * Original:
     * 
     * <code>
     * public enum BugEnum {
     *   VALUE1() {
     *     public String doSomeTest(){
     *       return "value1";
     *     }    
     *   }
     *   ,
     *   VALUE2{
     *     public String doSomeTest(){
     *       return "value2";
     *     }
     *   };
     *   
     *   public String doSomeTest(){
     *     return null;
     *   }
     * }
     * </code>
     * 
     * Expected result:
     * 
     * <code>
     * public enum Test {
     * public enum BugEnum {
     *   VALUE1() {
     *     public String doSomeTest(){
     *       return "value1";
     *     }    
     *   }
     *   ,
     *   VALUE22{
     *     public String doSomeTest(){
     *       return "value2";
     *     }
     *   };
     *   
     *   public String doSomeTest(){
     *     return null;
     *   }
     * }
     * </code>
     */
    public void testRenameConstantContainingBody3() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "public enum BugEnum {\n" +
            "  VALUE1() {\n" +
            "    public String doSomeTest(){\n" +
            "      return \"value1\";\n" +
            "    }    \n" +
            "  }\n" +
            "  ,\n" +
            "  VALUE2{\n" +
            "    public String doSomeTest(){\n" +
            "      return \"value2\";\n" +
            "    }\n" +
            "  };\n" +
            "  \n" +
            "  public String doSomeTest(){\n" +
            "    return null;\n" +
            "  }\n" +
            "}\n"
            );
        String golden =
            "public enum BugEnum {\n" +
            "  VALUE1() {\n" +
            "    public String doSomeTest(){\n" +
            "      return \"value1\";\n" +
            "    }    \n" +
            "  }\n" +
            "  ,\n" +
            "  VALUE22{\n" +
            "    public String doSomeTest(){\n" +
            "      return \"value2\";\n" +
            "    }\n" +
            "  };\n" +
            "  \n" +
            "  public String doSomeTest(){\n" +
            "    return null;\n" +
            "  }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                // VALUE1 -> VALUE_F
                VariableTree vt = (VariableTree) clazz.getMembers().get(2);
                workingCopy.rewrite(vt, make.setLabel(vt, "VALUE22"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testConstantAddition() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A, B, C;\n" +
            "    \n" +
            "    public void enumMethod() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A, D, B, C;\n" +
            "    \n" +
            "    public void enumMethod() {\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                //int mods = java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL | java.lang.reflect.Modifier.STATIC;
                int mods =  1<<14;
                ModifiersTree modifiers = make.Modifiers(mods, Collections.<AnnotationTree>emptyList());
                VariableTree newConstant = make.Variable(modifiers, "D", make.Identifier("Test"), null);
                workingCopy.rewrite(clazz, make.insertClassMember(clazz, 2, newConstant));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testConstantAddition147590a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A;\n" +
            "    \n" +
            "    public void enumMethod() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A, D;\n" +
            "    \n" +
            "    public void enumMethod() {\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                //int mods = java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL | java.lang.reflect.Modifier.STATIC;
                int mods =  1<<14;
                ModifiersTree modifiers = make.Modifiers(mods, Collections.<AnnotationTree>emptyList());
                VariableTree newConstant = make.Variable(modifiers, "D", make.Identifier("Test"), null);
                workingCopy.rewrite(clazz, make.insertClassMember(clazz, 2, newConstant));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testConstantAddition147590b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A, D\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                //int mods = java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL | java.lang.reflect.Modifier.STATIC;
                int mods =  1<<14;
                ModifiersTree modifiers = make.Modifiers(mods, Collections.<AnnotationTree>emptyList());
                VariableTree newConstant = make.Variable(modifiers, "D", make.Identifier("Test"), null);
                workingCopy.rewrite(clazz, make.insertClassMember(clazz, 2, newConstant));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testConstantRename143435() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A,\n" +
            "    B,\n" +
            "    C\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A2,\n" +
            "    B,\n" +
            "    C\n" +
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
                        VariableTree vt = (VariableTree) clazz.getMembers().get(1);
                        VariableTree copy = make.setLabel(vt, "A2");
                        workingCopy.rewrite(vt, copy);
                    }
                }
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testImplementsChange153066() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "public class Test {\n" +
            "    public enum Test {\n"+//implements Runnable {\n" +
            "        A, B, C;\n" +
            "        public void run() {}\n" +
            "    }\n}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\nimport java.lang.Object;\n\n" +
            "public class Test {\n" +
            "    public enum Test {\n"+//implements Runnable {\n" +
            "        A, B, C;\n" +
            "        public void run() {}\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                final TreeMaker make = workingCopy.getTreeMaker();

                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitVariable(VariableTree node, Void p) {
                        Element type = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), node.getType()));

                        if (type != null) {
                            workingCopy.rewrite(node.getType(), make.Identifier("Test.Test"));
                            return null;
                        } else {
                            return super.visitVariable(node, p);
                        }
                    }
                }.scan(cut, null);

                //ensure the whole file is rewritten:
                workingCopy.rewrite(cut, make.addCompUnitImport(cut, make.Import(make.Identifier("java.lang.Object"), false)));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test122461() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "public enum Test {\n"+
            "    A, B, C;\n" +
            "    int var;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "public enum Test {\n"+
            "    D, B, C;\n" +
            "    int var;\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree en = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree c = (VariableTree) en.getMembers().get(1);
                VariableTree nue = make.Variable(c.getModifiers(), "D", c.getType(), c.getInitializer());
                
                workingCopy.rewrite(c, nue);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    //duplicates "testAddMethodAfterConstants":
//    public void testAddFirstMemberWithSemicolon() throws Exception {
//        testAddFirstMember("package hierbas.del.litoral;\n" +
//                           "public enum Test {\n" +
//                           "    A, B, C;\n" +
//                           "}\n",
//                           "package hierbas.del.litoral;\n" +
//                           "public enum Test {\n" +
//                           "    A, B, C;\n\n" +
//                           "    public void run() {\n" +
//                           "    }\n" +
//                           "}\n");
//    }

    public void testAddFirstMemberWithoutSemicolon() throws Exception {
        testAddFirstMember("package hierbas.del.litoral;\n" +
                           "public enum Test {\n" +
                           "    A, B, C\n" +
                           "}\n",
                           "package hierbas.del.litoral;\n" +
                           "public enum Test {\n" +
                           "    A, B, C;\n\n" +
                           "    public void run() {\n" +
                           "    }\n" +
                           "}\n");
    }

    public void testAddFirstMemberWithComma() throws Exception {
        testAddFirstMember("package hierbas.del.litoral;\n" +
                           "public enum Test {\n" +
                           "    A, B, C,\n" +
                           "}\n",
                           "package hierbas.del.litoral;\n" +
                           "public enum Test {\n" +
                           "    A, B, C,;\n\n" +
                           "    public void run() {\n" +
                           "    }\n" +
                           "}\n");
    }

    public void testAddFirstMemberWithCommaAndSemicolon() throws Exception {
        testAddFirstMember("package hierbas.del.litoral;\n" +
                           "public enum Test {\n" +
                           "    A, B, C,;\n" +
                           "}\n",
                           "package hierbas.del.litoral;\n" +
                           "public enum Test {\n" +
                           "    A, B, C,;\n\n" +
                           "    public void run() {\n" +
                           "    }\n" +
                           "}\n");
    }

    public void testAddFirstMemberWithSemicolonOneConstant() throws Exception {
        testAddFirstMember("package hierbas.del.litoral;\n" +
                           "public enum Test {\n" +
                           "    A;\n" +
                           "}\n",
                           "package hierbas.del.litoral;\n" +
                           "public enum Test {\n" +
                           "    A;\n\n" +
                           "    public void run() {\n" +
                           "    }\n" +
                           "}\n");
    }

    public void testAddFirstMemberWithoutSemicolonOneConstant() throws Exception {
        testAddFirstMember("package hierbas.del.litoral;\n" +
                           "public enum Test {\n" +
                           "    A\n" +
                           "}\n",
                           "package hierbas.del.litoral;\n" +
                           "public enum Test {\n" +
                           "    A;\n\n" +
                           "    public void run() {\n" +
                           "    }\n" +
                           "}\n");
    }

    public void testAddFirstMemberWithCommaOneConstant() throws Exception {
        testAddFirstMember("package hierbas.del.litoral;\n" +
                           "public enum Test {\n" +
                           "    A,\n" +
                           "}\n",
                           "package hierbas.del.litoral;\n" +
                           "public enum Test {\n" +
                           "    A,;\n\n" +
                           "    public void run() {\n" +
                           "    }\n" +
                           "}\n");
    }

    public void testAddFirstMemberWithCommaAndSemicolonOneConstant() throws Exception {
        testAddFirstMember("package hierbas.del.litoral;\n" +
                           "public enum Test {\n" +
                           "    A,;\n" +
                           "}\n",
                           "package hierbas.del.litoral;\n" +
                           "public enum Test {\n" +
                           "    A,;\n\n" +
                           "    public void run() {\n" +
                           "    }\n" +
                           "}\n");
    }

    private void testAddFirstMember(String code, String golden) throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, code);
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                final TreeMaker make = workingCopy.getTreeMaker();

                ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
                Tree ret = make.Type(workingCopy.getTypes().getNoType(TypeKind.VOID));
                MethodTree mt = make.Method(mods, "run", ret, Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), "{}", null);

                ClassTree en = (ClassTree) cut.getTypeDecls().get(0);
                workingCopy.rewrite(en, make.addClassMember(en, mt));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test195500() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "public enum Test {\n" +
            "    @Deprecated\n"+
            "    Issue4;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "public enum Test {\n"+
            "    @Deprecated\n"+
            "    AAA;\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree en = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree c = (VariableTree) en.getMembers().get(1);

                workingCopy.rewrite(c, make.setLabel(c, "AAA"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test187766a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "public class Test {\n"+
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "public class Test {\n\n"+
            "    public enum E {\n" +
            "        A, B, C\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                List<VariableTree> constants = new LinkedList<VariableTree>();

                for (String name : Arrays.asList("A", "B", "C")) {
                    //int mods = java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL | java.lang.reflect.Modifier.STATIC;
                    int mods =  1<<14;
                    ModifiersTree modifiers = make.Modifiers(mods, Collections.<AnnotationTree>emptyList());
                    
                    constants.add(make.Variable(modifiers, name, make.Identifier("E"), null));
                }

                int mods =  1<<14 | 1 << 0;
                ModifiersTree modifiers = make.Modifiers(mods, Collections.<AnnotationTree>emptyList());
                ClassTree nueClazz = make.Class(modifiers, "E", Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), constants);

                workingCopy.rewrite(clazz, make.addClassMember(clazz, nueClazz));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test187766b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "public class Test {\n"+
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "public enum Test {\n"+
            "    A, B, C\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                List<VariableTree> constants = new LinkedList<VariableTree>();

                for (String name : Arrays.asList("A", "B", "C")) {
                    //int mods = java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL | java.lang.reflect.Modifier.STATIC;
                    int mods =  1<<14;
                    ModifiersTree modifiers = make.Modifiers(mods, Collections.<AnnotationTree>emptyList());

                    constants.add(make.Variable(modifiers, name, make.Identifier("Tests"), null));
                }

                int mods =  1<<14 | 1 << 0;
                ModifiersTree modifiers = make.Modifiers(mods, Collections.<AnnotationTree>emptyList());
                ClassTree nueClazz = make.Class(modifiers, "Test", Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), constants);

                workingCopy.rewrite(clazz, nueClazz);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test187766c() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "public class Test {\n"+
            "    private int ii = 0;\n"+
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "public enum Test {\n"+
            "    A, B, C;\n" +
            "    private int ii = 0;\n"+
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                List<Tree> constants = new LinkedList<Tree>();

                for (String name : Arrays.asList("A", "B", "C")) {
                    //int mods = java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL | java.lang.reflect.Modifier.STATIC;
                    int mods =  1<<14;
                    ModifiersTree modifiers = make.Modifiers(mods, Collections.<AnnotationTree>emptyList());

                    constants.add(make.Variable(modifiers, name, make.Identifier("Tests"), null));
                }

                constants.add(clazz.getMembers().get(1));

                int mods =  1<<14 | 1 << 0;
                ModifiersTree modifiers = make.Modifiers(mods, Collections.<AnnotationTree>emptyList());
                ClassTree nueClazz = make.Class(modifiers, "Test", Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), constants);

                workingCopy.rewrite(clazz, nueClazz);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test187766d() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "public class Test {\n"+
            "    private int ii = 0;\n"+
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "public enum Test {\n"+
            "    A, B, C;\n" +
            "    private int ii = 0;\n"+
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                List<Tree> constants = new LinkedList<Tree>();

                for (String name : Arrays.asList("A", "B", "C")) {
                    //int mods = java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL | java.lang.reflect.Modifier.STATIC;
                    int mods =  1<<14;
                    ModifiersTree modifiers = make.Modifiers(mods, Collections.<AnnotationTree>emptyList());

                    constants.add(make.Variable(modifiers, name, make.Identifier("Tests"), null));
                }

                constants.add(make.Variable(make.Modifiers(EnumSet.of(Modifier.PRIVATE)), "ii", make.PrimitiveType(TypeKind.INT), make.Literal(0)));

                int mods =  1<<14 | 1 << 0;
                ModifiersTree modifiers = make.Modifiers(mods, Collections.<AnnotationTree>emptyList());
                ClassTree nueClazz = make.Class(modifiers, "Test", Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), constants);

                workingCopy.rewrite(clazz, nueClazz);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test218318() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "public enum Test {\n"+
            "    A {\n" +
            "    };\n"+
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "public enum Test {\n"+
            "    A {\n" +
            "        public void run() {\n"+
            "        }\n"+
            "    };\n"+
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override public Void visitClass(ClassTree node, Void p) {
                        if (node.getSimpleName().length() == 0) {
                            MethodTree method = make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC)),
                                                            "run",
                                                            make.PrimitiveType(TypeKind.VOID),
                                                            Collections.<TypeParameterTree>emptyList(),
                                                            Collections.<VariableTree>emptyList(),
                                                            Collections.<ExpressionTree>emptyList(),
                                                            "{}",
                                                            null);
                            
                            workingCopy.rewrite(node, make.addClassMember(node, method));
                        }
                        return super.visitClass(node, p);
                    }
                }.scan(cut, null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Adds only a method to the enum
     */
    public void test237816CreateMethods() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "public enum Test {\n"+
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "public enum Test {\n"+
            "\n" +
            "    ;\n" +
            "\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                List<Tree> constants = new LinkedList<Tree>();

                MethodTree mt = make.Method(
                    make.Modifiers(EnumSet.of(Modifier.PUBLIC)), 
                    "run", make.Type("void"), Collections.<TypeParameterTree>emptyList(), 
                    Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), 
                    make.Block(Collections.<StatementTree>emptyList(), false), null);

                constants.add(mt);

                int mods =  1<<14 | 1 << 0;
                ModifiersTree modifiers = make.Modifiers(mods, Collections.<AnnotationTree>emptyList());
                ClassTree nueClazz = make.Class(modifiers, "Test", Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), constants);

                workingCopy.rewrite(clazz, nueClazz);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test237816CreateConstAndMethod() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "public enum Test {\n"+
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "public enum Test {\n"+
            "    A;\n" +
            "\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                List<Tree> constants = new LinkedList<Tree>();

                int mods =  1<<14;
                ModifiersTree modifiers = make.Modifiers(mods, Collections.<AnnotationTree>emptyList());
                constants.add(make.Variable(modifiers, "A", make.Identifier("Tests"), null)); // NOI18N
                
                MethodTree mt = make.Method(
                    make.Modifiers(EnumSet.of(Modifier.PUBLIC)), 
                    "run", make.Type("void"), Collections.<TypeParameterTree>emptyList(), 
                    Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), 
                    make.Block(Collections.<StatementTree>emptyList(), false), null);

                constants.add(mt);

                modifiers = make.Modifiers(mods | 1, Collections.<AnnotationTree>emptyList());
                ClassTree nueClazz = make.Class(modifiers, "Test", Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), constants);

                workingCopy.rewrite(clazz, nueClazz);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test237816AddConstToMethods() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "public enum Test {\n"+
            "\n" +
            "    ;\n" +
            "\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "public enum Test {\n"+
            "    A;\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                List<Tree> constants = new LinkedList<Tree>();

                int mods =  1<<14;
                ModifiersTree modifiers = make.Modifiers(mods, Collections.<AnnotationTree>emptyList());
                constants.add(make.Variable(modifiers, "A", make.Identifier("Tests"), null)); // NOI18N
                
                Tree mt = clazz.getMembers().get(1);

                constants.add(mt);

                modifiers = make.Modifiers(mods | 1, Collections.<AnnotationTree>emptyList());
                ClassTree nueClazz = make.Class(modifiers, "Test", Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), constants);

                workingCopy.rewrite(clazz, nueClazz);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test237816RemoveLastConst() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "public enum Test {\n"+
            "    A;\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "public enum Test {\n"+
            "\n" +
            "    ;\n" +
            "\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                List<Tree> constants = new LinkedList<Tree>();

                Tree mt = clazz.getMembers().get(2);

                constants.add(mt);

                ModifiersTree modifiers = make.Modifiers(1 << 14 | 1, Collections.<AnnotationTree>emptyList());
                ClassTree nueClazz = make.Class(modifiers, "Test", Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), constants);

                workingCopy.rewrite(clazz, nueClazz);
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
