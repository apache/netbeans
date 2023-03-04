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
import java.io.*;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Pavel Flaska
 */
public class AnonymousClassTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of AnonymousClassTest 
     * @param name 
     */
    public AnonymousClassTest(String name) {
        super(name);
    }
    
    /**
     * @return 
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(AnonymousClassTest.class);
//        suite.addTest(new AnonymousClassTest("testAddMethodToInvocParam"));
//        suite.addTest(new AnonymousClassTest("testAddInsideAnnWhenAnnInPar"));
          return suite;
    }
    
    /**
     * #96364: When completing NewClassTree parameter in invocation,
     * nothing is generated.
     * Example:
     *
     * method(new Runnable| ); 
     * 
     * should be completed to
     * 
     * method(new Runnable {
     *            public void run() {
     *            }
     *        });
     */
    public void testAddMethodToInvocParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "class Test {\n" +
            "    void method(Runnable r) {\n" +
            "        method(new Runnable() {});\n" +
            "    }" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "class Test {\n" +
            "    void method(Runnable r) {\n" +
            "        method(new Runnable() {\n" +
            "            public void run() {\n" +
            "            }\n" +
            "        });\n" +
            "    }}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                
                ClassTree testClass = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) testClass.getMembers().get(1);
                
                ExpressionStatementTree est = (ExpressionStatementTree) method.getBody().getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                NewClassTree nct = (NewClassTree) mit.getArguments().get(0);
                MethodTree m = make.Method(
                    make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)),
                    "run",
                    make.PrimitiveType(TypeKind.VOID),
                    Collections.<TypeParameterTree>emptyList(),
                    Collections.<VariableTree>emptyList(),
                    Collections.<ExpressionTree>emptyList(),
                    make.Block(Collections.<StatementTree>emptyList(), false),
                    null
                );
                workingCopy.rewrite(nct.getClassBody(), make.addClassMember(nct.getClassBody(), m));
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Deva issue.
     * Example:
     * 
     * public class Main {
     *     void m(Runnable r) {
     *     }
     *
     *     void method() {
     *         m(new Runnable() {
     *             public void run() {
     *                 Object o = null;
     *                 String s = o;
     *             }
     *         });
     *     }
     * }
     * 
     * When statement is changed, e.g. 'String s = o;' is changed
     * to 'String s = (String) o;', it is not replaced in the source.
     */
    public void testAddInsideAnnWhenAnnInPar() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "public class Main {\n" +
            "    void m(Runnable r) {\n" +
            "    }\n" +
            "    \n" +
            "    void method() {\n" +
            "        m(new Runnable() {\n" +
            "            public void run() {\n" +
            "                Object o = null;\n" +
            "                String s = o;\n" +
            "            }\n" +
            "        });\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package javaapplication1;\n" +
            "\n" +
            "public class Main {\n" +
            "    void m(Runnable r) {\n" +
            "    }\n" +
            "    \n" +
            "    void method() {\n" +
            "        m(new Runnable() {\n" +
            "            public void run() {\n" +
            "                Object o = null;\n" +
            "                String s = (String) o;\n" +
            "            }\n" +
            "        });\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                
                ClassTree testClass = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) testClass.getMembers().get(2);
                BlockTree block = method.getBody();
                ExpressionStatementTree est = (ExpressionStatementTree) block.getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                NewClassTree nct = (NewClassTree) mit.getArguments().get(0);
                ClassTree clazzTree = nct.getClassBody();
                method = (MethodTree) clazzTree.getMembers().get(1);
                VariableTree vt = (VariableTree) method.getBody().getStatements().get(1);
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
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
}
