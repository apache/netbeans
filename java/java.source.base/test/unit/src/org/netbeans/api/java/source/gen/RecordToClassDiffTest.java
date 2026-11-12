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

public class RecordToClassDiffTest extends GeneratorTestMDRCompat {

    private String sourceLevel;

    public RecordToClassDiffTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(RecordToClassDiffTest.class);
        return suite;
    }

    public void testRecordToClass() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                """
                package hierbas.del.litoral;
                public record R(int a, String b) {
                    public static final int K = 1;
                    public int getA() { return a; }
                }
                """);
        String golden =
                """
                package hierbas.del.litoral;
                public final class R {

                    private final int a;
                    private final String b;

                    public R(int a, String b) {
                        this.a = a;
                        this.b = b;
                    }
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
                assertEquals(Kind.RECORD, classDecl.getKind());
                ClassTree classTree = (ClassTree) classDecl;
                List<? extends Tree> members = classTree.getMembers();
                Tree k = null;
                Tree getA = null;
                VariableTree compA = null, compB = null;
                for (Tree m : members) {
                    if (m.getKind() == Kind.VARIABLE) {
                        if ("a".contentEquals(((VariableTree) m).getName())) compA = (VariableTree) m;
                        if ("b".contentEquals(((VariableTree) m).getName())) compB = (VariableTree) m;
                        if ("K".contentEquals(((VariableTree) m).getName())) k = m;
                    }
                    if (m.getKind() == Kind.METHOD && "getA".contentEquals(((com.sun.source.tree.MethodTree) m).getName())) getA = m;
                }
                assertNotNull(k);
                assertNotNull(getA);
                assertNotNull(compA);
                assertNotNull(compB);
                VariableTree a = make.Variable(make.Modifiers(EnumSet.of(Modifier.PRIVATE, Modifier.FINAL)),
                        compA.getName(),
                        compA.getType(),
                        null);
                VariableTree b = make.Variable(make.Modifiers(EnumSet.of(Modifier.PRIVATE, Modifier.FINAL)),
                        compB.getName(),
                        compB.getType(),
                        null);
                com.sun.source.tree.MethodTree ctor = make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC)),
                          "<init>",
                          null,
                          List.of(),
                          List.of(
                              make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), compA.getName(), compA.getType(), null),
                              make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), compB.getName(), compB.getType(), null)),
                          List.of(),
                          make.Block(List.of(
                              make.ExpressionStatement(make.Assignment(make.MemberSelect(make.Identifier("this"), compA.getName()), make.Identifier(compA.getName()))),
                              make.ExpressionStatement(make.Assignment(make.MemberSelect(make.Identifier("this"), compB.getName()), make.Identifier(compB.getName())))),
                              false),
                          null);
                ClassTree newTree = make.Class(make.Modifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL)),
                                                "R",
                                                List.of(),
                                                null,
                                                List.of(),
                                                List.of(),
                                                List.of(a, b, ctor, k, getA));
                workingCopy.rewrite(classTree, newTree);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }

    public void testRecordToClassExistingConstructor() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                """
                package hierbas.del.litoral;
                public record R(int a) {
                    public R(int a) { this.a = a; }
                }
                """);
        String golden =
                """
                package hierbas.del.litoral;
                public final class R {

                    private final int a;
                    public R(int a) { this.a = a; }
                }
                """;

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                Tree classDecl = cut.getTypeDecls().get(0);
                assertEquals(Kind.RECORD, classDecl.getKind());
                ClassTree classTree = (ClassTree) classDecl;
                Tree ctor = null;
                for (Tree m : classTree.getMembers()) {
                    if (m.getKind() == Kind.METHOD) ctor = m;
                }
                assertNotNull(ctor);
                VariableTree a = make.Variable(make.Modifiers(EnumSet.of(Modifier.PRIVATE, Modifier.FINAL)),
                                               "a",
                                               make.Type("int"),
                                               null);
                ClassTree newTree = make.Class(make.Modifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL)),
                                                "R",
                                                List.of(),
                                                null,
                                                List.of(),
                                                List.of(),
                                                List.of(a, ctor));
                workingCopy.rewrite(classTree, newTree);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
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
