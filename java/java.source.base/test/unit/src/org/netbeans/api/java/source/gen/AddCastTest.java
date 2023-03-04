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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import java.io.IOException;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests correct adding cast to statement.
 *
 * @author Pavel Flaska
 */
public class AddCastTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of AddCastTest */
    public AddCastTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(AddCastTest.class);
        return suite;
    }

    public void testAddCastToDeclStmt() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> {\n" +
            "    public void taragui() {\n" +
            "        System.err.println(\"taragui() method\");\n" + 
            "        String s = \"Oven.\";\n" +
            "//         line comment\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> {\n" +
            "    public void taragui() {\n" +
            "        System.err.println(\"taragui() method\");\n" + 
            "        String s = (String) \"Oven.\";\n" +
            "//         line comment\n" + 
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
                VariableTree var = (VariableTree) method.getBody().getStatements().get(1);
                ExpressionTree init = var.getInitializer();
                ExpressionTree cast = make.TypeCast(make.Identifier("String"), init);
                workingCopy.rewrite(init, cast);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddCastInForWithoutSteps() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> {\n" +
            "    public void cast() {\n" +
            "        Object o = null;\n" +
            "        for (int i = 0; i < 5; ) {\n" +
            "            String s = o;\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> {\n" +
            "    public void cast() {\n" +
            "        Object o = null;\n" +
            "        for (int i = 0; i < 5; ) {\n" +
            "            String s = (String) o;\n" +
            "        }\n" +
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
                ForLoopTree forLoop = (ForLoopTree) method.getBody().getStatements().get(1);
                BlockTree block = (BlockTree) forLoop.getStatement();
                VariableTree vt = (VariableTree) block.getStatements().get(0);
                ExpressionTree init = vt.getInitializer();
                ExpressionTree cast = make.TypeCast(make.Identifier("String"), init);
                workingCopy.rewrite(init, cast);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /*
     * #94324
     */
    public void testAddCast94324() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication3;\n" +
            "\n" +
            "public class Klasa {\n" +
            "    \n" +
            "    static Object method() {\n" +
            "        return null;\n" +
            "    }\n" +
            "    \n" +
            "    public static void main(String[] args) {\n" +
            "        // TODO code application logic here\n" +
            "        String s = Klasa.method();\n" +
            "    }\n" +
            "\n" +
            "}\n"
        );
        String golden =
            "package javaapplication3;\n" +
            "\n" +
            "public class Klasa {\n" +
            "    \n" +
            "    static Object method() {\n" +
            "        return null;\n" +
            "    }\n" +
            "    \n" +
            "    public static void main(String[] args) {\n" +
            "        // TODO code application logic here\n" +
            "        String s = (String) Klasa.method();\n" +
            "    }\n" +
            "\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(2);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);
                ExpressionTree init = var.getInitializer();
                ExpressionTree cast = make.TypeCast(make.Identifier("String"), init);
                workingCopy.rewrite(init, cast);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddCastLiteral() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication3;\n" +
            "\n" +
            "public class Klasa {\n" +
            "    public static void main(String[] args) {\n" +
            "        int i = args.length & 0xFEFE;\n" +
            "    }\n" +
            "\n" +
            "}\n"
        );
        String golden =
            "package javaapplication3;\n" +
            "\n" +
            "public class Klasa {\n" +
            "    public static void main(String[] args) {\n" +
            "        int i = (int) (args.length & 0xFEFE);\n" +
            "    }\n" +
            "\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);
                ExpressionTree init = var.getInitializer();
                ExpressionTree cast = make.TypeCast(make.PrimitiveType(TypeKind.INT), make.Parenthesized(init));
                workingCopy.rewrite(init, cast);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    /*
     * #94324 - second test. If the moved expression is changed, it is again
     * rewritten by VeryPretty - in this time, i do not see any solution for
     * that.
     *//*
    public void testAddCast94324_2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication3;\n" +
            "\n" +
            "public class Klasa {\n" +
            "    \n" +
            "    static Object method() {\n" +
            "        return null;\n" +
            "    }\n" +
            "    \n" +
            "    public static void main(String[] args) {\n" +
            "        // TODO code application logic here\n" +
            "        String s = Klasa.method();\n" +
            "    }\n" +
            "\n" +
            "}\n"
        );
        String golden =
            "package javaapplication3;\n" +
            "\n" +
            "public class Klasa {\n" +
            "    \n" +
            "    static Object method() {\n" +
            "        return null;\n" +
            "    }\n" +
            "    \n" +
            "    public static void main(String[] args) {\n" +
            "        // TODO code application logic here\n" +
            "        String s = (String) javaapplication3.Klasa.metoda();\n" +
            "    }\n" +
            "\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(2);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);
                MethodInvocationTree init = (MethodInvocationTree) var.getInitializer();
                MethodInvocationTree initCopy = make.MethodInvocation(
                        (List<ExpressionTree>) init.getTypeArguments(),
                        make.setLabel((MemberSelectTree) init.getMethodSelect(), "metoda"),
                        init.getArguments()
                );
                workingCopy.rewrite(init, initCopy);
                ExpressionTree cast = make.TypeCast(make.Identifier("String"), initCopy);
                workingCopy.rewrite(init, cast);
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }*/
    
    String getGoldenPckg() {
        return "";
    }
    
    String getSourcePckg() {
        return "";
    }
    
    
}
