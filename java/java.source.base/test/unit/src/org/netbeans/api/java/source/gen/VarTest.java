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
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.junit.NbTestSuite;

//TODO: disable source level downgrade, so that runs on JDK 8 as well:
public class VarTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of VarArgsTest */
    public VarTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(VarTest.class);
        return suite;
    }

    public void testVarToExplicitResolved1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    {\n" +
            "        final/*comment1*/var a = {new Object(),new Object()};\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    {\n" +
            "        final/*comment1*/Object[] a = {new Object(),new Object()};\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                BlockTree method = (BlockTree) clazz.getMembers().get(1);
                VariableTree localVar = (VariableTree) method.getStatements().get(0);
                VariableTree newVar = make.Variable(localVar.getModifiers(), localVar.getName(), make.Type("Object[]"), localVar.getInitializer());
                workingCopy.rewrite(localVar, newVar);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testVarToExplicitResolved2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    {\n" +
            "        final/*comment1*/var/*comment2*/ a = {new Object(),new Object()};\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    {\n" +
            "        final/*comment1*/Object[]/*comment2*/ a = {new Object(),new Object()};\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                BlockTree method = (BlockTree) clazz.getMembers().get(1);
                VariableTree localVar = (VariableTree) method.getStatements().get(0);
                VariableTree newVar = make.Variable(localVar.getModifiers(), localVar.getName(), make.Type("Object[]"), localVar.getInitializer());
                workingCopy.rewrite(localVar, newVar);
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

    @Override
    String getSourceLevel() {
        return "11";
    }

}
