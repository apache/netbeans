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
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.tools.javac.tree.JCTree;
import java.io.File;
import java.io.IOException;
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
public class InstanceOfTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of AddCastTest */
    public InstanceOfTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(InstanceOfTest.class);
        return suite;
    }

    public void testAddVariableName() throws Exception {
        if (!typeTestPatternAvailable())
            return ;

        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(Object o) {\n" +
            "        if (o instanceof String) {\n" + 
            "        }\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(Object o) {\n" +
            "        if (o instanceof String t) {\n" +
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
                IfTree ift = (IfTree) method.getBody().getStatements().get(0);
                InstanceOfTree it = (InstanceOfTree) ((ParenthesizedTree) ift.getCondition()).getExpression();
                InstanceOfTree nue = make.InstanceOf(it.getExpression(), make.BindingPattern("t", it.getType()));
                workingCopy.rewrite(it, nue);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveVariableName() throws Exception {
        if (!typeTestPatternAvailable())
            return ;

        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(Object o) {\n" +
            "        if (o instanceof String t) {\n" + 
            "        }\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(Object o) {\n" +
            "        if (o instanceof String) {\n" +
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
                IfTree ift = (IfTree) method.getBody().getStatements().get(0);
                InstanceOfTree it = (InstanceOfTree) ((ParenthesizedTree) ift.getCondition()).getExpression();
                InstanceOfTree nue = make.InstanceOf(it.getExpression(), it.getType());
                workingCopy.rewrite(it, nue);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddInstanceOf() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(Object o) {\n" +
            "        if (true) {\n" + 
            "        }\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(Object o) {\n" +
            "        if (o instanceof String) {\n" +
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
                IfTree ift = (IfTree) method.getBody().getStatements().get(0);
                ExpressionTree condition = ((ParenthesizedTree) ift.getCondition()).getExpression();
                InstanceOfTree nue = make.InstanceOf(make.Identifier("o"), make.QualIdent("java.lang.String"));
                workingCopy.rewrite(condition, nue);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    private boolean typeTestPatternAvailable() {
        try {
            Class.forName("com.sun.source.tree.BindingPatternTree", false, JCTree.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ex) {
            //OK
            return false;
        }
    }

    String getGoldenPckg() {
        return "";
    }
    
    String getSourcePckg() {
        return "";
    }
    
    
}
