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
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.parsing.JavacParser;

/**
 * Tests conversion of invalid var type variable to explicit array type.
 *
 * @author arusinha
 */
public class InvalidVarToExplicitArrayConversionTest extends TreeRewriteTestBase {

    public InvalidVarToExplicitArrayConversionTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(InvalidVarToExplicitArrayConversionTest.class);
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

    public void testInvalidVarToExplicitArrayConversion() throws Exception {

        String code = "package test;\n"
                + "public class Test {\n"
                + "    void m1() {\n"
                + "        final /*comment1*/ var/*comment2*/ k = {1, 'C'};\n"
                + "    }\n"
                + "}\n";
        String golden = "package test;\n"
                + "public class Test {\n"
                + "    void m1() {\n"
                + "        final /*comment1*/ int[]/*comment2*/ k = {1, 'C'};\n"
                + "    }\n"
                + "}\n";

        prepareTest("Test", code);

        rewriteStatement("int");
        String res = TestUtilities.copyFileToString(getTestFile());
        //System.err.println(res);
        assertEquals(golden, res);

    }

    public void testInvalidVarToExplicitArray2Conversion() throws Exception {

        String code = "package test;\n"
                + "import java.util.ArrayList;\n"
                + "public class Test {\n"
                + "    void m1() {\n"
                + "        /*comment1*/var k = {new ArrayList(), new ArrayList()};\n"
                + "    }\n"
                + "}\n";

        String golden = "package test;\n"
                + "import java.util.ArrayList;\n"
                + "public class Test {\n"
                + "    void m1() {\n"
                + "        /*comment1*/ArrayList[] k = {new ArrayList(), new ArrayList()};\n"
                + "    }\n"
                + "}\n";

        prepareTest("Test", code);

        rewriteStatement("ArrayList");
        String res = TestUtilities.copyFileToString(getTestFile());
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testInvalidVarToExplicitArray3Conversion() throws Exception {
        String code = "package test;\n"
                + "import java.util.ArrayList;\n"
                + "public class Test {\n"
                + "    void m1() {\n"
                + "        @NotNull final var j = {\"hello\", \"world\"};\n"
                + "    }\n"
                + "}\n";
        String golden = "package test;\n"
                + "import java.util.ArrayList;\n"
                + "public class Test {\n"
                + "    void m1() {\n"
                + "        @NotNull final String[] j = {\"hello\", \"world\"};\n"
                + "    }\n"
                + "}\n";

        prepareTest("Test", code);

        rewriteStatement("String");
        String res = TestUtilities.copyFileToString(getTestFile());
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testInvalidVarToExplicitArray4Conversion() throws Exception {

        String code = "package test;\n"
                + "public class Test {\n"
                + "    void m1() {\n"
                + "        var/*comment2*/ k = {1, 'C'};\n"
                + "    }\n"
                + "}\n";
        String golden = "package test;\n"
                + "public class Test {\n"
                + "    void m1() {\n"
                + "        int[]/*comment2*/ k = {1, 'C'};\n"
                + "    }\n"
                + "}\n";

        prepareTest("Test", code);

        rewriteStatement("int");
        String res = TestUtilities.copyFileToString(getTestFile());
        //System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * Replaces invalid var type array initialization statement with explicit
     * array type.
     *
     * @param arrayType : target explicit array type.
     * @throws IOException
     */
    private void rewriteStatement(String arrayType) throws IOException {

        JavaSource js = getJavaSource();
        assertNotNull(js);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);

                VariableTree oldVariableTree = (VariableTree) method.getBody().getStatements().get(0);
                VariableTree newVariableTree = make.Variable(
                        oldVariableTree.getModifiers(),
                        oldVariableTree.getName(),
                        make.ArrayType(make.Type(arrayType)),
                        oldVariableTree.getInitializer()
                );
                // converts var type to explicit array type
                workingCopy.rewrite(oldVariableTree, newVariableTree);
            }
        };
        js.runModificationTask(task).commit();
    }

}
