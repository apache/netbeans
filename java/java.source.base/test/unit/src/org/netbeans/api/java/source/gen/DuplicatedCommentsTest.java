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
import java.util.Collections;

import org.openide.filesystems.FileUtil;

import com.sun.source.tree.*;

import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;

import org.netbeans.junit.NbTestSuite;

/**
 * Regression tests.
 * 
 * @author Pavel Flaska
 */
public class DuplicatedCommentsTest extends GeneratorTestMDRCompat {

    public DuplicatedCommentsTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(DuplicatedCommentsTest.class);
//        suite.addTest(new DuplicatedCommentsTest("testLineAtTopLevel"));
        return suite;
    }
    
    public void testLineAtTopLevela() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package tohle;\n" +
            "\n" +
            "import java.util.List;\n" +
            "\n" +
            "// TODO:\n" +
            "\n" +
            "/**\n" +
            " * Alois\n" +
            " */\n" +
            "public class NewClass {\n" +
            "    \n" +
            "    public NewClass() {\n" +
            "        List l = new ArrayList();\n" +
            "    }\n" +
            "}\n");
        String golden = 
            "package tohle;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.List;\n" +
            "\n" +
            "// TODO:\n" +
            "\n" +
            "/**\n" +
            " * Alois\n" +
            " */\n" +
            "public class NewClass {\n" +
            "    \n" +
            "    public NewClass() {\n" +
            "        List l = new ArrayList();\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                // hucky staff, correct memberSelectTree should be provided.
                // for testing reason this hacky stuff is enough.
                ImportTree importt = make.Import(make.Identifier("java.util.ArrayList"), false);
                CompilationUnitTree copy = make.insertCompUnitImport(cut, 0, importt);
                workingCopy.rewrite(cut, copy);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testLineAtTopLevelb() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package tohle;\n" +
            "\n" +
            "/**\n" +
            " * Alois\n" +
            " */\n" +
            "// XXX:\n" +
            "public class OldClass {\n" +
            "    \n" +
            "}\n");
        String golden = 
            "package tohle;\n" +
            "\n" +
            "/**\n" +
            " * Alois\n" +
            " */\n" +
            "// XXX:\n" +
            "public class NewClass {\n" +
            "    \n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                Tree type = workingCopy.getCompilationUnit().getTypeDecls().get(0);
                workingCopy.rewrite(type, make.setLabel(type, "NewClass"));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testLineAtMethoda() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package tohle;\n"
                + "\n"
                + "public class OldClass {\n"
                + "    /**\n"
                + "     * Alois\n"
                + "     */\n"
                + "    // XXX:\n"
                + "    public void foo() {\n"
                + "    }\n"
                + "}\n");
        String golden
                = "package tohle;\n"
                + "\n"
                + "public class OldClass {\n"
                + "    /**\n"
                + "     * Alois\n"
                + "     */\n"
                + "    // XXX:\n"
                + "    public void bar() {\n"
                + "    }\n"
                + "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                Tree foo = ((ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0)).getMembers().get(1);
                workingCopy.rewrite(foo, make.setLabel(foo, "bar"));
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
