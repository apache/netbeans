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
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author Ralph Ruijs
 */
public class RewriteMultipleExpressionsTest extends GeneratorTestBase {
    
    
    public RewriteMultipleExpressionsTest(String aName) {
        super(aName);
    }
    
    @Test
    public void testRewriteMultipleExpressions() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "\n" +
                        "public class MultipleExpressionsTest {\n" +
                        "    public void testMethod() {\n" +
                        "        printGreeting();\n" +
                        "        printGreeting();\n" +
                        "        printGreeting();\n" +
                        "    }\n" +
                        "    public void printGreeting() {\n" +
                        "        System.out.println(\"Hello World!\");\n" +
                        "    }\n" +
                        "}\n");
        String golden = "\n" +
                "public class MultipleExpressionsTest {\n" +
                "    public void testMethod() {\n" +
                "        System.out.println(\"Hello World!\");\n" +
                "        System.out.println(\"Hello World!\");\n" +
                "        System.out.println(\"Hello World!\");\n" +
                "    }\n" +
                "    public void printGreeting() {\n" +
                "        System.out.println(\"Hello World!\");\n" +
                "    }\n" +
                "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                
                List<? extends Tree> classes = cut.getTypeDecls();
                ClassTree clazz = (ClassTree) classes.get(0);
                List<? extends Tree> trees = clazz.getMembers();
                
                MethodTree testMethod = (MethodTree) trees.get(1);
                BlockTree body = testMethod.getBody();
                
                MethodTree printMethod = (MethodTree) trees.get(2);
                BlockTree printBody = printMethod.getBody();
                
                List<StatementTree> statements = new LinkedList<StatementTree>();
                statements.add(printBody.getStatements().get(0));
                statements.add(printBody.getStatements().get(0));
                statements.add(printBody.getStatements().get(0));
                
                BlockTree modified = make.Block(statements, false);
                
                workingCopy.rewrite(body, modified);
            }

        };

        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
        assertEquals(golden, res);
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
}
