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
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.net.URL;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.indexing.TransactionContext;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests conversion of invalid 'var' type variable with explicit array type
 *
 * @author arusinha
 */
public class InvalidVarToExplicitArrayConversionTest extends GeneratorTestBase {

    private static final String SOURCE_LEVEL = "1.10";  // NOI18N

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
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
        super.setUp();
        TestUtilities.analyzeBinaries(SourceUtilsTestUtil.getBootClassPath());
        Main.initializeURLFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = false;
        for (URL bootCP : SourceUtilsTestUtil.getBootClassPath()) {
            TransactionContext ctx = TransactionContext.beginStandardTransaction(bootCP, false, () -> false, false);
            try {
                ClassIndexManager.getDefault().removeRoot(bootCP);
            } finally {
                ctx.commit();
            }
        }
        super.tearDown();
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

        prepareTest(code);

        RewriteStatement("int");
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
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

        prepareTest(code);

        RewriteStatement("ArrayList");
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
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

        prepareTest(code);

        RewriteStatement("String");
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    protected void prepareTest(String code) throws Exception {

        FileObject workFO = FileUtil.toFileObject(getWorkDir());

        assertNotNull(workFO);

        FileObject sourceRoot = FileUtil.createFolder(workFO, "src");
        FileObject buildRoot = workFO.createFolder("build");
        FileObject data = FileUtil.createData(sourceRoot, "Test.java");

        testFile = FileUtil.toFile(data);
        assertNotNull(testFile);
        TestUtilities.copyStringToFile(testFile, code);

        SourceUtilsTestUtil.setSourceLevel(data, SOURCE_LEVEL);

        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, CacheFolder.getCacheFolder(), new FileObject[0]);

        //re-index, in order to find classes-living-elsewhere
        IndexingManager.getDefault().refreshIndexAndWait(sourceRoot.getURL(), null);
    }

    /**
     *
     * @param arrayType : target explicit array type.
     * @throws IOException
     */
    void RewriteStatement(String arrayType) throws IOException {

        JavaSource js = getJavaSource(getTestFile());
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

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

}
