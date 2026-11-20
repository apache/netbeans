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

import java.io.File;
import java.io.IOException;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.*;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.junit.NbTestSuite;

public class ClassToRecordDiffTest extends GeneratorTestMDRCompat {

    private String sourceLevel;

    public ClassToRecordDiffTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(ClassToRecordDiffTest.class);
        return suite;
    }

    public void testClassToRecord() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                """
                package hierbas.del.litoral;
                public class R {
                    private int a;
                    private String b;
                    public static final int K = 1;
                    public int getA() { return a; }
                }
                """);
        String golden =
                """
                package hierbas.del.litoral;
                public record R(int a, String b) {
                    public static final int K = 1;
                    public int getA() { return a; }
                }
                """;

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                Tree classDecl = cut.getTypeDecls().get(0);
                assertEquals(Kind.CLASS, classDecl.getKind());
                ClassTree classTree = (ClassTree) classDecl;
                List<? extends Tree> members = classTree.getMembers();
                Tree k = null;
                Tree getA = null;
                for (Tree m : members) {
                    if (m.getKind() == Kind.VARIABLE && "K".contentEquals(((VariableTree) m).getName())) k = m;
                    if (m.getKind() == Kind.METHOD && "getA".contentEquals(((com.sun.source.tree.MethodTree) m).getName())) getA = m;
                }
                assertNotNull(k);
                assertNotNull(getA);
                VariableTree a = make.RecordComponent(make.Modifiers(EnumSet.noneOf(Modifier.class)),
                                                      "a",
                                                      make.Type("int"));
                VariableTree b = make.RecordComponent(make.Modifiers(EnumSet.noneOf(Modifier.class)),
                                                      "b",
                                                      make.Type("java.lang.String"));
                ClassTree newTree = make.Record(make.Modifiers(EnumSet.of(Modifier.PUBLIC)),
                                                "R",
                                                List.of(),
                                                List.of(),
                                                List.of(a, b, k, getA));
                workingCopy.rewrite(classTree, newTree);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testClassToRecordNoBody() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                """
                package hierbas.del.litoral;
                public class R {
                    private int a;
                }
                """);
        String golden =
                """
                package hierbas.del.litoral;
                public record R(int a) {}
                """;

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                Tree classDecl = cut.getTypeDecls().get(0);
                assertEquals(Kind.CLASS, classDecl.getKind());
                ClassTree classTree = (ClassTree) classDecl;
                VariableTree a = make.RecordComponent(make.Modifiers(EnumSet.noneOf(Modifier.class)),
                                                      "a",
                                                      make.Type("int"));
                ClassTree newTree = make.Record(make.Modifiers(EnumSet.of(Modifier.PUBLIC)),
                                                "R",
                                                List.of(),
                                                List.of(),
                                                List.of(a));
                workingCopy.rewrite(classTree, newTree);
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

