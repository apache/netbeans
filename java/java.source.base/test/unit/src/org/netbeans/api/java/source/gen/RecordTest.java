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

import java.io.*;
import com.sun.source.tree.*;
import com.sun.source.tree.Tree.Kind;
import java.util.EnumSet;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.*;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.junit.NbTestSuite;

public class RecordTest extends GeneratorTestMDRCompat {

    private String sourceLevel;

    public RecordTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(RecordTest.class);
        return suite;
    }

    public void testRenameComponent() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                """
                package hierbas.del.litoral;
                public record R(String component) {}
                """);
        String golden =
                """
                package hierbas.del.litoral;
                public record R(String newName) {}
                """;

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                Tree recordDecl = cut.getTypeDecls().get(0);
                assertEquals(Kind.RECORD, recordDecl.getKind());
                ClassTree classTree = (ClassTree) recordDecl;
                for (Tree m : classTree.getMembers()) {
                    if (m.getKind() == Kind.VARIABLE) {
                        workingCopy.rewrite(m, make.setLabel(m, "newName"));
                    }
                }
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddFirstComponent() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                """
                package hierbas.del.litoral;
                public record R() {}
                """);
        String golden =
                """
                package hierbas.del.litoral;
                public record R(String component) {}
                """;

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                Tree recordDecl = cut.getTypeDecls().get(0);
                assertEquals(Kind.RECORD, recordDecl.getKind());
                ClassTree classTree = (ClassTree) recordDecl;
                VariableTree newComponent = make.RecordComponent(make.Modifiers(EnumSet.noneOf(Modifier.class)),
                                                                 "component",
                                                                 make.Type("java.lang.String"));
                ClassTree newClassTree = make.addClassMember(classTree, newComponent);
                workingCopy.rewrite(classTree, newClassTree);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddSecondComponent() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                """
                package hierbas.del.litoral;
                public record R(String existing) {}
                """);
        String golden =
                """
                package hierbas.del.litoral;
                public record R(String existing, String component) {}
                """;

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                Tree recordDecl = cut.getTypeDecls().get(0);
                assertEquals(Kind.RECORD, recordDecl.getKind());
                ClassTree classTree = (ClassTree) recordDecl;
                VariableTree newComponent = make.RecordComponent(make.Modifiers(EnumSet.noneOf(Modifier.class)),
                                                                 "component",
                                                                 make.Type("java.lang.String"));
                ClassTree newClassTree = make.addClassMember(classTree, newComponent);
                workingCopy.rewrite(classTree, newClassTree);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveLastComponent() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                """
                package hierbas.del.litoral;
                public record R(String component) {}
                """);
        String golden =
                """
                package hierbas.del.litoral;
                public record R() {}
                """;

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                Tree recordDecl = cut.getTypeDecls().get(0);
                assertEquals(Kind.RECORD, recordDecl.getKind());
                ClassTree classTree = (ClassTree) recordDecl;
                for (Tree m : classTree.getMembers()) {
                    if (m.getKind() == Kind.VARIABLE) {
                        workingCopy.rewrite(classTree, make.removeClassMember(classTree, m));
                        break;
                    }
                }
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    /** 
     * Is the compact constructor preserved?
     * Added check for #7044
     * 
     */
    public void testPreserveCompact() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                """
                package hierbas.del.litoral;
                public record R(String first, String component) {
                    public R {
                        assert null != first;
                    }
                }
                """);
        String golden =
                """
                package hierbas.del.litoral;
                public record R(String first) {
                    public R {
                        assert null != first;
                    }
                }
                """;

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                Tree recordDecl = cut.getTypeDecls().get(0);
                assertEquals(Kind.RECORD, recordDecl.getKind());
                ClassTree classTree = (ClassTree) recordDecl;
                for (Tree m : classTree.getMembers()) {
                    if (m.getKind() == Kind.VARIABLE &&
                        ((VariableTree) m).getName().contentEquals("component")) {
                        workingCopy.rewrite(classTree, make.removeClassMember(classTree, m));
                        break;
                    }
                }
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveComponent() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                """
                package hierbas.del.litoral;
                public record R(String first, String component) {}
                """);
        String golden =
                """
                package hierbas.del.litoral;
                public record R(String first) {}
                """;

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                Tree recordDecl = cut.getTypeDecls().get(0);
                assertEquals(Kind.RECORD, recordDecl.getKind());
                ClassTree classTree = (ClassTree) recordDecl;
                for (Tree m : classTree.getMembers()) {
                    if (m.getKind() == Kind.VARIABLE &&
                        ((VariableTree) m).getName().contentEquals("component")) {
                        workingCopy.rewrite(classTree, make.removeClassMember(classTree, m));
                        break;
                    }
                }
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
        return sourceLevel;
    }

}
