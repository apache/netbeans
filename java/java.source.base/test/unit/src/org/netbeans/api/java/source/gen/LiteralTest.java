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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import javax.lang.model.SourceVersion;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;

/**
 * Test modifications of literal trees
 * 
 * @author Pavel Flaska
 * @author Jan Lahoda
 */
public class LiteralTest extends GeneratorTestMDRCompat {
    
    public LiteralTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(LiteralTest.class);
        return suite;
    }

    /**
     * Rewrite null literal to itself.
     */ 
    public void testNullLiteral43625() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui(Object o) {\n" +
            "        taragui(null);\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui(Object o) {\n" +
            "        taragui(null);\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree statement = (ExpressionStatementTree) method.getBody().getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) statement.getExpression();
                LiteralTree lt = (LiteralTree) mit.getArguments().get(0);
                workingCopy.rewrite(lt, make.Literal(null));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testNoExtraEscapesInStringLiteral() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public static final String C;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public static final String C = \"'\";\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();

                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                LiteralTree val = make.Literal("'");
                VariableTree nue = make.setInitialValue(var, val);
                workingCopy.rewrite(var, nue);
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testNoExtraEscapesInCharLiteral() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public static final char C;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public static final char C = '\"';\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();

                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                LiteralTree val = make.Literal('"');
                VariableTree nue = make.setInitialValue(var, val);
                workingCopy.rewrite(var, nue);
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testTextBlocksNew() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_13");
        } catch (IllegalArgumentException ex) {
            //OK, skip test:
            return ;
        }
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public static final String C;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public static final String C = \"\"\"\n" +
            "                                   line \"1\n" +
            "                                   line\\\"\\\"\\\"\"\"2\"\n" +
            "                                   line\\n3\"\"\";\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();

                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                LiteralTree val = make.Literal(new String[] {"line \"1",
                                                             "line\"\"\"\"\"2\"",
                                                             "line\n3"});
                VariableTree nue = make.setInitialValue(var, val);
                workingCopy.rewrite(var, nue);
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testTextBlocksReplace() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public static final String C = \"\"\"\n" +
            "                                   old\"\"\";\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public static final String C = \"\"\"\n" +
            "                                   new\n" +
            "                                   \"\"\";\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();

                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                LiteralTree val = make.Literal(new String[] {"new",
                                                             ""});
                VariableTree nue = make.setInitialValue(var, val);
                workingCopy.rewrite(var, nue);
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

}
