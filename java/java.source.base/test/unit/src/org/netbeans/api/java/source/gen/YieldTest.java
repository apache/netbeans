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

import com.sun.source.tree.*;
import com.sun.source.util.TreePathScanner;
import java.io.File;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;

/**
 * Test modifications in the yield statement.
 *
 * @author Pavel Flaska
 */
public class YieldTest extends GeneratorTestMDRCompat {

    /** Creates a new instance of TryTest */
    public YieldTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(YieldTest.class);
        return suite;
    }

    /**
     * Change yield expression.
     */
    public void testChangeYieldExpression() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                """
                package hierbas.del.litoral;

                public class Test {
                    public int taragui(int param) {
                        return switch (param) {
                            default -> {
                                yield 0;
                            }
                        };
                    }
                }
                """);
        String golden =
                """
                package hierbas.del.litoral;

                public class Test {
                    public int taragui(int param) {
                        return switch (param) {
                            default -> {
                                yield param++;
                            }
                        };
                    }
                }
                """;
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();

                new TreePathScanner<>() {
                    @Override
                    public Object visitYield(YieldTree node, Object p) {
                        workingCopy.rewrite(node, make.Yield(make.Unary(Tree.Kind.POSTFIX_INCREMENT, make.Identifier("param"))));
                        return super.visitYield(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddYield() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                """
                package hierbas.del.litoral;

                public class Test {
                    public int taragui(int param) {
                        return switch (param) {
                            default -> {
                            }
                        };
                    }
                }
                """);
        String golden =
                """
                package hierbas.del.litoral;

                public class Test {
                    public int taragui(int param) {
                        return switch (param) {
                            default -> {
                                yield 0;
                            }
                        };
                    }
                }
                """;
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();

                new TreePathScanner<>() {
                    @Override
                    public Object visitCase(CaseTree node, Object p) {
                        BlockTree body = (BlockTree) node.getBody();
                        workingCopy.rewrite(body, make.addBlockStatement(body, make.Yield(make.Literal(0))));
                        return super.visitCase(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        testSource.runModificationTask(task).commit();
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
