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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import java.io.IOException;

import java.util.Collections;
import java.util.EnumSet;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

import org.netbeans.junit.NbTestSuite;

/**
 * Tests imports matching and its correct adding/removing. Just generator
 * test, does not do anything with import analysis.
 * 
 * @author Pavel Flaska
 */
public class ImportsTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of MethodParametersTest */
    public ImportsTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(ImportsTest.class);
//        suite.addTest(new ImportsTest("testAddFirst"));
//        suite.addTest(new ImportsTest("testAddFirstAgain"));
//        suite.addTest(new ImportsTest("testAddSecondImport"));
//        suite.addTest(new ImportsTest("testAddSecondImportWithEndLineCmt"));
//        suite.addTest(new ImportsTest("testAddTwoImportsOrigWithComment"));
//        suite.addTest(new ImportsTest("testAddBetweenImports"));
//        suite.addTest(new ImportsTest("testRemoveBetweenImportsWithLineEndComment"));
//        suite.addTest(new ImportsTest("testRemoveAllImports"));
//        suite.addTest(new ImportsTest("testRemoveAllImports2"));
//        suite.addTest(new ImportsTest("testAddFirstTwoAgain"));
//        suite.addTest(new ImportsTest("testAddFirstTwo"));
//        suite.addTest(new ImportsTest("testAddFirstToExisting"));
//        suite.addTest(new ImportsTest("testRemoveInnerImport"));
//        suite.addTest(new ImportsTest("testEmptyLines"));
//        suite.addTest(new ImportsTest("testIndentedImport"));
//        suite.addTest(new ImportsTest("testIndentedImport2"));
//        suite.addTest(new ImportsTest("testUnformatted"));
//        suite.addTest(new ImportsTest("testMissingNewLine"));
//        suite.addTest(new ImportsTest("testRemoveAllInDefault"));
//        suite.addTest(new ImportsTest("testRemoveAllInDefault2"));
//        suite.addTest(new ImportsTest("testRemoveAfterEmpty"));
//        suite.addTest(new ImportsTest("testRemoveBeforeEmpty"));
//        suite.addTest(new ImportsTest("testRenameIdentifier"));
//        suite.addTest(new ImportsTest("testRenameIdentifier2"));
//        suite.addTest(new ImportsTest("testAtVeryBeginning"));
//        suite.addTest(new ImportsTest("testPackageInfo"));
//        suite.addTest(new ImportsTest("testRemoveAndAdd"));
//        suite.addTest(new ImportsTest("test166524a"));
//        suite.addTest(new ImportsTest("test166524b"));
//        suite.addTest(new ImportsTest("test166524c"));
        return suite;
    }

    public void testAddFirst() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.IOException;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.addCompUnitImport(
                       node,
                       make.Import(make.Identifier("java.io.IOException"), false)
                );
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddFirstAgain() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.IOException;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.addCompUnitImport(
                        node,
                        make.Import(make.Identifier("java.io.IOException"), false)
                );
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddFirstToExisting() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.lang.NullPointerException;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.IOException;\n" +
            "import java.lang.NullPointerException;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.insertCompUnitImport(
                        node,
                        0,
                        make.Import(make.Identifier("java.io.IOException"), false)
                );
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddFirstTwo() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException;\n" +
            "import java.util.List;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.addCompUnitImport(
                        node,
                        make.Import(make.Identifier("java.io.IOException"), false)
                );
                copy = make.addCompUnitImport(
                        copy,
                        make.Import(make.Identifier("java.util.List"), false)
                );
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddFirstTwoAgain() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "/** javadoc comment */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException;\n" +
            "import java.util.List;\n\n" +
            "/** javadoc comment */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.addCompUnitImport(
                        node,
                        make.Import(make.Identifier("java.io.IOException"), false)
                );
                copy = make.addCompUnitImport(
                        copy,
                        make.Import(make.Identifier("java.util.List"), false)
                );
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddSecondImport() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.IOException;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException;\n" +
            "import java.util.List;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.addCompUnitImport(
                        node,
                        make.Import(make.Identifier("java.util.List"), false)
                );
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }

    public void testAddSecondImportWithEndLineCmt() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException; // aa\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException; // aa\n" +
            "import java.util.List;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.addCompUnitImport(
                        node,
                        make.Import(make.Identifier("java.util.List"), false)
                );
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddTwoImportsOrigWithComment() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException; // yerba mate\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException; // yerba mate\n" +
            "import java.util.List;\n" +
            "import java.util.Collections;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.addCompUnitImport(
                        node,
                        make.Import(make.Identifier("java.util.List"), false)
                );
                copy = make.addCompUnitImport(
                        copy,
                        make.Import(make.Identifier("java.util.Collections"), false)
                );
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddBetweenImports() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.IOException; // yerba mate\n" +
            "import java.util.List;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.IOException; // yerba mate\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.List;\n" +
            "import java.util.LinkedList;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.insertCompUnitImport(
                        node, 1,
                        make.Import(make.Identifier("java.util.ArrayList"), false)
                );
                copy = make.insertCompUnitImport(
                        copy, 3,
                        make.Import(make.Identifier("java.util.LinkedList"), false)
                );
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveBetweenImportsWithLineEndComment() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException;\n" +
            "import java.util.ArrayList; // polovy seznam\n" +
            "import java.util.List; // yerba mate\n" +
            "import java.util.LinkedList;\n" +
            "import java.util.Collections;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException;\n" +
            "import java.util.ArrayList; // polovy seznam\n" +
            "import java.util.LinkedList;\n" +
            "import java.util.Collections;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(node, 2);
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveInnerImport() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.List;\n" +
            "import java.util.LinkedList;\n" +
            "import java.util.Collections;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.IOException;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.LinkedList;\n" +
            "import java.util.Collections;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(node, 2);
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveImportsNoEOL() throws Exception {
        testFile = new File(getWorkDir(), "MoveClass.java");
        TestUtilities.copyStringToFile(testFile,
                "package movepkg;"
                + " "
                + "import movepkg.*;"
                + " "
                + "public class MoveClass {"
                + "    "
                + "    public MoveClass() {"
                + "        super();"
                + "    }"
                + "}");
        String golden = "package movepkg;"
                + ""
                + "public class MoveClass {"
                + "    "
                + "    public MoveClass() {"
                + "        super();"
                + "    }"
                + "}";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.CompilationUnit(node.getPackageAnnotations(), node.getPackageName(), new java.util.LinkedList<ImportTree>(), node.getTypeDecls(), node.getSourceFile());
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveAllImports() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.ArrayList; // polovy seznam\n" +
            "import java.util.List; // yerba mate\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(node, 0);
                copy = make.removeCompUnitImport(copy, 0);
                copy = make.removeCompUnitImport(copy, 0);
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveAllImports2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.ArrayList; // polovy seznam\n" +
            "import java.util.List; // yerba mate\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(node, 0);
                copy = make.removeCompUnitImport(copy, 0);
                copy = make.removeCompUnitImport(copy, 0);
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testUnformatted() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;" +
            "import java.util.ArrayList; // polovy seznam\n" +
            "import java.util.List; // yerba mate\n" +
            "import java.util.Collections;" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;" +
            "import java.util.List; // yerba mate\n" +
            "import java.util.Collections;" +
            "import static java.util.Arrays;\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(node, 0);
                copy = make.addCompUnitImport(copy, make.Import(make.Identifier("java.util.Arrays"), true));
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void XtestEmptyLines() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "\n" +
            "import java.util.List;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.LinkedList;\n" +
            "\n" +
            "import java.util.List;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ImportTree novyDovoz = make.Import(make.Identifier("java.util.LinkedList"), false);
                CompilationUnitTree copy = make.insertCompUnitImport(cut, 1, novyDovoz);
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testIndentedImport() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "    import java.util.ArrayList;\n" +
            "\n" +
            "import java.util.List;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "\n" +
            "import java.util.List;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(cut, 0);
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testIndentedImport2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "import java.util.List;\n" +
            "    import java.util.ArrayList;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "import java.util.List;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(cut, 1);
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testMissingNewLine() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "import java.util.List;\n" +
            "import java.util.ArrayList;import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "import java.util.List;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(cut, 1);
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveAllInDefault() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "import java.util.List;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(cut, 0);
                copy = make.removeCompUnitImport(copy, 0);
                copy = make.removeCompUnitImport(copy, 0);
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveAllInDefault2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "import java.util.List;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.Collections; // test\n" +
            "/** test */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "/** test */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(cut, 0);
                copy = make.removeCompUnitImport(copy, 0);
                copy = make.removeCompUnitImport(copy, 0);
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveAfterEmpty() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "import java.util.List;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.Collections; // test\n" +
            "/** test */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "import java.util.List;\n" +
            "\n" +
            "import java.util.Collections; // test\n" +
            "/** test */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(cut, 1);
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveBeforeEmpty() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "import java.util.List;\n" +
            "import java.util.ArrayList;\n" +
            "\n" +
            "import java.util.Collections; // test\n" +
            "/** test */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "import java.util.List;\n" +
            "\n" +
            "import java.util.Collections; // test\n" +
            "/** test */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.removeCompUnitImport(cut, 1);
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRenameIdentifier() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "import java.util.List;\n" +
            "import java.util.ArrayList;\n" +
            "\n" +
            "import java.util.Collections; // test\n" +
            "/** test */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "import java.util.List;\n" +
            "import java.util.ArrayList;\n" +
            "\n" +
            "import java.util.Jitko; // test\n" +
            "/** test */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ImportTree dovoz = cut.getImports().get(2);
                MemberSelectTree mst = (MemberSelectTree) dovoz.getQualifiedIdentifier();
                workingCopy.rewrite(mst, make.setLabel(mst, "Jitko"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRenameIdentifier2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "import java.util.List;\n" +
            "import java.util.ArrayList;\n" +
            "\n" +
            "import java.util.Collections; // test\n" +
            "/** test */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "import java.util.List;\n" +
            "import java.util.ArrayList;\n" +
            "\n" +
            "import java.jitko.Collections; // test\n" +
            "/** test */\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ImportTree dovoz = cut.getImports().get(2);
                MemberSelectTree mst = (MemberSelectTree) dovoz.getQualifiedIdentifier();
                mst = (MemberSelectTree) mst.getExpression();
                workingCopy.rewrite(mst, make.setLabel(mst, "jitko"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * http://www.netbeans.org/issues/show_bug.cgi?id=100162
     */
    public void testAtVeryBeginning() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "\n" +
            "import java.io.IOException;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.addCompUnitImport(
                       node,
                       make.Import(make.Identifier("java.io.IOException"), false)
                );
                workingCopy.rewrite(node, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * http://www.netbeans.org/issues/show_bug.cgi?id=103429
     */
//    public void testPackageInfo() throws Exception {
//        testFile = new File(getWorkDir(), "package-info.java");
//        TestUtilities.copyStringToFile(testFile,
//            "@XmlSchema(namespace = \"urn:aaa\")\n" +
//            "package javaapplication2;\n"
//            );
//        String golden =
//            "@XmlSchema(namespace = \"urn:aaa\")\n" +
//            "package javaapplication2;\n" +
//            "\n" +
//            "import javax.xml.bind.annotation.XmlSchema;\n" +
//            "\n";
//
//        JavaSource src = getJavaSource(testFile);
//        Task<WorkingCopy> task = new Task<WorkingCopy>() {
//
//            public void run(WorkingCopy workingCopy) throws IOException {
//                workingCopy.toPhase(Phase.RESOLVED);
//                TreeMaker make = workingCopy.getTreeMaker();
//                CompilationUnitTree node = workingCopy.getCompilationUnit();
//                CompilationUnitTree copy = make.addCompUnitImport(
//                       node,
//                       make.Import(make.Identifier("javax.xml.bind.annotation.XmlSchema"), false)
//                );
//                workingCopy.rewrite(node, copy);
//            }
//
//        };
//        src.runModificationTask(task).commit();
//        String res = TestUtilities.copyFileToString(testFile);
//        //System.err.println(res);
//        assertEquals(golden, res);
//    }

    public void test138100() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "public class Test {\n" +
            "    public void test1() {\n" +
            "    }\n" +
            "    public void test2() {\n" +
            "    }\n" +
            "    public Test() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden = "\n" +
            "import java.util.LinkedList;\n" +
            "import java.util.List;\n\n" +
            "public class Test {\n" +
            "    public void test1() {\n" +
            "        List test;\n" +
            "    }\n" +
            "    public void test2() {\n" +
            "        LinkedList test;\n" +
            "    }\n" +
            "    public Test() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                TypeElement juList = workingCopy.getElements().getTypeElement("java.util.List");
                BlockTree block1 = ((MethodTree) ((ClassTree) node.getTypeDecls().get(0)).getMembers().get(0)).getBody();
                VariableTree var1 = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "test", make.QualIdent(juList), null);
                BlockTree nueBlock1 = workingCopy.getTreeMaker().addBlockStatement(block1, var1);
                workingCopy.rewrite(block1, nueBlock1);
                TypeElement juLinkedList = workingCopy.getElements().getTypeElement("java.util.LinkedList");
                BlockTree block2 = ((MethodTree) ((ClassTree) node.getTypeDecls().get(0)).getMembers().get(1)).getBody();
                VariableTree var2 = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "test", make.QualIdent(juLinkedList), null);
                BlockTree nueBlock2 = workingCopy.getTreeMaker().addBlockStatement(block2, var2);
                workingCopy.rewrite(block2, nueBlock2);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test137771() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "import java.util.LinkedList;\n" +
            "import java.util.List;//tttt\n" +
            "/*asdf\n" +
            " */\n" +
            "/**\n" +
            " */\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden = 
            "import java.util.LinkedList;\n" +
            "/*asdf\n" +
            " */\n" +
            "/**\n" +
            " */\n" +
            "public class Test {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                workingCopy.rewrite(node, make.removeCompUnitImport(node, 1));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveAndAdd() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "import java.lang.annotation.RetentionPolicy;\n" +
            "import static java.lang.annotation.RetentionPolicy.*;\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "import java.lang.annotation.RetentionPolicy;\n" +
            "public class Test {\n" +
            "\n" +
            "    RetentionPolicy p;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);

                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree nue = workingCopy.getTreeMaker().CompilationUnit(cut.getPackageName(), Collections.<ImportTree>emptyList(), cut.getTypeDecls(), cut.getSourceFile());
                
                workingCopy.rewrite(cut, nue);

                ClassTree ct = (ClassTree) cut.getTypeDecls().get(0);
                ExpressionTree type = make.QualIdent(workingCopy.getElements().getTypeElement("java.lang.annotation.RetentionPolicy"));
                VariableTree var = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "p",type, null);
                ClassTree nueCT = make.addClassMember(ct,var);

                workingCopy.rewrite(ct, nueCT);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test166524a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package test;\n" +
            "import org.jdesktop.layout.GroupLayout;\n" +
            "import org.jdesktop.layout.Baseline;\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package test;\n" +
            "import org.jdesktop.layout.Baseline;\n" +
            "import org.jdesktop.layout.GroupLayout;\n" +
            "public class Test {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                ImportTree first = node.getImports().get(0);
                CompilationUnitTree nue = make.addCompUnitImport(make.removeCompUnitImport(node, first), first);
                workingCopy.rewrite(node, nue);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test166524b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package test;\n" +
            "import org.jdesktop.layout.Baseline;\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package test;\n" +
            "import static org.jdesktop.layout.GroupLayout.CENTER;\n" +
            "public class Test {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                ImportTree first = node.getImports().get(0);
                ImportTree nue = make.Import(make.Identifier("org.jdesktop.layout.GroupLayout.CENTER"), true);
                workingCopy.rewrite(first, nue);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test166524c() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package test;\n" +
            "import java.awt.List;\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package test;\n" +
            "import java.util.List;\n" +
            "public class Test {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                ImportTree first = node.getImports().get(0);
                ImportTree nue = make.Import(make.MemberSelect(make.MemberSelect(make.Identifier("java"), "util"), "List"), false);
                workingCopy.rewrite(first, nue);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testDiffAddWithModuleImport() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package test;\n" +
            "import module java.base;\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package test;\n" +
            "import module java.base;\n" +
            "import java.util.List;\n" +
            "public class Test {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                ImportTree nueImport = make.Import(make.MemberSelect(make.MemberSelect(make.Identifier("java"), "util"), "List"), false);
                CompilationUnitTree nueNode = make.addCompUnitImport(node, nueImport);
                workingCopy.rewrite(node, nueNode);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddModuleImport() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                                       """
                                       package test;
                                       public class Test {
                                       }
                                       """);
        String golden =
            """
            package test;

            import module java.base;

            public class Test {
            }
            """;

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                ImportTree nueImport = make.ImportModule(make.QualIdent("java.base"));
                CompilationUnitTree nueNode = make.addCompUnitImport(node, nueImport);
                workingCopy.rewrite(node, nueNode);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveModuleImport() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                                       """
                                       package test;
                                       import module java.base;
                                       public class Test {
                                       }
                                       """);
        String golden =
            """
            package test;
            public class Test {
            }
            """;

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                CompilationUnitTree nueNode = make.removeCompUnitImport(node, 0);
                workingCopy.rewrite(node, nueNode);
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
