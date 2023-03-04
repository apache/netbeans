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
import org.netbeans.api.java.source.*;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Dusan Balek
 */
public class ModuleInfoTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of ModuleInfoTest */
    public ModuleInfoTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(ModuleInfoTest.class);
        return suite;
    }
    
    public void testRename() throws Exception {
        testFile = new File(getWorkDir(), "module-info.java");
        TestUtilities.copyStringToFile(testFile, 
                "module test {\n" +
                "}\n"
            );
        String golden =
            "module hierbas.test {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                ModuleTree moduleTree = cut.getModule();
                ExpressionTree nju = make.QualIdent("hierbas.test");
                workingCopy.rewrite(moduleTree.getName(), nju);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveAll() throws Exception {
        testFile = new File(getWorkDir(), "module-info.java");
        TestUtilities.copyStringToFile(testFile, 
                "module test {\n" +
                "\n" +
                "    requires java.base;\n" +
                "    requires java.desktop;\n" +
                "\n" +
                "    exports hierbas.del.litoral;\n" +
                "}\n"
            );
        String golden =
            "module test {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ModuleTree moduleTree = cut.getModule();
                ModuleTree nju = moduleTree;
                for (DirectiveTree tree : moduleTree.getDirectives()) {
                    nju = make.removeModuleDirective(nju, tree);
                }
                workingCopy.rewrite(moduleTree, nju);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddAtIndex0() throws Exception {
        testFile = new File(getWorkDir(), "module-info.java");
        TestUtilities.copyStringToFile(testFile, 
            "module test {\n" +
            "    exports hierbas.del.litoral;\n" +
            "}\n"
            );
        String golden =
            "module test {\n" +
            "    requires java.base;\n" +
            "    exports hierbas.del.litoral;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                ModuleTree moduleTree = cut.getModule();
                ExpressionTree name = make.QualIdent("java.base");
                ModuleTree copy = make.insertModuleDirective(moduleTree, 0, make.Requires(false, false, name));
                workingCopy.rewrite(moduleTree, copy);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddToEnd() throws Exception {
        testFile = new File(getWorkDir(), "module-info.java");
        TestUtilities.copyStringToFile(testFile, 
            "module test {\n" +
            "    requires java.base;\n" +
            "}\n"
            );
        String golden =
            "module test {\n" +
            "    requires java.base;\n" +
            "    exports hierbas.del.litoral;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ModuleTree moduleTree = cut.getModule();
                ExpressionTree pkgName = make.QualIdent("hierbas.del.litoral");
                ModuleTree copy = make.addModuleDirective(moduleTree, make.Exports(pkgName, Collections.emptyList()));
                workingCopy.rewrite(moduleTree, copy);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddToEmpty() throws Exception {
        testFile = new File(getWorkDir(), "module-info.java");
        TestUtilities.copyStringToFile(testFile, 
            "module test {\n" +
            "}\n"
            );
        String golden =
            "module test {\n" +
            "    exports hierbas.del.litoral;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                ModuleTree moduleTree = cut.getModule();
                ExpressionTree pkgName = make.QualIdent("hierbas.del.litoral");
                ModuleTree copy = make.addModuleDirective(moduleTree, make.Exports(pkgName, Collections.emptyList()));
                workingCopy.rewrite(moduleTree, copy);
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
        return "1.9";
    }    
}
