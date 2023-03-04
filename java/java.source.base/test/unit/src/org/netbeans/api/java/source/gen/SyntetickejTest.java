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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
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
 * Semicolon as a member is not parsed at all as of 3ff3f20471b4.
 * 
 * @author Pavel Flaska
 */
public class SyntetickejTest extends GeneratorTestMDRCompat {

    public SyntetickejTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(SyntetickejTest.class);
        return suite;
    }
    
    public void testEmptyStaticBlockSemicolon() throws Exception{
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package tohle;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    static enum Whorehouse {\n" +
            "        /** first prostitute */\n" +
            "        PrvniDevka,\n" +
            "        /** second prostitue */\n" +
            "        DruhaDevka,\n" +
            "        /** third prostitute */\n" +
            "        TretiDevka;\n" +
            "    };\n" + // the semicolon is strange here -- shouldn't be there, 
            "    \n" + // but it is correct and we have to handle such a situation
            "    void method() {\n" +
            "        Object o = null;\n" +
            "        String s = o;\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package tohle;\n" +
            "\n" +
            "public class Main {\n" +
            "\n" +
            "    static enum Whorehouse {\n" +
            "        /** first prostitute */\n" +
            "        PrvniDevka,\n" +
            "        /** second prostitue */\n" +
            "        DruhaDevka,\n" +
            "        /** third prostitute */\n" +
            "        TretiDevka;\n" +
            "    };\n" +
            "    \n" +
            "    void method() {\n" +
            "        Object o = null;\n" +
            "        String s = (String) o;\n" +
            "    }\n" +
            "}\n";
        
        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException{
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(2);
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
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

}
