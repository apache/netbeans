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
import java.io.IOException;

import com.sun.source.tree.CompilationUnitTree;

import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

import org.netbeans.junit.NbTestSuite;

/**
 * Makes source changes in broken sources.
 * 
 * @author Pavel Flaska
 */
public class BrokenSourceTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of BrokenSourceTest 
     
     * @param name  test name
     */
    public BrokenSourceTest(String name) {
        super(name);
    }
    
    /**
     * Return suite.
     * 
     * @return  suite
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(BrokenSourceTest.class);
//        suite.addTest(new BrokenSourceTest("testAddImportWhenClosingCurlyMissing"));
        return suite;
    }

    /**
     * Regression test for #97901.
     * 
     * @throws java.lang.Exception 
     */
    public void testAddImportWhenClosingCurlyMissing() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class User {\n" +
            "\n" +
            "    public User(Object node) {\n" +
            "        if (node instanceof Object) {\n" +
            "        } else if (node instanceof ArrayList)\n" +
            "        } else if (node instanceof LinkedList) {\n" +
            "        }\n" +
            "    }\n" +
            "    \n" +
            "    void method() {\n" +
            "        System.err.println(\"nafink\");\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "\n" +
            "public class User {\n" +
            "\n" +
            "    public User(Object node) {\n" +
            "        if (node instanceof Object) {\n" +
            "        } else if (node instanceof ArrayList)\n" +
            "        } else if (node instanceof LinkedList) {\n" +
            "        }\n" +
            "    }\n" +
            "    \n" +
            "    void method() {\n" +
            "        System.err.println(\"nafink\");\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "}\n";
        
        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree cutCopy = make.addCompUnitImport(
                       cut, 
                       make.Import(make.Identifier("java.util.ArrayList"), false)
                );
                workingCopy.rewrite(cut, cutCopy);
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
