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
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.List;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.parsing.JavacParser;

/**
 * 
 * @author vkprabha
 */
public class VarCompoundDeclarationTest extends TreeRewriteTestBase {

    public VarCompoundDeclarationTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(VarCompoundDeclarationTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sourceLevel = "1.10";
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = false;

    }

    public void testVarCompoundDeclaration1() throws Exception {

        String code = "package test;\n"
                + "public class Test {\n"
                + "    private void test() {\n"
                + "        final var x = 10, y = 11;\n"
                + "    }\n"
                + "}\n";
        String golden = "package test;\n"
                + "public class Test {\n"
                + "    private void test() {\n"
                + "        final var x = 10;\n"
                + "        final var y = 11;\n"
                + "    }\n"
                + "}\n";

        prepareTest("Test", code);

        rewriteBlockStatement();
        String res = TestUtilities.copyFileToString(getTestFile());
        //System.err.println(res);
        assertEquals(golden, res);

    }

    public void testVarCompoundDeclaration2() throws Exception {

        String code = "package test;\n"
                + "public class Test {\n"
                + "    private void test() {\n"
                + "        @DA final var v = 1, v1 = 10;\n"
                + "    }\n"
                + "}\n";

        String golden = "package test;\n"
                + "public class Test {\n"
                + "    private void test() {\n"
                + "        @DA\n"
                + "        final var v = 1;\n"
                + "        @DA\n"
                + "        final var v1 = 10;\n"
                + "    }\n"
                + "}\n";

        prepareTest("Test", code);

        rewriteBlockStatement();
        String res = TestUtilities.copyFileToString(getTestFile());
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testVarCompoundDeclaration3() throws Exception {
        String code = "package test;\n"
                + "public class Test {\n"
                + "    private void test() {\n"
                + "        int i = 1;\n"
                + "        switch(i){\n"
                + "            case 1:\n"
                + "            var v = 1, v1 = 10;\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        String golden = "package test;\n"
                + "public class Test {\n"
                + "    private void test() {\n"
                + "        int i = 1;\n"
                + "        switch(i){\n"
                + "            case 1:\n"
                + "            var v = 1;\n"
                + "            var v1 = 10;\n\n"
                + "        }\n"
                + "    }\n"
                + "}\n";

        prepareTest("Test", code);

        rewriteCaseStatement();
        String res = TestUtilities.copyFileToString(getTestFile());
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testVarCompoundDeclaration4() throws Exception {

        String code = "package test;\n"
                + "public class Test {\n"
                + "    private void test() {\n"
                + "        int i = 1;\n"
                + "        switch(i){\n"
                + "            case 1:\n"
                + "             final var v = 1, v1 = 10;\n"
                + "             break;\n"
                + "            }\n"
                + "    }\n"
                + "}\n";
        String golden = "package test;\n"
                + "public class Test {\n"
                + "    private void test() {\n"
                + "        int i = 1;\n"
                + "        switch(i){\n"
                + "            case 1:\n"
                + "             final var v = 1;\n"
                + "             final var v1 = 10;\n"
                + "             break;\n\n"
                + "            }\n"
                + "    }\n"
                + "}\n";

        prepareTest("Test", code);

        rewriteCaseStatement();
        String res = TestUtilities.copyFileToString(getTestFile());
        //System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * Fix compound variable declaration in block statement.
     * 
     * @throws IOException
     */
    private void rewriteBlockStatement() throws IOException {

        JavaSource js = getJavaSource();
        assertNotNull(js);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);

                List<? extends StatementTree> statements = ((BlockTree) method.getBody()).getStatements();

                for (int current = 0; current < statements.size(); current++) {
                    StatementTree t = (StatementTree) statements.get(current);
                    if (t instanceof VariableTree) {
                        VariableTree oldVariableTree = (VariableTree) t;
                        VariableTree newVariableTree = make.Variable(
                                oldVariableTree.getModifiers(),
                                oldVariableTree.getName(),
                                make.Type("var"), // NOI18N
                                oldVariableTree.getInitializer()
                        );
                        workingCopy.rewrite(oldVariableTree, newVariableTree);

                    }

                }
            }
        };

        js.runModificationTask(task).commit();
    }
    
    /**
     * Fix compound variable declaration in switch-case statement.
     * array type.
     * @throws IOException
     */
    private void rewriteCaseStatement() throws IOException {

        JavaSource js = getJavaSource();
        assertNotNull(js);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                SwitchTree st = (SwitchTree) method.getBody().getStatements().get(1);
                
                CaseTree ct = st.getCases().get(0);
                List<? extends StatementTree> statements = ct.getStatements();
                        
                for (int current = 0; current < statements.size(); current++) {
                    StatementTree t = (StatementTree) statements.get(current);
                    if (t instanceof VariableTree) {
                        VariableTree oldVariableTree = (VariableTree) t;
                        VariableTree newVariableTree = make.Variable(
                                oldVariableTree.getModifiers(),
                                oldVariableTree.getName(),
                                make.Type("var"), // NOI18N
                                oldVariableTree.getInitializer()
                        );
                        workingCopy.rewrite(oldVariableTree, newVariableTree);

                    }

                }
            }
        };

        js.runModificationTask(task).commit();
    }

}
